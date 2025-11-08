package com.inventario.mobile.server.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * DTO para requisição de registro de coletas em lote
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class MobileColetaBatchRequest {
    
    @NotEmpty(message = "Lista de coletas não pode estar vazia")
    @Valid
    private List<MobileColetaRequest> coletas;
    
    // Construtores
    public MobileColetaBatchRequest() {
    }
    
    public MobileColetaBatchRequest(List<MobileColetaRequest> coletas) {
        this.coletas = coletas;
    }
    
    // Getters e Setters
    public List<MobileColetaRequest> getColetas() {
        return coletas;
    }
    
    public void setColetas(List<MobileColetaRequest> coletas) {
        this.coletas = coletas;
    }
}
