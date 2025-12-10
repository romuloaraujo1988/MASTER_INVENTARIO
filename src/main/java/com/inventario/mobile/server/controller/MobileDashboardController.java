package com.inventario.mobile.server.controller;

import com.inventario.mobile.server.dto.ApiResponse;
import com.inventario.mobile.server.service.MobileDashboardService;
import com.inventario.security.annotation.RequireConsulta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para dashboard e estatísticas mobile
 * 
 * Segurança por Role:
 * - Todos os endpoints: Qualquer usuário autenticado
 * 
 * @author Sistema de Inventário
 * @version 2.0.0
 */
@RestController
@RequestMapping("/api/mobile/dashboard")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequireConsulta // Todos os endpoints de dashboard são acessíveis a qualquer usuário autenticado
public class MobileDashboardController {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileDashboardController.class);
    
    @Autowired
    private MobileDashboardService dashboardService;
    
    /**
     * Busca estatísticas gerais do dashboard
     * GET /api/mobile/dashboard/estatisticas
     * GET /api/mobile/dashboard/stats (alias para compatibilidade)
     */
    @GetMapping({"/estatisticas", "/stats"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> buscarEstatisticas(
            @RequestParam(required = false) Integer inventarioId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "anonymous";
            
            logger.info("Buscando estatísticas do dashboard para usuário: {}", username);
            
            Map<String, Object> estatisticas = dashboardService.buscarEstatisticasGerais(inventarioId);
            
            return ResponseEntity.ok(
                    ApiResponse.success(estatisticas, "Estatísticas carregadas"));
            
        } catch (IllegalArgumentException e) {
            // Inventário não encontrado - retornar estatísticas vazias ao invés de erro
            logger.warn("Inventário não encontrado, retornando estatísticas vazias: {}", e.getMessage());
            
            Map<String, Object> estatisticasVazias = new HashMap<>();
            estatisticasVazias.put("inventarioId", inventarioId);
            estatisticasVazias.put("inventarioNome", null);
            estatisticasVazias.put("totalPatrimonios", 0);
            estatisticasVazias.put("patrimoniosColetados", 0);
            estatisticasVazias.put("patrimoniosPendentes", 0);
            estatisticasVazias.put("percentualConclusao", 0.0);
            estatisticasVazias.put("divergencias", 0);
            estatisticasVazias.put("coletoresAtivos", 0);
            estatisticasVazias.put("mensagem", "Nenhum inventário ativo encontrado");
            
            return ResponseEntity.ok(
                    ApiResponse.success(estatisticasVazias, "Nenhum inventário ativo encontrado"));
            
        } catch (Exception e) {
            logger.error("Erro ao buscar estatísticas do dashboard", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar estatísticas", "FETCH_ERROR"));
        }
    }
    
    /**
     * Busca dados de evolução de coletas (gráfico de linhas)
     * GET /api/mobile/dashboard/evolucao
     */
    @GetMapping("/evolucao")
    public ResponseEntity<ApiResponse<Map<String, Object>>> buscarEvolucao(
            @RequestParam(required = false) Integer inventarioId,
            @RequestParam(defaultValue = "30") int dias) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "anonymous";
            
            logger.info("Buscando evolução de coletas (últimos {} dias) para usuário: {}", dias, username);
            
            Map<String, Object> evolucao = dashboardService.buscarEvolucaoColetas(inventarioId, dias);
            
            return ResponseEntity.ok(
                    ApiResponse.success(evolucao, "Dados de evolução carregados"));
            
        } catch (IllegalArgumentException e) {
            logger.warn("Inventário não encontrado para evolução: {}", e.getMessage());
            Map<String, Object> evolucaoVazia = new HashMap<>();
            evolucaoVazia.put("inventarioId", inventarioId);
            evolucaoVazia.put("dias", dias);
            evolucaoVazia.put("evolucao", new HashMap<>());
            evolucaoVazia.put("totalColetas", 0);
            return ResponseEntity.ok(ApiResponse.success(evolucaoVazia, "Nenhum inventário ativo"));
        } catch (Exception e) {
            logger.error("Erro ao buscar evolução de coletas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar evolução", "FETCH_ERROR"));
        }
    }
    
    /**
     * Busca top itens mais coletados (gráfico de barras)
     * GET /api/mobile/dashboard/top-itens
     */
    @GetMapping("/top-itens")
    public ResponseEntity<ApiResponse<Map<String, Object>>> buscarTopItens(
            @RequestParam(required = false) Integer inventarioId,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "anonymous";
            
            logger.info("Buscando top {} itens para usuário: {}", limit, username);
            
            Map<String, Object> topItens = dashboardService.buscarTopItens(inventarioId, limit);
            
            return ResponseEntity.ok(
                    ApiResponse.success(topItens, "Top itens carregados"));
            
        } catch (IllegalArgumentException e) {
            logger.warn("Inventário não encontrado para top itens: {}", e.getMessage());
            Map<String, Object> topItensVazio = new HashMap<>();
            topItensVazio.put("inventarioId", inventarioId);
            topItensVazio.put("limit", limit);
            topItensVazio.put("topItens", new HashMap<>());
            topItensVazio.put("total", 0);
            return ResponseEntity.ok(ApiResponse.success(topItensVazio, "Nenhum inventário ativo"));
        } catch (Exception e) {
            logger.error("Erro ao buscar top itens", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar top itens", "FETCH_ERROR"));
        }
    }
    
    /**
     * Busca estatísticas por status de coleta (gráfico de pizza)
     * GET /api/mobile/dashboard/status
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> buscarEstatisticasPorStatus(
            @RequestParam(required = false) Integer inventarioId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "anonymous";
            
            logger.info("Buscando estatísticas por status para usuário: {}", username);
            
            Map<String, Object> estatisticas = dashboardService.buscarEstatisticasPorStatus(inventarioId);
            
            return ResponseEntity.ok(
                    ApiResponse.success(estatisticas, "Estatísticas por status carregadas"));
            
        } catch (IllegalArgumentException e) {
            logger.warn("Inventário não encontrado para status: {}", e.getMessage());
            Map<String, Object> statusVazio = new HashMap<>();
            statusVazio.put("inventarioId", inventarioId);
            statusVazio.put("statusDistribuicao", new HashMap<>());
            statusVazio.put("total", 0);
            return ResponseEntity.ok(ApiResponse.success(statusVazio, "Nenhum inventário ativo"));
        } catch (Exception e) {
            logger.error("Erro ao buscar estatísticas por status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar estatísticas por status", "FETCH_ERROR"));
        }
    }
    
    /**
     * Busca distribuição de coletas por sala (gráfico de barras horizontais)
     * GET /api/mobile/dashboard/distribuicao-sala
     */
    @GetMapping("/distribuicao-sala")
    public ResponseEntity<ApiResponse<Map<String, Object>>> buscarDistribuicaoPorSala(
            @RequestParam(required = false) Integer inventarioId,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "anonymous";
            
            logger.info("Buscando distribuição por sala (top {}) para usuário: {}", limit, username);
            
            Map<String, Object> distribuicao = dashboardService.buscarDistribuicaoPorSala(inventarioId, limit);
            
            return ResponseEntity.ok(
                    ApiResponse.success(distribuicao, "Distribuição por sala carregada"));
            
        } catch (IllegalArgumentException e) {
            logger.warn("Inventário não encontrado para distribuição por sala: {}", e.getMessage());
            Map<String, Object> distribuicaoVazia = new HashMap<>();
            distribuicaoVazia.put("inventarioId", inventarioId);
            distribuicaoVazia.put("limit", limit);
            distribuicaoVazia.put("distribuicaoPorSala", new HashMap<>());
            distribuicaoVazia.put("total", 0);
            return ResponseEntity.ok(ApiResponse.success(distribuicaoVazia, "Nenhum inventário ativo"));
        } catch (Exception e) {
            logger.error("Erro ao buscar distribuição por sala", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar distribuição por sala", "FETCH_ERROR"));
        }
    }
}
