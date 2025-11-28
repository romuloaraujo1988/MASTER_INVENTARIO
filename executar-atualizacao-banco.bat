@echo off
chcp 65001 >nul
echo ============================================================
echo   ATUALIZAÇÃO SEGURA DO BANCO DE DADOS
echo   Sistema de Inventário Patrimonial - SIHCP
echo ============================================================
echo.

set DB_HOST=localhost
set DB_PORT=5432
set DB_NAME=sispatrimonio
set DB_USER=inventario
set SQL_FILE=sql\atualizar_banco_seguro.sql

echo Configurações:
echo    Host: %DB_HOST%
echo    Porta: %DB_PORT%
echo    Banco: %DB_NAME%
echo    Usuário: %DB_USER%
echo    Script: %SQL_FILE%
echo.

if not exist "%SQL_FILE%" (
    echo ❌ Arquivo SQL não encontrado: %SQL_FILE%
    pause
    exit /b 1
)

echo ⚠️  Este script irá:
echo    ✓ Adicionar tabelas que não existem
echo    ✓ Adicionar colunas que não existem
echo    ✓ Criar índices que não existem
echo.
echo    ✗ NÃO apaga dados
echo    ✗ NÃO modifica dados existentes
echo    ✗ NÃO remove tabelas ou colunas
echo.

set /p CONFIRM="Deseja continuar? (S/N): "
if /i not "%CONFIRM%"=="S" (
    echo Operação cancelada pelo usuário.
    pause
    exit /b 0
)

echo.
echo 🔄 Executando atualização...
echo.

set /p PGPASSWORD="Digite a senha do banco: "

psql -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -f "%SQL_FILE%"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ Atualização concluída com sucesso!
) else (
    echo.
    echo ❌ Erro durante a execução do script SQL
    echo.
    echo Se psql não foi encontrado, execute manualmente no pgAdmin:
    echo    1. Abra o pgAdmin
    echo    2. Conecte ao banco '%DB_NAME%'
    echo    3. Abra o arquivo: %SQL_FILE%
    echo    4. Execute o script (F5)
)

echo.
pause
