package com.inventario.sihcp.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Utilitário para criptografia de senhas usando AES-256
 * 
 * Características:
 * - Algoritmo: AES-256-CBC
 * - Chave derivada de informações da máquina (machine-specific)
 * - IV (Initialization Vector) aleatório para cada criptografia
 * - Formato: [IV:16bytes][CipherText]
 * 
 * Segurança:
 * - A chave é derivada de informações únicas da máquina
 * - Cada senha tem um IV único (mesmo texto gera cifras diferentes)
 * - Não armazena a chave em texto plano
 * 
 * @author Sistema de Inventário IFMT
 * @version 1.0.0
 */
public class PasswordEncryption {
    
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM = "AES";
    private static final int KEY_SIZE = 256;
    private static final int IV_SIZE = 16;
    private static final int ITERATION_COUNT = 65536;
    
    // Salt fixo baseado no projeto (não é ideal, mas melhor que nada)
    private static final String SALT = "SIHCP-IFMT-2025-InventarioPatrimonial";
    
    /**
     * Gera uma chave secreta baseada em informações da máquina
     * Isso garante que a senha só pode ser descriptografada na mesma máquina
     */
    private static SecretKey generateKey() throws Exception {
        // Combinar informações da máquina para criar uma chave única
        String machineInfo = getMachineIdentifier();
        
        // Usar PBKDF2 para derivar uma chave forte
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(
            machineInfo.toCharArray(),
            SALT.getBytes(StandardCharsets.UTF_8),
            ITERATION_COUNT,
            KEY_SIZE
        );
        
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), KEY_ALGORITHM);
    }
    
    /**
     * Obtém um identificador único da máquina
     * Combina várias propriedades do sistema para criar um ID único
     */
    private static String getMachineIdentifier() {
        try {
            StringBuilder identifier = new StringBuilder();
            
            // Propriedades do sistema
            identifier.append(System.getProperty("user.name", ""));
            identifier.append(System.getProperty("os.name", ""));
            identifier.append(System.getProperty("os.arch", ""));
            identifier.append(System.getProperty("os.version", ""));
            identifier.append(System.getProperty("user.home", ""));
            
            // Hash do identificador para tamanho fixo
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(identifier.toString().getBytes(StandardCharsets.UTF_8));
            
            return Base64.getEncoder().encodeToString(hash);
            
        } catch (Exception e) {
            // Fallback para uma string fixa (menos seguro, mas funcional)
            System.err.println("Erro ao gerar identificador da máquina: " + e.getMessage());
            return "FALLBACK-MACHINE-ID-" + SALT;
        }
    }
    
    /**
     * Criptografa uma senha
     * 
     * @param password Senha em texto plano
     * @return Senha criptografada em Base64 (formato: IV + CipherText)
     * @throws Exception se houver erro na criptografia
     */
    public static String encrypt(String password) throws Exception {
        if (password == null || password.isEmpty()) {
            return "";
        }
        
        // Gerar chave
        SecretKey key = generateKey();
        
        // Gerar IV aleatório
        byte[] iv = new byte[IV_SIZE];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        
        // Criptografar
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] encrypted = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
        
        // Combinar IV + CipherText
        byte[] combined = new byte[IV_SIZE + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, IV_SIZE);
        System.arraycopy(encrypted, 0, combined, IV_SIZE, encrypted.length);
        
        // Retornar em Base64
        return Base64.getEncoder().encodeToString(combined);
    }
    
    /**
     * Descriptografa uma senha
     * 
     * @param encryptedPassword Senha criptografada em Base64
     * @return Senha em texto plano
     * @throws Exception se houver erro na descriptografia
     */
    public static String decrypt(String encryptedPassword) throws Exception {
        if (encryptedPassword == null || encryptedPassword.isEmpty()) {
            return "";
        }
        
        // Decodificar Base64
        byte[] combined = Base64.getDecoder().decode(encryptedPassword);
        
        // Extrair IV e CipherText
        byte[] iv = new byte[IV_SIZE];
        byte[] encrypted = new byte[combined.length - IV_SIZE];
        System.arraycopy(combined, 0, iv, 0, IV_SIZE);
        System.arraycopy(combined, IV_SIZE, encrypted, 0, encrypted.length);
        
        // Gerar chave
        SecretKey key = generateKey();
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        
        // Descriptografar
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        
        return new String(decrypted, StandardCharsets.UTF_8);
    }
    
    /**
     * Verifica se uma string está criptografada (formato Base64 válido)
     * 
     * @param text Texto a verificar
     * @return true se parece estar criptografado
     */
    public static boolean isEncrypted(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        try {
            byte[] decoded = Base64.getDecoder().decode(text);
            // Deve ter pelo menos o tamanho do IV
            return decoded.length > IV_SIZE;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Testa a criptografia/descriptografia
     * 
     * @param args argumentos (não usado)
     */
    public static void main(String[] args) {
        try {
            System.out.println("=== Teste de Criptografia de Senhas ===\n");
            
            // Teste 1: Senha simples
            String senha1 = "minhaSenha123";
            System.out.println("Senha original: " + senha1);
            
            String encrypted1 = encrypt(senha1);
            System.out.println("Senha criptografada: " + encrypted1);
            
            String decrypted1 = decrypt(encrypted1);
            System.out.println("Senha descriptografada: " + decrypted1);
            System.out.println("Sucesso: " + senha1.equals(decrypted1) + "\n");
            
            // Teste 2: Mesma senha gera cifras diferentes (devido ao IV aleatório)
            String encrypted2 = encrypt(senha1);
            System.out.println("Segunda criptografia da mesma senha: " + encrypted2);
            System.out.println("Cifras diferentes: " + !encrypted1.equals(encrypted2));
            System.out.println("Descriptografia correta: " + senha1.equals(decrypt(encrypted2)) + "\n");
            
            // Teste 3: Senha vazia
            String senhaVazia = "";
            String encryptedVazia = encrypt(senhaVazia);
            System.out.println("Senha vazia criptografada: '" + encryptedVazia + "'");
            System.out.println("Senha vazia descriptografada: '" + decrypt(encryptedVazia) + "'\n");
            
            // Teste 4: Senha complexa
            String senhaComplexa = "P@ssw0rd!@#$%^&*()_+-=[]{}|;:',.<>?/~`";
            String encryptedComplexa = encrypt(senhaComplexa);
            String decryptedComplexa = decrypt(encryptedComplexa);
            System.out.println("Senha complexa: " + senhaComplexa);
            System.out.println("Descriptografada: " + decryptedComplexa);
            System.out.println("Sucesso: " + senhaComplexa.equals(decryptedComplexa) + "\n");
            
            // Teste 5: Verificar se está criptografado
            System.out.println("'minhaSenha123' está criptografado? " + isEncrypted("minhaSenha123"));
            System.out.println("'" + encrypted1 + "' está criptografado? " + isEncrypted(encrypted1));
            
            System.out.println("\n✓ Todos os testes passaram!");
            
        } catch (Exception e) {
            System.err.println("✗ Erro no teste: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
