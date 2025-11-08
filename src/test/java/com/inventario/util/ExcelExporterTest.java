package com.inventario.util;

import javax.swing.*;
import java.util.*;

/**
 * Teste de exportação Excel simulando dados do inventário
 */
public class ExcelExporterTest {

    public static void main(String[] args) {
        System.out.println("=== TESTE DE EXPORTAÇÃO EXCEL ===");
        System.out.println("Simulando exportação de dados do inventário...\n");

        // Criar dados de teste simulando um inventário real
        List<Map<String, Object>> dadosInventario = criarDadosInventarioTeste();

        System.out.println("✅ Dados de teste criados: " + dadosInventario.size() + " itens");
        System.out.println("Mostrando amostra dos dados:\n");

        // Mostrar amostra dos primeiros 3 itens
        for (int i = 0; i < Math.min(3, dadosInventario.size()); i++) {
            Map<String, Object> item = dadosInventario.get(i);
            System.out.println("Item " + (i + 1) + ":");
            System.out.println("  Número: " + item.get("numero"));
            System.out.println("  Descrição: " + item.get("descricao"));
            System.out.println("  Marca/Modelo: " + item.get("marca_modelo"));
            System.out.println("  Estado: " + item.get("estado"));
            System.out.println("  Setor: " + item.get("setor"));
            System.out.println("  Responsável: " + item.get("responsavel"));
            System.out.println("  Situação: " + item.get("situacao"));
            System.out.println("  Valor: " + item.get("valor"));
            System.out.println();
        }

        // Definir estrutura do relatório
        String[] colunas = {
            "Número Patrimônio",
            "Descrição",
            "Marca/Modelo",
            "Estado",
            "Setor/Local",
            "Responsável",
            "Situação",
            "Valor"
        };

        String[] chaves = {
            "numero",
            "descricao",
            "marca_modelo",
            "estado",
            "setor",
            "responsavel",
            "situacao",
            "valor"
        };

        System.out.println("=== INICIANDO TESTE DE EXPORTAÇÃO ===");
        System.out.println("Aguarde a janela de diálogo...\n");

        // Executar teste na EDT (Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            try {
                // Criar frame invisível como parent
                JFrame frame = new JFrame("Teste Exportação");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(400, 300);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

                System.out.println("🔍 Testando ExcelExporter.exportarRelatorioCustomizado()...");

                // Testar exportação
                boolean sucesso = ExcelExporter.exportarRelatorioCustomizado(
                    dadosInventario,
                    colunas,
                    chaves,
                    "TESTE DE EXPORTAÇÃO - RELATÓRIO DE INVENTÁRIO",
                    "teste_inventario_" + System.currentTimeMillis(),
                    (JComponent) frame.getContentPane()
                );

                if (sucesso) {
                    System.out.println("\n✅ TESTE PASSOU!");
                    System.out.println("A exportação foi concluída com sucesso.");
                    System.out.println("Verifique o arquivo gerado.");
                } else {
                    System.out.println("\n❌ TESTE FALHOU!");
                    System.out.println("A exportação não foi concluída.");
                }

                // Perguntar se deseja testar exportação alternativa
                int opcao = JOptionPane.showConfirmDialog(
                    frame,
                    "Deseja testar também a exportação alternativa (CSV)?",
                    "Teste Adicional",
                    JOptionPane.YES_NO_OPTION
                );

                if (opcao == JOptionPane.YES_OPTION) {
                    System.out.println("\n🔍 Testando ExcelExporterAlternativo.exportarParaExcelCSV()...");

                    boolean sucessoCSV = ExcelExporterAlternativo.exportarParaExcelCSV(
                        dadosInventario,
                        colunas,
                        chaves,
                        "TESTE ALTERNATIVO - RELATÓRIO DE INVENTÁRIO (CSV)",
                        "teste_inventario_csv_" + System.currentTimeMillis(),
                        (JComponent) frame.getContentPane()
                    );

                    if (sucessoCSV) {
                        System.out.println("\n✅ TESTE CSV PASSOU!");
                    } else {
                        System.out.println("\n❌ TESTE CSV FALHOU!");
                    }
                }

                // Fechar frame após 2 segundos
                javax.swing.Timer timer = new javax.swing.Timer(2000, e -> {
                    frame.dispose();
                    System.out.println("\n=== TESTE CONCLUÍDO ===");
                    System.exit(0);
                });
                timer.setRepeats(false);
                timer.start();

            } catch (Exception e) {
                System.err.println("\n❌ ERRO DURANTE O TESTE:");
                System.err.println("Tipo: " + e.getClass().getSimpleName());
                System.err.println("Mensagem: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        });
    }

    /**
     * Cria dados de teste simulando um inventário real
     */
    private static List<Map<String, Object>> criarDadosInventarioTeste() {
        List<Map<String, Object>> dados = new ArrayList<>();

        // Dados de teste realistas
        String[][] dadosExemplo = {
            {"PAT001", "Notebook Dell Latitude 5420", "Dell Latitude 5420", "Bom", "TI", "João Silva", "ATIVO", "R$ 4.500,00"},
            {"PAT002", "Monitor LG 24 polegadas", "LG 24MK430H", "Bom", "TI", "João Silva", "ATIVO", "R$ 850,00"},
            {"PAT003", "Teclado Mecânico", "Logitech K835", "Bom", "TI", "João Silva", "ATIVO", "R$ 350,00"},
            {"PAT004", "Mouse Wireless", "Logitech MX Master 3", "Bom", "TI", "João Silva", "ATIVO", "R$ 450,00"},
            {"PAT005", "Impressora Multifuncional", "HP LaserJet Pro M428fdw", "Bom", "Administração", "Maria Santos", "ATIVO", "R$ 2.800,00"},
            {"PAT006", "Mesa de Escritório", "Móveis ABC Executive", "Bom", "Administração", "Maria Santos", "ATIVO", "R$ 1.200,00"},
            {"PAT007", "Cadeira Ergonômica", "Herman Miller Aeron", "Bom", "Administração", "Maria Santos", "ATIVO", "R$ 3.500,00"},
            {"PAT008", "Ar Condicionado Split", "Samsung 12000 BTUs", "Bom", "RH", "Pedro Costa", "ATIVO", "R$ 2.200,00"},
            {"PAT009", "Projetor Multimídia", "Epson PowerLite X49", "Regular", "Sala de Reuniões", "Ana Oliveira", "ATIVO", "R$ 3.200,00"},
            {"PAT010", "Telefone IP", "Cisco 7841", "Bom", "Recepção", "Carlos Mendes", "ATIVO", "R$ 650,00"},
            {"PAT011", "Servidor Dell PowerEdge", "Dell PowerEdge R740", "Bom", "Data Center", "João Silva", "ATIVO", "R$ 25.000,00"},
            {"PAT012", "Switch de Rede 48 portas", "Cisco Catalyst 2960", "Bom", "Data Center", "João Silva", "ATIVO", "R$ 8.500,00"},
            {"PAT013", "Notebook Lenovo ThinkPad", "Lenovo ThinkPad X1 Carbon", "Bom", "Diretoria", "Roberto Alves", "ATIVO", "R$ 7.800,00"},
            {"PAT014", "Tablet Samsung", "Samsung Galaxy Tab S7", "Bom", "Vendas", "Juliana Lima", "ATIVO", "R$ 3.200,00"},
            {"PAT015", "Smartphone iPhone", "Apple iPhone 13 Pro", "Bom", "Gerência", "Fernando Souza", "ATIVO", "R$ 6.500,00"},
            {"PAT016", "Câmera de Segurança", "Intelbras VIP 1220 D", "Bom", "Segurança", "Marcos Pereira", "ATIVO", "R$ 450,00"},
            {"PAT017", "DVR 16 Canais", "Intelbras MHDX 3116", "Bom", "Segurança", "Marcos Pereira", "ATIVO", "R$ 1.200,00"},
            {"PAT018", "Bebedouro Industrial", "IBBL FR600", "Regular", "Copa", "Limpeza", "ATIVO", "R$ 850,00"},
            {"PAT019", "Micro-ondas", "Electrolux 31L", "Bom", "Copa", "Limpeza", "ATIVO", "R$ 550,00"},
            {"PAT020", "Geladeira Duplex", "Brastemp BRM54HK", "Bom", "Copa", "Limpeza", "ATIVO", "R$ 2.800,00"}
        };

        for (String[] linha : dadosExemplo) {
            Map<String, Object> item = new HashMap<>();
            item.put("numero", linha[0]);
            item.put("descricao", linha[1]);
            item.put("marca_modelo", linha[2]);
            item.put("estado", linha[3]);
            item.put("setor", linha[4]);
            item.put("responsavel", linha[5]);
            item.put("situacao", linha[6]);
            item.put("valor", linha[7]);
            dados.add(item);
        }

        return dados;
    }
}
