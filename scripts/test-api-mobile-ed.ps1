# ============================================
# Script de Teste - API Mobile com Campo ED
# Data: 16/11/2024
# ============================================

$ErrorActionPreference = "Stop"

# Configurações
$API_URL = "http://localhost:8080"
$PGHOST = "localhost"
$PGPORT = "5432"
$PGUSER = "postgres"
$PGDATABASE = "sispatrimonio"
$env:PGPASSWORD = "Romulo@2020"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  TESTE DE API MOBILE - CAMPO ED" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar se servidor está rodando
Write-Host "🔍 Verificando se servidor está rodando..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$API_URL/api/mobile/inventario/ativo" -Method GET -TimeoutSec 5 -ErrorAction Stop
    Write-Host "✅ Servidor está rodando" -ForegroundColor Green
} catch {
    Write-Host "❌ Servidor não está respondendo em $API_URL" -ForegroundColor Red
    Write-Host "Inicie o servidor antes de executar este teste." -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Criar patrimônio de teste no banco
Write-Host "📝 Criando patrimônio de teste..." -ForegroundColor Yellow

& "C:\Program Files\PostgreSQL\17\bin\psql.exe" `
    -h $PGHOST `
    -p $PGPORT `
    -U $PGUSER `
    -d $PGDATABASE `
    -c "DELETE FROM TABELA_PATRIMONIO WHERE NUMERO = 'TEST-API-001';" | Out-Null

$resultado = & "C:\Program Files\PostgreSQL\17\bin\psql.exe" `
    -h $PGHOST `
    -p $PGPORT `
    -U $PGUSER `
    -d $PGDATABASE `
    -c "INSERT INTO TABELA_PATRIMONIO (NUMERO, STATUS, ED, DESCRICAO, MARCA, MODELO, NUMERO_SERIE, NUMERO_NOTA_FISCAL, FORNECEDOR, VALOR_AQUISICAO, ESTADO_CONSERVACAO) VALUES ('TEST-API-001', 'ATIVO', '12311.0101', 'Computador Desktop Teste API', 'Dell', 'OptiPlex 7090', 'SN-TEST-001', 'NF-API-001', 'Dell Computadores LTDA', 2500.00, 'BOM') RETURNING ID;" 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Patrimônio de teste criado" -ForegroundColor Green
} else {
    Write-Host "❌ Erro ao criar patrimônio de teste: $resultado" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "🧪 Testando endpoints da API..." -ForegroundColor Yellow
Write-Host ""

$totalTestes = 0
$testesPassados = 0
$testesFalhados = 0

# Teste 1: Buscar patrimônio por número
$totalTestes++
Write-Host "Teste 1: GET /api/mobile/patrimonio/numero/TEST-API-001" -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri "$API_URL/api/mobile/patrimonio/numero/TEST-API-001" -Method GET -ErrorAction Stop
    
    if ($response.success -eq $true) {
        Write-Host "  ✅ Endpoint respondeu com sucesso" -ForegroundColor Green
        
        # Verificar se campo ED está presente
        if ($response.data.ed) {
            Write-Host "  ✅ Campo 'ed' presente na resposta: $($response.data.ed)" -ForegroundColor Green
            $testesPassados++
        } else {
            Write-Host "  ❌ Campo 'ed' NÃO está presente na resposta" -ForegroundColor Red
            $testesFalhados++
        }
        
        # Verificar se campo numeroNotaFiscal está presente
        if ($response.data.numeroNotaFiscal) {
            Write-Host "  ✅ Campo 'numeroNotaFiscal' presente: $($response.data.numeroNotaFiscal)" -ForegroundColor Green
        } else {
            Write-Host "  ⚠️  Campo 'numeroNotaFiscal' não está presente" -ForegroundColor Yellow
        }
        
        # Verificar se campo fornecedor está presente
        if ($response.data.fornecedor) {
            Write-Host "  ✅ Campo 'fornecedor' presente: $($response.data.fornecedor)" -ForegroundColor Green
        } else {
            Write-Host "  ⚠️  Campo 'fornecedor' não está presente" -ForegroundColor Yellow
        }
        
        # Mostrar resposta completa
        Write-Host ""
        Write-Host "  📋 Resposta completa:" -ForegroundColor Yellow
        $response.data | ConvertTo-Json -Depth 3 | Write-Host
        
    } else {
        Write-Host "  ❌ Endpoint retornou erro: $($response.message)" -ForegroundColor Red
        $testesFalhados++
    }
} catch {
    Write-Host "  ❌ Erro ao chamar endpoint: $_" -ForegroundColor Red
    $testesFalhados++
}

Write-Host ""

# Teste 2: Buscar patrimônio por QR Code
$totalTestes++
Write-Host "Teste 2: GET /api/mobile/patrimonio/qrcode/TEST-API-001" -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri "$API_URL/api/mobile/patrimonio/qrcode/TEST-API-001" -Method GET -ErrorAction Stop
    
    if ($response.success -eq $true) {
        Write-Host "  ✅ Endpoint respondeu com sucesso" -ForegroundColor Green
        
        if ($response.data.ed -eq "12311.0101") {
            Write-Host "  ✅ Campo 'ed' com valor correto: $($response.data.ed)" -ForegroundColor Green
            $testesPassados++
        } else {
            Write-Host "  ❌ Campo 'ed' com valor incorreto ou ausente" -ForegroundColor Red
            $testesFalhados++
        }
    } else {
        Write-Host "  ❌ Endpoint retornou erro: $($response.message)" -ForegroundColor Red
        $testesFalhados++
    }
} catch {
    Write-Host "  ❌ Erro ao chamar endpoint: $_" -ForegroundColor Red
    $testesFalhados++
}

Write-Host ""

# Teste 3: Validar patrimônio
$totalTestes++
Write-Host "Teste 3: GET /api/mobile/patrimonio/numero/TEST-API-001/validar" -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri "$API_URL/api/mobile/patrimonio/numero/TEST-API-001/validar" -Method GET -ErrorAction Stop
    
    if ($response.success -eq $true) {
        Write-Host "  ✅ Endpoint respondeu com sucesso" -ForegroundColor Green
        
        if ($response.data.valido -eq $true) {
            Write-Host "  ✅ Patrimônio validado com sucesso" -ForegroundColor Green
            
            if ($response.data.patrimonio.ed) {
                Write-Host "  ✅ Campo 'ed' presente no patrimônio validado: $($response.data.patrimonio.ed)" -ForegroundColor Green
                $testesPassados++
            } else {
                Write-Host "  ❌ Campo 'ed' NÃO está presente no patrimônio validado" -ForegroundColor Red
                $testesFalhados++
            }
        } else {
            Write-Host "  ❌ Patrimônio não foi validado" -ForegroundColor Red
            $testesFalhados++
        }
    } else {
        Write-Host "  ❌ Endpoint retornou erro: $($response.message)" -ForegroundColor Red
        $testesFalhados++
    }
} catch {
    Write-Host "  ❌ Erro ao chamar endpoint: $_" -ForegroundColor Red
    $testesFalhados++
}

Write-Host ""
Write-Host "🧹 Removendo patrimônio de teste..." -ForegroundColor Yellow

& "C:\Program Files\PostgreSQL\17\bin\psql.exe" `
    -h $PGHOST `
    -p $PGPORT `
    -U $PGUSER `
    -d $PGDATABASE `
    -c "DELETE FROM TABELA_PATRIMONIO WHERE NUMERO = 'TEST-API-001';" | Out-Null

Write-Host "✅ Patrimônio de teste removido" -ForegroundColor Green

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  RESUMO DOS TESTES" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Total de Testes: $totalTestes" -ForegroundColor White
Write-Host "Testes Passados: $testesPassados" -ForegroundColor Green
Write-Host "Testes Falhados: $testesFalhados" -ForegroundColor Red
Write-Host ""

if ($testesFalhados -eq 0) {
    Write-Host "✅ TODOS OS TESTES DA API PASSARAM!" -ForegroundColor Green
    Write-Host "A API Mobile está retornando o campo ED corretamente." -ForegroundColor Green
    exit 0
} else {
    Write-Host "❌ ALGUNS TESTES DA API FALHARAM!" -ForegroundColor Red
    Write-Host "Verifique os erros acima e corrija antes de fazer deploy." -ForegroundColor Red
    exit 1
}
