-- Script para adicionar campo DESCRICAO_RESUMIDA na tabela TABELA_PATRIMONIO
-- Este campo armazenará versões resumidas das descrições longas para facilitar identificação

-- Adicionar a coluna DESCRICAO_RESUMIDA
ALTER TABLE TABELA_PATRIMONIO 
ADD COLUMN DESCRICAO_RESUMIDA VARCHAR(100);

-- Adicionar comentário explicativo
COMMENT ON COLUMN TABELA_PATRIMONIO.DESCRICAO_RESUMIDA IS 'Versão resumida da descrição para facilitar identificação em campo';

-- Criar índice para busca eficiente por descrição resumida
CREATE INDEX idx_patrimonio_descricao_resumida 
ON TABELA_PATRIMONIO(DESCRICAO_RESUMIDA);

-- Criar índice composto para busca por descrição completa ou resumida
CREATE INDEX idx_patrimonio_descricoes 
ON TABELA_PATRIMONIO(DESCRICAO, DESCRICAO_RESUMIDA);

-- Função para gerar resumo básico (fallback quando o serviço Java não estiver disponível)
CREATE OR REPLACE FUNCTION gerar_resumo_basico(descricao_completa TEXT)
RETURNS VARCHAR(100) AS $$
DECLARE
    palavras TEXT[];
    resumo TEXT := '';
    palavra TEXT;
    contador INTEGER := 0;
BEGIN
    -- Se a descrição for nula ou vazia, retornar vazio
    IF descricao_completa IS NULL OR LENGTH(TRIM(descricao_completa)) = 0 THEN
        RETURN '';
    END IF;
    
    -- Se a descrição já for curta, retornar ela mesma
    IF LENGTH(descricao_completa) <= 50 THEN
        RETURN descricao_completa;
    END IF;
    
    -- Dividir em palavras e pegar as primeiras 4 palavras significativas
    palavras := string_to_array(UPPER(TRIM(descricao_completa)), ' ');
    
    FOREACH palavra IN ARRAY palavras
    LOOP
        -- Pular palavras muito pequenas ou conectivos
        IF LENGTH(palavra) > 2 AND 
           palavra NOT IN ('COM', 'DE', 'DA', 'DO', 'EM', 'PARA', 'POR', 'E', 'OU', 'QUE') THEN
            
            IF resumo = '' THEN
                resumo := palavra;
            ELSE
                resumo := resumo || ' ' || palavra;
            END IF;
            
            contador := contador + 1;
            
            -- Limitar a 4 palavras ou 50 caracteres
            IF contador >= 4 OR LENGTH(resumo) >= 45 THEN
                EXIT;
            END IF;
        END IF;
    END LOOP;
    
    -- Se o resumo ficou muito longo, truncar
    IF LENGTH(resumo) > 50 THEN
        resumo := LEFT(resumo, 47) || '...';
    END IF;
    
    RETURN resumo;
END;
$$ LANGUAGE plpgsql;

-- Trigger para gerar resumo automaticamente ao inserir/atualizar
CREATE OR REPLACE FUNCTION trigger_gerar_resumo_descricao()
RETURNS TRIGGER AS $$
BEGIN
    -- Gerar resumo apenas se a descrição foi alterada e o resumo está vazio
    IF (TG_OP = 'INSERT' OR OLD.DESCRICAO IS DISTINCT FROM NEW.DESCRICAO) AND 
       (NEW.DESCRICAO_RESUMIDA IS NULL OR NEW.DESCRICAO_RESUMIDA = '') THEN
        NEW.DESCRICAO_RESUMIDA := gerar_resumo_basico(NEW.DESCRICAO);
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Criar o trigger
DROP TRIGGER IF EXISTS tg_gerar_resumo_descricao ON TABELA_PATRIMONIO;
CREATE TRIGGER tg_gerar_resumo_descricao
    BEFORE INSERT OR UPDATE ON TABELA_PATRIMONIO
    FOR EACH ROW
    EXECUTE FUNCTION trigger_gerar_resumo_descricao();

-- Atualizar registros existentes com resumos (executar apenas uma vez)
-- ATENÇÃO: Este comando pode demorar dependendo da quantidade de registros
UPDATE TABELA_PATRIMONIO 
SET DESCRICAO_RESUMIDA = gerar_resumo_basico(DESCRICAO)
WHERE DESCRICAO_RESUMIDA IS NULL 
   OR DESCRICAO_RESUMIDA = ''
   AND DESCRICAO IS NOT NULL;

-- Verificar resultados
SELECT 
    ID,
    NUMERO,
    LEFT(DESCRICAO, 80) as DESCRICAO_ORIGINAL,
    DESCRICAO_RESUMIDA
FROM TABELA_PATRIMONIO 
WHERE DESCRICAO_RESUMIDA IS NOT NULL
ORDER BY ID
LIMIT 10;

-- Estatísticas
SELECT 
    COUNT(*) as total_registros,
    COUNT(DESCRICAO_RESUMIDA) as com_resumo,
    COUNT(*) - COUNT(DESCRICAO_RESUMIDA) as sem_resumo,
    ROUND(COUNT(DESCRICAO_RESUMIDA) * 100.0 / COUNT(*), 2) as percentual_com_resumo
FROM TABELA_PATRIMONIO;

PRINT 'Campo DESCRICAO_RESUMIDA adicionado com sucesso!';
PRINT 'Trigger criado para gerar resumos automaticamente.';
PRINT 'Índices criados para busca eficiente.';
PRINT 'Registros existentes atualizados com resumos básicos.';