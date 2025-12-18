package com.inventario.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.inventario.util.DatabaseConnection;

/**
 * DAO para operações de Reconciliação de Patrimônios
 * Relaciona itens NÃO ENCONTRADOS com itens SEM ETIQUETA
 */
@Repository
public class ReconciliacaoDAO {

    /**
     * Busca patrimônios NÃO ENCONTRADOS em um inventário
     * (patrimônios ativos que não foram coletados)
     */
    public List<Map<String, Object>> buscarPatrimoniosNaoEncontrados(int idInventario) throws SQLException {
        String sql = """
            SELECT 
                p.id,
                p.numero,
                p.descricao,
                p.estado_conservacao,
                s.descricao as sala_esperada,
                s.numero_sala,
                r.nome as responsavel
            FROM tabela_patrimonio p
            LEFT JOIN tabela_sala s ON p.id_sala = s.id_sala
            LEFT JOIN tabela_responsavel r ON p.id_responsavel = r.id
            WHERE UPPER(p.status) = 'ATIVO'
            AND p.id NOT IN (
                SELECT DISTINCT id_patrimonio 
                FROM tabela_coleta 
                WHERE id_inventario = ? 
                AND id_patrimonio IS NOT NULL
            )
            ORDER BY p.descricao
            """;

        List<Map<String, Object>> resultado = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idInventario);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", rs.getInt("id"));
                    item.put("numero", rs.getString("numero"));
                    item.put("descricao", rs.getString("descricao"));
                    item.put("estadoConservacao", rs.getString("estado_conservacao"));
                    item.put("salaEsperada", rs.getString("sala_esperada"));
                    item.put("numeroSala", rs.getString("numero_sala"));
                    item.put("responsavel", rs.getString("responsavel"));
                    resultado.add(item);
                }
            }
        }

        return resultado;
    }

    /**
     * Busca itens SEM ETIQUETA coletados em um inventário
     */
    public List<Map<String, Object>> buscarItensSemEtiqueta(int idInventario) throws SQLException {
        String sql = """
            SELECT 
                c.id,
                c.descricao_item_sem_etiqueta as descricao,
                c.categoria_item_sem_etiqueta as categoria,
                c.localizacao_encontrada,
                c.estado_encontrado,
                c.data_coleta,
                c.foto_patrimonio,
                c.observacao_coleta,
                COALESCE(u.nome_completo, 'N/A') as coletor
            FROM tabela_coleta c
            LEFT JOIN tabela_participante_inventario pi ON c.id_participante_inventario = pi.id_participante
            LEFT JOIN tabela_usuario u ON pi.id_usuario = u.id
            WHERE c.id_inventario = ?
            AND c.sem_etiqueta = true
            ORDER BY c.descricao_item_sem_etiqueta
            """;

        List<Map<String, Object>> resultado = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idInventario);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", rs.getInt("id"));
                    item.put("descricao", rs.getString("descricao"));
                    item.put("categoria", rs.getString("categoria"));
                    item.put("localizacaoEncontrada", rs.getString("localizacao_encontrada"));
                    item.put("estadoEncontrado", rs.getString("estado_encontrado"));
                    item.put("dataColeta", rs.getTimestamp("data_coleta"));
                    item.put("fotoPatrimonio", rs.getString("foto_patrimonio"));
                    item.put("observacao", rs.getString("observacao_coleta"));
                    item.put("coletor", rs.getString("coletor"));
                    resultado.add(item);
                }
            }
        }

        return resultado;
    }

    /**
     * Calcula similaridade entre duas strings (algoritmo Levenshtein simplificado)
     */
    public double calcularSimilaridade(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0;
        
        s1 = s1.toUpperCase().trim();
        s2 = s2.toUpperCase().trim();
        
        if (s1.equals(s2)) return 100.0;
        if (s1.isEmpty() || s2.isEmpty()) return 0.0;

        // Verificar se uma contém a outra
        if (s1.contains(s2) || s2.contains(s1)) {
            int menorTamanho = Math.min(s1.length(), s2.length());
            int maiorTamanho = Math.max(s1.length(), s2.length());
            return (menorTamanho * 100.0) / maiorTamanho;
        }

        // Calcular distância de Levenshtein
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= s2.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                    Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                );
            }
        }

        int distancia = dp[s1.length()][s2.length()];
        int maiorTamanho = Math.max(s1.length(), s2.length());
        
        return ((maiorTamanho - distancia) * 100.0) / maiorTamanho;
    }

    /**
     * Busca sugestões de reconciliação baseado em similaridade
     * Otimizado para grandes volumes de dados com pré-filtragem
     */
    public List<Map<String, Object>> buscarSugestoesReconciliacao(int idInventario, double limiarSimilaridade) throws SQLException {
        List<Map<String, Object>> semEtiqueta = buscarItensSemEtiqueta(idInventario);
        List<Map<String, Object>> sugestoes = new ArrayList<>();
        
        System.out.println("=== INICIANDO BUSCA DE SUGESTÕES ===");
        System.out.println("Inventário ID: " + idInventario);
        System.out.println("Limiar similaridade: " + limiarSimilaridade + "%");
        System.out.println("Itens sem etiqueta: " + semEtiqueta.size());
        
        if (semEtiqueta.isEmpty()) {
            System.out.println("Nenhum item sem etiqueta encontrado!");
            return sugestoes;
        }

        // Para cada item sem etiqueta, buscar patrimônios similares diretamente no banco
        for (Map<String, Object> itemSemEtiqueta : semEtiqueta) {
            String descItem = (String) itemSemEtiqueta.get("descricao");
            if (descItem == null || descItem.trim().isEmpty()) {
                System.out.println("Item sem etiqueta com descrição vazia, pulando...");
                continue;
            }
            
            System.out.println("Buscando matches para: " + descItem);
            
            // Buscar patrimônios com descrição similar diretamente no banco (mais eficiente)
            List<Map<String, Object>> patrimoniosSimilares = buscarPatrimoniosSimilares(idInventario, descItem);
            System.out.println("Patrimônios similares encontrados: " + patrimoniosSimilares.size());
            
            for (Map<String, Object> patrimonio : patrimoniosSimilares) {
                String descPatrimonio = (String) patrimonio.get("descricao");
                double similaridade = calcularSimilaridade(descPatrimonio, descItem);
                
                if (similaridade >= limiarSimilaridade) {
                    Map<String, Object> sugestao = new HashMap<>();
                    sugestao.put("patrimonio", patrimonio);
                    sugestao.put("itemSemEtiqueta", itemSemEtiqueta);
                    sugestao.put("similaridade", similaridade);
                    sugestoes.add(sugestao);
                    
                    System.out.println("  MATCH! Similaridade: " + String.format("%.1f%%", similaridade) + 
                        " | Patrimônio: " + patrimonio.get("numero") + " - " + descPatrimonio);
                }
            }
        }
        
        System.out.println("=== TOTAL DE SUGESTÕES: " + sugestoes.size() + " ===");

        // Ordenar por similaridade decrescente
        sugestoes.sort((a, b) -> Double.compare(
            (Double) b.get("similaridade"),
            (Double) a.get("similaridade")
        ));
        
        // Limitar a 500 sugestões para evitar problemas de memória/UI
        if (sugestoes.size() > 500) {
            return new ArrayList<>(sugestoes.subList(0, 500));
        }

        return sugestoes;
    }
    
    /**
     * Busca patrimônios com descrição similar usando LIKE no banco
     * Mais eficiente que comparar todos os 10k+ registros em memória
     */
    private List<Map<String, Object>> buscarPatrimoniosSimilares(int idInventario, String descricaoItem) throws SQLException {
        // Extrair palavras-chave da descrição (primeiras 3 palavras significativas)
        String[] palavras = descricaoItem.toUpperCase().split("\\s+");
        StringBuilder likePattern = new StringBuilder("%");
        int count = 0;
        for (String palavra : palavras) {
            if (palavra.length() > 3 && count < 3) { // Ignorar palavras curtas
                likePattern.append(palavra).append("%");
                count++;
            }
        }
        
        String sql = """
            SELECT 
                p.id,
                p.numero,
                p.descricao,
                p.estado_conservacao,
                s.descricao as sala_esperada,
                s.numero_sala,
                r.nome as responsavel
            FROM tabela_patrimonio p
            LEFT JOIN tabela_sala s ON p.id_sala = s.id_sala
            LEFT JOIN tabela_responsavel r ON p.id_responsavel = r.id
            WHERE UPPER(p.status) = 'ATIVO'
            AND (
                UPPER(p.descricao) LIKE ? 
                OR UPPER(p.descricao) = ?
            )
            AND p.id NOT IN (
                SELECT DISTINCT id_patrimonio 
                FROM tabela_coleta 
                WHERE id_inventario = ? 
                AND id_patrimonio IS NOT NULL
            )
            ORDER BY p.descricao
            LIMIT 100
            """;

        List<Map<String, Object>> resultado = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, likePattern.toString());
            stmt.setString(2, descricaoItem.toUpperCase());
            stmt.setInt(3, idInventario);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", rs.getInt("id"));
                    item.put("numero", rs.getString("numero"));
                    item.put("descricao", rs.getString("descricao"));
                    item.put("estadoConservacao", rs.getString("estado_conservacao"));
                    item.put("salaEsperada", rs.getString("sala_esperada"));
                    item.put("numeroSala", rs.getString("numero_sala"));
                    item.put("responsavel", rs.getString("responsavel"));
                    resultado.add(item);
                }
            }
        }

        return resultado;
    }

    /**
     * Registra uma reconciliação
     */
    public int registrarReconciliacao(int idPatrimonio, int idColetaSemEtiqueta, 
                                       int idInventario, int idUsuario, 
                                       double similaridade, String status, 
                                       String observacoes) throws SQLException {
        String sql = """
            INSERT INTO tabela_reconciliacao 
            (id_patrimonio, id_coleta_sem_etiqueta, id_inventario, id_usuario, 
             similaridade, status, observacoes, data_reconciliacao)
            VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, idPatrimonio);
            stmt.setInt(2, idColetaSemEtiqueta);
            stmt.setInt(3, idInventario);
            stmt.setInt(4, idUsuario);
            stmt.setDouble(5, similaridade);
            stmt.setString(6, status);
            stmt.setString(7, observacoes);

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
     * Atualiza status de uma reconciliação
     */
    public void atualizarStatusReconciliacao(int idReconciliacao, String status, 
                                              String acaoTomada, String observacoes) throws SQLException {
        String sql = """
            UPDATE tabela_reconciliacao 
            SET status = ?, acao_tomada = ?, observacoes = ?
            WHERE id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setString(2, acaoTomada);
            stmt.setString(3, observacoes);
            stmt.setInt(4, idReconciliacao);

            stmt.executeUpdate();
        }
    }

    /**
     * Busca reconciliações por inventário
     */
    public List<Map<String, Object>> buscarReconciliacoesPorInventario(int idInventario) throws SQLException {
        String sql = """
            SELECT 
                r.id,
                r.similaridade,
                r.status,
                r.observacoes,
                r.acao_tomada,
                r.data_reconciliacao,
                p.numero as numero_patrimonio,
                p.descricao as descricao_patrimonio,
                c.descricao_item_sem_etiqueta,
                c.localizacao_encontrada,
                u.nome_completo as usuario_reconciliacao
            FROM tabela_reconciliacao r
            JOIN tabela_patrimonio p ON r.id_patrimonio = p.id
            JOIN tabela_coleta c ON r.id_coleta_sem_etiqueta = c.id
            JOIN tabela_usuario u ON r.id_usuario = u.id
            WHERE r.id_inventario = ?
            ORDER BY r.data_reconciliacao DESC
            """;

        List<Map<String, Object>> resultado = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idInventario);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", rs.getInt("id"));
                    item.put("similaridade", rs.getDouble("similaridade"));
                    item.put("status", rs.getString("status"));
                    item.put("observacoes", rs.getString("observacoes"));
                    item.put("acaoTomada", rs.getString("acao_tomada"));
                    item.put("dataReconciliacao", rs.getTimestamp("data_reconciliacao"));
                    item.put("numeroPatrimonio", rs.getString("numero_patrimonio"));
                    item.put("descricaoPatrimonio", rs.getString("descricao_patrimonio"));
                    item.put("descricaoItemSemEtiqueta", rs.getString("descricao_item_sem_etiqueta"));
                    item.put("localizacaoEncontrada", rs.getString("localizacao_encontrada"));
                    item.put("usuarioReconciliacao", rs.getString("usuario_reconciliacao"));
                    resultado.add(item);
                }
            }
        }

        return resultado;
    }

    /**
     * Conta estatísticas de reconciliação
     */
    public Map<String, Integer> contarEstatisticas(int idInventario) throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        
        // Contar não encontrados
        String sqlNaoEncontrados = """
            SELECT COUNT(*) as total FROM tabela_patrimonio p
            WHERE UPPER(p.status) = 'ATIVO'
            AND p.id NOT IN (
                SELECT DISTINCT id_patrimonio FROM tabela_coleta 
                WHERE id_inventario = ? AND id_patrimonio IS NOT NULL
            )
            """;
        
        // Contar sem etiqueta
        String sqlSemEtiqueta = """
            SELECT COUNT(*) as total FROM tabela_coleta
            WHERE id_inventario = ? AND sem_etiqueta = true
            """;
        
        // Contar reconciliações por status
        String sqlReconciliacoes = """
            SELECT status, COUNT(*) as total FROM tabela_reconciliacao
            WHERE id_inventario = ?
            GROUP BY status
            """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Não encontrados
            try (PreparedStatement stmt = conn.prepareStatement(sqlNaoEncontrados)) {
                stmt.setInt(1, idInventario);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        stats.put("naoEncontrados", rs.getInt("total"));
                    }
                }
            }

            // Sem etiqueta
            try (PreparedStatement stmt = conn.prepareStatement(sqlSemEtiqueta)) {
                stmt.setInt(1, idInventario);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        stats.put("semEtiqueta", rs.getInt("total"));
                    }
                }
            }

            // Reconciliações
            try (PreparedStatement stmt = conn.prepareStatement(sqlReconciliacoes)) {
                stmt.setInt(1, idInventario);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        stats.put("reconciliacao_" + rs.getString("status").toLowerCase(), 
                                  rs.getInt("total"));
                    }
                }
            }
        }

        return stats;
    }

    /**
     * Exclui uma reconciliação
     */
    public void excluirReconciliacao(int idReconciliacao) throws SQLException {
        String sql = "DELETE FROM tabela_reconciliacao WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idReconciliacao);
            stmt.executeUpdate();
        }
    }
}
