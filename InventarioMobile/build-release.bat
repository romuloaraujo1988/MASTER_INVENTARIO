@echo off
REM ========================================
REM SIHCP Mobile - Build Release Script
REM ========================================

echo.
echo ========================================
echo   SIHCP Mobile - Build Release
echo ========================================
echo.

REM Limpar builds anteriores
echo [1/5] Limpando builds anteriores...
call gradlew.bat clean
if %ERRORLEVEL% NEQ 0 (
    echo ERRO: Falha ao limpar builds anteriores
    pause
    exit /b 1
)

echo.
echo [2/5] Gerando APK de Release...
call gradlew.bat assembleRelease
if %ERRORLEVEL% NEQ 0 (
    echo ERRO: Falha ao gerar APK
    pause
    exit /b 1
)

echo.
echo [3/5] Gerando AAB (App Bundle)...
call gradlew.bat bundleRelease
if %ERRORLEVEL% NEQ 0 (
    echo ERRO: Falha ao gerar AAB
    pause
    exit /b 1
)

echo.
echo [4/5] Copiando arquivos para pasta dist...
if not exist "..\dist" mkdir "..\dist"

REM Obter data e hora para nome do arquivo
for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /value') do set datetime=%%I
set DATE_TIME=%datetime:~0,8%-%datetime:~8,6%

copy "app\build\outputs\apk\release\app-release.apk" "..\dist\SIHCP-Mobile-v1.2.0-%DATE_TIME%.apk" /Y
copy "app\build\outputs\bundle\release\app-release.aab" "..\dist\SIHCP-Mobile-v1.2.0-%DATE_TIME%.aab" /Y

echo.
echo [5/5] Exibindo informações dos arquivos...
echo.
dir "..\dist\SIHCP-Mobile-v1.2.0-%DATE_TIME%.*" /B

echo.
echo ========================================
echo   BUILD CONCLUIDO COM SUCESSO!
echo ========================================
echo.
echo Arquivos gerados em: ..\dist\
echo.
echo APK: SIHCP-Mobile-v1.2.0-%DATE_TIME%.apk
echo AAB: SIHCP-Mobile-v1.2.0-%DATE_TIME%.aab
echo.
echo ========================================
echo.

pause
