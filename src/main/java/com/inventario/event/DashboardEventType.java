package com.inventario.event;

/**
 * Tipos de eventos que podem ser publicados para atualização da dashboard.
 * Cada tipo representa uma categoria de mudança no sistema que pode
 * interessar aos observers da dashboard.
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public enum DashboardEventType {
    
    /**
     * Evento disparado quando uma coleta é sincronizada do app mobile.
     * Metadata esperada: coletaId, patrimonioId, inventarioId, coletorNome, batchSize
     */
    COLETA_SINCRONIZADA("Coleta Sincronizada", "Nova coleta recebida do aplicativo mobile"),
    
    /**
     * Evento disparado quando um patrimônio é atualizado.
     * Metadata esperada: patrimonioId, campo, valorAnterior, valorNovo
     */
    PATRIMONIO_ATUALIZADO("Patrimônio Atualizado", "Dados de patrimônio foram modificados"),
    
    /**
     * Evento disparado quando o status de um inventário é alterado.
     * Metadata esperada: inventarioId, status, percentualConcluido
     */
    INVENTARIO_ALTERADO("Inventário Alterado", "Status ou dados do inventário foram modificados"),
    
    /**
     * Evento disparado quando uma sala é atualizada.
     * Metadata esperada: salaId, campo, valorAnterior, valorNovo
     */
    SALA_ATUALIZADA("Sala Atualizada", "Dados de sala foram modificados"),
    
    /**
     * Evento disparado quando um responsável é atualizado.
     * Metadata esperada: responsavelId, campo, valorAnterior, valorNovo
     */
    RESPONSAVEL_ATUALIZADO("Responsável Atualizado", "Dados de responsável foram modificados");
    
    private final String displayName;
    private final String description;
    
    DashboardEventType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * Retorna o nome de exibição amigável do tipo de evento.
     * @return Nome para exibição na UI
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Retorna a descrição do tipo de evento.
     * @return Descrição detalhada do evento
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Verifica se este tipo de evento está relacionado a coletas.
     * @return true se for evento de coleta
     */
    public boolean isColetaRelated() {
        return this == COLETA_SINCRONIZADA;
    }
    
    /**
     * Verifica se este tipo de evento está relacionado a patrimônios.
     * @return true se for evento de patrimônio
     */
    public boolean isPatrimonioRelated() {
        return this == PATRIMONIO_ATUALIZADO || this == COLETA_SINCRONIZADA;
    }
    
    /**
     * Verifica se este tipo de evento está relacionado a inventários.
     * @return true se for evento de inventário
     */
    public boolean isInventarioRelated() {
        return this == INVENTARIO_ALTERADO || this == COLETA_SINCRONIZADA;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
