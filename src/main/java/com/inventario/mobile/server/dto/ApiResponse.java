package com.inventario.mobile.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Classe padrão para respostas da API Mobile
 * Padroniza o formato de retorno de todos os endpoints
 * 
 * @param <T> Tipo de dados retornados
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private boolean success;
    private T data;
    private String message;
    private String errorCode;
    
    public ApiResponse() {
    }
    
    /**
     * Cria uma resposta de sucesso com dados
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        return response;
    }
    
    /**
     * Cria uma resposta de sucesso com dados e mensagem
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        response.message = message;
        return response;
    }
    
    /**
     * Cria uma resposta de erro com mensagem
     */
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.message = message;
        return response;
    }
    
    /**
     * Cria uma resposta de erro com mensagem e código
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.message = message;
        response.errorCode = errorCode;
        return response;
    }
    
    // Getters e Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}