package com.inventario.siads.service;

import com.inventario.siads.model.SiadsRegistro;
import com.inventario.siads.util.SiadsLayoutFormatter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Serviço responsável por exportar dados no formato SIADS
 */
public class SiadsExportService {
    
    private final SiadsLayoutFormatter formatter;
    
    public SiadsExportService() {
        this.formatter = new SiadsLayoutFormatter();
    }
    
    /**
     * Exporta registros para arquivo SIADS
     * 
     * @param registros Lista de registros a exportar
     * @param caminhoArquivo Caminho do arquivo de destino
     * @return Arquivo gerado
     * @throws IOException Se houver erro na escrita
     */
    public File exportarArquivo(List<SiadsRegistro> registros, String caminhoArquivo) 
            throws IOException {
        
        File arquivo = new File(caminhoArquivo);
        
        // Calcula valor total em centavos
        long valorTotal = registros.stream()
                .filter(r -> r.getValorAquisicao() != null)
                .mapToLong(r -> r.getValorAquisicao().multiply(new java.math.BigDecimal("100")).longValue())
                .sum();
        
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(arquivo, java.nio.charset.StandardCharsets.UTF_8))) {
            
            // Escreve cabeçalho
            writer.write(formatter.formatarCabecalho());
            writer.newLine();
            
            // Escreve registros
            for (SiadsRegistro registro : registros) {
                String linha = formatter.formatarRegistro(registro);
                writer.write(linha);
                writer.newLine();
            }
            
            // Escreve rodapé com total
            writer.write(formatter.formatarRodape(registros.size(), valorTotal));
            writer.newLine();
        }
        
        return arquivo;
    }
    
    /**
     * Configura os códigos institucionais no formatter
     */
    public void configurarInstituicao(String codigoOrgao, String codigoUG, String cpfResponsavel) {
        formatter.configurarInstituicao(codigoOrgao, codigoUG, cpfResponsavel);
    }
    
    /**
     * Gera nome de arquivo padrão SIADS
     * Formato: SIADS_AAAAMMDD_HHMMSS.txt
     */
    public String gerarNomeArquivo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        return String.format("SIADS_%s.txt", timestamp);
    }
    
    /**
     * Exporta com nome de arquivo automático
     */
    public File exportarArquivo(List<SiadsRegistro> registros, File diretorio) 
            throws IOException {
        String nomeArquivo = gerarNomeArquivo();
        String caminhoCompleto = new File(diretorio, nomeArquivo).getAbsolutePath();
        return exportarArquivo(registros, caminhoCompleto);
    }
}
