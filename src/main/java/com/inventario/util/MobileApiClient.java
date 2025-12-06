package com.inventario.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventario.dto.ConnectedDeviceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cliente HTTP para comunicação com a API Mobile
 * Usado pelo MobileMonitorFrame para gerenciar dispositivos conectados
 */
public class MobileApiClient {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileApiClient.class);
    private static final int TIMEOUT_MS = 5000; // 5 segundos
    
    private final String baseUrl;
    private final ObjectMapper objectMapper;
    
    public MobileApiClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Busca lista de dispositivos conectados
     */
    public List<ConnectedDeviceDTO> buscarDispositivosConectados() throws Exception {
        String url = baseUrl + "/api/mobile/v1/connection/active";
        logger.info("Buscando dispositivos conectados: {}", url);
        
        String jsonResponse = doGet(url);
        
        // Parsear JSON response
        JsonNode root = objectMapper.readTree(jsonResponse);
        
        if (!root.has("connections")) {
            logger.warn("Response não contém 'connections': {}", jsonResponse);
            return new ArrayList<>();
        }
        
        JsonNode connections = root.get("connections");
        List<ConnectedDeviceDTO> devices = new ArrayList<>();
        
        for (JsonNode node : connections) {
            try {
                ConnectedDeviceDTO device = objectMapper.treeToValue(node, ConnectedDeviceDTO.class);
                devices.add(device);
            } catch (Exception e) {
                logger.error("Erro ao parsear dispositivo: {}", node, e);
            }
        }
        
        logger.info("Encontrados {} dispositivos conectados", devices.size());
        return devices;
    }
    
    /**
     * Busca estatísticas de conexões
     */
    public Map<String, Object> buscarEstatisticas() throws Exception {
        String url = baseUrl + "/api/mobile/v1/connection/stats";
        logger.info("Buscando estatísticas: {}", url);
        
        String jsonResponse = doGet(url);
        JsonNode root = objectMapper.readTree(jsonResponse);
        
        Map<String, Object> stats = new HashMap<>();
        
        if (root.has("stats")) {
            JsonNode statsNode = root.get("stats");
            stats.put("totalConnections", statsNode.get("totalConnections").asInt());
            stats.put("uniqueUsers", statsNode.get("uniqueUsers").asInt());
        }
        
        return stats;
    }
    
    /**
     * Busca informações de um dispositivo específico
     */
    public ConnectedDeviceDTO buscarDispositivo(String deviceId) throws Exception {
        String url = baseUrl + "/api/mobile/v1/connection/" + deviceId;
        logger.info("Buscando dispositivo: {}", deviceId);
        
        String jsonResponse = doGet(url);
        JsonNode root = objectMapper.readTree(jsonResponse);
        
        if (root.has("device")) {
            return objectMapper.treeToValue(root.get("device"), ConnectedDeviceDTO.class);
        }
        
        return null;
    }
    
    /**
     * Desconecta um dispositivo
     */
    public boolean desconectarDispositivo(String deviceId) throws Exception {
        String url = baseUrl + "/api/mobile/v1/connection/" + deviceId;
        logger.info("Desconectando dispositivo: {}", deviceId);
        
        String jsonResponse = doDelete(url);
        JsonNode root = objectMapper.readTree(jsonResponse);
        
        return root.has("success") && root.get("success").asBoolean();
    }
    
    /**
     * Limpa dispositivos inativos
     */
    public int limparDispositivosInativos() throws Exception {
        String url = baseUrl + "/api/mobile/v1/connection/cleanup";
        logger.info("Limpando dispositivos inativos");
        
        String jsonResponse = doPost(url, "{}");
        JsonNode root = objectMapper.readTree(jsonResponse);
        
        if (root.has("removedCount")) {
            return root.get("removedCount").asInt();
        }
        
        return 0;
    }
    
    /**
     * Testa conexão com o servidor
     */
    public boolean testarConexao() {
        try {
            String url = baseUrl + "/api/mobile/v1/connection/stats";
            doGet(url);
            return true;
        } catch (Exception e) {
            logger.error("Erro ao testar conexão: {}", e.getMessage());
            return false;
        }
    }
    
    // ========== Métodos HTTP Auxiliares ==========
    
    private String doGet(String urlString) throws Exception {
        URL url = URI.create(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            
            int responseCode = conn.getResponseCode();
            
            if (responseCode != 200) {
                String errorMsg = readErrorStream(conn);
                throw new RuntimeException("Erro HTTP " + responseCode + ": " + errorMsg);
            }
            
            return readInputStream(conn);
            
        } finally {
            conn.disconnect();
        }
    }
    
    private String doPost(String urlString, String jsonBody) throws Exception {
        URL url = URI.create(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setDoOutput(true);
            
            // Enviar body
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            int responseCode = conn.getResponseCode();
            
            if (responseCode != 200) {
                String errorMsg = readErrorStream(conn);
                throw new RuntimeException("Erro HTTP " + responseCode + ": " + errorMsg);
            }
            
            return readInputStream(conn);
            
        } finally {
            conn.disconnect();
        }
    }
    
    private String doDelete(String urlString) throws Exception {
        URL url = URI.create(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            
            int responseCode = conn.getResponseCode();
            
            if (responseCode != 200) {
                String errorMsg = readErrorStream(conn);
                throw new RuntimeException("Erro HTTP " + responseCode + ": " + errorMsg);
            }
            
            return readInputStream(conn);
            
        } finally {
            conn.disconnect();
        }
    }
    
    private String readInputStream(HttpURLConnection conn) throws Exception {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            
            StringBuilder response = new StringBuilder();
            String line;
            
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            
            return response.toString();
        }
    }
    
    private String readErrorStream(HttpURLConnection conn) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
            
            StringBuilder response = new StringBuilder();
            String line;
            
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            
            return response.toString();
            
        } catch (Exception e) {
            return "Erro ao ler mensagem de erro";
        }
    }
}
