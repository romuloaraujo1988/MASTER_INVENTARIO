package com.inventario.sihcp.siads.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Configurações do SIADS
 * Armazena códigos institucionais necessários para exportação
 * 
 * Adequado às normas federais:
 * - IN SGD/ME nº 1/2019 (Gestão de Patrimônio)
 * - Decreto nº 9.373/2018 (Alienação de Bens)
 * - Manual SIADS v6.2.11
 * 
 * @version 2.0.0 - Adequação às normas federais
 */
public class SiadsConfig {
    
    private static final String CONFIG_FILE = "siads.properties";
    
    // Valores padrão para IFMT
    private static final String DEFAULT_CODIGO_ORGAO = "25000";      // MEC
    private static final String DEFAULT_CODIGO_UG = "158497";        // IFMT (exemplo)
    private static final String DEFAULT_CPF_RESPONSAVEL = "00000000000";
    private static final String DEFAULT_CNPJ = "10784782000127";     // CNPJ IFMT (exemplo)
    
    private Properties properties;
    
    public SiadsConfig() {
        properties = new Properties();
        carregarConfiguracao();
    }
    
    /**
     * Carrega configuração do arquivo
     */
    private void carregarConfiguracao() {
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            properties.load(fis);
        } catch (IOException e) {
            // Se não existir, usa valores padrão
            configurarValoresPadrao();
        }
    }
    
    /**
     * Configura valores padrão conforme normas federais
     */
    private void configurarValoresPadrao() {
        // Identificação institucional
        properties.setProperty("siads.codigo.orgao", DEFAULT_CODIGO_ORGAO);
        properties.setProperty("siads.codigo.ug", DEFAULT_CODIGO_UG);
        properties.setProperty("siads.cnpj", DEFAULT_CNPJ);
        properties.setProperty("siads.nome.instituicao", "IFMT - Instituto Federal de Mato Grosso");
        properties.setProperty("siads.sigla.instituicao", "IFMT");
        
        // Responsável pelo sistema
        properties.setProperty("siads.cpf.responsavel", DEFAULT_CPF_RESPONSAVEL);
        properties.setProperty("siads.nome.responsavel", "");
        properties.setProperty("siads.cargo.responsavel", "");
        properties.setProperty("siads.matricula.responsavel", "");
        
        // Configurações de depreciação (NBC TSP 07)
        properties.setProperty("siads.depreciacao.metodo", "LINEAR");
        properties.setProperty("siads.depreciacao.valor.residual.percentual", "10");
        
        // Configurações de exportação
        properties.setProperty("siads.exportacao.encoding", "UTF-8");
        properties.setProperty("siads.exportacao.delimitador.campo", "¥");
        properties.setProperty("siads.exportacao.delimitador.linha", "£");
        
        // Versão do layout SIADS
        properties.setProperty("siads.versao.layout", "6.2.11");
        
        // Configurações de validação
        properties.setProperty("siads.validacao.cpf.obrigatorio", "true");
        properties.setProperty("siads.validacao.valor.obrigatorio", "true");
        properties.setProperty("siads.validacao.data.obrigatoria", "true");
    }
    
    /**
     * Salva configuração no arquivo
     */
    public void salvarConfiguracao() throws IOException {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            properties.store(fos, "Configurações SIADS - Sistema de Inventário IFMT\n" +
                "# Adequado às normas federais:\n" +
                "# - IN SGD/ME nº 1/2019\n" +
                "# - Decreto nº 9.373/2018\n" +
                "# - Manual SIADS v6.2.11");
        }
    }
    
    // ========================================
    // GETTERS - Identificação Institucional
    // ========================================
    
    public String getCodigoOrgao() {
        return properties.getProperty("siads.codigo.orgao", DEFAULT_CODIGO_ORGAO);
    }
    
    public String getCodigoUG() {
        return properties.getProperty("siads.codigo.ug", DEFAULT_CODIGO_UG);
    }
    
    public String getCnpj() {
        return properties.getProperty("siads.cnpj", DEFAULT_CNPJ);
    }
    
    public String getNomeInstituicao() {
        return properties.getProperty("siads.nome.instituicao", "");
    }
    
    public String getSiglaInstituicao() {
        return properties.getProperty("siads.sigla.instituicao", "");
    }
    
    // ========================================
    // GETTERS - Responsável
    // ========================================
    
    public String getCpfResponsavel() {
        return properties.getProperty("siads.cpf.responsavel", DEFAULT_CPF_RESPONSAVEL);
    }
    
    public String getNomeResponsavel() {
        return properties.getProperty("siads.nome.responsavel", "");
    }
    
    public String getCargoResponsavel() {
        return properties.getProperty("siads.cargo.responsavel", "");
    }
    
    public String getMatriculaResponsavel() {
        return properties.getProperty("siads.matricula.responsavel", "");
    }
    
    // ========================================
    // GETTERS - Depreciação (NBC TSP 07)
    // ========================================
    
    public String getMetodoDepreciacao() {
        return properties.getProperty("siads.depreciacao.metodo", "LINEAR");
    }
    
    public int getValorResidualPercentual() {
        try {
            return Integer.parseInt(properties.getProperty("siads.depreciacao.valor.residual.percentual", "10"));
        } catch (NumberFormatException e) {
            return 10;
        }
    }
    
    // ========================================
    // GETTERS - Exportação
    // ========================================
    
    public String getEncoding() {
        return properties.getProperty("siads.exportacao.encoding", "UTF-8");
    }
    
    public String getDelimitadorCampo() {
        return properties.getProperty("siads.exportacao.delimitador.campo", "¥");
    }
    
    public String getDelimitadorLinha() {
        return properties.getProperty("siads.exportacao.delimitador.linha", "£");
    }
    
    public String getVersaoLayout() {
        return properties.getProperty("siads.versao.layout", "6.2.11");
    }
    
    // ========================================
    // GETTERS - Validação
    // ========================================
    
    public boolean isCpfObrigatorio() {
        return Boolean.parseBoolean(properties.getProperty("siads.validacao.cpf.obrigatorio", "true"));
    }
    
    public boolean isValorObrigatorio() {
        return Boolean.parseBoolean(properties.getProperty("siads.validacao.valor.obrigatorio", "true"));
    }
    
    public boolean isDataObrigatoria() {
        return Boolean.parseBoolean(properties.getProperty("siads.validacao.data.obrigatoria", "true"));
    }
    
    // ========================================
    // SETTERS - Identificação Institucional
    // ========================================
    
    public void setCodigoOrgao(String codigoOrgao) {
        properties.setProperty("siads.codigo.orgao", codigoOrgao);
    }
    
    public void setCodigoUG(String codigoUG) {
        properties.setProperty("siads.codigo.ug", codigoUG);
    }
    
    public void setCnpj(String cnpj) {
        properties.setProperty("siads.cnpj", cnpj);
    }
    
    public void setNomeInstituicao(String nomeInstituicao) {
        properties.setProperty("siads.nome.instituicao", nomeInstituicao);
    }
    
    public void setSiglaInstituicao(String siglaInstituicao) {
        properties.setProperty("siads.sigla.instituicao", siglaInstituicao);
    }
    
    // ========================================
    // SETTERS - Responsável
    // ========================================
    
    public void setCpfResponsavel(String cpfResponsavel) {
        properties.setProperty("siads.cpf.responsavel", cpfResponsavel);
    }
    
    public void setNomeResponsavel(String nomeResponsavel) {
        properties.setProperty("siads.nome.responsavel", nomeResponsavel);
    }
    
    public void setCargoResponsavel(String cargoResponsavel) {
        properties.setProperty("siads.cargo.responsavel", cargoResponsavel);
    }
    
    public void setMatriculaResponsavel(String matriculaResponsavel) {
        properties.setProperty("siads.matricula.responsavel", matriculaResponsavel);
    }
    
    // ========================================
    // SETTERS - Depreciação
    // ========================================
    
    public void setMetodoDepreciacao(String metodo) {
        properties.setProperty("siads.depreciacao.metodo", metodo);
    }
    
    public void setValorResidualPercentual(int percentual) {
        properties.setProperty("siads.depreciacao.valor.residual.percentual", String.valueOf(percentual));
    }
    
    // ========================================
    // SETTERS - Validação
    // ========================================
    
    public void setCpfObrigatorio(boolean obrigatorio) {
        properties.setProperty("siads.validacao.cpf.obrigatorio", String.valueOf(obrigatorio));
    }
    
    public void setValorObrigatorio(boolean obrigatorio) {
        properties.setProperty("siads.validacao.valor.obrigatorio", String.valueOf(obrigatorio));
    }
    
    public void setDataObrigatoria(boolean obrigatoria) {
        properties.setProperty("siads.validacao.data.obrigatoria", String.valueOf(obrigatoria));
    }
    
    /**
     * Retorna todas as propriedades para exibição
     */
    public Properties getProperties() {
        return properties;
    }
    
    /**
     * Verifica se a configuração está completa para exportação
     */
    public boolean isConfiguracaoCompleta() {
        return getCodigoOrgao() != null && !getCodigoOrgao().isEmpty() &&
               getCodigoUG() != null && !getCodigoUG().isEmpty() &&
               getCpfResponsavel() != null && !getCpfResponsavel().isEmpty() &&
               !getCpfResponsavel().equals("00000000000");
    }
    
    /**
     * Retorna mensagem de validação da configuração
     */
    public String validarConfiguracao() {
        StringBuilder erros = new StringBuilder();
        
        if (getCodigoOrgao() == null || getCodigoOrgao().isEmpty()) {
            erros.append("- Código do Órgão não configurado\n");
        }
        
        if (getCodigoUG() == null || getCodigoUG().isEmpty()) {
            erros.append("- Código da Unidade Gestora não configurado\n");
        }
        
        if (getCpfResponsavel() == null || getCpfResponsavel().isEmpty() || 
            getCpfResponsavel().equals("00000000000")) {
            erros.append("- CPF do Responsável não configurado\n");
        }
        
        if (erros.length() == 0) {
            return "Configuração válida";
        }
        
        return "Erros encontrados:\n" + erros.toString();
    }
}
