package com.inventario.mobile.data.strategy

import android.util.Log
import com.inventario.mobile.data.local.dao.PatrimonioDao
import com.inventario.mobile.data.local.dao.SalaDao
import com.inventario.mobile.data.model.Patrimonio
import com.inventario.mobile.data.model.Sala
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Estratégia para buscar dados do banco local (Room/SQLite)
 */
class LocalDataSourceStrategy(
    private val patrimonioDao: PatrimonioDao,
    private val salaDao: SalaDao
) : DataSourceStrategy {
    
    companion object {
        private const val TAG = "LocalDataSource"
    }
    
    override suspend fun isAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Verifica se há dados no banco local
            val patrimoniosCount = patrimonioDao.contarTodos()
            val salasCount = salaDao.contar()
            
            val available = patrimoniosCount > 0 || salasCount > 0
            
            if (available) {
                Log.d(TAG, "Banco local disponível: $patrimoniosCount patrimônios, $salasCount salas")
            } else {
                Log.w(TAG, "Banco local vazio")
            }
            
            available
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar disponibilidade do banco local", e)
            false
        }
    }
    
    override suspend fun getPatrimonios(): Result<List<Patrimonio>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Buscando patrimônios do banco local...")
            
            val entities = patrimonioDao.getAllPatrimoniosList()
            val patrimonios = entities.map { entity ->
                // ✅ v2.8: Formatar data da coleta se existir
                val dataColetaFormatada = entity.dataColeta?.let { timestamp ->
                    try {
                        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                        sdf.format(java.util.Date(timestamp))
                    } catch (e: Exception) {
                        null
                    }
                }
                
                Patrimonio(
                    id = entity.id.toLong(),
                    numeroPatrimonio = entity.numero,
                    descricao = entity.descricao,
                    marca = entity.marca,
                    modelo = entity.modelo,
                    numeroSerie = entity.numeroSerie,
                    estado = entity.status ?: entity.estado,
                    valor = entity.valor,
                    setorId = entity.setorId?.toLong(),
                    setorNome = entity.setorNome,
                    salaId = entity.idSala?.toLong() ?: entity.salaId?.toLong(),
                    salaNome = entity.nomeSala ?: entity.salaNome,
                    responsavelId = entity.idResponsavel?.toLong() ?: entity.responsavelId?.toLong(),
                    responsavelNome = entity.nomeResponsavel ?: entity.responsavelNome,
                    qrCode = entity.numero,
                    observacoes = entity.observacoes,
                    coletado = entity.coletado,
                    dataColeta = entity.dataColeta?.toString(),
                    coletadoPor = entity.coletadoPor,  // ✅ CORREÇÃO: Usar campo da entity
                    dataColetaFormatada = dataColetaFormatada,  // ✅ CORREÇÃO: Formatar data
                    observacoesColeta = entity.observacoesColeta,  // ✅ CORREÇÃO: Usar campo da entity
                    sincronizado = true,
                    servidorId = null
                )
            }
            
            Log.d(TAG, "✓ ${patrimonios.size} patrimônios obtidos do banco local")
            Result.success(patrimonios)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar patrimônios do banco local", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getPatrimonioPorNumero(numero: String): Result<Patrimonio> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Buscando patrimônio $numero do banco local...")
            
            val entity = patrimonioDao.buscarPorNumero(numero)
            
            if (entity != null) {
                // ✅ v2.8: Formatar data da coleta se existir
                val dataColetaFormatada = entity.dataColeta?.let { timestamp ->
                    try {
                        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                        sdf.format(java.util.Date(timestamp))
                    } catch (e: Exception) {
                        null
                    }
                }
                
                val patrimonio = Patrimonio(
                    id = entity.id.toLong(),
                    numeroPatrimonio = entity.numero,
                    descricao = entity.descricao,
                    marca = entity.marca,
                    modelo = entity.modelo,
                    numeroSerie = entity.numeroSerie,
                    estado = entity.status ?: entity.estado,
                    valor = entity.valor,
                    setorId = entity.setorId?.toLong(),
                    setorNome = entity.setorNome,
                    salaId = entity.idSala?.toLong() ?: entity.salaId?.toLong(),
                    salaNome = entity.nomeSala ?: entity.salaNome,
                    responsavelId = entity.idResponsavel?.toLong() ?: entity.responsavelId?.toLong(),
                    responsavelNome = entity.nomeResponsavel ?: entity.responsavelNome,
                    qrCode = entity.numero,
                    observacoes = entity.observacoes,
                    coletado = entity.coletado,
                    dataColeta = entity.dataColeta?.toString(),
                    coletadoPor = entity.coletadoPor,  // ✅ CORREÇÃO: Usar campo da entity
                    dataColetaFormatada = dataColetaFormatada,  // ✅ CORREÇÃO: Formatar data
                    observacoesColeta = entity.observacoesColeta,  // ✅ CORREÇÃO: Usar campo da entity
                    sincronizado = true,
                    servidorId = null
                )
                
                Log.d(TAG, "✓ Patrimônio encontrado no banco local")
                Log.d(TAG, "  Coletado: ${entity.coletado}")
                Log.d(TAG, "  Coletado por: ${entity.coletadoPor}")
                Log.d(TAG, "  Data coleta: $dataColetaFormatada")
                Result.success(patrimonio)
            } else {
                Log.w(TAG, "Patrimônio não encontrado no banco local")
                Result.failure(Exception("Patrimônio não encontrado no banco local"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar patrimônio do banco local", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getSalas(): Result<List<Sala>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Buscando salas do banco local...")
            
            val entities = salaDao.buscarTodas()
            val salas = entities.map { entity ->
                Sala(
                    id = entity.id,
                    nome = entity.nome,
                    descricao = null,
                    andar = null,
                    bloco = null,
                    ativa = true
                )
            }
            
            Log.d(TAG, "✓ ${salas.size} salas obtidas do banco local")
            Result.success(salas)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar salas do banco local", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getSalaPorId(id: Int): Result<Sala> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Buscando sala $id do banco local...")
            
            val entity = salaDao.buscarPorId(id)
            
            if (entity != null) {
                val sala = Sala(
                    id = entity.id,
                    nome = entity.nome,
                    descricao = null,
                    andar = null,
                    bloco = null,
                    ativa = true
                )
                
                Log.d(TAG, "✓ Sala encontrada no banco local")
                Result.success(sala)
            } else {
                Log.w(TAG, "Sala não encontrada no banco local")
                Result.failure(Exception("Sala não encontrada no banco local"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar sala do banco local", e)
            Result.failure(e)
        }
    }
    
    override suspend fun buscarDescricoesNaoColetadas(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Buscando descrições não coletadas do banco local...")
            
            val descricoes = patrimonioDao.buscarDescricoesNaoColetadas()
            
            Log.d(TAG, "✓ ${descricoes.size} descrições encontradas no banco local")
            Result.success(descricoes)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar descrições do banco local", e)
            Result.failure(e)
        }
    }
    
    override suspend fun buscarPorDescricaoNaoColetados(descricao: String): Result<List<Patrimonio>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Buscando patrimônios por descrição '$descricao' do banco local...")
            
            val entities = patrimonioDao.buscarPorDescricaoNaoColetados(descricao)
            val patrimonios = entities.map { entity ->
                // ✅ v2.8: Formatar data da coleta se existir
                val dataColetaFormatada = entity.dataColeta?.let { timestamp ->
                    try {
                        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                        sdf.format(java.util.Date(timestamp))
                    } catch (e: Exception) {
                        null
                    }
                }
                
                Patrimonio(
                    id = entity.id.toLong(),
                    numeroPatrimonio = entity.numero,
                    descricao = entity.descricao,
                    marca = entity.marca,
                    modelo = entity.modelo,
                    numeroSerie = entity.numeroSerie,
                    estado = entity.status ?: entity.estado,
                    valor = entity.valor,
                    setorId = entity.setorId?.toLong(),
                    setorNome = entity.setorNome,
                    salaId = entity.idSala?.toLong() ?: entity.salaId?.toLong(),
                    salaNome = entity.nomeSala ?: entity.salaNome,
                    responsavelId = entity.idResponsavel?.toLong() ?: entity.responsavelId?.toLong(),
                    responsavelNome = entity.nomeResponsavel ?: entity.responsavelNome,
                    qrCode = entity.numero,
                    observacoes = entity.observacoes,
                    coletado = entity.coletado,
                    dataColeta = entity.dataColeta?.toString(),
                    coletadoPor = entity.coletadoPor,  // ✅ CORREÇÃO: Usar campo da entity
                    dataColetaFormatada = dataColetaFormatada,  // ✅ CORREÇÃO: Formatar data
                    observacoesColeta = entity.observacoesColeta,  // ✅ CORREÇÃO: Usar campo da entity
                    sincronizado = true,
                    servidorId = null
                )
            }
            
            Log.d(TAG, "✓ ${patrimonios.size} patrimônios encontrados no banco local")
            Result.success(patrimonios)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar patrimônios por descrição do banco local", e)
            Result.failure(e)
        }
    }
    
    override fun getSourceType(): DataSourceType = DataSourceType.LOCAL
    
    // ========================================
    // Métodos para Inventário por Sala
    // ========================================
    
    /**
     * Busca patrimônios por sala com filtro opcional de status de coleta e paginação.
     */
    override suspend fun buscarPorSala(
        salaId: Int,
        coletado: Boolean?,
        page: Int,
        pageSize: Int
    ): Result<List<Patrimonio>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Buscando patrimônios da sala $salaId (coletado=$coletado, page=$page)")
            
            val offset = page * pageSize
            val entities = patrimonioDao.buscarPorSala(salaId, coletado, pageSize, offset)
            
            val patrimonios = entities.map { entity ->
                // ✅ v2.8: Formatar data da coleta se existir
                val dataColetaFormatada = entity.dataColeta?.let { timestamp ->
                    try {
                        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                        sdf.format(java.util.Date(timestamp))
                    } catch (e: Exception) {
                        null
                    }
                }
                
                Patrimonio(
                    id = entity.id.toLong(),
                    numeroPatrimonio = entity.numero,
                    descricao = entity.descricao,
                    marca = entity.marca,
                    modelo = entity.modelo,
                    numeroSerie = entity.numeroSerie,
                    estado = entity.status ?: entity.estado,
                    valor = entity.valor,
                    setorId = entity.setorId?.toLong(),
                    setorNome = entity.setorNome,
                    salaId = entity.idSala?.toLong() ?: entity.salaId?.toLong(),
                    salaNome = entity.nomeSala ?: entity.salaNome,
                    responsavelId = entity.idResponsavel?.toLong() ?: entity.responsavelId?.toLong(),
                    responsavelNome = entity.nomeResponsavel ?: entity.responsavelNome,
                    qrCode = entity.numero,
                    observacoes = entity.observacoes,
                    coletado = entity.coletado,
                    dataColeta = entity.dataColeta?.toString(),
                    coletadoPor = entity.coletadoPor,  // ✅ CORREÇÃO: Usar campo da entity
                    dataColetaFormatada = dataColetaFormatada,  // ✅ CORREÇÃO: Formatar data
                    observacoesColeta = entity.observacoesColeta,  // ✅ CORREÇÃO: Usar campo da entity
                    sincronizado = true,
                    servidorId = null
                )
            }
            
            Log.d(TAG, "✓ ${patrimonios.size} patrimônios encontrados na sala $salaId")
            Result.success(patrimonios)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar patrimônios da sala", e)
            Result.failure(e)
        }
    }
    
    /**
     * Conta total de patrimônios em uma sala.
     */
    override suspend fun contarPorSala(salaId: Int): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val total = patrimonioDao.contarPorSala(salaId)
            Log.d(TAG, "✓ Total: $total patrimônios na sala $salaId")
            Result.success(total)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao contar patrimônios da sala", e)
            Result.failure(e)
        }
    }
    
    /**
     * Conta patrimônios coletados em uma sala.
     */
    override suspend fun contarColetadosPorSala(salaId: Int): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val total = patrimonioDao.contarColetadosPorSala(salaId)
            Log.d(TAG, "✓ Total coletados: $total na sala $salaId")
            Result.success(total)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao contar patrimônios coletados", e)
            Result.failure(e)
        }
    }
    
    // ========================================
    // Métodos para Busca Rápida de Patrimônio
    // ========================================
    
    /**
     * Busca patrimônios por query (número, descrição ou nome da sala)
     * @see Requirements 1.1
     */
    override suspend fun buscarPorQuery(query: String): Result<List<Patrimonio>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Buscando patrimônios por query: $query")
            
            val entities = patrimonioDao.buscarPorQuery(query)
            val patrimonios = mapEntitiesToPatrimonios(entities)
            
            Log.d(TAG, "✓ ${patrimonios.size} patrimônios encontrados para '$query'")
            Result.success(patrimonios)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar patrimônios por query", e)
            Result.failure(e)
        }
    }
    
    /**
     * Busca patrimônios coletados por query
     * @see Requirements 3.1
     */
    override suspend fun buscarColetadosPorQuery(query: String): Result<List<Patrimonio>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Buscando patrimônios COLETADOS por query: $query")
            
            val entities = patrimonioDao.buscarColetadosPorQuery(query)
            val patrimonios = mapEntitiesToPatrimonios(entities)
            
            Log.d(TAG, "✓ ${patrimonios.size} patrimônios coletados encontrados para '$query'")
            Result.success(patrimonios)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar patrimônios coletados", e)
            Result.failure(e)
        }
    }
    
    /**
     * Busca patrimônios pendentes (não coletados) por query
     * @see Requirements 3.2
     */
    override suspend fun buscarPendentesPorQuery(query: String): Result<List<Patrimonio>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Buscando patrimônios PENDENTES por query: $query")
            
            val entities = patrimonioDao.buscarPendentesPorQuery(query)
            val patrimonios = mapEntitiesToPatrimonios(entities)
            
            Log.d(TAG, "✓ ${patrimonios.size} patrimônios pendentes encontrados para '$query'")
            Result.success(patrimonios)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar patrimônios pendentes", e)
            Result.failure(e)
        }
    }
    
    /**
     * Busca patrimônios com divergência por query
     * @see Requirements 3.3
     */
    override suspend fun buscarDivergenciasPorQuery(query: String, inventarioId: Int): Result<List<Patrimonio>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Buscando patrimônios com DIVERGÊNCIA por query: $query")
            
            val entities = patrimonioDao.buscarDivergenciasPorQuery(query, inventarioId)
            val patrimonios = mapEntitiesToPatrimonios(entities)
            
            Log.d(TAG, "✓ ${patrimonios.size} patrimônios com divergência encontrados para '$query'")
            Result.success(patrimonios)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar patrimônios com divergência", e)
            Result.failure(e)
        }
    }
    
    /**
     * Função auxiliar para mapear entities para domain models
     */
    private fun mapEntitiesToPatrimonios(entities: List<com.inventario.mobile.data.local.entity.PatrimonioEntity>): List<Patrimonio> {
        return entities.map { entity ->
            val dataColetaFormatada = entity.dataColeta?.let { timestamp ->
                try {
                    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                    sdf.format(java.util.Date(timestamp))
                } catch (e: Exception) {
                    null
                }
            }
            
            Patrimonio(
                id = entity.id.toLong(),
                numeroPatrimonio = entity.numero,
                descricao = entity.descricao,
                marca = entity.marca,
                modelo = entity.modelo,
                numeroSerie = entity.numeroSerie,
                estado = entity.status ?: entity.estado,
                valor = entity.valor,
                setorId = entity.setorId?.toLong(),
                setorNome = entity.setorNome,
                salaId = entity.idSala?.toLong() ?: entity.salaId?.toLong(),
                salaNome = entity.nomeSala ?: entity.salaNome,
                responsavelId = entity.idResponsavel?.toLong() ?: entity.responsavelId?.toLong(),
                responsavelNome = entity.nomeResponsavel ?: entity.responsavelNome,
                qrCode = entity.numero,
                observacoes = entity.observacoes,
                coletado = entity.coletado,
                dataColeta = entity.dataColeta?.toString(),
                coletadoPor = entity.coletadoPor,
                dataColetaFormatada = dataColetaFormatada,
                observacoesColeta = entity.observacoesColeta,
                sincronizado = true,
                servidorId = null
            )
        }
    }
}
