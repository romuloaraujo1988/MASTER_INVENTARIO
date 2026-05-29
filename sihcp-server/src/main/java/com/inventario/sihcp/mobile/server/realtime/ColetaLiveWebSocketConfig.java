package com.inventario.sihcp.mobile.server.realtime;

/**
 * Configuração do WebSocket nativo para o feed de coletas ao vivo.
 *
 * <p><b>MIGRADO:</b> o registro do handler {@link ColetaLiveSocketHandler}
 * foi movido para {@link com.inventario.sihcp.config.WebSocketConfig}
 * (via {@code registerWebSocketHandlers}), que já usa
 * {@code @EnableWebSocketMessageBroker} e inicializa o
 * {@code jakarta.websocket.server.ServerContainer} JSR-356 corretamente
 * em Spring Boot fat JAR.</p>
 *
 * <p>Manter uma segunda configuração {@code @EnableWebSocket} separada
 * causava {@code BeanCreationException} no startup porque o
 * {@code ServerContainer} ainda não estava disponível no
 * {@code ServletContext} no momento da criação do bean.</p>
 *
 * <p>Esta classe é mantida apenas como documentação histórica e pode ser
 * removida em uma limpeza futura.</p>
 */
public final class ColetaLiveWebSocketConfig {
    private ColetaLiveWebSocketConfig() {}
}
