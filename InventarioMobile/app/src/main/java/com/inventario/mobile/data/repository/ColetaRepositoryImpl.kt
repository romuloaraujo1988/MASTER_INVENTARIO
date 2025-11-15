package com.inventario.mobile.data.repository

import com.inventario.mobile.data.local.dao.ColetaDao
import com.inventario.mobile.data.local.dao.PatrimonioDao
import com.inventario.mobile.data.mapper.ColetaMapper
import com.inventario.mobile.data.remote.api.ColetaApi
import com.inventario.mobile.domain.model.Coleta
import com.inventario.mobile.domain.repository.ColetaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementação do repositório de Coleta
 * Estratégia: Offline-first com sincronização automática
 */
@javax.inject.Singleton
class ColetaRepositoryImpl @Inject constructor(
    private val coletaDao: ColetaDao,
    private val patrimonioDao: PatrimonioDao,
    private val coletaApi: ColetaApi,
    private val mapper: ColetaMapper
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
            // 1. Buscar dados do patrimônio para preencher campos
            val patrimonio = patrimonioDao.buscarPorId(coleta.patrimonioId.toInt())
            
            // 2. Buscar dados do usuário (se disponível)
            // TODO: Implementar busca de usuário quando necessário
            
            // 3. Criar entity com dados completos
            val entity = mapper.toEntity(coleta).copy(
                numeroPatrimonio = patrimonio?.numero ?: "",
                nomeUsuario = "Usuário ${coleta.usuarioId}" // TODO: Buscar nome real
            )
            
            // 4. Salvar localmente (offline-first)
            val id = coletaDao.inserir(entity)
            
            // 5. Marcar patrimônio como coletado
            patrimonioDao.marcarComoColetado(coleta.patrimonioId.toInt())
            
            // 6. Tentar sincronizar imediatamente (não bloqueia)
            try {
                // Converter para MobileColetaRequest (formato esperado pelo servidor)
                val request = com.inventario.mobile.data.remote.dto.MobileColetaRequest(
                    numeroPatrimonio = patrimonio?.numero ?: "",
                    idInventario = 2, // TODO: Obter ID do inventário ativo
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
                
                android.util.Log.d("ColetaRepositoryImpl", "Enviando coleta para servidor: $request")
                
                val response = coletaApi.registrarColeta(request)
                
                android.util.Log.d("ColetaRepositoryImpl", "Resposta do servidor: success=${response.success}, message=${response.message}")
                
                if (response.success) {
                    coletaDao.marcarSincronizada(id)
                    android.util.Log.d("ColetaRepositoryImpl", "✓ Coleta sincronizada com sucesso")
                } else {
                    android.util.Log.w("ColetaRepositoryImpl", "⚠ Servidor retornou success=false: ${response.message}")
                }
            } catch (e: Exception) {
                // Falha na sincronização não impede o sucesso local
                // Será sincronizado depois
                android.util.Log.e("ColetaRepositoryImpl", "✗ Erro ao sincronizar coleta", e)
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
                
                com.inventario.mobile.data.remote.dto.MobileColetaRequest(
                    numeroPatrimonio = patrimonio?.numero ?: entity.numeroPatrimonio,
                    idInventario = 2, // TODO: Obter ID do inventário ativo
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
                
                // Converter para MobileColetaRequest
                val request = com.inventario.mobile.data.remote.dto.MobileColetaRequest(
                    numeroPatrimonio = patrimonio?.numero ?: entity.numeroPatrimonio,
                    idInventario = 2, // TODO: Obter ID do inventário ativo
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
