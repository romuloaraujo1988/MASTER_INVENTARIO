package com.inventario.mobile.server.security;

import com.inventario.mobile.server.service.MobileAuthService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filtro JWT específico para endpoints mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class MobileJwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileJwtAuthenticationFilter.class);
    
    private final MobileAuthService mobileAuthService;
    private final UserDetailsService userDetailsService;
    
    public MobileJwtAuthenticationFilter(MobileAuthService mobileAuthService, UserDetailsService userDetailsService) {
        this.mobileAuthService = mobileAuthService;
        this.userDetailsService = userDetailsService;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        logger.info("=== MOBILE JWT FILTER EXECUTANDO ===");
        logger.info("Request Path: {}", requestPath);
        logger.info("Method: {}", request.getMethod());
        
        try {
            // Extrair token do header Authorization
            String jwt = getJwtFromRequest(request);
            logger.info("Token extraído: {}", jwt != null ? "SIM (length=" + jwt.length() + ")" : "NÃO");
            
            if (StringUtils.hasText(jwt)) {
                logger.info("Validando token...");
                boolean isValid = mobileAuthService.validateToken(jwt);
                logger.info("Token válido: {}", isValid);
                
                if (isValid) {
                    String username = mobileAuthService.getUsernameFromToken(jwt);
                    logger.info("Username extraído do token: {}", username);
                    
                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        logger.info("Carregando detalhes do usuário: {}", username);
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        
                        if (userDetails != null) {
                            UsernamePasswordAuthenticationToken authentication = 
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            
                            logger.info("✅ Usuário autenticado via JWT mobile: {}", username);
                        } else {
                            logger.warn("❌ UserDetails não encontrado para: {}", username);
                        }
                    } else {
                        logger.warn("❌ Username nulo ou já autenticado. Username: {}, Auth: {}", 
                                   username, SecurityContextHolder.getContext().getAuthentication());
                    }
                } else {
                    logger.warn("❌ Token inválido");
                }
            } else {
                logger.warn("❌ Nenhum token JWT encontrado no header Authorization");
            }
        } catch (Exception e) {
            logger.error("❌ Erro durante autenticação JWT mobile: {}", e.getMessage(), e);
        }
        
        logger.info("=== FIM MOBILE JWT FILTER ===");
        filterChain.doFilter(request, response);
    }
    
    /**
     * Extrai o token JWT do header Authorization
     * 
     * @param request requisição HTTP
     * @return token JWT ou null se não encontrado
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        return null;
    }
    
    /**
     * Determina se o filtro deve ser aplicado para esta requisição
     * Aplica apenas para endpoints mobile
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        logger.debug("Verificando se deve aplicar filtro para path: {}", path);
        
        // Considera o contexto path /inventario
        boolean shouldApply = path.contains("/api/mobile/");
        logger.debug("Deve aplicar filtro: {}", shouldApply);
        
        return !shouldApply;
    }
}