package com.inventario.mobile.data.repository

import com.inventario.mobile.data.model.LoginData
import com.inventario.mobile.data.model.LoginResponse
import com.inventario.mobile.data.model.UsuarioDto
import com.inventario.mobile.data.remote.dto.LoginRequest
import com.inventario.mobile.data.remote.dto.RefreshTokenRequest
import com.inventario.mobile.domain.repository.AuthRepository
import com.inventario.mobile.data.remote.api.ApiService
import com.inventario.mobile.utils.ConnectivityResult
import com.inventario.mobile.utils.ServerConfigManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Implementação do AuthRepository que faz chamadas reais para a API
 */
class AuthRepositoryImpl(
    private val apiService: ApiService,
    private val serverConfigManager: ServerConfigManager
) : AuthRepository {

    override suspend fun login(loginRequest: LoginRequest): Result<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("AuthRepositoryImpl", "═══════════════════════════════════════════")
                android.util.Log.d("AuthRepositoryImpl", "CHAMANDO API DE LOGIN")
                android.util.Log.d("AuthRepositoryImpl", "Username: ${loginRequest.username}")
                android.util.Log.d("AuthRepositoryImpl", "DeviceId: ${loginRequest.deviceId}")
                android.util.Log.d("AuthRepositoryImpl", "═══════════════════════════════════════════")
                
                val response = apiService.login(loginRequest)
                
                android.util.Log.d("AuthRepositoryImpl", "Resposta recebida:")
                android.util.Log.d("AuthRepositoryImpl", "  Status Code: ${response.code()}")
                android.util.Log.d("AuthRepositoryImpl", "  Is Successful: ${response.isSuccessful}")
                android.util.Log.d("AuthRepositoryImpl", "  Headers: ${response.headers()}")
                android.util.Log.d("AuthRepositoryImpl", "  Body presente: ${response.body() != null}")
                
                if (response.isSuccessful) {
                    response.body()?.let { dtoResponse ->
                        android.util.Log.d("AuthRepositoryImpl", "Processando resposta:")
                        android.util.Log.d("AuthRepositoryImpl", "  Access Token: ${dtoResponse.accessToken?.take(20)}...")
                        android.util.Log.d("AuthRepositoryImpl", "  Refresh Token: ${dtoResponse.refreshToken?.take(20)}...")
                        android.util.Log.d("AuthRepositoryImpl", "  Expires In: ${dtoResponse.expiresIn}")
                        android.util.Log.d("AuthRepositoryImpl", "  User: ${dtoResponse.user}")
                        android.util.Log.d("AuthRepositoryImpl", "  User ID: ${dtoResponse.user.id}")
                        android.util.Log.d("AuthRepositoryImpl", "  User Nome: ${dtoResponse.user.nome}")
                        android.util.Log.d("AuthRepositoryImpl", "  User Login: ${dtoResponse.user.username}")
                        // Converter LoginResponse do DTO para o modelo
                        val usuarioModel = UsuarioDto(
                            id = dtoResponse.user.id,
                            login = dtoResponse.user.username,
                            nome = dtoResponse.user.nome,
                            email = dtoResponse.user.email,
                            ativo = dtoResponse.user.ativo,
                            perfil = dtoResponse.user.perfil,
                            setorId = dtoResponse.user.setorId
                        )
                        val loginData = LoginData(
                            accessToken = dtoResponse.accessToken,
                            refreshToken = dtoResponse.refreshToken,
                            expiresIn = dtoResponse.expiresIn,
                            usuario = usuarioModel
                        )
                        val loginResponse = LoginResponse(
                            success = true,
                            message = "Login realizado com sucesso",
                            data = loginData
                        )
                        Result.success(loginResponse)
                    } ?: Result.failure(Exception("Resposta vazia do servidor"))
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Credenciais inválidas. Verifique seu login e senha."
                        403 -> "Acesso negado. Usuário não autorizado."
                        404 -> "Serviço de autenticação não encontrado."
                        500 -> "Erro interno do servidor. Tente novamente mais tarde."
                        else -> "Erro de autenticação (${response.code()}): ${response.message()}"
                    }
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is UnknownHostException -> "Não foi possível conectar ao servidor. Verifique o IP configurado e sua conexão com a internet."
                    is ConnectException -> "Conexão recusada pelo servidor. Verifique se o servidor está rodando na porta 8081."
                    is SocketTimeoutException -> "Timeout na conexão. O servidor pode estar sobrecarregado ou indisponível."
                    is HttpException -> "Erro HTTP: ${e.message()}"
                    is IOException -> "Erro de rede: ${e.message}"
                    else -> "Erro inesperado: ${e.message}"
                }
                Result.failure(Exception(errorMessage))
            }
        }
    }

    override suspend fun refreshToken(refreshTokenRequest: RefreshTokenRequest): Result<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.refreshToken(refreshTokenRequest)
                
                if (response.isSuccessful) {
                    response.body()?.let { dtoResponse ->
                        // Converter LoginResponse do DTO para o modelo
                        val usuarioModel = UsuarioDto(
                             id = dtoResponse.user.id,
                             login = dtoResponse.user.username,
                             nome = dtoResponse.user.nome,
                             email = dtoResponse.user.email,
                             ativo = dtoResponse.user.ativo,
                             perfil = dtoResponse.user.perfil,
                             setorId = dtoResponse.user.setorId
                         )
                        val loginData = LoginData(
                            accessToken = dtoResponse.accessToken,
                            refreshToken = dtoResponse.refreshToken,
                            expiresIn = dtoResponse.expiresIn,
                            usuario = usuarioModel
                        )
                        val loginResponse = LoginResponse(
                            success = true,
                            message = "Token renovado com sucesso",
                            data = loginData
                        )
                        Result.success(loginResponse)
                    } ?: Result.failure(Exception("Resposta vazia do servidor"))
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Token de refresh inválido ou expirado."
                        else -> "Erro ao renovar token (${response.code()}): ${response.message()}"
                    }
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is UnknownHostException -> "Não foi possível conectar ao servidor para renovar o token."
                    is ConnectException -> "Conexão recusada pelo servidor."
                    is SocketTimeoutException -> "Timeout na renovação do token."
                    else -> "Erro ao renovar token: ${e.message}"
                }
                Result.failure(Exception(errorMessage))
            }
        }
    }

    override suspend fun testConnectivity(): ConnectivityResult {
        return serverConfigManager.testServerConnectivity()
    }
}