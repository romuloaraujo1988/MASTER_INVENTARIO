package com.inventario.sync.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Resultado de validação de dados
 */
public class ValidationResult {
    private boolean valid;
    private List<ValidationError> errors;
    private int totalValidated;
    
    public ValidationResult() {
        this.errors = new ArrayList<>();
    }
    
    public ValidationResult(boolean valid, int totalValidated) {
        this.valid = valid;
        this.totalValidated = totalValidated;
        this.errors = new ArrayList<>();
    }
    
    public static class ValidationError {
        private String field;
        private String message;
        private Object invalidValue;
        
        public ValidationError(String field, String message) {
            this.field = field;
            this.message = message;
        }
        
        public ValidationError(String field, String message, Object invalidValue) {
            this.field = field;
            this.message = message;
            this.invalidValue = invalidValue;
        }
        
        // Getters and Setters
        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Object getInvalidValue() { return invalidValue; }
        public void setInvalidValue(Object invalidValue) { this.invalidValue = invalidValue; }
    }
    
    // Getters and Setters
    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public List<ValidationError> getErrors() { return errors; }
    public void setErrors(List<ValidationError> errors) { this.errors = errors; }
    public int getTotalValidated() { return totalValidated; }
    public void setTotalValidated(int totalValidated) { this.totalValidated = totalValidated; }
    
    public void addError(ValidationError error) {
        this.errors.add(error);
        this.valid = false;
    }
}
