package com.inventario.sihcp.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.inventario.sihcp.model.ComponenteDetalhe;
import com.inventario.sihcp.model.EstatisticasIntegridade;
import com.inventario.sihcp.model.FiltroRelatorioItemComposto;
import com.inventario.sihcp.model.Inventario;
import com.inventario.sihcp.model.ItemCompostoResumo;
import com.inventario.sihcp.model.Responsavel;
import com.inventario.sihcp.model.Sala;
import com.inventario.sihcp.model.Setor;
import com.inventario.sihcp.model.StatusComponente;
import com.inventario.sihcp.model.StatusIntegridade;
import com.inventario.sihcp.util.DatabaseConnection;

/**
 * DAO especializado para o relatório de integridade de itens compostos.
 * Fornece métodos otimizados para consultas com filtros dinâmicos.
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class ItemCompostoRelatorioDAO {
    
    /**
     * Busca o resumo de itens compostos com filtros aplicados
     * 
     * @param filtro critérios de filtro
     * @return lista de ItemCompostoResumo
     */
    public List<ItemCompostoResumo> buscarResumoComFiltros(FiltroRelatorioItemComposto filtro) throws SQLException {
        List<ItemCompostoResumo> resultado = new ArrayList<>();
        
        StringBuilder sql = new StringBuilder();
        sql.append("""
            SELECT 
                p.id AS id_patrimonio,
                p.numero AS numero_patrimonio,
                p.descricao AS descricao_patrimonio,
                s.id AS id_sala,
                COALESCE(s.descricao, 'Sem sala') AS nome_sala,
                r.id AS id_responsavel,
                COALESCE(r.nome, 'Sem responsável') AS nome_responsavel,
                COUNT(ic.id) AS total_componentes,
                SUM(ic.quantidade_esperada) AS componentes_esperados,
                COALESCE(SUM(cc.quantidade_encontrada), 0) AS componentes_encontrados,
                SUM(ic.quantidade_esperada) - COALESCE(SUM(cc.quantidade_encontrada), 0) AS componentes_faltantes,
                ROUND(
                    (COALESCE(SUM(cc.quantidade_encontrada), 0)::DECIMAL / 
                     NULLIF(SUM(ic.quantidade_esperada), 0)) * 100, 2
                ) AS taxa_integridade,
                STRING_AGG(
                    CASE WHEN COALESCE(cc.quantidade_encontrada, 0) < ic.quantidade_esperada 
                         THEN ic.tipo_componente END, 
                    ', '
                ) AS tipos_faltantes
            FROM tabela_patrimonio p
            INNER JOIN tabela_item_composto ic ON p.id = ic.id_patrimonio_principal
            LEFT JOIN tabela_sala s ON p.id_sala = s.id
            LEFT JOIN tabela_setor st ON s.id_setor = st.id
            LEFT JOIN tabela_responsavel r ON p.id_responsavel = r.id
            LEFT JOIN tabela_coleta_componente cc ON ic.id = cc.id_item_composto 
            """);
        
        // Adicionar filtro de inventário no JOIN
        if (filtro.getIdInventario() != null) {
            sql.append(" AND cc.id_inventario = ?");
        }
        
        sql.append(" WHERE 1=1");
        
        // Adicionar filtros dinâmicos
        List<Object> params = new ArrayList<>();
        
        if (filtro.getIdInventario() != null) {
            params.add(filtro.getIdInventario());
        }
        
        if (filtro.getIdSetor() != null) {
            sql.append(" AND st.id = ?");
            params.add(filtro.getIdSetor());
        }
        
        if (filtro.getIdSala() != null) {
            sql.append(" AND s.id = ?");
            params.add(filtro.getIdSala());
        }
        
        if (filtro.getIdResponsavel() != null) {
            sql.append(" AND r.id = ?");
            params.add(filtro.getIdResponsavel());
        }
        
        if (filtro.getTextoPesquisa() != null && !filtro.getTextoPesquisa().trim().isEmpty()) {
            sql.append(" AND (UPPER(p.numero) LIKE UPPER(?) OR UPPER(p.descricao) LIKE UPPER(?))");
            String pesquisa = "%" + filtro.getTextoPesquisa().trim() + "%";
            params.add(pesquisa);
            params.add(pesquisa);
        }
        
        sql.append(" GROUP BY p.id, p.numero, p.descricao, s.id, s.descricao, r.id, r.nome");
        
        // Filtro de status (HAVING)
        if (filtro.getStatusIntegridade() != null && filtro.getStatusIntegridade() != StatusIntegridade.TODOS) {
            switch (filtro.getStatusIntegridade()) {
                case COMPLETO:
                    sql.append(" HAVING COALESCE(SUM(cc.quantidade_encontrada), 0) >= SUM(ic.quantidade_esperada)");
                    break;
                case INCOMPLETO:
                    sql.append(" HAVING COALESCE(SUM(cc.quantidade_encontrada), 0) = 0");
                    break;
                case PARCIAL:
                    sql.append(" HAVING COALESCE(SUM(cc.quantidade_encontrada), 0) > 0 AND COALESCE(SUM(cc.quantidade_encontrada), 0) < SUM(ic.quantidade_esperada)");
                    break;
                default:
                    break;
            }
        }
        
        sql.append(" ORDER BY p.numero");
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            // Definir parâmetros
            int paramIndex = 1;
            for (Object param : params) {
                stmt.setObject(paramIndex++, param);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ItemCompostoResumo item = new ItemCompostoResumo();
                    item.setIdPatrimonio(rs.getInt("id_patrimonio"));
                    item.setNumeroPatrimonio(rs.getString("numero_patrimonio"));
                    item.setDescricaoPatrimonio(rs.getString("descricao_patrimonio"));
                    item.setIdSala(rs.getObject("id_sala") != null ? rs.getInt("id_sala") : null);
                    item.setNomeSala(rs.getString("nome_sala"));
                    item.setIdResponsavel(rs.getObject("id_responsavel") != null ? rs.getInt("id_responsavel") : null);
                    item.setNomeResponsavel(rs.getString("nome_responsavel"));
                    item.setTotalComponentes(rs.getInt("total_componentes"));
                    item.setComponentesEsperados(rs.getInt("componentes_esperados"));
                    item.setComponentesEncontrados(rs.getInt("componentes_encontrados"));
                    item.setComponentesFaltantes(rs.getInt("componentes_faltantes"));
                    
                    double taxa = rs.getDouble("taxa_integridade");
                    if (rs.wasNull()) {
                        taxa = 0.0;
                    }
                    item.setTaxaIntegridade(taxa);
                    item.setStatus(StatusIntegridade.fromTaxa(taxa));
                    
                    String tiposFaltantes = rs.getString("tipos_faltantes");
                    if (tiposFaltantes != null && !tiposFaltantes.isEmpty()) {
                        item.setTiposFaltantes(Arrays.asList(tiposFaltantes.split(", ")));
                    }
                    
                    resultado.add(item);
                }
            }
        }
        
        return resultado;
    }

    
    /**
     * Calcula as estatísticas de integridade com filtros aplicados
     * 
     * @param filtro critérios de filtro
     * @return EstatisticasIntegridade com os totais
     */
    public EstatisticasIntegridade calcularEstatisticas(FiltroRelatorioItemComposto filtro) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("""
            WITH resumo AS (
                SELECT 
                    p.id,
                    SUM(ic.quantidade_esperada) AS esperados,
                    COALESCE(SUM(cc.quantidade_encontrada), 0) AS encontrados
                FROM tabela_patrimonio p
                INNER JOIN tabela_item_composto ic ON p.id = ic.id_patrimonio_principal
                LEFT JOIN tabela_sala s ON p.id_sala = s.id
                LEFT JOIN tabela_setor st ON s.id_setor = st.id
                LEFT JOIN tabela_responsavel r ON p.id_responsavel = r.id
                LEFT JOIN tabela_coleta_componente cc ON ic.id = cc.id_item_composto
            """);
        
        if (filtro.getIdInventario() != null) {
            sql.append(" AND cc.id_inventario = ?");
        }
        
        sql.append(" WHERE 1=1");
        
        List<Object> params = new ArrayList<>();
        
        if (filtro.getIdInventario() != null) {
            params.add(filtro.getIdInventario());
        }
        
        if (filtro.getIdSetor() != null) {
            sql.append(" AND st.id = ?");
            params.add(filtro.getIdSetor());
        }
        
        if (filtro.getIdSala() != null) {
            sql.append(" AND s.id = ?");
            params.add(filtro.getIdSala());
        }
        
        if (filtro.getIdResponsavel() != null) {
            sql.append(" AND r.id = ?");
            params.add(filtro.getIdResponsavel());
        }
        
        if (filtro.getTextoPesquisa() != null && !filtro.getTextoPesquisa().trim().isEmpty()) {
            sql.append(" AND (UPPER(p.numero) LIKE UPPER(?) OR UPPER(p.descricao) LIKE UPPER(?))");
            String pesquisa = "%" + filtro.getTextoPesquisa().trim() + "%";
            params.add(pesquisa);
            params.add(pesquisa);
        }
        
        sql.append("""
                GROUP BY p.id
            )
            SELECT 
                COUNT(*) AS total_conjuntos,
                COUNT(CASE WHEN encontrados >= esperados THEN 1 END) AS conjuntos_completos,
                COUNT(CASE WHEN encontrados = 0 THEN 1 END) AS conjuntos_incompletos,
                COUNT(CASE WHEN encontrados > 0 AND encontrados < esperados THEN 1 END) AS conjuntos_parciais,
                ROUND(AVG(CASE WHEN esperados > 0 THEN (encontrados::DECIMAL / esperados) * 100 ELSE 100 END), 2) AS taxa_geral
            FROM resumo
            """);
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            for (Object param : params) {
                stmt.setObject(paramIndex++, param);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    EstatisticasIntegridade stats = new EstatisticasIntegridade();
                    stats.setTotalConjuntos(rs.getInt("total_conjuntos"));
                    stats.setConjuntosCompletos(rs.getInt("conjuntos_completos"));
                    stats.setConjuntosIncompletos(rs.getInt("conjuntos_incompletos"));
                    stats.setConjuntosParciais(rs.getInt("conjuntos_parciais"));
                    
                    double taxa = rs.getDouble("taxa_geral");
                    if (rs.wasNull()) {
                        taxa = 100.0;
                    }
                    stats.setTaxaIntegridadeGeral(taxa);
                    
                    return stats;
                }
            }
        }
        
        return new EstatisticasIntegridade(0, 0, 0, 100.0);
    }
    
    /**
     * Busca os detalhes dos componentes de um patrimônio específico
     * 
     * @param idPatrimonio ID do patrimônio
     * @param idInventario ID do inventário (pode ser null para buscar sem filtro de inventário)
     * @return lista de ComponenteDetalhe
     */
    public List<ComponenteDetalhe> buscarComponentesPorPatrimonio(Integer idPatrimonio, Integer idInventario) throws SQLException {
        List<ComponenteDetalhe> componentes = new ArrayList<>();
        
        // Verificar se a coluna localizacao_encontrada existe
        boolean colunaLocalizacaoExiste = verificarColunaExiste("tabela_coleta_componente", "localizacao_encontrada");
        
        StringBuilder sql = new StringBuilder();
        sql.append("""
            SELECT 
                ic.id,
                ic.tipo_componente,
                ic.descricao_componente,
                ic.quantidade_esperada,
                ic.obrigatorio,
                COALESCE(cc.quantidade_encontrada, 0) AS quantidade_encontrada,
                COALESCE(cc.status_componente, 'NAO_COLETADO') AS status_componente,
                COALESCE(cc.observacao_coleta, '') AS observacao,
            """);
        
        if (colunaLocalizacaoExiste) {
            sql.append("COALESCE(cc.localizacao_encontrada, '') AS localizacao_encontrada,");
        } else {
            sql.append("'' AS localizacao_encontrada,");
        }
        
        sql.append("""
                cc.data_coleta,
                u.nome_completo AS nome_coletor,
                cc.id_coletor
            FROM tabela_item_composto ic
            LEFT JOIN tabela_coleta_componente cc ON ic.id = cc.id_item_composto""");
        
        if (idInventario != null) {
            sql.append(" AND cc.id_inventario = ?");
        }
        
        sql.append("""
            
            LEFT JOIN tabela_usuario u ON cc.id_coletor = u.id
            WHERE ic.id_patrimonio_principal = ?
            ORDER BY ic.tipo_componente
            """);
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            if (idInventario != null) {
                stmt.setInt(paramIndex++, idInventario);
            }
            stmt.setInt(paramIndex, idPatrimonio);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ComponenteDetalhe comp = new ComponenteDetalhe();
                    comp.setId(rs.getInt("id"));
                    comp.setTipo(rs.getString("tipo_componente"));
                    comp.setDescricao(rs.getString("descricao_componente"));
                    comp.setQuantidadeEsperada(rs.getInt("quantidade_esperada"));
                    comp.setQuantidadeEncontrada(rs.getInt("quantidade_encontrada"));
                    comp.setObrigatorio(rs.getBoolean("obrigatorio"));
                    comp.setStatus(StatusComponente.fromString(rs.getString("status_componente")));
                    comp.setObservacao(rs.getString("observacao"));
                    comp.setLocalizacaoEncontrada(rs.getString("localizacao_encontrada"));
                    comp.setDataColeta(rs.getTimestamp("data_coleta"));
                    comp.setNomeColetor(rs.getString("nome_coletor"));
                    comp.setIdColetor(rs.getObject("id_coletor") != null ? rs.getInt("id_coletor") : null);
                    
                    componentes.add(comp);
                }
            }
        }
        
        return componentes;
    }

    
    /**
     * Lista inventários que possuem itens compostos
     */
    public List<Inventario> listarInventariosComItensCompostos() throws SQLException {
        List<Inventario> inventarios = new ArrayList<>();
        
        String sql = """
            SELECT DISTINCT i.id, i.nome, i.status_inventario, i.data_inicio, i.data_fim
            FROM tabela_inventario i
            WHERE EXISTS (
                SELECT 1 FROM tabela_item_composto ic
                INNER JOIN tabela_patrimonio p ON ic.id_patrimonio_principal = p.id
            )
            ORDER BY i.data_inicio DESC
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Inventario inv = new Inventario();
                inv.setId(rs.getInt("id"));
                inv.setNome(rs.getString("nome"));
                inv.setStatusInventario(rs.getString("status_inventario"));
                inv.setDataInicio(rs.getDate("data_inicio"));
                inv.setDataFim(rs.getDate("data_fim"));
                inventarios.add(inv);
            }
        }
        
        return inventarios;
    }
    
    /**
     * Lista setores que possuem itens compostos
     */
    public List<Setor> listarSetoresComItensCompostos() throws SQLException {
        List<Setor> setores = new ArrayList<>();
        
        String sql = """
            SELECT DISTINCT st.id, st.nome, st.descricao
            FROM tabela_setor st
            INNER JOIN tabela_sala s ON s.id_setor = st.id
            INNER JOIN tabela_patrimonio p ON p.id_sala = s.id
            INNER JOIN tabela_item_composto ic ON ic.id_patrimonio_principal = p.id
            ORDER BY st.nome
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Setor setor = new Setor();
                setor.setId(rs.getInt("id"));
                setor.setNome(rs.getString("nome"));
                setor.setDescricao(rs.getString("descricao"));
                setores.add(setor);
            }
        }
        
        return setores;
    }
    
    /**
     * Lista salas que possuem itens compostos, opcionalmente filtradas por setor
     * 
     * @param idSetor ID do setor (null para todas as salas)
     */
    public List<Sala> listarSalasComItensCompostos(Integer idSetor) throws SQLException {
        List<Sala> salas = new ArrayList<>();
        
        StringBuilder sql = new StringBuilder();
        sql.append("""
            SELECT DISTINCT s.id, s.numero_sala, s.descricao, s.id_setor
            FROM tabela_sala s
            INNER JOIN tabela_patrimonio p ON p.id_sala = s.id
            INNER JOIN tabela_item_composto ic ON ic.id_patrimonio_principal = p.id
            WHERE 1=1
            """);
        
        if (idSetor != null) {
            sql.append(" AND s.id_setor = ?");
        }
        
        sql.append(" ORDER BY s.numero_sala, s.descricao");
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            if (idSetor != null) {
                stmt.setInt(1, idSetor);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Sala sala = new Sala();
                    sala.setId(rs.getInt("id"));
                    sala.setNumero(rs.getString("numero_sala"));
                    sala.setDescricao(rs.getString("descricao"));
                    sala.setIdSetor(rs.getInt("id_setor"));
                    salas.add(sala);
                }
            }
        }
        
        return salas;
    }
    
    /**
     * Lista responsáveis que possuem itens compostos
     */
    public List<Responsavel> listarResponsaveisComItensCompostos() throws SQLException {
        List<Responsavel> responsaveis = new ArrayList<>();
        
        String sql = """
            SELECT DISTINCT r.id, r.nome, r.email
            FROM tabela_responsavel r
            INNER JOIN tabela_patrimonio p ON p.id_responsavel = r.id
            INNER JOIN tabela_item_composto ic ON ic.id_patrimonio_principal = p.id
            ORDER BY r.nome
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Responsavel resp = new Responsavel();
                resp.setId(rs.getInt("id"));
                resp.setNome(rs.getString("nome"));
                resp.setEmail(rs.getString("email"));
                responsaveis.add(resp);
            }
        }
        
        return responsaveis;
    }
    
    /**
     * Verifica se uma coluna existe em uma tabela
     */
    private boolean verificarColunaExiste(String tabela, String coluna) {
        String sql = """
            SELECT COUNT(*) FROM information_schema.columns 
            WHERE table_name = ? AND column_name = ?
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, tabela);
            stmt.setString(2, coluna);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao verificar coluna: " + e.getMessage());
        }
        
        return false;
    }
}
