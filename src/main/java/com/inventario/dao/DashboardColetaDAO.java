package com.inventario.dao;

import com.inventario.util.DatabaseConnection;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * DAO para buscar dados estatísticos em tempo real da coleta de inventário
 * Usado pelo dashboard gráfico para monitoramento
 */
@Repository
public class DashboardColetaDAO {
    
    /**
     * Busca estatísticas gerais da coleta do inventário ativo
     * @param idInventario ID do inventário ativo
     * @return Map com estatísticas da coleta
     */
    public Map<String, Integer> buscarEstatisticasColeta(int idInventario) {
        Map<String, Integer> estatisticas = new HashMap<>();
        
        // Debug: verificar se o campo correto está sendo usado
        System.out.println("DEBUG: Buscando estatísticas para inventário ID: " + idInventario);
        
        // Primeira consulta: estatísticas dos patrimônios cadastrados
        String sqlPatrimonios = "SELECT " +
                "COUNT(p.ID) as total_patrimonios, " +
                "COUNT(CASE WHEN c.STATUS_COLETA = 'COLETADO' THEN 1 END) as itens_coletados, " +
                "COUNT(CASE WHEN c.ID_PATRIMONIO IS NULL THEN 1 END) as itens_nao_encontrados, " +
                "COUNT(CASE WHEN c.STATUS_COLETA = 'NAO_ENCONTRADO' THEN 1 END) as itens_nao_coletados " +
                "FROM TABELA_PATRIMONIO p " +
                "LEFT JOIN TABELA_COLETA c ON p.ID = c.ID_PATRIMONIO AND c.ID_INVENTARIO = ? " +
                "WHERE p.status = 'Ativo'";
        
        // Segunda consulta: itens sem patrimônio encontrados (usando campo SEM_ETIQUETA)
        String sqlItensSemPatrimonio = "SELECT COUNT(*) as itens_sem_patrimonio " +
                "FROM TABELA_COLETA " +
                "WHERE ID_INVENTARIO = ? AND SEM_ETIQUETA = TRUE";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            
            // Executar primeira consulta - estatísticas dos patrimônios
            try (PreparedStatement stmt1 = conn.prepareStatement(sqlPatrimonios)) {
                stmt1.setInt(1, idInventario);
                
                System.out.println("DEBUG: Executando SQL Patrimônios: " + sqlPatrimonios.replace("?", String.valueOf(idInventario)));
                
                try (ResultSet rs1 = stmt1.executeQuery()) {
                    if (rs1.next()) {
                        int total = rs1.getInt("total_patrimonios");
                        int coletados = rs1.getInt("itens_coletados");
                        int naoEncontrados = rs1.getInt("itens_nao_encontrados");
                        int naoColetados = rs1.getInt("itens_nao_coletados");
                        
                        estatisticas.put("total_patrimonios", total);
                        estatisticas.put("itens_coletados", coletados);
                        estatisticas.put("itens_nao_encontrados", naoEncontrados);
                        estatisticas.put("itens_nao_coletados", naoColetados);
                        
                        System.out.println("DEBUG: Patrimônios - Total: " + total + ", Coletados: " + coletados + ", Não encontrados: " + naoEncontrados + ", Não coletados: " + naoColetados);
                    }
                }
            }
            
            // Executar segunda consulta - itens sem patrimônio
            try (PreparedStatement stmt2 = conn.prepareStatement(sqlItensSemPatrimonio)) {
                stmt2.setInt(1, idInventario);
                
                System.out.println("DEBUG: Executando SQL Itens Sem Patrimônio: " + sqlItensSemPatrimonio.replace("?", String.valueOf(idInventario)));
                
                try (ResultSet rs2 = stmt2.executeQuery()) {
                    if (rs2.next()) {
                        int itensSemPatrimonio = rs2.getInt("itens_sem_patrimonio");
                        estatisticas.put("itens_sem_patrimonio", itensSemPatrimonio);
                        
                        System.out.println("DEBUG: Itens sem patrimônio encontrados: " + itensSemPatrimonio);
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar estatísticas da coleta: " + e.getMessage());
            e.printStackTrace();
            // Retornar dados zerados em caso de erro
            estatisticas.put("total_patrimonios", 0);
            estatisticas.put("itens_coletados", 0);
            estatisticas.put("itens_nao_encontrados", 0);
            estatisticas.put("itens_sem_patrimonio", 0);
            estatisticas.put("itens_nao_coletados", 0);
        }
        
        return estatisticas;
    }
    
    /**
     * Busca estatísticas por responsável do inventário ativo
     * @param idInventario ID do inventário ativo
     * @return Map com estatísticas por responsável
     */
    public Map<String, Map<String, Integer>> buscarEstatisticasPorResponsavel(int idInventario) {
        Map<String, Map<String, Integer>> estatisticasPorResponsavel = new HashMap<>();
        
        String sql = """
            SELECT 
                r.NOME as responsavel,
                COUNT(p.ID) as total_patrimonios,
                COUNT(CASE WHEN c.STATUS_COLETA = 'COLETADO' THEN 1 END) as itens_coletados,
                COUNT(CASE WHEN c.STATUS_COLETA = 'NAO_ENCONTRADO' THEN 1 END) as itens_nao_encontrados
            FROM TABELA_PATRIMONIO p
            INNER JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID
            LEFT JOIN TABELA_COLETA c ON p.ID = c.ID_PATRIMONIO AND c.ID_INVENTARIO = ?
            WHERE p.status = 'Ativo'
            GROUP BY r.ID, r.NOME
            ORDER BY itens_coletados DESC
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String responsavel = rs.getString("responsavel");
                    Map<String, Integer> stats = new HashMap<>();
                    stats.put("total_patrimonios", rs.getInt("total_patrimonios"));
                    stats.put("itens_coletados", rs.getInt("itens_coletados"));
                    stats.put("itens_nao_encontrados", rs.getInt("itens_nao_encontrados"));
                    
                    estatisticasPorResponsavel.put(responsavel, stats);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar estatísticas por responsável: " + e.getMessage());
            // Retornar dados vazios em caso de erro
        }
        
        return estatisticasPorResponsavel;
    }
    
    /**
     * Busca progresso da coleta por setor do inventário ativo
     * @param idInventario ID do inventário ativo
     * @return Map com progresso por setor
     */
    public Map<String, Map<String, Integer>> buscarProgressoPorSetor(int idInventario) {
        Map<String, Map<String, Integer>> progressoPorSetor = new HashMap<>();
        
        String sql = """
            SELECT 
                s.NOME as setor,
                COUNT(p.ID) as total_patrimonios,
                COUNT(CASE WHEN c.STATUS_COLETA = 'COLETADO' THEN 1 END) as itens_coletados
            FROM TABELA_PATRIMONIO p
            INNER JOIN TABELA_SALA sa ON p.ID_SALA = sa.ID_SALA
            INNER JOIN TABELA_SETOR s ON sa.ID_SETOR = s.ID
            LEFT JOIN TABELA_COLETA c ON p.ID = c.ID_PATRIMONIO AND c.ID_INVENTARIO = ?
            WHERE p.status = 'Ativo'
            GROUP BY s.ID, s.NOME
            ORDER BY itens_coletados DESC
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String setor = rs.getString("setor");
                    Map<String, Integer> progress = new HashMap<>();
                    int total = rs.getInt("total_patrimonios");
                    int coletados = rs.getInt("itens_coletados");
                    
                    progress.put("total_patrimonios", total);
                    progress.put("itens_coletados", coletados);
                    progress.put("percentual", total > 0 ? (coletados * 100 / total) : 0);
                    
                    progressoPorSetor.put(setor, progress);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar progresso por setor: " + e.getMessage());
            // Retornar dados vazios em caso de erro
        }
        
        return progressoPorSetor;
    }
    
    /**
     * Busca estatísticas de coleta por coletor do inventário ativo
     * Considera apenas participantes com papel 'COLETOR' ativos da TABELA_PARTICIPANTE_INVENTARIO
     * @param idInventario ID do inventário ativo
     * @return Map com estatísticas por coletor
     */
    public Map<String, Integer> obterEstatisticasColetores(int idInventario) {
        Map<String, Integer> estatisticasColetores = new HashMap<>();
        
        String sql = """
            SELECT 
                u.NOME_COMPLETO as coletor,
                COALESCE(COUNT(c.ID), 0) as itens_coletados
            FROM TABELA_PARTICIPANTE_INVENTARIO p
            INNER JOIN TABELA_USUARIO u ON p.ID_USUARIO = u.ID
            LEFT JOIN TABELA_COLETA c ON c.ID_PARTICIPANTE_INVENTARIO = p.ID_PARTICIPANTE 
                AND c.ID_INVENTARIO = ? 
                AND c.STATUS_COLETA = 'COLETADO'
            WHERE p.ID_INVENTARIO = ?
                AND p.PAPEL = 'COLETOR'
                AND p.ATIVO = TRUE
            GROUP BY u.ID, u.NOME_COMPLETO
            ORDER BY itens_coletados DESC
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            stmt.setInt(2, idInventario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String coletor = rs.getString("coletor");
                    int itensColetados = rs.getInt("itens_coletados");
                    estatisticasColetores.put(coletor, itensColetados);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar estatísticas dos coletores: " + e.getMessage());
            // Retornar dados vazios em caso de erro
        }
        
        return estatisticasColetores;
    }
    
    /**
     * Busca desempenho detalhado dos coletores por período (últimos 7 dias)
     * Considera apenas participantes com papel 'COLETOR' ativos da TABELA_PARTICIPANTE_INVENTARIO
     * @param idInventario ID do inventário ativo
     * @return Map com desempenho por coletor e período
     */
    public Map<String, Map<String, Integer>> obterDesempenhoColetoresPorPeriodo(int idInventario) {
        Map<String, Map<String, Integer>> desempenhoDetalhado = new HashMap<>();
        
        String sql = """
            SELECT 
                u.NOME_COMPLETO as coletor,
                DATE(c.DATA_COLETA) as data_coleta,
                COUNT(c.ID) as itens_coletados
            FROM TABELA_PARTICIPANTE_INVENTARIO p
            INNER JOIN TABELA_USUARIO u ON p.ID_USUARIO = u.ID
            INNER JOIN TABELA_COLETA c ON c.ID_PARTICIPANTE_INVENTARIO = p.ID_PARTICIPANTE 
                AND c.ID_INVENTARIO = ?
            WHERE p.ID_INVENTARIO = ?
                AND p.PAPEL = 'COLETOR'
                AND p.ATIVO = TRUE
                AND c.STATUS_COLETA = 'COLETADO'
                AND c.DATA_COLETA >= CURRENT_DATE - INTERVAL '7 days'
            GROUP BY u.ID, u.NOME_COMPLETO, DATE(c.DATA_COLETA)
            ORDER BY u.NOME_COMPLETO, DATE(c.DATA_COLETA)
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            stmt.setInt(2, idInventario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String coletor = rs.getString("coletor");
                    String dataColeta = rs.getString("data_coleta");
                    int itensColetados = rs.getInt("itens_coletados");
                    
                    desempenhoDetalhado.computeIfAbsent(coletor, k -> new HashMap<>())
                                     .put(dataColeta, itensColetados);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar desempenho detalhado dos coletores: " + e.getMessage());
            // Retornar dados vazios em caso de erro
        }
        
        return desempenhoDetalhado;
    }

}