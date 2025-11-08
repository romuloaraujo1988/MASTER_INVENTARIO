package com.inventario.mobile.server.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Enumeration;

/**
 * Filtro para logar todas as requisições HTTP
 */
@Component
public class RequestLoggingFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            
            String uri = httpRequest.getRequestURI();
            
            // Logar apenas requisições para /api/mobile/
            if (uri.startsWith("/inventario/api/mobile/")) {
                logger.info("╔════════════════════════════════════════════════════════════════╗");
                logger.info("║  REQUISIÇÃO HTTP RECEBIDA                                      ║");
                logger.info("╠════════════════════════════════════════════════════════════════╣");
                String clientIp = getClientIpAddress(httpRequest);
                
                logger.info("  Método: {}", httpRequest.getMethod());
                logger.info("  URI: {}", uri);
                logger.info("  Query String: {}", httpRequest.getQueryString());
                logger.info("  Remote Addr (direto): {}", httpRequest.getRemoteAddr());
                logger.info("  Client IP (real): {}", clientIp);
                logger.info("  Content Type: {}", httpRequest.getContentType());
                logger.info("  Content Length: {}", httpRequest.getContentLength());
                
                // Logar headers
                logger.info("  Headers:");
                Enumeration<String> headerNames = httpRequest.getHeaderNames();
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    String headerValue = httpRequest.getHeader(headerName);
                    logger.info("    {}: {}", headerName, headerValue);
                }
                
                logger.info("╚════════════════════════════════════════════════════════════════╝");
            }
        }
        
        chain.doFilter(request, response);
    }
    
    /**
     * Obtém o IP real do cliente, considerando proxies e load balancers
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };
        
        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For pode conter múltiplos IPs separados por vírgula
                // O primeiro é o IP real do cliente
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                logger.debug("IP obtido do header {}: {}", header, ip);
                return ip;
            }
        }
        
        // Se nenhum header foi encontrado, usar o IP direto
        String remoteAddr = request.getRemoteAddr();
        logger.debug("Usando IP direto (RemoteAddr): {}", remoteAddr);
        return remoteAddr;
    }
}
