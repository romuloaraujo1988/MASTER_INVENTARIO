@echo off
chcp 65001 > nul
echo ========================================
echo   Teste de Conexão PostgreSQL
echo ========================================
echo.

REM Configurações (ajuste conforme necessário)
set PGHOST=localhost
set PGPORT=5432
set PGDATABASE=sispatrimonio
set PGUSER=postgres

echo Configuração:
echo   Host: %PGHOST%
echo   Porta: %PGPORT%
echo   Banco: %PGDATABASE%
echo   Usuário: %PGUSER%
echo.

REM Verificar se psql está instalado
where psql >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ ERRO: psql não encontrado no PATH
    echo.
    echo Adicione o PostgreSQL ao PATH ou execute este script
    echo da pasta bin do PostgreSQL.
    echo.
    echo Exemplo: C:\Program Files\PostgreSQL\15\bin\
    echo.
    pause
    exit /b 1
)

echo ✓ psql encontrado
echo.

REM Testar conexão
echo Testando conexão...
echo.

psql -h %PGHOST% -p %PGPORT% -U %PGUSER% -d %PGDATABASE% -c "SELECT version();"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo   ✓ CONEXÃO BEM-SUCEDIDA!
    echo ========================================
    echo.
    echo O PostgreSQL está acessível e funcionando.
    echo Você pode usar estas configurações no sistema.
) else (
    echo.
    echo ========================================
    echo   ❌ FALHA NA CONEXÃO
    echo ========================================
    echo.
    echo Possíveis causas:
    echo   • PostgreSQL não está rodando
    echo   • Usuário ou senha incorretos
    echo   • Banco de dados não existe
    echo   • Firewall bloqueando a porta
    echo   • Configuração pg_hba.conf incorreta
    echo.
    echo Consulte: GUIA_CONFIGURACAO_POSTGRESQL_REMOTO.md
)

echo.
pause
