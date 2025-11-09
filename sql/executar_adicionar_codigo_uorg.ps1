# Script PowerShell para adicionar coluna codigo_uorg na tabela TABELA_CAMPUS
# Data: 2025-11-08

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Adicionar Codigo UOrg - Campus" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configurações do banco de dados
$configFile = "configuracao_banco.json"

# Verificar se o arquivo de configuração existe
if (-not (Test-Path $configFile)) {
    Write-Host "ERRO: Arquivo de configuracao nao encontrado: $configFile" -ForegroundColor Red
    Write-Host "Procurando no diretorio do usuario..." -ForegroundColor Yellow
    $configFile = "$env:USERPROFILE\$configFile"
    
    if (-not (Test-Path $configFile)) {
        Write-Host "ERRO: Arquivo de configuracao nao encontrado em: $configFile" -ForegroundColor Red
        exit 1
    }
}

Write-Host "Lendo configuracao do banco de dados..." -ForegroundColor Yellow

# Ler configuração do banco
try {
    $config = Get-Content $configFile -Raw | ConvertFrom-Json
    $host_db = $config.postgresql.host
    $database = $config.postgresql.database
    $user = $config.postgresql.user
    $password = $config.postgresql.password
    $port = $config.postgresql.port
    
    Write-Host "Host: $host_db" -ForegroundColor Gray
    Write-Host "Database: $database" -ForegroundColor Gray
    Write-Host "User: $user" -ForegroundColor Gray
    Write-Host "Port: $port" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "ERRO ao ler configuracao: $_" -ForegroundColor Red
    exit 1
}

# Localizar psql.exe
Write-Host "Localizando PostgreSQL..." -ForegroundColor Yellow
$psqlPath = Get-ChildItem "C:\Program Files\PostgreSQL" -Recurse -Filter "psql.exe" -ErrorAction SilentlyContinue | Select-Object -First 1 -ExpandProperty FullName

if (-not $psqlPath) {
    Write-Host "ERRO: psql.exe nao encontrado. Verifique se o PostgreSQL esta instalado." -ForegroundColor Red
    exit 1
}

Write-Host "PostgreSQL encontrado: $psqlPath" -ForegroundColor Green
Write-Host ""

# Definir senha como variável de ambiente
$env:PGPASSWORD = $password

# Script SQL
$sqlFile = "sql/adicionar_codigo_uorg_campus.sql"

if (-not (Test-Path $sqlFile)) {
    Write-Host "ERRO: Arquivo SQL nao encontrado: $sqlFile" -ForegroundColor Red
    exit 1
}

Write-Host "Executando script SQL..." -ForegroundColor Yellow
Write-Host "Arquivo: $sqlFile" -ForegroundColor Gray
Write-Host ""

# Executar script SQL
try {
    $output = & $psqlPath -h $host_db -U $user -d $database -p $port -f $sqlFile 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "Script executado com sucesso!" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "Saida do PostgreSQL:" -ForegroundColor Cyan
        Write-Host $output
        Write-Host ""
        Write-Host "A coluna 'codigo_uorg' foi adicionada na tabela TABELA_CAMPUS" -ForegroundColor Green
        Write-Host ""
        Write-Host "Proximos passos:" -ForegroundColor Yellow
        Write-Host "1. Abrir a tela de gerenciamento de Campus" -ForegroundColor White
        Write-Host "2. Editar cada campus e adicionar o codigo UOrg" -ForegroundColor White
        Write-Host "3. Consultar o manual: DOCUMENTACAO/CODIGO_UORG_SIADS.md" -ForegroundColor White
    } else {
        Write-Host "========================================" -ForegroundColor Red
        Write-Host "ERRO ao executar script!" -ForegroundColor Red
        Write-Host "========================================" -ForegroundColor Red
        Write-Host ""
        Write-Host "Saida do PostgreSQL:" -ForegroundColor Yellow
        Write-Host $output
        exit 1
    }
} catch {
    Write-Host "ERRO: $_" -ForegroundColor Red
    exit 1
} finally {
    # Limpar senha da variável de ambiente
    Remove-Item Env:\PGPASSWORD -ErrorAction SilentlyContinue
}

Write-Host ""
Write-Host "Pressione qualquer tecla para sair..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
