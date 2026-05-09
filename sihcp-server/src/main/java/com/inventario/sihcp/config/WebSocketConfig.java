package com.inventario.sihcp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuração do WebSocket para monitoramento em tempo real
 * Suporta o SimpMessagingTemplate usado no RealTimeMonitoringService
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilita um broker simples em memória para enviar mensagens aos clientes
        config.enableSimpleBroker("/topic");
        
        // Define o prefixo para mensagens destinadas ao servidor
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Registra o endpoint WebSocket que os clientes usarão para conectar
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Permite todas as origens (ajustar para produção)
                .withSockJS(); // Habilita fallback SockJS para navegadores que não suportam WebSocket
        
        // Endpoint adicional sem SockJS para clientes nativos WebSocket
        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns("*");
    }
}