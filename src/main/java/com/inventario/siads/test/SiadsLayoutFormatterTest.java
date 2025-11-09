package com.inventario.siads.test;

import com.inventario.siads.model.SiadsRegistro;
import com.inventario.siads.util.SiadsLayoutFormatter;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Classe de teste para SiadsLayoutFormatter
 * Execute este teste para verificar a formatação dos registros
 */
public class SiadsLayoutFormatterTest {
    
    public static void main(String[] args) {
        System.out.println("=== Teste de Formatação SIADS ===\n");
        
        SiadsLayoutFormatter formatter = new SiadsLayoutFormatter();
        
        // Teste 1: Cabeçalho
        System.out.println("1. Teste de Cabeçalho:");
        String cabecalho = formatter.formatarCabecalho();
        System.out.println(cabecalho);
        System.out.println();
        
        // Teste 2: Registro completo
        System.out.println("2. Teste de Registro Completo:");
        SiadsRegistro registro1 = criarRegistroCompleto();
        String linha1 = formatter.formatarRegistro(registro1);
        System.out.println(linha1);
        System.out.println();
        
        // Teste 3: Registro com campos vazios
        System.out.println("3. Teste de Registro com Campos Vazios:");
        SiadsRegistro registro2 = criarRegistroVazio();
        String linha2 = formatter.formatarRegistro(registro2);
        System.out.println(linha2);
        System.out.println();
        
        // Teste 4: Rodapé
        System.out.println("4. Teste de Rodapé:");
        String rodape = formatter.formatarRodape(150);
        System.out.println(rodape);
        System.out.println();
        
        // Teste 5: Formatação de valores
        System.out.println("5. Teste de Formatação de Valores:");
        testFormatacaoValores();
        System.out.println();
        
        // Teste 6: Formatação de CPF
        System.out.println("6. Teste de Formatação de CPF:");
        testFormatacaoCPF();
        System.out.println();
        
        System.out.println("=== Testes Concluídos ===");
    }
    
    private static SiadsRegistro criarRegistroCompleto() {
        SiadsRegistro registro = new SiadsRegistro();
        
        registro.setNumeroPatrimonio("12345");
        registro.setDescricao("COMPUTADOR DESKTOP");
        registro.setEspecificacao("DELL OPTIPLEX 7090 I7 16GB 512GB SSD");
        registro.setCodigoMaterial("123456");
        registro.setGrupoMaterial("EQUIPAMENTOS DE INFORMATICA");
        registro.setClasseContabil("1.2.3.1.1.01.01");
        registro.setValorAquisicao(new BigDecimal("5500.00"));
        registro.setValorDepreciado(new BigDecimal("1100.00"));
        registro.setValorResidual(new BigDecimal("4400.00"));
        registro.setDataAquisicao(LocalDate.of(2023, 6, 15));
        registro.setDataIncorporacao(LocalDate.of(2023, 6, 20));
        registro.setDataInventario(LocalDate.of(2024, 11, 8));
        registro.setOrgao("IFMT");
        registro.setUnidadeGestora("158000");
        registro.setSetor("DIRETORIA DE TI");
        registro.setSala("SALA 101");
        registro.setCpfResponsavel("123.456.789-00");
        registro.setNomeResponsavel("JOAO DA SILVA");
        registro.setMatriculaResponsavel("123456");
        registro.setSituacaoBem("ATIVO");
        registro.setEstadoConservacao("BOM");
        registro.setFormaAquisicao("COMPRA");
        registro.setNumeroNotaFiscal("NF-12345");
        registro.setFornecedor("DELL COMPUTADORES DO BRASIL LTDA");
        
        return registro;
    }
    
    private static SiadsRegistro criarRegistroVazio() {
        SiadsRegistro registro = new SiadsRegistro();
        
        // Apenas campos obrigatórios
        registro.setNumeroPatrimonio("67890");
        registro.setDescricao("MESA DE ESCRITORIO");
        registro.setValorAquisicao(new BigDecimal("450.00"));
        registro.setDataAquisicao(LocalDate.of(2024, 1, 10));
        registro.setCpfResponsavel("987.654.321-00");
        registro.setNomeResponsavel("MARIA SANTOS");
        registro.setSituacaoBem("ATIVO");
        
        return registro;
    }
    
    private static void testFormatacaoValores() {
        SiadsLayoutFormatter formatter = new SiadsLayoutFormatter();
        
        SiadsRegistro r1 = new SiadsRegistro();
        r1.setValorAquisicao(new BigDecimal("1234.56"));
        System.out.println("Valor 1234.56 formatado: " + 
            formatter.formatarRegistro(r1).split("\\|")[7]);
        
        SiadsRegistro r2 = new SiadsRegistro();
        r2.setValorAquisicao(new BigDecimal("1000000.00"));
        System.out.println("Valor 1000000.00 formatado: " + 
            formatter.formatarRegistro(r2).split("\\|")[7]);
        
        SiadsRegistro r3 = new SiadsRegistro();
        r3.setValorAquisicao(new BigDecimal("0.50"));
        System.out.println("Valor 0.50 formatado: " + 
            formatter.formatarRegistro(r3).split("\\|")[7]);
    }
    
    private static void testFormatacaoCPF() {
        SiadsLayoutFormatter formatter = new SiadsLayoutFormatter();
        
        SiadsRegistro r1 = new SiadsRegistro();
        r1.setCpfResponsavel("123.456.789-00");
        System.out.println("CPF com pontuação: " + 
            formatter.formatarRegistro(r1).split("\\|")[17]);
        
        SiadsRegistro r2 = new SiadsRegistro();
        r2.setCpfResponsavel("12345678900");
        System.out.println("CPF sem pontuação: " + 
            formatter.formatarRegistro(r2).split("\\|")[17]);
    }
}
