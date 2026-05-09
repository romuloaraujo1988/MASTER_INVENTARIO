package com.inventario.sihcp.view;

import com.inventario.sihcp.model.Inventario;
import com.inventario.sihcp.service.DashboardService;
import com.inventario.sihcp.view.ui.ButtonStyleFactory;

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
import java.util.concurrent.ExecutionException;
import javax.swing.ListSelectionModel;

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
        comboFiltroStatus = new JComboBox<>(new String[] { "Todos", "Finalizada", "Em Andamento", "Não Iniciado", "Concluído", "Vazia", "Pendente" });
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

        String[] colunas = { "Setor", "Sala", "Total Itens", "Coletados", "Progresso", "Status", "ID" };
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2 || columnIndex == 3 || columnIndex == 6)
                    return Integer.class; // Total, Coletados e ID
                return Object.class;
            }
        };

        tabelaStatus = new JTable(tableModel);
        tabelaStatus.setRowHeight(30);
        tabelaStatus.getTableHeader().setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        tabelaStatus.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Configurar renderizadores
        StatusRenderer statusRenderer = new StatusRenderer();
        tabelaStatus.getColumnModel().getColumn(4).setCellRenderer(new ProgressoRenderer());
        tabelaStatus.getColumnModel().getColumn(5).setCellRenderer(statusRenderer);

        // Configurar ordenação
        sorter = new TableRowSorter<>(tableModel);
        tabelaStatus.setRowSorter(sorter);

        // Duplo clique para ver detalhes
        tabelaStatus.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    mostrarDetalhesSala();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tabelaStatus);
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel criarPainelInferior() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(248, 249, 250));

        // Painel esquerdo com ações de sala
        JPanel panelAcoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelAcoes.setOpaque(false);

        JButton btnFinalizarSala = ButtonStyleFactory.createSuccessButton("✅ Finalizar Sala");
        btnFinalizarSala.setToolTipText("Finaliza a coleta da sala selecionada");
        btnFinalizarSala.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        btnFinalizarSala.setPreferredSize(new Dimension(150, 35));
        btnFinalizarSala.addActionListener(e -> finalizarSalaSelecionada());
        panelAcoes.add(btnFinalizarSala);

        panelAcoes.add(Box.createHorizontalStrut(10));

        JButton btnReabrirSala = ButtonStyleFactory.createWarningButton("🔓 Reabrir Sala");
        btnReabrirSala.setToolTipText("Reabre a coleta de uma sala finalizada");
        btnReabrirSala.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        btnReabrirSala.setPreferredSize(new Dimension(150, 35));
        btnReabrirSala.addActionListener(e -> reabrirSalaSelecionada());
        panelAcoes.add(btnReabrirSala);

        panel.add(panelAcoes, BorderLayout.WEST);

        // Painel direito com botões gerais
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotoes.setOpaque(false);

        JButton btnAtualizar = ButtonStyleFactory.createPrimaryButton("🔄 Atualizar Dados");
        btnAtualizar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        btnAtualizar.setPreferredSize(new Dimension(150, 35));
        btnAtualizar.addActionListener(e -> carregarDados());

        JButton btnFechar = ButtonStyleFactory.createDangerButton("Fechar");
        btnFechar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        btnFechar.setPreferredSize(new Dimension(100, 35));
        btnFechar.addActionListener(e -> dispose());

        panelBotoes.add(btnAtualizar);
        panelBotoes.add(Box.createHorizontalStrut(10));
        panelBotoes.add(btnFechar);

        panel.add(panelBotoes, BorderLayout.EAST);

        return panel;
    }

    /**
     * Finaliza a coleta da sala selecionada na tabela
     */
    private void finalizarSalaSelecionada() {
        int selectedRow = tabelaStatus.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Selecione uma sala na tabela para finalizar.", 
                "Nenhuma Sala Selecionada", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Converter índice da view para o modelo (por causa do sorter)
        int modelRow = tabelaStatus.convertRowIndexToModel(selectedRow);
        
        String nomeSala = (String) tableModel.getValueAt(modelRow, 1);
        String status = (String) tableModel.getValueAt(modelRow, 5);
        String progresso = (String) tableModel.getValueAt(modelRow, 4);

        // Verificar se já está finalizada
        if ("Finalizada".equals(status)) {
            JOptionPane.showMessageDialog(this, 
                "Esta sala já está finalizada.", 
                "Sala Já Finalizada", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Confirmar finalização
        int confirm = JOptionPane.showConfirmDialog(this,
            "<html><b>Finalizar coleta da sala:</b> " + nomeSala + "<br><br>" +
            "Progresso atual: " + progresso + "<br>" +
            "Status atual: " + status + "<br><br>" +
            "Deseja realmente finalizar a coleta desta sala?</html>",
            "Confirmar Finalização",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Solicitar observações
        String observacoes = JOptionPane.showInputDialog(this,
            "Observações da finalização (opcional):",
            "Observações",
            JOptionPane.PLAIN_MESSAGE);

        // Obter ID da sala da coluna oculta
        Integer idSala = (Integer) tableModel.getValueAt(modelRow, 6);
        if (idSala == null || idSala == -1) {
            // Fallback: buscar pelo nome
            idSala = dashboardService.buscarIdSalaPorNome(nomeSala);
            if (idSala == -1) {
                JOptionPane.showMessageDialog(this, 
                    "Não foi possível identificar a sala. Tente novamente.", 
                    "Erro", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        boolean sucesso = dashboardService.finalizarColetaSala(idSala, idInventarioAtivo, observacoes);
        
        if (sucesso) {
            JOptionPane.showMessageDialog(this, 
                "Sala finalizada com sucesso!", 
                "Sucesso", 
                JOptionPane.INFORMATION_MESSAGE);
            carregarDados(); // Recarregar dados
        } else {
            JOptionPane.showMessageDialog(this, 
                "Erro ao finalizar a sala. Tente novamente.", 
                "Erro", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Reabre a coleta da sala selecionada na tabela
     */
    private void reabrirSalaSelecionada() {
        int selectedRow = tabelaStatus.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Selecione uma sala na tabela para reabrir.", 
                "Nenhuma Sala Selecionada", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Converter índice da view para o modelo (por causa do sorter)
        int modelRow = tabelaStatus.convertRowIndexToModel(selectedRow);
        
        String nomeSala = (String) tableModel.getValueAt(modelRow, 1);
        String status = (String) tableModel.getValueAt(modelRow, 5);

        // Verificar se está finalizada
        if (!"Finalizada".equals(status)) {
            JOptionPane.showMessageDialog(this, 
                "Apenas salas finalizadas podem ser reabertas.\nStatus atual: " + status, 
                "Sala Não Finalizada", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Solicitar motivo da reabertura
        String motivo = JOptionPane.showInputDialog(this,
            "<html>Informe o motivo da reabertura da sala:<br><b>" + nomeSala + "</b></html>",
            "Motivo da Reabertura",
            JOptionPane.QUESTION_MESSAGE);

        if (motivo == null || motivo.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "É necessário informar o motivo da reabertura.", 
                "Motivo Obrigatório", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Confirmar reabertura
        int confirm = JOptionPane.showConfirmDialog(this,
            "<html><b>Reabrir coleta da sala:</b> " + nomeSala + "<br><br>" +
            "Motivo: " + motivo + "<br><br>" +
            "Deseja realmente reabrir a coleta desta sala?</html>",
            "Confirmar Reabertura",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Obter ID da sala da coluna oculta
        Integer idSala = (Integer) tableModel.getValueAt(modelRow, 6);
        if (idSala == null || idSala == -1) {
            // Fallback: buscar pelo nome
            idSala = dashboardService.buscarIdSalaPorNome(nomeSala);
            if (idSala == -1) {
                JOptionPane.showMessageDialog(this, 
                    "Não foi possível identificar a sala. Tente novamente.", 
                    "Erro", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        boolean sucesso = dashboardService.reabrirColetaSala(idSala, idInventarioAtivo, motivo);
        
        if (sucesso) {
            JOptionPane.showMessageDialog(this, 
                "Sala reaberta com sucesso!", 
                "Sucesso", 
                JOptionPane.INFORMATION_MESSAGE);
            carregarDados(); // Recarregar dados
        } else {
            JOptionPane.showMessageDialog(this, 
                "Erro ao reabrir a sala. Tente novamente.", 
                "Erro", 
                JOptionPane.ERROR_MESSAGE);
        }
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
                } catch (InterruptedException | ExecutionException e) {
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
                    sala.get("status"),
                    sala.get("id_sala") // Coluna oculta com ID
            };
            tableModel.addRow(row);
        }
        // Ocultar coluna de ID
        tabelaStatus.getColumnModel().getColumn(6).setMinWidth(0);
        tabelaStatus.getColumnModel().getColumn(6).setMaxWidth(0);
        tabelaStatus.getColumnModel().getColumn(6).setWidth(0);
        
        aplicarFiltros(); // Reaplicar filtros se houver
    }

    private void atualizarResumo(List<Map<String, Object>> dados) {
        int totalSalas = dados.size();
        
        // Contar salas finalizadas (status oficial) e concluídas (100% coletado)
        long salasFinalizadas = dados.stream()
                .filter(m -> "Finalizada".equals(m.get("status")))
                .count();
        long salasConcluidas = dados.stream()
                .filter(m -> "Concluído".equals(m.get("status")) || "Finalizada".equals(m.get("status")))
                .count();

        int totalItens = dados.stream().mapToInt(m -> (Integer) m.get("total_patrimonios")).sum();
        int totalColetados = dados.stream().mapToInt(m -> (Integer) m.get("itens_coletados")).sum();
        double progressoGeral = totalItens > 0 ? (double) totalColetados / totalItens * 100.0 : 0.0;

        labelTotalSalas.setText("<html><div style='text-align: center;'>" +
                "<span style='font-size: 12px; color: #7f8c8d;'>Total de Salas</span><br>" +
                "<span style='font-size: 24px; font-weight: bold; color: #2c3e50;'>" + totalSalas + "</span>" +
                "</div></html>");

        // Mostrar finalizadas/concluídas
        String textoSalas = salasFinalizadas > 0 
            ? salasFinalizadas + " ✅ / " + salasConcluidas + " total"
            : String.valueOf(salasConcluidas);
        labelSalasConcluidas.setText("<html><div style='text-align: center;'>" +
                "<span style='font-size: 12px; color: #7f8c8d;'>Salas Finalizadas/Concluídas</span><br>" +
                "<span style='font-size: 24px; font-weight: bold; color: #27ae60;'>" + textoSalas + "</span>" +
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
            switch (status != null ? status : "") {
                case "Finalizada" -> {
                    // Verde escuro com ícone de check - sala oficialmente fechada
                    c.setForeground(new Color(0, 128, 0));
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                    setText("✅ " + status);
                }
                case "Concluído" -> {
                    // Verde claro - 100% coletado mas não finalizada oficialmente
                    c.setForeground(new Color(39, 174, 96));
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                    setText("✓ " + status);
                }
                case "Em Andamento" -> {
                    // Amarelo/Laranja - coleta em progresso
                    c.setForeground(new Color(243, 156, 18));
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                    setText("🔄 " + status);
                }
                case "Pendente" -> {
                    // Azul - aguardando início
                    c.setForeground(new Color(52, 152, 219));
                    c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    setText("⏳ " + status);
                }
                case "Não Iniciado" -> {
                    // Cinza - sem coletas
                    c.setForeground(new Color(127, 140, 141));
                    c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    setText("○ " + status);
                }
                case "Vazia" -> {
                    // Cinza claro - sala sem patrimônios
                    c.setForeground(new Color(189, 195, 199));
                    c.setFont(c.getFont().deriveFont(Font.ITALIC));
                    setText("∅ " + status);
                }
                default -> {
                    c.setForeground(new Color(127, 140, 141));
                    c.setFont(c.getFont().deriveFont(Font.PLAIN));
                }
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

    /**
     * Mostra detalhes da sala selecionada
     */
    private void mostrarDetalhesSala() {
        int selectedRow = tabelaStatus.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        int modelRow = tabelaStatus.convertRowIndexToModel(selectedRow);
        
        String setor = (String) tableModel.getValueAt(modelRow, 0);
        String nomeSala = (String) tableModel.getValueAt(modelRow, 1);
        Integer totalItens = (Integer) tableModel.getValueAt(modelRow, 2);
        Integer coletados = (Integer) tableModel.getValueAt(modelRow, 3);
        String progresso = (String) tableModel.getValueAt(modelRow, 4);
        String status = (String) tableModel.getValueAt(modelRow, 5);

        // Buscar dados adicionais da sala nos dadosAtuais
        Map<String, Object> dadosSala = null;
        if (dadosAtuais != null) {
            for (Map<String, Object> sala : dadosAtuais) {
                if (nomeSala.equals(sala.get("sala"))) {
                    dadosSala = sala;
                    break;
                }
            }
        }

        StringBuilder detalhes = new StringBuilder();
        detalhes.append("<html><body style='width: 300px; font-family: Segoe UI;'>");
        detalhes.append("<h2 style='color: #2c3e50;'>").append(nomeSala).append("</h2>");
        detalhes.append("<hr>");
        detalhes.append("<table style='width: 100%;'>");
        detalhes.append("<tr><td><b>Setor:</b></td><td>").append(setor).append("</td></tr>");
        detalhes.append("<tr><td><b>Total de Itens:</b></td><td>").append(totalItens).append("</td></tr>");
        detalhes.append("<tr><td><b>Itens Coletados:</b></td><td>").append(coletados).append("</td></tr>");
        detalhes.append("<tr><td><b>Progresso:</b></td><td>").append(progresso).append("</td></tr>");
        detalhes.append("<tr><td><b>Status:</b></td><td>").append(status).append("</td></tr>");
        
        if (dadosSala != null) {
            Boolean finalizada = (Boolean) dadosSala.get("coleta_finalizada");
            java.sql.Timestamp dataFinalizacao = (java.sql.Timestamp) dadosSala.get("data_finalizacao");
            
            if (finalizada != null && finalizada && dataFinalizacao != null) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
                detalhes.append("<tr><td><b>Finalizada em:</b></td><td>").append(sdf.format(dataFinalizacao)).append("</td></tr>");
            }
        }
        
        detalhes.append("</table>");
        detalhes.append("<hr>");
        
        // Ações disponíveis
        detalhes.append("<p style='color: #7f8c8d; font-size: 11px;'>");
        if ("Finalizada".equals(status)) {
            detalhes.append("✅ Esta sala está finalizada. Use o botão 'Reabrir Sala' para permitir novas coletas.");
        } else if ("Concluído".equals(status)) {
            detalhes.append("✓ Todos os itens foram coletados. Use o botão 'Finalizar Sala' para marcar como concluída oficialmente.");
        } else {
            detalhes.append("🔄 Coleta em andamento. Aguarde a conclusão ou finalize manualmente.");
        }
        detalhes.append("</p>");
        
        detalhes.append("</body></html>");

        JOptionPane.showMessageDialog(this, 
            detalhes.toString(), 
            "Detalhes da Sala", 
            JOptionPane.INFORMATION_MESSAGE);
    }
}
