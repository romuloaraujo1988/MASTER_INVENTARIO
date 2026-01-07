package com.inventario.dao;

import com.inventario.model.DescricaoResumo;
import com.inventario.model.FotoReferencia;
import com.inventario.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operações de Foto de Referência no banco de dados.
 * 
 * Gerencia fotos de referência vinculadas a descrições normalizadas de patrimônios,
 * permitindo identificação visual no app mobile durante a coleta.
 * 
 * @author Sistema de Inventário
 * @since 2.9.0
 */
public class FotoReferenciaDAO extends BaseDAO<FotoReferencia, Integer> {
    
    private static final String TABLE_NAME = "tabela_foto_referencia";
    
    // SQL Statements
    private static final String INSERT_SQL = 
        "INSERT INTO " + TABLE_NAME + " (descricao_normalizada, imagem_blob, hash_imagem, " +
        "tamanho_bytes, data_cadastro, data_atualizacao, ativo, usuario_cadastro) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = 
        "UPDATE " + TABLE_NAME + " SET descricao_normalizada = ?, imagem_blob = ?, " +
        "hash_imagem = ?, tamanho_bytes = ?, data_atualizacao = ?, usuario_cadastro = ? " +
        "WHERE id = ?";
    
    private static final String SOFT_DELETE_SQL = 
        "UPDATE " + TABLE_NAME + " SET ativo = FALSE, data_atualizacao = CURRENT_TIMESTAMP " +
        "WHERE id = ?";
    
    private static final String FIND_BY_DESCRICAO_SQL = 
        "SELECT * FROM " + TABLE_NAME + " WHERE descricao_normalizada = ? AND ativo = TRUE";
    
    private static final String FIND_BY_DESCRICAO_LIKE_SQL = 
        "SELECT * FROM " + TABLE_NAME + " WHERE descricao_normalizada ILIKE ? AND ativo = TRUE";
    
    private static final String FIND_ALL_ATIVAS_SQL = 
        "SELECT * FROM " + TABLE_NAME + " WHERE ativo = TRUE ORDER BY descricao_normalizada";
    
    private static final String FIND_ATUALIZADAS_DESDE_SQL = 
        "SELECT * FROM " + TABLE_NAME + " WHERE data_atualizacao > ? " +
        "ORDER BY data_atualizacao LIMIT ? OFFSET ?";
    
    private static final String COUNT_ATUALIZADAS_DESDE_SQL = 
        "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE data_atualizacao > ?";
    
    private static final String DESCRICOES_UNICAS_SQL = 
        "SELECT p.descricao_normalizada, COUNT(*) as quantidade, " +
        "       CASE WHEN f.id IS NOT NULL THEN TRUE ELSE FALSE END as possui_foto, " +
        "       COALESCE(f.id, 0) as id_foto " +
        "FROM (SELECT DISTINCT descricao as descricao_normalizada FROM tabela_patrimonio " +
        "      WHERE descricao IS NOT NULL AND descricao != '') p " +
        "LEFT JOIN " + TABLE_NAME + " f ON f.descricao_normalizada = p.descricao_normalizada AND f.ativo = TRUE " +
        "LEFT JOIN tabela_patrimonio pat ON pat.descricao = p.descricao_normalizada " +
        "GROUP BY p.descricao_normalizada, f.id " +
        "ORDER BY p.descricao_normalizada";
    
    private static final String ESTATISTICAS_SQL = 
        "SELECT " +
        "  (SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE ativo = TRUE) as total_fotos, " +
        "  (SELECT COUNT(DISTINCT descricao) FROM tabela_patrimonio WHERE descricao IS NOT NULL) as total_descricoes, " +
        "  (SELECT COALESCE(SUM(tamanho_bytes), 0) FROM " + TABLE_NAME + " WHERE ativo = TRUE) as uso_armazenamento";
    
    // ========== Implementação dos métodos abstratos ==========
    
    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }
    
    @Override
    protected FotoReferencia mapResultSetToEntity(ResultSet rs) throws SQLException {
        FotoReferencia foto = new FotoReferencia();
        foto.setId(rs.getInt("id"));
        foto.setDescricaoNormalizada(rs.getString("descricao_normalizada"));
        foto.setImagemBlob(rs.getBytes("imagem_blob"));
        foto.setHashImagem(rs.getString("hash_imagem"));
        foto.setTamanhoBytes(rs.getInt("tamanho_bytes"));
        foto.setDataCadastro(rs.getTimestamp("data_cadastro"));
        foto.setDataAtualizacao(rs.getTimestamp("data_atualizacao"));
        foto.setAtivo(rs.getBoolean("ativo"));
        foto.setUsuarioCadastro(rs.getString("usuario_cadastro"));
        return foto;
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement stmt, FotoReferencia foto) throws SQLException {
        stmt.setString(1, foto.getDescricaoNormalizada());
        stmt.setBytes(2, foto.getImagemBlob());
        stmt.setString(3, foto.getHashImagem());
        stmt.setInt(4, foto.getTamanhoBytes());
        stmt.setTimestamp(5, foto.getDataCadastro());
        stmt.setTimestamp(6, foto.getDataAtualizacao());
        stmt.setBoolean(7, foto.isAtivo());
        stmt.setString(8, foto.getUsuarioCadastro());
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement stmt, FotoReferencia foto) throws SQLException {
        stmt.setString(1, foto.getDescricaoNormalizada());
        stmt.setBytes(2, foto.getImagemBlob());
        stmt.setString(3, foto.getHashImagem());
        stmt.setInt(4, foto.getTamanhoBytes());
        stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
        stmt.setString(6, foto.getUsuarioCadastro());
        stmt.setInt(7, foto.getId());
    }
    
    @Override
    protected String getInsertSQL() {
        return INSERT_SQL;
    }
    
    @Override
    protected String getUpdateSQL() {
        return UPDATE_SQL;
    }
    
    @Override
    protected void setGeneratedId(FotoReferencia foto, int id) {
        foto.setId(id);
    }
    
    // ========== Métodos específicos ==========
    
    /**
     * Busca foto de referência por descrição exata
     * 
     * @param descricao Descrição normalizada
     * @return FotoReferencia ou null se não encontrada
     */
    public FotoReferencia buscarPorDescricao(String descricao) throws SQLException {
        return executeQuerySingle(FIND_BY_DESCRICAO_SQL, descricao);
    }
    
    /**
     * Busca foto de referência por descrição parcial (LIKE)
     * 
     * @param descricao Parte da descrição
     * @return FotoReferencia ou null se não encontrada
     */
    public FotoReferencia buscarPorDescricaoLike(String descricao) throws SQLException {
        return executeQuerySingle(FIND_BY_DESCRICAO_LIKE_SQL, "%" + descricao + "%");
    }
    
    /**
     * Lista todas as fotos de referência ativas
     * 
     * @return Lista de fotos ativas
     */
    public List<FotoReferencia> listarAtivas() throws SQLException {
        return executeQuery(FIND_ALL_ATIVAS_SQL);
    }
    
    /**
     * Busca fotos atualizadas desde um timestamp (para delta sync)
     * 
     * @param desde Timestamp de referência
     * @param limite Máximo de registros
     * @param offset Offset para paginação
     * @return Lista de fotos atualizadas
     */
    public List<FotoReferencia> buscarAtualizadasDesde(Timestamp desde, int limite, int offset) throws SQLException {
        return executeQuery(FIND_ATUALIZADAS_DESDE_SQL, desde, limite, offset);
    }
    
    /**
     * Conta fotos atualizadas desde um timestamp
     * 
     * @param desde Timestamp de referência
     * @return Quantidade de fotos atualizadas
     */
    public int contarAtualizadasDesde(Timestamp desde) throws SQLException {
        Integer count = executeScalar(COUNT_ATUALIZADAS_DESDE_SQL, Integer.class, desde);
        return count != null ? count : 0;
    }
    
    /**
     * Exclui foto de referência (soft delete)
     * 
     * @param id ID da foto
     * @return Número de registros afetados
     */
    public int excluirSoft(int id) throws SQLException {
        return executeUpdate(SOFT_DELETE_SQL, id);
    }
    
    /**
     * Lista descrições únicas de patrimônios com informação de foto
     * 
     * @return Lista de resumos de descrições
     */
    public List<DescricaoResumo> listarDescricoesUnicas() throws SQLException {
        List<DescricaoResumo> resumos = new ArrayList<>();
        
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            
            try (PreparedStatement stmt = conn.prepareStatement(DESCRICOES_UNICAS_SQL);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    DescricaoResumo resumo = new DescricaoResumo();
                    resumo.setDescricaoNormalizada(rs.getString("descricao_normalizada"));
                    resumo.setQuantidadePatrimonios(rs.getInt("quantidade"));
                    resumo.setPossuiFoto(rs.getBoolean("possui_foto"));
                    resumo.setIdFotoReferencia(rs.getInt("id_foto"));
                    resumos.add(resumo);
                }
            }
        } finally {
            ConnectionManager.closeConnection(conn);
        }
        
        return resumos;
    }
    
    /**
     * Lista descrições únicas filtradas por texto
     * 
     * @param filtro Texto para filtrar
     * @return Lista de resumos filtrados
     */
    public List<DescricaoResumo> listarDescricoesUnicasFiltradas(String filtro) throws SQLException {
        List<DescricaoResumo> todas = listarDescricoesUnicas();
        
        if (filtro == null || filtro.trim().isEmpty()) {
            return todas;
        }
        
        String filtroLower = filtro.toLowerCase().trim();
        List<DescricaoResumo> filtradas = new ArrayList<>();
        
        for (DescricaoResumo resumo : todas) {
            if (resumo.getDescricaoNormalizada() != null && 
                resumo.getDescricaoNormalizada().toLowerCase().contains(filtroLower)) {
                filtradas.add(resumo);
            }
        }
        
        return filtradas;
    }
    
    /**
     * Obtém estatísticas de fotos de referência
     * 
     * @return Array com [totalFotos, totalDescricoes, usoArmazenamento]
     */
    public long[] obterEstatisticas() throws SQLException {
        long[] stats = new long[3];
        
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            
            try (PreparedStatement stmt = conn.prepareStatement(ESTATISTICAS_SQL);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    stats[0] = rs.getLong("total_fotos");
                    stats[1] = rs.getLong("total_descricoes");
                    stats[2] = rs.getLong("uso_armazenamento");
                }
            }
        } finally {
            ConnectionManager.closeConnection(conn);
        }
        
        return stats;
    }
    
    /**
     * Salva ou atualiza foto de referência
     * Se já existe foto para a descrição, atualiza; senão, insere nova
     * 
     * @param foto Foto de referência
     */
    public void salvarOuAtualizar(FotoReferencia foto) throws SQLException {
        FotoReferencia existente = buscarPorDescricao(foto.getDescricaoNormalizada());
        
        if (existente != null) {
            foto.setId(existente.getId());
            update(foto);
        } else {
            insert(foto);
        }
    }
    
    /**
     * Verifica se existe foto para uma descrição
     * 
     * @param descricao Descrição normalizada
     * @return true se existe foto ativa
     */
    public boolean existeFotoParaDescricao(String descricao) throws SQLException {
        FotoReferencia foto = buscarPorDescricao(descricao);
        return foto != null;
    }
    
    /**
     * Conta total de fotos ativas
     * 
     * @return Quantidade de fotos ativas
     */
    public int contarAtivas() throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE ativo = TRUE";
        Integer count = executeScalar(sql, Integer.class);
        return count != null ? count : 0;
    }
    
    /**
     * Calcula uso total de armazenamento em bytes
     * 
     * @return Total de bytes usados
     */
    public long calcularUsoArmazenamento() throws SQLException {
        String sql = "SELECT COALESCE(SUM(tamanho_bytes), 0) FROM " + TABLE_NAME + " WHERE ativo = TRUE";
        Long total = executeScalar(sql, Long.class);
        return total != null ? total : 0L;
    }
}
