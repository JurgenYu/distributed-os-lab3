package edu.umass.toy.catalog.services;

import edu.umass.toy.catalog.models.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Service
public class CatalogService {

    // In memory stocks
    private final Map<Toys, Integer> stocks = Collections.synchronizedMap(new HashMap<>());

    // file persists on local disk
    private File file;

    // frontEnd hosts URI
    private String frontendHostURI;


    /**
     * @param name product name
     * @return QueryData
     * query product information
     */
    public QueryData query(String name) {
        try {
            // get toy object by name
            Toys toy = Toys.getToy(name);
            // return toy information
            return new QueryData(new QuerySuccessData(toy, stocks.get(toy)));
        } catch (EnumConstantNotPresentException e) {
            // catch Product Not Found exception
            return new QueryData(new QueryErrorData(HttpStatus.NOT_FOUND, "Product Not Found"));
        } catch (Exception e) {
            // catch for Internal Error
            return new QueryData(new QueryErrorData(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error"));
        }
    }


    /**
     * @param body Order Request Body
     * @return OrderData
     * update stock according to order request
     */
    public OrderData order(OrderRequestBody body) {
        try {
            // get toy object by name
            Toys toy = Toys.getToy(body.getName());
            int stockQuantity = stocks.get(toy);
            // check in stock
            if (stockQuantity >= body.getQuantity()) {
                // finish order and update stock
                stockQuantity -= body.getQuantity();
                stocks.put(toy, stockQuantity);
                updateFile();
                // request frontend to invalidate cache
                requestInvalidate(toy);
                return new OrderData(new OrderSuccessData(-1, toy.getName(), body.getQuantity()));
            } else {
                // exception for product insufficient stock
                return new OrderData(new OrderErrorData(HttpStatus.NOT_ACCEPTABLE, "Product insufficient stock"));
            }
        } catch (EnumConstantNotPresentException e) {
            e.printStackTrace();
            // exception for product not found
            return new OrderData(new OrderErrorData(HttpStatus.NOT_FOUND, "Product Not Found"));
        } catch (Exception e) {
            e.printStackTrace();
            // exception for internal error
            return new OrderData(new OrderErrorData(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error"));
        }

    }

    /**
     * reset local state, update stock file
     */
    public void reset() {
        stocks.clear();
        updateFile();
    }


    /**
     * @throws UnknownHostException UnknownHostException
     * initialize catalog service, called upon server ready
     */
    @PostConstruct
    public void init() throws UnknownHostException {
        // construct frontend host URI
        frontendHostURI = "http://" + Inet4Address.getByName(System.getenv("FRONTEND_HOST")).getHostName() + ":" + System.getenv("FRONTEND_PORT");

        // initialize stock quantity with 100
        for (Toys toy : Toys.values()) {
            this.stocks.put(toy, 100);
        }

        // get local file, create if not exists
        Path currentRelativePath = Paths.get("");
        File folder = new File(currentRelativePath.toAbsolutePath().toString() + "/data");
        System.out.println("folder: " + folder.toString());
        if (!folder.exists()) {
            folder.mkdir();
        }
        // read from local file
        this.file = new File(folder.getPath() + "/stock.csv");
        try {
            // create file
            if (this.file.createNewFile()) {
                System.out.println("Stock File created");
                updateFile();
            } else {
                // read from exist file
                System.out.println("Stock File exists");
                List<String> lines = Files.readAllLines(this.file.toPath());
                // read line from file
                for (String line : lines) {
                    String[] data = line.split(",");
                    if (data.length != 3) {
                        System.out.println("Data Invalid, skipping");
                    } else {
                        // update stock with data from local file
                        this.stocks.put(Toys.getToy(data[0]), Integer.parseInt(data[2]));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * write stock data into local file
     */
    private void updateFile() {
        try {
            FileWriter csvWriter = new FileWriter(file);
            for (Entry<Toys, Integer> entry : stocks.entrySet()) {
                String line = String.join(",", entry.getKey().getName(), entry.getKey().getPrice(), Integer.toString(entry.getValue())) + "\n";
                csvWriter.append(line);
            }
            // close file
            csvWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @param toy toy products being updated
     * request frontend service to erase cache of a modified product
     */
    private void requestInvalidate(Toys toy) {
        // construct frontend host URI
        final String url = frontendHostURI + "/invalidate/" + toy.getName();
        RestTemplate restTemplate = new RestTemplate();
        // call frontend service to get toy information
        ResponseEntity<String> res = restTemplate.getForEntity(url, String.class);
        System.out.println(res.getBody());
    }

    /**
     * restock the quantity to 100 every 10 seconds if the quantity is zero
     */
    @Scheduled(fixedDelay = 10000)
    private void restock() {
        System.out.println("Restocking");
        for (Toys toy : stocks.keySet()) {
            if (stocks.get(toy) == 0) {
                System.out.println("Restocked toy: " + toy.getName());
                stocks.put(toy, 100);
                requestInvalidate(toy);
            }
        }
    }

}
