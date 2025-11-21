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
 */
@Repository
public class SalaInventarioDAO {
    
    /**
     * Cria ou atualiza um registro de sala-inventário
     */
    public boolean salvar(SalaInventario salaInventario) {
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
     */
    public SalaInventario buscarPorSalaEInventario(Integer idSala, Integer idInventario) {
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
     */
    public boolean finalizarColeta(Integer idSala, Integer idInventario, Integer idParticipante, String observacoes) {
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
     */
    public boolean reabrirColeta(Integer idSala, Integer idInventario) {
        SalaInventario salaInventario = buscarPorSalaEInventario(idSala, idInventario);
        
        if (salaInventario != null) {
            salaInventario.reabrirColeta();
            return salvar(salaInventario);
        }
        
        return false;
    }
    
    /**
     * Verifica se a coleta de uma sala está finalizada
     */
    public boolean isColetaFinalizada(Integer idSala, Integer idInventario) {
        SalaInventario salaInventario = buscarPorSalaEInventario(idSala, idInventario);
        return salaInventario != null && salaInventario.isColetaFinalizada();
    }
    
    /**
     * Inicia a coleta de uma sala
     */
    public boolean iniciarColeta(Integer idSala, Integer idInventario, Integer idParticipante) {
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
     */
    public boolean atualizarEstatisticas(Integer idSala, Integer idInventario, Integer totalItens, Integer itensSemEtiqueta) {
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
     */
    private SalaInventario criarSalaInventarioFromResultSet(ResultSet rs) throws SQLException {
        SalaInventario salaInventario = new SalaInventario();
        
        salaInventario.setIdSalaInventario(rs.getInt("ID_SALA_INVENTARIO"));
        salaInventario.setIdSala(rs.getInt("ID_SALA"));
        salaInventario.setIdInventario(rs.getInt("ID_INVENTARIO"));
        
        int idParticipante = rs.getInt("ID_PARTICIPANTE");
        if (!rs.wasNull()) {
            salaInventario.setIdParticipante(idParticipante);
        }
        
        salaInventario.setColetaFinalizada(rs.getBoolean("COLETA_FINALIZADA"));
        
        Timestamp dataInicio = rs.getTimestamp("DATA_INICIO_COLETA");
        if (dataInicio != null) {
            salaInventario.setDataInicioColeta(dataInicio.toLocalDateTime());
        }
        
        Timestamp dataFinalizacao = rs.getTimestamp("DATA_FINALIZACAO_COLETA");
        if (dataFinalizacao != null) {
            salaInventario.setDataFinalizacaoColeta(dataFinalizacao.toLocalDateTime());
        }
        
        salaInventario.setObservacoesFinalizacao(rs.getString("OBSERVACOES_FINALIZACAO"));
        salaInventario.setTotalItensColetados(rs.getInt("TOTAL_ITENS_COLETADOS"));
        salaInventario.setTotalItensSemEtiqueta(rs.getInt("TOTAL_ITENS_SEM_ETIQUETA"));
        salaInventario.setPercentualConclusao(rs.getBigDecimal("PERCENTUAL_CONCLUSAO"));
        salaInventario.setStatusColeta(rs.getString("STATUS_COLETA"));
        
        Timestamp dataCadastro = rs.getTimestamp("data_criacao");
        if (dataCadastro != null) {
            salaInventario.setDataCadastro(dataCadastro.toLocalDateTime());
        }
        
        Timestamp dataAtualizacao = rs.getTimestamp("data_atualizacao");
        if (dataAtualizacao != null) {
            salaInventario.setDataUltimaAtualizacao(dataAtualizacao.toLocalDateTime());
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
        
        // LEFT JOIN para incluir salas que ainda não têm registro de coleta
        // Filtra apenas salas que não estão finalizadas (COLETA_FINALIZADA = FALSE ou NULL)
        String sql = "SELECT DISTINCT s.* FROM TABELA_SALA s " +
                    "LEFT JOIN TABELA_SALA_INVENTARIO si ON s.ID_SALA = si.ID_SALA AND si.ID_INVENTARIO = ? " +
                    "WHERE s.ATIVO = TRUE " +
                    "AND (si.COLETA_FINALIZADA = FALSE OR si.COLETA_FINALIZADA IS NULL) " +
                    "ORDER BY s.NUMERO_SALA";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            
            System.out.println("DEBUG SalaInventarioDAO: Buscando salas abertas para inventário ID: " + idInventario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    com.inventario.model.Sala sala = new com.inventario.model.Sala();
                    sala.setIdSala(rs.getInt("ID_SALA"));
                    sala.setNumeroSala(rs.getString("NUMERO_SALA"));
                    sala.setDescricao(rs.getString("DESCRICAO"));
                    sala.setIdSetor(rs.getInt("ID_SETOR"));
                    sala.setAtivo(rs.getBoolean("ATIVO"));
                    
                    salasAbertas.add(sala);
                    count++;
                    System.out.println("DEBUG SalaInventarioDAO: Sala " + count + " - " + sala.getIdentificacaoCompleta());
                }
                System.out.println("DEBUG SalaInventarioDAO: Total de salas abertas encontradas: " + count);
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar salas abertas para coleta: " + e.getMessage());
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
        
        String sql = "SELECT * FROM TABELA_SALA WHERE ATIVO = TRUE ORDER BY NUMERO_SALA";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            System.out.println("DEBUG SalaInventarioDAO: Buscando TODAS as salas ativas");
            
            int count = 0;
            while (rs.next()) {
                com.inventario.model.Sala sala = new com.inventario.model.Sala();
                sala.setIdSala(rs.getInt("ID_SALA"));
                sala.setNumeroSala(rs.getString("NUMERO_SALA"));
                sala.setDescricao(rs.getString("DESCRICAO"));
                sala.setIdSetor(rs.getInt("ID_SETOR"));
                sala.setAtivo(rs.getBoolean("ATIVO"));
                
                salas.add(sala);
                count++;
                System.out.println("DEBUG SalaInventarioDAO: Sala " + count + " - " + sala.getIdentificacaoCompleta());
            }
            System.out.println("DEBUG SalaInventarioDAO: Total de salas ativas: " + count);
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar todas as salas ativas: " + e.getMessage());
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
        String sql = "SELECT STATUS_COLETA FROM TABELA_SALA_INVENTARIO WHERE ID_SALA = ? AND ID_INVENTARIO = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idSala);
            stmt.setInt(2, idInventario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("STATUS_COLETA");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar status da sala: " + e.getMessage());
        }
        
        return null;
    }
}
