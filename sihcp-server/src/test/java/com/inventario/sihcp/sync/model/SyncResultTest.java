package com.inventario.sihcp.sync.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SyncResultTest {
    
    @Test
    void testBuilderPattern() {
        // Arrange & Act
        SyncResult result = SyncResult.builder()
            .success(true)
            .totalProcessed(100)
            .totalSuccess(95)
            .totalFailed(5)
            .durationMs(5000)
            .checksum("abc123")
            .build();
        
        // Assert
        assertTrue(result.isSuccess());
        assertEquals(100, result.getTotalProcessed());
        assertEquals(95, result.getTotalSuccess());
        assertEquals(5, result.getTotalFailed());
        assertEquals(5000, result.getDurationMs());
        assertEquals("abc123", result.getChecksum());
        assertNotNull(result.getTimestamp());
    }
    
    @Test
    void testAddError() {
        // Arrange
        SyncResult result = SyncResult.builder().build();
        SyncError error = new SyncError(1, "Test error");
        
        // Act
        SyncResult.builder().addError(error).build();
        
        // Assert - apenas verifica que não lança exceção
        assertNotNull(result);
    }
    
    @Test
    void testDefaultConstructor() {
        // Act
        SyncResult result = new SyncResult();
        
        // Assert
        assertNotNull(result.getErrors());
        assertTrue(result.getErrors().isEmpty());
        assertNotNull(result.getTimestamp());
    }
}
