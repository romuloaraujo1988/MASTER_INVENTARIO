@echo off
echo ============================================================
echo   Copiar Banco SQLite para Local Correto
echo ============================================================
echo.

set SOURCE=data\inventario.db
set DEST=%USERPROFILE%\.inventario\data\inventario.db

echo [1/3] Verificando banco de origem...
if not exist %SOURCE% (
    echo ERRO: Banco de origem nao encontrado: %SOURCE%
    pause
    exit /b 1
)
echo    OK Banco encontrado: %SOURCE%
echo.

echo [2/3] Criando diretorio de destino...
if not exist "%USERPROFILE%\.inventario\data" (
    mkdir "%USERPROFILE%\.inventario\data"
    echo    OK Diretorio criado
) else (
    echo    OK Diretorio ja existe
)
echo.

echo [3/3] Copiando banco de dados...
copy /Y %SOURCE% "%DEST%"
if errorlevel 1 (
    echo ERRO: Falha ao copiar banco
    pause
    exit /b 1
)
echo    OK Banco copiado com sucesso
echo.

echo ============================================================
echo   CONCLUIDO!
echo ============================================================
echo.
echo Banco SQLite copiado para:
echo %DEST%
echo.
echo Tamanho do arquivo:
dir "%DEST%" | find "inventario.db"
echo.
pause
