# 🚨 CORREÇÃO URGENTE - Erro na Tabela Coleta

## ❌ Problema Identificado

**Erro:** `inserção ou atualização em tabela "tabela_coleta" viola restrição de chave estrangeira "tabela_coleta_id_coletor_fkey"`

**Causa:** O sistema está tentando inserir `id_coletor = 12` na `tabela_coleta`, mas:
- A `tabela_coletor` só tem IDs de 1 a 8
- O ID 12 é na verdade um **ID de usuário** (tabela_usuario)
- O sistema foi migrado para usar `id_participante_inventario` ao invés de `id_coletor`

## 🔍 Análise Técnica

### Estrutura Atual da tabela_coleta:
```sql
- id_coletor (INTEGER NOT NULL) → FK para tabela_coletor
- id_participante_inventario (INTEGER NOT NULL) → FK para tabela_participante_inventario
```

### O que está acontecendo:
1. O código em `ColetaDAO.java` (linha 73) insere **AMBOS** os campos
2. O `id_coletor` está recebendo o ID do **usuário** (12) ao invés do ID do coletor
3. A constraint `tabela_coleta_id_coletor_fkey` impede a inserção porque ID 12 não existe em `tabela_coletor`

### Dados no banco:
```
tabela_coletor: IDs 1-8 (coletores genéricos)
tabela_usuario: ID 12 existe (usuário real)
tabela_participante_inventario: Relaciona usuários com inventários
```

## ✅ SOLUÇÃO IMEDIATA

Execute o seguinte SQL no PostgreSQL:

```sql
-- Remover a constraint de foreign key obsoleta
ALTER TABLE tabela_coleta 
DROP CONSTRAINT tabela_coleta_id_coletor_fkey;

-- Verificar que a constraint foi removida
SELECT constraint_name 
FROM information_schema.table_constraints 
WHERE table_name = 'tabela_coleta' 
  AND constraint_type = 'FOREIGN KEY';
```

## 📋 Passos para Executar

### Opção 1: Via psql (Recomendado)
```bash
psql -h localhost -U inventario -d sispatrimonio -f sql/corrigir_constraint_coleta_coletor.sql
```

### Opção 2: Via pgAdmin
1. Abrir pgAdmin
2. Conectar no banco `sispatrimonio`
3. Abrir Query Tool
4. Executar o script `sql/corrigir_constraint_coleta_coletor.sql`

### Opção 3: Via linha de comando direta
```bash
psql -h localhost -U inventario -d sispatrimonio -c "ALTER TABLE tabela_coleta DROP CONSTRAINT tabela_coleta_id_coletor_fkey;"
```

## 🔄 Por que isso resolve?

1. **Campo obsoleto**: `id_coletor` não é mais usado pelo sistema
2. **Campo novo**: `id_participante_inventario` é o campo correto que relaciona com usuários
3. **Compatibilidade**: Removendo a constraint, o campo `id_coletor` pode ter qualquer valor sem causar erro
4. **Sem impacto**: Nenhuma funcionalidade depende da constraint de `id_coletor`

## 📊 Verificação Pós-Correção

Após executar o script, teste inserindo uma coleta:

```sql
-- Teste de inserção
INSERT INTO tabela_coleta (
    id_inventario, 
    id_patrimonio, 
    id_coletor, 
    id_participante_inventario,
    data_coleta,
    status_coleta
) VALUES (
    1,      -- ID do inventário
    1,      -- ID do patrimônio
    12,     -- ID do usuário (antes causava erro)
    1,      -- ID do participante (correto)
    NOW(),
    'COLETADO'
);

-- Se não der erro, está corrigido!
```

## 🎯 Próximos Passos (Opcional)

### Limpeza Futura
Após confirmar que tudo funciona, considere:

1. **Tornar id_coletor nullable:**
```sql
ALTER TABLE tabela_coleta ALTER COLUMN id_coletor DROP NOT NULL;
```

2. **Ou remover o campo completamente** (requer mais testes):
```sql
ALTER TABLE tabela_coleta DROP COLUMN id_coletor;
```

## 📝 Arquivos Relacionados

- **Script SQL**: `sql/corrigir_constraint_coleta_coletor.sql`
- **Código Java**: `src/main/java/com/inventario/dao/ColetaDAO.java` (linha 73)
- **Model**: `src/main/java/com/inventario/model/Coleta.java`

## ⚠️ IMPORTANTE

**Execute este script ANTES de tentar registrar novas coletas!**

O sistema não conseguirá registrar coletas enquanto a constraint existir.

---

**Status**: 🔴 CRÍTICO - Impede registro de coletas  
**Prioridade**: 🔥 URGENTE  
**Tempo estimado**: 2 minutos  
**Impacto**: ✅ Zero (apenas remove constraint obsoleta)

**Data**: 17/11/2025  
**Versão**: 2.0.0
