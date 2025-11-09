package com.inventario.mobile.server.service;

import com.inventario.dao.*;
import com.inventario.model.*;
import com.inventario.service.ServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço para sincronização de dados offline
 * Prepara pacotes de dados otimizados para o app mobile
 * 
 * @author Sistema de Inventário
 * @version 2.0.0
 */
@Service
public class MobileSyncService {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileSyncService.class);
    
    private final PatrimonioDAORefactored patrimonioDAO;
    private final com.inventario.service.InventarioService inventarioService;
    private final com.inventario.service.SalaService salaService;
    private final com.inventario.service.ResponsavelService responsavelService;
    private final com.inventario.service.SetorService setorService;
    
    public MobileSyncService() {
        this.patrimonioDAO = new PatrimonioDAORefactored();
        
        // Obter services do ServiceFactory (agora com métodos estáticos)
        this.inventarioService = ServiceFactory.getInventarioService();
        this.salaService = ServiceFactory.getSalaService();
        this.responsavelService = ServiceFactory.getResponsavelService();
        this.setorService = ServiceFactory.getSetorService();
    }
    
    /**
     * Sincronização completa de todos os dados necessários
     */
    public Map<String, Object> sincronizacaoCompleta(Integer idInventario) throws SQLException {
        logger.info("Iniciando sincronização completa");
        
        long inicio = System.currentTimeMillis();
        
        // Buscar inventário ativo se não informado
        if (idInventario == null) {
            Inventario inventarioAtivo = inventarioService.buscarPorStatus("EM_ANDAMENTO");
            if (inventarioAtivo != null) {
                idInventario = inventarioAtivo.getId();
            }
        }
        
        // Buscar todos os dados
        List<Patrimonio> patrimonios = patrimonioDAO.findAll();
        List<com.inventario.model.Sala> salas = salaService.listarTodas();
        List<com.inventario.model.Responsavel> responsaveis = responsavelService.listarTodos();
        List<com.inventario.model.Setor> setores = setorService.listarTodos();
        Inventario inventario = idInventario != null ? inventarioService.buscarPorId(idInventario) : null;
        
        // Converter para formato otimizado
        List<Map<String, Object>> patrimoniosSimplificados = simplificarPatrimonios(patrimonios);
        List<Map<String, Object>> salasSimplificadas = simplificarSalas(salas);
        List<Map<String, Object>> responsaveisSimplificados = simplificarResponsaveis(responsaveis);
        List<Map<String, Object>> setoresSimplificados = simplificarSetores(setores);
        
        long duracao = System.currentTimeMillis() - inicio;
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("patrimonios", patrimoniosSimplificados);
        resultado.put("salas", salasSimplificadas);
        resultado.put("responsaveis", responsaveisSimplificados);
        resultado.put("setores", setoresSimplificados);
        resultado.put("inventario", inventario != null ? simplificarInventario(inventario) : null);
        resultado.put("totalPatrimonios", patrimonios.size());
        resultado.put("totalSalas", salas.size());
        resultado.put("totalResponsaveis", responsaveis.size());
        resultado.put("totalSetores", setores.size());
        resultado.put("timestamp", System.currentTimeMillis());
        resultado.put("duracaoMs", duracao);
        resultado.put("versao", "2.0.0");
        
        logger.info("Sincronização completa finalizada em {}ms", duracao);
        
        return resultado;
    }
    
    /**
     * Sincronizar apenas patrimônios (com filtro de data)
     */
    public Map<String, Object> sincronizarPatrimonios(Long ultimaAtualizacao) throws SQLException {
        List<Patrimonio> patrimonios = patrimonioDAO.findAll();
        
        // Filtrar por data se fornecida
        if (ultimaAtualizacao != null) {
            patrimonios = patrimonios.stream()
                .filter(p -> p.getDataCarga() != null && 
                            p.getDataCarga().getTime() > ultimaAtualizacao)
                .collect(Collectors.toList());
        }
        
        List<Map<String, Object>> simplificados = simplificarPatrimonios(patrimonios);
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("patrimonios", simplificados);
        resultado.put("total", simplificados.size());
        resultado.put("timestamp", System.currentTimeMillis());
        
        return resultado;
    }
    
    /**
     * Sincronizar apenas salas
     */
    public Map<String, Object> sincronizarSalas() throws SQLException {
        List<com.inventario.model.Sala> salas = salaService.listarTodas();
        List<Map<String, Object>> simplificadas = simplificarSalas(salas);
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("salas", simplificadas);
        resultado.put("total", simplificadas.size());
        resultado.put("timestamp", System.currentTimeMillis());
        
        return resultado;
    }
    
    /**
     * Sincronizar apenas responsáveis
     */
    public Map<String, Object> sincronizarResponsaveis() throws SQLException {
        List<com.inventario.model.Responsavel> responsaveis = responsavelService.listarTodos();
        List<Map<String, Object>> simplificados = simplificarResponsaveis(responsaveis);
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("responsaveis", simplificados);
        resultado.put("total", simplificados.size());
        resultado.put("timestamp", System.currentTimeMillis());
        
        return resultado;
    }
    
    /**
     * Verificar se há atualizações disponíveis
     */
    public Map<String, Object> verificarAtualizacoes(Long ultimaSincronizacao) throws SQLException {
        int patrimoniosNovos = contarPatrimoniosAtualizados(ultimaSincronizacao);
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("temAtualizacoes", patrimoniosNovos > 0);
        resultado.put("patrimoniosNovos", patrimoniosNovos);
        resultado.put("timestamp", System.currentTimeMillis());
        
        return resultado;
    }
    
    /**
     * Obter metadados da sincronização
     */
    public Map<String, Object> obterMetadados() throws SQLException {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("totalPatrimonios", patrimonioDAO.findAll().size());
        metadata.put("totalSalas", salaService.listarTodas().size());
        metadata.put("totalResponsaveis", responsavelService.listarTodos().size());
        metadata.put("versao", "2.0.0");
        metadata.put("timestamp", System.currentTimeMillis());
        
        return metadata;
    }
    
    // Métodos auxiliares de simplificação
    
    private List<Map<String, Object>> simplificarPatrimonios(List<Patrimonio> patrimonios) {
        return patrimonios.stream()
            .map(this::simplificarPatrimonio)
            .collect(Collectors.toList());
    }
    
    private Map<String, Object> simplificarPatrimonio(Patrimonio p) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", p.getId());
        map.put("numero", p.getNumero());
        map.put("descricao", p.getDescricao());
        map.put("idSala", p.getIdSala());
        map.put("nomeSala", p.getNomeSala());
        map.put("idResponsavel", p.getIdResponsavel());
        map.put("nomeResponsavel", p.getNomeResponsavel());
        map.put("status", p.getStatus());
        map.put("estadoConservacao", p.getEstadoConservacao());
        map.put("marca", p.getMarca());
        map.put("modelo", p.getModelo());
        map.put("numeroSerie", p.getNumeroSerie());
        return map;
    }
    
    private List<Map<String, Object>> simplificarSalas(List<com.inventario.model.Sala> salas) {
        return salas.stream()
            .map(this::simplificarSala)
            .collect(Collectors.toList());
    }
    
    private Map<String, Object> simplificarSala(com.inventario.model.Sala s) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", s.getIdSala());
        map.put("nome", s.getDescricao()); // Sala usa getDescricao()
        map.put("numero", s.getNumeroSala());
        map.put("andar", s.getAndar());
        map.put("bloco", s.getBloco());
        map.put("idSetor", s.getIdSetor());
        map.put("nomeSetor", s.getNomeSetor());
        return map;
    }
    
    private List<Map<String, Object>> simplificarResponsaveis(List<com.inventario.model.Responsavel> responsaveis) {
        return responsaveis.stream()
            .map(this::simplificarResponsavel)
            .collect(Collectors.toList());
    }
    
    private Map<String, Object> simplificarResponsavel(com.inventario.model.Responsavel r) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", r.getId());
        map.put("nome", r.getNome());
        map.put("cpf", r.getCpf());
        map.put("cargo", r.getCargo());
        map.put("email", r.getEmail());
        map.put("telefone", r.getTelefone());
        map.put("idSetor", r.getIdSetor());
        return map;
    }
    
    private List<Map<String, Object>> simplificarSetores(List<com.inventario.model.Setor> setores) {
        return setores.stream()
            .map(this::simplificarSetor)
            .collect(Collectors.toList());
    }
    
    private Map<String, Object> simplificarSetor(com.inventario.model.Setor s) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", s.getId());
        map.put("nome", s.getNome());
        map.put("sigla", s.getNome()); // Usar nome como sigla se não houver campo específico
        return map;
    }
    
    private Map<String, Object> simplificarInventario(Inventario i) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", i.getId());
        map.put("nome", i.getNome());
        map.put("dataInicio", i.getDataInicio());
        map.put("dataFim", i.getDataFim());
        map.put("status", i.getStatusInventario());
        return map;
    }
    
    private int contarPatrimoniosAtualizados(Long ultimaSincronizacao) throws SQLException {
        List<Patrimonio> todos = patrimonioDAO.findAll();
        return (int) todos.stream()
            .filter(p -> p.getDataCarga() != null && 
                        p.getDataCarga().getTime() > ultimaSincronizacao)
            .count();
    }
}
