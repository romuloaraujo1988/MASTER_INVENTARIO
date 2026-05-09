package com.inventario.sihcp.siads.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.inventario.sihcp.dao.PatrimonioDAO;
import com.inventario.sihcp.model.Patrimonio;
import com.inventario.sihcp.util.ConnectionManager;

/**
 * Extensão do PatrimonioDAORefactored com métodos específicos para SIADS
 * Fornece consultas otimizadas para exportação de dados patrimoniais
 */
public class SiadsPatrimonioDAO extends PatrimonioDAO {
    
    /**
     * Lista todos os patrimônios com informações completas para SIADS
     * Inclui joins com responsável e sala para evitar N+1 queries
     * 
     * @return Lista de patrimônios com relacionamentos carregados
     * @throws SQLException Se houver erro na consulta
     */
    public List<Patrimonio> listarTodosParaSiads() throws SQLException {
        String sql = "SELECT p.*, " +
                    "r.NOME as nome_responsavel, r.CPF as cpf_responsavel, " +
                    "s.DESCRICAO as nome_sala, " +
                    "st.NOME as nome_setor " +
                    "FROM TABELA_PATRIMONIO p " +
                    "LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID " +
                    "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA " +
                    "LEFT JOIN TABELA_SETOR st ON s.ID_SETOR = st.ID " +
                    "WHERE UPPER(p.STATUS) = 'ATIVO' " +
                    "ORDER BY p.NUMERO";
        
        return executeQueryWithExtendedMapping(sql);
    }
    
    /**
     * Lista patrimônios de um inventário específico com informações completas
     * 
     * @param idInventario ID do inventário
     * @return Lista de patrimônios do inventário
     * @throws SQLException Se houver erro na consulta
     */
    public List<Patrimonio> listarPorInventario(Long idInventario) throws SQLException {
        String sql = "SELECT DISTINCT p.*, " +
                    "r.NOME as nome_responsavel, r.CPF as cpf_responsavel, " +
                    "s.DESCRICAO as nome_sala, " +
                    "st.NOME as nome_setor, " +
                    "c.DATA_COLETA as data_inventario " +
                    "FROM TABELA_PATRIMONIO p " +
                    "INNER JOIN TABELA_COLETA c ON c.ID_PATRIMONIO = p.ID " +
                    "LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID " +
                    "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA " +
                    "LEFT JOIN TABELA_SETOR st ON s.ID_SETOR = st.ID " +
                    "WHERE c.ID_INVENTARIO = ? " +
                    "ORDER BY p.NUMERO";
        
        return executeQueryWithExtendedMapping(sql, idInventario);
    }
    
    /**
     * Lista patrimônios por setor para SIADS
     * 
     * @param idSetor ID do setor
     * @return Lista de patrimônios do setor
     * @throws SQLException Se houver erro na consulta
     */
    public List<Patrimonio> listarPorSetor(int idSetor) throws SQLException {
        String sql = "SELECT p.*, " +
                    "r.NOME as nome_responsavel, r.CPF as cpf_responsavel, " +
                    "s.DESCRICAO as nome_sala, " +
                    "st.NOME as nome_setor " +
                    "FROM TABELA_PATRIMONIO p " +
                    "LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID " +
                    "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA " +
                    "LEFT JOIN TABELA_SETOR st ON s.ID_SETOR = st.ID " +
                    "WHERE st.ID = ? AND UPPER(p.STATUS) = 'ATIVO' " +
                    "ORDER BY p.NUMERO";
        
        return executeQueryWithExtendedMapping(sql, idSetor);
    }
    
    /**
     * Executa query com mapeamento estendido incluindo campos de relacionamentos
     * 
     * @param sql Query SQL a executar
     * @param params Parâmetros da query
     * @return Lista de patrimônios com campos estendidos
     * @throws SQLException Se houver erro
     */
    private List<Patrimonio> executeQueryWithExtendedMapping(String sql, Object... params) throws SQLException {
        List<Patrimonio> patrimonios = new ArrayList<>();
        Connection conn = null;
        
        try {
            conn = ConnectionManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            // Setar parâmetros
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Patrimonio p = mapResultSetToEntity(rs);
                
                // Mapear campos adicionais para SIADS
                mapExtendedFields(p, rs);
                
                patrimonios.add(p);
            }
            
            rs.close();
            stmt.close();
            
        } finally {
            ConnectionManager.closeConnection(conn);
        }
        
        return patrimonios;
    }
    
    /**
     * Mapeia campos estendidos do ResultSet para o Patrimonio
     * Inclui informações de responsável, sala e setor necessárias para SIADS
     * 
     * @param patrimonio Objeto Patrimonio a ser preenchido
     * @param rs ResultSet com os dados
     * @throws SQLException Se houver erro ao ler campos
     */
    private void mapExtendedFields(Patrimonio patrimonio, ResultSet rs) throws SQLException {
        try {
            // Campos do responsável
            patrimonio.setNomeResponsavel(rs.getString("nome_responsavel"));
            patrimonio.setCpfResponsavel(rs.getString("cpf_responsavel"));
            
            // Campos da sala
            patrimonio.setNomeSala(rs.getString("nome_sala"));
            
            // Campos do setor
            patrimonio.setNomeSetor(rs.getString("nome_setor"));
            
            // Data do inventário (se disponível)
            try {
                java.sql.Timestamp dataInventario = rs.getTimestamp("data_inventario");
                if (dataInventario != null) {
                    // Armazenar em dataCarga como referência
                    patrimonio.setDataCarga(dataInventario);
                }
            } catch (SQLException e) {
                // Campo opcional - ignorar se não existir
            }
            
        } catch (SQLException e) {
            // Campos opcionais - apenas logar se necessário
            System.err.println("Aviso: Alguns campos estendidos não puderam ser mapeados: " + e.getMessage());
        }
    }
    
    /**
     * Conta total de patrimônios ativos para SIADS
     * 
     * @return Quantidade de patrimônios ativos
     * @throws SQLException Se houver erro
     */
    public long contarPatrimoniosAtivosParaSiads() throws SQLException {
        String sql = "SELECT COUNT(*) FROM TABELA_PATRIMONIO WHERE UPPER(STATUS) = 'ATIVO'";
        Connection conn = null;
        
        try {
            conn = ConnectionManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                long count = rs.getLong(1);
                rs.close();
                stmt.close();
                return count;
            }
            
            rs.close();
            stmt.close();
            
        } finally {
            ConnectionManager.closeConnection(conn);
        }
        
        return 0;
    }
    
    /**
     * Conta patrimônios com responsável definido
     * 
     * @return Quantidade de patrimônios com responsável
     * @throws SQLException Se houver erro
     */
    public long contarPatrimoniosComResponsavel() throws SQLException {
        String sql = "SELECT COUNT(*) FROM TABELA_PATRIMONIO " +
                    "WHERE UPPER(STATUS) = 'ATIVO' AND ID_RESPONSAVEL IS NOT NULL AND ID_RESPONSAVEL > 0";
        Connection conn = null;
        
        try {
            conn = ConnectionManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                long count = rs.getLong(1);
                rs.close();
                stmt.close();
                return count;
            }
            
            rs.close();
            stmt.close();
            
        } finally {
            ConnectionManager.closeConnection(conn);
        }
        
        return 0;
    }
    
    /**
     * Conta patrimônios com localização definida
     * 
     * @return Quantidade de patrimônios com sala
     * @throws SQLException Se houver erro
     */
    public long contarPatrimoniosComLocalizacao() throws SQLException {
        String sql = "SELECT COUNT(*) FROM TABELA_PATRIMONIO " +
                    "WHERE UPPER(STATUS) = 'ATIVO' AND ID_SALA IS NOT NULL AND ID_SALA > 0";
        Connection conn = null;
        
        try {
            conn = ConnectionManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                long count = rs.getLong(1);
                rs.close();
                stmt.close();
                return count;
            }
            
            rs.close();
            stmt.close();
            
        } finally {
            ConnectionManager.closeConnection(conn);
        }
        
        return 0;
    }
}
