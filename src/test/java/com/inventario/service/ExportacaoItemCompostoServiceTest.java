package com.inventario.service;

import com.inventario.model.EstatisticasIntegridade;
import com.inventario.model.ItemCompostoResumo;
import com.inventario.model.StatusIntegridade;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para ExportacaoItemCompostoService.
 * 
 * **Feature: relatorio-itens-compostos, Property 8: Exportação Excel contém estrutura correta**
 * **Feature: relatorio-itens-compostos, Property 9: Exportação CSV usa formato correto**
 * **Validates: Requirements 5.2, 5.4**
 */
@DisplayName("ExportacaoItemCompostoService - Testes de Exportação")
class ExportacaoItemCompostoServiceTest {
    
    private ExportacaoItemCompostoService service;
    private List<ItemCompostoResumo> itensParaTeste;
    private EstatisticasIntegridade statsParaTeste;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        service = new ExportacaoItemCompostoService();
        itensParaTeste = criarItensParaTeste();
        statsParaTeste = criarEstatisticasParaTeste();
    }
    
    private List<ItemCompostoResumo> criarItensParaTeste() {
        List<ItemCompostoResumo> itens = new ArrayList<>();
        
        ItemCompostoResumo item1 = new ItemCompostoResumo();
        item1.setIdPatrimonio(1);
        item1.setNumeroPatrimonio("12345");
        item1.setDescricaoPatrimonio("Conjunto de Escritório");
        item1.setNomeSala("Sala 101");
        item1.setNomeResponsavel("Ana Silva");
        item1.setComponentesEsperados(5);
        item1.setComponentesEncontrados(5);
        item1.setComponentesFaltantes(0);
        item1.setTaxaIntegridade(100.0);
        item1.setStatus(StatusIntegridade.COMPLETO);
        item1.setTiposFaltantes(Arrays.asList());
        itens.add(item1);
        
        ItemCompostoResumo item2 = new ItemCompostoResumo();
        item2.setIdPatrimonio(2);
        item2.setNumeroPatrimonio("67890");
        item2.setDescricaoPatrimonio("Conjunto de Informática");
        item2.setNomeSala("Sala 202");
        item2.setNomeResponsavel("Carlos Santos");
        item2.setComponentesEsperados(5);
        item2.setComponentesEncontrados(3);
        item2.setComponentesFaltantes(2);
        item2.setTaxaIntegridade(60.0);
        item2.setStatus(StatusIntegridade.PARCIAL);
        item2.setTiposFaltantes(Arrays.asList("MONITOR", "TECLADO"));
        itens.add(item2);
        
        return itens;
    }
    
    private EstatisticasIntegridade criarEstatisticasParaTeste() {
        return new EstatisticasIntegridade(100, 80, 10, 80.0);
    }
    
    @Nested
    @DisplayName("Testes de Exportação Excel")
    class ExportacaoExcel {
        
        @Test
        @DisplayName("exportarExcel() deve criar arquivo .xlsx válido")
        void exportarExcel_CriaArquivoValido() throws IOException {
            String caminho = tempDir.resolve("teste.xlsx").toString();
            
            File arquivo = service.exportarExcel(itensParaTeste, statsParaTeste, caminho);
            
            assertNotNull(arquivo);
            assertTrue(arquivo.exists());
            assertTrue(arquivo.getName().endsWith(".xlsx"));
        }
        
        @Test
        @DisplayName("exportarExcel() deve criar workbook com 2 abas")
        void exportarExcel_CriaDuasAbas() throws IOException {
            String caminho = tempDir.resolve("teste.xlsx").toString();
            
            service.exportarExcel(itensParaTeste, statsParaTeste, caminho);
            
            try (FileInputStream fis = new FileInputStream(caminho);
                 Workbook workbook = new XSSFWorkbook(fis)) {
                
                assertEquals(2, workbook.getNumberOfSheets());
            }
        }
        
        @Test
        @DisplayName("exportarExcel() deve ter aba 'Resumo' como primeira")
        void exportarExcel_PrimeiraAbaResumo() throws IOException {
            String caminho = tempDir.resolve("teste.xlsx").toString();
            
            service.exportarExcel(itensParaTeste, statsParaTeste, caminho);
            
            try (FileInputStream fis = new FileInputStream(caminho);
                 Workbook workbook = new XSSFWorkbook(fis)) {
                
                Sheet primeiraAba = workbook.getSheetAt(0);
                assertEquals("Resumo", primeiraAba.getSheetName());
            }
        }
        
        @Test
        @DisplayName("exportarExcel() deve ter aba 'Detalhamento' como segunda")
        void exportarExcel_SegundaAbaDetalhamento() throws IOException {
            String caminho = tempDir.resolve("teste.xlsx").toString();
            
            service.exportarExcel(itensParaTeste, statsParaTeste, caminho);
            
            try (FileInputStream fis = new FileInputStream(caminho);
                 Workbook workbook = new XSSFWorkbook(fis)) {
                
                Sheet segundaAba = workbook.getSheetAt(1);
                assertEquals("Detalhamento", segundaAba.getSheetName());
            }
        }
        
        @Test
        @DisplayName("exportarExcel() aba Detalhamento deve ter cabeçalho + dados")
        void exportarExcel_DetalhamentoTemCabecalhoEDados() throws IOException {
            String caminho = tempDir.resolve("teste.xlsx").toString();
            
            service.exportarExcel(itensParaTeste, statsParaTeste, caminho);
            
            try (FileInputStream fis = new FileInputStream(caminho);
                 Workbook workbook = new XSSFWorkbook(fis)) {
                
                Sheet detalhamento = workbook.getSheet("Detalhamento");
                
                // Linha 0 = cabeçalho, linhas 1+ = dados
                // Com 2 itens, deve ter 3 linhas (1 cabeçalho + 2 dados)
                assertTrue(detalhamento.getLastRowNum() >= 2);
                
                // Verificar cabeçalho
                assertEquals("Nº Patrimônio", detalhamento.getRow(0).getCell(0).getStringCellValue());
            }
        }
        
        @Test
        @DisplayName("exportarExcel() deve conter dados dos itens")
        void exportarExcel_ContemDadosDosItens() throws IOException {
            String caminho = tempDir.resolve("teste.xlsx").toString();
            
            service.exportarExcel(itensParaTeste, statsParaTeste, caminho);
            
            try (FileInputStream fis = new FileInputStream(caminho);
                 Workbook workbook = new XSSFWorkbook(fis)) {
                
                Sheet detalhamento = workbook.getSheet("Detalhamento");
                
                // Verificar primeiro item (linha 1)
                assertEquals("12345", detalhamento.getRow(1).getCell(0).getStringCellValue());
                
                // Verificar segundo item (linha 2)
                assertEquals("67890", detalhamento.getRow(2).getCell(0).getStringCellValue());
            }
        }
    }
    
    @Nested
    @DisplayName("Testes de Exportação CSV")
    class ExportacaoCSV {
        
        @Test
        @DisplayName("exportarCSV() deve criar arquivo válido")
        void exportarCSV_CriaArquivoValido() throws IOException {
            String caminho = tempDir.resolve("teste.csv").toString();
            
            File arquivo = service.exportarCSV(itensParaTeste, caminho);
            
            assertNotNull(arquivo);
            assertTrue(arquivo.exists());
        }
        
        @Test
        @DisplayName("exportarCSV() deve usar ponto-e-vírgula como separador")
        void exportarCSV_UsaPontoEVirgula() throws IOException {
            String caminho = tempDir.resolve("teste.csv").toString();
            
            service.exportarCSV(itensParaTeste, caminho);
            
            String conteudo = Files.readString(Path.of(caminho), StandardCharsets.UTF_8);
            
            // Verificar que usa ; como separador
            assertTrue(conteudo.contains(";"));
            
            // Verificar cabeçalho com separadores
            assertTrue(conteudo.contains("Nº Patrimônio;Descrição;Sala;"));
        }
        
        @Test
        @DisplayName("exportarCSV() deve usar UTF-8 com BOM")
        void exportarCSV_UsaUTF8ComBOM() throws IOException {
            String caminho = tempDir.resolve("teste.csv").toString();
            
            service.exportarCSV(itensParaTeste, caminho);
            
            byte[] bytes = Files.readAllBytes(Path.of(caminho));
            
            // BOM UTF-8: EF BB BF
            assertEquals((byte) 0xEF, bytes[0]);
            assertEquals((byte) 0xBB, bytes[1]);
            assertEquals((byte) 0xBF, bytes[2]);
        }
        
        @Test
        @DisplayName("exportarCSV() deve ter cabeçalho na primeira linha")
        void exportarCSV_TemCabecalho() throws IOException {
            String caminho = tempDir.resolve("teste.csv").toString();
            
            service.exportarCSV(itensParaTeste, caminho);
            
            List<String> linhas = Files.readAllLines(Path.of(caminho), StandardCharsets.UTF_8);
            
            // Primeira linha (após BOM) deve ser o cabeçalho
            String cabecalho = linhas.get(0).replace("\uFEFF", ""); // Remove BOM se presente
            assertTrue(cabecalho.startsWith("Nº Patrimônio"));
            assertTrue(cabecalho.contains("Descrição"));
            assertTrue(cabecalho.contains("Status"));
        }
        
        @Test
        @DisplayName("exportarCSV() deve ter uma linha por item")
        void exportarCSV_UmaLinhaPorItem() throws IOException {
            String caminho = tempDir.resolve("teste.csv").toString();
            
            service.exportarCSV(itensParaTeste, caminho);
            
            List<String> linhas = Files.readAllLines(Path.of(caminho), StandardCharsets.UTF_8);
            
            // 1 cabeçalho + 2 itens = 3 linhas
            assertEquals(3, linhas.size());
        }
        
        @Test
        @DisplayName("exportarCSV() deve conter dados dos itens")
        void exportarCSV_ContemDadosDosItens() throws IOException {
            String caminho = tempDir.resolve("teste.csv").toString();
            
            service.exportarCSV(itensParaTeste, caminho);
            
            String conteudo = Files.readString(Path.of(caminho), StandardCharsets.UTF_8);
            
            assertTrue(conteudo.contains("12345"));
            assertTrue(conteudo.contains("67890"));
            assertTrue(conteudo.contains("Conjunto de Escritório"));
            assertTrue(conteudo.contains("Ana Silva"));
            assertTrue(conteudo.contains("Completo"));
            assertTrue(conteudo.contains("Parcial"));
        }
        
        @Test
        @DisplayName("exportarCSV() deve escapar valores com ponto-e-vírgula")
        void exportarCSV_EscapaValoresComSeparador() throws IOException {
            // Criar item com descrição contendo ponto-e-vírgula
            ItemCompostoResumo itemComSeparador = new ItemCompostoResumo();
            itemComSeparador.setNumeroPatrimonio("99999");
            itemComSeparador.setDescricaoPatrimonio("Conjunto; com separador");
            itemComSeparador.setNomeSala("Sala 101");
            itemComSeparador.setNomeResponsavel("Teste");
            itemComSeparador.setComponentesEsperados(1);
            itemComSeparador.setComponentesEncontrados(1);
            itemComSeparador.setComponentesFaltantes(0);
            itemComSeparador.setTaxaIntegridade(100.0);
            itemComSeparador.setStatus(StatusIntegridade.COMPLETO);
            itemComSeparador.setTiposFaltantes(Arrays.asList());
            
            String caminho = tempDir.resolve("teste_escape.csv").toString();
            
            service.exportarCSV(Arrays.asList(itemComSeparador), caminho);
            
            String conteudo = Files.readString(Path.of(caminho), StandardCharsets.UTF_8);
            
            // Valor com separador deve estar entre aspas
            assertTrue(conteudo.contains("\"Conjunto; com separador\""));
        }
    }
    
    @Nested
    @DisplayName("Testes de Exportação PDF")
    class ExportacaoPDF {
        
        @Test
        @DisplayName("exportarPDF() deve criar arquivo válido")
        void exportarPDF_CriaArquivoValido() throws IOException {
            String caminho = tempDir.resolve("teste.pdf").toString();
            
            File arquivo = service.exportarPDF(itensParaTeste, statsParaTeste, caminho);
            
            assertNotNull(arquivo);
            assertTrue(arquivo.exists());
            assertTrue(arquivo.length() > 0);
        }
        
        @Test
        @DisplayName("exportarPDF() deve criar arquivo com extensão .pdf")
        void exportarPDF_ExtensaoPDF() throws IOException {
            String caminho = tempDir.resolve("teste.pdf").toString();
            
            File arquivo = service.exportarPDF(itensParaTeste, statsParaTeste, caminho);
            
            assertTrue(arquivo.getName().endsWith(".pdf"));
        }
        
        @Test
        @DisplayName("exportarPDF() deve criar arquivo com tamanho razoável")
        void exportarPDF_TamanhoRazoavel() throws IOException {
            String caminho = tempDir.resolve("teste.pdf").toString();
            
            File arquivo = service.exportarPDF(itensParaTeste, statsParaTeste, caminho);
            
            // PDF deve ter pelo menos alguns KB
            assertTrue(arquivo.length() > 1000, "PDF deve ter mais de 1KB");
            // Mas não deve ser excessivamente grande para 2 itens
            assertTrue(arquivo.length() < 100000, "PDF não deve ter mais de 100KB para 2 itens");
        }
    }
    
    @Nested
    @DisplayName("Testes de Casos Especiais")
    class CasosEspeciais {
        
        @Test
        @DisplayName("exportarCSV() com lista vazia deve criar arquivo só com cabeçalho")
        void exportarCSV_ListaVazia_SoCabecalho() throws IOException {
            String caminho = tempDir.resolve("teste_vazio.csv").toString();
            
            service.exportarCSV(new ArrayList<>(), caminho);
            
            List<String> linhas = Files.readAllLines(Path.of(caminho), StandardCharsets.UTF_8);
            
            assertEquals(1, linhas.size()); // Só cabeçalho
        }
        
        @Test
        @DisplayName("exportarExcel() com lista vazia deve criar arquivo válido")
        void exportarExcel_ListaVazia_ArquivoValido() throws IOException {
            String caminho = tempDir.resolve("teste_vazio.xlsx").toString();
            
            File arquivo = service.exportarExcel(new ArrayList<>(), statsParaTeste, caminho);
            
            assertTrue(arquivo.exists());
            
            try (FileInputStream fis = new FileInputStream(caminho);
                 Workbook workbook = new XSSFWorkbook(fis)) {
                
                assertEquals(2, workbook.getNumberOfSheets());
            }
        }
    }
}
