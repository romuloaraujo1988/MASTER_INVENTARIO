package com.inventario.mobile.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.inventario.mobile.api.SalaApi
import com.inventario.mobile.data.model.Sala
import retrofit2.HttpException
import java.io.IOException

/**
 * PagingSource para carregar salas do servidor com paginação
 * 
 * @param salaApi API de salas
 * @param query Termo de busca (null ou vazio para listar todas)
 */
class SalaPagingSource(
    private val salaApi: SalaApi,
    private val query: String?
) : PagingSource<Int, Sala>() {
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Sala> {
        return try {
            val page = params.key ?: 0
            val pageSize = params.loadSize
            
            android.util.Log.d(TAG, "Carregando página $page com $pageSize itens (query: ${query ?: "todas"})")
            
            // Escolher endpoint baseado na query
            val response = if (query.isNullOrBlank()) {
                salaApi.listarSalasPaginado(page, pageSize)
            } else {
                salaApi.buscarSalasPorNome(query, page, pageSize)
            }
            
            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                val salas = body.data
                val totalPages = body.totalPages
                
                android.util.Log.d(TAG, "✓ ${salas.size} salas carregadas (página $page de $totalPages)")
                
                LoadResult.Page(
                    data = salas,
                    prevKey = if (page == 0) null else page - 1,
                    nextKey = if (page < totalPages - 1) page + 1 else null
                )
            } else {
                val errorMsg = response.body()?.message ?: "Erro ao carregar salas"
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
    
    override fun getRefreshKey(state: PagingState<Int, Sala>): Int? {
        // Retorna a página mais próxima da posição de âncora
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
    
    companion object {
        private const val TAG = "SalaPagingSource"
    }
}
