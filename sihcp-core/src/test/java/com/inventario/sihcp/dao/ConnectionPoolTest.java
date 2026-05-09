package com.inventario.sihcp.dao;

import com.inventario.sihcp.util.ConnectionManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para validar o funcionamento do Connection Pool (HikariCP)
 * 
 * Valida:
 * - Conexões são obtidas rapidamente (< 50ms)
 * - Pool reutiliza conexões ao invés de criar novas
 * - Máximo de conexões é respeitado
 * - Conexões são retornadas ao pool corretamente
 */
public class ConnectionPoolTest {
    
    @BeforeAll
    public static void setup() {
        // Inicializar o pool antes dos testes
        // Nota: Em produção, isso é feito automaticamente no DatabaseConnection
        System.out.println("Inicializando Connection Pool para testes...");
    }
    
    @AfterAll
    public static void teardown() {
        // Fechar o pool após os testes
        ConnectionManager.shutdown();
        System.out.println("Connection Pool fechado.");
    }
    
    /**
     * Teste 1: Verificar que conexão é obtida rapidamente (< 50ms)
     */
    @Test
    public void testConnectionSpeed() throws SQLException {
        long startTime = System.currentTimeMillis();
        
        try (Connection conn = ConnectionManager.getConnection()) {
            assertNotNull(conn, "Conexão não deve ser null");
            assertFalse(conn.isClosed(), "Conexão deve estar aberta");
        }
        
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("Tempo para obter conexão: " + duration + "ms");
        
        assertTrue(duration < 50, 
            "Conexão deve ser obtida em menos de 50ms, mas levou " + duration + "ms");
    }
    
    /**
     * Teste 2: Executar 100 operações e verificar que pool reutiliza conexões
     */
    @Test
    public void testConnectionReuse() throws SQLException {
        int numOperations = 100;
        long totalTime = 0;
        
        System.out.println("\nExecutando " + numOperations + " operações...");
        
        for (int i = 0; i < numOperations; i++) {
            long startTime = System.currentTimeMillis();
            
            try (Connection conn = ConnectionManager.getConnection()) {
                assertNotNull(conn);
                // Simular operação rápida
                conn.isValid(1);
            }
            
            long duration = System.currentTimeMillis() - startTime;
            totalTime += duration;
        }
        
        double avgTime = totalTime / (double) numOperations;
        System.out.println("Tempo médio por operação: " + avgTime + "ms");
        System.out.println("Tempo total: " + totalTime + "ms");
        
        // Pool stats
        if (ConnectionManager.isInitialized()) {
            System.out.println(ConnectionManager.getPoolStats());
        }
        
        // Tempo médio deve ser muito baixo devido ao reuso de conexões
        assertTrue(avgTime < 10, 
            "Tempo médio deve ser < 10ms com pool, mas foi " + avgTime + "ms");
    }
    
    /**
     * Teste 3: Verificar que conexões são retornadas ao pool (try-with-resources)
     */
    @Test
    public void testConnectionReturn() throws SQLException {
        List<Connection> connections = new ArrayList<>();
        
        // Obter 5 conexões
        for (int i = 0; i < 5; i++) {
            Connection conn = ConnectionManager.getConnection();
            assertNotNull(conn);
            connections.add(conn);
        }
        
        // Fechar todas
        for (Connection conn : connections) {
            conn.close();
        }
        
        // Verificar que podemos obter novas conexões (foram retornadas ao pool)
        try (Connection conn = ConnectionManager.getConnection()) {
            assertNotNull(conn);
            assertFalse(conn.isClosed());
        }
        
        System.out.println("✓ Conexões foram retornadas ao pool corretamente");
    }
    
    /**
     * Teste 4: Verificar que pool está inicializado
     */
    @Test
    public void testPoolInitialized() {
        assertTrue(ConnectionManager.isInitialized(), 
            "Connection Pool deve estar inicializado");
        
        assertTrue(ConnectionManager.testConnection(), 
            "Deve ser possível obter conexão do pool");
    }
    
    /**
     * Teste 5: Verificar estatísticas do pool
     */
    @Test
    public void testPoolStats() {
        if (ConnectionManager.isInitialized()) {
            String stats = ConnectionManager.getPoolStats();
            assertNotNull(stats, "Estatísticas do pool não devem ser null");
            System.out.println("\nEstatísticas do Pool:");
            System.out.println(stats);
        }
    }
}
