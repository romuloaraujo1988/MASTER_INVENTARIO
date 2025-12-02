package com.inventario.mobile.server.service;

import com.inventario.dao.*;
import com.inventario.model.*;
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
 * @version 2.1.0 - Adicionado controle de memória
 */
@Service
public class MobileSyncService {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileSyncService.class);
    
    // Limites para evitar sobrecarga de memória
    private static final int MAX_PATRIMONIOS = 10000;
    private static final int MAX_SALAS = 1000;
    private static final int MAX_RESPONSAVEIS = 500;
    private static final int MAX_SETORES = 200;
    
    private final PatrimonioDAO patrimonioDAO;
    private final com.inventario.service.InventarioService inventarioService;
    private final com.inventario.service.SalaService salaService;
    private final com.inventario.service.ResponsavelService responsavelService;
    private final com.inventario.service.SetorService setorService;
    
    // Usar injeção de dependência do Spring ao invés de ServiceFactory
    public MobileSyncService(
            com.inventario.service.InventarioService inventarioService,
            com.inventario.service.SalaService salaService,
            com.inventario.service.ResponsavelService responsavelService,
            com.inventario.service.SetorService setorService) {
        this.patrimonioDAO = new PatrimonioDAO();
        this.inventarioService = inventarioService;
        this.salaService = salaService;
        this.responsavelService = responsavelService;
        this.setorService = setorService;
    }
    
    /**
     * Sincronização completa de todos os dados necessários
     */
    public Map<String, Object> sincronizacaoCompleta(Integer idInventario) throws SQLException {
        logger.debug("Iniciando sincronização completa");
        
        long inicio = System.currentTimeMillis();
        
        // Buscar inventário ativo se não informado
        if (idInventario == null) {
            Inventario inventarioAtivo = inventarioService.buscarPorStatus("EM_ANDAMENTO");
            if (inventarioAtivo != null) {
                idInventario = inventarioAtivo.getId();
            }
        }
        
        // Buscar dados com limites para evitar sobrecarga de memória
        List<Patrimonio> patrimonios = patrimonioDAO.findAll();
        if (patrimonios.size() > MAX_PATRIMONIOS) {
            logger.warn("Limite de patrimônios atingido: {} > {}. Truncando.", patrimonios.size(), MAX_PATRIMONIOS);
            patrimonios = patrimonios.subList(0, MAX_PATRIMONIOS);
        }
        
        List<com.inventario.model.Sala> salas = salaService.listarTodas();
        if (salas.size() > MAX_SALAS) {
            logger.warn("Limite de salas atingido: {} > {}. Truncando.", salas.size(), MAX_SALAS);
            salas = salas.subList(0, MAX_SALAS);
        }
        
        List<com.inventario.model.Responsavel> responsaveis = responsavelService.listarTodos();
        if (responsaveis.size() > MAX_RESPONSAVEIS) {
            logger.warn("Limite de responsáveis atingido: {} > {}. Truncando.", responsaveis.size(), MAX_RESPONSAVEIS);
            responsaveis = responsaveis.subList(0, MAX_RESPONSAVEIS);
        }
        
        List<com.inventario.model.Setor> setores = setorService.listarTodos();
        if (setores.size() > MAX_SETORES) {
            logger.warn("Limite de setores atingido: {} > {}. Truncando.", setores.size(), MAX_SETORES);
            setores = setores.subList(0, MAX_SETORES);
        }
        
        Inventario inventario = idInventario != null ? inventarioService.buscarPorId(idInventario) : null;
        
        // Converter para formato otimizado
        List<Map<String, Object>> patrimoniosSimplificados = simplificarPatrimonios(patrimonios);
        patrimonios = null; // Liberar memória
        
        List<Map<String, Object>> salasSimplificadas = simplificarSalas(salas);
        salas = null; // Liberar memória
        
        List<Map<String, Object>> responsaveisSimplificados = simplificarResponsaveis(responsaveis);
        responsaveis = null; // Liberar memória
        
        List<Map<String, Object>> setoresSimplificados = simplificarSetores(setores);
        setores = null; // Liberar memória
        
        // Guardar tamanhos antes de liberar memória
        int totalPatrimonios = patrimoniosSimplificados.size();
        int totalSalas = salasSimplificadas.size();
        int totalResponsaveis = responsaveisSimplificados.size();
        int totalSetores = setoresSimplificados.size();
        
        long duracao = System.currentTimeMillis() - inicio;
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("patrimonios", patrimoniosSimplificados);
        resultado.put("salas", salasSimplificadas);
        resultado.put("responsaveis", responsaveisSimplificados);
        resultado.put("setores", setoresSimplificados);
        resultado.put("inventario", inventario != null ? simplificarInventario(inventario) : null);
        resultado.put("totalPatrimonios", totalPatrimonios);
        resultado.put("totalSalas", totalSalas);
        resultado.put("totalResponsaveis", totalResponsaveis);
        resultado.put("totalSetores", totalSetores);
        resultado.put("timestamp", System.currentTimeMillis());
        resultado.put("duracaoMs", duracao);
        resultado.put("versao", "2.1.0");
        
        logger.debug("Sincronização completa finalizada em {}ms", duracao);
        
        return resultado;
    }
    
    /**
     * Sincronizar apenas patrimônios (com filtro de data)
     * OTIMIZADO: Usa query SQL com filtro ao invés de carregar todos
     */
    public Map<String, Object> sincronizarPatrimonios(Long ultimaAtualizacao) throws SQLException {
        List<Patrimonio> patrimonios;
        
        // Se tem data de última atualização, buscar apenas os atualizados
        if (ultimaAtualizacao != null && ultimaAtualizacao > 0) {
            patrimonios = buscarPatrimoniosAtualizados(ultimaAtualizacao);
        } else {
            // Primeira sincronização - carregar com limite
            patrimonios = patrimonioDAO.findAll();
            if (patrimonios.size() > MAX_PATRIMONIOS) {
                logger.warn("Muitos patrimônios ({}). Limitando a {}.", patrimonios.size(), MAX_PATRIMONIOS);
                patrimonios = patrimonios.subList(0, MAX_PATRIMONIOS);
            }
        }
        
        List<Map<String, Object>> simplificados = simplificarPatrimonios(patrimonios);
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("patrimonios", simplificados);
        resultado.put("total", simplificados.size());
        resultado.put("timestamp", System.currentTimeMillis());
        
        // Liberar memória imediatamente
        patrimonios = null;
        
        return resultado;
    }
    
    /**
     * Busca patrimônios atualizados desde uma data (query otimizada)
     */
    private List<Patrimonio> buscarPatrimoniosAtualizados(Long ultimaAtualizacao) throws SQLException {
        List<Patrimonio> resultado = new ArrayList<>();
        String sql = """
            SELECT * FROM TABELA_PATRIMONIO 
            WHERE DATA_CARGA > ? 
            AND (STATUS IS NULL OR UPPER(STATUS) = 'ATIVO' OR STATUS = '')
            ORDER BY ID
            LIMIT ?
            """;
        
        try (java.sql.Connection conn = com.inventario.util.DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, new java.sql.Timestamp(ultimaAtualizacao));
            stmt.setInt(2, MAX_PATRIMONIOS);
            
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Patrimonio p = new Patrimonio();
                    p.setId(rs.getInt("ID"));
                    p.setNumero(rs.getString("NUMERO"));
                    p.setDescricao(rs.getString("DESCRICAO"));
                    p.setIdSala(rs.getObject("ID_SALA") != null ? rs.getInt("ID_SALA") : null);
                    p.setIdResponsavel(rs.getObject("ID_RESPONSAVEL") != null ? rs.getInt("ID_RESPONSAVEL") : null);
                    p.setStatus(rs.getString("STATUS"));
                    resultado.add(p);
                }
            }
        }
        
        logger.debug("Encontrados {} patrimônios atualizados desde {}", resultado.size(), ultimaAtualizacao);
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
    /**
     * Obter metadados da sincronização
     * OTIMIZADO: Usa COUNT ao invés de carregar todos os registros
     */
    public Map<String, Object> obterMetadados() throws SQLException {
        Map<String, Object> metadata = new HashMap<>();
        // OTIMIZADO: Usar contagem ao invés de findAll().size()
        metadata.put("totalPatrimonios", patrimonioDAO.contarPatrimoniosAtivos());
        metadata.put("totalSalas", salaService.listarTodas().size()); // Salas são poucas
        metadata.put("totalResponsaveis", responsavelService.listarTodos().size()); // Responsáveis são poucos
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
    
    /**
     * Conta patrimônios atualizados desde última sincronização
     * OTIMIZADO: Usa query SQL ao invés de carregar todos em memória
     */
    private int contarPatrimoniosAtualizados(Long ultimaSincronizacao) throws SQLException {
        if (ultimaSincronizacao == null || ultimaSincronizacao == 0) {
            return patrimonioDAO.contarPatrimoniosAtivos();
        }
        
        // Usar query SQL direta para contar (mais eficiente)
        String sql = "SELECT COUNT(*) FROM TABELA_PATRIMONIO WHERE DATA_CARGA > ? AND (STATUS IS NULL OR UPPER(STATUS) = 'ATIVO' OR STATUS = '')";
        try (java.sql.Connection conn = com.inventario.util.DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, new java.sql.Timestamp(ultimaSincronizacao));
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
}
