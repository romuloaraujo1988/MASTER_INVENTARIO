@echo off
echo ============================================================
echo   SINCRONIZACAO PostgreSQL -^> SQLite
echo ============================================================
echo.

echo [1/3] Compilando projeto...
call mvnw.cmd compile -q
if errorlevel 1 (
    echo ERRO: Falha na compilacao
    exit /b 1
)
echo    OK Compilacao concluida
echo.

echo [2/3] Criando backup do banco SQLite...
if exist data\inventario.db (
    for /f "tokens=2-4 delims=/ " %%a in ('date /t') do (set mydate=%%c%%b%%a)
    for /f "tokens=1-2 delims=/:" %%a in ('time /t') do (set mytime=%%a%%b)
    copy data\inventario.db data\inventario_backup_%mydate%_%mytime%.db >nul
    echo    OK Backup criado
) else (
    echo    INFO Nenhum banco existente para backup
)
echo.

echo [3/3] Executando sincronizacao...
echo ============================================================
echo.
call mvnw.cmd exec:java -Dexec.mainClass="com.inventario.offline.SyncPostgresToSQLiteV2" -Dexec.cleanupDaemonThreads=false
echo.
echo ============================================================
if errorlevel 1 (
    echo   ERRO NA SINCRONIZACAO!
    echo ============================================================
    exit /b 1
) else (
    echo   SINCRONIZACAO CONCLUIDA COM SUCESSO!
    echo ============================================================
)
