package com.inventario.sihcp.mobile.server.service;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.inventario.sihcp.dao.InventarioDAO;
import com.inventario.sihcp.dao.PagedResult;
import com.inventario.sihcp.dao.PatrimonioDAO;
import com.inventario.sihcp.dao.SugestaoDescricaoRow;
import com.inventario.sihcp.mobile.server.dto.PagedResponseDTO;
import com.inventario.sihcp.mobile.server.dto.SugestaoDescricaoDTO;
import com.inventario.sihcp.model.Inventario;

/**
 * Serviço de sugestões de descrições de patrimônios não coletados
 * usado pelo endpoint {@code GET /api/mobile/descricoes/sugestoes}
 * (feature {@code coleta-descricao-livre-com-sugestao}).
 *
 * <p>Responsabilidades desta camada (a camada DAO lida apenas com SQL):</p>
 * <ul>
 *   <li><b>Normalização silenciosa do termo de busca</b> — {@code null} vira
 *       string vazia; aplica {@code trim()}; trunca silenciosamente em 100
 *       caracteres (Req 5.3).</li>
 *   <li><b>Sanitização silenciosa de paginação</b> — {@code page} é coagido
 *       para {@code >= 0}; {@code size} fora do intervalo {@code [1, 100]}
 *       (ou nulo) é substituído pelo padrão {@code 50} sem gerar erro
 *       (Req 5.7, 6.3).</li>
 *   <li><b>Resolução do inventário ativo</b> — se o cliente não informar
 *       {@code idInventarioSolicitado}, consulta
 *       {@link InventarioDAO#buscarInventarioAtivo()}; na ausência de
 *       inventário ativo, retorna {@link PagedResponseDTO#empty(boolean)}
 *       com {@code semInventarioAtivo = true} (Req 5.8).</li>
 *   <li><b>Mapeamento {@link SugestaoDescricaoRow} → {@link SugestaoDescricaoDTO}</b>
 *       e composição dos metadados de paginação ({@code totalPages},
 *       {@code hasNext}) a partir do {@link PagedResult} devolvido pelo
 *       DAO (Req 5.5, 5.6).</li>
 * </ul>
 *
 * <p>Exceções {@link SQLException} lançadas pela camada DAO são encapsuladas
 * em {@link RuntimeException} seguindo o padrão adotado pelos demais
 * services mobile deste módulo, de forma que o controller e o
 * {@code GlobalExceptionHandler} possam responder {@code HTTP 500}
 * apropriadamente.</p>
 *
 * <p>Requirements: 5.3, 5.5, 5.6, 5.7, 5.8, 6.2, 6.3.</p>
 */
@Service
public class MobileSugestaoDescricaoService {

    private static final Logger logger = LoggerFactory.getLogger(MobileSugestaoDescricaoService.class);

    /** Tamanho máximo silenciosamente aplicado ao termo de busca (Req 5.3). */
    private static final int MAX_TERMO_BUSCA = 100;

    /** Tamanho de página padrão quando o parâmetro é inválido ou ausente (Req 5.7, 6.3). */
    private static final int DEFAULT_PAGE_SIZE = 50;

    /** Tamanho mínimo aceito para {@code size} (Req 5.6). */
    private static final int MIN_PAGE_SIZE = 1;

    /** Tamanho máximo aceito para {@code size} (Req 5.6). */
    private static final int MAX_PAGE_SIZE = 100;

    private final PatrimonioDAO patrimonioDAO;
    private final InventarioDAO inventarioDAO;

    /**
     * Construtor principal (preferencial para testes) que permite a injeção
     * explícita dos DAOs.
     *
     * @param patrimonioDAO DAO de patrimônios (deve expor o método
     *                      {@code buscarSugestoesNaoColetadasPaginado})
     * @param inventarioDAO DAO de inventários (usado para resolver o
     *                      inventário ativo quando o cliente não informa
     *                      {@code idInventario})
     */
    public MobileSugestaoDescricaoService(PatrimonioDAO patrimonioDAO, InventarioDAO inventarioDAO) {
        this.patrimonioDAO = patrimonioDAO;
        this.inventarioDAO = inventarioDAO;
    }

    /**
     * Construtor padrão usado pelo Spring quando não há beans gerenciados
     * para os DAOs (padrão do módulo mobile: DAOs são instanciados via
     * {@code new} e compartilham {@code ConnectionManager}).
     */
    public MobileSugestaoDescricaoService() {
        this(new PatrimonioDAO(), new InventarioDAO());
    }

    /**
     * Lista sugestões de descrições de patrimônios não coletados no inventário
     * alvo, aplicando normalização silenciosa de entrada conforme os requisitos.
     *
     * <p>Todos os parâmetros são opcionais do ponto de vista do cliente HTTP
     * (refletem os {@code @RequestParam required=false} do controller). Esta
     * camada de service é o ponto central onde valores ausentes ou inválidos
     * são corrigidos silenciosamente (Req 5.7, 6.3). O comportamento para
     * cada parâmetro inválido é:</p>
     *
     * <ul>
     *   <li>{@code termoBusca == null} → {@code ""} (sem filtro);</li>
     *   <li>{@code termoBusca.length() > 100} → truncado em 100 chars, silencioso;</li>
     *   <li>{@code page == null} ou {@code page < 0} → {@code 0};</li>
     *   <li>{@code size == null}, {@code size < 1} ou {@code size > 100} → {@code 50}.</li>
     * </ul>
     *
     * <p>Quando {@code idInventarioSolicitado == null} e não há inventário
     * ativo, retorna {@link PagedResponseDTO#empty(boolean)} com
     * {@code semInventarioAtivo = true} (Req 5.8). O app usa essa flag para
     * diferenciar "sem resultados" de "sem inventário ativo".</p>
     *
     * @param termoBusca             termo de busca bruto vindo do cliente
     *                               (pode ser {@code null})
     * @param page                   índice da página (0-based), aceita
     *                               {@code null}
     * @param size                   tamanho da página, aceita {@code null}
     * @param idInventarioSolicitado id do inventário alvo; {@code null}
     *                               solicita resolução via inventário ativo
     * @return resposta paginada com as sugestões mapeadas e metadados de
     *         paginação preenchidos; nunca {@code null}
     * @throws RuntimeException se ocorrer {@link SQLException} ao consultar
     *                          {@link InventarioDAO} ou {@link PatrimonioDAO}
     */
    public PagedResponseDTO<SugestaoDescricaoDTO> listarSugestoes(
            String termoBusca,
            Integer page,
            Integer size,
            Integer idInventarioSolicitado
    ) {
        // 1. Normalização do termo de busca (Req 5.3)
        final String termoNormalizado = normalizarTermoBusca(termoBusca);

        // 2. Sanitização de page e size (Req 5.7, 6.3)
        final int pageSanitizado = sanitizarPage(page);
        final int sizeSanitizado = sanitizarSize(size);

        // 3. Resolução do inventário alvo (Req 5.8)
        final Integer idInventarioResolvido;
        try {
            idInventarioResolvido = resolverIdInventario(idInventarioSolicitado);
        } catch (SQLException e) {
            logger.error("Erro ao resolver inventário ativo para sugestões de descrição", e);
            throw new RuntimeException("Falha ao resolver inventário ativo", e);
        }

        if (idInventarioResolvido == null) {
            // Sem inventário ativo — Req 5.8
            logger.debug("Nenhum inventário ativo encontrado; retornando resposta vazia com semInventarioAtivo=true");
            return PagedResponseDTO.empty(true);
        }

        // 4. Consulta paginada ao DAO
        final PagedResult<SugestaoDescricaoRow> pagina;
        try {
            pagina = patrimonioDAO.buscarSugestoesNaoColetadasPaginado(
                    idInventarioResolvido,
                    termoNormalizado,
                    pageSanitizado,
                    sizeSanitizado
            );
        } catch (SQLException e) {
            logger.error("Erro ao buscar sugestões de descrição (idInventario={}, page={}, size={})",
                    idInventarioResolvido, pageSanitizado, sizeSanitizado, e);
            throw new RuntimeException("Falha ao buscar sugestões de descrição", e);
        }

        // 5. Mapeamento Row → DTO (Req 5.5)
        final List<SugestaoDescricaoDTO> content = pagina.items().stream()
                .map(row -> new SugestaoDescricaoDTO(row.id(), row.numero(), row.descricao()))
                .toList();

        // 6. Metadados de paginação (Req 5.6, 6.2)
        final long totalElements = pagina.totalElements();
        final int totalPages = totalElements == 0L
                ? 0
                : (int) Math.ceil((double) totalElements / (double) sizeSanitizado);
        final boolean hasNext = ((long) (pageSanitizado + 1) * (long) sizeSanitizado) < totalElements;

        logger.debug("Sugestões retornadas: {} itens (página {} de {}, total {})",
                content.size(), pageSanitizado, totalPages, totalElements);

        return new PagedResponseDTO<>(
                content,
                pageSanitizado,
                sizeSanitizado,
                totalElements,
                totalPages,
                hasNext,
                false // semInventarioAtivo — aqui sabemos que há inventário
        );
    }

    // ---------------------------------------------------------------------
    // Helpers de normalização/sanitização — extraídos para facilitar leitura
    // e para permitir testes unitários focados (tarefa 4.2).
    // ---------------------------------------------------------------------

    /**
     * Normaliza o termo de busca conforme Req 5.3: {@code null} → {@code ""};
     * aplica {@code trim()}; trunca silenciosamente em {@value #MAX_TERMO_BUSCA}
     * caracteres.
     */
    private static String normalizarTermoBusca(String termoBusca) {
        if (termoBusca == null) {
            return "";
        }
        String trimmed = termoBusca.trim();
        if (trimmed.length() > MAX_TERMO_BUSCA) {
            trimmed = trimmed.substring(0, MAX_TERMO_BUSCA);
        }
        return trimmed;
    }

    /**
     * Garante que {@code page >= 0} (Req 5.7). {@code null} ou negativo vira
     * {@code 0} silenciosamente.
     */
    private static int sanitizarPage(Integer page) {
        if (page == null || page < 0) {
            return 0;
        }
        return page;
    }

    /**
     * Coage {@code size} ao intervalo {@code [1, 100]} (Req 5.6, 5.7, 6.3).
     * {@code null} ou fora do intervalo vira {@value #DEFAULT_PAGE_SIZE}
     * silenciosamente, sem gerar erro ao cliente.
     */
    private static int sanitizarSize(Integer size) {
        if (size == null || size < MIN_PAGE_SIZE || size > MAX_PAGE_SIZE) {
            return DEFAULT_PAGE_SIZE;
        }
        return size;
    }

    /**
     * Resolve o id do inventário alvo. Se o cliente informou um valor, ele
     * é usado diretamente. Caso contrário consulta o inventário ativo via
     * {@link InventarioDAO#buscarInventarioAtivo()}; retorna {@code null}
     * quando não existe inventário ativo (caso Req 5.8).
     */
    private Integer resolverIdInventario(Integer idInventarioSolicitado) throws SQLException {
        if (idInventarioSolicitado != null) {
            return idInventarioSolicitado;
        }
        Inventario ativo = inventarioDAO.buscarInventarioAtivo();
        return (ativo == null) ? null : ativo.getId();
    }
}
