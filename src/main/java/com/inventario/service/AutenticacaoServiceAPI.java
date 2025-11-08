package com.inventario.service;

import com.inventario.model.Usuario;
import com.inventario.model.PerfilUsuario;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Serviço de autenticação via API REST
 * Conecta-se ao servidor mobile para autenticar usuários
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class AutenticacaoServiceAPI {
    
    private static final String API_BASE_URL = "http://localhost:8081/inventario/api/mobile";
    private static final int TIMEOUT = 5000; // 5 segundos
    private final ObjectMapper objectMapper;
    
    public AutenticacaoServiceAPI() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Autentica um usuário via API REST
     * @param login Login do usuário
     * @param senha Senha em texto plano
     * @return Usuario autenticado ou null se falhou
     */
    public Usuario autenticar(String login, String senha) {
        if (login == null || senha == null || login.trim().isEmpty() || senha.trim().isEmpty()) {
            System.err.println("Login ou senha vazios");
            return null;
        }
        
        HttpURLConnection conn = null;
        try {
            // Preparar URL do endpoint de login
            URL url = new URL(API_BASE_URL + "/auth/login");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            
            // Preparar corpo da requisição
            String jsonInputString = String.format(
                "{\"username\":\"%s\",\"password\":\"%s\"}", 
                login.trim(), 
                senha
            );
            
            System.out.println("Enviando requisição de autenticação para: " + url);
            System.out.println("Payload: " + jsonInputString);
            
            // Enviar requisição
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Verificar resposta
            int responseCode = conn.getResponseCode();
            System.out.println("Código de resposta: " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Ler resposta
                String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                System.out.println("Resposta da API: " + response);
                
                // Parsear resposta JSON
                JsonNode jsonResponse = objectMapper.readTree(response);
                
                // Extrair dados do usuário
                Usuario usuario = new Usuario();
                usuario.setId(jsonResponse.path("id").asInt());
                usuario.setLogin(jsonResponse.path("username").asText());
                usuario.setNomeCompleto(jsonResponse.path("nomeCompleto").asText());
                usuario.setEmail(jsonResponse.path("email").asText());
                usuario.setMatricula(jsonResponse.path("matricula").asText());
                
                // Mapear perfil
                String perfilStr = jsonResponse.path("perfil").asText();
                try {
                    usuario.setPerfil(PerfilUsuario.valueOf(perfilStr));
                } catch (IllegalArgumentException e) {
                    // Usar perfil padrão se não conseguir mapear
                    usuario.setPerfil(PerfilUsuario.COLETOR);
                }
                
                usuario.setAtivo(true);
                usuario.setBloqueado(false);
                usuario.setDataUltimoAcesso(LocalDateTime.now());
                
                System.out.println("Usuário autenticado com sucesso via API: " + login);
                return usuario;
                
            } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                System.err.println("Credenciais inválidas para usuário: " + login);
                return null;
                
            } else {
                String errorResponse = "";
                try {
                    errorResponse = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                } catch (Exception e) {
                    // Ignorar erro ao ler stream de erro
                }
                System.err.println("Erro na autenticação. Código: " + responseCode + ", Resposta: " + errorResponse);
                return null;
            }
            
        } catch (java.net.ConnectException e) {
            System.err.println("Erro de conexão: Servidor não está acessível em " + API_BASE_URL);
            System.err.println("Verifique se o servidor mobile está rodando na porta 8081");
            return null;
            
        } catch (java.net.SocketTimeoutException e) {
            System.err.println("Timeout ao conectar com o servidor: " + API_BASE_URL);
            return null;
            
        } catch (Exception e) {
            System.err.println("Erro ao autenticar via API: " + e.getMessage());
            e.printStackTrace();
            return null;
            
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
    
    /**
     * Verifica se o servidor está disponível
     * @return true se o servidor está acessível
     */
    public boolean verificarConexao() {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(API_BASE_URL + "/test/ping");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            
            int responseCode = conn.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;
            
        } catch (Exception e) {
            System.err.println("Servidor não está acessível: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
