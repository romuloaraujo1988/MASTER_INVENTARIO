package com.inventario.sihcp.mobile.server.controller;

import com.inventario.sihcp.mobile.server.dto.ApiResponse;
import com.inventario.sihcp.mobile.server.dto.MobileLoginRequest;
import com.inventario.sihcp.mobile.server.dto.MobileLoginResponse;
import com.inventario.sihcp.mobile.server.service.MobileAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;

/**
 * Controlador REST para autenticação mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/mobile/auth")
// @CrossOrigin removido — ver MobileSecurityConfig.corsConfigurationSource() (spec correcoes-seguranca Req 6.1)
public class MobileAuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileAuthController.class);
    
    @Autowired
    private MobileAuthService mobileAuthService;
    
    /**
     * Endpoint de login mobile
     * 
     * @param loginRequest dados de login
     * @return resposta com token e dados do usuário
     */
    @PostMapping("/login")
    public ResponseEntity<MobileLoginResponse> login(@Valid @RequestBody MobileLoginRequest loginRequest) {
        try {
            MobileLoginResponse loginResponse = mobileAuthService.authenticateUser(loginRequest);

            // Log sanitizado — sem fragmentos de token (spec correcoes-seguranca Req 8.4)
            logger.info("Login bem-sucedido: usuario={} expiresIn={}s",
                    loginResponse.getUser() != null ? loginResponse.getUser().getUsername() : loginRequest.getUsername(),
                    loginResponse.getExpiresIn());

            return ResponseEntity.ok(loginResponse);

        } catch (AuthenticationException e) {
            logger.warn("Falha de autenticação mobile: usuario={} motivo={}",
                    loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        } catch (Exception e) {
            logger.error("Erro inesperado no login mobile: usuario={}", loginRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Endpoint para validar token
     * 
     * @param token token a ser validado
     * @return status da validação
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@RequestParam String token) {
        try {
            boolean isValid = mobileAuthService.validateToken(token);
            
            if (isValid) {
                return ResponseEntity.ok(
                    ApiResponse.success(true, "Token válido")
                );
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Token inválido", "INVALID_TOKEN"));
            }
            
        } catch (Exception e) {
            logger.error("Erro ao validar token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro interno do servidor", "INTERNAL_ERROR"));
        }
    }
    
    /**
     * Endpoint para logout (invalidar token)
     * Adiciona o token à blacklist para impedir uso após logout.
     *
     * @param authHeader header Authorization com o token Bearer (preferencial)
     * @param token token a ser invalidado (fallback via query param)
     * @return confirmação de logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String token) {
        try {
            // Extrair token: preferir header Authorization, fallback para query param
            String tokenToInvalidate = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                tokenToInvalidate = authHeader.substring(7);
            } else if (token != null && !token.isBlank()) {
                tokenToInvalidate = token;
            }

            if (tokenToInvalidate != null) {
                mobileAuthService.invalidateToken(tokenToInvalidate);
                logger.info("Token invalidado para logout");
            } else {
                logger.warn("Logout chamado sem token — nenhuma invalidação realizada");
            }

            return ResponseEntity.ok(
                ApiResponse.success("Logout realizado com sucesso", "Logout realizado com sucesso")
            );

        } catch (Exception e) {
            logger.error("Erro no logout mobile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro interno do servidor", "INTERNAL_ERROR"));
        }
    }
    
    /**
     * Endpoint para refresh token
     * POST /api/mobile/auth/refresh
     * 
     * @param refreshToken token de refresh
     * @return novo access token e informações do usuário
     */
    @PostMapping("/refresh")
    public ResponseEntity<MobileLoginResponse> refreshToken(@RequestParam String refreshToken) {
        try {
            // Log sanitizado — sem fragmentos de token (spec correcoes-seguranca Req 8.4)
            logger.info("Refresh token solicitado");

            MobileLoginResponse response = mobileAuthService.refreshAccessToken(refreshToken);

            // Log sanitizado — sem fragmentos de token (spec correcoes-seguranca Req 8.4)
            logger.info("Refresh bem-sucedido: usuario={} expiresIn={}s",
                    response.getUser() != null ? response.getUser().getUsername() : "desconhecido",
                    response.getExpiresIn());

            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            logger.warn("Falha no refresh token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        } catch (Exception e) {
            logger.error("Erro inesperado no refresh token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Endpoint de health check
     * GET /api/mobile/auth/health
     * 
     * Usado pelo app para verificar se o servidor está disponível
     * Não requer autenticação
     * 
     * @return status do servidor
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(
            ApiResponse.success("OK", "Servidor disponível")
        );
    }
}