package com.inventario.mobile.data.repository

import android.content.Context
import android.util.Log
import com.inventario.mobile.data.local.LocalDataManager
import com.inventario.mobile.data.model.Coleta
import com.inventario.mobile.data.model.Patrimonio
import com.inventario.mobile.data.model.SyncStatus
import com.inventario.mobile.data.model.Usuario
import com.inventario.mobile.data.remote.api.ApiService
import com.inventario.mobile.data.remote.dto.LoginRequest
import com.inventario.mobile.data.remote.dto.PagedResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Resultado paginado de coletas
 */
data class PagedColetasResult(
    val coletas: List<Coleta>,
    val page: Int,
    val size: Int,
    val totalElements: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

/**
 * Repositório principal para gerenciar dados do inventário
 * Combina dados locais (SharedPreferences) com API remota (Retrofit)
 */
class InventarioRepository(
    private val apiService: ApiService,
    private val localDataManager: LocalDataManager
) {
    
    companion object {
        private const val TAG = "InventarioRepository"
        @Volatile
        private var INSTANCE: InventarioRepository? = null
        
        fun getInstance(context: Context, apiService: ApiService): InventarioRepository {
            return INSTANCE ?: synchronized(this) {
                val localDataManager = LocalDataManager.getInstance(context)
                INSTANCE ?: InventarioRepository(apiService, localDataManager).also { INSTANCE = it }
            }
        }
    }
    
    // ===== AUTENTICAÇÃO =====
    
    /**
     * Realiza login do usuário
     */
    suspend fun login(username: String, password: String): Result<Usuario> {
        return withContext(Dispatchers.IO) {
            try {
                val deviceId = getDeviceId()
                val appVersion = "1.0.0" // TODO: Pegar da BuildConfig
                
                val loginRequest = LoginRequest(
                    username = username,
                    password = password,
                    deviceId = deviceId,
                    appVersion = appVersion
                )
                
                val response = apiService.login(loginRequest)
                
                if (response.isSuccessful) {
                    val loginResponse = response.body()!!
                    
                    val usuario = Usuario.fromLoginResponse(
                        dto = loginResponse.user,
                        accessToken = loginResponse.accessToken,
                        refreshToken = loginResponse.refreshToken,
                        expiresIn = loginResponse.expiresIn
                    )
                    
                    // Salva usuário localmente
                    localDataManager.saveCurrentUser(usuario)
                    
                    Result.success(usuario)
                } else {
                    Result.failure(Exception("Erro de autenticação: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Faz logout do usuário
     */
    suspend fun logout() {
        withContext(Dispatchers.IO) {
            localDataManager.clearSessionData()
        }
    }
    
    /**
     * Verifica se há usuário logado
     */
    suspend fun isUserLoggedIn(): Boolean {
        return localDataManager.isUserLoggedIn()
    }
    
    /**
     * Recupera usuário atual
     */
    suspend fun getCurrentUser(): Usuario? {
        return localDataManager.getCurrentUser()
    }
    
    // ===== PATRIMÔNIOS =====
    
    /**
     * Busca patrimônio por número
     */
    suspend fun findPatrimonioByNumero(numero: String): Result<Patrimonio?> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "findPatrimonioByNumero iniciado para número: '$numero'")
                
                // Primeiro tenta buscar no cache local
                Log.d(TAG, "Buscando no cache local...")
                val cachedPatrimonio = localDataManager.findPatrimonioByNumero(numero)
                if (cachedPatrimonio != null) {
                    Log.d(TAG, "Patrimônio encontrado no cache local: ID=${cachedPatrimonio.id}, Número=${cachedPatrimonio.numeroPatrimonio}")
                    return@withContext Result.success(cachedPatrimonio)
                }
                Log.d(TAG, "Patrimônio não encontrado no cache local")
                
                // Se não encontrou no cache, busca na API usando o endpoint específico
                Log.d(TAG, "Fazendo chamada para API: getPatrimonioByNumero($numero)")
                val response = apiService.getPatrimonioByNumero(numero)
                
                Log.d(TAG, "Resposta da API recebida - Código: ${response.code()}, Sucesso: ${response.isSuccessful}")
                
                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Erro HTTP na API: ${response.code()} - $errorBody")
                    throw Exception("Erro HTTP ${response.code()}: $errorBody")
                }
                
                val responseBody = response.body()
                Log.d(TAG, "Corpo da resposta - Success: ${responseBody?.success}, Message: ${responseBody?.message}")
                
                if (responseBody?.success != true) {
                    Log.w(TAG, "API retornou success=false: ${responseBody?.message}")
                    throw Exception("Erro ao buscar patrimônio: ${responseBody?.message ?: "Erro desconhecido"}")
                }
                
                val patrimonioDto = responseBody.data
                if (patrimonioDto != null) {
                    Log.d(TAG, "DTO recebido da API - ID: ${patrimonioDto.id}, Código: ${patrimonioDto.codigo}")
                    val patrimonioModel = Patrimonio.fromMobileDto(patrimonioDto)
                    Log.d(TAG, "Modelo convertido - ID: ${patrimonioModel.id}, Número: ${patrimonioModel.numeroPatrimonio}")
                    
                    // Atualiza cache local
                    Log.d(TAG, "Salvando patrimônio no cache local")
                    localDataManager.savePatrimonio(patrimonioModel)
                    
                    Result.success(patrimonioModel)
                } else {
                    Log.w(TAG, "API retornou data=null")
                    Result.success(null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro na busca por número, tentando cache como fallback", e)
                
                // Em caso de erro de rede, tenta buscar no cache como fallback
                val cachedPatrimonio = localDataManager.findPatrimonioByNumero(numero)
                if (cachedPatrimonio != null) {
                    Log.d(TAG, "Patrimônio encontrado no cache como fallback")
                    Result.success(cachedPatrimonio)
                } else {
                    Log.e(TAG, "Patrimônio não encontrado nem no cache como fallback")
                    Result.failure(e)
                }
            }
        }
    }
    
    /**
     * Busca patrimônio por QR Code
     */
    suspend fun findPatrimonioByQrCode(qrCode: String): Result<Patrimonio?> {
        return withContext(Dispatchers.IO) {
            try {
                // Primeiro tenta buscar no cache local
                val cachedPatrimonio = localDataManager.findPatrimonioByQrCode(qrCode)
                if (cachedPatrimonio != null) {
                    return@withContext Result.success(cachedPatrimonio)
                }
                
                // Se não encontrou no cache, busca na API usando o endpoint específico
                val response = apiService.getPatrimonioByQrCode(qrCode)
                if (!response.isSuccessful || response.body()?.success != true) {
                    throw Exception("Erro ao buscar patrimônio: ${response.body()?.message ?: "Erro desconhecido"}")
                }
                
                val patrimonioDto = response.body()?.data
                if (patrimonioDto != null) {
                    val patrimonioModel = Patrimonio.fromMobileDto(patrimonioDto)
                    
                    // Atualiza cache local
                    localDataManager.savePatrimonio(patrimonioModel)
                    
                    Result.success(patrimonioModel)
                } else {
                    Result.success(null)
                }
            } catch (e: Exception) {
                // Em caso de erro de rede, tenta buscar no cache como fallback
                val cachedPatrimonio = localDataManager.findPatrimonioByQrCode(qrCode)
                if (cachedPatrimonio != null) {
                    Log.d(TAG, "Patrimônio encontrado no cache como fallback")
                    Result.success(cachedPatrimonio)
                } else {
                    Log.e(TAG, "Patrimônio não encontrado nem no cache como fallback")
                    Result.failure(e)
                }
            }
        }
    }
    
    /**
     * Busca patrimônio por ID
     */
    suspend fun findPatrimonioById(id: Long): Result<Patrimonio?> {
        return withContext(Dispatchers.IO) {
            try {
                // Primeiro tenta buscar no cache local
                val cachedPatrimonio = localDataManager.findPatrimonioById(id)
                if (cachedPatrimonio != null) {
                    return@withContext Result.success(cachedPatrimonio)
                }
                
                // Se não encontrou no cache, busca na API
                val response = apiService.getPatrimonios()
                if (!response.isSuccessful || response.body()?.success != true) {
                    throw Exception("Erro ao buscar patrimônios: ${response.body()?.message ?: "Erro desconhecido"}")
                }
                
                val patrimonios = response.body()?.data ?: emptyList()
                val patrimonio = patrimonios.find { it.id == id }
                
                if (patrimonio != null) {
                    val patrimonioModel = Patrimonio.fromMobileDto(patrimonio)
                    
                    // Atualiza cache local
                    val allPatrimonios = patrimonios.map { Patrimonio.fromMobileDto(it) }
                    localDataManager.savePatrimonioCache(allPatrimonios)
                    
                    Result.success(patrimonioModel)
                } else {
                    Result.success(null)
                }
            } catch (e: Exception) {
                // Em caso de erro de rede, tenta buscar no cache
                val cachedPatrimonio = localDataManager.findPatrimonioById(id)
                if (cachedPatrimonio != null) {
                    Result.success(cachedPatrimonio)
                } else {
                    Result.failure(e)
                }
            }
        }
    }
    
    /**
     * Busca todos os patrimônios
     */
    suspend fun getAllPatrimonios(): Result<List<Patrimonio>> {
        return withContext(Dispatchers.IO) {
            try {
                // Buscar na API (cache é gerenciado internamente pelo LocalDataManager)
                // val cachedPatrimonios = localDataManager.getPatrimonios()
                // if (cachedPatrimonios.isNotEmpty()) {
                //     return@withContext Result.success(cachedPatrimonios)
                // }
                
                // Se não encontrou no cache, busca na API
                val response = apiService.getPatrimonios()
                if (!response.isSuccessful || response.body()?.success != true) {
                    throw Exception("Erro ao buscar patrimônios: ${response.body()?.message ?: "Erro desconhecido"}")
                }
                
                val patrimonios = response.body()?.data ?: emptyList()
                val patrimoniosModel = patrimonios.map { Patrimonio.fromMobileDto(it) }
                
                // Atualiza cache local
                localDataManager.savePatrimonioCache(patrimoniosModel)
                
                Result.success(patrimoniosModel)
            } catch (e: Exception) {
                // Em caso de erro de rede, retorna falha
                // O cache é gerenciado internamente pelo LocalDataManager
                Result.failure(e)
            }
        }
    }
    
    // ===== COLETAS =====
    
    /**
     * Realiza coleta de patrimônio
     */
    suspend fun coletarPatrimonio(
        patrimonio: Patrimonio,
        observacoes: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ): Result<Coleta> {
        return withContext(Dispatchers.IO) {
            try {
                val usuario = withContext(Dispatchers.IO) { getCurrentUser() } ?: return@withContext Result.failure(Exception("Usuário não logado"))
                
                val coleta = Coleta.createLocal(
                    patrimonioId = patrimonio.id.toInt(),
                    usuarioId = usuario.id.toInt(),
                    observacoes = observacoes,
                    latitude = latitude,
                    longitude = longitude,
                    numeroPatrimonio = patrimonio.numeroPatrimonio,
                    descricaoPatrimonio = patrimonio.descricao
                )
                
                // Salva coleta localmente
                localDataManager.saveColeta(coleta)
                
                // Tenta enviar para o servidor
                try {
                    val inventarioAtivoResult = obterInventarioAtivo()
                    val inventarioAtivo = inventarioAtivoResult.getOrNull()
                    
                    if (inventarioAtivo == null) {
                        // Se não conseguir obter inventário ativo, mantém local
                        return@withContext Result.success(coleta)
                    }
                    
                    val coletaRequest = coleta.toMobileColetaRequest(
                        patrimonio = patrimonio,
                        idInventario = inventarioAtivo,
                        deviceId = android.os.Build.MODEL,
                        appVersion = "1.0.0"
                    )
                    
                    val response = apiService.createColeta(coletaRequest)
                    
                    // Verifica se foi bem-sucedido
                    if (response.isSuccessful && response.body()?.success == true) {
                        val coletaDto = response.body()?.data
                        if (coletaDto != null) {
                            val coletaAtualizada = Coleta.fromDto(coletaDto)
                            localDataManager.saveColeta(coletaAtualizada)
                            Result.success(coletaAtualizada)
                        } else {
                            Result.success(coleta)
                        }
                    } else {
                        // Se falhar, mantém local
                        Result.success(coleta)
                    }
                } catch (e: Exception) {
                    // Se falhar o envio, mantém coleta local para sincronização posterior
                    Result.success(coleta)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Realiza coleta de patrimônio com informação da sala
     */
    suspend fun coletarPatrimonioComSala(
        patrimonio: Patrimonio,
        salaNome: String,
        estadoEncontrado: String = "BOM",
        observacoes: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ): Result<Coleta> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Iniciando coleta do patrimônio ${patrimonio.numeroPatrimonio} na sala $salaNome com estado $estadoEncontrado")
                
                val usuario = withContext(Dispatchers.IO) { getCurrentUser() } ?: return@withContext Result.failure(Exception("Usuário não logado"))
                Log.d(TAG, "Usuário logado: ${usuario.nome} (ID: ${usuario.id})")
                
                // Detectar divergência: comparar sala onde foi encontrado com sala registrada
                val salaRegistrada = patrimonio.salaNome
                val divergencia = !salaRegistrada.isNullOrBlank() && 
                                 salaRegistrada.trim().uppercase() != salaNome.trim().uppercase()
                val motivoDivergencia = if (divergencia) {
                    "Item encontrado em sala diferente da registrada"
                } else null
                
                if (divergencia) {
                    Log.w(TAG, "DIVERGÊNCIA DETECTADA: Patrimônio ${patrimonio.numeroPatrimonio} registrado em '$salaRegistrada' mas encontrado em '$salaNome'")
                }
                
                val currentTime = System.currentTimeMillis().toString()
                val coleta = Coleta(
                    patrimonioId = patrimonio.id.toInt(),
                    usuarioId = usuario.id.toInt(),
                    dataColeta = currentTime,
                    localizacaoAtual = salaNome,
                    estadoEncontrado = estadoEncontrado.uppercase(),
                    observacoes = observacoes,
                    latitude = latitude,
                    longitude = longitude,
                    status = "COLETADO",
                    dataCriacao = currentTime,
                    dataAtualizacao = currentTime,
                    divergencia = divergencia,
                    motivoDivergencia = motivoDivergencia,
                    sincronizado = false,
                    numeroPatrimonio = patrimonio.numeroPatrimonio,
                    descricaoPatrimonio = patrimonio.descricao,
                    nomeSala = salaNome
                )
                
                Log.d(TAG, "Criada coleta: patrimonioId=${coleta.patrimonioId}, usuarioId=${coleta.usuarioId}, sala=$salaNome")
                
                // NOVA LÓGICA: Tentar enviar para o servidor ANTES de salvar localmente
                // Isso evita duplicação (salvar local + servidor)
                var tentativasRestantes = 2
                var coletaSalvaNoServidor = false
                var coletaFinal: Coleta = coleta
                
                while (tentativasRestantes > 0 && !coletaSalvaNoServidor) {
                    try {
                        Log.d(TAG, "Tentativa ${3 - tentativasRestantes} de 2: Enviando coleta para o servidor...")
                        
                        val inventarioAtivoResult = obterInventarioAtivo()
                        val inventarioAtivo = inventarioAtivoResult.getOrNull()
                        
                        if (inventarioAtivo == null) {
                            Log.w(TAG, "Inventário ativo não encontrado na tentativa ${3 - tentativasRestantes}")
                            tentativasRestantes--
                            if (tentativasRestantes > 0) {
                                kotlinx.coroutines.delay(1000) // Aguardar 1 segundo antes de tentar novamente
                            }
                            continue
                        }
                        
                        val coletaRequest = coleta.toMobileColetaRequest(
                            patrimonio = patrimonio,
                            idInventario = inventarioAtivo,
                            deviceId = android.os.Build.MODEL,
                            appVersion = "1.0.0"
                        )
                        
                        Log.d(TAG, "Enviando coleta: numeroPatrimonio=${coletaRequest.numeroPatrimonio}, idInventario=${coletaRequest.idInventario}")
                        val response = apiService.createColeta(coletaRequest)
                        
                        // Verifica se foi bem-sucedido
                        if (response.isSuccessful && response.body()?.success == true) {
                            val coletaDto = response.body()?.data
                            if (coletaDto != null) {
                                coletaFinal = Coleta.fromDto(coletaDto)
                                coletaSalvaNoServidor = true
                                Log.d(TAG, "✅ Coleta enviada para o servidor com sucesso na tentativa ${3 - tentativasRestantes}")
                            } else {
                                Log.w(TAG, "Resposta sem dados na tentativa ${3 - tentativasRestantes}")
                                tentativasRestantes--
                                if (tentativasRestantes > 0) {
                                    kotlinx.coroutines.delay(1000)
                                }
                            }
                        } else {
                            val errorMsg = response.body()?.message ?: "Erro desconhecido"
                            Log.w(TAG, "Falha ao enviar coleta na tentativa ${3 - tentativasRestantes}: $errorMsg")
                            tentativasRestantes--
                            if (tentativasRestantes > 0) {
                                kotlinx.coroutines.delay(1000)
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Erro ao enviar coleta na tentativa ${3 - tentativasRestantes}: ${e.message}")
                        tentativasRestantes--
                        if (tentativasRestantes > 0) {
                            kotlinx.coroutines.delay(1000)
                        }
                    }
                }
                
                // Salvar localmente apenas após tentar enviar para o servidor
                if (coletaSalvaNoServidor) {
                    // Salvar coleta sincronizada (com ID do servidor)
                    localDataManager.saveColeta(coletaFinal)
                    Log.d(TAG, "✅ Coleta salva localmente após sucesso no servidor (sincronizada)")
                    Result.success(coletaFinal)
                } else {
                    // Salvar coleta local (para sincronização posterior)
                    localDataManager.saveColeta(coleta)
                    Log.w(TAG, "⚠️ Coleta salva localmente após 2 tentativas falhadas no servidor (pendente de sincronização)")
                    Result.success(coleta)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao coletar patrimônio: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Realiza coleta de patrimônio por ID
     */
    suspend fun coletarPatrimonio(
        patrimonioId: Long,
        salaId: Int,
        observacoes: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ): Result<Coleta> {
        return withContext(Dispatchers.IO) {
            try {
                // Busca o patrimônio primeiro
                val patrimonioResult = findPatrimonioById(patrimonioId)
                patrimonioResult.fold(
                    onSuccess = { patrimonio ->
                        if (patrimonio != null) {
                            // Busca os dados da sala pelo ID
                            try {
                                val sala = apiService.getSalaById(salaId.toLong())
                                val salaNome = sala.nome ?: "Sala ${salaId}"
                                
                                // Usa o método coletarPatrimonioComSala que inclui dados da sala
                                coletarPatrimonioComSala(
                                    patrimonio = patrimonio,
                                    salaNome = salaNome,
                                    observacoes = observacoes,
                                    latitude = latitude,
                                    longitude = longitude
                                )
                            } catch (e: Exception) {
                                // Fallback: usa o método original se houver erro na busca da sala
                                Log.w(TAG, "Erro ao buscar sala $salaId: ${e.message}, usando método sem sala")
                                coletarPatrimonio(patrimonio, observacoes, latitude, longitude)
                            }
                        } else {
                            Result.failure(Exception("Patrimônio não encontrado"))
                        }
                    },
                    onFailure = { exception ->
                        Result.failure(exception)
                    }
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Recupera todas as coletas (locais + servidor)
     */
    suspend fun getColetas(): List<Coleta> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "getColetas: Iniciando busca de coletas")
                
                // Busca coletas do servidor
                val coletasServidor = try {
                    val response = apiService.getColetas()
                    if (response.isSuccessful && response.body()?.success == true) {
                        val serverColetas = response.body()?.data?.map { Coleta.fromDto(it) } ?: emptyList()
                        Log.d(TAG, "getColetas: ${serverColetas.size} coletas do servidor")
                        serverColetas
                    } else {
                        Log.w(TAG, "getColetas: Resposta sem sucesso do servidor")
                        emptyList()
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "getColetas: Erro ao buscar coletas do servidor: ${e.message}")
                    emptyList()
                }
                
                // Busca coletas locais
                val coletasLocais = localDataManager.getColetas()
                Log.d(TAG, "getColetas: ${coletasLocais.size} coletas locais")
                
                // Mescla as coletas (servidor + locais não sincronizadas)
                val coletasMap = mutableMapOf<Int, Coleta>()
                
                // Adiciona coletas do servidor
                coletasServidor.forEach { coleta ->
                    coleta.id?.let { id ->
                        coletasMap[id] = coleta
                    }
                }
                
                // Adiciona coletas locais que não estão no servidor
                coletasLocais.forEach { coleta ->
                    if (coleta.id == null || !coletasMap.containsKey(coleta.id)) {
                        // Usa patrimonioId como chave temporária para coletas locais
                        coletasMap[-coleta.patrimonioId] = coleta
                    }
                }
                
                val resultado = coletasMap.values.toList()
                Log.d(TAG, "getColetas: ${resultado.size} coletas finais após mesclagem")
                resultado
            } catch (e: Exception) {
                Log.e(TAG, "getColetas: Erro geral, retornando apenas coletas locais", e)
                // Em caso de erro, retorna apenas coletas locais
                localDataManager.getColetas()
            }
        }
    }
    
    /**
     * Recupera coletas com paginação (com cache)
     */
    suspend fun getColetasPaginadas(page: Int = 0, size: Int = 20, useCache: Boolean = true): Result<PagedColetasResult> {
        return withContext(Dispatchers.IO) {
            try {
                // TODO: Implementar cache quando necessário
                // Tentar buscar do cache primeiro
                // if (useCache) {
                //     val cacheManager = com.inventario.mobile.data.cache.CacheManager.getInstance(context)
                //     val cached = cacheManager.getColetas(page)
                //     if (cached != null) {
                //         return Result.success(cached as PagedColetasResult)
                //     }
                // }
                
                Log.d(TAG, "getColetasPaginadas: Buscando do servidor (página $page, tamanho $size)")
                
                val response = apiService.getColetasPaginadas(page, size)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val pagedResponse = response.body()?.data
                    
                    if (pagedResponse != null) {
                        val coletas = pagedResponse.content.map { Coleta.fromDto(it) }
                        
                        val result = PagedColetasResult(
                            coletas = coletas,
                            page = pagedResponse.page,
                            size = pagedResponse.size,
                            totalElements = pagedResponse.totalElements,
                            totalPages = pagedResponse.totalPages,
                            hasNext = pagedResponse.hasNext(),
                            hasPrevious = pagedResponse.hasPrevious()
                        )
                        
                        // TODO: Implementar cache quando necessário
                        // Salvar no cache
                        // if (useCache) {
                        //     val cacheManager = com.inventario.mobile.data.cache.CacheManager.getInstance(context)
                        //     cacheManager.putColetas(page, result)
                        // }
                        
                        Log.d(TAG, "getColetasPaginadas: ${coletas.size} coletas carregadas (página ${page + 1}/${pagedResponse.totalPages})")
                        Result.success(result)
                    } else {
                        Result.failure(Exception("Resposta vazia do servidor"))
                    }
                } else {
                    val errorMsg = "Erro ao buscar coletas: ${response.code()}"
                    Log.w(TAG, "getColetasPaginadas: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Log.e(TAG, "getColetasPaginadas: Erro", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Recupera apenas coletas locais (sem buscar do servidor)
     */
    suspend fun getColetasLocal(): List<Coleta> {
        return localDataManager.getColetas()
    }
    
    /**
     * Recupera coletas pendentes de sincronização
     */
    suspend fun getColetasPendentes(): List<Coleta> {
        return withContext(Dispatchers.IO) {
            localDataManager.getColetasPendentes()
        }
    }
    
    /**
     * Verifica se patrimônio já foi coletado (com logs detalhados para debug)
     */
    suspend fun isPatrimonioColetado(patrimonioId: Long): Boolean {
        return withContext(Dispatchers.IO) {
            val isColetadoLocal = localDataManager.isPatrimonioColetado(patrimonioId)
            Log.d(TAG, "Verificação de coleta - Patrimônio ID: $patrimonioId")
            Log.d(TAG, "Status local: ${if (isColetadoLocal) "COLETADO" else "NÃO COLETADO"}")
            
            // Lista todas as coletas locais para debug
            val coletasLocais = localDataManager.getColetas()
            Log.d(TAG, "Total de coletas locais: ${coletasLocais.size}")
            
            val coletaEspecifica = coletasLocais.find { it.patrimonioId.toLong() == patrimonioId }
            if (coletaEspecifica != null) {
                Log.d(TAG, "Coleta encontrada - ID: ${coletaEspecifica.patrimonioId}, Status: ${coletaEspecifica.status}, Sincronizado: ${coletaEspecifica.sincronizado}")
            } else {
                Log.d(TAG, "Nenhuma coleta local encontrada para este patrimônio")
            }
            
            isColetadoLocal
        }
    }

    /**
     * Verifica sincronização entre dados locais e servidor para um patrimônio específico
     */
    suspend fun verificarSincronizacaoPatrimonio(patrimonioId: Long): Result<SyncStatus> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Verificando sincronização para patrimônio ID: $patrimonioId")
                
                // Verificar status local
                val isColetadoLocal = localDataManager.isPatrimonioColetado(patrimonioId)
                Log.d(TAG, "Status local: ${if (isColetadoLocal) "COLETADO" else "NÃO COLETADO"}")
                
                // Buscar coleta local específica
                val coletaLocal = localDataManager.getColetas().find { it.patrimonioId.toLong() == patrimonioId }
                val sincronizado = coletaLocal?.sincronizado ?: false
                
                Log.d(TAG, "Coleta local encontrada: ${coletaLocal != null}")
                Log.d(TAG, "Status sincronização: $sincronizado")
                
                val syncStatus = SyncStatus(
                    patrimonioId = patrimonioId,
                    coletadoLocal = isColetadoLocal,
                    sincronizado = sincronizado,
                    coletaLocal = coletaLocal
                )
                
                Result.success(syncStatus)
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao verificar sincronização", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Remove coleta
     */
    suspend fun removeColeta(patrimonioId: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                localDataManager.removeColeta(patrimonioId.toLong())
                
                // TODO: Implementar remoção no servidor quando necessário
                
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    // ===== SINCRONIZAÇÃO =====
    
    /**
     * Sincroniza dados com o servidor
     */
    suspend fun syncData(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "=== INICIANDO SINCRONIZAÇÃO ===")
                
                // Atualiza cache de patrimônios
                Log.d(TAG, "Buscando patrimônios...")
                val response = apiService.getPatrimonios()
                Log.d(TAG, "Resposta patrimônios: isSuccessful=${response.isSuccessful}, success=${response.body()?.success}")
                
                if (!response.isSuccessful || response.body()?.success != true) {
                    throw Exception("Erro ao sincronizar patrimônios: ${response.body()?.message ?: "Erro desconhecido"}")
                }
                
                val patrimonios = response.body()?.data ?: emptyList()
                val patrimoniosModel = patrimonios.map { Patrimonio.fromMobileDto(it) }
                localDataManager.savePatrimonioCache(patrimoniosModel)
                
                // Buscar inventário ativo do servidor
                Log.d(TAG, "Buscando inventário ativo...")
                val inventarioAtivo = try {
                    val invResponse = apiService.obterInventarioAtivo()
                    Log.d(TAG, "Resposta inventário: isSuccessful=${invResponse.isSuccessful}, success=${invResponse.body()?.success}")
                    
                    if (invResponse.isSuccessful && invResponse.body()?.success == true) {
                        val invData = invResponse.body()?.data
                        val existeInventarioAtivo = invData?.get("existeInventarioAtivo") as? Boolean ?: false
                        
                        if (existeInventarioAtivo) {
                            val inventarioAtivoMap = invData?.get("inventarioAtivo") as? Map<String, Any>
                            Log.d(TAG, "inventarioAtivoMap: $inventarioAtivoMap")
                            val id = inventarioAtivoMap?.get("id")
                            Log.d(TAG, "ID extraído: $id (tipo: ${id?.javaClass?.simpleName})")
                            when (id) {
                                is Number -> id.toInt()
                                is Double -> id.toInt()
                                is Int -> id
                                else -> null
                            }
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao buscar inventário ativo", e)
                    null
                }
                
                if (inventarioAtivo == null) {
                    Log.e(TAG, "inventarioAtivo é NULL!")
                    throw Exception("Nenhum inventário ativo encontrado no servidor")
                }
                
                Log.d(TAG, "Inventário ativo ID: $inventarioAtivo")
                val idInventario = inventarioAtivo
                
                // Sincronizar coletas pendentes
                val coletasPendentes = localDataManager.getColetasPendentes()
                var sucessos = 0
                var falhas = 0
                val erros = mutableListOf<String>()
                
                for (coleta in coletasPendentes) {
                    try {
                        // Buscar patrimônio para completar os dados da coleta
                        val patrimonio = localDataManager.findPatrimonioById(coleta.patrimonioId.toLong())
                        
                        if (patrimonio == null) {
                            erros.add("Patrimônio ${coleta.patrimonioId} não encontrado")
                            falhas++
                            continue
                        }
                        
                        // Criar request no formato correto
                        val coletaRequest = coleta.toMobileColetaRequest(
                            patrimonio = patrimonio,
                            idInventario = idInventario,
                            deviceId = android.os.Build.MODEL,
                            appVersion = "1.0.0"
                        )
                        
                        val coletaResponse = apiService.createColeta(coletaRequest)
                        
                        // Verificar se o envio foi bem-sucedido
                        if (coletaResponse.isSuccessful && coletaResponse.body()?.success == true) {
                            localDataManager.removeColeta(coleta.patrimonioId.toLong())
                            sucessos++
                        } else {
                            val errorMsg = coletaResponse.body()?.message ?: "Erro desconhecido"
                            erros.add("Patrimônio ${patrimonio.numeroPatrimonio}: $errorMsg")
                            falhas++
                        }
                    } catch (e: Exception) {
                        erros.add("Erro ao sincronizar coleta ${coleta.patrimonioId}: ${e.message}")
                        falhas++
                    }
                }
                
                localDataManager.saveLastSyncTime(System.currentTimeMillis())
                
                if (falhas > 0 && sucessos == 0) {
                    val errosDetalhados = if (erros.isNotEmpty()) "\n${erros.joinToString("\n")}" else ""
                    throw Exception("Falha ao sincronizar todas as ${coletasPendentes.size} coletas pendentes$errosDetalhados")
                } else if (falhas > 0) {
                    val errosDetalhados = if (erros.isNotEmpty()) "\n${erros.joinToString("\n")}" else ""
                    throw Exception("Sincronização parcial: $sucessos enviadas, $falhas falharam$errosDetalhados")
                }
                
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Método de compatibilidade para sincronização baseada em contador
     * Alias para syncData() para manter compatibilidade com ScannerViewModel
     */
    suspend fun sincronizarDados(): Result<Unit> {
        return syncData()
    }
    
    /**
     * Verifica se precisa sincronizar
     */
    suspend fun needsSync(): Boolean {
        return localDataManager.needsSync()
    }
    
    /**
     * Obtém o timestamp da última sincronização formatado
     */
    suspend fun getLastSyncTime(): String {
        return localDataManager.getLastSyncTime()
    }
    
    /**
     * Obtém o inventário ativo (status EM_ANDAMENTO)
     */
    suspend fun obterInventarioAtivo(): Result<Int?> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.obterInventarioAtivo()
                if (response.isSuccessful && response.body()?.success == true) {
                    val apiResponse = response.body()
                    val data = apiResponse?.data
                    val existeInventarioAtivo = data?.get("existeInventarioAtivo") as? Boolean ?: false
                    
                    Log.d(TAG, "Resposta do servidor - existeInventarioAtivo: $existeInventarioAtivo")
                    
                    if (existeInventarioAtivo) {
                        val inventarioAtivo = data?.get("inventarioAtivo") as? Map<String, Any>
                        val id = inventarioAtivo?.get("id")
                        Log.d(TAG, "Inventário ativo encontrado - ID: $id, Tipo: ${id?.javaClass?.simpleName}")
                        when (id) {
                            is Number -> Result.success(id.toInt())
                            is Double -> Result.success(id.toInt())
                            is Int -> Result.success(id)
                            else -> {
                                Log.w(TAG, "ID do inventário em formato inesperado: $id")
                                Result.success(null)
                            }
                        }
                    } else {
                        Log.w(TAG, "Servidor retornou existeInventarioAtivo=false")
                        Result.success(null)
                    }
                } else {
                    val errorMsg = "Erro ao obter inventário ativo: ${response.code()} - ${response.body()?.message}"
                    Log.e(TAG, errorMsg)
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao obter inventário ativo", e)
                Result.failure(e)
            }
        }
    }
    
    // ===== UTILITÁRIOS =====
    
    /**
     * Gera ID único do dispositivo
     */
    private fun getDeviceId(): String {
        // TODO: Implementar geração/recuperação de ID único do dispositivo
        return UUID.randomUUID().toString()
    }
    
    /**
     * Limpa todos os dados
     */
    suspend fun clearAllData() {
        withContext(Dispatchers.IO) {
            localDataManager.clearAllData()
        }
    }

    /**
     * Busca patrimônios por responsável com paginação
     */
    suspend fun getPatrimoniosByResponsavel(
        responsavelId: Int,
        page: Int = 0,
        size: Int = 20,
        coletado: Boolean? = null
    ): Result<List<Patrimonio>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getPatrimoniosByResponsavel(responsavelId, page, size, coletado)
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        val patrimonios = apiResponse.data?.map { dto ->
                            Patrimonio.fromMobileDto(dto)
                        } ?: emptyList()
                        Result.success(patrimonios)
                    } else {
                        Result.failure(Exception(apiResponse.message ?: "Erro ao buscar patrimônios"))
                    }
                } else {
                    Result.failure(Exception("Erro na requisição: ${response.code()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao buscar patrimônios por responsável", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Conta patrimônios por responsável
     */
    suspend fun countPatrimoniosByResponsavel(responsavelId: Int): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.countPatrimoniosByResponsavel(responsavelId)
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        Result.success(apiResponse.data ?: 0)
                    } else {
                        Result.failure(Exception(apiResponse.message ?: "Erro ao contar patrimônios"))
                    }
                } else {
                    Result.failure(Exception("Erro na requisição: ${response.code()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao contar patrimônios por responsável", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Lista todos os responsáveis
     */
    suspend fun getResponsaveis(): Result<List<com.inventario.mobile.data.model.Responsavel>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getResponsaveis()
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        val responsaveis = apiResponse.data?.map { dto ->
                            com.inventario.mobile.data.model.Responsavel.fromDto(dto)
                        } ?: emptyList()
                        Result.success(responsaveis)
                    } else {
                        Result.failure(Exception(apiResponse.message ?: "Erro ao buscar responsáveis"))
                    }
                } else {
                    Result.failure(Exception("Erro na requisição: ${response.code()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao buscar responsáveis", e)
                Result.failure(e)
            }
        }
    }
}