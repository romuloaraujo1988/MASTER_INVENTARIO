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
        
        // Botão de sincronização - estilo moderno com gradiente
        btnSincronizar = criarBotaoModerno("🔄 Sincronizar");
        btnSincronizar.setEnabled(false);
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
    
    /**
     * Cria um botão moderno com gradiente e ícone (mesmo estilo do dashboard)
     */
    private JButton criarBotaoModerno(String texto) {
        JButton button = new JButton() {
            private boolean isHovered = false;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Cor de fundo com gradiente
                Color baseColor = new Color(52, 152, 219);
                Color hoverColor = new Color(41, 128, 185);
                Color disabledColor = new Color(149, 165, 166);
                
                Color topColor;
                Color bottomColor;
                
                if (!isEnabled()) {
                    topColor = disabledColor;
                    bottomColor = disabledColor.darker();
                } else if (isHovered) {
                    topColor = hoverColor;
                    bottomColor = hoverColor.darker();
                } else {
                    topColor = baseColor;
                    bottomColor = baseColor.darker();
                }

                GradientPaint gradient = new GradientPaint(
                        0, 0, topColor,
                        0, getHeight(), bottomColor);
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // Borda sutil
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

                g2d.dispose();

                // Desenhar texto com ícone
                g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2d.setColor(Color.WHITE);
                
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(texto);
                int textX = (getWidth() - textWidth) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                
                // Sombra do texto
                g2d.setColor(new Color(0, 0, 0, 80));
                g2d.drawString(texto, textX + 1, textY + 1);
                
                // Texto principal
                g2d.setColor(Color.WHITE);
                g2d.drawString(texto, textX, textY);

                g2d.dispose();
            }
        };

        button.setPreferredSize(new Dimension(140, 32));
        button.setMinimumSize(new Dimension(120, 28));
        button.setMaximumSize(new Dimension(160, 36));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Efeito hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                try {
                    java.lang.reflect.Field field = button.getClass().getDeclaredField("isHovered");
                    field.setAccessible(true);
                    field.set(button, true);
                    button.repaint();
                } catch (Exception e) {
                    // Fallback silencioso
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                try {
                    java.lang.reflect.Field field = button.getClass().getDeclaredField("isHovered");
                    field.setAccessible(true);
                    field.set(button, false);
                    button.repaint();
                } catch (Exception e) {
                    // Fallback silencioso
                }
            }
        });
        
        return button;
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
            // Recriar botão com texto de progresso
            JButton novoBotao = criarBotaoModerno("⏳ Sincronizando...");
            novoBotao.setEnabled(false);
            
            // Substituir botão no painel
            Container parent = btnSincronizar.getParent();
            if (parent != null) {
                int index = -1;
                for (int i = 0; i < parent.getComponentCount(); i++) {
                    if (parent.getComponent(i) == btnSincronizar) {
                        index = i;
                        break;
                    }
                }
                
                if (index >= 0) {
                    // Copiar listeners
                    ActionListener[] listeners = btnSincronizar.getActionListeners();
                    parent.remove(index);
                    btnSincronizar = novoBotao;
                    for (ActionListener listener : listeners) {
                        btnSincronizar.addActionListener(listener);
                    }
                    parent.add(btnSincronizar, index);
                    parent.revalidate();
                    parent.repaint();
                }
            }
        });
    }
    
    /**
     * Restaura o botão de sincronização ao estado normal
     */
    public void restaurarBotaoSincronizar() {
        SwingUtilities.invokeLater(() -> {
            // Recriar botão com texto normal
            JButton novoBotao = criarBotaoModerno("🔄 Sincronizar");
            novoBotao.setEnabled(estadoAtual == OfflineManager.OfflineState.ONLINE);
            
            // Substituir botão no painel
            Container parent = btnSincronizar.getParent();
            if (parent != null) {
                int index = -1;
                for (int i = 0; i < parent.getComponentCount(); i++) {
                    if (parent.getComponent(i) == btnSincronizar) {
                        index = i;
                        break;
                    }
                }
                
                if (index >= 0) {
                    // Copiar listeners
                    ActionListener[] listeners = btnSincronizar.getActionListeners();
                    parent.remove(index);
                    btnSincronizar = novoBotao;
                    for (ActionListener listener : listeners) {
                        btnSincronizar.addActionListener(listener);
                    }
                    parent.add(btnSincronizar, index);
                    parent.revalidate();
                    parent.repaint();
                }
            }
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
