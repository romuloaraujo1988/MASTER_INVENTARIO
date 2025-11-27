package com.inventario.mobile.server.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filtro OTIMIZADO para logar requisições HTTP
 * 
 * v3.0: Logs MÍNIMOS para economizar memória
 * - Apenas erros são logados por padrão
 * - Headers NÃO são logados (economiza memória)
 * - Pode ser habilitado via propriedade
 */
@Component
public class RequestLoggingFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
    
    // Desabilitado por padrão para economizar memória
    @Value("${mobile.server.request-logging.enabled:false}")
    private boolean loggingEnabled;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        // Se logging desabilitado, apenas passa adiante (RÁPIDO)
        if (!loggingEnabled) {
            chain.doFilter(request, response);
            return;
        }
        
        // Logging mínimo apenas se habilitado
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String uri = httpRequest.getRequestURI();
            
            // Logar apenas requisições para /api/mobile/ (sem headers)
            if (uri.startsWith("/inventario/api/mobile/")) {
                logger.debug("{} {}", httpRequest.getMethod(), uri);
            }
        }
        
        chain.doFilter(request, response);
    }
    
    /**
     * Obtém o IP real do cliente (usado apenas quando necessário)
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.contains(",") ? ip.split(",")[0].trim() : ip;
        }
        
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        
        return request.getRemoteAddr();
    }
}
