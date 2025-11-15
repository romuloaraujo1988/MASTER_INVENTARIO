@echo off
REM ========================================
REM Instalar Sistema como Serviço Windows
REM Requer privilégios de Administrador
REM ========================================

echo.
echo ========================================
echo   INSTALAR COMO SERVICO WINDOWS
echo ========================================
echo.

REM Verificar se está rodando como Administrador
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERRO] Este script requer privilegios de Administrador!
    echo.
    echo Clique com botao direito e selecione "Executar como Administrador"
    pause
    exit /b 1
)

echo [INFO] Privilegios de Administrador confirmados.
echo.

REM Definir variáveis
set SERVICE_NAME=SistemaInventario
set DISPLAY_NAME=Sistema de Inventario
set DESCRIPTION=Sistema de Controle de Coleta de Inventario de Patrimonio
set JAR_PATH=%CD%\target\sistema-inventario-2.0.0.jar
set JAVA_HOME_PATH=%JAVA_HOME%

REM Verificar se o JAR existe
if not exist "%JAR_PATH%" (
    echo [ERRO] JAR nao encontrado em: %JAR_PATH%
    echo.
    echo Execute primeiro: build-producao.bat
    pause
    exit /b 1
)

REM Verificar se JAVA_HOME está definido
if "%JAVA_HOME_PATH%"=="" (
    echo [AVISO] JAVA_HOME nao esta definido.
    echo Tentando encontrar Java automaticamente...
    
    for /f "tokens=*" %%i in ('where java 2^>nul') do set JAVA_EXE=%%i
    
    if "!JAVA_EXE!"=="" (
        echo [ERRO] Java nao encontrado!
        pause
        exit /b 1
    )
    
    echo Java encontrado em: !JAVA_EXE!
) else (
    set JAVA_EXE=%JAVA_HOME_PATH%\bin\java.exe
)

echo.
echo Configuracoes do Servico:
echo - Nome: %SERVICE_NAME%
echo - Display: %DISPLAY_NAME%
echo - JAR: %JAR_PATH%
echo - Java: %JAVA_EXE%
echo.

REM Verificar se o serviço já existe
sc query "%SERVICE_NAME%" >nul 2>&1
if %errorlevel% equ 0 (
    echo [AVISO] Servico ja existe. Removendo...
    sc stop "%SERVICE_NAME%"
    timeout /t 3 /nobreak >nul
    sc delete "%SERVICE_NAME%"
    timeout /t 2 /nobreak >nul
)

echo.
echo Criando servico...
echo.

REM Criar serviço usando sc create
sc create "%SERVICE_NAME%" ^
    binPath= "\"%JAVA_EXE%\" -Xms512m -Xmx2048m -Dspring.profiles.active=prod -jar \"%JAR_PATH%\"" ^
    DisplayName= "%DISPLAY_NAME%" ^
    start= auto

if %errorlevel% neq 0 (
    echo [ERRO] Falha ao criar servico!
    pause
    exit /b 1
)

REM Configurar descrição
sc description "%SERVICE_NAME%" "%DESCRIPTION%"

REM Configurar recuperação em caso de falha
sc failure "%SERVICE_NAME%" reset= 86400 actions= restart/60000/restart/60000/restart/60000

echo.
echo ========================================
echo   SERVICO INSTALADO COM SUCESSO!
echo ========================================
echo.
echo Para gerenciar o servico:
echo.
echo   Iniciar:  sc start %SERVICE_NAME%
echo   Parar:    sc stop %SERVICE_NAME%
echo   Status:   sc query %SERVICE_NAME%
echo   Remover:  sc delete %SERVICE_NAME%
echo.
echo Ou use o Gerenciador de Servicos do Windows:
echo   services.msc
echo.

choice /C SN /M "Deseja iniciar o servico agora"
if %errorlevel% equ 1 (
    echo.
    echo Iniciando servico...
    sc start "%SERVICE_NAME%"
    
    if %errorlevel% equ 0 (
        echo.
        echo Servico iniciado com sucesso!
    ) else (
        echo.
        echo [ERRO] Falha ao iniciar servico.
        echo Verifique os logs em: logs\sistema-inventario-prod.log
    )
)

echo.
pause
