package com.inventario.view;

import com.inventario.service.MobileServerManager;
import com.inventario.service.MobileServerManager.ServerStatus;
import com.inventario.service.MobileServerManager.ServerStatusListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Painel de gerenciamento do servidor mobile
 * Acessível apenas para administradores
 */
public class MobileServerPanel extends JPanel implements ServerStatusListener {
    
    private JLabel statusLabel;
    private JLabel statusIconLabel;
    private JTextArea infoTextArea;
    private JButton startButton;
    private JButton stopButton;
    private JButton restartButton;
    private JButton refreshButton;
    private JProgressBar progressBar;
    private JList<String> devicesList;
    private DefaultListModel<String> devicesListModel;
    private JLabel devicesCountLabel;
    
    public MobileServerPanel() {
        initComponents();
        MobileServerManager.addStatusListener(this);
        updateStatus();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Painel superior - Status
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.NORTH);
        
        // Painel central - Informações
        JPanel infoPanel = createInfoPanel();
        add(infoPanel, BorderLayout.CENTER);
        
        // Painel inferior - Botões
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Status do Servidor Mobile"));
        
        // Ícone e status
        JPanel statusInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        statusIconLabel = new JLabel();
        statusIconLabel.setFont(new Font("Dialog", Font.BOLD, 24));
        statusInfoPanel.add(statusIconLabel);
        
        statusLabel = new JLabel("Verificando...");
        statusLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        statusInfoPanel.add(statusLabel);
        
        panel.add(statusInfoPanel, BorderLayout.CENTER);
        
        // Barra de progresso
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);
        panel.add(progressBar, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Informações"));
        
        infoTextArea = new JTextArea(10, 40);
        infoTextArea.setEditable(false);
        infoTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        infoTextArea.setBackground(new Color(245, 245, 245));
        
        JScrollPane scrollPane = new JScrollPane(infoTextArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        startButton = new JButton("Iniciar Servidor");
        startButton.setIcon(UIManager.getIcon("FileView.computerIcon"));
        startButton.addActionListener(this::onStartServer);
        panel.add(startButton);
        
        stopButton = new JButton("Parar Servidor");
        stopButton.setIcon(UIManager.getIcon("OptionPane.errorIcon"));
        stopButton.addActionListener(this::onStopServer);
        panel.add(stopButton);
        
        restartButton = new JButton("Reiniciar Servidor");
        restartButton.setIcon(UIManager.getIcon("FileView.floppyDriveIcon"));
        restartButton.addActionListener(this::onRestartServer);
        panel.add(restartButton);
        
        refreshButton = new JButton("Atualizar Status");
        refreshButton.setIcon(UIManager.getIcon("FileView.fileIcon"));
        refreshButton.addActionListener(e -> updateStatus());
        panel.add(refreshButton);
        
        return panel;
    }
    
    private void onStartServer(ActionEvent e) {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Deseja iniciar o servidor mobile?\n\n" +
            "Isso pode levar alguns minutos.",
            "Confirmar Inicialização",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            setButtonsEnabled(false);
            progressBar.setIndeterminate(true);
            progressBar.setVisible(true);
            
            MobileServerManager.startServer().thenAccept(success -> {
                SwingUtilities.invokeLater(() -> {
                    progressBar.setVisible(false);
                    setButtonsEnabled(true);
                    updateStatus();
                    
                    if (success) {
                        JOptionPane.showMessageDialog(
                            this,
                            "Servidor mobile iniciado com sucesso!\n\n" +
                            "URL: http://localhost:8081/inventario/api/mobile",
                            "Sucesso",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                            this,
                            "Erro ao iniciar servidor mobile.\n" +
                            "Verifique os logs para mais detalhes.",
                            "Erro",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                });
            });
        }
    }
    
    private void onStopServer(ActionEvent e) {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Deseja parar o servidor mobile?\n\n" +
            "Isso desconectará todos os dispositivos móveis.",
            "Confirmar Parada",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            setButtonsEnabled(false);
            progressBar.setIndeterminate(true);
            progressBar.setVisible(true);
            
            MobileServerManager.stopServer().thenAccept(success -> {
                SwingUtilities.invokeLater(() -> {
                    progressBar.setVisible(false);
                    setButtonsEnabled(true);
                    updateStatus();
                    
                    if (success) {
                        JOptionPane.showMessageDialog(
                            this,
                            "Servidor mobile parado com sucesso!",
                            "Sucesso",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                            this,
                            "Erro ao parar servidor mobile.\n" +
                            "Verifique os logs para mais detalhes.",
                            "Erro",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                });
            });
        }
    }
    
    private void onRestartServer(ActionEvent e) {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Deseja reiniciar o servidor mobile?\n\n" +
            "Isso desconectará temporariamente todos os dispositivos móveis.",
            "Confirmar Reinicialização",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            setButtonsEnabled(false);
            progressBar.setIndeterminate(true);
            progressBar.setVisible(true);
            
            MobileServerManager.restartServer().thenAccept(success -> {
                SwingUtilities.invokeLater(() -> {
                    progressBar.setVisible(false);
                    setButtonsEnabled(true);
                    updateStatus();
                    
                    if (success) {
                        JOptionPane.showMessageDialog(
                            this,
                            "Servidor mobile reiniciado com sucesso!",
                            "Sucesso",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                            this,
                            "Erro ao reiniciar servidor mobile.\n" +
                            "Verifique os logs para mais detalhes.",
                            "Erro",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                });
            });
        }
    }
    
    private void updateStatus() {
        ServerStatus status = MobileServerManager.getServerStatus();
        updateStatusDisplay(status);
        updateInfoDisplay();
        updateButtons(status);
    }
    
    private void updateStatusDisplay(ServerStatus status) {
        switch (status) {
            case RUNNING:
                statusIconLabel.setText("✓");
                statusIconLabel.setForeground(new Color(0, 150, 0));
                statusLabel.setText("Servidor Rodando");
                statusLabel.setForeground(new Color(0, 150, 0));
                break;
                
            case STOPPED:
                statusIconLabel.setText("✗");
                statusIconLabel.setForeground(Color.RED);
                statusLabel.setText("Servidor Parado");
                statusLabel.setForeground(Color.RED);
                break;
                
            case STARTING:
                statusIconLabel.setText("⟳");
                statusIconLabel.setForeground(Color.ORANGE);
                statusLabel.setText("Iniciando...");
                statusLabel.setForeground(Color.ORANGE);
                break;
                
            case STOPPING:
                statusIconLabel.setText("⟳");
                statusIconLabel.setForeground(Color.ORANGE);
                statusLabel.setText("Parando...");
                statusLabel.setForeground(Color.ORANGE);
                break;
                
            case ERROR:
                statusIconLabel.setText("⚠");
                statusIconLabel.setForeground(Color.RED);
                statusLabel.setText("Erro");
                statusLabel.setForeground(Color.RED);
                break;
        }
    }
    
    private void updateInfoDisplay() {
        StringBuilder info = new StringBuilder();
        
        // Informações do servidor
        info.append("=== SERVIDOR MOBILE ===\n\n");
        info.append(MobileServerManager.getServerInfo());
        
        // Informações de rede
        info.append("\n=== ENDEREÇOS DE REDE ===\n\n");
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isUp() && !ni.isLoopback()) {
                    Enumeration<InetAddress> addresses = ni.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();
                        if (addr.getAddress().length == 4) { // IPv4
                            info.append(ni.getDisplayName()).append(": ");
                            info.append("http://").append(addr.getHostAddress());
                            info.append(":8081/inventario/api/mobile\n");
                        }
                    }
                }
            }
        } catch (Exception e) {
            info.append("Erro ao obter endereços de rede\n");
        }
        
        info.append("\n=== INSTRUÇÕES ===\n\n");
        info.append("1. Certifique-se de que o smartphone está na mesma rede\n");
        info.append("2. Configure o endereço IP no aplicativo mobile\n");
        info.append("3. Use a porta 8081 e o contexto /inventario\n");
        info.append("4. Exemplo: http://192.168.1.100:8081/inventario\n");
        
        infoTextArea.setText(info.toString());
        infoTextArea.setCaretPosition(0);
    }
    
    private void updateButtons(ServerStatus status) {
        boolean isRunning = (status == ServerStatus.RUNNING);
        boolean isTransitioning = (status == ServerStatus.STARTING || status == ServerStatus.STOPPING);
        
        startButton.setEnabled(!isRunning && !isTransitioning);
        stopButton.setEnabled(isRunning && !isTransitioning);
        restartButton.setEnabled(isRunning && !isTransitioning);
        refreshButton.setEnabled(!isTransitioning);
    }
    
    private void setButtonsEnabled(boolean enabled) {
        startButton.setEnabled(enabled);
        stopButton.setEnabled(enabled);
        restartButton.setEnabled(enabled);
        refreshButton.setEnabled(enabled);
    }
    
    @Override
    public void onStatusChanged(ServerStatus status, String message) {
        SwingUtilities.invokeLater(() -> {
            updateStatus();
            
            // Mostrar mensagem se for erro
            if (status == ServerStatus.ERROR) {
                JOptionPane.showMessageDialog(
                    this,
                    message,
                    "Erro no Servidor",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
    
    /**
     * Cria e exibe uma janela com o painel de gerenciamento
     */
    public static void showDialog(Component parent) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), 
            "Gerenciamento do Servidor Mobile", true);
        
        MobileServerPanel panel = new MobileServerPanel();
        dialog.add(panel);
        
        dialog.setSize(700, 500);
        dialog.setLocationRelativeTo(parent);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
    }
}
