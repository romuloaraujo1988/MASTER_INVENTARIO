package com.inventario.siads.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Configurações do SIADS
 * Armazena códigos institucionais necessários para exportação
 */
public class SiadsConfig {
    
    private static final String CONFIG_FILE = "siads.properties";
    private static final String DEFAULT_CODIGO_ORGAO = "25000";
    private static final String DEFAULT_CODIGO_UG = "00001";
    private static final String DEFAULT_CPF_RESPONSAVEL = "00000000000";
    
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
            properties.setProperty("siads.codigo.orgao", DEFAULT_CODIGO_ORGAO);
            properties.setProperty("siads.codigo.ug", DEFAULT_CODIGO_UG);
            properties.setProperty("siads.cpf.responsavel", DEFAULT_CPF_RESPONSAVEL);
            properties.setProperty("siads.nome.instituicao", "IFMT - Instituto Federal de Mato Grosso");
        }
    }
    
    /**
     * Salva configuração no arquivo
     */
    public void salvarConfiguracao() throws IOException {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            properties.store(fos, "Configurações SIADS");
        }
    }
    
    // Getters
    public String getCodigoOrgao() {
        return properties.getProperty("siads.codigo.orgao", DEFAULT_CODIGO_ORGAO);
    }
    
    public String getCodigoUG() {
        return properties.getProperty("siads.codigo.ug", DEFAULT_CODIGO_UG);
    }
    
    public String getCpfResponsavel() {
        return properties.getProperty("siads.cpf.responsavel", DEFAULT_CPF_RESPONSAVEL);
    }
    
    public String getNomeInstituicao() {
        return properties.getProperty("siads.nome.instituicao", "");
    }
    
    // Setters
    public void setCodigoOrgao(String codigoOrgao) {
        properties.setProperty("siads.codigo.orgao", codigoOrgao);
    }
    
    public void setCodigoUG(String codigoUG) {
        properties.setProperty("siads.codigo.ug", codigoUG);
    }
    
    public void setCpfResponsavel(String cpfResponsavel) {
        properties.setProperty("siads.cpf.responsavel", cpfResponsavel);
    }
    
    public void setNomeInstituicao(String nomeInstituicao) {
        properties.setProperty("siads.nome.instituicao", nomeInstituicao);
    }
}
