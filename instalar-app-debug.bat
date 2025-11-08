@echo off
echo ========================================
echo Instalacao do SIHCP Mobile (Debug)
echo ========================================
echo.

REM Verificar se o ADB esta disponivel
where adb >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERRO: ADB nao encontrado!
    echo.
    echo Por favor, instale o Android SDK Platform Tools
    echo ou adicione o caminho do ADB ao PATH do sistema.
    echo.
    pause
    exit /b 1
)

echo Verificando dispositivos conectados...
adb devices
echo.

echo Desinstalando versao anterior (se existir)...
adb uninstall com.inventario.mobile.debug
echo.

echo Instalando nova versao...
adb install -r SIHCP-Mobile-Debug.apk

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Instalacao concluida com sucesso!
    echo ========================================
    echo.
    echo O aplicativo foi instalado no dispositivo.
    echo Abra o app e teste a funcionalidade de coleta rapida com QR Code.
    echo.
) else (
    echo.
    echo ========================================
    echo ERRO na instalacao!
    echo ========================================
    echo.
    echo Verifique se:
    echo 1. O dispositivo esta conectado via USB
    echo 2. A depuracao USB esta ativada
    echo 3. O dispositivo esta autorizado no computador
    echo.
)

pause
