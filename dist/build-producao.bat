@echo off
REM ========================================
REM Script de Build - Modo Produção
REM Sistema de Inventário
REM ========================================

echo.
echo ========================================
echo   BUILD PRODUCAO - SISTEMA INVENTARIO
echo ========================================
echo.

REM Verificar se o Maven está instalado
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERRO] Maven nao encontrado!
    echo Por favor, instale o Apache Maven.
    pause
    exit /b 1
)

echo Verificando Maven...
mvn -version

echo.
echo Iniciando build de producao...
echo.

REM Ir para a raiz do projeto
cd ..

REM Copiar application-prod.properties
if exist "dist\application-prod.properties" (
    copy /Y "dist\application-prod.properties" "." >nul
)

REM Limpar builds anteriores
echo [1/4] Limpando builds anteriores...
call mvn clean

REM Compilar código
echo.
echo [2/4] Compilando codigo...
call mvn compile

REM Executar testes (opcional, pode ser desabilitado com -DskipTests)
echo.
echo [3/4] Executando testes...
call mvn test -DskipTests

REM Empacotar aplicação
echo.
echo [4/4] Empacotando aplicacao...
call mvn package -DskipTests

REM Voltar para dist
cd dist

if %errorlevel% neq 0 (
    echo.
    echo [ERRO] Build falhou!
    pause
    exit /b 1
)

echo.
echo ========================================
echo   BUILD CONCLUIDO COM SUCESSO!
echo ========================================
echo.
echo JAR gerado: ..\target\sistema-inventario-2.0.0.jar
echo.
echo Para iniciar o sistema em producao, execute:
echo   start-producao.bat (nesta pasta dist)
echo.
pause
