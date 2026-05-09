#Requires -Version 5.1
# ============================================================
# SIHCP - Script de Verificação de Saúde (Windows)
# Executa 4 testes em sequência e gera relatório de saúde.
# ============================================================

param(
    [string]$SenhaAdmin = ""
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "SilentlyContinue"

# Diretório base (raiz do pacote)
$BaseDir = Split-Path -Parent $PSScriptRoot
Set-Location $BaseDir

# Cores para output
function Write-Ok   { param($msg) Write-Host "[OK]  $msg" -ForegroundColor Green }
function Write-Fail { param($msg) Write-Host "[FAIL] $msg" -ForegroundColor Red }
function Write-Info { param($msg) Write-Host "      $msg" -ForegroundColor Gray }

# Data/hora para o relatório
$DataHora = Get-Date -Format "yyyyMMdd-HHmm"
$RelatorioPath = "relatorio-saude-$DataHora.txt"

# Acumular linhas do relatório
$Relatorio = [System.Collections.Generic.List[string]]::new()
$TodosOk = $true

function Add-Relatorio { param($linha) $Relatorio.Add($linha) }

# ============================================================
# Ler configurações do configuracao_banco.json
# ============================================================
if (-not (Test-Path "config\configuracao_banco.json")) {
    Write-Host "[ERRO] Arquivo config\configuracao_banco.json não encontrado." -ForegroundColor Red
    Write-Host "       Execute o setup primeiro: scripts\setup.ps1" -ForegroundColor Gray
    exit 1
}

try {
    $Config = Get-Content "config\configuracao_banco.json" | ConvertFrom-Json
    $DbHost   = $Config.postgresql.host
    $DbPort   = $Config.postgresql.port
    $ApiPort  = $Config.api.porta
    $Campus   = $Config.campus.nome
    $Versao   = $Config.api.versao
} catch {
    Write-Host "[ERRO] Falha ao ler config\configuracao_banco.json: $_" -ForegroundColor Red
    exit 1
}

if (-not $DbHost)  { $DbHost  = "localhost" }
if (-not $DbPort)  { $DbPort  = 5432 }
if (-not $ApiPort) { $ApiPort = 8080 }
if (-not $Campus)  { $Campus  = "Campus" }
if (-not $Versao)  { $Versao  = "N/A" }

# Detectar IP local
$IpLocal = (Get-NetIPAddress -AddressFamily IPv4 |
    Where-Object { $_.InterfaceAlias -notmatch "Loopback" -and $_.IPAddress -notmatch "^169\." } |
    Select-Object -First 1).IPAddress
if (-not $IpLocal) { $IpLocal = "localhost" }

$BaseUrl = "http://${IpLocal}:${ApiPort}"

# Solicitar senha do admin se não fornecida
if ([string]::IsNullOrWhiteSpace($SenhaAdmin)) {
    $SenhaAdminSecure = Read-Host "Senha do administrador SIHCP (usuário 'admin')" -AsSecureString
    $SenhaAdmin = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto(
        [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($SenhaAdminSecure))
}

# Cabeçalho
Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  SIHCP - Verificação de Saúde do Sistema" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  Campus : $Campus"
Write-Host "  Data   : $(Get-Date -Format 'dd/MM/yyyy HH:mm:ss')"
Write-Host "  Versão : $Versao"
Write-Host "  URL    : $BaseUrl"
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

Add-Relatorio "RELATÓRIO DE SAÚDE SIHCP"
Add-Relatorio "Campus: $Campus"
Add-Relatorio "Data: $(Get-Date -Format 'dd/MM/yyyy HH:mm:ss')"
Add-Relatorio "Versão: $Versao"
Add-Relatorio ""

# ============================================================
# Teste 1: Conectividade TCP com o banco
# ============================================================
Write-Host "Teste 1: Conectividade com o banco de dados ($DbHost`:$DbPort)..."
try {
    $tcpResult = Test-NetConnection -ComputerName $DbHost -Port $DbPort `
        -InformationLevel Quiet -WarningAction SilentlyContinue
    if ($tcpResult) {
        Write-Ok "Banco de dados: conectado ($DbHost`:$DbPort)"
        Add-Relatorio "[OK]  Banco de dados: conectado ($DbHost`:$DbPort)"
    } else {
        throw "Conexão TCP recusada"
    }
} catch {
    $TodosOk = $false
    Write-Fail "Banco de dados: inacessível ($DbHost`:$DbPort)"
    Write-Info "Erro: $_"
    Write-Info "Consulte: SOLUCAO_PROBLEMAS.md#banco"
    Add-Relatorio "[FAIL] Banco de dados: inacessível ($DbHost`:$DbPort)"
    Add-Relatorio "       Erro: $_"
    Add-Relatorio "       Consulte: SOLUCAO_PROBLEMAS.md#banco"
}

# ============================================================
# Teste 2: API respondendo (GET /api/mobile/health)
# ============================================================
Write-Host ""
Write-Host "Teste 2: API Mobile respondendo ($BaseUrl/api/mobile/health)..."
try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/api/mobile/health" `
        -Method GET -TimeoutSec 10 -UseBasicParsing -ErrorAction Stop
    if ($response.StatusCode -eq 200) {
        Write-Ok "API Mobile: respondendo (HTTP $($response.StatusCode))"
        Add-Relatorio "[OK]  API Mobile: respondendo (GET /api/mobile/health → $($response.StatusCode))"
    } else {
        throw "HTTP $($response.StatusCode)"
    }
} catch {
    $TodosOk = $false
    Write-Fail "API Mobile: não respondendo"
    Write-Info "Erro: $_"
    Write-Info "Consulte: SOLUCAO_PROBLEMAS.md#api"
    Add-Relatorio "[FAIL] API Mobile: não respondendo"
    Add-Relatorio "       Erro: $_"
    Add-Relatorio "       Consulte: SOLUCAO_PROBLEMAS.md#api"
}

# ============================================================
# Teste 3: Autenticação (POST /api/mobile/auth/login)
# ============================================================
Write-Host ""
Write-Host "Teste 3: Autenticação do administrador..."
$Token = $null
try {
    $loginBody = @{ login = "admin"; senha = $SenhaAdmin } | ConvertTo-Json
    $loginResponse = Invoke-WebRequest -Uri "$BaseUrl/api/mobile/auth/login" `
        -Method POST -Body $loginBody -ContentType "application/json" `
        -TimeoutSec 10 -UseBasicParsing -ErrorAction Stop
    if ($loginResponse.StatusCode -eq 200) {
        $loginData = $loginResponse.Content | ConvertFrom-Json
        # Tentar extrair token de diferentes estruturas de resposta
        $Token = $loginData.token
        if (-not $Token) { $Token = $loginData.data.token }
        if (-not $Token) { $Token = $loginData.accessToken }
        if ($Token) {
            Write-Ok "Autenticação: admin autenticado com sucesso"
            Add-Relatorio "[OK]  Autenticação: admin autenticado com sucesso"
        } else {
            throw "Token não encontrado na resposta"
        }
    } else {
        throw "HTTP $($loginResponse.StatusCode)"
    }
} catch {
    $TodosOk = $false
    Write-Fail "Autenticação: falha ao autenticar admin"
    Write-Info "Erro: $_"
    Write-Info "Consulte: SOLUCAO_PROBLEMAS.md#autenticacao"
    Add-Relatorio "[FAIL] Autenticação: falha ao autenticar admin"
    Add-Relatorio "       Erro: $_"
    Add-Relatorio "       Consulte: SOLUCAO_PROBLEMAS.md#autenticacao"
}

# ============================================================
# Teste 4: Listagem de patrimônios (GET /api/mobile/patrimonio)
# ============================================================
Write-Host ""
Write-Host "Teste 4: Listagem de patrimônios..."
if ($Token) {
    try {
        $patrimonioResponse = Invoke-WebRequest -Uri "$BaseUrl/api/mobile/patrimonio" `
            -Method GET -Headers @{ Authorization = "Bearer $Token" } `
            -TimeoutSec 15 -UseBasicParsing -ErrorAction Stop
        if ($patrimonioResponse.StatusCode -eq 200) {
            $patrimonioData = $patrimonioResponse.Content | ConvertFrom-Json
            $total = 0
            if ($patrimonioData.data) { $total = @($patrimonioData.data).Count }
            Write-Ok "Patrimônios: listagem retornou $total registros (HTTP $($patrimonioResponse.StatusCode))"
            Add-Relatorio "[OK]  Patrimônios: listagem retornou $total registros"
        } else {
            throw "HTTP $($patrimonioResponse.StatusCode)"
        }
    } catch {
        $TodosOk = $false
        Write-Fail "Patrimônios: falha na listagem"
        Write-Info "Erro: $_"
        Write-Info "Consulte: SOLUCAO_PROBLEMAS.md#patrimonio"
        Add-Relatorio "[FAIL] Patrimônios: falha na listagem"
        Add-Relatorio "       Erro: $_"
        Add-Relatorio "       Consulte: SOLUCAO_PROBLEMAS.md#patrimonio"
    }
} else {
    Write-Fail "Patrimônios: teste ignorado (autenticação falhou)"
    Add-Relatorio "[SKIP] Patrimônios: teste ignorado (autenticação falhou)"
}

# ============================================================
# Resultado final
# ============================================================
Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan

if ($TodosOk) {
    Write-Host "  ✅ Sistema SIHCP operacional e pronto para uso" -ForegroundColor Green
    Write-Host "  URL de acesso: $BaseUrl" -ForegroundColor Green
    Add-Relatorio ""
    Add-Relatorio "STATUS GERAL: ✅ OPERACIONAL"
    Add-Relatorio "URL de acesso: $BaseUrl"
} else {
    Write-Host "  ❌ Um ou mais testes falharam. Verifique os erros acima." -ForegroundColor Red
    Write-Host "  Consulte: SOLUCAO_PROBLEMAS.md" -ForegroundColor Yellow
    Add-Relatorio ""
    Add-Relatorio "STATUS GERAL: ❌ COM FALHAS"
    Add-Relatorio "Consulte: SOLUCAO_PROBLEMAS.md"
}

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

# Gravar relatório
$Relatorio | Set-Content -Path $RelatorioPath -Encoding UTF8
Write-Host "  Relatório gravado em: $RelatorioPath" -ForegroundColor Gray
Write-Host ""

if (-not $TodosOk) { exit 1 }
