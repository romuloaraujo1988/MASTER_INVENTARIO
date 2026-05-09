# Script para adicionar Maven ao PATH do Windows
# Execute como Administrador

Write-Host "═══════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  Configuração do Maven no PATH" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# Caminho do Maven
$mavenPath = "C:\MAVEN\mvn\bin"

# Verificar se o Maven existe
if (Test-Path "$mavenPath\mvn.cmd") {
    Write-Host "✓ Maven encontrado em: $mavenPath" -ForegroundColor Green
} else {
    Write-Host "✗ Maven não encontrado em: $mavenPath" -ForegroundColor Red
    Write-Host "  Verifique o caminho e tente novamente." -ForegroundColor Yellow
    exit 1
}

# Obter PATH atual do sistema
$currentPath = [Environment]::GetEnvironmentVariable("Path", "Machine")

# Verificar se Maven já está no PATH
if ($currentPath -like "*$mavenPath*") {
    Write-Host "✓ Maven já está no PATH do sistema" -ForegroundColor Yellow
} else {
    Write-Host "→ Adicionando Maven ao PATH do sistema..." -ForegroundColor Cyan
    
    try {
        # Adicionar ao PATH do sistema (requer admin)
        $newPath = "$currentPath;$mavenPath"
        [Environment]::SetEnvironmentVariable("Path", $newPath, "Machine")
        
        Write-Host "✓ Maven adicionado ao PATH do sistema com sucesso!" -ForegroundColor Green
        Write-Host ""
        Write-Host "IMPORTANTE:" -ForegroundColor Yellow
        Write-Host "  1. Feche e reabra o PowerShell/CMD" -ForegroundColor White
        Write-Host "  2. Ou execute: " -ForegroundColor White -NoNewline
        Write-Host "`$env:Path = [System.Environment]::GetEnvironmentVariable('Path','Machine')" -ForegroundColor Cyan
        Write-Host ""
    } catch {
        Write-Host "✗ Erro ao adicionar Maven ao PATH" -ForegroundColor Red
        Write-Host "  Execute este script como Administrador" -ForegroundColor Yellow
        Write-Host "  Erro: $_" -ForegroundColor Red
        exit 1
    }
}

# Adicionar ao PATH da sessão atual
$env:Path += ";$mavenPath"
Write-Host "✓ Maven adicionado ao PATH da sessão atual" -ForegroundColor Green

# Testar Maven
Write-Host ""
Write-Host "→ Testando Maven..." -ForegroundColor Cyan
try {
    $mvnVersion = & "$mavenPath\mvn.cmd" -version 2>&1 | Select-Object -First 1
    Write-Host "✓ Maven funcionando: $mvnVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ Erro ao testar Maven" -ForegroundColor Red
}

Write-Host ""
Write-Host "═══════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  Configuração concluída!" -ForegroundColor Green
Write-Host "═══════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "Comandos disponíveis:" -ForegroundColor Cyan
Write-Host "  mvn clean compile          - Compilar projeto" -ForegroundColor White
Write-Host "  mvn clean package          - Gerar JAR" -ForegroundColor White
Write-Host "  mvn clean install          - Instalar no repositório local" -ForegroundColor White
Write-Host "  mvn spring-boot:run        - Executar aplicação" -ForegroundColor White
Write-Host ""
