@echo off
echo ========================================
echo Instalacao no Emulador Android
echo ========================================
echo.

cd InventarioMobile

echo Verificando dispositivos conectados...
call gradlew.bat devices
echo.

echo Instalando aplicativo no emulador...
call gradlew.bat installDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Instalacao concluida com sucesso!
    echo ========================================
    echo.
    echo O aplicativo foi instalado no emulador.
    echo Abra o emulador e procure por "SIHCP Mobile"
    echo.
) else (
    echo.
    echo ========================================
    echo ERRO na instalacao!
    echo ========================================
    echo.
    echo Verifique se:
    echo 1. O emulador esta em execucao
    echo 2. O Android Studio esta aberto
    echo 3. O emulador esta completamente inicializado
    echo.
)

cd ..
pause
