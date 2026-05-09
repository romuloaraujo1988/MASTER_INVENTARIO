package com.inventario.sihcp.mobile.server.config;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.inventario.sihcp.mobile.server.dto.ApiResponse;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

/**
 * Handler global de exceções para a API Mobile.
 * 
 * PRINCÍPIO: O servidor NUNCA pode quebrar.
 * - Cada handler tem try-catch interno
 * - Sempre retorna resposta válida
 * - Gera código de erro único para rastreamento
 * - Logs detalhados para debugging
 */
@RestControllerAdvice(basePackages = "com.inventario.sihcp.mobile.server")
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    /**
     * Gera código de erro único para rastreamento
     * Formato: ERR-YYYYMMDD-HHMMSS-XXXX
     */
    private String gerarCodigoErro() {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String uuid = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "ERR-" + timestamp + "-" + uuid;
    }

    /**
     * Extrai stack trace como string para logging
     */
    private String getStackTraceAsString(Throwable ex) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            return sw.toString();
        } catch (Exception e) {
            return "Não foi possível extrair stack trace";
        }
    }

    /**
     * Cria resposta de erro segura - NUNCA lança exceção
     */
    private ResponseEntity<ApiResponse<Object>> criarRespostaErroSegura(
            String mensagem, String codigoErro, HttpStatus status) {
        try {
            ApiResponse<Object> response = new ApiResponse<>();
            response.setSuccess(false);
            response.setMessage(mensagem);
            response.setErrorCode(codigoErro);
            return ResponseEntity.status(status).body(response);
        } catch (Exception e) {
            // Fallback absoluto - resposta mínima
            logger.error("CRÍTICO: Falha ao criar resposta de erro: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== EXCEÇÕES DE AUTENTICAÇÃO ====================

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiResponse<Object>> handleExpiredJwtException(ExpiredJwtException ex) {
        String codigoErro = gerarCodigoErro();
        try {
            logger.warn("[{}] Token JWT expirado: {}", codigoErro, ex.getMessage());
            return criarRespostaErroSegura(
                    "Sessão expirada. Faça login novamente. [" + codigoErro + "]",
                    codigoErro,
                    HttpStatus.UNAUTHORIZED
            );
        } catch (Exception e) {
            logger.error("[{}] Erro ao processar ExpiredJwtException: {}", codigoErro, e.getMessage());
            return criarRespostaErroSegura("Erro de autenticação [" + codigoErro + "]", codigoErro, HttpStatus.UNAUTHORIZED);
        }
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<ApiResponse<Object>> handleMalformedJwtException(MalformedJwtException ex) {
        String codigoErro = gerarCodigoErro();
        try {
            logger.warn("[{}] Token JWT malformado: {}", codigoErro, ex.getMessage());
            return criarRespostaErroSegura(
                    "Token inválido. Faça login novamente. [" + codigoErro + "]",
                    codigoErro,
                    HttpStatus.UNAUTHORIZED
            );
        } catch (Exception e) {
            logger.error("[{}] Erro ao processar MalformedJwtException: {}", codigoErro, e.getMessage());
            return criarRespostaErroSegura("Erro de autenticação [" + codigoErro + "]", codigoErro, HttpStatus.UNAUTHORIZED);
        }
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<ApiResponse<Object>> handleSignatureException(SignatureException ex) {
        String codigoErro = gerarCodigoErro();
        try {
            logger.warn("[{}] Assinatura JWT inválida: {}", codigoErro, ex.getMessage());
            return criarRespostaErroSegura(
                    "Token com assinatura inválida. Faça login novamente. [" + codigoErro + "]",
                    codigoErro,
                    HttpStatus.UNAUTHORIZED
            );
        } catch (Exception e) {
            logger.error("[{}] Erro ao processar SignatureException: {}", codigoErro, e.getMessage());
            return criarRespostaErroSegura("Erro de autenticação [" + codigoErro + "]", codigoErro, HttpStatus.UNAUTHORIZED);
        }
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(BadCredentialsException ex) {
        String codigoErro = gerarCodigoErro();
        try {
            logger.warn("[{}] Credenciais inválidas", codigoErro);
            return criarRespostaErroSegura(
                    "Usuário ou senha inválidos.",
                    codigoErro,
                    HttpStatus.UNAUTHORIZED
            );
        } catch (Exception e) {
            logger.error("[{}] Erro ao processar BadCredentialsException: {}", codigoErro, e.getMessage());
            return criarRespostaErroSegura("Erro de autenticação [" + codigoErro + "]", codigoErro, HttpStatus.UNAUTHORIZED);
        }
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(AuthenticationException ex) {
        String codigoErro = gerarCodigoErro();
        try {
            logger.warn("[{}] Erro de autenticação: {}", codigoErro, ex.getMessage());
            return criarRespostaErroSegura(
                    "Falha na autenticação. [" + codigoErro + "]",
                    codigoErro,
                    HttpStatus.UNAUTHORIZED
            );
        } catch (Exception e) {
            logger.error("[{}] Erro ao processar AuthenticationException: {}", codigoErro, e.getMessage());
            return criarRespostaErroSegura("Erro de autenticação [" + codigoErro + "]", codigoErro, HttpStatus.UNAUTHORIZED);
        }
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException ex) {
        String codigoErro = gerarCodigoErro();
        try {
            logger.warn("[{}] Acesso negado: {}", codigoErro, ex.getMessage());
            return criarRespostaErroSegura(
                    "Você não tem permissão para acessar este recurso. [" + codigoErro + "]",
                    codigoErro,
                    HttpStatus.FORBIDDEN
            );
        } catch (Exception e) {
            logger.error("[{}] Erro ao processar AccessDeniedException: {}", codigoErro, e.getMessage());
            return criarRespostaErroSegura("Acesso negado [" + codigoErro + "]", codigoErro, HttpStatus.FORBIDDEN);
        }
    }

    // ==================== EXCEÇÕES DE VALIDAÇÃO ====================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        String codigoErro = gerarCodigoErro();
        try {
            Map<String, String> errors = new HashMap<>();
            ex.getBindingResult().getAllErrors().forEach(error -> {
                try {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    errors.put(fieldName, errorMessage != null ? errorMessage : "Campo inválido");
                } catch (Exception e) {
                    errors.put("campo", "Erro de validação");
                }
            });

            logger.warn("[{}] Erro de validação: {}", codigoErro, errors);

            ApiResponse<Object> response = new ApiResponse<>();
            response.setSuccess(false);
            response.setMessage("Dados inválidos. Verifique os campos. [" + codigoErro + "]");
            response.setErrorCode(codigoErro);
            response.setData(errors);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("[{}] Erro ao processar MethodArgumentNotValidException: {}", codigoErro, e.getMessage());
            return criarRespostaErroSegura("Dados inválidos [" + codigoErro + "]", codigoErro, HttpStatus.BAD_REQUEST);
        }
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolationException(ConstraintViolationException ex) {
        String codigoErro = gerarCodigoErro();
        try {
            Map<String, String> errors = new HashMap<>();
            for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
                try {
                    errors.put(
                            violation.getPropertyPath().toString(),
                            violation.getMessage()
                    );
                } catch (Exception e) {
                    errors.put("campo", "Violação de constraint");
                }
            }

            logger.warn("[{}] Violação de constraint: {}", codigoErro, errors);

            ApiResponse<Object> response = new ApiResponse<>();
            response.setSuccess(false);
            response.setMessage("Dados inválidos. [" + codigoErro + "]");
            response.setErrorCode(codigoErro);
            response.setData(errors);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("[{}] Erro ao processar ConstraintViolationException: {}", codigoErro, e.getMessage());
            return criarRespostaErroSegura("Dados inválidos [" + codigoErro + "]", codigoErro, HttpStatus.BAD_REQUEST);
        }
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingParameterException(MissingServletRequestParameterException ex) {
        String codigoErro = gerarCodigoErro();
        try {
            String paramName = ex.getParameterName() != null ? ex.getParameterName() : "desconhecido";
            logger.warn("[{}] Parâmetro ausente: {}", codigoErro, paramName);
            return criarRespostaErroSegura(
                    "Parâmetro obrigatório ausente: " + paramName + " [" + codigoErro + "]",
                    codigoErro,
                    HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            logger.error("[{}] Erro ao processar MissingServletRequestParameterException: {}", codigoErro, e.getMessage());
            return criarRespostaErroSegura("Parâmetro ausente [" + codigoErro + "]", codigoErro, HttpStatus.BAD_REQUEST);
        }
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String codigoErro = gerarCodigoErro();
        try {
            String paramName = ex.getName() != null ? ex.getName() : "desconhecido";
            Object value = ex.getValue();
            String valueStr = value != null ? value.toString() : "null";
            
            logger.warn("[{}] Tipo de argumento inválido: {} = {}", codigoErro, paramName, valueStr);
            return criarRespostaErroSegura(
                    "Parâmetro '" + paramName + "' com valor inválido. [" + codigoErro + "]",
                    codigoErro,
                    HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            logger.error("[{}] Erro ao processar MethodArgumentTypeMismatchException: {}", codigoErro, e.getMessage());
            return criarRespostaErroSegura("Parâmetro inválido [" + codigoErro + "]", codigoErro, HttpStatus.BAD_REQUEST);
        }
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        String codigoErro = gerarCodigoErro();
        try {
            logger.warn("[{}] JSON inválido: {}", codigoErro, ex.getMessage());
            return criarRespostaErroSegura(
                    "Formato JSON inválido no corpo da requisição. [" + codigoErro + "]",
                    codigoErro,
                    HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            logger.error("[{}] Erro ao processar HttpMessageNotReadableException: {}", codigoErro, e.getMessage());
            return criarRespostaErroSegura("Requisição inválida [" + codigoErro + "]", codigoErro, HttpStatus.BAD_REQUEST);
        }
    }

    // ==================== EXCEÇÕES DE BANCO DE DADOS ====================

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleEntityNotFoundException(EntityNotFoundException ex) {
        String codigoErro = gerarCodigoErro();
        try {
            String mensagem = ex.getMessage() != null ? ex.getMessage() : "Recurso não encontrado";
            logger.warn("[{}] Entidade não encontrada: {}", codigoErro, mensagem);
            return criarRespostaErroSegura(
                    mensagem + " [" + codigoErro + "]",
                    codigoErro,
                    HttpStatus.NOT_FOUND
            );
        } catch (Exception e) {
            logger.error("[{}] Erro ao processar EntityNotFoundException: {}", codigoErro, e.getMessage());
            return criarRespostaErroSegura("Recurso não encontrado [" + codigoErro + "]", codigoErro, HttpStatus.NOT_FOUND);
        }
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String codigoErro = gerarCodigoErro();
        try {
            logger.error("[{}] Violação de integridade: {}", codigoErro, ex.getMessage());

            String message = "Operação não permitida devido a restrições de dados.";
            String exMessage = ex.getMessage();
            
            if (exMessage != null) {
                String lowerMessage = exMessage.toLowerCase();
                if (lowerMessage.contains("duplicate") || lowerMessage.contains("unique") || lowerMessage.contains("duplicat")) {
                    message = "Registro duplicado. Este dado já existe no sistema.";
                } else if (lowerMessage.contains("foreign key") || lowerMessage.contains("fk_") || lowerMessage.contains("referential")) {
                    message = "Não é possível realizar esta operação. Existem registros relacionados.";
                } else if (lowerMessage.contains("not null") || lowerMessage.contains("cannot be null")) {
                    message = "Campo obrigatório não preenchido.";
                }
            }

            return criarRespostaErroSegura(
                    message + " [" + codigoErro + "]",
                    codigoErro,
                    HttpStatus.CONFLICT
            );
        } catch (Exception e) {
            logger.error("[{}] Erro ao processar DataIntegrityViolationException: {}", codigoErro, e.getMessage());
            return criarRespostaErroSegura("Erro de integridade de dados [" + codigoErro + "]", codigoErro, HttpStatus.CONFLICT);
        }
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataAccessException(DataAccessException ex) {
        String codigoErro = gerarCodigoErro();
        try {
            logger.error("[{}] Erro de acesso a dados: {}\n{}", codigoErro, ex.getMessage(), getStackTraceAsString(ex));
            return criarRespostaErroSegura(
                    "Erro ao acessar o banco de dados. Tente novamente. [" + codigoErro + "]",
                    codigoErro,
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        } catch (Exception e) {
            logger.error("[{}] Erro ao processar DataAccessException: {}", codigoErro, e.getMessage());
            return criarRespostaErroSegura("Erro de banco de dados [" + codigoErro + "]", codigoErro, HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    // ==================== EXCEÇÕES HTTP ====================

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        String codigoErro = gerarCodigoErro();
        try {
            String method = ex.getMethod() != null ? ex.getMethod() : "desconhecido";
            logger.warn("[{}] Método não suportado: {}", codigoErro, method);
            return criarRespostaErroSegura(
                    "Método " + method + " não suportado para este endpoint. [" + codigoErro + "]",
                    codigoErro,
                    HttpStatus.METHOD_NOT_ALLOWED
            );
        } catch (Exception e) {
            logger.error("[{}] Erro ao processar HttpRequestMethodNotSupportedException: {}", codigoErro, e.getMessage());
            return criarRespostaErroSegura("Método não suportado [" + codigoErro + "]", codigoErro, HttpStatus.METHOD_NOT_ALLOWED);
        }
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
        String codigoErro = gerarCodigoErro();
        try {
            logger.warn("[{}] Media type não suportado: {}", codigoErro, ex.getContentType());
            return criarRespostaErroSegura(
                    "Content-Type não suportado. Use application/json. [" + codigoErro + "]",
                    codigoErro,
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE
            );
        } catch (Exception e) {
            logger.error("[{}] Erro ao processar HttpMediaTypeNotSupportedException: {}", codigoErro, e.getMessage());
            return criarRespostaErroSegura("Content-Type não suportado [" + codigoErro + "]", codigoErro, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        String codigoErro = gerarCodigoErro();
        try {
            String url = ex.getRequestURL() != null ? ex.getRequestURL() : "desconhecida";
            logger.warn("[{}] Endpoint não encontrado: {} {}", codigoErro, ex.getHttpMethod(), url);
            return criarRespostaErroSegura(
                    "Endpoint não encontrado. [" + codigoErro + "]",
                    codigoErro,
                    HttpStatus.NOT_FOUND
            );
        } catch (Exception e) {
            logger.error("[{}] Erro ao processar NoHandlerFoundException: {}", codigoErro, e.getMessage());
            return criarRespostaErroSegura("Endpoint não encontrado [" + codigoErro + "]", codigoErro, HttpStatus.NOT_FOUND);
        }
    }

    // ==================== EXCEÇÕES DE NEGÓCIO ====================

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        String codigoErro = gerarCodigoErro();
        try {
            String mensagem = ex.getMessage() != null ? ex.getMessage() : "Argumento inválido";
            logger.warn("[{}] Argumento ilegal: {}", codigoErro, mensagem);
            return criarRespostaErroSegura(
                    mensagem + " [" + codigoErro + "]",
                    codigoErro,
                    HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            logger.error("[{}] Erro ao processar IllegalArgumentException: {}", codigoErro, e.getMessage());
            return criarRespostaErroSegura("Argumento inválido [" + codigoErro + "]", codigoErro, HttpStatus.BAD_REQUEST);
        }
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalStateException(IllegalStateException ex) {
        String codigoErro = gerarCodigoErro();
        try {
            String mensagem = ex.getMessage() != null ? ex.getMessage() : "Operação não permitida no estado atual";
            logger.warn("[{}] Estado ilegal: {}", codigoErro, mensagem);
            return criarRespostaErroSegura(
                    mensagem + " [" + codigoErro + "]",
                    codigoErro,
                    HttpStatus.CONFLICT
            );
        } catch (Exception e) {
            logger.error("[{}] Erro ao processar IllegalStateException: {}", codigoErro, e.getMessage());
            return criarRespostaErroSegura("Operação não permitida [" + codigoErro + "]", codigoErro, HttpStatus.CONFLICT);
        }
    }

    // ==================== EXCEÇÕES DE RUNTIME ====================

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<Object>> handleNullPointerException(NullPointerException ex) {
        String codigoErro = gerarCodigoErro();
        try {
            logger.error("[{}] NullPointerException: {}\n{}", codigoErro, ex.getMessage(), getStackTraceAsString(ex));
            return criarRespostaErroSegura(
                    "Erro interno do servidor. Reporte o código: " + codigoErro,
                    codigoErro,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        } catch (Exception e) {
            logger.error("[{}] Erro ao processar NullPointerException: {}", codigoErro, e.getMessage());
            return criarRespostaErroSegura("Erro interno [" + codigoErro + "]", codigoErro, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException ex) {
        String codigoErro = gerarCodigoErro();
        try {
            logger.error("[{}] RuntimeException: {}\n{}", codigoErro, ex.getMessage(), getStackTraceAsString(ex));
            return criarRespostaErroSegura(
                    "Erro inesperado. Reporte o código: " + codigoErro,
                    codigoErro,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        } catch (Exception e) {
            logger.error("[{}] Erro ao processar RuntimeException: {}", codigoErro, e.getMessage());
            return criarRespostaErroSegura("Erro interno [" + codigoErro + "]", codigoErro, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== EXCEÇÕES DE CANCELAMENTO DE CONEXÃO ====================
    
    /**
     * Trata cancelamento de requisição pelo cliente (ClientAbortException).
     * Ocorre quando o cliente fecha a conexão antes do servidor terminar de processar.
     * 
     * IMPORTANTE: Não é um erro real - é comportamento normal quando:
     * - Usuário cancela uma operação longa
     * - Usuário navega para outra tela
     * - Timeout do cliente
     * - Perda de conexão de rede
     * 
     * Retorna null para não tentar enviar resposta (conexão já fechada).
     */
    @ExceptionHandler(ClientAbortException.class)
    public ResponseEntity<ApiResponse<Object>> handleClientAbortException(ClientAbortException ex) {
        String codigoErro = gerarCodigoErro();
        try {
            // Log em nível INFO (não é erro, é comportamento esperado)
            logger.info("[{}] Cliente cancelou a requisição (ClientAbortException): {}", 
                       codigoErro, ex.getMessage());
            
            // Não tenta enviar resposta - conexão já foi fechada pelo cliente
            return null;
        } catch (Exception e) {
            // Silenciosamente ignora - não há como responder ao cliente
            logger.debug("[{}] Ignorando erro ao processar ClientAbortException: {}", codigoErro, e.getMessage());
            return null;
        }
    }
    
    /**
     * Trata IOException que pode indicar cancelamento de conexão.
     * Verifica se é "Broken pipe" ou "Connection reset" para tratar adequadamente.
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiResponse<Object>> handleIOException(IOException ex) {
        String codigoErro = gerarCodigoErro();
        try {
            String message = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
            
            // Verificar se é cancelamento de conexão pelo cliente
            boolean isClientDisconnect = message.contains("broken pipe") ||
                                         message.contains("connection reset") ||
                                         message.contains("connection abort") ||
                                         message.contains("socket closed") ||
                                         message.contains("stream closed") ||
                                         message.contains("an established connection was aborted");
            
            if (isClientDisconnect) {
                // Log em nível INFO - não é erro, cliente desconectou
                logger.info("[{}] Cliente desconectou durante processamento (IOException): {}", 
                           codigoErro, ex.getMessage());
                
                // Não tenta enviar resposta - conexão já foi fechada
                return null;
            }
            
            // Outros IOExceptions são erros reais
            logger.error("[{}] IOException: {}\n{}", codigoErro, ex.getMessage(), getStackTraceAsString(ex));
            return criarRespostaErroSegura(
                    "Erro de I/O. Tente novamente. [" + codigoErro + "]",
                    codigoErro,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        } catch (Exception e) {
            // Se falhar ao processar, provavelmente conexão já fechou
            logger.debug("[{}] Erro ao processar IOException: {}", codigoErro, e.getMessage());
            return null;
        }
    }
    
    /**
     * Trata timeout de requisições assíncronas.
     * Ocorre quando uma operação assíncrona excede o tempo limite configurado.
     */
    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public ResponseEntity<ApiResponse<Object>> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException ex) {
        String codigoErro = gerarCodigoErro();
        try {
            logger.warn("[{}] Timeout de requisição assíncrona", codigoErro);
            return criarRespostaErroSegura(
                    "A operação excedeu o tempo limite. Tente novamente. [" + codigoErro + "]",
                    codigoErro,
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        } catch (Exception e) {
            logger.debug("[{}] Erro ao processar AsyncRequestTimeoutException: {}", codigoErro, e.getMessage());
            return null;
        }
    }
    
    /**
     * Trata InterruptedException que pode ocorrer quando uma thread é interrompida.
     * Comum em operações longas que são canceladas.
     */
    @ExceptionHandler(InterruptedException.class)
    public ResponseEntity<ApiResponse<Object>> handleInterruptedException(InterruptedException ex) {
        String codigoErro = gerarCodigoErro();
        try {
            logger.info("[{}] Operação interrompida: {}", codigoErro, ex.getMessage());
            
            // Restaurar flag de interrupção
            Thread.currentThread().interrupt();
            
            return criarRespostaErroSegura(
                    "Operação cancelada. [" + codigoErro + "]",
                    codigoErro,
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        } catch (Exception e) {
            logger.debug("[{}] Erro ao processar InterruptedException: {}", codigoErro, e.getMessage());
            Thread.currentThread().interrupt();
            return null;
        }
    }

    // ==================== EXCEÇÃO GENÉRICA (FALLBACK FINAL) ====================

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ApiResponse<Object>> handleThrowable(Throwable ex) {
        String codigoErro = gerarCodigoErro();
        try {
            // Verificar se a causa raiz é um cancelamento de conexão
            if (isCausedByClientDisconnect(ex)) {
                logger.info("[{}] Cliente desconectou (detectado via causa raiz): {}", 
                           codigoErro, getRootCauseMessage(ex));
                return null; // Não tenta enviar resposta
            }
            
            logger.error("[{}] ERRO CRÍTICO (Throwable): {}\n{}", codigoErro, ex.getMessage(), getStackTraceAsString(ex));
            return criarRespostaErroSegura(
                    "Erro crítico do servidor. Reporte o código: " + codigoErro,
                    codigoErro,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        } catch (Exception e) {
            // Fallback absoluto - se tudo falhar, ainda retorna algo
            logger.error("FALHA CRÍTICA NO HANDLER DE EXCEÇÕES: {}", e.getMessage());
            try {
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Erro crítico. Código: " + codigoErro, codigoErro));
            } catch (Exception fatal) {
                // Último recurso - resposta vazia mas válida
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }
    
    /**
     * Verifica se a exceção foi causada por desconexão do cliente.
     * Percorre toda a cadeia de causas para encontrar ClientAbortException ou IOException relacionada.
     */
    private boolean isCausedByClientDisconnect(Throwable ex) {
        Throwable current = ex;
        int maxDepth = 10; // Evitar loop infinito
        int depth = 0;
        
        while (current != null && depth < maxDepth) {
            // Verificar se é ClientAbortException
            if (current instanceof ClientAbortException) {
                return true;
            }
            
            // Verificar se é IOException com mensagem de desconexão
            if (current instanceof IOException) {
                String message = current.getMessage();
                if (message != null) {
                    String lowerMessage = message.toLowerCase();
                    if (lowerMessage.contains("broken pipe") ||
                        lowerMessage.contains("connection reset") ||
                        lowerMessage.contains("connection abort") ||
                        lowerMessage.contains("socket closed") ||
                        lowerMessage.contains("stream closed") ||
                        lowerMessage.contains("an established connection was aborted")) {
                        return true;
                    }
                }
            }
            
            current = current.getCause();
            depth++;
        }
        
        return false;
    }
    
    /**
     * Obtém a mensagem da causa raiz da exceção.
     */
    private String getRootCauseMessage(Throwable ex) {
        Throwable current = ex;
        Throwable rootCause = ex;
        int maxDepth = 10;
        int depth = 0;
        
        while (current != null && depth < maxDepth) {
            rootCause = current;
            current = current.getCause();
            depth++;
        }
        
        return rootCause.getMessage() != null ? rootCause.getMessage() : rootCause.getClass().getSimpleName();
    }
}
