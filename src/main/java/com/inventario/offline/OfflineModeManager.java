package com.inventario.offline;

import java.awt.Component;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.swing.JOptionPane;

/**
 * Gerenciador de modo offline
 * Controla quais módulos estão disponíveis em modo offline
 * 
 * @author Sistema de Inventário IFMT
 * @version 1.0.0
 */
public class OfflineModeManager {
    
    private static final Logger LOGGER = Logger.getLogger(OfflineModeManager.class.getName());
    private static OfflineModeManager instance;
    
    private boolean modoOfflineForcado = false;
    private boolean dadosSincronizados = false;
    private final List<OfflineModeListener> listeners = new CopyOnWriteArrayList<>();
    
    // Módulos disponíveis em modo offline
    private static final String[] MODULOS_OFFLINE = {
        "COLETA"
    };
    
    private OfflineModeManager() {
        verificarDadosSincronizados();
    }
    
    /**
     * Obtém a instância singleton
     */
    public static synchronized OfflineModeManager getInstance() {
        if (instance == null) {
            instance = new OfflineModeManager();
        }
        return instance;
    }
    
    /**
     * Verifica se está em modo offline
     */
    public boolean isModoOffline() {
        if (modoOfflineForcado) {
            return true;
        }
        
        // Verifica conectividade
        ConnectivityManager connManager = ConnectivityManager.getInstance();
        return !connManager.isOnline();
    }

    
    /**
     * Força o modo offline (apenas se dados estiverem sincronizados)
     */
    public boolean forcarModoOffline() {
        if (!dadosSincronizados) {
            LOGGER.warning("Tentativa de forçar modo offline sem dados sincronizados");
            return false;
        }
        
        modoOfflineForcado = true;
        LOGGER.info("Modo offline forçado");
        notificarMudancaEstado(true);
        return true;
    }
    
    /**
     * Desativa o modo offline forçado
     */
    public void desativarModoOfflineForcado() {
        if (modoOfflineForcado) {
            modoOfflineForcado = false;
            LOGGER.info("Modo offline forçado desativado");
            notificarMudancaEstado(false);
        }
    }
    
    /**
     * Verifica se um módulo está disponível
     */
    public boolean isModuloDisponivel(String modulo) {
        if (!isModoOffline()) {
            return true; // Online: todos disponíveis
        }
        
        // Offline: apenas módulos permitidos
        for (String moduloOffline : MODULOS_OFFLINE) {
            if (moduloOffline.equalsIgnoreCase(modulo)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Verifica acesso a módulo e exibe mensagem se bloqueado
     */
    public boolean verificarAcessoModulo(String modulo, Component parent) {
        if (!isModuloDisponivel(modulo)) {
            JOptionPane.showMessageDialog(parent,
                "Este módulo não está disponível em modo offline.\n" +
                "Conecte-se ao servidor para acessar todos os módulos.",
                "Modo Offline - Acesso Restrito",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    
    /**
     * Verifica se os dados estão sincronizados
     */
    private void verificarDadosSincronizados() {
        try {
            OfflineDAO offlineDAO = new OfflineDAO();
            String lastSync = offlineDAO.obterMetadado("last_sync_timestamp");
            
            if (lastSync != null && !lastSync.isEmpty()) {
                dadosSincronizados = true;
                LOGGER.info("Dados sincronizados encontrados: " + lastSync);
            } else {
                dadosSincronizados = false;
                LOGGER.info("Nenhuma sincronização encontrada");
            }
        } catch (Exception e) {
            dadosSincronizados = false;
            LOGGER.log(Level.WARNING, "Erro ao verificar sincronização", e);
        }
    }
    
    /**
     * Marca dados como sincronizados
     */
    public void marcarDadosSincronizados() {
        dadosSincronizados = true;
        LOGGER.info("Dados marcados como sincronizados");
    }
    
    /**
     * Verifica se dados estão sincronizados
     */
    public boolean isDadosSincronizados() {
        return dadosSincronizados;
    }
    
    /**
     * Adiciona listener de mudança de estado
     */
    public void addListener(OfflineModeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove listener
     */
    public void removeListener(OfflineModeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notifica listeners sobre mudança de estado
     */
    private void notificarMudancaEstado(boolean offline) {
        for (OfflineModeListener listener : listeners) {
            try {
                if (offline) {
                    listener.onModoOfflineAtivado();
                } else {
                    listener.onModoOnlineRestaurado();
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Erro ao notificar listener", e);
            }
        }
    }
    
    /**
     * Obtém informações do sistema
     */
    public String getSystemInfo() {
        StringBuilder info = new StringBuilder();
        info.append("=== MODO OFFLINE ===\n");
        info.append("Modo Offline: ").append(isModoOffline() ? "Sim" : "Não").append("\n");
        info.append("Modo Forçado: ").append(modoOfflineForcado ? "Sim" : "Não").append("\n");
        info.append("Dados Sincronizados: ").append(dadosSincronizados ? "Sim" : "Não").append("\n");
        info.append("Módulos Disponíveis Offline: ").append(String.join(", ", MODULOS_OFFLINE)).append("\n");
        return info.toString();
    }
}
