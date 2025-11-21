package com.inventario.mobile.server.service;

import com.inventario.dao.SalaDAO;
import com.inventario.model.Sala;
import com.inventario.mobile.server.dto.MobileSalaDTO;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço para operações de sala mobile
 */
@Service
public class MobileSalaService {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileSalaService.class);
    
    private final SalaDAO salaDAO;
    
    public MobileSalaService() {
        this.salaDAO = new SalaDAO();
    }
    
    /**
     * Lista TODAS as salas ativas de uma vez (sem paginação)
     * Otimizado com query única
     */
    public List<MobileSalaDTO> listarTodasSalas() throws SQLException {
        logger.info("Listando TODAS as salas ativas");
        
        long startTime = System.currentTimeMillis();
        
        // Query otimizada com JOIN - busca TODAS as salas de uma vez
        String sql = "SELECT DISTINCT " +
                    "    s.ID_SALA, " +
                    "    s.NUMERO_SALA, " +
                    "    s.DESCRICAO, " +
                    "    s.ANDAR, " +
                    "    s.BLOCO, " +
                    "    s.ATIVO " +
                    "FROM TABELA_SALA s " +
                    "LEFT JOIN TABELA_SALA_INVENTARIO si ON s.ID_SALA = si.ID_SALA " +
                    "    AND si.ID_INVENTARIO = ( " +
                    "        SELECT ID FROM TABELA_INVENTARIO " +
                    "        WHERE STATUS_INVENTARIO = 'EM_ANDAMENTO' " +
                    "        ORDER BY DATA_INICIO DESC " +
                    "        LIMIT 1 " +
                    "    ) " +
                    "WHERE s.ATIVO = true " +
                    "    AND (si.STATUS_COLETA IS NULL OR si.STATUS_COLETA != 'FINALIZADA') " +
                    "ORDER BY s.NUMERO_SALA, s.DESCRICAO";
        
        List<MobileSalaDTO> dtos = new ArrayList<>();
        
        try (java.sql.Connection conn = com.inventario.util.DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                MobileSalaDTO dto = new MobileSalaDTO();
                dto.setId(rs.getInt("ID_SALA"));
                
                String numeroSala = rs.getString("NUMERO_SALA");
                String descricao = rs.getString("DESCRICAO");
                String nomeExibicao = (numeroSala != null && !numeroSala.trim().isEmpty()) 
                    ? numeroSala 
                    : descricao;
                
                dto.setNumeroSala(numeroSala);
                dto.setNome(nomeExibicao);
                dto.setDescricao(descricao);
                dto.setAndar(rs.getString("ANDAR"));
                dto.setBloco(rs.getString("BLOCO"));
                dto.setAtiva(rs.getBoolean("ATIVO"));
                
                dtos.add(dto);
            }
        }
        
        long endTime = System.currentTimeMillis();
        logger.info("✓ Retornadas {} salas em {}ms", dtos.size(), (endTime - startTime));
        
        return dtos;
    }
    
    /**
     * Lista salas ativas com paginação REAL (otimizado)
     * Performance: 15s → <500ms
     * 
     * @param page Número da página (0-based)
     * @param size Tamanho da página
     * @return Lista de salas paginadas
     */
    public List<MobileSalaDTO> listarSalasPaginado(int page, int size) throws SQLException {
        logger.info("Listando salas paginadas (page: {}, size: {})", page, size);
        
        long startTime = System.currentTimeMillis();
        
        // Query otimizada com JOIN e paginação no banco (1 única query!)
        String sql = "SELECT DISTINCT " +
                    "    s.ID_SALA, " +
                    "    s.NUMERO_SALA, " +
                    "    s.DESCRICAO, " +
                    "    s.ANDAR, " +
                    "    s.BLOCO, " +
                    "    s.ATIVO " +
                    "FROM TABELA_SALA s " +
                    "LEFT JOIN TABELA_SALA_INVENTARIO si ON s.ID_SALA = si.ID_SALA " +
                    "    AND si.ID_INVENTARIO = ( " +
                    "        SELECT ID FROM TABELA_INVENTARIO " +
                    "        WHERE STATUS_INVENTARIO = 'EM_ANDAMENTO' " +
                    "        ORDER BY DATA_INICIO DESC " +
                    "        LIMIT 1 " +
                    "    ) " +
                    "WHERE s.ATIVO = true " +
                    "    AND (si.STATUS_COLETA IS NULL OR si.STATUS_COLETA != 'FINALIZADA') " +
                    "ORDER BY s.NUMERO_SALA, s.DESCRICAO " +
                    "LIMIT ? OFFSET ?";
        
        List<MobileSalaDTO> dtos = new ArrayList<>();
        
        try (java.sql.Connection conn = com.inventario.util.DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, size);
            stmt.setInt(2, page * size);
            
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MobileSalaDTO dto = new MobileSalaDTO();
                    dto.setId(rs.getInt("ID_SALA"));
                    
                    String numeroSala = rs.getString("NUMERO_SALA");
                    String descricao = rs.getString("DESCRICAO");
                    String nomeExibicao = (numeroSala != null && !numeroSala.trim().isEmpty()) 
                        ? numeroSala 
                        : descricao;
                    
                    dto.setNumeroSala(numeroSala);
                    dto.setNome(nomeExibicao);
                    dto.setDescricao(descricao);
                    dto.setAndar(rs.getString("ANDAR"));
                    dto.setBloco(rs.getString("BLOCO"));
                    dto.setAtiva(rs.getBoolean("ATIVO"));
                    
                    dtos.add(dto);
                }
            }
        }
        
        long endTime = System.currentTimeMillis();
        logger.info("✓ Retornadas {} salas em {}ms", dtos.size(), (endTime - startTime));
        
        return dtos;
    }
    
    /**
     * Conta total de salas ativas (para paginação)
     */
    public int contarSalasAtivas() throws SQLException {
        String sql = "SELECT COUNT(DISTINCT s.ID_SALA) " +
                    "FROM TABELA_SALA s " +
                    "LEFT JOIN TABELA_SALA_INVENTARIO si ON s.ID_SALA = si.ID_SALA " +
                    "    AND si.ID_INVENTARIO = ( " +
                    "        SELECT ID FROM TABELA_INVENTARIO " +
                    "        WHERE STATUS_INVENTARIO = 'EM_ANDAMENTO' " +
                    "        ORDER BY DATA_INICIO DESC " +
                    "        LIMIT 1 " +
                    "    ) " +
                    "WHERE s.ATIVO = true " +
                    "    AND (si.STATUS_COLETA IS NULL OR si.STATUS_COLETA != 'FINALIZADA')";
        
        try (java.sql.Connection conn = com.inventario.util.DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        return 0;
    }
    
    /**
     * Verifica se uma sala está finalizada em um inventário
     */
    private boolean isSalaFinalizada(Integer idInventario, Integer idSala) {
        String sql = "SELECT COUNT(*) FROM TABELA_SALA_INVENTARIO " +
                    "WHERE ID_INVENTARIO = ? AND ID_SALA = ? AND STATUS_COLETA = 'FINALIZADA'";
        
        try (java.sql.Connection conn = com.inventario.util.DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            stmt.setInt(2, idSala);
            
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            logger.warn("Erro ao verificar se sala está finalizada: {}", e.getMessage());
            // Em caso de erro, não filtrar (retornar false para não bloquear)
            return false;
        }
        
        return false;
    }
    
    /**
     * Obtém o ID do inventário ativo
     */
    private Integer obterIdInventarioAtivo() {
        String sql = "SELECT ID FROM TABELA_INVENTARIO " +
                    "WHERE STATUS_INVENTARIO = 'EM_ANDAMENTO' " +
                    "ORDER BY DATA_INICIO DESC LIMIT 1";
        
        try (java.sql.Connection conn = com.inventario.util.DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("ID");
            }
        } catch (Exception e) {
            logger.warn("Erro ao obter inventário ativo: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Busca sala por ID
     */
    public MobileSalaDTO buscarPorId(Integer id) throws SQLException {
        logger.info("Buscando sala por ID: {}", id);
        
        Sala sala = salaDAO.buscarSalaPorId(id);
        
        if (sala != null) {
            return converterParaDTO(sala);
        }
        
        return null;
    }
    
    /**
     * Converte Sala para DTO
     */
    private MobileSalaDTO converterParaDTO(Sala sala) {
        MobileSalaDTO dto = new MobileSalaDTO();
        
        dto.setId(sala.getIdSala());
        
        // Usar NUMERO_SALA se disponível, senão usar DESCRICAO
        String numeroSala = sala.getNumeroSala();
        String nomeExibicao = (numeroSala != null && !numeroSala.trim().isEmpty()) 
            ? numeroSala 
            : sala.getDescricao();
        
        dto.setNumeroSala(numeroSala);
        dto.setNome(nomeExibicao); // Nome para exibição (NUMERO_SALA) - aparece em negrito
        dto.setDescricao(sala.getDescricao()); // Descrição completa - aparece embaixo em cinza
        dto.setAndar(sala.getAndar() != null ? sala.getAndar().toString() : null);
        dto.setBloco(sala.getBloco());
        dto.setAtiva(sala.isAtiva());
        
        return dto;
    }
}
