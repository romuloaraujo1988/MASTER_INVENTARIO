@echo off
REM ============================================
REM Sistema de Inventario - Desktop
REM Versao: ${project.version}
REM ============================================

echo.
echo ========================================
echo  Sistema de Inventario - IFMT
echo  Versao: ${project.version}
echo ========================================
echo.

REM Verificar se Java esta instalado
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERRO: Java nao encontrado!
    echo Por favor, instale o Java 21 ou superior.
    echo.
    pause
    exit /b 1
)

REM Verificar versao do Java
for /f tokens^=2-5^ delims^=.-_^" %%j in ('java -fullversion 2^>^&1') do set "jver=%%j"
if %jver% LSS 21 (
    echo AVISO: Java %jver% detectado. Recomendado Java 21 ou superior.
    echo.
)

REM Configurar memoria JVM
set JAVA_OPTS=-Xms512m -Xmx2048m

REM Configurar encoding
set JAVA_OPTS=%JAVA_OPTS% -Dfile.encoding=UTF-8

REM Configurar Look and Feel nativo
set JAVA_OPTS=%JAVA_OPTS% -Dswing.defaultlaf=com.sun.java.swing.plaf.windows.WindowsLookAndFeel

REM Diretorio de logs
if not exist "logs" mkdir logs

echo Iniciando aplicacao...
echo.

REM Executar aplicacao
java %JAVA_OPTS% -jar sistema-inventario-${project.version}.jar

if %errorlevel% neq 0 (
    echo.
    echo ERRO: Falha ao iniciar a aplicacao.
    echo Verifique o arquivo de log em logs/sistema-inventario.log
    echo.
    pause
    exit /b 1
)
