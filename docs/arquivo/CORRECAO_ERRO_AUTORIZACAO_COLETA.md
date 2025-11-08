# Correção: Erro de Autorização para Coleta

## 🐛 Problema Identificado

Usuários delegados para fazer coleta no inventário ativo estavam recebendo a mensagem:

```
Acesso Negado!

Você não está habilitado para realizar coletas neste inventário.
Entre em contato com o coordenador do inventário para obter as permissões necessárias.
```

## 🔍 Causa Raiz

O problema estava no **ParticipanteInventarioDAO.java**, especificamente no método `buscarIdParticipantePorUsuario()`.

### Erro Encontrado:

O DAO estava tentando buscar a coluna `ID_PARTICIPANTE` na tabela, mas a coluna correta é `ID`:

```java
// ❌ ERRADO - Coluna não existe
String sql = "SELECT ID_PARTICIPANTE FROM TABELA_PARTICIPANTE_INVENTARIO " +
            "WHERE ID_INVENTARIO = ? AND ID_USUARIO = ? AND ATIVO = TRUE";
```

### Estrutura Real da Tabela:

```sql
CREATE TABLE TABELA_PARTICIPANTE_INVENTARIO (
    ID SERIAL PRIMARY KEY,  -- ✅ Nome correto da coluna
    ID_INVENTARIO INTEGER NOT NULL,
    ID_USUARIO INTEGER NOT NULL,
    PAPEL VARCHAR(50) NOT NULL,
    ...
);
```

## ✅ Correção Aplicada

### Arquivo Corrigido: `ParticipanteInventarioDAO.java`

#### 1. Método `buscarIdParticipantePorUsuario()`

```java
// ✅ CORRETO
String sql = "SELECT ID FROM TABELA_PARTICIPANTE_INVENTARIO " +
            "WHERE ID_INVENTARIO = ? AND ID_USUARIO = ? AND ATIVO = TRUE";

try (ResultSet rs = stmt.executeQuery()) {
    if (rs.next()) {
        int idParticipante = rs.getInt("ID");  // ✅ Coluna correta
        return idParticipante;
    }
}
```

#### 2. Método `criarParticipanteFromResultSet()`

```java
// ✅ CORRETO
participante.setIdParticipante(rs.getInt("ID"));  // Antes: "ID_PARTICIPANTE"
```

#### 3. Método `reativarParticipanteInativo()`

```java
// ✅ CORRETO
String selectSql = "SELECT ID FROM TABELA_PARTICIPANTE_INVENTARIO " +
                  "WHERE ID_INVENTARIO = ? AND ID_USUARIO = ? AND ATIVO = TRUE";

try (ResultSet rs = selectStmt.executeQuery()) {
    if (rs.next()) {
        participante.setIdParticipante(rs.getInt("ID"));  // ✅ Coluna correta
    }
}
```

## 🧪 Como Testar

### 1. Verificar Participantes no Banco

Execute este SQL para verificar os participantes cadastrados:

```sql
SELECT 
    pi.ID,
    pi.ID_INVENTARIO,
    i.NOME as INVENTARIO,
    i.STATUS_INVENTARIO,
    pi.ID_USUARIO,
    u.NOME_COMPLETO as USUARIO,
    pi.PAPEL,
    pi.ATIVO
FROM TABELA_PARTICIPANTE_INVENTARIO pi
JOIN TABELA_INVENTARIO i ON pi.ID_INVENTARIO = i.ID
JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID
WHERE i.STATUS_INVENTARIO = 'EM_ANDAMENTO'
ORDER BY pi.PAPEL, u.NOME_COMPLETO;
```

### 2. Adicionar Usuário como Participante (se necessário)

Se o usuário não estiver cadastrado como participante:

```sql
-- Buscar ID do inventário ativo
SELECT ID, NOME, STATUS_INVENTARIO 
FROM TABELA_INVENTARIO 
WHERE STATUS_INVENTARIO = 'EM_ANDAMENTO';

-- Buscar ID do usuário
SELECT ID, NOME_COMPLETO, PERFIL 
FROM TABELA_USUARIO 
WHERE NOME_COMPLETO LIKE '%nome_do_usuario%';

-- Adicionar como participante (COLETOR)
INSERT INTO TABELA_PARTICIPANTE_INVENTARIO 
    (ID_INVENTARIO, ID_USUARIO, PAPEL, ATIVO, OBSERVACOES)
VALUES 
    (1, 2, 'COLETOR', TRUE, 'Adicionado para realizar coletas');
```

### 3. Testar no Aplicativo Desktop

1. **Faça login** com o usuário delegado
2. Abra a tela de **Coleta de Patrimônios**
3. Selecione uma sala
4. Tente buscar e coletar um patrimônio
5. **Resultado esperado:** Coleta deve funcionar normalmente

## 📋 Checklist de Validação

- [ ] Código compilado sem erros
- [ ] Usuário está cadastrado na tabela TABELA_PARTICIPANTE_INVENTARIO
- [ ] Usuário está com ATIVO = TRUE
- [ ] Inventário está com STATUS_INVENTARIO = 'EM_ANDAMENTO'
- [ ] Aplicativo desktop reiniciado
- [ ] Teste de coleta realizado com sucesso

## 🔧 Scripts SQL Úteis

### Verificar Inventário Ativo

```sql
SELECT * FROM TABELA_INVENTARIO 
WHERE STATUS_INVENTARIO = 'EM_ANDAMENTO';
```

### Listar Todos os Participantes Ativos

```sql
SELECT 
    u.NOME_COMPLETO,
    u.PERFIL,
    pi.PAPEL,
    i.NOME as INVENTARIO
FROM TABELA_PARTICIPANTE_INVENTARIO pi
JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID
JOIN TABELA_INVENTARIO i ON pi.ID_INVENTARIO = i.ID
WHERE pi.ATIVO = TRUE
AND i.STATUS_INVENTARIO = 'EM_ANDAMENTO';
```

### Adicionar Múltiplos Usuários como Coletores

```sql
-- Adicionar todos os usuários com perfil OPERADOR como coletores
INSERT INTO TABELA_PARTICIPANTE_INVENTARIO 
    (ID_INVENTARIO, ID_USUARIO, PAPEL, ATIVO)
SELECT 
    (SELECT ID FROM TABELA_INVENTARIO WHERE STATUS_INVENTARIO = 'EM_ANDAMENTO' LIMIT 1),
    u.ID,
    'COLETOR',
    TRUE
FROM TABELA_USUARIO u
WHERE u.PERFIL = 'OPERADOR'
AND u.ATIVO = TRUE
AND NOT EXISTS (
    SELECT 1 FROM TABELA_PARTICIPANTE_INVENTARIO pi
    WHERE pi.ID_USUARIO = u.ID
    AND pi.ID_INVENTARIO = (SELECT ID FROM TABELA_INVENTARIO WHERE STATUS_INVENTARIO = 'EM_ANDAMENTO' LIMIT 1)
);
```

### Reativar Participante Inativo

```sql
UPDATE TABELA_PARTICIPANTE_INVENTARIO
SET ATIVO = TRUE,
    DATA_REMOCAO = NULL,
    DATA_ULTIMA_ATUALIZACAO = CURRENT_TIMESTAMP
WHERE ID_INVENTARIO = ? 
AND ID_USUARIO = ?
AND ATIVO = FALSE;
```

## 📊 Estrutura de Papéis

### Papéis Disponíveis:

- **COORDENADOR** - Gerencia o inventário (apenas 1 por inventário)
- **COLETOR** - Realiza coletas de patrimônios
- **OBSERVADOR** - Visualiza informações (sem permissão de coleta)

### Permissões por Papel:

| Ação | COORDENADOR | COLETOR | OBSERVADOR |
|------|-------------|---------|------------|
| Realizar coletas | ✅ | ✅ | ❌ |
| Visualizar coletas | ✅ | ✅ | ✅ |
| Gerenciar participantes | ✅ | ❌ | ❌ |
| Finalizar inventário | ✅ | ❌ | ❌ |

## 🚨 Troubleshooting

### Problema: Usuário ainda não consegue coletar

**Verificações:**

1. **Usuário está ativo?**
   ```sql
   SELECT * FROM TABELA_USUARIO WHERE ID = ?;
   ```

2. **Usuário é participante ativo?**
   ```sql
   SELECT * FROM TABELA_PARTICIPANTE_INVENTARIO 
   WHERE ID_USUARIO = ? AND ATIVO = TRUE;
   ```

3. **Inventário está ativo?**
   ```sql
   SELECT * FROM TABELA_INVENTARIO 
   WHERE STATUS_INVENTARIO = 'EM_ANDAMENTO';
   ```

4. **Logs do aplicativo:**
   - Verifique o console para mensagens de debug
   - Procure por: `[DEBUG ParticipanteInventarioDAO]`

### Problema: Erro ao adicionar participante

**Possíveis causas:**

- Usuário já é participante (verificar constraint UNIQUE)
- Inventário não permite alterações (status diferente de PLANEJADO/ABERTO/REABERTO)
- Já existe um coordenador ativo (apenas 1 permitido)

**Solução:**

```sql
-- Verificar se já existe
SELECT * FROM TABELA_PARTICIPANTE_INVENTARIO
WHERE ID_INVENTARIO = ? AND ID_USUARIO = ?;

-- Se existir inativo, reativar
UPDATE TABELA_PARTICIPANTE_INVENTARIO
SET ATIVO = TRUE, DATA_REMOCAO = NULL
WHERE ID_INVENTARIO = ? AND ID_USUARIO = ?;
```

## 📝 Notas Importantes

1. **Sempre use ATIVO = TRUE** nas consultas para filtrar participantes ativos
2. **Soft delete** - Participantes são desativados, não deletados
3. **Constraint UNIQUE** - Não permite duplicação de (ID_INVENTARIO, ID_USUARIO)
4. **Triggers automáticos** - Atualizam DATA_ULTIMA_ATUALIZACAO e DATA_REMOCAO

## ✅ Status da Correção

- [x] Problema identificado
- [x] Causa raiz encontrada
- [x] Código corrigido
- [x] Documentação criada
- [ ] Teste realizado no ambiente
- [ ] Validação com usuário final

---

**Data da Correção:** 28/10/2025
**Arquivo Modificado:** `src/main/java/com/inventario/dao/ParticipanteInventarioDAO.java`
**Tipo de Correção:** Bug fix - Nome de coluna incorreto
**Impacto:** Alto - Bloqueava funcionalidade principal de coleta
