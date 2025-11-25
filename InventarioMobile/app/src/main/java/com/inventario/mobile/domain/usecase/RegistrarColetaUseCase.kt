package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.model.Coleta
import com.inventario.mobile.domain.repository.ColetaRepository
import com.inventario.mobile.domain.repository.PatrimonioRepository
import com.inventario.mobile.data.local.LocalDataManager
import javax.inject.Inject

/**
 * Use Case: Registrar uma coleta
 * Contém lógica de negócio para validação e registro de coletas
 */
class RegistrarColetaUseCase @Inject constructor(
    private val coletaRepository: ColetaRepository,
    private val patrimonioRepository: PatrimonioRepository,
    private val localDataManager: LocalDataManager
) {
    suspend operator fun invoke(
        numeroPatrimonio: String,
        salaId: Int? = null, // ✅ CRÍTICO: ID da sala onde está coletando
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
            
            // 4. TODO: Verificar se já foi coletado (implementar método no repositório)
            
            // 5. Criar coleta com número do patrimônio, usuário identificado e sala atual
            val coleta = Coleta(
                id = 0,
                patrimonioId = patrimonio.id,
                numeroPatrimonio = numeroPatrimonio,
                usuarioId = usuarioIdFinal, // ✅ Usuário identificado corretamente
                salaId = salaId, // ✅ CRÍTICO: ID da sala onde está coletando
                dataColeta = System.currentTimeMillis(),
                localizacaoAtual = localizacaoAtual,
                observacoes = observacoes,
                status = estadoEncontrado ?: "COLETADO",
                latitude = latitude,
                longitude = longitude,
                sincronizado = false
            )
            
            android.util.Log.d("RegistrarColetaUseCase", "✓ Coleta criada: Patrimônio ${coleta.numeroPatrimonio}, Usuário ${coleta.usuarioId}, Sala ${coleta.salaId}")
            

            
            // 6. Registrar coleta
            coletaRepository.registrarColeta(coleta)
            
        } catch (e: Exception) {
            android.util.Log.e("RegistrarColetaUseCase", "❌ Erro ao registrar coleta", e)
            Result.failure(e)
        }
    }
}
