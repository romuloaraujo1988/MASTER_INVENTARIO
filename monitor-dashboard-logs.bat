@echo off
echo Monitorando logs do Dashboard...
echo.
echo Abra o app e navegue para a Dashboard
echo Pressione Ctrl+C para parar
echo.
adb logcat -s DashboardViewModel:D DashboardFragment:D NetworkModule:D
