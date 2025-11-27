# Script para criar tabelas de itens compostos
# Sistema de Inventário - IFMT

$env:PGPASSWORD = "Romulo@2020"
$PGHOST = "localhost"
$PGUSER = "inventario"
$PGDATABASE = "sispatrimonio"
$PGPORT = "5432"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Criando Tabelas de Itens Compostos" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar se PostgreSQL está acessível
Write-Host "Verificando conexão com PostgreSQL..." -ForegroundColor Yellow

$sqlScript = @"
-- Tabela de definição de itens compostos
CREATE TABLE IF NOT EXISTS tabela_item_composto (
    id SERIAL PRIMARY KEY,
    id_patrimonio_principal INTEGER NOT NULL,
    tipo_componente VARCHAR(100) NOT NULL,
    descricao_componente VARCHAR(255) NOT NULL,
    quantidade_esperada INTEGER NOT NULL DEFAULT 1,
    obrigatorio BOOLEAN DEFAULT TRUE,
    observacao TEXT,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_item_composto_patrimonio 
        FOREIGN KEY (id_patrimonio_principal) 
        REFERENCES tabela_patrimonio(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT chk_quantidade_positiva 
        CHECK (quantidade_esperada > 0)
);

-- Tabela de coleta de componentes
CREATE TABLE IF NOT EXISTS tabela_coleta_componente (
    id SERIAL PRIMARY KEY,
    id_item_composto INTEGER NOT NULL,
    id_inventario INTEGER NOT NULL,
    id_coletor INTEGER NOT NULL,
    quantidade_encontrada INTEGER NOT NULL DEFAULT 0,
    status_componente VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    observacao_coleta TEXT,
    data_coleta TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_coleta_comp_item 
        FOREIGN KEY (id_item_composto) 
        REFERENCES tabela_item_composto(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT fk_coleta_comp_inventario 
        FOREIGN KEY (id_inventario) 
        REFERENCES tabela_inventario(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT fk_coleta_comp_coletor 
        FOREIGN KEY (id_coletor) 
        REFERENCES tabela_usuario(id) 
        ON DELETE RESTRICT,
    
    CONSTRAINT chk_status_componente 
        CHECK (status_componente IN ('PENDENTE', 'COMPLETO', 'PARCIAL', 'FALTANTE')),
    
    CONSTRAINT chk_quantidade_nao_negativa 
        CHECK (quantidade_encontrada >= 0),
    
    CONSTRAINT uk_coleta_componente_inventario 
        UNIQUE (id_item_composto, id_inventario)
);

-- Índices
CREATE INDEX IF NOT EXISTS idx_item_composto_patrimonio 
    ON tabela_item_composto(id_patrimonio_principal);

CREATE INDEX IF NOT EXISTS idx_coleta_componente_item 
    ON tabela_coleta_componente(id_item_composto);

CREATE INDEX IF NOT EXISTS idx_coleta_componente_inventario 
    ON tabela_coleta_componente(id_inventario);

CREATE INDEX IF NOT EXISTS idx_coleta_componente_status 
    ON tabela_coleta_componente(status_componente);

-- View para facilitar consultas
CREATE OR REPLACE VIEW view_itens_compostos AS
SELECT 
    ic.id,
    ic.id_patrimonio_principal,
    p.numero AS numero_patrimonio,
    p.descricao AS descricao_patrimonio,
    ic.tipo_componente,
    ic.descricao_componente,
    ic.quantidade_esperada,
    ic.obrigatorio,
    ic.observacao,
    p.id_sala,
    s.nome_sala,
    p.id_responsavel,
    r.nome AS nome_responsavel
FROM tabela_item_composto ic
JOIN tabela_patrimonio p ON ic.id_patrimonio_principal = p.id
LEFT JOIN tabela_sala s ON p.id_sala = s.id_sala
LEFT JOIN tabela_responsavel r ON p.id_responsavel = r.id_responsavel;

-- Dados de exemplo
INSERT INTO tabela_item_composto (id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio)
VALUES 
    (1, 'CADEIRA', 'Cadeira escolar', 1, TRUE),
    (1, 'MESA', 'Mesa individual', 1, TRUE)
ON CONFLICT DO NOTHING;

INSERT INTO tabela_item_composto (id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio)
VALUES 
    (2, 'MONITOR', 'Monitor LCD', 1, TRUE),
    (2, 'TECLADO', 'Teclado USB', 1, TRUE),
    (2, 'MOUSE', 'Mouse USB', 1, TRUE),
    (2, 'CPU', 'Gabinete CPU', 1, TRUE),
    (2, 'ESTABILIZADOR', 'Estabilizador 500VA', 1, FALSE)
ON CONFLICT DO NOTHING;
"@

try {
    # Executar via psql se disponível
    if (Get-Command psql -ErrorAction SilentlyContinue) {
        Write-Host "Executando via psql..." -ForegroundColor Green
        $sqlScript | & psql -h $PGHOST -U $PGUSER -d $PGDATABASE -p $PGPORT
    }
    else {
        # Executar via .NET
        Write-Host "Executando via .NET..." -ForegroundColor Green
        
        Add-Type -AssemblyName "Npgsql, Version=4.1.0.0, Culture=neutral, PublicKeyToken=5d8b90d52f46fda7" -ErrorAction SilentlyContinue
        
        $connectionString = "Host=$PGHOST;Port=$PGPORT;Database=$PGDATABASE;Username=$PGUSER;Password=$env:PGPASSWORD"
        $connection = New-Object Npgsql.NpgsqlConnection($connectionString)
        $connection.Open()
        
        $command = $connection.CreateCommand()
        $command.CommandText = $sqlScript
        $command.ExecuteNonQuery() | Out-Null
        
        $connection.Close()
        
        Write-Host ""
        Write-Host "✓ Tabelas criadas com sucesso!" -ForegroundColor Green
    }
}
catch {
    Write-Host ""
    Write-Host "✗ Erro ao criar tabelas: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Tente executar manualmente:" -ForegroundColor Yellow
    Write-Host "psql -h localhost -U inventario -d sispatrimonio -f sql/criar_tabelas_item_composto.sql" -ForegroundColor Cyan
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Concluído!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
