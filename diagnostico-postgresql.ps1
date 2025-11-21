# Script de Diagnóstico PostgreSQL
# Verifica configuração e conectividade

param(
    [string]$Host = "localhost",
    [int]$Port = 5432,
    [string]$Database = "sispatrimonio",
    [string]$User = "postgres"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Diagnóstico PostgreSQL" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Configuração:" -ForegroundColor Yellow
Write-Host "  Host: $Host"
Write-Host "  Porta: $Port"
Write-Host "  Banco: $Database"
Write-Host "  Usuário: $User"
Write-Host ""

# 1. Verificar se a porta está aberta
Write-Host "1. Verificando se a porta está aberta..." -ForegroundColor Yellow
try {
    $tcpClient = New-Object System.Net.Sockets.TcpClient
    $tcpClient.Connect($Host, $Port)
    $tcpClient.Close()
    Write-Host "   ✓ Porta $Port está aberta e acessível" -ForegroundColor Green
    $portaAberta = $true
} catch {
    Write-Host "   ❌ Porta $Port não está acessível" -ForegroundColor Red
    Write-Host "   Erro: $($_.Exception.Message)" -ForegroundColor Red
    $portaAberta = $false
}
Write-Host ""

# 2. Verificar se psql está disponível
Write-Host "2. Verificando psql..." -ForegroundColor Yellow
$psqlPath = Get-Command psql -ErrorAction SilentlyContinue
if ($psqlPath) {
    Write-Host "   ✓ psql encontrado: $($psqlPath.Source)" -ForegroundColor Green
    $psqlDisponivel = $true
} else {
    Write-Host "   ⚠ psql não encontrado no PATH" -ForegroundColor Yellow
    Write-Host "   Tentando localizar PostgreSQL..." -ForegroundColor Yellow
    
    # Procurar PostgreSQL em locais comuns
    $pgPaths = @(
        "C:\Program Files\PostgreSQL\*\bin\psql.exe",
        "C:\Program Files (x86)\PostgreSQL\*\bin\psql.exe"
    )
    
    $found = $false
    foreach ($path in $pgPaths) {
        $psqlExe = Get-ChildItem -Path $path -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($psqlExe) {
            Write-Host "   ✓ PostgreSQL encontrado: $($psqlExe.DirectoryName)" -ForegroundColor Green
            $env:PATH += ";$($psqlExe.DirectoryName)"
            $psqlDisponivel = $true
            $found = $true
            break
        }
    }
    
    if (-not $found) {
        Write-Host "   ❌ PostgreSQL não encontrado" -ForegroundColor Red
        $psqlDisponivel = $false
    }
}
Write-Host ""

# 3. Verificar serviço PostgreSQL (Windows)
Write-Host "3. Verificando serviço PostgreSQL..." -ForegroundColor Yellow
$pgServices = Get-Service -Name "postgresql*" -ErrorAction SilentlyContinue
if ($pgServices) {
    foreach ($service in $pgServices) {
        $status = if ($service.Status -eq "Running") { "✓" } else { "❌" }
        $color = if ($service.Status -eq "Running") { "Green" } else { "Red" }
        Write-Host "   $status $($service.Name): $($service.Status)" -ForegroundColor $color
    }
} else {
    Write-Host "   ⚠ Nenhum serviço PostgreSQL encontrado" -ForegroundColor Yellow
}
Write-Host ""

# 4. Testar conexão (se psql disponível)
if ($psqlDisponivel -and $portaAberta) {
    Write-Host "4. Testando conexão com o banco..." -ForegroundColor Yellow
    Write-Host "   (Digite a senha quando solicitado)" -ForegroundColor Gray
    
    $env:PGPASSWORD = Read-Host "   Senha" -AsSecureString
    $env:PGPASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
        [Runtime.InteropServices.Marshal]::SecureStringToBSTR($env:PGPASSWORD)
    )
    
    $result = & psql -h $Host -p $Port -U $User -d $Database -c "SELECT version();" 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   ✓ Conexão bem-sucedida!" -ForegroundColor Green
        Write-Host ""
        Write-Host "   Versão do PostgreSQL:" -ForegroundColor Cyan
        Write-Host "   $result" -ForegroundColor Gray
    } else {
        Write-Host "   ❌ Falha na conexão" -ForegroundColor Red
        Write-Host "   Erro: $result" -ForegroundColor Red
    }
    
    Remove-Item Env:\PGPASSWORD -ErrorAction SilentlyContinue
}
Write-Host ""

# 5. Verificar firewall
Write-Host "5. Verificando regras de firewall..." -ForegroundColor Yellow
$firewallRules = Get-NetFirewallRule -DisplayName "*PostgreSQL*" -ErrorAction SilentlyContinue
if ($firewallRules) {
    foreach ($rule in $firewallRules) {
        $status = if ($rule.Enabled) { "✓" } else { "❌" }
        $color = if ($rule.Enabled) { "Green" } else { "Red" }
        Write-Host "   $status $($rule.DisplayName): $($rule.Direction) - $($rule.Action)" -ForegroundColor $color
    }
} else {
    Write-Host "   ⚠ Nenhuma regra de firewall específica para PostgreSQL" -ForegroundColor Yellow
}
Write-Host ""

# 6. Verificar portas em uso
Write-Host "6. Verificando portas em uso..." -ForegroundColor Yellow
$connections = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue
if ($connections) {
    Write-Host "   ✓ Porta $Port está em uso:" -ForegroundColor Green
    foreach ($conn in $connections) {
        Write-Host "   - Estado: $($conn.State), PID: $($conn.OwningProcess)" -ForegroundColor Gray
    }
} else {
    Write-Host "   ❌ Porta $Port não está em uso" -ForegroundColor Red
    Write-Host "   PostgreSQL pode não estar rodando ou usando outra porta" -ForegroundColor Yellow
}
Write-Host ""

# Resumo
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  RESUMO DO DIAGNÓSTICO" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

if ($portaAberta -and $psqlDisponivel) {
    Write-Host "✓ Sistema pronto para conectar" -ForegroundColor Green
    Write-Host ""
    Write-Host "Configure no sistema:" -ForegroundColor Yellow
    Write-Host "  Servidor: $Host"
    Write-Host "  Porta: $Port"
    Write-Host "  Banco: $Database"
    Write-Host "  Usuário: $User"
} else {
    Write-Host "❌ Problemas detectados" -ForegroundColor Red
    Write-Host ""
    Write-Host "Ações recomendadas:" -ForegroundColor Yellow
    
    if (-not $portaAberta) {
        Write-Host "  • Verificar se PostgreSQL está rodando"
        Write-Host "  • Verificar firewall"
        Write-Host "  • Verificar se a porta está correta"
    }
    
    if (-not $psqlDisponivel) {
        Write-Host "  • Instalar PostgreSQL"
        Write-Host "  • Adicionar PostgreSQL ao PATH"
    }
    
    Write-Host ""
    Write-Host "Consulte: GUIA_CONFIGURACAO_POSTGRESQL_REMOTO.md" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "Pressione qualquer tecla para sair..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
