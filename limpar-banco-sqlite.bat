@echo off
REM Script para limpar bancos SQLite antigos
REM Sistema de Inventário IFMT

echo ========================================
echo Limpando Bancos SQLite Antigos
echo ========================================
echo.

REM Remover banco do diretório local
if exist ".\data\inventario.db" (
    echo Removendo: .\data\inventario.db
    del /F /Q ".\data\inventario.db"
)

if exist ".\data\inventario_offline.db" (
    echo Removendo: .\data\inventario_offline.db
    del /F /Q ".\data\inventario_offline.db"
)

REM Remover backups antigos
if exist ".\data\inventario_offline_backup_*.db" (
    echo Removendo backups antigos...
    del /F /Q ".\data\inventario_offline_backup_*.db"
)

REM Remover banco do diretório do usuário
if exist "%USERPROFILE%\.inventario\data\inventario.db" (
    echo Removendo: %USERPROFILE%\.inventario\data\inventario.db
    del /F /Q "%USERPROFILE%\.inventario\data\inventario.db"
)

if exist "%USERPROFILE%\.inventario\data\inventario_offline.db" (
    echo Removendo: %USERPROFILE%\.inventario\data\inventario_offline.db
    del /F /Q "%USERPROFILE%\.inventario\data\inventario_offline.db"
)

echo.
echo ========================================
echo Limpeza Concluída!
echo ========================================
echo.
echo O banco será recriado automaticamente na próxima importação.
echo.
pause
