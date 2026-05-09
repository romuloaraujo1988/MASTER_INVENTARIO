package com.inventario.sihcp.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.inventario.sihcp.model.Patrimonio;

/**
 * DAO Refatorado para operações com Patrimônio
 * Usa BaseDAO para eliminar código duplicado
 * 
 * REDUÇÃO: ~500 linhas → ~200 linhas (-60%)
 */
@Repository
public class PatrimonioDAO extends BaseDAO<Patrimonio, Integer> {
    
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
               "ESTADO_CONSERVACAO, CATEGORIA, ED) " +
               "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }
    
    @Override
    protected String getUpdateSQL() {
        return "UPDATE TABELA_PATRIMONIO SET NUMERO = ?, STATUS = ?, DESCRICAO = ?, " +
               "ROTULOS = ?, ID_RESPONSAVEL = ?, VALOR_AQUISICAO = ?, VALOR_DEPRECIADO = ?, " +
               "NUMERO_NOTA_FISCAL = ?, NUMERO_SERIE = ?, MARCA = ?, MODELO = ?, " +
               "DATA_ENTRADA = ?, FORNECEDOR = ?, ID_SALA = ?, ESTADO_CONSERVACAO = ?, " +
               "CATEGORIA = ?, ED = ? WHERE ID = ?";
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
        stmt.setString(17, p.getEd());
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement stmt, Patrimonio p) throws SQLException {
        setInsertParameters(stmt, p);
        stmt.setInt(18, p.getId());
    }
    
    @Override
    protected Patrimonio mapResultSetToEntity(ResultSet rs) throws SQLException {
        Patrimonio p = new Patrimonio();
        
        // ID: compatível em ambos (minúsculo)
        try {
            p.setId(rs.getInt("ID"));
        } catch (SQLException e) {
            p.setId(rs.getInt("id"));
        }
        
        // NUMERO: PostgreSQL usa NUMERO, SQLite usa numero
        try {
            p.setNumero(rs.getString("NUMERO"));
        } catch (SQLException e) {
            p.setNumero(rs.getString("numero"));
        }
        
        // STATUS: PostgreSQL usa STATUS, SQLite usa situacao
        try {
            p.setStatus(rs.getString("STATUS"));
        } catch (SQLException e) {
            try {
                p.setStatus(rs.getString("situacao"));
            } catch (SQLException e2) {
                p.setStatus("ATIVO"); // Valor padrão
            }
        }
        
        // DESCRICAO: PostgreSQL usa DESCRICAO, SQLite usa descricao
        try {
            p.setDescricao(rs.getString("DESCRICAO"));
        } catch (SQLException e) {
            p.setDescricao(rs.getString("descricao"));
        }
        
        // ROTULOS: compatível em ambos
        try {
            p.setRotulos(rs.getString("ROTULOS"));
        } catch (SQLException e) {
            try {
                p.setRotulos(rs.getString("rotulos"));
            } catch (SQLException e2) {
                p.setRotulos(null);
            }
        }
        
        // ID_RESPONSAVEL: compatível em ambos
        try {
            p.setIdResponsavel(rs.getInt("ID_RESPONSAVEL"));
        } catch (SQLException e) {
            try {
                p.setIdResponsavel(rs.getInt("id_responsavel"));
            } catch (SQLException e2) {
                p.setIdResponsavel(0);
            }
        }
        
        // VALOR_AQUISICAO: PostgreSQL usa VALOR_AQUISICAO, SQLite usa valor
        try {
            p.setValorAquisicao(rs.getBigDecimal("VALOR_AQUISICAO"));
        } catch (SQLException e) {
            try {
                p.setValorAquisicao(rs.getBigDecimal("valor"));
            } catch (SQLException e2) {
                p.setValorAquisicao(null);
            }
        }
        
        // VALOR_DEPRECIADO: compatível em ambos
        try {
            p.setValorDepreciado(rs.getBigDecimal("VALOR_DEPRECIADO"));
        } catch (SQLException e) {
            try {
                p.setValorDepreciado(rs.getBigDecimal("valor_depreciado"));
            } catch (SQLException e2) {
                p.setValorDepreciado(null);
            }
        }
        
        // NUMERO_NOTA_FISCAL: compatível em ambos
        try {
            p.setNumeroNotaFiscal(rs.getString("NUMERO_NOTA_FISCAL"));
        } catch (SQLException e) {
            try {
                p.setNumeroNotaFiscal(rs.getString("numero_nota_fiscal"));
            } catch (SQLException e2) {
                p.setNumeroNotaFiscal(null);
            }
        }
        
        // NUMERO_SERIE: compatível em ambos
        try {
            p.setNumeroSerie(rs.getString("NUMERO_SERIE"));
        } catch (SQLException e) {
            try {
                p.setNumeroSerie(rs.getString("numero_serie"));
            } catch (SQLException e2) {
                p.setNumeroSerie(null);
            }
        }
        
        // MARCA: compatível em ambos
        try {
            p.setMarca(rs.getString("MARCA"));
        } catch (SQLException e) {
            try {
                p.setMarca(rs.getString("marca"));
            } catch (SQLException e2) {
                p.setMarca(null);
            }
        }
        
        // MODELO: compatível em ambos
        try {
            p.setModelo(rs.getString("MODELO"));
        } catch (SQLException e) {
            try {
                p.setModelo(rs.getString("modelo"));
            } catch (SQLException e2) {
                p.setModelo(null);
            }
        }
        
        // Tratamento robusto para DATA_ENTRADA (compatibilidade SQLite)
        try {
            Timestamp tsEntrada = rs.getTimestamp("DATA_ENTRADA");
            if (tsEntrada != null) {
                p.setDataEntrada(new java.sql.Date(tsEntrada.getTime()));
            }
        } catch (SQLException e) {
            // Fallback: tentar como Date direto
            try {
                p.setDataEntrada(rs.getDate("DATA_ENTRADA"));
            } catch (SQLException e2) {
                // Ignorar se não conseguir ler
                System.err.println("DEBUG: Não foi possível ler DATA_ENTRADA: " + e2.getMessage());
            }
        }
        
        // DATA_CARGA: compatível em ambos
        try {
            p.setDataCarga(rs.getTimestamp("DATA_CARGA"));
        } catch (SQLException e) {
            try {
                p.setDataCarga(rs.getTimestamp("data_carga"));
            } catch (SQLException e2) {
                p.setDataCarga(null);
            }
        }
        
        // FORNECEDOR: compatível em ambos
        try {
            p.setFornecedor(rs.getString("FORNECEDOR"));
        } catch (SQLException e) {
            try {
                p.setFornecedor(rs.getString("fornecedor"));
            } catch (SQLException e2) {
                p.setFornecedor(null);
            }
        }
        
        // ID_SALA: compatível em ambos
        try {
            p.setIdSala(rs.getInt("ID_SALA"));
        } catch (SQLException e) {
            try {
                p.setIdSala(rs.getInt("id_sala"));
            } catch (SQLException e2) {
                p.setIdSala(0);
            }
        }
        
        // ESTADO_CONSERVACAO: PostgreSQL retorna em minúsculas
        // Tentar minúsculo primeiro (padrão PostgreSQL), depois maiúsculo
        String estadoConservacao = null;
        try {
            estadoConservacao = rs.getString("estado_conservacao");
        } catch (SQLException e) {
            // Ignorar
        }
        if (estadoConservacao == null) {
            try {
                estadoConservacao = rs.getString("ESTADO_CONSERVACAO");
            } catch (SQLException e) {
                // Ignorar
            }
        }
        p.setEstadoConservacao(estadoConservacao);
        
        // CATEGORIA: compatível em ambos
        try {
            p.setCategoria(rs.getString("CATEGORIA"));
        } catch (SQLException e) {
            try {
                p.setCategoria(rs.getString("categoria"));
            } catch (SQLException e2) {
                p.setCategoria(null);
            }
        }
        
        // ED: compatível em ambos
        try {
            p.setEd(rs.getString("ED"));
        } catch (SQLException e) {
            try {
                p.setEd(rs.getString("ed"));
            } catch (SQLException e2) {
                p.setEd(null);
            }
        }
        
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
        System.out.println("DEBUG PatrimonioDAO: buscarPorNumero() chamado para número: " + numeroPatrimonio);
        
        // CORREÇÃO: Sempre usar PostgreSQL para operações do desktop/servidor
        // O modo offline SQLite é apenas para o app Android
        String sql = "SELECT p.*, r.NOME as nome_responsavel, s.DESCRICAO as nome_sala " +
              "FROM TABELA_PATRIMONIO p " +
              "LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID " +
              "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA " +
              "WHERE p.NUMERO = ?";
        
        Patrimonio resultado = executeQuerySingle(sql, numeroPatrimonio);
        System.out.println("DEBUG PatrimonioDAO: Resultado da busca: " + (resultado != null ? "ENCONTRADO" : "NÃO ENCONTRADO"));
        
        return resultado;
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
                    "WHERE p.ID_SALA = ? AND (p.STATUS IS NULL OR UPPER(p.STATUS) != 'BAIXADO' OR p.STATUS = '') ORDER BY p.NUMERO";
        
        return executeQuery(sql, idSala);
    }
    
    /**
     * Busca patrimônios por sala (ID) com paginação
     * 
     * @param idSala ID da sala
     * @param page número da página (0-indexed)
     * @param size tamanho da página
     * @return lista de patrimônios paginada
     */
    public List<Patrimonio> buscarPorSalaComPaginacao(int idSala, int page, int size) throws SQLException {
        String sql = "SELECT p.*, r.NOME as nome_responsavel, s.DESCRICAO as nome_sala " +
                    "FROM TABELA_PATRIMONIO p " +
                    "LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID " +
                    "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA " +
                    "WHERE p.ID_SALA = ? AND (p.STATUS IS NULL OR UPPER(p.STATUS) != 'BAIXADO' OR p.STATUS = '') " +
                    "ORDER BY p.NUMERO " +
                    "LIMIT ? OFFSET ?";
        
        return executeQuery(sql, idSala, size, page * size);
    }
    
    /**
     * Busca patrimônios por sala (nome)
     */
    public List<Patrimonio> buscarPorSala(String nomeSala) throws SQLException {
        String sql = "SELECT p.*, r.NOME as nome_responsavel, s.DESCRICAO as nome_sala " +
                    "FROM TABELA_PATRIMONIO p " +
                    "LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID " +
                    "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA " +
                    "WHERE s.DESCRICAO ILIKE ? AND (p.STATUS IS NULL OR UPPER(p.STATUS) != 'BAIXADO' OR p.STATUS = '') ORDER BY p.NUMERO";
        
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
     * Busca patrimônios por responsável (ID)
     */
    public List<Patrimonio> buscarPorResponsavel(int idResponsavel) throws SQLException {
        String sql = "SELECT p.*, r.NOME as nome_responsavel, s.DESCRICAO as nome_sala " +
                    "FROM TABELA_PATRIMONIO p " +
                    "LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID " +
                    "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA " +
                    "WHERE p.ID_RESPONSAVEL = ? " +
                    "ORDER BY p.NUMERO";
        
        return executeQuery(sql, idResponsavel);
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
     * OTIMIZADO: Busca patrimônios por responsável com filtro de coleta em uma única query.
     * Evita N+1 queries ao fazer JOIN com tabela de coletas.
     * 
     * INTEGRAÇÃO 13/01/2026: Considera também coletas de itens compostos.
     * Patrimônios que tiveram componentes coletados são considerados como coletados.
     * 
     * @param idResponsavel ID do responsável
     * @param idInventario ID do inventário para verificar coletas
     * @param coletado filtro: true=coletados, false=não coletados
     * @param page número da página (0-based)
     * @param size tamanho da página
     * @return lista de patrimônios filtrados e paginados
     */
    public List<Patrimonio> buscarPorResponsavelComFiltroColeta(int idResponsavel, int idInventario, boolean coletado, int page, int size) throws SQLException {
        String sql;
        
        if (coletado) {
            // Buscar patrimônios que FORAM coletados (EXISTS na tabela de coletas OU na coleta de componentes)
            sql = "SELECT p.*, r.NOME as nome_responsavel, s.DESCRICAO as nome_sala " +
                  "FROM TABELA_PATRIMONIO p " +
                  "LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID " +
                  "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA " +
                  "WHERE p.ID_RESPONSAVEL = ? " +
                  "AND (EXISTS (SELECT 1 FROM TABELA_COLETA c WHERE c.ID_PATRIMONIO = p.ID AND c.ID_INVENTARIO = ?) " +
                  "     OR EXISTS (SELECT 1 FROM tabela_item_composto ic " +
                  "                INNER JOIN tabela_coleta_componente cc ON ic.id = cc.id_item_composto " +
                  "                WHERE ic.id_patrimonio_principal = p.ID AND cc.id_inventario = ?)) " +
                  "ORDER BY p.NUMERO " +
                  "LIMIT ? OFFSET ?";
            return executeQuery(sql, idResponsavel, idInventario, idInventario, size, page * size);
        } else {
            // Buscar patrimônios que NÃO foram coletados (NOT EXISTS em ambas as tabelas)
            sql = "SELECT p.*, r.NOME as nome_responsavel, s.DESCRICAO as nome_sala " +
                  "FROM TABELA_PATRIMONIO p " +
                  "LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID " +
                  "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA " +
                  "WHERE p.ID_RESPONSAVEL = ? " +
                  "AND NOT EXISTS (SELECT 1 FROM TABELA_COLETA c WHERE c.ID_PATRIMONIO = p.ID AND c.ID_INVENTARIO = ?) " +
                  "AND NOT EXISTS (SELECT 1 FROM tabela_item_composto ic " +
                  "                INNER JOIN tabela_coleta_componente cc ON ic.id = cc.id_item_composto " +
                  "                WHERE ic.id_patrimonio_principal = p.ID AND cc.id_inventario = ?) " +
                  "ORDER BY p.NUMERO " +
                  "LIMIT ? OFFSET ?";
            return executeQuery(sql, idResponsavel, idInventario, idInventario, size, page * size);
        }
    }
    
    /**
     * OTIMIZADO: Conta patrimônios por responsável com filtro de coleta.
     * Usado para paginação.
     * 
     * INTEGRAÇÃO 13/01/2026: Considera também coletas de itens compostos.
     * 
     * @param idResponsavel ID do responsável
     * @param idInventario ID do inventário para verificar coletas
     * @param coletado filtro: true=coletados, false=não coletados
     * @return total de patrimônios que atendem ao filtro
     */
    public int contarPorResponsavelComFiltroColeta(int idResponsavel, int idInventario, boolean coletado) throws SQLException {
        String sql;
        
        if (coletado) {
            sql = "SELECT COUNT(*) FROM TABELA_PATRIMONIO p " +
                  "WHERE p.ID_RESPONSAVEL = ? " +
                  "AND (EXISTS (SELECT 1 FROM TABELA_COLETA c WHERE c.ID_PATRIMONIO = p.ID AND c.ID_INVENTARIO = ?) " +
                  "     OR EXISTS (SELECT 1 FROM tabela_item_composto ic " +
                  "                INNER JOIN tabela_coleta_componente cc ON ic.id = cc.id_item_composto " +
                  "                WHERE ic.id_patrimonio_principal = p.ID AND cc.id_inventario = ?))";
            Integer count = executeScalar(sql, Integer.class, idResponsavel, idInventario, idInventario);
            return count != null ? count : 0;
        } else {
            sql = "SELECT COUNT(*) FROM TABELA_PATRIMONIO p " +
                  "WHERE p.ID_RESPONSAVEL = ? " +
                  "AND NOT EXISTS (SELECT 1 FROM TABELA_COLETA c WHERE c.ID_PATRIMONIO = p.ID AND c.ID_INVENTARIO = ?) " +
                  "AND NOT EXISTS (SELECT 1 FROM tabela_item_composto ic " +
                  "                INNER JOIN tabela_coleta_componente cc ON ic.id = cc.id_item_composto " +
                  "                WHERE ic.id_patrimonio_principal = p.ID AND cc.id_inventario = ?)";
            Integer count = executeScalar(sql, Integer.class, idResponsavel, idInventario, idInventario);
            return count != null ? count : 0;
        }
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
     * Conta patrimônios por sala
     * 
     * @param idSala ID da sala
     * @return quantidade de patrimônios na sala
     */
    public int contarPatrimoniosPorSala(int idSala) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TABELA_PATRIMONIO WHERE ID_SALA = ? AND (STATUS IS NULL OR UPPER(STATUS) = 'ATIVO' OR STATUS = '')";
        Integer count = executeScalar(sql, Integer.class, idSala);
        return count != null ? count : 0;
    }
    
    /**
     * Conta patrimônios ativos
     */
    public int contarPatrimoniosAtivos() throws SQLException {
        String sql = "SELECT COUNT(*)::INTEGER FROM TABELA_PATRIMONIO WHERE (STATUS IS NULL OR UPPER(STATUS) = 'ATIVO' OR STATUS = '')";
        Integer count = executeScalar(sql, Integer.class);
        return count != null ? count : 0;
    }
    
    /**
     * Conta patrimônios a inventariar (Ativo + Pendente)
     * Total de patrimônios que devem ser encontrados no inventário:
     * - Ativo: 10.810 patrimônios em uso
     * - Pendente: 260 patrimônios aguardando regularização
     * Total: 11.070 patrimônios
     * 
     * Exclui patrimônios com status 'Baixado' (500) que não devem ser inventariados.
     */
    public int contarPatrimoniosAInventariar() throws SQLException {
        String sql = "SELECT COUNT(*)::INTEGER FROM TABELA_PATRIMONIO WHERE UPPER(STATUS) IN ('ATIVO', 'PENDENTE')";
        Integer count = executeScalar(sql, Integer.class);
        return count != null ? count : 0;
    }
    
    /**
     * Calcula valor total dos patrimônios ativos
     */
    public double calcularValorTotal() throws SQLException {
        String sql = "SELECT COALESCE(SUM(VALOR_AQUISICAO), 0) FROM TABELA_PATRIMONIO WHERE (STATUS IS NULL OR UPPER(STATUS) != 'BAIXADO' OR STATUS = '')";
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
     * Conta total de patrimônios
     * Usado para paginação no sync offline
     */
    public int contarTotalPatrimonios() throws SQLException {
        String sql = "SELECT COUNT(*) FROM TABELA_PATRIMONIO";
        Integer count = executeScalar(sql, Integer.class);
        return count != null ? count : 0;
    }
    
    /**
     * Busca patrimônios com paginação (LIMIT/OFFSET)
     * Usado para sync offline em partes para evitar sobrecarga de memória
     * 
     * @param page número da página (0-based)
     * @param size tamanho da página
     * @return lista de patrimônios da página
     */
    public List<Patrimonio> buscarPatrimoniosComPaginacao(int page, int size) throws SQLException {
        int offset = page * size;
        
        String sql = "SELECT p.*, r.NOME as nome_responsavel, s.DESCRICAO as nome_sala " +
                    "FROM TABELA_PATRIMONIO p " +
                    "LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID " +
                    "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA " +
                    "ORDER BY p.ID " +
                    "LIMIT ? OFFSET ?";
        
        return executeQuery(sql, size, offset);
    }
    
    /**
     * Busca por ID com joins
     */
    public Patrimonio buscarPorIdComJoins(int id) throws SQLException {
        // Detectar tipo de banco
        Connection conn = null;
        boolean isSQLite = false;
        
        try {
            conn = com.inventario.sihcp.util.ConnectionManager.getConnection();
            String dbUrl = conn.getMetaData().getURL();
            isSQLite = dbUrl.contains("jdbc:sqlite");
        } catch (SQLException e) {
            // Fallback para PostgreSQL
        }
        
        String sql;
        if (isSQLite) {
            // SQLite: usar tabelas local_* (sem responsavel pois não há FK na tabela)
            sql = "SELECT p.*, s.nome as nome_sala, NULL as nome_responsavel " +
                  "FROM local_patrimonio p " +
                  "LEFT JOIN local_sala s ON p.id_sala = s.id " +
                  "WHERE p.id = ?";
        } else {
            // PostgreSQL: usar TABELA_*
            sql = "SELECT p.*, r.NOME as nome_responsavel, s.DESCRICAO as nome_sala " +
                  "FROM TABELA_PATRIMONIO p " +
                  "LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID " +
                  "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA " +
                  "WHERE p.ID = ?";
        }
        
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
     * CORREÇÃO 27/11/2025: Corrigido nome da coluna STATUS_INVENTARIO (era STATUS)
     * 
     * @param idInventario ID do inventário (null = inventário ativo)
     * @return lista de IDs de patrimônios coletados
     */
    public List<Integer> buscarPatrimoniosColetados(Integer idInventario) {
        List<Integer> idsColetados = new ArrayList<>();
        String sql;
        
        if (idInventario != null) {
            // INTEGRAÇÃO 14/01/2026: Inclui patrimônios com componentes coletados (itens compostos)
            sql = """
                SELECT DISTINCT ID_PATRIMONIO FROM TABELA_COLETA WHERE ID_INVENTARIO = ?
                UNION
                SELECT DISTINCT ic.id_patrimonio_principal
                FROM tabela_item_composto ic
                INNER JOIN tabela_coleta_componente cc ON ic.id = cc.id_item_composto
                WHERE cc.id_inventario = ?
                """;
        } else {
            // Buscar do inventário ativo (EM_ANDAMENTO)
            sql = """
                SELECT DISTINCT c.ID_PATRIMONIO FROM TABELA_COLETA c 
                INNER JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID 
                WHERE i.STATUS_INVENTARIO = 'EM_ANDAMENTO'
                UNION
                SELECT DISTINCT ic.id_patrimonio_principal
                FROM tabela_item_composto ic
                INNER JOIN tabela_coleta_componente cc ON ic.id = cc.id_item_composto
                INNER JOIN TABELA_INVENTARIO i ON cc.id_inventario = i.ID
                WHERE i.STATUS_INVENTARIO = 'EM_ANDAMENTO'
                """;
        }
        
        Connection conn = null;
        try {
            conn = com.inventario.sihcp.util.ConnectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                if (idInventario != null) {
                    stmt.setInt(1, idInventario);
                    stmt.setInt(2, idInventario); // Para o segundo SELECT do UNION
                }
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        idsColetados.add(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar patrimônios coletados: " + e.getMessage());
        } finally {
            if (conn != null) {
                com.inventario.sihcp.util.ConnectionManager.closeConnection(conn);
            }
        }
        
        return idsColetados;
    }
    
    /**
     * Busca patrimônios por código parcial (para consulta quando etiqueta está danificada)
     * 
     * @param codigoParcial Parte do código do patrimônio
     * @param limit Quantidade máxima de resultados
     * @return Lista de patrimônios encontrados
     * @throws SQLException Se ocorrer erro na consulta
     */
    public List<Patrimonio> buscarPorCodigoParcial(String codigoParcial, int limit) throws SQLException {
        String sql = "SELECT p.*, s.NOME as SALA_NOME, r.NOME_COMPLETO as RESPONSAVEL_NOME " +
                    "FROM TABELA_PATRIMONIO p " +
                    "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA " +
                    "LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID " +
                    "WHERE p.NUMERO LIKE ? " +
                    "  AND (p.STATUS IS NULL OR UPPER(p.STATUS) != 'BAIXADO' OR p.STATUS = '') " +
                    "ORDER BY p.NUMERO " +
                    "LIMIT ?";
        
        List<Patrimonio> patrimonios = new ArrayList<>();
        
        try (Connection conn = com.inventario.sihcp.util.ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + codigoParcial + "%");
            stmt.setInt(2, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    patrimonios.add(mapResultSetToEntity(rs));
                }
            }
        }
        
        return patrimonios;
    }
    
    /**
     * Busca patrimônios por descrição (para consulta quando etiqueta está danificada)
     * 
     * @param descricao Parte da descrição do patrimônio
     * @param limit Quantidade máxima de resultados
     * @return Lista de patrimônios encontrados
     * @throws SQLException Se ocorrer erro na consulta
     */
    public List<Patrimonio> buscarPorDescricao(String descricao, int limit) throws SQLException {
        String sql = "SELECT p.*, s.NOME as SALA_NOME, r.NOME_COMPLETO as RESPONSAVEL_NOME " +
                    "FROM TABELA_PATRIMONIO p " +
                    "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA " +
                    "LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID " +
                    "WHERE UPPER(p.DESCRICAO) LIKE UPPER(?) " +
                    "  AND (p.STATUS IS NULL OR UPPER(p.STATUS) != 'BAIXADO' OR p.STATUS = '') " +
                    "ORDER BY p.DESCRICAO " +
                    "LIMIT ?";
        
        List<Patrimonio> patrimonios = new ArrayList<>();
        
        try (Connection conn = com.inventario.sihcp.util.ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + descricao + "%");
            stmt.setInt(2, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    patrimonios.add(mapResultSetToEntity(rs));
                }
            }
        }
        
        return patrimonios;
    }
    
    /**
     * Busca detalhes completos de um patrimônio incluindo histórico de coletas
     * 
     * @param patrimonioId ID do patrimônio
     * @return Patrimônio com todos os detalhes ou null se não encontrado
     * @throws SQLException Se ocorrer erro na consulta
     */
    public Patrimonio buscarDetalhesCompletos(Long patrimonioId) throws SQLException {
        String sql = "SELECT p.*, " +
                    "s.NOME as SALA_NOME, s.BLOCO as SALA_BLOCO, s.ANDAR as SALA_ANDAR, s.DESCRICAO as SALA_DESCRICAO, " +
                    "r.NOME_COMPLETO as RESPONSAVEL_NOME, r.MATRICULA as RESPONSAVEL_MATRICULA, " +
                    "r.EMAIL as RESPONSAVEL_EMAIL, " +
                    "st.NOME as SETOR_NOME " +
                    "FROM TABELA_PATRIMONIO p " +
                    "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA " +
                    "LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID " +
                    "LEFT JOIN TABELA_SETOR st ON r.ID_SETOR = st.ID " +
                    "WHERE p.ID = ?";
        
        try (Connection conn = com.inventario.sihcp.util.ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, patrimonioId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Busca avançada com múltiplos critérios
     * Permite buscar por termo geral combinado com filtros específicos
     * 
     * @param termo Termo de busca geral (código ou descrição)
     * @param salaId ID da sala (opcional)
     * @param responsavelId ID do responsável (opcional)
     * @param limit Quantidade máxima de resultados
     * @return Lista de patrimônios encontrados
     * @throws SQLException Se ocorrer erro na consulta
     */
    public List<Patrimonio> buscarAvancada(
            String termo, 
            Integer salaId, 
            Integer responsavelId, 
            int limit) throws SQLException {
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT p.*, s.NOME as SALA_NOME, r.NOME_COMPLETO as RESPONSAVEL_NOME ");
        sql.append("FROM TABELA_PATRIMONIO p ");
        sql.append("LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA ");
        sql.append("LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID ");
        sql.append("WHERE (p.STATUS IS NULL OR UPPER(p.STATUS) != 'BAIXADO' OR p.STATUS = '') ");
        
        List<Object> parametros = new ArrayList<>();
        
        // Busca por termo (código ou descrição)
        if (termo != null && !termo.trim().isEmpty()) {
            sql.append("AND (UPPER(p.NUMERO) LIKE UPPER(?) ");
            sql.append("OR UPPER(p.DESCRICAO) LIKE UPPER(?) ");
            sql.append("OR UPPER(p.MARCA) LIKE UPPER(?) ");
            sql.append("OR UPPER(p.MODELO) LIKE UPPER(?)) ");
            
            String termoLike = "%" + termo.trim() + "%";
            parametros.add(termoLike);
            parametros.add(termoLike);
            parametros.add(termoLike);
            parametros.add(termoLike);
        }
        
        // Filtro por sala
        if (salaId != null && salaId > 0) {
            sql.append("AND p.ID_SALA = ? ");
            parametros.add(salaId);
        }
        
        // Filtro por responsável
        if (responsavelId != null && responsavelId > 0) {
            sql.append("AND p.ID_RESPONSAVEL = ? ");
            parametros.add(responsavelId);
        }
        
        sql.append("ORDER BY p.NUMERO ");
        sql.append("LIMIT ?");
        parametros.add(limit);
        
        List<Patrimonio> patrimonios = new ArrayList<>();
        
        try (Connection conn = com.inventario.sihcp.util.ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            // Definir parâmetros
            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Patrimonio patrimonio = mapResultSetToEntity(rs);
                    patrimonios.add(patrimonio);
                }
            }
        }
        
        return patrimonios;
    }
    
    /**
     * Lista patrimônios com paginação (otimizado para mobile)
     * 
     * @param page número da página (0-based)
     * @param size tamanho da página
     * @return lista de patrimônios da página
     */
    public List<Patrimonio> listarComPaginacao(int page, int size) throws SQLException {
        String sql = "SELECT p.*, " +
                     "s.NOME as SALA_NOME, " +
                     "r.NOME as RESPONSAVEL_NOME " +
                     "FROM TABELA_PATRIMONIO p " +
                     "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA " +
                     "LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID " +
                     "WHERE (p.STATUS IS NULL OR UPPER(p.STATUS) != 'BAIXADO' OR p.STATUS = '') " +
                     "ORDER BY p.ID " +
                     "LIMIT ? OFFSET ?";
        
        List<Patrimonio> patrimonios = new ArrayList<>();
        
        try (Connection conn = com.inventario.sihcp.util.ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, size);
            stmt.setInt(2, page * size);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Patrimonio patrimonio = mapResultSetToEntity(rs);
                    
                    // Adicionar nome da sala e responsável
                    patrimonio.setNomeSala(rs.getString("SALA_NOME"));
                    patrimonio.setNomeResponsavel(rs.getString("RESPONSAVEL_NOME"));
                    
                    patrimonios.add(patrimonio);
                }
            }
        }
        
        return patrimonios;
    }
    
    /**
     * Busca múltiplos patrimônios por IDs em uma única query (otimização de performance)
     * 
     * @param ids lista de IDs dos patrimônios
     * @return lista de patrimônios encontrados
     * @throws SQLException em caso de erro no banco
     */
    public List<Patrimonio> buscarPorIds(List<Integer> ids) throws SQLException {
        if (ids == null || ids.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        
        String placeholders = String.join(",", java.util.Collections.nCopies(ids.size(), "?"));
        // CORREÇÃO: Usar alias 'nome_sala' e 'nome_responsavel' para compatibilidade com mapResultSetToEntity
        String sql = "SELECT p.*, " +
                     "COALESCE(s.DESCRICAO, s.NUMERO_SALA) as nome_sala, " +
                     "r.NOME as nome_responsavel " +
                     "FROM TABELA_PATRIMONIO p " +
                     "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA " +
                     "LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID " +
                     "WHERE p.ID IN (" + placeholders + ")";
        
        List<Patrimonio> patrimonios = new java.util.ArrayList<>();
        
        try (Connection conn = com.inventario.sihcp.util.ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < ids.size(); i++) {
                stmt.setInt(i + 1, ids.get(i));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Patrimonio p = mapResultSetToEntity(rs);
                    // Garantir que nomeSala seja populado
                    try {
                        String nomeSala = rs.getString("nome_sala");
                        if (nomeSala != null && !nomeSala.isEmpty()) {
                            p.setNomeSala(nomeSala);
                        }
                    } catch (SQLException e) {
                        // Ignorar se coluna não existir
                    }
                    patrimonios.add(p);
                }
            }
            
            System.out.println("DEBUG PatrimonioDAO.buscarPorIds: Buscados " + patrimonios.size() + " patrimônios em batch");
        }
        
        return patrimonios;
    }
    
    /**
     * OTIMIZAÇÃO IMPORTAÇÃO: Busca todos os números de patrimônio com seus IDs em uma única query.
     * Usado para pré-carregar em memória e evitar N SELECTs durante importação de Excel/CSV.
     *
     * @return mapa de número do patrimônio → ID
     */
    public Map<String, Integer> buscarTodosNumerosComId() throws SQLException {
        String sql = "SELECT ID, NUMERO FROM TABELA_PATRIMONIO WHERE NUMERO IS NOT NULL";
        Map<String, Integer> mapa = new HashMap<>();

        try (Connection conn = com.inventario.sihcp.util.ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                mapa.put(rs.getString("NUMERO"), rs.getInt("ID"));
            }
        }
        System.out.println("DEBUG PatrimonioDAO.buscarTodosNumerosComId: " + mapa.size() + " patrimônios carregados em memória");
        return mapa;
    }

    /**
     * MÉTODO OTIMIZADO: Busca descrições agrupadas diretamente no banco
     * 
     * PROBLEMA RESOLVIDO: Vazamento de memória ao carregar todos os patrimônios
     * ANTES: findAll() + stream().groupBy() em memória (milhares de objetos)
     * DEPOIS: GROUP BY no SQL (retorna apenas descrições únicas)
     * 
     * @return lista de mapas com descricao e quantidade
     */
    public List<java.util.Map<String, Object>> buscarDescricoesAgrupadas() throws SQLException {
        String sql = "SELECT DESCRICAO, COUNT(*) as quantidade " +
                    "FROM TABELA_PATRIMONIO " +
                    "WHERE DESCRICAO IS NOT NULL AND TRIM(DESCRICAO) != '' " +
                    "AND (STATUS IS NULL OR UPPER(STATUS) != 'BAIXADO' OR STATUS = '') " +
                    "GROUP BY DESCRICAO " +
                    "ORDER BY DESCRICAO";
        
        List<java.util.Map<String, Object>> resultado = new java.util.ArrayList<>();
        
        try (Connection conn = com.inventario.sihcp.util.ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                java.util.Map<String, Object> item = new java.util.HashMap<>();
                item.put("descricao", rs.getString("DESCRICAO"));
                item.put("quantidade", rs.getLong("quantidade"));
                resultado.add(item);
            }
        }
        
        return resultado;
    }
    
    /**
     * MÉTODO OTIMIZADO: Busca descrições agrupadas por termo
     * Usa LIKE no SQL ao invés de carregar tudo em memória
     */
    public List<java.util.Map<String, Object>> buscarDescricoesAgrupadasPorTermo(String termo) throws SQLException {
        String sql = "SELECT DESCRICAO, COUNT(*) as quantidade " +
                    "FROM TABELA_PATRIMONIO " +
                    "WHERE DESCRICAO IS NOT NULL AND TRIM(DESCRICAO) != '' " +
                    "AND (STATUS IS NULL OR UPPER(STATUS) != 'BAIXADO' OR STATUS = '') " +
                    "AND UPPER(DESCRICAO) LIKE UPPER(?) " +
                    "GROUP BY DESCRICAO " +
                    "ORDER BY quantidade DESC, DESCRICAO " +
                    "LIMIT 100";
        
        List<java.util.Map<String, Object>> resultado = new java.util.ArrayList<>();
        
        try (Connection conn = com.inventario.sihcp.util.ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + termo + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    java.util.Map<String, Object> item = new java.util.HashMap<>();
                    item.put("descricao", rs.getString("DESCRICAO"));
                    item.put("quantidade", rs.getLong("quantidade"));
                    resultado.add(item);
                }
            }
        }
        
        return resultado;
    }
    
    /**
     * MÉTODO: Busca patrimônios por query de texto livre
     * Busca por número, descrição ou nome da sala
     * 
     * @param query termo de busca (já em lowercase)
     * @param limit limite de resultados
     * @return lista de patrimônios encontrados
     */
    public List<Patrimonio> buscarPorQueryTexto(String query, int limit) throws SQLException {
        String sql = "SELECT p.*, " +
                    "COALESCE(s.DESCRICAO, s.NUMERO_SALA) as nome_sala, " +
                    "r.NOME as nome_responsavel " +
                    "FROM TABELA_PATRIMONIO p " +
                    "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA " +
                    "LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID " +
                    "WHERE (p.STATUS IS NULL OR UPPER(p.STATUS) != 'BAIXADO' OR p.STATUS = '') " +
                    "AND (" +
                    "    LOWER(p.NUMERO) LIKE ? " +
                    "    OR LOWER(p.DESCRICAO) LIKE ? " +
                    "    OR LOWER(COALESCE(s.DESCRICAO, s.NUMERO_SALA, '')) LIKE ? " +
                    "    OR LOWER(COALESCE(r.NOME, '')) LIKE ? " +
                    ") " +
                    "ORDER BY p.NUMERO " +
                    "LIMIT ?";
        
        List<Patrimonio> resultado = new ArrayList<>();
        String queryPattern = "%" + query + "%";
        
        try (Connection conn = com.inventario.sihcp.util.ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, queryPattern);
            stmt.setString(2, queryPattern);
            stmt.setString(3, queryPattern);
            stmt.setString(4, queryPattern);
            stmt.setInt(5, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Patrimonio p = mapResultSetToEntity(rs);
                    // Garantir que nomeSala e nomeResponsavel sejam populados
                    try {
                        String nomeSala = rs.getString("nome_sala");
                        if (nomeSala != null && !nomeSala.isEmpty()) {
                            p.setNomeSala(nomeSala);
                        }
                    } catch (SQLException e) {
                        // Ignorar se coluna não existir
                    }
                    try {
                        String nomeResponsavel = rs.getString("nome_responsavel");
                        if (nomeResponsavel != null && !nomeResponsavel.isEmpty()) {
                            p.setNomeResponsavel(nomeResponsavel);
                        }
                    } catch (SQLException e) {
                        // Ignorar se coluna não existir
                    }
                    resultado.add(p);
                }
            }
        }
        
        return resultado;
    }

    /**
     * MÉTODO OTIMIZADO: Busca descrições NÃO coletadas agrupadas
     * Usado para coleta por descrição (sem etiqueta)
     */
    public List<java.util.Map<String, Object>> buscarDescricoesNaoColetadasAgrupadas(Integer idInventario) throws SQLException {
        String sql = "SELECT p.DESCRICAO, COUNT(*) as quantidade " +
                    "FROM TABELA_PATRIMONIO p " +
                    "WHERE p.DESCRICAO IS NOT NULL AND TRIM(p.DESCRICAO) != '' " +
                    "AND (p.STATUS IS NULL OR UPPER(p.STATUS) != 'BAIXADO' OR p.STATUS = '') " +
                    "AND NOT EXISTS (" +
                    "    SELECT 1 FROM TABELA_COLETA c " +
                    "    WHERE c.ID_PATRIMONIO = p.ID AND c.ID_INVENTARIO = ?" +
                    ") " +
                    "GROUP BY p.DESCRICAO " +
                    "ORDER BY quantidade DESC, p.DESCRICAO " +
                    "LIMIT 200";
        
        List<java.util.Map<String, Object>> resultado = new java.util.ArrayList<>();
        
        try (Connection conn = com.inventario.sihcp.util.ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    java.util.Map<String, Object> item = new java.util.HashMap<>();
                    item.put("descricao", rs.getString("DESCRICAO"));
                    item.put("quantidade", rs.getLong("quantidade"));
                    resultado.add(item);
                }
            }
        }
        
        return resultado;
    }
}
