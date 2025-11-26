package com.inventario.sync.service;

import com.inventario.model.Coleta;
import com.inventario.sync.model.ValidationResult;
import com.inventario.sync.model.ValidationResult.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Serviço de validação de dados antes de sincronização
 * Garante integridade e consistência dos dados
 */
public class ValidationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidationService.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Valida lista de coletas antes de sincronizar
     * @param coletas Lista de coletas a validar
     * @return Resultado da validação com lista de erros
     */
    public ValidationResult validateColetas(List<Coleta> coletas) {
        logger.info("Iniciando validação de {} coletas", coletas.size());
        
        ValidationResult result = new ValidationResult(true, coletas.size());
        
        for (Coleta coleta : coletas) {
            validateColeta(coleta, result);
        }
        
        if (result.isValid()) {
            logger.info("Validação concluída com sucesso: {} coletas válidas", coletas.size());
        } else {
            logger.warn("Validação falhou: {} erros encontrados", result.getErrors().size());
        }
        
        return result;
    }
    
    /**
     * Valida uma coleta individual
     */
    private void validateColeta(Coleta coleta, ValidationResult result) {
        // Validar patrimônio_id
        if (coleta.getIdPatrimonio() <= 0 && !coleta.isSemEtiqueta()) {
            result.addError(new ValidationError(
                "idPatrimonio",
                "ID do patrimônio é obrigatório e deve ser maior que zero",
                coleta.getIdPatrimonio()
            ));
        }
        
        // Validar inventario_id
        if (coleta.getIdInventario() <= 0) {
            result.addError(new ValidationError(
                "idInventario",
                "ID do inventário é obrigatório e deve ser maior que zero",
                coleta.getIdInventario()
            ));
        }
        
        // Validar coletor_id
        if (coleta.getIdColetor() <= 0) {
            result.addError(new ValidationError(
                "idColetor",
                "ID do coletor é obrigatório e deve ser maior que zero",
                coleta.getIdColetor()
            ));
        }
        
        // Validar timestamp
        if (coleta.getDataColeta() == null) {
            result.addError(new ValidationError(
                "dataColeta",
                "Data da coleta é obrigatória",
                null
            ));
        } else if (!validateTimestampFormat(coleta.getDataColeta())) {
            result.addError(new ValidationError(
                "dataColeta",
                "Data da coleta está em formato inválido",
                coleta.getDataColeta()
            ));
        }
        
        // Validar campos obrigatórios
        List<String> missingFields = validateRequiredFields(coleta);
        for (String field : missingFields) {
            result.addError(new ValidationError(
                field,
                "Campo obrigatório não pode ser nulo ou vazio",
                null
            ));
        }
    }
    
    /**
     * Valida integridade referencial de uma coleta
     * Verifica se as FKs referenciam registros existentes
     * @param coleta Coleta a validar
     * @return true se todas as FKs são válidas
     */
    public boolean validateReferentialIntegrity(Coleta coleta) {
        // Esta validação será implementada quando tivermos acesso aos managers
        // Por enquanto, apenas valida que os IDs são positivos
        boolean patrimonioValid = coleta.isSemEtiqueta() || coleta.getIdPatrimonio() > 0;
        boolean inventarioValid = coleta.getIdInventario() > 0;
        boolean coletorValid = coleta.getIdColetor() > 0;
        
        return patrimonioValid && inventarioValid && coletorValid;
    }
    
    /**
     * Valida formato de timestamp
     * @param timestamp Timestamp a validar
     * @return true se formato é válido e não está no futuro
     */
    public boolean validateTimestampFormat(Timestamp timestamp) {
        if (timestamp == null) {
            return false;
        }
        
        try {
            // Converter para LocalDateTime
            LocalDateTime dateTime = timestamp.toLocalDateTime();
            
            // Verificar se não está no futuro
            if (dateTime.isAfter(LocalDateTime.now())) {
                logger.warn("Timestamp está no futuro: {}", dateTime);
                return false;
            }
            
            // Tentar formatar no formato esperado
            String formatted = dateTime.format(TIMESTAMP_FORMATTER);
            
            // Tentar fazer parse de volta
            LocalDateTime.parse(formatted, TIMESTAMP_FORMATTER);
            
            return true;
        } catch (DateTimeParseException e) {
            logger.error("Erro ao validar formato de timestamp: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Valida formato de string de timestamp
     * @param timestampStr String de timestamp a validar
     * @return true se formato é válido
     */
    public boolean validateTimestampFormat(String timestampStr) {
        if (timestampStr == null || timestampStr.trim().isEmpty()) {
            return false;
        }
        
        try {
            LocalDateTime dateTime = LocalDateTime.parse(timestampStr, TIMESTAMP_FORMATTER);
            
            // Verificar se não está no futuro
            if (dateTime.isAfter(LocalDateTime.now())) {
                logger.warn("Timestamp está no futuro: {}", dateTime);
                return false;
            }
            
            return true;
        } catch (DateTimeParseException e) {
            logger.error("Erro ao validar formato de timestamp string: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Valida campos obrigatórios de uma entidade
     * @param entity Entidade a validar
     * @return Lista de campos faltantes
     */
    public List<String> validateRequiredFields(Object entity) {
        java.util.List<String> missingFields = new java.util.ArrayList<>();
        
        if (entity instanceof Coleta) {
            Coleta coleta = (Coleta) entity;
            
            // Status é obrigatório
            if (coleta.getStatusColeta() == null || coleta.getStatusColeta().trim().isEmpty()) {
                missingFields.add("statusColeta");
            }
            
            // Para itens sem etiqueta, validar campos específicos
            if (coleta.isSemEtiqueta()) {
                if (coleta.getDescricaoItemSemEtiqueta() == null || 
                    coleta.getDescricaoItemSemEtiqueta().trim().isEmpty()) {
                    missingFields.add("descricaoItemSemEtiqueta");
                }
                
                if (coleta.getCategoriaItemSemEtiqueta() == null || 
                    coleta.getCategoriaItemSemEtiqueta().trim().isEmpty()) {
                    missingFields.add("categoriaItemSemEtiqueta");
                }
            }
            
            // Localização encontrada é obrigatória
            if (coleta.getLocalizacaoEncontrada() == null || 
                coleta.getLocalizacaoEncontrada().trim().isEmpty()) {
                missingFields.add("localizacaoEncontrada");
            }
        }
        
        return missingFields;
    }
    
    /**
     * Valida se um ID é válido (maior que zero)
     * @param id ID a validar
     * @param fieldName Nome do campo para mensagem de erro
     * @return true se ID é válido
     */
    public boolean validateId(int id, String fieldName) {
        if (id <= 0) {
            logger.warn("ID inválido para campo {}: {}", fieldName, id);
            return false;
        }
        return true;
    }
}
