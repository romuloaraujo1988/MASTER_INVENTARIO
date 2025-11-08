package com.inventario.view;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Frame para monitorar usuários mobile conectados
 * Exibe informações em tempo real sobre dispositivos conectados ao servidor mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class MobileMonitorFrame extends JFrame {
    
    private static final String API_BASE_URL = "http://localhost:8080/api/mobile/v1/connection";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    private JTable tabelaConexoes;
    private DefaultTableModel modeloTabela;
    private JLabel lblTotalConexoes;
    private JLabel lblUsuariosUnicos;
    private JLabel lblUltimaAtualizacao;
    private JButton btnAtualizar;
    private JButton btnAutoRefresh;
    private Timer autoRefreshTimer;
    private boolean autoRefreshAtivo = false;
    
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    
    public MobileMonitorFrame() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.httpClient = HttpClient.newHttpClient();
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
        
        setTitle("Monitor de Usuários Mobile - Sistema de Inventário");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        
        // Carregar dados iniciais
        atualizarDados();
    }
    
    private void initializeComponents() {
        // Modelo da tabela
        String[] colunas = {
            "Usuário", "IP", "Hostname", "Dispositivo", "Versão App", 
            "Conectado em", "Última Atividade", "Duração", "Status"
        };
        
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabela somente leitura
            }
        };
        
        tabelaConexoes = new JTable(modeloTabela);
        tabelaConexoes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaConexoes.setRowHeight(25);
        tabelaConexoes.getTableHeader().setReorderingAllowed(false);
        
        // Configurar renderizador para a coluna de status
        tabelaConexoes.getColumnModel().getColumn(8).setCellRenderer(new StatusCellRenderer());
        
        // Ajustar larguras das colunas
        tabelaConexoes.getColumnModel().getColumn(0).setPreferredWidth(100); // Usuário
        tabelaConexoes.getColumnModel().getColumn(1).setPreferredWidth(120); // IP
        tabelaConexoes.getColumnModel().getColumn(2).setPreferredWidth(150); // Hostname
        tabelaConexoes.getColumnModel().getColumn(3).setPreferredWidth(120); // Dispositivo
        tabelaConexoes.getColumnModel().getColumn(4).setPreferredWidth(80);  // Versão
        tabelaConexoes.getColumnModel().getColumn(5).setPreferredWidth(130); // Conectado em
        tabelaConexoes.getColumnModel().getColumn(6).setPreferredWidth(130); // Última atividade
        tabelaConexoes.getColumnModel().getColumn(7).setPreferredWidth(80);  // Duração
        tabelaConexoes.getColumnModel().getColumn(8).setPreferredWidth(80);  // Status
        
        // Labels de estatísticas
        lblTotalConexoes = new JLabel("Total de Conexões: 0");
        lblTotalConexoes.setFont(new Font("Arial", Font.BOLD, 14));
        
        lblUsuariosUnicos = new JLabel("Usuários Únicos: 0");
        lblUsuariosUnicos.setFont(new Font("Arial", Font.BOLD, 14));
        
        lblUltimaAtualizacao = new JLabel("Última Atualização: Nunca");
        lblUltimaAtualizacao.setFont(new Font("Arial", Font.ITALIC, 12));
        lblUltimaAtualizacao.setForeground(Color.GRAY);
        
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
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Painel superior com estatísticas e controles
        JPanel painelSuperior = new JPanel(new BorderLayout());
        painelSuperior.setBorder(new EmptyBorder(10, 10, 10, 10));
        painelSuperior.setBackground(Color.WHITE);
        
        // Painel de estatísticas
        JPanel painelEstatisticas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelEstatisticas.setBackground(Color.WHITE);
        painelEstatisticas.add(lblTotalConexoes);
        painelEstatisticas.add(Box.createHorizontalStrut(20));
        painelEstatisticas.add(lblUsuariosUnicos);
        painelEstatisticas.add(Box.createHorizontalStrut(20));
        painelEstatisticas.add(lblUltimaAtualizacao);
        
        // Painel de controles
        JPanel painelControles = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        painelControles.setBackground(Color.WHITE);
        painelControles.add(btnAtualizar);
        painelControles.add(Box.createHorizontalStrut(10));
        painelControles.add(btnAutoRefresh);
        
        painelSuperior.add(painelEstatisticas, BorderLayout.WEST);
        painelSuperior.add(painelControles, BorderLayout.EAST);
        
        // Painel central com tabela
        JScrollPane scrollPane = new JScrollPane(tabelaConexoes);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Conexões Mobile Ativas"));
        
        // Painel inferior com informações
        JPanel painelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER));
        painelInferior.setBorder(new EmptyBorder(5, 10, 10, 10));
        painelInferior.setBackground(Color.WHITE);
        
        JLabel lblInfo = new JLabel("💡 As conexões são atualizadas automaticamente. Conexões inativas por mais de 5 minutos são removidas.");
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 11));
        lblInfo.setForeground(Color.GRAY);
        painelInferior.add(lblInfo);
        
        add(painelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(painelInferior, BorderLayout.SOUTH);
    }
    
    private void setupEventListeners() {
        btnAtualizar.addActionListener(e -> atualizarDados());
        
        btnAutoRefresh.addActionListener(e -> toggleAutoRefresh());
        
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
            private Map<String, Object> dadosConexoes;
            private Exception erro;
            
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    dadosConexoes = buscarConexoesAtivas();
                } catch (Exception e) {
                    erro = e;
                }
                return null;
            }
            
            @Override
            protected void done() {
                if (erro != null) {
                    mostrarErro("Erro ao buscar conexões: " + erro.getMessage());
                    return;
                }
                
                if (dadosConexoes != null) {
                    atualizarTabela(dadosConexoes);
                    atualizarEstatisticas(dadosConexoes);
                    lblUltimaAtualizacao.setText("Última Atualização: " + 
                        LocalDateTime.now().format(DATE_FORMATTER));
                }
            }
        };
        
        worker.execute();
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> buscarConexoesAtivas() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/active"))
                .header("Content-Type", "application/json")
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
        } else {
            throw new IOException("Erro HTTP: " + response.statusCode() + " - " + response.body());
        }
    }
    
    @SuppressWarnings("unchecked")
    private void atualizarTabela(Map<String, Object> dados) {
        // Limpar tabela
        modeloTabela.setRowCount(0);
        
        if (dados.get("success").equals(true)) {
            List<Map<String, Object>> conexoes = (List<Map<String, Object>>) dados.get("connections");
            
            for (Map<String, Object> conexao : conexoes) {
                Object[] linha = {
                    conexao.get("username"),
                    conexao.get("ipAddress"),
                    conexao.get("hostname"),
                    conexao.get("deviceInfo") != null ? conexao.get("deviceInfo") : "N/A",
                    conexao.get("appVersion") != null ? conexao.get("appVersion") : "N/A",
                    formatarDataHora((String) conexao.get("connectedAt")),
                    formatarDataHora((String) conexao.get("lastHeartbeat")),
                    conexao.get("connectionDuration"),
                    conexao.get("isActive").equals(true) ? "Ativo" : "Inativo"
                };
                modeloTabela.addRow(linha);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void atualizarEstatisticas(Map<String, Object> dados) {
        if (dados.get("success").equals(true)) {
            Map<String, Object> stats = (Map<String, Object>) dados.get("stats");
            
            lblTotalConexoes.setText("Total de Conexões: " + stats.get("totalConnections"));
            lblUsuariosUnicos.setText("Usuários Únicos: " + stats.get("uniqueUsers"));
        }
    }
    
    private String formatarDataHora(String isoDateTime) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(isoDateTime);
            return dateTime.format(DATE_FORMATTER);
        } catch (Exception e) {
            return isoDateTime;
        }
    }
    
    private void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
        lblUltimaAtualizacao.setText("Última Atualização: Erro - " + 
            LocalDateTime.now().format(DATE_FORMATTER));
    }
    
    /**
     * Renderizador personalizado para a coluna de status
     */
    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected) {
                if ("Ativo".equals(value)) {
                    c.setBackground(new Color(212, 237, 218));
                    c.setForeground(new Color(21, 87, 36));
                } else {
                    c.setBackground(new Color(248, 215, 218));
                    c.setForeground(new Color(114, 28, 36));
                }
            }
            
            setHorizontalAlignment(SwingConstants.CENTER);
            return c;
        }
    }
}