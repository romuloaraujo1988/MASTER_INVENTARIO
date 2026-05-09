package com.inventario.sihcp.mobile.server.service;

import com.inventario.sihcp.mobile.server.dto.MobileLoginRequest;
import com.inventario.sihcp.mobile.server.dto.MobileLoginResponse;
import com.inventario.sihcp.mobile.server.dto.MobileUserInfo;
import com.inventario.sihcp.mobile.server.dto.MobileInventarioInfo;
import com.inventario.sihcp.model.Usuario;
import com.inventario.sihcp.model.Inventario;
import com.inventario.sihcp.service.UsuarioService;
import com.inventario.sihcp.dao.InventarioDAO;
import com.inventario.sihcp.dao.ColetaDAO;
import com.inventario.sihcp.dao.PatrimonioDAO;
import com.inventario.sihcp.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.ConcurrentHashMap;

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
    
    // Blacklist de tokens invalidados por logout (token -> timestamp de expiração)
    private final ConcurrentHashMap<String, Long> tokenBlacklist = new ConcurrentHashMap<>();
    
    /**
     * Autentica usuário mobile
     * 
     * @param loginRequest dados de login
     * @return resposta com token e informações do usuário
     * @throws AuthenticationException se credenciais inválidas
     */
    public MobileLoginResponse authenticateUser(MobileLoginRequest loginRequest) {
        try {
            logger.debug("Autenticação mobile para usuário: {}", loginRequest.getUsername());
            
            // Autenticar usuário
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
            
            // Buscar dados completos do usuário
            Usuario usuario = usuarioService.buscarPorUsername(loginRequest.getUsername());
            
            if (usuario == null) {
                logger.error("Usuário não encontrado: {}", loginRequest.getUsername());
                throw new AuthenticationException("Usuário não encontrado") {};
            }
            
            logger.debug("Usuário encontrado - ID: {}, Ativo: {}", usuario.getId(), usuario.getAtivo());
            
            if (!usuario.getAtivo()) {
                logger.error("Usuário inativo: {}", loginRequest.getUsername());
                throw new AuthenticationException("Usuário inativo") {};
            }
            
            // Gerar tokens
            String accessToken = jwtTokenProvider.generateToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);
            Long expiresIn = jwtTokenProvider.getExpirationTime();
            
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
            
            // ✅ CORREÇÃO: Registrar data do último acesso e resetar tentativas
            usuarioService.registrarAcesso(usuario);
            
            // Buscar inventário ativo para retornar junto com o login
            MobileInventarioInfo inventarioInfo = buscarInventarioAtivo();
            
            logger.debug("Login mobile concluído: {} ({})", loginRequest.getUsername(), usuario.getPerfil());
            
            return new MobileLoginResponse(accessToken, refreshToken, expiresIn, userInfo, inventarioInfo);
            
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
            // Verificar blacklist primeiro (tokens invalidados por logout)
            if (isTokenBlacklisted(token)) {
                logger.debug("Token rejeitado: está na blacklist (logout realizado)");
                return false;
            }
            return jwtTokenProvider.validateToken(token);
        } catch (Exception e) {
            logger.error("Erro ao validar token mobile", e);
            return false;
        }
    }

    /**
     * Verifica se um token foi invalidado por logout.
     *
     * @param token token JWT a verificar
     * @return true se o token está na blacklist e ainda não expirou naturalmente
     */
    public boolean isTokenBlacklisted(String token) {
        Long expiresAt = tokenBlacklist.get(token);
        if (expiresAt == null) {
            return false;
        }
        if (System.currentTimeMillis() > expiresAt) {
            // Token já expirou naturalmente — remover da blacklist (limpeza lazy)
            tokenBlacklist.remove(token);
            return false;
        }
        return true;
    }

    /**
     * Adiciona um token à blacklist, invalidando-o até sua expiração natural.
     * Chamado pelo endpoint de logout.
     *
     * @param token token JWT a invalidar
     */
    public void invalidateToken(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        try {
            // Calcular quando o token expira para definir o TTL da blacklist
            java.util.Date expirationDate = jwtTokenProvider.getExpirationDateFromToken(token);
            if (expirationDate != null && expirationDate.getTime() > System.currentTimeMillis()) {
                tokenBlacklist.put(token, expirationDate.getTime());
                logger.info("Token adicionado à blacklist. Expira em: {}", expirationDate);
            }
        } catch (Exception e) {
            // Se não conseguir extrair a expiração, usar TTL padrão de 24h
            long defaultTtl = System.currentTimeMillis() + (24 * 60 * 60 * 1000L);
            tokenBlacklist.put(token, defaultTtl);
            logger.warn("Não foi possível extrair expiração do token, usando TTL padrão de 24h: {}",
                    e.getMessage());
        }
        // Limpeza lazy de tokens expirados
        cleanExpiredTokens();
    }

    /**
     * Remove tokens expirados da blacklist (limpeza lazy).
     * Chamado a cada invalidação para evitar acúmulo de memória.
     */
    private void cleanExpiredTokens() {
        long now = System.currentTimeMillis();
        tokenBlacklist.entrySet().removeIf(entry -> entry.getValue() <= now);
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
     * Renova access token usando refresh token
     * 
     * @param refreshToken refresh token válido
     * @return novo access token
     * @throws AuthenticationException se refresh token inválido
     */
    public MobileLoginResponse refreshAccessToken(String refreshToken) {
        try {
            logger.debug("Refresh token solicitado");
            
            // Validar refresh token
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                logger.error("Refresh token inválido ou expirado");
                throw new AuthenticationException("Refresh token inválido") {};
            }
            
            // Extrair username do refresh token
            String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
            
            if (username == null) {
                logger.error("Não foi possível extrair username do refresh token");
                throw new AuthenticationException("Token inválido") {};
            }
            
            // Buscar usuário
            Usuario usuario = usuarioService.buscarPorUsername(username);
            
            if (usuario == null) {
                logger.error("Usuário não encontrado: {}", username);
                throw new AuthenticationException("Usuário não encontrado") {};
            }
            
            if (!usuario.getAtivo()) {
                logger.error("Usuário inativo: {}", username);
                throw new AuthenticationException("Usuário inativo") {};
            }
            
            // Criar nova autenticação
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                username, 
                null, 
                new java.util.ArrayList<>()
            );
            
            // Gerar novo access token
            String newAccessToken = jwtTokenProvider.generateToken(authentication);
            Long expiresIn = jwtTokenProvider.getExpirationTime();
            
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
            
            logger.debug("Refresh token concluído para: {}", username);
            
            // Retornar com o mesmo refresh token (não precisa renovar)
            return new MobileLoginResponse(newAccessToken, refreshToken, expiresIn, userInfo);
            
        } catch (AuthenticationException e) {
            logger.error("Falha no refresh token", e);
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado durante refresh token", e);
            throw new RuntimeException("Erro interno do servidor", e);
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
            logger.debug("Dispositivo registrado - Usuário: {}, Device: {}", userId, deviceId);
        } catch (Exception e) {
            logger.warn("Erro ao registrar dispositivo", e);
            // Não falhar o login por causa disso
        }
    }
    
    /**
     * Busca o inventário ativo (em andamento) para retornar no login
     * Isso permite que o app salve o ID correto do inventário localmente
     * 
     * @return informações do inventário ativo ou null se não houver
     */
    private MobileInventarioInfo buscarInventarioAtivo() {
        try {
            InventarioDAO inventarioDAO = new InventarioDAO();
            Inventario inventario = inventarioDAO.buscarInventarioAtivo();
            
            if (inventario == null) {
                logger.warn("Nenhum inventário ativo encontrado no sistema");
                return null;
            }
            
            // Buscar estatísticas do inventário
            PatrimonioDAO patrimonioDAO = new PatrimonioDAO();
            ColetaDAO coletaDAO = new ColetaDAO();
            
            int totalPatrimonios = patrimonioDAO.contarPatrimoniosAtivos();
            int patrimoniosColetados = coletaDAO.contarColetasPorInventario(inventario.getId());
            double percentualConclusao = totalPatrimonios > 0 
                    ? (patrimoniosColetados * 100.0) / totalPatrimonios 
                    : 0.0;
            
            MobileInventarioInfo info = new MobileInventarioInfo(
                inventario.getId(),
                inventario.getNome(),
                inventario.getStatusInventario() != null ? inventario.getStatusInventario() : "EM_ANDAMENTO",
                inventario.getAno(),
                totalPatrimonios,
                patrimoniosColetados,
                Math.round(percentualConclusao * 100.0) / 100.0
            );
            
            logger.debug("Inventário ativo: ID={}, Progresso={}%", info.getId(), info.getPercentualConclusao());
            
            return info;
            
        } catch (Exception e) {
            logger.error("Erro ao buscar inventário ativo", e);
            // Não falhar o login por causa disso
            return null;
        }
    }
}