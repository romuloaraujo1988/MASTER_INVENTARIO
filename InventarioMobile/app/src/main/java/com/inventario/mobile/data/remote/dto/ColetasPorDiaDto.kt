package com.inventario.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para dados de coletas por dia
 * Usado no gráfico de evolução
 */
data class ColetasPorDiaDto(
    @SerializedName("data")
    val data: String, // Formato: "2025-11-06"
    
    @SerializedName("quantidade")
    val quantidade: Int = 0,
    
    @SerializedName("coletoresAtivos")
    val coletoresAtivos: Int = 0,
    
    @SerializedName("dataFormatada")
    val dataFormatada: String? = null // Formato: "06/11"
)

/**
 * Extensões para formatação
 */
fun getDataFormatada(dto: ColetasPorDiaDto): String {
    return try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val date = java.time.LocalDate.parse(dto.data)
            val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM")
            date.format(formatter)
        } else {
            dto.dataFormatada ?: dto.data.takeLast(5).replace("-", "/")
        }
    } catch (e: Exception) {
        dto.dataFormatada ?: dto.data.takeLast(5).replace("-", "/")
    }
}

fun getDiaDaSemana(dto: ColetasPorDiaDto): String {
    return try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val date = java.time.LocalDate.parse(dto.data)
            when (date.dayOfWeek.value) {
                1 -> "SEG"
                2 -> "TER"
                3 -> "QUA"
                4 -> "QUI"
                5 -> "SEX"
                6 -> "SÁB"
                7 -> "DOM"
                else -> "?"
            }
        } else {
            "?"
        }
    } catch (e: Exception) {
        "?"
    }
}
