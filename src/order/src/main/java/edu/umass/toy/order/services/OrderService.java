package edu.umass.toy.order.services;

import edu.umass.toy.order.models.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderService {
    private final ConcurrentHashMap<Integer, OrderData> orders = new ConcurrentHashMap<>();
    private File file;
    private int nextOrderNumber;
    private String catalogHostURI;
    private List<String> orderReplicasHostURIList = new ArrayList<>();
    private boolean synced;


    /**
     * @throws UnknownHostException UnknownHostException
     * @throws InterruptedException initialize order service ready
     */
    @Order(1)
    @PostConstruct
    public void init() throws UnknownHostException, InterruptedException {
        // initial next order number
        this.nextOrderNumber = 1;
        // construct catalog host URI from environment variable
        catalogHostURI = "http://" + Inet4Address.getByName(System.getenv("CATALOG_HOST")).getHostName() + ":" + System.getenv("CATALOG_PORT");
        // get local file, create if not exists
        Path currentRelativePath = Paths.get("");
        File folder = new File(currentRelativePath.toAbsolutePath().toString() + "/data");
        System.out.println("folder: " + folder.toString());
        if (!folder.exists()) {
            folder.mkdir();
        }
        // read from local file
        this.file = new File(folder.getPath() + "/orders.csv");
        try {
            // create file
            if (this.file.createNewFile()) {
                System.out.println("Orders File created");
            } else {
                // read from exist file
                System.out.println("Orders File exists");
                List<String> lines = Files.readAllLines(this.file.toPath());
                // read line from file
                for (String line : lines) {
                    System.out.println("line: " + line);
                    String[] data = line.split(",");
                    if (data.length != 3) {
                        System.out.println("Data Invalid, skipping");
                    } else {
                        int number = Integer.parseInt(data[0]);
                        String name = data[1];
                        int quantity = Integer.parseInt(data[2]);
                        // update orders with data from local file
                        orders.put(number, new OrderData(new OrderSuccessData(number, name, quantity)));
                        nextOrderNumber = number + 1;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * @throws InterruptedException helper method to re-sync orders missed during recovery
     */
    public void requestUnSyncOrderRecords() throws InterruptedException {
        // read replicas hosts from system environment variables
        findReplicas();
        Thread.sleep(1000);
        String orderRepliacHostURI = orderReplicasHostURIList.get(0);
        // find live replicat
        for (String url : orderReplicasHostURIList) {
            // check order replicas status
            if (pingPong(url)) {
                orderRepliacHostURI = url;
                System.out.println("orderRepliacHostURI: " + orderRepliacHostURI);
            }
        }
        RestTemplate restTemplate = new RestTemplate();
        // request missed orders from other replica
        List<OrderData> unsyncedOreders = restTemplate.getForEntity(orderRepliacHostURI + "/getUnsyncedOrderDataList/" + nextOrderNumber, UnsyncedOrderData.class).getBody().getOrderDataList();
        // append missed orders to data file and order cache
        if (unsyncedOreders.size() > 0) {
            for (OrderData unsyncedorder : unsyncedOreders) {
                orders.put(unsyncedorder.getData().getNumber(), unsyncedorder);
                System.out.println("order: " + unsyncedorder.getData().getNumber());
                nextOrderNumber = unsyncedorder.getData().getNumber() + 1;
            }
            updateFile();
        }
        synced = true;
    }

    /**
     * reset local state, update stock file
     */
    public void reset() {
        orders.clear();
        updateFile();
    }


    /**
     * @param body Order request body
     * @return OrderData
     * order product and request catalog service to update stock
     */
    public OrderData putOrders(@RequestBody OrderRequestBody body) {
        // check if all missed orders have been recovered after last crash
        if (!synced) {
            try {
                // re-sync orders missed during recovery
                requestUnSyncOrderRecords();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            // forward order to catalog service
            OrderData res = requestOrder(body);
            // check if order success
            if (res.getData() != null) {
                // generate new order number
                res.getData().setNumber(nextOrderNumber++);
                // record the order information
                orders.put(res.getData().getNumber(), res);

                // update data file
                updateFile();

                // sync data with other replicate
                requestSync(res);
            }
            return res;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            // catch exception for Internal Error
            return new OrderData(new OrderErrorData(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error"));
        }
    }


    /**
     * @param orderNumber order number
     * @return OrderData
     * retrieve previous orders
     */
    public OrderData getOrders(int orderNumber) {
        // check if order exists
        if (orders.containsKey(orderNumber)) {
            return orders.get(orderNumber);
        }
        // handle exception for Order Does not exist
        return new OrderData(new OrderErrorData(HttpStatus.NOT_FOUND, "Order Does not exist"));
    }


    /**
     * @param orderData order data
     * @return String
     * update local orders with order sent from leader
     */
    public String syncOrder(OrderData orderData) {
        System.out.println("orderData: " + orderData.getData().getName());
        if (orderData.getData() != null) {
            // append order info to local storage
            orders.put(orderData.getData().getNumber(), orderData);
            nextOrderNumber = orderData.getData().getNumber() + 1;
            System.out.println("nextOrderNumber: " + nextOrderNumber);
            updateFile();
        }
        return "Synced";
    }


    /**
     * @param lastNextOrderNumber last next order number
     * @return UnsyncedOrderData
     * help other replicas to get missed orders during its crash-recovery
     */
    public UnsyncedOrderData getUnsyncedOrderDataList(int lastNextOrderNumber) {
        List<OrderData> list = new ArrayList<>();
        // iterate from other service's last order number to its order number to find out missed orders
        while (lastNextOrderNumber < nextOrderNumber) {
            list.add(orders.get(lastNextOrderNumber));
            lastNextOrderNumber++;
        }
        return new UnsyncedOrderData(list);
    }

    /**
     * scheduled task that re-sync orders if it has not been synced after starting
     */
    @Scheduled(fixedDelay = 500)
    private void checkSynced() {
        if (!synced) {
            try {
                requestUnSyncOrderRecords();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * @param body Order request body
     * @return OrderData
     * @throws UnknownHostException helper method for forwarding order to catalog service
     */
    private OrderData requestOrder(OrderRequestBody body) throws UnknownHostException {
        // construct catalog endpoint
        final String url = catalogHostURI + "/orders";
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForObject(url, body, OrderData.class);
    }


    /**
     * @param orderData order data
     *                  helper method to push order to other replicas
     */
    private void requestSync(OrderData orderData) {
        RestTemplate restTemplate = new RestTemplate();
        for (String uri : orderReplicasHostURIList) {
            // check if other replica is live or not
            if (pingPong(uri)) {
                restTemplate.postForEntity(uri + "/sync", orderData, String.class);
            }

        }
    }


    /**
     * @param url url for ping
     * @return boolean
     * helper to check if other replica is live or not
     */
    private boolean pingPong(String url) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            return "pong".equals(restTemplate.getForEntity(url + "/ping", String.class).getBody());
        } catch (Exception e) {
            // e.printStackTrace();
            return false;
        }

    }

    /**
     * write stock data into local file
     */
    private void updateFile() {
        try {
            FileWriter csvWriter = new FileWriter(file);
            for (OrderData data : orders.values()) {
                String line = String.join(",", Integer.toString(data.getData().getNumber()), data.getData().getName(), Integer.toString(data.getData().getQuantity())) + "\n";
                csvWriter.append(line);
            }
            // close file
            csvWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * read system environment variable to get hosts of other replicas
     */
    private void findReplicas() {
        // get order host from environment variable
        JSONArray orderHostsJsonArray = new JSONArray(System.getenv("ORDER_HOSTS"));
        System.out.println("hosts: " + System.getenv("ORDER_HOSTS"));
        // iterate the environment variable to get all other hosts
        for (int i = 0; i < orderHostsJsonArray.length(); i++) {
            JSONObject otherHost = orderHostsJsonArray.getJSONObject(i);
            String otherHostURI = "http://" + otherHost.getString("hostname") + ":" + otherHost.getInt("port");
            System.out.println("otherHostURI: " + otherHostURI);
            // put all other hosts into a list
            orderReplicasHostURIList.add(otherHostURI);
        }
    }
}
