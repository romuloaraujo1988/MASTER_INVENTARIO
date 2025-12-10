package com.inventario.mobile.server.controller;

import com.inventario.mobile.server.dto.ApiResponse;
import com.inventario.dao.PatrimonioDAO;
import com.inventario.security.annotation.RequireColetor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador REST para descrições de patrimônios (itens sem patrimônio)
 * 
 * Segurança por Role:
 * - Todos os endpoints: ADMIN, SUPERVISOR ou COLETOR
 * 
 * @author Sistema de Inventário
 * @version 2.0.0
 */
@RestController
@RequestMapping("/api/mobile/descricoes")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequireColetor // Descrições são usadas para coleta, então requer role COLETOR ou superior
public class MobileDescricaoController {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileDescricaoController.class);
    
    @Autowired
    private PatrimonioDAO patrimonioDAO;
    
    /**
     * Listar descrições únicas de patrimônios
     * Retorna lista de descrições agrupadas com contagem
     * 
     * @return lista de descrições
     */
    /**
     * Listar descrições únicas de patrimônios
     * 
     * OTIMIZADO: Usa GROUP BY no SQL ao invés de carregar todos os patrimônios
     * ANTES: findAll() + stream().groupBy() (vazamento de memória)
     * DEPOIS: Query SQL com GROUP BY (retorna apenas descrições únicas)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listarDescricoes() {
        try {
            long startTime = System.currentTimeMillis();
            logger.info("Buscando descrições únicas (OTIMIZADO - GROUP BY no SQL)");
            
            // OTIMIZAÇÃO: Buscar descrições agrupadas diretamente no banco
            List<Map<String, Object>> descricoes = patrimonioDAO.buscarDescricoesAgrupadas();
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("✓ {} descrições únicas em {}ms", descricoes.size(), duration);
            
            return ResponseEntity.ok(
                    ApiResponse.success(descricoes, 
                            String.format("%d descrição(ões) em %dms", descricoes.size(), duration)));
            
        } catch (Exception e) {
            logger.error("Erro ao buscar descrições", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar descrições", "FETCH_ERROR"));
        }
    }
    
    /**
     * Buscar descrições por termo
     * 
     * OTIMIZADO: Usa GROUP BY + LIKE no SQL (máximo 100 resultados)
     * 
     * @param termo termo de busca
     * @return lista de descrições filtradas
     */
    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> buscarDescricoes(
            @RequestParam String termo) {
        try {
            long startTime = System.currentTimeMillis();
            logger.info("Buscando descrições com termo: {} (OTIMIZADO)", termo);
            
            // OTIMIZAÇÃO: Buscar descrições agrupadas por termo diretamente no banco
            List<Map<String, Object>> descricoes = patrimonioDAO.buscarDescricoesAgrupadasPorTermo(termo);
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("✓ {} descrições para '{}' em {}ms", descricoes.size(), termo, duration);
            
            return ResponseEntity.ok(
                    ApiResponse.success(descricoes, 
                            String.format("%d descrição(ões) em %dms", descricoes.size(), duration)));
            
        } catch (Exception e) {
            logger.error("Erro ao buscar descrições por termo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar descrições", "FETCH_ERROR"));
        }
    }
    
    /**
     * Listar descrições únicas de patrimônios NÃO COLETADOS
     * 
     * OTIMIZADO: Usa NOT EXISTS no SQL ao invés de carregar todos os patrimônios
     * ANTES: findAll() + stream().filter() (vazamento de memória)
     * DEPOIS: Query SQL com NOT EXISTS (máximo 200 resultados)
     * 
     * CORREÇÃO 01/12/2025: Retorna List<String> para compatibilidade com app Android
     * 
     * @param idInventario ID do inventário ativo
     * @return lista de descrições não coletadas (apenas strings)
     */
    @GetMapping("/nao-coletadas")
    public ResponseEntity<ApiResponse<List<String>>> listarDescricoesNaoColetadas(
            @RequestParam(required = false) Integer idInventario) {
        try {
            long startTime = System.currentTimeMillis();
            logger.info("Buscando descrições não coletadas (OTIMIZADO - NOT EXISTS no SQL)");
            
            // Obter inventário ativo se não informado
            if (idInventario == null) {
                try {
                    com.inventario.dao.InventarioDAO inventarioDAO = new com.inventario.dao.InventarioDAO();
                    com.inventario.model.Inventario inventarioAtivo = inventarioDAO.buscarInventarioAtivo();
                    if (inventarioAtivo != null) {
                        idInventario = inventarioAtivo.getId();
                    }
                } catch (Exception e) {
                    logger.warn("Erro ao buscar inventário ativo: {}", e.getMessage());
                }
            }
            
            if (idInventario == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Nenhum inventário ativo encontrado", "NO_INVENTORY"));
            }
            
            // OTIMIZAÇÃO: Buscar descrições não coletadas diretamente no banco
            List<Map<String, Object>> descricoesMap = patrimonioDAO.buscarDescricoesNaoColetadasAgrupadas(idInventario);
            
            // CORREÇÃO: Extrair apenas as descrições (strings) para compatibilidade com app Android
            List<String> descricoes = descricoesMap.stream()
                    .map(m -> (String) m.get("descricao"))
                    .filter(d -> d != null && !d.trim().isEmpty())
                    .collect(Collectors.toList());
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("✓ {} descrições não coletadas em {}ms", descricoes.size(), duration);
            
            return ResponseEntity.ok(
                    ApiResponse.success(descricoes, 
                            String.format("%d descrição(ões) não coletada(s) em %dms", descricoes.size(), duration)));
            
        } catch (Exception e) {
            logger.error("Erro ao buscar descrições não coletadas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar descrições não coletadas", "FETCH_ERROR"));
        }
    }
}
