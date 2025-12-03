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
import com.inventario.mobile.utils.NetworkMonitor
import com.inventario.mobile.utils.OfflineNotificationManager
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
    
    private val networkMonitor = NetworkMonitor.getInstance(context)
    private val notificationManager = OfflineNotificationManager.getInstance(context)

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
        
        // Monitorar conexão de rede
        monitorNetworkConnection()
    }
    
    /**
     * Monitora mudanças na conexão de rede
     */
    private fun monitorNetworkConnection() {
        viewModelScope.launch {
            networkMonitor.observeConnectivity().collect { isConnected ->
                Log.d("LoginViewModel", "🌐 Conexão mudou: ${if (isConnected) "ONLINE" else "OFFLINE"}")
                
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    isOnline = isConnected,
                    offlineMode = !isConnected
                )
                
                // Verificar se tem dados locais
                val hasLocalData = checkHasLocalData()
                
                if (!isConnected) {
                    // Sem conexão - mostrar notificação
                    notificationManager.showOfflineModeNotification(hasLocalData)
                    
                    if (!hasLocalData) {
                        // Sem dados locais - pedir sincronização
                        notificationManager.showSyncNeededNotification()
                    }
                } else {
                    // Conexão restaurada
                    notificationManager.cancelOfflineNotification()
                    notificationManager.cancelSyncNeededNotification()
                    
                    // Verificar se tem dados pendentes
                    val hasPendingSync = checkHasPendingSync()
                    if (hasPendingSync) {
                        notificationManager.showConnectionRestoredNotification(true)
                    }
                }
            }
        }
    }
    
    /**
     * Verifica se tem dados locais (patrimônios, salas, etc)
     */
    private fun checkHasLocalData(): Boolean {
        // TODO: Implementar verificação real no banco Room
        // Por enquanto, verificar se tem token salvo
        return preferencesManager.getAccessToken() != null
    }
    
    /**
     * Verifica se tem coletas pendentes de sincronização
     */
    private fun checkHasPendingSync(): Boolean {
        // TODO: Implementar verificação real no banco Room
        return false
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
        
        Log.d("LoginViewModel", "═══════════════════════════════════════")
        Log.d("LoginViewModel", "MÉTODO LOGIN CHAMADO")
        Log.d("LoginViewModel", "Login no estado: '${currentState.login}'")
        Log.d("LoginViewModel", "Senha no estado: ${if (currentState.password.isNotEmpty()) "***" else "(vazio)"}")
        Log.d("LoginViewModel", "IP no estado: '${currentState.serverIp}'")
        Log.d("LoginViewModel", "═══════════════════════════════════════")
        
        // Validações básicas
        if (currentState.login.isBlank()) {
            Log.w("LoginViewModel", "❌ Validação falhou: Login está vazio")
            _uiState.value = currentState.copy(loginError = "Login não pode ser vazio")
            return
        }
        
        if (currentState.password.isBlank()) {
            Log.w("LoginViewModel", "❌ Validação falhou: Senha está vazia")
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
                        
                        // ✅ CRÍTICO: Salvar ID do usuário para uso nas coletas
                        preferencesManager.saveUserId(loginResponse.data.usuario.id.toInt())
                        Log.d("LoginViewModel", "✓ User ID salvo: ${loginResponse.data.usuario.id}")
                        
                        // Salvar usuário com tokens no LocalDataManager para o NetworkModule usar
                        val localDataManager = com.inventario.mobile.data.local.LocalDataManager.getInstance(context)
                        
                        Log.d("LoginViewModel", "═══════════════════════════════════════════")
                        Log.d("LoginViewModel", "DADOS DO USUÁRIO DO BACKEND")
                        Log.d("LoginViewModel", "ID: ${loginResponse.data.usuario.id}")
                        Log.d("LoginViewModel", "Login: ${loginResponse.data.usuario.login}")
                        Log.d("LoginViewModel", "Nome: ${loginResponse.data.usuario.nome}")
                        Log.d("LoginViewModel", "═══════════════════════════════════════════")
                        
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
                        
                        Log.d("LoginViewModel", "Usuário criado - ID: ${usuario.id}, Nome: ${usuario.nome}")
                        
                        localDataManager.saveCurrentUserSuspend(usuario)
                        Log.d("LoginViewModel", "✓ Usuário salvo no LocalDataManager com token")
                        
                        // Verificar se foi salvo corretamente
                        val usuarioRecuperado = localDataManager.getCurrentUser()
                        Log.d("LoginViewModel", "Usuário recuperado - ID: ${usuarioRecuperado?.id}, Nome: ${usuarioRecuperado?.nome}")
                        
                        // CRÍTICO: Forçar recriação do ApiService ANTES de navegar
                        com.inventario.mobile.data.remote.api.ApiClient.recreateApiService(context)
                        Log.d("LoginViewModel", "✓ ApiService recriado com novo token")
                        
                        Log.d("LoginViewModel", "Tokens e dados salvos com sucesso!")
                        Log.d("LoginViewModel", "Token: ${loginResponse.data.accessToken.take(20)}...")
                        Log.d("LoginViewModel", "Usuário: ${loginResponse.data.usuario.nome}")
                        Log.d("LoginViewModel", "Verificando isLoggedIn: ${preferencesManager.isLoggedIn()}")
                        
                        // ========== SALVAR DADOS PARA LOGIN OFFLINE ==========
                        // Salvar username e nome completo para possível login offline futuro
                        val username = currentState.login
                        val fullName = loginResponse.data.usuario.nome
                        val accessToken = loginResponse.data.accessToken
                        
                        preferencesManager.saveUserForOfflineLogin(username, fullName, accessToken)
                        
                        // ✅ SALVAR ÚLTIMO USUÁRIO QUE FEZ LOGIN (para preencher automaticamente)
                        preferencesManager.saveLastLoginUsername(username)
                        Log.d("LoginViewModel", "✓ Último usuário salvo: $username")
                        
                        Log.d("LoginViewModel", "✓ Dados salvos para possível login offline futuro")
                        
                        // ========== SALVAR INVENTÁRIO ATIVO ==========
                        // Salvar o inventário ativo retornado pelo servidor
                        // Isso garante que o app sempre use o ID correto do inventário
                        loginResponse.data.inventarioAtivo?.let { inventario ->
                            salvarInventarioAtivo(inventario)
                        } ?: run {
                            // FALLBACK: Se não veio no login, buscar separadamente
                            Log.w("LoginViewModel", "⚠️ Inventário não veio no login, buscando separadamente...")
                            buscarInventarioAtivoDoServidor()
                        }
                        
                        // TODO: Registrar dispositivo automaticamente
                        // registrarDispositivoAutomaticamente(loginResponse.data.usuario.id)
                        
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
    
    // TODO: Implementar registro automático de dispositivo
    /*
    private fun registrarDispositivoAutomaticamente(idUsuario: Int) {
        viewModelScope.launch {
            try {
                Log.d("LoginViewModel", "Registrando dispositivo automaticamente...")
                // Implementação pendente
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Erro ao registrar dispositivo: ${e.message}", e)
            }
        }
    }
    */
    
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
    
    // ========== MÉTODOS PARA LOGIN OFFLINE COM BIOMETRIA E PIN ==========
    
    /**
     * Verifica se deve oferecer configuração de PIN
     */
    fun checkPinSetup() {
        viewModelScope.launch {
            try {
                val pinAuthManager = com.inventario.mobile.security.PinAuthManager(context)
                val biometricManager = com.inventario.mobile.security.BiometricAuthManager(context)
                
                val hasBiometric = biometricManager.isBiometricAvailable().isAvailable()
                val hasPinEnabled = pinAuthManager.isPinEnabled()
                val hasUserSaved = preferencesManager.hasUserSavedLocally()
                
                // Oferecer PIN se:
                // 1. Não tem biometria
                // 2. Não tem PIN configurado
                // 3. Tem usuário salvo (fez login antes)
                val shouldOfferPin = !hasBiometric && !hasPinEnabled && hasUserSaved
                
                _uiState.value = _uiState.value.copy(
                    shouldOfferPinSetup = shouldOfferPin,
                    hasPinEnabled = hasPinEnabled
                )
                
                Log.d("LoginViewModel", "Check PIN: hasBiometric=$hasBiometric, hasPinEnabled=$hasPinEnabled, shouldOffer=$shouldOfferPin")
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Erro ao verificar PIN", e)
            }
        }
    }
    
    /**
     * Verifica se há login offline disponível
     * Deve ser chamado no onCreate da Activity
     */
    fun checkOfflineLogin() {
        viewModelScope.launch {
            try {
                val hasUser = preferencesManager.hasUserSavedLocally()
                val biometricEnabled = preferencesManager.isBiometricEnabled()
                val pinAuthManager = com.inventario.mobile.security.PinAuthManager(context)
                val pinEnabled = pinAuthManager.isPinEnabled()
                val isOnline = com.inventario.mobile.utils.NetworkUtils.isNetworkAvailable(context)
                val savedUserName = preferencesManager.getSavedUserFullName()
                
                Log.d("LoginViewModel", "═══════════════════════════════════════════")
                Log.d("LoginViewModel", "VERIFICANDO LOGIN OFFLINE")
                Log.d("LoginViewModel", "Usuário salvo localmente: $hasUser")
                Log.d("LoginViewModel", "Biometria habilitada: $biometricEnabled")
                Log.d("LoginViewModel", "PIN habilitado: $pinEnabled")
                Log.d("LoginViewModel", "Online: $isOnline")
                Log.d("LoginViewModel", "Nome do usuário: $savedUserName")
                Log.d("LoginViewModel", "═══════════════════════════════════════════")
                
                // Mostrar biometria OU PIN (prioridade para biometria)
                val showBiometric = hasUser && biometricEnabled
                val showPin = hasUser && pinEnabled && !biometricEnabled
                
                _uiState.value = _uiState.value.copy(
                    hasUserSavedLocally = hasUser,
                    biometricEnabled = biometricEnabled,
                    hasPinEnabled = pinEnabled,
                    isOnline = isOnline,
                    showBiometricButton = showBiometric,
                    showPinLogin = showPin,
                    savedUserName = savedUserName
                )
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Erro ao verificar login offline", e)
            }
        }
    }
    
    /**
     * Realiza login offline usando dados salvos localmente
     * Deve ser chamado APÓS validação biométrica bem-sucedida
     */
    fun loginWithBiometric() {
        viewModelScope.launch {
            try {
                Log.d("LoginViewModel", "═══════════════════════════════════════════")
                Log.d("LoginViewModel", "INICIANDO LOGIN OFFLINE COM BIOMETRIA")
                
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                // Carregar dados do usuário local
                val username = preferencesManager.getSavedUsername()
                val fullName = preferencesManager.getSavedUserFullName()
                val token = preferencesManager.getAccessToken()
                val isLoggedIn = preferencesManager.isLoggedIn()
                
                Log.d("LoginViewModel", "Username: $username")
                Log.d("LoginViewModel", "Nome completo: $fullName")
                Log.d("LoginViewModel", "Token presente: ${token != null}")
                Log.d("LoginViewModel", "Já logado: $isLoggedIn")
                
                if (username != null && fullName != null && token != null) {
                    // Login offline bem-sucedido
                    Log.d("LoginViewModel", "✅ LOGIN OFFLINE BEM-SUCEDIDO")
                    Log.d("LoginViewModel", "Usuário: $fullName")
                    Log.d("LoginViewModel", "Modo: OFFLINE")
                    Log.d("LoginViewModel", "═══════════════════════════════════════════")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccessful = true,
                        offlineMode = true
                    )
                } else {
                    // Dados locais inválidos ou incompletos
                    Log.e("LoginViewModel", "❌ DADOS LOCAIS INVÁLIDOS")
                    Log.e("LoginViewModel", "Username: ${username != null}")
                    Log.e("LoginViewModel", "FullName: ${fullName != null}")
                    Log.e("LoginViewModel", "Token: ${token != null}")
                    Log.e("LoginViewModel", "═══════════════════════════════════════════")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Dados locais inválidos. Conecte-se à internet para fazer login."
                    )
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "❌ ERRO NO LOGIN OFFLINE", e)
                Log.e("LoginViewModel", "═══════════════════════════════════════════")
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro ao fazer login offline: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Habilita login por biometria
     * Deve ser chamado após login bem-sucedido e confirmação do usuário
     */
    fun enableBiometric(username: String, fullName: String, accessToken: String) {
        try {
            Log.d("LoginViewModel", "Habilitando login por biometria para: $fullName")
            
            preferencesManager.saveUserForOfflineLogin(username, fullName, accessToken)
            preferencesManager.setBiometricEnabled(true)
            
            Log.d("LoginViewModel", "✅ Login por biometria habilitado com sucesso")
        } catch (e: Exception) {
            Log.e("LoginViewModel", "Erro ao habilitar biometria", e)
        }
    }
    
    /**
     * Desabilita login por biometria
     */
    fun disableBiometric() {
        try {
            Log.d("LoginViewModel", "Desabilitando login por biometria")
            preferencesManager.setBiometricEnabled(false)
            Log.d("LoginViewModel", "✅ Login por biometria desabilitado")
        } catch (e: Exception) {
            Log.e("LoginViewModel", "Erro ao desabilitar biometria", e)
        }
    }
    
    /**
     * Login com PIN
     */
    fun loginWithPin(pin: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                val pinAuthManager = com.inventario.mobile.security.PinAuthManager(context)
                
                // Verificar se conta está bloqueada
                if (pinAuthManager.isAccountLocked()) {
                    val minutes = pinAuthManager.getLockTimeRemaining()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Conta bloqueada. Tente novamente em $minutes minutos."
                    )
                    return@launch
                }
                
                // Validar PIN
                if (pinAuthManager.validatePin(pin)) {
                    // PIN correto - fazer login offline
                    loginWithOfflineCredentials()
                } else {
                    // PIN incorreto
                    val remaining = pinAuthManager.getRemainingAttempts()
                    
                    val message = if (remaining > 0) {
                        "PIN incorreto. Tentativas restantes: $remaining"
                    } else {
                        "Conta bloqueada por 30 minutos devido a múltiplas tentativas incorretas."
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = message,
                        pinAttemptsRemaining = remaining
                    )
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Erro no login com PIN", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro ao validar PIN: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Login offline usando credenciais salvas
     */
    private fun loginWithOfflineCredentials() {
        val username = preferencesManager.getSavedUsername()
        val fullName = preferencesManager.getSavedUserFullName()
        val token = preferencesManager.getAccessToken()
        
        if (username != null && fullName != null && token != null) {
            Log.d("LoginViewModel", "✅ Login offline bem-sucedido")
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isLoginSuccessful = true,
                offlineMode = true
            )
        } else {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Dados locais inválidos. Conecte-se à internet."
            )
        }
    }
    
    /**
     * Limpa todos os dados do usuário salvo
     * Útil para logout ou troca de usuário
     */
    fun clearSavedUser() {
        try {
            Log.d("LoginViewModel", "Limpando dados do usuário salvo")
            preferencesManager.clearSavedUser()
            
            // Limpar PIN também
            val pinAuthManager = com.inventario.mobile.security.PinAuthManager(context)
            pinAuthManager.clearPin()
            
            _uiState.value = _uiState.value.copy(
                hasUserSavedLocally = false,
                biometricEnabled = false,
                hasPinEnabled = false,
                showBiometricButton = false,
                showPinLogin = false,
                savedUserName = null
            )
            
            Log.d("LoginViewModel", "✅ Dados do usuário limpos")
        } catch (e: Exception) {
            Log.e("LoginViewModel", "Erro ao limpar dados do usuário", e)
        }
    }
    
    // ========== MÉTODOS AUXILIARES PARA INVENTÁRIO ATIVO ==========
    
    /**
     * Salva o inventário ativo no PreferencesManager
     */
    private fun salvarInventarioAtivo(inventario: com.inventario.mobile.data.model.InventarioAtivoDto) {
        preferencesManager.saveInventarioAtivo(
            id = inventario.id,
            nome = inventario.nome,
            status = inventario.status
        )
        
        // Salvar estatísticas se disponíveis
        val coletados = inventario.getColetados()
        if (inventario.totalPatrimonios != null && coletados != null) {
            preferencesManager.saveInventarioEstatisticas(
                totalPatrimonios = inventario.totalPatrimonios,
                totalColetados = coletados,
                percentualConclusao = inventario.percentualConclusao ?: 0.0
            )
        }
        
        Log.d("LoginViewModel", "═══════════════════════════════════════════")
        Log.d("LoginViewModel", "✅ INVENTÁRIO ATIVO SALVO")
        Log.d("LoginViewModel", "ID: ${inventario.id}")
        Log.d("LoginViewModel", "Nome: ${inventario.nome}")
        Log.d("LoginViewModel", "Status: ${inventario.status}")
        Log.d("LoginViewModel", "Progresso: ${inventario.percentualConclusao ?: 0}%")
        Log.d("LoginViewModel", "═══════════════════════════════════════════")
    }
    
    /**
     * FALLBACK: Busca o inventário ativo diretamente do endpoint /api/mobile/inventario/ativo
     * Chamado quando o inventário não vem na resposta do login
     */
    private fun buscarInventarioAtivoDoServidor() {
        viewModelScope.launch {
            try {
                Log.d("LoginViewModel", "Buscando inventário ativo do servidor (fallback)...")
                
                val apiService = com.inventario.mobile.data.remote.api.ApiClient.getApiService(context)
                val response = apiService.getInventarioAtivo()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val inventario = response.body()?.data
                    
                    if (inventario != null) {
                        salvarInventarioAtivo(inventario)
                        Log.d("LoginViewModel", "✅ Inventário obtido via fallback: ${inventario.nome}")
                    } else {
                        Log.w("LoginViewModel", "⚠️ Resposta do servidor não contém inventário")
                    }
                } else {
                    Log.e("LoginViewModel", "❌ Falha ao buscar inventário: ${response.message()}")
                }
                
            } catch (e: Exception) {
                Log.e("LoginViewModel", "❌ Erro ao buscar inventário ativo (fallback)", e)
                // Não falhar o login por causa disso - o app vai detectar na MainActivity
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
    val errorMessage: String? = null,
    
    // ========== CAMPOS PARA LOGIN OFFLINE COM BIOMETRIA E PIN ==========
    val hasUserSavedLocally: Boolean = false,
    val biometricEnabled: Boolean = false,
    val hasPinEnabled: Boolean = false,
    val isOnline: Boolean = true,
    val showBiometricButton: Boolean = false,
    val showPinLogin: Boolean = false,
    val offlineMode: Boolean = false,
    val savedUserName: String? = null,
    val shouldOfferPinSetup: Boolean = false,
    val pinAttemptsRemaining: Int = 3
)
