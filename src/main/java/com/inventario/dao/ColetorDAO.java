package com.inventario.dao;

import com.inventario.model.Coletor;
import com.inventario.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operações de banco de dados relacionadas ao Coletor.
 * Corresponde à TABELA_COLETOR no banco de dados.
 */
public class ColetorDAO {
    
    public boolean inserirColetor(Coletor coletor) {
        String sql = "INSERT INTO TABELA_COLETOR (NOME_COLETOR, CPF, EMAIL, TELEFONE, ATIVO) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, coletor.getNomeColetor());
            stmt.setString(2, coletor.getCpf());
            stmt.setString(3, coletor.getEmail());
            stmt.setString(4, coletor.getTelefone());
            stmt.setBoolean(5, coletor.getAtivo());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        coletor.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao inserir coletor: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    public boolean atualizarColetor(Coletor coletor) {
        String sql = "UPDATE TABELA_COLETOR SET NOME_COLETOR = ?, CPF = ?, EMAIL = ?, " +
                    "TELEFONE = ?, ATIVO = ?, DATA_ATUALIZACAO = CURRENT_TIMESTAMP WHERE ID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, coletor.getNomeColetor());
            stmt.setString(2, coletor.getCpf());
            stmt.setString(3, coletor.getEmail());
            stmt.setString(4, coletor.getTelefone());
            stmt.setBoolean(5, coletor.getAtivo());
            stmt.setInt(6, coletor.getId());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar coletor: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    public boolean excluirColetor(int id) {
        String sql = "UPDATE TABELA_COLETOR SET ATIVO = FALSE WHERE ID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erro ao excluir coletor: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    public List<Coletor> listarColetores() {
        List<Coletor> coletores = new ArrayList<>();
        String sql = "SELECT * FROM TABELA_COLETOR WHERE ATIVO = TRUE ORDER BY NOME_COLETOR";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                coletores.add(criarColetorFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao listar coletores: " + e.getMessage());
            e.printStackTrace();
        }
        
        return coletores;
    }
    
    public List<Coletor> listarTodosColetores() {
        List<Coletor> coletores = new ArrayList<>();
        String sql = "SELECT * FROM TABELA_COLETOR ORDER BY NOME_COLETOR";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                coletores.add(criarColetorFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao listar todos os coletores: " + e.getMessage());
            e.printStackTrace();
        }
        
        return coletores;
    }
    
    public Coletor buscarColetorPorId(int id) {
        String sql = "SELECT * FROM TABELA_COLETOR WHERE ID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return criarColetorFromResultSet(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar coletor por ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    public List<Coletor> buscarColetoresPorFiltro(String filtro) {
        List<Coletor> coletores = new ArrayList<>();
        String sql = "SELECT * FROM TABELA_COLETOR WHERE ATIVO = TRUE AND (" +
                    "UPPER(NOME_COLETOR) LIKE UPPER(?) OR " +
                    "UPPER(EMAIL) LIKE UPPER(?) OR " +
                    "CPF LIKE ?) " +
                    "ORDER BY NOME_COLETOR";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String filtroLike = "%" + filtro + "%";
            stmt.setString(1, filtroLike);
            stmt.setString(2, filtroLike);
            stmt.setString(3, filtroLike);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    coletores.add(criarColetorFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar coletores por filtro: " + e.getMessage());
            e.printStackTrace();
        }
        
        return coletores;
    }
    
    public Coletor buscarColetorPorCpf(String cpf) {
        String sql = "SELECT * FROM TABELA_COLETOR WHERE CPF = ? AND ATIVO = TRUE";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, cpf);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return criarColetorFromResultSet(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar coletor por CPF: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    public Coletor buscarColetorPorEmail(String email) {
        String sql = "SELECT * FROM TABELA_COLETOR WHERE UPPER(EMAIL) = UPPER(?) AND ATIVO = TRUE";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return criarColetorFromResultSet(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar coletor por email: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    public boolean cpfExiste(String cpf) {
        String sql = "SELECT COUNT(*) FROM TABELA_COLETOR WHERE CPF = ? AND ATIVO = TRUE";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, cpf);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao verificar se CPF existe: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    public boolean emailExiste(String email) {
        String sql = "SELECT COUNT(*) FROM TABELA_COLETOR WHERE UPPER(EMAIL) = UPPER(?) AND ATIVO = TRUE";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao verificar se email existe: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    public int contarColetasRealizadas(int idColetor) {
        String sql = "SELECT COUNT(*) FROM TABELA_COLETA c " +
                    "LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                    "WHERE pi.ID_USUARIO = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idColetor);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao contar coletas realizadas: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
    
    private Coletor criarColetorFromResultSet(ResultSet rs) throws SQLException {
        Coletor coletor = new Coletor();
        
        coletor.setId(rs.getInt("ID"));
        coletor.setNomeColetor(rs.getString("NOME_COLETOR"));
        coletor.setCpf(rs.getString("CPF"));
        coletor.setEmail(rs.getString("EMAIL"));
        coletor.setTelefone(rs.getString("TELEFONE"));
        coletor.setAtivo(rs.getBoolean("ATIVO"));
        
        Timestamp dataCriacao = rs.getTimestamp("DATA_CRIACAO");
        if (dataCriacao != null) {
            coletor.setDataCriacao(dataCriacao.toLocalDateTime());
        }
        
        Timestamp dataAtualizacao = rs.getTimestamp("DATA_ATUALIZACAO");
        if (dataAtualizacao != null) {
            coletor.setDataAtualizacao(dataAtualizacao.toLocalDateTime());
        }
        
        return coletor;
    }
}