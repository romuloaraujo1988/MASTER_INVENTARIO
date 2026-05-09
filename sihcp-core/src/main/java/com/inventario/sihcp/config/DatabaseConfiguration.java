package com.inventario.sihcp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

/**
 * Configuração do DataSource para o banco de dados PostgreSQL
 * 
 * Esta classe configura explicitamente o DataSource necessário
 * para a conexão com o banco de dados PostgreSQL.
 * 
 * NOTA: Desativado quando profile "mobile" está ativo (usa MobileDataSourceConfig)
 * 
 * @author Sistema de Inventário
 * @version 1.1
 */
@Configuration
@Profile("!mobile")  // Não carrega quando profile mobile está ativo
public class DatabaseConfiguration {
    
    /**
     * Configura o DataSource principal do sistema
     * 
     * @return DataSource configurado para PostgreSQL
     */
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }
}