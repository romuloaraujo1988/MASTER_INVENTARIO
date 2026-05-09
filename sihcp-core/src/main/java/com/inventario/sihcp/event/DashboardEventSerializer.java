package com.inventario.sihcp.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Serializador para DashboardEvent.
 * Suporta serialização para JSON e deserialização de volta para objeto.
 * Implementa round-trip: deserialize(serialize(event)) == event
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class DashboardEventSerializer {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardEventSerializer.class);
    
    private final ObjectMapper objectMapper;
    
    /**
     * Construtor padrão com ObjectMapper configurado.
     */
    public DashboardEventSerializer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    /**
     * Construtor com ObjectMapper customizado.
     * @param objectMapper ObjectMapper a ser usado
     */
    public DashboardEventSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }
    
    /**
     * Serializa um DashboardEvent para JSON.
     * 
     * @param event Evento a ser serializado
     * @return String JSON representando o evento
     * @throws IllegalArgumentException se o evento for null ou inválido
     */
    public String serialize(DashboardEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        
        try {
            Map<String, Object> eventMap = new LinkedHashMap<>();
            eventMap.put("type", event.getType().name());
            eventMap.put("timestamp", event.getTimestamp());
            eventMap.put("source", event.getSource());
            eventMap.put("metadata", event.getMetadata());
            eventMap.put("affectedEntityIds", event.getAffectedEntityIds());
            
            return objectMapper.writeValueAsString(eventMap);
        } catch (JsonProcessingException e) {
            logger.error("Erro ao serializar evento: {}", event, e);
            throw new IllegalArgumentException("Failed to serialize event", e);
        }
    }
    
    /**
     * Deserializa uma string JSON para DashboardEvent.
     * 
     * @param json String JSON a ser deserializada
     * @return DashboardEvent reconstruído
     * @throws IllegalArgumentException se o JSON for null, vazio ou inválido
     */
    public DashboardEvent deserialize(String json) {
        if (json == null || json.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON cannot be null or empty");
        }
        
        try {
            Map<String, Object> eventMap = objectMapper.readValue(json, 
                new TypeReference<Map<String, Object>>() {});
            
            // Extrair tipo
            String typeStr = (String) eventMap.get("type");
            if (typeStr == null) {
                throw new IllegalArgumentException("Event type is required");
            }
            DashboardEventType type = DashboardEventType.valueOf(typeStr);
            
            // Extrair timestamp
            Object timestampObj = eventMap.get("timestamp");
            long timestamp = timestampObj instanceof Number 
                ? ((Number) timestampObj).longValue() 
                : System.currentTimeMillis();
            
            // Extrair source
            String source = (String) eventMap.get("source");
            if (source == null) {
                throw new IllegalArgumentException("Event source is required");
            }
            
            // Extrair metadata
            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = (Map<String, Object>) eventMap.get("metadata");
            
            // Extrair affectedEntityIds
            @SuppressWarnings("unchecked")
            List<Object> entityIdsRaw = (List<Object>) eventMap.get("affectedEntityIds");
            List<Integer> affectedEntityIds = new ArrayList<>();
            if (entityIdsRaw != null) {
                for (Object id : entityIdsRaw) {
                    if (id instanceof Number) {
                        affectedEntityIds.add(((Number) id).intValue());
                    }
                }
            }
            
            // Construir evento
            return DashboardEvent.builder(type)
                .timestamp(timestamp)
                .source(source)
                .metadata(metadata)
                .affectedEntityIds(affectedEntityIds)
                .build();
                
        } catch (JsonProcessingException e) {
            logger.error("Erro ao deserializar JSON: {}", json, e);
            throw new IllegalArgumentException("Failed to deserialize JSON", e);
        } catch (IllegalArgumentException e) {
            logger.error("JSON inválido: {}", json, e);
            throw e;
        }
    }
    
    /**
     * Serializa um evento para formato compacto (uma linha).
     * 
     * @param event Evento a ser serializado
     * @return String JSON compacta
     */
    public String serializeCompact(DashboardEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        
        try {
            Map<String, Object> eventMap = new LinkedHashMap<>();
            eventMap.put("type", event.getType().name());
            eventMap.put("timestamp", event.getTimestamp());
            eventMap.put("source", event.getSource());
            eventMap.put("metadata", event.getMetadata());
            eventMap.put("affectedEntityIds", event.getAffectedEntityIds());
            
            ObjectMapper compactMapper = new ObjectMapper();
            return compactMapper.writeValueAsString(eventMap);
        } catch (JsonProcessingException e) {
            logger.error("Erro ao serializar evento compacto: {}", event, e);
            throw new IllegalArgumentException("Failed to serialize event", e);
        }
    }
    
    /**
     * Verifica se uma string JSON é um evento válido.
     * 
     * @param json String JSON a ser validada
     * @return true se o JSON representa um evento válido
     */
    public boolean isValidEventJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        
        try {
            deserialize(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Serializa uma lista de eventos para JSON.
     * 
     * @param events Lista de eventos
     * @return String JSON representando a lista
     */
    public String serializeList(List<DashboardEvent> events) {
        if (events == null) {
            throw new IllegalArgumentException("Events list cannot be null");
        }
        
        try {
            List<Map<String, Object>> eventMaps = new ArrayList<>();
            for (DashboardEvent event : events) {
                Map<String, Object> eventMap = new LinkedHashMap<>();
                eventMap.put("type", event.getType().name());
                eventMap.put("timestamp", event.getTimestamp());
                eventMap.put("source", event.getSource());
                eventMap.put("metadata", event.getMetadata());
                eventMap.put("affectedEntityIds", event.getAffectedEntityIds());
                eventMaps.add(eventMap);
            }
            
            return objectMapper.writeValueAsString(eventMaps);
        } catch (JsonProcessingException e) {
            logger.error("Erro ao serializar lista de eventos", e);
            throw new IllegalArgumentException("Failed to serialize events list", e);
        }
    }
    
    /**
     * Deserializa uma lista de eventos de JSON.
     * 
     * @param json String JSON representando lista de eventos
     * @return Lista de DashboardEvent
     */
    public List<DashboardEvent> deserializeList(String json) {
        if (json == null || json.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON cannot be null or empty");
        }
        
        try {
            List<Map<String, Object>> eventMaps = objectMapper.readValue(json,
                new TypeReference<List<Map<String, Object>>>() {});
            
            List<DashboardEvent> events = new ArrayList<>();
            for (Map<String, Object> eventMap : eventMaps) {
                String typeStr = (String) eventMap.get("type");
                DashboardEventType type = DashboardEventType.valueOf(typeStr);
                
                Object timestampObj = eventMap.get("timestamp");
                long timestamp = timestampObj instanceof Number 
                    ? ((Number) timestampObj).longValue() 
                    : System.currentTimeMillis();
                
                String source = (String) eventMap.get("source");
                
                @SuppressWarnings("unchecked")
                Map<String, Object> metadata = (Map<String, Object>) eventMap.get("metadata");
                
                @SuppressWarnings("unchecked")
                List<Object> entityIdsRaw = (List<Object>) eventMap.get("affectedEntityIds");
                List<Integer> affectedEntityIds = new ArrayList<>();
                if (entityIdsRaw != null) {
                    for (Object id : entityIdsRaw) {
                        if (id instanceof Number) {
                            affectedEntityIds.add(((Number) id).intValue());
                        }
                    }
                }
                
                events.add(DashboardEvent.builder(type)
                    .timestamp(timestamp)
                    .source(source)
                    .metadata(metadata)
                    .affectedEntityIds(affectedEntityIds)
                    .build());
            }
            
            return events;
        } catch (JsonProcessingException e) {
            logger.error("Erro ao deserializar lista de eventos: {}", json, e);
            throw new IllegalArgumentException("Failed to deserialize events list", e);
        }
    }
}
