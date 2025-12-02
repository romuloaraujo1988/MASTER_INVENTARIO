@echo off
chcp 65001 >nul
title Servidor Mobile - LOW MEMORY (Max 512MB)

echo ═══════════════════════════════════════════════════════════════════════════════
echo   SERVIDOR MOBILE - MODO LOW MEMORY
echo   Limite RÍGIDO: 512MB Heap + 128MB Metaspace = ~700MB total
echo ═══════════════════════════════════════════════════════════════════════════════
echo.

REM Verificar se já existe um servidor rodando na porta 8081
netstat -ano | findstr ":8081" | findstr "LISTENING" >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ⚠️  AVISO: Já existe um processo na porta 8081!
    for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8081 ^| findstr LISTENING') do (
        echo    Finalizando processo %%a...
        taskkill /F /PID %%a >nul 2>&1
    )
    timeout /t 2 >nul
)

echo 📋 Configurações de Memória (ULTRA LOW):
echo    - Heap Inicial: 128MB
echo    - Heap Máximo: 512MB
echo    - Metaspace: 128MB
echo    - Thread Stack: 256KB (padrão é 1MB)
echo.
echo 📋 Otimizações Ativas:
echo    - G1GC com coleta agressiva
echo    - Compressão de ponteiros
echo    - Deduplicação de strings
echo    - Threads com stack reduzido
echo.

REM ═══════════════════════════════════════════════════════════════════════════════
REM CONFIGURAÇÕES JVM ULTRA LOW MEMORY
REM ═══════════════════════════════════════════════════════════════════════════════

set JAVA_OPTS=-Xms128m -Xmx512m
set JAVA_OPTS=%JAVA_OPTS% -XX:MaxMetaspaceSize=128m

REM Stack de threads reduzido (256KB ao invés de 1MB)
set JAVA_OPTS=%JAVA_OPTS% -Xss256k

REM G1GC com coleta agressiva
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseG1GC
set JAVA_OPTS=%JAVA_OPTS% -XX:MaxGCPauseMillis=50
set JAVA_OPTS=%JAVA_OPTS% -XX:G1HeapRegionSize=4m
set JAVA_OPTS=%JAVA_OPTS% -XX:InitiatingHeapOccupancyPercent=35
set JAVA_OPTS=%JAVA_OPTS% -XX:G1ReservePercent=20

REM Otimizações de memória
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseCompressedOops
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseCompressedClassPointers
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseStringDeduplication
set JAVA_OPTS=%JAVA_OPTS% -XX:+OptimizeStringConcat

REM Desabilitar features que consomem memória
set JAVA_OPTS=%JAVA_OPTS% -XX:+DisableExplicitGC
set JAVA_OPTS=%JAVA_OPTS% -XX:+AlwaysPreTouch

REM Diagnóstico
set JAVA_OPTS=%JAVA_OPTS% -XX:+HeapDumpOnOutOfMemoryError
set JAVA_OPTS=%JAVA_OPTS% -XX:HeapDumpPath=logs/
set JAVA_OPTS=%JAVA_OPTS% -XX:+ExitOnOutOfMemoryError

REM Encoding e profile
set JAVA_OPTS=%JAVA_OPTS% -Dfile.encoding=UTF-8
set JAVA_OPTS=%JAVA_OPTS% -Dspring.profiles.active=mobile

REM Desabilitar recursos Spring que consomem memória
set JAVA_OPTS=%JAVA_OPTS% -Dspring.jmx.enabled=false
set JAVA_OPTS=%JAVA_OPTS% -Dspring.main.lazy-initialization=true

echo 🚀 Iniciando servidor mobile (LOW MEMORY)...
echo    Porta: 8081
echo    Profile: mobile
echo    Memória máxima: 512MB
echo.
echo ═══════════════════════════════════════════════════════════════════════════════
echo.

REM Iniciar servidor
java %JAVA_OPTS% -jar target/sistema-inventario-1.2.0.jar --server.port=8081

echo.
echo ═══════════════════════════════════════════════════════════════════════════════
echo   Servidor encerrado.
echo ═══════════════════════════════════════════════════════════════════════════════
pause
