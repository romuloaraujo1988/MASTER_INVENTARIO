@echo off
REM ========================================
REM Script para executar aplicação Desktop
REM Sistema de Inventário - Versão 2.0.0
REM ========================================

echo.
echo ========================================
echo  Sistema de Inventario - Desktop
echo  Versao 2.0.0
echo ========================================
echo.

REM Verificar se Java está instalado
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERRO: Java nao encontrado!
    echo Por favor, instale o Java 21 ou superior.
    pause
    exit /b 1
)

REM Navegar para o diretório do JAR
cd /d "%~dp0target"

REM Verificar se o JAR existe
if not exist "mobile-server\sistema-inventario-2.0.0.jar" (
    echo ERRO: JAR nao encontrado!
    echo Execute: mvnw.cmd package -P thin-jar -DskipTests
    pause
    exit /b 1
)

REM Verificar se a pasta lib existe
if not exist "lib" (
    echo ERRO: Pasta lib nao encontrada!
    echo Execute: mvnw.cmd package -P thin-jar -DskipTests
    pause
    exit /b 1
)

echo Iniciando aplicacao desktop...
echo.

REM Executar o JAR com a classe principal do desktop
java -cp "mobile-server\sistema-inventario-2.0.0.jar;lib\*" com.inventario.SistemaInventarioApplication

echo.
echo Aplicacao encerrada.
pause
