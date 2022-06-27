package edu.umass.client.models;

public class QuerySuccessData {
    private String name;
    private double price;
    private int quantity;


    public QuerySuccessData() {
    }
    

    public QuerySuccessData(Toys toy, int quantity) {
        this.name = toy.getName();
        this.price = Double.parseDouble(toy.getPrice());
        this.quantity = quantity;
    }


    
    /** 
     * @return String
     */
    public String getName() {
        return this.name;
    }


    
    /** 
     * @return double
     */
    public double getPrice() {
        return this.price;
    }


    
    /** 
     * @return int
     */
    public int getQuantity() {
        return this.quantity;
    }


}
