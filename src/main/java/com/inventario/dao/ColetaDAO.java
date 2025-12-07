package com.inventario.dao;

import com.inventario.model.Coleta;
import com.inventario.util.DatabaseConnection;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO para operações com Coleta
 * Gerencia as operações de banco de dados para coletas de inventário
 */
@Repository
public class ColetaDAO {
    
    /**
     * Detecta se está usando SQLite
     * NOTA: Para operações do desktop/servidor, sempre usar PostgreSQL
     */
    private boolean isSQLite() throws SQLException {
        // CORREÇÃO: Sempre retornar false para forçar uso do PostgreSQL
        // O modo offline SQLite é apenas para o app Android
        return false;
    }
    
    /**
     * Retorna o nome correto da tabela de coleta
     */
    private String getColetaTableName() throws SQLException {
        return "TABELA_COLETA";
    }
    
    /**
     * Retorna o nome correto da tabela de patrimônio
     */
    private String getPatrimonioTableName() throws SQLException {
        return "TABELA_PATRIMONIO";
    }
    
    /**
     * Retorna o nome correto da tabela de inventário
     */
    private String getInventarioTableName() throws SQLException {
        return "TABELA_INVENTARIO";
    }
    
    /**
     * Retorna o nome correto da tabela de usuário
     */
    private String getUsuarioTableName() throws SQLException {
        return "TABELA_USUARIO";
    }
    
    /**
     * Retorna o nome correto da tabela de participante
     */
    private String getParticipanteTableName() throws SQLException {
        return "TABELA_PARTICIPANTE_INVENTARIO";
    }
    
    /**
     * Retorna o nome correto da tabela de sala
     */
    private String getSalaTableName() throws SQLException {
        return "TABELA_SALA";
    }
    
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
        
        // Detectar tipo de banco e usar SQL apropriado
        boolean sqlite = isSQLite();
        String sql;
        
        if (sqlite) {
            // SQLite: usar tabela local_coleta com nomes de colunas minúsculos
            sql = "INSERT INTO local_coleta (id_inventario, id_patrimonio, id_participante, " +
                  "numero_patrimonio, data_coleta, localizacao_atual, localizacao_encontrada, " +
                  "situacao_encontrada, observacoes, foto_patrimonio, sem_etiqueta, " +
                  "descricao_sem_etiqueta) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        } else {
            // PostgreSQL: usar TABELA_COLETA com nomes de colunas MAIÚSCULOS
            sql = "INSERT INTO TABELA_COLETA (ID_INVENTARIO, ID_PATRIMONIO, ID_COLETOR, ID_PARTICIPANTE_INVENTARIO, " +
                  "DATA_COLETA, STATUS_COLETA, OBSERVACAO_COLETA, LOCALIZACAO_ATUAL, " +
                  "LOCALIZACAO_ENCONTRADA, ESTADO_ENCONTRADO, DIVERGENCIA, MOTIVO_DIVERGENCIA, " +
                  "LATITUDE, LONGITUDE, FOTO_PATRIMONIO, SEM_ETIQUETA, " +
                  "DESCRICAO_ITEM_SEM_ETIQUETA, CATEGORIA_ITEM_SEM_ETIQUETA) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            if (sqlite) {
                // SQLite: 12 parâmetros
                stmt.setInt(1, coleta.getIdInventario());
                // ID_PATRIMONIO pode ser NULL para itens sem etiqueta
                if (coleta.isSemEtiqueta() || coleta.getIdPatrimonio() == 0) {
                    stmt.setNull(2, Types.INTEGER);
                } else {
                    stmt.setInt(2, coleta.getIdPatrimonio());
                }
                stmt.setInt(3, coleta.getIdParticipanteInventario()); // id_participante
                stmt.setString(4, coleta.getNumeroPatrimonio()); // numero_patrimonio
                stmt.setTimestamp(5, coleta.getDataColeta());
                stmt.setString(6, coleta.getLocalizacaoAtual());
                stmt.setString(7, coleta.getLocalizacaoEncontrada());
                stmt.setString(8, coleta.getEstadoEncontrado()); // situacao_encontrada
                stmt.setString(9, coleta.getObservacaoColeta()); // observacoes
                stmt.setString(10, coleta.getFotoPatrimonio());
                stmt.setBoolean(11, coleta.isSemEtiqueta());
                stmt.setString(12, coleta.getDescricaoItemSemEtiqueta());
            } else {
                // PostgreSQL: 18 parâmetros
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
            }
            
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
        // CORRIGIDO: Usar TO_CHAR (PostgreSQL) ao invés de DATE_FORMAT (MySQL)
        String sqlBusca = "SELECT c.ID FROM TABELA_COLETA c " +
                         "LEFT JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID " +
                         "WHERE p.ID_SALA = ? AND p.NUMERO = ? AND c.ESTADO_ENCONTRADO = ? " +
                         "AND COALESCE(c.OBSERVACAO_COLETA, '') = ? " +
                         "AND TO_CHAR(c.DATA_COLETA, 'DD/MM/YYYY HH24:MI') = ?";
        
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
        String sql = "SELECT c.ID, c.ID_INVENTARIO, c.ID_PATRIMONIO, c.ID_COLETOR, c.ID_PARTICIPANTE_INVENTARIO, " +
                    "c.DATA_COLETA, c.STATUS_COLETA, c.OBSERVACAO_COLETA, c.LOCALIZACAO_ATUAL, " +
                    "c.LOCALIZACAO_ENCONTRADA, c.ESTADO_ENCONTRADO, c.DIVERGENCIA, c.MOTIVO_DIVERGENCIA, " +
                    "c.LATITUDE, c.LONGITUDE, c.FOTO_PATRIMONIO, c.SEM_ETIQUETA, " +
                    "c.DESCRICAO_ITEM_SEM_ETIQUETA, c.CATEGORIA_ITEM_SEM_ETIQUETA, " +
                    "p.NUMERO as NUMERO_PATRIMONIO, p.DESCRICAO as DESCRICAO_PATRIMONIO, " +
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
    
    /**
     * Busca TODAS as coletas do sistema
     * @return Lista com todas as coletas
     * @throws SQLException
     */
    public List<Coleta> buscarTodas() throws SQLException {
        String sql = "SELECT c.ID, c.ID_INVENTARIO, c.ID_PATRIMONIO, c.ID_COLETOR, c.ID_PARTICIPANTE_INVENTARIO, " +
                    "c.DATA_COLETA, c.STATUS_COLETA, c.OBSERVACAO_COLETA, c.LOCALIZACAO_ATUAL, " +
                    "c.LOCALIZACAO_ENCONTRADA, c.ESTADO_ENCONTRADO, c.DIVERGENCIA, c.MOTIVO_DIVERGENCIA, " +
                    "c.LATITUDE, c.LONGITUDE, c.FOTO_PATRIMONIO, c.SEM_ETIQUETA, " +
                    "c.DESCRICAO_ITEM_SEM_ETIQUETA, c.CATEGORIA_ITEM_SEM_ETIQUETA, " +
                    "p.NUMERO as NUMERO_PATRIMONIO, p.DESCRICAO as DESCRICAO_PATRIMONIO, " +
                    "u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO " +
                    "FROM TABELA_COLETA c " +
                    "LEFT JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID " +
                    "LEFT JOIN TABELA_USUARIO u ON c.ID_COLETOR = u.ID " +
                    "LEFT JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID " +
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
        boolean sqlite = isSQLite();
        
        String sql;
        if (sqlite) {
            // SQLite: Query com JOINs para buscar dados completos
            // IMPORTANTE: Incluir situacao_encontrada (campo de estado no SQLite)
            sql = "SELECT c.*, " +
                  "c.situacao_encontrada, " +
                  "p.numero as NUMERO_PATRIMONIO, " +
                  "p.descricao as DESCRICAO_PATRIMONIO, " +
                  "NULL as NOME_COLETOR, " +
                  "i.nome as DESCRICAO_INVENTARIO " +
                  "FROM local_coleta c " +
                  "LEFT JOIN local_patrimonio p ON c.id_patrimonio = p.id " +
                  "LEFT JOIN local_inventario i ON c.id_inventario = i.id " +
                  "WHERE p.id_sala = ? " +
                  "ORDER BY c.data_coleta DESC";
        } else {
            // PostgreSQL: usar TABELA_* e colunas MAIÚSCULAS
            sql = "SELECT c.*, p.NUMERO as NUMERO_PATRIMONIO, p.DESCRICAO as DESCRICAO_PATRIMONIO, " +
                  "u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO " +
                  "FROM " + getColetaTableName() + " c " +
                  "LEFT JOIN " + getPatrimonioTableName() + " p ON c.ID_PATRIMONIO = p.ID " +
                  "LEFT JOIN " + getParticipanteTableName() + " pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                  "LEFT JOIN " + getUsuarioTableName() + " u ON pi.ID_USUARIO = u.ID " +
                  "LEFT JOIN " + getInventarioTableName() + " i ON c.ID_INVENTARIO = i.ID " +
                  "WHERE p.ID_SALA = ? ORDER BY c.DATA_COLETA DESC";
        }
        
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
     * Busca coletas por número da sala (usado no ColetaFrame)
     * Busca EXATA: "CAE" encontra apenas "CAE" (não "CAE(IFMT - PDL)")
     * @param numeroSala Número/identificação da sala (ex: "CAE", "101", etc)
     * @return Lista de coletas encontradas na sala
     */
    public List<Coleta> buscarColetasPorNumeroSala(String numeroSala) throws SQLException {
        System.out.println("[DEBUG ColetaDAO] Buscando coletas por número de sala: '" + numeroSala + "'");
        
        boolean sqlite = isSQLite();
        
        String sql;
        if (sqlite) {
            // SQLite: Busca EXATA pela localizacao_encontrada
            // IMPORTANTE: Incluir situacao_encontrada (campo de estado no SQLite)
            sql = "SELECT c.*, " +
                  "c.situacao_encontrada, " +
                  "p.numero as NUMERO_PATRIMONIO, " +
                  "p.descricao as DESCRICAO_PATRIMONIO, " +
                  "NULL as NOME_COLETOR, " +
                  "i.nome as DESCRICAO_INVENTARIO " +
                  "FROM local_coleta c " +
                  "LEFT JOIN local_patrimonio p ON c.id_patrimonio = p.id " +
                  "LEFT JOIN local_inventario i ON c.id_inventario = i.id " +
                  "WHERE c.localizacao_encontrada = ? " +
                  "ORDER BY c.data_coleta DESC";
        } else {
            // PostgreSQL: Busca EXATA pela LOCALIZACAO_ENCONTRADA
            sql = "SELECT c.*, p.NUMERO as NUMERO_PATRIMONIO, p.DESCRICAO as DESCRICAO_PATRIMONIO, " +
                  "u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO " +
                  "FROM " + getColetaTableName() + " c " +
                  "LEFT JOIN " + getPatrimonioTableName() + " p ON c.ID_PATRIMONIO = p.ID " +
                  "LEFT JOIN " + getParticipanteTableName() + " pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                  "LEFT JOIN " + getUsuarioTableName() + " u ON pi.ID_USUARIO = u.ID " +
                  "LEFT JOIN " + getInventarioTableName() + " i ON c.ID_INVENTARIO = i.ID " +
                  "WHERE c.LOCALIZACAO_ENCONTRADA = ? " +
                  "ORDER BY c.DATA_COLETA DESC";
        }
        
        List<Coleta> coletas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Busca EXATA: "CAE" encontra apenas "CAE"
            stmt.setString(1, numeroSala);
            
            System.out.println("[DEBUG ColetaDAO] Buscando com valor EXATO: '" + numeroSala + "'");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    coletas.add(criarColetaFromResultSet(rs));
                }
            }
        }
        
        System.out.println("[DEBUG ColetaDAO] Encontradas " + coletas.size() + " coletas para sala '" + numeroSala + "'");
        return coletas;
    }
    
    /**
     * Busca TODAS as coletas (útil para visualização geral)
     * @return Lista de todas as coletas
     * @throws SQLException Se ocorrer erro na consulta
     */
    public List<Coleta> buscarTodasColetas() throws SQLException {
        boolean sqlite = isSQLite();
        
        String sql;
        if (sqlite) {
            // SQLite: usar tabelas local_* e colunas minúsculas
            // IMPORTANTE: Incluir situacao_encontrada (campo de estado no SQLite)
            sql = "SELECT c.*, c.situacao_encontrada, p.numero as NUMERO_PATRIMONIO, p.descricao as DESCRICAO_PATRIMONIO, " +
                  "NULL as NOME_COLETOR, i.nome as DESCRICAO_INVENTARIO, s.nome as NOME_SALA " +
                  "FROM " + getColetaTableName() + " c " +
                  "LEFT JOIN " + getPatrimonioTableName() + " p ON c.id_patrimonio = p.id " +
                  "LEFT JOIN " + getInventarioTableName() + " i ON c.id_inventario = i.id " +
                  "LEFT JOIN " + getSalaTableName() + " s ON p.id_sala = s.id " +
                  "ORDER BY c.data_coleta DESC";
        } else {
            // PostgreSQL: usar TABELA_* e colunas MAIÚSCULAS
            sql = "SELECT c.*, p.NUMERO as NUMERO_PATRIMONIO, p.DESCRICAO as DESCRICAO_PATRIMONIO, " +
                  "u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO, s.NOME as NOME_SALA " +
                  "FROM " + getColetaTableName() + " c " +
                  "LEFT JOIN " + getPatrimonioTableName() + " p ON c.ID_PATRIMONIO = p.ID " +
                  "LEFT JOIN " + getParticipanteTableName() + " pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                  "LEFT JOIN " + getUsuarioTableName() + " u ON pi.ID_USUARIO = u.ID " +
                  "LEFT JOIN " + getInventarioTableName() + " i ON c.ID_INVENTARIO = i.ID " +
                  "LEFT JOIN " + getSalaTableName() + " s ON p.ID_SALA = s.ID " +
                  "ORDER BY c.DATA_COLETA DESC";
        }
        
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
     * Busca coletas por sala e inventário (mais eficiente - filtro no banco)
     * @param idSala ID da sala
     * @param idInventario ID do inventário
     * @return Lista de coletas da sala no inventário especificado
     * @throws SQLException Se ocorrer erro na consulta
     */
    public List<Coleta> buscarColetasPorSalaEInventario(int idSala, int idInventario) throws SQLException {
        String sql = "SELECT c.*, p.NUMERO as NUMERO_PATRIMONIO, p.DESCRICAO as DESCRICAO_PATRIMONIO, " +
                    "u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO " +
                    "FROM TABELA_COLETA c " +
                    "LEFT JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID " +
                    "LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                    "LEFT JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID " +
                    "LEFT JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID " +
                    "WHERE p.ID_SALA = ? AND c.ID_INVENTARIO = ? " +
                    "ORDER BY c.DATA_COLETA DESC";
        
        List<Coleta> coletas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idSala);
            stmt.setInt(2, idInventario);
            
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
        boolean sqlite = isSQLite();
        
        String sql;
        if (sqlite) {
            sql = "SELECT c.*, NULL as NOME_COLETOR, i.nome as DESCRICAO_INVENTARIO " +
                  "FROM " + getColetaTableName() + " c " +
                  "LEFT JOIN " + getInventarioTableName() + " i ON c.id_inventario = i.id " +
                  "WHERE c.sem_etiqueta = 1 " +
                  "AND c.localizacao_encontrada = ? " +
                  "ORDER BY c.data_coleta DESC";
        } else {
            sql = "SELECT c.*, u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO " +
                  "FROM " + getColetaTableName() + " c " +
                  "LEFT JOIN " + getParticipanteTableName() + " pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                  "LEFT JOIN " + getUsuarioTableName() + " u ON pi.ID_USUARIO = u.ID " +
                  "LEFT JOIN " + getInventarioTableName() + " i ON c.ID_INVENTARIO = i.ID " +
                  "WHERE c.SEM_ETIQUETA = true " +
                  "AND c.LOCALIZACAO_ENCONTRADA = ? " +
                  "ORDER BY c.DATA_COLETA DESC";
        }
        
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
        boolean sqlite = isSQLite();
        
        String sql;
        if (sqlite) {
            // SQLite não tem campo divergencia, retornar lista vazia
            return new ArrayList<>();
        } else {
            sql = "SELECT c.*, p.NUMERO as NUMERO_PATRIMONIO, p.DESCRICAO as DESCRICAO_PATRIMONIO, " +
                  "u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO " +
                  "FROM " + getColetaTableName() + " c " +
                  "LEFT JOIN " + getPatrimonioTableName() + " p ON c.ID_PATRIMONIO = p.ID " +
                  "LEFT JOIN " + getParticipanteTableName() + " pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                  "LEFT JOIN " + getUsuarioTableName() + " u ON pi.ID_USUARIO = u.ID " +
                  "LEFT JOIN " + getInventarioTableName() + " i ON c.ID_INVENTARIO = i.ID " +
                  "WHERE c.DIVERGENCIA = true ORDER BY c.DATA_COLETA DESC";
        }
        
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
     * Conta o número de divergências em um inventário
     * @param idInventario ID do inventário
     * @return Número de coletas com divergência
     */
    public int contarDivergenciasPorInventario(int idInventario) throws SQLException {
        boolean sqlite = isSQLite();
        
        String sql;
        if (sqlite) {
            sql = "SELECT COUNT(*) FROM " + getColetaTableName() + " WHERE id_inventario = ? AND divergencia = 1";
        } else {
            sql = "SELECT COUNT(*) FROM " + getColetaTableName() + " WHERE ID_INVENTARIO = ? AND DIVERGENCIA = true";
        }
        
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
     * Conta o número de coletores ativos (usuários distintos) em um inventário
     * @param idInventario ID do inventário
     * @return Número de usuários que fizeram coletas
     */
    public int contarColetoresAtivosPorInventario(int idInventario) throws SQLException {
        boolean sqlite = isSQLite();
        
        String sql;
        if (sqlite) {
            sql = "SELECT COUNT(DISTINCT pi.id_usuario) " +
                  "FROM " + getColetaTableName() + " c " +
                  "INNER JOIN " + getParticipanteTableName() + " pi ON c.id_participante_inventario = pi.id_participante " +
                  "WHERE c.id_inventario = ? AND pi.id_usuario IS NOT NULL";
        } else {
            sql = "SELECT COUNT(DISTINCT pi.ID_USUARIO) " +
                  "FROM " + getColetaTableName() + " c " +
                  "INNER JOIN " + getParticipanteTableName() + " pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                  "WHERE c.ID_INVENTARIO = ? AND pi.ID_USUARIO IS NOT NULL";
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0 ? count : 1; // Retorna pelo menos 1 se houver coletas
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Verifica se um patrimônio foi coletado em um inventário específico
     * @param idPatrimonio ID do patrimônio
     * @param idInventario ID do inventário
     * @return true se foi coletado, false caso contrário
     */
    public boolean verificarSePatrimonioFoiColetado(int idPatrimonio, int idInventario) throws SQLException {
        boolean sqlite = isSQLite();
        
        String sql;
        if (sqlite) {
            // SQLite: local_coleta não tem coluna status_coleta, apenas verifica se existe
            sql = "SELECT COUNT(*) FROM " + getColetaTableName() + " " +
                  "WHERE id_patrimonio = ? AND id_inventario = ?";
        } else {
            sql = "SELECT COUNT(*) FROM " + getColetaTableName() + " " +
                  "WHERE ID_PATRIMONIO = ? AND ID_INVENTARIO = ? AND STATUS_COLETA = 'COLETADO'";
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idPatrimonio);
            stmt.setInt(2, idInventario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Busca a data de coleta de um patrimônio em um inventário específico
     * @param idPatrimonio ID do patrimônio
     * @param idInventario ID do inventário
     * @return Data da coleta ou null se não foi coletado
     */
    public java.sql.Timestamp buscarDataColetaPatrimonio(int idPatrimonio, int idInventario) throws SQLException {
        boolean sqlite = isSQLite();
        
        String sql;
        if (sqlite) {
            // SQLite: local_coleta não tem coluna status_coleta
            sql = "SELECT data_coleta FROM " + getColetaTableName() + " " +
                  "WHERE id_patrimonio = ? AND id_inventario = ? " +
                  "ORDER BY data_coleta DESC LIMIT 1";
        } else {
            sql = "SELECT DATA_COLETA FROM " + getColetaTableName() + " " +
                  "WHERE ID_PATRIMONIO = ? AND ID_INVENTARIO = ? AND STATUS_COLETA = 'COLETADO' " +
                  "ORDER BY DATA_COLETA DESC LIMIT 1";
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idPatrimonio);
            stmt.setInt(2, idInventario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    if (sqlite) {
                        // ✅ CORRIGIDO: Usar método seguro para ler timestamp do SQLite
                        return com.inventario.util.DateFormatUtils.getTimestampSafe(rs, "data_coleta");
                    } else {
                        return rs.getTimestamp("DATA_COLETA");
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Busca o histórico de coletas de um patrimônio (todos os inventários)
     * @param idPatrimonio ID do patrimônio
     * @return Lista com histórico de coletas
     */
    public List<Map<String, Object>> buscarHistoricoColetasPatrimonio(int idPatrimonio) throws SQLException {
        String sql = "SELECT c.ID, c.DATA_COLETA, c.STATUS_COLETA, c.OBSERVACAO_COLETA, " +
                    "c.LOCALIZACAO_ENCONTRADA, c.ESTADO_ENCONTRADO, c.DIVERGENCIA, " +
                    "i.ID as INVENTARIO_ID, i.NOME as INVENTARIO_NOME, " +
                    "u.NOME_COMPLETO as COLETOR_NOME " +
                    "FROM TABELA_COLETA c " +
                    "INNER JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID " +
                    "LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                    "LEFT JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID " +
                    "WHERE c.ID_PATRIMONIO = ? " +
                    "ORDER BY c.DATA_COLETA DESC";
        
        List<Map<String, Object>> historico = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idPatrimonio);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> coleta = new java.util.LinkedHashMap<>();
                    coleta.put("id", rs.getInt("ID"));
                    coleta.put("dataColeta", rs.getTimestamp("DATA_COLETA"));
                    coleta.put("statusColeta", rs.getString("STATUS_COLETA"));
                    coleta.put("observacao", rs.getString("OBSERVACAO_COLETA"));
                    coleta.put("localizacaoEncontrada", rs.getString("LOCALIZACAO_ENCONTRADA"));
                    coleta.put("estadoEncontrado", rs.getString("ESTADO_ENCONTRADO"));
                    coleta.put("divergencia", rs.getBoolean("DIVERGENCIA"));
                    coleta.put("inventarioId", rs.getInt("INVENTARIO_ID"));
                    coleta.put("inventarioNome", rs.getString("INVENTARIO_NOME"));
                    coleta.put("coletorNome", rs.getString("COLETOR_NOME"));
                    historico.add(coleta);
                }
            }
        }
        
        return historico;
    }
    
    public boolean coletaExiste(int idInventario, int idPatrimonio) throws SQLException {
        boolean sqlite = isSQLite();
        
        String sql;
        if (sqlite) {
            // SQLite: local_coleta não tem coluna status_coleta, apenas verifica se existe
            sql = "SELECT COUNT(*) FROM " + getColetaTableName() + " " +
                  "WHERE id_inventario = ? AND id_patrimonio = ?";
        } else {
            sql = "SELECT COUNT(*) FROM " + getColetaTableName() + " " +
                  "WHERE ID_INVENTARIO = ? AND ID_PATRIMONIO = ? AND STATUS_COLETA = 'COLETADO'";
        }
        
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
    
    /**
     * Busca coleta existente por inventário e patrimônio
     * Retorna a coleta com dados do coletor para tratamento de duplicatas
     * 
     * @param idInventario ID do inventário
     * @param idPatrimonio ID do patrimônio
     * @return Coleta existente com dados do coletor ou null se não existir
     */
    public Coleta buscarColetaExistente(int idInventario, int idPatrimonio) throws SQLException {
        boolean sqlite = isSQLite();
        
        String sql;
        if (sqlite) {
            sql = "SELECT c.*, u.nome_completo as nome_coletor " +
                  "FROM " + getColetaTableName() + " c " +
                  "LEFT JOIN " + getParticipanteTableName() + " pi ON c.id_participante_inventario = pi.id_participante " +
                  "LEFT JOIN tabela_usuario u ON pi.id_usuario = u.id " +
                  "WHERE c.id_inventario = ? AND c.id_patrimonio = ? " +
                  "LIMIT 1";
        } else {
            sql = "SELECT c.*, u.nome_completo as nome_coletor " +
                  "FROM " + getColetaTableName() + " c " +
                  "LEFT JOIN " + getParticipanteTableName() + " pi ON c.id_participante_inventario = pi.id_participante " +
                  "LEFT JOIN tabela_usuario u ON pi.id_usuario = u.id " +
                  "WHERE c.id_inventario = ? AND c.id_patrimonio = ? AND c.status_coleta = 'COLETADO' " +
                  "LIMIT 1";
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            stmt.setInt(2, idPatrimonio);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Coleta coleta = new Coleta();
                    coleta.setId(rs.getInt("id"));
                    coleta.setIdInventario(rs.getInt("id_inventario"));
                    coleta.setIdPatrimonio(rs.getInt("id_patrimonio"));
                    coleta.setDataColeta(rs.getTimestamp("data_coleta"));
                    
                    // Nome do coletor (do JOIN)
                    String nomeColetor = rs.getString("nome_coletor");
                    coleta.setNomeColetor(nomeColetor);
                    
                    // Campos adicionais se existirem
                    try {
                        coleta.setStatusColeta(rs.getString("status_coleta"));
                    } catch (SQLException e) {
                        coleta.setStatusColeta("COLETADO");
                    }
                    
                    try {
                        coleta.setIdColetor(rs.getInt("id_coletor"));
                    } catch (SQLException e) {
                        // Campo pode não existir em SQLite
                    }
                    
                    return coleta;
                }
            }
        }
        
        return null;
    }
    
    public int contarColetasPorInventario(int idInventario) throws SQLException {
        boolean sqlite = isSQLite();
        
        String sql;
        if (sqlite) {
            sql = "SELECT COUNT(*) FROM " + getColetaTableName() + " WHERE id_inventario = ?";
        } else {
            sql = "SELECT COUNT(*)::INTEGER FROM " + getColetaTableName() + " WHERE ID_INVENTARIO = ?";
        }
        
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
        boolean sqlite = isSQLite();
        
        String sql;
        if (sqlite) {
            sql = "SELECT COUNT(*) FROM " + getColetaTableName() + " c " +
                  "LEFT JOIN " + getParticipanteTableName() + " pi ON c.id_participante_inventario = pi.id_participante " +
                  "WHERE pi.id_usuario = ?";
        } else {
            sql = "SELECT COUNT(*) FROM " + getColetaTableName() + " c " +
                  "LEFT JOIN " + getParticipanteTableName() + " pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                  "WHERE pi.ID_USUARIO = ?";
        }
        
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
        boolean sqlite = isSQLite();
        
        String sql;
        if (sqlite) {
            sql = "SELECT c.*, NULL as NOME_COLETOR, i.nome as DESCRICAO_INVENTARIO " +
                  "FROM " + getColetaTableName() + " c " +
                  "LEFT JOIN " + getInventarioTableName() + " i ON c.id_inventario = i.id " +
                  "WHERE c.sem_etiqueta = 1 ORDER BY c.data_coleta DESC";
        } else {
            sql = "SELECT c.*, u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO " +
                  "FROM " + getColetaTableName() + " c " +
                  "LEFT JOIN " + getParticipanteTableName() + " pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                  "LEFT JOIN " + getUsuarioTableName() + " u ON pi.ID_USUARIO = u.ID " +
                  "LEFT JOIN " + getInventarioTableName() + " i ON c.ID_INVENTARIO = i.ID " +
                  "WHERE c.SEM_ETIQUETA = true ORDER BY c.DATA_COLETA DESC";
        }
        
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
        boolean sqlite = isSQLite();
        
        String sql;
        if (sqlite) {
            sql = "SELECT c.*, NULL as NOME_COLETOR, i.nome as DESCRICAO_INVENTARIO " +
                  "FROM " + getColetaTableName() + " c " +
                  "LEFT JOIN " + getInventarioTableName() + " i ON c.id_inventario = i.id " +
                  "WHERE c.sem_etiqueta = 1 AND c.id_inventario = ? ORDER BY c.data_coleta DESC";
        } else {
            sql = "SELECT c.*, u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO " +
                  "FROM " + getColetaTableName() + " c " +
                  "LEFT JOIN " + getParticipanteTableName() + " pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                  "LEFT JOIN " + getUsuarioTableName() + " u ON pi.ID_USUARIO = u.ID " +
                  "LEFT JOIN " + getInventarioTableName() + " i ON c.ID_INVENTARIO = i.ID " +
                  "WHERE c.SEM_ETIQUETA = true AND c.ID_INVENTARIO = ? ORDER BY c.DATA_COLETA DESC";
        }
        
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
        boolean sqlite = isSQLite();
        
        String sql;
        if (sqlite) {
            // SQLite não tem campo categoria, retornar lista vazia
            return new ArrayList<>();
        } else {
            sql = "SELECT c.*, u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO " +
                  "FROM " + getColetaTableName() + " c " +
                  "LEFT JOIN " + getParticipanteTableName() + " pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                  "LEFT JOIN " + getUsuarioTableName() + " u ON pi.ID_USUARIO = u.ID " +
                  "LEFT JOIN " + getInventarioTableName() + " i ON c.ID_INVENTARIO = i.ID " +
                  "WHERE c.SEM_ETIQUETA = true AND c.CATEGORIA_ITEM_SEM_ETIQUETA = ? ORDER BY c.DATA_COLETA DESC";
        }
        
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
        boolean sqlite = isSQLite();
        
        String sql;
        if (sqlite) {
            sql = "SELECT COUNT(*) FROM " + getColetaTableName() + " WHERE sem_etiqueta = 1 AND id_inventario = ?";
        } else {
            sql = "SELECT COUNT(*) FROM " + getColetaTableName() + " WHERE SEM_ETIQUETA = true AND ID_INVENTARIO = ?";
        }
        
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
     * Retorna uma lista com descrições agrupadas e contagem de ocorrências (todos os inventários)
     * 
     * @return Lista de arrays com [descrição, categoria, quantidade, última_coleta]
     * @throws SQLException Se ocorrer erro na consulta
     */
    public List<Object[]> agruparItensSemEtiquetaPorDescricao() throws SQLException {
        boolean sqlite = isSQLite();
        
        String sql;
        if (sqlite) {
            // SQLite: usar nomes minúsculos e sintaxe compatível
            sql = "SELECT " +
                  "    TRIM(c.descricao_sem_etiqueta) as descricao, " +
                  "    'OUTROS' as categoria, " +
                  "    COUNT(*) as quantidade, " +
                  "    MAX(c.data_coleta) as ultima_coleta " +
                  "FROM " + getColetaTableName() + " c " +
                  "WHERE c.sem_etiqueta = 1 " +
                  "    AND c.descricao_sem_etiqueta IS NOT NULL " +
                  "    AND TRIM(c.descricao_sem_etiqueta) != '' " +
                  "GROUP BY TRIM(c.descricao_sem_etiqueta) " +
                  "ORDER BY quantidade DESC, descricao ASC";
        } else {
            // PostgreSQL: usar nomes maiúsculos
            sql = "SELECT " +
                  "    TRIM(c.DESCRICAO_ITEM_SEM_ETIQUETA) as descricao, " +
                  "    c.CATEGORIA_ITEM_SEM_ETIQUETA as categoria, " +
                  "    COUNT(*) as quantidade, " +
                  "    MAX(c.DATA_COLETA) as ultima_coleta " +
                  "FROM " + getColetaTableName() + " c " +
                  "WHERE c.SEM_ETIQUETA = true " +
                  "    AND c.DESCRICAO_ITEM_SEM_ETIQUETA IS NOT NULL " +
                  "    AND TRIM(c.DESCRICAO_ITEM_SEM_ETIQUETA) != '' " +
                  "GROUP BY TRIM(c.DESCRICAO_ITEM_SEM_ETIQUETA), c.CATEGORIA_ITEM_SEM_ETIQUETA " +
                  "ORDER BY quantidade DESC, descricao ASC";
        }
        
        List<Object[]> resultados = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Object[] item = new Object[4];
                item[0] = rs.getString("descricao");
                item[1] = rs.getString("categoria");
                item[2] = rs.getInt("quantidade");
                item[3] = rs.getTimestamp("ultima_coleta");
                resultados.add(item);
            }
        }
        
        return resultados;
    }
    
    /**
     * Agrupa itens sem etiqueta por descrições similares para um inventário específico
     * Retorna descrições agrupadas com contagem de ocorrências
     * 
     * @param idInventario ID do inventário
     * @return Lista de mapas com descrição, categoria, quantidade e última data de coleta
     * @throws SQLException Se ocorrer erro na consulta
     */
    public List<Map<String, Object>> agruparItensSemEtiquetaPorDescricao(int idInventario) throws SQLException {
        boolean sqlite = isSQLite();
        
        String sql;
        if (sqlite) {
            // SQLite: GROUP_CONCAT ao invés de STRING_AGG
            sql = "SELECT " +
                  "    TRIM(c.descricao_sem_etiqueta) as descricao, " +
                  "    'OUTROS' as categoria, " +
                  "    COUNT(*) as quantidade, " +
                  "    MAX(c.data_coleta) as ultima_coleta, " +
                  "    GROUP_CONCAT(DISTINCT c.localizacao_encontrada, ', ') as localizacoes " +
                  "FROM " + getColetaTableName() + " c " +
                  "WHERE c.sem_etiqueta = 1 " +
                  "    AND c.id_inventario = ? " +
                  "    AND c.descricao_sem_etiqueta IS NOT NULL " +
                  "    AND TRIM(c.descricao_sem_etiqueta) != '' " +
                  "GROUP BY TRIM(c.descricao_sem_etiqueta) " +
                  "ORDER BY quantidade DESC, descricao ASC";
        } else {
            sql = "SELECT " +
                  "    TRIM(c.DESCRICAO_ITEM_SEM_ETIQUETA) as descricao, " +
                  "    c.CATEGORIA_ITEM_SEM_ETIQUETA as categoria, " +
                  "    COUNT(*) as quantidade, " +
                  "    MAX(c.DATA_COLETA) as ultima_coleta, " +
                  "    STRING_AGG(DISTINCT c.LOCALIZACAO_ENCONTRADA, ', ' ORDER BY c.LOCALIZACAO_ENCONTRADA) as localizacoes " +
                  "FROM " + getColetaTableName() + " c " +
                  "WHERE c.SEM_ETIQUETA = true " +
                  "    AND c.ID_INVENTARIO = ? " +
                  "    AND c.DESCRICAO_ITEM_SEM_ETIQUETA IS NOT NULL " +
                  "    AND TRIM(c.DESCRICAO_ITEM_SEM_ETIQUETA) != '' " +
                  "GROUP BY TRIM(c.DESCRICAO_ITEM_SEM_ETIQUETA), c.CATEGORIA_ITEM_SEM_ETIQUETA " +
                  "ORDER BY quantidade DESC, descricao ASC";
        }
        
        List<Map<String, Object>> resultados = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new java.util.LinkedHashMap<>();
                    item.put("descricao", rs.getString("descricao"));
                    item.put("categoria", rs.getString("categoria"));
                    item.put("quantidade", rs.getInt("quantidade"));
                    item.put("ultimaColeta", rs.getTimestamp("ultima_coleta"));
                    item.put("localizacoes", rs.getString("localizacoes"));
                    resultados.add(item);
                }
            }
        }
        
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
        
        // ID: compatível em ambos (PostgreSQL: ID, SQLite: id)
        try {
            coleta.setId(rs.getInt("ID"));
        } catch (SQLException e) {
            coleta.setId(rs.getInt("id"));
        }
        
        // ID_INVENTARIO: compatível em ambos
        try {
            coleta.setIdInventario(rs.getInt("ID_INVENTARIO"));
        } catch (SQLException e) {
            coleta.setIdInventario(rs.getInt("id_inventario"));
        }
        
        // ID_PATRIMONIO: compatível em ambos
        try {
            coleta.setIdPatrimonio(rs.getInt("ID_PATRIMONIO"));
        } catch (SQLException e) {
            coleta.setIdPatrimonio(rs.getInt("id_patrimonio"));
        }
        
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
        
        // Tentar ler ID_COLETOR primeiro (campo atual)
        int idColetor = 0;
        try {
            idColetor = rs.getInt("ID_COLETOR");
            if (!rs.wasNull() && idColetor > 0) {
                coleta.setIdColetor(idColetor);
                System.out.println("[DEBUG ColetaDAO] ID_COLETOR lido: " + idColetor + " para coleta ID: " + coleta.getId());
            }
        } catch (SQLException e) {
            // Se ID_COLETOR não existir, tentar ID_USUARIO (compatibilidade)
            try {
                int idUsuario = rs.getInt("ID_USUARIO");
                if (!rs.wasNull() && idUsuario > 0) {
                    coleta.setIdColetor(idUsuario);
                    System.out.println("[DEBUG ColetaDAO] ID_USUARIO lido: " + idUsuario + " para coleta ID: " + coleta.getId());
                }
            } catch (SQLException e2) {
                System.out.println("[DEBUG ColetaDAO] AVISO: Nenhum ID de coletor encontrado para coleta ID: " + coleta.getId());
            }
        }
        // DATA_COLETA: compatível em ambos
        try {
            System.out.println("=== DEBUG TIMESTAMP: criarColetaFromResultSet ===");
            System.out.println("DEBUG TIMESTAMP: Lendo DATA_COLETA do ResultSet...");
            
            Timestamp dataColeta;
            try {
                dataColeta = rs.getTimestamp("DATA_COLETA");
            } catch (SQLException e) {
                dataColeta = rs.getTimestamp("data_coleta");
            }
            
            System.out.println("DEBUG TIMESTAMP: Timestamp lido: " + dataColeta);
            System.out.println("DEBUG TIMESTAMP: Timestamp (class): " + 
                (dataColeta != null ? dataColeta.getClass().getName() : "null"));
            System.out.println("DEBUG TIMESTAMP: Timestamp (time): " + 
                (dataColeta != null ? dataColeta.getTime() : "null"));
            
            coleta.setDataColeta(dataColeta);
            System.out.println("DEBUG TIMESTAMP: Timestamp setado na coleta");
            
        } catch (SQLException e) {
            System.err.println("DEBUG TIMESTAMP: ERRO ao ler DATA_COLETA: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        
        // STATUS_COLETA: compatível em ambos
        try {
            coleta.setStatusColeta(rs.getString("STATUS_COLETA"));
        } catch (SQLException e) {
            try {
                coleta.setStatusColeta(rs.getString("status_coleta"));
            } catch (SQLException e2) {
                coleta.setStatusColeta("COLETADO");
            }
        }
        
        // OBSERVACAO_COLETA: compatível em ambos
        try {
            coleta.setObservacaoColeta(rs.getString("OBSERVACAO_COLETA"));
        } catch (SQLException e) {
            try {
                coleta.setObservacaoColeta(rs.getString("observacao_coleta"));
            } catch (SQLException e2) {
                coleta.setObservacaoColeta(null);
            }
        }
        
        // LOCALIZACAO_ATUAL: compatível em ambos
        try {
            coleta.setLocalizacaoAtual(rs.getString("LOCALIZACAO_ATUAL"));
        } catch (SQLException e) {
            try {
                coleta.setLocalizacaoAtual(rs.getString("localizacao_atual"));
            } catch (SQLException e2) {
                coleta.setLocalizacaoAtual(null);
            }
        }
        
        // LOCALIZACAO_ENCONTRADA: compatível em ambos
        try {
            coleta.setLocalizacaoEncontrada(rs.getString("LOCALIZACAO_ENCONTRADA"));
        } catch (SQLException e) {
            try {
                coleta.setLocalizacaoEncontrada(rs.getString("localizacao_encontrada"));
            } catch (SQLException e2) {
                coleta.setLocalizacaoEncontrada(null);
            }
        }
        
        // ESTADO_ENCONTRADO: PostgreSQL usa ESTADO_ENCONTRADO, SQLite usa situacao_encontrada
        try {
            coleta.setEstadoEncontrado(rs.getString("ESTADO_ENCONTRADO"));
        } catch (SQLException e) {
            try {
                coleta.setEstadoEncontrado(rs.getString("estado_encontrado"));
            } catch (SQLException e2) {
                try {
                    // SQLite usa situacao_encontrada
                    coleta.setEstadoEncontrado(rs.getString("situacao_encontrada"));
                } catch (SQLException e3) {
                    coleta.setEstadoEncontrado(null);
                }
            }
        }
        
        // DIVERGENCIA: compatível em ambos
        try {
            coleta.setDivergencia(rs.getBoolean("DIVERGENCIA"));
        } catch (SQLException e) {
            try {
                coleta.setDivergencia(rs.getBoolean("divergencia"));
            } catch (SQLException e2) {
                coleta.setDivergencia(false);
            }
        }
        
        // MOTIVO_DIVERGENCIA: compatível em ambos
        try {
            coleta.setMotivoDivergencia(rs.getString("MOTIVO_DIVERGENCIA"));
        } catch (SQLException e) {
            try {
                coleta.setMotivoDivergencia(rs.getString("motivo_divergencia"));
            } catch (SQLException e2) {
                coleta.setMotivoDivergencia(null);
            }
        }
        
        // LATITUDE: compatível em ambos
        try {
            coleta.setLatitude(rs.getBigDecimal("LATITUDE"));
        } catch (SQLException e) {
            try {
                coleta.setLatitude(rs.getBigDecimal("latitude"));
            } catch (SQLException e2) {
                coleta.setLatitude(null);
            }
        }
        
        // LONGITUDE: compatível em ambos
        try {
            coleta.setLongitude(rs.getBigDecimal("LONGITUDE"));
        } catch (SQLException e) {
            try {
                coleta.setLongitude(rs.getBigDecimal("longitude"));
            } catch (SQLException e2) {
                coleta.setLongitude(null);
            }
        }
        
        // FOTO_PATRIMONIO: compatível em ambos
        try {
            coleta.setFotoPatrimonio(rs.getString("FOTO_PATRIMONIO"));
        } catch (SQLException e) {
            try {
                coleta.setFotoPatrimonio(rs.getString("foto_patrimonio"));
            } catch (SQLException e2) {
                coleta.setFotoPatrimonio(null);
            }
        }
        
        // SEM_ETIQUETA: compatível em ambos
        try {
            coleta.setSemEtiqueta(rs.getBoolean("SEM_ETIQUETA"));
        } catch (SQLException e) {
            try {
                coleta.setSemEtiqueta(rs.getBoolean("sem_etiqueta"));
            } catch (SQLException e2) {
                coleta.setSemEtiqueta(false);
            }
        }
        
        // DESCRICAO_ITEM_SEM_ETIQUETA: compatível em ambos
        try {
            coleta.setDescricaoItemSemEtiqueta(rs.getString("DESCRICAO_ITEM_SEM_ETIQUETA"));
        } catch (SQLException e) {
            try {
                coleta.setDescricaoItemSemEtiqueta(rs.getString("descricao_item_sem_etiqueta"));
            } catch (SQLException e2) {
                coleta.setDescricaoItemSemEtiqueta(null);
            }
        }
        
        // CATEGORIA_ITEM_SEM_ETIQUETA: compatível em ambos
        try {
            coleta.setCategoriaItemSemEtiqueta(rs.getString("CATEGORIA_ITEM_SEM_ETIQUETA"));
        } catch (SQLException e) {
            try {
                coleta.setCategoriaItemSemEtiqueta(rs.getString("categoria_item_sem_etiqueta"));
            } catch (SQLException e2) {
                coleta.setCategoriaItemSemEtiqueta(null);
            }
        }
        
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
        
        // CORREÇÃO 02/12/2025: Ler NOME_SALA do JOIN para exibição no app Android
        // Setar nomeSala diretamente (campo separado de localizacaoAtual)
        try {
            String nomeSala = rs.getString("NOME_SALA");
            coleta.setNomeSala(nomeSala);
            
            // Se localizacaoAtual estiver vazio, usar nomeSala como fallback
            if ((coleta.getLocalizacaoAtual() == null || coleta.getLocalizacaoAtual().isEmpty()) 
                && nomeSala != null && !nomeSala.isEmpty()) {
                coleta.setLocalizacaoAtual(nomeSala);
            }
        } catch (SQLException e) {
            // Campo não existe na query, ignorar
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
        List<Coleta> coletas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Detectar tipo de banco
            String dbType = conn.getMetaData().getDatabaseProductName().toLowerCase();
            boolean isSQLite = dbType.contains("sqlite");
            
            String sql;
            if (isSQLite) {
                // SQLite: não tem TABELA_SALA_INVENTARIO nem TABELA_PARTICIPANTE_INVENTARIO
                // Usar apenas TABELA_COLETA com filtro por sala na localização
                sql = "SELECT c.*, NULL as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO " +
                      "FROM local_coleta c " +
                      "LEFT JOIN TABELA_INVENTARIO i ON c.id_inventario = i.ID " +
                      "WHERE c.sem_etiqueta = 1 " +
                      "AND c.id_inventario = ? " +
                      "ORDER BY c.data_coleta DESC";
            } else {
                // PostgreSQL: query original com JOINs
                sql = "SELECT c.*, u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO " +
                      "FROM TABELA_COLETA c " +
                      "LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                      "LEFT JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID " +
                      "LEFT JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID " +
                      "LEFT JOIN TABELA_SALA_INVENTARIO si ON c.ID_INVENTARIO = si.ID_INVENTARIO " +
                      "WHERE c.SEM_ETIQUETA = true " +
                      "AND c.ID_INVENTARIO = ? " +
                      "AND si.ID_SALA = ? " +
                      "ORDER BY c.DATA_COLETA DESC";
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idInventario);
                if (!isSQLite) {
                    stmt.setInt(2, idSala);
                }
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        coletas.add(criarColetaFromResultSet(rs));
                    }
                }
            }
        }
        
        return coletas;
    }
    
    /**
     * Busca coletas com etiqueta baseado na localização encontrada (busca EXATA)
     * IMPORTANTE: Usa comparação exata para evitar que salas com nomes similares retornem os mesmos dados
     * @param localizacaoEncontrada A localização onde os itens foram encontrados
     * @return Lista de coletas com etiqueta encontradas na localização especificada
     * @throws SQLException Se ocorrer erro na consulta
     */
    public List<Coleta> buscarColetasComEtiquetaPorLocalizacaoEncontrada(String localizacaoEncontrada) throws SQLException {
        System.out.println("[DEBUG ColetaDAO] Buscando coletas para localização EXATA: '" + localizacaoEncontrada + "'");
        
        String sql = "SELECT c.*, p.NUMERO as NUMERO_PATRIMONIO, p.DESCRICAO as DESCRICAO_PATRIMONIO, " +
                    "u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO " +
                    "FROM TABELA_COLETA c " +
                    "LEFT JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID " +
                    "LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                    "LEFT JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID " +
                    "LEFT JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID " +
                    "WHERE (c.SEM_ETIQUETA = false OR c.SEM_ETIQUETA IS NULL) " +
                    "AND c.LOCALIZACAO_ENCONTRADA = ? " + // Comparação EXATA (não LIKE)
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
        
        System.out.println("[DEBUG ColetaDAO] Encontradas " + coletas.size() + " coletas para '" + localizacaoEncontrada + "'");
        return coletas;
    }
    
    /**
     * Busca evolução de coletas por dia (últimos N dias)
     * Retorna quantidade de coletas agrupadas por data
     * 
     * @param inventarioId ID do inventário
     * @param dias Quantidade de dias para buscar
     * @return Lista de mapas com data e quantidade
     * @throws SQLException Se ocorrer erro na consulta
     */
    public List<Map<String, Object>> buscarEvolucaoColetasPorDia(Integer inventarioId, int dias) throws SQLException {
        String sql = "SELECT " +
                    "    CAST(DATA_COLETA AS DATE) as data, " +
                    "    COUNT(*) as quantidade " +
                    "FROM TABELA_COLETA " +
                    "WHERE ID_INVENTARIO = ? " +
                    "    AND DATA_COLETA >= CURRENT_DATE - ? " +
                    "GROUP BY CAST(DATA_COLETA AS DATE) " +
                    "ORDER BY data ASC";
        
        List<Map<String, Object>> resultado = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, inventarioId);
            stmt.setInt(2, dias);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new java.util.HashMap<>();
                    item.put("data", rs.getDate("data"));
                    item.put("quantidade", rs.getInt("quantidade"));
                    resultado.add(item);
                }
            }
        }
        
        return resultado;
    }
    
    /**
     * Busca evolução de coletas por período específico (usando datas do inventário)
     * Retorna quantidade de coletas agrupadas por data dentro do período informado
     * 
     * NOVO: Este método usa as datas de início e fim do inventário ao invés de "últimos N dias"
     * 
     * @param inventarioId ID do inventário
     * @param dataInicio Data de início do período
     * @param dataFim Data de fim do período
     * @return Lista de mapas com data e quantidade
     * @throws SQLException Se ocorrer erro na consulta
     */
    public List<Map<String, Object>> buscarEvolucaoColetasPorPeriodo(
            Integer inventarioId, 
            java.util.Date dataInicio, 
            java.util.Date dataFim) throws SQLException {
        
        String sql = "SELECT " +
                    "    CAST(DATA_COLETA AS DATE) as data, " +
                    "    COUNT(*) as quantidade " +
                    "FROM TABELA_COLETA " +
                    "WHERE ID_INVENTARIO = ? " +
                    "    AND DATA_COLETA >= ? " +
                    "    AND DATA_COLETA <= ? " +
                    "GROUP BY CAST(DATA_COLETA AS DATE) " +
                    "ORDER BY data ASC";
        
        List<Map<String, Object>> resultado = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, inventarioId);
            stmt.setTimestamp(2, new java.sql.Timestamp(dataInicio.getTime()));
            // Adicionar 23:59:59 ao dataFim para incluir todo o dia
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(dataFim);
            cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
            cal.set(java.util.Calendar.MINUTE, 59);
            cal.set(java.util.Calendar.SECOND, 59);
            stmt.setTimestamp(3, new java.sql.Timestamp(cal.getTimeInMillis()));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new java.util.HashMap<>();
                    item.put("data", rs.getDate("data"));
                    item.put("quantidade", rs.getInt("quantidade"));
                    resultado.add(item);
                }
            }
        }
        
        return resultado;
    }
    
    /**
     * Busca top itens mais coletados (por descrição do patrimônio)
     * 
     * @param inventarioId ID do inventário
     * @param limit Quantidade máxima de itens a retornar
     * @return Lista de mapas com descrição e quantidade
     * @throws SQLException Se ocorrer erro na consulta
     */
    public List<Map<String, Object>> buscarTopItensColetados(Integer inventarioId, int limit) throws SQLException {
        String sql = "SELECT " +
                    "    p.DESCRICAO as descricao, " +
                    "    COUNT(*) as quantidade " +
                    "FROM TABELA_COLETA c " +
                    "INNER JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID " +
                    "WHERE c.ID_INVENTARIO = ? " +
                    "GROUP BY p.DESCRICAO " +
                    "ORDER BY quantidade DESC " +
                    "LIMIT ?";
        
        List<Map<String, Object>> resultado = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, inventarioId);
            stmt.setInt(2, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new java.util.HashMap<>();
                    item.put("descricao", rs.getString("descricao"));
                    item.put("quantidade", rs.getInt("quantidade"));
                    resultado.add(item);
                }
            }
        }
        
        return resultado;
    }
    
    /**
     * Busca estatísticas de coletas por status
     * 
     * @param inventarioId ID do inventário
     * @return Lista de mapas com status e quantidade
     * @throws SQLException Se ocorrer erro na consulta
     */
    public List<Map<String, Object>> buscarEstatisticasPorStatus(Integer inventarioId) throws SQLException {
        String sql = "SELECT " +
                    "    COALESCE(ESTADO_ENCONTRADO, 'SEM INFO') as status, " +
                    "    COUNT(*) as quantidade " +
                    "FROM TABELA_COLETA " +
                    "WHERE ID_INVENTARIO = ? " +
                    "GROUP BY ESTADO_ENCONTRADO " +
                    "ORDER BY quantidade DESC";
        
        List<Map<String, Object>> resultado = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, inventarioId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new java.util.HashMap<>();
                    item.put("status", rs.getString("status"));
                    item.put("quantidade", rs.getInt("quantidade"));
                    resultado.add(item);
                }
            }
        }
        
        return resultado;
    }
    
    /**
     * Busca distribuição de coletas por sala
     * 
     * @param inventarioId ID do inventário
     * @param limit Quantidade máxima de salas a retornar
     * @return Lista de mapas com sala e quantidade
     * @throws SQLException Se ocorrer erro na consulta
     */
    public List<Map<String, Object>> buscarDistribuicaoPorSala(Integer inventarioId, int limit) throws SQLException {
        String sql = "SELECT " +
                    "    c.LOCALIZACAO_ENCONTRADA as sala, " +
                    "    COUNT(*) as quantidade " +
                    "FROM TABELA_COLETA c " +
                    "WHERE c.ID_INVENTARIO = ? " +
                    "GROUP BY c.LOCALIZACAO_ENCONTRADA " +
                    "ORDER BY quantidade DESC " +
                    "LIMIT ?";
        
        List<Map<String, Object>> resultado = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, inventarioId);
            stmt.setInt(2, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new java.util.HashMap<>();
                    item.put("sala", rs.getString("sala"));
                    item.put("quantidade", rs.getInt("quantidade"));
                    resultado.add(item);
                }
            }
        }
        
        return resultado;
    }
    
    /**
     * Busca coletas modificadas desde um timestamp específico
     * 
     * @param dataUltimaSync timestamp da última sincronização
     * @return lista de coletas modificadas
     * @throws SQLException se ocorrer erro na consulta
     */
    public List<Coleta> buscarModificadasDesde(Timestamp dataUltimaSync) throws SQLException {
        String sql = "SELECT * FROM TABELA_COLETA " +
                    "WHERE DATA_COLETA >= ? " +
                    "ORDER BY DATA_COLETA DESC";
        
        List<Coleta> coletas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, dataUltimaSync);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Coleta coleta = mapearResultSet(rs);
                    coletas.add(coleta);
                }
            }
        }
        
        return coletas;
    }
    
    /**
     * Busca coletas modificadas desde um timestamp específico para um inventário
     * 
     * @param dataUltimaSync timestamp da última sincronização
     * @param inventarioId ID do inventário
     * @return lista de coletas modificadas
     * @throws SQLException se ocorrer erro na consulta
     */
    public List<Coleta> buscarModificadasDesde(Timestamp dataUltimaSync, Integer inventarioId) throws SQLException {
        String sql = "SELECT * FROM TABELA_COLETA " +
                    "WHERE DATA_COLETA >= ? AND ID_INVENTARIO = ? " +
                    "ORDER BY DATA_COLETA DESC";
        
        List<Coleta> coletas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, dataUltimaSync);
            stmt.setInt(2, inventarioId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Coleta coleta = mapearResultSet(rs);
                    coletas.add(coleta);
                }
            }
        }
        
        return coletas;
    }
    
    /**
     * Mapeia um ResultSet para um objeto Coleta
     * 
     * @param rs ResultSet com dados da coleta
     * @return objeto Coleta
     * @throws SQLException se ocorrer erro ao ler dados
     */
    private Coleta mapearResultSet(ResultSet rs) throws SQLException {
        Coleta coleta = new Coleta();
        coleta.setId(rs.getInt("ID"));
        coleta.setIdInventario(rs.getInt("ID_INVENTARIO"));
        coleta.setIdPatrimonio(rs.getInt("ID_PATRIMONIO"));
        coleta.setIdColetor(rs.getInt("ID_COLETOR"));
        coleta.setDataColeta(rs.getTimestamp("DATA_COLETA"));
        coleta.setLocalizacaoEncontrada(rs.getString("LOCALIZACAO_ENCONTRADA"));
        coleta.setEstadoEncontrado(rs.getString("ESTADO_ENCONTRADO"));
        coleta.setObservacaoColeta(rs.getString("OBSERVACAO_COLETA"));
        
        // Converter double para BigDecimal
        double lat = rs.getDouble("LATITUDE");
        double lon = rs.getDouble("LONGITUDE");
        coleta.setLatitude(lat != 0 ? java.math.BigDecimal.valueOf(lat) : null);
        coleta.setLongitude(lon != 0 ? java.math.BigDecimal.valueOf(lon) : null);
        
        return coleta;
    }

    /**
     * Conta o número de coletas realizadas em uma sala específica para um inventário
     * @param idInventario ID do inventário
     * @param localizacaoSala Identificação da sala (LOCALIZACAO_ENCONTRADA)
     * @return Array com [totalItensColetados, totalItensSemEtiqueta]
     */
    public int[] contarColetasPorSala(int idInventario, String localizacaoSala) throws SQLException {
        boolean sqlite = isSQLite();
        
        String sqlTotal;
        String sqlSemEtiqueta;
        
        if (sqlite) {
            sqlTotal = "SELECT COUNT(*) FROM " + getColetaTableName() + 
                      " WHERE id_inventario = ? AND localizacao_encontrada = ?";
            sqlSemEtiqueta = "SELECT COUNT(*) FROM " + getColetaTableName() + 
                            " WHERE id_inventario = ? AND localizacao_encontrada = ? AND sem_etiqueta = 1";
        } else {
            sqlTotal = "SELECT COUNT(*) FROM " + getColetaTableName() + 
                      " WHERE ID_INVENTARIO = ? AND LOCALIZACAO_ENCONTRADA = ?";
            sqlSemEtiqueta = "SELECT COUNT(*) FROM " + getColetaTableName() + 
                            " WHERE ID_INVENTARIO = ? AND LOCALIZACAO_ENCONTRADA = ? AND SEM_ETIQUETA = true";
        }
        
        int totalItens = 0;
        int totalSemEtiqueta = 0;
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Contar total de itens
            try (PreparedStatement stmt = conn.prepareStatement(sqlTotal)) {
                stmt.setInt(1, idInventario);
                stmt.setString(2, localizacaoSala);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        totalItens = rs.getInt(1);
                    }
                }
            }
            
            // Contar itens sem etiqueta
            try (PreparedStatement stmt = conn.prepareStatement(sqlSemEtiqueta)) {
                stmt.setInt(1, idInventario);
                stmt.setString(2, localizacaoSala);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        totalSemEtiqueta = rs.getInt(1);
                    }
                }
            }
        }
        
        System.out.println("[DEBUG ColetaDAO] contarColetasPorSala - Sala: '" + localizacaoSala + 
                          "', Total: " + totalItens + ", Sem Etiqueta: " + totalSemEtiqueta);
        
        return new int[] { totalItens, totalSemEtiqueta };
    }
    
    /**
     * MÉTODO OTIMIZADO: Busca histórico de coletas com dados do patrimônio em UMA única query
     * Evita N+1 queries e retorna dados prontos para exibição na tabela
     * 
     * @param numeroSala Número/identificação da sala
     * @param limite Quantidade máxima de registros (0 = sem limite)
     * @return Lista de ColetaResumo com dados prontos para exibição
     */
    public List<com.inventario.dto.ColetaResumo> buscarHistoricoOtimizado(String numeroSala, int limite) throws SQLException {
        System.out.println("[DEBUG ColetaDAO] buscarHistoricoOtimizado - Sala: '" + numeroSala + "', Limite: " + limite);
        
        boolean sqlite = isSQLite();
        
        String sql;
        if (sqlite) {
            // ✅ CORRIGIDO: Nome correto da coluna no SQLite é "descricao_sem_etiqueta"
            sql = "SELECT " +
                  "c.id as id_coleta, " +
                  "c.data_coleta, " +
                  "CASE WHEN c.sem_etiqueta = 1 THEN 'SEM ETIQUETA' ELSE COALESCE(p.numero, 'N/A') END as numero_patrimonio, " +
                  "CASE WHEN c.sem_etiqueta = 1 THEN COALESCE(c.descricao_sem_etiqueta, '-') ELSE COALESCE(p.descricao, '-') END as descricao, " +
                  "COALESCE(c.situacao_encontrada, '-') as estado, " +
                  "COALESCE(c.sem_etiqueta, 0) as sem_etiqueta " +
                  "FROM local_coleta c " +
                  "LEFT JOIN local_patrimonio p ON c.id_patrimonio = p.id " +
                  "WHERE c.localizacao_encontrada = ? " +
                  "ORDER BY c.data_coleta DESC" +
                  (limite > 0 ? " LIMIT " + limite : "");
        } else {
            sql = "SELECT " +
                  "c.ID as id_coleta, " +
                  "c.DATA_COLETA, " +
                  "CASE WHEN c.SEM_ETIQUETA = true THEN 'SEM ETIQUETA' ELSE COALESCE(p.NUMERO, 'N/A') END as numero_patrimonio, " +
                  "CASE WHEN c.SEM_ETIQUETA = true THEN COALESCE(c.DESCRICAO_ITEM_SEM_ETIQUETA, '-') ELSE COALESCE(p.DESCRICAO, '-') END as descricao, " +
                  "COALESCE(c.ESTADO_ENCONTRADO, '-') as estado, " +
                  "COALESCE(c.SEM_ETIQUETA, false) as sem_etiqueta " +
                  "FROM " + getColetaTableName() + " c " +
                  "LEFT JOIN " + getPatrimonioTableName() + " p ON c.ID_PATRIMONIO = p.ID " +
                  "WHERE c.LOCALIZACAO_ENCONTRADA = ? " +
                  "ORDER BY c.DATA_COLETA DESC" +
                  (limite > 0 ? " LIMIT " + limite : "");
        }
        
        List<com.inventario.dto.ColetaResumo> resultado = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, numeroSala);
            
            long inicio = System.currentTimeMillis();
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    com.inventario.dto.ColetaResumo resumo = new com.inventario.dto.ColetaResumo();
                    resumo.setId(rs.getLong("id_coleta"));
                    resumo.setDataColeta(rs.getTimestamp("data_coleta"));
                    resumo.setNumeroPatrimonio(rs.getString("numero_patrimonio"));
                    resumo.setDescricao(rs.getString("descricao"));
                    resumo.setEstadoEncontrado(rs.getString("estado"));
                    resumo.setSemEtiqueta(rs.getBoolean("sem_etiqueta"));
                    resultado.add(resumo);
                }
            }
            
            long tempo = System.currentTimeMillis() - inicio;
            System.out.println("[DEBUG ColetaDAO] buscarHistoricoOtimizado - " + resultado.size() + 
                             " registros em " + tempo + "ms (1 query apenas!)");
        }
        
        return resultado;
    }
    
    /**
     * MÉTODO OTIMIZADO: Busca coletas com paginação REAL no banco de dados
     * Evita carregar todas as coletas em memória
     * 
     * PROBLEMA RESOLVIDO: Vazamento de memória (200MB → 4GB)
     * ANTES: Carregava TODAS as coletas e paginava em memória
     * DEPOIS: Paginação no SQL com LIMIT/OFFSET
     * 
     * @param page número da página (0-based)
     * @param size tamanho da página
     * @return lista de coletas da página
     */
    public List<Coleta> buscarColetasComPaginacao(int page, int size) throws SQLException {
        // CORREÇÃO 01/12/2025: Adicionado JOIN com TABELA_SALA para trazer nomeSala
        String sql = "SELECT c.*, p.NUMERO as NUMERO_PATRIMONIO, p.DESCRICAO as DESCRICAO_PATRIMONIO, " +
                    "u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO, " +
                    "COALESCE(s.NUMERO_SALA, s.DESCRICAO) as NOME_SALA " +
                    "FROM TABELA_COLETA c " +
                    "LEFT JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID " +
                    "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA " +
                    "LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                    "LEFT JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID " +
                    "LEFT JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID " +
                    "ORDER BY c.DATA_COLETA DESC " +
                    "LIMIT ? OFFSET ?";
        
        List<Coleta> coletas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, size);
            stmt.setInt(2, page * size);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    coletas.add(criarColetaFromResultSet(rs));
                }
            }
        }
        
        return coletas;
    }
    
    /**
     * MÉTODO OTIMIZADO: Conta total de coletas (para paginação)
     * Query leve apenas para contagem
     */
    public int contarTotalColetas() throws SQLException {
        String sql = "SELECT COUNT(*) FROM TABELA_COLETA";
        
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
     * MÉTODO OTIMIZADO: Busca coletas por usuário com paginação REAL
     */
    public List<Coleta> buscarColetasPorUsuarioComPaginacao(int idUsuario, int page, int size) throws SQLException {
        // CORREÇÃO 01/12/2025: Adicionado JOIN com TABELA_SALA para trazer nomeSala
        String sql = "SELECT c.*, p.NUMERO as NUMERO_PATRIMONIO, p.DESCRICAO as DESCRICAO_PATRIMONIO, " +
                    "u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO, " +
                    "COALESCE(s.NUMERO_SALA, s.DESCRICAO) as NOME_SALA " +
                    "FROM TABELA_COLETA c " +
                    "LEFT JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID " +
                    "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA " +
                    "LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                    "LEFT JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID " +
                    "LEFT JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID " +
                    "WHERE c.ID_COLETOR = ? " +
                    "ORDER BY c.DATA_COLETA DESC " +
                    "LIMIT ? OFFSET ?";
        
        List<Coleta> coletas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, size);
            stmt.setInt(3, page * size);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    coletas.add(criarColetaFromResultSet(rs));
                }
            }
        }
        
        return coletas;
    }
    
    /**
     * MÉTODO OTIMIZADO: Conta coletas por usuário
     */
    public int contarColetasPorUsuario(int idUsuario) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TABELA_COLETA WHERE ID_COLETOR = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsuario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        
        return 0;
    }

    /**
     * MÉTODO OTIMIZADO: Busca uma coleta específica por patrimônio e inventário
     * Evita carregar TODAS as coletas do inventário para encontrar uma específica
     * 
     * PROBLEMA RESOLVIDO: Vazamento de memória ao buscar coletas
     * ANTES: buscarPorInventario() carregava TODAS as coletas (milhares)
     * DEPOIS: Query direta retorna apenas 1 registro
     * 
     * @param idInventario ID do inventário
     * @param idPatrimonio ID do patrimônio
     * @return Coleta encontrada ou null
     */
    public Coleta buscarColetaPorPatrimonioEInventario(int idInventario, int idPatrimonio) throws SQLException {
        String sql = "SELECT c.*, p.NUMERO as NUMERO_PATRIMONIO, p.DESCRICAO as DESCRICAO_PATRIMONIO, " +
                    "u.NOME_COMPLETO as NOME_COLETOR, i.NOME as DESCRICAO_INVENTARIO " +
                    "FROM TABELA_COLETA c " +
                    "LEFT JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID " +
                    "LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON c.ID_PARTICIPANTE_INVENTARIO = pi.id_participante " +
                    "LEFT JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID " +
                    "LEFT JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID " +
                    "WHERE c.ID_INVENTARIO = ? AND c.ID_PATRIMONIO = ? " +
                    "ORDER BY c.DATA_COLETA DESC LIMIT 1";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            stmt.setInt(2, idPatrimonio);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return criarColetaFromResultSet(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * MÉTODO OTIMIZADO: Conta o total de coletas de uma sala SEM trazer os dados
     * Query leve apenas para contagem - usado para exibir contador correto
     * 
     * @param numeroSala Número/identificação da sala
     * @return Total de coletas na sala
     */
    public int contarColetasTotalPorSala(String numeroSala) throws SQLException {
        System.out.println("[DEBUG ColetaDAO] contarColetasTotalPorSala - Sala: '" + numeroSala + "'");
        
        boolean sqlite = isSQLite();
        
        String sql;
        if (sqlite) {
            sql = "SELECT COUNT(*) FROM local_coleta WHERE localizacao_encontrada = ?";
        } else {
            sql = "SELECT COUNT(*) FROM " + getColetaTableName() + " WHERE LOCALIZACAO_ENCONTRADA = ?";
        }
        
        int total = 0;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, numeroSala);
            
            long inicio = System.currentTimeMillis();
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    total = rs.getInt(1);
                }
            }
            
            long tempo = System.currentTimeMillis() - inicio;
            System.out.println("[DEBUG ColetaDAO] contarColetasTotalPorSala - Total: " + total + 
                             " em " + tempo + "ms");
        }
        
        return total;
    }
    
    /**
     * Busca todas as salas distintas onde há coletas registradas
     * Retorna lista de salas com contagem de coletas para filtros
     * 
     * @param inventarioId ID do inventário (pode ser null para buscar de todos)
     * @return Lista de mapas com id, nome e quantidade de coletas por sala
     */
    public List<Map<String, Object>> buscarSalasComColetas(Integer inventarioId) throws SQLException {
        System.out.println("[DEBUG ColetaDAO] buscarSalasComColetas - Inventário: " + inventarioId);
        
        List<Map<String, Object>> salas = new ArrayList<>();
        
        // Query que busca salas distintas com coletas
        // Usa LOCALIZACAO_ENCONTRADA pois é onde o item foi realmente encontrado
        String sql;
        if (inventarioId != null) {
            sql = "SELECT DISTINCT " +
                  "    COALESCE(c.LOCALIZACAO_ENCONTRADA, s.NUMERO, 'Sem Sala') as nome_sala, " +
                  "    COUNT(*) as quantidade " +
                  "FROM " + getColetaTableName() + " c " +
                  "LEFT JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID " +
                  "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID " +
                  "WHERE c.ID_INVENTARIO = ? " +
                  "GROUP BY COALESCE(c.LOCALIZACAO_ENCONTRADA, s.NUMERO, 'Sem Sala') " +
                  "ORDER BY quantidade DESC, nome_sala";
        } else {
            sql = "SELECT DISTINCT " +
                  "    COALESCE(c.LOCALIZACAO_ENCONTRADA, s.NUMERO, 'Sem Sala') as nome_sala, " +
                  "    COUNT(*) as quantidade " +
                  "FROM " + getColetaTableName() + " c " +
                  "LEFT JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID " +
                  "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID " +
                  "GROUP BY COALESCE(c.LOCALIZACAO_ENCONTRADA, s.NUMERO, 'Sem Sala') " +
                  "ORDER BY quantidade DESC, nome_sala";
        }
        
        long inicio = System.currentTimeMillis();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (inventarioId != null) {
                stmt.setInt(1, inventarioId);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> sala = new HashMap<>();
                    sala.put("nome", rs.getString("nome_sala"));
                    sala.put("quantidade", rs.getInt("quantidade"));
                    salas.add(sala);
                }
            }
        }
        
        long tempo = System.currentTimeMillis() - inicio;
        System.out.println("[DEBUG ColetaDAO] buscarSalasComColetas - " + salas.size() + 
                         " salas encontradas em " + tempo + "ms");
        
        return salas;
    }

}
