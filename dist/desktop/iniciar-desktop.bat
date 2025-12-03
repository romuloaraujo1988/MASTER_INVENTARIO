@echo off
setlocal

echo ╔════════════════════════════════════════════════════════════════╗
echo ║  SIHCP - Sistema de Historico e Coleta Patrimonial             ║
echo ║  Aplicacao Desktop                                             ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

:: Configuracoes de memoria para desktop
set JAVA_OPTS=-Xms512m -Xmx2g
set JAVA_OPTS=%JAVA_OPTS% -XX:MaxMetaspaceSize=256m
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseG1GC
set JAVA_OPTS=%JAVA_OPTS% -XX:MaxGCPauseMillis=200
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseStringDeduplication

echo Iniciando aplicacao desktop...
echo.

java %JAVA_OPTS% -jar sistema-inventario.jar

pause
