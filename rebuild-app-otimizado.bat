@echo off
echo ========================================
echo   REBUILD APP - CORRECAO ANR
echo ========================================
echo.

cd InventarioMobile

echo [1/4] Limpando build anterior...
call gradlew.bat clean
if errorlevel 1 (
    echo ERRO: Falha ao limpar build
    pause
    exit /b 1
)

echo.
echo [2/4] Compilando app (debug)...
call gradlew.bat assembleDebug
if errorlevel 1 (
    echo ERRO: Falha ao compilar app
    pause
    exit /b 1
)

echo.
echo [3/4] Verificando dispositivos conectados...
adb devices

echo.
echo [4/4] Instalando app no dispositivo...
adb install -r app\build\outputs\apk\debug\app-debug.apk
if errorlevel 1 (
    echo ERRO: Falha ao instalar app
    pause
    exit /b 1
)

echo.
echo ========================================
echo   APP INSTALADO COM SUCESSO!
echo ========================================
echo.
echo Correcoes aplicadas:
echo   - Removido runBlocking do AuthInterceptor
echo   - Reduzidos timeouts (10s/15s/30s)
echo   - Otimizacoes de performance
echo.
echo Teste os seguintes fluxos:
echo   1. Login
echo   2. Carregar dashboard
echo   3. Fazer coleta
echo   4. Sincronizar dados
echo.
pause
