package com.inventario.service;

import com.inventario.dao.PatrimonioDAORefactored;
import com.inventario.model.Patrimonio;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Serviço responsável por sincronizar dados do banco de dados
 * com o arquivo JSON de treinamento da IA
 */
public class DataSyncService {
    
    private final PatrimonioDAORefactored patrimonioDAO;
    private final ObjectMapper objectMapper;
    private final String jsonFilePath;
    
    public DataSyncService() {
        this.patrimonioDAO = new PatrimonioDAORefactored();
        this.objectMapper = new ObjectMapper();
        this.jsonFilePath = "ai_training_optimized.json";
    }
    
    public DataSyncService(String customJsonPath) {
        this.patrimonioDAO = new PatrimonioDAORefactored();
        this.objectMapper = new ObjectMapper();
        this.jsonFilePath = customJsonPath;
    }
    
    /**
     * Sincroniza todos os dados do banco com o arquivo JSON
     */
    public void syncAllData() throws SQLException, IOException {
        System.out.println("[SYNC] Iniciando sincronização de dados...");
        
        // Buscar dados do banco
        List<Patrimonio> patrimonios = patrimonioDAO.findAll();
        System.out.println("[SYNC] Encontrados " + patrimonios.size() + " patrimônios no banco");
        
        // Ler arquivo JSON atual
        File jsonFile = new File(jsonFilePath);
        if (!jsonFile.exists()) {
            System.err.println("[SYNC] Arquivo JSON não encontrado: " + jsonFilePath);
            return;
        }
        
        JsonNode rootNode = objectMapper.readTree(jsonFile);
        ObjectNode root = (ObjectNode) rootNode;
        
        // Atualizar seção de patrimônios
        updatePatrimoniosSection(root, patrimonios);
        
        // Salvar arquivo atualizado
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, root);
        System.out.println("[SYNC] Arquivo JSON atualizado com sucesso!");
    }
    
    /**
     * Atualiza a seção de patrimônios no JSON
     */
    private void updatePatrimoniosSection(ObjectNode root, List<Patrimonio> patrimonios) {
        // Navegar até knowledge_base.patrimonios
        ObjectNode knowledgeBase = (ObjectNode) root.get("knowledge_base");
        if (knowledgeBase == null) {
            knowledgeBase = objectMapper.createObjectNode();
            root.set("knowledge_base", knowledgeBase);
        }
        
        // Criar array de patrimônios
        ArrayNode patrimoniosArray = objectMapper.createArrayNode();
        
        // Converter patrimônios do banco para JSON
        for (Patrimonio patrimonio : patrimonios) {
            ObjectNode patrimonioNode = objectMapper.createObjectNode();
            
            patrimonioNode.put("numero_patrimonio", patrimonio.getNumero());
            patrimonioNode.put("descricao", patrimonio.getDescricao());
            patrimonioNode.put("marca", patrimonio.getMarca());
            patrimonioNode.put("modelo", patrimonio.getModelo());
            patrimonioNode.put("situacao", patrimonio.getStatus());
            patrimonioNode.put("setor", "IFMT"); // Valor padrão
            patrimonioNode.put("campus", "Campus Principal"); // Valor padrão
            
            if (patrimonio.getValorAquisicao() != null) {
                patrimonioNode.put("valor_aquisicao", patrimonio.getValorAquisicao().doubleValue());
            }
            
            if (patrimonio.getDataEntrada() != null) {
                patrimonioNode.put("data_aquisicao", patrimonio.getDataEntrada().getTime());
            }
            
            // Buscar nome do responsável se disponível
            String responsavel = "Não informado";
            if (patrimonio.getNomeResponsavel() != null && !patrimonio.getNomeResponsavel().trim().isEmpty()) {
                responsavel = patrimonio.getNomeResponsavel();
            }
            patrimonioNode.put("responsavel", responsavel);
            
            patrimoniosArray.add(patrimonioNode);
        }
        
        // Atualizar o JSON
        knowledgeBase.set("patrimonios", patrimoniosArray);
        
        System.out.println("[SYNC] Seção de patrimônios atualizada com " + patrimonios.size() + " itens");
    }
    
    /**
     * Gera estatísticas atualizadas baseadas nos dados do banco
     */
    public String generateUpdatedStatistics() throws SQLException {
        List<Patrimonio> patrimonios = patrimonioDAO.findAll();
        
        int totalPatrimonios = patrimonios.size();
        long ativos = patrimonios.stream().filter(p -> "Ativo".equals(p.getStatus())).count();
        long inativos = totalPatrimonios - ativos;
        
        double valorTotal = patrimonios.stream()
            .filter(p -> p.getValorAquisicao() != null)
            .mapToDouble(p -> p.getValorAquisicao().doubleValue())
            .sum();
            
        double valorMedio = totalPatrimonios > 0 ? valorTotal / totalPatrimonios : 0;
        
        // Contar setores únicos
        long setoresCadastrados = patrimonios.stream()
            .filter(p -> p.getNomeResponsavel() != null)
            .map(p -> p.getNomeResponsavel())
            .distinct()
            .count();
        
        StringBuilder stats = new StringBuilder();
        stats.append("📊 **Estatísticas Gerais do Inventário:**\n\n");
        stats.append("🔢 **Total de Patrimônios:** ").append(totalPatrimonios).append("\n");
        stats.append("✅ **Patrimônios Ativos:** ").append(ativos).append(" (").append(String.format("%.0f", (double)ativos/totalPatrimonios*100)).append("%)\n");
        stats.append("❌ **Patrimônios Inativos:** ").append(inativos).append(" (").append(String.format("%.0f", (double)inativos/totalPatrimonios*100)).append("%)\n");
        stats.append("💰 **Valor Total:** R$ ").append(String.format("%.2f", valorTotal)).append("\n");
        stats.append("📈 **Valor Médio:** R$ ").append(String.format("%.2f", valorMedio)).append("\n");
        stats.append("🏢 **Responsáveis Ativos:** ").append(setoresCadastrados).append("\n");
        stats.append("📅 **Última Atualização:** Agora\n\n");
        
        return stats.toString();
    }
    
    /**
     * Método para testar a sincronização
     */
    public static void main(String[] args) {
        try {
            DataSyncService syncService = new DataSyncService();
            syncService.syncAllData();
            
            System.out.println("\n=== ESTATÍSTICAS ATUALIZADAS ===");
            System.out.println(syncService.generateUpdatedStatistics());
            
        } catch (Exception e) {
            System.err.println("Erro durante sincronização: " + e.getMessage());
            e.printStackTrace();
        }
    }
}