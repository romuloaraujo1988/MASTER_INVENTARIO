package com.inventario.itemcomposto.dao;

import com.inventario.dao.BaseDAO;
import com.inventario.itemcomposto.model.ItemComposto;
import com.inventario.util.ConnectionManager;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gerenciar itens compostos no banco de dados.
 * 
 * @author Sistema de Inventário Patrimonial
 * @version 1.0
 * @since 27/11/2025
 */
@Repository
public class ItemCompostoDAO extends BaseDAO<ItemComposto, Integer> {
    
    private final ComponenteDAO componenteDAO;
    
    public ItemCompostoDAO() {
        this.componenteDAO = new ComponenteDAO();
    }
    
    // ==================== MÉTODOS ABSTRATOS IMPLEMENTADOS ====================
    
    @Override
    protected String getTableName() {
        return "TABELA_ITEM_COMPOSTO";
    }
    
    @Override
    protected String getInsertSQL() {
        return "INSERT INTO TABELA_ITEM_COMPOSTO (ID_PATRIMONIO, DETECCAO_AUTOMATICA, ID_USUARIO_CRIACAO, OBSERVACOES) " +
               "VALUES (?, ?, ?, ?)";
    }
    
    @Override
    protected String getUpdateSQL() {
        return "UPDATE TABELA_ITEM_COMPOSTO SET ID_PATRIMONIO = ?, DETECCAO_AUTOMATICA = ?, OBSERVACOES = ? WHERE ID = ?";
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement stmt, ItemComposto entity) throws SQLException {
        stmt.setInt(1, entity.getIdPatrimonio());
        stmt.setBoolean(2, entity.isDeteccaoAutomatica());
        if (entity.getIdUsuarioCriacao() != null) {
            stmt.setInt(3, entity.getIdUsuarioCriacao());
        } else {
            stmt.setNull(3, Types.INTEGER);
        }
        stmt.setString(4, entity.getObservacoes());
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement stmt, ItemComposto entity) throws SQLException {
        stmt.setInt(1, entity.getIdPatrimonio());
        stmt.setBoolean(2, entity.isDeteccaoAutomatica());
        stmt.setString(3, entity.getObservacoes());
        stmt.setInt(4, entity.getId());
    }
    
    @Override
    protected void setGeneratedId(ItemComposto entity, int id) {
        entity.setId(id);
    }
    
    @Override
    protected ItemComposto mapResultSetToEntity(ResultSet rs) throws SQLException {
        ItemComposto item = new ItemComposto();
        item.setId(rs.getInt("ID"));
        item.setIdPatrimonio(rs.getInt("ID_PATRIMONIO"));
        item.setDeteccaoAutomatica(rs.getBoolean("DETECCAO_AUTOMATICA"));
        item.setDataCriacao(rs.getTimestamp("DATA_CRIACAO"));
        
        int idUsuario = rs.getInt("ID_USUARIO_CRIACAO");
        if (!rs.wasNull()) {
            item.setIdUsuarioCriacao(idUsuario);
        }
        
        item.setObservacoes(rs.getString("OBSERVACOES"));
        return item;
    }
    
    // ==================== MÉTODOS ESPECÍFICOS ====================
    
    /**
     * Busca item composto por ID do patrimônio
     */
    public ItemComposto buscarPorPatrimonio(Integer idPatrimonio) throws SQLException {
        String sql = "SELECT * FROM TABELA_ITEM_COMPOSTO WHERE ID_PATRIMONIO = ?";
        Connection conn = null;
        
        try {
            conn = ConnectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idPatrimonio);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        ItemComposto item = mapResultSetToEntity(rs);
                        // Carregar componentes
                        item.setComponentes(componenteDAO.listarPorItemComposto(item.getId()));
                        return item;
                    }
                }
            }
        } finally {
            ConnectionManager.closeConnection(conn);
        }
        return null;
    }
    
    /**
     * Lista todos os itens compostos com informações completas
     */
    public List<ItemComposto> listarTodos() throws SQLException {
        String sql = "SELECT * FROM VIEW_ITEM_COMPOSTO_RESUMO ORDER BY NUMERO_PATRIMONIO";
        Connection conn = null;
        List<ItemComposto> lista = new ArrayList<>();
        
        try {
            conn = ConnectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    ItemComposto item = mapResultSetFromView(rs);
                    lista.add(item);
                }
            }
        } finally {
            ConnectionManager.closeConnection(conn);
        }
        return lista;
    }
    
    /**
     * Lista itens compostos por inventário
     */
    public List<ItemComposto> listarPorInventario(Integer idInventario) throws SQLException {
        String sql = "SELECT DISTINCT ic.* FROM TABELA_ITEM_COMPOSTO ic " +
                     "INNER JOIN TABELA_COMPONENTE c ON ic.ID = c.ID_ITEM_COMPOSTO " +
                     "LEFT JOIN TABELA_COMPONENTE_COLETA cc ON c.ID = cc.ID_COMPONENTE AND cc.ID_INVENTARIO = ? " +
                     "ORDER BY ic.ID";
        
        Connection conn = null;
        List<ItemComposto> lista = new ArrayList<>();
        
        try {
            conn = ConnectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idInventario);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        ItemComposto item = mapResultSetToEntity(rs);
                        item.setComponentes(componenteDAO.listarPorItemComposto(item.getId()));
                        lista.add(item);
                    }
                }
            }
        } finally {
            ConnectionManager.closeConnection(conn);
        }
        return lista;
    }
    
    /**
     * Conta total de itens compostos
     */
    public int contarTotal() throws SQLException {
        String sql = "SELECT COUNT(*) FROM TABELA_ITEM_COMPOSTO";
        Connection conn = null;
        
        try {
            conn = ConnectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } finally {
            ConnectionManager.closeConnection(conn);
        }
        return 0;
    }
    
    /**
     * Verifica se patrimônio já é item composto
     */
    public boolean patrimonioJaEhComposto(Integer idPatrimonio) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TABELA_ITEM_COMPOSTO WHERE ID_PATRIMONIO = ?";
        Connection conn = null;
        
        try {
            conn = ConnectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idPatrimonio);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } finally {
            ConnectionManager.closeConnection(conn);
        }
        return false;
    }
    
    // ==================== MÉTODOS AUXILIARES ====================
    
    /**
     * Mapeia ResultSet da view para ItemComposto
     */
    private ItemComposto mapResultSetFromView(ResultSet rs) throws SQLException {
        ItemComposto item = new ItemComposto();
        item.setId(rs.getInt("ID_ITEM_COMPOSTO"));
        item.setIdPatrimonio(rs.getInt("ID_PATRIMONIO"));
        item.setNumeroPatrimonio(rs.getString("NUMERO_PATRIMONIO"));
        item.setDescricaoPatrimonio(rs.getString("DESCRICAO_PATRIMONIO"));
        item.setDeteccaoAutomatica(rs.getBoolean("DETECCAO_AUTOMATICA"));
        item.setDataCriacao(rs.getTimestamp("DATA_CRIACAO"));
        
        // Dados da sala
        int idSala = rs.getInt("ID_SALA");
        if (!rs.wasNull()) {
            item.setIdSala(idSala);
            item.setNomeSala(rs.getString("NOME_SALA"));
            item.setNumeroSala(rs.getString("NUMERO_SALA"));
        }
        
        // Dados do setor
        int idSetor = rs.getInt("ID_SETOR");
        if (!rs.wasNull()) {
            item.setIdSetor(idSetor);
            item.setNomeSetor(rs.getString("NOME_SETOR"));
        }
        
        // Dados do responsável
        int idResp = rs.getInt("ID_RESPONSAVEL");
        if (!rs.wasNull()) {
            item.setIdResponsavel(idResp);
            item.setNomeResponsavel(rs.getString("NOME_RESPONSAVEL"));
        }
        
        item.setObservacoes(rs.getString("OBSERVACOES"));
        
        // Carregar componentes
        try {
            item.setComponentes(componenteDAO.listarPorItemComposto(item.getId()));
        } catch (SQLException e) {
            // Log error but don't fail
            System.err.println("Erro ao carregar componentes: " + e.getMessage());
        }
        
        return item;
    }
}
