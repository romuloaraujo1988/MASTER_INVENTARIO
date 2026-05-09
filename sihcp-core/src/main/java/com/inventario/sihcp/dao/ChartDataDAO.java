package com.inventario.sihcp.dao;

import com.inventario.sihcp.util.DatabaseConnection;

import java.sql.*;
import java.util.*;

/**
 * DAO especializado para buscar dados para gráficos e estatísticas
 */
public class ChartDataDAO {

    /**
     * Conta patrimônios por status
     */
    public int contarPorStatus(String status) {
        String sql = "SELECT COUNT(*) as total FROM patrimonio WHERE UPPER(status) = UPPER(?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("total");
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao contar patrimônios por status: " + e.getMessage());
        }
        
        return 0;
    }

    /**
     * Conta total de patrimônios
     */
    public int contarTodosPatrimonios() {
        String sql = "SELECT COUNT(*) as total FROM patrimonio";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt("total");
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao contar todos os patrimônios: " + e.getMessage());
        }
        
        return 0;
    }

    /**
     * Busca quantidade de patrimônios por setor
     */
    public List<Map<String, Object>> contarPatrimoniosPorSetor() {
        List<Map<String, Object>> resultado = new ArrayList<>();
        
        String sql = """
            SELECT 
                COALESCE(s.nome, 'Sem Setor') as setor,
                COUNT(p.id) as quantidade
            FROM patrimonio p
            LEFT JOIN sala sa ON p.id_sala = sa.id
            LEFT JOIN setor s ON sa.id_setor = s.id
            GROUP BY s.nome
            ORDER BY quantidade DESC
            LIMIT 10
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("setor", rs.getString("setor"));
                row.put("quantidade", rs.getInt("quantidade"));
                resultado.add(row);
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar patrimônios por setor: " + e.getMessage());
        }
        
        return resultado;
    }

    /**
     * Conta coletas realizadas em um inventário
     */
    public int contarColetadosPorInventario(Integer idInventario) {
        if (idInventario == null) {
            return 0;
        }
        
        String sql = "SELECT COUNT(DISTINCT id_patrimonio) as total FROM coleta WHERE id_inventario = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("total");
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao contar coletados: " + e.getMessage());
        }
        
        return 0;
    }

    /**
     * Busca evolução diária das coletas
     */
    public List<Map<String, Object>> buscarEvolucaoDiaria(Integer idInventario) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        
        if (idInventario == null) {
            return resultado;
        }
        
        String sql = """
            SELECT 
                TO_CHAR(data_coleta, 'DD/MM') as data,
                COUNT(*) as quantidade
            FROM coleta
            WHERE id_inventario = ?
            GROUP BY DATE(data_coleta), TO_CHAR(data_coleta, 'DD/MM')
            ORDER BY DATE(data_coleta)
            LIMIT 30
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            ResultSet rs = stmt.executeQuery();
            
            int acumulado = 0;
            while (rs.next()) {
                acumulado += rs.getInt("quantidade");
                Map<String, Object> row = new HashMap<>();
                row.put("data", rs.getString("data"));
                row.put("quantidade", acumulado);
                resultado.add(row);
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar evolução diária: " + e.getMessage());
        }
        
        return resultado;
    }

    /**
     * Busca top 10 descrições mais coletadas
     */
    public List<Map<String, Object>> buscarTop10Descricoes(Integer idInventario) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        
        if (idInventario == null) {
            return resultado;
        }
        
        String sql = """
            SELECT 
                p.descricao,
                COUNT(c.id) as quantidade
            FROM coleta c
            INNER JOIN patrimonio p ON c.id_patrimonio = p.id
            WHERE c.id_inventario = ?
            GROUP BY p.descricao
            ORDER BY quantidade DESC
            LIMIT 10
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("descricao", rs.getString("descricao"));
                row.put("quantidade", rs.getInt("quantidade"));
                resultado.add(row);
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar top 10 descrições: " + e.getMessage());
        }
        
        return resultado;
    }

    /**
     * Busca estatísticas gerais do inventário
     */
    public Map<String, Object> buscarEstatisticasGerais(Integer idInventario) {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalPatrimonios", contarTodosPatrimonios());
        stats.put("totalColetados", contarColetadosPorInventario(idInventario));
        
        int total = (int) stats.get("totalPatrimonios");
        int coletados = (int) stats.get("totalColetados");
        int pendentes = total - coletados;
        
        stats.put("totalPendentes", pendentes);
        stats.put("percentualConcluido", total > 0 ? (coletados * 100.0 / total) : 0.0);
        
        return stats;
    }
}
