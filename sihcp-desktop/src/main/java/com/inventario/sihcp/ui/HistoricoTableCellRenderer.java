package com.inventario.sihcp.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Renderer customizado para células da tabela de histórico.
 * 
 * Aplica formatação visual para:
 * - Coleta mais recente (negrito)
 * - Mudanças de localização (fundo amarelo)
 * - Mudanças de estado (fundo vermelho/verde conforme melhora/piora)
 * - Ícones para tipos de mudança
 * 
 * @author Sistema Inventário
 * @version 1.0
 */
public class HistoricoTableCellRenderer extends DefaultTableCellRenderer {
    
    private static final long serialVersionUID = 1L;
    
    // Cores para destacar mudanças
    private static final Color COR_MUDANCA_LOCALIZACAO = new Color(255, 255, 200); // Amarelo claro
    private static final Color COR_MUDANCA_ESTADO_MELHORA = new Color(200, 255, 200); // Verde claro
    private static final Color COR_MUDANCA_ESTADO_PIORA = new Color(255, 200, 200); // Vermelho claro
    private static final Color COR_PRIMEIRA_COLETA = new Color(230, 240, 255); // Azul muito claro
    private static final Color COR_PADRAO = Color.WHITE;
    private static final Color COR_ALTERNADA = new Color(245, 245, 245); // Cinza muito claro
    
    // Ícones (podem ser substituídos por ícones reais)
    private static final String ICONE_LOCALIZACAO = "📍";
    private static final String ICONE_ESTADO_MELHORA = "⬆";
    private static final String ICONE_ESTADO_PIORA = "⬇";
    private static final String ICONE_PRIMEIRA = "⭐";
    
    // Formatador de data
    private final SimpleDateFormat dateFormat;
    
    /**
     * Construtor padrão
     */
    public HistoricoTableCellRenderer() {
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        setOpaque(true);
    }
    
    @Override
    public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {
        
        // Obter componente padrão
        Component component = super.getTableCellRendererComponent(
            table, value, isSelected, hasFocus, row, column
        );
        
        // Cast para JLabel para aplicar formatações
        if (component instanceof JLabel) {
            JLabel label = (JLabel) component;
            
            // Obter modelo da tabela
            HistoricoTableModel model = null;
            if (table.getModel() instanceof HistoricoTableModel) {
                model = (HistoricoTableModel) table.getModel();
            }
            
            // Aplicar formatações se modelo disponível
            if (model != null) {
                aplicarFormatacao(label, model, row, column, isSelected);
            }
            
            // Formatar valor conforme tipo
            formatarValor(label, value, column);
        }
        
        return component;
    }
    
    /**
     * Aplica formatação visual baseada no contexto da célula
     */
    private void aplicarFormatacao(
            JLabel label,
            HistoricoTableModel model,
            int row,
            int column,
            boolean isSelected) {
        
        // Se célula está selecionada, manter cor de seleção
        if (isSelected) {
            return;
        }
        
        // Cor de fundo padrão (alternada)
        Color corFundo = (row % 2 == 0) ? COR_PADRAO : COR_ALTERNADA;
        
        // Verificar se é a primeira coleta (mais recente)
        boolean isPrimeira = model.isPrimeiraColeta(row);
        if (isPrimeira) {
            corFundo = COR_PRIMEIRA_COLETA;
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            
            // Adicionar ícone na primeira coluna
            if (column == HistoricoTableModel.COL_DATA) {
                String texto = label.getText();
                label.setText(ICONE_PRIMEIRA + " " + texto);
            }
        }
        
        // Verificar mudança de localização
        boolean temMudancaLoc = model.temMudancaLocalizacao(row);
        if (temMudancaLoc && column == HistoricoTableModel.COL_LOCALIZACAO) {
            corFundo = COR_MUDANCA_LOCALIZACAO;
            
            // Adicionar ícone e tooltip
            String texto = label.getText();
            label.setText(ICONE_LOCALIZACAO + " " + texto);
            
            String locAnterior = model.getLocalizacaoAnterior(row);
            if (locAnterior != null) {
                label.setToolTipText(
                    "<html><b>Mudança de Localização</b><br>" +
                    "Anterior: " + locAnterior + "<br>" +
                    "Atual: " + texto.replace(ICONE_LOCALIZACAO + " ", "") +
                    "</html>"
                );
            }
        }
        
        // Verificar mudança de estado
        boolean temMudancaEst = model.temMudancaEstado(row);
        if (temMudancaEst && column == HistoricoTableModel.COL_ESTADO) {
            String estadoAtual = label.getText();
            String estadoAnterior = model.getEstadoAnterior(row);
            
            // Determinar se é melhora ou piora
            boolean isMelhora = isMelhoraEstado(estadoAnterior, estadoAtual);
            corFundo = isMelhora ? COR_MUDANCA_ESTADO_MELHORA : COR_MUDANCA_ESTADO_PIORA;
            
            // Adicionar ícone
            String icone = isMelhora ? ICONE_ESTADO_MELHORA : ICONE_ESTADO_PIORA;
            label.setText(icone + " " + estadoAtual);
            
            // Tooltip
            if (estadoAnterior != null) {
                label.setToolTipText(
                    "<html><b>Mudança de Estado</b><br>" +
                    "Anterior: " + estadoAnterior + "<br>" +
                    "Atual: " + estadoAtual + "<br>" +
                    (isMelhora ? "<font color='green'>Melhora</font>" : "<font color='red'>Piora</font>") +
                    "</html>"
                );
            }
        }
        
        // Aplicar cor de fundo
        label.setBackground(corFundo);
        
        // Tooltip para observações longas
        if (column == HistoricoTableModel.COL_OBSERVACOES) {
            String texto = label.getText();
            if (texto != null && texto.endsWith("...")) {
                // Buscar texto completo do modelo
                var coleta = model.getColetaAt(row);
                if (coleta != null && coleta.getObservacoes() != null) {
                    label.setToolTipText(
                        "<html><b>Observações:</b><br>" +
                        coleta.getObservacoes().replace("\n", "<br>") +
                        "</html>"
                    );
                }
            }
        }
    }
    
    /**
     * Formata o valor conforme o tipo da coluna
     */
    private void formatarValor(JLabel label, Object value, int column) {
        if (value == null) {
            label.setText("");
            return;
        }
        
        // Formatar data
        if (column == HistoricoTableModel.COL_DATA && value instanceof Date) {
            label.setText(dateFormat.format((Date) value));
        }
        
        // Centralizar algumas colunas
        if (column == HistoricoTableModel.COL_ESTADO) {
            label.setHorizontalAlignment(SwingConstants.CENTER);
        } else {
            label.setHorizontalAlignment(SwingConstants.LEFT);
        }
    }
    
    /**
     * Determina se mudança de estado é uma melhora
     * 
     * Ordem de qualidade: BOM > REGULAR > RUIM
     * 
     * @param estadoAnterior Estado anterior
     * @param estadoAtual Estado atual
     * @return true se é melhora, false se é piora
     */
    private boolean isMelhoraEstado(String estadoAnterior, String estadoAtual) {
        if (estadoAnterior == null || estadoAtual == null) {
            return false;
        }
        
        int valorAnterior = getValorEstado(estadoAnterior);
        int valorAtual = getValorEstado(estadoAtual);
        
        return valorAtual > valorAnterior;
    }
    
    /**
     * Converte estado para valor numérico para comparação
     * 
     * @param estado Estado do patrimônio
     * @return Valor numérico (maior = melhor)
     */
    private int getValorEstado(String estado) {
        if (estado == null) {
            return 0;
        }
        
        String estadoUpper = estado.toUpperCase().trim();
        
        switch (estadoUpper) {
            case "BOM":
            case "ÓTIMO":
            case "OTIMO":
            case "EXCELENTE":
                return 3;
                
            case "REGULAR":
            case "MÉDIO":
            case "MEDIO":
            case "RAZOÁVEL":
            case "RAZOAVEL":
                return 2;
                
            case "RUIM":
            case "PÉSSIMO":
            case "PESSIMO":
            case "DANIFICADO":
            case "QUEBRADO":
                return 1;
                
            default:
                return 0;
        }
    }
}
