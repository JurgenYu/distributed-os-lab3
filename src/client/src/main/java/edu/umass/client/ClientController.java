package edu.umass.client;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import edu.umass.client.models.OrderData;
import edu.umass.client.models.OrderRequestBody;
import edu.umass.client.models.QueryData;

/**
 * ClientController
 */
@Service
public class ClientController {

    @Value("${test.host}")
    private String host;
    @Value("${test.port}")
    private String port;
    @Value("${test.numberReq}")
    private int numberReq;

    private String uri;

    private String product = "barbie";

    private RestTemplate restTemplate = new RestTemplate();

    private List<QueryData> queries = new ArrayList<>();
    private List<OrderData> orders = new ArrayList<>();
    private List<OrderData> orderQueries = new ArrayList<>();

    private List<Long> queriesTime = new ArrayList<>();
    private List<Long> ordersTime = new ArrayList<>();
    private List<Long> orderQueriesTime = new ArrayList<>();

    @PostConstruct
    public void main() throws InterruptedException {
        constructURI();
        for (double p = 0.0; p < 1.0; p += 0.2) {
            for (int i = 0; i < numberReq; i++) {
                queryProduct(product);
                if (Math.random() < p) {
                    orderProduct(product);
                }
            }
            for (OrderData oneOrder : orders) {
                queryOrder(oneOrder.getData().getNumber());
            }
            compareOrders();
            printData(p);
            reset();
            Thread.sleep(10000);
        }
    }

    private void constructURI() {
        uri = "http://" + host + ":" + port;
    }

    private void queryProduct(String product) {
        long start = System.currentTimeMillis();
        QueryData data = restTemplate.getForEntity(uri + "/products/" + product, QueryData.class).getBody();
        if (data.getData() != null) {
            queries.add(data);
        }
        queriesTime.add(System.currentTimeMillis() - start);
    }

    private void orderProduct(String product) {
        long start = System.currentTimeMillis();
        OrderRequestBody body = new OrderRequestBody(product, 1);
        OrderData data = restTemplate.postForEntity(uri + "/orders", body, OrderData.class).getBody();
        if (data.getData() != null) {
            orders.add(data);
        }
        ordersTime.add(System.currentTimeMillis() - start);
    }

    private void queryOrder(int orderNumber) {
        long start = System.currentTimeMillis();
        OrderData data = restTemplate.getForEntity(uri + "/orders/" + orderNumber, OrderData.class).getBody();
        if (data.getData() != null) {
            orderQueries.add(data);
        }
        orderQueriesTime.add(System.currentTimeMillis() - start);
    }

    private void compareOrders() {
        boolean res = true;
        if (orders.size() != orderQueries.size()) {
            res = false;
        }
        for (int i = 0; i < orders.size(); i++) {
            if (orderQueries.get(i).getData().getNumber() != orders.get(i).getData().getNumber()) {
                res = false;
                break;
            }
        }
        System.out.println("All orders matched: " + res);
    }

    private void reset() {
        queries = new ArrayList<>();
        orders = new ArrayList<>();
        orderQueries = new ArrayList<>();

        queriesTime = new ArrayList<>();
        ordersTime = new ArrayList<>();
        orderQueriesTime = new ArrayList<>();
    }

    private void printData(double p) {
        System.out.println("===========================================");
        System.out.println("probability: " + p);
        System.out.println("Queries: " + queries.size());
        double queryLatency = avg(queriesTime);
        System.out.println("Orders: " + orders.size());
        System.out.println("queryLatency: " + queryLatency);
        double orderLatency = avg(ordersTime);
        System.out.println("orderLatency: " + orderLatency);
        System.out.println("OrderQueries: " + orderQueries.size());
        double orderQueryLatency = avg(orderQueriesTime);
        System.out.println("orderQueryLatency: " + orderQueryLatency);
        System.out.println("===========================================");

    }

    private double avg(List<Long> list) {
        long total = 0;
        for (int i = 0; i < list.size(); i++) {
            total += list.get(i);
        }
        return (double) total / (double) list.size();
    }
}