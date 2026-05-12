package com.inventario.mobile.domain.model

/**
 * Exceções de domínio para o fluxo de Relatório Fotográfico de Coletas.
 *
 * Estas exceções são lançadas pelos Use Cases da camada de domínio e
 * tratadas pelo ViewModel para exibir mensagens específicas ao usuário.
 */

/**
 * Lançada quando o servidor retorna HTTP 204 (nenhuma foto disponível).
 *
 * Indica que o inventário não possui fotos registradas para gerar o relatório.
 * O ViewModel deve exibir a mensagem ao usuário sem tratar como erro fatal.
 *
 * @see com.inventario.mobile.domain.usecase.BaixarRelatorioFotoPdfUseCase
 * Requirements: 3.4, 5.3
 */
class SemFotosException : Exception("Nenhuma foto disponível para gerar o relatório")

/**
 * Lançada quando o servidor retorna HTTP 403 (role insuficiente).
 *
 * Indica que o usuário autenticado não possui a role SUPERVISOR ou ADMIN
 * necessária para acessar o endpoint de geração de PDF.
 * O controle de acesso definitivo é feito pelo servidor; o app oculta
 * o botão de download apenas como medida de UX.
 *
 * @see com.inventario.mobile.domain.usecase.BaixarRelatorioFotoPdfUseCase
 * Requirements: 3.5, 5.4
 */
class PermissaoNegadaException : Exception("Você não tem permissão para acessar este relatório")
