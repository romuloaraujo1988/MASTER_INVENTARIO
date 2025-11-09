package com.inventario.service;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Factory para obter instâncias de Services gerenciados pelo Spring
 * Implementa ApplicationContextAware para acessar beans do Spring
 * 
 * Uso:
 * <pre>
 * PatrimonioService service = ServiceFactory.getPatrimonioService();
 * </pre>
 * 
 * @author Sistema de Inventário
 * @version 2.0.0
 */
@Component
public class ServiceFactory implements ApplicationContextAware {
    
    private static ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        ServiceFactory.applicationContext = context;
    }
    
    /**
     * Obtém instância do PatrimonioService gerenciada pelo Spring
     */
    public static PatrimonioService getPatrimonioService() {
        return applicationContext.getBean(PatrimonioService.class);
    }
    
    /**
     * Obtém instância do SetorService gerenciada pelo Spring
     */
    public static SetorService getSetorService() {
        return applicationContext.getBean(SetorService.class);
    }
    
    /**
     * Obtém instância do SalaService gerenciada pelo Spring
     */
    public static SalaService getSalaService() {
        return applicationContext.getBean(SalaService.class);
    }
    
    /**
     * Obtém instância do ResponsavelService gerenciada pelo Spring
     */
    public static ResponsavelService getResponsavelService() {
        return applicationContext.getBean(ResponsavelService.class);
    }
    
    /**
     * Obtém instância do UsuarioService gerenciada pelo Spring
     */
    public static UsuarioService getUsuarioService() {
        return applicationContext.getBean(UsuarioService.class);
    }
    
    /**
     * Obtém instância do InventarioService gerenciada pelo Spring
     */
    public static InventarioService getInventarioService() {
        return applicationContext.getBean(InventarioService.class);
    }
    
    /**
     * Obtém instância do ColetaService gerenciada pelo Spring
     */
    public static ColetaService getColetaService() {
        return applicationContext.getBean(ColetaService.class);
    }
    
    /**
     * Obtém instância do RelatorioService gerenciada pelo Spring
     */
    public static RelatorioService getRelatorioService() {
        return applicationContext.getBean(RelatorioService.class);
    }
    
    /**
     * Obtém instância do DashboardService gerenciada pelo Spring
     */
    public static DashboardService getDashboardService() {
        return applicationContext.getBean(DashboardService.class);
    }
    
    /**
     * Obtém instância do SalaInventarioService gerenciada pelo Spring
     */
    public static SalaInventarioService getSalaInventarioService() {
        return applicationContext.getBean(SalaInventarioService.class);
    }
    
    /**
     * Obtém instância do ParticipanteInventarioService gerenciada pelo Spring
     */
    public static ParticipanteInventarioService getParticipanteInventarioService() {
        return applicationContext.getBean(ParticipanteInventarioService.class);
    }
    
    /**
     * Obtém instância do CampusService gerenciada pelo Spring
     */
    public static CampusService getCampusService() {
        return applicationContext.getBean(CampusService.class);
    }
}
