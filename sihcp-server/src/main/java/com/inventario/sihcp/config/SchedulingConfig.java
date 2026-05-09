package com.inventario.sihcp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuração de tarefas agendadas
 * 
 * Habilita @Scheduled para:
 * - Monitoramento de memória
 * - Limpeza de cache
 * - Verificação de conexões
 */
@Configuration
@EnableScheduling
@Profile("mobile")
public class SchedulingConfig {
    // Configuração automática via anotação
}
