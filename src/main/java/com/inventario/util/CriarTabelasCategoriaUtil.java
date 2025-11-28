package com.inventario.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Utilitário para criar tabelas de categoria e subcategoria
 * Executa o script SQL diretamente via JDBC
 */
public class CriarTabelasCategoriaUtil {
    
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/sispatrimonio";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "Romulo@2020";
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("CRIANDO TABELAS DE CATEGORIA");
        System.out.println("========================================");
        System.out.println();
        
        try {
            // Carregar driver PostgreSQL
            Class.forName("org.postgresql.Driver");
            
            // Conectar ao banco
            System.out.println("Conectando ao banco de dados...");
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("✓ Conectado com sucesso!");
            System.out.println();
            
            // Executar comandos SQL
            Statement stmt = conn.createStatement();
            
            // 1. Criar tabela de categorias
            System.out.println("Criando tabela de categorias...");
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS tabela_categoria_patrimonio (" +
                "    id SERIAL PRIMARY KEY," +
                "    nome VARCHAR(100) NOT NULL UNIQUE," +
                "    descricao TEXT," +
                "    icone VARCHAR(50)," +
                "    cor VARCHAR(20)," +
                "    ordem_exibicao INTEGER DEFAULT 0," +
                "    ativo BOOLEAN DEFAULT TRUE," +
                "    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );
            System.out.println("✓ Tabela tabela_categoria_patrimonio criada");
            
            // 2. Criar tabela de subcategorias
            System.out.println("Criando tabela de subcategorias...");
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS tabela_subcategoria_patrimonio (" +
                "    id SERIAL PRIMARY KEY," +
                "    id_categoria INTEGER NOT NULL," +
                "    nome VARCHAR(100) NOT NULL," +
                "    descricao TEXT," +
                "    ordem_exibicao INTEGER DEFAULT 0," +
                "    ativo BOOLEAN DEFAULT TRUE," +
                "    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    FOREIGN KEY (id_categoria) REFERENCES tabela_categoria_patrimonio(id) ON DELETE CASCADE," +
                "    UNIQUE(id_categoria, nome)" +
                ")"
            );
            System.out.println("✓ Tabela tabela_subcategoria_patrimonio criada");
            
            // 3. Criar índices
            System.out.println("Criando índices...");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_categoria_ativo ON tabela_categoria_patrimonio(ativo)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_categoria_ordem ON tabela_categoria_patrimonio(ordem_exibicao)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_subcategoria_categoria ON tabela_subcategoria_patrimonio(id_categoria)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_subcategoria_ativo ON tabela_subcategoria_patrimonio(ativo)");
            System.out.println("✓ Índices criados");
            
            // 4. Inserir categorias principais
            System.out.println();
            System.out.println("Inserindo categorias principais...");
            
            String[] categorias = {
                "('MOBILIÁRIO', 'Móveis e mobiliário em geral', 'chair', '#795548', 1)",
                "('INFORMÁTICA', 'Equipamentos de informática e tecnologia', 'computer', '#2196F3', 2)",
                "('AUDIOVISUAL', 'Equipamentos audiovisuais e multimídia', 'videocam', '#9C27B0', 3)",
                "('LABORATÓRIO', 'Equipamentos e instrumentos de laboratório', 'science', '#4CAF50', 4)",
                "('CLIMATIZAÇÃO', 'Equipamentos de climatização e ventilação', 'ac_unit', '#00BCD4', 5)",
                "('ACERVO', 'Acervo bibliográfico e publicações', 'book', '#FF9800', 6)",
                "('VEÍCULO', 'Veículos automotores', 'directions_car', '#F44336', 7)",
                "('ELETRODOMÉSTICO', 'Eletrodomésticos e eletroeletrônicos', 'kitchen', '#607D8B', 8)",
                "('OUTROS', 'Outros patrimônios não classificados', 'category', '#9E9E9E', 99)"
            };
            
            for (String categoria : categorias) {
                stmt.execute(
                    "INSERT INTO tabela_categoria_patrimonio (nome, descricao, icone, cor, ordem_exibicao) " +
                    "VALUES " + categoria + " ON CONFLICT (nome) DO NOTHING"
                );
            }
            System.out.println("✓ " + categorias.length + " categorias inseridas");
            
            // 5. Inserir subcategorias
            System.out.println("Inserindo subcategorias...");
            
            inserirSubcategorias(stmt, "MOBILIÁRIO", new String[][]{
                {"Cadeira", "Cadeiras fixas, giratórias e de laboratório", "1"},
                {"Mesa", "Mesas de escritório, desenho e impressora", "2"},
                {"Conjunto Escolar", "Conjuntos escolares (mesa + cadeira)", "3"},
                {"Armário/Estante", "Armários, estantes e guarda-volumes", "4"},
                {"Poltrona", "Poltronas operacionais e giratórias", "5"},
                {"Banqueta", "Banquetas altas e giratórias", "6"},
                {"Outros", "Outros móveis não classificados", "99"}
            });
            
            inserirSubcategorias(stmt, "INFORMÁTICA", new String[][]{
                {"Desktop", "Computadores desktop e CPUs", "1"},
                {"Notebook", "Notebooks e chromebooks", "2"},
                {"Monitor", "Monitores e telas", "3"},
                {"Equipamento de Rede", "Roteadores, switches e access points", "4"},
                {"Impressora/Scanner", "Impressoras, scanners e multifuncionais", "5"},
                {"Webcam", "Webcams e câmeras", "6"},
                {"Periféricos", "Mouse, teclado, mesa digitalizadora", "7"},
                {"Outros", "Outros equipamentos de informática", "99"}
            });
            
            inserirSubcategorias(stmt, "AUDIOVISUAL", new String[][]{
                {"Projetor", "Projetores e datashows", "1"},
                {"Tela de Projeção", "Telas de projeção retráteis e fixas", "2"},
                {"Quadro/Lousa", "Quadros brancos, lousas e murais", "3"},
                {"Caixa de Som", "Caixas de som e sistemas de áudio", "4"},
                {"Outros", "Outros equipamentos audiovisuais", "99"}
            });
            
            inserirSubcategorias(stmt, "LABORATÓRIO", new String[][]{
                {"Microscópio", "Microscópios biológicos e estereoscópicos", "1"},
                {"Instrumento de Medição", "Paquímetros, balanças e medidores", "2"},
                {"Vidraria", "Vidrarias e recipientes de laboratório", "3"},
                {"Equipamento Específico", "Equipamentos específicos de laboratório", "4"},
                {"Outros", "Outros equipamentos de laboratório", "99"}
            });
            
            inserirSubcategorias(stmt, "CLIMATIZAÇÃO", new String[][]{
                {"Ar Condicionado", "Ar condicionado split, janela e piso-teto", "1"},
                {"Ventilador", "Ventiladores de teto, parede e coluna", "2"},
                {"Outros", "Outros equipamentos de climatização", "99"}
            });
            
            inserirSubcategorias(stmt, "ACERVO", new String[][]{
                {"Livro", "Livros didáticos e técnicos", "1"},
                {"Dicionário", "Dicionários e enciclopédias", "2"},
                {"Revista", "Revistas e periódicos", "3"},
                {"Outros", "Outras publicações", "99"}
            });
            
            inserirSubcategorias(stmt, "VEÍCULO", new String[][]{
                {"Veículo Leve", "Carros, vans e utilitários", "1"},
                {"Veículo Pesado", "Ônibus, caminhões e tratores", "2"},
                {"Outros", "Outros veículos", "99"}
            });
            
            inserirSubcategorias(stmt, "ELETRODOMÉSTICO", new String[][]{
                {"Refrigeração", "Geladeiras, freezers e bebedouros", "1"},
                {"Aquecimento", "Micro-ondas, fornos e cafeteiras", "2"},
                {"Outros", "Outros eletrodomésticos", "99"}
            });
            
            inserirSubcategorias(stmt, "OUTROS", new String[][]{
                {"Sem Subcategoria", "Itens sem subcategoria definida", "1"}
            });
            
            System.out.println("✓ Subcategorias inseridas");
            
            // 6. Criar views
            System.out.println();
            System.out.println("Criando views...");
            
            stmt.execute(
                "CREATE OR REPLACE VIEW view_categorias_com_contagem AS " +
                "SELECT c.id, c.nome, c.descricao, c.icone, c.cor, c.ordem_exibicao, c.ativo, " +
                "COUNT(s.id) as total_subcategorias " +
                "FROM tabela_categoria_patrimonio c " +
                "LEFT JOIN tabela_subcategoria_patrimonio s ON c.id = s.id_categoria AND s.ativo = TRUE " +
                "WHERE c.ativo = TRUE " +
                "GROUP BY c.id, c.nome, c.descricao, c.icone, c.cor, c.ordem_exibicao, c.ativo " +
                "ORDER BY c.ordem_exibicao"
            );
            
            stmt.execute(
                "CREATE OR REPLACE VIEW view_categorias_subcategorias AS " +
                "SELECT c.id as categoria_id, c.nome as categoria_nome, c.icone as categoria_icone, " +
                "c.cor as categoria_cor, s.id as subcategoria_id, s.nome as subcategoria_nome, " +
                "s.descricao as subcategoria_descricao, c.ordem_exibicao as categoria_ordem, " +
                "s.ordem_exibicao as subcategoria_ordem " +
                "FROM tabela_categoria_patrimonio c " +
                "LEFT JOIN tabela_subcategoria_patrimonio s ON c.id = s.id_categoria " +
                "WHERE c.ativo = TRUE AND (s.ativo = TRUE OR s.id IS NULL) " +
                "ORDER BY c.ordem_exibicao, s.ordem_exibicao"
            );
            
            System.out.println("✓ Views criadas");
            
            // 7. Verificar resultado
            System.out.println();
            System.out.println("========================================");
            System.out.println("VERIFICANDO RESULTADO");
            System.out.println("========================================");
            System.out.println();
            
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM tabela_categoria_patrimonio");
            if (rs.next()) {
                System.out.println("✓ Categorias criadas: " + rs.getInt(1));
            }
            
            rs = stmt.executeQuery("SELECT COUNT(*) FROM tabela_subcategoria_patrimonio");
            if (rs.next()) {
                System.out.println("✓ Subcategorias criadas: " + rs.getInt(1));
            }
            
            System.out.println();
            System.out.println("Categorias com subcategorias:");
            System.out.println("----------------------------------------");
            
            rs = stmt.executeQuery("SELECT nome, total_subcategorias FROM view_categorias_com_contagem ORDER BY ordem_exibicao");
            while (rs.next()) {
                System.out.printf("%-20s | %d subcategorias%n", rs.getString(1), rs.getInt(2));
            }
            
            System.out.println();
            System.out.println("========================================");
            System.out.println("✓ SUCESSO! TABELAS CRIADAS");
            System.out.println("========================================");
            
            // Fechar conexões
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (Exception e) {
            System.err.println();
            System.err.println("========================================");
            System.err.println("✗ ERRO AO CRIAR TABELAS");
            System.err.println("========================================");
            System.err.println();
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void inserirSubcategorias(Statement stmt, String nomeCategoria, String[][] subcategorias) throws Exception {
        for (String[] sub : subcategorias) {
            String sql = String.format(
                "INSERT INTO tabela_subcategoria_patrimonio (id_categoria, nome, descricao, ordem_exibicao) " +
                "SELECT id, '%s', '%s', %s FROM tabela_categoria_patrimonio WHERE nome = '%s' " +
                "ON CONFLICT (id_categoria, nome) DO NOTHING",
                sub[0].replace("'", "''"), 
                sub[1].replace("'", "''"), 
                sub[2], 
                nomeCategoria
            );
            stmt.execute(sql);
        }
    }
}
