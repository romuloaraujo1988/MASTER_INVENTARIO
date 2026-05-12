package com.inventario.mobile.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.inventario.mobile.data.local.database.AppDatabase
import com.inventario.mobile.data.local.entity.SugestaoDescricaoEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

/**
 * Property test P16 — Consistência do cache + filtro offline com patrimônios coletados.
 *
 * **Validates: Requirements 8.1, 8.2, 8.3, 8.4**
 *
 * Feature: coleta-descricao-livre-com-sugestao
 *
 * Para qualquer estado do cache `C` para `idInventario = I` e termo `t`:
 *
 *  - O conjunto devolvido por `SugestaoDescricaoDao.buscarFiltrado(I, normalize(t), limit)`
 *    contém apenas descrições `d` tais que existe em `C` pelo menos uma tupla
 *    `(I, _, d, _, coletadoLocal=0, _)` com `normalize(d).contains(normalize(t))`.
 *  - Reciprocamente, se tal tupla existe em `C`, `d` aparece no resultado enquanto não for
 *    empurrado para fora pelos primeiros `limit` por ordem (`descricaoNormalizada ASC,
 *    numeroPatrimonio ASC`).
 *  - Marcar todos os `idPatrimonio` associados a `d` como `coletadoLocal=1` faz `d`
 *    desaparecer; enquanto pelo menos um `idPatrimonio` de descrição `d` permanecer livre,
 *    `d` continua visível (Req 8.4 + Req 8.1).
 *
 * Uso de teste instrumentado porque o `SugestaoDescricaoDao` depende do motor SQLite real
 * fornecido pelo Room. `Room.inMemoryDatabaseBuilder` é criado diretamente, sem o
 * `openHelperFactory` do SQLCipher, de modo que o banco em memória é puro SQLite — isso
 * não impacta a propriedade verificada, pois o comportamento das queries é o mesmo.
 */
@RunWith(AndroidJUnit4::class)
class SugestaoDescricaoDaoPropertyTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: SugestaoDescricaoDao

    @Before
    fun setup() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.sugestaoDescricaoDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private fun entity(
        idInv: Int,
        idPat: Int,
        numero: String = "IFMT-%05d".format(idPat),
        descricao: String = "Descricao $idPat",
        descricaoNormalizada: String = descricao.lowercase(),
        coletadoLocal: Boolean = false
    ) = SugestaoDescricaoEntity(
        idInventario = idInv,
        idPatrimonio = idPat,
        numeroPatrimonio = numero,
        descricao = descricao,
        descricaoNormalizada = descricaoNormalizada,
        coletadoLocal = coletadoLocal,
        dataAtualizacao = 0L
    )

    // ---------------------------------------------------------------------
    // Casos concretos exigidos pela propriedade P16
    // ---------------------------------------------------------------------

    @Test
    fun buscarFiltrado_filtra_itens_coletados() = runBlocking {
        dao.upsertAll(
            listOf(
                entity(idInv = 1, idPat = 1, descricao = "Mesa", descricaoNormalizada = "mesa", coletadoLocal = false),
                entity(idInv = 1, idPat = 2, descricao = "Cadeira", descricaoNormalizada = "cadeira", coletadoLocal = true)
            )
        )

        val result = dao.buscarFiltrado(idInventario = 1, termoNormalizado = "", limit = 50)

        assertEquals("só a entrada não coletada deve ser retornada", 1, result.size)
        assertEquals(1, result[0].idPatrimonio)
        assertEquals("Mesa", result[0].descricao)
    }

    @Test
    fun buscarFiltrado_isola_por_inventario() = runBlocking {
        dao.upsertAll(
            listOf(
                entity(idInv = 1, idPat = 1, descricao = "Mesa", descricaoNormalizada = "mesa"),
                entity(idInv = 1, idPat = 2, descricao = "Cadeira", descricaoNormalizada = "cadeira"),
                entity(idInv = 2, idPat = 3, descricao = "Armario", descricaoNormalizada = "armario"),
                entity(idInv = 2, idPat = 4, descricao = "Computador", descricaoNormalizada = "computador")
            )
        )

        val r1 = dao.buscarFiltrado(idInventario = 1, termoNormalizado = "", limit = 50)
        val r2 = dao.buscarFiltrado(idInventario = 2, termoNormalizado = "", limit = 50)

        assertEquals(2, r1.size)
        assertTrue("todas as entradas devem pertencer ao inventário 1", r1.all { it.idInventario == 1 })
        assertEquals(2, r2.size)
        assertTrue(r2.all { it.idInventario == 2 })
    }

    @Test
    fun marcarColetado_torna_entrada_invisivel_para_buscarFiltrado() = runBlocking {
        dao.upsertAll(
            listOf(
                entity(idInv = 1, idPat = 1, descricao = "Mesa", descricaoNormalizada = "mesa")
            )
        )

        val antes = dao.buscarFiltrado(idInventario = 1, termoNormalizado = "", limit = 50)
        assertEquals("deve aparecer antes de marcar coletado", 1, antes.size)

        dao.marcarColetado(idInventario = 1, idPatrimonio = 1)

        val depois = dao.buscarFiltrado(idInventario = 1, termoNormalizado = "", limit = 50)
        assertEquals("não deve mais aparecer após marcar coletado", 0, depois.size)
    }

    @Test
    fun limparOutrosInventarios_remove_apenas_outros_inventarios() = runBlocking {
        dao.upsertAll(
            listOf(
                entity(idInv = 1, idPat = 1),
                entity(idInv = 1, idPat = 2),
                entity(idInv = 1, idPat = 3),
                entity(idInv = 2, idPat = 4),
                entity(idInv = 2, idPat = 5)
            )
        )

        dao.limparOutrosInventarios(idInventarioAtivo = 1)

        assertEquals("3 entradas do inventário ativo devem permanecer", 3, dao.contarPorInventario(1))
        assertEquals("entradas de outro inventário devem ter sido removidas", 0, dao.contarPorInventario(2))
    }

    @Test
    fun contarPorInventario_conta_apenas_nao_coletados() = runBlocking {
        dao.upsertAll(
            listOf(
                entity(idInv = 1, idPat = 1, coletadoLocal = false),
                entity(idInv = 1, idPat = 2, coletadoLocal = false),
                entity(idInv = 1, idPat = 3, coletadoLocal = false),
                entity(idInv = 1, idPat = 4, coletadoLocal = true),
                entity(idInv = 1, idPat = 5, coletadoLocal = true)
            )
        )

        assertEquals(
            "contarPorInventario deve contar apenas coletadoLocal = 0",
            3,
            dao.contarPorInventario(1)
        )
    }

    @Test
    fun multiplos_patrimonios_com_mesma_descricao_sao_preservados() = runBlocking {
        dao.upsertAll(
            listOf(
                entity(idInv = 1, idPat = 1, descricao = "Cadeira", descricaoNormalizada = "cadeira"),
                entity(idInv = 1, idPat = 2, descricao = "Cadeira", descricaoNormalizada = "cadeira"),
                entity(idInv = 1, idPat = 3, descricao = "Cadeira", descricaoNormalizada = "cadeira")
            )
        )

        val result = dao.buscarFiltrado(idInventario = 1, termoNormalizado = "", limit = 50)

        assertEquals("chave composta (idInventario, idPatrimonio) preserva múltiplas linhas da mesma descrição", 3, result.size)
        assertEquals(setOf(1, 2, 3), result.map { it.idPatrimonio }.toSet())
        assertTrue("todas devem ter a mesma descrição normalizada", result.all { it.descricaoNormalizada == "cadeira" })
    }

    @Test
    fun filtro_acento_case_insensivel_via_descricao_normalizada() = runBlocking {
        dao.upsertAll(
            listOf(
                entity(idInv = 1, idPat = 1, descricao = "Ônibus", descricaoNormalizada = "onibus"),
                entity(idInv = 1, idPat = 2, descricao = "Cadeira", descricaoNormalizada = "cadeira")
            )
        )

        val result = dao.buscarFiltrado(idInventario = 1, termoNormalizado = "onibus", limit = 50)

        assertEquals(1, result.size)
        assertEquals("Ônibus", result[0].descricao)
    }

    /**
     * Invariante derivada direta da P16: marcar apenas UM dos patrimônios associados
     * a uma descrição duplicada não faz a descrição desaparecer; marcar TODOS faz.
     *
     * Validates Requirements 8.1 + 8.4 em conjunto.
     */
    @Test
    fun descricao_duplicada_visivel_enquanto_houver_um_patrimonio_livre() = runBlocking {
        dao.upsertAll(
            listOf(
                entity(idInv = 1, idPat = 1, descricao = "Cadeira", descricaoNormalizada = "cadeira"),
                entity(idInv = 1, idPat = 2, descricao = "Cadeira", descricaoNormalizada = "cadeira")
            )
        )

        dao.marcarColetado(idInventario = 1, idPatrimonio = 1)
        val aindaVisivel = dao.buscarFiltrado(idInventario = 1, termoNormalizado = "cadeira", limit = 50)
        assertEquals("descrição deve continuar visível enquanto ao menos um patrimônio estiver livre", 1, aindaVisivel.size)
        assertEquals(2, aindaVisivel[0].idPatrimonio)

        dao.marcarColetado(idInventario = 1, idPatrimonio = 2)
        val agoraSumiu = dao.buscarFiltrado(idInventario = 1, termoNormalizado = "cadeira", limit = 50)
        assertEquals("ao marcar todos os patrimônios, a descrição desaparece", 0, agoraSumiu.size)
    }

    // ---------------------------------------------------------------------
    // Verificação estilo "property-based" — 30 cenários sintéticos
    // exercitam o invariante P16 com caches aleatórios.
    // ---------------------------------------------------------------------

    /**
     * Executa `TRIALS` cenários deterministicamente a partir de seeds fixos.
     * Para cada cenário, gera um cache de até 40 entradas e verifica o
     * invariante completo de P16 sobre o resultado de `buscarFiltrado`.
     */
    @Test
    fun propriedade_P16_consistencia_filtro_offline_com_patrimonios_coletados() = runBlocking {
        val termos = listOf("", "ca", "mesa", "nao-existe-zzz")
        repeat(TRIALS) { seed ->
            val rng = Random(seed.toLong())
            val idInventario = 1

            // Gerar um pool fechado de descrições (para permitir duplicatas).
            val poolDescricoes = listOf(
                "Mesa" to "mesa",
                "Cadeira" to "cadeira",
                "Cadeira Giratória" to "cadeira giratoria",
                "Armário" to "armario",
                "Computador" to "computador",
                "Impressora" to "impressora",
                "Monitor" to "monitor"
            )
            val tamanho = 1 + rng.nextInt(40)
            val entradas = (1..tamanho).map { idPat ->
                val (desc, norm) = poolDescricoes[rng.nextInt(poolDescricoes.size)]
                entity(
                    idInv = idInventario,
                    idPat = idPat,
                    descricao = desc,
                    descricaoNormalizada = norm,
                    coletadoLocal = rng.nextBoolean()
                )
            }
            // Ruído de outro inventário (não deve vazar para o resultado).
            val ruido = (1..5).map { i ->
                entity(idInv = 999, idPat = 10_000 + i, descricao = "Mesa", descricaoNormalizada = "mesa")
            }

            // Limpar o estado entre trials usando limparOutrosInventarios + marcar todos como coletados
            // de trials anteriores. Mais simples: deletar tudo via limpar para um id ativo "impossível".
            dao.limparOutrosInventarios(idInventarioAtivo = -1)

            dao.upsertAll(entradas + ruido)

            for (termo in termos) {
                val limit = 10 + rng.nextInt(41) // 10..50
                val resultado = dao.buscarFiltrado(
                    idInventario = idInventario,
                    termoNormalizado = termo,
                    limit = limit
                )

                // 1. Nenhum item do ruído (idInventario != 1) aparece.
                assertTrue(
                    "nenhuma entrada de outro inventário pode vazar (seed=$seed, termo=$termo)",
                    resultado.all { it.idInventario == idInventario }
                )

                // 2. Nenhum item coletado aparece.
                assertTrue(
                    "nenhuma entrada coletadoLocal=1 pode aparecer (seed=$seed, termo=$termo)",
                    resultado.all { !it.coletadoLocal }
                )

                // 3. Todo item do resultado casa com o termo (LIKE '%termo%' em descricaoNormalizada).
                assertTrue(
                    "toda linha retornada deve conter o termo normalizado na descricaoNormalizada (seed=$seed, termo=$termo)",
                    resultado.all { it.descricaoNormalizada.contains(termo) }
                )

                // 4. Ordenação estável: descricaoNormalizada ASC, numeroPatrimonio ASC.
                for (i in 1 until resultado.size) {
                    val prev = resultado[i - 1]
                    val cur = resultado[i]
                    val cmpDesc = prev.descricaoNormalizada.compareTo(cur.descricaoNormalizada)
                    assertTrue(
                        "resultado fora de ordem na posição $i (seed=$seed, termo=$termo)",
                        cmpDesc < 0 || (cmpDesc == 0 && prev.numeroPatrimonio <= cur.numeroPatrimonio)
                    )
                }

                // 5. Tamanho respeita o limit.
                assertTrue(
                    "resultado não pode exceder limit=$limit (seed=$seed, termo=$termo)",
                    resultado.size <= limit
                )

                // 6. Reciprocidade (dentro do limit): para todo item elegível no cache que
                //    aparece nos primeiros `limit` pela ordenação, espera-se que esteja
                //    presente no resultado. Calculamos o conjunto elegível e comparamos.
                val elegivel = entradas
                    .filter { !it.coletadoLocal && it.descricaoNormalizada.contains(termo) }
                    .sortedWith(
                        compareBy({ it.descricaoNormalizada }, { it.numeroPatrimonio })
                    )
                    .take(limit)
                val idsEsperados = elegivel.map { it.idPatrimonio }.toSet()
                val idsObtidos = resultado.map { it.idPatrimonio }.toSet()
                assertEquals(
                    "reciprocidade falhou (seed=$seed, termo=$termo)",
                    idsEsperados,
                    idsObtidos
                )
            }
        }
    }

    private companion object {
        private const val TRIALS = 30
    }
}
