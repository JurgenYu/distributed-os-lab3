package edu.umass.toy.order.controllers;

import edu.umass.toy.order.models.OrderData;
import edu.umass.toy.order.models.OrderRequestBody;
import edu.umass.toy.order.models.UnsyncedOrderData;
import edu.umass.toy.order.services.OrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;



@RestController
public class OrderController {
    @Autowired
    OrderService orderService;

    
    /** 
     * @param body Order RequestBody
     * @return OrderData
     * endpoint for requesting product order
     */
    @RequestMapping(value = "/orders", method = RequestMethod.POST)
    public OrderData putOrders(@RequestBody OrderRequestBody body) {
        return orderService.putOrders(body);
    }

    
    /** 
     * @param orderNumber order number
     * @return OrderData
     * endpoint for retrieving completed order
     */
    @RequestMapping(value="/orders/{order_number}", method=RequestMethod.GET)
    public OrderData getOrders(@PathVariable("order_number") int orderNumber) {
        return orderService.getOrders(orderNumber);
    }

    /**
     * endpoint for resetting local state
     */
    @RequestMapping(value = "/reset", method = RequestMethod.OPTIONS)
    public void reset() {
        orderService.reset();
    }

    
    /** 
     * @return String
     * endpoint for checking service status
     */
    @RequestMapping(value="/ping", method=RequestMethod.GET)
    public String hellow() {
        return "pong";
    }

    
    /** 
     * @param orderData order data
     * @return String
     * endpoint for updating order from other replicas
     */
    @PostMapping(value="/sync")
    public String syncOrder(@RequestBody OrderData orderData) {        
        return orderService.syncOrder(orderData);
    }

    
    /** 
     * @param remoteNextOrderNumber
     * @return UnsyncedOrderData
     * endpoint for other replicas to get missed orders during its crash-recovery
     */
    @GetMapping(value="/getUnsyncedOrderDataList/{next_order_number}")
    public UnsyncedOrderData getUnsyncedOrderDataList(@PathVariable("next_order_number") int remoteNextOrderNumber) {
        return orderService.getUnsyncedOrderDataList(remoteNextOrderNumber);
    }
    
    

}
