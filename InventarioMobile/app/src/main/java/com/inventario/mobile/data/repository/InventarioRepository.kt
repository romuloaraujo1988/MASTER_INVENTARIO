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
 * TODO: Substituir por repositórios Clean Architecture específicos
 */
class InventarioRepository(
    private val apiService: ApiService,
    private val localDataManager: LocalDataManager
) {
    
    // Construtor alternativo para aceitar Application
    constructor(application: Application) : this(
        ApiClient.getApiService(application.applicationContext),
        LocalDataManager.getInstance(application.applicationContext)
    )
    
    companion object {
        @Volatile
        private var INSTANCE: InventarioRepository? = null
        
        fun getInstance(context: Context, apiService: ApiService): InventarioRepository {
            return INSTANCE ?: synchronized(this) {
                val localDataManager = LocalDataManager.getInstance(context)
                INSTANCE ?: InventarioRepository(apiService, localDataManager).also { INSTANCE = it }
            }
        }
    }
    
    // Métodos stub - retornam valores padrão ou lançam exceção
    suspend fun getAllPatrimoniosList(): List<Patrimonio> {
        return try {
            android.util.Log.d("InventarioRepository", "═══════════════════════════════════════════")
            android.util.Log.d("InventarioRepository", "BUSCANDO TODOS OS PATRIMÔNIOS")
            android.util.Log.d("InventarioRepository", "═══════════════════════════════════════════")
            
            val response = apiService.getAllPatrimonios(page = 0, size = 10000)
            
            android.util.Log.d("InventarioRepository", "Response code: ${response.code()}")
            android.util.Log.d("InventarioRepository", "Response successful: ${response.isSuccessful}")
            android.util.Log.d("InventarioRepository", "Response raw: ${response.raw()}")
            
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("InventarioRepository", "Error body: $errorBody")
            }
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                android.util.Log.d("InventarioRepository", "API Response success: ${apiResponse.success}")
                
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
                    
                    android.util.Log.d("InventarioRepository", "✓ ${patrimonios.size} patrimônios carregados!")
                    android.util.Log.d("InventarioRepository", "  Coletados: ${patrimonios.count { it.coletado == true }}")
                    android.util.Log.d("InventarioRepository", "  Pendentes: ${patrimonios.count { it.coletado != true }}")
                    
                    patrimonios
                } else {
                    android.util.Log.w("InventarioRepository", "API retornou success=false ou data=null")
                    emptyList()
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("InventarioRepository", "✗ Erro HTTP ${response.code()}: $errorBody")
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("InventarioRepository", "═══════════════════════════════════════════")
            android.util.Log.e("InventarioRepository", "EXCEÇÃO AO BUSCAR PATRIMÔNIOS")
            android.util.Log.e("InventarioRepository", "Tipo: ${e.javaClass.simpleName}")
            android.util.Log.e("InventarioRepository", "Mensagem: ${e.message}")
            android.util.Log.e("InventarioRepository", "Stack trace:", e)
            android.util.Log.e("InventarioRepository", "═══════════════════════════════════════════")
            emptyList()
        }
    }
    
    suspend fun getPatrimoniosColetados(): List<Patrimonio> {
        return getAllPatrimoniosList().filter { it.coletado == true }
    }
    
    suspend fun getPatrimoniosNaoColetados(): List<Patrimonio> {
        return getAllPatrimoniosList().filter { it.coletado != true }
    }
    
    suspend fun findPatrimonioByNumero(numero: String): Result<Patrimonio?> {
        return try {
            android.util.Log.d("InventarioRepository", "═══════════════════════════════════════════")
            android.util.Log.d("InventarioRepository", "BUSCANDO PATRIMÔNIO POR NÚMERO")
            android.util.Log.d("InventarioRepository", "Número: $numero")
            android.util.Log.d("InventarioRepository", "═══════════════════════════════════════════")
            
            val response = apiService.getPatrimonioByNumero(numero)
            
            android.util.Log.d("InventarioRepository", "Response code: ${response.code()}")
            android.util.Log.d("InventarioRepository", "Response successful: ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                android.util.Log.d("InventarioRepository", "API Response success: ${apiResponse.success}")
                
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
                    
                    android.util.Log.d("InventarioRepository", "✓ Patrimônio encontrado!")
                    android.util.Log.d("InventarioRepository", "  ID: ${patrimonio.id}")
                    android.util.Log.d("InventarioRepository", "  Número: ${patrimonio.numeroPatrimonio}")
                    android.util.Log.d("InventarioRepository", "  Descrição: ${patrimonio.descricao}")
                    android.util.Log.d("InventarioRepository", "  Coletado: ${patrimonio.coletado}")
                    
                    Result.success(patrimonio)
                } else {
                    android.util.Log.w("InventarioRepository", "Patrimônio não encontrado ou API retornou success=false")
                    Result.success(null)
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("InventarioRepository", "✗ Erro HTTP ${response.code()}: $errorBody")
                
                if (response.code() == 404) {
                    Result.success(null) // Não encontrado
                } else {
                    Result.failure(Exception("Erro HTTP: ${response.code()} - ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("InventarioRepository", "═══════════════════════════════════════════")
            android.util.Log.e("InventarioRepository", "EXCEÇÃO AO BUSCAR PATRIMÔNIO")
            android.util.Log.e("InventarioRepository", "Tipo: ${e.javaClass.simpleName}")
            android.util.Log.e("InventarioRepository", "Mensagem: ${e.message}")
            android.util.Log.e("InventarioRepository", "Stack trace:", e)
            android.util.Log.e("InventarioRepository", "═══════════════════════════════════════════")
            Result.failure(e)
        }
    }
    
    suspend fun coletarPatrimonio(
        patrimonio: Patrimonio,
        observacoes: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ): Result<Coleta> {
        return Result.failure(Exception("Método não implementado - use ColetaRepository"))
    }
    
    suspend fun coletarPatrimonioComSala(
        patrimonio: Patrimonio,
        salaNome: String,
        estadoEncontrado: String = "BOM",
        observacoes: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ): Result<Coleta> {
        return try {
            android.util.Log.d("InventarioRepository", "═══════════════════════════════════════════")
            android.util.Log.d("InventarioRepository", "REGISTRANDO COLETA")
            android.util.Log.d("InventarioRepository", "Patrimônio: ${patrimonio.numeroPatrimonio}")
            android.util.Log.d("InventarioRepository", "Sala: $salaNome")
            android.util.Log.d("InventarioRepository", "Estado: $estadoEncontrado")
            android.util.Log.d("InventarioRepository", "═══════════════════════════════════════════")
            
            // Obter usuário atual
            val usuario = getCurrentUser()
            if (usuario == null) {
                android.util.Log.e("InventarioRepository", "✗ Usuário não autenticado")
                return Result.failure(Exception("Usuário não autenticado"))
            }
            
            // Obter inventário ativo
            val inventarioResult = obterInventarioAtivo()
            val idInventario = inventarioResult.getOrNull()
            if (idInventario == null) {
                android.util.Log.e("InventarioRepository", "✗ Nenhum inventário ativo encontrado")
                return Result.failure(Exception("Nenhum inventário ativo encontrado"))
            }
            
            android.util.Log.d("InventarioRepository", "Usuário ID: ${usuario.id}")
            android.util.Log.d("InventarioRepository", "Inventário ID: $idInventario")
            
            // Criar request de coleta
            val coletaRequest = com.inventario.mobile.data.remote.dto.MobileColetaRequest(
                numeroPatrimonio = patrimonio.numeroPatrimonio,
                idInventario = idInventario,
                usuarioId = usuario.id.toInt(),
                localizacaoEncontrada = salaNome,
                estadoEncontrado = estadoEncontrado,
                observacaoColeta = observacoes,
                latitude = latitude,
                longitude = longitude
            )
            
            val response = apiService.createColeta(coletaRequest)
            
            android.util.Log.d("InventarioRepository", "Response code: ${response.code()}")
            android.util.Log.d("InventarioRepository", "Response successful: ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                android.util.Log.d("InventarioRepository", "API Response success: ${apiResponse.success}")
                
                if (apiResponse.success && apiResponse.data != null) {
                    val dto = apiResponse.data
                    val coleta = Coleta(
                        id = dto.id ?: 0,
                        patrimonioId = dto.patrimonioId ?: patrimonio.id.toInt(),
                        numeroPatrimonio = dto.numeroPatrimonio ?: patrimonio.numeroPatrimonio,
                        descricaoPatrimonio = dto.descricaoPatrimonio ?: patrimonio.descricao,
                        usuarioId = dto.usuarioId ?: 0,
                        nomeColetor = dto.nomeColetor ?: "",
                        dataColeta = dto.dataColeta ?: "",
                        localizacaoAtual = dto.localizacaoEncontrada ?: salaNome,
                        estadoEncontrado = dto.estadoEncontrado ?: estadoEncontrado,
                        observacoes = dto.observacaoColeta ?: observacoes,
                        latitude = dto.latitude ?: latitude,
                        longitude = dto.longitude ?: longitude,
                        fotoPath = dto.fotoPath,
                        sincronizado = true,
                        nomeSala = dto.nomeSala ?: salaNome
                    )
                    
                    android.util.Log.d("InventarioRepository", "✓ Coleta registrada com sucesso!")
                    android.util.Log.d("InventarioRepository", "  ID: ${coleta.id}")
                    android.util.Log.d("InventarioRepository", "  Patrimônio: ${coleta.numeroPatrimonio}")
                    android.util.Log.d("InventarioRepository", "  Sala: ${coleta.localizacaoAtual}")
                    
                    Result.success(coleta)
                } else {
                    val errorMsg = apiResponse.message ?: "Erro desconhecido ao registrar coleta"
                    android.util.Log.e("InventarioRepository", "✗ Erro: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("InventarioRepository", "✗ Erro HTTP ${response.code()}: $errorBody")
                Result.failure(Exception("Erro HTTP: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("InventarioRepository", "═══════════════════════════════════════════")
            android.util.Log.e("InventarioRepository", "EXCEÇÃO AO REGISTRAR COLETA")
            android.util.Log.e("InventarioRepository", "Tipo: ${e.javaClass.simpleName}")
            android.util.Log.e("InventarioRepository", "Mensagem: ${e.message}")
            android.util.Log.e("InventarioRepository", "Stack trace:", e)
            android.util.Log.e("InventarioRepository", "═══════════════════════════════════════════")
            Result.failure(e)
        }
    }
    
    suspend fun getColetas(): List<Coleta> {
        return try {
            android.util.Log.d("InventarioRepository", "═══════════════════════════════════════════")
            android.util.Log.d("InventarioRepository", "BUSCANDO COLETAS")
            android.util.Log.d("InventarioRepository", "═══════════════════════════════════════════")
            
            val response = apiService.getColetas()
            
            android.util.Log.d("InventarioRepository", "Response code: ${response.code()}")
            android.util.Log.d("InventarioRepository", "Response successful: ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                android.util.Log.d("InventarioRepository", "API Response success: ${apiResponse.success}")
                
                if (apiResponse.success && apiResponse.data != null) {
                    val coletas = apiResponse.data.map { dto ->
                        Coleta(
                            id = dto.id,
                            patrimonioId = dto.patrimonioId ?: 0,
                            numeroPatrimonio = dto.numeroPatrimonio,
                            descricaoPatrimonio = dto.descricaoPatrimonio,
                            usuarioId = dto.usuarioId ?: 0,
                            nomeColetor = dto.nomeColetor,
                            dataColeta = dto.dataColeta ?: "",
                            localizacaoAtual = dto.localizacaoEncontrada,
                            estadoEncontrado = dto.estadoEncontrado,
                            observacoes = dto.observacaoColeta,
                            latitude = dto.latitude,
                            longitude = dto.longitude,
                            fotoPath = dto.fotoPath,
                            sincronizado = dto.sincronizado ?: true, // Dados do servidor já estão sincronizados
                            nomeSala = dto.nomeSala
                        )
                    }
                    
                    android.util.Log.d("InventarioRepository", "✓ ${coletas.size} coletas carregadas!")
                    coletas.take(5).forEachIndexed { index, coleta ->
                        android.util.Log.d("InventarioRepository", "  [$index] Patrimônio: ${coleta.numeroPatrimonio}, Sala: ${coleta.localizacaoAtual}")
                    }
                    
                    coletas
                } else {
                    android.util.Log.w("InventarioRepository", "API retornou success=false ou data=null")
                    emptyList()
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("InventarioRepository", "✗ Erro HTTP ${response.code()}: $errorBody")
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("InventarioRepository", "═══════════════════════════════════════════")
            android.util.Log.e("InventarioRepository", "EXCEÇÃO AO BUSCAR COLETAS")
            android.util.Log.e("InventarioRepository", "Tipo: ${e.javaClass.simpleName}")
            android.util.Log.e("InventarioRepository", "Mensagem: ${e.message}")
            android.util.Log.e("InventarioRepository", "Stack trace:", e)
            android.util.Log.e("InventarioRepository", "═══════════════════════════════════════════")
            emptyList()
        }
    }
    
    suspend fun getColetasPaginadas(page: Int = 0, size: Int = 50, useCache: Boolean = true): Result<PagedColetasResult> {
        return try {
            android.util.Log.d("InventarioRepository", "═══════════════════════════════════════════")
            android.util.Log.d("InventarioRepository", "BUSCANDO COLETAS PAGINADAS")
            android.util.Log.d("InventarioRepository", "Page: $page, Size: $size")
            android.util.Log.d("InventarioRepository", "═══════════════════════════════════════════")
            
            val response = apiService.getColetasPaginadas(page, size)
            
            android.util.Log.d("InventarioRepository", "Response code: ${response.code()}")
            android.util.Log.d("InventarioRepository", "Response successful: ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                android.util.Log.d("InventarioRepository", "API Response success: ${apiResponse.success}")
                
                if (apiResponse.success && apiResponse.data != null) {
                    val pagedData = apiResponse.data
                    val coletas = pagedData.content.map { dto ->
                        Coleta(
                            id = dto.id ?: 0,
                            patrimonioId = dto.patrimonioId ?: 0,
                            numeroPatrimonio = dto.numeroPatrimonio ?: "",
                            descricaoPatrimonio = dto.descricaoPatrimonio,
                            usuarioId = dto.usuarioId ?: 0,
                            nomeColetor = dto.nomeColetor,
                            dataColeta = dto.dataColeta ?: "",
                            localizacaoAtual = dto.localizacaoEncontrada,
                            estadoEncontrado = dto.estadoEncontrado,
                            observacoes = dto.observacaoColeta,
                            latitude = dto.latitude,
                            longitude = dto.longitude,
                            fotoPath = dto.fotoPath,
                            sincronizado = dto.sincronizado ?: true,
                            nomeSala = dto.nomeSala
                        )
                    }
                    
                    val result = PagedColetasResult(
                        coletas = coletas,
                        page = pagedData.page,
                        size = pagedData.size,
                        totalElements = pagedData.totalElements,
                        totalPages = pagedData.totalPages,
                        hasNext = !pagedData.last,
                        hasPrevious = !pagedData.first
                    )
                    
                    android.util.Log.d("InventarioRepository", "✓ ${coletas.size} coletas carregadas!")
                    android.util.Log.d("InventarioRepository", "  Página: ${result.page + 1}/${result.totalPages}")
                    android.util.Log.d("InventarioRepository", "  Total: ${result.totalElements}")
                    
                    Result.success(result)
                } else {
                    android.util.Log.w("InventarioRepository", "API retornou success=false ou data=null")
                    Result.success(PagedColetasResult(emptyList(), 0, 0, 0, 0, false, false))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("InventarioRepository", "✗ Erro HTTP ${response.code()}: $errorBody")
                Result.failure(Exception("Erro HTTP: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("InventarioRepository", "═══════════════════════════════════════════")
            android.util.Log.e("InventarioRepository", "EXCEÇÃO AO BUSCAR COLETAS PAGINADAS")
            android.util.Log.e("InventarioRepository", "Tipo: ${e.javaClass.simpleName}")
            android.util.Log.e("InventarioRepository", "Mensagem: ${e.message}")
            android.util.Log.e("InventarioRepository", "Stack trace:", e)
            android.util.Log.e("InventarioRepository", "═══════════════════════════════════════════")
            Result.failure(e)
        }
    }
    
    suspend fun removeColeta(coletaId: Int): Result<Unit> {
        return Result.success(Unit)
    }
    
    suspend fun isPatrimonioColetado(patrimonioId: Long): Boolean = false
    
    suspend fun sincronizarTodosDados(): Int = 0
    
    suspend fun getCurrentUser(): com.inventario.mobile.data.model.Usuario? {
        return try {
            android.util.Log.d("InventarioRepository", "Buscando usuário atual do LocalDataManager")
            val usuario = localDataManager.getCurrentUser()
            
            if (usuario != null) {
                android.util.Log.d("InventarioRepository", "✓ Usuário encontrado: ${usuario.nome} (ID: ${usuario.id})")
            } else {
                android.util.Log.w("InventarioRepository", "✗ Nenhum usuário autenticado encontrado")
            }
            
            usuario
        } catch (e: Exception) {
            android.util.Log.e("InventarioRepository", "Erro ao buscar usuário atual", e)
            null
        }
    }
    
    suspend fun obterInventarioAtivo(): Result<Int> {
        return try {
            android.util.Log.d("InventarioRepository", "═══════════════════════════════════════════")
            android.util.Log.d("InventarioRepository", "BUSCANDO INVENTÁRIO ATIVO")
            android.util.Log.d("InventarioRepository", "═══════════════════════════════════════════")
            
            val response = apiService.obterInventarioAtivo()
            
            android.util.Log.d("InventarioRepository", "Response code: ${response.code()}")
            android.util.Log.d("InventarioRepository", "Response successful: ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                android.util.Log.d("InventarioRepository", "API Response success: ${apiResponse.success}")
                android.util.Log.d("InventarioRepository", "API Response data: ${apiResponse.data}")
                
                if (apiResponse.success && apiResponse.data != null) {
                    // Verificar se existe inventário ativo
                    val existeInventarioAtivo = apiResponse.data["existeInventarioAtivo"] as? Boolean
                    android.util.Log.d("InventarioRepository", "Existe inventário ativo: $existeInventarioAtivo")
                    
                    if (existeInventarioAtivo == true) {
                        // Buscar dados do inventário ativo
                        val inventarioAtivo = apiResponse.data["inventarioAtivo"] as? Map<*, *>
                        android.util.Log.d("InventarioRepository", "Inventário ativo data: $inventarioAtivo")
                        
                        if (inventarioAtivo != null) {
                            val inventarioId = (inventarioAtivo["id"] as? Number)?.toInt()
                            
                            if (inventarioId != null) {
                                android.util.Log.d("InventarioRepository", "✓ Inventário ativo encontrado!")
                                android.util.Log.d("InventarioRepository", "  ID: $inventarioId")
                                android.util.Log.d("InventarioRepository", "  Nome: ${inventarioAtivo["nome"]}")
                                android.util.Log.d("InventarioRepository", "  Status: ${inventarioAtivo["status"]}")
                                Result.success(inventarioId)
                            } else {
                                android.util.Log.w("InventarioRepository", "✗ ID do inventário não encontrado")
                                Result.failure(Exception("ID do inventário não encontrado"))
                            }
                        } else {
                            android.util.Log.w("InventarioRepository", "✗ Dados do inventário ativo são null")
                            Result.failure(Exception("Dados do inventário ativo não encontrados"))
                        }
                    } else {
                        android.util.Log.w("InventarioRepository", "✗ Nenhum inventário ativo no sistema")
                        Result.failure(Exception("Nenhum inventário ativo encontrado"))
                    }
                } else {
                    android.util.Log.w("InventarioRepository", "✗ API retornou success=false ou data=null")
                    Result.failure(Exception("Erro na resposta da API"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("InventarioRepository", "✗ Erro HTTP ${response.code()}: $errorBody")
                Result.failure(Exception("Erro HTTP: ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("InventarioRepository", "═══════════════════════════════════════════")
            android.util.Log.e("InventarioRepository", "EXCEÇÃO AO BUSCAR INVENTÁRIO ATIVO")
            android.util.Log.e("InventarioRepository", "Tipo: ${e.javaClass.simpleName}")
            android.util.Log.e("InventarioRepository", "Mensagem: ${e.message}")
            android.util.Log.e("InventarioRepository", "Stack trace:", e)
            android.util.Log.e("InventarioRepository", "═══════════════════════════════════════════")
            Result.failure(e)
        }
    }
    
    suspend fun getDashboardStats(): com.inventario.mobile.presentation.dashboard.DashboardStats {
        return try {
            android.util.Log.d("InventarioRepository", "═══════════════════════════════════════════")
            android.util.Log.d("InventarioRepository", "BUSCANDO ESTATÍSTICAS DO DASHBOARD")
            android.util.Log.d("InventarioRepository", "═══════════════════════════════════════════")
            
            val response = apiService.getDashboardStats()
            
            android.util.Log.d("InventarioRepository", "Response code: ${response.code()}")
            android.util.Log.d("InventarioRepository", "Response successful: ${response.isSuccessful}")
            android.util.Log.d("InventarioRepository", "Response headers: ${response.headers()}")
            android.util.Log.d("InventarioRepository", "Response body: ${response.body()}")
            
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("InventarioRepository", "Error body: $errorBody")
            }
            
            if (response.isSuccessful && response.body()?.success == true) {
                val dto = response.body()?.data
                android.util.Log.d("InventarioRepository", "DTO recebido: $dto")
                
                if (dto != null) {
                    val stats = com.inventario.mobile.presentation.dashboard.DashboardStats(
                        patrimoniosColetados = dto.patrimoniosColetados,
                        patrimoniosPendentes = dto.patrimoniosPendentes,
                        divergencias = dto.divergencias,
                        coletoresAtivos = dto.coletoresAtivos,
                        totalPatrimonios = dto.totalPatrimonios,
                        percentualConcluido = dto.percentualConclusao.toFloat(),
                        percentualConclusao = dto.percentualConclusao.toFloat(),
                        valorTotal = dto.valorTotal
                    )
                    android.util.Log.d("InventarioRepository", "Stats convertido: $stats")
                    android.util.Log.d("InventarioRepository", "✓ Estatísticas carregadas com sucesso!")
                    stats
                } else {
                    android.util.Log.w("InventarioRepository", "DTO é null, retornando valores zerados")
                    com.inventario.mobile.presentation.dashboard.DashboardStats()
                }
            } else {
                android.util.Log.w("InventarioRepository", "Response não foi bem-sucedida ou success=false")
                android.util.Log.w("InventarioRepository", "Response message: ${response.message()}")
                android.util.Log.w("InventarioRepository", "Response errorBody: ${response.errorBody()?.string()}")
                com.inventario.mobile.presentation.dashboard.DashboardStats()
            }
        } catch (e: Exception) {
            android.util.Log.e("InventarioRepository", "═══════════════════════════════════════════")
            android.util.Log.e("InventarioRepository", "ERRO AO BUSCAR ESTATÍSTICAS")
            android.util.Log.e("InventarioRepository", "Tipo: ${e.javaClass.simpleName}")
            android.util.Log.e("InventarioRepository", "Mensagem: ${e.message}")
            android.util.Log.e("InventarioRepository", "Stack trace:", e)
            android.util.Log.e("InventarioRepository", "═══════════════════════════════════════════")
            com.inventario.mobile.presentation.dashboard.DashboardStats()
        }
    }
    
    suspend fun syncData(): Result<Unit> {
        return Result.failure(Exception("Método não implementado - use SincronizacaoRepository"))
    }
    
    suspend fun getColetasPendentes(): List<Coleta> = emptyList()
    
    suspend fun getLastSyncTime(): String? = null
    
    suspend fun sincronizarDados(): Int = 0
    
    suspend fun updatePatrimonio(patrimonio: Patrimonio): Result<Unit> {
        return Result.success(Unit)
    }
    
    /**
     * Busca patrimônios por responsável
     */
    suspend fun getPatrimoniosByResponsavel(
        responsavelId: Int,
        page: Int = 0,
        size: Int = 20,
        coletado: Boolean? = null
    ): Result<List<Patrimonio>> {
        return try {
            android.util.Log.d("InventarioRepository", "═══════════════════════════════════════════")
            android.util.Log.d("InventarioRepository", "BUSCANDO PATRIMÔNIOS POR RESPONSÁVEL")
            android.util.Log.d("InventarioRepository", "ResponsavelId: $responsavelId, Page: $page, Size: $size, Coletado: $coletado")
            android.util.Log.d("InventarioRepository", "═══════════════════════════════════════════")
            
            val response = apiService.getPatrimoniosByResponsavel(responsavelId, page, size, coletado)
            
            android.util.Log.d("InventarioRepository", "Response code: ${response.code()}")
            android.util.Log.d("InventarioRepository", "Response successful: ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                android.util.Log.d("InventarioRepository", "API Response success: ${apiResponse.success}")
                android.util.Log.d("InventarioRepository", "API Response message: ${apiResponse.message}")
                
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
                    
                    android.util.Log.d("InventarioRepository", "✓ ${patrimonios.size} patrimônios carregados!")
                    patrimonios.take(5).forEachIndexed { index, pat ->
                        android.util.Log.d("InventarioRepository", "  [$index] ${pat.numeroPatrimonio} - ${pat.descricao}")
                    }
                    
                    Result.success(patrimonios)
                } else {
                    val errorMsg = apiResponse.message ?: "Erro desconhecido ao buscar patrimônios"
                    android.util.Log.e("InventarioRepository", "✗ Erro: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("InventarioRepository", "✗ Erro HTTP ${response.code()}: $errorBody")
                Result.failure(Exception("Erro HTTP: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("InventarioRepository", "═══════════════════════════════════════════")
            android.util.Log.e("InventarioRepository", "EXCEÇÃO AO BUSCAR PATRIMÔNIOS")
            android.util.Log.e("InventarioRepository", "Tipo: ${e.javaClass.simpleName}")
            android.util.Log.e("InventarioRepository", "Mensagem: ${e.message}")
            android.util.Log.e("InventarioRepository", "Stack trace:", e)
            android.util.Log.e("InventarioRepository", "═══════════════════════════════════════════")
            Result.failure(e)
        }
    }
    
    /**
     * Busca todos os responsáveis ativos
     */
    suspend fun getResponsaveis(): Result<List<com.inventario.mobile.data.model.Responsavel>> {
        return try {
            android.util.Log.d("InventarioRepository", "═══════════════════════════════════════════")
            android.util.Log.d("InventarioRepository", "BUSCANDO RESPONSÁVEIS")
            android.util.Log.d("InventarioRepository", "═══════════════════════════════════════════")
            
            val response = apiService.getResponsaveis()
            
            android.util.Log.d("InventarioRepository", "Response code: ${response.code()}")
            android.util.Log.d("InventarioRepository", "Response successful: ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                android.util.Log.d("InventarioRepository", "API Response success: ${apiResponse.success}")
                android.util.Log.d("InventarioRepository", "API Response message: ${apiResponse.message}")
                
                if (apiResponse.success && apiResponse.data != null) {
                    val responsaveis = apiResponse.data.map { dto ->
                        com.inventario.mobile.data.model.Responsavel.fromDto(dto)
                    }
                    
                    android.util.Log.d("InventarioRepository", "✓ ${responsaveis.size} responsáveis carregados com sucesso!")
                    responsaveis.forEachIndexed { index, resp ->
                        android.util.Log.d("InventarioRepository", "  [$index] ID: ${resp.id}, Nome: ${resp.nome}")
                    }
                    
                    Result.success(responsaveis)
                } else {
                    val errorMsg = apiResponse.message ?: "Erro desconhecido ao buscar responsáveis"
                    android.util.Log.e("InventarioRepository", "✗ Erro: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("InventarioRepository", "✗ Erro HTTP ${response.code()}: $errorBody")
                Result.failure(Exception("Erro HTTP: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("InventarioRepository", "═══════════════════════════════════════════")
            android.util.Log.e("InventarioRepository", "EXCEÇÃO AO BUSCAR RESPONSÁVEIS")
            android.util.Log.e("InventarioRepository", "Tipo: ${e.javaClass.simpleName}")
            android.util.Log.e("InventarioRepository", "Mensagem: ${e.message}")
            android.util.Log.e("InventarioRepository", "Stack trace:", e)
            android.util.Log.e("InventarioRepository", "═══════════════════════════════════════════")
            Result.failure(e)
        }
    }
}

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
