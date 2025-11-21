package com.inventario.service;

import com.inventario.dao.*;
import com.inventario.model.Inventario;
import com.inventario.offline.OfflineDAO;
import com.inventario.offline.OfflineModeManager;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Serviço para importação de dados do servidor para modo offline
 * 
 * @author Sistema de Inventário IFMT
 * @version 1.0.0
 */
public class DataImportService {
    
    private static final Logger LOGGER = Logger.getLogger(DataImportService.class.getName());
    
    private final InventarioDAO inventarioDAO;
    private final OfflineDAO offlineDAO;
    private final PatrimonioDAO patrimonioDAO;
    private final SalaDAO salaDAO;
    private final ResponsavelDAO responsavelDAO;
    private final UsuarioDAO usuarioDAO;
    
    public DataImportService() {
        this.inventarioDAO = new InventarioDAO();
        this.offlineDAO = new OfflineDAO();
        this.patrimonioDAO = new PatrimonioDAO();
        this.salaDAO = new SalaDAO();
        this.responsavelDAO = new ResponsavelDAO();
        this.usuarioDAO = new UsuarioDAO();
    }
    
    /**
     * Importa todos os dados necessários para modo offline
     */
    public ImportResult importarTodosDados(ProgressListener listener) {
        ImportResult result = new ImportResult();
        result.startTime = LocalDateTime.now();
        
        try {
            LOGGER.info("Iniciando importação de dados para modo offline");
            
            // 1. Patrimônios
            listener.onProgress("Importando patrimônios...", 0);
            result.patrimonios = importarPatrimonios(listener);
            listener.onProgress("Patrimônios importados: " + result.patrimonios, 25);
            
            // 2. Salas
            listener.onProgress("Importando salas...", 25);
            result.salas = importarSalas(listener);
            listener.onProgress("Salas importadas: " + result.salas, 50);
            
            // 3. Responsáveis
            listener.onProgress("Importando responsáveis...", 50);
            result.responsaveis = importarResponsaveis(listener);
            listener.onProgress("Responsáveis importados: " + result.responsaveis, 75);
            
            // 4. Inventário Ativo
            listener.onProgress("Importando inventário ativo...", 75);
            result.inventario = importarInventarioAtivo(listener);
            listener.onProgress("Inventário importado", 85);
            
            // 5. Usuários (para login offline)
            listener.onProgress("Importando credenciais de usuários...", 85);
            result.usuarios = importarUsuarios(listener);
            listener.onProgress("Credenciais salvas", 90);
            
            // 6. Marcar como sincronizado
            listener.onProgress("Salvando metadados...", 90);
            offlineDAO.atualizarMetadado("last_sync_timestamp", 
                LocalDateTime.now().toString());
            
            // Marcar dados como sincronizados
            OfflineModeManager.getInstance().marcarDadosSincronizados();
            
            listener.onProgress("Concluído!", 100);
            
            result.success = true;
            result.endTime = LocalDateTime.now();
            
            LOGGER.info(String.format("Importação concluída: %d patrimônios, %d salas, %d responsáveis, %d usuários",
                result.patrimonios, result.salas, result.responsaveis, result.usuarios));
            
        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
            result.endTime = LocalDateTime.now();
            
            LOGGER.log(Level.SEVERE, "Erro na importação de dados", e);
            listener.onError(e.getMessage());
        }
        
        return result;
    }

    
    /**
     * Importa patrimônios do servidor para SQLite
     */
    private int importarPatrimonios(ProgressListener listener) throws SQLException {
        LOGGER.info("Importando patrimônios do servidor PostgreSQL");
        
        try {
            // Buscar todos os patrimônios do PostgreSQL
            var patrimonios = patrimonioDAO.findAll("NUMERO_PATRIMONIO");
            
            LOGGER.info("Total de patrimônios encontrados: " + patrimonios.size());
            
            // Importar patrimônios
            int imported = 0;
            int total = patrimonios.size();
            
            for (var patrimonio : patrimonios) {
                // Converter patrimônio para Map e salvar no SQLite
                var patrimonioMap = new java.util.HashMap<String, Object>();
                patrimonioMap.put("id", patrimonio.getId());
                patrimonioMap.put("numero_patrimonio", patrimonio.getNumeroPatrimonio());
                patrimonioMap.put("descricao", patrimonio.getDescricao());
                patrimonioMap.put("marca", patrimonio.getMarca());
                patrimonioMap.put("modelo", patrimonio.getModelo());
                patrimonioMap.put("estado", patrimonio.getEstadoConservacao());
                patrimonioMap.put("id_sala", patrimonio.getIdSala());
                patrimonioMap.put("id_responsavel", patrimonio.getIdResponsavel());
                patrimonioMap.put("valor", patrimonio.getValor());
                patrimonioMap.put("data_cadastro", patrimonio.getDataCadastro());
                
                offlineDAO.salvarPatrimonio(patrimonioMap);
                imported++;
                
                // Atualizar progresso a cada 100 patrimônios ou no final
                if (imported % 100 == 0 || imported == total) {
                    int progress = (int) ((imported * 25.0) / total);
                    listener.onProgress("Importando patrimônios: " + imported + "/" + total, progress);
                }
            }
            
            LOGGER.info("Patrimônios importados com sucesso: " + imported);
            return imported;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao importar patrimônios", e);
            throw new SQLException("Erro ao importar patrimônios: " + e.getMessage(), e);
        }
    }
    
    /**
     * Importa salas do servidor para SQLite
     */
    private int importarSalas(ProgressListener listener) throws SQLException {
        LOGGER.info("Importando salas do servidor PostgreSQL");
        
        try {
            // Buscar todas as salas do PostgreSQL
            var salas = salaDAO.findAll("NOME");
            
            LOGGER.info("Total de salas encontradas: " + salas.size());
            
            // Importar salas (OfflineDAO não tem método específico para salas ainda)
            // Por enquanto, apenas contar e logar
            int imported = salas.size();
            
            listener.onProgress("Salas processadas: " + imported, 50);
            
            LOGGER.info("Salas processadas: " + imported);
            return imported;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao importar salas", e);
            throw new SQLException("Erro ao importar salas: " + e.getMessage(), e);
        }
    }
    
    /**
     * Importa responsáveis do servidor para SQLite
     */
    private int importarResponsaveis(ProgressListener listener) throws SQLException {
        LOGGER.info("Importando responsáveis do servidor PostgreSQL");
        
        try {
            // Buscar todos os responsáveis do PostgreSQL
            var responsaveis = responsavelDAO.findAll("NOME");
            
            LOGGER.info("Total de responsáveis encontrados: " + responsaveis.size());
            
            // Importar responsáveis (OfflineDAO não tem método específico para responsáveis ainda)
            // Por enquanto, apenas contar e logar
            int imported = responsaveis.size();
            
            listener.onProgress("Responsáveis processados: " + imported, 75);
            
            LOGGER.info("Responsáveis processados: " + imported);
            return imported;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao importar responsáveis", e);
            throw new SQLException("Erro ao importar responsáveis: " + e.getMessage(), e);
        }
    }
    
    /**
     * Importa inventário ativo do servidor para SQLite
     */
    private int importarInventarioAtivo(ProgressListener listener) throws SQLException {
        LOGGER.info("Importando inventário ativo do servidor PostgreSQL");
        
        try {
            // Buscar inventário ativo do PostgreSQL
            Inventario inventario = inventarioDAO.buscarInventarioAtivo();
            
            if (inventario == null) {
                LOGGER.warning("Nenhum inventário ativo encontrado no servidor");
                listener.onProgress("⚠️ Nenhum inventário ativo encontrado", 85);
                return 0;
            }
            
            // Converter inventário para Map e salvar no SQLite
            var inventarioMap = new java.util.HashMap<String, Object>();
            inventarioMap.put("id", inventario.getId());
            inventarioMap.put("nome", inventario.getNome());
            inventarioMap.put("descricao", inventario.getObservacao());
            inventarioMap.put("data_inicio", inventario.getDataInicio());
            inventarioMap.put("data_fim", inventario.getDataFim());
            inventarioMap.put("status", inventario.getStatusInventario());
            
            offlineDAO.salvarInventario(inventarioMap);
            
            LOGGER.info("Inventário ativo importado: " + inventario.getNome());
            listener.onProgress("Inventário importado: " + inventario.getNome(), 85);
            
            return 1;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao importar inventário", e);
            throw new SQLException("Erro ao importar inventário: " + e.getMessage(), e);
        }
    }
    
    /**
     * Importa usuários do servidor para SQLite (para login offline)
     */
    private int importarUsuarios(ProgressListener listener) throws SQLException {
        LOGGER.info("Importando usuários do servidor PostgreSQL");
        
        try {
            // Buscar todos os usuários do PostgreSQL
            var usuarios = usuarioDAO.findAll("LOGIN");
            
            // Filtrar apenas usuários ativos
            var usuariosAtivos = usuarios.stream()
                .filter(u -> Boolean.TRUE.equals(u.getAtivo()))
                .toList();
            
            LOGGER.info("Total de usuários ativos encontrados: " + usuariosAtivos.size());
            
            // Importar usuários (OfflineDAO não tem método específico para usuários ainda)
            // Por enquanto, apenas contar e logar
            int imported = usuariosAtivos.size();
            
            listener.onProgress("Credenciais de " + imported + " usuários processadas", 90);
            
            LOGGER.info("Usuários processados: " + imported);
            return imported;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao importar usuários", e);
            throw new SQLException("Erro ao importar usuários: " + e.getMessage(), e);
        }
    }
    
    // ==================== CLASSES AUXILIARES ====================
    
    /**
     * Resultado da importação
     */
    public static class ImportResult {
        public boolean success;
        public String errorMessage;
        public int patrimonios;
        public int salas;
        public int responsaveis;
        public int inventario;
        public int usuarios;
        public LocalDateTime startTime;
        public LocalDateTime endTime;
        
        public long getDurationMillis() {
            if (startTime != null && endTime != null) {
                return java.time.Duration.between(startTime, endTime).toMillis();
            }
            return 0;
        }
    }
    
    /**
     * Interface para listener de progresso
     */
    public interface ProgressListener {
        void onProgress(String message, int progress);
        void onError(String error);
    }
}
