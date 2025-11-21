@echo off
echo ========================================
echo Teste de Sincronizacao - App Android
echo ========================================
echo.

echo 1. Limpando logs anteriores...
adb logcat -c
echo    OK
echo.

echo 2. Abrindo app...
adb shell am start -n com.inventario.mobile.debug/.presentation.main.MainActivity
timeout /t 2 >nul
echo    OK
echo.

echo 3. Iniciando monitoramento de logs...
echo.
echo ========================================
echo AGUARDANDO LOGS DE SINCRONIZACAO
echo ========================================
echo.
echo INSTRUCOES:
echo 1. No app, abra o menu lateral (drawer)
echo 2. Clique em "Dados" -^> "Sincronizacao"
echo 3. Clique no botao "Sincronizar do Servidor"
echo.
echo Pressione Ctrl+C para parar o monitoramento
echo.
echo ========================================
echo.

adb logcat -v time ^
  -s "SyncRepository:*" ^
     "SyncViewModel:*" ^
     "SyncActivity:*" ^
     "PatrimonioApi:*" ^
     "SalaApi:*" ^
     "NetworkModule:*" ^
     "OkHttp:*"
