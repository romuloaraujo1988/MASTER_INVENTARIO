package com.inventario.mobile.server.controller;

import com.inventario.mobile.server.dto.ApiResponse;
import com.inventario.mobile.server.service.MobileSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Controlador para sincronização de dados offline
 * Fornece endpoints para download de dados para o app mobile
 * 
 * @author Sistema de Inventário
 * @version 2.0.0
 */
@RestController
@RequestMapping("/api/mobile/sync")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MobileSyncController {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileSyncController.class);
    
    @Autowired
    private MobileSyncService syncService;
    
    /**
     * Sincronizar todos os dados necessários para modo offline
     * Retorna patrimônios, salas, responsáveis e inventário ativo
     * 
     * @param idInventario ID do inventário (opcional, usa o ativo se não informado)
     * @return Pacote completo de dados para sincronização
     */
    @GetMapping("/full")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sincronizacaoCompleta(
            @RequestParam(required = false) Integer idInventario) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : null;
            
            logger.info("Iniciando sincronização completa para usuário: {}, inventário: {}", 
                username, idInventario);
            
            Map<String, Object> dados = syncService.sincronizacaoCompleta(idInventario);
            
            logger.info("Sincronização completa: {} patrimônios, {} salas, {} responsáveis",
                dados.get("totalPatrimonios"),
                dados.get("totalSalas"),
                dados.get("totalResponsaveis"));
            
            return ResponseEntity.ok(
                ApiResponse.success(dados, "Sincronização completa realizada com sucesso"));
            
        } catch (Exception e) {
            logger.error("Erro na sincronização completa", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Erro na sincronização: " + e.getMessage(), "SYNC_ERROR"));
        }
    }
    
    /**
     * Sincronizar apenas patrimônios
     */
    @GetMapping("/patrimonios")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sincronizarPatrimonios(
            @RequestParam(required = false) Long ultimaAtualizacao) {
        try {
            logger.info("Sincronizando patrimônios desde: {}", ultimaAtualizacao);
            
            Map<String, Object> dados = syncService.sincronizarPatrimonios(ultimaAtualizacao);
            
            return ResponseEntity.ok(
                ApiResponse.success(dados, "Patrimônios sincronizados"));
            
        } catch (Exception e) {
            logger.error("Erro ao sincronizar patrimônios", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Erro ao sincronizar patrimônios", "SYNC_ERROR"));
        }
    }
    
    /**
     * Sincronizar apenas salas
     */
    @GetMapping("/salas")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sincronizarSalas() {
        try {
            logger.info("Sincronizando salas");
            
            Map<String, Object> dados = syncService.sincronizarSalas();
            
            return ResponseEntity.ok(
                ApiResponse.success(dados, "Salas sincronizadas"));
            
        } catch (Exception e) {
            logger.error("Erro ao sincronizar salas", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Erro ao sincronizar salas", "SYNC_ERROR"));
        }
    }
    
    /**
     * Sincronizar apenas responsáveis
     */
    @GetMapping("/responsaveis")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sincronizarResponsaveis() {
        try {
            logger.info("Sincronizando responsáveis");
            
            Map<String, Object> dados = syncService.sincronizarResponsaveis();
            
            return ResponseEntity.ok(
                ApiResponse.success(dados, "Responsáveis sincronizados"));
            
        } catch (Exception e) {
            logger.error("Erro ao sincronizar responsáveis", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Erro ao sincronizar responsáveis", "SYNC_ERROR"));
        }
    }
    
    /**
     * Verificar se há atualizações disponíveis
     */
    @GetMapping("/check-updates")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verificarAtualizacoes(
            @RequestParam Long ultimaSincronizacao) {
        try {
            logger.info("Verificando atualizações desde: {}", ultimaSincronizacao);
            
            Map<String, Object> atualizacoes = syncService.verificarAtualizacoes(ultimaSincronizacao);
            
            return ResponseEntity.ok(
                ApiResponse.success(atualizacoes, "Verificação de atualizações concluída"));
            
        } catch (Exception e) {
            logger.error("Erro ao verificar atualizações", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Erro ao verificar atualizações", "CHECK_ERROR"));
        }
    }
    
    /**
     * Obter metadados da sincronização (tamanhos, versões, etc)
     */
    @GetMapping("/metadata")
    public ResponseEntity<ApiResponse<Map<String, Object>>> obterMetadados() {
        try {
            Map<String, Object> metadata = syncService.obterMetadados();
            
            return ResponseEntity.ok(
                ApiResponse.success(metadata, "Metadados obtidos"));
            
        } catch (Exception e) {
            logger.error("Erro ao obter metadados", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Erro ao obter metadados", "METADATA_ERROR"));
        }
    }
}
