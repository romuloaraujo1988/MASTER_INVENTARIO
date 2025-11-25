@echo off
echo ========================================
echo Build Desktop Thin-JAR (Swing)
echo ========================================
echo.

echo Limpando build anterior...
call mvnw.cmd clean

echo.
echo Compilando projeto...
call mvnw.cmd compile

echo.
echo Gerando JAR thin com dependencias...
call mvnw.cmd jar:jar dependency:copy-dependencies -Dmdep.outputDirectory=target/lib

echo.
echo Verificando JAR gerado...
if exist "target\sistema-inventario-2.0.0.jar" (
    echo [OK] JAR gerado: target\sistema-inventario-2.0.0.jar
    dir target\sistema-inventario-2.0.0.jar
) else (
    echo [ERRO] JAR nao foi gerado!
    exit /b 1
)

echo.
echo Verificando dependencias...
if exist "target\lib\" (
    echo [OK] Dependencias copiadas para: target\lib\
    dir target\lib\ | find /c ".jar"
) else (
    echo [ERRO] Dependencias nao foram copiadas!
    exit /b 1
)

echo.
echo ========================================
echo Build concluido com sucesso!
echo ========================================
echo.
echo Para executar:
echo   cd target
echo   java -jar sistema-inventario-2.0.0.jar
echo.

pause
