package com.inventario.mobile.server.controller;

import com.inventario.mobile.server.dto.ApiResponse;
import com.inventario.mobile.server.dto.MobileColetaRequest;
import com.inventario.mobile.server.dto.MobileColetaResponse;
import com.inventario.mobile.server.dto.MobileColetaBatchRequest;
import com.inventario.mobile.server.service.MobileColetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para operações de coleta mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/mobile/coletas")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MobileColetaController {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileColetaController.class);
    
    @Autowired
    private MobileColetaService mobileColetaService;
    
    @Autowired
    private com.inventario.security.JwtTokenProvider jwtTokenProvider;
    
    /**
     * Registrar uma nova coleta
     * 
     * @param coletaRequest dados da coleta
     * @return resposta com dados da coleta registrada
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MobileColetaResponse>> registrarColeta(
            @Valid @RequestBody MobileColetaRequest coletaRequest) {
        try {
            // Obter username do contexto de segurança ou do request
            String username = null;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated() 
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                username = authentication.getName();
            }
            
            // Se não houver autenticação, o usuarioId deve vir obrigatoriamente no request
            if (username == null && coletaRequest.getUsuarioId() == null) {
                logger.warn("Tentativa de registrar coleta sem usuário autenticado e sem usuarioId no request");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("É obrigatório informar o usuário que está realizando a coleta", "USUARIO_OBRIGATORIO"));
            }
            
            logger.info("Registrando coleta para patrimônio {} por usuário: {} (usuarioId: {})", 
                    coletaRequest.getNumeroPatrimonio(), username, coletaRequest.getUsuarioId());
            
            MobileColetaResponse response = mobileColetaService.registrarColeta(coletaRequest, username);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response, "Coleta registrada com sucesso"));
            
        } catch (IllegalArgumentException e) {
            logger.warn("Dados inválidos na coleta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "INVALID_DATA"));
                    
        } catch (Exception e) {
            logger.error("Erro ao registrar coleta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao registrar coleta: " + e.getMessage(), "COLETA_ERROR"));
        }
    }
    
    /**
     * Registrar múltiplas coletas em lote
     * 
     * @param batchRequest lista de coletas
     * @return resposta com resultado do processamento
     */
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<Map<String, Object>>> registrarColetasEmLote(
            @Valid @RequestBody MobileColetaBatchRequest batchRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("Registrando {} coletas em lote por usuário: {}", 
                    batchRequest.getColetas().size(), username);
            
            Map<String, Object> resultado = mobileColetaService.registrarColetasEmLote(
                    batchRequest.getColetas(), username);
            
            return ResponseEntity.ok(
                    ApiResponse.success(resultado, "Coletas processadas em lote"));
            
        } catch (Exception e) {
            logger.error("Erro ao registrar coletas em lote", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao processar coletas em lote: " + e.getMessage(), 
                            "BATCH_ERROR"));
        }
    }
    
    /**
     * Buscar todas as coletas do usuário autenticado com paginação
     * Endpoint público que aceita token JWT opcional
     * 
     * @param authHeader header de autorização com token JWT (opcional)
     * @param page número da página (começa em 0)
     * @param size tamanho da página
     * @return lista paginada de coletas
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> buscarTodasColetas(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            String username = null;
            
            // Tentar extrair username do token JWT se fornecido
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    username = jwtTokenProvider.getUsernameFromToken(token);
                    logger.info("Username extraído do token JWT: {}", username);
                } catch (Exception e) {
                    logger.warn("Erro ao extrair username do token: {}", e.getMessage());
                }
            }
            
            // Se não conseguiu extrair do token, tentar do SecurityContext
            if (username == null) {
                try {
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    if (authentication != null && !"anonymousUser".equals(authentication.getName())) {
                        username = authentication.getName();
                        logger.info("Username extraído do SecurityContext: {}", username);
                    }
                } catch (Exception e) {
                    logger.warn("Erro ao extrair username do SecurityContext: {}", e.getMessage());
                }
            }
            
            logger.info("Buscando coletas (page={}, size={})", page, size);
            
            // Buscar coletas - se tiver username, busca do usuário, senão busca todas do sistema
            List<MobileColetaResponse> todasColetas;
            if (username != null) {
                logger.info("Buscando coletas do usuário: {}", username);
                todasColetas = mobileColetaService.buscarTodasColetas(username);
            } else {
                logger.info("Buscando todas as coletas do sistema");
                todasColetas = mobileColetaService.buscarTodasColetasDoSistema();
            }
            
            // Calcular paginação
            int totalElements = todasColetas.size();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            int fromIndex = page * size;
            int toIndex = Math.min(fromIndex + size, totalElements);
            
            // Validar página
            if (fromIndex > totalElements) {
                fromIndex = 0;
                toIndex = Math.min(size, totalElements);
            }
            
            List<MobileColetaResponse> coletasPaginadas = fromIndex < totalElements 
                ? todasColetas.subList(fromIndex, toIndex)
                : List.of();
            
            // Montar resposta paginada
            Map<String, Object> response = Map.of(
                "content", coletasPaginadas,
                "page", page,
                "size", size,
                "totalElements", totalElements,
                "totalPages", totalPages,
                "first", page == 0,
                "last", page >= totalPages - 1
            );
            
            logger.info("Retornando {} coletas (página {}/{}, total: {})", 
                coletasPaginadas.size(), page + 1, totalPages, totalElements);
            
            return ResponseEntity.ok(
                    ApiResponse.success(response, "Coletas carregadas com sucesso"));
            
        } catch (Exception e) {
            logger.error("Erro ao buscar coletas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar coletas", "FETCH_ERROR"));
        }
    }
    
    /**
     * Buscar todas as coletas sem paginação (para compatibilidade com app)
     * 
     * @return lista de todas as coletas
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<MobileColetaResponse>>> buscarTodasColetasSemPaginacao() {
        try {
            String username = null;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && !"anonymousUser".equals(authentication.getName())) {
                username = authentication.getName();
            }
            
            logger.info("Buscando todas as coletas");
            
            // Buscar todas as coletas do sistema
            List<MobileColetaResponse> coletas;
            if (username != null) {
                logger.info("Buscando coletas do usuário: {}", username);
                coletas = mobileColetaService.buscarTodasColetas(username);
            } else {
                logger.info("Buscando todas as coletas do sistema");
                coletas = mobileColetaService.buscarTodasColetasDoSistema();
            }
            
            logger.info("Retornando {} coletas", coletas.size());
            
            return ResponseEntity.ok(
                    ApiResponse.success(coletas, String.format("%d coletas carregadas", coletas.size())));
            
        } catch (Exception e) {
            logger.error("Erro ao buscar coletas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar coletas", "FETCH_ERROR"));
        }
    }
    
    /**
     * Buscar coletas pendentes de sincronização
     * 
     * @return lista de coletas pendentes
     */
    @GetMapping("/pendentes")
    public ResponseEntity<ApiResponse<List<MobileColetaResponse>>> buscarColetasPendentes() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("Buscando coletas pendentes para usuário: {}", username);
            
            List<MobileColetaResponse> coletas = mobileColetaService.buscarColetasPendentes(username);
            
            return ResponseEntity.ok(
                    ApiResponse.success(coletas, "Coletas pendentes carregadas"));
            
        } catch (Exception e) {
            logger.error("Erro ao buscar coletas pendentes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar coletas pendentes", "FETCH_ERROR"));
        }
    }
    
    /**
     * Buscar histórico de coletas do usuário
     * 
     * @param limit número máximo de registros
     * @return lista de coletas
     */
    @GetMapping("/historico")
    public ResponseEntity<ApiResponse<List<MobileColetaResponse>>> buscarHistoricoColetas(
            @RequestParam(defaultValue = "50") int limit) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("Buscando histórico de coletas para usuário: {} (limit: {})", username, limit);
            
            List<MobileColetaResponse> coletas = mobileColetaService.buscarHistoricoColetas(username, limit);
            
            return ResponseEntity.ok(
                    ApiResponse.success(coletas, "Histórico de coletas carregado"));
            
        } catch (Exception e) {
            logger.error("Erro ao buscar histórico de coletas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar histórico", "FETCH_ERROR"));
        }
    }
    
    /**
     * Buscar coleta por ID
     * 
     * @param id ID da coleta
     * @return dados da coleta
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MobileColetaResponse>> buscarColetaPorId(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("Buscando coleta {} para usuário: {}", id, username);
            
            MobileColetaResponse coleta = mobileColetaService.buscarColetaPorId(id, username);
            
            if (coleta != null) {
                return ResponseEntity.ok(
                        ApiResponse.success(coleta, "Coleta encontrada"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Coleta não encontrada", "NOT_FOUND"));
            }
            
        } catch (Exception e) {
            logger.error("Erro ao buscar coleta por ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar coleta", "FETCH_ERROR"));
        }
    }
    
    /**
     * Atualizar uma coleta existente
     * 
     * @param id ID da coleta
     * @param coletaRequest novos dados da coleta
     * @return coleta atualizada
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MobileColetaResponse>> atualizarColeta(
            @PathVariable Long id,
            @Valid @RequestBody MobileColetaRequest coletaRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("Atualizando coleta {} por usuário: {}", id, username);
            
            MobileColetaResponse response = mobileColetaService.atualizarColeta(id, coletaRequest, username);
            
            return ResponseEntity.ok(
                    ApiResponse.success(response, "Coleta atualizada com sucesso"));
            
        } catch (IllegalArgumentException e) {
            logger.warn("Dados inválidos na atualização: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "INVALID_DATA"));
                    
        } catch (Exception e) {
            logger.error("Erro ao atualizar coleta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao atualizar coleta", "UPDATE_ERROR"));
        }
    }
    
    /**
     * Excluir uma coleta (apenas admin)
     * 
     * @param id ID da coleta
     * @return confirmação
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> excluirColeta(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("Excluindo coleta {} por usuário: {}", id, username);
            
            boolean excluida = mobileColetaService.excluirColeta(id, username);
            
            if (excluida) {
                return ResponseEntity.ok(
                        ApiResponse.success("Coleta excluída com sucesso", "Coleta excluída"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Coleta não encontrada", "NOT_FOUND"));
            }
            
        } catch (SecurityException e) {
            logger.warn("Tentativa não autorizada de exclusão: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Sem permissão para excluir coleta", "FORBIDDEN"));
                    
        } catch (Exception e) {
            logger.error("Erro ao excluir coleta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao excluir coleta", "DELETE_ERROR"));
        }
    }
    
    /**
     * Buscar descrições de patrimônios pendentes de coleta
     * Retorna apenas descrições de itens que ainda não foram coletados no inventário ativo
     * 
     * @param termoBusca termo para buscar na descrição
     * @param idInventario ID do inventário (opcional, usa o ativo se não informado)
     * @return lista de descrições pendentes com estatísticas
     */
    @GetMapping("/descricoes-pendentes")
    public ResponseEntity<ApiResponse<Map<String, Object>>> buscarDescricoesPendentes(
            @RequestParam String termoBusca,
            @RequestParam(required = false) Integer idInventario) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : null;
            
            logger.info("Buscando descrições pendentes para termo '{}' no inventário {} por usuário: {}", 
                    termoBusca, idInventario, username);
            
            Map<String, Object> resultado = mobileColetaService.buscarDescricoesPendentes(
                    termoBusca, idInventario);
            
            return ResponseEntity.ok(
                    ApiResponse.success(resultado, "Descrições pendentes carregadas"));
            
        } catch (IllegalArgumentException e) {
            logger.warn("Parâmetros inválidos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "INVALID_PARAMS"));
                    
        } catch (Exception e) {
            logger.error("Erro ao buscar descrições pendentes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar descrições pendentes: " + e.getMessage(), 
                            "FETCH_ERROR"));
        }
    }

    /**
     * Verifica se uma coleta seria duplicada
     * POST /api/mobile/coletas/verificar-duplicata
     * 
     * @param request dados para verificação (numeroPatrimonio, inventarioId)
     * @return informações sobre duplicação
     */
    @PostMapping("/verificar-duplicata")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verificarDuplicataColeta(
            @RequestBody Map<String, Object> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "anonymous";
            
            String numeroPatrimonio = (String) request.get("numeroPatrimonio");
            Integer inventarioId = request.get("inventarioId") != null 
                    ? Integer.valueOf(request.get("inventarioId").toString()) 
                    : null;
            
            if (numeroPatrimonio == null || numeroPatrimonio.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Número do patrimônio é obrigatório", "MISSING_PARAMETER"));
            }
            
            logger.info("Verificando duplicata de coleta: patrimônio={}, inventário={}, usuário={}", 
                    numeroPatrimonio, inventarioId, username);
            
            // Usar o serviço de patrimônio para verificar duplicata
            com.inventario.mobile.server.service.MobilePatrimonioService patrimonioService = 
                    new com.inventario.mobile.server.service.MobilePatrimonioService();
            
            Map<String, Object> resultado = patrimonioService.verificarDuplicataColeta(
                    numeroPatrimonio, inventarioId);
            
            boolean duplicado = (Boolean) resultado.get("duplicado");
            String mensagem = (String) resultado.get("mensagem");
            
            HttpStatus status = duplicado ? HttpStatus.CONFLICT : HttpStatus.OK;
            
            return ResponseEntity.status(status)
                    .body(ApiResponse.success(resultado, mensagem));
            
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao verificar duplicata: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "VALIDATION_ERROR"));
        } catch (Exception e) {
            logger.error("Erro ao verificar duplicata de coleta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao verificar duplicata: " + e.getMessage(), "CHECK_ERROR"));
        }
    }
    
    /**
     * Sincronização incremental de coletas
     * Retorna apenas coletas modificadas após o timestamp fornecido
     * 
     * @param lastSyncTimestamp timestamp da última sincronização (em milissegundos)
     * @param inventarioId ID do inventário (opcional)
     * @param limit limite de registros (default: 100)
     * @param offset offset para paginação (default: 0)
     * @return coletas modificadas desde o último timestamp
     */
    @GetMapping("/incremental")
    public ResponseEntity<ApiResponse<com.inventario.mobile.server.dto.IncrementalSyncResponse<MobileColetaResponse>>> 
            sincronizacaoIncremental(
                @RequestParam(required = false, defaultValue = "0") Long lastSyncTimestamp,
                @RequestParam(required = false) Integer inventarioId,
                @RequestParam(required = false, defaultValue = "100") Integer limit,
                @RequestParam(required = false, defaultValue = "0") Integer offset) {
        try {
            logger.info("📥 Sincronização incremental de coletas: lastSync={}, inventario={}, limit={}, offset={}", 
                lastSyncTimestamp, inventarioId, limit, offset);
            
            // Obter username do contexto de segurança
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : null;
            
            // Buscar coletas modificadas desde o último timestamp
            com.inventario.mobile.server.dto.IncrementalSyncResponse<MobileColetaResponse> response = 
                mobileColetaService.buscarColetasIncrementais(lastSyncTimestamp, inventarioId, limit, offset);
            
            String mensagem = String.format("✓ %d coleta(s) retornada(s) de %d total", 
                response.getReturnedCount(), response.getTotalCount());
            
            logger.info(mensagem);
            
            return ResponseEntity.ok(ApiResponse.success(response, mensagem));
            
        } catch (Exception e) {
            logger.error("❌ Erro na sincronização incremental de coletas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro na sincronização incremental: " + e.getMessage(), "SYNC_ERROR"));
        }
    }

}
