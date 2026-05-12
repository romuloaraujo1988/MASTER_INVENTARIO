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
        return try {
            val entity = coletaDao.buscarPorId(id)
            entity?.let { mapper.toDomain(it) }
        } catch (e: Exception) {
            android.util.Log.e("ColetaRepositoryImpl", "Erro ao buscar coleta por ID $id", e)
            null
        }
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
            // EXCEÇÃO: Coleta por descrição (patrimonioId = 0) permite múltiplas coletas
            val inventarioId = preferencesManager.getInventarioAtivoId() ?: 0
            val isColetaPorDescricao = coleta.patrimonioId <= 0 || !coleta.descricaoPatrimonio.isNullOrBlank()
            
            if (isColetaPorDescricao) {
                android.util.Log.d("ColetaRepositoryImpl", "✓ Coleta por descrição - Validação de duplicata IGNORADA")
                android.util.Log.d("ColetaRepositoryImpl", "  Descrição: ${coleta.descricaoPatrimonio}")
            } else {
                // Verificar duplicata apenas para coletas normais (com patrimônio específico)
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
            }
            
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
                        // Timeout de 15 segundos para garantir sincronização (era 5s)
                        val timeout = networkQualityMonitor.getRecommendedTimeout().coerceAtLeast(10000L)
                        android.util.Log.d("ColetaRepositoryImpl", "🔄 Iniciando sync com timeout de ${timeout}ms")
                        kotlinx.coroutines.withTimeout(timeout) {
                            // ✅ Obter ID do inventário ativo
                            val inventarioId = preferencesManager.getInventarioAtivoId() ?: 0
                            
                            // ⚠️ VALIDAÇÃO: Verificar se inventário está configurado
                            if (inventarioId <= 0) {
                                // Buscar tentativas atuais da coleta para decidir se registra erro permanente
                                val tentativasAtuais = coletaDao.buscarPorId(id)?.tentativasSincronizacao ?: 0
                                if (tentativasAtuais >= 3) {
                                    val erro = "Inventário ativo não configurado no app"
                                    android.util.Log.e("ColetaRepositoryImpl", "❌ $erro (tentativa $tentativasAtuais — registrando erro permanente)")
                                    coletaDao.registrarErroSincronizacao(id, erro)
                                } else {
                                    // Nas primeiras tentativas, apenas incrementar contador sem registrar erro
                                    // A coleta ficará pendente e será retentada no próximo ciclo de sync
                                    android.util.Log.w("ColetaRepositoryImpl",
                                        "⚠️ Inventário não configurado ainda (tentativa ${tentativasAtuais + 1}/3) — coleta $id ficará pendente para retry")
                                    coletaDao.registrarErroSincronizacao(id, null)  // incrementa tentativas sem erro
                                }
                                return@withTimeout
                            }
                            android.util.Log.d("ColetaRepositoryImpl", "📤 Sincronizando coleta:")
                            android.util.Log.d("ColetaRepositoryImpl", "   Patrimônio: $numeroPatrimonio")
                            android.util.Log.d("ColetaRepositoryImpl", "   Estado de Conservação: ${coleta.estadoEncontrado ?: "BOM (padrão)"}")
                            
                            // Converter para MobileColetaRequest (formato esperado pelo servidor)
                            val request = com.inventario.mobile.data.remote.dto.MobileColetaRequest(
                                numeroPatrimonio = numeroPatrimonio,
                                idInventario = inventarioId,
                                usuarioId = coleta.usuarioId.toInt(),
                                idSala = patrimonio?.idSala,
                                localizacaoEncontrada = coleta.localizacaoEncontrada ?: coleta.localizacaoAtual,
                                estadoEncontrado = coleta.estadoEncontrado ?: "BOM",
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
                                divergencia = coleta.divergencia,          // v2.13: real value
                                motivoDivergencia = coleta.motivoDivergencia, // v2.13: real value
                                tempoColetaSegundos = coleta.tempoColetaSegundos,
                                tempoScanSegundos = coleta.tempoScanSegundos,
                                tempoPreenchimentoSegundos = coleta.tempoPreenchimentoSegundos,
                                metodoColeta = coleta.metodoColeta,
                                tipoScan = coleta.tipoScan
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
                                val erroMsg = response.message ?: "Erro desconhecido do servidor"
                                android.util.Log.w("ColetaRepositoryImpl", "⚠ Servidor retornou success=false: $erroMsg")
                                
                                // ✅ CORREÇÃO: Registrar erro no banco para que coleta não fique "presa"
                                coletaDao.registrarErroSincronizacao(id, erroMsg)
                                
                                // Registrar erro de sincronização na auditoria
                                auditService.registrarErroSincronizacao(
                                    coletaId = id,
                                    erro = erroMsg,
                                    tentativa = 1
                                )
                            }
                        }
                    } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                        // Timeout: registrar erro na coleta
                        networkQualityMonitor.registerSyncFailure()
                        val erroMsg = "Timeout na sincronização (${networkQualityMonitor.getRecommendedTimeout()}ms)"
                        android.util.Log.w("ColetaRepositoryImpl", "⏱️ $erroMsg - Coleta ficará pendente")
                        
                        // ✅ CORREÇÃO: Registrar erro no banco
                        coletaDao.registrarErroSincronizacao(id, erroMsg)
                        
                        auditService.registrarErroSincronizacao(
                            coletaId = id,
                            erro = erroMsg,
                            tentativa = 1
                        )
                    } catch (e: Exception) {
                        // Erro: registrar no banco para diagnóstico
                        networkQualityMonitor.registerSyncFailure()
                        val erroMsg = e.message ?: "Erro de conexão desconhecido"
                        android.util.Log.e("ColetaRepositoryImpl", "✗ Erro ao sincronizar coleta em background: $erroMsg", e)
                        
                        // ✅ CORREÇÃO: Registrar erro no banco para que usuário saiba o motivo
                        coletaDao.registrarErroSincronizacao(id, erroMsg)
                        
                        auditService.registrarErroSincronizacao(
                            coletaId = id,
                            erro = erroMsg,
                            tentativa = 1
                        )
                    }
                }
                
                android.util.Log.d("ColetaRepositoryImpl", "✓ Coleta salva localmente - Sincronização iniciada em background")
            } else {
                // Rede ruim/instável: tentar sincronizar mesmo assim com timeout curto
                android.util.Log.w("ColetaRepositoryImpl", 
                    "⚠ Rede instável ($networkQuality) - Tentando sincronizar mesmo assim...")
                
                // ✅ NOVO: Tentar sincronizar mesmo com rede ruim (fallback)
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    try {
                        kotlinx.coroutines.withTimeout(8000L) { // 8s timeout curto
                            val inventarioId = preferencesManager.getInventarioAtivoId() ?: 0
                            
                            // ⚠️ VALIDAÇÃO: Verificar se inventário está configurado
                            if (inventarioId <= 0) {
                                // Buscar tentativas atuais da coleta para decidir se registra erro permanente
                                val tentativasAtuais = coletaDao.buscarPorId(id)?.tentativasSincronizacao ?: 0
                                if (tentativasAtuais >= 3) {
                                    val erro = "Inventário ativo não configurado no app"
                                    android.util.Log.e("ColetaRepositoryImpl", "❌ $erro (tentativa $tentativasAtuais — registrando erro permanente)")
                                    coletaDao.registrarErroSincronizacao(id, erro)
                                } else {
                                    android.util.Log.w("ColetaRepositoryImpl",
                                        "⚠️ Inventário não configurado ainda (tentativa ${tentativasAtuais + 1}/3) — coleta $id ficará pendente para retry")
                                    coletaDao.registrarErroSincronizacao(id, null)  // incrementa tentativas sem erro
                                }
                                return@withTimeout
                            }
                            
                            val patrimonioLocal = patrimonioDao.buscarPorId(coleta.patrimonioId.toInt())
                            
                            val request = com.inventario.mobile.data.remote.dto.MobileColetaRequest(
                                numeroPatrimonio = coleta.numeroPatrimonio ?: patrimonioLocal?.numero ?: coleta.patrimonioId.toString(),
                                idInventario = inventarioId,
                                usuarioId = coleta.usuarioId.toInt(),
                                idSala = patrimonioLocal?.idSala,
                                localizacaoEncontrada = coleta.localizacaoEncontrada ?: coleta.localizacaoAtual,
                                estadoEncontrado = coleta.estadoEncontrado ?: "BOM",
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
                                divergencia = coleta.divergencia,          // v2.13: real value
                                motivoDivergencia = coleta.motivoDivergencia, // v2.13: real value
                                tempoColetaSegundos = coleta.tempoColetaSegundos,
                                tempoScanSegundos = coleta.tempoScanSegundos,
                                tempoPreenchimentoSegundos = coleta.tempoPreenchimentoSegundos,
                                metodoColeta = coleta.metodoColeta,
                                tipoScan = coleta.tipoScan
                            )
                            
                            android.util.Log.d("ColetaRepositoryImpl", "🔄 Tentativa de sync (fallback) - rede: $networkQuality")
                            
                            val response = coletaApi.registrarColeta(request)
                            
                            if (response.success) {
                                coletaDao.marcarSincronizada(id)
                                networkQualityMonitor.registerSyncSuccess()
                                android.util.Log.d("ColetaRepositoryImpl", "✓ Sync fallback bem-sucedido!")
                            } else {
                                val erroMsg = response.message ?: "Erro do servidor no fallback"
                                android.util.Log.w("ColetaRepositoryImpl", "⚠ Sync fallback falhou: $erroMsg")
                                // ✅ CORREÇÃO: Registrar erro no banco
                                coletaDao.registrarErroSincronizacao(id, erroMsg)
                            }
                        }
                    } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                        val erroMsg = "Timeout no sync fallback (8s) - Rede instável"
                        android.util.Log.w("ColetaRepositoryImpl", "⏱️ $erroMsg")
                        // ✅ CORREÇÃO: Registrar erro no banco
                        coletaDao.registrarErroSincronizacao(id, erroMsg)
                    } catch (e: Exception) {
                        val erroMsg = "Sync fallback falhou: ${e.message ?: "Erro desconhecido"}"
                        android.util.Log.w("ColetaRepositoryImpl", "⚠ $erroMsg")
                        // ✅ CORREÇÃO: Registrar erro no banco
                        coletaDao.registrarErroSincronizacao(id, erroMsg)
                    }
                }
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
            android.util.Log.d("ColetaRepositoryImpl", "═══════════════════════════════════════════")
            android.util.Log.d("ColetaRepositoryImpl", "🔄 INICIANDO SINCRONIZAÇÃO DE COLETAS PENDENTES")

            // Reset automático de erros recuperáveis (Bug 5)
            // Coletas com 5+ tentativas e erros transitórios recebem nova chance
            try {
                val coletasComMuitasTentativas = coletaDao.buscarColetasComMuitasTentativas(5)
                val errosRecuperaveis = listOf("timeout", "Timeout", "conexão", "connection", "network", "rede", "Rede")
                var resetadas = 0
                coletasComMuitasTentativas.forEach { entity ->
                    val erro = entity.erroSincronizacao ?: return@forEach
                    val ehRecuperavel = errosRecuperaveis.any { erro.contains(it, ignoreCase = true) }
                    if (ehRecuperavel) {
                        coletaDao.limparErroSincronizacao(entity.id)
                        resetadas++
                        android.util.Log.i("ColetaRepositoryImpl", "♻️ Erro recuperável resetado para coleta ${entity.id}: $erro")
                    }
                }
                if (resetadas > 0) {
                    android.util.Log.i("ColetaRepositoryImpl", "♻️ $resetadas coleta(s) com erro recuperável resetadas para nova tentativa")
                }
            } catch (e: Exception) {
                android.util.Log.w("ColetaRepositoryImpl", "Aviso: falha ao resetar erros recuperáveis", e)
            }

            val coletasPendentes = coletaDao.buscarPendentes()
            val pendentesAntes = coletasPendentes.size
            
            if (coletasPendentes.isEmpty()) {
                android.util.Log.d("ColetaRepositoryImpl", "✓ Nenhuma coleta pendente para sincronizar")
                android.util.Log.d("ColetaRepositoryImpl", "═══════════════════════════════════════════")
                return 0
            }
            
            android.util.Log.d("ColetaRepositoryImpl", "📋 Coletas pendentes ANTES: $pendentesAntes")
            coletasPendentes.forEachIndexed { index, entity ->
                android.util.Log.d("ColetaRepositoryImpl", "   ${index + 1}. ID=${entity.id}, Patrimônio=${entity.numeroPatrimonio}, Sincronizado=${entity.sincronizado}")
            }
            
            // Estratégia: Tentar batch primeiro, se falhar, sincronizar uma por uma
            // IMPORTANTE: buscarPendentes() retorna coletas de QUALQUER inventário (sem filtro).
            // O idInventario correto de cada coleta é lido da própria ColetaEntity (entity.idInventario),
            // que foi salvo no momento da coleta. O PreferencesManager NÃO é usado como fonte primária
            // para o idInventario no lote — apenas como fallback que agora rejeita coletas inválidas.
            val sincronizadas = try {
                sincronizarEmLote(coletasPendentes)
            } catch (e: Exception) {
                android.util.Log.w("ColetaRepositoryImpl", "⚠️ Falha no sync em lote, tentando individual", e)
                sincronizarIndividualmente(coletasPendentes)
            }
            
            // VERIFICAÇÃO: Conferir se as coletas foram realmente marcadas
            val pendentesDepois = coletaDao.contarPendentes()
            android.util.Log.d("ColetaRepositoryImpl", "═══════════════════════════════════════════")
            android.util.Log.d("ColetaRepositoryImpl", "📊 RESULTADO DA SINCRONIZAÇÃO:")
            android.util.Log.d("ColetaRepositoryImpl", "   Pendentes ANTES: $pendentesAntes")
            android.util.Log.d("ColetaRepositoryImpl", "   Sincronizadas: $sincronizadas")
            android.util.Log.d("ColetaRepositoryImpl", "   Pendentes DEPOIS: $pendentesDepois")
            
            if (pendentesDepois > 0) {
                android.util.Log.w("ColetaRepositoryImpl", "⚠️ ATENÇÃO: Ainda há $pendentesDepois coletas pendentes!")
                
                // Listar as coletas que ainda estão pendentes
                val aindaPendentes = coletaDao.buscarPendentes()
                aindaPendentes.forEach { entity ->
                    android.util.Log.w("ColetaRepositoryImpl", "   ⏳ ID=${entity.id}, Patrimônio=${entity.numeroPatrimonio}, Erro=${entity.erroSincronizacao ?: "nenhum"}")
                }
            }
            
            android.util.Log.d("ColetaRepositoryImpl", "═══════════════════════════════════════════")
            sincronizadas
            
        } catch (e: Exception) {
            android.util.Log.e("ColetaRepositoryImpl", "❌ Erro ao sincronizar coletas pendentes", e)
            -1
        }
    }
    
    /**
     * Sincroniza coletas em lote (mais eficiente)
     * v2.5: Apaga coletas sincronizadas do banco local para liberar espaço
     */
    private suspend fun sincronizarEmLote(coletasPendentes: List<com.inventario.mobile.data.local.entity.ColetaEntity>): Int {
        // Converter entities para requests
        val requests = coletasPendentes.mapNotNull { entity ->
            try {
                val coleta = mapper.toDomain(entity)
                val patrimonio = patrimonioDao.buscarPorId(coleta.patrimonioId.toInt())
                
                // ✅ Usar idInventario da própria entity (salvo no momento da coleta)
                // Não usar PreferencesManager pois o inventário ativo pode ter mudado
                val inventarioId = entity.idInventario.takeIf { it > 0 } ?: run {
                    android.util.Log.w("ColetaRepositoryImpl",
                        "⚠️ Coleta ${entity.id} (${entity.numeroPatrimonio}) ignorada no lote: idInventario inválido (0)")
                    coletaDao.registrarErroSincronizacao(
                        entity.id,
                        "idInventario inválido (0) — coleta não pode ser enviada sem inventário definido"
                    )
                    return@mapNotNull null  // pular esta coleta no lote
                }
                
                com.inventario.mobile.data.remote.dto.MobileColetaRequest(
                    numeroPatrimonio = patrimonio?.numero ?: entity.numeroPatrimonio,
                    idInventario = inventarioId,
                    usuarioId = coleta.usuarioId.toInt(),
                    idSala = patrimonio?.idSala,
                    localizacaoEncontrada = coleta.localizacaoEncontrada ?: coleta.localizacaoAtual,
                    estadoEncontrado = coleta.estadoEncontrado ?: "BOM",
                    observacaoColeta = coleta.observacoes,
                    dataColeta = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", java.util.Locale.getDefault())
                        .format(java.util.Date(coleta.dataColeta)),
                    latitude = coleta.latitude,
                    longitude = coleta.longitude,
                    fotoPatrimonio = null,
                    // ✅ Preservar campos de item sem etiqueta da entity
                    semEtiqueta = entity.semEtiqueta,
                    descricaoItemSemEtiqueta = entity.descricaoItemSemEtiqueta,
                    categoriaItemSemEtiqueta = entity.categoriaItemSemEtiqueta,
                    deviceId = android.os.Build.MODEL,
                    appVersion = "1.2",
                    divergencia = false,
                    motivoDivergencia = null,
                    // ✅ Preservar métricas de tempo
                    tempoColetaSegundos = entity.tempoColetaSegundos,
                    tempoScanSegundos = entity.tempoScanSegundos,
                    tempoPreenchimentoSegundos = entity.tempoPreenchimentoSegundos,
                    metodoColeta = entity.metodoColeta,
                    tipoScan = entity.tipoScan
                )
            } catch (e: Exception) {
                android.util.Log.e("ColetaRepositoryImpl", "Erro ao converter coleta ${entity.id}", e)
                null
            }
        }
        
        if (requests.isEmpty()) {
            return 0
        }
        
        // ✅ Preservar mapeamento request ↔ entity para rastrear qual coleta local foi
        //    enviada em cada posição do lote. O lote ENVIADO ao servidor é `requests`,
        //    mas `coletasPendentes` pode conter entries que foram puladas (idInventario<=0
        //    via mapNotNull). Reconstruímos a lista paralela de entities ENVIADAS.
        val entitiesEnviadas = coletasPendentes.filter { entity ->
            entity.idInventario > 0
        }

        if (entitiesEnviadas.size != requests.size) {
            android.util.Log.w(
                "ColetaRepositoryImpl",
                "⚠️ Desalinhamento entre requests (${requests.size}) e entitiesEnviadas " +
                        "(${entitiesEnviadas.size}). Verificar lógica de filtro."
            )
        }

        // Enviar em lote
        val batchRequest = com.inventario.mobile.data.remote.dto.MobileColetaBatchRequest(requests)
        val response = coletaApi.registrarColetasEmLote(batchRequest)

        if (response.success) {
            // Extrair resultado do response
            val resultado = response.data
            val sucesso = (resultado?.get("sucesso") as? Number)?.toInt() ?: 0
            val falhas = (resultado?.get("falhas") as? Number)?.toInt() ?: 0
            @Suppress("UNCHECKED_CAST")
            val erros = (resultado?.get("erros") as? List<String>) ?: emptyList()

            // ✅ BUGFIX F7 (07/05/2026): preferir a nova estrutura estruturada `resultados[]`
            //    que o servidor passou a devolver. Cada item tem { indice, numeroPatrimonio,
            //    status (SUCESSO/DUPLICADA/FALHA), coletaId, mensagem }. Com ela, marcamos
            //    apenas as coletas que foram REALMENTE persistidas pelo servidor, em vez
            //    de usar regex frágil sobre as mensagens de erro.
            @Suppress("UNCHECKED_CAST")
            val resultadosEstruturados = resultado?.get("resultados") as? List<Map<String, Any?>>

            android.util.Log.d("ColetaRepositoryImpl", "═══════════════════════════════════════════")
            android.util.Log.d("ColetaRepositoryImpl", "📊 RESULTADO DO BATCH SYNC:")
            android.util.Log.d("ColetaRepositoryImpl", "   ✅ Sucesso: $sucesso")
            android.util.Log.d("ColetaRepositoryImpl", "   ❌ Falhas: $falhas")
            android.util.Log.d(
                "ColetaRepositoryImpl",
                "   📦 Resultados estruturados disponíveis: ${resultadosEstruturados != null}"
            )

            // Caminho 1: servidor moderno devolveu `resultados[]` — fonte da verdade
            if (!resultadosEstruturados.isNullOrEmpty() &&
                resultadosEstruturados.size == entitiesEnviadas.size
            ) {
                var marcadasComSucesso = 0
                resultadosEstruturados.forEachIndexed { indice, item ->
                    val entity = entitiesEnviadas.getOrNull(indice)
                    if (entity == null) {
                        android.util.Log.w(
                            "ColetaRepositoryImpl",
                            "⚠️ resultados[$indice] sem entity correspondente, ignorando"
                        )
                        return@forEachIndexed
                    }
                    val status = (item["status"] as? String).orEmpty()
                    val mensagem = item["mensagem"] as? String
                    val servidorId = (item["coletaId"] as? Number)?.toLong()

                    when (status) {
                        "SUCESSO" -> {
                            try {
                                if (servidorId != null && servidorId > 0) {
                                    coletaDao.marcarSincronizadaComServidor(entity.id, servidorId)
                                } else {
                                    coletaDao.marcarSincronizada(entity.id)
                                }
                                marcadasComSucesso++
                                android.util.Log.d(
                                    "ColetaRepositoryImpl",
                                    "✅ Coleta ${entity.id} (${entity.numeroPatrimonio}) sincronizada (servidorId=$servidorId)"
                                )
                            } catch (e: Exception) {
                                android.util.Log.e(
                                    "ColetaRepositoryImpl",
                                    "❌ Erro ao marcar coleta ${entity.id} como sincronizada",
                                    e
                                )
                            }
                        }
                        "DUPLICADA" -> {
                            // Servidor detectou duplicata — coleta já existe lá. Do ponto
                            // de vista do app, não há mais nada a fazer: marcar como
                            // sincronizada para sair da fila. `servidorId` aponta para
                            // a coleta original.
                            try {
                                if (servidorId != null && servidorId > 0) {
                                    coletaDao.marcarSincronizadaComServidor(entity.id, servidorId)
                                } else {
                                    coletaDao.marcarSincronizada(entity.id)
                                }
                                marcadasComSucesso++
                                android.util.Log.d(
                                    "ColetaRepositoryImpl",
                                    "♻️ Coleta ${entity.id} (${entity.numeroPatrimonio}) DUPLICADA no servidor (original=$servidorId)"
                                )
                            } catch (e: Exception) {
                                android.util.Log.e(
                                    "ColetaRepositoryImpl",
                                    "❌ Erro ao marcar coleta duplicada ${entity.id}",
                                    e
                                )
                            }
                        }
                        "FALHA" -> {
                            val erroMsg = mensagem ?: "Erro desconhecido no batch"
                            android.util.Log.w(
                                "ColetaRepositoryImpl",
                                "⚠️ Coleta ${entity.id} (${entity.numeroPatrimonio}) falhou: $erroMsg"
                            )
                            coletaDao.registrarErroSincronizacao(entity.id, erroMsg)
                        }
                        else -> {
                            // Status desconhecido — tratar conservadoramente como falha
                            // (não marcar como sincronizada) para evitar perda silenciosa.
                            val erroMsg = "Status desconhecido do servidor: '$status'. ${mensagem ?: ""}"
                            android.util.Log.w(
                                "ColetaRepositoryImpl",
                                "⚠️ Coleta ${entity.id} — $erroMsg"
                            )
                            coletaDao.registrarErroSincronizacao(entity.id, erroMsg)
                        }
                    }
                }

                android.util.Log.d("ColetaRepositoryImpl", "═══════════════════════════════════════════")
                android.util.Log.d(
                    "ColetaRepositoryImpl",
                    "✅ Batch sync (estruturado): $marcadasComSucesso marcadas como sincronizadas"
                )
                return marcadasComSucesso
            }

            // Caminho 2 (retrocompatibilidade): servidor antigo sem `resultados[]`.
            // Fluxo antigo preservado para não quebrar transições de deploy.
            android.util.Log.w(
                "ColetaRepositoryImpl",
                "⚠️ Servidor não retornou `resultados[]` estruturados — usando fallback legado"
            )

            // Se TODAS as coletas foram sincronizadas com sucesso
            if (falhas == 0 && sucesso == entitiesEnviadas.size) {
                entitiesEnviadas.forEach { entity ->
                    try {
                        coletaDao.marcarSincronizada(entity.id)
                        android.util.Log.d(
                            "ColetaRepositoryImpl",
                            "✅ Coleta ${entity.id} marcada como sincronizada (fallback)"
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("ColetaRepositoryImpl", "❌ Erro ao marcar coleta ${entity.id}", e)
                    }
                }
                android.util.Log.d("ColetaRepositoryImpl", "═══════════════════════════════════════════")
                return sucesso
            }

            // ✅ BUGFIX F7 (fallback seguro): se houve falhas e não temos estrutura
            //    `resultados[]`, NÃO marcar nenhuma coleta como sincronizada para evitar
            //    perda silenciosa. Registrar erro em todas com a mensagem agregada.
            //    O sync periódico tentará novamente na próxima execução.
            android.util.Log.w(
                "ColetaRepositoryImpl",
                "⚠️ Falhas parciais sem `resultados[]`: marcando todas como pendentes para retry. " +
                        "sucesso=$sucesso, falhas=$falhas, erros=${erros.size}"
            )
            val mensagemAgregada = if (erros.isNotEmpty()) {
                "Batch com falhas parciais — servidor antigo: ${erros.joinToString("; ").take(200)}"
            } else {
                "Batch com $falhas falha(s) — servidor antigo sem resultados estruturados"
            }
            entitiesEnviadas.forEach { entity ->
                coletaDao.registrarErroSincronizacao(entity.id, mensagemAgregada)
            }
            android.util.Log.d("ColetaRepositoryImpl", "═══════════════════════════════════════════")
            return 0
        } else {
            android.util.Log.e("ColetaRepositoryImpl", "❌ Batch sync falhou completamente: ${response.message}")
            throw Exception("Batch sync falhou: ${response.message}")
        }
    }
    
    /**
     * Sincroniza coletas individualmente (fallback)
     * v2.5: Apaga coletas sincronizadas do banco local para liberar espaço
     */
    private suspend fun sincronizarIndividualmente(coletasPendentes: List<com.inventario.mobile.data.local.entity.ColetaEntity>): Int {
        var sincronizadas = 0
        
        for (entity in coletasPendentes) {
            try {
                val coleta = mapper.toDomain(entity)
                val patrimonio = patrimonioDao.buscarPorId(coleta.patrimonioId.toInt())
                
                // ✅ Usar idInventario da própria entity (salvo no momento da coleta)
                val inventarioId = entity.idInventario.takeIf { it > 0 } ?: run {
                    android.util.Log.w("ColetaRepositoryImpl",
                        "⚠️ Coleta ${entity.id} (${entity.numeroPatrimonio}) ignorada: idInventario inválido (0)")
                    coletaDao.registrarErroSincronizacao(
                        entity.id,
                        "idInventario inválido (0) — coleta não pode ser enviada sem inventário definido"
                    )
                    -1
                }
                if (inventarioId <= 0) continue
                
                // Converter para MobileColetaRequest
                val request = com.inventario.mobile.data.remote.dto.MobileColetaRequest(
                    numeroPatrimonio = patrimonio?.numero ?: entity.numeroPatrimonio,
                    idInventario = inventarioId,
                    usuarioId = coleta.usuarioId.toInt(),
                    idSala = patrimonio?.idSala,
                    localizacaoEncontrada = coleta.localizacaoEncontrada ?: coleta.localizacaoAtual,
                    estadoEncontrado = coleta.estadoEncontrado ?: "BOM",
                    observacaoColeta = coleta.observacoes,
                    dataColeta = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", java.util.Locale.getDefault())
                        .format(java.util.Date(coleta.dataColeta)),
                    latitude = coleta.latitude,
                    longitude = coleta.longitude,
                    fotoPatrimonio = null,
                    // ✅ Preservar campos de item sem etiqueta da entity
                    semEtiqueta = entity.semEtiqueta,
                    descricaoItemSemEtiqueta = entity.descricaoItemSemEtiqueta,
                    categoriaItemSemEtiqueta = entity.categoriaItemSemEtiqueta,
                    deviceId = android.os.Build.MODEL,
                    appVersion = "1.2",
                    divergencia = false,
                    motivoDivergencia = null,
                    // ✅ Preservar métricas de tempo
                    tempoColetaSegundos = entity.tempoColetaSegundos,
                    tempoScanSegundos = entity.tempoScanSegundos,
                    tempoPreenchimentoSegundos = entity.tempoPreenchimentoSegundos,
                    metodoColeta = entity.metodoColeta,
                    tipoScan = entity.tipoScan
                )
                
                val response = coletaApi.registrarColeta(request)
                
                if (response.success) {
                    // ✅ Marcar como sincronizada COM o ID do servidor
                    try {
                        val servidorId = response.data?.id
                        if (servidorId != null) {
                            coletaDao.marcarSincronizadaComServidor(entity.id, servidorId)
                            android.util.Log.d("ColetaRepositoryImpl", "✅ Coleta ${entity.id} sincronizada (servidorId=$servidorId)")
                        } else {
                            coletaDao.marcarSincronizada(entity.id)
                            android.util.Log.d("ColetaRepositoryImpl", "✅ Coleta ${entity.id} sincronizada (sem servidorId)")
                        }
                        sincronizadas++
                    } catch (e: Exception) {
                        android.util.Log.e("ColetaRepositoryImpl", "❌ Erro ao marcar coleta ${entity.id}", e)
                    }
                } else {
                    android.util.Log.w("ColetaRepositoryImpl", "⚠️ Coleta ${entity.id} falhou: ${response.message}")
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
        
        android.util.Log.d("ColetaRepositoryImpl", "✓ Sync individual: $sincronizadas coletas sincronizadas")
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
    
    // ========================================
    // Diagnóstico de Coletas Pendentes (v2.6)
    // ========================================
    
    override suspend fun getColetasPendentesComErro(): List<Coleta> {
        return try {
            coletaDao.buscarPendentesComErro().map { mapper.toDomain(it) }
        } catch (e: Exception) {
            android.util.Log.e("ColetaRepositoryImpl", "Erro ao buscar coletas com erro", e)
            emptyList()
        }
    }
    
    override suspend fun getColetasPendentesSemErro(): List<Coleta> {
        return try {
            coletaDao.buscarPendentesSemErro().map { mapper.toDomain(it) }
        } catch (e: Exception) {
            android.util.Log.e("ColetaRepositoryImpl", "Erro ao buscar coletas sem erro", e)
            emptyList()
        }
    }
    
    override suspend fun limparErroColeta(coletaId: Long) {
        try {
            coletaDao.limparErroSincronizacao(coletaId)
            android.util.Log.d("ColetaRepositoryImpl", "✓ Erro limpo da coleta $coletaId")
        } catch (e: Exception) {
            android.util.Log.e("ColetaRepositoryImpl", "Erro ao limpar erro da coleta $coletaId", e)
        }
    }
    
    override suspend fun limparTodosErrosColetas(): Int {
        return try {
            val quantidade = coletaDao.limparTodosErrosSincronizacao()
            android.util.Log.d("ColetaRepositoryImpl", "✓ Erros limpos de $quantidade coletas")
            quantidade
        } catch (e: Exception) {
            android.util.Log.e("ColetaRepositoryImpl", "Erro ao limpar todos os erros", e)
            0
        }
    }
    
    override suspend fun removerColetaPendente(coletaId: Long) {
        try {
            coletaDao.deletar(coletaId)
            android.util.Log.d("ColetaRepositoryImpl", "✓ Coleta pendente $coletaId removida")
        } catch (e: Exception) {
            android.util.Log.e("ColetaRepositoryImpl", "Erro ao remover coleta $coletaId", e)
        }
    }
    
    /**
     * Sincroniza uma coleta específica
     * Usado para reenviar coletas que falharam
     */
    override suspend fun sincronizarColetaEspecifica(coletaId: Long): Boolean {
        return try {
            android.util.Log.d("ColetaRepositoryImpl", "🔄 Sincronizando coleta específica: $coletaId")
            
            // Buscar coleta
            val entity = coletaDao.buscarPorId(coletaId) ?: run {
                android.util.Log.e("ColetaRepositoryImpl", "❌ Coleta $coletaId não encontrada")
                return false
            }
            
            // Converter para domain
            val coleta = mapper.toDomain(entity)
            val patrimonio = patrimonioDao.buscarPorId(coleta.patrimonioId.toInt())
            
            // Obter inventário ativo — preferir o da entity, fallback para PreferencesManager
            val inventarioId = entity.idInventario.takeIf { it > 0 }
                ?: preferencesManager.getInventarioAtivoId()
                ?: 0
            
            if (inventarioId <= 0) {
                android.util.Log.e("ColetaRepositoryImpl", "❌ Inventário não configurado")
                coletaDao.registrarErroSincronizacao(coletaId, "Inventário não configurado no app")
                return false
            }
            
            // Criar request
            val request = com.inventario.mobile.data.remote.dto.MobileColetaRequest(
                numeroPatrimonio = patrimonio?.numero ?: entity.numeroPatrimonio,
                idInventario = inventarioId,
                usuarioId = coleta.usuarioId.toInt(),
                idSala = patrimonio?.idSala,
                localizacaoEncontrada = coleta.localizacaoEncontrada ?: coleta.localizacaoAtual,
                estadoEncontrado = coleta.estadoEncontrado ?: "BOM",
                observacaoColeta = coleta.observacoes,
                dataColeta = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", java.util.Locale.getDefault())
                    .format(java.util.Date(coleta.dataColeta)),
                latitude = coleta.latitude,
                longitude = coleta.longitude,
                fotoPatrimonio = null,
                // ✅ Preservar campos de item sem etiqueta da entity
                semEtiqueta = entity.semEtiqueta,
                descricaoItemSemEtiqueta = entity.descricaoItemSemEtiqueta,
                categoriaItemSemEtiqueta = entity.categoriaItemSemEtiqueta,
                deviceId = android.os.Build.MODEL,
                appVersion = "1.2",
                divergencia = false,
                motivoDivergencia = null,
                // ✅ Preservar métricas de tempo
                tempoColetaSegundos = entity.tempoColetaSegundos,
                tempoScanSegundos = entity.tempoScanSegundos,
                tempoPreenchimentoSegundos = entity.tempoPreenchimentoSegundos,
                metodoColeta = entity.metodoColeta,
                tipoScan = entity.tipoScan
            )
            
            // Enviar para servidor
            android.util.Log.d("ColetaRepositoryImpl", "📤 Enviando coleta $coletaId para servidor...")
            val response = coletaApi.registrarColeta(request)
            
            if (response.success) {
                // ✅ Marcar como sincronizada COM o ID do servidor
                val servidorId = response.data?.id
                if (servidorId != null) {
                    coletaDao.marcarSincronizadaComServidor(coletaId, servidorId)
                    android.util.Log.d("ColetaRepositoryImpl", "✅ Coleta $coletaId sincronizada (servidorId=$servidorId)")
                } else {
                    coletaDao.marcarSincronizada(coletaId)
                    android.util.Log.d("ColetaRepositoryImpl", "✅ Coleta $coletaId sincronizada (sem servidorId)")
                }
                true
            } else {
                val erro = response.message ?: "Erro desconhecido"
                android.util.Log.e("ColetaRepositoryImpl", "❌ Falha ao sincronizar coleta $coletaId: $erro")
                coletaDao.registrarErroSincronizacao(coletaId, erro)
                false
            }
            
        } catch (e: Exception) {
            android.util.Log.e("ColetaRepositoryImpl", "❌ Exceção ao sincronizar coleta $coletaId", e)
            coletaDao.registrarErroSincronizacao(coletaId, e.message ?: "Erro de conexão")
            false
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
    
    /**
     * v2.7: Registra coleta de item SEM etiqueta
     * Envia para o servidor com os campos específicos de sem etiqueta
     */
    override suspend fun registrarColetaSemEtiqueta(coleta: Coleta): Result<Coleta> {
        return try {
            android.util.Log.d("ColetaRepositoryImpl", "═══════════════════════════════════════════")
            android.util.Log.d("ColetaRepositoryImpl", "🏷️ REGISTRANDO ITEM SEM ETIQUETA")
            android.util.Log.d("ColetaRepositoryImpl", "   Descrição: ${coleta.descricaoItemSemEtiqueta}")
            android.util.Log.d("ColetaRepositoryImpl", "   Categoria: ${coleta.categoriaItemSemEtiqueta}")
            
            // 1. Obter inventário ativo
            val inventarioId = preferencesManager.getInventarioAtivoId() ?: 0
            if (inventarioId <= 0) {
                return Result.failure(Exception("Inventário ativo não configurado"))
            }
            
            // 2. Criar entity para salvar localmente
            val entity = com.inventario.mobile.data.local.entity.ColetaEntity(
                id = 0,
                idPatrimonio = 0,
                numeroPatrimonio = "",
                idInventario = inventarioId,
                idSala = coleta.salaId,
                nomeSala = null,                                                    // sem etiqueta não tem sala de origem
                localizacaoEncontrada = coleta.localizacaoAtual,                    // onde foi encontrado
                idResponsavel = null,
                nomeResponsavel = null,
                observacao = coleta.observacoes,
                estadoPatrimonio = coleta.status,
                latitude = coleta.latitude,
                longitude = coleta.longitude,
                dataColeta = coleta.dataColeta,
                idUsuario = coleta.usuarioId.toInt(),
                nomeUsuario = preferencesManager.getUserName(),
                sincronizado = false,
                metodoColeta = "SEM_ETIQUETA",
                // v2.7: Campos de item sem etiqueta
                semEtiqueta = true,
                descricaoItemSemEtiqueta = coleta.descricaoItemSemEtiqueta,
                categoriaItemSemEtiqueta = coleta.categoriaItemSemEtiqueta,
                // v2.21: caminho do arquivo de foto (não Base64)
                fotoPath = coleta.fotoPath,
                fotoThumbnailPath = coleta.fotoThumbnailPath,
                fotoSincronizada = false
            )
            
            // 3. Salvar localmente
            val id = coletaDao.inserir(entity)
            android.util.Log.d("ColetaRepositoryImpl", "✓ Item sem etiqueta salvo localmente (ID: $id)")
            
            // 4. Tentar sincronizar com servidor
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    withTimeout(15000L) {
                        val request = com.inventario.mobile.data.remote.dto.MobileColetaRequest(
                            numeroPatrimonio = "",
                            idInventario = inventarioId,
                            usuarioId = coleta.usuarioId.toInt(),
                            idSala = coleta.salaId,
                            localizacaoEncontrada = coleta.localizacaoEncontrada ?: coleta.localizacaoAtual,
                            estadoEncontrado = coleta.estadoEncontrado ?: "BOM",
                            observacaoColeta = coleta.observacoes,
                            dataColeta = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", java.util.Locale.getDefault())
                                .format(java.util.Date(coleta.dataColeta)),
                            latitude = coleta.latitude,
                            longitude = coleta.longitude,
                            fotoPatrimonio = coleta.fotoPath,
                            semEtiqueta = true,
                            descricaoItemSemEtiqueta = coleta.descricaoItemSemEtiqueta,
                            categoriaItemSemEtiqueta = coleta.categoriaItemSemEtiqueta,
                            deviceId = android.os.Build.MODEL,
                            appVersion = "1.2",
                            divergencia = false,
                            motivoDivergencia = null
                        )
                        
                        android.util.Log.d("ColetaRepositoryImpl", "🔄 Sincronizando item sem etiqueta...")
                        
                        val response = coletaApi.registrarColeta(request)
                        
                        if (response.success) {
                            coletaDao.marcarSincronizada(id)
                            android.util.Log.d("ColetaRepositoryImpl", "✓ Item sem etiqueta sincronizado!")
                        } else {
                            val erro = response.message ?: "Erro desconhecido"
                            android.util.Log.w("ColetaRepositoryImpl", "⚠ Erro ao sincronizar: $erro")
                            coletaDao.registrarErroSincronizacao(id, erro)
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ColetaRepositoryImpl", "❌ Erro ao sincronizar item sem etiqueta", e)
                    coletaDao.registrarErroSincronizacao(id, e.message ?: "Erro de conexão")
                }
            }
            
            android.util.Log.d("ColetaRepositoryImpl", "═══════════════════════════════════════════")
            Result.success(coleta.copy(id = id))
            
        } catch (e: Exception) {
            android.util.Log.e("ColetaRepositoryImpl", "❌ Erro ao registrar item sem etiqueta", e)
            Result.failure(e)
        }
    }
}
