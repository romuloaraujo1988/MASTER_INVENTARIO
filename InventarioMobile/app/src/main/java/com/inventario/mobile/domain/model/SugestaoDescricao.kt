package com.inventario.mobile.domain.model

/**
 * Modelo de domínio que representa uma sugestão de descrição de patrimônio
 * não coletado no inventário ativo, apresentada ao coletor no autocomplete
 * da tela de coleta de item sem etiqueta.
 *
 * Esta é uma entidade pura (sem dependências de Android ou Room),
 * pertencente à camada de domínio conforme Clean Architecture.
 *
 * Uma mesma [descricao] pode estar associada a múltiplos patrimônios
 * no inventário ativo; cada ocorrência é representada por uma instância
 * distinta com seu próprio [idPatrimonio] (ver Requirement 8.4).
 *
 * @property idPatrimonio Identificador único do patrimônio no servidor.
 *                        Usado para marcar o patrimônio como coletado no
 *                        cache local após a coleta (Requirement 8.1).
 * @property numeroPatrimonio Número de etiqueta do patrimônio (ex.: "IFMT-01234").
 *                            Serve como desempate determinístico na ordenação
 *                            alfabética da lista de sugestões (Requirement 3.1).
 * @property descricao Descrição textual cadastrada do patrimônio, exibida
 *                     no autocomplete e preenchida no campo livre ao ser
 *                     selecionada pelo coletor (Requirement 3.5).
 */
data class SugestaoDescricao(
    val idPatrimonio: Int,
    val numeroPatrimonio: String,
    val descricao: String
)
