package com.inventario.util;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Utilitário para criar tabelas de itens compostos
 */
public class CriarTabelasItemComposto {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Criando Tabelas de Itens Compostos");
        System.out.println("========================================");
        
        try {
            // Usar conexão do sistema (configuracao_banco.json)
            try (Connection conn = DatabaseConnection.getConnection()) {
                System.out.println("✓ Conectado ao banco de dados: " + conn.getMetaData().getURL());
                
                try (Statement stmt = conn.createStatement()) {
                    
                    // 1. Criar tabela de itens compostos
                    System.out.println("\n1. Criando tabela_item_composto...");
                    stmt.execute("""
                        CREATE TABLE IF NOT EXISTS tabela_item_composto (
                            id SERIAL PRIMARY KEY,
                            id_patrimonio_principal INTEGER NOT NULL,
                            tipo_componente VARCHAR(100) NOT NULL,
                            descricao_componente VARCHAR(255) NOT NULL,
                            quantidade_esperada INTEGER NOT NULL DEFAULT 1,
                            obrigatorio BOOLEAN DEFAULT TRUE,
                            observacao TEXT,
                            data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT fk_item_composto_patrimonio 
                                FOREIGN KEY (id_patrimonio_principal) 
                                REFERENCES tabela_patrimonio(id) 
                                ON DELETE CASCADE,
                            CONSTRAINT chk_quantidade_positiva 
                                CHECK (quantidade_esperada > 0)
                        )
                        """);
                    System.out.println("   ✓ tabela_item_composto criada");
                    
                    // 2. Criar tabela de coleta de componentes
                    System.out.println("\n2. Criando tabela_coleta_componente...");
                    stmt.execute("""
                        CREATE TABLE IF NOT EXISTS tabela_coleta_componente (
                            id SERIAL PRIMARY KEY,
                            id_item_composto INTEGER NOT NULL,
                            id_inventario INTEGER NOT NULL,
                            id_coletor INTEGER NOT NULL,
                            quantidade_encontrada INTEGER NOT NULL DEFAULT 0,
                            status_componente VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
                            observacao_coleta TEXT,
                            data_coleta TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT fk_coleta_comp_item 
                                FOREIGN KEY (id_item_composto) 
                                REFERENCES tabela_item_composto(id) 
                                ON DELETE CASCADE,
                            CONSTRAINT fk_coleta_comp_inventario 
                                FOREIGN KEY (id_inventario) 
                                REFERENCES tabela_inventario(id) 
                                ON DELETE CASCADE,
                            CONSTRAINT fk_coleta_comp_coletor 
                                FOREIGN KEY (id_coletor) 
                                REFERENCES tabela_usuario(id) 
                                ON DELETE RESTRICT,
                            CONSTRAINT chk_status_componente 
                                CHECK (status_componente IN ('PENDENTE', 'COMPLETO', 'PARCIAL', 'FALTANTE')),
                            CONSTRAINT chk_quantidade_nao_negativa 
                                CHECK (quantidade_encontrada >= 0),
                            CONSTRAINT uk_coleta_componente_inventario 
                                UNIQUE (id_item_composto, id_inventario)
                        )
                        """);
                    System.out.println("   ✓ tabela_coleta_componente criada");
                    
                    // 3. Criar índices
                    System.out.println("\n3. Criando índices...");
                    try {
                        stmt.execute("CREATE INDEX IF NOT EXISTS idx_item_composto_patrimonio ON tabela_item_composto(id_patrimonio_principal)");
                        stmt.execute("CREATE INDEX IF NOT EXISTS idx_coleta_componente_item ON tabela_coleta_componente(id_item_composto)");
                        stmt.execute("CREATE INDEX IF NOT EXISTS idx_coleta_componente_inventario ON tabela_coleta_componente(id_inventario)");
                        stmt.execute("CREATE INDEX IF NOT EXISTS idx_coleta_componente_status ON tabela_coleta_componente(status_componente)");
                        System.out.println("   ✓ Índices criados");
                    } catch (Exception e) {
                        System.out.println("   ⚠ Índices já existem ou erro: " + e.getMessage());
                    }
                    
                    // 4. Inserir dados de exemplo
                    System.out.println("\n4. Inserindo dados de exemplo...");
                    try {
                        // Verificar se já existem dados
                        var rs = stmt.executeQuery("SELECT COUNT(*) FROM tabela_item_composto");
                        rs.next();
                        int count = rs.getInt(1);
                        
                        if (count == 0) {
                            // Conjunto escolar (patrimônio 1)
                            stmt.execute("""
                                INSERT INTO tabela_item_composto (id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio)
                                VALUES (1, 'CADEIRA', 'Cadeira escolar', 1, TRUE)
                                """);
                            stmt.execute("""
                                INSERT INTO tabela_item_composto (id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio)
                                VALUES (1, 'MESA', 'Mesa individual', 1, TRUE)
                                """);
                            
                            // Computador completo (patrimônio 2)
                            stmt.execute("""
                                INSERT INTO tabela_item_composto (id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio)
                                VALUES (2, 'MONITOR', 'Monitor LCD', 1, TRUE)
                                """);
                            stmt.execute("""
                                INSERT INTO tabela_item_composto (id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio)
                                VALUES (2, 'TECLADO', 'Teclado USB', 1, TRUE)
                                """);
                            stmt.execute("""
                                INSERT INTO tabela_item_composto (id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio)
                                VALUES (2, 'MOUSE', 'Mouse USB', 1, TRUE)
                                """);
                            stmt.execute("""
                                INSERT INTO tabela_item_composto (id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio)
                                VALUES (2, 'CPU', 'Gabinete CPU', 1, TRUE)
                                """);
                            System.out.println("   ✓ Dados de exemplo inseridos");
                        } else {
                            System.out.println("   ⚠ Dados já existem (" + count + " registros)");
                        }
                    } catch (Exception e) {
                        System.out.println("   ⚠ Erro ao inserir dados: " + e.getMessage());
                    }
                    
                    System.out.println("\n========================================");
                    System.out.println("✓ TABELAS CRIADAS COM SUCESSO!");
                    System.out.println("========================================");
                    System.out.println("\nTabelas disponíveis:");
                    System.out.println("  - tabela_item_composto");
                    System.out.println("  - tabela_coleta_componente");
                }
            }
            
        } catch (Exception e) {
            System.err.println("\n✗ ERRO: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
