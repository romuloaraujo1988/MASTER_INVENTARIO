package com.inventario.sihcp.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inventario.sihcp.dao.HistoricoColetaDAO;
import com.inventario.sihcp.dao.PatrimonioDAO;
import com.inventario.sihcp.dto.ComparacaoColetaDTO;
import com.inventario.sihcp.dto.EstatisticasHistoricoDTO;
import com.inventario.sihcp.dto.FiltroHistoricoDTO;
import com.inventario.sihcp.dto.FormatoExportacao;
import com.inventario.sihcp.dto.HistoricoColetaDTO;
import com.inventario.sihcp.model.Patrimonio;
import com.inventario.sihcp.util.HistoricoExcelGenerator;
import com.inventario.sihcp.util.HistoricoPDFGenerator;

/**
 * Serviço para operações de histórico de coletas de patrimônio.
 * Fornece métodos para buscar, comparar e exportar histórico de coletas.
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@Service
@Transactional(readOnly = true)
public class HistoricoColetaService {
    
    private static final Logger logger = LoggerFactory.getLogger(HistoricoColetaService.class);
    
    private final HistoricoColetaDAO historicoDAO;
    private final PatrimonioDAO patrimonioDAO;
    
    public HistoricoColetaService() {
        this.historicoDAO = new HistoricoColetaDAO();
        this.patrimonioDAO = new PatrimonioDAO();
    }
    
    public HistoricoColetaService(HistoricoColetaDAO historicoDAO, PatrimonioDAO patrimonioDAO) {
        this.historicoDAO = historicoDAO;
        this.patrimonioDAO = patrimonioDAO;
    }
    
    /**
     * Busca o histórico completo de coletas de um patrimônio.
     * Aplica filtros opcionais e identifica mudanças entre coletas consecutivas.
     * 
     * @param patrimonioId ID do patrimônio
     * @param filtros Filtros a aplicar (pode ser null)
     * @return Lista de DTOs com histórico de coletas
     * @throws BusinessException Se patrimônio não existir ou houver erro
     */
    public List<HistoricoColetaDTO> buscarHistorico(
            Integer patrimonioId,
            FiltroHistoricoDTO filtros) throws BusinessException {
        
        logger.info("Buscando histórico de coletas para patrimônio: {}", patrimonioId);
        
        // Validar entrada
        if (patrimonioId == null || patrimonioId <= 0) {
            throw new BusinessException("ID do patrimônio inválido: " + patrimonioId);
        }
        
        try {
            // Verificar se patrimônio existe
            Patrimonio patrimonio = patrimonioDAO.buscarPorIdComJoins(patrimonioId);
            if (patrimonio == null) {
                throw new BusinessException("Patrimônio não encontrado: " + patrimonioId);
            }
            
            // Buscar coletas com filtros
            List<Map<String, Object>> coletasMap;
            if (filtros != null && filtros.temFiltros()) {
                coletasMap = historicoDAO.buscarColetasComFiltros(
                    patrimonioId,
                    filtros.getInventarioId(),
                    filtros.getColetorId(),
                    filtros.getDataInicio() != null ? new java.sql.Date(filtros.getDataInicio().getTime()) : null,
                    filtros.getDataFim() != null ? new java.sql.Date(filtros.getDataFim().getTime()) : null,
                    filtros.getOffset(),
                    filtros.getLimit()
                );
            } else {
                Integer offsetValue = (filtros != null) ? filtros.getOffset() : null;
                Integer limitValue = (filtros != null) ? filtros.getLimit() : null;
                int offset = (offsetValue != null) ? offsetValue : 0;
                int limit = (limitValue != null) ? limitValue : 100;
                coletasMap = historicoDAO.buscarColetasPorPatrimonio(patrimonioId, offset, limit);
            }
            
            // Converter para DTOs
            List<HistoricoColetaDTO> historico = converterParaDTOs(coletasMap);
            
            // Identificar mudanças entre coletas consecutivas
            identificarMudancas(historico);
            
            logger.info("Histórico carregado: {} coletas encontradas", historico.size());
            
            return historico;
            
        } catch (SQLException e) {
            logger.error("Erro ao buscar histórico do patrimônio: {}", patrimonioId, e);
            throw new BusinessException("Erro ao buscar histórico: " + e.getMessage(), e);
        }
    }
    
    /**
     * Compara duas coletas específicas e identifica as mudanças.
     * 
     * @param coletaId1 ID da primeira coleta
     * @param coletaId2 ID da segunda coleta
     * @return DTO com as mudanças identificadas
     * @throws BusinessException Se alguma coleta não existir ou houver erro
     */
    public ComparacaoColetaDTO compararColetas(
            Integer coletaId1,
            Integer coletaId2) throws BusinessException {
        
        logger.info("Comparando coletas: {} e {}", coletaId1, coletaId2);
        
        // Validar entrada
        if (coletaId1 == null || coletaId1 <= 0) {
            throw new BusinessException("ID da primeira coleta inválido: " + coletaId1);
        }
        if (coletaId2 == null || coletaId2 <= 0) {
            throw new BusinessException("ID da segunda coleta inválido: " + coletaId2);
        }
        
        try {
            // Buscar ambas as coletas
            Map<String, Object> coleta1Map = historicoDAO.buscarColetaPorId(coletaId1);
            if (coleta1Map == null) {
                throw new BusinessException("Coleta não encontrada: " + coletaId1);
            }
            
            Map<String, Object> coleta2Map = historicoDAO.buscarColetaPorId(coletaId2);
            if (coleta2Map == null) {
                throw new BusinessException("Coleta não encontrada: " + coletaId2);
            }
            
            // Converter para DTOs
            HistoricoColetaDTO coleta1 = converterParaDTO(coleta1Map);
            HistoricoColetaDTO coleta2 = converterParaDTO(coleta2Map);
            
            // Verificar se são do mesmo patrimônio
            if (!coleta1.getPatrimonioId().equals(coleta2.getPatrimonioId())) {
                throw new BusinessException("As coletas não são do mesmo patrimônio");
            }
            
            // Criar DTO de comparação
            ComparacaoColetaDTO comparacao = new ComparacaoColetaDTO();
            comparacao.setColetaAnterior(coleta1);
            comparacao.setColetaAtual(coleta2);
            
            // Identificar mudanças
            List<ComparacaoColetaDTO.Mudanca> mudancas = new ArrayList<>();
            
            // Comparar localização
            if (!Objects.equals(coleta1.getLocalizacaoEncontrada(), coleta2.getLocalizacaoEncontrada())) {
                mudancas.add(new ComparacaoColetaDTO.Mudanca(
                    "Localização",
                    coleta1.getLocalizacaoEncontrada(),
                    coleta2.getLocalizacaoEncontrada(),
                    ComparacaoColetaDTO.TipoMudanca.LOCALIZACAO
                ));
            }
            
            // Comparar estado
            if (!Objects.equals(coleta1.getEstadoEncontrado(), coleta2.getEstadoEncontrado())) {
                mudancas.add(new ComparacaoColetaDTO.Mudanca(
                    "Estado",
                    coleta1.getEstadoEncontrado(),
                    coleta2.getEstadoEncontrado(),
                    ComparacaoColetaDTO.TipoMudanca.ESTADO
                ));
            }
            
            comparacao.setMudancas(mudancas);
            
            logger.info("Comparação concluída: {} mudanças identificadas", mudancas.size());
            
            return comparacao;
            
        } catch (SQLException e) {
            logger.error("Erro ao comparar coletas: {} e {}", coletaId1, coletaId2, e);
            throw new BusinessException("Erro ao comparar coletas: " + e.getMessage(), e);
        }
    }
    
    /**
     * Busca estatísticas do histórico de coletas de um patrimônio.
     * 
     * @param patrimonioId ID do patrimônio
     * @return DTO com estatísticas calculadas
     * @throws BusinessException Se patrimônio não existir ou houver erro
     */
    public EstatisticasHistoricoDTO buscarEstatisticas(Integer patrimonioId) throws BusinessException {
        logger.info("Buscando estatísticas do histórico para patrimônio: {}", patrimonioId);
        
        // Validar entrada
        if (patrimonioId == null || patrimonioId <= 0) {
            throw new BusinessException("ID do patrimônio inválido: " + patrimonioId);
        }
        
        try {
            // Verificar se patrimônio existe
            Patrimonio patrimonio = patrimonioDAO.buscarPorIdComJoins(patrimonioId);
            if (patrimonio == null) {
                throw new BusinessException("Patrimônio não encontrado: " + patrimonioId);
            }
            
            // Buscar todas as coletas (sem paginação)
            List<Map<String, Object>> coletasMap = historicoDAO.buscarColetasPorPatrimonio(
                patrimonioId, 0, Integer.MAX_VALUE
            );
            
            if (coletasMap.isEmpty()) {
                // Retornar estatísticas vazias
                return new EstatisticasHistoricoDTO();
            }
            
            // Converter para DTOs
            List<HistoricoColetaDTO> coletas = converterParaDTOs(coletasMap);
            
            // Calcular estatísticas
            EstatisticasHistoricoDTO stats = new EstatisticasHistoricoDTO();
            stats.setTotalColetas(coletas.size());
            
            // Primeira e última coleta (lista já vem ordenada DESC)
            stats.setPrimeiraColeta(coletas.get(coletas.size() - 1).getDataColeta());
            stats.setUltimaColeta(coletas.get(0).getDataColeta());
            
            // Contar mudanças
            int mudancasLocalizacao = 0;
            int mudancasEstado = 0;
            Set<Integer> inventariosDistintos = new HashSet<>();
            
            for (int i = 0; i < coletas.size() - 1; i++) {
                HistoricoColetaDTO atual = coletas.get(i);
                HistoricoColetaDTO anterior = coletas.get(i + 1);
                
                // Contar mudanças de localização
                if (!Objects.equals(atual.getLocalizacaoEncontrada(), anterior.getLocalizacaoEncontrada())) {
                    mudancasLocalizacao++;
                }
                
                // Contar mudanças de estado
                if (!Objects.equals(atual.getEstadoEncontrado(), anterior.getEstadoEncontrado())) {
                    mudancasEstado++;
                }
                
                // Coletar inventários distintos
                inventariosDistintos.add(atual.getInventarioId());
            }
            // Adicionar último inventário
            if (!coletas.isEmpty()) {
                inventariosDistintos.add(coletas.get(coletas.size() - 1).getInventarioId());
            }
            
            stats.setTotalMudancasLocalizacao(mudancasLocalizacao);
            stats.setTotalMudancasEstado(mudancasEstado);
            stats.setTotalInventarios(inventariosDistintos.size());
            
            logger.info("Estatísticas calculadas: {} coletas, {} mudanças de localização, {} mudanças de estado",
                stats.getTotalColetas(), mudancasLocalizacao, mudancasEstado);
            
            return stats;
            
        } catch (SQLException e) {
            logger.error("Erro ao buscar estatísticas do patrimônio: {}", patrimonioId, e);
            throw new BusinessException("Erro ao buscar estatísticas: " + e.getMessage(), e);
        }
    }
    
    /**
     * Exporta o histórico de coletas em formato específico.
     * 
     * @param patrimonioId ID do patrimônio
     * @param formato Formato de exportação (PDF ou EXCEL)
     * @param filtros Filtros a aplicar (pode ser null)
     * @return Bytes do arquivo gerado
     * @throws BusinessException Se houver erro na exportação
     */
    public byte[] exportarHistorico(
            Integer patrimonioId,
            FormatoExportacao formato,
            FiltroHistoricoDTO filtros) throws BusinessException {
        
        logger.info("Exportando histórico do patrimônio {} em formato {}", patrimonioId, formato);
        
        // Validar entrada
        if (patrimonioId == null || patrimonioId <= 0) {
            throw new BusinessException("ID do patrimônio inválido: " + patrimonioId);
        }
        if (formato == null) {
            throw new BusinessException("Formato de exportação não especificado");
        }
        
        try {
            // Buscar histórico completo (sem paginação para exportação)
            FiltroHistoricoDTO filtroExportacao = filtros != null ? filtros : new FiltroHistoricoDTO();
            filtroExportacao.setOffset(0);
            filtroExportacao.setLimit(Integer.MAX_VALUE);
            
            List<HistoricoColetaDTO> historico = buscarHistorico(patrimonioId, filtroExportacao);
            
            if (historico.isEmpty()) {
                throw new BusinessException("Nenhuma coleta encontrada para exportar");
            }
            
            // Buscar estatísticas
            EstatisticasHistoricoDTO estatisticas = buscarEstatisticas(patrimonioId);
            
            // Delegar para gerador apropriado
            byte[] arquivo;
            switch (formato) {
                case PDF -> arquivo = gerarPDF(historico, estatisticas);
                case EXCEL -> arquivo = gerarExcel(historico, estatisticas);
                default -> throw new BusinessException("Formato não suportado: " + formato);
            }
            
            logger.info("Exportação concluída: {} bytes gerados", arquivo.length);
            
            return arquivo;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Erro ao exportar histórico do patrimônio: {}", patrimonioId, e);
            throw new BusinessException("Erro ao exportar histórico: " + e.getMessage(), e);
        }
    }
    
    /**
     * Converte lista de Maps para lista de DTOs.
     */
    private List<HistoricoColetaDTO> converterParaDTOs(List<Map<String, Object>> coletasMap) {
        List<HistoricoColetaDTO> dtos = new ArrayList<>();
        for (Map<String, Object> map : coletasMap) {
            dtos.add(converterParaDTO(map));
        }
        return dtos;
    }
    
    /**
     * Converte um Map para DTO.
     */
    private HistoricoColetaDTO converterParaDTO(Map<String, Object> map) {
        HistoricoColetaDTO dto = new HistoricoColetaDTO();
        
        dto.setId((Integer) map.get("id"));
        dto.setPatrimonioId((Integer) map.get("patrimonioId"));
        dto.setNumeroPatrimonio((String) map.get("numeroPatrimonio"));
        dto.setDescricaoPatrimonio((String) map.get("descricaoPatrimonio"));
        dto.setInventarioId((Integer) map.get("inventarioId"));
        dto.setNomeInventario((String) map.get("nomeInventario"));
        dto.setColetorId((Integer) map.get("coletorId"));
        dto.setNomeColetorCompleto((String) map.get("nomeColetorCompleto"));
        dto.setDataColeta((Date) map.get("dataColeta"));
        dto.setLocalizacaoEncontrada((String) map.get("localizacaoEncontrada"));
        dto.setEstadoEncontrado((String) map.get("estadoEncontrado"));
        dto.setObservacoes((String) map.get("observacoes"));
        dto.setSalaId((Integer) map.get("salaId"));
        dto.setNomeSala((String) map.get("nomeSala"));
        dto.setSetorId((Integer) map.get("setorId"));
        dto.setNomeSetor((String) map.get("nomeSetor"));
        
        return dto;
    }
    
    /**
     * Identifica mudanças entre coletas consecutivas.
     * Atualiza os DTOs com flags de mudança.
     */
    private void identificarMudancas(List<HistoricoColetaDTO> historico) {
        if (historico.size() < 2) {
            return; // Não há coletas suficientes para comparar
        }
        
        // Lista já vem ordenada DESC (mais recente primeiro)
        for (int i = 0; i < historico.size() - 1; i++) {
            HistoricoColetaDTO atual = historico.get(i);
            HistoricoColetaDTO anterior = historico.get(i + 1);
            
            // Verificar mudança de localização
            if (!Objects.equals(atual.getLocalizacaoEncontrada(), anterior.getLocalizacaoEncontrada())) {
                atual.setTemMudancaLocalizacao(true);
                atual.setLocalizacaoAnterior(anterior.getLocalizacaoEncontrada());
            }
            
            // Verificar mudança de estado
            if (!Objects.equals(atual.getEstadoEncontrado(), anterior.getEstadoEncontrado())) {
                atual.setTemMudancaEstado(true);
                atual.setEstadoAnterior(anterior.getEstadoEncontrado());
            }
        }
    }
    
    /**
     * Gera arquivo PDF com o histórico.
     */
    private byte[] gerarPDF(List<HistoricoColetaDTO> historico, EstatisticasHistoricoDTO estatisticas) 
            throws Exception {
        logger.debug("Gerando PDF com {} coletas", historico.size());
        HistoricoPDFGenerator generator = new HistoricoPDFGenerator();
        return generator.gerarPDF(historico, estatisticas);
    }
    
    /**
     * Gera arquivo Excel com o histórico.
     */
    private byte[] gerarExcel(List<HistoricoColetaDTO> historico, EstatisticasHistoricoDTO estatisticas) 
            throws Exception {
        logger.debug("Gerando Excel com {} coletas", historico.size());
        HistoricoExcelGenerator generator = new HistoricoExcelGenerator();
        return generator.gerarExcel(historico, estatisticas);
    }
}
