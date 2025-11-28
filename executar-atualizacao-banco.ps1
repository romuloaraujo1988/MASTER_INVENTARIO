# ============================================================================
# Script para executar atualização segura do banco de dados
# Sistema de Inventário Patrimonial - SIHCP
# 
# Este script executa o SQL que APENAS ADICIONA tabelas e colunas que não existem.
# NÃO APAGA nem MODIFICA dados existentes.
# ============================================================================

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  ATUALIZAÇÃO SEGURA DO BANCO DE DADOS" -ForegroundColor Cyan
Write-Host "  Sistema de Inventário Patrimonial - SIHCP" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

# Configurações do banco
$DB_HOST = "localhost"
$DB_PORT = "5432"
$DB_NAME = "sispatrimonio"
$DB_USER = "inventario"

# Verificar se o arquivo SQL existe
$SQL_FILE = "sql\atualizar_banco_seguro.sql"
if (-not (Test-Path $SQL_FILE)) {
    Write-Host "❌ Arquivo SQL não encontrado: $SQL_FILE" -ForegroundColor Red
    exit 1
}

Write-Host "📋 Configurações:" -ForegroundColor Yellow
Write-Host "   Host: $DB_HOST"
Write-Host "   Porta: $DB_PORT"
Write-Host "   Banco: $DB_NAME"
Write-Host "   Usuário: $DB_USER"
Write-Host "   Script: $SQL_FILE"
Write-Host ""

# Confirmar execução
Write-Host "⚠️  Este script irá:" -ForegroundColor Yellow
Write-Host "   ✓ Adicionar tabelas que não existem"
Write-Host "   ✓ Adicionar colunas que não existem"
Write-Host "   ✓ Criar índices que não existem"
Write-Host ""
Write-Host "   ✗ NÃO apaga dados"
Write-Host "   ✗ NÃO modifica dados existentes"
Write-Host "   ✗ NÃO remove tabelas ou colunas"
Write-Host ""

$confirm = Read-Host "Deseja continuar? (S/N)"
if ($confirm -ne "S" -and $confirm -ne "s") {
    Write-Host "Operação cancelada pelo usuário." -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "🔄 Executando atualização..." -ForegroundColor Cyan

# Tentar executar com psql
try {
    # Verificar se psql está disponível
    $psqlPath = Get-Command psql -ErrorAction SilentlyContinue
    
    if ($psqlPath) {
        Write-Host "   Usando psql encontrado em: $($psqlPath.Source)" -ForegroundColor Gray
        
        # Executar o script SQL
        $env:PGPASSWORD = Read-Host "Digite a senha do banco" -AsSecureString | ConvertFrom-SecureString -AsPlainText
        
        psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f $SQL_FILE
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host ""
            Write-Host "✅ Atualização concluída com sucesso!" -ForegroundColor Green
        } else {
            Write-Host ""
            Write-Host "❌ Erro durante a execução do script SQL" -ForegroundColor Red
            exit 1
        }
    } else {
        Write-Host "❌ psql não encontrado no PATH" -ForegroundColor Red
        Write-Host ""
        Write-Host "Alternativas:" -ForegroundColor Yellow
        Write-Host "1. Adicione o PostgreSQL ao PATH"
        Write-Host "2. Execute manualmente no pgAdmin:"
        Write-Host "   - Abra o pgAdmin"
        Write-Host "   - Conecte ao banco '$DB_NAME'"
        Write-Host "   - Abra o arquivo: $SQL_FILE"
        Write-Host "   - Execute o script (F5)"
        exit 1
    }
} catch {
    Write-Host "❌ Erro: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  ATUALIZAÇÃO FINALIZADA" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
