package com.inventario.presentation.viewmodel;

import com.inventario.dao.RelatorioColetaDAO;
import com.inventario.dao.InventarioDAO;
import com.inventario.dao.SetorDAORefactored;
import com.inventario.dao.ResponsavelDAO;
import com.inventario.dao.SalaDAORefactored;
import com.inventario.model.Inventario;
import com.inventario.model.Setor;
import com.inventario.model.Responsavel;
import com.inventario.model.Sala;
import com.inventario.presentation.state.RelatorioState;

import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.ArrayList;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * ViewModel para tela de Relatórios
 * Segue padrão MVVM - gerencia estado da UI e coordena operações de negócio
 * 
 * Responsabilidades:
 * - Gerenciar estado da UI (RelatorioState)
 * - Coordenar chamadas aos DAOs (temporário até migrar para Use Cases)
 * - Notificar View sobre mudanças de estado
 * - Validar entrada do usuário
 */
public class RelatorioViewModel {
    
    // DAOs - Acesso temporário direto (TODO: migrar para Use Cases)
    private final RelatorioColetaDAO relatorioDAO;
    private final InventarioDAO inventarioDAO;
    private final SetorDAORefactored setorDAO;
    private final ResponsavelDAO responsavelDAO;
    private final SalaDAORefactored salaDAO;
    
    // Estado atual
    private RelatorioState state;
    
    // Suporte para notificação de mudanças (Observer pattern)
    private final PropertyChangeSupport propertyChangeSupport;
    
    public RelatorioViewModel() {
        this.relatorioDAO = new RelatorioColetaDAO();
        this.inventarioDAO = new InventarioDAO();
        this.setorDAO = new SetorDAORefactored();
        this.responsavelDAO = new ResponsavelDAO();
        this.salaDAO = new SalaDAORefactored();
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.state = RelatorioState.Idle.INSTANCE;
    }
    
    // ========== GERENCIAMENTO DE ESTADO ==========
    
    public RelatorioState getState() {
        return state;
    }
    
    private void setState(RelatorioState newState) {
        RelatorioState oldState = this.state;
        this.state = newState;
        propertyChangeSupport.firePropertyChange("state", oldState, newState);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
    
    // ========== OPERAÇÕES DE NEGÓCIO ==========
    
    /**
     * Carrega lista de inventários disponíveis
     */
    public void carregarInventarios() {
        try {
            // Não mostrar loading para carregamento de combos (operação rápida)
            List<Inventario> inventarios = inventarioDAO.listarInventarios();
            setState(new RelatorioState.InventariosCarregados(inventarios));
        } catch (Exception e) {
            setState(new RelatorioState.Error("Erro ao carregar inventários: " + e.getMessage()));
        }
    }
    
    /**
     * Carrega lista de setores
     */
    public void carregarSetores() {
        try {
            List<Setor> setores = setorDAO.listarSetores();
            setState(new RelatorioState.SetoresCarregados(setores));
        } catch (Exception e) {
            setState(new RelatorioState.Error("Erro ao carregar setores: " + e.getMessage()));
        }
    }
    
    /**
     * Carrega lista de responsáveis
     */
    public void carregarResponsaveis() {
        try {
            List<Responsavel> responsaveis = responsavelDAO.listarResponsaveis();
            setState(new RelatorioState.ResponsaveisCarregados(responsaveis));
        } catch (Exception e) {
            setState(new RelatorioState.Error("Erro ao carregar responsáveis: " + e.getMessage()));
        }
    }
    
    /**
     * Carrega responsáveis de um setor específico
     */
    public void carregarResponsaveisPorSetor(int idSetor) {
        try {
            List<Responsavel> responsaveis = responsavelDAO.listarResponsaveisPorSetor(idSetor);
            setState(new RelatorioState.ResponsaveisCarregados(responsaveis));
        } catch (Exception e) {
            setState(new RelatorioState.Error("Erro ao carregar responsáveis do setor: " + e.getMessage()));
        }
    }
    
    /**
     * Carrega lista de salas
     */
    public void carregarSalas() {
        try {
            List<Sala> salas = salaDAO.listarSalas();
            setState(new RelatorioState.SalasCarregadas(salas));
        } catch (Exception e) {
            setState(new RelatorioState.Error("Erro ao carregar salas: " + e.getMessage()));
        }
    }
    
    /**
     * Gera relatório baseado no tipo selecionado
     */
    public void gerarRelatorio(String tipoRelatorio, int idInventario, String setor, 
                               String responsavel, Date dataInicio, Date dataFim) {
        try {
            // Validar entrada
            if (idInventario == -1 && !tipoRelatorio.equals("Relatório Geral de Patrimônio")) {
                setState(new RelatorioState.Error("Selecione um inventário"));
                return;
            }
            
            setState(RelatorioState.Loading.INSTANCE);
            
            List<Map<String, Object>> dados = new ArrayList<>();
            
            // Delegar para DAO apropriado baseado no tipo
            switch (tipoRelatorio) {
                case "Itens Encontrados":
                    dados = relatorioDAO.gerarRelatorioItensEncontrados(idInventario);
                    break;
                case "Itens Não Encontrados":
                    dados = relatorioDAO.gerarRelatorioItensNaoEncontrados(idInventario);
                    break;
                case "Itens Sem Plaqueta de Patrimônio":
                    dados = relatorioDAO.gerarRelatorioItensSemEtiqueta(idInventario);
                    break;
                case "Relatório por Responsável":
                    if (!"Todos".equals(responsavel)) {
                        dados = relatorioDAO.gerarRelatorioDetalhadoPorResponsavel(idInventario, responsavel);
                    } else {
                        dados = relatorioDAO.gerarRelatorioItensEncontrados(idInventario);
                    }
                    break;
                case "Itens Não Coletados":
                    dados = relatorioDAO.gerarRelatorioItensNaoColetados(idInventario);
                    break;
                case "Relatório de Divergências":
                    dados = relatorioDAO.gerarRelatorioDivergencias(idInventario);
                    break;
                case "Estatísticas do Inventário":
                    dados = relatorioDAO.gerarEstatisticasGerais(idInventario);
                    break;
                case "Relatório Avançado por Setor":
                    String setorSelecionado = "Todos".equals(setor) ? "" : setor;
                    dados = relatorioDAO.gerarRelatorioAvancadoPorSetor(idInventario, setorSelecionado, 
                                                                        dataInicio, dataFim);
                    break;
                case "Relatório Avançado por Responsável":
                    String responsavelSelecionado = "Todos".equals(responsavel) ? "" : responsavel;
                    dados = relatorioDAO.gerarRelatorioAvancadoPorResponsavel(idInventario, 
                                                                              responsavelSelecionado, 
                                                                              dataInicio, dataFim);
                    break;
                case "Relatório Avançado por Período":
                    dados = relatorioDAO.gerarRelatorioAvancadoPorPeriodo(idInventario, dataInicio, dataFim);
                    break;
                case "Estatísticas Avançadas por Setor":
                    dados = relatorioDAO.gerarEstatisticasAvancadasPorSetor(idInventario, dataInicio, dataFim);
                    break;
                case "Relatório Consolidado Executivo":
                    dados = relatorioDAO.gerarRelatorioConsolidado(idInventario, dataInicio, dataFim);
                    break;
                default:
                    dados = relatorioDAO.gerarRelatorioGeralCompleto(idInventario);
            }
            
            setState(new RelatorioState.Success(dados, tipoRelatorio));
            
        } catch (Exception e) {
            setState(new RelatorioState.Error("Erro ao gerar relatório: " + e.getMessage()));
        }
    }
    
    /**
     * Limpa o estado atual
     */
    public void limpar() {
        setState(RelatorioState.Idle.INSTANCE);
    }
}
