@echo off
echo Iniciando Desktop e Servidor Mobile...
echo.

start "Servidor Mobile" cmd /c iniciar-servidor-mobile.bat
timeout /t 5 /nobreak > nul
start "Desktop" cmd /c iniciar-desktop.bat
