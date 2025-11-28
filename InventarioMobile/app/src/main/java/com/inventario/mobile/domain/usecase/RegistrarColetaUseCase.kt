package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.model.Coleta
import com.inventario.mobile.domain.repository.ColetaRepository
import com.inventario.mobile.domain.repository.PatrimonioRepository
import com.inventario.mobile.data.local.LocalDataManager
import javax.inject.Inject

/**
 * Use Case: Registrar uma coleta
 * Contém lógica de negócio para validação e registro de coletas
 * 
 * v2.7: Suporte a itens sem etiqueta
 */
class RegistrarColetaUseCase @Inject constructor(
    private val coletaRepository: ColetaRepository,
    private val patrimonioRepository: PatrimonioRepository,
    private val localDataManager: LocalDataManager
) {
    /**
     * Registra coleta de patrimônio COM etiqueta
     */
    suspend operator fun invoke(
        numeroPatrimonio: String,
        salaId: Int? = null,
        localizacaoAtual: String?,
        estadoEncontrado: String? = null,
        observacoes: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        idUsuario: Long? = null
    ): Result<Coleta> {
        return try {
            // 1. Validar entrada
            if (numeroPatrimonio.isBlank()) {
                return Result.failure(Exception("Número do patrimônio é obrigatório"))
            }
            
            // 2. Obter usuário atual (CRÍTICO para modo offline)
            val usuarioAtual = localDataManager.getCurrentUser()
            if (usuarioAtual == null) {
                return Result.failure(Exception("[USUARIO_NAO_IDENTIFICADO] Usuário não está logado. Faça login novamente."))
            }
            
            val usuarioIdFinal = idUsuario ?: usuarioAtual.id.toLong()
            
            android.util.Log.d("RegistrarColetaUseCase", "✓ Usuário identificado: ${usuarioAtual.nome} (ID: $usuarioIdFinal)")
            
            // 3. Buscar patrimônio
            val patrimonio = patrimonioRepository.buscarPorNumero(numeroPatrimonio)
                ?: return Result.failure(Exception("Patrimônio não encontrado"))
            
            // 4. Criar coleta com número do patrimônio
            val coleta = Coleta(
                id = 0,
                patrimonioId = patrimonio.id,
                numeroPatrimonio = numeroPatrimonio,
                usuarioId = usuarioIdFinal,
                salaId = salaId,
                dataColeta = System.currentTimeMillis(),
                localizacaoAtual = localizacaoAtual,
                observacoes = observacoes,
                status = estadoEncontrado ?: "COLETADO",
                latitude = latitude,
                longitude = longitude,
                sincronizado = false
            )
            
            android.util.Log.d("RegistrarColetaUseCase", "✓ Coleta criada: Patrimônio ${coleta.numeroPatrimonio}, Usuário ${coleta.usuarioId}, Sala ${coleta.salaId}")
            
            // 5. Registrar coleta
            coletaRepository.registrarColeta(coleta)
            
        } catch (e: Exception) {
            android.util.Log.e("RegistrarColetaUseCase", "❌ Erro ao registrar coleta", e)
            Result.failure(e)
        }
    }
    
    /**
     * v2.7: Registra coleta de item SEM etiqueta
     */
    suspend fun registrarItemSemEtiqueta(
        descricao: String,
        categoria: String,
        salaId: Int? = null,
        localizacaoAtual: String?,
        estadoEncontrado: String,
        observacoes: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        fotoBase64: String? = null
    ): Result<Coleta> {
        return try {
            // 1. Validar entrada
            if (descricao.isBlank()) {
                return Result.failure(Exception("Descrição do item é obrigatória"))
            }
            
            // 2. Obter usuário atual
            val usuarioAtual = localDataManager.getCurrentUser()
            if (usuarioAtual == null) {
                return Result.failure(Exception("[USUARIO_NAO_IDENTIFICADO] Usuário não está logado. Faça login novamente."))
            }
            
            android.util.Log.d("RegistrarColetaUseCase", "✓ Registrando item SEM ETIQUETA: $descricao")
            
            // 3. Criar coleta para item sem etiqueta
            val coleta = Coleta(
                id = 0,
                patrimonioId = 0,  // Sem patrimônio associado
                numeroPatrimonio = null,  // Sem número
                descricaoPatrimonio = descricao,  // Descrição do item
                usuarioId = usuarioAtual.id.toLong(),
                salaId = salaId,
                dataColeta = System.currentTimeMillis(),
                localizacaoAtual = localizacaoAtual,
                observacoes = "Categoria: $categoria | $observacoes",
                status = estadoEncontrado,
                latitude = latitude,
                longitude = longitude,
                sincronizado = false,
                // v2.7: Campos específicos para item sem etiqueta
                semEtiqueta = true,
                descricaoItemSemEtiqueta = descricao,
                categoriaItemSemEtiqueta = categoria,
                fotoPath = fotoBase64
            )
            
            android.util.Log.d("RegistrarColetaUseCase", "✓ Coleta SEM ETIQUETA criada: $descricao, Categoria: $categoria")
            
            // 4. Registrar coleta
            coletaRepository.registrarColetaSemEtiqueta(coleta)
            
        } catch (e: Exception) {
            android.util.Log.e("RegistrarColetaUseCase", "❌ Erro ao registrar item sem etiqueta", e)
            Result.failure(e)
        }
    }
}
