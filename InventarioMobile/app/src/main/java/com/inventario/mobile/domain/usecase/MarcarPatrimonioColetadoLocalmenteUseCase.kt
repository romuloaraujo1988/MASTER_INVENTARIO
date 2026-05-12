package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.repository.SugestaoDescricaoRepository
import javax.inject.Inject

/**
 * Use Case: Marcar patrimônio como coletado localmente no [Cache_Sugestoes_Local].
 *
 * Regras de negócio:
 * - Deve ser invocado pela camada de apresentação imediatamente após a
 *   persistência local de uma coleta, independentemente de a coleta já
 *   ter sido sincronizada com o [Servidor_Mobile] (Requirement 8.1 —
 *   marcação imediata pós-coleta).
 * - A marcação local é o gatilho que dispara a persistência no cache Room
 *   via [SugestaoDescricaoRepository.marcarColetadoLocalmente]. O próprio
 *   DAO/Room cuida da persistência através de sessões (Requirement 8.2),
 *   garantindo que a descrição não volte a aparecer como sugestão mesmo
 *   que o app seja reiniciado antes da sincronização com o servidor.
 *
 * Comportamento no-op (guarda):
 * - Quando uma coleta de item sem etiqueta não possui vínculo com um
 *   patrimônio identificado ([idPatrimonio] == null), a operação é um
 *   no-op: não há patrimônio a ser marcado no cache e o método retorna
 *   silenciosamente sem tocar no repositório. Esse caso ocorre para
 *   coletas totalmente livres, onde o coletor descreveu o item sem
 *   selecionar uma sugestão vinculada a um patrimônio existente.
 *
 * Requirements: 8.1, 8.2
 */
class MarcarPatrimonioColetadoLocalmenteUseCase @Inject constructor(
    private val repository: SugestaoDescricaoRepository
) {
    /**
     * Marca o patrimônio como coletado localmente no cache do inventário informado.
     *
     * @param idInventario Identificador do [Inventario_Ativo] onde a coleta
     *                     foi registrada.
     * @param idPatrimonio Identificador do patrimônio vinculado à coleta.
     *                     Quando `null` (item sem etiqueta sem vínculo a
     *                     patrimônio existente), a operação é um no-op.
     */
    suspend operator fun invoke(idInventario: Int, idPatrimonio: Int?) {
        if (idPatrimonio == null) return // no-op para item sem etiqueta sem vínculo
        repository.marcarColetadoLocalmente(idInventario, idPatrimonio)
    }
}
