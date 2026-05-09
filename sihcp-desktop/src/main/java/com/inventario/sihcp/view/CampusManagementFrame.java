package com.inventario.sihcp.view;

import com.inventario.sihcp.dao.CampusDAO;
import com.inventario.sihcp.model.Campus;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Tela de gerenciamento de Campus
 * Permite listar, cadastrar, editar e visualizar campus
 */
public class CampusManagementFrame extends JFrame {
    
    private final CampusDAO campusDAO;
    private JTable tableCampus;
    private DefaultTableModel tableModel;
    private JTextField txtBusca;
    private JButton btnNovo;
    private JButton btnEditar;
    private JButton btnAtualizar;
    private JButton btnFechar;
    
    public CampusManagementFrame() {
        this.campusDAO = new CampusDAO();
        
        setTitle("Gerenciamento de Campus");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        initComponents();
        carregarCampus();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Painel superior - busca e acoes
        JPanel panelTop = new JPanel(new BorderLayout(10, 10));
        panelTop.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Painel de busca
        JPanel panelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBusca.add(new JLabel("Buscar:"));
        
        txtBusca = new JTextField(30);
        txtBusca.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filtrarCampus();
            }
        });
        panelBusca.add(txtBusca);
        
        panelTop.add(panelBusca, BorderLayout.WEST);
        
        // Painel de botoes
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        btnNovo = new JButton("Novo Campus");
        btnNovo.addActionListener(e -> novoCampus());
        panelBotoes.add(btnNovo);
        
        btnEditar = new JButton("Editar");
        btnEditar.setEnabled(false);
        btnEditar.addActionListener(e -> editarCampus());
        panelBotoes.add(btnEditar);
        
        btnAtualizar = new JButton("Atualizar");
        btnAtualizar.addActionListener(e -> carregarCampus());
        panelBotoes.add(btnAtualizar);
        
        panelTop.add(panelBotoes, BorderLayout.EAST);
        
        add(panelTop, BorderLayout.NORTH);
        
        // Tabela de campus
        String[] colunas = {"ID", "Nome", "Codigo UOrg", "Endereco", "Telefone", "Email", "Ativo"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableCampus = new JTable(tableModel);
        tableCampus.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableCampus.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                btnEditar.setEnabled(tableCampus.getSelectedRow() != -1);
            }
        });
        
        // Duplo clique para editar
        tableCampus.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tableCampus.getSelectedRow() != -1) {
                    editarCampus();
                }
            }
        });
        
        // Ajustar largura das colunas
        tableCampus.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        tableCampus.getColumnModel().getColumn(1).setPreferredWidth(200); // Nome
        tableCampus.getColumnModel().getColumn(2).setPreferredWidth(100); // Codigo UOrg
        tableCampus.getColumnModel().getColumn(3).setPreferredWidth(200); // Endereco
        tableCampus.getColumnModel().getColumn(4).setPreferredWidth(120); // Telefone
        tableCampus.getColumnModel().getColumn(5).setPreferredWidth(150); // Email
        tableCampus.getColumnModel().getColumn(6).setPreferredWidth(60);  // Ativo
        
        JScrollPane scrollPane = new JScrollPane(tableCampus);
        add(scrollPane, BorderLayout.CENTER);
        
        // Painel inferior
        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBottom.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(e -> dispose());
        panelBottom.add(btnFechar);
        
        add(panelBottom, BorderLayout.SOUTH);
    }
    
    private void carregarCampus() {
        try {
            tableModel.setRowCount(0);
            List<Campus> listaCampus = campusDAO.listarCampus();
            
            for (Campus campus : listaCampus) {
                Object[] row = {
                    campus.getId(),
                    campus.getNome(),
                    campus.getCodigoUorg() != null ? campus.getCodigoUorg() : "-",
                    campus.getLocal() != null ? campus.getLocal() : "-",
                    campus.getTelefone() != null ? campus.getTelefone() : "-",
                    campus.getEmail() != null ? campus.getEmail() : "-",
                    campus.isAtivo() ? "Sim" : "Nao"
                };
                tableModel.addRow(row);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Erro ao carregar campus: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }
    
    private void filtrarCampus() {
        String busca = txtBusca.getText().toLowerCase().trim();
        
        if (busca.isEmpty()) {
            carregarCampus();
            return;
        }
        
        try {
            tableModel.setRowCount(0);
            List<Campus> listaCampus = campusDAO.listarCampus();
            
            for (Campus campus : listaCampus) {
                boolean match = campus.getNome().toLowerCase().contains(busca) ||
                               (campus.getCodigoUorg() != null && campus.getCodigoUorg().toLowerCase().contains(busca)) ||
                               (campus.getLocal() != null && campus.getLocal().toLowerCase().contains(busca)) ||
                               (campus.getEmail() != null && campus.getEmail().toLowerCase().contains(busca));
                
                if (match) {
                    Object[] row = {
                        campus.getId(),
                        campus.getNome(),
                        campus.getCodigoUorg() != null ? campus.getCodigoUorg() : "-",
                        campus.getLocal() != null ? campus.getLocal() : "-",
                        campus.getTelefone() != null ? campus.getTelefone() : "-",
                        campus.getEmail() != null ? campus.getEmail() : "-",
                        campus.isAtivo() ? "Sim" : "Nao"
                    };
                    tableModel.addRow(row);
                }
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Erro ao filtrar campus: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }
    
    private void novoCampus() {
        CampusFormDialog dialog = new CampusFormDialog(this, null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmado()) {
            carregarCampus();
        }
    }
    
    private void editarCampus() {
        int selectedRow = tableCampus.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }
        
        try {
            int campusId = (int) tableModel.getValueAt(selectedRow, 0);
            Campus campus = campusDAO.buscarCampusPorId(campusId);
            
            if (campus == null) {
                JOptionPane.showMessageDialog(
                    this,
                    "Campus nao encontrado.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            
            CampusFormDialog dialog = new CampusFormDialog(this, campus);
            dialog.setVisible(true);
            
            if (dialog.isConfirmado()) {
                carregarCampus();
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Erro ao editar campus: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CampusManagementFrame frame = new CampusManagementFrame();
            frame.setVisible(true);
        });
    }
}
