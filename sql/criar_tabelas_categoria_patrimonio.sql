-- =====================================================
-- TABELAS DE CATEGORIA E SUBCATEGORIA DE PATRIMÔNIOS
-- Sistema de Inventário - IFMT
-- Data: 27/11/2025
-- =====================================================

-- Tabela de Categorias Principais
CREATE TABLE IF NOT EXISTS tabela_categoria_patrimonio (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    descricao TEXT,
    icone VARCHAR(50), -- Nome do ícone para UI (ex: 'chair', 'computer', 'book')
    cor VARCHAR(20), -- Cor hexadecimal para UI (ex: '#2196F3')
    ordem_exibicao INTEGER DEFAULT 0,
    ativo BOOLEAN DEFAULT TRUE,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de Subcategorias
CREATE TABLE IF NOT EXISTS tabela_subcategoria_patrimonio (
    id SERIAL PRIMARY KEY,
    id_categoria INTEGER NOT NULL,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    ordem_exibicao INTEGER DEFAULT 0,
    ativo BOOLEAN DEFAULT TRUE,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_categoria) REFERENCES tabela_categoria_patrimonio(id) ON DELETE CASCADE,
    UNIQUE(id_categoria, nome)
);

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_categoria_ativo ON tabela_categoria_patrimonio(ativo);
CREATE INDEX IF NOT EXISTS idx_categoria_ordem ON tabela_categoria_patrimonio(ordem_exibicao);
CREATE INDEX IF NOT EXISTS idx_subcategoria_categoria ON tabela_subcategoria_patrimonio(id_categoria);
CREATE INDEX IF NOT EXISTS idx_subcategoria_ativo ON tabela_subcategoria_patrimonio(ativo);

-- Comentários nas tabelas
COMMENT ON TABLE tabela_categoria_patrimonio IS 'Categorias principais de patrimônios (Mobiliário, Informática, etc)';
COMMENT ON TABLE tabela_subcategoria_patrimonio IS 'Subcategorias de patrimônios (Cadeira, Desktop, etc)';

COMMENT ON COLUMN tabela_categoria_patrimonio.icone IS 'Nome do ícone Material Design para exibição no app';
COMMENT ON COLUMN tabela_categoria_patrimonio.cor IS 'Cor em hexadecimal para identificação visual';
COMMENT ON COLUMN tabela_categoria_patrimonio.ordem_exibicao IS 'Ordem de exibição nas listas (menor = primeiro)';

-- =====================================================
-- INSERIR CATEGORIAS PRINCIPAIS
-- =====================================================

INSERT INTO tabela_categoria_patrimonio (nome, descricao, icone, cor, ordem_exibicao) VALUES
('MOBILIÁRIO', 'Móveis e mobiliário em geral', 'chair', '#795548', 1),
('INFORMÁTICA', 'Equipamentos de informática e tecnologia', 'computer', '#2196F3', 2),
('AUDIOVISUAL', 'Equipamentos audiovisuais e multimídia', 'videocam', '#9C27B0', 3),
('LABORATÓRIO', 'Equipamentos e instrumentos de laboratório', 'science', '#4CAF50', 4),
('CLIMATIZAÇÃO', 'Equipamentos de climatização e ventilação', 'ac_unit', '#00BCD4', 5),
('ACERVO', 'Acervo bibliográfico e publicações', 'book', '#FF9800', 6),
('VEÍCULO', 'Veículos automotores', 'directions_car', '#F44336', 7),
('ELETRODOMÉSTICO', 'Eletrodomésticos e eletroeletrônicos', 'kitchen', '#607D8B', 8),
('OUTROS', 'Outros patrimônios não classificados', 'category', '#9E9E9E', 99)
ON CONFLICT (nome) DO NOTHING;

-- =====================================================
-- INSERIR SUBCATEGORIAS - MOBILIÁRIO
-- =====================================================

INSERT INTO tabela_subcategoria_patrimonio (id_categoria, nome, descricao, ordem_exibicao)
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
FROM tabela_categoria_patrimonio WHERE nome = 'MOBILIÁRIO'
ON CONFLICT (id_categoria, nome) DO NOTHING;

-- =====================================================
-- INSERIR SUBCATEGORIAS - INFORMÁTICA
-- =====================================================

INSERT INTO tabela_subcategoria_patrimonio (id_categoria, nome, descricao, ordem_exibicao)
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
FROM tabela_categoria_patrimonio WHERE nome = 'INFORMÁTICA'
ON CONFLICT (id_categoria, nome) DO NOTHING;

-- =====================================================
-- INSERIR SUBCATEGORIAS - AUDIOVISUAL
-- =====================================================

INSERT INTO tabela_subcategoria_patrimonio (id_categoria, nome, descricao, ordem_exibicao)
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
FROM tabela_categoria_patrimonio WHERE nome = 'AUDIOVISUAL'
ON CONFLICT (id_categoria, nome) DO NOTHING;

-- =====================================================
-- INSERIR SUBCATEGORIAS - LABORATÓRIO
-- =====================================================

INSERT INTO tabela_subcategoria_patrimonio (id_categoria, nome, descricao, ordem_exibicao)
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
FROM tabela_categoria_patrimonio WHERE nome = 'LABORATÓRIO'
ON CONFLICT (id_categoria, nome) DO NOTHING;

-- =====================================================
-- INSERIR SUBCATEGORIAS - CLIMATIZAÇÃO
-- =====================================================

INSERT INTO tabela_subcategoria_patrimonio (id_categoria, nome, descricao, ordem_exibicao)
SELECT id, 'Ar Condicionado', 'Ar condicionado split, janela e piso-teto', 1
FROM tabela_categoria_patrimonio WHERE nome = 'CLIMATIZAÇÃO'
UNION ALL
SELECT id, 'Ventilador', 'Ventiladores de teto, parede e coluna', 2
FROM tabela_categoria_patrimonio WHERE nome = 'CLIMATIZAÇÃO'
UNION ALL
SELECT id, 'Outros', 'Outros equipamentos de climatização', 99
FROM tabela_categoria_patrimonio WHERE nome = 'CLIMATIZAÇÃO'
ON CONFLICT (id_categoria, nome) DO NOTHING;

-- =====================================================
-- INSERIR SUBCATEGORIAS - ACERVO
-- =====================================================

INSERT INTO tabela_subcategoria_patrimonio (id_categoria, nome, descricao, ordem_exibicao)
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
FROM tabela_categoria_patrimonio WHERE nome = 'ACERVO'
ON CONFLICT (id_categoria, nome) DO NOTHING;

-- =====================================================
-- INSERIR SUBCATEGORIAS - VEÍCULO
-- =====================================================

INSERT INTO tabela_subcategoria_patrimonio (id_categoria, nome, descricao, ordem_exibicao)
SELECT id, 'Veículo Leve', 'Carros, vans e utilitários', 1
FROM tabela_categoria_patrimonio WHERE nome = 'VEÍCULO'
UNION ALL
SELECT id, 'Veículo Pesado', 'Ônibus, caminhões e tratores', 2
FROM tabela_categoria_patrimonio WHERE nome = 'VEÍCULO'
UNION ALL
SELECT id, 'Outros', 'Outros veículos', 99
FROM tabela_categoria_patrimonio WHERE nome = 'VEÍCULO'
ON CONFLICT (id_categoria, nome) DO NOTHING;

-- =====================================================
-- INSERIR SUBCATEGORIAS - ELETRODOMÉSTICO
-- =====================================================

INSERT INTO tabela_subcategoria_patrimonio (id_categoria, nome, descricao, ordem_exibicao)
SELECT id, 'Refrigeração', 'Geladeiras, freezers e bebedouros', 1
FROM tabela_categoria_patrimonio WHERE nome = 'ELETRODOMÉSTICO'
UNION ALL
SELECT id, 'Aquecimento', 'Micro-ondas, fornos e cafeteiras', 2
FROM tabela_categoria_patrimonio WHERE nome = 'ELETRODOMÉSTICO'
UNION ALL
SELECT id, 'Outros', 'Outros eletrodomésticos', 99
FROM tabela_categoria_patrimonio WHERE nome = 'ELETRODOMÉSTICO'
ON CONFLICT (id_categoria, nome) DO NOTHING;

-- =====================================================
-- INSERIR SUBCATEGORIAS - OUTROS
-- =====================================================

INSERT INTO tabela_subcategoria_patrimonio (id_categoria, nome, descricao, ordem_exibicao)
SELECT id, 'Sem Subcategoria', 'Itens sem subcategoria definida', 1
FROM tabela_categoria_patrimonio WHERE nome = 'OUTROS'
ON CONFLICT (id_categoria, nome) DO NOTHING;

-- =====================================================
-- VIEWS ÚTEIS
-- =====================================================

-- View para listar categorias com contagem de subcategorias
CREATE OR REPLACE VIEW view_categorias_com_contagem AS
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
LEFT JOIN tabela_subcategoria_patrimonio s ON c.id = s.id_categoria AND s.ativo = TRUE
WHERE c.ativo = TRUE
GROUP BY c.id, c.nome, c.descricao, c.icone, c.cor, c.ordem_exibicao, c.ativo
ORDER BY c.ordem_exibicao;

-- View para listar todas as categorias e subcategorias
CREATE OR REPLACE VIEW view_categorias_subcategorias AS
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
WHERE c.ativo = TRUE AND (s.ativo = TRUE OR s.id IS NULL)
ORDER BY c.ordem_exibicao, s.ordem_exibicao;

-- =====================================================
-- TRIGGER PARA ATUALIZAR data_atualizacao
-- =====================================================

CREATE OR REPLACE FUNCTION atualizar_data_atualizacao()
RETURNS TRIGGER AS $$
BEGIN
    NEW.data_atualizacao = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_atualizar_categoria
BEFORE UPDATE ON tabela_categoria_patrimonio
FOR EACH ROW
EXECUTE FUNCTION atualizar_data_atualizacao();

CREATE TRIGGER trigger_atualizar_subcategoria
BEFORE UPDATE ON tabela_subcategoria_patrimonio
FOR EACH ROW
EXECUTE FUNCTION atualizar_data_atualizacao();

-- =====================================================
-- GRANTS (ajustar conforme necessário)
-- =====================================================

-- GRANT SELECT, INSERT, UPDATE ON tabela_categoria_patrimonio TO usuario_app;
-- GRANT SELECT, INSERT, UPDATE ON tabela_subcategoria_patrimonio TO usuario_app;
-- GRANT SELECT ON view_categorias_com_contagem TO usuario_app;
-- GRANT SELECT ON view_categorias_subcategorias TO usuario_app;

-- =====================================================
-- SCRIPT CONCLUÍDO
-- =====================================================

-- Verificar dados inseridos
SELECT 'Categorias criadas:' as info, COUNT(*) as total FROM tabela_categoria_patrimonio;
SELECT 'Subcategorias criadas:' as info, COUNT(*) as total FROM tabela_subcategoria_patrimonio;

-- Listar todas as categorias e subcategorias
SELECT * FROM view_categorias_subcategorias;
