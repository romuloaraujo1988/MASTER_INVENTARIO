@echo off
echo ========================================
echo Sistema de Inventario - Desktop (Thin JAR)
echo ========================================
echo.
echo Iniciando aplicacao desktop...
echo.

REM Verificar se o JAR existe
if not exist "target\mobile-server\sistema-inventario-2.0.0.jar" (
    echo ERRO: JAR nao encontrado!
    echo Execute primeiro: mvnw.cmd clean package -P thin-jar -DskipTests
    pause
    exit /b 1
)

REM Verificar se a pasta lib existe
if not exist "target\lib" (
    echo ERRO: Pasta lib nao encontrada!
    echo Execute primeiro: mvnw.cmd clean package -P thin-jar -DskipTests
    pause
    exit /b 1
)

REM Configurar JVM para desktop
set JAVA_OPTS=-Xms256m -Xmx1g -XX:+UseG1GC

REM Executar aplicacao desktop
cd target\mobile-server
java %JAVA_OPTS% -jar sistema-inventario-2.0.0.jar

cd ..\..
pause
