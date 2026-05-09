package com.inventario.sihcp.sync.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

class RetryPolicyTest {
    
    @Test
    void testDefaultPolicy() {
        // Act
        RetryPolicy policy = RetryPolicy.defaultPolicy();
        
        // Assert
        assertEquals(3, policy.getMaxRetries());
        assertEquals(5000, policy.getInitialDelayMs());
        assertEquals(2.0, policy.getBackoffMultiplier());
        assertTrue(policy.getRetryableExceptions().contains(SocketTimeoutException.class));
        assertTrue(policy.getRetryableExceptions().contains(ConnectException.class));
    }
    
    @Test
    void testCustomPolicy() {
        // Arrange & Act
        RetryPolicy policy = new RetryPolicy();
        policy.setMaxRetries(5);
        policy.setInitialDelayMs(1000);
        policy.setBackoffMultiplier(1.5);
        
        // Assert
        assertEquals(5, policy.getMaxRetries());
        assertEquals(1000, policy.getInitialDelayMs());
        assertEquals(1.5, policy.getBackoffMultiplier());
    }
}
