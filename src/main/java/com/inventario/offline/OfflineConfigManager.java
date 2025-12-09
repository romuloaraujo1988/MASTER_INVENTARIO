package com.inventario.offline;

import java.io.*;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Gerenciador de configurações para o modo offline.
 * Carrega e gerencia todas as configurações do arquivo offline.properties.
 * 
 * @author Sistema de Inventário IFMT
 * @version 1.0.0
 */
public class OfflineConfigManager {
    private static final Logger logger = Logger.getLogger(OfflineConfigManager.class.getName());
    private static OfflineConfigManager instance;
    private Properties properties;
    private static final String CONFIG_FILE = "/offline.properties";
    
    // Configurações padrão
    private static final String DEFAULT_DATABASE_PATH = "./data/inventario.db";
    private static final int DEFAULT_SYNC_INTERVAL = 5;
    private static final int DEFAULT_CONNECTIVITY_CHECK_INTERVAL = 30;
    private static final String DEFAULT_CONFLICT_RESOLUTION = "TIMESTAMP_WINS";
    private static final int DEFAULT_BATCH_SIZE = 50;
    private static final boolean DEFAULT_OFFLINE_ENABLED = true;
    
    private OfflineConfigManager() {
        loadConfiguration();
    }
    
    /**
     * Obtém a instância singleton do gerenciador de configurações.
     * 
     * @return Instância do OfflineConfigManager
     */
    public static synchronized OfflineConfigManager getInstance() {
        if (instance == null) {
            instance = new OfflineConfigManager();
        }
        return instance;
    }
    
    /**
     * Carrega as configurações do arquivo offline.properties.
     */
    private void loadConfiguration() {
        properties = new Properties();
        
        try (InputStream input = getClass().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
                logger.info("Configurações offline carregadas com sucesso");
            } else {
                logger.warning("Arquivo de configuração offline.properties não encontrado. Usando configurações padrão.");
                loadDefaultConfiguration();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Erro ao carregar configurações offline", e);
            loadDefaultConfiguration();
        }
    }
    
    /**
     * Carrega configurações padrão quando o arquivo não está disponível.
     */
    private void loadDefaultConfiguration() {
        properties.setProperty("offline.enabled", String.valueOf(DEFAULT_OFFLINE_ENABLED));
        properties.setProperty("offline.database.path", DEFAULT_DATABASE_PATH);
        properties.setProperty("sync.interval.minutes", String.valueOf(DEFAULT_SYNC_INTERVAL));
        properties.setProperty("connectivity.check.interval", String.valueOf(DEFAULT_CONNECTIVITY_CHECK_INTERVAL));
        properties.setProperty("sync.conflict.resolution", DEFAULT_CONFLICT_RESOLUTION);
        properties.setProperty("sync.batch.size", String.valueOf(DEFAULT_BATCH_SIZE));
    }
    
    /**
     * Recarrega as configurações do arquivo.
     */
    public void reloadConfiguration() {
        loadConfiguration();
        logger.info("Configurações offline recarregadas");
    }
    
    // ==================== MÉTODOS DE ACESSO ÀS CONFIGURAÇÕES ====================
    
    /**
     * Verifica se o modo offline está habilitado.
     * 
     * @return true se o modo offline estiver habilitado
     */
    public boolean isOfflineEnabled() {
        return getBooleanProperty("offline.enabled", DEFAULT_OFFLINE_ENABLED);
    }
    
    /**
     * Obtém o caminho do banco de dados SQLite.
     * 
     * @return Caminho do banco de dados
     */
    public String getDatabasePath() {
        return getStringProperty("offline.database.path", DEFAULT_DATABASE_PATH);
    }
    
    /**
     * Obtém o timeout para operações de banco em segundos.
     * 
     * @return Timeout em segundos
     */
    public int getDatabaseTimeout() {
        return getIntProperty("offline.database.timeout", 30);
    }
    
    /**
     * Obtém o intervalo de verificação de conectividade em segundos.
     * 
     * @return Intervalo em segundos
     */
    public int getConnectivityCheckInterval() {
        return getIntProperty("connectivity.check.interval", DEFAULT_CONNECTIVITY_CHECK_INTERVAL);
    }
    
    /**
     * Obtém o timeout para teste de conectividade em segundos.
     * 
     * @return Timeout em segundos
     */
    public int getConnectivityTimeout() {
        return getIntProperty("connectivity.timeout", 5);
    }
    
    /**
     * Obtém a URL para teste de conectividade.
     * 
     * @return URL de teste
     */
    public String getConnectivityTestUrl() {
        return getStringProperty("connectivity.test.url", "http://www.google.com");
    }
    
    /**
     * Obtém o número de tentativas de reconexão.
     * 
     * @return Número de tentativas
     */
    public int getConnectivityRetryAttempts() {
        return getIntProperty("connectivity.retry.attempts", 3);
    }
    
    /**
     * Obtém o intervalo entre tentativas de reconexão em segundos.
     * 
     * @return Intervalo em segundos
     */
    public int getConnectivityRetryInterval() {
        return getIntProperty("connectivity.retry.interval", 10);
    }
    
    /**
     * Verifica se a sincronização automática está habilitada.
     * 
     * @return true se a sincronização automática estiver habilitada
     */
    public boolean isAutoSyncEnabled() {
        return getBooleanProperty("sync.auto.enabled", true);
    }
    
    /**
     * Obtém o intervalo de sincronização automática em minutos.
     * 
     * @return Intervalo em minutos
     */
    public int getSyncIntervalMinutes() {
        return getIntProperty("sync.interval.minutes", DEFAULT_SYNC_INTERVAL);
    }
    
    /**
     * Obtém o tamanho do lote para sincronização.
     * 
     * @return Tamanho do lote
     */
    public int getSyncBatchSize() {
        return getIntProperty("sync.batch.size", DEFAULT_BATCH_SIZE);
    }
    
    /**
     * Obtém o timeout para operações de sincronização em segundos.
     * 
     * @return Timeout em segundos
     */
    public int getSyncTimeout() {
        return getIntProperty("sync.timeout", 120);
    }
    
    /**
     * Obtém a estratégia de resolução de conflitos.
     * 
     * @return Estratégia de resolução
     */
    public String getConflictResolutionStrategy() {
        return getStringProperty("sync.conflict.resolution", DEFAULT_CONFLICT_RESOLUTION);
    }
    
    /**
     * Obtém o número máximo de tentativas de sincronização.
     * 
     * @return Número máximo de tentativas
     */
    public int getSyncMaxRetries() {
        return getIntProperty("sync.max.retries", 3);
    }
    
    /**
     * Obtém o intervalo entre tentativas de sincronização em segundos.
     * 
     * @return Intervalo em segundos
     */
    public int getSyncRetryInterval() {
        return getIntProperty("sync.retry.interval", 30);
    }
    
    /**
     * Obtém o tamanho máximo do cache local em MB.
     * 
     * @return Tamanho máximo em MB
     */
    public int getStorageCacheMaxSize() {
        return getIntProperty("storage.cache.max.size", 100);
    }
    
    /**
     * Obtém o tempo de retenção de dados sincronizados em dias.
     * 
     * @return Tempo de retenção em dias
     */
    public int getStorageRetentionDays() {
        return getIntProperty("storage.retention.days", 30);
    }
    
    /**
     * Verifica se a compressão de dados está habilitada.
     * 
     * @return true se a compressão estiver habilitada
     */
    public boolean isCompressionEnabled() {
        return getBooleanProperty("storage.compression.enabled", true);
    }
    
    /**
     * Verifica se o backup automático está habilitado.
     * 
     * @return true se o backup automático estiver habilitado
     */
    public boolean isBackupEnabled() {
        return getBooleanProperty("storage.backup.enabled", true);
    }
    
    /**
     * Obtém o intervalo de backup automático em horas.
     * 
     * @return Intervalo em horas
     */
    public int getBackupInterval() {
        return getIntProperty("storage.backup.interval", 24);
    }
    
    /**
     * Obtém o número máximo de backups a manter.
     * 
     * @return Número máximo de backups
     */
    public int getBackupMaxCount() {
        return getIntProperty("storage.backup.max.count", 7);
    }
    
    /**
     * Obtém o tamanho do pool de threads para sincronização.
     * 
     * @return Tamanho do pool
     */
    public int getThreadPoolSize() {
        return getIntProperty("performance.thread.pool.size", 2);
    }
    
    /**
     * Obtém o timeout para operações assíncronas em segundos.
     * 
     * @return Timeout em segundos
     */
    public int getAsyncTimeout() {
        return getIntProperty("performance.async.timeout", 60);
    }
    
    /**
     * Verifica se as otimizações de performance estão habilitadas.
     * 
     * @return true se as otimizações estiverem habilitadas
     */
    public boolean isPerformanceOptimizationsEnabled() {
        return getBooleanProperty("performance.optimizations.enabled", true);
    }
    
    /**
     * Obtém o intervalo de limpeza automática em horas.
     * 
     * @return Intervalo em horas
     */
    public int getCleanupInterval() {
        return getIntProperty("performance.cleanup.interval", 6);
    }
    
    /**
     * Obtém o nível de log para modo offline.
     * 
     * @return Nível de log
     */
    public String getLoggingLevel() {
        return getStringProperty("logging.level", "INFO");
    }
    
    /**
     * Verifica se o log detalhado de sincronização está habilitado.
     * 
     * @return true se o log detalhado estiver habilitado
     */
    public boolean isSyncDetailedLogging() {
        return getBooleanProperty("logging.sync.detailed", false);
    }
    
    /**
     * Verifica se o log de operações de banco está habilitado.
     * 
     * @return true se o log de operações estiver habilitado
     */
    public boolean isDatabaseOperationsLogging() {
        return getBooleanProperty("logging.database.operations", false);
    }
    
    /**
     * Verifica se o indicador de status offline está habilitado na interface.
     * 
     * @return true se o indicador estiver habilitado
     */
    public boolean isStatusIndicatorEnabled() {
        return getBooleanProperty("ui.status.indicator.enabled", true);
    }
    
    /**
     * Obtém a posição do indicador de status.
     * 
     * @return Posição do indicador
     */
    public String getStatusIndicatorPosition() {
        return getStringProperty("ui.status.indicator.position", "TOP_RIGHT");
    }
    
    /**
     * Verifica se as notificações de sincronização estão habilitadas.
     * 
     * @return true se as notificações estiverem habilitadas
     */
    public boolean isSyncNotificationsEnabled() {
        return getBooleanProperty("ui.sync.notifications.enabled", true);
    }
    
    /**
     * Obtém a duração das notificações em segundos.
     * 
     * @return Duração em segundos
     */
    public int getNotificationsDuration() {
        return getIntProperty("ui.notifications.duration", 5);
    }
    
    /**
     * Verifica se a coleta offline de patrimônios está habilitada.
     * 
     * @return true se a coleta offline estiver habilitada
     */
    public boolean isPatrimonioOfflineEnabled() {
        return getBooleanProperty("features.patrimonio.offline.enabled", true);
    }
    
    /**
     * Verifica se a criação offline de inventários está habilitada.
     * 
     * @return true se a criação offline estiver habilitada
     */
    public boolean isInventarioOfflineEnabled() {
        return getBooleanProperty("features.inventario.offline.enabled", true);
    }
    
    /**
     * Verifica se o backup de fotos offline está habilitado.
     * 
     * @return true se o backup de fotos estiver habilitado
     */
    public boolean isFotosOfflineEnabled() {
        return getBooleanProperty("features.fotos.offline.enabled", true);
    }
    
    /**
     * Obtém o tamanho máximo de foto em KB.
     * 
     * @return Tamanho máximo em KB
     */
    public int getFotosMaxSize() {
        return getIntProperty("features.fotos.max.size", 500);
    }
    
    /**
     * Obtém a qualidade de compressão de fotos (0-100).
     * 
     * @return Qualidade de compressão
     */
    public int getFotosCompressionQuality() {
        return getIntProperty("features.fotos.compression.quality", 80);
    }
    
    /**
     * Verifica se o modo de desenvolvimento está habilitado.
     * 
     * @return true se o modo de desenvolvimento estiver habilitado
     */
    public boolean isDevelopmentMode() {
        return getBooleanProperty("development.mode.enabled", false);
    }
    
    /**
     * Obtém o número máximo de registros offline.
     * 
     * @return Número máximo de registros
     */
    public int getMaxOfflineRecords() {
        return getIntProperty("limits.max.offline.records", 10000);
    }
    
    /**
     * Obtém o tamanho máximo do banco SQLite em MB.
     * 
     * @return Tamanho máximo em MB
     */
    public int getDatabaseMaxSize() {
        return getIntProperty("limits.database.max.size", 500);
    }
    
    /**
     * Obtém o número máximo de operações pendentes.
     * 
     * @return Número máximo de operações
     */
    public int getMaxPendingOperations() {
        return getIntProperty("limits.max.pending.operations", 1000);
    }
    
    /**
     * Obtém o tempo máximo offline antes de alerta em horas.
     * 
     * @return Tempo máximo em horas
     */
    public int getMaxOfflineTime() {
        return getIntProperty("limits.max.offline.time", 24);
    }
    
    // ==================== MÉTODOS AUXILIARES ====================
    
    /**
     * Obtém uma propriedade como string.
     * 
     * @param key Chave da propriedade
     * @param defaultValue Valor padrão
     * @return Valor da propriedade
     */
    private String getStringProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Obtém uma propriedade como inteiro.
     * 
     * @param key Chave da propriedade
     * @param defaultValue Valor padrão
     * @return Valor da propriedade
     */
    private int getIntProperty(String key, int defaultValue) {
        try {
            String value = properties.getProperty(key);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Valor inválido para propriedade {0}. Usando valor padrão: {1}", new Object[]{key, defaultValue});
            return defaultValue;
        }
    }
    
    /**
     * Obtém uma propriedade como boolean.
     * 
     * @param key Chave da propriedade
     * @param defaultValue Valor padrão
     * @return Valor da propriedade
     */
    private boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }
    /**
     * Obtém uma propriedade como double.
     * 
     * @param key Chave da propriedade
     * @param defaultValue Valor padrão
     * @return Valor da propriedade
     */
    
    /**
     * Define uma propriedade.
     * 
     * @param key Chave da propriedade
     * @param value Valor da propriedade
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
    
    /**
     * Salva as configurações no arquivo.
     * 
     * @throws IOException Se houver erro ao salvar
     */
    public void saveConfiguration() throws IOException {
        // Implementação para salvar configurações modificadas
        // Por enquanto, apenas log da operação
        logger.info("Configurações offline salvas");
    }
    
    /**
     * Obtém todas as propriedades.
     * 
     * @return Properties com todas as configurações
     */
    public Properties getAllProperties() {
        return new Properties(properties);
    }
    
    /**
     * Verifica se uma propriedade existe.
     * 
     * @param key Chave da propriedade
     * @return true se a propriedade existir
     */
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }
    
    /**
     * Remove uma propriedade.
     * 
     * @param key Chave da propriedade
     * @return Valor removido ou null
     */
    public String removeProperty(String key) {
        return (String) properties.remove(key);
    }
    
    /**
     * Limpa todas as propriedades e recarrega as padrão.
     */
    public void resetToDefaults() {
        properties.clear();
        loadDefaultConfiguration();
        logger.info("Configurações offline resetadas para valores padrão");
    }
    
    /**
     * Obtém informações de debug sobre as configurações.
     * 
     * @return String com informações de debug
     */
    public String getDebugInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Configurações Offline Debug ===").append("\n");
        sb.append("Offline Habilitado: ").append(isOfflineEnabled()).append("\n");
        sb.append("Caminho do Banco: ").append(getDatabasePath()).append("\n");
        sb.append("Intervalo de Sync: ").append(getSyncIntervalMinutes()).append(" min\n");
        sb.append("Estratégia de Conflito: ").append(getConflictResolutionStrategy()).append("\n");
        sb.append("Tamanho do Lote: ").append(getSyncBatchSize()).append("\n");
        sb.append("Total de Propriedades: ").append(properties.size()).append("\n");
        return sb.toString();
    }
}