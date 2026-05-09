package com.inventario.sihcp.mobile.server.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.inventario.sihcp.dao.PatrimonioDAO;
import com.inventario.sihcp.mobile.server.dto.MobilePatrimonioDTO;
import com.inventario.sihcp.mobile.server.dto.PatrimonioDetalheDTO;
import com.inventario.sihcp.model.Patrimonio;

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
    private final com.inventario.sihcp.dao.ColetaDAO coletaDAO;
    private final com.inventario.sihcp.dao.InventarioDAO inventarioDAO;
    private final com.inventario.sihcp.dao.SalaDAO salaDAO;
    
    public MobileConsultaService() {
        this.patrimonioDAO = new PatrimonioDAO();
        this.coletaDAO = new com.inventario.sihcp.dao.ColetaDAO();
        this.inventarioDAO = new com.inventario.sihcp.dao.InventarioDAO();
        this.salaDAO = new com.inventario.sihcp.dao.SalaDAO();
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
        logger.debug("Buscando patrimônios por código parcial: '{}' (limit: {})", codigo, limit);
        
        List<Patrimonio> patrimonios = patrimonioDAO.buscarPorCodigoParcial(codigo, limit);
        
        logger.debug("Encontrados {} patrimônios", patrimonios.size());
        
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
        logger.debug("Buscando patrimônios por descrição: '{}' (limit: {})", descricao, limit);
        
        List<Patrimonio> patrimonios = patrimonioDAO.buscarPorDescricao(descricao, limit);
        
        logger.debug("Encontrados {} patrimônios", patrimonios.size());
        
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
        logger.debug("Buscando detalhes completos do patrimônio ID: {}", id);
        
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
                com.inventario.sihcp.model.Sala sala = salaDAO.buscarSalaPorId(patrimonio.getIdSala());
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
            com.inventario.sihcp.model.Inventario inventarioAtivo = inventarioDAO.buscarInventarioAtivo();
            
            if (inventarioAtivo != null) {
                // Buscar coleta completa do patrimônio no inventário ativo
                com.inventario.sihcp.model.Coleta coleta = coletaDAO.buscarColetaPorPatrimonioEInventario(
                    inventarioAtivo.getId(),
                    patrimonio.getId()
                );
                
                if (coleta != null) {
                    detalhes.setColetado(true);
                    detalhes.setDataColeta(coleta.getDataColeta());
                    detalhes.setLocalizacaoEncontrada(coleta.getLocalizacaoEncontrada());
                    detalhes.setEstadoEncontrado(coleta.getEstadoEncontrado());
                    detalhes.setObservacoesColeta(coleta.getObservacaoColeta());
                    
                    // Buscar nome do coletor
                    if (coleta.getIdParticipanteInventario() > 0) {
                        try {
                            String nomeColetor = buscarNomeColetor(coleta.getIdParticipanteInventario());
                            detalhes.setColetadoPor(nomeColetor);
                        } catch (Exception e) {
                            logger.warn("Erro ao buscar nome do coletor: {}", e.getMessage());
                        }
                    } else if (coleta.getIdColetor() > 0) {
                        try {
                            String nomeColetor = buscarNomeColetorPorIdUsuario(coleta.getIdColetor());
                            detalhes.setColetadoPor(nomeColetor);
                        } catch (Exception e) {
                            logger.warn("Erro ao buscar nome do coletor por ID_USUARIO: {}", e.getMessage());
                        }
                    }
                    
                    logger.debug("Coleta encontrada: data={}, local={}, estado={}, coletor={}", 
                            coleta.getDataColeta(), coleta.getLocalizacaoEncontrada(), 
                            coleta.getEstadoEncontrado(), detalhes.getColetadoPor());
                } else {
                    detalhes.setColetado(false);
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
            logger.debug("Histórico de coletas carregado: {} registros", historico.size());
        } catch (Exception e) {
            logger.warn("Erro ao buscar histórico de coletas do patrimônio {}: {}", id, e.getMessage());
            detalhes.setHistoricoColetas(new java.util.ArrayList<>());
        }
        
        logger.debug("Detalhes do patrimônio {} carregados", id);
        
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
        
        logger.debug("Busca avançada: termo='{}', salaId={}, responsavelId={}, limit={}", 
                termo, salaId, responsavelId, limit);
        
        List<Patrimonio> patrimonios = patrimonioDAO.buscarAvancada(
                termo, salaId, responsavelId, limit);
        
        logger.debug("Busca avançada encontrou {} patrimônios", patrimonios.size());
        
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
            com.inventario.sihcp.model.Inventario inventarioAtivo = inventarioDAO.buscarInventarioAtivo();
            
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
    
    /**
     * Busca o nome do coletor pelo ID do participante do inventário
     * 
     * @param idParticipante ID do participante do inventário
     * @return Nome do coletor ou null se não encontrado
     */
    private String buscarNomeColetor(int idParticipante) throws SQLException {
        String sql = "SELECT u.NOME_COMPLETO " +
                    "FROM TABELA_PARTICIPANTE_INVENTARIO pi " +
                    "JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID " +
                    "WHERE pi.ID_PARTICIPANTE = ?";
        
        try (java.sql.Connection conn = com.inventario.sihcp.util.DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idParticipante);
            
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("NOME_COMPLETO");
                }
            }
        }
        
        return null;
    }
    
    /**
     * Busca o nome do coletor pelo ID do usuário (compatibilidade com estrutura antiga)
     * 
     * @param idUsuario ID do usuário
     * @return Nome do coletor ou null se não encontrado
     */
    private String buscarNomeColetorPorIdUsuario(int idUsuario) throws SQLException {
        String sql = "SELECT NOME_COMPLETO FROM TABELA_USUARIO WHERE ID = ?";
        
        try (java.sql.Connection conn = com.inventario.sihcp.util.DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsuario);
            
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("NOME_COMPLETO");
                }
            }
        }
        
        return null;
    }
}
