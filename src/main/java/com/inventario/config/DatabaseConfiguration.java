package com.inventario.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Configuração do DataSource para o banco de dados PostgreSQL
 * 
 * Esta classe configura explicitamente o DataSource necessário
 * para a conexão com o banco de dados PostgreSQL.
 * 
 * @author Sistema de Inventário
 * @version 1.0
 */
@Configuration
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