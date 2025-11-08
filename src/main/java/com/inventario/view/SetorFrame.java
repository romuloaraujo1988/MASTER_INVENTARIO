package com.inventario.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import com.inventario.service.SetorService;
import com.inventario.service.ServiceFactory;
import com.inventario.service.BusinessException;
import com.inventario.model.Setor;
import com.inventario.view.ui.ButtonStyleFactory;

/**
 * Tela principal para gerenciamento de setores
 * Permite visualizar, adicionar, editar e excluir setores
 * 
 * REFATORADO: Usa SetorService ao invés de DAO diretamente
 */
public class SetorFrame extends JFrame {
    private JTable tabelaSetor;
    private DefaultTableModel modeloTabela;
    private final SetorService setorService;
    private JTextField campoBusca;
    private JButton btnNovo, btnEditar, btnExcluir, btnBuscar;
    
    public SetorFrame() {
        this.setorService = ServiceFactory.getInstance().getSetorService();
        initComponents();
        aplicarEstiloModerno();
    }
    
    /**
     * Construtor para testes (injeção de dependência)
     */
    public SetorFrame(SetorService setorService) {
        this.setorService = setorService;
        initComponents();
        aplicarEstiloModerno();
    }
    
    private void initComponents() {
        setTitle("Gerenciamento de Setores");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Painel superior - busca e ações
        JPanel painelSuperior = new JPanel();
        painelSuperior.setLayout(new BoxLayout(painelSuperior, BoxLayout.Y_AXIS));
        painelSuperior.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        // Título
        JLabel lblTitulo = new JLabel("Gerenciamento de Setores");
        lblTitulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelSuperior.add(lblTitulo);
        painelSuperior.add(Box.createVerticalStrut(15));
        
        // Painel de busca
        JPanel painelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        painelBusca.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblBuscar = new JLabel("🔍 Buscar:");
        lblBuscar.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        painelBusca.add(lblBuscar);
        painelBusca.add(Box.createHorizontalStrut(10));
        
        campoBusca = new JTextField(25);
        campoBusca.setPreferredSize(new Dimension(250, 32));
        painelBusca.add(campoBusca);
        painelBusca.add(Box.createHorizontalStrut(10));
        
        btnBuscar = ButtonStyleFactory.createSecondaryButton("Buscar");
        painelBusca.add(btnBuscar);
        
        painelSuperior.add(painelBusca);
        painelSuperior.add(Box.createVerticalStrut(15));
        
        // Painel de botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        painelBotoes.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        btnNovo = ButtonStyleFactory.createSuccessButton("➕ Novo Setor");
        btnEditar = ButtonStyleFactory.createPrimaryButton("✏️ Editar");
        btnExcluir = ButtonStyleFactory.createDangerButton("🗑️ Excluir");
        
        painelBotoes.add(btnNovo);
        painelBotoes.add(Box.createHorizontalStrut(10));
        painelBotoes.add(btnEditar);
        painelBotoes.add(Box.createHorizontalStrut(10));
        painelBotoes.add(btnExcluir);
        
        painelSuperior.add(painelBotoes);
        
        add(painelSuperior, BorderLayout.NORTH);
        
        // Tabela central
        String[] colunas = {"ID", "Nome", "Descrição", "Responsável"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabelaSetor = new JTable(modeloTabela);
        tabelaSetor.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaSetor.setRowHeight(28);
        tabelaSetor.setShowGrid(false);
        tabelaSetor.setIntercellSpacing(new Dimension(0, 0));
        
        // Configurar header da tabela
        JTableHeader header = tabelaSetor.getTableHeader();
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 35));
        header.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        // Configurar renderizador para linhas alternadas
        tabelaSetor.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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
        
        JScrollPane scrollPane = new JScrollPane(tabelaSetor);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        // Painel da tabela com margem
        JPanel painelTabela = new JPanel(new BorderLayout());
        painelTabela.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        painelTabela.add(scrollPane, BorderLayout.CENTER);
        
        add(painelTabela, BorderLayout.CENTER);
        
        // Configurar eventos
        configurarEventos();
        
        setSize(800, 550);
        setLocationRelativeTo(null);
    }
    
    private void aplicarEstiloModerno() {
        // Configurar cores do frame
        getContentPane().setBackground(new Color(245, 245, 245));
        
        // Estilizar campo de busca
        campoBusca.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        campoBusca.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        // Estilizar tabela
        tabelaSetor.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        tabelaSetor.setSelectionBackground(new Color(0, 123, 255, 50));
        tabelaSetor.setSelectionForeground(Color.BLACK);
        
        JTableHeader header = tabelaSetor.getTableHeader();
        header.setBackground(new Color(248, 249, 250));
        header.setForeground(new Color(73, 80, 87));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(222, 226, 230)));
    }
    
    private void configurarEventos() {
        btnNovo.addActionListener(e -> abrirFormularioSetor(null));
        btnEditar.addActionListener(e -> editarSetor());
        btnExcluir.addActionListener(e -> excluirSetor());
        btnBuscar.addActionListener(e -> buscarSetores());
    }
    
    private void carregarSetores() {
        modeloTabela.setRowCount(0);
        List<Setor> setores = setorService.listarTodos();
        for (Setor s : setores) {
            modeloTabela.addRow(new Object[]{
                s.getId(),
                s.getNome(),
                s.getDescricao(),
                s.getResponsavelSetor()
            });
        }
    }
    
    private void abrirFormularioSetor(Setor setor) {
        SetorFormDialog dialog = new SetorFormDialog(this, setor);
        dialog.setVisible(true);
        if (dialog.isConfirmado()) {
            carregarSetores();
        }
    }
    
    private void editarSetor() {
        int linhaSelecionada = tabelaSetor.getSelectedRow();
        if (linhaSelecionada >= 0) {
            Integer id = (Integer) modeloTabela.getValueAt(linhaSelecionada, 0);
            Setor setor = setorService.buscarPorId(id);
            if (setor != null) {
                abrirFormularioSetor(setor);
            } else {
                JOptionPane.showMessageDialog(this, "Setor não encontrado.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um setor para editar.");
        }
    }
    
    private void excluirSetor() {
        int linhaSelecionada = tabelaSetor.getSelectedRow();
        if (linhaSelecionada >= 0) {
            Integer id = (Integer) modeloTabela.getValueAt(linhaSelecionada, 0);
            String nomeSetor = (String) modeloTabela.getValueAt(linhaSelecionada, 1);
            
            int confirmacao = JOptionPane.showConfirmDialog(this, 
                "Tem certeza que deseja excluir o setor '" + nomeSetor + "'?", 
                "Confirmar Exclusão", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (confirmacao == JOptionPane.YES_OPTION) {
                try {
                    setorService.excluir(id);
                    JOptionPane.showMessageDialog(this, 
                        "Setor excluído com sucesso!", 
                        "Sucesso", 
                        JOptionPane.INFORMATION_MESSAGE);
                    carregarSetores();
                } catch (BusinessException e) {
                    JOptionPane.showMessageDialog(this, 
                        e.getMessage(), 
                        "Não é Possível Excluir", 
                        JOptionPane.WARNING_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um setor para excluir.");
        }
    }
    
    private void buscarSetores() {
        String termo = campoBusca.getText().trim();
        modeloTabela.setRowCount(0);
        
        List<Setor> setores;
        if (!termo.isEmpty()) {
            setores = setorService.buscarPorTermo(termo);
        } else {
            setores = setorService.listarTodos();
        }
        
        for (Setor s : setores) {
             modeloTabela.addRow(new Object[]{
                 s.getId(),
                 s.getNome(),
                 s.getDescricao(),
                 s.getResponsavelSetor()
             });
         }
    }
}