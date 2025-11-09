package com.inventario.siads.test;

import com.inventario.siads.config.SiadsConfig;
import com.inventario.siads.model.SiadsRegistro;
import com.inventario.siads.service.SiadsExportService;
import com.inventario.siads.util.SiadsLayoutFormatter;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Teste de exportação SIADS
 * Execute este teste para verificar se o formato está correto
 */
public class SiadsExportTest {
    
    public static void main(String[] args) {
        System.out.println("=== Teste de Exportação SIADS ===\n");
        
        try {
            // 1. Criar registros de teste
            List<SiadsRegistro> registros = criarRegistrosTeste();
            System.out.println("✓ Criados " + registros.size() + " registros de teste");
            
            // 2. Configurar serviço
            SiadsExportService exportService = new SiadsExportService();
            exportService.configurarInstituicao("25000", "158000", "12345678900");
            System.out.println("✓ Serviço configurado");
            
            // 3. Exportar arquivo
            File diretorio = new File(".");
            File arquivo = exportService.exportarArquivo(registros, diretorio);
            System.out.println("✓ Arquivo gerado: " + arquivo.getAbsolutePath());
            
            // 4. Verificar conteúdo
            System.out.println("\n=== Conteúdo do Arquivo ===");
            java.nio.file.Files.lines(arquivo.toPath()).forEach(System.out::println);
            
            System.out.println("\n=== Teste Concluído com Sucesso! ===");
            System.out.println("\nVerifique o arquivo: " + arquivo.getName());
            System.out.println("Formato: UTF-8, delimitadores ¥ e £");
            
        } catch (Exception e) {
            System.err.println("\n✗ Erro no teste:");
            e.printStackTrace();
        }
    }
    
    private static List<SiadsRegistro> criarRegistrosTeste() {
        List<SiadsRegistro> registros = new ArrayList<>();
        
        // Registro 1: Computador
        SiadsRegistro r1 = new SiadsRegistro();
        r1.setCodigoMaterial("P101479014");
        r1.setNumeroPatrimonio("12345");
        r1.setDescricao("COMPUTADOR DESKTOP");
        r1.setEspecificacao("DELL OPTIPLEX 7090 I7 16GB 512GB SSD");
        r1.setClasseContabil("254602152");
        r1.setSala("SALA 101");
        r1.setUnidadeGestora("1790001");
        r1.setDataAquisicao(LocalDate.of(2024, 5, 15));
        r1.setValorAquisicao(new BigDecimal("5500.00"));
        r1.setFormaAquisicao("COMPRA");
        r1.setEstadoConservacao("BOM");
        r1.setSituacaoBem("ATIVO");
        r1.setCpfResponsavel("12345678900");
        r1.setNomeResponsavel("JOAO DA SILVA");
        r1.setGrupoMaterial("DELL");
        registros.add(r1);
        
        // Registro 2: Mesa
        SiadsRegistro r2 = new SiadsRegistro();
        r2.setCodigoMaterial("P101479015");
        r2.setNumeroPatrimonio("12346");
        r2.setDescricao("MESA DE ESCRITORIO");
        r2.setEspecificacao("MESA EM L 1,40X1,40M");
        r2.setClasseContabil("254602153");
        r2.setSala("SALA 102");
        r2.setUnidadeGestora("1790002");
        r2.setDataAquisicao(LocalDate.of(2024, 6, 20));
        r2.setValorAquisicao(new BigDecimal("850.00"));
        r2.setFormaAquisicao("COMPRA");
        r2.setEstadoConservacao("OTIMO");
        r2.setSituacaoBem("ATIVO");
        r2.setCpfResponsavel("98765432100");
        r2.setNomeResponsavel("MARIA SANTOS");
        registros.add(r2);
        
        // Registro 3: Cadeira
        SiadsRegistro r3 = new SiadsRegistro();
        r3.setCodigoMaterial("P101479016");
        r3.setNumeroPatrimonio("12347");
        r3.setDescricao("CADEIRA GIRATORIA");
        r3.setEspecificacao("CADEIRA PRESIDENTE COM BRACO");
        r3.setClasseContabil("254602154");
        r3.setSala("SALA 103");
        r3.setUnidadeGestora("1790003");
        r3.setDataAquisicao(LocalDate.of(2024, 7, 10));
        r3.setValorAquisicao(new BigDecimal("450.00"));
        r3.setFormaAquisicao("COMPRA");
        r3.setEstadoConservacao("REGULAR");
        r3.setSituacaoBem("ATIVO");
        r3.setCpfResponsavel("11122233344");
        r3.setNomeResponsavel("PEDRO OLIVEIRA");
        registros.add(r3);
        
        return registros;
    }
}
