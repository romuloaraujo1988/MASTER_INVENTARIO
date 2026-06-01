package com.inventario.sihcp.mobile.server.realtime;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Handler WebSocket nativo para o feed em tempo real de coletas.
 *
 * <p>Por que não STOMP? O projeto já tem {@link com.inventario.sihcp.config.WebSocketConfig}
 * configurando STOMP+SockJS para a dashboard web, mas o app Android é um
 * cliente nativo simples — usar STOMP aqui exigiria uma lib extra no app
 * (ex.: NaikSoftware/StompProtocolAndroid) e um broker mais pesado. O
 * payload que precisamos trocar é trivial (um JSON unidirecional
 * server→cliente), então um handler raw resolve com OkHttp puro do lado
 * Android.</p>
 *
 * <p><b>Endpoint:</b> {@code ws[s]://servidor:porta/ws-coletas-live}</p>
 *
 * <p><b>Protocolo:</b></p>
 * <ul>
 *   <li>Cliente conecta passando {@code ?inventarioId=N} na query string
 *       (opcional). Se não informado, recebe broadcast de todos os
 *       inventários — útil para administradores monitorando o sistema.</li>
 *   <li>Servidor envia {@link ColetaEventoDTO} serializado como JSON em
 *       cada coleta registrada.</li>
 *   <li>Mensagens enviadas pelo cliente são ignoradas — feed é one-way.</li>
 * </ul>
 *
 * <p><b>Estado:</b> sessões mantidas em memória. Em ambiente single-instance
 * isso basta. Para múltiplas instâncias do servidor seria preciso usar um
 * broker externo (Redis pub/sub, RabbitMQ) — fica como TODO quando o
 * deploy escalar.</p>
 *
 * <p><b>Concurrency:</b> {@link CopyOnWriteArraySet} para cada bucket de
 * sessões — leitura barata (broadcast) e escrita rara (conexões). Sem
 * locks explícitos.</p>
 */
@Component
public class ColetaLiveSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ColetaLiveSocketHandler.class);

    /** Bucket especial para clientes que não filtram por inventário. */
    private static final Integer GLOBAL_BUCKET = -1;

    /** Atributo da sessão onde o ID do inventário fica armazenado. */
    private static final String ATTR_INVENTARIO_ID = "inventarioId";

    /** Sessões agrupadas por inventário (ou {@link #GLOBAL_BUCKET}). */
    private final Map<Integer, Set<WebSocketSession>> sessoesPorInventario = new ConcurrentHashMap<>();

    /**
     * ObjectMapper compartilhado da aplicação Spring — garante que datas e
     * configurações de Jackson sejam consistentes com o resto da API.
     */
    private final ObjectMapper objectMapper;

    @Autowired
    public ColetaLiveSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Integer inventarioId = extrairInventarioId(session);
        session.getAttributes().put(ATTR_INVENTARIO_ID, inventarioId);

        Integer bucket = inventarioId != null ? inventarioId : GLOBAL_BUCKET;
        sessoesPorInventario
                .computeIfAbsent(bucket, k -> new CopyOnWriteArraySet<>())
                .add(session);

        logger.info("WS coletas-live conectado: session={}, inventarioId={}, totalNoBucket={}",
                session.getId(), inventarioId,
                sessoesPorInventario.get(bucket).size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Integer bucket = (Integer) session.getAttributes().get(ATTR_INVENTARIO_ID);
        if (bucket == null) {
            bucket = GLOBAL_BUCKET;
        }
        Set<WebSocketSession> sessoes = sessoesPorInventario.get(bucket);
        if (sessoes != null) {
            sessoes.remove(session);
            if (sessoes.isEmpty()) {
                sessoesPorInventario.remove(bucket);
            }
        }
        logger.info("WS coletas-live desconectado: session={}, status={}",
                session.getId(), status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Feed é unidirecional. Logamos só pra debug em caso de cliente
        // mal-comportado mandando ping/pong custom.
        if (logger.isDebugEnabled()) {
            logger.debug("Mensagem recebida do cliente {} ignorada: {}",
                    session.getId(), message.getPayload());
        }
    }

    /**
     * Publica um evento de coleta para todos os clientes interessados.
     *
     * <p>Faz fan-out para:</p>
     * <ul>
     *   <li>Sessões inscritas no inventário específico do evento</li>
     *   <li>Sessões inscritas no bucket global (sem filtro)</li>
     * </ul>
     *
     * <p>Erros de envio para uma sessão (cliente desconectou no meio, buffer
     * cheio) são logados mas não interrompem o broadcast — outras sessões
     * continuam recebendo. A sessão problemática é encerrada para liberar
     * recursos.</p>
     */
    public void broadcastColeta(ColetaEventoDTO evento) {
        if (evento == null) {
            return;
        }

        // Serializa uma única vez — broadcast pode atingir dezenas de sessões.
        final String payload;
        try {
            payload = objectMapper.writeValueAsString(evento);
        } catch (JsonProcessingException e) {
            logger.warn("Falha ao serializar ColetaEventoDTO para WS: {}", e.getMessage());
            return;
        }

        TextMessage message = new TextMessage(payload);

        // Sessões filtradas pelo inventário do evento.
        if (evento.getInventarioId() != null) {
            Set<WebSocketSession> alvos = sessoesPorInventario.get(evento.getInventarioId());
            int n = alvos != null ? alvos.size() : 0;
            logger.info("WS broadcast: bucket inv-{} tem {} sessão(ões)", evento.getInventarioId(), n);
            enviarParaTodas(alvos, message, "inv-" + evento.getInventarioId());
        }

        // Sessões globais (sem filtro).
        Set<WebSocketSession> globais = sessoesPorInventario.get(GLOBAL_BUCKET);
        int ng = globais != null ? globais.size() : 0;
        logger.info("WS broadcast: bucket global tem {} sessão(ões). Buckets ativos: {}",
                ng, sessoesPorInventario.keySet());
        enviarParaTodas(globais, message, "global");
    }

    private void enviarParaTodas(Set<WebSocketSession> sessoes, TextMessage message, String contexto) {
        if (sessoes == null || sessoes.isEmpty()) {
            return;
        }
        int enviadas = 0;
        for (WebSocketSession s : sessoes) {
            if (!s.isOpen()) {
                sessoes.remove(s);
                continue;
            }
            try {
                // sendMessage é synchronous internamente; em cargas altas
                // pode ser substituído por ConcurrentWebSocketSessionDecorator.
                s.sendMessage(message);
                enviadas++;
            } catch (IOException | IllegalStateException e) {
                logger.debug("Falha ao enviar para session {} (contexto={}): {} — encerrando sessão",
                        s.getId(), contexto, e.getMessage());
                sessoes.remove(s);
                try {
                    s.close(CloseStatus.SERVER_ERROR);
                } catch (IOException closeEx) {
                    // Silencioso — sessão já está em estado ruim.
                }
            }
        }
        if (logger.isDebugEnabled() && enviadas > 0) {
            logger.debug("Broadcast WS coletas-live: {} sessões alcançadas (contexto={})",
                    enviadas, contexto);
        }
    }

    /**
     * Extrai o {@code inventarioId} da query string da URI de handshake.
     * Retorna {@code null} se ausente, vazio ou inválido — o cliente vai
     * para o bucket global.
     */
    private Integer extrairInventarioId(WebSocketSession session) {
        if (session.getUri() == null) {
            return null;
        }
        String query = session.getUri().getQuery();
        if (query == null || query.isEmpty()) {
            return null;
        }
        for (String par : query.split("&")) {
            int eq = par.indexOf('=');
            if (eq <= 0 || eq == par.length() - 1) {
                continue;
            }
            String chave = par.substring(0, eq);
            String valor = par.substring(eq + 1);
            if ("inventarioId".equals(chave)) {
                try {
                    return Integer.parseInt(valor);
                } catch (NumberFormatException e) {
                    logger.debug("inventarioId inválido na query string: {}", valor);
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Apenas para diagnóstico/admin — quantidade de sessões ativas por bucket.
     * Usar em endpoints de health/admin se quiser monitorar.
     */
    public int totalSessoesAtivas() {
        int total = 0;
        for (Set<WebSocketSession> sessoes : sessoesPorInventario.values()) {
            total += sessoes.size();
        }
        return total;
    }
}
