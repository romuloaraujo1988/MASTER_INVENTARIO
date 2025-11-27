# 📋 Instruções para Criar Tabelas de Categoria

## Opção 1: Usando pgAdmin (Recomendado)

1. Abra o **pgAdmin**
2. Conecte ao banco `sispatrimonio`
3. Clique com botão direito no banco → **Query Tool**
4. Abra o arquivo: `sql/criar_tabelas_categoria_patrimonio.sql`
5. Clique em **Execute** (F5)
6. Verifique os resultados na aba **Messages**

---

## Opção 2: Usando DBeaver

1. Abra o **DBeaver**
2. Conecte ao banco `sispatrimonio`
3. Clique em **SQL Editor** → **New SQL Script**
4. Abra o arquivo: `sql/criar_tabelas_categoria_patrimonio.sql`
5. Clique em **Execute SQL Script** (Ctrl+Enter)
6. Verifique os resultados

---

## Opção 3: Linha de Comando (se psql estiver instalado)

```bash
# Windows
SET PGPASSWORD=inventario
psql -h localhost -U inventario -d sispatrimonio -f sql\criar_tabelas_categoria_patrimonio.sql

# Linux/Mac
PGPASSWORD=inventario psql -h localhost -U inventario -d sispatrimonio -f sql/criar_tabelas_categoria_patrimonio.sql
```

---

## Opção 4: Copiar e Colar SQL Direto

Se preferir, copie e cole este SQL diretamente no Query Tool:

```sql
-- =====================================================
-- CRIAR TABELA DE CATEGORIAS
-- =====================================================

CREATE TABLE IF NOT EXISTS tabela_categoria_patrimonio (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    descricao TEXT,
    icone VARCHAR(50),
    cor VARCHAR(20),
    ordem_exibicao INTEGER DEFAULT 0,
    ativo BOOLEAN DEFAULT TRUE,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- CRIAR TABELA DE SUBCATEGORIAS
-- =====================================================

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

-- =====================================================
-- CRIAR ÍNDICES
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_categoria_ativo ON tabela_categoria_patrimonio(ativo);
CREATE INDEX IF NOT EXISTS idx_categoria_ordem ON tabela_categoria_patrimonio(ordem_exibicao);
CREATE INDEX IF NOT EXISTS idx_subcategoria_categoria ON tabela_subcategoria_patrimonio(id_categoria);
CREATE INDEX IF NOT EXISTS idx_subcategoria_ativo ON tabela_subcategoria_patrimonio(ativo);

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
SELECT id, 'Cadeira', 'Cadeiras fixas, giratórias e de laboratório', 1 FROM tabela_categoria_patrimonio WHERE nome = 'MOBILIÁRIO'
UNION ALL SELECT id, 'Mesa', 'Mesas de escritório, desenho e impressora', 2 FROM tabela_categoria_patrimonio WHERE nome = 'MOBILIÁRIO'
UNION ALL SELECT id, 'Conjunto Escolar', 'Conjuntos escolares (mesa + cadeira)', 3 FROM tabela_categoria_patrimonio WHERE nome = 'MOBILIÁRIO'
UNION ALL SELECT id, 'Armário/Estante', 'Armários, estantes e guarda-volumes', 4 FROM tabela_categoria_patrimonio WHERE nome = 'MOBILIÁRIO'
UNION ALL SELECT id, 'Poltrona', 'Poltronas operacionais e giratórias', 5 FROM tabela_categoria_patrimonio WHERE nome = 'MOBILIÁRIO'
UNION ALL SELECT id, 'Banqueta', 'Banquetas altas e giratórias', 6 FROM tabela_categoria_patrimonio WHERE nome = 'MOBILIÁRIO'
UNION ALL SELECT id, 'Outros', 'Outros móveis não classificados', 99 FROM tabela_categoria_patrimonio WHERE nome = 'MOBILIÁRIO'
ON CONFLICT (id_categoria, nome) DO NOTHING;

-- =====================================================
-- INSERIR SUBCATEGORIAS - INFORMÁTICA
-- =====================================================

INSERT INTO tabela_subcategoria_patrimonio (id_categoria, nome, descricao, ordem_exibicao)
SELECT id, 'Desktop', 'Computadores desktop e CPUs', 1 FROM tabela_categoria_patrimonio WHERE nome = 'INFORMÁTICA'
UNION ALL SELECT id, 'Notebook', 'Notebooks e chromebooks', 2 FROM tabela_categoria_patrimonio WHERE nome = 'INFORMÁTICA'
UNION ALL SELECT id, 'Monitor', 'Monitores e telas', 3 FROM tabela_categoria_patrimonio WHERE nome = 'INFORMÁTICA'
UNION ALL SELECT id, 'Equipamento de Rede', 'Roteadores, switches e access points', 4 FROM tabela_categoria_patrimonio WHERE nome = 'INFORMÁTICA'
UNION ALL SELECT id, 'Impressora/Scanner', 'Impressoras, scanners e multifuncionais', 5 FROM tabela_categoria_patrimonio WHERE nome = 'INFORMÁTICA'
UNION ALL SELECT id, 'Webcam', 'Webcams e câmeras', 6 FROM tabela_categoria_patrimonio WHERE nome = 'INFORMÁTICA'
UNION ALL SELECT id, 'Periféricos', 'Mouse, teclado, mesa digitalizadora', 7 FROM tabela_categoria_patrimonio WHERE nome = 'INFORMÁTICA'
UNION ALL SELECT id, 'Outros', 'Outros equipamentos de informática', 99 FROM tabela_categoria_patrimonio WHERE nome = 'INFORMÁTICA'
ON CONFLICT (id_categoria, nome) DO NOTHING;

-- =====================================================
-- INSERIR SUBCATEGORIAS - AUDIOVISUAL
-- =====================================================

INSERT INTO tabela_subcategoria_patrimonio (id_categoria, nome, descricao, ordem_exibicao)
SELECT id, 'Projetor', 'Projetores e datashows', 1 FROM tabela_categoria_patrimonio WHERE nome = 'AUDIOVISUAL'
UNION ALL SELECT id, 'Tela de Projeção', 'Telas de projeção retráteis e fixas', 2 FROM tabela_categoria_patrimonio WHERE nome = 'AUDIOVISUAL'
UNION ALL SELECT id, 'Quadro/Lousa', 'Quadros brancos, lousas e murais', 3 FROM tabela_categoria_patrimonio WHERE nome = 'AUDIOVISUAL'
UNION ALL SELECT id, 'Caixa de Som', 'Caixas de som e sistemas de áudio', 4 FROM tabela_categoria_patrimonio WHERE nome = 'AUDIOVISUAL'
UNION ALL SELECT id, 'Outros', 'Outros equipamentos audiovisuais', 99 FROM tabela_categoria_patrimonio WHERE nome = 'AUDIOVISUAL'
ON CONFLICT (id_categoria, nome) DO NOTHING;

-- =====================================================
-- INSERIR SUBCATEGORIAS - LABORATÓRIO
-- =====================================================

INSERT INTO tabela_subcategoria_patrimonio (id_categoria, nome, descricao, ordem_exibicao)
SELECT id, 'Microscópio', 'Microscópios biológicos e estereoscópicos', 1 FROM tabela_categoria_patrimonio WHERE nome = 'LABORATÓRIO'
UNION ALL SELECT id, 'Instrumento de Medição', 'Paquímetros, balanças e medidores', 2 FROM tabela_categoria_patrimonio WHERE nome = 'LABORATÓRIO'
UNION ALL SELECT id, 'Vidraria', 'Vidrarias e recipientes de laboratório', 3 FROM tabela_categoria_patrimonio WHERE nome = 'LABORATÓRIO'
UNION ALL SELECT id, 'Equipamento Específico', 'Equipamentos específicos de laboratório', 4 FROM tabela_categoria_patrimonio WHERE nome = 'LABORATÓRIO'
UNION ALL SELECT id, 'Outros', 'Outros equipamentos de laboratório', 99 FROM tabela_categoria_patrimonio WHERE nome = 'LABORATÓRIO'
ON CONFLICT (id_categoria, nome) DO NOTHING;

-- =====================================================
-- INSERIR SUBCATEGORIAS - CLIMATIZAÇÃO
-- =====================================================

INSERT INTO tabela_subcategoria_patrimonio (id_categoria, nome, descricao, ordem_exibicao)
SELECT id, 'Ar Condicionado', 'Ar condicionado split, janela e piso-teto', 1 FROM tabela_categoria_patrimonio WHERE nome = 'CLIMATIZAÇÃO'
UNION ALL SELECT id, 'Ventilador', 'Ventiladores de teto, parede e coluna', 2 FROM tabela_categoria_patrimonio WHERE nome = 'CLIMATIZAÇÃO'
UNION ALL SELECT id, 'Outros', 'Outros equipamentos de climatização', 99 FROM tabela_categoria_patrimonio WHERE nome = 'CLIMATIZAÇÃO'
ON CONFLICT (id_categoria, nome) DO NOTHING;

-- =====================================================
-- INSERIR SUBCATEGORIAS - ACERVO
-- =====================================================

INSERT INTO tabela_subcategoria_patrimonio (id_categoria, nome, descricao, ordem_exibicao)
SELECT id, 'Livro', 'Livros didáticos e técnicos', 1 FROM tabela_categoria_patrimonio WHERE nome = 'ACERVO'
UNION ALL SELECT id, 'Dicionário', 'Dicionários e enciclopédias', 2 FROM tabela_categoria_patrimonio WHERE nome = 'ACERVO'
UNION ALL SELECT id, 'Revista', 'Revistas e periódicos', 3 FROM tabela_categoria_patrimonio WHERE nome = 'ACERVO'
UNION ALL SELECT id, 'Outros', 'Outras publicações', 99 FROM tabela_categoria_patrimonio WHERE nome = 'ACERVO'
ON CONFLICT (id_categoria, nome) DO NOTHING;

-- =====================================================
-- INSERIR SUBCATEGORIAS - VEÍCULO
-- =====================================================

INSERT INTO tabela_subcategoria_patrimonio (id_categoria, nome, descricao, ordem_exibicao)
SELECT id, 'Veículo Leve', 'Carros, vans e utilitários', 1 FROM tabela_categoria_patrimonio WHERE nome = 'VEÍCULO'
UNION ALL SELECT id, 'Veículo Pesado', 'Ônibus, caminhões e tratores', 2 FROM tabela_categoria_patrimonio WHERE nome = 'VEÍCULO'
UNION ALL SELECT id, 'Outros', 'Outros veículos', 99 FROM tabela_categoria_patrimonio WHERE nome = 'VEÍCULO'
ON CONFLICT (id_categoria, nome) DO NOTHING;

-- =====================================================
-- INSERIR SUBCATEGORIAS - ELETRODOMÉSTICO
-- =====================================================

INSERT INTO tabela_subcategoria_patrimonio (id_categoria, nome, descricao, ordem_exibicao)
SELECT id, 'Refrigeração', 'Geladeiras, freezers e bebedouros', 1 FROM tabela_categoria_patrimonio WHERE nome = 'ELETRODOMÉSTICO'
UNION ALL SELECT id, 'Aquecimento', 'Micro-ondas, fornos e cafeteiras', 2 FROM tabela_categoria_patrimonio WHERE nome = 'ELETRODOMÉSTICO'
UNION ALL SELECT id, 'Outros', 'Outros eletrodomésticos', 99 FROM tabela_categoria_patrimonio WHERE nome = 'ELETRODOMÉSTICO'
ON CONFLICT (id_categoria, nome) DO NOTHING;

-- =====================================================
-- INSERIR SUBCATEGORIAS - OUTROS
-- =====================================================

INSERT INTO tabela_subcategoria_patrimonio (id_categoria, nome, descricao, ordem_exibicao)
SELECT id, 'Sem Subcategoria', 'Itens sem subcategoria definida', 1 FROM tabela_categoria_patrimonio WHERE nome = 'OUTROS'
ON CONFLICT (id_categoria, nome) DO NOTHING;

-- =====================================================
-- CRIAR VIEWS
-- =====================================================

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
-- VERIFICAR RESULTADO
-- =====================================================

SELECT 'Categorias criadas:' as info, COUNT(*) as total FROM tabela_categoria_patrimonio;
SELECT 'Subcategorias criadas:' as info, COUNT(*) as total FROM tabela_subcategoria_patrimonio;

-- Listar categorias com subcategorias
SELECT nome, total_subcategorias FROM view_categorias_com_contagem ORDER BY ordem_exibicao;
```

---

## ✅ Verificar se Funcionou

Após executar, rode esta query para verificar:

```sql
SELECT nome, total_subcategorias 
FROM view_categorias_com_contagem 
ORDER BY ordem_exibicao;
```

**Resultado esperado:**
```
MOBILIÁRIO          | 7
INFORMÁTICA         | 8
AUDIOVISUAL         | 5
LABORATÓRIO         | 5
CLIMATIZAÇÃO        | 3
ACERVO              | 4
VEÍCULO             | 3
ELETRODOMÉSTICO     | 3
OUTROS              | 1
```

---

## 🆘 Problemas?

### Erro: "relation already exists"
As tabelas já existem. Tudo certo!

### Erro: "permission denied"
Você precisa de permissões de CREATE TABLE no banco.

### Erro: "database does not exist"
Verifique se está conectado ao banco `sispatrimonio`.

---

**Criado em:** 27/11/2025
