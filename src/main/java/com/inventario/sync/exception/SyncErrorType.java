package com.inventario.sync.exception;

/**
 * Tipos de erros que podem ocorrer durante sincronização
 */
public enum SyncErrorType {
    VALIDATION_ERROR("Erro de validação de dados"),
    NETWORK_ERROR("Erro de rede ou conectividade"),
    CONSTRAINT_VIOLATION("Violação de constraint do banco de dados"),
    AUTHENTICATION_ERROR("Erro de autenticação"),
    TRANSACTION_ERROR("Erro de transação"),
    CONFLICT_ERROR("Conflito de dados"),
    INTEGRITY_ERROR("Erro de integridade de dados"),
    BACKUP_ERROR("Erro ao criar ou restaurar backup");
    
    private final String description;
    
    SyncErrorType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
