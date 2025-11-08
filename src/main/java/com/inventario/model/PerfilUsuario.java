package com.inventario.model;

/**
 * Enum que define os perfis de usuário do sistema
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public enum PerfilUsuario {
    ADMIN("Administrador", "Acesso total ao sistema"),
    SUPERVISOR("Supervisor", "Supervisiona coletas e relatórios"),
    COLETOR("Coletor", "Pode realizar coletas de inventário"),
    CONSULTA("Consulta", "Apenas consulta de dados");

    private final String nome;
    private final String descricao;

    PerfilUsuario(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return nome;
    }
}