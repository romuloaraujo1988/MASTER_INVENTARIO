package com.inventario.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.inventario.model.Campus;
import com.inventario.service.CampusService;
import com.inventario.view.ui.ModernButtons;

public class CampusFormDialog extends JDialog {
    private Campus campus;
    private boolean confirmado = false;
    
    // Componentes da interface
    private JTextField campoNome;
    private JTextField campoLocal;
    private JTextField campoCnpj;
    private JTextField campoCodigoUorg;
    private JTextField campoDiretor;
    private JTextField campoTelefone;
    private JTextField campoEmail;
    private JTextArea campoObservacoes;
    private JButton btnSalvar;
    private JButton btnCancelar;
    
    public CampusFormDialog(Frame parent, Campus campus) {
        super(parent, campus == null ? "Novo Campus" : "Editar Campus", true);
        this.campus = campus;
        
        initComponents();
        aplicarEstiloModerno();
        preencherCampos();
    }
    
    private void initComponents() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Painel principal
        JPanel painelPrincipal = new JPanel(new BorderLayout());
        painelPrincipal.setBackground(Color.WHITE);
        painelPrincipal.setBorder(new EmptyBorder(25, 30, 20, 30));
        
        // Título
        JLabel titulo = new JLabel(campus == null ? "📍 Novo Campus" : "📍 Editar Campus");
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        titulo.setForeground(new Color(52, 58, 64));
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        titulo.setBorder(new EmptyBorder(0, 0, 25, 0));
        painelPrincipal.add(titulo, BorderLayout.NORTH);
        
        // Painel de campos
        JPanel painelCampos = new JPanel();
        painelCampos.setLayout(new BoxLayout(painelCampos, BoxLayout.Y_AXIS));
        painelCampos.setBackground(Color.WHITE);
        
        // Campo Nome
        painelCampos.add(criarCampoFormulario("📝 Nome do Campus *", campoNome = new JTextField()));
        painelCampos.add(Box.createVerticalStrut(15));
        
        // Campo Local
        painelCampos.add(criarCampoFormulario("📍 Local *", campoLocal = new JTextField()));
        painelCampos.add(Box.createVerticalStrut(15));
        
        // Campo CNPJ
        painelCampos.add(criarCampoFormulario("🏢 CNPJ *", campoCnpj = new JTextField()));
        painelCampos.add(Box.createVerticalStrut(15));
        
        // Campo Código UOrg (importante para SIADS)
        painelCampos.add(criarCampoFormulario("🔢 Código UOrg (SIADS) *", campoCodigoUorg = new JTextField()));
        painelCampos.add(Box.createVerticalStrut(15));
        
        // Campo Diretor
        painelCampos.add(criarCampoFormulario("👤 Diretor", campoDiretor = new JTextField()));
        painelCampos.add(Box.createVerticalStrut(15));
        
        // Campo Telefone
        painelCampos.add(criarCampoFormulario("📞 Telefone", campoTelefone = new JTextField()));
        painelCampos.add(Box.createVerticalStrut(15));
        
        // Campo Email
        painelCampos.add(criarCampoFormulario("📧 Email", campoEmail = new JTextField()));
        painelCampos.add(Box.createVerticalStrut(15));
        
        // Campo Observações
        painelCampos.add(criarCampoObservacoes("📋 Observações", campoObservacoes = new JTextArea(3, 20)));
        
        painelPrincipal.add(painelCampos, BorderLayout.CENTER);
        add(painelPrincipal, BorderLayout.CENTER);
        
        // Painel de botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        painelBotoes.setBackground(new Color(245, 245, 245));
        painelBotoes.setBorder(new EmptyBorder(20, 30, 25, 30));
        
        btnSalvar = ModernButtons.primary("💾 Salvar");
        btnCancelar = ModernButtons.muted("❌ Cancelar");
        
        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnCancelar);
        add(painelBotoes, BorderLayout.SOUTH);
        
        // Configurar eventos
        configurarEventos();
        
        setSize(550, 650);
        setLocationRelativeTo(getParent());
        setResizable(false);
    }
    
    private JPanel criarCampoFormulario(String labelTexto, JTextField campo) {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(Color.WHITE);
        
        JLabel label = new JLabel(labelTexto);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        label.setForeground(new Color(73, 80, 87));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        campo.setPreferredSize(new Dimension(450, 35));
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        campo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        painel.add(label);
        painel.add(Box.createVerticalStrut(8));
        painel.add(campo);
        
        return painel;
    }
    
    private JPanel criarCampoObservacoes(String labelTexto, JTextArea campo) {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(Color.WHITE);
        
        JLabel label = new JLabel(labelTexto);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        label.setForeground(new Color(73, 80, 87));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        campo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        campo.setLineWrap(true);
        campo.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(campo);
        scrollPane.setPreferredSize(new Dimension(450, 80));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(206, 212, 218), 1));
        
        painel.add(label);
        painel.add(Box.createVerticalStrut(8));
        painel.add(scrollPane);
        
        return painel;
    }
    
    private void aplicarEstiloModerno() {
        // Adicionar efeitos de foco
        adicionarEfeitoFocus(campoNome);
        adicionarEfeitoFocus(campoLocal);
        adicionarEfeitoFocus(campoCnpj);
        adicionarEfeitoFocus(campoCodigoUorg);
        adicionarEfeitoFocus(campoDiretor);
        adicionarEfeitoFocus(campoTelefone);
        adicionarEfeitoFocus(campoEmail);
        adicionarEfeitoFocusTextArea(campoObservacoes);
    }
    

    
    private void adicionarEfeitoFocus(JTextField campo) {
        Color corNormal = new Color(206, 212, 218);
        Color corFocus = new Color(0, 123, 255);
        
        campo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                campo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(corFocus, 2),
                    new EmptyBorder(7, 11, 7, 11)
                ));
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                campo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(corNormal, 1),
                    new EmptyBorder(8, 12, 8, 12)
                ));
            }
        });
    }
    
    private void adicionarEfeitoFocusTextArea(JTextArea campo) {
        Color corNormal = new Color(206, 212, 218);
        Color corFocus = new Color(0, 123, 255);
        
        campo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                JScrollPane scrollPane = (JScrollPane) campo.getParent().getParent();
                scrollPane.setBorder(BorderFactory.createLineBorder(corFocus, 2));
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                JScrollPane scrollPane = (JScrollPane) campo.getParent().getParent();
                scrollPane.setBorder(BorderFactory.createLineBorder(corNormal, 1));
            }
        });
    }
    
    private void configurarEventos() {
        btnSalvar.addActionListener(e -> salvarCampus());
        btnCancelar.addActionListener(e -> dispose());
    }
    
    private void preencherCampos() {
        if (campus != null) {
            campoNome.setText(campus.getNome());
            campoLocal.setText(campus.getLocal());
            campoCnpj.setText(campus.getCnpj());
            campoCodigoUorg.setText(campus.getCodigoUorg());
            campoDiretor.setText(campus.getDiretor());
            campoTelefone.setText(campus.getTelefone());
            campoEmail.setText(campus.getEmail());
            campoObservacoes.setText(campus.getObservacoes());
        }
    }
    
    private void salvarCampus() {
        try {
            // Validar campos obrigatórios
            if (campoNome.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "O nome do campus é obrigatório.");
                campoNome.requestFocus();
                return;
            }
            
            if (campoLocal.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "O local do campus é obrigatório.");
                campoLocal.requestFocus();
                return;
            }
            
            if (campoCnpj.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "O CNPJ do campus é obrigatório.");
                campoCnpj.requestFocus();
                return;
            }
            
            if (campoCodigoUorg.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "O Código UOrg é obrigatório para integração com SIADS.");
                campoCodigoUorg.requestFocus();
                return;
            }
            
            // Criar ou atualizar campus
            if (campus == null) {
                campus = new Campus();
            }
            
            campus.setNome(campoNome.getText().trim());
            campus.setLocal(campoLocal.getText().trim());
            campus.setCnpj(campoCnpj.getText().trim());
            campus.setCodigoUorg(campoCodigoUorg.getText().trim());
            campus.setDiretor(campoDiretor.getText().trim());
            campus.setTelefone(campoTelefone.getText().trim());
            campus.setEmail(campoEmail.getText().trim());
            campus.setObservacoes(campoObservacoes.getText().trim());
            
            // Usar o serviço para validação e salvamento
            CampusService campusService = new CampusService();
            
            boolean sucesso;
            if (campus.getId() == 0) {
                // Novo campus
                Integer novoId = campusService.inserirCampus(campus);
                sucesso = (novoId != null);
                if (sucesso) {
                    campus.setId(novoId);
                    JOptionPane.showMessageDialog(this, "Campus cadastrado com sucesso!");
                }
            } else {
                // Atualizar campus existente
                sucesso = campusService.atualizarCampus(campus);
                if (sucesso) {
                    JOptionPane.showMessageDialog(this, "Campus atualizado com sucesso!");
                }
            }
            
            if (!sucesso) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar campus.");
                return;
            }
            
            confirmado = true;
            dispose();
            
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar campus: " + e.getMessage());
        }
    }
    
    public boolean isConfirmado() {
        return confirmado;
    }
    
    public Campus getCampus() {
        return campus;
    }
}