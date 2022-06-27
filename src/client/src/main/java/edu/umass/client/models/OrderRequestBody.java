package edu.umass.client.models;

public class OrderRequestBody {
    private String name;
    private int quantity;

    public OrderRequestBody(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }


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
