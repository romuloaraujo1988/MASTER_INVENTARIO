package com.inventario.sihcp.mobile.server.config;

import com.inventario.sihcp.mobile.server.security.LoginRateLimiter;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração que registra o {@link LoginRateLimiter} como
 * {@link org.springframework.web.servlet.HandlerInterceptor} aplicado
 * exclusivamente ao endpoint {@code POST /api/mobile/auth/login}.
 *
 * <p>O registro é feito via
 * {@link WebMvcConfigurer#addInterceptors(InterceptorRegistry)}, limitando
 * o escopo do interceptor apenas ao path pattern
 * {@code /api/mobile/auth/login}. Assim, o rate limiting afeta somente o
 * fluxo de autenticação mobile — os demais endpoints autenticados da API
 * permanecem sem essa camada, conforme previsto no requisito 7.5 da spec
 * {@code correcoes-seguranca}.
 *
 * <p>O {@link LoginRateLimiter} é anotado com
 * {@link org.springframework.stereotype.Component @Component} e injetado
 * pelo Spring via construtor. A flag
 * {@code api.mobile.rate-limit.enabled} (ativada no perfil {@code prod})
 * controla internamente se o interceptor efetivamente bloqueia requisições
 * ou apenas as deixa passar, permitindo ativar/desativar o rate limiting
 * sem remover este bean do contexto.
 *
 * <p>Esta classe não substitui, e é independente de,
 * {@link WebMvcConfig} (que registra o
 * {@link DeviceTrackingInterceptor}): ambas as configurações coexistem e
 * o Spring combina os interceptors de todos os
 * {@link WebMvcConfigurer}s encontrados.
 *
 * @author Sistema de Inventário
 * @version 1.0.0
 * @see LoginRateLimiter
 * @see WebMvcConfig
 */
@Configuration
public class RateLimitConfig implements WebMvcConfigurer {

    /** Path pattern ao qual o rate limiter é aplicado. */
    private static final String LOGIN_PATH_PATTERN = "/api/mobile/auth/login";

    /** Interceptor responsável por impor o limite de tentativas de login por IP. */
    private final LoginRateLimiter loginRateLimiter;

    /**
     * Construtor com injeção de dependência do interceptor.
     *
     * @param loginRateLimiter interceptor de rate limit do login mobile
     *                         (nunca {@code null})
     */
    public RateLimitConfig(LoginRateLimiter loginRateLimiter) {
        this.loginRateLimiter = loginRateLimiter;
    }

    /**
     * Registra o {@link LoginRateLimiter} no {@link InterceptorRegistry}
     * restringindo sua aplicação ao path {@value #LOGIN_PATH_PATTERN}.
     *
     * <p>Como apenas um path pattern é adicionado, o interceptor nunca
     * é executado para outros endpoints da aplicação — cumprindo o
     * requisito de que o rate limiting não afete endpoints autenticados
     * como {@code /api/mobile/coletas}, {@code /api/mobile/sync}, etc.
     *
     * @param registry registro de interceptors fornecido pelo Spring MVC
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginRateLimiter)
                .addPathPatterns(LOGIN_PATH_PATTERN);
    }
}
