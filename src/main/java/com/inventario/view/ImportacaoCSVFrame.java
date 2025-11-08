package com.inventario.view;

import com.inventario.util.ImportacaoCSV;
import com.inventario.util.ImportacaoCSV.RelatorioImportacao;
import com.inventario.util.ImportacaoExcel;
import com.inventario.view.ui.ButtonStyleFactory;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import javax.swing.SwingWorker;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import com.inventario.util.DatabaseConnection;

/**
 * Interface para importação de arquivos CSV do SUAP
 * Permite selecionar arquivo, configurar opções e acompanhar o progresso da importação
 */
public class ImportacaoCSVFrame extends JFrame {
    private JTextField campoArquivo;
    private JButton btnSelecionarArquivo;
    private JButton btnImportar;
    private JButton btnLimpar;
    private JTextArea areaLog;
    private JProgressBar progressBar;
    private JLabel lblStatus;
    private JLabel lblEstatisticas;
    
    // Opções de importação
    private JCheckBox chkCriarResponsaveis;
    private JCheckBox chkCriarSetores;
    private JCheckBox chkCriarSalas;
    private JCheckBox chkAtualizarExistentes;
    private JCheckBox chkIgnorarErros;
    
    private File arquivoSelecionado;
    private ImportacaoCSV importacao;
    private SwingWorker<RelatorioImportacao, String> workerAtual;
    private JButton btnCancelar;
    
    public ImportacaoCSVFrame() {
        this.importacao = new ImportacaoCSV();
        initializeComponents();
        setupLayout();
        setupEventListeners();
        aplicarEstiloModerno();
        
        setTitle("Importação de Dados do SUAP (CSV/Excel)");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 750);
        setLocationRelativeTo(null);
    }
    
    private void initializeComponents() {
        // Campo de arquivo
        campoArquivo = new JTextField(40);
        campoArquivo.setEditable(false);
        btnSelecionarArquivo = ButtonStyleFactory.createPrimaryButton("Selecionar");
        btnSelecionarArquivo.setPreferredSize(new Dimension(180, 40)); // Alargado para caber o texto
        
        // Botões de ação
        btnImportar = ButtonStyleFactory.createSuccessButton("Iniciar");
        btnImportar.setEnabled(false);
        btnCancelar = ButtonStyleFactory.createDangerButton("Cancelar Importação");
        btnCancelar.setVisible(false);
        btnLimpar = ButtonStyleFactory.createSecondaryButton("Limpar");
        
        // Área de log
        areaLog = new JTextArea(15, 60);
        areaLog.setEditable(false);
        areaLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        areaLog.setBackground(Color.BLACK);
        areaLog.setForeground(Color.GREEN);
        
        // Barra de progresso
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("Aguardando...");
        
        // Labels de status
        lblStatus = new JLabel("Selecione um arquivo CSV ou Excel (.xls, .xlsx) para começar");
        lblEstatisticas = new JLabel(" ");
        
        // Opções de importação
        chkCriarResponsaveis = new JCheckBox("Criar responsáveis automaticamente", true);
        chkCriarSetores = new JCheckBox("Criar setores automaticamente", true);
        chkCriarSalas = new JCheckBox("Criar salas automaticamente", true);
        chkAtualizarExistentes = new JCheckBox("Atualizar patrimônios existentes", true);
        chkIgnorarErros = new JCheckBox("Continuar processamento mesmo com erros", true);
        
        // Configurar tooltips
        configurarTooltips();
    }
    
    private void configurarTooltips() {
        campoArquivo.setToolTipText("Caminho do arquivo CSV/Excel exportado do SUAP");
        chkCriarResponsaveis.setToolTipText("Se marcado, cria automaticamente responsáveis que não existem no sistema");
        chkCriarSetores.setToolTipText("Se marcado, cria automaticamente setores que não existem no sistema");
        chkCriarSalas.setToolTipText("Se marcado, cria automaticamente salas que não existem no sistema");
        chkAtualizarExistentes.setToolTipText("Se marcado, atualiza dados de patrimônios que já existem no sistema");
        chkIgnorarErros.setToolTipText("Se marcado, continua a importação mesmo quando encontra erros em linhas específicas");
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel superior - seleção de arquivo
        JPanel panelArquivo = new JPanel(new BorderLayout());
        panelArquivo.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), 
            "Arquivo CSV/Excel", 
            TitledBorder.LEFT, 
            TitledBorder.TOP, 
            new Font("Segoe UI", Font.BOLD, 12), 
            new Color(60, 60, 60)
        ));
        panelArquivo.setBackground(Color.WHITE);
        panelArquivo.setBorder(BorderFactory.createCompoundBorder(
            panelArquivo.getBorder(),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        JPanel panelCampoArquivo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelCampoArquivo.setBackground(Color.WHITE);
        JLabel lblArquivo = new JLabel("Arquivo:");
        lblArquivo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblArquivo.setForeground(new Color(60, 60, 60));
        panelCampoArquivo.add(lblArquivo);
        panelCampoArquivo.add(campoArquivo);
        panelCampoArquivo.add(btnSelecionarArquivo);
        
        panelArquivo.add(panelCampoArquivo, BorderLayout.CENTER);
        
        // Panel de opções
        JPanel panelOpcoes = new JPanel(new GridLayout(3, 2, 10, 8));
        panelOpcoes.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), 
            "Opções de Importação", 
            TitledBorder.LEFT, 
            TitledBorder.TOP, 
            new Font("Segoe UI", Font.BOLD, 12), 
            new Color(60, 60, 60)
        ));
        panelOpcoes.setBackground(Color.WHITE);
        panelOpcoes.setBorder(BorderFactory.createCompoundBorder(
            panelOpcoes.getBorder(),
            new EmptyBorder(10, 10, 10, 10)
        ));
        panelOpcoes.add(chkCriarResponsaveis);
        panelOpcoes.add(chkCriarSetores);
        panelOpcoes.add(chkCriarSalas);
        panelOpcoes.add(chkAtualizarExistentes);
        panelOpcoes.add(chkIgnorarErros);
        panelOpcoes.add(new JLabel()); // Espaço vazio
        
        // Panel superior completo
        JPanel panelSuperior = new JPanel(new BorderLayout(0, 10));
        panelSuperior.setBackground(new Color(245, 245, 245));
        panelSuperior.setBorder(new EmptyBorder(15, 15, 10, 15));
        panelSuperior.add(panelArquivo, BorderLayout.NORTH);
        panelSuperior.add(panelOpcoes, BorderLayout.CENTER);
        
        // Panel central - log e progresso
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), 
            "Log de Importação", 
            TitledBorder.LEFT, 
            TitledBorder.TOP, 
            new Font("Segoe UI", Font.BOLD, 12), 
            new Color(60, 60, 60)
        ));
        panelCentral.setBackground(Color.WHITE);
        panelCentral.setBorder(BorderFactory.createCompoundBorder(
            panelCentral.getBorder(),
            new EmptyBorder(10, 15, 10, 15)
        ));
        
        JScrollPane scrollLog = new JScrollPane(areaLog);
        scrollLog.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollLog.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        panelCentral.add(scrollLog, BorderLayout.CENTER);
        
        // Panel de progresso
        JPanel panelProgresso = new JPanel(new BorderLayout(10, 0));
        panelProgresso.setBackground(Color.WHITE);
        panelProgresso.setBorder(new EmptyBorder(10, 0, 0, 0));
        JLabel lblProgresso = new JLabel("Progresso:");
        lblProgresso.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblProgresso.setForeground(new Color(60, 60, 60));
        panelProgresso.add(lblProgresso, BorderLayout.WEST);
        panelProgresso.add(progressBar, BorderLayout.CENTER);
        
        panelCentral.add(panelProgresso, BorderLayout.SOUTH);
        
        // Panel inferior - status e ações
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBackground(new Color(245, 245, 245));
        panelInferior.setBorder(new EmptyBorder(10, 15, 15, 15));
        
        JPanel panelStatus = new JPanel(new GridLayout(2, 1, 0, 5));
        panelStatus.setBackground(new Color(245, 245, 245));
        panelStatus.add(lblStatus);
        panelStatus.add(lblEstatisticas);
        
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBotoes.setBackground(new Color(245, 245, 245));
        panelBotoes.add(btnLimpar);
        panelBotoes.add(btnCancelar);
        panelBotoes.add(btnImportar);
        
        panelInferior.add(panelStatus, BorderLayout.CENTER);
        panelInferior.add(panelBotoes, BorderLayout.EAST);
        
        add(panelSuperior, BorderLayout.NORTH);
        add(panelCentral, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);
    }
    
    private void setupEventListeners() {
        btnSelecionarArquivo.addActionListener(e -> selecionarArquivo());
        btnImportar.addActionListener(e -> iniciarImportacao());
        btnCancelar.addActionListener(e -> cancelarImportacao());
        btnLimpar.addActionListener(e -> limparLog());
    }
    
    private void cancelarImportacao() {
        int confirmacao = JOptionPane.showConfirmDialog(this,
            "Deseja realmente cancelar a importação?\n\n" +
            "IMPORTANTE:\n" +
            "• Os patrimônios já importados serão mantidos no banco\n" +
            "• O processo será interrompido imediatamente\n" +
            "• Um relatório parcial será gerado\n\n" +
            "Confirma o cancelamento?",
            "Confirmar Cancelamento",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirmacao == JOptionPane.YES_OPTION && workerAtual != null) {
            adicionarLog("\n⚠️ CANCELAMENTO SOLICITADO PELO USUÁRIO");
            adicionarLog("Aguarde... Finalizando operações em andamento...");
            workerAtual.cancel(true);
            btnCancelar.setEnabled(false);
            btnCancelar.setText("Cancelando...");
        }
    }
    
    private void selecionarArquivo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter(
            "Arquivos CSV/Excel (*.csv, *.xlsx, *.xls)", "csv", "xlsx", "xls"));
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        fileChooser.setDialogTitle("Selecionar Arquivo para Importação");
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            arquivoSelecionado = fileChooser.getSelectedFile();
            campoArquivo.setText(arquivoSelecionado.getAbsolutePath());
            btnImportar.setEnabled(true);
            
            // Verificar se o arquivo existe e é legível
            if (!arquivoSelecionado.exists()) {
                adicionarLog("❌ ERRO: Arquivo não encontrado: " + arquivoSelecionado.getAbsolutePath());
                btnImportar.setEnabled(false);
            } else if (!arquivoSelecionado.canRead()) {
                adicionarLog("❌ ERRO: Não é possível ler o arquivo: " + arquivoSelecionado.getAbsolutePath());
                btnImportar.setEnabled(false);
            } else {
                String nome = arquivoSelecionado.getName().toLowerCase();
                String tipo = nome.endsWith(".csv") ? "CSV" : 
                             nome.endsWith(".xlsx") ? "Excel (XLSX)" : 
                             nome.endsWith(".xls") ? "Excel (XLS)" : "Desconhecido";
                
                adicionarLog("✓ Arquivo selecionado: " + arquivoSelecionado.getName());
                adicionarLog("  Tipo: " + tipo);
                adicionarLog("  Tamanho: " + formatarTamanhoArquivo(arquivoSelecionado.length()));
                
                if (nome.endsWith(".xlsx") || nome.endsWith(".xls")) {
                    adicionarLog("  ℹ️ Formato Excel detectado - Importação mais confiável!");
                }
                
                lblStatus.setText("Arquivo " + tipo + " pronto para importação");
            }
        }
    }
    
    private void iniciarImportacao() {
        if (arquivoSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um arquivo CSV primeiro.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String tipoArquivo = arquivoSelecionado.getName().toLowerCase().endsWith(".csv") ? "CSV" : "Excel";
        
        int confirmacao = JOptionPane.showConfirmDialog(this,
            "Deseja iniciar a importação do arquivo " + tipoArquivo + " selecionado?\n\n" +
            "IMPORTANTE:\n" +
            "• Patrimônios serão importados mesmo sem responsável, sala ou estado\n" +
            "• Um relatório detalhado será gerado ao final\n" +
            "• Você poderá ajustar os dados posteriormente\n" +
            "• Esta operação pode demorar alguns minutos\n\n" +
            "Deseja continuar?",
            "Confirmar Importação",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirmacao != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Desabilitar controles durante a importação
        habilitarControles(false);
        btnCancelar.setVisible(true);
        btnCancelar.setEnabled(true);
        btnCancelar.setText("Cancelar Importação");
        limparLog();
        
        adicionarLog("=== INICIANDO IMPORTAÇÃO DE DADOS DO SUAP ===");
        adicionarLog("Arquivo: " + arquivoSelecionado.getName());
        adicionarLog("Opções selecionadas:");
        adicionarLog("  - Criar responsáveis: " + (chkCriarResponsaveis.isSelected() ? "SIM" : "NÃO"));
        adicionarLog("  - Criar setores: " + (chkCriarSetores.isSelected() ? "SIM" : "NÃO"));
        adicionarLog("  - Criar salas: " + (chkCriarSalas.isSelected() ? "SIM" : "NÃO"));
        adicionarLog("  - Atualizar existentes: " + (chkAtualizarExistentes.isSelected() ? "SIM" : "NÃO"));
        adicionarLog("  - Ignorar erros: " + (chkIgnorarErros.isSelected() ? "SIM" : "NÃO"));
        adicionarLog("");
        
        // Executar importação em thread separada
        workerAtual = new SwingWorker<RelatorioImportacao, String>() {
            @Override
            protected RelatorioImportacao doInBackground() throws Exception {
                SwingUtilities.invokeLater(() -> {
                    lblStatus.setText("Importando dados...");
                    progressBar.setIndeterminate(true);
                    progressBar.setString("Processando...");
                });
                
                publish("Iniciando processamento do arquivo...");
                
                // Criar callback para atualizações em tempo real
                ImportacaoCSV.ProgressCallback callback = new ImportacaoCSV.ProgressCallback() {
                    @Override
                    public void onProgress(int linhasProcessadas, String mensagem) {
                        // Verificar se foi cancelado
                        if (isCancelled()) {
                            publish("[AVISO] Cancelamento detectado na linha " + linhasProcessadas);
                            return;
                        }
                        publish("[PROGRESSO] " + mensagem);
                        SwingUtilities.invokeLater(() -> {
                            progressBar.setString("Linha " + linhasProcessadas);
                        });
                    }
                    
                    @Override
                    public void onError(String mensagem) {
                        publish("[ERRO] " + mensagem);
                    }
                    
                    @Override
                    public void onInfo(String mensagem) {
                        publish("[INFO] " + mensagem);
                    }
                };
                
                long inicio = System.currentTimeMillis();
                String caminho = arquivoSelecionado.getAbsolutePath();
                String nome = arquivoSelecionado.getName().toLowerCase();
                RelatorioImportacao relatorio;

                if (nome.endsWith(".csv")) {
                    relatorio = importacao.importarPatrimonios(caminho, callback);
                } else if (nome.endsWith(".xlsx") || nome.endsWith(".xls")) {
                    ImportacaoExcel importacaoExcel = new ImportacaoExcel();
                    relatorio = importacaoExcel.importarPatrimonios(caminho, callback);
                } else {
                    publish("[ERRO] Formato de arquivo não suportado: " + nome);
                    throw new IllegalArgumentException("Formato de arquivo não suportado");
                }
                
                // Verificar se foi cancelado antes de retornar
                if (isCancelled()) {
                    publish("[AVISO] Importação cancelada pelo usuário");
                    return relatorio; // Retorna o que foi processado até agora
                }
                
                long fim = System.currentTimeMillis();
                relatorio.setTempoExecucao((fim - inicio) / 1000.0);
                
                return relatorio;
            }
            
            @Override
            protected void process(java.util.List<String> chunks) {
                for (String mensagem : chunks) {
                    adicionarLog(mensagem);
                }
            }
            
            @Override
            protected void done() {
                btnCancelar.setVisible(false);
                
                try {
                    if (isCancelled()) {
                        // Importação foi cancelada
                        progressBar.setIndeterminate(false);
                        progressBar.setValue(0);
                        progressBar.setString("Cancelado");
                        
                        adicionarLog("");
                        adicionarLog("═══════════════════════════════════════════");
                        adicionarLog("⚠️  IMPORTAÇÃO CANCELADA PELO USUÁRIO");
                        adicionarLog("═══════════════════════════════════════════");
                        adicionarLog("");
                        
                        // Tentar obter relatório parcial
                        try {
                            RelatorioImportacao relatorioParcial = get();
                            if (relatorioParcial != null) {
                                adicionarLog("RELATÓRIO PARCIAL:");
                                adicionarLog(relatorioParcial.toString());
                                
                                lblStatus.setText("Importação cancelada - Dados parciais mantidos");
                                lblEstatisticas.setText(String.format(
                                    "PARCIAL - Processadas: %d | Inseridas: %d | Atualizadas: %d",
                                    relatorioParcial.getLinhasProcessadas(),
                                    relatorioParcial.getItensInseridos(),
                                    relatorioParcial.getItensAtualizados()
                                ));
                                
                                mostrarResumoImportacaoCancelada(relatorioParcial);
                            }
                        } catch (Exception ex) {
                            adicionarLog("Não foi possível gerar relatório parcial");
                        }
                        
                        lblStatus.setText("Importação cancelada pelo usuário");
                        
                    } else {
                        // Importação concluída normalmente
                        RelatorioImportacao relatorio = get();
                        
                        progressBar.setIndeterminate(false);
                        progressBar.setValue(100);
                        progressBar.setString("Concluído");
                        
                        adicionarLog("");
                        adicionarLog("=== RELATÓRIO DE IMPORTAÇÃO ===");
                        adicionarLog(relatorio.toString());
                        
                        lblStatus.setText("Importação concluída");
                        lblEstatisticas.setText(String.format(
                            "Processadas: %d | Inseridas: %d | Atualizadas: %d | Erros: %d",
                            relatorio.getLinhasProcessadas(),
                            relatorio.getItensInseridos(),
                            relatorio.getItensAtualizados(),
                            relatorio.getErros()
                        ));
                        
                        // Mostrar resumo em dialog
                        mostrarResumoImportacao(relatorio);
                    }
                    
                } catch (java.util.concurrent.CancellationException e) {
                    adicionarLog("⚠️ Importação cancelada");
                    lblStatus.setText("Importação cancelada pelo usuário");
                    progressBar.setString("Cancelado");
                    
                } catch (Exception e) {
                    adicionarLog("ERRO: " + e.getMessage());
                    lblStatus.setText("Erro durante a importação");
                    progressBar.setString("Erro");
                    
                    JOptionPane.showMessageDialog(ImportacaoCSVFrame.this,
                        "Erro durante a importação: " + e.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    habilitarControles(true);
                    workerAtual = null;
                }
            }
        };
        
        workerAtual.execute();
    }
    
    private void mostrarResumoImportacaoCancelada(RelatorioImportacao relatorio) {
        String mensagem = String.format(
            "⚠️ Importação cancelada pelo usuário!\n\n" +
            "Dados parcialmente importados:\n" +
            "• Linhas processadas: %d\n" +
            "• Itens inseridos: %d\n" +
            "• Itens atualizados: %d\n" +
            "• Erros encontrados: %d\n\n" +
            "IMPORTANTE:\n" +
            "• Os patrimônios já importados foram mantidos no banco\n" +
            "• Você pode reimportar o arquivo para completar\n" +
            "• Os itens já importados serão atualizados (não duplicados)\n\n" +
            "Deseja visualizar o relatório parcial?",
            relatorio.getLinhasProcessadas(),
            relatorio.getItensInseridos(),
            relatorio.getItensAtualizados(),
            relatorio.getErros()
        );
        
        int opcao = JOptionPane.showConfirmDialog(this,
            mensagem,
            "Importação Cancelada",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (opcao == JOptionPane.YES_OPTION) {
            // Gerar relatório de ajustes
            String relatorioAjustes = gerarRelatorioAjustes();
            mostrarRelatorioDetalhado(relatorio, relatorioAjustes);
        }
    }
    
    private void mostrarResumoImportacao(RelatorioImportacao relatorio) {
        // Gerar relatório de ajustes necessários
        String relatorioAjustes = gerarRelatorioAjustes();
        
        String mensagem = String.format(
            "Importação concluída com sucesso!\n\n" +
            "Resumo:\n" +
            "• Linhas processadas: %d\n" +
            "• Itens inseridos: %d\n" +
            "• Itens atualizados: %d\n" +
            "• Erros encontrados: %d\n" +
            "• Tempo de execução: %.1f segundos\n\n" +
            "%s" +
            "Deseja visualizar o relatório completo?",
            relatorio.getLinhasProcessadas(),
            relatorio.getItensInseridos(),
            relatorio.getItensAtualizados(),
            relatorio.getErros(),
            relatorio.getTempoExecucao(),
            relatorioAjustes.isEmpty() ? "" : "⚠️ Existem patrimônios que precisam de ajustes!\n\n"
        );
        
        int opcao = JOptionPane.showConfirmDialog(this,
            mensagem,
            "Importação Concluída",
            JOptionPane.YES_NO_OPTION,
            relatorioAjustes.isEmpty() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
        
        if (opcao == JOptionPane.YES_OPTION) {
            // Mostrar relatório detalhado em nova janela
            mostrarRelatorioDetalhado(relatorio, relatorioAjustes);
        }
    }
    
    private String gerarRelatorioAjustes() {
        StringBuilder relatorio = new StringBuilder();
        
        try {
            // Buscar patrimônios sem responsável
            Connection conn = DatabaseConnection.getConnection();
            
            // Patrimônios sem responsável
            String sqlSemResponsavel = 
                "SELECT COUNT(*) FROM patrimonio WHERE id_responsavel IS NULL";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sqlSemResponsavel);
            int semResponsavel = 0;
            if (rs.next()) {
                semResponsavel = rs.getInt(1);
            }
            rs.close();
            
            // Patrimônios sem sala
            String sqlSemSala = 
                "SELECT COUNT(*) FROM patrimonio WHERE id_sala IS NULL";
            rs = stmt.executeQuery(sqlSemSala);
            int semSala = 0;
            if (rs.next()) {
                semSala = rs.getInt(1);
            }
            rs.close();
            
            // Patrimônios sem estado de conservação
            String sqlSemEstado = 
                "SELECT COUNT(*) FROM patrimonio WHERE estado_conservacao IS NULL OR estado_conservacao = ''";
            rs = stmt.executeQuery(sqlSemEstado);
            int semEstado = 0;
            if (rs.next()) {
                semEstado = rs.getInt(1);
            }
            rs.close();
            
            stmt.close();
            conn.close();
            
            if (semResponsavel > 0 || semSala > 0 || semEstado > 0) {
                relatorio.append("Patrimônios que precisam de ajustes:\n");
                if (semResponsavel > 0) {
                    relatorio.append(String.format("• %d sem responsável\n", semResponsavel));
                }
                if (semSala > 0) {
                    relatorio.append(String.format("• %d sem sala\n", semSala));
                }
                if (semEstado > 0) {
                    relatorio.append(String.format("• %d sem estado de conservação\n", semEstado));
                }
            }
            
        } catch (Exception e) {
            System.err.println("Erro ao gerar relatório de ajustes: " + e.getMessage());
        }
        
        return relatorio.toString();
    }
    
    private void mostrarRelatorioDetalhado(RelatorioImportacao relatorio, String relatorioAjustes) {
        JDialog dialog = new JDialog(this, "Relatório Detalhado de Importação", true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);
        
        JTextArea areaRelatorio = new JTextArea();
        areaRelatorio.setEditable(false);
        areaRelatorio.setFont(new Font("Consolas", Font.PLAIN, 12));
        areaRelatorio.setBackground(Color.WHITE);
        areaRelatorio.setForeground(new Color(60, 60, 60));
        
        StringBuilder textoCompleto = new StringBuilder();
        textoCompleto.append("═══════════════════════════════════════════════════════════\n");
        textoCompleto.append("           RELATÓRIO DETALHADO DE IMPORTAÇÃO\n");
        textoCompleto.append("═══════════════════════════════════════════════════════════\n\n");
        
        textoCompleto.append("RESUMO GERAL\n");
        textoCompleto.append("───────────────────────────────────────────────────────────\n");
        textoCompleto.append(String.format("Linhas processadas:    %d\n", relatorio.getLinhasProcessadas()));
        textoCompleto.append(String.format("Itens inseridos:       %d\n", relatorio.getItensInseridos()));
        textoCompleto.append(String.format("Itens atualizados:     %d\n", relatorio.getItensAtualizados()));
        textoCompleto.append(String.format("Erros encontrados:     %d\n", relatorio.getErros()));
        textoCompleto.append(String.format("Tempo de execução:     %.1f segundos\n", relatorio.getTempoExecucao()));
        textoCompleto.append(String.format("Taxa de sucesso:       %.1f%%\n\n", 
            (relatorio.getLinhasProcessadas() - relatorio.getErros()) * 100.0 / relatorio.getLinhasProcessadas()));
        
        if (!relatorioAjustes.isEmpty()) {
            textoCompleto.append("⚠️  ATENÇÃO: AJUSTES NECESSÁRIOS\n");
            textoCompleto.append("───────────────────────────────────────────────────────────\n");
            textoCompleto.append(relatorioAjustes);
            textoCompleto.append("\n");
            
            // Adicionar lista detalhada de patrimônios que precisam ajuste
            textoCompleto.append(gerarListaPatrimoniosParaAjuste());
        }
        
        if (!relatorio.getListaErros().isEmpty()) {
            textoCompleto.append("ERROS ENCONTRADOS\n");
            textoCompleto.append("───────────────────────────────────────────────────────────\n");
            for (String erro : relatorio.getListaErros()) {
                textoCompleto.append("• " + erro + "\n");
            }
            textoCompleto.append("\n");
        }
        
        textoCompleto.append("RECOMENDAÇÕES\n");
        textoCompleto.append("───────────────────────────────────────────────────────────\n");
        textoCompleto.append("1. Revise os patrimônios sem responsável e atribua um\n");
        textoCompleto.append("2. Verifique os patrimônios sem sala e defina a localização\n");
        textoCompleto.append("3. Atualize o estado de conservação dos patrimônios\n");
        textoCompleto.append("4. Corrija os erros listados acima, se houver\n\n");
        
        textoCompleto.append("═══════════════════════════════════════════════════════════\n");
        textoCompleto.append("           FIM DO RELATÓRIO\n");
        textoCompleto.append("═══════════════════════════════════════════════════════════\n");
        
        areaRelatorio.setText(textoCompleto.toString());
        areaRelatorio.setCaretPosition(0);
        
        JScrollPane scrollPane = new JScrollPane(areaRelatorio);
        
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnExportar = ButtonStyleFactory.createPrimaryButton("Exportar Relatório");
        JButton btnFechar = ButtonStyleFactory.createSecondaryButton("Fechar");
        
        btnExportar.addActionListener(e -> exportarRelatorio(textoCompleto.toString()));
        btnFechar.addActionListener(e -> dialog.dispose());
        
        panelBotoes.add(btnExportar);
        panelBotoes.add(btnFechar);
        
        dialog.setLayout(new BorderLayout());
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(panelBotoes, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    private String gerarListaPatrimoniosParaAjuste() {
        StringBuilder lista = new StringBuilder();
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            
            // Listar patrimônios sem responsável (primeiros 20)
            lista.append("\nPATRIMÔNIOS SEM RESPONSÁVEL (primeiros 20):\n");
            String sql = "SELECT numero, descricao FROM patrimonio " +
                        "WHERE id_responsavel IS NULL LIMIT 20";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            int count = 0;
            while (rs.next()) {
                count++;
                lista.append(String.format("  %d. %s - %s\n", 
                    count, rs.getString("numero"), rs.getString("descricao")));
            }
            if (count == 0) {
                lista.append("  ✓ Nenhum patrimônio sem responsável\n");
            }
            rs.close();
            
            // Listar patrimônios sem sala (primeiros 20)
            lista.append("\nPATRIMÔNIOS SEM SALA (primeiros 20):\n");
            sql = "SELECT numero, descricao FROM patrimonio " +
                  "WHERE id_sala IS NULL LIMIT 20";
            rs = stmt.executeQuery(sql);
            count = 0;
            while (rs.next()) {
                count++;
                lista.append(String.format("  %d. %s - %s\n", 
                    count, rs.getString("numero"), rs.getString("descricao")));
            }
            if (count == 0) {
                lista.append("  ✓ Nenhum patrimônio sem sala\n");
            }
            rs.close();
            
            // Listar patrimônios sem estado (primeiros 20)
            lista.append("\nPATRIMÔNIOS SEM ESTADO DE CONSERVAÇÃO (primeiros 20):\n");
            sql = "SELECT numero, descricao FROM patrimonio " +
                  "WHERE estado_conservacao IS NULL OR estado_conservacao = '' LIMIT 20";
            rs = stmt.executeQuery(sql);
            count = 0;
            while (rs.next()) {
                count++;
                lista.append(String.format("  %d. %s - %s\n", 
                    count, rs.getString("numero"), rs.getString("descricao")));
            }
            if (count == 0) {
                lista.append("  ✓ Nenhum patrimônio sem estado de conservação\n");
            }
            rs.close();
            
            stmt.close();
            conn.close();
            
        } catch (Exception e) {
            lista.append("\nErro ao gerar lista detalhada: " + e.getMessage() + "\n");
        }
        
        return lista.toString();
    }
    
    private void exportarRelatorio(String conteudo) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar Relatório");
        fileChooser.setSelectedFile(new File("relatorio_importacao_" + 
            new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".txt"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                java.nio.file.Files.write(
                    fileChooser.getSelectedFile().toPath(), 
                    conteudo.getBytes(java.nio.charset.StandardCharsets.UTF_8)
                );
                JOptionPane.showMessageDialog(this,
                    "Relatório exportado com sucesso!",
                    "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Erro ao exportar relatório: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void limparLog() {
        areaLog.setText("");
        lblEstatisticas.setText(" ");
        progressBar.setValue(0);
        progressBar.setString("Aguardando...");
    }
    
    private void adicionarLog(String mensagem) {
        SwingUtilities.invokeLater(() -> {
            areaLog.append(mensagem + "\n");
            areaLog.setCaretPosition(areaLog.getDocument().getLength());
        });
    }
    
    private void habilitarControles(boolean habilitar) {
        btnSelecionarArquivo.setEnabled(habilitar);
        btnImportar.setEnabled(habilitar && arquivoSelecionado != null);
        btnCancelar.setVisible(!habilitar);
        chkCriarResponsaveis.setEnabled(habilitar);
        chkCriarSetores.setEnabled(habilitar);
        chkCriarSalas.setEnabled(habilitar);
        chkAtualizarExistentes.setEnabled(habilitar);
        chkIgnorarErros.setEnabled(habilitar);
    }
    
    private String formatarTamanhoArquivo(long bytes) {
        if (bytes < 1024) {
            return bytes + " bytes";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }
    
    private void aplicarEstiloModerno() {
        // Configurar cores do frame
        getContentPane().setBackground(new Color(245, 245, 245));
        
        // Estilizar campo de arquivo
        campoArquivo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        campoArquivo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(8, 10, 8, 10)
        ));
        campoArquivo.setBackground(Color.WHITE);
        
        // Estilizar checkboxes
        estilizarCheckBox(chkCriarResponsaveis);
        estilizarCheckBox(chkCriarSetores);
        estilizarCheckBox(chkCriarSalas);
        estilizarCheckBox(chkAtualizarExistentes);
        estilizarCheckBox(chkIgnorarErros);
        
        // Estilizar área de log
        areaLog.setFont(new Font("Consolas", Font.PLAIN, 11));
        areaLog.setBackground(new Color(40, 44, 52));
        areaLog.setForeground(new Color(171, 178, 191));
        areaLog.setCaretColor(new Color(171, 178, 191));
        areaLog.setSelectionColor(new Color(61, 96, 139));
        areaLog.setSelectedTextColor(Color.WHITE);
        
        // Estilizar barra de progresso
        progressBar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        progressBar.setForeground(new Color(52, 152, 219));
        progressBar.setBackground(new Color(236, 240, 241));
        progressBar.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));
        
        // Estilizar labels de status
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(new Color(60, 60, 60));
        lblEstatisticas.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblEstatisticas.setForeground(new Color(52, 73, 94));
    }
    
    private void estilizarCheckBox(JCheckBox checkBox) {
        checkBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        checkBox.setForeground(new Color(60, 60, 60));
        checkBox.setBackground(Color.WHITE);
        checkBox.setFocusPainted(false);
        checkBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
