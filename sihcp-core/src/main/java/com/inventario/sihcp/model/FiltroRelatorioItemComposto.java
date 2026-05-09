package com.inventario.sihcp.model;

/**
 * Classe que representa os filtros aplicáveis ao relatório de itens compostos.
 * Usada para passar critérios de busca para o DAO.
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class FiltroRelatorioItemComposto {
    
    // Filtros de identificação
    private Integer idInventario;
    private Integer idSetor;
    private Integer idSala;
    private Integer idResponsavel;
    
    // Filtro de status
    private StatusIntegridade statusIntegridade;
    
    // Filtro de texto (pesquisa)
    private String textoPesquisa;
    
    public FiltroRelatorioItemComposto() {
        this.statusIntegridade = StatusIntegridade.TODOS;
    }
    
    // === Métodos de Negócio ===
    
    /**
     * Verifica se há algum filtro ativo (além do status TODOS)
     * 
     * @return true se pelo menos um filtro está definido
     */
    public boolean temFiltrosAtivos() {
        return idInventario != null ||
               idSetor != null ||
               idSala != null ||
               idResponsavel != null ||
               (statusIntegridade != null && statusIntegridade != StatusIntegridade.TODOS) ||
               (textoPesquisa != null && !textoPesquisa.trim().isEmpty());
    }
    
    /**
     * Limpa todos os filtros, retornando ao estado inicial
     */
    public void limpar() {
        this.idInventario = null;
        this.idSetor = null;
        this.idSala = null;
        this.idResponsavel = null;
        this.statusIntegridade = StatusIntegridade.TODOS;
        this.textoPesquisa = null;
    }
    
    /**
     * Cria uma cópia do filtro atual
     */
    public FiltroRelatorioItemComposto copiar() {
        FiltroRelatorioItemComposto copia = new FiltroRelatorioItemComposto();
        copia.setIdInventario(this.idInventario);
        copia.setIdSetor(this.idSetor);
        copia.setIdSala(this.idSala);
        copia.setIdResponsavel(this.idResponsavel);
        copia.setStatusIntegridade(this.statusIntegridade);
        copia.setTextoPesquisa(this.textoPesquisa);
        return copia;
    }
    
    /**
     * Retorna uma descrição dos filtros ativos para exibição
     */
    public String getDescricaoFiltros() {
        StringBuilder sb = new StringBuilder();
        
        if (idInventario != null) {
            sb.append("Inventário: ").append(idInventario).append("; ");
        }
        if (idSetor != null) {
            sb.append("Setor: ").append(idSetor).append("; ");
        }
        if (idSala != null) {
            sb.append("Sala: ").append(idSala).append("; ");
        }
        if (idResponsavel != null) {
            sb.append("Responsável: ").append(idResponsavel).append("; ");
        }
        if (statusIntegridade != null && statusIntegridade != StatusIntegridade.TODOS) {
            sb.append("Status: ").append(statusIntegridade.getDescricao()).append("; ");
        }
        if (textoPesquisa != null && !textoPesquisa.trim().isEmpty()) {
            sb.append("Pesquisa: '").append(textoPesquisa).append("'; ");
        }
        
        if (sb.length() == 0) {
            return "Nenhum filtro aplicado";
        }
        
        return sb.toString();
    }
    
    // === Getters e Setters ===
    
    public Integer getIdInventario() {
        return idInventario;
    }
    
    public void setIdInventario(Integer idInventario) {
        this.idInventario = idInventario;
    }
    
    public Integer getIdSetor() {
        return idSetor;
    }
    
    public void setIdSetor(Integer idSetor) {
        this.idSetor = idSetor;
    }
    
    public Integer getIdSala() {
        return idSala;
    }
    
    public void setIdSala(Integer idSala) {
        this.idSala = idSala;
    }
    
    public Integer getIdResponsavel() {
        return idResponsavel;
    }
    
    public void setIdResponsavel(Integer idResponsavel) {
        this.idResponsavel = idResponsavel;
    }
    
    public StatusIntegridade getStatusIntegridade() {
        return statusIntegridade;
    }
    
    public void setStatusIntegridade(StatusIntegridade statusIntegridade) {
        this.statusIntegridade = statusIntegridade != null ? statusIntegridade : StatusIntegridade.TODOS;
    }
    
    public String getTextoPesquisa() {
        return textoPesquisa;
    }
    
    public void setTextoPesquisa(String textoPesquisa) {
        this.textoPesquisa = textoPesquisa;
    }
    
    @Override
    public String toString() {
        return "FiltroRelatorioItemComposto{" +
                "idInventario=" + idInventario +
                ", idSetor=" + idSetor +
                ", idSala=" + idSala +
                ", idResponsavel=" + idResponsavel +
                ", status=" + statusIntegridade +
                ", pesquisa='" + textoPesquisa + '\'' +
                '}';
    }
}
