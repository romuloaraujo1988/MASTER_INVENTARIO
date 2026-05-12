package com.inventario.mobile.domain.repository

import com.inventario.mobile.domain.model.ResultadoSugestoes
import com.inventario.mobile.domain.model.SugestaoDescricao

/**
 * Contrato do repositório de sugestões de descrição para a tela de coleta
 * de item sem etiqueta do App_Android.
 *
 * Este repositório coordena a obtenção de sugestões a partir do
 * [Endpoint_Sugestoes] e do [Cache_Sugestoes_Local], aplicando a estratégia
 * offline-first descrita em `design.md`:
 *
 * - Em cenário online, consulta o servidor e atualiza o cache local.
 * - Em cenário offline (ou falha de rede/timeout/HTTP), faz fallback para
 *   o cache local e sinaliza a origem dos dados via [ResultadoSugestoes.origem].
 *
 * Interface pura da camada de domínio, conforme Clean Architecture
 * (steering rule `clean-architecture.md`). Não depende de Android,
 * Room, Retrofit ou qualquer framework de infraestrutura — apenas de
 * Kotlin puro e dos modelos de domínio [SugestaoDescricao] e
 * [ResultadoSugestoes]. A implementação concreta reside na camada de dados.
 *
 * Todas as operações são `suspend` porque podem envolver I/O (rede ou
 * banco local) e devem ser executadas em um dispatcher apropriado pela
 * camada de dados (tipicamente `Dispatchers.IO`).
 *
 * Isolamento por inventário: o cache é particionado por `idInventario`
 * (chave composta na entidade Room), de forma que sugestões de um
 * inventário não contaminem as de outro (Requirement 7.1).
 */
interface SugestaoDescricaoRepository {

    /**
     * Busca sugestões de descrição para o inventário informado, aplicando
     * a estratégia offline-first: tenta o servidor primeiro e, em caso de
     * falha (exceção de rede, `HttpException` ou `TimeoutCancellationException`
     * com timeout de 10 segundos), faz fallback para o cache local.
     *
     * Em caso de sucesso na chamada ao servidor, a implementação deve
     * atualizar o [Cache_Sugestoes_Local] via [atualizarCache] antes de
     * retornar (Requirement 7.1).
     *
     * O [Result] retornado encapsula apenas [ResultadoSugestoes.origem] como
     * sinalização explícita da fonte dos dados; a camada de apresentação
     * deve usar essa informação para exibir feedback contextual ao coletor
     * (por exemplo, aviso de uso de cache — Requirement 3.2).
     *
     * @param idInventario Identificador do [Inventario_Ativo] cujo conjunto
     *                     de sugestões deve ser consultado. Usado tanto na
     *                     requisição HTTP quanto na consulta ao cache local
     *                     (Requirement 7.1).
     * @param termoBusca Texto livre corrente do [Campo_Descricao_Livre] usado
     *                   para filtrar as sugestões. Correspondência parcial
     *                   case-insensitive e insensível a acentuação aplicada
     *                   pela implementação (Requirement 3.4). String vazia
     *                   significa "sem filtro".
     * @param page Índice da página (baseado em zero) a ser carregada do
     *             servidor. Usado apenas no modo online; o fallback offline
     *             não é paginado (Requirement 3.1).
     * @param size Tamanho máximo da página solicitada ao servidor (entre
     *             1 e 100 inclusive; valores fora do intervalo são
     *             normalizados pelo servidor — Requirement 5.6).
     *
     * @return [Result.success] contendo um [ResultadoSugestoes] com a
     *         origem dos dados ([OrigemSugestoes.SERVIDOR],
     *         [OrigemSugestoes.CACHE] ou [OrigemSugestoes.VAZIO_SEM_CACHE]).
     *         A implementação não deve retornar [Result.failure] no caminho
     *         de falha de rede — a degradação para cache vazio é sinalizada
     *         via origem, preservando o fluxo offline-first.
     *
     * Requirements: 3.1, 3.2, 7.2, 7.3
     */
    suspend fun buscarSugestoes(
        idInventario: Int,
        termoBusca: String,
        page: Int,
        size: Int
    ): Result<ResultadoSugestoes>

    /**
     * Atualiza o [Cache_Sugestoes_Local] com as sugestões recebidas do
     * servidor, associando-as ao inventário informado.
     *
     * A implementação deve persistir cada sugestão com sua descrição
     * normalizada (acento/caso-insensível) pré-computada, de modo que
     * consultas offline subsequentes possam filtrar sem normalizar a cada
     * execução (Requirement 3.4).
     *
     * Política de erro — esta operação NÃO deve propagar exceções de I/O
     * para o chamador. Em caso de falha (por exemplo, erro de escrita no
     * banco), a implementação deve logar o erro e retornar normalmente,
     * preservando o conteúdo anterior do cache (Requirement 8.7). Isso
     * garante que uma falha de atualização nunca interrompa o fluxo de
     * exibição de sugestões na UI.
     *
     * @param idInventario Identificador do inventário ao qual as sugestões
     *                     pertencem. Usado como parte da chave composta
     *                     do cache (isolamento por inventário —
     *                     Requirement 7.1).
     * @param sugestoes Lista de sugestões a serem persistidas via upsert
     *                  (inserção ou substituição por conflito de chave
     *                  primária composta). Pode estar vazia, caso em que
     *                  nenhuma operação de escrita é efetuada.
     *
     * Requirements: 7.1, 8.7
     */
    suspend fun atualizarCache(idInventario: Int, sugestoes: List<SugestaoDescricao>)

    /**
     * Consulta diretamente o [Cache_Sugestoes_Local] para o inventário
     * informado, aplicando filtro acento/caso-insensível sobre o termo
     * de busca e limitando o número de resultados retornados.
     *
     * Usado primariamente como fonte de dados em cenário offline (falha
     * de rede, timeout ou erro HTTP), conforme fluxo de fallback do
     * [buscarSugestoes] (Requirements 3.2, 7.2). A implementação deve
     * excluir entradas cujo patrimônio já esteja marcado como coletado
     * localmente (Requirement 8.3) e ordenar os resultados alfabeticamente
     * pela descrição normalizada, com desempate determinístico pelo
     * número do patrimônio.
     *
     * @param idInventario Identificador do [Inventario_Ativo] cujo cache
     *                     deve ser consultado.
     * @param termoBusca Texto livre usado para filtrar as sugestões. A
     *                   implementação normaliza internamente (NFD + remoção
     *                   de diacríticos + lowercase pt-BR) antes de comparar
     *                   contra a descrição normalizada pré-computada. String
     *                   vazia significa "sem filtro".
     * @param limit Número máximo de sugestões a retornar. Tipicamente 10
     *              em cenário offline, conforme teto definido para a UI
     *              quando sem conectividade (Requirement 7.2).
     *
     * @return Lista de sugestões vinda do cache, possivelmente vazia.
     *         Nunca lança — falhas de leitura devem ser tratadas pela
     *         implementação retornando lista vazia e logando o erro
     *         (Requirement 7.7).
     *
     * Requirements: 3.4, 7.2, 7.3, 7.7, 8.3
     */
    suspend fun buscarOffline(
        idInventario: Int,
        termoBusca: String,
        limit: Int
    ): List<SugestaoDescricao>

    /**
     * Marca, no [Cache_Sugestoes_Local], que o patrimônio de identificador
     * [idPatrimonio] foi coletado no [Inventario_Ativo] indicado, de modo
     * que sua descrição deixe de ser exibida como sugestão para esse
     * inventário.
     *
     * Essa operação deve ser invocada pela camada superior imediatamente
     * após a persistência local de uma coleta vinculada a um patrimônio
     * identificado, independentemente de a coleta já ter sido sincronizada
     * com o [Servidor_Mobile] (Requirement 8.1). A propriedade
     * `coletadoLocal` é usada pelo filtro do autocomplete para excluir a
     * entrada; o patrimônio permanece no cache (não é deletado) para
     * preservar auditoria e para que, em caso de múltiplos patrimônios
     * compartilhando a mesma descrição, a descrição permaneça visível
     * enquanto pelo menos um deles não estiver coletado (Requirement 8.4).
     *
     * @param idInventario Identificador do inventário onde a coleta foi
     *                     registrada. Combinado com [idPatrimonio] forma a
     *                     chave composta da entidade do cache.
     * @param idPatrimonio Identificador do patrimônio que foi coletado e
     *                     cuja entrada no cache deve ser marcada.
     *
     * Requirements: 8.1, 8.4
     */
    suspend fun marcarColetadoLocalmente(idInventario: Int, idPatrimonio: Int)

    /**
     * Remove do [Cache_Sugestoes_Local] todas as entradas cujo identificador
     * de inventário seja diferente do [idInventarioAtivo] informado.
     *
     * Garante o isolamento por inventário (Requirement 7.1): ao trocar o
     * [Inventario_Ativo] (por abertura de novo inventário, mudança de
     * contexto do coletor ou sincronização que altere o inventário vigente),
     * o cache de inventários anteriores é limpo para evitar que sugestões
     * obsoletas apareçam no autocomplete.
     *
     * Deve ser invocada ao iniciar a `ItemSemEtiquetaActivity` e/ou ao
     * detectar mudança do inventário ativo pela camada de apresentação.
     *
     * @param idInventarioAtivo Identificador do inventário atualmente
     *                          ativo, cujas entradas no cache devem ser
     *                          PRESERVADAS. Entradas associadas a qualquer
     *                          outro `idInventario` são removidas.
     *
     * Requirements: 7.1
     */
    suspend fun limparCacheDeOutrosInventarios(idInventarioAtivo: Int)
}
