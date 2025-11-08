package com.inventario.mobile.data.model

data class Sala(
    val id: Int,
    val nome: String,
    val descricao: String? = null,
    val andar: String? = null,
    val bloco: String? = null,
    val ativa: Boolean = true
)
