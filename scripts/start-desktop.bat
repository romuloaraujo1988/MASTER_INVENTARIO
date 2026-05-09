@echo off
REM ============================================
REM Sistema de Inventario - Desktop (Modulado)
REM ============================================

setlocal enabledelayedexpansion

REM Caminho relativo para o JAR do módulo desktop
set "JAR_PATH=%~dp0..\sihcp-desktop\target\sihcp-desktop-2.7.0.jar"

echo.
echo ========================================
echo  Sistema de Inventario - IFMT
echo  Versao: 2.7.0
echo ========================================
echo.

REM Verificar se Java esta instalado
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERRO: Java nao encontrado!
    echo Por favor, instale o Java 17 ou superior.
    echo.
    pause
    exit /b 1
)

REM Configurar memoria JVM e Look and Feel
set JAVA_OPTS=-Xms512m -Xmx2048m -Dfile.encoding=UTF-8 -Dswing.defaultlaf=com.sun.java.swing.plaf.windows.WindowsLookAndFeel

echo Iniciando aplicacao...
echo.

REM Executar aplicacao a partir da raiz (um nivel acima de scripts/)
cd /d "%~dp0.."
java %JAVA_OPTS% -jar "%JAR_PATH%"

if %errorlevel% neq 0 (
    echo.
    echo ERRO: Falha ao iniciar a aplicacao.
    echo.
    pause
    exit /b 1
)
