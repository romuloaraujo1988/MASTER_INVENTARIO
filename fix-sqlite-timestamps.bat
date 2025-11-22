@echo off
REM ========================================
REM Script para corrigir timestamps SQLite
REM Sistema de Inventário - Versão 2.0.0
REM ========================================

echo.
echo ========================================
echo   Correcao de Timestamps SQLite
echo   Sistema de Inventario v2.0.0
echo ========================================
echo.

REM Verificar se o banco SQLite existe
set SQLITE_DB=data\inventario_offline.db

if not exist "%SQLITE_DB%" (
    echo [AVISO] Banco de dados SQLite nao encontrado: %SQLITE_DB%
    echo [INFO] O banco sera criado automaticamente no primeiro uso offline.
    echo.
    pause
    exit /b 0
)

echo [INFO] Banco de dados encontrado: %SQLITE_DB%
echo.

REM Verificar se sqlite3.exe está disponível
where sqlite3 >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERRO] sqlite3.exe nao encontrado no PATH!
    echo.
    echo Para instalar o SQLite:
    echo 1. Baixe de: https://www.sqlite.org/download.html
    echo 2. Extraia sqlite3.exe para uma pasta no PATH
    echo 3. Ou coloque sqlite3.exe na pasta do projeto
    echo.
    pause
    exit /b 1
)

echo [INFO] SQLite encontrado: 
sqlite3 -version
echo.

REM Criar backup antes de executar
echo [INFO] Criando backup do banco de dados...
set BACKUP_FILE=data\inventario_offline_backup_%date:~-4,4%%date:~-7,2%%date:~-10,2%_%time:~0,2%%time:~3,2%%time:~6,2%.db
set BACKUP_FILE=%BACKUP_FILE: =0%

copy "%SQLITE_DB%" "%BACKUP_FILE%" >nul
if %errorlevel% neq 0 (
    echo [ERRO] Falha ao criar backup!
    pause
    exit /b 1
)

echo [OK] Backup criado: %BACKUP_FILE%
echo.

REM Executar script de correção
echo [INFO] Executando script de correcao de timestamps...
echo.

sqlite3 "%SQLITE_DB%" < sql\fix_sqlite_timestamp_compatibility.sql

if %errorlevel% neq 0 (
    echo.
    echo [ERRO] Falha ao executar script de correcao!
    echo [INFO] Restaurando backup...
    copy "%BACKUP_FILE%" "%SQLITE_DB%" >nul
    echo [OK] Backup restaurado.
    pause
    exit /b 1
)

echo.
echo [OK] Script executado com sucesso!
echo.

REM Verificar se há timestamps inválidos
echo [INFO] Verificando formato de timestamps...
echo.

sqlite3 "%SQLITE_DB%" "SELECT COUNT(*) as total_invalidos FROM v_timestamp_validation WHERE status_formato = 'FORMATO_INVALIDO';" > temp_validation.txt

set /p INVALIDOS=<temp_validation.txt
del temp_validation.txt

if "%INVALIDOS%"=="0" (
    echo [OK] Todos os timestamps estao no formato correto!
) else (
    echo [AVISO] Ainda existem %INVALIDOS% timestamp(s) em formato invalido.
    echo [INFO] Execute novamente ou verifique manualmente.
)

echo.
echo ========================================
echo   CORRECAO CONCLUIDA
echo ========================================
echo.
echo [INFO] Backup mantido em: %BACKUP_FILE%
echo [INFO] Para remover backups antigos, delete arquivos em data\*_backup_*.db
echo.
pause
