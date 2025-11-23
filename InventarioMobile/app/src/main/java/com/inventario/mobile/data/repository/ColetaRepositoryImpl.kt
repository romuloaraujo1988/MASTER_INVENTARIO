package com.inventario.mobile.data.repository

import com.inventario.mobile.data.local.dao.ColetaDao
import com.inventario.mobile.data.local.dao.PatrimonioDao
import com.inventario.mobile.data.mapper.ColetaMapper
import com.inventario.mobile.data.remote.api.ColetaApi
import com.inventario.mobile.domain.model.Coleta
import com.inventario.mobile.domain.repository.ColetaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

/**
 * Implementação do repositório de Coleta
 * Estratégia: Offline-first com sincronização automática
 * v2.0: Usa PreferencesManager para obter inventário ativo
 * v2.1: Usa NetworkQualityMonitor para decisão inteligente de sync
 */
@javax.inject.Singleton
class ColetaRepositoryImpl @Inject constructor(
    private val coletaDao: ColetaDao,
    private val patrimonioDao: PatrimonioDao,
    private val coletaApi: ColetaApi,
    private val patrimonioApi: com.inventario.mobile.data.remote.api.PatrimonioApi,
    private val mapper: ColetaMapper,
    private val preferencesManager: com.inventario.mobile.utils.PreferencesManager,
    private val networkQualityMonitor: com.inventario.mobile.network.NetworkQualityMonitor,
    private val auditService: com.inventario.mobile.data.audit.AuditService  // v2.2: Auditoria
) : ColetaRepository {
    
    override fun getAllColetas(): Flow<List<Coleta>> {
        return coletaDao.observarPendentes()
            .map { entities -> mapper.toDomainList(entities) }
    }
    
    override suspend fun getColetaById(id: Long): Coleta? {
        // TODO: Implementar busca por ID
        return null
    }
    
    override suspend fun getColetasByPatrimonio(patrimonioId: Long): List<Coleta> {
        // TODO: Implementar quando necessário
        return emptyList()
    }
    
    override suspend fun getColetasByUsuario(usuarioId: Long): List<Coleta> {
        // TODO: Implementar quando necessário
        return emptyList()
    }
    
    override suspend fun getColetasNaoSincronizadas(): List<Coleta> {
        return coletaDao.buscarPendentes().map { mapper.toDomain(it) }
    }
    
    override suspend fun insertColeta(coleta: Coleta): Long {
        val entity = mapper.toEntity(coleta)
        return coletaDao.inserir(entity)
    }
    
    override suspend fun insertColetas(coletas: List<Coleta>) {
        coletas.forEach { insertColeta(it) }
    }
    
    override suspend fun updateColeta(coleta: Coleta) {
        val entity = mapper.toEntity(coleta)
        coletaDao.inserir(entity)
    }
    
    override suspend fun marcarComoSincronizado(id: Long, servidorId: Long) {
        coletaDao.marcarSincronizada(id)
    }
    
    override suspend fun deleteColeta(coleta: Coleta) {
        coletaDao.deletar(coleta.id.toLong())
    }
    
    override suspend fun getColetasNaoSincronizadasCount(): Int {
        return coletaDao.buscarPendentes().size
    }
    
    override suspend fun sincronizarColetas(): Result<Unit> {
        return sincronizarColetasPendentes().let { 
            if (it >= 0) Result.success(Unit) 
            else Result.failure(Exception("Erro na sincronização"))
        }
    }
    
    override suspend fun enviarColetasParaServidor(): Result<Unit> {
        return sincronizarColetas()
    }
    
    // ========== Novos métodos Clean Architecture ==========
    
    override suspend fun registrarColeta(coleta: Coleta): Result<Coleta> {
        return try {
            // ========================================
            // FASE 1: VALIDAÇÃO RIGOROSA
            // ========================================
            
            // 1. Validar dados críticos ANTES de salvar
            com.inventario.mobile.domain.validator.ColetaValidator.validar(coleta).getOrElse { erro ->
                android.util.Log.e("ColetaRepositoryImpl", "❌ Validação falhou: ${erro.message}")
                
                // Registrar falha de validação no log de auditoria
                auditService.registrarValidacao(
                    coletaId = 0,
                    sucesso = false,
                    erro = erro.message
                )
                
                return Result.failure(erro)
            }
            
            android.util.Log.d("ColetaRepositoryImpl", "✓ Validação passou - Dados críticos OK")
            
            // ========================================
            // FASE 2: VERIFICAR DUPLICATA
            // ========================================
            
            // 2. Verificar se patrimônio já foi coletado neste inventário
            val inventarioId = preferencesManager.getInventarioAtivoId() ?: 0
            val coletaExistente = coletaDao.buscarColetaExistente(
                coleta.patrimonioId.toInt(),
                inventarioId
            )
            
            if (coletaExistente != null) {
                val dataFormatada = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                    .format(java.util.Date(coletaExistente.dataColeta))
                
                android.util.Log.w("ColetaRepositoryImpl", "⚠️ Patrimônio já coletado em $dataFormatada")
                
                // Registrar detecção de duplicata
                auditService.registrarDuplicataDetectada(
                    coletaId = 0,
                    numeroPatrimonio = coleta.numeroPatrimonio ?: "",
                    coletaAnteriorId = coletaExistente.id
                )
                
                return Result.failure(Exception(
                    "⚠️ Patrimônio ${coleta.numeroPatrimonio} já foi coletado em $dataFormatada por ${coletaExistente.nomeUsuario}"
                ))
            }
            
            android.util.Log.d("ColetaRepositoryImpl", "✓ Sem duplicata - Patrimônio não foi coletado ainda")
            
            // ========================================
            // FASE 3: PREPARAR DADOS
            // ========================================
            
            // 3. Buscar dados do patrimônio para preencher campos
            val patrimonio = patrimonioDao.buscarPorId(coleta.patrimonioId.toInt())
            
            // 2. Obter número do patrimônio (CRÍTICO: não pode ser vazio!)
            val numeroPatrimonio = coleta.numeroPatrimonio ?: patrimonio?.numero ?: coleta.patrimonioId.toString()
            
            android.util.Log.d("ColetaRepositoryImpl", "Patrimônio ID: ${coleta.patrimonioId}, Número: $numeroPatrimonio, numeroPatrimonio da coleta: ${coleta.numeroPatrimonio}")
            
            // 4. Criar entity com dados completos (mapper já usa PreferencesManager)
            val entity = mapper.toEntity(coleta)
            
            // ========================================
            // FASE 4: SALVAR COM TRANSAÇÃO ATÔMICA
            // ========================================
            
            // 5. Salvar localmente com transação (tudo ou nada)
            val id = try {
                coletaDao.registrarColetaComTransacao(entity)
            } catch (e: Exception) {
                android.util.Log.e("ColetaRepositoryImpl", "❌ Erro na transação atômica", e)
                return Result.failure(Exception("Erro ao salvar coleta: ${e.message}"))
            }
            
            android.util.Log.d("ColetaRepositoryImpl", "✓ Coleta salva com sucesso (ID: $id) - Transação atômica OK")
            
            // Registrar criação da coleta no log de auditoria
            auditService.registrarColetaCriada(
                coletaId = id,
                numeroPatrimonio = numeroPatrimonio,
                sucesso = true
            )
            
            // Registrar validação bem-sucedida
            auditService.registrarValidacao(
                coletaId = id,
                sucesso = true
            )
            
            // ========================================
            // FASE 5: SINCRONIZAÇÃO INTELIGENTE (NÃO-BLOQUEANTE)
            // ========================================
            
            // 6. Decidir se tenta sincronizar baseado na qualidade da rede
            val shouldAttemptSync = networkQualityMonitor.shouldAttemptSync()
            val networkQuality = networkQualityMonitor.networkQuality.value
            
            android.util.Log.d("ColetaRepositoryImpl", 
                "Qualidade da rede: $networkQuality - Tentar sync: $shouldAttemptSync")
            
            if (shouldAttemptSync) {
                // ✅ Sincronização ASSÍNCRONA para não bloquear a UI
                // Lançar em coroutine separada com timeout
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    try {
                        // Timeout de 5 segundos para evitar travamento
                        kotlinx.coroutines.withTimeout(5000L) {
                            // ✅ Obter ID do inventário ativo
                            val inventarioId = preferencesManager.getInventarioAtivoId() ?: 0
                            
                            // Converter para MobileColetaRequest (formato esperado pelo servidor)
                            val request = com.inventario.mobile.data.remote.dto.MobileColetaRequest(
                                numeroPatrimonio = numeroPatrimonio,
                                idInventario = inventarioId,
                                usuarioId = coleta.usuarioId.toInt(),
                                idSala = patrimonio?.idSala,
                                localizacaoEncontrada = coleta.localizacaoAtual,
                                estadoEncontrado = coleta.status ?: "BOM",
                                observacaoColeta = coleta.observacoes,
                                dataColeta = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", java.util.Locale.getDefault())
                                    .format(java.util.Date(coleta.dataColeta)),
                                latitude = coleta.latitude,
                                longitude = coleta.longitude,
                                fotoPatrimonio = null,
                                semEtiqueta = false,
                                descricaoItemSemEtiqueta = null,
                                categoriaItemSemEtiqueta = null,
                                deviceId = android.os.Build.MODEL,
                                appVersion = "1.2",
                                divergencia = false,
                                motivoDivergencia = null
                            )
                            
                            android.util.Log.d("ColetaRepositoryImpl", "🔄 Sincronizando coleta em background (rede: $networkQuality)")
                            
                            val response = coletaApi.registrarColeta(request)
                            
                            android.util.Log.d("ColetaRepositoryImpl", "Resposta do servidor: success=${response.success}, message=${response.message}")
                            
                            if (response.success) {
                                coletaDao.marcarSincronizada(id)
                                networkQualityMonitor.registerSyncSuccess()
                                android.util.Log.d("ColetaRepositoryImpl", "✓ Coleta sincronizada com sucesso em background")
                                
                                // Registrar sincronização bem-sucedida
                                auditService.registrarSincronizacao(
                                    coletaId = id,
                                    servidorId = null
                                )
                            } else {
                                networkQualityMonitor.registerSyncFailure()
                                android.util.Log.w("ColetaRepositoryImpl", "⚠ Servidor retornou success=false: ${response.message}")
                                
                                // Registrar erro de sincronização
                                auditService.registrarErroSincronizacao(
                                    coletaId = id,
                                    erro = response.message ?: "Erro desconhecido",
                                    tentativa = 1
                                )
                            }
                        }
                    } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                        // Timeout: não bloqueia, apenas loga
                        networkQualityMonitor.registerSyncFailure()
                        android.util.Log.w("ColetaRepositoryImpl", "⏱️ Timeout na sincronização (5s) - Coleta ficará pendente")
                        
                        auditService.registrarErroSincronizacao(
                            coletaId = id,
                            erro = "Timeout na sincronização",
                            tentativa = 1
                        )
                    } catch (e: Exception) {
                        // Erro: não bloqueia, apenas loga
                        networkQualityMonitor.registerSyncFailure()
                        android.util.Log.e("ColetaRepositoryImpl", "✗ Erro ao sincronizar coleta em background", e)
                        
                        auditService.registrarErroSincronizacao(
                            coletaId = id,
                            erro = e.message ?: "Erro de conexão",
                            tentativa = 1
                        )
                    }
                }
                
                android.util.Log.d("ColetaRepositoryImpl", "✓ Coleta salva localmente - Sincronização iniciada em background")
            } else {
                // Rede ruim/instável: salvar apenas localmente
                android.util.Log.d("ColetaRepositoryImpl", 
                    "⚠ Rede instável ($networkQuality) - Salvando apenas localmente. Será sincronizado em background.")
            }
            
            Result.success(coleta.copy(id = id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun jaFoiColetado(idPatrimonio: Int): Boolean {
        return try {
            val patrimonio = patrimonioDao.buscarPorId(idPatrimonio)
            patrimonio?.coletado == true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun sincronizarColetasPendentes(): Int {
        return try {
            val coletasPendentes = coletaDao.buscarPendentes()
            
            if (coletasPendentes.isEmpty()) {
                android.util.Log.d("ColetaRepositoryImpl", "Nenhuma coleta pendente para sincronizar")
                return 0
            }
            
            android.util.Log.d("ColetaRepositoryImpl", "Sincronizando ${coletasPendentes.size} coletas pendentes")
            
            // Estratégia: Tentar batch primeiro, se falhar, sincronizar uma por uma
            val sincronizadas = try {
                sincronizarEmLote(coletasPendentes)
            } catch (e: Exception) {
                android.util.Log.w("ColetaRepositoryImpl", "Falha no sync em lote, tentando individual", e)
                sincronizarIndividualmente(coletasPendentes)
            }
            
            android.util.Log.d("ColetaRepositoryImpl", "✓ ${sincronizadas} coletas sincronizadas com sucesso")
            sincronizadas
            
        } catch (e: Exception) {
            android.util.Log.e("ColetaRepositoryImpl", "Erro ao sincronizar coletas pendentes", e)
            -1
        }
    }
    
    /**
     * Sincroniza coletas em lote (mais eficiente)
     */
    private suspend fun sincronizarEmLote(coletasPendentes: List<com.inventario.mobile.data.local.entity.ColetaEntity>): Int {
        // Converter entities para requests
        val requests = coletasPendentes.mapNotNull { entity ->
            try {
                val coleta = mapper.toDomain(entity)
                val patrimonio = patrimonioDao.buscarPorId(coleta.patrimonioId.toInt())
                
                // ✅ Obter ID do inventário ativo
                val inventarioId = preferencesManager.getInventarioAtivoId() ?: 0
                
                com.inventario.mobile.data.remote.dto.MobileColetaRequest(
                    numeroPatrimonio = patrimonio?.numero ?: entity.numeroPatrimonio,
                    idInventario = inventarioId, // ✅ Do PreferencesManager
                    usuarioId = coleta.usuarioId.toInt(),
                    idSala = patrimonio?.idSala,
                    localizacaoEncontrada = coleta.localizacaoAtual,
                    estadoEncontrado = coleta.status ?: "BOM",
                    observacaoColeta = coleta.observacoes,
                    dataColeta = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", java.util.Locale.getDefault())
                        .format(java.util.Date(coleta.dataColeta)),
                    latitude = coleta.latitude,
                    longitude = coleta.longitude,
                    fotoPatrimonio = null,
                    semEtiqueta = false,
                    descricaoItemSemEtiqueta = null,
                    categoriaItemSemEtiqueta = null,
                    deviceId = android.os.Build.MODEL,
                    appVersion = "1.2",
                    divergencia = false,
                    motivoDivergencia = null
                )
            } catch (e: Exception) {
                android.util.Log.e("ColetaRepositoryImpl", "Erro ao converter coleta ${entity.id}", e)
                null
            }
        }
        
        if (requests.isEmpty()) {
            return 0
        }
        
        // Enviar em lote
        val batchRequest = com.inventario.mobile.data.remote.dto.MobileColetaBatchRequest(requests)
        val response = coletaApi.registrarColetasEmLote(batchRequest)
        
        if (response.success) {
            // Marcar todas como sincronizadas
            coletasPendentes.forEach { entity ->
                coletaDao.marcarSincronizada(entity.id)
            }
            
            // Extrair quantidade de sucesso do response
            val resultado = response.data
            val sucesso = (resultado?.get("sucesso") as? Number)?.toInt() ?: coletasPendentes.size
            
            android.util.Log.d("ColetaRepositoryImpl", "Batch sync: ${sucesso} sucesso de ${coletasPendentes.size}")
            return sucesso
        } else {
            throw Exception("Batch sync falhou: ${response.message}")
        }
    }
    
    /**
     * Sincroniza coletas individualmente (fallback)
     */
    private suspend fun sincronizarIndividualmente(coletasPendentes: List<com.inventario.mobile.data.local.entity.ColetaEntity>): Int {
        var sincronizadas = 0
        
        for (entity in coletasPendentes) {
            try {
                val coleta = mapper.toDomain(entity)
                val patrimonio = patrimonioDao.buscarPorId(coleta.patrimonioId.toInt())
                
                // ✅ Obter ID do inventário ativo
                val inventarioId = preferencesManager.getInventarioAtivoId() ?: 0
                
                // Converter para MobileColetaRequest
                val request = com.inventario.mobile.data.remote.dto.MobileColetaRequest(
                    numeroPatrimonio = patrimonio?.numero ?: entity.numeroPatrimonio,
                    idInventario = inventarioId, // ✅ Do PreferencesManager
                    usuarioId = coleta.usuarioId.toInt(),
                    idSala = patrimonio?.idSala,
                    localizacaoEncontrada = coleta.localizacaoAtual,
                    estadoEncontrado = coleta.status ?: "BOM",
                    observacaoColeta = coleta.observacoes,
                    dataColeta = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", java.util.Locale.getDefault())
                        .format(java.util.Date(coleta.dataColeta)),
                    latitude = coleta.latitude,
                    longitude = coleta.longitude,
                    fotoPatrimonio = null,
                    semEtiqueta = false,
                    descricaoItemSemEtiqueta = null,
                    categoriaItemSemEtiqueta = null,
                    deviceId = android.os.Build.MODEL,
                    appVersion = "1.2",
                    divergencia = false,
                    motivoDivergencia = null
                )
                
                val response = coletaApi.registrarColeta(request)
                
                if (response.success) {
                    coletaDao.marcarSincronizada(entity.id)
                    sincronizadas++
                } else {
                    coletaDao.registrarErroSincronizacao(
                        entity.id,
                        response.message ?: "Erro desconhecido"
                    )
                }
            } catch (e: Exception) {
                coletaDao.registrarErroSincronizacao(
                    entity.id,
                    e.message ?: "Erro de conexão"
                )
            }
        }
        
        return sincronizadas
    }
    
    override suspend fun getColetasLocal(): List<Coleta> {
        return try {
            coletaDao.buscarTodas().map { mapper.toDomain(it) }
        } catch (e: Exception) {
            android.util.Log.e("ColetaRepositoryImpl", "Erro ao buscar coletas locais", e)
            emptyList()
        }
    }
    
    /**
     * Atualiza coletas antigas que têm campos vazios
     * Busca os dados do patrimônio e preenche
     */
    suspend fun atualizarColetasAntigas() {
        try {
            val coletas = coletaDao.buscarTodas()
            
            coletas.forEach { coleta ->
                // Se numeroPatrimonio está vazio, atualizar
                if (coleta.numeroPatrimonio.isBlank()) {
                    val patrimonio = patrimonioDao.buscarPorId(coleta.idPatrimonio)
                    
                    if (patrimonio != null) {
                        val coletaAtualizada = coleta.copy(
                            numeroPatrimonio = patrimonio.numero
                        )
                        coletaDao.inserir(coletaAtualizada)
                        
                        android.util.Log.d("ColetaRepositoryImpl", 
                            "Coleta ${coleta.id} atualizada com número ${patrimonio.numero}")
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ColetaRepositoryImpl", "Erro ao atualizar coletas antigas", e)
        }
    }
}
