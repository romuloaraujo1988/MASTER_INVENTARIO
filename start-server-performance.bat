@echo off
echo ========================================
echo Sistema de Inventario - Modo Performance
echo ========================================
echo.
echo Iniciando servidor com otimizacoes de performance...
echo.
echo Configuracoes:
echo - Thread Pool: 20-200 threads
echo - Conexoes DB: 10-30 simultaneas
echo - Cache: Caffeine (alta performance)
echo - Compressao: Ativada
echo.

REM Configurar JVM para performance
set JAVA_OPTS=-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200

REM Iniciar servidor com profile performance
java %JAVA_OPTS% -jar target\sistema-inventario-2.0.0.jar --spring.profiles.active=mobile,performance

pause
