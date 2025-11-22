# Script completo para diagnosticar e corrigir o problema das salas
# Verifica PostgreSQL, ativa salas, sincroniza e testa

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  DIAGNÓSTICO E CORREÇÃO DE SALAS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configurações do banco
$dbHost = "localhost"
$dbPort = "5432"
$dbName = "sispatrimonio"
$dbUser = "inventario"

# ========== PASSO 1: DIAGNÓSTICO POSTGRESQL ==========
Write-Host "PASSO 1: Diagnosticando PostgreSQL..." -ForegroundColor Yellow
Write-Host ""

$sqlDiagnostico = @"
SELECT 
    COUNT(*) as total,
    COUNT(*) FILTER (WHERE ATIVO = TRUE) as ativas,
    COUNT(*) FILTER (WHERE ATIVO = FALSE) as inativas
FROM TABELA_SALA;
"@

$result = psql -h $dbHost -p $dbPort -U $dbUser -d $dbName -t -A -F'|' -c $sqlDiagnostico 2>&1

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ ERRO ao conectar ao PostgreSQL!" -ForegroundColor Red
    Write-Host "Mensagem: $result" -ForegroundColor Red
    Write-Host ""
    Write-Host "Verifique:" -ForegroundColor Yellow
    Write-Host "  1. PostgreSQL está rodando?" -ForegroundColor Gray
    Write-Host "  2. Credenciais estão corretas?" -ForegroundColor Gray
    Write-Host "  3. Banco 'sispatrimonio' existe?" -ForegroundColor Gray
    exit 1
}

$valores = $result.Trim() -split '\|'
$total = [int]$valores[0]
$ativas = [int]$valores[1]
$inativas = [int]$valores[2]

Write-Host "Status PostgreSQL:" -ForegroundColor Cyan
Write-Host "  Total de salas: $total" -ForegroundColor White
Write-Host "  Salas ATIVAS: $ativas" -ForegroundColor Green
Write-Host "  Salas INATIVAS: $inativas" -ForegroundColor Red
Write-Host ""

if ($total -eq 0) {
    Write-Host "❌ ERRO: Nenhuma sala encontrada no PostgreSQL!" -ForegroundColor Red
    Write-Host "Execute a importação de dados primeiro." -ForegroundColor Yellow
    exit 1
}

# ========== PASSO 2: ATIVAR SALAS (SE NECESSÁRIO) ==========
if ($inativas -gt 0) {
    Write-Host "PASSO 2: Ativando salas inativas..." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Encontradas $inativas sala(s) inativa(s)." -ForegroundColor Yellow
    Write-Host "Deseja ativar todas? (S/N): " -ForegroundColor Yellow -NoNewline
    $resposta = Read-Host
    
    if ($resposta -eq "S" -or $resposta -eq "s") {
        $sqlAtivar = "UPDATE TABELA_SALA SET ATIVO = TRUE WHERE ATIVO = FALSE;"
        psql -h $dbHost -p $dbPort -U $dbUser -d $dbName -c $sqlAtivar | Out-Null
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ $inativas sala(s) ativada(s) com sucesso!" -ForegroundColor Green
            $ativas = $total
        } else {
            Write-Host "❌ Erro ao ativar salas!" -ForegroundColor Red
            exit 1
        }
    } else {
        Write-Host "⚠️ Salas inativas não foram ativadas." -ForegroundColor Yellow
        Write-Host "Apenas $ativas sala(s) serão sincronizadas." -ForegroundColor Yellow
    }
} else {
    Write-Host "PASSO 2: Todas as salas já estão ativas! ✅" -ForegroundColor Green
}

Write-Host ""

# ========== PASSO 3: VERIFICAR SALAS FINALIZADAS ==========
Write-Host "PASSO 3: Verificando salas finalizadas..." -ForegroundColor Yellow
Write-Host ""

$sqlFinalizadas = @"
SELECT COUNT(DISTINCT si.ID_SALA)
FROM TABELA_SALA_INVENTARIO si
INNER JOIN TABELA_INVENTARIO i ON si.ID_INVENTARIO = i.ID_INVENTARIO
WHERE i.STATUS = 'EM_ANDAMENTO'
AND si.COLETA_FINALIZADA = TRUE;
"@

$finalizadas = psql -h $dbHost -p $dbPort -U $dbUser -d $dbName -t -c $sqlFinalizadas 2>&1

if ($LASTEXITCODE -eq 0) {
    $numFinalizadas = [int]$finalizadas.Trim()
    Write-Host "Salas finalizadas no inventário ativo: $numFinalizadas" -ForegroundColor Cyan
    
    if ($numFinalizadas -gt 0) {
        Write-Host ""
        Write-Host "⚠️ ATENÇÃO: $numFinalizadas sala(s) estão finalizadas!" -ForegroundColor Yellow
        Write-Host "Salas finalizadas NÃO aparecem no ColetaFrame_v2." -ForegroundColor Yellow
        Write-Host ""
        Write-Host "Deseja reabrir TODAS as salas finalizadas? (S/N): " -ForegroundColor Yellow -NoNewline
        $resposta = Read-Host
        
        if ($resposta -eq "S" -or $resposta -eq "s") {
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
            psql -h $dbHost -p $dbPort -U $dbUser -d $dbName -c $sqlReabrir | Out-Null
            
            if ($LASTEXITCODE -eq 0) {
                Write-Host "✅ $numFinalizadas sala(s) reaberta(s) com sucesso!" -ForegroundColor Green
            } else {
                Write-Host "❌ Erro ao reabrir salas!" -ForegroundColor Red
            }
        }
    }
} else {
    Write-Host "⚠️ Não foi possível verificar salas finalizadas." -ForegroundColor Yellow
}

Write-Host ""

# ========== PASSO 4: SINCRONIZAR PARA SQLITE ==========
Write-Host "PASSO 4: Sincronizando para SQLite..." -ForegroundColor Yellow
Write-Host ""

if (Test-Path ".\sincronizar-sqlite-offline.ps1") {
    Write-Host "Executando sincronização..." -ForegroundColor Gray
    & ".\sincronizar-sqlite-offline.ps1"
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Sincronização concluída!" -ForegroundColor Green
    } else {
        Write-Host "❌ Erro na sincronização!" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "❌ Script sincronizar-sqlite-offline.ps1 não encontrado!" -ForegroundColor Red
    exit 1
}

Write-Host ""

# ========== PASSO 5: VERIFICAR SQLITE ==========
Write-Host "PASSO 5: Verificando SQLite..." -ForegroundColor Yellow
Write-Host ""

if (Test-Path "data\inventario.db") {
    $salasLocal = sqlite3 "data\inventario.db" "SELECT COUNT(*) FROM local_sala WHERE ativa = 1;" 2>&1
    $salasSala = sqlite3 "data\inventario.db" "SELECT COUNT(*) FROM SALA WHERE ATIVA = 1;" 2>&1
    
    Write-Host "Salas no SQLite:" -ForegroundColor Cyan
    Write-Host "  local_sala: $salasLocal" -ForegroundColor White
    Write-Host "  SALA: $salasSala" -ForegroundColor White
} else {
    Write-Host "⚠️ Banco SQLite não encontrado em data\inventario.db" -ForegroundColor Yellow
}

Write-Host ""

# ========== RESUMO FINAL ==========
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  RESUMO FINAL" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "PostgreSQL:" -ForegroundColor White
Write-Host "  ✅ Total de salas: $total" -ForegroundColor Green
Write-Host "  ✅ Salas ativas: $ativas" -ForegroundColor Green
Write-Host ""
Write-Host "SQLite:" -ForegroundColor White
if (Test-Path "data\inventario.db") {
    Write-Host "  ✅ Salas sincronizadas: $salasLocal" -ForegroundColor Green
} else {
    Write-Host "  ⚠️ Banco não encontrado" -ForegroundColor Yellow
}
Write-Host ""
Write-Host "Próximos passos:" -ForegroundColor Cyan
Write-Host "  1. Abra o ColetaFrame_v2" -ForegroundColor Gray
Write-Host "  2. Verifique se todas as $ativas salas aparecem" -ForegroundColor Gray
Write-Host "  3. Se ainda aparecerem apenas 3, verifique salas finalizadas" -ForegroundColor Gray
Write-Host ""
Write-Host "Pressione qualquer tecla para sair..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
