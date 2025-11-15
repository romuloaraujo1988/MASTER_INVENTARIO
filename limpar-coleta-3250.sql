-- Limpar coleta do patrimônio 3250 para permitir novo teste
-- Execute este script no pgAdmin ou outro cliente PostgreSQL

-- Ver coletas atuais do patrimônio 3250
SELECT c.id, c.id_patrimonio, p.numero, c.data_coleta 
FROM coleta c
JOIN patrimonio p ON c.id_patrimonio = p.id
WHERE p.numero = '3250' AND c.id_inventario = 2;

-- Deletar a coleta
DELETE FROM coleta 
WHERE id_patrimonio = 10 AND id_inventario = 2;

-- Confirmar que foi deletada
SELECT 'Coleta removida com sucesso!' as resultado;
