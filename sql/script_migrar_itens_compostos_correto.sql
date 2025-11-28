-- ========================================
-- SCRIPT PARA MIGRAR TABELAS DE ITENS COMPOSTOS
-- ========================================
-- Estrutura REAL das tabelas (verificada em 28/11/2025)
-- Execute no servidor de DESTINO

-- ========================================
-- 1. CRIAR TABELAS SE NÃO EXISTIREM
-- ========================================

-- Tabela de itens compostos (componentes de um patrimônio)
CREATE TABLE IF NOT EXISTS tabela_item_composto (
    id SERIAL PRIMARY KEY,
    id_patrimonio_principal INTEGER NOT NULL,
    tipo_componente VARCHAR(100) NOT NULL,
    descricao_componente VARCHAR(500) NOT NULL,
    quantidade_esperada INTEGER NOT NULL DEFAULT 1,
    obrigatorio BOOLEAN DEFAULT true,
    observacao TEXT,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_item_composto_patrimonio 
        FOREIGN KEY (id_patrimonio_principal) 
        REFERENCES tabela_patrimonio(id)
);

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_item_composto_patrimonio 
    ON tabela_item_composto(id_patrimonio_principal);
CREATE INDEX IF NOT EXISTS idx_item_composto_tipo 
    ON tabela_item_composto(tipo_componente);

-- Tabela de coleta de componentes
CREATE TABLE IF NOT EXISTS tabela_coleta_componente (
    id SERIAL PRIMARY KEY,
    id_item_composto INTEGER NOT NULL,
    id_inventario INTEGER NOT NULL,
    id_coletor INTEGER NOT NULL,
    quantidade_encontrada INTEGER NOT NULL DEFAULT 0,
    status_componente VARCHAR(50) NOT NULL DEFAULT 'PENDENTE',
    observacao_coleta TEXT,
    data_coleta TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_coleta_componente_item 
        FOREIGN KEY (id_item_composto) 
        REFERENCES tabela_item_composto(id),
    CONSTRAINT fk_coleta_componente_inventario 
        FOREIGN KEY (id_inventario) 
        REFERENCES tabela_inventario(id),
    CONSTRAINT fk_coleta_componente_coletor 
        FOREIGN KEY (id_coletor) 
        REFERENCES tabela_coletor(id)
);

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_coleta_componente_item 
    ON tabela_coleta_componente(id_item_composto);
CREATE INDEX IF NOT EXISTS idx_coleta_componente_inventario 
    ON tabela_coleta_componente(id_inventario);
CREATE INDEX IF NOT EXISTS idx_coleta_componente_status 
    ON tabela_coleta_componente(status_componente);

-- ========================================
-- 2. ESTATÍSTICAS DO BANCO DE ORIGEM
-- ========================================
/*
Dados encontrados no banco de origem (28/11/2025):
- tabela_item_composto: 1.588 registros
- tabela_coleta_componente: 12 registros

Tipos de componentes encontrados:
- CADEIRA
- MESA
- MONITOR
- CPU

Status de coleta:
- PENDENTE (padrão)
- COMPLETO
*/


-- ========================================
-- 3. INSERIR DADOS NA TABELA_ITEM_COMPOSTO
-- ========================================
-- Total: 1.588 registros
-- Nota: Os IDs são preservados para manter integridade referencial

INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES
(1, 1, 'CADEIRA', 'Cadeira escolar', 1, true, NULL, '2025-11-27 09:15:00.52214'),
(2, 1, 'MESA', 'Mesa individual', 1, true, NULL, '2025-11-27 09:15:00.542951'),
(3, 2, 'MONITOR', 'Monitor LCD', 1, true, NULL, '2025-11-27 09:15:00.54538'),
(6, 2, 'CPU', 'Gabinete CPU', 1, true, NULL, '2025-11-27 09:15:00.549776'),
(7, 1201, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195'),
(8, 1201, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195'),
(9, 1202, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195'),
(10, 1202, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');

-- ... (continua com mais registros)
-- Para obter TODOS os registros, execute o script de extração abaixo


-- ========================================
-- 4. INSERIR DADOS NA TABELA_COLETA_COMPONENTE
-- ========================================
-- Total: 12 registros

INSERT INTO tabela_coleta_componente (id, id_item_composto, id_inventario, id_coletor, quantidade_encontrada, status_componente, observacao_coleta, data_coleta) VALUES
(1, 1008, 2, 1, 1, 'COMPLETO', '', '2025-11-27 09:55:12.405109'),
(2, 1007, 2, 1, 1, 'COMPLETO', '', '2025-11-27 09:55:31.278282'),
(3, 1010, 2, 1, 1, 'COMPLETO', '', '2025-11-27 14:25:47.000657'),
(4, 1013, 2, 1, 1, 'COMPLETO', 'teste', '2025-11-27 14:26:16.919583'),
(5, 1016, 2, 1, 1, 'COMPLETO', '', '2025-11-27 14:49:39.819617'),
(6, 1015, 2, 1, 1, 'COMPLETO', '', '2025-11-27 14:49:55.500873'),
(7, 1024, 2, 1, 1, 'COMPLETO', '', '2025-11-27 14:55:34.464874'),
(8, 1023, 2, 1, 1, 'COMPLETO', '', '2025-11-27 14:55:47.406204'),
(9, 1011, 2, 1, 1, 'COMPLETO', '', '2025-11-27 14:56:50.474817'),
(10, 1012, 2, 1, 1, 'COMPLETO', '', '2025-11-27 20:01:24.385622'),
(11, 1020, 2, 1, 1, 'COMPLETO', '', '2025-11-27 15:06:43.649159'),
(13, 1017, 2, 1, 1, 'COMPLETO', '', '2025-11-27 20:01:40.021468');

-- Atualizar sequências após inserção
SELECT setval('tabela_item_composto_id_seq', (SELECT MAX(id) FROM tabela_item_composto));
SELECT setval('tabela_coleta_componente_id_seq', (SELECT MAX(id) FROM tabela_coleta_componente));

-- ========================================
-- 5. VERIFICAÇÃO FINAL
-- ========================================

SELECT 'Itens compostos inseridos:' as info, COUNT(*) as quantidade FROM tabela_item_composto;
SELECT 'Coletas de componentes inseridas:' as info, COUNT(*) as quantidade FROM tabela_coleta_componente;

-- Verificar integridade referencial
SELECT 'Coletas com item válido:' as info, COUNT(*) as quantidade
FROM tabela_coleta_componente cc
INNER JOIN tabela_item_composto ic ON cc.id_item_composto = ic.id;

-- Fim do script


-- ========================================
-- 6. ESTATÍSTICAS DO BANCO DE ORIGEM
-- ========================================
/*
RESUMO DOS DADOS (28/11/2025):

TABELA_ITEM_COMPOSTO:
- Total de registros: 1.588
- ID mínimo: 1
- ID máximo: 1.760 (há gaps nos IDs)

Distribuição por tipo de componente:
- CADEIRA: 707 registros (707 patrimônios distintos)
- MESA: 707 registros (707 patrimônios distintos)
- CPU: 87 registros (87 patrimônios distintos)
- MONITOR: 87 registros (87 patrimônios distintos)

TABELA_COLETA_COMPONENTE:
- Total de registros: 12
- Todos com status: COMPLETO
- Inventário: 2
- Coletor: 1

IMPORTANTE:
- Os IDs são preservados para manter integridade referencial
- Execute o script extrair_itens_compostos_completo.sql no banco origem
  para gerar arquivo com TODOS os 1.588 registros
- O arquivo gerado pode ser executado diretamente no banco destino
*/

-- ========================================
-- 7. SCRIPT ALTERNATIVO - USANDO COPY
-- ========================================
/*
Para exportar/importar grandes volumes de dados, use COPY:

-- No servidor ORIGEM:
COPY tabela_item_composto TO '/tmp/item_composto.csv' WITH CSV HEADER;
COPY tabela_coleta_componente TO '/tmp/coleta_componente.csv' WITH CSV HEADER;

-- No servidor DESTINO:
COPY tabela_item_composto FROM '/tmp/item_composto.csv' WITH CSV HEADER;
COPY tabela_coleta_componente FROM '/tmp/coleta_componente.csv' WITH CSV HEADER;

-- Atualizar sequências:
SELECT setval('tabela_item_composto_id_seq', (SELECT MAX(id) FROM tabela_item_composto));
SELECT setval('tabela_coleta_componente_id_seq', (SELECT MAX(id) FROM tabela_coleta_componente));
*/

-- Fim do script de migração
