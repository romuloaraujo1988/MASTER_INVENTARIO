@echo off
echo ========================================
echo Teste de Correcao - Erro Timestamp Sala
echo ========================================
echo.

echo [1/3] Compilando projeto...
call mvnw.cmd clean compile -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ERRO: Falha na compilacao
    pause
    exit /b 1
)

echo.
echo [2/3] Executando aplicacao...
echo Aguarde a janela de login aparecer...
echo.
echo INSTRUCOES:
echo 1. Faca login com suas credenciais
echo 2. Abra: Menu Coleta ^> Coleta de Patrimonios v2
echo 3. Verifique se o combo de salas carrega sem erro
echo 4. Observe os logs no console
echo.
echo Pressione Ctrl+C para encerrar quando terminar o teste
echo.

call mvnw.cmd spring-boot:run

pause
