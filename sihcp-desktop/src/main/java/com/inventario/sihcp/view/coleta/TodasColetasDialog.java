package com.inventario.sihcp.view.coleta;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inventario.sihcp.dao.ColetaDAO;
import com.inventario.sihcp.dao.InventarioDAO;
import com.inventario.sihcp.model.Coleta;
import com.inventario.sihcp.model.Inventario;
import com.inventario.sihcp.view.ui.StyledButtonFactory;

/**
 * Diálogo modal que lista todas as coletas do inventário ativo (todas as salas),
 * com filtro por sala/texto e exportação CSV.
 *
 * <p>Extraído de {@code ColetaFrame_v2.mostrarTodasColetas} como parte da
 * refatoração estrutural (Tarefa 4a). É totalmente autocontido: recebe os DAOs,
 * o limite de registros e o callback de registro de worker no construtor. O
 * comportamento foi preservado integralmente.</p>
 *
 * <p>Uso: {@code new TodasColetasDialog(frame, ...).setVisible(true);}</p>
 */
public class TodasColetasDialog extends JDialog {

    private static final Logger LOG = LoggerFactory.getLogger(TodasColetasDialog.class);

    private final transient InventarioDAO inventarioDAO;
    private final transient ColetaDAO coletaDAO;
    private final int limiteTodasColetas;
    private final transient Consumer<SwingWorker<?, ?>> workerRegistrar;

    public TodasColetasDialog(Frame owner,
                              InventarioDAO inventarioDAO,
                              ColetaDAO coletaDAO,
                              int limiteTodasColetas,
                              Consumer<SwingWorker<?, ?>> workerRegistrar) {
        super(owner, "📋 Todas as Coletas do Inventário", true);
        this.inventarioDAO = inventarioDAO;
        this.coletaDAO = coletaDAO;
        this.limiteTodasColetas = limiteTodasColetas;
        this.workerRegistrar = workerRegistrar;
        construirEcarregar(owner);
    }

    private void construirEcarregar(Frame owner) {
        setLayout(new BorderLayout(10, 10));
        setSize(1000, 600);
        setLocationRelativeTo(owner);

        // Painel principal
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panelPrincipal.setBackground(Color.WHITE);

        // Painel superior com informações e filtros
        JPanel panelSuperior = new JPanel(new BorderLayout(10, 5));
        panelSuperior.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("📋 Todas as Coletas do Inventário Atual");
        lblTitulo.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(52, 73, 94));

        JLabel lblStatus = new JLabel("Carregando...");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(new Color(100, 100, 100));

        // Painel de filtro por sala
        JPanel panelFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelFiltro.setBackground(Color.WHITE);

        JLabel lblFiltro = new JLabel("Filtrar por sala:");
        lblFiltro.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JComboBox<String> comboFiltroSala = new JComboBox<>();
        comboFiltroSala.addItem("Todas as salas");
        comboFiltroSala.setPreferredSize(new Dimension(250, 28));

        JTextField campoFiltroTexto = new JTextField(20);
        campoFiltroTexto.setToolTipText("Filtrar por número ou descrição");

        JButton btnFiltrar = StyledButtonFactory.create("🔍 Filtrar", new Color(52, 152, 219));
        btnFiltrar.setPreferredSize(new Dimension(100, 28));

        panelFiltro.add(lblFiltro);
        panelFiltro.add(comboFiltroSala);
        panelFiltro.add(new JLabel("  Buscar:"));
        panelFiltro.add(campoFiltroTexto);
        panelFiltro.add(btnFiltrar);

        JPanel panelTituloStatus = new JPanel(new BorderLayout());
        panelTituloStatus.setBackground(Color.WHITE);
        panelTituloStatus.add(lblTitulo, BorderLayout.NORTH);
        panelTituloStatus.add(lblStatus, BorderLayout.SOUTH);

        panelSuperior.add(panelTituloStatus, BorderLayout.NORTH);
        panelSuperior.add(panelFiltro, BorderLayout.SOUTH);

        // Tabela de todas as coletas (com coluna Sala adicional)
        String[] colunas = {"Data/Hora", "Sala", "Patrimônio", "Descrição", "Estado", "Coletor"};
        DefaultTableModel modeloTodasColetas = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tabelaTodasColetas = new JTable(modeloTodasColetas);
        tabelaTodasColetas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaTodasColetas.setRowHeight(28);
        tabelaTodasColetas.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabelaTodasColetas.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabelaTodasColetas.getTableHeader().setBackground(new Color(52, 73, 94));
        tabelaTodasColetas.getTableHeader().setForeground(Color.WHITE);
        tabelaTodasColetas.setGridColor(new Color(220, 220, 220));
        tabelaTodasColetas.setBackground(Color.WHITE);

        // Configurar larguras das colunas
        tabelaTodasColetas.getColumnModel().getColumn(0).setPreferredWidth(130); // Data/Hora
        tabelaTodasColetas.getColumnModel().getColumn(1).setPreferredWidth(120); // Sala
        tabelaTodasColetas.getColumnModel().getColumn(2).setPreferredWidth(100); // Patrimônio
        tabelaTodasColetas.getColumnModel().getColumn(3).setPreferredWidth(300); // Descrição
        tabelaTodasColetas.getColumnModel().getColumn(4).setPreferredWidth(100); // Estado
        tabelaTodasColetas.getColumnModel().getColumn(5).setPreferredWidth(120); // Coletor

        // Renderer para cores alternadas com texto preto
        tabelaTodasColetas.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    c.setBackground(new Color(52, 152, 219));
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 248));
                    c.setForeground(new Color(44, 62, 80));
                }
                return c;
            }
        });

        JScrollPane scrollTodasColetas = new JScrollPane(tabelaTodasColetas);
        scrollTodasColetas.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        // Painel inferior com botões
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelInferior.setBackground(Color.WHITE);

        JButton btnExportar = StyledButtonFactory.create("📥 Exportar CSV", new Color(46, 204, 113));
        btnExportar.addActionListener(e -> exportarTodasColetas(modeloTodasColetas));

        JButton btnFechar = StyledButtonFactory.create("❌ Fechar", new Color(108, 117, 125));
        btnFechar.addActionListener(e -> dispose());

        panelInferior.add(btnExportar);
        panelInferior.add(btnFechar);

        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);
        panelPrincipal.add(scrollTodasColetas, BorderLayout.CENTER);
        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);

        add(panelPrincipal);

        // Lista para armazenar todas as coletas (para filtro)
        final List<Object[]> todasAsColetas = new ArrayList<>();

        // Carregar dados em background
        SwingWorker<Void, Object[]> worker = new SwingWorker<>() {
            private int totalColetas = 0;
            private final java.util.Set<String> salasEncontradas = new java.util.HashSet<>();

            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // Buscar inventário ativo
                    Inventario inventarioAtivo = inventarioDAO.buscarPorStatus("EM_ANDAMENTO");
                    if (inventarioAtivo == null) {
                        SwingUtilities.invokeLater(() ->
                            lblStatus.setText("❌ Nenhum inventário ativo encontrado"));
                        return null;
                    }

                    SwingUtilities.invokeLater(() ->
                        lblStatus.setText("⏳ Carregando coletas do inventário: " + inventarioAtivo.getNome()));

                    // Buscar todas as coletas do inventário
                    List<Coleta> coletas = coletaDAO.buscarColetasPorInventario(inventarioAtivo.getId(), limiteTodasColetas);
                    totalColetas = coletas.size();

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                    for (Coleta coleta : coletas) {
                        String data = coleta.getDataColeta() != null ? sdf.format(coleta.getDataColeta()) : "-";
                        String sala = coleta.getLocalizacaoEncontrada() != null ? coleta.getLocalizacaoEncontrada() : "-";
                        String numero = coleta.getNumeroPatrimonio() != null ? coleta.getNumeroPatrimonio() :
                                        (coleta.isSemEtiqueta() ? "SEM ETIQUETA" : "-");
                        String descricao = coleta.getDescricaoPatrimonio() != null ? coleta.getDescricaoPatrimonio() :
                                          (coleta.getDescricaoItemSemEtiqueta() != null ? coleta.getDescricaoItemSemEtiqueta() : "-");
                        String estado = coleta.getEstadoEncontrado() != null ? coleta.getEstadoEncontrado() : "-";
                        String coletor = coleta.getNomeColetor() != null ? coleta.getNomeColetor() : "-";

                        salasEncontradas.add(sala);

                        Object[] linha = {data, sala, numero, descricao, estado, coletor};
                        publish(linha);
                    }

                } catch (SQLException e) {
                    LOG.error("Erro ao carregar todas as coletas: {}", e.getMessage(), e);
                    SwingUtilities.invokeLater(() ->
                        lblStatus.setText("❌ Erro ao carregar: " + e.getMessage()));
                }
                return null;
            }

            @Override
            protected void process(List<Object[]> chunks) {
                for (Object[] linha : chunks) {
                    modeloTodasColetas.addRow(linha);
                    todasAsColetas.add(linha);
                }
                lblStatus.setText("⏳ Carregando... " + modeloTodasColetas.getRowCount() + " coletas");
            }

            @Override
            protected void done() {
                lblStatus.setText(String.format("✅ %d coletas carregadas (limite: %d)",
                    totalColetas, limiteTodasColetas));

                // Preencher combo de salas
                for (String sala : salasEncontradas) {
                    comboFiltroSala.addItem(sala);
                }
            }
        };

        // Ação do filtro
        ActionListener filtrarAction = e -> {
            String salaFiltro = (String) comboFiltroSala.getSelectedItem();
            String textoFiltro = campoFiltroTexto.getText().toLowerCase().trim();

            modeloTodasColetas.setRowCount(0);

            for (Object[] linha : todasAsColetas) {
                String sala = (String) linha[1];
                String numero = (String) linha[2];
                String descricao = (String) linha[3];

                boolean passaSala = "Todas as salas".equals(salaFiltro) || sala.equals(salaFiltro);
                boolean passaTexto = textoFiltro.isEmpty() ||
                                    numero.toLowerCase().contains(textoFiltro) ||
                                    descricao.toLowerCase().contains(textoFiltro);

                if (passaSala && passaTexto) {
                    modeloTodasColetas.addRow(linha);
                }
            }

            lblStatus.setText(String.format("🔍 Exibindo %d de %d coletas",
                modeloTodasColetas.getRowCount(), todasAsColetas.size()));
        };

        btnFiltrar.addActionListener(filtrarAction);
        comboFiltroSala.addActionListener(filtrarAction);
        campoFiltroTexto.addActionListener(filtrarAction);

        if (workerRegistrar != null) {
            workerRegistrar.accept(worker);
        }
        worker.execute();
    }

    /**
     * Exporta todas as coletas exibidas para um arquivo CSV.
     */
    private void exportarTodasColetas(DefaultTableModel modelo) {
        if (modelo.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "Não há coletas para exportar.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar Todas as Coletas");
        String nomeArquivo = "todas_coletas_" + new SimpleDateFormat("yyyyMMdd_HHmm").format(new java.util.Date()) + ".csv";
        fileChooser.setSelectedFile(new java.io.File(nomeArquivo));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(fileChooser.getSelectedFile(), "UTF-8")) {
                // Cabecalho
                writer.println("Data/Hora;Sala;Patrimonio;Descricao;Estado;Coletor");

                // Dados
                for (int i = 0; i < modelo.getRowCount(); i++) {
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < modelo.getColumnCount(); j++) {
                        if (j > 0) sb.append(";");
                        Object valor = modelo.getValueAt(i, j);
                        sb.append(valor != null ? valor.toString().replace(";", ",") : "");
                    }
                    writer.println(sb);
                }

                JOptionPane.showMessageDialog(this,
                    "✅ Exportado com sucesso!\n" + fileChooser.getSelectedFile().getAbsolutePath(),
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "❌ Erro ao exportar: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
