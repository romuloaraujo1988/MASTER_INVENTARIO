# ============================================================
# POPULAR ITENS COMPOSTOS - Sistema de Inventario
# ============================================================

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "POPULAR ITENS COMPOSTOS - Sistema de Inventario" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Este script ira:" -ForegroundColor Yellow
Write-Host "  1. Recriar as tabelas de itens compostos"
Write-Host "  2. Popular com dados baseados nos patrimonios existentes"
Write-Host ""
Write-Host "ATENCAO: Certifique-se de que o PostgreSQL esta rodando!" -ForegroundColor Red
Write-Host ""

$confirm = Read-Host "Deseja continuar? (S/N)"
if ($confirm -ne "S" -and $confirm -ne "s") {
    Write-Host "Operacao cancelada." -ForegroundColor Yellow
    exit
}

Write-Host ""
Write-Host "Executando script SQL..." -ForegroundColor Green
Write-Host ""

# Configuracoes do banco
$env:PGPASSWORD = "inventario"
$host_db = "localhost"
$user_db = "inventario"
$database = "sispatrimonio"
$script_path = "sql/popular_itens_compostos_v2.sql"

try {
    # Executar o script
    psql -h $host_db -U $user_db -d $database -f $script_path
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "============================================================" -ForegroundColor Green
        Write-Host "SUCESSO! Itens compostos populados com sucesso." -ForegroundColor Green
        Write-Host "============================================================" -ForegroundColor Green
    } else {
        Write-Host ""
        Write-Host "============================================================" -ForegroundColor Red
        Write-Host "ERRO! Verifique as mensagens acima." -ForegroundColor Red
        Write-Host "============================================================" -ForegroundColor Red
    }
} catch {
    Write-Host ""
    Write-Host "ERRO: $_" -ForegroundColor Red
}

Write-Host ""
Read-Host "Pressione ENTER para sair"
