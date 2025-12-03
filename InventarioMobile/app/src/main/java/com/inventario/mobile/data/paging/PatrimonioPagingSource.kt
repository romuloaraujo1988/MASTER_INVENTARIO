package com.inventario.mobile.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.inventario.mobile.data.remote.api.PatrimonioApi
import com.inventario.mobile.data.remote.dto.MobilePatrimonioDto
import retrofit2.HttpException
import java.io.IOException

/**
 * PagingSource para carregar patrimônios do servidor com paginação
 * 
 * Usa o endpoint /api/mobile/patrimonio/paged que retorna PagedResponse
 * com metadados de paginação (totalElements, totalPages, hasNext, etc)
 * 
 * @param patrimonioApi API de patrimônios
 * @param pageSize tamanho da página (padrão: 20)
 */
class PatrimonioPagingSource(
    private val patrimonioApi: PatrimonioApi,
    private val pageSize: Int = 20
) : PagingSource<Int, MobilePatrimonioDto>() {
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MobilePatrimonioDto> {
        return try {
            val page = params.key ?: 0
            
            android.util.Log.d(TAG, "Carregando página $page com $pageSize itens")
            
            val response = patrimonioApi.buscarPatrimoniosPaginado(page, pageSize)
            
            if (response.success && response.data != null) {
                val pagedData = response.data
                
                android.util.Log.d(TAG, "✓ ${pagedData.content.size} patrimônios carregados " +
                        "(página ${page + 1} de ${pagedData.totalPages}, total: ${pagedData.totalElements})")
                
                LoadResult.Page(
                    data = pagedData.content,
                    prevKey = if (pagedData.first) null else page - 1,
                    nextKey = if (pagedData.last) null else page + 1
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
        private const val TAG = "PatrimonioPagingSource"
    }
}
