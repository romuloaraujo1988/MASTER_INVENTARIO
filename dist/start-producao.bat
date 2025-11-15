@echo off
REM ========================================
REM Script de Inicialização - Modo Produção
REM Sistema de Inventário
REM ========================================

echo.
echo ========================================
echo   SISTEMA DE INVENTARIO - PRODUCAO
echo ========================================
echo.

REM Verificar se o Java está instalado
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERRO] Java nao encontrado!
    echo Por favor, instale o Java 21 ou superior.
    pause
    exit /b 1
)

REM Verificar versão do Java
echo Verificando versao do Java...
java -version

REM Criar diretório de logs se não existir (na raiz do projeto)
if not exist "..\logs" mkdir "..\logs"

REM Copiar application-prod.properties para a raiz
if exist "application-prod.properties" (
    copy /Y "application-prod.properties" ".." >nul
)

REM Definir variáveis de ambiente
set JAVA_OPTS=-Xms512m -Xmx2048m -XX:+UseG1GC -XX:MaxGCPauseMillis=200
set SPRING_PROFILES_ACTIVE=prod

echo.
echo Iniciando sistema em modo PRODUCAO...
echo.
echo Configuracoes:
echo - Perfil: PRODUCAO
echo - Memoria: 512MB - 2GB
echo - Porta: 8080
echo - Logs: ..\logs\sistema-inventario-prod.log
echo.

REM Verificar se o JAR existe
if not exist "..\target\sistema-inventario-2.0.0.jar" (
    echo [AVISO] JAR nao encontrado. Compilando...
    cd ..
    call mvn clean package -DskipTests
    cd dist
    if %errorlevel% neq 0 (
        echo [ERRO] Falha na compilacao!
        pause
        exit /b 1
    )
)

REM Iniciar aplicação
echo Iniciando aplicacao...
cd ..
java %JAVA_OPTS% -Dspring.profiles.active=%SPRING_PROFILES_ACTIVE% -jar target\sistema-inventario-2.0.0.jar
cd dist

REM Se a aplicação encerrar, pausar para ver mensagens
if %errorlevel% neq 0 (
    echo.
    echo [ERRO] Aplicacao encerrada com erro!
    pause
)
