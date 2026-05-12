package com.inventario.sihcp.mobile.server.controller;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inventario.sihcp.mobile.server.dto.ApiResponse;
import com.inventario.sihcp.security.annotation.RequireAdmin;
import com.inventario.sihcp.security.annotation.RequireColetor;
import com.inventario.sihcp.security.annotation.RequireConsulta;
import com.inventario.sihcp.security.annotation.RequireSupervisor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Smoke test de contrato para o endpoint legado
 * {@code GET /api/mobile/descricoes/nao-coletadas}.
 *
 * <p><strong>Validates: Requirements 5.9</strong></p>
 *
 * <p>Feature: {@code coleta-descricao-livre-com-sugestao} — tarefa 5.3.</p>
 *
 * <h2>Por que é um "smoke test de contrato" e não um teste HTTP</h2>
 *
 * <p>A Req 5.9 exige que os endpoints existentes sob
 * {@code /api/mobile/descricoes/} permaneçam <b>intocados</b> após a
 * introdução do novo endpoint {@code GET /sugestoes}. A regra steering
 * {@code endpoints-nao-alterar.md} é categórica: nenhuma URL, método,
 * parâmetro, tipo de retorno, obrigatoriedade ou autorização pode mudar.</p>
 *
 * <p>Em vez de subir um contexto Spring Boot completo (que é lento, depende
 * de banco e mascara regressões estáticas), este teste usa <b>reflexão</b>
 * para inspecionar a superfície pública do controller como um "snapshot
 * estrutural" pré-feature:</p>
 *
 * <ol>
 *   <li>URL base da classe: {@code /api/mobile/descricoes}.</li>
 *   <li>Método {@code listarDescricoesNaoColetadas(Integer)} existe com
 *       o mesmo nome e assinatura.</li>
 *   <li>Verbo HTTP e path do método: {@code @GetMapping("/nao-coletadas")}.</li>
 *   <li>Tipo de retorno generico preservado:
 *       {@code ResponseEntity<ApiResponse<List<String>>>}.</li>
 *   <li>Parâmetro {@code idInventario} com {@code @RequestParam(required = false)}
 *       — permanece opcional para preservar compatibilidade retroativa.</li>
 *   <li>Autorização herdada da classe ({@code @RequireColetor}) — o método
 *       NÃO sobrescreve com {@code @RequireAdmin}/{@code @RequireSupervisor}/
 *       {@code @RequireConsulta}.</li>
 *   <li>Método NÃO foi anotado com {@code @Deprecated} — o endpoint legado
 *       continua sendo de primeira classe para clientes antigos.</li>
 * </ol>
 *
 * <p>Se qualquer dessas invariantes mudar, o teste falha imediatamente no
 * build — evidência concreta de violação da Req 5.9.</p>
 *
 * <p>O teste roda sem runtime Spring (apenas reflexão + JUnit 5). Não
 * requer banco, rede, nem contexto de aplicação.</p>
 *
 * @author Sistema de Inventário
 * @since 2.7.0
 */
@Tag("smoke-test")
@DisplayName("Contrato legado: GET /api/mobile/descricoes/nao-coletadas (Req 5.9)")
class MobileDescricaoControllerContratoLegadoTest {

    /** Nome exato do método do endpoint legado — parte do contrato público. */
    private static final String METHOD_NAME = "listarDescricoesNaoColetadas";

    /** Path esperado da classe — imutável por Req 5.9 / steering endpoints-nao-alterar. */
    private static final String EXPECTED_CLASS_PATH = "/api/mobile/descricoes";

    /** Path esperado do método — imutável por Req 5.9 / steering endpoints-nao-alterar. */
    private static final String EXPECTED_METHOD_PATH = "/nao-coletadas";

    /**
     * Resolve o {@link Method} alvo uma única vez. A falha aqui significa
     * que o método foi renomeado, removido ou teve o tipo de parâmetro
     * alterado — violação direta da Req 5.9.
     */
    private static Method resolverMetodoLegado() {
        try {
            return MobileDescricaoController.class.getDeclaredMethod(
                    METHOD_NAME, Integer.class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(
                    "Método legado " + METHOD_NAME + "(Integer) não encontrado em "
                            + MobileDescricaoController.class.getName()
                            + ". Isso viola a Req 5.9 (endpoints existentes intocados). "
                            + "Restaure a assinatura original do método.",
                    e);
        }
    }

    // =========================================================================
    // 1. URL base da classe
    // =========================================================================

    @Test
    @DisplayName("Classe tem @RequestMapping(\"/api/mobile/descricoes\") (Req 5.9)")
    void classeMantemRequestMappingOriginal() {
        RequestMapping mapping =
                MobileDescricaoController.class.getAnnotation(RequestMapping.class);

        assertNotNull(mapping,
                "MobileDescricaoController deve preservar @RequestMapping no nível de classe");
        assertArrayEquals(
                new String[]{EXPECTED_CLASS_PATH}, mapping.value(),
                "Path raiz do controller não pode mudar. "
                        + "Regra steering endpoints-nao-alterar.md + Req 5.9. "
                        + "Esperado: [" + EXPECTED_CLASS_PATH + "], "
                        + "obtido: " + Arrays.toString(mapping.value()));
    }

    @Test
    @DisplayName("Classe é anotada com @RestController")
    void classePermaneceRestController() {
        // RestController é marker de que os retornos são serializados para
        // o body (não para views). Parte do contrato implícito do endpoint.
        assertNotNull(
                MobileDescricaoController.class.getAnnotation(RestController.class),
                "MobileDescricaoController deve permanecer anotado com @RestController");
    }

    // =========================================================================
    // 2. Autorização a nível de classe
    // =========================================================================

    @Test
    @DisplayName("Classe mantém @RequireColetor (autorização herdada) (Req 5.9)")
    void classeMantemRequireColetor() {
        // A steering security-roles.md exige @RequireColetor a nível de classe
        // para MobileDescricaoController. Mudar isso altera o comportamento
        // de 403 para todos os endpoints e viola Req 5.9.
        assertNotNull(
                MobileDescricaoController.class.getAnnotation(RequireColetor.class),
                "Classe deve manter @RequireColetor como autorização padrão. "
                        + "Remover/trocar essa anotação altera a matriz de perfis "
                        + "do endpoint legado e viola Req 5.9.");
    }

    // =========================================================================
    // 3. Assinatura do método: nome, parâmetros, tipo de retorno
    // =========================================================================

    @Test
    @DisplayName("Método " + METHOD_NAME + "(Integer) existe com nome e parâmetro originais")
    void metodoPossuiAssinaturaEsperada() {
        Method m = resolverMetodoLegado();

        assertEquals(METHOD_NAME, m.getName(),
                "Nome do método legado não pode mudar (Req 5.9)");
        assertEquals(1, m.getParameterCount(),
                "Método legado deve continuar recebendo exatamente 1 parâmetro "
                        + "(idInventario). Adicionar/remover parâmetros é breaking change.");
        assertEquals(Integer.class, m.getParameterTypes()[0],
                "Tipo do parâmetro idInventario deve permanecer Integer "
                        + "(boxed, para permitir ausência com required=false)");
    }

    @Test
    @DisplayName("Tipo de retorno: ResponseEntity<ApiResponse<List<String>>> (Req 5.9)")
    void tipoDeRetornoEhResponseEntityDeApiResponseDeListDeString() {
        Method m = resolverMetodoLegado();

        // Camada externa: ResponseEntity
        assertEquals(ResponseEntity.class, m.getReturnType(),
                "Retorno deve ser ResponseEntity");

        Type generic = m.getGenericReturnType();
        ParameterizedType responseEntityType = assertInstanceOf(
                ParameterizedType.class, generic,
                "Retorno deve ser ResponseEntity parametrizado");

        // Camada intermediária: ApiResponse<...>
        Type[] responseEntityArgs = responseEntityType.getActualTypeArguments();
        assertEquals(1, responseEntityArgs.length);
        ParameterizedType apiResponseType = assertInstanceOf(
                ParameterizedType.class, responseEntityArgs[0],
                "ResponseEntity deve estar parametrizado por ApiResponse<...>");
        assertEquals(ApiResponse.class, apiResponseType.getRawType(),
                "Camada intermediária do retorno deve ser ApiResponse, "
                        + "não uma estrutura alternativa (Req 5.9)");

        // Camada interna: List<String>
        Type[] apiResponseArgs = apiResponseType.getActualTypeArguments();
        assertEquals(1, apiResponseArgs.length);
        ParameterizedType listType = assertInstanceOf(
                ParameterizedType.class, apiResponseArgs[0],
                "ApiResponse deve estar parametrizado por List<...>");
        assertEquals(List.class, listType.getRawType(),
                "Dados retornados devem ser List<String>, não Map/Set/outra "
                        + "estrutura (Req 5.9 — preserva compat com app Android "
                        + "que hoje espera [\"descricao1\", \"descricao2\", ...])");

        Type[] listArgs = listType.getActualTypeArguments();
        assertEquals(1, listArgs.length);
        assertEquals(String.class, listArgs[0],
                "Elementos da lista devem ser String (não Map/objeto). "
                        + "Correção 01/12/2025 mencionada no javadoc do controller "
                        + "estabeleceu esse contrato — Req 5.9 o protege.");
    }

    // =========================================================================
    // 4. Mapeamento HTTP do método
    // =========================================================================

    @Test
    @DisplayName("Método tem @GetMapping(\"/nao-coletadas\") (Req 5.9)")
    void metodoTemGetMappingCorreto() {
        Method m = resolverMetodoLegado();

        GetMapping get = m.getAnnotation(GetMapping.class);
        assertNotNull(get,
                "Método legado deve ser @GetMapping. "
                        + "Trocar verbo HTTP (POST/PUT/etc.) viola Req 5.9.");
        assertArrayEquals(
                new String[]{EXPECTED_METHOD_PATH}, get.value(),
                "Path do endpoint legado não pode mudar. "
                        + "Esperado: [" + EXPECTED_METHOD_PATH + "], "
                        + "obtido: " + Arrays.toString(get.value()) + ". "
                        + "URL completa esperada: "
                        + EXPECTED_CLASS_PATH + EXPECTED_METHOD_PATH);
    }

    // =========================================================================
    // 5. Parâmetro idInventario: opcional e com nome preservado
    // =========================================================================

    @Test
    @DisplayName("Parâmetro idInventario tem @RequestParam(required = false) (Req 5.9)")
    void parametroIdInventarioPermaneceOpcional() {
        Method m = resolverMetodoLegado();

        RequestParam reqParam =
                m.getParameters()[0].getAnnotation(RequestParam.class);
        assertNotNull(reqParam,
                "Parâmetro idInventario deve continuar anotado com @RequestParam");
        assertFalse(reqParam.required(),
                "idInventario deve permanecer required=false para backward "
                        + "compatibility. Clientes legados chamam o endpoint sem "
                        + "esse parâmetro (o servidor resolve via inventário ativo). "
                        + "Torná-lo obrigatório quebraria esses clientes — Req 5.9.");
    }

    // =========================================================================
    // 6. Método NÃO sobrescreve autorização da classe
    // =========================================================================

    @Test
    @DisplayName("Método NÃO sobrescreve @RequireColetor herdado da classe (Req 5.9)")
    void metodoNaoSobrescreveAutorizacao() {
        Method m = resolverMetodoLegado();

        // Qualquer anotação de autorização a nível de método sobrescreve a
        // da classe. Para preservar o contrato, o método legado DEVE deixar
        // a classe decidir — ou seja, NÃO pode ter nenhuma dessas.
        assertNull(m.getAnnotation(RequireAdmin.class),
                "Método não pode ter @RequireAdmin — isso elevaria a role "
                        + "mínima e bloquearia coletores que hoje conseguem acessar.");
        assertNull(m.getAnnotation(RequireSupervisor.class),
                "Método não pode ter @RequireSupervisor — idem.");
        assertNull(m.getAnnotation(RequireConsulta.class),
                "Método não pode ter @RequireConsulta — isso afrouxaria a "
                        + "autorização e permitiria perfil CONSULTA que hoje é negado.");
        // @RequireColetor a nível de método é redundante (já herdado) mas
        // NÃO altera comportamento — portanto não é proibido. Não testamos.
    }

    // =========================================================================
    // 7. Método NÃO foi deprecado
    // =========================================================================

    @Test
    @DisplayName("Endpoint legado NÃO foi anotado com @Deprecated (Req 5.9)")
    void endpointLegadoNaoFoiDeprecated() {
        Method m = resolverMetodoLegado();

        // @Deprecated sinaliza intenção de remoção e é um warning visível
        // para consumidores. A Req 5.9 mantém o endpoint como de primeira
        // classe; sua deprecação exigiria acordo explícito.
        assertNull(m.getAnnotation(Deprecated.class),
                "Método legado deve permanecer sem @Deprecated. "
                        + "A Req 5.9 exige que o endpoint continue sendo "
                        + "suportado ativamente, não apenas tolerado.");
    }
}
