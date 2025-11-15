package com.inventario.mobile.data.mapper

import android.util.Log
import com.inventario.mobile.data.local.dao.PatrimonioDao
import com.inventario.mobile.data.local.entity.ColetaEntity
import com.inventario.mobile.domain.model.Coleta
import javax.inject.Inject

/**
 * Mapper: Converte entre Entity (Room) e Model (Domain)
 * 
 * ATUALIZADO: Agora preenche campos completos ao converter para Entity
 */
class ColetaMapper @Inject constructor(
    private val patrimonioDao: PatrimonioDao
) {
    companion object {
        private const val TAG = "ColetaMapper"
    }
    
    fun toDomain(entity: ColetaEntity): Coleta {
        return Coleta(
            id = entity.id,
            patrimonioId = entity.idPatrimonio.toLong(),
            usuarioId = entity.idUsuario.toLong(),
            dataColeta = entity.dataColeta,
            localizacaoAtual = entity.nomeSala,
            observacoes = entity.observacao,
            status = "COLETADO", // Status padrão
            latitude = entity.latitude,
            longitude = entity.longitude,
            sincronizado = entity.sincronizado
        )
    }
    
    /**
     * Converte Domain para Entity, preenchendo campos completos
     * Busca dados do patrimônio se necessário
     */
    suspend fun toEntity(domain: Coleta, idInventario: Int = 0): ColetaEntity {
        // Buscar dados do patrimônio para preencher campos
        val patrimonio = try {
            patrimonioDao.buscarPorId(domain.patrimonioId.toInt())
        } catch (e: Exception) {
            Log.w(TAG, "Erro ao buscar patrimônio ${domain.patrimonioId}", e)
            null
        }
        
        return ColetaEntity(
            id = domain.id,
            idPatrimonio = domain.patrimonioId.toInt(),
            numeroPatrimonio = patrimonio?.numero ?: "", // ✅ Preenchido do banco
            idInventario = idInventario,
            idSala = patrimonio?.idSala,
            nomeSala = patrimonio?.nomeSala ?: domain.localizacaoAtual, // ✅ Preenchido
            idResponsavel = patrimonio?.idResponsavel,
            nomeResponsavel = patrimonio?.nomeResponsavel,
            observacao = domain.observacoes,
            estadoPatrimonio = null,
            latitude = domain.latitude,
            longitude = domain.longitude,
            dataColeta = domain.dataColeta,
            idUsuario = domain.usuarioId.toInt(),
            nomeUsuario = "Usuário ${domain.usuarioId}", // TODO: Buscar nome real
            sincronizado = domain.sincronizado
        )
    }
    
    /**
     * Converte Domain para Entity sem buscar dados adicionais
     * Usado quando os dados já estão completos
     */
    fun toEntitySimple(domain: Coleta, idInventario: Int = 0): ColetaEntity {
        return ColetaEntity(
            id = domain.id,
            idPatrimonio = domain.patrimonioId.toInt(),
            numeroPatrimonio = "",
            idInventario = idInventario,
            idSala = null,
            nomeSala = domain.localizacaoAtual,
            idResponsavel = null,
            nomeResponsavel = null,
            observacao = domain.observacoes,
            estadoPatrimonio = null,
            latitude = domain.latitude,
            longitude = domain.longitude,
            dataColeta = domain.dataColeta,
            idUsuario = domain.usuarioId.toInt(),
            nomeUsuario = "",
            sincronizado = domain.sincronizado
        )
    }
    
    fun toDomainList(entities: List<ColetaEntity>): List<Coleta> {
        return entities.map { toDomain(it) }
    }
}

