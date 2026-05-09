package com.inventario.mobile.data.local.dao

import androidx.room.*
import com.inventario.mobile.data.local.entity.ColetaEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações de Coleta no banco local
 */
@Dao
interface ColetaDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(coleta: ColetaEntity): Long
    
    @Query("UPDATE coleta SET sincronizado = :sincronizado, servidorId = :servidorId WHERE id = :id")
    suspend fun atualizarSincronizado(id: Long, sincronizado: Boolean, servidorId: Int)
    
    @Query("SELECT * FROM coleta WHERE id = :id")
    suspend fun buscarPorId(id: Long): ColetaEntity?
    
    @Query("SELECT * FROM coleta WHERE sincronizado = 0 AND idInventario = :idInventario ORDER BY dataColeta ASC")
    suspend fun buscarPendentes(idInventario: Int): List<ColetaEntity>

    @Query("SELECT * FROM coleta WHERE sincronizado = 0 ORDER BY dataColeta ASC")
    suspend fun buscarPendentes(): List<ColetaEntity>
    
    @Query("SELECT * FROM coleta WHERE sincronizado = 0")
    fun observarPendentes(): Flow<List<ColetaEntity>>
    
    @Query("SELECT * FROM coleta WHERE idInventario = :idInventario ORDER BY dataColeta DESC")
    suspend fun buscarTodas(idInventario: Int): List<ColetaEntity>

    @Query("SELECT * FROM coleta ORDER BY dataColeta DESC")
    suspend fun buscarTodas(): List<ColetaEntity>
    
    @Query("SELECT COUNT(*) FROM coleta WHERE sincronizado = 0")
    fun observarQuantidadePendentes(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM coleta WHERE sincronizado = 0")
    suspend fun contarPendentes(): Int
    
    @Query("UPDATE coleta SET sincronizado = 1, servidorId = :servidorId WHERE id = :id")
    suspend fun marcarSincronizadaComServidor(id: Long, servidorId: Long)
    
    /**
     * Marca coleta como sincronizada (sem ID do servidor)
     * IMPORTANTE: Esta é a versão principal usada na sincronização
     */
    @Query("UPDATE coleta SET sincronizado = 1 WHERE id = :id")
    suspend fun marcarSincronizada(id: Long)
    
    @Query("UPDATE coleta SET tentativasSincronizacao = tentativasSincronizacao + 1, erroSincronizacao = :erro WHERE id = :id")
    suspend fun registrarErroSincronizacao(id: Long, erro: String?)
    
    @Query("DELETE FROM coleta WHERE id = :id")
    suspend fun deletar(id: Long)
    
    @Query("DELETE FROM coleta WHERE sincronizado = 1")
    suspend fun limparSincronizadas()
    
    @Query("DELETE FROM coleta")
    suspend fun limparTodas()
    
    @Query("DELETE FROM coleta WHERE sincronizado = 1 AND dataColeta < :timestamp")
    suspend fun limparSincronizadasAntigas(timestamp: Long): Int
    
    @Query("SELECT * FROM coleta ORDER BY dataColeta DESC LIMIT :limit")
    suspend fun buscarRecentes(limit: Int): List<ColetaEntity>
    
    @Query("SELECT COUNT(*) FROM coleta")
    suspend fun contarTodas(): Int
    
    // ========================================
    // Queries para Visualização de Coletas (v2.3)
    // ========================================
    
    /**
     * Busca coletas por usuário
     * @see Requirements 2.1
     */
    @Query("SELECT * FROM coleta WHERE idUsuario = :usuarioId ORDER BY dataColeta DESC")
    suspend fun buscarPorUsuario(usuarioId: Int): List<ColetaEntity>
    
    /**
     * Busca coletas por sala
     * @see Requirements 3.1
     */
    @Query("SELECT * FROM coleta WHERE idSala = :salaId AND idInventario = :idInventario ORDER BY dataColeta DESC")
    suspend fun buscarPorSala(salaId: Int, idInventario: Int): List<ColetaEntity>

    @Query("SELECT * FROM coleta WHERE idSala = :salaId ORDER BY dataColeta DESC")
    suspend fun buscarPorSala(salaId: Int): List<ColetaEntity>
    
    /**
     * Conta coletas sincronizadas
     * @see Requirements 6.1
     */
    @Query("SELECT COUNT(*) FROM coleta WHERE sincronizado = 1")
    suspend fun contarSincronizadas(): Int
    
    /**
     * Busca todas as salas distintas das coletas
     * Usado para popular filtro de salas
     */
    @Query("SELECT DISTINCT nomeSala FROM coleta WHERE nomeSala IS NOT NULL ORDER BY nomeSala ASC")
    suspend fun buscarSalasDistintas(): List<String>
    
    /**
     * Observa todas as coletas como Flow
     * Usado para atualização reativa da UI
     */
    @Query("SELECT * FROM coleta ORDER BY dataColeta DESC")
    fun observarTodas(): Flow<List<ColetaEntity>>
    
    // ========================================
    // TRANSAÇÕES ATÔMICAS (v2.2)
    // ========================================
    
    /**
     * Registra coleta com transação atômica
     * Garante que coleta e atualização do patrimônio aconteçam juntas
     * Se uma falhar, ambas são revertidas (tudo ou nada)
     *
     * v2.20.7: além de marcar `coletado = 1`, também persiste os campos de
     * auditoria (`coletadoPor`, `dataColeta`, `localizacaoEncontrada`,
     * `estadoEncontrado`, `observacoesColeta`) diretamente na tabela `patrimonio`.
     * Isso garante que esses dados sobrevivam mesmo após a coleta ser apagada
     * pós-sincronização (`limparSincronizadas`), e aparece corretamente nos
     * relatórios exportados.
     */
    @Transaction
    suspend fun registrarColetaComTransacao(coleta: ColetaEntity): Long {
        // 1. Inserir coleta
        val coletaId = inserir(coleta)

        // 2. Atualizar patrimônio com dados completos da coleta (mesma transação)
        atualizarPatrimonioComColeta(
            patrimonioId = coleta.idPatrimonio,
            dataColeta = coleta.dataColeta,
            coletadoPor = coleta.nomeUsuario,
            localizacaoEncontrada = coleta.nomeSala,
            estadoEncontrado = coleta.estadoPatrimonio,
            observacoesColeta = coleta.observacao
        )

        // 3. Retornar ID da coleta
        return coletaId
    }

    /**
     * Marca patrimônio como coletado preenchendo todos os campos de auditoria.
     * Usado dentro da transação de `registrarColetaComTransacao`.
     */
    @Query("""
        UPDATE patrimonio SET
            coletado = 1,
            dataColeta = :dataColeta,
            coletadoPor = :coletadoPor,
            localizacaoEncontrada = COALESCE(:localizacaoEncontrada, localizacaoEncontrada),
            estadoEncontrado = COALESCE(:estadoEncontrado, estadoEncontrado),
            observacoesColeta = COALESCE(:observacoesColeta, observacoesColeta)
        WHERE id = :patrimonioId
    """)
    suspend fun atualizarPatrimonioComColeta(
        patrimonioId: Int,
        dataColeta: Long,
        coletadoPor: String,
        localizacaoEncontrada: String?,
        estadoEncontrado: String?,
        observacoesColeta: String?
    )

    /**
     * Marca patrimônio como coletado (apenas o flag)
     * Mantido para compatibilidade com código legado.
     */
    @Query("UPDATE patrimonio SET coletado = 1 WHERE id = :patrimonioId")
    suspend fun marcarPatrimonioColetado(patrimonioId: Int)
    
    /**
     * Verifica se patrimônio já foi coletado neste inventário
     * CRÍTICO: Evita duplicatas
     */
    @Query("""
        SELECT * FROM coleta 
        WHERE idPatrimonio = :idPatrimonio 
        AND idInventario = :idInventario
        LIMIT 1
    """)
    suspend fun buscarColetaExistente(
        idPatrimonio: Int, 
        idInventario: Int
    ): ColetaEntity?
    
    /**
     * Conta quantas vezes um patrimônio foi coletado
     */
    @Query("""
        SELECT COUNT(*) FROM coleta 
        WHERE idPatrimonio = :idPatrimonio 
        AND idInventario = :idInventario
    """)
    suspend fun contarColetasPatrimonio(
        idPatrimonio: Int, 
        idInventario: Int
    ): Int
    
    // ========================================
    // Queries para Gráficos
    // ========================================
    
    /**
     * Conta coletas por inventário
     */
    @Query("SELECT COUNT(DISTINCT idPatrimonio) FROM coleta WHERE idInventario = :idInventario")
    suspend fun countByInventario(idInventario: Int): Int
    
    // ========================================
    // Queries para Diagnóstico de Coletas Pendentes (v2.6)
    // ========================================
    
    /**
     * Busca coletas pendentes COM erro de sincronização
     * Útil para diagnóstico de coletas "presas"
     */
    @Query("""
        SELECT * FROM coleta 
        WHERE sincronizado = 0 
        AND erroSincronizacao IS NOT NULL 
        ORDER BY tentativasSincronizacao DESC, dataColeta DESC
    """)
    suspend fun buscarPendentesComErro(): List<ColetaEntity>
    
    /**
     * Busca coletas pendentes SEM erro (nunca tentaram sincronizar)
     */
    @Query("""
        SELECT * FROM coleta 
        WHERE sincronizado = 0 
        AND erroSincronizacao IS NULL 
        ORDER BY dataColeta DESC
    """)
    suspend fun buscarPendentesSemErro(): List<ColetaEntity>
    
    /**
     * Busca coletas com muitas tentativas de sincronização (possível problema permanente)
     */
    @Query("""
        SELECT * FROM coleta 
        WHERE sincronizado = 0 
        AND tentativasSincronizacao >= :minTentativas
        ORDER BY tentativasSincronizacao DESC
    """)
    suspend fun buscarColetasComMuitasTentativas(minTentativas: Int = 3): List<ColetaEntity>
    
    /**
     * Limpa erro de sincronização para permitir nova tentativa
     */
    @Query("UPDATE coleta SET erroSincronizacao = NULL, tentativasSincronizacao = 0 WHERE id = :id")
    suspend fun limparErroSincronizacao(id: Long)
    
    /**
     * Limpa erros de todas as coletas pendentes (reset para nova tentativa)
     */
    @Query("UPDATE coleta SET erroSincronizacao = NULL, tentativasSincronizacao = 0 WHERE sincronizado = 0")
    suspend fun limparTodosErrosSincronizacao(): Int
    
    /**
     * Busca evolução diária das coletas (últimos 30 dias)
     * Retorna data formatada e quantidade acumulada
     */
    @Query("""
        SELECT 
            strftime('%d/%m', dataColeta / 1000, 'unixepoch') as data,
            COUNT(*) as quantidade
        FROM coleta
        WHERE idInventario = :idInventario
        GROUP BY date(dataColeta / 1000, 'unixepoch')
        ORDER BY date(dataColeta / 1000, 'unixepoch') ASC
        LIMIT 30
    """)
    suspend fun getEvolutionData(idInventario: Int): List<EvolutionData>
    
    /**
     * Busca top 10 descrições mais coletadas
     */
    @Query("""
        SELECT 
            p.descricao as descricao,
            COUNT(c.id) as quantidade
        FROM coleta c
        INNER JOIN patrimonio p ON c.idPatrimonio = p.id
        WHERE c.idInventario = :idInventario
        GROUP BY p.descricao
        ORDER BY quantidade DESC
        LIMIT 10
    """)
    suspend fun getTopItems(idInventario: Int): List<TopItemData>
    
    // ========================================
    // Queries para Fotos (v2.11)
    // ========================================
    
    /**
     * Busca coletas com foto pendente de sincronização
     */
    @Query("""
        SELECT * FROM coleta 
        WHERE fotoPath IS NOT NULL 
        AND fotoSincronizada = 0
        ORDER BY dataColeta ASC
    """)
    suspend fun buscarColetasComFotoPendente(): List<ColetaEntity>
    
    /**
     * Marca foto como sincronizada
     */
    @Query("UPDATE coleta SET fotoSincronizada = 1 WHERE id = :id")
    suspend fun marcarFotoSincronizada(id: Long)
    
    /**
     * Conta coletas com foto
     */
    @Query("SELECT COUNT(*) FROM coleta WHERE fotoPath IS NOT NULL")
    suspend fun contarColetasComFoto(): Int
    
    /**
     * Conta fotos pendentes de sincronização
     */
    @Query("SELECT COUNT(*) FROM coleta WHERE fotoPath IS NOT NULL AND fotoSincronizada = 0")
    suspend fun contarFotosPendentes(): Int
    
    /**
     * Atualiza caminho da foto de uma coleta
     */
    @Query("""
        UPDATE coleta SET 
            fotoPath = :fotoPath, 
            fotoThumbnailPath = :thumbnailPath,
            motivoFoto = :motivo,
            fotoSincronizada = 0
        WHERE id = :id
    """)
    suspend fun atualizarFoto(id: Long, fotoPath: String?, thumbnailPath: String?, motivo: String?)
    
    /**
     * Remove foto de uma coleta
     */
    @Query("UPDATE coleta SET fotoPath = NULL, fotoThumbnailPath = NULL, motivoFoto = NULL WHERE id = :id")
    suspend fun removerFoto(id: Long)
    
}
