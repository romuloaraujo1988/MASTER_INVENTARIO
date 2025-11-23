package com.inventario.view;

import com.inventario.dto.ConnectedDeviceDTO;
import com.inventario.util.MobileApiClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Frame para monitorar dispositivos mobile conectados EM TEMPO REAL
 * Usa a API REST do MobileConnectionController para mostrar apenas dispositivos ativos
 * 
 * @author Sistema de Inventário
 * @version 2.0.0
 */
public class MobileMonitorFrameV2 extends JFrame {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    private JTable tabelaConexoes;
    private DefaultTableModel modeloTabela;
    private JLabel lblTotalConexoes;
    private JLabel lblUsuariosUnicos;
    private JLabel lblUltimaAtualizacao;
    private JLabel lblStatusServidor;
    private JButton btnAtualizar;
    private JButton btnAutoRefresh;
    private JButton btnDesconectar;
    private JButton btnLimparInativos;
    private JButton btnDetalhes;
    private JButton btnConfigurarUrl;
    private JTextField txtServerUrl;
    private Timer autoRefreshTimer;
    private boolean autoRefreshAtivo = false;
    
    private MobileApiClient apiClient;
    private String serverUrl;
    
    public MobileMonitorFrameV2() {
        // Carregar URL do servidor
        this.serverUrl = carregarServerUrl();
        this.apiClient = new MobileApiClient(serverUrl);
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
        
        setTitle("Monitor de Dispositivos Mobile (Tempo Real) - Sistema de Inventário");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1400, 750);
        setLocationRelativeTo(null);
        
        // Testar conexão e carregar dados iniciais
        testarConexaoECarregarDados();
    }
    
    private String carregarServerUrl() {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("application.properties"));
            return props.getProperty("mobile.server.url", "http://localhost:8081/inventario");
        } catch (Exception e) {
            // Fallback para localhost:8081/inventario (porta e context path do servidor mobile)
            return "http://localhost:8081/inventario";
        }
    }
    
    private void initializeComponents() {
        // Modelo da tabela - COLUNAS ADAPTADAS PARA CONEXÕES EM TEMPO REAL
        String[] colunas = {
            "Device ID", "Usuário", "Modelo", "Android", "App", 
            "IP", "Hostname", "Conectado Em", "Última Atividade", 
            "Duração", "Requisições", "Status"
        };
        
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabelaConexoes = new JTable(modeloTabela);
        tabelaConexoes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaConexoes.setRowHeight(25);
        tabelaConexoes.getTableHeader().setReorderingAllowed(false);
        tabelaConexoes.setAutoCreateRowSorter(true);
        
        // Ajustar larguras das colunas
        tabelaConexoes.getColumnModel().getColumn(0).setPreferredWidth(100); // Device ID
        tabelaConexoes.getColumnModel().getColumn(1).setPreferredWidth(120); // Usuário
        tabelaConexoes.getColumnModel().getColumn(2).setPreferredWidth(150); // Modelo
        tabelaConexoes.getColumnModel().getColumn(3).setPreferredWidth(70);  // Android
        tabelaConexoes.getColumnModel().getColumn(4).setPreferredWidth(70);  // App
        tabelaConexoes.getColumnModel().getColumn(5).setPreferredWidth(120); // IP
        tabelaConexoes.getColumnModel().getColumn(6).setPreferredWidth(120); // Hostname
        tabelaConexoes.getColumnModel().getColumn(7).setPreferredWidth(130); // Conectado Em
        tabelaConexoes.getColumnModel().getColumn(8).setPreferredWidth(130); // Última Atividade
        tabelaConexoes.getColumnModel().getColumn(9).setPreferredWidth(100); // Duração
        tabelaConexoes.getColumnModel().getColumn(10).setPreferredWidth(80); // Requisições
        tabelaConexoes.getColumnModel().getColumn(11).setPreferredWidth(80); // Status
        
        // Renderizador personalizado
        tabelaConexoes.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    String status = (String) table.getValueAt(row, 11);
                    
                    if ("🟢 Ativo".equals(status)) {
                        c.setBackground(new Color(230, 255, 230)); // Verde claro
                    } else {
                        c.setBackground(new Color(255, 240, 230)); // Laranja claro
                    }
                } else {
                    c.setBackground(table.getSelectionBackground());
                }
                
                return c;
            }
        });
        
        // Labels de estatísticas
        lblTotalConexoes = new JLabel("Conectados: 0");
        lblTotalConexoes.setFont(new Font("Arial", Font.BOLD, 14));
        lblTotalConexoes.setForeground(new Color(0, 150, 0));
        
        lblUsuariosUnicos = new JLabel("Usuários: 0");
        lblUsuariosUnicos.setFont(new Font("Arial", Font.BOLD, 14));
        
        lblStatusServidor = new JLabel("🔴 Servidor: Desconhecido");
        lblStatusServidor.setFont(new Font("Arial", Font.BOLD, 14));
        lblStatusServidor.setForeground(Color.RED);
        
        lblUltimaAtualizacao = new JLabel("Última Atualização: Nunca");
        lblUltimaAtualizacao.setFont(new Font("Arial", Font.ITALIC, 12));
        lblUltimaAtualizacao.setForeground(Color.GRAY);
        
        // Campo de URL do servidor
        txtServerUrl = new JTextField(serverUrl, 30);
        txtServerUrl.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Botões
        btnAtualizar = new JButton("🔄 Atualizar");
        btnAtualizar.setFont(new Font("Arial", Font.BOLD, 12));
        btnAtualizar.setBackground(new Color(52, 152, 219));
        btnAtualizar.setForeground(Color.WHITE);
        btnAtualizar.setFocusPainted(false);
        btnAtualizar.setBorderPainted(false);
        
        btnAutoRefresh = new JButton("▶️ Auto Refresh (OFF)");
        btnAutoRefresh.setFont(new Font("Arial", Font.BOLD, 12));
        btnAutoRefresh.setBackground(new Color(46, 204, 113));
        btnAutoRefresh.setForeground(Color.WHITE);
        btnAutoRefresh.setFocusPainted(false);
        btnAutoRefresh.setBorderPainted(false);
        
        btnDetalhes = new JButton("📋 Ver Detalhes");
        btnDetalhes.setFont(new Font("Arial", Font.PLAIN, 12));
        
        btnDesconectar = new JButton("🔌 Desconectar");
        btnDesconectar.setFont(new Font("Arial", Font.PLAIN, 12));
        btnDesconectar.setForeground(Color.RED);
        
        btnLimparInativos = new JButton("🧹 Limpar Inativos");
        btnLimparInativos.setFont(new Font("Arial", Font.PLAIN, 12));
        
        btnConfigurarUrl = new JButton("💾 Salvar URL");
        btnConfigurarUrl.setFont(new Font("Arial", Font.PLAIN, 11));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Painel superior com configuração de URL
        JPanel painelConfig = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelConfig.setBorder(new EmptyBorder(5, 10, 5, 10));
        painelConfig.setBackground(new Color(240, 240, 240));
        painelConfig.add(new JLabel("URL do Servidor Mobile:"));
        painelConfig.add(txtServerUrl);
        painelConfig.add(btnConfigurarUrl);
        painelConfig.add(Box.createHorizontalStrut(20));
        painelConfig.add(lblStatusServidor);
        
        // Painel com estatísticas e controles
        JPanel painelControles = new JPanel(new BorderLayout());
        painelControles.setBorder(new EmptyBorder(10, 10, 10, 10));
        painelControles.setBackground(Color.WHITE);
        
        // Painel de estatísticas
        JPanel painelEstatisticas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelEstatisticas.setBackground(Color.WHITE);
        painelEstatisticas.add(lblTotalConexoes);
        painelEstatisticas.add(Box.createHorizontalStrut(15));
        painelEstatisticas.add(lblUsuariosUnicos);
        painelEstatisticas.add(Box.createHorizontalStrut(15));
        painelEstatisticas.add(lblUltimaAtualizacao);
        
        // Painel de botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        painelBotoes.setBackground(Color.WHITE);
        painelBotoes.add(btnAtualizar);
        painelBotoes.add(Box.createHorizontalStrut(5));
        painelBotoes.add(btnAutoRefresh);
        painelBotoes.add(Box.createHorizontalStrut(10));
        painelBotoes.add(btnDetalhes);
        painelBotoes.add(Box.createHorizontalStrut(5));
        painelBotoes.add(btnDesconectar);
        painelBotoes.add(Box.createHorizontalStrut(5));
        painelBotoes.add(btnLimparInativos);
        
        painelControles.add(painelEstatisticas, BorderLayout.WEST);
        painelControles.add(painelBotoes, BorderLayout.EAST);
        
        // Painel central com tabela
        JScrollPane scrollPane = new JScrollPane(tabelaConexoes);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Dispositivos Conectados em Tempo Real"));
        
        // Painel inferior com informações
        JPanel painelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER));
        painelInferior.setBorder(new EmptyBorder(5, 10, 10, 10));
        painelInferior.setBackground(Color.WHITE);
        
        JLabel lblInfo = new JLabel("💡 Mostra apenas dispositivos conectados AGORA. Atualização automática a cada 10 segundos.");
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 11));
        lblInfo.setForeground(Color.GRAY);
        painelInferior.add(lblInfo);
        
        // Adicionar painéis ao frame
        JPanel painelNorte = new JPanel(new BorderLayout());
        painelNorte.add(painelConfig, BorderLayout.NORTH);
        painelNorte.add(painelControles, BorderLayout.CENTER);
        
        add(painelNorte, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(painelInferior, BorderLayout.SOUTH);
    }
    
    private void setupEventListeners() {
        btnAtualizar.addActionListener(e -> atualizarDados());
        btnAutoRefresh.addActionListener(e -> toggleAutoRefresh());
        btnDetalhes.addActionListener(e -> mostrarDetalhes());
        btnDesconectar.addActionListener(e -> desconectarDispositivo());
        btnLimparInativos.addActionListener(e -> limparDispositivosInativos());
        btnConfigurarUrl.addActionListener(e -> salvarNovaUrl());
        
        // Fechar timer ao fechar a janela
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (autoRefreshTimer != null) {
                    autoRefreshTimer.cancel();
                }
            }
        });
    }
    
    private void testarConexaoECarregarDados() {
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return apiClient.testarConexao();
            }
            
            @Override
            protected void done() {
                try {
                    boolean conectado = get();
                    if (conectado) {
                        lblStatusServidor.setText("🟢 Servidor: Online");
                        lblStatusServidor.setForeground(new Color(0, 150, 0));
                        atualizarDados();
                    } else {
                        lblStatusServidor.setText("🔴 Servidor: Offline");
                        lblStatusServidor.setForeground(Color.RED);
                        mostrarErro("Não foi possível conectar ao servidor mobile.\n" +
                                  "Verifique se o servidor está rodando em: " + serverUrl);
                    }
                } catch (Exception e) {
                    lblStatusServidor.setText("🔴 Servidor: Erro");
                    lblStatusServidor.setForeground(Color.RED);
                    mostrarErro("Erro ao testar conexão: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private void salvarNovaUrl() {
        String novaUrl = txtServerUrl.getText().trim();
        
        if (novaUrl.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "URL não pode estar vazia!",
                "Erro",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Validar formato básico
        if (!novaUrl.startsWith("http://") && !novaUrl.startsWith("https://")) {
            JOptionPane.showMessageDialog(this,
                "URL deve começar com http:// ou https://",
                "Erro",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        this.serverUrl = novaUrl;
        this.apiClient = new MobileApiClient(serverUrl);
        
        // Salvar em properties (opcional)
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("application.properties"));
            props.setProperty("mobile.server.url", serverUrl);
            props.store(new java.io.FileOutputStream("application.properties"), 
                       "Updated by MobileMonitorFrameV2");
            
            JOptionPane.showMessageDialog(this,
                "URL salva com sucesso!",
                "Sucesso",
                JOptionPane.INFORMATION_MESSAGE);
            
            testarConexaoECarregarDados();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "URL atualizada, mas não foi possível salvar no arquivo.\n" +
                "A mudança será perdida ao reiniciar o aplicativo.",
                "Aviso",
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void toggleAutoRefresh() {
        if (autoRefreshAtivo) {
            // Parar auto refresh
            if (autoRefreshTimer != null) {
                autoRefreshTimer.cancel();
                autoRefreshTimer = null;
            }
            autoRefreshAtivo = false;
            btnAutoRefresh.setText("▶️ Auto Refresh (OFF)");
            btnAutoRefresh.setBackground(new Color(46, 204, 113));
        } else {
            // Iniciar auto refresh
            autoRefreshTimer = new Timer("MobileMonitorAutoRefresh", true);
            autoRefreshTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    SwingUtilities.invokeLater(() -> atualizarDados());
                }
            }, 0, 10000); // Atualizar a cada 10 segundos
            
            autoRefreshAtivo = true;
            btnAutoRefresh.setText("⏸️ Auto Refresh (ON)");
            btnAutoRefresh.setBackground(new Color(231, 76, 60));
        }
    }
    
    private void atualizarDados() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private List<ConnectedDeviceDTO> dispositivos;
            private Map<String, Object> stats;
            private Exception erro;
            
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    dispositivos = apiClient.buscarDispositivosConectados();
                    stats = apiClient.buscarEstatisticas();
                } catch (java.net.ConnectException e) {
                    erro = new Exception("Servidor mobile não está rodando");
                } catch (java.net.SocketTimeoutException e) {
                    erro = new Exception("Timeout ao conectar ao servidor");
                } catch (Exception e) {
                    erro = e;
                }
                return null;
            }
            
            @Override
            protected void done() {
                if (erro != null) {
                    lblStatusServidor.setText("🔴 Servidor: Offline");
                    lblStatusServidor.setForeground(Color.RED);
                    mostrarErro("Erro ao buscar dispositivos: " + erro.getMessage() + 
                              "\n\nVerifique se o servidor está rodando em: " + serverUrl);
                    return;
                }
                
                if (dispositivos != null) {
                    lblStatusServidor.setText("🟢 Servidor: Online");
                    lblStatusServidor.setForeground(new Color(0, 150, 0));
                    atualizarTabela(dispositivos);
                    atualizarEstatisticas(stats);
                    lblUltimaAtualizacao.setText("Última Atualização: " + 
                        LocalDateTime.now().format(DATE_FORMATTER));
                }
            }
        };
        
        worker.execute();
    }
    
    private void atualizarTabela(List<ConnectedDeviceDTO> dispositivos) {
        modeloTabela.setRowCount(0);
        
        for (ConnectedDeviceDTO device : dispositivos) {
            Object[] row = {
                device.getDeviceId() != null ? device.getDeviceId() : "N/A",
                device.getUsername() != null ? device.getUsername() : "N/A",
                device.getDeviceInfo() != null ? device.getDeviceInfo() : "N/A",
                device.getAndroidVersion() != null ? device.getAndroidVersion() : "N/A",
                device.getAppVersion() != null ? device.getAppVersion() : "N/A",
                device.getIpAddress() != null ? device.getIpAddress() : "N/A",
                device.getHostname() != null ? device.getHostname() : "N/A",
                device.getConnectedAt() != null ? device.getConnectedAt() : "N/A",
                device.getLastHeartbeat() != null ? device.getLastHeartbeat() : "N/A",
                device.getConnectionDuration() != null ? device.getConnectionDuration() : "N/A",
                device.getRequestCount(),
                device.isActive() ? "🟢 Ativo" : "🟡 Inativo"
            };
            
            modeloTabela.addRow(row);
        }
    }
    
    private void atualizarEstatisticas(Map<String, Object> stats) {
        if (stats != null) {
            int total = (int) stats.getOrDefault("totalConnections", 0);
            int usuarios = (int) stats.getOrDefault("uniqueUsers", 0);
            
            lblTotalConexoes.setText("Conectados: " + total);
            lblUsuariosUnicos.setText("Usuários: " + usuarios);
        }
    }
    
    private void mostrarDetalhes() {
        int selectedRow = tabelaConexoes.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Selecione um dispositivo para ver os detalhes.",
                "Aviso",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = tabelaConexoes.convertRowIndexToModel(selectedRow);
        String deviceId = (String) modeloTabela.getValueAt(modelRow, 0);
        
        SwingWorker<ConnectedDeviceDTO, Void> worker = new SwingWorker<ConnectedDeviceDTO, Void>() {
            @Override
            protected ConnectedDeviceDTO doInBackground() throws Exception {
                return apiClient.buscarDispositivo(deviceId);
            }
            
            @Override
            protected void done() {
                try {
                    ConnectedDeviceDTO device = get();
                    
                    if (device != null) {
                        StringBuilder detalhes = new StringBuilder();
                        detalhes.append("═══════════════════════════════════════\n");
                        detalhes.append("    DISPOSITIVO CONECTADO - DETALHES\n");
                        detalhes.append("═══════════════════════════════════════\n\n");
                        detalhes.append("IDENTIFICAÇÃO\n");
                        detalhes.append("  Device ID: ").append(device.getDeviceId()).append("\n");
                        detalhes.append("  Usuário: ").append(device.getUsername()).append("\n\n");
                        detalhes.append("DISPOSITIVO\n");
                        detalhes.append("  Modelo: ").append(device.getDeviceInfo()).append("\n");
                        detalhes.append("  Android: ").append(device.getAndroidVersion()).append("\n");
                        detalhes.append("  App: ").append(device.getAppVersion()).append("\n\n");
                        detalhes.append("REDE\n");
                        detalhes.append("  IP: ").append(device.getIpAddress()).append("\n");
                        detalhes.append("  Hostname: ").append(device.getHostname()).append("\n\n");
                        detalhes.append("CONEXÃO\n");
                        detalhes.append("  Conectado em: ").append(device.getConnectedAt()).append("\n");
                        detalhes.append("  Última atividade: ").append(device.getLastHeartbeat()).append("\n");
                        detalhes.append("  Duração: ").append(device.getConnectionDuration()).append("\n");
                        detalhes.append("  Requisições: ").append(device.getRequestCount()).append("\n");
                        detalhes.append("  Status: ").append(device.isActive() ? "🟢 Ativo" : "🟡 Inativo").append("\n");
                        
                        JTextArea textArea = new JTextArea(detalhes.toString());
                        textArea.setEditable(false);
                        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                        
                        JScrollPane scrollPane = new JScrollPane(textArea);
                        scrollPane.setPreferredSize(new Dimension(500, 400));
                        
                        JOptionPane.showMessageDialog(MobileMonitorFrameV2.this,
                            scrollPane,
                            "Detalhes - " + device.getDeviceInfo(),
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                    
                } catch (Exception e) {
                    mostrarErro("Erro ao buscar detalhes: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private void desconectarDispositivo() {
        int selectedRow = tabelaConexoes.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Selecione um dispositivo para desconectar.",
                "Aviso",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = tabelaConexoes.convertRowIndexToModel(selectedRow);
        String deviceId = (String) modeloTabela.getValueAt(modelRow, 0);
        String usuario = (String) modeloTabela.getValueAt(modelRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Deseja realmente desconectar o dispositivo do usuário " + usuario + "?\n" +
            "O dispositivo será removido da lista de conexões ativas.",
            "Confirmar Desconexão",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return apiClient.desconectarDispositivo(deviceId);
                }
                
                @Override
                protected void done() {
                    try {
                        boolean sucesso = get();
                        if (sucesso) {
                            JOptionPane.showMessageDialog(MobileMonitorFrameV2.this,
                                "Dispositivo desconectado com sucesso!",
                                "Sucesso",
                                JOptionPane.INFORMATION_MESSAGE);
                            atualizarDados();
                        } else {
                            mostrarErro("Não foi possível desconectar o dispositivo.");
                        }
                    } catch (Exception e) {
                        mostrarErro("Erro ao desconectar dispositivo: " + e.getMessage());
                    }
                }
            };
            
            worker.execute();
        }
    }
    
    private void limparDispositivosInativos() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Deseja limpar todos os dispositivos inativos?\n" +
            "Dispositivos sem atividade nos últimos 5 minutos serão removidos.",
            "Confirmar Limpeza",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
                @Override
                protected Integer doInBackground() throws Exception {
                    return apiClient.limparDispositivosInativos();
                }
                
                @Override
                protected void done() {
                    try {
                        int removidos = get();
                        JOptionPane.showMessageDialog(MobileMonitorFrameV2.this,
                            removidos + " dispositivo(s) inativo(s) removido(s).",
                            "Limpeza Concluída",
                            JOptionPane.INFORMATION_MESSAGE);
                        atualizarDados();
                    } catch (Exception e) {
                        mostrarErro("Erro ao limpar dispositivos inativos: " + e.getMessage());
                    }
                }
            };
            
            worker.execute();
        }
    }
    
    private void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
        lblUltimaAtualizacao.setText("Última Atualização: Erro - " + 
            LocalDateTime.now().format(DATE_FORMATTER));
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MobileMonitorFrameV2 frame = new MobileMonitorFrameV2();
            frame.setVisible(true);
        });
    }
}
