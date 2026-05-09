package com.inventario.sihcp.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * Dialog modal para exibir o histórico de coletas de um patrimônio.
 * 
 * Encapsula o HistoricoColetaPanel em um diálogo modal, permitindo
 * visualização, filtros e exportação do histórico de coletas.
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class HistoricoColetaDialog extends JDialog {
    
    private static final long serialVersionUID = 1L;
    
    private final HistoricoColetaPanel painelHistorico;
    
    /**
     * Construtor básico com ID do patrimônio
     * 
     * @param parent Frame pai
     * @param idPatrimonio ID do patrimônio
     */
    public HistoricoColetaDialog(JFrame parent, int idPatrimonio) {
        super(parent, "Histórico de Coletas", true);
        
        this.painelHistorico = new HistoricoColetaPanel(idPatrimonio);
        
        initComponents();
        setupDialog();
    }
    
    /**
     * Construtor completo com dados do patrimônio
     * 
     * @param parent Frame pai
     * @param idPatrimonio ID do patrimônio
     * @param numeroPatrimonio Número do patrimônio
     * @param descricaoPatrimonio Descrição do patrimônio
     */
    public HistoricoColetaDialog(JFrame parent, int idPatrimonio, 
                                  String numeroPatrimonio, String descricaoPatrimonio) {
        super(parent, "Histórico de Coletas", true);
        
        this.painelHistorico = new HistoricoColetaPanel(
            idPatrimonio, numeroPatrimonio, descricaoPatrimonio);
        
        initComponents();
        setupDialog();
    }
    
    /**
     * Inicializa os componentes do diálogo
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        add(painelHistorico, BorderLayout.CENTER);
    }
    
    /**
     * Configura as propriedades do diálogo
     */
    private void setupDialog() {
        // Tamanho e posição
        setSize(1200, 700);
        setMinimumSize(new Dimension(900, 500));
        setLocationRelativeTo(getParent());
        
        // Comportamento ao fechar
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        // Listener para limpeza de recursos
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }
    
    /**
     * Recarrega o histórico (útil após alterações externas)
     */
    public void recarregar() {
        if (painelHistorico != null) {
            painelHistorico.recarregar();
        }
    }
    
    /**
     * Atualiza os dados do patrimônio exibidos
     * 
     * @param numeroPatrimonio Número do patrimônio
     * @param descricaoPatrimonio Descrição do patrimônio
     */
    public void setDadosPatrimonio(String numeroPatrimonio, String descricaoPatrimonio) {
        if (painelHistorico != null) {
            painelHistorico.setDadosPatrimonio(numeroPatrimonio, descricaoPatrimonio);
        }
    }
}
