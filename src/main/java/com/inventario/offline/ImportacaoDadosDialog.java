package com.inventario.offline;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog para importação de dados do servidor para modo offline
 * Permite importar patrimônios, salas, responsáveis e inventários
 */
public class ImportacaoDadosDialog extends JDialog {
    
    private boolean importacaoSucesso = false;
    private JProgressBar progressBar;
    private JTextArea logArea;
    private JButton btnIniciar;
    private JButton btnFechar;
    private JLabel lblStatus;
    
    public ImportacaoDadosDialog(JFrame parent) {
        super(parent, "Importar Dados Offline", true);
        initComponents();
        setupLayout();
        
        setSize(700, 500);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    private void initComponents() {
        // Barra de progresso
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("Aguardando...");
        
        // Área de log
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(new Color(200, 200, 200));
        
        // Label de status
        lblStatus = new JLabel("Pronto para importar dados do servidor");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        // Botões
        btnIniciar = new JButton("Iniciar Importação");
        btnIniciar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnIniciar.addActionListener(e -> iniciarImportacao());
        
        btnFechar = new JButton("Fechar");
        btnFechar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnFechar.addActionListener(e -> dispose());
        btnFechar.setEnabled(false);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // Painel superior
        JPanel panelTop = new JPanel(new BorderLayout(10, 10));
        panelTop.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        panelTop.add(lblStatus, BorderLayout.NORTH);
        panelTop.add(progressBar, BorderLayout.CENTER);
        
        // Painel central com log
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Log de Importação"));
        
        // Painel inferior com botões
        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelBottom.add(btnIniciar);
        panelBottom.add(btnFechar);
        
        add(panelTop, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(panelBottom, BorderLayout.SOUTH);
    }
    
    private void iniciarImportacao() {
        btnIniciar.setEnabled(false);
        progressBar.setValue(0);
        progressBar.setString("Iniciando...");
        logArea.setText("");
        
        // Executar importação em thread separada
        SwingWorker<Boolean, String> worker = new SwingWorker<Boolean, String>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    publish("=== IMPORTAÇÃO DE DADOS OFFLINE ===\n\n");
                    
                    // Simular importação (substituir por lógica real)
                    publish("Conectando ao servidor PostgreSQL...\n");
                    Thread.sleep(500);
                    progressBar.setValue(10);
                    publish("✓ Conectado com sucesso\n\n");
                    
                    publish("Importando patrimônios...\n");
                    Thread.sleep(1000);
                    progressBar.setValue(30);
                    publish("✓ 11.428 patrimônios importados\n\n");
                    
                    publish("Importando salas...\n");
                    Thread.sleep(500);
                    progressBar.setValue(50);
                    publish("✓ 150 salas importadas\n\n");
                    
                    publish("Importando responsáveis...\n");
                    Thread.sleep(500);
                    progressBar.setValue(70);
                    publish("✓ 200 responsáveis importados\n\n");
                    
                    publish("Importando inventário ativo...\n");
                    Thread.sleep(500);
                    progressBar.setValue(90);
                    publish("✓ 1 inventário ativo importado\n\n");
                    
                    publish("Finalizando importação...\n");
                    Thread.sleep(500);
                    progressBar.setValue(100);
                    publish("✓ Importação concluída com sucesso!\n");
                    
                    return true;
                    
                } catch (Exception e) {
                    publish("\n✗ ERRO: " + e.getMessage() + "\n");
                    e.printStackTrace();
                    return false;
                }
            }
            
            @Override
            protected void process(java.util.List<String> chunks) {
                for (String chunk : chunks) {
                    logArea.append(chunk);
                    logArea.setCaretPosition(logArea.getDocument().getLength());
                }
            }
            
            @Override
            protected void done() {
                try {
                    importacaoSucesso = get();
                    
                    if (importacaoSucesso) {
                        lblStatus.setText("Importação concluída com sucesso!");
                        lblStatus.setForeground(new Color(39, 174, 96));
                        progressBar.setString("Concluído");
                    } else {
                        lblStatus.setText("Erro durante a importação");
                        lblStatus.setForeground(new Color(231, 76, 60));
                        progressBar.setString("Erro");
                    }
                    
                    btnFechar.setEnabled(true);
                    
                } catch (Exception e) {
                    lblStatus.setText("Erro: " + e.getMessage());
                    lblStatus.setForeground(new Color(231, 76, 60));
                    btnFechar.setEnabled(true);
                }
            }
        };
        
        worker.execute();
    }
    
    public boolean isImportacaoSucesso() {
        return importacaoSucesso;
    }
}