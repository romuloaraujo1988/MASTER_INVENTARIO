# Script para popular SQLite com dados de exemplo
# Use este script se a importação automática não estiver funcionando

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Popular SQLite com Dados de Exemplo" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$SQLITE_DB = "data\inventario.db"

# Verificar se sqlite3 está disponível
$sqlite3 = Get-Command sqlite3 -ErrorAction SilentlyContinue
if (!$sqlite3) {
    Write-Host "[ERRO] sqlite3 não encontrado no PATH" -ForegroundColor Red
    Write-Host "[DICA] Instale o SQLite CLI ou adicione ao PATH" -ForegroundColor Yellow
    exit 1
}

# Verificar se o banco existe
if (!(Test-Path $SQLITE_DB)) {
    Write-Host "[ERRO] Banco SQLite não encontrado: $SQLITE_DB" -ForegroundColor Red
    Write-Host "[DICA] Execute primeiro: .\sincronizar-sqlite-offline.ps1" -ForegroundColor Yellow
    exit 1
}

Write-Host "[OK] Banco SQLite encontrado" -ForegroundColor Green
Write-Host ""

# SQL para inserir dados de exemplo
$sql = @"
-- Limpar dados existentes
DELETE FROM TABELA_INVENTARIO;
DELETE FROM USUARIO;
DELETE FROM SALA;
DELETE FROM PATRIMONIO;
DELETE FROM RESPONSAVEL;
DELETE FROM SALA_INVENTARIO;
DELETE FROM PARTICIPANTE_INVENTARIO;

-- Inserir inventário ativo
INSERT INTO TABELA_INVENTARIO (ID, NOME, ANO, DATA_INICIO, DATA_FIM, STATUS_INVENTARIO, RESPONSAVEL_INVENTARIO, PERCENTUAL_CONCLUSAO)
VALUES (1, 'Inventário Teste Offline 2024', 2024, date('now'), date('now', '+30 days'), 'EM_ANDAMENTO', 'Administrador do Sistema', 0.00);

-- Inserir usuário admin (senha: admin123)
INSERT INTO USUARIO (ID, LOGIN, SENHA, NOME_COMPLETO, EMAIL, PERFIL, ATIVO)
VALUES (1, 'admin', '\$2a\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Administrador do Sistema', 'admin@ifmt.edu.br', 'ADMIN', 1);

-- Inserir salas de exemplo
INSERT INTO SALA (ID_SALA, NUMERO_SALA, NOME_SALA, ANDAR, BLOCO, ATIVA)
VALUES 
(1, '101', 'Sala de Aula 101', '1º Andar', 'Bloco A', 1),
(2, '102', 'Sala de Aula 102', '1º Andar', 'Bloco A', 1),
(3, '103', 'Sala de Aula 103', '1º Andar', 'Bloco A', 1),
(4, '201', 'Laboratório de Informática', '2º Andar', 'Bloco B', 1),
(5, '202', 'Laboratório de Química', '2º Andar', 'Bloco B', 1);

-- Inserir responsáveis de exemplo
INSERT INTO RESPONSAVEL (ID, NOME, CPF, EMAIL, TELEFONE, CARGO, SETOR, ATIVO)
VALUES 
(1, 'João Silva', '111.111.111-11', 'joao.silva@ifmt.edu.br', '(65) 99999-0001', 'Professor', 'Departamento de TI', 1),
(2, 'Maria Santos', '222.222.222-22', 'maria.santos@ifmt.edu.br', '(65) 99999-0002', 'Coordenadora', 'Administração', 1),
(3, 'Pedro Oliveira', '333.333.333-33', 'pedro.oliveira@ifmt.edu.br', '(65) 99999-0003', 'Técnico', 'Manutenção', 1);

-- Inserir patrimônios de exemplo
INSERT INTO PATRIMONIO (ID, NUMERO, DESCRICAO, DESCRICAO_RESUMIDA, MARCA, MODELO, ESTADO_CONSERVACAO, VALOR_AQUISICAO, STATUS, ID_SALA, NOME_SALA, ID_RESPONSAVEL)
VALUES 
(1, '000001', 'Cadeira Giratória Executiva', 'Cadeira Giratória', 'Marca A', 'Modelo X', 'BOM', 350.00, 'ATIVO', 1, '101', 1),
(2, '000002', 'Mesa de Escritório em L', 'Mesa Escritório', 'Marca B', 'Modelo Y', 'BOM', 800.00, 'ATIVO', 1, '101', 1),
(3, '000003', 'Computador Desktop Core i5', 'Computador Desktop', 'Dell', 'Optiplex 3080', 'BOM', 3500.00, 'ATIVO', 4, '201', 2),
(4, '000004', 'Projetor Multimídia Full HD', 'Projetor', 'Epson', 'PowerLite X41+', 'BOM', 2800.00, 'ATIVO', 2, '102', 2),
(5, '000005', 'Quadro Branco 2x1m', 'Quadro Branco', 'Stalo', 'Standard', 'BOM', 180.00, 'ATIVO', 1, '101', 1),
(6, '000006', 'Ar Condicionado Split 12000 BTUs', 'Ar Condicionado', 'LG', 'Dual Inverter', 'BOM', 2200.00, 'ATIVO', 3, '103', 3),
(7, '000007', 'Impressora Multifuncional Laser', 'Impressora', 'HP', 'LaserJet Pro M428', 'BOM', 1800.00, 'ATIVO', 4, '201', 2),
(8, '000008', 'Notebook Dell i7 16GB', 'Notebook', 'Dell', 'Inspiron 15 5000', 'BOM', 4200.00, 'ATIVO', 4, '201', 2),
(9, '000009', 'Armário de Aço 2 Portas', 'Armário', 'Pandin', 'Standard', 'BOM', 650.00, 'ATIVO', 2, '102', 1),
(10, '000010', 'Ventilador de Teto', 'Ventilador', 'Ventisol', 'Wind', 'BOM', 280.00, 'ATIVO', 3, '103', 3);

-- Vincular salas ao inventário
INSERT INTO SALA_INVENTARIO (ID_SALA, ID_INVENTARIO, STATUS)
VALUES 
(1, 1, 'ABERTA'),
(2, 1, 'ABERTA'),
(3, 1, 'ABERTA'),
(4, 1, 'ABERTA'),
(5, 1, 'ABERTA');

-- Adicionar admin como participante do inventário
INSERT INTO PARTICIPANTE_INVENTARIO (ID_INVENTARIO, ID_USUARIO, NOME_PARTICIPANTE, EMAIL, PERFIL, ATIVO)
VALUES (1, 1, 'Administrador do Sistema', 'admin@ifmt.edu.br', 'ADMIN', 1);

-- Verificar dados inseridos
SELECT 'INVENTÁRIOS' as TABELA, COUNT(*) as TOTAL FROM TABELA_INVENTARIO
UNION ALL
SELECT 'USUÁRIOS', COUNT(*) FROM USUARIO
UNION ALL
SELECT 'SALAS', COUNT(*) FROM SALA
UNION ALL
SELECT 'PATRIMÔNIOS', COUNT(*) FROM PATRIMONIO
UNION ALL
SELECT 'RESPONSÁVEIS', COUNT(*) FROM RESPONSAVEL;
"@

# Salvar SQL em arquivo temporário
$sqlFile = "temp_popular_sqlite.sql"
$sql | Out-File -FilePath $sqlFile -Encoding UTF8

Write-Host "[INFO] Executando SQL no banco SQLite..." -ForegroundColor Cyan
Write-Host ""

# Executar SQL
$output = sqlite3 $SQLITE_DB ".read $sqlFile" 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "[OK] Dados inseridos com sucesso!" -ForegroundColor Green
    Write-Host ""
    
    # Mostrar resumo
    Write-Host "=== RESUMO DOS DADOS INSERIDOS ===" -ForegroundColor Cyan
    sqlite3 $SQLITE_DB "
    SELECT 'INVENTÁRIOS' as TABELA, COUNT(*) as TOTAL FROM TABELA_INVENTARIO
    UNION ALL
    SELECT 'USUÁRIOS', COUNT(*) FROM USUARIO
    UNION ALL
    SELECT 'SALAS', COUNT(*) FROM SALA
    UNION ALL
    SELECT 'PATRIMÔNIOS', COUNT(*) FROM PATRIMONIO
    UNION ALL
    SELECT 'RESPONSÁVEIS', COUNT(*) FROM RESPONSAVEL;
    "
    
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "  Dados de Exemplo Inseridos!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "[INFO] Credenciais de teste:" -ForegroundColor Cyan
    Write-Host "       Login: admin" -ForegroundColor White
    Write-Host "       Senha: admin123" -ForegroundColor White
    Write-Host ""
    Write-Host "[INFO] Agora você pode:" -ForegroundColor Cyan
    Write-Host "       1. Forçar Modo Offline no sistema" -ForegroundColor White
    Write-Host "       2. Fazer login com as credenciais acima" -ForegroundColor White
    Write-Host "       3. Realizar coletas offline" -ForegroundColor White
    
} else {
    Write-Host "[ERRO] Falha ao inserir dados" -ForegroundColor Red
    Write-Host $output
}

# Limpar arquivo temporário
Remove-Item $sqlFile -ErrorAction SilentlyContinue

Write-Host ""
Write-Host "Pressione qualquer tecla para sair..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
