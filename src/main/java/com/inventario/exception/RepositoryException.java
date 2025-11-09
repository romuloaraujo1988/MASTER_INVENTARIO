package com.inventario.exception;

/**
 * Exceção customizada para erros na camada de Repository
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class RepositoryException extends RuntimeException {
    
    public RepositoryException(String message) {
        super(message);
    }
    
    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public RepositoryException(Throwable cause) {
        super(cause);
    }
}
