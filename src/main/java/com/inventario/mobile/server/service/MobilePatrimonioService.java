package com.inventario.mobile.server.service;

import com.inventario.dao.PatrimonioDAO;
import com.inventario.dao.SalaDAO;
import com.inventario.dao.ColetaDAO;
import com.inventario.dao.InventarioDAO;
import com.inventario.model.Patrimonio;
import com.inventario.model.Inventario;
import com.inventario.mobile.server.dto.MobilePatrimonioDTO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço para operações de patrimônio mobile
 * Com cache de alta performance para reduzir consultas ao banco
 * 
 * @author Sistema de Inventário
 * @version 1.1.0
 */
@Service
public class MobilePatrimonioService {
    
    private static final Logger logger = LoggerFactory.getLogger(MobilePatrimonioService.class);
    
    private final PatrimonioDAO patrimonioDAO;
    private final SalaDAO salaDAO;
    private final ColetaDAO coletaDAO;
    private final InventarioDAO inventarioDAO;
    
    public MobilePatrimonioService() {
        this.patrimonioDAO = new PatrimonioDAO();
        this.salaDAO = new SalaDAO();
        this.coletaDAO = new ColetaDAO();
        this.inventarioDAO = new InventarioDAO();
    }
    
    /**
     * Busca patrimônio por QR Code (com cache)
     */
    @Cacheable(value = "patrimonios", key = "#qrCode")
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
     * Busca patrimônio por número (com cache)
     */
    @Cacheable(value = "patrimonios", key = "#numero")
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
     * Lista patrimônios com paginação (OTIMIZADO)
     * Usa paginação no banco de dados ao invés de carregar tudo em memória
     */
    public List<MobilePatrimonioDTO> listarPatrimonios(int page, int size) throws SQLException {
        logger.info("═══════════════════════════════════════════");
        logger.info("LISTANDO PATRIMÔNIOS (OTIMIZADO)");
        logger.info("Page: {}, Size: {}", page, size);
        logger.info("═══════════════════════════════════════════");
        
        // Buscar patrimônios com paginação no banco (OTIMIZADO)
        List<Patrimonio> patrimonios = patrimonioDAO.listarComPaginacao(page, size);
        
        logger.info("✓ {} patrimônios retornados do banco (página {})", patrimonios.size(), page);
        
        List<MobilePatrimonioDTO> dtos = new ArrayList<>();
        
        for (Patrimonio patrimonio : patrimonios) {
            dtos.add(converterParaDTO(patrimonio));
        }
        
        logger.info("✓ {} DTOs convertidos e prontos para retornar", dtos.size());
        logger.info("═══════════════════════════════════════════");
        
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
     * 
     * @param idResponsavel ID do responsável
     * @param page número da página (0-based)
     * @param size tamanho da página
     * @param coletado filtro de coleta (true=coletados, false=não coletados, null=todos)
     * @return lista de patrimônios filtrados e paginados
     */
    public List<MobilePatrimonioDTO> buscarPorResponsavel(Integer idResponsavel, int page, int size, Boolean coletado) throws SQLException {
        logger.info("Buscando patrimônios do responsável {} (page: {}, size: {}, coletado: {})", idResponsavel, page, size, coletado);
        
        // Obter inventário ativo para verificar coletas
        Inventario inventarioAtivo = null;
        try {
            inventarioAtivo = inventarioDAO.buscarInventarioAtivo();
            if (inventarioAtivo != null) {
                logger.info("Inventário ativo encontrado: ID={}, Nome={}", inventarioAtivo.getId(), inventarioAtivo.getNome());
            } else {
                logger.warn("Nenhum inventário ativo encontrado");
            }
        } catch (Exception e) {
            logger.warn("Erro ao obter inventário ativo: {}", e.getMessage());
        }
        
        // Se há filtro de coleta, precisamos buscar todos e filtrar em memória
        // porque a paginação no DAO não considera o status de coleta
        List<Patrimonio> patrimonios;
        if (coletado != null) {
            // Buscar todos os patrimônios do responsável (sem paginação)
            patrimonios = patrimonioDAO.buscarPorResponsavel(idResponsavel);
            logger.info("Encontrados {} patrimônios do responsável {} no banco (sem filtro)", patrimonios.size(), idResponsavel);
        } else {
            // Se não há filtro, usar paginação no DAO (mais eficiente)
            patrimonios = patrimonioDAO.buscarPorResponsavelComPaginacao(idResponsavel, page, size);
            logger.info("Encontrados {} patrimônios do responsável {} (página {})", patrimonios.size(), idResponsavel, page);
        }
        
        List<MobilePatrimonioDTO> dtos = new ArrayList<>();
        
        // Processar cada patrimônio
        for (Patrimonio patrimonio : patrimonios) {
            // Verificar se foi coletado no inventário ativo
            boolean foiColetado = false;
            if (inventarioAtivo != null) {
                try {
                    foiColetado = coletaDAO.coletaExiste(inventarioAtivo.getId(), patrimonio.getId());
                } catch (Exception e) {
                    logger.warn("Erro ao verificar coleta do patrimônio {}: {}", patrimonio.getId(), e.getMessage());
                }
            }
            
            // Aplicar filtro de coleta se especificado
            if (coletado != null) {
                if (coletado && !foiColetado) {
                    continue; // Pular se queremos coletados mas não foi coletado
                }
                if (!coletado && foiColetado) {
                    continue; // Pular se queremos não coletados mas foi coletado
                }
            }
            
            // Converter para DTO
            MobilePatrimonioDTO dto = converterParaDTO(patrimonio);
            dto.setColetado(foiColetado);
            dtos.add(dto);
        }
        
        logger.info("Após filtro de coleta: {} patrimônios", dtos.size());
        
        // Se aplicamos filtro de coleta, precisamos paginar manualmente
        if (coletado != null) {
            int start = page * size;
            int end = Math.min(start + size, dtos.size());
            
            List<MobilePatrimonioDTO> paginados = new ArrayList<>();
            for (int i = start; i < end && i < dtos.size(); i++) {
                paginados.add(dtos.get(i));
            }
            
            logger.info("✓ Retornando {} patrimônios do responsável {} (página {}, total filtrado: {})", 
                    paginados.size(), idResponsavel, page, dtos.size());
            
            return paginados;
        }
        
        logger.info("✓ Retornando {} patrimônios do responsável {} (página {})", dtos.size(), idResponsavel, page);
        
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
    
    /**
     * Verifica se um patrimônio já foi coletado no inventário
     * 
     * @param numeroPatrimonio número do patrimônio
     * @param inventarioId ID do inventário (opcional, usa ativo se null)
     * @return informações sobre a coleta
     */
    public java.util.Map<String, Object> verificarSePatrimonioFoiColetado(String numeroPatrimonio, Integer inventarioId) throws SQLException {
        logger.info("Verificando se patrimônio {} foi coletado no inventário {}", numeroPatrimonio, inventarioId);
        
        // Buscar patrimônio
        Patrimonio patrimonio = patrimonioDAO.buscarPorNumero(numeroPatrimonio);
        if (patrimonio == null) {
            throw new IllegalArgumentException("Patrimônio não encontrado: " + numeroPatrimonio);
        }
        
        // Obter inventário
        Inventario inventario;
        if (inventarioId != null) {
            inventario = inventarioDAO.findById(inventarioId);
            if (inventario == null) {
                throw new IllegalArgumentException("Inventário não encontrado: " + inventarioId);
            }
        } else {
            inventario = inventarioDAO.buscarInventarioAtivo();
            if (inventario == null) {
                throw new IllegalArgumentException("Nenhum inventário ativo encontrado");
            }
        }
        
        // Verificar se foi coletado
        boolean coletado = coletaDAO.coletaExiste(inventario.getId(), patrimonio.getId());
        
        java.util.Map<String, Object> resultado = new java.util.HashMap<>();
        resultado.put("coletado", coletado);
        resultado.put("numeroPatrimonio", numeroPatrimonio);
        resultado.put("patrimonioId", patrimonio.getId());
        resultado.put("inventarioId", inventario.getId());
        resultado.put("inventarioNome", inventario.getNome());
        
        // Se foi coletado, buscar informações da coleta
        if (coletado) {
            try {
                List<com.inventario.model.Coleta> coletas = coletaDAO.buscarPorInventario(inventario.getId());
                for (com.inventario.model.Coleta coleta : coletas) {
                    if (coleta.getIdPatrimonio() == patrimonio.getId()) {
                        resultado.put("dataColeta", coleta.getDataColetaFormatada());
                        resultado.put("coletadoPor", coleta.getNomeColetor());
                        resultado.put("coletaId", coleta.getId());
                        resultado.put("observacoes", coleta.getObservacaoColeta());
                        resultado.put("localizacaoEncontrada", coleta.getLocalizacaoEncontrada());
                        resultado.put("estadoEncontrado", coleta.getEstadoEncontrado());
                        break;
                    }
                }
            } catch (Exception e) {
                logger.warn("Erro ao buscar detalhes da coleta: {}", e.getMessage());
            }
        }
        
        logger.info("Patrimônio {} {} coletado no inventário {}", 
                numeroPatrimonio, coletado ? "JÁ FOI" : "NÃO FOI", inventario.getId());
        
        return resultado;
    }
    
    /**
     * Valida um número de patrimônio antes de coletar
     * Verifica se existe, se está ativo, e retorna informações completas
     * 
     * @param numeroPatrimonio número do patrimônio
     * @return informações de validação
     */
    public java.util.Map<String, Object> validarPatrimonio(String numeroPatrimonio) throws SQLException {
        logger.info("Validando patrimônio: {}", numeroPatrimonio);
        
        java.util.Map<String, Object> resultado = new java.util.HashMap<>();
        
        // Buscar patrimônio
        Patrimonio patrimonio = patrimonioDAO.buscarPorNumero(numeroPatrimonio);
        
        if (patrimonio == null) {
            resultado.put("valido", false);
            resultado.put("motivo", "PATRIMONIO_NAO_ENCONTRADO");
            resultado.put("mensagem", "Patrimônio não encontrado no sistema");
            logger.warn("Patrimônio {} não encontrado", numeroPatrimonio);
            return resultado;
        }
        
        // Verificar se está ativo (verificar campo de status se existir)
        // Nota: Patrimonio pode não ter campo "estado" de ativo/inativo
        // Assumimos que se foi encontrado, está disponível para coleta
        boolean ativo = true; // Por padrão, patrimônios encontrados estão ativos
        
        // Se houver campo específico de status, descomentar:
        // boolean ativo = "ATIVO".equalsIgnoreCase(patrimonio.getStatus());
        
        if (!ativo) {
            resultado.put("valido", false);
            resultado.put("motivo", "PATRIMONIO_INATIVO");
            resultado.put("mensagem", "Patrimônio está inativo ou baixado");
            logger.warn("Patrimônio {} está inativo", numeroPatrimonio);
            return resultado;
        }
        
        // Verificar se já foi coletado no inventário ativo
        Inventario inventarioAtivo = inventarioDAO.buscarInventarioAtivo();
        boolean jaColetado = false;
        String coletadoPor = null;
        String dataColeta = null;
        
        if (inventarioAtivo != null) {
            jaColetado = coletaDAO.coletaExiste(inventarioAtivo.getId(), patrimonio.getId());
            
            if (jaColetado) {
                // Buscar informações de quem coletou
                List<com.inventario.model.Coleta> coletas = coletaDAO.buscarPorInventario(inventarioAtivo.getId());
                for (com.inventario.model.Coleta coleta : coletas) {
                    if (coleta.getIdPatrimonio() == patrimonio.getId()) {
                        coletadoPor = coleta.getNomeColetor();
                        dataColeta = coleta.getDataColetaFormatada();
                        break;
                    }
                }
            }
        }
        
        // Patrimônio válido
        resultado.put("valido", true);
        resultado.put("patrimonio", converterParaDTO(patrimonio));
        resultado.put("jaColetado", jaColetado);
        
        if (jaColetado) {
            resultado.put("avisoColeta", "Este patrimônio já foi coletado anteriormente");
            resultado.put("coletadoPor", coletadoPor);
            resultado.put("dataColeta", dataColeta);
        }
        
        logger.info("Patrimônio {} é válido (já coletado: {})", numeroPatrimonio, jaColetado);
        
        return resultado;
    }
    
    /**
     * Verifica se uma coleta seria duplicada
     * 
     * @param numeroPatrimonio número do patrimônio
     * @param inventarioId ID do inventário
     * @return informações sobre duplicação
     */
    public java.util.Map<String, Object> verificarDuplicataColeta(String numeroPatrimonio, Integer inventarioId) throws SQLException {
        logger.info("Verificando duplicata de coleta: patrimônio={}, inventário={}", numeroPatrimonio, inventarioId);
        
        java.util.Map<String, Object> resultado = new java.util.HashMap<>();
        
        // Buscar patrimônio
        Patrimonio patrimonio = patrimonioDAO.buscarPorNumero(numeroPatrimonio);
        if (patrimonio == null) {
            resultado.put("duplicado", false);
            resultado.put("motivo", "PATRIMONIO_NAO_ENCONTRADO");
            resultado.put("mensagem", "Patrimônio não encontrado");
            resultado.put("podeRegistrar", false);
            return resultado;
        }
        
        // Obter inventário
        Inventario inventario;
        if (inventarioId != null) {
            inventario = inventarioDAO.findById(inventarioId);
        } else {
            inventario = inventarioDAO.buscarInventarioAtivo();
        }
        
        if (inventario == null) {
            resultado.put("duplicado", false);
            resultado.put("motivo", "INVENTARIO_NAO_ENCONTRADO");
            resultado.put("mensagem", "Inventário não encontrado ou não há inventário ativo");
            resultado.put("podeRegistrar", false);
            return resultado;
        }
        
        // Verificar se já existe coleta
        boolean duplicado = coletaDAO.coletaExiste(inventario.getId(), patrimonio.getId());
        
        resultado.put("duplicado", duplicado);
        resultado.put("numeroPatrimonio", numeroPatrimonio);
        resultado.put("patrimonioId", patrimonio.getId());
        resultado.put("inventarioId", inventario.getId());
        resultado.put("podeRegistrar", !duplicado);
        
        if (duplicado) {
            resultado.put("mensagem", "Este patrimônio já foi coletado neste inventário");
            resultado.put("motivo", "COLETA_DUPLICADA");
            
            // Buscar informações da coleta existente
            try {
                List<com.inventario.model.Coleta> coletas = coletaDAO.buscarPorInventario(inventario.getId());
                for (com.inventario.model.Coleta coleta : coletas) {
                    if (coleta.getIdPatrimonio() == patrimonio.getId()) {
                        resultado.put("coletaExistente", new java.util.HashMap<String, Object>() {{
                            put("id", coleta.getId());
                            put("dataColeta", coleta.getDataColetaFormatada());
                            put("coletadoPor", coleta.getNomeColetor());
                            put("localizacao", coleta.getLocalizacaoEncontrada());
                            put("estado", coleta.getEstadoEncontrado());
                        }});
                        break;
                    }
                }
            } catch (Exception e) {
                logger.warn("Erro ao buscar coleta existente: {}", e.getMessage());
            }
            
            logger.warn("Coleta duplicada detectada: patrimônio {} já foi coletado no inventário {}", 
                    numeroPatrimonio, inventario.getId());
        } else {
            resultado.put("mensagem", "Patrimônio pode ser coletado");
            logger.info("Patrimônio {} pode ser coletado no inventário {}", numeroPatrimonio, inventario.getId());
        }
        
        return resultado;
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
        dto.setEd(patrimonio.getEd());
        dto.setNumeroNotaFiscal(patrimonio.getNumeroNotaFiscal());
        dto.setFornecedor(patrimonio.getFornecedor());
        
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
