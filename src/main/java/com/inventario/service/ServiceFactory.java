package com.inventario.service;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Factory para obter instâncias de Services
 * Suporta tanto Spring (quando disponível) quanto instanciação direta (Swing)
 * 
 * Uso:
 * <pre>
 * PatrimonioService service = ServiceFactory.getPatrimonioService();
 * </pre>
 * 
 * @author Sistema de Inventário
 * @version 2.1.0
 */
@Component
public class ServiceFactory implements ApplicationContextAware {
    
    private static ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        ServiceFactory.applicationContext = context;
    }
    
    // Cache de instâncias para aplicação Swing (sem Spring)
    private static PatrimonioService patrimonioServiceInstance;
    private static SetorService setorServiceInstance;
    private static SalaService salaServiceInstance;
    private static ResponsavelService responsavelServiceInstance;
    private static UsuarioService usuarioServiceInstance;
    private static InventarioService inventarioServiceInstance;
    private static ColetaService coletaServiceInstance;
    private static RelatorioService relatorioServiceInstance;
    private static DashboardService dashboardServiceInstance;
    private static SalaInventarioService salaInventarioServiceInstance;
    private static ParticipanteInventarioService participanteInventarioServiceInstance;
    private static CampusService campusServiceInstance;
    
    /**
     * Verifica se o Spring está disponível
     */
    private static boolean isSpringAvailable() {
        return applicationContext != null;
    }
    
    /**
     * Obtém instância do PatrimonioService
     */
    public static PatrimonioService getPatrimonioService() {
        if (isSpringAvailable()) {
            return applicationContext.getBean(PatrimonioService.class);
        }
        if (patrimonioServiceInstance == null) {
            patrimonioServiceInstance = createPatrimonioService();
        }
        return patrimonioServiceInstance;
    }
    
    /**
     * Obtém instância do SetorService
     */
    public static SetorService getSetorService() {
        if (isSpringAvailable()) {
            return applicationContext.getBean(SetorService.class);
        }
        if (setorServiceInstance == null) {
            setorServiceInstance = createSetorService();
        }
        return setorServiceInstance;
    }
    
    /**
     * Obtém instância do SalaService
     */
    public static SalaService getSalaService() {
        if (isSpringAvailable()) {
            return applicationContext.getBean(SalaService.class);
        }
        if (salaServiceInstance == null) {
            salaServiceInstance = createSalaService();
        }
        return salaServiceInstance;
    }
    
    /**
     * Obtém instância do ResponsavelService
     */
    public static ResponsavelService getResponsavelService() {
        if (isSpringAvailable()) {
            return applicationContext.getBean(ResponsavelService.class);
        }
        if (responsavelServiceInstance == null) {
            responsavelServiceInstance = createResponsavelService();
        }
        return responsavelServiceInstance;
    }
    
    /**
     * Obtém instância do UsuarioService
     */
    public static UsuarioService getUsuarioService() {
        if (isSpringAvailable()) {
            return applicationContext.getBean(UsuarioService.class);
        }
        if (usuarioServiceInstance == null) {
            usuarioServiceInstance = createUsuarioService();
        }
        return usuarioServiceInstance;
    }
    
    /**
     * Obtém instância do InventarioService
     */
    public static InventarioService getInventarioService() {
        if (isSpringAvailable()) {
            return applicationContext.getBean(InventarioService.class);
        }
        if (inventarioServiceInstance == null) {
            inventarioServiceInstance = createInventarioService();
        }
        return inventarioServiceInstance;
    }
    
    /**
     * Obtém instância do ColetaService
     */
    public static ColetaService getColetaService() {
        if (isSpringAvailable()) {
            return applicationContext.getBean(ColetaService.class);
        }
        if (coletaServiceInstance == null) {
            coletaServiceInstance = createColetaService();
        }
        return coletaServiceInstance;
    }
    
    /**
     * Obtém instância do RelatorioService
     */
    public static RelatorioService getRelatorioService() {
        if (isSpringAvailable()) {
            return applicationContext.getBean(RelatorioService.class);
        }
        if (relatorioServiceInstance == null) {
            relatorioServiceInstance = createRelatorioService();
        }
        return relatorioServiceInstance;
    }
    
    /**
     * Obtém instância do DashboardService
     */
    public static DashboardService getDashboardService() {
        if (isSpringAvailable()) {
            return applicationContext.getBean(DashboardService.class);
        }
        if (dashboardServiceInstance == null) {
            dashboardServiceInstance = createDashboardService();
        }
        return dashboardServiceInstance;
    }
    
    /**
     * Obtém instância do SalaInventarioService
     */
    public static SalaInventarioService getSalaInventarioService() {
        if (isSpringAvailable()) {
            return applicationContext.getBean(SalaInventarioService.class);
        }
        if (salaInventarioServiceInstance == null) {
            salaInventarioServiceInstance = createSalaInventarioService();
        }
        return salaInventarioServiceInstance;
    }
    
    /**
     * Obtém instância do ParticipanteInventarioService
     */
    public static ParticipanteInventarioService getParticipanteInventarioService() {
        if (isSpringAvailable()) {
            return applicationContext.getBean(ParticipanteInventarioService.class);
        }
        if (participanteInventarioServiceInstance == null) {
            participanteInventarioServiceInstance = createParticipanteInventarioService();
        }
        return participanteInventarioServiceInstance;
    }
    
    /**
     * Obtém instância do CampusService
     */
    public static CampusService getCampusService() {
        if (isSpringAvailable()) {
            return applicationContext.getBean(CampusService.class);
        }
        if (campusServiceInstance == null) {
            campusServiceInstance = createCampusService();
        }
        return campusServiceInstance;
    }
    
    // Métodos privados para criar instâncias sem Spring
    // NOTA: Os Services usam Spring Data JPA Repository e não podem ser instanciados sem Spring
    // Para aplicações Swing, use os DAOs diretamente
    
    private static PatrimonioService createPatrimonioService() {
        throw new UnsupportedOperationException(
            "PatrimonioService requer Spring Framework. " +
            "Para aplicações Swing, use PatrimonioDAO diretamente."
        );
    }
    
    private static SetorService createSetorService() {
        // Criar instância do Service sem Spring para aplicações Swing
        return new SetorService();
    }
    
    private static SalaService createSalaService() {
        throw new UnsupportedOperationException(
            "SalaService requer Spring Framework. " +
            "Para aplicações Swing, use SalaDAO diretamente."
        );
    }
    
    private static ResponsavelService createResponsavelService() {
        // Criar instância do Service sem Spring para aplicações Swing
        return new ResponsavelService();
    }
    
    private static UsuarioService createUsuarioService() {
        // Criar instância do Service sem Spring para aplicações Swing
        return new UsuarioService();
    }
    
    private static InventarioService createInventarioService() {
        // Criar instância do Service sem Spring para aplicações Swing
        return new InventarioService();
    }
    
    private static ColetaService createColetaService() {
        throw new UnsupportedOperationException(
            "ColetaService requer Spring Framework. " +
            "Para aplicações Swing, use ColetaDAO diretamente."
        );
    }
    
    private static RelatorioService createRelatorioService() {
        throw new UnsupportedOperationException(
            "RelatorioService requer Spring Framework. " +
            "Para aplicações Swing, use RelatorioService com DAOs diretamente."
        );
    }
    
    private static DashboardService createDashboardService() {
        throw new UnsupportedOperationException(
            "DashboardService requer Spring Framework. " +
            "Para aplicações Swing, use DashboardService com DAOs diretamente."
        );
    }
    
    private static SalaInventarioService createSalaInventarioService() {
        throw new UnsupportedOperationException(
            "SalaInventarioService requer Spring Framework. " +
            "Para aplicações Swing, use SalaInventarioDAO diretamente."
        );
    }
    
    private static ParticipanteInventarioService createParticipanteInventarioService() {
        throw new UnsupportedOperationException(
            "ParticipanteInventarioService requer Spring Framework. " +
            "Para aplicações Swing, use ParticipanteInventarioDAO diretamente."
        );
    }
    
    private static CampusService createCampusService() {
        throw new UnsupportedOperationException(
            "CampusService requer Spring Framework. " +
            "Para aplicações Swing, use CampusDAO diretamente."
        );
    }
}
