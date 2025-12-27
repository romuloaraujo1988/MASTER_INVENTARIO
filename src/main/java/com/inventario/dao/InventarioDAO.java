package com.inventario.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.inventario.event.DashboardEvent;
import com.inventario.event.DashboardEventBus;
import com.inventario.event.DashboardEventType;
import com.inventario.model.Inventario;

/**
 * DAO Refatorado para gerenciar operações CRUD da tabela INVENTARIO
 * 
 * Herda funcionalidades comuns do BaseDAO:
 * - Gerenciamento de conexões via ConnectionManager
 * - Métodos CRUD padronizados (insert, update, delete, findById, findAll)
 * - Tratamento de erros consistente
 * - Logs estruturados
 * 
 * Redução: ~450 linhas → ~180 linhas (-60%)
 * 
 * @author Sistema de Inventário IFMT
 * @version 2.0 - Refatorado
 */
@Repository
public class InventarioDAO extends BaseDAO<Inventario, Integer> {

    /**
     * Retorna o nome correto da tabela de inventário
     * NOTA: Sempre usa TABELA_INVENTARIO (PostgreSQL)
     */
    private String getInventarioTableName() throws SQLException {
        // CORREÇÃO: Sempre usar tabela PostgreSQL
        return "TABELA_INVENTARIO";
    }

    // ==================== MÉTODOS ABSTRATOS IMPLEMENTADOS ====================

    @Override
    protected String getTableName() {
        try {
            return getInventarioTableName();
        } catch (SQLException e) {
            return "TABELA_INVENTARIO"; // Fallback
        }
    }

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO TABELA_INVENTARIO (NOME, DATA_INICIO, DATA_FIM, STATUS_INVENTARIO, " +
                "RESPONSAVEL_INVENTARIO, PERCENTUAL_CONCLUSAO) VALUES (?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE TABELA_INVENTARIO SET NOME = ?, DATA_INICIO = ?, DATA_FIM = ?, " +
                "STATUS_INVENTARIO = ?, RESPONSAVEL_INVENTARIO = ?, PERCENTUAL_CONCLUSAO = ? WHERE ID = ?";
    }

    @Override
    protected void setInsertParameters(PreparedStatement stmt, Inventario inventario) throws SQLException {
        stmt.setString(1, inventario.getNome());
        stmt.setDate(2, new java.sql.Date(inventario.getDataInicio().getTime()));
        stmt.setDate(3, new java.sql.Date(inventario.getDataFim().getTime()));
        stmt.setString(4, inventario.getStatusInventario());
        stmt.setString(5, inventario.getResponsavelInventario());
        stmt.setBigDecimal(6, inventario.getPercentualConclusao());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement stmt, Inventario inventario) throws SQLException {
        setInsertParameters(stmt, inventario);
        stmt.setInt(7, inventario.getId());
    }

    @Override
    protected Inventario mapResultSetToEntity(ResultSet rs) throws SQLException {
        Inventario inventario = new Inventario();

        inventario.setId(rs.getInt("ID"));
        inventario.setNome(rs.getString("NOME"));

        // Ler DATA_INICIO com tratamento robusto
        try {
            try {
                Timestamp tsInicio = rs.getTimestamp("DATA_INICIO");
                if (tsInicio != null) {
                    inventario.setDataInicio(new java.sql.Date(tsInicio.getTime()));
                }
            } catch (SQLException e1) {
                String dataStr = rs.getString("DATA_INICIO");
                if (dataStr != null && !dataStr.isEmpty()) {
                    try {
                        java.sql.Date data = parseDataFromString(dataStr);
                        inventario.setDataInicio(data);
                    } catch (Exception e2) {
                        inventario.setDataInicio(new java.sql.Date(System.currentTimeMillis()));
                    }
                }
            }
        } catch (Exception e) {
            // Ignorar erros
        }

        // Ler DATA_FIM com tratamento robusto
        try {
            try {
                Timestamp tsFim = rs.getTimestamp("DATA_FIM");
                if (tsFim != null) {
                    inventario.setDataFim(new java.sql.Date(tsFim.getTime()));
                }
            } catch (SQLException e1) {
                String dataStr = rs.getString("DATA_FIM");
                if (dataStr != null && !dataStr.isEmpty()) {
                    try {
                        java.sql.Date data = parseDataFromString(dataStr);
                        inventario.setDataFim(data);
                    } catch (Exception e2) {
                        inventario.setDataFim(new java.sql.Date(System.currentTimeMillis()));
                    }
                }
            }
        } catch (Exception e) {
            // Ignorar erros
        }

        inventario.setStatusInventario(rs.getString("STATUS_INVENTARIO"));
        inventario.setResponsavelInventario(rs.getString("RESPONSAVEL_INVENTARIO"));
        inventario.setPercentualConclusao(rs.getBigDecimal("PERCENTUAL_CONCLUSAO"));

        // Campos opcionais
        try {
            Timestamp dataCriacao = rs.getTimestamp("DATA_CRIACAO");
            if (dataCriacao != null) {
                inventario.setDataCriacao(dataCriacao.toLocalDateTime());
            }
        } catch (SQLException e) {
            // Campo pode não existir
        }

        return inventario;
    }

    @Override
    protected void setGeneratedId(Inventario inventario, int id) {
        inventario.setId(id);
    }

    // ==================== MÉTODOS ESPECÍFICOS ====================

    /**
     * Lista todos os inventários ordenados por nome
     */
    @Override
    public List<Inventario> findAll() throws SQLException {
        String sql = "SELECT * FROM TABELA_INVENTARIO ORDER BY NOME";
        return executeQuery(sql);
    }

    /**
     * Busca inventários por filtro (nome ou status)
     */
    public List<Inventario> buscarPorFiltro(String filtro) throws SQLException {
        String sql = "SELECT * FROM TABELA_INVENTARIO " +
                "WHERE UPPER(NOME) LIKE UPPER(?) OR UPPER(STATUS_INVENTARIO) LIKE UPPER(?) " +
                "ORDER BY NOME";
        String filtroLike = "%" + filtro + "%";
        return executeQuery(sql, filtroLike, filtroLike);
    }

    /**
     * Busca inventário por status (retorna o mais recente)
     * Compatível com PostgreSQL (TABELA_INVENTARIO) e SQLite (local_inventario)
     */
    public Inventario buscarPorStatus(String status) throws SQLException {
        String tableName = getInventarioTableName();
        boolean isSqlite = tableName.equals("local_inventario");

        String sql;
        if (isSqlite) {
            sql = "SELECT id as ID, nome_inventario as NOME, data_inicio as DATA_INICIO, " +
                    "data_fim as DATA_FIM, status as STATUS_INVENTARIO, " +
                    "responsavel as RESPONSAVEL_INVENTARIO, percentual_conclusao as PERCENTUAL_CONCLUSAO, " +
                    "local_created_at as DATA_CRIACAO " +
                    "FROM " + tableName + " " +
                    "WHERE status = ? " +
                    "ORDER BY local_created_at DESC LIMIT 1";
        } else {
            sql = "SELECT * FROM " + tableName + " " +
                    "WHERE STATUS_INVENTARIO = ? " +
                    "ORDER BY DATA_CRIACAO DESC LIMIT 1";
        }

        return executeQuerySingle(sql, status);
    }

    /**
     * Busca o inventário ativo (em andamento)
     * Retorna o inventário com status EM_ANDAMENTO mais recente
     * Compatível com PostgreSQL (TABELA_INVENTARIO) e SQLite (local_inventario)
     */
    public Inventario buscarInventarioAtivo() throws SQLException {
        String tableName = getInventarioTableName();
        boolean isSqlite = tableName.equals("local_inventario");

        String sql;
        if (isSqlite) {
            sql = "SELECT id as ID, nome_inventario as NOME, data_inicio as DATA_INICIO, " +
                    "data_fim as DATA_FIM, status as STATUS_INVENTARIO, " +
                    "responsavel as RESPONSAVEL_INVENTARIO, percentual_conclusao as PERCENTUAL_CONCLUSAO, " +
                    "local_created_at as DATA_CRIACAO " +
                    "FROM " + tableName + " " +
                    "WHERE status = 'EM_ANDAMENTO' " +
                    "ORDER BY data_inicio DESC LIMIT 1";
        } else {
            sql = "SELECT * FROM " + tableName + " " +
                    "WHERE STATUS_INVENTARIO = 'EM_ANDAMENTO' " +
                    "ORDER BY DATA_INICIO DESC LIMIT 1";
        }

        Inventario inventario = executeQuerySingle(sql);

        if (inventario == null) {
            // Fallback: buscar o inventário mais recente independente do status
            if (isSqlite) {
                sql = "SELECT id as ID, nome_inventario as NOME, data_inicio as DATA_INICIO, " +
                        "data_fim as DATA_FIM, status as STATUS_INVENTARIO, " +
                        "responsavel as RESPONSAVEL_INVENTARIO, percentual_conclusao as PERCENTUAL_CONCLUSAO, " +
                        "local_created_at as DATA_CRIACAO " +
                        "FROM " + tableName + " " +
                        "ORDER BY data_inicio DESC LIMIT 1";
            } else {
                sql = "SELECT * FROM " + tableName + " " +
                        "ORDER BY DATA_INICIO DESC LIMIT 1";
            }

            inventario = executeQuerySingle(sql);
        }

        return inventario;
    }

    /**
     * Busca inventários por status (todos)
     */
    public List<Inventario> buscarTodosPorStatus(String status) throws SQLException {
        String sql = "SELECT * FROM TABELA_INVENTARIO " +
                "WHERE STATUS_INVENTARIO = ? " +
                "ORDER BY DATA_CRIACAO DESC";
        return executeQuery(sql, status);
    }

    /**
     * Finaliza um inventário (altera status para CONCLUIDO)
     * 
     * CORREÇÃO 27/12/2025: Adicionada atualização de DATA_FIM
     * - Registra a data de encerramento do inventário
     * - Atualiza status para CONCLUIDO
     * - Define percentual de conclusão como 100%
     */
    public boolean finalizar(int id) throws SQLException {
        String sql = "UPDATE TABELA_INVENTARIO SET STATUS_INVENTARIO = ?, PERCENTUAL_CONCLUSAO = ?, DATA_FIM = ? WHERE ID = ?";
        return executeUpdate(sql, "CONCLUIDO", new java.math.BigDecimal("100.00"), new java.sql.Date(System.currentTimeMillis()), id) > 0;
    }

    /**
     * Atualiza o percentual de conclusão
     */
    public boolean atualizarPercentual(int id, java.math.BigDecimal percentual) throws SQLException {
        String sql = "UPDATE TABELA_INVENTARIO SET PERCENTUAL_CONCLUSAO = ? WHERE ID = ?";
        return executeUpdate(sql, percentual, id) > 0;
    }

    private static final Logger logger = LoggerFactory.getLogger(InventarioDAO.class);
    
    /**
     * Atualiza o status do inventário
     */
    public boolean atualizarStatus(int id, String status) throws SQLException {
        String sql = "UPDATE TABELA_INVENTARIO SET STATUS_INVENTARIO = ? WHERE ID = ?";
        boolean atualizado = executeUpdate(sql, status, id) > 0;
        
        if (atualizado) {
            // Publicar evento para atualização da dashboard
            publicarEventoInventarioAlterado(id, status);
        }
        
        return atualizado;
    }
    
    /**
     * Publica evento de inventário alterado para a dashboard.
     * Este método não lança exceções para não afetar a operação principal.
     */
    private void publicarEventoInventarioAlterado(int inventarioId, String novoStatus) {
        try {
            DashboardEvent event = DashboardEvent.builder(DashboardEventType.INVENTARIO_ALTERADO)
                .source("InventarioDAO")
                .addMetadata("inventarioId", inventarioId)
                .addMetadata("status", novoStatus)
                .addAffectedEntityId(inventarioId)
                .build();
            
            DashboardEventBus.getInstance().publish(event);
            logger.debug("Evento INVENTARIO_ALTERADO publicado para inventário ID: {}", inventarioId);
        } catch (Exception e) {
            logger.warn("Falha ao publicar evento de inventário alterado: {}", e.getMessage());
        }
    }

    /**
     * Conta inventários por status
     */
    public int contarPorStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TABELA_INVENTARIO WHERE STATUS_INVENTARIO = ?";
        Integer count = executeScalar(sql, Integer.class, status);
        return count != null ? count : 0;
    }

    /**
     * Verifica se existe inventário ativo (EM_ANDAMENTO)
     */
    public boolean existeInventarioAtivo() throws SQLException {
        String sql = "SELECT COUNT(*) FROM TABELA_INVENTARIO WHERE STATUS_INVENTARIO = 'EM_ANDAMENTO'";
        Integer count = executeScalar(sql, Integer.class);
        return count != null && count > 0;
    }

    // ==================== MÉTODOS LEGADOS (COMPATIBILIDADE) ====================

    /**
     * @deprecated Use insert() do BaseDAO que retorna void
     */
    @Deprecated
    public Integer inserir(Inventario inventario) {
        try {
            insert(inventario);
            return inventario.getId();
        } catch (SQLException e) {
            System.err.println("Erro ao inserir inventário: " + e.getMessage());
            return null;
        }
    }

    /**
     * @deprecated Use update() do BaseDAO
     */
    @Deprecated
    public boolean atualizar(Inventario inventario) {
        try {
            update(inventario);
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar inventário: " + e.getMessage());
            return false;
        }
    }

    /**
     * @deprecated Use delete() do BaseDAO
     */
    @Deprecated
    public boolean excluir(Integer id) {
        try {
            delete(id);
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao excluir inventário: " + e.getMessage());
            return false;
        }
    }

    /**
     * @deprecated Use findAll() do BaseDAO
     */
    @Deprecated
    public List<Inventario> listarInventarios() {
        try {
            return findAll();
        } catch (SQLException e) {
            System.err.println("Erro ao listar inventários: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    /**
     * @deprecated Use findById() do BaseDAO
     */
    @Deprecated
    public Inventario buscarInventarioPorId(int id) {
        try {
            return findById(id);
        } catch (SQLException e) {
            System.err.println("Erro ao buscar inventário: " + e.getMessage());
            return null;
        }
    }

    /**
     * @deprecated Use buscarPorFiltro()
     */
    @Deprecated
    public List<Inventario> buscarInventariosPorFiltro(String filtro) {
        try {
            return buscarPorFiltro(filtro);
        } catch (SQLException e) {
            System.err.println("Erro ao buscar inventários: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    /**
     * @deprecated Use buscarPorStatus()
     */
    @Deprecated
    public Inventario buscarInventarioPorStatus(String status) {
        try {
            return buscarPorStatus(status);
        } catch (SQLException e) {
            System.err.println("Erro ao buscar inventário por status: " + e.getMessage());
            return null;
        }
    }

    /**
     * Método auxiliar para parsear data de String em diferentes formatos
     * Necessário para compatibilidade com SQLite que pode retornar timestamps em
     * formatos variados
     */
    private java.sql.Date parseDataFromString(String dataStr) throws Exception {
        if (dataStr == null || dataStr.trim().isEmpty()) {
            return null;
        }

        String[] formatos = {
                "yyyy-MM-dd HH:mm:ss.SSS",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd",
                "dd/MM/yyyy HH:mm:ss",
                "dd/MM/yyyy"
        };

        for (String formato : formatos) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(formato);
                sdf.setLenient(false);
                java.util.Date parsed = sdf.parse(dataStr);
                return new java.sql.Date(parsed.getTime());
            } catch (Exception e) {
                // Tentar próximo formato
            }
        }

        // Se nenhum formato funcionou, tentar parsear como long (milissegundos)
        try {
            long millis = Long.parseLong(dataStr);
            return new java.sql.Date(millis);
        } catch (NumberFormatException e) {
            // Não é um número
        }

        throw new Exception("Não foi possível parsear a data: " + dataStr);
    }

    /**
     * Busca múltiplos inventários por IDs em uma única query (otimização de
     * performance)
     * 
     * @param ids lista de IDs dos inventários
     * @return lista de inventários encontrados
     * @throws SQLException em caso de erro no banco
     */
    public List<Inventario> buscarPorIds(List<Integer> ids) throws SQLException {
        if (ids == null || ids.isEmpty()) {
            return new java.util.ArrayList<>();
        }

        String tableName = getInventarioTableName();
        String placeholders = String.join(",", java.util.Collections.nCopies(ids.size(), "?"));
        String sql = "SELECT * FROM " + tableName + " WHERE id IN (" + placeholders + ")";

        List<Inventario> inventarios = new java.util.ArrayList<>();

        Connection conn = null;
        try {
            conn = com.inventario.util.DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);

            for (int i = 0; i < ids.size(); i++) {
                stmt.setInt(i + 1, ids.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                inventarios.add(mapResultSetToEntity(rs));
            }

            // Log: Buscados X inventários em batch
        } finally {
            com.inventario.util.ConnectionManager.closeConnection(conn);
        }

        return inventarios;
    }
}
