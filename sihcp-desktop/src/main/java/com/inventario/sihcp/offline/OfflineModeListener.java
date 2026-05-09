package com.inventario.sihcp.offline;

/**
 * Interface para listeners de mudança de modo offline/online
 * 
 * @author Sistema de Inventário IFMT
 * @version 1.0.0
 */
public interface OfflineModeListener {
    
    /**
     * Chamado quando o sistema entra em modo offline
     */
    void onModoOfflineAtivado();
    
    /**
     * Chamado quando o sistema volta ao modo online
     */
    void onModoOnlineRestaurado();
}
