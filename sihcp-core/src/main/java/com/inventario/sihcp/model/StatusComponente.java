package com.inventario.sihcp.model;

import java.awt.Color;

/**
 * Enum que representa o status de um componente individual de um item composto.
 * Usado para indicar se um componente específico foi encontrado durante a coleta.
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public enum StatusComponente {
    
    /**
     * Componente foi encontrado na quantidade esperada ou superior
     */
    ENCONTRADO("Encontrado", new Color(46, 204, 113)),  // Verde
    
    /**
     * Componente não foi encontrado (quantidade = 0)
     */
    FALTANTE("Faltante", new Color(231, 76, 60)),  // Vermelho
    
    /**
     * Componente foi encontrado parcialmente (quantidade < esperada)
     */
    PARCIAL("Parcial", new Color(241, 196, 15)),  // Amarelo/Laranja
    
    /**
     * Componente ainda não foi verificado/coletado
     */
    NAO_COLETADO("Não Coletado", new Color(149, 165, 166));  // Cinza
    
    private final String descricao;
    private final Color cor;
    
    StatusComponente(String descricao, Color cor) {
        this.descricao = descricao;
        this.cor = cor;
    }
    
    /**
     * Retorna a descrição amigável do status
     */
    public String getDescricao() {
        return descricao;
    }
    
    /**
     * Retorna a cor associada ao status para uso na UI
     */
    public Color getCor() {
        return cor;
    }
    
    /**
     * Retorna a cor de fundo (mais clara) para uso em tabelas
     */
    public Color getCorFundo() {
        return switch (this) {
            case ENCONTRADO -> new Color(212, 239, 223);   // Verde claro
            case FALTANTE -> new Color(250, 219, 216);     // Vermelho claro
            case PARCIAL -> new Color(252, 243, 207);      // Amarelo claro
            case NAO_COLETADO -> new Color(236, 240, 241); // Cinza claro
        };
    }
    
    @Override
    public String toString() {
        return descricao;
    }
    
    /**
     * Determina o status baseado nas quantidades
     * 
     * @param quantidadeEncontrada quantidade encontrada na coleta
     * @param quantidadeEsperada quantidade esperada do componente
     * @return o status do componente correspondente
     */
    public static StatusComponente fromQuantidades(int quantidadeEncontrada, int quantidadeEsperada) {
        if (quantidadeEsperada == 0) {
            return ENCONTRADO;
        }
        
        if (quantidadeEncontrada >= quantidadeEsperada) {
            return ENCONTRADO;
        } else if (quantidadeEncontrada > 0) {
            return PARCIAL;
        } else {
            return FALTANTE;
        }
    }
    
    /**
     * Converte uma string de status do banco de dados para o enum
     * 
     * @param statusBanco string do status vinda do banco (COMPLETO, PARCIAL, FALTANTE, PENDENTE)
     * @return o StatusComponente correspondente
     */
    public static StatusComponente fromString(String statusBanco) {
        if (statusBanco == null) {
            return NAO_COLETADO;
        }
        
        return switch (statusBanco.toUpperCase()) {
            case "COMPLETO", "ENCONTRADO" -> ENCONTRADO;
            case "PARCIAL" -> PARCIAL;
            case "FALTANTE" -> FALTANTE;
            case "PENDENTE", "NAO_COLETADO" -> NAO_COLETADO;
            default -> NAO_COLETADO;
        };
    }
}
