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
    private OfflineAuthService offlineAuthService; // Não final - será criado sob demanda
    private OfflineManager offlineManager; // Não final - será obtido sob demanda
    
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
        this.offlineAuthService = null; // Será criado sob demanda
        this.offlineManager = null; // Será obtido sob demanda
        
        // NÃO obter OfflineManager aqui!
        // Será obtido apenas quando necessário (modo offline)
        LOGGER.info("UnifiedAuthService criado - Todos os componentes offline serão inicializados sob demanda");
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
        LOGGER.info("DEBUG: Modo atual antes de autenticar: " + currentMode);
        
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
                // Autenticação online falhou - credenciais incorretas
                // Tentar offline como fallback (usuário pode ter sido sincronizado antes)
                LOGGER.info("Autenticação online falhou: credenciais incorretas");
                return tentarFallbackOffline(login, senha, "Usuário ou senha incorretos");
            }
            
        } catch (Exception e) {
            // Erro na autenticação online (conexão, timeout, etc.)
            // Tentar fallback para modo offline
            LOGGER.log(Level.WARNING, "Erro na autenticação online - tentando fallback offline", e);
            return tentarFallbackOffline(login, senha, "Erro ao conectar com o servidor: " + e.getMessage());
        }
    }
    
    /**
     * Tenta autenticação offline como fallback
     * @param login Login do usuário
     * @param senha Senha
     * @param mensagemErroOriginal Mensagem de erro original (caso offline também falhe)
     * @return Resultado da autenticação
     */
    private AuthResult tentarFallbackOffline(String login, String senha, String mensagemErroOriginal) {
        try {
            LOGGER.info("Tentando fallback para autenticação offline: " + login);
            
            // Criar OfflineAuthService se ainda não foi criado
            ensureOfflineAuthServiceCreated();
            
            // Verificar se existem usuários no banco offline
            if (!offlineAuthService.existemUsuarios()) {
                LOGGER.info("Nenhum usuário no banco offline - fallback não disponível");
                return AuthResult.failure(mensagemErroOriginal + "\n(Modo offline não disponível - nenhum usuário sincronizado)");
            }
            
            // Tentar autenticação offline
            Usuario usuario = offlineAuthService.autenticarOffline(login, senha);
            
            if (usuario != null) {
                LOGGER.info("Autenticação offline bem-sucedida (fallback): " + login);
                
                // Forçar modo offline já que online não está disponível
                ensureOfflineManagerObtained();
                if (offlineManager != null) {
                    offlineManager.forceOfflineMode();
                }
                
                return AuthResult.success(usuario, true);
            } else {
                LOGGER.info("Autenticação offline também falhou: credenciais incorretas");
                return AuthResult.failure("Usuário ou senha incorretos");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro no fallback offline", e);
            return AuthResult.failure(mensagemErroOriginal);
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
            // Criar OfflineAuthService se ainda não foi criado
            ensureOfflineAuthServiceCreated();
            
            // Inicializar OfflineManager se ainda não foi inicializado
            ensureOfflineManagerInitialized();
            
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
            // Criar OfflineAuthService se ainda não foi criado
            ensureOfflineAuthServiceCreated();
            
            boolean sincronizado = offlineAuthService.sincronizarUsuario(usuario);
            if (sincronizado) {
                LOGGER.info("Usuário sincronizado para banco offline: " + usuario.getLogin());
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao sincronizar usuário para offline", e);
        }
    }
    
    /**
     * Garante que o OfflineAuthService está criado
     * Inicialização lazy - só cria quando realmente necessário
     */
    private void ensureOfflineAuthServiceCreated() {
        if (offlineAuthService == null) {
            LOGGER.info("Criando OfflineAuthService sob demanda...");
            offlineAuthService = new OfflineAuthService();
            LOGGER.info("OfflineAuthService criado com sucesso");
        }
    }
    
    /**
     * Garante que o OfflineManager está obtido
     * Inicialização lazy - só obtém quando realmente necessário
     */
    private void ensureOfflineManagerObtained() {
        if (offlineManager == null) {
            LOGGER.info("Obtendo OfflineManager sob demanda...");
            offlineManager = OfflineManager.getInstance();
            LOGGER.info("OfflineManager obtido com sucesso");
        }
    }
    
    /**
     * Garante que o OfflineManager está inicializado
     * Inicialização lazy - só inicializa quando realmente necessário
     */
    private void ensureOfflineManagerInitialized() {
        try {
            // Primeiro garantir que foi obtido
            ensureOfflineManagerObtained();
            
            OfflineManager.OfflineState state = offlineManager.getCurrentState();
            
            // Se está inicializando ou em erro, tentar inicializar
            if (state == OfflineManager.OfflineState.INITIALIZING || state == OfflineManager.OfflineState.ERROR) {
                LOGGER.info("Inicializando OfflineManager sob demanda (estado atual: " + state + ")...");
                offlineManager.initialize();
                LOGGER.info("OfflineManager inicializado com sucesso");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao inicializar OfflineManager", e);
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
            ensureOfflineManagerInitialized(); // Garantir que está inicializado
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
        // Caso contrário, verificar o estado real do OfflineManager (se existir)
        if (offlineManager == null) {
            return false; // Se não foi criado ainda, não está offline
        }
        return offlineManager.isOperatingOffline();
    }
    
    /**
     * Força o sistema para modo offline
     */
    public void forceOfflineMode() {
        ensureOfflineManagerObtained();
        offlineManager.forceOfflineMode();
        setAuthMode(AuthMode.OFFLINE_ONLY);
        LOGGER.info("Sistema forçado para modo offline");
    }
    
    /**
     * Tenta reconectar ao modo online
     * @return true se conseguiu reconectar
     */
    public boolean tryReconnect() {
        ensureOfflineManagerObtained();
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
        
        // Criar no banco offline (NÃO criar automaticamente - apenas se offline estiver ativo)
        // O admin offline será criado quando o usuário importar dados ou forçar modo offline
        LOGGER.info("Admin offline será criado quando necessário (importação ou modo offline)");
    }
    
    /**
     * Verifica se existem usuários no banco offline
     * @return true se existem usuários
     */
    public boolean existemUsuariosOffline() {
        try {
            // Criar OfflineAuthService se ainda não foi criado
            ensureOfflineAuthServiceCreated();
            return offlineAuthService.existemUsuarios();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao verificar usuários offline", e);
            return false;
        }
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
