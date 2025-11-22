@echo off
echo ========================================
echo Teste Final - Coletas por Sala
echo ========================================
echo.

echo [1] Total de coletas no SQLite:
sqlite3 data/inventario.db "SELECT COUNT(*) as total FROM local_coleta;"
echo.

echo [2] Coletas com suas salas:
sqlite3 data/inventario.db "SELECT c.id as coleta_id, p.numero as patrimonio, s.id as sala_id, s.nome as sala FROM local_coleta c LEFT JOIN local_patrimonio p ON c.id_patrimonio = p.id LEFT JOIN local_sala s ON p.id_sala = s.id ORDER BY s.nome;"
echo.

echo [3] Contagem de coletas por sala:
sqlite3 data/inventario.db "SELECT s.id, s.nome, COUNT(c.id) as total_coletas FROM local_sala s LEFT JOIN local_patrimonio p ON s.id = p.id_sala LEFT JOIN local_coleta c ON p.id = c.id_patrimonio GROUP BY s.id, s.nome HAVING COUNT(c.id) > 0 ORDER BY total_coletas DESC;"
echo.

echo [4] Teste da query do ColetaDAO (sala 119):
sqlite3 data/inventario.db "SELECT c.id, c.data_coleta, p.numero, p.descricao FROM local_coleta c LEFT JOIN local_patrimonio p ON c.id_patrimonio = p.id WHERE p.id_sala = 119;"
echo.

echo [5] Teste da query do ColetaDAO (sala 30):
sqlite3 data/inventario.db "SELECT c.id, c.data_coleta, p.numero, p.descricao FROM local_coleta c LEFT JOIN local_patrimonio p ON c.id_patrimonio = p.id WHERE p.id_sala = 30;"
echo.

echo ========================================
echo Teste concluido!
echo ========================================
echo.
echo COMO TESTAR NO SISTEMA:
echo 1. Abrir ColetaFrame_v2
echo 2. Selecionar sala "NAPNE" (ID=20)
echo    - Deve mostrar 1 coleta
echo 3. Selecionar sala "Area do Campus" (ID=30)
echo    - Deve mostrar 2 coletas
echo 4. Selecionar sala "SALA DOS PROFESSORES 2" (ID=119)
echo    - Deve mostrar 1 coleta
echo.
pause
