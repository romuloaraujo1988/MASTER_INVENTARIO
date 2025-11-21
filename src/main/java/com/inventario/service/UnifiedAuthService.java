package com.inventario.service;

import com.inventario.model.Usuario;
import com.inventario.offline.OfflineAuthService;
import com.inventario.offline.OfflineManager;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Serviço unificado de autenticação
 * Gerencia login online e offline de forma transparente
 * 
 * @author Sistema de Inventário IFMT
 * @version 1.0.0
 */
public class UnifiedAuthService {
    
    private static final Logger LOGGER = Logger.getLogger(UnifiedAuthService.class.getName());
    
    private final AutenticacaoServiceDB onlineAuthService;
    private final OfflineAuthService offlineAuthService;
    private final OfflineManager offlineManager;
    
    // Modo de operação
    private AuthMode currentMode = AuthMode.AUTO;
    
    /**
     * Modos de autenticação
     */
    public enum AuthMode {
        AUTO,           // Tenta online primeiro, fallback para offline
        ONLINE_ONLY,    // Apenas online
        OFFLINE_ONLY    // Apenas offline
    }
    
    /**
     * Resultado da autenticação
     */
    public static class AuthResult {
        public final boolean success;
        public final Usuario usuario;
        public final String message;
        public final boolean wasOffline;
        
        public AuthResult(boolean success, Usuario usuario, String message, boolean wasOffline) {
            this.success = success;
            this.usuario = usuario;
            this.message = message;
            this.wasOffline = wasOffline;
        }
        
        public static AuthResult success(Usuario usuario, boolean wasOffline) {
            return new AuthResult(true, usuario, "Autenticação bem-sucedida", wasOffline);
        }
        
        public static AuthResult failure(String message) {
            return new AuthResult(false, null, message, false);
        }
    }
    
    public UnifiedAuthService() {
        this.onlineAuthService = new AutenticacaoServiceDB();
        this.offlineAuthService = new OfflineAuthService();
        this.offlineManager = OfflineManager.getInstance();
        
        // Inicializar sistema offline se necessário
        try {
            if (!offlineManager.getCurrentState().equals(OfflineManager.OfflineState.ONLINE) &&
                !offlineManager.getCurrentState().equals(OfflineManager.OfflineState.OFFLINE)) {
                offlineManager.initialize();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao inicializar sistema offline", e);
        }
    }
    
    /**
     * Autentica usuário de forma unificada
     * @param login Login do usuário
     * @param senha Senha em texto plano
     * @return Resultado da autenticação
     */
    public AuthResult autenticar(String login, String senha) {
        if (login == null || senha == null || login.trim().isEmpty() || senha.trim().isEmpty()) {
            return AuthResult.failure("Login e senha são obrigatórios");
        }
        
        switch (currentMode) {
            case ONLINE_ONLY:
                return autenticarOnline(login, senha);
                
            case OFFLINE_ONLY:
                return autenticarOffline(login, senha);
                
            case AUTO:
            default:
                return autenticarAuto(login, senha);
        }
    }
    
    /**
     * Autenticação automática (tenta online, fallback para offline)
     * @param login Login do usuário
     * @param senha Senha
     * @return Resultado da autenticação
     */
    private AuthResult autenticarAuto(String login, String senha) {
        // Verificar estado do sistema offline
        OfflineManager.OfflineState state = offlineManager.getCurrentState();
        
        // Se está offline ou modo offline forçado, usar autenticação offline
        if (state == OfflineManager.OfflineState.OFFLINE || offlineManager.isForcedOffline()) {
            LOGGER.info("Sistema em modo offline, usando autenticação local");
            return autenticarOffline(login, senha);
        }
        
        // Tentar autenticação online primeiro
        try {
            LOGGER.info("Tentando autenticação online para: " + login);
            Usuario usuario = onlineAuthService.autenticar(login, senha);
            
            if (usuario != null) {
                // Autenticação online bem-sucedida
                LOGGER.info("Autenticação online bem-sucedida: " + login);
                
                // Sincronizar usuário para o banco offline (para uso futuro)
                sincronizarUsuarioParaOffline(usuario);
                
                return AuthResult.success(usuario, false);
            } else {
                // Autenticação online falhou
                LOGGER.info("Autenticação online falhou, tentando offline: " + login);
                return autenticarOffline(login, senha);
            }
            
        } catch (Exception e) {
            // Erro na autenticação online, tentar offline
            LOGGER.log(Level.WARNING, "Erro na autenticação online, tentando offline", e);
            return autenticarOffline(login, senha);
        }
    }
    
    /**
     * Autenticação apenas online
     * @param login Login do usuário
     * @param senha Senha
     * @return Resultado da autenticação
     */
    private AuthResult autenticarOnline(String login, String senha) {
        try {
            LOGGER.info("Autenticação online para: " + login);
            Usuario usuario = onlineAuthService.autenticar(login, senha);
            
            if (usuario != null) {
                LOGGER.info("Autenticação online bem-sucedida: " + login);
                
                // Sincronizar usuário para o banco offline
                sincronizarUsuarioParaOffline(usuario);
                
                return AuthResult.success(usuario, false);
            } else {
                return AuthResult.failure("Usuário ou senha incorretos");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro na autenticação online", e);
            return AuthResult.failure("Erro ao conectar com o servidor: " + e.getMessage());
        }
    }
    
    /**
     * Autenticação apenas offline
     * @param login Login do usuário
     * @param senha Senha
     * @return Resultado da autenticação
     */
    private AuthResult autenticarOffline(String login, String senha) {
        try {
            LOGGER.info("Autenticação offline para: " + login);
            Usuario usuario = offlineAuthService.autenticarOffline(login, senha);
            
            if (usuario != null) {
                LOGGER.info("Autenticação offline bem-sucedida: " + login);
                return AuthResult.success(usuario, true);
            } else {
                return AuthResult.failure("Usuário ou senha incorretos (modo offline)");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro na autenticação offline", e);
            return AuthResult.failure("Erro na autenticação offline: " + e.getMessage());
        }
    }
    
    /**
     * Sincroniza usuário do banco online para o offline
     * @param usuario Usuario autenticado
     */
    private void sincronizarUsuarioParaOffline(Usuario usuario) {
        try {
            boolean sincronizado = offlineAuthService.sincronizarUsuario(usuario);
            if (sincronizado) {
                LOGGER.info("Usuário sincronizado para banco offline: " + usuario.getLogin());
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao sincronizar usuário para offline", e);
        }
    }
    
    /**
     * Define o modo de autenticação
     * @param mode Modo de autenticação
     */
    public void setAuthMode(AuthMode mode) {
        AuthMode previousMode = this.currentMode;
        this.currentMode = mode;
        
        LOGGER.info("Modo de autenticação alterado de " + previousMode + " para: " + mode);
        
        // Se mudou para OFFLINE_ONLY, forçar o OfflineManager
        if (mode == AuthMode.OFFLINE_ONLY && previousMode != AuthMode.OFFLINE_ONLY) {
            LOGGER.info("Forçando OfflineManager para modo offline");
            offlineManager.forceOfflineMode();
        }
        // Se saiu do OFFLINE_ONLY, tentar reconectar
        else if (previousMode == AuthMode.OFFLINE_ONLY && mode != AuthMode.OFFLINE_ONLY) {
            LOGGER.info("Saindo do modo offline forçado, tentando reconectar");
            offlineManager.tryReconnect();
        }
    }
    
    /**
     * Obtém o modo de autenticação atual
     * @return Modo atual
     */
    public AuthMode getAuthMode() {
        return currentMode;
    }
    
    /**
     * Verifica se o sistema está operando offline
     * @return true se offline
     */
    public boolean isOperatingOffline() {
        // Se o modo está forçado para OFFLINE_ONLY, retornar true
        if (currentMode == AuthMode.OFFLINE_ONLY) {
            return true;
        }
        // Caso contrário, verificar o estado real do OfflineManager
        return offlineManager.isOperatingOffline();
    }
    
    /**
     * Força o sistema para modo offline
     */
    public void forceOfflineMode() {
        offlineManager.forceOfflineMode();
        setAuthMode(AuthMode.OFFLINE_ONLY);
        LOGGER.info("Sistema forçado para modo offline");
    }
    
    /**
     * Tenta reconectar ao modo online
     * @return true se conseguiu reconectar
     */
    public boolean tryReconnect() {
        boolean reconnected = offlineManager.tryReconnect();
        if (reconnected) {
            setAuthMode(AuthMode.AUTO);
            LOGGER.info("Sistema reconectado ao modo online");
        }
        return reconnected;
    }
    
    /**
     * Cria usuário administrador padrão em ambos os bancos
     */
    public void criarUsuarioAdminPadrao() {
        // Criar no banco online
        try {
            onlineAuthService.criarUsuarioAdminPadrao();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao criar admin no banco online", e);
        }
        
        // Criar no banco offline
        try {
            offlineAuthService.criarUsuarioAdminPadrao();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao criar admin no banco offline", e);
        }
    }
    
    /**
     * Verifica se existem usuários no banco offline
     * @return true se existem usuários
     */
    public boolean existemUsuariosOffline() {
        return offlineAuthService.existemUsuarios();
    }
    
    /**
     * Obtém informações do sistema
     * @return String com informações
     */
    public String getSystemInfo() {
        StringBuilder info = new StringBuilder();
        info.append("=== SISTEMA DE AUTENTICAÇÃO ===\n");
        info.append("Modo Atual: ").append(currentMode).append("\n");
        info.append("Estado Offline: ").append(offlineManager.getCurrentState()).append("\n");
        info.append("Operando Offline: ").append(isOperatingOffline() ? "Sim" : "Não").append("\n");
        info.append("Modo Offline Forçado: ").append(offlineManager.isForcedOffline() ? "Sim" : "Não").append("\n");
        info.append("Usuários no Banco Offline: ").append(existemUsuariosOffline() ? "Sim" : "Não").append("\n");
        info.append("\n");
        info.append(offlineManager.getSystemInfo());
        
        return info.toString();
    }
}
