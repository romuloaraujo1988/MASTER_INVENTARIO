package com.inventario.sihcp.view;

import com.inventario.sihcp.util.ConfiguracaoBancoUtil;

import javax.swing.*;

/**
 * Teste manual do ConfiguracaoBancoDialog
 * Execute este main para testar o dialog de configuração
 */
public class ConfiguracaoBancoDialogTest {
    
    public static void main(String[] args) {
        // Configurar Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            // Criar frame de teste
            JFrame testFrame = new JFrame("Teste de Configuração");
            testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            testFrame.setSize(400, 300);
            testFrame.setLocationRelativeTo(null);
            
            // Painel com botões de teste
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            // Botão 1: Abrir configuração
            JButton btnAbrir = new JButton("Abrir Configuração");
            btnAbrir.addActionListener(e -> {
                boolean confirmado = ConfiguracaoBancoUtil.abrirDialogConfiguracao(testFrame);
                System.out.println("Configuração confirmada: " + confirmado);
            });
            
            // Botão 2: Verificar configuração
            JButton btnVerificar = new JButton("Verificar Configuração");
            btnVerificar.addActionListener(e -> {
                boolean valida = ConfiguracaoBancoUtil.temConfiguracaoValida();
                String msg = valida ? "Configuração válida encontrada!" : "Nenhuma configuração válida";
                JOptionPane.showMessageDialog(testFrame, msg);
            });
            
            // Botão 3: Mostrar informações
            JButton btnInfo = new JButton("Mostrar Informações");
            btnInfo.addActionListener(e -> {
                ConfiguracaoBancoUtil.mostrarInfoConfiguracao(testFrame);
            });
            
            // Botão 4: Testar conexão
            JButton btnTestar = new JButton("Testar Conexão");
            btnTestar.addActionListener(e -> {
                boolean conectado = ConfiguracaoBancoUtil.testarConexao();
                String msg = conectado ? "✓ Conexão bem-sucedida!" : "✗ Falha na conexão";
                int tipo = conectado ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE;
                JOptionPane.showMessageDialog(testFrame, msg, "Teste de Conexão", tipo);
            });
            
            // Botão 5: Verificar ou configurar
            JButton btnVerificarOuConfigurar = new JButton("Verificar ou Configurar");
            btnVerificarOuConfigurar.addActionListener(e -> {
                boolean ok = ConfiguracaoBancoUtil.verificarOuConfigurar(testFrame);
                System.out.println("Configuração OK: " + ok);
            });
            
            // Adicionar botões ao painel
            panel.add(btnAbrir);
            panel.add(Box.createVerticalStrut(10));
            panel.add(btnVerificar);
            panel.add(Box.createVerticalStrut(10));
            panel.add(btnInfo);
            panel.add(Box.createVerticalStrut(10));
            panel.add(btnTestar);
            panel.add(Box.createVerticalStrut(10));
            panel.add(btnVerificarOuConfigurar);
            
            testFrame.add(panel);
            testFrame.setVisible(true);
            
            System.out.println("=== Teste de Configuração de Banco ===");
            System.out.println("Clique nos botões para testar as funcionalidades");
            System.out.println("Configuração atual: " + ConfiguracaoBancoUtil.getInfoConfiguracao());
        });
    }
}
