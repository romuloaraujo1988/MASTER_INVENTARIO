package com.inventario.sihcp.event;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Representa um evento que pode ser publicado para atualização da dashboard.
 * Esta classe é imutável para garantir thread-safety.
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public final class DashboardEvent {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    private final DashboardEventType type;
    private final long timestamp;
    private final String source;
    private final Map<String, Object> metadata;
    private final List<Integer> affectedEntityIds;
    
    /**
     * Construtor privado - use o Builder para criar instâncias.
     */
    private DashboardEvent(Builder builder) {
        this.type = Objects.requireNonNull(builder.type, "Event type cannot be null");
        this.timestamp = builder.timestamp > 0 ? builder.timestamp : System.currentTimeMillis();
        this.source = Objects.requireNonNull(builder.source, "Event source cannot be null");
        this.metadata = builder.metadata != null 
            ? Collections.unmodifiableMap(new HashMap<>(builder.metadata))
            : Collections.emptyMap();
        this.affectedEntityIds = builder.affectedEntityIds != null
            ? Collections.unmodifiableList(new ArrayList<>(builder.affectedEntityIds))
            : Collections.emptyList();
    }
    
    /**
     * Retorna o tipo do evento.
     * @return Tipo do evento
     */
    public DashboardEventType getType() {
        return type;
    }
    
    /**
     * Retorna o timestamp do evento em milissegundos.
     * @return Timestamp Unix em milissegundos
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Retorna o timestamp formatado para exibição.
     * @return Timestamp formatado (dd/MM/yyyy HH:mm:ss)
     */
    public String getFormattedTimestamp() {
        LocalDateTime dateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp), 
            ZoneId.systemDefault()
        );
        return dateTime.format(FORMATTER);
    }
    
    /**
     * Retorna a fonte/origem do evento.
     * @return Nome do serviço/componente que gerou o evento
     */
    public String getSource() {
        return source;
    }
    
    /**
     * Retorna os metadados do evento.
     * @return Mapa imutável de metadados
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    /**
     * Retorna um valor específico dos metadados.
     * @param key Chave do metadado
     * @return Valor ou null se não existir
     */
    public Object getMetadataValue(String key) {
        return metadata.get(key);
    }
    
    /**
     * Retorna um valor específico dos metadados com tipo.
     * @param key Chave do metadado
     * @param clazz Classe esperada do valor
     * @return Valor tipado ou null se não existir ou tipo incompatível
     */
    @SuppressWarnings("unchecked")
    public <T> T getMetadataValue(String key, Class<T> clazz) {
        Object value = metadata.get(key);
        if (value != null && clazz.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
    
    /**
     * Retorna os IDs das entidades afetadas pelo evento.
     * @return Lista imutável de IDs
     */
    public List<Integer> getAffectedEntityIds() {
        return affectedEntityIds;
    }
    
    /**
     * Verifica se o evento afeta uma entidade específica.
     * @param entityId ID da entidade
     * @return true se a entidade está na lista de afetados
     */
    public boolean affectsEntity(int entityId) {
        return affectedEntityIds.contains(entityId);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DashboardEvent that = (DashboardEvent) o;
        return timestamp == that.timestamp &&
               type == that.type &&
               Objects.equals(source, that.source) &&
               Objects.equals(metadata, that.metadata) &&
               Objects.equals(affectedEntityIds, that.affectedEntityIds);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(type, timestamp, source, metadata, affectedEntityIds);
    }
    
    @Override
    public String toString() {
        return String.format("DashboardEvent{type=%s, timestamp=%s, source='%s', entities=%d}",
            type.getDisplayName(),
            getFormattedTimestamp(),
            source,
            affectedEntityIds.size()
        );
    }
    
    /**
     * Retorna uma representação resumida para exibição em tooltips.
     * @return String resumida do evento
     */
    public String toSummary() {
        return String.format("[%s] %s - %s", 
            getFormattedTimestamp(),
            type.getDisplayName(),
            source
        );
    }
    
    /**
     * Cria um novo Builder para construir um DashboardEvent.
     * @param type Tipo do evento (obrigatório)
     * @return Builder configurado com o tipo
     */
    public static Builder builder(DashboardEventType type) {
        return new Builder(type);
    }
    
    /**
     * Cria um evento simples de coleta sincronizada.
     * @param coletaId ID da coleta
     * @param patrimonioId ID do patrimônio
     * @param inventarioId ID do inventário
     * @return Evento configurado
     */
    public static DashboardEvent coletaSincronizada(int coletaId, int patrimonioId, int inventarioId) {
        return builder(DashboardEventType.COLETA_SINCRONIZADA)
            .source("MobileColetaService")
            .addMetadata("coletaId", coletaId)
            .addMetadata("patrimonioId", patrimonioId)
            .addMetadata("inventarioId", inventarioId)
            .addAffectedEntityId(patrimonioId)
            .build();
    }
    
    /**
     * Cria um evento de batch de coletas sincronizadas.
     * @param coletaIds IDs das coletas
     * @param inventarioId ID do inventário
     * @param batchSize Tamanho do batch
     * @return Evento configurado
     */
    public static DashboardEvent coletasBatchSincronizadas(List<Integer> coletaIds, int inventarioId, int batchSize) {
        return builder(DashboardEventType.COLETA_SINCRONIZADA)
            .source("MobileColetaService")
            .addMetadata("inventarioId", inventarioId)
            .addMetadata("batchSize", batchSize)
            .addMetadata("coletaIds", coletaIds)
            .addAffectedEntityIds(coletaIds)
            .build();
    }
    
    /**
     * Builder para construção fluente de DashboardEvent.
     */
    public static class Builder {
        private final DashboardEventType type;
        private long timestamp;
        private String source;
        private Map<String, Object> metadata;
        private List<Integer> affectedEntityIds;
        
        private Builder(DashboardEventType type) {
            this.type = type;
            this.timestamp = System.currentTimeMillis();
            this.metadata = new HashMap<>();
            this.affectedEntityIds = new ArrayList<>();
        }
        
        /**
         * Define o timestamp do evento.
         * @param timestamp Timestamp em milissegundos
         * @return Builder
         */
        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        /**
         * Define a fonte do evento.
         * @param source Nome do serviço/componente
         * @return Builder
         */
        public Builder source(String source) {
            this.source = source;
            return this;
        }
        
        /**
         * Adiciona um metadado ao evento.
         * @param key Chave
         * @param value Valor
         * @return Builder
         */
        public Builder addMetadata(String key, Object value) {
            if (this.metadata == null) {
                this.metadata = new HashMap<>();
            }
            this.metadata.put(key, value);
            return this;
        }
        
        /**
         * Define todos os metadados do evento.
         * @param metadata Mapa de metadados
         * @return Builder
         */
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
            return this;
        }
        
        /**
         * Adiciona um ID de entidade afetada.
         * @param entityId ID da entidade
         * @return Builder
         */
        public Builder addAffectedEntityId(int entityId) {
            if (this.affectedEntityIds == null) {
                this.affectedEntityIds = new ArrayList<>();
            }
            this.affectedEntityIds.add(entityId);
            return this;
        }
        
        /**
         * Adiciona múltiplos IDs de entidades afetadas.
         * @param entityIds Lista de IDs
         * @return Builder
         */
        public Builder addAffectedEntityIds(List<Integer> entityIds) {
            if (this.affectedEntityIds == null) {
                this.affectedEntityIds = new ArrayList<>();
            }
            if (entityIds != null) {
                this.affectedEntityIds.addAll(entityIds);
            }
            return this;
        }
        
        /**
         * Define os IDs das entidades afetadas.
         * @param entityIds Lista de IDs
         * @return Builder
         */
        public Builder affectedEntityIds(List<Integer> entityIds) {
            this.affectedEntityIds = entityIds != null ? new ArrayList<>(entityIds) : new ArrayList<>();
            return this;
        }
        
        /**
         * Constrói o DashboardEvent.
         * @return Evento construído
         * @throws NullPointerException se type ou source forem null
         */
        public DashboardEvent build() {
            return new DashboardEvent(this);
        }
    }
}
