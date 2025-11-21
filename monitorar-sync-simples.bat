@echo off
cls
echo ========================================
echo Monitoramento de Sincronizacao
echo ========================================
echo.
echo INSTRUCOES:
echo 1. Abra o app no celular/emulador
echo 2. Va em Menu -^> Dados -^> Sincronizacao
echo 3. Clique em "Sincronizar do Servidor"
echo.
echo Aguardando logs...
echo ========================================
echo.

adb logcat -c
adb logcat -v time -s "SyncRepository:*" "SyncViewModel:*" "SyncActivity:*" "NetworkModule:*" "OkHttp:*" "PatrimonioApi:*" "SalaApi:*"
