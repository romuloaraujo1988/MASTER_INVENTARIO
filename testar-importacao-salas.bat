@echo off
echo ========================================
echo   TESTE DE IMPORTACAO DE SALAS
echo ========================================
echo.

echo Compilando teste...
call mvn compile -q

if errorlevel 1 (
    echo.
    echo ERRO ao compilar!
    pause
    exit /b 1
)

echo.
echo Executando teste...
echo.

call mvn exec:java -Dexec.mainClass="com.inventario.test.TestSalaImport" -q

echo.
echo ========================================
echo   TESTE CONCLUIDO
echo ========================================
echo.
pause
