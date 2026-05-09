package com.inventario.sihcp.offline;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.inventario.sihcp.config.DatabaseConfig;
import com.inventario.sihcp.util.DatabaseConnection;

/**
 * Gerenciador de conectividade para o modo offline
 * Monitora a conexão com a internet e notifica listeners sobre mudanças
 * 
 * @author Sistema de Inventário IFMT
 * @version 1.0.0
 */
public class ConnectivityManager {
    
    private static final Logger LOGGER = Logger.getLogger(ConnectivityManager.class.getName());
    private static ConnectivityManager instance;
    
    private boolean isOnline;
    private boolean isMonitoring;
    private final List<ConnectivityListener> listeners;
    private ScheduledExecutorService scheduler;
    
    // Configurações de monitoramento
    private static final int CHECK_INTERVAL_SECONDS = 10;
    private static final String[] TEST_HOSTS = {
        "8.8.8.8",      // Google DNS
        "1.1.1.1",      // Cloudflare DNS
        "208.67.222.222" // OpenDNS
    };
    private static final int TIMEOUT_MS = 5000;
    
    private ConnectivityManager() {
        this.listeners = new ArrayList<>();
        this.isOnline = false;
        this.isMonitoring = false;
        
        // Verificação inicial
        this.isOnline = checkConnection();
    }
    
    /**
     * Obtém a instância singleton do ConnectivityManager
     * @return Instância do ConnectivityManager
     */
    public static synchronized ConnectivityManager getInstance() {
        if (instance == null) {
            instance = new ConnectivityManager();
        }
        return instance;
    }
    
    /**
     * Verifica se há conexão com a internet
     * @return true se há conexão, false caso contrário
     */
    public boolean isOnline() {
        return isOnline;
    }
    
    /**
     * Verifica a conectividade testando múltiplos hosts
     * @return true se conseguir conectar a pelo menos um host
     */
    public boolean checkConnection() {
        // Primeiro testa conectividade com o servidor do banco
        if (checkDatabaseServerConnection()) {
            LOGGER.info("Conexão detectada com servidor do banco de dados");
            return true;
        }
        
        // Se falhar, testa conectividade geral com a internet
        for (String host : TEST_HOSTS) {
            if (testHost(host)) {
                LOGGER.info("Conexão detectada via host: " + host);
                return true;
            }
        }
        
        LOGGER.warning("Nenhuma conexão detectada");
        return false;
    }
    
    /**
     * Testa conectividade com um host específico
     * @param host Host para testar
     * @return true se conseguir conectar
     */
    private boolean testHost(String host) {
        try {
            InetAddress address = InetAddress.getByName(host);
            return address.isReachable(TIMEOUT_MS);
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Falha ao testar host " + host, e);
            return false;
        }
    }
    
    /**
     * Testa conectividade específica com o servidor do banco de dados
     * @return true se conseguir conectar com o servidor do banco
     */
    private boolean checkDatabaseServerConnection() {
        try {
            DatabaseConfig config = DatabaseConnection.getCurrentConfig();
            if (config != null && config.isValid()) {
                String dbHost = config.getHost();
                
                // Se for localhost, não precisa testar conectividade de rede
                if ("localhost".equals(dbHost) || "127.0.0.1".equals(dbHost)) {
                    // Testa se o banco está realmente acessível
                    return DatabaseConnection.testConnection(config);
                }
                
                // Para hosts remotos, testa conectividade de rede primeiro
                if (testHost(dbHost)) {
                    // Se há conectividade de rede, testa conexão com o banco
                    return DatabaseConnection.testConnection(config);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Falha ao testar conectividade com servidor do banco", e);
        }
        
        return false;
    }
    
    /**
     * Inicia o monitoramento automático de conectividade
     */
    public synchronized void startMonitoring() {
        if (isMonitoring) {
            LOGGER.info("Monitoramento já está ativo");
            return;
        }
        
        LOGGER.info("Iniciando monitoramento de conectividade");
        
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ConnectivityMonitor");
            t.setDaemon(true);
            return t;
        });
        
        scheduler.scheduleAtFixedRate(this::checkAndNotify, 
                                    0, 
                                    CHECK_INTERVAL_SECONDS, 
                                    TimeUnit.SECONDS);
        
        isMonitoring = true;
    }
    
    /**
     * Para o monitoramento de conectividade
     */
    public synchronized void stopMonitoring() {
        if (!isMonitoring) {
            return;
        }
        
        LOGGER.info("Parando monitoramento de conectividade");
        
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        isMonitoring = false;
    }
    
    /**
     * Verifica conectividade e notifica listeners sobre mudanças
     */
    private void checkAndNotify() {
        try {
            boolean currentStatus = checkConnection();
            
            if (currentStatus != isOnline) {
                boolean wasOnline = isOnline;
                isOnline = currentStatus;
                
                LOGGER.info("Status de conectividade alterado: " + 
                           (isOnline ? "ONLINE" : "OFFLINE"));
                
                // Notifica listeners
                if (isOnline && !wasOnline) {
                    notifyConnectionEstablished();
                } else if (!isOnline && wasOnline) {
                    notifyConnectionLost();
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro durante verificação de conectividade", e);
        }
    }
    
    /**
     * Adiciona um listener para mudanças de conectividade
     * @param listener Listener a ser adicionado
     */
    public synchronized void addListener(ConnectivityListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            LOGGER.fine("Listener adicionado: " + listener.getClass().getSimpleName());
        }
    }
    
    /**
     * Remove um listener
     * @param listener Listener a ser removido
     */
    public synchronized void removeListener(ConnectivityListener listener) {
        if (listeners.remove(listener)) {
            LOGGER.fine("Listener removido: " + listener.getClass().getSimpleName());
        }
    }
    
    /**
     * Notifica todos os listeners sobre conexão estabelecida
     */
    private void notifyConnectionEstablished() {
        List<ConnectivityListener> currentListeners;
        synchronized (this) {
            currentListeners = new ArrayList<>(listeners);
        }
        
        for (ConnectivityListener listener : currentListeners) {
            try {
                listener.onConnectionEstablished();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Erro ao notificar listener sobre conexão estabelecida", e);
            }
        }
    }
    
    /**
     * Notifica todos os listeners sobre perda de conexão
     */
    private void notifyConnectionLost() {
        List<ConnectivityListener> currentListeners;
        synchronized (this) {
            currentListeners = new ArrayList<>(listeners);
        }
        
        for (ConnectivityListener listener : currentListeners) {
            try {
                listener.onConnectionLost();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Erro ao notificar listener sobre perda de conexão", e);
            }
        }
    }
    
    /**
     * Força uma verificação imediata de conectividade
     * @return Status atual da conexão
     */
    public boolean forceCheck() {
        LOGGER.info("Verificação forçada de conectividade");
        checkAndNotify();
        return isOnline;
    }
    
    /**
     * Marca o sistema como offline manualmente
     * Útil quando uma operação falha por erro de conexão
     */
    public void markOffline() {
        if (isOnline) {
            LOGGER.warning("Marcando sistema como OFFLINE devido a falha de conexão");
            isOnline = false;
            notifyConnectionLost();
        }
    }
    
    /**
     * Marca o sistema como online manualmente
     * Útil após reconexão bem-sucedida
     */
    public void markOnline() {
        if (!isOnline) {
            LOGGER.info("Marcando sistema como ONLINE");
            isOnline = true;
            notifyConnectionEstablished();
        }
    }
    
    /**
     * Testa especificamente a conectividade com o servidor do banco de dados
     * @return true se conseguir conectar com o servidor do banco
     */
    public boolean checkDatabaseConnection() {
        LOGGER.info("Verificando conectividade específica com servidor do banco");
        return checkDatabaseServerConnection();
    }
    
    /**
     * Obtém estatísticas do monitoramento
     * @return String com informações de status
     */
    public String getStatusInfo() {
        return String.format(
            "Status: %s | Monitoramento: %s | Listeners: %d",
            isOnline ? "ONLINE" : "OFFLINE",
            isMonitoring ? "ATIVO" : "INATIVO",
            listeners.size()
        );
    }
    
    /**
     * Cleanup ao finalizar a aplicação
     */
    public void shutdown() {
        LOGGER.info("Finalizando ConnectivityManager");
        stopMonitoring();
        synchronized (this) {
            listeners.clear();
        }
    }
}