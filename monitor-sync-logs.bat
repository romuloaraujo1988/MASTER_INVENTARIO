@echo off
echo ========================================
echo Monitorando Logs de Sincronizacao
echo ========================================
echo.
echo Aguardando logs...
echo.

adb logcat -v time ^
  -s "SyncRepository:*" ^
     "SyncViewModel:*" ^
     "SyncActivity:*" ^
     "PatrimonioApi:*" ^
     "SalaApi:*" ^
     "NetworkModule:*" ^
     "OkHttp:*"
