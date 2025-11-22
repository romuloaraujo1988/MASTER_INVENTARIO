@echo off
REM Script para testar importação de dados offline
REM Sistema de Inventário IFMT

echo ========================================
echo TESTE DE IMPORTACAO OFFLINE
echo ========================================
echo.

REM Verificar se o banco SQLite existe
if exist "data\inventario_offline.db" (
    echo [OK] Banco SQLite encontrado
    echo.
    
    REM Verificar tabelas criadas
    echo Verificando tabelas...
    sqlite3 data\inventario_offline.db ".tables"
    echo.
    
    REM Contar registros em cada tabela
    echo Contando registros importados...
    echo.
    
    echo Patrimonios:
    sqlite3 data\inventario_offline.db "SELECT COUNT(*) FROM local_patrimonio;"
    
    echo Salas:
    sqlite3 data\inventario_offline.db "SELECT COUNT(*) FROM local_sala;"
    
    echo Responsaveis:
    sqlite3 data\inventario_offline.db "SELECT COUNT(*) FROM local_responsavel;"
    
    echo Usuarios:
    sqlite3 data\inventario_offline.db "SELECT COUNT(*) FROM local_usuario;"
    
    echo Inventarios:
    sqlite3 data\inventario_offline.db "SELECT COUNT(*) FROM local_inventario;"
    
    echo.
    echo Ultimos 5 patrimonios importados:
    sqlite3 data\inventario_offline.db "SELECT id, numero, descricao FROM local_patrimonio LIMIT 5;"
    
    echo.
    echo Ultimas 5 salas importadas:
    sqlite3 data\inventario_offline.db "SELECT id, nome, bloco FROM local_sala LIMIT 5;"
    
    echo.
    echo Ultimos 5 responsaveis importados:
    sqlite3 data\inventario_offline.db "SELECT id, nome, cargo FROM local_responsavel LIMIT 5;"
    
) else (
    echo [ERRO] Banco SQLite nao encontrado!
    echo Execute a importacao primeiro no sistema.
)

echo.
echo ========================================
echo TESTE CONCLUIDO
echo ========================================
pause
