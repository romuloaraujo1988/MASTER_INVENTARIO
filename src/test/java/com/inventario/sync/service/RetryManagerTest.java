package com.inventario.sync.service;

import com.inventario.sync.exception.SyncErrorType;
import com.inventario.sync.exception.SyncException;
import com.inventario.sync.model.RetryPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para RetryManager
 * Testa casos específicos de retry
 */
class RetryManagerTest {
    
    private RetryManager retryManager;
    
    @BeforeEach
    void setUp() {
        retryManager = new RetryManager();
    }
    
    /**
     * Teste: Timeout de rede (exemplo)
     * Requirements: 5.1
     */
    @Test
    void deveRetryAposTimeoutDeRede() throws Exception {
        // Arrange
        AtomicInteger attempts = new AtomicInteger(0);
        
        Callable<String> operation = () -> {
            int attempt = attempts.incrementAndGet();
            if (attempt < 2) {
                throw new SocketTimeoutException("Connection timeout");
            }
            return "Success";
        };
        
        // Act
        String result = retryManager.executeWithRetry(operation);
        
        // Assert
        assertEquals("Success", result);
        assertEquals(2, attempts.get(), "Deve ter tentado 2 vezes");
    }
    
    /**
     * Teste: Erro de constraint (exemplo)
     * Requirements: 5.3
     */
    @Test
    void deveRegistrarErroEPularParaProximoRegistroEmConstraint() {
        // Arrange
        SQLException constraintError = new SQLException("Constraint violation", "23505");
        
        // Act
        boolean isRetryable = retryManager.isRetryableError(constraintError);
        
        // Assert
        assertFalse(isRetryable, "Erro de constraint não deve ser recuperável");
    }
    
    /**
     * Teste: Erro de autenticação (exemplo)
     * Requirements: 5.4
     */
    @Test
    void naoDeveRetryEmErroDeAutenticacao() {
        // Arrange
        SyncException authError = new SyncException(
            "Authentication failed",
            SyncErrorType.AUTHENTICATION_ERROR,
            "test"
        );
        
        AtomicInteger attempts = new AtomicInteger(0);
        
        Callable<String> operation = () -> {
            attempts.incrementAndGet();
            throw authError;
        };
        
        // Act & Assert
        assertThrows(SyncException.class, () -> {
            retryManager.executeWithRetry(operation);
        });
        
        // Deve ter tentado apenas 1 vez (sem retry)
        assertEquals(1, attempts.get(), "Não deve fazer retry em erro de autenticação");
    }
    
    @Test
    void deveCalcularBackoffExponencial() {
        // Arrange & Act
        long delay1 = retryManager.calculateBackoffDelay(1);
        long delay2 = retryManager.calculateBackoffDelay(2);
        long delay3 = retryManager.calculateBackoffDelay(3);
        
        // Assert
        assertEquals(5000, delay1, "Primeiro delay deve ser 5000ms");
        assertEquals(10000, delay2, "Segundo delay deve ser 10000ms");
        assertEquals(20000, delay3, "Terceiro delay deve ser 20000ms");
    }
    
    @Test
    void deveIdentificarErrosRecuperaveis() {
        // Arrange
        SocketTimeoutException timeout = new SocketTimeoutException();
        ConnectException connectError = new ConnectException();
        SQLException deadlock = new SQLException("Deadlock", "40001");
        
        // Act & Assert
        assertTrue(retryManager.isRetryableError(timeout), "Timeout deve ser recuperável");
        assertTrue(retryManager.isRetryableError(connectError), "ConnectException deve ser recuperável");
        assertTrue(retryManager.isRetryableError(deadlock), "Deadlock deve ser recuperável");
    }
    
    @Test
    void deveIdentificarErrosNaoRecuperaveis() {
        // Arrange
        SyncException validationError = new SyncException(
            "Validation failed",
            SyncErrorType.VALIDATION_ERROR,
            "test"
        );
        
        SQLException constraintError = new SQLException("Constraint", "23505");
        
        // Act & Assert
        assertFalse(retryManager.isRetryableError(validationError), 
            "Erro de validação não deve ser recuperável");
        assertFalse(retryManager.isRetryableError(constraintError), 
            "Erro de constraint não deve ser recuperável");
    }
    
    @Test
    void deveLancarExcecaoAposMaxRetries() {
        // Arrange
        AtomicInteger attempts = new AtomicInteger(0);
        
        Callable<String> operation = () -> {
            attempts.incrementAndGet();
            throw new ConnectException("Always fails");
        };
        
        // Act & Assert
        assertThrows(RetryManager.MaxRetriesExceededException.class, () -> {
            retryManager.executeWithRetry(operation);
        });
        
        // Deve ter tentado exatamente 3 vezes
        assertEquals(3, attempts.get(), "Deve ter tentado maxRetries vezes");
    }
    
    @Test
    void deveExecutarCallbackDeRetry() throws Exception {
        // Arrange
        AtomicInteger attempts = new AtomicInteger(0);
        AtomicInteger callbackCalls = new AtomicInteger(0);
        
        Callable<String> operation = () -> {
            int attempt = attempts.incrementAndGet();
            if (attempt < 2) {
                throw new ConnectException("Fail");
            }
            return "Success";
        };
        
        RetryManager.RetryCallback callback = (attemptNumber, lastException) -> {
            callbackCalls.incrementAndGet();
        };
        
        // Act
        String result = retryManager.executeWithRetry(
            operation, 
            RetryPolicy.defaultPolicy(), 
            callback
        );
        
        // Assert
        assertEquals("Success", result);
        assertEquals(1, callbackCalls.get(), "Callback deve ser chamado 1 vez (antes do retry)");
    }
    
    @Test
    void deveUsarPoliticaCustomizada() throws Exception {
        // Arrange
        RetryPolicy customPolicy = new RetryPolicy();
        customPolicy.setMaxRetries(5);
        customPolicy.setInitialDelayMs(1000);
        customPolicy.setBackoffMultiplier(1.5);
        
        AtomicInteger attempts = new AtomicInteger(0);
        
        Callable<String> operation = () -> {
            int attempt = attempts.incrementAndGet();
            if (attempt < 4) {
                throw new ConnectException("Fail");
            }
            return "Success";
        };
        
        // Act
        String result = retryManager.executeWithRetry(operation, customPolicy);
        
        // Assert
        assertEquals("Success", result);
        assertEquals(4, attempts.get(), "Deve ter tentado 4 vezes com política customizada");
    }
}
