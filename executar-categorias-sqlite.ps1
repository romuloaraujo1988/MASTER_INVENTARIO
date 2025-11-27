# =====================================================
# Script para criar tabelas de categoria no SQLite
# Sistema de Inventário - IFMT (Modo Offline)
# Data: 27/11/2025
# =====================================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "CRIANDO TABELAS DE CATEGORIA (SQLite)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Caminho do banco SQLite
$DB_PATH = "data\inventario_offline.db"

# Verificar se o diretório data existe
if (-not (Test-Path "data")) {
    Write-Host "Criando diretório 'data'..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Path "data" | Out-Null
}

# Verificar se o banco existe
if (-not (Test-Path $DB_PATH)) {
    Write-Host "Banco de dados não encontrado. Criando novo banco..." -ForegroundColor Yellow
    # Criar banco vazio
    sqlite3 $DB_PATH "SELECT 1;"
}

Write-Host "Banco de dados: $DB_PATH" -ForegroundColor Gray
Write-Host ""

# Executar script SQL
Write-Host "Executando script SQL..." -ForegroundColor Yellow

try {
    # Executar script
    Get-Content "sql\criar_tabelas_categoria_patrimonio_sqlite.sql" | sqlite3 $DB_PATH
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "✓ TABELAS CRIADAS COM SUCESSO!" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
        Write-Host ""
        
        Write-Host "Tabelas criadas:" -ForegroundColor Cyan
        Write-Host "  - tabela_categoria_patrimonio" -ForegroundColor White
        Write-Host "  - tabela_subcategoria_patrimonio" -ForegroundColor White
        Write-Host ""
        
        Write-Host "Views criadas:" -ForegroundColor Cyan
        Write-Host "  - view_categorias_com_contagem" -ForegroundColor White
        Write-Host "  - view_categorias_subcategorias" -ForegroundColor White
        Write-Host ""
        
        Write-Host "Dados inseridos:" -ForegroundColor Cyan
        Write-Host "  - 9 categorias principais" -ForegroundColor White
        Write-Host "  - 40+ subcategorias" -ForegroundColor White
        Write-Host ""
        
        # Consultar dados inseridos
        Write-Host "Consultando categorias criadas..." -ForegroundColor Yellow
        Write-Host ""
        
        $query = "SELECT nome, total_subcategorias FROM view_categorias_com_contagem ORDER BY ordem_exibicao;"
        sqlite3 $DB_PATH $query -header -column
        
        Write-Host ""
        Write-Host "Banco SQLite criado em: $DB_PATH" -ForegroundColor Green
        
    } else {
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Red
        Write-Host "✗ ERRO AO CRIAR TABELAS" -ForegroundColor Red
        Write-Host "========================================" -ForegroundColor Red
        Write-Host ""
        Write-Host "Verifique os erros acima e tente novamente." -ForegroundColor Yellow
    }
    
} catch {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "✗ ERRO NA EXECUÇÃO" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "Erro: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "Verifique se:" -ForegroundColor Yellow
    Write-Host "  1. sqlite3 está instalado" -ForegroundColor White
    Write-Host "  2. sqlite3 está no PATH do sistema" -ForegroundColor White
    Write-Host "  3. Você tem permissão para criar arquivos no diretório 'data'" -ForegroundColor White
}

Write-Host ""
Write-Host "Pressione qualquer tecla para sair..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
