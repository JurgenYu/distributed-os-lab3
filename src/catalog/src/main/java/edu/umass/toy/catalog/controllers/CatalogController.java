package edu.umass.toy.catalog.controllers;

import edu.umass.toy.catalog.models.OrderRequestBody;
import edu.umass.toy.catalog.models.QueryData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import edu.umass.toy.catalog.models.OrderData;
import edu.umass.toy.catalog.services.CatalogService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@RestController
public class CatalogController {
    @Autowired
    CatalogService catalogService;

    
    /** 
     * @return String
     */
    @RequestMapping(value="/", method=RequestMethod.GET)
    public String hellow() {
        return "Hello";
    }
    

    
    /** 
     * @param name product name
     * @return QueryData
     * endpoint for query product information
     */
    @RequestMapping(value = "/query/{product_name}", method = RequestMethod.GET)
    @ResponseBody
    public QueryData query(@PathVariable("product_name") String name) {
        return catalogService.query(name);
    }

    
    /** 
     * @param body Order RequestBody
     * @return OrderData
     * endpoint for requesting product order
     */
    @RequestMapping(value = "/orders", method = RequestMethod.POST)
    @ResponseBody
    public OrderData order(@RequestBody OrderRequestBody body) {
        return catalogService.order(body);
    }

    /**
     * endpoint for resetting local state
     */
    @RequestMapping(value = "/reset", method = RequestMethod.OPTIONS)
    public void reset() {
        catalogService.reset();
    }
}
