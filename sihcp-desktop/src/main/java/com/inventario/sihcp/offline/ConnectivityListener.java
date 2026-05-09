package com.inventario.sihcp.offline;

/**
 * Interface para receber notificações sobre mudanças de conectividade
 * 
 * @author Sistema de Inventário IFMT
 * @version 1.0.0
 */
public interface ConnectivityListener {
    
    /**
     * Chamado quando a conexão com a internet é estabelecida
     */
    void onConnectionEstablished();
    
    /**
     * Chamado quando a conexão com a internet é perdida
     */
    void onConnectionLost();
}