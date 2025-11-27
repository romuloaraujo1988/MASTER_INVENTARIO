package com.inventario.view;

import com.inventario.dao.ItemCompostoDAO;
import com.inventario.dao.InventarioDAO;
import com.inventario.model.Inventario;
import com.inventario.util.ExportadorExcel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Map;

/**
 * Frame para relatórios de coleta de itens compostos
 * Exibe estatísticas, detalhes e permite exportação
 */
public class RelatorioItensCompostosFrame extends JFrame {
    
    private ItemCompostoDAO itemCompostoDAO;
    private InventarioDAO inventarioDAO;
    private Inventario inventarioAtivo;
    
    // Componentes de filtro
    private JComboBox<String> cmbFiltroStatus;
    private JComboBox<String> cmbFiltroSala;
    private JTextField txtFiltroPesquisa;
    private JButton btnFiltrar;
    private JButton btnLimparFiltro;
    
    // Estatísticas
    private JLabel lblTotalPatrimonios;
    private JLabel lblTotalComponentes;
    private JLabel lblColetados;
    private JLabel lblPendentes;
    private JLabel lblPercentual;
    
    // Tabela principal
    private JTable tblRelatorio;
    private DefaultTableModel modelRelatorio;
    
    // Botões de ação
    private JButton btnExportarExcel;
    private JButton btnExportarPDF;
    private JButton btnAtualizar;
    private JButton btnFechar;
    
    public RelatorioItensCompostosFrame() {
        this.itemCompostoDAO = new ItemCompostoDAO();
        this.inventarioDAO = new InventarioDAO();
        
        carregarInventarioAtivo();
        initializeComponents();
        setupLayout();
        setupEventListeners();
        carregarDados();
        
        setTitle("Relatório de Itens Compostos - Inventário");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setIconImages(com.inventario.util.IconManager.getAppIconImages());
    }
    
    private void carregarInventarioAtivo() {
        try {
            this.inventarioAtivo = inventarioDAO.buscarPorStatus("EM_ANDAMENTO");
        } catch (Exception e) {
            System.err.println("Erro ao carregar inventário: " + e.getMessage());
        }
    }
    
    private void initializeComponents() {
        // Filtros
        cmbFiltroStatus = new JComboBox<>(new String[]{"Todos", "COMPLETO", "PARCIAL", "PENDENTE"});
        cmbFiltroSala = new JComboBox<>();
        cmbFiltroSala.addItem("Todas as Salas");
        carregarSalas();
        
        txtFiltroPesquisa = new JTextField(20);
        txtFiltroPesquisa.setToolTipText("Pesquisar por número ou descrição");
        
        btnFiltrar = createButton("🔍 Filtrar", new Color(52, 152, 219));
        btnLimparFiltro = createButton("🗑️ Limpar", new Color(149, 165, 166));
        
        // Labels de estatísticas
        lblTotalPatrimonios = createStatLabel("0");
        lblTotalComponentes = createStatLabel("0");
        lblColetados = createStatLabel("0");
        lblPendentes = createStatLabel("0");
        lblPercentual = createStatLabel("0%");
        
        // Tabela
        String[] colunas = {
            "Nº Patrimônio", "Descrição", "Sala", "Componente", 
            "Qtd. Esperada", "Qtd. Encontrada", "Local Encontrado", 
            "Status", "Data Coleta", "Coletor"
        };
        modelRelatorio = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblRelatorio = new JTable(modelRelatorio);
        tblRelatorio.setFont(new Font("Arial", Font.PLAIN, 11));
        tblRelatorio.setRowHeight(25);
        tblRelatorio.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        tblRelatorio.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblRelatorio.setFillsViewportHeight(true);
        
        // Renderizador de cores para status
        tblRelatorio.getColumnModel().getColumn(7).setCellRenderer(new StatusCellRenderer());
        
        // Ajustar larguras
        tblRelatorio.getColumnModel().getColumn(0).setPreferredWidth(80);
        tblRelatorio.getColumnModel().getColumn(1).setPreferredWidth(200);
        tblRelatorio.getColumnModel().getColumn(2).setPreferredWidth(120);
        tblRelatorio.getColumnModel().getColumn(3).setPreferredWidth(100);
        tblRelatorio.getColumnModel().getColumn(4).setPreferredWidth(70);
        tblRelatorio.getColumnModel().getColumn(5).setPreferredWidth(70);
        tblRelatorio.getColumnModel().getColumn(6).setPreferredWidth(150);
        tblRelatorio.getColumnModel().getColumn(7).setPreferredWidth(80);
        tblRelatorio.getColumnModel().getColumn(8).setPreferredWidth(100);
        tblRelatorio.getColumnModel().getColumn(9).setPreferredWidth(100);
        
        // Botões de ação
        btnExportarExcel = createButton("📊 Exportar Excel", new Color(39, 174, 96));
        btnExportarPDF = createButton("📄 Exportar PDF", new Color(231, 76, 60));
        btnAtualizar = createButton("🔄 Atualizar", new Color(52, 152, 219));
        btnFechar = createButton("❌ Fechar", new Color(127, 140, 141));
    }
    
    private void carregarSalas() {
        try {
            com.inventario.dao.SalaDAO salaDAO = new com.inventario.dao.SalaDAO();
            List<com.inventario.model.Sala> salas = salaDAO.listarSalas();
            for (com.inventario.model.Sala sala : salas) {
                String item = sala.getDescricao();
                if (sala.getNumero() != null && !sala.getNumero().isEmpty()) {
                    item = sala.getNumero() + " - " + sala.getDescricao();
                }
                cmbFiltroSala.addItem(item);
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar salas: " + e.getMessage());
        }
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(236, 240, 241));
        
        // Painel superior - Título e Inventário
        JPanel panelTitulo = createPanelTitulo();
        
        // Painel de estatísticas
        JPanel panelEstatisticas = createPanelEstatisticas();
        
        // Painel de filtros
        JPanel panelFiltros = createPanelFiltros();
        
        // Painel superior combinado
        JPanel panelSuperior = new JPanel(new BorderLayout(10, 10));
        panelSuperior.setOpaque(false);
        panelSuperior.add(panelTitulo, BorderLayout.NORTH);
        panelSuperior.add(panelEstatisticas, BorderLayout.CENTER);
        panelSuperior.add(panelFiltros, BorderLayout.SOUTH);
        
        // Painel central - Tabela
        JPanel panelTabela = createPanelTabela();
        
        // Painel inferior - Botões
        JPanel panelBotoes = createPanelBotoes();
        
        mainPanel.add(panelSuperior, BorderLayout.NORTH);
        mainPanel.add(panelTabela, BorderLayout.CENTER);
        mainPanel.add(panelBotoes, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createPanelTitulo() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(new Color(155, 89, 182));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel lblTitulo = new JLabel("📊 Relatório de Coleta - Itens Compostos");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);
        
        JLabel lblInventario = new JLabel("| Inventário: " + 
            (inventarioAtivo != null ? inventarioAtivo.getNome() : "Nenhum ativo"));
        lblInventario.setFont(new Font("Arial", Font.PLAIN, 14));
        lblInventario.setForeground(Color.WHITE);
        
        panel.add(lblTitulo);
        panel.add(lblInventario);
        
        return panel;
    }
    
    private JPanel createPanelEstatisticas() {
        JPanel panel = new JPanel(new GridLayout(1, 5, 15, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        panel.add(createStatCard("📦 Patrimônios", lblTotalPatrimonios, new Color(52, 152, 219)));
        panel.add(createStatCard("🔧 Componentes", lblTotalComponentes, new Color(155, 89, 182)));
        panel.add(createStatCard("✓ Coletados", lblColetados, new Color(46, 204, 113)));
        panel.add(createStatCard("⏳ Pendentes", lblPendentes, new Color(241, 196, 15)));
        panel.add(createStatCard("📈 Progresso", lblPercentual, new Color(52, 73, 94)));
        
        return panel;
    }
    
    private JPanel createStatCard(String titulo, JLabel lblValor, Color cor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createMatteBorder(0, 4, 0, 0, cor));
        
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Arial", Font.PLAIN, 12));
        lblTitulo.setForeground(new Color(127, 140, 141));
        lblTitulo.setBorder(new EmptyBorder(0, 10, 0, 0));
        
        lblValor.setFont(new Font("Arial", Font.BOLD, 24));
        lblValor.setForeground(cor);
        lblValor.setBorder(new EmptyBorder(0, 10, 0, 0));
        
        card.add(lblTitulo, BorderLayout.NORTH);
        card.add(lblValor, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createPanelFiltros() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        panel.add(new JLabel("Status:"));
        panel.add(cmbFiltroStatus);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(new JLabel("Sala:"));
        panel.add(cmbFiltroSala);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(new JLabel("Pesquisar:"));
        panel.add(txtFiltroPesquisa);
        panel.add(btnFiltrar);
        panel.add(btnLimparFiltro);
        
        return panel;
    }
    
    private JPanel createPanelTabela() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
            "Detalhes da Coleta",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 13),
            new Color(52, 152, 219)
        ));
        
        JScrollPane scrollPane = new JScrollPane(tblRelatorio);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createPanelBotoes() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBackground(new Color(236, 240, 241));
        
        panel.add(btnAtualizar);
        panel.add(btnExportarExcel);
        panel.add(btnExportarPDF);
        panel.add(btnFechar);
        
        return panel;
    }
    
    private void setupEventListeners() {
        btnFiltrar.addActionListener(e -> aplicarFiltros());
        btnLimparFiltro.addActionListener(e -> limparFiltros());
        btnAtualizar.addActionListener(e -> carregarDados());
        btnExportarExcel.addActionListener(e -> exportarExcel());
        btnExportarPDF.addActionListener(e -> exportarPDF());
        btnFechar.addActionListener(e -> dispose());
        
        txtFiltroPesquisa.addActionListener(e -> aplicarFiltros());
    }
    
    private void carregarDados() {
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            modelRelatorio.setRowCount(0);
            
            if (inventarioAtivo == null) {
                JOptionPane.showMessageDialog(this,
                    "Nenhum inventário ativo encontrado.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Carregar dados do relatório
            List<Map<String, Object>> dados = itemCompostoDAO.buscarRelatorioCompleto(inventarioAtivo.getId());
            
            int totalPatrimonios = 0;
            int totalComponentes = 0;
            int coletados = 0;
            int pendentes = 0;
            String ultimoPatrimonio = "";
            
            for (Map<String, Object> row : dados) {
                String numeroPatrimonio = (String) row.get("numero");
                if (!numeroPatrimonio.equals(ultimoPatrimonio)) {
                    totalPatrimonios++;
                    ultimoPatrimonio = numeroPatrimonio;
                }
                totalComponentes++;
                
                String status = (String) row.get("status");
                if ("COMPLETO".equals(status)) {
                    coletados++;
                } else {
                    pendentes++;
                }
                
                modelRelatorio.addRow(new Object[]{
                    row.get("numero"),
                    truncarTexto((String) row.get("descricao"), 40),
                    row.get("sala"),
                    row.get("tipoComponente"),
                    row.get("qtdEsperada"),
                    row.get("qtdEncontrada"),
                    row.get("localEncontrado") != null ? row.get("localEncontrado") : "-",
                    status,
                    row.get("dataColeta") != null ? row.get("dataColeta") : "-",
                    row.get("coletor") != null ? row.get("coletor") : "-"
                });
            }
            
            // Atualizar estatísticas
            lblTotalPatrimonios.setText(String.valueOf(totalPatrimonios));
            lblTotalComponentes.setText(String.valueOf(totalComponentes));
            lblColetados.setText(String.valueOf(coletados));
            lblPendentes.setText(String.valueOf(pendentes));
            
            double percentual = totalComponentes > 0 ? 
                (coletados * 100.0 / totalComponentes) : 0;
            lblPercentual.setText(String.format("%.1f%%", percentual));
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erro ao carregar dados: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
    
    private void aplicarFiltros() {
        // Implementar filtros na tabela
        carregarDados(); // Por enquanto recarrega tudo
        // TODO: Implementar filtro local na tabela
    }
    
    private void limparFiltros() {
        cmbFiltroStatus.setSelectedIndex(0);
        cmbFiltroSala.setSelectedIndex(0);
        txtFiltroPesquisa.setText("");
        carregarDados();
    }
    
    private void exportarExcel() {
        JOptionPane.showMessageDialog(this,
            "Exportação para Excel será implementada em breve.",
            "Em Desenvolvimento",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void exportarPDF() {
        JOptionPane.showMessageDialog(this,
            "Exportação para PDF será implementada em breve.",
            "Em Desenvolvimento",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private String truncarTexto(String texto, int maxLength) {
        if (texto == null) return "";
        if (texto.length() <= maxLength) return texto;
        return texto.substring(0, maxLength - 3) + "...";
    }
    
    private JLabel createStatLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        return label;
    }
    
    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(150, 35));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    /**
     * Renderizador de células para colorir status
     */
    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, 
                isSelected, hasFocus, row, column);
            
            if (!isSelected && value != null) {
                String status = value.toString();
                switch (status) {
                    case "COMPLETO":
                        c.setBackground(new Color(212, 239, 223));
                        c.setForeground(new Color(39, 174, 96));
                        break;
                    case "PARCIAL":
                        c.setBackground(new Color(252, 243, 207));
                        c.setForeground(new Color(241, 196, 15));
                        break;
                    case "PENDENTE":
                        c.setBackground(new Color(250, 219, 216));
                        c.setForeground(new Color(231, 76, 60));
                        break;
                    default:
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                }
            }
            
            setHorizontalAlignment(CENTER);
            return c;
        }
    }
}
