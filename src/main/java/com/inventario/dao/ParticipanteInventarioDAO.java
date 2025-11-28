package com.inventario.dao;

import com.inventario.model.ParticipanteInventario;
import com.inventario.util.DatabaseConnection;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operações de banco de dados relacionadas aos participantes de inventário.
 * Corresponde à TABELA_PARTICIPANTE_INVENTARIO no banco de dados.
 * 
 * Esta classe implementa a regra de negócio que garante que apenas usuários
 * cadastrados no sistema possam participar de inventários através das
 * chaves estrangeiras e validações.
 */
@Repository
public class ParticipanteInventarioDAO {
    
    /**
     * Detecta se está usando SQLite
     * NOTA: Para operações do desktop/servidor, sempre usar PostgreSQL
     */
    private boolean isSQLite() throws SQLException {
        // CORREÇÃO: Sempre retornar false para forçar uso do PostgreSQL
        return false;
    }
    
    /**
     * Retorna o nome correto da tabela
     */
    private String getTableName() throws SQLException {
        return "TABELA_PARTICIPANTE_INVENTARIO";
    }
    
    /**
     * Adiciona um participante ao inventário
     * Valida se o usuário existe e se não é já participante ativo
     * Se existe um registro inativo, reativa-o em vez de criar novo
     */
    public boolean adicionarParticipante(ParticipanteInventario participante) {
        // Primeiro valida se o usuário existe
        if (!usuarioExiste(participante.getIdUsuario())) {
            throw new IllegalArgumentException("Usuário não encontrado no sistema. ID: " + participante.getIdUsuario());
        }
        
        // Valida se o inventário existe
        if (!inventarioExiste(participante.getIdInventario())) {
            throw new IllegalArgumentException("Inventário não encontrado. ID: " + participante.getIdInventario());
        }
        
        // Verifica se já é participante ativo
        if (isParticipanteAtivo(participante.getIdInventario(), participante.getIdUsuario())) {
            throw new IllegalArgumentException("Usuário já é participante ativo deste inventário.");
        }
        
        // Verifica se existe um registro inativo para reativar
        if (reativarParticipanteInativo(participante)) {
            return true;
        }
        
        // Se não existe registro inativo, cria um novo
        String sql = "INSERT INTO TABELA_PARTICIPANTE_INVENTARIO " +
                    "(ID_INVENTARIO, ID_USUARIO, PAPEL, DATA_INCLUSAO, ATIVO, OBSERVACOES) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, participante.getIdInventario());
            stmt.setInt(2, participante.getIdUsuario());
            stmt.setString(3, participante.getPapel());
            stmt.setTimestamp(4, Timestamp.valueOf(participante.getDataInclusao()));
            stmt.setBoolean(5, participante.getAtivo());
            stmt.setString(6, participante.getObservacoes());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        participante.setIdParticipante(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao adicionar participante: " + e.getMessage());
            throw new RuntimeException("Erro ao adicionar participante ao inventário", e);
        }
        
        return false;
    }
    
    /**
     * Remove um participante do inventário (soft delete)
     */
    public boolean removerParticipante(int idInventario, int idUsuario) {
        String sql = "UPDATE tabela_participante_inventario SET " +
                    "ativo = FALSE, data_remocao = CURRENT_TIMESTAMP, " +
                    "data_ultima_atualizacao = CURRENT_TIMESTAMP " +
                    "WHERE id_inventario = ? AND id_usuario = ? AND ativo = TRUE";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            stmt.setInt(2, idUsuario);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erro ao remover participante: " + e.getMessage());
            throw new RuntimeException("Erro ao remover participante do inventário", e);
        }
    }
    
    /**
     * Reativa um participante inativo se existir
     * Retorna true se conseguiu reativar, false se não existe registro inativo
     */
    private boolean reativarParticipanteInativo(ParticipanteInventario participante) {
        String sql = "UPDATE tabela_participante_inventario SET " +
                    "ativo = TRUE, data_remocao = NULL, papel = ?, " +
                    "data_ultima_atualizacao = CURRENT_TIMESTAMP, observacoes = ? " +
                    "WHERE id_inventario = ? AND id_usuario = ? AND ativo = FALSE";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, participante.getPapel());
            stmt.setString(2, participante.getObservacoes());
            stmt.setInt(3, participante.getIdInventario());
            stmt.setInt(4, participante.getIdUsuario());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Buscar o ID do participante reativado
                String selectSql = "SELECT id_participante FROM tabela_participante_inventario " +
                                  "WHERE id_inventario = ? AND id_usuario = ? AND ativo = TRUE";
                
                try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                    selectStmt.setInt(1, participante.getIdInventario());
                    selectStmt.setInt(2, participante.getIdUsuario());
                    
                    try (ResultSet rs = selectStmt.executeQuery()) {
                        if (rs.next()) {
                            participante.setIdParticipante(rs.getInt("id_participante"));
                        }
                    }
                }
                
                System.out.println("[DEBUG] Participante reativado: ID_INVENTARIO=" + 
                                 participante.getIdInventario() + ", ID_USUARIO=" + participante.getIdUsuario());
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao reativar participante: " + e.getMessage());
            // Não lança exceção aqui, apenas retorna false para tentar criar novo registro
        }
        
        return false;
    }
    
    /**
     * Altera o papel de um participante
     */
    public boolean alterarPapelParticipante(int idInventario, int idUsuario, String novoPapel) {
        String sql = "UPDATE tabela_participante_inventario SET " +
                    "papel = ?, data_ultima_atualizacao = CURRENT_TIMESTAMP " +
                    "WHERE id_inventario = ? AND id_usuario = ? AND ativo = TRUE";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, novoPapel);
            stmt.setInt(2, idInventario);
            stmt.setInt(3, idUsuario);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erro ao alterar papel do participante: " + e.getMessage());
            throw new RuntimeException("Erro ao alterar papel do participante", e);
        }
    }
    
    /**
     * Lista todos os participantes ativos de um inventário
     */
    public List<ParticipanteInventario> listarParticipantesInventario(int idInventario) {
        List<ParticipanteInventario> participantes = new ArrayList<>();
        
        String sql = "SELECT p.id_participante, p.id_inventario, p.id_usuario, p.papel, " +
                    "p.data_inclusao, p.data_remocao, p.ativo, p.observacoes, " +
                    "p.data_ultima_atualizacao, u.nome_completo as nome_usuario " +
                    "FROM tabela_participante_inventario p " +
                    "INNER JOIN tabela_usuario u ON p.id_usuario = u.id " +
                    "WHERE p.id_inventario = ? AND p.ativo = TRUE " +
                    "ORDER BY p.papel, u.nome_completo";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    participantes.add(criarParticipanteFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao listar participantes: " + e.getMessage());
            throw new RuntimeException("Erro ao listar participantes do inventário", e);
        }
        
        return participantes;
    }
    
    /**
     * Busca participantes por papel
     */
    public List<ParticipanteInventario> buscarParticipantesPorPapel(int idInventario, String papel) {
        List<ParticipanteInventario> participantes = new ArrayList<>();
        
        String sql = "SELECT p.id_participante, p.id_inventario, p.id_usuario, p.papel, " +
                    "p.data_inclusao, p.data_remocao, p.ativo, p.observacoes, " +
                    "p.data_ultima_atualizacao, u.nome_completo as nome_usuario " +
                    "FROM tabela_participante_inventario p " +
                    "INNER JOIN tabela_usuario u ON p.id_usuario = u.id " +
                    "WHERE p.id_inventario = ? AND p.papel = ? AND p.ativo = TRUE " +
                    "ORDER BY u.nome_completo";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            stmt.setString(2, papel);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    participantes.add(criarParticipanteFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar participantes por papel: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar participantes por papel", e);
        }
        
        return participantes;
    }
    
    /**
     * Verifica se um usuário é participante ativo de um inventário
     */
    public boolean isParticipanteAtivo(int idInventario, int idUsuario) {
        String sql = "SELECT COUNT(*) FROM tabela_participante_inventario " +
                    "WHERE id_inventario = ? AND id_usuario = ? AND ativo = TRUE";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            stmt.setInt(2, idUsuario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao verificar participante: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Conta quantos coordenadores ativos um inventário possui
     */
    public int contarCoordenadoresAtivos(int idInventario) {
        String sql = "SELECT COUNT(*) FROM tabela_participante_inventario " +
                    "WHERE id_inventario = ? AND papel = 'COORDENADOR' AND ativo = TRUE";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao contar coordenadores: " + e.getMessage());
        }
        
        return 0;
    }
    
    /**
     * Lista inventários dos quais um usuário participa
     */
    public List<Integer> listarInventariosDoUsuario(int idUsuario) {
        List<Integer> inventarios = new ArrayList<>();
        
        String sql = "SELECT DISTINCT id_inventario FROM tabela_participante_inventario " +
                    "WHERE id_usuario = ? AND ativo = TRUE";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsuario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    inventarios.add(rs.getInt("id_inventario"));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao listar inventários do usuário: " + e.getMessage());
        }
        
        return inventarios;
    }
    
    /**
     * Busca o ID do participante baseado no ID do usuário e inventário
     * Retorna null se o usuário não for participante ativo do inventário
     */
    public Integer buscarIdParticipantePorUsuario(int idInventario, int idUsuario) {
        System.out.println("[DEBUG ParticipanteInventarioDAO] ========================================");
        System.out.println("[DEBUG ParticipanteInventarioDAO] Buscando participante:");
        System.out.println("[DEBUG ParticipanteInventarioDAO]   idInventario = " + idInventario);
        System.out.println("[DEBUG ParticipanteInventarioDAO]   idUsuario = " + idUsuario);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // ✅ CORRIGIDO: Detectar se é SQLite ou PostgreSQL
            boolean isSQLite = conn.getMetaData().getDriverName().toLowerCase().contains("sqlite");
            String tableName = isSQLite ? "local_participante_inventario" : "tabela_participante_inventario";
            
            System.out.println("[DEBUG ParticipanteInventarioDAO]   Banco: " + (isSQLite ? "SQLite" : "PostgreSQL"));
            System.out.println("[DEBUG ParticipanteInventarioDAO]   Tabela: " + tableName);
            
            // Debug: verificar se existe registro (com ou sem ativo)
            String sqlDebug = "SELECT id_participante, ativo FROM " + tableName + " " +
                             "WHERE id_inventario = ? AND id_usuario = ?";
            
            try (PreparedStatement stmtDebug = conn.prepareStatement(sqlDebug)) {
                stmtDebug.setInt(1, idInventario);
                stmtDebug.setInt(2, idUsuario);
                
                try (ResultSet rsDebug = stmtDebug.executeQuery()) {
                    if (rsDebug.next()) {
                        int idPart = rsDebug.getInt("id_participante");
                        boolean ativo = rsDebug.getBoolean("ativo");
                        System.out.println("[DEBUG ParticipanteInventarioDAO] Registro encontrado:");
                        System.out.println("[DEBUG ParticipanteInventarioDAO]   id_participante = " + idPart);
                        System.out.println("[DEBUG ParticipanteInventarioDAO]   ativo = " + ativo);
                    } else {
                        System.out.println("[DEBUG ParticipanteInventarioDAO] Nenhum registro encontrado (sem filtro ativo)");
                    }
                }
            }
            
            // Query principal com filtro de ativo
            String sql = "SELECT id_participante FROM " + tableName + " " +
                        "WHERE id_inventario = ? AND id_usuario = ? AND ativo = " + (isSQLite ? "1" : "TRUE");
            
            System.out.println("[DEBUG ParticipanteInventarioDAO]   SQL = " + sql);
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idInventario);
                stmt.setInt(2, idUsuario);
                
                System.out.println("[DEBUG ParticipanteInventarioDAO] Executando query principal...");
                
                try (ResultSet rs = stmt.executeQuery()) {
                    System.out.println("[DEBUG ParticipanteInventarioDAO] Query executada com sucesso");
                    
                    if (rs.next()) {
                        int idParticipante = rs.getInt("id_participante");
                        System.out.println("[DEBUG ParticipanteInventarioDAO] ✓ Participante ENCONTRADO!");
                        System.out.println("[DEBUG ParticipanteInventarioDAO]   id_participante = " + idParticipante);
                        System.out.println("[DEBUG ParticipanteInventarioDAO] ========================================");
                        return idParticipante;
                    } else {
                        System.out.println("[DEBUG ParticipanteInventarioDAO] ✗ Participante NÃO encontrado com ativo=TRUE");
                        System.out.println("[DEBUG ParticipanteInventarioDAO] ========================================");
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("[ERROR ParticipanteInventarioDAO] ========================================");
            System.err.println("[ERROR ParticipanteInventarioDAO] EXCEÇÃO SQL capturada:");
            System.err.println("[ERROR ParticipanteInventarioDAO]   Mensagem: " + e.getMessage());
            System.err.println("[ERROR ParticipanteInventarioDAO]   SQLState: " + e.getSQLState());
            System.err.println("[ERROR ParticipanteInventarioDAO]   ErrorCode: " + e.getErrorCode());
            System.err.println("[ERROR ParticipanteInventarioDAO] ========================================");
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Valida se um usuário existe no sistema
     * Esta é a validação principal que garante que apenas usuários
     * cadastrados possam participar de inventários
     */
    private boolean usuarioExiste(int idUsuario) {
        String sql = "SELECT COUNT(*) FROM tabela_usuario WHERE id = ? AND ativo = TRUE";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsuario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao verificar usuário: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Valida se um inventário existe
     */
    private boolean inventarioExiste(int idInventario) {
        String sql = "SELECT COUNT(*) FROM tabela_inventario WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao verificar inventário: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Cria um objeto ParticipanteInventario a partir do ResultSet
     */
    private ParticipanteInventario criarParticipanteFromResultSet(ResultSet rs) throws SQLException {
        ParticipanteInventario participante = new ParticipanteInventario();
        
        participante.setIdParticipante(rs.getInt("id_participante"));
        participante.setIdInventario(rs.getInt("id_inventario"));
        participante.setIdUsuario(rs.getInt("id_usuario"));
        participante.setPapel(rs.getString("papel"));
        
        Timestamp dataInclusao = rs.getTimestamp("data_inclusao");
        if (dataInclusao != null) {
            participante.setDataInclusao(dataInclusao.toLocalDateTime());
        }
        
        Timestamp dataRemocao = rs.getTimestamp("data_remocao");
        if (dataRemocao != null) {
            participante.setDataRemocao(dataRemocao.toLocalDateTime());
        }
        
        participante.setAtivo(rs.getBoolean("ativo"));
        participante.setObservacoes(rs.getString("observacoes"));
        
        Timestamp dataUltimaAtualizacao = rs.getTimestamp("data_ultima_atualizacao");
        if (dataUltimaAtualizacao != null) {
            participante.setDataUltimaAtualizacao(dataUltimaAtualizacao.toLocalDateTime());
        }
        
        // Nome do usuário (do JOIN)
        participante.setNomeUsuario(rs.getString("nome_usuario"));
        
        return participante;
    }
}