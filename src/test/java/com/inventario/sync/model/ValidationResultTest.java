package com.inventario.sync.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidationResultTest {
    
    @Test
    void testValidResult() {
        // Arrange & Act
        ValidationResult result = new ValidationResult(true, 10);
        
        // Assert
        assertTrue(result.isValid());
        assertEquals(10, result.getTotalValidated());
        assertTrue(result.getErrors().isEmpty());
    }
    
    @Test
    void testAddError() {
        // Arrange
        ValidationResult result = new ValidationResult(true, 10);
        ValidationResult.ValidationError error = 
            new ValidationResult.ValidationError("field1", "Invalid value");
        
        // Act
        result.addError(error);
        
        // Assert
        assertFalse(result.isValid()); // deve mudar para false ao adicionar erro
        assertEquals(1, result.getErrors().size());
        assertEquals("field1", result.getErrors().get(0).getField());
    }
    
    @Test
    void testValidationErrorWithInvalidValue() {
        // Arrange & Act
        ValidationResult.ValidationError error = 
            new ValidationResult.ValidationError("age", "Must be positive", -5);
        
        // Assert
        assertEquals("age", error.getField());
        assertEquals("Must be positive", error.getMessage());
        assertEquals(-5, error.getInvalidValue());
    }
}
