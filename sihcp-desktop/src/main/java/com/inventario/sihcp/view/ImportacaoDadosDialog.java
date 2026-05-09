package com.inventario.sihcp.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import com.inventario.sihcp.model.Usuario;
import com.inventario.sihcp.service.DataImportService;
import com.inventario.sihcp.service.DataImportService.ImportResult;
import com.inventario.sihcp.service.DataImportService.ProgressListener;

/**
 * Dialog para importação de dados do servidor para modo offline
 * 
 * @author Sistema de Inventário IFMT
 * @version 1.0.0
 */
public class ImportacaoDadosDialog extends JDialog {
    
    private static final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(44, 62, 80);
    
    private final DataImportService importService;
    
    // Componentes UI
    private JProgressBar progressBar;
    private JLabel lblStatus;
    private JLabel lblPatrimonios;
    private JLabel lblSalas;
    private JLabel lblResponsaveis;
    private JLabel lblInventario;
    private JLabel lblUsuario;
    private JButton btnIniciar;
    private JButton btnFechar;
    private JButton btnCancelar;
    private JTextArea txtLog;
    
    private boolean importacaoEmAndamento = false;
    private SwingWorker<ImportResult, Void> importWorker;
    
    public ImportacaoDadosDialog(Frame parent, Usuario usuario) {
        super(parent, "Importar Dados para Modo Offline", true);
        this.importService = new DataImportService();
        
        initComponents();
        setLocationRelativeTo(parent);
    }

    
    private void initComponents() {
        setSize(600, 550);
        setResizable(false);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        // Adicionar listener para confirmar fechamento durante importação
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (importacaoEmAndamento) {
                    int opcao = JOptionPane.showConfirmDialog(ImportacaoDadosDialog.this, """
                                                                                          A importa\u00e7\u00e3o est\u00e1 em andamento.
                                                                                          
                                                                                          Deseja cancelar e fechar?""",
                        "Importação em Andamento",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                    
                    if (opcao == JOptionPane.YES_OPTION) {
                        if (importWorker != null && !importWorker.isDone()) {
                            importWorker.cancel(true);
                        }
                        dispose();
                    }
                } else {
                    dispose();
                }
            }
        });
        
        // Definir ícone
        setIconImages(com.inventario.sihcp.util.IconManager.getAppIconImages());
        
        // Painel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(CARD_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Cabeçalho
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        
        // Área de progresso
        mainPanel.add(createProgressPanel(), BorderLayout.CENTER);
        
        // Botões
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);
        
        JLabel titleLabel = new JLabel("📥 Importar Dados para Modo Offline");
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR);
        
        JLabel subtitleLabel = new JLabel(
            "<html>Esta operação irá baixar dados do servidor para permitir<br>" +
            "que você trabalhe offline. Isso pode levar alguns minutos.</html>"
        );
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(127, 140, 141));
        subtitleLabel.setBorder(new EmptyBorder(5, 0, 0, 0));
        
        JPanel titleContainer = new JPanel();
        titleContainer.setLayout(new BoxLayout(titleContainer, BoxLayout.Y_AXIS));
        titleContainer.setBackground(CARD_COLOR);
        titleContainer.add(titleLabel);
        titleContainer.add(subtitleLabel);
        
        headerPanel.add(titleContainer, BorderLayout.CENTER);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        return headerPanel;
    }

    
    private JPanel createProgressPanel() {
        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
        progressPanel.setBackground(CARD_COLOR);
        
        // Status geral
        lblStatus = new JLabel("Pronto para iniciar importação");
        lblStatus.setFont(new Font("Arial", Font.BOLD, 14));
        lblStatus.setForeground(TEXT_COLOR);
        lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Barra de progresso
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(560, 30));
        progressBar.setMaximumSize(new Dimension(560, 30));
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Itens de importação
        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBackground(CARD_COLOR);
        itemsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        itemsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        lblPatrimonios = createItemLabel("⏳ Patrimônios: aguardando...");
        lblSalas = createItemLabel("⏳ Salas: aguardando...");
        lblResponsaveis = createItemLabel("⏳ Responsáveis: aguardando...");
        lblInventario = createItemLabel("⏳ Inventário: aguardando...");
        lblUsuario = createItemLabel("⏳ Credenciais: aguardando...");
        
        itemsPanel.add(lblPatrimonios);
        itemsPanel.add(Box.createVerticalStrut(8));
        itemsPanel.add(lblSalas);
        itemsPanel.add(Box.createVerticalStrut(8));
        itemsPanel.add(lblResponsaveis);
        itemsPanel.add(Box.createVerticalStrut(8));
        itemsPanel.add(lblInventario);
        itemsPanel.add(Box.createVerticalStrut(8));
        itemsPanel.add(lblUsuario);
        
        // Log de importação
        txtLog = new JTextArea(8, 50);
        txtLog.setEditable(false);
        txtLog.setFont(new Font("Monospaced", Font.PLAIN, 11));
        txtLog.setBackground(new Color(245, 245, 245));
        JScrollPane scrollLog = new JScrollPane(txtLog);
        scrollLog.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Adicionar componentes
        progressPanel.add(lblStatus);
        progressPanel.add(Box.createVerticalStrut(10));
        progressPanel.add(progressBar);
        progressPanel.add(Box.createVerticalStrut(15));
        progressPanel.add(itemsPanel);
        progressPanel.add(Box.createVerticalStrut(10));
        progressPanel.add(scrollLog);
        
        return progressPanel;
    }
    
    private JLabel createItemLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        label.setForeground(TEXT_COLOR);
        return label;
    }

    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(CARD_COLOR);
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Arial", Font.PLAIN, 12));
        btnCancelar.setEnabled(false);
        btnCancelar.addActionListener(e -> cancelarImportacao());
        
        btnIniciar = new JButton("Iniciar Importação");
        btnIniciar.setFont(new Font("Arial", Font.PLAIN, 12));
        btnIniciar.addActionListener(e -> iniciarImportacao());
        
        btnFechar = new JButton("Fechar");
        btnFechar.setFont(new Font("Arial", Font.PLAIN, 12));
        btnFechar.addActionListener(e -> dispose());
        
        buttonPanel.add(btnCancelar);
        buttonPanel.add(btnIniciar);
        buttonPanel.add(btnFechar);
        
        return buttonPanel;
    }

    
    private void iniciarImportacao() {
        System.out.println(">>> DEBUG: iniciarImportacao() CHAMADO!");
        
        if (importacaoEmAndamento) {
            System.out.println(">>> DEBUG: Importação já em andamento, retornando...");
            return;
        }
        
        importacaoEmAndamento = true;
        btnIniciar.setEnabled(false);
        btnCancelar.setEnabled(true);
        btnFechar.setEnabled(false); // Desabilitar durante importação
        
        lblStatus.setText("Importando dados do servidor...");
        lblStatus.setForeground(PRIMARY_COLOR);
        addLog("=== Iniciando importação de dados ===");
        
        System.out.println(">>> DEBUG: Criando SwingWorker para importação...");
        
        // Executar importação em thread separada
        importWorker = new SwingWorker<ImportResult, Void>() {
            @Override
            protected ImportResult doInBackground() throws Exception {
                System.out.println(">>> DEBUG: SwingWorker.doInBackground() INICIADO!");
                System.out.println(">>> DEBUG: Chamando importService.importarTodosDados()...");
                
                ImportResult result = importService.importarTodosDados(new ProgressListener() {
                    @Override
                    public void onProgress(String message, int progress) {
                        System.out.println(">>> DEBUG: onProgress() - " + message + " (" + progress + "%)");
                        
                        // Verificar se foi cancelado
                        if (isCancelled()) {
                            throw new RuntimeException(new InterruptedException("Importação cancelada pelo usuário"));
                        }
                        
                        SwingUtilities.invokeLater(() -> {
                            progressBar.setValue(progress);
                            addLog(message);
                            atualizarStatusItens(message, progress);
                        });
                    }
                    
                    @Override
                    public void onError(String error) {
                        System.err.println(">>> DEBUG: onError() - " + error);
                        SwingUtilities.invokeLater(() -> {
                            addLog("ERRO: " + error);
                        });
                    }
                });
                
                System.out.println(">>> DEBUG: importService.importarTodosDados() RETORNOU!");
                System.out.println(">>> DEBUG: Resultado - Success: " + result.success);
                if (result.success) {
                    System.out.println(">>> DEBUG: Patrimônios: " + result.patrimonios);
                    System.out.println(">>> DEBUG: Salas: " + result.salas);
                    System.out.println(">>> DEBUG: Responsáveis: " + result.responsaveis);
                }
                
                return result;
            }
            
            @Override
            protected void done() {
                System.out.println(">>> DEBUG: SwingWorker.done() CHAMADO!");
                
                try {
                    if (isCancelled()) {
                        System.out.println(">>> DEBUG: Importação foi cancelada");
                        finalizarCancelamento();
                    } else {
                        System.out.println(">>> DEBUG: Obtendo resultado...");
                        ImportResult result = get();
                        System.out.println(">>> DEBUG: Finalizando importação...");
                        finalizarImportacao(result);
                    }
                } catch (java.util.concurrent.CancellationException e) {
                    System.err.println(">>> DEBUG: CancellationException - " + e.getMessage());
                    finalizarCancelamento();
                } catch (InterruptedException e) {
                    System.err.println(">>> DEBUG: InterruptedException - " + e.getMessage());
                    finalizarCancelamento();
                } catch (java.util.concurrent.ExecutionException e) {
                    System.err.println(">>> DEBUG: ExecutionException - " + e.getMessage());
                    
                    // Verificar se foi cancelamento encapsulado
                    Throwable cause = e.getCause();
                    if (cause instanceof RuntimeException && 
                        cause.getCause() instanceof InterruptedException) {
                        finalizarCancelamento();
                    } else {
                        finalizarComErro(e);
                    }
                } catch (Exception e) {
                    System.err.println(">>> DEBUG: Exception genérica - " + e.getMessage());
                    finalizarComErro(e);
                }
            }
        };
        
        System.out.println(">>> DEBUG: Executando SwingWorker...");
        importWorker.execute();
        System.out.println(">>> DEBUG: SwingWorker.execute() chamado!");
    }

    
    private void atualizarStatusItens(String message, int progress) {
        if (message.contains("Patrimônios")) {
            if (message.contains("importados")) {
                lblPatrimonios.setText("✅ " + message);
                lblPatrimonios.setForeground(SUCCESS_COLOR);
            } else {
                lblPatrimonios.setText("⏳ " + message);
                lblPatrimonios.setForeground(PRIMARY_COLOR);
            }
        } else if (message.contains("Salas")) {
            if (message.contains("importadas")) {
                lblSalas.setText("✅ " + message);
                lblSalas.setForeground(SUCCESS_COLOR);
            } else {
                lblSalas.setText("⏳ " + message);
                lblSalas.setForeground(PRIMARY_COLOR);
            }
        } else if (message.contains("Responsáveis")) {
            if (message.contains("importados")) {
                lblResponsaveis.setText("✅ " + message);
                lblResponsaveis.setForeground(SUCCESS_COLOR);
            } else {
                lblResponsaveis.setText("⏳ " + message);
                lblResponsaveis.setForeground(PRIMARY_COLOR);
            }
        } else if (message.contains("Inventário")) {
            if (message.contains("importado")) {
                lblInventario.setText("✅ " + message);
                lblInventario.setForeground(SUCCESS_COLOR);
            } else {
                lblInventario.setText("⏳ " + message);
                lblInventario.setForeground(PRIMARY_COLOR);
            }
        } else if (message.contains("Credenciais") || message.contains("credenciais")) {
            if (message.contains("salvas") || message.contains("Concluído")) {
                lblUsuario.setText("✅ Credenciais salvas");
                lblUsuario.setForeground(SUCCESS_COLOR);
            } else {
                lblUsuario.setText("⏳ " + message);
                lblUsuario.setForeground(PRIMARY_COLOR);
            }
        }
    }
    
    private void finalizarImportacao(ImportResult result) {
        importacaoEmAndamento = false;
        
        btnCancelar.setEnabled(false);
        btnFechar.setEnabled(true);
        
        if (result.success) {
            // Verificar se os dados realmente foram salvos no SQLite
            addLog("=== Verificando dados no banco SQLite ===");
            verificarDadosImportados(result);
            
            lblStatus.setText("✅ Importação concluída com sucesso!");
            lblStatus.setForeground(SUCCESS_COLOR);
            progressBar.setValue(100);
            
            addLog("=== Importação concluída ===");
            addLog(String.format("Total: %d patrimônios, %d salas, %d responsáveis",
                result.patrimonios, result.salas, result.responsaveis));
            
            JOptionPane.showMessageDialog(this,
                """
                Dados importados com sucesso!
                
                Patrim\u00f4nios: """ + result.patrimonios + "\n" +
                "Salas: " + result.salas + "\n" +
                "Responsáveis: " + result.responsaveis + "\n\n" +
                "Você agora pode trabalhar em modo offline.",
                "Importação Concluída",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            lblStatus.setText("❌ Erro na importação");
            lblStatus.setForeground(DANGER_COLOR);
            addLog("ERRO: " + result.errorMessage);
            
            JOptionPane.showMessageDialog(this,
                "Erro ao importar dados:\n" + result.errorMessage,
                "Erro na Importação",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Verifica se os dados foram realmente salvos no banco SQLite
     */
    private void verificarDadosImportados(ImportResult result) {
        try {
            java.sql.Connection conn = com.inventario.sihcp.offline.SQLiteConnection.getInstance().getConnection();
            
            // Verificar patrimônios
            try (java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM local_patrimonio")) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    addLog("✅ Patrimônios no SQLite: " + count);
                    if (count != result.patrimonios) {
                        addLog("⚠️ AVISO: Esperado " + result.patrimonios + " mas encontrado " + count);
                    }
                }
            }
            
            // Verificar salas
            try (java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM local_sala")) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    addLog("✅ Salas no SQLite: " + count);
                    if (count != result.salas) {
                        addLog("⚠️ AVISO: Esperado " + result.salas + " mas encontrado " + count);
                    }
                }
            }
            
            // Verificar responsáveis
            try (java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM local_responsavel")) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    addLog("✅ Responsáveis no SQLite: " + count);
                    if (count != result.responsaveis) {
                        addLog("⚠️ AVISO: Esperado " + result.responsaveis + " mas encontrado " + count);
                    }
                }
            }
            
            // Verificar tabelas de compatibilidade
            try (java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM TABELA_INVENTARIO")) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    addLog("✅ Inventários (TABELA_INVENTARIO): " + count);
                }
            } catch (Exception e) {
                addLog("⚠️ Tabela TABELA_INVENTARIO não encontrada: " + e.getMessage());
            }
            
            try (java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM SALA")) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    addLog("✅ Salas (SALA): " + count);
                }
            } catch (Exception e) {
                addLog("⚠️ Tabela SALA não encontrada: " + e.getMessage());
            }
            
        } catch (SQLException e) {
            addLog("⚠️ Erro ao verificar dados: " + e.getMessage());
        }
    }
    
    private void finalizarComErro(Exception e) {
        importacaoEmAndamento = false;
        btnCancelar.setEnabled(false);
        btnFechar.setEnabled(true);
        
        lblStatus.setText("❌ Erro na importação");
        lblStatus.setForeground(DANGER_COLOR);
        addLog("ERRO: " + e.getMessage());
        
        JOptionPane.showMessageDialog(this,
            "Erro ao importar dados:\n" + e.getMessage(),
            "Erro na Importação",
            JOptionPane.ERROR_MESSAGE);
    }
    
    private void cancelarImportacao() {
        int opcao = JOptionPane.showConfirmDialog(this, """
                                                        Deseja realmente cancelar a importa\u00e7\u00e3o?
                                                        
                                                        \u26a0\ufe0f ATEN\u00c7\u00c3O: Os dados j\u00e1 importados ser\u00e3o mantidos,
                                                        mas a importa\u00e7\u00e3o n\u00e3o estar\u00e1 completa.""",
            "Cancelar Importação",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (opcao == JOptionPane.YES_OPTION) {
            if (importWorker != null && !importWorker.isDone()) {
                addLog("⚠️ Cancelando importação...");
                lblStatus.setText("⚠️ Cancelando importação...");
                lblStatus.setForeground(DANGER_COLOR);
                
                // Cancelar o worker
                importWorker.cancel(true);
                
                // Desabilitar botão de cancelar
                btnCancelar.setEnabled(false);
            }
        }
    }
    
    /**
     * Finaliza a importação após cancelamento
     */
    private void finalizarCancelamento() {
        importacaoEmAndamento = false;
        
        btnCancelar.setEnabled(false);
        btnFechar.setEnabled(true);
        btnIniciar.setEnabled(true);
        
        lblStatus.setText("⚠️ Importação cancelada");
        lblStatus.setForeground(DANGER_COLOR);
        
        addLog("=== Importação cancelada pelo usuário ===");
        addLog("⚠️ Dados parcialmente importados podem estar disponíveis");
        
        JOptionPane.showMessageDialog(this, """
                                            Importa\u00e7\u00e3o cancelada!
                                            
                                            \u26a0\ufe0f Os dados j\u00e1 importados foram mantidos,
                                            mas a importa\u00e7\u00e3o n\u00e3o est\u00e1 completa.
                                            
                                            Voc\u00ea pode tentar novamente quando desejar.""",
            "Importação Cancelada",
            JOptionPane.WARNING_MESSAGE);
    }
    
    private void addLog(String message) {
        String timestamp = java.time.LocalTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        txtLog.append("[" + timestamp + "] " + message + "\n");
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }
}
