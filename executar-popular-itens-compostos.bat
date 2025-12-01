@echo off
echo ============================================================
echo POPULAR ITENS COMPOSTOS - Sistema de Inventario
echo ============================================================
echo.
echo Este script ira:
echo   1. Recriar as tabelas de itens compostos
echo   2. Popular com dados baseados nos patrimonios existentes
echo.
echo ATENCAO: Certifique-se de que o PostgreSQL esta rodando!
echo.
pause

echo.
echo Executando script SQL...
echo.

psql -h localhost -U inventario -d sispatrimonio -f sql/popular_itens_compostos_v2.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================================
    echo SUCESSO! Itens compostos populados com sucesso.
    echo ============================================================
) else (
    echo.
    echo ============================================================
    echo ERRO! Verifique as mensagens acima.
    echo ============================================================
)

echo.
pause
