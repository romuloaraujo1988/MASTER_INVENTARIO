# Script para diagnosticar coletas pendentes no app Android
# Executa queries no banco SQLite do app via ADB

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "DIAGNÓSTICO DE COLETAS PENDENTES" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Caminho do banco no dispositivo
$DB_PATH = "/data/data/com.inventario.mobile/databases/inventario_database"

# Verificar se ADB está disponível
$adbPath = Get-Command adb -ErrorAction SilentlyContinue
if (-not $adbPath) {
    Write-Host "❌ ADB não encontrado. Instale o Android SDK." -ForegroundColor Red
    exit 1
}

# Verificar dispositivo conectado
$devices = adb devices | Select-String "device$"
if (-not $devices) {
    Write-Host "❌ Nenhum dispositivo Android conectado." -ForegroundColor Red
    exit 1
}

Write-Host "✓ Dispositivo conectado" -ForegroundColor Green
Write-Host ""

# Função para executar query SQLite
function Invoke-SQLiteQuery {
    param([string]$query)
    $result = adb shell "run-as com.inventario.mobile sqlite3 $DB_PATH `"$query`"" 2>&1
    return $result
}

# 1. Contar coletas pendentes
Write-Host "📊 ESTATÍSTICAS GERAIS" -ForegroundColor Yellow
Write-Host "----------------------------------------"

$totalColetas = Invoke-SQLiteQuery "SELECT COUNT(*) FROM coleta;"
$pendentes = Invoke-SQLiteQuery "SELECT COUNT(*) FROM coleta WHERE sincronizado = 0;"
$sincronizadas = Invoke-SQLiteQuery "SELECT COUNT(*) FROM coleta WHERE sincronizado = 1;"
$comErro = Invoke-SQLiteQuery "SELECT COUNT(*) FROM coleta WHERE sincronizado = 0 AND erroSincronizacao IS NOT NULL;"

Write-Host "Total de coletas: $totalColetas"
Write-Host "Pendentes: $pendentes" -ForegroundColor $(if ($pendentes -gt 0) { "Yellow" } else { "Green" })
Write-Host "Sincronizadas: $sincronizadas" -ForegroundColor Green
Write-Host "Com erro: $comErro" -ForegroundColor $(if ($comErro -gt 0) { "Red" } else { "Green" })
Write-Host ""

# 2. Listar coletas pendentes com detalhes
if ($pendentes -gt 0) {
    Write-Host "📋 COLETAS PENDENTES" -ForegroundColor Yellow
    Write-Host "----------------------------------------"
    
    $query = @"
SELECT 
    id,
    numeroPatrimonio,
    idInventario,
    tentativasSincronizacao,
    COALESCE(erroSincronizacao, 'Sem erro registrado') as erro,
    datetime(dataColeta/1000, 'unixepoch', 'localtime') as data
FROM coleta 
WHERE sincronizado = 0 
ORDER BY dataColeta DESC 
LIMIT 10;
"@
    
    $coletas = Invoke-SQLiteQuery $query
    Write-Host $coletas
    Write-Host ""
}

# 3. Listar coletas com erro
if ($comErro -gt 0) {
    Write-Host "❌ COLETAS COM ERRO DE SINCRONIZAÇÃO" -ForegroundColor Red
    Write-Host "----------------------------------------"
    
    $query = @"
SELECT 
    id,
    numeroPatrimonio,
    tentativasSincronizacao as tentativas,
    erroSincronizacao as erro
FROM coleta 
WHERE sincronizado = 0 
AND erroSincronizacao IS NOT NULL 
ORDER BY tentativasSincronizacao DESC;
"@
    
    $coletasErro = Invoke-SQLiteQuery $query
    Write-Host $coletasErro
    Write-Host ""
}

# 4. Verificar inventário ativo
Write-Host "🏢 INVENTÁRIO CONFIGURADO" -ForegroundColor Yellow
Write-Host "----------------------------------------"

# Tentar ler SharedPreferences (pode não funcionar em todos os dispositivos)
$prefsPath = "/data/data/com.inventario.mobile/shared_prefs"
$inventarioId = adb shell "run-as com.inventario.mobile cat $prefsPath/inventario_prefs.xml 2>/dev/null | grep inventario" 2>&1

if ($inventarioId -match "inventario") {
    Write-Host "Inventário configurado: $inventarioId"
} else {
    Write-Host "⚠️ Não foi possível verificar inventário configurado" -ForegroundColor Yellow
}
Write-Host ""

# 5. Sugestões
Write-Host "💡 SUGESTÕES" -ForegroundColor Cyan
Write-Host "----------------------------------------"

if ($comErro -gt 0) {
    Write-Host "1. Verifique os erros listados acima"
    Write-Host "2. Se 'Inventário não configurado': Configure o inventário no app"
    Write-Host "3. Se 'Usuário não participante': Verifique cadastro no servidor"
    Write-Host "4. Se 'Timeout': Tente novamente com rede melhor"
    Write-Host ""
    Write-Host "Para limpar erros e tentar novamente:" -ForegroundColor Yellow
    Write-Host "  UPDATE coleta SET erroSincronizacao = NULL, tentativasSincronizacao = 0 WHERE sincronizado = 0;"
} elseif ($pendentes -gt 0) {
    Write-Host "Coletas pendentes sem erro registrado."
    Write-Host "Possíveis causas:"
    Write-Host "  - Sincronização ainda não foi tentada"
    Write-Host "  - App foi fechado antes de tentar sincronizar"
    Write-Host ""
    Write-Host "Tente sincronizar manualmente no app."
} else {
    Write-Host "✓ Nenhuma coleta pendente!" -ForegroundColor Green
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "FIM DO DIAGNÓSTICO" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
