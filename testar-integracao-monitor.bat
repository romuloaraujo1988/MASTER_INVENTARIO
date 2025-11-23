@echo off
echo ========================================
echo   Teste de Integracao - Monitor Mobile
echo ========================================
echo.

echo [1/5] Verificando arquivos criados...
echo.

if not exist "src\main\java\com\inventario\dto\ConnectedDeviceDTO.java" (
    echo [ERRO] ConnectedDeviceDTO.java nao encontrado!
    pause
    exit /b 1
)
echo [OK] ConnectedDeviceDTO.java

if not exist "src\main\java\com\inventario\util\MobileApiClient.java" (
    echo [ERRO] MobileApiClient.java nao encontrado!
    pause
    exit /b 1
)
echo [OK] MobileApiClient.java

if not exist "src\main\java\com\inventario\view\MobileMonitorFrameV2.java" (
    echo [ERRO] MobileMonitorFrameV2.java nao encontrado!
    pause
    exit /b 1
)
echo [OK] MobileMonitorFrameV2.java
echo.

echo [2/5] Verificando integracao no MainFrame...
findstr /C:"abrirMonitorMobile" "src\main\java\com\inventario\view\MainFrame.java" > nul
if %errorlevel% neq 0 (
    echo [ERRO] Metodo abrirMonitorMobile nao encontrado no MainFrame!
    pause
    exit /b 1
)
echo [OK] Metodo abrirMonitorMobile encontrado no MainFrame
echo.

echo [3/5] Verificando servidor mobile...
curl -s http://localhost:8080/api/mobile/v1/connection/stats > nul 2>&1
if %errorlevel% neq 0 (
    echo [AVISO] Servidor mobile nao esta rodando
    echo.
    echo Para testar completamente, inicie o servidor:
    echo   java -jar target/sistema-inventario.jar --spring.profiles.active=mobile
    echo.
) else (
    echo [OK] Servidor mobile esta online!
    echo.
    echo Testando endpoint /active...
    curl -s http://localhost:8080/api/mobile/v1/connection/active
    echo.
)
echo.

echo [4/5] Compilando projeto...
call mvn clean compile -DskipTests -q
if %errorlevel% neq 0 (
    echo [ERRO] Falha na compilacao!
    pause
    exit /b 1
)
echo [OK] Compilacao bem-sucedida!
echo.

echo [5/5] Verificando documentacao...
if not exist "IMPLEMENTACAO_GERENCIAMENTO_DISPOSITIVOS_COMPLETA.md" (
    echo [AVISO] Documentacao tecnica nao encontrada
) else (
    echo [OK] Documentacao tecnica presente
)

if not exist "INTEGRACAO_MONITOR_DISPOSITIVOS_MAINFRAME.md" (
    echo [AVISO] Documentacao de integracao nao encontrada
) else (
    echo [OK] Documentacao de integracao presente
)
echo.

echo ========================================
echo   Integracao Concluida com Sucesso!
echo ========================================
echo.
echo Proximos passos:
echo.
echo 1. Iniciar servidor mobile (se ainda nao estiver rodando):
echo    java -jar target/sistema-inventario.jar --spring.profiles.active=mobile
echo.
echo 2. Iniciar sistema desktop:
echo    java -jar target/sistema-inventario.jar
echo.
echo 3. Acessar menu:
echo    Sistema ^> Monitor de Usuarios Mobile
echo.
echo 4. Verificar:
echo    - Janela abre sem erros
echo    - Status do servidor aparece
echo    - Tabela carrega
echo    - Botoes funcionam
echo.
pause
