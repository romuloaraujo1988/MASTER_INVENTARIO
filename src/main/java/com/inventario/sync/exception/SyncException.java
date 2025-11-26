package com.inventario.sync.exception;

/**
 * Exceção base para erros de sincronização
 */
public class SyncException extends Exception {
    private final SyncErrorType errorType;
    private final String context;
    
    public SyncException(String message, SyncErrorType errorType, String context) {
        super(message);
        this.errorType = errorType;
        this.context = context;
    }
    
    public SyncException(String message, SyncErrorType errorType, String context, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
        this.context = context;
    }
    
    public SyncErrorType getErrorType() {
        return errorType;
    }
    
    public String getContext() {
        return context;
    }
    
    @Override
    public String toString() {
        return String.format("SyncException{type=%s, context='%s', message='%s'}", 
            errorType, context, getMessage());
    }
}
