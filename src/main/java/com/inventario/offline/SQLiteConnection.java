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
    
    private static final String DEFAULT_DB_PATH = "./data/inventario_offline.db";
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
     * @return Conexão ativa
     * @throws SQLException
     */
    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            createConnection();
        }
        return connection;
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
        LOGGER.info("Inicializando banco de dados SQLite");
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Cria tabelas do sistema offline
            createSystemTables(stmt);
            
            // Cria tabelas espelho das entidades principais
            createMirrorTables(stmt);
            
            // Cria índices para otimização
            createIndexes(stmt);
            
            LOGGER.info("Banco de dados SQLite inicializado com sucesso");
            
        } catch (SQLException e) {
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
        
        // Tabela de metadados de sincronização
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS sync_metadata (
                key TEXT PRIMARY KEY,
                value TEXT,
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """);
        
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
        
        // Tabela local de participantes
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS local_participante_inventario (
                id INTEGER PRIMARY KEY,
                id_inventario INTEGER,
                id_usuario INTEGER,
                nome_participante TEXT,
                email TEXT,
                perfil TEXT,
                ativo BOOLEAN DEFAULT TRUE,
                sync_status TEXT DEFAULT 'PENDING',
                last_modified DATETIME DEFAULT CURRENT_TIMESTAMP,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """);
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