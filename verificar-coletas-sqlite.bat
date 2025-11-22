@echo off
echo ========================================
echo Verificacao de Coletas no SQLite
echo ========================================
echo.

echo [1] Total de coletas:
sqlite3 data/inventario.db "SELECT COUNT(*) as total FROM local_coleta;"
echo.

echo [2] Coletas por sala:
sqlite3 data/inventario.db "SELECT s.nome as sala, COUNT(c.id) as total_coletas FROM local_coleta c LEFT JOIN local_patrimonio p ON c.id_patrimonio = p.id LEFT JOIN local_sala s ON p.id_sala = s.id GROUP BY s.nome ORDER BY total_coletas DESC;"
echo.

echo [3] Ultimas 5 coletas:
sqlite3 data/inventario.db "SELECT c.id, c.data_coleta, p.numero, s.nome as sala FROM local_coleta c LEFT JOIN local_patrimonio p ON c.id_patrimonio = p.id LEFT JOIN local_sala s ON p.id_sala = s.id ORDER BY c.data_coleta DESC LIMIT 5;"
echo.

echo [4] Detalhes das coletas:
sqlite3 data/inventario.db "SELECT c.id as coleta_id, c.id_patrimonio, c.data_coleta, p.numero, p.descricao, s.nome as sala FROM local_coleta c LEFT JOIN local_patrimonio p ON c.id_patrimonio = p.id LEFT JOIN local_sala s ON p.id_sala = s.id ORDER BY c.data_coleta DESC;"
echo.

echo ========================================
echo Verificacao concluida!
echo ========================================
pause
