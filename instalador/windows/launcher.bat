@echo off
REM Launcher para SIHCP - Sistema de Inventário
REM Este script inicia a aplicação Java

REM Definir diretório da aplicação
set APP_DIR=%~dp0
set JAR_FILE=%APP_DIR%sistema-inventario.jar

REM Verificar se Java está instalado
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERRO: Java nao foi encontrado!
    echo.
    echo O SIHCP requer Java 21 ou superior.
    echo Por favor, instale o Java de: https://adoptium.net/
    echo.
    pause
    exit /b 1
)

REM Verificar se JAR existe
if not exist "%JAR_FILE%" (
    echo ERRO: Arquivo JAR nao encontrado!
    echo Esperado em: %JAR_FILE%
    echo.
    pause
    exit /b 1
)

REM Configurar memória JVM
set JAVA_OPTS=-Xms256m -Xmx1024m

REM Configurar encoding
set JAVA_OPTS=%JAVA_OPTS% -Dfile.encoding=UTF-8

REM Configurar Look and Feel
set JAVA_OPTS=%JAVA_OPTS% -Dswing.defaultlaf=com.sun.java.swing.plaf.windows.WindowsLookAndFeel

REM Verificar parâmetros
if "%1"=="--config" (
    REM Abrir apenas configuração
    set JAVA_OPTS=%JAVA_OPTS% -Dinventario.config.only=true
)

REM Iniciar aplicação
echo Iniciando SIHCP - Sistema de Inventario...
start "SIHCP" javaw %JAVA_OPTS% -jar "%JAR_FILE%" %*

REM Aguardar um pouco para verificar se iniciou
timeout /t 2 /nobreak >nul

REM Verificar se processo está rodando
tasklist /FI "WINDOWTITLE eq SIHCP*" 2>nul | find /I "java" >nul
if %ERRORLEVEL% EQU 0 (
    echo Aplicacao iniciada com sucesso!
) else (
    echo AVISO: Nao foi possivel verificar se a aplicacao iniciou.
    echo Se houver problemas, execute: java -jar "%JAR_FILE%"
    pause
)

exit /b 0
