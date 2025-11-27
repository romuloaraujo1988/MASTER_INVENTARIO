-- =====================================================
-- INSTALAÇÃO COMPLETA DO MÓDULO DE ITENS COMPOSTOS
-- Sistema de Inventário de Patrimônio IFMT
-- =====================================================
-- Este script executa todos os scripts necessários
-- para instalar o módulo de itens compostos
-- =====================================================
-- Data de Criação: 27/11/2025
-- Versão: 1.0
-- =====================================================

\echo '=============================================='
\echo 'INSTALAÇÃO DO MÓDULO DE ITENS COMPOSTOS'
\echo 'Sistema de Inventário Patrimonial - IFMT'
\echo '=============================================='
\echo ''

-- Verificar se as tabelas principais existem
DO $
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'tabela_patrimonio') THEN
        RAISE EXCEPTION 'ERRO: TABELA_PATRIMONIO não encontrada. Execute primeiro o script principal do sistema.';
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'tabela_inventario') THEN
        RAISE EXCEPTION 'ERRO: TABELA_INVENTARIO não encontrada. Execute primeiro o script principal do sistema.';
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'tabela_coleta') THEN
        RAISE EXCEPTION 'ERRO: TABELA_COLETA não encontrada. Execute primeiro o script principal do sistema.';
    END IF;
    
    RAISE NOTICE 'Verificação de pré-requisitos: OK';
END $;

\echo ''
\echo 'Passo 1/3: Criando tabelas...'
\echo ''

-- Executar script de criação de tabelas
\i sql/criar_tabelas_item_composto.sql

\echo ''
\echo 'Passo 2/3: Criando views...'
\echo ''

-- Executar script de criação de views
\i sql/criar_views_item_composto.sql

\echo ''
\echo 'Passo 3/3: Inserindo padrões de detecção...'
\echo ''

-- Executar script de inserção de padrões
\i sql/inserir_padroes_deteccao_item_composto.sql

\echo ''
\echo '=============================================='
\echo 'INSTALAÇÃO CONCLUÍDA COM SUCESSO!'
\echo '=============================================='
\echo ''

-- Resumo final
DO $
DECLARE
    total_tabelas INTEGER;
    total_views INTEGER;
    total_padroes INTEGER;
    total_indices INTEGER;
BEGIN
    -- Contar tabelas do módulo
    SELECT COUNT(*) INTO total_tabelas
    FROM information_schema.tables
    WHERE table_name IN (
        'tabela_item_composto',
        'tabela_componente',
        'tabela_componente_coleta',
        'tabela_padrao_deteccao',
        'tabela_componente_padrao'
    );
    
    -- Contar views do módulo
    SELECT COUNT(*) INTO total_views
    FROM information_schema.views
    WHERE table_name LIKE 'view_%item_composto%'
       OR table_name LIKE 'view_%componente%'
       OR table_name LIKE 'view_%descricao_agrupada%'
       OR table_name LIKE 'view_%estatistica%';
    
    -- Contar padrões
    SELECT COUNT(*) INTO total_padroes
    FROM tabela_padrao_deteccao;
    
    -- Contar índices do módulo
    SELECT COUNT(*) INTO total_indices
    FROM pg_indexes
    WHERE tablename IN (
        'tabela_item_composto',
        'tabela_componente',
        'tabela_componente_coleta',
        'tabela_padrao_deteccao',
        'tabela_componente_padrao'
    );
    
    RAISE NOTICE '';
    RAISE NOTICE 'RESUMO DA INSTALAÇÃO:';
    RAISE NOTICE '  • Tabelas criadas: %', total_tabelas;
    RAISE NOTICE '  • Views criadas: %', total_views;
    RAISE NOTICE '  • Índices criados: %', total_indices;
    RAISE NOTICE '  • Padrões de detecção: %', total_padroes;
    RAISE NOTICE '';
    RAISE NOTICE 'O módulo de Itens Compostos está pronto para uso!';
    RAISE NOTICE '';
    RAISE NOTICE 'Próximos passos:';
    RAISE NOTICE '  1. Compilar as classes Java do módulo';
    RAISE NOTICE '  2. Adicionar opções no menu do sistema';
    RAISE NOTICE '  3. Testar a funcionalidade de detecção automática';
    RAISE NOTICE '';
END $;
