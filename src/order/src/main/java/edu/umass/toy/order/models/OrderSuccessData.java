package edu.umass.toy.order.models;

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

    
    /** 
     * @param number
     */
    public void setNumber(int number) {
        this.number = number;
    }

    
    /** 
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    
    /** 
     * @param quantity
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

}
