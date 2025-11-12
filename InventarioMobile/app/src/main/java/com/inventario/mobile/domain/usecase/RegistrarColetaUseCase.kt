package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.model.Coleta
import com.inventario.mobile.domain.repository.ColetaRepository
import com.inventario.mobile.domain.repository.PatrimonioRepository
import javax.inject.Inject

/**
 * Use Case: Registrar uma coleta
 * Contém lógica de negócio para validação e registro de coletas
 */
class RegistrarColetaUseCase @Inject constructor(
    private val coletaRepository: ColetaRepository,
    private val patrimonioRepository: PatrimonioRepository
) {
    suspend operator fun invoke(
        numeroPatrimonio: String,
        localizacaoAtual: String?,
        observacoes: String?,
        latitude: Double?,
        longitude: Double?,
        idUsuario: Long
    ): Result<Coleta> {
        return try {
            // 1. Validar entrada
            if (numeroPatrimonio.isBlank()) {
                return Result.failure(Exception("Número do patrimônio é obrigatório"))
            }
            
            // 2. Buscar patrimônio
            val patrimonio = patrimonioRepository.buscarPorNumero(numeroPatrimonio)
                ?: return Result.failure(Exception("Patrimônio não encontrado"))
            
            // 3. TODO: Verificar se já foi coletado (implementar método no repositório)
            
            // 4. Criar coleta
            val coleta = Coleta(
                id = 0,
                patrimonioId = patrimonio.id, // já é Long
                usuarioId = idUsuario,
                dataColeta = System.currentTimeMillis(),
                localizacaoAtual = localizacaoAtual,
                observacoes = observacoes,
                status = "COLETADO",
                latitude = latitude,
                longitude = longitude,
                sincronizado = false
            )
            
            // 5. Registrar coleta
            coletaRepository.registrarColeta(coleta)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
