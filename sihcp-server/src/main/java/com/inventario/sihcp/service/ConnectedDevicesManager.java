package com.inventario.sihcp.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.inventario.sihcp.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gerenciador de dispositivos móveis conectados
 * Rastreia quais smartphones estão conectados ao servidor
 * 
 * v2.0: Otimizado para baixo consumo de memória
 * - Usa SLF4J ao invés de System.out
 * - Limpeza automática de dispositivos inativos
 */
public class ConnectedDevicesManager {
    
    private static final Logger logger = LoggerFactory.getLogger(ConnectedDevicesManager.class);

    private static final Map<String, ConnectedDevice> connectedDevices = new ConcurrentHashMap<>();
    private static final long TIMEOUT_MINUTES = 3; // Reduzido de 5 para 3 minutos
    private static final int MAX_DEVICES = 20; // Reduzido de 50 para 20 dispositivos

    // Contador para limpeza periódica - mais frequente
    private static int operationCount = 0;
    private static final int CLEANUP_INTERVAL = 50; // Reduzido de 100 para 50 operações

    /**
     * Representa um dispositivo conectado
     */
    public static class ConnectedDevice {
        private String deviceId;
        private String username;
        private String deviceModel;
        private String androidVersion;
        private String appVersion;
        private String ipAddress;
        private LocalDateTime lastActivity;
        private LocalDateTime connectedAt;
        private int requestCount;

        public ConnectedDevice(String deviceId, String username, String ipAddress) {
            this.deviceId = deviceId;
            this.username = username;
            this.ipAddress = ipAddress;
            this.connectedAt = LocalDateTime.now();
            this.lastActivity = LocalDateTime.now();
            this.requestCount = 0;
        }

        // Getters e Setters
        public String getDeviceId() {
            return deviceId;
        }

        public String getUsername() {
            return username;
        }

        public String getDeviceModel() {
            return deviceModel;
        }

        public void setDeviceModel(String deviceModel) {
            this.deviceModel = deviceModel;
        }

        public String getAndroidVersion() {
            return androidVersion;
        }

        public void setAndroidVersion(String androidVersion) {
            this.androidVersion = androidVersion;
        }

        public String getAppVersion() {
            return appVersion;
        }

        public void setAppVersion(String appVersion) {
            this.appVersion = appVersion;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public LocalDateTime getLastActivity() {
            return lastActivity;
        }

        public LocalDateTime getConnectedAt() {
            return connectedAt;
        }

        public int getRequestCount() {
            return requestCount;
        }

        public void updateActivity() {
            this.lastActivity = LocalDateTime.now();
            this.requestCount++;
        }

        public boolean isActive() {
            return LocalDateTime.now().minusMinutes(TIMEOUT_MINUTES).isBefore(lastActivity);
        }

        public String getConnectionDuration() {
            long minutes = java.time.Duration.between(connectedAt, LocalDateTime.now()).toMinutes();
            if (minutes < 60) {
                return minutes + " minutos";
            } else {
                long hours = minutes / 60;
                long remainingMinutes = minutes % 60;
                return hours + "h " + remainingMinutes + "m";
            }
        }

        @Override
        public String toString() {
            return String.format("%s (%s) - %s - %s",
                    username, deviceModel != null ? deviceModel : "Desconhecido",
                    ipAddress, isActive() ? "Ativo" : "Inativo");
        }
    }

    /**
     * Registra um novo dispositivo conectado
     * OTIMIZADO: Limita número de dispositivos em memória
     */
    public static void registerDevice(String deviceId, String username, String ipAddress) {
        // Limpeza periódica para evitar acúmulo de memória
        periodicCleanup();

        // Verificar limite de dispositivos
        if (connectedDevices.size() >= MAX_DEVICES) {
            cleanupInactiveDevices();
        }

        ConnectedDevice device = new ConnectedDevice(deviceId, username, ipAddress);
        connectedDevices.put(deviceId, device);

        // Salvar no banco de dados (async para não bloquear)
        try {
            saveDeviceConnection(device);
        } catch (Exception e) {
            // Não falhar se banco não disponível
        }
    }

    /**
     * Limpeza periódica de dispositivos inativos
     */
    private static void periodicCleanup() {
        operationCount++;
        if (operationCount >= CLEANUP_INTERVAL) {
            operationCount = 0;
            cleanupInactiveDevices();
        }
    }

    /**
     * Remove dispositivos inativos da memória
     * PÚBLICO para permitir limpeza externa
     */
    public static void cleanupInactiveDevices() {
        connectedDevices.entrySet().removeIf(entry -> !entry.getValue().isActive());
    }

    /**
     * Atualiza informações do dispositivo
     */
    public static void updateDeviceInfo(String deviceId, String model, String androidVersion, String appVersion) {
        ConnectedDevice device = connectedDevices.get(deviceId);
        if (device != null) {
            device.setDeviceModel(model);
            device.setAndroidVersion(androidVersion);
            device.setAppVersion(appVersion);
            device.updateActivity();
        }
    }

    /**
     * Registra atividade de um dispositivo
     */
    public static void registerActivity(String deviceId) {
        ConnectedDevice device = connectedDevices.get(deviceId);
        if (device != null) {
            device.updateActivity();
        }
    }

    /**
     * Remove um dispositivo (logout)
     */
    public static void removeDevice(String deviceId) {
        ConnectedDevice device = connectedDevices.remove(deviceId);
        if (device != null) {
            logger.debug("Dispositivo desconectado: {}", device);
            updateDeviceDisconnection(deviceId);
        }
    }

    /**
     * Obtém todos os dispositivos conectados (ativos)
     */
    public static List<ConnectedDevice> getConnectedDevices() {
        List<ConnectedDevice> activeDevices = new ArrayList<>();

        // Limpar dispositivos inativos
        connectedDevices.entrySet().removeIf(entry -> !entry.getValue().isActive());

        activeDevices.addAll(connectedDevices.values());
        return activeDevices;
    }

    /**
     * Obtém número de dispositivos conectados
     */
    public static int getConnectedDevicesCount() {
        return getConnectedDevices().size();
    }

    /**
     * Obtém dispositivo por ID
     */
    public static ConnectedDevice getDevice(String deviceId) {
        return connectedDevices.get(deviceId);
    }

    /**
     * Limpa todos os dispositivos
     */
    public static void clearAll() {
        connectedDevices.clear();
    }

    /**
     * Salva conexão no banco de dados
     */
    private static void saveDeviceConnection(ConnectedDevice device) {
        String sql = "INSERT INTO mobile_device_connection " +
                "(device_id, username, device_model, android_version, app_version, " +
                "ip_address, connected_at, last_activity) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (device_id) DO UPDATE SET " +
                "connected_at = EXCLUDED.connected_at, " +
                "last_activity = EXCLUDED.last_activity, " +
                "ip_address = EXCLUDED.ip_address";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, device.getDeviceId());
            stmt.setString(2, device.getUsername());
            stmt.setString(3, device.getDeviceModel());
            stmt.setString(4, device.getAndroidVersion());
            stmt.setString(5, device.getAppVersion());
            stmt.setString(6, device.getIpAddress());
            stmt.setTimestamp(7, Timestamp.valueOf(device.getConnectedAt()));
            stmt.setTimestamp(8, Timestamp.valueOf(device.getLastActivity()));

            stmt.executeUpdate();

        } catch (Exception e) {
            logger.warn("Erro ao salvar conexão do dispositivo: {}", e.getMessage());
            // Não falhar se a tabela não existir
        }
    }

    /**
     * Atualiza desconexão no banco
     */
    private static void updateDeviceDisconnection(String deviceId) {
        String sql = "UPDATE mobile_device_connection SET disconnected_at = ? WHERE device_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(2, deviceId);

            stmt.executeUpdate();

        } catch (Exception e) {
            logger.warn("Erro ao atualizar desconexão: {}", e.getMessage());
        }
    }

    /**
     * Carrega dispositivos conectados do banco (ao iniciar servidor)
     */
    public static void loadConnectedDevicesFromDatabase() {
        String sql = "SELECT device_id, username, device_model, android_version, app_version, " +
                "ip_address, connected_at, last_activity " +
                "FROM mobile_device_connection " +
                "WHERE disconnected_at IS NULL " +
                "AND last_activity > ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now().minusMinutes(TIMEOUT_MINUTES)));

            // CORREÇÃO: try-with-resources para fechar ResultSet automaticamente
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String deviceId = rs.getString("device_id");
                    String username = rs.getString("username");
                    String ipAddress = rs.getString("ip_address");

                    ConnectedDevice device = new ConnectedDevice(deviceId, username, ipAddress);
                    device.setDeviceModel(rs.getString("device_model"));
                    device.setAndroidVersion(rs.getString("android_version"));
                    device.setAppVersion(rs.getString("app_version"));

                    connectedDevices.put(deviceId, device);
                }
            }

            logger.debug("Carregados {} dispositivos do banco", connectedDevices.size());

        } catch (Exception e) {
            logger.warn("Erro ao carregar dispositivos do banco: {}", e.getMessage());
        }
    }

    /**
     * Obtém estatísticas de conexões
     */
    public static String getConnectionStats() {
        List<ConnectedDevice> devices = getConnectedDevices();

        if (devices.isEmpty()) {
            return "Nenhum dispositivo conectado";
        }

        int totalRequests = devices.stream().mapToInt(ConnectedDevice::getRequestCount).sum();

        return String.format("Dispositivos: %d | Total de Requisições: %d",
                devices.size(), totalRequests);
    }
}
