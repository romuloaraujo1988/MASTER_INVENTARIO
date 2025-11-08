package com.inventario.mobile.server.service;

import com.inventario.mobile.server.dto.MobilePatrimonioDTO;
import com.inventario.mobile.server.dto.MobileSyncRequest;
import com.inventario.mobile.server.dto.MobileSyncResponse;
import com.inventario.model.Patrimonio;
import com.inventario.model.Usuario;
import com.inventario.model.Coleta;
import com.inventario.model.Inventario;
import com.inventario.util.SoundNotification;
import com.inventario.service.PatrimonioService;
import com.inventario.service.UsuarioService;
import com.inventario.dao.ColetaDAO;
import com.inventario.dao.InventarioDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço de sincronização para aplicação mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@Service
@Transactional
public class MobileSyncService {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileSyncService.class);
    
    @Autowired
    private PatrimonioService patrimonioService;
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private ColetaDAO coletaDAO;
    
    @Autowired
    private InventarioDAO inventarioDAO;
    
    /**
     * Sincroniza dados entre mobile e servidor
     * 
     * @param syncRequest dados de sincronização
     * @param username usuário autenticado
     * @return resposta da sincronização
     */
    public MobileSyncResponse syncData(MobileSyncRequest syncRequest, String username) {
        try {
            logger.info("Iniciando sincronização mobile para usuário: {}", username);
            
            Usuario usuario = usuarioService.buscarPorUsername(username);
            if (usuario == null) {
                throw new RuntimeException("Usuário não encontrado: " + username);
            }
            
            MobileSyncResponse response = new MobileSyncResponse();
            List<String> erros = new ArrayList<>();
            int totalProcessados = 0;
            
            // Processar patrimônios coletados pelo mobile
            if (syncRequest.getPatrimoniosColetados() != null && !syncRequest.getPatrimoniosColetados().isEmpty()) {
                totalProcessados = processarPatrimoniosColetados(syncRequest.getPatrimoniosColetados(), usuario, erros);
            }
            
            // Buscar patrimônios atualizados no servidor
            List<MobilePatrimonioDTO> patrimoniosAtualizados = buscarPatrimoniosAtualizados(
                syncRequest.getLastSyncTime(), 
                syncRequest.getSetorId(),
                syncRequest.getSalaId()
            );
            
            // Buscar novos patrimônios
            List<MobilePatrimonioDTO> patrimoniosNovos = buscarPatrimoniosNovos(
                syncRequest.getLastSyncTime(),
                syncRequest.getSetorId(),
                syncRequest.getSalaId()
            );
            
            // Buscar patrimônios removidos
            List<Long> patrimoniosRemovidos = buscarPatrimoniosRemovidos(syncRequest.getLastSyncTime());
            
            response.setPatrimoniosAtualizados(patrimoniosAtualizados);
            response.setPatrimoniosNovos(patrimoniosNovos);
            response.setPatrimoniosRemovidos(patrimoniosRemovidos);
            response.setTotalProcessados(totalProcessados);
            response.setTotalErros(erros.size());
            response.setErros(erros);
            
            logger.info("Sincronização concluída - Processados: {}, Erros: {}, Atualizados: {}, Novos: {}", 
                       totalProcessados, erros.size(), patrimoniosAtualizados.size(), patrimoniosNovos.size());
            
            return response;
            
        } catch (Exception e) {
            logger.error("Erro durante sincronização mobile", e);
            throw new RuntimeException("Erro na sincronização: " + e.getMessage(), e);
        }
    }
    
    /**
     * Processa patrimônios coletados pelo mobile
     */
    private int processarPatrimoniosColetados(List<MobilePatrimonioDTO> patrimoniosColetados, 
                                            Usuario usuario, List<String> erros) {
        int processados = 0;
        
        // Buscar inventário ativo
        Inventario inventarioAtivo = inventarioDAO.buscarInventarioPorStatus("EM_ANDAMENTO");
        if (inventarioAtivo == null) {
            logger.error("Nenhum inventário ativo encontrado para sincronização mobile");
            erros.add("Nenhum inventário ativo encontrado no sistema");
            return 0;
        }
        
        logger.info("Processando coletas para inventário: {} (ID: {})", 
                   inventarioAtivo.getNome(), inventarioAtivo.getId());
        
        for (MobilePatrimonioDTO dto : patrimoniosColetados) {
            try {
                Patrimonio patrimonio = patrimonioService.buscarPorId(dto.getId());
                if (patrimonio != null) {
                    // Criar ou atualizar registro de coleta
                    Coleta coleta = new Coleta();
                    coleta.setIdInventario(inventarioAtivo.getId()); // CORREÇÃO: Definir o ID do inventário
                    coleta.setIdPatrimonio(patrimonio.getId());
                    coleta.setIdColetor(usuario.getId());
                    coleta.setDataColeta(dto.getDataColeta() != null ? 
                        Timestamp.valueOf(dto.getDataColeta()) : 
                        new Timestamp(System.currentTimeMillis()));
                    coleta.setStatusColeta(dto.getColetado() ? "COLETADO" : "NAO_ENCONTRADO");
                    coleta.setObservacaoColeta(dto.getObservacoes());
                    
                    // Atualizar estado do patrimônio se informado
                    if (dto.getEstado() != null) {
                        patrimonio.setEstadoConservacao(dto.getEstado());
                        patrimonioService.salvar(patrimonio);
                    }
                    
                    // Inserir ou atualizar coleta
                    coletaDAO.inserirColeta(coleta);
                    
                    // Reproduzir som de sucesso
                    SoundNotification.playColetaSalvaSound();
                    
                    processados++;
                    
                    logger.debug("Patrimônio {} processado com dados da coleta mobile", dto.getCodigo());
                } else {
                    erros.add("Patrimônio não encontrado: " + dto.getCodigo());
                }
            } catch (Exception e) {
                logger.error("Erro ao processar patrimônio {}", dto.getCodigo(), e);
                erros.add("Erro ao processar patrimônio " + dto.getCodigo() + ": " + e.getMessage());
            }
        }
        
        return processados;
    }
    
    /**
     * Busca patrimônios atualizados no servidor
     */
    private List<MobilePatrimonioDTO> buscarPatrimoniosAtualizados(LocalDateTime lastSync, 
                                                                  Long setorId, Long salaId) {
        try {
            List<Patrimonio> patrimonios = patrimonioService.buscarAtualizadosApos(lastSync, setorId, salaId);
            return patrimonios.stream()
                    .map(this::converterParaDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Erro ao buscar patrimônios atualizados", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Busca novos patrimônios
     */
    private List<MobilePatrimonioDTO> buscarPatrimoniosNovos(LocalDateTime lastSync, 
                                                           Long setorId, Long salaId) {
        try {
            List<Patrimonio> patrimonios = patrimonioService.buscarCriadosApos(lastSync, setorId, salaId);
            return patrimonios.stream()
                    .map(this::converterParaDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Erro ao buscar novos patrimônios", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Busca patrimônios removidos
     */
    private List<Long> buscarPatrimoniosRemovidos(LocalDateTime lastSync) {
        try {
            // Implementar lógica para buscar patrimônios removidos/inativos
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("Erro ao buscar patrimônios removidos", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Converte um objeto Patrimonio para MobilePatrimonioDTO
     */
    private MobilePatrimonioDTO converterParaDTO(Patrimonio patrimonio) {
        MobilePatrimonioDTO dto = new MobilePatrimonioDTO();
        
        dto.setId(Long.valueOf(patrimonio.getId()));
        dto.setCodigo(patrimonio.getNumero());
        dto.setDescricao(patrimonio.getDescricao());
        dto.setMarca(patrimonio.getMarca());
        dto.setModelo(patrimonio.getModelo());
        dto.setNumeroSerie(patrimonio.getNumeroSerie());
        dto.setEstado(patrimonio.getEstadoConservacao());
        dto.setValor(patrimonio.getValorAquisicao() != null ? 
            patrimonio.getValorAquisicao().doubleValue() : null);
        dto.setSetorId(patrimonio.getIdSala() > 0 ? Long.valueOf(patrimonio.getIdSala()) : null);
        dto.setSalaId(patrimonio.getIdSala() > 0 ? Long.valueOf(patrimonio.getIdSala()) : null);
        dto.setResponsavelId(patrimonio.getIdResponsavel() > 0 ? Long.valueOf(patrimonio.getIdResponsavel()) : null);
        dto.setResponsavelNome(patrimonio.getNomeResponsavel());
        dto.setSalaNome(patrimonio.getNomeSala());
        
        // QR Code pode ser gerado baseado no número do patrimônio
        dto.setQrCode("QR_" + patrimonio.getNumero());
        
        // Verificar se existe coleta para este patrimônio
        try {
            List<Coleta> coletas = coletaDAO.buscarPorPatrimonio(patrimonio.getId());
            if (coletas != null && !coletas.isEmpty()) {
                // Pegar a coleta mais recente (primeira da lista, já ordenada por data)
                Coleta coleta = coletas.get(0);
                dto.setColetado("COLETADO".equals(coleta.getStatusColeta()));
                dto.setDataColeta(coleta.getDataColeta() != null ? 
                    coleta.getDataColeta().toLocalDateTime() : null);
                dto.setObservacoes(coleta.getObservacaoColeta());
            } else {
                dto.setColetado(false);
                dto.setDataColeta(null);
                dto.setObservacoes(patrimonio.getObservacoes());
            }
        } catch (Exception e) {
            logger.warn("Erro ao buscar coleta para patrimônio {}: {}", patrimonio.getId(), e.getMessage());
            dto.setColetado(false);
            dto.setDataColeta(null);
            dto.setObservacoes(patrimonio.getObservacoes());
        }
        
        return dto;
    }
}