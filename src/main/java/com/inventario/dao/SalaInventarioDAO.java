package com.inventario.dao;

import com.inventario.model.SalaInventario;
import com.inventario.util.DatabaseConnection;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

/**
 * DAO para gerenciar operações da tabela SALA_INVENTARIO
 * IMPORTANTE: A maioria dos métodos só funciona no PostgreSQL
 */
@Repository
public class SalaInventarioDAO {
    
    /**
     * Verifica se está usando SQLite (modo offline)
     * @param conn Conexão ativa
     * @return true se for SQLite, false se for PostgreSQL
     */
    private boolean isSQLite(Connection conn) throws SQLException {
        String dbType = conn.getMetaData().getDatabaseProductName().toLowerCase();
        return dbType.contains("sqlite");
    }
    
    /**
     * Cria ou atualiza um registro de sala-inventário
     * NOTA: Não funciona em modo offline (SQLite)
     */
    public boolean salvar(SalaInventario salaInventario) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (isSQLite(conn)) {
                System.out.println("SQLite detectado: salvar() não disponível em modo offline");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao verificar tipo de banco: " + e.getMessage());
            return false;
        }
        if (salaInventario.getIdSalaInventario() == null) {
            // Verifica se já existe um registro com a mesma sala e inventário
            SalaInventario existente = buscarPorSalaEInventario(
                salaInventario.getIdSala(), 
                salaInventario.getIdInventario()
            );
            
            if (existente != null) {
                // Se já existe, atualiza o registro existente
                salaInventario.setIdSalaInventario(existente.getIdSalaInventario());
                return atualizar(salaInventario);
            } else {
                // Se não existe, insere novo registro
                return inserir(salaInventario);
            }
        } else {
            return atualizar(salaInventario);
        }
    }
    
    /**
     * Insere um novo registro de sala-inventário
     */
    private boolean inserir(SalaInventario salaInventario) {
        String sql = "INSERT INTO TABELA_SALA_INVENTARIO (ID_SALA, ID_INVENTARIO, ID_PARTICIPANTE, " +
                    "COLETA_FINALIZADA, DATA_INICIO_COLETA, DATA_FINALIZACAO_COLETA, OBSERVACOES_FINALIZACAO, " +
                    "TOTAL_ITENS_COLETADOS, TOTAL_ITENS_SEM_ETIQUETA, PERCENTUAL_CONCLUSAO, STATUS_COLETA) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, salaInventario.getIdSala());
            stmt.setInt(2, salaInventario.getIdInventario());
            
            if (salaInventario.getIdParticipante() != null) {
                stmt.setInt(3, salaInventario.getIdParticipante());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            
            stmt.setBoolean(4, salaInventario.getColetaFinalizada());
            
            if (salaInventario.getDataInicioColeta() != null) {
                stmt.setTimestamp(5, Timestamp.valueOf(salaInventario.getDataInicioColeta()));
            } else {
                stmt.setNull(5, Types.TIMESTAMP);
            }
            
            if (salaInventario.getDataFinalizacaoColeta() != null) {
                stmt.setTimestamp(6, Timestamp.valueOf(salaInventario.getDataFinalizacaoColeta()));
            } else {
                stmt.setNull(6, Types.TIMESTAMP);
            }
            
            stmt.setString(7, salaInventario.getObservacoesFinalizacao());
            stmt.setInt(8, salaInventario.getTotalItensColetados());
            stmt.setInt(9, salaInventario.getTotalItensSemEtiqueta());
            stmt.setBigDecimal(10, salaInventario.getPercentualConclusao());
            stmt.setString(11, salaInventario.getStatusColeta());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        salaInventario.setIdSalaInventario(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Atualiza um registro existente de sala-inventário
     */
    private boolean atualizar(SalaInventario salaInventario) {
        String sql = "UPDATE TABELA_SALA_INVENTARIO SET ID_PARTICIPANTE = ?, COLETA_FINALIZADA = ?, " +
                    "DATA_INICIO_COLETA = ?, DATA_FINALIZACAO_COLETA = ?, OBSERVACOES_FINALIZACAO = ?, " +
                    "TOTAL_ITENS_COLETADOS = ?, TOTAL_ITENS_SEM_ETIQUETA = ?, PERCENTUAL_CONCLUSAO = ?, " +
                    "STATUS_COLETA = ? WHERE ID_SALA_INVENTARIO = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (salaInventario.getIdParticipante() != null) {
                stmt.setInt(1, salaInventario.getIdParticipante());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            
            stmt.setBoolean(2, salaInventario.getColetaFinalizada());
            
            if (salaInventario.getDataInicioColeta() != null) {
                stmt.setTimestamp(3, Timestamp.valueOf(salaInventario.getDataInicioColeta()));
            } else {
                stmt.setNull(3, Types.TIMESTAMP);
            }
            
            if (salaInventario.getDataFinalizacaoColeta() != null) {
                stmt.setTimestamp(4, Timestamp.valueOf(salaInventario.getDataFinalizacaoColeta()));
            } else {
                stmt.setNull(4, Types.TIMESTAMP);
            }
            
            stmt.setString(5, salaInventario.getObservacoesFinalizacao());
            stmt.setInt(6, salaInventario.getTotalItensColetados());
            stmt.setInt(7, salaInventario.getTotalItensSemEtiqueta());
            stmt.setBigDecimal(8, salaInventario.getPercentualConclusao());
            stmt.setString(9, salaInventario.getStatusColeta());
            stmt.setInt(10, salaInventario.getIdSalaInventario());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Busca um registro de sala-inventário por ID da sala e ID do inventário
     * NOTA: Não funciona em modo offline (SQLite)
     */
    public SalaInventario buscarPorSalaEInventario(Integer idSala, Integer idInventario) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (isSQLite(conn)) {
                System.out.println("SQLite detectado: buscarPorSalaEInventario() não disponível em modo offline");
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao verificar tipo de banco: " + e.getMessage());
            return null;
        }
        
        String sql = "SELECT * FROM TABELA_SALA_INVENTARIO WHERE ID_SALA = ? AND ID_INVENTARIO = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idSala);
            stmt.setInt(2, idInventario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return criarSalaInventarioFromResultSet(rs);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Busca um registro de sala-inventário por ID
     */
    public SalaInventario buscarPorId(Integer idSalaInventario) {
        String sql = "SELECT * FROM TABELA_SALA_INVENTARIO WHERE ID_SALA_INVENTARIO = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idSalaInventario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return criarSalaInventarioFromResultSet(rs);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Lista todas as salas-inventário de um inventário específico
     */
    public List<SalaInventario> listarPorInventario(Integer idInventario) {
        List<SalaInventario> lista = new ArrayList<>();
        String sql = "SELECT * FROM TABELA_SALA_INVENTARIO WHERE ID_INVENTARIO = ? ORDER BY ID_SALA";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(criarSalaInventarioFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return lista;
    }
    
    /**
     * Lista todas as salas-inventário de uma sala específica
     */
    public List<SalaInventario> listarPorSala(Integer idSala) {
        List<SalaInventario> lista = new ArrayList<>();
        String sql = "SELECT * FROM TABELA_SALA_INVENTARIO WHERE ID_SALA = ? ORDER BY ID_INVENTARIO DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idSala);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(criarSalaInventarioFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return lista;
    }
    
    /**
     * Lista salas-inventário por status
     */
    public List<SalaInventario> listarPorStatus(Integer idInventario, String status) {
        List<SalaInventario> lista = new ArrayList<>();
        String sql = "SELECT * FROM TABELA_SALA_INVENTARIO WHERE ID_INVENTARIO = ? AND STATUS_COLETA = ? ORDER BY ID_SALA";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            stmt.setString(2, status);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(criarSalaInventarioFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return lista;
    }
    
    /**
     * Finaliza a coleta de uma sala
     * NOTA: Não funciona em modo offline (SQLite)
     */
    public boolean finalizarColeta(Integer idSala, Integer idInventario, Integer idParticipante, String observacoes) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (isSQLite(conn)) {
                System.out.println("SQLite detectado: finalizarColeta() não disponível em modo offline");
                return true; // Em modo offline, retornar sucesso sem fazer nada
            }
        } catch (SQLException e) {
            System.err.println("Erro ao verificar tipo de banco: " + e.getMessage());
            return false;
        }
        
        SalaInventario salaInventario = buscarPorSalaEInventario(idSala, idInventario);
        
        if (salaInventario == null) {
            // Cria um novo registro se não existir
            salaInventario = new SalaInventario(idSala, idInventario);
            salaInventario.setIdParticipante(idParticipante);
        }
        
        salaInventario.finalizarColeta(observacoes);
        return salvar(salaInventario);
    }
    
    /**
     * Reabre a coleta de uma sala
     * NOTA: Não funciona em modo offline (SQLite)
     */
    public boolean reabrirColeta(Integer idSala, Integer idInventario) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (isSQLite(conn)) {
                System.out.println("SQLite detectado: reabrirColeta() não disponível em modo offline");
                return true; // Em modo offline, retornar sucesso sem fazer nada
            }
        } catch (SQLException e) {
            System.err.println("Erro ao verificar tipo de banco: " + e.getMessage());
            return false;
        }
        
        SalaInventario salaInventario = buscarPorSalaEInventario(idSala, idInventario);
        
        if (salaInventario != null) {
            salaInventario.reabrirColeta();
            return salvar(salaInventario);
        }
        
        return false;
    }
    
    /**
     * Verifica se a coleta de uma sala está finalizada
     * NOTA: Não funciona em modo offline (SQLite)
     */
    public boolean isColetaFinalizada(Integer idSala, Integer idInventario) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (isSQLite(conn)) {
                System.out.println("SQLite detectado: isColetaFinalizada() não disponível em modo offline");
                return false; // Em modo offline, considerar que nenhuma sala está finalizada
            }
        } catch (SQLException e) {
            System.err.println("Erro ao verificar tipo de banco: " + e.getMessage());
            return false;
        }
        
        SalaInventario salaInventario = buscarPorSalaEInventario(idSala, idInventario);
        return salaInventario != null && salaInventario.isColetaFinalizada();
    }
    
    /**
     * Inicia a coleta de uma sala
     * NOTA: Não funciona em modo offline (SQLite)
     */
    public boolean iniciarColeta(Integer idSala, Integer idInventario, Integer idParticipante) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (isSQLite(conn)) {
                System.out.println("SQLite detectado: iniciarColeta() não disponível em modo offline");
                return true; // Em modo offline, retornar sucesso sem fazer nada
            }
        } catch (SQLException e) {
            System.err.println("Erro ao verificar tipo de banco: " + e.getMessage());
            return false;
        }
        
        SalaInventario salaInventario = buscarPorSalaEInventario(idSala, idInventario);
        
        if (salaInventario == null) {
            // Cria um novo registro se não existir
            salaInventario = new SalaInventario(idSala, idInventario);
            salaInventario.setIdParticipante(idParticipante);
        }
        
        salaInventario.iniciarColeta();
        return salvar(salaInventario);
    }
    
    /**
     * Atualiza as estatísticas de coleta de uma sala
     * NOTA: Não funciona em modo offline (SQLite)
     */
    public boolean atualizarEstatisticas(Integer idSala, Integer idInventario, Integer totalItens, Integer itensSemEtiqueta) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (isSQLite(conn)) {
                System.out.println("SQLite detectado: atualizarEstatisticas() não disponível em modo offline");
                return true; // Em modo offline, retornar sucesso sem fazer nada
            }
        } catch (SQLException e) {
            System.err.println("Erro ao verificar tipo de banco: " + e.getMessage());
            return false;
        }
        
        SalaInventario salaInventario = buscarPorSalaEInventario(idSala, idInventario);
        
        if (salaInventario == null) {
            // Cria um novo registro se não existir
            salaInventario = new SalaInventario(idSala, idInventario);
        }
        
        salaInventario.setTotalItensColetados(totalItens);
        salaInventario.setTotalItensSemEtiqueta(itensSemEtiqueta);
        
        // Calcula percentual baseado em alguma lógica (pode ser customizada)
        if (totalItens > 0) {
            BigDecimal percentual = new BigDecimal(totalItens).multiply(new BigDecimal("10"));
            if (percentual.compareTo(new BigDecimal("100")) > 0) {
                percentual = new BigDecimal("100");
            }
            salaInventario.setPercentualConclusao(percentual);
        }
        
        return salvar(salaInventario);
    }
    
    /**
     * Conta quantas salas estão finalizadas em um inventário
     */
    public int contarSalasFinalizadas(Integer idInventario) {
        String sql = "SELECT COUNT(*) FROM TABELA_SALA_INVENTARIO WHERE ID_INVENTARIO = ? AND COLETA_FINALIZADA = true";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Exclui um registro de sala-inventário
     */
    public boolean excluir(Integer idSalaInventario) {
        String sql = "DELETE FROM TABELA_SALA_INVENTARIO WHERE ID_SALA_INVENTARIO = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idSalaInventario);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Cria um objeto SalaInventario a partir do ResultSet
     * Com tratamento robusto de timestamps para evitar erros de parsing
     */
    private SalaInventario criarSalaInventarioFromResultSet(ResultSet rs) throws SQLException {
        SalaInventario salaInventario = new SalaInventario();
        
        try {
            salaInventario.setIdSalaInventario(rs.getInt("ID_SALA_INVENTARIO"));
            salaInventario.setIdSala(rs.getInt("ID_SALA"));
            salaInventario.setIdInventario(rs.getInt("ID_INVENTARIO"));
            
            int idParticipante = rs.getInt("ID_PARTICIPANTE");
            if (!rs.wasNull()) {
                salaInventario.setIdParticipante(idParticipante);
            }
            
            salaInventario.setColetaFinalizada(rs.getBoolean("COLETA_FINALIZADA"));
            
            // Tratamento robusto de timestamps para SQLite e PostgreSQL
            try {
                Object dataInicioObj = rs.getObject("DATA_INICIO_COLETA");
                if (dataInicioObj != null) {
                    if (dataInicioObj instanceof String) {
                        // SQLite: converter String para Timestamp
                        Timestamp dataInicio = Timestamp.valueOf((String) dataInicioObj);
                        salaInventario.setDataInicioColeta(dataInicio.toLocalDateTime());
                    } else {
                        // PostgreSQL: já é Timestamp
                        Timestamp dataInicio = rs.getTimestamp("DATA_INICIO_COLETA");
                        if (dataInicio != null && !rs.wasNull()) {
                            salaInventario.setDataInicioColeta(dataInicio.toLocalDateTime());
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Aviso: Erro ao parsear DATA_INICIO_COLETA - " + e.getMessage());
                // Continua sem a data
            }
            
            try {
                Object dataFinalizacaoObj = rs.getObject("DATA_FINALIZACAO_COLETA");
                if (dataFinalizacaoObj != null) {
                    if (dataFinalizacaoObj instanceof String) {
                        // SQLite: converter String para Timestamp
                        Timestamp dataFinalizacao = Timestamp.valueOf((String) dataFinalizacaoObj);
                        salaInventario.setDataFinalizacaoColeta(dataFinalizacao.toLocalDateTime());
                    } else {
                        // PostgreSQL: já é Timestamp
                        Timestamp dataFinalizacao = rs.getTimestamp("DATA_FINALIZACAO_COLETA");
                        if (dataFinalizacao != null && !rs.wasNull()) {
                            salaInventario.setDataFinalizacaoColeta(dataFinalizacao.toLocalDateTime());
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Aviso: Erro ao parsear DATA_FINALIZACAO_COLETA - " + e.getMessage());
                // Continua sem a data
            }
            
            salaInventario.setObservacoesFinalizacao(rs.getString("OBSERVACOES_FINALIZACAO"));
            salaInventario.setTotalItensColetados(rs.getInt("TOTAL_ITENS_COLETADOS"));
            salaInventario.setTotalItensSemEtiqueta(rs.getInt("TOTAL_ITENS_SEM_ETIQUETA"));
            
            BigDecimal percentual = rs.getBigDecimal("PERCENTUAL_CONCLUSAO");
            if (percentual != null && !rs.wasNull()) {
                salaInventario.setPercentualConclusao(percentual);
            } else {
                salaInventario.setPercentualConclusao(BigDecimal.ZERO);
            }
            
            salaInventario.setStatusColeta(rs.getString("STATUS_COLETA"));
            
            // Tentar ler colunas de auditoria se existirem (com tratamento de erro)
            try {
                Object dataCadastroObj = rs.getObject("data_criacao");
                if (dataCadastroObj != null) {
                    if (dataCadastroObj instanceof String) {
                        // SQLite: converter String para Timestamp
                        Timestamp dataCadastro = Timestamp.valueOf((String) dataCadastroObj);
                        salaInventario.setDataCadastro(dataCadastro.toLocalDateTime());
                    } else {
                        // PostgreSQL: já é Timestamp
                        Timestamp dataCadastro = rs.getTimestamp("data_criacao");
                        if (dataCadastro != null && !rs.wasNull()) {
                            salaInventario.setDataCadastro(dataCadastro.toLocalDateTime());
                        }
                    }
                }
            } catch (Exception e) {
                // Coluna data_criacao não existe ou erro de parsing - ignorar silenciosamente
            }
            
            try {
                Object dataAtualizacaoObj = rs.getObject("data_atualizacao");
                if (dataAtualizacaoObj != null) {
                    if (dataAtualizacaoObj instanceof String) {
                        // SQLite: converter String para Timestamp
                        Timestamp dataAtualizacao = Timestamp.valueOf((String) dataAtualizacaoObj);
                        salaInventario.setDataUltimaAtualizacao(dataAtualizacao.toLocalDateTime());
                    } else {
                        // PostgreSQL: já é Timestamp
                        Timestamp dataAtualizacao = rs.getTimestamp("data_atualizacao");
                        if (dataAtualizacao != null && !rs.wasNull()) {
                            salaInventario.setDataUltimaAtualizacao(dataAtualizacao.toLocalDateTime());
                        }
                    }
                }
            } catch (Exception e) {
                // Coluna data_atualizacao não existe ou erro de parsing - ignorar silenciosamente
            }
            
        } catch (SQLException e) {
            System.err.println("ERRO ao criar SalaInventario do ResultSet: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-lançar para tratamento superior
        }
        
        return salaInventario;
    }
    
    /**
     * Busca todas as salas abertas (não finalizadas) para coleta em um inventário
     * Retorna salas vinculadas ao inventário que ainda não foram finalizadas
     * 
     * @param idInventario ID do inventário
     * @return Lista de salas abertas para coleta
     */
    public List<com.inventario.model.Sala> buscarSalasAbertasParaColeta(int idInventario) {
        List<com.inventario.model.Sala> salasAbertas = new ArrayList<>();
        
        System.out.println("=== INÍCIO buscarSalasAbertasParaColeta ===");
        System.out.println("Inventário ID: " + idInventario);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            
            // Detectar tipo de banco de dados
            String dbType = conn.getMetaData().getDatabaseProductName().toLowerCase();
            boolean isSQLite = dbType.contains("sqlite");
            
            System.out.println("Tipo de banco detectado: " + dbType + (isSQLite ? " (SQLite/Offline)" : " (PostgreSQL/Online)"));
            
            // Montar SQL de acordo com o tipo de banco
            String sql;
            if (isSQLite) {
                // SQLite: tabela SALA, campo ATIVA, sem TABELA_SALA_INVENTARIO
                sql = "SELECT DISTINCT s.ID_SALA, s.NUMERO_SALA, s.NOME_SALA as DESCRICAO, " +
                     "0 as ID_SETOR, s.ATIVA as ATIVO, " +
                     "NULL AS DATA_CADASTRO " +
                     "FROM SALA s " +
                     "WHERE s.ATIVA = 1 " +
                     "ORDER BY s.NUMERO_SALA";
            } else {
                // PostgreSQL: tabela TABELA_SALA, campo ATIVO, com TABELA_SALA_INVENTARIO
                // ALTERADO: Removido filtro de COLETA_FINALIZADA para mostrar TODAS as salas ativas
                sql = "SELECT DISTINCT s.ID_SALA, s.NUMERO_SALA, s.DESCRICAO, s.ID_SETOR, s.ATIVO, " +
                     "CAST(NULL AS TIMESTAMP) AS DATA_CADASTRO " +
                     "FROM TABELA_SALA s " +
                     "LEFT JOIN TABELA_SALA_INVENTARIO si ON s.ID_SALA = si.ID_SALA AND si.ID_INVENTARIO = ? " +
                     "WHERE s.ATIVO = TRUE " +
                     "ORDER BY s.NUMERO_SALA";
            }
            
            System.out.println("SQL preparado:");
            System.out.println(sql);
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            // Setar parâmetro apenas para PostgreSQL (SQLite não usa)
            if (!isSQLite) {
                stmt.setInt(1, idInventario);
                System.out.println("Parâmetro setado: idInventario = " + idInventario);
            } else {
                System.out.println("SQLite: sem parâmetros (carregando todas as salas ativas)");
            }
            
            System.out.println("Executando query...");
            
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("Query executada com sucesso, processando resultados...");
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.println("\n--- Processando sala " + count + " ---");
                    try {
                        // Log dos valores brutos do ResultSet
                        System.out.println("  ID_SALA: " + rs.getInt("ID_SALA"));
                        System.out.println("  NUMERO_SALA: " + rs.getString("NUMERO_SALA"));
                        System.out.println("  DESCRICAO: " + rs.getString("DESCRICAO"));
                        System.out.println("  ID_SETOR: " + rs.getInt("ID_SETOR"));
                        System.out.println("  ATIVO: " + rs.getBoolean("ATIVO"));
                        
                        // Tentar ler DATA_CADASTRO com tratamento especial
                        try {
                            Object dataCadastroObj = rs.getObject("DATA_CADASTRO");
                            System.out.println("  DATA_CADASTRO (Object): " + dataCadastroObj + " (tipo: " + (dataCadastroObj != null ? dataCadastroObj.getClass().getName() : "null") + ")");
                        } catch (Exception e) {
                            System.err.println("  DATA_CADASTRO: ERRO ao ler como Object - " + e.getMessage());
                        }
                        
                        System.out.println("  Criando objeto Sala...");
                        com.inventario.model.Sala sala = criarSalaMinimalFromResultSet(rs);
                        salasAbertas.add(sala);
                        System.out.println("  ✓ Sala criada: " + sala.getIdentificacaoCompleta());
                    } catch (Exception e) {
                        System.err.println("  ✗ ERRO ao processar sala " + count + ":");
                        System.err.println("     Tipo: " + e.getClass().getName());
                        System.err.println("     Mensagem: " + e.getMessage());
                        e.printStackTrace();
                        // Continua processando as outras salas
                    }
                }
                System.out.println("\n=== Total de salas processadas: " + count + " ===");
                System.out.println("=== Salas adicionadas à lista: " + salasAbertas.size() + " ===");
            }
            
        } catch (SQLException e) {
            System.err.println("ERRO CRÍTICO ao buscar salas abertas para coleta:");
            System.err.println("  Mensagem: " + e.getMessage());
            System.err.println("  SQLState: " + e.getSQLState());
            System.err.println("  ErrorCode: " + e.getErrorCode());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("ERRO INESPERADO ao buscar salas: " + e.getMessage());
            e.printStackTrace();
        }
        
        return salasAbertas;
    }
    
    /**
     * Busca TODAS as salas ativas do sistema (sem filtro de inventário)
     * Útil quando se quer permitir coleta em qualquer sala, independente de vínculo com inventário
     * 
     * @return Lista de todas as salas ativas
     */
    public List<com.inventario.model.Sala> buscarTodasSalasAtivas() {
        List<com.inventario.model.Sala> salas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            
            // Detectar tipo de banco de dados
            String dbType = conn.getMetaData().getDatabaseProductName().toLowerCase();
            boolean isSQLite = dbType.contains("sqlite");
            
            System.out.println("DEBUG SalaInventarioDAO: Buscando TODAS as salas ativas");
            System.out.println("DEBUG SalaInventarioDAO: Tipo de banco: " + dbType);
            
            // Montar SQL de acordo com o tipo de banco
            String sql;
            if (isSQLite) {
                // SQLite: tabela SALA, campo ATIVA
                sql = "SELECT ID_SALA, NUMERO_SALA, NOME_SALA as DESCRICAO, " +
                     "0 as ID_SETOR, ATIVA as ATIVO, " +
                     "NULL AS DATA_CADASTRO " +
                     "FROM SALA WHERE ATIVA = 1 ORDER BY NUMERO_SALA";
            } else {
                // PostgreSQL: tabela TABELA_SALA, campo ATIVO
                sql = "SELECT ID_SALA, NUMERO_SALA, DESCRICAO, ID_SETOR, ATIVO, " +
                     "CAST(NULL AS TIMESTAMP) AS DATA_CADASTRO " +
                     "FROM TABELA_SALA WHERE ATIVO = TRUE ORDER BY NUMERO_SALA";
            }
            
            System.out.println("DEBUG SalaInventarioDAO: SQL = " + sql);
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                try {
                    com.inventario.model.Sala sala = criarSalaMinimalFromResultSet(rs);
                    salas.add(sala);
                    count++;
                    System.out.println("DEBUG SalaInventarioDAO: Sala " + count + " - " + sala.getIdentificacaoCompleta());
                } catch (Exception e) {
                    System.err.println("ERRO ao processar sala individual: " + e.getMessage());
                    e.printStackTrace();
                    // Continua processando as outras salas
                }
            }
            System.out.println("DEBUG SalaInventarioDAO: Total de salas ativas: " + count);
            
        } catch (SQLException e) {
            System.err.println("ERRO CRÍTICO ao buscar todas as salas ativas:");
            System.err.println("  Mensagem: " + e.getMessage());
            System.err.println("  SQLState: " + e.getSQLState());
            System.err.println("  ErrorCode: " + e.getErrorCode());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("ERRO INESPERADO ao buscar salas: " + e.getMessage());
            e.printStackTrace();
        }
        
        return salas;
    }
    
    /**
     * Busca o status de coleta de uma sala em um inventário
     * 
     * @param idSala ID da sala
     * @param idInventario ID do inventário
     * @return Status da coleta ou null se não encontrado
     */
    public String buscarStatusSala(int idSala, int idInventario) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Detectar tipo de banco
            String dbType = conn.getMetaData().getDatabaseProductName().toLowerCase();
            boolean isSQLite = dbType.contains("sqlite");
            
            if (isSQLite) {
                // SQLite não tem TABELA_SALA_INVENTARIO, retornar null
                System.out.println("SQLite detectado: buscarStatusSala() não disponível em modo offline");
                return null;
            }
            
            // PostgreSQL: usar TABELA_SALA_INVENTARIO
            String sql = "SELECT STATUS_COLETA FROM TABELA_SALA_INVENTARIO WHERE ID_SALA = ? AND ID_INVENTARIO = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idSala);
                stmt.setInt(2, idInventario);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("STATUS_COLETA");
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar status da sala: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Cria um objeto Sala MINIMAL a partir do ResultSet
     * Usa APENAS os campos essenciais para evitar problemas com timestamps
     * 
     * @param rs ResultSet posicionado em uma linha válida
     * @return Objeto Sala preenchido com campos essenciais
     * @throws SQLException se houver erro ao ler dados do ResultSet
     */
    private com.inventario.model.Sala criarSalaMinimalFromResultSet(ResultSet rs) throws SQLException {
        com.inventario.model.Sala sala = new com.inventario.model.Sala();
        
        System.out.println("DEBUG criarSalaMinimalFromResultSet: Iniciando criação de Sala");
        
        try {
            // Campos ESSENCIAIS apenas - com tratamento individual
            try {
                int idSala = rs.getInt("ID_SALA");
                sala.setIdSala(idSala);
                System.out.println("  ✓ ID_SALA: " + idSala);
            } catch (SQLException e) {
                System.err.println("  ✗ ERRO ao ler ID_SALA: " + e.getMessage());
                throw e;
            }
            
            try {
                String numeroSala = rs.getString("NUMERO_SALA");
                sala.setNumeroSala(numeroSala);
                System.out.println("  ✓ NUMERO_SALA: " + numeroSala);
            } catch (SQLException e) {
                System.err.println("  ✗ ERRO ao ler NUMERO_SALA: " + e.getMessage());
                throw e;
            }
            
            try {
                String descricao = rs.getString("DESCRICAO");
                sala.setDescricao(descricao);
                System.out.println("  ✓ DESCRICAO: " + descricao);
            } catch (SQLException e) {
                System.err.println("  ✗ ERRO ao ler DESCRICAO: " + e.getMessage());
                throw e;
            }
            
            // ID do setor - pode ser NULL
            try {
                int idSetor = rs.getInt("ID_SETOR");
                if (!rs.wasNull()) {
                    sala.setIdSetor(idSetor);
                    System.out.println("  ✓ ID_SETOR: " + idSetor);
                } else {
                    System.out.println("  ✓ ID_SETOR: NULL");
                }
            } catch (SQLException e) {
                System.err.println("  ⚠ Aviso ao ler ID_SETOR: " + e.getMessage() + " - usando NULL");
            }
            
            // Ativo
            try {
                boolean ativo = rs.getBoolean("ATIVO");
                sala.setAtivo(ativo);
                System.out.println("  ✓ ATIVO: " + ativo);
            } catch (SQLException e) {
                System.err.println("  ✗ ERRO ao ler ATIVO: " + e.getMessage());
                throw e;
            }
            
            // DATA_CADASTRO - tratamento ULTRA seguro para SQLite e PostgreSQL
            System.out.println("  Tentando ler DATA_CADASTRO...");
            try {
                // Tentar ler como Object primeiro para ver o tipo
                Object dataCadastroObj = rs.getObject("DATA_CADASTRO");
                System.out.println("  DATA_CADASTRO (Object): " + dataCadastroObj + 
                    " (tipo: " + (dataCadastroObj != null ? dataCadastroObj.getClass().getName() : "null") + ")");
                
                if (dataCadastroObj != null) {
                    // Verificar se é String (SQLite) ou Timestamp (PostgreSQL)
                    if (dataCadastroObj instanceof String) {
                        // SQLite retorna TEXT - converter para Timestamp
                        String dataStr = (String) dataCadastroObj;
                        System.out.println("  DATA_CADASTRO é String (SQLite): " + dataStr);
                        try {
                            // Formato esperado: 'YYYY-MM-DD HH:MM:SS'
                            Timestamp dataCadastro = Timestamp.valueOf(dataStr);
                            sala.setDataCadastro(dataCadastro);
                            System.out.println("  ✓ DATA_CADASTRO convertido de String para Timestamp");
                        } catch (IllegalArgumentException e) {
                            System.err.println("  ⚠ Formato de data inválido: " + dataStr);
                            System.err.println("     Usando data padrão do construtor");
                        }
                    } else {
                        // PostgreSQL retorna Timestamp diretamente
                        Timestamp dataCadastro = rs.getTimestamp("DATA_CADASTRO");
                        sala.setDataCadastro(dataCadastro);
                        System.out.println("  ✓ DATA_CADASTRO lido como Timestamp (PostgreSQL)");
                    }
                } else {
                    System.out.println("  ✓ DATA_CADASTRO é NULL - usando data padrão do construtor");
                }
            } catch (SQLException e) {
                System.out.println("  ⚠ SQLException ao ler DATA_CADASTRO: " + e.getMessage());
                System.out.println("     SQLState: " + e.getSQLState());
                System.out.println("     ErrorCode: " + e.getErrorCode());
                System.out.println("     Usando data padrão do construtor");
                // NÃO lançar exceção - apenas usar data padrão
            } catch (Exception e) {
                System.err.println("  ⚠ Exceção genérica ao ler DATA_CADASTRO: " + e.getClass().getName());
                System.err.println("     Mensagem: " + e.getMessage());
                System.err.println("     Usando data padrão do construtor");
                // NÃO lançar exceção - apenas usar data padrão
            }
            
            System.out.println("DEBUG criarSalaMinimalFromResultSet: Sala criada com sucesso");
            
        } catch (SQLException e) {
            System.err.println("ERRO ao criar Sala MINIMAL do ResultSet:");
            System.err.println("  ID_SALA: " + tryGetInt(rs, "ID_SALA"));
            System.err.println("  NUMERO_SALA: " + tryGetString(rs, "NUMERO_SALA"));
            System.err.println("  Mensagem: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        
        return sala;
    }
    
    /**
     * Cria um objeto Sala a partir do ResultSet
     * Com tratamento robusto para evitar erros de parsing de timestamps
     * 
     * @param rs ResultSet posicionado em uma linha válida
     * @return Objeto Sala preenchido
     * @throws SQLException se houver erro ao ler dados do ResultSet
     */
    private com.inventario.model.Sala criarSalaFromResultSet(ResultSet rs) throws SQLException {
        com.inventario.model.Sala sala = new com.inventario.model.Sala();
        
        try {
            // Campos obrigatórios
            sala.setIdSala(rs.getInt("ID_SALA"));
            sala.setNumeroSala(rs.getString("NUMERO_SALA"));
            sala.setDescricao(rs.getString("DESCRICAO"));
            
            // ID do setor
            int idSetor = rs.getInt("ID_SETOR");
            if (!rs.wasNull()) {
                sala.setIdSetor(idSetor);
            }
            
            // Ativo
            sala.setAtivo(rs.getBoolean("ATIVO"));
            
            // Campos opcionais
            try {
                Integer andar = rs.getInt("ANDAR");
                if (!rs.wasNull()) {
                    sala.setAndar(andar);
                }
            } catch (SQLException e) {
                // Coluna ANDAR não existe - ignorar
            }
            
            try {
                sala.setBloco(rs.getString("BLOCO"));
            } catch (SQLException e) {
                // Coluna BLOCO não existe - ignorar
            }
            
            try {
                Integer capacidade = rs.getInt("CAPACIDADE");
                if (!rs.wasNull()) {
                    sala.setCapacidade(capacidade);
                }
            } catch (SQLException e) {
                // Coluna CAPACIDADE não existe - ignorar
            }
            
            try {
                Double areaM2 = rs.getDouble("AREA_M2");
                if (!rs.wasNull()) {
                    sala.setAreaM2(areaM2);
                }
            } catch (SQLException e) {
                // Coluna AREA_M2 não existe - ignorar
            }
            
            try {
                sala.setTipoSala(rs.getString("TIPO_SALA"));
            } catch (SQLException e) {
                // Coluna TIPO_SALA não existe - ignorar
            }
            
            try {
                sala.setObservacoes(rs.getString("OBSERVACOES"));
            } catch (SQLException e) {
                // Coluna OBSERVACOES não existe - ignorar
            }
            
            // Timestamp - tratamento especial para SQLite e PostgreSQL
            try {
                Object dataCadastroObj = rs.getObject("DATA_CADASTRO");
                if (dataCadastroObj != null) {
                    if (dataCadastroObj instanceof String) {
                        // SQLite: converter String para Timestamp
                        Timestamp dataCadastro = Timestamp.valueOf((String) dataCadastroObj);
                        sala.setDataCadastro(dataCadastro);
                    } else {
                        // PostgreSQL: já é Timestamp
                        Timestamp dataCadastro = rs.getTimestamp("DATA_CADASTRO");
                        if (dataCadastro != null && !rs.wasNull()) {
                            sala.setDataCadastro(dataCadastro);
                        }
                    }
                }
            } catch (Exception e) {
                // Coluna DATA_CADASTRO não existe ou erro de parsing - ignorar
                // Sala já tem data padrão do construtor
            }
            
        } catch (SQLException e) {
            System.err.println("ERRO ao criar Sala do ResultSet: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        
        return sala;
    }
    
    /**
     * Método auxiliar para tentar obter um int do ResultSet sem lançar exceção
     */
    private String tryGetInt(ResultSet rs, String columnName) {
        try {
            return String.valueOf(rs.getInt(columnName));
        } catch (Exception e) {
            return "ERRO: " + e.getMessage();
        }
    }
    
    /**
     * Método auxiliar para tentar obter uma string do ResultSet sem lançar exceção
     */
    private String tryGetString(ResultSet rs, String columnName) {
        try {
            return rs.getString(columnName);
        } catch (Exception e) {
            return "ERRO: " + e.getMessage();
        }
    }
}
