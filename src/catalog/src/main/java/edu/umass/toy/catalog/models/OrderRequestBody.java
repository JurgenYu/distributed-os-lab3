package edu.umass.toy.catalog.models;

public class OrderRequestBody {
    private String name;
    private int quantity;


    public OrderRequestBody() {
    }


    
    /** 
     * @return String
     */
    public String getName() {
        return this.name;
    }

    
    /** 
     * @return int
     */
    public int getQuantity() {
        return this.quantity;
    }


}
