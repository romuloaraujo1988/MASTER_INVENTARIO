package com.inventario.sihcp.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import com.inventario.sihcp.util.ConnectionManager;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Property-based tests for pagination and stable ordering of
 * {@link PatrimonioDAO#buscarSugestoesNaoColetadasPaginado(int, String, int, int)}.
 *
 * <p>Feature: {@code coleta-descricao-livre-com-sugestao} — tarefa 3.4 do plano.</p>
 *
 * <h2>Propriedades validadas</h2>
 * <ul>
 *   <li><strong>P11 — Validates: Requirements 5.5, 5.6, 5.7, 6.2, 6.3</strong>:
 *       iterando sobre todas as páginas de um dataset conhecido, a união dos
 *       resultados é igual ao dataset esperado, sem duplicatas nem omissões;
 *       cada página respeita {@code size} e a última tem
 *       {@code count % size} itens (ou {@code size} quando múltiplo exato).</li>
 *   <li><strong>P12 — Validates: Requirements 6.4</strong>: a ordem gerada
 *       pela SQL
 *       {@code ORDER BY unaccent(lower(descricao)) ASC, numero ASC}
 *       é monótona crescente dentro de cada página e permanece monótona ao
 *       concatenar as páginas consecutivas. Para descrições duplicadas, o
 *       desempate é determinístico por {@code numero}.</li>
 * </ul>
 *
 * <h2>Pré-requisitos de ambiente</h2>
 * <p>Idênticos a {@link PatrimonioDAOSugestoesPropertyTest}: PostgreSQL com
 * extensão {@code unaccent} habilitada. Veja o script
 * {@code sql/adicionar_indices_sugestoes_descricao.sql}.</p>
 *
 * <p>Variáveis de ambiente:</p>
 * <ul>
 *   <li>{@code TEST_DB_URL}</li>
 *   <li>{@code TEST_DB_USER}</li>
 *   <li>{@code TEST_DB_PASSWORD}</li>
 * </ul>
 *
 * <h2>Como rodar</h2>
 * <p>Classe marcada com {@link Disabled} por padrão. Remova localmente para
 * rodar com um banco real. Comando:
 * {@code mvn -pl sihcp-server test -Dtest=PatrimonioDAOSugestoesPaginacaoPropertyTest}.</p>
 */
@Tag("property-test")
@Disabled("Requires PostgreSQL TEST_DB_URL env var + unaccent extension - see class Javadoc")
@EnabledIfEnvironmentVariable(named = "TEST_DB_URL", matches = ".+")
@DisplayName("Property P11 + P12: paginação completa e ordenação estável entre páginas")
class PatrimonioDAOSugestoesPaginacaoPropertyTest {

    /** Inventário sintético dedicado a esta classe (valor alto para não conflitar). */
    private static final int TEST_INVENTARIO_ID = 999_911; // "911" = P11/P12
    private static final String NUMERO_PREFIX = "TEST-PAG-";
    private static final int TOTAL_PATRIMONIOS = 123;
    private static final int PAGE_SIZE = 50;

    private PatrimonioDAO dao;

    @BeforeAll
    static void initConnectionPool() {
        if (!ConnectionManager.isInitialized()) {
            ConnectionManager.initialize(
                    System.getenv("TEST_DB_URL"),
                    System.getenv("TEST_DB_USER"),
                    System.getenv("TEST_DB_PASSWORD")
            );
        }
    }

    @BeforeEach
    void setup() throws SQLException {
        dao = new PatrimonioDAO();
        limparDadosDeTeste();
    }

    @AfterEach
    void cleanup() throws SQLException {
        limparDadosDeTeste();
    }

    // ------------------------------------------------------------------
    // P11 — Paginação completa e size respeitado
    // ------------------------------------------------------------------

    /**
     * <strong>Property 11 — Validates: Requirements 5.5, 5.6, 5.7, 6.2, 6.3</strong>
     *
     * <p>Dado um dataset de {@link #TOTAL_PATRIMONIOS}=123 patrimônios ativos,
     * todos não coletados e com descrições únicas, iterar sobre as páginas
     * {@code 0, 1, 2} com {@code size=50} deve:</p>
     * <ul>
     *   <li>produzir {@code 50 + 50 + 23} = 123 itens no total;</li>
     *   <li>a concatenação dos ids é igual em conjunto ao dataset inserido;</li>
     *   <li>não haver duplicatas entre páginas;</li>
     *   <li>a última página conter exatamente {@code 123 mod 50 = 23} itens;</li>
     *   <li>as duas primeiras páginas conterem exatamente 50 itens;</li>
     *   <li>{@code totalElements} reportado ser 123 em todas as páginas;</li>
     *   <li>{@code page} e {@code size} retornados refletirem os parâmetros.</li>
     * </ul>
     */
    @Test
    @DisplayName("P11: paginação completa cobre o dataset, respeita size e não duplica")
    void p11_paginacaoCompletaRespeitaSize() throws SQLException {
        // --- Arrange: 123 patrimônios com descrições únicas e variadas ------
        List<Integer> idsInseridos = new ArrayList<>(TOTAL_PATRIMONIOS);
        for (int i = 0; i < TOTAL_PATRIMONIOS; i++) {
            String numero = String.format("%s%04d", NUMERO_PREFIX, i);
            // Descrições propositalmente misturam caixa e acento para exercitar
            // unaccent(lower(...)). Prefixos diferentes garantem unicidade.
            String descricao = descricaoDeterministica(i);
            idsInseridos.add(inserirPatrimonio(numero, "ATIVO", descricao));
        }

        // --- Act: iterar páginas 0, 1, 2 ------------------------------------
        PagedResult<SugestaoDescricaoRow> p0 =
                dao.buscarSugestoesNaoColetadasPaginado(TEST_INVENTARIO_ID, "", 0, PAGE_SIZE);
        PagedResult<SugestaoDescricaoRow> p1 =
                dao.buscarSugestoesNaoColetadasPaginado(TEST_INVENTARIO_ID, "", 1, PAGE_SIZE);
        PagedResult<SugestaoDescricaoRow> p2 =
                dao.buscarSugestoesNaoColetadasPaginado(TEST_INVENTARIO_ID, "", 2, PAGE_SIZE);

        List<SugestaoDescricaoRow> concatenado = new ArrayList<>();
        concatenado.addAll(p0.items());
        concatenado.addAll(p1.items());
        concatenado.addAll(p2.items());

        Set<Integer> idsRetornados = concatenado.stream()
                .map(SugestaoDescricaoRow::id)
                .collect(Collectors.toSet());

        // --- Assert ---------------------------------------------------------
        assertAll("P11 — invariantes de paginação",
                // tamanhos por página
                () -> assertEquals(PAGE_SIZE, p0.items().size(),
                        "Página 0 deve ter exatamente size=50 itens"),
                () -> assertEquals(PAGE_SIZE, p1.items().size(),
                        "Página 1 deve ter exatamente size=50 itens"),
                () -> assertEquals(TOTAL_PATRIMONIOS - 2 * PAGE_SIZE, p2.items().size(),
                        "Última página deve conter 23 itens (123 - 2*50)"),
                // total e união
                () -> assertEquals(TOTAL_PATRIMONIOS, concatenado.size(),
                        "Concatenação deve ter 123 itens"),
                () -> assertEquals(TOTAL_PATRIMONIOS, idsRetornados.size(),
                        "Sem duplicatas entre páginas"),
                () -> assertEquals(new HashSet<>(idsInseridos), idsRetornados,
                        "Conjunto retornado deve ser igual ao dataset inserido"),
                // metadados reportados
                () -> assertEquals(TOTAL_PATRIMONIOS, p0.totalElements(),
                        "totalElements deve refletir o total do dataset"),
                () -> assertEquals(TOTAL_PATRIMONIOS, p1.totalElements()),
                () -> assertEquals(TOTAL_PATRIMONIOS, p2.totalElements()),
                () -> assertEquals(0, p0.page()),
                () -> assertEquals(1, p1.page()),
                () -> assertEquals(2, p2.page()),
                () -> assertEquals(PAGE_SIZE, p0.size())
        );

        // Propriedade derivada: hasNext = (page+1)*size < totalElements.
        // Calculada pelo service, mas aqui conferimos que os metadados do DAO
        // permitem esse cálculo corretamente.
        assertTrue((0 + 1) * PAGE_SIZE < p0.totalElements(),
                "Após página 0 deve haver mais resultados");
        assertTrue((1 + 1) * PAGE_SIZE < p1.totalElements(),
                "Após página 1 deve haver mais resultados");
        assertFalse((2 + 1) * PAGE_SIZE < p2.totalElements(),
                "Após página 2 não deve haver mais resultados");
    }

    // ------------------------------------------------------------------
    // P12 — Ordenação estável entre páginas
    // ------------------------------------------------------------------

    /**
     * <strong>Property 12 — Validates: Requirements 6.4</strong>
     *
     * <p>A ordem especificada é
     * {@code ORDER BY unaccent(lower(descricao)) ASC, numero ASC}.
     * Esta propriedade verifica que:</p>
     * <ul>
     *   <li>dentro de uma única página a sequência é monótona (cresce ou
     *       mantém-se para a chave composta);</li>
     *   <li>concatenar páginas consecutivas preserva a monotonicidade (sem
     *       "descidas" na fronteira entre páginas);</li>
     *   <li>para descrições duplicadas, o desempate é feito pelo
     *       {@code numero} do patrimônio em ordem crescente.</li>
     * </ul>
     *
     * <p>Para exercitar o desempate, o dataset contém <em>propositalmente</em>
     * descrições repetidas em diferentes {@code numero}s.</p>
     */
    @Test
    @DisplayName("P12: ordenação monótona dentro da página e preservada entre páginas")
    void p12_ordenacaoEstavelEntrePaginas() throws SQLException {
        // --- Arrange: dataset com descrições repetidas em numeros distintos --
        // Usamos 60 patrimônios com 20 descrições, cada uma repetida 3 vezes.
        final int totalItens = 60;
        final int numDescricoesUnicas = 20;
        final String[] descricoes = {
                "Árvore de natal",  // com acento na letra A
                "Bandeira nacional",
                "Cadeira giratória",
                "Caixa arquivo",
                "Escrivaninha branca",
                "Estante de aço",
                "Extintor de incêndio",
                "Ferro de passar",
                "Gaveteiro",
                "Impressora laser",
                "Livro de registro",
                "Mesa de reunião",
                "Microfone sem fio",
                "Monitor LED",
                "Notebook Dell",
                "Projetor multimídia",
                "Quadro branco",
                "Roteador Wi-Fi",
                "Sofá três lugares",
                "Ventilador de coluna"
        };
        assertEquals(numDescricoesUnicas, descricoes.length,
                "Invariante do teste: 20 descrições únicas esperadas");

        for (int i = 0; i < totalItens; i++) {
            String numero = String.format("%s%04d", NUMERO_PREFIX, i);
            String descricao = descricoes[i % numDescricoesUnicas];
            inserirPatrimonio(numero, "ATIVO", descricao);
        }

        // --- Act: coletar páginas 0..N em ordem natural ---------------------
        final int size = 25;
        List<SugestaoDescricaoRow> concatenado = new ArrayList<>();
        int page = 0;
        while (true) {
            PagedResult<SugestaoDescricaoRow> p =
                    dao.buscarSugestoesNaoColetadasPaginado(TEST_INVENTARIO_ID, "", page, size);
            concatenado.addAll(p.items());
            if (p.items().size() < size) {
                break; // última página
            }
            page++;
            if (page > 10) {
                throw new IllegalStateException("Iteração de páginas não convergiu");
            }
        }

        // --- Assert: monotonicidade global ----------------------------------
        assertEquals(totalItens, concatenado.size(),
                "Concatenação de páginas deve cobrir o dataset completo");

        for (int i = 1; i < concatenado.size(); i++) {
            SugestaoDescricaoRow prev = concatenado.get(i - 1);
            SugestaoDescricaoRow curr = concatenado.get(i);

            String keyPrev = chaveDeOrdenacaoUnaccentLower(prev.descricao());
            String keyCurr = chaveDeOrdenacaoUnaccentLower(curr.descricao());

            int cmpDescricao = keyPrev.compareTo(keyCurr);
            if (cmpDescricao > 0) {
                throw new AssertionError(
                        "Descrição fora de ordem entre índices " + (i - 1) + " e " + i + ": " +
                        "'" + prev.descricao() + "' > '" + curr.descricao() + "'");
            }
            if (cmpDescricao == 0) {
                // Desempate por numero ASC
                int cmpNumero = prev.numero().compareTo(curr.numero());
                assertTrue(cmpNumero <= 0,
                        "Desempate por numero violado: prev=" + prev.numero() +
                                ", curr=" + curr.numero());
            }
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /**
     * Gera descrição determinística única para o índice {@code i}, com
     * mistura de caixa e acento para exercitar a normalização do servidor.
     */
    private static String descricaoDeterministica(int i) {
        // Descrições com prefixos de A a Z para distribuir a ordenação.
        // Misturam acentos em algumas posições para exercitar unaccent.
        char letra = (char) ('A' + (i % 26));
        String acento = (i % 3 == 0) ? "ô" : (i % 3 == 1) ? "É" : "a";
        return letra + "quipamento " + acento + " #" + i;
    }

    /**
     * Espelha {@code unaccent(lower(x))} em Java para comparação de chaves
     * de ordenação no código de teste. Usa a mesma estratégia aplicada
     * pelo {@code TextNormalizer} do app Android (NFD + remoção de
     * diacríticos + lowercase pt-BR).
     */
    private static String chaveDeOrdenacaoUnaccentLower(String input) {
        if (input == null) {
            return "";
        }
        String semAcento = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return semAcento.toLowerCase(Locale.forLanguageTag("pt-BR"));
    }

    private int inserirPatrimonio(String numero, String status, String descricao) throws SQLException {
        final String sql =
                "INSERT INTO tabela_patrimonio (numero, status, descricao, data_carga) " +
                "VALUES (?, ?, ?, ?) RETURNING id";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, numero);
            stmt.setString(2, status);
            stmt.setString(3, descricao);
            stmt.setTimestamp(4, Timestamp.from(Instant.now()));

            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                throw new SQLException("INSERT não retornou id para numero=" + numero);
            }
        }
    }

    private void limparDadosDeTeste() throws SQLException {
        try (Connection conn = ConnectionManager.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM tabela_coleta WHERE id_inventario = ? " +
                    " OR id_patrimonio IN (SELECT id FROM tabela_patrimonio WHERE numero LIKE ?)")) {
                stmt.setInt(1, TEST_INVENTARIO_ID);
                stmt.setString(2, NUMERO_PREFIX + "%");
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM tabela_patrimonio WHERE numero LIKE ?")) {
                stmt.setString(1, NUMERO_PREFIX + "%");
                stmt.executeUpdate();
            }
        }
    }
}
