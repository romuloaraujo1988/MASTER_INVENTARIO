package com.inventario.mobile.utils

import android.content.Context
import com.inventario.mobile.R
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Classe utilitária para mapear exceções para mensagens de erro específicas
 */
object ErrorMapper {
    
    /**
     * Mapeia uma exceção para uma mensagem de erro específica
     */
    fun mapErrorToMessage(context: Context, throwable: Throwable): String {
        return when (throwable) {
            is UnknownHostException -> {
                context.getString(R.string.error_host_not_found)
            }
            is ConnectException -> {
                context.getString(R.string.error_connection_refused)
            }
            is SocketTimeoutException -> {
                context.getString(R.string.error_connection_timeout)
            }
            is HttpException -> {
                mapHttpErrorToMessage(context, throwable)
            }
            is IOException -> {
                context.getString(R.string.error_io_network, throwable.message ?: "Erro de rede")
            }
            else -> {
                // Verificar se a mensagem contém indicações de erro de autenticação
                val message = throwable.message?.lowercase() ?: ""
                when {
                    message.contains("401") || 
                    message.contains("unauthorized") || 
                    message.contains("credenciais") ||
                    message.contains("credentials") ||
                    message.contains("authentication") -> {
                        context.getString(R.string.error_http_401)
                    }
                    message.contains("403") || message.contains("forbidden") -> {
                        context.getString(R.string.error_http_403)
                    }
                    message.contains("404") || message.contains("not found") -> {
                        context.getString(R.string.error_http_404)
                    }
                    message.contains("500") || message.contains("internal server") -> {
                        context.getString(R.string.error_http_500)
                    }
                    else -> {
                        context.getString(R.string.error_unknown)
                    }
                }
            }
        }
    }
    
    /**
     * Mapeia erros HTTP específicos para mensagens apropriadas
     */
    private fun mapHttpErrorToMessage(context: Context, httpException: HttpException): String {
        return when (httpException.code()) {
            401 -> context.getString(R.string.error_http_401)
            403 -> context.getString(R.string.error_http_403)
            404 -> context.getString(R.string.error_http_404)
            500 -> context.getString(R.string.error_http_500)
            in 500..599 -> context.getString(R.string.error_server_error)
            else -> {
                val message = httpException.message() ?: "Erro HTTP"
                context.getString(R.string.error_http_generic, httpException.code(), message)
            }
        }
    }
    
    /**
     * Mapeia resultado de conectividade para mensagem
     */
    fun mapConnectivityResultToMessage(context: Context, isSuccess: Boolean, message: String?): String {
        return if (isSuccess) {
            context.getString(R.string.success_connectivity_test)
        } else {
            message ?: context.getString(R.string.error_connectivity_test)
        }
    }
}