package com.inventario.mobile.data.mapper

import android.util.Log
import com.inventario.mobile.data.local.dao.PatrimonioDao
import com.inventario.mobile.data.local.entity.ColetaEntity
import com.inventario.mobile.domain.model.Coleta
import com.inventario.mobile.utils.PreferencesManager
import javax.inject.Inject

/**
 * Mapper: Converte entre Entity (Room) e Model (Domain)
 * 
 * ATUALIZADO: Agora preenche campos completos ao converter para Entity
 * v2.0: Usa PreferencesManager para obter inventário ativo e nome do usuário
 */
class ColetaMapper @Inject constructor(
    private val patrimonioDao: PatrimonioDao,
    private val preferencesManager: PreferencesManager
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
     * v2.0: Usa PreferencesManager para obter inventário ativo e nome do usuário
     */
    suspend fun toEntity(domain: Coleta, idInventario: Int? = null): ColetaEntity {
        // Buscar dados do patrimônio para preencher campos
        val patrimonio = try {
            patrimonioDao.buscarPorId(domain.patrimonioId.toInt())
        } catch (e: Exception) {
            Log.w(TAG, "Erro ao buscar patrimônio ${domain.patrimonioId}", e)
            null
        }
        
        // ✅ Obter ID do inventário ativo do PreferencesManager
        val inventarioAtivoId = idInventario ?: preferencesManager.getInventarioAtivoId() ?: 0
        
        // ✅ Obter nome do usuário do PreferencesManager
        val nomeUsuario = preferencesManager.getUserName().takeIf { it.isNotEmpty() }
            ?: "Usuário ${domain.usuarioId}"
        
        if (inventarioAtivoId == 0) {
            Log.w(TAG, "⚠️ Inventário ativo não encontrado! Usando 0 como fallback")
        }
        
        return ColetaEntity(
            id = domain.id,
            idPatrimonio = domain.patrimonioId.toInt(),
            numeroPatrimonio = patrimonio?.numero ?: "", // ✅ Preenchido do banco
            idInventario = inventarioAtivoId, // ✅ Do PreferencesManager
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
            nomeUsuario = nomeUsuario, // ✅ Do PreferencesManager
            sincronizado = domain.sincronizado
        )
    }
    
    /**
     * Converte Domain para Entity sem buscar dados adicionais
     * Usado quando os dados já estão completos
     * v2.0: Usa PreferencesManager para obter inventário ativo
     */
    fun toEntitySimple(domain: Coleta, idInventario: Int? = null): ColetaEntity {
        // ✅ Obter ID do inventário ativo do PreferencesManager
        val inventarioAtivoId = idInventario ?: preferencesManager.getInventarioAtivoId() ?: 0
        
        // ✅ Obter nome do usuário do PreferencesManager
        val nomeUsuario = preferencesManager.getUserName().takeIf { it.isNotEmpty() }
            ?: "Usuário ${domain.usuarioId}"
        
        return ColetaEntity(
            id = domain.id,
            idPatrimonio = domain.patrimonioId.toInt(),
            numeroPatrimonio = "",
            idInventario = inventarioAtivoId, // ✅ Do PreferencesManager
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
            nomeUsuario = nomeUsuario, // ✅ Do PreferencesManager
            sincronizado = domain.sincronizado
        )
    }
    
    fun toDomainList(entities: List<ColetaEntity>): List<Coleta> {
        return entities.map { toDomain(it) }
    }
}

