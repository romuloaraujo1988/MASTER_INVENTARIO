package com.inventario.mobile.presentation.state

import com.inventario.mobile.domain.model.RelatorioFotoInfo
import java.io.File

/**
 * Estados possíveis da operação de relatório fotográfico de itens sem etiqueta.
 *
 * Utilizado pelo `CollectionViewViewModelClean` para comunicar o progresso
 * das operações de busca de informações e download de PDF à
 * `CollectionViewActivity`.
 *
 * Transições válidas:
 * ```
 * Idle → Loading     → InfoCarregada | Erro
 * Idle → Downloading → PdfPronto     | Erro
 * PdfPronto → Idle   (após limparEstadoRelatorioFoto)
 * Erro      → Idle   (após limparEstadoRelatorioFoto)
 * ```
 *
 * Requirements: 6.1
 */
sealed class RelatorioFotoState {

    /** Estado inicial — nenhuma operação em andamento. */
    object Idle : RelatorioFotoState()

    /** Buscando informações de fotos no servidor (`/info`). */
    object Loading : RelatorioFotoState()

    /**
     * Informações de fotos carregadas com sucesso.
     *
     * @property info Contagens de fotos retornadas pelo endpoint `/info`.
     */
    data class InfoCarregada(val info: RelatorioFotoInfo) : RelatorioFotoState()

    /** Download do PDF em andamento. */
    object Downloading : RelatorioFotoState()

    /**
     * PDF baixado e salvo com sucesso no cache do app.
     *
     * @property arquivo Arquivo PDF salvo em `context.cacheDir`.
     */
    data class PdfPronto(val arquivo: File) : RelatorioFotoState()

    /**
     * Erro em qualquer operação (busca de info ou download de PDF).
     *
     * @property mensagem Mensagem de erro localizada para exibição ao usuário.
     */
    data class Erro(val mensagem: String) : RelatorioFotoState()
}
