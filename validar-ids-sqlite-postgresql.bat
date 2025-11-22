@echo off
echo ========================================
echo Validacao de IDs - SQLite vs PostgreSQL
echo ========================================
echo.

echo [1] Verificar estrutura das tabelas:
echo.
echo --- local_patrimonio ---
sqlite3 data/inventario.db "PRAGMA table_info(local_patrimonio);" | findstr "id"
echo.
echo --- local_sala ---
sqlite3 data/inventario.db "PRAGMA table_info(local_sala);" | findstr "id"
echo.
echo --- TABELA_SALA_INVENTARIO ---
sqlite3 data/inventario.db "PRAGMA table_info(TABELA_SALA_INVENTARIO);" | findstr "ID"
echo.

echo [2] Verificar se IDs sao INTEGER (nao AUTOINCREMENT):
sqlite3 data/inventario.db "SELECT sql FROM sqlite_master WHERE name='local_patrimonio';" | findstr "id"
echo.

echo [3] Amostras de IDs no SQLite:
echo.
echo --- Patrimonios (primeiros 5) ---
sqlite3 data/inventario.db "SELECT id, numero, SUBSTR(descricao, 1, 30) FROM local_patrimonio ORDER BY id LIMIT 5;"
echo.
echo --- Salas (primeiras 5) ---
sqlite3 data/inventario.db "SELECT id, nome FROM local_sala ORDER BY id LIMIT 5;"
echo.
echo --- Inventario ---
sqlite3 data/inventario.db "SELECT id, nome FROM local_inventario;"
echo.

echo [4] Verificar coletas (se existirem):
sqlite3 data/inventario.db "SELECT c.id as coleta_id, c.id_patrimonio, p.numero FROM local_coleta c LEFT JOIN local_patrimonio p ON c.id_patrimonio = p.id LIMIT 5;"
echo.

echo [5] Verificar TABELA_SALA_INVENTARIO:
sqlite3 data/inventario.db "SELECT ID_SALA, ID_INVENTARIO, NUMERO_SALA FROM TABELA_SALA_INVENTARIO LIMIT 5;"
echo.

echo ========================================
echo Validacao concluida!
echo ========================================
echo.
echo IMPORTANTE:
echo - IDs devem ser os mesmos do PostgreSQL
echo - Nao deve ter AUTOINCREMENT
echo - TABELA_SALA_INVENTARIO deve ter chave composta
echo.
pause
