package com.inventario.mobile.data.repository

import com.inventario.mobile.data.local.dao.SugestaoDescricaoDao
import com.inventario.mobile.data.local.entity.SugestaoDescricaoEntity
import com.inventario.mobile.data.mapper.SugestaoDescricaoMapper
import com.inventario.mobile.data.remote.api.DescricaoSugestaoApi
import com.inventario.mobile.data.util.TextNormalizer
import com.inventario.mobile.domain.model.SugestaoDescricao
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.stringPattern
import io.kotest.property.checkAll
import kotlinx.coroutines.runBlocking

/**
 * Feature: coleta-descricao-livre-com-sugestao
 *
 * **Property 15: `atualizarCache` garante consistência em sucesso e preserva em falha**
 *
 * **Validates: Requirements 7.1, 8.7**
 *
 *  - Caso sucesso: após `atualizarCache(I, L)`, o cache contém exatamente `L`
 *    mapeado com `descricaoNormalizada` correta (Req 7.1).
 *  - Caso falha (DAO lança exceção): o cache anterior é preservado — o método
 *    NÃO propaga a exceção, apenas loga e retorna (Req 8.7).
 *
 * Estratégia do teste:
 *  - `DescricaoSugestaoApi` e `SugestaoDescricaoDao` são mockados via `mockk` —
 *    o foco é validar o contrato de [SugestaoDescricaoRepositoryImpl.atualizarCache]
 *    em isolamento, sem subir Room nem Retrofit.
 *  - `SugestaoDescricaoMapper` e `TextNormalizer` são instâncias reais para que a
 *    propriedade "descricaoNormalizada correta" seja verificada ponta a ponta.
 */
class SugestaoDescricaoRepositoryImplAtualizarCacheTest : StringSpec({

    "atualizarCache em sucesso persiste todas as sugestões com descricaoNormalizada correta" {
        val api = mockk<DescricaoSugestaoApi>()
        val dao = mockk<SugestaoDescricaoDao>()
        val mapper = SugestaoDescricaoMapper()
        val normalizer = TextNormalizer()

        val capturedEntities = slot<List<SugestaoDescricaoEntity>>()
        coEvery { dao.upsertAll(capture(capturedEntities)) } just Runs

        val repo = SugestaoDescricaoRepositoryImpl(dao, api, mapper, normalizer)
        val sugestoes = listOf(
            SugestaoDescricao(idPatrimonio = 10, numeroPatrimonio = "A-001", descricao = "Cadeira Giratória"),
            SugestaoDescricao(idPatrimonio = 11, numeroPatrimonio = "A-002", descricao = "ÔNIBUS Escolar")
        )

        runBlocking { repo.atualizarCache(idInventario = 1, sugestoes = sugestoes) }

        coVerify(exactly = 1) { dao.upsertAll(any()) }
        val entities = capturedEntities.captured
        entities shouldHaveSize 2
        entities[0].idInventario shouldBe 1
        entities[0].idPatrimonio shouldBe 10
        entities[0].numeroPatrimonio shouldBe "A-001"
        entities[0].descricao shouldBe "Cadeira Giratória"
        entities[0].descricaoNormalizada shouldBe "cadeira giratoria"
        entities[1].idPatrimonio shouldBe 11
        entities[1].descricao shouldBe "ÔNIBUS Escolar"
        entities[1].descricaoNormalizada shouldBe "onibus escolar"
    }

    "atualizarCache com lista vazia não invoca DAO" {
        val api = mockk<DescricaoSugestaoApi>()
        val dao = mockk<SugestaoDescricaoDao>(relaxed = true)
        val repo = SugestaoDescricaoRepositoryImpl(
            dao, api, SugestaoDescricaoMapper(), TextNormalizer()
        )

        runBlocking { repo.atualizarCache(idInventario = 1, sugestoes = emptyList()) }

        coVerify(exactly = 0) { dao.upsertAll(any()) }
    }

    "atualizarCache NÃO propaga exceção do DAO (Req 8.7)" {
        val api = mockk<DescricaoSugestaoApi>()
        val dao = mockk<SugestaoDescricaoDao>()
        coEvery { dao.upsertAll(any()) } throws RuntimeException("db corrupted")

        val repo = SugestaoDescricaoRepositoryImpl(
            dao, api, SugestaoDescricaoMapper(), TextNormalizer()
        )

        // Falhas de escrita devem ser engolidas — o cache anterior (ausente ou
        // populado) é preservado exatamente por NÃO propagar a exceção.
        shouldNotThrow<Exception> {
            runBlocking {
                repo.atualizarCache(
                    idInventario = 1,
                    sugestoes = listOf(SugestaoDescricao(10, "A-001", "Teste"))
                )
            }
        }
    }

    "atualizarCache isola todas as entradas pelo idInventario informado" {
        val api = mockk<DescricaoSugestaoApi>()
        val dao = mockk<SugestaoDescricaoDao>()
        val captured = slot<List<SugestaoDescricaoEntity>>()
        coEvery { dao.upsertAll(capture(captured)) } just Runs

        val repo = SugestaoDescricaoRepositoryImpl(
            dao, api, SugestaoDescricaoMapper(), TextNormalizer()
        )
        runBlocking {
            repo.atualizarCache(
                idInventario = 42,
                sugestoes = listOf(
                    SugestaoDescricao(1, "X-001", "Item Um"),
                    SugestaoDescricao(2, "X-002", "Item Dois"),
                    SugestaoDescricao(3, "X-003", "Item Três")
                )
            )
        }

        captured.captured.forEach { it.idInventario shouldBe 42 }
    }

    "property: atualizarCache com sugestões aleatórias preserva todos os campos de domínio" {
        checkAll(iterations = 50, Arb.list(sugestaoArb, 0..20)) { sugestoes ->
            val api = mockk<DescricaoSugestaoApi>()
            val dao = mockk<SugestaoDescricaoDao>()
            val captured = slot<List<SugestaoDescricaoEntity>>()
            coEvery { dao.upsertAll(capture(captured)) } just Runs

            val normalizer = TextNormalizer()
            val repo = SugestaoDescricaoRepositoryImpl(
                dao, api, SugestaoDescricaoMapper(), normalizer
            )

            runBlocking { repo.atualizarCache(idInventario = 7, sugestoes = sugestoes) }

            if (sugestoes.isEmpty()) {
                coVerify(exactly = 0) { dao.upsertAll(any()) }
            } else {
                val entities = captured.captured
                entities shouldHaveSize sugestoes.size
                entities.zip(sugestoes).forEach { (entity, domain) ->
                    entity.idInventario shouldBe 7
                    entity.idPatrimonio shouldBe domain.idPatrimonio
                    entity.numeroPatrimonio shouldBe domain.numeroPatrimonio
                    entity.descricao shouldBe domain.descricao
                    // descricaoNormalizada deve ser idêntica à aplicação direta do
                    // normalizer sobre a descrição de origem (Req 7.1, 7.2).
                    entity.descricaoNormalizada shouldBe normalizer.normalize(domain.descricao)
                }
            }
        }
    }
})

/**
 * Arbitrary para [SugestaoDescricao] — gera trios (idPatrimonio, numeroPatrimonio,
 * descricao) plausíveis: IDs positivos, números formatados tipo "A-001" e descrições
 * arbitrárias de 1 a 40 caracteres. Suficiente para exercitar a propriedade sem
 * custo excessivo de execução.
 */
private val sugestaoArb = arbitrary { rs ->
    SugestaoDescricao(
        idPatrimonio = Arb.int(1..10_000).next(rs),
        numeroPatrimonio = Arb.stringPattern("[A-Z]-[0-9]{3}").next(rs),
        descricao = Arb.string(1..40).next(rs)
    )
}
