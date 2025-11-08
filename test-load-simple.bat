@echo off
echo ========================================
echo Teste de Carga Simples
echo ========================================
echo.

echo Verificando se servidor esta rodando...
netstat -ano | findstr ":8081" >nul
if errorlevel 1 (
    echo ERRO: Servidor nao esta rodando na porta 8081!
    pause
    exit /b 1
)
echo ✓ Servidor OK
echo.

echo ========================================
echo Executando 100 requisicoes simultaneas
echo ========================================
echo.

set "URL=http://localhost:8081/inventario/api/mobile/dashboard/stats"
set "TOTAL=100"
set "SUCCESS=0"
set "FAILED=0"

echo Iniciando teste...
echo.

for /L %%i in (1,1,%TOTAL%) do (
    curl -s -o nul -w "%%{http_code}" %URL% > temp_%%i.txt 2>&1
    set /p CODE=<temp_%%i.txt
    del temp_%%i.txt
    
    if "!CODE!"=="200" (
        set /a SUCCESS+=1
        echo [%%i/%TOTAL%] ✓ Sucesso
    ) else (
        set /a FAILED+=1
        echo [%%i/%TOTAL%] ✗ Falhou (codigo: !CODE!)
    )
)

echo.
echo ========================================
echo RESULTADOS
echo ========================================
echo Total de requisicoes: %TOTAL%
echo Sucesso: %SUCCESS%
echo Falhas: %FAILED%
echo Taxa de sucesso: %SUCCESS%%%
echo ========================================
echo.

pause
