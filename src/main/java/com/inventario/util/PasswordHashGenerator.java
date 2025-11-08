package com.inventario.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utilitário para gerar hash BCrypt de senhas
 */
public class PasswordHashGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Gerar hash para senha 'admin'
        String password = "admin";
        String hash = encoder.encode(password);
        
        System.out.println("Senha: " + password);
        System.out.println("Hash BCrypt: " + hash);
        System.out.println();
        System.out.println("SQL para atualizar:");
        System.out.println("UPDATE usuario SET senha_hash = '" + hash + "' WHERE login = 'admin';");
    }
}
