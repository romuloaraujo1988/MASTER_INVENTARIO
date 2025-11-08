## 🔍 Debug: Participante Não Encontrado

### Problema
Usuário ADMIN (ou qualquer participante) não consegue fazer coleta, mesmo estando cadastrado como participante do inventário.

### Log Observado
```
[DEBUG: Usuário ADMIN detectado - acesso total autorizado]
[DEBUG ParticipanteInventarioDAO] Buscando participante: idInventario=2, idUsuario=1
```

Mas não aparece a mensagem de "Participante encontrado" ou "Participante NÃO encontrado".

### Melhorias Aplicadas

Adicionamos logs detalhados no método `buscarIdParticipantePorUsuario()`:

1. **Debug sem filtro de ativo** - Verifica se o registro existe
2. **Mostra valor do campo ativo** - Para ver se está TRUE ou FALSE
3. **Query principal com filtro** - Busca com `ativo = TRUE`
4. **Logs de exceção detalhados** - SQLState e ErrorCode

### Como Usar

1. **Recompile o projeto**
2. **Reinicie o aplicativo**
3. **Tente fazer uma coleta**
4. **Observe os logs no console**

### Logs Esperados

#### Se o registro existe e está ativo:
```
[DEBUG ParticipanteInventarioDAO] ========================================
[DEBUG ParticipanteInventarioDAO] Buscando participante:
[DEBUG ParticipanteInventarioDAO]   idInventario = 2
[DEBUG ParticipanteInventarioDAO]   idUsuario = 1
[DEBUG ParticipanteInventarioDAO] Registro encontrado:
[DEBUG ParticipanteInventarioDAO]   id_participante = 123
[DEBUG ParticipanteInventarioDAO]   ativo = true
[DEBUG ParticipanteInventarioDAO]   SQL = SELECT id_participante FROM...
[DEBUG ParticipanteInventarioDAO] Executando query principal...
[DEBUG ParticipanteInventarioDAO] Query executada com sucesso
[DEBUG ParticipanteInventarioDAO] ✓ Participante ENCONTRADO!
[DEBUG ParticipanteInventarioDAO]   id_participante = 123
[DEBUG ParticipanteInventarioDAO] ========================================
```

#### Se o registro existe mas está inativo:
```
[DEBUG ParticipanteInventarioDAO] Registro encontrado:
[DEBUG ParticipanteInventarioDAO]   id_participante = 123
[DEBUG ParticipanteInventarioDAO]   ativo = false  ← PROBLEMA AQUI
[DEBUG ParticipanteInventarioDAO] ✗ Participante NÃO encontrado com ativo=TRUE
```

**Solução:** Reativar o participante:
```sql
UPDATE tabela_participante_inventario
SET ativo = TRUE
WHERE id_participante = 123;
```

#### Se o registro não existe:
```
[DEBUG ParticipanteInventarioDAO] Nenhum registro encontrado (sem filtro ativo)
[DEBUG ParticipanteInventarioDAO] ✗ Participante NÃO encontrado com ativo=TRUE
```

**Solução:** Adicionar o participante:
```sql
INSERT INTO tabela_participante_inventario 
(id_inventario, id_usuario, papel, ativo)
VALUES (2, 1, 'COORDENADOR', TRUE);
```

#### Se houver erro SQL:
```
[ERROR ParticipanteInventarioDAO] ========================================
[ERROR ParticipanteInventarioDAO] EXCEÇÃO SQL capturada:
[ERROR ParticipanteInventarioDAO]   Mensagem: column "id_participante" does not exist
[ERROR ParticipanteInventarioDAO]   SQLState: 42703
[ERROR ParticipanteInventarioDAO]   ErrorCode: 0
[ERROR ParticipanteInventarioDAO] ========================================
```

**Solução:** Verificar estrutura da tabela com `sql/testar_query_participante.sql`

### Scripts SQL Criados

1. **`sql/testar_query_participante.sql`**
   - Testa a query com diferentes variações
   - Verifica estrutura da tabela
   - Mostra todos os participantes do inventário

2. **`sql/adicionar_admin_como_participante.sql`**
   - Adiciona ADMIN como participante se não existir
   - Verifica participação atual
   - Testa a query do DAO

### Próximos Passos

1. **Execute o aplicativo** e tente fazer coleta
2. **Copie os logs** completos do console
3. **Execute** `sql/testar_query_participante.sql` no banco
4. **Compare** os resultados

### Possíveis Causas

1. ✅ **Nome da coluna errado** - Já corrigido para `id_participante`
2. ⏳ **Registro não existe** - Verificar com script SQL
3. ⏳ **Campo ativo = FALSE** - Reativar participante
4. ⏳ **Tipo boolean incompatível** - Verificar estrutura da tabela
5. ⏳ **IDs incorretos** - Verificar se idInventario e idUsuario estão corretos

---

**Data:** 28/10/2025
**Status:** Aguardando logs detalhados
