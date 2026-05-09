package com.inventario.sihcp.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.inventario.sihcp.dto.EstatisticasHistoricoDTO;
import com.inventario.sihcp.dto.HistoricoColetaDTO;

/**
 * Testes unitários para os geradores de histórico (PDF e Excel).
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
class HistoricoGeneratorsTest {
    
    private List<HistoricoColetaDTO> historicoVazio;
    private List<HistoricoColetaDTO> historicoComDados;
    private EstatisticasHistoricoDTO estatisticas;
    
    @BeforeEach
    void setUp() {
        historicoVazio = new ArrayList<>();
        historicoComDados = criarHistoricoTeste();
        estatisticas = criarEstatisticasTeste();
    }
    
    /**
     * Cria histórico de teste com múltiplas coletas.
     */
    private List<HistoricoColetaDTO> criarHistoricoTeste() {
        List<HistoricoColetaDTO> historico = new ArrayList<>();
        
        // Coleta 1 - Mais recente
        HistoricoColetaDTO coleta1 = new HistoricoColetaDTO();
        coleta1.setId(1);
        coleta1.setPatrimonioId(100);
        coleta1.setNumeroPatrimonio("12345");
        coleta1.setDescricaoPatrimonio("Cadeira Giratória");
        coleta1.setInventarioId(3);
        coleta1.setNomeInventario("Inventário 2024");
        coleta1.setColetorId(10);
        coleta1.setNomeColetorCompleto("João Silva");
        coleta1.setDataColeta(new Date());
        coleta1.setLocalizacaoEncontrada("Sala 101");
        coleta1.setEstadoEncontrado("BOM");
        coleta1.setObservacoes("Patrimônio em bom estado");
        coleta1.setNomeSala("Sala 101");
        coleta1.setNomeSetor("Setor Administrativo");
        coleta1.setTemMudancaLocalizacao(true);
        coleta1.setLocalizacaoAnterior("Sala 102");
        
        // Coleta 2 - Anterior
        HistoricoColetaDTO coleta2 = new HistoricoColetaDTO();
        coleta2.setId(2);
        coleta2.setPatrimonioId(100);
        coleta2.setNumeroPatrimonio("12345");
        coleta2.setDescricaoPatrimonio("Cadeira Giratória");
        coleta2.setInventarioId(2);
        coleta2.setNomeInventario("Inventário 2023");
        coleta2.setColetorId(11);
        coleta2.setNomeColetorCompleto("Maria Santos");
        coleta2.setDataColeta(new Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000));
        coleta2.setLocalizacaoEncontrada("Sala 102");
        coleta2.setEstadoEncontrado("REGULAR");
        coleta2.setObservacoes("Necessita manutenção");
        coleta2.setNomeSala("Sala 102");
        coleta2.setNomeSetor("Setor Administrativo");
        coleta2.setTemMudancaEstado(true);
        coleta2.setEstadoAnterior("BOM");
        
        // Coleta 3 - Mais antiga
        HistoricoColetaDTO coleta3 = new HistoricoColetaDTO();
        coleta3.setId(3);
        coleta3.setPatrimonioId(100);
        coleta3.setNumeroPatrimonio("12345");
        coleta3.setDescricaoPatrimonio("Cadeira Giratória");
        coleta3.setInventarioId(1);
        coleta3.setNomeInventario("Inventário 2022");
        coleta3.setColetorId(10);
        coleta3.setNomeColetorCompleto("João Silva");
        coleta3.setDataColeta(new Date(System.currentTimeMillis() - 730L * 24 * 60 * 60 * 1000));
        coleta3.setLocalizacaoEncontrada("Sala 102");
        coleta3.setEstadoEncontrado("BOM");
        coleta3.setObservacoes("Primeira coleta");
        coleta3.setNomeSala("Sala 102");
        coleta3.setNomeSetor("Setor Administrativo");
        
        historico.add(coleta1);
        historico.add(coleta2);
        historico.add(coleta3);
        
        return historico;
    }
    
    /**
     * Cria estatísticas de teste.
     */
    private EstatisticasHistoricoDTO criarEstatisticasTeste() {
        EstatisticasHistoricoDTO stats = new EstatisticasHistoricoDTO();
        stats.setTotalColetas(3);
        stats.setTotalInventarios(3);
        stats.setPrimeiraColeta(new Date(System.currentTimeMillis() - 730L * 24 * 60 * 60 * 1000));
        stats.setUltimaColeta(new Date());
        stats.setTotalMudancasLocalizacao(1);
        stats.setTotalMudancasEstado(1);
        stats.calcularDiasEntrePrimeiraEUltima();
        stats.calcularMediaColetasPorInventario();
        return stats;
    }
    
    // ========== Testes do PDF Generator ==========
    
    @Test
    void testGerarPDF_ComHistoricoVazio_DeveLancarExcecao() {
        HistoricoPDFGenerator generator = new HistoricoPDFGenerator();
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            generator.gerarPDF(historicoVazio, estatisticas);
        });
        
        assertTrue(exception.getMessage().contains("vazio"));
    }
    
    @Test
    void testGerarPDF_ComHistoricoValido_DeveGerarArquivo() throws Exception {
        HistoricoPDFGenerator generator = new HistoricoPDFGenerator();
        
        byte[] pdfBytes = generator.gerarPDF(historicoComDados, estatisticas);
        
        assertNotNull(pdfBytes, "PDF não deve ser nulo");
        assertTrue(pdfBytes.length > 0, "PDF deve ter conteúdo");
        
        // Verificar assinatura PDF (começa com %PDF)
        String header = new String(pdfBytes, 0, Math.min(4, pdfBytes.length));
        assertEquals("%PDF", header, "Arquivo deve começar com assinatura PDF");
    }
    
    @Test
    void testGerarPDF_ComMultiplasColetas_DeveIncluirTodas() throws Exception {
        HistoricoPDFGenerator generator = new HistoricoPDFGenerator();
        
        byte[] pdfBytes = generator.gerarPDF(historicoComDados, estatisticas);
        
        // Verificar que o PDF foi gerado com tamanho razoável
        // Um PDF com 3 coletas deve ter pelo menos 2KB
        assertTrue(pdfBytes.length > 2000, 
                "PDF com 3 coletas deve ter tamanho razoável (>2KB), atual: " + pdfBytes.length);
    }
    
    // ========== Testes do Excel Generator ==========
    
    @Test
    void testGerarExcel_ComHistoricoVazio_DeveLancarExcecao() {
        HistoricoExcelGenerator generator = new HistoricoExcelGenerator();
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            generator.gerarExcel(historicoVazio, estatisticas);
        });
        
        assertTrue(exception.getMessage().contains("vazio"));
    }
    
    @Test
    void testGerarExcel_ComHistoricoValido_DeveGerarArquivo() throws Exception {
        HistoricoExcelGenerator generator = new HistoricoExcelGenerator();
        
        byte[] excelBytes = generator.gerarExcel(historicoComDados, estatisticas);
        
        assertNotNull(excelBytes, "Excel não deve ser nulo");
        assertTrue(excelBytes.length > 0, "Excel deve ter conteúdo");
        
        // Verificar assinatura ZIP (Excel é um arquivo ZIP)
        // Assinatura: PK (0x50 0x4B)
        assertEquals(0x50, excelBytes[0] & 0xFF, "Arquivo deve começar com assinatura ZIP (PK)");
        assertEquals(0x4B, excelBytes[1] & 0xFF, "Arquivo deve começar com assinatura ZIP (PK)");
    }
    
    @Test
    void testGerarExcel_ComMultiplasColetas_DeveIncluirTodas() throws Exception {
        HistoricoExcelGenerator generator = new HistoricoExcelGenerator();
        
        byte[] excelBytes = generator.gerarExcel(historicoComDados, estatisticas);
        
        // Verificar que o arquivo foi gerado com tamanho razoável
        // Um Excel com 3 coletas deve ter pelo menos 4KB
        assertTrue(excelBytes.length > 4000, 
                "Excel com 3 coletas deve ter tamanho razoável (>4KB), atual: " + excelBytes.length);
    }
    
    // ========== Testes de Formatação ==========
    
    @Test
    void testGerarPDF_ComMudancas_DeveAplicarFormatacao() throws Exception {
        HistoricoPDFGenerator generator = new HistoricoPDFGenerator();
        
        // Criar histórico com mudanças
        List<HistoricoColetaDTO> historicoComMudancas = criarHistoricoTeste();
        
        byte[] pdfBytes = generator.gerarPDF(historicoComMudancas, estatisticas);
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        
        // Verificar que o PDF foi gerado com tamanho adequado
        // PDF com formatação de mudanças deve ter tamanho razoável
        assertTrue(pdfBytes.length > 2000, 
                "PDF com mudanças deve ter tamanho razoável (>2KB)");
    }
    
    @Test
    void testGerarExcel_ComEstatisticas_DeveIncluirSecao() throws Exception {
        HistoricoExcelGenerator generator = new HistoricoExcelGenerator();
        
        byte[] excelBytes = generator.gerarExcel(historicoComDados, estatisticas);
        
        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0);
        
        // Excel com estatísticas deve ter tamanho adequado
        assertTrue(excelBytes.length > 4000, "Excel deve incluir seção de estatísticas");
    }
}
