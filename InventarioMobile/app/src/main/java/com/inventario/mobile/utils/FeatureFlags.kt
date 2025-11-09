package com.inventario.mobile.utils

/**
 * Feature flags stub - Clean Architecture está sempre ativo
 * Este arquivo é mantido apenas para compatibilidade temporária
 */
object FeatureFlags {
    // Sempre true - Clean Architecture está ativo
    const val useCleanArchitecture: Boolean = true
    const val useCleanDescricao: Boolean = true
    const val useCleanColeta: Boolean = true
    const val useCleanDashboard: Boolean = true
    const val useCleanSync: Boolean = true
    
    fun rollbackAll() {
        // No-op - não há mais rollback
    }
    
    fun init(context: android.content.Context) {
        // No-op - não há mais inicialização
    }
    
    fun printStatus() {
        // No-op
    }
    
    fun enableAll() {
        // No-op - já está tudo ativo
    }
    
    fun getStatus(): List<String> {
        return listOf(
            "Clean Architecture: ATIVO",
            "Clean Descrição: ATIVO",
            "Clean Coleta: ATIVO",
            "Clean Dashboard: ATIVO",
            "Clean Sync: ATIVO"
        )
    }
}
