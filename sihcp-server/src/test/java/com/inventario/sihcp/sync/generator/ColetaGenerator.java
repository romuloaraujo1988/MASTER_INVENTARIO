package com.inventario.sihcp.sync.generator;

import com.inventario.sihcp.model.Coleta;
import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Gerador customizado de Coletas para Property-Based Testing
 */
public class ColetaGenerator extends Generator<Coleta> {
    
    public ColetaGenerator() {
        super(Coleta.class);
    }
    
    @Override
    public Coleta generate(SourceOfRandomness random, GenerationStatus status) {
        Coleta coleta = new Coleta();
        
        // Gerar IDs aleatórios (alguns podem ser inválidos para testar validação)
        coleta.setId(random.nextInt(1, 10000));
        coleta.setIdPatrimonio(random.nextInt(-5, 1000)); // Pode ser negativo para testar
        coleta.setIdInventario(random.nextInt(-5, 100));  // Pode ser negativo para testar
        coleta.setIdColetor(random.nextInt(-5, 50));      // Pode ser negativo para testar
        
        // Gerar timestamp aleatório (passado)
        coleta.setDataColeta(generateRandomTimestamp(random));
        
        // Gerar status aleatório
        String[] statuses = {
            Coleta.STATUS_COLETADO,
            Coleta.STATUS_NAO_ENCONTRADO,
            Coleta.STATUS_DANIFICADO,
            Coleta.STATUS_DIVERGENCIA
        };
        coleta.setStatusColeta(statuses[random.nextInt(0, statuses.length - 1)]);
        
        // Gerar observações aleatórias
        coleta.setObservacaoColeta(generateRandomString(random, 0, 200));
        
        // Gerar localização
        coleta.setLocalizacaoEncontrada(generateRandomString(random, 5, 50));
        
        // Gerar estado
        String[] estados = {
            Coleta.ESTADO_OTIMO,
            Coleta.ESTADO_BOM,
            Coleta.ESTADO_REGULAR,
            Coleta.ESTADO_RUIM,
            Coleta.ESTADO_PESSIMO
        };
        coleta.setEstadoEncontrado(estados[random.nextInt(0, estados.length - 1)]);
        
        // Marcar como não sincronizada
        coleta.setDivergencia(random.nextBoolean());
        
        return coleta;
    }
    
    /**
     * Gera timestamp aleatório no passado
     */
    private Timestamp generateRandomTimestamp(SourceOfRandomness random) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime past = now.minusDays(random.nextInt(1, 365));
        return Timestamp.valueOf(past);
    }
    
    /**
     * Gera string aleatória
     */
    private String generateRandomString(SourceOfRandomness random, int minLength, int maxLength) {
        int length = random.nextInt(minLength, maxLength);
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            char c = (char) random.nextInt('a', 'z');
            sb.append(c);
        }
        
        return sb.toString();
    }
}
