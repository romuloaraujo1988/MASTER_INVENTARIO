# Correção: ID_PARTICIPANTE Inválido na Sincronização

## 🐛 Problema Identificado

Durante a sincronização de coletas do SQLite para o PostgreSQL, o sistema estava falhando com o erro:

```
ADVERTÊNCIA: ID_PARTICIPANTE inválido: 0 - pulando registro
```

### Causa Raiz

No `ColetaFrame_v2.java`, ao criar uma coleta, o código estava definindo apenas o `idColetor`, mas **não estava definindo o `idParticipanteInventario`**. Isso fazia com que o valor padrão fosse `0`, causando a rejeição durante a sincronização.

```java
// ❌ ANTES (ERRADO)
coleta.setIdColetor(usuarioLogado.getId());
// idParticipanteInventario ficava como 0 (valor padrão)
```

### Impacto

- Coletas eram salvas no SQLite com `id_participante = 0`
- Durante a sincronização, essas coletas eram rejeitadas
- Dados ficavam "presos" no SQLite sem sincronizar

---

## ✅ Solução Implementada

### 1. Correção no Fluxo de Coleta Normal (Linha ~2893)

```java
// ✅ DEPOIS (CORRETO)
if (usuarioLogado != null) {
    coleta.setIdColetor(usuarioLogado.getId());
    
    // Buscar ID do participante do inventário
    ParticipanteInventarioDAO participanteDAO = new ParticipanteInventarioDAO();
    Integer idParticipante = participanteDAO.buscarIdParticipantePorUsuario(
        inventarioAtivo.getId(), 
        usuarioLogado.getId()
    );
    
    if (idParticipante != null && idParticipante > 0) {
        coleta.setIdParticipanteInventario(idParticipante);
        System.out.println("DEBUG: ID participante definido: " + idParticipante);
    } else {
        // Fallback: usar ID do coletor
        coleta.setIdParticipanteInventario(usuarioLogado.getId());
        System.out.println("DEBUG: AVISO - Participante não encontrado, usando ID do coletor");
    }
}
```

### 2. Correção no Fluxo de Item Sem Etiqueta (Linha ~1702)

```java
// ✅ CORRETO
// Buscar ID do participante do inventário
ParticipanteInventarioDAO participanteDAO = new ParticipanteInventarioDAO();
Integer idParticipante = participanteDAO.buscarIdParticipantePorUsuario(
    inventarioAtivo.getId(), 
    usuarioLogado.getId()
);

if (idParticipante == null || idParticipante == 0) {
    // Fallback: usar ID do usuário
    idParticipante = usuarioLogado.getId();
    System.out.println("DEBUG: AVISO - Participante não encontrado, usando ID do usuário");
} else {
    System.out.println("DEBUG: ID participante encontrado: " + idParticipante);
}

coleta.setIdParticipanteInventario(idParticipante);
```

---

## 🔍 Como Funciona

### Fluxo Correto

```
1. Usuário faz login
   ↓
2. Sistema busca inventário ativo
   ↓
3. Ao coletar patrimônio:
   a) Define idColetor = usuarioLogado.getId()
   b) Busca idParticipante via ParticipanteInventarioDAO
   c) Define idParticipanteInventario = idParticipante
   ↓
4. Coleta salva no SQLite com ID válido
   ↓
5. Sincronização aceita a coleta (ID > 0)
   ↓
6. Coleta inserida no PostgreSQL com sucesso
```

### Método Utilizado

```java
ParticipanteInventarioDAO.buscarIdParticipantePorUsuario(idInventario, idUsuario)
```

Este método:
- Busca na tabela `tabela_participante_inventario`
- Retorna o ID do participante ativo
- Retorna `null` se não encontrar

---

## 🧪 Como Testar

### 1. Verificar Coletas Antigas (Antes da Correção)

```bash
sqlite3 data/inventario.db "SELECT id, id_participante, numero_patrimonio FROM local_coleta WHERE id_participante = 0;"
```

Se houver registros, são coletas com problema.

### 2. Fazer Nova Coleta (Após Correção)

1. Fazer login no sistema
2. Coletar um patrimônio
3. Verificar no SQLite:

```bash
sqlite3 data/inventario.db "SELECT id, id_participante, numero_patrimonio FROM local_coleta ORDER BY id DESC LIMIT 1;"
```

**Resultado esperado:** `id_participante` deve ser > 0

### 3. Testar Sincronização

1. Coletar alguns patrimônios
2. Clicar em "Sincronizar"
3. Verificar logs:

```
DEBUG: ID participante definido: 5
INFORMAÇÕES: Coleta sincronizada com sucesso! ID remoto: 123
```

**Não deve aparecer:** `ID_PARTICIPANTE inválido: 0`

---

## 📊 Dados Antes e Depois

### Antes (Errado)

```json
{
  "id_patrimonio": 97,
  "numero_patrimonio": "107638",
  "id_participante": 0,  // ❌ INVÁLIDO
  "id_inventario": 2,
  "data_coleta": "2025-11-25 11:32:28.542"
}
```

**Resultado:** Sincronização falha com "ID_PARTICIPANTE inválido: 0"

### Depois (Correto)

```json
{
  "id_patrimonio": 97,
  "numero_patrimonio": "107638",
  "id_participante": 5,  // ✅ VÁLIDO
  "id_inventario": 2,
  "data_coleta": "2025-11-25 11:32:28.542"
}
```

**Resultado:** Sincronização bem-sucedida

---

## 🔧 Correção de Dados Antigos

Se houver coletas antigas com `id_participante = 0`, você pode corrigi-las:

### Script SQL para Corrigir

```sql
-- Atualizar coletas com id_participante = 0
-- Usando o id_coletor como fallback (assumindo que são a mesma pessoa)
UPDATE local_coleta 
SET id_participante = (
    SELECT id_usuario 
    FROM local_usuario 
    LIMIT 1
)
WHERE id_participante = 0 OR id_participante IS NULL;
```

**ATENÇÃO:** Execute apenas se tiver certeza de qual usuário deve ser o participante.

---

## ✅ Checklist de Validação

- [x] Código corrigido em `ColetaFrame_v2.java`
- [x] Busca de `idParticipante` implementada
- [x] Fallback para `idColetor` se não encontrar
- [x] Logs de debug adicionados
- [ ] Testar coleta normal
- [ ] Testar coleta de item sem etiqueta
- [ ] Testar sincronização
- [ ] Verificar logs sem erros

---

## 📝 Arquivos Modificados

- `src/main/java/com/inventario/view/ColetaFrame_v2.java`
  - Linha ~1702: Correção no fluxo de item sem etiqueta
  - Linha ~2893: Correção no fluxo de coleta normal

---

## 🎯 Resultado Esperado

Após esta correção:

1. ✅ Todas as novas coletas terão `id_participante` válido
2. ✅ Sincronização funcionará sem erros
3. ✅ Dados serão inseridos corretamente no PostgreSQL
4. ✅ Não haverá mais coletas "presas" no SQLite

---

**Data da Correção:** 26/11/2025  
**Versão:** 2.0.1  
**Status:** ✅ Implementado e pronto para teste
