package com.inventario.view;

import com.inventario.dao.ReconciliacaoDAO;
import com.inventario.dao.InventarioDAO;
import com.inventario.model.Inventario;
import com.inventario.model.Usuario;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * Tela de Reconciliação de Patrimônios
 * Relaciona itens NÃO ENCONTRADOS com itens SEM ETIQUETA
 */
public class ReconciliacaoFrame extends JFrame {

    private final ReconciliacaoDAO reconciliacaoDAO;
    private final InventarioDAO inventarioDAO;
    private final Usuario usuarioLogado;
    
    // Componentes
    private JComboBox<Inventario> cmbInventario;
    private JSlider sliderSimilaridade;
    private JLabel lblSimilaridade;
    
    // Tabelas
    private JTable tblNaoEncontrados;
    private JTable tblSemEtiqueta;
    private JTable tblSugestoes;
    private JTable tblReconciliacoes;
    
    // Models
    private DefaultTableModel modelNaoEncontrados;
    private DefaultTableModel modelSemEtiqueta;
    private DefaultTableModel modelSugestoes;
    private DefaultTableModel modelReconciliacoes;
    
    // Labels de estatísticas
    private JLabel lblTotalNaoEncontrados;
    private JLabel lblTotalSemEtiqueta;
    private JLabel lblTotalSugestoes;
    private JLabel lblReconciliacoesPendentes;
    private JLabel lblReconciliacoesConfirmadas;

    public ReconciliacaoFrame(Usuario usuario) {
        this.usuarioLogado = usuario;
        this.reconciliacaoDAO = new ReconciliacaoDAO();
        this.inventarioDAO = new InventarioDAO();
        
        initComponents();
        carregarInventarios();
    }

    private void initComponents() {
        setTitle("Reconciliação de Patrimônios - Não Encontrados x Sem Etiqueta");
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Painel superior - Filtros e estatísticas
        mainPanel.add(criarPainelSuperior(), BorderLayout.NORTH);

        // Painel central - Abas
        mainPanel.add(criarPainelAbas(), BorderLayout.CENTER);

        // Painel inferior - Botões
        mainPanel.add(criarPainelBotoes(), BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel criarPainelSuperior() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Filtros
        JPanel filtrosPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filtrosPanel.setBorder(new TitledBorder("Filtros"));

        filtrosPanel.add(new JLabel("Inventário:"));
        cmbInventario = new JComboBox<>();
        cmbInventario.setPreferredSize(new Dimension(300, 25));
        cmbInventario.addActionListener(e -> carregarDados());
        filtrosPanel.add(cmbInventario);

        filtrosPanel.add(Box.createHorizontalStrut(20));
        filtrosPanel.add(new JLabel("Similaridade mínima:"));
        
        sliderSimilaridade = new JSlider(0, 100, 50);
        sliderSimilaridade.setPreferredSize(new Dimension(150, 25));
        sliderSimilaridade.addChangeListener(e -> {
            lblSimilaridade.setText(sliderSimilaridade.getValue() + "%");
        });
        filtrosPanel.add(sliderSimilaridade);
        
        lblSimilaridade = new JLabel("50%");
        filtrosPanel.add(lblSimilaridade);

        JButton btnBuscarSugestoes = new JButton("🔍 Buscar Sugestões");
        btnBuscarSugestoes.addActionListener(e -> buscarSugestoes());
        filtrosPanel.add(btnBuscarSugestoes);

        panel.add(filtrosPanel, BorderLayout.CENTER);

        // Estatísticas
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        statsPanel.setBorder(new TitledBorder("Estatísticas"));

        lblTotalNaoEncontrados = new JLabel("Não Encontrados: 0");
        lblTotalNaoEncontrados.setForeground(new Color(220, 53, 69));
        statsPanel.add(lblTotalNaoEncontrados);

        lblTotalSemEtiqueta = new JLabel("Sem Etiqueta: 0");
        lblTotalSemEtiqueta.setForeground(new Color(255, 193, 7));
        statsPanel.add(lblTotalSemEtiqueta);

        lblTotalSugestoes = new JLabel("Sugestões: 0");
        lblTotalSugestoes.setForeground(new Color(0, 123, 255));
        statsPanel.add(lblTotalSugestoes);

        lblReconciliacoesPendentes = new JLabel("Pendentes: 0");
        lblReconciliacoesPendentes.setForeground(new Color(108, 117, 125));
        statsPanel.add(lblReconciliacoesPendentes);

        lblReconciliacoesConfirmadas = new JLabel("Confirmadas: 0");
        lblReconciliacoesConfirmadas.setForeground(new Color(40, 167, 69));
        statsPanel.add(lblReconciliacoesConfirmadas);

        panel.add(statsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JTabbedPane criarPainelAbas() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // Aba 1: Sugestões de Reconciliação
        tabbedPane.addTab("🔗 Sugestões de Reconciliação", criarAbaSugestoes());

        // Aba 2: Patrimônios Não Encontrados
        tabbedPane.addTab("❌ Não Encontrados", criarAbaNaoEncontrados());

        // Aba 3: Itens Sem Etiqueta
        tabbedPane.addTab("🏷️ Sem Etiqueta", criarAbaSemEtiqueta());

        // Aba 4: Histórico de Reconciliações
        tabbedPane.addTab("📋 Histórico", criarAbaHistorico());

        return tabbedPane;
    }

    private JPanel criarAbaSugestoes() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Tabela de sugestões
        String[] colunas = {"", "Patrimônio", "Descrição Patrimônio", "Sala Esperada", 
                           "Item Sem Etiqueta", "Local Encontrado", "Similaridade"};
        modelSugestoes = new DefaultTableModel(colunas, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : String.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };

        tblSugestoes = new JTable(modelSugestoes);
        tblSugestoes.setRowHeight(25);
        tblSugestoes.getColumnModel().getColumn(0).setMaxWidth(30);
        tblSugestoes.getColumnModel().getColumn(1).setPreferredWidth(80);
        tblSugestoes.getColumnModel().getColumn(2).setPreferredWidth(250);
        tblSugestoes.getColumnModel().getColumn(3).setPreferredWidth(100);
        tblSugestoes.getColumnModel().getColumn(4).setPreferredWidth(250);
        tblSugestoes.getColumnModel().getColumn(5).setPreferredWidth(100);
        tblSugestoes.getColumnModel().getColumn(6).setPreferredWidth(80);

        // Colorir similaridade
        tblSugestoes.getColumnModel().getColumn(6).setCellRenderer(new SimilaridadeRenderer());

        JScrollPane scrollPane = new JScrollPane(tblSugestoes);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Botões de ação
        JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton btnConfirmarSelecionados = new JButton("✅ Confirmar Selecionados");
        btnConfirmarSelecionados.setBackground(new Color(40, 167, 69));
        btnConfirmarSelecionados.setForeground(Color.WHITE);
        btnConfirmarSelecionados.addActionListener(e -> confirmarSelecionados());
        botoesPanel.add(btnConfirmarSelecionados);

        JButton btnRejeitarSelecionados = new JButton("❌ Rejeitar Selecionados");
        btnRejeitarSelecionados.setBackground(new Color(220, 53, 69));
        btnRejeitarSelecionados.setForeground(Color.WHITE);
        btnRejeitarSelecionados.addActionListener(e -> rejeitarSelecionados());
        botoesPanel.add(btnRejeitarSelecionados);

        panel.add(botoesPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel criarAbaNaoEncontrados() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] colunas = {"ID", "Número", "Descrição", "Estado", "Sala Esperada", "Responsável"};
        modelNaoEncontrados = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblNaoEncontrados = new JTable(modelNaoEncontrados);
        tblNaoEncontrados.setRowHeight(25);
        tblNaoEncontrados.getColumnModel().getColumn(0).setMaxWidth(60);
        tblNaoEncontrados.getColumnModel().getColumn(1).setPreferredWidth(100);
        tblNaoEncontrados.getColumnModel().getColumn(2).setPreferredWidth(300);
        tblNaoEncontrados.getColumnModel().getColumn(3).setPreferredWidth(80);
        tblNaoEncontrados.getColumnModel().getColumn(4).setPreferredWidth(150);
        tblNaoEncontrados.getColumnModel().getColumn(5).setPreferredWidth(150);

        JScrollPane scrollPane = new JScrollPane(tblNaoEncontrados);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Botões de ação
        JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton btnAcoes = new JButton("⚡ Ações");
        btnAcoes.setBackground(new Color(0, 123, 255));
        btnAcoes.setForeground(Color.WHITE);
        btnAcoes.setToolTipText("Abrir painel de ações para o patrimônio selecionado");
        btnAcoes.addActionListener(e -> abrirAcoesPatrimonio());
        botoesPanel.add(btnAcoes);
        
        JButton btnVincularManual = new JButton("🔗 Vincular Manualmente");
        btnVincularManual.addActionListener(e -> vincularManualmente());
        botoesPanel.add(btnVincularManual);
        
        panel.add(botoesPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel criarAbaSemEtiqueta() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] colunas = {"ID", "Descrição", "Categoria", "Local Encontrado", "Estado", "Data Coleta", "Coletor"};
        modelSemEtiqueta = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblSemEtiqueta = new JTable(modelSemEtiqueta);
        tblSemEtiqueta.setRowHeight(25);
        tblSemEtiqueta.getColumnModel().getColumn(0).setMaxWidth(60);
        tblSemEtiqueta.getColumnModel().getColumn(1).setPreferredWidth(300);
        tblSemEtiqueta.getColumnModel().getColumn(2).setPreferredWidth(120);
        tblSemEtiqueta.getColumnModel().getColumn(3).setPreferredWidth(120);
        tblSemEtiqueta.getColumnModel().getColumn(4).setPreferredWidth(80);
        tblSemEtiqueta.getColumnModel().getColumn(5).setPreferredWidth(120);
        tblSemEtiqueta.getColumnModel().getColumn(6).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(tblSemEtiqueta);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel criarAbaHistorico() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] colunas = {"ID", "Patrimônio", "Descrição Patrimônio", "Item Sem Etiqueta", 
                           "Local Encontrado", "Similaridade", "Status", "Data", "Usuário"};
        modelReconciliacoes = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblReconciliacoes = new JTable(modelReconciliacoes);
        tblReconciliacoes.setRowHeight(25);
        tblReconciliacoes.getColumnModel().getColumn(0).setMaxWidth(50);
        tblReconciliacoes.getColumnModel().getColumn(1).setPreferredWidth(80);
        tblReconciliacoes.getColumnModel().getColumn(2).setPreferredWidth(200);
        tblReconciliacoes.getColumnModel().getColumn(3).setPreferredWidth(200);
        tblReconciliacoes.getColumnModel().getColumn(4).setPreferredWidth(100);
        tblReconciliacoes.getColumnModel().getColumn(5).setPreferredWidth(80);
        tblReconciliacoes.getColumnModel().getColumn(6).setPreferredWidth(80);
        tblReconciliacoes.getColumnModel().getColumn(7).setPreferredWidth(120);
        tblReconciliacoes.getColumnModel().getColumn(8).setPreferredWidth(100);

        // Colorir status
        tblReconciliacoes.getColumnModel().getColumn(6).setCellRenderer(new StatusRenderer());

        JScrollPane scrollPane = new JScrollPane(tblReconciliacoes);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Botões
        JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnExcluir = new JButton("🗑️ Excluir Selecionada");
        btnExcluir.addActionListener(e -> excluirReconciliacao());
        botoesPanel.add(btnExcluir);
        panel.add(botoesPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel criarPainelBotoes() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));

        JButton btnAtualizar = new JButton("🔄 Atualizar");
        btnAtualizar.addActionListener(e -> carregarDados());
        panel.add(btnAtualizar);

        JButton btnExportar = new JButton("📊 Exportar Relatório");
        btnExportar.addActionListener(e -> exportarRelatorio());
        panel.add(btnExportar);

        JButton btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(e -> dispose());
        panel.add(btnFechar);

        return panel;
    }

    // Renderer para colorir similaridade
    private class SimilaridadeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value != null) {
                String strValue = value.toString().replace("%", "");
                try {
                    double sim = Double.parseDouble(strValue);
                    if (sim >= 80) {
                        c.setBackground(new Color(212, 237, 218)); // Verde claro
                    } else if (sim >= 60) {
                        c.setBackground(new Color(255, 243, 205)); // Amarelo claro
                    } else {
                        c.setBackground(new Color(248, 215, 218)); // Vermelho claro
                    }
                } catch (NumberFormatException e) {
                    c.setBackground(Color.WHITE);
                }
            }
            
            if (isSelected) {
                c.setBackground(table.getSelectionBackground());
            }
            
            setHorizontalAlignment(CENTER);
            return c;
        }
    }

    // Renderer para colorir status
    private class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value != null) {
                String status = value.toString();
                switch (status) {
                    case "CONFIRMADO" -> {
                        c.setBackground(new Color(212, 237, 218));
                        c.setForeground(new Color(21, 87, 36));
                    }
                    case "REJEITADO" -> {
                        c.setBackground(new Color(248, 215, 218));
                        c.setForeground(new Color(114, 28, 36));
                    }
                    default -> {
                        c.setBackground(new Color(255, 243, 205));
                        c.setForeground(new Color(133, 100, 4));
                    }
                }
            }
            
            if (isSelected) {
                c.setBackground(table.getSelectionBackground());
                c.setForeground(table.getSelectionForeground());
            }
            
            setHorizontalAlignment(CENTER);
            return c;
        }
    }

    private void carregarInventarios() {
        try {
            List<Inventario> inventarios = inventarioDAO.findAll();
            cmbInventario.removeAllItems();
            for (Inventario inv : inventarios) {
                cmbInventario.addItem(inv);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao carregar inventários: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarDados() {
        Inventario inventario = (Inventario) cmbInventario.getSelectedItem();
        if (inventario == null) return;

        int idInventario = inventario.getId();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                carregarNaoEncontrados(idInventario);
                carregarSemEtiqueta(idInventario);
                carregarHistorico(idInventario);
                carregarEstatisticas(idInventario);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ReconciliacaoFrame.this,
                        "Erro ao carregar dados: " + e.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void carregarNaoEncontrados(int idInventario) throws SQLException {
        List<Map<String, Object>> dados = reconciliacaoDAO.buscarPatrimoniosNaoEncontrados(idInventario);
        
        SwingUtilities.invokeLater(() -> {
            modelNaoEncontrados.setRowCount(0);
            for (Map<String, Object> item : dados) {
                modelNaoEncontrados.addRow(new Object[]{
                    item.get("id"),
                    item.get("numero"),
                    item.get("descricao"),
                    item.get("estadoConservacao"),
                    item.get("salaEsperada"),
                    item.get("responsavel")
                });
            }
            lblTotalNaoEncontrados.setText("Não Encontrados: " + dados.size());
        });
    }

    private void carregarSemEtiqueta(int idInventario) throws SQLException {
        List<Map<String, Object>> dados = reconciliacaoDAO.buscarItensSemEtiqueta(idInventario);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        SwingUtilities.invokeLater(() -> {
            modelSemEtiqueta.setRowCount(0);
            for (Map<String, Object> item : dados) {
                java.sql.Timestamp dataColeta = (java.sql.Timestamp) item.get("dataColeta");
                modelSemEtiqueta.addRow(new Object[]{
                    item.get("id"),
                    item.get("descricao"),
                    item.get("categoria"),
                    item.get("localizacaoEncontrada"),
                    item.get("estadoEncontrado"),
                    dataColeta != null ? sdf.format(dataColeta) : "",
                    item.get("coletor")
                });
            }
            lblTotalSemEtiqueta.setText("Sem Etiqueta: " + dados.size());
        });
    }

    private void carregarHistorico(int idInventario) throws SQLException {
        List<Map<String, Object>> dados = reconciliacaoDAO.buscarReconciliacoesPorInventario(idInventario);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        SwingUtilities.invokeLater(() -> {
            modelReconciliacoes.setRowCount(0);
            for (Map<String, Object> item : dados) {
                java.sql.Timestamp dataRec = (java.sql.Timestamp) item.get("dataReconciliacao");
                Double sim = (Double) item.get("similaridade");
                modelReconciliacoes.addRow(new Object[]{
                    item.get("id"),
                    item.get("numeroPatrimonio"),
                    item.get("descricaoPatrimonio"),
                    item.get("descricaoItemSemEtiqueta"),
                    item.get("localizacaoEncontrada"),
                    sim != null ? String.format("%.1f%%", sim) : "",
                    item.get("status"),
                    dataRec != null ? sdf.format(dataRec) : "",
                    item.get("usuarioReconciliacao")
                });
            }
        });
    }

    private void carregarEstatisticas(int idInventario) throws SQLException {
        Map<String, Integer> stats = reconciliacaoDAO.contarEstatisticas(idInventario);
        
        SwingUtilities.invokeLater(() -> {
            lblReconciliacoesPendentes.setText("Pendentes: " + 
                stats.getOrDefault("reconciliacao_pendente", 0));
            lblReconciliacoesConfirmadas.setText("Confirmadas: " + 
                stats.getOrDefault("reconciliacao_confirmado", 0));
        });
    }

    private void buscarSugestoes() {
        Inventario inventario = (Inventario) cmbInventario.getSelectedItem();
        if (inventario == null) {
            JOptionPane.showMessageDialog(this, "Selecione um inventário", 
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int limiar = sliderSimilaridade.getValue();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                return reconciliacaoDAO.buscarSugestoesReconciliacao(inventario.getId(), limiar);
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    List<Map<String, Object>> sugestoes = get();
                    modelSugestoes.setRowCount(0);
                    
                    for (Map<String, Object> sugestao : sugestoes) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> patrimonio = (Map<String, Object>) sugestao.get("patrimonio");
                        @SuppressWarnings("unchecked")
                        Map<String, Object> itemSemEtiqueta = (Map<String, Object>) sugestao.get("itemSemEtiqueta");
                        Double similaridade = (Double) sugestao.get("similaridade");

                        modelSugestoes.addRow(new Object[]{
                            false, // Checkbox
                            patrimonio.get("numero"),
                            patrimonio.get("descricao"),
                            patrimonio.get("salaEsperada"),
                            itemSemEtiqueta.get("descricao"),
                            itemSemEtiqueta.get("localizacaoEncontrada"),
                            String.format("%.1f%%", similaridade)
                        });
                    }
                    
                    lblTotalSugestoes.setText("Sugestões: " + sugestoes.size());
                    
                    if (sugestoes.isEmpty()) {
                        JOptionPane.showMessageDialog(ReconciliacaoFrame.this,
                            "Nenhuma sugestão encontrada com similaridade >= " + limiar + "%",
                            "Informação", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ReconciliacaoFrame.this,
                        "Erro ao buscar sugestões: " + e.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void confirmarSelecionados() {
        processarSelecionados("CONFIRMADO");
    }

    private void rejeitarSelecionados() {
        processarSelecionados("REJEITADO");
    }

    private void processarSelecionados(String status) {
        Inventario inventario = (Inventario) cmbInventario.getSelectedItem();
        if (inventario == null) return;

        int count = 0;
        for (int i = 0; i < modelSugestoes.getRowCount(); i++) {
            Boolean selecionado = (Boolean) modelSugestoes.getValueAt(i, 0);
            if (selecionado != null && selecionado) {
                count++;
            }
        }

        if (count == 0) {
            JOptionPane.showMessageDialog(this, 
                "Selecione pelo menos uma sugestão", 
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String acao = status.equals("CONFIRMADO") ? "confirmar" : "rejeitar";
        int confirm = JOptionPane.showConfirmDialog(this,
            "Deseja " + acao + " " + count + " reconciliação(ões)?",
            "Confirmação", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        // Processar em background
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() throws Exception {
                int processados = 0;
                
                for (int i = modelSugestoes.getRowCount() - 1; i >= 0; i--) {
                    Boolean selecionado = (Boolean) modelSugestoes.getValueAt(i, 0);
                    if (selecionado != null && selecionado) {
                        // Buscar IDs correspondentes
                        String numeroPatrimonio = (String) modelSugestoes.getValueAt(i, 1);
                        String descItemSemEtiqueta = (String) modelSugestoes.getValueAt(i, 4);
                        String simStr = ((String) modelSugestoes.getValueAt(i, 6)).replace("%", "");
                        double similaridade = Double.parseDouble(simStr);

                        // Buscar IDs reais
                        int idPatrimonio = buscarIdPatrimonio(numeroPatrimonio);
                        int idColeta = buscarIdColetaSemEtiqueta(descItemSemEtiqueta, inventario.getId());

                        if (idPatrimonio > 0 && idColeta > 0) {
                            reconciliacaoDAO.registrarReconciliacao(
                                idPatrimonio, idColeta, inventario.getId(),
                                usuarioLogado.getId(), similaridade, status, null
                            );
                            processados++;
                        }
                    }
                }
                
                return processados;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    int processados = get();
                    JOptionPane.showMessageDialog(ReconciliacaoFrame.this,
                        processados + " reconciliação(ões) " + 
                        (status.equals("CONFIRMADO") ? "confirmada(s)" : "rejeitada(s)") + 
                        " com sucesso!",
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    carregarDados();
                    buscarSugestoes();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ReconciliacaoFrame.this,
                        "Erro ao processar: " + e.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private int buscarIdPatrimonio(String numero) throws SQLException {
        String sql = "SELECT id FROM tabela_patrimonio WHERE numero = ?";
        try (Connection conn = com.inventario.util.DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, numero);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return -1;
    }

    private int buscarIdColetaSemEtiqueta(String descricao, int idInventario) throws SQLException {
        String sql = "SELECT id FROM tabela_coleta WHERE descricao_item_sem_etiqueta = ? AND id_inventario = ? AND sem_etiqueta = true";
        try (Connection conn = com.inventario.util.DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, descricao);
            stmt.setInt(2, idInventario);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return -1;
    }

    private void abrirAcoesPatrimonio() {
        int row = tblNaoEncontrados.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, 
                "Selecione um patrimônio não encontrado na tabela",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Inventario inventario = (Inventario) cmbInventario.getSelectedItem();
        if (inventario == null) return;

        // Montar dados do patrimônio selecionado
        java.util.Map<String, Object> dadosPatrimonio = new java.util.HashMap<>();
        dadosPatrimonio.put("id", modelNaoEncontrados.getValueAt(row, 0));
        dadosPatrimonio.put("numero", modelNaoEncontrados.getValueAt(row, 1));
        dadosPatrimonio.put("descricao", modelNaoEncontrados.getValueAt(row, 2));
        dadosPatrimonio.put("estadoConservacao", modelNaoEncontrados.getValueAt(row, 3));
        dadosPatrimonio.put("salaEsperada", modelNaoEncontrados.getValueAt(row, 4));
        dadosPatrimonio.put("responsavel", modelNaoEncontrados.getValueAt(row, 5));

        // Abrir diálogo de ações
        AcoesPatrimonioDialog dialog = new AcoesPatrimonioDialog(
            this, dadosPatrimonio, inventario.getId(), usuarioLogado
        );
        dialog.setOnActionCompleted(() -> carregarDados());
        dialog.setVisible(true);
    }

    private void vincularManualmente() {
        int rowNaoEncontrado = tblNaoEncontrados.getSelectedRow();
        if (rowNaoEncontrado < 0) {
            JOptionPane.showMessageDialog(this, 
                "Selecione um patrimônio não encontrado na tabela",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Inventario inventario = (Inventario) cmbInventario.getSelectedItem();
        if (inventario == null) return;

        // Criar diálogo para selecionar item sem etiqueta
        JDialog dialog = new JDialog(this, "Vincular Item Sem Etiqueta", true);
        dialog.setSize(800, 500);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Info do patrimônio selecionado
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setBorder(new TitledBorder("Patrimônio Não Encontrado"));
        infoPanel.add(new JLabel("Número: " + modelNaoEncontrados.getValueAt(rowNaoEncontrado, 1)));
        infoPanel.add(new JLabel("Descrição: " + modelNaoEncontrados.getValueAt(rowNaoEncontrado, 2)));
        panel.add(infoPanel, BorderLayout.NORTH);

        // Tabela de itens sem etiqueta
        JTable tblSelecao = new JTable(modelSemEtiqueta);
        tblSelecao.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(tblSelecao), BorderLayout.CENTER);

        // Botões
        JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnVincular = new JButton("Vincular");
        btnVincular.addActionListener(e -> {
            int rowSemEtiqueta = tblSelecao.getSelectedRow();
            if (rowSemEtiqueta < 0) {
                JOptionPane.showMessageDialog(dialog, "Selecione um item sem etiqueta");
                return;
            }

            try {
                int idPatrimonio = (int) modelNaoEncontrados.getValueAt(rowNaoEncontrado, 0);
                int idColeta = (int) modelSemEtiqueta.getValueAt(rowSemEtiqueta, 0);
                
                String descPatrimonio = (String) modelNaoEncontrados.getValueAt(rowNaoEncontrado, 2);
                String descItem = (String) modelSemEtiqueta.getValueAt(rowSemEtiqueta, 1);
                double similaridade = reconciliacaoDAO.calcularSimilaridade(descPatrimonio, descItem);

                reconciliacaoDAO.registrarReconciliacao(
                    idPatrimonio, idColeta, inventario.getId(),
                    usuarioLogado.getId(), similaridade, "CONFIRMADO", 
                    "Vinculação manual"
                );

                JOptionPane.showMessageDialog(dialog, "Vinculação realizada com sucesso!");
                dialog.dispose();
                carregarDados();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Erro ao vincular: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        botoesPanel.add(btnVincular);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dialog.dispose());
        botoesPanel.add(btnCancelar);

        panel.add(botoesPanel, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void excluirReconciliacao() {
        int row = tblReconciliacoes.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, 
                "Selecione uma reconciliação para excluir",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Deseja realmente excluir esta reconciliação?",
            "Confirmação", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int id = (int) modelReconciliacoes.getValueAt(row, 0);
                reconciliacaoDAO.excluirReconciliacao(id);
                JOptionPane.showMessageDialog(this, "Reconciliação excluída com sucesso!");
                carregarDados();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, 
                    "Erro ao excluir: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportarRelatorio() {
        JOptionPane.showMessageDialog(this, 
            "Funcionalidade de exportação será implementada em breve.",
            "Em desenvolvimento", JOptionPane.INFORMATION_MESSAGE);
    }
}
