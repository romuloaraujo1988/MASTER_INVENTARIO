package com.inventario.sihcp.mobile.server.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.inventario.sihcp.dao.ColetaDAO;
import com.inventario.sihcp.dao.InventarioDAO;
import com.inventario.sihcp.dao.PatrimonioDAO;
import com.inventario.sihcp.dao.SalaDAO;
import com.inventario.sihcp.mobile.server.dto.MobilePatrimonioDTO;
import com.inventario.sihcp.model.Inventario;
import com.inventario.sihcp.model.Patrimonio;

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
     * Busca patrimônio por QR Code (SEM CACHE para status de coleta sempre atualizado)
     * Inclui verificação se já foi coletado no inventário ativo
     * 
     * ⚠️ IMPORTANTE: Cache removido para garantir que o status de coleta seja sempre atual
     */
    public MobilePatrimonioDTO buscarPorQRCode(String qrCode) throws SQLException {
        logger.debug("Buscando patrimônio por QR Code: {}", qrCode);
        
        // QR Code geralmente contém o número do patrimônio
        Patrimonio patrimonio = patrimonioDAO.buscarPorNumero(qrCode);
        
        if (patrimonio != null) {
            MobilePatrimonioDTO dto = converterParaDTO(patrimonio);
            
            // ✅ SEMPRE verificar se já foi coletado (sem cache)
            try {
                Inventario inventarioAtivo = inventarioDAO.buscarInventarioAtivo();
                if (inventarioAtivo != null) {
                    boolean foiColetado = coletaDAO.coletaExiste(inventarioAtivo.getId(), patrimonio.getId());
                    dto.setColetado(foiColetado);
                } else {
                    dto.setColetado(false);
                }
            } catch (Exception e) {
                logger.error("Erro ao verificar coleta do patrimônio {}: {}", qrCode, e.getMessage());
                dto.setColetado(false);
            }
            
            return dto;
        }
        
        logger.debug("Patrimônio {} não encontrado", qrCode);
        return null;
    }
    
    /**
     * Busca patrimônio por número (SEM CACHE para status de coleta sempre atualizado)
     * Inclui verificação se já foi coletado no inventário ativo
     * 
     * ⚠️ IMPORTANTE: Cache removido para garantir que o status de coleta seja sempre atual
     */
    public MobilePatrimonioDTO buscarPorNumero(String numero) throws SQLException {
        logger.debug("Buscando patrimônio por número: {}", numero);
        
        Patrimonio patrimonio = patrimonioDAO.buscarPorNumero(numero);
        
        if (patrimonio != null) {
            MobilePatrimonioDTO dto = converterParaDTO(patrimonio);
            
            // ✅ SEMPRE verificar se já foi coletado (sem cache)
            try {
                Inventario inventarioAtivo = inventarioDAO.buscarInventarioAtivo();
                if (inventarioAtivo != null) {
                    boolean foiColetado = coletaDAO.coletaExiste(inventarioAtivo.getId(), patrimonio.getId());
                    dto.setColetado(foiColetado);
                } else {
                    dto.setColetado(false);
                }
            } catch (Exception e) {
                logger.error("Erro ao verificar coleta do patrimônio {}: {}", numero, e.getMessage());
                dto.setColetado(false);
            }
            
            return dto;
        }
        
        logger.debug("Patrimônio {} não encontrado", numero);
        return null;
    }
    
    /**
     * Busca patrimônios por sala
     */
    public List<MobilePatrimonioDTO> buscarPorSala(Integer salaId) throws SQLException {
        logger.debug("Buscando patrimônios da sala: {}", salaId);
        
        List<Patrimonio> patrimonios = patrimonioDAO.buscarPorSala(salaId);
        List<MobilePatrimonioDTO> dtos = new ArrayList<>();
        
        for (Patrimonio patrimonio : patrimonios) {
            dtos.add(converterParaDTO(patrimonio));
        }
        
        logger.debug("Retornados {} patrimônios da sala {}", dtos.size(), salaId);
        
        return dtos;
    }
    
    /**
     * Busca patrimônios por setor
     */
    public List<MobilePatrimonioDTO> buscarPorSetor(Integer setorId) throws SQLException {
        logger.debug("Buscando patrimônios do setor: {}", setorId);
        
        // Buscar todas as salas
        List<com.inventario.sihcp.model.Sala> todasSalas = salaDAO.listarSalas();
        List<MobilePatrimonioDTO> dtos = new ArrayList<>();
        
        // Filtrar salas do setor
        for (com.inventario.sihcp.model.Sala sala : todasSalas) {
            if (sala.getIdSetor() != null && sala.getIdSetor().equals(setorId)) {
                List<Patrimonio> patrimonios = patrimonioDAO.buscarPorSala(sala.getIdSala());
                for (Patrimonio patrimonio : patrimonios) {
                    dtos.add(converterParaDTO(patrimonio));
                }
            }
        }
        
        logger.debug("Retornados {} patrimônios do setor {}", dtos.size(), setorId);
        
        return dtos;
    }
    

    
    /**
     * Lista patrimônios com paginação (OTIMIZADO)
     * Usa paginação no banco de dados ao invés de carregar tudo em memória
     */
    public List<MobilePatrimonioDTO> listarPatrimonios(int page, int size) throws SQLException {
        logger.debug("Listando patrimônios (page: {}, size: {})", page, size);
        
        // Buscar patrimônios com paginação no banco (OTIMIZADO)
        List<Patrimonio> patrimonios = patrimonioDAO.listarComPaginacao(page, size);
        
        List<MobilePatrimonioDTO> dtos = new ArrayList<>();
        
        for (Patrimonio patrimonio : patrimonios) {
            dtos.add(converterParaDTO(patrimonio));
        }
        
        logger.debug("Retornados {} patrimônios (página {})", dtos.size(), page);
        
        return dtos;
    }
    
    /**
     * Busca patrimônio por ID
     */
    public MobilePatrimonioDTO buscarPorId(Integer id) throws SQLException {
        logger.debug("Buscando patrimônio por ID: {}", id);
        
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
     * Busca patrimônios por query (texto livre) com filtros
     * Busca por número, descrição ou nome da sala
     * 
     * @param query termo de busca (mínimo 3 caracteres)
     * @param filtro filtro de status: ALL, COLETADOS, PENDENTES, DIVERGENCIAS
     * @param inventarioId ID do inventário (opcional, usa ativo se não informado)
     * @param limit limite de resultados (padrão 100)
     * @return lista de patrimônios encontrados
     */
    public List<MobilePatrimonioDTO> buscarPorQuery(String query, String filtro, Integer inventarioId, int limit) throws SQLException {
        logger.info("Buscando patrimônios por query: '{}', filtro: {}, inventarioId: {}, limit: {}", 
                query, filtro, inventarioId, limit);
        
        if (query == null || query.trim().length() < 3) {
            logger.warn("Query muito curta: '{}'", query);
            return new ArrayList<>();
        }
        
        String queryLimpa = query.trim().toLowerCase();
        
        // Obter inventário ativo se não foi informado
        Inventario inventario = null;
        try {
            if (inventarioId != null && inventarioId > 0) {
                inventario = inventarioDAO.findById(inventarioId);
            } else {
                inventario = inventarioDAO.buscarInventarioAtivo();
            }
        } catch (Exception e) {
            logger.warn("Erro ao obter inventário: {}", e.getMessage());
        }
        
        // Buscar patrimônios que correspondem à query
        List<Patrimonio> patrimonios = patrimonioDAO.buscarPorQueryTexto(queryLimpa, limit);
        logger.debug("Encontrados {} patrimônios para query '{}'", patrimonios.size(), queryLimpa);
        
        List<MobilePatrimonioDTO> resultados = new ArrayList<>();
        
        for (Patrimonio patrimonio : patrimonios) {
            // Verificar status de coleta
            boolean foiColetado = false;
            String coletadoPor = null;
            String dataColeta = null;
            String localizacaoEncontrada = null;
            String estadoEncontrado = null;
            boolean temDivergencia = false;
            
            if (inventario != null) {
                try {
                    foiColetado = coletaDAO.coletaExiste(inventario.getId(), patrimonio.getId());
                    
                    if (foiColetado) {
                        // Buscar detalhes da coleta
                        com.inventario.sihcp.model.Coleta coleta = coletaDAO.buscarColetaPorPatrimonioEInventario(
                                inventario.getId(), patrimonio.getId());
                        if (coleta != null) {
                            coletadoPor = coleta.getNomeColetor();
                            dataColeta = coleta.getDataColetaFormatada();
                            localizacaoEncontrada = coleta.getLocalizacaoEncontrada();
                            estadoEncontrado = coleta.getEstadoEncontrado();
                            
                            // Verificar divergência (localização encontrada diferente da cadastrada)
                            if (localizacaoEncontrada != null && patrimonio.getNomeSala() != null) {
                                temDivergencia = !localizacaoEncontrada.equalsIgnoreCase(patrimonio.getNomeSala());
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Erro ao verificar coleta do patrimônio {}: {}", patrimonio.getId(), e.getMessage());
                }
            }
            
            // Aplicar filtro
            boolean incluir = true;
            if (filtro != null) {
                switch (filtro.toUpperCase()) {
                    case "COLETADOS":
                        incluir = foiColetado;
                        break;
                    case "PENDENTES":
                        incluir = !foiColetado;
                        break;
                    case "DIVERGENCIAS":
                        incluir = foiColetado && temDivergencia;
                        break;
                    case "ALL":
                    default:
                        incluir = true;
                        break;
                }
            }
            
            if (incluir) {
                MobilePatrimonioDTO dto = converterParaDTO(patrimonio);
                dto.setColetado(foiColetado);
                dto.setColetadoPor(coletadoPor);
                dto.setDataColetaFormatada(dataColeta);
                dto.setLocalizacaoEncontrada(localizacaoEncontrada);
                dto.setEstadoEncontrado(estadoEncontrado);
                dto.setTemDivergencia(temDivergencia);
                resultados.add(dto);
            }
        }
        
        logger.info("Retornando {} patrimônios após filtro '{}'", resultados.size(), filtro);
        return resultados;
    }

    /**
     * Busca patrimônios por responsável com paginação e filtro de coleta
     * 
     * OTIMIZADO: Usa query única com JOIN ao invés de N+1 queries
     * 
     * @param idResponsavel ID do responsável
     * @param page número da página (0-based)
     * @param size tamanho da página
     * @param coletado filtro de coleta (true=coletados, false=não coletados, null=todos)
     * @return lista de patrimônios filtrados e paginados
     */
    public List<MobilePatrimonioDTO> buscarPorResponsavel(Integer idResponsavel, int page, int size, Boolean coletado) throws SQLException {
        logger.debug("Buscando patrimônios do responsável {} (page: {}, size: {}, coletado: {})", idResponsavel, page, size, coletado);
        
        // Obter inventário ativo para verificar coletas
        Inventario inventarioAtivo = null;
        try {
            inventarioAtivo = inventarioDAO.buscarInventarioAtivo();
            if (inventarioAtivo == null) {
                logger.debug("Nenhum inventário ativo encontrado");
            }
        } catch (Exception e) {
            logger.warn("Erro ao obter inventário ativo: {}", e.getMessage());
        }
        
        List<Patrimonio> patrimonios;
        
        // OTIMIZAÇÃO: Se há filtro de coleta E inventário ativo, usar query otimizada
        if (coletado != null && inventarioAtivo != null) {
            logger.debug("Usando query OTIMIZADA com filtro de coleta no banco");
            patrimonios = patrimonioDAO.buscarPorResponsavelComFiltroColeta(
                    idResponsavel, inventarioAtivo.getId(), coletado, page, size);
            logger.debug("Query otimizada retornou {} patrimônios", patrimonios.size());
            
            // Converter para DTOs (sem verificar coleta novamente - já filtrado)
            List<MobilePatrimonioDTO> dtos = new ArrayList<>();
            for (Patrimonio patrimonio : patrimonios) {
                MobilePatrimonioDTO dto = converterParaDTOSimples(patrimonio);
                dto.setColetado(coletado); // Já sabemos o status pelo filtro
                dtos.add(dto);
            }
            
            logger.debug("Retornando {} patrimônios do responsável {} (página {}, filtro otimizado)", 
                    dtos.size(), idResponsavel, page);
            return dtos;
        }
        
        // Sem filtro de coleta: usar paginação simples no DAO
        patrimonios = patrimonioDAO.buscarPorResponsavelComPaginacao(idResponsavel, page, size);
        logger.debug("Encontrados {} patrimônios do responsável {} (página {})", patrimonios.size(), idResponsavel, page);
        
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
            
            // Converter para DTO
            MobilePatrimonioDTO dto = converterParaDTOSimples(patrimonio);
            dto.setColetado(foiColetado);
            dtos.add(dto);
        }
        
        logger.debug("Retornando {} patrimônios do responsável {} (página {})", dtos.size(), idResponsavel, page);
        
        return dtos;
    }

    /**
     * Conta total de patrimônios por responsável
     */
    public int contarPatrimoniosPorResponsavel(Integer idResponsavel) throws SQLException {
        logger.debug("Contando patrimônios do responsável: {}", idResponsavel);
        
        int total = patrimonioDAO.contarPatrimoniosPorResponsavel(idResponsavel);
        
        logger.debug("Total de patrimônios do responsável {}: {}", idResponsavel, total);
        
        return total;
    }
    
    /**
     * Conta total de patrimônios ativos
     * Usado para paginação (retornar totalElements)
     */
    public long contarPatrimoniosAtivos() throws SQLException {
        logger.debug("Contando patrimônios ativos");
        
        int total = patrimonioDAO.contarPatrimoniosAtivos();
        
        logger.debug("Total de patrimônios ativos: {}", total);
        
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
        logger.debug("Verificando se patrimônio {} foi coletado no inventário {}", numeroPatrimonio, inventarioId);
        
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
        
        // Se foi coletado, buscar informações da coleta (OTIMIZADO - busca apenas 1 registro)
        if (coletado) {
            try {
                // OTIMIZAÇÃO: Usar método que busca apenas a coleta específica
                // ANTES: buscarPorInventario() carregava TODAS as coletas (vazamento de memória)
                // DEPOIS: buscarColetaPorPatrimonioEInventario() retorna apenas 1 registro
                com.inventario.sihcp.model.Coleta coleta = coletaDAO.buscarColetaPorPatrimonioEInventario(
                        inventario.getId(), patrimonio.getId());
                
                if (coleta != null) {
                    resultado.put("dataColeta", coleta.getDataColetaFormatada());
                    resultado.put("coletadoPor", coleta.getNomeColetor());
                    resultado.put("coletaId", coleta.getId());
                    resultado.put("observacoes", coleta.getObservacaoColeta());
                    resultado.put("localizacaoEncontrada", coleta.getLocalizacaoEncontrada());
                    resultado.put("estadoEncontrado", coleta.getEstadoEncontrado());
                }
            } catch (Exception e) {
                logger.warn("Erro ao buscar detalhes da coleta: {}", e.getMessage());
            }
        }
        
        logger.debug("Patrimônio {} {} coletado no inventário {}", 
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
        logger.debug("Validando patrimônio: {}", numeroPatrimonio);
        
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
                // OTIMIZAÇÃO: Buscar apenas a coleta específica (não todas do inventário)
                try {
                    com.inventario.sihcp.model.Coleta coleta = coletaDAO.buscarColetaPorPatrimonioEInventario(
                            inventarioAtivo.getId(), patrimonio.getId());
                    if (coleta != null) {
                        coletadoPor = coleta.getNomeColetor();
                        dataColeta = coleta.getDataColetaFormatada();
                    }
                } catch (Exception e) {
                    logger.warn("Erro ao buscar coleta: {}", e.getMessage());
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
            
            // OTIMIZAÇÃO: Buscar informações completas da coleta específica (não todas)
            try {
                com.inventario.sihcp.model.Coleta coleta = coletaDAO.buscarColetaPorPatrimonioEInventario(
                        inventarioAtivo.getId(), patrimonio.getId());
                if (coleta != null) {
                    resultado.put("coletaId", coleta.getId());
                    resultado.put("localizacaoEncontrada", coleta.getLocalizacaoEncontrada());
                    resultado.put("estadoEncontrado", coleta.getEstadoEncontrado());
                    resultado.put("observacoes", coleta.getObservacaoColeta());
                }
            } catch (Exception e) {
                logger.warn("Erro ao buscar detalhes completos da coleta: {}", e.getMessage());
            }
        }
        
        logger.debug("Patrimônio {} é válido (já coletado: {})", numeroPatrimonio, jaColetado);
        
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
        logger.debug("Verificando duplicata de coleta: patrimônio={}, inventário={}", numeroPatrimonio, inventarioId);
        
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
            
            // OTIMIZAÇÃO: Buscar apenas a coleta específica (não todas do inventário)
            try {
                com.inventario.sihcp.model.Coleta coleta = coletaDAO.buscarColetaPorPatrimonioEInventario(
                        inventario.getId(), patrimonio.getId());
                if (coleta != null) {
                    java.util.Map<String, Object> coletaExistente = new java.util.HashMap<>();
                    coletaExistente.put("id", coleta.getId());
                    coletaExistente.put("dataColeta", coleta.getDataColetaFormatada());
                    coletaExistente.put("coletadoPor", coleta.getNomeColetor());
                    coletaExistente.put("localizacao", coleta.getLocalizacaoEncontrada());
                    coletaExistente.put("estado", coleta.getEstadoEncontrado());
                    resultado.put("coletaExistente", coletaExistente);
                }
            } catch (Exception e) {
                logger.warn("Erro ao buscar coleta existente: {}", e.getMessage());
            }
            
            logger.warn("Coleta duplicada detectada: patrimônio {} já foi coletado no inventário {}", 
                    numeroPatrimonio, inventario.getId());
        } else {
            resultado.put("mensagem", "Patrimônio pode ser coletado");
            logger.debug("Patrimônio {} pode ser coletado no inventário {}", numeroPatrimonio, inventario.getId());
        }
        
        return resultado;
    }
    
    /**
     * Busca patrimônios não coletados por descrição
     * Usado para coleta por descrição (sem etiqueta)
     * 
     * @param descricao descrição do patrimônio
     * @param inventarioId ID do inventário (opcional, usa ativo se null)
     * @return lista de patrimônios não coletados com essa descrição
     */
    public List<MobilePatrimonioDTO> buscarPorDescricaoNaoColetados(String descricao, Integer inventarioId) throws SQLException {
        logger.debug("Buscando patrimônios não coletados com descrição: '{}'", descricao);
        
        // Obter inventário
        Inventario inventario;
        if (inventarioId != null) {
            inventario = inventarioDAO.findById(inventarioId);
        } else {
            inventario = inventarioDAO.buscarInventarioAtivo();
        }
        
        if (inventario == null) {
            logger.warn("Nenhum inventário encontrado para busca por descrição");
            return new ArrayList<>();
        }
        
        // Buscar todos os patrimônios com essa descrição
        List<Patrimonio> todosPatrimonios = patrimonioDAO.buscarPorDescricao(descricao);
        logger.debug("Encontrados {} patrimônios com descrição '{}'", todosPatrimonios.size(), descricao);
        
        // Filtrar apenas os não coletados
        List<MobilePatrimonioDTO> naoColetados = new ArrayList<>();
        
        for (Patrimonio patrimonio : todosPatrimonios) {
            boolean foiColetado = coletaDAO.coletaExiste(inventario.getId(), patrimonio.getId());
            
            if (!foiColetado) {
                MobilePatrimonioDTO dto = converterParaDTO(patrimonio);
                dto.setColetado(false);
                naoColetados.add(dto);
            }
        }
        
        logger.debug("{} patrimônios NÃO coletados com descrição '{}'", naoColetados.size(), descricao);
        
        return naoColetados;
    }
    
    /**
     * Busca patrimônios por sala com paginação e filtro de coleta.
     * 
     * @param salaId ID da sala
     * @param page número da página (0-based)
     * @param size tamanho da página
     * @param coletado filtro de coleta (true=coletados, false=não coletados, null=todos)
     * @param inventarioId ID do inventário (opcional, usa ativo se null)
     * @return lista de patrimônios filtrados e paginados
     */
    public List<MobilePatrimonioDTO> buscarPorSalaComFiltro(Integer salaId, int page, int size, Boolean coletado, Integer inventarioId) throws SQLException {
        logger.debug("Buscando patrimônios da sala {} (page: {}, size: {}, coletado: {}, inventário: {})", 
                salaId, page, size, coletado, inventarioId);
        
        // Obter inventário
        Inventario inventario;
        if (inventarioId != null) {
            inventario = inventarioDAO.findById(inventarioId);
        } else {
            inventario = inventarioDAO.buscarInventarioAtivo();
        }
        
        if (inventario == null) {
            logger.warn("Nenhum inventário encontrado para busca por sala");
        }

        // ✅ CORREÇÃO v2.20.7: carregar todas as coletas do inventário uma única vez
        // e indexar por idPatrimonio. Assim populamos os campos de auditoria
        // (coletadoPor, dataColetaFormatada, localizacaoEncontrada, estadoEncontrado)
        // sem fazer N consultas ao banco. Sem isso, o relatório exportado mostrava
        // "-" em todas as colunas de auditoria.
        java.util.Map<Integer, com.inventario.sihcp.model.Coleta> coletasPorPatrimonio = new java.util.HashMap<>();
        if (inventario != null) {
            try {
                List<com.inventario.sihcp.model.Coleta> coletasInv = coletaDAO.buscarPorInventario(inventario.getId());
                for (com.inventario.sihcp.model.Coleta c : coletasInv) {
                    // Em caso de múltiplas coletas do mesmo patrimônio, a ordem DESC
                    // por DATA_COLETA em `buscarPorInventario` garante que a mais
                    // recente seja a primeira; `putIfAbsent` preserva essa escolha.
                    coletasPorPatrimonio.putIfAbsent(c.getIdPatrimonio(), c);
                }
                logger.debug("Coletas indexadas: {} para o inventário {}", coletasPorPatrimonio.size(), inventario.getId());
            } catch (Exception e) {
                logger.warn("Falha ao carregar coletas do inventário {}: {}", inventario.getId(), e.getMessage());
            }
        }
        
        List<MobilePatrimonioDTO> dtos = new ArrayList<>();
        
        // Se não há filtro de coleta, usar paginação direta no banco (mais eficiente)
        if (coletado == null) {
            List<Patrimonio> patrimonios = patrimonioDAO.buscarPorSalaComPaginacao(salaId, page, size);
            logger.debug("Encontrados {} patrimônios na sala {} (paginação no banco)", patrimonios.size(), salaId);
            
            for (Patrimonio patrimonio : patrimonios) {
                com.inventario.sihcp.model.Coleta coleta = coletasPorPatrimonio.get(patrimonio.getId());
                boolean foiColetado = coleta != null;
                MobilePatrimonioDTO dto = converterParaDTOSimples(patrimonio);
                dto.setColetado(foiColetado);
                if (foiColetado) {
                    aplicarDadosAuditoriaColeta(dto, coleta);
                }
                dtos.add(dto);
            }
            
            logger.debug("Retornando {} patrimônios da sala {} (página {})", dtos.size(), salaId, page);
            return dtos;
        }
        
        // Com filtro de coleta: buscar em lotes para evitar carregar tudo na memória
        // Estratégia: buscar lotes do banco até ter itens suficientes para a página
        int batchSize = size * 3; // Buscar 3x o tamanho da página por vez
        int currentPage = 0;
        int itemsToSkip = page * size;
        int itemsSkipped = 0;
        int itemsCollected = 0;
        
        logger.debug("Buscando com filtro coletado={}, precisamos pular {} itens e coletar {}", 
                coletado, itemsToSkip, size);
        
        while (itemsCollected < size) {
            List<Patrimonio> batch = patrimonioDAO.buscarPorSalaComPaginacao(salaId, currentPage, batchSize);
            
            if (batch.isEmpty()) {
                logger.debug("Não há mais patrimônios para buscar");
                break; // Não há mais dados
            }
            
            for (Patrimonio patrimonio : batch) {
                com.inventario.sihcp.model.Coleta coleta = coletasPorPatrimonio.get(patrimonio.getId());
                boolean foiColetado = coleta != null;
                
                // Verificar se passa no filtro
                boolean passaFiltro = (coletado && foiColetado) || (!coletado && !foiColetado);
                
                if (passaFiltro) {
                    if (itemsSkipped < itemsToSkip) {
                        itemsSkipped++;
                    } else {
                        MobilePatrimonioDTO dto = converterParaDTOSimples(patrimonio);
                        dto.setColetado(foiColetado);
                        if (foiColetado) {
                            aplicarDadosAuditoriaColeta(dto, coleta);
                        }
                        dtos.add(dto);
                        itemsCollected++;
                        
                        if (itemsCollected >= size) {
                            break;
                        }
                    }
                }
            }
            
            currentPage++;
            
            // Limite de segurança para evitar loop infinito
            if (currentPage > 100) {
                logger.warn("Limite de páginas atingido na busca por sala com filtro");
                break;
            }
        }
        
        logger.debug("Retornando {} patrimônios da sala {} (página {}, filtro coletado={})", 
                dtos.size(), salaId, page, coletado);
        
        return dtos;
    }

    /**
     * Preenche os campos de auditoria da coleta no DTO.
     * Extraído para reutilização entre os dois caminhos de buscarPorSalaComFiltro
     * e garantir que o app receba sempre os mesmos dados.
     */
    private void aplicarDadosAuditoriaColeta(MobilePatrimonioDTO dto, com.inventario.sihcp.model.Coleta coleta) {
        if (coleta == null) return;
        dto.setColetadoPor(coleta.getNomeColetor());
        dto.setDataColetaFormatada(coleta.getDataColetaFormatada());
        dto.setLocalizacaoEncontrada(coleta.getLocalizacaoEncontrada());
        dto.setEstadoEncontrado(coleta.getEstadoEncontrado());
    }
    
    /**
     * Converte Patrimonio para DTO sem verificar status de coleta (mais rápido).
     * Usado quando o status de coleta já foi verificado externamente.
     */
    private MobilePatrimonioDTO converterParaDTOSimples(Patrimonio patrimonio) {
        MobilePatrimonioDTO dto = new MobilePatrimonioDTO();
        
        dto.setId(patrimonio.getId() > 0 ? Long.valueOf(patrimonio.getId()) : 0L);
        dto.setCodigo(patrimonio.getNumero() != null ? patrimonio.getNumero() : "");
        dto.setDescricao(patrimonio.getDescricao() != null ? patrimonio.getDescricao() : "Sem descrição");
        
        if (patrimonio.getMarca() != null && !patrimonio.getMarca().trim().isEmpty()) {
            dto.setMarca(patrimonio.getMarca());
        }
        if (patrimonio.getModelo() != null && !patrimonio.getModelo().trim().isEmpty()) {
            dto.setModelo(patrimonio.getModelo());
        }
        if (patrimonio.getEstadoConservacao() != null && !patrimonio.getEstadoConservacao().trim().isEmpty()) {
            dto.setEstado(patrimonio.getEstadoConservacao());
        }
        if (patrimonio.getIdSala() > 0) {
            dto.setSalaId(Long.valueOf(patrimonio.getIdSala()));
        }
        if (patrimonio.getNomeSala() != null && !patrimonio.getNomeSala().trim().isEmpty()) {
            dto.setSalaNome(patrimonio.getNomeSala());
        }
        if (patrimonio.getIdResponsavel() > 0) {
            dto.setResponsavelId(Long.valueOf(patrimonio.getIdResponsavel()));
        }
        if (patrimonio.getNomeResponsavel() != null && !patrimonio.getNomeResponsavel().trim().isEmpty()) {
            dto.setResponsavelNome(patrimonio.getNomeResponsavel());
        }
        dto.setQrCode(patrimonio.getNumero() != null ? patrimonio.getNumero() : "");
        
        return dto;
    }
    
    // Método auxiliar para converter Patrimonio para DTO
    // ✅ TRATAMENTO ROBUSTO: Garante compatibilidade com versões antigas do app
    // - Todos os campos são verificados antes de serem setados
    // - Campos nulos são tratados com valores padrão seguros
    // - Exceções são capturadas e logadas sem quebrar a conversão
    private MobilePatrimonioDTO converterParaDTO(Patrimonio patrimonio) {
        MobilePatrimonioDTO dto = new MobilePatrimonioDTO();
        
        try {
            // Campos obrigatórios com fallback
            dto.setId(patrimonio.getId() > 0 ? Long.valueOf(patrimonio.getId()) : 0L);
            dto.setCodigo(patrimonio.getNumero() != null ? patrimonio.getNumero() : "");
            dto.setDescricao(patrimonio.getDescricao() != null ? patrimonio.getDescricao() : "Sem descrição");
            
            // Campos opcionais - só seta se não for null
            if (patrimonio.getMarca() != null && !patrimonio.getMarca().trim().isEmpty()) {
                dto.setMarca(patrimonio.getMarca());
            }
            
            if (patrimonio.getModelo() != null && !patrimonio.getModelo().trim().isEmpty()) {
                dto.setModelo(patrimonio.getModelo());
            }
            
            if (patrimonio.getEstadoConservacao() != null && !patrimonio.getEstadoConservacao().trim().isEmpty()) {
                dto.setEstado(patrimonio.getEstadoConservacao());
            }
            
            // IDs com verificação de valores válidos (int primitivo, não pode ser null)
            if (patrimonio.getIdSala() > 0) {
                dto.setSalaId(Long.valueOf(patrimonio.getIdSala()));
            }
            
            if (patrimonio.getNomeSala() != null && !patrimonio.getNomeSala().trim().isEmpty()) {
                dto.setSalaNome(patrimonio.getNomeSala());
            }
            
            if (patrimonio.getIdResponsavel() > 0) {
                dto.setResponsavelId(Long.valueOf(patrimonio.getIdResponsavel()));
            }
            
            if (patrimonio.getNomeResponsavel() != null && !patrimonio.getNomeResponsavel().trim().isEmpty()) {
                dto.setResponsavelNome(patrimonio.getNomeResponsavel());
            }
            
            // QR Code é o número do patrimônio
            dto.setQrCode(patrimonio.getNumero() != null ? patrimonio.getNumero() : "");
            
            // Valor com tratamento de exceção
            try {
                if (patrimonio.getValor() != null) {
                    dto.setValor(patrimonio.getValor().doubleValue());
                }
            } catch (Exception e) {
                logger.warn("Erro ao converter valor do patrimônio {}: {}", patrimonio.getId(), e.getMessage());
                dto.setValor(0.0);
            }
            
            // Campos adicionais opcionais
            if (patrimonio.getObservacoes() != null && !patrimonio.getObservacoes().trim().isEmpty()) {
                dto.setObservacoes(patrimonio.getObservacoes());
            }
            
            if (patrimonio.getEd() != null && !patrimonio.getEd().trim().isEmpty()) {
                dto.setEd(patrimonio.getEd());
            }
            
            if (patrimonio.getNumeroNotaFiscal() != null && !patrimonio.getNumeroNotaFiscal().trim().isEmpty()) {
                dto.setNumeroNotaFiscal(patrimonio.getNumeroNotaFiscal());
            }
            
            if (patrimonio.getFornecedor() != null && !patrimonio.getFornecedor().trim().isEmpty()) {
                dto.setFornecedor(patrimonio.getFornecedor());
            }
            
        } catch (Exception e) {
            logger.error("Erro ao converter patrimônio {} para DTO: {}", 
                    patrimonio != null ? patrimonio.getId() : "null", e.getMessage());
            // Retornar DTO com dados mínimos para não quebrar o app
            dto.setId(patrimonio != null && patrimonio.getId() > 0 ? Long.valueOf(patrimonio.getId()) : 0L);
            dto.setCodigo(patrimonio != null && patrimonio.getNumero() != null ? patrimonio.getNumero() : "ERRO");
            dto.setDescricao("Erro ao carregar dados completos");
        }
        
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
                    List<com.inventario.sihcp.model.Coleta> coletas = coletaDAO.buscarPorInventario(inventarioAtivo.getId());
                    
                    for (com.inventario.sihcp.model.Coleta coleta : coletas) {
                        if (coleta.getIdPatrimonio() == patrimonio.getId()) {
                            coletadoPor = coleta.getNomeColetor();
                            dataColetaFormatada = coleta.getDataColetaFormatada();
                            dto.setLocalizacaoEncontrada(coleta.getLocalizacaoEncontrada());
                            dto.setEstadoEncontrado(coleta.getEstadoEncontrado());
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
