# Testar busca de coletas para sala CAE
Write-Host "=== Testando busca de coletas para sala CAE ===" -ForegroundColor Cyan

# Query 1: Buscar por localizacao_encontrada
Write-Host "`nQuery 1: Buscar por localizacao_encontrada LIKE '%CAE%'" -ForegroundColor Yellow
sqlite3 data/inventario.db "SELECT COUNT(*) as total FROM local_coleta WHERE localizacao_encontrada LIKE '%CAE%';"

# Query 2: Buscar por localizacao_atual
Write-Host "`nQuery 2: Buscar por localizacao_atual LIKE '%CAE%'" -ForegroundColor Yellow
sqlite3 data/inventario.db "SELECT COUNT(*) as total FROM local_coleta WHERE localizacao_atual LIKE '%CAE%';"

# Query 3: Buscar detalhes das coletas
Write-Host "`nQuery 3: Detalhes das coletas encontradas" -ForegroundColor Yellow
sqlite3 data/inventario.db "SELECT id, numero_patrimonio, data_coleta, localizacao_encontrada FROM local_coleta WHERE localizacao_encontrada LIKE '%CAE%' LIMIT 5;"

# Query 4: Verificar todas as localizações únicas
Write-Host "`nQuery 4: Localizações únicas na tabela" -ForegroundColor Yellow
sqlite3 data/inventario.db "SELECT DISTINCT localizacao_encontrada FROM local_coleta LIMIT 10;"

# Query 5: Verificar estrutura da tabela
Write-Host "`nQuery 5: Estrutura da tabela local_coleta" -ForegroundColor Yellow
sqlite3 data/inventario.db "PRAGMA table_info(local_coleta);"

Write-Host "`n=== Teste concluído ===" -ForegroundColor Green
