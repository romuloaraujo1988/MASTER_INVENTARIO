# Script para ativar TODAS as salas no PostgreSQL
# Isso fará com que todas as 108 salas sejam sincronizadas para o SQLite

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  ATIVAR TODAS AS SALAS NO POSTGRESQL" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configurações do banco
$dbHost = "localhost"
$dbPort = "5432"
$dbName = "sispatrimonio"
$dbUser = "inventario"

Write-Host "Conectando ao banco de dados..." -ForegroundColor Yellow
Write-Host "Host: $dbHost" -ForegroundColor Gray
Write-Host "Database: $dbName" -ForegroundColor Gray
Write-Host ""

# Verificar quantas salas estão inativas
$sqlVerificar = @"
SELECT 
    COUNT(*) FILTER (WHERE ATIVO = TRUE) as ativas,
    COUNT(*) FILTER (WHERE ATIVO = FALSE) as inativas,
    COUNT(*) as total
FROM TABELA_SALA;
"@

Write-Host "Verificando status das salas..." -ForegroundColor Yellow
$result = psql -h $dbHost -p $dbPort -U $dbUser -d $dbName -t -A -F'|' -c $sqlVerificar

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERRO ao conectar ao banco de dados!" -ForegroundColor Red
    Write-Host "Verifique as credenciais e tente novamente." -ForegroundColor Red
    exit 1
}

$valores = $result.Trim() -split '\|'
$ativas = $valores[0]
$inativas = $valores[1]
$total = $valores[2]

Write-Host "Status atual:" -ForegroundColor Cyan
Write-Host "  Salas ATIVAS: $ativas" -ForegroundColor Green
Write-Host "  Salas INATIVAS: $inativas" -ForegroundColor Red
Write-Host "  Total: $total" -ForegroundColor Cyan
Write-Host ""

if ($inativas -eq "0") {
    Write-Host "Todas as salas já estão ativas!" -ForegroundColor Green
    Write-Host "Nenhuma ação necessária." -ForegroundColor Green
    Write-Host ""
    Write-Host "Pressione qualquer tecla para sair..."
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit 0
}

# Confirmar ação
Write-Host "ATENÇÃO: Esta ação irá ativar $inativas sala(s) inativa(s)!" -ForegroundColor Yellow
Write-Host "Após ativar, execute a sincronização para atualizar o SQLite." -ForegroundColor Yellow
Write-Host ""
$confirmacao = Read-Host "Deseja continuar? (S/N)"

if ($confirmacao -ne "S" -and $confirmacao -ne "s") {
    Write-Host "Operação cancelada pelo usuário." -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "Ativando salas..." -ForegroundColor Yellow

# SQL para ativar todas as salas
$sqlAtivar = @"
UPDATE TABELA_SALA
SET ATIVO = TRUE
WHERE ATIVO = FALSE;
"@

psql -h $dbHost -p $dbPort -U $dbUser -d $dbName -c $sqlAtivar

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✓ Sucesso!" -ForegroundColor Green
    Write-Host "$inativas sala(s) foram ativadas." -ForegroundColor Green
    Write-Host ""
    Write-Host "PRÓXIMO PASSO:" -ForegroundColor Cyan
    Write-Host "Execute a sincronização para atualizar o SQLite:" -ForegroundColor Cyan
    Write-Host "  .\sincronizar-sqlite-offline.ps1" -ForegroundColor Yellow
} else {
    Write-Host ""
    Write-Host "✗ Erro ao ativar salas!" -ForegroundColor Red
    Write-Host "Verifique os logs acima para mais detalhes." -ForegroundColor Red
}

Write-Host ""
Write-Host "Pressione qualquer tecla para sair..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
