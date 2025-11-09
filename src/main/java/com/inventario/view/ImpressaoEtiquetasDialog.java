package com.inventario.view;

import com.inventario.model.Patrimonio;
import com.inventario.print.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.print.PrinterException;
import java.util.List;

/**
 * Dialog para configuração e impressão de etiquetas patrimoniais
 * Permite escolher formato, tamanho e visualizar antes de imprimir
 */
public class ImpressaoEtiquetasDialog extends JDialog {
    
    private List<Patrimonio> patrimonios;
    private JComboBox<String> cmbFormato;
    private JComboBox<String> cmbTamanho;
    private JCheckBox chkIncluirQRCode;
    private JCheckBox chkIncluirDescricao;
    private JSpinner spnCopias;
    private JTextArea txtPreview;
    private boolean impressaoConfirmada = false;
    
    public ImpressaoEtiquetasDialog(Frame parent, List<Patrimonio> patrimonios) {
        super(parent, "Impressão de Etiquetas", true);
        this.patrimonios = patrimonios;
        
        initComponents();
        setupLayout();
        
        setSize(600, 500);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        // Formato da etiqueta
        String[] formatos = {"Padrão (com QR Code)", "Compacto", "Detalhado"};
        cmbFormato = new JComboBox<>(formatos);
        cmbFormato.addActionListener(e -> atualizarPreview());
        
        // Tamanho da etiqueta
        String[] tamanhos = {"50x30mm", "60x40mm", "70x50mm", "A4 (múltiplas)"};
        cmbTamanho = new JComboBox<>(tamanhos);
        cmbTamanho.addActionListener(e -> atualizarPreview());
        
        // Opções
        chkIncluirQRCode = new JCheckBox("Incluir QR Code", true);
        chkIncluirQRCode.addActionListener(e -> atualizarPreview());
        
        chkIncluirDescricao = new JCheckBox("Incluir Descrição", true);
        chkIncluirDescricao.addActionListener(e -> atualizarPreview());
        
        // Número de cópias
        SpinnerNumberModel modelCopias = new SpinnerNumberModel(1, 1, 10, 1);
        spnCopias = new JSpinner(modelCopias);
        
        // Preview
        txtPreview = new JTextArea();
        txtPreview.setEditable(false);
        txtPreview.setFont(new Font("Monospaced", Font.PLAIN, 11));
        txtPreview.setBackground(new Color(250, 250, 250));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // Painel principal com padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Painel de configurações
        JPanel configPanel = new JPanel(new GridBagLayout());
        configPanel.setBorder(new TitledBorder("Configurações"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Formato
        gbc.gridx = 0; gbc.gridy = 0;
        configPanel.add(new JLabel("Formato:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        configPanel.add(cmbFormato, gbc);
        
        // Tamanho
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        configPanel.add(new JLabel("Tamanho:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        configPanel.add(cmbTamanho, gbc);
        
        // Cópias
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        configPanel.add(new JLabel("Cópias:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        configPanel.add(spnCopias, gbc);
        
        // Opções
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        configPanel.add(chkIncluirQRCode, gbc);
        
        gbc.gridy = 4;
        configPanel.add(chkIncluirDescricao, gbc);
        
        // Info
        gbc.gridy = 5;
        JLabel lblInfo = new JLabel(patrimonios.size() + " patrimônio(s) selecionado(s)");
        lblInfo.setFont(new Font("Arial", Font.BOLD, 12));
        lblInfo.setForeground(new Color(52, 152, 219));
        configPanel.add(lblInfo, gbc);
        
        // Painel de preview
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBorder(new TitledBorder("Visualização"));
        JScrollPane scrollPreview = new JScrollPane(txtPreview);
        scrollPreview.setPreferredSize(new Dimension(0, 200));
        previewPanel.add(scrollPreview, BorderLayout.CENTER);
        
        // Painel de botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton btnVisualizar = new JButton("Visualizar");
        btnVisualizar.addActionListener(e -> visualizarEtiquetas());
        
        JButton btnImprimir = new JButton("Imprimir");
        btnImprimir.addActionListener(e -> imprimirEtiquetas());
        btnImprimir.setBackground(new Color(52, 152, 219));
        btnImprimir.setForeground(Color.WHITE);
        btnImprimir.setFocusPainted(false);
        
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());
        
        buttonPanel.add(btnVisualizar);
        buttonPanel.add(btnImprimir);
        buttonPanel.add(btnCancelar);
        
        // Adicionar ao painel principal
        mainPanel.add(configPanel, BorderLayout.NORTH);
        mainPanel.add(previewPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Atualizar preview inicial
        atualizarPreview();
    }
    
    private void atualizarPreview() {
        StringBuilder preview = new StringBuilder();
        preview.append("═══════════════════════════════════════════════\n");
        preview.append("  PREVIEW DA ETIQUETA\n");
        preview.append("═══════════════════════════════════════════════\n\n");
        
        String formato = (String) cmbFormato.getSelectedItem();
        String tamanho = (String) cmbTamanho.getSelectedItem();
        
        preview.append("Formato: ").append(formato).append("\n");
        preview.append("Tamanho: ").append(tamanho).append("\n");
        preview.append("Cópias: ").append(spnCopias.getValue()).append("\n\n");
        
        preview.append("───────────────────────────────────────────────\n");
        preview.append("Exemplo de etiqueta:\n");
        preview.append("───────────────────────────────────────────────\n\n");
        
        if (!patrimonios.isEmpty()) {
            Patrimonio p = patrimonios.get(0);
            
            if (chkIncluirQRCode.isSelected()) {
                preview.append("  [QR CODE]\n");
                preview.append("  ▓▓▓▓▓▓▓\n");
                preview.append("  ▓     ▓\n");
                preview.append("  ▓ ▓▓▓ ▓\n");
                preview.append("  ▓     ▓\n");
                preview.append("  ▓▓▓▓▓▓▓\n\n");
            }
            
            preview.append("  Nº: ").append(p.getNumeroPatrimonio()).append("\n");
            
            if (chkIncluirDescricao.isSelected() && p.getDescricao() != null) {
                String desc = p.getDescricao();
                if (desc.length() > 40) {
                    desc = desc.substring(0, 37) + "...";
                }
                preview.append("  ").append(desc).append("\n");
            }
            
            if (p.getNomeSala() != null) {
                preview.append("  Sala: ").append(p.getNomeSala()).append("\n");
            }
        }
        
        preview.append("\n───────────────────────────────────────────────\n");
        preview.append("Total de etiquetas: ").append(patrimonios.size() * (int)spnCopias.getValue()).append("\n");
        
        txtPreview.setText(preview.toString());
        txtPreview.setCaretPosition(0);
    }
    
    private void visualizarEtiquetas() {
        try {
            EtiquetaConfig config = criarConfiguracao();
            EtiquetaPrinter printer = new EtiquetaPrinter(config);
            
            // Criar preview visual
            JDialog previewDialog = new JDialog(this, "Visualização de Etiquetas", true);
            previewDialog.setLayout(new BorderLayout());
            
            JPanel previewPanel = printer.criarPainelPreview(patrimonios);
            JScrollPane scroll = new JScrollPane(previewPanel);
            scroll.setPreferredSize(new Dimension(800, 600));
            
            JButton btnFechar = new JButton("Fechar");
            btnFechar.addActionListener(e -> previewDialog.dispose());
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.add(btnFechar);
            
            previewDialog.add(scroll, BorderLayout.CENTER);
            previewDialog.add(buttonPanel, BorderLayout.SOUTH);
            previewDialog.pack();
            previewDialog.setLocationRelativeTo(this);
            previewDialog.setVisible(true);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Erro ao gerar visualização: " + ex.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void imprimirEtiquetas() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Deseja imprimir " + (patrimonios.size() * (int)spnCopias.getValue()) + " etiqueta(s)?",
            "Confirmar Impressão",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            EtiquetaConfig config = criarConfiguracao();
            EtiquetaPrinter printer = new EtiquetaPrinter(config);
            
            // Criar lista com cópias
            List<Patrimonio> listaImpressao = new java.util.ArrayList<>();
            int copias = (int) spnCopias.getValue();
            for (int i = 0; i < copias; i++) {
                listaImpressao.addAll(patrimonios);
            }
            
            printer.imprimir(listaImpressao);
            
            impressaoConfirmada = true;
            
            JOptionPane.showMessageDialog(this,
                "Etiquetas enviadas para impressão com sucesso!",
                "Sucesso",
                JOptionPane.INFORMATION_MESSAGE);
                
            dispose();
            
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this,
                "Erro ao imprimir etiquetas: " + ex.getMessage(),
                "Erro de Impressão",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Erro inesperado: " + ex.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private EtiquetaConfig criarConfiguracao() {
        EtiquetaConfig config = new EtiquetaConfig();
        
        // Formato
        String formato = (String) cmbFormato.getSelectedItem();
        if (formato.contains("Compacto")) {
            config.setFormato(EtiquetaConfig.Formato.COMPACTO);
        } else if (formato.contains("Detalhado")) {
            config.setFormato(EtiquetaConfig.Formato.DETALHADO);
        } else {
            config.setFormato(EtiquetaConfig.Formato.PADRAO);
        }
        
        // Tamanho
        String tamanho = (String) cmbTamanho.getSelectedItem();
        if (tamanho.contains("50x30")) {
            config.setLargura(50);
            config.setAltura(30);
        } else if (tamanho.contains("60x40")) {
            config.setLargura(60);
            config.setAltura(40);
        } else if (tamanho.contains("70x50")) {
            config.setLargura(70);
            config.setAltura(50);
        } else {
            config.setLargura(210);
            config.setAltura(297);
        }
        
        config.setIncluirQRCode(chkIncluirQRCode.isSelected());
        config.setIncluirDescricao(chkIncluirDescricao.isSelected());
        
        return config;
    }
    
    public boolean isImpressaoConfirmada() {
        return impressaoConfirmada;
    }
}