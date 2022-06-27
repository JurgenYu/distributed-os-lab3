package edu.umass.toy.frontend.services;

import edu.umass.toy.frontend.models.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class FrontEndService {
    // stock cache for product information
    private final ConcurrentMap<Toys, Integer> cache = new ConcurrentHashMap<>();

    // catalog host URI
    private String catalogHostURI;

    // order leader host URI
    private String orderLeaderHostURI = null;


    /**
     * @param name product name
     * @return QueryData
     * query product information
     */
    public QueryData query(String name) {
        try {
            // get toy object by name
            Toys toy = Toys.getToy(name);
            // return from cache if cache hits
            if (cache.containsKey(toy)) {
                System.out.println("Cached");
                // return toy information
                return new QueryData(new QuerySuccessData(toy, cache.get(toy)));
            } else {
                // request catalog service if cache missed
                QueryData res = requestQuery(name);
                if (res.getData() != null) {
                    cache.put(toy, res.getData().getQuantity());
                }
                return res;
            }
        } catch (EnumConstantNotPresentException e) {
            // catch Product Not Found exception
            return new QueryData(new QueryErrorData(HttpStatus.NOT_FOUND, "Product not found"));
        } catch (Exception e) {
            e.printStackTrace();
            // catch for Internal Error
            return new QueryData(new QueryErrorData(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error@query@FrontEndService"));
        }
    }


    /**
     * @param body Order Request Body
     * @return OrderData
     * order product
     */
    public OrderData orders(OrderRequestBody body) {
        try {
            // check leader exist or not
            while (!pingPong(orderLeaderHostURI)) {
                // re-elect leader
                findLeader();
            }
            // send order
            return requestOrder(body);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return new OrderData(new OrderErrorData(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error"));
        }
    }


    /**
     * @throws Exception initial frontend service
     * initialize frontend service
     */
    @PostConstruct
    public void init() throws Exception {
        // wait for order ready
        Thread.sleep(5000);
        // construct catalog host URI
        catalogHostURI = "http://" + Inet4Address.getByName(System.getenv("CATALOG_HOST")).getHostName() + ":" + System.getenv("CATALOG_PORT");
        // elect leader
        findLeader();
        System.out.println(orderLeaderHostURI);
    }


    /**
     * @throws UnknownHostException
     * find leader in three replicas
     */
    private void findLeader() throws UnknownHostException {
        // find order leader
        orderLeaderHostURI = null;
        // get order host from system environment variable of "ORDER_HOSTS"
        JSONArray orderHostsJson = new JSONArray(System.getenv("ORDER_HOSTS"));
        // init highest id
        int highest = -1;

        // iterate each order host to check if online or not, and get the service id
        for (int i = 0; i < orderHostsJson.length(); i++) {
            JSONObject selectedOrderLeaderJson = orderHostsJson.getJSONObject(i);
            String tempURI = "http://" + selectedOrderLeaderJson.getString("hostname") + ":" + selectedOrderLeaderJson.getInt("port");
            System.out.println("tempURI: " + tempURI);
            try {
                // check service status
                if (pingPong(tempURI)) {
                    // replace order leader if it has greater service id
                    if (highest < selectedOrderLeaderJson.getInt("id")) {
                        highest = selectedOrderLeaderJson.getInt("id");
                        orderLeaderHostURI = tempURI;
                        System.out.println("orderLeaderHostURI: " + orderLeaderHostURI);
                    }
                }
            } catch (RestClientException e) {
                e.printStackTrace();
            }
        }
        if (orderLeaderHostURI == null) {
            throw new RuntimeException();
        }
    }


    /**
     * @param URL service URL
     * @return boolean
     * ping services to check the status
     */
    private boolean pingPong(String URL) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            // get the response from ping service
            String res = restTemplate.getForEntity(URL + "/ping", String.class).getBody();
            return "pong".equals(res);
        } catch (Exception e) {
            // e.printStackTrace();
            return false;
        }
    }


    /**
     * @param productName product name
     * @return String
     * endpoint for updating local cache from catalog service
     */
    public String invalidate(String productName) {
        cache.remove(Toys.getToy(productName));
        System.out.println("Cache Invalidated, product is:" + productName);
        return "Cache Update Success";
    }


    /**
     * @param orderNumber order number
     * @return OrderData
     *  retrieve completed order
     */
    public OrderData orders_query(String orderNumber) {
        try {
            return requestOrderQuery(orderNumber);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return new OrderData(new OrderErrorData(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error"));
        }
    }


    /**
     * @param orderNumber order number
     * @return OrderData
     * @throws UnknownHostException
     * helper method for requesting order information from order service
     */
    private OrderData requestOrderQuery(String orderNumber) throws UnknownHostException {
        // construct order leader host URI
        final String url = orderLeaderHostURI + "/orders/" + orderNumber;
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, OrderData.class);
    }


    /**
     * @param body order request body
     * @return OrderData
     * @throws UnknownHostException
     *  helper method for requesting order to order service
     */
    private OrderData requestOrder(OrderRequestBody body) throws UnknownHostException {
        // check service status
        if (pingPong(orderLeaderHostURI)) {
            // construct order leader host URI
            final String url = orderLeaderHostURI + "/orders";
            RestTemplate restTemplate = new RestTemplate();
            return restTemplate.postForObject(url, body, OrderData.class);
        }
        findLeader();
        return requestOrder(body);
    }


    /**
     * @param name product name
     * @return QueryData
     * @throws UnknownHostException
     * helper method for requesting catalog service if cache missed
     */
    private QueryData requestQuery(String name) throws UnknownHostException {
        // construct catalog host URI
        final String url = catalogHostURI + "/query/" + name;
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, QueryData.class);
    }

}