@echo off
echo ========================================
echo Criando Tabelas de Itens Compostos
echo ========================================

cd /d "%~dp0"

echo Compilando e executando...
call mvnw.cmd compile exec:java -Dexec.mainClass="com.inventario.util.CriarTabelasItemComposto" -Dexec.cleanupDaemonThreads=false -q

echo.
echo Pressione qualquer tecla para sair...
pause > nul
