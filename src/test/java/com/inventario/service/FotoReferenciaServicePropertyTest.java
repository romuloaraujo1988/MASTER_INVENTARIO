package com.inventario.service;

import com.inventario.dao.FotoReferenciaDAO;
import com.inventario.model.FotoReferencia;
import com.inventario.util.ImageProcessor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Property-based tests para FotoReferenciaService.
 * 
 * Feature: foto-referencia-descricao
 * Property 3: Completude de Metadados
 * Validates: Requirements 2.2, 4.4
 * 
 * Testes baseados em propriedades que verificam:
 * - Presença de todos os metadados obrigatórios após salvar
 * - Descrição normalizada não nula e não vazia
 * - Hash da imagem presente
 * - Data de cadastro presente
 * - Tamanho em bytes maior que zero
 */
@DisplayName("FotoReferenciaService - Property Tests")
public class FotoReferenciaServicePropertyTest {
    
    private FotoReferenciaService service;
    private FotoReferenciaDAO mockDAO;
    private ImageProcessor imageProcessor;
    private Random random;
    
    private static final String[] DESCRICOES_TESTE = {
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
    
    private static final String[] USUARIOS_TESTE = {
        "admin",
        "usuario1",
        "operador",
        "sistema"
    };

    @BeforeEach
    void setUp() {
        mockDAO = mock(FotoReferenciaDAO.class);
        imageProcessor = new ImageProcessor();
        service = new FotoReferenciaService(mockDAO, imageProcessor);
        random = new Random();
    }
    
    // ========================================================================
    // Property 3: Completude de Metadados
    // For any foto de referência salva no sistema, todos os metadados 
    // obrigatórios devem estar presentes: descrição normalizada, hash da 
    // imagem, data de cadastro, tamanho em bytes.
    // ========================================================================
    
    @Nested
    @DisplayName("Property 3.1: Descrição Normalizada Presente")
    class DescricaoNormalizadaPresente {
        
        /**
         * Property: For any foto salva, a descrição normalizada deve estar
         * presente e não vazia.
         */
        @RepeatedTest(value = 100, name = "Iteração {currentRepetition}/{totalRepetitions}")
        @DisplayName("Descrição normalizada deve estar presente após salvar")
        void descricaoNormalizadaDeveEstarPresente() throws Exception {
            // Arrange
            String descricaoOriginal = gerarDescricaoAleatoria();
            byte[] imagemValida = gerarImagemAleatoriaValida();
            String usuario = USUARIOS_TESTE[random.nextInt(USUARIOS_TESTE.length)];
            
            configurarMockParaSalvar();
            
            // Act
            FotoReferencia resultado = service.salvarFotoReferencia(descricaoOriginal, imagemValida, usuario);
            
            // Assert
            assertNotNull(resultado.getDescricaoNormalizada(),
                "Descrição normalizada não deve ser nula");
            assertFalse(resultado.getDescricaoNormalizada().trim().isEmpty(),
                "Descrição normalizada não deve ser vazia");
        }
        
        /**
         * Property: For any descrição com padrões ANAC, a normalização deve
         * remover os padrões e preservar a descrição base.
         */
        @RepeatedTest(value = 100, name = "Iteração {currentRepetition}/{totalRepetitions}")
        @DisplayName("Descrição deve ser normalizada corretamente")
        void descricaoDeveSerNormalizada() throws Exception {
            // Arrange
            String descricaoBase = DESCRICOES_TESTE[random.nextInt(DESCRICOES_TESTE.length)];
            String descricaoComAnac = descricaoBase + " PATRIMONIO ANAC " + (100000 + random.nextInt(900000));
            byte[] imagemValida = gerarImagemAleatoriaValida();
            
            configurarMockParaSalvar();
            
            // Act
            FotoReferencia resultado = service.salvarFotoReferencia(descricaoComAnac, imagemValida, "admin");
            
            // Assert
            assertFalse(resultado.getDescricaoNormalizada().contains("PATRIMONIO ANAC"),
                "Descrição normalizada não deve conter 'PATRIMONIO ANAC'");
            assertTrue(resultado.getDescricaoNormalizada().contains(descricaoBase.toUpperCase()),
                "Descrição base deve ser preservada");
        }
    }
    
    @Nested
    @DisplayName("Property 3.2: Hash da Imagem Presente")
    class HashImagemPresente {
        
        /**
         * Property: For any foto salva, o hash da imagem deve estar presente
         * e ter formato válido (64 caracteres hexadecimais para SHA-256).
         */
        @RepeatedTest(value = 100, name = "Iteração {currentRepetition}/{totalRepetitions}")
        @DisplayName("Hash da imagem deve estar presente e válido")
        void hashImagemDeveEstarPresente() throws Exception {
            // Arrange
            String descricao = gerarDescricaoAleatoria();
            byte[] imagemValida = gerarImagemAleatoriaValida();
            
            configurarMockParaSalvar();
            
            // Act
            FotoReferencia resultado = service.salvarFotoReferencia(descricao, imagemValida, "admin");
            
            // Assert
            assertNotNull(resultado.getHashImagem(),
                "Hash da imagem não deve ser nulo");
            assertFalse(resultado.getHashImagem().trim().isEmpty(),
                "Hash da imagem não deve ser vazio");
            assertEquals(64, resultado.getHashImagem().length(),
                "Hash SHA-256 deve ter 64 caracteres");
            assertTrue(resultado.getHashImagem().matches("[a-f0-9]+"),
                "Hash deve conter apenas caracteres hexadecimais");
        }
        
        /**
         * Property: For any duas imagens diferentes, os hashes devem ser diferentes.
         */
        @RepeatedTest(value = 50, name = "Iteração {currentRepetition}/{totalRepetitions}")
        @DisplayName("Imagens diferentes devem ter hashes diferentes")
        void imagensDiferentesDevemTerHashesDiferentes() throws Exception {
            // Arrange
            String descricao1 = "CADEIRA GIRATORIA";
            String descricao2 = "MESA DE ESCRITORIO";
            byte[] imagem1 = gerarImagemAleatoriaValida();
            byte[] imagem2 = gerarImagemAleatoriaValida();
            
            configurarMockParaSalvar();
            
            // Act
            FotoReferencia resultado1 = service.salvarFotoReferencia(descricao1, imagem1, "admin");
            FotoReferencia resultado2 = service.salvarFotoReferencia(descricao2, imagem2, "admin");
            
            // Assert - hashes devem ser diferentes (com alta probabilidade)
            // Nota: há uma chance infinitesimal de colisão, mas é aceitável para testes
            assertNotEquals(resultado1.getHashImagem(), resultado2.getHashImagem(),
                "Imagens diferentes devem ter hashes diferentes");
        }
    }
    
    @Nested
    @DisplayName("Property 3.3: Data de Cadastro Presente")
    class DataCadastroPresente {
        
        /**
         * Property: For any foto salva, a data de cadastro deve estar presente.
         */
        @RepeatedTest(value = 100, name = "Iteração {currentRepetition}/{totalRepetitions}")
        @DisplayName("Data de cadastro deve estar presente")
        void dataCadastroDeveEstarPresente() throws Exception {
            // Arrange
            String descricao = gerarDescricaoAleatoria();
            byte[] imagemValida = gerarImagemAleatoriaValida();
            
            configurarMockParaSalvar();
            
            // Act
            FotoReferencia resultado = service.salvarFotoReferencia(descricao, imagemValida, "admin");
            
            // Assert
            assertNotNull(resultado.getDataCadastro(),
                "Data de cadastro não deve ser nula");
        }
        
        /**
         * Property: For any foto salva, a data de cadastro deve ser recente
         * (dentro dos últimos 5 segundos).
         */
        @RepeatedTest(value = 50, name = "Iteração {currentRepetition}/{totalRepetitions}")
        @DisplayName("Data de cadastro deve ser recente")
        void dataCadastroDeveSerRecente() throws Exception {
            // Arrange
            String descricao = gerarDescricaoAleatoria();
            byte[] imagemValida = gerarImagemAleatoriaValida();
            long antesDoSalvar = System.currentTimeMillis();
            
            configurarMockParaSalvar();
            
            // Act
            FotoReferencia resultado = service.salvarFotoReferencia(descricao, imagemValida, "admin");
            long depoisDoSalvar = System.currentTimeMillis();
            
            // Assert
            long timestampCadastro = resultado.getDataCadastro().getTime();
            assertTrue(timestampCadastro >= antesDoSalvar - 1000,
                "Data de cadastro deve ser após o início do teste");
            assertTrue(timestampCadastro <= depoisDoSalvar + 1000,
                "Data de cadastro deve ser antes do fim do teste");
        }
    }
    
    @Nested
    @DisplayName("Property 3.4: Tamanho em Bytes Presente")
    class TamanhoBytesPresente {
        
        /**
         * Property: For any foto salva, o tamanho em bytes deve ser maior que zero.
         */
        @RepeatedTest(value = 100, name = "Iteração {currentRepetition}/{totalRepetitions}")
        @DisplayName("Tamanho em bytes deve ser maior que zero")
        void tamanhoBytesDeveSerMaiorQueZero() throws Exception {
            // Arrange
            String descricao = gerarDescricaoAleatoria();
            byte[] imagemValida = gerarImagemAleatoriaValida();
            
            configurarMockParaSalvar();
            
            // Act
            FotoReferencia resultado = service.salvarFotoReferencia(descricao, imagemValida, "admin");
            
            // Assert
            assertTrue(resultado.getTamanhoBytes() > 0,
                "Tamanho em bytes deve ser maior que zero");
        }
        
        /**
         * Property: For any foto salva, o tamanho em bytes deve ser <= 50KB.
         */
        @RepeatedTest(value = 100, name = "Iteração {currentRepetition}/{totalRepetitions}")
        @DisplayName("Tamanho em bytes deve ser <= 50KB")
        void tamanhoBytesDeveSerMenorOuIgualA50KB() throws Exception {
            // Arrange
            String descricao = gerarDescricaoAleatoria();
            byte[] imagemValida = gerarImagemAleatoriaValida();
            
            configurarMockParaSalvar();
            
            // Act
            FotoReferencia resultado = service.salvarFotoReferencia(descricao, imagemValida, "admin");
            
            // Assert
            assertTrue(resultado.getTamanhoBytes() <= 51200,
                "Tamanho em bytes deve ser <= 50KB (51200 bytes). Atual: " + resultado.getTamanhoBytes());
        }
        
        /**
         * Property: For any foto salva, o tamanho em bytes deve corresponder
         * ao tamanho real do blob da imagem.
         */
        @RepeatedTest(value = 100, name = "Iteração {currentRepetition}/{totalRepetitions}")
        @DisplayName("Tamanho em bytes deve corresponder ao blob")
        void tamanhoBytesDeveCorresponderAoBlob() throws Exception {
            // Arrange
            String descricao = gerarDescricaoAleatoria();
            byte[] imagemValida = gerarImagemAleatoriaValida();
            
            configurarMockParaSalvar();
            
            // Act
            FotoReferencia resultado = service.salvarFotoReferencia(descricao, imagemValida, "admin");
            
            // Assert
            assertEquals(resultado.getImagemBlob().length, resultado.getTamanhoBytes(),
                "Tamanho em bytes deve corresponder ao tamanho real do blob");
        }
    }
    
    @Nested
    @DisplayName("Property 3.5: Completude Geral de Metadados")
    class CompletudeGeralMetadados {
        
        /**
         * Property: For any foto salva, o método metadadosCompletos() deve
         * retornar true.
         */
        @RepeatedTest(value = 100, name = "Iteração {currentRepetition}/{totalRepetitions}")
        @DisplayName("Método metadadosCompletos() deve retornar true")
        void metadadosCompletosDeveRetornarTrue() throws Exception {
            // Arrange
            String descricao = gerarDescricaoAleatoria();
            byte[] imagemValida = gerarImagemAleatoriaValida();
            
            configurarMockParaSalvar();
            
            // Act
            FotoReferencia resultado = service.salvarFotoReferencia(descricao, imagemValida, "admin");
            
            // Assert
            assertTrue(resultado.metadadosCompletos(),
                "metadadosCompletos() deve retornar true para foto salva corretamente. " +
                "Descrição: " + resultado.getDescricaoNormalizada() + ", " +
                "Hash: " + resultado.getHashImagem() + ", " +
                "DataCadastro: " + resultado.getDataCadastro() + ", " +
                "Tamanho: " + resultado.getTamanhoBytes());
        }
        
        /**
         * Property: For any foto salva com diferentes combinações de entrada,
         * todos os metadados obrigatórios devem estar presentes.
         */
        @RepeatedTest(value = 100, name = "Iteração {currentRepetition}/{totalRepetitions}")
        @DisplayName("Todos os metadados obrigatórios devem estar presentes")
        void todosMetadadosObrigatoriosDevemEstarPresentes() throws Exception {
            // Arrange
            String descricao = gerarDescricaoComVariacoes();
            byte[] imagemValida = gerarImagemComDimensoesAleatorias();
            String usuario = USUARIOS_TESTE[random.nextInt(USUARIOS_TESTE.length)];
            
            configurarMockParaSalvar();
            
            // Act
            FotoReferencia resultado = service.salvarFotoReferencia(descricao, imagemValida, usuario);
            
            // Assert - verificar cada metadado individualmente
            assertAll("Todos os metadados obrigatórios",
                () -> assertNotNull(resultado.getDescricaoNormalizada(), "Descrição normalizada"),
                () -> assertFalse(resultado.getDescricaoNormalizada().isEmpty(), "Descrição não vazia"),
                () -> assertNotNull(resultado.getHashImagem(), "Hash da imagem"),
                () -> assertFalse(resultado.getHashImagem().isEmpty(), "Hash não vazio"),
                () -> assertNotNull(resultado.getDataCadastro(), "Data de cadastro"),
                () -> assertTrue(resultado.getTamanhoBytes() > 0, "Tamanho > 0")
            );
        }
    }
    
    @Nested
    @DisplayName("Property 3.6: Imagem Blob Presente")
    class ImagemBlobPresente {
        
        /**
         * Property: For any foto salva, o blob da imagem deve estar presente.
         */
        @RepeatedTest(value = 100, name = "Iteração {currentRepetition}/{totalRepetitions}")
        @DisplayName("Blob da imagem deve estar presente")
        void blobImagemDeveEstarPresente() throws Exception {
            // Arrange
            String descricao = gerarDescricaoAleatoria();
            byte[] imagemValida = gerarImagemAleatoriaValida();
            
            configurarMockParaSalvar();
            
            // Act
            FotoReferencia resultado = service.salvarFotoReferencia(descricao, imagemValida, "admin");
            
            // Assert
            assertNotNull(resultado.getImagemBlob(),
                "Blob da imagem não deve ser nulo");
            assertTrue(resultado.getImagemBlob().length > 0,
                "Blob da imagem não deve ser vazio");
        }
        
        /**
         * Property: For any foto salva, o blob deve ser uma imagem JPEG válida.
         */
        @RepeatedTest(value = 50, name = "Iteração {currentRepetition}/{totalRepetitions}")
        @DisplayName("Blob deve ser JPEG válido")
        void blobDeveSerJpegValido() throws Exception {
            // Arrange
            String descricao = gerarDescricaoAleatoria();
            byte[] imagemValida = gerarImagemAleatoriaValida();
            
            configurarMockParaSalvar();
            
            // Act
            FotoReferencia resultado = service.salvarFotoReferencia(descricao, imagemValida, "admin");
            
            // Assert - verificar magic bytes de JPEG (FF D8 FF)
            byte[] blob = resultado.getImagemBlob();
            assertTrue(blob.length >= 3, "Blob deve ter pelo menos 3 bytes");
            assertEquals((byte) 0xFF, blob[0], "Primeiro byte deve ser 0xFF (JPEG)");
            assertEquals((byte) 0xD8, blob[1], "Segundo byte deve ser 0xD8 (JPEG)");
            assertEquals((byte) 0xFF, blob[2], "Terceiro byte deve ser 0xFF (JPEG)");
        }
    }
    
    // ========== Métodos auxiliares ==========
    
    private void configurarMockParaSalvar() throws SQLException {
        doAnswer(invocation -> {
            FotoReferencia foto = invocation.getArgument(0);
            foto.setId(random.nextInt(10000) + 1);
            return null;
        }).when(mockDAO).salvarOuAtualizar(any(FotoReferencia.class));
        
        when(mockDAO.buscarPorDescricao(anyString())).thenReturn(null);
    }
    
    private String gerarDescricaoAleatoria() {
        String base = DESCRICOES_TESTE[random.nextInt(DESCRICOES_TESTE.length)];
        
        // Adicionar variações aleatórias
        if (random.nextBoolean()) {
            base += " PATRIMONIO ANAC " + (100000 + random.nextInt(900000));
        }
        if (random.nextBoolean()) {
            base += " N/S: ABC" + random.nextInt(10000);
        }
        
        return base;
    }
    
    private String gerarDescricaoComVariacoes() {
        String base = DESCRICOES_TESTE[random.nextInt(DESCRICOES_TESTE.length)];
        StringBuilder sb = new StringBuilder(base);
        
        // Adicionar padrão ANAC
        if (random.nextBoolean()) {
            String[] padroes = {
                " PATRIMONIO ANAC %d",
                " - PATRIMONIO ANAC %d",
                " PATRIMONIO ANAC: %d"
            };
            sb.append(String.format(padroes[random.nextInt(padroes.length)], 
                100000 + random.nextInt(900000)));
        }
        
        // Adicionar número de série
        if (random.nextBoolean()) {
            String[] padroes = {" N/S: %s", " SERIE: %s", " SERIAL: %s"};
            sb.append(String.format(padroes[random.nextInt(padroes.length)], 
                "ABC" + random.nextInt(10000)));
        }
        
        // Adicionar número final
        if (random.nextBoolean()) {
            sb.append(" - ").append(10000 + random.nextInt(90000));
        }
        
        return sb.toString();
    }
    
    private byte[] gerarImagemAleatoriaValida() throws IOException {
        // Gerar imagem com dimensões aleatórias (50-500 pixels)
        int width = 50 + random.nextInt(450);
        int height = 50 + random.nextInt(450);
        
        return gerarImagemJpeg(width, height);
    }
    
    private byte[] gerarImagemComDimensoesAleatorias() throws IOException {
        // Gerar imagem com dimensões variadas para testar redimensionamento
        int[] dimensoes = {50, 100, 200, 300, 500, 800, 1000};
        int width = dimensoes[random.nextInt(dimensoes.length)];
        int height = dimensoes[random.nextInt(dimensoes.length)];
        
        return gerarImagemJpeg(width, height);
    }
    
    private byte[] gerarImagemJpeg(int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // Preencher com cor aleatória
        g2d.setColor(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
        g2d.fillRect(0, 0, width, height);
        
        // Adicionar alguns elementos aleatórios para variar o conteúdo
        for (int i = 0; i < 5 + random.nextInt(10); i++) {
            g2d.setColor(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int w = 10 + random.nextInt(50);
            int h = 10 + random.nextInt(50);
            
            if (random.nextBoolean()) {
                g2d.fillRect(x, y, w, h);
            } else {
                g2d.fillOval(x, y, w, h);
            }
        }
        
        g2d.dispose();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return baos.toByteArray();
    }
}
