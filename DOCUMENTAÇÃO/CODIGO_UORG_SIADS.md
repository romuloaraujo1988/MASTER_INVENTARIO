# Código UOrg - Integração SIADS

## Visão Geral

O código UOrg (Unidade Organizacional) é um identificador único utilizado pelo SIADS (Sistema Integrado de Administração de Serviços) para identificar unidades organizacionais dentro da estrutura do IFMT.

## Implementação

### 1. Modelo de Dados

Foi adicionado o campo `codigoUorg` na classe `Campus`:

```java
private String codigoUorg; // Código da Unidade Organizacional no SIADS
```

### 2. Banco de Dados

**Script SQL**: `sql/adicionar_codigo_uorg_campus.sql`

```sql
ALTER TABLE TABELA_CAMPUS 
ADD COLUMN IF NOT EXISTS codigo_uorg VARCHAR(20);

COMMENT ON COLUMN TABELA_CAMPUS.codigo_uorg IS 'Código da Unidade Organizacional no SIADS';

CREATE INDEX IF NOT EXISTS idx_campus_codigo_uorg ON TABELA_CAMPUS(codigo_uorg);
```

### 3. Camada DAO

**Arquivo**: `src/main/java/com/inventario/dao/CampusDAO.java`

Atualizações realizadas:
- Queries SQL incluem o campo `codigo_uorg`
- Método `mapResultSetToCampus` popula o campo
- Métodos `inserir` e `atualizar` persistem o valor

### 4. Integração SIADS

**Arquivo**: `src/main/java/com/inventario/siads/dao/SiadsPatrimonioDAO.java`

As queries foram atualizadas para incluir JOIN com a tabela Campus e buscar o código UOrg:

```java
LEFT JOIN TABELA_CAMPUS c ON st.ID_CAMPUS = c.id
```

O campo é mapeado no patrimônio:
```java
patrimonio.setCodigoUOrg(rs.getString("codigo_uorg"));
```

### 5. Interface de Usuário

**Formulário de Campus**: `src/main/java/com/inventario/view/CampusFormDialog.java`

Campo adicionado:
- Label: "🔢 Código UOrg (SIADS)"
- Tooltip com informações sobre o SIADS
- Validação de formato (7 dígitos numéricos)

**Tela de Gerenciamento**: `src/main/java/com/inventario/view/CampusManagementFrame.java`

Funcionalidades:
- Listagem de campus com código UOrg
- Busca por código UOrg
- Edição do código UOrg

## Formato do Código UOrg

### Padrão Recomendado
- **Formato**: 7 dígitos numéricos
- **Exemplo**: `1790001`

### Estrutura Típica
```
179 0001
│   │
│   └─ Sequencial da unidade
└───── Código da instituição
```

## Como Obter o Código UOrg

1. **Consultar o SIADS**
   - Acesse o sistema SIADS
   - Navegue até Cadastros > Unidades Organizacionais
   - Localize o campus desejado
   - Copie o código UOrg

2. **Contatar o Suporte**
   - Entre em contato com a equipe de TI
   - Solicite a lista de códigos UOrg dos campus
   - Mantenha a documentação atualizada

3. **Documentação Institucional**
   - Consulte o manual do SIADS
   - Verifique documentos de integração
   - Mantenha registro dos códigos

## Uso na Exportação SIADS

O código UOrg é utilizado na exportação de patrimônios para o SIADS:

```java
// No arquivo de exportação
patrimonio.getCodigoUOrg() // Retorna o código do campus
```

### Campos Relacionados na Exportação

| Campo Sistema | Campo SIADS | Origem |
|---------------|-------------|--------|
| codigoUOrg | CODIGO_UORG | Campus |
| numeroPatrimonio | NUMERO_PATRIMONIO | Patrimônio |
| descricao | DESCRICAO | Patrimônio |
| responsavel | RESPONSAVEL | Responsável |

## Validações

### No Formulário
1. Campo opcional (não obrigatório)
2. Se preenchido, deve ter 7 dígitos
3. Aviso se formato diferente do padrão

### No Serviço
1. Validação de formato
2. Verificação de duplicidade (se necessário)
3. Consistência com dados do SIADS

## Migração de Dados Existentes

Para campus já cadastrados sem código UOrg:

```sql
-- Atualizar campus específico
UPDATE TABELA_CAMPUS 
SET codigo_uorg = '1790001' 
WHERE id = 1;

-- Verificar campus sem código
SELECT id, nome, codigo_uorg 
FROM TABELA_CAMPUS 
WHERE codigo_uorg IS NULL OR codigo_uorg = '';
```

## Troubleshooting

### Problema: Código UOrg não aparece na exportação
**Solução**: 
1. Verificar se o campus tem código UOrg cadastrado
2. Verificar se o setor está vinculado ao campus correto
3. Verificar se o patrimônio está vinculado ao setor correto

### Problema: Erro ao salvar código UOrg
**Solução**:
1. Verificar se a coluna existe no banco
2. Executar script `adicionar_codigo_uorg_campus.sql`
3. Verificar permissões do usuário do banco

### Problema: Formato inválido
**Solução**:
1. Código deve ter exatamente 7 dígitos
2. Apenas números são permitidos
3. Consultar documentação do SIADS

## Referências

- Manual do SIADS
- Documentação de Integração IFMT
- Estrutura Organizacional do IFMT

## Histórico de Alterações

| Data | Versão | Descrição |
|------|--------|-----------|
| 2025-11-08 | 1.0.0 | Implementação inicial do código UOrg |

## Contatos

Para dúvidas sobre códigos UOrg:
- Suporte SIADS: suporte.siads@ifmt.edu.br
- TI Institucional: ti@ifmt.edu.br
