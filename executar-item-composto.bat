@echo off
echo ========================================
echo Criando Tabelas de Itens Compostos
echo ========================================
echo.

set PGPASSWORD=Romulo@2020

echo Executando script SQL...
echo.

java -cp "target/lib/*;target/classes" com.inventario.util.ExecutarScriptSQL sql/criar_tabelas_item_composto.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Tabelas criadas com sucesso!
    echo ========================================
) else (
    echo.
    echo ========================================
    echo Erro ao criar tabelas
    echo ========================================
)

pause
