-- =====================================================
-- PADRÕES DE DETECÇÃO PADRÃO
-- Módulo de Itens Compostos
-- =====================================================
-- Este arquivo insere padrões pré-configurados para
-- detecção automática de itens compostos
-- =====================================================
-- Data de Criação: 27/11/2025
-- Versão: 1.0
-- =====================================================

-- =====================================================
-- 1. PADRÃO: MESA COM CADEIRAS
-- =====================================================

INSERT INTO TABELA_PADRAO_DETECCAO (
    NOME, 
    DESCRICAO_PADRAO, 
    TIPO_PADRAO, 
    ATIVO, 
    PRIORIDADE
) VALUES (
    'Mesa com Cadeiras',
    'MESA.*COM.*CADEIRA|MESA.*E.*CADEIRA|CONJUNTO.*MESA.*CADEIRA',
    'REGEX',
    TRUE,
    10
) ON CONFLICT (NOME) DO NOTHING;

-- Componentes do padrão "Mesa com Cadeiras"
INSERT INTO TABELA_COMPONENTE_PADRAO (
    ID_PADRAO_DETECCAO,
    TIPO,
    DESCRICAO,
    QUANTIDADE_PADRAO,
    ORDEM
)
SELECT 
    p.ID,
    'MESA',
    'Mesa escolar',
    1,
    1
FROM TABELA_PADRAO_DETECCAO p
WHERE p.NOME = 'Mesa com Cadeiras'
ON CONFLICT DO NOTHING;

INSERT INTO TABELA_COMPONENTE_PADRAO (
    ID_PADRAO_DETECCAO,
    TIPO,
    DESCRICAO,
    QUANTIDADE_PADRAO,
    ORDEM
)
SELECT 
    p.ID,
    'CADEIRA',
    'Cadeira escolar',
    4,
    2
FROM TABELA_PADRAO_DETECCAO p
WHERE p.NOME = 'Mesa com Cadeiras'
ON CONFLICT DO NOTHING;

-- =====================================================
-- 2. PADRÃO: ESTAÇÃO DE TRABALHO
-- =====================================================

INSERT INTO TABELA_PADRAO_DETECCAO (
    NOME, 
    DESCRICAO_PADRAO, 
    TIPO_PADRAO, 
    ATIVO, 
    PRIORIDADE
) VALUES (
    'Estação de Trabalho',
    'ESTACAO.*TRABALHO|WORKSTATION|POSTO.*TRABALHO',
    'REGEX',
    TRUE,
    9
) ON CONFLICT (NOME) DO NOTHING;

-- Componentes do padrão "Estação de Trabalho"
INSERT INTO TABELA_COMPONENTE_PADRAO (
    ID_PADRAO_DETECCAO,
    TIPO,
    DESCRICAO,
    QUANTIDADE_PADRAO,
    ORDEM
)
SELECT 
    p.ID,
    'MESA',
    'Mesa de escritório',
    1,
    1
FROM TABELA_PADRAO_DETECCAO p
WHERE p.NOME = 'Estação de Trabalho'
ON CONFLICT DO NOTHING;

INSERT INTO TABELA_COMPONENTE_PADRAO (
    ID_PADRAO_DETECCAO,
    TIPO,
    DESCRICAO,
    QUANTIDADE_PADRAO,
    ORDEM
)
SELECT 
    p.ID,
    'CADEIRA',
    'Cadeira de escritório',
    1,
    2
FROM TABELA_PADRAO_DETECCAO p
WHERE p.NOME = 'Estação de Trabalho'
ON CONFLICT DO NOTHING;

INSERT INTO TABELA_COMPONENTE_PADRAO (
    ID_PADRAO_DETECCAO,
    TIPO,
    DESCRICAO,
    QUANTIDADE_PADRAO,
    ORDEM
)
SELECT 
    p.ID,
    'COMPUTADOR',
    'Computador desktop',
    1,
    3
FROM TABELA_PADRAO_DETECCAO p
WHERE p.NOME = 'Estação de Trabalho'
ON CONFLICT DO NOTHING;

-- =====================================================
-- 3. PADRÃO: COMPUTADOR COMPLETO
-- =====================================================

INSERT INTO TABELA_PADRAO_DETECCAO (
    NOME, 
    DESCRICAO_PADRAO, 
    TIPO_PADRAO, 
    ATIVO, 
    PRIORIDADE
) VALUES (
    'Computador Completo',
    'COMPUTADOR.*(MONITOR|TECLADO|MOUSE)|PC.*COMPLETO',
    'REGEX',
    TRUE,
    8
) ON CONFLICT (NOME) DO NOTHING;

-- Componentes do padrão "Computador Completo"
INSERT INTO TABELA_COMPONENTE_PADRAO (
    ID_PADRAO_DETECCAO,
    TIPO,
    DESCRICAO,
    QUANTIDADE_PADRAO,
    ORDEM
)
SELECT 
    p.ID,
    'COMPUTADOR',
    'Computador desktop',
    1,
    1
FROM TABELA_PADRAO_DETECCAO p
WHERE p.NOME = 'Computador Completo'
ON CONFLICT DO NOTHING;

INSERT INTO TABELA_COMPONENTE_PADRAO (
    ID_PADRAO_DETECCAO,
    TIPO,
    DESCRICAO,
    QUANTIDADE_PADRAO,
    ORDEM
)
SELECT 
    p.ID,
    'MONITOR',
    'Monitor',
    1,
    2
FROM TABELA_PADRAO_DETECCAO p
WHERE p.NOME = 'Computador Completo'
ON CONFLICT DO NOTHING;

INSERT INTO TABELA_COMPONENTE_PADRAO (
    ID_PADRAO_DETECCAO,
    TIPO,
    DESCRICAO,
    QUANTIDADE_PADRAO,
    ORDEM
)
SELECT 
    p.ID,
    'TECLADO',
    'Teclado',
    1,
    3
FROM TABELA_PADRAO_DETECCAO p
WHERE p.NOME = 'Computador Completo'
ON CONFLICT DO NOTHING;

INSERT INTO TABELA_COMPONENTE_PADRAO (
    ID_PADRAO_DETECCAO,
    TIPO,
    DESCRICAO,
    QUANTIDADE_PADRAO,
    ORDEM
)
SELECT 
    p.ID,
    'MOUSE',
    'Mouse',
    1,
    4
FROM TABELA_PADRAO_DETECCAO p
WHERE p.NOME = 'Computador Completo'
ON CONFLICT DO NOTHING;

-- =====================================================
-- 4. PADRÃO: CONJUNTO DE LABORATÓRIO
-- =====================================================

INSERT INTO TABELA_PADRAO_DETECCAO (
    NOME, 
    DESCRICAO_PADRAO, 
    TIPO_PADRAO, 
    ATIVO, 
    PRIORIDADE
) VALUES (
    'Conjunto de Laboratório',
    'CONJUNTO.*LABORATORIO|KIT.*LAB|BANCADA.*COMPLETA',
    'REGEX',
    TRUE,
    7
) ON CONFLICT (NOME) DO NOTHING;

-- Componentes do padrão "Conjunto de Laboratório"
INSERT INTO TABELA_COMPONENTE_PADRAO (
    ID_PADRAO_DETECCAO,
    TIPO,
    DESCRICAO,
    QUANTIDADE_PADRAO,
    ORDEM
)
SELECT 
    p.ID,
    'BANCADA',
    'Bancada de laboratório',
    1,
    1
FROM TABELA_PADRAO_DETECCAO p
WHERE p.NOME = 'Conjunto de Laboratório'
ON CONFLICT DO NOTHING;

INSERT INTO TABELA_COMPONENTE_PADRAO (
    ID_PADRAO_DETECCAO,
    TIPO,
    DESCRICAO,
    QUANTIDADE_PADRAO,
    ORDEM
)
SELECT 
    p.ID,
    'BANQUETA',
    'Banqueta',
    2,
    2
FROM TABELA_PADRAO_DETECCAO p
WHERE p.NOME = 'Conjunto de Laboratório'
ON CONFLICT DO NOTHING;

-- =====================================================
-- 5. PADRÃO: ARMÁRIO COM GAVETAS
-- =====================================================

INSERT INTO TABELA_PADRAO_DETECCAO (
    NOME, 
    DESCRICAO_PADRAO, 
    TIPO_PADRAO, 
    ATIVO, 
    PRIORIDADE
) VALUES (
    'Armário com Gavetas',
    'ARMARIO.*GAVETA|GAVETEIRO',
    'REGEX',
    TRUE,
    6
) ON CONFLICT (NOME) DO NOTHING;

-- Componentes do padrão "Armário com Gavetas"
INSERT INTO TABELA_COMPONENTE_PADRAO (
    ID_PADRAO_DETECCAO,
    TIPO,
    DESCRICAO,
    QUANTIDADE_PADRAO,
    ORDEM
)
SELECT 
    p.ID,
    'ARMARIO',
    'Armário',
    1,
    1
FROM TABELA_PADRAO_DETECCAO p
WHERE p.NOME = 'Armário com Gavetas'
ON CONFLICT DO NOTHING;

INSERT INTO TABELA_COMPONENTE_PADRAO (
    ID_PADRAO_DETECCAO,
    TIPO,
    DESCRICAO,
    QUANTIDADE_PADRAO,
    ORDEM
)
SELECT 
    p.ID,
    'GAVETA',
    'Gaveta',
    4,
    2
FROM TABELA_PADRAO_DETECCAO p
WHERE p.NOME = 'Armário com Gavetas'
ON CONFLICT DO NOTHING;

-- =====================================================
-- FIM DO SCRIPT
-- =====================================================

-- Mensagem de sucesso
DO $
DECLARE
    total_padroes INTEGER;
    total_componentes INTEGER;
BEGIN
    SELECT COUNT(*) INTO total_padroes FROM TABELA_PADRAO_DETECCAO;
    SELECT COUNT(*) INTO total_componentes FROM TABELA_COMPONENTE_PADRAO;
    
    RAISE NOTICE 'Padrões de detecção inseridos com sucesso!';
    RAISE NOTICE 'Total de padrões: %', total_padroes;
    RAISE NOTICE 'Total de componentes padrão: %', total_componentes;
    RAISE NOTICE '';
    RAISE NOTICE 'Padrões configurados:';
    RAISE NOTICE '  1. Mesa com Cadeiras (prioridade 10)';
    RAISE NOTICE '  2. Estação de Trabalho (prioridade 9)';
    RAISE NOTICE '  3. Computador Completo (prioridade 8)';
    RAISE NOTICE '  4. Conjunto de Laboratório (prioridade 7)';
    RAISE NOTICE '  5. Armário com Gavetas (prioridade 6)';
END $;
