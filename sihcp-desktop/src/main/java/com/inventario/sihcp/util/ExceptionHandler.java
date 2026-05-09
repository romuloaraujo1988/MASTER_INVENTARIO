package com.inventario.sihcp.util;

import java.awt.Component;
import java.sql.SQLException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tratamento centralizado de exceções
 * Elimina try-catch duplicados e inconsistentes
 */
public class ExceptionHandler {
    
    private static final Logger LOGGER = Logger.getLogger(ExceptionHandler.class.getName());
    
    /**
     * Trata exceção genérica
     */
    public static void handle(Component parent, Exception e, String context) {
        LOGGER.log(Level.SEVERE, "Erro em " + context, e);
        
        String message = buildUserMessage(e, context);
        DialogUtils.showError(parent, message);
    }
    
    /**
     * Trata exceção de banco de dados
     */
    public static void handleDatabase(Component parent, SQLException e, String operation) {
        LOGGER.log(Level.SEVERE, "Erro de banco de dados em " + operation, e);
        
        String message = "Erro ao " + operation + ":\n\n";
        
        // Mensagens específicas por código de erro
        String sqlState = e.getSQLState();
        if (sqlState != null) {
            switch (sqlState) {
                case "23505": // Unique violation
                    message += "Registro duplicado. Este item já existe no sistema.";
                    break;
                case "23503": // Foreign key violation
                    message += "Não é possível realizar esta operação.\n" +
                              "Existem registros relacionados que impedem a ação.";
                    break;
                case "23502": // Not null violation
                    message += "Campo obrigatório não preenchido.";
                    break;
                case "08001": // Connection error
                case "08006":
                    message += "Erro de conexão com o banco de dados.\n" +
                              "Verifique se o servidor está ativo.";
                    break;
                default:
                    message += e.getMessage();
            }
        } else {
            message += e.getMessage();
        }
        
        DialogUtils.showError(parent, message);
    }
    
    /**
     * Trata exceção de IO
     */
    public static void handleIO(Component parent, IOException e, String operation) {
        LOGGER.log(Level.SEVERE, "Erro de IO em " + operation, e);
        
        String message = "Erro ao " + operation + ":\n\n";
        
        if (e.getMessage().contains("Access is denied")) {
            message += "Acesso negado. Verifique as permissões do arquivo.";
        } else if (e.getMessage().contains("No such file")) {
            message += "Arquivo não encontrado.";
        } else if (e.getMessage().contains("The process cannot access")) {
            message += "Arquivo em uso por outro processo.";
        } else {
            message += e.getMessage();
        }
        
        DialogUtils.showError(parent, message);
    }
    
    /**
     * Trata exceção de validação
     */
    public static void handleValidation(Component parent, String message) {
        LOGGER.log(Level.WARNING, "Erro de validação: " + message);
        DialogUtils.showWarning(parent, message);
    }
    
    /**
     * Trata exceção de negócio
     */
    public static void handleBusiness(Component parent, String message) {
        LOGGER.log(Level.WARNING, "Erro de negócio: " + message);
        DialogUtils.showWarning(parent, message);
    }
    
    /**
     * Executa operação com tratamento de erro
     */
    public static void executeWithErrorHandling(Component parent, String operation, ThrowingRunnable action) {
        try {
            action.run();
        } catch (SQLException e) {
            handleDatabase(parent, e, operation);
        } catch (IOException e) {
            handleIO(parent, e, operation);
        } catch (Exception e) {
            handle(parent, e, operation);
        }
    }
    
    /**
     * Executa operação com tratamento de erro e retorno
     */
    public static <T> T executeWithErrorHandling(Component parent, String operation, ThrowingSupplier<T> action, T defaultValue) {
        try {
            return action.get();
        } catch (SQLException e) {
            handleDatabase(parent, e, operation);
            return defaultValue;
        } catch (IOException e) {
            handleIO(parent, e, operation);
            return defaultValue;
        } catch (Exception e) {
            handle(parent, e, operation);
            return defaultValue;
        }
    }
    
    /**
     * Constrói mensagem amigável para o usuário
     */
    private static String buildUserMessage(Exception e, String context) {
        String message = "Erro ao " + context + ":\n\n";
        
        // Mensagens específicas por tipo de exceção
        if (e instanceof NullPointerException) {
            message += "Dados inválidos ou não encontrados.";
        } else if (e instanceof IllegalArgumentException) {
            message += "Parâmetros inválidos: " + e.getMessage();
        } else if (e instanceof IllegalStateException) {
            message += "Operação não permitida no estado atual.";
        } else {
            message += e.getMessage();
        }
        
        return message;
    }
    
    /**
     * Interface funcional para operações que podem lançar exceções
     */
    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }
    
    /**
     * Interface funcional para operações com retorno que podem lançar exceções
     */
    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
