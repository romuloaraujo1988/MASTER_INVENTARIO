# Script PowerShell para criar banco de dados SQLite offline
# Sistema de Inventário de Patrimônio

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Criando Banco de Dados SQLite Offline" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Criar diretório data se não existir
if (-not (Test-Path "data")) {
    Write-Host "Criando diretório data..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Path "data" | Out-Null
}

# Verificar se o arquivo SQL existe
$sqlFile = "sql\criar_tabelas_sqlite_offline.sql"
if (-not (Test-Path $sqlFile)) {
    Write-Host "ERRO: Arquivo $sqlFile não encontrado!" -ForegroundColor Red
    Read-Host "Pressione Enter para sair"
    exit 1
}

# Caminho do banco de dados
$dbPath = "data\inventario.db"

# Remover banco existente se houver
if (Test-Path $dbPath) {
    Write-Host "Removendo banco de dados existente..." -ForegroundColor Yellow
    Remove-Item $dbPath -Force
}

Write-Host "Criando banco de dados: $dbPath" -ForegroundColor Green
Write-Host ""

# Verificar se sqlite3 está disponível
$sqlite3 = Get-Command sqlite3 -ErrorAction SilentlyContinue

if ($null -eq $sqlite3) {
    Write-Host "AVISO: sqlite3.exe não encontrado no PATH" -ForegroundColor Yellow
    Write-Host "Tentando usar sqlite3.exe local..." -ForegroundColor Yellow
    
    if (Test-Path "sqlite3.exe") {
        $sqlite3Path = ".\sqlite3.exe"
    } else {
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Red
        Write-Host "ERRO: sqlite3.exe não encontrado!" -ForegroundColor Red
        Write-Host "========================================" -ForegroundColor Red
        Write-Host ""
        Write-Host "Por favor, baixe o sqlite3.exe de:" -ForegroundColor Yellow
        Write-Host "https://www.sqlite.org/download.html" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "E coloque na pasta do projeto ou adicione ao PATH" -ForegroundColor Yellow
        Write-Host ""
        Read-Host "Pressione Enter para sair"
        exit 1
    }
} else {
    $sqlite3Path = "sqlite3"
}

# Executar script SQL
try {
    Write-Host "Executando script SQL..." -ForegroundColor Yellow
    Get-Content $sqlFile | & $sqlite3Path $dbPath
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "Banco SQLite criado com sucesso!" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "Localização: $dbPath" -ForegroundColor Cyan
        Write-Host ""
        
        # Verificar tabelas criadas
        Write-Host "Verificando tabelas criadas..." -ForegroundColor Yellow
        $tables = & $sqlite3Path $dbPath ".tables"
        Write-Host "Tabelas encontradas:" -ForegroundColor Cyan
        Write-Host $tables -ForegroundColor White
        Write-Host ""
        
        # Verificar dados iniciais
        Write-Host "Verificando dados iniciais..." -ForegroundColor Yellow
        $userCount = & $sqlite3Path $dbPath "SELECT COUNT(*) FROM USUARIO;"
        $invCount = & $sqlite3Path $dbPath "SELECT COUNT(*) FROM TABELA_INVENTARIO;"
        $salaCount = & $sqlite3Path $dbPath "SELECT COUNT(*) FROM SALA;"
        
        Write-Host "Usuários: $userCount" -ForegroundColor White
        Write-Host "Inventários: $invCount" -ForegroundColor White
        Write-Host "Salas: $salaCount" -ForegroundColor White
        Write-Host ""
        
    } else {
        throw "Erro ao executar script SQL"
    }
    
} catch {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "ERRO ao criar banco SQLite!" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "Detalhes do erro:" -ForegroundColor Yellow
    Write-Host $_.Exception.Message -ForegroundColor Red
    Write-Host ""
    Read-Host "Pressione Enter para sair"
    exit 1
}

Write-Host "Processo concluído!" -ForegroundColor Green
Read-Host "Pressione Enter para sair"
