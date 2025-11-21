@echo off
REM ========================================
REM Script para compilar aplicação Desktop
REM Sistema de Inventário - Versão 2.0.0
REM Thin JAR (JAR pequeno + lib/)
REM ========================================

echo.
echo ========================================
echo   Build Desktop - Thin JAR
echo   Sistema de Inventario - v2.0.0
echo ========================================
echo.

REM Verificar se Maven wrapper existe
if not exist "mvnw.cmd" (
    echo [ERRO] mvnw.cmd nao encontrado!
    pause
    exit /b 1
)

echo [INFO] Limpando build anterior...
call mvnw.cmd clean

echo.
echo [INFO] Compilando aplicacao (thin-jar)...
echo [INFO] Profile: thin-jar
echo [INFO] Testes: DESABILITADOS
echo.

call mvnw.cmd package -P thin-jar -DskipTests

if %errorlevel% neq 0 (
    echo.
    echo [ERRO] Falha na compilacao!
    pause
    exit /b 1
)

echo.
echo ========================================
echo   BUILD CONCLUIDO COM SUCESSO!
echo ========================================
echo.
echo [INFO] JAR criado em:
echo       target\mobile-server\sistema-inventario-2.0.0.jar
echo.
echo [INFO] Dependencias em:
echo       target\lib\ (174 arquivos)
echo.
echo [INFO] Tamanho do JAR:
dir target\mobile-server\sistema-inventario-2.0.0.jar | find "sistema-inventario"
echo.
echo [INFO] Para executar:
echo       run-desktop.bat
echo.
pause
