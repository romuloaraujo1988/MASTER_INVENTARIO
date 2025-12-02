@echo off
chcp 65001 >nul
title Servidor Mobile - Memória Controlada (Max 1GB)

echo ═══════════════════════════════════════════════════════════════════════════════
echo   SERVIDOR MOBILE - MEMÓRIA CONTROLADA
echo   Limite: 1GB (evita travamento em máquinas com 4GB RAM)
echo ═══════════════════════════════════════════════════════════════════════════════
echo.

REM Verificar se já existe um servidor rodando na porta 8081
netstat -ano | findstr ":8081" >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ⚠️  AVISO: Já existe um processo na porta 8081!
    echo    Feche o servidor existente antes de iniciar um novo.
    echo.
    pause
    exit /b 1
)

echo 📋 Configurações de Memória:
echo    - Memória Inicial: 256MB
echo    - Memória Máxima: 1GB (limite rígido)
echo    - Metaspace: 128MB
echo    - GC: G1GC com pausas curtas
echo.
echo 📋 Otimizações Ativas:
echo    - Compressão de ponteiros (UseCompressedOops)
echo    - Deduplicação de strings
echo    - Heap dump em caso de OOM
echo.

REM Configurações JVM otimizadas para memória limitada
set JAVA_OPTS=-Xms256m -Xmx1g
set JAVA_OPTS=%JAVA_OPTS% -XX:MaxMetaspaceSize=128m

REM G1GC com pausas curtas e coleta agressiva
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseG1GC
set JAVA_OPTS=%JAVA_OPTS% -XX:MaxGCPauseMillis=100
set JAVA_OPTS=%JAVA_OPTS% -XX:G1HeapRegionSize=8m
set JAVA_OPTS=%JAVA_OPTS% -XX:InitiatingHeapOccupancyPercent=45
set JAVA_OPTS=%JAVA_OPTS% -XX:G1ReservePercent=15

REM Otimizações de memória
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseCompressedOops
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseStringDeduplication
set JAVA_OPTS=%JAVA_OPTS% -XX:+OptimizeStringConcat

REM Diagnóstico em caso de problemas
set JAVA_OPTS=%JAVA_OPTS% -XX:+HeapDumpOnOutOfMemoryError
set JAVA_OPTS=%JAVA_OPTS% -XX:HeapDumpPath=logs/
set JAVA_OPTS=%JAVA_OPTS% -XX:+ExitOnOutOfMemoryError

REM Desabilitar features que consomem memória
set JAVA_OPTS=%JAVA_OPTS% -XX:+DisableExplicitGC

REM Encoding e profile
set JAVA_OPTS=%JAVA_OPTS% -Dfile.encoding=UTF-8
set JAVA_OPTS=%JAVA_OPTS% -Dspring.profiles.active=mobile

echo 🚀 Iniciando servidor mobile...
echo    Porta: 8081
echo    Profile: mobile
echo.
echo ═══════════════════════════════════════════════════════════════════════════════
echo.

REM Iniciar servidor
java %JAVA_OPTS% -jar target/sistema-inventario-1.2.0.jar

echo.
echo ═══════════════════════════════════════════════════════════════════════════════
echo   Servidor encerrado.
echo ═══════════════════════════════════════════════════════════════════════════════
pause
