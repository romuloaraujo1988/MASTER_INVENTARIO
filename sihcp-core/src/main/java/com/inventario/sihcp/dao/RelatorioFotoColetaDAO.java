package com.inventario.sihcp.dao;

import com.inventario.sihcp.util.ConnectionManager;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO para buscar coletas que possuem foto, usado na geração do
 * Relatório Fotográfico de Coletas.
 *
 * <h2>Filtros disponíveis</h2>
 * <ul>
 *   <li>{@code todos} — todas as coletas com foto</li>
 *   <li>{@code patrimonio} — coletas normais com etiqueta</li>
 *   <li>{@code sem_etiqueta} — itens sem etiqueta</li>
 *   <li>{@code divergencia} — coletas com divergência detectada</li>
 * </ul>
 *
 * <h2>Dados retornados por coleta</h2>
 * <pre>
 *   coletaId, fotoPath, tipo,
 *   numeroPatrimonio, descricao, categoria,
 *   localizacaoEncontrada, estadoEncontrado,
 *   dataColeta, observacao, nomeColetor,
 *   nomeSala (sala de origem), nomeSalaEncontrada
 * </pre>
 *
 * @author Sistema de Inventário IFMT
 * @version 1.0.0
 */
@Repository
public class RelatorioFotoColetaDAO {

    /**
     * Busca todas as coletas com foto de um inventário, com filtro opcional por tipo.
     *
     * @param idInventario ID do inventário
     * @param tipo         Filtro: {@code todos}, {@code patrimonio},
     *                     {@code sem_etiqueta} ou {@code divergencia}.
     *                     {@code null} ou {@code "todos"} retorna tudo.
     * @param idSala       Filtro opcional por sala (null = todas as salas)
     * @return Lista de mapas com os dados de cada coleta com foto
     */
    public List<Map<String, Object>> buscarColetasComFoto(
            int idInventario, String tipo, Integer idSala) throws SQLException {

        StringBuilder sql = new StringBuilder("""
                SELECT
                    c.ID                            AS coletaId,
                    c.FOTO_PATH                     AS fotoPath,
                    CASE
                        WHEN c.SEM_ETIQUETA = TRUE  THEN 'sem_etiqueta'
                        WHEN c.DIVERGENCIA  = TRUE  THEN 'divergencia'
                        ELSE 'patrimonio'
                    END                             AS tipo,
                    COALESCE(p.NUMERO, '')          AS numeroPatrimonio,
                    COALESCE(
                        c.DESCRICAO_ITEM_SEM_ETIQUETA,
                        p.DESCRICAO,
                        'Item sem descrição'
                    )                               AS descricao,
                    COALESCE(c.CATEGORIA_ITEM_SEM_ETIQUETA, '') AS categoria,
                    COALESCE(c.LOCALIZACAO_ENCONTRADA,
                             c.LOCALIZACAO_ATUAL,
                             sl.NOME, '')           AS localizacaoEncontrada,
                    COALESCE(c.ESTADO_ENCONTRADO, '') AS estadoEncontrado,
                    c.DATA_COLETA                   AS dataColeta,
                    COALESCE(c.OBSERVACAO_COLETA, '') AS observacao,
                    COALESCE(u.NOME, 'Desconhecido') AS nomeColetor,
                    COALESCE(so.NOME, '')           AS nomeSalaOrigem,
                    c.MOTIVO_DIVERGENCIA            AS motivoDivergencia
                FROM TABELA_COLETA c
                LEFT JOIN TABELA_PATRIMONIO p
                       ON c.ID_PATRIMONIO = p.ID
                LEFT JOIN TABELA_SALA so
                       ON p.ID_SALA = so.ID
                LEFT JOIN TABELA_SALA sl
                       ON c.ID_SALA = sl.ID
                LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi
                       ON c.ID_PARTICIPANTE_INVENTARIO = pi.ID
                LEFT JOIN TABELA_USUARIO u
                       ON pi.ID_USUARIO = u.ID
                WHERE c.ID_INVENTARIO = ?
                  AND c.FOTO_PATH IS NOT NULL
                  AND c.FOTO_PATH <> ''
                """);

        // Filtro por tipo
        if (tipo != null && !tipo.isBlank() && !tipo.equalsIgnoreCase("todos")) {
            switch (tipo.toLowerCase()) {
                case "sem_etiqueta" -> sql.append("  AND c.SEM_ETIQUETA = TRUE\n");
                case "divergencia"  -> sql.append("  AND c.DIVERGENCIA = TRUE\n");
                case "patrimonio"   -> sql.append("  AND c.SEM_ETIQUETA = FALSE AND c.DIVERGENCIA = FALSE\n");
            }
        }

        // Filtro por sala
        if (idSala != null) {
            sql.append("  AND (c.ID_SALA = ? OR p.ID_SALA = ?)\n");
        }

        sql.append("ORDER BY c.DATA_COLETA DESC");

        List<Map<String, Object>> resultado = new ArrayList<>();

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIdx = 1;
            stmt.setInt(paramIdx++, idInventario);
            if (idSala != null) {
                stmt.setInt(paramIdx++, idSala);
                stmt.setInt(paramIdx, idSala);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("coletaId",            rs.getInt("coletaId"));
                    row.put("fotoPath",             rs.getString("fotoPath"));
                    row.put("tipo",                 rs.getString("tipo"));
                    row.put("numeroPatrimonio",     rs.getString("numeroPatrimonio"));
                    row.put("descricao",            rs.getString("descricao"));
                    row.put("categoria",            rs.getString("categoria"));
                    row.put("localizacaoEncontrada",rs.getString("localizacaoEncontrada"));
                    row.put("estadoEncontrado",     rs.getString("estadoEncontrado"));
                    row.put("dataColeta",           rs.getTimestamp("dataColeta"));
                    row.put("observacao",           rs.getString("observacao"));
                    row.put("nomeColetor",          rs.getString("nomeColetor"));
                    row.put("nomeSalaOrigem",       rs.getString("nomeSalaOrigem"));
                    row.put("motivoDivergencia",    rs.getString("motivoDivergencia"));
                    resultado.add(row);
                }
            }
        }

        return resultado;
    }

    /**
     * Conta quantas coletas com foto existem para um inventário.
     *
     * @param idInventario ID do inventário
     * @return total de coletas com foto
     */
    public int contarColetasComFoto(int idInventario) throws SQLException {
        String sql = """
                SELECT COUNT(*) FROM TABELA_COLETA
                WHERE ID_INVENTARIO = ?
                  AND FOTO_PATH IS NOT NULL
                  AND FOTO_PATH <> ''
                """;

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idInventario);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
