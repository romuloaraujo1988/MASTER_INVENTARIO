package com.inventario.sihcp.view.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Componente de navegação de paginação para Swing
 * Exibe controles de navegação: [Primeira] [Anterior] [Página X de Y] [Próxima] [Última]
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class PaginationPanel extends JPanel {
    
    private JButton btnFirst;
    private JButton btnPrevious;
    private JLabel lblPageInfo;
    private JButton btnNext;
    private JButton btnLast;
    private JComboBox<Integer> cboPageSize;
    private JLabel lblTotalInfo;
    
    private int currentPage = 0;
    private int totalPages = 0;
    private long totalElements = 0;
    
    public PaginationPanel() {
        initComponents();
        setupLayout();
        updatePageInfo(0, 0, 0);
    }
    
    private void initComponents() {
        // Botões de navegação
        btnFirst = new JButton("⏮ Primeira");
        btnPrevious = new JButton("◀ Anterior");
        lblPageInfo = new JLabel("Página 1 de 1");
        btnNext = new JButton("Próxima ▶");
        btnLast = new JButton("Última ⏭");
        
        // ComboBox para tamanho da página
        cboPageSize = new JComboBox<>(new Integer[]{10, 25, 50, 100, 200, 500});
        cboPageSize.setSelectedItem(50);
        
        // Label de informação total
        lblTotalInfo = new JLabel("0 itens");
        
        // Estilizar componentes
        styleButton(btnFirst);
        styleButton(btnPrevious);
        styleButton(btnNext);
        styleButton(btnLast);
        
        lblPageInfo.setFont(new Font("Arial", Font.BOLD, 12));
        lblPageInfo.setForeground(new Color(73, 80, 87));
        
        lblTotalInfo.setFont(new Font("Arial", Font.PLAIN, 11));
        lblTotalInfo.setForeground(new Color(108, 117, 125));
        
        cboPageSize.setFont(new Font("Arial", Font.PLAIN, 11));
    }
    
    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.PLAIN, 11));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(new Color(255, 255, 255));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
    }
    
    private void setupLayout() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 8));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 226, 230)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        setBackground(new Color(248, 249, 250));
        
        // Adicionar componentes
        add(new JLabel("Itens por página:"));
        add(cboPageSize);
        add(Box.createHorizontalStrut(20));
        add(btnFirst);
        add(btnPrevious);
        add(lblPageInfo);
        add(btnNext);
        add(btnLast);
        add(Box.createHorizontalStrut(20));
        add(lblTotalInfo);
    }
    
    /**
     * Atualiza as informações da página
     * 
     * @param currentPage Página atual (0-based)
     * @param totalPages Total de páginas
     * @param totalElements Total de elementos
     */
    public void updatePageInfo(int currentPage, int totalPages, long totalElements) {
        this.currentPage = currentPage;
        this.totalPages = Math.max(1, totalPages);
        this.totalElements = totalElements;
        
        // Atualizar label de página
        if (totalElements == 0) {
            lblPageInfo.setText("Nenhum registro");
        } else {
            lblPageInfo.setText(String.format("Página %d de %d", 
                currentPage + 1, this.totalPages));
        }
        
        // Atualizar label de total
        lblTotalInfo.setText(String.format("Total: %,d %s", 
            totalElements, totalElements == 1 ? "item" : "itens"));
        
        // Habilitar/desabilitar botões
        boolean hasRecords = totalElements > 0;
        boolean hasPrevious = currentPage > 0;
        boolean hasNext = currentPage < this.totalPages - 1;
        
        btnFirst.setEnabled(hasRecords && hasPrevious);
        btnPrevious.setEnabled(hasRecords && hasPrevious);
        btnNext.setEnabled(hasRecords && hasNext);
        btnLast.setEnabled(hasRecords && hasNext);
        
        // Estilizar botões desabilitados
        updateButtonStyle(btnFirst, hasRecords && hasPrevious);
        updateButtonStyle(btnPrevious, hasRecords && hasPrevious);
        updateButtonStyle(btnNext, hasRecords && hasNext);
        updateButtonStyle(btnLast, hasRecords && hasNext);
    }
    
    private void updateButtonStyle(JButton button, boolean enabled) {
        if (enabled) {
            button.setForeground(Color.BLACK);
            button.setBackground(Color.WHITE);
        } else {
            button.setForeground(new Color(173, 181, 189));
            button.setBackground(new Color(248, 249, 250));
        }
    }
    
    /**
     * Adiciona listener para o botão "Primeira Página"
     */
    public void addFirstPageListener(ActionListener listener) {
        btnFirst.addActionListener(listener);
    }
    
    /**
     * Adiciona listener para o botão "Página Anterior"
     */
    public void addPreviousPageListener(ActionListener listener) {
        btnPrevious.addActionListener(listener);
    }
    
    /**
     * Adiciona listener para o botão "Próxima Página"
     */
    public void addNextPageListener(ActionListener listener) {
        btnNext.addActionListener(listener);
    }
    
    /**
     * Adiciona listener para o botão "Última Página"
     */
    public void addLastPageListener(ActionListener listener) {
        btnLast.addActionListener(listener);
    }
    
    /**
     * Adiciona listener para mudança de tamanho de página
     */
    public void addPageSizeChangeListener(ActionListener listener) {
        cboPageSize.addActionListener(listener);
    }
    
    /**
     * Retorna o tamanho de página selecionado
     */
    public int getPageSize() {
        return (Integer) cboPageSize.getSelectedItem();
    }
    
    /**
     * Define o tamanho de página
     */
    public void setPageSize(int size) {
        cboPageSize.setSelectedItem(size);
    }
    
    /**
     * Retorna a página atual
     */
    public int getCurrentPage() {
        return currentPage;
    }
    
    /**
     * Retorna o total de páginas
     */
    public int getTotalPages() {
        return totalPages;
    }
    
    /**
     * Retorna o total de elementos
     */
    public long getTotalElements() {
        return totalElements;
    }
}
