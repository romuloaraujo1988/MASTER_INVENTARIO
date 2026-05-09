package com.inventario.sihcp.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

/**
 * Configuração do DataSource para o perfil mobile
 * Garante que o HikariCP seja configurado corretamente
 */
@Configuration
@Profile("mobile")
public class MobileDataSourceConfig {

    @Value("${spring.datasource.url:jdbc:postgresql://localhost:5432/sispatrimonio}")
    private String jdbcUrl;

    @Value("${spring.datasource.username:postgres}")
    private String username;

    @Value("${spring.datasource.password:}")
    private String password;

    @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
    private String driverClassName;

    @Value("${spring.datasource.hikari.maximum-pool-size:10}")
    private int maximumPoolSize;

    @Value("${spring.datasource.hikari.minimum-idle:5}")
    private int minimumIdle;

    @Value("${spring.datasource.hikari.connection-timeout:30000}")
    private long connectionTimeout;

    @Value("${spring.datasource.hikari.idle-timeout:600000}")
    private long idleTimeout;

    @Value("${spring.datasource.hikari.max-lifetime:1800000}")
    private long maxLifetime;

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        // Configurações obrigatórias
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        
        // Configurações do pool - OTIMIZADO para baixo consumo de memória
        config.setMaximumPoolSize(Math.min(maximumPoolSize, 5)); // Máximo 5 conexões
        config.setMinimumIdle(Math.min(minimumIdle, 1));         // Mínimo 1 conexão
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(Math.min(idleTimeout, 300000));    // Máximo 5 minutos
        config.setMaxLifetime(Math.min(maxLifetime, 900000));    // Máximo 15 minutos
        
        // Configurações adicionais
        config.setConnectionTestQuery("SELECT 1");
        config.setPoolName("MobilePool");
        config.setAutoCommit(true);
        
        // Configurações de validação
        config.setValidationTimeout(3000);
        config.setLeakDetectionThreshold(30000); // Detectar vazamentos mais rápido
        
        // Propriedades específicas do PostgreSQL - REDUZIDO cache
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "100");   // Era: 250
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "1024"); // Era: 2048
        
        return new HikariDataSource(config);
    }
}
