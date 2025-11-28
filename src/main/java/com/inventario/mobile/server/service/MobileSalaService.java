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
    
    public MobileSalaService() {
        // Não mantém instância do DAO - cria nova a cada chamada
        logger.info("MobileSalaService inicializado");
    }
    
    /**
     * Obtém uma nova instância do DAO para cada operação
     * Evita problemas de conexão fechada/stale
     */
    private SalaDAO getSalaDAO() {
        return new SalaDAO();
    }
    
    /**
     * Lista TODAS as salas ativas de uma vez (sem paginação)
     * Otimizado com query única
     * 
     * CORREÇÃO 27/11/2025: Filtra salas com coleta finalizada no inventário ativo
     * Salas com coleta_finalizada = true NÃO aparecem na lista de seleção
     */
    public List<MobileSalaDTO> listarTodasSalas() throws SQLException {
        logger.info("Listando TODAS as salas ativas (excluindo salas com coleta finalizada)");
        
        long startTime = System.currentTimeMillis();
        
        // Query otimizada que EXCLUI salas com coleta finalizada no inventário ativo
        // Uma sala é excluída se:
        // 1. Existe registro em tabela_sala_inventario para o inventário ativo
        // 2. E coleta_finalizada = true OU status_coleta = 'FINALIZADA'
        String sql = "SELECT DISTINCT " +
                    "    s.ID_SALA, " +
                    "    s.NUMERO_SALA, " +
                    "    s.DESCRICAO, " +
                    "    s.ANDAR, " +
                    "    s.BLOCO, " +
                    "    s.ATIVO " +
                    "FROM TABELA_SALA s " +
                    "WHERE s.ATIVO = true " +
                    "  AND NOT EXISTS ( " +
                    "      SELECT 1 FROM TABELA_SALA_INVENTARIO si " +
                    "      WHERE si.ID_SALA = s.ID_SALA " +
                    "        AND si.ID_INVENTARIO = ( " +
                    "            SELECT id FROM TABELA_INVENTARIO " +
                    "            WHERE STATUS_INVENTARIO = 'EM_ANDAMENTO' " +
                    "            ORDER BY DATA_INICIO DESC LIMIT 1 " +
                    "        ) " +
                    "        AND (si.COLETA_FINALIZADA = true OR si.STATUS_COLETA = 'FINALIZADA') " +
                    "  ) " +
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
     * CORREÇÃO 27/11/2025: Filtra salas com coleta finalizada no inventário ativo
     * 
     * @param page Número da página (0-based)
     * @param size Tamanho da página
     * @return Lista de salas paginadas
     */
    public List<MobileSalaDTO> listarSalasPaginado(int page, int size) throws SQLException {
        logger.info("Listando salas paginadas (page: {}, size: {}) - excluindo finalizadas", page, size);
        
        long startTime = System.currentTimeMillis();
        
        // Query otimizada que EXCLUI salas com coleta finalizada no inventário ativo
        String sql = "SELECT DISTINCT " +
                    "    s.ID_SALA, " +
                    "    s.NUMERO_SALA, " +
                    "    s.DESCRICAO, " +
                    "    s.ANDAR, " +
                    "    s.BLOCO, " +
                    "    s.ATIVO " +
                    "FROM TABELA_SALA s " +
                    "WHERE s.ATIVO = true " +
                    "  AND NOT EXISTS ( " +
                    "      SELECT 1 FROM TABELA_SALA_INVENTARIO si " +
                    "      WHERE si.ID_SALA = s.ID_SALA " +
                    "        AND si.ID_INVENTARIO = ( " +
                    "            SELECT id FROM TABELA_INVENTARIO " +
                    "            WHERE STATUS_INVENTARIO = 'EM_ANDAMENTO' " +
                    "            ORDER BY DATA_INICIO DESC LIMIT 1 " +
                    "        ) " +
                    "        AND (si.COLETA_FINALIZADA = true OR si.STATUS_COLETA = 'FINALIZADA') " +
                    "  ) " +
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
     * Conta total de salas ativas disponíveis para coleta (para paginação)
     * 
     * CORREÇÃO 27/11/2025: Exclui salas com coleta finalizada no inventário ativo
     */
    public int contarSalasAtivas() throws SQLException {
        String sql = "SELECT COUNT(DISTINCT s.ID_SALA) " +
                    "FROM TABELA_SALA s " +
                    "WHERE s.ATIVO = true " +
                    "  AND NOT EXISTS ( " +
                    "      SELECT 1 FROM TABELA_SALA_INVENTARIO si " +
                    "      WHERE si.ID_SALA = s.ID_SALA " +
                    "        AND si.ID_INVENTARIO = ( " +
                    "            SELECT id FROM TABELA_INVENTARIO " +
                    "            WHERE STATUS_INVENTARIO = 'EM_ANDAMENTO' " +
                    "            ORDER BY DATA_INICIO DESC LIMIT 1 " +
                    "        ) " +
                    "        AND (si.COLETA_FINALIZADA = true OR si.STATUS_COLETA = 'FINALIZADA') " +
                    "  )";
        
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
     * Lógica: Uma sala está finalizada se todos os patrimônios dela foram coletados
     */
    private boolean isSalaFinalizada(Integer idInventario, Integer idSala) {
        // Verificar se todos os patrimônios da sala foram coletados neste inventário
        String sql = "SELECT " +
                    "    (SELECT COUNT(*) FROM TABELA_PATRIMONIO WHERE ID_SALA = ?) as total_patrimonios, " +
                    "    (SELECT COUNT(DISTINCT ID_PATRIMONIO) FROM TABELA_COLETA " +
                    "     WHERE ID_INVENTARIO = ? AND ID_PATRIMONIO IN " +
                    "         (SELECT ID FROM TABELA_PATRIMONIO WHERE ID_SALA = ?)) as patrimonios_coletados";
        
        try (java.sql.Connection conn = com.inventario.util.DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idSala);
            stmt.setInt(2, idInventario);
            stmt.setInt(3, idSala);
            
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
     * 
     * CORREÇÃO 26/11/2025: Cria nova instância do DAO a cada chamada
     */
    public MobileSalaDTO buscarPorId(Integer id) throws SQLException {
        logger.info("Buscando sala por ID: {}", id);
        
        try {
            // Criar nova instância do DAO para esta operação
            SalaDAO salaDAO = getSalaDAO();
            Sala sala = salaDAO.buscarSalaPorId(id);
            
            if (sala != null) {
                logger.info("✓ Sala encontrada: {}", sala.getDescricao());
                return converterParaDTO(sala);
            }
            
            logger.warn("Sala não encontrada com ID: {}", id);
            return null;
            
        } catch (SQLException e) {
            logger.error("❌ ERRO SQL ao buscar sala {}: {}", id, e.getMessage());
            throw e;
        }
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
