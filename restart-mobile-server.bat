@echo off
echo ========================================
echo Reiniciando Servidor Mobile
echo ========================================
echo.

echo [1/4] Parando servidor atual...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8081"') do (
    echo Matando processo %%a
    taskkill /F /PID %%a 2>nul
)
timeout /t 2 /nobreak >nul

echo.
echo [2/4] Limpando build anterior...
if exist target\classes rmdir /s /q target\classes
timeout /t 1 /nobreak >nul

echo.
echo [3/4] Compilando projeto...
call mvn clean compile -DskipTests
if errorlevel 1 (
    echo.
    echo ERRO: Falha na compilacao!
    pause
    exit /b 1
)

echo.
echo [4/4] Iniciando servidor mobile...
echo.
echo Servidor iniciando na porta 8081...
echo Pressione Ctrl+C para parar
echo.
start "Mobile API Server" mvn exec:java -Dexec.mainClass="com.inventario.MobileApiApplication"

timeout /t 5 /nobreak >nul
echo.
echo Servidor iniciado! Verifique a janela "Mobile API Server"
echo.
pause
