# Problema: ComboBox de Responsáveis não carrega no formulário de Inventário

## Sintoma

No formulário de criação/edição de inventário (`InventarioFormDialog`), o combobox de responsáveis não está carregando os dados do banco de dados.

## Diagnóstico

### 1. Fluxo de Carregamento

```java
// InventarioFormDialog.java - linha ~180
private void carregarResponsaveis() {
    try {
        comboResponsavel.addItem("Selecione um responsável...");
        
        ResponsavelService responsavelService = ServiceFactory.getInstance().getResponsavelService();
        List<Responsavel> responsaveis = responsavelService.listarAtivos();
        
        if (responsaveis.isEmpty()) {
            // Carrega responsáveis de exemplo
            carregarResponsaveisExemplo();
        } else {
            for (Responsavel resp : responsaveis) {
                comboResponsavel.addItem(resp.getNome());
            }
        }
    } catch (Exception e) {
        // Em caso de erro, carrega responsáveis de exemplo
        carregarResponsaveisExemplo();
    }
}
```

### 2. Filtro no DAO

O método `findAll()` em `ResponsavelDAORefactored` filtra apenas responsáveis ativos:

```java
@Override
public List<Responsavel> findAll() throws SQLException {
    String sql = "SELECT r.*, s.NOME as NOME_SETOR FROM TABELA_RESPONSAVEL r " +
                "LEFT JOIN TABELA_SETOR s ON r.ID_SETOR = s.ID " +
                "WHERE r.ATIVO = TRUE ORDER BY r.NOME";  // ← FILTRO ATIVO
    
    return executeQuery(sql);
}
```

## Possíveis Causas

### Causa 1: Nenhum Responsável Cadastrado
- Tabela `TABELA_RESPONSAVEL` está vazia
- **Solução**: Cadastrar responsáveis no sistema

### Causa 2: Todos os Responsáveis estão Inativos
- Existem responsáveis, mas todos com `ATIVO = FALSE`
- **Solução**: Ativar responsáveis existentes ou cadastrar novos

### Causa 3: Erro de Conexão com Banco
- Problema na conexão com PostgreSQL
- **Solução**: Verificar configuração do banco em `configuracao_banco.json`

### Causa 4: Coluna ATIVO não existe
- Tabela não tem a coluna `ATIVO`
- **Solução**: Executar script de migração para adicionar coluna

## Verificação

### 1. Verificar Responsáveis no Banco

Execute o script SQL `verificar_responsaveis.sql`:

```sql
-- Ver todos os responsáveis
SELECT ID, NOME, CPF, EMAIL, CARGO, ATIVO 
FROM TABELA_RESPONSAVEL 
ORDER BY NOME;

-- Contar ativos/inativos
SELECT 
    COUNT(*) as total,
    SUM(CASE WHEN ATIVO = TRUE THEN 1 ELSE 0 END) as ativos,
    SUM(CASE WHEN ATIVO = FALSE THEN 1 ELSE 0 END) as inativos
FROM TABELA_RESPONSAVEL;
```

### 2. Verificar Logs da Aplicação

Procure por mensagens de debug no console:

```
[DEBUG] Iniciando carregamento de responsáveis...
[DEBUG] ResponsavelService obtido com sucesso
[DEBUG] Quantidade de responsáveis encontrados: X
```

Se aparecer:
- `Quantidade de responsáveis encontrados: 0` → Nenhum responsável ativo
- `[ERRO] Erro ao carregar responsáveis` → Problema de conexão/SQL

## Soluções

### Solução 1: Cadastrar Responsáveis

Execute o script SQL para inserir responsáveis de teste:

```sql
-- Inserir responsáveis de exemplo
INSERT INTO TABELA_RESPONSAVEL (NOME, CPF, EMAIL, TELEFONE, CARGO, ATIVO, DATA_CADASTRO)
VALUES 
    ('João Silva', '123.456.789-00', 'joao.silva@ifmt.edu.br', '(65) 3333-4444', 'Administrador', TRUE, NOW()),
    ('Maria Santos', '987.654.321-00', 'maria.santos@ifmt.edu.br', '(65) 3333-5555', 'Coordenadora TI', TRUE, NOW()),
    ('Pedro Oliveira', '111.222.333-44', 'pedro.oliveira@ifmt.edu.br', '(65) 3333-6666', 'Gerente Financeiro', TRUE, NOW()),
    ('Ana Costa', '555.666.777-88', 'ana.costa@ifmt.edu.br', '(65) 3333-7777', 'Supervisora RH', TRUE, NOW()),
    ('Carlos Ferreira', '999.888.777-66', 'carlos.ferreira@ifmt.edu.br', '(65) 3333-8888', 'Responsável Almoxarifado', TRUE, NOW());
```

### Solução 2: Ativar Responsáveis Existentes

Se já existem responsáveis mas estão inativos:

```sql
-- Ativar todos os responsáveis
UPDATE TABELA_RESPONSAVEL 
SET ATIVO = TRUE 
WHERE ATIVO = FALSE OR ATIVO IS NULL;
```

### Solução 3: Adicionar Coluna ATIVO (se não existir)

```sql
-- Verificar se coluna existe
SELECT column_name 
FROM information_schema.columns 
WHERE table_name = 'tabela_responsavel' 
  AND column_name = 'ativo';

-- Se não existir, adicionar
ALTER TABLE TABELA_RESPONSAVEL 
ADD COLUMN IF NOT EXISTS ATIVO BOOLEAN DEFAULT TRUE;

-- Atualizar registros existentes
UPDATE TABELA_RESPONSAVEL 
SET ATIVO = TRUE 
WHERE ATIVO IS NULL;
```

### Solução 4: Remover Filtro ATIVO (temporário)

Se precisar carregar TODOS os responsáveis (ativos e inativos), modifique temporariamente o DAO:

```java
// ResponsavelDAORefactored.java
@Override
public List<Responsavel> findAll() throws SQLException {
    String sql = "SELECT r.*, s.NOME as NOME_SETOR FROM TABELA_RESPONSAVEL r " +
                "LEFT JOIN TABELA_SETOR s ON r.ID_SETOR = s.ID " +
                "ORDER BY r.NOME";  // ← SEM FILTRO ATIVO
    
    return executeQuery(sql);
}
```

## Comportamento Atual (Fallback)

Quando não encontra responsáveis, o sistema carrega responsáveis de exemplo:

```java
private void carregarResponsaveisExemplo() {
    String[] responsaveisExemplo = {
        "João Silva - Administrador",
        "Maria Santos - Coordenadora TI",
        "Pedro Oliveira - Gerente Financeiro",
        "Ana Costa - Supervisora RH",
        "Carlos Ferreira - Responsável Almoxarifado"
    };
    
    for (String responsavel : responsaveisExemplo) {
        comboResponsavel.addItem(responsavel);
    }
}
```

**Problema**: Esses responsáveis de exemplo não existem no banco, então ao salvar o inventário, o nome do responsável será salvo como string, mas não haverá vínculo com um registro real.

## Recomendação

1. **Verificar banco de dados** usando `verificar_responsaveis.sql`
2. **Cadastrar responsáveis reais** se não existirem
3. **Ativar responsáveis** se estiverem inativos
4. **Testar novamente** o formulário de inventário

## Teste de Validação

Após aplicar a solução:

1. Abrir o sistema desktop
2. Menu: **Inventário → Novo Inventário**
3. Verificar se o combobox "Responsável" está populado
4. Deve aparecer: "Selecione um responsável..." + lista de responsáveis reais
5. Selecionar um responsável e salvar
6. Verificar se o inventário foi salvo corretamente com o responsável vinculado

---

**Data**: 09/11/2025  
**Versão**: 1.0
