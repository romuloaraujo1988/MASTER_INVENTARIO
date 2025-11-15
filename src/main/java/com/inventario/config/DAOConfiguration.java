package com.inventario.config;

import com.inventario.dao.UsuarioDAO;
import com.inventario.dao.PatrimonioDAO;
import com.inventario.dao.SalaDAO;
import com.inventario.dao.SetorDAO;
import com.inventario.dao.ResponsavelDAO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuração para os DAOs do sistema
 * 
 * Esta classe configura explicitamente os DAOs como beans Spring
 * para garantir que sejam injetados corretamente.
 * 
 * ATUALIZADO: Agora usa DAOs refatorados que herdam de BaseDAO
 * 
 * @author Sistema de Inventário
 * @version 2.0 - Refatorado
 */
@Configuration
public class DAOConfiguration {
    
    /**
     * Configura o UsuarioDAORefactored como bean Spring
     * 
     * @return UsuarioDAORefactored configurado
     */
    @Bean
    @Primary
    public UsuarioDAO usuarioDAO() {
        return new UsuarioDAO();
    }
    
    /**
     * Configura o PatrimonioDAORefactored como bean Spring
     * 
     * @return PatrimonioDAORefactored configurado
     */
    @Bean
    public PatrimonioDAO patrimonioDAO() {
        return new PatrimonioDAO();
    }
    
    /**
     * Configura o SalaDAORefactored como bean Spring
     * 
     * @return SalaDAORefactored configurado
     */
    @Bean
    public SalaDAO salaDAO() {
        return new SalaDAO();
    }
    
    /**
     * Configura o SetorDAORefactored como bean Spring
     * 
     * @return SetorDAORefactored configurado
     */
    @Bean
    public SetorDAO setorDAO() {
        return new SetorDAO();
    }
    
    /**
     * Configura o ResponsavelDAORefactored como bean Spring
     * 
     * @return ResponsavelDAORefactored configurado
     */
    @Bean
    public ResponsavelDAO responsavelDAO() {
        return new ResponsavelDAO();
    }
}