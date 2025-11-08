package com.inventario.view;

import com.inventario.dao.UsuarioDAORefactored;
import com.inventario.dao.SetorDAORefactored;
import com.inventario.model.Usuario;
import com.inventario.model.PerfilUsuario;
import com.inventario.model.Setor;
import com.inventario.util.PasswordUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Dialog para criação e edição de usuários
 */
public class UsuarioFormDialog extends JDialog {
    
    private static final long serialVersionUID = 1L;
    
    // Componentes do formulário
    private JTextField txtLogin;
    private JPasswordField txtSenha;
    private JPasswordField txtConfirmarSenha;
    private JTextField txtNomeCompleto;
    private JTextField txtEmail;
    private JTextField txtMatricula;
    private JComboBox<PerfilUsuario> cbPerfil;
    private JComboBox<Setor> cbSetor;
    private JCheckBox chkAtivo;
    private JCheckBox chkBloqueado;
    private JCheckBox chkPrimeiroAcesso;
    private JTextArea txtObservacoes;
    
    private JButton btnSalvar;
    private JButton btnCancelar;
    
    // Dados
    private Usuario usuario;
    private UsuarioDAORefactored usuarioDAO;
    private SetorDAORefactored setorDAO;
    private boolean usuarioSalvo = false;
    private boolean isEdicao = false;
    
    // Validação
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private static final Pattern MATRICULA_PATTERN = Pattern.compile(
        "^[A-Za-z0-9]{4,20}$"
    );
    
    public UsuarioFormDialog(Frame parent, Usuario usuario, UsuarioDAORefactored usuarioDAO, SetorDAORefactored setorDAO) {
        super(parent, true);
        
        this.usuario = usuario;
        this.usuarioDAO = usuarioDAO;
        this.setorDAO = setorDAO;
        this.isEdicao = (usuario != null);
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
        carregarSetores();
        
        if (isEdicao) {
            preencherFormulario();
            setTitle("Editar Usuário");
        } else {
            setTitle("Novo Usuário");
            this.usuario = new Usuario();
        }
        
        setSize(500, 600);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    private void initializeComponents() {
        // Campos de texto
        txtLogin = new JTextField(20);
        txtSenha = new JPasswordField(20);
        txtConfirmarSenha = new JPasswordField(20);
        txtNomeCompleto = new JTextField(30);
        txtEmail = new JTextField(30);
        txtMatricula = new JTextField(15);
        
        // ComboBoxes
        cbPerfil = new JComboBox<>(PerfilUsuario.values());
        cbSetor = new JComboBox<>();
        cbSetor.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Setor) {
                    setText(((Setor) value).getNome());
                } else if (value == null) {
                    setText("Selecione um setor...");
                }
                return this;
            }
        });
        
        // CheckBoxes
        chkAtivo = new JCheckBox("Usuário Ativo", true);
        chkBloqueado = new JCheckBox("Usuário Bloqueado", false);
        chkPrimeiroAcesso = new JCheckBox("Primeiro Acesso", true);
        
        // Área de texto para observações
        txtObservacoes = new JTextArea(4, 30);
        txtObservacoes.setLineWrap(true);
        txtObservacoes.setWrapStyleWord(true);
        
        // Botões
        btnSalvar = new JButton("Salvar");
        btnCancelar = new JButton("Cancelar");
        
        // Configurações específicas para edição
        if (isEdicao) {
            txtLogin.setEditable(false); // Login não pode ser alterado
            txtSenha.setEnabled(false);
            txtConfirmarSenha.setEnabled(false);
        }
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Painel principal com formulário
        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // Login
        gbc.gridx = 0; gbc.gridy = row;
        panelForm.add(new JLabel("Login *:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panelForm.add(txtLogin, gbc);
        row++;
        
        // Senha (apenas para novos usuários)
        if (!isEdicao) {
            gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
            panelForm.add(new JLabel("Senha *:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            panelForm.add(txtSenha, gbc);
            row++;
            
            gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
            panelForm.add(new JLabel("Confirmar Senha *:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            panelForm.add(txtConfirmarSenha, gbc);
            row++;
        }
        
        // Nome Completo
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        panelForm.add(new JLabel("Nome Completo *:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panelForm.add(txtNomeCompleto, gbc);
        row++;
        
        // Email
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        panelForm.add(new JLabel("Email *:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panelForm.add(txtEmail, gbc);
        row++;
        
        // Matrícula
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        panelForm.add(new JLabel("Matrícula:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panelForm.add(txtMatricula, gbc);
        row++;
        
        // Perfil
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        panelForm.add(new JLabel("Perfil *:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panelForm.add(cbPerfil, gbc);
        row++;
        
        // Setor
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        panelForm.add(new JLabel("Setor:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panelForm.add(cbSetor, gbc);
        row++;
        
        // CheckBoxes
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        panelForm.add(chkAtivo, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row;
        panelForm.add(chkBloqueado, gbc);
        row++;
        
        if (!isEdicao) {
            gbc.gridx = 0; gbc.gridy = row;
            panelForm.add(chkPrimeiroAcesso, gbc);
            row++;
        }
        
        // Observações
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panelForm.add(new JLabel("Observações:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        JScrollPane scrollObservacoes = new JScrollPane(txtObservacoes);
        panelForm.add(scrollObservacoes, gbc);
        
        // Painel de botões
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotoes.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        panelBotoes.add(btnCancelar);
        panelBotoes.add(btnSalvar);
        
        // Adicionar painéis ao dialog
        add(panelForm, BorderLayout.CENTER);
        add(panelBotoes, BorderLayout.SOUTH);
        
        // Adicionar nota sobre campos obrigatórios
        JLabel lblNota = new JLabel("* Campos obrigatórios");
        lblNota.setFont(lblNota.getFont().deriveFont(Font.ITALIC));
        lblNota.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
        add(lblNota, BorderLayout.NORTH);
    }
    
    private void setupEventListeners() {
        btnSalvar.addActionListener(e -> salvarUsuario());
        btnCancelar.addActionListener(e -> dispose());
        
        // Formatação automática da matrícula
        txtMatricula.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                formatarMatricula();
            }
        });
    }
    
    private void carregarSetores() {
        try {
            List<Setor> setores = setorDAO.findAll("NOME");
            
            cbSetor.removeAllItems();
            cbSetor.addItem(null); // Opção "nenhum setor"
            
            for (Setor setor : setores) {
                cbSetor.addItem(setor);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erro ao carregar setores: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void preencherFormulario() {
        if (usuario != null) {
            txtLogin.setText(usuario.getLogin());
            txtNomeCompleto.setText(usuario.getNomeCompleto());
            txtEmail.setText(usuario.getEmail());
            txtMatricula.setText(usuario.getMatricula());
            
            if (usuario.getPerfil() != null) {
                cbPerfil.setSelectedItem(usuario.getPerfil());
            }
            
            // Selecionar setor
            if (usuario.getIdSetor() != null) {
                for (int i = 0; i < cbSetor.getItemCount(); i++) {
                    Setor setor = cbSetor.getItemAt(i);
                    if (setor != null && setor.getId() == usuario.getIdSetor().intValue()) {
                        cbSetor.setSelectedItem(setor);
                        break;
                    }
                }
            }
            
            chkAtivo.setSelected(Boolean.TRUE.equals(usuario.getAtivo()));
            chkBloqueado.setSelected(Boolean.TRUE.equals(usuario.getBloqueado()));
            chkPrimeiroAcesso.setSelected(Boolean.TRUE.equals(usuario.getPrimeiroAcesso()));
            
            txtObservacoes.setText(usuario.getObservacoes());
        }
    }
    
    private void formatarMatricula() {
        String texto = txtMatricula.getText().replaceAll("[^A-Za-z0-9]", "");
        
        if (texto.length() <= 20) {
            txtMatricula.setText(texto.toUpperCase());
        }
    }
    
    private boolean validarFormulario() {
        StringBuilder erros = new StringBuilder();
        
        // Validar login
        if (txtLogin.getText().trim().isEmpty()) {
            erros.append("- Login é obrigatório\n");
        } else if (txtLogin.getText().trim().length() < 3) {
            erros.append("- Login deve ter pelo menos 3 caracteres\n");
        } else if (!isEdicao) {
            try {
                if (usuarioDAO.loginExiste(txtLogin.getText().trim())) {
                    erros.append("- Login já existe no sistema\n");
                }
            } catch (Exception e) {
                erros.append("- Erro ao verificar login: " + e.getMessage() + "\n");
            }
        }
        
        // Validar senha (apenas para novos usuários)
        if (!isEdicao) {
            String senha = new String(txtSenha.getPassword());
            String confirmarSenha = new String(txtConfirmarSenha.getPassword());
            
            if (senha.isEmpty()) {
                erros.append("- Senha é obrigatória\n");
            } else if (senha.length() < 6) {
                erros.append("- Senha deve ter pelo menos 6 caracteres\n");
            } else if (!senha.equals(confirmarSenha)) {
                erros.append("- Senhas não conferem\n");
            }
        }
        
        // Validar nome completo
        if (txtNomeCompleto.getText().trim().isEmpty()) {
            erros.append("- Nome completo é obrigatório\n");
        }
        
        // Validar email
        if (txtEmail.getText().trim().isEmpty()) {
            erros.append("- Email é obrigatório\n");
        } else if (!EMAIL_PATTERN.matcher(txtEmail.getText().trim()).matches()) {
            erros.append("- Email inválido\n");
        } else {
            try {
                if (!isEdicao && usuarioDAO.emailExiste(txtEmail.getText().trim())) {
                    erros.append("- Email já existe no sistema\n");
                } else if (isEdicao && !txtEmail.getText().trim().equals(usuario.getEmail()) && 
                           usuarioDAO.emailExiste(txtEmail.getText().trim())) {
                    erros.append("- Email já existe no sistema\n");
                }
            } catch (Exception e) {
                erros.append("- Erro ao verificar email: " + e.getMessage() + "\n");
            }
        }
        
        // Validar matrícula (se preenchida)
        if (!txtMatricula.getText().trim().isEmpty() && 
            !MATRICULA_PATTERN.matcher(txtMatricula.getText().trim()).matches()) {
            erros.append("- Matrícula inválida (4-20 caracteres alfanuméricos)\n");
        }
        
        // Validar perfil
        if (cbPerfil.getSelectedItem() == null) {
            erros.append("- Perfil é obrigatório\n");
        }
        
        if (erros.length() > 0) {
            JOptionPane.showMessageDialog(this,
                "Corrija os seguintes erros:\n\n" + erros.toString(),
                "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    private void salvarUsuario() {
        if (!validarFormulario()) {
            return;
        }
        
        try {
            // Preencher dados do usuário
            usuario.setLogin(txtLogin.getText().trim());
            usuario.setNomeCompleto(txtNomeCompleto.getText().trim());
            usuario.setEmail(txtEmail.getText().trim());
            usuario.setMatricula(txtMatricula.getText().trim().isEmpty() ? null : txtMatricula.getText().trim());
            usuario.setPerfil((PerfilUsuario) cbPerfil.getSelectedItem());
            
            Setor setorSelecionado = (Setor) cbSetor.getSelectedItem();
            usuario.setIdSetor(setorSelecionado != null ? setorSelecionado.getId() : null);
            
            usuario.setAtivo(chkAtivo.isSelected());
            usuario.setBloqueado(chkBloqueado.isSelected());
            
            if (!isEdicao) {
                usuario.setPrimeiroAcesso(chkPrimeiroAcesso.isSelected());
                // Hash da senha para novos usuários
                String senha = new String(txtSenha.getPassword());
                usuario.setSenhaHash(PasswordUtil.hashPassword(senha));
            }
            
            usuario.setObservacoes(txtObservacoes.getText().trim().isEmpty() ? 
                                 null : txtObservacoes.getText().trim());
            
            // Salvar no banco
            boolean sucesso;
            if (isEdicao) {
                sucesso = usuarioDAO.atualizarUsuario(usuario);
            } else {
                sucesso = usuarioDAO.inserirUsuario(usuario);
            }
            
            if (sucesso) {
                usuarioSalvo = true;
                JOptionPane.showMessageDialog(this,
                    isEdicao ? "Usuário atualizado com sucesso!" : "Usuário criado com sucesso!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Erro ao salvar usuário. Tente novamente.",
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erro ao salvar usuário: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean isUsuarioSalvo() {
        return usuarioSalvo;
    }
}