package com.inventario.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utilitário para operações com senhas
 * Usa BCrypt para hash seguro de senhas (compatível com Spring Security)
 * 
 * @author Sistema de Inventário
 * @version 2.0.0
 */
public class PasswordUtil {
    
    private static final BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();
    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;
    
    /**
     * Gera hash da senha usando BCrypt
     * @param password Senha em texto plano
     * @return Hash BCrypt da senha
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Senha não pode ser nula ou vazia");
        }
        
        return bcryptEncoder.encode(password);
    }
    
    /**
     * Verifica se a senha corresponde ao hash BCrypt
     * @param password Senha em texto plano
     * @param hashedPassword Hash BCrypt armazenado
     * @return true se a senha corresponde ao hash
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        if (password == null || hashedPassword == null) {
            return false;
        }
        
        try {
            // Tentar verificar com BCrypt primeiro
            if (hashedPassword.startsWith("$2a$") || hashedPassword.startsWith("$2b$") || hashedPassword.startsWith("$2y$")) {
                return bcryptEncoder.matches(password, hashedPassword);
            }
            
            // Fallback para hash antigo com salt (SHA-256)
            try {
                byte[] saltedHash = Base64.getDecoder().decode(hashedPassword);
                
                if (saltedHash.length <= SALT_LENGTH) {
                    return false;
                }
                
                byte[] salt = new byte[SALT_LENGTH];
                System.arraycopy(saltedHash, 0, salt, 0, SALT_LENGTH);
                
                byte[] originalHash = new byte[saltedHash.length - SALT_LENGTH];
                System.arraycopy(saltedHash, SALT_LENGTH, originalHash, 0, originalHash.length);
                
                MessageDigest md = MessageDigest.getInstance(ALGORITHM);
                md.update(salt);
                byte[] testHash = md.digest(password.getBytes("UTF-8"));
                
                return MessageDigest.isEqual(originalHash, testHash);
                
            } catch (Exception e) {
                // Fallback para hash simples
                return verifyPasswordSimple(password, hashedPassword);
            }
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Gera uma senha aleatória
     * @param length Comprimento da senha
     * @return Senha aleatória
     */
    public static String generateRandomPassword(int length) {
        if (length < 6) {
            throw new IllegalArgumentException("Senha deve ter pelo menos 6 caracteres");
        }
        
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }
    
    /**
     * Valida se a senha atende aos critérios de segurança
     * @param password Senha a ser validada
     * @return true se a senha é válida
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        
        boolean hasLetter = false;
        boolean hasDigit = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            }
            if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        
        return hasLetter && hasDigit;
    }
    
    /**
     * Método para compatibilidade com senhas antigas (sem salt)
     * @param password Senha em texto plano
     * @return Hash SHA-256 simples
     */
    public static String hashPasswordSimple(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Senha não pode ser nula ou vazia");
        }
        
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            byte[] hashedPassword = md.digest(password.getBytes("UTF-8"));
            
            // Converter para hexadecimal
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedPassword) {
                sb.append(String.format("%02x", b));
            }
            
            return sb.toString();
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar hash da senha", e);
        }
    }
    
    /**
     * Verifica senha com hash simples (compatibilidade)
     * @param password Senha em texto plano
     * @param hashedPassword Hash simples
     * @return true se corresponde
     */
    public static boolean verifyPasswordSimple(String password, String hashedPassword) {
        if (password == null || hashedPassword == null) {
            return false;
        }
        
        try {
            String testHash = hashPasswordSimple(password);
            return testHash.equals(hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}