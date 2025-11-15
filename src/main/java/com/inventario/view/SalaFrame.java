package com.inventario.view;

import com.inventario.dao.SalaDAO;
import com.inventario.model.Sala;
import com.inventario.view.ui.ButtonStyleFactory;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Interface gráfica para gerenciamento de Salas
 * Sistema de Inventário IFMT
 */
public class SalaFrame extends JFrame {
    
    private final SalaDAO salaDAO;
    private JTable tableSalas;
    private DefaultTableModel tableModel;
    private JTextField txtFiltro;
    private JButton btnNovo, btnEditar, btnExcluir, btnAtualizar;
    
    public SalaFrame() {
        // === SWING: Usar DAO diretamente (sem Spring) ===
        this.salaDAO = new SalaDAO();
        initializeComponents();
    }
    
    private void initializeComponents() {
        setTitle("Gerenciamento de Salas - SIHCP");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        
        // Layout principal
        setLayout(new BorderLayout());
        
        // Panel superior - Filtros e botões
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Panel central - Tabela
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);
        
        // Panel inferior - Status
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel de filtro
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filtrar:"));
        
        txtFiltro = new JTextField(20);
        txtFiltro.addActionListener(e -> filtrarSalas());
        filterPanel.add(txtFiltro);
        
        JButton btnFiltrar = ButtonStyleFactory.createSecondaryButton("Buscar");
        btnFiltrar.addActionListener(e -> filtrarSalas());
        filterPanel.add(btnFiltrar);
        
        // Panel de botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        btnNovo = ButtonStyleFactory.createSuccessButton("Nova Sala");
        btnNovo.addActionListener(e -> abrirFormularioSala(null));
        
        btnEditar = ButtonStyleFactory.createPrimaryButton("Editar");
        btnEditar.setEnabled(false);
        btnEditar.addActionListener(e -> editarSalaSelecionada());
        
        btnExcluir = ButtonStyleFactory.createDangerButton("Excluir");
        btnExcluir.setEnabled(false);
        btnExcluir.addActionListener(e -> excluirSalaSelecionada());
        
        btnAtualizar = ButtonStyleFactory.createSecondaryButton("Atualizar");
        btnAtualizar.addActionListener(e -> carregarSalas());
        
        buttonPanel.add(btnNovo);
        buttonPanel.add(btnEditar);
        buttonPanel.add(btnExcluir);
        buttonPanel.add(btnAtualizar);
        
        panel.add(filterPanel, BorderLayout.WEST);
        panel.add(buttonPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        
        // Criar modelo da tabela
        String[] colunas = {"ID", "Número", "Descrição", "Andar", "Bloco", "Setor", "Capacidade", "Área (m²)", "Tipo"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabela não editável
            }
        };
        
        tableSalas = new JTable(tableModel);
        tableSalas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableSalas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = tableSalas.getSelectedRow() != -1;
                btnEditar.setEnabled(hasSelection);
                btnExcluir.setEnabled(hasSelection);
            }
        });
        
        // Duplo clique para editar
        tableSalas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tableSalas.getSelectedRow() != -1) {
                    editarSalaSelecionada();
                }
            }
        });
        
        // Configurar larguras das colunas
        tableSalas.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        tableSalas.getColumnModel().getColumn(1).setPreferredWidth(80);  // Número
        tableSalas.getColumnModel().getColumn(2).setPreferredWidth(200); // Descrição
        tableSalas.getColumnModel().getColumn(3).setPreferredWidth(60);  // Andar
        tableSalas.getColumnModel().getColumn(4).setPreferredWidth(60);  // Bloco
        tableSalas.getColumnModel().getColumn(5).setPreferredWidth(150); // Setor
        tableSalas.getColumnModel().getColumn(6).setPreferredWidth(80);  // Capacidade
        tableSalas.getColumnModel().getColumn(7).setPreferredWidth(80);  // Área
        tableSalas.getColumnModel().getColumn(8).setPreferredWidth(100); // Tipo
        
        JScrollPane scrollPane = new JScrollPane(tableSalas);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        JLabel lblStatus = new JLabel("Pronto");
        panel.add(lblStatus);
        
        return panel;
    }
    
    private void carregarSalas() {
        try {
            List<Sala> salas = salaDAO.listarSalas();
            atualizarTabela(salas);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao carregar salas: " + e.getMessage(), 
                "Erro", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void filtrarSalas() {
        String filtro = txtFiltro.getText().trim().toLowerCase();
        try {
            List<Sala> salas = salaDAO.listarSalas();
            
            if (!filtro.isEmpty()) {
                // Filtrar localmente por número, descrição, bloco ou tipo
                salas = salas.stream()
                    .filter(s -> 
                        (s.getNumeroSala() != null && s.getNumeroSala().toLowerCase().contains(filtro)) ||
                        (s.getDescricao() != null && s.getDescricao().toLowerCase().contains(filtro)) ||
                        (s.getBloco() != null && s.getBloco().toLowerCase().contains(filtro)) ||
                        (s.getTipoSala() != null && s.getTipoSala().toLowerCase().contains(filtro))
                    )
                    .collect(java.util.stream.Collectors.toList());
            }
            
            atualizarTabela(salas);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao filtrar salas: " + e.getMessage(), 
                "Erro", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void atualizarTabela(List<Sala> salas) {
        tableModel.setRowCount(0);
        
        for (Sala sala : salas) {
            Object[] row = {
                sala.getIdSala(),
                sala.getNumeroSala(),
                sala.getDescricao(),
                sala.getAndar(),
                sala.getBloco(),
                sala.getNomeSetor() != null ? sala.getNomeSetor() : "N/A",
                sala.getCapacidade(),
                String.format("%.2f", sala.getAreaM2()),
                sala.getTipoSala()
            };
            tableModel.addRow(row);
        }
    }
    
    private void abrirFormularioSala(Sala sala) {
        SalaFormDialog dialog = new SalaFormDialog(this, sala);
        dialog.setVisible(true);
        
        if (dialog.isSalvo()) {
            carregarSalas();
        }
    }
    
    private void editarSalaSelecionada() {
        int selectedRow = tableSalas.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Selecione uma sala para editar.", 
                "Aviso", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int idSala = (Integer) tableModel.getValueAt(selectedRow, 0);
        try {
            Sala sala = salaDAO.buscarSalaPorId(idSala);
            
            if (sala != null) {
                abrirFormularioSala(sala);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Sala não encontrada.", 
                    "Erro", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao buscar sala: " + e.getMessage(), 
                "Erro", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void excluirSalaSelecionada() {
        int selectedRow = tableSalas.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Selecione uma sala para excluir.", 
                "Aviso", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int idSala = (Integer) tableModel.getValueAt(selectedRow, 0);
        String numeroSala = (String) tableModel.getValueAt(selectedRow, 1);
        String descricao = (String) tableModel.getValueAt(selectedRow, 2);
        
        int confirmacao = JOptionPane.showConfirmDialog(this, 
            "Tem certeza que deseja excluir a sala:\n" + 
            numeroSala + " - " + descricao + "?", 
            "Confirmar Exclusão", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirmacao == JOptionPane.YES_OPTION) {
            try {
                salaDAO.excluirSala(idSala);
                JOptionPane.showMessageDialog(this, 
                    "Sala excluída com sucesso!", 
                    "Sucesso", 
                    JOptionPane.INFORMATION_MESSAGE);
                carregarSalas();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Erro ao excluir sala: " + e.getMessage(), 
                    "Erro", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}