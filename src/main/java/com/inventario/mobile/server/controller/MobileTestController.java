package com.inventario.mobile.server.controller;

import com.inventario.mobile.server.dto.ApiResponse;
import com.inventario.mobile.server.util.NetworkUtils;
import com.inventario.dao.InventarioDAO;
import com.inventario.dao.ColetaDAO;
import com.inventario.dao.PatrimonioDAORefactored;
import com.inventario.model.Inventario;
import com.inventario.model.Coleta;
import com.inventario.model.Patrimonio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador de teste para verificar conectividade mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/mobile/test")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MobileTestController {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileTestController.class);
    
    @Autowired
    private InventarioDAO inventarioDAO;
    
    @Autowired
    private ColetaDAO coletaDAO;
    
    @Autowired
    private PatrimonioDAORefactored patrimonioDAO;
    
    /**
     * Endpoint de teste simples - não requer autenticação
     * Use para verificar se o smartphone consegue se conectar ao servidor
     */
    @GetMapping("/ping")
    public ResponseEntity<ApiResponse<Map<String, Object>>> ping() {
        logger.info("╔════════════════════════════════════════════════════════════════");
        logger.info("║ PING RECEBIDO DO SMARTPHONE!");
        logger.info("╚════════════════════════════════════════════════════════════════");
        
        Map<String, Object> data = new HashMap<>();
        data.put("message", "Pong! Servidor está funcionando");
        data.put("timestamp", LocalDateTime.now().toString());
        data.put("server", "Sistema de Inventário - API Mobile");
        data.put("version", "1.2.0");
        
        return ResponseEntity.ok(
            ApiResponse.success(data, "Conexão estabelecida com sucesso")
        );
    }
    
    /**
     * Endpoint POST de teste - não requer autenticação
     * Use para verificar se o smartphone consegue enviar dados
     */
    @PostMapping("/echo")
    public ResponseEntity<ApiResponse<Map<String, Object>>> echo(@RequestBody Map<String, Object> data) {
        logger.info("╔════════════════════════════════════════════════════════════════");
        logger.info("║ ECHO RECEBIDO DO SMARTPHONE!");
        logger.info("║ Dados recebidos: {}", data);
        logger.info("╚════════════════════════════════════════════════════════════════");
        
        Map<String, Object> response = new HashMap<>();
        response.put("received", data);
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("message", "Dados recebidos com sucesso");
        
        return ResponseEntity.ok(
            ApiResponse.success(response, "Echo realizado com sucesso")
        );
    }
    
    /**
     * Endpoint para testar autenticação básica
     * Retorna informações sobre o que o servidor espera receber
     */
    @GetMapping("/login-info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> loginInfo() {
        logger.info("╔════════════════════════════════════════════════════════════════");
        logger.info("║ INFORMAÇÕES DE LOGIN SOLICITADAS");
        logger.info("╚════════════════════════════════════════════════════════════════");
        
        Map<String, Object> info = new HashMap<>();
        String serverBaseUrl = NetworkUtils.construirURLBase();
        String fullLoginUrl = serverBaseUrl + "/api/mobile/auth/login";
        
        info.put("serverBaseUrl", serverBaseUrl);
        info.put("loginEndpoint", "/api/mobile/auth/login");
        info.put("fullLoginUrl", fullLoginUrl);
        info.put("method", "POST");
        info.put("contentType", "application/json");
        
        Map<String, Object> body = new HashMap<>();
        body.put("username", "admin");
        body.put("password", "admin");
        body.put("deviceId", "opcional");
        body.put("appVersion", "opcional");
        
        info.put("exampleBody", body);
        info.put("requiredFields", new String[]{"username", "password"});
        info.put("optionalFields", new String[]{"deviceId", "appVersion"});
        
        Map<String, String> instructions = new HashMap<>();
        instructions.put("step1", "Configure a URL base no app: " + serverBaseUrl);
        instructions.put("step2", "O app deve fazer POST para: /api/mobile/auth/login");
        instructions.put("step3", "URL completa: " + fullLoginUrl);
        instructions.put("step4", "Envie JSON com username e password");
        
        info.put("instructions", instructions);
        
        logger.info("Informações de login enviadas com sucesso para IP: {}", NetworkUtils.obterIPLocal());
        
        return ResponseEntity.ok(
            ApiResponse.success(info, "Informações de login")
        );
    }
    
    /**
     * Endpoint que mostra a configuração atual do servidor
     */
    @GetMapping("/config")
    public ResponseEntity<ApiResponse<Map<String, Object>>> config() {
        logger.info("╔════════════════════════════════════════════════════════════════");
        logger.info("║ CONFIGURAÇÃO DO SERVIDOR SOLICITADA");
        logger.info("╚════════════════════════════════════════════════════════════════");
        
        Map<String, Object> config = new HashMap<>();
        String serverIp = NetworkUtils.obterIPLocal();
        config.put("serverIp", serverIp);
        config.put("serverPort", "8081");
        config.put("contextPath", "/inventario");
        config.put("apiBasePath", "/api/mobile");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("ping", "GET /api/mobile/test/ping");
        endpoints.put("echo", "POST /api/mobile/test/echo");
        endpoints.put("loginInfo", "GET /api/mobile/test/login-info");
        endpoints.put("config", "GET /api/mobile/test/config");
        endpoints.put("login", "POST /api/mobile/auth/login");
        endpoints.put("health", "GET /actuator/health");
        
        config.put("availableEndpoints", endpoints);
        
        Map<String, String> urls = new HashMap<>();
        urls.put("baseUrl", "http://" + serverIp + ":8081/inventario");
        urls.put("apiUrl", "http://" + serverIp + ":8081/inventario/api/mobile");
        urls.put("loginUrl", "http://" + serverIp + ":8081/inventario/api/mobile/auth/login");
        urls.put("pingUrl", "http://" + serverIp + ":8081/inventario/api/mobile/test/ping");
        
        config.put("fullUrls", urls);
        
        return ResponseEntity.ok(
            ApiResponse.success(config, "Configuração do servidor")
        );
    }
    
    /**
     * Endpoint para verificar inventários ativos no banco de dados
     */
    @GetMapping("/inventarios-ativos")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verificarInventariosAtivos() {
        logger.info("╔════════════════════════════════════════════════════════════════");
        logger.info("║ VERIFICANDO INVENTÁRIOS ATIVOS");
        logger.info("╚════════════════════════════════════════════════════════════════");
        
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            // Buscar inventário com status EM_ANDAMENTO
            Inventario inventarioAtivo = inventarioDAO.buscarInventarioPorStatus("EM_ANDAMENTO");
            
            resultado.put("existeInventarioAtivo", inventarioAtivo != null);
            
            if (inventarioAtivo != null) {
                Map<String, Object> inventarioInfo = new HashMap<>();
                
                inventarioInfo.put("id", inventarioAtivo.getId());
                inventarioInfo.put("nome", inventarioAtivo.getNome());
                inventarioInfo.put("status", inventarioAtivo.getStatusInventario());
                inventarioInfo.put("dataInicio", inventarioAtivo.getDataInicio());
                
                resultado.put("inventarioAtivo", inventarioInfo);
                logger.info("Inventário ativo encontrado: ID={}, Nome={}", inventarioAtivo.getId(), inventarioAtivo.getNome());
            } else {
                resultado.put("inventarioAtivo", null);
                logger.warn("NENHUM INVENTÁRIO ATIVO ENCONTRADO!");
            }
            
            return ResponseEntity.ok(
                ApiResponse.success(resultado, "Verificação de inventários ativos concluída")
            );
            
        } catch (Exception e) {
            logger.error("Erro ao verificar inventários ativos: {}", e.getMessage(), e);
            resultado.put("erro", e.getMessage());
            resultado.put("existeInventarioAtivo", false);
            
            return ResponseEntity.ok(
                ApiResponse.success(resultado, "Erro ao verificar inventários ativos")
            );
        }
    }
    
    /**
     * Endpoint para verificar se um patrimônio foi coletado
     * GET /api/mobile/test/patrimonio/{numero}/coleta
     */
    @GetMapping("/patrimonio/{numero}/coleta")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verificarColetaPatrimonio(@PathVariable String numero) {
        logger.info("=== VERIFICANDO COLETA DO PATRIMÔNIO: {} ===", numero);
        
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            // 1. Buscar o patrimônio pelo número
            Patrimonio patrimonio = patrimonioDAO.buscarPorNumero(numero);
            
            if (patrimonio == null) {
                resultado.put("patrimonioEncontrado", false);
                resultado.put("erro", "Patrimônio não encontrado");
                logger.warn("Patrimônio {} não encontrado", numero);
                return ResponseEntity.ok(
                    ApiResponse.success(resultado, "Patrimônio não encontrado")
                );
            }
            
            resultado.put("patrimonioEncontrado", true);
            Map<String, Object> patrimonioInfo = new HashMap<>();
            patrimonioInfo.put("id", patrimonio.getId());
            patrimonioInfo.put("numero", patrimonio.getNumero());
            patrimonioInfo.put("descricao", patrimonio.getDescricao());
            resultado.put("patrimonio", patrimonioInfo);
            
            // 2. Buscar inventário ativo
            Inventario inventarioAtivo = inventarioDAO.buscarInventarioPorStatus("EM_ANDAMENTO");
            
            if (inventarioAtivo == null) {
                resultado.put("inventarioAtivo", false);
                resultado.put("foiColetado", false);
                resultado.put("motivo", "Nenhum inventário ativo encontrado");
                logger.warn("Nenhum inventário ativo encontrado");
                return ResponseEntity.ok(
                    ApiResponse.success(resultado, "Nenhum inventário ativo")
                );
            }
            
            resultado.put("inventarioAtivo", true);
            Map<String, Object> inventarioInfo = new HashMap<>();
            inventarioInfo.put("id", inventarioAtivo.getId());
            inventarioInfo.put("nome", inventarioAtivo.getNome());
            resultado.put("inventarioInfo", inventarioInfo);
            
            // 3. Verificar se existe coleta para este patrimônio no inventário ativo
            boolean foiColetado = coletaDAO.coletaExiste(inventarioAtivo.getId(), patrimonio.getId());
            
            resultado.put("foiColetado", foiColetado);
            
            if (foiColetado) {
                // Se foi coletado, buscar os detalhes da coleta
                List<Coleta> coletasDoPatrimonio = coletaDAO.buscarPorPatrimonio(patrimonio.getId());
                Coleta coletaDoInventario = null;
                
                // Encontrar a coleta específica deste inventário
                for (Coleta coleta : coletasDoPatrimonio) {
                    if (coleta.getIdInventario() == inventarioAtivo.getId()) {
                        coletaDoInventario = coleta;
                        break;
                    }
                }
                
                if (coletaDoInventario != null) {
                    Map<String, Object> coletaInfo = new HashMap<>();
                    coletaInfo.put("id", coletaDoInventario.getId());
                    coletaInfo.put("dataColeta", coletaDoInventario.getDataColeta());
                    coletaInfo.put("idColetor", coletaDoInventario.getIdColetor());
                    coletaInfo.put("observacoes", coletaDoInventario.getObservacaoColeta());
                    coletaInfo.put("statusColeta", coletaDoInventario.getStatusColeta());
                    resultado.put("coleta", coletaInfo);
                }
                
                logger.info("Patrimônio {} FOI COLETADO no inventário {}", numero, inventarioAtivo.getId());
            } else {
                resultado.put("motivo", "Nenhuma coleta encontrada para este patrimônio no inventário ativo");
                logger.warn("Patrimônio {} NÃO FOI COLETADO no inventário {}", numero, inventarioAtivo.getId());
            }
            
            return ResponseEntity.ok(
                ApiResponse.success(resultado, "Verificação de coleta concluída")
            );
            
        } catch (Exception e) {
            logger.error("Erro ao verificar coleta do patrimônio {}: {}", numero, e.getMessage(), e);
            resultado.put("erro", e.getMessage());
            resultado.put("foiColetado", false);
            
            return ResponseEntity.status(500).body(
                ApiResponse.error("Erro interno do servidor: " + e.getMessage())
            );
        }
    }
}
