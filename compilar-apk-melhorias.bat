@echo off
echo ========================================
echo Compilando APK com Melhorias Offline
echo ========================================
echo.

cd InventarioMobile

echo [1/3] Limpando build anterior...
call gradlew.bat clean

echo.
echo [2/3] Compilando APK Debug...
call gradlew.bat assembleDebug

echo.
echo [3/3] Verificando APK gerado...
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo.
    echo ========================================
    echo APK COMPILADO COM SUCESSO!
    echo ========================================
    echo.
    echo Localização: InventarioMobile\app\build\outputs\apk\debug\app-debug.apk
    echo.
    dir "app\build\outputs\apk\debug\app-debug.apk"
    echo.
) else (
    echo.
    echo ========================================
    echo ERRO: APK não foi gerado
    echo ========================================
    echo.
)

cd ..

echo.
echo Pressione qualquer tecla para sair...
pause >nul
