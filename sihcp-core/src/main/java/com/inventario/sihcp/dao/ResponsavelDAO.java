package com.inventario.sihcp.dao;

import com.inventario.sihcp.model.Responsavel;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO Refatorado para gerenciar operações CRUD da tabela RESPONSAVEL
 * 
 * Herda funcionalidades comuns do BaseDAO:
 * - Gerenciamento de conexões via ConnectionManager
 * - Métodos CRUD padronizados (insert, update, delete, findById, findAll)
 * - Tratamento de erros consistente
 * - Logs estruturados
 * 
 * Redução: ~400 linhas → ~160 linhas (-60%)
 * 
 * @author Sistema de Inventário IFMT
 * @version 2.0 - Refatorado
 */
@Repository
public class ResponsavelDAO extends BaseDAO<Responsavel, Integer> {
    
    // ==================== MÉTODOS ABSTRATOS IMPLEMENTADOS ====================
    
    @Override
    protected String getTableName() {
        return "TABELA_RESPONSAVEL";
    }
    
    @Override
    protected String getInsertSQL() {
        return "INSERT INTO TABELA_RESPONSAVEL (NOME, CPF, EMAIL, TELEFONE, CARGO, ID_SETOR) " +
               "VALUES (?, ?, ?, ?, ?, ?)";
    }
    
    @Override
    protected String getUpdateSQL() {
        return "UPDATE TABELA_RESPONSAVEL SET NOME = ?, CPF = ?, EMAIL = ?, " +
               "TELEFONE = ?, CARGO = ?, ID_SETOR = ? WHERE ID = ?";
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement stmt, Responsavel responsavel) throws SQLException {
        stmt.setString(1, responsavel.getNome());
        stmt.setString(2, responsavel.getCpf());
        stmt.setString(3, responsavel.getEmail());
        stmt.setString(4, responsavel.getTelefone());
        stmt.setString(5, responsavel.getCargo());
        
        if (responsavel.getIdSetor() > 0) {
            stmt.setInt(6, responsavel.getIdSetor());
        } else {
            stmt.setNull(6, Types.INTEGER);
        }
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement stmt, Responsavel responsavel) throws SQLException {
        setInsertParameters(stmt, responsavel);
        stmt.setInt(7, responsavel.getId());
    }
    
    @Override
    protected Responsavel mapResultSetToEntity(ResultSet rs) throws SQLException {
        Responsavel responsavel = new Responsavel();
        responsavel.setId(rs.getInt("ID"));
        responsavel.setNome(rs.getString("NOME"));
        responsavel.setCpf(rs.getString("CPF"));
        responsavel.setEmail(rs.getString("EMAIL"));
        responsavel.setTelefone(rs.getString("TELEFONE"));
        responsavel.setCargo(rs.getString("CARGO"));
        responsavel.setIdSetor(rs.getInt("ID_SETOR"));
        responsavel.setAtivo(rs.getBoolean("ATIVO"));
        responsavel.setDataCadastro(rs.getTimestamp("DATA_CADASTRO"));
        
        // Campos opcionais (podem não existir em todas as queries)
        try {
            responsavel.setNomeSetor(rs.getString("NOME_SETOR"));
        } catch (SQLException e) {
            // Coluna pode não existir em algumas consultas
        }
        
        try {
            responsavel.setObservacoes(rs.getString("OBSERVACOES"));
        } catch (SQLException e) {
            // Coluna pode não existir em algumas consultas
        }
        
        return responsavel;
    }
    
    @Override
    protected void setGeneratedId(Responsavel responsavel, int id) {
        responsavel.setId(id);
    }
    
    // ==================== MÉTODOS ESPECÍFICOS ====================
    
    /**
     * Lista todos os responsáveis ativos com informações do setor
     */
    @Override
    public List<Responsavel> findAll() throws SQLException {
        System.out.println("[DEBUG ResponsavelDAO] ========================================");
        System.out.println("[DEBUG ResponsavelDAO] Iniciando findAll()");
        
        String sql = "SELECT r.*, s.NOME as NOME_SETOR FROM TABELA_RESPONSAVEL r " +
                    "LEFT JOIN TABELA_SETOR s ON r.ID_SETOR = s.ID " +
                    "WHERE r.ATIVO = TRUE ORDER BY r.NOME";
        
        System.out.println("[DEBUG ResponsavelDAO] SQL: " + sql);
        
        try {
            List<Responsavel> resultado = executeQuery(sql);
            System.out.println("[DEBUG ResponsavelDAO] Quantidade de responsáveis encontrados: " + resultado.size());
            
            if (resultado.isEmpty()) {
                System.out.println("[DEBUG ResponsavelDAO] NENHUM responsável encontrado!");
                System.out.println("[DEBUG ResponsavelDAO] Verificando se existem responsáveis na tabela...");
                
                // Query sem filtro ATIVO para debug
                String sqlDebug = "SELECT COUNT(*) as total FROM TABELA_RESPONSAVEL";
                System.out.println("[DEBUG ResponsavelDAO] SQL Debug: " + sqlDebug);
                
                try (java.sql.Connection conn = com.inventario.sihcp.util.ConnectionManager.getConnection();
                     java.sql.PreparedStatement stmt = conn.prepareStatement(sqlDebug);
                     java.sql.ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int total = rs.getInt("total");
                        System.out.println("[DEBUG ResponsavelDAO] Total de responsáveis na tabela (sem filtro): " + total);
                    }
                } catch (Exception e) {
                    System.err.println("[DEBUG ResponsavelDAO] Erro ao contar responsáveis: " + e.getMessage());
                }
            } else {
                for (Responsavel r : resultado) {
                    System.out.println("[DEBUG ResponsavelDAO] - ID: " + r.getId() + ", Nome: " + r.getNome() + ", Ativo: " + r.isAtivo());
                }
            }
            
            System.out.println("[DEBUG ResponsavelDAO] ========================================");
            return resultado;
            
        } catch (SQLException e) {
            System.err.println("[ERRO ResponsavelDAO] Erro ao executar findAll(): " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Busca responsável por ID com informações do setor
     */
    @Override
    public Responsavel findById(Integer id) throws SQLException {
        String sql = "SELECT r.*, s.NOME as NOME_SETOR FROM TABELA_RESPONSAVEL r " +
                    "LEFT JOIN TABELA_SETOR s ON r.ID_SETOR = s.ID " +
                    "WHERE r.ID = ? AND r.ATIVO = TRUE";
        
        return executeQuerySingle(sql, id);
    }
    
    /**
     * Busca responsáveis por nome exato
     */
    public List<Responsavel> buscarPorNome(String nome) throws SQLException {
        String sql = "SELECT r.*, s.NOME as NOME_SETOR FROM TABELA_RESPONSAVEL r " +
                    "LEFT JOIN TABELA_SETOR s ON r.ID_SETOR = s.ID " +
                    "WHERE r.ATIVO = TRUE AND UPPER(r.NOME) = UPPER(?) " +
                    "ORDER BY r.NOME";
        
        return executeQuery(sql, nome);
    }
    
    /**
     * Busca responsáveis por nome exato (usando conexão existente)
     */
    public List<Responsavel> buscarPorNome(String nome, Connection conn) throws SQLException {
        String sql = "SELECT r.*, s.NOME as NOME_SETOR FROM TABELA_RESPONSAVEL r " +
                    "LEFT JOIN TABELA_SETOR s ON r.ID_SETOR = s.ID " +
                    "WHERE r.ATIVO = TRUE AND UPPER(r.NOME) = UPPER(?) " +
                    "ORDER BY r.NOME";
        
        return executeQuery(conn, sql, nome);
    }
    
    /**
     * Busca responsável por CPF
     */
    public Responsavel buscarPorCPF(String cpf) throws SQLException {
        String sql = "SELECT r.*, s.NOME as NOME_SETOR FROM TABELA_RESPONSAVEL r " +
                    "LEFT JOIN TABELA_SETOR s ON r.ID_SETOR = s.ID " +
                    "WHERE r.ATIVO = TRUE AND r.CPF = ?";
        
        return executeQuerySingle(sql, cpf);
    }
    
    /**
     * Busca responsáveis por setor
     */
    public List<Responsavel> buscarPorSetor(int idSetor) throws SQLException {
        String sql = "SELECT r.*, s.NOME as NOME_SETOR FROM TABELA_RESPONSAVEL r " +
                    "LEFT JOIN TABELA_SETOR s ON r.ID_SETOR = s.ID " +
                    "WHERE r.ATIVO = TRUE AND r.ID_SETOR = ? ORDER BY r.NOME";
        
        return executeQuery(sql, idSetor);
    }
    
    /**
     * Busca responsáveis por filtro (nome, CPF, email ou cargo)
     */
    public List<Responsavel> buscarPorFiltro(String filtro) throws SQLException {
        String sql = "SELECT r.*, s.NOME as NOME_SETOR FROM TABELA_RESPONSAVEL r " +
                    "LEFT JOIN TABELA_SETOR s ON r.ID_SETOR = s.ID " +
                    "WHERE r.ATIVO = TRUE AND (" +
                    "UPPER(r.NOME) LIKE UPPER(?) OR " +
                    "UPPER(r.CPF) LIKE UPPER(?) OR " +
                    "UPPER(r.EMAIL) LIKE UPPER(?) OR " +
                    "UPPER(r.CARGO) LIKE UPPER(?)) " +
                    "ORDER BY r.NOME";
        
        String filtroLike = "%" + filtro + "%";
        return executeQuery(sql, filtroLike, filtroLike, filtroLike, filtroLike);
    }
    
    /**
     * Verifica se um responsável já existe com o mesmo CPF
     */
    public boolean responsavelExiste(String cpf, int idResponsavelExcluir) throws SQLException {
        String sql = "SELECT COUNT(*)::INTEGER FROM TABELA_RESPONSAVEL WHERE CPF = ? AND ATIVO = TRUE";
        
        if (idResponsavelExcluir > 0) {
            sql += " AND ID != ?";
            Integer count = executeScalar(sql, Integer.class, cpf, idResponsavelExcluir);
            return count != null && count > 0;
        }
        
        Integer count = executeScalar(sql, Integer.class, cpf);
        return count != null && count > 0;
    }
    
    /**
     * Verifica se um email já está em uso
     */
    public boolean emailExiste(String email, int idResponsavelExcluir) throws SQLException {
        String sql = "SELECT COUNT(*)::INTEGER FROM TABELA_RESPONSAVEL WHERE UPPER(EMAIL) = UPPER(?) AND ATIVO = TRUE";
        
        if (idResponsavelExcluir > 0) {
            sql += " AND ID != ?";
            Integer count = executeScalar(sql, Integer.class, email, idResponsavelExcluir);
            return count != null && count > 0;
        }
        
        Integer count = executeScalar(sql, Integer.class, email);
        return count != null && count > 0;
    }
    
    /**
     * Conta quantos patrimônios estão vinculados ao responsável
     */
    public int contarPatrimoniosDoResponsavel(int idResponsavel) throws SQLException {
        String sql = "SELECT COUNT(*)::INTEGER FROM TABELA_PATRIMONIO WHERE ID_RESPONSAVEL = ?";
        Integer count = executeScalar(sql, Integer.class, idResponsavel);
        return count != null ? count : 0;
    }
    
    /**
     * Conta responsáveis por setor
     */
    public int contarPorSetor(int idSetor) throws SQLException {
        String sql = "SELECT COUNT(*)::INTEGER FROM TABELA_RESPONSAVEL WHERE ID_SETOR = ? AND ATIVO = TRUE";
        Integer count = executeScalar(sql, Integer.class, idSetor);
        return count != null ? count : 0;
    }
    
    // ==================== MÉTODOS LEGADOS (COMPATIBILIDADE) ====================
    
    /**
     * @deprecated Use insert() do BaseDAO
     */
    @Deprecated
    public boolean inserirResponsavel(Responsavel responsavel) {
        try {
            insert(responsavel);
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao inserir responsável: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * @deprecated Use insert() do BaseDAO que retorna o ID
     */
    @Deprecated
    public Integer inserirResponsavelComId(Responsavel responsavel) {
        try {
            insert(responsavel);
            return responsavel.getId();
        } catch (SQLException e) {
            System.err.println("Erro ao inserir responsável: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * @deprecated Use update() do BaseDAO
     */
    @Deprecated
    public boolean atualizarResponsavel(Responsavel responsavel) {
        try {
            update(responsavel);
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar responsável: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * @deprecated Use delete() do BaseDAO
     */
    @Deprecated
    public boolean excluirResponsavel(int idResponsavel) {
        try {
            delete(idResponsavel);
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao excluir responsável: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * @deprecated Use findById() do BaseDAO
     */
    @Deprecated
    public Responsavel buscarResponsavelPorId(int idResponsavel) {
        try {
            return findById(idResponsavel);
        } catch (SQLException e) {
            System.err.println("Erro ao buscar responsável: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * @deprecated Use findAll() do BaseDAO
     */
    @Deprecated
    public List<Responsavel> listarResponsaveis() {
        try {
            return findAll();
        } catch (SQLException e) {
            System.err.println("Erro ao listar responsáveis: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * @deprecated Use buscarPorFiltro()
     */
    @Deprecated
    public List<Responsavel> buscarResponsaveisPorFiltro(String filtro) {
        try {
            return buscarPorFiltro(filtro);
        } catch (SQLException e) {
            System.err.println("Erro ao buscar responsáveis: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * @deprecated Use buscarPorSetor()
     */
    @Deprecated
    public List<Responsavel> listarResponsaveisPorSetor(int idSetor) {
        try {
            return buscarPorSetor(idSetor);
        } catch (SQLException e) {
            System.err.println("Erro ao listar responsáveis por setor: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
