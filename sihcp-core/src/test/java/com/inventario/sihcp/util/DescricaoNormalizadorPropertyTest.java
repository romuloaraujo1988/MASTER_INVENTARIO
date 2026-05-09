package com.inventario.sihcp.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Property-based tests para DescricaoNormalizador.
 * 
 * Feature: foto-referencia-descricao
 * Property 4: Normalizacao de Descricoes
 * Validates: Requirements 3.2
 * 
 * Testes baseados em propriedades que verificam:
 * - Remocao correta de padroes ANAC
 * - Remocao de numeros de serie
 * - Preservacao da descricao base
 * - Idempotencia da normalizacao
 */
@DisplayName("DescricaoNormalizador - Property Tests")
public class DescricaoNormalizadorPropertyTest {
    
    private Random random;
    
    private static final String[] PADROES_ANAC = {
        "PATRIMONIO ANAC %d",
        "- PATRIMONIO ANAC %d",
        "PATRIMONIO ANAC: %d",
        "PATRIMONIO ANAC-%d"
    };
    
    private static final String[] PADROES_SERIE = {
        "N/S: %s",
        "N/S %s",
        "SERIE: %s",
        "SERIAL: %s",
        ", N/S: %s"
    };
    
    private static final String[] DESCRICOES_BASE = {
        "CADEIRA GIRATORIA",
        "MONITOR LED 24 POLEGADAS",
        "MESA DE ESCRITORIO",
        "COMPUTADOR DESKTOP",
        "IMPRESSORA LASER",
        "AR CONDICIONADO SPLIT",
        "NOTEBOOK DELL",
        "PROJETOR MULTIMIDIA",
        "TELEFONE IP",
        "ARMARIO DE ACO"
    };

    @BeforeEach
    void setUp() {
        random = new Random();
    }
    
    // ========================================================================
    // Property 4: Normalizacao de Descricoes
    // For any descricao de patrimonio, a normalizacao deve remover numeros de
    // patrimonio ANAC, numeros de serie, e preservar a descricao base do item.
    // ========================================================================
    
    @Nested
    @DisplayName("Property 4.1: Remocao de Padrao ANAC")
    class RemocaoPadraoAnac {
        
        /**
         * Property: For any descricao com padrao ANAC, a normalizacao deve
         * remover completamente o padrao ANAC.
         */
        @RepeatedTest(value = 100, name = "Iteracao {currentRepetition}/{totalRepetitions}")
        @DisplayName("Deve remover padrao ANAC de descricoes aleatorias")
        void deveRemoverPadraoAnac() {
            // Arrange: gerar descricao com padrao ANAC aleatorio
            String descricaoBase = DESCRICOES_BASE[random.nextInt(DESCRICOES_BASE.length)];
            String padraoAnac = String.format(
                PADROES_ANAC[random.nextInt(PADROES_ANAC.length)],
                100000 + random.nextInt(900000)
            );
            String descricaoComAnac = descricaoBase + " " + padraoAnac;
            
            // Act
            String resultado = DescricaoNormalizador.normalizar(descricaoComAnac);
            
            // Assert: resultado nao deve conter "PATRIMONIO ANAC"
            assertFalse(resultado.toUpperCase().contains("PATRIMONIO ANAC"),
                "Resultado nao deve conter 'PATRIMONIO ANAC': " + resultado);
            
            // Assert: descricao base deve ser preservada
            assertTrue(resultado.contains(descricaoBase.toUpperCase()),
                "Descricao base deve ser preservada. Esperado conter: " + descricaoBase + 
                ", Obtido: " + resultado);
        }
        
        @Test
        @DisplayName("Deve remover padrao ANAC no inicio da descricao")
        void deveRemoverPadraoAnacNoInicio() {
            String descricao = "PATRIMONIO ANAC 123456 - CADEIRA GIRATORIA";
            String resultado = DescricaoNormalizador.normalizar(descricao);
            
            assertFalse(resultado.contains("PATRIMONIO ANAC"));
            assertFalse(resultado.contains("123456"));
        }
    }

    @Nested
    @DisplayName("Property 4.2: Remocao de Numero de Serie")
    class RemocaoNumeroSerie {
        
        /**
         * Property: For any descricao com numero de serie, a normalizacao deve
         * remover completamente o numero de serie.
         */
        @RepeatedTest(value = 100, name = "Iteracao {currentRepetition}/{totalRepetitions}")
        @DisplayName("Deve remover numero de serie de descricoes aleatorias")
        void deveRemoverNumeroSerie() {
            // Arrange: gerar descricao com numero de serie aleatorio
            String descricaoBase = DESCRICOES_BASE[random.nextInt(DESCRICOES_BASE.length)];
            String numeroSerie = gerarNumeroSerieAleatorio();
            String padraoSerie = String.format(
                PADROES_SERIE[random.nextInt(PADROES_SERIE.length)],
                numeroSerie
            );
            String descricaoComSerie = descricaoBase + " " + padraoSerie;
            
            // Act
            String resultado = DescricaoNormalizador.normalizar(descricaoComSerie);
            
            // Assert: resultado nao deve conter padroes de serie
            assertFalse(resultado.contains("N/S:"), "Nao deve conter 'N/S:': " + resultado);
            assertFalse(resultado.contains("SERIE:"), "Nao deve conter 'SERIE:': " + resultado);
            assertFalse(resultado.contains("SERIAL:"), "Nao deve conter 'SERIAL:': " + resultado);
            
            // Assert: descricao base deve ser preservada
            assertTrue(resultado.contains(descricaoBase.toUpperCase()),
                "Descricao base deve ser preservada. Esperado: " + descricaoBase + 
                ", Obtido: " + resultado);
        }
        
        private String gerarNumeroSerieAleatorio() {
            StringBuilder sb = new StringBuilder();
            int tamanho = 5 + random.nextInt(10);
            for (int i = 0; i < tamanho; i++) {
                if (random.nextBoolean()) {
                    sb.append((char) ('A' + random.nextInt(26)));
                } else {
                    sb.append(random.nextInt(10));
                }
            }
            return sb.toString();
        }
    }

    @Nested
    @DisplayName("Property 4.3: Preservacao da Descricao Base")
    class PreservacaoDescricaoBase {
        
        /**
         * Property: For any descricao base valida, a normalizacao deve preservar
         * a descricao base mesmo quando combinada com padroes removiveis.
         */
        @RepeatedTest(value = 100, name = "Iteracao {currentRepetition}/{totalRepetitions}")
        @DisplayName("Deve preservar descricao base apos normalizacao")
        void devePreservarDescricaoBase() {
            // Arrange: gerar descricao complexa com multiplos padroes
            String descricaoBase = DESCRICOES_BASE[random.nextInt(DESCRICOES_BASE.length)];
            String descricaoCompleta = gerarDescricaoCompleta(descricaoBase);
            
            // Act
            String resultado = DescricaoNormalizador.normalizar(descricaoCompleta);
            
            // Assert: descricao base deve estar presente no resultado
            assertTrue(resultado.contains(descricaoBase.toUpperCase()),
                "Descricao base '" + descricaoBase + "' deve ser preservada. " +
                "Input: " + descricaoCompleta + ", Output: " + resultado);
        }
        
        private String gerarDescricaoCompleta(String base) {
            StringBuilder sb = new StringBuilder(base);
            
            // Adicionar padrao ANAC aleatoriamente
            if (random.nextBoolean()) {
                sb.append(" ");
                sb.append(String.format(
                    PADROES_ANAC[random.nextInt(PADROES_ANAC.length)],
                    100000 + random.nextInt(900000)
                ));
            }
            
            // Adicionar numero de serie aleatoriamente
            if (random.nextBoolean()) {
                sb.append(" ");
                sb.append(String.format(
                    PADROES_SERIE[random.nextInt(PADROES_SERIE.length)],
                    "ABC" + random.nextInt(10000)
                ));
            }
            
            // Adicionar numero final aleatoriamente
            if (random.nextBoolean()) {
                sb.append(" - ");
                sb.append(10000 + random.nextInt(90000));
            }
            
            return sb.toString();
        }
    }

    @Nested
    @DisplayName("Property 4.4: Idempotencia")
    class Idempotencia {
        
        /**
         * Property: For any descricao, normalizar(normalizar(x)) == normalizar(x)
         * A normalizacao deve ser idempotente.
         */
        @RepeatedTest(value = 100, name = "Iteracao {currentRepetition}/{totalRepetitions}")
        @DisplayName("Normalizacao deve ser idempotente")
        void normalizacaoDeveSerIdempotente() {
            // Arrange: gerar descricao aleatoria
            String descricaoOriginal = gerarDescricaoAleatoria();
            
            // Act
            String primeiraPassagem = DescricaoNormalizador.normalizar(descricaoOriginal);
            String segundaPassagem = DescricaoNormalizador.normalizar(primeiraPassagem);
            
            // Assert: resultado deve ser igual apos multiplas normalizacoes
            assertEquals(primeiraPassagem, segundaPassagem,
                "Normalizacao deve ser idempotente. " +
                "Original: " + descricaoOriginal + 
                ", 1a passagem: " + primeiraPassagem + 
                ", 2a passagem: " + segundaPassagem);
        }
        
        private String gerarDescricaoAleatoria() {
            String base = DESCRICOES_BASE[random.nextInt(DESCRICOES_BASE.length)];
            StringBuilder sb = new StringBuilder(base);
            
            // Adicionar elementos aleatorios
            int numElementos = random.nextInt(3);
            for (int i = 0; i < numElementos; i++) {
                int tipo = random.nextInt(3);
                if (tipo == 0) {
                    sb.append(" PATRIMONIO ANAC ");
                    sb.append(100000 + random.nextInt(900000));
                } else if (tipo == 1) {
                    sb.append(" N/S: ABC");
                    sb.append(random.nextInt(10000));
                } else {
                    sb.append(" - ");
                    sb.append(10000 + random.nextInt(90000));
                }
            }
            
            return sb.toString();
        }
    }

    @Nested
    @DisplayName("Property 4.5: Equivalencia de Descricoes")
    class EquivalenciaDescricoes {
        
        /**
         * Property: For any duas descricoes com mesma base mas diferentes
         * numeros ANAC/serie, elas devem ser equivalentes apos normalizacao.
         */
        @RepeatedTest(value = 100, name = "Iteracao {currentRepetition}/{totalRepetitions}")
        @DisplayName("Descricoes com mesma base devem ser equivalentes")
        void descricoesComMesmaBaseDevemSerEquivalentes() {
            // Arrange: gerar duas descricoes com mesma base
            String descricaoBase = DESCRICOES_BASE[random.nextInt(DESCRICOES_BASE.length)];
            
            String descricao1 = descricaoBase + " PATRIMONIO ANAC " + (100000 + random.nextInt(900000));
            String descricao2 = descricaoBase + " PATRIMONIO ANAC " + (100000 + random.nextInt(900000));
            
            // Act
            boolean saoEquivalentes = DescricaoNormalizador.saoEquivalentes(descricao1, descricao2);
            
            // Assert
            assertTrue(saoEquivalentes,
                "Descricoes com mesma base devem ser equivalentes. " +
                "Desc1: " + descricao1 + ", Desc2: " + descricao2);
        }
        
        /**
         * Property: For any duas descricoes com bases diferentes,
         * elas NAO devem ser equivalentes apos normalizacao.
         */
        @RepeatedTest(value = 100, name = "Iteracao {currentRepetition}/{totalRepetitions}")
        @DisplayName("Descricoes com bases diferentes nao devem ser equivalentes")
        void descricoesComBasesDiferentesNaoDevemSerEquivalentes() {
            // Arrange: gerar duas descricoes com bases diferentes
            int idx1 = random.nextInt(DESCRICOES_BASE.length);
            int idx2 = (idx1 + 1 + random.nextInt(DESCRICOES_BASE.length - 1)) % DESCRICOES_BASE.length;
            
            String descricao1 = DESCRICOES_BASE[idx1] + " PATRIMONIO ANAC " + random.nextInt(900000);
            String descricao2 = DESCRICOES_BASE[idx2] + " PATRIMONIO ANAC " + random.nextInt(900000);
            
            // Act
            boolean saoEquivalentes = DescricaoNormalizador.saoEquivalentes(descricao1, descricao2);
            
            // Assert
            assertFalse(saoEquivalentes,
                "Descricoes com bases diferentes nao devem ser equivalentes. " +
                "Desc1: " + descricao1 + ", Desc2: " + descricao2);
        }
    }

    @Nested
    @DisplayName("Property 4.6: Tratamento de Entradas Especiais")
    class TratamentoEntradasEspeciais {
        
        @Test
        @DisplayName("Deve retornar string vazia para null")
        void deveRetornarVazioParaNull() {
            assertEquals("", DescricaoNormalizador.normalizar(null));
        }
        
        @Test
        @DisplayName("Deve retornar string vazia para string vazia")
        void deveRetornarVazioParaStringVazia() {
            assertEquals("", DescricaoNormalizador.normalizar(""));
        }
        
        @Test
        @DisplayName("Deve retornar string vazia para string com apenas espacos")
        void deveRetornarVazioParaApenasEspacos() {
            assertEquals("", DescricaoNormalizador.normalizar("   "));
        }
        
        /**
         * Property: For any descricao nao-nula e nao-vazia, o resultado
         * da normalizacao deve ser nao-nulo.
         */
        @RepeatedTest(value = 100, name = "Iteracao {currentRepetition}/{totalRepetitions}")
        @DisplayName("Resultado nunca deve ser nulo para entrada valida")
        void resultadoNuncaDeveSerNulo() {
            String descricao = DESCRICOES_BASE[random.nextInt(DESCRICOES_BASE.length)];
            String resultado = DescricaoNormalizador.normalizar(descricao);
            
            assertNotNull(resultado, "Resultado nao deve ser nulo");
        }
    }

    @Nested
    @DisplayName("Property 4.7: Extracao de Numero ANAC")
    class ExtracaoNumeroAnac {
        
        /**
         * Property: For any descricao com numero ANAC valido, a extracao
         * deve retornar exatamente o numero inserido.
         */
        @RepeatedTest(value = 100, name = "Iteracao {currentRepetition}/{totalRepetitions}")
        @DisplayName("Deve extrair numero ANAC corretamente")
        void deveExtrairNumeroAnacCorretamente() {
            // Arrange
            String descricaoBase = DESCRICOES_BASE[random.nextInt(DESCRICOES_BASE.length)];
            int numeroAnac = 100000 + random.nextInt(900000);
            String descricao = descricaoBase + " PATRIMONIO ANAC " + numeroAnac;
            
            // Act
            String extraido = DescricaoNormalizador.extrairNumeroAnac(descricao);
            
            // Assert
            assertNotNull(extraido, "Numero ANAC deve ser extraido");
            assertEquals(String.valueOf(numeroAnac), extraido,
                "Numero extraido deve ser igual ao inserido");
        }
    }
}
