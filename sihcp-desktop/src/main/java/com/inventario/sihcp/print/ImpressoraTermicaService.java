package com.inventario.sihcp.print;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.OrientationRequested;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço para impressão em impressoras térmicas
 * Suporta impressoras compatíveis com Java Print Service
 */
public class ImpressoraTermicaService {
    
    private final EtiquetaGenerator generator;
    
    public ImpressoraTermicaService() {
        this.generator = new EtiquetaGenerator();
    }
    
    /**
     * Lista todas as impressoras disponíveis
     */
    public List<String> listarImpressoras() {
        List<String> impressoras = new ArrayList<>();
        
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService service : printServices) {
            impressoras.add(service.getName());
        }
        
        return impressoras;
    }
    
    /**
     * Obtém a impressora padrão
     */
    public String getImpressoraPadrao() {
        PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
        return defaultService != null ? defaultService.getName() : null;
    }
    
    /**
     * Busca impressora por nome
     */
    public PrintService buscarImpressora(String nomeImpressora) {
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        
        for (PrintService service : printServices) {
            if (service.getName().equalsIgnoreCase(nomeImpressora)) {
                return service;
            }
        }
        
        return null;
    }
    
    /**
     * Imprime etiqueta
     */
    public boolean imprimirEtiqueta(EtiquetaData data, EtiquetaLayout layout, String nomeImpressora) {
        return imprimirEtiqueta(data, layout, nomeImpressora, 1);
    }
    
    /**
     * Imprime múltiplas cópias de uma etiqueta
     */
    public boolean imprimirEtiqueta(EtiquetaData data, EtiquetaLayout layout, 
                                   String nomeImpressora, int copias) {
        try {
            // Buscar impressora
            PrintService printService;
            if (nomeImpressora == null || nomeImpressora.isEmpty()) {
                printService = PrintServiceLookup.lookupDefaultPrintService();
            } else {
                printService = buscarImpressora(nomeImpressora);
            }
            
            if (printService == null) {
                System.err.println("Impressora não encontrada: " + nomeImpressora);
                return false;
            }
            
            // Gerar imagem da etiqueta
            BufferedImage etiqueta = generator.gerarEtiqueta(data, layout);
            byte[] imagemBytes = generator.imagemParaBytes(etiqueta);
            
            // Criar job de impressão
            DocPrintJob printJob = printService.createPrintJob();
            
            // Configurar atributos de impressão
            PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
            attributes.add(new Copies(copias));
            attributes.add(OrientationRequested.PORTRAIT);
            
            // Criar documento
            Doc doc = new SimpleDoc(
                new ByteArrayInputStream(imagemBytes),
                DocFlavor.INPUT_STREAM.PNG,
                null
            );
            
            // Imprimir
            printJob.print(doc, attributes);
            
            System.out.println("Etiqueta enviada para impressão: " + printService.getName());
            return true;
            
        } catch (Exception e) {
            System.err.println("Erro ao imprimir etiqueta: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Imprime múltiplas etiquetas
     */
    public int imprimirEtiquetas(List<EtiquetaData> etiquetas, EtiquetaLayout layout, 
                                String nomeImpressora) {
        int sucesso = 0;
        
        for (EtiquetaData data : etiquetas) {
            if (imprimirEtiqueta(data, layout, nomeImpressora)) {
                sucesso++;
            }
        }
        
        return sucesso;
    }
    
    /**
     * Verifica se uma impressora suporta o formato
     */
    public boolean impressoraSuportaFormato(String nomeImpressora, DocFlavor flavor) {
        PrintService service = buscarImpressora(nomeImpressora);
        if (service == null) return false;
        
        DocFlavor[] flavors = service.getSupportedDocFlavors();
        for (DocFlavor f : flavors) {
            if (f.equals(flavor)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Obtém informações sobre uma impressora
     */
    public String getInfoImpressora(String nomeImpressora) {
        PrintService service = buscarImpressora(nomeImpressora);
        if (service == null) return "Impressora não encontrada";
        
        StringBuilder info = new StringBuilder();
        info.append("Nome: ").append(service.getName()).append("\n");
        
        // Formatos suportados
        info.append("Formatos suportados:\n");
        DocFlavor[] flavors = service.getSupportedDocFlavors();
        for (DocFlavor flavor : flavors) {
            info.append("  - ").append(flavor.toString()).append("\n");
        }
        
        return info.toString();
    }
}
