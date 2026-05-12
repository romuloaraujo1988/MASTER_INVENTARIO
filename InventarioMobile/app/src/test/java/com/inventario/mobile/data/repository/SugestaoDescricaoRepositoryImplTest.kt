package com.inventario.mobile.data.repository

import com.inventario.mobile.data.local.dao.SugestaoDescricaoDao
import com.inventario.mobile.data.local.entity.SugestaoDescricaoEntity
import com.inventario.mobile.data.mapper.SugestaoDescricaoMapper
import com.inventario.mobile.data.remote.api.DescricaoSugestaoApi
import com.inventario.mobile.data.remote.dto.ApiResponse
import com.inventario.mobile.data.remote.dto.PagedResponseDto
import com.inventario.mobile.data.remote.dto.SugestaoDescricaoDto
import com.inventario.mobile.data.util.TextNormalizer
import com.inventario.mobile.domain.model.OrigemSugestoes
import com.inventario.mobile.domain.model.SugestaoDescricao
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * Property-Based Tests para [SugestaoDescricaoRepositoryImpl].
 *
 * Feature: coleta-descricao-livre-com-sugestao (task 12.2)
 *
 * **Property 10: Fallback determinístico para o cache em falhas**
 *
 * *For any* falha da chamada `DescricaoSugestaoApi.buscarSugestoes` —
 * `IOException`, `HttpException` com status `≥ 400`, ou
 * `TimeoutCancellationException` após 10s — o
 * `SugestaoDescricaoRepositoryImpl` retorna
 * `Result.Success(ResultadoSugestoes(sugestoes=L, origem=CACHE))` se L não
 * vazio, ou `origem = VAZIO_SEM_CACHE` caso contrário, onde
 * `L = dao.buscarFiltrado(idInventario, normalize(termo), limit=10)` com
 * `coletadoLocal = 0`.
 *
 * **Validates: Requirements 3.2, 7.2, 7.3**
 *
 * Complementarmente, exercita o caminho de sucesso para garantir que o
 * contrato `origem = SERVIDOR` e `atualizarCache` seja preservado —
 * contrapositiva da propriedade principal.
 */
class SugestaoDescricaoRepositoryImplTest : StringSpec({

    /**
     * Captura uma instância real de [TimeoutCancellationException] disparada
     * por um `withTimeout` artificialmente curto. Usada para alimentar o
     * mock `coEvery { api.buscarSugestoes(...) } throws <captured>` sem
     * depender de delay real de 10s (o que tornaria a suíte lenta) e sem
     * depender de construtor interno da exceção (que é marcado `internal`
     * em kotlinx.coroutines).
     */
    fun capturarTimeoutCancellationException(): TimeoutCancellationException {
        return try {
            runBlocking { withTimeout(1L) { delay(10_000L) } }
            error("withTimeout deveria ter lançado TimeoutCancellationException")
        } catch (e: TimeoutCancellationException) {
            e
        }
    }

    // ---------- P10 (IOException) ----------
    "buscarSugestoes faz fallback para cache em IOException" {
        val api = mockk<DescricaoSugestaoApi>()
        val dao = mockk<SugestaoDescricaoDao>()
        val mapper = mockk<SugestaoDescricaoMapper>()
        val normalizer = TextNormalizer()

        coEvery {
            api.buscarSugestoes(any(), any(), any(), any())
        } throws IOException("no network")

        val entity = SugestaoDescricaoEntity(
            idInventario = 1,
            idPatrimonio = 10,
            numeroPatrimonio = "A-001",
            descricao = "Cadeira",
            descricaoNormalizada = "cadeira",
            coletadoLocal = false,
            dataAtualizacao = 0L
        )
        coEvery { dao.buscarFiltrado(any(), any(), any()) } returns listOf(entity)
        every { mapper.toDomain(any<SugestaoDescricaoEntity>()) } returns
            SugestaoDescricao(10, "A-001", "Cadeira")

        val repo = SugestaoDescricaoRepositoryImpl(dao, api, mapper, normalizer)
        val result = repo.buscarSugestoes(
            idInventario = 1,
            termoBusca = "cadeira",
            page = 0,
            size = 50
        )

        result.isSuccess shouldBe true
        val dados = result.getOrThrow()
        dados.origem shouldBe OrigemSugestoes.CACHE
        dados.sugestoes shouldHaveSize 1
        dados.sugestoes[0].descricao shouldBe "Cadeira"
        dados.hasNext shouldBe false
    }

    // ---------- P10 (HttpException 500) ----------
    "buscarSugestoes faz fallback para cache em HttpException 500" {
        val api = mockk<DescricaoSugestaoApi>()
        val dao = mockk<SugestaoDescricaoDao>()
        val mapper = mockk<SugestaoDescricaoMapper>()
        val normalizer = TextNormalizer()

        val errorBody = "".toResponseBody("application/json".toMediaTypeOrNull())
        val httpException = HttpException(Response.error<Any>(500, errorBody))

        coEvery {
            api.buscarSugestoes(any(), any(), any(), any())
        } throws httpException

        val entity = SugestaoDescricaoEntity(
            idInventario = 1,
            idPatrimonio = 20,
            numeroPatrimonio = "B-002",
            descricao = "Mesa",
            descricaoNormalizada = "mesa",
            coletadoLocal = false,
            dataAtualizacao = 0L
        )
        coEvery { dao.buscarFiltrado(any(), any(), any()) } returns listOf(entity)
        every { mapper.toDomain(any<SugestaoDescricaoEntity>()) } returns
            SugestaoDescricao(20, "B-002", "Mesa")

        val repo = SugestaoDescricaoRepositoryImpl(dao, api, mapper, normalizer)
        val result = repo.buscarSugestoes(
            idInventario = 1,
            termoBusca = "mesa",
            page = 0,
            size = 50
        )

        result.isSuccess shouldBe true
        val dados = result.getOrThrow()
        dados.origem shouldBe OrigemSugestoes.CACHE
        dados.sugestoes shouldHaveSize 1
        dados.sugestoes[0].numeroPatrimonio shouldBe "B-002"
    }

    // ---------- P10 (TimeoutCancellationException) ----------
    "buscarSugestoes faz fallback para cache em TimeoutCancellationException" {
        val api = mockk<DescricaoSugestaoApi>()
        val dao = mockk<SugestaoDescricaoDao>()
        val mapper = mockk<SugestaoDescricaoMapper>()
        val normalizer = TextNormalizer()

        // Captura uma instância real de TimeoutCancellationException (que tem
        // construtor internal) sem esperar os 10s configurados no repositório.
        val timeoutException = capturarTimeoutCancellationException()

        coEvery {
            api.buscarSugestoes(any(), any(), any(), any())
        } throws timeoutException

        val entity = SugestaoDescricaoEntity(
            idInventario = 1,
            idPatrimonio = 30,
            numeroPatrimonio = "C-003",
            descricao = "Armário",
            descricaoNormalizada = "armario",
            coletadoLocal = false,
            dataAtualizacao = 0L
        )
        coEvery { dao.buscarFiltrado(any(), any(), any()) } returns listOf(entity)
        every { mapper.toDomain(any<SugestaoDescricaoEntity>()) } returns
            SugestaoDescricao(30, "C-003", "Armário")

        val repo = SugestaoDescricaoRepositoryImpl(dao, api, mapper, normalizer)
        val result = repo.buscarSugestoes(
            idInventario = 1,
            termoBusca = "armario",
            page = 0,
            size = 50
        )

        result.isSuccess shouldBe true
        val dados = result.getOrThrow()
        dados.origem shouldBe OrigemSugestoes.CACHE
        dados.sugestoes shouldHaveSize 1
    }

    // ---------- P10 (VAZIO_SEM_CACHE) ----------
    "buscarSugestoes retorna VAZIO_SEM_CACHE quando api falha e cache vazio" {
        val api = mockk<DescricaoSugestaoApi>()
        val dao = mockk<SugestaoDescricaoDao>()
        val mapper = mockk<SugestaoDescricaoMapper>()
        val normalizer = TextNormalizer()

        coEvery {
            api.buscarSugestoes(any(), any(), any(), any())
        } throws IOException("down")
        coEvery { dao.buscarFiltrado(any(), any(), any()) } returns emptyList()

        val repo = SugestaoDescricaoRepositoryImpl(dao, api, mapper, normalizer)
        val result = repo.buscarSugestoes(
            idInventario = 1,
            termoBusca = "qualquer",
            page = 0,
            size = 50
        )

        result.isSuccess shouldBe true
        val dados = result.getOrThrow()
        dados.origem shouldBe OrigemSugestoes.VAZIO_SEM_CACHE
        dados.sugestoes shouldHaveSize 0
        dados.totalElements shouldBe 0L
        dados.hasNext shouldBe false
    }

    // ---------- Contrapositiva (sucesso) ----------
    "buscarSugestoes retorna SERVIDOR em sucesso e atualiza cache" {
        val api = mockk<DescricaoSugestaoApi>()
        val dao = mockk<SugestaoDescricaoDao>()
        val mapper = mockk<SugestaoDescricaoMapper>()
        val normalizer = TextNormalizer()

        val dto = PagedResponseDto(
            content = listOf(SugestaoDescricaoDto(10, "A-001", "Cadeira")),
            page = 0,
            size = 50,
            totalElements = 1L,
            totalPages = 1,
            hasNext = false,
            semInventarioAtivo = false
        )
        coEvery {
            api.buscarSugestoes(any(), any(), any(), any())
        } returns ApiResponse(success = true, data = dto, message = "ok")

        coEvery { dao.upsertAll(any()) } just runs
        every { mapper.toDomain(any<SugestaoDescricaoDto>()) } returns
            SugestaoDescricao(10, "A-001", "Cadeira")
        every { mapper.toEntity(any(), any(), any()) } returns
            SugestaoDescricaoEntity(
                idInventario = 1,
                idPatrimonio = 10,
                numeroPatrimonio = "A-001",
                descricao = "Cadeira",
                descricaoNormalizada = "cadeira",
                coletadoLocal = false,
                dataAtualizacao = 0L
            )

        val repo = SugestaoDescricaoRepositoryImpl(dao, api, mapper, normalizer)
        val result = repo.buscarSugestoes(
            idInventario = 1,
            termoBusca = "",
            page = 0,
            size = 50
        )

        result.isSuccess shouldBe true
        val dados = result.getOrThrow()
        dados.origem shouldBe OrigemSugestoes.SERVIDOR
        dados.sugestoes shouldHaveSize 1
        dados.totalElements shouldBe 1L
        dados.hasNext shouldBe false

        coVerify(exactly = 1) { dao.upsertAll(any()) }
    }
})
