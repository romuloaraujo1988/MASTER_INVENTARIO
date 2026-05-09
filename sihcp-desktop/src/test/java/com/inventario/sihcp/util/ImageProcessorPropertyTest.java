package com.inventario.sihcp.util;

import org.junit.jupiter.api.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests para ImageProcessor.
 * 
 * **Feature: foto-referencia-descricao**
 * **Property 1: Validação de Entrada de Imagem**
 * **Property 2: Processamento de Thumbnail**
 * **Validates: Requirements 1.2, 1.3, 1.6, 2.4**
 */
@DisplayName("ImageProcessor - Property Tests")
class ImageProcessorPropertyTest {
    
    private ImageProcessor processor;
    private Random random;
    
    @BeforeEach
    void setUp() {
        processor = new ImageProcessor();
        random = new Random();
    }
    
    // ========== Property 1: Validação de Entrada de Imagem ==========
    
    @Nested
    @DisplayName("Property 1: Validação de Entrada de Imagem")
    class ValidacaoEntradaImagem {
        
        @RepeatedTest(value = 100, name = "Iteração {currentRepetition}/{totalRepetitions}")
        @DisplayName("Deve rejeitar arquivos que não são imagens")
        void deveRejeitarArquivosNaoImagem() {
            // Gerar bytes aleatórios que não são imagem
            byte[] dadosAleatorios = gerarBytesAleatorios(1024, 50000);
            
            // Garantir que não começa com magic bytes de imagem
            if (dadosAleatorios.length >= 4) {
                dadosAleatorios[0] = 0x00;
                dadosAleatorios[1] = 0x00;
                dadosAleatorios[2] = 0x00;
                dadosAleatorios[3] = 0x00;
            }
            
            boolean resultado = processor.isImagemValida(dadosAleatorios);
            
            assertFalse(resultado, "Bytes aleatórios não devem ser considerados imagem válida");
        }
        
        @RepeatedTest(value = 100, name = "Iteração {currentRepetition}/{totalRepetitions}")
        @DisplayName("Deve rejeitar arquivos maiores que 2MB")
        void deveRejeitarArquivosMaioresQue2MB() throws IOException {
            // Gerar imagem válida mas muito grande (> 2MB)
            int largura = 2000 + random.nextInt(1000);
            int altura = 2000 + random.nextInt(1000);
            BufferedImage imagemGrande = gerarImagemAleatoria(largura, altura);
            
            // Converter para bytes PNG (sem compressão significativa)
            byte[] bytesOriginal = imagemParaBytes(imagemGrande, "png");
            
            // Criar array maior que 2MB
            final byte[] bytesGrandes;
            if (bytesOriginal.length <= ImageProcessor.MAX_INPUT_SIZE_BYTES) {
                bytesGrandes = new byte[(int) ImageProcessor.MAX_INPUT_SIZE_BYTES + 1];
                // Copiar magic bytes de PNG para parecer válido
                bytesGrandes[0] = (byte) 0x89;
                bytesGrandes[1] = (byte) 0x50;
                bytesGrandes[2] = (byte) 0x4E;
                bytesGrandes[3] = (byte) 0x47;
            } else {
                bytesGrandes = bytesOriginal;
            }
            
            // Deve lançar exceção ao processar
            assertThrows(ImageProcessor.ImageProcessingException.class, () -> {
                processor.processarParaThumbnail(bytesGrandes);
            }, "Arquivos > 2MB devem ser rejeitados");
        }
        
        @RepeatedTest(value = 100, name = "Iteração {currentRepetition}/{totalRepetitions}")
        @DisplayName("Deve aceitar imagens JPEG válidas")
        void deveAceitarImagensJPEGValidas() throws IOException {
            int largura = 50 + random.nextInt(500);
            int altura = 50 + random.nextInt(500);
            BufferedImage imagem = gerarImagemAleatoria(largura, altura);
            byte[] bytes = imagemParaBytes(imagem, "jpg");
            
            boolean resultado = processor.isImagemValida(bytes);
            
            assertTrue(resultado, "Imagens JPEG válidas devem ser aceitas");
        }
        
        @RepeatedTest(value = 100, name = "Iteração {currentRepetition}/{totalRepetitions}")
        @DisplayName("Deve aceitar imagens PNG válidas")
        void deveAceitarImagensPNGValidas() throws IOException {
            int largura = 50 + random.nextInt(500);
            int altura = 50 + random.nextInt(500);
            BufferedImage imagem = gerarImagemAleatoria(largura, altura);
            byte[] bytes = imagemParaBytes(imagem, "png");
            
            boolean resultado = processor.isImagemValida(bytes);
            
            assertTrue(resultado, "Imagens PNG válidas devem ser aceitas");
        }
        
        @Test
        @DisplayName("Deve rejeitar array nulo")
        void deveRejeitarArrayNulo() {
            assertFalse(processor.isImagemValida(null));
        }
        
        @Test
        @DisplayName("Deve rejeitar array vazio")
        void deveRejeitarArrayVazio() {
            assertFalse(processor.isImagemValida(new byte[0]));
        }
        
        @RepeatedTest(value = 50, name = "Iteração {currentRepetition}/{totalRepetitions}")
        @DisplayName("Deve rejeitar arrays muito pequenos")
        void deveRejeitarArraysMuitoPequenos() {
            int tamanho = random.nextInt(8); // 0 a 7 bytes
            byte[] dados = new byte[tamanho];
            random.nextBytes(dados);
            
            assertFalse(processor.isImagemValida(dados), 
                "Arrays com menos de 8 bytes não podem ser imagens válidas");
        }
    }
    
    // ========== Property 2: Processamento de Thumbnail ==========
    
    @Nested
    @DisplayName("Property 2: Processamento de Thumbnail")
    class ProcessamentoThumbnail {
        
        @RepeatedTest(value = 100, name = "Iteração {currentRepetition}/{totalRepetitions}")
        @DisplayName("Thumbnail deve ter dimensões <= 200x200")
        void thumbnailDeveTerDimensoesCorretas() throws Exception {
            // Gerar imagem com dimensões aleatórias
            int largura = 50 + random.nextInt(1000);
            int altura = 50 + random.nextInt(1000);
            BufferedImage imagem = gerarImagemAleatoria(largura, altura);
            byte[] bytesOriginal = imagemParaBytes(imagem, "jpg");
            
            // Processar para thumbnail
            byte[] thumbnail = processor.processarParaThumbnail(bytesOriginal);
            
            // Verificar dimensões
            BufferedImage resultado = bytesParaImagem(thumbnail);
            assertNotNull(resultado, "Thumbnail não deve ser nulo");
            assertTrue(resultado.getWidth() <= ImageProcessor.MAX_WIDTH, 
                String.format("Largura %d deve ser <= %d", resultado.getWidth(), ImageProcessor.MAX_WIDTH));
            assertTrue(resultado.getHeight() <= ImageProcessor.MAX_HEIGHT, 
                String.format("Altura %d deve ser <= %d", resultado.getHeight(), ImageProcessor.MAX_HEIGHT));
        }
        
        @RepeatedTest(value = 100, name = "Iteração {currentRepetition}/{totalRepetitions}")
        @DisplayName("Thumbnail deve ter tamanho <= 50KB")
        void thumbnailDeveTerTamanhoCorreto() throws Exception {
            // Gerar imagem com dimensões aleatórias
            int largura = 100 + random.nextInt(800);
            int altura = 100 + random.nextInt(800);
            BufferedImage imagem = gerarImagemAleatoria(largura, altura);
            byte[] bytesOriginal = imagemParaBytes(imagem, "jpg");
            
            // Processar para thumbnail
            byte[] thumbnail = processor.processarParaThumbnail(bytesOriginal);
            
            // Verificar tamanho
            assertTrue(thumbnail.length <= ImageProcessor.MAX_SIZE_BYTES, 
                String.format("Tamanho %d bytes deve ser <= %d bytes (50KB)", 
                    thumbnail.length, ImageProcessor.MAX_SIZE_BYTES));
        }
        
        @RepeatedTest(value = 50, name = "Iteração {currentRepetition}/{totalRepetitions}")
        @DisplayName("Thumbnail deve manter proporção da imagem original")
        void thumbnailDeveManterProporcao() throws Exception {
            // Gerar imagem com proporção aleatória
            int largura = 100 + random.nextInt(500);
            int altura = 100 + random.nextInt(500);
            BufferedImage imagem = gerarImagemAleatoria(largura, altura);
            byte[] bytesOriginal = imagemParaBytes(imagem, "jpg");
            
            double proporcaoOriginal = (double) largura / altura;
            
            // Processar para thumbnail
            byte[] thumbnail = processor.processarParaThumbnail(bytesOriginal);
            BufferedImage resultado = bytesParaImagem(thumbnail);
            
            double proporcaoResultado = (double) resultado.getWidth() / resultado.getHeight();
            
            // Tolerância de 5% para arredondamentos
            double diferenca = Math.abs(proporcaoOriginal - proporcaoResultado);
            double tolerancia = proporcaoOriginal * 0.05;
            
            assertTrue(diferenca <= tolerancia, 
                String.format("Proporção deve ser mantida. Original: %.2f, Resultado: %.2f", 
                    proporcaoOriginal, proporcaoResultado));
        }
        
        @RepeatedTest(value = 50, name = "Iteração {currentRepetition}/{totalRepetitions}")
        @DisplayName("Imagens pequenas não devem ser ampliadas")
        void imagensPequenasNaoDevemSerAmpliadas() throws Exception {
            // Gerar imagem menor que 200x200
            int largura = 10 + random.nextInt(190);
            int altura = 10 + random.nextInt(190);
            BufferedImage imagem = gerarImagemAleatoria(largura, altura);
            byte[] bytesOriginal = imagemParaBytes(imagem, "jpg");
            
            // Processar para thumbnail
            byte[] thumbnail = processor.processarParaThumbnail(bytesOriginal);
            BufferedImage resultado = bytesParaImagem(thumbnail);
            
            // Dimensões não devem aumentar
            assertTrue(resultado.getWidth() <= largura, 
                "Largura não deve aumentar para imagens pequenas");
            assertTrue(resultado.getHeight() <= altura, 
                "Altura não deve aumentar para imagens pequenas");
        }
        
        @RepeatedTest(value = 100, name = "Iteração {currentRepetition}/{totalRepetitions}")
        @DisplayName("Thumbnail deve ser JPEG válido")
        void thumbnailDeveSerJPEGValido() throws Exception {
            int largura = 100 + random.nextInt(400);
            int altura = 100 + random.nextInt(400);
            BufferedImage imagem = gerarImagemAleatoria(largura, altura);
            byte[] bytesOriginal = imagemParaBytes(imagem, "png"); // Entrada PNG
            
            // Processar para thumbnail
            byte[] thumbnail = processor.processarParaThumbnail(bytesOriginal);
            
            // Verificar magic bytes JPEG (FF D8 FF)
            assertTrue(thumbnail.length >= 3, "Thumbnail deve ter pelo menos 3 bytes");
            assertEquals((byte) 0xFF, thumbnail[0], "Primeiro byte deve ser FF (JPEG)");
            assertEquals((byte) 0xD8, thumbnail[1], "Segundo byte deve ser D8 (JPEG)");
            assertEquals((byte) 0xFF, thumbnail[2], "Terceiro byte deve ser FF (JPEG)");
        }
    }
    
    // ========== Testes de Hash ==========
    
    @Nested
    @DisplayName("Cálculo de Hash")
    class CalculoHash {
        
        @RepeatedTest(value = 50, name = "Iteração {currentRepetition}/{totalRepetitions}")
        @DisplayName("Hash deve ter 64 caracteres hexadecimais")
        void hashDeveTer64Caracteres() throws IOException {
            int largura = 50 + random.nextInt(200);
            int altura = 50 + random.nextInt(200);
            BufferedImage imagem = gerarImagemAleatoria(largura, altura);
            byte[] bytes = imagemParaBytes(imagem, "jpg");
            
            String hash = processor.calcularHash(bytes);
            
            assertEquals(64, hash.length(), "Hash SHA-256 deve ter 64 caracteres");
            assertTrue(hash.matches("[0-9a-f]+"), "Hash deve conter apenas hexadecimais");
        }
        
        @RepeatedTest(value = 50, name = "Iteração {currentRepetition}/{totalRepetitions}")
        @DisplayName("Mesmo conteúdo deve gerar mesmo hash")
        void mesmoConteudoMesmoHash() throws IOException {
            int largura = 50 + random.nextInt(200);
            int altura = 50 + random.nextInt(200);
            BufferedImage imagem = gerarImagemAleatoria(largura, altura);
            byte[] bytes = imagemParaBytes(imagem, "jpg");
            
            String hash1 = processor.calcularHash(bytes);
            String hash2 = processor.calcularHash(bytes);
            
            assertEquals(hash1, hash2, "Mesmo conteúdo deve gerar mesmo hash");
        }
        
        @RepeatedTest(value = 50, name = "Iteração {currentRepetition}/{totalRepetitions}")
        @DisplayName("Conteúdos diferentes devem gerar hashes diferentes")
        void conteudosDiferentesHashesDiferentes() throws IOException {
            BufferedImage imagem1 = gerarImagemAleatoria(100, 100);
            BufferedImage imagem2 = gerarImagemAleatoria(100, 100);
            
            byte[] bytes1 = imagemParaBytes(imagem1, "jpg");
            byte[] bytes2 = imagemParaBytes(imagem2, "jpg");
            
            String hash1 = processor.calcularHash(bytes1);
            String hash2 = processor.calcularHash(bytes2);
            
            // Muito improvável que sejam iguais
            assertNotEquals(hash1, hash2, "Conteúdos diferentes devem gerar hashes diferentes");
        }
    }
    
    // ========== Métodos auxiliares ==========
    
    /**
     * Gera uma imagem com pixels aleatórios.
     */
    private BufferedImage gerarImagemAleatoria(int largura, int altura) {
        BufferedImage imagem = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = imagem.createGraphics();
        
        // Preencher com cor de fundo aleatória
        g2d.setColor(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
        g2d.fillRect(0, 0, largura, altura);
        
        // Adicionar alguns elementos aleatórios
        int numElementos = 5 + random.nextInt(20);
        for (int i = 0; i < numElementos; i++) {
            g2d.setColor(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
            int x = random.nextInt(largura);
            int y = random.nextInt(altura);
            int w = 10 + random.nextInt(Math.max(1, largura / 4));
            int h = 10 + random.nextInt(Math.max(1, altura / 4));
            
            if (random.nextBoolean()) {
                g2d.fillRect(x, y, w, h);
            } else {
                g2d.fillOval(x, y, w, h);
            }
        }
        
        g2d.dispose();
        return imagem;
    }
    
    /**
     * Converte BufferedImage para bytes.
     */
    private byte[] imagemParaBytes(BufferedImage imagem, String formato) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(imagem, formato, baos);
        return baos.toByteArray();
    }
    
    /**
     * Converte bytes para BufferedImage.
     */
    private BufferedImage bytesParaImagem(byte[] bytes) throws IOException {
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }
    
    /**
     * Gera array de bytes aleatórios.
     */
    private byte[] gerarBytesAleatorios(int minTamanho, int maxTamanho) {
        int tamanho = minTamanho + random.nextInt(maxTamanho - minTamanho);
        byte[] bytes = new byte[tamanho];
        random.nextBytes(bytes);
        return bytes;
    }
}
