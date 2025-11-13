@echo off
echo ========================================
echo Teste de Criptografia de Senhas
echo ========================================
echo.

echo 1. Compilando projeto...
call mvn clean compile -q
if %ERRORLEVEL% NEQ 0 (
    echo ERRO: Falha na compilacao
    pause
    exit /b 1
)
echo    OK - Compilado com sucesso
echo.

echo 2. Executando teste de criptografia basica...
call mvn exec:java -Dexec.mainClass="com.inventario.util.PasswordEncryption" -q
echo.

echo 3. Executando teste de integracao...
call mvn exec:java -Dexec.mainClass="com.inventario.config.DatabaseConfigManagerEncryptionTest" -q
echo.

echo ========================================
echo Testes concluidos!
echo ========================================
pause
