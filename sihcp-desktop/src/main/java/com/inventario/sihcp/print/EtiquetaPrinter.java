package com.inventario.sihcp.print;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.inventario.sihcp.model.Patrimonio;

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
        
        // Configurar tamanho da página baseado na configuração (LARGURA TOTAL DAS COLUNAS)
        Paper paper = pageFormat.getPaper();
        double totalWidth = config.getLargura() * config.getColunas() * MM_TO_POINTS;
        // Adicionar pequeno gap entre colunas se houver mais de uma
        if (config.getColunas() > 1) {
            totalWidth += (config.getColunas() - 1) * 2 * MM_TO_POINTS; 
        }
        double height = config.getAltura() * MM_TO_POINTS;
        
        paper.setSize(totalWidth, height);
        paper.setImageableArea(0, 0, totalWidth, height);
        pageFormat.setPaper(paper);
        pageFormat.setOrientation(PageFormat.PORTRAIT);
        
        job.setPrintable(this, pageFormat);
        
        if (job.printDialog()) {
            job.print();
        }
    }
    
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        int labelsPorPagina = config.getColunas();
        int totalPaginas = (int) Math.ceil((double) patrimonios.size() / labelsPorPagina);
        
        if (pageIndex >= totalPaginas) {
            return NO_SUCH_PAGE;
        }
        
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Desenhar cada etiqueta da linha
        for (int col = 0; col < labelsPorPagina; col++) {
            int patrimonioIndex = pageIndex * labelsPorPagina + col;
            if (patrimonioIndex < patrimonios.size()) {
                Patrimonio p = patrimonios.get(patrimonioIndex);
                double offsetX = col * (config.getLargura() + 2) * MM_TO_POINTS;
                desenharEtiqueta(g2d, p, offsetX, 0);
            }
        }
        
        return PAGE_EXISTS;
    }
    
    public void desenharEtiqueta(Graphics2D g2d, Patrimonio patrimonio, double offsetX, double offsetY) {
        int x = (int) offsetX;
        int y = (int) offsetY;
        
        g2d.setColor(Color.BLACK);
        
        // 1. Cabeçalho
        if (config.getCabecalho() != null && !config.getCabecalho().isEmpty()) {
            g2d.setFont(new Font("Arial", Font.BOLD, config.getTamanhoFonteCabecalho()));
            g2d.drawString(config.getCabecalho(), 
                (int)(x + config.getxCabecalho() * MM_TO_POINTS), 
                (int)(y + config.getyCabecalho() * MM_TO_POINTS));
        }
        
        // 2. QR Code
        int qrSize = (int) (config.getTamanhoQRCode() * MM_TO_POINTS);
        if (config.isIncluirQRCode()) {
            try {
                BufferedImage qrCode = gerarQRCode(patrimonio.getNumeroPatrimonio(), qrSize, qrSize);
                g2d.drawImage(qrCode, 
                    (int)(x + config.getxQRCode() * MM_TO_POINTS), 
                    (int)(y + config.getyQRCode() * MM_TO_POINTS), null);
            } catch (WriterException e) {
                g2d.drawRect((int)(x + config.getxQRCode() * MM_TO_POINTS), 
                             (int)(y + config.getyQRCode() * MM_TO_POINTS), qrSize, qrSize);
            }
        }
        
        // 3. Número do patrimônio
        g2d.setFont(new Font("Arial", Font.BOLD, config.getTamanhoFonteNumero()));
        g2d.drawString(patrimonio.getNumeroPatrimonio(), 
            (int)(x + config.getxNumero() * MM_TO_POINTS), 
            (int)(y + config.getyNumero() * MM_TO_POINTS));
        
        // 4. Descrição
        if (config.isIncluirDescricao() && patrimonio.getDescricao() != null) {
            g2d.setFont(new Font("Arial", Font.PLAIN, config.getTamanhoFonteDescricao()));
            String desc = patrimonio.getDescricao();
            if (desc.length() > 30) desc = desc.substring(0, 27) + "...";
            g2d.drawString(desc, 
                (int)(x + config.getxDescricao() * MM_TO_POINTS), 
                (int)(y + config.getyDescricao() * MM_TO_POINTS));
        }
        
        // 5. Marca/Modelo
        if (config.isIncluirMarcaModelo()) {
            g2d.setFont(new Font("Arial", Font.ITALIC, config.getTamanhoFonteMarcaModelo()));
            String marca = (patrimonio.getMarca() != null ? patrimonio.getMarca() : "");
            String modelo = (patrimonio.getModelo() != null ? patrimonio.getModelo() : "");
            String info = (marca + " " + modelo).trim();
            if (info.length() > 30) info = info.substring(0, 27) + "...";
            if (!info.isEmpty()) {
                g2d.drawString(info, 
                    (int)(x + config.getxMarcaModelo() * MM_TO_POINTS), 
                    (int)(y + config.getyMarcaModelo() * MM_TO_POINTS));
            }
        }
    }
    
    private BufferedImage gerarQRCode(String texto, int width, int height) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(texto, BarcodeFormat.QR_CODE, width, height);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }
    
    public JPanel criarPainelPreview(List<Patrimonio> patrimonios) {
        JPanel container = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        container.setBackground(new Color(240, 240, 240));
        
        // Simular uma folha ou rolo
        int colunas = config.getColunas();
        int rows = (int) Math.ceil((double) patrimonios.size() / colunas);
        
        JPanel sheet = new JPanel(new GridLayout(rows, colunas, 10, 10));
        sheet.setBackground(Color.WHITE);
        sheet.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        for (Patrimonio p : patrimonios) {
            sheet.add(new EtiquetaPreview(p));
        }
        
        // Preencher células vazias na última linha
        int totalCells = rows * colunas;
        for (int i = patrimonios.size(); i < totalCells; i++) {
            JPanel empty = new JPanel();
            empty.setOpaque(false);
            sheet.add(empty);
        }
        
        container.add(sheet);
        return container;
    }
    
    private class EtiquetaPreview extends JPanel {
        private Patrimonio patrimonio;
        
        public EtiquetaPreview(Patrimonio p) {
            this.patrimonio = p;
            // Escalar mm para pixels no preview (aprox 3.5x para ficar visível)
            setPreferredSize(new Dimension((int)(config.getLargura() * 3.5), (int)(config.getAltura() * 3.5)));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Usar um fator de escala para o preview ser proporcional
            double scale = 3.5 / MM_TO_POINTS; 
            g2d.scale(scale * MM_TO_POINTS, scale * MM_TO_POINTS);
            
            desenharEtiqueta(g2d, patrimonio, 0, 0);
            g2d.dispose();
        }
    }
}
