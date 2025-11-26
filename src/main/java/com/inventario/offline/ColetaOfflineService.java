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
     * 
     * @param coleta Coleta a ser salva
     * @return ID da coleta salva
     * @throws SQLException
     */
    public int salvarColeta(Coleta coleta) throws SQLException {
        LOGGER.info("Salvando coleta - Modo: " +
                (offlineManager.isOperatingOffline() ? "OFFLINE" : "ONLINE"));
        
        // DEBUG: Log detalhado do timestamp recebido
        System.out.println("=== DEBUG TIMESTAMP: ColetaOfflineService.salvarColeta ===");
        System.out.println("DEBUG TIMESTAMP: Data Coleta recebida: " + coleta.getDataColeta());
        System.out.println("DEBUG TIMESTAMP: Data Coleta (class): " + 
            (coleta.getDataColeta() != null ? coleta.getDataColeta().getClass().getName() : "null"));
        System.out.println("DEBUG TIMESTAMP: Data Coleta (time): " + 
            (coleta.getDataColeta() != null ? coleta.getDataColeta().getTime() : "null"));

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
            offlineDAO.salvarColeta(coletaMap);

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

        int idColeta = offlineDAO.salvarColeta(coletaMap);

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
     * Busca coletas por localização (para histórico offline)
     * 
     * @param localizacao Localização encontrada
     * @return Lista de coletas na localização
     */
    public List<Coleta> buscarColetasPorLocalizacao(String localizacao) {
        List<Coleta> coletas = new ArrayList<>();
        try {
            // Buscar todas as coletas locais (pendentes e sincronizadas)
            // Idealmente o OfflineDAO teria um método específico, mas vamos filtrar em
            // memória por enquanto
            // ou buscar tudo da tabela local_coleta e coleta_offline

            // Por simplificação, vamos buscar da tabela coleta_offline (que armazena o que
            // foi feito offline)
            // Se precisar de dados que vieram do servidor (sync), precisaria consultar
            // local_coleta também

            List<Map<String, Object>> coletasMap = offlineDAO.buscarColetasPendentes(); // Isso busca só pendentes
            // TODO: Melhorar para buscar também as já sincronizadas que estão no banco
            // local

            for (Map<String, Object> map : coletasMap) {
                String loc = (String) map.get("localizacao_encontrada");
                if (loc != null && loc.equals(localizacao)) {
                    coletas.add(mapToColeta(map));
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar coletas por localização offline", e);
        }
        return coletas;
    }

    /**
     * Sincroniza todas as coletas pendentes
     * 
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
        System.out.println("=== DEBUG TIMESTAMP: coletaToMap ===");
        System.out.println("DEBUG: Estado Encontrado: " + coleta.getEstadoEncontrado());
        
        Map<String, Object> map = new HashMap<>();
        map.put("id_inventario", coleta.getIdInventario());
        map.put("id_patrimonio", coleta.getIdPatrimonio());
        map.put("id_participante", coleta.getIdParticipanteInventario());
        map.put("numero_patrimonio", coleta.getNumeroPatrimonio());
        map.put("descricao_sem_etiqueta", coleta.getDescricaoItemSemEtiqueta());
        map.put("localizacao_encontrada", coleta.getLocalizacaoEncontrada());
        map.put("localizacao_atual", coleta.getLocalizacaoAtual());
        
        // ✅ CORREÇÃO: Garantir que o estado de conservação seja salvo corretamente
        String estadoEncontrado = coleta.getEstadoEncontrado();
        if (estadoEncontrado == null || estadoEncontrado.trim().isEmpty()) {
            estadoEncontrado = "BOM"; // Valor padrão se não informado
        }
        map.put("situacao_encontrada", estadoEncontrado); // SQLite usa situacao_encontrada
        System.out.println("DEBUG: situacao_encontrada mapeada: " + estadoEncontrado);
        
        map.put("observacoes", coleta.getObservacaoColeta());
        map.put("foto_patrimonio", coleta.getFotoPatrimonio());
        map.put("sem_etiqueta", coleta.isSemEtiqueta());
        
        // DEBUG: Log detalhado do timestamp antes de colocar no map
        java.sql.Timestamp dataColeta = coleta.getDataColeta();
        System.out.println("DEBUG TIMESTAMP: Data Coleta antes do map.put: " + dataColeta);
        System.out.println("DEBUG TIMESTAMP: Data Coleta (class): " + 
            (dataColeta != null ? dataColeta.getClass().getName() : "null"));
        System.out.println("DEBUG TIMESTAMP: Data Coleta (time): " + 
            (dataColeta != null ? dataColeta.getTime() : "null"));
        
        map.put("data_coleta", dataColeta);
        
        System.out.println("DEBUG TIMESTAMP: Valor no map após put: " + map.get("data_coleta"));
        System.out.println("DEBUG TIMESTAMP: Classe no map: " + 
            (map.get("data_coleta") != null ? map.get("data_coleta").getClass().getName() : "null"));
        
        return map;
    }

    /**
     * Converte Map para Coleta
     */
    private Coleta mapToColeta(Map<String, Object> map) {
        Coleta coleta = new Coleta();
        if (map.get("id") != null)
            coleta.setId(((Number) map.get("id")).intValue());
        if (map.get("id_inventario") != null)
            coleta.setIdInventario(((Number) map.get("id_inventario")).intValue());
        if (map.get("id_patrimonio") != null)
            coleta.setIdPatrimonio(((Number) map.get("id_patrimonio")).intValue());
        if (map.get("id_participante") != null)
            coleta.setIdParticipanteInventario(((Number) map.get("id_participante")).intValue());

        coleta.setNumeroPatrimonio((String) map.get("numero_patrimonio"));
        coleta.setDescricaoItemSemEtiqueta((String) map.get("descricao_sem_etiqueta"));
        coleta.setLocalizacaoEncontrada((String) map.get("localizacao_encontrada"));
        coleta.setLocalizacaoAtual((String) map.get("localizacao_atual"));
        coleta.setEstadoEncontrado((String) map.get("situacao_encontrada")); // SQLite usa situacao_encontrada
        coleta.setObservacaoColeta((String) map.get("observacoes"));
        coleta.setFotoPatrimonio((String) map.get("foto_patrimonio"));
        coleta.setSemEtiqueta((Boolean) map.getOrDefault("sem_etiqueta", false));

        // Conversão segura de data - tratamento robusto para diferentes formatos
        Object dataObj = map.get("data_coleta");
        coleta.setDataColeta(converterParaTimestamp(dataObj));

        return coleta;
    }
    
    /**
     * Converte diferentes tipos de objeto para Timestamp de forma segura
     * Suporta: Timestamp, Long, String em vários formatos
     */
    private java.sql.Timestamp converterParaTimestamp(Object dataObj) {
        if (dataObj == null) {
            return new java.sql.Timestamp(System.currentTimeMillis());
        }
        
        if (dataObj instanceof java.sql.Timestamp) {
            return (java.sql.Timestamp) dataObj;
        }
        
        if (dataObj instanceof Long) {
            return new java.sql.Timestamp((Long) dataObj);
        }
        
        if (dataObj instanceof java.util.Date) {
            return new java.sql.Timestamp(((java.util.Date) dataObj).getTime());
        }
        
        if (dataObj instanceof String) {
            String dataStr = ((String) dataObj).trim();
            
            if (dataStr.isEmpty()) {
                return new java.sql.Timestamp(System.currentTimeMillis());
            }
            
            // 1. Tentar como número (milissegundos)
            try {
                long millis = Long.parseLong(dataStr);
                return new java.sql.Timestamp(millis);
            } catch (NumberFormatException e) {
                // Não é número, continuar
            }
            
            // 2. Formato padrão JDBC: yyyy-MM-dd HH:mm:ss[.fffffffff]
            try {
                return java.sql.Timestamp.valueOf(dataStr);
            } catch (IllegalArgumentException e) {
                // Não é formato padrão
            }
            
            // 3. Formato ISO 8601 com T: yyyy-MM-ddTHH:mm:ss
            if (dataStr.contains("T")) {
                try {
                    String normalized = dataStr.replace("T", " ");
                    // Remover timezone se existir
                    if (normalized.contains("+")) {
                        normalized = normalized.substring(0, normalized.indexOf("+"));
                    }
                    if (normalized.contains("Z")) {
                        normalized = normalized.replace("Z", "");
                    }
                    return java.sql.Timestamp.valueOf(normalized);
                } catch (IllegalArgumentException e) {
                    // Continuar tentando
                }
            }
            
            // 4. Formato brasileiro: dd/MM/yyyy HH:mm:ss
            if (dataStr.contains("/")) {
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    java.util.Date date = sdf.parse(dataStr);
                    return new java.sql.Timestamp(date.getTime());
                } catch (java.text.ParseException e) {
                    // Tentar só data sem hora
                    try {
                        java.text.SimpleDateFormat sdf2 = new java.text.SimpleDateFormat("dd/MM/yyyy");
                        java.util.Date date = sdf2.parse(dataStr);
                        return new java.sql.Timestamp(date.getTime());
                    } catch (java.text.ParseException e2) {
                        // Continuar
                    }
                }
            }
            
            // 5. Formato americano: MM/dd/yyyy HH:mm:ss
            // (menos comum, mas possível)
            
            LOGGER.warning("Não foi possível converter timestamp: '" + dataStr + "' - usando data atual");
        }
        
        // Fallback: usar data atual
        return new java.sql.Timestamp(System.currentTimeMillis());
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
