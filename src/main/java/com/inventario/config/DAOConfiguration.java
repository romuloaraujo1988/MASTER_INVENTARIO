package com.inventario.config;

import com.inventario.dao.UsuarioDAORefactored;
import com.inventario.dao.PatrimonioDAORefactored;
import com.inventario.dao.SalaDAORefactored;
import com.inventario.dao.SetorDAORefactored;
import com.inventario.dao.ResponsavelDAORefactored;
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
    public UsuarioDAORefactored usuarioDAO() {
        return new UsuarioDAORefactored();
    }
    
    /**
     * Configura o PatrimonioDAORefactored como bean Spring
     * 
     * @return PatrimonioDAORefactored configurado
     */
    @Bean
    public PatrimonioDAORefactored patrimonioDAO() {
        return new PatrimonioDAORefactored();
    }
    
    /**
     * Configura o SalaDAORefactored como bean Spring
     * 
     * @return SalaDAORefactored configurado
     */
    @Bean
    public SalaDAORefactored salaDAO() {
        return new SalaDAORefactored();
    }
    
    /**
     * Configura o SetorDAORefactored como bean Spring
     * 
     * @return SetorDAORefactored configurado
     */
    @Bean
    public SetorDAORefactored setorDAO() {
        return new SetorDAORefactored();
    }
    
    /**
     * Configura o ResponsavelDAORefactored como bean Spring
     * 
     * @return ResponsavelDAORefactored configurado
     */
    @Bean
    public ResponsavelDAORefactored responsavelDAO() {
        return new ResponsavelDAORefactored();
    }
}