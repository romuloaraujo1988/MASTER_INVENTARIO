@echo off
REM ============================================================
REM Script para atualizar tabelas de itens compostos
REM Seguro - não quebra dados existentes
REM ============================================================

echo.
echo ========================================
echo  Atualizacao de Tabelas - Itens Compostos
echo  SEGURO - Nao quebra dados existentes
echo ========================================
echo.

REM Configurações do banco
set PGHOST=localhost
set PGPORT=5432
set PGDATABASE=sispatrimonio
set PGUSER=postgres

echo Banco: %PGDATABASE%
echo Host: %PGHOST%:%PGPORT%
echo Usuario: %PGUSER%
echo.

REM Verificar se psql está disponível
where psql >nul 2>&1
if errorlevel 1 (
    echo AVISO: psql nao encontrado no PATH
    echo Tentando caminho padrao do PostgreSQL...
    set PSQL_PATH="C:\Program Files\PostgreSQL\16\bin\psql.exe"
    if not exist %PSQL_PATH% (
        set PSQL_PATH="C:\Program Files\PostgreSQL\15\bin\psql.exe"
    )
    if not exist %PSQL_PATH% (
        set PSQL_PATH="C:\Program Files\PostgreSQL\14\bin\psql.exe"
    )
    if not exist %PSQL_PATH% (
        echo ERRO: PostgreSQL nao encontrado!
        echo Instale o PostgreSQL ou adicione ao PATH.
        pause
        exit /b 1
    )
) else (
    set PSQL_PATH=psql
)

echo.
echo Deseja executar a atualizacao? (S/N)
set /p CONFIRMA=

if /i "%CONFIRMA%" neq "S" (
    echo Operacao cancelada.
    pause
    exit /b 0
)

echo.
echo Executando script...
echo.

REM Solicitar senha
set /p PGPASSWORD=Digite a senha do PostgreSQL: 

REM Executar script
%PSQL_PATH% -h %PGHOST% -p %PGPORT% -U %PGUSER% -d %PGDATABASE% -f sql/atualizar-tabelas-itens-compostos-seguro.sql

if errorlevel 1 (
    echo.
    echo ERRO ao executar script!
    echo Verifique as mensagens acima.
) else (
    echo.
    echo ========================================
    echo  Atualizacao concluida com sucesso!
    echo ========================================
)

echo.
pause
