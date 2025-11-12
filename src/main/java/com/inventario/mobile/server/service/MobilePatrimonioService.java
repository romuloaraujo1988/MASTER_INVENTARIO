package com.inventario.mobile.server.service;

import com.inventario.dao.PatrimonioDAO;
import com.inventario.dao.SalaDAORefactored;
import com.inventario.dao.ColetaDAO;
import com.inventario.dao.InventarioDAO;
import com.inventario.model.Patrimonio;
import com.inventario.model.Inventario;
import com.inventario.mobile.server.dto.MobilePatrimonioDTO;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço para operações de patrimônio mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@Service
public class MobilePatrimonioService {
    
    private static final Logger logger = LoggerFactory.getLogger(MobilePatrimonioService.class);
    
    private final PatrimonioDAO patrimonioDAO;
    private final SalaDAORefactored salaDAO;
    private final ColetaDAO coletaDAO;
    private final InventarioDAO inventarioDAO;
    
    public MobilePatrimonioService() {
        this.patrimonioDAO = new PatrimonioDAO();
        this.salaDAO = new SalaDAORefactored();
        this.coletaDAO = new ColetaDAO();
        this.inventarioDAO = new InventarioDAO();
    }
    
    /**
     * Busca patrimônio por QR Code
     */
    public MobilePatrimonioDTO buscarPorQRCode(String qrCode) throws SQLException {
        logger.info("Buscando patrimônio por QR Code: {}", qrCode);
        
        // QR Code geralmente contém o número do patrimônio
        Patrimonio patrimonio = patrimonioDAO.buscarPorNumero(qrCode);
        
        if (patrimonio != null) {
            return converterParaDTO(patrimonio);
        }
        
        return null;
    }
    
    /**
     * Busca patrimônio por número
     */
    public MobilePatrimonioDTO buscarPorNumero(String numero) throws SQLException {
        logger.info("Buscando patrimônio por número: {}", numero);
        
        Patrimonio patrimonio = patrimonioDAO.buscarPorNumero(numero);
        
        if (patrimonio != null) {
            return converterParaDTO(patrimonio);
        }
        
        return null;
    }
    
    /**
     * Busca patrimônios por sala
     */
    public List<MobilePatrimonioDTO> buscarPorSala(Integer salaId) throws SQLException {
        logger.info("Buscando patrimônios da sala: {}", salaId);
        
        List<Patrimonio> patrimonios = patrimonioDAO.buscarPorSala(salaId);
        List<MobilePatrimonioDTO> dtos = new ArrayList<>();
        
        for (Patrimonio patrimonio : patrimonios) {
            dtos.add(converterParaDTO(patrimonio));
        }
        
        logger.info("Encontrados {} patrimônios na sala {}", dtos.size(), salaId);
        
        return dtos;
    }
    
    /**
     * Busca patrimônios por setor
     */
    public List<MobilePatrimonioDTO> buscarPorSetor(Integer setorId) throws SQLException {
        logger.info("Buscando patrimônios do setor: {}", setorId);
        
        // Buscar todas as salas
        List<com.inventario.model.Sala> todasSalas = salaDAO.listarSalas();
        List<MobilePatrimonioDTO> dtos = new ArrayList<>();
        
        // Filtrar salas do setor
        for (com.inventario.model.Sala sala : todasSalas) {
            if (sala.getIdSetor() != null && sala.getIdSetor().equals(setorId)) {
                List<Patrimonio> patrimonios = patrimonioDAO.buscarPorSala(sala.getIdSala());
                for (Patrimonio patrimonio : patrimonios) {
                    dtos.add(converterParaDTO(patrimonio));
                }
            }
        }
        
        logger.info("Encontrados {} patrimônios no setor {}", dtos.size(), setorId);
        
        return dtos;
    }
    
    /**
     * Lista patrimônios com paginação
     */
    public List<MobilePatrimonioDTO> listarPatrimonios(int page, int size) throws SQLException {
        logger.info("Listando patrimônios (page: {}, size: {})", page, size);
        
        // Buscar todos os patrimônios com joins
        List<Patrimonio> todosPatrimonios = patrimonioDAO.listarTodosComJoins();
        
        logger.info("Total de patrimônios no banco: {}", todosPatrimonios.size());
        
        List<MobilePatrimonioDTO> dtos = new ArrayList<>();
        
        // Aplicar paginação manual
        int start = page * size;
        int end = Math.min(start + size, todosPatrimonios.size());
        
        for (int i = start; i < end && i < todosPatrimonios.size(); i++) {
            dtos.add(converterParaDTO(todosPatrimonios.get(i)));
        }
        
        logger.info("Retornando {} patrimônios (página {}, total: {})", dtos.size(), page, todosPatrimonios.size());
        
        return dtos;
    }
    
    /**
     * Busca patrimônio por ID
     */
    public MobilePatrimonioDTO buscarPorId(Integer id) throws SQLException {
        logger.info("Buscando patrimônio por ID: {}", id);
        
        Patrimonio patrimonio = null;
        try {
            patrimonio = patrimonioDAO.findById(id);
        } catch (SQLException e) {
            logger.error("Erro ao buscar patrimônio por ID: {}", id, e);
            throw e;
        }
        
        if (patrimonio != null) {
            return converterParaDTO(patrimonio);
        }
        
        return null;
    }

    /**
     * Busca patrimônios por responsável com paginação e filtro de coleta
     */
    public List<MobilePatrimonioDTO> buscarPorResponsavel(Integer idResponsavel, int page, int size, Boolean coletado) throws SQLException {
        logger.info("Buscando patrimônios do responsável: {} (page: {}, size: {}, coletado: {})", idResponsavel, page, size, coletado);
        
        List<Patrimonio> patrimonios = patrimonioDAO.buscarPorResponsavelComPaginacao(idResponsavel, page, size);
        List<MobilePatrimonioDTO> dtos = new ArrayList<>();
        
        // Obter inventário ativo para verificar coletas
        Inventario inventarioAtivo = null;
        try {
            inventarioAtivo = inventarioDAO.buscarInventarioAtivo();
        } catch (Exception e) {
            logger.warn("Não foi possível obter inventário ativo: {}", e.getMessage());
        }
        
        for (Patrimonio patrimonio : patrimonios) {
            MobilePatrimonioDTO dto = converterParaDTO(patrimonio);
            
            // Aplicar filtro de coleta se especificado
            if (coletado != null && inventarioAtivo != null) {
                boolean patrimonioColetado = coletaDAO.coletaExiste(
                    inventarioAtivo.getId(), 
                    patrimonio.getId()
                );
                
                // Se o filtro não corresponde ao status, pular este patrimônio
                if (coletado.booleanValue() != patrimonioColetado) {
                    continue;
                }
            }
            
            dtos.add(dto);
        }
        
        logger.info("Encontrados {} patrimônios do responsável {} (após filtro de coleta)", dtos.size(), idResponsavel);
        
        return dtos;
    }

    /**
     * Conta total de patrimônios por responsável
     */
    public int contarPatrimoniosPorResponsavel(Integer idResponsavel) throws SQLException {
        logger.info("Contando patrimônios do responsável: {}", idResponsavel);
        
        int total = patrimonioDAO.contarPatrimoniosPorResponsavel(idResponsavel);
        
        logger.info("Total de patrimônios do responsável {}: {}", idResponsavel, total);
        
        return total;
    }
    
    // Método auxiliar para converter Patrimonio para DTO
    private MobilePatrimonioDTO converterParaDTO(Patrimonio patrimonio) {
        MobilePatrimonioDTO dto = new MobilePatrimonioDTO();
        
        dto.setId(Long.valueOf(patrimonio.getId()));
        dto.setCodigo(patrimonio.getNumero());
        dto.setDescricao(patrimonio.getDescricao());
        dto.setMarca(patrimonio.getMarca());
        dto.setModelo(patrimonio.getModelo());
        dto.setEstado(patrimonio.getEstadoConservacao());
        dto.setSalaId(patrimonio.getIdSala() > 0 ? Long.valueOf(patrimonio.getIdSala()) : null);
        dto.setSalaNome(patrimonio.getNomeSala());
        dto.setResponsavelId(patrimonio.getIdResponsavel() > 0 ? Long.valueOf(patrimonio.getIdResponsavel()) : null);
        dto.setResponsavelNome(patrimonio.getNomeResponsavel());
        dto.setQrCode(patrimonio.getNumero()); // QR Code é o número do patrimônio
        
        if (patrimonio.getValor() != null) {
            dto.setValor(patrimonio.getValor().doubleValue());
        }
        
        dto.setObservacoes(patrimonio.getObservacoes());
        
        // Verificar se o patrimônio foi coletado no inventário ativo
        boolean coletado = false;
        String coletadoPor = null;
        String dataColetaFormatada = null;
        
        try {
            Inventario inventarioAtivo = inventarioDAO.buscarInventarioAtivo();
            if (inventarioAtivo != null) {
                // Verificar se existe coleta
                coletado = coletaDAO.coletaExiste(inventarioAtivo.getId(), patrimonio.getId());
                
                // Se foi coletado, buscar informações de quem coletou
                if (coletado) {
                    List<com.inventario.model.Coleta> coletas = coletaDAO.buscarPorInventario(inventarioAtivo.getId());
                    
                    for (com.inventario.model.Coleta coleta : coletas) {
                        if (coleta.getIdPatrimonio() == patrimonio.getId()) {
                            coletadoPor = coleta.getNomeColetor();
                            dataColetaFormatada = coleta.getDataColetaFormatada();
                            break;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.warn("Erro ao verificar status de coleta para patrimônio {}: {}", patrimonio.getId(), e.getMessage());
        }
        
        dto.setColetado(coletado);
        dto.setColetadoPor(coletadoPor);
        dto.setDataColetaFormatada(dataColetaFormatada);
        
        return dto;
    }
}
