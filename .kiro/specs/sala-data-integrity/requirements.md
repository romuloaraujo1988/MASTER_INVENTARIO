# Spec: Correção de Integridade de Dados - Seleção de Salas

## Problema Identificado

A tabela `tabela_sala` no PostgreSQL possui **duas colunas de ID diferentes** com valores inconsistentes:

- `id_sala` (PK, sequence) - Usado como chave primária e FK em `tabela_patrimonio`
- `id` (sequence separada) - Coluna adicional com valores diferentes

### Dados do Problema

| Métrica | Valor |
|---------|-------|
| Total de salas | 130 |
| Salas com `id_sala = id` | 25 (19%) |
| Salas com `id_sala != id` | 105 (81%) |

### Exemplo de Inconsistência

| id_sala | id | numero_sala | descricao |
|---------|-----|-------------|-----------|
| 1 | 42 | ADM-01 | Sala de Administração |
| 2 | 50 | TI-01 | Sala de TI |
| 3 | 48 | BIB-01 | Biblioteca Principal |

### Fluxo de Dados Atual

```
PostgreSQL (tabela_sala)
    ↓ MobileSalaService.java usa ID_SALA
    ↓ dto.setId(rs.getInt("ID_SALA")) ✅ CORRETO
MobileSalaDTO.java
    ↓ JSON: { "id": <ID_SALA>, ... }
Android SalaDto.kt
    ↓ val id: Int (recebe ID_SALA)
SalaEntity.kt (Room)
    ↓ val id: Int (armazena ID_SALA)
Sala.kt (Domain)
    ↓ val id: Long (converte para Long)
SalaSelectionActivity.kt
    ↓ navegarParaColeta(sala)
    ↓ sala.id.toInt() → passa para ScannerActivity
```

### Análise do Problema

O backend está **corretamente** usando `ID_SALA` nas queries SQL. O problema pode estar em:

1. **Conversão de tipos**: `Long` → `Int` pode causar problemas em IDs grandes
2. **Cache desatualizado**: SQLite pode ter dados antigos com IDs errados
3. **Sincronização**: Dados podem ter sido sincronizados com a coluna `id` errada em algum momento

---

## User Stories

### US-001: Garantir Consistência de IDs de Sala

**Como** desenvolvedor do sistema  
**Quero** garantir que o ID correto (`id_sala`) seja usado em todo o fluxo  
**Para que** as coletas sejam associadas às salas corretas

#### Critérios de Aceitação

- [ ] Backend sempre retorna `id_sala` como `id` no DTO
- [ ] Android armazena `id_sala` no Room
- [ ] Navegação usa o ID correto ao passar para outras Activities
- [ ] Coletas são salvas com o `id_sala` correto

### US-002: Validar Integridade de Dados no SQLite

**Como** usuário do app  
**Quero** que os dados locais estejam sincronizados corretamente  
**Para que** eu possa trabalhar offline sem problemas

#### Critérios de Aceitação

- [ ] Ao sincronizar, limpar dados antigos antes de inserir novos
- [ ] Verificar se IDs no SQLite correspondem aos do servidor
- [ ] Log de diagnóstico para identificar inconsistências

### US-003: Adicionar Logs de Diagnóstico

**Como** desenvolvedor  
**Quero** logs detalhados do fluxo de IDs de sala  
**Para que** eu possa identificar onde ocorre a inconsistência

#### Critérios de Aceitação

- [ ] Log ao receber sala do servidor (ID recebido)
- [ ] Log ao salvar no SQLite (ID salvo)
- [ ] Log ao navegar para coleta (ID passado)
- [ ] Log ao registrar coleta (ID usado)

---

## Investigação Realizada ✅

### 1. Estrutura do Banco PostgreSQL

A tabela `tabela_sala` tem **duas colunas de ID**:
- `id_sala` (PK, sequence) - **CORRETO** - usado em FKs
- `id` (sequence separada) - **REDUNDANTE** - não usado em relações

### 2. Verificação de Patrimônios ✅

```sql
-- Resultado: Patrimônios usam id_sala CORRETAMENTE
SELECT p.id_sala, s.id_sala, s.id, s.numero_sala
FROM tabela_patrimonio p
JOIN tabela_sala s ON p.id_sala = s.id_sala
-- Status: OK - todos os JOINs funcionam corretamente
```

### 3. Verificação de Coletas ✅

```sql
-- Resultado: Coletas referenciam patrimônios que usam id_sala
SELECT c.id_patrimonio, p.id_sala, s.id_sala, s.numero_sala
FROM tabela_coleta c
JOIN tabela_patrimonio p ON c.id_patrimonio = p.id
JOIN tabela_sala s ON p.id_sala = s.id_sala
-- Status: OK - fluxo de dados correto
```

### 4. Backend ✅

O `MobileSalaService.java` usa `ID_SALA` corretamente:
```java
dto.setId(rs.getInt("ID_SALA")); // ✅ CORRETO
```

### 5. Fluxo Android - Potencial Problema Identificado

```
SalaSelectionViewModel.kt
    ↓ Sala(id = entity.id.toLong()) // entity.id é Int (ID_SALA do servidor)
    ↓ sala.id é Long
SalaSelectionActivity.kt
    ↓ navegarParaColeta(sala)
    ↓ putExtra(EXTRA_SALA_ID, sala.id.toInt()) // Converte Long → Int
ScannerActivity.kt
    ↓ val salaId = intent.getIntExtra(EXTRA_SALA_ID, 0) // Recebe Int
    ↓ preferencesManager.setCurrentSalaId(salaId) // Salva Int
```

**Problema**: Conversão desnecessária `Long → Int → Int`

### 6. Inconsistência de Tipos

| Componente | Tipo de ID |
|------------|------------|
| PostgreSQL `id_sala` | INTEGER |
| Backend DTO | Integer |
| Android SalaEntity | Int |
| Android Sala (domain) | Long |
| PreferencesManager | Int |
| Intent extras | Int |

**Problema**: Modelo de domínio usa `Long`, mas todo o resto usa `Int`

---

## Solução Proposta

### Opção A: Padronizar Tipos para Int (Recomendado)

O ID de sala no PostgreSQL é `INTEGER`, então faz sentido usar `Int` em todo o fluxo Android.

**Mudanças necessárias:**

1. **Sala.kt (domain)**: Mudar `id: Long` para `id: Int`
2. **SalaSelectionActivity.kt**: Remover `.toInt()` desnecessário
3. **Verificar outros modelos de domínio** que usam `Long` desnecessariamente

### Opção B: Remover Coluna `id` Redundante no PostgreSQL

1. Verificar se `id` é usado em algum lugar
2. Migrar dados se necessário
3. Remover coluna `id`
4. Manter apenas `id_sala` como PK

### Opção C: Adicionar Logs de Diagnóstico (Imediato)

Adicionar logs para rastrear o ID da sala em cada etapa:
1. Ao receber do servidor
2. Ao salvar no SQLite
3. Ao navegar entre Activities
4. Ao registrar coleta

---

## Arquivos Relacionados

### Backend
- `src/main/java/com/inventario/mobile/server/service/MobileSalaService.java`
- `src/main/java/com/inventario/mobile/server/dto/MobileSalaDTO.java`

### Android
- `InventarioMobile/app/src/main/java/com/inventario/mobile/presentation/sala/SalaSelectionActivity.kt`
- `InventarioMobile/app/src/main/java/com/inventario/mobile/presentation/sala/SalaSelectionViewModel.kt`
- `InventarioMobile/app/src/main/java/com/inventario/mobile/data/local/entity/SalaEntity.kt`
- `InventarioMobile/app/src/main/java/com/inventario/mobile/domain/model/Sala.kt`
- `InventarioMobile/app/src/main/java/com/inventario/mobile/data/remote/dto/SalaDto.kt`

---

## Próximos Passos

1. [ ] Executar queries de investigação no PostgreSQL
2. [ ] Verificar logs do app durante seleção de sala
3. [ ] Comparar dados SQLite com PostgreSQL
4. [ ] Decidir entre Opção A, B ou C
5. [ ] Implementar correção
6. [ ] Testar fluxo completo de coleta

---

**Criado em:** 11/01/2026  
**Status:** Em Investigação  
**Prioridade:** Alta
