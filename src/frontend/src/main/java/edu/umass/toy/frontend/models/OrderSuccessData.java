package edu.umass.toy.frontend.models;

public class OrderSuccessData {
    private int number;
    private String name;
    private int quantity;


    public OrderSuccessData() {
    }


    public OrderSuccessData(int number, String name, int quantity) {
        this.number = number;
        this.name = name;
        this.quantity = quantity;
    }


    
    /** 
     * @return int
     */
    public int getNumber() {
        return this.number;
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
