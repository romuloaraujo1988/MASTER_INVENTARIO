# ========================================
# Script para exportar dados de itens compostos
# ========================================
# Execute no servidor de ORIGEM

param(
    [string]$Host = "localhost",
    [string]$Database = "sispatrimonio",
    [string]$User = "inventario",
    [string]$OutputFile = "itens_compostos_export.sql"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Exportando dados de itens compostos" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Verificar se psql está disponível
$psqlPath = Get-Command psql -ErrorAction SilentlyContinue
if (-not $psqlPath) {
    Write-Host "ERRO: psql não encontrado no PATH" -ForegroundColor Red
    Write-Host "Instale o PostgreSQL ou adicione ao PATH" -ForegroundColor Yellow
    exit 1
}

Write-Host "Conectando ao banco: $Database@$Host" -ForegroundColor Yellow

# Criar arquivo de saída
$header = @"
-- ========================================
-- DADOS EXPORTADOS DE ITENS COMPOSTOS
-- ========================================
-- Gerado em: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
-- Banco origem: $Database@$Host

-- Desabilitar constraints temporariamente
SET session_replication_role = replica;

-- ========================================
-- TABELA_ITEM_COMPOSTO
-- ========================================

"@

$header | Out-File -FilePath $OutputFile -Encoding UTF8

# Exportar tabela_item_composto
Write-Host "Exportando tabela_item_composto..." -ForegroundColor Yellow

$query = @"
SELECT 'INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES' || chr(10) ||
'(' || id || ', ' || id_patrimonio_principal || ', ''' || 
tipo_componente || ''', ''' || 
REPLACE(descricao_componente, '''', '''''') || ''', ' || 
quantidade_esperada || ', ' || obrigatorio || ', ' ||
COALESCE('''' || REPLACE(observacao, '''', '''''') || '''', 'NULL') || ', ''' ||
data_cadastro || ''');'
FROM tabela_item_composto 
ORDER BY id;
"@

$env:PGPASSWORD = Read-Host "Digite a senha do banco" -AsSecureString | ConvertFrom-SecureString -AsPlainText
psql -h $Host -U $User -d $Database -t -A -c $query >> $OutputFile

# Exportar tabela_coleta_componente
Write-Host "Exportando tabela_coleta_componente..." -ForegroundColor Yellow

$footer = @"

-- ========================================
-- TABELA_COLETA_COMPONENTE
-- ========================================

"@
$footer | Out-File -FilePath $OutputFile -Append -Encoding UTF8

$query2 = @"
SELECT 'INSERT INTO tabela_coleta_componente (id, id_item_composto, id_inventario, id_coletor, quantidade_encontrada, status_componente, observacao_coleta, data_coleta) VALUES' || chr(10) ||
'(' || id || ', ' || id_item_composto || ', ' || id_inventario || ', ' || 
id_coletor || ', ' || quantidade_encontrada || ', ''' || 
status_componente || ''', ' ||
COALESCE('''' || REPLACE(observacao_coleta, '''', '''''') || '''', 'NULL') || ', ''' ||
data_coleta || ''');'
FROM tabela_coleta_componente 
ORDER BY id;
"@

psql -h $Host -U $User -d $Database -t -A -c $query2 >> $OutputFile

# Adicionar comandos finais
$final = @"

-- ========================================
-- ATUALIZAR SEQUÊNCIAS
-- ========================================
SELECT setval('tabela_item_composto_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tabela_item_composto));
SELECT setval('tabela_coleta_componente_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tabela_coleta_componente));

-- Reabilitar constraints
SET session_replication_role = DEFAULT;

-- ========================================
-- VERIFICAÇÃO
-- ========================================
SELECT 'Itens compostos:' as tabela, COUNT(*) as total FROM tabela_item_composto;
SELECT 'Coletas componentes:' as tabela, COUNT(*) as total FROM tabela_coleta_componente;
"@

$final | Out-File -FilePath $OutputFile -Append -Encoding UTF8

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "Exportação concluída!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host "Arquivo gerado: $OutputFile" -ForegroundColor Cyan
Write-Host ""
Write-Host "Para importar no servidor destino:" -ForegroundColor Yellow
Write-Host "  psql -h <host> -U <user> -d <database> -f $OutputFile" -ForegroundColor White
