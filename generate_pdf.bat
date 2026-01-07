@echo off
echo Gerando PDF do documento de Melhoria de Processo...
npx md-to-pdf MELHORIA_PROCESSO_COLETA_PATRIMONIAL.md
if %ERRORLEVEL% EQU 0 (
    echo PDF gerado com sucesso: MELHORIA_PROCESSO_COLETA_PATRIMONIAL.pdf
) else (
    echo Erro ao gerar PDF. Verifique se o NodeJS esta instalado.
)
pause
