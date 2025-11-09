package com.inventario.dao;

import com.inventario.model.DispositivoMobile;
import com.inventario.model.StatusDispositivo;
import com.inventario.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para gerenciamento de dispositivos mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class DispositivoMobileDAO {

    /**
     * Registra um novo dispositivo ou atualiza se já existir
     */
    public DispositivoMobile registrarDispositivo(DispositivoMobile dispositivo) throws SQLException {
        // Verificar se já existe
        Optional<DispositivoMobile> existente = buscarPorDeviceId(dispositivo.getDeviceId());
        
        if (existente.isPresent()) {
            // Atualizar informações do dispositivo existente
            return atualizarInformacoes(existente.get().getId(), dispositivo);
        } else {
            // Inserir novo dispositivo
            return inserir(dispositivo);
        }
    }

    /**
     * Insere um novo dispositivo (automaticamente aprovado)
     */
    private DispositivoMobile inserir(DispositivoMobile dispositivo) throws SQLException {
        String sql = """
            INSERT INTO dispositivo_mobile (
                device_id, id_usuario, modelo, fabricante, versao_android, versao_app,
                endereco_ip, endereco_mac, status, data_registro, data_ultima_conexao,
                ativo, observacoes
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'APROVADO'::status_dispositivo, ?, ?, ?, ?)
            RETURNING id
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, dispositivo.getDeviceId());
            stmt.setInt(2, dispositivo.getIdUsuario());
            stmt.setString(3, dispositivo.getModelo());
            stmt.setString(4, dispositivo.getFabricante());
            stmt.setString(5, dispositivo.getVersaoAndroid());
            stmt.setString(6, dispositivo.getVersaoApp());
            stmt.setString(7, dispositivo.getEnderecoIp());
            stmt.setString(8, dispositivo.getEnderecoMac());
            stmt.setTimestamp(9, Timestamp.valueOf(dispositivo.getDataRegistro()));
            stmt.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setBoolean(11, dispositivo.getAtivo());
            stmt.setString(12, dispositivo.getObservacoes());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                dispositivo.setId(rs.getInt("id"));
                dispositivo.setStatus(StatusDispositivo.APROVADO); // Forçar status aprovado
            }

            return dispositivo;
        }
    }

    /**
     * Atualiza informações do dispositivo
     */
    private DispositivoMobile atualizarInformacoes(Integer id, DispositivoMobile dispositivo) throws SQLException {
        String sql = """
            UPDATE dispositivo_mobile SET
                modelo = ?,
                fabricante = ?,
                versao_android = ?,
                versao_app = ?,
                endereco_ip = ?,
                endereco_mac = ?,
                data_ultima_conexao = ?
            WHERE id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, dispositivo.getModelo());
            stmt.setString(2, dispositivo.getFabricante());
            stmt.setString(3, dispositivo.getVersaoAndroid());
            stmt.setString(4, dispositivo.getVersaoApp());
            stmt.setString(5, dispositivo.getEnderecoIp());
            stmt.setString(6, dispositivo.getEnderecoMac());
            stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(8, id);

            stmt.executeUpdate();
            
            return buscarPorId(id).orElse(dispositivo);
        }
    }

    /**
     * Busca dispositivo por ID
     */
    public Optional<DispositivoMobile> buscarPorId(Integer id) throws SQLException {
        String sql = """
            SELECT d.*, u.nome_completo as nome_usuario
            FROM dispositivo_mobile d
            LEFT JOIN tabela_usuario u ON d.id_usuario = u.id
            WHERE d.id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapearResultSet(rs));
            }

            return Optional.empty();
        }
    }

    /**
     * Busca dispositivo por Device ID
     */
    public Optional<DispositivoMobile> buscarPorDeviceId(String deviceId) throws SQLException {
        String sql = """
            SELECT d.*, u.nome_completo as nome_usuario
            FROM dispositivo_mobile d
            LEFT JOIN tabela_usuario u ON d.id_usuario = u.id
            WHERE d.device_id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, deviceId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapearResultSet(rs));
            }

            return Optional.empty();
        }
    }

    /**
     * Lista todos os dispositivos
     */
    public List<DispositivoMobile> listarTodos() throws SQLException {
        String sql = """
            SELECT d.*, u.nome_completo as nome_usuario
            FROM dispositivo_mobile d
            LEFT JOIN tabela_usuario u ON d.id_usuario = u.id
            ORDER BY d.data_registro DESC
            """;

        List<DispositivoMobile> dispositivos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                dispositivos.add(mapearResultSet(rs));
            }
        }

        return dispositivos;
    }

    /**
     * Lista dispositivos por status
     */
    public List<DispositivoMobile> listarPorStatus(StatusDispositivo status) throws SQLException {
        String sql = """
            SELECT d.*, u.nome_completo as nome_usuario
            FROM dispositivo_mobile d
            LEFT JOIN tabela_usuario u ON d.id_usuario = u.id
            WHERE d.status = ?::status_dispositivo
            ORDER BY d.data_registro DESC
            """;

        List<DispositivoMobile> dispositivos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                dispositivos.add(mapearResultSet(rs));
            }
        }

        return dispositivos;
    }

    /**
     * Lista dispositivos de um usuário
     */
    public List<DispositivoMobile> listarPorUsuario(Integer idUsuario) throws SQLException {
        String sql = """
            SELECT d.*, u.nome_completo as nome_usuario
            FROM dispositivo_mobile d
            LEFT JOIN tabela_usuario u ON d.id_usuario = u.id
            WHERE d.id_usuario = ?
            ORDER BY d.data_registro DESC
            """;

        List<DispositivoMobile> dispositivos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                dispositivos.add(mapearResultSet(rs));
            }
        }

        return dispositivos;
    }

    /**
     * Atualiza status do dispositivo
     */
    public void atualizarStatus(Integer id, StatusDispositivo novoStatus) throws SQLException {
        String sql = "UPDATE dispositivo_mobile SET status = ?::status_dispositivo WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, novoStatus.name());
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Atualiza token do dispositivo
     */
    public void atualizarToken(Integer id, String token, LocalDateTime dataExpiracao) throws SQLException {
        String sql = """
            UPDATE dispositivo_mobile SET
                token_atual = ?,
                data_expiracao_token = ?,
                data_ultima_conexao = ?
            WHERE id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, token);
            stmt.setTimestamp(2, Timestamp.valueOf(dataExpiracao));
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(4, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Registra sincronização
     */
    public void registrarSincronizacao(Integer id) throws SQLException {
        String sql = "UPDATE dispositivo_mobile SET data_ultima_sincronizacao = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Ativa/desativa dispositivo
     */
    public void alterarAtivacao(Integer id, boolean ativo) throws SQLException {
        String sql = "UPDATE dispositivo_mobile SET ativo = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, ativo);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Remove dispositivo
     */
    public void remover(Integer id) throws SQLException {
        String sql = "DELETE FROM dispositivo_mobile WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Mapeia ResultSet para DispositivoMobile
     */
    private DispositivoMobile mapearResultSet(ResultSet rs) throws SQLException {
        DispositivoMobile dispositivo = new DispositivoMobile();
        
        dispositivo.setId(rs.getInt("id"));
        dispositivo.setDeviceId(rs.getString("device_id"));
        dispositivo.setIdUsuario(rs.getInt("id_usuario"));
        dispositivo.setNomeUsuario(rs.getString("nome_usuario"));
        dispositivo.setModelo(rs.getString("modelo"));
        dispositivo.setFabricante(rs.getString("fabricante"));
        dispositivo.setVersaoAndroid(rs.getString("versao_android"));
        dispositivo.setVersaoApp(rs.getString("versao_app"));
        dispositivo.setEnderecoIp(rs.getString("endereco_ip"));
        dispositivo.setEnderecoMac(rs.getString("endereco_mac"));
        dispositivo.setStatus(StatusDispositivo.valueOf(rs.getString("status")));
        
        Timestamp dataRegistro = rs.getTimestamp("data_registro");
        if (dataRegistro != null) {
            dispositivo.setDataRegistro(dataRegistro.toLocalDateTime());
        }
        
        Timestamp dataUltimaConexao = rs.getTimestamp("data_ultima_conexao");
        if (dataUltimaConexao != null) {
            dispositivo.setDataUltimaConexao(dataUltimaConexao.toLocalDateTime());
        }
        
        Timestamp dataUltimaSincronizacao = rs.getTimestamp("data_ultima_sincronizacao");
        if (dataUltimaSincronizacao != null) {
            dispositivo.setDataUltimaSincronizacao(dataUltimaSincronizacao.toLocalDateTime());
        }
        
        dispositivo.setAtivo(rs.getBoolean("ativo"));
        dispositivo.setTokenAtual(rs.getString("token_atual"));
        
        Timestamp dataExpiracaoToken = rs.getTimestamp("data_expiracao_token");
        if (dataExpiracaoToken != null) {
            dispositivo.setDataExpiracaoToken(dataExpiracaoToken.toLocalDateTime());
        }
        
        dispositivo.setObservacoes(rs.getString("observacoes"));
        
        return dispositivo;
    }
}
