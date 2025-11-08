package com.inventario.dao;

import com.inventario.model.Inventario;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gerenciar operações CRUD da tabela TABELA_INVENTARIO
 */
@Repository
public class InventarioDAO {
    
    // Configurações do banco de dados
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/sispatrimonio";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "Romulo@2020";
    
    /**
     * Obtém conexão com o banco de dados
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    
    /**
     * Insere um novo inventário
     */
    public Integer inserir(Inventario inventario) {
        String sql = "INSERT INTO TABELA_INVENTARIO (NOME, DATA_INICIO, DATA_FIM, STATUS_INVENTARIO, RESPONSAVEL_INVENTARIO, PERCENTUAL_CONCLUSAO) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, inventario.getNome());
            stmt.setDate(2, new java.sql.Date(inventario.getDataInicio().getTime()));
            stmt.setDate(3, new java.sql.Date(inventario.getDataFim().getTime()));
            stmt.setString(4, inventario.getStatusInventario());
            stmt.setString(5, inventario.getResponsavelInventario());
            stmt.setBigDecimal(6, inventario.getPercentualConclusao());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Integer id = generatedKeys.getInt(1);
                        inventario.setId(id);
                        return id;
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao inserir inventário: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Atualiza um inventário existente
     */
    public boolean atualizar(Inventario inventario) {
        String sql = "UPDATE TABELA_INVENTARIO SET NOME = ?, DATA_INICIO = ?, DATA_FIM = ?, STATUS_INVENTARIO = ?, RESPONSAVEL_INVENTARIO = ?, PERCENTUAL_CONCLUSAO = ? WHERE ID = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, inventario.getNome());
            stmt.setDate(2, new java.sql.Date(inventario.getDataInicio().getTime()));
            stmt.setDate(3, new java.sql.Date(inventario.getDataFim().getTime()));
            stmt.setString(4, inventario.getStatusInventario());
            stmt.setString(5, inventario.getResponsavelInventario());
            stmt.setBigDecimal(6, inventario.getPercentualConclusao());
            stmt.setInt(7, inventario.getId());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar inventário: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Lista todos os inventários
     */
    public List<Inventario> listarInventarios() {
        List<Inventario> inventarios = new ArrayList<>();
        String sql = "SELECT * FROM TABELA_INVENTARIO ORDER BY NOME";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                inventarios.add(criarInventarioFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao listar inventários: " + e.getMessage());
        }
        return inventarios;
    }
    
    /**
     * Busca inventário por ID
     */
    public Inventario buscarInventarioPorId(int id) {
        String sql = "SELECT * FROM TABELA_INVENTARIO WHERE ID = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return criarInventarioFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar inventário: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Busca inventários por filtro
     */
    public List<Inventario> buscarInventariosPorFiltro(String filtro) {
        List<Inventario> inventarios = new ArrayList<>();
        String sql = "SELECT * FROM TABELA_INVENTARIO WHERE UPPER(NOME) LIKE UPPER(?) OR UPPER(STATUS_INVENTARIO) LIKE UPPER(?) ORDER BY NOME";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String filtroLike = "%" + filtro + "%";
            stmt.setString(1, filtroLike);
            stmt.setString(2, filtroLike);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                inventarios.add(criarInventarioFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar inventários por filtro: " + e.getMessage());
        }
        return inventarios;
    }
    
    /**
     * Busca inventário por status
     */
    public Inventario buscarInventarioPorStatus(String status) {
        String sql = "SELECT * FROM TABELA_INVENTARIO WHERE STATUS_INVENTARIO = ? ORDER BY DATA_CRIACAO DESC LIMIT 1";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return criarInventarioFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar inventário por status: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Finaliza um inventário (altera status para CONCLUIDO)
     */
    public boolean finalizar(int id) {
        String sql = "UPDATE TABELA_INVENTARIO SET STATUS_INVENTARIO = ?, PERCENTUAL_CONCLUSAO = ? WHERE ID = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, Inventario.STATUS_CONCLUIDO);
            stmt.setBigDecimal(2, new java.math.BigDecimal("100.00"));
            stmt.setInt(3, id);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erro ao finalizar inventário: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Exclui um inventário
     */
    public boolean excluir(Integer id) {
        String sql = "DELETE FROM TABELA_INVENTARIO WHERE ID = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erro ao excluir inventário: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Cria objeto Inventario a partir de ResultSet
     */
    private Inventario criarInventarioFromResultSet(ResultSet rs) throws SQLException {
        Inventario inventario = new Inventario();
        inventario.setId(rs.getInt("ID"));
        inventario.setNome(rs.getString("NOME"));
        inventario.setDataInicio(rs.getDate("DATA_INICIO"));
        inventario.setDataFim(rs.getDate("DATA_FIM"));
        inventario.setStatusInventario(rs.getString("STATUS_INVENTARIO"));
        inventario.setResponsavelInventario(rs.getString("RESPONSAVEL_INVENTARIO"));
        inventario.setPercentualConclusao(rs.getBigDecimal("PERCENTUAL_CONCLUSAO"));
        return inventario;
    }
}