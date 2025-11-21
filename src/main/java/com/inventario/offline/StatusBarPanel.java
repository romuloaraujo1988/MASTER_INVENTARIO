package com.inventario.offline;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Painel de status para exibir informações de conectividade e sincronização
 * Exibe indicadores visuais, status de conexão e botão de sincronização
 */
public class StatusBarPanel extends JPanel {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    // Componentes visuais
    private JLabel lblIndicador;
    private JLabel lblStatus;
    private JLabel lblUltimaSincronizacao;
    private JLabel lblColetasPendentes;
    private JButton btnSincronizar;
    
    // Estado atual
    private OfflineManager.OfflineState estadoAtual;
    private int coletasPendentes = 0;
    private LocalDateTime ultimaSincronizacao;
    
    public StatusBarPanel() {
        initComponents();
        setupLayout();
        atualizarStatus(OfflineManager.OfflineState.INITIALIZING);
    }
    
    private void initComponents() {
        // Indicador visual (emoji/ícone)
        lblIndicador = new JLabel("🔄");
        lblIndicador.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        lblIndicador.setToolTipText("Status da conexão");
        
        // Label de status
        lblStatus = new JLabel("Inicializando...");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatus.setForeground(new Color(100, 100, 100));
        
        // Label de última sincronização
        lblUltimaSincronizacao = new JLabel("Última sincronização: Nunca");
        lblUltimaSincronizacao.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblUltimaSincronizacao.setForeground(new Color(120, 120, 120));
        
        // Label de coletas pendentes
        lblColetasPendentes = new JLabel("Coletas pendentes: 0");
        lblColetasPendentes.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblColetasPendentes.setForeground(new Color(120, 120, 120));
        
        // Botão de sincronização
        btnSincronizar = new JButton("Sincronizar Agora");
        btnSincronizar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnSincronizar.setFocusPainted(false);
        btnSincronizar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSincronizar.setEnabled(false);
        estilizarBotao(btnSincronizar);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 0));
        setBackground(new Color(44, 62, 80));
        setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        // Painel esquerdo - Indicador e status
        JPanel panelEsquerdo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelEsquerdo.setOpaque(false);
        panelEsquerdo.add(lblIndicador);
        panelEsquerdo.add(lblStatus);
        panelEsquerdo.add(createSeparator());
        panelEsquerdo.add(lblUltimaSincronizacao);
        
        // Painel direito - Coletas pendentes e botão
        JPanel panelDireito = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelDireito.setOpaque(false);
        panelDireito.add(lblColetasPendentes);
        panelDireito.add(btnSincronizar);
        
        add(panelEsquerdo, BorderLayout.WEST);
        add(panelDireito, BorderLayout.EAST);
    }
    
    private JLabel createSeparator() {
        JLabel separator = new JLabel("|");
        separator.setForeground(new Color(150, 150, 150));
        separator.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return separator;
    }
    
    private void estilizarBotao(JButton button) {
        button.setBackground(new Color(52, 152, 219));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(41, 128, 185), 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        
        // Efeito hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(new Color(41, 128, 185));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(52, 152, 219));
            }
        });
    }
    
    /**
     * Atualiza o status visual baseado no estado atual
     */
    public void atualizarStatus(OfflineManager.OfflineState estado) {
        this.estadoAtual = estado;
        
        SwingUtilities.invokeLater(() -> {
            switch (estado) {
                case ONLINE:
                    lblIndicador.setText("🟢");
                    lblIndicador.setToolTipText("Sistema ONLINE - Conectado ao servidor");
                    lblStatus.setText("ONLINE");
                    lblStatus.setForeground(new Color(39, 174, 96));
                    btnSincronizar.setEnabled(true);
                    break;
                    
                case OFFLINE:
                    lblIndicador.setText("🔴");
                    lblIndicador.setToolTipText("Sistema OFFLINE - Operando localmente");
                    lblStatus.setText("OFFLINE");
                    lblStatus.setForeground(new Color(231, 76, 60));
                    btnSincronizar.setEnabled(false);
                    break;
                    
                case SYNCING:
                    lblIndicador.setText("🔄");
                    lblIndicador.setToolTipText("Sincronizando dados...");
                    lblStatus.setText("SINCRONIZANDO");
                    lblStatus.setForeground(new Color(243, 156, 18));
                    btnSincronizar.setEnabled(false);
                    break;
                    
                case ERROR:
                    lblIndicador.setText("⚠️");
                    lblIndicador.setToolTipText("Erro no sistema");
                    lblStatus.setText("ERRO");
                    lblStatus.setForeground(new Color(192, 57, 43));
                    btnSincronizar.setEnabled(false);
                    break;
                    
                case INITIALIZING:
                default:
                    lblIndicador.setText("🟡");
                    lblIndicador.setToolTipText("Inicializando sistema...");
                    lblStatus.setText("INICIALIZANDO");
                    lblStatus.setForeground(new Color(149, 165, 166));
                    btnSincronizar.setEnabled(false);
                    break;
            }
        });
    }
    
    /**
     * Atualiza a data/hora da última sincronização
     */
    public void atualizarUltimaSincronizacao(LocalDateTime dataHora) {
        this.ultimaSincronizacao = dataHora;
        
        SwingUtilities.invokeLater(() -> {
            if (dataHora != null) {
                String dataFormatada = dataHora.format(DATE_FORMATTER);
                lblUltimaSincronizacao.setText("Última sincronização: " + dataFormatada);
                lblUltimaSincronizacao.setForeground(new Color(39, 174, 96));
            } else {
                lblUltimaSincronizacao.setText("Última sincronização: Nunca");
                lblUltimaSincronizacao.setForeground(new Color(120, 120, 120));
            }
        });
    }
    
    /**
     * Atualiza o contador de coletas pendentes
     */
    public void atualizarColetasPendentes(int quantidade) {
        this.coletasPendentes = quantidade;
        
        SwingUtilities.invokeLater(() -> {
            lblColetasPendentes.setText("Coletas pendentes: " + quantidade);
            
            if (quantidade > 0) {
                lblColetasPendentes.setForeground(new Color(243, 156, 18));
                lblColetasPendentes.setFont(new Font("Segoe UI", Font.BOLD, 11));
            } else {
                lblColetasPendentes.setForeground(new Color(120, 120, 120));
                lblColetasPendentes.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            }
        });
    }
    
    /**
     * Adiciona listener para o botão de sincronização
     */
    public void addSincronizarListener(ActionListener listener) {
        btnSincronizar.addActionListener(listener);
    }
    
    /**
     * Define se o botão de sincronização está habilitado
     */
    public void setBotaoSincronizarHabilitado(boolean habilitado) {
        SwingUtilities.invokeLater(() -> {
            btnSincronizar.setEnabled(habilitado);
        });
    }
    
    /**
     * Mostra indicador de sincronização em progresso
     */
    public void mostrarSincronizandoProgresso() {
        SwingUtilities.invokeLater(() -> {
            btnSincronizar.setText("Sincronizando...");
            btnSincronizar.setEnabled(false);
        });
    }
    
    /**
     * Restaura o botão de sincronização ao estado normal
     */
    public void restaurarBotaoSincronizar() {
        SwingUtilities.invokeLater(() -> {
            btnSincronizar.setText("Sincronizar Agora");
            btnSincronizar.setEnabled(estadoAtual == OfflineManager.OfflineState.ONLINE);
        });
    }
    
    // Getters
    public OfflineManager.OfflineState getEstadoAtual() {
        return estadoAtual;
    }
    
    public int getColetasPendentes() {
        return coletasPendentes;
    }
    
    public LocalDateTime getUltimaSincronizacao() {
        return ultimaSincronizacao;
    }
}
