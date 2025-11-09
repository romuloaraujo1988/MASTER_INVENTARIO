-- Verificar responsáveis cadastrados
SELECT 
    ID,
    NOME,
    CPF,
    EMAIL,
    CARGO,
    ID_SETOR,
    ATIVO,
    DATA_CADASTRO
FROM TABELA_RESPONSAVEL
ORDER BY NOME;

-- Contar responsáveis ativos
SELECT 
    COUNT(*) as total_responsaveis,
    SUM(CASE WHEN ATIVO = TRUE THEN 1 ELSE 0 END) as ativos,
    SUM(CASE WHEN ATIVO = FALSE THEN 1 ELSE 0 END) as inativos
FROM TABELA_RESPONSAVEL;
