package com.inventario.service;

/**
 * Factory para criar e gerenciar instâncias de Services
 * Implementa Singleton para garantir instância única
 * 
 * Uso:
 * <pre>
 * SetorService setorService = ServiceFactory.getInstance().getSetorService();
 * </pre>
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class ServiceFactory {
    
    private static ServiceFactory instance;
    
    // Services
    private final SetorService setorService;
    private final SalaService salaService;
    private final ResponsavelService responsavelService;
    private final PatrimonioService patrimonioService;
    private final UsuarioService usuarioService;
    private final InventarioService inventarioService;
    private final ColetaService coletaService;
    private final RelatorioService relatorioService;
    private final DashboardService dashboardService;
    private final SalaInventarioService salaInventarioService;
    private final ParticipanteInventarioService participanteInventarioService;
    private final CampusService campusService;
    
    /**
     * Construtor privado (Singleton)
     */
    private ServiceFactory() {
        // Inicializar services
        this.setorService = new SetorService();
        this.salaService = new SalaService();
        this.responsavelService = new ResponsavelService();
        this.patrimonioService = new PatrimonioService();
        this.usuarioService = new UsuarioService();
        this.inventarioService = new InventarioService();
        this.coletaService = new ColetaService();
        this.relatorioService = new RelatorioService();
        this.dashboardService = new DashboardService();
        this.salaInventarioService = new SalaInventarioService();
        this.participanteInventarioService = new ParticipanteInventarioService();
        this.campusService = new CampusService();
    }
    
    /**
     * Obtém instância única do ServiceFactory
     */
    public static synchronized ServiceFactory getInstance() {
        if (instance == null) {
            instance = new ServiceFactory();
        }
        return instance;
    }
    
    /**
     * Obtém instância do SetorService
     */
    public SetorService getSetorService() {
        return setorService;
    }
    
    /**
     * Obtém instância do PatrimonioService
     */
    public PatrimonioService getPatrimonioService() {
        return patrimonioService;
    }
    
    /**
     * Obtém instância do UsuarioService
     */
    public UsuarioService getUsuarioService() {
        return usuarioService;
    }
    
    /**
     * Obtém instância do SalaService
     */
    public SalaService getSalaService() {
        return salaService;
    }
    
    /**
     * Obtém instância do ResponsavelService
     */
    public ResponsavelService getResponsavelService() {
        return responsavelService;
    }
    
    /**
     * Obtém instância do InventarioService
     */
    public InventarioService getInventarioService() {
        return inventarioService;
    }
    
    /**
     * Obtém instância do ColetaService
     */
    public ColetaService getColetaService() {
        return coletaService;
    }
    
    /**
     * Obtém instância do RelatorioService
     */
    public RelatorioService getRelatorioService() {
        return relatorioService;
    }
    
    /**
     * Obtém instância do DashboardService
     */
    public DashboardService getDashboardService() {
        return dashboardService;
    }
    
    /**
     * Obtém instância do SalaInventarioService
     */
    public SalaInventarioService getSalaInventarioService() {
        return salaInventarioService;
    }
    
    /**
     * Obtém instância do ParticipanteInventarioService
     */
    public ParticipanteInventarioService getParticipanteInventarioService() {
        return participanteInventarioService;
    }
    
    /**
     * Obtém instância do CampusService
     */
    public CampusService getCampusService() {
        return campusService;
    }
}
