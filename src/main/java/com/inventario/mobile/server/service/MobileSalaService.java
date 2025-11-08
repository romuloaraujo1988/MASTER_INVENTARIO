package com.inventario.mobile.server.service;

import com.inventario.dao.SalaDAORefactored;
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
    
    private final SalaDAORefactored salaDAO;
    
    public MobileSalaService() {
        this.salaDAO = new SalaDAORefactored();
    }
    
    /**
     * Lista todas as salas ativas (excluindo salas com coleta finalizada)
     */
    public List<MobileSalaDTO> listarSalas() throws SQLException {
        logger.info("Listando todas as salas ativas (excluindo finalizadas)");
        
        List<Sala> salas = salaDAO.listarSalas();
        List<MobileSalaDTO> dtos = new ArrayList<>();
        
        // Obter inventário ativo
        Integer idInventarioAtivo = obterIdInventarioAtivo();
        
        for (Sala sala : salas) {
            if (sala.isAtiva()) {
                // Verificar se a sala não está finalizada no inventário ativo
                if (idInventarioAtivo == null || !isSalaFinalizada(idInventarioAtivo, sala.getIdSala())) {
                    dtos.add(converterParaDTO(sala));
                }
            }
        }
        
        logger.info("Encontradas {} salas ativas (excluindo finalizadas)", dtos.size());
        
        return dtos;
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
        dto.setNome(sala.getNumeroSala()); // Usar numeroSala como nome
        dto.setDescricao(sala.getDescricao());
        dto.setAndar(sala.getAndar() != null ? sala.getAndar().toString() : null); // Converter Integer para String
        dto.setBloco(sala.getBloco());
        dto.setAtiva(sala.isAtiva());
        
        return dto;
    }
}
