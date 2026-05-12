package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.repository.SugestaoDescricaoRepository
import javax.inject.Inject

/**
 * Use Case: remove do [Cache_Sugestoes_Local] entradas de inventários
 * diferentes do [idInventarioAtivo] informado.
 *
 * Garante o isolamento por inventário exigido pela Requirement 7.1: ao
 * abrir a `ItemSemEtiquetaActivity`, ou ao detectar mudança do inventário
 * ativo, o cache de inventários anteriores é limpo para evitar que
 * sugestões obsoletas apareçam no autocomplete.
 *
 * Delegação pura ao [SugestaoDescricaoRepository]: nenhuma lógica
 * adicional é introduzida aqui. O uso case existe apenas para preservar
 * a regra arquitetural (Clean Architecture) de que a camada de
 * apresentação — incluindo a `ItemSemEtiquetaViewModel` — não acesse
 * diretamente a interface de repositório da camada de domínio para
 * comportamentos triviais, mantendo o contrato explícito e testável.
 *
 * Política de erro: qualquer exceção lançada pelo repositório é
 * propagada ao chamador. A `ItemSemEtiquetaViewModel` é responsável por
 * tratar falhas (tipicamente via `runCatching`), já que a ausência de
 * limpeza não deve impedir a abertura da tela — no pior caso o filtro
 * do DAO já restringe por `idInventario` na consulta (defesa em
 * profundidade do isolamento).
 *
 * Requirements: 7.1
 */
class LimparCacheDeOutrosInventariosUseCase @Inject constructor(
    private val repository: SugestaoDescricaoRepository
) {
    /**
     * Remove entradas do cache cujo `idInventario` seja diferente do
     * [idInventarioAtivo].
     *
     * @param idInventarioAtivo Identificador do inventário corrente cujas
     *                          entradas no cache devem ser PRESERVADAS.
     */
    suspend operator fun invoke(idInventarioAtivo: Int) {
        repository.limparCacheDeOutrosInventarios(idInventarioAtivo)
    }
}
