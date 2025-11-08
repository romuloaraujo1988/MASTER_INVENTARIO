package com.inventario.view;

import com.inventario.service.ServiceFactory;
import com.inventario.service.UsuarioService;
import com.inventario.util.PasswordUtil;
import com.inventario.view.ui.ModernButtons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Diálogo para alteração de senha de usuários
 * Permite que administradores alterem senhas de outros usuários
 * ou que usuários alterem suas próprias senhas
 */
public class AlterarSenhaDialog extends JDialog {
    
    private static final long serialVersionUID = 1L;
    
    private JPasswordField txtSenhaAtual;
    private JPasswordField txtNovaSenha;
    private JPasswordField txtConfirmarSenha;
    private JButton btnSalvar;
    private JButton btnCancelar;
    private JLabel lblForcaSenha;
    private JProgressBar barForcaSenha;
    
    private UsuarioService usuarioService;
    private Integer idUsuario;
    private String nomeUsuario;
    private boolean isAdmin;
    private boolean senhaSalva = false;
    
    /**
     * Construtor para administrador alterando senha de outro usuário
     */
    public AlterarSenhaDialog(Frame parent, Integer idUsuario, String nomeUsuario) {
        super(parent, "Alterar Senha - " + nomeUsuario, true);
        this.idUsuario = idUsuario;
        this.nomeUsuario = nomeUsuario;
        this.usuarioService = ServiceFactory.getInstance().getUsuarioService();
        this.isAdmin = true;
        
        initComponents();
        setupLayout();
        setupEventListeners();
        
        setSize(450, 350);
        setLocationRelativeTo(parent);
        setResizable(false);
    }
    
    /**
     * Construtor para usuário alterando sua própria senha
     */
    public AlterarSenhaDialog(Frame parent, Integer idUsuario, String nomeUsuario, boolean requireSenhaAtual) {
        super(parent, "Alterar Minha Senha", true);
        this.idUsuario = idUsuario;
        this.nomeUsuario = nomeUsuario;
        this.usuarioService = ServiceFactory.getInstance().getUsuarioService();
        this.isAdmin = !requireSenhaAtual;
        
        initComponents();
        setupLayout();
        setupEventListeners();
        
        setSize(450, isAdmin ? 350 : 400);
        setLocationRelativeTo(parent);
        setResizable(false);
    }
    
    private void initComponents() {
        txtSenhaAtual = new JPasswordField();
        txtNovaSenha = new JPasswordField();
        txtConfirmarSenha = new JPasswordField();
        
        btnSalvar = ModernButtons.primary("Salvar");
        btnCancelar = ModernButtons.muted("Cancelar");
        
        lblForcaSenha = new JLabel("Força da senha: ");
        barForcaSenha = new JProgressBar(0, 100);
        barForcaSenha.setStringPainted(true);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Painel principal
        JPanel panelMain = new JPanel();
        panelMain.setLayout(new BoxLayout(panelMain, BoxLayout.Y_AXIS));
        panelMain.setBorder(new EmptyBorder(20, 30, 20, 30));
        panelMain.setBackground(Color.WHITE);
        
        // Título
        JLabel lblTitulo = new JLabel(isAdmin ? "Alterar senha do usuário" : "Alterar minha senha");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelMain.add(lblTitulo);
        
        panelMain.add(Box.createVerticalStrut(5));
        
        JLabel lblUsuario = new JLabel("Usuário: " + nomeUsuario);
        lblUsuario.setFont(new Font("Arial", Font.PLAIN, 12));
        lblUsuario.setForeground(new Color(108, 117, 125));
        lblUsuario.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelMain.add(lblUsuario);
        
        panelMain.add(Box.createVerticalStrut(20));
        
        // Campo senha atual (apenas se não for admin)
        if (!isAdmin) {
            JLabel lblSenhaAtual = new JLabel("Senha Atual:");
            lblSenhaAtual.setFont(new Font("Arial", Font.BOLD, 12));
            lblSenhaAtual.setAlignmentX(Component.LEFT_ALIGNMENT);
            panelMain.add(lblSenhaAtual);
            
            panelMain.add(Box.createVerticalStrut(5));
            
            txtSenhaAtual.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            txtSenhaAtual.setFont(new Font("Arial", Font.PLAIN, 14));
            txtSenhaAtual.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(5, 10, 5, 10)
            ));
            panelMain.add(txtSenhaAtual);
            
            panelMain.add(Box.createVerticalStrut(15));
        }
        
        // Campo nova senha
        JLabel lblNovaSenha = new JLabel("Nova Senha:");
        lblNovaSenha.setFont(new Font("Arial", Font.BOLD, 12));
        lblNovaSenha.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelMain.add(lblNovaSenha);
        
        panelMain.add(Box.createVerticalStrut(5));
        
        txtNovaSenha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        txtNovaSenha.setFont(new Font("Arial", Font.PLAIN, 14));
        txtNovaSenha.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(5, 10, 5, 10)
        ));
        panelMain.add(txtNovaSenha);
        
        panelMain.add(Box.createVerticalStrut(15));
        
        // Campo confirmar senha
        JLabel lblConfirmarSenha = new JLabel("Confirmar Nova Senha:");
        lblConfirmarSenha.setFont(new Font("Arial", Font.BOLD, 12));
        lblConfirmarSenha.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelMain.add(lblConfirmarSenha);
        
        panelMain.add(Box.createVerticalStrut(5));
        
        txtConfirmarSenha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        txtConfirmarSenha.setFont(new Font("Arial", Font.PLAIN, 14));
        txtConfirmarSenha.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(5, 10, 5, 10)
        ));
        panelMain.add(txtConfirmarSenha);
        
        panelMain.add(Box.createVerticalStrut(15));
        
        // Indicador de força da senha
        lblForcaSenha.setFont(new Font("Arial", Font.PLAIN, 11));
        lblForcaSenha.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelMain.add(lblForcaSenha);
        
        panelMain.add(Box.createVerticalStrut(5));
        
        barForcaSenha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        barForcaSenha.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelMain.add(barForcaSenha);
        
        panelMain.add(Box.createVerticalStrut(10));
        
        // Requisitos da senha
        JLabel lblRequisitos = new JLabel("<html><small>A senha deve ter no mínimo 6 caracteres, incluindo letras e números.</small></html>");
        lblRequisitos.setFont(new Font("Arial", Font.PLAIN, 10));
        lblRequisitos.setForeground(new Color(108, 117, 125));
        lblRequisitos.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelMain.add(lblRequisitos);
        
        // Painel de botões
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotoes.setBackground(new Color(248, 249, 250));
        panelBotoes.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 226, 230)));
        
        panelBotoes.add(btnCancelar);
        panelBotoes.add(btnSalvar);
        
        add(panelMain, BorderLayout.CENTER);
        add(panelBotoes, BorderLayout.SOUTH);
    }
    
    private void setupEventListeners() {
        btnSalvar.addActionListener(e -> salvarSenha());
        btnCancelar.addActionListener(e -> dispose());
        
        // Atualizar força da senha em tempo real
        txtNovaSenha.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { atualizarForcaSenha(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { atualizarForcaSenha(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { atualizarForcaSenha(); }
        });
        
        // Enter para salvar
        txtConfirmarSenha.addActionListener(e -> salvarSenha());
    }
    
    private void atualizarForcaSenha() {
        String senha = new String(txtNovaSenha.getPassword());
        int forca = calcularForcaSenha(senha);
        
        barForcaSenha.setValue(forca);
        
        if (forca < 30) {
            lblForcaSenha.setText("Força da senha: Fraca");
            barForcaSenha.setForeground(new Color(220, 53, 69));
        } else if (forca < 60) {
            lblForcaSenha.setText("Força da senha: Média");
            barForcaSenha.setForeground(new Color(255, 193, 7));
        } else {
            lblForcaSenha.setText("Força da senha: Forte");
            barForcaSenha.setForeground(new Color(40, 167, 69));
        }
    }
    
    private int calcularForcaSenha(String senha) {
        if (senha.isEmpty()) return 0;
        
        int forca = 0;
        
        // Comprimento
        forca += Math.min(senha.length() * 4, 40);
        
        // Letras maiúsculas
        if (senha.matches(".*[A-Z].*")) forca += 10;
        
        // Letras minúsculas
        if (senha.matches(".*[a-z].*")) forca += 10;
        
        // Números
        if (senha.matches(".*[0-9].*")) forca += 15;
        
        // Caracteres especiais
        if (senha.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) forca += 25;
        
        return Math.min(forca, 100);
    }
    
    private void salvarSenha() {
        try {
            // Validar senha atual (se não for admin)
            if (!isAdmin) {
                String senhaAtual = new String(txtSenhaAtual.getPassword());
                if (senhaAtual.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                        "Por favor, informe a senha atual.",
                        "Campo Obrigatório", JOptionPane.WARNING_MESSAGE);
                    txtSenhaAtual.requestFocus();
                    return;
                }
                
                // Verificar senha atual no banco
                var usuario = usuarioService.buscarPorId(idUsuario);
                if (usuario == null || !PasswordUtil.verifyPassword(senhaAtual, usuario.getSenhaHash())) {
                    JOptionPane.showMessageDialog(this,
                        "Senha atual incorreta.",
                        "Erro de Autenticação", JOptionPane.ERROR_MESSAGE);
                    txtSenhaAtual.setText("");
                    txtSenhaAtual.requestFocus();
                    return;
                }
            }
            
            // Validar nova senha
            String novaSenha = new String(txtNovaSenha.getPassword());
            String confirmarSenha = new String(txtConfirmarSenha.getPassword());
            
            if (novaSenha.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Por favor, informe a nova senha.",
                    "Campo Obrigatório", JOptionPane.WARNING_MESSAGE);
                txtNovaSenha.requestFocus();
                return;
            }
            
            if (novaSenha.length() < 6) {
                JOptionPane.showMessageDialog(this,
                    "A senha deve ter no mínimo 6 caracteres.",
                    "Senha Inválida", JOptionPane.WARNING_MESSAGE);
                txtNovaSenha.requestFocus();
                return;
            }
            
            // Validar se contém letras e números
            if (!novaSenha.matches(".*[a-zA-Z].*") || !novaSenha.matches(".*[0-9].*")) {
                JOptionPane.showMessageDialog(this,
                    "A senha deve conter pelo menos uma letra e um número.",
                    "Senha Inválida", JOptionPane.WARNING_MESSAGE);
                txtNovaSenha.requestFocus();
                return;
            }
            
            if (!novaSenha.equals(confirmarSenha)) {
                JOptionPane.showMessageDialog(this,
                    "As senhas não coincidem.",
                    "Erro de Confirmação", JOptionPane.ERROR_MESSAGE);
                txtConfirmarSenha.setText("");
                txtConfirmarSenha.requestFocus();
                return;
            }
            
            // Gerar hash da nova senha usando BCrypt
            String novaSenhaHash = PasswordUtil.hashPassword(novaSenha);
            
            // Atualizar senha no banco
            try {
                usuarioService.atualizarSenha(idUsuario, novaSenhaHash);
                JOptionPane.showMessageDialog(this,
                    "Senha alterada com sucesso!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                senhaSalva = true;
                dispose();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Erro ao atualizar senha no banco de dados: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erro ao alterar senha: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    

    
    public boolean isSenhaSalva() {
        return senhaSalva;
    }
}
