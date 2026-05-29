package com.inventario.mobile.domain.usecase

import android.util.Log
import com.inventario.mobile.data.local.dao.ColetaDao
import com.inventario.mobile.data.local.entity.ColetaEntity
import com.inventario.mobile.data.remote.api.ApiService
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Use Case: Sincronizar coletas do servidor para o banco local
 * 
 * Responsabilidade:
 * - Baixar coletas do servidor PostgreSQL
 * - Converter para ColetaEntity com todos os campos preenchidos
 * - Salvar no banco SQLite local
 * - Evitar duplicação
 */
class SincronizarColetasDoServidorUseCase @Inject constructor(
    private val apiService: ApiService,
    private val coletaDao: ColetaDao
) {
    companion object {
        private const val TAG = "SincronizarColetasUseCase"
    }
    
    /**
     * Sincroniza coletas do servidor
     * 
     * @return Result com quantidade de coletas sincronizadas ou erro
     */
    suspend operator fun invoke(): Result<SyncResult> {
        return try {
            Log.d(TAG, "═══════════════════════════════════════════")
            Log.d(TAG, "INICIANDO SINCRONIZAÇÃO DE COLETAS")
            
            // 1. Buscar coletas do servidor
            Log.d(TAG, "Buscando coletas do servidor...")
            val response = apiService.buscarTodasColetasSemPaginacao()
            
            if (!response.isSuccessful || response.body() == null) {
                Log.e(TAG, "Erro HTTP ${response.code()}")
                return Result.failure(Exception("Erro ao buscar coletas: HTTP ${response.code()}"))
            }
            
            val coletasAllResponse = response.body()!!
            
            if (!coletasAllResponse.success || coletasAllResponse.data == null) {
                Log.w(TAG, "API retornou success=false ou data=null")
                return Result.failure(Exception("Erro na resposta da API"))
            }
            
            val coletasServidor = coletasAllResponse.data.content
            Log.d(TAG, "✓ ${coletasServidor.size} coletas recebidas do servidor (de ${coletasAllResponse.data.totalElements} total)")
            
            // 2. Buscar coletas locais existentes
            val coletasLocais = coletaDao.buscarTodas()
            val idsLocais = coletasLocais.map { it.servidorId }.toSet()
            Log.d(TAG, "Coletas locais existentes: ${coletasLocais.size}")
            
            // 2.1 NOVO (v2.29.5): Remover coletas locais que foram excluídas no servidor
            // 
            // PROBLEMA: Se uma coleta foi excluída no servidor, ela permanecia no
            // banco local e bloqueava nova coleta do mesmo patrimônio.
            // 
            // SOLUÇÃO: Ao sincronizar, identificar coletas locais SINCRONIZADAS que
            // não estão mais no servidor (foram excluídas) e removê-las do banco
            // local. Coletas NÃO sincronizadas são preservadas (são pendentes locais).
            val idsServidor = coletasServidor.mapNotNull { it.id }.toSet()
            val coletasParaRemover = coletasLocais.filter { local ->
                local.sincronizado &&                                  // Apenas sincronizadas
                local.servidorId != null &&                            // Tem ID do servidor
                local.servidorId !in idsServidor                       // Não existe mais no servidor
            }
            
            var removidas = 0
            if (coletasParaRemover.isNotEmpty()) {
                Log.d(TAG, "🗑️ Removendo ${coletasParaRemover.size} coletas excluídas no servidor")
                for (coletaRemover in coletasParaRemover) {
                    try {
                        coletaDao.deletarPorId(coletaRemover.id)
                        removidas++
                        Log.d(TAG, "  ✓ Coleta local ${coletaRemover.id} (servidor=${coletaRemover.servidorId}) removida")
                    } catch (e: Exception) {
                        Log.e(TAG, "  ✗ Erro ao remover coleta local ${coletaRemover.id}", e)
                    }
                }
            }
            
            // 3. Converter e salvar coletas
            var novas = 0
            var atualizadas = 0
            var erros = 0
            
            for (dto in coletasServidor) {
                try {
                    // Converter DTO para Entity
                    val entity = convertDtoToEntity(dto)
                    
                    // Verificar se já existe
                    val jaExiste = dto.id != null && idsLocais.contains(dto.id)
                    
                    if (jaExiste) {
                        // Atualizar existente
                        coletaDao.inserir(entity)
                        atualizadas++
                        Log.d(TAG, "  ✓ Coleta ${dto.id} atualizada")
                    } else {
                        // Inserir nova
                        coletaDao.inserir(entity)
                        novas++
                        Log.d(TAG, "  ✓ Coleta ${dto.id} inserida")
                    }
                    
                } catch (e: Exception) {
                    erros++
                    Log.e(TAG, "  ✗ Erro ao processar coleta ${dto.id}", e)
                }
            }
            
            Log.d(TAG, "═══════════════════════════════════════════")
            Log.d(TAG, "SINCRONIZAÇÃO CONCLUÍDA")
            Log.d(TAG, "Novas: $novas")
            Log.d(TAG, "Atualizadas: $atualizadas")
            Log.d(TAG, "Removidas (excluídas no servidor): $removidas")
            Log.d(TAG, "Erros: $erros")
            Log.d(TAG, "═══════════════════════════════════════════")
            
            val result = SyncResult(
                total = coletasServidor.size,
                novas = novas,
                atualizadas = atualizadas,
                erros = erros,
                removidas = removidas
            )
            
            Result.success(result)
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro na sincronização", e)
            Result.failure(e)
        }
    }
    
    /**
     * Converte DTO do servidor para Entity local
     */
    private fun convertDtoToEntity(dto: com.inventario.mobile.data.remote.dto.MobileColetaResponseDto): ColetaEntity {
        return ColetaEntity(
            id = 0, // Autoincrement
            idPatrimonio = dto.patrimonioId,
            numeroPatrimonio = dto.numeroPatrimonio ?: "",
            idInventario = dto.idInventario ?: 0,
            idSala = dto.idSala,
            nomeSala = dto.nomeSala,                         // sala de ORIGEM do patrimônio
            localizacaoEncontrada = dto.localizacaoEncontrada, // onde foi ENCONTRADO
            idResponsavel = null,
            nomeResponsavel = null,
            observacao = dto.observacoes,
            estadoPatrimonio = dto.estadoEncontrado,
            latitude = null,
            longitude = null,
            dataColeta = parseDataColeta(dto.dataColeta),
            idUsuario = dto.usuarioId,
            nomeUsuario = dto.nomeColetor ?: "Usuário ${dto.usuarioId}",
            sincronizado = true, // Dados do servidor são sempre sincronizados
            tentativasSincronizacao = 0,
            erroSincronizacao = null,
            servidorId = dto.id
        )
    }
    
    /**
     * Converte string de data para timestamp
     */
    private fun parseDataColeta(dataStr: String?): Long {
        if (dataStr == null) return System.currentTimeMillis()
        
        return try {
            // Tentar vários formatos
            val formats = listOf(
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "dd/MM/yyyy HH:mm:ss",
                "dd/MM/yyyy HH:mm"
            )
            
            for (format in formats) {
                try {
                    val sdf = SimpleDateFormat(format, Locale.getDefault())
                    return sdf.parse(dataStr)?.time ?: System.currentTimeMillis()
                } catch (e: Exception) {
                    // Tentar próximo formato
                }
            }
            
            // Se nenhum formato funcionou, usar timestamp atual
            System.currentTimeMillis()
            
        } catch (e: Exception) {
            Log.w(TAG, "Erro ao parsear data: $dataStr", e)
            System.currentTimeMillis()
        }
    }
}

/**
 * Resultado da sincronização
 */
data class SyncResult(
    val total: Int,
    val novas: Int,
    val atualizadas: Int,
    val erros: Int,
    val removidas: Int = 0  // v2.29.5: coletas excluídas no servidor que foram removidas localmente
) {
    val sucesso: Int get() = novas + atualizadas
    val percentualSucesso: Float get() = if (total > 0) (sucesso.toFloat() / total.toFloat()) * 100 else 0f
}
