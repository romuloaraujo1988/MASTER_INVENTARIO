package com.inventario.sihcp.dao;

import com.inventario.sihcp.dto.FiltroHistoricoDTO;
import com.inventario.sihcp.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO para operações de histórico de coletas de patrimônio.
 * Fornece métodos otimizados para buscar e analisar histórico de coletas.
 */
public class HistoricoColetaDAO {
    
    /**
     * Busca todas as coletas de um patrimônio ordenadas por data (mais recente primeiro).
     * 
     * @param patrimonioId ID do patrimônio
     * @param offset Offset para paginação
     * @param limit Limite de registros
     * @return Lista de mapas com dados das coletas
     * @throws SQLException
     */
    public List<Map<String, Object>> buscarColetasPorPatrimonio(
            Integer patrimonioId,
            int offset,
            int limit) throws SQLException {
        
        String sql = "SELECT " +
                    "c.ID, " +
                    "c.ID_PATRIMONIO, " +
                    "c.ID_INVENTARIO, " +
                    "c.ID_COLETOR, " +
                    "c.ID_PARTICIPANTE_INVENTARIO, " +
                    "c.DATA_COLETA, " +
                    "c.LOCALIZACAO_ENCONTRADA, " +
                    "c.ESTADO_ENCONTRADO, " +
                    "c.OBSERVACAO_COLETA, " +
                    "p.NUMERO as NUMERO_PATRIMONIO, " +
                    "p.DESCRICAO as DESCRICAO_PATRIMONIO, " +
                    "i.NOME as NOME_INVENTARIO, " +
                    "u.NOME_COMPLETO as NOME_COLETOR_COMPLETO, " +
                    "s.ID as SALA_ID, " +
                    "COALESCE(s.NUMERO_SALA, s.DESCRICAO) as NOME_SALA, " +
                    "st.ID as SETOR_ID, " +
                    "st.NOME as NOME_SETOR " +
                    "FROM TABELA_COLETA c " +
                    "INNER JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID " +
                    "INNER JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID " +
                    "LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.ID_PARTICIPANTE " +
                    "LEFT JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID " +
                    "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA " +
                    "LEFT JOIN TABELA_SETOR st ON s.ID_SETOR = st.ID " +
                    "WHERE c.ID_PATRIMONIO = ? " +
                    "ORDER BY c.DATA_COLETA DESC " +
                    "OFFSET ? LIMIT ?";
        
        List<Map<String, Object>> coletas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, patrimonioId);
            stmt.setInt(2, offset);
            stmt.setInt(3, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    coletas.add(mapResultSetToMap(rs));
                }
            }
        }
        
        return coletas;
    }
    
    /**
     * Busca coletas de um patrimônio com filtros aplicados.
     * 
     * @param patrimonioId ID do patrimônio
     * @param inventarioId ID do inventário (opcional)
     * @param coletorId ID do coletor (opcional)
     * @param dataInicio Data início do período (opcional)
     * @param dataFim Data fim do período (opcional)
     * @param offset Offset para paginação
     * @param limit Limite de registros
     * @return Lista de mapas com dados das coletas
     * @throws SQLException
     */
    public List<Map<String, Object>> buscarColetasComFiltros(
            Integer patrimonioId,
            Integer inventarioId,
            Integer coletorId,
            Date dataInicio,
            Date dataFim,
            int offset,
            int limit) throws SQLException {
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("c.ID, ");
        sql.append("c.ID_PATRIMONIO, ");
        sql.append("c.ID_INVENTARIO, ");
        sql.append("c.ID_COLETOR, ");
        sql.append("c.ID_PARTICIPANTE_INVENTARIO, ");
        sql.append("c.DATA_COLETA, ");
        sql.append("c.LOCALIZACAO_ENCONTRADA, ");
        sql.append("c.ESTADO_ENCONTRADO, ");
        sql.append("c.OBSERVACAO_COLETA, ");
        sql.append("p.NUMERO as NUMERO_PATRIMONIO, ");
        sql.append("p.DESCRICAO as DESCRICAO_PATRIMONIO, ");
        sql.append("i.NOME as NOME_INVENTARIO, ");
        sql.append("u.NOME_COMPLETO as NOME_COLETOR_COMPLETO, ");
        sql.append("s.ID as SALA_ID, ");
        sql.append("COALESCE(s.NUMERO_SALA, s.DESCRICAO) as NOME_SALA, ");
        sql.append("st.ID as SETOR_ID, ");
        sql.append("st.NOME as NOME_SETOR ");
        sql.append("FROM TABELA_COLETA c ");
        sql.append("INNER JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID ");
        sql.append("INNER JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID ");
        sql.append("LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.ID_PARTICIPANTE ");
        sql.append("LEFT JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID ");
        sql.append("LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA ");
        sql.append("LEFT JOIN TABELA_SETOR st ON s.ID_SETOR = st.ID ");
        sql.append("WHERE c.ID_PATRIMONIO = ? ");
        
        // Adicionar filtros opcionais
        List<Object> params = new ArrayList<>();
        params.add(patrimonioId);
        
        if (inventarioId != null) {
            sql.append("AND c.ID_INVENTARIO = ? ");
            params.add(inventarioId);
        }
        
        if (coletorId != null) {
            sql.append("AND pi.ID_USUARIO = ? ");
            params.add(coletorId);
        }
        
        if (dataInicio != null) {
            sql.append("AND c.DATA_COLETA >= ? ");
            params.add(new Timestamp(dataInicio.getTime()));
        }
        
        if (dataFim != null) {
            sql.append("AND c.DATA_COLETA <= ? ");
            params.add(new Timestamp(dataFim.getTime()));
        }
        
        sql.append("ORDER BY c.DATA_COLETA DESC ");
        sql.append("OFFSET ? LIMIT ?");
        params.add(offset);
        params.add(limit);
        
        List<Map<String, Object>> coletas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            // Setar parâmetros
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    coletas.add(mapResultSetToMap(rs));
                }
            }
        }
        
        return coletas;
    }
    
    /**
     * Conta o total de coletas de um patrimônio com filtros aplicados.
     * 
     * @param patrimonioId ID do patrimônio
     * @param filtros Filtros a aplicar
     * @return Total de coletas
     * @throws SQLException
     */
    public int contarColetas(Integer patrimonioId, FiltroHistoricoDTO filtros) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) ");
        sql.append("FROM TABELA_COLETA c ");
        sql.append("LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.ID_PARTICIPANTE ");
        sql.append("WHERE c.ID_PATRIMONIO = ? ");
        
        List<Object> params = new ArrayList<>();
        params.add(patrimonioId);
        
        if (filtros != null) {
            if (filtros.getInventarioId() != null) {
                sql.append("AND c.ID_INVENTARIO = ? ");
                params.add(filtros.getInventarioId());
            }
            
            if (filtros.getColetorId() != null) {
                sql.append("AND pi.ID_USUARIO = ? ");
                params.add(filtros.getColetorId());
            }
            
            if (filtros.getDataInicio() != null) {
                sql.append("AND c.DATA_COLETA >= ? ");
                params.add(new Timestamp(filtros.getDataInicio().getTime()));
            }
            
            if (filtros.getDataFim() != null) {
                sql.append("AND c.DATA_COLETA <= ? ");
                params.add(new Timestamp(filtros.getDataFim().getTime()));
            }
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Busca a coleta imediatamente anterior a uma data de referência.
     * Útil para comparação de mudanças entre coletas consecutivas.
     * 
     * @param patrimonioId ID do patrimônio
     * @param dataReferencia Data de referência
     * @return Mapa com dados da coleta anterior, ou null se não houver
     * @throws SQLException
     */
    public Map<String, Object> buscarColetaAnterior(
            Integer patrimonioId,
            Date dataReferencia) throws SQLException {
        
        String sql = "SELECT " +
                    "c.ID, " +
                    "c.ID_PATRIMONIO, " +
                    "c.ID_INVENTARIO, " +
                    "c.ID_COLETOR, " +
                    "c.ID_PARTICIPANTE_INVENTARIO, " +
                    "c.DATA_COLETA, " +
                    "c.LOCALIZACAO_ENCONTRADA, " +
                    "c.ESTADO_ENCONTRADO, " +
                    "c.OBSERVACAO_COLETA, " +
                    "p.NUMERO as NUMERO_PATRIMONIO, " +
                    "p.DESCRICAO as DESCRICAO_PATRIMONIO, " +
                    "i.NOME as NOME_INVENTARIO, " +
                    "u.NOME_COMPLETO as NOME_COLETOR_COMPLETO, " +
                    "s.ID as SALA_ID, " +
                    "COALESCE(s.NUMERO_SALA, s.DESCRICAO) as NOME_SALA, " +
                    "st.ID as SETOR_ID, " +
                    "st.NOME as NOME_SETOR " +
                    "FROM TABELA_COLETA c " +
                    "INNER JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID " +
                    "INNER JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID " +
                    "LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.ID_PARTICIPANTE " +
                    "LEFT JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID " +
                    "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA " +
                    "LEFT JOIN TABELA_SETOR st ON s.ID_SETOR = st.ID " +
                    "WHERE c.ID_PATRIMONIO = ? " +
                    "AND c.DATA_COLETA < ? " +
                    "ORDER BY c.DATA_COLETA DESC " +
                    "LIMIT 1";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, patrimonioId);
            stmt.setTimestamp(2, new Timestamp(dataReferencia.getTime()));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMap(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Busca uma coleta específica por ID.
     * 
     * @param coletaId ID da coleta
     * @return Mapa com dados da coleta, ou null se não encontrada
     * @throws SQLException
     */
    public Map<String, Object> buscarColetaPorId(Integer coletaId) throws SQLException {
        String sql = "SELECT " +
                    "c.ID, " +
                    "c.ID_PATRIMONIO, " +
                    "c.ID_INVENTARIO, " +
                    "c.ID_COLETOR, " +
                    "c.ID_PARTICIPANTE_INVENTARIO, " +
                    "c.DATA_COLETA, " +
                    "c.LOCALIZACAO_ENCONTRADA, " +
                    "c.ESTADO_ENCONTRADO, " +
                    "c.OBSERVACAO_COLETA, " +
                    "p.NUMERO as NUMERO_PATRIMONIO, " +
                    "p.DESCRICAO as DESCRICAO_PATRIMONIO, " +
                    "i.NOME as NOME_INVENTARIO, " +
                    "u.NOME_COMPLETO as NOME_COLETOR_COMPLETO, " +
                    "s.ID as SALA_ID, " +
                    "COALESCE(s.NUMERO_SALA, s.DESCRICAO) as NOME_SALA, " +
                    "st.ID as SETOR_ID, " +
                    "st.NOME as NOME_SETOR " +
                    "FROM TABELA_COLETA c " +
                    "INNER JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID " +
                    "INNER JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID " +
                    "LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.ID_PARTICIPANTE " +
                    "LEFT JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID " +
                    "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA " +
                    "LEFT JOIN TABELA_SETOR st ON s.ID_SETOR = st.ID " +
                    "WHERE c.ID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, coletaId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMap(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Mapeia um ResultSet para um Map com os dados da coleta.
     * 
     * @param rs ResultSet posicionado em uma linha
     * @return Map com os dados da coleta
     * @throws SQLException
     */
    private Map<String, Object> mapResultSetToMap(ResultSet rs) throws SQLException {
        Map<String, Object> coleta = new HashMap<>();
        
        coleta.put("id", rs.getInt("ID"));
        coleta.put("patrimonioId", rs.getInt("ID_PATRIMONIO"));
        coleta.put("numeroPatrimonio", rs.getString("NUMERO_PATRIMONIO"));
        coleta.put("descricaoPatrimonio", rs.getString("DESCRICAO_PATRIMONIO"));
        coleta.put("inventarioId", rs.getInt("ID_INVENTARIO"));
        coleta.put("nomeInventario", rs.getString("NOME_INVENTARIO"));
        
        // ID do coletor pode vir de ID_COLETOR ou ID_PARTICIPANTE_INVENTARIO
        Integer coletorId = rs.getInt("ID_COLETOR");
        if (coletorId == 0) {
            coletorId = rs.getInt("ID_PARTICIPANTE_INVENTARIO");
        }
        coleta.put("coletorId", coletorId);
        
        coleta.put("nomeColetorCompleto", rs.getString("NOME_COLETOR_COMPLETO"));
        coleta.put("dataColeta", rs.getTimestamp("DATA_COLETA"));
        coleta.put("localizacaoEncontrada", rs.getString("LOCALIZACAO_ENCONTRADA"));
        coleta.put("estadoEncontrado", rs.getString("ESTADO_ENCONTRADO"));
        coleta.put("observacoes", rs.getString("OBSERVACAO_COLETA"));
        
        // Dados da sala/setor (podem ser null)
        coleta.put("salaId", rs.getObject("SALA_ID"));
        coleta.put("nomeSala", rs.getString("NOME_SALA"));
        coleta.put("setorId", rs.getObject("SETOR_ID"));
        coleta.put("nomeSetor", rs.getString("NOME_SETOR"));
        
        return coleta;
    }
}
