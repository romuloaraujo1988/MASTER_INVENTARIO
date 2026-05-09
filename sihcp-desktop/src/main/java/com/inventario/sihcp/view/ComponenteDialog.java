package com.inventario.sihcp.view;

import com.inventario.sihcp.view.ItemCompostoFrame.ComponenteItem;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Dialog para adicionar ou editar componentes de um item composto
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class ComponenteDialog extends JDialog {
    
    private JComboBox<String> cmbTipo;
    private JTextField txtDescricao;
    private JSpinner spnQuantidade;
    private JSpinner spnOrdem;
    
    private JButton btnConfirmar;
    private JButton btnCancelar;
    
    private boolean confirmado = false;
    private ComponenteItem componente;
    private boolean isEdicao;
    
    // Tipos de componentes predefinidos
    private static final String[] TIPOS_COMPONENTES = {
        "CADEIRA",
        "MESA",
        "GAVETA",
        "PRATELEIRA",
        "PORTA",
        "RODIZIO",
        "TAMPO",
        "PÉ",
        "ASSENTO",
        "ENCOSTO",
        "BRAÇO",
        "SUPORTE",
        "PARAFUSO",
        "DOBRADIÇA",
        "PUXADOR",
        "OUTRO"
    };
    
    public ComponenteDialog(Frame parent, ComponenteItem componenteExistente) {
        super(parent, componenteExistente == null ? "Adicionar Componente" : "Editar Componente", true);
        
        this.componente = componenteExistente;
        this.isEdicao = componenteExistente != null;
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
        
        if (isEdicao) {
            carregarDados();
        }
        
        setSize(500, 350);
        setLocationRelativeTo(parent);
        setResizable(false);
    }
    
    private void initializeComponents() {
        // ComboBox de tipos
        cmbTipo = new JComboBox<>(TIPOS_COMPONENTES);
        cmbTipo.setFont(new Font("Arial", Font.PLAIN, 13));
        cmbTipo.setEditable(true); // Permite digitar tipo personalizado
        
        // Campo de descrição
        txtDescricao = new JTextField(30);
        txtDescricao.setFont(new Font("Arial", Font.PLAIN, 13));
        
        // Spinner de quantidade
        SpinnerNumberModel modelQuantidade = new SpinnerNumberModel(1, 1, 999, 1);
        spnQuantidade = new JSpinner(modelQuantidade);
        spnQuantidade.setFont(new Font("Arial", Font.PLAIN, 13));
        ((JSpinner.DefaultEditor) spnQuantidade.getEditor()).getTextField().setColumns(5);
        
        // Spinner de ordem
        SpinnerNumberModel modelOrdem = new SpinnerNumberModel(1, 1, 99, 1);
        spnOrdem = new JSpinner(modelOrdem);
        spnOrdem.setFont(new Font("Arial", Font.PLAIN, 13));
        ((JSpinner.DefaultEditor) spnOrdem.getEditor()).getTextField().setColumns(5);
        
        // Botões
        btnConfirmar = createModernButton("✓ Confirmar", new Color(39, 174, 96));
        btnCancelar = createModernButton("✗ Cancelar", new Color(192, 57, 43));
    }
    
    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // Painel de formulário
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Tipo
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        formPanel.add(createLabel("Tipo:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        formPanel.add(cmbTipo, gbc);
        
        // Descrição
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(createLabel("Descrição:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        formPanel.add(txtDescricao, gbc);
        
        // Quantidade Esperada
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        formPanel.add(createLabel("Quantidade Esperada:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        formPanel.add(spnQuantidade, gbc);
        
        // Ordem
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        formPanel.add(createLabel("Ordem de Exibição:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        formPanel.add(spnOrdem, gbc);
        
        // Painel de informações
        JPanel infoPanel = createInfoPanel();
        
        // Painel de botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(btnConfirmar);
        buttonPanel.add(btnCancelar);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(infoPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(236, 240, 241));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel lblInfo = new JLabel(
            "<html>" +
            "<b>ℹ️ Informações:</b><br>" +
            "• <b>Tipo:</b> Categoria do componente (ex: CADEIRA, MESA)<br>" +
            "• <b>Descrição:</b> Detalhes específicos do componente<br>" +
            "• <b>Quantidade:</b> Número esperado deste componente<br>" +
            "• <b>Ordem:</b> Ordem de exibição na lista" +
            "</html>"
        );
        lblInfo.setFont(new Font("Arial", Font.PLAIN, 11));
        
        panel.add(lblInfo);
        
        return panel;
    }
    
    private void setupEventListeners() {
        btnConfirmar.addActionListener(e -> confirmar());
        btnCancelar.addActionListener(e -> cancelar());
        
        // Enter no campo de descrição confirma
        txtDescricao.addActionListener(e -> confirmar());
        
        // ESC cancela
        getRootPane().registerKeyboardAction(
            e -> cancelar(),
            KeyStroke.getKeyStroke("ESCAPE"),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }
    
    private void carregarDados() {
        if (componente != null) {
            cmbTipo.setSelectedItem(componente.getTipo());
            txtDescricao.setText(componente.getDescricao());
            spnQuantidade.setValue(componente.getQuantidadeEsperada());
            spnOrdem.setValue(componente.getOrdem());
        }
    }
    
    private void confirmar() {
        // Validações
        String tipo = cmbTipo.getSelectedItem().toString().trim();
        if (tipo.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Selecione ou digite um tipo de componente.",
                "Campo Obrigatório",
                JOptionPane.WARNING_MESSAGE);
            cmbTipo.requestFocus();
            return;
        }
        
        String descricao = txtDescricao.getText().trim();
        if (descricao.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Digite uma descrição para o componente.",
                "Campo Obrigatório",
                JOptionPane.WARNING_MESSAGE);
            txtDescricao.requestFocus();
            return;
        }
        
        if (descricao.length() < 3) {
            JOptionPane.showMessageDialog(this,
                "A descrição deve ter no mínimo 3 caracteres.",
                "Validação",
                JOptionPane.WARNING_MESSAGE);
            txtDescricao.requestFocus();
            return;
        }
        
        int quantidade = (Integer) spnQuantidade.getValue();
        int ordem = (Integer) spnOrdem.getValue();
        
        // Criar ou atualizar componente
        componente = new ComponenteItem(
            tipo.toUpperCase(),
            descricao,
            quantidade,
            ordem
        );
        
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
    
    public ComponenteItem getComponente() {
        return componente;
    }
    
    // Métodos auxiliares
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(new Color(52, 73, 94));
        return label;
    }
    
    private JButton createModernButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(130, 35));
        
        // Efeito hover
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
