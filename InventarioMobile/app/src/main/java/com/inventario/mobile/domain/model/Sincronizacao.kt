package com.inventario.mobile.domain.model

// import androidx.room.Entity
// import androidx.room.PrimaryKey
// import androidx.room.Index

/**
 * Entidade que controla o status de sincronização
 */
// @Entity(
//     tableName = "sincronizacao",
//     indices = [
//         Index(value = ["entidade", "entidadeId"], unique = true),
//         Index(value = ["dataSincronizacao"])
//     ]
// )
data class Sincronizacao(
    // @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val entidade: String, // PATRIMONIO, COLETA, SETOR, SALA, USUARIO
    val entidadeId: Long,
    val operacao: String, // CREATE, UPDATE, DELETE
    val status: String, // PENDENTE, SINCRONIZADO, ERRO
    val tentativas: Int = 0,
    val ultimoErro: String? = null,
    
    val dataCriacao: Long = System.currentTimeMillis(),
    val dataSincronizacao: Long? = null,
    val dataUltimaTentativa: Long? = null
)