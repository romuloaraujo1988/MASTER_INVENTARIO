@echo off
echo ========================================
echo   MONITORAR ANR E LOGS DO APP
echo ========================================
echo.
echo Monitorando logs em tempo real...
echo Pressione Ctrl+C para parar
echo.
echo Filtros ativos:
echo   - ANR (Application Not Responding)
echo   - DashboardFragment
echo   - ApiModule
echo   - HTTP requests
echo   - Erros e excecoes
echo.
echo ========================================
echo.

adb logcat -c
adb logcat | findstr /I "ANR DashboardFragment ApiModule HTTP ERROR Exception inventario"
