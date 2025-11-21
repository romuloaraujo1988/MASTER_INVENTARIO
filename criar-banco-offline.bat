@echo off
echo ========================================
echo Criando Banco de Dados SQLite Offline
echo ========================================
echo.

REM Criar diretório data se não existir
if not exist "data" mkdir data

REM Verificar se o arquivo SQL existe
if not exist "sql\criar_tabelas_sqlite_offline.sql" (
    echo ERRO: Arquivo sql\criar_tabelas_sqlite_offline.sql nao encontrado!
    pause
    exit /b 1
)

echo Criando banco de dados: data\inventario.db
echo.

REM Executar script SQL usando sqlite3
REM Nota: Você precisa ter o sqlite3.exe no PATH ou na pasta atual
sqlite3 data\inventario.db < sql\criar_tabelas_sqlite_offline.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Banco SQLite criado com sucesso!
    echo ========================================
    echo.
    echo Localizacao: data\inventario.db
    echo.
) else (
    echo.
    echo ========================================
    echo ERRO ao criar banco SQLite!
    echo ========================================
    echo.
    echo Verifique se o sqlite3.exe esta instalado.
    echo Download: https://www.sqlite.org/download.html
    echo.
)

pause
