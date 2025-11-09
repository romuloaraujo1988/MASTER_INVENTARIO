package com.inventario.dao;

import com.inventario.model.Patrimonio;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO Refatorado para operações com Patrimônio
 * Usa BaseDAO para eliminar código duplicado
 * 
 * REDUÇÃO: ~500 linhas → ~200 linhas (-60%)
 */
@Repository
public class PatrimonioDAORefactored extends BaseDAO<Patrimonio, Integer> {
    
    // ========== Implementação dos Métodos Abstratos ==========
    
    @Override
    protected String getTableName() {
        return "TABELA_PATRIMONIO";
    }
    
    @Override
    protected String getInsertSQL() {
        return "INSERT INTO TABELA_PATRIMONIO (NUMERO, STATUS, DESCRICAO, ROTULOS, " +
               "ID_RESPONSAVEL, VALOR_AQUISICAO, VALOR_DEPRECIADO, NUMERO_NOTA_FISCAL, " +
               "NUMERO_SERIE, MARCA, MODELO, DATA_ENTRADA, FORNECEDOR, ID_SALA, " +
               "ESTADO_CONSERVACAO, CATEGORIA) " +
               "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }
    
    @Override
    protected String getUpdateSQL() {
        return "UPDATE TABELA_PATRIMONIO SET NUMERO = ?, STATUS = ?, DESCRICAO = ?, " +
               "ROTULOS = ?, ID_RESPONSAVEL = ?, VALOR_AQUISICAO = ?, VALOR_DEPRECIADO = ?, " +
               "NUMERO_NOTA_FISCAL = ?, NUMERO_SERIE = ?, MARCA = ?, MODELO = ?, " +
               "DATA_ENTRADA = ?, FORNECEDOR = ?, ID_SALA = ?, ESTADO_CONSERVACAO = ?, " +
               "CATEGORIA = ? WHERE ID = ?";
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement stmt, Patrimonio p) throws SQLException {
        stmt.setString(1, p.getNumero());
        stmt.setString(2, p.getStatus());
        stmt.setString(3, p.getDescricao());
        stmt.setString(4, p.getRotulos());
        
        // ID_RESPONSAVEL (nullable)
        if (p.getIdResponsavel() > 0) {
            stmt.setInt(5, p.getIdResponsavel());
        } else {
            stmt.setNull(5, Types.INTEGER);
        }
        
        stmt.setBigDecimal(6, p.getValorAquisicao());
        stmt.setBigDecimal(7, p.getValorDepreciado());
        stmt.setString(8, p.getNumeroNotaFiscal());
        stmt.setString(9, p.getNumeroSerie());
        stmt.setString(10, p.getMarca());
        stmt.setString(11, p.getModelo());
        stmt.setDate(12, p.getDataEntrada());
        stmt.setString(13, p.getFornecedor());
        
        // ID_SALA (nullable)
        if (p.getIdSala() > 0) {
            stmt.setInt(14, p.getIdSala());
        } else {
            stmt.setNull(14, Types.INTEGER);
        }
        
        stmt.setString(15, p.getEstadoConservacao());
        stmt.setString(16, p.getCategoria());
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement stmt, Patrimonio p) throws SQLException {
        setInsertParameters(stmt, p);
        stmt.setInt(17, p.getId());
    }
    
    @Override
    protected Patrimonio mapResultSetToEntity(ResultSet rs) throws SQLException {
        Patrimonio p = new Patrimonio();
        
        p.setId(rs.getInt("ID"));
        p.setNumero(rs.getString("NUMERO"));
        p.setStatus(rs.getString("STATUS"));
        p.setDescricao(rs.getString("DESCRICAO"));
        p.setRotulos(rs.getString("ROTULOS"));
        p.setIdResponsavel(rs.getInt("ID_RESPONSAVEL"));
        p.setValorAquisicao(rs.getBigDecimal("VALOR_AQUISICAO"));
        p.setValorDepreciado(rs.getBigDecimal("VALOR_DEPRECIADO"));
        p.setNumeroNotaFiscal(rs.getString("NUMERO_NOTA_FISCAL"));
        p.setNumeroSerie(rs.getString("NUMERO_SERIE"));
        p.setMarca(rs.getString("MARCA"));
        p.setModelo(rs.getString("MODELO"));
        p.setDataEntrada(rs.getDate("DATA_ENTRADA"));
        p.setDataCarga(rs.getTimestamp("DATA_CARGA"));
        p.setFornecedor(rs.getString("FORNECEDOR"));
        p.setIdSala(rs.getInt("ID_SALA"));
        p.setEstadoConservacao(rs.getString("ESTADO_CONSERVACAO"));
        p.setCategoria(rs.getString("CATEGORIA"));
        
        // Valores padrão
        p.setSituacao("ATIVO");
        String descricao = rs.getString("DESCRICAO");
        p.setDescricaoResumida(descricao != null && descricao.length() > 50 ? 
            descricao.substring(0, 50) + "..." : descricao);
        
        // Campos calculados (se existirem no ResultSet)
        try {
            p.setNomeResponsavel(rs.getString("nome_responsavel"));
            p.setNomeSala(rs.getString("nome_sala"));
        } catch (SQLException e) {
            // Campos opcionais - ignorar se não existirem
        }
        
        return p;
    }
    
    @Override
    protected void setGeneratedId(Patrimonio entity, int id) {
        entity.setId(id);
    }
    
    // ========== Métodos Específicos do PatrimonioDAO ==========
    
    /**
     * Busca patrimônio por número
     */
    public Patrimonio buscarPorNumero(String numeroPatrimonio) throws SQLException {
        String sql = "SELECT p.*, r.NOME as nome_responsavel, s.DESCRICAO as nome_sala " +
                    "FROM TABELA_PATRIMONIO p " +
                    "LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID " +
                    "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA " +
                    "WHERE p.NUMERO = ?";
        return executeQuerySingle(sql, numeroPatrimonio);
    }
    
    /**
     * Busca patrimônios por termo (busca em múltiplos campos)
     */
    public List<Patrimonio> buscarPorTermo(String termo) throws SQLException {
        String sql = "SELECT p.*, r.NOME as nome_responsavel, s.DESCRICAO as nome_sala " +
                    "FROM TABELA_PATRIMONIO p " +
                    "LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID " +
                    "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA " +
                    "WHERE (p.NUMERO ILIKE ? OR p.DESCRICAO ILIKE ? OR p.ROTULOS ILIKE ? " +
                    "OR p.NUMERO_SERIE ILIKE ? OR r.NOME ILIKE ? OR s.DESCRICAO ILIKE ?) " +
                    "ORDER BY p.NUMERO";
        
        String termoBusca = "%" + termo + "%";
        return executeQuery(sql, termoBusca, termoBusca, termoBusca, termoBusca, termoBusca, termoBusca);
    }
    
    /**
     * Busca patrimônios por descrição
     */
    public List<Patrimonio> buscarPorDescricao(String descricao) throws SQLException {
        String sql = "SELECT p.*, r.NOME as nome_responsavel, s.DESCRICAO as nome_sala " +
                    "FROM TABELA_PATRIMONIO p " +
                    "LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID " +
                    "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA " +
                    "WHERE p.DESCRICAO ILIKE ? ORDER BY p.DESCRICAO";
        
        return executeQuery(sql, "%" + descricao + "%");
    }
    
    /**
     * Busca patrimônios por sala (ID)
     */
    public List<Patrimonio> buscarPorSala(int idSala) throws SQLException {
        String sql = "SELECT p.*, r.NOME as nome_responsavel, s.DESCRICAO as nome_sala " +
                    "FROM TABELA_PATRIMONIO p " +
                    "LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID " +
                    "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA " +
                    "WHERE p.ID_SALA = ? ORDER BY p.NUMERO";
        
        return executeQuery(sql, idSala);
    }
    
    /**
     * Busca patrimônios por sala (nome)
     */
    public List<Patrimonio> buscarPorSala(String nomeSala) throws SQLException {
        String sql = "SELECT p.*, r.NOME as nome_responsavel, s.DESCRICAO as nome_sala " +
                    "FROM TABELA_PATRIMONIO p " +
                    "LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID " +
                    "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA " +
                    "WHERE s.DESCRICAO ILIKE ? ORDER BY p.NUMERO";
        
        return executeQuery(sql, "%" + nomeSala + "%");
    }
    
    /**
     * Busca patrimônios por responsável (nome)
     */
    public List<Patrimonio> buscarPorResponsavel(String nomeResponsavel) throws SQLException {
        String sql = "SELECT p.*, r.NOME as nome_responsavel, s.DESCRICAO as nome_sala " +
                    "FROM TABELA_PATRIMONIO p " +
                    "LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID " +
                    "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA " +
                    "WHERE r.NOME ILIKE ? ORDER BY p.NUMERO";
        
        return executeQuery(sql, "%" + nomeResponsavel + "%");
    }
    
    /**
     * Busca patrimônios por responsável (ID) com paginação
     */
    public List<Patrimonio> buscarPorResponsavelComPaginacao(int idResponsavel, int page, int size) throws SQLException {
        String sql = "SELECT p.*, r.NOME as nome_responsavel, s.DESCRICAO as nome_sala " +
                    "FROM TABELA_PATRIMONIO p " +
                    "LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID " +
                    "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA " +
                    "WHERE p.ID_RESPONSAVEL = ? " +
                    "ORDER BY p.NUMERO " +
                    "LIMIT ? OFFSET ?";
        
        return executeQuery(sql, idResponsavel, size, page * size);
    }
    
    /**
     * Verifica se número de patrimônio já existe (exceto o próprio)
     */
    public boolean numeroPatrimonioExiste(String numeroPatrimonio, int idExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TABELA_PATRIMONIO WHERE NUMERO = ? AND ID != ?";
        Integer count = executeScalar(sql, Integer.class, numeroPatrimonio, idExcluir);
        return count != null && count > 0;
    }
    
    /**
     * Conta patrimônios por responsável
     */
    public int contarPatrimoniosPorResponsavel(int idResponsavel) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TABELA_PATRIMONIO WHERE ID_RESPONSAVEL = ?";
        Integer count = executeScalar(sql, Integer.class, idResponsavel);
        return count != null ? count : 0;
    }
    
    /**
     * Conta patrimônios ativos
     */
    public int contarPatrimoniosAtivos() throws SQLException {
        String sql = "SELECT COUNT(*) FROM TABELA_PATRIMONIO WHERE STATUS = 'ATIVO'";
        Integer count = executeScalar(sql, Integer.class);
        return count != null ? count : 0;
    }
    
    /**
     * Calcula valor total dos patrimônios ativos
     */
    public double calcularValorTotal() throws SQLException {
        String sql = "SELECT COALESCE(SUM(VALOR_AQUISICAO), 0) FROM TABELA_PATRIMONIO WHERE STATUS = 'ATIVO'";
        Double valor = executeScalar(sql, Double.class);
        return valor != null ? valor : 0.0;
    }
    
    /**
     * Lista todos os patrimônios com joins
     */
    public List<Patrimonio> listarTodosComJoins() throws SQLException {
        String sql = "SELECT p.*, r.NOME as nome_responsavel, s.DESCRICAO as nome_sala " +
                    "FROM TABELA_PATRIMONIO p " +
                    "LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID " +
                    "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA " +
                    "ORDER BY p.NUMERO";
        
        return executeQuery(sql);
    }
    
    /**
     * Busca por ID com joins
     */
    public Patrimonio buscarPorIdComJoins(int id) throws SQLException {
        String sql = "SELECT p.*, r.NOME as nome_responsavel, s.DESCRICAO as nome_sala " +
                    "FROM TABELA_PATRIMONIO p " +
                    "LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID " +
                    "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA " +
                    "WHERE p.ID = ?";
        
        return executeQuerySingle(sql, id);
    }
    
    /**
     * Busca abrangente por descrição (busca em múltiplos campos com relevância)
     */
    public List<Patrimonio> buscarPorDescricaoAbrangente(String descricao) throws SQLException {
        if (descricao == null || descricao.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // Dividir em termos
        String[] termos = descricao.trim().toLowerCase().split("\\s+");
        
        // Construir SQL com score de relevância
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT p.*, r.NOME as nome_responsavel, s.DESCRICAO as nome_sala, ");
        
        // Calcular score de relevância
        sqlBuilder.append("(");
        for (int i = 0; i < termos.length; i++) {
            if (i > 0) sqlBuilder.append(" + ");
            sqlBuilder.append("(CASE WHEN LOWER(p.DESCRICAO) LIKE ? THEN 10 ELSE 0 END)");
            sqlBuilder.append(" + (CASE WHEN LOWER(p.MARCA) LIKE ? THEN 5 ELSE 0 END)");
            sqlBuilder.append(" + (CASE WHEN LOWER(p.MODELO) LIKE ? THEN 5 ELSE 0 END)");
            sqlBuilder.append(" + (CASE WHEN LOWER(p.CATEGORIA) LIKE ? THEN 3 ELSE 0 END)");
        }
        sqlBuilder.append(") as relevancia ");
        
        sqlBuilder.append("FROM TABELA_PATRIMONIO p ");
        sqlBuilder.append("LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID ");
        sqlBuilder.append("LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA ");
        sqlBuilder.append("WHERE (");
        
        // Condições de busca
        for (int i = 0; i < termos.length; i++) {
            if (i > 0) sqlBuilder.append(" OR ");
            sqlBuilder.append("(LOWER(p.DESCRICAO) LIKE ? ");
            sqlBuilder.append("OR LOWER(p.MARCA) LIKE ? ");
            sqlBuilder.append("OR LOWER(p.MODELO) LIKE ? ");
            sqlBuilder.append("OR LOWER(p.CATEGORIA) LIKE ?)");
        }
        
        sqlBuilder.append(") ORDER BY relevancia DESC, p.DESCRICAO");
        
        // Preparar parâmetros
        List<Object> params = new ArrayList<>();
        
        // Parâmetros para relevância
        for (String termo : termos) {
            String termoBusca = "%" + termo + "%";
            params.add(termoBusca); // DESCRICAO
            params.add(termoBusca); // MARCA
            params.add(termoBusca); // MODELO
            params.add(termoBusca); // CATEGORIA
        }
        
        // Parâmetros para WHERE
        for (String termo : termos) {
            String termoBusca = "%" + termo + "%";
            params.add(termoBusca); // DESCRICAO
            params.add(termoBusca); // MARCA
            params.add(termoBusca); // MODELO
            params.add(termoBusca); // CATEGORIA
        }
        
        return executeQuery(sqlBuilder.toString(), params.toArray());
    }
    
    // ==================== MÉTODOS LEGADOS ADICIONAIS (COMPATIBILIDADE) ====================
    
    /**
     * @deprecated Use findAll() ou listarTodosComJoins() do BaseDAO
     */
    @Deprecated
    public List<Patrimonio> listarTodos() {
        try {
            return listarTodosComJoins();
        } catch (SQLException e) {
            System.err.println("Erro ao listar patrimônios: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * @deprecated Use findById() ou buscarPorIdComJoins()
     */
    @Deprecated
    public Patrimonio buscarPorId(Integer id) {
        try {
            return buscarPorIdComJoins(id);
        } catch (SQLException e) {
            System.err.println("Erro ao buscar patrimônio: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * @deprecated Use insert() do BaseDAO
     */
    @Deprecated
    public boolean inserirPatrimonio(Patrimonio patrimonio) {
        try {
            insert(patrimonio);
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao inserir patrimônio: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * @deprecated Use update() do BaseDAO
     */
    @Deprecated
    public boolean atualizarPatrimonio(Patrimonio patrimonio) {
        try {
            update(patrimonio);
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar patrimônio: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * @deprecated Use delete() do BaseDAO
     */
    @Deprecated
    public boolean excluirPatrimonio(Integer id) {
        try {
            delete(id);
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao excluir patrimônio: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Buscar IDs de patrimônios já coletados em um inventário
     * 
     * @param idInventario ID do inventário (null = inventário ativo)
     * @return lista de IDs de patrimônios coletados
     */
    public List<Integer> buscarPatrimoniosColetados(Integer idInventario) {
        List<Integer> idsColetados = new ArrayList<>();
        String sql;
        
        if (idInventario != null) {
            sql = "SELECT DISTINCT ID_PATRIMONIO FROM TABELA_COLETA WHERE ID_INVENTARIO = ?";
        } else {
            sql = "SELECT DISTINCT c.ID_PATRIMONIO FROM TABELA_COLETA c " +
                  "INNER JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID " +
                  "WHERE i.STATUS = 'ATIVO'";
        }
        
        Connection conn = null;
        try {
            conn = com.inventario.util.ConnectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                if (idInventario != null) {
                    stmt.setInt(1, idInventario);
                }
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        idsColetados.add(rs.getInt("ID_PATRIMONIO"));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar patrimônios coletados: " + e.getMessage());
        } finally {
            if (conn != null) {
                com.inventario.util.ConnectionManager.closeConnection(conn);
            }
        }
        
        return idsColetados;
    }
}
