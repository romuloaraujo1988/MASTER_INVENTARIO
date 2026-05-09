#Requires -Version 5.1
# ============================================================
# SIHCP - Script de Setup Automatizado (Windows)
# Conduz o Administrador_Campus por todas as etapas de
# implantação de forma interativa, com validação em cada passo.
# ============================================================

param()

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# Diretório base (raiz do pacote)
$BaseDir = Split-Path -Parent $PSScriptRoot
Set-Location $BaseDir

# Cores para output
function Write-Step   { param($msg) Write-Host $msg -ForegroundColor Cyan }
function Write-Ok     { param($msg) Write-Host "[OK] $msg" -ForegroundColor Green }
function Write-Warn   { param($msg) Write-Host "[AVISO] $msg" -ForegroundColor Yellow }
function Write-Err    { param($msg) Write-Host "[ERRO] $msg" -ForegroundColor Red }
function Write-Info   { param($msg) Write-Host "      $msg" -ForegroundColor Gray }

Clear-Host
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  SIHCP - Setup Automatizado de Implantação em Campus" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

# ============================================================
# [1/6] Validar pré-requisitos
# ============================================================
Write-Step "[1/6] Validando pré-requisitos..."

# Verificar Java 21+
try {
    $javaOutput = & java -version 2>&1 | Select-String "version"
    if (-not $javaOutput) { throw "Java não encontrado" }
    $javaVerRaw = ($javaOutput -replace '.*version "([^"]+)".*', '$1').Trim()
    $javaMajor  = [int]($javaVerRaw -split '\.')[0]
    if ($javaMajor -lt 21) { throw "Versão $javaMajor encontrada, necessário 21+" }
    Write-Ok "JDK $javaMajor encontrado."
} catch {
    Write-Err "JDK 21 não encontrado ou versão insuficiente."
    Write-Info "Instale o JDK 21 em: https://adoptium.net"
    exit 1
}

# Verificar psql (PostgreSQL)
try {
    $psqlOutput = & psql --version 2>&1
    if (-not $psqlOutput) { throw "psql não encontrado" }
    Write-Ok "PostgreSQL client encontrado: $psqlOutput"
} catch {
    Write-Err "PostgreSQL (psql) não encontrado no PATH."
    Write-Info "Instale o PostgreSQL 12+ em: https://www.postgresql.org/download/"
    Write-Info "Certifique-se de que o diretório bin do PostgreSQL está no PATH."
    exit 1
}

Write-Host ""

# ============================================================
# [2/6] Coletar dados do campus
# ============================================================
Write-Step "[2/6] Coletando dados do campus..."
Write-Host ""

# --- Nome do campus ---
do {
    $NomeCampus = Read-Host "  Nome do campus (ex: IFMT - Campus Cuiabá)"
    if ([string]::IsNullOrWhiteSpace($NomeCampus)) {
        Write-Warn "O nome do campus não pode ser vazio."
    } elseif ($NomeCampus.Length -gt 100) {
        Write-Warn "O nome do campus deve ter no máximo 100 caracteres (atual: $($NomeCampus.Length))."
        $NomeCampus = ""
    }
} while ([string]::IsNullOrWhiteSpace($NomeCampus))

# --- Sigla ---
$Sigla = Read-Host "  Sigla do campus (ex: CBA)"

# --- Cidade ---
$Cidade = Read-Host "  Cidade"

# --- Estado ---
do {
    $Estado = (Read-Host "  Estado (UF, ex: MT)").ToUpper()
    if ($Estado.Length -ne 2) {
        Write-Warn "Informe a UF com 2 letras (ex: MT, SP, RJ)."
    }
} while ($Estado.Length -ne 2)

# --- Porta da API ---
do {
    $PortaApiStr = Read-Host "  Porta da API Mobile (padrão: 8080)"
    if ([string]::IsNullOrWhiteSpace($PortaApiStr)) { $PortaApiStr = "8080" }
    $PortaApi = 0
    $portaValida = [int]::TryParse($PortaApiStr, [ref]$PortaApi)
    if (-not $portaValida -or $PortaApi -lt 1024 -or $PortaApi -gt 65535) {
        Write-Warn "Porta inválida. Informe um valor entre 1024 e 65535."
        $PortaApi = 0
        continue
    }
    # Verificar se a porta está em uso
    $emUso = netstat -ano 2>$null | Select-String ":$PortaApi " | Select-String "LISTENING"
    if ($emUso) {
        Write-Warn "Porta $PortaApi já está em uso. Escolha outra porta."
        $PortaApi = 0
    }
} while ($PortaApi -eq 0)

# --- Host PostgreSQL ---
$HostPg = Read-Host "  Host do PostgreSQL (padrão: localhost)"
if ([string]::IsNullOrWhiteSpace($HostPg)) { $HostPg = "localhost" }

# --- Porta PostgreSQL ---
$PortaPgStr = Read-Host "  Porta do PostgreSQL (padrão: 5432)"
if ([string]::IsNullOrWhiteSpace($PortaPgStr)) { $PortaPgStr = "5432" }
$PortaPg = [int]$PortaPgStr

# --- Nome do banco ---
$Banco = Read-Host "  Nome do banco de dados (padrão: sispatrimonio)"
if ([string]::IsNullOrWhiteSpace($Banco)) { $Banco = "sispatrimonio" }

# --- Usuário PostgreSQL ---
do {
    $UsuarioPg = Read-Host "  Usuário do PostgreSQL"
} while ([string]::IsNullOrWhiteSpace($UsuarioPg))

# --- Senha PostgreSQL ---
$SenhaPgSecure = Read-Host "  Senha do PostgreSQL" -AsSecureString
$SenhaPg = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($SenhaPgSecure))

# --- Senha do admin SIHCP ---
do {
    $SenhaAdminSecure = Read-Host "  Senha do administrador SIHCP (usuário 'admin')" -AsSecureString
    $SenhaAdmin = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto(
        [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($SenhaAdminSecure))
    if ([string]::IsNullOrWhiteSpace($SenhaAdmin)) {
        Write-Warn "A senha do administrador não pode ser vazia."
    }
} while ([string]::IsNullOrWhiteSpace($SenhaAdmin))

Write-Host ""
Write-Ok "Dados coletados com sucesso."
Write-Host ""

# ============================================================
# [3/6] Gerar arquivos de configuração
# ============================================================
Write-Step "[3/6] Gerando arquivos de configuração..."

# Criar diretório config/ se não existir
if (-not (Test-Path "config")) {
    New-Item -ItemType Directory -Path "config" | Out-Null
}

# Gerar config/configuracao_banco.json
$ConfigJson = @"
{
  "campus": {
    "nome": "$NomeCampus",
    "sigla": "$Sigla",
    "cidade": "$Cidade",
    "estado": "$Estado",
    "responsavel_tecnico": "",
    "contato": ""
  },
  "postgresql": {
    "host": "$HostPg",
    "port": $PortaPg,
    "database": "$Banco",
    "user": "$UsuarioPg",
    "password": "$SenhaPg"
  },
  "api": {
    "porta": $PortaApi,
    "versao": "1.2.0"
  }
}
"@

$ConfigJson | Set-Content -Path "config\configuracao_banco.json" -Encoding UTF8
Write-Ok "config\configuracao_banco.json gerado."

# Gerar config/application.properties a partir do template
$TemplatePath = "config\application.properties.template"
if (Test-Path $TemplatePath) {
    $AppProps = Get-Content $TemplatePath -Raw
    $AppProps = $AppProps -replace '\$\{PORTA_API\}', $PortaApi
    $AppProps = $AppProps -replace '\$\{NOME_CAMPUS\}', $NomeCampus
    $AppProps = $AppProps -replace '\$\{SIGLA_CAMPUS\}', $Sigla
    $AppProps | Set-Content -Path "config\application.properties" -Encoding UTF8
    Write-Ok "config\application.properties gerado a partir do template."
} else {
    # Gerar application.properties básico
    @"
server.port=$PortaApi
spring.profiles.active=mobile,prod
campus.nome=$NomeCampus
campus.sigla=$Sigla
"@ | Set-Content -Path "config\application.properties" -Encoding UTF8
    Write-Ok "config\application.properties gerado (template não encontrado, usando padrão)."
}

Write-Host ""

# ============================================================
# [4/6] Criar banco e executar SQL
# ============================================================
Write-Step "[4/6] Criando banco de dados e executando SQL..."

$env:PGPASSWORD = $SenhaPg

# Criar banco de dados
Write-Info "Criando banco '$Banco'..."
try {
    $result = & psql -h $HostPg -p $PortaPg -U $UsuarioPg -d postgres `
        -c "CREATE DATABASE $Banco" 2>&1
    if ($LASTEXITCODE -ne 0 -and $result -notmatch "already exists") {
        throw $result
    }
    Write-Ok "Banco '$Banco' criado (ou já existia)."
} catch {
    Write-Err "Falha ao criar banco de dados."
    Write-Info "Erro: $_"
    Write-Info "Verifique as credenciais e tente novamente."
    Write-Info "As configurações já foram salvas em config\configuracao_banco.json"
    Write-Info "Você pode executar manualmente: psql -h $HostPg -p $PortaPg -U $UsuarioPg -d postgres -c `"CREATE DATABASE $Banco`""
    $env:PGPASSWORD = ""
    exit 1
}

# Executar SQL de setup
if (Test-Path "sql\setup_banco_completo.sql") {
    Write-Info "Executando sql\setup_banco_completo.sql..."
    try {
        $result = & psql -h $HostPg -p $PortaPg -U $UsuarioPg -d $Banco `
            -f "sql\setup_banco_completo.sql" 2>&1
        if ($LASTEXITCODE -ne 0) { throw $result }
        Write-Ok "Schema do banco criado com sucesso."
    } catch {
        Write-Err "Falha ao executar o script SQL."
        Write-Info "Etapa: Execução de sql\setup_banco_completo.sql"
        Write-Info "Erro: $_"
        Write-Info "As configurações já foram salvas. Execute manualmente:"
        Write-Info "  psql -h $HostPg -p $PortaPg -U $UsuarioPg -d $Banco -f sql\setup_banco_completo.sql"
        $env:PGPASSWORD = ""
        exit 1
    }
} else {
    Write-Warn "Arquivo sql\setup_banco_completo.sql não encontrado. Pulando criação do schema."
}

$env:PGPASSWORD = ""
Write-Host ""

# ============================================================
# [5/6] Criar usuário admin
# ============================================================
Write-Step "[5/6] Criando usuário administrador inicial..."

$env:PGPASSWORD = $SenhaPg

$SqlAdmin = @"
INSERT INTO TABELA_USUARIO (LOGIN, SENHA_HASH, NOME_COMPLETO, EMAIL, PERFIL, ATIVO)
VALUES (
    'admin',
    crypt('$SenhaAdmin', gen_salt('bf')),
    'Administrador',
    'admin@campus.ifmt.edu.br',
    'ADMIN',
    'S'
)
ON CONFLICT (LOGIN) DO NOTHING;
"@

try {
    $result = & psql -h $HostPg -p $PortaPg -U $UsuarioPg -d $Banco `
        -c $SqlAdmin 2>&1
    if ($LASTEXITCODE -ne 0) { throw $result }
    Write-Ok "Usuário 'admin' criado com sucesso."
} catch {
    Write-Warn "Não foi possível criar o usuário admin automaticamente."
    Write-Info "Erro: $_"
    Write-Info "Execute manualmente no banco '$Banco':"
    Write-Info "  INSERT INTO TABELA_USUARIO (LOGIN, SENHA_HASH, NOME_COMPLETO, EMAIL, PERFIL, ATIVO)"
    Write-Info "  VALUES ('admin', crypt('<senha>', gen_salt('bf')), 'Administrador', 'admin@campus.ifmt.edu.br', 'ADMIN', 'S')"
    Write-Info "  ON CONFLICT (LOGIN) DO NOTHING;"
}

$env:PGPASSWORD = ""
Write-Host ""

# ============================================================
# [6/6] Gerar QR Code e exibir resumo
# ============================================================
Write-Step "[6/6] Gerando QR Code e exibindo resumo..."

# Detectar IP local da máquina
$IpLocal = (Get-NetIPAddress -AddressFamily IPv4 |
    Where-Object { $_.InterfaceAlias -notmatch "Loopback" -and $_.IPAddress -notmatch "^169\." } |
    Select-Object -First 1).IPAddress

if (-not $IpLocal) { $IpLocal = "localhost" }

$UrlApi = "http://${IpLocal}:${PortaApi}/api/mobile"

# Criar diretório qrcode/ se não existir
if (-not (Test-Path "qrcode")) {
    New-Item -ItemType Directory -Path "qrcode" | Out-Null
}

# Gerar QR Code via QrCodeGenerator
if (Test-Path "bin\mobile-server.jar") {
    Write-Info "Gerando QR Code..."
    try {
        & java -cp "bin\mobile-server.jar" com.inventario.util.QrCodeGenerator `
            "$UrlApi" "qrcode\api-qrcode.png" 300 2>&1 | Out-Null
        if ($LASTEXITCODE -eq 0) {
            Write-Ok "QR Code gerado em qrcode\api-qrcode.png"
        } else {
            Write-Warn "Não foi possível gerar o QR Code automaticamente."
        }
    } catch {
        Write-Warn "Não foi possível gerar o QR Code: $_"
    }
} else {
    Write-Warn "bin\mobile-server.jar não encontrado. QR Code não gerado."
}

# Exibir resumo
Write-Host ""
Write-Host "============================================================" -ForegroundColor Green
Write-Host "  ✅ Implantação concluída!" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green
Write-Host ""
Write-Host "  Campus    : $NomeCampus ($Sigla)" -ForegroundColor White
Write-Host "  URL da API: $UrlApi" -ForegroundColor White
Write-Host "  Admin     : admin / [senha definida durante o setup]" -ForegroundColor White
Write-Host "  APK       : bin\sihcp-mobile.apk" -ForegroundColor White
Write-Host "  QR Code   : qrcode\api-qrcode.png" -ForegroundColor White
Write-Host ""
Write-Host "  Próximos passos:" -ForegroundColor Cyan
Write-Host "  1. Inicie o servidor: scripts\iniciar-servidor.bat" -ForegroundColor Cyan
Write-Host "  2. Verifique a saúde: scripts\verificar-saude.ps1" -ForegroundColor Cyan
Write-Host "  3. Distribua o APK : bin\sihcp-mobile.apk" -ForegroundColor Cyan
Write-Host ""
