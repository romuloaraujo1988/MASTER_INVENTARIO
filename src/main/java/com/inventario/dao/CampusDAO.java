package com.inventario.dao;

import com.inventario.model.Campus;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) para gerenciar operações CRUD da tabela TABELA_CAMPUS
 * Sistema de Inventário IFMT
 */
@Repository
public class CampusDAO {
    
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
     * Insere um novo campus no banco de dados
     */
    public Integer inserirCampus(Campus campus) {
        String sql = "INSERT INTO TABELA_CAMPUS (NOME, LOCAL, CNPJ, DIRETOR, CURSOS, TELEFONE, EMAIL, OBSERVACOES, ATIVO) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, campus.getNome());
            stmt.setString(2, campus.getLocal());
            stmt.setString(3, campus.getCnpj());
            stmt.setString(4, campus.getDiretor());
            stmt.setString(5, campus.getCursos());
            stmt.setString(6, campus.getTelefone());
            stmt.setString(7, campus.getEmail());
            stmt.setString(8, campus.getObservacoes());
            stmt.setBoolean(9, campus.getAtivo() != null ? campus.getAtivo() : true);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Integer id = generatedKeys.getInt(1);
                        campus.setId(id);
                        return id;
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao inserir campus: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Atualiza um campus existente
     */
    public boolean atualizarCampus(Campus campus) {
        String sql = "UPDATE TABELA_CAMPUS SET NOME = ?, LOCAL = ?, CNPJ = ?, DIRETOR = ?, CURSOS = ?, " +
                    "TELEFONE = ?, EMAIL = ?, OBSERVACOES = ? WHERE ID = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, campus.getNome());
            stmt.setString(2, campus.getLocal());
            stmt.setString(3, campus.getCnpj());
            stmt.setString(4, campus.getDiretor());
            stmt.setString(5, campus.getCursos());
            stmt.setString(6, campus.getTelefone());
            stmt.setString(7, campus.getEmail());
            stmt.setString(8, campus.getObservacoes());
            stmt.setInt(9, campus.getId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar campus: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Exclui um campus (desativa)
     */
    public boolean excluirCampus(int idCampus) {
        String sql = "UPDATE TABELA_CAMPUS SET ATIVO = FALSE WHERE ID = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idCampus);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Erro ao excluir campus: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Busca um campus por ID
     */
    public Campus buscarCampusPorId(int idCampus) {
        String sql = "SELECT * FROM TABELA_CAMPUS WHERE ID = ? AND ATIVO = TRUE";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idCampus);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return criarCampusFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar campus: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Lista todos os campus ativos
     */
    public List<Campus> listarCampus() {
        List<Campus> campusList = new ArrayList<>();
        String sql = "SELECT * FROM TABELA_CAMPUS WHERE ATIVO = TRUE ORDER BY NOME";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                campusList.add(criarCampusFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao listar campus: " + e.getMessage());
        }
        
        return campusList;
    }
    
    /**
     * Busca campus por filtro
     */
    public List<Campus> buscarCampusPorFiltro(String filtro) {
        List<Campus> campusList = new ArrayList<>();
        String sql = "SELECT * FROM TABELA_CAMPUS WHERE ATIVO = TRUE AND " +
                    "(UPPER(NOME) LIKE UPPER(?) OR UPPER(LOCAL) LIKE UPPER(?) OR UPPER(DIRETOR) LIKE UPPER(?)) " +
                    "ORDER BY NOME";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String filtroLike = "%" + filtro + "%";
            stmt.setString(1, filtroLike);
            stmt.setString(2, filtroLike);
            stmt.setString(3, filtroLike);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                campusList.add(criarCampusFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar campus por filtro: " + e.getMessage());
        }
        
        return campusList;
    }
    
    /**
     * Busca campus por CNPJ
     */
    public Campus buscarCampusPorCnpj(String cnpj) {
        String sql = "SELECT * FROM TABELA_CAMPUS WHERE CNPJ = ? AND ATIVO = TRUE";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, cnpj);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return criarCampusFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar campus por CNPJ: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Busca campus por filtros específicos
     */
    public List<Campus> buscarCampusPorFiltro(String nome, String local, String diretor, String cnpj) {
        List<Campus> campusList = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM TABELA_CAMPUS WHERE ATIVO = TRUE ");
        
        List<Object> parametros = new ArrayList<>();
        
        if (nome != null && !nome.trim().isEmpty()) {
            sql.append("AND UPPER(NOME) LIKE UPPER(?) ");
            parametros.add("%" + nome + "%");
        }
        
        if (local != null && !local.trim().isEmpty()) {
            sql.append("AND UPPER(LOCAL) LIKE UPPER(?) ");
            parametros.add("%" + local + "%");
        }
        
        if (diretor != null && !diretor.trim().isEmpty()) {
            sql.append("AND UPPER(DIRETOR) LIKE UPPER(?) ");
            parametros.add("%" + diretor + "%");
        }
        
        if (cnpj != null && !cnpj.trim().isEmpty()) {
            sql.append("AND CNPJ = ? ");
            parametros.add(cnpj);
        }
        
        sql.append("ORDER BY NOME");
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                campusList.add(criarCampusFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar campus por filtros: " + e.getMessage());
        }
        
        return campusList;
    }
    
    /**
     * Cria objeto Campus a partir do ResultSet
     */
    private Campus criarCampusFromResultSet(ResultSet rs) throws SQLException {
        Campus campus = new Campus();
        campus.setId(rs.getInt("ID"));
        campus.setNome(rs.getString("NOME"));
        campus.setLocal(rs.getString("LOCAL"));
        campus.setCnpj(rs.getString("CNPJ"));
        campus.setDiretor(rs.getString("DIRETOR"));
        campus.setCursos(rs.getString("CURSOS"));
        campus.setTelefone(rs.getString("TELEFONE"));
        campus.setEmail(rs.getString("EMAIL"));
        campus.setObservacoes(rs.getString("OBSERVACOES"));
        campus.setAtivo(rs.getBoolean("ATIVO"));
        
        // Tentar ler DATA_CRIACAO se existir (para compatibilidade)
        try {
            campus.setDataCriacao(rs.getTimestamp("DATA_CRIACAO"));
        } catch (SQLException e) {
            // Coluna não existe, ignorar
            campus.setDataCriacao(null);
        }
        
        return campus;
    }
    
    /**
     * Busca campus por nome
     */
    public Campus buscarCampusPorNome(String nome) {
        String sql = "SELECT * FROM TABELA_CAMPUS WHERE UPPER(NOME) = UPPER(?) AND ATIVO = TRUE";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nome);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return criarCampusFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar campus por nome: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Verifica se um campus já existe com o mesmo nome
     */
    public boolean campusExistePorNome(String nome) {
        return buscarCampusPorNome(nome) != null;
    }
    
    /**
     * Verifica se um campus já existe com o mesmo CNPJ
     */
    public boolean campusExistePorCnpj(String cnpj) {
        return buscarCampusPorCnpj(cnpj) != null;
    }
    
    /**
     * Verifica se um campus já existe com o mesmo nome (excluindo um ID específico)
     */
    public boolean campusExiste(String nome, int idCampusExcluir) {
        String sql = "SELECT COUNT(*) FROM TABELA_CAMPUS WHERE UPPER(NOME) = UPPER(?) AND ATIVO = TRUE";
        
        if (idCampusExcluir > 0) {
            sql += " AND ID != ?";
        }
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nome);
            if (idCampusExcluir > 0) {
                stmt.setInt(2, idCampusExcluir);
            }
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao verificar existência do campus: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Verifica se um CNPJ já existe
     */
    public boolean cnpjExiste(String cnpj, int idCampusExcluir) {
        String sql = "SELECT COUNT(*) FROM TABELA_CAMPUS WHERE CNPJ = ? AND ATIVO = TRUE";
        
        if (idCampusExcluir > 0) {
            sql += " AND ID != ?";
        }
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, cnpj);
            if (idCampusExcluir > 0) {
                stmt.setInt(2, idCampusExcluir);
            }
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao verificar existência do CNPJ: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Conta quantos setores estão vinculados ao campus
     */
    public int contarSetoresDoCampus(int idCampus) {
        String sql = "SELECT COUNT(*) FROM TABELA_SETOR WHERE ID_CAMPUS = ? AND ATIVO = TRUE";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idCampus);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao contar setores do campus: " + e.getMessage());
        }
        
        return 0;
    }
}