package com.inventario.itemcomposto.service;

import com.inventario.itemcomposto.dao.ItemCompostoDAO;
import com.inventario.itemcomposto.model.Componente;
import com.inventario.itemcomposto.model.ItemComposto;
import com.inventario.util.ConnectionManager;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

/**
 * Service para detecção e aplicação em lote de itens compostos.
 * Permite configurar múltiplos patrimônios de uma vez baseado na descrição.
 * 
 * @author Sistema de Inventário Patrimonial
 * @version 1.0
 * @since 27/11/2025
 */
@Service
public class DeteccaoEmLoteService {
    
    private final ItemCompostoService itemCompostoService;
    private final ItemCompostoDAO itemCompostoDAO;
    
    public DeteccaoEmLoteService() {
        this.itemCompostoService = new ItemCompostoService();
        this.itemCompostoDAO = new ItemCompostoDAO();
    }
    
    /**
     * Agrupa patrimônios por descrição única
     * Retorna Map<Descrição, DescricaoAgrupada>
     */
    public Map<String, DescricaoAgrupada> agruparPorDescricao() throws SQLException {
        String sql = "SELECT * FROM VIEW_DESCRICAO_AGRUPADA";
        Connection conn = null;
        Map<String, DescricaoAgrupada> mapa = new LinkedHashMap<>();
        
        try {
            conn = ConnectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    DescricaoAgrupada agrupada = new DescricaoAgrupada();
                    agrupada.setDescricao(rs.getString("DESCRICAO"));
                    agrupada.setQuantidadePatrimonios(rs.getInt("QUANTIDADE_PATRIMONIOS"));
                    agrupada.setQuantidadeJaConfigurados(rs.getInt("QUANTIDADE_JA_CONFIGURADOS"));
                    agrupada.setQuantidadeNaoConfigurados(rs.getInt("QUANTIDADE_NAO_CONFIGURADOS"));
                    agrupada.setTodosConfigurados(rs.getBoolean("TODOS_CONFIGURADOS"));
                    
                    // Converter array PostgreSQL para List
                    Array idsArray = rs.getArray("IDS_PATRIMONIOS");
                    if (idsArray != null) {
                        Integer[] ids = (Integer[]) idsArray.getArray();
                        agrupada.setIdsPatrimonios(Arrays.asList(ids));
                    }
                    
                    mapa.put(agrupada.getDescricao(), agrupada);
                }
            }
        } finally {
            ConnectionManager.closeConnection(conn);
        }
        
        return mapa;
    }
    
    /**
     * Aplica componentes em lote para todos os patrimônios com uma descrição
     */
    public ResultadoAplicacaoLote aplicarEmLote(
            String descricao, 
            List<Componente> componentes,
            Integer idUsuario,
            ProgressCallback callback) throws SQLException {
        
        long inicio = System.currentTimeMillis();
        ResultadoAplicacaoLote resultado = new ResultadoAplicacaoLote();
        resultado.setErros(new ArrayList<>());
        
        // Buscar patrimônios com essa descrição
        List<Integer> idsPatrimonios = buscarPatrimoniosPorDescricao(descricao);
        
        if (idsPatrimonios.isEmpty()) {
            resultado.setSucesso(false);
            resultado.getErros().add("Nenhum patrimônio encontrado com a descrição: " + descricao);
            return resultado;
        }
        
        Connection conn = null;
        int processados = 0;
        int componentesCriados = 0;
        
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false); // Iniciar transação
            
            for (int i = 0; i < idsPatrimonios.size(); i++) {
                // Verificar cancelamento
                if (callback != null && callback.isCancelado()) {
                    conn.rollback();
                    resultado.setSucesso(false);
                    resultado.getErros().add("Operação cancelada pelo usuário");
                    return resultado;
                }
                
                Integer idPatrimonio = idsPatrimonios.get(i);
                
                try {
                    // Verificar se já é composto
                    if (!itemCompostoDAO.patrimonioJaEhComposto(idPatrimonio)) {
                        // Criar item composto
                        ItemComposto item = itemCompostoService.criarItemComposto(
                            idPatrimonio, 
                            clonarComponentes(componentes), 
                            idUsuario
                        );
                        processados++;
                        componentesCriados += componentes.size();
                    }
                    
                    // Atualizar progresso
                    if (callback != null && i % 10 == 0) {
                        callback.onProgress(i + 1, idsPatrimonios.size(), 
                            String.format("Processando %d de %d...", i + 1, idsPatrimonios.size()));
                    }
                    
                } catch (Exception e) {
                    resultado.getErros().add(
                        String.format("Erro no patrimônio ID %d: %s", idPatrimonio, e.getMessage())
                    );
                }
            }
            
            // Commit da transação
            conn.commit();
            resultado.setSucesso(true);
            
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    // Log error
                }
            }
            resultado.setSucesso(false);
            resultado.getErros().add("Erro geral: " + e.getMessage());
            
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    // Log error
                }
                ConnectionManager.closeConnection(conn);
            }
        }
        
        // Finalizar resultado
        resultado.setPatrimoniosConfigurados(processados);
        resultado.setComponentesCriados(componentesCriados);
        resultado.setTempoExecucaoMs(System.currentTimeMillis() - inicio);
        
        return resultado;
    }
    
    /**
     * Busca IDs de patrimônios por descrição exata
     */
    private List<Integer> buscarPatrimoniosPorDescricao(String descricao) throws SQLException {
        String sql = "SELECT ID FROM TABELA_PATRIMONIO WHERE DESCRICAO = ? AND STATUS = 'ATIVO' ORDER BY NUMERO";
        Connection conn = null;
        List<Integer> ids = new ArrayList<>();
        
        try {
            conn = ConnectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, descricao);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        ids.add(rs.getInt("ID"));
                    }
                }
            }
        } finally {
            ConnectionManager.closeConnection(conn);
        }
        
        return ids;
    }
    
    /**
     * Clona lista de componentes (para evitar compartilhamento de referências)
     */
    private List<Componente> clonarComponentes(List<Componente> componentes) {
        List<Componente> clones = new ArrayList<>();
        for (Componente comp : componentes) {
            Componente clone = new Componente(comp.getTipo(), comp.getDescricao(), comp.getQuantidadeEsperada());
            clone.setOrdem(comp.getOrdem());
            clones.add(clone);
        }
        return clones;
    }
    
    // ==================== CLASSES AUXILIARES ====================
    
    public static class DescricaoAgrupada {
        private String descricao;
        private int quantidadePatrimonios;
        private int quantidadeJaConfigurados;
        private int quantidadeNaoConfigurados;
        private boolean todosConfigurados;
        private List<Integer> idsPatrimonios;
        
        // Getters e Setters
        public String getDescricao() { return descricao; }
        public void setDescricao(String descricao) { this.descricao = descricao; }
        
        public int getQuantidadePatrimonios() { return quantidadePatrimonios; }
        public void setQuantidadePatrimonios(int quantidadePatrimonios) { 
            this.quantidadePatrimonios = quantidadePatrimonios; 
        }
        
        public int getQuantidadeJaConfigurados() { return quantidadeJaConfigurados; }
        public void setQuantidadeJaConfigurados(int quantidadeJaConfigurados) { 
            this.quantidadeJaConfigurados = quantidadeJaConfigurados; 
        }
        
        public int getQuantidadeNaoConfigurados() { return quantidadeNaoConfigurados; }
        public void setQuantidadeNaoConfigurados(int quantidadeNaoConfigurados) { 
            this.quantidadeNaoConfigurados = quantidadeNaoConfigurados; 
        }
        
        public boolean isTodosConfigurados() { return todosConfigurados; }
        public void setTodosConfigurados(boolean todosConfigurados) { 
            this.todosConfigurados = todosConfigurados; 
        }
        
        public List<Integer> getIdsPatrimonios() { return idsPatrimonios; }
        public void setIdsPatrimonios(List<Integer> idsPatrimonios) { 
            this.idsPatrimonios = idsPatrimonios; 
        }
    }
    
    public static class ResultadoAplicacaoLote {
        private int patrimoniosConfigurados;
        private int componentesCriados;
        private long tempoExecucaoMs;
        private List<String> erros;
        private boolean sucesso;
        
        // Getters e Setters
        public int getPatrimoniosConfigurados() { return patrimoniosConfigurados; }
        public void setPatrimoniosConfigurados(int patrimoniosConfigurados) { 
            this.patrimoniosConfigurados = patrimoniosConfigurados; 
        }
        
        public int getComponentesCriados() { return componentesCriados; }
        public void setComponentesCriados(int componentesCriados) { 
            this.componentesCriados = componentesCriados; 
        }
        
        public long getTempoExecucaoMs() { return tempoExecucaoMs; }
        public void setTempoExecucaoMs(long tempoExecucaoMs) { 
            this.tempoExecucaoMs = tempoExecucaoMs; 
        }
        
        public List<String> getErros() { return erros; }
        public void setErros(List<String> erros) { this.erros = erros; }
        
        public boolean isSucesso() { return sucesso; }
        public void setSucesso(boolean sucesso) { this.sucesso = sucesso; }
    }
    
    public interface ProgressCallback {
        void onProgress(int atual, int total, String mensagem);
        boolean isCancelado();
    }
}
