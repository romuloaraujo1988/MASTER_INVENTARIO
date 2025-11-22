# Script para sincronizar dados do PostgreSQL para SQLite offline
# Sistema de Inventário de Patrimônio

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Sincronização PostgreSQL -> SQLite" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configurações
$SQLITE_DB = "data\inventario.db"
$SQLITE_SCRIPT = "sql\criar_tabelas_sqlite_offline.sql"
$JAVA_CLASS = "com.inventario.offline.SyncPostgresToSQLiteV2"

# Verificar se o banco SQLite existe
if (Test-Path $SQLITE_DB) {
    Write-Host "[INFO] Banco SQLite encontrado: $SQLITE_DB" -ForegroundColor Green
} else {
    Write-Host "[AVISO] Banco SQLite não encontrado. Criando..." -ForegroundColor Yellow
    
    # Criar diretório data se não existir
    if (!(Test-Path "data")) {
        New-Item -ItemType Directory -Path "data" | Out-Null
    }
    
    # Criar banco SQLite executando o script
    if (Test-Path $SQLITE_SCRIPT) {
        Write-Host "[INFO] Executando script de criação: $SQLITE_SCRIPT" -ForegroundColor Cyan
        
        # Usar sqlite3 se disponível
        $sqlite3 = Get-Command sqlite3 -ErrorAction SilentlyContinue
        if ($sqlite3) {
            Get-Content $SQLITE_SCRIPT | sqlite3 $SQLITE_DB
            Write-Host "[OK] Banco SQLite criado com sucesso!" -ForegroundColor Green
        } else {
            Write-Host "[ERRO] sqlite3 não encontrado. Instale o SQLite CLI ou use o Java." -ForegroundColor Red
            Write-Host "[INFO] Tentando criar via Java..." -ForegroundColor Yellow
        }
    }
}

Write-Host ""
Write-Host "[INFO] Iniciando sincronização de dados..." -ForegroundColor Cyan
Write-Host ""

# Executar classe Java de sincronização
Write-Host "[INFO] Executando: mvn exec:java -Dexec.mainClass=$JAVA_CLASS" -ForegroundColor Cyan
mvn exec:java -Dexec.mainClass="$JAVA_CLASS" -Dexec.cleanupDaemonThreads=false

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "  Sincronização concluída com sucesso!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "[INFO] Banco SQLite atualizado: $SQLITE_DB" -ForegroundColor Green
    Write-Host "[INFO] O sistema está pronto para operar em modo offline." -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "  ERRO na sincronização!" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "[ERRO] Verifique os logs acima para mais detalhes." -ForegroundColor Red
    Write-Host "[DICA] Certifique-se de que o PostgreSQL está acessível." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Pressione qualquer tecla para sair..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
