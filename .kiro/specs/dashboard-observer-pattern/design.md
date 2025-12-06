# Design Document - Dashboard Observer Pattern

## Overview

Este documento descreve a arquitetura e design para implementar o padrão Observer na dashboard da aplicação desktop do SIHCP. A solução substitui o mecanismo atual de polling (Timer de 30 segundos) por um sistema de eventos reativo, onde a dashboard é notificada automaticamente quando mudanças relevantes ocorrem no sistema.

A arquitetura segue o padrão Event-Driven com um EventBus centralizado que gerencia a publicação e distribuição de eventos entre os serviços (publishers) e a dashboard (subscriber).

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              Event Publishers                                │
├─────────────────┬─────────────────┬─────────────────┬───────────────────────┤
│ MobileColeta    │ Patrimonio      │ Inventario      │ Sala/Responsavel      │
│ Service         │ Service         │ Service         │ Service               │
└────────┬────────┴────────┬────────┴────────┬────────┴───────────┬───────────┘
         │                 │                 │                    │
         │ publish()       │ publish()       │ publish()          │ publish()
         ▼                 ▼                 ▼                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              DashboardEventBus                               │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │  - observers: Map<EventType, List<DashboardObserver>>               │    │
│  │  - eventQueue: BlockingQueue<DashboardEvent>                        │    │
│  │  - executorService: ExecutorService (async delivery)                │    │
│  │  - debouncer: EventDebouncer (rate limiting)                        │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
└────────────────────────────────────┬────────────────────────────────────────┘
                                     │
                                     │ notify() (async, debounced)
                                     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           DashboardColetaFrame                               │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │  implements DashboardObserver                                        │    │
│  │  - eventLog: CircularBuffer<DashboardEvent> (last 10)               │    │
│  │  - isPaused: boolean                                                 │    │
│  │  - pendingEvents: Queue<DashboardEvent> (when paused/hidden)        │    │
│  │  - updateIndicator: JLabel (visual feedback)                        │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Components and Interfaces

### 1. DashboardEvent (Domain Model)

```java
public class DashboardEvent {
    private final DashboardEventType type;
    private final long timestamp;
    private final String source;
    private final Map<String, Object> metadata;
    private final List<Integer> affectedEntityIds;
    
    // Constructor, getters, equals, hashCode, toString
    // Pretty-printer for serialization (round-trip support)
}

public enum DashboardEventType {
    COLETA_SINCRONIZADA,
    PATRIMONIO_ATUALIZADO,
    INVENTARIO_ALTERADO,
    SALA_ATUALIZADA,
    RESPONSAVEL_ATUALIZADO
}
```

### 2. DashboardObserver (Interface)

```java
public interface DashboardObserver {
    /**
     * Called when a relevant event occurs.
     * @param event The event that occurred
     */
    void onDashboardEvent(DashboardEvent event);
    
    /**
     * Returns the event types this observer is interested in.
     * @return Set of event types to subscribe to, or empty for all types
     */
    default Set<DashboardEventType> getSubscribedEventTypes() {
        return EnumSet.allOf(DashboardEventType.class);
    }
}
```

### 3. DashboardEventBus (Singleton)

```java
public class DashboardEventBus {
    private static volatile DashboardEventBus instance;
    
    private final Map<DashboardEventType, List<DashboardObserver>> observers;
    private final ExecutorService executorService;
    private final EventDebouncer debouncer;
    private final Logger logger;
    
    // Singleton getInstance()
    
    public void register(DashboardObserver observer);
    public void unregister(DashboardObserver observer);
    public void publish(DashboardEvent event);
    
    // Internal: async notification with error handling
    private void notifyObservers(DashboardEvent event);
}
```

### 4. EventDebouncer (Rate Limiting)

```java
public class EventDebouncer {
    private final int maxEventsPerSecond;
    private final int burstThreshold;
    private final long debounceWindowMs;
    
    private final Queue<DashboardEvent> pendingEvents;
    private final AtomicLong lastUpdateTime;
    private final AtomicInteger eventCountInWindow;
    
    public boolean shouldDebounce(DashboardEvent event);
    public List<DashboardEvent> getConsolidatedEvents();
    public void reset();
}
```

### 5. DashboardEventSerializer (Pretty-Printer)

```java
public class DashboardEventSerializer {
    /**
     * Serializes a DashboardEvent to a string representation.
     */
    public String serialize(DashboardEvent event);
    
    /**
     * Deserializes a string back to a DashboardEvent.
     */
    public DashboardEvent deserialize(String serialized);
}
```

### 6. CircularEventBuffer (Event Log)

```java
public class CircularEventBuffer {
    private final int capacity;
    private final DashboardEvent[] buffer;
    private int head;
    private int size;
    
    public void add(DashboardEvent event);
    public List<DashboardEvent> getAll();
    public int size();
}
```

## Data Models

### DashboardEvent

| Field | Type | Description |
|-------|------|-------------|
| type | DashboardEventType | Type of event (enum) |
| timestamp | long | Unix timestamp when event occurred |
| source | String | Service/component that generated the event |
| metadata | Map<String, Object> | Additional event-specific data |
| affectedEntityIds | List<Integer> | IDs of entities affected by this event |

### Event Metadata Examples

```java
// COLETA_SINCRONIZADA
{
    "coletaId": 123,
    "patrimonioId": 456,
    "inventarioId": 1,
    "coletorNome": "João Silva",
    "batchSize": 5  // if batch sync
}

// PATRIMONIO_ATUALIZADO
{
    "patrimonioId": 456,
    "campo": "estado",
    "valorAnterior": "BOM",
    "valorNovo": "REGULAR"
}

// INVENTARIO_ALTERADO
{
    "inventarioId": 1,
    "status": "EM_ANDAMENTO",
    "percentualConcluido": 45.5
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

Based on the prework analysis, the following correctness properties have been identified:

### Property 1: Event Delivery Timeliness
*For any* collection synchronization event published to the EventBus, the registered dashboard observer shall receive the event within 2 seconds of publication.
**Validates: Requirements 1.1**

### Property 2: Batch Event Consolidation
*For any* batch of N events (N > 1) published within a 100ms window, the dashboard shall receive at most one consolidated update notification.
**Validates: Requirements 1.2**

### Property 3: Event Queuing When Hidden
*For any* sequence of events published while the dashboard is not visible, all events shall be queued and delivered when the dashboard becomes visible again, preserving order.
**Validates: Requirements 1.4**

### Property 4: Observer Registration Round-Trip
*For any* observer registered with the EventBus, that observer shall receive events of its subscribed types, and after unregistering, shall receive no further events.
**Validates: Requirements 2.1**

### Property 5: Multiple Observer Support
*For any* set of N observers registered for the same event type, when an event of that type is published, all N observers shall receive the event exactly once.
**Validates: Requirements 2.2**

### Property 6: Async Event Delivery
*For any* event published to the EventBus, the publish() method shall return within 10ms regardless of observer processing time.
**Validates: Requirements 2.3**

### Property 7: Observer Exception Isolation
*For any* set of observers where one throws an exception during event handling, all other observers shall still receive the event successfully.
**Validates: Requirements 2.4**

### Property 8: Singleton Consistency
*For any* number of calls to DashboardEventBus.getInstance(), all calls shall return the same instance (reference equality).
**Validates: Requirements 2.5**

### Property 9: Event Log Bounded Size
*For any* sequence of N events (N > 10) added to the event log, the log shall contain exactly the last 10 events in order.
**Validates: Requirements 4.1**

### Property 10: Event Log Completeness
*For any* event added to the log, the log entry shall contain non-null values for event type, timestamp, and source.
**Validates: Requirements 4.3**

### Property 11: Event Type Filtering
*For any* observer registered for a specific subset of event types, that observer shall receive only events of those types and no others.
**Validates: Requirements 5.3**

### Property 12: Event Serialization Round-Trip
*For any* valid DashboardEvent, serializing and then deserializing shall produce an event that is equal to the original.
**Validates: Requirements 5.4**

### Property 13: Pause State Consistency
*For any* dashboard in paused state, no automatic event-driven updates shall occur until resumed.
**Validates: Requirements 6.1**

### Property 14: Resume Triggers Refresh
*For any* dashboard that is resumed from paused state, a data refresh shall be triggered within 100ms of resuming.
**Validates: Requirements 6.3**

### Property 15: Auto-Resume After Timeout
*For any* dashboard paused for 5 minutes, the dashboard shall automatically resume and trigger a data refresh.
**Validates: Requirements 6.4**

### Property 16: Service Event Publishing
*For any* service method that modifies data (MobileColetaService.sincronizarColeta, PatrimonioService.atualizar, InventarioService.alterarStatus), a corresponding event shall be published to the EventBus.
**Validates: Requirements 7.1, 7.2, 7.3**

### Property 17: Event Publishing Resilience
*For any* service operation, if event publishing fails (throws exception), the original operation shall still complete successfully.
**Validates: Requirements 7.4**

### Property 18: Debounce Rate Limiting
*For any* burst of more than 5 events within 1 second, the dashboard shall perform at most 2 UI updates during that second.
**Validates: Requirements 8.1, 8.2**

## Error Handling

### EventBus Error Handling

1. **Observer Exception**: Log error with stack trace, continue notifying other observers
2. **Queue Full**: Log warning, drop oldest events if necessary
3. **Executor Shutdown**: Gracefully reject new events, log warning

### Dashboard Error Handling

1. **Update Failure**: Display error indicator, offer retry button
2. **Event Deserialization Error**: Log error, skip malformed event
3. **Database Connection Error**: Fall back to cached data, show stale indicator

### Service Integration Error Handling

1. **Event Publishing Failure**: Log error, do not affect original transaction
2. **EventBus Unavailable**: Log warning, continue without event publishing

## Testing Strategy

### Dual Testing Approach

This implementation uses both unit tests and property-based tests:

- **Unit tests**: Verify specific examples, edge cases, and integration points
- **Property-based tests**: Verify universal properties that should hold across all inputs

### Property-Based Testing Framework

**Framework**: jqwik (Java property-based testing library)

**Configuration**: Each property test runs minimum 100 iterations.

### Test Categories

#### 1. EventBus Tests
- Property tests for registration/unregistration (Property 4)
- Property tests for multiple observers (Property 5)
- Property tests for async delivery (Property 6)
- Property tests for exception isolation (Property 7)
- Property tests for singleton (Property 8)
- Property tests for event filtering (Property 11)

#### 2. Event Serialization Tests
- Property tests for round-trip serialization (Property 12)
- Unit tests for edge cases (null fields, special characters)

#### 3. CircularEventBuffer Tests
- Property tests for bounded size (Property 9)
- Property tests for completeness (Property 10)
- Unit tests for empty buffer, single element

#### 4. EventDebouncer Tests
- Property tests for rate limiting (Property 18)
- Property tests for batch consolidation (Property 2)
- Unit tests for timing edge cases

#### 5. Dashboard Integration Tests
- Property tests for pause/resume (Properties 13, 14, 15)
- Property tests for event queuing (Property 3)
- Unit tests for UI state transitions

#### 6. Service Integration Tests
- Property tests for event publishing (Property 16)
- Property tests for resilience (Property 17)
- Unit tests for specific service methods

### Test Annotations

Each property-based test must be annotated with:
```java
/**
 * Feature: dashboard-observer-pattern, Property {number}: {property_text}
 * Validates: Requirements {X.Y}
 */
```
