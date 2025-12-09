package com.inventario.util;

import java.util.regex.Pattern;

/**
 * Utilitário centralizado para validações
 * Elimina duplicação de código de validação em múltiplos frames
 */
public class ValidationUtils {
    
    // Padrões de validação
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}$"
    );
    
    // CPF_PATTERN removido - validação usa algoritmo de dígitos verificadores
    
    private static final Pattern NUMERO_PATRIMONIO_PATTERN = Pattern.compile(
        "^[0-9]{1,20}$"
    );
    
    /**
     * Valida se string não é nula ou vazia
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    /**
     * Valida se string é nula ou vazia
     */
    public static boolean isEmpty(String value) {
        return !isNotEmpty(value);
    }
    
    /**
     * Valida campo obrigatório
     */
    public static ValidationResult validateRequired(String value, String fieldName) {
        if (isEmpty(value)) {
            return ValidationResult.error(fieldName + " é obrigatório");
        }
        return ValidationResult.success();
    }
    
    /**
     * Valida tamanho mínimo
     */
    public static ValidationResult validateMinLength(String value, int minLength, String fieldName) {
        if (value != null && value.length() < minLength) {
            return ValidationResult.error(
                fieldName + " deve ter no mínimo " + minLength + " caracteres"
            );
        }
        return ValidationResult.success();
    }
    
    /**
     * Valida tamanho máximo
     */
    public static ValidationResult validateMaxLength(String value, int maxLength, String fieldName) {
        if (value != null && value.length() > maxLength) {
            return ValidationResult.error(
                fieldName + " deve ter no máximo " + maxLength + " caracteres"
            );
        }
        return ValidationResult.success();
    }
    
    /**
     * Valida email
     */
    public static ValidationResult validateEmail(String email) {
        if (isEmpty(email)) {
            return ValidationResult.error("Email é obrigatório");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return ValidationResult.error("Email inválido");
        }
        return ValidationResult.success();
    }
    
    /**
     * Valida telefone
     */
    public static ValidationResult validatePhone(String phone) {
        if (isEmpty(phone)) {
            return ValidationResult.error("Telefone é obrigatório");
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            return ValidationResult.error("Telefone inválido. Use formato: (XX) XXXXX-XXXX");
        }
        return ValidationResult.success();
    }
    
    /**
     * Valida CPF
     */
    public static ValidationResult validateCPF(String cpf) {
        if (isEmpty(cpf)) {
            return ValidationResult.error("CPF é obrigatório");
        }
        
        // Remove formatação
        String cpfNumeros = cpf.replaceAll("[^0-9]", "");
        
        if (cpfNumeros.length() != 11) {
            return ValidationResult.error("CPF deve ter 11 dígitos");
        }
        
        // Verifica se todos os dígitos são iguais
        if (cpfNumeros.matches("(\\d)\\1{10}")) {
            return ValidationResult.error("CPF inválido");
        }
        
        // Validação dos dígitos verificadores
        if (!validarDigitosCPF(cpfNumeros)) {
            return ValidationResult.error("CPF inválido");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Valida número de patrimônio
     */
    public static ValidationResult validateNumeroPatrimonio(String numero) {
        if (isEmpty(numero)) {
            return ValidationResult.error("Número do patrimônio é obrigatório");
        }
        if (!NUMERO_PATRIMONIO_PATTERN.matcher(numero).matches()) {
            return ValidationResult.error("Número do patrimônio deve conter apenas dígitos");
        }
        return ValidationResult.success();
    }
    
    /**
     * Valida número inteiro
     */
    public static ValidationResult validateInteger(String value, String fieldName) {
        if (isEmpty(value)) {
            return ValidationResult.error(fieldName + " é obrigatório");
        }
        if (!value.matches("-?\\d+")) {
            return ValidationResult.error(fieldName + " deve ser um número inteiro");
        }
        return ValidationResult.success();
    }
    
    /**
     * Valida número decimal
     */
    public static ValidationResult validateDecimal(String value, String fieldName) {
        if (isEmpty(value)) {
            return ValidationResult.error(fieldName + " é obrigatório");
        }
        // Verifica se é um número decimal válido usando regex
        if (!value.matches("-?\\d+(\\.\\d+)?")) {
            return ValidationResult.error(fieldName + " deve ser um número decimal");
        }
        return ValidationResult.success();
    }
    
    /**
     * Valida range numérico
     */
    public static ValidationResult validateRange(int value, int min, int max, String fieldName) {
        if (value < min || value > max) {
            return ValidationResult.error(
                fieldName + " deve estar entre " + min + " e " + max
            );
        }
        return ValidationResult.success();
    }
    
    /**
     * Valida senha
     */
    public static ValidationResult validatePassword(String password) {
        if (isEmpty(password)) {
            return ValidationResult.error("Senha é obrigatória");
        }
        if (password.length() < 6) {
            return ValidationResult.error("Senha deve ter no mínimo 6 caracteres");
        }
        return ValidationResult.success();
    }
    
    /**
     * Valida confirmação de senha
     */
    public static ValidationResult validatePasswordConfirmation(String password, String confirmation) {
        if (!password.equals(confirmation)) {
            return ValidationResult.error("As senhas não coincidem");
        }
        return ValidationResult.success();
    }
    
    /**
     * Valida dígitos verificadores do CPF
     */
    private static boolean validarDigitosCPF(String cpf) {
        // Primeiro dígito
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
        }
        int digito1 = 11 - (soma % 11);
        if (digito1 > 9) digito1 = 0;
        
        if (digito1 != Character.getNumericValue(cpf.charAt(9))) {
            return false;
        }
        
        // Segundo dígito
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
        }
        int digito2 = 11 - (soma % 11);
        if (digito2 > 9) digito2 = 0;
        
        return digito2 == Character.getNumericValue(cpf.charAt(10));
    }
    
    /**
     * Classe para resultado de validação
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        
        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
