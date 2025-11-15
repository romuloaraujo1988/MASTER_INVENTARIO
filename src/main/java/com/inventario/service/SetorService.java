package com.inventario.service;

import com.inventario.dao.SetorDAO;
import com.inventario.model.Setor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço para operações com Setor
 * Camada de lógica de negócio entre Views e DAOs
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@Service
@Transactional
public class SetorService {
    
    private static final Logger logger = LoggerFactory.getLogger(SetorService.class);
    
    @Autowired
    private SetorDAO setorDAO;
    
    /**
     * Construtor para uso sem Spring (Desktop)
     */
    public SetorService() {
        this.setorDAO = new SetorDAO();
    }
    
    /**
     * Construtor com injeção de dependência (para testes e Spring)
     */
    public SetorService(SetorDAO setorDAO) {
        this.setorDAO = setorDAO;
    }
    
    /**
     * Lista todos os setores ativos ordenados por nome
     */
    public List<Setor> listarSetoresAtivos() {
        try {
            logger.debug("Listando setores ativos");
            return setorDAO.listarSetoresAtivos();
        } catch (SQLException e) {
            logger.error("Erro ao listar setores ativos", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Lista todos os setores (ativos e inativos)
     */
    public List<Setor> listarTodos() {
        try {
            logger.debug("Listando todos os setores");
            return setorDAO.findAll("NOME");
        } catch (SQLException e) {
            logger.error("Erro ao listar setores", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Lista todos os setores (alias para listarTodos)
     */
    public List<Setor> listarSetores() {
        return listarTodos();
    }
    
    /**
     * Busca setor por ID
     */
    public Setor buscarPorId(int id) {
        try {
            logger.debug("Buscando setor por ID: {}", id);
            return setorDAO.findById(id);
        } catch (SQLException e) {
            logger.error("Erro ao buscar setor por ID: {}", id, e);
            return null;
        }
    }
    
    /**
     * Busca setores por termo (nome ou descrição)
     */
    public List<Setor> buscarPorTermo(String termo) {
        try {
            logger.debug("Buscando setores por termo: {}", termo);
            return setorDAO.buscarPorTermo(termo);
        } catch (SQLException e) {
            logger.error("Erro ao buscar setores por termo: {}", termo, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Salva um setor (insert ou update)
     * Valida regras de negócio antes de persistir
     */
    public void salvar(Setor setor) throws BusinessException {
        try {
            logger.info("Salvando setor: {}", setor.getNome());
            
            // Validações de negócio
            validarSetor(setor);
            
            // Verificar duplicidade
            if (setorDAO.setorExiste(setor.getNome(), setor.getId())) {
                throw new BusinessException("Já existe um setor com este nome");
            }
            
            // Persistir
            if (setor.getId() == 0) {
                setorDAO.insert(setor);
                logger.info("Setor criado com sucesso: ID={}", setor.getId());
            } else {
                setorDAO.update(setor);
                logger.info("Setor atualizado com sucesso: ID={}", setor.getId());
            }
            
        } catch (SQLException e) {
            logger.error("Erro ao salvar setor: {}", setor.getNome(), e);
            throw new BusinessException("Erro ao salvar setor: " + e.getMessage(), e);
        }
    }
    
    /**
     * Exclui um setor
     * Verifica se há vinculações antes de excluir
     */
    public void excluir(int id) throws BusinessException {
        try {
            logger.info("Excluindo setor: ID={}", id);
            
            // Verificar vinculações
            int qtdResponsaveis = setorDAO.contarResponsaveisVinculados(id);
            int qtdSalas = setorDAO.contarSalasVinculadas(id);
            
            if (qtdResponsaveis > 0 || qtdSalas > 0) {
                String mensagem = String.format(
                    "Não é possível excluir o setor pois possui %d responsável(is) e %d sala(s) vinculada(s)",
                    qtdResponsaveis, qtdSalas
                );
                throw new BusinessException(mensagem);
            }
            
            setorDAO.delete(id);
            logger.info("Setor excluído com sucesso: ID={}", id);
            
        } catch (SQLException e) {
            logger.error("Erro ao excluir setor: ID={}", id, e);
            throw new BusinessException("Erro ao excluir setor: " + e.getMessage(), e);
        }
    }
    
    /**
     * Ativa ou desativa um setor
     */
    public void alterarStatus(int id, boolean ativo) throws BusinessException {
        try {
            logger.info("Alterando status do setor: ID={}, Ativo={}", id, ativo);
            setorDAO.alterarStatus(id, ativo);
        } catch (SQLException e) {
            logger.error("Erro ao alterar status do setor: ID={}", id, e);
            throw new BusinessException("Erro ao alterar status: " + e.getMessage(), e);
        }
    }
    
    /**
     * Conta responsáveis vinculados ao setor
     */
    public int contarResponsaveisVinculados(int idSetor) {
        try {
            return setorDAO.contarResponsaveisVinculados(idSetor);
        } catch (SQLException e) {
            logger.error("Erro ao contar responsáveis vinculados: ID={}", idSetor, e);
            return 0;
        }
    }
    
    /**
     * Conta salas vinculadas ao setor
     */
    public int contarSalasVinculadas(int idSetor) {
        try {
            return setorDAO.contarSalasVinculadas(idSetor);
        } catch (SQLException e) {
            logger.error("Erro ao contar salas vinculadas: ID={}", idSetor, e);
            return 0;
        }
    }
    
    /**
     * Valida regras de negócio do setor
     */
    private void validarSetor(Setor setor) throws BusinessException {
        if (setor == null) {
            throw new BusinessException("Setor não pode ser nulo");
        }
        
        if (setor.getNome() == null || setor.getNome().trim().isEmpty()) {
            throw new BusinessException("Nome do setor é obrigatório");
        }
        
        if (setor.getNome().length() < 3) {
            throw new BusinessException("Nome do setor deve ter pelo menos 3 caracteres");
        }
        
        if (setor.getNome().length() > 100) {
            throw new BusinessException("Nome do setor deve ter no máximo 100 caracteres");
        }
    }
}
