-- ========================================
-- VERIFICACAO DE DADOS NO SQLITE
-- ========================================

.mode column
.headers on
.width 30 10

-- Verificar tabelas criadas
SELECT '=== TABELAS CRIADAS ===' AS info;
SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;

-- Verificar patrimônios
SELECT '' AS '';
SELECT '=== PATRIMONIOS ===' AS info;
SELECT COUNT(*) AS total FROM local_patrimonio;
SELECT * FROM local_patrimonio LIMIT 5;

-- Verificar salas
SELECT '' AS '';
SELECT '=== SALAS ===' AS info;
SELECT COUNT(*) AS total FROM local_sala;
SELECT * FROM local_sala LIMIT 5;

-- Verificar responsáveis
SELECT '' AS '';
SELECT '=== RESPONSAVEIS ===' AS info;
SELECT COUNT(*) AS total FROM local_responsavel;
SELECT * FROM local_responsavel LIMIT 5;

-- Verificar usuários
SELECT '' AS '';
SELECT '=== USUARIOS ===' AS info;
SELECT COUNT(*) AS total FROM local_usuario;
SELECT id, login, nome_completo, perfil, ativo FROM local_usuario;

-- Verificar inventários
SELECT '' AS '';
SELECT '=== INVENTARIOS ===' AS info;
SELECT COUNT(*) AS total FROM local_inventario;
SELECT * FROM local_inventario;

-- Verificar tabela de compatibilidade
SELECT '' AS '';
SELECT '=== TABELA_INVENTARIO (Compatibilidade) ===' AS info;
SELECT COUNT(*) AS total FROM TABELA_INVENTARIO;
SELECT * FROM TABELA_INVENTARIO;

-- Verificar metadados
SELECT '' AS '';
SELECT '=== METADADOS DE SINCRONIZACAO ===' AS info;
SELECT * FROM sync_metadata;
