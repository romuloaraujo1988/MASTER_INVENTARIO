@echo off
echo ========================================
echo Verificando URL do App
echo ========================================
echo.
echo Aguardando logs de requisicao HTTP...
echo.
echo INSTRUCOES:
echo 1. Faca login no app
echo 2. Tente sincronizar
echo 3. Observe as URLs abaixo
echo.
echo Pressione Ctrl+C para parar
echo ========================================
echo.

adb logcat -c
adb logcat -v time -s "NetworkModule:*" | findstr /C:"URL:" /C:"Base URL"
