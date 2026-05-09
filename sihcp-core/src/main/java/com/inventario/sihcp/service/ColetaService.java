package com.inventario.sihcp.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inventario.sihcp.dao.ColetaDAO;
import com.inventario.sihcp.dao.InventarioDAO;
import com.inventario.sihcp.dao.SalaInventarioDAO;
import com.inventario.sihcp.model.Coleta;
import com.inventario.sihcp.model.Inventario;

/**
 * Serviço para operações com Coleta
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@Service
@Transactional
public class ColetaService {
    
    private static final Logger logger = LoggerFactory.getLogger(ColetaService.class);
    
    private final ColetaDAO coletaDAO;
    private final SalaInventarioDAO salaInventarioDAO;
    private final InventarioDAO inventarioDAO;
    
    public ColetaService() {
        this.coletaDAO = new ColetaDAO();
        this.salaInventarioDAO = new SalaInventarioDAO();
        this.inventarioDAO = new InventarioDAO();
    }
    
    public ColetaService(ColetaDAO coletaDAO, SalaInventarioDAO salaInventarioDAO) {
        this.coletaDAO = coletaDAO;
        this.salaInventarioDAO = salaInventarioDAO;
        this.inventarioDAO = new InventarioDAO();
    }
    
    /**
     * Busca coletas por inventário
     */
    public List<Coleta> buscarPorInventario(int idInventario) {
        try {
            return coletaDAO.buscarPorInventario(idInventario);
        } catch (SQLException e) {
            logger.error("Erro ao buscar coletas do inventário: {}", idInventario, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Conta coletas por inventário
     */
    public int contarColetasPorInventario(int idInventario) {
        try {
            return coletaDAO.contarColetasPorInventario(idInventario);
        } catch (SQLException e) {
            logger.error("Erro ao contar coletas do inventário: {}", idInventario, e);
            return 0;
        }
    }
    
    /**
     * Busca coletas por sala e inventário
     * Filtra coletas que pertencem a uma sala específica dentro de um inventário
     * 
     * @param idSala ID da sala
     * @param idInventario ID do inventário
     * @return Lista de coletas da sala no inventário especificado
     */
    public List<Coleta> buscarPorSalaEInventario(int idSala, int idInventario) {
        try {
            // Buscar todas as coletas da sala (através do JOIN com patrimônio)
            List<Coleta> coletasSala = coletaDAO.buscarColetasPorSala(idSala);
            
            // Filtrar apenas as coletas do inventário especificado
            List<Coleta> coletasFiltradas = new ArrayList<>();
            for (Coleta coleta : coletasSala) {
                if (coleta.getIdInventario() == idInventario) {
                    coletasFiltradas.add(coleta);
                }
            }
            
            logger.debug("Encontradas {} coletas da sala {} no inventário {}", 
                    coletasFiltradas.size(), idSala, idInventario);
            
            return coletasFiltradas;
        } catch (SQLException e) {
            logger.error("Erro ao buscar coletas da sala {} no inventário {}", idSala, idInventario, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Registra uma nova coleta
     */
    public boolean registrarColeta(Coleta coleta) throws BusinessException {
        try {
            validarColeta(coleta);
            coletaDAO.inserirColeta(coleta);
            return true;
        } catch (BusinessException | SQLException e) {
            logger.error("Erro ao registrar coleta", e);
            throw new BusinessException("Erro ao registrar coleta: " + e.getMessage());
        }
    }
    
    /**
     * Atualiza uma coleta existente
     */
    public boolean atualizarColeta(Coleta coleta) throws BusinessException {
        try {
            validarColeta(coleta);
            coletaDAO.atualizarColeta(coleta);
            return true;
        } catch (BusinessException | SQLException e) {
            logger.error("Erro ao atualizar coleta", e);
            throw new BusinessException("Erro ao atualizar coleta: " + e.getMessage());
        }
    }
    
    /**
     * Remove uma coleta
     */
    public boolean removerColeta(int idColeta) throws BusinessException {
        try {
            coletaDAO.excluirColeta(idColeta);
            return true;
        } catch (SQLException e) {
            logger.error("Erro ao remover coleta: {}", idColeta, e);
            throw new BusinessException("Erro ao remover coleta: " + e.getMessage());
        }
    }
    
    /**
     * Finaliza coleta de uma sala
     */
    public boolean finalizarColetaSala(int idSala, int idInventario, int idUsuario) throws BusinessException {
        try {
            return salaInventarioDAO.finalizarColeta(idSala, idInventario, idUsuario, "FINALIZADO");
        } catch (Exception e) {
            logger.error("Erro ao finalizar coleta da sala {} no inventário {}", idSala, idInventario, e);
            throw new BusinessException("Erro ao finalizar coleta da sala: " + e.getMessage());
        }
    }
    
    /**
     * Verifica se sala já foi coletada
     * Uma sala é considerada coletada se:
     * 1. Há pelo menos uma coleta registrada para patrimônios dessa sala no inventário
     * 2. OU se o status da sala no inventário está como "FINALIZADO"
     * 
     * @param idSala ID da sala
     * @param idInventario ID do inventário
     * @return true se a sala já foi coletada
     */
    public boolean salaJaColetada(int idSala, int idInventario) {
        try {
            // Verifica se há coletas registradas para a sala neste inventário
            List<Coleta> coletas = buscarPorSalaEInventario(idSala, idInventario);
            
            if (!coletas.isEmpty()) {
                logger.debug("Sala {} já possui {} coletas no inventário {}", 
                        idSala, coletas.size(), idInventario);
                return true;
            }
            
            // Verifica o status da sala no inventário através do SalaInventarioDAO
            try {
                String status = salaInventarioDAO.buscarStatusSala(idSala, idInventario);
                
                if ("FINALIZADO".equals(status) || "COLETADO".equals(status)) {
                    logger.debug("Sala {} está com status '{}' no inventário {}", 
                            idSala, status, idInventario);
                    return true;
                }
            } catch (Exception e) {
                // Se não conseguir verificar o status, considera apenas as coletas
                logger.debug("Não foi possível verificar status da sala no inventário: {}", e.getMessage());
            }
            
            logger.debug("Sala {} ainda não foi coletada no inventário {}", idSala, idInventario);
            return false;
            
        } catch (Exception e) {
            logger.error("Erro ao verificar se sala foi coletada: {} - {}", idSala, idInventario, e);
            // Em caso de erro, retorna false para permitir coleta
            return false;
        }
    }
    
    /**
     * Verifica se sala foi completamente coletada
     * Compara a quantidade de coletas com a quantidade de patrimônios esperados
     * 
     * @param idSala ID da sala
     * @param idInventario ID do inventário
     * @return true se todos os patrimônios da sala foram coletados
     */
    public boolean salaCompletamenteColetada(int idSala, int idInventario) {
        try {
            // Buscar coletas da sala
            List<Coleta> coletas = buscarPorSalaEInventario(idSala, idInventario);
            
            if (coletas.isEmpty()) {
                return false;
            }
            
            // Buscar total de patrimônios da sala
            // Assumindo que há um método no DAO para contar patrimônios
            try {
                com.inventario.sihcp.dao.PatrimonioDAO patrimonioDAO = new com.inventario.sihcp.dao.PatrimonioDAO();
                int totalPatrimonios = patrimonioDAO.contarPatrimoniosPorSala(idSala);
                
                // Contar apenas coletas de patrimônios com etiqueta (não itens sem etiqueta)
                long coletasComEtiqueta = coletas.stream()
                        .filter(c -> !c.isSemEtiqueta())
                        .count();
                
                boolean completo = coletasComEtiqueta >= totalPatrimonios;
                
                logger.debug("Sala {}: {}/{} patrimônios coletados ({})", 
                        idSala, coletasComEtiqueta, totalPatrimonios, 
                        completo ? "COMPLETO" : "INCOMPLETO");
                
                return completo;
                
            } catch (SQLException e) {
                logger.warn("Não foi possível verificar completude da coleta: {}", e.getMessage());
                // Se não conseguir verificar, considera que há coletas
                return !coletas.isEmpty();
            }
            
        } catch (Exception e) {
            logger.error("Erro ao verificar se sala foi completamente coletada: {} - {}", 
                    idSala, idInventario, e);
            return false;
        }
    }
    
    /**
     * Busca coletas por sala
     */
    public List<Coleta> buscarColetasPorSala(int idSala) {
        try {
            return coletaDAO.buscarColetasPorSala(idSala);
        } catch (SQLException e) {
            logger.error("Erro ao buscar coletas da sala: {}", idSala, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Busca coletas sem etiqueta por sala
     */
    public List<Coleta> buscarColetasSemEtiquetaPorSala(int idSala, String localizacaoSala) {
        try {
            return coletaDAO.buscarColetasSemEtiquetaPorSala(idSala, localizacaoSala);
        } catch (SQLException e) {
            logger.error("Erro ao buscar coletas sem etiqueta da sala: {}", idSala, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Busca coletas com etiqueta por localização encontrada
     */
    public List<Coleta> buscarColetasComEtiquetaPorLocalizacaoEncontrada(String localizacaoEncontrada) {
        try {
            return coletaDAO.buscarColetasComEtiquetaPorLocalizacaoEncontrada(localizacaoEncontrada);
        } catch (SQLException e) {
            logger.error("Erro ao buscar coletas por localização: {}", localizacaoEncontrada, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Busca coletas por patrimônio
     */
    public List<Coleta> buscarPorPatrimonio(int idPatrimonio) {
        try {
            return coletaDAO.buscarPorPatrimonio(idPatrimonio);
        } catch (SQLException e) {
            logger.error("Erro ao buscar coletas do patrimônio: {}", idPatrimonio, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Verifica se coleta existe
     */
    public boolean coletaExiste(int idInventario, int idPatrimonio) {
        try {
            return coletaDAO.coletaExiste(idInventario, idPatrimonio);
        } catch (SQLException e) {
            logger.error("Erro ao verificar existência de coleta: {} - {}", idInventario, idPatrimonio, e);
            return false;
        }
    }
    
    /**
     * Agrupa itens sem etiqueta por descrição
     */
    public List<Object[]> agruparItensSemEtiquetaPorDescricao() {
        try {
            return coletaDAO.agruparItensSemEtiquetaPorDescricao();
        } catch (SQLException e) {
            logger.error("Erro ao agrupar itens sem etiqueta", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Exclui coleta por critérios específicos
     */
    public boolean excluirColeta(int idSala, String numeroPatrimonio, String dataHora, String estado, String observacoes) {
        try {
            return coletaDAO.excluirColeta(idSala, numeroPatrimonio, dataHora, estado, observacoes);
        } catch (SQLException e) {
            logger.error("Erro ao excluir coleta por critérios", e);
            return false;
        }
    }
    
    /**
     * Insere uma nova coleta
     */
    public void inserirColeta(Coleta coleta) throws BusinessException {
        try {
            validarColeta(coleta);
            coletaDAO.inserirColeta(coleta);
        } catch (BusinessException | SQLException e) {
            logger.error("Erro ao inserir coleta", e);
            throw new BusinessException("Erro ao inserir coleta: " + e.getMessage());
        }
    }
    
    /**
     * Exclui coleta por ID
     */
    public void excluirColeta(int id) throws BusinessException {
        try {
            coletaDAO.excluirColeta(id);
        } catch (SQLException e) {
            logger.error("Erro ao excluir coleta: {}", id, e);
            throw new BusinessException("Erro ao excluir coleta: " + e.getMessage());
        }
    }
    
    /**
     * Busca a situação de patrimônios em um inventário específico.
     *
     * Regra de negócio:
     * - COLETADO       = há coleta com STATUS = COLETADO
     * - NAO_ENCONTRADO = há coleta com STATUS = NAO_ENCONTRADO
     *                    OU inventário CONCLUIDO/CANCELADO sem coleta
     * - PENDENTE       = inventário EM_ANDAMENTO/PLANEJADO sem coleta
     *
     * @param idInventario ID do inventário
     * @param termoBusca   Número ou descrição parcial (pode ser vazio = todos)
     * @return Lista de mapas com situação de cada patrimônio
     */
    public List<Map<String, Object>> buscarSituacaoItensInventario(int idInventario, String termoBusca) {
        try {
            return coletaDAO.buscarSituacaoPatrimonioNoInventario(idInventario, termoBusca);
        } catch (Exception e) {
            logger.error("Erro ao buscar situação de itens no inventário {}: {}", idInventario, e.getMessage(), e);
            return new java.util.ArrayList<>();
        }
    }

    /**
     * Busca o histórico completo de rastreabilidade de um patrimônio
     * em todos os inventários onde ele foi registrado.
     *
     * @param numeroPatrimonio Número exato do patrimônio
     * @return Lista de mapas com histórico por inventário (mais recentes primeiro)
     */
    public List<Map<String, Object>> buscarHistoricoPatrimonio(String numeroPatrimonio) {
        try {
            return coletaDAO.buscarHistoricoPatrimonioPorNumero(numeroPatrimonio);
        } catch (Exception e) {
            logger.error("Erro ao buscar histórico do patrimônio {}: {}", numeroPatrimonio, e.getMessage(), e);
            return new java.util.ArrayList<>();
        }
    }

    /**
     * Valida dados da coleta
     *
     * CORREÇÃO 27/12/2025: Adicionada validação de status do inventário
     * - Bloqueia coletas se inventário não estiver EM_ANDAMENTO
     * - Impede coletas em inventários CONCLUIDO, CANCELADO ou PLANEJADO
     */
    private void validarColeta(Coleta coleta) throws BusinessException {
        if (coleta == null) {
            throw new BusinessException("Coleta não pode ser nula");
        }
        
        if (coleta.getIdInventario() <= 0) {
            throw new BusinessException("Inventário inválido");
        }
        
        if (coleta.getIdPatrimonio() <= 0 && !coleta.isSemEtiqueta()) {
            throw new BusinessException("Patrimônio inválido");
        }
        
        // VALIDAÇÃO CRÍTICA: Verificar se inventário está EM_ANDAMENTO
        try {
            Inventario inventario = inventarioDAO.findById(coleta.getIdInventario());
            if (inventario == null) {
                throw new BusinessException("Inventário não encontrado: " + coleta.getIdInventario());
            }
            
            if (!inventario.isEmAndamento()) {
                String statusAtual = inventario.getStatusInventario();
                String mensagem;
                
                if (null == statusAtual) {
                    mensagem = "Inventário com status '" + statusAtual + "' não permite coletas. " +
                            "Apenas inventários com status 'EM_ANDAMENTO' aceitam coletas.";
                } else mensagem = switch (statusAtual) {
                    case Inventario.STATUS_CONCLUIDO -> "Este inventário já foi FINALIZADO e não aceita mais coletas. " +
                        "Data de encerramento: " + (inventario.getDataFim() != null ?
                        inventario.getDataFim().toString() : "não registrada");
                    case Inventario.STATUS_CANCELADO -> "Este inventário foi CANCELADO e não aceita coletas.";
                    case Inventario.STATUS_PLANEJADO -> "Este inventário ainda está em PLANEJAMENTO. " +
                        "Aguarde a abertura para iniciar as coletas.";
                    default -> "Inventário com status '" + statusAtual + "' não permite coletas. " +
                        "Apenas inventários com status 'EM_ANDAMENTO' aceitam coletas.";
                };
                
                logger.warn("Tentativa de coleta em inventário não ativo: ID={}, Status={}", 
                        inventario.getId(), statusAtual);
                throw new BusinessException(mensagem);
            }
        } catch (SQLException e) {
            logger.error("Erro ao validar status do inventário: {}", e.getMessage());
            throw new BusinessException("Erro ao validar inventário: " + e.getMessage());
        }
    }
}
