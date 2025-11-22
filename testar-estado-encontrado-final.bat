@echo off
echo ========================================
echo Teste Final - Estado Encontrado
echo ========================================
echo.

echo [1] Estrutura da tabela local_coleta:
sqlite3 data/inventario.db "PRAGMA table_info(local_coleta);" | findstr "estado_encontrado\|observacoes"
echo.

echo [2] Coletas atuais (ultimas 5):
sqlite3 data/inventario.db "SELECT id, id_patrimonio, estado_encontrado, observacoes FROM local_coleta ORDER BY id DESC LIMIT 5;"
echo.

echo [3] Contagem de coletas por estado:
sqlite3 data/inventario.db "SELECT estado_encontrado, COUNT(*) as total FROM local_coleta GROUP BY estado_encontrado;"
echo.

echo ========================================
echo Teste concluido!
echo ========================================
echo.
echo COMO TESTAR:
echo 1. Abrir ColetaFrame_v2
echo 2. Selecionar uma sala
echo 3. Buscar patrimonio
echo 4. Selecionar estado: BOM
echo 5. Adicionar observacao: "Teste de estado"
echo 6. Registrar coleta
echo 7. Executar este script novamente
echo 8. Verificar se estado foi salvo
echo.
pause
