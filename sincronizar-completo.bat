@echo off
echo ============================================================
echo   SINCRONIZACAO COMPLETA PostgreSQL -^> SQLite
echo ============================================================
echo.
echo Este script ira:
echo   1. Sincronizar dados do PostgreSQL para SQLite
echo   2. Copiar banco para local do sistema
echo   3. Verificar integridade dos dados
echo.
pause

echo.
echo ============================================================
echo   ETAPA 1/3: Sincronizando do PostgreSQL
echo ============================================================
echo.

call sincronizar-postgresql-sqlite.bat
if errorlevel 1 (
    echo.
    echo ERRO: Falha na sincronizacao do PostgreSQL
    pause
    exit /b 1
)

echo.
echo ============================================================
echo   ETAPA 2/3: Copiando para Local do Sistema
echo ============================================================
echo.

set SOURCE=data\inventario.db
set DEST=%USERPROFILE%\.inventario\data\inventario.db

echo Verificando banco de origem...
if not exist %SOURCE% (
    echo ERRO: Banco de origem nao encontrado: %SOURCE%
    pause
    exit /b 1
)
echo    OK Banco encontrado

echo.
echo Criando diretorio de destino...
if not exist "%USERPROFILE%\.inventario\data" (
    mkdir "%USERPROFILE%\.inventario\data"
    echo    OK Diretorio criado
) else (
    echo    OK Diretorio ja existe
)

echo.
echo Copiando banco de dados...
copy /Y %SOURCE% "%DEST%" >nul
if errorlevel 1 (
    echo ERRO: Falha ao copiar banco
    pause
    exit /b 1
)
echo    OK Banco copiado com sucesso

echo.
echo ============================================================
echo   ETAPA 3/3: Verificando Integridade
echo ============================================================
echo.

echo Verificando dados no banco de origem...
sqlite3 %SOURCE% "SELECT 'Patrimonios: ' || COUNT(*) FROM local_patrimonio UNION ALL SELECT 'Salas: ' || COUNT(*) FROM local_sala UNION ALL SELECT 'Responsaveis: ' || COUNT(*) FROM local_responsavel;"

echo.
echo Verificando dados no banco de destino...
sqlite3 "%DEST%" "SELECT 'Patrimonios: ' || COUNT(*) FROM local_patrimonio UNION ALL SELECT 'Salas: ' || COUNT(*) FROM local_sala UNION ALL SELECT 'Responsaveis: ' || COUNT(*) FROM local_responsavel;"

echo.
echo ============================================================
echo   SINCRONIZACAO COMPLETA CONCLUIDA!
echo ============================================================
echo.
echo Banco SQLite disponivel em:
echo   1. %SOURCE%
echo   2. %DEST%
echo.
echo O sistema desktop agora pode trabalhar em modo offline.
echo.
pause
