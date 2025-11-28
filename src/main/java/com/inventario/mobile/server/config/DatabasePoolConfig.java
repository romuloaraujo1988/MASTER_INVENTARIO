package com.inventario.mobile.server.config;

import com.inventario.util.ConnectionManager;
import com.inventario.util.DatabaseConnection;
import com.inventario.config.DatabaseConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Configuração do pool de conexões para o servidor mobile
 * 
 * Inicializa o ConnectionManager com HikariCP para reutilizar conexões
 * ao invés de criar uma nova conexão a cada operação.
 * 
 * ⚠️ CRÍTICO: O servidor mobile DEVE usar PostgreSQL.
 * SQLite é apenas para o app desktop em modo offline.
 * Se não conseguir conectar ao PostgreSQL, o servidor DEVE falhar.
 * 
 * BENEFÍCIOS:
 * - Reutilização de conexões (evita overhead de criar/fechar)
 * - Melhor performance
 * - Menos logs de conexão
 * - Controle de pool (máximo de conexões)
 */
@Configuration
public class DatabasePoolConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabasePoolConfig.class);
    
    @Value("${spring.datasource.url:}")
    private String datasourceUrl;
    
    @Value("${spring.datasource.username:}")
    private String datasourceUsername;
    
    @Value("${spring.datasource.password:}")
    private String datasourcePassword;
    
    /**
     * Inicializa o pool de conexões ao iniciar o servidor
     * 
     * ⚠️ CRÍTICO: Valida que a conexão é PostgreSQL, nunca SQLite
     */
    @PostConstruct
    public void initializeConnectionPool() {
        logger.info("═══════════════════════════════════════════════════════════════");
        logger.info("  INICIALIZANDO POOL DE CONEXÕES - SERVIDOR MOBILE");
        logger.info("═══════════════════════════════════════════════════════════════");
        
        try {
            String url = null;
            String username = null;
            String password = null;
            
            // PRIORIDADE 1: Usar configuração do Spring (application-mobile.properties)
            if (datasourceUrl != null && !datasourceUrl.isEmpty()) {
                url = datasourceUrl;
                username = datasourceUsername;
                password = datasourcePassword;
                logger.info("✓ Usando configuração do Spring: {}", datasourceUrl);
            } else {
                // PRIORIDADE 2: Usar configuração do DatabaseConnection (configuracao_banco.json)
                DatabaseConfig config = DatabaseConnection.getCurrentConfig();
                if (config != null && config.isValid()) {
                    url = String.format("jdbc:postgresql://%s:%d/%s",
                            config.getHost(), config.getPort(), config.getDatabase());
                    username = config.getUsername();
                    password = config.getPassword();
                    logger.info("✓ Usando configuração do configuracao_banco.json: {}", url);
                }
            }
            
            // ⚠️ VALIDAÇÃO CRÍTICA: Servidor mobile DEVE ter PostgreSQL
            if (url == null || url.isEmpty()) {
                logger.error("╔════════════════════════════════════════════════════════════════╗");
                logger.error("║  ❌ ERRO CRÍTICO: Nenhuma configuração de banco encontrada!    ║");
                logger.error("╠════════════════════════════════════════════════════════════════╣");
                logger.error("║  O servidor mobile REQUER PostgreSQL para funcionar.          ║");
                logger.error("║                                                                ║");
                logger.error("║  Verifique:                                                    ║");
                logger.error("║  1. Arquivo configuracao_banco.json existe na raiz            ║");
                logger.error("║  2. Configuração spring.datasource.url em application.properties║");
                logger.error("╚════════════════════════════════════════════════════════════════╝");
                throw new RuntimeException("Servidor mobile requer configuração de PostgreSQL");
            }
            
            // ⚠️ VALIDAÇÃO CRÍTICA: Garantir que NÃO é SQLite
            if (url.contains("sqlite")) {
                logger.error("╔════════════════════════════════════════════════════════════════╗");
                logger.error("║  ❌ ERRO CRÍTICO: Configuração aponta para SQLite!            ║");
                logger.error("╠════════════════════════════════════════════════════════════════╣");
                logger.error("║  O servidor mobile NÃO PODE usar SQLite.                      ║");
                logger.error("║  SQLite é apenas para o app desktop em modo offline.          ║");
                logger.error("║                                                                ║");
                logger.error("║  Corrija a configuração para apontar para PostgreSQL.         ║");
                logger.error("╚════════════════════════════════════════════════════════════════╝");
                throw new RuntimeException("Servidor mobile não pode usar SQLite - configure PostgreSQL");
            }
            
            // Testar conexão antes de inicializar o pool
            logger.info("Testando conexão com PostgreSQL...");
            try (Connection testConn = DriverManager.getConnection(url, username, password)) {
                if (testConn != null && !testConn.isClosed()) {
                    String dbName = testConn.getMetaData().getDatabaseProductName();
                    String dbVersion = testConn.getMetaData().getDatabaseProductVersion();
                    logger.info("✓ Conexão com {} {} validada com sucesso!", dbName, dbVersion);
                    
                    // Validar que é PostgreSQL
                    if (!dbName.toLowerCase().contains("postgresql")) {
                        logger.warn("⚠️ AVISO: Banco de dados não é PostgreSQL: {}", dbName);
                    }
                }
            } catch (Exception e) {
                logger.error("╔════════════════════════════════════════════════════════════════╗");
                logger.error("║  ❌ ERRO: Não foi possível conectar ao PostgreSQL!            ║");
                logger.error("╠════════════════════════════════════════════════════════════════╣");
                logger.error("║  URL: {}                                                       ", url);
                logger.error("║  Erro: {}                                                      ", e.getMessage());
                logger.error("║                                                                ║");
                logger.error("║  Verifique:                                                    ║");
                logger.error("║  1. PostgreSQL está rodando                                    ║");
                logger.error("║  2. Host/porta estão corretos                                  ║");
                logger.error("║  3. Usuário/senha estão corretos                               ║");
                logger.error("║  4. Banco de dados existe                                      ║");
                logger.error("╚════════════════════════════════════════════════════════════════╝");
                throw new RuntimeException("Falha ao conectar com PostgreSQL: " + e.getMessage(), e);
            }
            
            // Inicializar pool de conexões
            ConnectionManager.initialize(url, username, password);
            
            if (ConnectionManager.isInitialized()) {
                logger.info("═══════════════════════════════════════════════════════════════");
                logger.info("  ✓ POOL DE CONEXÕES POSTGRESQL INICIALIZADO COM SUCESSO!");
                logger.info("  {}", ConnectionManager.getPoolStats());
                logger.info("═══════════════════════════════════════════════════════════════");
            }
            
        } catch (RuntimeException e) {
            // Re-lançar exceções de validação para impedir inicialização
            throw e;
        } catch (Exception e) {
            logger.error("Erro ao inicializar pool de conexões: {}", e.getMessage());
            throw new RuntimeException("Falha crítica ao inicializar banco de dados", e);
        }
    }
    
    /**
     * Fecha o pool de conexões ao parar o servidor
     */
    @PreDestroy
    public void shutdownConnectionPool() {
        logger.info("Fechando pool de conexões...");
        ConnectionManager.shutdown();
        logger.info("✓ Pool de conexões fechado");
    }
}
