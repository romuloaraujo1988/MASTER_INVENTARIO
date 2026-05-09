package com.inventario.sihcp.presentation.viewmodel;

import com.inventario.sihcp.dao.RelatorioColetaDAO;
import com.inventario.sihcp.dao.InventarioDAO;
import com.inventario.sihcp.dao.SetorDAO;
import com.inventario.sihcp.dao.ResponsavelDAO;
import com.inventario.sihcp.dao.SalaDAO;
import com.inventario.sihcp.model.Inventario;
import com.inventario.sihcp.model.Setor;
import com.inventario.sihcp.model.Responsavel;
import com.inventario.sihcp.model.Sala;
import com.inventario.sihcp.presentation.state.RelatorioState;

import java.util.List;
import java.util.Map;
import java.util.Date;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.sql.SQLException;

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

    // DAOs - Acesso direto (padrão atual do projeto desktop)
    private final RelatorioColetaDAO relatorioDAO;
    private final InventarioDAO inventarioDAO;
    private final SetorDAO setorDAO;
    private final ResponsavelDAO responsavelDAO;
    private final SalaDAO salaDAO;

    // Estado atual
    private RelatorioState state;

    // Suporte para notificação de mudanças (Observer pattern)
    private final PropertyChangeSupport propertyChangeSupport;

    public RelatorioViewModel() {
        this.relatorioDAO = new RelatorioColetaDAO();
        this.inventarioDAO = new InventarioDAO();
        this.setorDAO = new SetorDAO();
        this.responsavelDAO = new ResponsavelDAO();
        this.salaDAO = new SalaDAO();
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
            List<Inventario> inventarios = inventarioDAO.findAll();
            setState(new RelatorioState.InventariosCarregados(inventarios));
        } catch (SQLException e) {
            setState(new RelatorioState.Error("Erro ao carregar inventários: " + e.getMessage()));
        }
    }

    /**
     * Carrega lista de setores
     */
    public void carregarSetores() {
        try {
            List<Setor> setores = setorDAO.findAll();
            setState(new RelatorioState.SetoresCarregados(setores));
        } catch (SQLException e) {
            setState(new RelatorioState.Error("Erro ao carregar setores: " + e.getMessage()));
        }
    }

    /**
     * Carrega lista de responsáveis
     */
    public void carregarResponsaveis() {
        try {
            List<Responsavel> responsaveis = responsavelDAO.findAll();
            setState(new RelatorioState.ResponsaveisCarregados(responsaveis));
        } catch (SQLException e) {
            setState(new RelatorioState.Error("Erro ao carregar responsáveis: " + e.getMessage()));
        }
    }

    /**
     * Carrega responsáveis de um setor específico
     */
    public void carregarResponsaveisPorSetor(int idSetor) {
        try {
            List<Responsavel> responsaveis = responsavelDAO.buscarPorSetor(idSetor);
            setState(new RelatorioState.ResponsaveisCarregados(responsaveis));
        } catch (SQLException e) {
            setState(new RelatorioState.Error("Erro ao carregar responsáveis do setor: " + e.getMessage()));
        }
    }

    /**
     * Carrega lista de salas com contagem de patrimônios
     * Ordenadas por quantidade de patrimônios (decrescente)
     */
    public void carregarSalas() {
        try {
            // Usar método que retorna contagem de patrimônios por sala
            List<Sala> salas = salaDAO.listarSalasComContagemPatrimonios();
            setState(new RelatorioState.SalasCarregadas(salas));
        } catch (SQLException e) {
            // Fallback para método simples se houver erro
            try {
                List<Sala> salas = salaDAO.listarSalas();
                setState(new RelatorioState.SalasCarregadas(salas));
            } catch (SQLException e2) {
                setState(new RelatorioState.Error("Erro ao carregar salas: " + e2.getMessage()));
            }
        }
    }

    /**
     * Gera relatório baseado no tipo selecionado
     */
    /**
     * Gera relatório baseado no tipo selecionado
     * @param tipoRelatorio Tipo do relatório a ser gerado
     * @param idInventario ID do inventário selecionado
     * @param setor Nome do setor (ou "Todos")
     * @param responsavel Nome do responsável (ou "Todos")
     * @param dataInicio Data inicial do período
     * @param dataFim Data final do período
     * @param status Status do patrimônio
     */
    public void gerarRelatorio(String tipoRelatorio, int idInventario, String setor,
            String responsavel, Date dataInicio, Date dataFim, String status) {
        // Chamar versão completa com sala = null
        gerarRelatorio(tipoRelatorio, idInventario, setor, responsavel, dataInicio, dataFim, status, null);
    }

    /**
     * Gera relatório baseado no tipo selecionado (versão completa com filtro de sala)
     * @param tipoRelatorio Tipo do relatório a ser gerado
     * @param idInventario ID do inventário selecionado
     * @param setor Nome do setor (ou "Todos")
     * @param responsavel Nome do responsável (ou "Todos")
     * @param dataInicio Data inicial do período
     * @param dataFim Data final do período
     * @param status Status do patrimônio
     * @param sala Nome da sala para filtro (ou null/vazio para todas)
     */
    public void gerarRelatorio(String tipoRelatorio, int idInventario, String setor,
            String responsavel, Date dataInicio, Date dataFim, String status, String sala) {
        try {
            // Validar entrada
            if (idInventario == -1 && !tipoRelatorio.equals("Relatório Geral de Patrimônio")) {
                setState(new RelatorioState.Error("Selecione um inventário"));
                return;
            }

            setState(RelatorioState.Loading.INSTANCE);

            List<Map<String, Object>> dados;

            // Delegar para DAO apropriado baseado no tipo
            switch (tipoRelatorio) {
                case "Itens Encontrados" -> dados = relatorioDAO.gerarRelatorioItensEncontrados(idInventario);
                case "Itens Não Encontrados" -> dados = relatorioDAO.gerarRelatorioItensNaoEncontrados(idInventario);
                case "Itens Sem Plaqueta de Patrimônio" -> dados = relatorioDAO.gerarRelatorioItensSemEtiqueta(idInventario);
                case "Relatório por Responsável" -> {
                    if (!"Todos".equals(responsavel)) {
                        dados = relatorioDAO.gerarRelatorioDetalhadoPorResponsavel(idInventario, responsavel, status);
                    } else {
                        dados = relatorioDAO.gerarRelatorioItensEncontrados(idInventario);
                    }
                }
                case "Itens Não Coletados" -> dados = relatorioDAO.gerarRelatorioItensNaoColetados(idInventario);
                case "Relatório de Divergências" -> dados = relatorioDAO.gerarRelatorioDivergencias(idInventario);
                case "Estatísticas do Inventário" -> dados = relatorioDAO.gerarEstatisticasGerais(idInventario);
                case "Relatório Avançado por Setor" -> {
                    String setorSelecionado = "Todos".equals(setor) ? "" : setor;
                    dados = relatorioDAO.gerarRelatorioAvancadoPorSetor(idInventario, setorSelecionado,
                            dataInicio, dataFim, status);
                }
                case "Relatório Avançado por Responsável" -> {
                    String responsavelSelecionado = "Todos".equals(responsavel) ? "" : responsavel;
                    dados = relatorioDAO.gerarRelatorioAvancadoPorResponsavel(idInventario,
                            responsavelSelecionado,
                            dataInicio, dataFim, status);
                }
                case "Relatório Avançado por Período" -> dados = relatorioDAO.gerarRelatorioAvancadoPorPeriodo(idInventario, dataInicio, dataFim);
                case "Estatísticas Avançadas por Setor" -> dados = relatorioDAO.gerarEstatisticasAvancadasPorSetor(idInventario, dataInicio, dataFim);
                case "Relatório Consolidado Executivo" -> dados = relatorioDAO.gerarRelatorioConsolidado(idInventario, dataInicio, dataFim);
                case "Relatório de Coletas por Sala" -> {
                    // Usar filtro de sala se fornecido, senão usar string vazia (todas as salas)
                    String salaSelecionada = (sala != null && !sala.isEmpty() && !"Todas as Salas".equals(sala)) 
                            ? sala : "";
                    dados = relatorioDAO.gerarRelatorioColetasPorSala(idInventario, salaSelecionada);
                }
                case "Estatísticas de Coletas por Sala" -> dados = relatorioDAO.gerarEstatisticasColetasPorSala(idInventario);
                default -> dados = relatorioDAO.gerarRelatorioGeralCompleto(idInventario);
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
