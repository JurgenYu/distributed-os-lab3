package edu.umass.toy.frontend.models;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class QueryData {
    private QuerySuccessData data;
    private QueryErrorData error;


    public QueryData() {
    }


    public QueryData(QuerySuccessData data) {
        this.data = data;
        this.error = null;
    }

    public QueryData(QueryErrorData error) {
        this.data = null;
        this.error = error;
    }


    
    /** 
     * @return QuerySuccessData
     */
    public QuerySuccessData getData() {
        return this.data;
    }


    
    /** 
     * @return QueryErrorData
     */
    public QueryErrorData getError() {
        return this.error;
    }

}
