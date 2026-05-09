package com.inventario.mobile.domain.usecase

import android.util.Log
import com.inventario.mobile.data.local.dao.ColetaDao
import com.inventario.mobile.data.model.Coleta
import com.inventario.mobile.data.remote.api.ApiService
import com.inventario.mobile.util.NetworkChecker
import com.inventario.mobile.utils.PreferencesManager
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
    private val networkChecker: NetworkChecker,
    private val preferencesManager: PreferencesManager
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
            val idInventarioAtivo = preferencesManager.getInventarioAtivoId()
            Log.d(TAG, "Buscando coletas do servidor (inventárioId=$idInventarioAtivo)...")
            val response = apiService.buscarTodasColetasSemPaginacao(idInventarioAtivo)
            
            if (response.isSuccessful && response.body() != null) {
                val coletasAllResponse = response.body()!!
                
                if (coletasAllResponse.success && coletasAllResponse.data != null) {
                    val pagedData = coletasAllResponse.data
                    Log.d(TAG, "Resposta do servidor: ${pagedData.content.size} coletas de ${pagedData.totalElements} total")
                    
                    // Coletas do servidor (sincronizadas)
                    val coletasServidor = pagedData.content.map { dto ->
                        Log.d(TAG, "═══════════════════════════════════════")
                        Log.d(TAG, "Mapeando DTO: id=${dto.id}")
                        Log.d(TAG, "  numeroPatrimonio='${dto.numeroPatrimonio}'")
                        Log.d(TAG, "  localizacaoEncontrada='${dto.localizacaoEncontrada}'")
                        Log.d(TAG, "  nomeSala='${dto.nomeSala}'")
                        Log.d(TAG, "  localizacaoAtual='${dto.localizacaoAtual}'")
                        Log.d(TAG, "═══════════════════════════════════════")
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
                    
                    // Buscar coletas pendentes locais APENAS do inventário ativo
                    val idInventarioAtivo = preferencesManager.getInventarioAtivoId() ?: 0
                    val coletasPendentesLocais = try {
                        val entities = if (idInventarioAtivo > 0) {
                            coletaDao.buscarPendentes(idInventarioAtivo)
                        } else {
                            coletaDao.buscarPendentes()
                        }
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
                                // Para pendentes locais, a localização "encontrada" é a mesma
                                // sala gravada no momento da coleta (não há dado separado na entity).
                                localizacaoEncontrada = entity.nomeSala,
                                observacoes = entity.observacao,
                                sincronizado = false,  // ✓ Pendentes
                                // Preservar o estado de conservação selecionado na coleta
                                estadoEncontrado = entity.estadoPatrimonio,
                                // Preservar campos de item sem etiqueta para que o filtro
                                // "Sem Etiqueta" funcione offline. (Bug corrigido em v2.20.1)
                                semEtiqueta = entity.semEtiqueta,
                                descricaoItemSemEtiqueta = entity.descricaoItemSemEtiqueta,
                                categoriaItemSemEtiqueta = entity.categoriaItemSemEtiqueta,
                                // Campo de erro de sincronização (v2.6) — útil no menu de reenvio
                                erroSincronizacao = entity.erroSincronizacao,
                                tentativasSincronizacao = entity.tentativasSincronizacao
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
            val idInventarioAtivo = preferencesManager.getInventarioAtivoId() ?: 0
            val entities = if (idInventarioAtivo > 0) {
                Log.d(TAG, "Filtrando por inventário ativo: $idInventarioAtivo")
                coletaDao.buscarTodas(idInventarioAtivo)
            } else {
                Log.w(TAG, "Sem inventário ativo, buscando todas as coletas")
                coletaDao.buscarTodas()
            }
            
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
                    // Offline: sem dado separado de localização encontrada, usamos a
                    // própria sala gravada na coleta (mesmo tratamento que o servidor
                    // aplica quando não há divergência).
                    localizacaoEncontrada = entity.nomeSala,
                    observacoes = entity.observacao,
                    sincronizado = entity.sincronizado,
                    // Campos preservados do banco local para filtros funcionarem offline
                    // (Bug corrigido em v2.20.1: antes esses campos eram descartados,
                    //  fazendo o chip "Sem Etiqueta" ficar sempre vazio offline e o
                    //  estado de conservação não aparecer no histórico local.)
                    estadoEncontrado = entity.estadoPatrimonio,
                    semEtiqueta = entity.semEtiqueta,
                    descricaoItemSemEtiqueta = entity.descricaoItemSemEtiqueta,
                    categoriaItemSemEtiqueta = entity.categoriaItemSemEtiqueta,
                    erroSincronizacao = entity.erroSincronizacao,
                    tentativasSincronizacao = entity.tentativasSincronizacao
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
