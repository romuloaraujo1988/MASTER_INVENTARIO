package com.inventario.analytics.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Card visual para exibição de KPIs no dashboard.
 * 
 * Exibe título, valor principal, descrição e indicador de variação.
 */
public class KPICard extends JPanel {
    
    private final JLabel labelValor;
    private final JLabel labelVariacao;
    
    /**
     * Cria um card de KPI.
     * 
     * @param titulo Título do KPI
     * @param valorInicial Valor inicial a exibir
     * @param descricao Descrição do KPI
     * @param cor Cor de destaque do card
     * @param onClick Ação ao clicar no card (pode ser null)
     */
    public KPICard(String titulo, String valorInicial, String descricao, Color cor, Runnable onClick) {
        
        setLayout(new BorderLayout(5, 5));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);
        setCursor(onClick != null ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
        
        // Borda colorida à esquerda
        JPanel bordaColorida = new JPanel();
        bordaColorida.setPreferredSize(new Dimension(5, 0));
        bordaColorida.setBackground(cor);
        add(bordaColorida, BorderLayout.WEST);
        
        // Conteúdo central
        JPanel conteudo = new JPanel();
        conteudo.setLayout(new BoxLayout(conteudo, BoxLayout.Y_AXIS));
        conteudo.setOpaque(false);
        
        // Título
        JLabel labelTitulo = new JLabel(titulo);
        labelTitulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        labelTitulo.setForeground(new Color(100, 100, 100));
        labelTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        conteudo.add(labelTitulo);
        
        conteudo.add(Box.createVerticalStrut(10));
        
        // Painel de valor e variação
        JPanel painelValor = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        painelValor.setOpaque(false);
        painelValor.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Valor principal
        labelValor = new JLabel(valorInicial);
        labelValor.setFont(new Font("SansSerif", Font.BOLD, 32));
        labelValor.setForeground(cor);
        painelValor.add(labelValor);
        
        // Indicador de variação
        labelVariacao = new JLabel("");
        labelVariacao.setFont(new Font("SansSerif", Font.PLAIN, 14));
        painelValor.add(labelVariacao);
        
        conteudo.add(painelValor);
        
        conteudo.add(Box.createVerticalStrut(5));
        
        // Descrição
        JLabel labelDescricao = new JLabel(descricao);
        labelDescricao.setFont(new Font("SansSerif", Font.PLAIN, 11));
        labelDescricao.setForeground(new Color(150, 150, 150));
        labelDescricao.setAlignmentX(Component.LEFT_ALIGNMENT);
        conteudo.add(labelDescricao);
        
        add(conteudo, BorderLayout.CENTER);
        
        // Efeito hover e clique
        if (onClick != null) {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(new Color(248, 249, 250));
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(Color.WHITE);
                }
                
                @Override
                public void mouseClicked(MouseEvent e) {
                    onClick.run();
                }
            });
        }
    }
    
    /**
     * Atualiza o valor exibido no card.
     */
    public void setValor(String valor) {
        labelValor.setText(valor);
    }
    
    /**
     * Define o indicador de variação.
     * 
     * @param variacao Valor da variação (positivo = aumento, negativo = diminuição)
     * @param menorMelhor Se true, diminuição é positiva (verde), aumento é negativo (vermelho)
     */
    public void setVariacao(Double variacao, boolean menorMelhor) {
        if (variacao == null || variacao == 0) {
            labelVariacao.setText("");
            return;
        }
        
        boolean melhorou = menorMelhor ? variacao < 0 : variacao > 0;
        String seta = variacao > 0 ? "↑" : "↓";
        String texto = String.format("%s %.1f%%", seta, Math.abs(variacao));
        
        labelVariacao.setText(texto);
        labelVariacao.setForeground(melhorou ? new Color(40, 167, 69) : new Color(220, 53, 69));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Sombra sutil
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(0, 0, 0, 20));
        g2d.fillRoundRect(2, 2, getWidth() - 2, getHeight() - 2, 8, 8);
        g2d.dispose();
    }
}
