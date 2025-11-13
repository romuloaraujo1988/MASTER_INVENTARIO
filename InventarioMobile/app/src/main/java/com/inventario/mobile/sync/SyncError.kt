package com.inventario.mobile.sync

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Sealed class para erros de sincronização
 * Permite tratamento específico baseado no tipo de erro
 */
sealed class SyncError : Exception() {
    
    // Erros que devem ter retry automático
    data class NetworkError(override val message: String) : SyncError()
    data class ServerError(val code: Int, override val message: String) : SyncError()
    data class TimeoutError(override val message: String) : SyncError()
    
    // Erros que NÃO devem ter retry
    data class AuthError(override val message: String) : SyncError()
    data class ValidationError(override val message: String) : SyncError()
    data class UnknownError(override val cause: Throwable?) : SyncError()
    
    /**
     * Verifica se o erro deve ter retry
     */
    fun shouldRetry(): Boolean {
        return when (this) {
            is NetworkError, is ServerError, is TimeoutError -> true
            is AuthError, is ValidationError, is UnknownError -> false
        }
    }
}

/**
 * Extension function para converter Throwable em SyncError
 */
fun Throwable.toSyncError(): SyncError {
    return when (this) {
        is IOException -> SyncError.NetworkError(
            message ?: "Erro de rede. Verifique sua conexão."
        )
        is HttpException -> {
            when (code()) {
                401, 403 -> SyncError.AuthError(
                    "Erro de autenticação. Faça login novamente."
                )
                in 400..499 -> SyncError.ValidationError(
                    message() ?: "Erro de validação dos dados."
                )
                in 500..599 -> SyncError.ServerError(
                    code(),
                    "Erro no servidor. Tente novamente mais tarde."
                )
                else -> SyncError.UnknownError(this)
            }
        }
        is SocketTimeoutException -> SyncError.TimeoutError(
            "Tempo esgotado. Verifique sua conexão."
        )
        else -> SyncError.UnknownError(this)
    }
}

/**
 * Extension function para obter mensagem amigável
 */
fun SyncError.getUserMessage(): String {
    return when (this) {
        is SyncError.NetworkError -> message
        is SyncError.ServerError -> message
        is SyncError.TimeoutError -> message
        is SyncError.AuthError -> message
        is SyncError.ValidationError -> message
        is SyncError.UnknownError -> cause?.message ?: "Erro desconhecido"
    }
}
