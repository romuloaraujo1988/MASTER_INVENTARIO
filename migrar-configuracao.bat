@echo off
echo ========================================
echo Migracao de Configuracao do Banco
echo ========================================
echo.
echo Este script ajuda a migrar senhas hardcoded
echo para o arquivo de configuracao criptografado.
echo.
echo IMPORTANTE:
echo - A senha sera criptografada com AES-256
echo - Funciona apenas na maquina onde foi criada
echo - Arquivo salvo em: %%USERPROFILE%%\.inventario\
echo.
pause
echo.

echo Compilando projeto...
call mvn clean compile -q
if %ERRORLEVEL% NEQ 0 (
    echo ERRO: Falha na compilacao
    pause
    exit /b 1
)
echo OK - Compilado
echo.

echo Executando utilitario de migracao...
echo.
call mvn exec:java -Dexec.mainClass="com.inventario.util.ConfigurationMigration" -q
echo.

echo ========================================
echo.
echo Proximos passos:
echo 1. Edite ConfigurationMigration.java
echo 2. Descomente o bloco de migracao
echo 3. Ajuste os valores (host, porta, usuario, senha)
echo 4. Execute este script novamente
echo.
echo Ou use a interface grafica:
echo - Tela de Login ^> Botao "Configurar Banco"
echo.
echo ========================================
pause
