package com.inventario.siads.view;

import com.inventario.siads.config.SiadsConfig;
import javax.swing.*;
import java.awt.*;

/**
 * Dialog para configuração dos códigos institucionais SIADS
 */
public class SiadsConfigDialog extends JDialog {
    
    private final SiadsConfig config;
    
    private JTextField txtCodigoOrgao;
    private JTextField txtCodigoUG;
    private JTextField txtCpfResponsavel;
    private JTextField txtNomeInstituicao;
    private JButton btnSalvar;
    private JButton btnCancelar;
    
    public SiadsConfigDialog(Frame parent, SiadsConfig config) {
        super(parent, "Configurações SIADS", true);
        this.config = config;
        
        initComponents();
        carregarDados();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(600, 400);
        setLocationRelativeTo(getParent());
        
        // Painel de informações
        JPanel panelInfo = new JPanel(new BorderLayout());
        panelInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextArea txtInfo = new JTextArea(
            "Configure os códigos institucionais necessários para exportação SIADS.\n\n" +
            "Estes códigos devem ser obtidos junto ao órgão responsável pelo SIADS.\n" +
            "Consulte o manual ou entre em contato com o suporte técnico."
        );
        txtInfo.setEditable(false);
        txtInfo.setWrapStyleWord(true);
        txtInfo.setLineWrap(true);
        txtInfo.setBackground(new Color(255, 255, 200));
        txtInfo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.ORANGE),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        panelInfo.add(txtInfo, BorderLayout.CENTER);
        
        add(panelInfo, BorderLayout.NORTH);
        
        // Painel de campos
        JPanel panelCampos = new JPanel(new GridBagLayout());
        panelCampos.setBorder(BorderFactory.createTitledBorder("Dados Institucionais"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Nome da Instituição
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panelCampos.add(new JLabel("Nome da Instituição:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtNomeInstituicao = new JTextField(30);
        panelCampos.add(txtNomeInstituicao, gbc);
        
        // Código do Órgão
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        JLabel lblOrgao = new JLabel("Código do Órgão:*");
        lblOrgao.setToolTipText("Código fornecido pelo governo federal (ex: 25000)");
        panelCampos.add(lblOrgao, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtCodigoOrgao = new JTextField(10);
        txtCodigoOrgao.setToolTipText("Exemplo: 25000");
        panelCampos.add(txtCodigoOrgao, gbc);
        
        // Código da UG
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        JLabel lblUG = new JLabel("Código da Unidade Gestora:*");
        lblUG.setToolTipText("Código da UG (ex: 158000)");
        panelCampos.add(lblUG, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtCodigoUG = new JTextField(10);
        txtCodigoUG.setToolTipText("Exemplo: 158000");
        panelCampos.add(txtCodigoUG, gbc);
        
        // CPF do Responsável
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        JLabel lblCPF = new JLabel("CPF do Responsável:*");
        lblCPF.setToolTipText("CPF do servidor responsável (11 dígitos)");
        panelCampos.add(lblCPF, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtCpfResponsavel = new JTextField(15);
        txtCpfResponsavel.setToolTipText("Apenas números, 11 dígitos");
        panelCampos.add(txtCpfResponsavel, gbc);
        
        // Nota de campos obrigatórios
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        JLabel lblNota = new JLabel("* Campos obrigatórios");
        lblNota.setFont(lblNota.getFont().deriveFont(Font.ITALIC));
        lblNota.setForeground(Color.GRAY);
        panelCampos.add(lblNota, gbc);
        
        add(panelCampos, BorderLayout.CENTER);
        
        // Painel de botões
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        btnSalvar = new JButton("Salvar");
        btnSalvar.addActionListener(e -> salvar());
        panelBotoes.add(btnSalvar);
        
        btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());
        panelBotoes.add(btnCancelar);
        
        add(panelBotoes, BorderLayout.SOUTH);
    }
    
    private void carregarDados() {
        txtNomeInstituicao.setText(config.getNomeInstituicao());
        txtCodigoOrgao.setText(config.getCodigoOrgao());
        txtCodigoUG.setText(config.getCodigoUG());
        txtCpfResponsavel.setText(config.getCpfResponsavel());
    }
    
    private void salvar() {
        // Validar campos obrigatórios
        if (txtCodigoOrgao.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "O código do órgão é obrigatório.",
                "Validação",
                JOptionPane.WARNING_MESSAGE
            );
            txtCodigoOrgao.requestFocus();
            return;
        }
        
        if (txtCodigoUG.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "O código da Unidade Gestora é obrigatório.",
                "Validação",
                JOptionPane.WARNING_MESSAGE
            );
            txtCodigoUG.requestFocus();
            return;
        }
        
        String cpf = txtCpfResponsavel.getText().replaceAll("[^0-9]", "");
        if (cpf.length() != 11) {
            JOptionPane.showMessageDialog(
                this,
                "O CPF deve conter 11 dígitos.",
                "Validação",
                JOptionPane.WARNING_MESSAGE
            );
            txtCpfResponsavel.requestFocus();
            return;
        }
        
        // Salvar configurações
        config.setNomeInstituicao(txtNomeInstituicao.getText().trim());
        config.setCodigoOrgao(txtCodigoOrgao.getText().trim());
        config.setCodigoUG(txtCodigoUG.getText().trim());
        config.setCpfResponsavel(cpf);
        
        try {
            config.salvarConfiguracao();
            JOptionPane.showMessageDialog(
                this,
                "Configurações salvas com sucesso!",
                "Sucesso",
                JOptionPane.INFORMATION_MESSAGE
            );
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Erro ao salvar configurações:\n" + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
