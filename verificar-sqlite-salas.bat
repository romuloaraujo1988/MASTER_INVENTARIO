@echo off
echo ========================================
echo Verificando Banco SQLite - Tabela SALA
echo ========================================
echo.

REM Verificar se o banco existe
if not exist "data\inventario.db" (
    echo ERRO: Banco SQLite nao encontrado em data\inventario.db
    echo.
    echo Verifique se:
    echo 1. O sistema foi executado em modo offline
    echo 2. Os dados foram importados (Menu: Sistema -^> Importar Dados Offline)
    echo.
    pause
    exit /b 1
)

echo Banco encontrado: data\inventario.db
echo.

REM Verificar estrutura da tabela SALA
echo ========================================
echo 1. ESTRUTURA DA TABELA SALA
echo ========================================
sqlite3 data\inventario.db ".schema SALA"
echo.

REM Verificar dados
echo ========================================
echo 2. DADOS DA TABELA SALA
echo ========================================
sqlite3 data\inventario.db "SELECT ID_SALA, NUMERO_SALA, NOME_SALA, ATIVA, DATA_CADASTRO FROM SALA LIMIT 10;"
echo.

REM Contar salas ativas
echo ========================================
echo 3. TOTAL DE SALAS ATIVAS
echo ========================================
sqlite3 data\inventario.db "SELECT COUNT(*) as total FROM SALA WHERE ATIVA = 1;"
echo.

REM Verificar tipos de dados
echo ========================================
echo 4. TIPOS DE DADOS (PRAGMA)
echo ========================================
sqlite3 data\inventario.db "PRAGMA table_info(SALA);"
echo.

echo ========================================
echo Verificacao concluida!
echo ========================================
pause
