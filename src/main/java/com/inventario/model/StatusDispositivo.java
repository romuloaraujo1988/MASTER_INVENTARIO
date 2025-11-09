package com.inventario.model;

/**
 * Enum que representa os possíveis status de um dispositivo mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public enum StatusDispositivo {
    
    PENDENTE("Pendente de Aprovação"),
    APROVADO("Aprovado"),
    BLOQUEADO("Bloqueado"),
    REJEITADO("Rejeitado");

    private final String descricao;

    StatusDispositivo(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }

    public static StatusDispositivo fromString(String texto) {
        if (texto == null) {
            return null;
        }
        
        for (StatusDispositivo status : StatusDispositivo.values()) {
            if (status.name().equalsIgnoreCase(texto) || 
                status.descricao.equalsIgnoreCase(texto)) {
                return status;
            }
        }
        
        throw new IllegalArgumentException("Status de dispositivo inválido: " + texto);
    }
}
