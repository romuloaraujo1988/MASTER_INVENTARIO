package com.inventario.mobile.data.repository

import com.inventario.mobile.data.local.dao.ColetaDao
import com.inventario.mobile.data.local.dao.PatrimonioDao
import com.inventario.mobile.data.local.dao.SalaDao
import com.inventario.mobile.data.local.dao.ResponsavelDao
import com.inventario.mobile.api.PatrimonioApi
import com.inventario.mobile.api.SalaApi
import com.inventario.mobile.data.remote.api.ColetaApi
import com.inventario.mobile.data.remote.api.ApiService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositório para sincronização de dados
 * Gerencia sincronização completa entre servidor e banco local
 */
@Singleton
class SyncRepository @Inject constructor(
    private val patrimonioDao: PatrimonioDao,
    private val salaDao: SalaDao,
    private val responsavelDao: ResponsavelDao,
    private val coletaDao: ColetaDao,
    private val patrimonioApi: PatrimonioApi,
    private val salaApi: SalaApi,
    private val coletaApi: ColetaApi,
    private val apiService: ApiService,
    private val offlineSyncApi: com.inventario.mobile.data.remote.api.OfflineSyncApi  // v2.2: API dedicada
) {
    
    /**
     * Resultado da sincronização
     */
    data class SyncResult(
        val patrimonios: Int,
        val salas: Int,
        val responsaveis: Int,
        val tempoMs: Long
    )
    
    /**
     * Força sincronização completa do servidor usando endpoint dedicado
     * 
     * v2.2: USA ENDPOINT OTIMIZADO /api/mobile/sync/offline-data
     * - 1 requisição ao invés de múltiplas
     * - Dados compactados
     * - Mais rápido e confiável
     */
    suspend fun forceSyncFromServerOptimized(): Result<SyncResult> {
        return try {
            android.util.Log.d("SyncRepository", "═══════════════════════════════════════════")
            android.util.Log.d("SyncRepository", "🔄 SINCRONIZAÇÃO OTIMIZADA (ENDPOINT DEDICADO)")
            val startTime = System.currentTimeMillis()
            
            // Buscar TODOS os dados em uma única requisição
            android.util.Log.d("SyncRepository", "📡 Chamando endpoint /api/mobile/sync/offline-data...")
            val response = offlineSyncApi.buscarDadosOffline()
            
            if (!response.isSuccessful || response.body()?.success != true) {
                val erro = "Erro ao buscar dados: ${response.message()}"
                android.util.Log.e("SyncRepository", "❌ $erro")
                return Result.failure(Exception(erro))
            }
            
            val dados = response.body()?.data
            if (dados == null) {
                android.util.Log.e("SyncRepository", "❌ Resposta sem dados")
                return Result.failure(Exception("Resposta sem dados"))
            }
            
            android.util.Log.d("SyncRepository", "✅ Dados recebidos do servidor:")
            android.util.Log.d("SyncRepository", "   📦 Patrimônios: ${dados.patrimonios.size}")
            android.util.Log.d("SyncRepository", "   🏢 Salas: ${dados.salas.size}")
            android.util.Log.d("SyncRepository", "   👤 Responsáveis: ${dados.responsaveis.size}")
            
            // Salvar patrimônios
            android.util.Log.d("SyncRepository", "💾 Salvando patrimônios no banco local...")
            val patrimoniosEntities = dados.patrimonios.map { dto ->
                com.inventario.mobile.data.local.entity.PatrimonioEntity(
                    id = dto.id,
                    numero = dto.numeroPatrimonio,
                    numeroPatrimonio = dto.numeroPatrimonio,
                    descricao = dto.descricao,
                    marca = dto.marca,
                    modelo = dto.modelo,
                    numeroSerie = null,
                    estado = dto.estado,
                    valor = null,
                    setorId = null,
                    setorNome = null,
                    idSala = dto.salaId,
                    nomeSala = dto.salaNome,
                    idResponsavel = dto.responsavelId,
                    nomeResponsavel = dto.responsavelNome,
                    status = dto.estado,
                    coletado = dto.coletado
                )
            }
            patrimonioDao.inserirTodos(patrimoniosEntities)
            android.util.Log.d("SyncRepository", "✅ ${patrimoniosEntities.size} patrimônios salvos")
            
            // Salvar salas
            android.util.Log.d("SyncRepository", "💾 Salvando salas no banco local...")
            dados.salas.forEach { dto ->
                val entity = com.inventario.mobile.data.local.entity.SalaEntity(
                    id = dto.id,
                    nome = dto.nome,
                    idSetor = null,
                    nomeSetor = null,
                    ativa = dto.ativa
                )
                salaDao.inserir(entity)
            }
            android.util.Log.d("SyncRepository", "✅ ${dados.salas.size} salas salvas")
            
            // Salvar responsáveis
            android.util.Log.d("SyncRepository", "💾 Salvando responsáveis no banco local...")
            dados.responsaveis.forEach { dto ->
                val entity = com.inventario.mobile.data.local.entity.ResponsavelEntity(
                    id = dto.id,
                    nome = dto.nome,
                    cpf = dto.cpf,
                    email = null
                )
                responsavelDao.inserir(entity)
            }
            android.util.Log.d("SyncRepository", "✅ ${dados.responsaveis.size} responsáveis salvos")
            
            val tempoMs = System.currentTimeMillis() - startTime
            val tempoSeg = tempoMs / 1000.0
            
            android.util.Log.d("SyncRepository", "═══════════════════════════════════════════")
            android.util.Log.d("SyncRepository", "✅ SINCRONIZAÇÃO OTIMIZADA CONCLUÍDA")
            android.util.Log.d("SyncRepository", "📊 Patrimônios: ${dados.patrimonios.size}")
            android.util.Log.d("SyncRepository", "🏢 Salas: ${dados.salas.size}")
            android.util.Log.d("SyncRepository", "👤 Responsáveis: ${dados.responsaveis.size}")
            android.util.Log.d("SyncRepository", "⏱️ Tempo: ${tempoSeg}s (${tempoMs}ms)")
            android.util.Log.d("SyncRepository", "🚀 Versão servidor: ${dados.metadata.versaoServidor}")
            android.util.Log.d("SyncRepository", "═══════════════════════════════════════════")
            
            Result.success(SyncResult(
                patrimonios = dados.patrimonios.size,
                salas = dados.salas.size,
                responsaveis = dados.responsaveis.size,
                tempoMs = tempoMs
            ))
            
        } catch (e: Exception) {
            android.util.Log.e("SyncRepository", "❌ ERRO na sincronização otimizada", e)
            Result.failure(e)
        }
    }
    
    /**
     * Força sincronização completa do servidor (MÉTODO ANTIGO - MANTIDO PARA COMPATIBILIDADE)
     * Baixa TODOS os dados: patrimônios, salas e responsáveis
     * 
     * @deprecated Use forceSyncFromServerOptimized() que é mais rápido
     */
    @Deprecated("Use forceSyncFromServerOptimized()")
    suspend fun forceSyncFromServer(): Result<SyncResult> {
        return try {
            android.util.Log.d("SyncRepository", "═══════════════════════════════════════════")
            android.util.Log.d("SyncRepository", "🔄 INICIANDO SINCRONIZAÇÃO COMPLETA")
            val startTime = System.currentTimeMillis()
            
            var patrimoniosCount = 0
            var salasCount = 0
            var responsaveisCount = 0
            
            // 1. Baixar patrimônios do servidor (COM PAGINAÇÃO - SEM LIMITE)
            android.util.Log.d("SyncRepository", "1️⃣ Baixando patrimônios do servidor...")
            try {
                var page = 0
                val pageSize = 100
                var totalPatrimonios = 0
                
                while (true) {
                    android.util.Log.d("SyncRepository", "   📄 Baixando página ${page + 1} de patrimônios (${page * pageSize} - ${(page + 1) * pageSize})...")
                    val patrimoniosResponse = patrimonioApi.listarPatrimoniosPaginado(page, pageSize)
                    
                    if (patrimoniosResponse.isSuccessful && patrimoniosResponse.body()?.success == true) {
                        val patrimonios = patrimoniosResponse.body()?.data ?: emptyList()
                        
                        if (patrimonios.isEmpty()) {
                            android.util.Log.d("SyncRepository", "   ✓ Nenhum patrimônio na página ${page + 1}, finalizando paginação")
                            break
                        }
                        
                        android.util.Log.d("SyncRepository", "   ✓ ${patrimonios.size} patrimônios recebidos na página ${page + 1}")
                        
                        // Converter para entities
                        val entities = patrimonios.map { patrimonio ->
                            com.inventario.mobile.data.local.entity.PatrimonioEntity(
                                id = patrimonio.id, // Long → Long (compatível)
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
                                idResponsavel = patrimonio.responsavelId?.toInt(),
                                nomeResponsavel = patrimonio.responsavelNome,
                                status = patrimonio.estado,
                                coletado = patrimonio.coletado
                            )
                        }
                        
                        // Salvar em lote (MUITO mais rápido)
                        patrimonioDao.inserirTodos(entities)
                        
                        totalPatrimonios += patrimonios.size
                        android.util.Log.d("SyncRepository", "   💾 ${patrimonios.size} patrimônios salvos em lote (total acumulado: $totalPatrimonios)")
                        
                        page++
                        
                        // Se retornou menos que pageSize, não há mais páginas
                        if (patrimonios.size < pageSize) {
                            android.util.Log.d("SyncRepository", "   ✓ Última página alcançada (recebeu ${patrimonios.size} < $pageSize)")
                            break
                        }
                        
                        // REMOVIDO: Limite artificial de 200 páginas
                        // Agora continua até não haver mais dados
                        
                    } else {
                        android.util.Log.w("SyncRepository", "   ⚠️ Erro ao baixar patrimônios página ${page + 1}: ${patrimoniosResponse.message()}")
                        break
                    }
                }
                
                patrimoniosCount = totalPatrimonios
                android.util.Log.d("SyncRepository", "✅ ${patrimoniosCount} patrimônios salvos no banco local (TOTAL)")
            } catch (e: Exception) {
                android.util.Log.e("SyncRepository", "❌ ERRO CRÍTICO ao sincronizar patrimônios: ${e.message}", e)
                e.printStackTrace()
                // Não interrompe - continua com salas e responsáveis
            }
            
            // 2. Baixar salas do servidor (TODAS de uma vez)
            android.util.Log.d("SyncRepository", "2️⃣ Baixando TODAS as salas do servidor...")
            try {
                val salasResponse = salaApi.listarSalas()
                
                if (salasResponse.isSuccessful && salasResponse.body()?.success == true) {
                    val salas = salasResponse.body()?.data ?: emptyList()
                    
                    android.util.Log.d("SyncRepository", "   ✓ ${salas.size} salas recebidas do servidor")
                    
                    // Salvar todas no banco local
                    for (sala in salas) {
                        val entity = com.inventario.mobile.data.local.entity.SalaEntity(
                            id = sala.id,
                            nome = sala.nome,
                            idSetor = null,
                            nomeSetor = null,
                            ativa = sala.ativa ?: true
                        )
                        salaDao.inserir(entity)
                    }
                    
                    salasCount = salas.size
                    android.util.Log.d("SyncRepository", "✅ ${salasCount} salas salvas no banco local (TOTAL)")
                } else {
                    android.util.Log.w("SyncRepository", "   ⚠️ Erro ao baixar salas: ${salasResponse.message()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("SyncRepository", "❌ ERRO CRÍTICO ao sincronizar salas: ${e.message}", e)
                e.printStackTrace()
                // Não interrompe - continua com responsáveis
            }
            
            // 3. Baixar responsáveis do servidor (TODAS de uma vez)
            android.util.Log.d("SyncRepository", "3️⃣ Baixando TODOS os responsáveis do servidor...")
            try {
                val responsaveisResponse = apiService.getResponsaveis()
                
                if (responsaveisResponse.isSuccessful && responsaveisResponse.body()?.success == true) {
                    val responsaveis = responsaveisResponse.body()?.data ?: emptyList()
                    
                    android.util.Log.d("SyncRepository", "   ✓ ${responsaveis.size} responsáveis recebidos do servidor")
                    
                    // Salvar todos no banco local
                    for (responsavel in responsaveis) {
                        val entity = com.inventario.mobile.data.local.entity.ResponsavelEntity(
                            id = responsavel.id,
                            nome = responsavel.nome,
                            cpf = responsavel.cpf,
                            email = responsavel.email
                        )
                        responsavelDao.inserir(entity)
                    }
                    
                    responsaveisCount = responsaveis.size
                    android.util.Log.d("SyncRepository", "✅ ${responsaveisCount} responsáveis salvos no banco local (TOTAL)")
                } else {
                    android.util.Log.w("SyncRepository", "   ⚠️ Erro ao baixar responsáveis: ${responsaveisResponse.message()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("SyncRepository", "❌ ERRO CRÍTICO ao sincronizar responsáveis: ${e.message}", e)
                e.printStackTrace()
            }
            
            val tempoMs = System.currentTimeMillis() - startTime
            val tempoSeg = tempoMs / 1000.0
            
            android.util.Log.d("SyncRepository", "═══════════════════════════════════════════")
            android.util.Log.d("SyncRepository", "✅ SINCRONIZAÇÃO CONCLUÍDA COM SUCESSO")
            android.util.Log.d("SyncRepository", "📊 Patrimônios: $patrimoniosCount")
            android.util.Log.d("SyncRepository", "🏢 Salas: $salasCount")
            android.util.Log.d("SyncRepository", "👤 Responsáveis: $responsaveisCount")
            android.util.Log.d("SyncRepository", "⏱️ Tempo: ${tempoSeg}s (${tempoMs}ms)")
            android.util.Log.d("SyncRepository", "═══════════════════════════════════════════")
            
            Result.success(SyncResult(
                patrimonios = patrimoniosCount,
                salas = salasCount,
                responsaveis = responsaveisCount,
                tempoMs = tempoMs
            ))
        } catch (e: Exception) {
            android.util.Log.e("SyncRepository", "❌ ERRO GERAL NA SINCRONIZAÇÃO", e)
            Result.failure(e)
        }
    }
    
    /**
     * Verifica se há dados locais
     */
    suspend fun hasLocalData(): Boolean {
        return try {
            patrimonioDao.contarTodos() > 0
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Limpa TODOS os dados locais
     * Remove patrimônios, salas, responsáveis e coletas sincronizadas
     * 
     * ATENÇÃO: Coletas NÃO sincronizadas são preservadas para evitar perda de dados!
     * 
     * @return Resultado com estatísticas da limpeza
     */
    suspend fun clearAllLocalData(): Result<ClearResult> {
        return try {
            android.util.Log.d("SyncRepository", "═══════════════════════════════════════════")
            android.util.Log.d("SyncRepository", "🗑️ INICIANDO LIMPEZA DE DADOS LOCAIS")
            
            // Contar antes de limpar
            val patrimoniosAntes = patrimonioDao.contarTodos()
            val salasAntes = salaDao.contar()
            val responsaveisAntes = responsavelDao.contar()
            val coletasPendentes = coletaDao.contarPendentes()
            
            android.util.Log.d("SyncRepository", "📊 Dados antes da limpeza:")
            android.util.Log.d("SyncRepository", "   📦 Patrimônios: $patrimoniosAntes")
            android.util.Log.d("SyncRepository", "   🏢 Salas: $salasAntes")
            android.util.Log.d("SyncRepository", "   👤 Responsáveis: $responsaveisAntes")
            android.util.Log.d("SyncRepository", "   ⏳ Coletas pendentes: $coletasPendentes (PRESERVADAS)")
            
            // Limpar dados
            android.util.Log.d("SyncRepository", "🗑️ Limpando patrimônios...")
            patrimonioDao.limparTodos()
            
            android.util.Log.d("SyncRepository", "🗑️ Limpando salas...")
            salaDao.limparTodas()
            
            android.util.Log.d("SyncRepository", "🗑️ Limpando responsáveis...")
            responsavelDao.limparTodos()
            
            android.util.Log.d("SyncRepository", "🗑️ Limpando coletas sincronizadas...")
            coletaDao.limparSincronizadas()
            
            android.util.Log.d("SyncRepository", "═══════════════════════════════════════════")
            android.util.Log.d("SyncRepository", "✅ LIMPEZA CONCLUÍDA")
            android.util.Log.d("SyncRepository", "   📦 Patrimônios removidos: $patrimoniosAntes")
            android.util.Log.d("SyncRepository", "   🏢 Salas removidas: $salasAntes")
            android.util.Log.d("SyncRepository", "   👤 Responsáveis removidos: $responsaveisAntes")
            android.util.Log.d("SyncRepository", "   ⏳ Coletas pendentes preservadas: $coletasPendentes")
            android.util.Log.d("SyncRepository", "═══════════════════════════════════════════")
            
            Result.success(ClearResult(
                patrimoniosRemovidos = patrimoniosAntes,
                salasRemovidas = salasAntes,
                responsaveisRemovidos = responsaveisAntes,
                coletasPendentesPreservadas = coletasPendentes
            ))
            
        } catch (e: Exception) {
            android.util.Log.e("SyncRepository", "❌ ERRO ao limpar dados locais", e)
            Result.failure(e)
        }
    }
    
    /**
     * Resultado da limpeza de dados
     */
    data class ClearResult(
        val patrimoniosRemovidos: Int,
        val salasRemovidas: Int,
        val responsaveisRemovidos: Int,
        val coletasPendentesPreservadas: Int
    )
    
    /**
     * Obtém estatísticas dos dados locais
     * 
     * v2.6: CORRIGIDO - Agora conta coletas pendentes de sincronização
     */
    suspend fun getLocalStats(): Map<String, Int> {
        return try {
            android.util.Log.d("SyncRepository", "═══════════════════════════════════════════")
            android.util.Log.d("SyncRepository", "📊 BUSCANDO ESTATÍSTICAS LOCAIS")
            
            // Dados gerais
            val totalPatrimonios = patrimonioDao.contarTodos()
            val salas = salaDao.contar()
            val responsaveis = responsavelDao.contar()
            
            // Coletas (CORRIGIDO)
            val totalColetas = coletaDao.contarTodas()
            val coletasSincronizadas = coletaDao.contarSincronizadas()
            val coletasPendentes = coletaDao.contarPendentes()
            
            // Logs detalhados
            android.util.Log.d("SyncRepository", "📦 Patrimônios no banco: $totalPatrimonios")
            android.util.Log.d("SyncRepository", "🏢 Salas no banco: $salas")
            android.util.Log.d("SyncRepository", "👤 Responsáveis no banco: $responsaveis")
            android.util.Log.d("SyncRepository", "")
            android.util.Log.d("SyncRepository", "📋 COLETAS:")
            android.util.Log.d("SyncRepository", "   Total: $totalColetas")
            android.util.Log.d("SyncRepository", "   ✅ Sincronizadas: $coletasSincronizadas")
            android.util.Log.d("SyncRepository", "   ⏳ Pendentes: $coletasPendentes")
            
            // Validação
            if (coletasSincronizadas + coletasPendentes != totalColetas) {
                android.util.Log.w("SyncRepository", "⚠️ INCONSISTÊNCIA: Soma não bate!")
                android.util.Log.w("SyncRepository", "   $coletasSincronizadas + $coletasPendentes ≠ $totalColetas")
            }
            
            android.util.Log.d("SyncRepository", "═══════════════════════════════════════════")
            
            mapOf(
                "patrimonios" to totalPatrimonios,
                "salas" to salas,
                "responsaveis" to responsaveis,
                "coletados" to coletasSincronizadas,
                "pendentes" to coletasPendentes
            )
        } catch (e: Exception) {
            android.util.Log.e("SyncRepository", "❌ ERRO ao buscar estatísticas locais", e)
            emptyMap()
        }
    }
}
