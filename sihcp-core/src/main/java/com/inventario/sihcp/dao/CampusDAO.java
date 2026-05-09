package com.inventario.sihcp.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.inventario.sihcp.model.Campus;
import com.inventario.sihcp.util.DatabaseConnection;

/**
 * DAO (Data Access Object) para gerenciar operações CRUD da tabela TABELA_CAMPUS
 * Sistema de Inventário IFMT
 */
@Repository
public class CampusDAO {
    
    /**
     * Obtém conexão com o banco de dados usando configuração centralizada
     */
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }
    
    /**
     * Insere um novo campus no banco de dados
     */
    public Integer inserirCampus(Campus campus) {
        String sql = "INSERT INTO TABELA_CAMPUS (NOME, LOCAL, CNPJ, CODIGO_UORG, DIRETOR, CURSOS, TELEFONE, EMAIL, OBSERVACOES, ATIVO) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, campus.getNome());
            stmt.setString(2, campus.getLocal());
            stmt.setString(3, campus.getCnpj());
            stmt.setString(4, campus.getCodigoUorg());
            stmt.setString(5, campus.getDiretor());
            stmt.setString(6, campus.getCursos());
            stmt.setString(7, campus.getTelefone());
            stmt.setString(8, campus.getEmail());
            stmt.setString(9, campus.getObservacoes());
            stmt.setBoolean(10, campus.getAtivo() != null ? campus.getAtivo() : true);
            
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
        String sql = "UPDATE TABELA_CAMPUS SET NOME = ?, LOCAL = ?, CNPJ = ?, CODIGO_UORG = ?, DIRETOR = ?, CURSOS = ?, " +
                    "TELEFONE = ?, EMAIL = ?, OBSERVACOES = ? WHERE ID = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, campus.getNome());
            stmt.setString(2, campus.getLocal());
            stmt.setString(3, campus.getCnpj());
            stmt.setString(4, campus.getCodigoUorg());
            stmt.setString(5, campus.getDiretor());
            stmt.setString(6, campus.getCursos());
            stmt.setString(7, campus.getTelefone());
            stmt.setString(8, campus.getEmail());
            stmt.setString(9, campus.getObservacoes());
            stmt.setInt(10, campus.getId());
            
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
     * CORREÇÃO 13/01/2026: Usa OR entre os campos para busca mais flexível
     */
    public List<Campus> buscarCampusPorFiltro(String nome, String local, String diretor, String cnpj) {
        List<Campus> campusList = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM TABELA_CAMPUS WHERE ATIVO = TRUE ");
        
        List<Object> parametros = new ArrayList<>();
        List<String> condicoes = new ArrayList<>();
        
        // Se todos os filtros de texto são iguais, usar OR entre eles (busca geral)
        boolean buscaGeral = nome != null && local != null && diretor != null 
                && nome.equals(local) && local.equals(diretor) && !nome.trim().isEmpty();
        
        System.out.println("[DEBUG CampusDAO] buscarCampusPorFiltro - nome: " + nome + ", local: " + local + ", diretor: " + diretor);
        System.out.println("[DEBUG CampusDAO] buscaGeral: " + buscaGeral);
        
        if (buscaGeral) {
            // Busca geral: termo pode estar em qualquer campo
            sql.append("AND (UPPER(NOME) LIKE UPPER(?) OR UPPER(LOCAL) LIKE UPPER(?) OR UPPER(DIRETOR) LIKE UPPER(?)) ");
            parametros.add("%" + nome + "%");
            parametros.add("%" + local + "%");
            parametros.add("%" + diretor + "%");
        } else {
            // Busca específica: cada filtro é aplicado individualmente com OR
            if (nome != null && !nome.trim().isEmpty()) {
                condicoes.add("UPPER(NOME) LIKE UPPER(?)");
                parametros.add("%" + nome + "%");
            }
            
            if (local != null && !local.trim().isEmpty()) {
                condicoes.add("UPPER(LOCAL) LIKE UPPER(?)");
                parametros.add("%" + local + "%");
            }
            
            if (diretor != null && !diretor.trim().isEmpty()) {
                condicoes.add("UPPER(DIRETOR) LIKE UPPER(?)");
                parametros.add("%" + diretor + "%");
            }
            
            if (!condicoes.isEmpty()) {
                sql.append("AND (").append(String.join(" OR ", condicoes)).append(") ");
            }
        }
        
        if (cnpj != null && !cnpj.trim().isEmpty()) {
            sql.append("AND CNPJ = ? ");
            parametros.add(cnpj);
        }
        
        sql.append("ORDER BY NOME");
        
        System.out.println("[DEBUG CampusDAO] SQL: " + sql.toString());
        System.out.println("[DEBUG CampusDAO] Parametros: " + parametros);
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                campusList.add(criarCampusFromResultSet(rs));
            }
            
            System.out.println("[DEBUG CampusDAO] Resultados encontrados: " + campusList.size());
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar campus por filtros: " + e.getMessage());
            e.printStackTrace();
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
        campus.setCodigoUorg(rs.getString("CODIGO_UORG"));
        campus.setDiretor(rs.getString("DIRETOR"));
        campus.setCursos(rs.getString("CURSOS"));
        campus.setTelefone(rs.getString("TELEFONE"));
        campus.setEmail(rs.getString("EMAIL"));
        campus.setObservacoes(rs.getString("OBSERVACOES"));
        campus.setAtivo(rs.getBoolean("ATIVO"));
        
        // Tentar ler CREATED_AT se existir (para compatibilidade)
        try {
            campus.setDataCriacao(rs.getTimestamp("CREATED_AT"));
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