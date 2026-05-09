package com.inventario.mobile.domain.usecase

import android.util.Log
import com.inventario.mobile.api.PatrimonioApi
import com.inventario.mobile.data.local.dao.ColetaDao
import com.inventario.mobile.data.local.dao.PatrimonioDao
import com.inventario.mobile.data.local.entity.ColetaEntity
import com.inventario.mobile.data.local.entity.PatrimonioEntity
import com.inventario.mobile.domain.model.ExportConfig
import com.inventario.mobile.domain.model.ExportFilter
import com.inventario.mobile.domain.model.ExportFormat
import com.inventario.mobile.domain.model.ExportResult
import com.inventario.mobile.domain.model.Patrimonio
import com.inventario.mobile.domain.model.Sala
import com.inventario.mobile.domain.repository.ExportRepository
import com.inventario.mobile.domain.repository.PatrimonioRepository
import com.inventario.mobile.utils.PreferencesManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Use Case para gerar relatórios em diferentes formatos (PDF, Excel, CSV)
 */
class GerarRelatorioUseCase @Inject constructor(
    private val exportRepository: ExportRepository,
    private val patrimonioRepository: PatrimonioRepository,
    private val patrimonioApi: PatrimonioApi,
    private val patrimonioDao: PatrimonioDao,
    private val coletaDao: ColetaDao,
    private val preferencesManager: PreferencesManager
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
            
            // 2. Buscar patrimônios — se online, priorizar servidor pois só ele
            // possui dados de auditoria completos (coletadoPor, dataColeta,
            // localizacaoEncontrada, estadoEncontrado) mesmo para coletas já
            // sincronizadas. Se offline, cai para o banco local.
            //
            // v2.20.7: antes buscava local primeiro e só ia ao servidor se
            // local estivesse vazio. Isso deixava o relatório offline sem os
            // dados de auditoria, porque `limparSincronizadas()` apaga a
            // tabela `coleta` após sync, perdendo o histórico local.
            var patrimonios: List<Patrimonio> = if (!isOffline) {
                Log.d(TAG, "Modo online — buscando do servidor primeiro")
                val doServidor = buscarPatrimoniosServidor(sala.id.toInt(), coletadoFilter)
                if (doServidor.isNotEmpty()) {
                    Log.d(TAG, "Patrimônios do servidor: ${doServidor.size}")
                    doServidor
                } else {
                    Log.w(TAG, "Servidor retornou vazio — caindo para banco local")
                    buscarPatrimoniosLocal(sala.id.toInt(), coletadoFilter)
                }
            } else {
                Log.d(TAG, "Modo offline — buscando do banco local")
                buscarPatrimoniosLocal(sala.id.toInt(), coletadoFilter)
            }
            Log.d(TAG, "Total de patrimônios antes de enriquecer: ${patrimonios.size}")

            // 2.1. Enriquecer com dados de coleta local (safety net)
            // Garante que campos de auditoria sejam preenchidos mesmo se vier
            // apenas do banco local — útil em modo offline ou quando o servidor
            // está degradado.
            if (patrimonios.isNotEmpty()) {
                patrimonios = enriquecerComDadosColeta(patrimonios)
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
            // Buscar em lotes para evitar OutOfMemoryError com Int.MAX_VALUE
            val pageSize = 500
            val allPatrimonios = mutableListOf<Patrimonio>()
            var page = 0

            while (true) {
                val patrimoniosResult = patrimonioRepository.buscarPorSala(
                    salaId = salaId,
                    coletado = coletado,
                    page = page,
                    pageSize = pageSize
                )

                if (patrimoniosResult.isSuccess) {
                    val batch = patrimoniosResult.getOrNull() ?: emptyList()
                    allPatrimonios.addAll(batch)
                    if (batch.size < pageSize) break // última página
                    page++
                } else {
                    break
                }
            }

            allPatrimonios
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
                        // v2.20.8 fix: mapear todos os campos de localização/responsável.
                        // Antes faltavam nomeSala/nomeSetor/idResponsavel/nomeResponsavel,
                        // o que resultava em colunas "Responsável" e "Sala" vazias no
                        // relatório exportado quando os dados vinham do servidor.
                        idSetor = p.setorId?.toInt(),
                        nomeSetor = p.setorNome,
                        idSala = p.salaId?.toInt(),
                        nomeSala = p.salaNome,
                        idResponsavel = p.responsavelId?.toInt(),
                        nomeResponsavel = p.responsavelNome,
                        qrCode = p.qrCode ?: p.numeroPatrimonio,
                        sincronizado = true,
                        coletado = p.coletado,
                        dataColeta = p.dataColeta,
                        // coletorId no domain é o ID de quem realizou a coleta —
                        // não tem relação com responsavelId do patrimônio.
                        // O DTO do servidor não traz esse ID separado, então fica null.
                        coletorId = null,
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
                    nomeSala = p.salaNome,
                    idResponsavel = p.responsavelId?.toInt(),
                    nomeResponsavel = p.responsavelNome,
                    observacoes = p.observacoes,
                    coletado = p.coletado,
                    dataColeta = p.dataColeta?.toLongOrNull(),
                    coletadoPor = p.coletadoPor,
                    observacoesColeta = null,
                    // v2.20.7: persistir dados de auditoria vindos do servidor
                    localizacaoEncontrada = p.localizacaoEncontrada,
                    estadoEncontrado = p.estadoEncontrado,
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
     * Enriquece a lista de patrimônios locais com dados de coleta.
     *
     * **Contexto:** Quando uma coleta é registrada localmente, a tabela `patrimonio`
     * recebe apenas `coletado = 1`. Os detalhes da coleta (data, usuário, localização
     * onde foi encontrado, estado encontrado) ficam apenas na tabela `coleta`.
     *
     * Sem esse merge, os relatórios exportados mostram "-" nas colunas de auditoria
     * mesmo para patrimônios efetivamente coletados. Esta função busca a coleta mais
     * recente de cada patrimônio no inventário ativo e preenche os campos faltantes.
     *
     * Só considera coletas do inventário ativo para evitar "vazamento" de dados entre
     * inventários diferentes.
     */
    private suspend fun enriquecerComDadosColeta(
        patrimonios: List<Patrimonio>
    ): List<Patrimonio> {
        return try {
            val inventarioAtivoId = preferencesManager.getInventarioAtivoId() ?: 0
            if (inventarioAtivoId <= 0) {
                Log.w(TAG, "Inventário ativo não definido - exportando sem dados de coleta")
                return patrimonios
            }

            // Carrega todas as coletas do inventário ativo uma única vez e indexa por
            // idPatrimonio para lookup O(1). Para inventários grandes isso é muito mais
            // eficiente que consultar o DAO por patrimônio.
            val coletasDoInventario = coletaDao.buscarTodas(inventarioAtivoId)
            Log.d(TAG, "Coletas do inventário $inventarioAtivoId: ${coletasDoInventario.size}")

            // Em caso de múltiplas coletas (histórico) pegamos a mais recente
            val coletasPorPatrimonio: Map<Int, ColetaEntity> = coletasDoInventario
                .groupBy { it.idPatrimonio }
                .mapValues { (_, lista) -> lista.maxBy { it.dataColeta } }

            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))

            patrimonios.map { patrimonio ->
                val coleta = coletasPorPatrimonio[patrimonio.id]
                if (coleta != null) {
                    // Coleta encontrada - preencher campos de auditoria
                    val dataFormatada = try {
                        sdf.format(Date(coleta.dataColeta))
                    } catch (e: Exception) {
                        null
                    }
                    patrimonio.copy(
                        coletado = true,
                        dataColeta = coleta.dataColeta.toString(),
                        dataColetaFormatada = dataFormatada,
                        coletadoPor = coleta.nomeUsuario.ifBlank { patrimonio.coletadoPor },
                        // `nomeSala` na coleta representa a localização onde o item FOI
                        // encontrado (pode ser diferente da sala original do patrimônio)
                        localizacaoEncontrada = coleta.nomeSala ?: patrimonio.localizacaoEncontrada,
                        estadoEncontrado = coleta.estadoPatrimonio ?: patrimonio.estadoEncontrado
                    )
                } else {
                    patrimonio
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao enriquecer patrimônios com dados de coleta: ${e.message}", e)
            // Falha no enriquecimento não deve impedir a exportação — retorna lista original
            patrimonios
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
