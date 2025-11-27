package com.inventario.mobile.server.service;

import com.inventario.dao.ResponsavelDAO;
import com.inventario.model.Responsavel;
import com.inventario.mobile.server.dto.MobileResponsavelDTO;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço para operações de responsável mobile
 * 
 * CORREÇÃO 26/11/2025: Removido instância estática do DAO
 * Agora cria nova instância a cada chamada para evitar problemas de conexão fechada
 */
@Service
public class MobileResponsavelService {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileResponsavelService.class);
    
    public MobileResponsavelService() {
        // Não mantém instância do DAO - cria nova a cada chamada
        logger.info("MobileResponsavelService inicializado");
    }
    
    /**
     * Obtém uma nova instância do DAO para cada operação
     * Evita problemas de conexão fechada/stale
     */
    private ResponsavelDAO getResponsavelDAO() {
        return new ResponsavelDAO();
    }
    
    /**
     * Lista todos os responsáveis ativos
     * 
     * CORREÇÃO 26/11/2025: Cria nova instância do DAO a cada chamada
     * para evitar problemas de conexão fechada
     */
    public List<MobileResponsavelDTO> listarResponsaveis() throws SQLException {
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("INICIANDO LISTAGEM DE RESPONSÁVEIS");
        logger.info("═══════════════════════════════════════════════════════════");
        
        try {
            // Criar nova instância do DAO para esta operação
            ResponsavelDAO responsavelDAO = getResponsavelDAO();
            
            logger.info("Executando findAll() no ResponsavelDAO...");
            List<Responsavel> responsaveis = responsavelDAO.findAll();
            
            logger.info("findAll() retornou {} responsáveis", responsaveis.size());
            
            List<MobileResponsavelDTO> dtos = new ArrayList<>();
            
            for (Responsavel responsavel : responsaveis) {
                // Filtrar apenas os ativos (já filtrado no DAO, mas garantir)
                if (responsavel.isAtivo()) {
                    dtos.add(converterParaDTO(responsavel));
                    logger.debug("Responsável adicionado: ID={}, Nome={}", 
                        responsavel.getId(), responsavel.getNome());
                }
            }
            
            logger.info("✓ Encontrados {} responsáveis ativos", dtos.size());
            logger.info("═══════════════════════════════════════════════════════════");
            
            return dtos;
            
        } catch (SQLException e) {
            logger.error("❌ ERRO SQL ao listar responsáveis: {}", e.getMessage());
            logger.error("SQLState: {}, ErrorCode: {}", e.getSQLState(), e.getErrorCode());
            throw e;
        } catch (Exception e) {
            logger.error("❌ ERRO ao listar responsáveis: {}", e.getMessage(), e);
            throw new SQLException("Erro ao listar responsáveis: " + e.getMessage(), e);
        }
    }
    
    /**
     * Busca responsável por ID
     * 
     * CORREÇÃO 26/11/2025: Cria nova instância do DAO a cada chamada
     */
    public MobileResponsavelDTO buscarPorId(Integer id) throws SQLException {
        logger.info("Buscando responsável por ID: {}", id);
        
        try {
            // Criar nova instância do DAO para esta operação
            ResponsavelDAO responsavelDAO = getResponsavelDAO();
            
            Responsavel responsavel = responsavelDAO.findById(id);
            
            if (responsavel != null) {
                logger.info("✓ Responsável encontrado: {}", responsavel.getNome());
                return converterParaDTO(responsavel);
            }
            
            logger.warn("Responsável não encontrado com ID: {}", id);
            return null;
            
        } catch (SQLException e) {
            logger.error("❌ ERRO SQL ao buscar responsável {}: {}", id, e.getMessage());
            throw e;
        }
    }
    
    /**
     * Converte Responsavel para DTO
     */
    private MobileResponsavelDTO converterParaDTO(Responsavel responsavel) {
        MobileResponsavelDTO dto = new MobileResponsavelDTO();
        
        dto.setId(responsavel.getId());
        dto.setNome(responsavel.getNome());
        dto.setCpf(responsavel.getCpf());
        dto.setEmail(responsavel.getEmail());
        dto.setTelefone(responsavel.getTelefone());
        dto.setCargo(responsavel.getCargo());
        dto.setIdSetor(responsavel.getIdSetor());
        dto.setNomeSetor(responsavel.getNomeSetor());
        dto.setAtivo(responsavel.isAtivo());
        
        if (responsavel.getDataCadastro() != null) {
            dto.setDataCadastro(responsavel.getDataCadastro().toString());
        }
        
        return dto;
    }
}
