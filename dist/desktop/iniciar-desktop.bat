@echo off
REM ============================================================================
REM Sistema de Inventario - Aplicacao Desktop
REM IFMT - Instituto Federal de Mato Grosso
REM ============================================================================

echo Iniciando Sistema de Inventario Desktop...
echo.

REM Verificar se Java esta instalado
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERRO] Java nao encontrado!
    echo Por favor, instale o Java 21 ou superior.
    pause
    exit /b 1
)

REM Executar aplicacao
java -jar sistema-inventario-desktop.jar

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERRO] Falha ao iniciar a aplicacao!
    pause
    exit /b 1
)
