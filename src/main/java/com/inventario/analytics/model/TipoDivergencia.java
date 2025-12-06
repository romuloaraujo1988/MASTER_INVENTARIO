package com.inventario.analytics.model;

/**
 * Tipos de divergência detectados durante a coleta de patrimônios.
 */
public enum TipoDivergencia {
    
    /**
     * Divergência de localização - patrimônio encontrado em local diferente do cadastrado.
     */
    LOCALIZACAO("Localização", "O patrimônio foi encontrado em local diferente do cadastrado"),
    
    /**
     * Divergência de estado de conservação - estado encontrado diferente do cadastrado.
     */
    ESTADO("Estado", "O estado de conservação encontrado difere do cadastrado"),
    
    /**
     * Divergência de responsável - patrimônio sob responsabilidade de pessoa diferente.
     */
    RESPONSAVEL("Responsável", "O responsável encontrado difere do cadastrado"),
    
    /**
     * Divergência de valor - valor do patrimônio difere do cadastrado.
     */
    VALOR("Valor", "O valor do patrimônio difere do cadastrado"),
    
    /**
     * Divergência registrada manualmente pelo coletor.
     */
    MANUAL("Manual", "Divergência registrada manualmente pelo coletor"),
    
    /**
     * Outro tipo de divergência não categorizado.
     */
    OUTRO("Outro", "Outro tipo de divergência");
    
    private final String descricao;
    private final String explicacao;
    
    TipoDivergencia(String descricao, String explicacao) {
        this.descricao = descricao;
        this.explicacao = explicacao;
    }
    
    /**
     * Retorna a descrição curta do tipo de divergência.
     */
    public String getDescricao() {
        return descricao;
    }
    
    /**
     * Retorna a explicação detalhada do tipo de divergência.
     */
    public String getExplicacao() {
        return explicacao;
    }
    
    /**
     * Converte uma string para o enum correspondente.
     * 
     * @param valor String a ser convertida
     * @return TipoDivergencia correspondente ou OUTRO se não encontrado
     */
    public static TipoDivergencia fromString(String valor) {
        if (valor == null || valor.isEmpty()) {
            return OUTRO;
        }
        
        String valorUpper = valor.toUpperCase().trim();
        
        // Tenta match direto
        try {
            return TipoDivergencia.valueOf(valorUpper);
        } catch (IllegalArgumentException e) {
            // Tenta match por descrição
            for (TipoDivergencia tipo : values()) {
                if (tipo.descricao.equalsIgnoreCase(valor)) {
                    return tipo;
                }
            }
            return OUTRO;
        }
    }
    
    @Override
    public String toString() {
        return descricao;
    }
}
