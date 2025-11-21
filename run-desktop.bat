@echo off
REM ========================================
REM Script para executar aplicação Desktop
REM Sistema de Inventário - Versão 2.0.0
REM ========================================

echo.
echo ========================================
echo   Sistema de Inventario - Desktop
echo   Versao 2.0.0
echo ========================================
echo.

REM Verificar se Java está instalado
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERRO] Java nao encontrado!
    echo Por favor, instale o Java 21 ou superior.
    pause
    exit /b 1
)

REM Verificar se o JAR existe
if not exist "target\mobile-server\sistema-inventario-2.0.0.jar" (
    echo [ERRO] JAR nao encontrado!
    echo Execute primeiro: mvnw.cmd clean package -P thin-jar -DskipTests
    pause
    exit /b 1
)

REM Verificar se o diretório lib existe
if not exist "target\lib" (
    echo [ERRO] Diretorio lib/ nao encontrado!
    echo Execute primeiro: mvnw.cmd clean package -P thin-jar -DskipTests
    pause
    exit /b 1
)

echo [INFO] Iniciando aplicacao desktop...
echo [INFO] JAR: target\mobile-server\sistema-inventario-2.0.0.jar
echo [INFO] Dependencias: target\lib\
echo.

REM Executar aplicação desktop (SistemaInventarioApplication)
cd target\mobile-server
java -jar sistema-inventario-2.0.0.jar

pause
