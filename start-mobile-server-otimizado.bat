@echo off
REM =====================================================
REM Script de Inicialização OTIMIZADO - Servidor Mobile
REM Configurado para ~15 usuários e baixo consumo de memória
REM =====================================================

echo ========================================
echo  Servidor Mobile - Modo OTIMIZADO
echo  Configurado para ~15 usuarios
echo ========================================
echo.

REM Verificar se Java está instalado
java -version >nul 2>&1
if errorlevel 1 (
    echo ERRO: Java nao encontrado!
    echo Instale o JDK 21 ou superior.
    pause
    exit /b 1
)

REM Matar processos anteriores na porta 8081
echo [1/3] Liberando porta 8081...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8081 ^| findstr LISTENING') do (
    taskkill /F /PID %%a >nul 2>&1
)
timeout /t 2 /nobreak >nul

REM Configurações JVM OTIMIZADAS para baixa memória
REM -Xms256m: Memória inicial de 256MB
REM -Xmx512m: Memória máxima de 512MB (suficiente para 15 usuários)
REM -XX:+UseG1GC: Garbage Collector otimizado
REM -XX:MaxGCPauseMillis=100: Pausas curtas do GC
REM -XX:+UseStringDeduplication: Economiza memória com strings duplicadas
REM -XX:+ParallelRefProcEnabled: Processamento paralelo de referências
set JAVA_OPTS=-Xms256m -Xmx512m
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseG1GC
set JAVA_OPTS=%JAVA_OPTS% -XX:MaxGCPauseMillis=100
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseStringDeduplication
set JAVA_OPTS=%JAVA_OPTS% -XX:+ParallelRefProcEnabled
set JAVA_OPTS=%JAVA_OPTS% -XX:InitiatingHeapOccupancyPercent=45
set JAVA_OPTS=%JAVA_OPTS% -XX:G1HeapRegionSize=4m
set JAVA_OPTS=%JAVA_OPTS% -XX:+DisableExplicitGC

REM Desabilitar JMX e outras features que consomem memória
set JAVA_OPTS=%JAVA_OPTS% -Dcom.sun.management.jmxremote=false
set JAVA_OPTS=%JAVA_OPTS% -Djava.awt.headless=true

echo [2/3] Configuracoes JVM:
echo    Memoria inicial: 256MB
echo    Memoria maxima:  512MB
echo    GC: G1GC otimizado
echo.

echo [3/3] Iniciando servidor mobile...
echo.
echo Servidor iniciando na porta 8081...
echo Pressione Ctrl+C para parar
echo.

REM Iniciar com Maven
call mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mobile -Dspring-boot.run.jvmArguments="%JAVA_OPTS%"

pause
