package com.inventario.mobile.data.local.database

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Teste instrumentado da migração Room 16 → 17.
 *
 * Valida a migração introduzida pela feature
 * `coleta-descricao-livre-com-sugestao` (Task 10.4) — criação da tabela
 * `sugestao_descricao` sem impacto às tabelas pré-existentes. Cobre:
 *
 * - Requirements 7.1: cache Room isolado por inventário (nova tabela + 3 índices).
 * - Requirements 9.2: migração Room NÃO altera o schema das tabelas pré-existentes.
 *
 * ## Abordagem
 *
 * `AppDatabase` está declarado com `exportSchema = false`, o que impede o
 * uso do `MigrationTestHelper` tradicional (que exige schema JSON exportado).
 * Para não alterar config de produção, o teste aplica a migração
 * manualmente:
 *
 * 1. Cria um banco SQLite **na versão 16** usando `FrameworkSQLiteOpenHelperFactory`
 *    com um callback `SupportSQLiteOpenHelper.Callback(version=16)` que instala
 *    um subconjunto mínimo de tabelas pré-existentes (`patrimonio`, `coleta`)
 *    com dados fictícios.
 * 2. Executa `AppDatabase.MIGRATION_16_17.migrate(db)` diretamente sobre a
 *    conexão aberta.
 * 3. Introspecta o schema resultante via `sqlite_master`, `PRAGMA table_info`
 *    e `PRAGMA index_list` para validar:
 *    - existência da tabela `sugestao_descricao`;
 *    - chave primária composta `(idInventario, idPatrimonio)`;
 *    - presença dos três índices documentados no `design.md`;
 *    - preservação das tabelas pré-existentes e dos dados fictícios inseridos
 *      antes da migração.
 *
 * Para habilitar o acesso a `MIGRATION_16_17`, sua visibilidade em
 * `AppDatabase` foi alterada de `private` para `internal` com
 * `@VisibleForTesting` — mudança mínima e idiomática, sem afetar a API
 * pública do banco.
 *
 * ## Como executar
 *
 * ```bash
 * cd InventarioMobile
 * .\gradlew.bat :app:connectedDebugAndroidTest \
 *   --tests com.inventario.mobile.data.local.database.Migration16To17Test
 * ```
 *
 * Requer emulador ou dispositivo conectado (`adb devices`).
 *
 * **Validates: Requirements 7.1, 9.2**
 */
@RunWith(AndroidJUnit4::class)
class Migration16To17Test {

    private val testDbName = "migration-16-17-test.db"
    private lateinit var context: Context
    private var helper: SupportSQLiteOpenHelper? = null

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        // Garantir partida limpa — cada teste começa sem banco residual.
        context.deleteDatabase(testDbName)
    }

    @After
    fun tearDown() {
        try {
            helper?.close()
        } catch (_: Exception) {
            // best-effort
        }
        helper = null
        context.deleteDatabase(testDbName)
    }

    @Test
    fun migrate16To17_criaTabelaSugestaoDescricaoComChaveCompostaEIndices() {
        // 1. Criar banco na versão 16 com subconjunto mínimo de tabelas pré-existentes.
        helper = createV16Helper(
            context = context,
            dbName = testDbName,
            onCreateV16 = { db ->
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS patrimonio (
                        id INTEGER PRIMARY KEY NOT NULL,
                        numero TEXT NOT NULL,
                        numeroPatrimonio TEXT NOT NULL,
                        descricao TEXT NOT NULL,
                        dataUltimaAtualizacao INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS coleta (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        numeroPatrimonio TEXT NOT NULL,
                        dataColeta INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        )

        val dbV16 = helper!!.writableDatabase

        // Inserir dados fictícios para verificar integridade pós-migração.
        dbV16.execSQL(
            "INSERT INTO patrimonio (id, numero, numeroPatrimonio, descricao, dataUltimaAtualizacao) " +
                "VALUES (1, 'A-001', 'A-001', 'Cadeira giratoria preta', 1700000000000)"
        )
        dbV16.execSQL(
            "INSERT INTO patrimonio (id, numero, numeroPatrimonio, descricao, dataUltimaAtualizacao) " +
                "VALUES (2, 'A-002', 'A-002', 'Mesa de madeira', 1700000000000)"
        )
        dbV16.execSQL(
            "INSERT INTO coleta (numeroPatrimonio, dataColeta) VALUES ('A-001', 1700000000000)"
        )

        // 2. Aplicar a migração 16 → 17 diretamente.
        AppDatabase.MIGRATION_16_17.migrate(dbV16)

        // 3a. Verificar existência da tabela `sugestao_descricao`.
        val tabelas = listarTabelas(dbV16)
        assertTrue(
            "Tabela sugestao_descricao deve ter sido criada pela migração",
            tabelas.contains("sugestao_descricao")
        )

        // 3b. Verificar colunas e chave primária composta (idInventario, idPatrimonio).
        val colunas = pragmaTableInfo(dbV16, "sugestao_descricao")
        val nomesColunas = colunas.map { it.name }.toSet()
        val esperadas = setOf(
            "idInventario",
            "idPatrimonio",
            "numeroPatrimonio",
            "descricao",
            "descricaoNormalizada",
            "coletadoLocal",
            "dataAtualizacao"
        )
        assertEquals(
            "Schema de sugestao_descricao deve conter exatamente as colunas documentadas",
            esperadas,
            nomesColunas
        )

        val colunasPk = colunas.filter { it.pk > 0 }.map { it.name }.toSet()
        assertEquals(
            "PK composta deve ser (idInventario, idPatrimonio)",
            setOf("idInventario", "idPatrimonio"),
            colunasPk
        )

        // NOT NULL em todas as colunas — campos do cache são obrigatórios.
        colunas.forEach { c ->
            assertTrue(
                "Coluna ${c.name} deve ser NOT NULL em sugestao_descricao",
                c.notnull
            )
        }

        // 3c. Verificar índices criados pela migração (3 índices documentados
        //     no design.md, ignorando o índice automático da PK composta).
        val indices = pragmaIndexList(dbV16, "sugestao_descricao")
        val nomesIndices = indices.map { it.name }.toSet()
        assertTrue(
            "Índice composto (idInventario, coletadoLocal) deve existir. " +
                "Índices encontrados: $nomesIndices",
            nomesIndices.any { it.contains("idInventario_coletadoLocal") }
        )
        assertTrue(
            "Índice composto (idInventario, descricaoNormalizada) deve existir. " +
                "Índices encontrados: $nomesIndices",
            nomesIndices.any { it.contains("idInventario_descricaoNormalizada") }
        )
        assertTrue(
            "Índice simples (descricaoNormalizada) deve existir. " +
                "Índices encontrados: $nomesIndices",
            nomesIndices.any {
                it.endsWith("descricaoNormalizada") &&
                    !it.contains("idInventario")
            }
        )

        // 3d. Verificar integridade das tabelas pré-existentes (Req 9.2).
        assertTrue(
            "Tabela patrimonio deve permanecer após a migração",
            tabelas.contains("patrimonio")
        )
        assertTrue(
            "Tabela coleta deve permanecer após a migração",
            tabelas.contains("coleta")
        )

        val totalPatrimonios = contar(dbV16, "SELECT COUNT(*) FROM patrimonio")
        assertEquals(
            "Dados fictícios inseridos em patrimonio devem ser preservados",
            2,
            totalPatrimonios
        )
        val totalColetas = contar(dbV16, "SELECT COUNT(*) FROM coleta")
        assertEquals(
            "Dados fictícios inseridos em coleta devem ser preservados",
            1,
            totalColetas
        )

        // Registros individuais continuam íntegros.
        dbV16.query("SELECT descricao FROM patrimonio WHERE id = 1").use { cursor ->
            assertTrue("Patrimônio id=1 deve existir", cursor.moveToFirst())
            assertEquals("Cadeira giratoria preta", cursor.getString(0))
        }

        // 3e. Tabela `sugestao_descricao` deve estar vazia após a migração
        //     (migração apenas cria a estrutura, não popula dados).
        val totalSugestoes = contar(dbV16, "SELECT COUNT(*) FROM sugestao_descricao")
        assertEquals(
            "Tabela sugestao_descricao deve estar vazia logo após a migração",
            0,
            totalSugestoes
        )

        // 3f. Inserir + consultar um registro válido confirma que a
        //     estrutura aceita o schema esperado pela entidade Room.
        dbV16.execSQL(
            """
            INSERT INTO sugestao_descricao
                (idInventario, idPatrimonio, numeroPatrimonio, descricao,
                 descricaoNormalizada, coletadoLocal, dataAtualizacao)
            VALUES (42, 1, 'A-001', 'Cadeira giratória', 'cadeira giratoria', 0, 1700000000000)
            """.trimIndent()
        )
        val inseridos = contar(
            dbV16,
            "SELECT COUNT(*) FROM sugestao_descricao WHERE idInventario = 42 AND idPatrimonio = 1"
        )
        assertEquals(
            "INSERT simulando a entidade Room deve ser aceito pela estrutura migrada",
            1,
            inseridos
        )

        // Violação de PK composta deve falhar — confirma constraint ativa.
        var violouPk = false
        try {
            dbV16.execSQL(
                """
                INSERT INTO sugestao_descricao
                    (idInventario, idPatrimonio, numeroPatrimonio, descricao,
                     descricaoNormalizada, coletadoLocal, dataAtualizacao)
                VALUES (42, 1, 'A-001-DUP', 'Dup', 'dup', 0, 1700000000001)
                """.trimIndent()
            )
        } catch (e: android.database.sqlite.SQLiteConstraintException) {
            violouPk = true
        } catch (e: android.database.SQLException) {
            // Algumas versões/ROMs lançam SQLException genérica para UNIQUE violation.
            violouPk = true
        }
        assertTrue(
            "Inserir linha duplicada com a mesma (idInventario, idPatrimonio) " +
                "deve ser rejeitado pela PK composta",
            violouPk
        )
    }

    // ---------- helpers ----------

    /**
     * Cria um [SupportSQLiteOpenHelper] fixado na versão 16 com um
     * `onCreate` customizado. Permite simular um banco pré-migração sem
     * depender do `AppDatabase` real (que está na versão 17).
     */
    private fun createV16Helper(
        context: Context,
        dbName: String,
        onCreateV16: (SupportSQLiteDatabase) -> Unit
    ): SupportSQLiteOpenHelper {
        val callback = object : SupportSQLiteOpenHelper.Callback(16) {
            override fun onCreate(db: SupportSQLiteDatabase) {
                onCreateV16(db)
            }

            override fun onUpgrade(
                db: SupportSQLiteDatabase,
                oldVersion: Int,
                newVersion: Int
            ) {
                // Nenhum upgrade automático — a migração é aplicada manualmente
                // pelo próprio teste para manter controle explícito sobre
                // qual código de migração está sob teste.
            }
        }
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(callback)
            .build()
        return FrameworkSQLiteOpenHelperFactory().create(config)
    }

    private fun listarTabelas(db: SupportSQLiteDatabase): Set<String> {
        val tabelas = mutableSetOf<String>()
        db.query("SELECT name FROM sqlite_master WHERE type = 'table'").use { cursor ->
            while (cursor.moveToNext()) {
                tabelas.add(cursor.getString(0))
            }
        }
        return tabelas
    }

    private fun pragmaTableInfo(
        db: SupportSQLiteDatabase,
        tabela: String
    ): List<ColumnInfo> {
        val colunas = mutableListOf<ColumnInfo>()
        db.query("PRAGMA table_info($tabela)").use { cursor ->
            while (cursor.moveToNext()) {
                colunas.add(
                    ColumnInfo(
                        name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        notnull = cursor.getInt(cursor.getColumnIndexOrThrow("notnull")) != 0,
                        pk = cursor.getInt(cursor.getColumnIndexOrThrow("pk"))
                    )
                )
            }
        }
        return colunas
    }

    private fun pragmaIndexList(
        db: SupportSQLiteDatabase,
        tabela: String
    ): List<IndexInfo> {
        val indices = mutableListOf<IndexInfo>()
        db.query("PRAGMA index_list($tabela)").use { cursor ->
            while (cursor.moveToNext()) {
                indices.add(
                    IndexInfo(
                        name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        unique = cursor.getInt(cursor.getColumnIndexOrThrow("unique")) != 0
                    )
                )
            }
        }
        return indices
    }

    private fun contar(db: SupportSQLiteDatabase, sql: String): Int {
        db.query(sql).use { cursor ->
            assertTrue("Query de contagem deve retornar ao menos uma linha", cursor.moveToFirst())
            return cursor.getInt(0)
        }
    }

    private data class ColumnInfo(
        val name: String,
        val notnull: Boolean,
        /** 0 = não faz parte da PK; >0 = posição na PK (1-based). */
        val pk: Int
    )

    private data class IndexInfo(
        val name: String,
        val unique: Boolean
    )
}
