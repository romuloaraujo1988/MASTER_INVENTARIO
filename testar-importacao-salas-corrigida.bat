@echo off
echo ========================================
echo Teste de Importacao de Salas - Corrigida
echo ========================================
echo.

echo [1] Verificando estrutura da TABELA_SALA_INVENTARIO:
sqlite3 data/inventario.db "SELECT sql FROM sqlite_master WHERE name='TABELA_SALA_INVENTARIO';"
echo.

echo [2] Total de salas em TABELA_SALA_INVENTARIO:
sqlite3 data/inventario.db "SELECT COUNT(*) as total FROM TABELA_SALA_INVENTARIO;"
echo.

echo [3] Salas por inventario:
sqlite3 data/inventario.db "SELECT ID_INVENTARIO, COUNT(*) as total_salas FROM TABELA_SALA_INVENTARIO GROUP BY ID_INVENTARIO;"
echo.

echo [4] Primeiras 5 salas:
sqlite3 data/inventario.db "SELECT ID_SALA, ID_INVENTARIO, NUMERO_SALA, NOME_SALA FROM TABELA_SALA_INVENTARIO LIMIT 5;"
echo.

echo [5] Verificando coletas:
sqlite3 data/inventario.db "SELECT COUNT(*) as total_coletas FROM local_coleta;"
echo.

echo [6] Coletas com salas:
sqlite3 data/inventario.db "SELECT c.id, p.numero, s.nome as sala FROM local_coleta c LEFT JOIN local_patrimonio p ON c.id_patrimonio = p.id LEFT JOIN local_sala s ON p.id_sala = s.id LIMIT 5;"
echo.

echo ========================================
echo Teste concluido!
echo ========================================
echo.
echo Agora tente abrir o ColetaFrame_v2 e selecionar uma sala.
echo As coletas devem aparecer na tabela!
echo.
pause
