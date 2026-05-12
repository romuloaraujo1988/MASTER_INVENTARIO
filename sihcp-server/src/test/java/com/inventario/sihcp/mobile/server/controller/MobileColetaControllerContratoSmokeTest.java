package com.inventario.sihcp.mobile.server.controller;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inventario.sihcp.mobile.server.dto.ApiResponse;
import com.inventario.sihcp.mobile.server.dto.MobileColetaBatchRequest;
import com.inventario.sihcp.mobile.server.dto.MobileColetaRequest;
import com.inventario.sihcp.mobile.server.dto.MobileColetaResponse;
import com.inventario.sihcp.security.annotation.RequireAdmin;
import com.inventario.sihcp.security.annotation.RequireColetor;
import com.inventario.sihcp.security.annotation.RequireConsulta;
import com.inventario.sihcp.security.annotation.RequireSupervisor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Smoke test de contrato dos endpoints de coleta do app mobile:
 *
 * <ul>
 *   <li>{@code POST /api/mobile/coletas} — registra uma coleta individual.</li>
 *   <li>{@code POST /api/mobile/coletas/batch} — registra coletas em lote.</li>
 * </ul>
 *
 * <p><strong>Validates: Requirements 9.7, 9.8</strong></p>
 *
 * <p>Feature: {@code coleta-descricao-livre-com-sugestao} — tarefa 19.2.</p>
 *
 * <h2>Por que é um smoke test reflexivo e não um teste HTTP de ponta a ponta</h2>
 *
 * <p>A Req 9.7 exige que esses endpoints preservem método HTTP, URL, campos
 * obrigatórios/opcionais, tipos e estrutura de resposta. A Req 9.8 exige
 * que uma requisição legada (sem os campos novos desta feature) seja
 * aceita normalmente. Um teste HTTP completo contra {@code MockMvc} exigiria
 * contexto Spring, mocks de serviços, banco de teste e stub de JWT — custo
 * elevado para o ganho real, que é detectar alterações estáticas no contrato.</p>
 *
 * <p>Esta suíte captura o contrato como um "snapshot estrutural" via
 * reflexão, exatamente no mesmo estilo do teste irmão
 * {@code MobileDescricaoControllerContratoLegadoTest} (tarefa 5.3):</p>
 *
 * <ol>
 *   <li>URL base da classe: {@code /api/mobile/coletas}.</li>
 *   <li>Método {@code registrarColeta(MobileColetaRequest)} mapeado em
 *       {@code @PostMapping} na raiz (sem path adicional).</li>
 *   <li>Método {@code registrarColetasEmLote(MobileColetaBatchRequest)}
 *       mapeado em {@code @PostMapping("/batch")}.</li>
 *   <li>Autorização {@code @RequireColetor} em ambos os métodos.</li>
 *   <li>Tipos de retorno parametrizados preservados:
 *       {@code ResponseEntity<ApiResponse<MobileColetaResponse>>} e
 *       {@code ResponseEntity<ApiResponse<Map<String, Object>>>}.</li>
 *   <li>Campo {@code descricaoItemSemEtiqueta} em
 *       {@link MobileColetaRequest} permanece opcional (sem {@link NotNull},
 *       {@link NotEmpty}, {@link NotBlank} ou {@link Size})
 *       — requisito central da Req 9.8 (cliente legado não envia o campo).</li>
 * </ol>
 *
 * <p>Se qualquer uma dessas invariantes mudar, o teste falha imediatamente
 * no build, evidenciando violação da Req 9.7 ou 9.8.</p>
 *
 * <p>Executa sem contexto Spring — apenas reflexão + JUnit 5.</p>
 *
 * @author Sistema de Inventário
 * @since 2.7.0
 */
@Tag("smoke-test")
@DisplayName("Contrato preservado: POST /api/mobile/coletas e /batch (Req 9.7, 9.8)")
class MobileColetaControllerContratoSmokeTest {

    private static final String EXPECTED_CLASS_PATH = "/api/mobile/coletas";
    private static final String EXPECTED_BATCH_PATH = "/batch";

    // =========================================================================
    // 1. Controller: URL base e natureza
    // =========================================================================

    @Test
    @DisplayName("Classe tem @RequestMapping(\"/api/mobile/coletas\") (Req 9.7)")
    void classeMantemRequestMappingOriginal() {
        RequestMapping mapping =
                MobileColetaController.class.getAnnotation(RequestMapping.class);

        assertNotNull(mapping,
                "MobileColetaController deve preservar @RequestMapping na classe");
        assertArrayEquals(
                new String[]{EXPECTED_CLASS_PATH}, mapping.value(),
                "Path raiz do controller não pode mudar (Req 9.7 + steering "
                        + "endpoints-nao-alterar.md). Esperado: ["
                        + EXPECTED_CLASS_PATH + "], obtido: "
                        + Arrays.toString(mapping.value()));
    }

    @Test
    @DisplayName("Classe permanece @RestController")
    void classePermaneceRestController() {
        assertNotNull(
                MobileColetaController.class.getAnnotation(RestController.class),
                "MobileColetaController deve permanecer @RestController");
    }

    // =========================================================================
    // 2. POST /api/mobile/coletas (registrar coleta individual)
    // =========================================================================

    @Test
    @DisplayName("registrarColeta: @PostMapping na raiz (Req 9.7)")
    void registrarColetaMapeadoComoPostNaRaiz() {
        Method m = resolver("registrarColeta", MobileColetaRequest.class);

        PostMapping post = m.getAnnotation(PostMapping.class);
        assertNotNull(post,
                "registrarColeta deve ser @PostMapping — trocar verbo HTTP "
                        + "viola Req 9.7");

        // Quando @PostMapping não recebe value explícito, o array default é
        // vazio → o endpoint é mapeado na raiz da classe (ou seja,
        // POST /api/mobile/coletas). Aceitamos tanto [] quanto [""].
        String[] paths = post.value();
        boolean mapeadoNaRaiz = paths.length == 0
                || (paths.length == 1 && paths[0].isEmpty());
        assertEquals(true, mapeadoNaRaiz,
                "registrarColeta deve ser mapeado na raiz da classe "
                        + "(POST /api/mobile/coletas), não em subpath. Obtido: "
                        + Arrays.toString(paths));
    }

    @Test
    @DisplayName("registrarColeta: autorização @RequireColetor (Req 9.7)")
    void registrarColetaExigeColetor() {
        Method m = resolver("registrarColeta", MobileColetaRequest.class);

        assertNotNull(m.getAnnotation(RequireColetor.class),
                "registrarColeta deve manter @RequireColetor (matriz de "
                        + "permissões documentada em steering security-roles.md)");
        assertNull(m.getAnnotation(RequireAdmin.class),
                "registrarColeta NÃO pode elevar para @RequireAdmin");
        assertNull(m.getAnnotation(RequireSupervisor.class),
                "registrarColeta NÃO pode restringir para @RequireSupervisor");
        assertNull(m.getAnnotation(RequireConsulta.class),
                "registrarColeta NÃO pode afrouxar para @RequireConsulta");
    }

    @Test
    @DisplayName("registrarColeta: retorno ResponseEntity<ApiResponse<MobileColetaResponse>> (Req 9.7)")
    void registrarColetaRetornaTipoContratual() {
        Method m = resolver("registrarColeta", MobileColetaRequest.class);
        assertReturnTypeMatches(
                m,
                ResponseEntity.class, ApiResponse.class, MobileColetaResponse.class);
    }

    // =========================================================================
    // 3. POST /api/mobile/coletas/batch (registrar em lote)
    // =========================================================================

    @Test
    @DisplayName("registrarColetasEmLote: @PostMapping(\"/batch\") (Req 9.7)")
    void registrarColetasEmLoteMapeadoEmBatch() {
        Method m = resolver("registrarColetasEmLote", MobileColetaBatchRequest.class);

        PostMapping post = m.getAnnotation(PostMapping.class);
        assertNotNull(post,
                "registrarColetasEmLote deve ser @PostMapping — trocar verbo "
                        + "HTTP viola Req 9.7");
        assertArrayEquals(
                new String[]{EXPECTED_BATCH_PATH}, post.value(),
                "Subpath do endpoint batch não pode mudar (Req 9.7). "
                        + "Esperado: [" + EXPECTED_BATCH_PATH + "], obtido: "
                        + Arrays.toString(post.value()));
    }

    @Test
    @DisplayName("registrarColetasEmLote: autorização @RequireColetor (Req 9.7)")
    void registrarColetasEmLoteExigeColetor() {
        Method m = resolver("registrarColetasEmLote", MobileColetaBatchRequest.class);

        assertNotNull(m.getAnnotation(RequireColetor.class),
                "registrarColetasEmLote deve manter @RequireColetor");
        assertNull(m.getAnnotation(RequireAdmin.class),
                "registrarColetasEmLote NÃO pode elevar para @RequireAdmin");
        assertNull(m.getAnnotation(RequireSupervisor.class),
                "registrarColetasEmLote NÃO pode restringir para @RequireSupervisor");
        assertNull(m.getAnnotation(RequireConsulta.class),
                "registrarColetasEmLote NÃO pode afrouxar para @RequireConsulta");
    }

    @Test
    @DisplayName("registrarColetasEmLote: retorno ResponseEntity<ApiResponse<Map<String, Object>>> (Req 9.7)")
    void registrarColetasEmLoteRetornaTipoContratual() {
        Method m = resolver("registrarColetasEmLote", MobileColetaBatchRequest.class);

        // Camada externa: ResponseEntity<ApiResponse<Map<String, Object>>>
        assertEquals(ResponseEntity.class, m.getReturnType(),
                "Retorno deve ser ResponseEntity");

        ParameterizedType responseEntityType = assertInstanceOf(
                ParameterizedType.class, m.getGenericReturnType(),
                "Retorno deve ser ResponseEntity parametrizado");

        Type[] reArgs = responseEntityType.getActualTypeArguments();
        assertEquals(1, reArgs.length);

        ParameterizedType apiResponseType = assertInstanceOf(
                ParameterizedType.class, reArgs[0],
                "ResponseEntity deve estar parametrizado por ApiResponse<...>");
        assertEquals(ApiResponse.class, apiResponseType.getRawType(),
                "Camada intermediária do retorno deve ser ApiResponse (Req 9.7)");

        Type[] arArgs = apiResponseType.getActualTypeArguments();
        assertEquals(1, arArgs.length);

        ParameterizedType mapType = assertInstanceOf(
                ParameterizedType.class, arArgs[0],
                "ApiResponse do batch deve estar parametrizado por Map<String, Object>");
        assertEquals(Map.class, mapType.getRawType(),
                "Estrutura de resposta do batch é Map<String, Object> (contém "
                        + "total/sucesso/falhas/duplicadas/erros/resultados). "
                        + "Trocar para outro tipo viola Req 9.7.");

        Type[] mapArgs = mapType.getActualTypeArguments();
        assertEquals(2, mapArgs.length);
        assertEquals(String.class, mapArgs[0],
                "Chaves do Map de resposta do batch devem permanecer String");
        assertEquals(Object.class, mapArgs[1],
                "Valores do Map de resposta do batch devem permanecer Object "
                        + "para suportar estrutura heterogênea (números, listas, etc.)");
    }

    // =========================================================================
    // 4. DTO MobileColetaRequest: compatibilidade retroativa (Req 9.8)
    // =========================================================================

    @Test
    @DisplayName("MobileColetaRequest.descricaoItemSemEtiqueta existe como String (Req 9.8)")
    void dtoPreservaCampoDescricaoItemSemEtiqueta() throws NoSuchFieldException {
        Field f = MobileColetaRequest.class.getDeclaredField("descricaoItemSemEtiqueta");
        assertEquals(String.class, f.getType(),
                "descricaoItemSemEtiqueta deve permanecer String para suportar "
                        + "texto digitado livremente (Req 9.8)");
    }

    @Test
    @DisplayName("descricaoItemSemEtiqueta permanece OPCIONAL (sem @NotNull/@NotBlank/@NotEmpty/@Size) (Req 9.8)")
    void campoDescricaoItemSemEtiquetaPermaneceOpcional() throws NoSuchFieldException {
        // Req 9.8 exige que requisições legadas (sem os campos novos desta
        // feature) sejam aceitas normalmente. Se o DTO ganhar @NotNull ou
        // congêneres, clientes antigos começam a receber 400 — breaking change.
        //
        // A validação de tamanho [3, 255] é feita programaticamente no
        // controller (ver MobileColetaController#validarDescricaoItemSemEtiqueta)
        // e NÃO via Bean Validation, exatamente porque null precisa passar.
        Field f = MobileColetaRequest.class.getDeclaredField("descricaoItemSemEtiqueta");

        assertNull(f.getAnnotation(NotNull.class),
                "descricaoItemSemEtiqueta NÃO pode ter @NotNull — quebraria "
                        + "compatibilidade com clientes legados (Req 9.8)");
        assertNull(f.getAnnotation(NotBlank.class),
                "descricaoItemSemEtiqueta NÃO pode ter @NotBlank — quebraria "
                        + "compatibilidade com clientes legados (Req 9.8)");
        assertNull(f.getAnnotation(NotEmpty.class),
                "descricaoItemSemEtiqueta NÃO pode ter @NotEmpty — quebraria "
                        + "compatibilidade com clientes legados (Req 9.8)");
        assertNull(f.getAnnotation(Size.class),
                "descricaoItemSemEtiqueta NÃO pode ter @Size (Bean Validation) "
                        + "— a validação 3..255 é feita no controller para permitir "
                        + "null em coletas de clientes legados (Req 9.8)");
    }

    @Test
    @DisplayName("Campos pré-existentes de MobileColetaRequest permanecem OPCIONAIS (Req 9.8)")
    void camposPreexistentesDoRequestPermanecemOpcionais() {
        // Vários campos foram historicamente marcados como opcionais para
        // suportar coletas offline antigas. A Req 9.8 exige que esta feature
        // não torne nenhum deles obrigatório.
        assertCampoOpcional(MobileColetaRequest.class, "numeroPatrimonio");
        assertCampoOpcional(MobileColetaRequest.class, "estadoEncontrado");
        assertCampoOpcional(MobileColetaRequest.class, "usuarioId");
        assertCampoOpcional(MobileColetaRequest.class, "categoriaItemSemEtiqueta");
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private static Method resolver(String nome, Class<?>... parametros) {
        try {
            return MobileColetaController.class.getDeclaredMethod(nome, parametros);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(
                    "Método '" + nome + "(" + Arrays.toString(parametros)
                            + ")' não encontrado em "
                            + MobileColetaController.class.getName()
                            + ". Isso viola a Req 9.7 (contrato dos endpoints de "
                            + "coleta). Restaure a assinatura original.", e);
        }
    }

    /**
     * Verifica se o método tem o retorno parametrizado esperado na forma
     * {@code Outer<Middle<Inner>>}. Falha com mensagem explicativa se
     * qualquer camada divergir.
     */
    private static void assertReturnTypeMatches(
            Method m, Class<?> outer, Class<?> middle, Class<?> inner) {

        assertEquals(outer, m.getReturnType(),
                "Camada externa do retorno deve ser " + outer.getSimpleName());

        Type generic = m.getGenericReturnType();
        ParameterizedType outerType = assertInstanceOf(
                ParameterizedType.class, generic,
                "Retorno deve ser " + outer.getSimpleName() + " parametrizado");

        Type[] outerArgs = outerType.getActualTypeArguments();
        assertEquals(1, outerArgs.length,
                outer.getSimpleName() + " deve ter exatamente 1 parâmetro de tipo");

        ParameterizedType middleType = assertInstanceOf(
                ParameterizedType.class, outerArgs[0],
                outer.getSimpleName() + " deve estar parametrizado por "
                        + middle.getSimpleName() + "<...>");
        assertEquals(middle, middleType.getRawType(),
                "Camada intermediária deve ser " + middle.getSimpleName()
                        + " (Req 9.7)");

        Type[] middleArgs = middleType.getActualTypeArguments();
        assertEquals(1, middleArgs.length);
        assertEquals(inner, middleArgs[0],
                "Camada interna deve ser " + inner.getSimpleName()
                        + " (Req 9.7). Trocar por outro DTO quebra consumidores.");
    }

    private static void assertCampoOpcional(Class<?> classe, String nome) {
        Field f;
        try {
            f = classe.getDeclaredField(nome);
        } catch (NoSuchFieldException e) {
            fail("Campo '" + nome + "' não existe em "
                    + classe.getSimpleName()
                    + " — mas deveria existir como parte do contrato (Req 9.7/9.8)");
            return;
        }

        if (f.getAnnotation(NotNull.class) != null
                || f.getAnnotation(NotBlank.class) != null
                || f.getAnnotation(NotEmpty.class) != null) {
            fail("Campo '" + nome + "' em " + classe.getSimpleName()
                    + " ganhou anotação de obrigatoriedade (NotNull/NotBlank/NotEmpty). "
                    + "Isso viola Req 9.8 — requisições legadas sem esse campo passariam "
                    + "a receber 400. Remova a anotação para manter o contrato.");
        }
    }
}
