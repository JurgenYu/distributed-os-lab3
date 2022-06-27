package edu.umass.toy.order.models;

import java.util.List;

/**
 * UnsyncedOrderData
 */
public class UnsyncedOrderData {

    List<OrderData> orderDataList;

    public UnsyncedOrderData(List<OrderData> orderDataList) {
        this.orderDataList = orderDataList;
    }

    public UnsyncedOrderData() {
    }

    
    /** 
     * @return List<OrderData>
     */
    public List<OrderData> getOrderDataList() {
        return this.orderDataList;
    }

    
    /** 
     * @param orderDataList
     */
    public void setOrderDataList(List<OrderData> orderDataList) {
        this.orderDataList = orderDataList;
    }
    

}