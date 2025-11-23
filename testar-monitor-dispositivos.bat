@echo off
echo ========================================
echo   Teste - Monitor de Dispositivos V2
echo ========================================
echo.

echo [1/4] Verificando se servidor mobile esta rodando...
curl -s http://localhost:8080/api/mobile/v1/connection/stats > nul 2>&1
if %errorlevel% neq 0 (
    echo [ERRO] Servidor mobile nao esta rodando!
    echo.
    echo Inicie o servidor com:
    echo   java -jar target/sistema-inventario.jar --spring.profiles.active=mobile
    echo.
    pause
    exit /b 1
)
echo [OK] Servidor mobile esta online!
echo.

echo [2/4] Testando endpoint /active...
curl -s http://localhost:8080/api/mobile/v1/connection/active
echo.
echo.

echo [3/4] Testando endpoint /stats...
curl -s http://localhost:8080/api/mobile/v1/connection/stats
echo.
echo.

echo [4/4] Compilando MobileMonitorFrameV2...
javac -cp "target/classes;lib/*" ^
    src/main/java/com/inventario/dto/ConnectedDeviceDTO.java ^
    src/main/java/com/inventario/util/MobileApiClient.java ^
    src/main/java/com/inventario/view/MobileMonitorFrameV2.java
    
if %errorlevel% neq 0 (
    echo [ERRO] Falha na compilacao!
    pause
    exit /b 1
)
echo [OK] Compilacao bem-sucedida!
echo.

echo ========================================
echo   Testes Concluidos com Sucesso!
echo ========================================
echo.
echo Para abrir o monitor, execute:
echo   java -cp "target/classes;lib/*" com.inventario.view.MobileMonitorFrameV2
echo.
pause
