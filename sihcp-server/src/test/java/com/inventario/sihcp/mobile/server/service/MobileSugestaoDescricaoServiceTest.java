package com.inventario.sihcp.mobile.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.inventario.sihcp.dao.InventarioDAO;
import com.inventario.sihcp.dao.PagedResult;
import com.inventario.sihcp.dao.PatrimonioDAO;
import com.inventario.sihcp.dao.SugestaoDescricaoRow;
import com.inventario.sihcp.mobile.server.dto.PagedResponseDTO;
import com.inventario.sihcp.mobile.server.dto.SugestaoDescricaoDTO;
import com.inventario.sihcp.model.Inventario;

/**
 * Testes unitários para {@link MobileSugestaoDescricaoService} com mocks de
 * {@link PatrimonioDAO} e {@link InventarioDAO}.
 *
 * <p>Feature: {@code coleta-descricao-livre-com-sugestao} — tarefa 4.2.</p>
 *
 * <p>Este conjunto de testes valida o comportamento da camada de service
 * responsável pela normalização silenciosa de entrada, resolução de inventário
 * ativo, sanitização de paginação e mapeamento dos registros para DTO.</p>
 *
 * <p>Validates: Requirements 5.3, 5.6, 5.7, 5.8, 6.2, 6.3.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MobileSugestaoDescricaoService — unit tests")
class MobileSugestaoDescricaoServiceTest {

    @Mock
    private PatrimonioDAO patrimonioDAO;

    @Mock
    private InventarioDAO inventarioDAO;

    @InjectMocks
    private MobileSugestaoDescricaoService service;

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    /**
     * Cria um {@link Inventario} stub com o {@code id} informado. A camada de
     * service apenas chama {@link Inventario#getId()} no resultado de
     * {@link InventarioDAO#buscarInventarioAtivo()}, por isso o restante dos
     * campos é irrelevante para estes testes.
     */
    private static Inventario inventarioStub(int id) {
        Inventario inv = new Inventario();
        inv.setId(id);
        return inv;
    }

    /** Atalho para {@code PagedResult} vazio com o {@code page}/{@code size} informados. */
    private static PagedResult<SugestaoDescricaoRow> pagedVazio(int page, int size) {
        return new PagedResult<>(List.of(), 0L, page, size);
    }

    // ---------------------------------------------------------------------
    // Cenário "sem inventário ativo" (Req 5.8)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Sem inventário ativo retorna resposta vazia com semInventarioAtivo=true (Req 5.8)")
    void semInventarioAtivo_retornaRespostaVaziaComFlagTrue() throws SQLException {
        when(inventarioDAO.buscarInventarioAtivo()).thenReturn(null);

        PagedResponseDTO<SugestaoDescricaoDTO> result = service.listarSugestoes(null, 0, 50, null);

        assertNotNull(result);
        assertTrue(result.content().isEmpty(), "content deve estar vazio");
        assertTrue(result.semInventarioAtivo(), "semInventarioAtivo deve ser true");
        assertEquals(0L, result.totalElements(), "totalElements deve ser 0");
        assertEquals(0, result.totalPages(), "totalPages deve ser 0");
        assertFalse(result.hasNext(), "hasNext deve ser false");

        // Garante que o DAO de patrimônios não é consultado quando não há inventário ativo
        verifyNoInteractions(patrimonioDAO);
    }

    // ---------------------------------------------------------------------
    // Inventário solicitado explicitamente (Req 5.8 — atalho do resolver)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Quando idInventario é informado, não consulta inventário ativo (Req 5.8)")
    void inventarioSolicitadoExplicitamente_naoConsultaInventarioAtivo() throws SQLException {
        when(patrimonioDAO.buscarSugestoesNaoColetadasPaginado(42, "", 0, 50))
                .thenReturn(pagedVazio(0, 50));

        PagedResponseDTO<SugestaoDescricaoDTO> result = service.listarSugestoes(null, 0, 50, 42);

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertFalse(result.semInventarioAtivo(),
                "semInventarioAtivo deve ser false porque foi fornecido idInventario");
        assertEquals(0L, result.totalElements());
        assertEquals(0, result.totalPages());
        assertFalse(result.hasNext());

        // InventarioDAO nunca é acionado quando o cliente fornece idInventario
        verify(inventarioDAO, never()).buscarInventarioAtivo();
        verify(patrimonioDAO).buscarSugestoesNaoColetadasPaginado(42, "", 0, 50);
    }

    // ---------------------------------------------------------------------
    // Sanitização silenciosa de "size" (Req 5.7, 6.3)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("size inválido é substituído por 50 silenciosamente (Req 5.7, 6.3)")
    void sizeInvalido_aplicaDefault50Silenciosamente() throws SQLException {
        when(inventarioDAO.buscarInventarioAtivo()).thenReturn(inventarioStub(7));
        when(patrimonioDAO.buscarSugestoesNaoColetadasPaginado(eq(7), eq(""), eq(0), anyInt()))
                .thenAnswer(inv -> pagedVazio(inv.getArgument(2), inv.getArgument(3)));

        // Valores inválidos conforme spec: null, <1 e >100
        Integer[] valoresInvalidos = {null, -1, 0, 101, 9999};

        ArgumentCaptor<Integer> sizeCaptor = ArgumentCaptor.forClass(Integer.class);

        for (Integer invalido : valoresInvalidos) {
            PagedResponseDTO<SugestaoDescricaoDTO> result =
                    service.listarSugestoes(null, 0, invalido, null);

            assertEquals(50, result.size(),
                    () -> "Resposta deve refletir size sanitizado = 50 para entrada inválida " + invalido);
        }

        verify(patrimonioDAO, org.mockito.Mockito.times(valoresInvalidos.length))
                .buscarSugestoesNaoColetadasPaginado(eq(7), eq(""), eq(0), sizeCaptor.capture());

        for (Integer capturado : sizeCaptor.getAllValues()) {
            assertEquals(50, capturado,
                    "DAO deve receber size=50 para qualquer valor inválido, não o valor bruto");
        }
    }

    // ---------------------------------------------------------------------
    // Sanitização silenciosa de "page" (Req 5.7)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("page negativa é substituída por 0 silenciosamente (Req 5.7)")
    void pageNegativa_aplicaZeroSilenciosamente() throws SQLException {
        when(inventarioDAO.buscarInventarioAtivo()).thenReturn(inventarioStub(10));
        when(patrimonioDAO.buscarSugestoesNaoColetadasPaginado(10, "", 0, 50))
                .thenReturn(pagedVazio(0, 50));

        PagedResponseDTO<SugestaoDescricaoDTO> result =
                service.listarSugestoes(null, -5, 50, null);

        assertEquals(0, result.page(), "Resposta deve refletir page sanitizada = 0");
        verify(patrimonioDAO).buscarSugestoesNaoColetadasPaginado(10, "", 0, 50);
    }

    // ---------------------------------------------------------------------
    // Truncamento silencioso do termo (Req 5.3)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("termo de busca com length>100 é truncado silenciosamente em 100 chars (Req 5.3)")
    void termoBuscaAcimaDe100Chars_truncaSilenciosamente() throws SQLException {
        // String de 150 caracteres 'a' — sem espaços, trim não altera nada
        String termo150 = "a".repeat(150);
        String esperado100 = "a".repeat(100);

        when(inventarioDAO.buscarInventarioAtivo()).thenReturn(inventarioStub(1));
        when(patrimonioDAO.buscarSugestoesNaoColetadasPaginado(eq(1), anyString(), eq(0), eq(50)))
                .thenReturn(pagedVazio(0, 50));

        service.listarSugestoes(termo150, 0, 50, null);

        ArgumentCaptor<String> termoCaptor = ArgumentCaptor.forClass(String.class);
        verify(patrimonioDAO).buscarSugestoesNaoColetadasPaginado(
                eq(1), termoCaptor.capture(), eq(0), eq(50));

        String termoEnviadoAoDAO = termoCaptor.getValue();
        assertEquals(100, termoEnviadoAoDAO.length(),
                "Termo enviado ao DAO deve ter exatamente 100 chars após truncamento silencioso");
        assertEquals(esperado100, termoEnviadoAoDAO,
                "Termo enviado ao DAO deve ser o prefixo de 100 caracteres do termo original");
    }

    // ---------------------------------------------------------------------
    // Normalização de termo nulo (Req 5.3)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("termo de busca null é convertido em string vazia (Req 5.3)")
    void termoBuscaNull_convertidoEmStringVazia() throws SQLException {
        when(inventarioDAO.buscarInventarioAtivo()).thenReturn(inventarioStub(3));
        when(patrimonioDAO.buscarSugestoesNaoColetadasPaginado(3, "", 0, 50))
                .thenReturn(pagedVazio(0, 50));

        service.listarSugestoes(null, 0, 50, null);

        verify(patrimonioDAO).buscarSugestoesNaoColetadasPaginado(
                eq(3), eq(""), eq(0), eq(50));
    }

    // ---------------------------------------------------------------------
    // Metadados de paginação (Req 5.6, 6.2)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Calcula totalPages (ceil) e hasNext corretamente a partir do PagedResult (Req 5.6, 6.2)")
    void calculoTotalPagesEHasNext() throws SQLException {
        // Cenário A: totalElements=123, page=1, size=50 → totalPages=3 (ceil), hasNext=true
        when(inventarioDAO.buscarInventarioAtivo()).thenReturn(inventarioStub(5));
        when(patrimonioDAO.buscarSugestoesNaoColetadasPaginado(5, "", 1, 50))
                .thenReturn(new PagedResult<>(List.of(), 123L, 1, 50));

        PagedResponseDTO<SugestaoDescricaoDTO> cenarioA =
                service.listarSugestoes(null, 1, 50, null);

        assertEquals(123L, cenarioA.totalElements());
        assertEquals(3, cenarioA.totalPages(),
                "ceil(123/50) = 3");
        assertTrue(cenarioA.hasNext(),
                "(1+1)*50 = 100 < 123 → deve haver próxima página");
        assertEquals(1, cenarioA.page());
        assertEquals(50, cenarioA.size());

        // Cenário B: totalElements=100, page=1, size=50 → totalPages=2, hasNext=false
        when(patrimonioDAO.buscarSugestoesNaoColetadasPaginado(5, "", 1, 50))
                .thenReturn(new PagedResult<>(List.of(), 100L, 1, 50));

        PagedResponseDTO<SugestaoDescricaoDTO> cenarioB =
                service.listarSugestoes(null, 1, 50, null);

        assertEquals(100L, cenarioB.totalElements());
        assertEquals(2, cenarioB.totalPages(),
                "ceil(100/50) = 2");
        assertFalse(cenarioB.hasNext(),
                "(1+1)*50 = 100 NÃO é < 100 → não há próxima página");
    }

    // ---------------------------------------------------------------------
    // Mapeamento Row → DTO (sanity check — Req 5.5)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Mapeia cada SugestaoDescricaoRow para SugestaoDescricaoDTO preservando campos (Req 5.5)")
    void mapeiaRowsParaDtosPreservandoCampos() throws SQLException {
        SugestaoDescricaoRow row1 = new SugestaoDescricaoRow(11, "IFMT-00011", "Cadeira giratória preta");
        SugestaoDescricaoRow row2 = new SugestaoDescricaoRow(22, "IFMT-00022", "Mesa de escritório");

        when(inventarioDAO.buscarInventarioAtivo()).thenReturn(inventarioStub(9));
        when(patrimonioDAO.buscarSugestoesNaoColetadasPaginado(9, "", 0, 50))
                .thenReturn(new PagedResult<>(List.of(row1, row2), 2L, 0, 50));

        PagedResponseDTO<SugestaoDescricaoDTO> result =
                service.listarSugestoes(null, 0, 50, null);

        assertEquals(2, result.content().size());
        SugestaoDescricaoDTO dto1 = result.content().get(0);
        SugestaoDescricaoDTO dto2 = result.content().get(1);

        assertEquals(11, dto1.idPatrimonio());
        assertEquals("IFMT-00011", dto1.numeroPatrimonio());
        assertEquals("Cadeira giratória preta", dto1.descricao());

        assertEquals(22, dto2.idPatrimonio());
        assertEquals("IFMT-00022", dto2.numeroPatrimonio());
        assertEquals("Mesa de escritório", dto2.descricao());
    }
}
