package com.inventario.sihcp.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Ponto de entrada para autenticação JWT da API Mobile.
 *
 * Contrato de resposta (requisito 9.3 de correcoes-seguranca):
 *  - HTTP 401 + Content-Type: application/json
 *  - Body `{"error":"token_expired"}` quando a causa é {@link ExpiredJwtException}
 *  - Body `{"error":"invalid_token"}` para demais falhas de autenticação
 *
 * A distinção "expirado vs inválido" é propagada pelo filtro JWT via atributo de request
 * ({@link #ATTR_JWT_EXCEPTION}) porque o Spring Security normaliza a exceção ao chegar aqui.
 * Também inspecionamos {@link AuthenticationException#getCause()} como fallback.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    public static final String ATTR_JWT_EXCEPTION = "com.inventario.sihcp.JWT_EXCEPTION";

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    private static final String ERROR_TOKEN_EXPIRED = "token_expired";
    private static final String ERROR_INVALID_TOKEN = "invalid_token";

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String errorCode = resolveErrorCode(request, authException);

        response.getWriter().write("{\"error\":\"" + errorCode + "\"}");

        log.debug("Auth entry point: error={}", errorCode);
    }

    /**
     * Determina o código de erro (`token_expired` ou `invalid_token`) sem expor
     * fragmentos do token. Não loga a exceção em nível INFO+.
     */
    private String resolveErrorCode(HttpServletRequest request, AuthenticationException authException) {
        Object attr = request.getAttribute(ATTR_JWT_EXCEPTION);
        if (attr instanceof ExpiredJwtException) {
            return ERROR_TOKEN_EXPIRED;
        }
        if (attr instanceof JwtException) {
            return ERROR_INVALID_TOKEN;
        }

        Throwable cause = authException != null ? authException.getCause() : null;
        while (cause != null) {
            if (cause instanceof ExpiredJwtException) {
                return ERROR_TOKEN_EXPIRED;
            }
            if (cause instanceof JwtException) {
                return ERROR_INVALID_TOKEN;
            }
            cause = cause.getCause();
        }

        return ERROR_INVALID_TOKEN;
    }
}
