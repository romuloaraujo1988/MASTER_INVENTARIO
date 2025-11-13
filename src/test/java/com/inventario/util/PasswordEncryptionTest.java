package com.inventario.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para PasswordEncryption
 */
public class PasswordEncryptionTest {
    
    @Test
    public void testEncryptDecrypt() throws Exception {
        String originalPassword = "minhaSenha123";
        
        // Criptografar
        String encrypted = PasswordEncryption.encrypt(originalPassword);
        assertNotNull(encrypted);
        assertNotEquals(originalPassword, encrypted);
        
        // Descriptografar
        String decrypted = PasswordEncryption.decrypt(encrypted);
        assertEquals(originalPassword, decrypted);
    }
    
    @Test
    public void testEmptyPassword() throws Exception {
        String empty = "";
        String encrypted = PasswordEncryption.encrypt(empty);
        assertEquals("", encrypted);
        
        String decrypted = PasswordEncryption.decrypt(encrypted);
        assertEquals("", decrypted);
    }
    
    @Test
    public void testComplexPassword() throws Exception {
        String complex = "P@ssw0rd!@#$%^&*()_+-=[]{}|;:',.<>?/~`";
        
        String encrypted = PasswordEncryption.encrypt(complex);
        String decrypted = PasswordEncryption.decrypt(encrypted);
        
        assertEquals(complex, decrypted);
    }
    
    @Test
    public void testDifferentCiphersForSamePassword() throws Exception {
        String password = "testPassword";
        
        String encrypted1 = PasswordEncryption.encrypt(password);
        String encrypted2 = PasswordEncryption.encrypt(password);
        
        // Cifras devem ser diferentes (devido ao IV aleatório)
        assertNotEquals(encrypted1, encrypted2);
        
        // Mas ambas devem descriptografar para a senha original
        assertEquals(password, PasswordEncryption.decrypt(encrypted1));
        assertEquals(password, PasswordEncryption.decrypt(encrypted2));
    }
    
    @Test
    public void testIsEncrypted() throws Exception {
        String plainText = "plainPassword";
        String encrypted = PasswordEncryption.encrypt(plainText);
        
        assertFalse(PasswordEncryption.isEncrypted(plainText));
        assertTrue(PasswordEncryption.isEncrypted(encrypted));
    }
    
    @Test
    public void testNullPassword() throws Exception {
        String encrypted = PasswordEncryption.encrypt(null);
        assertEquals("", encrypted);
        
        String decrypted = PasswordEncryption.decrypt(null);
        assertEquals("", decrypted);
    }
}
