package com.inventario.util;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;

/**
 * Utilitário para operações com imagens
 * Fornece métodos para conversão, redimensionamento e manipulação de imagens
 * 
 * @author Sistema de Inventário IFMT
 * @version 1.0.0
 */
public class ImageUtils {
    
    /**
     * Converte BufferedImage para array de bytes
     * @param image Imagem a ser convertida
     * @param format Formato da imagem (PNG, JPG, etc.)
     * @return Array de bytes da imagem
     * @throws IOException
     */
    public static byte[] imageToBytes(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return baos.toByteArray();
    }
    
    /**
     * Converte array de bytes para BufferedImage
     * @param imageBytes Array de bytes da imagem
     * @return BufferedImage
     * @throws IOException
     */
    public static BufferedImage bytesToImage(byte[] imageBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
        return ImageIO.read(bais);
    }
    
    /**
     * Converte BufferedImage para String Base64
     * @param image Imagem a ser convertida
     * @param format Formato da imagem
     * @return String Base64 da imagem
     * @throws IOException
     */
    public static String imageToBase64(BufferedImage image, String format) throws IOException {
        byte[] imageBytes = imageToBytes(image, format);
        return Base64.getEncoder().encodeToString(imageBytes);
    }
    
    /**
     * Converte String Base64 para BufferedImage
     * @param base64String String Base64 da imagem
     * @return BufferedImage
     * @throws IOException
     */
    public static BufferedImage base64ToImage(String base64String) throws IOException {
        byte[] imageBytes = Base64.getDecoder().decode(base64String);
        return bytesToImage(imageBytes);
    }
    
    /**
     * Redimensiona uma imagem mantendo a proporção
     * @param originalImage Imagem original
     * @param targetWidth Largura desejada
     * @param targetHeight Altura desejada
     * @return Imagem redimensionada
     */
    public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        // Calcula as dimensões mantendo a proporção
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        double widthRatio = (double) targetWidth / originalWidth;
        double heightRatio = (double) targetHeight / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);
        
        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);
        
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();
        
        // Configurações para melhor qualidade
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        
        return resizedImage;
    }
    
    /**
     * Converte BufferedImage para ImageIcon
     * @param image Imagem a ser convertida
     * @return ImageIcon
     */
    public static ImageIcon toImageIcon(BufferedImage image) {
        return new ImageIcon(image);
    }
    
    /**
     * Converte BufferedImage para ImageIcon com redimensionamento
     * @param image Imagem a ser convertida
     * @param width Largura desejada
     * @param height Altura desejada
     * @return ImageIcon redimensionado
     */
    public static ImageIcon toImageIcon(BufferedImage image, int width, int height) {
        BufferedImage resized = resizeImage(image, width, height);
        return new ImageIcon(resized);
    }
    
    /**
     * Salva uma imagem em arquivo
     * @param image Imagem a ser salva
     * @param file Arquivo de destino
     * @param format Formato da imagem (PNG, JPG, etc.)
     * @throws IOException
     */
    public static void saveImage(BufferedImage image, File file, String format) throws IOException {
        ImageIO.write(image, format, file);
    }
    
    /**
     * Carrega uma imagem de arquivo
     * @param file Arquivo da imagem
     * @return BufferedImage
     * @throws IOException
     */
    public static BufferedImage loadImage(File file) throws IOException {
        return ImageIO.read(file);
    }
    
    /**
     * Cria uma imagem em branco
     * @param width Largura
     * @param height Altura
     * @param color Cor de fundo
     * @return BufferedImage em branco
     */
    public static BufferedImage createBlankImage(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
        return image;
    }
    
    /**
     * Verifica se um arquivo é uma imagem válida
     * @param file Arquivo a ser verificado
     * @return true se for uma imagem válida
     */
    public static boolean isValidImageFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }
        
        String name = file.getName().toLowerCase();
        return name.endsWith(".png") || name.endsWith(".jpg") || 
               name.endsWith(".jpeg") || name.endsWith(".gif") || 
               name.endsWith(".bmp");
    }
    
    /**
     * Obtém as dimensões de uma imagem sem carregá-la completamente
     * @param file Arquivo da imagem
     * @return Dimension com largura e altura, ou null se erro
     */
    public static Dimension getImageDimensions(File file) {
        try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
            var readers = ImageIO.getImageReaders(iis);
            if (readers.hasNext()) {
                var reader = readers.next();
                reader.setInput(iis);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                reader.dispose();
                return new Dimension(width, height);
            }
        } catch (IOException e) {
            // Ignora erro e retorna null
        }
        return null;
    }
}