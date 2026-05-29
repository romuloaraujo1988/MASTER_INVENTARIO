package com.inventario.sihcp.mobile.server.realtime;

import org.apache.tomcat.websocket.server.WsSci;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Garante que o {@code jakarta.websocket.server.ServerContainer} (JSR-356)
 * seja registrado no {@code ServletContext} do Tomcat embedded.
 *
 * <h2>Por que isso é necessário</h2>
 * <p>O handshake do WebSocket nativo ({@code /ws-coletas-live}, via
 * {@code TextWebSocketHandler}) usa o {@code AbstractStandardUpgradeStrategy}
 * do Spring, que exige o atributo
 * {@code jakarta.websocket.server.ServerContainer} no {@code ServletContext}.
 * Sem ele, o handshake falha com:</p>
 * <pre>
 *   IllegalArgumentException: No 'jakarta.websocket.server.ServerContainer'
 *     ServletContext attribute. Are you running in a Servlet container that
 *     supports JSR-356?
 * </pre>
 *
 * <p>Em Spring Boot fat JAR com Tomcat embedded, o {@code WsSci}
 * ({@code ServletContainerInitializer} do Tomcat que popula esse atributo)
 * nem sempre é descoberto via SPI — o {@code META-INF/services} do
 * {@code tomcat-embed-websocket} pode não ser preservado/varrido pelo
 * {@code spring-boot-maven-plugin:repackage}. Resultado: o STOMP funciona
 * (usa transportes SockJS sobre HTTP, que não dependem do
 * {@code ServerContainer}), mas o WebSocket nativo quebra no upgrade.</p>
 *
 * <h2>Por que um {@code WebServerFactoryCustomizer} e não um
 * {@code ServletServerContainerFactoryBean}</h2>
 * <p>O {@code ServletServerContainerFactoryBean} <b>lê</b> o atributo no
 * momento da criação do bean — se o {@code WsSci} ainda não rodou, ele falha
 * com "Attribute not found". Já o {@code WebServerFactoryCustomizer} adiciona
 * o {@code WsSci} ao contexto do Tomcat <b>durante a construção do
 * WebServer</b>, no momento certo do boot, antes de qualquer requisição —
 * é o ponto de extensão correto.</p>
 */
@Configuration
public class TomcatWebSocketCustomizerConfig {

    private static final Logger logger =
            LoggerFactory.getLogger(TomcatWebSocketCustomizerConfig.class);

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> websocketContainerCustomizer() {
        return factory -> {
            factory.addContextCustomizers(context -> {
                // Registra o ServletContainerInitializer do Tomcat que cria o
                // ServerContainer JSR-356 e o coloca no ServletContext.
                // O segundo parâmetro (Set<Class<?>>) é null — o WsSci não
                // precisa de classes pré-descobertas para o uso programático
                // que fazemos (registro de handlers via Spring, não anotações).
                context.addServletContainerInitializer(new WsSci(), null);
            });
            logger.info("✓ WsSci do Tomcat registrado — ServerContainer JSR-356 será inicializado no boot");
        };
    }
}
