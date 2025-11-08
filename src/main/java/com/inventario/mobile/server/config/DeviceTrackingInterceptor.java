package com.inventario.mobile.server.config;

import com.inventario.service.ConnectedDevicesManager;
import com.inventario.service.ConnectedDevicesManager.ConnectedDevice;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interceptor para rastrear dispositivos móveis conectados
 * Registra automaticamente atividade de dispositivos em cada requisição
 */
@Component
public class DeviceTrackingInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Verificar se é uma requisição da API mobile
        String requestURI = request.getRequestURI();
        
        if (requestURI.startsWith("/api/mobile") || requestURI.startsWith("/inventario/api/mobile")) {
            // Extrair informações do dispositivo dos headers
            String deviceId = request.getHeader("X-Device-ID");
            String username = request.getHeader("X-Username");
            String deviceModel = request.getHeader("X-Device-Model");
            String androidVersion = request.getHeader("X-Android-Version");
            String appVersion = request.getHeader("X-App-Version");
            String ipAddress = getClientIpAddress(request);
            
            // Se tem device ID e username, registrar/atualizar
            if (deviceId != null && username != null) {
                ConnectedDevice device = ConnectedDevicesManager.getDevice(deviceId);
                
                if (device == null) {
                    // Novo dispositivo
                    ConnectedDevicesManager.registerDevice(deviceId, username, ipAddress);
                    
                    // Atualizar informações adicionais
                    if (deviceModel != null || androidVersion != null || appVersion != null) {
                        ConnectedDevicesManager.updateDeviceInfo(deviceId, deviceModel, androidVersion, appVersion);
                    }
                } else {
                    // Dispositivo existente - apenas registrar atividade
                    ConnectedDevicesManager.registerActivity(deviceId);
                    
                    // Atualizar informações se mudaram
                    if (deviceModel != null || androidVersion != null || appVersion != null) {
                        ConnectedDevicesManager.updateDeviceInfo(deviceId, deviceModel, androidVersion, appVersion);
                    }
                }
            }
        }
        
        return true;
    }
    
    /**
     * Obtém o endereço IP real do cliente, considerando proxies
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };
        
        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Pegar o primeiro IP se houver múltiplos
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }
        
        return request.getRemoteAddr();
    }
}
