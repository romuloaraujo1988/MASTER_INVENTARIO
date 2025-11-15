# Limpar coleta do patrimônio 3250 para permitir novo teste

Write-Host "🗑️ Limpando coleta do patrimônio 3250..." -ForegroundColor Yellow

# Conectar ao PostgreSQL e deletar a coleta
$env:PGPASSWORD = "inventario"
$query = "DELETE FROM coleta WHERE id_patrimonio = 10 AND id_inventario = 2; SELECT 'Coleta removida com sucesso' as resultado;"

try {
    $result = & psql -h localhost -U inventario -d sispatrimonio -c $query 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Coleta do patrimônio 3250 removida com sucesso!" -ForegroundColor Green
        Write-Host "   Agora você pode testar novamente no app mobile" -ForegroundColor Cyan
    } else {
        Write-Host "❌ Erro ao remover coleta" -ForegroundColor Red
        Write-Host $result
    }
} catch {
    Write-Host "❌ Erro: $($_.Exception.Message)" -ForegroundColor Red
}
