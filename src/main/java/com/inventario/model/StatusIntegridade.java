package com.inventario.model;

import java.awt.Color;

/**
 * Enum que representa o status de integridade de um item composto.
 * Usado para classificar conjuntos patrimoniais baseado na completude dos componentes.
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public enum StatusIntegridade {
    
    /**
     * Todos os componentes esperados foram encontrados
     */
    COMPLETO("Completo", new Color(46, 204, 113)),  // Verde
    
    /**
     * Um ou mais componentes estão faltando
     */
    INCOMPLETO("Incompleto", new Color(231, 76, 60)),  // Vermelho
    
    /**
     * Alguns componentes foram encontrados, mas não todos
     */
    PARCIAL("Parcial", new Color(241, 196, 15)),  // Amarelo/Laranja
    
    /**
     * Usado em filtros para representar "todos os status"
     */
    TODOS("Todos", Color.BLACK);
    
    private final String descricao;
    private final Color cor;
    
    StatusIntegridade(String descricao, Color cor) {
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
            case COMPLETO -> new Color(212, 239, 223);   // Verde claro
            case INCOMPLETO -> new Color(250, 219, 216); // Vermelho claro
            case PARCIAL -> new Color(252, 243, 207);    // Amarelo claro
            case TODOS -> Color.WHITE;
        };
    }
    
    @Override
    public String toString() {
        return descricao;
    }
    
    /**
     * Determina o status baseado na taxa de integridade
     * 
     * @param componentesEncontrados quantidade de componentes encontrados
     * @param componentesEsperados quantidade total de componentes esperados
     * @return o status de integridade correspondente
     */
    public static StatusIntegridade fromContagem(int componentesEncontrados, int componentesEsperados) {
        if (componentesEsperados == 0) {
            return COMPLETO;
        }
        
        if (componentesEncontrados >= componentesEsperados) {
            return COMPLETO;
        } else if (componentesEncontrados > 0) {
            return PARCIAL;
        } else {
            return INCOMPLETO;
        }
    }
    
    /**
     * Determina o status baseado na taxa de integridade percentual
     * 
     * @param taxaIntegridade taxa de 0 a 100
     * @return o status de integridade correspondente
     */
    public static StatusIntegridade fromTaxa(double taxaIntegridade) {
        if (taxaIntegridade >= 100.0) {
            return COMPLETO;
        } else if (taxaIntegridade > 0) {
            return PARCIAL;
        } else {
            return INCOMPLETO;
        }
    }
    
    /**
     * Retorna o status a partir da descrição amigável
     * 
     * @param descricao descrição do status (ex: "Completo", "Incompleto", "Parcial")
     * @return o status correspondente ou TODOS se não encontrado
     */
    public static StatusIntegridade fromDescricao(String descricao) {
        if (descricao == null) {
            return TODOS;
        }
        for (StatusIntegridade status : values()) {
            if (status.getDescricao().equalsIgnoreCase(descricao)) {
                return status;
            }
        }
        return TODOS;
    }
}
