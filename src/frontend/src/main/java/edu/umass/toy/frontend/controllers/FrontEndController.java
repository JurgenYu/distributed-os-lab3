package edu.umass.toy.frontend.controllers;

import edu.umass.toy.frontend.models.OrderData;
import edu.umass.toy.frontend.models.OrderRequestBody;
import edu.umass.toy.frontend.models.QueryData;
import edu.umass.toy.frontend.services.FrontEndService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FrontEndController {

    // frontEnd service
    @Autowired
    FrontEndService frontEndService;

    
    /** 
     * @param name product name
     * @return QueryData
     * endpoint for query product information
     */
    @RequestMapping(value="/products/{product_name}", method=RequestMethod.GET)
    public QueryData query(@PathVariable("product_name") String name) {
        return frontEndService.query(name);
    }

    
    /** 
     * @param body Order RequestBody
     * @return OrderData
     * endpoint for requesting product order
     */
    @RequestMapping(value="/orders", method=RequestMethod.POST)
    public OrderData orders(@RequestBody OrderRequestBody body) {
        return frontEndService.orders(body);
    }

    
    /** 
     * @param orderNumber order number
     * @return OrderData
     * endpoint for retrieving completed order
     */
    @RequestMapping(value="/orders/{order_number}", method=RequestMethod.GET)
    public OrderData orders_query(@PathVariable("order_number") String orderNumber) {
        return frontEndService.orders_query(orderNumber);
    }

    
    /** 
     * @param productName product name
     * @return String
     * endpoint for updating local cache from catalog service
     */
    @RequestMapping(value="/invalidate/{product_name}", method=RequestMethod.GET)
    public String invalidate(@PathVariable("product_name") String productName) {
        return frontEndService.invalidate(productName);
    }


}