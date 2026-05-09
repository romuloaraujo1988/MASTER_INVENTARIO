#Requires -Version 5.1
# ============================================================
# SIHCP - Script de Coleta de Diagnóstico (Windows)
# Coleta logs, versões, configuração anonimizada e relatório
# de saúde, compacta tudo em um ZIP para envio ao suporte.
# IMPORTANTE: Senhas NÃO são incluídas no diagnóstico.
# ============================================================

param()

Set-StrictMode -Version Latest
$ErrorActionPreference = "SilentlyContinue"

# Diretório base (raiz do pacote)
$BaseDir = Split-Path -Parent $PSScriptRoot
Set-Location $BaseDir

# Cores para output
function Write-Ok   { param($msg) Write-Host "[OK] $msg" -ForegroundColor Green }
function Write-Warn { param($msg) Write-Host "[AVISO] $msg" -ForegroundColor Yellow }
function Write-Err  { param($msg) Write-Host "[ERRO] $msg" -ForegroundColor Red }
function Write-Info { param($msg) Write-Host "      $msg" -ForegroundColor Gray }

Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  SIHCP - Coleta de Diagnóstico" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

# ============================================================
# Ler nome do campus para o nome do arquivo
# ============================================================
$Campus = "campus"
if (Test-Path "config\configuracao_banco.json") {
    try {
        $Config = Get-Content "config\configuracao_banco.json" | ConvertFrom-Json
        $CampusNome = $Config.campus.sigla
        if (-not $CampusNome) { $CampusNome = $Config.campus.nome }
        if ($CampusNome) {
            # Sanitizar nome para uso em arquivo
            $Campus = $CampusNome -replace '[^a-zA-Z0-9_-]', '_' -replace '__+', '_'
            $Campus = $Campus.ToLower().Trim('_')
        }
    } catch {}
}

$DataHora   = Get-Date -Format "yyyyMMdd-HHmm"
$TempDir    = "diagnostico-temp"
$ZipNome    = "diagnostico-${Campus}-${DataHora}.zip"

# Limpar diretório temporário anterior se existir
if (Test-Path $TempDir) {
    Remove-Item -Recurse -Force $TempDir
}
New-Item -ItemType Directory -Path $TempDir | Out-Null
New-Item -ItemType Directory -Path "$TempDir\logs" | Out-Null

Write-Info "Diretório temporário: $TempDir"
Write-Host ""

# ============================================================
# [1] Coletar últimas 1000 linhas dos logs
# ============================================================
Write-Host "[1/6] Coletando logs da API Mobile..."

if (Test-Path "logs") {
    $logFiles = Get-ChildItem -Path "logs" -Filter "*.log" -ErrorAction SilentlyContinue
    if ($logFiles.Count -gt 0) {
        foreach ($logFile in $logFiles) {
            try {
                # Coletar últimas 1000 linhas
                $linhas = Get-Content $logFile.FullName -Tail 1000 -ErrorAction SilentlyContinue
                if ($linhas) {
                    $destino = "$TempDir\logs\$($logFile.Name)"
                    $linhas | Set-Content -Path $destino -Encoding UTF8
                    Write-Ok "Log coletado: $($logFile.Name) ($($linhas.Count) linhas)"
                }
            } catch {
                Write-Warn "Não foi possível coletar $($logFile.Name): $_"
            }
        }
    } else {
        "Nenhum arquivo de log encontrado em logs/" | Set-Content "$TempDir\logs\sem-logs.txt"
        Write-Warn "Nenhum arquivo de log encontrado em logs/"
    }
} else {
    "Diretório logs/ não encontrado." | Set-Content "$TempDir\logs\sem-logs.txt"
    Write-Warn "Diretório logs/ não encontrado."
}

# ============================================================
# [2] Coletar versões de software
# ============================================================
Write-Host ""
Write-Host "[2/6] Coletando informações do ambiente..."

$ambienteLinhas = [System.Collections.Generic.List[string]]::new()
$ambienteLinhas.Add("=== AMBIENTE DE SOFTWARE ===")
$ambienteLinhas.Add("Data de coleta: $(Get-Date -Format 'dd/MM/yyyy HH:mm:ss')")
$ambienteLinhas.Add("")

# Java
try {
    $javaVer = & java -version 2>&1 | Out-String
    $ambienteLinhas.Add("--- Java ---")
    $ambienteLinhas.Add($javaVer.Trim())
    $ambienteLinhas.Add("")
} catch {
    $ambienteLinhas.Add("--- Java ---")
    $ambienteLinhas.Add("Não encontrado ou erro: $_")
    $ambienteLinhas.Add("")
}

# PostgreSQL
try {
    $psqlVer = & psql --version 2>&1 | Out-String
    $ambienteLinhas.Add("--- PostgreSQL Client ---")
    $ambienteLinhas.Add($psqlVer.Trim())
    $ambienteLinhas.Add("")
} catch {
    $ambienteLinhas.Add("--- PostgreSQL Client ---")
    $ambienteLinhas.Add("Não encontrado ou erro: $_")
    $ambienteLinhas.Add("")
}

# PowerShell
$ambienteLinhas.Add("--- PowerShell ---")
$ambienteLinhas.Add("Versão: $($PSVersionTable.PSVersion)")
$ambienteLinhas.Add("")

$ambienteLinhas | Set-Content -Path "$TempDir\ambiente.txt" -Encoding UTF8
Write-Ok "Informações do ambiente coletadas."

# ============================================================
# [3] Coletar configuração anonimizada (SEM senha)
# ============================================================
Write-Host ""
Write-Host "[3/6] Coletando configuração anonimizada..."

$configLinhas = [System.Collections.Generic.List[string]]::new()
$configLinhas.Add("=== CONFIGURAÇÃO DO SISTEMA (ANONIMIZADA) ===")
$configLinhas.Add("Data de coleta: $(Get-Date -Format 'dd/MM/yyyy HH:mm:ss')")
$configLinhas.Add("NOTA: Senhas foram removidas por segurança.")
$configLinhas.Add("")

if (Test-Path "config\configuracao_banco.json") {
    try {
        $Config = Get-Content "config\configuracao_banco.json" | ConvertFrom-Json

        $configLinhas.Add("--- Campus ---")
        $configLinhas.Add("Nome    : $($Config.campus.nome)")
        $configLinhas.Add("Sigla   : $($Config.campus.sigla)")
        $configLinhas.Add("Cidade  : $($Config.campus.cidade)")
        $configLinhas.Add("Estado  : $($Config.campus.estado)")
        $configLinhas.Add("")

        $configLinhas.Add("--- Banco de Dados ---")
        $configLinhas.Add("Host    : $($Config.postgresql.host)")
        $configLinhas.Add("Porta   : $($Config.postgresql.port)")
        $configLinhas.Add("Banco   : $($Config.postgresql.database)")
        $configLinhas.Add("Usuário : $($Config.postgresql.user)")
        $configLinhas.Add("Senha   : [REMOVIDA]")
        $configLinhas.Add("")

        $configLinhas.Add("--- API ---")
        $configLinhas.Add("Porta   : $($Config.api.porta)")
        $configLinhas.Add("Versão  : $($Config.api.versao)")
        $configLinhas.Add("")

        Write-Ok "Configuração anonimizada coletada (senha removida)."
    } catch {
        $configLinhas.Add("Erro ao ler configuracao_banco.json: $_")
        Write-Warn "Erro ao ler configuração: $_"
    }
} else {
    $configLinhas.Add("Arquivo config\configuracao_banco.json não encontrado.")
    Write-Warn "Arquivo de configuração não encontrado."
}

$configLinhas | Set-Content -Path "$TempDir\configuracao.txt" -Encoding UTF8

# ============================================================
# [4] Executar verificar-saude e copiar relatório
# ============================================================
Write-Host ""
Write-Host "[4/6] Executando verificação de saúde..."

$saude_script = "$PSScriptRoot\verificar-saude.ps1"
if (Test-Path $saude_script) {
    try {
        # Executar verificar-saude com senha vazia (modo silencioso)
        $SenhaAdminDiag = Read-Host "Senha do admin para verificação de saúde (Enter para pular)"
        if (-not [string]::IsNullOrWhiteSpace($SenhaAdminDiag)) {
            & powershell -NoProfile -File $saude_script -SenhaAdmin $SenhaAdminDiag 2>&1 | Out-Null
        }
        # Copiar o relatório mais recente gerado
        $relatorioRecente = Get-ChildItem -Path "." -Filter "relatorio-saude-*.txt" |
            Sort-Object LastWriteTime -Descending | Select-Object -First 1
        if ($relatorioRecente) {
            Copy-Item $relatorioRecente.FullName "$TempDir\relatorio-saude.txt"
            Write-Ok "Relatório de saúde copiado: $($relatorioRecente.Name)"
        } else {
            "Relatório de saúde não disponível." | Set-Content "$TempDir\relatorio-saude.txt"
            Write-Warn "Nenhum relatório de saúde encontrado."
        }
    } catch {
        "Erro ao executar verificar-saude.ps1: $_" | Set-Content "$TempDir\relatorio-saude.txt"
        Write-Warn "Erro ao executar verificação de saúde: $_"
    }
} else {
    "Script verificar-saude.ps1 não encontrado." | Set-Content "$TempDir\relatorio-saude.txt"
    Write-Warn "Script verificar-saude.ps1 não encontrado."
}

# ============================================================
# [5] Coletar informações do sistema operacional
# ============================================================
Write-Host ""
Write-Host "[5/6] Coletando informações do sistema operacional..."

try {
    $sistemaLinhas = [System.Collections.Generic.List[string]]::new()
    $sistemaLinhas.Add("=== INFORMAÇÕES DO SISTEMA OPERACIONAL ===")
    $sistemaLinhas.Add("Data de coleta: $(Get-Date -Format 'dd/MM/yyyy HH:mm:ss')")
    $sistemaLinhas.Add("")

    # systeminfo
    $sysinfo = & systeminfo 2>&1 | Out-String
    $sistemaLinhas.Add($sysinfo)

    $sistemaLinhas | Set-Content -Path "$TempDir\sistema.txt" -Encoding UTF8
    Write-Ok "Informações do sistema coletadas."
} catch {
    "Erro ao coletar systeminfo: $_" | Set-Content "$TempDir\sistema.txt"
    Write-Warn "Erro ao coletar informações do sistema: $_"
}

# ============================================================
# [6] Compactar e limpar
# ============================================================
Write-Host ""
Write-Host "[6/6] Compactando diagnóstico..."

try {
    Compress-Archive -Path "$TempDir\*" -DestinationPath $ZipNome -Force
    $tamanhoMB = [math]::Round((Get-Item $ZipNome).Length / 1MB, 2)
    Write-Ok "Arquivo ZIP gerado: $ZipNome ($tamanhoMB MB)"
} catch {
    Write-Err "Falha ao compactar diagnóstico: $_"
    Write-Info "Os arquivos estão disponíveis em: $TempDir\"
    exit 1
}

# Remover diretório temporário
try {
    Remove-Item -Recurse -Force $TempDir
    Write-Info "Diretório temporário removido."
} catch {
    Write-Warn "Não foi possível remover o diretório temporário: $TempDir"
}

# Resultado final
Write-Host ""
Write-Host "============================================================" -ForegroundColor Green
Write-Host "  ✅ Diagnóstico coletado com sucesso!" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green
Write-Host ""
Write-Host "  Arquivo: $(Resolve-Path $ZipNome)" -ForegroundColor White
Write-Host ""
Write-Host "  Envie este arquivo para o suporte:" -ForegroundColor Cyan
Write-Host "  E-mail    : suporte@sihcp.ifmt.edu.br" -ForegroundColor Cyan
Write-Host "  Repositório: https://github.com/ifmt/sihcp" -ForegroundColor Cyan
Write-Host ""
