package com.inventario.util;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Iterator;

/**
 * Processador de imagens especializado para fotos de referência de patrimônios.
 * 
 * Responsável por:
 * - Validar imagens (tipo e tamanho)
 * - Redimensionar para thumbnail (200x200px)
 * - Comprimir para JPEG com qualidade configurável
 * - Garantir tamanho máximo de 50KB
 * - Calcular hash SHA-256 para verificação de integridade
 * 
 * @author Sistema de Inventário
 * @since 2.9.0
 */
public class ImageProcessor {
    
    // Constantes de configuração
    public static final int MAX_WIDTH = 200;
    public static final int MAX_HEIGHT = 200;
    public static final int MAX_SIZE_BYTES = 51200; // 50KB
    public static final long MAX_INPUT_SIZE_BYTES = 2 * 1024 * 1024; // 2MB
    public static final float DEFAULT_QUALITY = 0.80f; // 80%
    public static final float MIN_QUALITY = 0.30f; // 30% mínimo
    
    // Formatos suportados
    private static final String[] SUPPORTED_FORMATS = {"jpg", "jpeg", "png", "gif", "bmp"};
    
    /**
     * Processa uma imagem para uso como foto de referência.
     * Pipeline completo: validação → redimensionamento → compressão → verificação de tamanho
     * 
     * @param imagemOriginal Bytes da imagem original
     * @return Bytes do thumbnail processado (JPEG, max 50KB)
     * @throws ImageProcessingException Se a imagem for inválida ou não puder ser processada
     */
    public byte[] processarParaThumbnail(byte[] imagemOriginal) throws ImageProcessingException {
        // 1. Validar entrada
        if (imagemOriginal == null || imagemOriginal.length == 0) {
            throw new ImageProcessingException("Imagem não pode ser nula ou vazia");
        }
        
        if (imagemOriginal.length > MAX_INPUT_SIZE_BYTES) {
            throw new ImageProcessingException(
                String.format("Imagem excede o tamanho máximo de %d MB", MAX_INPUT_SIZE_BYTES / (1024 * 1024)));
        }
        
        // 2. Verificar se é imagem válida
        if (!isImagemValida(imagemOriginal)) {
            throw new ImageProcessingException("Arquivo não é uma imagem válida (JPG, PNG, GIF ou BMP)");
        }
        
        try {
            // 3. Carregar imagem
            BufferedImage imagem = ImageUtils.bytesToImage(imagemOriginal);
            if (imagem == null) {
                throw new ImageProcessingException("Não foi possível decodificar a imagem");
            }
            
            // 4. Redimensionar mantendo proporção
            BufferedImage thumbnail = redimensionar(imagem, MAX_WIDTH, MAX_HEIGHT);
            
            // 5. Comprimir para JPEG com qualidade ajustável para caber em 50KB
            byte[] resultado = comprimirComTamanhoMaximo(thumbnail, MAX_SIZE_BYTES);
            
            return resultado;
            
        } catch (IOException e) {
            throw new ImageProcessingException("Erro ao processar imagem: " + e.getMessage(), e);
        }
    }
    
    /**
     * Verifica se os bytes representam uma imagem válida.
     * 
     * @param dados Bytes do arquivo
     * @return true se for uma imagem válida (JPG, PNG, GIF ou BMP)
     */
    public boolean isImagemValida(byte[] dados) {
        if (dados == null || dados.length < 8) {
            return false;
        }
        
        // Verificar magic bytes
        // JPEG: FF D8 FF
        if (dados[0] == (byte) 0xFF && dados[1] == (byte) 0xD8 && dados[2] == (byte) 0xFF) {
            return true;
        }
        
        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if (dados[0] == (byte) 0x89 && dados[1] == (byte) 0x50 && 
            dados[2] == (byte) 0x4E && dados[3] == (byte) 0x47) {
            return true;
        }
        
        // GIF: 47 49 46 38
        if (dados[0] == (byte) 0x47 && dados[1] == (byte) 0x49 && 
            dados[2] == (byte) 0x46 && dados[3] == (byte) 0x38) {
            return true;
        }
        
        // BMP: 42 4D
        if (dados[0] == (byte) 0x42 && dados[1] == (byte) 0x4D) {
            return true;
        }
        
        // Tentar carregar como imagem (fallback)
        try {
            BufferedImage img = ImageUtils.bytesToImage(dados);
            return img != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Redimensiona uma imagem mantendo a proporção.
     * 
     * @param original Imagem original
     * @param maxWidth Largura máxima
     * @param maxHeight Altura máxima
     * @return Imagem redimensionada
     */
    public BufferedImage redimensionar(BufferedImage original, int maxWidth, int maxHeight) {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();
        
        // Se já está dentro dos limites, retornar cópia
        if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
            return copyImage(original);
        }
        
        // Calcular proporção
        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);
        
        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);
        
        // Garantir dimensões mínimas
        newWidth = Math.max(1, newWidth);
        newHeight = Math.max(1, newHeight);
        
        // Criar imagem redimensionada com alta qualidade
        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        
        // Configurações para melhor qualidade
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Preencher fundo branco (para imagens com transparência)
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, newWidth, newHeight);
        
        // Desenhar imagem redimensionada
        g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        
        return resized;
    }
    
    /**
     * Comprime uma imagem para JPEG com qualidade especificada.
     * 
     * @param imagem Imagem a comprimir
     * @param qualidade Qualidade (0.0 a 1.0)
     * @return Bytes da imagem comprimida
     * @throws IOException Se houver erro na compressão
     */
    public byte[] comprimirJpeg(BufferedImage imagem, float qualidade) throws IOException {
        // Garantir que a imagem está em formato RGB (sem alpha)
        BufferedImage rgbImage = convertToRGB(imagem);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            throw new IOException("Nenhum writer JPEG disponível");
        }
        
        ImageWriter writer = writers.next();
        
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            writer.setOutput(ios);
            
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(qualidade);
            
            writer.write(null, new IIOImage(rgbImage, null, null), param);
        } finally {
            writer.dispose();
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Comprime uma imagem garantindo que o tamanho final não exceda o máximo.
     * Reduz a qualidade progressivamente se necessário.
     * 
     * @param imagem Imagem a comprimir
     * @param maxBytes Tamanho máximo em bytes
     * @return Bytes da imagem comprimida
     * @throws IOException Se não for possível comprimir dentro do limite
     */
    public byte[] comprimirComTamanhoMaximo(BufferedImage imagem, int maxBytes) throws IOException {
        float qualidade = DEFAULT_QUALITY;
        byte[] resultado;
        
        // Tentar comprimir com qualidade decrescente
        while (qualidade >= MIN_QUALITY) {
            resultado = comprimirJpeg(imagem, qualidade);
            
            if (resultado.length <= maxBytes) {
                return resultado;
            }
            
            // Reduzir qualidade em 10%
            qualidade -= 0.10f;
        }
        
        // Última tentativa com qualidade mínima
        resultado = comprimirJpeg(imagem, MIN_QUALITY);
        
        if (resultado.length > maxBytes) {
            // Se ainda não couber, redimensionar mais
            int newWidth = (int) (imagem.getWidth() * 0.8);
            int newHeight = (int) (imagem.getHeight() * 0.8);
            BufferedImage menor = redimensionar(imagem, newWidth, newHeight);
            return comprimirComTamanhoMaximo(menor, maxBytes);
        }
        
        return resultado;
    }
    
    /**
     * Calcula o hash SHA-256 de uma imagem.
     * 
     * @param dados Bytes da imagem
     * @return Hash em formato hexadecimal (64 caracteres)
     */
    public String calcularHash(byte[] dados) {
        if (dados == null || dados.length == 0) {
            return "";
        }
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(dados);
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 sempre disponível em Java
            throw new RuntimeException("SHA-256 não disponível", e);
        }
    }
    
    /**
     * Converte bytes de imagem para Base64.
     * 
     * @param dados Bytes da imagem
     * @return String Base64
     */
    public String toBase64(byte[] dados) {
        if (dados == null || dados.length == 0) {
            return "";
        }
        return Base64.getEncoder().encodeToString(dados);
    }
    
    /**
     * Converte Base64 para bytes de imagem.
     * 
     * @param base64 String Base64
     * @return Bytes da imagem
     */
    public byte[] fromBase64(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return new byte[0];
        }
        return Base64.getDecoder().decode(base64);
    }
    
    /**
     * Obtém as dimensões de uma imagem.
     * 
     * @param dados Bytes da imagem
     * @return Array [largura, altura] ou null se erro
     */
    public int[] getDimensoes(byte[] dados) {
        try {
            BufferedImage img = ImageUtils.bytesToImage(dados);
            if (img != null) {
                return new int[] { img.getWidth(), img.getHeight() };
            }
        } catch (IOException e) {
            // Ignora
        }
        return null;
    }
    
    /**
     * Verifica se as dimensões estão dentro dos limites.
     * 
     * @param dados Bytes da imagem
     * @return true se dimensões <= 200x200
     */
    public boolean dimensoesValidas(byte[] dados) {
        int[] dim = getDimensoes(dados);
        if (dim == null) {
            return false;
        }
        return dim[0] <= MAX_WIDTH && dim[1] <= MAX_HEIGHT;
    }
    
    /**
     * Verifica se o tamanho está dentro do limite.
     * 
     * @param dados Bytes da imagem
     * @return true se tamanho <= 50KB
     */
    public boolean tamanhoValido(byte[] dados) {
        return dados != null && dados.length <= MAX_SIZE_BYTES;
    }
    
    // ========== Métodos auxiliares privados ==========
    
    /**
     * Cria uma cópia de uma imagem.
     */
    private BufferedImage copyImage(BufferedImage source) {
        BufferedImage copy = new BufferedImage(
            source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = copy.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, copy.getWidth(), copy.getHeight());
        g2d.drawImage(source, 0, 0, null);
        g2d.dispose();
        return copy;
    }
    
    /**
     * Converte imagem para RGB (remove canal alpha).
     */
    private BufferedImage convertToRGB(BufferedImage source) {
        if (source.getType() == BufferedImage.TYPE_INT_RGB) {
            return source;
        }
        
        BufferedImage rgb = new BufferedImage(
            source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = rgb.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
        g2d.drawImage(source, 0, 0, null);
        g2d.dispose();
        return rgb;
    }
    
    /**
     * Exceção específica para erros de processamento de imagem.
     */
    public static class ImageProcessingException extends Exception {
        public ImageProcessingException(String message) {
            super(message);
        }
        
        public ImageProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
