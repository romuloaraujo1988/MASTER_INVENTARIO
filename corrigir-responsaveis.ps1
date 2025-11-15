# Script para verificar e corrigir responsáveis

Write-Host "🔍 Verificando responsáveis no banco de dados..." -ForegroundColor Cyan

# Configurar variáveis de ambiente
$env:PGPASSWORD = "inventario"

# Função para executar query e mostrar resultado
function Execute-Query {
    param($query, $titulo)
    
    Write-Host "`n📊 $titulo" -ForegroundColor Yellow
    $result = & psql -h localhost -U inventario -d sispatrimonio -t -c $query 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host $result -ForegroundColor Green
    } else {
        Write-Host "❌ Erro ao executar query" -ForegroundColor Red
        Write-Host $result -ForegroundColor Red
    }
}

# 1. Verificar total de responsáveis
Execute-Query "SELECT COUNT(*) FROM TABELA_RESPONSAVEL;" "Total de responsáveis"

# 2. Verificar responsáveis ativos
Execute-Query "SELECT COUNT(*) FROM TABELA_RESPONSAVEL WHERE ATIVO = TRUE;" "Responsáveis ativos"

# 3. Verificar responsáveis inativos
Execute-Query "SELECT COUNT(*) FROM TABELA_RESPONSAVEL WHERE ATIVO = FALSE OR ATIVO IS NULL;" "Responsáveis inativos"

# 4. Listar todos
Write-Host "`n📋 Lista de responsáveis:" -ForegroundColor Yellow
& psql -h localhost -U inventario -d sispatrimonio -c "SELECT ID, NOME, ATIVO FROM TABELA_RESPONSAVEL ORDER BY NOME;" 2>&1

# 5. Perguntar se deseja ativar todos
Write-Host "`n❓ Deseja ativar todos os responsáveis inativos? (S/N)" -ForegroundColor Cyan
$resposta = Read-Host

if ($resposta -eq "S" -or $resposta -eq "s") {
    Write-Host "`n🔄 Ativando todos os responsáveis..." -ForegroundColor Yellow
    & psql -h localhost -U inventario -d sispatrimonio -c "UPDATE TABELA_RESPONSAVEL SET ATIVO = TRUE WHERE ATIVO = FALSE OR ATIVO IS NULL;" 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Responsáveis ativados com sucesso!" -ForegroundColor Green
        Execute-Query "SELECT COUNT(*) FROM TABELA_RESPONSAVEL WHERE ATIVO = TRUE;" "Responsáveis ativos (após correção)"
    }
}

# 6. Verificar se há responsáveis, se não, perguntar se deseja inserir exemplos
$total = & psql -h localhost -U inventario -d sispatrimonio -t -c "SELECT COUNT(*) FROM TABELA_RESPONSAVEL;" 2>&1
$total = $total.Trim()

if ($total -eq "0") {
    Write-Host "`n⚠️ Nenhum responsável encontrado no banco!" -ForegroundColor Yellow
    Write-Host "❓ Deseja inserir responsáveis de exemplo? (S/N)" -ForegroundColor Cyan
    $resposta = Read-Host
    
    if ($resposta -eq "S" -or $resposta -eq "s") {
        Write-Host "`n📝 Inserindo responsáveis de exemplo..." -ForegroundColor Yellow
        
        $sql = @"
INSERT INTO TABELA_RESPONSAVEL (NOME, CPF, EMAIL, TELEFONE, CARGO, ATIVO) 
VALUES 
('João Silva', '123.456.789-00', 'joao.silva@ifmt.edu.br', '(65) 3333-4444', 'Administrador', TRUE),
('Maria Santos', '987.654.321-00', 'maria.santos@ifmt.edu.br', '(65) 3333-5555', 'Coordenadora TI', TRUE),
('Pedro Oliveira', '111.222.333-44', 'pedro.oliveira@ifmt.edu.br', '(65) 3333-6666', 'Gerente Financeiro', TRUE),
('Ana Costa', '555.666.777-88', 'ana.costa@ifmt.edu.br', '(65) 3333-7777', 'Supervisora RH', TRUE),
('Carlos Ferreira', '999.888.777-66', 'carlos.ferreira@ifmt.edu.br', '(65) 3333-8888', 'Responsável Almoxarifado', TRUE);
"@
        
        & psql -h localhost -U inventario -d sispatrimonio -c $sql 2>&1
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Responsáveis de exemplo inseridos com sucesso!" -ForegroundColor Green
            Execute-Query "SELECT COUNT(*) FROM TABELA_RESPONSAVEL WHERE ATIVO = TRUE;" "Responsáveis ativos"
        }
    }
}

Write-Host "`n✅ Verificação concluída!" -ForegroundColor Green
Write-Host "Agora tente abrir a tela de inventário novamente." -ForegroundColor Cyan
