-- =====================================================
-- TABELAS DE CATEGORIA E SUBCATEGORIA DE PATRIMÔNIOS
-- Sistema de Inventário - IFMT (SQLite - Modo Offline)
-- Data: 27/11/2025
-- =====================================================

-- Tabela de Categorias Principais
CREATE TABLE IF NOT EXISTS tabela_categoria_patrimonio (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL UNIQUE,
    descricao TEXT,
    icone TEXT, -- Nome do ícone para UI (ex: 'chair', 'computer', 'book')
    cor TEXT, -- Cor hexadecimal para UI (ex: '#2196F3')
    ordem_exibicao INTEGER DEFAULT 0,
    ativo INTEGER DEFAULT 1, -- SQLite não tem BOOLEAN, usa 0/1
    data_criacao TEXT DEFAULT (datetime('now', 'localtime')),
    data_atualizacao TEXT DEFAULT (datetime('now', 'localtime'))
);

-- Tabela de Subcategorias
CREATE TABLE IF NOT EXISTS tabela_subcategoria_patrimonio (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    id_categoria INTEGER NOT NULL,
    nome TEXT NOT NULL,
    descricao TEXT,
    ordem_exibicao INTEGER DEFAULT 0,
    ativo INTEGER DEFAULT 1,
    data_criacao TEXT DEFAULT (datetime('now', 'localtime')),
    data_atualizacao TEXT DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (id_categoria) REFERENCES tabela_categoria_patrimonio(id) ON DELETE CASCADE,
    UNIQUE(id_categoria, nome)
);

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_categoria_ativo ON tabela_categoria_patrimonio(ativo);
CREATE INDEX IF NOT EXISTS idx_categoria_ordem ON tabela_categoria_patrimonio(ordem_exibicao);
CREATE INDEX IF NOT EXISTS idx_subcategoria_categoria ON tabela_subcategoria_patrimonio(id_categoria);
CREATE INDEX IF NOT EXISTS idx_subcategoria_ativo ON tabela_subcategoria_patrimonio(ativo);

-- =====================================================
-- INSERIR CATEGORIAS PRINCIPAIS
-- =====================================================

INSERT OR IGNORE INTO tabela_categoria_patrimonio (nome, descricao, icone, cor, ordem_exibicao) VALUES
('MOBILIÁRIO', 'Móveis e mobiliário em geral', 'chair', '#795548', 1),
('INFORMÁTICA', 'Equipamentos de informática e tecnologia', 'computer', '#2196F3', 2),
('AUDIOVISUAL', 'Equipamentos audiovisuais e multimídia', 'videocam', '#9C27B0', 3),
('LABORATÓRIO', 'Equipamentos e instrumentos de laboratório', 'science', '#4CAF50', 4),
('CLIMATIZAÇÃO', 'Equipamentos de climatização e ventilação', 'ac_unit', '#00BCD4', 5),
('ACERVO', 'Acervo bibliográfico e publicações', 'book', '#FF9800', 6),
('VEÍCULO', 'Veículos automotores', 'directions_car', '#F44336', 7),
('ELETRODOMÉSTICO', 'Eletrodomésticos e eletroeletrônicos', 'kitchen', '#607D8B', 8),
('OUTROS', 'Outros patrimônios não classificados', 'category', '#9E9E9E', 99);

-- =====================================================
-- INSERIR SUBCATEGORIAS - MOBILIÁRIO
-- =====================================================

INSERT OR IGNORE INTO tabela_subcategoria_patrimonio (id_categoria, nome, descricao, ordem_exibicao)
SELECT id, 'Cadeira', 'Cadeiras fixas, giratórias e de laboratório', 1
FROM tabela_categoria_patrimonio WHERE nome = 'MOBILIÁRIO'
UNION ALL
SELECT id, 'Mesa', 'Mesas de escritório, desenho e impressora', 2
FROM tabela_categoria_patrimonio WHERE nome = 'MOBILIÁRIO'
UNION ALL
SELECT id, 'Conjunto Escolar', 'Conjuntos escolares (mesa + cadeira)', 3
FROM tabela_categoria_patrimonio WHERE nome = 'MOBILIÁRIO'
UNION ALL
SELECT id, 'Armário/Estante', 'Armários, estantes e guarda-volumes', 4
FROM tabela_categoria_patrimonio WHERE nome = 'MOBILIÁRIO'
UNION ALL
SELECT id, 'Poltrona', 'Poltronas operacionais e giratórias', 5
FROM tabela_categoria_patrimonio WHERE nome = 'MOBILIÁRIO'
UNION ALL
SELECT id, 'Banqueta', 'Banquetas altas e giratórias', 6
FROM tabela_categoria_patrimonio WHERE nome = 'MOBILIÁRIO'
UNION ALL
SELECT id, 'Outros', 'Outros móveis não classificados', 99
FROM tabela_categoria_patrimonio WHERE nome = 'MOBILIÁRIO';

-- =====================================================
-- INSERIR SUBCATEGORIAS - INFORMÁTICA
-- =====================================================

INSERT OR IGNORE INTO tabela_subcategoria_patrimonio (id_categoria, nome, descricao, ordem_exibicao)
SELECT id, 'Desktop', 'Computadores desktop e CPUs', 1
FROM tabela_categoria_patrimonio WHERE nome = 'INFORMÁTICA'
UNION ALL
SELECT id, 'Notebook', 'Notebooks e chromebooks', 2
FROM tabela_categoria_patrimonio WHERE nome = 'INFORMÁTICA'
UNION ALL
SELECT id, 'Monitor', 'Monitores e telas', 3
FROM tabela_categoria_patrimonio WHERE nome = 'INFORMÁTICA'
UNION ALL
SELECT id, 'Equipamento de Rede', 'Roteadores, switches e access points', 4
FROM tabela_categoria_patrimonio WHERE nome = 'INFORMÁTICA'
UNION ALL
SELECT id, 'Impressora/Scanner', 'Impressoras, scanners e multifuncionais', 5
FROM tabela_categoria_patrimonio WHERE nome = 'INFORMÁTICA'
UNION ALL
SELECT id, 'Webcam', 'Webcams e câmeras', 6
FROM tabela_categoria_patrimonio WHERE nome = 'INFORMÁTICA'
UNION ALL
SELECT id, 'Periféricos', 'Mouse, teclado, mesa digitalizadora', 7
FROM tabela_categoria_patrimonio WHERE nome = 'INFORMÁTICA'
UNION ALL
SELECT id, 'Outros', 'Outros equipamentos de informática', 99
FROM tabela_categoria_patrimonio WHERE nome = 'INFORMÁTICA';

-- =====================================================
-- INSERIR SUBCATEGORIAS - AUDIOVISUAL
-- =====================================================

INSERT OR IGNORE INTO tabela_subcategoria_patrimonio (id_categoria, nome, descricao, ordem_exibicao)
SELECT id, 'Projetor', 'Projetores e datashows', 1
FROM tabela_categoria_patrimonio WHERE nome = 'AUDIOVISUAL'
UNION ALL
SELECT id, 'Tela de Projeção', 'Telas de projeção retráteis e fixas', 2
FROM tabela_categoria_patrimonio WHERE nome = 'AUDIOVISUAL'
UNION ALL
SELECT id, 'Quadro/Lousa', 'Quadros brancos, lousas e murais', 3
FROM tabela_categoria_patrimonio WHERE nome = 'AUDIOVISUAL'
UNION ALL
SELECT id, 'Caixa de Som', 'Caixas de som e sistemas de áudio', 4
FROM tabela_categoria_patrimonio WHERE nome = 'AUDIOVISUAL'
UNION ALL
SELECT id, 'Outros', 'Outros equipamentos audiovisuais', 99
FROM tabela_categoria_patrimonio WHERE nome = 'AUDIOVISUAL';

-- =====================================================
-- INSERIR SUBCATEGORIAS - LABORATÓRIO
-- =====================================================

INSERT OR IGNORE INTO tabela_subcategoria_patrimonio (id_categoria, nome, descricao, ordem_exibicao)
SELECT id, 'Microscópio', 'Microscópios biológicos e estereoscópicos', 1
FROM tabela_categoria_patrimonio WHERE nome = 'LABORATÓRIO'
UNION ALL
SELECT id, 'Instrumento de Medição', 'Paquímetros, balanças e medidores', 2
FROM tabela_categoria_patrimonio WHERE nome = 'LABORATÓRIO'
UNION ALL
SELECT id, 'Vidraria', 'Vidrarias e recipientes de laboratório', 3
FROM tabela_categoria_patrimonio WHERE nome = 'LABORATÓRIO'
UNION ALL
SELECT id, 'Equipamento Específico', 'Equipamentos específicos de laboratório', 4
FROM tabela_categoria_patrimonio WHERE nome = 'LABORATÓRIO'
UNION ALL
SELECT id, 'Outros', 'Outros equipamentos de laboratório', 99
FROM tabela_categoria_patrimonio WHERE nome = 'LABORATÓRIO';

-- =====================================================
-- INSERIR SUBCATEGORIAS - CLIMATIZAÇÃO
-- =====================================================

INSERT OR IGNORE INTO tabela_subcategoria_patrimonio (id_categoria, nome, descricao, ordem_exibicao)
SELECT id, 'Ar Condicionado', 'Ar condicionado split, janela e piso-teto', 1
FROM tabela_categoria_patrimonio WHERE nome = 'CLIMATIZAÇÃO'
UNION ALL
SELECT id, 'Ventilador', 'Ventiladores de teto, parede e coluna', 2
FROM tabela_categoria_patrimonio WHERE nome = 'CLIMATIZAÇÃO'
UNION ALL
SELECT id, 'Outros', 'Outros equipamentos de climatização', 99
FROM tabela_categoria_patrimonio WHERE nome = 'CLIMATIZAÇÃO';

-- =====================================================
-- INSERIR SUBCATEGORIAS - ACERVO
-- =====================================================

INSERT OR IGNORE INTO tabela_subcategoria_patrimonio (id_categoria, nome, descricao, ordem_exibicao)
SELECT id, 'Livro', 'Livros didáticos e técnicos', 1
FROM tabela_categoria_patrimonio WHERE nome = 'ACERVO'
UNION ALL
SELECT id, 'Dicionário', 'Dicionários e enciclopédias', 2
FROM tabela_categoria_patrimonio WHERE nome = 'ACERVO'
UNION ALL
SELECT id, 'Revista', 'Revistas e periódicos', 3
FROM tabela_categoria_patrimonio WHERE nome = 'ACERVO'
UNION ALL
SELECT id, 'Outros', 'Outras publicações', 99
FROM tabela_categoria_patrimonio WHERE nome = 'ACERVO';

-- =====================================================
-- INSERIR SUBCATEGORIAS - VEÍCULO
-- =====================================================

INSERT OR IGNORE INTO tabela_subcategoria_patrimonio (id_categoria, nome, descricao, ordem_exibicao)
SELECT id, 'Veículo Leve', 'Carros, vans e utilitários', 1
FROM tabela_categoria_patrimonio WHERE nome = 'VEÍCULO'
UNION ALL
SELECT id, 'Veículo Pesado', 'Ônibus, caminhões e tratores', 2
FROM tabela_categoria_patrimonio WHERE nome = 'VEÍCULO'
UNION ALL
SELECT id, 'Outros', 'Outros veículos', 99
FROM tabela_categoria_patrimonio WHERE nome = 'VEÍCULO';

-- =====================================================
-- INSERIR SUBCATEGORIAS - ELETRODOMÉSTICO
-- =====================================================

INSERT OR IGNORE INTO tabela_subcategoria_patrimonio (id_categoria, nome, descricao, ordem_exibicao)
SELECT id, 'Refrigeração', 'Geladeiras, freezers e bebedouros', 1
FROM tabela_categoria_patrimonio WHERE nome = 'ELETRODOMÉSTICO'
UNION ALL
SELECT id, 'Aquecimento', 'Micro-ondas, fornos e cafeteiras', 2
FROM tabela_categoria_patrimonio WHERE nome = 'ELETRODOMÉSTICO'
UNION ALL
SELECT id, 'Outros', 'Outros eletrodomésticos', 99
FROM tabela_categoria_patrimonio WHERE nome = 'ELETRODOMÉSTICO';

-- =====================================================
-- INSERIR SUBCATEGORIAS - OUTROS
-- =====================================================

INSERT OR IGNORE INTO tabela_subcategoria_patrimonio (id_categoria, nome, descricao, ordem_exibicao)
SELECT id, 'Sem Subcategoria', 'Itens sem subcategoria definida', 1
FROM tabela_categoria_patrimonio WHERE nome = 'OUTROS';

-- =====================================================
-- VIEWS ÚTEIS (SQLite)
-- =====================================================

-- View para listar categorias com contagem de subcategorias
CREATE VIEW IF NOT EXISTS view_categorias_com_contagem AS
SELECT 
    c.id,
    c.nome,
    c.descricao,
    c.icone,
    c.cor,
    c.ordem_exibicao,
    c.ativo,
    COUNT(s.id) as total_subcategorias
FROM tabela_categoria_patrimonio c
LEFT JOIN tabela_subcategoria_patrimonio s ON c.id = s.id_categoria AND s.ativo = 1
WHERE c.ativo = 1
GROUP BY c.id, c.nome, c.descricao, c.icone, c.cor, c.ordem_exibicao, c.ativo
ORDER BY c.ordem_exibicao;

-- View para listar todas as categorias e subcategorias
CREATE VIEW IF NOT EXISTS view_categorias_subcategorias AS
SELECT 
    c.id as categoria_id,
    c.nome as categoria_nome,
    c.icone as categoria_icone,
    c.cor as categoria_cor,
    s.id as subcategoria_id,
    s.nome as subcategoria_nome,
    s.descricao as subcategoria_descricao,
    c.ordem_exibicao as categoria_ordem,
    s.ordem_exibicao as subcategoria_ordem
FROM tabela_categoria_patrimonio c
LEFT JOIN tabela_subcategoria_patrimonio s ON c.id = s.id_categoria
WHERE c.ativo = 1 AND (s.ativo = 1 OR s.id IS NULL)
ORDER BY c.ordem_exibicao, s.ordem_exibicao;

-- =====================================================
-- TRIGGERS PARA ATUALIZAR data_atualizacao (SQLite)
-- =====================================================

CREATE TRIGGER IF NOT EXISTS trigger_atualizar_categoria
AFTER UPDATE ON tabela_categoria_patrimonio
FOR EACH ROW
BEGIN
    UPDATE tabela_categoria_patrimonio 
    SET data_atualizacao = datetime('now', 'localtime')
    WHERE id = NEW.id;
END;

CREATE TRIGGER IF NOT EXISTS trigger_atualizar_subcategoria
AFTER UPDATE ON tabela_subcategoria_patrimonio
FOR EACH ROW
BEGIN
    UPDATE tabela_subcategoria_patrimonio 
    SET data_atualizacao = datetime('now', 'localtime')
    WHERE id = NEW.id;
END;

-- =====================================================
-- SCRIPT CONCLUÍDO
-- =====================================================

-- Verificar dados inseridos
SELECT 'Categorias criadas:' as info, COUNT(*) as total FROM tabela_categoria_patrimonio;
SELECT 'Subcategorias criadas:' as info, COUNT(*) as total FROM tabela_subcategoria_patrimonio;

-- Listar todas as categorias e subcategorias
SELECT * FROM view_categorias_subcategorias;
