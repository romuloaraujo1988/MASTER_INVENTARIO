package com.inventario.view;

import com.inventario.model.ComponenteDetalhe;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Diálogo para exibir detalhes dos componentes de um item composto.
 * Mostra informações do patrimônio e lista todos os componentes com status.
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class DetalheItemCompostoDialog extends JDialog {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    
    private final String numeroPatrimonio;
    private final String descricaoPatrimonio;
    private final String nomeSala;
    private final String nomeResponsavel;
    private final List<ComponenteDetalhe> componentes;
    
    private JTable tblComponentes;
    private DefaultTableModel modelComponentes;
    
    public DetalheItemCompostoDialog(Frame parent, String numeroPatrimonio, String descricaoPatrimonio,
                                      String nomeSala, String nomeResponsavel, 
                                      List<ComponenteDetalhe> componentes) {
        super(parent, "Detalhes do Item Composto", true);
        
        this.numeroPatrimonio = numeroPatrimonio;
        this.descricaoPatrimonio = descricaoPatrimonio;
        this.nomeSala = nomeSala;
        this.nomeResponsavel = nomeResponsavel;
        this.componentes = componentes;
        
        initializeComponents();
        setupLayout();
        preencherDados();
        
        setSize(850, 550);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    private void initializeComponents() {
        String[] colunas = {"Tipo", "Descrição", "Qtd. Esperada", "Qtd. Encontrada", 
                           "Faltante", "Status", "Observação", "Data Coleta"};
        
        modelComponentes = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tblComponentes = new JTable(modelComponentes);
        tblComponentes.setFont(new Font("Arial", Font.PLAIN, 12));
        tblComponentes.setRowHeight(28);
        tblComponentes.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tblComponentes.setFillsViewportHeight(true);
        
        // Renderizador para status
        tblComponentes.getColumnModel().getColumn(5).setCellRenderer(new StatusComponenteRenderer());
        
        // Ajustar larguras
        tblComponentes.getColumnModel().getColumn(0).setPreferredWidth(100);
        tblComponentes.getColumnModel().getColumn(1).setPreferredWidth(180);
        tblComponentes.getColumnModel().getColumn(2).setPreferredWidth(80);
        tblComponentes.getColumnModel().getColumn(3).setPreferredWidth(90);
        tblComponentes.getColumnModel().getColumn(4).setPreferredWidth(60);
        tblComponentes.getColumnModel().getColumn(5).setPreferredWidth(90);
        tblComponentes.getColumnModel().getColumn(6).setPreferredWidth(120);
        tblComponentes.getColumnModel().getColumn(7).setPreferredWidth(110);
    }

    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(236, 240, 241));
        
        // Painel de informações do patrimônio
        JPanel panelInfo = createPanelInfo();
        
        // Painel da tabela de componentes
        JPanel panelTabela = createPanelTabela();
        
        // Painel de botões
        JPanel panelBotoes = createPanelBotoes();
        
        mainPanel.add(panelInfo, BorderLayout.NORTH);
        mainPanel.add(panelTabela, BorderLayout.CENTER);
        mainPanel.add(panelBotoes, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createPanelInfo() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 15, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(155, 89, 182), 2),
                "Informações do Patrimônio",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13),
                new Color(155, 89, 182)
            ),
            new EmptyBorder(10, 15, 10, 15)
        ));
        
        panel.add(createInfoLabel("Nº Patrimônio:", numeroPatrimonio));
        panel.add(createInfoLabel("Descrição:", descricaoPatrimonio));
        panel.add(createInfoLabel("Sala:", nomeSala != null ? nomeSala : "Não informada"));
        panel.add(createInfoLabel("Responsável:", nomeResponsavel != null ? nomeResponsavel : "Não informado"));
        
        return panel;
    }
    
    private JPanel createInfoLabel(String label, String value) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setOpaque(false);
        
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Arial", Font.BOLD, 12));
        lblLabel.setForeground(new Color(127, 140, 141));
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Arial", Font.PLAIN, 12));
        lblValue.setForeground(new Color(44, 62, 80));
        
        panel.add(lblLabel);
        panel.add(lblValue);
        
        return panel;
    }
    
    private JPanel createPanelTabela() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
            "Componentes",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 13),
            new Color(52, 152, 219)
        ));
        
        JScrollPane scrollPane = new JScrollPane(tblComponentes);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createPanelBotoes() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panel.setOpaque(false);
        
        JButton btnFechar = new JButton("Fechar");
        btnFechar.setFont(new Font("Arial", Font.BOLD, 12));
        btnFechar.setForeground(Color.WHITE);
        btnFechar.setBackground(new Color(127, 140, 141));
        btnFechar.setFocusPainted(false);
        btnFechar.setBorderPainted(false);
        btnFechar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnFechar.setPreferredSize(new Dimension(100, 32));
        btnFechar.addActionListener(e -> dispose());
        
        panel.add(btnFechar);
        
        return panel;
    }
    
    private void preencherDados() {
        modelComponentes.setRowCount(0);
        
        for (ComponenteDetalhe comp : componentes) {
            String dataColeta = comp.getDataColeta() != null ? 
                    DATE_FORMAT.format(comp.getDataColeta()) : "-";
            
            modelComponentes.addRow(new Object[]{
                comp.getTipo(),
                comp.getDescricao(),
                comp.getQuantidadeEsperada(),
                comp.getQuantidadeEncontrada(),
                comp.getQuantidadeFaltante(),
                comp.getStatus().getDescricao(),
                comp.getObservacao() != null ? comp.getObservacao() : "",
                dataColeta
            });
        }
    }
    
    /**
     * Renderizador para colorir status dos componentes
     */
    private class StatusComponenteRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, 
                isSelected, hasFocus, row, column);
            
            if (!isSelected && value != null) {
                String status = value.toString();
                switch (status) {
                    case "Encontrado":
                        c.setBackground(new Color(212, 239, 223));
                        c.setForeground(new Color(39, 174, 96));
                        break;
                    case "Faltante":
                        c.setBackground(new Color(250, 219, 216));
                        c.setForeground(new Color(231, 76, 60));
                        break;
                    case "Parcial":
                        c.setBackground(new Color(252, 243, 207));
                        c.setForeground(new Color(241, 196, 15));
                        break;
                    case "Não Coletado":
                        c.setBackground(new Color(235, 237, 239));
                        c.setForeground(new Color(127, 140, 141));
                        break;
                    default:
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                }
            } else if (isSelected) {
                c.setBackground(table.getSelectionBackground());
                c.setForeground(table.getSelectionForeground());
            }
            
            setHorizontalAlignment(CENTER);
            return c;
        }
    }
}
