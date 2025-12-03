package com.inventario.mobile.utils

// Temporarily commented out for minimal working version
/*
import android.util.Log
import com.inventario.mobile.data.local.database.InventarioDatabase
import com.inventario.mobile.domain.model.Sala
import com.inventario.mobile.domain.model.Setor
import kotlinx.coroutines.flow.first
import java.util.Date

/**
 * Inicializador de dados para desenvolvimento e testes
 */
object DatabaseInitializer {
    
    private const val TAG = "DatabaseInitializer"
    
    /**
     * Inicializa o banco com dados de exemplo
     */
    suspend fun initializeDatabase(database: InventarioDatabase, forceReset: Boolean = false) {
        Log.d(TAG, "Iniciando inicialização do banco de dados...")
        
        if (forceReset) {
            Log.w(TAG, "ATENÇÃO: Limpando banco de dados...")
            clearDatabase(database)
        }
        
        initializeSetores(database)
        initializeSalas(database)
        
        Log.d(TAG, "Inicialização do banco de dados concluída!")
    }
    
    /**
     * Inicializa setores de exemplo
     */
    private suspend fun initializeSetores(database: InventarioDatabase) {
        val setorDao = database.setorDao()
        
        // Verificar se já existem setores
        val count = setorDao.getAllSetores().first().size
        if (count > 0) {
            Log.d(TAG, "Banco já possui $count setores")
            return
        }
        
        Log.d(TAG, "Criando setores de exemplo...")
        
        val setores = listOf(
            Setor(
                nome = "Tecnologia da Informação",
                codigo = "TI",
                descricao = "Setor de Tecnologia da Informação",
                ativo = true,
                sincronizado = false,
                dataCriacao = System.currentTimeMillis(),
                dataAtualizacao = System.currentTimeMillis()
            ),
            Setor(
                nome = "Administração",
                codigo = "ADM",
                descricao = "Setor administrativo",
                ativo = true,
                sincronizado = false,
                dataCriacao = System.currentTimeMillis(),
                dataAtualizacao = System.currentTimeMillis()
            ),
            Setor(
                nome = "Biblioteca",
                codigo = "BIB",
                descricao = "Setor da biblioteca",
                ativo = true,
                sincronizado = false,
                dataCriacao = System.currentTimeMillis(),
                dataAtualizacao = System.currentTimeMillis()
            ),
            Setor(
                nome = "Ensino",
                codigo = "ENS",
                descricao = "Setor de ensino e salas de aula",
                ativo = true,
                sincronizado = false,
                dataCriacao = System.currentTimeMillis(),
                dataAtualizacao = System.currentTimeMillis()
            )
        )
        
        setorDao.insertSetores(setores)
        Log.d(TAG, "${setores.size} setores inseridos com sucesso")
    }
    
    /**
     * Inicializa salas de exemplo
     */
    suspend fun initializeSalas(database: InventarioDatabase) {
        val salaDao = database.salaDao()
        
        // Verificar se já existem salas
        val count = salaDao.getAllSalas().first().size
        if (count > 0) {
            Log.d(TAG, "Banco já possui $count salas")
            return
        }
        
        Log.d(TAG, "Criando salas de exemplo...")
        
        val salas = listOf(
            // Salas de TI (setorId = 1)
            Sala(
                nome = "Laboratório de Informática 1",
                codigo = "LAB-INFO-01",
                descricao = "Laboratório com 30 computadores Dell",
                ativo = true,
                setorId = 1,
                sincronizado = false,
                dataCriacao = System.currentTimeMillis(),
                dataAtualizacao = System.currentTimeMillis()
            ),
            Sala(
                nome = "Laboratório de Informática 2",
                codigo = "LAB-INFO-02",
                descricao = "Laboratório com 25 computadores HP",
                ativo = true,
                setorId = 1,
                sincronizado = false,
                dataCriacao = System.currentTimeMillis(),
                dataAtualizacao = System.currentTimeMillis()
            ),
            Sala(
                nome = "Laboratório de Redes",
                codigo = "LAB-REDES",
                descricao = "Laboratório de infraestrutura de redes",
                ativo = true,
                setorId = 1,
                sincronizado = false,
                dataCriacao = System.currentTimeMillis(),
                dataAtualizacao = System.currentTimeMillis()
            ),
            Sala(
                nome = "Sala de TI",
                codigo = "TI-001",
                descricao = "Sala de Tecnologia da Informação",
                ativo = true,
                setorId = 1,
                sincronizado = false,
                dataCriacao = System.currentTimeMillis(),
                dataAtualizacao = System.currentTimeMillis()
            ),
            Sala(
                nome = "Sala de Servidores",
                codigo = "SERVIDORES",
                descricao = "Sala com servidores e equipamentos de rede",
                ativo = true,
                setorId = 1,
                sincronizado = false,
                dataCriacao = System.currentTimeMillis(),
                dataAtualizacao = System.currentTimeMillis()
            ),
            
            // Salas de Administração (setorId = 2)
            Sala(
                nome = "Secretaria",
                codigo = "SECRETARIA",
                descricao = "Secretaria administrativa",
                ativo = true,
                setorId = 2,
                sincronizado = false,
                dataCriacao = System.currentTimeMillis(),
                dataAtualizacao = System.currentTimeMillis()
            ),
            Sala(
                nome = "Diretoria",
                codigo = "DIRETORIA",
                descricao = "Sala da diretoria",
                ativo = true,
                setorId = 2,
                sincronizado = false,
                dataCriacao = System.currentTimeMillis(),
                dataAtualizacao = System.currentTimeMillis()
            ),
            Sala(
                nome = "Sala de Reuniões",
                codigo = "REUNIOES",
                descricao = "Sala de reuniões com 20 lugares",
                ativo = true,
                setorId = 2,
                sincronizado = false,
                dataCriacao = System.currentTimeMillis(),
                dataAtualizacao = System.currentTimeMillis()
            ),
            
            // Salas de Biblioteca (setorId = 3)
            Sala(
                nome = "Biblioteca - Acervo",
                codigo = "BIB-ACERVO",
                descricao = "Acervo principal da biblioteca",
                ativo = true,
                setorId = 3,
                sincronizado = false,
                dataCriacao = System.currentTimeMillis(),
                dataAtualizacao = System.currentTimeMillis()
            ),
            Sala(
                nome = "Biblioteca - Sala de Estudos",
                codigo = "BIB-ESTUDOS",
                descricao = "Sala de estudos com 40 lugares",
                ativo = true,
                setorId = 3,
                sincronizado = false,
                dataCriacao = System.currentTimeMillis(),
                dataAtualizacao = System.currentTimeMillis()
            ),
            
            // Salas de Ensino (setorId = 4)
            Sala(
                nome = "Sala de Aula 101",
                codigo = "SALA-101",
                descricao = "Sala de aula com 40 lugares",
                ativo = true,
                setorId = 4,
                sincronizado = false,
                dataCriacao = System.currentTimeMillis(),
                dataAtualizacao = System.currentTimeMillis()
            ),
            Sala(
                nome = "Sala de Aula 102",
                codigo = "SALA-102",
                descricao = "Sala de aula com 35 lugares",
                ativo = true,
                setorId = 4,
                sincronizado = false,
                dataCriacao = System.currentTimeMillis(),
                dataAtualizacao = System.currentTimeMillis()
            ),
            Sala(
                nome = "Sala de Aula 103",
                codigo = "SALA-103",
                descricao = "Sala de aula com 30 lugares",
                ativo = true,
                setorId = 4,
                sincronizado = false,
                dataCriacao = System.currentTimeMillis(),
                dataAtualizacao = System.currentTimeMillis()
            ),
            Sala(
                nome = "Auditório",
                codigo = "AUDITORIO",
                descricao = "Auditório com 200 lugares",
                ativo = true,
                setorId = 4,
                sincronizado = false,
                dataCriacao = System.currentTimeMillis(),
                dataAtualizacao = System.currentTimeMillis()
            ),
            Sala(
                nome = "Laboratório de Química",
                codigo = "LAB-QUIMICA",
                descricao = "Laboratório de química experimental",
                ativo = true,
                setorId = 4,
                sincronizado = false,
                dataCriacao = System.currentTimeMillis(),
                dataAtualizacao = System.currentTimeMillis()
            ),
            Sala(
                nome = "Laboratório de Física",
                codigo = "LAB-FISICA",
                descricao = "Laboratório de física experimental",
                ativo = true,
                setorId = 4,
                sincronizado = false,
                dataCriacao = System.currentTimeMillis(),
                dataAtualizacao = System.currentTimeMillis()
            )
        )
        
        salaDao.insertSalas(salas)
        Log.d(TAG, "${salas.size} salas inseridas com sucesso")
        
        // Listar salas inseridas
        salas.forEachIndexed { index, sala ->
            Log.d(TAG, "  [$index] ${sala.nome} (${sala.codigo})")
        }
    }
    
    /**
     * Limpa todos os dados do banco
     */
    private suspend fun clearDatabase(database: InventarioDatabase) {
        database.clearAllTables()
        Log.w(TAG, "Banco de dados limpo!")
    }
    
    /**
     * Verifica status do banco de dados
     */
    suspend fun checkDatabaseStatus(database: InventarioDatabase): DatabaseStatus {
        val setorCount = database.setorDao().getAllSetores().first().size
        val salaCount = database.salaDao().getAllSalas().first().size
        // TODO: Implementar contagens reais quando métodos estiverem disponíveis
        val patrimonioCount = 0
        val coletaCount = 0
        
        return DatabaseStatus(
            setores = setorCount,
            salas = salaCount,
            patrimonios = patrimonioCount,
            coletas = coletaCount
        )
    }
    
    /**
     * Status do banco de dados
     */
    data class DatabaseStatus(
        val setores: Int,
        val salas: Int,
        val patrimonios: Int,
        val coletas: Int
    ) {
        fun isEmpty(): Boolean = setores == 0 && salas == 0 && patrimonios == 0 && coletas == 0
        
        override fun toString(): String {
            return """
                Status do Banco de Dados:
                - Setores: $setores
                - Salas: $salas
                - Patrimônios: $patrimonios
                - Coletas: $coletas
            """.trimIndent()
        }
    }
}
*/
