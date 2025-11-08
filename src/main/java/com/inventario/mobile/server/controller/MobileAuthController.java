package com.inventario.mobile.server.controller;

import com.inventario.mobile.server.dto.ApiResponse;
import com.inventario.mobile.server.dto.MobileLoginRequest;
import com.inventario.mobile.server.dto.MobileLoginResponse;
import com.inventario.mobile.server.service.MobileAuthService;
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
@CrossOrigin(origins = "*", maxAge = 3600)
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
            logger.info("═══════════════════════════════════════════════════════════");
            logger.info("ENDPOINT DE LOGIN MOBILE CHAMADO");
            logger.info("Usuário: {}", loginRequest.getUsername());
            logger.info("Senha fornecida: {}", loginRequest.getPassword() != null && !loginRequest.getPassword().isEmpty() ? "SIM" : "NÃO");
            logger.info("Device ID: {}", loginRequest.getDeviceId());
            logger.info("App Version: {}", loginRequest.getAppVersion());
            logger.info("═══════════════════════════════════════════════════════════");
            
            logger.info("Chamando MobileAuthService.authenticateUser()...");
            MobileLoginResponse loginResponse = mobileAuthService.authenticateUser(loginRequest);
            
            logger.info("═══════════════════════════════════════════════════════════");
            logger.info("LOGIN BEM-SUCEDIDO!");
            logger.info("Usuário: {}", loginRequest.getUsername());
            logger.info("Access Token: {}", loginResponse.getAccessToken() != null ? loginResponse.getAccessToken().substring(0, Math.min(20, loginResponse.getAccessToken().length())) + "..." : "null");
            logger.info("Refresh Token: {}", loginResponse.getRefreshToken() != null ? "presente" : "null");
            logger.info("Expires In: {}", loginResponse.getExpiresIn());
            logger.info("User Info: {}", loginResponse.getUser() != null ? loginResponse.getUser().getUsername() : "null");
            logger.info("Token Type: {}", loginResponse.getTokenType());
            logger.info("═══════════════════════════════════════════════════════════");
            
            ResponseEntity<MobileLoginResponse> response = ResponseEntity.ok(loginResponse);
            
            logger.info("Resposta HTTP:");
            logger.info("  Status Code: {}", response.getStatusCode());
            logger.info("  Headers: {}", response.getHeaders());
            logger.info("  Body presente: {}", response.getBody() != null);
            
            return response;
            
        } catch (AuthenticationException e) {
            logger.error("═══════════════════════════════════════════════════════════");
            logger.error("FALHA NA AUTENTICAÇÃO MOBILE");
            logger.error("Usuário: {}", loginRequest.getUsername());
            logger.error("Erro: {}", e.getMessage());
            logger.error("═══════════════════════════════════════════════════════════");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                    
        } catch (Exception e) {
            logger.error("═══════════════════════════════════════════════════════════");
            logger.error("ERRO INESPERADO NO LOGIN MOBILE");
            logger.error("Usuário: {}", loginRequest.getUsername());
            logger.error("Erro: {}", e.getMessage(), e);
            logger.error("═══════════════════════════════════════════════════════════");
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
     * 
     * @param token token a ser invalidado
     * @return confirmação de logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestParam String token) {
        try {
            // Implementar lógica de invalidação de token se necessário
            logger.info("Logout mobile realizado");
            
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
     * 
     * @param refreshToken token de refresh
     * @return novo access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<String>> refreshToken(@RequestParam String refreshToken) {
        try {
            // Implementar lógica de refresh token
            logger.info("Refresh token solicitado");
            
            // Por enquanto retorna erro - implementar conforme necessário
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(ApiResponse.error("Funcionalidade não implementada", "NOT_IMPLEMENTED"));
            
        } catch (Exception e) {
            logger.error("Erro no refresh token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro interno do servidor", "INTERNAL_ERROR"));
        }
    }
}