package com.inventario.mobile.data.repository

import com.inventario.mobile.data.local.dao.HistoricoScanDao
import com.inventario.mobile.data.local.entity.HistoricoScanEntity
import com.inventario.mobile.data.mapper.HistoricoScanMapper
import com.inventario.mobile.domain.model.HistoricoScan
import com.inventario.mobile.domain.model.TipoAcesso
import com.inventario.mobile.domain.repository.EstatisticasHistorico
import com.inventario.mobile.domain.repository.HistoricoScanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação do repositório de histórico de scans
 * 
 * @since v2.11.0
 */
@Singleton
class HistoricoScanRepositoryImpl @Inject constructor(
    private val historicoDao: HistoricoScanDao,
    private val mapper: HistoricoScanMapper
) : HistoricoScanRepository {
    
    companion object {
        private const val TAG = "HistoricoScanRepo"
        private const val MAX_REGISTROS = 50
    }
    
    override suspend fun registrarAcesso(
        numeroPatrimonio: String,
        descricao: String?,
        nomeSala: String?,
        salaId: Int?,
        tipoAcesso: TipoAcesso,
        estadoPatrimonio: String?,
        jaEstaColetado: Boolean
    ): Long {
        android.util.Log.d(TAG, "Registrando acesso: $numeroPatrimonio (${tipoAcesso.name})")
        
        val entity = HistoricoScanEntity(
            numeroPatrimonio = numeroPatrimonio,
            descricao = descricao,
            nomeSala = nomeSala,
            salaId = salaId,
            tipoAcesso = tipoAcesso.name,
            estadoPatrimonio = estadoPatrimonio,
            jaEstaColetado = jaEstaColetado,
            foiColetado = false,
            timestamp = System.currentTimeMillis()
        )
        
        val id = historicoDao.inserir(entity)
        
        // Limpar registros antigos automaticamente
        val total = historicoDao.contar()
        if (total > MAX_REGISTROS) {
            historicoDao.limparAntigos(MAX_REGISTROS)
            android.util.Log.d(TAG, "Limpeza automática: mantendo últimos $MAX_REGISTROS registros")
        }
        
        android.util.Log.d(TAG, "✓ Acesso registrado com ID: $id")
        return id
    }
    
    override suspend fun marcarComoColetado(numeroPatrimonio: String) {
        android.util.Log.d(TAG, "Marcando como coletado: $numeroPatrimonio")
        historicoDao.marcarComoColetado(numeroPatrimonio)
    }
    
    override suspend fun buscarUltimos(limite: Int): List<HistoricoScan> {
        val entities = historicoDao.buscarUltimos(limite)
        return mapper.toDomainList(entities)
    }
    
    override fun observarUltimos(limite: Int): Flow<List<HistoricoScan>> {
        return historicoDao.observarUltimos(limite).map { entities ->
            mapper.toDomainList(entities)
        }
    }
    
    override suspend fun buscarDeHoje(): List<HistoricoScan> {
        val inicioHoje = getInicioDeHoje()
        val entities = historicoDao.buscarDeHoje(inicioHoje)
        return mapper.toDomainList(entities)
    }
    
    override suspend fun getEstatisticasHoje(): EstatisticasHistorico {
        val inicioHoje = getInicioDeHoje()
        val totalScans = historicoDao.contarDeHoje(inicioHoje)
        val totalColetas = historicoDao.contarColetasDeHoje(inicioHoje)
        
        return EstatisticasHistorico(
            totalScansHoje = totalScans,
            totalColetasHoje = totalColetas,
            totalConsultasHoje = totalScans - totalColetas
        )
    }
    
    override suspend fun limparAntigos(manter: Int) {
        historicoDao.limparAntigos(manter)
        android.util.Log.d(TAG, "Registros antigos limpos, mantendo últimos $manter")
    }
    
    override suspend fun limparTudo() {
        historicoDao.limparTudo()
        android.util.Log.d(TAG, "Todo histórico limpo")
    }
    
    override suspend fun buscarColetados(limite: Int): List<HistoricoScan> {
        val entities = historicoDao.buscarColetados(limite)
        return mapper.toDomainList(entities)
    }
    
    override suspend fun buscarApenasConsultados(limite: Int): List<HistoricoScan> {
        val entities = historicoDao.buscarApenasConsultados(limite)
        return mapper.toDomainList(entities)
    }
    
    /**
     * Retorna timestamp do início do dia atual (00:00:00)
     */
    private fun getInicioDeHoje(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}
