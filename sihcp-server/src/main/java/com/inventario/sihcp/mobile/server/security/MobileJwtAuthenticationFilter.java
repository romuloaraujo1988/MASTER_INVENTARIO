package com.inventario.sihcp.mobile.server.security;

import com.inventario.sihcp.mobile.server.service.MobileAuthService;
import com.inventario.sihcp.security.JwtAuthenticationEntryPoint;
import com.inventario.sihcp.security.JwtTokenProvider;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
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
 * Filtro JWT específico para endpoints mobile.
 *
 * Além de autenticar o usuário, marca a request com o atributo
 * {@link JwtAuthenticationEntryPoint#ATTR_JWT_EXCEPTION} quando o token é
 * inválido ou expirado, permitindo ao entry point distinguir
 * `token_expired` de `invalid_token` (requisito 9.3 de correcoes-seguranca).
 *
 * @author Sistema de Inventário
 * @version 1.1.0
 */
public class MobileJwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(MobileJwtAuthenticationFilter.class);

    private final MobileAuthService mobileAuthService;
    private final UserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;

    public MobileJwtAuthenticationFilter(MobileAuthService mobileAuthService,
                                         UserDetailsService userDetailsService,
                                         JwtTokenProvider jwtTokenProvider) {
        this.mobileAuthService = mobileAuthService;
        this.userDetailsService = userDetailsService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (logger.isDebugEnabled()) {
            logger.debug("MobileJwtAuthenticationFilter: path={}, method={}", request.getRequestURI(), request.getMethod());
        }

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                // Parse estrito para capturar ExpiredJwtException vs demais JwtException.
                // O atributo permite ao JwtAuthenticationEntryPoint montar o body correto
                // (`token_expired` vs `invalid_token`) sem mudar o fluxo geral do filtro.
                try {
                    jwtTokenProvider.validateTokenOrThrow(jwt);
                } catch (ExpiredJwtException expired) {
                    request.setAttribute(JwtAuthenticationEntryPoint.ATTR_JWT_EXCEPTION, expired);
                    logger.debug("JWT expirado detectado no filtro mobile");
                    filterChain.doFilter(request, response);
                    return;
                } catch (JwtException | IllegalArgumentException invalid) {
                    request.setAttribute(JwtAuthenticationEntryPoint.ATTR_JWT_EXCEPTION, invalid);
                    logger.debug("JWT inválido detectado no filtro mobile");
                    filterChain.doFilter(request, response);
                    return;
                }

                // Checagem adicional (blacklist/etc.) já existente
                if (mobileAuthService.validateToken(jwt)) {
                    String username = mobileAuthService.getUsernameFromToken(jwt);

                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        if (userDetails != null) {
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);

                            logger.debug("Usuário autenticado via JWT mobile: {}", username);
                        } else {
                            logger.debug("UserDetails não encontrado para usuário autenticado via JWT");
                        }
                    }
                } else {
                    // Token passou no parse estrito mas foi rejeitado por regras adicionais
                    // (p.ex. blacklist). Tratamos como invalid_token.
                    request.setAttribute(JwtAuthenticationEntryPoint.ATTR_JWT_EXCEPTION,
                            new JwtException("Token rejeitado"));
                    logger.debug("Token JWT rejeitado por validação adicional (ex.: blacklist)");
                }
            }
        } catch (Exception e) {
            // Não logamos o token nem o stack em nível INFO+; detalhes só em debug
            logger.debug("Erro inesperado na autenticação JWT mobile: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrai o token JWT do header Authorization.
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
     * Determina se o filtro deve ser aplicado para esta requisição.
     * Aplica apenas para endpoints mobile.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        boolean shouldApply = path.contains("/api/mobile/");
        return !shouldApply;
    }
}
