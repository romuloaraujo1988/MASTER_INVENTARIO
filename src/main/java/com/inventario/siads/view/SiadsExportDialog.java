package com.inventario.siads.view;

import com.inventario.siads.service.SiadsIntegrationService;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Dialog para exportação de arquivos SIADS
 */
public class SiadsExportDialog extends JDialog {
    
    private final SiadsIntegrationService siadsService;
    
    private JTextArea txtEstatisticas;
    private JTextField txtDiretorio;
    private JButton btnSelecionarDiretorio;
    private JButton btnValidar;
    private JButton btnExportar;
    private JButton btnCancelar;
    private JProgressBar progressBar;
    private JLabel lblStatus;
    
    public SiadsExportDialog(Frame parent) {
        super(parent, "Exportação SIADS", true);
        this.siadsService = new SiadsIntegrationService();
        
        initComponents();
        carregarEstatisticas();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(700, 500);
        setLocationRelativeTo(getParent());
        
        // Painel superior - Estatísticas
        JPanel panelTop = new JPanel(new BorderLayout(5, 5));
        panelTop.setBorder(BorderFactory.createTitledBorder("Estatísticas"));
        
        txtEstatisticas = new JTextArea(5, 50);
        txtEstatisticas.setEditable(false);
        txtEstatisticas.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollStats = new JScrollPane(txtEstatisticas);
        panelTop.add(scrollStats, BorderLayout.CENTER);
        
        add(panelTop, BorderLayout.NORTH);
        
        // Painel central - Configurações
        JPanel panelCenter = new JPanel(new GridBagLayout());
        panelCenter.setBorder(BorderFactory.createTitledBorder("Configurações de Exportação"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Diretório de destino
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panelCenter.add(new JLabel("Diretório:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtDiretorio = new JTextField(30);
        txtDiretorio.setEditable(false);
        panelCenter.add(txtDiretorio, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0;
        btnSelecionarDiretorio = new JButton("Selecionar...");
        btnSelecionarDiretorio.addActionListener(e -> selecionarDiretorio());
        panelCenter.add(btnSelecionarDiretorio, gbc);
        
        // Progress bar
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        panelCenter.add(progressBar, gbc);
        
        // Status
        gbc.gridy = 2;
        lblStatus = new JLabel(" ");
        lblStatus.setForeground(Color.BLUE);
        panelCenter.add(lblStatus, gbc);
        
        add(panelCenter, BorderLayout.CENTER);
        
        // Painel inferior - Botões
        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton btnConfig = new JButton("Configurações");
        btnConfig.addActionListener(e -> abrirConfiguracoes());
        panelBottom.add(btnConfig);
        
        panelBottom.add(Box.createHorizontalStrut(20)); // Espaçador
        
        btnValidar = new JButton("Validar Dados");
        btnValidar.addActionListener(e -> validarDados());
        panelBottom.add(btnValidar);
        
        btnExportar = new JButton("Exportar");
        btnExportar.addActionListener(e -> exportar());
        btnExportar.setEnabled(false);
        panelBottom.add(btnExportar);
        
        btnCancelar = new JButton("Fechar");
        btnCancelar.addActionListener(e -> dispose());
        panelBottom.add(btnCancelar);
        
        add(panelBottom, BorderLayout.SOUTH);
    }
    
    private void abrirConfiguracoes() {
        SiadsConfigDialog configDialog = new SiadsConfigDialog(
            (Frame) getParent(), 
            siadsService.getConfig()
        );
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
                    txtEstatisticas.setText(get());
                } catch (Exception e) {
                    txtEstatisticas.setText("Erro ao carregar estatísticas: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    private void selecionarDiretorio() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Selecione o diretório de destino");
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File dir = chooser.getSelectedFile();
            txtDiretorio.setText(dir.getAbsolutePath());
            btnExportar.setEnabled(true);
        }
    }
    
    private void validarDados() {
        lblStatus.setText("Validando dados...");
        lblStatus.setForeground(Color.BLUE);
        
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
                        lblStatus.setText("✓ Dados validados com sucesso!");
                        lblStatus.setForeground(new Color(0, 128, 0));
                        JOptionPane.showMessageDialog(
                            SiadsExportDialog.this,
                            "Todos os dados estão válidos para exportação SIADS.",
                            "Validação",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    } else {
                        lblStatus.setText("✗ Erros encontrados na validação");
                        lblStatus.setForeground(Color.RED);
                        
                        StringBuilder msg = new StringBuilder("Erros encontrados:\n\n");
                        for (String erro : erros) {
                            msg.append("• ").append(erro).append("\n");
                        }
                        
                        JTextArea textArea = new JTextArea(msg.toString());
                        textArea.setEditable(false);
                        JScrollPane scrollPane = new JScrollPane(textArea);
                        scrollPane.setPreferredSize(new Dimension(500, 300));
                        
                        JOptionPane.showMessageDialog(
                            SiadsExportDialog.this,
                            scrollPane,
                            "Erros de Validação",
                            JOptionPane.WARNING_MESSAGE
                        );
                    }
                } catch (Exception e) {
                    lblStatus.setText("Erro na validação");
                    lblStatus.setForeground(Color.RED);
                    JOptionPane.showMessageDialog(
                        SiadsExportDialog.this,
                        "Erro ao validar dados: " + e.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        worker.execute();
    }
    
    private void exportar() {
        String diretorio = txtDiretorio.getText();
        
        if (diretorio == null || diretorio.trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Selecione um diretório de destino.",
                "Atenção",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        File dir = new File(diretorio);
        if (!dir.exists() || !dir.isDirectory()) {
            JOptionPane.showMessageDialog(
                this,
                "Diretório inválido.",
                "Erro",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        
        // Desabilitar botões durante exportação
        btnExportar.setEnabled(false);
        btnValidar.setEnabled(false);
        btnSelecionarDiretorio.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        lblStatus.setText("Exportando...");
        lblStatus.setForeground(Color.BLUE);
        
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
                    lblStatus.setText("✓ Exportação concluída!");
                    lblStatus.setForeground(new Color(0, 128, 0));
                    
                    int opcao = JOptionPane.showConfirmDialog(
                        SiadsExportDialog.this,
                        "Arquivo exportado com sucesso:\n" + arquivo.getAbsolutePath() +
                        "\n\nDeseja abrir o diretório?",
                        "Sucesso",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    
                    if (opcao == JOptionPane.YES_OPTION) {
                        Desktop.getDesktop().open(arquivo.getParentFile());
                    }
                    
                } catch (Exception e) {
                    lblStatus.setText("✗ Erro na exportação");
                    lblStatus.setForeground(Color.RED);
                    JOptionPane.showMessageDialog(
                        SiadsExportDialog.this,
                        "Erro ao exportar arquivo:\n" + e.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE
                    );
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
}
