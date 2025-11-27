package com.inventario.mobile.data.repository

import android.app.Application
import android.content.Context
import com.inventario.mobile.data.local.LocalDataManager
import com.inventario.mobile.data.model.Coleta
import com.inventario.mobile.data.model.Patrimonio
import com.inventario.mobile.data.remote.api.ApiService
import com.inventario.mobile.data.remote.api.ApiClient

/**
 * InventarioRepository stub - Mantido para compatibilidade temporária
 * 
 * NOTA: Este é um stub temporário para manter compatibilidade com código legado.
 * Para novas funcionalidades, use os repositórios Clean Architecture específicos:
 * - SalaRepository para salas
 * - PatrimonioRepository para patrimônios
 * - ColetaRepository para coletas
 */
class InventarioRepository(
    private val apiService: ApiService,
    private val localDataManager: LocalDataManager,
    private val context: Context
) {
    
    // Construtor alternativo para aceitar Application
    constructor(application: Application) : this(
        ApiClient.getApiService(application.applicationContext),
        LocalDataManager.getInstance(application.applicationContext),
        application.applicationContext
    )
    
    companion object {
        @Volatile
        private var INSTANCE: InventarioRepository? = null
        
        fun getInstance(context: Context, apiService: ApiService): InventarioRepository {
            return INSTANCE ?: synchronized(this) {
                val localDataManager = LocalDataManager.getInstance(context)
                INSTANCE ?: InventarioRepository(apiService, localDataManager, context).also { INSTANCE = it }
            }
        }
        
        // Cache de estatísticas (válido por 30 segundos)
        private const val CACHE_DURATION_MS = 30_000L
    }
    
    // Cache em memória para estatísticas
    private var cachedStats: DashboardStats? = null
    private var cacheTimestamp: Long = 0L
    
    /**
     * Verifica se o cache ainda é válido
     */
    private fun isCacheValid(): Boolean {
        return cachedStats != null && (System.currentTimeMillis() - cacheTimestamp) < CACHE_DURATION_MS
    }
    
    /**
     * Invalida o cache de estatísticas
     * Deve ser chamado após registrar uma coleta
     */
    fun invalidarCacheEstatisticas() {
        android.util.Log.d("InventarioRepository", "Cache de estatísticas invalidado")
        cachedStats = null
        cacheTimestamp = 0L
    }
    
    // Métodos stub - retornam valores padrão
    suspend fun getAllPatrimoniosList(): List<Patrimonio> {
        return try {
            android.util.Log.d("InventarioRepository", "Buscando todos os patrimônios...")
            
            val response = apiService.getAllPatrimonios(page = 0, size = 10000)
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                
                if (apiResponse.success && apiResponse.data != null) {
                    val patrimonios = apiResponse.data.map { dto ->
                        Patrimonio(
                            id = dto.id,
                            numeroPatrimonio = dto.codigo,
                            descricao = dto.descricao,
                            marca = dto.marca,
                            modelo = dto.modelo,
                            numeroSerie = dto.numeroSerie,
                            estado = dto.estado,
                            valor = dto.valor,
                            setorId = dto.setorId,
                            setorNome = dto.setorNome,
                            salaId = dto.salaId,
                            salaNome = dto.salaNome,
                            responsavelId = dto.responsavelId,
                            responsavelNome = dto.responsavelNome,
                            coletado = dto.coletado,
                            dataColeta = dto.dataColeta,
                            observacoesColeta = null,
                            observacoes = dto.observacoes
                        )
                    }
                    
                    android.util.Log.d("InventarioRepository", "✓ ${patrimonios.size} patrimônios carregados")
                    patrimonios
                } else {
                    android.util.Log.w("InventarioRepository", "API retornou success=false ou data=null")
                    emptyList()
                }
            } else {
                android.util.Log.e("InventarioRepository", "Erro HTTP ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("InventarioRepository", "Erro ao buscar patrimônios", e)
            emptyList()
        }
    }
    
    suspend fun getPatrimoniosColetados(): List<Patrimonio> = emptyList()
    
    suspend fun getPatrimoniosNaoColetados(): List<Patrimonio> = emptyList()
    
    suspend fun findPatrimonioByNumero(numero: String): Result<Patrimonio?> {
        return try {
            android.util.Log.d("InventarioRepository", "Buscando patrimônio por número: $numero")
            
            // Primeiro tentar buscar do banco local (Room)
            val database = com.inventario.mobile.data.local.database.InventarioDatabase.getDatabase(context)
            val patrimonioDao = database.patrimonioDao()
            
            // ✅ CORREÇÃO: Usar query otimizada que busca nome da sala e status de coleta
            // Usar inventário ID 1 como padrão (pode ser melhorado depois)
            val inventarioId = 1
            
            android.util.Log.d("InventarioRepository", "Usando query otimizada com JOIN (sala + coleta)")
            val patrimonioEntity = patrimonioDao.buscarPorNumeroComStatusColeta(numero, inventarioId)
            
            if (patrimonioEntity != null) {
                android.util.Log.d("InventarioRepository", "✓ Patrimônio encontrado no banco local")
                android.util.Log.d("InventarioRepository", "  ID: ${patrimonioEntity.id}")
                android.util.Log.d("InventarioRepository", "  Número: ${patrimonioEntity.numeroPatrimonio}")
                android.util.Log.d("InventarioRepository", "  Sala ID: ${patrimonioEntity.salaId ?: patrimonioEntity.idSala}")
                android.util.Log.d("InventarioRepository", "  Sala Nome: ${patrimonioEntity.salaNome ?: patrimonioEntity.nomeSala}")
                android.util.Log.d("InventarioRepository", "  Coletado: ${patrimonioEntity.coletado}")
                android.util.Log.d("InventarioRepository", "  Coletado Por: ${patrimonioEntity.coletadoPor}")
                
                val patrimonio = Patrimonio(
                    id = patrimonioEntity.id,
                    numeroPatrimonio = patrimonioEntity.numeroPatrimonio,
                    descricao = patrimonioEntity.descricao,
                    marca = patrimonioEntity.marca,
                    modelo = patrimonioEntity.modelo,
                    numeroSerie = patrimonioEntity.numeroSerie,
                    estado = patrimonioEntity.estado,
                    valor = patrimonioEntity.valor,
                    setorId = patrimonioEntity.setorId?.toLong(),
                    setorNome = patrimonioEntity.setorNome,
                    salaId = patrimonioEntity.salaId?.toLong() ?: patrimonioEntity.idSala?.toLong(),
                    salaNome = patrimonioEntity.salaNome ?: patrimonioEntity.nomeSala,  // ✅ CORREÇÃO: Prioriza salaNome
                    responsavelId = patrimonioEntity.responsavelId?.toLong() ?: patrimonioEntity.idResponsavel?.toLong(),
                    responsavelNome = patrimonioEntity.responsavelNome ?: patrimonioEntity.nomeResponsavel,
                    coletado = patrimonioEntity.coletado,  // ✅ CORREÇÃO: Agora vem do JOIN
                    dataColeta = patrimonioEntity.dataColeta?.toString(),
                    coletadoPor = patrimonioEntity.coletadoPor,  // ✅ CORREÇÃO: Agora vem do JOIN
                    dataColetaFormatada = patrimonioEntity.dataColeta?.let { 
                        try {
                            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                            sdf.format(java.util.Date(it))
                        } catch (e: Exception) {
                            null
                        }
                    },
                    observacoesColeta = patrimonioEntity.observacoesColeta,
                    observacoes = patrimonioEntity.observacoes
                )
                
                android.util.Log.d("InventarioRepository", "✓ Patrimônio mapeado:")
                android.util.Log.d("InventarioRepository", "  salaNome: ${patrimonio.salaNome}")
                android.util.Log.d("InventarioRepository", "  coletado: ${patrimonio.coletado}")
                android.util.Log.d("InventarioRepository", "  coletadoPor: ${patrimonio.coletadoPor}")
                
                return Result.success(patrimonio)
            }
            
            // Se não encontrou no banco local, buscar da API
            android.util.Log.d("InventarioRepository", "Patrimônio não encontrado localmente, buscando da API...")
            
            val response = apiService.getPatrimonioByNumero(numero)
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                
                if (apiResponse.success && apiResponse.data != null) {
                    val dto = apiResponse.data
                    
                    val patrimonio = Patrimonio(
                        id = dto.id,
                        numeroPatrimonio = dto.codigo,
                        descricao = dto.descricao,
                        marca = dto.marca,
                        modelo = dto.modelo,
                        numeroSerie = dto.numeroSerie,
                        estado = dto.estado,
                        valor = dto.valor,
                        setorId = dto.setorId,
                        setorNome = dto.setorNome,
                        salaId = dto.salaId,
                        salaNome = dto.salaNome,
                        responsavelId = dto.responsavelId,
                        responsavelNome = dto.responsavelNome,
                        coletado = dto.coletado,
                        dataColeta = dto.dataColeta,
                        observacoesColeta = null,
                        observacoes = dto.observacoes
                    )
                    
                    android.util.Log.d("InventarioRepository", "✓ Patrimônio encontrado na API")
                    
                    // Salvar no banco local para cache
                    try {
                        val entity = com.inventario.mobile.data.local.entity.PatrimonioEntity(
                            id = patrimonio.id,
                            numero = patrimonio.numeroPatrimonio,
                            numeroPatrimonio = patrimonio.numeroPatrimonio,
                            descricao = patrimonio.descricao,
                            marca = patrimonio.marca,
                            modelo = patrimonio.modelo,
                            numeroSerie = patrimonio.numeroSerie,
                            estado = patrimonio.estado,
                            valor = patrimonio.valor,
                            setorId = patrimonio.setorId?.toInt(),
                            setorNome = patrimonio.setorNome,
                            idSala = patrimonio.salaId?.toInt(),
                            nomeSala = patrimonio.salaNome,
                            salaId = patrimonio.salaId?.toInt(),
                            salaNome = patrimonio.salaNome,
                            idResponsavel = patrimonio.responsavelId?.toInt(),
                            nomeResponsavel = patrimonio.responsavelNome,
                            responsavelId = patrimonio.responsavelId?.toInt(),
                            responsavelNome = patrimonio.responsavelNome,
                            status = patrimonio.estado,
                            coletado = patrimonio.coletado,
                            dataColeta = patrimonio.dataColeta?.toLongOrNull(),
                            coletadoPor = patrimonio.coletadoPor,
                            observacoesColeta = patrimonio.observacoesColeta,
                            observacoes = patrimonio.observacoes
                        )
                        patrimonioDao.inserir(entity)
                        android.util.Log.d("InventarioRepository", "✓ Patrimônio salvo no cache local")
                    } catch (e: Exception) {
                        android.util.Log.w("InventarioRepository", "Erro ao salvar no cache local", e)
                    }
                    
                    Result.success(patrimonio)
                } else {
                    android.util.Log.w("InventarioRepository", "Patrimônio não encontrado na API")
                    Result.success(null)
                }
            } else {
                android.util.Log.e("InventarioRepository", "Erro HTTP ${response.code()}")
                Result.success(null)
            }
        } catch (e: Exception) {
            android.util.Log.e("InventarioRepository", "Erro ao buscar patrimônio por número", e)
            Result.failure(e)
        }
    }
    
    suspend fun coletarPatrimonio(
        patrimonio: Patrimonio,
        observacoes: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ): Result<Coleta> = Result.failure(Exception("Use ColetaRepository"))
    
    suspend fun coletarPatrimonioComSala(
        patrimonio: Patrimonio,
        salaNome: String,
        estadoEncontrado: String = "BOM",
        observacoes: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ): Result<Coleta> {
        return try {
            android.util.Log.d("InventarioRepository", "═══════════════════════════════════════")
            android.util.Log.d("InventarioRepository", "INICIANDO COLETA DE PATRIMÔNIO")
            android.util.Log.d("InventarioRepository", "═══════════════════════════════════════")
            android.util.Log.d("InventarioRepository", "Patrimônio: ${patrimonio.numeroPatrimonio}")
            android.util.Log.d("InventarioRepository", "Sala: $salaNome")
            android.util.Log.d("InventarioRepository", "Estado: $estadoEncontrado")
            android.util.Log.d("InventarioRepository", "Observações: $observacoes")
            
            // Obter dados do usuário e inventário
            val usuarioId = localDataManager.getUserId()
            // TODO: Obter inventário ativo do PreferencesManager ou API
            val inventarioId = 1 // Usar inventário padrão por enquanto
            
            // Criar DTO para enviar à API
            val coletaRequest = com.inventario.mobile.data.remote.dto.MobileColetaRequest(
                numeroPatrimonio = patrimonio.numeroPatrimonio,
                idInventario = inventarioId,
                usuarioId = usuarioId,
                idSala = patrimonio.salaId?.toInt(),
                localizacaoEncontrada = salaNome,
                estadoEncontrado = estadoEncontrado,
                observacaoColeta = observacoes,
                dataColeta = System.currentTimeMillis().toString(),
                latitude = latitude,
                longitude = longitude,
                deviceId = android.provider.Settings.Secure.getString(context.contentResolver, android.provider.Settings.Secure.ANDROID_ID),
                appVersion = try { context.packageManager.getPackageInfo(context.packageName, 0).versionName } catch (e: Exception) { "1.0.0" }
            )
            
            android.util.Log.d("InventarioRepository", "DTO criado:")
            android.util.Log.d("InventarioRepository", "  numeroPatrimonio: ${coletaRequest.numeroPatrimonio}")
            android.util.Log.d("InventarioRepository", "  usuarioId: ${coletaRequest.usuarioId}")
            android.util.Log.d("InventarioRepository", "  estadoEncontrado: ${coletaRequest.estadoEncontrado}")
            android.util.Log.d("InventarioRepository", "  idInventario: ${coletaRequest.idInventario}")
            android.util.Log.d("InventarioRepository", "  localizacaoEncontrada: ${coletaRequest.localizacaoEncontrada}")
            
            // Tentar enviar para API
            android.util.Log.d("InventarioRepository", "Tentando enviar coleta para API...")
            android.util.Log.d("InventarioRepository", "Request: numeroPatrimonio=${coletaRequest.numeroPatrimonio}, idInventario=${coletaRequest.idInventario}, usuarioId=${coletaRequest.usuarioId}")
            
            try {
                val response = apiService.registrarColeta(coletaRequest)
                
                android.util.Log.d("InventarioRepository", "Response recebida: code=${response.code()}, isSuccessful=${response.isSuccessful}")
                
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    
                    android.util.Log.d("InventarioRepository", "API Response: success=${apiResponse.success}, hasData=${apiResponse.data != null}")
                    
                    if (apiResponse.success && apiResponse.data != null) {
                        val dto = apiResponse.data
                        
                        val coleta = Coleta(
                            id = dto.id?.toInt(),
                            patrimonioId = patrimonio.id.toInt(),
                            usuarioId = usuarioId,
                            dataColeta = dto.dataColeta ?: System.currentTimeMillis().toString(),
                            localizacaoAtual = salaNome,
                            estadoEncontrado = estadoEncontrado,
                            observacoes = observacoes,
                            latitude = latitude,
                            longitude = longitude,
                            sincronizado = true,
                            status = "SINCRONIZADO",
                            numeroPatrimonio = patrimonio.numeroPatrimonio,
                            nomeSala = salaNome
                        )
                        
                        android.util.Log.d("InventarioRepository", "✓ Coleta registrada com sucesso na API")
                        
                        // Salvar no banco local
                        try {
                            val database = com.inventario.mobile.data.local.database.InventarioDatabase.getDatabase(context)
                            val coletaDao = database.coletaDao()
                            
                            val coletaEntity = com.inventario.mobile.data.local.entity.ColetaEntity(
                                id = coleta.id?.toLong() ?: 0L,
                                idPatrimonio = coleta.patrimonioId,
                                numeroPatrimonio = coleta.numeroPatrimonio ?: patrimonio.numeroPatrimonio,
                                idInventario = inventarioId,
                                idSala = patrimonio.salaId?.toInt(),
                                nomeSala = salaNome,
                                idResponsavel = patrimonio.responsavelId?.toInt(),
                                nomeResponsavel = patrimonio.responsavelNome,
                                observacao = coleta.observacoes,
                                estadoPatrimonio = coleta.estadoEncontrado,
                                latitude = coleta.latitude,
                                longitude = coleta.longitude,
                                dataColeta = coleta.dataColeta.toLongOrNull() ?: System.currentTimeMillis(),
                                idUsuario = coleta.usuarioId,
                                nomeUsuario = localDataManager.getUserName() ?: "Usuário",
                                sincronizado = true,
                                servidorId = coleta.id?.toLong()
                            )
                            
                            coletaDao.inserir(coletaEntity)
                            
                            // Marcar patrimônio como coletado
                            val patrimonioDao = database.patrimonioDao()
                            patrimonioDao.marcarComoColetado(patrimonio.id)
                            
                            android.util.Log.d("InventarioRepository", "✓ Coleta salva no banco local")
                        } catch (e: Exception) {
                            android.util.Log.w("InventarioRepository", "Erro ao salvar coleta localmente", e)
                        }
                        
                        // Invalidar cache de estatísticas
                        invalidarCacheEstatisticas()
                        
                        return Result.success(coleta)
                    } else {
                        android.util.Log.w("InventarioRepository", "API retornou success=false ou data=null")
                        android.util.Log.w("InventarioRepository", "Message: ${apiResponse.message}")
                    }
                } else {
                    android.util.Log.w("InventarioRepository", "Response não foi successful ou body é null")
                    if (!response.isSuccessful) {
                        android.util.Log.e("InventarioRepository", "HTTP Error: ${response.code()} - ${response.message()}")
                        try {
                            val errorBody = response.errorBody()?.string()
                            android.util.Log.e("InventarioRepository", "Error body: $errorBody")
                        } catch (e: Exception) {
                            android.util.Log.e("InventarioRepository", "Não foi possível ler error body", e)
                        }
                    }
                }
                
                android.util.Log.w("InventarioRepository", "Falha na API, salvando coleta localmente (offline)")
            } catch (e: Exception) {
                android.util.Log.e("InventarioRepository", "EXCEÇÃO ao conectar com API!", e)
                android.util.Log.e("InventarioRepository", "Tipo: ${e.javaClass.simpleName}")
                android.util.Log.e("InventarioRepository", "Mensagem: ${e.message}")
                android.util.Log.e("InventarioRepository", "Salvando localmente (offline)...")
            }
            
            // Se chegou aqui, salvar apenas localmente (modo offline)
            android.util.Log.d("InventarioRepository", "Salvando coleta no modo OFFLINE")
            
            val database = com.inventario.mobile.data.local.database.InventarioDatabase.getDatabase(context)
            val coletaDao = database.coletaDao()
            val patrimonioDao = database.patrimonioDao()
            
            android.util.Log.d("InventarioRepository", "Criando ColetaEntity...")
            val coletaEntity = com.inventario.mobile.data.local.entity.ColetaEntity(
                id = 0, // ID será gerado pelo Room
                idPatrimonio = patrimonio.id.toInt(),
                numeroPatrimonio = patrimonio.numeroPatrimonio,
                idInventario = inventarioId,
                idSala = patrimonio.salaId?.toInt(),
                nomeSala = salaNome,
                idResponsavel = patrimonio.responsavelId?.toInt(),
                nomeResponsavel = patrimonio.responsavelNome,
                observacao = observacoes,
                estadoPatrimonio = estadoEncontrado,
                latitude = latitude,
                longitude = longitude,
                dataColeta = System.currentTimeMillis(),
                idUsuario = usuarioId,
                nomeUsuario = localDataManager.getUserName() ?: "Usuário",
                sincronizado = false // Marcar como não sincronizado
            )
            
            android.util.Log.d("InventarioRepository", "Inserindo coleta no banco...")
            val coletaId = coletaDao.inserir(coletaEntity)
            android.util.Log.d("InventarioRepository", "✓ Coleta inserida com ID: $coletaId")
            
            // Marcar patrimônio como coletado
            android.util.Log.d("InventarioRepository", "Marcando patrimônio como coletado...")
            patrimonioDao.marcarComoColetado(patrimonio.id)
            android.util.Log.d("InventarioRepository", "✓ Patrimônio marcado como coletado")
            
            val coleta = Coleta(
                id = coletaId.toInt(),
                patrimonioId = patrimonio.id.toInt(),
                usuarioId = usuarioId,
                dataColeta = System.currentTimeMillis().toString(),
                localizacaoAtual = salaNome,
                estadoEncontrado = estadoEncontrado,
                observacoes = observacoes,
                latitude = latitude,
                longitude = longitude,
                sincronizado = false,
                status = "PENDENTE",
                numeroPatrimonio = patrimonio.numeroPatrimonio,
                nomeSala = salaNome
            )
            
            android.util.Log.d("InventarioRepository", "✓ Coleta salva localmente (modo offline)")
            android.util.Log.d("InventarioRepository", "  Coleta ID: $coletaId")
            android.util.Log.d("InventarioRepository", "  Patrimônio: ${patrimonio.numeroPatrimonio}")
            android.util.Log.d("InventarioRepository", "  Sala: $salaNome")
            android.util.Log.d("InventarioRepository", "  Estado: $estadoEncontrado")
            
            // Invalidar cache de estatísticas
            invalidarCacheEstatisticas()
            
            return Result.success(coleta)
            
        } catch (e: Exception) {
            android.util.Log.e("InventarioRepository", "Erro ao coletar patrimônio", e)
            Result.failure(e)
        }
    }
    
    suspend fun getColetas(): List<Coleta> = emptyList()
    
    suspend fun getColetasPaginadas(
        page: Int = 0,
        size: Int = 50,
        useCache: Boolean = true
    ): Result<PagedColetasResult> {
        // TODO: Implementar busca de coletas do banco Room
        // Por enquanto retorna lista vazia para não quebrar a compilação
        android.util.Log.w("InventarioRepository", "getColetasPaginadas() não implementado - retornando lista vazia")
        return Result.success(
            PagedColetasResult(emptyList(), 0, 0, 0, 0, false, false)
        )
    }
    
    suspend fun removeColeta(coletaId: Int): Result<Unit> = Result.success(Unit)
    
    suspend fun isPatrimonioColetado(patrimonioId: Long): Boolean = false
    
    suspend fun sincronizarTodosDados(): Int = 0
    
    suspend fun buscarDescricoesNaoColetadas(): List<String> = emptyList()
    
    suspend fun syncData(): Result<Unit> = Result.success(Unit)
    
    // Métodos stub adicionais para compatibilidade
    suspend fun getDashboardStats(): Result<DashboardStats> {
        return try {
            // Verificar cache primeiro
            if (isCacheValid()) {
                android.util.Log.d("InventarioRepository", "✓ Usando estatísticas do cache (${(System.currentTimeMillis() - cacheTimestamp) / 1000}s atrás)")
                return Result.success(cachedStats!!)
            }
            
            android.util.Log.d("InventarioRepository", "Carregando estatísticas do dashboard (otimizado)...")
            
            // Usar endpoint otimizado ao invés de buscar todos os patrimônios
            val response = apiService.getDashboardStats()
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                
                if (apiResponse.success && apiResponse.data != null) {
                    val dto = apiResponse.data
                    
                    val stats = DashboardStats(
                        totalPatrimonios = dto.totalPatrimonios,
                        coletados = dto.patrimoniosColetados,
                        naoColetados = dto.patrimoniosPendentes,
                        percentualColetado = dto.percentualConclusao,
                        coletoresAtivos = dto.coletoresAtivos,
                        divergencias = dto.divergencias,
                        valorTotal = dto.valorTotal
                    )
                    
                    android.util.Log.d("InventarioRepository", "✓ Estatísticas carregadas (otimizado):")
                    android.util.Log.d("InventarioRepository", "  Total: ${stats.totalPatrimonios}")
                    android.util.Log.d("InventarioRepository", "  Coletados: ${stats.coletados}")
                    android.util.Log.d("InventarioRepository", "  Não Coletados: ${stats.naoColetados}")
                    android.util.Log.d("InventarioRepository", "  Percentual: ${String.format("%.2f", stats.percentualColetado)}%")
                    android.util.Log.d("InventarioRepository", "  Coletores Ativos: ${stats.coletoresAtivos}")
                    android.util.Log.d("InventarioRepository", "  Divergências: ${stats.divergencias}")
                    
                    // Atualizar cache
                    cachedStats = stats
                    cacheTimestamp = System.currentTimeMillis()
                    
                    Result.success(stats)
                } else {
                    android.util.Log.w("InventarioRepository", "API retornou success=false ou data=null")
                    
                    // Fallback: calcular localmente se o endpoint falhar
                    android.util.Log.d("InventarioRepository", "Usando fallback: calculando estatísticas localmente...")
                    calcularEstatisticasLocalmente()
                }
            } else {
                android.util.Log.e("InventarioRepository", "Erro HTTP ${response.code()}")
                
                // Fallback: calcular localmente
                android.util.Log.d("InventarioRepository", "Usando fallback: calculando estatísticas localmente...")
                calcularEstatisticasLocalmente()
            }
        } catch (e: Exception) {
            android.util.Log.e("InventarioRepository", "Erro ao carregar estatísticas", e)
            
            // Fallback: tentar calcular localmente
            try {
                android.util.Log.d("InventarioRepository", "Usando fallback: calculando estatísticas localmente...")
                calcularEstatisticasLocalmente()
            } catch (fallbackError: Exception) {
                android.util.Log.e("InventarioRepository", "Erro no fallback", fallbackError)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Fallback: Calcula estatísticas localmente quando o endpoint otimizado falha
     * Usa cache do Room Database para melhor performance
     */
    private suspend fun calcularEstatisticasLocalmente(): Result<DashboardStats> {
        return try {
            // Tentar buscar do banco local primeiro (mais rápido)
            val database = com.inventario.mobile.data.local.database.InventarioDatabase.getDatabase(context)
            val patrimonioDao = database.patrimonioDao()
            
            val total = patrimonioDao.contarTodos()
            val coletados = patrimonioDao.contarColetados()
            val naoColetados = patrimonioDao.contarNaoColetados()
            val percentual = if (total > 0) (coletados.toDouble() / total.toDouble()) * 100.0 else 0.0
            
            val stats = DashboardStats(
                totalPatrimonios = total,
                coletados = coletados,
                naoColetados = naoColetados,
                percentualColetado = percentual
            )
            
            android.util.Log.d("InventarioRepository", "✓ Estatísticas calculadas localmente:")
            android.util.Log.d("InventarioRepository", "  Total: $total")
            android.util.Log.d("InventarioRepository", "  Coletados: $coletados")
            android.util.Log.d("InventarioRepository", "  Não Coletados: $naoColetados")
            
            // Atualizar cache
            cachedStats = stats
            cacheTimestamp = System.currentTimeMillis()
            
            Result.success(stats)
        } catch (e: Exception) {
            android.util.Log.e("InventarioRepository", "Erro ao calcular estatísticas localmente", e)
            Result.failure(e)
        }
    }
    
    suspend fun getPatrimoniosByResponsavel(
        responsavelId: Int,
        page: Int = 0,
        size: Int = 50,
        coletado: Boolean? = null
    ): Result<List<Patrimonio>> {
        return try {
            android.util.Log.d("InventarioRepository", "Buscando patrimônios do responsável $responsavelId (page: $page, size: $size, coletado: $coletado)")
            
            val response = apiService.getPatrimoniosByResponsavel(responsavelId, page, size, coletado)
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                
                if (apiResponse.success && apiResponse.data != null) {
                    val patrimonios = apiResponse.data.map { dto ->
                        Patrimonio(
                            id = dto.id,
                            numeroPatrimonio = dto.codigo,
                            descricao = dto.descricao,
                            marca = dto.marca,
                            modelo = dto.modelo,
                            numeroSerie = dto.numeroSerie,
                            estado = dto.estado,
                            valor = dto.valor,
                            setorId = dto.setorId,
                            setorNome = dto.setorNome,
                            salaId = dto.salaId,
                            salaNome = dto.salaNome,
                            responsavelId = dto.responsavelId,
                            responsavelNome = dto.responsavelNome,
                            coletado = dto.coletado,
                            dataColeta = dto.dataColeta,
                            observacoesColeta = null,
                            observacoes = dto.observacoes
                        )
                    }
                    
                    android.util.Log.d("InventarioRepository", "✓ ${patrimonios.size} patrimônios carregados do responsável $responsavelId")
                    Result.success(patrimonios)
                } else {
                    android.util.Log.w("InventarioRepository", "API retornou success=false ou data=null")
                    Result.success(emptyList())
                }
            } else {
                android.util.Log.e("InventarioRepository", "Erro HTTP ${response.code()}")
                Result.failure(Exception("Erro ao buscar patrimônios: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("InventarioRepository", "Erro ao buscar patrimônios do responsável", e)
            Result.failure(e)
        }
    }
    
    suspend fun getResponsaveis(): Result<List<com.inventario.mobile.data.model.Responsavel>> {
        return try {
            android.util.Log.d("InventarioRepository", "Buscando responsáveis...")
            
            val response = apiService.getResponsaveis()
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                
                if (apiResponse.success && apiResponse.data != null) {
                    val responsaveis = apiResponse.data.map { dto ->
                        com.inventario.mobile.data.model.Responsavel.fromDto(dto)
                    }
                    
                    android.util.Log.d("InventarioRepository", "✓ ${responsaveis.size} responsáveis carregados")
                    Result.success(responsaveis)
                } else {
                    android.util.Log.w("InventarioRepository", "API retornou success=false ou data=null")
                    Result.success(emptyList())
                }
            } else {
                android.util.Log.e("InventarioRepository", "Erro HTTP ${response.code()}")
                Result.failure(Exception("Erro ao buscar responsáveis: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("InventarioRepository", "Erro ao buscar responsáveis", e)
            Result.failure(e)
        }
    }
    
    /**
     * v2.6: Busca coletas pendentes do banco Room
     * Inclui informações de erro de sincronização
     */
    suspend fun getColetasPendentes(): List<Coleta> {
        return try {
            android.util.Log.d("InventarioRepository", "Buscando coletas pendentes do banco local...")
            
            val database = com.inventario.mobile.data.local.database.InventarioDatabase.getDatabase(context)
            val coletaDao = database.coletaDao()
            
            val coletasEntity = coletaDao.buscarPendentes()
            
            val coletas = coletasEntity.map { entity ->
                Coleta(
                    id = entity.id.toInt(),
                    patrimonioId = entity.idPatrimonio,
                    usuarioId = entity.idUsuario,
                    dataColeta = entity.dataColeta.toString(),
                    localizacaoAtual = entity.nomeSala,
                    estadoEncontrado = entity.estadoPatrimonio,
                    observacoes = entity.observacao,
                    latitude = entity.latitude,
                    longitude = entity.longitude,
                    sincronizado = entity.sincronizado,
                    tentativasSincronizacao = entity.tentativasSincronizacao,
                    erroSincronizacao = entity.erroSincronizacao,  // v2.6: Incluir erro
                    status = if (entity.erroSincronizacao != null) "ERRO" else "PENDENTE",
                    numeroPatrimonio = entity.numeroPatrimonio,
                    descricaoPatrimonio = null,  // Pode ser buscado do patrimônio se necessário
                    nomeSala = entity.nomeSala,
                    nomeColetor = entity.nomeUsuario
                )
            }
            
            android.util.Log.d("InventarioRepository", "✓ ${coletas.size} coletas pendentes encontradas")
            
            // Log de coletas com erro
            val coletasComErro = coletas.filter { !it.erroSincronizacao.isNullOrBlank() }
            if (coletasComErro.isNotEmpty()) {
                android.util.Log.w("InventarioRepository", "⚠️ ${coletasComErro.size} coletas com erro de sincronização")
            }
            
            coletas
        } catch (e: Exception) {
            android.util.Log.e("InventarioRepository", "Erro ao buscar coletas pendentes", e)
            emptyList()
        }
    }
    
    suspend fun getLastSyncTime(): Long = 0L
    
    suspend fun sincronizarDados(): Result<Int> = Result.success(0)
    
    /**
     * Busca evolução de coletas por dia (últimos N dias)
     * Usado para gráfico de linhas no dashboard
     */
    suspend fun getColetasEvolucao(dias: Int = 30): Result<List<com.inventario.mobile.data.remote.dto.ColetasPorDiaDto>> {
        return try {
            android.util.Log.d("InventarioRepository", "Buscando evolução de coletas (últimos $dias dias)...")
            
            val response = apiService.getColetasEvolucao(dias = dias)
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                
                if (apiResponse.success && apiResponse.data != null) {
                    // Converter Map<String, Int> para List<ColetasPorDiaDto>
                    val evolucaoMap = apiResponse.data["evolucao"] as? Map<*, *>
                    
                    if (evolucaoMap != null) {
                        val evolucaoList = evolucaoMap.entries.map { entry ->
                            val dataFormatada = entry.key.toString()
                            val quantidade = (entry.value as? Number)?.toInt() ?: 0
                            
                            // Converter data formatada "dd/MM" para "2025-11-dd"
                            val ano = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                            val partes = dataFormatada.split("/")
                            val dia = partes.getOrNull(0)?.padStart(2, '0') ?: "01"
                            val mes = partes.getOrNull(1)?.padStart(2, '0') ?: "01"
                            val dataISO = "$ano-$mes-$dia"
                            
                            com.inventario.mobile.data.remote.dto.ColetasPorDiaDto(
                                data = dataISO,
                                quantidade = quantidade,
                                coletoresAtivos = 0,
                                dataFormatada = dataFormatada
                            )
                        }.sortedBy { it.data }
                        
                        android.util.Log.d("InventarioRepository", "Evolução carregada: ${evolucaoList.size} dias")
                        Result.success(evolucaoList)
                    } else {
                        android.util.Log.w("InventarioRepository", "Dados de evolução não encontrados")
                        Result.success(emptyList())
                    }
                } else {
                    android.util.Log.e("InventarioRepository", "Resposta sem sucesso: ${apiResponse.message}")
                    Result.failure(Exception(apiResponse.message ?: "Erro ao buscar evolução"))
                }
            } else {
                android.util.Log.e("InventarioRepository", "Erro HTTP ${response.code()}")
                Result.failure(Exception("Erro ao buscar evolução: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("InventarioRepository", "Erro ao buscar evolução de coletas", e)
            Result.failure(e)
        }
    }
    
    // Métodos auxiliares
    fun getCurrentUser(): com.inventario.mobile.data.model.Usuario? = null
    
    private suspend fun obterInventarioAtivo(): Result<Int> = Result.success(1)
    
    /**
     * v2.7: Limpa erro de sincronização de uma coleta específica
     * Permite nova tentativa de sincronização
     */
    suspend fun limparErroColeta(coletaId: Long): Result<Unit> {
        return try {
            android.util.Log.d("InventarioRepository", "Limpando erro da coleta $coletaId...")
            
            val database = com.inventario.mobile.data.local.database.InventarioDatabase.getDatabase(context)
            database.coletaDao().limparErroSincronizacao(coletaId)
            
            android.util.Log.d("InventarioRepository", "✓ Erro da coleta $coletaId limpo")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("InventarioRepository", "Erro ao limpar erro da coleta $coletaId", e)
            Result.failure(e)
        }
    }
    
    /**
     * v2.7: Limpa erros de todas as coletas pendentes
     * Permite nova tentativa de sincronização em lote
     */
    suspend fun limparTodosErrosColetas(): Result<Int> {
        return try {
            android.util.Log.d("InventarioRepository", "Limpando erros de todas as coletas pendentes...")
            
            val database = com.inventario.mobile.data.local.database.InventarioDatabase.getDatabase(context)
            val quantidade = database.coletaDao().limparTodosErrosSincronizacao()
            
            android.util.Log.d("InventarioRepository", "✓ Erros limpos de $quantidade coletas")
            Result.success(quantidade)
        } catch (e: Exception) {
            android.util.Log.e("InventarioRepository", "Erro ao limpar erros das coletas", e)
            Result.failure(e)
        }
    }
}

// Data classes para compatibilidade
data class DashboardStats(
    val totalPatrimonios: Int,
    val coletados: Int,
    val naoColetados: Int,
    val percentualColetado: Double,
    val coletoresAtivos: Int = 0,
    val divergencias: Int = 0,
    val valorTotal: Double = 0.0
)

// Data class para resultado paginado
data class PagedColetasResult(
    val coletas: List<Coleta>,
    val page: Int,
    val size: Int,
    val totalElements: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)
