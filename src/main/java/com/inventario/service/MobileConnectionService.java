package com.inventario.service;

import com.inventario.model.MobileConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Serviço para gerenciar conexões mobile ativas
 * Mantém registro de dispositivos conectados e monitora heartbeats
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@Service
public class MobileConnectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileConnectionService.class);
    
    // Mapa thread-safe para armazenar conexões ativas
    private final Map<String, MobileConnection> activeConnections = new ConcurrentHashMap<>();
    
    // Timer para limpeza automática de conexões expiradas
    private final Timer cleanupTimer;
    
    public MobileConnectionService() {
        // Iniciar timer para limpeza automática a cada 2 minutos
        cleanupTimer = new Timer("MobileConnectionCleanup", true);
        cleanupTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                cleanupExpiredConnections();
            }
        }, 120000, 120000); // 2 minutos
        
        logger.info("MobileConnectionService inicializado com limpeza automática");
    }
    
    /**
     * Registra uma nova conexão mobile
     */
    public MobileConnection registerConnection(String sessionId, String ipAddress, 
                                             String hostname, String username, 
                                             String deviceInfo, String appVersion) {
        
        MobileConnection connection = new MobileConnection(sessionId, ipAddress, hostname, username);
        connection.setDeviceInfo(deviceInfo);
        connection.setAppVersion(appVersion);
        
        activeConnections.put(sessionId, connection);
        
        logger.info("Nova conexão mobile registrada: {} - {} ({})", username, ipAddress, hostname);
        return connection;
    }
    
    /**
     * Atualiza o heartbeat de uma conexão existente
     */
    public boolean updateHeartbeat(String sessionId) {
        MobileConnection connection = activeConnections.get(sessionId);
        if (connection != null) {
            connection.updateHeartbeat();
            logger.debug("Heartbeat atualizado para sessão: {}", sessionId);
            return true;
        }
        
        logger.warn("Tentativa de atualizar heartbeat para sessão inexistente: {}", sessionId);
        return false;
    }
    
    /**
     * Remove uma conexão específica
     */
    public boolean removeConnection(String sessionId) {
        MobileConnection removed = activeConnections.remove(sessionId);
        if (removed != null) {
            logger.info("Conexão mobile removida: {} - {}", removed.getUsername(), removed.getIpAddress());
            return true;
        }
        return false;
    }
    
    /**
     * Retorna todas as conexões ativas
     */
    public List<MobileConnection> getActiveConnections() {
        return new ArrayList<>(activeConnections.values())
                .stream()
                .filter(conn -> !conn.isExpired())
                .collect(Collectors.toList());
    }
    
    /**
     * Retorna o número de conexões ativas
     */
    public int getActiveConnectionCount() {
        return (int) activeConnections.values().stream()
                .filter(conn -> !conn.isExpired())
                .count();
    }
    
    /**
     * Retorna conexões por usuário
     */
    public List<MobileConnection> getConnectionsByUser(String username) {
        return activeConnections.values().stream()
                .filter(conn -> username.equals(conn.getUsername()) && !conn.isExpired())
                .collect(Collectors.toList());
    }
    
    /**
     * Verifica se um usuário específico está online
     */
    public boolean isUserOnline(String username) {
        return activeConnections.values().stream()
                .anyMatch(conn -> username.equals(conn.getUsername()) && !conn.isExpired());
    }
    
    /**
     * Retorna estatísticas das conexões
     */
    public Map<String, Object> getConnectionStats() {
        List<MobileConnection> active = getActiveConnections();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalConnections", active.size());
        stats.put("uniqueUsers", active.stream()
                .map(MobileConnection::getUsername)
                .collect(Collectors.toSet()).size());
        stats.put("uniqueIPs", active.stream()
                .map(MobileConnection::getIpAddress)
                .collect(Collectors.toSet()).size());
        
        // Agrupar por usuário
        Map<String, Long> userConnections = active.stream()
                .collect(Collectors.groupingBy(
                        MobileConnection::getUsername,
                        Collectors.counting()
                ));
        stats.put("connectionsByUser", userConnections);
        
        return stats;
    }
    
    /**
     * Remove conexões expiradas automaticamente
     */
    private void cleanupExpiredConnections() {
        List<String> expiredSessions = activeConnections.entrySet().stream()
                .filter(entry -> entry.getValue().isExpired())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        for (String sessionId : expiredSessions) {
            MobileConnection expired = activeConnections.remove(sessionId);
            if (expired != null) {
                logger.info("Conexão expirada removida: {} - {} (última atividade: {})", 
                           expired.getUsername(), expired.getIpAddress(), expired.getLastHeartbeat());
            }
        }
        
        if (!expiredSessions.isEmpty()) {
            logger.debug("Limpeza automática: {} conexões expiradas removidas", expiredSessions.size());
        }
    }
    
    /**
     * Força a limpeza de todas as conexões (para shutdown)
     */
    public void shutdown() {
        cleanupTimer.cancel();
        activeConnections.clear();
        logger.info("MobileConnectionService finalizado");
    }
}