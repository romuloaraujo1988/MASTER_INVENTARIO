package com.inventario.sihcp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.inventario.sihcp.mobile.server.realtime.ColetaLiveSocketHandler;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

/**
 * Configuração do WebSocket para monitoramento em tempo real.
 *
 * <p>Combina STOMP (dashboard web) e WebSocket nativo (monitor ao vivo do
 * app Android) em uma única classe de configuração.</p>
 *
 * <p><b>Por que as DUAS anotações ({@code @EnableWebSocketMessageBroker} +
 * {@code @EnableWebSocket}) na mesma classe:</b></p>
 * <ul>
 *   <li>{@code @EnableWebSocketMessageBroker} ativa o STOMP e processa
 *       {@link WebSocketMessageBrokerConfigurer} (métodos
 *       {@code configureMessageBroker} e {@code registerStompEndpoints}).</li>
 *   <li>{@code @EnableWebSocket} ativa o WebSocket nativo e processa
 *       {@link WebSocketConfigurer} (método {@link #registerWebSocketHandlers}).
 *       <b>SEM esta anotação, {@code registerWebSocketHandlers} NUNCA é
 *       chamado</b> — foi exatamente o bug que deixava o endpoint
 *       {@code /ws-coletas-live} sem handler (nenhum log de WS no servidor).</li>
 * </ul>
 *
 * <p>O {@code jakarta.websocket.server.ServerContainer} JSR-356 é necessário
 * para o handshake do WebSocket nativo. Garantimos sua inicialização de forma
 * <b>à prova de configuração</b> via {@link #wsServerContainerInitializer()} —
 * ver Javadoc daquele bean.</p>
 */
@Configuration
@EnableWebSocketMessageBroker
@EnableWebSocket
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer, WebSocketConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    /**
     * Handler do feed nativo de coletas em tempo real.
     * Injetado aqui para registrar o endpoint {@code /ws/coletas-live}
     * dentro do mesmo contexto WebSocket que o STOMP.
     */
    @Autowired(required = false)
    private ColetaLiveSocketHandler coletaLiveSocketHandler;

    /**
     * Garante a inicialização do {@code jakarta.websocket.server.ServerContainer}
     * (JSR-356) <b>independentemente da configuração</b> de
     * {@code spring.autoconfigure.exclude}.
     *
     * <p><b>Por que este bean existe:</b> o handshake do WebSocket nativo
     * ({@code /ws-coletas-live}) usa o {@code AbstractStandardUpgradeStrategy}
     * do Spring, que exige o atributo {@code ServerContainer} no
     * {@code ServletContext}. Esse atributo normalmente é criado pela
     * {@code WebSocketServletAutoConfiguration} do Spring Boot — mas se ela for
     * excluída (como já aconteceu neste projeto "para economizar memória"), o
     * handshake falha com:</p>
     * <pre>
     *   No 'jakarta.websocket.server.ServerContainer' ServletContext attribute.
     *   Are you running in a Servlet container that supports JSR-356?
     * </pre>
     *
     * <p>Registrar o {@code WsSci} (o {@code ServletContainerInitializer} do
     * Tomcat que popula esse atributo) diretamente no {@code ServletContext}
     * via {@link ServletContextInitializer} torna a inicialização robusta:
     * funciona mesmo com a auto-config excluída, e é idempotente — se o
     * {@code ServerContainer} já existe (auto-config ativa), o {@code WsSci}
     * apenas reusa, sem duplicar.</p>
     *
     * <p>Usamos reflection para carregar {@code org.apache.tomcat.websocket.server.WsSci}
     * a fim de não criar dependência rígida de compilação — em qualquer outro
     * container (Jetty/Undertow) o bean simplesmente loga e segue, sem quebrar
     * o boot.</p>
     */
    @Bean
    public ServletContextInitializer wsServerContainerInitializer() {
        return (ServletContext servletContext) -> {
            final String attr = "jakarta.websocket.server.ServerContainer";
            if (servletContext.getAttribute(attr) != null) {
                logger.info("✓ ServerContainer JSR-356 já presente — WebSocket nativo OK");
                return;
            }
            try {
                Class<?> wsSciClass = Class.forName("org.apache.tomcat.websocket.server.WsSci");
                jakarta.servlet.ServletContainerInitializer wsSci =
                        (jakarta.servlet.ServletContainerInitializer) wsSciClass
                                .getDeclaredConstructor().newInstance();
                wsSci.onStartup(null, servletContext);
                logger.info("✓ WsSci registrado manualmente — ServerContainer JSR-356 inicializado "
                        + "(handshake do /ws-coletas-live habilitado)");
            } catch (ClassNotFoundException e) {
                logger.warn("WsSci do Tomcat não encontrado no classpath — "
                        + "container não-Tomcat? WebSocket nativo pode não funcionar.");
            } catch (ServletException | ReflectiveOperationException e) {
                logger.error("Falha ao registrar WsSci manualmente: {}", e.getMessage(), e);
            }
        };
    }

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

    /**
     * Registra o handler nativo do feed de coletas ao vivo.
     *
     * <p><b>Path {@code /ws-coletas-live} (com hífen, NÃO sob {@code /ws/}):</b>
     * o endpoint STOMP {@code /ws} é registrado com {@code .withSockJS()}, e o
     * SockJS captura todo o namespace {@code /ws/**} — ao tentar usar
     * {@code /ws/coletas-live}, o SockJS interceptava a requisição antes do
     * handler raw, falhando com "Invalid SockJS path - required to have 3 path
     * segments" e ignorando o handshake. Usar um path fora de {@code /ws/}
     * elimina a colisão.</p>
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        if (coletaLiveSocketHandler != null) {
            registry.addHandler(coletaLiveSocketHandler, "/ws-coletas-live")
                    .setAllowedOriginPatterns("*");
            logger.info("✓ WebSocket nativo registrado: /ws-coletas-live (monitor ao vivo)");
        } else {
            logger.warn("⚠ ColetaLiveSocketHandler nulo — endpoint /ws-coletas-live NÃO registrado");
        }
    }
}