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
 * v2.0: Usa PreferencesManager para obter inventário ativo e nome do usuário
 * v2.21: Corrigido mapeamento de fotoPath — usa entity.fotoPath (caminho de arquivo)
 *        em vez de entity.fotoPatrimonio (Base64 legado). O campo fotoPatrimonio
 *        é mantido apenas para compatibilidade com dados antigos.
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
            numeroPatrimonio = entity.numeroPatrimonio,
            usuarioId = entity.idUsuario.toLong(),
            salaId = entity.idSala,
            dataColeta = entity.dataColeta,
            localizacaoAtual = entity.nomeSala,
            localizacaoEncontrada = entity.localizacaoEncontrada,
            observacoes = entity.observacao,
            status = if (entity.sincronizado) "SINCRONIZADO" else "PENDENTE",
            estadoEncontrado = entity.estadoPatrimonio,
            latitude = entity.latitude,
            longitude = entity.longitude,
            sincronizado = entity.sincronizado,
            tentativasSincronizacao = entity.tentativasSincronizacao,
            erroSincronizacao = entity.erroSincronizacao,
            // v2.7: Campos de item sem etiqueta
            semEtiqueta = entity.semEtiqueta,
            descricaoItemSemEtiqueta = entity.descricaoItemSemEtiqueta,
            categoriaItemSemEtiqueta = entity.categoriaItemSemEtiqueta,
            // v2.21: fotoPath = caminho do arquivo em disco (não Base64)
            fotoPath = entity.fotoPath,
            fotoThumbnailPath = entity.fotoThumbnailPath,
            tempoColetaSegundos = entity.tempoColetaSegundos,
            tempoScanSegundos = entity.tempoScanSegundos,
            tempoPreenchimentoSegundos = entity.tempoPreenchimentoSegundos,
            metodoColeta = entity.metodoColeta,
            tipoScan = entity.tipoScan,
            divergencia = entity.divergencia,
            motivoDivergencia = entity.motivoDivergencia
        )
    }

    /**
     * Converte Domain para Entity, preenchendo campos completos.
     * Busca dados do patrimônio se necessário.
     *
     * v2.3: PRIORIZA localizacaoAtual da coleta (onde foi realmente encontrado)
     * v2.4: GARANTE que usuarioId seja preenchido do PreferencesManager se não informado
     * v2.21: fotoPath salvo no campo correto (não em fotoPatrimonio)
     */
    suspend fun toEntity(domain: Coleta, idInventario: Int? = null): ColetaEntity {
        val patrimonio = try {
            patrimonioDao.buscarPorId(domain.patrimonioId.toInt())
        } catch (e: Exception) {
            Log.w(TAG, "Erro ao buscar patrimônio ${domain.patrimonioId}", e)
            null
        }

        val inventarioAtivoId = idInventario ?: preferencesManager.getInventarioAtivoId() ?: 0

        val usuarioIdFinal = if (domain.usuarioId > 0) {
            domain.usuarioId.toInt()
        } else {
            val savedUserId = preferencesManager.getUserId() ?: 0
            if (savedUserId > 0) {
                Log.d(TAG, "✓ Usando usuarioId do PreferencesManager: $savedUserId")
                savedUserId
            } else {
                Log.w(TAG, "⚠️ usuarioId não encontrado! Usando 0 como fallback")
                0
            }
        }

        val nomeUsuario = preferencesManager.getUserName().takeIf { it.isNotEmpty() }
            ?: "Usuário $usuarioIdFinal"

        if (inventarioAtivoId == 0) {
            Log.w(TAG, "⚠️ Inventário ativo não encontrado! Usando 0 como fallback")
        }

        val salaIdReal = domain.salaId ?: patrimonio?.idSala
        val localizacaoEncontradaReal = domain.localizacaoAtual
        val nomeSalaOrigem = patrimonio?.nomeSala

        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "MAPEANDO COLETA PARA ENTITY")
        Log.d(TAG, "Sala ID da coleta (atual): ${domain.salaId}")
        Log.d(TAG, "Sala ID do patrimônio (cadastrado): ${patrimonio?.idSala}")
        Log.d(TAG, "Sala ID FINAL (usado): $salaIdReal")
        Log.d(TAG, "localizacaoEncontrada: $localizacaoEncontradaReal")
        Log.d(TAG, "nomeSala (origem): $nomeSalaOrigem")
        Log.d(TAG, "fotoPath: ${domain.fotoPath}")
        Log.d(TAG, "═══════════════════════════════════════")

        return ColetaEntity(
            id = domain.id,
            idPatrimonio = domain.patrimonioId.toInt(),
            numeroPatrimonio = patrimonio?.numero ?: "",
            idInventario = inventarioAtivoId,
            idSala = salaIdReal,
            nomeSala = nomeSalaOrigem,
            localizacaoEncontrada = localizacaoEncontradaReal,
            idResponsavel = patrimonio?.idResponsavel,
            nomeResponsavel = patrimonio?.nomeResponsavel,
            observacao = domain.observacoes,
            estadoPatrimonio = domain.estadoEncontrado,
            latitude = domain.latitude,
            longitude = domain.longitude,
            dataColeta = domain.dataColeta,
            idUsuario = usuarioIdFinal,
            nomeUsuario = nomeUsuario,
            sincronizado = domain.sincronizado,
            // v2.7: Campos de item sem etiqueta
            semEtiqueta = domain.semEtiqueta,
            descricaoItemSemEtiqueta = domain.descricaoItemSemEtiqueta,
            categoriaItemSemEtiqueta = domain.categoriaItemSemEtiqueta,
            // v2.21: salvar no campo correto (fotoPath = arquivo em disco)
            fotoPath = domain.fotoPath,
            fotoThumbnailPath = domain.fotoThumbnailPath,
            fotoSincronizada = false,
            tempoColetaSegundos = domain.tempoColetaSegundos,
            tempoScanSegundos = domain.tempoScanSegundos,
            tempoPreenchimentoSegundos = domain.tempoPreenchimentoSegundos,
            metodoColeta = domain.metodoColeta,
            tipoScan = domain.tipoScan,
            divergencia = domain.divergencia,
            motivoDivergencia = domain.motivoDivergencia
        )
    }

    /**
     * Converte Domain para Entity sem buscar dados adicionais.
     * Usado quando os dados já estão completos.
     *
     * v2.4: GARANTE que usuarioId seja preenchido do PreferencesManager se não informado
     * v2.21: fotoPath salvo no campo correto
     */
    fun toEntitySimple(domain: Coleta, idInventario: Int? = null): ColetaEntity {
        val inventarioAtivoId = idInventario ?: preferencesManager.getInventarioAtivoId() ?: 0

        val usuarioIdFinal = if (domain.usuarioId > 0) {
            domain.usuarioId.toInt()
        } else {
            preferencesManager.getUserId() ?: 0
        }

        val nomeUsuario = preferencesManager.getUserName().takeIf { it.isNotEmpty() }
            ?: "Usuário $usuarioIdFinal"

        return ColetaEntity(
            id = domain.id,
            idPatrimonio = domain.patrimonioId.toInt(),
            numeroPatrimonio = "",
            idInventario = inventarioAtivoId,
            idSala = null,
            nomeSala = domain.localizacaoAtual,
            idResponsavel = null,
            nomeResponsavel = null,
            observacao = domain.observacoes,
            estadoPatrimonio = domain.estadoEncontrado,
            latitude = domain.latitude,
            longitude = domain.longitude,
            dataColeta = domain.dataColeta,
            idUsuario = usuarioIdFinal,
            nomeUsuario = nomeUsuario,
            sincronizado = domain.sincronizado,
            // v2.7: Campos de item sem etiqueta
            semEtiqueta = domain.semEtiqueta,
            descricaoItemSemEtiqueta = domain.descricaoItemSemEtiqueta,
            categoriaItemSemEtiqueta = domain.categoriaItemSemEtiqueta,
            // v2.21: salvar no campo correto (fotoPath = arquivo em disco)
            fotoPath = domain.fotoPath,
            fotoThumbnailPath = domain.fotoThumbnailPath,
            fotoSincronizada = false,
            tempoColetaSegundos = domain.tempoColetaSegundos,
            tempoScanSegundos = domain.tempoScanSegundos,
            tempoPreenchimentoSegundos = domain.tempoPreenchimentoSegundos,
            metodoColeta = domain.metodoColeta,
            tipoScan = domain.tipoScan,
            divergencia = domain.divergencia,
            motivoDivergencia = domain.motivoDivergencia
        )
    }

    fun toDomainList(entities: List<ColetaEntity>): List<Coleta> {
        return entities.map { toDomain(it) }
    }
}
