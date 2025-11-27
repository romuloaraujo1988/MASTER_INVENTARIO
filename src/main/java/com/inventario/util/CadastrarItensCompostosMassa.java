package com.inventario.util;

import java.sql.*;
import java.util.*;

/**
 * Utilitário para cadastrar itens compostos em massa baseado na descrição
 * 
 * Exemplo de uso:
 * - Todos os patrimônios com descrição "CONJUNTO ESCOLAR..." terão
 *   componentes MESA e CADEIRA cadastrados automaticamente
 */
public class CadastrarItensCompostosMassa {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Cadastro em Massa de Itens Compostos");
        System.out.println("========================================\n");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("✓ Conectado ao banco de dados\n");
            
            // Definir os templates de itens compostos
            List<TemplateItemComposto> templates = criarTemplates();
            
            for (TemplateItemComposto template : templates) {
                processarTemplate(conn, template);
            }
            
            System.out.println("\n========================================");
            System.out.println("✓ PROCESSAMENTO CONCLUÍDO!");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("✗ ERRO: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Cria os templates de itens compostos
     * Cada template define:
     * - Padrão de descrição (LIKE)
     * - Lista de componentes a cadastrar
     */
    private static List<TemplateItemComposto> criarTemplates() {
        List<TemplateItemComposto> templates = new ArrayList<>();
        
        // Template 1: Conjunto Escolar CJA-06 (Mesa + Cadeira)
        TemplateItemComposto conjuntoEscolar = new TemplateItemComposto(
            "Conjunto Escolar CJA-06",
            "%CONJUNTO ESCOLAR%CJA-06%CONTENDO%MESA%CADEIRA%"
        );
        conjuntoEscolar.addComponente("MESA", "Mesa com tampo MDP 18mm, 450x600mm", 1, true);
        conjuntoEscolar.addComponente("CADEIRA", "Cadeira empilhável, assento e encosto em polipropileno", 1, true);
        templates.add(conjuntoEscolar);
        
        // Template 2: Conjunto Escolar genérico (Mesa + Cadeira)
        TemplateItemComposto conjuntoEscolarGenerico = new TemplateItemComposto(
            "Conjunto Escolar Genérico",
            "%CONJUNTO ESCOLAR%CONTENDO%MESA%CADEIRA%"
        );
        conjuntoEscolarGenerico.addComponente("MESA", "Mesa escolar", 1, true);
        conjuntoEscolarGenerico.addComponente("CADEIRA", "Cadeira escolar", 1, true);
        templates.add(conjuntoEscolarGenerico);
        
        // Template 3: Conjunto Escolar Plaxmetal
        TemplateItemComposto conjuntoPlaxmetal = new TemplateItemComposto(
            "Conjunto Escolar Plaxmetal",
            "%CONJUNTO ESCOLAR%COMPONENTES MESA E CADEIRA%PLAXMETAL%"
        );
        conjuntoPlaxmetal.addComponente("MESA", "Mesa bitrapezoidal em resina plástica", 1, true);
        conjuntoPlaxmetal.addComponente("CADEIRA", "Cadeira em resina plástica", 1, true);
        templates.add(conjuntoPlaxmetal);
        
        // Template 4: Conjunto Trapézio Professor
        TemplateItemComposto conjuntoTrapezio = new TemplateItemComposto(
            "Conjunto Trapézio Professor",
            "%CONJUNTO TRAPEZIO PROFESSOR%CONTENDO%MESA%CADEIRA%"
        );
        conjuntoTrapezio.addComponente("MESA", "Mesa professor tampo injetado 1180x600x600mm", 1, true);
        conjuntoTrapezio.addComponente("CADEIRA", "Cadeira professor em resina plástica", 1, true);
        templates.add(conjuntoTrapezio);
        
        // Template 5: Conjunto Refeitório 8 lugares
        TemplateItemComposto conjuntoRefeitorio = new TemplateItemComposto(
            "Conjunto Refeitório 8 Lugares",
            "%CONJUNTO REFEITORIO 8 LUGARES%CONTENDO%MESA%"
        );
        conjuntoRefeitorio.addComponente("MESA", "Mesa refeitório tampo tripartido 2400x800x760mm", 1, true);
        conjuntoRefeitorio.addComponente("CADEIRA", "Cadeira refeitório em resina plástica", 8, true);
        templates.add(conjuntoRefeitorio);
        
        // Template 6: Computador completo
        TemplateItemComposto computador = new TemplateItemComposto(
            "Computador Completo",
            "%COMPUTADOR%MONITOR%TECLADO%MOUSE%"
        );
        computador.addComponente("CPU", "Gabinete/CPU", 1, true);
        computador.addComponente("MONITOR", "Monitor", 1, true);
        computador.addComponente("TECLADO", "Teclado", 1, true);
        computador.addComponente("MOUSE", "Mouse", 1, true);
        templates.add(computador);
        
        return templates;
    }

    
    /**
     * Processa um template, cadastrando componentes para todos os patrimônios que correspondem
     */
    private static void processarTemplate(Connection conn, TemplateItemComposto template) throws SQLException {
        System.out.println("─────────────────────────────────────────");
        System.out.println("Processando: " + template.nome);
        System.out.println("Padrão: " + template.padraoDescricao);
        
        // Buscar patrimônios que correspondem ao padrão
        String sqlBusca = """
            SELECT id, numero, descricao 
            FROM tabela_patrimonio 
            WHERE UPPER(descricao) LIKE UPPER(?)
            AND id NOT IN (SELECT DISTINCT id_patrimonio_principal FROM tabela_item_composto)
            ORDER BY numero
            """;
        
        List<int[]> patrimonios = new ArrayList<>(); // [id]
        
        try (PreparedStatement stmt = conn.prepareStatement(sqlBusca)) {
            stmt.setString(1, template.padraoDescricao);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    patrimonios.add(new int[]{rs.getInt("id")});
                }
            }
        }
        
        System.out.println("Patrimônios encontrados: " + patrimonios.size());
        
        if (patrimonios.isEmpty()) {
            System.out.println("⚠ Nenhum patrimônio novo para processar (já cadastrados ou não encontrados)");
            return;
        }
        
        // Cadastrar componentes para cada patrimônio
        String sqlInsert = """
            INSERT INTO tabela_item_composto 
                (id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT DO NOTHING
            """;
        
        int totalCadastrados = 0;
        int totalComponentes = 0;
        
        try (PreparedStatement stmt = conn.prepareStatement(sqlInsert)) {
            conn.setAutoCommit(false);
            
            for (int[] pat : patrimonios) {
                int idPatrimonio = pat[0];
                
                for (Componente comp : template.componentes) {
                    stmt.setInt(1, idPatrimonio);
                    stmt.setString(2, comp.tipo);
                    stmt.setString(3, comp.descricao);
                    stmt.setInt(4, comp.quantidade);
                    stmt.setBoolean(5, comp.obrigatorio);
                    stmt.addBatch();
                    totalComponentes++;
                }
                totalCadastrados++;
                
                // Executar batch a cada 100 patrimônios
                if (totalCadastrados % 100 == 0) {
                    stmt.executeBatch();
                    System.out.println("  Processados: " + totalCadastrados + "/" + patrimonios.size());
                }
            }
            
            // Executar batch final
            stmt.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
        }
        
        System.out.println("✓ Cadastrados: " + totalCadastrados + " patrimônios, " + totalComponentes + " componentes");
    }
    
    /**
     * Classe auxiliar para representar um template de item composto
     */
    static class TemplateItemComposto {
        String nome;
        String padraoDescricao;
        List<Componente> componentes = new ArrayList<>();
        
        TemplateItemComposto(String nome, String padraoDescricao) {
            this.nome = nome;
            this.padraoDescricao = padraoDescricao;
        }
        
        void addComponente(String tipo, String descricao, int quantidade, boolean obrigatorio) {
            componentes.add(new Componente(tipo, descricao, quantidade, obrigatorio));
        }
    }
    
    /**
     * Classe auxiliar para representar um componente
     */
    static class Componente {
        String tipo;
        String descricao;
        int quantidade;
        boolean obrigatorio;
        
        Componente(String tipo, String descricao, int quantidade, boolean obrigatorio) {
            this.tipo = tipo;
            this.descricao = descricao;
            this.quantidade = quantidade;
            this.obrigatorio = obrigatorio;
        }
    }
}
