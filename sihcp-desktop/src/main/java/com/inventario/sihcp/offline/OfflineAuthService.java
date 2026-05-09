package com.inventario.sihcp.offline;

import com.inventario.sihcp.model.Usuario;
import com.inventario.sihcp.model.PerfilUsuario;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Serviço de autenticação offline
 * Gerencia login e usuários no banco SQLite local
 * 
 * @author Sistema de Inventário IFMT
 * @version 1.0.0
 */
public class OfflineAuthService {
    
    private static final Logger LOGGER = Logger.getLogger(OfflineAuthService.class.getName());
    private final SQLiteConnection sqliteConnection;
    private static final int MAX_TENTATIVAS_LOGIN = 5;
    
    public OfflineAuthService() {
        this.sqliteConnection = SQLiteConnection.getInstance();
    }
    
    /**
     * Autentica usuário no modo offline
     * @param login Login do usuário
     * @param senha Senha em texto plano
     * @return Usuario autenticado ou null se falhou
     */
    public Usuario autenticarOffline(String login, String senha) {
        if (login == null || senha == null || login.trim().isEmpty() || senha.trim().isEmpty()) {
            return null;
        }
        
        try (Connection conn = sqliteConnection.getConnection()) {
            
            // Buscar usuário no banco local
            Usuario usuario = buscarUsuarioPorLogin(conn, login.trim());
            
            if (usuario == null) {
                LOGGER.log(Level.INFO, "Usuário não encontrado no banco offline: {0}", login);
                return null;
            }
            
            // Verificar se o usuário está bloqueado
            if (Boolean.TRUE.equals(usuario.getBloqueado())) {
                LOGGER.log(Level.INFO, "Usuário bloqueado: {0}", login);
                return null;
            }
            
            // Verificar se o usuário está ativo
            if (!Boolean.TRUE.equals(usuario.getAtivo())) {
                LOGGER.log(Level.INFO, "Usuário inativo: {0}", login);
                return null;
            }
            
            // Verificar a senha
            boolean senhaCorreta = verificarSenha(senha, usuario.getSenhaHash());
            
            if (!senhaCorreta) {
                // Incrementar tentativas de login
                incrementarTentativasLogin(conn, usuario);
                return null;
            }
            
            // Autenticação bem-sucedida
            atualizarUltimoAcesso(conn, usuario);
            
            LOGGER.log(Level.INFO, "Usuário autenticado com sucesso no modo offline: {0}", login);
            return usuario;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao autenticar usuário offline", e);
            return null;
        }
    }
    
    /**
     * Busca usuário por login no banco local
     * @param conn Conexão com o banco
     * @param login Login do usuário
     * @return Usuario encontrado ou null
     * @throws SQLException
     */
    private Usuario buscarUsuarioPorLogin(Connection conn, String login) throws SQLException {
        String sql = "SELECT * FROM local_usuario WHERE login = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUsuario(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Mapeia ResultSet para objeto Usuario
     * @param rs ResultSet
     * @return Usuario
     * @throws SQLException
     */
    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id"));
        usuario.setLogin(rs.getString("login"));
        usuario.setSenhaHash(rs.getString("senha_hash"));
        usuario.setNomeCompleto(rs.getString("nome_completo"));
        usuario.setEmail(rs.getString("email"));
        usuario.setMatricula(rs.getString("matricula"));
        
        String perfilStr = rs.getString("perfil");
        if (perfilStr != null) {
            usuario.setPerfil(PerfilUsuario.valueOf(perfilStr));
        }
        
        usuario.setAtivo(rs.getBoolean("ativo"));
        usuario.setBloqueado(rs.getBoolean("bloqueado"));
        usuario.setTentativasLogin(rs.getInt("tentativas_login"));
        usuario.setPrimeiroAcesso(rs.getBoolean("primeiro_acesso"));
        
        // ✅ CORRIGIDO: Usar método seguro para ler timestamp do SQLite
        Timestamp dataUltimoAcesso = com.inventario.sihcp.util.DateFormatUtils.getTimestampSafe(rs, "data_ultimo_acesso");
        if (dataUltimoAcesso != null) {
            usuario.setDataUltimoAcesso(dataUltimoAcesso.toLocalDateTime());
        }
        
        return usuario;
    }
    
    /**
     * Verifica se a senha está correta
     * @param senhaDigitada Senha digitada pelo usuário
     * @param senhaHash Hash armazenado no banco
     * @return true se a senha está correta
     */
    private boolean verificarSenha(String senhaDigitada, String senhaHash) {
        try {
            // Tentar verificação com hash seguro primeiro
            boolean senhaCorreta = com.inventario.sihcp.util.PasswordUtil.verifyPassword(senhaDigitada, senhaHash);
            
            // Se falhar, tentar verificação simples (compatibilidade)
            if (!senhaCorreta) {
                senhaCorreta = com.inventario.sihcp.util.PasswordUtil.verifyPasswordSimple(senhaDigitada, senhaHash);
            }
            
            // Se ainda falhar, tentar comparação direta (compatibilidade total)
            if (!senhaCorreta) {
                senhaCorreta = senhaDigitada.equals(senhaHash);
            }
            
            return senhaCorreta;
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro na verificação de senha", e);
            // Fallback para comparação direta
            return senhaDigitada.equals(senhaHash);
        }
    }
    
    /**
     * Incrementa tentativas de login e bloqueia se necessário
     * @param conn Conexão com o banco
     * @param usuario Usuario
     * @throws SQLException
     */
    private void incrementarTentativasLogin(Connection conn, Usuario usuario) throws SQLException {
        int tentativas = (usuario.getTentativasLogin() != null ? usuario.getTentativasLogin() : 0) + 1;
        
        String sql;
        if (tentativas >= MAX_TENTATIVAS_LOGIN) {
            // Bloquear usuário
            sql = "UPDATE local_usuario SET tentativas_login = ?, bloqueado = TRUE WHERE id = ?";
            LOGGER.log(Level.WARNING, "Usuário bloqueado por excesso de tentativas: {0}", usuario.getLogin());
        } else {
            sql = "UPDATE local_usuario SET tentativas_login = ? WHERE id = ?";
            LOGGER.log(Level.INFO, "Senha incorreta para usuário: {0} (Tentativa {1}/{2})", 
                       new Object[]{usuario.getLogin(), tentativas, MAX_TENTATIVAS_LOGIN});
        }
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tentativas);
            stmt.setInt(2, usuario.getId());
            stmt.executeUpdate();
        }
    }
    
    /**
     * Atualiza data do último acesso e reseta tentativas
     * @param conn Conexão com o banco
     * @param usuario Usuario
     * @throws SQLException
     */
    private void atualizarUltimoAcesso(Connection conn, Usuario usuario) throws SQLException {
        String sql = """
            UPDATE local_usuario 
            SET tentativas_login = 0, 
                data_ultimo_acesso = ?, 
                primeiro_acesso = FALSE 
            WHERE id = ?
        """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, usuario.getId());
            stmt.executeUpdate();
        }
    }
    
    /**
     * Sincroniza usuário do banco online para o offline
     * @param usuario Usuario do banco online
     * @return true se sincronizado com sucesso
     */
    public boolean sincronizarUsuario(Usuario usuario) {
        if (usuario == null) {
            return false;
        }
        
        try (Connection conn = sqliteConnection.getConnection()) {
            
            // Verificar se usuário já existe
            Usuario usuarioExistente = buscarUsuarioPorLogin(conn, usuario.getLogin());
            
            if (usuarioExistente != null) {
                // Atualizar usuário existente
                return atualizarUsuario(conn, usuario);
            } else {
                // Inserir novo usuário
                return inserirUsuario(conn, usuario);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao sincronizar usuário", e);
            return false;
        }
    }
    
    /**
     * Insere novo usuário no banco local
     * @param conn Conexão com o banco
     * @param usuario Usuario
     * @return true se inserido com sucesso
     * @throws SQLException
     */
    private boolean inserirUsuario(Connection conn, Usuario usuario) throws SQLException {
        String sql = """
            INSERT INTO local_usuario 
            (id, login, senha_hash, nome_completo, email, matricula, perfil, 
             ativo, bloqueado, tentativas_login, primeiro_acesso, data_ultimo_acesso, sync_status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setUsuarioParameters(stmt, usuario);
            stmt.setString(13, "SYNCED");
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Usuário inserido no banco offline: {0}", usuario.getLogin());
                return true;
            }
            
            return false;
        }
    }
    
    /**
     * Atualiza usuário existente no banco local
     * @param conn Conexão com o banco
     * @param usuario Usuario
     * @return true se atualizado com sucesso
     * @throws SQLException
     */
    private boolean atualizarUsuario(Connection conn, Usuario usuario) throws SQLException {
        String sql = """
            UPDATE local_usuario SET 
            senha_hash = ?, nome_completo = ?, email = ?, matricula = ?, perfil = ?, 
            ativo = ?, bloqueado = ?, tentativas_login = ?, primeiro_acesso = ?, 
            data_ultimo_acesso = ?, sync_status = ?, last_modified = CURRENT_TIMESTAMP
            WHERE id = ?
        """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario.getSenhaHash());
            stmt.setString(2, usuario.getNomeCompleto());
            stmt.setString(3, usuario.getEmail());
            stmt.setString(4, usuario.getMatricula());
            stmt.setString(5, usuario.getPerfil() != null ? usuario.getPerfil().name() : null);
            stmt.setBoolean(6, Boolean.TRUE.equals(usuario.getAtivo()));
            stmt.setBoolean(7, Boolean.TRUE.equals(usuario.getBloqueado()));
            stmt.setInt(8, usuario.getTentativasLogin() != null ? usuario.getTentativasLogin() : 0);
            stmt.setBoolean(9, Boolean.TRUE.equals(usuario.getPrimeiroAcesso()));
            
            if (usuario.getDataUltimoAcesso() != null) {
                stmt.setTimestamp(10, Timestamp.valueOf(usuario.getDataUltimoAcesso()));
            } else {
                stmt.setNull(10, Types.TIMESTAMP);
            }
            
            stmt.setString(11, "SYNCED");
            stmt.setInt(12, usuario.getId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Usuário atualizado no banco offline: {0}", usuario.getLogin());
                return true;
            }
            
            return false;
        }
    }
    
    /**
     * Define parâmetros do usuário no PreparedStatement
     * @param stmt PreparedStatement
     * @param usuario Usuario
     * @throws SQLException
     */
    private void setUsuarioParameters(PreparedStatement stmt, Usuario usuario) throws SQLException {
        stmt.setInt(1, usuario.getId());
        stmt.setString(2, usuario.getLogin());
        stmt.setString(3, usuario.getSenhaHash());
        stmt.setString(4, usuario.getNomeCompleto());
        stmt.setString(5, usuario.getEmail());
        stmt.setString(6, usuario.getMatricula());
        stmt.setString(7, usuario.getPerfil() != null ? usuario.getPerfil().name() : null);
        stmt.setBoolean(8, Boolean.TRUE.equals(usuario.getAtivo()));
        stmt.setBoolean(9, Boolean.TRUE.equals(usuario.getBloqueado()));
        stmt.setInt(10, usuario.getTentativasLogin() != null ? usuario.getTentativasLogin() : 0);
        stmt.setBoolean(11, Boolean.TRUE.equals(usuario.getPrimeiroAcesso()));
        
        if (usuario.getDataUltimoAcesso() != null) {
            stmt.setTimestamp(12, Timestamp.valueOf(usuario.getDataUltimoAcesso()));
        } else {
            stmt.setNull(12, Types.TIMESTAMP);
        }
    }
    
    /**
     * Desbloqueia um usuário no banco local
     * @param usuarioId ID do usuário
     * @return true se desbloqueado com sucesso
     */
    public boolean desbloquearUsuario(int usuarioId) {
        String sql = "UPDATE local_usuario SET bloqueado = FALSE, tentativas_login = 0 WHERE id = ?";
        
        try (Connection conn = sqliteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, usuarioId);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Usuário desbloqueado no banco offline: {0}", usuarioId);
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao desbloquear usuário offline", e);
            return false;
        }
    }
    
    /**
     * Verifica se existe algum usuário no banco local
     * @return true se existe pelo menos um usuário
     */
    public boolean existemUsuarios() {
        String sql = "SELECT COUNT(*) FROM local_usuario";
        
        try (Connection conn = sqliteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
            return false;
            
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Erro ao verificar existência de usuários", e);
            return false;
        }
    }
    
    /**
     * Cria usuário administrador padrão no banco offline
     * @return true se criado com sucesso
     */
    public boolean criarUsuarioAdminPadrao() {
        try (Connection conn = sqliteConnection.getConnection()) {
            
            // Verificar se admin já existe
            Usuario admin = buscarUsuarioPorLogin(conn, "admin");
            
            if (admin == null) {
                admin = new Usuario();
                admin.setId(1); // ID fixo para admin
                admin.setLogin("admin");
                admin.setSenhaHash(com.inventario.sihcp.util.PasswordUtil.hashPassword("admin123"));
                admin.setNomeCompleto("Administrador do Sistema");
                admin.setEmail("admin@ifmt.edu.br");
                admin.setMatricula("0000000");
                admin.setPerfil(PerfilUsuario.ADMIN);
                admin.setAtivo(true);
                admin.setBloqueado(false);
                admin.setPrimeiroAcesso(true);
                admin.setTentativasLogin(0);
                
                boolean inserido = inserirUsuario(conn, admin);
                
                if (inserido) {
                    LOGGER.info("Usuário administrador padrão criado no banco offline");
                    return true;
                }
            }
            
            return false;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao criar usuário administrador padrão offline", e);
            return false;
        }
    }
}
