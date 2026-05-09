package com.inventario.sihcp.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.inventario.sihcp.util.DatabaseConnection;

/**
 * DAO para buscar dados estatísticos em tempo real da coleta de inventário
 * Usado pelo dashboard gráfico para monitoramento
 */
@Repository
public class DashboardColetaDAO {

    /**
     * Busca estatísticas gerais da coleta do inventário ativo
     * 
     * INTEGRAÇÃO 13/01/2026: Inclui coletas de itens compostos nas estatísticas.
     * Patrimônios que tiveram componentes coletados são contados como coletados.
     * 
     * @param idInventario ID do inventário ativo
     * @return Map com estatísticas da coleta
     */
    public Map<String, Integer> buscarEstatisticasColeta(int idInventario) {
        Map<String, Integer> estatisticas = new HashMap<>();

        // Debug: verificar se o campo correto está sendo usado
        System.out.println("DEBUG: Buscando estatísticas para inventário ID: " + idInventario);

        // Primeira consulta: estatísticas dos patrimônios cadastrados
        // ATUALIZADO: Inclui patrimônios com status 'Ativo' E 'Pendente' (total: 11.070)
        // Exclui patrimônios 'Baixado' que não devem ser inventariados
        // INTEGRAÇÃO 13/01/2026: Considera coletas de itens compostos
        String sqlPatrimonios = """
                SELECT 
                    COUNT(p.ID) as total_patrimonios,
                    COUNT(CASE 
                        WHEN c.STATUS_COLETA = 'COLETADO' THEN 1
                        WHEN EXISTS (
                            SELECT 1 FROM tabela_item_composto ic
                            INNER JOIN tabela_coleta_componente cc ON ic.id = cc.id_item_composto
                            WHERE ic.id_patrimonio_principal = p.ID AND cc.id_inventario = ?
                        ) THEN 1
                    END) as itens_coletados,
                    COUNT(CASE 
                        WHEN c.ID_PATRIMONIO IS NULL 
                        AND NOT EXISTS (
                            SELECT 1 FROM tabela_item_composto ic
                            INNER JOIN tabela_coleta_componente cc ON ic.id = cc.id_item_composto
                            WHERE ic.id_patrimonio_principal = p.ID AND cc.id_inventario = ?
                        ) THEN 1 
                    END) as itens_nao_encontrados,
                    COUNT(CASE WHEN c.STATUS_COLETA = 'NAO_ENCONTRADO' THEN 1 END) as itens_nao_coletados
                FROM TABELA_PATRIMONIO p
                LEFT JOIN TABELA_COLETA c ON p.ID = c.ID_PATRIMONIO AND c.ID_INVENTARIO = ?
                WHERE UPPER(p.status) IN ('ATIVO', 'PENDENTE')
                """;

        // Segunda consulta: itens sem patrimônio encontrados (usando campo
        // SEM_ETIQUETA)
        String sqlItensSemPatrimonio = "SELECT COUNT(*) as itens_sem_patrimonio " +
                "FROM TABELA_COLETA " +
                "WHERE ID_INVENTARIO = ? AND SEM_ETIQUETA = TRUE";

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Executar primeira consulta - estatísticas dos patrimônios
            try (PreparedStatement stmt1 = conn.prepareStatement(sqlPatrimonios)) {
                stmt1.setInt(1, idInventario); // Para EXISTS de itens compostos (coletados)
                stmt1.setInt(2, idInventario); // Para NOT EXISTS de itens compostos (não encontrados)
                stmt1.setInt(3, idInventario); // Para LEFT JOIN de coletas

                System.out.println("DEBUG: Executando SQL Patrimônios com integração de itens compostos");

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

                        System.out.println("DEBUG: Patrimônios - Total: " + total + ", Coletados: " + coletados
                                + ", Não encontrados: " + naoEncontrados + ", Não coletados: " + naoColetados);
                    }
                }
            }

            // Executar segunda consulta - itens sem patrimônio
            try (PreparedStatement stmt2 = conn.prepareStatement(sqlItensSemPatrimonio)) {
                stmt2.setInt(1, idInventario);

                System.out.println("DEBUG: Executando SQL Itens Sem Patrimônio: "
                        + sqlItensSemPatrimonio.replace("?", String.valueOf(idInventario)));

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
     * 
     * INTEGRAÇÃO 13/01/2026: Inclui coletas de itens compostos nas estatísticas.
     * 
     * @param idInventario ID do inventário ativo
     * @return Map com estatísticas por responsável
     */
    public Map<String, Map<String, Integer>> buscarEstatisticasPorResponsavel(int idInventario) {
        Map<String, Map<String, Integer>> estatisticasPorResponsavel = new HashMap<>();

        String sql = """
                SELECT
                    r.NOME as responsavel,
                    COUNT(p.ID) as total_patrimonios,
                    COUNT(CASE 
                        WHEN c.STATUS_COLETA = 'COLETADO' THEN 1
                        WHEN EXISTS (
                            SELECT 1 FROM tabela_item_composto ic
                            INNER JOIN tabela_coleta_componente cc ON ic.id = cc.id_item_composto
                            WHERE ic.id_patrimonio_principal = p.ID AND cc.id_inventario = ?
                        ) THEN 1
                    END) as itens_coletados,
                    COUNT(CASE WHEN c.STATUS_COLETA = 'NAO_ENCONTRADO' THEN 1 END) as itens_nao_encontrados
                FROM TABELA_PATRIMONIO p
                INNER JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID
                LEFT JOIN TABELA_COLETA c ON p.ID = c.ID_PATRIMONIO AND c.ID_INVENTARIO = ?
                WHERE UPPER(p.status) IN ('ATIVO', 'PENDENTE')
                GROUP BY r.ID, r.NOME
                ORDER BY itens_coletados DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idInventario); // Para EXISTS de itens compostos
            stmt.setInt(2, idInventario); // Para LEFT JOIN de coletas

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
     * 
     * INTEGRAÇÃO 13/01/2026: Inclui coletas de itens compostos nas estatísticas.
     * 
     * @param idInventario ID do inventário ativo
     * @return Map com progresso por setor
     */
    public Map<String, Map<String, Integer>> buscarProgressoPorSetor(int idInventario) {
        Map<String, Map<String, Integer>> progressoPorSetor = new HashMap<>();

        String sql = """
                SELECT
                    s.NOME as setor,
                    COUNT(p.ID) as total_patrimonios,
                    COUNT(CASE 
                        WHEN c.STATUS_COLETA = 'COLETADO' THEN 1
                        WHEN EXISTS (
                            SELECT 1 FROM tabela_item_composto ic
                            INNER JOIN tabela_coleta_componente cc ON ic.id = cc.id_item_composto
                            WHERE ic.id_patrimonio_principal = p.ID AND cc.id_inventario = ?
                        ) THEN 1
                    END) as itens_coletados
                FROM TABELA_PATRIMONIO p
                INNER JOIN TABELA_SALA sa ON p.ID_SALA = sa.ID_SALA
                INNER JOIN TABELA_SETOR s ON sa.ID_SETOR = s.ID
                LEFT JOIN TABELA_COLETA c ON p.ID = c.ID_PATRIMONIO AND c.ID_INVENTARIO = ?
                WHERE UPPER(p.status) IN ('ATIVO', 'PENDENTE')
                GROUP BY s.ID, s.NOME
                ORDER BY itens_coletados DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idInventario); // Para EXISTS de itens compostos
            stmt.setInt(2, idInventario); // Para LEFT JOIN de coletas

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
     * Considera apenas participantes com papel 'COLETOR' ativos da
     * TABELA_PARTICIPANTE_INVENTARIO
     * 
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
     * Considera apenas participantes com papel 'COLETOR' ativos da
     * TABELA_PARTICIPANTE_INVENTARIO
     * 
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

    /**
     * Busca estatísticas de coleta por sala do inventário ativo
     * Inclui o status real da sala (FINALIZADA, EM_ANDAMENTO, etc.) da tabela_sala_inventario
     * 
     * @param idInventario ID do inventário ativo
     * @return List com estatísticas por sala
     */
    public java.util.List<Map<String, Object>> buscarEstatisticasPorSala(int idInventario) {
        java.util.List<Map<String, Object>> estatisticasPorSala = new java.util.ArrayList<>();

        // Incluir status real da sala da tabela_sala_inventario
        // ATUALIZADO: Inclui patrimônios com status 'Ativo' E 'Pendente' (total: 11.070)
        String sql = "SELECT " +
                "s.ID_SALA, " +
                "s.NUMERO_SALA, " +
                "s.DESCRICAO, " +
                "st.NOME as setor, " +
                "COUNT(p.ID) as total_patrimonios, " +
                "COUNT(CASE WHEN c.STATUS_COLETA = 'COLETADO' THEN 1 END) as itens_coletados, " +
                "si.status_coleta as status_sala_inventario, " +
                "si.coleta_finalizada, " +
                "si.data_finalizacao_coleta, " +
                "si.percentual_conclusao as percentual_sala_inventario " +
                "FROM TABELA_SALA s " +
                "LEFT JOIN TABELA_SETOR st ON s.ID_SETOR = st.ID " +
                "LEFT JOIN TABELA_PATRIMONIO p ON p.ID_SALA = s.ID_SALA AND UPPER(p.status) IN ('ATIVO', 'PENDENTE') " +
                "LEFT JOIN TABELA_COLETA c ON p.ID = c.ID_PATRIMONIO AND c.ID_INVENTARIO = ? " +
                "LEFT JOIN TABELA_SALA_INVENTARIO si ON s.ID_SALA = si.ID_SALA AND si.ID_INVENTARIO = ? " +
                "WHERE s.ATIVO = TRUE " +
                "GROUP BY s.ID_SALA, s.NUMERO_SALA, s.DESCRICAO, st.NOME, " +
                "si.status_coleta, si.coleta_finalizada, si.data_finalizacao_coleta, si.percentual_conclusao " +
                "ORDER BY st.NOME, s.NUMERO_SALA";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idInventario);
            stmt.setInt(2, idInventario);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> salaStats = new HashMap<>();

                    int idSala = rs.getInt("ID_SALA");
                    String numero = rs.getString("NUMERO_SALA");
                    String descricao = rs.getString("DESCRICAO");
                    String nomeSala = (numero != null ? numero : "") + " - " + (descricao != null ? descricao : "");
                    if (nomeSala.startsWith(" - "))
                        nomeSala = nomeSala.substring(3);

                    salaStats.put("id_sala", idSala);
                    salaStats.put("sala", nomeSala);
                    salaStats.put("setor", rs.getString("setor") != null ? rs.getString("setor") : "Sem Setor");

                    int total = rs.getInt("total_patrimonios");
                    int coletados = rs.getInt("itens_coletados");

                    salaStats.put("total_patrimonios", total);
                    salaStats.put("itens_coletados", coletados);

                    double percentual = total > 0 ? (double) coletados / total * 100.0 : 0.0;
                    salaStats.put("percentual", percentual);

                    // ✅ NOVO: Usar status real da tabela_sala_inventario se disponível
                    String statusSalaInventario = rs.getString("status_sala_inventario");
                    boolean coletaFinalizada = rs.getBoolean("coleta_finalizada");
                    java.sql.Timestamp dataFinalizacao = rs.getTimestamp("data_finalizacao_coleta");
                    
                    String status;
                    if (statusSalaInventario != null) {
                        // Usar status real da tabela_sala_inventario
                        switch (statusSalaInventario) {
                            case "FINALIZADA":
                                status = "Finalizada";
                                break;
                            case "EM_ANDAMENTO":
                                status = "Em Andamento";
                                break;
                            case "NAO_INICIADA":
                                status = "Não Iniciado";
                                break;
                            case "PENDENTE":
                                status = "Pendente";
                                break;
                            default:
                                status = statusSalaInventario;
                        }
                        
                        // Se está marcada como finalizada, mostrar como Finalizada
                        if (coletaFinalizada) {
                            status = "Finalizada";
                        }
                    } else {
                        // Fallback: calcular status baseado nos itens coletados
                        if (coletados == 0 && total > 0) {
                            status = "Não Iniciado";
                        } else if (coletados == 0 && total == 0) {
                            status = "Vazia";
                        } else if (coletados < total) {
                            status = "Em Andamento";
                        } else {
                            status = "Concluído";
                        }
                    }
                    salaStats.put("status", status);
                    
                    // Adicionar informações extras
                    salaStats.put("coleta_finalizada", coletaFinalizada);
                    salaStats.put("data_finalizacao", dataFinalizacao);

                    estatisticasPorSala.add(salaStats);
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar estatísticas por sala: " + e.getMessage());
            e.printStackTrace();
        }

        return estatisticasPorSala;
    }

    /**
     * Finaliza a coleta de uma sala no inventário
     * 
     * @param idSala ID da sala
     * @param idInventario ID do inventário
     * @param observacoes Observações da finalização
     * @return true se finalizou com sucesso
     */
    public boolean finalizarColetaSala(int idSala, int idInventario, String observacoes) {
        // Primeiro verificar se já existe registro na tabela_sala_inventario
        String sqlVerifica = "SELECT id_sala_inventario FROM tabela_sala_inventario WHERE id_sala = ? AND id_inventario = ?";
        String sqlInsert = "INSERT INTO tabela_sala_inventario (id_sala, id_inventario, coleta_finalizada, data_finalizacao_coleta, observacoes_finalizacao, status_coleta, data_atualizacao) VALUES (?, ?, TRUE, CURRENT_TIMESTAMP, ?, 'FINALIZADA', CURRENT_TIMESTAMP)";
        String sqlUpdate = "UPDATE tabela_sala_inventario SET coleta_finalizada = TRUE, data_finalizacao_coleta = CURRENT_TIMESTAMP, observacoes_finalizacao = ?, status_coleta = 'FINALIZADA', data_atualizacao = CURRENT_TIMESTAMP WHERE id_sala = ? AND id_inventario = ?";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Verificar se existe
            boolean existe = false;
            try (PreparedStatement stmtVerifica = conn.prepareStatement(sqlVerifica)) {
                stmtVerifica.setInt(1, idSala);
                stmtVerifica.setInt(2, idInventario);
                try (ResultSet rs = stmtVerifica.executeQuery()) {
                    existe = rs.next();
                }
            }
            
            if (existe) {
                // Atualizar registro existente
                try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
                    stmt.setString(1, observacoes);
                    stmt.setInt(2, idSala);
                    stmt.setInt(3, idInventario);
                    int rows = stmt.executeUpdate();
                    return rows > 0;
                }
            } else {
                // Inserir novo registro
                try (PreparedStatement stmt = conn.prepareStatement(sqlInsert)) {
                    stmt.setInt(1, idSala);
                    stmt.setInt(2, idInventario);
                    stmt.setString(3, observacoes);
                    int rows = stmt.executeUpdate();
                    return rows > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao finalizar coleta da sala: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Reabre a coleta de uma sala no inventário
     * 
     * @param idSala ID da sala
     * @param idInventario ID do inventário
     * @param motivo Motivo da reabertura
     * @return true se reabriu com sucesso
     */
    public boolean reabrirColetaSala(int idSala, int idInventario, String motivo) {
        String sql = "UPDATE tabela_sala_inventario SET coleta_finalizada = FALSE, data_finalizacao_coleta = NULL, status_coleta = 'EM_ANDAMENTO', observacoes_finalizacao = CONCAT(COALESCE(observacoes_finalizacao, ''), ' | Reaberta: ', ?), data_atualizacao = CURRENT_TIMESTAMP WHERE id_sala = ? AND id_inventario = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, motivo);
            stmt.setInt(2, idSala);
            stmt.setInt(3, idInventario);
            
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao reabrir coleta da sala: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Busca o ID da sala pelo nome (número + descrição)
     * 
     * @param nomeSala Nome da sala no formato "NUMERO - DESCRICAO"
     * @return ID da sala ou -1 se não encontrada
     */
    public int buscarIdSalaPorNome(String nomeSala) {
        String sql = "SELECT id_sala FROM tabela_sala WHERE CONCAT(COALESCE(numero_sala, ''), ' - ', COALESCE(descricao, '')) = ? OR descricao = ? OR numero_sala = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nomeSala);
            stmt.setString(2, nomeSala);
            stmt.setString(3, nomeSala);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_sala");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar ID da sala: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }
}
