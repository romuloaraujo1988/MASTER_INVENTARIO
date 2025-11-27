package com.inventario.itemcomposto.dao;

import com.inventario.dao.BaseDAO;
import com.inventario.itemcomposto.model.Componente;
import com.inventario.util.ConnectionManager;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gerenciar componentes de itens compostos.
 * 
 * @author Sistema de Inventário Patrimonial
 * @version 1.0
 * @since 27/11/2025
 */
@Repository
public class ComponenteDAO extends BaseDAO<Componente, Integer> {
    
    @Override
    protected String getTableName() {
        return "TABELA_COMPONENTE";
    }
    
    @Override
    protected String getInsertSQL() {
        return "INSERT INTO TABELA_COMPONENTE (ID_ITEM_COMPOSTO, TIPO, DESCRICAO, QUANTIDADE_ESPERADA, ORDEM) " +
               "VALUES (?, ?, ?, ?, ?)";
    }
    
    @Override
    protected String getUpdateSQL() {
        return "UPDATE TABELA_COMPONENTE SET TIPO = ?, DESCRICAO = ?, QUANTIDADE_ESPERADA = ?, ORDEM = ? WHERE ID = ?";
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement stmt, Componente entity) throws SQLException {
        stmt.setInt(1, entity.getIdItemComposto());
        stmt.setString(2, entity.getTipo());
        stmt.setString(3, entity.getDescricao());
        stmt.setInt(4, entity.getQuantidadeEsperada());
        stmt.setInt(5, entity.getOrdem());
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement stmt, Componente entity) throws SQLException {
        stmt.setString(1, entity.getTipo());
        stmt.setString(2, entity.getDescricao());
        stmt.setInt(3, entity.getQuantidadeEsperada());
        stmt.setInt(4, entity.getOrdem());
        stmt.setInt(5, entity.getId());
    }
    
    @Override
    protected void setGeneratedId(Componente entity, int id) {
        entity.setId(id);
    }
    
    @Override
    protected Componente mapResultSetToEntity(ResultSet rs) throws SQLException {
        Componente comp = new Componente();
        comp.setId(rs.getInt("ID"));
        comp.setIdItemComposto(rs.getInt("ID_ITEM_COMPOSTO"));
        comp.setTipo(rs.getString("TIPO"));
        comp.setDescricao(rs.getString("DESCRICAO"));
        comp.setQuantidadeEsperada(rs.getInt("QUANTIDADE_ESPERADA"));
        comp.setOrdem(rs.getInt("ORDEM"));
        return comp;
    }
    
    /**
     * Lista componentes por item composto
     */
    public List<Componente> listarPorItemComposto(Integer idItemComposto) throws SQLException {
        String sql = "SELECT * FROM TABELA_COMPONENTE WHERE ID_ITEM_COMPOSTO = ? ORDER BY ORDEM, ID";
        Connection conn = null;
        List<Componente> lista = new ArrayList<>();
        
        try {
            conn = ConnectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idItemComposto);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapResultSetToEntity(rs));
                    }
                }
            }
        } finally {
            ConnectionManager.closeConnection(conn);
        }
        return lista;
    }
    
    /**
     * Lista tipos de componentes distintos
     */
    public List<String> listarTiposComponentes() throws SQLException {
        String sql = "SELECT DISTINCT TIPO FROM TABELA_COMPONENTE ORDER BY TIPO";
        Connection conn = null;
        List<String> tipos = new ArrayList<>();
        
        try {
            conn = ConnectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tipos.add(rs.getString("TIPO"));
                }
            }
        } finally {
            ConnectionManager.closeConnection(conn);
        }
        return tipos;
    }
    
    /**
     * Exclui todos os componentes de um item composto
     */
    public void excluirPorItemComposto(Integer idItemComposto) throws SQLException {
        String sql = "DELETE FROM TABELA_COMPONENTE WHERE ID_ITEM_COMPOSTO = ?";
        Connection conn = null;
        
        try {
            conn = ConnectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idItemComposto);
                stmt.executeUpdate();
            }
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }
}
