# 🔍 Busca Rápida - Funcionalidade

## 📋 O que é?

A **Busca Rápida** é uma ferramenta para localizar patrimônios de forma rápida e eficiente no app Android, sem precisar navegar por várias telas.

---

## 🎯 Funcionalidades

### 1. **Busca por Texto**
- Digite número do patrimônio, descrição, sala, etc.
- Busca em tempo real (300ms de delay)
- Resultados aparecem conforme você digita

### 2. **Filtros Rápidos (Chips)**
- **Coletados**: Mostra apenas patrimônios já coletados
- **Pendentes**: Mostra apenas patrimônios não coletados
- **Divergências**: Mostra patrimônios com problemas
- **Todos**: Remove filtros (padrão)

### 3. **Busca por Voz** 🎤
- Clique no ícone do microfone
- Fale o que deseja buscar
- Exemplo: "Cadeira sala 101"
- Busca automática com o texto reconhecido

### 4. **Filtros Avançados**
- Chip "Filtros Avançados"
- Abre tela com mais opções:
  - Filtrar por sala
  - Filtrar por responsável
  - Filtrar por estado de conservação
  - Filtrar por valor
  - Filtrar por data

---

## 🔄 Como Funciona

### Fluxo de Busca

```
1. Usuário digita texto
   ↓
2. Aguarda 300ms (debounce)
   ↓
3. Busca no banco local (Room)
   ↓
4. Aplica filtros selecionados
   ↓
5. Mostra resultados na lista
```

### Campos Pesquisados

A busca procura em:
- ✅ Número do patrimônio
- ✅ Descrição
- ✅ Marca
- ✅ Modelo
- ✅ Nome da sala
- ✅ Nome do responsável
- ✅ Observações

---

## 📱 Interface

### Componentes

```
┌─────────────────────────────────────┐
│  ← Busca Rápida              🎤     │
├─────────────────────────────────────┤
│  🔍 [Digite para buscar...]    ✕    │
├─────────────────────────────────────┤
│  [Coletados] [Pendentes] [Diverg.]  │
│  [Filtros Avançados]                │
├─────────────────────────────────────┤
│  📦 Patrimônio 12345                │
│     Cadeira Giratória               │
│     Sala: 101 | Resp: João          │
├─────────────────────────────────────┤
│  📦 Patrimônio 12346                │
│     Mesa de Escritório              │
│     Sala: 102 | Resp: Maria         │
└─────────────────────────────────────┘
```

### Estados da UI

1. **Vazio (inicial)**
   - Mostra ícone de busca
   - Mensagem: "Digite para buscar patrimônios"

2. **Buscando**
   - Mostra loading
   - Mensagem: "Buscando..."

3. **Resultados**
   - Lista de patrimônios encontrados
   - Contador: "X resultados encontrados"

4. **Sem resultados**
   - Ícone de busca vazia
   - Mensagem: "Nenhum patrimônio encontrado"

---

## 🎨 Filtros Disponíveis

### Filtros Rápidos (Chips)

| Filtro | Descrição | Query |
|--------|-----------|-------|
| **Todos** | Sem filtro | Todos os patrimônios |
| **Coletados** | Já foram coletados | `WHERE coletado = 1` |
| **Pendentes** | Ainda não coletados | `WHERE coletado = 0` |
| **Divergências** | Com problemas | `WHERE divergencia = 1` |

### Filtros Avançados

Abre tela `FiltrosActivity` com:
- Sala específica
- Responsável específico
- Estado de conservação
- Faixa de valor
- Período de cadastro

---

## 🔊 Busca por Voz

### Como Usar

1. Clique no ícone do microfone 🎤
2. Permita acesso ao microfone (primeira vez)
3. Fale claramente o que deseja buscar
4. Aguarde o reconhecimento
5. Busca automática com o texto

### Exemplos de Comandos

```
"Cadeira sala 101"
"Mesa João Silva"
"Computador"
"Patrimônio 12345"
"Impressora HP"
```

### Tecnologia

- **VoiceSearchManager**: Gerencia reconhecimento de voz
- **Android Speech Recognition**: API nativa
- **Permissão**: `RECORD_AUDIO`

---

## 💾 Busca Local (Offline)

A busca funciona **100% offline** usando Room Database:

```kotlin
@Query("""
    SELECT * FROM patrimonio 
    WHERE 
        numeroPatrimonio LIKE '%' || :query || '%' OR
        descricao LIKE '%' || :query || '%' OR
        marca LIKE '%' || :query || '%' OR
        modelo LIKE '%' || :query || '%' OR
        nomeSala LIKE '%' || :query || '%' OR
        nomeResponsavel LIKE '%' || :query || '%'
    ORDER BY numeroPatrimonio ASC
""")
suspend fun buscarPatrimonios(query: String): List<PatrimonioEntity>
```

---

## 🚀 Performance

### Otimizações

1. **Debounce (300ms)**
   - Evita buscas a cada tecla
   - Aguarda usuário parar de digitar

2. **Busca Assíncrona**
   - Não trava a UI
   - Usa Coroutines

3. **Índices no Banco**
   - Índice em `numeroPatrimonio`
   - Índice em `descricao`
   - Busca rápida mesmo com milhares de registros

4. **Cancelamento Automático**
   - Cancela busca anterior ao digitar
   - Evita resultados desatualizados

---

## 🎯 Casos de Uso

### Caso 1: Buscar Patrimônio Específico
```
Usuário: "12345"
Resultado: Patrimônio 12345 - Cadeira Giratória
```

### Caso 2: Buscar por Descrição
```
Usuário: "cadeira"
Resultado: Todos os patrimônios com "cadeira" na descrição
```

### Caso 3: Buscar por Sala
```
Usuário: "sala 101"
Resultado: Todos os patrimônios da sala 101
```

### Caso 4: Buscar Pendentes
```
Usuário: Ativa filtro "Pendentes"
Resultado: Apenas patrimônios não coletados
```

### Caso 5: Busca por Voz
```
Usuário: 🎤 "Computador Dell"
Resultado: Todos os computadores Dell
```

---

## 📊 Estatísticas

A busca mostra:
- **Total de resultados** encontrados
- **Tempo de busca** (opcional)
- **Filtros ativos** (chips destacados)

---

## 🔗 Integração com Outras Telas

### Navegação

```
Dashboard → Busca Rápida
   ↓
Resultados → Detalhes do Patrimônio
   ↓
Detalhes → Coletar Patrimônio
```

### Intent Extras

```kotlin
// Abrir busca com query pré-definida
val intent = Intent(this, QuickSearchActivity::class.java)
intent.putExtra("SEARCH_QUERY", "cadeira")
intent.putExtra("AUTO_SEARCH", true)
startActivity(intent)
```

---

## 🛠️ Arquivos Relacionados

### Android
- `QuickSearchActivity.kt` - Activity principal
- `PatrimonioAdapter.kt` - Adapter da lista
- `VoiceSearchManager.kt` - Gerenciador de voz
- `FiltrosActivity.kt` - Filtros avançados
- `activity_quick_search.xml` - Layout

### Database
- `PatrimonioDao.kt` - Queries de busca
- `PatrimonioEntity.kt` - Entidade Room

---

## ✅ Benefícios

1. **Rapidez**: Encontra patrimônios em segundos
2. **Offline**: Funciona sem internet
3. **Intuitivo**: Interface simples e clara
4. **Flexível**: Múltiplos filtros e busca por voz
5. **Eficiente**: Otimizado para grandes volumes

---

## 🎯 Melhorias Futuras

- [ ] Histórico de buscas recentes
- [ ] Sugestões automáticas (autocomplete)
- [ ] Busca por código de barras
- [ ] Exportar resultados
- [ ] Compartilhar resultados
- [ ] Busca por foto (OCR)

---

**Versão:** 2.0.0  
**Status:** ✅ Implementado e funcional
