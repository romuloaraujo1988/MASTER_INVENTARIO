package com.inventario.sihcp.dao;

import com.inventario.sihcp.util.DatabaseConnection;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO para operações de Ocorrências de Patrimônios
 * Registra histórico de investigações para itens não encontrados
 */
@Repository
public class OcorrenciaPatrimonioDAO {

    // Tipos de ocorrência
    public static final String TIPO_NOTIFICACAO = "NOTIFICACAO_RESPONSAVEL";
    public static final String TIPO_INVESTIGACAO = "INVESTIGACAO";
    public static final String TIPO_TRANSFERENCIA = "TRANSFERENCIA";
    public static final String TIPO_LOCALIZACAO = "LOCALIZACAO_ATUALIZADA";
    public static final String TIPO_EXTRAVIO = "EXTRAVIO_CONFIRMADO";
    public static final String TIPO_BAIXA = "BAIXA_SOLICITADA";

    // Status
    public static final String STATUS_ABERTA = "ABERTA";
    public static final String STATUS_EM_ANDAMENTO = "EM_ANDAMENTO";
    public static final String STATUS_AGUARDANDO = "AGUARDANDO_RESPOSTA";
    public static final String STATUS_RESOLVIDA = "RESOLVIDA";
    public static final String STATUS_CANCELADA = "CANCELADA";

    /**
     * Registra uma nova ocorrência
     */
    public int registrarOcorrencia(int idPatrimonio, int idInventario, int idUsuario,
                                    String tipoOcorrencia, String descricao, 
                                    Timestamp dataPrazo) throws SQLException {
        String sql = """
            INSERT INTO tabela_ocorrencia_patrimonio 
            (id_patrimonio, id_inventario, id_usuario, tipo_ocorrencia, descricao, data_prazo)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, idPatrimonio);
            stmt.setInt(2, idInventario);
            stmt.setInt(3, idUsuario);
            stmt.setString(4, tipoOcorrencia);
            stmt.setString(5, descricao);
            stmt.setTimestamp(6, dataPrazo);

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * Atualiza status de uma ocorrência
     */
    public void atualizarStatus(int idOcorrencia, String status, String acaoResolucao) throws SQLException {
        String sql = """
            UPDATE tabela_ocorrencia_patrimonio 
            SET status = ?, acao_resolucao = ?,
                data_fechamento = CASE WHEN ? IN ('RESOLVIDA', 'CANCELADA') THEN CURRENT_TIMESTAMP ELSE NULL END
            WHERE id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setString(2, acaoResolucao);
            stmt.setString(3, status);
            stmt.setInt(4, idOcorrencia);
            stmt.executeUpdate();
        }
    }

    /**
     * Registra resposta do responsável
     */
    public void registrarResposta(int idOcorrencia, String resposta, String novaLocalizacao) throws SQLException {
        String sql = """
            UPDATE tabela_ocorrencia_patrimonio 
            SET resposta_responsavel = ?, nova_localizacao = ?, status = 'EM_ANDAMENTO'
            WHERE id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, resposta);
            stmt.setString(2, novaLocalizacao);
            stmt.setInt(3, idOcorrencia);
            stmt.executeUpdate();
        }
    }

    /**
     * Marca como notificado
     */
    public void marcarNotificado(int idOcorrencia, String emailResponsavel) throws SQLException {
        String sql = """
            UPDATE tabela_ocorrencia_patrimonio 
            SET responsavel_notificado = true, data_notificacao = CURRENT_TIMESTAMP, 
                email_responsavel = ?, status = 'AGUARDANDO_RESPOSTA'
            WHERE id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, emailResponsavel);
            stmt.setInt(2, idOcorrencia);
            stmt.executeUpdate();
        }
    }

    /**
     * Busca ocorrências por patrimônio
     */
    public List<Map<String, Object>> buscarPorPatrimonio(int idPatrimonio) throws SQLException {
        String sql = """
            SELECT o.*, u.nome_completo as usuario_nome
            FROM tabela_ocorrencia_patrimonio o
            JOIN tabela_usuario u ON o.id_usuario = u.id
            WHERE o.id_patrimonio = ?
            ORDER BY o.data_abertura DESC
            """;

        return executarConsulta(sql, idPatrimonio);
    }

    /**
     * Busca ocorrências por inventário
     */
    public List<Map<String, Object>> buscarPorInventario(int idInventario) throws SQLException {
        String sql = """
            SELECT o.*, p.numero as numero_patrimonio, p.descricao as descricao_patrimonio,
                   r.nome as responsavel_nome, u.nome_completo as usuario_nome
            FROM tabela_ocorrencia_patrimonio o
            JOIN tabela_patrimonio p ON o.id_patrimonio = p.id
            LEFT JOIN tabela_responsavel r ON p.id_responsavel = r.id
            JOIN tabela_usuario u ON o.id_usuario = u.id
            WHERE o.id_inventario = ?
            ORDER BY o.data_abertura DESC
            """;

        return executarConsulta(sql, idInventario);
    }

    /**
     * Busca ocorrências abertas/pendentes
     */
    public List<Map<String, Object>> buscarPendentes(int idInventario) throws SQLException {
        String sql = """
            SELECT o.*, p.numero as numero_patrimonio, p.descricao as descricao_patrimonio,
                   r.nome as responsavel_nome, r.email as responsavel_email,
                   u.nome_completo as usuario_nome
            FROM tabela_ocorrencia_patrimonio o
            JOIN tabela_patrimonio p ON o.id_patrimonio = p.id
            LEFT JOIN tabela_responsavel r ON p.id_responsavel = r.id
            JOIN tabela_usuario u ON o.id_usuario = u.id
            WHERE o.id_inventario = ?
            AND o.status NOT IN ('RESOLVIDA', 'CANCELADA')
            ORDER BY o.data_prazo ASC NULLS LAST, o.data_abertura DESC
            """;

        return executarConsulta(sql, idInventario);
    }

    /**
     * Conta ocorrências por status
     */
    public Map<String, Integer> contarPorStatus(int idInventario) throws SQLException {
        String sql = """
            SELECT status, COUNT(*) as total
            FROM tabela_ocorrencia_patrimonio
            WHERE id_inventario = ?
            GROUP BY status
            """;

        Map<String, Integer> resultado = new HashMap<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idInventario);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.put(rs.getString("status"), rs.getInt("total"));
                }
            }
        }

        return resultado;
    }

    private List<Map<String, Object>> executarConsulta(String sql, int parametro) throws SQLException {
        List<Map<String, Object>> resultado = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, parametro);

            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int colunas = meta.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> linha = new HashMap<>();
                    for (int i = 1; i <= colunas; i++) {
                        linha.put(meta.getColumnLabel(i), rs.getObject(i));
                    }
                    resultado.add(linha);
                }
            }
        }

        return resultado;
    }

    /**
     * Verifica se patrimônio já tem ocorrência aberta no inventário
     */
    public boolean temOcorrenciaAberta(int idPatrimonio, int idInventario) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM tabela_ocorrencia_patrimonio
            WHERE id_patrimonio = ? AND id_inventario = ?
            AND status NOT IN ('RESOLVIDA', 'CANCELADA')
            """;

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
}
