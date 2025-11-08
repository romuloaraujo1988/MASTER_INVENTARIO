package com.inventario.dao;

import com.inventario.model.Coleta;
import com.inventario.util.DatabaseConnection;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DAO para operações com Coleta
 * Gerencia as operações de banco de dados para coletas de inventário
 */
@Repository
public class ColetaDAO {
    

    
    public void inserirColeta(Coleta coleta) throws SQLException {
        System.out.println("[DEBUG ColetaDAO] ========================================");
        System.out.println("[DEBUG ColetaDAO] Inserindo coleta:");
        System.out.println("[DEBUG ColetaDAO]   idInventario = " + coleta.getIdInventario());
        System.out.println("[DEBUG ColetaDAO]   idColetor = " + coleta.getIdColetor());
        System.out.println("[DEBUG ColetaDAO]   idParticipanteInventario = " + coleta.getIdParticipanteInventario());
        
        // Se idParticipanteInventario já foi informado e é válido, usar ele
        // Caso contrário, buscar baseado no idColetor
        if (coleta.getIdParticipanteInventario() == 0 && coleta.getIdColetor() > 0) {
            System.out.println("[DEBUG ColetaDAO] idParticipanteInventario não informado, buscando...");
            ParticipanteInventarioDAO participanteDAO = new ParticipanteInventarioDAO();
            Integer idParticipante = participanteDAO.buscarIdParticipantePorUsuario(
                coleta.getIdInventario(), coleta.getIdColetor());
            
            System.out.println("[DEBUG ColetaDAO]   idParticipante encontrado = " + idParticipante);
            
            if (idParticipante != null) {
                coleta.setIdParticipanteInventario(idParticipante);
                System.out.println("[DEBUG ColetaDAO] Usando idParticipanteInventario = " + idParticipante);
            } else {
                // Se não encontrou, usar 0 ou o ID do usuário como fallback
                System.out.println("[DEBUG ColetaDAO] AVISO: Participante não encontrado, usando fallback");
                coleta.setIdParticipanteInventario(coleta.getIdColetor());
            }
        }
        
        System.out.println("[DEBUG ColetaDAO] Prosseguindo com inserção...");
        System.out.println("[DEBUG ColetaDAO] ========================================");
        
        // Incluir AMBAS as colunas ID_COLETOR e ID_PARTICIPANTE_INVENTARIO para compatibilidade
        String sql = "INSERT INTO TABELA_COLETA (ID_INVENTARIO, ID_PATRIMONIO, ID_COLETOR, ID_PARTICIPANTE_INVENTARIO, " +
                  "DATA_COLETA, STATUS_COLETA, OBSERVACAO_COLETA, LOCALIZACAO_ATUAL, " +
                  "LOCALIZACAO_ENCONTRADA, ESTADO_ENCONTRADO, DIVERGENCIA, MOTIVO_DIVERGENCIA, " +
                  "LATITUDE, LONGITUDE, FOTO_PATRIMONIO, SEM_ETIQUETA, " +
                  "DESCRICAO_ITEM_SEM_ETIQUETA, CATEGORIA_ITEM_SEM_ETIQUETA) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, coleta.getIdInventario());
            // ID_PATRIMONIO pode ser NULL para itens sem etiqueta
            if (coleta.isSemEtiqueta() || coleta.getIdPatrimonio() == 0) {
                stmt.setNull(2, Types.INTEGER);
            } else {
                stmt.setInt(2, coleta.getIdPatrimonio());
            }
            stmt.setInt(3, coleta.getIdColetor()); // ID_COLETOR (obrigatório)
            stmt.setInt(4, coleta.getIdParticipanteInventario()); // ID_PARTICIPANTE_INVENTARIO
            stmt.setTimestamp(5, coleta.getDataColeta());
            stmt.setString(6, coleta.getStatusColeta());
            stmt.setString(7, coleta.getObservacaoColeta());
            stmt.setString(8, coleta.getLocalizacaoAtual());
            stmt.setString(9, coleta.getLocalizacaoEncontrada());
            stmt.setString(10, coleta.getEstadoEncontrado());
            stmt.setBoolean(11, coleta.isDivergencia());
            stmt.setString(12, coleta.getMotivoDivergencia());
            
            if (coleta.getLatitude() != null) {
                stmt.setBigDecimal(13, coleta.getLatitude());
            } else {
                stmt.setNull(13, Types.DECIMAL);
            }
            
            if (coleta.getLongitude() != null) {
                stmt.setBigDecimal(14, coleta.getLongitude());
            } else {
                stmt.setNull(14, Types.DECIMAL);
            }
            
            stmt.setString(15, coleta.getFotoPatrimonio());
            stmt.setBoolean(16, coleta.isSemEtiqueta());
            stmt.setString(17, coleta.getDescricaoItemSemEtiqueta());
            stmt.setString(18, coleta.getCategoriaItemSemEtiqueta());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    coleta.setId(rs.getInt(1));
                }
            }
        }
    }
    
    public void atualizarColeta(Coleta coleta) throws SQLException {
        // Usar ID_PARTICIPANTE_INVENTARIO se disponível, senão usar ID_USUARIO (compatibilidade)
        String sql;
        if (coleta.getIdParticipanteInventario() > 0) {
            sql = "UPDATE TABELA_COLETA SET ID_INVENTARIO = ?, ID_PATRIMONIO = ?, " +
                  "ID_PARTICIPANTE_INVENTARIO = ?, DATA_COLETA = ?, STATUS_COLETA = ?, OBSERVACAO_COLETA = ?, " +
                  "LOCALIZACAO_ATUAL = ?, LOCALIZACAO_ENCONTRADA = ?, ESTADO_ENCONTRADO = ?, " +
                  "DIVERGENCIA = ?, MOTIVO_DIVERGENCIA = ?, LATITUDE = ?, LONGITUDE = ?, " +
                  "FOTO_PATRIMONIO = ?, SEM_ETIQUETA = ?, DESCRICAO_ITEM_SEM_ETIQUETA = ?, " +
                  "CATEGORIA_ITEM_SEM_ETIQUETA = ? WHERE ID = ?";
        } else {
            sql = "UPDATE TABELA_COLETA SET ID_INVENTARIO = ?, ID_PATRIMONIO = ?, " +
                  "ID_USUARIO = ?, DATA_COLETA = ?, STATUS_COLETA = ?, OBSERVACAO_COLETA = ?, " +
                  "LOCALIZACAO_ATUAL = ?, LOCALIZACAO_ENCONTRADA = ?, ESTADO_ENCONTRADO = ?, " +
                  "DIVERGENCIA = ?, MOTIVO_DIVERGENCIA = ?, LATITUDE = ?, LONGITUDE = ?, " +
                  "FOTO_PATRIMONIO = ?, SEM_ETIQUETA = ?, DESCRICAO_ITEM_SEM_ETIQUETA = ?, " +
                  "CATEGORIA_ITEM_SEM_ETIQUETA = ? WHERE ID = ?";
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, coleta.getIdInventario());
            stmt.setInt(2, coleta.getIdPatrimonio());
            // Usar ID_PARTICIPANTE_INVENTARIO se disponível, senão usar ID_COLETOR
            if (coleta.getIdParticipanteInventario() > 0) {
                stmt.setInt(3, coleta.getIdParticipanteInventario());
            } else {
                stmt.setInt(3, coleta.getIdColetor());
            }
            stmt.setTimestamp(4, coleta.getDataColeta());
            stmt.setString(5, coleta.getStatusColeta());
            stmt.setString(6, coleta.getObservacaoColeta());
            stmt.setString(7, coleta.getLocalizacaoAtual());
            stmt.setString(8, coleta.getLocalizacaoEncontrada());
            stmt.setString(9, coleta.getEstadoEncontrado());
            stmt.setBoolean(10, coleta.isDivergencia());
            stmt.setString(11, coleta.getMotivoDivergencia());
            
            if (coleta.getLatitude() != null) {
                stmt.setBigDecimal(12, coleta.getLatitude());
            } else {
                stmt.setNull(12, Types.DECIMAL);
            }
            
            if (coleta.getLongitude() != null) {
                stmt.setBigDecimal(13, coleta.getLongitude());
            } else {
                stmt.setNull(13, Types.DECIMAL);
            }
            
            stmt.setString(14, coleta.getFotoPatrimonio());
            stmt.setBoolean(15, coleta.isSemEtiqueta());
            stmt.setString(16, coleta.getDescricaoItemSemEtiqueta());
            stmt.setString(17, coleta.getCategoriaItemSemEtiqueta());
            stmt.setInt(18, coleta.getId());
            
            stmt.executeUpdate();
        }
    }
    
    public void excluirColeta(int id) throws SQLException {
        String sql = "DELETE FROM TABELA_COLETA WHERE ID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Exclui coleta baseado em critérios específicos (para uso na interface)
     */
    public boolean excluirColeta(int idSala, String numeroPatrimonio, String dataHora, String estado, String observacoes) throws SQLException {
        // Primeiro, buscar a coleta específica
        String sqlBusca = "SELECT c.ID FROM TABELA_COLETA c " +
                         "LEFT JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID " +
                         "WHERE p.ID_SALA = ? AND p.NUMERO = ? AND c.ESTADO_ENCONTRADO = ? " +
                         "AND c.OBSERVACAO_COLETA = ? " +
                         "AND DATE_FORMAT(c.DATA_COLETA, '%d/%m/%Y %H:%i') = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmtBusca = conn.prepareStatement(sqlBusca)) {
            
            stmtBusca.setInt(1, idSala);
            stmtBusca.setString(2, numeroPatrimonio);
            stmtBusca.setString(3, estado);
            stmtBusca.setString(4, observacoes != null ? observacoes : "");
            stmtBusca.setString(5, dataHora);
            
            try (ResultSet rs = stmtBusca.executeQuery()) {
                if (rs.next()) {
                    int idColeta = rs.getInt("ID");
                    
                    // Agora excluir a coleta
                    String sqlExcluir = "DELETE FROM TABELA_COLETA WHERE ID = ?";
                    try (PreparedStatement stmtExcluir = conn.prepareStatement(sqlExcluir)) {
                        stmtExcluir.setInt(1, idColeta);
                        int rowsAffected = stmtExcluir.executeUpdate();
                        return rowsAffected > 0;
                    }
                }
            }
        }
        
        return false;
    }
    
    public Coleta buscarPorId(int id) throws SQLException {
        String sql = "SELECT c.*, p.NUMERO as NUMERO_PATRIMONIO, p.DESCRICAO as DESCRICAO_PATRIMONIO, " +
                    "u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO " +
                    "FROM TABELA_COLETA c " +
                    "LEFT JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID " +
                    "LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                    "LEFT JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID " +
                    "LEFT JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID " +
                    "WHERE c.ID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return criarColetaFromResultSet(rs);
                }
            }
        }
        
        return null;
    }
    
    public List<Coleta> listarTodas() throws SQLException {
        String sql = "SELECT c.*, p.NUMERO as NUMERO_PATRIMONIO, p.DESCRICAO as DESCRICAO_PATRIMONIO, " +
                    "u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO " +
                    "FROM TABELA_COLETA c " +
                    "LEFT JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID " +
                    "LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                    "LEFT JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID " +
                    "LEFT JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID " +
                    "ORDER BY c.DATA_COLETA DESC";
        
        List<Coleta> coletas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                coletas.add(criarColetaFromResultSet(rs));
            }
        }
        
        return coletas;
    }
    
    public List<Coleta> buscarPorInventario(int idInventario) throws SQLException {
        String sql = "SELECT c.*, p.NUMERO as NUMERO_PATRIMONIO, p.DESCRICAO as DESCRICAO_PATRIMONIO, " +
                    "u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO " +
                    "FROM TABELA_COLETA c " +
                    "LEFT JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID " +
                    "LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                    "LEFT JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID " +
                    "LEFT JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID " +
                    "WHERE c.ID_INVENTARIO = ? ORDER BY c.DATA_COLETA DESC";
        
        List<Coleta> coletas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    coletas.add(criarColetaFromResultSet(rs));
                }
            }
        }
        
        return coletas;
    }
    
    public List<Coleta> buscarPorColetor(int idColetor) throws SQLException {
        String sql = "SELECT c.*, p.NUMERO as NUMERO_PATRIMONIO, p.DESCRICAO as DESCRICAO_PATRIMONIO, " +
                    "u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO " +
                    "FROM TABELA_COLETA c " +
                    "LEFT JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID " +
                    "LEFT JOIN TABELA_USUARIO u ON c.ID_COLETOR = u.ID " +
                    "LEFT JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID " +
                    "WHERE c.ID_COLETOR = ? ORDER BY c.DATA_COLETA DESC";
        
        List<Coleta> coletas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idColetor);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    coletas.add(criarColetaFromResultSet(rs));
                }
            }
        }
        
        return coletas;
    }
    
    public List<Coleta> buscarPorPatrimonio(int idPatrimonio) throws SQLException {
        String sql = "SELECT c.*, p.NUMERO as NUMERO_PATRIMONIO, p.DESCRICAO as DESCRICAO_PATRIMONIO, " +
                    "u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO " +
                    "FROM TABELA_COLETA c " +
                    "LEFT JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID " +
                    "LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                    "LEFT JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID " +
                    "LEFT JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID " +
                    "WHERE c.ID_PATRIMONIO = ? ORDER BY c.DATA_COLETA DESC";
        
        List<Coleta> coletas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idPatrimonio);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    coletas.add(criarColetaFromResultSet(rs));
                }
            }
        }
        
        return coletas;
    }
    
    public List<Coleta> buscarPorStatus(String status) throws SQLException {
        String sql = "SELECT c.*, p.NUMERO as NUMERO_PATRIMONIO, p.DESCRICAO as DESCRICAO_PATRIMONIO, " +
                    "u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO " +
                    "FROM TABELA_COLETA c " +
                    "LEFT JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID " +
                    "LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                    "LEFT JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID " +
                    "LEFT JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID " +
                    "WHERE c.STATUS_COLETA = ? ORDER BY c.DATA_COLETA DESC";
        
        List<Coleta> coletas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    coletas.add(criarColetaFromResultSet(rs));
                }
            }
        }
        
        return coletas;
    }
    
    public List<Coleta> buscarColetasPorSala(int idSala) throws SQLException {
        String sql = "SELECT c.*, p.NUMERO as NUMERO_PATRIMONIO, p.DESCRICAO as DESCRICAO_PATRIMONIO, " +
                    "u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO " +
                    "FROM TABELA_COLETA c " +
                    "LEFT JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID " +
                    "LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                    "LEFT JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID " +
                    "LEFT JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID " +
                    "WHERE p.ID_SALA = ? ORDER BY c.DATA_COLETA DESC";
        
        List<Coleta> coletas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idSala);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    coletas.add(criarColetaFromResultSet(rs));
                }
            }
        }
        
        return coletas;
    }
    
    /**
     * Busca APENAS coletas de patrimônios COM etiqueta em uma sala específica
     * @param idSala ID da sala
     * @return Lista de coletas de patrimônios com etiqueta
     */
    public List<Coleta> buscarColetasComEtiquetaPorSala(int idSala) throws SQLException {
        String sql = "SELECT c.*, p.NUMERO as NUMERO_PATRIMONIO, p.DESCRICAO as DESCRICAO_PATRIMONIO, " +
                    "u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO " +
                    "FROM TABELA_COLETA c " +
                    "INNER JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID " +
                    "LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                    "LEFT JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID " +
                    "LEFT JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID " +
                    "WHERE p.ID_SALA = ? AND (c.SEM_ETIQUETA = false OR c.SEM_ETIQUETA IS NULL) " +
                    "ORDER BY c.DATA_COLETA DESC";
        
        List<Coleta> coletas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idSala);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    coletas.add(criarColetaFromResultSet(rs));
                }
            }
        }
        
        return coletas;
    }
    
    /**
     * Busca APENAS coletas de itens SEM etiqueta em uma sala específica
     * @param idSala ID da sala
     * @param localizacaoSala Identificação completa da sala para filtrar pela localização
     * @return Lista de coletas de itens sem etiqueta
     */
    public List<Coleta> buscarColetasSemEtiquetaPorSala(int idSala, String localizacaoSala) throws SQLException {
        String sql = "SELECT c.*, u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO " +
                    "FROM TABELA_COLETA c " +
                    "LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                    "LEFT JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID " +
                    "LEFT JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID " +
                    "WHERE c.SEM_ETIQUETA = true " +
                    "AND c.LOCALIZACAO_ENCONTRADA = ? " +
                    "ORDER BY c.DATA_COLETA DESC";
        
        List<Coleta> coletas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, localizacaoSala);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    coletas.add(criarColetaFromResultSet(rs));
                }
            }
        }
        
        return coletas;
    }
    
    public List<Coleta> buscarComDivergencia() throws SQLException {
        String sql = "SELECT c.*, p.NUMERO as NUMERO_PATRIMONIO, p.DESCRICAO as DESCRICAO_PATRIMONIO, " +
                    "u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO " +
                    "FROM TABELA_COLETA c " +
                    "LEFT JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID " +
                    "LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                    "LEFT JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID " +
                    "LEFT JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID " +
                    "WHERE c.DIVERGENCIA = true ORDER BY c.DATA_COLETA DESC";
        
        List<Coleta> coletas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                coletas.add(criarColetaFromResultSet(rs));
            }
        }
        
        return coletas;
    }
    
    public boolean coletaExiste(int idInventario, int idPatrimonio) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TABELA_COLETA WHERE ID_INVENTARIO = ? AND ID_PATRIMONIO = ? AND STATUS_COLETA = 'COLETADO'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            stmt.setInt(2, idPatrimonio);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    public int contarColetasPorInventario(int idInventario) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TABELA_COLETA WHERE ID_INVENTARIO = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        
        return 0;
    }
    
    public int contarColetasPorColetor(int idColetor) throws SQLException {
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
        }
        
        return 0;
    }
    
    /**
     * Busca todas as coletas de itens sem etiqueta
     */
    public List<Coleta> buscarItensSemEtiqueta() throws SQLException {
        String sql = "SELECT c.*, u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO " +
                    "FROM TABELA_COLETA c " +
                    "LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                    "LEFT JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID " +
                    "LEFT JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID " +
                    "WHERE c.SEM_ETIQUETA = true ORDER BY c.DATA_COLETA DESC";
        
        List<Coleta> coletas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                coletas.add(criarColetaFromResultSet(rs));
            }
        }
        
        return coletas;
    }
    
    /**
     * Busca itens sem etiqueta por inventário
     */
    public List<Coleta> buscarItensSemEtiquetaPorInventario(int idInventario) throws SQLException {
        String sql = "SELECT c.*, u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO " +
                    "FROM TABELA_COLETA c " +
                    "LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                    "LEFT JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID " +
                    "LEFT JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID " +
                    "WHERE c.SEM_ETIQUETA = true AND c.ID_INVENTARIO = ? ORDER BY c.DATA_COLETA DESC";
        
        List<Coleta> coletas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    coletas.add(criarColetaFromResultSet(rs));
                }
            }
        }
        
        return coletas;
    }
    
    /**
     * Busca itens sem etiqueta por categoria
     */
    public List<Coleta> buscarItensSemEtiquetaPorCategoria(String categoria) throws SQLException {
        String sql = "SELECT c.*, u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO " +
                    "FROM TABELA_COLETA c " +
                    "LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                    "LEFT JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID " +
                    "LEFT JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID " +
                    "WHERE c.SEM_ETIQUETA = true AND c.CATEGORIA_ITEM_SEM_ETIQUETA = ? ORDER BY c.DATA_COLETA DESC";
        
        List<Coleta> coletas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, categoria);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    coletas.add(criarColetaFromResultSet(rs));
                }
            }
        }
        
        return coletas;
    }
    
    /**
     * Conta o número de itens sem etiqueta por inventário
     */
    public int contarItensSemEtiquetaPorInventario(int idInventario) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TABELA_COLETA WHERE SEM_ETIQUETA = true AND ID_INVENTARIO = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Agrupa itens sem etiqueta por descrições similares para facilitar identificação
     * Retorna uma lista com descrições agrupadas e contagem de ocorrências
     */
    public List<Object[]> agruparItensSemEtiquetaPorDescricao() throws SQLException {
        // Funcionalidade temporariamente desabilitada - colunas SEM_ETIQUETA e DESCRICAO_ITEM_SEM_ETIQUETA não existem
        List<Object[]> resultados = new ArrayList<>();
        return resultados;
    }
    
    /**
     * Agrupa itens sem etiqueta por descrições similares para um inventário específico
     */
    public List<Map<String, Object>> agruparItensSemEtiquetaPorDescricao(int idInventario) throws SQLException {
        // Funcionalidade temporariamente desabilitada - colunas SEM_ETIQUETA e DESCRICAO_ITEM_SEM_ETIQUETA não existem
        List<Map<String, Object>> resultados = new ArrayList<>();
        // TODO: Executar script implementar_itens_sem_etiqueta.sql para adicionar as colunas necessárias
        return resultados;
    }
    
    /**
     * Busca itens sem etiqueta com descrição similar (para análise de duplicatas)
     */
    public List<Coleta> buscarItensSemEtiquetaPorDescricaoSimilar(String descricao) throws SQLException {
        String sql = "SELECT c.*, u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO " +
                    "FROM TABELA_COLETA c " +
                    "LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                    "LEFT JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID " +
                    "LEFT JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID " +
                    "WHERE c.SEM_ETIQUETA = true " +
                    "AND UPPER(TRIM(c.DESCRICAO_ITEM_SEM_ETIQUETA)) = UPPER(TRIM(?)) " +
                    "ORDER BY c.DATA_COLETA DESC";
        
        List<Coleta> coletas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, descricao);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    coletas.add(criarColetaFromResultSet(rs));
                }
            }
        }
        
        return coletas;
    }
    
    /**
     * Busca itens sem etiqueta por descrição de forma flexível, permitindo que os termos
     * apareçam em qualquer ordem na descrição
     * @param descricao Descrição com termos separados por espaço
     * @return Lista de coletas de itens sem etiqueta encontrados
     */
    public List<Coleta> buscarItensSemEtiquetaPorDescricaoFlexivel(String descricao) throws SQLException {
        if (descricao == null || descricao.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // Implementação simplificada da busca flexível
        String sql = "SELECT c.*, u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO " +
                    "FROM TABELA_COLETA c " +
                    "LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                    "LEFT JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID " +
                    "LEFT JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID " +
                    "WHERE c.SEM_ETIQUETA = true AND LOWER(c.DESCRICAO_ITEM_SEM_ETIQUETA) LIKE ? " +
                    "ORDER BY c.DATA_COLETA DESC";
        
        List<Coleta> coletas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String termoBusca = "%" + descricao.trim().toLowerCase() + "%";
            stmt.setString(1, termoBusca);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    coletas.add(criarColetaFromResultSet(rs));
                }
            }
        }
        
        return coletas;
    }
    
    private Coleta criarColetaFromResultSet(ResultSet rs) throws SQLException {
        Coleta coleta = new Coleta();
        
        coleta.setId(rs.getInt("ID"));
        coleta.setIdInventario(rs.getInt("ID_INVENTARIO"));
        coleta.setIdPatrimonio(rs.getInt("ID_PATRIMONIO"));
        
        // Suporte para ambos os campos durante a transição
        try {
            // Tentar ler ID_PARTICIPANTE_INVENTARIO primeiro (nova estrutura)
            int idParticipante = rs.getInt("ID_PARTICIPANTE_INVENTARIO");
            if (!rs.wasNull() && idParticipante > 0) {
                coleta.setIdParticipanteInventario(idParticipante);
            }
        } catch (SQLException e) {
            // Coluna não existe ainda, ignorar
        }
        
        try {
            // Manter compatibilidade com ID_USUARIO/ID_COLETOR
            int idUsuario = rs.getInt("ID_USUARIO");
            if (!rs.wasNull()) {
                coleta.setIdColetor(idUsuario);
            }
        } catch (SQLException e) {
            // Tentar ID_COLETOR se ID_USUARIO não existir
            try {
                int idColetor = rs.getInt("ID_COLETOR");
                if (!rs.wasNull()) {
                    coleta.setIdColetor(idColetor);
                }
            } catch (SQLException e2) {
                // Nenhum dos campos existe, ignorar
            }
        }
        coleta.setDataColeta(rs.getTimestamp("DATA_COLETA"));
        coleta.setStatusColeta(rs.getString("STATUS_COLETA"));
        coleta.setObservacaoColeta(rs.getString("OBSERVACAO_COLETA"));
        coleta.setLocalizacaoAtual(rs.getString("LOCALIZACAO_ATUAL"));
        coleta.setLocalizacaoEncontrada(rs.getString("LOCALIZACAO_ENCONTRADA"));
        coleta.setEstadoEncontrado(rs.getString("ESTADO_ENCONTRADO"));
        coleta.setDivergencia(rs.getBoolean("DIVERGENCIA"));
        coleta.setMotivoDivergencia(rs.getString("MOTIVO_DIVERGENCIA"));
        coleta.setLatitude(rs.getBigDecimal("LATITUDE"));
        coleta.setLongitude(rs.getBigDecimal("LONGITUDE"));
        coleta.setFotoPatrimonio(rs.getString("FOTO_PATRIMONIO"));
        
        // Campos para itens sem etiqueta
        coleta.setSemEtiqueta(rs.getBoolean("SEM_ETIQUETA"));
        coleta.setDescricaoItemSemEtiqueta(rs.getString("DESCRICAO_ITEM_SEM_ETIQUETA"));
        coleta.setCategoriaItemSemEtiqueta(rs.getString("CATEGORIA_ITEM_SEM_ETIQUETA"));
        
        // Campos transientes - tratar NULL para itens sem patrimônio
        try {
            coleta.setNumeroPatrimonio(rs.getString("NUMERO_PATRIMONIO"));
        } catch (SQLException e) {
            coleta.setNumeroPatrimonio(null);
        }
        
        try {
            coleta.setDescricaoPatrimonio(rs.getString("DESCRICAO_PATRIMONIO"));
        } catch (SQLException e) {
            coleta.setDescricaoPatrimonio(null);
        }
        
        try {
            coleta.setNomeColetor(rs.getString("NOME_COLETOR"));
        } catch (SQLException e) {
            coleta.setNomeColetor(null);
        }
        
        try {
            coleta.setDescricaoInventario(rs.getString("DESCRICAO_INVENTARIO"));
        } catch (SQLException e) {
            coleta.setDescricaoInventario(null);
        }
        
        return coleta;
    }
    
    /**
     * Busca abrangente por descrição de itens sem etiqueta
     * Busca em múltiplos campos e ordena por relevância
     * @param descricao Descrição com termos separados por espaço
     * @return Lista de coletas de itens sem etiqueta encontrados ordenados por relevância
     */
    public List<Coleta> buscarItensSemEtiquetaPorDescricaoAbrangente(String descricao) throws SQLException {
        if (descricao == null || descricao.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // Dividir a descrição em termos
        String[] termos = descricao.trim().toLowerCase().split("\\s+");
        
        // Construir a consulta SQL com busca em múltiplos campos e ordenação por relevância
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT c.*, u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO, ");
        
        // Calcular score de relevância (sem unaccent)
        sqlBuilder.append("(");
        for (int i = 0; i < termos.length; i++) {
            if (i > 0) sqlBuilder.append(" + ");
            sqlBuilder.append("(CASE WHEN LOWER(c.DESCRICAO_ITEM_SEM_ETIQUETA) LIKE ? THEN 10 ELSE 0 END)");
            sqlBuilder.append(" + (CASE WHEN LOWER(c.CATEGORIA_ITEM_SEM_ETIQUETA) LIKE ? THEN 5 ELSE 0 END)");
            sqlBuilder.append(" + (CASE WHEN LOWER(c.LOCALIZACAO_ENCONTRADA) LIKE ? THEN 3 ELSE 0 END)");
        }
        sqlBuilder.append(") as relevancia ");
        
        sqlBuilder.append("FROM TABELA_COLETA c ");
        sqlBuilder.append("LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante ");
        sqlBuilder.append("LEFT JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID ");
        sqlBuilder.append("LEFT JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID ");
        sqlBuilder.append("WHERE c.SEM_ETIQUETA = true AND (");
        
        // Condições de busca (OR para ser mais flexível)
        for (int i = 0; i < termos.length; i++) {
            if (i > 0) sqlBuilder.append(" OR ");
            sqlBuilder.append("(LOWER(c.DESCRICAO_ITEM_SEM_ETIQUETA) LIKE ? ");
            sqlBuilder.append("OR LOWER(c.CATEGORIA_ITEM_SEM_ETIQUETA) LIKE ? ");
            sqlBuilder.append("OR LOWER(c.LOCALIZACAO_ENCONTRADA) LIKE ?)");
        }
        
        sqlBuilder.append(") ORDER BY relevancia DESC, c.DATA_COLETA DESC");
        
        List<Coleta> coletas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {
            
            int paramIndex = 1;
            
            // Parâmetros para o cálculo de relevância
            for (String termo : termos) {
                String termoBusca = "%" + termo + "%";
                stmt.setString(paramIndex++, termoBusca); // DESCRICAO_ITEM_SEM_ETIQUETA
                stmt.setString(paramIndex++, termoBusca); // CATEGORIA_ITEM_SEM_ETIQUETA
                stmt.setString(paramIndex++, termoBusca); // LOCALIZACAO_ENCONTRADA
            }
            
            // Parâmetros para as condições WHERE
            for (String termo : termos) {
                String termoBusca = "%" + termo + "%";
                stmt.setString(paramIndex++, termoBusca); // DESCRICAO_ITEM_SEM_ETIQUETA
                stmt.setString(paramIndex++, termoBusca); // CATEGORIA_ITEM_SEM_ETIQUETA
                stmt.setString(paramIndex++, termoBusca); // LOCALIZACAO_ENCONTRADA
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    coletas.add(criarColetaFromResultSet(rs));
                }
            }
        }
        
        return coletas;
    }
    
    /**
     * Versão alternativa da busca abrangente sem usar unaccent (para compatibilidade)
     */
    private List<Coleta> buscarItensSemEtiquetaPorDescricaoAbrangenteSemUnaccent(String descricao) throws SQLException {
        String descricaoNormalizada = normalizarTexto(descricao.trim());
        String[] termos = descricaoNormalizada.split("\\s+");
        
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT c.*, u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO ");
        sqlBuilder.append("FROM TABELA_COLETA c ");
        sqlBuilder.append("LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante ");
        sqlBuilder.append("LEFT JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID ");
        sqlBuilder.append("LEFT JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID ");
        sqlBuilder.append("WHERE c.SEM_ETIQUETA = true AND (");
        
        for (int i = 0; i < termos.length; i++) {
            if (i > 0) sqlBuilder.append(" OR ");
            sqlBuilder.append("(LOWER(c.DESCRICAO_ITEM_SEM_ETIQUETA) LIKE ? ");
            sqlBuilder.append("OR LOWER(c.CATEGORIA_ITEM_SEM_ETIQUETA) LIKE ? ");
            sqlBuilder.append("OR LOWER(c.LOCALIZACAO_ENCONTRADA) LIKE ?)");
        }
        
        sqlBuilder.append(") ORDER BY ");
        
        // Ordenação por relevância manual
        sqlBuilder.append("CASE ");
        for (String termo : termos) {
            sqlBuilder.append("WHEN LOWER(c.DESCRICAO_ITEM_SEM_ETIQUETA) LIKE '%" + termo + "%' THEN 1 ");
        }
        sqlBuilder.append("ELSE 2 END, c.DATA_COLETA DESC");
        
        List<Coleta> coletas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {
            
            int paramIndex = 1;
            for (String termo : termos) {
                String termoBusca = "%" + termo + "%";
                stmt.setString(paramIndex++, termoBusca); // DESCRICAO_ITEM_SEM_ETIQUETA
                stmt.setString(paramIndex++, termoBusca); // CATEGORIA_ITEM_SEM_ETIQUETA
                stmt.setString(paramIndex++, termoBusca); // LOCALIZACAO_ENCONTRADA
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    coletas.add(criarColetaFromResultSet(rs));
                }
            }
        }
        
        return coletas;
    }
    
    /**
     * Busca itens sem etiqueta por categoria e descrição
     */
    public List<Coleta> buscarItensSemEtiquetaPorCategoriaEDescricao(String categoria, String descricao) throws SQLException {
        System.out.println("=== DEBUG DAO BUSCA POR CATEGORIA E DESCRIÇÃO ===");
        System.out.println("DEBUG DAO: Parâmetros recebidos:");
        System.out.println("  - categoria: '" + categoria + "'");
        System.out.println("  - descricao: '" + descricao + "'");
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT c.*, u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO ");
        sql.append("FROM TABELA_COLETA c ");
        sql.append("LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante ");
        sql.append("LEFT JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID ");
        sql.append("LEFT JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID ");
        sql.append("WHERE c.SEM_ETIQUETA = true ");
        
        List<Object> parametros = new ArrayList<>();
        
        // Filtro por categoria se especificada
        if (categoria != null && !categoria.trim().isEmpty() && !"TODAS".equalsIgnoreCase(categoria)) {
            System.out.println("DEBUG DAO: Adicionando filtro por categoria: " + categoria);
            sql.append("AND c.CATEGORIA_ITEM_SEM_ETIQUETA = ? ");
            parametros.add(categoria);
        } else {
            System.out.println("DEBUG DAO: Sem filtro por categoria (categoria é TODAS ou vazia)");
        }
        
        // Filtro por descrição se especificada
        if (descricao != null && !descricao.trim().isEmpty()) {
            System.out.println("DEBUG DAO: Adicionando filtro por descrição: " + descricao);
            sql.append("AND (LOWER(c.DESCRICAO_ITEM_SEM_ETIQUETA) LIKE ? ");
            sql.append("OR LOWER(c.OBSERVACAO_COLETA) LIKE ?) ");
            String termoBusca = "%" + descricao.toLowerCase() + "%";
            parametros.add(termoBusca);
            parametros.add(termoBusca);
            System.out.println("DEBUG DAO: Termo de busca formatado: '" + termoBusca + "'");
        } else {
            System.out.println("DEBUG DAO: Sem filtro por descrição (descrição vazia)");
        }
        
        sql.append("ORDER BY c.DATA_COLETA DESC");
        
        System.out.println("DEBUG DAO: SQL final: " + sql.toString());
        System.out.println("DEBUG DAO: Parâmetros: " + parametros);
        
        List<Coleta> coletas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            System.out.println("DEBUG DAO: Conexão obtida com sucesso");
            
            // Definir parâmetros
            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
                System.out.println("DEBUG DAO: Parâmetro " + (i+1) + " = " + parametros.get(i));
            }
            
            System.out.println("DEBUG DAO: Executando consulta...");
            try (ResultSet rs = stmt.executeQuery()) {
                int contador = 0;
                while (rs.next()) {
                    coletas.add(criarColetaFromResultSet(rs));
                    contador++;
                    if (contador <= 3) { // Log apenas os primeiros 3 resultados
                        System.out.println("DEBUG DAO: Resultado " + contador + " - ID: " + rs.getLong("ID") + 
                                         ", Descrição: " + rs.getString("DESCRICAO_ITEM_SEM_ETIQUETA") +
                                         ", Categoria: " + rs.getString("CATEGORIA_ITEM_SEM_ETIQUETA"));
                    }
                }
                System.out.println("DEBUG DAO: Total de resultados encontrados: " + contador);
            }
        } catch (SQLException e) {
            System.out.println("DEBUG DAO: ERRO na execução da consulta: " + e.getMessage());
            throw e;
        }
        
        System.out.println("DEBUG DAO: Retornando " + coletas.size() + " coletas");
        return coletas;
    }

    /**
     * Normaliza texto removendo acentos e caracteres especiais
     */
    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        
        // Remover acentos manualmente (versão simplificada)
        return texto.toLowerCase()
                   .replace("á", "a").replace("à", "a").replace("ã", "a").replace("â", "a")
                   .replace("é", "e").replace("ê", "e")
                   .replace("í", "i")
                   .replace("ó", "o").replace("ô", "o").replace("õ", "o")
                   .replace("ú", "u").replace("ü", "u")
                   .replace("ç", "c")
                   .replaceAll("[^a-z0-9\\s]", " ")
                   .replaceAll("\\s+", " ")
                   .trim();
    }

    /**
     * Busca patrimônios por descrição na tabela_patrimonio
     * Este método busca diretamente na tabela de patrimônios, não na tabela de coletas
     * @param descricao Descrição para buscar
     * @return Lista de objetos Coleta simulados com dados dos patrimônios encontrados
     */
    public List<Coleta> buscarPatrimoniosPorDescricao(String descricao) throws SQLException {
        if (descricao == null || descricao.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String sql = "SELECT DISTINCT p.descricao, p.categoria " +
                    "FROM tabela_patrimonio p " +
                    "WHERE LOWER(p.descricao) LIKE ? " +
                    "ORDER BY p.descricao";

        List<Coleta> resultados = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String termoBusca = "%" + descricao.trim().toLowerCase() + "%";
            stmt.setString(1, termoBusca);

            try (ResultSet rs = stmt.executeQuery()) {
                int idFicticio = 1; // Contador para IDs fictícios
                while (rs.next()) {
                    // Criar um objeto Coleta simplificado apenas com descrição e categoria
                    Coleta coleta = new Coleta();
                    
                    // Definir um ID fictício para evitar erros de acesso
                    coleta.setId(idFicticio++);
                    
                    // Usar campos de item sem etiqueta para armazenar dados do patrimônio
                    coleta.setDescricaoItemSemEtiqueta(rs.getString("descricao"));
                    coleta.setCategoriaItemSemEtiqueta(rs.getString("categoria"));
                    
                    // Definir como item sem etiqueta para compatibilidade com a interface
                    coleta.setSemEtiqueta(true);
                    
                    // Definir data atual como data de "coleta"
                    coleta.setDataColeta(new java.sql.Timestamp(System.currentTimeMillis()));
                    
                    // Definir coletor como "Sistema" para indicar busca automática
                    coleta.setNomeColetor("Sistema - Busca Patrimônio");
                    
                    resultados.add(coleta);
                }
            }
        }

        return resultados;
    }
    
    /**
     * Exclui um item sem patrimônio baseado na descrição e data/hora
     * @param descricao Descrição do item sem patrimônio
     * @param dataHora Data e hora formatada (dd/MM/yyyy HH:mm)
     * @param idInventario ID do inventário
     * @return true se a exclusão foi bem-sucedida
     */
    public boolean excluirItemSemPatrimonio(String descricao, String dataHora, int idInventario) throws SQLException {
        String sql = "DELETE FROM TABELA_COLETA " +
                    "WHERE SEM_ETIQUETA = true " +
                    "AND DESCRICAO_ITEM_SEM_ETIQUETA = ? " +
                    "AND DATE_FORMAT(DATA_COLETA, '%d/%m/%Y %H:%i') = ? " +
                    "AND ID_INVENTARIO = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, descricao);
            stmt.setString(2, dataHora);
            stmt.setInt(3, idInventario);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Busca um item sem patrimônio específico para obter seu ID
     * @param descricao Descrição do item
     * @param dataHora Data e hora formatada
     * @param idInventario ID do inventário
     * @return ID do item se encontrado, null caso contrário
     */
    public Integer buscarIdItemSemPatrimonio(String descricao, String dataHora, int idInventario) throws SQLException {
        String sql = "SELECT ID FROM TABELA_COLETA " +
                    "WHERE SEM_ETIQUETA = true " +
                    "AND DESCRICAO_ITEM_SEM_ETIQUETA = ? " +
                    "AND DATE_FORMAT(DATA_COLETA, '%d/%m/%Y %H:%i') = ? " +
                    "AND ID_INVENTARIO = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, descricao);
            stmt.setString(2, dataHora);
            stmt.setInt(3, idInventario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ID");
                }
            }
        }
        
        return null;
    }
    
    /**
     * Busca todos os itens sem patrimônio registrados no inventário ativo
     * @return Lista de coletas de itens sem patrimônio
     */
    public List<Coleta> buscarTodosItensSemPatrimonio() throws SQLException {
        String sql = "SELECT c.*, u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO " +
                    "FROM TABELA_COLETA c " +
                    "LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                    "LEFT JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID " +
                    "LEFT JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID " +
                    "WHERE c.SEM_ETIQUETA = true " +
                    "ORDER BY c.DATA_COLETA DESC";
        
        List<Coleta> coletas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    coletas.add(criarColetaFromResultSet(rs));
                }
            }
        }
        
        return coletas;
    }
    
    /**
     * Busca descrições únicas de itens sem patrimônio para seleção
     * @param termo Termo de busca para filtrar descrições
     * @return Lista de descrições únicas encontradas
     */
    public List<String> buscarDescricoesUnicasItensSemPatrimonio(String termo) throws SQLException {
        String sql = "SELECT DISTINCT DESCRICAO_ITEM_SEM_ETIQUETA " +
                    "FROM TABELA_COLETA " +
                    "WHERE SEM_ETIQUETA = true " +
                    "AND DESCRICAO_ITEM_SEM_ETIQUETA IS NOT NULL " +
                    "AND DESCRICAO_ITEM_SEM_ETIQUETA != '' ";
        
        if (termo != null && !termo.trim().isEmpty()) {
            sql += "AND UPPER(DESCRICAO_ITEM_SEM_ETIQUETA) LIKE UPPER(?) ";
        }
        
        sql += "ORDER BY DESCRICAO_ITEM_SEM_ETIQUETA";
        
        List<String> descricoes = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (termo != null && !termo.trim().isEmpty()) {
                stmt.setString(1, "%" + termo.trim() + "%");
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String descricao = rs.getString("DESCRICAO_ITEM_SEM_ETIQUETA");
                    if (descricao != null && !descricao.trim().isEmpty()) {
                        descricoes.add(descricao.trim());
                    }
                }
            }
        }
        
        return descricoes;
    }
    
    /**
     * Busca itens sem patrimônio por descrição exata para seleção
     * @param descricao Descrição exata do item
     * @return Lista de coletas com a descrição especificada
     */
    public List<Coleta> buscarItensSemPatrimonioPorDescricaoExata(String descricao) throws SQLException {
        String sql = "SELECT c.*, u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO " +
                    "FROM TABELA_COLETA c " +
                    "LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                    "LEFT JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID " +
                    "LEFT JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID " +
                    "WHERE c.SEM_ETIQUETA = true " +
                    "AND c.DESCRICAO_ITEM_SEM_ETIQUETA = ? " +
                    "ORDER BY c.DATA_COLETA DESC";
        
        List<Coleta> coletas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, descricao);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    coletas.add(criarColetaFromResultSet(rs));
                }
            }
        }
        
        return coletas;
    }
    
    /**
     * Busca itens sem patrimônio coletados em uma sala específica
     * @param idInventario ID do inventário
     * @param idSala ID da sala
     * @return Lista de coletas de itens sem patrimônio na sala
     */
    public List<Coleta> buscarItensSemPatrimonioPorSala(Integer idInventario, Integer idSala) throws SQLException {
        String sql = "SELECT c.*, u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO " +
                    "FROM TABELA_COLETA c " +
                    "LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                    "LEFT JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID " +
                    "LEFT JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID " +
                    "LEFT JOIN TABELA_SALA_INVENTARIO si ON c.ID_INVENTARIO = si.ID_INVENTARIO " +
                    "WHERE c.SEM_ETIQUETA = true " +
                    "AND c.ID_INVENTARIO = ? " +
                    "AND si.ID_SALA = ? " +
                    "ORDER BY c.DATA_COLETA DESC";
        
        List<Coleta> coletas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            stmt.setInt(2, idSala);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    coletas.add(criarColetaFromResultSet(rs));
                }
            }
        }
        
        return coletas;
    }
    
    /**
     * Busca coletas com etiqueta baseado na localização encontrada
     * @param localizacaoEncontrada A localização onde os itens foram encontrados
     * @return Lista de coletas com etiqueta encontradas na localização especificada
     * @throws SQLException Se ocorrer erro na consulta
     */
    public List<Coleta> buscarColetasComEtiquetaPorLocalizacaoEncontrada(String localizacaoEncontrada) throws SQLException {
        // Primeiro tenta busca exata
        List<Coleta> coletas = buscarColetasComEtiquetaPorLocalizacaoExata(localizacaoEncontrada);
        
        // Se não encontrou resultados, tenta busca flexível
        if (coletas.isEmpty()) {
            coletas = buscarColetasComEtiquetaPorLocalizacaoFlexivel(localizacaoEncontrada);
        }
        
        return coletas;
    }
    
    /**
     * Busca coletas com etiqueta baseado na localização encontrada (busca exata)
     * @param localizacaoEncontrada A localização onde os itens foram encontrados
     * @return Lista de coletas com etiqueta encontradas na localização especificada
     * @throws SQLException Se ocorrer erro na consulta
     */
    private List<Coleta> buscarColetasComEtiquetaPorLocalizacaoExata(String localizacaoEncontrada) throws SQLException {
        String sql = "SELECT c.*, p.NUMERO as NUMERO_PATRIMONIO, p.DESCRICAO as DESCRICAO_PATRIMONIO, " +
                    "u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO " +
                    "FROM TABELA_COLETA c " +
                    "LEFT JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID " +
                    "LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                    "LEFT JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID " +
                    "LEFT JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID " +
                    "WHERE c.LOCALIZACAO_ENCONTRADA = ? AND (c.SEM_ETIQUETA = false OR c.SEM_ETIQUETA IS NULL) " +
                    "ORDER BY c.DATA_COLETA DESC";
        
        List<Coleta> coletas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, localizacaoEncontrada);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    coletas.add(criarColetaFromResultSet(rs));
                }
            }
        }
        
        return coletas;
    }
    
    /**
     * Busca coletas com etiqueta baseado na localização encontrada (busca flexível)
     * Permite correspondências parciais para resolver incompatibilidades de nomenclatura
     * @param localizacaoEncontrada A localização onde os itens foram encontrados
     * @return Lista de coletas com etiqueta encontradas na localização especificada
     * @throws SQLException Se ocorrer erro na consulta
     */
    private List<Coleta> buscarColetasComEtiquetaPorLocalizacaoFlexivel(String localizacaoEncontrada) throws SQLException {
        String sql = "SELECT c.*, p.NUMERO as NUMERO_PATRIMONIO, p.DESCRICAO as DESCRICAO_PATRIMONIO, " +
                    "u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO " +
                    "FROM TABELA_COLETA c " +
                    "LEFT JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID " +
                    "LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                    "LEFT JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID " +
                    "LEFT JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID " +
                    "WHERE (c.LOCALIZACAO_ENCONTRADA LIKE ? OR ? LIKE '%' || c.LOCALIZACAO_ENCONTRADA || '%') " +
                    "AND (c.SEM_ETIQUETA = false OR c.SEM_ETIQUETA IS NULL) " +
                    "ORDER BY c.DATA_COLETA DESC";
        
        List<Coleta> coletas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + localizacaoEncontrada + "%");
            stmt.setString(2, localizacaoEncontrada);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    coletas.add(criarColetaFromResultSet(rs));
                }
            }
        }
        
        return coletas;
    }
}
