package com.inventario.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.inventario.model.PerfilUsuario;
import com.inventario.model.Usuario;
import com.inventario.util.DateFormatUtils;
import com.inventario.view.ui.ButtonStyleFactory;

/**
 * Frame para gerenciamento de usuários do sistema
 * Permite criar, editar, excluir e visualizar usuários
 */
public class UsuarioFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    // Componentes da interface
    private JTable tabelaUsuarios;
    private DefaultTableModel modeloTabela;
    private JTextField txtFiltro;
    private JComboBox<String> cbFiltroTipo;
    private JButton btnNovo, btnEditar, btnExcluir;
    private JButton btnBloquear, btnDesbloquear, btnAlterarSenha;
    private JLabel lblStatus;

    // === SWING: DAOs (sem Spring) ===
    private final com.inventario.dao.UsuarioDAO usuarioDAO;
    private final com.inventario.dao.SetorDAO setorDAO;

    public UsuarioFrame() {
        // === SWING: Usar DAOs diretamente (sem Spring) ===
        this.usuarioDAO = new com.inventario.dao.UsuarioDAO();
        this.setorDAO = new com.inventario.dao.SetorDAO();
        initializeComponents();
        setupLayout();
        aplicarEstiloModerno();
        setupEventListeners();

        setTitle("Gerenciamento de Usuários - SIHCP");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void initializeComponents() {
        // Painel de filtros
        txtFiltro = new JTextField(20);
        cbFiltroTipo = new JComboBox<>(new String[] { "Todos", "Nome", "Login", "Email", "Perfil", "Setor" });

        // Botões de ação usando ButtonStyleFactory
        // Configuração de estilo para botões
        Font btnFont = new Font(Font.SANS_SERIF, Font.BOLD, 11);
        Dimension btnSize = new Dimension(110, 30);

        // Botões de ação usando ButtonStyleFactory
        btnNovo = ButtonStyleFactory.createPrimaryButton("Novo");
        btnNovo.setFont(btnFont);
        btnNovo.setPreferredSize(new Dimension(80, 30));

        btnEditar = ButtonStyleFactory.createInfoButton("Editar");
        btnEditar.setFont(btnFont);
        btnEditar.setPreferredSize(new Dimension(80, 30));

        btnExcluir = ButtonStyleFactory.createDangerButton("Excluir");
        btnExcluir.setFont(btnFont);
        btnExcluir.setPreferredSize(new Dimension(80, 30));

        btnBloquear = ButtonStyleFactory.createWarningButton("Bloquear");
        btnBloquear.setFont(btnFont);
        btnBloquear.setPreferredSize(btnSize);

        btnDesbloquear = ButtonStyleFactory.createSuccessButton("Desbloquear");
        btnDesbloquear.setFont(btnFont);
        btnDesbloquear.setPreferredSize(btnSize);

        btnAlterarSenha = ButtonStyleFactory.createInfoButton("Alterar Senha");
        btnAlterarSenha.setFont(btnFont);
        btnAlterarSenha.setPreferredSize(new Dimension(120, 30));

        // Ícones removidos para evitar erro de 'location is null'
        // Os botões funcionarão apenas com texto

        // Tabela de usuários
        String[] colunas = {
                "ID", "Login", "Nome Completo", "Email", "Matrícula", "Perfil",
                "Setor", "Ativo", "Bloqueado", "Primeiro Acesso", "Data Criação"
        };

        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabela não editável
            }
        };

        tabelaUsuarios = new JTable(modeloTabela);
        tabelaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaUsuarios.setAutoCreateRowSorter(true);
        tabelaUsuarios.setRowHeight(25);

        // Configurar larguras das colunas
        tabelaUsuarios.getColumnModel().getColumn(0).setPreferredWidth(50); // ID
        tabelaUsuarios.getColumnModel().getColumn(1).setPreferredWidth(100); // Login
        tabelaUsuarios.getColumnModel().getColumn(2).setPreferredWidth(200); // Nome
        tabelaUsuarios.getColumnModel().getColumn(3).setPreferredWidth(180); // Email
        tabelaUsuarios.getColumnModel().getColumn(4).setPreferredWidth(120); // Matrícula
        tabelaUsuarios.getColumnModel().getColumn(5).setPreferredWidth(100); // Perfil
        tabelaUsuarios.getColumnModel().getColumn(6).setPreferredWidth(150); // Setor
        tabelaUsuarios.getColumnModel().getColumn(7).setPreferredWidth(60); // Ativo
        tabelaUsuarios.getColumnModel().getColumn(8).setPreferredWidth(80); // Bloqueado
        tabelaUsuarios.getColumnModel().getColumn(9).setPreferredWidth(130); // Último Acesso
        tabelaUsuarios.getColumnModel().getColumn(10).setPreferredWidth(130); // Data Criação

        // Label de status
        lblStatus = new JLabel("Pronto");
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Painel superior com filtros e botões
        JPanel panelTop = createTopPanel();

        // Painel central com tabela
        JPanel panelCenter = createCenterPanel();

        // Painel inferior com status
        JPanel panelBottom = createBottomPanel();

        add(panelTop, BorderLayout.NORTH);
        add(panelCenter, BorderLayout.CENTER);
        add(panelBottom, BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));
        panel.setBackground(new Color(245, 245, 245));

        // Painel de filtros
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFiltros.setBackground(new Color(245, 245, 245));

        JLabel lblFiltrar = new JLabel("Filtrar por:");
        lblFiltrar.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        lblFiltrar.setForeground(new Color(73, 80, 87));
        panelFiltros.add(lblFiltrar);
        panelFiltros.add(cbFiltroTipo);
        panelFiltros.add(txtFiltro);

        JButton btnFiltrar = ButtonStyleFactory.createPrimaryButton("🔍 Filtrar");
        btnFiltrar.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        btnFiltrar.setPreferredSize(new Dimension(100, 30));
        btnFiltrar.addActionListener(e -> aplicarFiltro());
        panelFiltros.add(btnFiltrar);

        // Painel de botões de ação
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotoes.setBackground(new Color(245, 245, 245));
        panelBotoes.add(btnNovo);
        panelBotoes.add(btnEditar);
        panelBotoes.add(btnExcluir);
        panelBotoes.add(Box.createHorizontalStrut(10));
        panelBotoes.add(btnBloquear);
        panelBotoes.add(btnDesbloquear);
        panelBotoes.add(btnAlterarSenha);

        panel.add(panelFiltros, BorderLayout.WEST);
        panel.add(panelBotoes, BorderLayout.EAST);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        // Configurar renderizador para linhas alternadas
        tabelaUsuarios.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250));
                }
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(tabelaUsuarios);
        scrollPane.setPreferredSize(new Dimension(0, 400));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 226, 230)));
        panel.setBackground(new Color(248, 249, 250));
        lblStatus.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        lblStatus.setForeground(new Color(73, 80, 87));
        panel.add(lblStatus);

        return panel;
    }

    private void setupEventListeners() {
        // Botões de ação
        btnNovo.addActionListener(e -> novoUsuario());
        btnEditar.addActionListener(e -> editarUsuarioSelecionado());
        btnExcluir.addActionListener(e -> excluirUsuarioSelecionado());
        btnBloquear.addActionListener(e -> bloquearUsuarioSelecionado());
        btnDesbloquear.addActionListener(e -> desbloquearUsuarioSelecionado());
        btnAlterarSenha.addActionListener(e -> alterarSenhaUsuarioSelecionado());

        // Duplo clique na tabela para editar
        tabelaUsuarios.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editarUsuarioSelecionado();
                }
            }
        });

        // Filtro em tempo real
        txtFiltro.addActionListener(e -> aplicarFiltro());

        // Atualizar estado dos botões quando seleção muda
        tabelaUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                atualizarEstadoBotoes();
            }
        });
    }

    private void carregarUsuarios() {
        SwingUtilities.invokeLater(() -> {
            try {
                lblStatus.setText("Carregando usuários...");

                List<Usuario> usuarios = usuarioDAO.findAll();
                atualizarTabelaUsuarios(usuarios);

                lblStatus.setText("Total de usuários ativos: " + usuarios.size());

            } catch (SQLException e) {
                lblStatus.setText("Erro ao carregar usuários");
                JOptionPane.showMessageDialog(this,
                        "Erro ao carregar usuários: " + e.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void atualizarTabelaUsuarios(List<Usuario> usuarios) {
        modeloTabela.setRowCount(0);

        for (Usuario usuario : usuarios) {
            Object[] linha = {
                    usuario.getId(),
                    usuario.getLogin(),
                    usuario.getNomeCompleto(),
                    usuario.getEmail(),
                    usuario.getMatricula(),
                    usuario.getPerfil() != null ? usuario.getPerfil().name() : "",
                    usuario.getNomeSetor() != null ? usuario.getNomeSetor() : "",
                    usuario.getAtivo() ? "Sim" : "Não",
                    usuario.getBloqueado() ? "Sim" : "Não",
                    usuario.getDataUltimoAcesso() != null
                            ? DateFormatUtils.formatDateTime(usuario.getDataUltimoAcesso())
                            : "Nunca",
                    usuario.getDataCriacao() != null ? DateFormatUtils.formatDateTime(usuario.getDataCriacao()) : ""
            };
            modeloTabela.addRow(linha);
        }

        atualizarEstadoBotoes();
    }

    private void aplicarFiltro() {
        String filtro = txtFiltro.getText().trim();
        String tipoFiltro = (String) cbFiltroTipo.getSelectedItem();

        if (filtro.isEmpty()) {
            carregarUsuarios();
            return;
        }

        try {
            lblStatus.setText("Aplicando filtro...");

            List<Usuario> usuariosFiltrados;

            switch (tipoFiltro) {
                case "Nome", "Login", "Email" -> usuariosFiltrados = usuarioDAO.findAll().stream()
                        .filter(u -> u.getNomeCompleto().toLowerCase().contains(filtro.toLowerCase()) ||
                                u.getLogin().toLowerCase().contains(filtro.toLowerCase()) ||
                                u.getEmail().toLowerCase().contains(filtro.toLowerCase()))
                        .collect(java.util.stream.Collectors.toList());
                case "Perfil" -> {
                    try {
                        PerfilUsuario perfil = PerfilUsuario.valueOf(filtro.toUpperCase());
                        usuariosFiltrados = usuarioDAO.findAll().stream()
                                .filter(u -> u.getPerfil() == perfil)
                                .collect(java.util.stream.Collectors.toList());
                    } catch (IllegalArgumentException e) {
                        JOptionPane.showMessageDialog(this,
                                "Perfil inválido. Use: ADMIN, SUPERVISOR, COLETOR, CONSULTA",
                                "Filtro Inválido", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
                default -> usuariosFiltrados = usuarioDAO.findAll().stream()
                        .filter(u -> u.getNomeCompleto().toLowerCase().contains(filtro.toLowerCase()) ||
                                u.getLogin().toLowerCase().contains(filtro.toLowerCase()))
                        .collect(java.util.stream.Collectors.toList());
            }

            atualizarTabelaUsuarios(usuariosFiltrados);
            lblStatus.setText("Filtro aplicado. Usuários encontrados: " + usuariosFiltrados.size());

        } catch (HeadlessException | SQLException e) {
            lblStatus.setText("Erro ao aplicar filtro");
            JOptionPane.showMessageDialog(this,
                    "Erro ao aplicar filtro: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aplicarEstiloModerno() {
        // Configurar cores do frame
        getContentPane().setBackground(new Color(245, 245, 245));

        // Estilizar campo de filtro
        txtFiltro.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        txtFiltro.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        // Estilizar combobox
        cbFiltroTipo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        cbFiltroTipo.setBackground(Color.WHITE);

        // Estilizar tabela
        tabelaUsuarios.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        tabelaUsuarios.setSelectionBackground(new Color(0, 123, 255, 50));
        tabelaUsuarios.setSelectionForeground(Color.BLACK);

        JTableHeader header = tabelaUsuarios.getTableHeader();
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 35));
        header.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        header.setBackground(new Color(248, 249, 250));
        header.setForeground(new Color(73, 80, 87));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(222, 226, 230)));
    }

    private void novoUsuario() {
        UsuarioFormDialog dialog = new UsuarioFormDialog(this, null, usuarioDAO, setorDAO);
        dialog.setVisible(true);

        if (dialog.isUsuarioSalvo()) {
            carregarUsuarios();
        }
    }

    private void editarUsuarioSelecionado() {
        int linhaSelecionada = tabelaUsuarios.getSelectedRow();

        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um usuário para editar.",
                    "Nenhum usuário selecionado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Converter índice da view para o modelo (caso haja ordenação)
            int modelRow = tabelaUsuarios.convertRowIndexToModel(linhaSelecionada);
            Integer idUsuario = (Integer) modeloTabela.getValueAt(modelRow, 0);

            Usuario usuario = usuarioDAO.findById(idUsuario);

            if (usuario != null) {
                UsuarioFormDialog dialog = new UsuarioFormDialog(this, usuario, usuarioDAO, setorDAO);
                dialog.setVisible(true);

                if (dialog.isUsuarioSalvo()) {
                    carregarUsuarios();
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Usuário não encontrado.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }

        } catch (HeadlessException | SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar dados do usuário: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirUsuarioSelecionado() {
        int linhaSelecionada = tabelaUsuarios.getSelectedRow();

        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um usuário para excluir.",
                    "Nenhum usuário selecionado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int modelRow = tabelaUsuarios.convertRowIndexToModel(linhaSelecionada);
            Integer idUsuario = (Integer) modeloTabela.getValueAt(modelRow, 0);
            String nomeUsuario = (String) modeloTabela.getValueAt(modelRow, 2);
            String loginUsuario = (String) modeloTabela.getValueAt(modelRow, 1);

            // Primeira confirmação - aviso sobre exclusão permanente
            int opcao1 = JOptionPane.showConfirmDialog(this,
                    """
                    \u26a0\ufe0f ATEN\u00c7\u00c3O: EXCLUS\u00c3O PERMANENTE \u26a0\ufe0f
                    
                    Voc\u00ea est\u00e1 prestes a EXCLUIR PERMANENTEMENTE o usu\u00e1rio:
                    
                    Nome: """ + nomeUsuario + "\n" +
                            "Login: " + loginUsuario + "\n\n" +
                            "Esta ação é IRREVERSÍVEL e irá:\n" +
                            "• Remover o usuário do banco de dados\n" +
                            "• Apagar todos os dados associados\n" +
                            "• Impossibilitar o login deste usuário\n\n" +
                            "Deseja continuar?",
                    "⚠️ Confirmar Exclusão Permanente",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (opcao1 != JOptionPane.YES_OPTION) {
                return;
            }

            // Segunda confirmação - confirmação final
            int opcao2 = JOptionPane.showConfirmDialog(this,
                    """
                    \u00daLTIMA CONFIRMA\u00c7\u00c3O
                    
                    Tem ABSOLUTA CERTEZA que deseja excluir permanentemente
                    o usu\u00e1rio '""" + nomeUsuario + "'?\n\n" +
                            "Esta ação NÃO PODE SER DESFEITA!",
                    "⚠️ Confirmação Final",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE);

            if (opcao2 == JOptionPane.YES_OPTION) {
                try {
                    // Executar exclusão permanente
                    usuarioDAO.delete(idUsuario);

                    JOptionPane.showMessageDialog(this,
                            "Usuário '" + nomeUsuario + "' foi excluído permanentemente do sistema.",
                            "Exclusão Concluída",
                            JOptionPane.INFORMATION_MESSAGE);

                    carregarUsuarios();

                } catch (HeadlessException | SQLException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Erro ao excluir usuário: " + ex.getMessage() + "\n\n" +
                                    "Possíveis causas:\n" +
                                    "• Usuário possui registros vinculados no sistema\n" +
                                    "• Restrições de integridade do banco de dados\n" +
                                    "• Permissões insuficientes",
                            "Erro na Exclusão",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (HeadlessException e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao processar exclusão: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void bloquearUsuarioSelecionado() {
        int linhaSelecionada = tabelaUsuarios.getSelectedRow();

        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um usuário para bloquear.",
                    "Nenhum usuário selecionado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int modelRow = tabelaUsuarios.convertRowIndexToModel(linhaSelecionada);
            Integer idUsuario = (Integer) modeloTabela.getValueAt(modelRow, 0);
            String nomeUsuario = (String) modeloTabela.getValueAt(modelRow, 2);

            int opcao = JOptionPane.showConfirmDialog(this,
                    "Tem certeza que deseja bloquear o usuário '" + nomeUsuario + "'?",
                    "Confirmar Bloqueio", JOptionPane.YES_NO_OPTION);

            if (opcao == JOptionPane.YES_OPTION) {
                try {
                    Usuario u = usuarioDAO.findById(idUsuario);
                    if (u != null) {
                        u.setBloqueado(true);
                        usuarioDAO.update(u);
                        JOptionPane.showMessageDialog(this,
                                "Usuário bloqueado com sucesso!",
                                "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                        carregarUsuarios();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Usuário não encontrado.",
                                "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (HeadlessException | SQLException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Erro ao bloquear usuário: " + ex.getMessage(),
                            "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (HeadlessException e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao bloquear usuário: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void desbloquearUsuarioSelecionado() {
        int linhaSelecionada = tabelaUsuarios.getSelectedRow();

        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um usuário para desbloquear.",
                    "Nenhum usuário selecionado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int modelRow = tabelaUsuarios.convertRowIndexToModel(linhaSelecionada);
            Integer idUsuario = (Integer) modeloTabela.getValueAt(modelRow, 0);
            String nomeUsuario = (String) modeloTabela.getValueAt(modelRow, 2);

            int opcao = JOptionPane.showConfirmDialog(this,
                    "Tem certeza que deseja desbloquear o usuário '" + nomeUsuario + "'?",
                    "Confirmar Desbloqueio", JOptionPane.YES_NO_OPTION);

            if (opcao == JOptionPane.YES_OPTION) {
                try {
                    Usuario u = usuarioDAO.findById(idUsuario);
                    if (u != null) {
                        u.setBloqueado(false);
                        usuarioDAO.update(u);
                        JOptionPane.showMessageDialog(this,
                                "Usuário desbloqueado com sucesso!",
                                "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                        carregarUsuarios();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Usuário não encontrado.",
                                "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (HeadlessException | SQLException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Erro ao desbloquear usuário: " + ex.getMessage(),
                            "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (HeadlessException e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao desbloquear usuário: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void alterarSenhaUsuarioSelecionado() {
        int linhaSelecionada = tabelaUsuarios.getSelectedRow();

        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um usuário para alterar a senha.",
                    "Nenhum usuário selecionado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int modelRow = tabelaUsuarios.convertRowIndexToModel(linhaSelecionada);
            Integer idUsuario = (Integer) modeloTabela.getValueAt(modelRow, 0);
            String nomeUsuario = (String) modeloTabela.getValueAt(modelRow, 2);

            AlterarSenhaDialog dialog = new AlterarSenhaDialog(this, idUsuario, nomeUsuario);
            dialog.setVisible(true);

            if (dialog.isSenhaSalva()) {
                carregarUsuarios();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao alterar senha: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void atualizarEstadoBotoes() {
        boolean usuarioSelecionado = tabelaUsuarios.getSelectedRow() != -1;

        btnEditar.setEnabled(usuarioSelecionado);
        btnExcluir.setEnabled(usuarioSelecionado);
        btnBloquear.setEnabled(usuarioSelecionado);
        btnDesbloquear.setEnabled(usuarioSelecionado);
        btnAlterarSenha.setEnabled(usuarioSelecionado);
    }
}