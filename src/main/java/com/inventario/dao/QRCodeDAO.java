package com.inventario.dao;

import com.inventario.util.DatabaseConnection;
import com.inventario.model.QRCode;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operações com QR Codes
 */
@Repository
public class QRCodeDAO {
    
    /**
     * Salva um novo QR Code no banco de dados
     * @param qrCode QR Code a ser salvo
     * @return ID do QR Code salvo
     * @throws SQLException
     */
    public int salvar(QRCode qrCode) throws SQLException {
        String sql = """
            INSERT INTO TABELA_QR_CODE 
            (ID_PATRIMONIO, CODIGO_QR, HASH_DADOS, FORMATO, TAMANHO, ATIVO) 
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, qrCode.getIdPatrimonio());
            stmt.setString(2, qrCode.getCodigoQR());
            stmt.setString(3, qrCode.getHashDados());
            stmt.setString(4, qrCode.getFormato());
            stmt.setInt(5, qrCode.getTamanho());
            stmt.setBoolean(6, qrCode.isAtivo());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        qrCode.setId(id);
                        return id;
                    }
                }
            }
            
            throw new SQLException("Falha ao salvar QR Code, nenhum ID foi gerado.");
        }
    }
    
    /**
     * Atualiza um QR Code existente
     * @param qrCode QR Code a ser atualizado
     * @return true se foi atualizado com sucesso
     * @throws SQLException
     */
    public boolean atualizar(QRCode qrCode) throws SQLException {
        String sql = """
            UPDATE TABELA_QR_CODE 
            SET CODIGO_QR = ?, HASH_DADOS = ?, FORMATO = ?, TAMANHO = ?, ATIVO = ?
            WHERE ID = ?
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, qrCode.getCodigoQR());
            stmt.setString(2, qrCode.getHashDados());
            stmt.setString(3, qrCode.getFormato());
            stmt.setInt(4, qrCode.getTamanho());
            stmt.setBoolean(5, qrCode.isAtivo());
            stmt.setInt(6, qrCode.getId());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Busca QR Code por ID
     * @param id ID do QR Code
     * @return QR Code encontrado ou null
     * @throws SQLException
     */
    public QRCode buscarPorId(int id) throws SQLException {
        String sql = """
            SELECT qr.*, p.NUMERO as NUMERO_PATRIMONIO, p.DESCRICAO as DESCRICAO_PATRIMONIO
            FROM TABELA_QR_CODE qr
            LEFT JOIN TABELA_PATRIMONIO p ON qr.ID_PATRIMONIO = p.ID
            WHERE qr.ID = ?
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Busca QR Code ativo por patrimônio
     * @param idPatrimonio ID do patrimônio
     * @return QR Code ativo ou null
     * @throws SQLException
     */
    public QRCode buscarPorPatrimonio(int idPatrimonio) throws SQLException {
        String sql = """
            SELECT qr.*, p.NUMERO as NUMERO_PATRIMONIO, p.DESCRICAO as DESCRICAO_PATRIMONIO
            FROM TABELA_QR_CODE qr
            LEFT JOIN TABELA_PATRIMONIO p ON qr.ID_PATRIMONIO = p.ID
            WHERE qr.ID_PATRIMONIO = ? AND qr.ATIVO = true
            ORDER BY qr.DATA_GERACAO DESC
            LIMIT 1
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idPatrimonio);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Busca QR Code por hash dos dados
     * @param hash Hash dos dados
     * @return QR Code encontrado ou null
     * @throws SQLException
     */
    public QRCode buscarPorHash(String hash) throws SQLException {
        String sql = """
            SELECT qr.*, p.NUMERO as NUMERO_PATRIMONIO, p.DESCRICAO as DESCRICAO_PATRIMONIO
            FROM TABELA_QR_CODE qr
            LEFT JOIN TABELA_PATRIMONIO p ON qr.ID_PATRIMONIO = p.ID
            WHERE qr.HASH_DADOS = ?
            ORDER BY qr.DATA_GERACAO DESC
            LIMIT 1
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, hash);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Lista todos os QR Codes ativos
     * @return Lista de QR Codes ativos
     * @throws SQLException
     */
    public List<QRCode> listarAtivos() throws SQLException {
        String sql = """
            SELECT qr.*, p.NUMERO as NUMERO_PATRIMONIO, p.DESCRICAO as DESCRICAO_PATRIMONIO
            FROM TABELA_QR_CODE qr
            LEFT JOIN TABELA_PATRIMONIO p ON qr.ID_PATRIMONIO = p.ID
            WHERE qr.ATIVO = true
            ORDER BY qr.DATA_GERACAO DESC
            """;
        
        List<QRCode> qrCodes = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                qrCodes.add(mapearResultSet(rs));
            }
        }
        
        return qrCodes;
    }
    
    /**
     * Lista QR Codes com paginação
     * @param offset Offset para paginação
     * @param limit Limite de registros
     * @return Lista de QR Codes
     * @throws SQLException
     */
    public List<QRCode> listarComPaginacao(int offset, int limit) throws SQLException {
        String sql = """
            SELECT qr.*, p.NUMERO as NUMERO_PATRIMONIO, p.DESCRICAO as DESCRICAO_PATRIMONIO
            FROM TABELA_QR_CODE qr
            LEFT JOIN TABELA_PATRIMONIO p ON qr.ID_PATRIMONIO = p.ID
            ORDER BY qr.DATA_GERACAO DESC
            LIMIT ? OFFSET ?
            """;
        
        List<QRCode> qrCodes = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    qrCodes.add(mapearResultSet(rs));
                }
            }
        }
        
        return qrCodes;
    }
    
    /**
     * Desativa QR Code (marca como inativo)
     * @param id ID do QR Code
     * @return true se foi desativado com sucesso
     * @throws SQLException
     */
    public boolean desativar(int id) throws SQLException {
        String sql = "UPDATE TABELA_QR_CODE SET ATIVO = false WHERE ID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Desativa todos os QR Codes de um patrimônio
     * @param idPatrimonio ID do patrimônio
     * @return número de QR Codes desativados
     * @throws SQLException
     */
    public int desativarPorPatrimonio(int idPatrimonio) throws SQLException {
        String sql = "UPDATE TABELA_QR_CODE SET ATIVO = false WHERE ID_PATRIMONIO = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idPatrimonio);
            return stmt.executeUpdate();
        }
    }
    
    /**
     * Conta total de QR Codes
     * @return Total de QR Codes
     * @throws SQLException
     */
    public int contarTotal() throws SQLException {
        String sql = "SELECT COUNT(*) FROM TABELA_QR_CODE";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        return 0;
    }
    
    /**
     * Conta QR Codes ativos
     * @return Total de QR Codes ativos
     * @throws SQLException
     */
    public int contarAtivos() throws SQLException {
        String sql = "SELECT COUNT(*) FROM TABELA_QR_CODE WHERE ATIVO = true";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        return 0;
    }
    
    /**
     * Verifica se patrimônio já possui QR Code ativo
     * @param idPatrimonio ID do patrimônio
     * @return true se já possui QR Code ativo
     * @throws SQLException
     */
    public boolean patrimonioTemQRCodeAtivo(int idPatrimonio) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TABELA_QR_CODE WHERE ID_PATRIMONIO = ? AND ATIVO = true";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idPatrimonio);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Mapeia ResultSet para objeto QRCode
     * @param rs ResultSet
     * @return QRCode mapeado
     * @throws SQLException
     */
    private QRCode mapearResultSet(ResultSet rs) throws SQLException {
        QRCode qrCode = new QRCode();
        
        qrCode.setId(rs.getInt("ID"));
        qrCode.setIdPatrimonio(rs.getInt("ID_PATRIMONIO"));
        qrCode.setCodigoQR(rs.getString("CODIGO_QR"));
        qrCode.setHashDados(rs.getString("HASH_DADOS"));
        qrCode.setDataGeracao(rs.getTimestamp("DATA_GERACAO"));
        qrCode.setFormato(rs.getString("FORMATO"));
        qrCode.setTamanho(rs.getInt("TAMANHO"));
        qrCode.setAtivo(rs.getBoolean("ATIVO"));
        qrCode.setCreatedAt(rs.getTimestamp("CREATED_AT"));
        qrCode.setUpdatedAt(rs.getTimestamp("UPDATED_AT"));
        
        // Campos transientes do patrimônio
        qrCode.setNumeroPatrimonio(rs.getString("NUMERO_PATRIMONIO"));
        qrCode.setDescricaoPatrimonio(rs.getString("DESCRICAO_PATRIMONIO"));
        
        return qrCode;
    }
}