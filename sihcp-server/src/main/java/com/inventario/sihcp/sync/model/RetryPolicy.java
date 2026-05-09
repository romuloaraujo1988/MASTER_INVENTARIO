package com.inventario.sihcp.sync.model;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Set;

public class RetryPolicy {
    private int maxRetries;
    private long initialDelayMs;
    private double backoffMultiplier;
    private Set<Class<? extends Exception>> retryableExceptions;
    
    public RetryPolicy() {
        this.maxRetries = 3;
        this.initialDelayMs = 5000;
        this.backoffMultiplier = 2.0;
        this.retryableExceptions = new HashSet<>();
    }
    
    public RetryPolicy(int maxRetries, long initialDelayMs, double backoffMultiplier, 
                      Set<Class<? extends Exception>> retryableExceptions) {
        this.maxRetries = maxRetries;
        this.initialDelayMs = initialDelayMs;
        this.backoffMultiplier = backoffMultiplier;
        this.retryableExceptions = retryableExceptions;
    }
    
    public static RetryPolicy defaultPolicy() {
        Set<Class<? extends Exception>> retryable = new HashSet<>();
        retryable.add(SocketTimeoutException.class);
        retryable.add(ConnectException.class);
        return new RetryPolicy(3, 5000, 2.0, retryable);
    }
    
    // Getters and Setters
    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
    public long getInitialDelayMs() { return initialDelayMs; }
    public void setInitialDelayMs(long initialDelayMs) { this.initialDelayMs = initialDelayMs; }
    public double getBackoffMultiplier() { return backoffMultiplier; }
    public void setBackoffMultiplier(double backoffMultiplier) { this.backoffMultiplier = backoffMultiplier; }
    public Set<Class<? extends Exception>> getRetryableExceptions() { return retryableExceptions; }
    public void setRetryableExceptions(Set<Class<? extends Exception>> retryableExceptions) { 
        this.retryableExceptions = retryableExceptions; 
    }
}
