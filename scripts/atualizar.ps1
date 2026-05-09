#Requires -Version 5.1
# ============================================================
# SIHCP - Script de Atualização de Versão (Windows)
# Realiza backup, substitui JARs, aplica migrações SQL e
# reinicia o servidor com rollback automático em caso de falha.
# ============================================================

param(
    [string]$UsuarioPg  = "",
    [string]$SenhaPg    = "",
    [string]$UpdateDir  = "update"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# Diretório base (raiz do pacote)
$BaseDir = Split-Path -Parent $PSScriptRoot
Set-Location $BaseDir

# Cores para output
function Write-Step { param($msg) Write-Host $msg -ForegroundColor Cyan }
function Write-Ok   { param($msg) Write-Host "[OK] $msg" -ForegroundColor Green }
function Write-Warn { param($msg) Write-Host "[AVISO] $msg" -ForegroundColor Yellow }
function Write-Err  { param($msg) Write-Host "[ERRO] $msg" -ForegroundColor Red }
function Write-Info { param($msg) Write-Host "      $msg" -ForegroundColor Gray }

Clear-Host
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  SIHCP - Atualização de Versão" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

# ============================================================
# Ler configurações
# ============================================================
if (-not (Test-Path "config\configuracao_banco.json")) {
    Write-Err "Arquivo config\configuracao_banco.json não encontrado."
    Write-Info "Execute o setup primeiro: scripts\setup.ps1"
    exit 1
}

try {
    $Config  = Get-Content "config\configuracao_banco.json" | ConvertFrom-Json
    $DbHost  = $Config.postgresql.host
    $DbPort  = $Config.postgresql.port
    $Banco   = $Config.postgresql.database
    $ApiPort = $Config.api.porta
    if (-not $UsuarioPg) { $UsuarioPg = $Config.postgresql.user }
} catch {
    Write-Err "Falha ao ler config\configuracao_banco.json: $_"
    exit 1
}

if (-not $DbHost)  { $DbHost  = "localhost" }
if (-not $DbPort)  { $DbPort  = 5432 }
if (-not $Banco)   { $Banco   = "sispatrimonio" }
if (-not $ApiPort) { $ApiPort = 8080 }

# Solicitar credenciais se não fornecidas
if ([string]::IsNullOrWhiteSpace($UsuarioPg)) {
    $UsuarioPg = Read-Host "Usuário do PostgreSQL"
}
if ([string]::IsNullOrWhiteSpace($SenhaPg)) {
    $SenhaPgSecure = Read-Host "Senha do PostgreSQL" -AsSecureString
    $SenhaPg = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto(
        [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($SenhaPgSecure))
}

# Variáveis de controle
$DataHora   = Get-Date -Format "yyyyMMdd_HHmmss"
$BackupPath = "backups\sispatrimonio_backup_$DataHora.backup"
$VersaoAnterior = "N/A"
$VersaoAtual    = "N/A"
$MigracoesAplicadas = 0

# ============================================================
# [1/6] Backup do banco de dados
# ============================================================
Write-Step "[1/6] Criando backup do banco de dados..."

if (-not (Test-Path "backups")) {
    New-Item -ItemType Directory -Path "backups" | Out-Null
}

$env:PGPASSWORD = $SenhaPg
try {
    & pg_dump -h $DbHost -p $DbPort -U $UsuarioPg `
        -F c -f $BackupPath $Banco 2>&1
    if ($LASTEXITCODE -ne 0) { throw "pg_dump retornou código $LASTEXITCODE" }
    $tamanhoMB = [math]::Round((Get-Item $BackupPath).Length / 1MB, 2)
    Write-Ok "Backup criado: $BackupPath ($tamanhoMB MB)"
} catch {
    Write-Err "Falha ao criar backup. Atualização cancelada."
    Write-Info "Erro: $_"
    Write-Info "Nenhuma alteração foi aplicada."
    $env:PGPASSWORD = ""
    exit 1
}
$env:PGPASSWORD = ""

# Obter versão anterior do schema
$env:PGPASSWORD = $SenhaPg
try {
    $VersaoAnterior = (& psql -h $DbHost -p $DbPort -U $UsuarioPg -d $Banco `
        -t -c "SELECT versao FROM schema_version ORDER BY id DESC LIMIT 1" 2>$null).Trim()
    if (-not $VersaoAnterior) { $VersaoAnterior = "N/A" }
} catch { $VersaoAnterior = "N/A" }
$env:PGPASSWORD = ""

Write-Info "Versão atual do schema: $VersaoAnterior"
Write-Host ""

# ============================================================
# [2/6] Parar o servidor
# ============================================================
Write-Step "[2/6] Parando o servidor..."

$JavaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue |
    Where-Object { $_.CommandLine -match "mobile-server\.jar" }

if ($JavaProcesses) {
    foreach ($proc in $JavaProcesses) {
        try {
            $proc | Stop-Process -Force
            Write-Ok "Processo Java (PID $($proc.Id)) encerrado."
        } catch {
            Write-Warn "Não foi possível encerrar o processo PID $($proc.Id): $_"
        }
    }
    Start-Sleep -Seconds 3
} else {
    Write-Info "Nenhum processo do servidor encontrado em execução."
}
Write-Host ""

# ============================================================
# [3/6] Substituir JARs
# ============================================================
Write-Step "[3/6] Substituindo JARs em bin/..."

if (-not (Test-Path $UpdateDir)) {
    Write-Warn "Diretório '$UpdateDir' não encontrado. Pulando substituição de JARs."
    Write-Info "Coloque os novos JARs em '$UpdateDir\' antes de executar este script."
} else {
    $jarsNovos = Get-ChildItem -Path $UpdateDir -Filter "*.jar" -ErrorAction SilentlyContinue
    if ($jarsNovos.Count -eq 0) {
        Write-Warn "Nenhum JAR encontrado em '$UpdateDir'. Pulando substituição."
    } else {
        if (-not (Test-Path "bin")) { New-Item -ItemType Directory -Path "bin" | Out-Null }
        foreach ($jar in $jarsNovos) {
            $destino = "bin\$($jar.Name)"
            Copy-Item -Path $jar.FullName -Destination $destino -Force
            Write-Ok "JAR atualizado: $destino"
        }
    }
}
Write-Host ""

# ============================================================
# [4/6] Aplicar scripts SQL de migração
# ============================================================
Write-Step "[4/6] Aplicando scripts SQL de migração..."

$env:PGPASSWORD = $SenhaPg

# Obter versões já aplicadas
$VersoesAplicadas = @()
try {
    $rows = & psql -h $DbHost -p $DbPort -U $UsuarioPg -d $Banco `
        -t -c "SELECT versao FROM schema_version ORDER BY id" 2>$null
    $VersoesAplicadas = $rows | ForEach-Object { $_.Trim() } | Where-Object { $_ -ne "" }
} catch {
    Write-Warn "Não foi possível consultar schema_version. Assumindo banco sem migrações."
}

# Procurar scripts de migração em sql/migrations/
$MigrationDir = "sql\migrations"
if (Test-Path $MigrationDir) {
    $scripts = Get-ChildItem -Path $MigrationDir -Filter "*.sql" | Sort-Object Name
    foreach ($script in $scripts) {
        # Extrair versão do nome do arquivo (ex: V1.1.0__descricao.sql → 1.1.0)
        $versaoScript = ($script.Name -replace '^V([^_]+)__.*\.sql$', '$1')
        if ($VersoesAplicadas -contains $versaoScript) {
            Write-Info "Migração $versaoScript já aplicada. Pulando."
            continue
        }

        Write-Info "Aplicando migração: $($script.Name)..."
        try {
            $result = & psql -h $DbHost -p $DbPort -U $UsuarioPg -d $Banco `
                -f $script.FullName 2>&1
            if ($LASTEXITCODE -ne 0) { throw $result }
            $MigracoesAplicadas++
            $VersaoAtual = $versaoScript
            Write-Ok "Migração $versaoScript aplicada."
        } catch {
            Write-Err "Falha ao aplicar migração: $($script.Name)"
            Write-Info "Erro: $_"
            Write-Info "Iniciando rollback via pg_restore..."

            # Rollback: restaurar backup
            $env:PGPASSWORD = $SenhaPg
            try {
                & pg_restore -h $DbHost -p $DbPort -U $UsuarioPg `
                    -d $Banco -c $BackupPath 2>&1
                Write-Ok "Rollback concluído. Banco restaurado para o estado anterior."
            } catch {
                Write-Err "Falha no rollback: $_"
                Write-Info "Restaure manualmente: pg_restore -h $DbHost -p $DbPort -U $UsuarioPg -d $Banco -c $BackupPath"
            }
            $env:PGPASSWORD = ""
            exit 1
        }
    }
} else {
    Write-Info "Diretório '$MigrationDir' não encontrado. Nenhuma migração aplicada."
}

if ($MigracoesAplicadas -eq 0) {
    Write-Info "Nenhuma migração nova para aplicar."
}

$env:PGPASSWORD = ""
Write-Host ""

# ============================================================
# [5/6] Reiniciar o servidor
# ============================================================
Write-Step "[5/6] Reiniciando o servidor..."

if (Test-Path "bin\mobile-server.jar") {
    try {
        $startInfo = New-Object System.Diagnostics.ProcessStartInfo
        $startInfo.FileName  = "java"
        $startInfo.Arguments = "-Xms256m -Xmx1g -Dinventario.config.mode=APP_DIR -jar bin\mobile-server.jar --spring.profiles.active=mobile,prod --spring.config.additional-location=config\"
        $startInfo.UseShellExecute = $true
        $startInfo.WindowStyle = [System.Diagnostics.ProcessWindowStyle]::Minimized
        [System.Diagnostics.Process]::Start($startInfo) | Out-Null
        Start-Sleep -Seconds 5
        Write-Ok "Servidor reiniciado em segundo plano."
    } catch {
        Write-Warn "Não foi possível reiniciar o servidor automaticamente: $_"
        Write-Info "Inicie manualmente: scripts\iniciar-servidor.bat"
    }
} else {
    Write-Warn "bin\mobile-server.jar não encontrado. Inicie o servidor manualmente."
}
Write-Host ""

# ============================================================
# [6/6] Exibir resumo
# ============================================================
Write-Step "[6/6] Resumo da atualização..."

# Obter versão atual do schema
$env:PGPASSWORD = $SenhaPg
try {
    $VersaoAtualSchema = (& psql -h $DbHost -p $DbPort -U $UsuarioPg -d $Banco `
        -t -c "SELECT versao FROM schema_version ORDER BY id DESC LIMIT 1" 2>$null).Trim()
    if ($VersaoAtualSchema) { $VersaoAtual = $VersaoAtualSchema }
} catch {}
$env:PGPASSWORD = ""

Write-Host ""
Write-Host "============================================================" -ForegroundColor Green
Write-Host "  ✅ Atualização concluída!" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green
Write-Host ""
Write-Host "  Versão anterior : $VersaoAnterior" -ForegroundColor White
Write-Host "  Versão atual    : $VersaoAtual" -ForegroundColor White
Write-Host "  Migrações SQL   : $MigracoesAplicadas aplicada(s)" -ForegroundColor White
Write-Host "  Backup          : $BackupPath" -ForegroundColor White
Write-Host ""
Write-Host "  Execute a verificação de saúde: scripts\verificar-saude.ps1" -ForegroundColor Cyan
Write-Host ""
