package com.inventario.mobile.domain.usecase

import android.util.Log
import com.inventario.mobile.api.PatrimonioApi
import com.inventario.mobile.data.local.dao.PatrimonioDao
import com.inventario.mobile.data.local.entity.PatrimonioEntity
import com.inventario.mobile.domain.model.ExportConfig
import com.inventario.mobile.domain.model.ExportFilter
import com.inventario.mobile.domain.model.ExportFormat
import com.inventario.mobile.domain.model.ExportResult
import com.inventario.mobile.domain.model.Patrimonio
import com.inventario.mobile.domain.model.Sala
import com.inventario.mobile.domain.repository.ExportRepository
import com.inventario.mobile.domain.repository.PatrimonioRepository
import javax.inject.Inject

/**
 * Use Case para gerar relatórios em diferentes formatos (PDF, Excel, CSV)
 */
class GerarRelatorioUseCase @Inject constructor(
    private val exportRepository: ExportRepository,
    private val patrimonioRepository: PatrimonioRepository,
    private val patrimonioApi: PatrimonioApi,
    private val patrimonioDao: PatrimonioDao
) {
    companion object {
        private const val TAG = "GerarRelatorioUseCase"
    }
    
    /**
     * Gera um relatório com os patrimônios da sala selecionada
     * 
     * @param sala Sala para exportar
     * @param filter Filtro de status (TODOS, COLETADOS, NAO_COLETADOS)
     * @param format Formato de exportação (PDF, EXCEL, CSV)
     * @param isOffline Se está em modo offline
     * @return Result com ExportResult em caso de sucesso
     */
    suspend operator fun invoke(
        sala: Sala,
        filter: ExportFilter,
        format: ExportFormat,
        isOffline: Boolean
    ): Result<ExportResult> {
        return try {
            Log.d(TAG, "Gerando relatório para sala ${sala.nome} (id=${sala.id})")
            Log.d(TAG, "Filtro: $filter, Formato: $format, Offline: $isOffline")
            
            // 1. Determinar filtro de coleta
            val coletadoFilter: Boolean? = when (filter) {
                ExportFilter.TODOS -> null
                ExportFilter.COLETADOS -> true
                ExportFilter.NAO_COLETADOS -> false
            }
            
            // 2. Buscar patrimônios - primeiro do banco local
            var patrimonios = buscarPatrimoniosLocal(sala.id.toInt(), coletadoFilter)
            Log.d(TAG, "Patrimônios no banco local: ${patrimonios.size}")
            
            // 3. Se banco local estiver vazio, buscar do servidor
            if (patrimonios.isEmpty() && !isOffline) {
                Log.d(TAG, "Banco local vazio, buscando do servidor...")
                patrimonios = buscarPatrimoniosServidor(sala.id.toInt(), coletadoFilter)
                Log.d(TAG, "Patrimônios do servidor: ${patrimonios.size}")
            }
            
            // 4. Verificar se há dados para exportar
            if (patrimonios.isEmpty()) {
                Log.w(TAG, "Nenhum patrimônio encontrado para exportar")
                return Result.failure(NoDataException("Nenhum patrimônio encontrado com os filtros selecionados"))
            }
            
            Log.d(TAG, "Total de patrimônios para exportar: ${patrimonios.size}")
            
            // 5. Gerar relatório no formato especificado
            exportRepository.generateReport(
                patrimonios = patrimonios,
                sala = sala,
                filter = filter,
                format = format,
                isOffline = isOffline
            )
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao gerar relatório: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Busca patrimônios do banco local
     */
    private suspend fun buscarPatrimoniosLocal(salaId: Int, coletado: Boolean?): List<Patrimonio> {
        return try {
            val patrimoniosResult = patrimonioRepository.buscarPorSala(
                salaId = salaId,
                coletado = coletado,
                page = 0,
                pageSize = Int.MAX_VALUE
            )
            
            if (patrimoniosResult.isSuccess) {
                patrimoniosResult.getOrNull() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar patrimônios locais: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Busca patrimônios do servidor e salva no banco local
     */
    private suspend fun buscarPatrimoniosServidor(salaId: Int, coletado: Boolean?): List<Patrimonio> {
        return try {
            Log.d(TAG, "Buscando patrimônios do servidor para sala $salaId...")
            
            val response = patrimonioApi.buscarPorSala(
                salaId = salaId,
                page = 0,
                size = 1000, // Buscar todos
                coletado = coletado
            )
            
            if (response.isSuccessful && response.body()?.success == true) {
                val patrimoniosApi = response.body()?.data ?: emptyList()
                Log.d(TAG, "✓ ${patrimoniosApi.size} patrimônios do servidor")
                
                // Salvar no banco local para cache
                salvarNoBancoLocal(patrimoniosApi, salaId)
                
                // Converter para domain model
                patrimoniosApi.map { p ->
                    Patrimonio(
                        id = p.id.toInt(),
                        numeroPatrimonio = p.numeroPatrimonio,
                        descricao = p.descricao,
                        marca = p.marca,
                        modelo = p.modelo,
                        numeroSerie = p.numeroSerie,
                        estado = p.estado ?: "ATIVO",
                        valor = p.valor,
                        dataAquisicao = null,
                        observacoes = p.observacoes,
                        idSetor = p.setorId?.toInt(),
                        idSala = p.salaId?.toInt(),
                        qrCode = p.qrCode ?: p.numeroPatrimonio,
                        sincronizado = true,
                        coletado = p.coletado,
                        dataColeta = p.dataColeta,
                        coletorId = p.responsavelId,
                        coletadoPor = p.coletadoPor,
                        dataColetaFormatada = p.dataColetaFormatada,
                        localizacaoEncontrada = p.localizacaoEncontrada,
                        estadoEncontrado = p.estadoEncontrado
                    )
                }
            } else {
                Log.w(TAG, "Falha ao buscar do servidor: ${response.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar patrimônios do servidor: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Salva patrimônios no banco local para cache
     */
    private suspend fun salvarNoBancoLocal(patrimonios: List<com.inventario.mobile.data.model.Patrimonio>, salaId: Int) {
        try {
            val entities = patrimonios.map { p ->
                PatrimonioEntity(
                    id = p.id,
                    numero = p.numeroPatrimonio,
                    numeroPatrimonio = p.numeroPatrimonio,
                    descricao = p.descricao,
                    marca = p.marca,
                    modelo = p.modelo,
                    numeroSerie = p.numeroSerie,
                    status = p.estado,
                    estado = p.estado,
                    valor = p.valor,
                    setorId = p.setorId?.toInt(),
                    setorNome = p.setorNome,
                    idSala = salaId,
                    salaId = salaId,
                    nomeSala = p.salaNome,
                    salaNome = p.salaNome,
                    idResponsavel = p.responsavelId?.toInt(),
                    responsavelId = p.responsavelId?.toInt(),
                    nomeResponsavel = p.responsavelNome,
                    responsavelNome = p.responsavelNome,
                    observacoes = p.observacoes,
                    coletado = p.coletado,
                    dataColeta = p.dataColeta?.toLongOrNull(),
                    coletadoPor = p.coletadoPor,
                    observacoesColeta = null,
                    dataUltimaAtualizacao = System.currentTimeMillis()
                )
            }
            patrimonioDao.inserirTodos(entities)
            Log.d(TAG, "✓ ${entities.size} patrimônios salvos no banco local")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar patrimônios no banco local: ${e.message}", e)
        }
    }
    
    /**
     * Gera relatório usando ExportConfig (compatibilidade com código existente)
     */
    suspend fun fromConfig(config: ExportConfig, format: ExportFormat): Result<ExportResult> {
        return invoke(
            sala = config.sala,
            filter = config.filter,
            format = format,
            isOffline = config.isOffline
        )
    }
}
