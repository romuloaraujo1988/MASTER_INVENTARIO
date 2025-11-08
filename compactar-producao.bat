@echo off
REM ============================================================================
REM Compactar Pacote de Producao
REM ============================================================================

echo.
echo ============================================================================
echo   COMPACTANDO PACOTE DE PRODUCAO
echo ============================================================================
echo.

set PROD_DIR=SISTEMA_INVENTARIO_PRODUCAO_V1.2.0
set ZIP_FILE=SISTEMA_INVENTARIO_PRODUCAO_V1.2.0.zip

if not exist %PROD_DIR% (
    echo [ERRO] Diretorio %PROD_DIR% nao encontrado!
    echo Execute primeiro: criar-pacote-producao.bat
    pause
    exit /b 1
)

echo Compactando %PROD_DIR%...
echo.

REM Usar PowerShell para compactar
powershell -Command "Compress-Archive -Path '%PROD_DIR%' -DestinationPath '%ZIP_FILE%' -Force"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================================================
    echo   COMPACTACAO CONCLUIDA!
    echo ============================================================================
    echo.
    echo Arquivo criado: %ZIP_FILE%
    echo.
    for %%A in (%ZIP_FILE%) do echo Tamanho: %%~zA bytes
    echo.
    echo Este arquivo pode ser distribuido para instalacao.
    echo.
) else (
    echo.
    echo [ERRO] Falha ao compactar!
    echo.
)

pause
