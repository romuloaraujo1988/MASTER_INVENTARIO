package com.inventario.view.components;

import com.inventario.util.Page;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Teste visual do componente PaginationPanel
 * Execute este arquivo para visualizar o componente
 */
public class PaginationPanelTest {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Teste - PaginationPanel");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());
            
            // Criar painel de paginação
            PaginationPanel paginationPanel = new PaginationPanel();
            
            // Simular dados
            int currentPage = 0;
            int pageSize = 50;
            long totalElements = 1234; // Simular 1234 registros
            
            // Criar página de exemplo
            List<String> mockData = new ArrayList<>();
            for (int i = 0; i < pageSize; i++) {
                mockData.add("Item " + (currentPage * pageSize + i + 1));
            }
            Page<String> page = new Page<>(mockData, currentPage, pageSize, totalElements);
            
            // Atualizar painel
            paginationPanel.updatePageInfo(
                page.getPageNumber(),
                page.getTotalPages(),
                page.getTotalElements()
            );
            
            // Adicionar listeners de teste
            paginationPanel.addFirstPageListener(e -> {
                System.out.println("Primeira página clicada");
                paginationPanel.updatePageInfo(0, page.getTotalPages(), totalElements);
            });
            
            paginationPanel.addPreviousPageListener(e -> {
                int newPage = Math.max(0, paginationPanel.getCurrentPage() - 1);
                System.out.println("Página anterior: " + (newPage + 1));
                paginationPanel.updatePageInfo(newPage, page.getTotalPages(), totalElements);
            });
            
            paginationPanel.addNextPageListener(e -> {
                int newPage = Math.min(page.getTotalPages() - 1, paginationPanel.getCurrentPage() + 1);
                System.out.println("Próxima página: " + (newPage + 1));
                paginationPanel.updatePageInfo(newPage, page.getTotalPages(), totalElements);
            });
            
            paginationPanel.addLastPageListener(e -> {
                int lastPage = page.getTotalPages() - 1;
                System.out.println("Última página: " + (lastPage + 1));
                paginationPanel.updatePageInfo(lastPage, page.getTotalPages(), totalElements);
            });
            
            paginationPanel.addPageSizeChangeListener(e -> {
                int newSize = paginationPanel.getPageSize();
                System.out.println("Tamanho de página alterado para: " + newSize);
                // Recalcular páginas
                Page<String> newPage = new Page<>(mockData, 0, newSize, totalElements);
                paginationPanel.updatePageInfo(0, newPage.getTotalPages(), totalElements);
            });
            
            // Adicionar informações de teste
            JPanel infoPanel = new JPanel(new GridLayout(8, 1, 5, 5));
            infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            infoPanel.add(new JLabel("📊 Teste do Componente PaginationPanel"));
            infoPanel.add(new JLabel(" "));
            infoPanel.add(new JLabel("Página atual: " + (page.getPageNumber() + 1)));
            infoPanel.add(new JLabel("Total de páginas: " + page.getTotalPages()));
            infoPanel.add(new JLabel("Total de elementos: " + page.getTotalElements()));
            infoPanel.add(new JLabel("Elementos nesta página: " + page.getNumberOfElements()));
            infoPanel.add(new JLabel("Tem próxima: " + page.hasNext()));
            infoPanel.add(new JLabel("Tem anterior: " + page.hasPrevious()));
            
            frame.add(infoPanel, BorderLayout.CENTER);
            frame.add(paginationPanel, BorderLayout.SOUTH);
            
            frame.setSize(800, 300);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            
            System.out.println("=== Teste PaginationPanel ===");
            System.out.println(page.toString());
            System.out.println("Primeiro elemento: " + page.getFirstElementNumber());
            System.out.println("Último elemento: " + page.getLastElementNumber());
        });
    }
}
