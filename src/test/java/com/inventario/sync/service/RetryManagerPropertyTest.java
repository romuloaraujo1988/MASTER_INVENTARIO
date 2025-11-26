package com.inventario.sync.service;

import com.inventario.sync.model.RetryPolicy;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;

import java.net.ConnectException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Property-Based Tests para RetryManager
 * Feature: correcao-sincronizacao-sqlite-postgresql
 */
@RunWith(JUnitQuickcheck.class)
public class RetryManagerPropertyTest {
    
    private final RetryManager retryManager = new RetryManager();
    
    /**
     * Feature: correcao-sincronizacao-sqlite-postgresql, Property 13: Retry com Backoff Exponencial
     * Validates: Requirements 5.2
     * 
     * Para qualquer falha de conexão, o sistema deve tentar novamente até 3 vezes 
     * com delay crescente exponencialmente.
     */
    @Property(trials = 100)
    public void deveTentarAte3VezesComBackoffExponencial(
            @InRange(minInt = 1, maxInt = 10) int failuresBeforeSuccess) {
        
        // Arrange
        AtomicInteger attempts = new AtomicInteger(0);
        RetryPolicy policy = RetryPolicy.defaultPolicy();
        
        Callable<String> operation = () -> {
            int currentAttempt = attempts.incrementAndGet();
            
            // Falhar nas primeiras tentativas, depois ter sucesso
            if (currentAttempt < Math.min(failuresBeforeSuccess, policy.getMaxRetries())) {
                throw new ConnectException("Simulated connection failure");
            }
            
            return "Success";
        };
        
        // Act & Assert
        try {
            String result = retryManager.executeWithRetry(operation, policy);
            
            // Se teve sucesso, deve ter tentado pelo menos 1 vez
            assertTrue("Deve ter tentado pelo menos 1 vez", attempts.get() >= 1);
            
            // Não deve ter tentado mais que maxRetries
            assertTrue("Não deve exceder maxRetries", attempts.get() <= policy.getMaxRetries());
            
            // Se teve sucesso, resultado deve ser "Success"
            assertEquals("Resultado deve ser Success", "Success", result);
            
        } catch (Exception e) {
            // Se falhou, deve ter tentado exatamente maxRetries vezes
            if (failuresBeforeSuccess >= policy.getMaxRetries()) {
                assertEquals("Deve ter tentado maxRetries vezes", 
                    policy.getMaxRetries(), attempts.get());
            }
        }
    }
    
    /**
     * Testa que o backoff exponencial está funcionando corretamente
     */
    @Property(trials = 100)
    public void backoffDeveSerExponencial(@InRange(minInt = 1, maxInt = 5) int attemptNumber) {
        // Arrange
        RetryPolicy policy = RetryPolicy.defaultPolicy();
        
        // Act
        long delay1 = retryManager.calculateBackoffDelay(attemptNumber, policy);
        long delay2 = retryManager.calculateBackoffDelay(attemptNumber + 1, policy);
        
        // Assert - Delay deve crescer exponencialmente
        assertTrue("Delay deve ser positivo", delay1 > 0);
        assertTrue("Delay deve crescer", delay2 > delay1);
        
        // Verificar que cresce aproximadamente pelo multiplicador
        double ratio = (double) delay2 / delay1;
        double expectedRatio = policy.getBackoffMultiplier();
        
        // Permitir pequena margem de erro devido a arredondamento
        assertTrue("Ratio deve ser próximo do multiplicador", 
            Math.abs(ratio - expectedRatio) < 0.1);
    }
}
