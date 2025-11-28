@echo off
chcp 65001 >nul
title Servidor Mobile API - OTIMIZADO (HikariCP Pool)

echo ═══════════════════════════════════════════════════════════════════════════════
echo   🚀 SERVIDOR MOBILE API - VERSÃO OTIMIZADA
echo ═══════════════════════════════════════════════════════════════════════════════
echo.
echo   ✅ HikariCP Connection Pool (15 conexões máx)
echo   ✅ Limite de memória: 2GB
echo   ✅ G1GC com pausas curtas
echo   ✅ Detecção de vazamento de conexões
echo.
echo ═══════════════════════════════════════════════════════════════════════════════
echo.

REM Verificar se o JAR existe
if not exist "target\sistema-inventario-1.2.0.jar" (
    echo ❌ JAR não encontrado! Execute: mvn clean package -DskipTests
    pause
    exit /b 1
)

REM Matar processo anterior na porta 8081
echo 🔄 Verificando porta 8081...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8081 ^| findstr LISTENING') do (
    echo    Finalizando processo %%a na porta 8081...
    taskkill /F /PID %%a >nul 2>&1
)

echo.
echo 🚀 Iniciando servidor na porta 8081...
echo.

REM ═══════════════════════════════════════════════════════════════════════════════
REM CONFIGURAÇÕES JVM OTIMIZADAS
REM ═══════════════════════════════════════════════════════════════════════════════
REM
REM MEMÓRIA:
REM   -Xms512m        : Memória inicial 512MB
REM   -Xmx2g          : Memória máxima 2GB
REM   -XX:MaxMetaspaceSize=256m : Limite metaspace
REM
REM GARBAGE COLLECTOR (G1GC):
REM   -XX:+UseG1GC              : Usar G1 Garbage Collector
REM   -XX:MaxGCPauseMillis=100  : Pausas curtas (100ms máx)
REM   -XX:+ParallelRefProcEnabled : Processamento paralelo de referências
REM
REM OTIMIZAÇÕES:
REM   -XX:+UseStringDeduplication : Deduplica strings (economia de memória)
REM   -XX:+OptimizeStringConcat   : Otimiza concatenação de strings
REM
REM DIAGNÓSTICO:
REM   -XX:+HeapDumpOnOutOfMemoryError : Dump em caso de OOM
REM   -XX:HeapDumpPath=logs/          : Local do dump
REM
REM ═══════════════════════════════════════════════════════════════════════════════

java ^
    -Xms512m ^
    -Xmx2g ^
    -XX:MaxMetaspaceSize=256m ^
    -XX:+UseG1GC ^
    -XX:MaxGCPauseMillis=100 ^
    -XX:+ParallelRefProcEnabled ^
    -XX:+UseStringDeduplication ^
    -XX:+OptimizeStringConcat ^
    -XX:+UseCompressedOops ^
    -XX:+HeapDumpOnOutOfMemoryError ^
    -XX:HeapDumpPath=logs/ ^
    -Dfile.encoding=UTF-8 ^
    -Dspring.profiles.active=mobile ^
    -Dserver.port=8081 ^
    -jar target\sistema-inventario-1.2.0.jar

echo.
echo ═══════════════════════════════════════════════════════════════════════════════
echo   Servidor finalizado
echo ═══════════════════════════════════════════════════════════════════════════════
pause
