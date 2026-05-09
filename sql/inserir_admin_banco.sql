-- Script para adicionar manualmente o usuário admin no banco de dados ativo
INSERT INTO TABELA_USUARIO (
    LOGIN, SENHA_HASH, NOME_COMPLETO, EMAIL, PERFIL, ID_SETOR, ATIVO, PRIMEIRO_ACESSO
)
SELECT 
    'admin', 
    '$2a$10$N9qo8uLOickgx2ZMRZoMye1VdLIqCRFpb6AQBaOLLqI.xjVgLOjHi', 
    'Administrador do Sistema', 
    'admin@sistema.com', 
    'ADMIN',
    (SELECT ID FROM TABELA_SETOR WHERE NOME = 'Administração' LIMIT 1),
    'S',
    'S'
WHERE NOT EXISTS (
    SELECT 1 FROM TABELA_USUARIO WHERE LOGIN = 'admin'
);
