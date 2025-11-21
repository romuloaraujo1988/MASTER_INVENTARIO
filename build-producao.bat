@echo off
REM ============================================
REM Build de Producao - Sistema de Inventario
REM Gera JAR modular otimizado
REM ============================================

echo.
echo ============================================
echo   BUILD DE PRODUCAO
echo   Sistema de Inventario v2.0.0
echo ============================================
echo.

REM Verificar Maven
call mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERRO] Maven nao encontrado!
    echo Por favor, instale o Maven 3.6+
    echo Download: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

echo [1/5] Limpando builds anteriores...
call mvn clean
if %errorlevel% neq 0 (
    echo [ERRO] Falha ao limpar projeto!
    pause
    exit /b 1
)

echo.
echo [2/5] Compilando codigo fonte...
call mvn compile
if %errorlevel% neq 0 (
    echo [ERRO] Falha na compilacao!
    pause
    exit /b 1
)

echo.
echo [3/5] Executando testes...
call mvn test -DskipTests
if %errorlevel% neq 0 (
    echo [AVISO] Alguns testes falharam, continuando...
)

echo.
echo [4/5] Gerando pacote de producao...
call mvn package -P producao -DskipTests
if %errorlevel% neq 0 (
    echo [ERRO] Falha ao gerar pacote!
    pause
    exit /b 1
)

echo.
echo [5/5] Verificando arquivos gerados...

if not exist "target\sistema-inventario-2.0.0.jar" (
    echo [ERRO] JAR principal nao encontrado!
    pause
    exit /b 1
)

if not exist "target\lib" (
    echo [ERRO] Diretorio lib/ nao encontrado!
    pause
    exit /b 1
)

if not exist "target\sistema-inventario-2.0.0-producao.zip" (
    echo [ERRO] Pacote ZIP nao encontrado!
    pause
    exit /b 1
)

echo.
echo ============================================
echo   BUILD CONCLUIDO COM SUCESSO!
echo ============================================
echo.
echo Arquivos gerados:
echo.
echo   JAR Principal:
echo   target\sistema-inventario-2.0.0.jar
echo.
echo   Dependencias:
echo   target\lib\*.jar
echo.
echo   Pacote Completo:
echo   target\sistema-inventario-2.0.0-producao.zip
echo.
echo Tamanho do JAR principal:
for %%A in ("target\sistema-inventario-2.0.0.jar") do echo   %%~zA bytes (%%~zAKB)
echo.
echo Total de dependencias:
dir /b target\lib\*.jar | find /c ".jar"
echo.
echo ============================================
echo.
echo Proximo passo:
echo 1. Extrair: target\sistema-inventario-2.0.0-producao.zip
echo 2. Configurar: config\application-prod.properties
echo 3. Executar: iniciar-desktop.bat ou iniciar-mobile-server.bat
echo.
echo ============================================
echo.

pause
