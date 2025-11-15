package com.inventario.mobile.domain.usecase

import com.inventario.mobile.data.local.LocalDataManager
import com.inventario.mobile.data.model.Usuario
import javax.inject.Inject

/**
 * Use Case: Obter usuário atual
 * 
 * Responsabilidade: Retornar o usuário logado atualmente
 */
class ObterUsuarioAtualUseCase @Inject constructor(
    private val localDataManager: LocalDataManager
) {
    /**
     * Obtém o usuário atualmente logado
     * 
     * @return Usuário logado ou null se não houver sessão
     */
    suspend operator fun invoke(): Usuario? {
        return localDataManager.getCurrentUser()
    }
}
