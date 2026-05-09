-- ============================================================================
-- SCRIPT PARA GERAR ITENS COMPOSTOS AUTOMATICAMENTE
-- Sistema de Inventário Patrimonial - IFMT
-- Data: 28/11/2025
-- ============================================================================
-- Este script gera automaticamente itens compostos para patrimônios baseado
-- em palavras-chave na descrição do patrimônio
-- ============================================================================

-- Limpar dados existentes (CUIDADO: isso apaga todos os itens compostos!)
-- Descomente apenas se quiser começar do zero
-- TRUNCATE TABLE tabela_coleta_componente CASCADE;
-- TRUNCATE TABLE tabela_item_composto CASCADE;

-- ============================================================================
-- FUNÇÃO AUXILIAR: Gerar componentes baseado em palavras-chave
-- ============================================================================

DO $$
DECLARE
    v_patrimonio RECORD;
    v_count INTEGER := 0;
BEGIN
    RAISE NOTICE '============================================================';
    RAISE NOTICE 'INICIANDO GERAÇÃO DE ITENS COMPOSTOS';
    RAISE NOTICE '============================================================';
    RAISE NOTICE '';
    
    -- ========================================================================
    -- 1. CONJUNTOS ESCOLARES (Mesa + Cadeira)
    -- ========================================================================
    RAISE NOTICE '1. Processando CONJUNTOS ESCOLARES...';
    
    FOR v_patrimonio IN 
        SELECT DISTINCT p.id, p.numero, p.descricao
        FROM tabela_patrimonio p
        WHERE (
            UPPER(p.descricao) LIKE '%CONJUNTO%ESCOLAR%' OR
            UPPER(p.descricao) LIKE '%CARTEIRA%ESCOLAR%'
        )
        AND NOT EXISTS (
            SELECT 1 FROM tabela_item_composto ic 
            WHERE ic.id_patrimonio_principal = p.id
        )
        ORDER BY p.id
    LOOP
        -- Adicionar MESA
        INSERT INTO tabela_item_composto 
            (id_patrimonio_principal, tipo_componente, descricao_componente, 
             quantidade_esperada, obrigatorio, observacao)
        VALUES 
            (v_patrimonio.id, 'MESA', 'Mesa escolar com tampo MDP 18mm, 450x600mm', 
             1, true, 'Componente do conjunto escolar');
        
        -- Adicionar CADEIRA
        INSERT INTO tabela_item_composto 
            (id_patrimonio_principal, tipo_componente, descricao_componente, 
             quantidade_esperada, obrigatorio, observacao)
        VALUES 
            (v_patrimonio.id, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 
             1, true, 'Componente do conjunto escolar');
        
        v_count := v_count + 1;
        
        IF v_count % 100 = 0 THEN
            RAISE NOTICE '  - Processados % conjuntos escolares...', v_count;
        END IF;
    END LOOP;
    
    RAISE NOTICE '  ✓ Total de conjuntos escolares processados: %', v_count;
    RAISE NOTICE '';
    
    -- ========================================================================
    -- 2. COMPUTADORES / ESTAÇÕES DE TRABALHO (Monitor + CPU + Teclado + Mouse)
    -- ========================================================================
    RAISE NOTICE '2. Processando COMPUTADORES E ESTAÇÕES DE TRABALHO...';
    v_count := 0;
    
    FOR v_patrimonio IN 
        SELECT DISTINCT p.id, p.numero, p.descricao
        FROM tabela_patrimonio p
        WHERE (
            UPPER(p.descricao) LIKE '%COMPUTADOR%' OR
            UPPER(p.descricao) LIKE '%ESTAÇÃO%TRABALHO%' OR
            UPPER(p.descricao) LIKE '%DESKTOP%' OR
            UPPER(p.descricao) LIKE '%MICROCOMPUTADOR%'
        )
        AND NOT EXISTS (
            SELECT 1 FROM tabela_item_composto ic 
            WHERE ic.id_patrimonio_principal = p.id
        )
        ORDER BY p.id
    LOOP
        -- Adicionar MONITOR
        INSERT INTO tabela_item_composto 
            (id_patrimonio_principal, tipo_componente, descricao_componente, 
             quantidade_esperada, obrigatorio, observacao)
        VALUES 
            (v_patrimonio.id, 'MONITOR', 'Monitor LCD/LED', 
             1, true, 'Componente do computador');
        
        -- Adicionar CPU
        INSERT INTO tabela_item_composto 
            (id_patrimonio_principal, tipo_componente, descricao_componente, 
             quantidade_esperada, obrigatorio, observacao)
        VALUES 
            (v_patrimonio.id, 'CPU', 'Gabinete/CPU do computador', 
             1, true, 'Componente do computador');
        
        -- Adicionar TECLADO
        INSERT INTO tabela_item_composto 
            (id_patrimonio_principal, tipo_componente, descricao_componente, 
             quantidade_esperada, obrigatorio, observacao)
        VALUES 
            (v_patrimonio.id, 'TECLADO', 'Teclado USB/PS2', 
             1, true, 'Componente do computador');
        
        -- Adicionar MOUSE
        INSERT INTO tabela_item_composto 
            (id_patrimonio_principal, tipo_componente, descricao_componente, 
             quantidade_esperada, obrigatorio, observacao)
        VALUES 
            (v_patrimonio.id, 'MOUSE', 'Mouse USB/PS2', 
             1, true, 'Componente do computador');
        
        v_count := v_count + 1;
        
        IF v_count % 50 = 0 THEN
            RAISE NOTICE '  - Processados % computadores...', v_count;
        END IF;
    END LOOP;
    
    RAISE NOTICE '  ✓ Total de computadores processados: %', v_count;
    RAISE NOTICE '';
    
    -- ========================================================================
    -- 3. NOTEBOOKS (Carregador + Mouse opcional)
    -- ========================================================================
    RAISE NOTICE '3. Processando NOTEBOOKS...';
    v_count := 0;
    
    FOR v_patrimonio IN 
        SELECT DISTINCT p.id, p.numero, p.descricao
        FROM tabela_patrimonio p
        WHERE (
            UPPER(p.descricao) LIKE '%NOTEBOOK%' OR
            UPPER(p.descricao) LIKE '%LAPTOP%'
        )
        AND NOT EXISTS (
            SELECT 1 FROM tabela_item_composto ic 
            WHERE ic.id_patrimonio_principal = p.id
        )
        ORDER BY p.id
    LOOP
        -- Adicionar CARREGADOR
        INSERT INTO tabela_item_composto 
            (id_patrimonio_principal, tipo_componente, descricao_componente, 
             quantidade_esperada, obrigatorio, observacao)
        VALUES 
            (v_patrimonio.id, 'CARREGADOR', 'Fonte/Carregador do notebook', 
             1, true, 'Componente do notebook');
        
        -- Adicionar MOUSE (opcional)
        INSERT INTO tabela_item_composto 
            (id_patrimonio_principal, tipo_componente, descricao_componente, 
             quantidade_esperada, obrigatorio, observacao)
        VALUES 
            (v_patrimonio.id, 'MOUSE', 'Mouse USB', 
             1, false, 'Componente opcional do notebook');
        
        v_count := v_count + 1;
        
        IF v_count % 50 = 0 THEN
            RAISE NOTICE '  - Processados % notebooks...', v_count;
        END IF;
    END LOOP;
    
    RAISE NOTICE '  ✓ Total de notebooks processados: %', v_count;
    RAISE NOTICE '';
    
    -- ========================================================================
    -- 4. IMPRESSORAS (Cabo de força + Cabo USB)
    -- ========================================================================
    RAISE NOTICE '4. Processando IMPRESSORAS...';
    v_count := 0;
    
    FOR v_patrimonio IN 
        SELECT DISTINCT p.id, p.numero, p.descricao
        FROM tabela_patrimonio p
        WHERE (
            UPPER(p.descricao) LIKE '%IMPRESSORA%'
        )
        AND NOT EXISTS (
            SELECT 1 FROM tabela_item_composto ic 
            WHERE ic.id_patrimonio_principal = p.id
        )
        ORDER BY p.id
    LOOP
        -- Adicionar CABO DE FORÇA
        INSERT INTO tabela_item_composto 
            (id_patrimonio_principal, tipo_componente, descricao_componente, 
             quantidade_esperada, obrigatorio, observacao)
        VALUES 
            (v_patrimonio.id, 'CABO_FORCA', 'Cabo de força', 
             1, true, 'Componente da impressora');
        
        -- Adicionar CABO USB
        INSERT INTO tabela_item_composto 
            (id_patrimonio_principal, tipo_componente, descricao_componente, 
             quantidade_esperada, obrigatorio, observacao)
        VALUES 
            (v_patrimonio.id, 'CABO_USB', 'Cabo USB para impressora', 
             1, false, 'Componente opcional da impressora');
        
        v_count := v_count + 1;
        
        IF v_count % 50 = 0 THEN
            RAISE NOTICE '  - Processadas % impressoras...', v_count;
        END IF;
    END LOOP;
    
    RAISE NOTICE '  ✓ Total de impressoras processadas: %', v_count;
    RAISE NOTICE '';
    
    -- ========================================================================
    -- ATUALIZAR SEQUENCE
    -- ========================================================================
    PERFORM setval('tabela_item_composto_id_seq', 
                   (SELECT COALESCE(MAX(id), 1) FROM tabela_item_composto));
    
    RAISE NOTICE '============================================================';
    RAISE NOTICE '✅ GERAÇÃO DE ITENS COMPOSTOS CONCLUÍDA';
    RAISE NOTICE '============================================================';
END $$;

-- ============================================================================
-- ESTATÍSTICAS FINAIS
-- ============================================================================

SELECT 
    'RESUMO DA GERAÇÃO' as titulo,
    COUNT(DISTINCT id_patrimonio_principal) as total_patrimonios_com_componentes,
    COUNT(*) as total_componentes_criados,
    COUNT(DISTINCT tipo_componente) as tipos_diferentes
FROM tabela_item_composto;

-- Detalhamento por tipo de componente
SELECT 
    tipo_componente,
    COUNT(*) as quantidade,
    SUM(CASE WHEN obrigatorio THEN 1 ELSE 0 END) as obrigatorios,
    SUM(CASE WHEN NOT obrigatorio THEN 1 ELSE 0 END) as opcionais
FROM tabela_item_composto
GROUP BY tipo_componente
ORDER BY quantidade DESC;

-- ============================================================================
-- FIM DO SCRIPT
-- ============================================================================
