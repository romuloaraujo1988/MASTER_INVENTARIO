@echo off
REM =====================================================
REM Script para criar tabelas de categoria no PostgreSQL
REM Sistema de Inventário - IFMT
REM Data: 27/11/2025
REM =====================================================

echo ========================================
echo CRIANDO TABELAS DE CATEGORIA
echo ========================================
echo.

REM Configurações do banco
set DB_HOST=localhost
set DB_PORT=5432
set DB_NAME=sispatrimonio
set DB_USER=inventario
set PGPASSWORD=inventario

echo Conectando ao banco de dados...
echo Host: %DB_HOST%
echo Database: %DB_NAME%
echo User: %DB_USER%
echo.

echo Executando script SQL...
echo.

REM Executar script SQL
"C:\Program Files\PostgreSQL\12\bin\psql.exe" -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -f "sql\criar_tabelas_categoria_patrimonio.sql"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo TABELAS CRIADAS COM SUCESSO!
    echo ========================================
    echo.
    
    echo Tabelas criadas:
    echo   - tabela_categoria_patrimonio
    echo   - tabela_subcategoria_patrimonio
    echo.
    
    echo Views criadas:
    echo   - view_categorias_com_contagem
    echo   - view_categorias_subcategorias
    echo.
    
    echo Dados inseridos:
    echo   - 9 categorias principais
    echo   - 40+ subcategorias
    echo.
    
    REM Consultar dados inseridos
    echo Consultando categorias criadas...
    echo.
    
    "C:\Program Files\PostgreSQL\12\bin\psql.exe" -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -c "SELECT nome, total_subcategorias FROM view_categorias_com_contagem ORDER BY ordem_exibicao;"
    
) else (
    echo.
    echo ========================================
    echo ERRO AO CRIAR TABELAS
    echo ========================================
    echo.
    echo Verifique os erros acima e tente novamente.
)

echo.
pause
