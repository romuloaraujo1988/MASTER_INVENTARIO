@echo off
echo ========================================
echo TESTE DIRETO DE IMPORTACAO
echo ========================================
echo.

REM Compilar
echo Compilando projeto...
call mvn clean compile -DskipTests
if errorlevel 1 (
    echo ERRO na compilacao!
    pause
    exit /b 1
)

echo.
echo ========================================
echo EXECUTANDO TESTE
echo ========================================
echo.

REM Executar classe de teste
java -cp "target/classes;lib/*" com.inventario.service.DataImportServiceTest

pause
