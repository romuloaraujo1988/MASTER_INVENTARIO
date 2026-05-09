package com.inventario.sihcp.mobile.server.controller;

import com.inventario.sihcp.service.ConnectedDevicesManager;
import com.inventario.sihcp.service.ConnectedDevicesManager.ConnectedDevice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller REST para gerenciar e monitorar conexões mobile
 */
@RestController
@RequestMapping("/api/mobile/v1/connection")
// @CrossOrigin removido — ver MobileSecurityConfig.corsConfigurationSource() (spec correcoes-seguranca Req 6.1)
public class MobileConnectionController {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    /**
     * Retorna todas as conexões ativas
     */
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveConnections() {
        try {
            List<ConnectedDevice> devices = ConnectedDevicesManager.getConnectedDevices();
            
            List<Map<String, Object>> connections = devices.stream()
                .map(this::deviceToMap)
                .collect(Collectors.toList());
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalConnections", devices.size());
            stats.put("uniqueUsers", devices.stream()
                .map(ConnectedDevice::getUsername)
                .distinct()
                .count());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("connections", connections);
            response.put("stats", stats);
            response.put("timestamp", new Date());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Erro ao buscar conexões: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Retorna estatísticas de conexões
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getConnectionStats() {
        try {
            List<ConnectedDevice> devices = ConnectedDevicesManager.getConnectedDevices();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalConnections", devices.size());
            stats.put("uniqueUsers", devices.stream()
                .map(ConnectedDevice::getUsername)
                .distinct()
                .count());
            stats.put("totalRequests", devices.stream()
                .mapToInt(ConnectedDevice::getRequestCount)
                .sum());
            stats.put("averageConnectionDuration", calculateAverageConnectionDuration(devices));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Erro ao buscar estatísticas: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Retorna informações de um dispositivo específico
     */
    @GetMapping("/{deviceId}")
    public ResponseEntity<Map<String, Object>> getDeviceInfo(@PathVariable String deviceId) {
        try {
            ConnectedDevice device = ConnectedDevicesManager.getDevice(deviceId);
            
            if (device == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Dispositivo não encontrado");
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("device", deviceToMap(device));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Erro ao buscar dispositivo: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Desconecta um dispositivo (força logout)
     */
    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Map<String, Object>> disconnectDevice(@PathVariable String deviceId) {
        try {
            ConnectedDevicesManager.removeDevice(deviceId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Dispositivo desconectado com sucesso");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Erro ao desconectar dispositivo: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Limpa todas as conexões inativas
     */
    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupInactiveConnections() {
        try {
            int beforeCount = ConnectedDevicesManager.getConnectedDevicesCount();
            
            // Forçar limpeza de inativos
            List<ConnectedDevice> devices = ConnectedDevicesManager.getConnectedDevices();
            
            int afterCount = devices.size();
            int removed = beforeCount - afterCount;
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Limpeza concluída");
            response.put("removedConnections", removed);
            response.put("activeConnections", afterCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Erro ao limpar conexões: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Converte ConnectedDevice para Map
     */
    private Map<String, Object> deviceToMap(ConnectedDevice device) {
        Map<String, Object> map = new HashMap<>();
        map.put("deviceId", device.getDeviceId());
        map.put("username", device.getUsername());
        map.put("deviceInfo", device.getDeviceModel());
        map.put("androidVersion", device.getAndroidVersion());
        map.put("appVersion", device.getAppVersion());
        map.put("ipAddress", device.getIpAddress());
        map.put("hostname", getHostnameFromIp(device.getIpAddress()));
        map.put("connectedAt", device.getConnectedAt().format(DATE_FORMATTER));
        map.put("lastHeartbeat", device.getLastActivity().format(DATE_FORMATTER));
        map.put("connectionDuration", device.getConnectionDuration());
        map.put("requestCount", device.getRequestCount());
        map.put("isActive", device.isActive());
        return map;
    }
    
    /**
     * Tenta obter hostname do IP
     */
    private String getHostnameFromIp(String ip) {
        try {
            java.net.InetAddress addr = java.net.InetAddress.getByName(ip);
            return addr.getHostName();
        } catch (Exception e) {
            return ip;
        }
    }
    
    /**
     * Calcula duração média de conexão
     */
    private String calculateAverageConnectionDuration(List<ConnectedDevice> devices) {
        if (devices.isEmpty()) {
            return "0 minutos";
        }
        
        long totalMinutes = devices.stream()
            .mapToLong(device -> {
                return java.time.Duration.between(
                    device.getConnectedAt(), 
                    java.time.LocalDateTime.now()
                ).toMinutes();
            })
            .sum();
        
        long avgMinutes = totalMinutes / devices.size();
        
        if (avgMinutes < 60) {
            return avgMinutes + " minutos";
        } else {
            long hours = avgMinutes / 60;
            long minutes = avgMinutes % 60;
            return hours + "h " + minutes + "m";
        }
    }
}
