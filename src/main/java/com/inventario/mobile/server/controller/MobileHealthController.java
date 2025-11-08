package com.inventario.mobile.server.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para health check da API mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/mobile")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MobileHealthController {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileHealthController.class);
    
    /**
     * Endpoint de health check
     * 
     * @return status do servidor
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("HEALTH CHECK ENDPOINT CHAMADO");
        logger.info("Timestamp: {}", LocalDateTime.now());
        logger.info("═══════════════════════════════════════════════════════════");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "SIHCP Mobile API");
        response.put("version", "1.0.0");
        response.put("timestamp", LocalDateTime.now().toString());
        
        logger.info("Health check respondido com sucesso");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint de teste simples
     * 
     * @return mensagem de teste
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        logger.info("Endpoint de teste chamado");
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "API Mobile funcionando corretamente");
        response.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }
}
