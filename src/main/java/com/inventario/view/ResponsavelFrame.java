package com.inventario.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import com.inventario.service.ResponsavelService;
import com.inventario.service.BusinessException;
import com.inventario.model.Responsavel;
import com.inventario.view.ui.ButtonStyleFactory;

/**
 * Tela principal para gerenciamento de responsáveis
 * REFATORADO: Usa ResponsavelService ao invés de DAO
 */
public class ResponsavelFrame extends JFrame {
    private JTable tabelaResponsavel;
    private DefaultTableModel modeloTabela;
    private final ResponsavelService responsavelService;
    private JTextField campoBusca;
    private JButton btnNovo, btnEditar, btnExcluir, btnBuscar;

    public ResponsavelFrame() {
        // Instantiate service directly (no Spring context in Swing app)
        this.responsavelService = new ResponsavelService();
        initComponents();
        aplicarEstiloModerno();
        carregarResponsaveis();
    }
    
    public ResponsavelFrame(ResponsavelService responsavelService) {
        this.responsavelService = responsavelService;
        initComponents();
        aplicarEstiloModerno();
        carregarResponsaveis();
    }

    private void initComponents() {
        setTitle("Gerenciamento de Responsáveis - SIHCP");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Painel superior - busca e ações
        JPanel painelSuperior = new JPanel(new BorderLayout());
        painelSuperior.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));
        painelSuperior.setBackground(new Color(245, 245, 245));

        // Subpainel de busca
        JPanel painelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelBusca.setBackground(new Color(245, 245, 245));
        JLabel lblBuscar = new JLabel("Buscar:");
        lblBuscar.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        lblBuscar.setForeground(new Color(73, 80, 87));
        painelBusca.add(lblBuscar);

        campoBusca = new JTextField(20);
        painelBusca.add(campoBusca);

        btnBuscar = ButtonStyleFactory.createSecondaryButton("Buscar");
        painelBusca.add(btnBuscar);

        // Subpainel de ações
        JPanel painelAcoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        painelAcoes.setBackground(new Color(245, 245, 245));

        btnNovo = ButtonStyleFactory.createSuccessButton("Novo");
        btnEditar = ButtonStyleFactory.createPrimaryButton("Editar");
        btnExcluir = ButtonStyleFactory.createDangerButton("Excluir");

        painelAcoes.add(btnNovo);
        painelAcoes.add(btnEditar);
        painelAcoes.add(btnExcluir);

        // Organizar painéis
        painelSuperior.add(painelBusca, BorderLayout.WEST);
        painelSuperior.add(painelAcoes, BorderLayout.EAST);

        add(painelSuperior, BorderLayout.NORTH);

        // Tabela central
        String[] colunas = { "ID", "Nome", "CPF", "Email", "Telefone", "Cargo", "Setor", "Status" };
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaResponsavel = new JTable(modeloTabela);
        tabelaResponsavel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaResponsavel.setRowHeight(25);

        // Configurar cabeçalho da tabela
        JTableHeader header = tabelaResponsavel.getTableHeader();
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 35));
        header.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));

        // Configurar renderizador para linhas alternadas
        tabelaResponsavel.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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

        JScrollPane scrollPane = new JScrollPane(tabelaResponsavel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        // Painel da tabela com margem
        JPanel painelTabela = new JPanel(new BorderLayout());
        painelTabela.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        painelTabela.add(scrollPane, BorderLayout.CENTER);

        add(painelTabela, BorderLayout.CENTER);

        // Configurar eventos
        configurarEventos();

        setSize(1000, 600);
        setLocationRelativeTo(null);
    }

    private void configurarEventos() {
        btnNovo.addActionListener(e -> abrirFormularioResponsavel(null));
        btnEditar.addActionListener(e -> editarResponsavel());
        btnExcluir.addActionListener(e -> excluirResponsavel());
        btnBuscar.addActionListener(e -> buscarResponsaveis());

        // Permitir buscar ao pressionar Enter no campo de busca
        campoBusca.addActionListener(e -> buscarResponsaveis());

        // Duplo clique na tabela para editar
        tabelaResponsavel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editarResponsavel();
                }
            }
        });
    }

    private void carregarResponsaveis() {
        try {
            // Limpar tabela
            modeloTabela.setRowCount(0);

            // Carregar dados do banco
            List<Responsavel> responsaveis = responsavelService.listarTodos();
            for (Responsavel responsavel : responsaveis) {
                modeloTabela.addRow(new Object[] {
                        responsavel.getId(),
                        responsavel.getNome(),
                        responsavel.getCpf(),
                        responsavel.getEmail(),
                        responsavel.getTelefone(),
                        responsavel.getCargo(),
                        responsavel.getNomeSetor(),
                        responsavel.getStatusAtivacao()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar responsáveis: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirFormularioResponsavel(Responsavel responsavel) {
        ResponsavelFormDialog dialog = new ResponsavelFormDialog(this, responsavel);
        dialog.setVisible(true);
        if (dialog.isConfirmado()) {
            carregarResponsaveis();
        }
    }

    private void editarResponsavel() {
        int linhaSelecionada = tabelaResponsavel.getSelectedRow();
        if (linhaSelecionada >= 0) {
            try {
                Integer id = (Integer) modeloTabela.getValueAt(linhaSelecionada, 0);
                Responsavel responsavel = responsavelService.buscarPorId(id);
                if (responsavel != null) {
                    ResponsavelFormDialog dialog = new ResponsavelFormDialog(this, responsavel);
                    dialog.setVisible(true);
                    carregarResponsaveis();
                } else {
                    JOptionPane.showMessageDialog(this, "Responsável não encontrado.", "Erro",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao editar responsável: " + e.getMessage(), "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um responsável para editar.");
        }
    }

    private void excluirResponsavel() {
        int linhaSelecionada = tabelaResponsavel.getSelectedRow();
        if (linhaSelecionada >= 0) {
            try {
                Integer id = (Integer) modeloTabela.getValueAt(linhaSelecionada, 0);
                String nome = (String) modeloTabela.getValueAt(linhaSelecionada, 1);

                int confirmacao = JOptionPane.showConfirmDialog(this,
                        "Tem certeza que deseja excluir o responsável \"" + nome + "\"?",
                        "Confirmar Exclusão",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (confirmacao == JOptionPane.YES_OPTION) {
                    try {
                        responsavelService.excluir(id);
                        JOptionPane.showMessageDialog(this, 
                            "Responsável excluído com sucesso!",
                            "Sucesso",
                            JOptionPane.INFORMATION_MESSAGE);
                        carregarResponsaveis();
                    } catch (BusinessException e) {
                        JOptionPane.showMessageDialog(this,
                            e.getMessage(),
                            "Não é Possível Excluir",
                            JOptionPane.WARNING_MESSAGE);
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(), "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um responsável para excluir.");
        }
    }

    private void buscarResponsaveis() {
        String termo = campoBusca.getText().trim();
        if (!termo.isEmpty()) {
            try {
                // Limpar tabela
                modeloTabela.setRowCount(0);

                // Buscar responsáveis por filtro (busca parcial em nome, CPF, email e cargo)
                List<Responsavel> resultados = responsavelService.buscarPorFiltro(termo);
                for (Responsavel responsavel : resultados) {
                    modeloTabela.addRow(new Object[] {
                            responsavel.getId(),
                            responsavel.getNome(),
                            responsavel.getCpf(),
                            responsavel.getEmail(),
                            responsavel.getTelefone(),
                            responsavel.getCargo(),
                            responsavel.getNomeSetor(),
                            responsavel.getStatusAtivacao()
                    });
                }

                if (resultados.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Nenhum responsável encontrado com o termo: \"" + termo + "\"\n\n" +
                                    "A busca procura em: Nome, CPF, Email e Cargo",
                            "Busca",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao buscar responsáveis: " + e.getMessage(), "Erro",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } else {
            carregarResponsaveis();
        }
    }

    private void aplicarEstiloModerno() {
        // Configurar cores do frame
        getContentPane().setBackground(new Color(245, 245, 245));

        // Estilizar campo de busca
        campoBusca.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        campoBusca.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        // Estilizar tabela
        tabelaResponsavel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        tabelaResponsavel.setSelectionBackground(new Color(0, 123, 255, 50));
        tabelaResponsavel.setSelectionForeground(Color.BLACK);

        JTableHeader header = tabelaResponsavel.getTableHeader();
        header.setBackground(new Color(248, 249, 250));
        header.setForeground(new Color(73, 80, 87));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(222, 226, 230)));
    }
}