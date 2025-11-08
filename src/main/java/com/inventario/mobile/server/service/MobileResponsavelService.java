package com.inventario.mobile.server.service;

import com.inventario.dao.ResponsavelDAORefactored;
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
 */
@Service
public class MobileResponsavelService {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileResponsavelService.class);
    
    private final ResponsavelDAORefactored responsavelDAO;
    
    public MobileResponsavelService() {
        this.responsavelDAO = new ResponsavelDAORefactored();
    }
    
    /**
     * Lista todos os responsáveis ativos
     */
    public List<MobileResponsavelDTO> listarResponsaveis() throws SQLException {
        logger.info("Listando todos os responsáveis ativos");
        
        List<Responsavel> responsaveis = responsavelDAO.findAll();
        List<MobileResponsavelDTO> dtos = new ArrayList<>();
        
        for (Responsavel responsavel : responsaveis) {
            // Filtrar apenas os ativos
            if (responsavel.isAtivo()) {
                dtos.add(converterParaDTO(responsavel));
            }
        }
        
        logger.info("Encontrados {} responsáveis ativos", dtos.size());
        
        return dtos;
    }
    
    /**
     * Busca responsável por ID
     */
    public MobileResponsavelDTO buscarPorId(Integer id) throws SQLException {
        logger.info("Buscando responsável por ID: {}", id);
        
        Responsavel responsavel = responsavelDAO.findById(id);
        
        if (responsavel != null) {
            return converterParaDTO(responsavel);
        }
        
        return null;
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
