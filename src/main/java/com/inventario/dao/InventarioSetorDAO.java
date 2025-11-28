package com.inventario.dao;

import com.inventario.model.InventarioSetor;
import com.inventario.model.Setor;
import com.inventario.util.DatabaseConnection;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gerenciar operações da tabela TABELA_INVENTARIO_SETOR
 * 
 * Esta classe é responsável por:
 * - Salvar/atualizar relacionamentos entre inventários e setores
 * - Consultar setores de um inventário
 * - Verificar se um setor está no escopo de um inventário
 * - Gerenciar configurações de "incluir todos os setores"
 * 
 * @author Sistema de Inventário IFMT
 * @version 1.0
 */
@Repository
public class InventarioSetorDAO {
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(InventarioSetorDAO.class);
    
    public InventarioSetorDAO() {
        // Conexão será obtida por operação para evitar connection leak
    }
    
    /**
     * Obtém uma conexão do pool
     */
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }
    
    /**
     * Salva um novo relacionamento inventário-setor
     */
    public boolean salvar(InventarioSetor inventarioSetor) {
        if (!inventarioSetor.isValido()) {
            throw new IllegalArgumentException("InventarioSetor inválido: " + inventarioSetor);
        }
        
        String sql = "INSERT INTO TABELA_INVENTARIO_SETOR " +
                    "(ID_INVENTARIO, ID_SETOR, INCLUIR_TODOS_SETORES, DATA_INCLUSAO, ATIVO, OBSERVACOES) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, inventarioSetor.getIdInventario());
            
            if (inventarioSetor.getIdSetor() != null) {
                stmt.setInt(2, inventarioSetor.getIdSetor());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            
            stmt.setBoolean(3, inventarioSetor.getIncluirTodosSetores());
            stmt.setTimestamp(4, Timestamp.valueOf(inventarioSetor.getDataInclusao()));
            stmt.setBoolean(5, inventarioSetor.getAtivo());
            stmt.setString(6, inventarioSetor.getObservacoes());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        inventarioSetor.setId(rs.getInt(1));
                    }
                }
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao salvar InventarioSetor: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Atualiza um relacionamento inventário-setor existente
     */
    public boolean atualizar(InventarioSetor inventarioSetor) {
        if (inventarioSetor.getId() == null || !inventarioSetor.isValido()) {
            throw new IllegalArgumentException("InventarioSetor inválido para atualização: " + inventarioSetor);
        }
        
        String sql = "UPDATE TABELA_INVENTARIO_SETOR SET " +
                    "ID_INVENTARIO = ?, ID_SETOR = ?, INCLUIR_TODOS_SETORES = ?, " +
                    "ATIVO = ?, OBSERVACOES = ? " +
                    "WHERE ID = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, inventarioSetor.getIdInventario());
            
            if (inventarioSetor.getIdSetor() != null) {
                stmt.setInt(2, inventarioSetor.getIdSetor());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            
            stmt.setBoolean(3, inventarioSetor.getIncluirTodosSetores());
            stmt.setBoolean(4, inventarioSetor.getAtivo());
            stmt.setString(5, inventarioSetor.getObservacoes());
            stmt.setInt(6, inventarioSetor.getId());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar InventarioSetor: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Remove todos os relacionamentos de um inventário
     */
    public boolean removerPorInventario(Integer idInventario) {
        String sql = "DELETE FROM TABELA_INVENTARIO_SETOR WHERE ID_INVENTARIO = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idInventario);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erro ao remover InventarioSetor por inventário: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Desativa todos os relacionamentos de um inventário
     */
    public boolean desativarPorInventario(Integer idInventario) {
        String sql = "UPDATE TABELA_INVENTARIO_SETOR SET ATIVO = false WHERE ID_INVENTARIO = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idInventario);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erro ao desativar InventarioSetor por inventário: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Busca todos os relacionamentos de um inventário
     */
    public List<InventarioSetor> buscarPorInventario(Integer idInventario) {
        List<InventarioSetor> lista = new ArrayList<>();
        
        String sql = "SELECT is.*, i.NOME as nome_inventario, s.NOME as nome_setor " +
                    "FROM TABELA_INVENTARIO_SETOR is " +
                    "LEFT JOIN TABELA_INVENTARIO i ON is.ID_INVENTARIO = i.ID " +
                    "LEFT JOIN TABELA_SETOR s ON is.ID_SETOR = s.ID " +
                    "WHERE is.ID_INVENTARIO = ? AND is.ATIVO = true " +
                    "ORDER BY is.INCLUIR_TODOS_SETORES DESC, s.NOME";
        
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idInventario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    InventarioSetor inventarioSetor = criarObjetoDoResultSet(rs);
                    lista.add(inventarioSetor);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar InventarioSetor por inventário: " + e.getMessage());
            e.printStackTrace();
        }
        
        return lista;
    }
    
    /**
     * Verifica se um inventário inclui todos os setores
     */
    public boolean inventarioIncluiTodosSetores(Integer idInventario) {
        String sql = "SELECT COUNT(*) FROM TABELA_INVENTARIO_SETOR " +
                    "WHERE ID_INVENTARIO = ? AND INCLUIR_TODOS_SETORES = true AND ATIVO = true";
        
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idInventario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao verificar se inventário inclui todos os setores: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Verifica se um setor específico está no escopo de um inventário
     */
    public boolean setorNoEscopoInventario(Integer idInventario, Integer idSetor) {
        // Primeiro verifica se o inventário inclui todos os setores
        if (inventarioIncluiTodosSetores(idInventario)) {
            return true;
        }
        
        // Senão, verifica se o setor específico está incluído
        String sql = "SELECT COUNT(*) FROM TABELA_INVENTARIO_SETOR " +
                    "WHERE ID_INVENTARIO = ? AND ID_SETOR = ? AND ATIVO = true";
        
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idInventario);
            stmt.setInt(2, idSetor);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao verificar setor no escopo do inventário: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Busca todos os setores no escopo de um inventário
     */
    public List<Setor> buscarSetoresDoInventario(Integer idInventario) {
        List<Setor> setores = new ArrayList<>();
        
        // Se inclui todos os setores, busca todos os setores ativos
        if (inventarioIncluiTodosSetores(idInventario)) {
            SetorDAO setorDAO = new SetorDAO();
            try {
                return setorDAO.findAll("NOME");
            } catch (SQLException e) {
                throw new RuntimeException("Erro ao listar setores", e);
            }
        }
        
        // Senão, busca apenas os setores específicos
        String sql = "SELECT s.* FROM TABELA_SETOR s " +
                    "INNER JOIN TABELA_INVENTARIO_SETOR is ON s.ID = is.ID_SETOR " +
                    "WHERE is.ID_INVENTARIO = ? AND is.ATIVO = true " +
                    "ORDER BY s.NOME";
        
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idInventario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Setor setor = new Setor();
                    setor.setId(rs.getInt("ID"));
                    setor.setNome(rs.getString("NOME"));
                    setor.setDescricao(rs.getString("DESCRICAO"));
                    setores.add(setor);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar setores do inventário: " + e.getMessage());
            e.printStackTrace();
        }
        
        return setores;
    }
    
    /**
     * Salva a configuração de setores de um inventário
     * Remove configurações anteriores e salva as novas
     */
    public boolean salvarConfiguracaoSetores(Integer idInventario, boolean incluirTodos, List<Integer> idsSetores) {
        // Usa transação simples - cada operação já gerencia sua própria conexão
        try {
            // Remove configurações anteriores
            removerPorInventario(idInventario);
            
            if (incluirTodos) {
                // Salva configuração "incluir todos os setores"
                InventarioSetor inventarioSetor = new InventarioSetor(idInventario, true);
                return salvar(inventarioSetor);
            } else if (idsSetores != null && !idsSetores.isEmpty()) {
                // Salva setores específicos
                for (Integer idSetor : idsSetores) {
                    InventarioSetor inventarioSetor = new InventarioSetor(idInventario, idSetor);
                    if (!salvar(inventarioSetor)) {
                        return false;
                    }
                }
                return true;
            }
            return true;
            
        } catch (Exception e) {
            logger.error("Erro ao salvar configuração de setores: {}", e.getMessage(), e);
        }
        
        return false;
    }
    
    /**
     * Busca por ID
     */
    public InventarioSetor buscarPorId(Integer id) {
        String sql = "SELECT is.*, i.NOME as nome_inventario, s.NOME as nome_setor " +
                    "FROM TABELA_INVENTARIO_SETOR is " +
                    "LEFT JOIN TABELA_INVENTARIO i ON is.ID_INVENTARIO = i.ID " +
                    "LEFT JOIN TABELA_SETOR s ON is.ID_SETOR = s.ID " +
                    "WHERE is.ID = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return criarObjetoDoResultSet(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar InventarioSetor por ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Lista todos os relacionamentos ativos
     */
    public List<InventarioSetor> listarTodos() {
        List<InventarioSetor> lista = new ArrayList<>();
        
        String sql = "SELECT is.*, i.NOME as nome_inventario, s.NOME as nome_setor " +
                    "FROM TABELA_INVENTARIO_SETOR is " +
                    "LEFT JOIN TABELA_INVENTARIO i ON is.ID_INVENTARIO = i.ID " +
                    "LEFT JOIN TABELA_SETOR s ON is.ID_SETOR = s.ID " +
                    "WHERE is.ATIVO = true " +
                    "ORDER BY i.NOME, is.INCLUIR_TODOS_SETORES DESC, s.NOME";
        
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                InventarioSetor inventarioSetor = criarObjetoDoResultSet(rs);
                lista.add(inventarioSetor);
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao listar InventarioSetor: " + e.getMessage());
            e.printStackTrace();
        }
        
        return lista;
    }
    
    /**
     * Cria objeto InventarioSetor a partir do ResultSet
     */
    private InventarioSetor criarObjetoDoResultSet(ResultSet rs) throws SQLException {
        InventarioSetor inventarioSetor = new InventarioSetor();
        
        inventarioSetor.setId(rs.getInt("ID"));
        inventarioSetor.setIdInventario(rs.getInt("ID_INVENTARIO"));
        
        int idSetor = rs.getInt("ID_SETOR");
        if (!rs.wasNull()) {
            inventarioSetor.setIdSetor(idSetor);
        }
        
        inventarioSetor.setIncluirTodosSetores(rs.getBoolean("INCLUIR_TODOS_SETORES"));
        
        Timestamp dataInclusao = rs.getTimestamp("DATA_INCLUSAO");
        if (dataInclusao != null) {
            inventarioSetor.setDataInclusao(dataInclusao.toLocalDateTime());
        }
        
        inventarioSetor.setAtivo(rs.getBoolean("ATIVO"));
        inventarioSetor.setObservacoes(rs.getString("OBSERVACOES"));
        
        // Campos auxiliares
        inventarioSetor.setNomeInventario(rs.getString("nome_inventario"));
        inventarioSetor.setNomeSetor(rs.getString("nome_setor"));
        
        return inventarioSetor;
    }
    
    /**
     * Conta quantos setores estão no escopo de um inventário
     */
    public int contarSetoresDoInventario(Integer idInventario) {
        if (inventarioIncluiTodosSetores(idInventario)) {
            // Se inclui todos, conta todos os setores ativos
            String sql = "SELECT COUNT(*) FROM TABELA_SETOR WHERE ATIVO = true";
            try (Connection connection = getConnection();
                 PreparedStatement stmt = connection.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException e) {
                logger.error("Erro ao contar setores: {}", e.getMessage());
            }
        } else {
            // Senão, conta apenas os setores específicos
            String sql = "SELECT COUNT(*) FROM TABELA_INVENTARIO_SETOR " +
                        "WHERE ID_INVENTARIO = ? AND ID_SETOR IS NOT NULL AND ATIVO = true";
            try (Connection connection = getConnection();
                 PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, idInventario);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            } catch (SQLException e) {
                logger.error("Erro ao contar setores específicos: {}", e.getMessage());
            }
        }
        return 0;
    }
}