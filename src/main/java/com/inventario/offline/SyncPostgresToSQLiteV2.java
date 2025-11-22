package com.inventario.offline;

import com.inventario.dao.*;
import com.inventario.model.*;
import com.inventario.util.DatabaseConnection;
import java.sql.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * Classe para sincronizar dados do PostgreSQL para o SQLite offline (Versão 2)
 * Usa as tabelas corretas com prefixo 'local_'
 */
public class SyncPostgresToSQLiteV2 {
    
    private static final Logger LOGGER = Logger.getLogger(SyncPostgresToSQLiteV2.class.getName());
    private static final String SQLITE_DB = "data/inventario.db";
    
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("  SINCRONIZAÇÃO PostgreSQL -> SQLite (V2)");
        System.out.println("=".repeat(60));
        System.out.println();
        
        try {
            SyncPostgresToSQLiteV2 sync = new SyncPostgresToSQLiteV2();
            sync.executarSincronizacao();
            
            System.out.println();
            System.out.println("=".repeat(60));
            System.out.println("  ✅ SINCRONIZAÇÃO CONCLUÍDA COM SUCESSO!");
            System.out.println("=".repeat(60));
            
        } catch (Exception e) {
            System.err.println();
            System.err.println("=".repeat(60));
            System.err.println("  ❌ ERRO NA SINCRONIZAÇÃO!");
            System.err.println("=".repeat(60));
            System.err.println();
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public void executarSincronizacao() throws Exception {
        System.out.println("[1/6] Sincronizando Inventários...");
        sincronizarInventarios();
        
        System.out.println("[2/6] Sincronizando Salas...");
        sincronizarSalas();
        
        System.out.println("[3/6] Sincronizando Responsáveis...");
        sincronizarResponsaveis();
        
        System.out.println("[4/6] Sincronizando Patrimônios...");
        sincronizarPatrimonios();
        
        System.out.println("[5/6] Sincronizando Usuários...");
        sincronizarUsuarios();
        
        System.out.println("[6/6] Sincronizando Participantes...");
        sincronizarParticipantes();
        
        System.out.println();
        System.out.println("✅ Todas as tabelas sincronizadas!");
    }
    
    private void sincronizarInventarios() throws Exception {
        InventarioDAO dao = new InventarioDAO();
        List<Inventario> inventarios = dao.findAll();
        
        try (Connection conn = getSQLiteConnection()) {
            // Limpar ambas as tabelas
            conn.createStatement().execute("DELETE FROM local_inventario");
            conn.createStatement().execute("DELETE FROM TABELA_INVENTARIO");
            
            // Sincronizar para local_inventario (tabela com prefixo local_)
            String sqlLocal = """
                INSERT INTO local_inventario 
                (id, nome_inventario, descricao, data_inicio, data_fim, status_inventario, 
                 percentual_conclusao, sync_status)
                VALUES (?, ?, ?, ?, ?, ?, ?, 'SYNCED')
            """;
            
            // Sincronizar para TABELA_INVENTARIO (tabela compatível com InventarioDAO)
            String sqlTabela = """
                INSERT INTO TABELA_INVENTARIO 
                (ID, NOME, ANO, DATA_INICIO, DATA_FIM, STATUS_INVENTARIO, 
                 RESPONSAVEL_INVENTARIO, PERCENTUAL_CONCLUSAO)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
            
            try (PreparedStatement stmtLocal = conn.prepareStatement(sqlLocal);
                 PreparedStatement stmtTabela = conn.prepareStatement(sqlTabela)) {
                
                for (Inventario inv : inventarios) {
                    // Inserir em local_inventario
                    stmtLocal.setInt(1, inv.getId());
                    stmtLocal.setString(2, inv.getNome());
                    stmtLocal.setString(3, inv.getNome()); // Usar nome como descrição
                    
                    if (inv.getDataInicio() != null) {
                        stmtLocal.setTimestamp(4, new Timestamp(inv.getDataInicio().getTime()));
                    } else {
                        stmtLocal.setNull(4, Types.TIMESTAMP);
                    }
                    
                    if (inv.getDataFim() != null) {
                        stmtLocal.setTimestamp(5, new Timestamp(inv.getDataFim().getTime()));
                    } else {
                        stmtLocal.setNull(5, Types.TIMESTAMP);
                    }
                    
                    stmtLocal.setString(6, inv.getStatusInventario());
                    stmtLocal.setBigDecimal(7, inv.getPercentualConclusao());
                    stmtLocal.addBatch();
                    
                    // Inserir em TABELA_INVENTARIO (compatibilidade com InventarioDAO)
                    stmtTabela.setInt(1, inv.getId());
                    stmtTabela.setString(2, inv.getNome());
                    
                    // Extrair ano da data de início
                    Integer ano = null;
                    if (inv.getDataInicio() != null) {
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        cal.setTime(inv.getDataInicio());
                        ano = cal.get(java.util.Calendar.YEAR);
                    }
                    stmtTabela.setObject(3, ano);
                    
                    if (inv.getDataInicio() != null) {
                        stmtTabela.setDate(4, new java.sql.Date(inv.getDataInicio().getTime()));
                    } else {
                        stmtTabela.setNull(4, Types.DATE);
                    }
                    
                    if (inv.getDataFim() != null) {
                        stmtTabela.setDate(5, new java.sql.Date(inv.getDataFim().getTime()));
                    } else {
                        stmtTabela.setNull(5, Types.DATE);
                    }
                    
                    stmtTabela.setString(6, inv.getStatusInventario());
                    stmtTabela.setString(7, inv.getResponsavelInventario());
                    stmtTabela.setBigDecimal(8, inv.getPercentualConclusao());
                    stmtTabela.addBatch();
                }
                
                stmtLocal.executeBatch();
                stmtTabela.executeBatch();
            }
            
            System.out.println("   ✓ " + inventarios.size() + " inventário(s) sincronizado(s) em ambas as tabelas");
        }
    }
    
    private void sincronizarSalas() throws Exception {
        SalaDAO dao = new SalaDAO();
        // Buscar TODAS as salas (ativas e inativas) para sincronização completa
        List<Sala> salas = dao.listarTodasSalas();
        
        try (Connection conn = getSQLiteConnection()) {
            // Limpar tabela
            conn.createStatement().execute("DELETE FROM local_sala");
            
            String sql = """
                INSERT INTO local_sala 
                (id, nome, descricao, andar, bloco, ativa, sync_status)
                VALUES (?, ?, ?, ?, ?, ?, 'SYNCED')
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Sala sala : salas) {
                    stmt.setInt(1, sala.getIdSala());
                    stmt.setString(2, sala.getNumeroSala()); // Usar como nome
                    stmt.setString(3, sala.getDescricao());
                    stmt.setString(4, sala.getAndar() != null ? sala.getAndar().toString() : null);
                    stmt.setString(5, sala.getBloco());
                    stmt.setBoolean(6, sala.isAtiva());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            
            System.out.println("   ✓ " + salas.size() + " sala(s) sincronizada(s)");
        }
    }
    
    private void sincronizarResponsaveis() throws Exception {
        ResponsavelDAO dao = new ResponsavelDAO();
        List<Responsavel> responsaveis = dao.findAll();
        
        try (Connection conn = getSQLiteConnection()) {
            // Limpar tabela
            conn.createStatement().execute("DELETE FROM local_responsavel");
            
            String sql = """
                INSERT INTO local_responsavel 
                (id, nome, cpf, email, telefone, cargo, setor, ativo, sync_status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'SYNCED')
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Responsavel resp : responsaveis) {
                    stmt.setInt(1, resp.getId());
                    stmt.setString(2, resp.getNome());
                    stmt.setString(3, resp.getCpf());
                    stmt.setString(4, resp.getEmail());
                    stmt.setString(5, resp.getTelefone());
                    stmt.setString(6, resp.getCargo());
                    stmt.setString(7, resp.getNomeSetor());
                    stmt.setBoolean(8, resp.isAtivo());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            
            System.out.println("   ✓ " + responsaveis.size() + " responsável(is) sincronizado(s)");
        }
    }
    
    private void sincronizarPatrimonios() throws Exception {
        PatrimonioDAO dao = new PatrimonioDAO();
        List<Patrimonio> patrimonios = dao.listarTodosComJoins();
        
        try (Connection conn = getSQLiteConnection()) {
            // Limpar tabela
            conn.createStatement().execute("DELETE FROM local_patrimonio");
            
            String sql = """
                INSERT INTO local_patrimonio 
                (id, numero, descricao, descricao_resumida, marca, modelo, 
                 numero_serie, situacao, valor, id_sala, sync_status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'SYNCED')
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Patrimonio pat : patrimonios) {
                    stmt.setInt(1, pat.getId());
                    stmt.setString(2, pat.getNumero());
                    stmt.setString(3, pat.getDescricao());
                    stmt.setString(4, pat.getDescricaoResumida());
                    stmt.setString(5, pat.getMarca());
                    stmt.setString(6, pat.getModelo());
                    stmt.setString(7, pat.getNumeroSerie());
                    stmt.setString(8, pat.getEstadoConservacao());
                    stmt.setBigDecimal(9, pat.getValorAquisicao());
                    stmt.setObject(10, pat.getIdSala());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            
            System.out.println("   ✓ " + patrimonios.size() + " patrimônio(s) sincronizado(s)");
        }
    }
    
    private void sincronizarUsuarios() throws Exception {
        UsuarioDAO dao = new UsuarioDAO();
        List<Usuario> usuarios = dao.findAllIncludingInactive();
        
        try (Connection conn = getSQLiteConnection()) {
            // Limpar tabela (exceto admin que já existe)
            conn.createStatement().execute("DELETE FROM local_usuario WHERE login != 'admin'");
            
            String sql = """
                INSERT OR REPLACE INTO local_usuario 
                (id, login, senha_hash, nome_completo, email, perfil, ativo, sync_status)
                VALUES (?, ?, ?, ?, ?, ?, ?, 'SYNCED')
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Usuario user : usuarios) {
                    stmt.setInt(1, user.getId());
                    stmt.setString(2, user.getLogin());
                    stmt.setString(3, user.getSenhaHash());
                    stmt.setString(4, user.getNomeCompleto());
                    stmt.setString(5, user.getEmail());
                    stmt.setString(6, user.getPerfil().name());
                    stmt.setBoolean(7, user.getAtivo() != null ? user.getAtivo() : true);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            
            System.out.println("   ✓ " + usuarios.size() + " usuário(s) sincronizado(s)");
        }
    }
    
    private void sincronizarParticipantes() throws Exception {
        try (Connection conn = getSQLiteConnection()) {
            // Limpar tabela
            conn.createStatement().execute("DELETE FROM local_participante_inventario");
            
            // Buscar todos os participantes do PostgreSQL
            String sqlSelect = """
                SELECT pi.id_participante, pi.id_inventario, pi.id_usuario, 
                       u.nome_completo, u.email, u.perfil, pi.ativo
                FROM tabela_participante_inventario pi
                LEFT JOIN tabela_usuario u ON pi.id_usuario = u.id
            """;
            
            String sqlInsert = """
                INSERT INTO local_participante_inventario 
                (id, id_inventario, id_usuario, nome_participante, email, perfil, ativo, sync_status)
                VALUES (?, ?, ?, ?, ?, ?, ?, 'SYNCED')
            """;
            
            int count = 0;
            try (Connection pgConn = DatabaseConnection.getConnection();
                 PreparedStatement stmtSelect = pgConn.prepareStatement(sqlSelect);
                 ResultSet rs = stmtSelect.executeQuery();
                 PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert)) {
                
                while (rs.next()) {
                    stmtInsert.setInt(1, rs.getInt("id_participante"));
                    stmtInsert.setInt(2, rs.getInt("id_inventario"));
                    stmtInsert.setInt(3, rs.getInt("id_usuario"));
                    stmtInsert.setString(4, rs.getString("nome_completo"));
                    stmtInsert.setString(5, rs.getString("email"));
                    stmtInsert.setString(6, rs.getString("perfil"));
                    stmtInsert.setBoolean(7, rs.getBoolean("ativo"));
                    stmtInsert.addBatch();
                    count++;
                }
                stmtInsert.executeBatch();
            }
            
            System.out.println("   ✓ " + count + " participante(s) sincronizado(s)");
        }
    }
    
    private Connection getSQLiteConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + SQLITE_DB);
    }
}
