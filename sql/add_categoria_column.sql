-- Script para adicionar coluna categoria na tabela patrimônio
-- Este script deve ser executado no banco de dados para suportar o treinamento de IA

-- Adicionar coluna categoria na tabela patrimônio
ALTER TABLE tabela_patrimonio ADD COLUMN categoria VARCHAR(50);

-- Criar índice para melhorar performance de consultas por categoria
CREATE INDEX idx_patrimonio_categoria ON tabela_patrimonio(categoria);

-- Comentário da coluna
COMMENT ON COLUMN tabela_patrimonio.categoria IS 'Categoria do patrimônio definida pela IA treinada';

-- Atualizar categorias existentes baseado na descrição (opcional)
-- Isso pode ser executado para categorizar patrimônios já existentes
UPDATE tabela_patrimonio SET categoria = 'INFORMATICA' 
WHERE categoria IS NULL AND (
    LOWER(descricao) LIKE '%computador%' OR 
    LOWER(descricao) LIKE '%notebook%' OR 
    LOWER(descricao) LIKE '%monitor%' OR 
    LOWER(descricao) LIKE '%impressora%' OR 
    LOWER(descricao) LIKE '%scanner%'
);

UPDATE tabela_patrimonio SET categoria = 'MOBILIARIO' 
WHERE categoria IS NULL AND (
    LOWER(descricao) LIKE '%mesa%' OR 
    LOWER(descricao) LIKE '%cadeira%' OR 
    LOWER(descricao) LIKE '%armario%' OR 
    LOWER(descricao) LIKE '%estante%'
);

UPDATE tabela_patrimonio SET categoria = 'ELETRODOMESTICO' 
WHERE categoria IS NULL AND (
    LOWER(descricao) LIKE '%geladeira%' OR 
    LOWER(descricao) LIKE '%microondas%' OR 
    LOWER(descricao) LIKE '%ar condicionado%'
);

UPDATE tabela_patrimonio SET categoria = 'VEICULO' 
WHERE categoria IS NULL AND (
    LOWER(descricao) LIKE '%carro%' OR 
    LOWER(descricao) LIKE '%caminhao%' OR 
    LOWER(descricao) LIKE '%moto%'
);

UPDATE tabela_patrimonio SET categoria = 'EQUIPAMENTO_LABORATORIO' 
WHERE categoria IS NULL AND (
    LOWER(descricao) LIKE '%microscopio%' OR 
    LOWER(descricao) LIKE '%balanca%' OR 
    LOWER(descricao) LIKE '%centrifuga%'
);

-- Definir categoria padrão para patrimônios sem categoria específica
UPDATE tabela_patrimonio SET categoria = 'OUTROS' WHERE categoria IS NULL;

COMMIT;