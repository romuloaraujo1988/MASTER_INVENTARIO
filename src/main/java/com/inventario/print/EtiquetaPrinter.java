package com.inventario.print;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.inventario.model.Patrimonio;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.util.List;

/**
 * Classe responsável pela impressão de etiquetas patrimoniais
 */
public class EtiquetaPrinter implements Printable {
    
    private EtiquetaConfig config;
    private List<Patrimonio> patrimonios;
    private static final double MM_TO_POINTS = 2.83465; // Conversão mm para pontos
    
    public EtiquetaPrinter(EtiquetaConfig config) {
        this.config = config;
    }
    
    public void imprimir(List<Patrimonio> patrimonios) throws PrinterException {
        this.patrimonios = patrimonios;
        
        PrinterJob job = PrinterJob.getPrinterJob();
        PageFormat pageFormat = job.defaultPage();
        
        // Configurar tamanho da página baseado na configuração
        Paper paper = pageFormat.getPaper();
        double width = config.getLargura() * MM_TO_POINTS;
        double height = config.getAltura() * MM_TO_POINTS;
        paper.setSize(width, height);
        paper.setImageableArea(0, 0, width, height);
        pageFormat.setPaper(paper);
        
        job.setPrintable(this, pageFormat);
        
        if (job.printDialog()) {
            job.print();
        }
    }
    
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex >= patrimonios.size()) {
            return NO_SUCH_PAGE;
        }
        
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        Patrimonio patrimonio = patrimonios.get(pageIndex);
        desenharEtiqueta(g2d, patrimonio, pageFormat);
        
        return PAGE_EXISTS;
    }
    
    private void desenharEtiqueta(Graphics2D g2d, Patrimonio patrimonio, PageFormat pageFormat) {
        int x = (int) pageFormat.getImageableX();
        int y = (int) pageFormat.getImageableY();
        int width = (int) pageFormat.getImageableWidth();
        int height = (int) pageFormat.getImageableHeight();
        
        // Margens
        int marginX = (int) (config.getMargemHorizontal() * MM_TO_POINTS);
        int marginY = (int) (config.getMargemVertical() * MM_TO_POINTS);
        
        x += marginX;
        y += marginY;
        width -= 2 * marginX;
        height -= 2 * marginY;
        
        // Desenhar borda
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
        
        int currentY = y + 10;
        
        // QR Code
        if (config.isIncluirQRCode()) {
            try {
                BufferedImage qrCode = gerarQRCode(patrimonio.getNumeroPatrimonio(), 80, 80);
                int qrX = x + (width - 80) / 2;
                g2d.drawImage(qrCode, qrX, currentY, null);
                currentY += 90;
            } catch (WriterException e) {
                // Ignorar erro de QR Code
            }
        }
        
        // Número do patrimônio
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        String numero = "Nº " + patrimonio.getNumeroPatrimonio();
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x + (width - fm.stringWidth(numero)) / 2;
        g2d.drawString(numero, textX, currentY);
        currentY += 20;
        
        // Descrição
        if (config.isIncluirDescricao() && patrimonio.getDescricao() != null) {
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            String desc = patrimonio.getDescricao();
            if (desc.length() > 40) {
                desc = desc.substring(0, 37) + "...";
            }
            fm = g2d.getFontMetrics();
            textX = x + (width - fm.stringWidth(desc)) / 2;
            g2d.drawString(desc, textX, currentY);
            currentY += 15;
        }
        
        // Sala
        if (patrimonio.getNomeSala() != null) {
            g2d.setFont(new Font("Arial", Font.PLAIN, 9));
            String sala = "Sala: " + patrimonio.getNomeSala();
            fm = g2d.getFontMetrics();
            textX = x + (width - fm.stringWidth(sala)) / 2;
            g2d.drawString(sala, textX, currentY);
        }
    }
    
    private BufferedImage gerarQRCode(String texto, int width, int height) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(texto, BarcodeFormat.QR_CODE, width, height);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }
    
    public JPanel criarPainelPreview(List<Patrimonio> patrimonios) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        
        for (Patrimonio patrimonio : patrimonios) {
            JPanel etiquetaPanel = criarEtiquetaPreview(patrimonio);
            panel.add(etiquetaPanel);
            panel.add(Box.createVerticalStrut(10));
        }
        
        return panel;
    }
    
    private JPanel criarEtiquetaPreview(Patrimonio patrimonio) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Simular etiqueta
                g2d.setColor(Color.BLACK);
                g2d.drawRect(5, 5, getWidth() - 10, getHeight() - 10);
                
                int currentY = 20;
                
                // QR Code simulado
                if (config.isIncluirQRCode()) {
                    try {
                        BufferedImage qrCode = gerarQRCode(patrimonio.getNumeroPatrimonio(), 80, 80);
                        int qrX = (getWidth() - 80) / 2;
                        g2d.drawImage(qrCode, qrX, currentY, null);
                        currentY += 90;
                    } catch (WriterException e) {
                        // Desenhar placeholder
                        g2d.drawRect((getWidth() - 80) / 2, currentY, 80, 80);
                        currentY += 90;
                    }
                }
                
                // Número
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                String numero = "Nº " + patrimonio.getNumeroPatrimonio();
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(numero)) / 2;
                g2d.drawString(numero, textX, currentY);
                currentY += 20;
                
                // Descrição
                if (config.isIncluirDescricao() && patrimonio.getDescricao() != null) {
                    g2d.setFont(new Font("Arial", Font.PLAIN, 10));
                    String desc = patrimonio.getDescricao();
                    if (desc.length() > 40) {
                        desc = desc.substring(0, 37) + "...";
                    }
                    fm = g2d.getFontMetrics();
                    textX = (getWidth() - fm.stringWidth(desc)) / 2;
                    g2d.drawString(desc, textX, currentY);
                    currentY += 15;
                }
                
                // Sala
                if (patrimonio.getNomeSala() != null) {
                    g2d.setFont(new Font("Arial", Font.PLAIN, 9));
                    String sala = "Sala: " + patrimonio.getNomeSala();
                    fm = g2d.getFontMetrics();
                    textX = (getWidth() - fm.stringWidth(sala)) / 2;
                    g2d.drawString(sala, textX, currentY);
                }
            }
        };
        
        panel.setPreferredSize(new Dimension(250, 200));
        panel.setMaximumSize(new Dimension(250, 200));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        return panel;
    }
}
