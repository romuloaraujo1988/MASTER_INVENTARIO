package com.inventario.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import com.inventario.model.Campus;
import com.inventario.service.CampusService;
import com.inventario.view.ui.ButtonStyleFactory;

/**
 * Tela principal para gerenciamento de campus
 * Permite visualizar, adicionar, editar e excluir campus
 */
public class CampusFrame extends JFrame {
    private JTable tabelaCampus;
    private DefaultTableModel modeloTabela;
    private final CampusService campusService;
    private JTextField campoBusca;
    private JButton btnNovo, btnEditar, btnExcluir, btnBuscar;
    
    public CampusFrame() {
        // Instantiate service directly (no Spring context in Swing app)
        this.campusService = new CampusService();
        initComponents();
        aplicarEstiloModerno();
        carregarCampus();
    }
    
    private void initComponents() {
        setTitle("Gerenciamento de Campus - SIHCP");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Painel superior - busca e ações
        JPanel painelSuperior = new JPanel();
        painelSuperior.setLayout(new BoxLayout(painelSuperior, BoxLayout.Y_AXIS));
        painelSuperior.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        // Título
        JLabel lblTitulo = new JLabel("Gerenciamento de Campus");
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
        campoBusca.setToolTipText("Digite o nome, local ou diretor do campus");
        painelBusca.add(campoBusca);
        painelBusca.add(Box.createHorizontalStrut(10));
        
        btnBuscar = ButtonStyleFactory.createSecondaryButton("Buscar");
        painelBusca.add(btnBuscar);
        
        painelSuperior.add(painelBusca);
        painelSuperior.add(Box.createVerticalStrut(15));
        
        // Painel de botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        painelBotoes.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        btnNovo = ButtonStyleFactory.createSuccessButton("➕ Novo Campus");
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
        String[] colunas = {"ID", "Nome", "Local", "CNPJ", "Diretor", "Telefone", "Email"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabelaCampus = new JTable(modeloTabela);
        tabelaCampus.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaCampus.setRowHeight(28);
        tabelaCampus.setShowGrid(false);
        tabelaCampus.setIntercellSpacing(new Dimension(0, 0));
        
        // Configurar larguras das colunas
        tabelaCampus.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        tabelaCampus.getColumnModel().getColumn(1).setPreferredWidth(150); // Nome
        tabelaCampus.getColumnModel().getColumn(2).setPreferredWidth(200); // Local
        tabelaCampus.getColumnModel().getColumn(3).setPreferredWidth(120); // CNPJ
        tabelaCampus.getColumnModel().getColumn(4).setPreferredWidth(150); // Diretor
        tabelaCampus.getColumnModel().getColumn(5).setPreferredWidth(120); // Telefone
        tabelaCampus.getColumnModel().getColumn(6).setPreferredWidth(180); // Email
        
        // Configurar header da tabela
        JTableHeader header = tabelaCampus.getTableHeader();
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 35));
        header.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        // Configurar renderizador para linhas alternadas
        tabelaCampus.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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
        
        JScrollPane scrollPane = new JScrollPane(tabelaCampus);
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
        tabelaCampus.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        tabelaCampus.setSelectionBackground(new Color(0, 123, 255, 50));
        tabelaCampus.setSelectionForeground(Color.BLACK);
        
        JTableHeader header = tabelaCampus.getTableHeader();
        header.setBackground(new Color(248, 249, 250));
        header.setForeground(new Color(73, 80, 87));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(222, 226, 230)));
    }
    

    
    private void configurarEventos() {
        btnNovo.addActionListener(e -> abrirFormularioCampus(null));
        btnEditar.addActionListener(e -> editarCampus());
        btnExcluir.addActionListener(e -> excluirCampus());
        btnBuscar.addActionListener(e -> buscarCampus());
        
        // Permitir busca ao pressionar Enter no campo de busca
        campoBusca.addActionListener(e -> buscarCampus());
        
        // Duplo clique na tabela para editar
        tabelaCampus.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editarCampus();
                }
            }
        });
    }
    
    private void carregarCampus() {
        modeloTabela.setRowCount(0);
        try {
            List<Campus> campusList = campusService.listarCampusAtivos();
            for (Campus campus : campusList) {
                modeloTabela.addRow(new Object[]{
                    campus.getId(),
                    campus.getNome(),
                    campus.getLocal(),
                    campus.getCnpj() != null ? campus.getCnpj() : "",
                    campus.getDiretor() != null ? campus.getDiretor() : "",
                    campus.getTelefone() != null ? campus.getTelefone() : "",
                    campus.getEmail() != null ? campus.getEmail() : ""
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar campus: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void abrirFormularioCampus(Campus campus) {
        CampusFormDialog dialog = new CampusFormDialog(this, campus);
        dialog.setVisible(true);
        if (dialog.isConfirmado()) {
            carregarCampus();
        }
    }
    
    private void editarCampus() {
        int linhaSelecionada = tabelaCampus.getSelectedRow();
        if (linhaSelecionada >= 0) {
            try {
                Integer id = (Integer) modeloTabela.getValueAt(linhaSelecionada, 0);
                Campus campus = campusService.buscarCampusPorId(id);
                if (campus != null) {
                    abrirFormularioCampus(campus);
                } else {
                    JOptionPane.showMessageDialog(this, "Campus não encontrado.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar campus: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um campus para editar.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void excluirCampus() {
        int linhaSelecionada = tabelaCampus.getSelectedRow();
        if (linhaSelecionada >= 0) {
            try {
                Integer id = (Integer) modeloTabela.getValueAt(linhaSelecionada, 0);
                String nomeCampus = (String) modeloTabela.getValueAt(linhaSelecionada, 1);
                
                // Verificar se há setores vinculados ao campus
                int qtdSetores = campusService.contarSetoresDoCampus(id);
                
                if (qtdSetores > 0) {
                    String mensagem = "Não é possível excluir o campus '" + nomeCampus + "' pois possui " +
                        qtdSetores + " setor(es) vinculado(s).\n\nRemova as vinculações antes de excluir o campus.";
                    JOptionPane.showMessageDialog(this, mensagem, "Exclusão não permitida", 
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                int confirmacao = JOptionPane.showConfirmDialog(this, 
                    "Tem certeza que deseja excluir o campus '" + nomeCampus + "'?", 
                    "Confirmar Exclusão", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
                
                if (confirmacao == JOptionPane.YES_OPTION) {
                    if (campusService.excluirCampus(id)) {
                        JOptionPane.showMessageDialog(this, "Campus excluído com sucesso!",
                            "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                        carregarCampus();
                    } else {
                        JOptionPane.showMessageDialog(this, "Erro ao excluir campus.",
                            "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir campus: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um campus para excluir.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void buscarCampus() {
        String termo = campoBusca.getText().trim();
        modeloTabela.setRowCount(0);
        
        try {
            List<Campus> campusList;
            if (!termo.isEmpty()) {
                // Buscar por nome, local ou diretor
                campusList = campusService.buscarCampusPorFiltro(termo, termo, termo);
            } else {
                campusList = campusService.listarCampusAtivos();
            }
            
            for (Campus campus : campusList) {
                modeloTabela.addRow(new Object[]{
                    campus.getId(),
                    campus.getNome(),
                    campus.getLocal(),
                    campus.getCnpj() != null ? campus.getCnpj() : "",
                    campus.getDiretor() != null ? campus.getDiretor() : "",
                    campus.getTelefone() != null ? campus.getTelefone() : "",
                    campus.getEmail() != null ? campus.getEmail() : ""
                });
            }
            
            if (campusList.isEmpty() && !termo.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nenhum campus encontrado com o termo: " + termo,
                    "Resultado da Busca", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao buscar campus: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}