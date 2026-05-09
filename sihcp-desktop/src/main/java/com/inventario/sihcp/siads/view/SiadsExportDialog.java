package com.inventario.sihcp.siads.view;

import com.inventario.sihcp.siads.service.SiadsIntegrationService;
import com.inventario.sihcp.view.ui.ButtonStyleFactory;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Dialog moderno para exportacao de arquivos SIADS
 */
public class SiadsExportDialog extends JDialog {
    
    private static final Color BACKGROUND_COLOR = new Color(240, 242, 245);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color PRIMARY_COLOR = new Color(59, 130, 246);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color DANGER_COLOR = new Color(239, 68, 68);
    private static final Color WARNING_COLOR = new Color(245, 158, 11);
    private static final Color TEXT_PRIMARY = new Color(17, 24, 39);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    private static final Color HEADER_GRADIENT_START = new Color(59, 130, 246);
    private static final Color HEADER_GRADIENT_END = new Color(37, 99, 235);
    
    private final SiadsIntegrationService siadsService;
    
    private JLabel lblTotalPatrimonios;
    private JLabel lblComResponsavel;
    private JLabel lblComLocalizacao;
    private JTextField txtDiretorio;
    private JButton btnSelecionarDiretorio;
    private JButton btnValidar;
    private JButton btnExportar;
    private JProgressBar progressBar;
    private JLabel lblStatus;
    
    public SiadsExportDialog(Frame parent) {
        super(parent, "Exportacao SIADS", true);
        this.siadsService = new SiadsIntegrationService();
        initComponents();
        carregarEstatisticas();
        try {
            setIconImages(com.inventario.sihcp.util.IconManager.getAppIconImages());
        } catch (Exception e) { }
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setSize(650, 520);
        setLocationRelativeTo(getParent());
        getContentPane().setBackground(BACKGROUND_COLOR);
        add(criarHeader(), BorderLayout.NORTH);
        add(criarConteudoPrincipal(), BorderLayout.CENTER);
        add(criarPainelBotoes(), BorderLayout.SOUTH);
    }
    
    private JPanel criarHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gradient = new GradientPaint(0, 0, HEADER_GRADIENT_START, getWidth(), 0, HEADER_GRADIENT_END);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        header.setPreferredSize(new Dimension(0, 70));
        header.setBorder(new EmptyBorder(15, 25, 15, 25));
        
        JLabel lblTitulo = new JLabel("Exportacao SIADS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);
        
        JLabel lblSubtitulo = new JLabel("Sistema Integrado de Administracao de Servicos");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitulo.setForeground(new Color(255, 255, 255, 200));
        
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.add(lblTitulo);
        titlePanel.add(Box.createVerticalStrut(3));
        titlePanel.add(lblSubtitulo);
        header.add(titlePanel, BorderLayout.WEST);
        return header;
    }
    
    private JPanel criarConteudoPrincipal() {
        JPanel content = new JPanel(new BorderLayout(15, 15));
        content.setBackground(BACKGROUND_COLOR);
        content.setBorder(new EmptyBorder(20, 20, 10, 20));
        content.add(criarPainelEstatisticas(), BorderLayout.NORTH);
        content.add(criarPainelConfiguracao(), BorderLayout.CENTER);
        return content;
    }
    
    private JPanel criarPainelEstatisticas() {
        JPanel painel = new JPanel(new GridLayout(1, 3, 15, 0));
        painel.setBackground(BACKGROUND_COLOR);
        painel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        lblTotalPatrimonios = criarCardEstatistica("Total de Patrimonios", "0", PRIMARY_COLOR);
        painel.add(criarCardContainer(lblTotalPatrimonios));
        
        lblComResponsavel = criarCardEstatistica("Com Responsavel", "0", SUCCESS_COLOR);
        painel.add(criarCardContainer(lblComResponsavel));
        
        lblComLocalizacao = criarCardEstatistica("Com Localizacao", "0", WARNING_COLOR);
        painel.add(criarCardContainer(lblComLocalizacao));
        
        return painel;
    }
    
    private JLabel criarCardEstatistica(String titulo, String valor, Color cor) {
        JLabel card = new JLabel();
        atualizarCardEstatistica(card, titulo, valor, cor);
        card.setHorizontalAlignment(SwingConstants.CENTER);
        return card;
    }
    
    private void atualizarCardEstatistica(JLabel card, String titulo, String valor, Color cor) {
        card.setText(String.format(
            "<html><div style='text-align: center; padding: 15px; font-family: Segoe UI, sans-serif;'>" +
            "<div style='color: rgb(%d,%d,%d); font-size: 11px; font-weight: bold; margin-bottom: 8px;'>%s</div>" +
            "<div style='font-size: 24px; font-weight: bold; color: rgb(17,24,39);'>%s</div></div></html>",
            cor.getRed(), cor.getGreen(), cor.getBlue(), titulo.toUpperCase(), valor));
    }
    
    private JPanel criarCardContainer(JLabel card) {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(CARD_COLOR);
        container.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 2, new Color(0, 0, 0, 15)),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(5, 5, 5, 5))));
        container.add(card, BorderLayout.CENTER);
        return container;
    }

    private JPanel criarPainelConfiguracao() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBackground(CARD_COLOR);
        painel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 2, new Color(0, 0, 0, 15)),
                BorderFactory.createLineBorder(BORDER_COLOR, 1)),
            new EmptyBorder(20, 20, 20, 20)));
        
        JLabel lblTituloSecao = new JLabel("Configuracoes de Exportacao");
        lblTituloSecao.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTituloSecao.setForeground(TEXT_PRIMARY);
        lblTituloSecao.setBorder(new EmptyBorder(0, 0, 15, 0));
        painel.add(lblTituloSecao, BorderLayout.NORTH);
        
        JPanel conteudo = new JPanel(new GridBagLayout());
        conteudo.setBackground(CARD_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel lblDiretorio = new JLabel("Diretorio de Destino:");
        lblDiretorio.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDiretorio.setForeground(TEXT_SECONDARY);
        conteudo.add(lblDiretorio, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtDiretorio = new JTextField(30);
        txtDiretorio.setEditable(false);
        txtDiretorio.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDiretorio.setBackground(new Color(249, 250, 251));
        txtDiretorio.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)));
        conteudo.add(txtDiretorio, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        btnSelecionarDiretorio = ButtonStyleFactory.createPrimaryButton("Selecionar");
        btnSelecionarDiretorio.setPreferredSize(new Dimension(110, 42));
        btnSelecionarDiretorio.addActionListener(e -> selecionarDiretorio());
        conteudo.add(btnSelecionarDiretorio, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(0, 25));
        progressBar.setForeground(PRIMARY_COLOR);
        conteudo.add(progressBar, gbc);
        
        gbc.gridy = 2;
        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(TEXT_SECONDARY);
        conteudo.add(lblStatus, gbc);
        
        painel.add(conteudo, BorderLayout.CENTER);
        return painel;
    }
    
    private JPanel criarPainelBotoes() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(BACKGROUND_COLOR);
        painel.setBorder(new EmptyBorder(10, 20, 20, 20));
        
        JPanel painelEsquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        painelEsquerda.setBackground(BACKGROUND_COLOR);
        JButton btnConfig = ButtonStyleFactory.createSecondaryButton("Configuracoes");
        btnConfig.setPreferredSize(new Dimension(130, 40));
        btnConfig.addActionListener(e -> abrirConfiguracoes());
        painelEsquerda.add(btnConfig);
        painel.add(painelEsquerda, BorderLayout.WEST);
        
        JPanel painelDireita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        painelDireita.setBackground(BACKGROUND_COLOR);
        
        btnValidar = ButtonStyleFactory.createInfoButton("Validar Dados");
        btnValidar.setPreferredSize(new Dimension(130, 40));
        btnValidar.addActionListener(e -> validarDados());
        painelDireita.add(btnValidar);
        
        btnExportar = ButtonStyleFactory.createSuccessButton("Exportar");
        btnExportar.setPreferredSize(new Dimension(110, 40));
        btnExportar.addActionListener(e -> exportar());
        btnExportar.setEnabled(false);
        painelDireita.add(btnExportar);
        
        JButton btnCancelar = ButtonStyleFactory.createDangerButton("Fechar");
        btnCancelar.setPreferredSize(new Dimension(100, 40));
        btnCancelar.addActionListener(e -> dispose());
        painelDireita.add(btnCancelar);
        
        painel.add(painelDireita, BorderLayout.EAST);
        return painel;
    }

    private void abrirConfiguracoes() {
        SiadsConfigDialog configDialog = new SiadsConfigDialog((Frame) getParent(), siadsService.getConfig());
        configDialog.setVisible(true);
    }
    
    private void carregarEstatisticas() {
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return siadsService.gerarEstatisticas();
            }
            @Override
            protected void done() {
                try {
                    parseEstatisticas(get());
                } catch (Exception e) {
                    atualizarCardEstatistica(lblTotalPatrimonios, "Total de Patrimonios", "Erro", DANGER_COLOR);
                }
            }
        };
        worker.execute();
    }
    
    private void parseEstatisticas(String stats) {
        try {
            String[] linhas = stats.split("\n");
            for (String linha : linhas) {
                linha = linha.trim();
                if (linha.toLowerCase().contains("total de patrim")) {
                    atualizarCardEstatistica(lblTotalPatrimonios, "Total de Patrimonios", extrairNumero(linha), PRIMARY_COLOR);
                } else if (linha.toLowerCase().contains("com respons")) {
                    atualizarCardEstatistica(lblComResponsavel, "Com Responsavel", extrairValorComPercentual(linha), SUCCESS_COLOR);
                } else if (linha.toLowerCase().contains("com localiza")) {
                    atualizarCardEstatistica(lblComLocalizacao, "Com Localizacao", extrairValorComPercentual(linha), WARNING_COLOR);
                }
            }
        } catch (Exception e) { }
    }
    
    private String extrairNumero(String linha) {
        String[] partes = linha.split(":");
        if (partes.length > 1) return partes[1].trim().replaceAll("[^0-9]", "");
        return "0";
    }
    
    private String extrairValorComPercentual(String linha) {
        String[] partes = linha.split(":");
        if (partes.length > 1) return partes[1].trim().replaceAll(",0%", "%");
        return "0";
    }
    
    private void selecionarDiretorio() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Selecione o diretorio de destino");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtDiretorio.setText(chooser.getSelectedFile().getAbsolutePath());
            btnExportar.setEnabled(true);
            lblStatus.setText("Diretorio selecionado. Pronto para exportar.");
            lblStatus.setForeground(SUCCESS_COLOR);
        }
    }

    private void validarDados() {
        lblStatus.setText("Validando dados...");
        lblStatus.setForeground(PRIMARY_COLOR);
        
        SwingWorker<List<String>, Void> worker = new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() {
                return siadsService.validarDadosParaExportacao();
            }
            @Override
            protected void done() {
                try {
                    List<String> erros = get();
                    if (erros.isEmpty()) {
                        lblStatus.setText("Dados validados com sucesso!");
                        lblStatus.setForeground(SUCCESS_COLOR);
                        JOptionPane.showMessageDialog(SiadsExportDialog.this,
                            "Todos os dados estao validos para exportacao SIADS.",
                            "Validacao", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        lblStatus.setText("Erros encontrados na validacao");
                        lblStatus.setForeground(DANGER_COLOR);
                        StringBuilder msg = new StringBuilder("Erros encontrados:\n\n");
                        for (String erro : erros) msg.append("  ").append(erro).append("\n");
                        JTextArea textArea = new JTextArea(msg.toString());
                        textArea.setEditable(false);
                        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                        JScrollPane scrollPane = new JScrollPane(textArea);
                        scrollPane.setPreferredSize(new Dimension(500, 300));
                        JOptionPane.showMessageDialog(SiadsExportDialog.this, scrollPane,
                            "Erros de Validacao", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception e) {
                    lblStatus.setText("Erro na validacao");
                    lblStatus.setForeground(DANGER_COLOR);
                    JOptionPane.showMessageDialog(SiadsExportDialog.this,
                        "Erro ao validar dados: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void exportar() {
        String diretorio = txtDiretorio.getText();
        if (diretorio == null || diretorio.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione um diretorio de destino.", "Atencao", JOptionPane.WARNING_MESSAGE);
            return;
        }
        File dir = new File(diretorio);
        if (!dir.exists() || !dir.isDirectory()) {
            JOptionPane.showMessageDialog(this, "Diretorio invalido.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        btnExportar.setEnabled(false);
        btnValidar.setEnabled(false);
        btnSelecionarDiretorio.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        lblStatus.setText("Exportando arquivos...");
        lblStatus.setForeground(PRIMARY_COLOR);
        
        SwingWorker<File, Void> worker = new SwingWorker<File, Void>() {
            @Override
            protected File doInBackground() throws Exception {
                return siadsService.gerarArquivoSiads(dir);
            }
            @Override
            protected void done() {
                progressBar.setVisible(false);
                btnExportar.setEnabled(true);
                btnValidar.setEnabled(true);
                btnSelecionarDiretorio.setEnabled(true);
                try {
                    File arquivo = get();
                    lblStatus.setText("Exportacao concluida com sucesso!");
                    lblStatus.setForeground(SUCCESS_COLOR);
                    int opcao = JOptionPane.showConfirmDialog(SiadsExportDialog.this,
                        "Arquivo exportado com sucesso:\n" + arquivo.getAbsolutePath() + "\n\nDeseja abrir o diretorio?",
                        "Sucesso", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                    if (opcao == JOptionPane.YES_OPTION) {
                        Desktop.getDesktop().open(arquivo.getParentFile());
                    }
                } catch (Exception e) {
                    lblStatus.setText("Erro na exportacao");
                    lblStatus.setForeground(DANGER_COLOR);
                    JOptionPane.showMessageDialog(SiadsExportDialog.this,
                        "Erro ao exportar arquivo:\n" + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
