package com.inventario.mobile.server.config;

import com.inventario.service.ConnectedDevicesManager;
import com.inventario.service.ConnectedDevicesManager.ConnectedDevice;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interceptor OTIMIZADO para rastrear dispositivos móveis conectados
 * 
 * v3.0: Otimizado para baixo consumo de memória
 * - Pode ser desabilitado via propriedade
 * - Rastreamento simplificado
 */
@Component
public class DeviceTrackingInterceptor implements HandlerInterceptor {
    
    // Desabilitado por padrão para economizar memória
    @Value("${mobile.server.device-tracking.enabled:false}")
    private boolean trackingEnabled;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Se tracking desabilitado, retorna imediatamente (RÁPIDO)
        if (!trackingEnabled) {
            return true;
        }
        
        // Verificar se é uma requisição da API mobile
        String requestURI = request.getRequestURI();
        
        if (requestURI.startsWith("/api/mobile") || requestURI.startsWith("/inventario/api/mobile")) {
            // Extrair apenas informações essenciais
            String deviceId = request.getHeader("X-Device-ID");
            String username = request.getHeader("X-Username");
            
            // Se tem device ID e username, registrar/atualizar
            if (deviceId != null && username != null) {
                ConnectedDevice device = ConnectedDevicesManager.getDevice(deviceId);
                
                if (device == null) {
                    // Novo dispositivo - registrar com informações mínimas
                    String ipAddress = getClientIpAddress(request);
                    ConnectedDevicesManager.registerDevice(deviceId, username, ipAddress);
                } else {
                    // Dispositivo existente - apenas registrar atividade
                    ConnectedDevicesManager.registerActivity(deviceId);
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
