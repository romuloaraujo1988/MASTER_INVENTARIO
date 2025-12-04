package com.inventario.presentation.state;

import com.inventario.model.*;

import java.util.List;

/**
 * Estados possíveis da tela de Relatório de Itens Compostos.
 * Sealed class pattern para estados mutuamente exclusivos.
 * 
 * Segue o padrão MVVM do RelatorioState.java existente.
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public abstract class RelatorioItemCompostoState {
    
    /**
     * Estado inicial - aguardando ação do usuário
     */
    public static class Idle extends RelatorioItemCompostoState {
        public static final Idle INSTANCE = new Idle();
        private Idle() {}
    }
    
    /**
     * Estado de carregamento - operação em andamento
     */
    public static class Loading extends RelatorioItemCompostoState {
        private final String mensagem;
        
        public Loading(String mensagem) {
            this.mensagem = mensagem;
        }
        
        public Loading() {
            this.mensagem = "Carregando...";
        }
        
        public String getMensagem() {
            return mensagem;
        }
    }
    
    /**
     * Relatório gerado com sucesso
     */
    public static class Success extends RelatorioItemCompostoState {
        private final List<ItemCompostoResumo> itens;
        private final EstatisticasIntegridade estatisticas;
        
        public Success(List<ItemCompostoResumo> itens, EstatisticasIntegridade estatisticas) {
            this.itens = itens;
            this.estatisticas = estatisticas;
        }
        
        public List<ItemCompostoResumo> getItens() {
            return itens;
        }
        
        public EstatisticasIntegridade getEstatisticas() {
            return estatisticas;
        }
    }

    /**
     * Detalhes de um item composto carregados
     */
    public static class DetalhesCarregados extends RelatorioItemCompostoState {
        private final Integer idPatrimonio;
        private final String numeroPatrimonio;
        private final String descricaoPatrimonio;
        private final String nomeSala;
        private final String nomeResponsavel;
        private final List<ComponenteDetalhe> componentes;
        
        public DetalhesCarregados(Integer idPatrimonio, String numeroPatrimonio, 
                                   String descricaoPatrimonio, String nomeSala,
                                   String nomeResponsavel, List<ComponenteDetalhe> componentes) {
            this.idPatrimonio = idPatrimonio;
            this.numeroPatrimonio = numeroPatrimonio;
            this.descricaoPatrimonio = descricaoPatrimonio;
            this.nomeSala = nomeSala;
            this.nomeResponsavel = nomeResponsavel;
            this.componentes = componentes;
        }
        
        public Integer getIdPatrimonio() { return idPatrimonio; }
        public String getNumeroPatrimonio() { return numeroPatrimonio; }
        public String getDescricaoPatrimonio() { return descricaoPatrimonio; }
        public String getNomeSala() { return nomeSala; }
        public String getNomeResponsavel() { return nomeResponsavel; }
        public List<ComponenteDetalhe> getComponentes() { return componentes; }
    }
    
    /**
     * Exportação concluída com sucesso
     */
    public static class ExportacaoSucesso extends RelatorioItemCompostoState {
        private final String caminhoArquivo;
        private final String formato;
        
        public ExportacaoSucesso(String caminhoArquivo, String formato) {
            this.caminhoArquivo = caminhoArquivo;
            this.formato = formato;
        }
        
        public String getCaminhoArquivo() {
            return caminhoArquivo;
        }
        
        public String getFormato() {
            return formato;
        }
    }
    
    /**
     * Erro ao executar operação
     */
    public static class Error extends RelatorioItemCompostoState {
        private final String mensagem;
        
        public Error(String mensagem) {
            this.mensagem = mensagem;
        }
        
        public String getMensagem() {
            return mensagem;
        }
    }
    
    /**
     * Inventários carregados para filtro
     */
    public static class InventariosCarregados extends RelatorioItemCompostoState {
        private final List<Inventario> inventarios;
        
        public InventariosCarregados(List<Inventario> inventarios) {
            this.inventarios = inventarios;
        }
        
        public List<Inventario> getInventarios() {
            return inventarios;
        }
    }
    
    /**
     * Setores carregados para filtro
     */
    public static class SetoresCarregados extends RelatorioItemCompostoState {
        private final List<Setor> setores;
        
        public SetoresCarregados(List<Setor> setores) {
            this.setores = setores;
        }
        
        public List<Setor> getSetores() {
            return setores;
        }
    }
    
    /**
     * Responsáveis carregados para filtro
     */
    public static class ResponsaveisCarregados extends RelatorioItemCompostoState {
        private final List<Responsavel> responsaveis;
        
        public ResponsaveisCarregados(List<Responsavel> responsaveis) {
            this.responsaveis = responsaveis;
        }
        
        public List<Responsavel> getResponsaveis() {
            return responsaveis;
        }
    }
    
    /**
     * Salas carregadas para filtro
     */
    public static class SalasCarregadas extends RelatorioItemCompostoState {
        private final List<Sala> salas;
        
        public SalasCarregadas(List<Sala> salas) {
            this.salas = salas;
        }
        
        public List<Sala> getSalas() {
            return salas;
        }
    }
}
