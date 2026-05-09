package com.inventario.sihcp.event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.inventario.sihcp.util.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monitora o banco de dados em busca de novas coletas para disparar eventos em tempo real.
 * Funciona como um bridge entre o banco de dados e o DashboardEventBus local.
 */
public class DatabaseEventMonitor {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseEventMonitor.class);
    
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "DatabaseEventMonitor");
        t.setDaemon(true);
        return t;
    });
    
    private int lastMaxIdColeta = -1;
    private int idInventarioAtivo = -1;
    private boolean running = false;

    public void start(int idInventario) {
        if (running) return;
        this.idInventarioAtivo = idInventario;
        this.running = true;
        
        // Inicializar com o ID atual
        this.lastMaxIdColeta = getLatestColetaId(idInventario);
        
        // Agendar verificação a cada 3 segundos
        scheduler.scheduleAtFixedRate(this::checkNewEvents, 3, 3, TimeUnit.SECONDS);
        logger.info("DatabaseEventMonitor iniciado para inventário {}", idInventario);
    }
    
    public void stop() {
        running = false;
        scheduler.shutdown();
        logger.info("DatabaseEventMonitor parado");
    }
    
    private void checkNewEvents() {
        if (!running || idInventarioAtivo <= 0) return;
        
        int currentMaxId = getLatestColetaId(idInventarioAtivo);
        
        if (currentMaxId > lastMaxIdColeta) {
            logger.info("Novos eventos detectados no banco! (Last ID: {}, New ID: {})", lastMaxIdColeta, currentMaxId);
            lastMaxIdColeta = currentMaxId;
            
            // Notificar o EventBus local
            DashboardEvent event = DashboardEvent.builder(DashboardEventType.COLETA_SINCRONIZADA)
                .source("DatabaseEventMonitor")
                .addMetadata("inventarioId", idInventarioAtivo)
                .addMetadata("lastId", currentMaxId)
                .build();
            DashboardEventBus.getInstance().publish(event);
        }
    }
    
    private int getLatestColetaId(int idInventario) {
        String sql = "SELECT MAX(ID) FROM TABELA_COLETA WHERE ID_INVENTARIO = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Erro ao verificar novas coletas: {}", e.getMessage());
        }
        return lastMaxIdColeta;
    }
}
