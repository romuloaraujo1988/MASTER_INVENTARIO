@echo off
REM ============================================================
REM Servidor Mobile - OTIMIZADO PARA BAIXO CONSUMO DE MEMÓRIA
REM ============================================================
REM 
REM PROBLEMA RESOLVIDO: Servidor saltava de 200MB para 3GB
REM CAUSA: Métodos carregavam TODAS as coletas do inventário
REM SOLUÇÃO: Queries otimizadas + limites de memória JVM
REM
REM ============================================================

echo ============================================================
echo   SERVIDOR MOBILE - MEMORIA OTIMIZADA
echo ============================================================
echo.

REM Verificar se já existe processo na porta 8081
netstat -ano | findstr :8081 >nul 2>&1
if %errorlevel%==0 (
    echo [AVISO] Porta 8081 ja esta em uso!
    echo Encerrando processo anterior...
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081 ^| findstr LISTENING') do (
        taskkill /F /PID %%a >nul 2>&1
    )
    timeout /t 2 >nul
)

echo.
echo Configuracoes de memoria:
echo   - Memoria inicial: 128MB
echo   - Memoria maxima: 512MB
echo   - GC: G1GC (otimizado para baixa latencia)
echo.

REM Configurações JVM otimizadas para baixo consumo de memória
set JAVA_OPTS=-Xms128m -Xmx512m
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseG1GC
set JAVA_OPTS=%JAVA_OPTS% -XX:MaxGCPauseMillis=100
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseStringDeduplication
set JAVA_OPTS=%JAVA_OPTS% -XX:+OptimizeStringConcat
set JAVA_OPTS=%JAVA_OPTS% -XX:+HeapDumpOnOutOfMemoryError
set JAVA_OPTS=%JAVA_OPTS% -XX:HeapDumpPath=logs/heapdump.hprof

echo Iniciando servidor na porta 8081...
echo.

java %JAVA_OPTS% -jar target/mobile-server/sistema-inventario-2.0.0.jar --spring.profiles.active=mobile --server.port=8081

pause
