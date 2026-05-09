package com.inventario.sihcp.mobile.server.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

/**
 * Interceptor de controle de taxa (rate limiting) para o endpoint de login
 * mobile.
 *
 * <p>Aplica um balde de tokens (token bucket) por IP ao endpoint
 * {@code POST /api/mobile/auth/login}, permitindo no máximo
 * {@value #CAPACITY} tentativas de login por janela de {@value #REFILL_WINDOW_MINUTES}
 * minuto(s). Quando o limite é excedido, responde com HTTP 429
 * ({@link HttpStatus#TOO_MANY_REQUESTS}) incluindo o cabeçalho
 * {@code Retry-After} e um corpo JSON no formato:
 * <pre>{@code {"error":"rate_limited","retryAfterSeconds":N}}</pre>
 *
 * <p>A flag {@code api.mobile.rate-limit.enabled} permite ligar/desligar o
 * rate limiting sem remover o bean — útil para ambientes de desenvolvimento.
 *
 * <p>Além do bloqueio, o interceptor monitora a ocorrência de bloqueios
 * por IP e emite um log de alerta ({@code RATE_LIMIT_ALERT}) quando o
 * mesmo IP atinge {@value #EXCESS_ALERT_THRESHOLD} bloqueios em uma janela
 * deslizante de {@value #EXCESS_ALERT_WINDOW_MINUTES} minutos, facilitando
 * a detecção de ataques de força bruta.
 *
 * <p>Resolução do IP do cliente considera o cabeçalho
 * {@code X-Forwarded-For} (primeiro IP antes da vírgula) quando o servidor
 * estiver atrás de um proxy reverso; caso o cabeçalho esteja ausente ou
 * vazio, usa {@link HttpServletRequest#getRemoteAddr()}.
 *
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@Component
public class LoginRateLimiter implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LoginRateLimiter.class);

    /** Capacidade máxima do balde: número de tentativas permitidas por janela. */
    private static final int CAPACITY = 10;

    /** Janela de reabastecimento completa do balde (1 minuto). */
    private static final Duration REFILL_WINDOW = Duration.ofMinutes(1);

    /** Número de bloqueios por IP que dispara um log de alerta. */
    private static final int EXCESS_ALERT_THRESHOLD = 3;

    /** Janela deslizante usada para contabilizar bloqueios por IP (5 minutos). */
    private static final Duration EXCESS_ALERT_WINDOW = Duration.ofMinutes(5);

    /** Path (terminação) reconhecida como login mobile. */
    private static final String LOGIN_PATH_SUFFIX = "/api/mobile/auth/login";

    /** Ativa/desativa o rate limiting via property {@code api.mobile.rate-limit.enabled}. */
    @Value("${api.mobile.rate-limit.enabled:false}")
    private boolean enabled;

    /** Baldes de tokens por IP — criados sob demanda. */
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /** Histórico de bloqueios recentes por IP para alerta de abuso. */
    private final Map<String, Deque<Instant>> excessEvents = new ConcurrentHashMap<>();

    /**
     * Intercepta a requisição antes do controller. Aplica rate limit apenas
     * quando {@link #enabled} é {@code true} e o path é
     * {@code POST /api/mobile/auth/login}.
     *
     * @param req     requisição HTTP
     * @param resp    resposta HTTP (usada para escrever 429 se necessário)
     * @param handler handler do Spring MVC (não utilizado)
     * @return {@code true} para continuar o processamento, {@code false}
     *         quando a requisição é bloqueada por rate limit
     * @throws IOException em caso de falha ao escrever no corpo da resposta
     */
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler)
            throws IOException {
        if (!enabled) {
            return true;
        }
        if (!isLoginRequest(req)) {
            return true;
        }

        String ip = resolveClientIp(req);
        Bucket bucket = buckets.computeIfAbsent(ip, k -> newBucket());
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            return true;
        }

        long waitSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()) + 1;
        resp.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        resp.setHeader("Retry-After", String.valueOf(waitSeconds));
        resp.setContentType("application/json");
        resp.getWriter().write(
                "{\"error\":\"rate_limited\",\"retryAfterSeconds\":" + waitSeconds + "}");
        registerExcessAndMaybeAlert(ip);
        return false;
    }

    /**
     * Cria um novo {@link Bucket} com capacidade {@value #CAPACITY} tokens e
     * reabastecimento total a cada 1 minuto ({@link #REFILL_WINDOW}).
     *
     * @return um balde de tokens recém-criado para um IP
     */
    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.classic(CAPACITY, Refill.intervally(CAPACITY, REFILL_WINDOW));
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Verifica se a requisição corresponde ao endpoint de login mobile.
     *
     * @param r requisição HTTP
     * @return {@code true} quando o método é {@code POST} e o path termina em
     *         {@link #LOGIN_PATH_SUFFIX}
     */
    private boolean isLoginRequest(HttpServletRequest r) {
        return "POST".equalsIgnoreCase(r.getMethod())
                && r.getRequestURI() != null
                && r.getRequestURI().endsWith(LOGIN_PATH_SUFFIX);
    }

    /**
     * Resolve o IP do cliente levando em conta proxies reversos.
     * Preferência para o primeiro IP declarado em {@code X-Forwarded-For};
     * caso ausente ou em branco, usa {@link HttpServletRequest#getRemoteAddr()}.
     *
     * @param req requisição HTTP
     * @return IP do cliente como string (nunca {@code null} para requisições válidas)
     */
    private String resolveClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }

    /**
     * Registra um bloqueio por IP na janela deslizante e, ao ultrapassar o
     * limiar {@value #EXCESS_ALERT_THRESHOLD} dentro de 5 minutos
     * ({@link #EXCESS_ALERT_WINDOW}), emite um log de alerta
     * {@code RATE_LIMIT_ALERT} para monitoração/SOC.
     *
     * @param ip IP cliente bloqueado
     */
    private void registerExcessAndMaybeAlert(String ip) {
        Deque<Instant> events = excessEvents.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>());
        Instant now = Instant.now();
        events.addLast(now);
        Instant cutoff = now.minus(EXCESS_ALERT_WINDOW);
        while (!events.isEmpty() && events.peekFirst().isBefore(cutoff)) {
            events.pollFirst();
        }
        if (events.size() >= EXCESS_ALERT_THRESHOLD) {
            log.warn("RATE_LIMIT_ALERT ip={} excess_events_last_5min={}", ip, events.size());
        }
    }
}
