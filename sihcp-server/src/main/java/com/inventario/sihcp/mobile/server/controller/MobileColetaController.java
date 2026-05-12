package com.inventario.sihcp.mobile.server.controller;

import com.inventario.sihcp.mobile.server.dto.ApiResponse;
import com.inventario.sihcp.mobile.server.dto.MobileColetaRequest;
import com.inventario.sihcp.mobile.server.dto.MobileColetaResponse;
import com.inventario.sihcp.mobile.server.dto.MobileColetaBatchRequest;
import com.inventario.sihcp.mobile.server.service.MobileColetaService;
import com.inventario.sihcp.mobile.server.service.MobilePatrimonioService;
import com.inventario.sihcp.security.annotation.RequireAdmin;
import com.inventario.sihcp.security.annotation.RequireColetor;
import com.inventario.sihcp.security.annotation.RequireConsulta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para operações de coleta mobile
 * 
 * Segurança por Role:
 * - POST (registrar): ADMIN, SUPERVISOR, COLETOR
 * - GET (consultar): ADMIN, SUPERVISOR, COLETOR, CONSULTA
 * - PUT (atualizar): ADMIN, SUPERVISOR, COLETOR (próprias coletas)
 * - DELETE (excluir): ADMIN apenas
 * 
 * @author Sistema de Inventário
 * @version 2.0.0
 */
@RestController
@RequestMapping("/api/mobile/coletas")
// @CrossOrigin removido — ver MobileSecurityConfig.corsConfigurationSource() (spec correcoes-seguranca Req 6.1)
public class MobileColetaController {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileColetaController.class);
    
    private static final int MAX_COLETAS_ALL = 500;

    // Spec coleta-descricao-livre-com-sugestao (Req 1.7, 9.7, 9.8):
    // Quando `descricaoItemSemEtiqueta` é informado, o valor após trim DEVE ter
    // tamanho no intervalo fechado [3, 255]. Campo AUSENTE (null) é aceito
    // silenciosamente para preservar compatibilidade com clientes legados (Req 9.8).
    private static final int DESCRICAO_ITEM_MIN_LENGTH = 3;
    private static final int DESCRICAO_ITEM_MAX_LENGTH = 255;
    private static final String DESCRICAO_ITEM_RANGE_MSG =
            "descricaoItemSemEtiqueta deve ter entre 3 e 255 caracteres (após trim)";
    
    @Autowired
    private MobileColetaService mobileColetaService;

    @Autowired
    private MobilePatrimonioService mobilePatrimonioService;
    
    @Autowired
    private com.inventario.sihcp.security.JwtTokenProvider jwtTokenProvider;
    
    /**
     * Registrar uma nova coleta
     * Requer role: ADMIN, SUPERVISOR ou COLETOR
     * 
     * @param coletaRequest dados da coleta
     * @return resposta com dados da coleta registrada
     */
    @PostMapping
    @RequireColetor
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
            
            // Spec coleta-descricao-livre-com-sugestao Req 1.7, 9.7, 9.8:
            // Validar tamanho de descricaoItemSemEtiqueta se presente (legado = null passa).
            String descricaoError = validarDescricaoItemSemEtiqueta(coletaRequest.getDescricaoItemSemEtiqueta());
            if (descricaoError != null) {
                logger.warn("Descrição inválida em registrarColeta: {}", descricaoError);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(descricaoError, "VALIDATION_ERROR"));
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
                    
        } catch (SQLException | RuntimeException e) {
            logger.error("Erro ao registrar coleta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao registrar coleta: " + e.getMessage(), "COLETA_ERROR"));
        }
    }
    
    /**
     * Registrar múltiplas coletas em lote
     * Requer role: ADMIN, SUPERVISOR ou COLETOR
     * 
     * @param batchRequest lista de coletas
     * @return resposta com resultado do processamento
     */
    @PostMapping("/batch")
    @RequireColetor
    public ResponseEntity<ApiResponse<Map<String, Object>>> registrarColetasEmLote(
            @Valid @RequestBody MobileColetaBatchRequest batchRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            List<MobileColetaRequest> todasColetas = batchRequest.getColetas();
            logger.info("Registrando {} coletas em lote por usuário: {}", 
                    todasColetas.size(), username);
            
            // Spec coleta-descricao-livre-com-sugestao Req 1.7, 9.7, 9.8:
            // Pré-validar descricaoItemSemEtiqueta item a item. Itens inválidos não são
            // enviados ao service — retornam FALHA individual preservando a semântica
            // atual do batch (erro por item, não por requisição inteira).
            List<MobileColetaRequest> coletasValidas = new ArrayList<>();
            List<Integer> indicesOriginaisValidos = new ArrayList<>();
            List<Map<String, Object>> resultadosPreValidacao = new ArrayList<>();
            List<String> errosPreValidacao = new ArrayList<>();
            int falhasPreValidacao = 0;
            
            for (int i = 0; i < todasColetas.size(); i++) {
                MobileColetaRequest item = todasColetas.get(i);
                String erro = validarDescricaoItemSemEtiqueta(item.getDescricaoItemSemEtiqueta());
                if (erro == null) {
                    coletasValidas.add(item);
                    indicesOriginaisValidos.add(i);
                } else {
                    falhasPreValidacao++;
                    Map<String, Object> preInvalido = new HashMap<>();
                    preInvalido.put("indice", i);
                    preInvalido.put("numeroPatrimonio", item.getNumeroPatrimonio());
                    preInvalido.put("status", "FALHA");
                    preInvalido.put("coletaId", null);
                    preInvalido.put("mensagem", erro);
                    resultadosPreValidacao.add(preInvalido);
                    errosPreValidacao.add("Patrimônio " + item.getNumeroPatrimonio() + ": " + erro);
                    logger.warn("Item batch [{}] com descrição inválida: {}", i, erro);
                }
            }
            
            Map<String, Object> resultadoServico;
            if (!coletasValidas.isEmpty()) {
                resultadoServico = mobileColetaService.registrarColetasEmLote(
                        coletasValidas, username);
            } else {
                resultadoServico = new HashMap<>();
                resultadoServico.put("total", 0);
                resultadoServico.put("sucesso", 0);
                resultadoServico.put("falhas", 0);
                resultadoServico.put("duplicadas", 0);
                resultadoServico.put("erros", new ArrayList<String>());
                resultadoServico.put("coletasDuplicadas", new ArrayList<String>());
                resultadoServico.put("resultados", new ArrayList<Map<String, Object>>());
            }
            
            // Mesclar resultados preservando os índices originais do batch.
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> resultadosServico =
                    (List<Map<String, Object>>) resultadoServico.get("resultados");
            List<Map<String, Object>> resultadosFinais = new ArrayList<>();
            if (resultadosServico != null) {
                for (Map<String, Object> r : resultadosServico) {
                    Map<String, Object> copy = new HashMap<>(r);
                    Object indiceServicoObj = copy.get("indice");
                    if (indiceServicoObj instanceof Integer) {
                        int indiceServico = (Integer) indiceServicoObj;
                        if (indiceServico >= 0 && indiceServico < indicesOriginaisValidos.size()) {
                            copy.put("indice", indicesOriginaisValidos.get(indiceServico));
                        }
                    }
                    resultadosFinais.add(copy);
                }
            }
            resultadosFinais.addAll(resultadosPreValidacao);
            resultadosFinais.sort((a, b) -> {
                int ia = a.get("indice") instanceof Integer ? (Integer) a.get("indice") : 0;
                int ib = b.get("indice") instanceof Integer ? (Integer) b.get("indice") : 0;
                return Integer.compare(ia, ib);
            });
            
            @SuppressWarnings("unchecked")
            List<String> errosServico = (List<String>) resultadoServico.getOrDefault(
                    "erros", new ArrayList<String>());
            List<String> errosFinais = new ArrayList<>(errosServico);
            errosFinais.addAll(errosPreValidacao);
            
            int sucessoServico = ((Number) resultadoServico.getOrDefault("sucesso", 0)).intValue();
            int falhasServico = ((Number) resultadoServico.getOrDefault("falhas", 0)).intValue();
            int duplicadasServico = ((Number) resultadoServico.getOrDefault("duplicadas", 0)).intValue();
            
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("total", todasColetas.size());
            resultado.put("sucesso", sucessoServico);
            resultado.put("falhas", falhasServico + falhasPreValidacao);
            resultado.put("duplicadas", duplicadasServico);
            resultado.put("erros", errosFinais);
            resultado.put("coletasDuplicadas", resultadoServico.getOrDefault(
                    "coletasDuplicadas", new ArrayList<String>()));
            resultado.put("resultados", resultadosFinais);
            
            return ResponseEntity.ok(
                    ApiResponse.success(resultado, "Coletas processadas em lote"));
            
        } catch (RuntimeException e) {
            logger.error("Erro ao registrar coletas em lote", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao processar coletas em lote: " + e.getMessage(), 
                            "BATCH_ERROR"));
        }
    }
    
    /**
     * Buscar todas as coletas do usuário autenticado com paginação
     * Requer role: ADMIN, SUPERVISOR, COLETOR ou CONSULTA
     * 
     * @param authHeader header de autorização com token JWT (opcional)
     * @param page número da página (começa em 0)
     * @param size tamanho da página
     * @return lista paginada de coletas
     */
    @GetMapping
    @RequireConsulta
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
            
            logger.info("Buscando coletas com PAGINAÇÃO REAL (page={}, size={})", page, size);
            
            // OTIMIZAÇÃO: Usar paginação REAL no banco de dados
            // ANTES: Carregava TODAS as coletas em memória (vazamento de memória)
            // DEPOIS: LIMIT/OFFSET no SQL (máximo 100 registros por vez)
            Map<String, Object> response;
            if (username != null) {
                logger.info("Buscando coletas do usuário: {} (paginação real)", username);
                response = mobileColetaService.buscarColetasUsuarioComPaginacaoReal(username, page, size);
            } else {
                logger.info("Buscando coletas do sistema (paginação real)");
                response = mobileColetaService.buscarColetasComPaginacaoReal(page, size);
            }
            
            int totalElements = (int) response.get("totalElements");
            int totalPages = (int) response.get("totalPages");
            List<?> content = (List<?>) response.get("content");
            
            logger.info("Retornando {} coletas (página {}/{}, total: {})", 
                content.size(), page + 1, totalPages, totalElements);
            
            return ResponseEntity.ok(
                    ApiResponse.success(response, "Coletas carregadas com sucesso"));
            
        } catch (SQLException | RuntimeException e) {
            logger.error("Erro ao buscar coletas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar coletas", "FETCH_ERROR"));
        }
    }
    
    /**
     * Buscar coletas pendentes de sincronização
     * Requer role: ADMIN, SUPERVISOR ou COLETOR
     * 
     * @return lista de coletas pendentes
     */
    @GetMapping("/pendentes")
    @RequireColetor
    public ResponseEntity<ApiResponse<List<MobileColetaResponse>>> buscarColetasPendentes() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("Buscando coletas pendentes para usuário: {}", username);
            
            List<MobileColetaResponse> coletas = mobileColetaService.buscarColetasPendentes(username);
            
            return ResponseEntity.ok(
                    ApiResponse.success(coletas, "Coletas pendentes carregadas"));
            
        } catch (SQLException | RuntimeException e) {
            logger.error("Erro ao buscar coletas pendentes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar coletas pendentes", "FETCH_ERROR"));
        }
    }
    
    /**
     * Buscar todas as coletas sem paginação (OTIMIZADO)
     * Endpoint: GET /api/mobile/coletas/all
     * 
     * Usa cache e batch queries para melhor performance:
     * - 40 coletas: ~200-500ms (antes: 3-5s)
     * - 95% menos queries ao banco
     * 
     * @return lista completa de coletas
     */
    /**
     * @deprecated Este endpoint foi DESABILITADO para evitar vazamento de memória.
     * Use GET /api/mobile/coletas com paginação (page, size).
     * 
     * PROBLEMA: Carregava TODAS as coletas em memória (200MB → 4GB)
     * SOLUÇÃO: Usar paginação obrigatória
     */
    /**
     * Buscar todas as coletas sem paginação
     * Endpoint: GET /api/mobile/coletas/all
     * 
     * NOTA: Retorna TODAS as coletas do inventário ativo.
     * Use com cuidado em inventários com muitas coletas.
     * 
     * @return lista completa de coletas
     */
    @Deprecated
    @GetMapping("/all")
    @RequireConsulta
    public ResponseEntity<ApiResponse<Map<String, Object>>> buscarTodasColetasSemPaginacao(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) Integer inventarioId) {
        try {
            long startTime = System.currentTimeMillis();

            logger.info("📥 Buscando coletas (inventárioId={})...", inventarioId != null ? inventarioId : "ativo");

            // Delegar ao service a resolução do inventário ativo quando não informado
            int totalColetas = mobileColetaService.contarTotalColetas(inventarioId);
            logger.info("Total de coletas no inventário {}: {}", inventarioId != null ? inventarioId : "ativo", totalColetas);

            int limiteEfetivo = Math.min(totalColetas, MAX_COLETAS_ALL);
            Map<String, Object> response = mobileColetaService.buscarColetasComPaginacaoReal(
                    0, Math.max(limiteEfetivo, 1), inventarioId);

            long duration = System.currentTimeMillis() - startTime;
            List<?> content = (List<?>) response.get("content");

            logger.info("✓ Retornando {} coletas em {}ms", content.size(), duration);

            String warningMsg;
            if (totalColetas > MAX_COLETAS_ALL) {
                warningMsg = "Resultado truncado em " + MAX_COLETAS_ALL + " de " + totalColetas +
                             " coletas. Endpoint deprecated. Use GET /api/mobile/coletas?page=0&size=20";
            } else {
                warningMsg = "Endpoint deprecated. Use GET /api/mobile/coletas?page=0&size=20 para paginacao";
            }

            return ResponseEntity.ok()
                    .header("X-Warning", warningMsg)
                    .body(ApiResponse.success(response, String.format("%d coletas em %dms",
                            content.size(), duration)));

        } catch (SQLException | RuntimeException e) {
            logger.error("❌ Erro ao buscar coletas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar coletas", "FETCH_ERROR"));
        }
    }
    
    /**
     * Buscar histórico de coletas do usuário
     * Requer role: ADMIN, SUPERVISOR, COLETOR ou CONSULTA
     * 
     * @param limit número máximo de registros
     * @return lista de coletas
     */
    @GetMapping("/historico")
    @RequireConsulta
    public ResponseEntity<ApiResponse<List<MobileColetaResponse>>> buscarHistoricoColetas(
            @RequestParam(defaultValue = "50") int limit) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("Buscando histórico de coletas para usuário: {} (limit: {})", username, limit);
            
            List<MobileColetaResponse> coletas = mobileColetaService.buscarHistoricoColetas(username, limit);
            
            return ResponseEntity.ok(
                    ApiResponse.success(coletas, "Histórico de coletas carregado"));
            
        } catch (SQLException | RuntimeException e) {
            logger.error("Erro ao buscar histórico de coletas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar histórico", "FETCH_ERROR"));
        }
    }
    
    /**
     * Buscar coleta por ID
     * Requer role: ADMIN, SUPERVISOR, COLETOR ou CONSULTA
     * 
     * @param id ID da coleta
     * @return dados da coleta
     */
    @GetMapping("/{id}")
    @RequireConsulta
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
            
        } catch (SQLException | RuntimeException e) {
            logger.error("Erro ao buscar coleta por ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar coleta", "FETCH_ERROR"));
        }
    }
    
    /**
     * Atualizar uma coleta existente
     * Requer role: ADMIN, SUPERVISOR ou COLETOR (próprias coletas)
     * 
     * @param id ID da coleta
     * @param coletaRequest novos dados da coleta
     * @return coleta atualizada
     */
    @PutMapping("/{id}")
    @RequireColetor
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
                    
        } catch (SQLException | RuntimeException e) {
            logger.error("Erro ao atualizar coleta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao atualizar coleta", "UPDATE_ERROR"));
        }
    }
    
    /**
     * Excluir uma coleta (apenas admin)
     * Requer role: ADMIN apenas
     * 
     * @param id ID da coleta
     * @return confirmação
     */
    @DeleteMapping("/{id}")
    @RequireAdmin
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
                    
        } catch (SQLException | RuntimeException e) {
            logger.error("Erro ao excluir coleta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao excluir coleta", "DELETE_ERROR"));
        }
    }
    
    /**
     * Buscar descrições de patrimônios pendentes de coleta
     * Retorna apenas descrições de itens que ainda não foram coletados no inventário ativo
     * Requer role: ADMIN, SUPERVISOR ou COLETOR
     * 
     * @param termoBusca termo para buscar na descrição
     * @param idInventario ID do inventário (opcional, usa o ativo se não informado)
     * @return lista de descrições pendentes com estatísticas
     */
    @GetMapping("/descricoes-pendentes")
    @RequireColetor
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
                    
        } catch (RuntimeException e) {
            logger.error("Erro ao buscar descrições pendentes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar descrições pendentes: " + e.getMessage(), 
                            "FETCH_ERROR"));
        }
    }

    /**
     * Verifica se uma coleta seria duplicada
     * POST /api/mobile/coletas/verificar-duplicata
     * Requer role: ADMIN, SUPERVISOR ou COLETOR
     * 
     * @param request dados para verificação (numeroPatrimonio, inventarioId)
     * @return informações sobre duplicação
     */
    @PostMapping("/verificar-duplicata")
    @RequireColetor
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
            Map<String, Object> resultado = mobilePatrimonioService.verificarDuplicataColeta(
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
        } catch (SQLException | RuntimeException e) {
            logger.error("Erro ao verificar duplicata de coleta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao verificar duplicata: " + e.getMessage(), "CHECK_ERROR"));
        }
    }
    
    /**
     * Buscar todas as salas onde há coletas registradas
     * Útil para filtros na tela de itens coletados
     * Requer role: ADMIN, SUPERVISOR, COLETOR ou CONSULTA
     * 
     * @param inventarioId ID do inventário (opcional, usa o ativo se não informado)
     * @return lista de salas distintas com coletas
     */
    @GetMapping("/salas-com-coletas")
    @RequireConsulta
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> buscarSalasComColetas(
            @RequestParam(required = false) Integer inventarioId) {
        try {
            logger.info("Buscando salas com coletas para inventário: {}", 
                    inventarioId != null ? inventarioId : "ATIVO");
            
            long startTime = System.currentTimeMillis();
            
            List<Map<String, Object>> salas = mobileColetaService.buscarSalasComColetas(inventarioId);
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("✓ {} salas com coletas encontradas em {}ms", salas.size(), duration);
            
            return ResponseEntity.ok(
                    ApiResponse.success(salas, 
                            String.format("%d sala(s) com coletas", salas.size())));
            
        } catch (SQLException | RuntimeException e) {
            logger.error("Erro ao buscar salas com coletas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar salas: " + e.getMessage(), "FETCH_ERROR"));
        }
    }
    
    /**
     * Sincronização incremental de coletas
     * Retorna apenas coletas modificadas após o timestamp fornecido
     * Requer role: ADMIN, SUPERVISOR ou COLETOR
     * 
     * @param lastSyncTimestamp timestamp da última sincronização (em milissegundos)
     * @param inventarioId ID do inventário (opcional)
     * @param limit limite de registros (default: 100)
     * @param offset offset para paginação (default: 0)
     * @return coletas modificadas desde o último timestamp
     */
    @GetMapping("/incremental")
    @RequireColetor
    public ResponseEntity<ApiResponse<com.inventario.sihcp.mobile.server.dto.IncrementalSyncResponse<MobileColetaResponse>>> 
            sincronizacaoIncremental(
                @RequestParam(required = false, defaultValue = "0") Long lastSyncTimestamp,
                @RequestParam(required = false) Integer inventarioId,
                @RequestParam(required = false, defaultValue = "100") Integer limit,
                @RequestParam(required = false, defaultValue = "0") Integer offset) {
        try {
            logger.info("📥 Sincronização incremental de coletas: lastSync={}, inventario={}, limit={}, offset={}", 
                lastSyncTimestamp, inventarioId, limit, offset);
            
            // Buscar coletas modificadas desde o último timestamp
            com.inventario.sihcp.mobile.server.dto.IncrementalSyncResponse<MobileColetaResponse> response = 
                mobileColetaService.buscarColetasIncrementais(lastSyncTimestamp, inventarioId, limit, offset);
            
            String mensagem = String.format("✓ %d coleta(s) retornada(s) de %d total", 
                response.getReturnedCount(), response.getTotalCount());
            
            logger.info(mensagem);
            
            return ResponseEntity.ok(ApiResponse.success(response, mensagem));
            
        } catch (RuntimeException e) {
            logger.error("❌ Erro na sincronização incremental de coletas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro na sincronização incremental: " + e.getMessage(), "SYNC_ERROR"));
        }
    }

    /**
     * Valida o campo {@code descricaoItemSemEtiqueta} conforme a spec
     * <em>coleta-descricao-livre-com-sugestao</em> (Req 1.7, 9.7, 9.8).
     *
     * <p>Regra:</p>
     * <ul>
     *   <li>Se o campo for {@code null} (cliente legado — Req 9.8), aceita silenciosamente
     *       e retorna {@code null} (sem erro).</li>
     *   <li>Se presente, aplica {@code trim()} e verifica que o tamanho está no
     *       intervalo fechado [3, 255]. Fora desse intervalo, retorna uma mensagem
     *       de erro descritiva.</li>
     * </ul>
     *
     * <p>Este método apenas valida; não modifica o request e não altera URL,
     * método HTTP ou estrutura de campos dos endpoints (regra steering
     * {@code endpoints-nao-alterar.md}).</p>
     *
     * @param descricao valor do campo no payload (pode ser {@code null})
     * @return mensagem de erro quando inválido, ou {@code null} quando válido
     */
    private String validarDescricaoItemSemEtiqueta(String descricao) {
        if (descricao == null) {
            // Campo ausente = cliente legado. Aceitar normalmente (Req 9.8).
            return null;
        }
        int tamanho = descricao.trim().length();
        if (tamanho < DESCRICAO_ITEM_MIN_LENGTH || tamanho > DESCRICAO_ITEM_MAX_LENGTH) {
            return DESCRICAO_ITEM_RANGE_MSG;
        }
        return null;
    }

}
