package com.inventario.dao;

import com.inventario.model.Usuario;
import com.inventario.model.PerfilUsuario;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO Refatorado para gerenciar operações CRUD da tabela USUARIO
 * 
 * Herda funcionalidades comuns do BaseDAO:
 * - Gerenciamento de conexões via ConnectionManager
 * - Métodos CRUD padronizados (insert, update, delete, findById, findAll)
 * - Tratamento de erros consistente
 * - Logs estruturados
 * 
 * Redução: ~450 linhas → ~200 linhas (-56%)
 * 
 * @author Sistema de Inventário IFMT
 * @version 2.0 - Refatorado
 */
@Repository
public class UsuarioDAO extends BaseDAO<Usuario, Integer> {
    
    // ==================== MÉTODOS ABSTRATOS IMPLEMENTADOS ====================
    
    @Override
    protected String getTableName() {
        return "TABELA_USUARIO";
    }
    
    @Override
    protected String getInsertSQL() {
        return "INSERT INTO TABELA_USUARIO (LOGIN, SENHA_HASH, NOME_COMPLETO, EMAIL, MATRICULA, " +
               "PERFIL, ID_SETOR, ATIVO, BLOQUEADO, PRIMEIRO_ACESSO, OBSERVACOES) " +
               "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }
    
    @Override
    protected String getUpdateSQL() {
        return "UPDATE TABELA_USUARIO SET LOGIN = ?, SENHA_HASH = ?, NOME_COMPLETO = ?, " +
               "EMAIL = ?, MATRICULA = ?, PERFIL = ?, ID_SETOR = ?, ATIVO = ?, BLOQUEADO = ?, " +
               "PRIMEIRO_ACESSO = ?, OBSERVACOES = ? WHERE ID = ?";
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement stmt, Usuario usuario) throws SQLException {
        stmt.setString(1, usuario.getLogin());
        stmt.setString(2, usuario.getSenhaHash());
        stmt.setString(3, usuario.getNomeCompleto());
        stmt.setString(4, usuario.getEmail());
        stmt.setString(5, usuario.getMatricula());
        stmt.setString(6, usuario.getPerfil().name());
        
        // ID_SETOR - pode ser null
        if (usuario.getIdSetor() != null) {
            stmt.setInt(7, usuario.getIdSetor());
        } else {
            stmt.setNull(7, java.sql.Types.INTEGER);
        }
        
        // Campos BOOLEAN - usar setBoolean (funciona tanto para BOOLEAN quanto CHAR(1))
        stmt.setBoolean(8, Boolean.TRUE.equals(usuario.getAtivo()));
        stmt.setBoolean(9, Boolean.TRUE.equals(usuario.getBloqueado()));
        stmt.setBoolean(10, Boolean.TRUE.equals(usuario.getPrimeiroAcesso()));
        stmt.setString(11, usuario.getObservacoes());
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement stmt, Usuario usuario) throws SQLException {
        stmt.setString(1, usuario.getLogin());
        stmt.setString(2, usuario.getSenhaHash());
        stmt.setString(3, usuario.getNomeCompleto());
        stmt.setString(4, usuario.getEmail());
        stmt.setString(5, usuario.getMatricula());
        stmt.setString(6, usuario.getPerfil().name());
        
        // ID_SETOR - pode ser null
        if (usuario.getIdSetor() != null) {
            stmt.setInt(7, usuario.getIdSetor());
        } else {
            stmt.setNull(7, java.sql.Types.INTEGER);
        }
        
        // Campos BOOLEAN
        stmt.setBoolean(8, Boolean.TRUE.equals(usuario.getAtivo()));
        stmt.setBoolean(9, Boolean.TRUE.equals(usuario.getBloqueado()));
        stmt.setBoolean(10, Boolean.TRUE.equals(usuario.getPrimeiroAcesso()));
        stmt.setString(11, usuario.getObservacoes());
        
        // ID para WHERE clause
        stmt.setInt(12, usuario.getId());
    }
    
    @Override
    protected Usuario mapResultSetToEntity(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        
        // ID - Converter de forma segura
        int id = rs.getInt("ID");
        if (!rs.wasNull()) {
            usuario.setId(id);
        }
        
        usuario.setLogin(rs.getString("LOGIN"));
        usuario.setSenhaHash(rs.getString("SENHA_HASH"));
        usuario.setNomeCompleto(rs.getString("NOME_COMPLETO"));
        usuario.setEmail(rs.getString("EMAIL"));
        
        String perfilStr = rs.getString("PERFIL");
        if (perfilStr != null) {
            try {
                usuario.setPerfil(PerfilUsuario.valueOf(perfilStr));
            } catch (IllegalArgumentException e) {
                usuario.setPerfil(PerfilUsuario.CONSULTA); // Valor padrão
            }
        }
        
        // ID_SETOR - Converter de forma segura
        int idSetor = rs.getInt("ID_SETOR");
        if (!rs.wasNull()) {
            usuario.setIdSetor(idSetor);
        }
        
        usuario.setMatricula(rs.getString("MATRICULA"));
        
        // Campos BOOLEAN - Suporta tanto BOOLEAN quanto CHAR(1) 'S'/'N'
        usuario.setAtivo(converterParaBoolean(rs, "ATIVO"));
        usuario.setBloqueado(converterParaBoolean(rs, "BLOQUEADO"));
        usuario.setPrimeiroAcesso(converterParaBoolean(rs, "PRIMEIRO_ACESSO"));
        
        // Campos de data
        Timestamp dataCriacao = rs.getTimestamp("DATA_CRIACAO");
        if (dataCriacao != null) {
            usuario.setDataCriacao(dataCriacao.toLocalDateTime());
        }
        
        Timestamp dataUltimoAcesso = rs.getTimestamp("DATA_ULTIMO_ACESSO");
        if (dataUltimoAcesso != null) {
            usuario.setDataUltimoAcesso(dataUltimoAcesso.toLocalDateTime());
        }
        
        Timestamp dataBloqueio = rs.getTimestamp("DATA_BLOQUEIO");
        if (dataBloqueio != null) {
            usuario.setDataBloqueio(dataBloqueio.toLocalDateTime());
        }
        
        Timestamp dataExpiracaoSenha = rs.getTimestamp("DATA_EXPIRACAO_SENHA");
        if (dataExpiracaoSenha != null) {
            usuario.setDataExpiracaoSenha(dataExpiracaoSenha.toLocalDateTime());
        }
        
        Timestamp dataAtualizacao = rs.getTimestamp("UPDATED_AT");
        if (dataAtualizacao != null) {
            usuario.setDataAtualizacao(dataAtualizacao.toLocalDateTime());
        }
        
        // TENTATIVAS_LOGIN - Converter de forma segura (pode ser INT ou BIGINT)
        int tentativas = rs.getInt("TENTATIVAS_LOGIN");
        if (!rs.wasNull()) {
            usuario.setTentativasLogin(tentativas);
        }
        
        // Campo de observações
        usuario.setObservacoes(rs.getString("OBSERVACOES"));
        
        // Nome do setor (se disponível no JOIN)
        try {
            usuario.setNomeSetor(rs.getString("NOME_SETOR"));
        } catch (SQLException e) {
            // Coluna pode não existir em algumas consultas
        }
        
        return usuario;
    }
    
    /**
     * Converte valores do banco para Boolean
     * Suporta: BOOLEAN (true/false), CHAR(1) ('S'/'N'), INTEGER (1/0)
     */
    private Boolean converterParaBoolean(ResultSet rs, String columnName) throws SQLException {
        Object value = rs.getObject(columnName);
        if (value == null) {
            return false;
        }
        
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        
        if (value instanceof String) {
            String str = ((String) value).trim().toUpperCase();
            return "S".equals(str) || "TRUE".equals(str) || "1".equals(str);
        }
        
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        
        return false;
    }
    
    @Override
    protected void setGeneratedId(Usuario usuario, int id) {
        usuario.setId(id);
    }
    
    // ==================== MÉTODOS ESPECÍFICOS ====================
    
    /**
     * Lista todos os usuários ativos com informações do setor
     */
    @Override
    public List<Usuario> findAll() throws SQLException {
        String sql = "SELECT u.*, s.NOME as NOME_SETOR FROM TABELA_USUARIO u " +
                    "LEFT JOIN TABELA_SETOR s ON u.ID_SETOR = s.ID " +
                    "WHERE u.ATIVO = true ORDER BY u.NOME_COMPLETO";
        
        return executeQuery(sql);
    }
    
    /**
     * Lista todos os usuários (ativos e inativos)
     */
    public List<Usuario> findAllIncludingInactive() throws SQLException {
        String sql = "SELECT u.*, s.NOME as NOME_SETOR FROM TABELA_USUARIO u " +
                    "LEFT JOIN TABELA_SETOR s ON u.ID_SETOR = s.ID " +
                    "ORDER BY u.NOME_COMPLETO";
        
        return executeQuery(sql);
    }
    
    /**
     * Busca usuário por ID com informações do setor
     */
    @Override
    public Usuario findById(Integer id) throws SQLException {
        String sql = "SELECT u.*, s.NOME as NOME_SETOR FROM TABELA_USUARIO u " +
                    "LEFT JOIN TABELA_SETOR s ON u.ID_SETOR = s.ID " +
                    "WHERE u.ID = ?";
        
        return executeQuerySingle(sql, id);
    }
    
    /**
     * Busca usuário por login
     */
    public Usuario buscarPorLogin(String login) throws SQLException {
        String sql = "SELECT u.*, s.NOME as NOME_SETOR FROM TABELA_USUARIO u " +
                    "LEFT JOIN TABELA_SETOR s ON u.ID_SETOR = s.ID " +
                    "WHERE u.LOGIN = ? AND u.ATIVO = true";
        
        return executeQuerySingle(sql, login);
    }
    
    /**
     * Busca usuário por email
     */
    public Usuario buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT u.*, s.NOME as NOME_SETOR FROM TABELA_USUARIO u " +
                    "LEFT JOIN TABELA_SETOR s ON u.ID_SETOR = s.ID " +
                    "WHERE UPPER(u.EMAIL) = UPPER(?) AND u.ATIVO = true";
        
        return executeQuerySingle(sql, email);
    }
    
    /**
     * Busca usuários por filtro (login, nome ou email)
     */
    public List<Usuario> buscarPorFiltro(String filtro) throws SQLException {
        String sql = "SELECT u.*, s.NOME as NOME_SETOR FROM TABELA_USUARIO u " +
                    "LEFT JOIN TABELA_SETOR s ON u.ID_SETOR = s.ID " +
                    "WHERE u.ATIVO = true AND (" +
                    "UPPER(u.LOGIN) LIKE UPPER(?) OR " +
                    "UPPER(u.NOME_COMPLETO) LIKE UPPER(?) OR " +
                    "UPPER(u.EMAIL) LIKE UPPER(?)) " +
                    "ORDER BY u.NOME_COMPLETO";
        
        String filtroLike = "%" + filtro + "%";
        return executeQuery(sql, filtroLike, filtroLike, filtroLike);
    }
    
    /**
     * Busca usuários por perfil
     */
    public List<Usuario> buscarPorPerfil(PerfilUsuario perfil) throws SQLException {
        String sql = "SELECT u.*, s.NOME as NOME_SETOR FROM TABELA_USUARIO u " +
                    "LEFT JOIN TABELA_SETOR s ON u.ID_SETOR = s.ID " +
                    "WHERE u.PERFIL = ? AND u.ATIVO = true " +
                    "ORDER BY u.NOME_COMPLETO";
        
        return executeQuery(sql, perfil.name());
    }
    
    /**
     * Busca usuários por setor
     */
    public List<Usuario> buscarPorSetor(int idSetor) throws SQLException {
        String sql = "SELECT u.*, s.NOME as NOME_SETOR FROM TABELA_USUARIO u " +
                    "LEFT JOIN TABELA_SETOR s ON u.ID_SETOR = s.ID " +
                    "WHERE u.ID_SETOR = ? AND u.ATIVO = true " +
                    "ORDER BY u.NOME_COMPLETO";
        
        return executeQuery(sql, idSetor);
    }
    
    /**
     * Verifica se um login já existe
     */
    public boolean loginExiste(String login) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TABELA_USUARIO WHERE LOGIN = ?";
        Long count = executeScalar(sql, Long.class, login);
        return count != null && count > 0;
    }
    
    /**
     * Verifica se um email já existe
     */
    public boolean emailExiste(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TABELA_USUARIO WHERE UPPER(EMAIL) = UPPER(?)";
        Long count = executeScalar(sql, Long.class, email);
        return count != null && count > 0;
    }
    
    /**
     * Bloqueia um usuário
     */
    public boolean bloquearUsuario(int id) throws SQLException {
        String sql = "UPDATE TABELA_USUARIO SET BLOQUEADO = true, DATA_BLOQUEIO = CURRENT_TIMESTAMP, " +
                    "UPDATED_AT = CURRENT_TIMESTAMP WHERE ID = ?";
        return executeUpdate(sql, id) > 0;
    }
    
    /**
     * Desbloqueia um usuário
     */
    public boolean desbloquearUsuario(int id) throws SQLException {
        String sql = "UPDATE TABELA_USUARIO SET BLOQUEADO = false, DATA_BLOQUEIO = NULL, " +
                    "TENTATIVAS_LOGIN = 0, UPDATED_AT = CURRENT_TIMESTAMP WHERE ID = ?";
        return executeUpdate(sql, id) > 0;
    }
    
    /**
     * Atualiza a senha do usuário
     */
    public boolean atualizarSenha(int id, String novaSenhaHash) throws SQLException {
        String sql = "UPDATE TABELA_USUARIO SET SENHA_HASH = ?, PRIMEIRO_ACESSO = false, " +
                    "DATA_EXPIRACAO_SENHA = CURRENT_DATE + INTERVAL '90 days', " +
                    "UPDATED_AT = CURRENT_TIMESTAMP WHERE ID = ?";
        return executeUpdate(sql, novaSenhaHash, id) > 0;
    }
    
    /**
     * Soft delete - desativa o usuário
     */
    @Override
    public void delete(Integer id) throws SQLException {
        String sql = "UPDATE TABELA_USUARIO SET ATIVO = false, UPDATED_AT = CURRENT_TIMESTAMP WHERE ID = ?";
        executeUpdate(sql, id);
    }
    
    // ==================== MÉTODOS LEGADOS (COMPATIBILIDADE) ====================
    
    /**
     * @deprecated Use insert() do BaseDAO
     */
    @Deprecated
    public boolean inserirUsuario(Usuario usuario) {
        try {
            insert(usuario);
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao inserir usuário: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * @deprecated Use update() do BaseDAO
     */
    @Deprecated
    public boolean atualizarUsuario(Usuario usuario) {
        try {
            update(usuario);
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar usuário: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * @deprecated Use delete() do BaseDAO
     */
    @Deprecated
    public boolean excluirUsuario(int id) {
        try {
            delete(id);
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao excluir usuário: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * @deprecated Use findAll() do BaseDAO
     */
    @Deprecated
    public List<Usuario> listarUsuarios() {
        try {
            return findAll();
        } catch (SQLException e) {
            System.err.println("Erro ao listar usuários: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * @deprecated Use findAllIncludingInactive()
     */
    @Deprecated
    public List<Usuario> listarTodosUsuarios() {
        try {
            return findAllIncludingInactive();
        } catch (SQLException e) {
            System.err.println("Erro ao listar todos os usuários: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * @deprecated Use findById() do BaseDAO
     */
    @Deprecated
    public Usuario buscarUsuarioPorId(int id) {
        try {
            return findById(id);
        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuário por ID: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * @deprecated Use buscarPorLogin()
     */
    @Deprecated
    public Usuario buscarUsuarioPorLogin(String login) {
        try {
            return buscarPorLogin(login);
        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuário por login: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * @deprecated Use buscarPorEmail()
     */
    @Deprecated
    public Usuario buscarUsuarioPorEmail(String email) {
        try {
            return buscarPorEmail(email);
        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuário por email: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * @deprecated Use buscarPorFiltro()
     */
    @Deprecated
    public List<Usuario> buscarUsuariosPorFiltro(String filtro) {
        try {
            return buscarPorFiltro(filtro);
        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuários por filtro: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * @deprecated Use buscarPorPerfil()
     */
    @Deprecated
    public List<Usuario> buscarUsuariosPorPerfil(PerfilUsuario perfil) {
        try {
            return buscarPorPerfil(perfil);
        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuários por perfil: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * @deprecated Use buscarPorSetor()
     */
    @Deprecated
    public List<Usuario> buscarUsuariosPorSetor(int idSetor) {
        try {
            return buscarPorSetor(idSetor);
        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuários por setor: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
