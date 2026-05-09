package com.inventario.sihcp.service;

import com.inventario.sihcp.dao.ItemCompostoRelatorioDAO;
import com.inventario.sihcp.model.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Service para geração do relatório de integridade de itens compostos.
 * Coordena o DAO e implementa lógica de negócio como ordenação.
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class RelatorioItemCompostoService {
    
    private final ItemCompostoRelatorioDAO dao;
    
    public RelatorioItemCompostoService() {
        this.dao = new ItemCompostoRelatorioDAO();
    }
    
    public RelatorioItemCompostoService(ItemCompostoRelatorioDAO dao) {
        this.dao = dao;
    }
    
    /**
     * Gera o relatório de itens compostos com filtros aplicados
     * 
     * @param filtro critérios de filtro
     * @return lista de ItemCompostoResumo
     */
    public List<ItemCompostoResumo> gerarRelatorio(FiltroRelatorioItemComposto filtro) throws SQLException {
        return dao.buscarResumoComFiltros(filtro);
    }
    
    /**
     * Calcula as estatísticas de integridade
     * 
     * @param filtro critérios de filtro
     * @return EstatisticasIntegridade
     */
    public EstatisticasIntegridade calcularEstatisticas(FiltroRelatorioItemComposto filtro) throws SQLException {
        return dao.calcularEstatisticas(filtro);
    }
    
    /**
     * Obtém os detalhes dos componentes de um patrimônio
     * 
     * @param idPatrimonio ID do patrimônio
     * @param idInventario ID do inventário
     * @return lista de ComponenteDetalhe
     */
    public List<ComponenteDetalhe> obterDetalhes(Integer idPatrimonio, Integer idInventario) throws SQLException {
        return dao.buscarComponentesPorPatrimonio(idPatrimonio, idInventario);
    }
    
    /**
     * Ordena a lista de itens compostos por uma coluna específica
     * 
     * @param itens lista de itens a ordenar
     * @param coluna nome da coluna para ordenação
     * @param crescente true para ordem crescente, false para decrescente
     * @return lista ordenada (nova instância)
     */
    public List<ItemCompostoResumo> ordenar(List<ItemCompostoResumo> itens, String coluna, boolean crescente) {
        if (itens == null || itens.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<ItemCompostoResumo> resultado = new ArrayList<>(itens);
        
        Comparator<ItemCompostoResumo> comparator = getComparator(coluna);
        
        if (!crescente) {
            comparator = comparator.reversed();
        }
        
        resultado.sort(comparator);
        
        return resultado;
    }
    
    /**
     * Retorna o comparator apropriado para a coluna especificada
     */
    private Comparator<ItemCompostoResumo> getComparator(String coluna) {
        if (coluna == null) {
            return Comparator.comparing(ItemCompostoResumo::getNumeroPatrimonio, 
                    Comparator.nullsLast(String::compareToIgnoreCase));
        }
        
        return switch (coluna.toLowerCase()) {
            case "numero", "numeropatrimonio", "nº patrimônio" -> 
                Comparator.comparing(ItemCompostoResumo::getNumeroPatrimonio, 
                        Comparator.nullsLast(String::compareToIgnoreCase));
                        
            case "descricao", "descricaopatrimonio", "descrição" -> 
                Comparator.comparing(ItemCompostoResumo::getDescricaoPatrimonio, 
                        Comparator.nullsLast(String::compareToIgnoreCase));
                        
            case "sala", "nomesala" -> 
                Comparator.comparing(ItemCompostoResumo::getNomeSala, 
                        Comparator.nullsLast(String::compareToIgnoreCase));
                        
            case "responsavel", "nomeresponsavel", "responsável" -> 
                Comparator.comparing(ItemCompostoResumo::getNomeResponsavel, 
                        Comparator.nullsLast(String::compareToIgnoreCase));
                        
            case "esperados", "componentesesperados" -> 
                Comparator.comparingInt(ItemCompostoResumo::getComponentesEsperados);
                
            case "encontrados", "componentesencontrados" -> 
                Comparator.comparingInt(ItemCompostoResumo::getComponentesEncontrados);
                
            case "faltantes", "componentesfaltantes" -> 
                Comparator.comparingInt(ItemCompostoResumo::getComponentesFaltantes);
                
            case "taxa", "taxaintegridade", "taxa de integridade" -> 
                // Ordenação NUMÉRICA para taxa (não alfabética)
                Comparator.comparingDouble(ItemCompostoResumo::getTaxaIntegridade);
                
            case "status" -> 
                Comparator.comparing(item -> item.getStatus() != null ? item.getStatus().ordinal() : 999);
                
            default -> 
                Comparator.comparing(ItemCompostoResumo::getNumeroPatrimonio, 
                        Comparator.nullsLast(String::compareToIgnoreCase));
        };
    }
    
    // === Métodos para carregar dados de filtros ===
    
    /**
     * Lista inventários que possuem itens compostos
     */
    public List<Inventario> listarInventarios() throws SQLException {
        return dao.listarInventariosComItensCompostos();
    }
    
    /**
     * Lista setores que possuem itens compostos
     */
    public List<Setor> listarSetores() throws SQLException {
        return dao.listarSetoresComItensCompostos();
    }
    
    /**
     * Lista salas que possuem itens compostos
     * 
     * @param idSetor ID do setor (null para todas)
     */
    public List<Sala> listarSalas(Integer idSetor) throws SQLException {
        return dao.listarSalasComItensCompostos(idSetor);
    }
    
    /**
     * Lista responsáveis que possuem itens compostos
     */
    public List<Responsavel> listarResponsaveis() throws SQLException {
        return dao.listarResponsaveisComItensCompostos();
    }
}
