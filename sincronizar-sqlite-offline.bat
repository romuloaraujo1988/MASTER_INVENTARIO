@echo off
chcp 65001 >nul
cls

echo ========================================
echo   Sincronização PostgreSQL -^> SQLite
echo ========================================
echo.

REM Verificar se o banco SQLite existe
if exist "data\inventario.db" (
    echo [INFO] Banco SQLite encontrado: data\inventario.db
) else (
    echo [AVISO] Banco SQLite não encontrado. Criando...
    
    REM Criar diretório data se não existir
    if not exist "data" mkdir data
    
    REM Criar banco SQLite executando o script
    if exist "sql\criar_tabelas_sqlite_offline.sql" (
        echo [INFO] Executando script de criação...
        REM Usar sqlite3 se disponível, senão criar via Java
        where sqlite3 >nul 2>&1
        if %ERRORLEVEL% EQU 0 (
            sqlite3 data\inventario.db < sql\criar_tabelas_sqlite_offline.sql
            echo [OK] Banco SQLite criado com sucesso!
        ) else (
            echo [INFO] sqlite3 não encontrado. Criando via Java...
        )
    )
)

echo.
echo [INFO] Iniciando sincronização de dados...
echo.

REM Executar classe Java de sincronização
mvn exec:java -Dexec.mainClass="com.inventario.offline.SyncPostgresToSQLite" -Dexec.cleanupDaemonThreads=false

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo   Sincronização concluída com sucesso!
    echo ========================================
    echo.
    echo [INFO] Banco SQLite atualizado: data\inventario.db
    echo [INFO] O sistema está pronto para operar em modo offline.
) else (
    echo.
    echo ========================================
    echo   ERRO na sincronização!
    echo ========================================
    echo.
    echo [ERRO] Verifique os logs acima para mais detalhes.
    echo [DICA] Certifique-se de que o PostgreSQL está acessível.
)

echo.
pause
