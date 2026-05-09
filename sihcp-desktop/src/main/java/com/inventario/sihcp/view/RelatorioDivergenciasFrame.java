package com.inventario.sihcp.view;

import com.inventario.sihcp.model.Inventario;
import com.inventario.sihcp.service.DashboardService;
import com.inventario.sihcp.service.RelatorioService;
import com.inventario.sihcp.view.ui.ButtonStyleFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Frame para relatório de divergências (automáticas e manuais)
 */
public class RelatorioDivergenciasFrame extends JFrame {

    private final DashboardService dashboardService;
    private final RelatorioService relatorioService;
    private int idInventarioAtivo = -1;
    private String nomeInventarioAtivo = "Nenhum inventário ativo";

    private JTable tabelaDivergencias;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    private JComboBox<String> comboFiltroTipo;
    private JLabel labelTotalDivergencias;
    private JLabel labelDivergenciasLocal;
    private JLabel labelDivergenciasEstado;

    private List<Map<String, Object>> dadosAtuais;

    public RelatorioDivergenciasFrame() {
        this.dashboardService = new DashboardService();
        this.relatorioService = new RelatorioService();
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
        setTitle("Relatório de Divergências - " + nomeInventarioAtivo);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 500);
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

        labelTotalDivergencias = criarCardResumo("Total de Divergências", "0", new Color(231, 76, 60));
        labelDivergenciasLocal = criarCardResumo("Divergências de Local", "0", new Color(243, 156, 18));
        labelDivergenciasEstado = criarCardResumo("Divergências de Estado", "0", new Color(52, 152, 219));

        panelResumo.add(labelTotalDivergencias);
        panelResumo.add(labelDivergenciasLocal);
        panelResumo.add(labelDivergenciasEstado);

        // Filtros
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFiltros.setOpaque(false);
        panelFiltros.setBorder(new EmptyBorder(15, 0, 0, 0));

        panelFiltros.add(new JLabel("Filtrar por Tipo:"));
        comboFiltroTipo = new JComboBox<>(new String[] { "Todos", "Localização", "Estado", "Manual", "Outro" });
        comboFiltroTipo.addActionListener(e -> aplicarFiltros());
        panelFiltros.add(comboFiltroTipo);

        JButton btnLimparFiltros = new JButton("Limpar Filtros");
        btnLimparFiltros.addActionListener(e -> comboFiltroTipo.setSelectedIndex(0));
        panelFiltros.add(Box.createHorizontalStrut(10));
        panelFiltros.add(btnLimparFiltros);

        panel.add(panelResumo, BorderLayout.NORTH);
        panel.add(panelFiltros, BorderLayout.SOUTH);

        return panel;
    }

    private JLabel criarCardResumo(String titulo, String valorInicial, Color corDestaque) {
        JLabel label = new JLabel("<html><div style='text-align: center;'>" +
                "<span style='font-size: 12px; color: #7f8c8d;'>" + titulo + "</span><br>" +
                "<span style='font-size: 24px; font-weight: bold; color: rgb(" + corDestaque.getRed() + ","
                + corDestaque.getGreen() + "," + corDestaque.getBlue() + ");'>" + valorInicial + "</span>" +
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

        String[] colunas = { "Patrimônio", "Descrição", "Tipo Divergência", "Valor Cadastrado", "Valor Encontrado",
                "Motivo" };
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaDivergencias = new JTable(tableModel);
        tabelaDivergencias.setRowHeight(30);
        tabelaDivergencias.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Configurar renderizadores
        tabelaDivergencias.getColumnModel().getColumn(2).setCellRenderer(new TipoDivergenciaRenderer());

        // Configurar ordenação
        sorter = new TableRowSorter<>(tableModel);
        tabelaDivergencias.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(tabelaDivergencias);
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
                return relatorioService.gerarRelatorioDivergenciasAutomaticas(idInventarioAtivo);
            }

            @Override
            protected void done() {
                try {
                    dadosAtuais = get();
                    atualizarTabela(dadosAtuais);
                    atualizarResumo(dadosAtuais);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(RelatorioDivergenciasFrame.this,
                            "Erro ao carregar dados: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void atualizarTabela(List<Map<String, Object>> dados) {
        tableModel.setRowCount(0);
        for (Map<String, Object> item : dados) {
            String tipo = (String) item.get("Tipo Divergência");
            String valorCadastrado = "";
            String valorEncontrado = "";

            if ("Localização".equals(tipo)) {
                valorCadastrado = (String) item.get("Localização Cadastrada");
                valorEncontrado = (String) item.get("Localização Encontrada");
            } else if ("Estado".equals(tipo)) {
                valorCadastrado = (String) item.get("Estado Cadastrado");
                valorEncontrado = (String) item.get("Estado Encontrado");
            } else {
                valorCadastrado = "-";
                valorEncontrado = "-";
            }

            Object[] row = {
                    item.get("Número Patrimônio"),
                    item.get("Descrição"),
                    tipo,
                    valorCadastrado,
                    valorEncontrado,
                    item.get("Motivo")
            };
            tableModel.addRow(row);
        }
        aplicarFiltros();
    }

    private void atualizarResumo(List<Map<String, Object>> dados) {
        int total = dados.size();
        long local = dados.stream().filter(m -> "Localização".equals(m.get("Tipo Divergência"))).count();
        long estado = dados.stream().filter(m -> "Estado".equals(m.get("Tipo Divergência"))).count();

        atualizarLabelResumo(labelTotalDivergencias, "Total de Divergências", String.valueOf(total),
                new Color(231, 76, 60));
        atualizarLabelResumo(labelDivergenciasLocal, "Divergências de Local", String.valueOf(local),
                new Color(243, 156, 18));
        atualizarLabelResumo(labelDivergenciasEstado, "Divergências de Estado", String.valueOf(estado),
                new Color(52, 152, 219));
    }

    private void atualizarLabelResumo(JLabel label, String titulo, String valor, Color cor) {
        label.setText("<html><div style='text-align: center;'>" +
                "<span style='font-size: 12px; color: #7f8c8d;'>" + titulo + "</span><br>" +
                "<span style='font-size: 24px; font-weight: bold; color: rgb(" + cor.getRed() + "," + cor.getGreen()
                + "," + cor.getBlue() + ");'>" + valor + "</span>" +
                "</div></html>");
    }

    private void aplicarFiltros() {
        String tipoSelecionado = (String) comboFiltroTipo.getSelectedItem();

        RowFilter<DefaultTableModel, Object> rf = new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                String tipo = (String) entry.getValue(2);
                return "Todos".equals(tipoSelecionado) || tipo.equals(tipoSelecionado);
            }
        };
        sorter.setRowFilter(rf);
    }

    // Renderizadores Personalizados

    private static class TipoDivergenciaRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String tipo = (String) value;
            if ("Localização".equals(tipo)) {
                c.setForeground(new Color(243, 156, 18));
                c.setFont(c.getFont().deriveFont(Font.BOLD));
            } else if ("Estado".equals(tipo)) {
                c.setForeground(new Color(52, 152, 219));
                c.setFont(c.getFont().deriveFont(Font.BOLD));
            } else if ("Manual".equals(tipo)) {
                c.setForeground(new Color(231, 76, 60));
                c.setFont(c.getFont().deriveFont(Font.BOLD));
            } else {
                c.setForeground(Color.BLACK);
                c.setFont(c.getFont().deriveFont(Font.PLAIN));
            }

            return c;
        }
    }
}
