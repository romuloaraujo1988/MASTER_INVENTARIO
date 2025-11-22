@echo off
echo ========================================
echo TESTE DE IMPORTACAO SQLITE
echo ========================================
echo.

REM Limpar banco SQLite antigo
if exist "data\inventario.db" (
    echo Removendo banco SQLite antigo...
    del /F /Q "data\inventario.db"
    echo ✓ Banco removido
) else (
    echo Banco SQLite nao existe ainda
)

echo.
echo Criando diretorio data...
if not exist "data" mkdir data
echo ✓ Diretorio criado

echo.
echo ========================================
echo INSTRUCOES:
echo ========================================
echo 1. Execute a aplicacao desktop
echo 2. Va em Menu > Modo Offline > Importar Dados
echo 3. Clique em "Iniciar Importacao"
echo 4. Observe os logs no console
echo.
echo LOGS ESPERADOS:
echo ========================================
echo === INICIALIZANDO BANCO SQLITE ===
echo >>> Criando tabelas do sistema...
echo >>> Criando tabelas espelho...
echo >>> Criando local_patrimonio...
echo >>> Criando local_sala...
echo >>> Criando local_responsavel...
echo >>> Criando local_usuario...
echo === BANCO SQLITE INICIALIZADO ===
echo.
echo === INICIANDO IMPORTACAO DE DADOS ===
echo >>> Limpando tabela local_patrimonio...
echo >>> Estrutura da tabela local_patrimonio:
echo >>>   - id (INTEGER)
echo >>>   - numero (TEXT)
echo >>>   - descricao (TEXT)
echo >>> DEBUG: Primeiro patrimonio a ser salvo:
echo >>>   ID: XXX
echo >>>   Numero: XXXXX
echo >>> Progresso: 100/1000 patrimonios
echo === IMPORTACAO CONCLUIDA COM SUCESSO ===
echo.
echo ========================================
echo VERIFICACAO APOS IMPORTACAO:
echo ========================================
echo.
echo Execute este comando para verificar:
echo sqlite3 data\inventario.db "SELECT COUNT(*) FROM local_patrimonio"
echo.
pause
