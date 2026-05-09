package com.inventario.sihcp.event;

import java.util.EnumSet;
import java.util.Set;

/**
 * Interface para observers que desejam receber notificações de eventos da dashboard.
 * Implementações desta interface podem se registrar no DashboardEventBus para
 * receber eventos específicos ou todos os eventos.
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public interface DashboardObserver {
    
    /**
     * Chamado quando um evento relevante ocorre.
     * Este método é chamado de forma assíncrona pelo EventBus.
     * 
     * Implementações devem:
     * - Ser thread-safe
     * - Não bloquear por longos períodos
     * - Tratar exceções internamente quando possível
     * 
     * @param event O evento que ocorreu
     */
    void onDashboardEvent(DashboardEvent event);
    
    /**
     * Retorna os tipos de eventos que este observer deseja receber.
     * Por padrão, retorna todos os tipos de eventos.
     * 
     * Sobrescreva este método para filtrar eventos específicos:
     * <pre>
     * {@code
     * @Override
     * public Set<DashboardEventType> getSubscribedEventTypes() {
     *     return EnumSet.of(
     *         DashboardEventType.COLETA_SINCRONIZADA,
     *         DashboardEventType.INVENTARIO_ALTERADO
     *     );
     * }
     * }
     * </pre>
     * 
     * @return Set de tipos de eventos para subscrever, ou todos os tipos se vazio/null
     */
    default Set<DashboardEventType> getSubscribedEventTypes() {
        return EnumSet.allOf(DashboardEventType.class);
    }
    
    /**
     * Chamado quando múltiplos eventos são consolidados (batch).
     * Por padrão, chama onDashboardEvent para cada evento.
     * 
     * Sobrescreva para otimizar o processamento de batches:
     * <pre>
     * {@code
     * @Override
     * public void onDashboardEventBatch(java.util.List<DashboardEvent> events) {
     *     // Processar todos os eventos de uma vez
     *     refreshDashboard();
     * }
     * }
     * </pre>
     * 
     * @param events Lista de eventos consolidados
     */
    default void onDashboardEventBatch(java.util.List<DashboardEvent> events) {
        if (events != null) {
            for (DashboardEvent event : events) {
                onDashboardEvent(event);
            }
        }
    }
    
    /**
     * Retorna um nome identificador para este observer.
     * Usado para logging e debugging.
     * 
     * @return Nome do observer (padrão: nome da classe)
     */
    default String getObserverName() {
        return getClass().getSimpleName();
    }
    
    /**
     * Verifica se este observer está interessado em um tipo específico de evento.
     * 
     * @param eventType Tipo de evento a verificar
     * @return true se o observer quer receber este tipo de evento
     */
    default boolean isInterestedIn(DashboardEventType eventType) {
        Set<DashboardEventType> subscribed = getSubscribedEventTypes();
        return subscribed == null || subscribed.isEmpty() || subscribed.contains(eventType);
    }
}
