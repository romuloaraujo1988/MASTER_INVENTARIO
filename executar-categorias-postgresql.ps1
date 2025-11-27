# =====================================================
# Script para criar tabelas de categoria no PostgreSQL
# Sistema de Inventário - IFMT
# Data: 27/11/2025
# =====================================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "CRIANDO TABELAS DE CATEGORIA" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configurações do banco
$DB_HOST = "localhost"
$DB_PORT = "5432"
$DB_NAME = "sispatrimonio"
$DB_USER = "inventario"

# Solicitar senha
$DB_PASSWORD = Read-Host "Digite a senha do banco de dados" -AsSecureString
$BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($DB_PASSWORD)
$DB_PASSWORD_PLAIN = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)

# Definir variável de ambiente para senha
$env:PGPASSWORD = $DB_PASSWORD_PLAIN

Write-Host "Conectando ao banco de dados..." -ForegroundColor Yellow
Write-Host "Host: $DB_HOST" -ForegroundColor Gray
Write-Host "Database: $DB_NAME" -ForegroundColor Gray
Write-Host "User: $DB_USER" -ForegroundColor Gray
Write-Host ""

# Executar script SQL
Write-Host "Executando script SQL..." -ForegroundColor Yellow

try {
    psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f "sql/criar_tabelas_categoria_patrimonio.sql"
    
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
        psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c $query
        
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
    Write-Host "  1. PostgreSQL está rodando" -ForegroundColor White
    Write-Host "  2. Credenciais estão corretas" -ForegroundColor White
    Write-Host "  3. Banco de dados 'sispatrimonio' existe" -ForegroundColor White
    Write-Host "  4. psql está no PATH do sistema" -ForegroundColor White
}

# Limpar senha da memória
$env:PGPASSWORD = $null

Write-Host ""
Write-Host "Pressione qualquer tecla para sair..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
