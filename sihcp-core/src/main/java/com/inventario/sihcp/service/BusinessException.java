package com.inventario.sihcp.service;

/**
 * Exceção para erros de regras de negócio
 * Usada pela camada de Service para comunicar erros de validação
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class BusinessException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
