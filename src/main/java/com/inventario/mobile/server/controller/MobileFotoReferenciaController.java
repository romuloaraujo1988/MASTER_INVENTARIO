package com.inventario.mobile.server.controller;

import com.inventario.mobile.server.dto.ApiResponse;
import com.inventario.mobile.server.dto.MobileFotoReferenciaDTO;
import com.inventario.mobile.server.dto.PagedResponse;
import com.inventario.mobile.server.service.MobileFotoReferenciaService;
import com.inventario.security.annotation.RequireConsulta;
import com.inventario.security.annotation.RequireColetor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para operações de Foto de Referência na API Mobile.
 * 
 * Fornece endpoints para sincronização de fotos de referência entre
 * servidor e app Android, com suporte a delta sync e paginação.
 * 
 * Segurança por Role:
 * - Endpoints de consulta: Qualquer usuário autenticado (CONSULTA+)
 * - Endpoints de sincronização: Coletores e acima (COLETOR+)
 * 
 * @author Sistema de Inventário
 * @since 2.9.0
 */
@RestController
@RequestMapping("/api/mobile/fotos-referencia")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MobileFotoReferenciaController {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileFotoReferenciaController.class);
    
    @Autowired
    private MobileFotoReferenciaService fotoReferenciaService;
    
    /**
     * Lista fotos de referência com paginação e delta sync.
     * 
     * GET /api/mobile/fotos-referencia?ultimaAtualizacao=1234567890&page=0&size=20
     * 
     * @param ultimaAtualizacao Timestamp da última sincronização (opcional)
     * @param page Número da página (0-indexed, padrão: 0)
     * @param size Tamanho da página (padrão: 20, máximo: 50)
     * @return Resposta paginada com fotos de referência
     */
    @GetMapping
    @RequireColetor
    public ResponseEntity<ApiResponse<PagedResponse<MobileFotoReferenciaDTO>>> listarFotos(
            @RequestParam(required = false) Long ultimaAtualizacao,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth != null ? auth.getName() : "anonymous";
            
            logger.info("Listando fotos de referência - ultimaAtualizacao: {}, page: {}, size: {}, usuário: {}",
                ultimaAtualizacao, page, size, username);
            
            PagedResponse<MobileFotoReferenciaDTO> response = 
                fotoReferenciaService.buscarFotosAtualizadas(ultimaAtualizacao, page, size);
            
            logger.info("✓ Retornando {} fotos (página {} de {})", 
                response.getContent().size(), page + 1, response.getTotalPages());
            
            return ResponseEntity.ok(
                ApiResponse.success(response, 
                    String.format("Página %d de %d (%d fotos)", 
                        page + 1, response.getTotalPages(), response.getContent().size())));
            
        } catch (SQLException e) {
            logger.error("Erro SQL ao listar fotos de referência: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erro de banco de dados: " + e.getMessage(), "DATABASE_ERROR"));
        } catch (Exception e) {
            logger.error("Erro ao listar fotos de referência: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erro ao listar fotos: " + e.getMessage(), "FETCH_ERROR"));
        }
    }
    
    /**
     * Busca foto de referência por ID.
     * 
     * GET /api/mobile/fotos-referencia/{id}
     * 
     * @param id ID da foto
     * @return DTO da foto ou 404 se não encontrada
     */
    @GetMapping("/{id}")
    @RequireConsulta
    public ResponseEntity<ApiResponse<MobileFotoReferenciaDTO>> buscarPorId(@PathVariable Integer id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth != null ? auth.getName() : "anonymous";
            
            logger.info("Buscando foto de referência por ID: {} para usuário: {}", id, username);
            
            MobileFotoReferenciaDTO foto = fotoReferenciaService.buscarPorId(id);
            
            if (foto != null) {
                logger.info("✓ Foto {} encontrada", id);
                return ResponseEntity.ok(ApiResponse.success(foto, "Foto encontrada"));
            } else {
                logger.info("Foto {} não encontrada", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Foto não encontrada", "NOT_FOUND"));
            }
            
        } catch (SQLException e) {
            logger.error("Erro SQL ao buscar foto por ID: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erro de banco de dados: " + e.getMessage(), "DATABASE_ERROR"));
        } catch (Exception e) {
            logger.error("Erro ao buscar foto por ID: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erro ao buscar foto: " + e.getMessage(), "FETCH_ERROR"));
        }
    }
    
    /**
     * Busca foto de referência por descrição.
     * A descrição é normalizada automaticamente antes da busca.
     * 
     * GET /api/mobile/fotos-referencia/descricao?q=CADEIRA%20GIRATORIA
     * 
     * @param q Descrição do patrimônio (URL encoded)
     * @return DTO da foto ou 404 se não encontrada
     */
    @GetMapping("/descricao")
    @RequireConsulta
    public ResponseEntity<ApiResponse<MobileFotoReferenciaDTO>> buscarPorDescricao(
            @RequestParam("q") String descricao) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth != null ? auth.getName() : "anonymous";
            
            // Decodificar descrição (pode vir URL encoded)
            String descricaoDecodificada = java.net.URLDecoder.decode(descricao, "UTF-8");
            
            logger.info("Buscando foto por descrição: '{}' para usuário: {}", descricaoDecodificada, username);
            
            MobileFotoReferenciaDTO foto = fotoReferenciaService.buscarPorDescricao(descricaoDecodificada);
            
            if (foto != null) {
                logger.info("✓ Foto encontrada para descrição: '{}'", descricaoDecodificada);
                return ResponseEntity.ok(ApiResponse.success(foto, "Foto encontrada"));
            } else {
                logger.info("Foto não encontrada para descrição: '{}'", descricaoDecodificada);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Foto não encontrada para esta descrição", "NOT_FOUND"));
            }
            
        } catch (SQLException e) {
            logger.error("Erro SQL ao buscar foto por descrição: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erro de banco de dados: " + e.getMessage(), "DATABASE_ERROR"));
        } catch (Exception e) {
            logger.error("Erro ao buscar foto por descrição: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erro ao buscar foto: " + e.getMessage(), "FETCH_ERROR"));
        }
    }
    
    /**
     * Busca múltiplas fotos por lista de descrições.
     * Útil para pré-carregar fotos de uma lista de patrimônios.
     * 
     * POST /api/mobile/fotos-referencia/batch
     * Body: ["CADEIRA GIRATORIA", "MESA DE ESCRITORIO", ...]
     * 
     * @param descricoes Lista de descrições
     * @return Mapa de descrição normalizada -> DTO
     */
    @PostMapping("/batch")
    @RequireColetor
    public ResponseEntity<ApiResponse<Map<String, MobileFotoReferenciaDTO>>> buscarPorDescricoes(
            @RequestBody List<String> descricoes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth != null ? auth.getName() : "anonymous";
            
            logger.info("Buscando fotos para {} descrições para usuário: {}", 
                descricoes != null ? descricoes.size() : 0, username);
            
            if (descricoes == null || descricoes.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lista de descrições vazia", "INVALID_INPUT"));
            }
            
            // Limitar quantidade de descrições por requisição
            if (descricoes.size() > 100) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Máximo de 100 descrições por requisição", "TOO_MANY_ITEMS"));
            }
            
            Map<String, MobileFotoReferenciaDTO> fotos = fotoReferenciaService.buscarPorDescricoes(descricoes);
            
            logger.info("✓ Encontradas {} fotos de {} descrições", fotos.size(), descricoes.size());
            
            return ResponseEntity.ok(
                ApiResponse.success(fotos, 
                    String.format("Encontradas %d fotos de %d descrições", fotos.size(), descricoes.size())));
            
        } catch (SQLException e) {
            logger.error("Erro SQL ao buscar fotos em batch: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erro de banco de dados: " + e.getMessage(), "DATABASE_ERROR"));
        } catch (Exception e) {
            logger.error("Erro ao buscar fotos em batch: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erro ao buscar fotos: " + e.getMessage(), "FETCH_ERROR"));
        }
    }
    
    /**
     * Verifica se há atualizações disponíveis desde um timestamp.
     * Útil para verificar se precisa sincronizar antes de baixar.
     * 
     * GET /api/mobile/fotos-referencia/check-updates?ultimaAtualizacao=1234567890
     * 
     * @param ultimaAtualizacao Timestamp da última sincronização
     * @return Informações sobre atualizações disponíveis
     */
    @GetMapping("/check-updates")
    @RequireColetor
    public ResponseEntity<ApiResponse<Map<String, Object>>> verificarAtualizacoes(
            @RequestParam(required = false) Long ultimaAtualizacao) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth != null ? auth.getName() : "anonymous";
            
            logger.info("Verificando atualizações de fotos - ultimaAtualizacao: {} para usuário: {}",
                ultimaAtualizacao, username);
            
            Map<String, Object> resultado = fotoReferenciaService.verificarAtualizacoes(ultimaAtualizacao);
            
            boolean temAtualizacoes = (Boolean) resultado.get("temAtualizacoes");
            int quantidade = (Integer) resultado.get("quantidadeAtualizada");
            
            logger.info("✓ {} atualizações disponíveis", quantidade);
            
            String mensagem = temAtualizacoes 
                ? String.format("%d foto(s) para sincronizar", quantidade)
                : "Nenhuma atualização disponível";
            
            return ResponseEntity.ok(ApiResponse.success(resultado, mensagem));
            
        } catch (SQLException e) {
            logger.error("Erro SQL ao verificar atualizações: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erro de banco de dados: " + e.getMessage(), "DATABASE_ERROR"));
        } catch (Exception e) {
            logger.error("Erro ao verificar atualizações: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erro ao verificar atualizações: " + e.getMessage(), "CHECK_ERROR"));
        }
    }
    
    /**
     * Obtém estatísticas de fotos de referência.
     * 
     * GET /api/mobile/fotos-referencia/stats
     * 
     * @return Estatísticas (total, uso de armazenamento, etc)
     */
    @GetMapping("/stats")
    @RequireConsulta
    public ResponseEntity<ApiResponse<Map<String, Object>>> obterEstatisticas() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth != null ? auth.getName() : "anonymous";
            
            logger.info("Obtendo estatísticas de fotos para usuário: {}", username);
            
            Map<String, Object> stats = fotoReferenciaService.obterEstatisticas();
            
            logger.info("✓ Estatísticas: {} fotos, {} KB", 
                stats.get("totalFotos"), stats.get("usoArmazenamentoKB"));
            
            return ResponseEntity.ok(ApiResponse.success(stats, "Estatísticas obtidas com sucesso"));
            
        } catch (SQLException e) {
            logger.error("Erro SQL ao obter estatísticas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erro de banco de dados: " + e.getMessage(), "DATABASE_ERROR"));
        } catch (Exception e) {
            logger.error("Erro ao obter estatísticas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erro ao obter estatísticas: " + e.getMessage(), "STATS_ERROR"));
        }
    }
    
    /**
     * Lista apenas metadados das fotos (sem imagem Base64).
     * Útil para verificar quais fotos o cliente precisa baixar.
     * 
     * GET /api/mobile/fotos-referencia/metadata?ultimaAtualizacao=1234567890
     * 
     * @param ultimaAtualizacao Timestamp da última sincronização
     * @return Lista de DTOs sem imagem
     */
    @GetMapping("/metadata")
    @RequireColetor
    public ResponseEntity<ApiResponse<List<MobileFotoReferenciaDTO>>> listarMetadados(
            @RequestParam(required = false) Long ultimaAtualizacao) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth != null ? auth.getName() : "anonymous";
            
            logger.info("Listando metadados de fotos - ultimaAtualizacao: {} para usuário: {}",
                ultimaAtualizacao, username);
            
            List<MobileFotoReferenciaDTO> metadados = fotoReferenciaService.listarMetadados(ultimaAtualizacao);
            
            logger.info("✓ Retornando metadados de {} fotos", metadados.size());
            
            return ResponseEntity.ok(
                ApiResponse.success(metadados, 
                    String.format("%d foto(s) disponível(is)", metadados.size())));
            
        } catch (SQLException e) {
            logger.error("Erro SQL ao listar metadados: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erro de banco de dados: " + e.getMessage(), "DATABASE_ERROR"));
        } catch (Exception e) {
            logger.error("Erro ao listar metadados: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erro ao listar metadados: " + e.getMessage(), "FETCH_ERROR"));
        }
    }
}
