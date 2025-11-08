package com.inventario.mobile.server.util;

import org.springframework.util.StringUtils;
import java.util.regex.Pattern;

/**
 * Utilitário para validações específicas do mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class MobileValidationUtil {
    
    // Padrões de validação
    private static final Pattern DEVICE_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{8,64}$");
    private static final Pattern APP_VERSION_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+$");
    private static final Pattern CODIGO_PATRIMONIO_PATTERN = Pattern.compile("^[A-Z0-9]{6,20}$");
    
    /**
     * Valida ID do dispositivo
     * 
     * @param deviceId ID do dispositivo
     * @return true se válido
     */
    public static boolean isValidDeviceId(String deviceId) {
        return StringUtils.hasText(deviceId) && DEVICE_ID_PATTERN.matcher(deviceId).matches();
    }
    
    /**
     * Valida versão do aplicativo
     * 
     * @param appVersion versão do app
     * @return true se válido
     */
    public static boolean isValidAppVersion(String appVersion) {
        return StringUtils.hasText(appVersion) && APP_VERSION_PATTERN.matcher(appVersion).matches();
    }
    
    /**
     * Valida código de patrimônio
     * 
     * @param codigo código do patrimônio
     * @return true se válido
     */
    public static boolean isValidCodigoPatrimonio(String codigo) {
        return StringUtils.hasText(codigo) && CODIGO_PATRIMONIO_PATTERN.matcher(codigo).matches();
    }
    
    /**
     * Valida username
     * 
     * @param username nome de usuário
     * @return true se válido
     */
    public static boolean isValidUsername(String username) {
        return StringUtils.hasText(username) && 
               username.length() >= 3 && 
               username.length() <= 50 &&
               username.matches("^[a-zA-Z0-9._-]+$");
    }
    
    /**
     * Valida password
     * 
     * @param password senha
     * @return true se válido
     */
    public static boolean isValidPassword(String password) {
        return StringUtils.hasText(password) && 
               password.length() >= 6 && 
               password.length() <= 100;
    }
    
    /**
     * Valida email
     * 
     * @param email endereço de email
     * @return true se válido
     */
    public static boolean isValidEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }
    
    /**
     * Sanitiza string removendo caracteres especiais
     * 
     * @param input string de entrada
     * @return string sanitizada
     */
    public static String sanitizeString(String input) {
        if (!StringUtils.hasText(input)) {
            return "";
        }
        
        // Remove caracteres de controle e normaliza espaços
        return input.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "")
                   .replaceAll("\\s+", " ")
                   .trim();
    }
    
    /**
     * Valida se o valor está dentro do range permitido
     * 
     * @param value valor a validar
     * @param min valor mínimo
     * @param max valor máximo
     * @return true se válido
     */
    public static boolean isInRange(Number value, Number min, Number max) {
        if (value == null) {
            return false;
        }
        
        double val = value.doubleValue();
        double minVal = min != null ? min.doubleValue() : Double.MIN_VALUE;
        double maxVal = max != null ? max.doubleValue() : Double.MAX_VALUE;
        
        return val >= minVal && val <= maxVal;
    }
    
    /**
     * Valida se a string não excede o tamanho máximo
     * 
     * @param text texto a validar
     * @param maxLength tamanho máximo
     * @return true se válido
     */
    public static boolean isValidLength(String text, int maxLength) {
        return text == null || text.length() <= maxLength;
    }
}