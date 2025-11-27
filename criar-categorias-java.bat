@echo off
REM =====================================================
REM Executar classe Java para criar tabelas de categoria
REM =====================================================

echo ========================================
echo CRIANDO TABELAS DE CATEGORIA VIA JAVA
echo ========================================
echo.

REM Compilar se necessário
echo Compilando classe...
.\mvnw.cmd compile -q

if %ERRORLEVEL% NEQ 0 (
    echo ERRO ao compilar!
    pause
    exit /b 1
)

echo Compilacao OK!
echo.
echo Executando criacao de tabelas...
echo.

REM Executar classe diretamente
java -cp "target/classes;%USERPROFILE%\.m2\repository\org\postgresql\postgresql\42.7.1\postgresql-42.7.1.jar" com.inventario.util.CriarTabelasCategoriaUtil

echo.
pause
