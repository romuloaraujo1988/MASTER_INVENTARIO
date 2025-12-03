package com.inventario.mobile.data.model

import com.google.gson.annotations.SerializedName
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

/**
 * Modelos de dados para comunicação com a API
 */

// ============= AUTENTICAÇÃO =============

/**
 * Request para login
 */
data class LoginRequest(
    @SerializedName("login")
    val login: String,
    
    @SerializedName("senha")
    val senha: String,
    
    @SerializedName("dispositivo_id")
    val dispositivoId: String,
    
    @SerializedName("dispositivo_info")
    val dispositivoInfo: DispositivoInfo
)

/**
 * Response do login
 */
data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: LoginData?
)

/**
 * Dados do login
 * 
 * v1.1.0 - Adicionado inventarioAtivo para salvar automaticamente no login
 */
data class LoginData(
    @SerializedName("access_token")
    val accessToken: String,
    
    @SerializedName("refresh_token")
    val refreshToken: String,
    
    @SerializedName("expires_in")
    val expiresIn: Long,
    
    @SerializedName("usuario")
    val usuario: UsuarioDto,
    
    /**
     * Inventário ativo (em andamento) retornado automaticamente no login
     * O app deve salvar este ID localmente para usar nas coletas
     */
    @SerializedName("inventario_ativo")
    val inventarioAtivo: InventarioAtivoDto? = null
)

/**
 * DTO com informações do inventário ativo
 * Retornado no login e no endpoint /api/mobile/inventario/ativo
 * 
 * Compatível com MobileInventarioDTO do backend e MobileInventarioInfo
 */
data class InventarioAtivoDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("nome")
    val nome: String,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("descricao")
    val descricao: String? = null,
    
    @SerializedName("ano")
    val ano: Int? = null,
    
    // Campos do MobileInventarioDTO (endpoint /inventario/ativo)
    @SerializedName("totalPatrimonios")
    val totalPatrimonios: Int? = null,
    
    @SerializedName("totalColetados")
    val totalColetados: Int? = null,
    
    // Campos do MobileInventarioInfo (resposta do login)
    @SerializedName("patrimoniosColetados")
    val patrimoniosColetados: Int? = null,
    
    @SerializedName("percentualConclusao")
    val percentualConclusao: Double? = null
) {
    /**
     * Retorna o total de patrimônios coletados
     * Compatível com ambos os formatos de resposta
     */
    fun getColetados(): Int? = totalColetados ?: patrimoniosColetados
}

/**
 * Request para refresh token
 */
data class RefreshTokenRequest(
    @SerializedName("refresh_token")
    val refreshToken: String
)

/**
 * Response do refresh token
 */
data class RefreshTokenResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: TokenData?
)

/**
 * Dados do token
 */
data class TokenData(
    @SerializedName("access_token")
    val accessToken: String,
    
    @SerializedName("expires_in")
    val expiresIn: Long
)

/**
 * Informações do dispositivo
 */
@Parcelize
data class DispositivoInfo(
    @SerializedName("modelo")
    val modelo: String,
    
    @SerializedName("sistema_operacional")
    val sistemaOperacional: String,
    
    @SerializedName("versao_so")
    val versaoSO: String,
    
    @SerializedName("versao_app")
    val versaoApp: String,
    
    @SerializedName("resolucao_tela")
    val resolucaoTela: String
) : Parcelable

// ============= USUÁRIO =============

/**
 * DTO do usuário
 */
@Parcelize
data class UsuarioDto(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("nome")
    val nome: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("login")
    val login: String,
    
    @SerializedName("ativo")
    val ativo: Boolean,
    
    @SerializedName("perfil")
    val perfil: String?,
    
    @SerializedName("setor_id")
    val setorId: Long?
) : Parcelable

// ============= PATRIMÔNIO =============

/**
 * DTO do patrimônio
 */
@Parcelize
data class PatrimonioDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("codigo")
    val codigo: String,
    
    @SerializedName("descricao")
    val descricao: String,
    
    @SerializedName("descricao_resumida")
    val descricaoResumida: String?,
    
    @SerializedName("marca")
    val marca: String?,
    
    @SerializedName("modelo")
    val modelo: String?,
    
    @SerializedName("numero_serie")
    val numeroSerie: String?,
    
    @SerializedName("situacao")
    val situacao: String,
    
    @SerializedName("valor")
    val valor: Double?,
    
    @SerializedName("data_aquisicao")
    val dataAquisicao: String?,
    
    @SerializedName("setor_id")
    val setorId: Int?,
    
    @SerializedName("sala_id")
    val salaId: Int?,
    
    @SerializedName("responsavel_id")
    val responsavelId: Int?,
    
    @SerializedName("qr_code")
    val qrCode: String?,
    
    @SerializedName("sem_etiqueta")
    val semEtiqueta: Boolean,
    
    @SerializedName("observacoes")
    val observacoes: String?,
    
    @SerializedName("versao")
    val versao: Int
) : Parcelable

/**
 * Request para buscar patrimônio
 */
data class BuscarPatrimonioRequest(
    @SerializedName("codigo")
    val codigo: String?,
    
    @SerializedName("qr_code")
    val qrCode: String?
)

// ============= COLETA =============

/**
 * DTO da coleta
 */
@Parcelize
data class ColetaDto(
    @SerializedName("id")
    val id: Int?,
    
    @SerializedName("patrimonio_id")
    val patrimonioId: Int,
    
    @SerializedName("usuario_id")
    val usuarioId: Int,
    
    @SerializedName("data_coleta")
    val dataColeta: String,
    
    @SerializedName("setor_encontrado_id")
    val setorEncontradoId: Int?,
    
    @SerializedName("sala_encontrada_id")
    val salaEncontradaId: Int?,
    
    @SerializedName("situacao_encontrada")
    val situacaoEncontrada: String?,
    
    @SerializedName("observacoes")
    val observacoes: String?,
    
    @SerializedName("latitude")
    val latitude: Double?,
    
    @SerializedName("longitude")
    val longitude: Double?,
    
    @SerializedName("foto_base64")
    val fotoBase64: String?,
    
    @SerializedName("metodo_coleta")
    val metodoColeta: String,
    
    @SerializedName("dispositivo_id")
    val dispositivoId: String
) : Parcelable

/**
 * Request para registrar coleta
 */
data class RegistrarColetaRequest(
    @SerializedName("coletas")
    val coletas: List<ColetaDto>
)

/**
 * Response do registro de coleta
 */
data class RegistrarColetaResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: ResultadoColeta?
)

/**
 * Resultado do registro de coleta
 */
data class ResultadoColeta(
    @SerializedName("total_processadas")
    val totalProcessadas: Int,
    
    @SerializedName("total_sucesso")
    val totalSucesso: Int,
    
    @SerializedName("total_erro")
    val totalErro: Int,
    
    @SerializedName("erros")
    val erros: List<ErroColeta>?
)

/**
 * Erro na coleta
 */
data class ErroColeta(
    @SerializedName("patrimonio_id")
    val patrimonioId: Int,
    
    @SerializedName("codigo")
    val codigo: String?,
    
    @SerializedName("erro")
    val erro: String
)

// ============= SINCRONIZAÇÃO =============

/**
 * Request para sincronização
 */
data class SincronizacaoRequest(
    @SerializedName("ultima_sincronizacao")
    val ultimaSincronizacao: String?,
    
    @SerializedName("tipos")
    val tipos: List<String> // ["patrimonio", "setor", "sala", "usuario"]
)

/**
 * Response da sincronização
 */
data class SincronizacaoResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: DadosSincronizacao?
)

/**
 * Dados da sincronização
 */
data class DadosSincronizacao(
    @SerializedName("patrimonio")
    val patrimonio: List<PatrimonioDto>?,
    
    @SerializedName("setor")
    val setor: List<SetorDto>?,
    
    @SerializedName("sala")
    val sala: List<SalaDto>?,
    
    @SerializedName("usuario")
    val usuario: List<UsuarioDto>?,
    
    @SerializedName("timestamp_sincronizacao")
    val timestampSincronizacao: String
)

// ============= SETOR E SALA =============

/**
 * DTO do setor
 */
@Parcelize
data class SetorDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("nome")
    val nome: String,
    
    @SerializedName("descricao")
    val descricao: String?,
    
    @SerializedName("campus_id")
    val campusId: Int?,
    
    @SerializedName("ativo")
    val ativo: Boolean
) : Parcelable

/**
 * DTO da sala
 */
@Parcelize
data class SalaDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("nome")
    val nome: String,
    
    @SerializedName("descricao")
    val descricao: String?,
    
    @SerializedName("setor_id")
    val setorId: Int,
    
    @SerializedName("ativo")
    val ativo: Boolean
) : Parcelable

// ============= RESPOSTA GENÉRICA =============

/**
 * Response genérica da API
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: T?,
    
    @SerializedName("errors")
    val errors: List<String>?
)

/**
 * Response paginada
 */
data class PaginatedResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: List<T>?,
    
    @SerializedName("pagination")
    val pagination: PaginationInfo?
)

/**
 * Informações de paginação
 */
data class PaginationInfo(
    @SerializedName("current_page")
    val currentPage: Int,
    
    @SerializedName("per_page")
    val perPage: Int,
    
    @SerializedName("total")
    val total: Int,
    
    @SerializedName("total_pages")
    val totalPages: Int,
    
    @SerializedName("has_next")
    val hasNext: Boolean,
    
    @SerializedName("has_previous")
    val hasPrevious: Boolean
)

// ============= STATUS E ESTATÍSTICAS =============

/**
 * Status da sincronização
 */
data class StatusSincronizacao(
    @SerializedName("ultima_sincronizacao")
    val ultimaSincronizacao: String?,
    
    @SerializedName("total_patrimonio")
    val totalPatrimonio: Int,
    
    @SerializedName("total_coletas")
    val totalColetas: Int,
    
    @SerializedName("coletas_pendentes")
    val coletasPendentes: Int
)

/**
 * Estatísticas do servidor
 */
data class EstatisticasServidor(
    @SerializedName("total_patrimonio")
    val totalPatrimonio: Int,
    
    @SerializedName("total_coletas")
    val totalColetas: Int,
    
    @SerializedName("total_usuarios_ativos")
    val totalUsuariosAtivos: Int,
    
    @SerializedName("ultima_atualizacao")
    val ultimaAtualizacao: String
)
