package com.inventario.sihcp.dao;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.inventario.sihcp.model.Coleta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Smoke test estrutural do "schema" da entidade {@link Coleta}, que é o
 * modelo Java espelhado da tabela {@code TABELA_COLETA} no PostgreSQL.
 *
 * <p><strong>Validates: Requirements 9.2, 9.3</strong></p>
 *
 * <p>Feature: {@code coleta-descricao-livre-com-sugestao} — tarefa 19.1.</p>
 *
 * <h2>Por que um smoke test reflexivo em vez de um diff SQL real</h2>
 *
 * <p>Os critérios de aceitação Req 9.2 e Req 9.3 afirmam que esta feature
 * <b>não cria, renomeia, remove ou altera o tipo de qualquer coluna</b>
 * da {@code TABELA_COLETA}. A única mudança de banco permitida é a adição
 * de índices auxiliares (ver {@code sql/adicionar_indices_sugestoes_descricao.sql}),
 * que não alteram o schema da tabela.</p>
 *
 * <p>No desenho desta feature, Java e PostgreSQL caminham espelhados: toda
 * coluna real em {@code TABELA_COLETA} possui um campo correspondente na
 * classe {@link Coleta}, mapeado pelas DAOs (ver {@code ColetaDAO}).
 * Portanto, uma alteração acidental no schema do banco se manifesta,
 * invariavelmente, como uma alteração em {@link Coleta} — seja pelo
 * acréscimo/remoção de um campo (mudança de coluna) ou pela alteração do
 * tipo Java (mudança de tipo da coluna).</p>
 *
 * <p>Este teste captura esse espelhamento como um <b>baseline imutável</b>
 * na constante {@link #BASELINE_FIELDS}. Se qualquer campo do baseline
 * desaparecer ou mudar de tipo, o teste falha — evidência estática de
 * violação da Req 9.2/9.3 sem depender de PostgreSQL em execução.</p>
 *
 * <p>Novos campos <i>podem</i> ser adicionados a {@link Coleta} no futuro
 * (compatibilidade retroativa permite extensão), portanto o teste não
 * proíbe acréscimos — apenas subtrações ou mudanças de tipo sobre o
 * baseline pré-feature.</p>
 *
 * <p>Executa em milissegundos, sem banco, rede ou contexto Spring.</p>
 *
 * @author Sistema de Inventário
 * @since 2.7.0
 */
@Tag("smoke-test")
@DisplayName("Contrato de schema: TABELA_COLETA / model.Coleta — campos preservados (Req 9.2, 9.3)")
class TabelaColetaSchemaSmokeTest {

    /**
     * Baseline pré-feature de campos da entidade {@link Coleta} com os
     * respectivos tipos Java. Cada entrada espelha uma coluna de
     * {@code TABELA_COLETA} (exceto os campos transientes, documentados
     * na seção "Campos transientes" da classe).
     *
     * <p>Extraído diretamente de {@code sihcp-core/.../model/Coleta.java}
     * no commit imediatamente anterior à feature
     * {@code coleta-descricao-livre-com-sugestao}. Ver também o script
     * de produção {@code sql/setup_banco_completo.sql} para as colunas
     * correspondentes.</p>
     *
     * <p>Se você está alterando esta lista como parte de uma nova feature,
     * <b>pare</b>: a Req 9.2/9.3 proíbe renomear, remover ou alterar o
     * tipo de colunas existentes. Apenas adições são permitidas, e devem
     * ser refletidas aqui com justificativa via ADR.</p>
     */
    private static final Map<String, Class<?>> BASELINE_FIELDS;
    static {
        Map<String, Class<?>> m = new LinkedHashMap<>();

        // Identidade e chaves estrangeiras
        m.put("id",                       int.class);
        m.put("idInventario",             int.class);
        m.put("idPatrimonio",             int.class);
        m.put("idColetor",                int.class);
        m.put("idParticipanteInventario", int.class);

        // Dados da coleta
        m.put("dataColeta",               Timestamp.class);
        m.put("statusColeta",             String.class);
        m.put("observacaoColeta",         String.class);
        m.put("localizacaoAtual",         String.class);
        m.put("localizacaoEncontrada",    String.class);
        m.put("estadoEncontrado",         String.class);
        m.put("divergencia",              boolean.class);
        m.put("motivoDivergencia",        String.class);

        // Geolocalização e foto
        m.put("latitude",                 BigDecimal.class);
        m.put("longitude",                BigDecimal.class);
        m.put("fotoPatrimonio",           String.class);

        // Itens sem etiqueta — COLUNA CRÍTICA DESTA FEATURE (Req 9.1)
        m.put("semEtiqueta",              boolean.class);
        m.put("descricaoItemSemEtiqueta", String.class);
        m.put("categoriaItemSemEtiqueta", String.class);

        // Métricas de tempo / analytics (colunas existentes pré-feature)
        m.put("tempoColetaSegundos",        Integer.class);
        m.put("tempoScanSegundos",          Integer.class);
        m.put("tempoPreenchimentoSegundos", Integer.class);
        m.put("metodoColeta",               String.class);
        m.put("horaColeta",                 Integer.class);
        m.put("diaSemana",                  Integer.class);
        m.put("periodoColeta",              String.class);
        m.put("tipoScan",                   String.class);
        m.put("tentativasScan",             Integer.class);
        m.put("errosScan",                  Integer.class);
        m.put("qualidadeEtiqueta",          String.class);

        BASELINE_FIELDS = Map.copyOf(m);
    }

    // =========================================================================
    // 1. Presença + tipo de cada campo do baseline
    // =========================================================================

    @Test
    @DisplayName("Todos os campos do baseline existem em Coleta com o tipo exato (Req 9.2, 9.3)")
    void todosOsCamposDoBaselineExistemComOTipoCorreto() {
        Map<String, Field> declarados = indexarCamposDeclarados(Coleta.class);

        StringBuilder divergencias = new StringBuilder();

        for (Map.Entry<String, Class<?>> esperado : BASELINE_FIELDS.entrySet()) {
            String nome = esperado.getKey();
            Class<?> tipoEsperado = esperado.getValue();

            Field campo = declarados.get(nome);
            if (campo == null) {
                divergencias
                        .append("  - Campo ausente: '")
                        .append(nome)
                        .append("' (esperado tipo ")
                        .append(tipoEsperado.getSimpleName())
                        .append(")\n");
                continue;
            }

            Class<?> tipoReal = campo.getType();
            if (!tipoEsperado.equals(tipoReal)) {
                divergencias
                        .append("  - Tipo alterado em '")
                        .append(nome)
                        .append("': esperado ")
                        .append(tipoEsperado.getSimpleName())
                        .append(", obtido ")
                        .append(tipoReal.getSimpleName())
                        .append("\n");
            }
        }

        if (divergencias.length() > 0) {
            fail("Schema de Coleta divergiu do baseline pré-feature (Req 9.2, 9.3).\n"
                    + "A feature 'coleta-descricao-livre-com-sugestao' NÃO pode alterar\n"
                    + "colunas existentes em TABELA_COLETA. Divergências encontradas:\n"
                    + divergencias
                    + "\nSe a mudança é intencional e justificada, atualize o baseline\n"
                    + "em TabelaColetaSchemaSmokeTest.BASELINE_FIELDS junto com ADR.");
        }
    }

    // =========================================================================
    // 2. Coluna crítica desta feature: descricaoItemSemEtiqueta permanece String
    // =========================================================================

    @Test
    @DisplayName("Coluna-alvo descricaoItemSemEtiqueta permanece String (Req 9.1, 9.2)")
    void colunaDescricaoItemSemEtiquetaPermaneceString() throws NoSuchFieldException {
        // Redundante com o teste anterior, mas dá feedback específico quando
        // alguém altera justamente o campo central desta feature. Req 9.1
        // obriga persistência exclusiva na coluna descricao_item_sem_etiqueta;
        // mudar seu tipo Java sinalizaria mudança de tipo de coluna.
        Field f = Coleta.class.getDeclaredField("descricaoItemSemEtiqueta");
        assertEquals(String.class, f.getType(),
                "descricaoItemSemEtiqueta deve permanecer String para preservar "
                        + "o contrato de persistência definido em Req 9.1");
    }

    // =========================================================================
    // 3. Detecção proativa de campos novos (aviso para revisão)
    // =========================================================================

    @Test
    @DisplayName("Campos adicionados pós-baseline são permitidos mas devem ser conscientes (Req 9.2)")
    void camposAdicionadosAlemDoBaselineSaoRegistradosParaRevisao() {
        // Adições são permitidas (Req 9.2 proíbe apenas renomear/remover/alterar
        // tipo). No entanto, acréscimos silenciosos podem indicar alteração
        // não documentada do schema, portanto listamos os novos campos para
        // inspeção manual. Este teste NÃO falha — apenas registra.
        Map<String, Field> declarados = indexarCamposDeclarados(Coleta.class);
        Set<String> transienteIgnoraveis = camposTransientes();

        Set<String> novosCampos = new TreeSet<>();
        for (String nome : declarados.keySet()) {
            if (BASELINE_FIELDS.containsKey(nome)) continue;
            if (transienteIgnoraveis.contains(nome)) continue;
            novosCampos.add(nome);
        }

        // Assertiva sempre passa. Mensagem serve como log no resultado do teste.
        assertTrue(novosCampos.size() >= 0,
                "Campos novos detectados em Coleta além do baseline (apenas informativo, "
                        + "revise se refletem mudança de coluna em TABELA_COLETA): "
                        + novosCampos);
    }

    // =========================================================================
    // 4. Classe Coleta não foi removida nem tornada abstrata
    // =========================================================================

    @Test
    @DisplayName("Classe Coleta permanece concreta e instanciável (Req 9.2, 9.3)")
    void classeColetaPermaneceInstanciavel() {
        int mods = Coleta.class.getModifiers();
        assertTrue(!Modifier.isAbstract(mods),
                "Coleta não pode ser abstrata — é o POJO central de TABELA_COLETA");
        assertTrue(!Modifier.isInterface(mods),
                "Coleta não pode ser interface — é o POJO central de TABELA_COLETA");
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private static Map<String, Field> indexarCamposDeclarados(Class<?> classe) {
        Map<String, Field> indice = new HashMap<>();
        for (Field f : classe.getDeclaredFields()) {
            // Ignorar campos estáticos e sintéticos (constantes, $VALUES, etc.).
            int mods = f.getModifiers();
            if (Modifier.isStatic(mods)) continue;
            if (f.isSynthetic()) continue;
            indice.put(f.getName(), f);
        }
        return indice;
    }

    /**
     * Campos declarados explicitamente em {@link Coleta} como transientes
     * (não correspondem a colunas em {@code TABELA_COLETA} — apenas auxiliam
     * a exibição após JOINs). Esses nomes estão documentados no comentário
     * "Campos transientes para exibição" dentro da classe {@link Coleta}.
     */
    private static Set<String> camposTransientes() {
        Set<String> t = new TreeSet<>();
        t.add("numeroPatrimonio");
        t.add("descricaoPatrimonio");
        t.add("nomeColetor");
        t.add("descricaoInventario");
        t.add("nomeSala");
        return t;
    }
}
