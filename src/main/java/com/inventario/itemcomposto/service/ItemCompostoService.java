package com.inventario.itemcomposto.service;

import com.inventario.itemcomposto.dao.ComponenteDAO;
import com.inventario.itemcomposto.dao.ItemCompostoDAO;
import com.inventario.itemcomposto.model.Componente;
import com.inventario.itemcomposto.model.ItemComposto;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

/**
 * Service para gerenciar itens compostos.
 * Contém a lógica de negócio para criação, atualização e validação.
 * 
 * @author Sistema de Inventário Patrimonial
 * @version 1.0
 * @since 27/11/2025
 */
@Service
public class ItemCompostoService {
    
    private final ItemCompostoDAO itemCompostoDAO;
    private final ComponenteDAO componenteDAO;
    
    public ItemCompostoService() {
        this.itemCompostoDAO = new ItemCompostoDAO();
        this.componenteDAO = new ComponenteDAO();
    }
    
    /**
     * Cria um novo item composto com seus componentes
     */
    public ItemComposto criarItemComposto(Integer idPatrimonio, List<Componente> componentes, Integer idUsuario) 
            throws SQLException, IllegalStateException {
        
        // Validar
        if (idPatrimonio == null) {
            throw new IllegalArgumentException("ID do patrimônio é obrigatório");
        }
        
        if (componentes == null || componentes.isEmpty()) {
            throw new IllegalArgumentException("Item composto deve ter pelo menos um componente");
        }
        
        // Verificar se já existe
        if (itemCompostoDAO.patrimonioJaEhComposto(idPatrimonio)) {
            throw new IllegalStateException("Patrimônio já é um item composto");
        }
        
        // Criar item composto
        ItemComposto item = new ItemComposto(idPatrimonio);
        item.setIdUsuarioCriacao(idUsuario);
        item.setComponentes(componentes);
        
        // Validar componentes
        for (Componente comp : componentes) {
            comp.validar();
        }
        
        // Inserir no banco
        itemCompostoDAO.insert(item);
        
        // Inserir componentes
        for (int i = 0; i < componentes.size(); i++) {
            Componente comp = componentes.get(i);
            comp.setIdItemComposto(item.getId());
            comp.setOrdem(i);
            componenteDAO.insert(comp);
        }
        
        return item;
    }
    
    /**
     * Atualiza componentes de um item composto
     */
    public void atualizarComponentes(Integer idItemComposto, List<Componente> novosComponentes) 
            throws SQLException {
        
        if (novosComponentes == null || novosComponentes.isEmpty()) {
            throw new IllegalArgumentException("Item composto deve ter pelo menos um componente");
        }
        
        // Validar componentes
        for (Componente comp : novosComponentes) {
            comp.validar();
        }
        
        // Excluir componentes antigos
        componenteDAO.excluirPorItemComposto(idItemComposto);
        
        // Inserir novos componentes
        for (int i = 0; i < novosComponentes.size(); i++) {
            Componente comp = novosComponentes.get(i);
            comp.setIdItemComposto(idItemComposto);
            comp.setOrdem(i);
            componenteDAO.insert(comp);
        }
    }
    
    /**
     * Adiciona um componente a um item composto existente
     */
    public void adicionarComponente(Integer idItemComposto, Componente componente) 
            throws SQLException {
        
        componente.validar();
        componente.setIdItemComposto(idItemComposto);
        
        // Buscar maior ordem atual
        List<Componente> existentes = componenteDAO.listarPorItemComposto(idItemComposto);
        int maxOrdem = existentes.stream()
                .mapToInt(Componente::getOrdem)
                .max()
                .orElse(-1);
        
        componente.setOrdem(maxOrdem + 1);
        componenteDAO.insert(componente);
    }
    
    /**
     * Remove um componente
     */
    public void removerComponente(Integer idComponente) throws SQLException {
        componenteDAO.delete(idComponente);
    }
    
    /**
     * Busca item composto por ID do patrimônio
     */
    public ItemComposto buscarPorPatrimonio(Integer idPatrimonio) throws SQLException {
        return itemCompostoDAO.buscarPorPatrimonio(idPatrimonio);
    }
    
    /**
     * Lista todos os itens compostos
     */
    public List<ItemComposto> listarTodos() throws SQLException {
        return itemCompostoDAO.listarTodos();
    }
    
    /**
     * Verifica se patrimônio já é item composto
     */
    public boolean patrimonioJaEhComposto(Integer idPatrimonio) throws SQLException {
        return itemCompostoDAO.patrimonioJaEhComposto(idPatrimonio);
    }
    
    /**
     * Exclui um item composto (cascade exclui componentes)
     */
    public void excluir(Integer idItemComposto) throws SQLException {
        itemCompostoDAO.delete(idItemComposto);
    }
    
    /**
     * Valida um item composto
     */
    public void validarItemComposto(ItemComposto item) throws IllegalStateException {
        item.validar();
    }
}
