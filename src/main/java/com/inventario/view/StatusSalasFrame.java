package com.inventario.view;

import com.inventario.model.Inventario;
import com.inventario.service.DashboardService;
import com.inventario.view.ui.ButtonStyleFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Frame para acompanhamento do status de coleta por sala
 */
public class StatusSalasFrame extends JFrame {

    private final DashboardService dashboardService;
    private int idInventarioAtivo = -1;
    private String nomeInventarioAtivo = "Nenhum inventário ativo";

    private JTable tabelaStatus;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    private JComboBox<String> comboFiltroSetor;
    private JComboBox<String> comboFiltroStatus;
    private JLabel labelTotalSalas;
    private JLabel labelSalasConcluidas;
    private JLabel labelProgressoGeral;

    private List<Map<String, Object>> dadosAtuais;

    public StatusSalasFrame() {
        this.dashboardService = new DashboardService();
        obterInventarioAtivo();
        initializeComponents();
        carregarDados();
    }

    private void obterInventarioAtivo() {
        Inventario inventario = dashboardService.obterInventarioAtivo();
        if (inventario != null) {
            this.idInventarioAtivo = inventario.getId();
            this.nomeInventarioAtivo = inventario.getNome();
        }
    }

    private void initializeComponents() {
        setTitle("Status de Coleta por Sala - " + nomeInventarioAtivo);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Painel Superior (Filtros e Resumo)
        add(criarPainelSuperior(), BorderLayout.NORTH);

        // Tabela Central
        add(criarPainelTabela(), BorderLayout.CENTER);

        // Painel Inferior (Ações)
        add(criarPainelInferior(), BorderLayout.SOUTH);
    }

    private JPanel criarPainelSuperior() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(248, 249, 250));

        // Título e Resumo
        JPanel panelResumo = new JPanel(new GridLayout(1, 3, 20, 0));
        panelResumo.setOpaque(false);

        labelTotalSalas = criarCardResumo("Total de Salas", "0");
        labelSalasConcluidas = criarCardResumo("Salas Concluídas", "0");
        labelProgressoGeral = criarCardResumo("Progresso Geral", "0%");

        panelResumo.add(labelTotalSalas);
        panelResumo.add(labelSalasConcluidas);
        panelResumo.add(labelProgressoGeral);

        // Filtros
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFiltros.setOpaque(false);
        panelFiltros.setBorder(new EmptyBorder(15, 0, 0, 0));

        panelFiltros.add(new JLabel("Filtrar por Setor:"));
        comboFiltroSetor = new JComboBox<>();
        comboFiltroSetor.addItem("Todos");
        comboFiltroSetor.addActionListener(e -> aplicarFiltros());
        panelFiltros.add(comboFiltroSetor);

        panelFiltros.add(Box.createHorizontalStrut(20));

        panelFiltros.add(new JLabel("Filtrar por Status:"));
        comboFiltroStatus = new JComboBox<>(new String[] { "Todos", "Concluído", "Em Andamento", "Não Iniciado" });
        comboFiltroStatus.addActionListener(e -> aplicarFiltros());
        panelFiltros.add(comboFiltroStatus);

        JButton btnLimparFiltros = new JButton("Limpar Filtros");
        btnLimparFiltros.addActionListener(e -> {
            comboFiltroSetor.setSelectedIndex(0);
            comboFiltroStatus.setSelectedIndex(0);
        });
        panelFiltros.add(Box.createHorizontalStrut(10));
        panelFiltros.add(btnLimparFiltros);

        panel.add(panelResumo, BorderLayout.NORTH);
        panel.add(panelFiltros, BorderLayout.SOUTH);

        return panel;
    }

    private JLabel criarCardResumo(String titulo, String valorInicial) {
        JLabel label = new JLabel("<html><div style='text-align: center; font-family: Segoe UI Emoji;'>" +
                "<span style='font-size: 12px; color: #7f8c8d;'>" + titulo + "</span><br>" +
                "<span style='font-size: 24px; font-weight: bold; color: #2c3e50;'>" + valorInicial + "</span>" +
                "</div></html>");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 221, 225)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        return label;
    }

    private JPanel criarPainelTabela() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(0, 15, 0, 15));
        panel.setBackground(new Color(248, 249, 250));

        String[] colunas = { "Setor", "Sala", "Total Itens", "Coletados", "Progresso", "Status" };
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2 || columnIndex == 3)
                    return Integer.class; // Total e Coletados
                return Object.class;
            }
        };

        tabelaStatus = new JTable(tableModel);
        tabelaStatus.setRowHeight(30);
        tabelaStatus.getTableHeader().setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));

        // Configurar renderizadores
        StatusRenderer statusRenderer = new StatusRenderer();
        tabelaStatus.getColumnModel().getColumn(4).setCellRenderer(new ProgressoRenderer());
        tabelaStatus.getColumnModel().getColumn(5).setCellRenderer(statusRenderer);

        // Configurar ordenação
        sorter = new TableRowSorter<>(tableModel);
        tabelaStatus.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(tabelaStatus);
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel criarPainelInferior() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(248, 249, 250));

        JButton btnAtualizar = ButtonStyleFactory.createPrimaryButton("Atualizar Dados");
        btnAtualizar.addActionListener(e -> carregarDados());

        JButton btnFechar = ButtonStyleFactory.createDangerButton("Fechar");
        btnFechar.addActionListener(e -> dispose());

        panel.add(btnAtualizar);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(btnFechar);

        return panel;
    }

    private void carregarDados() {
        if (idInventarioAtivo == -1) {
            JOptionPane.showMessageDialog(this, "Nenhum inventário ativo encontrado.", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        new SwingWorker<List<Map<String, Object>>, Void>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                return dashboardService.obterEstatisticasPorSala(idInventarioAtivo);
            }

            @Override
            protected void done() {
                try {
                    dadosAtuais = get();
                    atualizarTabela(dadosAtuais);
                    atualizarResumo(dadosAtuais);
                    atualizarFiltroSetores(dadosAtuais);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(StatusSalasFrame.this, "Erro ao carregar dados: " + e.getMessage(),
                            "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void atualizarTabela(List<Map<String, Object>> dados) {
        tableModel.setRowCount(0);
        for (Map<String, Object> sala : dados) {
            Object[] row = {
                    sala.get("setor"),
                    sala.get("sala"),
                    sala.get("total_patrimonios"),
                    sala.get("itens_coletados"),
                    String.format("%.1f%%", (Double) sala.get("percentual")),
                    sala.get("status")
            };
            tableModel.addRow(row);
        }
        aplicarFiltros(); // Reaplicar filtros se houver
    }

    private void atualizarResumo(List<Map<String, Object>> dados) {
        int totalSalas = dados.size();
        long salasConcluidas = dados.stream()
                .filter(m -> "Concluído".equals(m.get("status")))
                .count();

        int totalItens = dados.stream().mapToInt(m -> (Integer) m.get("total_patrimonios")).sum();
        int totalColetados = dados.stream().mapToInt(m -> (Integer) m.get("itens_coletados")).sum();
        double progressoGeral = totalItens > 0 ? (double) totalColetados / totalItens * 100.0 : 0.0;

        labelTotalSalas.setText("<html><div style='text-align: center;'>" +
                "<span style='font-size: 12px; color: #7f8c8d;'>Total de Salas</span><br>" +
                "<span style='font-size: 24px; font-weight: bold; color: #2c3e50;'>" + totalSalas + "</span>" +
                "</div></html>");

        labelSalasConcluidas.setText("<html><div style='text-align: center;'>" +
                "<span style='font-size: 12px; color: #7f8c8d;'>Salas Concluídas</span><br>" +
                "<span style='font-size: 24px; font-weight: bold; color: #27ae60;'>" + salasConcluidas + "</span>" +
                "</div></html>");

        labelProgressoGeral.setText("<html><div style='text-align: center;'>" +
                "<span style='font-size: 12px; color: #7f8c8d;'>Progresso Geral</span><br>" +
                "<span style='font-size: 24px; font-weight: bold; color: #2980b9;'>"
                + String.format("%.1f%%", progressoGeral) + "</span>" +
                "</div></html>");
    }

    private void atualizarFiltroSetores(List<Map<String, Object>> dados) {
        String selecaoAtual = (String) comboFiltroSetor.getSelectedItem();
        Set<String> setores = new TreeSet<>();
        for (Map<String, Object> sala : dados) {
            setores.add((String) sala.get("setor"));
        }

        comboFiltroSetor.removeAllItems();
        comboFiltroSetor.addItem("Todos");
        for (String setor : setores) {
            comboFiltroSetor.addItem(setor);
        }

        if (selecaoAtual != null) {
            comboFiltroSetor.setSelectedItem(selecaoAtual);
        }
    }

    private void aplicarFiltros() {
        String setorSelecionado = (String) comboFiltroSetor.getSelectedItem();
        String statusSelecionado = (String) comboFiltroStatus.getSelectedItem();

        RowFilter<DefaultTableModel, Object> rf = new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                String setor = (String) entry.getValue(0);
                String status = (String) entry.getValue(5);

                boolean matchSetor = "Todos".equals(setorSelecionado) || setor.equals(setorSelecionado);
                boolean matchStatus = "Todos".equals(statusSelecionado) || status.equals(statusSelecionado);

                return matchSetor && matchStatus;
            }
        };
        sorter.setRowFilter(rf);
    }

    // Renderizadores Personalizados

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String status = (String) value;
            if ("Concluído".equals(status)) {
                c.setForeground(new Color(39, 174, 96));
                c.setFont(c.getFont().deriveFont(Font.BOLD));
            } else if ("Em Andamento".equals(status)) {
                c.setForeground(new Color(243, 156, 18));
                c.setFont(c.getFont().deriveFont(Font.BOLD));
            } else {
                c.setForeground(new Color(127, 140, 141));
                c.setFont(c.getFont().deriveFont(Font.PLAIN));
            }

            return c;
        }
    }

    private static class ProgressoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            JProgressBar progressBar = new JProgressBar(0, 100);
            String sValue = (String) value;
            int percent = (int) Double.parseDouble(sValue.replace("%", "").replace(",", "."));
            progressBar.setValue(percent);
            progressBar.setStringPainted(true);
            progressBar.setString(sValue);

            if (percent == 100) {
                progressBar.setForeground(new Color(39, 174, 96));
            } else if (percent > 0) {
                progressBar.setForeground(new Color(41, 128, 185));
            } else {
                progressBar.setForeground(new Color(127, 140, 141));
            }

            return progressBar;
        }
    }
}
