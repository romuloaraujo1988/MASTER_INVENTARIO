package com.inventario.mobile.presentation.login

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.data.remote.dto.LoginRequest
import com.inventario.mobile.domain.repository.AuthRepository
import com.inventario.mobile.utils.ErrorMapper
import com.inventario.mobile.utils.PreferencesManager
import com.inventario.mobile.utils.ServerConfigManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// @HiltViewModel - Temporarily disabled
class LoginViewModel(
    private val context: Context,
    private val preferencesManager: PreferencesManager,
    private val serverConfigManager: ServerConfigManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        // Carregar IP salvo (a partir da URL persistida em PreferencesManager)
        val savedIp = serverConfigManager.getServerIp()
        if (savedIp != null && savedIp.isNotBlank()) {
            _uiState.value = _uiState.value.copy(serverIp = savedIp)
        } else {
            // Se não houver IP salvo, sugerir IPs comuns
            val suggestedIps = com.inventario.mobile.utils.ServerValidator.suggestCommonIps()
            if (suggestedIps.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(serverIp = suggestedIps[0])
            }
        }
    }

    fun updateLogin(login: String) {
        _uiState.value = _uiState.value.copy(
            login = login,
            loginError = null
        )
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = null
        )
    }

    fun updateServerIp(serverIp: String) {
        val currentState = _uiState.value
        
        // Validar IP (aceita IPv4 e "localhost")
        val isValidIp = ServerConfigManager.isValidIp(serverIp)
        val errorMessage = if (serverIp.isNotBlank() && !isValidIp) {
            "IP inválido"
        } else null
        
        _uiState.value = currentState.copy(
            serverIp = serverIp,
            serverIpError = errorMessage
        )
        
        // Salvar IP se válido
        if (isValidIp) {
            serverConfigManager.setServerIp(serverIp)
        }
    }

    fun login() {
        val currentState = _uiState.value
        
        // Validações básicas
        if (currentState.login.isBlank()) {
            _uiState.value = currentState.copy(loginError = "Login não pode ser vazio")
            return
        }
        
        if (currentState.password.isBlank()) {
            _uiState.value = currentState.copy(passwordError = "Senha não pode ser vazia")
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
                
                // Primeiro, testar conectividade com o servidor
                val connectivityResult = authRepository.testConnectivity()
                if (!connectivityResult.success) {
                    val errorMessage = ErrorMapper.mapConnectivityResultToMessage(
                        context, 
                        connectivityResult.success, 
                        connectivityResult.message
                    )
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = errorMessage
                    )
                    return@launch
                }
                
                // Obter informações do dispositivo
                val deviceId = preferencesManager.getDeviceId() ?: android.provider.Settings.Secure.getString(
                    context.contentResolver,
                    android.provider.Settings.Secure.ANDROID_ID
                ).also { preferencesManager.saveDeviceId(it) }
                
                val appVersion = try {
                    context.packageManager.getPackageInfo(context.packageName, 0).versionName
                } catch (e: Exception) {
                    "1.0"
                }
                
                // Fazer login real
                val loginRequest = LoginRequest(
                    username = currentState.login,
                    password = currentState.password,
                    deviceId = deviceId,
                    appVersion = appVersion
                )
                
                // Log detalhado da URL sendo usada
                val baseUrl = serverConfigManager.getBaseUrl()
                val loginUrl = serverConfigManager.getLoginUrl()
                Log.d("LoginViewModel", "═══════════════════════════════════════════")
                Log.d("LoginViewModel", "TENTANDO LOGIN")
                Log.d("LoginViewModel", "Base URL: $baseUrl")
                Log.d("LoginViewModel", "Login URL: $loginUrl")
                Log.d("LoginViewModel", "Username: ${currentState.login}")
                Log.d("LoginViewModel", "═══════════════════════════════════════════")
                
                val loginResult = authRepository.login(loginRequest)
                
                if (loginResult.isSuccess) {
                    val loginResponse = loginResult.getOrNull()
                    if (loginResponse != null && loginResponse.data != null) {
                        Log.d("LoginViewModel", "Login bem-sucedido! Salvando tokens e dados do usuário...")
                        
                        // Salvar tokens com expiração usando armazenamento criptografado
                        preferencesManager.saveTokens(
                            loginResponse.data.accessToken,
                            loginResponse.data.refreshToken,
                            loginResponse.data.expiresIn
                        )
                        
                        // IMPORTANTE: Salvar dados do usuário PRIMEIRO
                        preferencesManager.saveUserData(loginResponse.data.usuario)
                        
                        // Salvar usuário com tokens no LocalDataManager para o NetworkModule usar
                        val localDataManager = com.inventario.mobile.data.local.LocalDataManager.getInstance(context)
                        val usuarioRemoteDto = com.inventario.mobile.data.remote.dto.UsuarioDto(
                            id = loginResponse.data.usuario.id,
                            username = loginResponse.data.usuario.login,
                            nome = loginResponse.data.usuario.nome,
                            email = loginResponse.data.usuario.email,
                            ativo = loginResponse.data.usuario.ativo,
                            perfil = loginResponse.data.usuario.perfil,
                            setorId = loginResponse.data.usuario.setorId,
                            setorNome = null
                        )
                        val usuario = com.inventario.mobile.data.model.Usuario.fromLoginResponse(
                            usuarioRemoteDto,
                            loginResponse.data.accessToken,
                            loginResponse.data.refreshToken,
                            loginResponse.data.expiresIn
                        )
                        localDataManager.saveCurrentUserSuspend(usuario)
                        Log.d("LoginViewModel", "✓ Usuário salvo no LocalDataManager com token")
                        
                        // CRÍTICO: Forçar recriação do ApiService ANTES de navegar
                        com.inventario.mobile.data.remote.api.ApiClient.recreateApiService(context)
                        Log.d("LoginViewModel", "✓ ApiService recriado com novo token")
                        
                        Log.d("LoginViewModel", "Tokens e dados salvos com sucesso!")
                        Log.d("LoginViewModel", "Token: ${loginResponse.data.accessToken.take(20)}...")
                        Log.d("LoginViewModel", "Usuário: ${loginResponse.data.usuario.nome}")
                        Log.d("LoginViewModel", "Verificando isLoggedIn: ${preferencesManager.isLoggedIn()}")
                        
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            isLoginSuccessful = true
                        )
                    } else {
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            errorMessage = "Erro inesperado: resposta vazia do servidor"
                        )
                    }
                } else {
                    val exception = loginResult.exceptionOrNull() ?: Exception("Erro desconhecido no login")
                    Log.e("LoginViewModel", "═══════════════════════════════════════════")
                    Log.e("LoginViewModel", "LOGIN FALHOU - Result.failure")
                    Log.e("LoginViewModel", "Exception: ${exception.javaClass.simpleName}")
                    Log.e("LoginViewModel", "Mensagem: ${exception.message}")
                    Log.e("LoginViewModel", "Stack trace:", exception)
                    Log.e("LoginViewModel", "═══════════════════════════════════════════")
                    val errorMessage = ErrorMapper.mapErrorToMessage(context, exception)
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = errorMessage
                    )
                }
                
            } catch (e: Exception) {
                Log.e("LoginViewModel", "═══════════════════════════════════════════")
                Log.e("LoginViewModel", "EXCEÇÃO CAPTURADA NO LOGIN")
                Log.e("LoginViewModel", "Tipo: ${e.javaClass.simpleName}")
                Log.e("LoginViewModel", "Mensagem: ${e.message}")
                Log.e("LoginViewModel", "Stack trace:", e)
                Log.e("LoginViewModel", "═══════════════════════════════════════════")
                val errorMessage = ErrorMapper.mapErrorToMessage(context, e)
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = errorMessage
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * Valida a configuração do servidor
     * Útil para testar antes de fazer login
     */
    fun validateServerConfiguration() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                val validationResult = com.inventario.mobile.utils.ServerValidator.validateServerConfiguration(
                    serverConfigManager
                )
                
                if (validationResult.isValid) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "✓ ${validationResult.message}\n\nVocê pode fazer login agora."
                    )
                } else {
                    val helpMessage = if (validationResult.details?.ipConfigured == false) {
                        "\n\n${com.inventario.mobile.utils.ServerValidator.getConfigurationHelp()}"
                    } else {
                        ""
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "✗ ${validationResult.message}$helpMessage"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro ao validar servidor: ${e.message}"
                )
            }
        }
    }
}

// Estado da UI de Login
// O valor inicial do serverIp será carregado do PreferencesManager ou sugerido automaticamente
data class LoginUiState(
    val login: String = "",
    val password: String = "",
    val serverIp: String = "",  // Será preenchido pelo init()
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isLoginSuccessful: Boolean = false,
    val loginError: String? = null,
    val passwordError: String? = null,
    val serverIpError: String? = null,
    val errorMessage: String? = null
)