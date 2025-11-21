@echo off
echo ========================================
echo Limpando e Recompilando o Projeto
echo ========================================
echo.

echo [1/3] Limpando compilacao anterior...
call mvn clean
if %ERRORLEVEL% NEQ 0 (
    echo ERRO ao limpar projeto!
    pause
    exit /b 1
)

echo.
echo [2/3] Recompilando projeto...
call mvn compile -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ERRO ao compilar projeto!
    pause
    exit /b 1
)

echo.
echo [3/3] Verificando compilacao...
if exist "target\classes\com\inventario\view\MainFrame.class" (
    echo ✓ MainFrame.class compilado com sucesso!
) else (
    echo ✗ MainFrame.class NAO foi compilado!
    pause
    exit /b 1
)

if exist "target\classes\com\inventario\view\JLogin.class" (
    echo ✓ JLogin.class compilado com sucesso!
) else (
    echo ✗ JLogin.class NAO foi compilado!
    pause
    exit /b 1
)

echo.
echo ========================================
echo Compilacao concluida com sucesso!
echo ========================================
echo.
echo Agora voce pode executar o sistema.
echo.
pause
