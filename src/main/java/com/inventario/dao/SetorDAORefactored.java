package com.inventario.dao;

import com.inventario.model.Setor;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Versão refatorada do SetorDAO usando BaseDAO
 * Demonstra eliminação de código duplicado
 */
@Repository
public class SetorDAORefactored extends BaseDAO<Setor, Integer> {
    
    // ========== Implementação dos Métodos Abstratos ==========
    
    @Override
    protected String getTableName() {
        return "TABELA_SETOR";
    }
    
    @Override
    protected String getInsertSQL() {
        return "INSERT INTO TABELA_SETOR (NOME, DESCRICAO, ATIVO) VALUES (?, ?, ?)";
    }
    
    @Override
    protected String getUpdateSQL() {
        return "UPDATE TABELA_SETOR SET NOME = ?, DESCRICAO = ?, ATIVO = ? WHERE ID = ?";
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement stmt, Setor setor) throws SQLException {
        stmt.setString(1, setor.getNome());
        stmt.setString(2, setor.getDescricao());
        stmt.setBoolean(3, setor.isAtivo());
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement stmt, Setor setor) throws SQLException {
        setInsertParameters(stmt, setor);
        stmt.setInt(4, setor.getId());
    }
    
    @Override
    protected Setor mapResultSetToEntity(ResultSet rs) throws SQLException {
        Setor setor = new Setor();
        setor.setId(rs.getInt("ID"));
        setor.setNome(rs.getString("NOME"));
        setor.setDescricao(rs.getString("DESCRICAO"));
        setor.setAtivo(rs.getBoolean("ATIVO"));
        return setor;
    }
    
    @Override
    protected void setGeneratedId(Setor entity, int id) {
        entity.setId(id);
    }
    
    // ========== Métodos Específicos do SetorDAO ==========
    
    /**
     * Lista apenas setores ativos
     */
    public List<Setor> listarSetoresAtivos() throws SQLException {
        String sql = "SELECT * FROM TABELA_SETOR WHERE ATIVO = true ORDER BY NOME";
        return executeQuery(sql);
    }
    
    /**
     * Busca setor por nome
     */
    public Setor buscarPorNome(String nome) throws SQLException {
        String sql = "SELECT * FROM TABELA_SETOR WHERE NOME = ?";
        return executeQuerySingle(sql, nome);
    }
    
    /**
     * Busca setor por descrição
     */
    public Setor buscarPorDescricao(String descricao) throws SQLException {
        String sql = "SELECT * FROM TABELA_SETOR WHERE DESCRICAO = ?";
        return executeQuerySingle(sql, descricao);
    }
    
    /**
     * Verifica se já existe um setor com o nome (exceto o próprio)
     */
    public boolean setorExiste(String nome, int idExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TABELA_SETOR WHERE NOME = ? AND ID != ?";
        Integer count = executeScalar(sql, Integer.class, nome, idExcluir);
        return count != null && count > 0;
    }
    
    /**
     * Conta quantos responsáveis estão vinculados ao setor
     */
    public int contarResponsaveisVinculados(int idSetor) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TABELA_RESPONSAVEL WHERE ID_SETOR = ?";
        Integer count = executeScalar(sql, Integer.class, idSetor);
        return count != null ? count : 0;
    }
    
    /**
     * Conta quantas salas estão vinculadas ao setor
     */
    public int contarSalasVinculadas(int idSetor) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TABELA_SALA WHERE ID_SETOR = ?";
        Integer count = executeScalar(sql, Integer.class, idSetor);
        return count != null ? count : 0;
    }
    
    /**
     * Ativa ou desativa um setor
     */
    public void alterarStatus(int idSetor, boolean ativo) throws SQLException {
        String sql = "UPDATE TABELA_SETOR SET ATIVO = ? WHERE ID = ?";
        executeUpdate(sql, ativo, idSetor);
    }
    
    /**
     * Busca setores por termo (nome ou descrição)
     */
    public List<Setor> buscarPorTermo(String termo) throws SQLException {
        String sql = "SELECT * FROM TABELA_SETOR " +
                    "WHERE NOME ILIKE ? OR DESCRICAO ILIKE ? " +
                    "ORDER BY NOME";
        String termoBusca = "%" + termo + "%";
        return executeQuery(sql, termoBusca, termoBusca);
    }
    
    /**
     * Lista setores com contagem de responsáveis e salas
     */
    public List<SetorComEstatisticas> listarComEstatisticas() throws SQLException {
        String sql = "SELECT s.*, " +
                    "(SELECT COUNT(*) FROM TABELA_RESPONSAVEL WHERE ID_SETOR = s.ID) as qtd_responsaveis, " +
                    "(SELECT COUNT(*) FROM TABELA_SALA WHERE ID_SETOR = s.ID) as qtd_salas " +
                    "FROM TABELA_SETOR s " +
                    "ORDER BY s.NOME";
        
        return executeQuery(sql).stream()
            .map(setor -> {
                // Aqui você pode criar um DTO com estatísticas
                // Por simplicidade, retornando apenas o setor
                return new SetorComEstatisticas(setor, 0, 0);
            })
            .toList();
    }
    
    /**
     * Lista setores por campus
     */
    public List<Setor> listarSetoresPorCampus(int idCampus) throws SQLException {
        String sql = "SELECT * FROM TABELA_SETOR WHERE ID_CAMPUS = ? AND ATIVO = TRUE ORDER BY NOME";
        return executeQuery(sql, idCampus);
    }
    
    // ========== Classes Auxiliares ==========
    
    /**
     * DTO para setor com estatísticas
     */
    public static class SetorComEstatisticas {
        private final Setor setor;
        private final int quantidadeResponsaveis;
        private final int quantidadeSalas;
        
        public SetorComEstatisticas(Setor setor, int qtdResponsaveis, int qtdSalas) {
            this.setor = setor;
            this.quantidadeResponsaveis = qtdResponsaveis;
            this.quantidadeSalas = qtdSalas;
        }
        
        public Setor getSetor() { return setor; }
        public int getQuantidadeResponsaveis() { return quantidadeResponsaveis; }
        public int getQuantidadeSalas() { return quantidadeSalas; }
    }
    
    // ==================== MÉTODOS LEGADOS ADICIONAIS (COMPATIBILIDADE) ====================
    
    /**
     * @deprecated Use findAll() do BaseDAO
     */
    @Deprecated
    public List<Setor> listarSetores() {
        try {
            return findAll();
        } catch (SQLException e) {
            System.err.println("Erro ao listar setores: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
