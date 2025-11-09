package com.inventario.view;

import com.inventario.model.DispositivoMobile;
import com.inventario.service.DispositivoMobileService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Frame para monitorar dispositivos mobile conectados
 * Exibe informações em tempo real sobre dispositivos conectados ao sistema
 * 
 * @author Sistema de Inventário
 * @version 2.0.0
 */
public class MobileMonitorFrame extends JFrame {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    private JTable tabelaConexoes;
    private DefaultTableModel modeloTabela;
    private JLabel lblTotalConexoes;
    private JLabel lblUsuariosUnicos;
    private JLabel lblUltimaAtualizacao;
    private JLabel lblAtivos;
    private JButton btnAtualizar;
    private JButton btnAutoRefresh;
    private JButton btnBloquear;
    private JButton btnDesbloquear;
    private JButton btnDetalhes;
    private Timer autoRefreshTimer;
    private boolean autoRefreshAtivo = false;
    
    private final DispositivoMobileService dispositivoService;
    
    public MobileMonitorFrame() {
        this.dispositivoService = new DispositivoMobileService();
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
        
        setTitle("Monitor de Dispositivos Mobile - Sistema de Inventário");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        
        // Carregar dados iniciais
        atualizarDados();
    }
    
    private void initializeComponents() {
        // Modelo da tabela
        String[] colunas = {
            "ID", "Usuário", "Modelo", "Fabricante", "Android", "App", 
            "IP", "Registro", "Última Conexão", "Status", "Ativo"
        };
        
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 10) return Boolean.class; // Coluna Ativo
                return String.class;
            }
        };
        
        tabelaConexoes = new JTable(modeloTabela);
        tabelaConexoes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaConexoes.setRowHeight(25);
        tabelaConexoes.getTableHeader().setReorderingAllowed(false);
        tabelaConexoes.setAutoCreateRowSorter(true);
        
        // Ajustar larguras das colunas
        tabelaConexoes.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        tabelaConexoes.getColumnModel().getColumn(1).setPreferredWidth(150); // Usuário
        tabelaConexoes.getColumnModel().getColumn(2).setPreferredWidth(120); // Modelo
        tabelaConexoes.getColumnModel().getColumn(3).setPreferredWidth(100); // Fabricante
        tabelaConexoes.getColumnModel().getColumn(4).setPreferredWidth(70);  // Android
        tabelaConexoes.getColumnModel().getColumn(5).setPreferredWidth(70);  // App
        tabelaConexoes.getColumnModel().getColumn(6).setPreferredWidth(120); // IP
        tabelaConexoes.getColumnModel().getColumn(7).setPreferredWidth(130); // Registro
        tabelaConexoes.getColumnModel().getColumn(8).setPreferredWidth(130); // Última Conexão
        tabelaConexoes.getColumnModel().getColumn(9).setPreferredWidth(100); // Status
        tabelaConexoes.getColumnModel().getColumn(10).setPreferredWidth(60); // Ativo
        
        // Renderizador personalizado
        tabelaConexoes.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    String status = (String) table.getValueAt(row, 9);
                    Boolean ativo = (Boolean) table.getValueAt(row, 10);
                    
                    if (!ativo) {
                        c.setBackground(new Color(255, 230, 230)); // Vermelho claro
                    } else if ("APROVADO".equals(status)) {
                        c.setBackground(new Color(230, 255, 230)); // Verde claro
                    } else if ("BLOQUEADO".equals(status)) {
                        c.setBackground(new Color(255, 200, 200)); // Vermelho
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                } else {
                    c.setBackground(table.getSelectionBackground());
                }
                
                return c;
            }
        });
        
        // Labels de estatísticas
        lblTotalConexoes = new JLabel("Total: 0");
        lblTotalConexoes.setFont(new Font("Arial", Font.BOLD, 14));
        
        lblAtivos = new JLabel("Ativos: 0");
        lblAtivos.setFont(new Font("Arial", Font.BOLD, 14));
        lblAtivos.setForeground(new Color(0, 150, 0));
        
        lblUsuariosUnicos = new JLabel("Usuários: 0");
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
        
        btnDetalhes = new JButton("Ver Detalhes");
        btnDetalhes.setFont(new Font("Arial", Font.PLAIN, 12));
        
        btnBloquear = new JButton("Bloquear");
        btnBloquear.setFont(new Font("Arial", Font.PLAIN, 12));
        btnBloquear.setForeground(Color.RED);
        
        btnDesbloquear = new JButton("Desbloquear");
        btnDesbloquear.setFont(new Font("Arial", Font.PLAIN, 12));
        btnDesbloquear.setForeground(new Color(0, 150, 0));
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
        painelEstatisticas.add(Box.createHorizontalStrut(15));
        painelEstatisticas.add(lblAtivos);
        painelEstatisticas.add(Box.createHorizontalStrut(15));
        painelEstatisticas.add(lblUsuariosUnicos);
        painelEstatisticas.add(Box.createHorizontalStrut(15));
        painelEstatisticas.add(lblUltimaAtualizacao);
        
        // Painel de controles
        JPanel painelControles = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        painelControles.setBackground(Color.WHITE);
        painelControles.add(btnAtualizar);
        painelControles.add(Box.createHorizontalStrut(5));
        painelControles.add(btnAutoRefresh);
        painelControles.add(Box.createHorizontalStrut(10));
        painelControles.add(btnDetalhes);
        painelControles.add(Box.createHorizontalStrut(5));
        painelControles.add(btnBloquear);
        painelControles.add(Box.createHorizontalStrut(5));
        painelControles.add(btnDesbloquear);
        
        painelSuperior.add(painelEstatisticas, BorderLayout.WEST);
        painelSuperior.add(painelControles, BorderLayout.EAST);
        
        // Painel central com tabela
        JScrollPane scrollPane = new JScrollPane(tabelaConexoes);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Dispositivos Mobile Registrados"));
        
        // Painel inferior com informações
        JPanel painelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER));
        painelInferior.setBorder(new EmptyBorder(5, 10, 10, 10));
        painelInferior.setBackground(Color.WHITE);
        
        JLabel lblInfo = new JLabel("💡 Dispositivos são registrados automaticamente no primeiro login. Use Auto Refresh para monitorar em tempo real.");
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
        btnDetalhes.addActionListener(e -> mostrarDetalhes());
        btnBloquear.addActionListener(e -> bloquearDispositivo());
        btnDesbloquear.addActionListener(e -> desbloquearDispositivo());
        
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
            private List<DispositivoMobile> dispositivos;
            private Exception erro;
            
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    dispositivos = dispositivoService.listarTodos();
                } catch (Exception e) {
                    erro = e;
                }
                return null;
            }
            
            @Override
            protected void done() {
                if (erro != null) {
                    mostrarErro("Erro ao buscar dispositivos: " + erro.getMessage());
                    return;
                }
                
                if (dispositivos != null) {
                    atualizarTabela(dispositivos);
                    atualizarEstatisticas(dispositivos);
                    lblUltimaAtualizacao.setText("Última Atualização: " + 
                        LocalDateTime.now().format(DATE_FORMATTER));
                }
            }
        };
        
        worker.execute();
    }
    
    private void atualizarTabela(List<DispositivoMobile> dispositivos) {
        modeloTabela.setRowCount(0);
        
        for (DispositivoMobile dispositivo : dispositivos) {
            Object[] row = {
                dispositivo.getId(),
                dispositivo.getNomeUsuario() != null ? dispositivo.getNomeUsuario() : "N/A",
                dispositivo.getModelo() != null ? dispositivo.getModelo() : "N/A",
                dispositivo.getFabricante() != null ? dispositivo.getFabricante() : "N/A",
                dispositivo.getVersaoAndroid() != null ? dispositivo.getVersaoAndroid() : "N/A",
                dispositivo.getVersaoApp() != null ? dispositivo.getVersaoApp() : "N/A",
                dispositivo.getEnderecoIp() != null ? dispositivo.getEnderecoIp() : "N/A",
                dispositivo.getDataRegistro() != null ? dispositivo.getDataRegistro().format(DATE_FORMATTER) : "N/A",
                dispositivo.getDataUltimaConexao() != null ? dispositivo.getDataUltimaConexao().format(DATE_FORMATTER) : "Nunca",
                dispositivo.getStatus().name(),
                dispositivo.getAtivo()
            };
            
            modeloTabela.addRow(row);
        }
    }
    
    private void atualizarEstatisticas(List<DispositivoMobile> dispositivos) {
        int total = dispositivos.size();
        int ativos = (int) dispositivos.stream()
            .filter(d -> Boolean.TRUE.equals(d.getAtivo()))
            .count();
        int usuariosUnicos = (int) dispositivos.stream()
            .map(DispositivoMobile::getIdUsuario)
            .distinct()
            .count();
        
        lblTotalConexoes.setText("Total: " + total);
        lblAtivos.setText("Ativos: " + ativos);
        lblUsuariosUnicos.setText("Usuários: " + usuariosUnicos);
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
        Integer id = (Integer) modeloTabela.getValueAt(modelRow, 0);
        
        try {
            var dispositivo = dispositivoService.buscarPorId(id);
            
            if (dispositivo.isPresent()) {
                DispositivoMobile d = dispositivo.get();
                
                StringBuilder detalhes = new StringBuilder();
                detalhes.append("═══════════════════════════════════════\n");
                detalhes.append("         DETALHES DO DISPOSITIVO\n");
                detalhes.append("═══════════════════════════════════════\n\n");
                detalhes.append("ID: ").append(d.getId()).append("\n");
                detalhes.append("Device ID: ").append(d.getDeviceId()).append("\n\n");
                detalhes.append("USUÁRIO\n");
                detalhes.append("  Nome: ").append(d.getNomeUsuario()).append("\n\n");
                detalhes.append("DISPOSITIVO\n");
                detalhes.append("  Modelo: ").append(d.getModelo()).append("\n");
                detalhes.append("  Fabricante: ").append(d.getFabricante()).append("\n");
                detalhes.append("  Android: ").append(d.getVersaoAndroid()).append("\n");
                detalhes.append("  App: ").append(d.getVersaoApp()).append("\n\n");
                detalhes.append("REDE\n");
                detalhes.append("  IP: ").append(d.getEnderecoIp()).append("\n");
                detalhes.append("  MAC: ").append(d.getEnderecoMac() != null ? d.getEnderecoMac() : "N/A").append("\n\n");
                detalhes.append("STATUS\n");
                detalhes.append("  Status: ").append(d.getStatus().getDescricao()).append("\n");
                detalhes.append("  Ativo: ").append(d.getAtivo() ? "Sim" : "Não").append("\n\n");
                detalhes.append("HISTÓRICO\n");
                detalhes.append("  Registro: ").append(d.getDataRegistro().format(DATE_FORMATTER)).append("\n");
                detalhes.append("  Última Conexão: ").append(
                    d.getDataUltimaConexao() != null ? d.getDataUltimaConexao().format(DATE_FORMATTER) : "Nunca"
                ).append("\n");
                detalhes.append("  Última Sincronização: ").append(
                    d.getDataUltimaSincronizacao() != null ? d.getDataUltimaSincronizacao().format(DATE_FORMATTER) : "Nunca"
                ).append("\n");
                
                if (d.getDataUltimaConexao() != null) {
                    long minutosDesdeConexao = ChronoUnit.MINUTES.between(d.getDataUltimaConexao(), LocalDateTime.now());
                    detalhes.append("  Tempo desde última conexão: ").append(minutosDesdeConexao).append(" minutos\n");
                }
                
                if (d.getObservacoes() != null && !d.getObservacoes().isEmpty()) {
                    detalhes.append("\nOBSERVAÇÕES\n");
                    detalhes.append("  ").append(d.getObservacoes()).append("\n");
                }
                
                JTextArea textArea = new JTextArea(detalhes.toString());
                textArea.setEditable(false);
                textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(500, 500));
                
                JOptionPane.showMessageDialog(this,
                    scrollPane,
                    "Detalhes do Dispositivo - " + d.getModelo(),
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (SQLException e) {
            mostrarErro("Erro ao buscar detalhes: " + e.getMessage());
        }
    }
    
    private void bloquearDispositivo() {
        int selectedRow = tabelaConexoes.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Selecione um dispositivo para bloquear.",
                "Aviso",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = tabelaConexoes.convertRowIndexToModel(selectedRow);
        Integer id = (Integer) modeloTabela.getValueAt(modelRow, 0);
        String usuario = (String) modeloTabela.getValueAt(modelRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Deseja realmente bloquear o dispositivo do usuário " + usuario + "?\n" +
            "O dispositivo não poderá mais sincronizar dados.",
            "Confirmar Bloqueio",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                dispositivoService.bloquearDispositivo(id);
                JOptionPane.showMessageDialog(this,
                    "Dispositivo bloqueado com sucesso!",
                    "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);
                atualizarDados();
            } catch (SQLException e) {
                mostrarErro("Erro ao bloquear dispositivo: " + e.getMessage());
            }
        }
    }
    
    private void desbloquearDispositivo() {
        int selectedRow = tabelaConexoes.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Selecione um dispositivo para desbloquear.",
                "Aviso",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = tabelaConexoes.convertRowIndexToModel(selectedRow);
        Integer id = (Integer) modeloTabela.getValueAt(modelRow, 0);
        String usuario = (String) modeloTabela.getValueAt(modelRow, 1);
        
        try {
            dispositivoService.desbloquearDispositivo(id);
            JOptionPane.showMessageDialog(this,
                "Dispositivo desbloqueado com sucesso!",
                "Sucesso",
                JOptionPane.INFORMATION_MESSAGE);
            atualizarDados();
        } catch (SQLException e) {
            mostrarErro("Erro ao desbloquear dispositivo: " + e.getMessage());
        }
    }
    
    private void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
        lblUltimaAtualizacao.setText("Última Atualização: Erro - " + 
            LocalDateTime.now().format(DATE_FORMATTER));
    }
}