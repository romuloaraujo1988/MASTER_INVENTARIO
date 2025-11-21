package com.inventario.mobile.server.service;

import com.inventario.dao.PatrimonioDAO;
import com.inventario.mobile.server.dto.MobilePatrimonioDTO;
import com.inventario.mobile.server.dto.PatrimonioDetalheDTO;
import com.inventario.model.Patrimonio;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Serviço para consulta de patrimônios
 * Implementa lógica de busca por código parcial e descrição
 * 
 * @author Sistema de Inventário
 * @version 1.0
 */
@Service
public class MobileConsultaService {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileConsultaService.class);
    
    private final PatrimonioDAO patrimonioDAO;
    private final com.inventario.dao.ColetaDAO coletaDAO;
    private final com.inventario.dao.InventarioDAO inventarioDAO;
    private final com.inventario.dao.SalaDAO salaDAO;
    
    public MobileConsultaService() {
        this.patrimonioDAO = new PatrimonioDAO();
        this.coletaDAO = new com.inventario.dao.ColetaDAO();
        this.inventarioDAO = new com.inventario.dao.InventarioDAO();
        this.salaDAO = new com.inventario.dao.SalaDAO();
    }
    
    /**
     * Busca patrimônios por código parcial
     * 
     * @param codigo Código parcial (mínimo 2 caracteres)
     * @param limit Quantidade máxima de resultados
     * @return Lista de patrimônios encontrados
     * @throws SQLException Se ocorrer erro no banco
     */
    public List<MobilePatrimonioDTO> buscarPorCodigoParcial(String codigo, int limit) throws SQLException {
        logger.info("Buscando patrimônios por código parcial: '{}' (limit: {})", codigo, limit);
        
        List<Patrimonio> patrimonios = patrimonioDAO.buscarPorCodigoParcial(codigo, limit);
        
        logger.info("Encontrados {} patrimônios", patrimonios.size());
        
        return patrimonios.stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca patrimônios por descrição
     * 
     * @param descricao Descrição ou parte dela (mínimo 3 caracteres)
     * @param limit Quantidade máxima de resultados
     * @return Lista de patrimônios encontrados
     * @throws SQLException Se ocorrer erro no banco
     */
    public List<MobilePatrimonioDTO> buscarPorDescricao(String descricao, int limit) throws SQLException {
        logger.info("Buscando patrimônios por descrição: '{}' (limit: {})", descricao, limit);
        
        List<Patrimonio> patrimonios = patrimonioDAO.buscarPorDescricao(descricao, limit);
        
        logger.info("Encontrados {} patrimônios", patrimonios.size());
        
        return patrimonios.stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtém detalhes completos de um patrimônio
     * 
     * @param id ID do patrimônio
     * @return Detalhes completos do patrimônio
     * @throws SQLException Se ocorrer erro no banco
     */
    public PatrimonioDetalheDTO obterDetalhesCompletos(Long id) throws SQLException {
        logger.info("Buscando detalhes completos do patrimônio ID: {}", id);
        
        Patrimonio patrimonio = patrimonioDAO.findById(id.intValue());
        
        if (patrimonio == null) {
            logger.warn("Patrimônio não encontrado: ID {}", id);
            return null;
        }
        
        // Criar DTO com detalhes completos
        PatrimonioDetalheDTO detalhes = new PatrimonioDetalheDTO();
        
        // Dados básicos
        detalhes.setId(patrimonio.getId());
        detalhes.setCodigo(patrimonio.getNumero());
        detalhes.setDescricao(patrimonio.getDescricao());
        detalhes.setMarca(patrimonio.getMarca());
        detalhes.setModelo(patrimonio.getModelo());
        detalhes.setNumeroSerie(patrimonio.getNumeroSerie());
        detalhes.setEstado(patrimonio.getEstadoConservacao());
        detalhes.setValor(patrimonio.getValor());
        detalhes.setObservacoes(patrimonio.getObservacoes());
        
        // Dados da sala (buscar dados completos da tabela SALA)
        if (patrimonio.getIdSala() > 0) {
            detalhes.setSalaId(patrimonio.getIdSala());
            detalhes.setSalaNome(patrimonio.getNomeSala());
            
            // Buscar dados completos da sala (bloco e andar)
            try {
                com.inventario.model.Sala sala = salaDAO.buscarSalaPorId(patrimonio.getIdSala());
                if (sala != null) {
                    detalhes.setSalaBloco(sala.getBloco());
                    detalhes.setSalaAndar(sala.getAndar() != null ? sala.getAndar().toString() : null);
                    
                    // Montar localização completa (ex: "Bloco A - 2º Andar - Sala 201")
                    StringBuilder localizacao = new StringBuilder();
                    if (sala.getBloco() != null && !sala.getBloco().isEmpty()) {
                        localizacao.append("Bloco ").append(sala.getBloco());
                    }
                    if (sala.getAndar() != null) {
                        if (localizacao.length() > 0) localizacao.append(" - ");
                        localizacao.append(sala.getAndar()).append("º Andar");
                    }
                    if (sala.getNumeroSala() != null && !sala.getNumeroSala().isEmpty()) {
                        if (localizacao.length() > 0) localizacao.append(" - ");
                        localizacao.append("Sala ").append(sala.getNumeroSala());
                    }
                    detalhes.setSalaLocalizacaoCompleta(localizacao.toString());
                    
                    logger.debug("Dados da sala carregados: bloco={}, andar={}, localização={}", 
                            sala.getBloco(), sala.getAndar(), localizacao.toString());
                } else {
                    logger.warn("Sala não encontrada: ID {}", patrimonio.getIdSala());
                }
            } catch (SQLException e) {
                logger.warn("Erro ao buscar dados da sala {}: {}", 
                        patrimonio.getIdSala(), e.getMessage());
            }
        }
        
        // Dados do responsável (campos diretos)
        if (patrimonio.getIdResponsavel() > 0) {
            detalhes.setResponsavelId(patrimonio.getIdResponsavel());
            detalhes.setResponsavelNome(patrimonio.getNomeResponsavel());
            detalhes.setResponsavelMatricula(patrimonio.getMatriculaResponsavel());
            detalhes.setResponsavelSetor(patrimonio.getNomeSetor());
        }
        
        // Status de coleta (buscar da tabela COLETA)
        try {
            // Buscar inventário ativo
            com.inventario.model.Inventario inventarioAtivo = inventarioDAO.buscarInventarioAtivo();
            
            if (inventarioAtivo != null) {
                // Verificar se foi coletado no inventário ativo
                boolean coletado = coletaDAO.verificarSePatrimonioFoiColetado(
                    patrimonio.getId(), 
                    inventarioAtivo.getId()
                );
                detalhes.setColetado(coletado);
                
                // Buscar data da coleta se foi coletado
                if (coletado) {
                    java.sql.Timestamp dataColeta = coletaDAO.buscarDataColetaPatrimonio(
                        patrimonio.getId(), 
                        inventarioAtivo.getId()
                    );
                    detalhes.setDataColeta(dataColeta);
                } else {
                    detalhes.setDataColeta(null);
                }
            } else {
                // Sem inventário ativo
                detalhes.setColetado(false);
                detalhes.setDataColeta(null);
            }
        } catch (Exception e) {
            logger.warn("Erro ao verificar coleta do patrimônio {}: {}", id, e.getMessage());
            detalhes.setColetado(false);
            detalhes.setDataColeta(null);
        }
        
        // Buscar histórico de coletas
        try {
            List<Map<String, Object>> historico = coletaDAO.buscarHistoricoColetasPatrimonio(patrimonio.getId());
            detalhes.setHistoricoColetas(historico);
            logger.info("Histórico de coletas carregado: {} registros", historico.size());
        } catch (Exception e) {
            logger.warn("Erro ao buscar histórico de coletas do patrimônio {}: {}", id, e.getMessage());
            detalhes.setHistoricoColetas(new java.util.ArrayList<>());
        }
        
        logger.info("Detalhes do patrimônio {} carregados com sucesso", id);
        
        return detalhes;
    }
    
    /**
     * Busca avançada com múltiplos critérios
     * 
     * @param termo Termo de busca geral
     * @param salaId ID da sala (opcional)
     * @param responsavelId ID do responsável (opcional)
     * @param limit Quantidade máxima de resultados
     * @return Lista de patrimônios encontrados
     * @throws SQLException Se ocorrer erro no banco
     */
    public List<MobilePatrimonioDTO> buscarAvancada(
            String termo, 
            Integer salaId, 
            Integer responsavelId, 
            int limit) throws SQLException {
        
        logger.info("Busca avançada: termo='{}', salaId={}, responsavelId={}, limit={}", 
                termo, salaId, responsavelId, limit);
        
        List<Patrimonio> patrimonios = patrimonioDAO.buscarAvancada(
                termo, salaId, responsavelId, limit);
        
        logger.info("Busca avançada encontrou {} patrimônios", patrimonios.size());
        
        return patrimonios.stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Converte Patrimonio (model) para MobilePatrimonioDTO
     * 
     * @param patrimonio Patrimônio do modelo
     * @return DTO para mobile
     */
    private MobilePatrimonioDTO converterParaDTO(Patrimonio patrimonio) {
        MobilePatrimonioDTO dto = new MobilePatrimonioDTO();
        
        // ID com cast para Long
        dto.setId((long) patrimonio.getId());
        
        // Dados básicos
        dto.setCodigo(patrimonio.getNumero());
        dto.setDescricao(patrimonio.getDescricao());
        dto.setMarca(patrimonio.getMarca());
        dto.setModelo(patrimonio.getModelo());
        dto.setNumeroSerie(patrimonio.getNumeroSerie());
        
        // Estado (método correto)
        dto.setEstado(patrimonio.getEstadoConservacao());
        
        // Valor com conversão para Double
        BigDecimal valor = patrimonio.getValor();
        dto.setValor(valor != null ? valor.doubleValue() : null);
        
        dto.setObservacoes(patrimonio.getObservacoes());
        
        // Sala (campos diretos com cast para Long)
        if (patrimonio.getIdSala() > 0) {
            dto.setSalaId((long) patrimonio.getIdSala());
            dto.setSalaNome(patrimonio.getNomeSala());
        }
        
        // Responsável (campos diretos com cast para Long)
        if (patrimonio.getIdResponsavel() > 0) {
            dto.setResponsavelId((long) patrimonio.getIdResponsavel());
            dto.setResponsavelNome(patrimonio.getNomeResponsavel());
        }
        
        // Setor (apenas nome disponível)
        if (patrimonio.getNomeSetor() != null && !patrimonio.getNomeSetor().isEmpty()) {
            dto.setSetorNome(patrimonio.getNomeSetor());
            // setorId não está disponível no modelo Patrimonio
        }
        
        // Status de coleta (buscar da tabela COLETA)
        try {
            // Buscar inventário ativo
            com.inventario.model.Inventario inventarioAtivo = inventarioDAO.buscarInventarioAtivo();
            
            if (inventarioAtivo != null) {
                // Verificar se foi coletado no inventário ativo
                boolean coletado = coletaDAO.verificarSePatrimonioFoiColetado(
                    patrimonio.getId(), 
                    inventarioAtivo.getId()
                );
                dto.setColetado(coletado);
                
                // Buscar data da coleta se foi coletado
                if (coletado) {
                    java.sql.Timestamp dataColeta = coletaDAO.buscarDataColetaPatrimonio(
                        patrimonio.getId(), 
                        inventarioAtivo.getId()
                    );
                    dto.setDataColeta(dataColeta != null ? dataColeta.toString() : null);
                } else {
                    dto.setDataColeta(null);
                }
            } else {
                // Sem inventário ativo
                dto.setColetado(false);
                dto.setDataColeta(null);
            }
        } catch (Exception e) {
            logger.warn("Erro ao verificar coleta do patrimônio {}: {}", 
                patrimonio.getId(), e.getMessage());
            dto.setColetado(false);
            dto.setDataColeta(null);
        }
        
        return dto;
    }
}
