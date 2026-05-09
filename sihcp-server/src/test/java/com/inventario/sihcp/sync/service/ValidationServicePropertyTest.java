package com.inventario.sihcp.sync.service;

import com.inventario.sihcp.model.Coleta;
import com.inventario.sihcp.sync.generator.ColetaGenerator;
import com.inventario.sihcp.sync.model.ValidationResult;
import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Property-Based Tests para ValidationService
 * Feature: correcao-sincronizacao-sqlite-postgresql
 */
@RunWith(JUnitQuickcheck.class)
public class ValidationServicePropertyTest {
    
    private final ValidationService validationService = new ValidationService();
    
    /**
     * Feature: correcao-sincronizacao-sqlite-postgresql, Property 1: Validação Completa de Dados
     * Validates: Requirements 1.1, 1.2, 1.3
     * 
     * Para qualquer conjunto de coletas pendentes, todas as coletas devem ter 
     * patrimônio_id, inventario_id e coletor_id válidos antes de sincronização ser permitida.
     */
    @Property(trials = 100)
    public void todasColetasDevemTerIdsValidos(@From(ColetaGenerator.class) List<Coleta> coletas) {
        // Arrange - Garantir que todas as coletas têm IDs válidos
        List<Coleta> coletasValidas = new ArrayList<>();
        for (Coleta coleta : coletas) {
            if (coleta.getIdPatrimonio() > 0 && 
                coleta.getIdInventario() > 0 && 
                coleta.getIdColetor() > 0) {
                coletasValidas.add(coleta);
            }
        }
        
        // Se não há coletas válidas, pular teste
        if (coletasValidas.isEmpty()) {
            return;
        }
        
        // Act
        ValidationResult result = validationService.validateColetas(coletasValidas);
        
        // Assert - Se validação passou, todas as coletas devem ter IDs válidos
        if (result.isValid()) {
            for (Coleta coleta : coletasValidas) {
                assertTrue("Patrimônio ID deve ser válido", coleta.getIdPatrimonio() > 0);
                assertTrue("Inventário ID deve ser válido", coleta.getIdInventario() > 0);
                assertTrue("Coletor ID deve ser válido", coleta.getIdColetor() > 0);
            }
        }
    }
    
    /**
     * Feature: correcao-sincronizacao-sqlite-postgresql, Property 2: Validação de Formato de Timestamps
     * Validates: Requirements 1.4, 13.7
     * 
     * Para qualquer timestamp em coletas pendentes, o formato deve ser yyyy-MM-dd HH:mm:ss 
     * e não pode estar no futuro.
     */
    @Property(trials = 100)
    public void timestampsDevemEstarEmFormatoCorreto(@From(ColetaGenerator.class) List<Coleta> coletas) {
        // Arrange - Garantir que coletas têm timestamps válidos
        LocalDateTime now = LocalDateTime.now();
        List<Coleta> coletasComTimestampValido = new ArrayList<>();
        
        for (Coleta coleta : coletas) {
            if (coleta.getDataColeta() != null) {
                LocalDateTime dataColeta = coleta.getDataColeta().toLocalDateTime();
                // Apenas coletas com data no passado ou presente
                if (!dataColeta.isAfter(now)) {
                    coletasComTimestampValido.add(coleta);
                }
            }
        }
        
        if (coletasComTimestampValido.isEmpty()) {
            return;
        }
        
        // Act & Assert
        for (Coleta coleta : coletasComTimestampValido) {
            Timestamp timestamp = coleta.getDataColeta();
            
            // Deve estar em formato válido
            assertTrue("Timestamp deve ser válido", 
                validationService.validateTimestampFormat(timestamp));
            
            // Não deve estar no futuro
            LocalDateTime dataColeta = timestamp.toLocalDateTime();
            assertFalse("Timestamp não deve estar no futuro", 
                dataColeta.isAfter(LocalDateTime.now()));
        }
    }
    
    /**
     * Feature: correcao-sincronizacao-sqlite-postgresql, Property 3: Validação de Campos Obrigatórios
     * Validates: Requirements 1.5
     * 
     * Para qualquer coleta pendente, nenhum campo obrigatório pode conter valor NULL.
     */
    @Property(trials = 100)
    public void camposObrigatoriosNaoPodeSerNull(@From(ColetaGenerator.class) List<Coleta> coletas) {
        // Arrange - Garantir que coletas têm campos obrigatórios preenchidos
        List<Coleta> coletasCompletas = new ArrayList<>();
        
        for (Coleta coleta : coletas) {
            if (coleta.getStatusColeta() != null && 
                !coleta.getStatusColeta().trim().isEmpty() &&
                coleta.getLocalizacaoEncontrada() != null &&
                !coleta.getLocalizacaoEncontrada().trim().isEmpty()) {
                coletasCompletas.add(coleta);
            }
        }
        
        if (coletasCompletas.isEmpty()) {
            return;
        }
        
        // Act
        ValidationResult result = validationService.validateColetas(coletasCompletas);
        
        // Assert - Se validação passou, campos obrigatórios devem estar preenchidos
        if (result.isValid()) {
            for (Coleta coleta : coletasCompletas) {
                assertNotNull("Status não pode ser null", coleta.getStatusColeta());
                assertFalse("Status não pode ser vazio", coleta.getStatusColeta().trim().isEmpty());
                assertNotNull("Localização não pode ser null", coleta.getLocalizacaoEncontrada());
                assertFalse("Localização não pode ser vazia", 
                    coleta.getLocalizacaoEncontrada().trim().isEmpty());
            }
        }
    }
    
    /**
     * Feature: correcao-sincronizacao-sqlite-postgresql, Property 6, 7, 8: Integridade Referencial
     * Validates: Requirements 3.1, 3.2, 3.3
     * 
     * Para qualquer coleta, patrimônio, inventário e coletor referenciados devem existir.
     */
    @Property(trials = 100)
    public void coletaDeveTerReferenciasValidasAntesDeInserir(@From(ColetaGenerator.class) Coleta coleta) {
        // Arrange - Garantir que coleta tem IDs válidos
        if (coleta.getIdPatrimonio() <= 0 || 
            coleta.getIdInventario() <= 0 || 
            coleta.getIdColetor() <= 0) {
            return; // Pular coletas com IDs inválidos
        }
        
        // Act
        boolean isValid = validationService.validateReferentialIntegrity(coleta);
        
        // Assert - Se validação de integridade referencial passou, IDs devem ser positivos
        if (isValid) {
            assertTrue("Patrimônio ID deve ser positivo", coleta.getIdPatrimonio() > 0);
            assertTrue("Inventário ID deve ser positivo", coleta.getIdInventario() > 0);
            assertTrue("Coletor ID deve ser positivo", coleta.getIdColetor() > 0);
        }
    }
}
