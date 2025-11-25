package com.inventario.mobile.domain.usecase

import android.util.Log
import com.inventario.mobile.data.local.dao.ColetaDao
import com.inventario.mobile.data.model.Coleta
import com.inventario.mobile.data.remote.api.ApiService
import com.inventario.mobile.util.NetworkChecker
import javax.inject.Inject

/**
 * Use Case: Buscar coletas com fallback automático
 * 
 * Estratégia Offline-First:
 * 1. Se online: tenta servidor, fallback para local se falhar
 * 2. Se offline: busca do local diretamente
 * 
 * Responsabilidade:
 * - Garantir que o app sempre funcione
 * - Priorizar dados do servidor quando disponível
 * - Fallback transparente para dados locais
 */
class BuscarColetasComFallbackUseCase @Inject constructor(
    private val apiService: ApiService,
    private val coletaDao: ColetaDao,
    private val networkChecker: NetworkChecker
) {
    companion object {
        private const val TAG = "BuscarColetasFallback"
    }
    
    /**
     * Busca coletas com fallback automático
     * 
     * @return Result com lista de coletas e fonte dos dados
     */
    suspend operator fun invoke(): Result<ColetasResult> {
        return try {
            if (networkChecker.isOnline()) {
                Log.d(TAG, "📶 Online - tentando buscar do servidor...")
                buscarDoServidorComFallback()
            } else {
                Log.d(TAG, "📵 Offline - buscando do banco local...")
                buscarDoLocal()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro inesperado", e)
            // Último recurso: tentar local
            buscarDoLocal()
        }
    }
    
    /**
     * Tenta buscar do servidor, com fallback para local
     * IMPORTANTE: Retorna coletas do servidor + coletas pendentes locais
     */
    private suspend fun buscarDoServidorComFallback(): Result<ColetasResult> {
        return try {
            Log.d(TAG, "Buscando coletas do servidor...")
            val response = apiService.buscarTodasColetasSemPaginacao()
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                
                if (apiResponse.success && apiResponse.data != null) {
                    // Coletas do servidor (sincronizadas)
                    val coletasServidor = apiResponse.data.map { dto ->
                        Log.d(TAG, "Mapeando DTO: id=${dto.id}, localizacaoEncontrada='${dto.localizacaoEncontrada}', nomeSala='${dto.nomeSala}'")
                        Coleta(
                            id = dto.id?.toInt(),
                            patrimonioId = dto.patrimonioId,
                            numeroPatrimonio = dto.numeroPatrimonio,
                            descricaoPatrimonio = dto.descricaoPatrimonio,
                            usuarioId = dto.usuarioId,
                            nomeColetor = dto.nomeColetor,
                            dataColeta = dto.dataColeta ?: "",
                            nomeSala = dto.nomeSala,  // Localização ORIGINAL do patrimônio
                            localizacaoAtual = dto.nomeSala,  // Localização ORIGINAL do patrimônio
                            localizacaoEncontrada = dto.localizacaoEncontrada,  // Onde foi ENCONTRADO durante a coleta
                            observacoes = dto.observacoes,
                            sincronizado = true,
                            estadoEncontrado = dto.estadoEncontrado,
                            status = dto.statusColeta
                        )
                    }
                    
                    Log.d(TAG, "✓ ${coletasServidor.size} coletas sincronizadas do servidor")
                    
                    // Buscar coletas pendentes locais (não sincronizadas)
                    val coletasPendentesLocais = try {
                        val entities = coletaDao.buscarPendentes()
                        entities.map { entity ->
                            Coleta(
                                id = entity.id.toInt(),
                                patrimonioId = entity.idPatrimonio,
                                numeroPatrimonio = entity.numeroPatrimonio,
                                descricaoPatrimonio = null,
                                usuarioId = entity.idUsuario,
                                nomeColetor = entity.nomeUsuario,
                                dataColeta = entity.dataColeta.toString(),
                                nomeSala = entity.nomeSala,
                                localizacaoAtual = entity.nomeSala,
                                observacoes = entity.observacao,
                                sincronizado = false  // ✓ Pendentes
                            )
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Erro ao buscar pendentes locais: ${e.message}")
                        emptyList()
                    }
                    
                    Log.d(TAG, "✓ ${coletasPendentesLocais.size} coletas pendentes locais")
                    
                    // Combinar: servidor + pendentes locais
                    val todasColetas = coletasServidor + coletasPendentesLocais
                    
                    Log.d(TAG, "✓ Total: ${todasColetas.size} coletas (${coletasServidor.size} sincronizadas + ${coletasPendentesLocais.size} pendentes)")
                    
                    val result = ColetasResult(
                        coletas = todasColetas,
                        fonte = FonteDados.SERVIDOR,
                        timestamp = System.currentTimeMillis()
                    )
                    
                    return Result.success(result)
                }
            }
            
            // Se chegou aqui, servidor falhou
            Log.w(TAG, "⚠ Servidor retornou erro, usando fallback local")
            buscarDoLocal()
            
        } catch (e: Exception) {
            Log.w(TAG, "⚠ Erro ao buscar do servidor: ${e.message}, usando fallback local")
            buscarDoLocal()
        }
    }
    
    /**
     * Busca coletas do banco local
     */
    private suspend fun buscarDoLocal(): Result<ColetasResult> {
        return try {
            Log.d(TAG, "Buscando coletas do banco local...")
            val entities = coletaDao.buscarTodas()
            
            // Converter Entity para data.model.Coleta
            val coletas = entities.map { entity ->
                Coleta(
                    id = entity.id.toInt(),
                    patrimonioId = entity.idPatrimonio,
                    numeroPatrimonio = entity.numeroPatrimonio,
                    descricaoPatrimonio = null,
                    usuarioId = entity.idUsuario,
                    nomeColetor = entity.nomeUsuario,
                    dataColeta = entity.dataColeta.toString(),
                    nomeSala = entity.nomeSala,
                    localizacaoAtual = entity.nomeSala,
                    observacoes = entity.observacao,
                    sincronizado = entity.sincronizado
                )
            }
            
            Log.d(TAG, "✓ ${coletas.size} coletas carregadas do banco local")
            
            val result = ColetasResult(
                coletas = coletas,
                fonte = FonteDados.LOCAL,
                timestamp = System.currentTimeMillis()
            )
            
            Result.success(result)
            
        } catch (e: Exception) {
            Log.e(TAG, "✗ Erro ao buscar do banco local", e)
            Result.failure(e)
        }
    }
}

/**
 * Resultado da busca de coletas
 */
data class ColetasResult(
    val coletas: List<Coleta>,
    val fonte: FonteDados,
    val timestamp: Long
) {
    val isFromServer: Boolean get() = fonte == FonteDados.SERVIDOR
    val isFromLocal: Boolean get() = fonte == FonteDados.LOCAL
}

/**
 * Fonte dos dados
 */
enum class FonteDados {
    SERVIDOR,
    LOCAL
}
