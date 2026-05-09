package com.inventario.sihcp.service;

import com.inventario.sihcp.service.DataImportService.ImportResult;
import com.inventario.sihcp.service.DataImportService.ProgressListener;

/**
 * Teste simples e direto do DataImportService
 */
public class DataImportServiceTest {
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║         TESTE DIRETO DO DATA IMPORT SERVICE               ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        try {
            // Criar instância do serviço
            System.out.println("1. Criando DataImportService...");
            DataImportService service = new DataImportService();
            System.out.println("   ✓ Service criado: " + service);
            System.out.println();
            
            // Criar listener
            System.out.println("2. Criando ProgressListener...");
            ProgressListener listener = new ProgressListener() {
                @Override
                public void onProgress(String message, int progress) {
                    System.out.println("   [" + progress + "%] " + message);
                }
                
                @Override
                public void onError(String error) {
                    System.err.println("   [ERRO] " + error);
                }
            };
            System.out.println("   ✓ Listener criado");
            System.out.println();
            
            // Executar importação
            System.out.println("3. Iniciando importação...");
            System.out.println("   (Aguarde, isso pode levar alguns minutos)");
            System.out.println();
            
            ImportResult result = service.importarTodosDados(listener);
            
            // Mostrar resultado
            System.out.println();
            System.out.println("╔════════════════════════════════════════════════════════════╗");
            System.out.println("║                    RESULTADO FINAL                         ║");
            System.out.println("╚════════════════════════════════════════════════════════════╝");
            System.out.println();
            System.out.println("Status: " + (result.success ? "✓ SUCESSO" : "✗ FALHA"));
            System.out.println();
            
            if (result.success) {
                System.out.println("Patrimônios importados: " + result.patrimonios);
                System.out.println("Salas importadas: " + result.salas);
                System.out.println("Responsáveis importados: " + result.responsaveis);
                System.out.println("Inventários importados: " + result.inventario);
                System.out.println("Usuários importados: " + result.usuarios);
                System.out.println();
                System.out.println("Tempo total: " + result.getDurationMillis() + "ms");
            } else {
                System.err.println("Erro: " + result.errorMessage);
            }
            
            System.out.println();
            System.out.println("╔════════════════════════════════════════════════════════════╗");
            System.out.println("║                    TESTE CONCLUÍDO                         ║");
            System.out.println("╚════════════════════════════════════════════════════════════╝");
            
        } catch (Exception e) {
            System.err.println();
            System.err.println("╔════════════════════════════════════════════════════════════╗");
            System.err.println("║                    ERRO NO TESTE                           ║");
            System.err.println("╚════════════════════════════════════════════════════════════╝");
            System.err.println();
            System.err.println("Mensagem: " + e.getMessage());
            System.err.println();
            System.err.println("Stack trace:");
            e.printStackTrace();
        }
    }
}
