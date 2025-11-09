package com.inventario.mobile.data.strategy

import android.util.Log
import com.inventario.mobile.data.local.dao.PatrimonioDao
import com.inventario.mobile.data.local.dao.SalaDao
import com.inventario.mobile.model.Patrimonio
import com.inventario.mobile.model.Sala
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
            val patrimoniosCount = patrimonioDao.count()
            val salasCount = salaDao.count()
            
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
            
            val entities = patrimonioDao.getAll()
            val patrimonios = entities.map { entity ->
                Patrimonio(
                    id = entity.id,
                    numero = entity.numero,
                    descricao = entity.descricao,
                    idSala = entity.idSala,
                    nomeSala = entity.nomeSala,
                    estado = entity.estado,
                    valor = entity.valor,
                    dataAquisicao = entity.dataAquisicao
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
            
            val entity = patrimonioDao.getByNumero(numero)
            
            if (entity != null) {
                val patrimonio = Patrimonio(
                    id = entity.id,
                    numero = entity.numero,
                    descricao = entity.descricao,
                    idSala = entity.idSala,
                    nomeSala = entity.nomeSala,
                    estado = entity.estado,
                    valor = entity.valor,
                    dataAquisicao = entity.dataAquisicao
                )
                
                Log.d(TAG, "✓ Patrimônio encontrado no banco local")
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
            
            val entities = salaDao.getAll()
            val salas = entities.map { entity ->
                Sala(
                    id = entity.id,
                    nome = entity.nome,
                    descricao = entity.descricao,
                    andar = entity.andar,
                    bloco = entity.bloco,
                    capacidade = entity.capacidade,
                    ativa = entity.ativa
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
            
            val entity = salaDao.getById(id)
            
            if (entity != null) {
                val sala = Sala(
                    id = entity.id,
                    nome = entity.nome,
                    descricao = entity.descricao,
                    andar = entity.andar,
                    bloco = entity.bloco,
                    capacidade = entity.capacidade,
                    ativa = entity.ativa
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
    
    override fun getSourceType(): DataSourceType = DataSourceType.LOCAL
}
