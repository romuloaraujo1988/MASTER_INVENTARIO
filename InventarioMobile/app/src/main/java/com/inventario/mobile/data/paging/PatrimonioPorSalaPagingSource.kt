package com.inventario.mobile.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.inventario.mobile.data.remote.api.PatrimonioApi
import com.inventario.mobile.data.remote.dto.MobilePatrimonioDto
import retrofit2.HttpException
import java.io.IOException

/**
 * PagingSource para carregar patrimônios de uma sala específica
 * 
 * @param patrimonioApi API de patrimônios
 * @param salaId ID da sala
 * @param coletado filtro: true=coletados, false=não coletados, null=todos
 * @param inventarioId ID do inventário (opcional)
 * @param pageSize tamanho da página
 */
class PatrimonioPorSalaPagingSource(
    private val patrimonioApi: PatrimonioApi,
    private val salaId: Int,
    private val coletado: Boolean? = null,
    private val inventarioId: Int? = null,
    private val pageSize: Int = 50
) : PagingSource<Int, MobilePatrimonioDto>() {
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MobilePatrimonioDto> {
        return try {
            val page = params.key ?: 0
            
            android.util.Log.d(TAG, "Carregando patrimônios da sala $salaId " +
                    "(página $page, coletado: $coletado)")
            
            val response = patrimonioApi.buscarPorSalaPaginado(
                salaId = salaId,
                page = page,
                size = pageSize,
                coletado = coletado,
                inventarioId = inventarioId
            )
            
            if (response.success && response.data != null) {
                val patrimonios = response.data
                
                android.util.Log.d(TAG, "✓ ${patrimonios.size} patrimônios carregados da sala $salaId")
                
                // Como o endpoint não retorna metadados de paginação,
                // assumimos que é a última página se retornar menos que pageSize
                val isLastPage = patrimonios.size < pageSize
                
                LoadResult.Page(
                    data = patrimonios,
                    prevKey = if (page == 0) null else page - 1,
                    nextKey = if (isLastPage) null else page + 1
                )
            } else {
                val errorMsg = response.message ?: "Erro ao carregar patrimônios"
                android.util.Log.e(TAG, "✗ Erro na resposta: $errorMsg")
                LoadResult.Error(Exception(errorMsg))
            }
        } catch (e: IOException) {
            android.util.Log.e(TAG, "✗ Erro de rede", e)
            LoadResult.Error(e)
        } catch (e: HttpException) {
            android.util.Log.e(TAG, "✗ Erro HTTP: ${e.code()}", e)
            LoadResult.Error(e)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "✗ Erro inesperado", e)
            LoadResult.Error(e)
        }
    }
    
    override fun getRefreshKey(state: PagingState<Int, MobilePatrimonioDto>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
    
    companion object {
        private const val TAG = "PatrimonioPorSalaPaging"
    }
}
