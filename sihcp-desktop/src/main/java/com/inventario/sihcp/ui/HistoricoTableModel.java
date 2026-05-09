package com.inventario.sihcp.ui;

import com.inventario.sihcp.dto.HistoricoColetaDTO;

import javax.swing.table.AbstractTableModel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Modelo de tabela customizado para exibição do histórico de coletas.
 * 
 * Colunas:
 * - Data/Hora: Data e hora da coleta
 * - Inventário: Nome do inventário
 * - Coletor: Nome completo do coletor
 * - Localização: Localização encontrada (sala/setor)
 * - Estado: Estado do patrimônio
 * - Observações: Observações da coleta
 * 
 * @author Sistema Inventário
 * @version 1.0
 */
public class HistoricoTableModel extends AbstractTableModel {
    
    private static final long serialVersionUID = 1L;
    
    // Nomes das colunas
    private static final String[] COLUMN_NAMES = {
        "Data/Hora",
        "Inventário",
        "Coletor",
        "Localização",
        "Estado",
        "Observações"
    };
    
    // Índices das colunas
    public static final int COL_DATA = 0;
    public static final int COL_INVENTARIO = 1;
    public static final int COL_COLETOR = 2;
    public static final int COL_LOCALIZACAO = 3;
    public static final int COL_ESTADO = 4;
    public static final int COL_OBSERVACOES = 5;
    
    // Dados da tabela
    private List<HistoricoColetaDTO> dados;
    
    // Formatador de data
    private final SimpleDateFormat dateFormat;
    
    /**
     * Construtor padrão
     */
    public HistoricoTableModel() {
        this.dados = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    }
    
    /**
     * Define os dados da tabela
     * 
     * @param dados Lista de histórico de coletas
     */
    public void setData(List<HistoricoColetaDTO> dados) {
        if (dados == null) {
            this.dados = new ArrayList<>();
        } else {
            this.dados = new ArrayList<>(dados);
        }
        fireTableDataChanged();
    }
    
    /**
     * Adiciona uma coleta ao histórico
     * 
     * @param coleta Coleta a ser adicionada
     */
    public void addColeta(HistoricoColetaDTO coleta) {
        if (coleta != null) {
            this.dados.add(coleta);
            int row = this.dados.size() - 1;
            fireTableRowsInserted(row, row);
        }
    }
    
    /**
     * Remove todas as coletas
     */
    public void clear() {
        this.dados.clear();
        fireTableDataChanged();
    }
    
    /**
     * Retorna a coleta em uma linha específica
     * 
     * @param row Índice da linha
     * @return DTO da coleta ou null se índice inválido
     */
    public HistoricoColetaDTO getColetaAt(int row) {
        if (row >= 0 && row < dados.size()) {
            return dados.get(row);
        }
        return null;
    }
    
    /**
     * Retorna todas as coletas
     * 
     * @return Lista de coletas
     */
    public List<HistoricoColetaDTO> getAllColetas() {
        return new ArrayList<>(dados);
    }
    
    @Override
    public int getRowCount() {
        return dados.size();
    }
    
    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }
    
    @Override
    public String getColumnName(int column) {
        if (column >= 0 && column < COLUMN_NAMES.length) {
            return COLUMN_NAMES[column];
        }
        return "";
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case COL_DATA:
                return Date.class;
            case COL_INVENTARIO:
            case COL_COLETOR:
            case COL_LOCALIZACAO:
            case COL_ESTADO:
            case COL_OBSERVACOES:
                return String.class;
            default:
                return Object.class;
        }
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // Histórico é somente leitura
        return false;
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= dados.size()) {
            return null;
        }
        
        HistoricoColetaDTO coleta = dados.get(rowIndex);
        
        switch (columnIndex) {
            case COL_DATA:
                return coleta.getDataColeta();
                
            case COL_INVENTARIO:
                return coleta.getNomeInventario() != null 
                    ? coleta.getNomeInventario() 
                    : "N/A";
                
            case COL_COLETOR:
                return coleta.getNomeColetorCompleto() != null 
                    ? coleta.getNomeColetorCompleto() 
                    : "N/A";
                
            case COL_LOCALIZACAO:
                return formatarLocalizacao(coleta);
                
            case COL_ESTADO:
                return coleta.getEstadoEncontrado() != null 
                    ? coleta.getEstadoEncontrado() 
                    : "N/A";
                
            case COL_OBSERVACOES:
                String obs = coleta.getObservacoes();
                if (obs != null && !obs.trim().isEmpty()) {
                    // Limitar tamanho para exibição
                    return obs.length() > 50 
                        ? obs.substring(0, 47) + "..." 
                        : obs;
                }
                return "";
                
            default:
                return null;
        }
    }
    
    /**
     * Formata a localização combinando sala e setor
     * 
     * @param coleta Coleta com dados de localização
     * @return String formatada com localização
     */
    private String formatarLocalizacao(HistoricoColetaDTO coleta) {
        StringBuilder sb = new StringBuilder();
        
        // Adicionar sala
        if (coleta.getNomeSala() != null && !coleta.getNomeSala().trim().isEmpty()) {
            sb.append(coleta.getNomeSala());
        }
        
        // Adicionar setor
        if (coleta.getNomeSetor() != null && !coleta.getNomeSetor().trim().isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" - ");
            }
            sb.append(coleta.getNomeSetor());
        }
        
        // Se não tem sala nem setor, usar localização encontrada
        if (sb.length() == 0 && coleta.getLocalizacaoEncontrada() != null) {
            sb.append(coleta.getLocalizacaoEncontrada());
        }
        
        return sb.length() > 0 ? sb.toString() : "N/A";
    }
    
    /**
     * Formata data para exibição
     * 
     * @param data Data a ser formatada
     * @return String formatada
     */
    public String formatarData(Date data) {
        if (data != null) {
            return dateFormat.format(data);
        }
        return "";
    }
    
    /**
     * Verifica se uma linha é a mais recente (primeira)
     * 
     * @param row Índice da linha
     * @return true se é a primeira linha
     */
    public boolean isPrimeiraColeta(int row) {
        return row == 0 && !dados.isEmpty();
    }
    
    /**
     * Verifica se há mudança de localização em relação à coleta anterior
     * 
     * @param row Índice da linha
     * @return true se houve mudança
     */
    public boolean temMudancaLocalizacao(int row) {
        if (row >= 0 && row < dados.size()) {
            HistoricoColetaDTO coleta = dados.get(row);
            return coleta.getTemMudancaLocalizacao() != null 
                && coleta.getTemMudancaLocalizacao();
        }
        return false;
    }
    
    /**
     * Verifica se há mudança de estado em relação à coleta anterior
     * 
     * @param row Índice da linha
     * @return true se houve mudança
     */
    public boolean temMudancaEstado(int row) {
        if (row >= 0 && row < dados.size()) {
            HistoricoColetaDTO coleta = dados.get(row);
            return coleta.getTemMudancaEstado() != null 
                && coleta.getTemMudancaEstado();
        }
        return false;
    }
    
    /**
     * Retorna a localização anterior (para comparação)
     * 
     * @param row Índice da linha
     * @return Localização anterior ou null
     */
    public String getLocalizacaoAnterior(int row) {
        if (row >= 0 && row < dados.size()) {
            HistoricoColetaDTO coleta = dados.get(row);
            return coleta.getLocalizacaoAnterior();
        }
        return null;
    }
    
    /**
     * Retorna o estado anterior (para comparação)
     * 
     * @param row Índice da linha
     * @return Estado anterior ou null
     */
    public String getEstadoAnterior(int row) {
        if (row >= 0 && row < dados.size()) {
            HistoricoColetaDTO coleta = dados.get(row);
            return coleta.getEstadoAnterior();
        }
        return null;
    }
}
