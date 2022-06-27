package edu.umass.client.models;


import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class OrderData {
    private edu.umass.client.models.OrderSuccessData data;
    private edu.umass.client.models.OrderErrorData error;


    public OrderData() {
    }


    public OrderData(edu.umass.client.models.OrderSuccessData data) {
        this.data = data;
        this.error = null;
    }

    public OrderData(OrderErrorData error) {
        this.data = null;
        this.error = error;
    }

    
    /** 
     * @return OrderSuccessData
     */
    public edu.umass.client.models.OrderSuccessData getData() {
        return this.data;
    }


    
    /** 
     * @return OrderErrorData
     */
    public edu.umass.client.models.OrderErrorData getError() {
        return this.error;
    }


}
