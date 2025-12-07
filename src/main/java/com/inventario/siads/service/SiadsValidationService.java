package com.inventario.siads.service;

import com.inventario.siads.config.SiadsConfig;
import com.inventario.siads.model.SiadsRegistro;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Serviço de validação de dados para exportação SIADS
 * 
 * Valida conformidade com normas federais:
 * - IN SGD/ME nº 1/2019 (Gestão de Patrimônio)
 * - Decreto nº 9.373/2018 (Alienação de Bens)
 * - Manual SIADS v6.2.11
 * 
 * @version 1.0.0
 */
public class SiadsValidationService {
    
    private SiadsConfig config;
    
    // Padrões de validação
    private static final Pattern CPF_PATTERN = Pattern.compile("^\\d{11}$");
    private static final Pattern CNPJ_PATTERN = Pattern.compile("^\\d{14}$");
    private static final Pattern CODIGO_UG_PATTERN = Pattern.compile("^\\d{6}$");
    private static final Pattern CODIGO_ORGAO_PATTERN = Pattern.compile("^\\d{5}$");
    
    public SiadsValidationService() {
        this.config = new SiadsConfig();
    }
    
    public SiadsValidationService(SiadsConfig config) {
        this.config = config;
    }
    
    /**
     * Resultado da validação
     */
    public static class ValidationResult {
        private boolean valid;
        private List<String> errors;
        private List<String> warnings;
        
        public ValidationResult() {
            this.valid = true;
            this.errors = new ArrayList<>();
            this.warnings = new ArrayList<>();
        }
        
        public void addError(String error) {
            this.errors.add(error);
            this.valid = false;
        }
        
        public void addWarning(String warning) {
            this.warnings.add(warning);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public List<String> getWarnings() {
            return warnings;
        }
        
        public String getResumo() {
            StringBuilder sb = new StringBuilder();
            if (valid) {
                sb.append("✅ Validação OK\n");
            } else {
                sb.append("❌ Validação com erros\n");
            }
            
            if (!errors.isEmpty()) {
                sb.append("\nErros (").append(errors.size()).append("):\n");
                for (String error : errors) {
                    sb.append("  • ").append(error).append("\n");
                }
            }
            
            if (!warnings.isEmpty()) {
                sb.append("\nAvisos (").append(warnings.size()).append("):\n");
                for (String warning : warnings) {
                    sb.append("  ⚠ ").append(warning).append("\n");
                }
            }
            
            return sb.toString();
        }
    }
    
    /**
     * Valida um registro SIADS conforme normas federais
     */
    public ValidationResult validarRegistro(SiadsRegistro registro) {
        ValidationResult result = new ValidationResult();
        
        // ========================================
        // CAMPOS OBRIGATÓRIOS (Manual SIADS v6.2.11)
        // ========================================
        
        // Número do patrimônio (obrigatório)
        if (registro.getNumeroPatrimonio() == null || registro.getNumeroPatrimonio().trim().isEmpty()) {
            result.addError("Número do patrimônio é obrigatório");
        }
        
        // Descrição (obrigatório)
        if (registro.getDescricao() == null || registro.getDescricao().trim().isEmpty()) {
            result.addError("Descrição do bem é obrigatória");
        }
        
        // Código UOrg (obrigatório)
        if (registro.getCodigoUorg() == null || registro.getCodigoUorg().trim().isEmpty()) {
            result.addError("Código da Unidade Organizacional (UOrg) é obrigatório");
        }
        
        // Data de aquisição (obrigatório)
        if (config.isDataObrigatoria() && registro.getDataAquisicao() == null) {
            result.addError("Data de aquisição é obrigatória");
        }
        
        // Valor de aquisição (obrigatório)
        if (config.isValorObrigatorio()) {
            if (registro.getValorAquisicao() == null || 
                registro.getValorAquisicao().compareTo(BigDecimal.ZERO) <= 0) {
                result.addError("Valor de aquisição é obrigatório e deve ser maior que zero");
            }
        }
        
        // Estado de conservação (obrigatório)
        if (registro.getEstadoConservacao() == null || registro.getEstadoConservacao().trim().isEmpty()) {
            result.addError("Estado de conservação é obrigatório");
        } else {
            // Validar código (1-5)
            String estado = registro.getEstadoConservacao();
            if (!estado.matches("[1-5]")) {
                result.addError("Estado de conservação deve ser um código de 1 a 5");
            }
        }
        
        // Situação do bem (obrigatório)
        if (registro.getSituacaoBem() == null || registro.getSituacaoBem().trim().isEmpty()) {
            result.addError("Situação do bem é obrigatória");
        } else {
            // Validar código (1-3)
            String situacao = registro.getSituacaoBem();
            if (!situacao.matches("[1-3]")) {
                result.addError("Situação do bem deve ser um código de 1 a 3");
            }
        }
        
        // ========================================
        // VALIDAÇÃO DE CPF (IN SGD/ME nº 1/2019)
        // ========================================
        
        if (config.isCpfObrigatorio()) {
            String cpf = registro.getCpfResponsavel();
            if (cpf == null || cpf.trim().isEmpty()) {
                result.addError("CPF do responsável é obrigatório");
            } else {
                // Limpar CPF
                String cpfLimpo = cpf.replaceAll("[^0-9]", "");
                if (!CPF_PATTERN.matcher(cpfLimpo).matches()) {
                    result.addError("CPF do responsável deve ter 11 dígitos");
                } else if (!validarDigitosCPF(cpfLimpo)) {
                    result.addWarning("CPF do responsável pode estar inválido (dígitos verificadores)");
                }
            }
        }
        
        // ========================================
        // VALIDAÇÃO DE BAIXA (Decreto nº 9.373/2018)
        // ========================================
        
        if (registro.isBaixado()) {
            // Se baixado, deve ter data de baixa
            if (registro.getDataBaixa() == null) {
                result.addError("Bem baixado deve ter data de baixa informada");
            }
            
            // Se baixado, deve ter motivo
            if (registro.getMotivoBaixa() == null || registro.getMotivoBaixa().trim().isEmpty()) {
                result.addError("Bem baixado deve ter motivo de baixa informado");
            } else {
                // Validar código do motivo (1-10)
                String motivo = registro.getMotivoBaixa();
                if (!motivo.matches("([1-9]|10)")) {
                    result.addWarning("Motivo de baixa deve ser um código de 1 a 10");
                }
            }
            
            // Número do processo de baixa (recomendado)
            if (registro.getNumeroProcessoBaixa() == null || registro.getNumeroProcessoBaixa().trim().isEmpty()) {
                result.addWarning("Recomendado informar número do processo de baixa");
            }
        }
        
        // ========================================
        // VALIDAÇÕES DE DEPRECIAÇÃO (NBC TSP 07)
        // ========================================
        
        if (registro.getValorDepreciado() != null && registro.getValorAquisicao() != null) {
            if (registro.getValorDepreciado().compareTo(registro.getValorAquisicao()) > 0) {
                result.addWarning("Valor depreciado é maior que valor de aquisição");
            }
        }
        
        // ========================================
        // AVISOS (Campos opcionais recomendados)
        // ========================================
        
        // Código CATMAT
        if (registro.getCodigoCatmat() == null || registro.getCodigoCatmat().trim().isEmpty()) {
            result.addWarning("Código CATMAT não informado - recomendado para integração com ComprasNet");
        }
        
        // Modelo
        if (registro.getModelo() == null || registro.getModelo().trim().isEmpty()) {
            result.addWarning("Modelo do bem não informado");
        }
        
        // Número de série
        if (registro.getNumeroSerie() == null || registro.getNumeroSerie().trim().isEmpty()) {
            result.addWarning("Número de série não informado");
        }
        
        // Nota fiscal
        if (registro.getNumeroNotaFiscal() == null || registro.getNumeroNotaFiscal().trim().isEmpty()) {
            result.addWarning("Número da nota fiscal não informado");
        }
        
        return result;
    }
    
    /**
     * Valida uma lista de registros
     */
    public ValidationResult validarRegistros(List<SiadsRegistro> registros) {
        ValidationResult resultGeral = new ValidationResult();
        int registrosComErro = 0;
        int registrosComAviso = 0;
        
        for (int i = 0; i < registros.size(); i++) {
            SiadsRegistro registro = registros.get(i);
            ValidationResult result = validarRegistro(registro);
            
            if (!result.isValid()) {
                registrosComErro++;
                for (String error : result.getErrors()) {
                    resultGeral.addError("Registro " + (i + 1) + " (" + registro.getNumeroPatrimonio() + "): " + error);
                }
            }
            
            if (!result.getWarnings().isEmpty()) {
                registrosComAviso++;
                for (String warning : result.getWarnings()) {
                    resultGeral.addWarning("Registro " + (i + 1) + " (" + registro.getNumeroPatrimonio() + "): " + warning);
                }
            }
        }
        
        // Adicionar resumo
        if (registrosComErro > 0) {
            resultGeral.addError("Total de registros com erro: " + registrosComErro + " de " + registros.size());
        }
        
        if (registrosComAviso > 0) {
            resultGeral.addWarning("Total de registros com avisos: " + registrosComAviso + " de " + registros.size());
        }
        
        return resultGeral;
    }
    
    /**
     * Valida dígitos verificadores do CPF
     */
    private boolean validarDigitosCPF(String cpf) {
        if (cpf == null || cpf.length() != 11) return false;
        
        // Verifica se todos os dígitos são iguais
        if (cpf.matches("(\\d)\\1{10}")) return false;
        
        try {
            // Calcula primeiro dígito verificador
            int soma = 0;
            for (int i = 0; i < 9; i++) {
                soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
            }
            int resto = soma % 11;
            int digito1 = resto < 2 ? 0 : 11 - resto;
            
            // Calcula segundo dígito verificador
            soma = 0;
            for (int i = 0; i < 10; i++) {
                soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
            }
            resto = soma % 11;
            int digito2 = resto < 2 ? 0 : 11 - resto;
            
            // Verifica se os dígitos calculados conferem
            return Character.getNumericValue(cpf.charAt(9)) == digito1 &&
                   Character.getNumericValue(cpf.charAt(10)) == digito2;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Valida configuração institucional
     */
    public ValidationResult validarConfiguracao() {
        ValidationResult result = new ValidationResult();
        
        // Código do órgão
        String codigoOrgao = config.getCodigoOrgao();
        if (codigoOrgao == null || !CODIGO_ORGAO_PATTERN.matcher(codigoOrgao).matches()) {
            result.addError("Código do órgão deve ter 5 dígitos");
        }
        
        // Código da UG
        String codigoUG = config.getCodigoUG();
        if (codigoUG == null || !CODIGO_UG_PATTERN.matcher(codigoUG).matches()) {
            result.addError("Código da Unidade Gestora deve ter 6 dígitos");
        }
        
        // CPF do responsável
        String cpf = config.getCpfResponsavel();
        if (cpf == null || cpf.equals("00000000000")) {
            result.addError("CPF do responsável pelo sistema não configurado");
        } else {
            String cpfLimpo = cpf.replaceAll("[^0-9]", "");
            if (!CPF_PATTERN.matcher(cpfLimpo).matches()) {
                result.addError("CPF do responsável deve ter 11 dígitos");
            }
        }
        
        // CNPJ (opcional mas recomendado)
        String cnpj = config.getCnpj();
        if (cnpj != null && !cnpj.isEmpty()) {
            String cnpjLimpo = cnpj.replaceAll("[^0-9]", "");
            if (!CNPJ_PATTERN.matcher(cnpjLimpo).matches()) {
                result.addWarning("CNPJ deve ter 14 dígitos");
            }
        }
        
        return result;
    }
}
