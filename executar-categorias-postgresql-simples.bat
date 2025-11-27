@echo off
REM =====================================================
REM Script Simples para criar tabelas de categoria
REM =====================================================

echo ========================================
echo CRIANDO TABELAS DE CATEGORIA
echo ========================================
echo.

REM Tentar encontrar psql
set PSQL_PATH=

REM Verificar caminhos comuns
if exist "C:\Program Files\PostgreSQL\16\bin\psql.exe" set PSQL_PATH=C:\Program Files\PostgreSQL\16\bin\psql.exe
if exist "C:\Program Files\PostgreSQL\15\bin\psql.exe" set PSQL_PATH=C:\Program Files\PostgreSQL\15\bin\psql.exe
if exist "C:\Program Files\PostgreSQL\14\bin\psql.exe" set PSQL_PATH=C:\Program Files\PostgreSQL\14\bin\psql.exe
if exist "C:\Program Files\PostgreSQL\13\bin\psql.exe" set PSQL_PATH=C:\Program Files\PostgreSQL\13\bin\psql.exe
if exist "C:\Program Files\PostgreSQL\12\bin\psql.exe" set PSQL_PATH=C:\Program Files\PostgreSQL\12\bin\psql.exe

if "%PSQL_PATH%"=="" (
    echo ERRO: psql.exe nao encontrado!
    echo.
    echo Por favor, execute manualmente:
    echo.
    echo SET PGPASSWORD=inventario
    echo psql -h localhost -U inventario -d sispatrimonio -f sql\criar_tabelas_categoria_patrimonio.sql
    echo.
    pause
    exit /b 1
)

echo Usando: %PSQL_PATH%
echo.

REM Configurar senha
set PGPASSWORD=inventario

REM Executar script
"%PSQL_PATH%" -h localhost -U inventario -d sispatrimonio -f "sql\criar_tabelas_categoria_patrimonio.sql"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo SUCESSO! Tabelas criadas.
    echo ========================================
    echo.
    
    REM Consultar resultado
    "%PSQL_PATH%" -h localhost -U inventario -d sispatrimonio -c "SELECT nome, total_subcategorias FROM view_categorias_com_contagem ORDER BY ordem_exibicao;"
) else (
    echo.
    echo ========================================
    echo ERRO ao criar tabelas
    echo ========================================
)

echo.
pause
