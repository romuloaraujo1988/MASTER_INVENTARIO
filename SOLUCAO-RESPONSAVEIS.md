# 🔧 Solução: Responsáveis não carregam na tela de Inventário

## 📋 Problema Identificado

O método `ResponsavelDAO.findAll()` filtra apenas responsáveis **ATIVOS**:

```sql
SELECT r.*, s.NOME as NOME_SETOR 
FROM TABELA_RESPONSAVEL r 
LEFT JOIN TABELA_SETOR s ON r.ID_SETOR = s.ID 
WHERE r.ATIVO = TRUE 
ORDER BY r.NOME
```

**Possíveis causas:**
1. ❌ Não existem responsáveis cadastrados
2. ❌ Todos os responsáveis estão com `ATIVO = FALSE`
3. ❌ A coluna `ATIVO` está com valor `NULL`

## 🎯 Soluções

### Solução 1: Verificar no pgAdmin (RECOMENDADO)

1. **Abra o pgAdmin**
2. **Conecte ao banco `sispatrimonio`**
3. **Execute a query:**

```sql
-- Verificar quantos responsáveis existem
SELECT COUNT(*) as total FROM TABELA_RESPONSAVEL;

-- Verificar quantos estão ativos
SELECT COUNT(*) as ativos FROM TABELA_RESPONSAVEL WHERE ATIVO = TRUE;

-- Listar todos com status
SELECT ID, NOME, CARGO, ATIVO FROM TABELA_RESPONSAVEL ORDER BY NOME;
```

### Solução 2: Ativar todos os responsáveis

Se houver responsáveis mas todos estão inativos:

```sql
UPDATE TABELA_RESPONSAVEL 
SET ATIVO = TRUE 
WHERE ATIVO = FALSE OR ATIVO IS NULL;
```

### Solução 3: Inserir responsáveis de exemplo

Se a tabela estiver vazia:

```sql
INSERT INTO TABELA_RESPONSAVEL (NOME, CPF, EMAIL, TELEFONE, CARGO, ATIVO) 
VALUES 
('João Silva', '123.456.789-00', 'joao.silva@ifmt.edu.br', '(65) 3333-4444', 'Administrador', TRUE),
('Maria Santos', '987.654.321-00', 'maria.santos@ifmt.edu.br', '(65) 3333-5555', 'Coordenadora TI', TRUE),
('Pedro Oliveira', '111.222.333-44', 'pedro.oliveira@ifmt.edu.br', '(65) 3333-6666', 'Gerente Financeiro', TRUE),
('Ana Costa', '555.666.777-88', 'ana.costa@ifmt.edu.br', '(65) 3333-7777', 'Supervisora RH', TRUE),
('Carlos Ferreira', '999.888.777-66', 'carlos.ferreira@ifmt.edu.br', '(65) 3333-8888', 'Responsável Almoxarifado', TRUE);
```

### Solução 4: Usar o script SQL criado

Execute o arquivo `verificar-responsaveis.sql` no pgAdmin:

1. Abra o arquivo no pgAdmin
2. Descomente as linhas necessárias
3. Execute o script

## 🔍 Como Verificar se Funcionou

Após executar uma das soluções:

1. **Feche e reabra a tela de Inventário** no sistema desktop
2. **Clique em "Novo Inventário"**
3. **Verifique se o combo "Responsável" está preenchido**

## 📊 Logs de Debug

O sistema já tem logs de debug ativados. Ao abrir a tela de inventário, você verá no console:

```
[DEBUG ResponsavelDAO] Iniciando findAll()
[DEBUG ResponsavelDAO] Quantidade de responsáveis encontrados: X
[DEBUG ResponsavelService] Total retornado do DAO: X
[DEBUG ResponsavelService] Total após filtro isAtivo(): X
[DEBUG] Quantidade de responsáveis encontrados: X
```

Se aparecer `0` em qualquer um desses logs, o problema está confirmado.

## 🚀 Solução Rápida (Copiar e Colar)

**Se você tem acesso ao pgAdmin, execute este script completo:**

```sql
-- 1. Verificar situação atual
SELECT 'Total' as tipo, COUNT(*) as quantidade FROM TABELA_RESPONSAVEL
UNION ALL
SELECT 'Ativos', COUNT(*) FROM TABELA_RESPONSAVEL WHERE ATIVO = TRUE
UNION ALL
SELECT 'Inativos', COUNT(*) FROM TABELA_RESPONSAVEL WHERE ATIVO = FALSE OR ATIVO IS NULL;

-- 2. Ativar todos (se houver inativos)
UPDATE TABELA_RESPONSAVEL SET ATIVO = TRUE WHERE ATIVO = FALSE OR ATIVO IS NULL;

-- 3. Inserir exemplos (se tabela vazia)
INSERT INTO TABELA_RESPONSAVEL (NOME, CPF, EMAIL, TELEFONE, CARGO, ATIVO) 
SELECT 'João Silva', '123.456.789-00', 'joao.silva@ifmt.edu.br', '(65) 3333-4444', 'Administrador', TRUE
WHERE NOT EXISTS (SELECT 1 FROM TABELA_RESPONSAVEL);

INSERT INTO TABELA_RESPONSAVEL (NOME, CPF, EMAIL, TELEFONE, CARGO, ATIVO) 
SELECT 'Maria Santos', '987.654.321-00', 'maria.santos@ifmt.edu.br', '(65) 3333-5555', 'Coordenadora TI', TRUE
WHERE (SELECT COUNT(*) FROM TABELA_RESPONSAVEL) < 2;

-- 4. Verificar resultado
SELECT ID, NOME, CARGO, ATIVO FROM TABELA_RESPONSAVEL WHERE ATIVO = TRUE ORDER BY NOME;
```

## 📝 Notas

- O sistema tem fallback para responsáveis de exemplo se houver erro
- Os logs de debug ajudam a identificar exatamente onde está o problema
- A coluna `ATIVO` deve ser `TRUE` para o responsável aparecer no combo

**Data:** 2025-11-14
**Status:** Solução documentada e pronta para aplicação
