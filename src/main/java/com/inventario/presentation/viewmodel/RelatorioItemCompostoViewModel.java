package com.inventario.presentation.viewmodel;

import com.inventario.dao.ItemCompostoRelatorioDAO;
import com.inventario.model.*;
import com.inventario.presentation.state.RelatorioItemCompostoState;
import com.inventario.service.ExportacaoItemCompostoService;
import com.inventario.service.RelatorioItemCompostoService;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel para tela de Relatório de Itens Compostos.
 * Segue padrão MVVM - gerencia estado da UI e coordena operações de negócio.
 * 
 * Responsabilidades:
 * - Gerenciar estado da UI (RelatorioItemCompostoState)
 * - Coordenar chamadas aos Services
 * - Notificar View sobre mudanças de estado
 * - Validar entrada do usuário
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class RelatorioItemCompostoViewModel {

    // Services
    private final RelatorioItemCompostoService relatorioService;
    private final ExportacaoItemCompostoService exportacaoService;
    private final ItemCompostoRelatorioDAO relatorioDAO;

    // Estado atual
    private RelatorioItemCompostoState state;
    
    // Dados em cache para ordenação e exportação
    private List<ItemCompostoResumo> itensAtuais;
    private EstatisticasIntegridade estatisticasAtuais;
    private FiltroRelatorioItemComposto filtroAtual;
    private Integer inventarioAtivoId;
    
    // Controle de ordenação
    private String colunaOrdenacaoAtual;
    private boolean ordenacaoCrescente = true;

    // Suporte para notificação de mudanças (Observer pattern)
    private final PropertyChangeSupport propertyChangeSupport;

    public RelatorioItemCompostoViewModel() {
        this.relatorioService = new RelatorioItemCompostoService();
        this.exportacaoService = new ExportacaoItemCompostoService();
        this.relatorioDAO = new ItemCompostoRelatorioDAO();
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.state = RelatorioItemCompostoState.Idle.INSTANCE;
        this.itensAtuais = new ArrayList<>();
        this.filtroAtual = new FiltroRelatorioItemComposto();
    }


    // ========== GERENCIAMENTO DE ESTADO ==========

    public RelatorioItemCompostoState getState() {
        return state;
    }

    private void setState(RelatorioItemCompostoState newState) {
        RelatorioItemCompostoState oldState = this.state;
        this.state = newState;
        propertyChangeSupport.firePropertyChange("state", oldState, newState);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    // ========== GETTERS PARA DADOS EM CACHE ==========
    
    public List<ItemCompostoResumo> getItensAtuais() {
        return itensAtuais;
    }
    
    public EstatisticasIntegridade getEstatisticasAtuais() {
        return estatisticasAtuais;
    }
    
    public FiltroRelatorioItemComposto getFiltroAtual() {
        return filtroAtual;
    }
    
    public Integer getInventarioAtivoId() {
        return inventarioAtivoId;
    }
    
    public void setInventarioAtivoId(Integer id) {
        this.inventarioAtivoId = id;
    }

    // ========== OPERAÇÕES DE CARREGAMENTO DE FILTROS ==========

    /**
     * Carrega lista de inventários que possuem itens compostos
     */
    public void carregarInventarios() {
        try {
            List<Inventario> inventarios = relatorioDAO.listarInventariosComItensCompostos();
            setState(new RelatorioItemCompostoState.InventariosCarregados(inventarios));
        } catch (Exception e) {
            setState(new RelatorioItemCompostoState.Error("Erro ao carregar inventários: " + e.getMessage()));
        }
    }

    /**
     * Carrega lista de setores que possuem itens compostos
     */
    public void carregarSetores() {
        try {
            List<Setor> setores = relatorioDAO.listarSetoresComItensCompostos();
            setState(new RelatorioItemCompostoState.SetoresCarregados(setores));
        } catch (Exception e) {
            setState(new RelatorioItemCompostoState.Error("Erro ao carregar setores: " + e.getMessage()));
        }
    }

    /**
     * Carrega lista de salas de um setor específico
     */
    public void carregarSalas(Integer idSetor) {
        try {
            List<Sala> salas = relatorioDAO.listarSalasComItensCompostos(idSetor);
            setState(new RelatorioItemCompostoState.SalasCarregadas(salas));
        } catch (Exception e) {
            setState(new RelatorioItemCompostoState.Error("Erro ao carregar salas: " + e.getMessage()));
        }
    }

    /**
     * Carrega lista de responsáveis que possuem itens compostos
     */
    public void carregarResponsaveis() {
        try {
            List<Responsavel> responsaveis = relatorioDAO.listarResponsaveisComItensCompostos();
            setState(new RelatorioItemCompostoState.ResponsaveisCarregados(responsaveis));
        } catch (Exception e) {
            setState(new RelatorioItemCompostoState.Error("Erro ao carregar responsáveis: " + e.getMessage()));
        }
    }


    // ========== OPERAÇÕES DE RELATÓRIO ==========

    /**
     * Carrega o relatório de integridade com os filtros especificados
     */
    public void carregarRelatorio(FiltroRelatorioItemComposto filtro) {
        try {
            // Validar filtro
            if (filtro.getIdInventario() == null) {
                setState(new RelatorioItemCompostoState.Error("Selecione um inventário"));
                return;
            }

            setState(new RelatorioItemCompostoState.Loading("Carregando relatório..."));
            
            this.filtroAtual = filtro;

            // Buscar dados
            List<ItemCompostoResumo> itens = relatorioService.gerarRelatorio(filtro);
            EstatisticasIntegridade stats = relatorioService.calcularEstatisticas(filtro);
            
            // Armazenar em cache
            this.itensAtuais = itens;
            this.estatisticasAtuais = stats;
            
            // Resetar ordenação
            this.colunaOrdenacaoAtual = null;
            this.ordenacaoCrescente = true;

            setState(new RelatorioItemCompostoState.Success(itens, stats));

        } catch (Exception e) {
            setState(new RelatorioItemCompostoState.Error("Erro ao carregar relatório: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    /**
     * Ordena os itens por uma coluna específica.
     * Se a mesma coluna for clicada novamente, inverte a ordenação.
     */
    public void ordenarPor(String coluna) {
        try {
            if (itensAtuais == null || itensAtuais.isEmpty()) {
                return;
            }

            // Toggle ordenação se mesma coluna
            if (coluna.equals(colunaOrdenacaoAtual)) {
                ordenacaoCrescente = !ordenacaoCrescente;
            } else {
                colunaOrdenacaoAtual = coluna;
                ordenacaoCrescente = true;
            }

            // Ordenar usando o service
            itensAtuais = relatorioService.ordenar(itensAtuais, coluna, ordenacaoCrescente);

            // Notificar mudança (mantém estatísticas)
            setState(new RelatorioItemCompostoState.Success(itensAtuais, estatisticasAtuais));

        } catch (Exception e) {
            setState(new RelatorioItemCompostoState.Error("Erro ao ordenar: " + e.getMessage()));
        }
    }

    /**
     * Carrega os detalhes de um item composto específico
     */
    public void carregarDetalhes(ItemCompostoResumo item) {
        try {
            if (item == null || filtroAtual.getIdInventario() == null) {
                return;
            }

            setState(new RelatorioItemCompostoState.Loading("Carregando detalhes..."));

            List<ComponenteDetalhe> componentes = relatorioService.obterDetalhes(
                    item.getIdPatrimonio(), filtroAtual.getIdInventario());

            setState(new RelatorioItemCompostoState.DetalhesCarregados(
                    item.getIdPatrimonio(),
                    item.getNumeroPatrimonio(),
                    item.getDescricaoPatrimonio(),
                    item.getNomeSala(),
                    item.getNomeResponsavel(),
                    componentes
            ));

        } catch (Exception e) {
            setState(new RelatorioItemCompostoState.Error("Erro ao carregar detalhes: " + e.getMessage()));
        }
    }


    // ========== OPERAÇÕES DE EXPORTAÇÃO ==========

    /**
     * Exporta o relatório para Excel
     */
    public void exportarExcel(String caminhoArquivo) {
        try {
            if (itensAtuais == null || itensAtuais.isEmpty()) {
                setState(new RelatorioItemCompostoState.Error("Não há dados para exportar"));
                return;
            }

            setState(new RelatorioItemCompostoState.Loading("Exportando para Excel..."));

            File arquivo = exportacaoService.exportarExcel(itensAtuais, estatisticasAtuais, caminhoArquivo);

            setState(new RelatorioItemCompostoState.ExportacaoSucesso(arquivo.getAbsolutePath(), "Excel"));

        } catch (Exception e) {
            setState(new RelatorioItemCompostoState.Error("Erro ao exportar Excel: " + e.getMessage()));
        }
    }

    /**
     * Exporta o relatório para PDF
     */
    public void exportarPDF(String caminhoArquivo) {
        try {
            if (itensAtuais == null || itensAtuais.isEmpty()) {
                setState(new RelatorioItemCompostoState.Error("Não há dados para exportar"));
                return;
            }

            setState(new RelatorioItemCompostoState.Loading("Exportando para PDF..."));

            File arquivo = exportacaoService.exportarPDF(itensAtuais, estatisticasAtuais, caminhoArquivo);

            setState(new RelatorioItemCompostoState.ExportacaoSucesso(arquivo.getAbsolutePath(), "PDF"));

        } catch (Exception e) {
            setState(new RelatorioItemCompostoState.Error("Erro ao exportar PDF: " + e.getMessage()));
        }
    }

    /**
     * Exporta o relatório para CSV
     */
    public void exportarCSV(String caminhoArquivo) {
        try {
            if (itensAtuais == null || itensAtuais.isEmpty()) {
                setState(new RelatorioItemCompostoState.Error("Não há dados para exportar"));
                return;
            }

            setState(new RelatorioItemCompostoState.Loading("Exportando para CSV..."));

            File arquivo = exportacaoService.exportarCSV(itensAtuais, caminhoArquivo);

            setState(new RelatorioItemCompostoState.ExportacaoSucesso(arquivo.getAbsolutePath(), "CSV"));

        } catch (Exception e) {
            setState(new RelatorioItemCompostoState.Error("Erro ao exportar CSV: " + e.getMessage()));
        }
    }

    // ========== OPERAÇÕES AUXILIARES ==========

    /**
     * Limpa o estado atual e volta para Idle
     */
    public void limpar() {
        this.itensAtuais = new ArrayList<>();
        this.estatisticasAtuais = null;
        this.filtroAtual = new FiltroRelatorioItemComposto();
        this.colunaOrdenacaoAtual = null;
        this.ordenacaoCrescente = true;
        setState(RelatorioItemCompostoState.Idle.INSTANCE);
    }

    /**
     * Volta para o estado de sucesso após fechar diálogo de detalhes
     */
    public void voltarParaRelatorio() {
        if (itensAtuais != null && !itensAtuais.isEmpty() && estatisticasAtuais != null) {
            setState(new RelatorioItemCompostoState.Success(itensAtuais, estatisticasAtuais));
        } else {
            setState(RelatorioItemCompostoState.Idle.INSTANCE);
        }
    }

    /**
     * Retorna informações sobre a ordenação atual
     */
    public String getColunaOrdenacaoAtual() {
        return colunaOrdenacaoAtual;
    }

    public boolean isOrdenacaoCrescente() {
        return ordenacaoCrescente;
    }
}
