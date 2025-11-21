@echo off
echo ========================================
echo   VERIFICACAO ANR - VERSAO CORRIGIDA
echo ========================================
echo.
echo App instalado com sucesso!
echo.
echo CORRECOES APLICADAS:
echo   [x] Removido runBlocking do AuthInterceptor
echo   [x] Timeouts reduzidos (10s/15s/30s)
echo   [x] Otimizacoes de performance
echo.
echo ========================================
echo   TESTE OS SEGUINTES FLUXOS:
echo ========================================
echo.
echo 1. Abra o app no emulador
echo 2. Faca login
echo 3. Observe o Dashboard carregando
echo 4. Navegue entre as telas
echo 5. Faca uma coleta
echo 6. Sincronize dados
echo.
echo ========================================
echo   MONITORAR LOGS (OPCIONAL)
echo ========================================
echo.
echo Deseja monitorar os logs em tempo real?
echo.
choice /C SN /M "Monitorar logs"

if errorlevel 2 goto :fim
if errorlevel 1 goto :logs

:logs
echo.
echo Iniciando monitoramento de logs...
echo Pressione Ctrl+C para parar
echo.
adb logcat -c
adb logcat | findstr /I "ANR DashboardFragment ApiModule HTTP inventario ERROR"
goto :fim

:fim
echo.
echo ========================================
echo   VERIFICACAO CONCLUIDA
echo ========================================
echo.
echo Se o app nao apresentar mais ANR:
echo   - Marque como RESOLVIDO
echo   - Documente no CHANGELOG
echo.
pause
