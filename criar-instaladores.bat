@echo off
REM Script para criar todos os instaladores do SIHCP
REM Windows

echo ========================================
echo SIHCP - Criacao de Instaladores
echo ========================================
echo.

REM Verificar se Maven esta instalado
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERRO: Maven nao encontrado!
    echo Instale o Maven: https://maven.apache.org/
    pause
    exit /b 1
)

REM Verificar se Inno Setup esta instalado
set INNO_SETUP="C:\Program Files (x86)\Inno Setup 6\ISCC.exe"
if not exist %INNO_SETUP% (
    echo AVISO: Inno Setup nao encontrado em %INNO_SETUP%
    echo Instalador Windows nao sera criado.
    echo Download: https://jrsoftware.org/isdl.php
    set SKIP_WINDOWS=1
)

echo.
echo 1. Limpando builds anteriores...
call mvn clean
if %ERRORLEVEL% NEQ 0 (
    echo ERRO: Falha ao limpar projeto
    pause
    exit /b 1
)
echo    OK

echo.
echo 2. Compilando projeto...
call mvn package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ERRO: Falha ao compilar projeto
    pause
    exit /b 1
)
echo    OK

echo.
echo 3. Criando estrutura de diretorios...
mkdir dist 2>nul
mkdir instalador\windows\output 2>nul
echo    OK

echo.
echo 4. Copiando arquivos para instalador Windows...
copy target\sistema-inventario.jar instalador\windows\ >nul
if exist lib (
    xcopy /E /I /Y lib instalador\windows\lib >nul
)
echo    OK

if not defined SKIP_WINDOWS (
    echo.
    echo 5. Criando instalador Windows...
    %INNO_SETUP% instalador\windows\sihcp-setup.iss
    if %ERRORLEVEL% NEQ 0 (
        echo ERRO: Falha ao criar instalador Windows
        pause
        exit /b 1
    )
    echo    OK - Instalador criado em dist\
) else (
    echo.
    echo 5. Pulando instalador Windows (Inno Setup nao encontrado)
)

echo.
echo 6. Criando versao portatil...
mkdir SIHCP-Portable 2>nul
copy target\sistema-inventario.jar SIHCP-Portable\ >nul
if exist lib (
    xcopy /E /I /Y lib SIHCP-Portable\lib >nul
)
mkdir SIHCP-Portable\config 2>nul
mkdir SIHCP-Portable\docs 2>nul
if exist DOCUMENTAÇÃO (
    xcopy /E /I /Y DOCUMENTAÇÃO SIHCP-Portable\docs >nul
)

REM Criar launcher Windows
echo @echo off > SIHCP-Portable\SIHCP.bat
echo java -jar sistema-inventario.jar >> SIHCP-Portable\SIHCP.bat

REM Criar launcher Linux
echo #!/bin/bash > SIHCP-Portable\sihcp.sh
echo java -jar sistema-inventario.jar >> SIHCP-Portable\sihcp.sh

REM Criar README
(
echo SIHCP - Sistema de Inventario ^(Versao Portatil^)
echo.
echo Para executar:
echo - Windows: Clique duas vezes em SIHCP.bat
echo - Linux/Mac: Execute ./sihcp.sh
echo.
echo Configuracao:
echo - Configure o banco via interface
echo - Configuracoes salvas em: ./config/
echo.
echo Requisitos:
echo - Java 21 ou superior
) > SIHCP-Portable\README.txt

REM Compactar
powershell -Command "Compress-Archive -Path SIHCP-Portable\* -DestinationPath dist\SIHCP-Inventario-Portable.zip -Force"
rmdir /S /Q SIHCP-Portable
echo    OK - Versao portatil criada em dist\

echo.
echo 7. Criando pacote Linux...
mkdir SIHCP-Linux-Installer 2>nul
copy target\sistema-inventario.jar SIHCP-Linux-Installer\ >nul
if exist lib (
    xcopy /E /I /Y lib SIHCP-Linux-Installer\lib >nul
)
copy instalador\linux\install.sh SIHCP-Linux-Installer\ >nul
copy instalador\linux\uninstall.sh SIHCP-Linux-Installer\ >nul
if exist DOCUMENTAÇÃO (
    xcopy /E /I /Y DOCUMENTAÇÃO SIHCP-Linux-Installer\docs >nul
)

REM Compactar
powershell -Command "Compress-Archive -Path SIHCP-Linux-Installer\* -DestinationPath dist\SIHCP-Linux-Installer.tar.gz -Force"
rmdir /S /Q SIHCP-Linux-Installer
echo    OK - Pacote Linux criado em dist\

echo.
echo ========================================
echo Instaladores criados com sucesso!
echo ========================================
echo.
echo Arquivos em dist\:
dir /B dist\
echo.
echo Tamanhos:
for %%F in (dist\*) do echo   %%~nxF: %%~zF bytes
echo.
pause
