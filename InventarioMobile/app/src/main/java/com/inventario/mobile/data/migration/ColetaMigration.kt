package com.inventario.mobile.data.migration

import android.util.Log
import com.inventario.mobile.data.local.dao.ColetaDao
import com.inventario.mobile.data.local.dao.PatrimonioDao
import javax.inject.Inject

/**
 * Migração de dados de coletas antigas
 * 
 * Responsabilidade:
 * - Identificar coletas com campos vazios
 * - Buscar dados do patrimônio
 * - Preencher campos faltantes
 * - Atualizar no banco
 */
class ColetaMigration @Inject constructor(
    private val coletaDao: ColetaDao,
    private val patrimonioDao: PatrimonioDao
) {
    companion object {
        private const val TAG = "ColetaMigration"
    }
    
    /**
     * Migra coletas antigas com dados incompletos
     * 
     * @return Result com quantidade de coletas atualizadas
     */
    suspend fun migrarColetasAntigas(): Result<MigrationResult> {
        return try {
            Log.d(TAG, "═══════════════════════════════════════════")
            Log.d(TAG, "INICIANDO MIGRAÇÃO DE COLETAS ANTIGAS")
            
            // 1. Buscar todas as coletas
            val todasColetas = coletaDao.buscarTodas()
            Log.d(TAG, "Total de coletas: ${todasColetas.size}")
            
            // 2. Identificar coletas incompletas
            val coletasIncompletas = todasColetas.filter { coleta ->
                coleta.numeroPatrimonio.isBlank() || 
                coleta.nomeSala.isNullOrBlank() ||
                coleta.nomeUsuario.isBlank()
            }
            
            Log.d(TAG, "Coletas incompletas: ${coletasIncompletas.size}")
            
            if (coletasIncompletas.isEmpty()) {
                Log.d(TAG, "✓ Nenhuma coleta precisa de migração")
                return Result.success(MigrationResult(0, 0, 0))
            }
            
            // 3. Atualizar cada coleta
            var atualizadas = 0
            var semPatrimonio = 0
            var erros = 0
            
            for (coleta in coletasIncompletas) {
                try {
                    // Buscar dados do patrimônio
                    val patrimonio = patrimonioDao.buscarPorId(coleta.idPatrimonio)
                    
                    if (patrimonio != null) {
                        // Atualizar campos vazios
                        val coletaAtualizada = coleta.copy(
                            numeroPatrimonio = if (coleta.numeroPatrimonio.isBlank()) {
                                patrimonio.numero
                            } else {
                                coleta.numeroPatrimonio
                            },
                            nomeSala = if (coleta.nomeSala.isNullOrBlank()) {
                                patrimonio.nomeSala
                            } else {
                                coleta.nomeSala
                            },
                            idSala = if (coleta.idSala == null) {
                                patrimonio.idSala
                            } else {
                                coleta.idSala
                            },
                            idResponsavel = if (coleta.idResponsavel == null) {
                                patrimonio.idResponsavel
                            } else {
                                coleta.idResponsavel
                            },
                            nomeResponsavel = if (coleta.nomeResponsavel.isNullOrBlank()) {
                                patrimonio.nomeResponsavel
                            } else {
                                coleta.nomeResponsavel
                            }
                        )
                        
                        // Salvar no banco
                        coletaDao.inserir(coletaAtualizada)
                        atualizadas++
                        
                        Log.d(TAG, "  ✓ Coleta ${coleta.id} atualizada (patrimônio ${patrimonio.numero})")
                        
                    } else {
                        semPatrimonio++
                        Log.w(TAG, "  ⚠ Coleta ${coleta.id}: patrimônio ${coleta.idPatrimonio} não encontrado")
                    }
                    
                } catch (e: Exception) {
                    erros++
                    Log.e(TAG, "  ✗ Erro ao migrar coleta ${coleta.id}", e)
                }
            }
            
            Log.d(TAG, "═══════════════════════════════════════════")
            Log.d(TAG, "MIGRAÇÃO CONCLUÍDA")
            Log.d(TAG, "Atualizadas: $atualizadas")
            Log.d(TAG, "Sem patrimônio: $semPatrimonio")
            Log.d(TAG, "Erros: $erros")
            Log.d(TAG, "═══════════════════════════════════════════")
            
            val result = MigrationResult(
                atualizadas = atualizadas,
                semPatrimonio = semPatrimonio,
                erros = erros
            )
            
            Result.success(result)
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro na migração", e)
            Result.failure(e)
        }
    }
    
    /**
     * Verifica se há coletas que precisam de migração
     */
    suspend fun precisaMigracao(): Boolean {
        return try {
            val todasColetas = coletaDao.buscarTodas()
            todasColetas.any { coleta ->
                coleta.numeroPatrimonio.isBlank() || 
                coleta.nomeSala.isNullOrBlank()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar migração", e)
            false
        }
    }
}

/**
 * Resultado da migração
 */
data class MigrationResult(
    val atualizadas: Int,
    val semPatrimonio: Int,
    val erros: Int
) {
    val total: Int get() = atualizadas + semPatrimonio + erros
    val sucesso: Boolean get() = erros == 0
}
