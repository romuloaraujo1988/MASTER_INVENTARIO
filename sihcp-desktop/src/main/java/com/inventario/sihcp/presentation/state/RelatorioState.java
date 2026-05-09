package com.inventario.sihcp.presentation.state;

import com.inventario.sihcp.model.Inventario;
import com.inventario.sihcp.model.Setor;
import com.inventario.sihcp.model.Responsavel;
import com.inventario.sihcp.model.Sala;

import java.util.List;
import java.util.Map;

/**
 * Estados possíveis da tela de Relatórios
 * Sealed class pattern para estados mutuamente exclusivos
 * 
 * Regra Clean Architecture: UI State deve ser imutável e representar
 * exatamente o estado visual da tela
 */
public abstract class RelatorioState {
    
    /**
     * Estado inicial - aguardando ação do usuário
     */
    public static class Idle extends RelatorioState {
        public static final Idle INSTANCE = new Idle();
        private Idle() {}
    }
    
    /**
     * Estado de carregamento - operação em andamento
     */
    public static class Loading extends RelatorioState {
        public static final Loading INSTANCE = new Loading();
        private Loading() {}
    }
    
    /**
     * Relatório gerado com sucesso
     */
    public static class Success extends RelatorioState {
        private final List<Map<String, Object>> dados;
        private final String tipoRelatorio;
        
        public Success(List<Map<String, Object>> dados, String tipoRelatorio) {
            this.dados = dados;
            this.tipoRelatorio = tipoRelatorio;
        }
        
        public List<Map<String, Object>> getDados() {
            return dados;
        }
        
        public String getTipoRelatorio() {
            return tipoRelatorio;
        }
    }
    
    /**
     * Erro ao gerar relatório
     */
    public static class Error extends RelatorioState {
        private final String message;
        
        public Error(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    /**
     * Inventários carregados
     */
    public static class InventariosCarregados extends RelatorioState {
        private final List<Inventario> inventarios;
        
        public InventariosCarregados(List<Inventario> inventarios) {
            this.inventarios = inventarios;
        }
        
        public List<Inventario> getInventarios() {
            return inventarios;
        }
    }
    
    /**
     * Setores carregados
     */
    public static class SetoresCarregados extends RelatorioState {
        private final List<Setor> setores;
        
        public SetoresCarregados(List<Setor> setores) {
            this.setores = setores;
        }
        
        public List<Setor> getSetores() {
            return setores;
        }
    }
    
    /**
     * Responsáveis carregados
     */
    public static class ResponsaveisCarregados extends RelatorioState {
        private final List<Responsavel> responsaveis;
        
        public ResponsaveisCarregados(List<Responsavel> responsaveis) {
            this.responsaveis = responsaveis;
        }
        
        public List<Responsavel> getResponsaveis() {
            return responsaveis;
        }
    }
    
    /**
     * Salas carregadas
     */
    public static class SalasCarregadas extends RelatorioState {
        private final List<Sala> salas;
        
        public SalasCarregadas(List<Sala> salas) {
            this.salas = salas;
        }
        
        public List<Sala> getSalas() {
            return salas;
        }
    }
}
