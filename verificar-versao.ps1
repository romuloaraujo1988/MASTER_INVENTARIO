#!/usr/bin/env pwsh
# Script para verificar a versão do sistema

Write-Host ""
Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║     SIHCP - Sistema de Histórico e Coleta Patrimonial     ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Versão do sistema
Write-Host "📦 Versão: " -NoNewline -ForegroundColor White
Write-Host "2.0.0" -ForegroundColor Green

# Informações do Git
try {
    $commit = git rev-parse HEAD 2>$null
    $commitShort = git rev-parse --short HEAD 2>$null
    $branch = git rev-parse --abbrev-ref HEAD 2>$null
    $commitDate = git log -1 --format=%cd --date=format:"%d/%m/%Y %H:%M" 2>$null
    
    Write-Host "🔖 Commit: " -NoNewline -ForegroundColor White
    Write-Host "$commitShort" -ForegroundColor Yellow
    
    Write-Host "🌿 Branch: " -NoNewline -ForegroundColor White
    Write-Host "$branch" -ForegroundColor Magenta
    
    Write-Host "📅 Data do Commit: " -NoNewline -ForegroundColor White
    Write-Host "$commitDate" -ForegroundColor Gray
    
    # Verificar mudanças não commitadas
    $status = git status --porcelain 2>$null
    if ($status) {
        Write-Host "⚠️  Há mudanças não commitadas" -ForegroundColor DarkYellow
    }
    
    # Verificar commits à frente
    $ahead = git rev-list --count "@{u}.." 2>$null
    if ($ahead -and $ahead -gt 0) {
        Write-Host "↑  $ahead commit(s) à frente do remote" -ForegroundColor Blue
    }
    
} catch {
    Write-Host "⚠️  Git não disponível ou não é um repositório Git" -ForegroundColor DarkYellow
}

Write-Host ""

# Informações do ambiente
Write-Host "🖥️  Sistema: " -NoNewline -ForegroundColor White
Write-Host "$([System.Environment]::OSVersion.Platform) $([System.Environment]::OSVersion.Version)" -ForegroundColor Gray

# Java version
try {
    $javaVersion = java -version 2>&1 | Select-String "version" | ForEach-Object { $_ -replace '.*version "([^"]*)".*', '$1' }
    Write-Host "☕ Java: " -NoNewline -ForegroundColor White
    Write-Host "$javaVersion" -ForegroundColor Gray
} catch {
    Write-Host "☕ Java: " -NoNewline -ForegroundColor White
    Write-Host "Não detectado" -ForegroundColor DarkYellow
}

# Maven version
try {
    $mvnVersion = mvn -version 2>&1 | Select-String "Apache Maven" | ForEach-Object { $_ -replace 'Apache Maven ([^ ]*) .*', '$1' }
    Write-Host "🔨 Maven: " -NoNewline -ForegroundColor White
    Write-Host "$mvnVersion" -ForegroundColor Gray
} catch {
    Write-Host "🔨 Maven: " -NoNewline -ForegroundColor White
    Write-Host "Não detectado" -ForegroundColor DarkYellow
}

Write-Host ""
Write-Host "────────────────────────────────────────────────────────────" -ForegroundColor DarkGray
Write-Host "Para mais informações, consulte CHANGELOG.md" -ForegroundColor DarkGray
Write-Host ""
