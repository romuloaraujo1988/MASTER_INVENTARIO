@echo off
setlocal enabledelayedexpansion

echo ========================================
echo Criando Tabelas de Itens Compostos
echo ========================================
echo.

set PGPASSWORD=Romulo@2020
set PGHOST=localhost
set PGUSER=inventario
set PGDATABASE=sispatrimonio
set PGPORT=5432

echo Executando script SQL via Java...
echo.

java -cp "target/lib/*;target/classes" ^
  -Ddb.host=%PGHOST% ^
  -Ddb.port=%PGPORT% ^
  -Ddb.name=%PGDATABASE% ^
  -Ddb.user=%PGUSER% ^
  -Ddb.password=%PGPASSWORD% ^
  com.inventario.util.ExecutarScriptSQL sql/criar_tabelas_item_composto.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo ✓ Tabelas criadas com sucesso!
    echo ========================================
    echo.
    echo Tabelas criadas:
    echo   - tabela_item_composto
    echo   - tabela_coleta_componente
    echo   - view_itens_compostos
    echo.
    echo Dados de exemplo inseridos:
    echo   - Conjunto escolar (patrimonio 1)
    echo   - Computador completo (patrimonio 2)
) else (
    echo.
    echo ========================================
    echo ✗ Erro ao criar tabelas
    echo ========================================
    echo.
    echo Verifique:
    echo   1. PostgreSQL está rodando
    echo   2. Credenciais estão corretas
    echo   3. Banco 'sispatrimonio' existe
)

echo.
pause
