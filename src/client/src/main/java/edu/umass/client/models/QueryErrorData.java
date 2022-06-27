package edu.umass.client.models;

import org.springframework.http.HttpStatus;

public class QueryErrorData {
    private int code;
    private String message;


    public QueryErrorData() {
    }


    public QueryErrorData(HttpStatus code, String message) {
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
