package com.inventario.sihcp.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inventario.sihcp.model.Inventario;
import com.inventario.sihcp.service.ColetaService;

/**
 * Dialog de busca de situação de itens em um inventário específico.
 *
 * Painel superior: mostra a situação de cada patrimônio no inventário
 * selecionado.
 * - COLETADO → verde (coleta registrada)
 * - PENDENTE → amarelo (sem coleta, inventário EM_ANDAMENTO)
 * - NAO_ENCONTRADO → vermelho (sem coleta, inventário CONCLUIDO/CANCELADO
 * ou coleta com STATUS = NAO_ENCONTRADO)
 *
 * Painel inferior: exibe o histórico do patrimônio selecionado em TODOS os
 * inventários do sistema, permitindo rastrear sua trajetória ao longo do tempo.
 */
public class BuscaItemInventarioDialog extends JDialog {

    private static final Logger logger = LoggerFactory.getLogger(BuscaItemInventarioDialog.class);

    // ── Tema ──────────────────────────────────────────────────────────────────
    private static final Color BG = new Color(240, 242, 245);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color PRIMARY = new Color(59, 130, 246);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color DANGER = new Color(239, 68, 68);
    private static final Color WARNING = new Color(245, 158, 11);
    private static final Color INDIGO = new Color(99, 102, 241);
    private static final Color TEXT_PRI = new Color(17, 24, 39);
    private static final Color TEXT_SEC = new Color(107, 114, 128);
    private static final Color BORDER = new Color(229, 231, 235);
    private static final Color ROW_STRIP = new Color(249, 250, 251);

    private static final Color COR_COLETADO = new Color(220, 252, 231);
    private static final Color COR_COLETADO_TEXT = new Color(22, 101, 52);
    private static final Color COR_PENDENTE = new Color(254, 243, 199);
    private static final Color COR_PENDENTE_TEXT = new Color(120, 53, 15);
    private static final Color COR_NAO_ENC = new Color(254, 226, 226);
    private static final Color COR_NAO_ENC_TEXT = new Color(153, 27, 27);

    // ── Dados ─────────────────────────────────────────────────────────────────
    private final Inventario inventario;
    private final ColetaService coletaService;
    private List<Map<String, Object>> dadosAtuais = List.of();
    private String filtroAtivo = "TODOS";

    // ── Painel superior ───────────────────────────────────────────────────────
    private JTextField campoBusca;
    private JRadioButton rbTodos, rbPendentes, rbColetados, rbNaoEnc;
    private JLabel lblColetados, lblPendentes, lblNaoEnc, lblTotal;
    private DefaultTableModel modeloSuperior;
    private JTable tabelaSuperior;

    // ── Painel inferior ───────────────────────────────────────────────────────
    private JLabel lblTituloHistorico;
    private DefaultTableModel modeloHistorico;
    private JTable tabelaHistorico;

    // ── Debounce ──────────────────────────────────────────────────────────────
    private final ScheduledExecutorService debounceExec = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> debounceTask;

    private static final SimpleDateFormat FMT_DATA = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    // ═════════════════════════════════════════════════════════════════════════
    public BuscaItemInventarioDialog(JFrame parent, Inventario inventario) {
        super(parent, "Buscar Item — " + inventario.getNome(), true);
        this.inventario = inventario;
        this.coletaService = new ColetaService();

        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        add(criarHeader(), BorderLayout.NORTH);
        add(criarConteudo(), BorderLayout.CENTER);
        add(criarRodape(), BorderLayout.SOUTH);

        setSize(1050, 700);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                debounceExec.shutdownNow();
            }
        });

        // Carrega todos os itens ao abrir
        executarBusca("");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Header
    // ═════════════════════════════════════════════════════════════════════════

    private JPanel criarHeader() {
        JPanel p = new JPanel(new BorderLayout(16, 0));
        p.setBackground(PRIMARY);
        p.setBorder(new EmptyBorder(18, 24, 18, 24));

        JLabel titulo = new JLabel("🔍  Busca de Itens no Inventário");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(Color.WHITE);
        p.add(titulo, BorderLayout.WEST);

        JLabel sub = new JLabel(inventario.getNome() +
                (inventario.getAno() != null ? "  •  " + inventario.getAno() : "") +
                "  •  " + traduzirStatus(inventario.getStatusInventario()));
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(191, 219, 254));
        p.add(sub, BorderLayout.EAST);

        return p;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Conteúdo principal (JSplitPane)
    // ═════════════════════════════════════════════════════════════════════════

    private JSplitPane criarConteudo() {
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                criarPainelSuperior(), criarPainelHistorico());
        split.setBackground(BG);
        split.setDividerSize(6);
        split.setResizeWeight(0.58);
        split.setBorder(BorderFactory.createEmptyBorder(12, 16, 4, 16));
        return split;
    }

    // ── Painel superior ───────────────────────────────────────────────────────

    private JPanel criarPainelSuperior() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(12, 16, 12, 16)));

        p.add(criarBarraBusca(), BorderLayout.NORTH);
        p.add(criarCardResumo(), BorderLayout.CENTER); // inserido antes aqui para medir

        JScrollPane scroll = new JScrollPane(criarTabelaSuperior());
        scroll.setBorder(BorderFactory.createEmptyBorder());
        p.add(scroll, BorderLayout.SOUTH);

        // Reorganizar: barra → resumo → tabela
        p.removeAll();
        p.setLayout(new BorderLayout(0, 8));
        p.add(criarBarraBuscaInternal(), BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 6));
        centro.setOpaque(false);
        centro.add(criarCardResumoInternal(), BorderLayout.NORTH);
        JScrollPane sc = new JScrollPane(tabelaSuperior);
        sc.setBorder(BorderFactory.createEmptyBorder());
        centro.add(sc, BorderLayout.CENTER);
        p.add(centro, BorderLayout.CENTER);

        return p;
    }

    private JPanel criarBarraBuscaInternal() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setOpaque(false);

        // Campo busca
        JPanel searchWrap = new JPanel(new BorderLayout());
        searchWrap.setBackground(Color.WHITE);
        searchWrap.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));

        JLabel icone = new JLabel("  🔍 ");
        icone.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        icone.setForeground(TEXT_SEC);
        searchWrap.add(icone, BorderLayout.WEST);

        campoBusca = new JTextField();
        campoBusca.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        campoBusca.setBorder(new EmptyBorder(8, 4, 8, 8));
        campoBusca.setToolTipText("Buscar por número ou descrição do patrimônio");
        campoBusca.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    executarBusca(campoBusca.getText());
                } else {
                    agendarBusca(campoBusca.getText());
                }
            }
        });
        searchWrap.add(campoBusca, BorderLayout.CENTER);
        bar.add(searchWrap, BorderLayout.CENTER);

        // Botão limpar
        JButton btnLimpar = criarBotao("✖ Limpar", BORDER, TEXT_SEC);
        btnLimpar.addActionListener(e -> {
            campoBusca.setText("");
            executarBusca("");
        });
        bar.add(btnLimpar, BorderLayout.EAST);

        // Filtros de situação
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filtros.setOpaque(false);

        ButtonGroup grupo = new ButtonGroup();
        rbTodos = criarRadio("Todos", grupo, filtros, true);
        rbColetados = criarRadio("✅ Coletados", grupo, filtros, false);
        rbPendentes = criarRadio("⏳ Pendentes", grupo, filtros, false);
        rbNaoEnc = criarRadio("❌ Não Encontrados", grupo, filtros, false);

        JPanel topo = new JPanel(new BorderLayout(0, 4));
        topo.setOpaque(false);
        topo.add(bar, BorderLayout.NORTH);
        topo.add(filtros, BorderLayout.SOUTH);
        return topo;
    }

    private JPanel criarCardResumoInternal() {
        JPanel card = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 4));
        card.setBackground(new Color(248, 250, 252));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(6, 14, 6, 14)));

        JLabel icone = new JLabel("📊 ");
        icone.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        card.add(icone);

        lblColetados = criarLabelContador("✅ 0 coletados", COR_COLETADO_TEXT);
        lblPendentes = criarLabelContador("⏳ 0 pendentes", COR_PENDENTE_TEXT);
        lblNaoEnc = criarLabelContador("❌ 0 não encontrados", COR_NAO_ENC_TEXT);
        lblTotal = criarLabelContador("Total: 0", TEXT_SEC);

        card.add(lblColetados);
        card.add(new JLabel("|") {
            {
                setForeground(BORDER);
            }
        });
        card.add(lblPendentes);
        card.add(new JLabel("|") {
            {
                setForeground(BORDER);
            }
        });
        card.add(lblNaoEnc);
        card.add(new JLabel("|") {
            {
                setForeground(BORDER);
            }
        });
        card.add(lblTotal);

        return card;
    }

    private JTable criarTabelaSuperior() {
        String[] cols = { "Nº Patrimônio", "Descrição", "Sala", "Situação", "Data Coleta", "Coletor",
                "Local Encontrado", "Estado" };
        modeloSuperior = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tabelaSuperior = new JTable(modeloSuperior);
        estilizarTabela(tabelaSuperior);

        tabelaSuperior.getColumnModel().getColumn(0).setPreferredWidth(110);
        tabelaSuperior.getColumnModel().getColumn(1).setPreferredWidth(220);
        tabelaSuperior.getColumnModel().getColumn(2).setPreferredWidth(100);
        tabelaSuperior.getColumnModel().getColumn(3).setPreferredWidth(120);
        tabelaSuperior.getColumnModel().getColumn(4).setPreferredWidth(120);
        tabelaSuperior.getColumnModel().getColumn(5).setPreferredWidth(140);
        tabelaSuperior.getColumnModel().getColumn(6).setPreferredWidth(150);
        tabelaSuperior.getColumnModel().getColumn(7).setPreferredWidth(90);

        tabelaSuperior.getColumnModel().getColumn(3).setCellRenderer(new SituacaoCellRenderer());

        // Ao clicar → carregar histórico
        tabelaSuperior.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tabelaSuperior.getSelectedRow();
                if (row >= 0) {
                    String numero = (String) modeloSuperior.getValueAt(row, 0);
                    carregarHistorico(numero);
                }
            }
        });

        return tabelaSuperior;
    }

    // ── Painel inferior ───────────────────────────────────────────────────────

    private JPanel criarPainelHistorico() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(10, 16, 10, 16)));

        lblTituloHistorico = new JLabel("📋  Histórico de rastreabilidade — selecione um item acima");
        lblTituloHistorico.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTituloHistorico.setForeground(TEXT_SEC);
        p.add(lblTituloHistorico, BorderLayout.NORTH);

        String[] cols = { "Inventário", "Ano", "Status do Inventário", "Data Coleta", "Situação", "Coletor",
                "Local Encontrado", "Estado", "Observação" };
        modeloHistorico = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tabelaHistorico = new JTable(modeloHistorico);
        estilizarTabela(tabelaHistorico);

        tabelaHistorico.getColumnModel().getColumn(0).setPreferredWidth(180);
        tabelaHistorico.getColumnModel().getColumn(1).setPreferredWidth(50);
        tabelaHistorico.getColumnModel().getColumn(2).setPreferredWidth(130);
        tabelaHistorico.getColumnModel().getColumn(3).setPreferredWidth(120);
        tabelaHistorico.getColumnModel().getColumn(4).setPreferredWidth(120);
        tabelaHistorico.getColumnModel().getColumn(5).setPreferredWidth(140);
        tabelaHistorico.getColumnModel().getColumn(6).setPreferredWidth(140);
        tabelaHistorico.getColumnModel().getColumn(7).setPreferredWidth(80);
        tabelaHistorico.getColumnModel().getColumn(8).setPreferredWidth(200);

        tabelaHistorico.getColumnModel().getColumn(4).setCellRenderer(new SituacaoCellRenderer());

        JScrollPane scroll = new JScrollPane(tabelaHistorico);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        p.add(scroll, BorderLayout.CENTER);

        return p;
    }

    // ── Rodapé ────────────────────────────────────────────────────────────────

    private JPanel criarRodape() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        p.setBackground(BG);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));

        JButton btnExportar = criarBotao("📄 Exportar CSV", INDIGO, Color.WHITE);
        btnExportar.addActionListener(e -> exportarCSV());

        JButton btnFechar = criarBotao("Fechar", BORDER, TEXT_PRI);
        btnFechar.addActionListener(e -> dispose());

        p.add(btnExportar);
        p.add(btnFechar);
        return p;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Lógica de busca
    // ═════════════════════════════════════════════════════════════════════════

    private void agendarBusca(String texto) {
        if (debounceTask != null && !debounceTask.isDone()) {
            debounceTask.cancel(false);
        }
        debounceTask = debounceExec.schedule(
                () -> SwingUtilities.invokeLater(() -> executarBusca(texto)),
                300, TimeUnit.MILLISECONDS);
    }

    private void executarBusca(String texto) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            dadosAtuais = coletaService.buscarSituacaoItensInventario(inventario.getId(), texto);
            aplicarFiltroEAtualizar();
        } catch (Exception ex) {
            logger.error("Erro na busca de itens", ex);
            JOptionPane.showMessageDialog(this, "Erro ao buscar itens: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void aplicarFiltroEAtualizar() {
        modeloSuperior.setRowCount(0);
        int coletados = 0, pendentes = 0, naoEnc = 0;

        for (Map<String, Object> row : dadosAtuais) {
            String sit = str(row.get("SITUACAO"));

            // Contar sempre
            if ("COLETADO".equals(sit))
                coletados++;
            else if ("PENDENTE".equals(sit))
                pendentes++;
            else
                naoEnc++;

            // Aplicar filtro
            boolean exibir = switch (filtroAtivo) {
                case "COLETADO" -> "COLETADO".equals(sit);
                case "PENDENTE" -> "PENDENTE".equals(sit);
                case "NAO_ENCONTRADO" -> !"COLETADO".equals(sit) && !"PENDENTE".equals(sit);
                default -> true;
            };

            if (exibir) {
                modeloSuperior.addRow(new Object[] {
                        str(row.get("NUMERO")),
                        str(row.get("DESCRICAO")),
                        str(row.get("SALA")),
                        formatarSituacao(sit),
                        formatarData(row.get("DATA_COLETA")),
                        str(row.get("COLETOR")),
                        str(row.get("LOCALIZACAO_ENCONTRADA")),
                        str(row.get("ESTADO_ENCONTRADO"))
                });
            }
        }

        lblColetados.setText("✅ " + coletados + " coletados");
        lblPendentes.setText("⏳ " + pendentes + " pendentes");
        lblNaoEnc.setText("❌ " + naoEnc + " não encontrados");
        lblTotal.setText("Total: " + dadosAtuais.size());

        // Limpar histórico ao buscar novamente
        modeloHistorico.setRowCount(0);
        lblTituloHistorico.setText("📋  Histórico de rastreabilidade — selecione um item acima");
        lblTituloHistorico.setForeground(TEXT_SEC);
    }

    private void carregarHistorico(String numeroPatrimonio) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            List<Map<String, Object>> hist = coletaService.buscarHistoricoPatrimonio(numeroPatrimonio);
            modeloHistorico.setRowCount(0);

            if (hist.isEmpty()) {
                lblTituloHistorico
                        .setText("📋  Histórico: " + numeroPatrimonio + " — nenhum registro anterior encontrado");
                lblTituloHistorico.setForeground(TEXT_SEC);
            } else {
                lblTituloHistorico.setText("📋  Histórico de rastreabilidade: " + numeroPatrimonio
                        + "  (" + hist.size() + " registro" + (hist.size() > 1 ? "s" : "") + ")");
                lblTituloHistorico.setForeground(INDIGO);

                for (Map<String, Object> row : hist) {
                    int ano = 0;
                    Object anoObj = row.get("ANO");
                    if (anoObj instanceof Number n)
                        ano = n.intValue();

                    modeloHistorico.addRow(new Object[] {
                            str(row.get("INVENTARIO")),
                            ano == 0 ? "-" : ano,
                            traduzirStatus(str(row.get("STATUS_INVENTARIO"))),
                            formatarData(row.get("DATA_COLETA")),
                            formatarSituacao(str(row.get("SITUACAO"))),
                            str(row.get("COLETOR")),
                            str(row.get("LOCALIZACAO_ENCONTRADA")),
                            str(row.get("ESTADO_ENCONTRADO")),
                            str(row.get("OBSERVACAO"))
                    });
                }
            }
        } catch (Exception ex) {
            logger.error("Erro ao carregar histórico do patrimônio {}", numeroPatrimonio, ex);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Exportar CSV
    // ═════════════════════════════════════════════════════════════════════════

    private void exportarCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Exportar CSV");
        fc.setSelectedFile(new File("inventario_" + inventario.getId() + "_busca.csv"));
        fc.setFileFilter(new FileNameExtensionFilter("Arquivo CSV", "csv"));

        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        File arquivo = fc.getSelectedFile();
        if (!arquivo.getName().endsWith(".csv")) {
            arquivo = new File(arquivo.getAbsolutePath() + ".csv");
        }

        try (BufferedWriter w = new BufferedWriter(new FileWriter(arquivo))) {
            // Cabeçalho situação atual
            w.write("=== SITUAÇÃO ATUAL — " + inventario.getNome() + " ===");
            w.newLine();
            w.write("\"Nº Patrimônio\";\"Descrição\";\"Sala\";\"Situação\";\"Data Coleta\";\"Coletor\";\"Local Encontrado\";\"Estado\"");
            w.newLine();
            for (int r = 0; r < modeloSuperior.getRowCount(); r++) {
                StringBuilder sb = new StringBuilder();
                for (int c = 0; c < modeloSuperior.getColumnCount(); c++) {
                    if (c > 0)
                        sb.append(";");
                    Object v = modeloSuperior.getValueAt(r, c);
                    sb.append("\"").append(v != null ? v.toString().replace("\"", "\"\"") : "").append("\"");
                }
                w.write(sb.toString());
                w.newLine();
            }

            // Histórico
            if (modeloHistorico.getRowCount() > 0) {
                w.newLine();
                w.write("=== HISTÓRICO DE RASTREABILIDADE ===");
                w.newLine();
                w.write("\"Inventário\";\"Ano\";\"Status Inventário\";\"Data Coleta\";\"Situação\";\"Coletor\";\"Local Encontrado\";\"Estado\";\"Observação\"");
                w.newLine();
                for (int r = 0; r < modeloHistorico.getRowCount(); r++) {
                    StringBuilder sb = new StringBuilder();
                    for (int c = 0; c < modeloHistorico.getColumnCount(); c++) {
                        if (c > 0)
                            sb.append(";");
                        Object v = modeloHistorico.getValueAt(r, c);
                        sb.append("\"").append(v != null ? v.toString().replace("\"", "\"\"") : "").append("\"");
                    }
                    w.write(sb.toString());
                    w.newLine();
                }
            }

            JOptionPane.showMessageDialog(this, "CSV exportado com sucesso:\n" + arquivo.getAbsolutePath(),
                    "Exportação Concluída", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            logger.error("Erro ao exportar CSV", ex);
            JOptionPane.showMessageDialog(this, "Erro ao exportar: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Helpers de criação de componentes
    // ═════════════════════════════════════════════════════════════════════════

    private void estilizarTabela(JTable t) {
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setRowHeight(30);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setSelectionBackground(new Color(59, 130, 246, 40));
        t.setSelectionForeground(TEXT_PRI);

        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.getTableHeader().setBackground(new Color(249, 250, 251));
        t.getTableHeader().setForeground(TEXT_SEC);
        t.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        t.getTableHeader().setReorderingAllowed(false);

        // Renderer zebra para colunas sem renderer customizado
        DefaultTableCellRenderer zebra = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? CARD_BG : ROW_STRIP);
                    setForeground(TEXT_PRI);
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        };
        for (int c = 0; c < t.getColumnCount(); c++) {
            if (t.getColumnModel().getColumn(c).getCellRenderer() == null) {
                t.getColumnModel().getColumn(c).setCellRenderer(zebra);
            }
        }
    }

    private JButton criarBotao(String texto, Color bg, Color fg) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        return btn;
    }

    private JRadioButton criarRadio(String texto, ButtonGroup grupo, JPanel container, boolean selecionado) {
        JRadioButton rb = new JRadioButton(texto, selecionado);
        rb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rb.setForeground(TEXT_PRI);
        rb.setOpaque(false);
        rb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        grupo.add(rb);
        container.add(rb);
        rb.addActionListener(e -> {
            filtroAtivo = switch (texto) {
                case "✅ Coletados" -> "COLETADO";
                case "⏳ Pendentes" -> "PENDENTE";
                case "❌ Não Encontrados" -> "NAO_ENCONTRADO";
                default -> "TODOS";
            };
            aplicarFiltroEAtualizar();
        });
        return rb;
    }

    private JLabel criarLabelContador(String texto, Color cor) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(cor);
        return lbl;
    }

    // Stubs para satisfazer compilação (componentes criados inline no método
    // reorganizado)
    private JPanel criarBarraBusca() {
        return new JPanel();
    }

    private JPanel criarCardResumo() {
        return new JPanel();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Utilitários
    // ═════════════════════════════════════════════════════════════════════════

    private String str(Object v) {
        if (v == null)
            return "-";
        String s = v.toString().trim();
        return s.isEmpty() ? "-" : s;
    }

    private String formatarData(Object v) {
        if (v == null)
            return "-";
        if (v instanceof Timestamp ts)
            return FMT_DATA.format(ts);
        return str(v);
    }

    private String formatarSituacao(String sit) {
        if (sit == null)
            return "-";
        return switch (sit) {
            case "COLETADO" -> "✅ Coletado";
            case "PENDENTE" -> "⏳ Pendente";
            case "NAO_ENCONTRADO" -> "❌ Não Encontrado";
            case "DIVERGENCIA" -> "⚠ Divergência";
            default -> sit;
        };
    }

    private String traduzirStatus(String s) {
        if (s == null)
            return "";
        return switch (s) {
            case "EM_ANDAMENTO" -> "Em Andamento";
            case "CONCLUIDO" -> "Concluído";
            case "CANCELADO" -> "Cancelado";
            case "PLANEJADO" -> "Planejado";
            default -> s;
        };
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Renderer de situação com cores
    // ═════════════════════════════════════════════════════════════════════════

    private class SituacaoCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(new EmptyBorder(0, 6, 0, 6));

            if (!isSelected && value != null) {
                String v = value.toString();
                if (v.contains("Coletado")) {
                    setBackground(COR_COLETADO);
                    setForeground(COR_COLETADO_TEXT);
                } else if (v.contains("Pendente")) {
                    setBackground(COR_PENDENTE);
                    setForeground(COR_PENDENTE_TEXT);
                } else if (v.contains("Não Encontrado")) {
                    setBackground(COR_NAO_ENC);
                    setForeground(COR_NAO_ENC_TEXT);
                } else if (v.contains("Divergência")) {
                    setBackground(new Color(254, 243, 199));
                    setForeground(new Color(120, 53, 15));
                } else {
                    setBackground(Color.WHITE);
                    setForeground(TEXT_PRI);
                }
            }
            return this;
        }
    }
}
