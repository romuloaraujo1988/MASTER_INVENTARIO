# Script para testar formato de data no endpoint de coletas
# Verifica se o servidor está retornando data como String ISO 8601

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Teste de Formato de Data - Coletas" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8081"
$endpoint = "$baseUrl/api/mobile/coletas/all"

Write-Host "1. Testando endpoint: $endpoint" -ForegroundColor Yellow
Write-Host ""

try {
    $response = Invoke-RestMethod -Uri $endpoint -Method Get -ContentType "application/json"
    
    if ($response.success) {
        Write-Host "✓ Endpoint respondeu com sucesso" -ForegroundColor Green
        
        $coletas = $response.data
        $totalColetas = $coletas.Count
        
        Write-Host "✓ Total de coletas: $totalColetas" -ForegroundColor Green
        Write-Host ""
        
        if ($totalColetas -gt 0) {
            Write-Host "2. Analisando formato da primeira coleta:" -ForegroundColor Yellow
            Write-Host ""
            
            $primeiraColeta = $coletas[0]
            
            # Verificar campos principais
            Write-Host "  ID: $($primeiraColeta.id)" -ForegroundColor White
            Write-Host "  Número Patrimônio: $($primeiraColeta.numeroPatrimonio)" -ForegroundColor White
            Write-Host "  Data Coleta: $($primeiraColeta.dataColeta)" -ForegroundColor White
            Write-Host "  Tipo da Data: $($primeiraColeta.dataColeta.GetType().Name)" -ForegroundColor White
            Write-Host ""
            
            # Verificar se é String
            if ($primeiraColeta.dataColeta -is [string]) {
                Write-Host "✓ Data está como String (correto!)" -ForegroundColor Green
                
                # Verificar formato ISO 8601
                if ($primeiraColeta.dataColeta -match '^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}') {
                    Write-Host "✓ Formato ISO 8601 detectado (correto!)" -ForegroundColor Green
                    Write-Host "  Formato: YYYY-MM-DDTHH:MM:SS" -ForegroundColor Gray
                } else {
                    Write-Host "✗ Formato não é ISO 8601" -ForegroundColor Red
                    Write-Host "  Esperado: 2025-11-24T21:30:45" -ForegroundColor Gray
                    Write-Host "  Recebido: $($primeiraColeta.dataColeta)" -ForegroundColor Gray
                }
            } else {
                Write-Host "✗ Data NÃO está como String!" -ForegroundColor Red
                Write-Host "  Tipo recebido: $($primeiraColeta.dataColeta.GetType().Name)" -ForegroundColor Gray
                Write-Host "  Esperado: String" -ForegroundColor Gray
            }
            
            Write-Host ""
            Write-Host "3. Estrutura completa da coleta:" -ForegroundColor Yellow
            Write-Host ""
            $primeiraColeta | ConvertTo-Json -Depth 3 | Write-Host -ForegroundColor Gray
            
        } else {
            Write-Host "⚠ Nenhuma coleta encontrada no sistema" -ForegroundColor Yellow
            Write-Host "  Registre uma coleta primeiro para testar o formato" -ForegroundColor Gray
        }
        
    } else {
        Write-Host "✗ Endpoint retornou erro" -ForegroundColor Red
        Write-Host "  Mensagem: $($response.message)" -ForegroundColor Gray
    }
    
} catch {
    Write-Host "✗ Erro ao conectar com o servidor" -ForegroundColor Red
    Write-Host "  Erro: $($_.Exception.Message)" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Verifique se:" -ForegroundColor Yellow
    Write-Host "  1. O servidor está rodando" -ForegroundColor Gray
    Write-Host "  2. A porta 8081 está correta" -ForegroundColor Gray
    Write-Host "  3. Não há firewall bloqueando" -ForegroundColor Gray
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Teste Concluído" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
