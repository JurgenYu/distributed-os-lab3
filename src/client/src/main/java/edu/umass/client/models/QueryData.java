package edu.umass.client.models;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class QueryData {
    private edu.umass.client.models.QuerySuccessData data;
    private edu.umass.client.models.QueryErrorData error;


    public QueryData() {
    }


    public QueryData(edu.umass.client.models.QuerySuccessData data) {
        this.data = data;
        this.error = null;
    }

    public QueryData(edu.umass.client.models.QueryErrorData error) {
        this.data = null;
        this.error = error;
    }


    
    /** 
     * @return QuerySuccessData
     */
    public edu.umass.client.models.QuerySuccessData getData() {
        return this.data;
    }


    
    /** 
     * @return QueryErrorData
     */
    public edu.umass.client.models.QueryErrorData getError() {
        return this.error;
    }

}
