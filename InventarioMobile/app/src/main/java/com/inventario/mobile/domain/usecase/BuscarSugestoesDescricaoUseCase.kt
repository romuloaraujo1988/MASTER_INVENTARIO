package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.model.ResultadoSugestoes
import com.inventario.mobile.domain.repository.SugestaoDescricaoRepository
import com.inventario.mobile.network.ConnectivityMonitor
import javax.inject.Inject

/**
 * Use Case: Buscar sugestões de descrição para a tela de coleta de item
 * sem etiqueta do App_Android.
 *
 * Delega integralmente ao [SugestaoDescricaoRepository], cuja implementação
 * é responsável pela estratégia offline-first (tentativa online com fallback
 * para o cache local em caso de falha de rede/timeout/HTTP). A decisão sobre
 * consultar servidor ou cache NÃO é tomada aqui — está no repositório,
 * conforme `design.md` (seção Architecture) e Requirement 7.2.
 *
 * O [ConnectivityMonitor] é injetado de forma defensiva para permitir
 * evoluções futuras (por exemplo, decidir pular a chamada ao servidor
 * quando o estado de conectividade já indica ausência de internet),
 * mas não é consultado diretamente na invocação atual — a decisão de
 * fallback permanece no repositório, mantendo a assinatura deste caso
 * de uso estável.
 *
 * Caso de uso pertencente à camada de domínio conforme Clean Architecture
 * (steering rule `clean-architecture.md`): não depende de Android ou
 * frameworks de infraestrutura além do `@Inject` do Hilt e dos contratos
 * de domínio.
 *
 * Requirements: 3.1, 3.2, 7.2
 */
class BuscarSugestoesDescricaoUseCase @Inject constructor(
    private val repository: SugestaoDescricaoRepository,
    @Suppress("unused") private val connectivityMonitor: ConnectivityMonitor
) {

    /**
     * Executa a busca de sugestões para o inventário informado.
     *
     * @param idInventario Identificador do inventário ativo cujo conjunto
     *                     de sugestões deve ser consultado (Requirement 7.1).
     * @param termoBusca Texto livre corrente do campo de descrição usado
     *                   para filtrar as sugestões. Correspondência parcial
     *                   case-insensitive e insensível a acentuação
     *                   (Requirement 3.4). String vazia significa
     *                   "sem filtro".
     * @param page Índice da página (baseado em zero) a ser carregada do
     *             servidor. Padrão `0`. Usado apenas no modo online
     *             (Requirement 3.1).
     * @param size Tamanho máximo da página solicitada ao servidor. Padrão
     *             `50`, conforme definição do contrato do endpoint
     *             (Requirement 5.6).
     *
     * @return [Result.success] contendo um [ResultadoSugestoes] com a
     *         origem dos dados (servidor, cache ou cache vazio). A
     *         implementação do repositório não retorna [Result.failure]
     *         em falhas de rede — a degradação é sinalizada via
     *         [ResultadoSugestoes.origem], preservando o fluxo
     *         offline-first (Requirements 3.2, 7.2, 7.3).
     */
    suspend operator fun invoke(
        idInventario: Int,
        termoBusca: String,
        page: Int = 0,
        size: Int = 50
    ): Result<ResultadoSugestoes> {
        return repository.buscarSugestoes(idInventario, termoBusca, page, size)
    }
}
