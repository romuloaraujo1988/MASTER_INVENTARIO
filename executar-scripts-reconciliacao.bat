@echo off
echo ============================================
echo  Criando tabelas de Reconciliacao
echo ============================================
echo.

REM Configuracoes do banco
set PGHOST=localhost
set PGPORT=5432
set PGDATABASE=sispatrimonio
set PGUSER=inventario

echo Executando script 1: criar_tabela_reconciliacao.sql
echo.
"C:\Program Files\PostgreSQL\16\bin\psql.exe" -f sql/criar_tabela_reconciliacao.sql
if %ERRORLEVEL% NEQ 0 (
    echo [AVISO] Tentando com PostgreSQL 15...
    "C:\Program Files\PostgreSQL\15\bin\psql.exe" -f sql/criar_tabela_reconciliacao.sql
)

echo.
echo Executando script 2: criar_tabela_ocorrencia_patrimonio.sql
echo.
"C:\Program Files\PostgreSQL\16\bin\psql.exe" -f sql/criar_tabela_ocorrencia_patrimonio.sql
if %ERRORLEVEL% NEQ 0 (
    echo [AVISO] Tentando com PostgreSQL 15...
    "C:\Program Files\PostgreSQL\15\bin\psql.exe" -f sql/criar_tabela_ocorrencia_patrimonio.sql
)

echo.
echo ============================================
echo  Scripts executados!
echo ============================================
pause
