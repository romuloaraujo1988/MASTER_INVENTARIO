package com.inventario.sihcp.print;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Gerador de imagens de etiquetas para impressão
 */
public class EtiquetaGenerator {
    
    private static final int DPI_TERMICA = 203; // DPI padrão de impressoras térmicas
    private static final Font FONT_NUMERO = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_DESCRICAO = new Font("Arial", Font.PLAIN, 10);
    private static final Font FONT_LOCALIZACAO = new Font("Arial", Font.PLAIN, 8);
    
    /**
     * Gera imagem da etiqueta
     */
    public BufferedImage gerarEtiqueta(EtiquetaData data, EtiquetaLayout layout) throws WriterException {
        int largura = layout.getLarguraPixels(DPI_TERMICA);
        int altura = layout.getAlturaPixels(DPI_TERMICA);
        
        BufferedImage etiqueta = new BufferedImage(largura, altura, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2d = etiqueta.createGraphics();
        
        // Configurar renderização
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Fundo branco
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, largura, altura);
        
        // Cor preta para texto e QR Code
        g2d.setColor(Color.BLACK);
        
        // Gerar QR Code
        int tamanhoQr = layout.getTamanhoQrPixels(DPI_TERMICA);
        BufferedImage qrCode = gerarQRCode(data.getNumeroPatrimonio(), tamanhoQr);
        
        // Posicionar elementos
        int margemX = 10;
        int margemY = 10;
        int posY = margemY;
        
        // Desenhar QR Code (centralizado horizontalmente)
        int qrX = (largura - tamanhoQr) / 2;
        g2d.drawImage(qrCode, qrX, posY, null);
        posY += tamanhoQr + 5;
        
        // Desenhar número do patrimônio
        if (layout.isIncluirNumero()) {
            g2d.setFont(FONT_NUMERO);
            String numero = data.getNumeroPatrimonio();
            FontMetrics fm = g2d.getFontMetrics();
            int numeroX = (largura - fm.stringWidth(numero)) / 2;
            g2d.drawString(numero, numeroX, posY);
            posY += fm.getHeight();
        }
        
        // Desenhar descrição
        if (layout.isIncluirDescricao() && data.getDescricao() != null) {
            g2d.setFont(FONT_DESCRICAO);
            String descricao = data.getDescricaoResumida(30);
            FontMetrics fm = g2d.getFontMetrics();
            
            // Quebrar texto se necessário
            String[] linhas = quebrarTexto(descricao, fm, largura - (2 * margemX));
            for (String linha : linhas) {
                int textoX = (largura - fm.stringWidth(linha)) / 2;
                g2d.drawString(linha, textoX, posY);
                posY += fm.getHeight();
            }
        }
        
        // Desenhar localização
        if (layout.isIncluirLocalizacao() && data.getLocalizacao() != null) {
            g2d.setFont(FONT_LOCALIZACAO);
            String localizacao = data.getLocalizacao();
            FontMetrics fm = g2d.getFontMetrics();
            int locX = (largura - fm.stringWidth(localizacao)) / 2;
            g2d.drawString(localizacao, locX, posY);
        }
        
        g2d.dispose();
        return etiqueta;
    }
    
    /**
     * Gera QR Code
     */
    private BufferedImage gerarQRCode(String conteudo, int tamanho) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);
        
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(conteudo, BarcodeFormat.QR_CODE, tamanho, tamanho, hints);
        
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }
    
    /**
     * Quebra texto em múltiplas linhas
     */
    private String[] quebrarTexto(String texto, FontMetrics fm, int larguraMax) {
        if (fm.stringWidth(texto) <= larguraMax) {
            return new String[]{texto};
        }
        
        String[] palavras = texto.split(" ");
        StringBuilder linha1 = new StringBuilder();
        StringBuilder linha2 = new StringBuilder();
        
        boolean primeiraLinha = true;
        for (String palavra : palavras) {
            String teste = (primeiraLinha ? linha1 : linha2).toString();
            if (!teste.isEmpty()) teste += " ";
            teste += palavra;
            
            if (fm.stringWidth(teste) <= larguraMax) {
                if (primeiraLinha) {
                    if (linha1.length() > 0) linha1.append(" ");
                    linha1.append(palavra);
                } else {
                    if (linha2.length() > 0) linha2.append(" ");
                    linha2.append(palavra);
                }
            } else {
                if (primeiraLinha) {
                    primeiraLinha = false;
                    linha2.append(palavra);
                } else {
                    break; // Não cabe mais
                }
            }
        }
        
        if (linha2.length() == 0) {
            return new String[]{linha1.toString()};
        }
        return new String[]{linha1.toString(), linha2.toString()};
    }
    
    /**
     * Converte imagem para bytes PNG
     */
    public byte[] imagemParaBytes(BufferedImage imagem) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(imagem, "PNG", baos);
        return baos.toByteArray();
    }
}
