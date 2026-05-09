@echo off
REM Script para verificar e fechar processos usando a porta 8081

echo ========================================
echo   Verificando porta 8081...
echo ========================================
echo.

REM Verificar processos usando a porta 8081
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081 ^| findstr LISTENING') do (
    set PID=%%a
    goto :found
)

echo Nenhum processo encontrado usando a porta 8081.
goto :end

:found
echo Processo encontrado: PID %PID%
echo.

REM Obter informações do processo
for /f "tokens=1" %%b in ('tasklist /FI "PID eq %PID%" /NH') do (
    echo Nome do processo: %%b
)

echo.
set /p CONFIRM="Deseja encerrar este processo? (S/N): "

if /i "%CONFIRM%"=="S" (
    echo Encerrando processo PID %PID%...
    taskkill /F /PID %PID%
    if %ERRORLEVEL% EQU 0 (
        echo.
        echo Processo encerrado com sucesso!
    ) else (
        echo.
        echo Erro ao encerrar o processo.
    )
) else (
    echo Operacao cancelada.
)

:end
echo.
echo ========================================
pause
