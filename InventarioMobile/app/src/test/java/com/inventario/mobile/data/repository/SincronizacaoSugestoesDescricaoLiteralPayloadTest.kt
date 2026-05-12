package com.inventario.mobile.data.repository

import com.inventario.mobile.data.local.entity.SugestaoDescricaoEntity
import com.inventario.mobile.data.mapper.SugestaoDescricaoMapper
import com.inventario.mobile.data.remote.dto.SugestaoDescricaoDto
import com.inventario.mobile.data.util.TextNormalizer
import com.inventario.mobile.domain.model.SugestaoDescricao
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Property-Based Tests para preservação ipsis litteris da descrição ao longo
 * da camada de sincronização de sugestões.
 *
 * Feature: coleta-descricao-livre-com-sugestao (task 18.4)
 *
 * **Property 17: Sincronização preserva a descrição ipsis litteris**
 *
 * *For any* descrição `d` capturada pelo coletor, o texto persistido
 * localmente e o texto trafegado entre as representações (DTO ↔ domínio ↔
 * entity) permanece byte-a-byte idêntico à descrição original — nenhuma
 * transformação de caso, trim, remoção de diacríticos ou normalização Unicode
 * é aplicada ao campo `descricao` além do `trim()` explícito já realizado
 * pelo ViewModel antes da chamada ao UseCase.
 *
 * **Validates: Requirements 7.6**
 *
 * Escopo e estratégia do teste:
 *  - A invariante de P17 é composta por dois elos:
 *    1. VM → UseCase: `ItemSemEtiquetaViewModel.confirmarColeta` chama
 *       `RegistrarColetaUseCase(descricao = d.trim(), ...)` sem transformação
 *       adicional. Isso é coberto pelos property tests P2/P3 definidos em
 *       tasks 15.8/15.9 (`ItemSemEtiquetaViewModelConfirmacaoTest`).
 *    2. **Camada de dados (este teste)**: as conversões do
 *       [SugestaoDescricaoMapper] entre DTO (payload do servidor), modelo de
 *       domínio e entity Room (cache offline) preservam o campo `descricao`
 *       inalterado — nem mesmo a normalização derivada
 *       [SugestaoDescricaoEntity.descricaoNormalizada] afeta o campo original.
 *
 *  - Juntos, os dois elos formam a cadeia completa de P17: o texto digitado
 *    no `EditText` (após `trim()` determinístico) chega ao cache local e ao
 *    payload de sync byte-a-byte idêntico à descrição final validada no VM.
 *
 *  - Este teste não sobe Room nem Retrofit: exercita diretamente o
 *    [SugestaoDescricaoMapper] com strings arbitrárias, o que é suficiente
 *    para verificar a invariante de preservação sem custo de I/O.
 *
 *  - Strings arbitrárias cobrem diacríticos, caixa mista, espaços internos,
 *    dígitos, pontuação e caracteres Unicode arbitrários — o espaço exato
 *    que pode ser capturado pelo `TextInputEditText` em produção.
 */
class SincronizacaoSugestoesDescricaoLiteralPayloadTest : StringSpec({

    "P17: toEntity preserva descricao ipsis litteris (não normaliza o campo descricao)" {
        val mapper = SugestaoDescricaoMapper()
        val normalizer = TextNormalizer()

        // Descrições arbitrárias entre 1 e 200 caracteres — espaço amplo o
        // bastante para exercitar diacríticos, caixa mista e pontuação sem
        // ultrapassar o teto de 255 do `maxLength` no layout.
        checkAll(Arb.string(1..200)) { descricao ->
            val domain = SugestaoDescricao(
                idPatrimonio = 1,
                numeroPatrimonio = "A-001",
                descricao = descricao
            )

            val entity = mapper.toEntity(
                domain = domain,
                idInventario = 1,
                normalizer = normalizer
            )

            // Invariante central de P17 na camada de dados: o campo `descricao`
            // da entity Room é byte-idêntico ao da origem de domínio. A
            // normalização acento/caso-insensível vive em um campo derivado
            // (`descricaoNormalizada`) e NÃO afeta o original.
            entity.descricao shouldBe descricao
        }
    }

    "P17: toDomain(entity) reverte para a descrição original preservada" {
        val mapper = SugestaoDescricaoMapper()

        checkAll(Arb.string(1..200)) { descricao ->
            // `descricaoNormalizada` é campo de persistência — não participa
            // da roundtrip para domínio. Usamos um valor qualquer aqui, pois
            // o contrato do mapper só lê `descricao` ao montar o domínio.
            val entity = SugestaoDescricaoEntity(
                idInventario = 1,
                idPatrimonio = 1,
                numeroPatrimonio = "A-001",
                descricao = descricao,
                descricaoNormalizada = "irrelevante para roundtrip de domínio",
                coletadoLocal = false,
                dataAtualizacao = 0L
            )

            val domain = mapper.toDomain(entity)

            // A leitura do cache offline reconstrói a descrição original
            // byte-a-byte — sem mutação na volta da persistência.
            domain.descricao shouldBe descricao
        }
    }

    "P17: toDomain(dto) preserva descricao do servidor ipsis litteris" {
        val mapper = SugestaoDescricaoMapper()

        checkAll(Arb.string(1..200)) { descricao ->
            val dto = SugestaoDescricaoDto(
                idPatrimonio = 1,
                numeroPatrimonio = "A-001",
                descricao = descricao
            )

            val domain = mapper.toDomain(dto)

            // O payload recebido do servidor chega ao domínio inalterado — o
            // mapper não aplica trim/normalização ao descer do DTO para o
            // domínio; quaisquer transformações de apresentação ficam a cargo
            // de camadas superiores.
            domain.descricao shouldBe descricao
        }
    }

    "P17: roundtrip completo DTO → domínio → entity → domínio preserva descricao" {
        val mapper = SugestaoDescricaoMapper()
        val normalizer = TextNormalizer()

        checkAll(Arb.string(1..200)) { descricao ->
            // Simula o caminho real de uma sugestão recém-sincronizada:
            //   servidor → DTO → domínio → upsert no Room → leitura do cache → domínio
            val dto = SugestaoDescricaoDto(
                idPatrimonio = 1,
                numeroPatrimonio = "A-001",
                descricao = descricao
            )

            val domainFromDto = mapper.toDomain(dto)
            val entity = mapper.toEntity(
                domain = domainFromDto,
                idInventario = 1,
                normalizer = normalizer
            )
            val domainFromCache = mapper.toDomain(entity)

            // Em qualquer ponto intermediário, a descrição é a mesma da entrada
            // original — não há perda ao ida-e-volta pelo cache offline.
            domainFromDto.descricao shouldBe descricao
            entity.descricao shouldBe descricao
            domainFromCache.descricao shouldBe descricao
        }
    }
})
