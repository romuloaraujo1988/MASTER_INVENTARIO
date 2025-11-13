package com.inventario.offline;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Gerenciador central do modo offline
 * Coordena conectividade, sincronização e persistência local
 * 
 * @author Sistema de Inventário IFMT
 * @version 1.0.0
 */
public class OfflineManager {
    
    private static final Logger LOGGER = Logger.getLogger(OfflineManager.class.getName());
    private static OfflineManager instance;
    
    private final ConnectivityManager connectivityManager;
    private final SQLiteConnection sqliteConnection;
    private final OfflineDAO offlineDAO;
    private final DataSynchronizer dataSynchronizer;
    
    private boolean offlineModeEnabled = false;
    private boolean initialized = false;
    
    // Listeners para mudanças de estado
    private final List<OfflineStateListener> stateListeners = new CopyOnWriteArrayList<>();
    
    /**
     * Estados do modo offline
     */
    public enum OfflineState {
        ONLINE,           // Conectado e sincronizado
        OFFLINE,          // Desconectado, operando localmente
        SYNCING,          // Sincronizando dados
        ERROR,            // Erro no sistema offline
        INITIALIZING      // Inicializando sistema offline
    }
    
    private OfflineState currentState = OfflineState.INITIALIZING;
    
    private OfflineManager() {
        this.connectivityManager = ConnectivityManager.getInstance();
        this.sqliteConnection = SQLiteConnection.getInstance();
        this.offlineDAO = new OfflineDAO();
        this.dataSynchronizer = new DataSynchronizer();
        
        setupConnectivityListener();
    }
    
    /**
     * Obtém a instância singleton
     * @return Instância do OfflineManager
     */
    public static synchronized OfflineManager getInstance() {
        if (instance == null) {
            instance = new OfflineManager();
        }
        return instance;
    }
    
    // ==================== INICIALIZAÇÃO ====================
    
    /**
     * Inicializa o sistema offline
     * @throws SQLException
     */
    public void initialize() throws SQLException {
        if (initialized) {
            LOGGER.info("OfflineManager já inicializado");
            return;
        }
        
        LOGGER.info("Inicializando sistema offline");
        setState(OfflineState.INITIALIZING);
        
        try {
            // Inicializa banco SQLite
            sqliteConnection.initializeDatabase();
            
            // Testa conexão local
            if (!sqliteConnection.testConnection()) {
                throw new SQLException("Falha ao conectar com banco SQLite");
            }
            
            // Carrega configurações
            loadOfflineConfiguration();
            
            // Inicia monitoramento de conectividade
            connectivityManager.startMonitoring();
            
            // Define estado inicial baseado na conectividade
            // Primeiro verifica conectividade com o banco de dados
            boolean dbConnected = checkDatabaseConnectivity();
            boolean networkOnline = connectivityManager.isOnline();
            
            if (dbConnected && networkOnline) {
                setState(OfflineState.ONLINE);
                if (offlineModeEnabled) {
                    dataSynchronizer.startAutoSync();
                }
                LOGGER.info("Sistema iniciado em modo ONLINE - banco e rede conectados");
            } else if (dbConnected && !networkOnline) {
                // Banco conectado mas sem rede - ainda consideramos online para operações locais
                setState(OfflineState.ONLINE);
                LOGGER.info("Sistema iniciado em modo ONLINE - banco conectado (rede limitada)");
            } else {
                setState(OfflineState.OFFLINE);
                LOGGER.info("Sistema iniciado em modo OFFLINE - sem conectividade com banco");
            }
            
            initialized = true;
            LOGGER.info("Sistema offline inicializado com sucesso");
            
        } catch (Exception e) {
            setState(OfflineState.ERROR);
            LOGGER.log(Level.SEVERE, "Erro ao inicializar sistema offline", e);
            throw e;
        }
    }
    
    /**
     * Carrega configurações do modo offline
     */
    private void loadOfflineConfiguration() {
        try {
            // Carrega configurações do banco ou arquivo
            String autoSyncEnabled = offlineDAO.obterMetadado("auto_sync_enabled");
            if ("true".equals(autoSyncEnabled)) {
                enableOfflineMode();
            }
            
            String syncInterval = offlineDAO.obterMetadado("sync_interval_minutes");
            if (syncInterval != null) {
                dataSynchronizer.setSyncInterval(Integer.parseInt(syncInterval));
            }
            
            String conflictStrategy = offlineDAO.obterMetadado("conflict_resolution_strategy");
            if (conflictStrategy != null) {
                DataSynchronizer.ConflictResolution strategy = 
                    DataSynchronizer.ConflictResolution.valueOf(conflictStrategy);
                dataSynchronizer.setConflictResolution(strategy);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao carregar configurações offline", e);
        }
    }
    
    /**
     * Configura listener de conectividade
     */
    private void setupConnectivityListener() {
        connectivityManager.addListener(new ConnectivityListener() {
            @Override
            public void onConnectionEstablished() {
                handleConnectionEstablished();
            }
            
            @Override
            public void onConnectionLost() {
                handleConnectionLost();
            }
        });
    }
    
    // ==================== GERENCIAMENTO DE ESTADO ====================
    
    /**
     * Manipula estabelecimento de conexão
     */
    private void handleConnectionEstablished() {
        LOGGER.info("Conexão estabelecida - mudando para modo online");
        
        if (currentState == OfflineState.OFFLINE) {
            setState(OfflineState.SYNCING);
            
            // Executa sincronização em thread separada
            new Thread(() -> {
                try {
                    DataSynchronizer.SyncResult result = dataSynchronizer.executarSincronizacaoManual();
                    
                    if (result.success) {
                        setState(OfflineState.ONLINE);
                        LOGGER.info("Sincronização concluída: " + result);
                    } else {
                        setState(OfflineState.ERROR);
                        LOGGER.warning("Falha na sincronização: " + result.errorMessage);
                    }
                    
                } catch (Exception e) {
                    setState(OfflineState.ERROR);
                    LOGGER.log(Level.SEVERE, "Erro durante sincronização", e);
                }
            }, "SyncThread").start();
        } else {
            setState(OfflineState.ONLINE);
        }
    }
    
    /**
     * Manipula perda de conexão
     */
    private void handleConnectionLost() {
        LOGGER.info("Conexão perdida - mudando para modo offline");
        setState(OfflineState.OFFLINE);
    }
    
    /**
     * Define o estado atual
     * @param newState Novo estado
     */
    private void setState(OfflineState newState) {
        OfflineState oldState = this.currentState;
        this.currentState = newState;
        
        LOGGER.info(String.format("Estado alterado: %s -> %s", oldState, newState));
        
        // Notifica listeners
        for (OfflineStateListener listener : stateListeners) {
            try {
                listener.onStateChanged(oldState, newState);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Erro ao notificar listener de estado", e);
            }
        }
    }
    
    // ==================== CONTROLE DO MODO OFFLINE ====================
    
    /**
     * Habilita o modo offline
     */
    public void enableOfflineMode() {
        if (!offlineModeEnabled) {
            offlineModeEnabled = true;
            
            try {
                offlineDAO.atualizarMetadado("auto_sync_enabled", "true");
                
                if (connectivityManager.isOnline()) {
                    dataSynchronizer.startAutoSync();
                }
                
                LOGGER.info("Modo offline habilitado");
                
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erro ao habilitar modo offline", e);
            }
        }
    }
    
    /**
     * Desabilita o modo offline
     */
    public void disableOfflineMode() {
        if (offlineModeEnabled) {
            offlineModeEnabled = false;
            
            try {
                offlineDAO.atualizarMetadado("auto_sync_enabled", "false");
                dataSynchronizer.stopAutoSync();
                
                LOGGER.info("Modo offline desabilitado");
                
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erro ao desabilitar modo offline", e);
            }
        }
    }
    
    /**
     * Verifica se o modo offline está habilitado
     * @return true se habilitado
     */
    public boolean isOfflineModeEnabled() {
        return offlineModeEnabled;
    }
    
    /**
     * Verifica se está operando offline
     * @return true se offline
     */
    public boolean isOperatingOffline() {
        return currentState == OfflineState.OFFLINE;
    }
    
    /**
     * Obtém o estado atual
     * @return Estado atual
     */
    public OfflineState getCurrentState() {
        return currentState;
    }
    
    /**
     * Força o sistema para modo offline manualmente
     * Ignora o status de conectividade e opera apenas localmente
     */
    public void forceOfflineMode() {
        LOGGER.info("Forçando modo offline manualmente");
        
        try {
            // Para sincronização automática se estiver ativa
            dataSynchronizer.stopAutoSync();
            
            // Define estado como offline
            setState(OfflineState.OFFLINE);
            
            // Habilita modo offline se não estiver habilitado
            if (!offlineModeEnabled) {
                enableOfflineMode();
            }
            
            // Salva configuração de modo forçado
            offlineDAO.atualizarMetadado("forced_offline_mode", "true");
            
            LOGGER.info("Sistema forçado para modo offline com sucesso");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao forçar modo offline", e);
            setState(OfflineState.ERROR);
            throw new RuntimeException("Falha ao forçar modo offline", e);
        }
    }
    
    /**
     * Tenta reconectar e voltar ao modo online
     * Executa verificação de conectividade e sincronização se possível
     * @return true se conseguiu reconectar
     */
    public boolean tryReconnect() {
        LOGGER.info("Tentando reconectar ao modo online");
        
        try {
            // Remove flag de modo forçado
            offlineDAO.atualizarMetadado("forced_offline_mode", "false");
            
            // Força verificação de conectividade
            boolean isConnected = connectivityManager.forceCheck();
            
            if (isConnected) {
                LOGGER.info("Conexão detectada, iniciando sincronização");
                
                setState(OfflineState.SYNCING);
                
                // Executa sincronização em thread separada
                new Thread(() -> {
                    try {
                        DataSynchronizer.SyncResult result = dataSynchronizer.executarSincronizacaoManual();
                        
                        if (result.success) {
                            setState(OfflineState.ONLINE);
                            
                            // Reinicia sincronização automática se habilitada
                            if (offlineModeEnabled) {
                                dataSynchronizer.startAutoSync();
                            }
                            
                            LOGGER.info("Reconexão bem-sucedida: " + result);
                        } else {
                            setState(OfflineState.OFFLINE);
                            LOGGER.warning("Falha na sincronização durante reconexão: " + result.errorMessage);
                        }
                        
                    } catch (Exception e) {
                        setState(OfflineState.OFFLINE);
                        LOGGER.log(Level.SEVERE, "Erro durante reconexão", e);
                    }
                }, "ReconnectThread").start();
                
                return true;
                
            } else {
                LOGGER.info("Conexão não disponível, mantendo modo offline");
                setState(OfflineState.OFFLINE);
                return false;
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao tentar reconectar", e);
            setState(OfflineState.ERROR);
            return false;
        }
    }
    
    /**
     * Verifica se o modo offline foi forçado manualmente
     * @return true se foi forçado
     */
    public boolean isForcedOffline() {
        try {
            String forced = offlineDAO.obterMetadado("forced_offline_mode");
            return "true".equals(forced);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao verificar modo forçado", e);
            return false;
        }
    }
    
    // ==================== OPERAÇÕES DE DADOS ====================
    
    /**
     * Salva patrimônio (online ou offline)
     * @param patrimonio Dados do patrimônio
     * @return ID gerado
     * @throws SQLException
     */
    public int salvarPatrimonio(Map<String, Object> patrimonio) throws SQLException {
        if (isOperatingOffline() || !connectivityManager.isOnline()) {
            LOGGER.info("Salvando patrimônio offline");
            return offlineDAO.salvarPatrimonio(patrimonio);
        } else {
            // Salva online e também offline se modo habilitado
            LOGGER.info("Salvando patrimônio online");
            // Implementar salvamento online
            
            if (offlineModeEnabled) {
                // Também salva offline para backup
                offlineDAO.salvarPatrimonio(patrimonio);
            }
            
            return 0; // Placeholder - implementar salvamento online
        }
    }
    
    /**
     * Salva coleta (online ou offline)
     * @param coleta Dados da coleta
     * @return ID gerado
     * @throws SQLException
     */
    public int salvarColeta(Map<String, Object> coleta) throws SQLException {
        if (isOperatingOffline() || !connectivityManager.isOnline()) {
            LOGGER.info("Salvando coleta offline");
            return offlineDAO.salvarColeta(coleta);
        } else {
            LOGGER.info("Salvando coleta online");
            // Implementar salvamento online
            
            if (offlineModeEnabled) {
                offlineDAO.salvarColeta(coleta);
            }
            
            return 0; // Placeholder
        }
    }
    
    /**
     * Lista patrimônios (online ou offline)
     * @param filtros Filtros de busca
     * @param limite Limite de registros
     * @param offset Offset para paginação
     * @return Lista de patrimônios
     * @throws SQLException
     */
    public List<Map<String, Object>> listarPatrimonios(Map<String, Object> filtros, int limite, int offset) throws SQLException {
        if (isOperatingOffline() || !connectivityManager.isOnline()) {
            LOGGER.info("Listando patrimônios offline");
            return offlineDAO.listarPatrimonios(filtros, limite, offset);
        } else {
            LOGGER.info("Listando patrimônios online");
            // Implementar listagem online
            return new ArrayList<>(); // Placeholder
        }
    }
    
    // ==================== SINCRONIZAÇÃO ====================
    
    /**
     * Executa sincronização manual
     * @return Resultado da sincronização
     */
    public DataSynchronizer.SyncResult executarSincronizacaoManual() {
        if (!connectivityManager.isOnline()) {
            DataSynchronizer.SyncResult result = new DataSynchronizer.SyncResult();
            result.success = false;
            result.errorMessage = "Sem conexão com o servidor";
            return result;
        }
        
        setState(OfflineState.SYNCING);
        
        try {
            DataSynchronizer.SyncResult result = dataSynchronizer.executarSincronizacaoManual();
            
            if (result.success) {
                setState(OfflineState.ONLINE);
            } else {
                setState(OfflineState.ERROR);
            }
            
            return result;
            
        } catch (Exception e) {
            setState(OfflineState.ERROR);
            
            DataSynchronizer.SyncResult result = new DataSynchronizer.SyncResult();
            result.success = false;
            result.errorMessage = e.getMessage();
            return result;
        }
    }
    
    /**
     * Obtém status da sincronização
     * @return Status atual
     */
    public DataSynchronizer.SyncStatus getSyncStatus() {
        return dataSynchronizer.getStatus();
    }
    
    // ==================== CONFIGURAÇÕES ====================
    
    /**
     * Define intervalo de sincronização
     * @param minutes Intervalo em minutos
     */
    public void setSyncInterval(int minutes) {
        dataSynchronizer.setSyncInterval(minutes);
        
        try {
            offlineDAO.atualizarMetadado("sync_interval_minutes", String.valueOf(minutes));
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Erro ao salvar intervalo de sync", e);
        }
    }
    
    /**
     * Define estratégia de resolução de conflitos
     * @param strategy Estratégia
     */
    public void setConflictResolution(DataSynchronizer.ConflictResolution strategy) {
        dataSynchronizer.setConflictResolution(strategy);
        
        try {
            offlineDAO.atualizarMetadado("conflict_resolution_strategy", strategy.name());
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Erro ao salvar estratégia de conflito", e);
        }
    }
    
    // ==================== ESTATÍSTICAS ====================
    
    /**
     * Obtém estatísticas do sistema offline
     * @return Mapa com estatísticas
     */
    public Map<String, Object> getOfflineStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Estatísticas do banco offline
            Map<String, Object> dbStats = offlineDAO.obterEstatisticas();
            stats.putAll(dbStats);
            
            // Informações de estado
            stats.put("current_state", currentState.name());
            stats.put("offline_mode_enabled", offlineModeEnabled);
            stats.put("connected", connectivityManager.isOnline());
            
            // Status de sincronização
            DataSynchronizer.SyncStatus syncStatus = dataSynchronizer.getStatus();
            stats.put("auto_sync_enabled", syncStatus.autoSyncEnabled);
            stats.put("sync_interval", syncStatus.syncInterval);
            stats.put("pending_operations", syncStatus.pendingOperations);
            stats.put("last_sync_time", syncStatus.lastSyncTime);
            
            // Informações do banco SQLite
            stats.put("database_info", sqliteConnection.getDatabaseInfo());
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao obter estatísticas", e);
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }
    
    // ==================== LISTENERS ====================
    
    /**
     * Adiciona listener de mudanças de estado
     * @param listener Listener
     */
    public void addStateListener(OfflineStateListener listener) {
        stateListeners.add(listener);
    }
    
    /**
     * Remove listener de mudanças de estado
     * @param listener Listener
     */
    public void removeStateListener(OfflineStateListener listener) {
        stateListeners.remove(listener);
    }
    
    // ==================== MANUTENÇÃO ====================
    
    /**
     * Executa limpeza do banco offline
     * @throws SQLException
     */
    public void cleanupOfflineData() throws SQLException {
        LOGGER.info("Executando limpeza de dados offline");
        
        try {
            // Remove registros sincronizados antigos
            // Implementar lógica de limpeza
            
            // Executa VACUUM no SQLite
            sqliteConnection.vacuum();
            
            LOGGER.info("Limpeza concluída");
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro durante limpeza", e);
            throw e;
        }
    }
    
    /**
     * Reinicia o sistema offline
     * @throws SQLException
     */
    public void restart() throws SQLException {
        LOGGER.info("Reiniciando sistema offline");
        
        shutdown();
        initialized = false;
        initialize();
    }
    
    /**
     * Finaliza o sistema offline
     */
    public void shutdown() {
        LOGGER.info("Finalizando sistema offline");
        
        try {
            dataSynchronizer.shutdown();
            connectivityManager.stopMonitoring();
            sqliteConnection.shutdown();
            
            initialized = false;
            setState(OfflineState.INITIALIZING);
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro durante finalização", e);
        }
    }
    
    // ==================== INTERFACE DE LISTENER ====================
    
    /**
     * Interface para listeners de mudanças de estado offline
     */
    public interface OfflineStateListener {
        /**
         * Chamado quando o estado offline muda
         * @param oldState Estado anterior
         * @param newState Novo estado
         */
        void onStateChanged(OfflineState oldState, OfflineState newState);
    }
    
    // ==================== INFORMAÇÕES DO SISTEMA ====================
    
    /**
     * Obtém informações detalhadas do sistema offline
     * @return String com informações
     */
    public String getSystemInfo() {
        StringBuilder info = new StringBuilder();
        info.append("=== SISTEMA OFFLINE ===\n");
        info.append("Estado: ").append(currentState).append("\n");
        info.append("Modo Offline: ").append(offlineModeEnabled ? "Habilitado" : "Desabilitado").append("\n");
        info.append("Conectado: ").append(connectivityManager.isOnline() ? "Sim" : "Não").append("\n");
        info.append("Inicializado: ").append(initialized ? "Sim" : "Não").append("\n");
        
        try {
            DataSynchronizer.SyncStatus syncStatus = dataSynchronizer.getStatus();
            info.append("\n=== SINCRONIZAÇÃO ===\n");
            info.append("Auto Sync: ").append(syncStatus.autoSyncEnabled ? "Habilitado" : "Desabilitado").append("\n");
            info.append("Intervalo: ").append(syncStatus.syncInterval).append(" minutos\n");
            info.append("Operações Pendentes: ").append(syncStatus.pendingOperations).append("\n");
            info.append("Última Sync: ").append(syncStatus.lastSyncTime != null ? syncStatus.lastSyncTime : "Nunca").append("\n");
            
            info.append("\n=== BANCO SQLITE ===\n");
            info.append(sqliteConnection.getDatabaseInfo());
            
        } catch (Exception e) {
            info.append("\nErro ao obter informações: ").append(e.getMessage());
        }
        
        return info.toString();
    }
    
    /**
     * Verifica conectividade com o banco de dados
     * @return true se conectado ao banco
     */
    private boolean checkDatabaseConnectivity() {
        try {
            // Usa o ConnectivityManager atualizado para testar conectividade do banco
            ConnectivityManager connManager = ConnectivityManager.getInstance();
            boolean dbConnected = connManager.checkDatabaseConnection();
            
            if (dbConnected) {
                LOGGER.info("Conectividade com banco de dados confirmada");
                return true;
            } else {
                LOGGER.warning("Falha na conectividade com banco de dados");
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao verificar conectividade do banco: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Força transição para estado ONLINE quando conectividade for detectada
     * Método público para ser chamado externamente quando conectividade for confirmada
     */
    public void forceOnlineState() {
        if (currentState == OfflineState.INITIALIZING || currentState == OfflineState.ERROR) {
            boolean dbConnected = checkDatabaseConnectivity();
            if (dbConnected) {
                setState(OfflineState.ONLINE);
                LOGGER.info("Estado forçado para ONLINE devido à conectividade detectada");
                
                // Inicia sincronização se modo offline estiver habilitado
                if (offlineModeEnabled) {
                    try {
                        dataSynchronizer.startAutoSync();
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Erro ao iniciar auto-sync: " + e.getMessage(), e);
                    }
                }
            } else {
                LOGGER.warning("Tentativa de forçar estado ONLINE falhou - sem conectividade com banco");
            }
        }
    }
}