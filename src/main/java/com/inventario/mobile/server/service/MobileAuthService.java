package com.inventario.mobile.server.service;

import com.inventario.mobile.server.dto.MobileLoginRequest;
import com.inventario.mobile.server.dto.MobileLoginResponse;
import com.inventario.mobile.server.dto.MobileUserInfo;
import com.inventario.model.Usuario;
import com.inventario.service.UsuarioService;
import com.inventario.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serviço de autenticação para aplicação mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@Service
public class MobileAuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileAuthService.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private UsuarioService usuarioService;
    
    /**
     * Autentica usuário mobile
     * 
     * @param loginRequest dados de login
     * @return resposta com token e informações do usuário
     * @throws AuthenticationException se credenciais inválidas
     */
    public MobileLoginResponse authenticateUser(MobileLoginRequest loginRequest) {
        try {
            logger.info("=== INÍCIO AUTENTICAÇÃO MOBILE ===");
            logger.info("Usuário: {}", loginRequest.getUsername());
            logger.info("Senha fornecida: {}", loginRequest.getPassword() != null ? "***" : "null");
            
            // Autenticar usuário
            logger.info("Chamando AuthenticationManager...");
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
            logger.info("Autenticação bem-sucedida pelo AuthenticationManager");
            
            // Buscar dados completos do usuário
            logger.info("Buscando dados completos do usuário...");
            Usuario usuario = usuarioService.buscarPorUsername(loginRequest.getUsername());
            
            if (usuario == null) {
                logger.error("Usuário não encontrado no banco: {}", loginRequest.getUsername());
                throw new AuthenticationException("Usuário não encontrado") {};
            }
            
            logger.info("Usuário encontrado - ID: {}, Nome: {}, Ativo: {}", 
                       usuario.getId(), usuario.getNomeCompleto(), usuario.getAtivo());
            
            if (!usuario.getAtivo()) {
                logger.error("Usuário inativo: {}", loginRequest.getUsername());
                throw new AuthenticationException("Usuário inativo") {};
            }
            
            // Gerar tokens
            logger.info("Gerando tokens JWT...");
            String accessToken = jwtTokenProvider.generateToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);
            Long expiresIn = jwtTokenProvider.getExpirationTime();
            logger.info("Tokens gerados com sucesso");
            
            // Criar informações do usuário
            MobileUserInfo userInfo = new MobileUserInfo(
                usuario.getId() != null ? usuario.getId().longValue() : null,
                usuario.getLogin(),
                usuario.getNomeCompleto(),
                usuario.getEmail(),
                usuario.getIdSetor() != null ? usuario.getIdSetor().longValue() : null,
                usuario.getNomeSetor(),
                usuario.getPerfil().name(),
                usuario.getAtivo()
            );
            
            // Registrar dispositivo se fornecido
            if (loginRequest.getDeviceId() != null) {
                registrarDispositivo(usuario.getId(), loginRequest.getDeviceId(), loginRequest.getAppVersion());
            }
            
            logger.info("=== LOGIN MOBILE CONCLUÍDO COM SUCESSO ===");
            logger.info("Usuário: {}, Perfil: {}", loginRequest.getUsername(), usuario.getPerfil());
            
            return new MobileLoginResponse(accessToken, refreshToken, expiresIn, userInfo);
            
        } catch (AuthenticationException e) {
            logger.error("Falha na autenticação mobile para usuário: {}", loginRequest.getUsername(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado durante autenticação mobile", e);
            throw new RuntimeException("Erro interno do servidor", e);
        }
    }
    
    /**
     * Valida token de acesso
     * 
     * @param token token a ser validado
     * @return true se válido
     */
    public boolean validateToken(String token) {
        try {
            return jwtTokenProvider.validateToken(token);
        } catch (Exception e) {
            logger.error("Erro ao validar token mobile", e);
            return false;
        }
    }
    
    /**
     * Obtém username do token
     * 
     * @param token token JWT
     * @return username
     */
    public String getUsernameFromToken(String token) {
        try {
            return jwtTokenProvider.getUsernameFromToken(token);
        } catch (Exception e) {
            logger.error("Erro ao extrair username do token", e);
            return null;
        }
    }
    
    /**
     * Registra dispositivo do usuário
     * 
     * @param userId ID do usuário
     * @param deviceId ID do dispositivo
     * @param appVersion versão do app
     */
    private void registrarDispositivo(Integer userId, String deviceId, String appVersion) {
        try {
            // Implementar lógica de registro de dispositivo se necessário
            logger.info("Dispositivo registrado - Usuário: {}, Device: {}, Versão: {}", 
                       userId, deviceId, appVersion);
        } catch (Exception e) {
            logger.warn("Erro ao registrar dispositivo", e);
            // Não falhar o login por causa disso
        }
    }
}