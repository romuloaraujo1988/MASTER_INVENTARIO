package com.inventario.sihcp.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Provedor de tokens JWT para autenticação mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:inventario-mobile-secret-key-2024-very-long-secret-for-security}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400}") // 24 horas em segundos
    private Long jwtExpirationInMs;

    @Value("${jwt.refresh-expiration:604800}") // 7 dias em segundos
    private Long refreshExpirationInMs;

    /**
     * Gera chave secreta a partir da string configurada
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Gera token de acesso a partir da autenticação
     * 
     * @param authentication objeto de autenticação
     * @return token JWT
     */
    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInMs * 1000);

        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Gera token de refresh a partir da autenticação
     * 
     * @param authentication objeto de autenticação
     * @return refresh token JWT
     */
    public String generateRefreshToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Date expiryDate = new Date(System.currentTimeMillis() + refreshExpirationInMs * 1000);

        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Obtém o tempo de expiração em segundos
     * 
     * @return tempo de expiração
     */
    public Long getExpirationTime() {
        return jwtExpirationInMs;
    }

    /**
     * Valida o token JWT
     * 
     * @param token token a ser validado
     * @return true se válido
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Valida o token JWT propagando a causa raiz em caso de falha.
     *
     * Uso típico: filtros que precisam distinguir {@link ExpiredJwtException} de
     * demais {@link JwtException} para responder contratos específicos
     * (ex.: body `token_expired` vs `invalid_token`).
     *
     * @param token token a ser validado
     * @throws JwtException             se o token for inválido (malformado, assinatura inválida)
     * @throws ExpiredJwtException      se o token estiver expirado
     * @throws IllegalArgumentException se o token for nulo ou vazio
     */
    public void validateTokenOrThrow(String token) {
        Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token);
    }

    /**
     * Extrai o username do token
     * 
     * @param token token JWT
     * @return username
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Extrai a data de expiração do token
     * 
     * @param token token JWT
     * @return data de expiração
     */
    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Verifica se o token expirou
     * 
     * @param token token JWT
     * @return true se expirado
     */
    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration != null && expiration.before(new Date());
    }

    /**
     * Verifica se é um token de refresh
     * 
     * @param token token JWT
     * @return true se for refresh token
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return "refresh".equals(claims.get("type"));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}