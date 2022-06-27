package edu.umass.toy.order.models;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class OrderData {
    private OrderSuccessData data;
    private OrderErrorData error;


    public OrderData() {
    }


    public OrderData(OrderSuccessData data) {
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
    public OrderSuccessData getData() {
        return this.data;
    }


    
    /** 
     * @return OrderErrorData
     */
    public OrderErrorData getError() {
        return this.error;
    }


}
