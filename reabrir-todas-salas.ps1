# Script para reabrir TODAS as salas finalizadas no inventário ativo
# Use com CUIDADO - isso reabrirá todas as salas para coleta

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  REABRIR TODAS AS SALAS FINALIZADAS" -ForegroundColor Cyan
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

# Verificar quantas salas estão finalizadas
$sqlVerificar = @"
SELECT COUNT(*) as total
FROM TABELA_SALA_INVENTARIO si
INNER JOIN TABELA_INVENTARIO i ON si.ID_INVENTARIO = i.ID_INVENTARIO
WHERE i.STATUS = 'EM_ANDAMENTO'
AND si.COLETA_FINALIZADA = TRUE;
"@

Write-Host "Verificando salas finalizadas..." -ForegroundColor Yellow
$result = psql -h $dbHost -p $dbPort -U $dbUser -d $dbName -t -c $sqlVerificar

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERRO ao conectar ao banco de dados!" -ForegroundColor Red
    Write-Host "Verifique as credenciais e tente novamente." -ForegroundColor Red
    exit 1
}

$totalFinalizadas = $result.Trim()
Write-Host "Total de salas finalizadas: $totalFinalizadas" -ForegroundColor Cyan
Write-Host ""

if ($totalFinalizadas -eq "0") {
    Write-Host "Nenhuma sala finalizada encontrada." -ForegroundColor Green
    Write-Host "Todas as salas já estão abertas para coleta." -ForegroundColor Green
    exit 0
}

# Confirmar ação
Write-Host "ATENÇÃO: Esta ação irá reabrir $totalFinalizadas sala(s)!" -ForegroundColor Yellow
Write-Host "Isso permitirá que novas coletas sejam realizadas nessas salas." -ForegroundColor Yellow
Write-Host ""
$confirmacao = Read-Host "Deseja continuar? (S/N)"

if ($confirmacao -ne "S" -and $confirmacao -ne "s") {
    Write-Host "Operação cancelada pelo usuário." -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "Reabrindo salas..." -ForegroundColor Yellow

# SQL para reabrir todas as salas
$sqlReabrir = @"
UPDATE TABELA_SALA_INVENTARIO si
SET 
    COLETA_FINALIZADA = FALSE,
    DATA_FINALIZACAO = NULL,
    ID_PARTICIPANTE_FINALIZOU = NULL,
    OBSERVACOES_FINALIZACAO = NULL
FROM TABELA_INVENTARIO i
WHERE si.ID_INVENTARIO = i.ID_INVENTARIO
AND i.STATUS = 'EM_ANDAMENTO'
AND si.COLETA_FINALIZADA = TRUE;
"@

psql -h $dbHost -p $dbPort -U $dbUser -d $dbName -c $sqlReabrir

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✓ Sucesso!" -ForegroundColor Green
    Write-Host "$totalFinalizadas sala(s) foram reabertas." -ForegroundColor Green
    Write-Host ""
    Write-Host "As salas agora aparecerão no aplicativo mobile e desktop." -ForegroundColor Cyan
} else {
    Write-Host ""
    Write-Host "✗ Erro ao reabrir salas!" -ForegroundColor Red
    Write-Host "Verifique os logs acima para mais detalhes." -ForegroundColor Red
}

Write-Host ""
Write-Host "Pressione qualquer tecla para sair..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
