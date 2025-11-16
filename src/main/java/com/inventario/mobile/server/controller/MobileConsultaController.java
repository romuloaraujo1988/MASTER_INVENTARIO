package com.inventario.mobile.server.controller;

import com.inventario.mobile.server.dto.ApiResponse;
import com.inventario.mobile.server.dto.MobilePatrimonioDTO;
import com.inventario.mobile.server.dto.PatrimonioDetalheDTO;
import com.inventario.mobile.server.service.MobileConsultaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Controlador REST para consulta de patrimônios
 * Permite busca por código parcial ou descrição para casos de etiquetas danificadas
 * 
 * @author Sistema de Inventário
 * @version 1.0
 */
@RestController
@RequestMapping("/api/mobile/consulta")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MobileConsultaController {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileConsultaController.class);
    
    @Autowired
    private MobileConsultaService consultaService;
    
    /**
     * Busca patrimônios por código parcial
     * Útil quando a etiqueta está parcialmente danificada
     * 
     * GET /api/mobile/consulta/buscar-por-codigo?codigo=123&limit=10
     * 
     * @param codigo Código parcial do patrimônio (mínimo 2 caracteres)
     * @param limit Quantidade máxima de resultados (padrão: 10)
     * @return Lista de patrimônios encontrados
     */
    @GetMapping("/buscar-por-codigo")
    public ResponseEntity<ApiResponse<List<MobilePatrimonioDTO>>> buscarPorCodigoParcial(
            @RequestParam String codigo,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "anonymous";
            
            logger.info("Busca por código parcial: '{}' (limit: {}) - Usuário: {}", 
                    codigo, limit, username);
            
            // Validação
            if (codigo == null || codigo.trim().length() < 2) {
                logger.warn("Código muito curto: '{}'", codigo);
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Digite ao menos 2 caracteres", "VALIDATION_ERROR"));
            }
            
            if (limit < 1 || limit > 50) {
                logger.warn("Limite inválido: {}", limit);
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Limite deve estar entre 1 e 50", "VALIDATION_ERROR"));
            }
            
            // Buscar patrimônios
            List<MobilePatrimonioDTO> patrimonios = consultaService.buscarPorCodigoParcial(
                    codigo.trim(), limit);
            
            logger.info("Encontrados {} patrimônios para código '{}'", patrimonios.size(), codigo);
            
            return ResponseEntity.ok(
                    ApiResponse.success(patrimonios, 
                            String.format("Encontrados %d patrimônios", patrimonios.size())));
            
        } catch (Exception e) {
            logger.error("Erro ao buscar por código parcial: " + codigo, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar patrimônios", "SEARCH_ERROR"));
        }
    }
    
    /**
     * Busca patrimônios por descrição
     * Útil quando não se sabe o código mas conhece o tipo do item
     * 
     * GET /api/mobile/consulta/buscar-por-descricao?descricao=cadeira&limit=10
     * 
     * @param descricao Descrição ou parte da descrição (mínimo 3 caracteres)
     * @param limit Quantidade máxima de resultados (padrão: 10)
     * @return Lista de patrimônios encontrados
     */
    @GetMapping("/buscar-por-descricao")
    public ResponseEntity<ApiResponse<List<MobilePatrimonioDTO>>> buscarPorDescricao(
            @RequestParam String descricao,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "anonymous";
            
            logger.info("Busca por descrição: '{}' (limit: {}) - Usuário: {}", 
                    descricao, limit, username);
            
            // Validação
            if (descricao == null || descricao.trim().length() < 3) {
                logger.warn("Descrição muito curta: '{}'", descricao);
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Digite ao menos 3 caracteres", "VALIDATION_ERROR"));
            }
            
            if (limit < 1 || limit > 50) {
                logger.warn("Limite inválido: {}", limit);
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Limite deve estar entre 1 e 50", "VALIDATION_ERROR"));
            }
            
            // Buscar patrimônios
            List<MobilePatrimonioDTO> patrimonios = consultaService.buscarPorDescricao(
                    descricao.trim(), limit);
            
            logger.info("Encontrados {} patrimônios para descrição '{}'", 
                    patrimonios.size(), descricao);
            
            return ResponseEntity.ok(
                    ApiResponse.success(patrimonios, 
                            String.format("Encontrados %d patrimônios", patrimonios.size())));
            
        } catch (Exception e) {
            logger.error("Erro ao buscar por descrição: " + descricao, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar patrimônios", "SEARCH_ERROR"));
        }
    }
    
    /**
     * Obtém detalhes completos de um patrimônio
     * Inclui informações de sala, responsável e histórico de coletas
     * 
     * GET /api/mobile/consulta/patrimonio/{id}/detalhes
     * 
     * @param id ID do patrimônio
     * @return Detalhes completos do patrimônio
     */
    @GetMapping("/patrimonio/{id}/detalhes")
    public ResponseEntity<ApiResponse<PatrimonioDetalheDTO>> obterDetalhesCompletos(
            @PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "anonymous";
            
            logger.info("Buscando detalhes do patrimônio ID: {} - Usuário: {}", id, username);
            
            // Validação
            if (id == null || id <= 0) {
                logger.warn("ID inválido: {}", id);
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("ID do patrimônio inválido", "VALIDATION_ERROR"));
            }
            
            // Buscar detalhes
            PatrimonioDetalheDTO detalhes = consultaService.obterDetalhesCompletos(id);
            
            if (detalhes == null) {
                logger.warn("Patrimônio não encontrado: ID {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Patrimônio não encontrado", "NOT_FOUND"));
            }
            
            logger.info("Detalhes do patrimônio {} carregados com sucesso", id);
            
            return ResponseEntity.ok(
                    ApiResponse.success(detalhes, "Detalhes carregados com sucesso"));
            
        } catch (Exception e) {
            logger.error("Erro ao buscar detalhes do patrimônio: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar detalhes", "FETCH_ERROR"));
        }
    }
    
    /**
     * Busca patrimônios por múltiplos critérios
     * Permite busca combinada por código, descrição, sala, etc.
     * 
     * GET /api/mobile/consulta/buscar-avancada?termo=cadeira&salaId=10&limit=20
     * 
     * @param termo Termo de busca geral
     * @param salaId ID da sala (opcional)
     * @param responsavelId ID do responsável (opcional)
     * @param limit Quantidade máxima de resultados (padrão: 10)
     * @return Lista de patrimônios encontrados
     */
    @GetMapping("/buscar-avancada")
    public ResponseEntity<ApiResponse<List<MobilePatrimonioDTO>>> buscarAvancada(
            @RequestParam String termo,
            @RequestParam(required = false) Integer salaId,
            @RequestParam(required = false) Integer responsavelId,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "anonymous";
            
            logger.info("Busca avançada: termo='{}', salaId={}, responsavelId={}, limit={} - Usuário: {}", 
                    termo, salaId, responsavelId, limit, username);
            
            // Validação
            if (termo == null || termo.trim().length() < 2) {
                logger.warn("Termo muito curto: '{}'", termo);
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Digite ao menos 2 caracteres", "VALIDATION_ERROR"));
            }
            
            if (limit < 1 || limit > 50) {
                logger.warn("Limite inválido: {}", limit);
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Limite deve estar entre 1 e 50", "VALIDATION_ERROR"));
            }
            
            // Buscar patrimônios
            List<MobilePatrimonioDTO> patrimonios = consultaService.buscarAvancada(
                    termo.trim(), salaId, responsavelId, limit);
            
            logger.info("Busca avançada encontrou {} patrimônios", patrimonios.size());
            
            return ResponseEntity.ok(
                    ApiResponse.success(patrimonios, 
                            String.format("Encontrados %d patrimônios", patrimonios.size())));
            
        } catch (Exception e) {
            logger.error("Erro na busca avançada", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar patrimônios", "SEARCH_ERROR"));
        }
    }
    
    /**
     * Endpoint de health check para verificar disponibilidade do serviço
     * 
     * GET /api/mobile/consulta/health
     * 
     * @return Status do serviço
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        logger.debug("Health check do serviço de consulta");
        return ResponseEntity.ok(
                ApiResponse.success("OK", "Serviço de consulta operacional"));
    }
}
