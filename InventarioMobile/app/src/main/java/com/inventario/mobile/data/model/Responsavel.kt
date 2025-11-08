package com.inventario.mobile.data.model

import com.inventario.mobile.data.remote.dto.ResponsavelDto

data class Responsavel(
    val id: Int,
    val nome: String,
    val cpf: String? = null,
    val email: String? = null,
    val telefone: String? = null,
    val cargo: String? = null,
    val idSetor: Int? = null,
    val nomeSetor: String? = null,
    val ativo: Boolean = true,
    val dataCadastro: String? = null
) {
    companion object {
        fun fromDto(dto: ResponsavelDto): Responsavel {
            return Responsavel(
                id = dto.id,
                nome = dto.nome,
                cpf = dto.cpf,
                email = dto.email,
                telefone = dto.telefone,
                cargo = dto.cargo,
                idSetor = dto.idSetor,
                nomeSetor = dto.nomeSetor,
                ativo = dto.ativo,
                dataCadastro = dto.dataCadastro
            )
        }
    }
}
