package com.inventario.sihcp.mobile.server.controller;

import com.inventario.sihcp.mobile.server.dto.ApiResponse;
import com.inventario.sihcp.mobile.server.dto.PagedResponseDTO;
import com.inventario.sihcp.mobile.server.dto.SugestaoDescricaoDTO;
import com.inventario.sihcp.mobile.server.service.MobileSugestaoDescricaoService;
import com.inventario.sihcp.dao.PatrimonioDAO;
import com.inventario.sihcp.security.annotation.RequireColetor;
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
// @CrossOrigin removido — ver MobileSecurityConfig.corsConfigurationSource() (spec correcoes-seguranca Req 6.1)
@RequireColetor // Descrições são usadas para coleta, então requer role COLETOR ou superior
public class MobileDescricaoController {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileDescricaoController.class);
    
    @Autowired
    private PatrimonioDAO patrimonioDAO;

    @Autowired
    private MobileSugestaoDescricaoService mobileSugestaoDescricaoService;
    
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
                    com.inventario.sihcp.dao.InventarioDAO inventarioDAO = new com.inventario.sihcp.dao.InventarioDAO();
                    com.inventario.sihcp.model.Inventario inventarioAtivo = inventarioDAO.buscarInventarioAtivo();
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

    /**
     * Lista paginada de sugestões de descrições de patrimônios
     * <b>não coletados</b> no inventário alvo (ativo, por padrão).
     *
     * <p>Endpoint novo da feature {@code coleta-descricao-livre-com-sugestao},
     * adicionado <b>sem alterar</b> os endpoints existentes sob
     * {@code /api/mobile/descricoes/} (regra steering
     * {@code endpoints-nao-alterar.md}). A URL completa é
     * {@code GET /api/mobile/descricoes/sugestoes}.</p>
     *
     * <p>Segurança: herda {@link RequireColetor} do nível de classe, sem
     * anotação no método. Perfis aceitos: {@code ADMIN}, {@code SUPERVISOR},
     * {@code COLETOR}. {@code CONSULTA} recebe HTTP 403; ausência/expiração
     * de JWT recebe HTTP 401 (Req 10.1, 10.2, 10.3).</p>
     *
     * <p>Todos os parâmetros são opcionais (Req 5.3, 5.5, 5.6, 5.7, 5.8):</p>
     * <ul>
     *   <li>{@code q}            — termo de busca (0–100 chars após trim);
     *       excedente é truncado silenciosamente pelo service;</li>
     *   <li>{@code page}         — índice da página (0-based); valores
     *       nulos/negativos são coagidos a {@code 0} pelo service;</li>
     *   <li>{@code size}         — tamanho da página; valores fora de
     *       {@code [1, 100]} (ou nulos) são substituídos pelo padrão
     *       {@code 50} silenciosamente pelo service (Req 5.7, 6.3);</li>
     *   <li>{@code idInventario} — id do inventário alvo; quando ausente,
     *       o service resolve via inventário ativo, e na ausência deste
     *       retorna {@link PagedResponseDTO#empty(boolean)} com
     *       {@code semInventarioAtivo=true} (Req 5.8).</li>
     * </ul>
     *
     * <p>A mensagem de {@link ApiResponse} inclui o tempo de processamento
     * observado no servidor para apoio a diagnóstico de performance
     * (Req 6.1).</p>
     *
     * @param termoBusca   termo de busca opcional ({@code q})
     * @param page         número da página (0-based), opcional
     * @param size         tamanho da página, opcional
     * @param idInventario id do inventário alvo, opcional
     * @return resposta paginada com sugestões de descrições
     *
     * Requirements: 5.1, 5.9, 10.1, 10.2, 10.3.
     */
    @GetMapping("/sugestoes")
    public ResponseEntity<ApiResponse<PagedResponseDTO<SugestaoDescricaoDTO>>> listarSugestoes(
            @RequestParam(name = "q", required = false) String termoBusca,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size,
            @RequestParam(name = "idInventario", required = false) Integer idInventario) {
        long startTime = System.currentTimeMillis();
        logger.info("Buscando sugestões de descrições (q='{}', page={}, size={}, idInventario={})",
                termoBusca, page, size, idInventario);

        PagedResponseDTO<SugestaoDescricaoDTO> resultado =
                mobileSugestaoDescricaoService.listarSugestoes(termoBusca, page, size, idInventario);

        long duration = System.currentTimeMillis() - startTime;
        String message = "Sugestões retornadas em " + duration + "ms";
        logger.info("✓ {} sugestão(ões) retornada(s) em {}ms (totalElements={}, semInventarioAtivo={})",
                resultado.content().size(), duration, resultado.totalElements(), resultado.semInventarioAtivo());

        return ResponseEntity.ok(ApiResponse.success(resultado, message));
    }
}
