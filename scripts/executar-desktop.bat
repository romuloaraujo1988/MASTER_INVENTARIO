@echo off
REM ============================================
REM Sistema de Inventario - Desktop (Modulado)
REM ============================================

setlocal enabledelayedexpansion

REM Diretório do módulo desktop
set "DESKTOP_DIR=%~dp0sihcp-desktop\target"
set "ROOT_DIR=%~dp0"

echo ========================================
echo  Sistema de Inventario - IFMT
echo  Versao: 2.7.0
echo ========================================
echo.

REM [1/3] Verificar Java 17+
echo [1/3] Verificando Java...
java -version 2>&1 | findstr /i "version" > nul
if errorlevel 1 (
    echo [ERRO] Java nao encontrado. Instale o JDK 17+.
    pause
    exit /b 1
)
echo [OK] Java detectado.

REM [2/3] Verificar JAR
echo [2/3] Verificando executavel...
if not exist "%DESKTOP_DIR%\sihcp-desktop-2.7.0.jar" (
    echo [ERRO] Executavel nao encontrado em %DESKTOP_DIR%
    echo        Execute 'mvn clean package' primeiro.
    pause
    exit /b 1
)
echo [OK] Executavel encontrado.

REM [3/3] Iniciar aplicação
echo [3/3] Iniciando aplicacao...
echo.

cd /d "%ROOT_DIR%"
java -Xms512m -Xmx2g ^
    -Dfile.encoding=UTF-8 ^
    -Dswing.defaultlaf=com.sun.java.swing.plaf.windows.WindowsLookAndFeel ^
    -jar "%DESKTOP_DIR%\sihcp-desktop-2.7.0.jar"

if errorlevel 1 (
    echo.
    echo [AVISO] Aplicacao encerrada com codigo %errorlevel%
    pause
)
