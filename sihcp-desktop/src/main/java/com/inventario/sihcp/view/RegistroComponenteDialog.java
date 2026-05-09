package com.inventario.sihcp.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import com.inventario.sihcp.view.ColetaItemCompostoFrame.ComponenteColeta;

/**
 * Dialog para registrar a coleta de um componente específico
 * 
 * @author Sistema de Inventário
 * @version 1.1.0 - Adicionado campo de localização
 */
public class RegistroComponenteDialog extends JDialog {
    
    private ComponenteColeta componente;
    private JSpinner spnQuantidade;
    private JTextField txtLocalizacao;
    private JTextArea txtObservacao;
    private JButton btnConfirmar;
    private JButton btnCancelar;
    private boolean confirmado = false;
    
    // Localização pré-definida (vem do frame principal)
    private String localizacaoPadrao;
    
    public RegistroComponenteDialog(Frame parent, ComponenteColeta componente) {
        this(parent, componente, null);
    }
    
    /**
     * Construtor com localização pré-definida
     */
    public RegistroComponenteDialog(Frame parent, ComponenteColeta componente, String localizacaoPadrao) {
        super(parent, "Registrar Componente", true);
        
        this.componente = componente;
        this.localizacaoPadrao = localizacaoPadrao;
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
        
        setSize(520, 450);
        setLocationRelativeTo(parent);
        setResizable(false);
    }
    
    private void initializeComponents() {
        // Spinner de quantidade - INICIA COM 1 (não 0) para facilitar coleta
        int valorInicial = componente.getQuantidadeEncontrada() > 0 
            ? componente.getQuantidadeEncontrada() 
            : 1;  // Começa com 1 por padrão
        
        SpinnerNumberModel model = new SpinnerNumberModel(
            valorInicial, 
            0, 
            componente.getQuantidadeEsperada() * 2, 
            1
        );
        spnQuantidade = new JSpinner(model);
        spnQuantidade.setFont(new Font("Arial", Font.BOLD, 16));
        ((JSpinner.DefaultEditor) spnQuantidade.getEditor()).getTextField().setColumns(5);
        
        // Campo de localização onde o componente foi encontrado
        txtLocalizacao = new JTextField(30);
        txtLocalizacao.setFont(new Font("Arial", Font.PLAIN, 12));
        txtLocalizacao.setToolTipText("Informe onde este componente foi encontrado (ex: Sala 101, Bloco A)");
        
        // Se tem localização pré-definida, usar
        if (localizacaoPadrao != null && !localizacaoPadrao.isEmpty()) {
            txtLocalizacao.setText(localizacaoPadrao);
        } else if (componente.getLocalizacaoEncontrada() != null) {
            txtLocalizacao.setText(componente.getLocalizacaoEncontrada());
        }
        
        // Área de observação
        txtObservacao = new JTextArea(3, 30);
        txtObservacao.setFont(new Font("Arial", Font.PLAIN, 12));
        txtObservacao.setLineWrap(true);
        txtObservacao.setWrapStyleWord(true);
        if (componente.getObservacao() != null) {
            txtObservacao.setText(componente.getObservacao());
        }
        
        // Botões
        btnConfirmar = createButton("✓ Confirmar", new Color(39, 174, 96));
        btnCancelar = createButton("✗ Cancelar", new Color(192, 57, 43));
    }
    
    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // Painel de informações do componente
        JPanel panelInfo = createPanelInfo();
        
        // Painel de formulário
        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Quantidade encontrada
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panelForm.add(createLabel("Quantidade Encontrada:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panelForm.add(spnQuantidade, gbc);
        
        // Localização encontrada
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel lblLoc = createLabel("📍 Local Encontrado:");
        lblLoc.setForeground(new Color(155, 89, 182));
        panelForm.add(lblLoc, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panelForm.add(txtLocalizacao, gbc);
        
        // Dica de localização
        gbc.gridx = 1; gbc.gridy = 2;
        JLabel lblDica = new JLabel("💡 Ex: Sala 101, Bloco A, Laboratório");
        lblDica.setFont(new Font("Segoe UI Emoji", Font.ITALIC, 10));
        lblDica.setForeground(new Color(127, 140, 141));
        panelForm.add(lblDica, gbc);
        
        // Observação
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panelForm.add(createLabel("Observação:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        JScrollPane scrollObs = new JScrollPane(txtObservacao);
        scrollObs.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));
        panelForm.add(scrollObs, gbc);
        
        // Painel de botões
        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelButtons.setBackground(Color.WHITE);
        panelButtons.add(btnConfirmar);
        panelButtons.add(btnCancelar);
        
        mainPanel.add(panelInfo, BorderLayout.NORTH);
        mainPanel.add(panelForm, BorderLayout.CENTER);
        mainPanel.add(panelButtons, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private JPanel createPanelInfo() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5));
        panel.setBackground(new Color(236, 240, 241));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel lblTipo = new JLabel("📦 Tipo: " + componente.getTipo());
        lblTipo.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        
        JLabel lblDescricao = new JLabel("📝 Descrição: " + componente.getDescricao());
        lblDescricao.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        
        JLabel lblEsperado = new JLabel("🎯 Quantidade Esperada: " + componente.getQuantidadeEsperada());
        lblEsperado.setFont(new Font("Arial", Font.PLAIN, 12));
        
        panel.add(lblTipo);
        panel.add(lblDescricao);
        panel.add(lblEsperado);
        
        return panel;
    }
    
    private void setupEventListeners() {
        btnConfirmar.addActionListener(e -> confirmar());
        btnCancelar.addActionListener(e -> cancelar());
        
        // ESC cancela
        getRootPane().registerKeyboardAction(
            e -> cancelar(),
            KeyStroke.getKeyStroke("ESCAPE"),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        // ENTER confirma
        getRootPane().setDefaultButton(btnConfirmar);
    }
    
    private void confirmar() {
        int quantidade = (Integer) spnQuantidade.getValue();
        
        if (quantidade > componente.getQuantidadeEsperada() * 2) {
            int opcao = JOptionPane.showConfirmDialog(this,
                "A quantidade encontrada (" + quantidade + ") é muito maior que a esperada (" + 
                componente.getQuantidadeEsperada() + ").\n" +
                "Deseja confirmar mesmo assim?",
                "Confirmar Quantidade",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (opcao != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        confirmado = true;
        dispose();
    }
    
    private void cancelar() {
        confirmado = false;
        dispose();
    }
    
    public boolean isConfirmado() {
        return confirmado;
    }
    
    public int getQuantidadeEncontrada() {
        return (Integer) spnQuantidade.getValue();
    }
    
    public String getLocalizacaoEncontrada() {
        return txtLocalizacao.getText().trim();
    }
    
    public String getObservacao() {
        return txtObservacao.getText().trim();
    }
    
    // Métodos auxiliares
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        // Usar Segoe UI Emoji para suportar emojis nas labels
        label.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        label.setForeground(new Color(52, 73, 94));
        return label;
    }
    
    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        // Usar Segoe UI Emoji para suportar emojis nos botões
        button.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(130, 35));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
}
