package edu.umass.client.models;

import org.springframework.http.HttpStatus;

public class OrderErrorData {
    private int code;
    private String message;

    public OrderErrorData() {
    }

    public OrderErrorData(HttpStatus code, String message) {
        this.code = code.value();
        this.message = message;
    }


    
    /** 
     * @return int
     */
    public int getCode() {
        return this.code;
    }


    
    /** 
     * @return String
     */
    public String getMessage() {
        return this.message;
    }


}
