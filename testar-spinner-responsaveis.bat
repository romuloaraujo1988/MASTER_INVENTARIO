@echo off
echo ========================================
echo Teste do Spinner de Responsaveis
echo ========================================
echo.

echo [1/5] Limpando build anterior...
cd InventarioMobile
call gradlew.bat clean
if errorlevel 1 (
    echo ERRO: Falha ao limpar build
    pause
    exit /b 1
)

echo.
echo [2/5] Compilando APK...
call gradlew.bat assembleDebug
if errorlevel 1 (
    echo ERRO: Falha ao compilar APK
    pause
    exit /b 1
)

echo.
echo [3/5] Instalando no emulador...
adb install -r app\build\outputs\apk\debug\app-debug.apk
if errorlevel 1 (
    echo ERRO: Falha ao instalar APK
    pause
    exit /b 1
)

echo.
echo [4/5] Limpando logs anteriores...
adb logcat -c

echo.
echo [5/5] Iniciando monitoramento de logs...
echo.
echo ========================================
echo LOGS DO APP (Ctrl+C para sair)
echo ========================================
echo.
echo Procure por:
echo   - "Responsaveis carregados com sucesso"
echo   - "Spinner configurado com sucesso"
echo.

adb logcat -s InventarioActivity:* InventarioViewModel:* InventarioRepository:*

pause
