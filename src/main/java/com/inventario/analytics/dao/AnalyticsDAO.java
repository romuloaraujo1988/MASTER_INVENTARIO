package com.inventario.analytics.dao;

import com.inventario.util.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * DAO para consultas analíticas otimizadas.
 * 
 * Fornece queries SQL para análise de divergências e métricas de tempo de coleta.
 */
public class AnalyticsDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsDAO.class);
    
    /**
     * Busca divergências com detalhes completos (patrimônio, setor, responsável, coletor).
     * 
     * @param idInventario ID do inventário
     * @return Lista de mapas com dados das divergências
     */
    public List<Map<String, Object>> buscarDivergenciasComDetalhes(int idInventario) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        
        String sql = """
            SELECT 
                c.id as id_coleta,
                c.data_coleta,
                c.localizacao_encontrada,
                c.estado_encontrado,
                c.divergencia,
                c.motivo_divergencia,
                p.numero_patrimonio,
                p.descricao as descricao_patrimonio,
                p.estado_conservacao as estado_cadastrado,
                s.id as id_sala,
                s.nome as nome_sala,
                st.id as id_setor,
                st.nome as nome_setor,
                r.id as id_responsavel,
                r.nome as nome_responsavel,
                u.id as id_coletor,
                u.nome as nome_coletor
            FROM tabela_coleta c
            INNER JOIN tabela_patrimonio p ON c.id_patrimonio = p.id
            LEFT JOIN tabela_sala s ON p.id_sala = s.id
            LEFT JOIN tabela_setor st ON s.id_setor = st.id
            LEFT JOIN tabela_responsavel r ON p.id_responsavel = r.id
            LEFT JOIN tabela_participante_inventario pi ON c.id_participante_inventario = pi.id
            LEFT JOIN tabela_usuario u ON pi.id_usuario = u.id
            WHERE c.id_inventario = ?
            AND (c.divergencia = true 
                 OR c.localizacao_encontrada != s.nome 
                 OR c.estado_encontrado != p.estado_conservacao)
            ORDER BY c.data_coleta DESC
            """;
        
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id_coleta", rs.getInt("id_coleta"));
                    row.put("data_coleta", rs.getTimestamp("data_coleta"));
                    row.put("localizacao_encontrada", rs.getString("localizacao_encontrada"));
                    row.put("estado_encontrado", rs.getString("estado_encontrado"));
                    row.put("divergencia", rs.getBoolean("divergencia"));
                    row.put("motivo_divergencia", rs.getString("motivo_divergencia"));
                    row.put("numero_patrimonio", rs.getString("numero_patrimonio"));
                    row.put("descricao_patrimonio", rs.getString("descricao_patrimonio"));
                    row.put("estado_cadastrado", rs.getString("estado_cadastrado"));
                    row.put("id_sala", rs.getObject("id_sala"));
                    row.put("nome_sala", rs.getString("nome_sala"));
                    row.put("id_setor", rs.getObject("id_setor"));
                    row.put("nome_setor", rs.getString("nome_setor"));
                    row.put("id_responsavel", rs.getObject("id_responsavel"));
                    row.put("nome_responsavel", rs.getString("nome_responsavel"));
                    row.put("id_coletor", rs.getObject("id_coletor"));
                    row.put("nome_coletor", rs.getString("nome_coletor"));
                    resultado.add(row);
                }
            }
            
            logger.debug("Encontradas {} divergências para inventário {}", resultado.size(), idInventario);
            
        } catch (SQLException e) {
            logger.error("Erro ao buscar divergências: {}", e.getMessage(), e);
        }
        
        return resultado;
    }
    
    /**
     * Busca métricas de tempo agrupadas por coletor, período ou dia da semana.
     * 
     * @param idInventario ID do inventário
     * @param agrupamento Tipo de agrupamento: "COLETOR", "PERIODO", "DIA_SEMANA"
     * @return Lista de mapas com métricas agrupadas
     */
    public List<Map<String, Object>> buscarMetricasTempoAgrupadas(int idInventario, String agrupamento) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        
        String groupByClause;
        String selectClause;
        
        switch (agrupamento.toUpperCase()) {
            case "COLETOR":
                selectClause = "u.id as id_grupo, u.nome as nome_grupo";
                groupByClause = "u.id, u.nome";
                break;
            case "PERIODO":
                selectClause = """
                    CASE 
                        WHEN EXTRACT(HOUR FROM c.data_coleta) >= 6 AND EXTRACT(HOUR FROM c.data_coleta) < 12 THEN 'MANHA'
                        WHEN EXTRACT(HOUR FROM c.data_coleta) >= 12 AND EXTRACT(HOUR FROM c.data_coleta) < 18 THEN 'TARDE'
                        ELSE 'NOITE'
                    END as id_grupo,
                    CASE 
                        WHEN EXTRACT(HOUR FROM c.data_coleta) >= 6 AND EXTRACT(HOUR FROM c.data_coleta) < 12 THEN 'Manhã'
                        WHEN EXTRACT(HOUR FROM c.data_coleta) >= 12 AND EXTRACT(HOUR FROM c.data_coleta) < 18 THEN 'Tarde'
                        ELSE 'Noite'
                    END as nome_grupo
                    """;
                groupByClause = """
                    CASE 
                        WHEN EXTRACT(HOUR FROM c.data_coleta) >= 6 AND EXTRACT(HOUR FROM c.data_coleta) < 12 THEN 'MANHA'
                        WHEN EXTRACT(HOUR FROM c.data_coleta) >= 12 AND EXTRACT(HOUR FROM c.data_coleta) < 18 THEN 'TARDE'
                        ELSE 'NOITE'
                    END,
                    CASE 
                        WHEN EXTRACT(HOUR FROM c.data_coleta) >= 6 AND EXTRACT(HOUR FROM c.data_coleta) < 12 THEN 'Manhã'
                        WHEN EXTRACT(HOUR FROM c.data_coleta) >= 12 AND EXTRACT(HOUR FROM c.data_coleta) < 18 THEN 'Tarde'
                        ELSE 'Noite'
                    END
                    """;
                break;
            case "DIA_SEMANA":
                selectClause = """
                    EXTRACT(DOW FROM c.data_coleta) as id_grupo,
                    CASE EXTRACT(DOW FROM c.data_coleta)
                        WHEN 0 THEN 'Domingo'
                        WHEN 1 THEN 'Segunda'
                        WHEN 2 THEN 'Terça'
                        WHEN 3 THEN 'Quarta'
                        WHEN 4 THEN 'Quinta'
                        WHEN 5 THEN 'Sexta'
                        WHEN 6 THEN 'Sábado'
                    END as nome_grupo
                    """;
                groupByClause = "EXTRACT(DOW FROM c.data_coleta)";
                break;
            default:
                logger.warn("Agrupamento inválido: {}", agrupamento);
                return resultado;
        }
        
        String sql = String.format("""
            SELECT 
                %s,
                COUNT(*) as total_coletas,
                COUNT(c.tempo_coleta_segundos) FILTER (WHERE c.tempo_coleta_segundos > 0 AND c.tempo_coleta_segundos < 600) as coletas_com_tempo,
                AVG(c.tempo_coleta_segundos) FILTER (WHERE c.tempo_coleta_segundos > 0 AND c.tempo_coleta_segundos < 600) as tempo_medio,
                MIN(c.tempo_coleta_segundos) FILTER (WHERE c.tempo_coleta_segundos > 0 AND c.tempo_coleta_segundos < 600) as tempo_minimo,
                MAX(c.tempo_coleta_segundos) FILTER (WHERE c.tempo_coleta_segundos > 0 AND c.tempo_coleta_segundos < 600) as tempo_maximo,
                STDDEV(c.tempo_coleta_segundos) FILTER (WHERE c.tempo_coleta_segundos > 0 AND c.tempo_coleta_segundos < 600) as desvio_padrao
            FROM tabela_coleta c
            LEFT JOIN tabela_participante_inventario pi ON c.id_participante_inventario = pi.id
            LEFT JOIN tabela_usuario u ON pi.id_usuario = u.id
            WHERE c.id_inventario = ?
            GROUP BY %s
            ORDER BY tempo_medio ASC NULLS LAST
            """, selectClause, groupByClause);
        
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id_grupo", rs.getObject("id_grupo"));
                    row.put("nome_grupo", rs.getString("nome_grupo"));
                    row.put("total_coletas", rs.getInt("total_coletas"));
                    row.put("coletas_com_tempo", rs.getInt("coletas_com_tempo"));
                    row.put("tempo_medio", rs.getObject("tempo_medio"));
                    row.put("tempo_minimo", rs.getObject("tempo_minimo"));
                    row.put("tempo_maximo", rs.getObject("tempo_maximo"));
                    row.put("desvio_padrao", rs.getObject("desvio_padrao"));
                    resultado.add(row);
                }
            }
            
            logger.debug("Encontradas {} métricas agrupadas por {} para inventário {}", 
                    resultado.size(), agrupamento, idInventario);
            
        } catch (SQLException e) {
            logger.error("Erro ao buscar métricas de tempo: {}", e.getMessage(), e);
        }
        
        return resultado;
    }
    
    /**
     * Busca ranking de setores por taxa de divergência.
     * 
     * @param idInventario ID do inventário
     * @return Lista de mapas com ranking de setores
     */
    public List<Map<String, Object>> buscarRankingSetores(int idInventario) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        
        String sql = """
            SELECT 
                st.id as id_setor,
                st.nome as nome_setor,
                COUNT(c.id) as total_coletas,
                COUNT(c.id) FILTER (WHERE c.divergencia = true 
                    OR c.localizacao_encontrada != s.nome 
                    OR c.estado_encontrado != p.estado_conservacao) as total_divergencias
            FROM tabela_coleta c
            INNER JOIN tabela_patrimonio p ON c.id_patrimonio = p.id
            LEFT JOIN tabela_sala s ON p.id_sala = s.id
            LEFT JOIN tabela_setor st ON s.id_setor = st.id
            WHERE c.id_inventario = ?
            AND st.id IS NOT NULL
            GROUP BY st.id, st.nome
            HAVING COUNT(c.id) > 0
            ORDER BY (COUNT(c.id) FILTER (WHERE c.divergencia = true 
                OR c.localizacao_encontrada != s.nome 
                OR c.estado_encontrado != p.estado_conservacao) * 100.0 / COUNT(c.id)) DESC
            """;
        
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                int posicao = 1;
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    int totalColetas = rs.getInt("total_coletas");
                    int totalDivergencias = rs.getInt("total_divergencias");
                    double taxa = totalColetas > 0 ? (totalDivergencias * 100.0 / totalColetas) : 0;
                    
                    row.put("id_setor", rs.getInt("id_setor"));
                    row.put("nome_setor", rs.getString("nome_setor"));
                    row.put("total_coletas", totalColetas);
                    row.put("total_divergencias", totalDivergencias);
                    row.put("taxa_divergencia", taxa);
                    row.put("posicao_ranking", posicao++);
                    resultado.add(row);
                }
            }
            
            logger.debug("Encontrados {} setores no ranking para inventário {}", resultado.size(), idInventario);
            
        } catch (SQLException e) {
            logger.error("Erro ao buscar ranking de setores: {}", e.getMessage(), e);
        }
        
        return resultado;
    }
    
    /**
     * Busca ranking de coletores por tempo médio de coleta.
     * 
     * @param idInventario ID do inventário
     * @return Lista de mapas com ranking de coletores
     */
    public List<Map<String, Object>> buscarRankingColetores(int idInventario) {
        return buscarMetricasTempoAgrupadas(idInventario, "COLETOR");
    }
    
    /**
     * Busca KPIs gerais do inventário.
     * 
     * @param idInventario ID do inventário
     * @return Mapa com KPIs
     */
    public Map<String, Object> buscarKPIsGerais(int idInventario) {
        Map<String, Object> kpis = new HashMap<>();
        
        String sql = """
            SELECT 
                COUNT(c.id) as total_coletas,
                COUNT(c.id) FILTER (WHERE c.divergencia = true) as total_divergencias,
                AVG(c.tempo_coleta_segundos) FILTER (WHERE c.tempo_coleta_segundos > 0 AND c.tempo_coleta_segundos < 600) as tempo_medio,
                COUNT(DISTINCT pi.id_usuario) as coletores_ativos,
                MIN(c.data_coleta) as primeira_coleta,
                MAX(c.data_coleta) as ultima_coleta
            FROM tabela_coleta c
            LEFT JOIN tabela_participante_inventario pi ON c.id_participante_inventario = pi.id
            WHERE c.id_inventario = ?
            """;
        
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int totalColetas = rs.getInt("total_coletas");
                    int totalDivergencias = rs.getInt("total_divergencias");
                    
                    kpis.put("total_coletas", totalColetas);
                    kpis.put("total_divergencias", totalDivergencias);
                    kpis.put("taxa_divergencia", totalColetas > 0 ? (totalDivergencias * 100.0 / totalColetas) : 0);
                    kpis.put("tempo_medio", rs.getObject("tempo_medio"));
                    kpis.put("coletores_ativos", rs.getInt("coletores_ativos"));
                    kpis.put("primeira_coleta", rs.getTimestamp("primeira_coleta"));
                    kpis.put("ultima_coleta", rs.getTimestamp("ultima_coleta"));
                    
                    // Calcular coletas por hora
                    Timestamp primeira = rs.getTimestamp("primeira_coleta");
                    Timestamp ultima = rs.getTimestamp("ultima_coleta");
                    if (primeira != null && ultima != null && totalColetas > 0) {
                        long horasTrabalhadas = (ultima.getTime() - primeira.getTime()) / (1000 * 60 * 60);
                        kpis.put("coletas_por_hora", horasTrabalhadas > 0 ? (double) totalColetas / horasTrabalhadas : totalColetas);
                    } else {
                        kpis.put("coletas_por_hora", 0.0);
                    }
                }
            }
            
            logger.debug("KPIs calculados para inventário {}: {}", idInventario, kpis);
            
        } catch (SQLException e) {
            logger.error("Erro ao buscar KPIs: {}", e.getMessage(), e);
        }
        
        return kpis;
    }
    
    /**
     * Busca divergências agrupadas por dia para análise temporal.
     * 
     * @param idInventario ID do inventário
     * @param inicio Data inicial (opcional)
     * @param fim Data final (opcional)
     * @return Lista de mapas com divergências por dia
     */
    public List<Map<String, Object>> buscarDivergenciasPorPeriodo(int idInventario, LocalDate inicio, LocalDate fim) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        
        StringBuilder sql = new StringBuilder("""
            SELECT 
                DATE(c.data_coleta) as data,
                COUNT(c.id) FILTER (WHERE c.localizacao_encontrada != s.nome) as divergencias_localizacao,
                COUNT(c.id) FILTER (WHERE c.estado_encontrado != p.estado_conservacao) as divergencias_estado,
                COUNT(c.id) FILTER (WHERE c.divergencia = true AND c.motivo_divergencia IS NOT NULL) as divergencias_manual,
                COUNT(c.id) FILTER (WHERE c.divergencia = true) as total_divergencias
            FROM tabela_coleta c
            INNER JOIN tabela_patrimonio p ON c.id_patrimonio = p.id
            LEFT JOIN tabela_sala s ON p.id_sala = s.id
            WHERE c.id_inventario = ?
            """);
        
        if (inicio != null) {
            sql.append(" AND DATE(c.data_coleta) >= ?");
        }
        if (fim != null) {
            sql.append(" AND DATE(c.data_coleta) <= ?");
        }
        
        sql.append(" GROUP BY DATE(c.data_coleta) ORDER BY DATE(c.data_coleta)");
        
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            stmt.setInt(paramIndex++, idInventario);
            
            if (inicio != null) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(inicio));
            }
            if (fim != null) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(fim));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("data", rs.getDate("data").toLocalDate());
                    row.put("divergencias_localizacao", rs.getInt("divergencias_localizacao"));
                    row.put("divergencias_estado", rs.getInt("divergencias_estado"));
                    row.put("divergencias_manual", rs.getInt("divergencias_manual"));
                    row.put("total_divergencias", rs.getInt("total_divergencias"));
                    resultado.add(row);
                }
            }
            
            logger.debug("Encontrados {} dias com divergências para inventário {}", resultado.size(), idInventario);
            
        } catch (SQLException e) {
            logger.error("Erro ao buscar divergências por período: {}", e.getMessage(), e);
        }
        
        return resultado;
    }
    
    /**
     * Busca ranking de responsáveis por quantidade de divergências.
     * 
     * @param idInventario ID do inventário
     * @return Lista de mapas com ranking de responsáveis
     */
    public List<Map<String, Object>> buscarRankingResponsaveis(int idInventario) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        
        String sql = """
            SELECT 
                r.id as id_responsavel,
                r.nome as nome_responsavel,
                COUNT(DISTINCT p.id) as total_patrimonios,
                COUNT(c.id) FILTER (WHERE c.divergencia = true 
                    OR c.localizacao_encontrada != s.nome 
                    OR c.estado_encontrado != p.estado_conservacao) as total_divergencias
            FROM tabela_coleta c
            INNER JOIN tabela_patrimonio p ON c.id_patrimonio = p.id
            LEFT JOIN tabela_sala s ON p.id_sala = s.id
            LEFT JOIN tabela_responsavel r ON p.id_responsavel = r.id
            WHERE c.id_inventario = ?
            AND r.id IS NOT NULL
            GROUP BY r.id, r.nome
            HAVING COUNT(c.id) FILTER (WHERE c.divergencia = true 
                OR c.localizacao_encontrada != s.nome 
                OR c.estado_encontrado != p.estado_conservacao) > 0
            ORDER BY COUNT(c.id) FILTER (WHERE c.divergencia = true 
                OR c.localizacao_encontrada != s.nome 
                OR c.estado_encontrado != p.estado_conservacao) DESC
            """;
        
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                int posicao = 1;
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id_responsavel", rs.getInt("id_responsavel"));
                    row.put("nome_responsavel", rs.getString("nome_responsavel"));
                    row.put("total_patrimonios", rs.getInt("total_patrimonios"));
                    row.put("total_divergencias", rs.getInt("total_divergencias"));
                    row.put("posicao_ranking", posicao++);
                    resultado.add(row);
                }
            }
            
            logger.debug("Encontrados {} responsáveis no ranking para inventário {}", resultado.size(), idInventario);
            
        } catch (SQLException e) {
            logger.error("Erro ao buscar ranking de responsáveis: {}", e.getMessage(), e);
        }
        
        return resultado;
    }
}
