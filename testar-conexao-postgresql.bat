@echo off
REM Script para testar conexão com PostgreSQL
REM Sistema de Inventário IFMT

echo ========================================
echo TESTE DE CONEXAO POSTGRESQL
echo ========================================
echo.

REM Verificar se PostgreSQL está rodando
echo Verificando se PostgreSQL esta rodando...
netstat -an | findstr ":5432" >nul
if %errorlevel% equ 0 (
    echo [OK] PostgreSQL esta rodando na porta 5432
) else (
    echo [ERRO] PostgreSQL NAO esta rodando na porta 5432
    echo.
    echo Execute: net start postgresql-x64-12
    pause
    exit /b 1
)

echo.
echo Testando conexao com o banco...
echo.

REM Tentar conectar ao banco
psql -h localhost -U inventario -d sispatrimonio -c "SELECT COUNT(*) as total_patrimonios FROM TABELA_PATRIMONIO;" 2>nul
if %errorlevel% equ 0 (
    echo.
    echo [OK] Conexao com PostgreSQL funcionando!
    echo.
    
    echo Contando registros no PostgreSQL...
    echo.
    
    echo Patrimonios:
    psql -h localhost -U inventario -d sispatrimonio -c "SELECT COUNT(*) FROM TABELA_PATRIMONIO;" -t
    
    echo.
    echo Salas:
    psql -h localhost -U inventario -d sispatrimonio -c "SELECT COUNT(*) FROM TABELA_SALA;" -t
    
    echo.
    echo Responsaveis:
    psql -h localhost -U inventario -d sispatrimonio -c "SELECT COUNT(*) FROM TABELA_RESPONSAVEL;" -t
    
    echo.
    echo Usuarios:
    psql -h localhost -U inventario -d sispatrimonio -c "SELECT COUNT(*) FROM TABELA_USUARIO;" -t
    
    echo.
    echo Inventarios:
    psql -h localhost -U inventario -d sispatrimonio -c "SELECT COUNT(*) FROM TABELA_INVENTARIO;" -t
    
) else (
    echo [ERRO] Nao foi possivel conectar ao PostgreSQL
    echo.
    echo Verifique:
    echo 1. PostgreSQL esta rodando?
    echo 2. Usuario 'inventario' existe?
    echo 3. Banco 'sispatrimonio' existe?
    echo 4. Senha esta correta?
)

echo.
echo ========================================
echo TESTE CONCLUIDO
echo ========================================
pause
