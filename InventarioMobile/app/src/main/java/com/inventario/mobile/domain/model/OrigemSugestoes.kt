package com.inventario.mobile.domain.model

/**
 * Indica a origem do conjunto de sugestões de descrição apresentado ao coletor
 * na tela de coleta de item sem etiqueta.
 *
 * Este enum permite que as camadas superiores (ViewModel e UI) diferenciem
 * resultados vindos do servidor, do cache local ou a ausência de dados,
 * habilitando feedback contextual ao usuário (por exemplo, aviso de que as
 * sugestões exibidas vêm do cache em cenário offline — Requirement 3.2).
 *
 * Entidade pura, pertencente à camada de domínio conforme Clean Architecture,
 * sem dependências de Android ou frameworks externos.
 *
 * Valores:
 * - [SERVIDOR]: a lista foi obtida com sucesso do endpoint de sugestões e
 *   reflete o estado atual dos patrimônios não coletados no inventário ativo.
 *   Fluxo normal com conectividade de rede (Requirement 3.1).
 * - [CACHE]: a requisição ao servidor falhou (timeout, erro de rede ou erro
 *   HTTP) e o fallback para o [Cache_Sugestoes_Local] retornou ao menos uma
 *   sugestão. A UI deve sinalizar ao coletor que os dados podem estar
 *   desatualizados (Requirements 3.2, 7.2).
 * - [VAZIO_SEM_CACHE]: a requisição ao servidor falhou e o cache local está
 *   vazio para o inventário ativo. A UI deve informar a indisponibilidade
 *   de sugestões sem travar a entrada no campo livre (Requirement 7.3).
 */
enum class OrigemSugestoes {
    SERVIDOR,
    CACHE,
    VAZIO_SEM_CACHE
}
