package com.inventario.sihcp.sync.service;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inventario.sihcp.sync.exception.SyncErrorType;
import com.inventario.sihcp.sync.exception.SyncException;
import com.inventario.sihcp.sync.model.RetryPolicy;

/**
 * Gerenciador de retry inteligente com backoff exponencial
 * Implementa retry automático para falhas temporárias
 */
public class RetryManager {
    
    private static final Logger logger = LoggerFactory.getLogger(RetryManager.class);
    
    /**
     * Executa operação com retry automático
     * @param operation Operação a executar
     * @param retryPolicy Política de retry
     * @return Resultado da operação
     * @throws Exception se todas as tentativas falharem
     */
    @SuppressWarnings("java:S2142") // Thread.sleep in loop is intentional for retry backoff
    public <T> T executeWithRetry(Callable<T> operation, RetryPolicy retryPolicy) throws Exception {
        int attempt = 0;
        Exception lastException = null;
        
        while (attempt < retryPolicy.getMaxRetries()) {
            try {
                logger.debug("Tentativa {} de {}", attempt + 1, retryPolicy.getMaxRetries());
                
                T result = operation.call();
                
                if (attempt > 0) {
                    logger.info("Operação bem-sucedida após {} tentativas", attempt + 1);
                }
                
                return result;
                
            } catch (Exception e) {
                lastException = e;
                attempt++;
                
                logger.warn("Tentativa {} falhou: {}", attempt, e.getMessage());
                
                // Verificar se erro é recuperável
                if (!isRetryableError(e, retryPolicy)) {
                    logger.error("Erro não recuperável, abortando retries: {}", e.getMessage());
                    throw e;
                }
                
                // Se não é a última tentativa, aguardar antes de tentar novamente
                if (attempt < retryPolicy.getMaxRetries()) {
                    long delay = calculateBackoffDelay(attempt, retryPolicy);
                    logger.info("Aguardando {} ms antes da próxima tentativa", delay);
                    
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new SyncException(
                            "Retry interrompido",
                            SyncErrorType.NETWORK_ERROR,
                            "executeWithRetry",
                            ie
                        );
                    }
                }
            }
        }
        
        // Todas as tentativas falharam
        logger.error("Todas as {} tentativas falharam", retryPolicy.getMaxRetries());
        throw new MaxRetriesExceededException(
            String.format("Operação falhou após %d tentativas", retryPolicy.getMaxRetries()),
            lastException
        );
    }
    
    /**
     * Executa operação com retry usando política padrão
     * @param operation Operação a executar
     * @return Resultado da operação
     * @throws Exception se todas as tentativas falharem
     */
    public <T> T executeWithRetry(Callable<T> operation) throws Exception {
        return executeWithRetry(operation, RetryPolicy.defaultPolicy());
    }
    
    /**
     * Calcula delay para próxima tentativa usando backoff exponencial
     * @param attemptNumber Número da tentativa atual (começando em 1)
     * @return Delay em milissegundos
     */
    public long calculateBackoffDelay(int attemptNumber) {
        return calculateBackoffDelay(attemptNumber, RetryPolicy.defaultPolicy());
    }
    
    /**
     * Calcula delay para próxima tentativa usando backoff exponencial
     * @param attemptNumber Número da tentativa atual (começando em 1)
     * @param retryPolicy Política de retry
     * @return Delay em milissegundos
     */
    public long calculateBackoffDelay(int attemptNumber, RetryPolicy retryPolicy) {
        if (attemptNumber <= 0) {
            return 0;
        }
        
        // Backoff exponencial: initialDelay * (multiplier ^ attemptNumber)
        double delay = retryPolicy.getInitialDelayMs() * 
            Math.pow(retryPolicy.getBackoffMultiplier(), attemptNumber - 1);
        
        return (long) delay;
    }
    
    /**
     * Determina se erro é recuperável e deve fazer retry
     * @param exception Exceção ocorrida
     * @return true se deve fazer retry
     */
    public boolean isRetryableError(Exception exception) {
        return isRetryableError(exception, RetryPolicy.defaultPolicy());
    }
    
    /**
     * Determina se erro é recuperável e deve fazer retry
     * @param exception Exceção ocorrida
     * @param retryPolicy Política de retry
     * @return true se deve fazer retry
     */
    public boolean isRetryableError(Exception exception, RetryPolicy retryPolicy) {
        if (exception == null) {
            return false;
        }
        
        // Verificar se exceção está na lista de retryable
        for (Class<? extends Exception> retryableClass : retryPolicy.getRetryableExceptions()) {
            if (retryableClass.isInstance(exception)) {
                logger.debug("Erro recuperável detectado: {}", exception.getClass().getSimpleName());
                return true;
            }
        }
        
        // Verificar tipos específicos de erro
        if (exception instanceof SocketTimeoutException) {
            logger.debug("Timeout de rede detectado - recuperável");
            return true;
        }
        
        if (exception instanceof ConnectException) {
            logger.debug("Erro de conexão detectado - recuperável");
            return true;
        }
        
        if (exception instanceof SQLException sqlEx) {
            // Alguns erros SQL são temporários
            String sqlState = sqlEx.getSQLState();
            
            // Deadlock, timeout, connection failure são recuperáveis
            if (sqlState != null && (
                sqlState.startsWith("40") ||  // Transaction rollback
                sqlState.startsWith("08") ||  // Connection exception
                sqlState.equals("57P03"))) {  // Cannot connect now
                logger.debug("Erro SQL temporário detectado - recuperável: {}", sqlState);
                return true;
            }
        }
        
        // Verificar se é SyncException com tipo recuperável
        if (exception instanceof SyncException syncEx) {
            SyncErrorType errorType = syncEx.getErrorType();
            
            // Erros de rede e transação são recuperáveis
            if (errorType == SyncErrorType.NETWORK_ERROR || 
                errorType == SyncErrorType.TRANSACTION_ERROR) {
                logger.debug("SyncException recuperável detectada: {}", errorType);
                return true;
            }
            
            // Erros de autenticação e validação não são recuperáveis
            if (errorType == SyncErrorType.AUTHENTICATION_ERROR || 
                errorType == SyncErrorType.VALIDATION_ERROR) {
                logger.debug("SyncException não recuperável detectada: {}", errorType);
                return false;
            }
        }
        
        logger.debug("Erro não recuperável: {}", exception.getClass().getSimpleName());
        return false;
    }
    
    /**
     * Executa operação com retry e callback de progresso
     * @param operation Operação a executar
     * @param retryPolicy Política de retry
     * @param onRetry Callback executado antes de cada retry
     * @return Resultado da operação
     * @throws Exception se todas as tentativas falharem
     */
    @SuppressWarnings("java:S2142") // Thread.sleep in loop is intentional for retry backoff
    public <T> T executeWithRetry(
            Callable<T> operation, 
            RetryPolicy retryPolicy,
            RetryCallback onRetry) throws Exception {
        
        int attempt = 0;
        Exception lastException = null;
        
        while (attempt < retryPolicy.getMaxRetries()) {
            try {
                if (attempt > 0 && onRetry != null) {
                    onRetry.onRetry(attempt, lastException);
                }
                
                return operation.call();
                
            } catch (Exception e) {
                lastException = e;
                attempt++;
                
                if (!isRetryableError(e, retryPolicy) || attempt >= retryPolicy.getMaxRetries()) {
                    throw e;
                }
                
                long delay = calculateBackoffDelay(attempt, retryPolicy);
                waitBeforeRetry(delay);
            }
        }
        
        throw new MaxRetriesExceededException(
            String.format("Operação falhou após %d tentativas", retryPolicy.getMaxRetries()),
            lastException
        );
    }
    
    /**
     * Aguarda antes de fazer retry (encapsula Thread.sleep)
     * @param delayMs Tempo de espera em milissegundos
     * @throws InterruptedException se a thread for interrompida
     */
    private void waitBeforeRetry(long delayMs) throws InterruptedException {
        Thread.sleep(delayMs);
    }
    
    /**
     * Interface para callback de retry
     */
    @FunctionalInterface
    public interface RetryCallback {
        void onRetry(int attemptNumber, Exception lastException);
    }
    
    /**
     * Exceção lançada quando todas as tentativas de retry falham
     */
    public static class MaxRetriesExceededException extends Exception {
        public MaxRetriesExceededException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
