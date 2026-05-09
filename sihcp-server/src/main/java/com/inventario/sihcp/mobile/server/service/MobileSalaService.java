package com.inventario.sihcp.mobile.server.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.inventario.sihcp.dao.SalaDAO;
import com.inventario.sihcp.mobile.server.dto.MobileSalaComProgressoDTO;
import com.inventario.sihcp.mobile.server.dto.MobileSalaDTO;
import com.inventario.sihcp.model.Sala;

/**
 * Serviço para operações de sala mobile
 */
@Service
public class MobileSalaService {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileSalaService.class);
    
    public MobileSalaService() {
        // Não mantém instância do DAO - cria nova a cada chamada
        logger.debug("MobileSalaService inicializado");
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
        logger.debug("Listando salas ativas");
        
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
        
        try (java.sql.Connection conn = com.inventario.sihcp.util.DatabaseConnection.getConnection();
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
        logger.debug("Retornadas {} salas em {}ms", dtos.size(), (endTime - startTime));
        
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
        logger.debug("Listando salas paginadas (page: {}, size: {})", page, size);
        
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
        
        try (java.sql.Connection conn = com.inventario.sihcp.util.DatabaseConnection.getConnection();
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
        logger.debug("Retornadas {} salas em {}ms", dtos.size(), (endTime - startTime));
        
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
        
        try (java.sql.Connection conn = com.inventario.sihcp.util.DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        return 0;
    }
    
    /**
     * Lista todas as salas com estatísticas de progresso de coleta.
     * Retorna total de patrimônios, coletados, pendentes e percentual.
     * 
     * @param idInventario ID do inventário (null = inventário ativo)
     * @return Lista de salas com progresso
     */
    public List<MobileSalaComProgressoDTO> listarSalasComProgresso(Integer idInventario) throws SQLException {
        logger.info("Listando salas com progresso para inventário: {}", 
            idInventario != null ? idInventario : "ATIVO");
        
        long startTime = System.currentTimeMillis();
        
        // Se não informou inventário, buscar o ativo
        Integer idInv = idInventario;
        if (idInv == null) {
            idInv = buscarIdInventarioAtivo();
            if (idInv == null) {
                logger.warn("Nenhum inventário ativo encontrado");
                return new ArrayList<>();
            }
        }
        
        // Query que retorna salas com contagem de patrimônios e coletas
        String sql = """
            SELECT 
                s.ID_SALA,
                s.NUMERO_SALA,
                s.DESCRICAO,
                s.ANDAR,
                s.BLOCO,
                s.ATIVO,
                COUNT(DISTINCT p.ID) as total_patrimonios,
                COUNT(DISTINCT c.ID) as coletados
            FROM TABELA_SALA s
            LEFT JOIN TABELA_PATRIMONIO p ON p.ID_SALA = s.ID_SALA
            LEFT JOIN TABELA_COLETA c ON c.ID_PATRIMONIO = p.ID AND c.ID_INVENTARIO = ?
            WHERE s.ATIVO = true
            GROUP BY s.ID_SALA, s.NUMERO_SALA, s.DESCRICAO, s.ANDAR, s.BLOCO, s.ATIVO
            ORDER BY s.NUMERO_SALA, s.DESCRICAO
            """;
        
        List<MobileSalaComProgressoDTO> dtos = new ArrayList<>();
        
        try (java.sql.Connection conn = com.inventario.sihcp.util.DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInv);
            
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MobileSalaComProgressoDTO dto = new MobileSalaComProgressoDTO();
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
                    dto.setTotalPatrimonios(rs.getInt("total_patrimonios"));
                    dto.setColetados(rs.getInt("coletados"));
                    dto.calcularEstatisticas();
                    
                    dtos.add(dto);
                }
            }
        }
        
        long endTime = System.currentTimeMillis();
        logger.info("✓ Retornadas {} salas com progresso em {}ms", dtos.size(), (endTime - startTime));
        
        return dtos;
    }
    
    /**
     * Busca o ID do inventário ativo (EM_ANDAMENTO)
     */
    private Integer buscarIdInventarioAtivo() throws SQLException {
        String sql = "SELECT ID FROM TABELA_INVENTARIO WHERE STATUS_INVENTARIO = 'EM_ANDAMENTO' ORDER BY DATA_INICIO DESC LIMIT 1";
        
        try (java.sql.Connection conn = com.inventario.sihcp.util.DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("ID");
            }
        }
        return null;
    }
    
    /**
     * SINCRONIZAÇÃO INCREMENTAL: Busca salas modificadas após uma data.
     * Retorna salas criadas ou atualizadas após o timestamp informado.
     * 
     * @param lastSync Timestamp da última sincronização (milissegundos)
     * @return Lista de salas modificadas
     */
    public List<MobileSalaDTO> buscarSalasModificadasApos(Long lastSync) throws SQLException {
        logger.debug("Buscando salas modificadas após: {}", lastSync);
        
        long startTime = System.currentTimeMillis();
        
        // Converte timestamp para formato SQL
        java.sql.Timestamp timestamp = new java.sql.Timestamp(lastSync);
        
        // Query que busca salas modificadas após a data
        // Usa COALESCE para tratar casos onde DATA_ATUALIZACAO é null
        String sql = """
            SELECT DISTINCT 
                s.ID_SALA, 
                s.NUMERO_SALA, 
                s.DESCRICAO, 
                s.ANDAR, 
                s.BLOCO, 
                s.ATIVO,
                COALESCE(s.DATA_ATUALIZACAO, s.DATA_CADASTRO, NOW()) as ultima_modificacao
            FROM TABELA_SALA s
            WHERE s.ATIVO = true
              AND (
                  COALESCE(s.DATA_ATUALIZACAO, s.DATA_CADASTRO, NOW()) > ?
                  OR s.DATA_CADASTRO > ?
              )
            ORDER BY s.NUMERO_SALA, s.DESCRICAO
            """;
        
        List<MobileSalaDTO> dtos = new ArrayList<>();
        
        try (java.sql.Connection conn = com.inventario.sihcp.util.DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, timestamp);
            stmt.setTimestamp(2, timestamp);
            
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
        logger.debug("Retornadas {} salas modificadas em {}ms", dtos.size(), (endTime - startTime));
        
        return dtos;
    }
    
    /**
     * SINCRONIZAÇÃO INCREMENTAL: Busca IDs de salas inativadas após uma data.
     * Retorna IDs de salas que foram desativadas/fechadas após o timestamp.
     * 
     * @param lastSync Timestamp da última sincronização (milissegundos)
     * @return Lista de IDs de salas removidas/inativadas
     */
    public List<Integer> buscarSalasInativadasApos(Long lastSync) throws SQLException {
        logger.debug("Buscando salas inativadas após: {}", lastSync);
        
        long startTime = System.currentTimeMillis();
        
        // Converte timestamp para formato SQL
        java.sql.Timestamp timestamp = new java.sql.Timestamp(lastSync);
        
        // Query que busca salas inativadas após a data
        // Salas com ATIVO = false e DATA_ATUALIZACAO > lastSync
        String sql = """
            SELECT s.ID_SALA
            FROM TABELA_SALA s
            WHERE s.ATIVO = false
              AND COALESCE(s.DATA_ATUALIZACAO, NOW()) > ?
            ORDER BY s.ID_SALA
            """;
        
        List<Integer> ids = new ArrayList<>();
        
        try (java.sql.Connection conn = com.inventario.sihcp.util.DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, timestamp);
            
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("ID_SALA"));
                }
            }
        }
        
        long endTime = System.currentTimeMillis();
        logger.debug("Retornados {} IDs de salas inativadas em {}ms", ids.size(), (endTime - startTime));
        
        return ids;
    }
    
    /**
     * Busca sala por ID
     * 
     * CORREÇÃO 26/11/2025: Cria nova instância do DAO a cada chamada
     */
    public MobileSalaDTO buscarPorId(Integer id) throws SQLException {
        logger.debug("Buscando sala por ID: {}", id);
        
        try {
            // Criar nova instância do DAO para esta operação
            SalaDAO salaDAO = getSalaDAO();
            Sala sala = salaDAO.buscarSalaPorId(id);
            
            if (sala != null) {
                return converterParaDTO(sala);
            }
            
            logger.debug("Sala não encontrada com ID: {}", id);
            return null;
            
        } catch (SQLException e) {
            logger.error("Erro SQL ao buscar sala {}: {}", id, e.getMessage());
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
