package com.inventario.view;

import com.inventario.offline.OfflineModeManager;
import com.inventario.offline.OfflineModeListener;
import com.inventario.service.SyncStatusManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Painel de barra de status para exibir informações de conexão e sincronização
 * 
 * @author Sistema de Inventário IFMT
 * @version 1.0.0
 */
public class StatusBarPanel extends JPanel implements OfflineModeListener {
    
    private static final Color ONLINE_COLOR = new Color(46, 204, 113);
    private static final Color OFFLINE_COLOR = new Color(243, 156, 18);
    private static final Color SYNCING_COLOR = new Color(52, 152, 219);
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    
    private final OfflineModeManager offlineModeManager;
    
    // Componentes
    private JLabel lblStatus;
    private JLabel lblLastSync;
    private JLabel lblPendingCount;
    private JButton btnSync;
    
    // Listener externo para sincronização (delegado ao MainFrame)
    private java.awt.event.ActionListener syncListener;
    
    public StatusBarPanel() {
        this.offlineModeManager = OfflineModeManager.getInstance();
        
        initComponents();
        setupLayout();
        updateStatus();
        
        // Registrar listener
        offlineModeManager.addListener(this);
    }
    
    private void initComponents() {
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)),
            new EmptyBorder(5, 10, 5, 10)
        ));
        
        // Label de status
        lblStatus = new JLabel();
        lblStatus.setFont(new Font("Arial", Font.BOLD, 12));
        
        // Label de última sincronização
        lblLastSync = new JLabel();
        lblLastSync.setFont(new Font("Arial", Font.PLAIN, 11));
        lblLastSync.setForeground(new Color(127, 140, 141));
        
        // Label de pendências
        lblPendingCount = new JLabel();
        lblPendingCount.setFont(new Font("Arial", Font.PLAIN, 11));
        lblPendingCount.setForeground(new Color(127, 140, 141));
        lblPendingCount.setVisible(false);
        
        // Botão de sincronização
        btnSync = new JButton("🔄 Sincronizar");
        btnSync.setFont(new Font("Arial", Font.PLAIN, 11));
        btnSync.setFocusPainted(false);
        btnSync.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSync.setToolTipText("Sincronizar dados com o servidor");
        btnSync.addActionListener(e -> sincronizarAgora());
    }

    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Painel esquerdo (status)
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(lblStatus);
        leftPanel.add(new JSeparator(SwingConstants.VERTICAL));
        leftPanel.add(lblLastSync);
        
        // Painel direito (pendências e botão)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(lblPendingCount);
        rightPanel.add(btnSync);
        
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
    }
    
    /**
     * Atualiza o status da barra
     */
    public void updateStatus() {
        boolean offline = offlineModeManager.isModoOffline();
        boolean dadosSincronizados = offlineModeManager.isDadosSincronizados();
        
        if (offline) {
            // Modo offline
            lblStatus.setText("🟡 MODO OFFLINE");
            lblStatus.setForeground(OFFLINE_COLOR);
            lblStatus.setToolTipText("Sistema operando em modo offline com dados locais");
            
            if (dadosSincronizados) {
                lblLastSync.setText("Dados locais disponíveis");
            } else {
                lblLastSync.setText("⚠️ Dados não sincronizados");
                lblLastSync.setForeground(Color.RED);
            }
            
            btnSync.setEnabled(false);
            btnSync.setToolTipText("Conecte-se ao servidor para sincronizar");
            
        } else {
            // Modo online
            lblStatus.setText("🟢 ONLINE");
            lblStatus.setForeground(ONLINE_COLOR);
            lblStatus.setToolTipText("Sistema conectado ao servidor");
            
            // Atualizar última sincronização
            atualizarUltimaSincronizacao();
            
            btnSync.setEnabled(true);
            btnSync.setToolTipText("Sincronizar dados com o servidor");
        }
        
        // Atualizar contador de pendências
        atualizarContadorPendencias();
    }
    
    /**
     * Atualiza a label de última sincronização
     */
    private void atualizarUltimaSincronizacao() {
        try {
            SyncStatusManager syncManager = SyncStatusManager.getInstance();
            LocalDateTime lastSync = syncManager.getLastSyncTime();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            if (lastSync != null) {
                lblLastSync.setText("Última sync: " + lastSync.format(formatter));
                
                // Verificar se dados estão desatualizados
                if (syncManager.isDataOutdated()) {
                    lblLastSync.setForeground(OFFLINE_COLOR);
                    lblLastSync.setToolTipText("⚠️ Dados desatualizados (>7 dias). Sincronize novamente.");
                } else {
                    lblLastSync.setForeground(new Color(127, 140, 141));
                    lblLastSync.setToolTipText("Dados atualizados");
                }
            } else {
                lblLastSync.setText("Nunca sincronizado");
                lblLastSync.setForeground(OFFLINE_COLOR);
                lblLastSync.setToolTipText("⚠️ Nenhuma sincronização realizada ainda");
            }
        } catch (Exception e) {
            lblLastSync.setText("Erro ao verificar sincronização");
            lblLastSync.setForeground(new Color(127, 140, 141));
            System.err.println("Erro ao atualizar última sincronização: " + e.getMessage());
        }
    }
    
    /**
     * Atualiza o contador de coletas pendentes
     */
    private void atualizarContadorPendencias() {
        try {
            // Buscar do SyncStatusManager
            SyncStatusManager syncManager = SyncStatusManager.getInstance();
            int pendentes = syncManager.getPendingCount();
            
            if (pendentes > 0) {
                lblPendingCount.setText(String.format("📤 %d coleta%s pendente%s", 
                    pendentes, 
                    pendentes > 1 ? "s" : "",
                    pendentes > 1 ? "s" : ""));
                lblPendingCount.setVisible(true);
                lblPendingCount.setForeground(OFFLINE_COLOR);
                lblPendingCount.setToolTipText(String.format("%d coleta%s aguardando sincronização com o servidor", 
                    pendentes, pendentes > 1 ? "s" : ""));
            } else {
                lblPendingCount.setVisible(false);
            }
        } catch (Exception e) {
            // Em caso de erro, ocultar o contador
            lblPendingCount.setVisible(false);
            System.err.println("Erro ao atualizar contador de pendências: " + e.getMessage());
        }
    }
    
    /**
     * Inicia sincronização
     * Delega para o listener externo (MainFrame) que possui acesso ao OfflineManager
     */
    private void sincronizarAgora() {
        if (syncListener != null) {
            // Delegar para o MainFrame que possui a lógica de sincronização
            syncListener.actionPerformed(new java.awt.event.ActionEvent(
                btnSync, java.awt.event.ActionEvent.ACTION_PERFORMED, "sync"));
        } else {
            // Fallback: mostrar mensagem se não houver listener configurado
            JOptionPane.showMessageDialog(
                this,
                "Sincronização não configurada.\nUse o menu Sistema > Sincronizar.",
                "Aviso",
                JOptionPane.WARNING_MESSAGE
            );
        }
    }
    
    /**
     * Adiciona listener para o botão de sincronização
     * Permite que o MainFrame gerencie a sincronização real
     * 
     * @param listener ActionListener que será chamado ao clicar em sincronizar
     */
    public void addSincronizarListener(java.awt.event.ActionListener listener) {
        this.syncListener = listener;
    }
    
    /**
     * Atualiza o status visual para "sincronizando"
     * Chamado pelo MainFrame durante a sincronização
     */
    public void mostrarSincronizando() {
        lblStatus.setText("🔄 SINCRONIZANDO");
        lblStatus.setForeground(SYNCING_COLOR);
        btnSync.setEnabled(false);
    }
    
    /**
     * Atualiza a última sincronização manualmente
     * Chamado pelo MainFrame após sincronização bem-sucedida
     * 
     * @param timestamp Data/hora da sincronização
     */
    public void atualizarUltimaSincronizacao(java.time.LocalDateTime timestamp) {
        if (timestamp != null) {
            java.time.format.DateTimeFormatter formatter = 
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            lblLastSync.setText("Última sync: " + timestamp.format(formatter));
            lblLastSync.setForeground(new Color(127, 140, 141));
        }
    }
    
    /**
     * Atualiza o contador de coletas pendentes
     * Chamado pelo MainFrame quando o status muda
     * 
     * @param count Número de coletas pendentes
     */
    public void atualizarColetasPendentes(int count) {
        if (count > 0) {
            lblPendingCount.setText(String.format("📤 %d coleta%s pendente%s", 
                count, count > 1 ? "s" : "", count > 1 ? "s" : ""));
            lblPendingCount.setVisible(true);
            lblPendingCount.setForeground(OFFLINE_COLOR);
        } else {
            lblPendingCount.setVisible(false);
        }
    }
    
    /**
     * Atualiza o status visual baseado no estado do OfflineManager
     * 
     * @param state Estado atual (ONLINE, OFFLINE, etc)
     */
    public void atualizarStatus(com.inventario.offline.OfflineManager.OfflineState state) {
        switch (state) {
            case ONLINE:
                lblStatus.setText("🟢 ONLINE");
                lblStatus.setForeground(ONLINE_COLOR);
                btnSync.setEnabled(true);
                break;
            case OFFLINE:
                lblStatus.setText("🟡 MODO OFFLINE");
                lblStatus.setForeground(OFFLINE_COLOR);
                btnSync.setEnabled(false);
                break;
            default:
                lblStatus.setText("⚪ DESCONHECIDO");
                lblStatus.setForeground(Color.GRAY);
        }
    }
    
    @Override
    public void onModoOfflineAtivado() {
        SwingUtilities.invokeLater(this::updateStatus);
    }
    
    @Override
    public void onModoOnlineRestaurado() {
        SwingUtilities.invokeLater(this::updateStatus);
    }
}
