package com.inventario.mobile.data.remote.api

import com.inventario.mobile.data.remote.dto.*
import retrofit2.Response

/**
 * Implementação mock temporária do ApiService para testes sem Hilt
 */
class MockApiService : ApiService {

    override suspend fun login(loginRequest: LoginRequest): Response<LoginResponse> {
        throw NotImplementedError("Mock implementation - not available offline")
    }

    override suspend fun refreshToken(refreshRequest: RefreshTokenRequest): Response<LoginResponse> {
        throw NotImplementedError("Mock implementation - not available offline")
    }

    override suspend fun getPatrimonios(): Response<ApiResponse<List<MobilePatrimonioDto>>> {
        val apiResponse = ApiResponse(
            success = true,
            message = "Mock data",
            data = emptyList<MobilePatrimonioDto>()
        )
        return Response.success(apiResponse)
    }

    override suspend fun getPatrimonioByNumero(numero: String): Response<ApiResponse<MobilePatrimonioDto>> {
        val apiResponse = ApiResponse<MobilePatrimonioDto>(
            success = false,
            message = "Patrimônio não encontrado - Mock implementation",
            data = null
        )
        return Response.success(apiResponse)
    }

    override suspend fun getPatrimonioById(id: Long): Response<ApiResponse<MobilePatrimonioDto>> {
        val apiResponse = ApiResponse<MobilePatrimonioDto>(
            success = false,
            message = "Patrimônio não encontrado - Mock implementation",
            data = null
        )
        return Response.success(apiResponse)
    }

    override suspend fun getPatrimonioByQrCode(qrCode: String): Response<ApiResponse<MobilePatrimonioDto>> {
        val apiResponse = ApiResponse<MobilePatrimonioDto>(
            success = false,
            message = "Patrimônio não encontrado - Mock implementation",
            data = null
        )
        return Response.success(apiResponse)
    }

    override suspend fun getAllPatrimonios(page: Int, size: Int): Response<ApiResponse<List<MobilePatrimonioDto>>> {
        val apiResponse = ApiResponse(
            success = true,
            message = "Mock data",
            data = emptyList<MobilePatrimonioDto>()
        )
        return Response.success(apiResponse)
    }

    override suspend fun getPatrimoniosByResponsavel(
        responsavelId: Int,
        page: Int,
        size: Int,
        coletado: Boolean?
    ): Response<ApiResponse<List<MobilePatrimonioDto>>> {
        val apiResponse = ApiResponse(
            success = true,
            message = "Mock data",
            data = emptyList<MobilePatrimonioDto>()
        )
        return Response.success(apiResponse)
    }

    override suspend fun countPatrimoniosByResponsavel(responsavelId: Int): Response<ApiResponse<Int>> {
        val apiResponse = ApiResponse(
            success = true,
            message = "Mock data",
            data = 0
        )
        return Response.success(apiResponse)
    }

    override suspend fun createPatrimonio(patrimonio: PatrimonioDto): PatrimonioDto {
        throw NotImplementedError("Mock implementation - not available offline")
    }

    override suspend fun updatePatrimonio(id: Long, patrimonio: PatrimonioDto): PatrimonioDto {
        throw NotImplementedError("Mock implementation - not available offline")
    }

    override suspend fun deletePatrimonio(id: Long): Response<Unit> {
        throw NotImplementedError("Mock implementation - not available offline")
    }

    override suspend fun getSalas(): List<SalaDto> {
        return emptyList()
    }

    override suspend fun getSalasWithResponse(): Response<ApiResponse<List<SalaDto>>> {
        val apiResponse = ApiResponse(
            success = true,
            message = "Mock data",
            data = emptyList<SalaDto>()
        )
        return Response.success(apiResponse)
    }

    override suspend fun getSalaById(id: Long): SalaDto {
        throw NotImplementedError("Mock implementation - not available offline")
    }

    override suspend fun getResponsaveis(): Response<ApiResponse<List<ResponsavelDto>>> {
        val apiResponse = ApiResponse(
            success = true,
            message = "Mock data",
            data = emptyList<ResponsavelDto>()
        )
        return Response.success(apiResponse)
    }

    override suspend fun getResponsavelById(id: Int): Response<ApiResponse<ResponsavelDto>> {
        val apiResponse = ApiResponse<ResponsavelDto>(
            success = false,
            message = "Responsável não encontrado - Mock implementation",
            data = null
        )
        return Response.success(apiResponse)
    }



    override suspend fun getSetores(): List<SetorDto> {
        return emptyList()
    }

    override suspend fun getSetorById(id: Long): SetorDto {
        throw NotImplementedError("Mock implementation - not available offline")
    }



    override suspend fun getUsuarios(): List<UsuarioDto> {
        return emptyList()
    }

    override suspend fun getUsuarioById(id: Long): UsuarioDto {
        throw NotImplementedError("Mock implementation - not available offline")
    }



    override suspend fun getSalasBySetor(setorId: Long): List<SalaDto> {
        return emptyList()
    }

    override suspend fun getColetas(): Response<ApiResponse<List<ColetaDto>>> {
        return Response.success(ApiResponse(success = true, message = "Mock data", data = emptyList()))
    }

    override suspend fun createColeta(coleta: MobileColetaRequest): Response<ApiResponse<ColetaDto>> {
        throw NotImplementedError("Mock implementation - not available offline")
    }

    override suspend fun updateColeta(id: Long, coleta: MobileColetaRequest): Response<ApiResponse<ColetaDto>> {
        throw NotImplementedError("Mock implementation - not available offline")
    }

    override suspend fun getSyncStatus(): SyncStatusResponse {
        throw NotImplementedError("Mock implementation - not available offline")
    }

    override suspend fun uploadData(syncData: SyncDataRequest): Response<SyncDataResponse> {
        throw NotImplementedError("Mock implementation - not available offline")
    }

    override suspend fun downloadData(lastSync: String?): SyncDataResponse {
        throw NotImplementedError("Mock implementation - not available offline")
    }

    override suspend fun getDashboardStats(): Response<ApiResponse<DashboardStatsDto>> {
        throw NotImplementedError("Mock implementation - not available offline")
    }
    
    override suspend fun getDashboardStatsByInventario(inventarioId: Int): Response<ApiResponse<DashboardStatsDto>> {
        throw NotImplementedError("Mock implementation - not available offline")
    }
    
    override suspend fun getColetasEvolucao(dias: Int): Response<ApiResponse<List<ColetasPorDiaDto>>> {
        throw NotImplementedError("Mock implementation - not available offline")
    }

    override suspend fun getDescricoes(): Response<ApiResponse<List<Map<String, Any>>>> {
        val apiResponse = ApiResponse(
            success = true,
            message = "Mock data",
            data = emptyList<Map<String, Any>>()
        )
        return Response.success(apiResponse)
    }

    override suspend fun searchDescricoes(termo: String): Response<ApiResponse<List<Map<String, Any>>>> {
        val apiResponse = ApiResponse(
            success = true,
            message = "Mock data",
            data = emptyList<Map<String, Any>>()
        )
        return Response.success(apiResponse)
    }

    override suspend fun obterInventarioAtivo(): Response<ApiResponse<Map<String, Any>>> {
        val mockData: Map<String, Any> = mapOf(
            "existeInventarioAtivo" to false,
            "inventarioAtivo" to mapOf<String, Any>()
        )
        val apiResponse = ApiResponse(
            success = true,
            message = "Mock data - no active inventory",
            data = mockData
        )
        return Response.success(apiResponse)
    }
    
    override suspend fun getColetasPaginadas(
        page: Int,
        size: Int
    ): Response<ApiResponse<PagedResponse<ColetaDto>>> {
        throw NotImplementedError("Mock implementation - not available offline")
    }
}
