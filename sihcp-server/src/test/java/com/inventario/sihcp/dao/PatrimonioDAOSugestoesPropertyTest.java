package com.inventario.sihcp.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.inventario.sihcp.util.ConnectionManager;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Property-based tests for {@link PatrimonioDAO#buscarSugestoesNaoColetadasPaginado(int, String, int, int)}
 * cobrindo as propriedades <strong>P13</strong> (filtro por inventário alvo e patrimônios não coletados)
 * e <strong>P7</strong> (consistência do filtro acento/caso-insensível entre app e servidor).
 *
 * <p>Feature: {@code coleta-descricao-livre-com-sugestao} — tarefas 3.2 e 3.3 do plano.</p>
 *
 * <h2>Propriedades validadas</h2>
 * <ul>
 *   <li><strong>P13 — Validates: Requirements 5.2, 5.8</strong>: o resultado contém
 *       somente patrimônios ainda não registrados na {@code TABELA_COLETA} para o
 *       inventário alvo e exclui itens de outros inventários.</li>
 *   <li><strong>P7 — Validates: Requirements 3.4, 5.4</strong>: variações
 *       case/accent sobre o mesmo termo de busca produzem o mesmo conjunto de
 *       resultados (espelhando o comportamento do filtro offline do app).</li>
 * </ul>
 *
 * <h2>Pré-requisitos de ambiente</h2>
 * <p>Estes testes requerem uma instância PostgreSQL real com schema equivalente
 * ao de produção (tabelas {@code tabela_patrimonio}, {@code tabela_coleta}) e
 * a extensão {@code unaccent} habilitada, conforme o script
 * {@code sql/adicionar_indices_sugestoes_descricao.sql}.</p>
 *
 * <p>Variáveis de ambiente esperadas:</p>
 * <ul>
 *   <li>{@code TEST_DB_URL} — URL JDBC (ex.: {@code jdbc:postgresql://localhost:5432/sispatrimonio_test})</li>
 *   <li>{@code TEST_DB_USER} — usuário do banco</li>
 *   <li>{@code TEST_DB_PASSWORD} — senha do usuário</li>
 * </ul>
 *
 * <h2>Como rodar</h2>
 * <p>Esta classe está marcada com {@link Disabled} <em>por padrão</em> para não
 * quebrar CI em ambientes sem PostgreSQL. Para rodar localmente:</p>
 * <ol>
 *   <li>Garanta que o banco de teste foi criado e que o script
 *       {@code sql/adicionar_indices_sugestoes_descricao.sql} foi aplicado.</li>
 *   <li>Exporte as variáveis {@code TEST_DB_URL}, {@code TEST_DB_USER},
 *       {@code TEST_DB_PASSWORD}.</li>
 *   <li>Remova (localmente) a anotação {@link Disabled} desta classe.</li>
 *   <li>Execute: {@code mvn -pl sihcp-server test -Dtest=PatrimonioDAOSugestoesPropertyTest}.</li>
 * </ol>
 *
 * <h2>Isolamento de dados</h2>
 * <p>Os testes usam um {@link #TEST_INVENTARIO_ID} dedicado (valor alto,
 * improvável em produção) e um prefixo de {@code NUMERO} ({@link #NUMERO_PREFIX})
 * para identificar e limpar todos os registros criados entre execuções. Nenhum
 * schema existente é alterado — apenas linhas com esse prefixo são inseridas/
 * removidas.</p>
 */
@Tag("property-test")
@Disabled("Requires PostgreSQL TEST_DB_URL env var + unaccent extension - see class Javadoc")
@EnabledIfEnvironmentVariable(named = "TEST_DB_URL", matches = ".+")
@DisplayName("Property P13 + P7: buscarSugestoesNaoColetadasPaginado — filtros de inventário e acento/caso-insensível")
class PatrimonioDAOSugestoesPropertyTest {

    /** Inventário sintético usado exclusivamente por esta classe de testes. */
    private static final int TEST_INVENTARIO_ID = 999_913; // "913" = P13
    private static final int OUTRO_INVENTARIO_ID = 999_914;
    /** Prefixo aplicado a {@code NUMERO} de patrimônio para facilitar cleanup. */
    private static final String NUMERO_PREFIX = "TEST-P13-";

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
    // P13 — Resultado contém apenas patrimônios não coletados do inventário alvo
    // ------------------------------------------------------------------

    /**
     * <strong>Property 13 — Validates: Requirements 5.2, 5.8</strong>
     *
     * <p>Após inserir um conjunto controlado de patrimônios (alguns já coletados
     * no inventário alvo, outros coletados em um inventário diferente, outros
     * ainda não coletados em nenhum), a consulta paginada deve retornar
     * <em>somente</em> os patrimônios que não possuem registro em
     * {@code TABELA_COLETA} para {@link #TEST_INVENTARIO_ID}.</p>
     *
     * <p>As asserções derivam diretamente da especificação da propriedade:</p>
     * <ul>
     *   <li>(a) nenhum item do resultado aparece em
     *       {@code TABELA_COLETA} para {@code TEST_INVENTARIO_ID};</li>
     *   <li>(b) patrimônios coletados em {@code OUTRO_INVENTARIO_ID}
     *       <em>devem</em> aparecer (somente a coleta no inventário alvo filtra);</li>
     *   <li>(c) patrimônios com {@code STATUS} em {@code ('BAIXADO','INATIVO')}
     *       são excluídos mesmo quando não coletados;</li>
     *   <li>(d) descrições nulas ou vazias são excluídas.</li>
     * </ul>
     */
    @Test
    @DisplayName("P13: resultado contém apenas patrimônios não coletados do inventário alvo")
    void p13_apenasNaoColetadosEInventarioAlvo() throws SQLException {
        // --- Arrange: dataset controlado ------------------------------------
        // Não coletado em nenhum inventário — deve aparecer
        int idLivre1 = inserirPatrimonio(NUMERO_PREFIX + "001", "ATIVO", "Cadeira giratória preta");
        int idLivre2 = inserirPatrimonio(NUMERO_PREFIX + "002", "ATIVO", "Mesa de escritório");

        // Coletado em OUTRO inventário — deve aparecer no resultado do alvo
        int idForaDoAlvo = inserirPatrimonio(NUMERO_PREFIX + "003", "ATIVO", "Cadeira giratória azul");
        inserirColeta(idForaDoAlvo, OUTRO_INVENTARIO_ID);

        // Coletado no inventário alvo — NÃO deve aparecer
        int idColetadoAlvo = inserirPatrimonio(NUMERO_PREFIX + "004", "ATIVO", "Armário baixo");
        inserirColeta(idColetadoAlvo, TEST_INVENTARIO_ID);

        // Status BAIXADO — NÃO deve aparecer (mesmo sem coleta)
        int idBaixado = inserirPatrimonio(NUMERO_PREFIX + "005", "BAIXADO", "Ventilador antigo");

        // Descrição vazia / nula — NÃO devem aparecer
        int idSemDescricao = inserirPatrimonio(NUMERO_PREFIX + "006", "ATIVO", "");
        int idDescricaoNull = inserirPatrimonio(NUMERO_PREFIX + "007", "ATIVO", null);

        // --- Act -----------------------------------------------------------
        PagedResult<SugestaoDescricaoRow> pagina =
                dao.buscarSugestoesNaoColetadasPaginado(TEST_INVENTARIO_ID, "", 0, 100);

        // --- Assert --------------------------------------------------------
        List<Integer> idsRetornados = pagina.items().stream()
                .map(SugestaoDescricaoRow::id)
                .toList();

        assertAll("P13 — invariantes do filtro de inventário",
                // (a) nenhum item do resultado está em tabela_coleta para TEST_INVENTARIO_ID
                () -> assertFalse(idsRetornados.contains(idColetadoAlvo),
                        "Patrimônio coletado no inventário alvo NÃO deve aparecer"),
                // (b) patrimônios coletados apenas em outro inventário devem aparecer
                () -> assertTrue(idsRetornados.contains(idForaDoAlvo),
                        "Coleta em outro inventário não deve filtrar o item"),
                // (c) itens BAIXADO/INATIVO são excluídos
                () -> assertFalse(idsRetornados.contains(idBaixado),
                        "Patrimônio com status BAIXADO deve ser excluído"),
                // (d) descrições nulas/vazias são excluídas
                () -> assertFalse(idsRetornados.contains(idSemDescricao),
                        "Descrição vazia deve ser excluída"),
                () -> assertFalse(idsRetornados.contains(idDescricaoNull),
                        "Descrição nula deve ser excluída"),
                // positivos — os dois itens livres aparecem
                () -> assertTrue(idsRetornados.contains(idLivre1),
                        "Patrimônio não coletado deve aparecer"),
                () -> assertTrue(idsRetornados.contains(idLivre2),
                        "Patrimônio não coletado deve aparecer")
        );
    }

    // ------------------------------------------------------------------
    // P7 — Filtro acento/caso-insensível consistente
    // ------------------------------------------------------------------

    /**
     * <strong>Property 7 — Validates: Requirements 3.4, 5.4</strong>
     *
     * <p>Dado um dataset contendo exatamente uma linha cuja descrição é
     * {@code "Cadeira giratória preta"} (com acento), qualquer termo entre as
     * variações abaixo deve retornar essa linha:</p>
     * <ul>
     *   <li>{@code "cadeira"} (tudo minúsculo sem acento)</li>
     *   <li>{@code "CADEIRA"} (tudo maiúsculo sem acento)</li>
     *   <li>{@code "cAdEiRa"} (caixa mista sem acento)</li>
     *   <li>{@code "cadéira"} (minúsculo com acento — variação do usuário)</li>
     *   <li>{@code "CADÉIRA"} (maiúsculo com acento — variação do usuário)</li>
     *   <li>{@code "giratoria"} (sem acento vs. dado com acento)</li>
     *   <li>{@code "GIRATÓRIA"} (maiúsculo com acento)</li>
     * </ul>
     *
     * <p>Isso exercita a cláusula SQL
     * {@code unaccent(lower(p.DESCRICAO)) LIKE '%' || unaccent(lower(?)) || '%'}
     * e confirma que o filtro do servidor é equivalente ao filtro offline do
     * app (que usa {@code Normalizer.Form.NFD} + remoção de diacríticos +
     * {@code lowercase}).</p>
     */
    @ParameterizedTest(name = "termo = \"{0}\" encontra \"Cadeira giratória preta\"")
    @ValueSource(strings = {
            "cadeira",
            "CADEIRA",
            "cAdEiRa",
            "cadéira",
            "CADÉIRA",
            "giratoria",
            "GIRATÓRIA"
    })
    @DisplayName("P7: termo case+accent insensitive retorna a mesma linha")
    void p7_filtroUnaccentLowerConsistente(String termo) throws SQLException {
        // Dataset com exatamente uma linha-alvo + ruídos não-casáveis.
        int idAlvo = inserirPatrimonio(NUMERO_PREFIX + "P7-alvo", "ATIVO", "Cadeira giratória preta");
        inserirPatrimonio(NUMERO_PREFIX + "P7-ruido1", "ATIVO", "Mesa de madeira");
        inserirPatrimonio(NUMERO_PREFIX + "P7-ruido2", "ATIVO", "Ventilador de teto");

        PagedResult<SugestaoDescricaoRow> pagina =
                dao.buscarSugestoesNaoColetadasPaginado(TEST_INVENTARIO_ID, termo, 0, 100);

        List<Integer> ids = pagina.items().stream().map(SugestaoDescricaoRow::id).toList();

        assertTrue(ids.contains(idAlvo),
                "Termo \"" + termo + "\" deve encontrar a linha-alvo independentemente de caixa/acento");
        assertEquals(1, ids.size(),
                "Termo \"" + termo + "\" deve retornar exatamente a linha-alvo (sem casar ruídos)");
    }

    // ------------------------------------------------------------------
    // Helpers de dataset
    // ------------------------------------------------------------------

    private int inserirPatrimonio(String numero, String status, String descricao) throws SQLException {
        final String sql =
                "INSERT INTO tabela_patrimonio (numero, status, descricao, data_carga) " +
                "VALUES (?, ?, ?, ?) RETURNING id";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, numero);
            stmt.setString(2, status);
            if (descricao == null) {
                stmt.setNull(3, Types.VARCHAR);
            } else {
                stmt.setString(3, descricao);
            }
            stmt.setTimestamp(4, Timestamp.from(Instant.now()));

            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                throw new SQLException("INSERT não retornou id para numero=" + numero);
            }
        }
    }

    private void inserirColeta(int idPatrimonio, int idInventario) throws SQLException {
        // Usa apenas colunas não-nulas estritamente necessárias para o
        // anti-join da consulta sob teste (id_patrimonio, id_inventario).
        // Demais colunas seguem defaults / null se permitido pelo schema.
        final String sql =
                "INSERT INTO tabela_coleta (id_patrimonio, id_inventario, data_coleta) " +
                "VALUES (?, ?, ?)";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPatrimonio);
            stmt.setInt(2, idInventario);
            stmt.setTimestamp(3, Timestamp.from(Instant.now()));
            stmt.executeUpdate();
        }
    }

    private void limparDadosDeTeste() throws SQLException {
        try (Connection conn = ConnectionManager.getConnection()) {
            // Remove coletas antes de patrimônios (FK).
            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM tabela_coleta WHERE id_inventario IN (?, ?) " +
                    " OR id_patrimonio IN (SELECT id FROM tabela_patrimonio WHERE numero LIKE ?)")) {
                stmt.setInt(1, TEST_INVENTARIO_ID);
                stmt.setInt(2, OUTRO_INVENTARIO_ID);
                stmt.setString(3, NUMERO_PREFIX + "%");
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
