@echo off
REM ============================================
REM Build Desktop - JAR Não Monolítico
REM Sistema de Inventário - Aplicação Desktop
REM ============================================

echo.
echo ========================================
echo   BUILD DESKTOP - JAR NAO MONOLITICO
echo ========================================
echo.

REM Limpar builds anteriores
echo [1/4] Limpando builds anteriores...
call mvn clean
if %ERRORLEVEL% NEQ 0 (
    echo ERRO: Falha ao limpar projeto
    pause
    exit /b 1
)

REM Compilar e empacotar com profile thin-jar
echo.
echo [2/4] Compilando e empacotando aplicacao desktop...
call mvn package -P thin-jar -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ERRO: Falha ao compilar projeto
    pause
    exit /b 1
)

REM Criar estrutura de distribuição
echo.
echo [3/4] Criando estrutura de distribuicao...

REM Criar diretório de distribuição
if not exist "dist\desktop" mkdir dist\desktop
if not exist "dist\desktop\lib" mkdir dist\desktop\lib
if not exist "dist\desktop\config" mkdir dist\desktop\config

REM Copiar JAR principal
copy /Y "target\sistema-inventario-2.0.0.jar" "dist\desktop\" >nul
if %ERRORLEVEL% NEQ 0 (
    echo ERRO: Falha ao copiar JAR principal
    pause
    exit /b 1
)

REM Copiar dependências
xcopy /Y /Q "target\lib\*" "dist\desktop\lib\" >nul
if %ERRORLEVEL% NEQ 0 (
    echo ERRO: Falha ao copiar dependencias
    pause
    exit /b 1
)

REM Copiar arquivos de configuração
if exist "src\main\resources\application.properties" (
    copy /Y "src\main\resources\application.properties" "dist\desktop\config\" >nul
)

REM Criar script de execução
echo.
echo [4/4] Criando scripts de execucao...

REM Script Windows
(
echo @echo off
echo REM ============================================
echo REM Sistema de Inventario - Aplicacao Desktop
echo REM ============================================
echo.
echo echo Iniciando Sistema de Inventario Desktop...
echo echo.
echo.
echo REM Verificar Java
echo java -version ^>nul 2^>^&1
echo if %%ERRORLEVEL%% NEQ 0 ^(
echo     echo ERRO: Java nao encontrado!
echo     echo Instale Java 21 ou superior
echo     pause
echo     exit /b 1
echo ^)
echo.
echo REM Executar aplicacao
echo java -jar sistema-inventario-2.0.0.jar
echo.
echo if %%ERRORLEVEL%% NEQ 0 ^(
echo     echo.
echo     echo ERRO: Falha ao executar aplicacao
echo     pause
echo     exit /b 1
echo ^)
) > "dist\desktop\executar-desktop.bat"

REM Script Linux/Mac
(
echo #!/bin/bash
echo # ============================================
echo # Sistema de Inventario - Aplicacao Desktop
echo # ============================================
echo.
echo echo "Iniciando Sistema de Inventario Desktop..."
echo echo
echo.
echo # Verificar Java
echo if ! command -v java ^&^> /dev/null; then
echo     echo "ERRO: Java nao encontrado!"
echo     echo "Instale Java 21 ou superior"
echo     exit 1
echo fi
echo.
echo # Executar aplicacao
echo java -jar sistema-inventario-2.0.0.jar
echo.
echo if [ $? -ne 0 ]; then
echo     echo
echo     echo "ERRO: Falha ao executar aplicacao"
echo     exit 1
echo fi
) > "dist\desktop\executar-desktop.sh"

REM Criar README
(
echo ============================================
echo SISTEMA DE INVENTARIO - APLICACAO DESKTOP
echo ============================================
echo.
echo VERSAO: 2.0.0
echo DATA: %date%
echo.
echo ESTRUTURA:
echo   sistema-inventario-2.0.0.jar  - Aplicacao principal ^(~2MB^)
echo   lib/                          - Dependencias externas ^(~150MB^)
echo   config/                       - Arquivos de configuracao
echo   executar-desktop.bat          - Script Windows
echo   executar-desktop.sh           - Script Linux/Mac
echo.
echo REQUISITOS:
echo   - Java 21 ou superior
echo   - PostgreSQL 12+ ^(configurado e rodando^)
echo   - 4GB RAM minimo
echo   - 500MB espaco em disco
echo.
echo EXECUCAO:
echo   Windows: Execute executar-desktop.bat
echo   Linux/Mac: Execute ./executar-desktop.sh
echo   Manual: java -jar sistema-inventario-2.0.0.jar
echo.
echo CONFIGURACAO:
echo   1. Configure o banco de dados em:
echo      Windows: %%USERPROFILE%%\.inventario\configuracao_banco.json
echo      Linux/Mac: ~/.inventario/configuracao_banco.json
echo.
echo   2. Exemplo de configuracao:
echo      {
echo        "host": "localhost",
echo        "porta": "5432",
echo        "database": "sispatrimonio",
echo        "usuario": "inventario",
echo        "senha": "sua_senha"
echo      }
echo.
echo PRIMEIRO USO:
echo   1. Instale PostgreSQL
echo   2. Crie o banco de dados: sispatrimonio
echo   3. Execute os scripts SQL em sql/
echo   4. Configure o arquivo configuracao_banco.json
echo   5. Execute a aplicacao
echo.
echo SUPORTE:
echo   Email: suporte@inventario.com
echo   Documentacao: docs/
echo.
) > "dist\desktop\README.txt"

REM Calcular tamanhos
echo.
echo ========================================
echo   BUILD CONCLUIDO COM SUCESSO!
echo ========================================
echo.
echo Estrutura criada em: dist\desktop\
echo.
echo Arquivos gerados:
dir /B "dist\desktop\*.jar" 2>nul
echo   + %CD%\dist\desktop\lib\ (dependencias)
echo   + executar-desktop.bat
echo   + executar-desktop.sh
echo   + README.txt
echo.

REM Mostrar tamanhos
for %%F in ("dist\desktop\sistema-inventario-2.0.0.jar") do (
    set /A size=%%~zF/1024
    echo Tamanho JAR principal: !size! KB
)

echo.
echo Para executar:
echo   cd dist\desktop
echo   executar-desktop.bat
echo.
pause
