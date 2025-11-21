package com.inventario.offline;

import com.inventario.dao.ColetaDAO;
import com.inventario.model.Coleta;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Serviço para gerenciar coletas em modo offline
 * Garante que coletas sejam salvas localmente quando offline
 * e sincronizadas automaticamente quando online
 */
public class ColetaOfflineService {
    
    private static final Logger LOGGER = Logger.getLogger(ColetaOfflineService.class.getName());
    private static ColetaOfflineService instance;
    
    private final OfflineManager offlineManager;
    private final OfflineDAO offlineDAO;
    private final ColetaDAO coletaDAO;
    private final SyncStatusManager syncStatusManager;
    
    private ColetaOfflineService() {
        this.offlineManager = OfflineManager.getInstance();
        this.offlineDAO = new OfflineDAO();
        this.coletaDAO = new ColetaDAO();
        this.syncStatusManager = SyncStatusManager.getInstance();
    }
    
    public static synchronized ColetaOfflineService getInstance() {
        if (instance == null) {
            instance = new ColetaOfflineService();
        }
        return instance;
    }
    
    /**
     * Salva uma coleta (online ou offline)
     * @param coleta Coleta a ser salva
     * @return ID da coleta salva
     * @throws SQLException
     */
    public int salvarColeta(Coleta coleta) throws SQLException {
        LOGGER.info("Salvando coleta - Modo: " + 
            (offlineManager.isOperatingOffline() ? "OFFLINE" : "ONLINE"));
        
        try {
            if (offlineManager.isOperatingOffline()) {
                // Modo offline: salvar apenas localmente
                return salvarColetaOffline(coleta);
            } else {
                // Modo online: salvar no PostgreSQL e backup no SQLite
                return salvarColetaOnline(coleta);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao salvar coleta", e);
            
            // Se falhou online, tentar salvar offline como fallback
            if (!offlineManager.isOperatingOffline()) {
                LOGGER.warning("Falha ao salvar online, tentando offline como fallback");
                return salvarColetaOffline(coleta);
            }
            
            throw e;
        }
    }
    
    /**
     * Salva coleta em modo online (PostgreSQL + SQLite backup)
     */
    private int salvarColetaOnline(Coleta coleta) throws SQLException {
        LOGGER.info("Salvando coleta online");
        
        // 1. Salvar no PostgreSQL
        coletaDAO.inserirColeta(coleta);
        int idColeta = coleta.getId();
        
        // 2. Salvar backup no SQLite
        try {
            Map<String, Object> coletaMap = coletaToMap(coleta);
            coletaMap.put("sincronizado", 1); // Já está sincronizado
            offlineDAO.salvarColetaOffline(coletaMap);
            
            LOGGER.info("Coleta salva online com sucesso - ID: " + idColeta);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao salvar backup no SQLite", e);
            // Não falha se backup falhar
        }
        
        return idColeta;
    }
    
    /**
     * Salva coleta em modo offline (apenas SQLite)
     */
    private int salvarColetaOffline(Coleta coleta) throws SQLException {
        LOGGER.info("Salvando coleta offline");
        
        Map<String, Object> coletaMap = coletaToMap(coleta);
        coletaMap.put("sincronizado", 0); // Pendente de sincronização
        
        int idColeta = offlineDAO.salvarColetaOffline(coletaMap);
        
        // Atualizar contador de pendentes
        syncStatusManager.adicionarItensPendentes("coletas", 1);
        
        LOGGER.info("Coleta salva offline com sucesso - ID local: " + idColeta);
        
        return idColeta;
    }
    
    /**
     * Verifica se uma coleta já existe
     */
    public boolean coletaExiste(int idInventario, int idPatrimonio) throws SQLException {
        if (offlineManager.isOperatingOffline()) {
            // Verificar no SQLite
            return offlineDAO.coletaExiste(idInventario, idPatrimonio);
        } else {
            // Verificar no PostgreSQL
            return coletaDAO.coletaExiste(idInventario, idPatrimonio);
        }
    }
    
    /**
     * Busca coletas pendentes de sincronização
     */
    public List<Coleta> buscarColetasPendentes() throws SQLException {
        List<Map<String, Object>> coletasMap = offlineDAO.buscarColetasPendentes();
        List<Coleta> coletas = new ArrayList<>();
        
        for (Map<String, Object> map : coletasMap) {
            coletas.add(mapToColeta(map));
        }
        
        LOGGER.info("Encontradas " + coletas.size() + " coletas pendentes");
        return coletas;
    }
    
    /**
     * Sincroniza todas as coletas pendentes
     * @return Quantidade de coletas sincronizadas
     */
    public int sincronizarColetasPendentes() {
        if (offlineManager.isOperatingOffline()) {
            LOGGER.warning("Tentativa de sincronizar em modo offline");
            return 0;
        }
        
        LOGGER.info("Iniciando sincronização de coletas pendentes");
        int sincronizadas = 0;
        
        try {
            List<Coleta> coletasPendentes = buscarColetasPendentes();
            
            for (Coleta coleta : coletasPendentes) {
                try {
                    // Verificar se já existe no PostgreSQL
                    if (!coletaDAO.coletaExiste(coleta.getIdInventario(), coleta.getIdPatrimonio())) {
                        // Inserir no PostgreSQL
                        coletaDAO.inserirColeta(coleta);
                        
                        // Marcar como sincronizada no SQLite
                        offlineDAO.marcarColetaSincronizada(coleta.getId());
                        
                        sincronizadas++;
                        LOGGER.info("Coleta sincronizada: " + coleta.getId());
                    } else {
                        // Já existe, apenas marcar como sincronizada
                        offlineDAO.marcarColetaSincronizada(coleta.getId());
                        LOGGER.info("Coleta já existia no servidor: " + coleta.getId());
                    }
                    
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Erro ao sincronizar coleta " + coleta.getId(), e);
                    // Continua com as próximas
                }
            }
            
            // Atualizar contador de pendentes
            if (sincronizadas > 0) {
                syncStatusManager.removerItensPendentes("coletas", sincronizadas);
                syncStatusManager.atualizarUltimaSincronizacaoGeral();
            }
            
            LOGGER.info("Sincronização concluída: " + sincronizadas + " coletas");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro durante sincronização", e);
        }
        
        return sincronizadas;
    }
    
    /**
     * Converte Coleta para Map
     */
    private Map<String, Object> coletaToMap(Coleta coleta) {
        Map<String, Object> map = new HashMap<>();
        map.put("id_inventario", coleta.getIdInventario());
        map.put("id_patrimonio", coleta.getIdPatrimonio());
        map.put("id_participante", coleta.getIdParticipanteInventario());
        map.put("numero_patrimonio", coleta.getNumeroPatrimonio());
        map.put("descricao_item_sem_etiqueta", coleta.getDescricaoItemSemEtiqueta());
        map.put("localizacao_encontrada", coleta.getLocalizacaoEncontrada());
        map.put("estado_encontrado", coleta.getEstadoEncontrado());
        map.put("observacoes", coleta.getObservacaoColeta());
        map.put("data_coleta", coleta.getDataColeta());
        return map;
    }
    
    /**
     * Converte Map para Coleta
     */
    private Coleta mapToColeta(Map<String, Object> map) {
        Coleta coleta = new Coleta();
        coleta.setId((Integer) map.get("id"));
        coleta.setIdInventario((Integer) map.get("id_inventario"));
        coleta.setIdPatrimonio((Integer) map.get("id_patrimonio"));
        coleta.setIdParticipanteInventario((Integer) map.get("id_participante"));
        coleta.setNumeroPatrimonio((String) map.get("numero_patrimonio"));
        coleta.setDescricaoItemSemEtiqueta((String) map.get("descricao_item_sem_etiqueta"));
        coleta.setLocalizacaoEncontrada((String) map.get("localizacao_encontrada"));
        coleta.setEstadoEncontrado((String) map.get("estado_encontrado"));
        coleta.setObservacaoColeta((String) map.get("observacoes"));
        coleta.setDataColeta((java.sql.Timestamp) map.get("data_coleta"));
        return coleta;
    }
    
    /**
     * Obtém quantidade de coletas pendentes
     */
    public int getQuantidadeColetasPendentes() {
        try {
            return buscarColetasPendentes().size();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao contar coletas pendentes", e);
            return 0;
        }
    }
}
