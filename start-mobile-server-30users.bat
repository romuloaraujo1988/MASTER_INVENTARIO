@echo off
REM =====================================================
REM Script de Inicialização - Servidor Mobile Spring Boot
REM OTIMIZADO PARA 30 USUÁRIOS COM MELHOR DESEMPENHO
REM =====================================================
REM Requisitos:
REM - Java 21+
REM - 4GB RAM disponível (mínimo 2GB)
REM - 4 cores CPU (mínimo 2)
REM =====================================================

title Servidor Mobile - 30 Usuarios (Otimizado)

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║  SERVIDOR MOBILE - SPRING BOOT                                 ║
echo ║  Otimizado para 30 usuarios simultaneos                        ║
echo ╠════════════════════════════════════════════════════════════════╣
echo ║  Porta: 8081                                                   ║
echo ║  Contexto: /inventario                                         ║
echo ║  Perfil: mobile                                                ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

REM Verificar se Java está instalado
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERRO] Java nao encontrado! Instale o JDK 21+
    pause
    exit /b 1
)

REM Verificar se o JAR existe
set JAR_FILE=target\sistema-inventario-1.2.0.jar
if not exist "%JAR_FILE%" (
    echo [AVISO] JAR nao encontrado. Compilando...
    call mvn clean package -DskipTests -q
    if errorlevel 1 (
        echo [ERRO] Falha na compilacao!
        pause
        exit /b 1
    )
)

echo [INFO] Iniciando servidor com configuracoes otimizadas...
echo.

REM =====================================================
REM PARAMETROS JVM OTIMIZADOS PARA 30 USUARIOS
REM =====================================================
REM
REM MEMORIA:
REM   -Xms1g        : Memoria inicial 1GB (evita resize)
REM   -Xmx2g        : Memoria maxima 2GB
REM   -XX:MaxMetaspaceSize=256m : Limite metaspace
REM
REM GARBAGE COLLECTOR (G1GC - melhor para aplicacoes web):
REM   -XX:+UseG1GC              : Usar G1 Garbage Collector
REM   -XX:MaxGCPauseMillis=100  : Pausas GC max 100ms
REM   -XX:G1HeapRegionSize=16m  : Tamanho regiao heap
REM   -XX:+ParallelRefProcEnabled : Processamento paralelo
REM
REM PERFORMANCE:
REM   -XX:+UseStringDeduplication : Deduplica strings (economia memoria)
REM   -XX:+OptimizeStringConcat   : Otimiza concatenacao
REM   -XX:+UseCompressedOops      : Ponteiros comprimidos
REM
REM DIAGNOSTICO:
REM   -XX:+HeapDumpOnOutOfMemoryError : Dump em OOM
REM   -XX:HeapDumpPath=logs/          : Local do dump
REM
REM =====================================================

java ^
    -Xms1g ^
    -Xmx2g ^
    -XX:MaxMetaspaceSize=256m ^
    -XX:+UseG1GC ^
    -XX:MaxGCPauseMillis=100 ^
    -XX:G1HeapRegionSize=16m ^
    -XX:+ParallelRefProcEnabled ^
    -XX:+UseStringDeduplication ^
    -XX:+OptimizeStringConcat ^
    -XX:+UseCompressedOops ^
    -XX:+HeapDumpOnOutOfMemoryError ^
    -XX:HeapDumpPath=logs/ ^
    -Dfile.encoding=UTF-8 ^
    -Dspring.profiles.active=mobile ^
    -Dserver.port=8081 ^
    -jar "%JAR_FILE%"

echo.
echo [INFO] Servidor encerrado.
pause
