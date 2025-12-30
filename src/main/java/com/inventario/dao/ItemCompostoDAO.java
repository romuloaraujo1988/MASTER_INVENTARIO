package com.inventario.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.inventario.util.DatabaseConnection;

/**
 * DAO para gerenciar itens compostos e suas coletas
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class ItemCompostoDAO {
    
    /**
     * Verifica se um patrimônio é um item composto
     */
    public boolean isItemComposto(int idPatrimonio) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tabela_item_composto WHERE id_patrimonio_principal = ?";
        
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
     * Busca todos os componentes de um item composto
     */
    public List<Map<String, Object>> buscarComponentes(int idPatrimonio) throws SQLException {
        String sql = """
            SELECT 
                ic.id,
                ic.tipo_componente,
                ic.descricao_componente,
                ic.quantidade_esperada,
                ic.obrigatorio,
                ic.observacao
            FROM tabela_item_composto ic
            WHERE ic.id_patrimonio_principal = ?
            ORDER BY ic.tipo_componente
            """;
        
        List<Map<String, Object>> componentes = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idPatrimonio);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> comp = new HashMap<>();
                    comp.put("id", rs.getInt("id"));
                    comp.put("tipo", rs.getString("tipo_componente"));
                    comp.put("descricao", rs.getString("descricao_componente"));
                    comp.put("quantidadeEsperada", rs.getInt("quantidade_esperada"));
                    comp.put("obrigatorio", rs.getBoolean("obrigatorio"));
                    comp.put("observacao", rs.getString("observacao"));
                    componentes.add(comp);
                }
            }
        }
        
        return componentes;
    }
    
    /**
     * Busca o status de coleta dos componentes em um inventário
     */
    public List<Map<String, Object>> buscarStatusColeta(int idPatrimonio, int idInventario) throws SQLException {
        // Verificar se a coluna localizacao_encontrada existe
        boolean colunaLocalizacaoExiste = verificarColunaExiste("tabela_coleta_componente", "localizacao_encontrada");
        
        String sql;
        if (colunaLocalizacaoExiste) {
            sql = """
                SELECT 
                    ic.id,
                    ic.tipo_componente,
                    ic.descricao_componente,
                    ic.quantidade_esperada,
                    ic.obrigatorio,
                    COALESCE(cc.quantidade_encontrada, 0) as quantidade_encontrada,
                    COALESCE(cc.status_componente, 'PENDENTE') as status_componente,
                    COALESCE(cc.observacao_coleta, '') as observacao_coleta,
                    COALESCE(cc.localizacao_encontrada, '') as localizacao_encontrada,
                    cc.data_coleta
                FROM tabela_item_composto ic
                LEFT JOIN tabela_coleta_componente cc 
                    ON ic.id = cc.id_item_composto 
                    AND cc.id_inventario = ?
                WHERE ic.id_patrimonio_principal = ?
                ORDER BY ic.tipo_componente
                """;
        } else {
            // Query sem a coluna localizacao_encontrada
            sql = """
                SELECT 
                    ic.id,
                    ic.tipo_componente,
                    ic.descricao_componente,
                    ic.quantidade_esperada,
                    ic.obrigatorio,
                    COALESCE(cc.quantidade_encontrada, 0) as quantidade_encontrada,
                    COALESCE(cc.status_componente, 'PENDENTE') as status_componente,
                    COALESCE(cc.observacao_coleta, '') as observacao_coleta,
                    cc.data_coleta
                FROM tabela_item_composto ic
                LEFT JOIN tabela_coleta_componente cc 
                    ON ic.id = cc.id_item_composto 
                    AND cc.id_inventario = ?
                WHERE ic.id_patrimonio_principal = ?
                ORDER BY ic.tipo_componente
                """;
        }
        
        List<Map<String, Object>> componentes = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            stmt.setInt(2, idPatrimonio);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> comp = new HashMap<>();
                    comp.put("id", rs.getInt("id"));
                    comp.put("tipo", rs.getString("tipo_componente"));
                    comp.put("descricao", rs.getString("descricao_componente"));
                    comp.put("quantidadeEsperada", rs.getInt("quantidade_esperada"));
                    comp.put("quantidadeEncontrada", rs.getInt("quantidade_encontrada"));
                    comp.put("obrigatorio", rs.getBoolean("obrigatorio"));
                    comp.put("status", rs.getString("status_componente"));
                    comp.put("observacao", rs.getString("observacao_coleta"));
                    // Localização só existe se a coluna foi criada
                    if (colunaLocalizacaoExiste) {
                        comp.put("localizacaoEncontrada", rs.getString("localizacao_encontrada"));
                    } else {
                        comp.put("localizacaoEncontrada", "");
                    }
                    comp.put("dataColeta", rs.getTimestamp("data_coleta"));
                    componentes.add(comp);
                }
            }
        }
        
        return componentes;
    }
    
    /**
     * Verifica se uma coluna existe em uma tabela
     */
    private boolean verificarColunaExiste(String tabela, String coluna) {
        String sql = """
            SELECT COUNT(*) FROM information_schema.columns 
            WHERE table_name = ? AND column_name = ?
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, tabela);
            stmt.setString(2, coluna);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao verificar coluna: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Registra ou atualiza a coleta de um componente (sem localização)
     */
    public void registrarColetaComponente(int idItemComposto, int idInventario, int idColetor,
                                         int quantidadeEncontrada, String observacao) throws SQLException {
        registrarColetaComponente(idItemComposto, idInventario, idColetor, quantidadeEncontrada, observacao, null);
    }
    
    /**
     * Registra ou atualiza a coleta de um componente COM localização
     * 
     * @param idItemComposto ID do componente
     * @param idInventario ID do inventário
     * @param idColetor ID do usuário coletor
     * @param quantidadeEncontrada Quantidade encontrada
     * @param observacao Observação da coleta
     * @param localizacaoEncontrada Local onde o componente foi encontrado
     */
    public void registrarColetaComponente(int idItemComposto, int idInventario, int idColetor,
                                         int quantidadeEncontrada, String observacao, 
                                         String localizacaoEncontrada) throws SQLException {
        
        // Determinar status baseado na quantidade
        String status = determinarStatus(idItemComposto, quantidadeEncontrada);
        
        // Verificar se a coluna localizacao_encontrada existe
        boolean colunaLocalizacaoExiste = verificarColunaExiste("tabela_coleta_componente", "localizacao_encontrada");
        
        String sql;
        if (colunaLocalizacaoExiste) {
            sql = """
                INSERT INTO tabela_coleta_componente 
                    (id_item_composto, id_inventario, id_coletor, quantidade_encontrada, 
                     status_componente, observacao_coleta, localizacao_encontrada)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id_item_composto, id_inventario) 
                DO UPDATE SET
                    quantidade_encontrada = EXCLUDED.quantidade_encontrada,
                    status_componente = EXCLUDED.status_componente,
                    observacao_coleta = EXCLUDED.observacao_coleta,
                    localizacao_encontrada = EXCLUDED.localizacao_encontrada,
                    data_coleta = CURRENT_TIMESTAMP
                """;
        } else {
            // SQL sem a coluna localizacao_encontrada
            sql = """
                INSERT INTO tabela_coleta_componente 
                    (id_item_composto, id_inventario, id_coletor, quantidade_encontrada, 
                     status_componente, observacao_coleta)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (id_item_composto, id_inventario) 
                DO UPDATE SET
                    quantidade_encontrada = EXCLUDED.quantidade_encontrada,
                    status_componente = EXCLUDED.status_componente,
                    observacao_coleta = EXCLUDED.observacao_coleta,
                    data_coleta = CURRENT_TIMESTAMP
                """;
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idItemComposto);
            stmt.setInt(2, idInventario);
            stmt.setInt(3, idColetor);
            stmt.setInt(4, quantidadeEncontrada);
            stmt.setString(5, status);
            stmt.setString(6, observacao);
            
            if (colunaLocalizacaoExiste) {
                stmt.setString(7, localizacaoEncontrada);
            }
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Determina o status do componente baseado na quantidade encontrada
     */
    private String determinarStatus(int idItemComposto, int quantidadeEncontrada) throws SQLException {
        String sql = "SELECT quantidade_esperada FROM tabela_item_composto WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idItemComposto);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int esperada = rs.getInt("quantidade_esperada");
                    
                    if (quantidadeEncontrada >= esperada) {
                        return "COMPLETO";
                    } else if (quantidadeEncontrada > 0) {
                        return "PARCIAL";
                    } else {
                        return "FALTANTE";
                    }
                }
            }
        }
        
        return "PENDENTE";
    }
    
    /**
     * Busca o status geral de um item composto
     */
    public Map<String, Object> buscarStatusGeral(int idPatrimonio, int idInventario) throws SQLException {
        String sql = """
            SELECT 
                COUNT(ic.id) as total_componentes,
                SUM(CASE WHEN cc.status_componente = 'COMPLETO' THEN 1 ELSE 0 END) as completos,
                SUM(CASE WHEN cc.status_componente = 'PARCIAL' THEN 1 ELSE 0 END) as parciais,
                SUM(CASE WHEN cc.status_componente = 'FALTANTE' THEN 1 ELSE 0 END) as faltantes,
                SUM(CASE WHEN cc.status_componente = 'PENDENTE' OR cc.id IS NULL THEN 1 ELSE 0 END) as pendentes
            FROM tabela_item_composto ic
            LEFT JOIN tabela_coleta_componente cc 
                ON ic.id = cc.id_item_composto 
                AND cc.id_inventario = ?
            WHERE ic.id_patrimonio_principal = ?
            """;
        
        Map<String, Object> status = new HashMap<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            stmt.setInt(2, idPatrimonio);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total_componentes");
                    int completos = rs.getInt("completos");
                    int parciais = rs.getInt("parciais");
                    int faltantes = rs.getInt("faltantes");
                    int pendentes = rs.getInt("pendentes");
                    
                    status.put("total", total);
                    status.put("completos", completos);
                    status.put("parciais", parciais);
                    status.put("faltantes", faltantes);
                    status.put("pendentes", pendentes);
                    
                    // Determinar status geral
                    String statusGeral;
                    if (completos == total) {
                        statusGeral = "COMPLETO";
                    } else if (completos + parciais > 0) {
                        statusGeral = "PARCIAL";
                    } else if (pendentes == total) {
                        statusGeral = "PENDENTE";
                    } else {
                        statusGeral = "FALTANTE";
                    }
                    
                    status.put("statusGeral", statusGeral);
                }
            }
        }
        
        return status;
    }
    
    /**
     * Adiciona um novo componente a um item composto
     */
    public void adicionarComponente(int idPatrimonio, String tipo, String descricao, 
                                    int quantidade, boolean obrigatorio, String observacao) throws SQLException {
        String sql = """
            INSERT INTO tabela_item_composto 
                (id_patrimonio_principal, tipo_componente, descricao_componente, 
                 quantidade_esperada, obrigatorio, observacao)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idPatrimonio);
            stmt.setString(2, tipo);
            stmt.setString(3, descricao);
            stmt.setInt(4, quantidade);
            stmt.setBoolean(5, obrigatorio);
            stmt.setString(6, observacao);
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Remove um componente de um item composto
     */
    public void removerComponente(int idComponente) throws SQLException {
        String sql = "DELETE FROM tabela_item_composto WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idComponente);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Remove todos os componentes de um item composto (remove o patrimônio dos itens compostos)
     * Também remove as coletas de componentes associadas
     * 
     * @param idPatrimonio ID do patrimônio principal
     * @return Número de componentes removidos
     */
    public int removerItemComposto(int idPatrimonio) throws SQLException {
        int componentesRemovidos = 0;
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // 1. Primeiro, remover as coletas de componentes associadas
                String sqlRemoverColetas = """
                    DELETE FROM tabela_coleta_componente 
                    WHERE id_item_composto IN (
                        SELECT id FROM tabela_item_composto WHERE id_patrimonio_principal = ?
                    )
                    """;
                
                try (PreparedStatement stmt = conn.prepareStatement(sqlRemoverColetas)) {
                    stmt.setInt(1, idPatrimonio);
                    stmt.executeUpdate();
                }
                
                // 2. Depois, remover os componentes
                String sqlRemoverComponentes = "DELETE FROM tabela_item_composto WHERE id_patrimonio_principal = ?";
                
                try (PreparedStatement stmt = conn.prepareStatement(sqlRemoverComponentes)) {
                    stmt.setInt(1, idPatrimonio);
                    componentesRemovidos = stmt.executeUpdate();
                }
                
                conn.commit();
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
        
        return componentesRemovidos;
    }
    
    /**
     * Conta quantos componentes um item composto possui
     */
    public int contarComponentes(int idPatrimonio) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tabela_item_composto WHERE id_patrimonio_principal = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idPatrimonio);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Verifica se um item composto possui coletas em um inventário específico
     * Retorna informações sobre as coletas existentes
     * 
     * @param idPatrimonio ID do patrimônio principal
     * @param idInventario ID do inventário (null para buscar inventário ativo)
     * @return Map com informações: temColetas, totalComponentes, componentesColetados, inventarioNome
     */
    public Map<String, Object> verificarColetasExistentes(int idPatrimonio, Integer idInventario) throws SQLException {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("temColetas", false);
        resultado.put("totalComponentes", 0);
        resultado.put("componentesColetados", 0);
        resultado.put("inventarioNome", "");
        
        // Se não foi informado inventário, buscar o ativo
        String sqlInventario = idInventario != null 
            ? "SELECT id, nome FROM tabela_inventario WHERE id = ?"
            : "SELECT id, nome FROM tabela_inventario WHERE status_inventario = 'EM_ANDAMENTO' LIMIT 1";
        
        int invId = 0;
        String invNome = "";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Buscar inventário
            try (PreparedStatement stmt = conn.prepareStatement(sqlInventario)) {
                if (idInventario != null) {
                    stmt.setInt(1, idInventario);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        invId = rs.getInt("id");
                        invNome = rs.getString("nome");
                    }
                }
            }
            
            if (invId == 0) {
                return resultado; // Sem inventário ativo
            }
            
            resultado.put("inventarioNome", invNome);
            
            // Contar componentes e coletas
            String sql = """
                SELECT 
                    COUNT(ic.id) as total_componentes,
                    COUNT(cc.id) as componentes_coletados
                FROM tabela_item_composto ic
                LEFT JOIN tabela_coleta_componente cc 
                    ON ic.id = cc.id_item_composto 
                    AND cc.id_inventario = ?
                WHERE ic.id_patrimonio_principal = ?
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, invId);
                stmt.setInt(2, idPatrimonio);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int total = rs.getInt("total_componentes");
                        int coletados = rs.getInt("componentes_coletados");
                        
                        resultado.put("totalComponentes", total);
                        resultado.put("componentesColetados", coletados);
                        resultado.put("temColetas", coletados > 0);
                    }
                }
            }
        }
        
        return resultado;
    }
    
    /**
     * Lista todos os itens compostos cadastrados
     */
    public List<Map<String, Object>> listarItensCompostos() throws SQLException {
        String sql = """
            SELECT DISTINCT
                p.id,
                p.numero,
                p.descricao,
                COUNT(ic.id) as total_componentes
            FROM tabela_patrimonio p
            JOIN tabela_item_composto ic ON p.id = ic.id_patrimonio_principal
            GROUP BY p.id, p.numero, p.descricao
            ORDER BY p.numero
            """;
        
        List<Map<String, Object>> itens = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", rs.getInt("id"));
                item.put("numero", rs.getString("numero"));
                item.put("descricao", rs.getString("descricao"));
                item.put("totalComponentes", rs.getInt("total_componentes"));
                itens.add(item);
            }
        }
        
        return itens;
    }
    
    /**
     * Lista todos os itens compostos com detalhes (número, descrição, sala)
     */
    public List<Map<String, Object>> listarItensCompostosComDetalhes() throws SQLException {
        String sql = """
            SELECT DISTINCT
                p.id,
                p.numero,
                p.descricao,
                s.numero_sala as sala,
                COUNT(ic.id) as total_componentes
            FROM tabela_patrimonio p
            JOIN tabela_item_composto ic ON p.id = ic.id_patrimonio_principal
            LEFT JOIN tabela_sala s ON p.id_sala = s.id
            GROUP BY p.id, p.numero, p.descricao, s.numero_sala
            ORDER BY p.numero
            """;
        
        List<Map<String, Object>> itens = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", rs.getInt("id"));
                item.put("numero", rs.getString("numero"));
                item.put("descricao", rs.getString("descricao"));
                item.put("sala", rs.getString("sala"));
                item.put("totalComponentes", rs.getInt("total_componentes"));
                itens.add(item);
            }
        }
        
        return itens;
    }
    
    /**
     * Busca itens compostos por descrição (filtro)
     */
    public List<Map<String, Object>> buscarItensCompostosPorDescricao(String filtro) throws SQLException {
        String sql = """
            SELECT DISTINCT
                p.id,
                p.numero,
                p.descricao,
                s.numero_sala as sala,
                COUNT(ic.id) as total_componentes
            FROM tabela_patrimonio p
            JOIN tabela_item_composto ic ON p.id = ic.id_patrimonio_principal
            LEFT JOIN tabela_sala s ON p.id_sala = s.id
            WHERE UPPER(p.descricao) LIKE UPPER(?)
               OR UPPER(p.numero) LIKE UPPER(?)
            GROUP BY p.id, p.numero, p.descricao, s.numero_sala
            ORDER BY p.numero
            """;
        
        List<Map<String, Object>> itens = new ArrayList<>();
        String filtroLike = "%" + filtro + "%";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, filtroLike);
            stmt.setString(2, filtroLike);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", rs.getInt("id"));
                    item.put("numero", rs.getString("numero"));
                    item.put("descricao", rs.getString("descricao"));
                    item.put("sala", rs.getString("sala"));
                    item.put("totalComponentes", rs.getInt("total_componentes"));
                    itens.add(item);
                }
            }
        }
        
        return itens;
    }
    
    /**
     * Busca relatório completo de itens compostos para um inventário
     * Retorna todos os componentes com status de coleta
     */
    public List<Map<String, Object>> buscarRelatorioCompleto(int idInventario) throws SQLException {
        // Verificar se a coluna localizacao_encontrada existe
        boolean colunaLocalizacaoExiste = verificarColunaExiste("tabela_coleta_componente", "localizacao_encontrada");
        
        String sql;
        if (colunaLocalizacaoExiste) {
            sql = """
                SELECT 
                    p.numero,
                    p.descricao,
                    COALESCE(s.descricao, 'Sem sala') as sala,
                    ic.tipo_componente,
                    ic.descricao_componente,
                    ic.quantidade_esperada,
                    COALESCE(cc.quantidade_encontrada, 0) as quantidade_encontrada,
                    COALESCE(cc.localizacao_encontrada, '') as localizacao_encontrada,
                    COALESCE(cc.status_componente, 'PENDENTE') as status,
                    TO_CHAR(cc.data_coleta, 'DD/MM/YYYY HH24:MI') as data_coleta,
                    u.nome_completo as coletor
                FROM tabela_patrimonio p
                INNER JOIN tabela_item_composto ic ON p.id = ic.id_patrimonio_principal
                LEFT JOIN tabela_sala s ON p.id_sala = s.id
                LEFT JOIN tabela_coleta_componente cc ON ic.id = cc.id_item_composto AND cc.id_inventario = ?
                LEFT JOIN tabela_usuario u ON cc.id_coletor = u.id
                ORDER BY p.numero, ic.tipo_componente
                """;
        } else {
            sql = """
                SELECT 
                    p.numero,
                    p.descricao,
                    COALESCE(s.descricao, 'Sem sala') as sala,
                    ic.tipo_componente,
                    ic.descricao_componente,
                    ic.quantidade_esperada,
                    COALESCE(cc.quantidade_encontrada, 0) as quantidade_encontrada,
                    COALESCE(cc.status_componente, 'PENDENTE') as status,
                    TO_CHAR(cc.data_coleta, 'DD/MM/YYYY HH24:MI') as data_coleta,
                    u.nome_completo as coletor
                FROM tabela_patrimonio p
                INNER JOIN tabela_item_composto ic ON p.id = ic.id_patrimonio_principal
                LEFT JOIN tabela_sala s ON p.id_sala = s.id
                LEFT JOIN tabela_coleta_componente cc ON ic.id = cc.id_item_composto AND cc.id_inventario = ?
                LEFT JOIN tabela_usuario u ON cc.id_coletor = u.id
                ORDER BY p.numero, ic.tipo_componente
                """;
        }
        
        List<Map<String, Object>> resultado = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("numero", rs.getString("numero"));
                    row.put("descricao", rs.getString("descricao"));
                    row.put("sala", rs.getString("sala"));
                    row.put("tipoComponente", rs.getString("tipo_componente"));
                    row.put("descricaoComponente", rs.getString("descricao_componente"));
                    row.put("qtdEsperada", rs.getInt("quantidade_esperada"));
                    row.put("qtdEncontrada", rs.getInt("quantidade_encontrada"));
                    row.put("localEncontrado", colunaLocalizacaoExiste ? rs.getString("localizacao_encontrada") : "");
                    row.put("status", rs.getString("status"));
                    row.put("dataColeta", rs.getString("data_coleta"));
                    row.put("coletor", rs.getString("coletor"));
                    resultado.add(row);
                }
            }
        }
        
        return resultado;
    }
}
