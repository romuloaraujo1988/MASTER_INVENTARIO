package com.inventario.sihcp.mobile.server.controller;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.inventario.sihcp.mobile.server.dto.PagedResponseDTO;
import com.inventario.sihcp.mobile.server.service.MobileAuthService;
import com.inventario.sihcp.mobile.server.service.MobileSugestaoDescricaoService;
import com.inventario.sihcp.security.JwtTokenProvider;

import io.jsonwebtoken.JwtException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Property test P18 — Autorização baseada em role do JWT no endpoint
 * {@code GET /api/mobile/descricoes/sugestoes}.
 *
 * <p><strong>Validates: Requirements 10.1, 10.2, 10.3</strong></p>
 *
 * <p>Exercita a tabela de decisão de autorização do endpoint novo da feature
 * {@code coleta-descricao-livre-com-sugestao}, combinando estados do token
 * JWT (ausente, expirado, malformado, assinatura inválida, válido) com os
 * papéis (roles) presentes na hierarquia do sistema
 * (ver steering {@code security-roles.md}):</p>
 *
 * <table>
 *   <caption>Matriz de autorização esperada</caption>
 *   <tr><th>Token</th><th>Role</th><th>Status HTTP</th><th>Requisito</th></tr>
 *   <tr><td>Ausente</td>               <td>—</td>          <td>401</td><td>10.1, 10.2</td></tr>
 *   <tr><td>Expirado</td>              <td>—</td>          <td>401</td><td>10.2</td></tr>
 *   <tr><td>Malformado</td>            <td>—</td>          <td>401</td><td>10.2</td></tr>
 *   <tr><td>Assinatura inválida</td>   <td>—</td>          <td>401</td><td>10.2</td></tr>
 *   <tr><td>Válido</td>                <td>CONSULTA</td>   <td>403</td><td>10.3</td></tr>
 *   <tr><td>Válido</td>                <td>COLETOR</td>    <td>200</td><td>10.1</td></tr>
 *   <tr><td>Válido</td>                <td>SUPERVISOR</td> <td>200</td><td>10.1</td></tr>
 *   <tr><td>Válido</td>                <td>ADMIN</td>      <td>200</td><td>10.1</td></tr>
 * </table>
 *
 * <h2>Status: {@link Disabled}</h2>
 *
 * <p>Este teste está desabilitado por padrão porque requer o contexto
 * completo do Spring Boot ({@link SpringBootTest} + {@link AutoConfigureMockMvc})
 * com beans de segurança configurados ({@code MobileSecurityConfig},
 * {@code JwtAuthenticationEntryPoint}, {@code MobileJwtAuthenticationFilter}).
 * Subir o contexto mobile completo em CI exige:</p>
 *
 * <ul>
 *   <li>Profile de teste com {@code application-test.properties} apontando
 *       para banco H2/Testcontainers (o profile {@code mobile} padrão tenta
 *       conectar em PostgreSQL de produção via {@code configuracao_banco.json}).</li>
 *   <li>Mocks adicionais para {@code PatrimonioDAO}/{@code InventarioDAO}
 *       ou um banco de teste com as tabelas mínimas criadas.</li>
 *   <li>Chave JWT estável via {@code jwt.secret} no profile de teste para
 *       que a geração de tokens via {@link JwtTokenProvider} seja
 *       determinística entre as execuções.</li>
 * </ul>
 *
 * <p>Enquanto a infraestrutura de testes de integração não estiver pronta,
 * o esqueleto permanece aqui (compilando) como contrato vivo do que P18
 * deve validar. Para habilitar localmente:</p>
 *
 * <ol>
 *   <li>Remover a anotação {@link Disabled}.</li>
 *   <li>Criar {@code src/test/resources/application-test.properties} com
 *       {@code spring.profiles.active=test} e uma configuração de banco de
 *       teste (H2 em memória ou Testcontainers PostgreSQL com
 *       {@code unaccent}).</li>
 *   <li>Anotar esta classe com
 *       {@code @ActiveProfiles("test")}.</li>
 *   <li>Executar via {@code mvn -pl sihcp-server test
 *       -Dtest=MobileDescricaoControllerAuthPropertyTest}.</li>
 * </ol>
 *
 * <p>Referências:</p>
 * <ul>
 *   <li>Steering {@code security-roles.md} — hierarquia de perfis</li>
 *   <li>Steering {@code endpoints-nao-alterar.md} — contrato preservado</li>
 *   <li>{@code design.md} da feature — Property 18</li>
 *   <li>Requirements 10.1, 10.2, 10.3 — autenticação/autorização JWT</li>
 * </ul>
 *
 * @author Sistema de Inventário
 * @since 2.7.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@Tag("property-test")
@Tag("integration")
@DisplayName("Property Test P18: Autorização JWT no endpoint GET /api/mobile/descricoes/sugestoes")
@Disabled("Requer contexto Spring Boot de integração mobile com profile de teste. "
        + "Ver Javadoc da classe para instruções de habilitação.")
class MobileDescricaoControllerAuthPropertyTest {

    private static final String ENDPOINT = "/api/mobile/descricoes/sugestoes";

    @Autowired
    private MockMvc mockMvc;

    /**
     * Provedor real — usado para gerar tokens válidos assinados com a chave
     * configurada no profile de teste. Não é mockado para garantir que os
     * tokens passem pela validação criptográfica real do filtro JWT.
     *
     * <p>Atualmente não é referenciado porque {@link TokenFactory} usa
     * placeholders; será consumido quando o teste for habilitado e os
     * tokens forem gerados via {@link JwtTokenProvider#generateToken(
     * org.springframework.security.core.Authentication)}.</p>
     */
    @SuppressWarnings("unused")
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Mock do serviço de autenticação para isolar o teste do acesso a banco
     * (consulta de usuário, blacklist de tokens, etc.). Configurado por
     * cenário em {@link #configurarMobileAuthService(AuthScenario)}.
     */
    @MockBean
    private MobileAuthService mobileAuthService;

    /**
     * Mock do serviço de domínio para que o endpoint retorne 200 determinístico
     * em cenários de sucesso, sem depender de dados reais no banco.
     */
    @MockBean
    private MobileSugestaoDescricaoService mobileSugestaoDescricaoService;

    /**
     * Mock do carregador de usuários — reutilizado pelo filtro JWT para montar
     * o {@link org.springframework.security.core.context.SecurityContext}
     * com as authorities correspondentes ao role do cenário.
     */
    @MockBean
    private UserDetailsService userDetailsService;

    /**
     * Property P18 principal: para toda combinação (token, role) na matriz
     * de decisão, o endpoint deve responder exatamente o status HTTP
     * esperado.
     *
     * <p>A propriedade é paramétrica por {@link AuthScenario} e cobre os
     * 8 casos enumerados em {@link #scenarios()}. Para escalar a cobertura,
     * a versão "full property" pode gerar tokens aleatórios (bytes inválidos,
     * assinaturas variáveis) e cruzar com todas as roles — a abordagem
     * tabular atual cobre classes de equivalência e é suficiente para
     * garantir o contrato.</p>
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("scenarios")
    @DisplayName("Tabela de decisão: token × role ⇒ status HTTP esperado")
    void authorizationMatrix(AuthScenario scenario) throws Exception {
        configurarMobileAuthService(scenario);
        configurarUserDetailsService(scenario);
        configurarMobileSugestaoDescricaoService();

        MockHttpServletRequestBuilder request = get(ENDPOINT)
                .accept(MediaType.APPLICATION_JSON);

        if (scenario.authorizationHeader() != null) {
            request.header("Authorization", scenario.authorizationHeader());
        }

        mockMvc.perform(request)
                .andExpect(status().is(scenario.expectedStatus()));
    }

    /**
     * Fonte de cenários da matriz de decisão. Cada cenário é construído
     * com um {@link TokenFixture} (o que o cliente envia) cruzado com um
     * {@link RoleFixture} (o que o sistema reconhece para aquele token).
     *
     * <p>A construção tardia do token válido em cada cenário permite que
     * o {@link JwtTokenProvider} (injetado via contexto Spring) seja
     * usado para gerar tokens realmente assinados com a chave do profile
     * de teste.</p>
     */
    static Stream<AuthScenario> scenarios() {
        return Stream.of(
                // Ausência de token — 401 (Req 10.1, 10.2)
                new AuthScenario(
                        "Sem header Authorization ⇒ 401",
                        TokenFixture.AUSENTE, RoleFixture.NENHUM, 401),
                // Token expirado — 401 (Req 10.2)
                new AuthScenario(
                        "Token expirado ⇒ 401",
                        TokenFixture.EXPIRADO, RoleFixture.NENHUM, 401),
                // Token malformado — 401 (Req 10.2)
                new AuthScenario(
                        "Token malformado (não-JWS) ⇒ 401",
                        TokenFixture.MALFORMADO, RoleFixture.NENHUM, 401),
                // Token com assinatura inválida — 401 (Req 10.2)
                new AuthScenario(
                        "Token com assinatura inválida ⇒ 401",
                        TokenFixture.ASSINATURA_INVALIDA, RoleFixture.NENHUM, 401),
                // Token válido + CONSULTA — 403 (Req 10.3)
                new AuthScenario(
                        "Token válido + CONSULTA ⇒ 403",
                        TokenFixture.VALIDO, RoleFixture.CONSULTA, 403),
                // Token válido + COLETOR — 200 (Req 10.1)
                new AuthScenario(
                        "Token válido + COLETOR ⇒ 200",
                        TokenFixture.VALIDO, RoleFixture.COLETOR, 200),
                // Token válido + SUPERVISOR — 200 (Req 10.1)
                new AuthScenario(
                        "Token válido + SUPERVISOR ⇒ 200",
                        TokenFixture.VALIDO, RoleFixture.SUPERVISOR, 200),
                // Token válido + ADMIN — 200 (Req 10.1)
                new AuthScenario(
                        "Token válido + ADMIN ⇒ 200",
                        TokenFixture.VALIDO, RoleFixture.ADMIN, 200)
        );
    }

    // =========================================================================
    // Configuração de mocks por cenário
    // =========================================================================

    /**
     * Configura {@link MobileAuthService} para refletir o estado do token
     * do cenário. O filtro consulta {@code validateToken} e
     * {@code getUsernameFromToken} após o parse estrito do
     * {@link JwtTokenProvider}.
     */
    private void configurarMobileAuthService(AuthScenario scenario) {
        switch (scenario.token()) {
            case VALIDO -> {
                when(mobileAuthService.validateToken(anyString())).thenReturn(true);
                when(mobileAuthService.getUsernameFromToken(anyString()))
                        .thenReturn(scenario.role().username());
            }
            case EXPIRADO, MALFORMADO, ASSINATURA_INVALIDA -> {
                // O filtro estrito lança JwtException antes de chegar aqui, mas
                // mantemos o mock defensivo para não vazar stubs entre cenários.
                when(mobileAuthService.validateToken(anyString())).thenReturn(false);
                when(mobileAuthService.getUsernameFromToken(anyString())).thenReturn(null);
            }
            case AUSENTE -> {
                // Nada a fazer — filtro nem chega a consultar o MobileAuthService.
            }
        }
    }

    /**
     * Configura {@link UserDetailsService} para devolver um {@link UserDetails}
     * com as authorities do role do cenário (quando aplicável). Em cenários
     * sem role válida, lança {@link org.springframework.security.core.userdetails.UsernameNotFoundException}
     * para simular usuário inexistente.
     */
    private void configurarUserDetailsService(AuthScenario scenario) {
        if (scenario.role() == RoleFixture.NENHUM) {
            return;
        }
        UserDetails userDetails = User.withUsername(scenario.role().username())
                .password("{noop}irrelevante")
                .authorities(scenario.role().authorities())
                .build();
        when(userDetailsService.loadUserByUsername(scenario.role().username()))
                .thenReturn(userDetails);
    }

    /**
     * Configura a resposta determinística do serviço de domínio para cenários
     * de sucesso. Nos cenários 401/403 o serviço nunca é invocado (a cadeia
     * de filtros bloqueia antes do handler). O stub aqui apenas garante que,
     * se for invocado, a resposta seja uma página vazia bem formada.
     *
     * <p>Para verificar a ausência de invocação em cenários 401/403, usar
     * {@link org.mockito.Mockito#verifyNoInteractions(Object...)} em
     * assertions futuras (não inclusas no skeleton atual).</p>
     */
    private void configurarMobileSugestaoDescricaoService() {
        when(mobileSugestaoDescricaoService.listarSugestoes(
                any(), any(), any(), any()))
                .thenReturn(PagedResponseDTO.empty(false));
    }

    // =========================================================================
    // Fixtures
    // =========================================================================

    /**
     * Representa um cenário da matriz de decisão. A conversão token-fixture →
     * header HTTP é feita em {@link #authorizationHeader()} de forma tardia
     * para que a geração do token válido dependa do {@link JwtTokenProvider}
     * do contexto Spring.
     */
    record AuthScenario(String name, TokenFixture token, RoleFixture role, int expectedStatus) {
        @Override
        public String toString() {
            return name;
        }

        /**
         * Converte o fixture do token no valor efetivo do header
         * {@code Authorization}. Retorna {@code null} quando o cenário é
         * "sem token" (o header não deve ser enviado).
         *
         * <p>Nota: a implementação atual é um placeholder. Quando a classe
         * for habilitada, a geração dos tokens concretos (principalmente
         * {@link TokenFixture#VALIDO} e {@link TokenFixture#EXPIRADO}) deve
         * usar o {@link JwtTokenProvider} do contexto Spring com um usuário
         * sintético do {@link RoleFixture}. Como o método é estático, essa
         * geração precisa ser injetada via método de instância ou
         * {@link org.junit.jupiter.api.BeforeEach}.</p>
         */
        String authorizationHeader() {
            return switch (token) {
                case AUSENTE              -> null;
                case VALIDO               -> "Bearer " + TokenFactory.placeholderValid(role);
                case EXPIRADO             -> "Bearer " + TokenFactory.placeholderExpired();
                case MALFORMADO           -> "Bearer abc.def"; // não é um JWS
                case ASSINATURA_INVALIDA  -> "Bearer " + TokenFactory.placeholderTamperedSignature();
            };
        }
    }

    /** Classes de equivalência para o token enviado pelo cliente. */
    enum TokenFixture {
        AUSENTE,
        VALIDO,
        EXPIRADO,
        MALFORMADO,
        ASSINATURA_INVALIDA
    }

    /**
     * Classes de equivalência para o papel do usuário associado ao token
     * válido. Cada papel mapeia para as authorities Spring Security
     * correspondentes à hierarquia descrita em {@code security-roles.md}.
     */
    enum RoleFixture {
        NENHUM("n/a", List.of()),
        CONSULTA("consulta_user", List.of(new SimpleGrantedAuthority("ROLE_CONSULTA"))),
        COLETOR("coletor_user", List.of(new SimpleGrantedAuthority("ROLE_COLETOR"))),
        SUPERVISOR("supervisor_user", List.of(new SimpleGrantedAuthority("ROLE_SUPERVISOR"))),
        ADMIN("admin_user", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        private final String username;
        private final List<SimpleGrantedAuthority> authorities;

        RoleFixture(String username, List<SimpleGrantedAuthority> authorities) {
            this.username = username;
            this.authorities = authorities;
        }

        String username() { return username; }

        Collection<SimpleGrantedAuthority> authorities() { return authorities; }
    }

    /**
     * Utilidade de geração de tokens de teste. Como {@link AuthScenario} é
     * um {@code record} estático, a geração concreta deve ser feita aqui ou
     * via um hook de instância (a habilitar quando o teste sair do estado
     * {@link Disabled}).
     *
     * <p>Placeholders atuais produzem strings sintaticamente plausíveis mas
     * sem assinatura real — suficientes para o skeleton compilar. Quando o
     * teste for habilitado, substituir por geração real via
     * {@link JwtTokenProvider} (token válido assinado com a chave do profile
     * de teste, token expirado forjado com {@code exp} no passado, e token
     * com assinatura adulterada por mutação do último segmento).</p>
     */
    private static final class TokenFactory {

        private TokenFactory() { /* util */ }

        static String placeholderValid(RoleFixture role) {
            // TODO: substituir por jwtTokenProvider.generateToken(authenticationFor(role))
            //       quando o teste for habilitado (requer refatoração para método não-estático).
            return "placeholder.valid." + role.name().toLowerCase();
        }

        static String placeholderExpired() {
            // TODO: forjar token com exp=epoch.past e re-assinar com a chave do profile.
            return "placeholder.expired.token";
        }

        static String placeholderTamperedSignature() {
            // Token com formato JWS mas última parte adulterada — provoca JwtException.
            return "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0In0.TAMPERED_SIGNATURE_XYZ";
        }
    }

    /**
     * Handler de exceção alinhado ao contrato do {@code JwtAuthenticationEntryPoint}.
     * Mantido como referência no Javadoc — não é instanciado enquanto o teste
     * estiver desabilitado.
     */
    @SuppressWarnings("unused")
    private static final Class<? extends Throwable> EXPECTED_INVALID_TOKEN_CAUSE = JwtException.class;
}
