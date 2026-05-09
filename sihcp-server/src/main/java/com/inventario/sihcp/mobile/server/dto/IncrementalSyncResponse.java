package com.inventario.sihcp.mobile.server.dto;

import java.util.List;

/**
 * Response para sincronização incremental
 * Retorna apenas dados novos/modificados desde o último timestamp
 */
public class IncrementalSyncResponse<T> {
    
    private List<T> data;
    private Long serverTimestamp;
    private Integer totalCount;
    private Integer returnedCount;
    private Boolean hasMore;
    private String message;
    
    public IncrementalSyncResponse() {
    }
    
    public IncrementalSyncResponse(List<T> data, Long serverTimestamp, Integer totalCount) {
        this.data = data;
        this.serverTimestamp = serverTimestamp;
        this.totalCount = totalCount;
        this.returnedCount = data != null ? data.size() : 0;
        this.hasMore = false;
    }
    
    // Getters e Setters
    
    public List<T> getData() {
        return data;
    }
    
    public void setData(List<T> data) {
        this.data = data;
        this.returnedCount = data != null ? data.size() : 0;
    }
    
    public Long getServerTimestamp() {
        return serverTimestamp;
    }
    
    public void setServerTimestamp(Long serverTimestamp) {
        this.serverTimestamp = serverTimestamp;
    }
    
    public Integer getTotalCount() {
        return totalCount;
    }
    
    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }
    
    public Integer getReturnedCount() {
        return returnedCount;
    }
    
    public void setReturnedCount(Integer returnedCount) {
        this.returnedCount = returnedCount;
    }
    
    public Boolean getHasMore() {
        return hasMore;
    }
    
    public void setHasMore(Boolean hasMore) {
        this.hasMore = hasMore;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
