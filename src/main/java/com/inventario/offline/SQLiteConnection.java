package com.inventario.offline;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Gerenciador de conexão com banco SQLite local para modo offline
 * 
 * @author Sistema de Inventário IFMT
 * @version 1.0.0
 */
public class SQLiteConnection {
    
    private static final Logger LOGGER = Logger.getLogger(SQLiteConnection.class.getName());
    private static SQLiteConnection instance;
    
    private static final String DEFAULT_DB_PATH = "./data/inventario.db";
    private static final String JDBC_URL_PREFIX = "jdbc:sqlite:";
    
    private String dbPath;
    private Connection connection;
    
    private SQLiteConnection() {
        this.dbPath = DEFAULT_DB_PATH;
    }
    
    /**
     * Obtém a instância singleton do SQLiteConnection
     * @return Instância do SQLiteConnection
     */
    public static synchronized SQLiteConnection getInstance() {
        if (instance == null) {
            instance = new SQLiteConnection();
        }
        return instance;
    }
    
    /**
     * Define o caminho do banco de dados
     * @param dbPath Caminho para o arquivo do banco
     */
    public void setDatabasePath(String dbPath) {
        this.dbPath = dbPath;
    }
    
    /**
     * Obtém uma conexão com o banco SQLite
     * CORREÇÃO: Agora sempre retorna uma nova conexão independente para evitar
     * problemas de concorrência e "stmt pointer is closed".
     * A conexão DEVE ser fechada pelo chamador após o uso (try-with-resources).
     * @return Nova conexão independente
     * @throws SQLException
     */
    public synchronized Connection getConnection() throws SQLException {
        // CORREÇÃO: Sempre criar nova conexão para evitar problemas de concorrência
        return getNewConnection();
    }
    
    /**
     * Obtém a conexão compartilhada (uso interno apenas)
     * ATENÇÃO: Esta conexão NÃO deve ser fechada pelo chamador.
     * @return Conexão compartilhada
     * @throws SQLException
     */
    public synchronized Connection getSharedConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            createConnection();
        }
        return connection;
    }
    
    /**
     * Cria uma nova conexão independente com o banco SQLite
     * Esta conexão DEVE ser fechada pelo chamador após o uso.
     * Use este método quando precisar de operações isoladas ou em threads diferentes.
     * @return Nova conexão independente
     * @throws SQLException
     */
    public Connection getNewConnection() throws SQLException {
        try {
            // Garante que o diretório existe
            ensureDirectoryExists();
            
            // Carrega o driver SQLite
            Class.forName("org.sqlite.JDBC");
            
            // Cria uma nova conexão independente
            String url = JDBC_URL_PREFIX + dbPath;
            Connection newConn = DriverManager.getConnection(url);
            
            // Configura a conexão
            try (Statement stmt = newConn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
                stmt.execute("PRAGMA journal_mode = WAL");
                stmt.execute("PRAGMA busy_timeout = 30000");
                stmt.execute("PRAGMA synchronous = NORMAL");
            }
            
            LOGGER.fine("Nova conexão SQLite criada: " + dbPath);
            return newConn;
            
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver SQLite não encontrado", e);
        }
    }
    
    /**
     * Cria uma nova conexão com o banco SQLite
     * @throws SQLException
     */
    private void createConnection() throws SQLException {
        try {
            // Garante que o diretório existe
            ensureDirectoryExists();
            
            // Carrega o driver SQLite
            Class.forName("org.sqlite.JDBC");
            
            // Cria a conexão
            String url = JDBC_URL_PREFIX + dbPath;
            connection = DriverManager.getConnection(url);
            
            // Configura a conexão
            configureConnection();
            
            LOGGER.info("Conexão SQLite estabelecida: " + dbPath);
            
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver SQLite não encontrado", e);
        }
    }
    
    /**
     * Configura parâmetros da conexão SQLite
     * @throws SQLException
     */
    private void configureConnection() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Habilita chaves estrangeiras
            stmt.execute("PRAGMA foreign_keys = ON");
            
            // Configura modo WAL para melhor concorrência
            stmt.execute("PRAGMA journal_mode = WAL");
            
            // Configura timeout para locks
            stmt.execute("PRAGMA busy_timeout = 30000");
            
            // Otimizações de performance
            stmt.execute("PRAGMA synchronous = NORMAL");
            stmt.execute("PRAGMA cache_size = 10000");
            stmt.execute("PRAGMA temp_store = MEMORY");
        }
    }
    
    /**
     * Garante que o diretório do banco de dados existe
     */
    private void ensureDirectoryExists() {
        File dbFile = new File(dbPath);
        File parentDir = dbFile.getParentFile();
        
        if (parentDir != null && !parentDir.exists()) {
            if (parentDir.mkdirs()) {
                LOGGER.info("Diretório criado: " + parentDir.getAbsolutePath());
            } else {
                LOGGER.warning("Falha ao criar diretório: " + parentDir.getAbsolutePath());
            }
        }
    }
    
    /**
     * Inicializa o banco de dados criando as tabelas necessárias
     * @throws SQLException
     */
    public void initializeDatabase() throws SQLException {
        System.out.println("========================================");
        System.out.println("=== INICIALIZANDO BANCO SQLITE ===");
        System.out.println("========================================");
        LOGGER.info("Inicializando banco de dados SQLite");
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            System.out.println(">>> Criando tabelas do sistema...");
            // Cria tabelas do sistema offline
            createSystemTables(stmt);
            System.out.println(">>> ✅ Tabelas do sistema criadas");
            
            System.out.println(">>> Criando tabelas espelho...");
            // Cria tabelas espelho das entidades principais
            createMirrorTables(stmt);
            System.out.println(">>> ✅ Tabelas espelho criadas");
            
            System.out.println(">>> Criando tabelas de compatibilidade...");
            // Cria tabelas de compatibilidade (para sistema desktop)
            createCompatibilityTables(stmt);
            System.out.println(">>> ✅ Tabelas de compatibilidade criadas");
            
            System.out.println(">>> Criando índices...");
            // Cria índices para otimização
            createIndexes(stmt);
            System.out.println(">>> ✅ Índices criados");
            
            System.out.println("========================================");
            System.out.println("=== BANCO SQLITE INICIALIZADO ===");
            System.out.println("========================================");
            LOGGER.info("Banco de dados SQLite inicializado com sucesso");
            
        } catch (SQLException e) {
            System.err.println("========================================");
            System.err.println("=== ERRO AO INICIALIZAR SQLITE ===");
            System.err.println("========================================");
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, "Erro ao inicializar banco SQLite", e);
            throw e;
        }
    }
    
    /**
     * Cria tabelas do sistema de sincronização
     * @param stmt Statement para execução
     * @throws SQLException
     */
    private void createSystemTables(Statement stmt) throws SQLException {
        System.out.println("  - Criando sync_control...");
        // Tabela de controle de sincronização
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS sync_control (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                table_name TEXT NOT NULL,
                record_id INTEGER NOT NULL,
                operation TEXT NOT NULL,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                synced BOOLEAN DEFAULT FALSE,
                conflict_resolved BOOLEAN DEFAULT FALSE,
                data_json TEXT,
                error_message TEXT
            )
        """);
        
        System.out.println("  - Criando sync_metadata...");
        // Tabela de metadados de sincronização
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS sync_metadata (
                key TEXT PRIMARY KEY,
                value TEXT,
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """);
        
        System.out.println("  - Criando offline_logs...");
        // Tabela de logs offline
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS offline_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                level TEXT NOT NULL,
                message TEXT NOT NULL,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                details TEXT
            )
        """);
        
        System.out.println("  - Inserindo metadados iniciais...");
        // Insere metadados iniciais
        stmt.execute("""
            INSERT OR IGNORE INTO sync_metadata (key, value) VALUES 
            ('last_sync_timestamp', '1970-01-01 00:00:00'),
            ('sync_mode', 'AUTO'),
            ('conflict_resolution_strategy', 'TIMESTAMP_WINS'),
            ('db_version', '1.0.0')
        """);
    }
    
    /**
     * Cria tabelas espelho das entidades principais
     * @param stmt Statement para execução
     * @throws SQLException
     */
    private void createMirrorTables(Statement stmt) throws SQLException {
        
        System.out.println("  - Criando local_usuario...");
        // Tabela local de usuários (CRÍTICA PARA LOGIN OFFLINE)
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS local_usuario (
                id INTEGER PRIMARY KEY,
                login TEXT UNIQUE NOT NULL,
                senha_hash TEXT NOT NULL,
                nome_completo TEXT,
                email TEXT,
                matricula TEXT,
                perfil TEXT,
                ativo BOOLEAN DEFAULT TRUE,
                bloqueado BOOLEAN DEFAULT FALSE,
                tentativas_login INTEGER DEFAULT 0,
                primeiro_acesso BOOLEAN DEFAULT TRUE,
                data_ultimo_acesso DATETIME,
                sync_status TEXT DEFAULT 'PENDING',
                last_modified DATETIME DEFAULT CURRENT_TIMESTAMP,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """);
        
        System.out.println("  - Criando local_patrimonio...");
        // Tabela local de patrimônios
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS local_patrimonio (
                id INTEGER PRIMARY KEY,
                numero TEXT,
                descricao TEXT,
                descricao_resumida TEXT,
                marca TEXT,
                modelo TEXT,
                numero_serie TEXT,
                situacao TEXT,
                valor DECIMAL(15,2),
                data_aquisicao DATE,
                id_setor INTEGER,
                id_sala INTEGER,
                observacoes TEXT,
                sync_status TEXT DEFAULT 'PENDING',
                last_modified DATETIME DEFAULT CURRENT_TIMESTAMP,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """);
        
        System.out.println("  - Criando local_coleta...");
        // Tabela local de coletas
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS local_coleta (
                id INTEGER PRIMARY KEY,
                id_patrimonio INTEGER,
                id_inventario INTEGER,
                id_participante INTEGER,
                numero_patrimonio TEXT,
                data_coleta DATETIME,
                localizacao_atual TEXT,
                localizacao_encontrada TEXT,
                situacao_encontrada TEXT,
                observacoes TEXT,
                foto_patrimonio TEXT,
                sem_etiqueta BOOLEAN DEFAULT FALSE,
                descricao_sem_etiqueta TEXT,
                sync_status TEXT DEFAULT 'PENDING',
                last_modified DATETIME DEFAULT CURRENT_TIMESTAMP,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """);
        
        System.out.println("  - Criando local_inventario...");
        // Tabela local de inventários
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS local_inventario (
                id INTEGER PRIMARY KEY,
                nome TEXT NOT NULL,
                descricao TEXT,
                data_inicio DATE,
                data_fim DATE,
                status TEXT DEFAULT 'PLANEJAMENTO',
                id_responsavel INTEGER,
                observacoes TEXT,
                sync_status TEXT DEFAULT 'PENDING',
                last_modified DATETIME DEFAULT CURRENT_TIMESTAMP,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """);
        
        System.out.println("  - Criando local_sala...");
        // Tabela local de salas
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS local_sala (
                id INTEGER PRIMARY KEY,
                nome TEXT,
                descricao TEXT,
                bloco TEXT,
                andar TEXT,
                capacidade INTEGER,
                tipo TEXT,
                ativa BOOLEAN DEFAULT TRUE,
                sync_status TEXT DEFAULT 'SYNCED',
                last_modified DATETIME DEFAULT CURRENT_TIMESTAMP,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """);
        
        System.out.println("  - Criando local_responsavel...");
        // Tabela local de responsáveis
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS local_responsavel (
                id INTEGER PRIMARY KEY,
                nome TEXT NOT NULL,
                cpf TEXT,
                matricula TEXT,
                email TEXT,
                telefone TEXT,
                cargo TEXT,
                setor TEXT,
                ativo BOOLEAN DEFAULT TRUE,
                sync_status TEXT DEFAULT 'SYNCED',
                last_modified DATETIME DEFAULT CURRENT_TIMESTAMP,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """);
        
        System.out.println("  - Criando local_participante_inventario...");
        // Tabela local de participantes do inventário (CRÍTICA PARA SINCRONIZAÇÃO)
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS local_participante_inventario (
                id_participante INTEGER PRIMARY KEY,
                id_inventario INTEGER NOT NULL,
                id_usuario INTEGER NOT NULL,
                papel TEXT DEFAULT 'COLETOR',
                ativo BOOLEAN DEFAULT TRUE,
                data_inclusao DATETIME DEFAULT CURRENT_TIMESTAMP,
                sync_status TEXT DEFAULT 'SYNCED',
                last_modified DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """);
    }
    
    /**
     * Cria tabelas de compatibilidade para o sistema desktop
     * @param stmt Statement para execução
     * @throws SQLException
     */
    private void createCompatibilityTables(Statement stmt) throws SQLException {
        
        System.out.println("  - Criando TABELA_INVENTARIO...");
        // Tabela TABELA_INVENTARIO (compatibilidade com InventarioDAO)
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS TABELA_INVENTARIO (
                ID INTEGER PRIMARY KEY AUTOINCREMENT,
                NOME TEXT NOT NULL,
                ANO INTEGER,
                DATA_INICIO DATE NOT NULL,
                DATA_FIM DATE,
                OBSERVACAO TEXT,
                STATUS_INVENTARIO TEXT DEFAULT 'PLANEJADO',
                RESPONSAVEL_INVENTARIO TEXT,
                TOTAL_PATRIMONIOS INTEGER DEFAULT 0,
                PATRIMONIOS_COLETADOS INTEGER DEFAULT 0,
                PERCENTUAL_CONCLUSAO DECIMAL(5,2) DEFAULT 0.00,
                DATA_CRIACAO DATETIME DEFAULT CURRENT_TIMESTAMP,
                DATA_ULTIMA_ATUALIZACAO DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """);
        
        System.out.println("  - Criando SALA...");
        // Tabela SALA (compatibilidade com SalaDAO)
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS SALA (
                ID_SALA INTEGER PRIMARY KEY AUTOINCREMENT,
                NUMERO_SALA TEXT NOT NULL,
                NOME_SALA TEXT,
                ANDAR TEXT,
                BLOCO TEXT,
                CAPACIDADE INTEGER,
                TIPO_SALA TEXT,
                ATIVA BOOLEAN DEFAULT TRUE,
                DATA_CADASTRO DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """);
        
        LOGGER.info("Tabelas de compatibilidade criadas com sucesso");
    }
    
    /**
     * Cria índices para otimização
     * @param stmt Statement para execução
     * @throws SQLException
     */
    private void createIndexes(Statement stmt) throws SQLException {
        
        // Índices para sync_control
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_sync_control_table ON sync_control(table_name)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_sync_control_synced ON sync_control(synced)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_sync_control_timestamp ON sync_control(timestamp)");
        
        // Índices para tabela de usuários
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_local_usuario_login ON local_usuario(login)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_local_usuario_sync ON local_usuario(sync_status)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_local_usuario_ativo ON local_usuario(ativo)");
        
        // Índices para tabelas locais
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_local_patrimonio_numero ON local_patrimonio(numero)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_local_patrimonio_sync ON local_patrimonio(sync_status)");
        
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_local_coleta_patrimonio ON local_coleta(id_patrimonio)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_local_coleta_inventario ON local_coleta(id_inventario)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_local_coleta_sync ON local_coleta(sync_status)");
        
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_local_inventario_status ON local_inventario(status)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_local_inventario_sync ON local_inventario(sync_status)");
        
        // Índices para tabela de salas
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_local_sala_nome ON local_sala(nome)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_local_sala_ativa ON local_sala(ativa)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_local_sala_sync ON local_sala(sync_status)");
        
        // Índices para tabela de responsáveis
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_local_responsavel_nome ON local_responsavel(nome)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_local_responsavel_ativo ON local_responsavel(ativo)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_local_responsavel_sync ON local_responsavel(sync_status)");
    }
    
    /**
     * Testa a conexão com o banco
     * @return true se a conexão está funcionando
     */
    public boolean testConnection() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("SELECT 1");
            return true;
            
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Falha no teste de conexão SQLite", e);
            return false;
        }
    }
    
    /**
     * Fecha a conexão com o banco
     */
    public synchronized void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                LOGGER.info("Conexão SQLite fechada");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erro ao fechar conexão SQLite", e);
            } finally {
                connection = null;
            }
        }
    }
    
    /**
     * Obtém informações sobre o banco de dados
     * @return String com informações do banco
     */
    public String getDatabaseInfo() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            var rs = stmt.executeQuery("PRAGMA database_list");
            StringBuilder info = new StringBuilder();
            info.append("Banco SQLite: ").append(dbPath).append("\n");
            
            while (rs.next()) {
                info.append("Nome: ").append(rs.getString("name"))
                    .append(", Arquivo: ").append(rs.getString("file"))
                    .append("\n");
            }
            
            return info.toString();
            
        } catch (SQLException e) {
            return "Erro ao obter informações: " + e.getMessage();
        }
    }
    
    /**
     * Executa vacuum no banco para otimização
     * @throws SQLException
     */
    public void vacuum() throws SQLException {
        LOGGER.info("Executando VACUUM no banco SQLite");
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("VACUUM");
            LOGGER.info("VACUUM executado com sucesso");
            
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Erro ao executar VACUUM", e);
            throw e;
        }
    }
    
    /**
     * Cleanup ao finalizar a aplicação
     */
    public void shutdown() {
        LOGGER.info("Finalizando SQLiteConnection");
        closeConnection();
    }
}