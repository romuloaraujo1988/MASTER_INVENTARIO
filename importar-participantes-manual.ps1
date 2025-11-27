# Script para importar participantes manualmente do PostgreSQL para SQLite
# Data: 26/11/2025

Write-Host "=== Importação Manual de Participantes ===" -ForegroundColor Cyan
Write-Host ""

# 1. Verificar participantes no PostgreSQL
Write-Host "1. Verificando participantes no PostgreSQL..." -ForegroundColor Yellow
$pgParticipantes = psql -h localhost -U postgres -d sispatrimonio -t -c "SELECT COUNT(*) FROM tabela_participante_inventario WHERE ativo = TRUE;"
Write-Host "   Participantes ativos no PostgreSQL: $pgParticipantes" -ForegroundColor White

# 2. Verificar participantes no SQLite
Write-Host ""
Write-Host "2. Verificando participantes no SQLite..." -ForegroundColor Yellow
$sqliteParticipantes = sqlite3 data/inventario.db "SELECT COUNT(*) FROM local_participante_inventario;"
Write-Host "   Participantes no SQLite: $sqliteParticipantes" -ForegroundColor White

# 3. Exportar participantes do PostgreSQL
Write-Host ""
Write-Host "3. Exportando participantes do PostgreSQL..." -ForegroundColor Yellow
$query = @"
SELECT 
    id_participante,
    id_inventario,
    id_usuario,
    papel,
    CASE WHEN ativo THEN 1 ELSE 0 END as ativo,
    to_char(data_inclusao, 'YYYY-MM-DD HH24:MI:SS') as data_inclusao
FROM tabela_participante_inventario 
WHERE ativo = TRUE
ORDER BY id_participante;
"@

$participantes = psql -h localhost -U postgres -d sispatrimonio -t -A -F"|" -c $query

if ($participantes) {
    Write-Host "   ✅ Participantes exportados com sucesso" -ForegroundColor Green
    
    # 4. Importar para SQLite
    Write-Host ""
    Write-Host "4. Importando para SQLite..." -ForegroundColor Yellow
    
    $count = 0
    foreach ($linha in $participantes) {
        if ($linha.Trim()) {
            $campos = $linha.Split("|")
            
            $id_participante = $campos[0]
            $id_inventario = $campos[1]
            $id_usuario = $campos[2]
            $papel = $campos[3]
            $ativo = $campos[4]
            $data_inclusao = $campos[5]
            
            $insertSql = @"
INSERT OR REPLACE INTO local_participante_inventario 
(id_participante, id_inventario, id_usuario, papel, ativo, data_inclusao)
VALUES ($id_participante, $id_inventario, $id_usuario, '$papel', $ativo, '$data_inclusao');
"@
            
            sqlite3 data/inventario.db $insertSql
            $count++
            
            Write-Host "   ✅ Participante $id_participante importado (Inventário: $id_inventario, Usuário: $id_usuario)" -ForegroundColor Green
        }
    }
    
    Write-Host ""
    Write-Host "   Total importado: $count participantes" -ForegroundColor Cyan
    
} else {
    Write-Host "   ❌ Nenhum participante encontrado" -ForegroundColor Red
}

# 5. Verificar resultado
Write-Host ""
Write-Host "5. Verificando resultado..." -ForegroundColor Yellow
$sqliteParticipantesDepois = sqlite3 data/inventario.db "SELECT COUNT(*) FROM local_participante_inventario;"
Write-Host "   Participantes no SQLite após importação: $sqliteParticipantesDepois" -ForegroundColor White

# 6. Mostrar participantes importados
Write-Host ""
Write-Host "6. Participantes importados:" -ForegroundColor Yellow
sqlite3 data/inventario.db "SELECT id_participante, id_inventario, id_usuario, papel FROM local_participante_inventario;"

Write-Host ""
Write-Host "=== Importação Concluída ===" -ForegroundColor Cyan
