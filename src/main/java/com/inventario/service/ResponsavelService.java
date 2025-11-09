package com.inventario.service;

import com.inventario.dao.ResponsavelDAORefactored;
import com.inventario.model.Responsavel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço para operações com Responsável
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@Service
@Transactional
public class ResponsavelService {
    
    private static final Logger logger = LoggerFactory.getLogger(ResponsavelService.class);
    
    @Autowired
    private ResponsavelDAORefactored responsavelDAO;
    
    public ResponsavelService() {
        this.responsavelDAO = new ResponsavelDAORefactored();
    }
    
    public ResponsavelService(ResponsavelDAORefactored responsavelDAO) {
        this.responsavelDAO = responsavelDAO;
    }
    
    public List<Responsavel> listarTodos() {
        try {
            return responsavelDAO.findAll();
        } catch (SQLException e) {
            logger.error("Erro ao listar responsáveis", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Lista todos os responsáveis (alias para listarTodos)
     */
    public List<Responsavel> listarResponsaveis() {
        return listarTodos();
    }
    
    /**
     * Lista responsáveis por setor
     */
    public List<Responsavel> listarResponsaveisPorSetor(int idSetor) {
        try {
            return responsavelDAO.buscarPorSetor(idSetor);
        } catch (SQLException e) {
            logger.error("Erro ao listar responsáveis por setor: {}", idSetor, e);
            return new ArrayList<>();
        }
    }
    
    public List<Responsavel> listarAtivos() {
        System.out.println("[DEBUG ResponsavelService] ========================================");
        System.out.println("[DEBUG ResponsavelService] Iniciando listarAtivos()");
        System.out.println("[DEBUG ResponsavelService] ResponsavelDAO: " + (responsavelDAO != null ? "OK" : "NULL"));
        
        try {
            // Buscar todos e filtrar ativos localmente
            List<Responsavel> todos = responsavelDAO.findAll();
            System.out.println("[DEBUG ResponsavelService] Total retornado do DAO: " + todos.size());
            
            List<Responsavel> ativos = todos.stream()
                .filter(Responsavel::isAtivo)
                .collect(Collectors.toList());
            
            System.out.println("[DEBUG ResponsavelService] Total após filtro isAtivo(): " + ativos.size());
            System.out.println("[DEBUG ResponsavelService] ========================================");
            
            return ativos;
        } catch (SQLException e) {
            System.err.println("[ERRO ResponsavelService] Erro ao listar responsáveis ativos: " + e.getMessage());
            logger.error("Erro ao listar responsáveis ativos", e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public Responsavel buscarPorId(int id) {
        try {
            return responsavelDAO.findById(id);
        } catch (SQLException e) {
            logger.error("Erro ao buscar responsável por ID: {}", id, e);
            return null;
        }
    }
    
    public List<Responsavel> buscarPorNome(String nome) {
        try {
            return responsavelDAO.buscarPorNome(nome);
        } catch (SQLException e) {
            logger.error("Erro ao buscar responsável por nome: {}", nome, e);
            return new ArrayList<>();
        }
    }
    
    public List<Responsavel> buscarPorFiltro(String filtro) {
        try {
            if (filtro == null || filtro.trim().isEmpty()) {
                return listarAtivos();
            }
            return responsavelDAO.buscarPorFiltro(filtro);
        } catch (SQLException e) {
            logger.error("Erro ao buscar responsáveis por filtro", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Busca responsáveis por filtro com parâmetros específicos
     */
    public List<Responsavel> buscarPorFiltroAvancado(String nome, Integer idSetor, Boolean ativo) {
        try {
            List<Responsavel> resultado = responsavelDAO.findAll();
            
            // Filtrar por nome se fornecido
            if (nome != null && !nome.trim().isEmpty()) {
                String nomeLower = nome.toLowerCase();
                resultado = resultado.stream()
                    .filter(r -> r.getNome().toLowerCase().contains(nomeLower))
                    .collect(Collectors.toList());
            }
            
            // Filtrar por setor se fornecido
            if (idSetor != null && idSetor > 0) {
                resultado = resultado.stream()
                    .filter(r -> r.getIdSetor() == idSetor)
                    .collect(Collectors.toList());
            }
            
            // Filtrar por status ativo se fornecido
            if (ativo != null) {
                resultado = resultado.stream()
                    .filter(r -> r.isAtivo() == ativo)
                    .collect(Collectors.toList());
            }
            
            return resultado;
        } catch (SQLException e) {
            logger.error("Erro ao buscar responsáveis por filtro avançado", e);
            return new ArrayList<>();
        }
    }
    
    public void salvar(Responsavel responsavel) throws BusinessException {
        try {
            validarResponsavel(responsavel);
            
            if (responsavel.getId() == 0) {
                responsavelDAO.insert(responsavel);
                logger.info("Responsável criado: ID={}", responsavel.getId());
            } else {
                responsavelDAO.update(responsavel);
                logger.info("Responsável atualizado: ID={}", responsavel.getId());
            }
        } catch (SQLException e) {
            logger.error("Erro ao salvar responsável", e);
            throw new BusinessException("Erro ao salvar responsável: " + e.getMessage(), e);
        }
    }
    
    public void excluir(int id) throws BusinessException {
        try {
            int qtdPatrimonios = responsavelDAO.contarPatrimoniosDoResponsavel(id);
            if (qtdPatrimonios > 0) {
                throw new BusinessException(
                    String.format("Não é possível excluir o responsável pois possui %d patrimônio(s) vinculado(s)", qtdPatrimonios)
                );
            }
            
            responsavelDAO.delete(id);
            logger.info("Responsável excluído: ID={}", id);
        } catch (SQLException e) {
            logger.error("Erro ao excluir responsável: ID={}", id, e);
            throw new BusinessException("Erro ao excluir responsável: " + e.getMessage(), e);
        }
    }
    
    public void alterarStatus(int id, boolean ativo) throws BusinessException {
        try {
            Responsavel responsavel = responsavelDAO.findById(id);
            if (responsavel != null) {
                responsavel.setAtivo(ativo);
                responsavelDAO.update(responsavel);
            }
        } catch (SQLException e) {
            logger.error("Erro ao alterar status do responsável: ID={}", id, e);
            throw new BusinessException("Erro ao alterar status: " + e.getMessage(), e);
        }
    }
    
    private void validarResponsavel(Responsavel responsavel) throws BusinessException {
        if (responsavel == null) {
            throw new BusinessException("Responsável não pode ser nulo");
        }
        
        if (responsavel.getNome() == null || responsavel.getNome().trim().isEmpty()) {
            throw new BusinessException("Nome do responsável é obrigatório");
        }
        
        if (responsavel.getNome().length() < 3) {
            throw new BusinessException("Nome deve ter pelo menos 3 caracteres");
        }
    }
}
