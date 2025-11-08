# Diagrama de Funcionalidades - Versão 1.3.0

## 🗺️ Mapa de Funcionalidades

```
┌─────────────────────────────────────────────────────────────────┐
│                    TELA DE INVENTÁRIO v1.3.0                    │
└─────────────────────────────────────────────────────────────────┘
                                 │
                ┌────────────────┼────────────────┐
                │                │                │
                ▼                ▼                ▼
        ┌───────────┐    ┌───────────┐    ┌───────────┐
        │   BUSCA   │    │ ORDENAÇÃO │    │  AÇÕES    │
        └───────────┘    └───────────┘    └───────────┘
                │                │                │
                │                │                │
    ┌───────────┴───────────┐    │    ┌───────────┴───────────┐
    │                       │    │    │                       │
    ▼                       ▼    ▼    ▼                       ▼
┌────────┐            ┌────────┐  ┌────────┐            ┌────────┐
│ Número │            │ Marca  │  │ Número │            │Scanner │
│Descrição│            │ Modelo │  │Descrição│            │Atualizar│
│ Setor  │            │ Setor  │  │ Setor  │            │Exportar│
│  Sala  │            │  Sala  │  │        │            │        │
└────────┘            └────────┘  └────────┘            └────────┘
                                       │
                                       ▼
                                ┌──────────┐
                                │ DETALHES │
                                └──────────┘
                                       │
                    ┌──────────────────┼──────────────────┐
                    │                  │                  │
                    ▼                  ▼                  ▼
            ┌──────────────┐   ┌──────────────┐   ┌──────────────┐
            │ Informações  │   │ Localização  │   │    Ações     │
            │   Básicas    │   │              │   │              │
            └──────────────┘   └──────────────┘   └──────────────┘
            │                  │                  │
            │ • Número         │ • Setor          │ • Escanear QR
            │ • Descrição      │ • Sala           │ • Fechar
            │ • Marca          │ • Responsável    │
            │ • Modelo         │                  │
            │ • Série          │                  │
            │ • Estado         │                  │
            │ • Valor          │                  │
            │ • Status Coleta  │                  │
            └──────────────────┘──────────────────┘──────────────┘
```

---

## 🔄 Fluxo de Interação

### Fluxo 1: Buscar Patrimônio
```
[Usuário] → [Toca 🔍] → [Digite termo] → [Lista filtra] → [Toca item] → [Ver detalhes]
                                              │
                                              └─→ [Contador atualiza]
```

### Fluxo 2: Ordenar Lista
```
[Usuário] → [Toca 📊] → [Seleciona critério] → [Lista reordena] → [Visualiza]
                              │
                              ├─→ Número ↑↓
                              ├─→ Descrição A-Z
                              └─→ Setor A-Z
```

### Fluxo 3: Ver Detalhes
```
[Usuário] → [Toca item] → [Diálogo abre] → [Visualiza info] → [Opções]
                                                                    │
                                                    ┌───────────────┼───────────────┐
                                                    │               │               │
                                                    ▼               ▼               ▼
                                              [Escanear QR]    [Fechar]      [Compartilhar]
```

### Fluxo 4: Ações Rápidas
```
[Usuário] → [Toca FAB ⚡] → [Menu abre] → [Seleciona ação]
                                              │
                                ┌─────────────┼─────────────┐
                                │             │             │
                                ▼             ▼             ▼
                          [Scanner]    [Atualizar]   [Exportar]
```

---

## 🎨 Hierarquia Visual

```
┌─────────────────────────────────────────────────────────────┐
│ ← Inventário                              🔍 📊 ⚙️          │ ← Toolbar
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌───────────────────────────────────────────────────────┐ │
│  │ 📦 Total: 1,234 patrimônios                           │ │ ← Header
│  └───────────────────────────────────────────────────────┘ │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐ │
│  │ 123456                              [Coletado]        │ │
│  │ Computador Desktop Dell                               │ │
│  │ Dell | OptiPlex 7090                                  │ │ ← Item Card
│  │ 🏢 Tecnologia da Informação  🚪 Sala 101             │ │
│  └───────────────────────────────────────────────────────┘ │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐ │
│  │ 123457                              [Pendente]        │ │
│  │ Monitor LCD 24"                                       │ │
│  │ LG | 24MK430H                                         │ │ ← Item Card
│  │ 🏢 Tecnologia da Informação  🚪 Sala 102             │ │
│  └───────────────────────────────────────────────────────┘ │
│                                                             │
│  [Mais itens...]                                            │
│                                                             │
│                                                      [⚡]   │ ← FAB
└─────────────────────────────────────────────────────────────┘
```

---

## 🔀 Estados da Tela

### Estado 1: Normal (Lista Completa)
```
┌─────────────────────┐
│ Toolbar             │
├─────────────────────┤
│ 📦 Total: 1,234     │
├─────────────────────┤
│ [Item 1]            │
│ [Item 2]            │
│ [Item 3]            │
│ ...                 │
│                [⚡] │
└─────────────────────┘
```

### Estado 2: Buscando
```
┌─────────────────────┐
│ [🔍 dell____]       │ ← SearchView ativa
├─────────────────────┤
│ 📦 Encontrados:     │
│     15 de 1,234     │
├─────────────────────┤
│ [Item Dell 1]       │
│ [Item Dell 2]       │
│ [Item Dell 3]       │
│                [⚡] │
└─────────────────────┘
```

### Estado 3: Ordenando
```
┌─────────────────────┐
│ Toolbar             │
├─────────────────────┤
│ ┌─────────────────┐ │
│ │ Ordenar por     │ │ ← Diálogo
│ │ ○ Número ↑      │ │
│ │ ● Descrição A-Z │ │
│ │ ○ Setor A-Z     │ │
│ │   [Cancelar]    │ │
│ └─────────────────┘ │
└─────────────────────┘
```

### Estado 4: Visualizando Detalhes
```
┌─────────────────────┐
│ Detalhes            │
├─────────────────────┤
│ Número: 123456      │
│                     │
│ Descrição:          │
│ Computador Dell     │
│                     │
│ Marca: Dell         │
│ Modelo: OptiPlex    │
│ ...                 │
├─────────────────────┤
│ [Escanear] [Fechar] │
└─────────────────────┘
```

### Estado 5: Menu de Ações
```
┌─────────────────────┐
│ Toolbar             │
├─────────────────────┤
│ Lista...            │
│                     │
│ ┌─────────────────┐ │
│ │ Ações Rápidas   │ │ ← Menu FAB
│ │ 📷 Escanear QR  │ │
│ │ 🔄 Atualizar    │ │
│ │ 📤 Exportar     │ │
│ │   [Cancelar]    │ │
│ └─────────────────┘ │
│                [⚡] │
└─────────────────────┘
```

### Estado 6: Carregando
```
┌─────────────────────┐
│ Toolbar             │
├─────────────────────┤
│    ⟳ Carregando...  │ ← Spinner
│                     │
│                     │
│                     │
└─────────────────────┘
```

### Estado 7: Lista Vazia
```
┌─────────────────────┐
│ Toolbar             │
├─────────────────────┤
│ 📦 Total: 0         │
├─────────────────────┤
│                     │
│      📦             │ ← Ícone vazio
│  Nenhum patrimônio  │
│   encontrado        │
│                     │
│                [⚡] │
└─────────────────────┘
```

---

## 🎯 Pontos de Interação

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  1️⃣ Voltar          2️⃣ Buscar    3️⃣ Ordenar   4️⃣ Filtros  │
│  ←                  🔍           📊          ⚙️           │
│                                                             │
│  5️⃣ Contador                                                │
│  📦 Total: 1,234 patrimônios                               │
│                                                             │
│  6️⃣ Item da Lista (clicável)                                │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ 123456                              [Coletado]      │   │
│  │ Computador Desktop Dell                             │   │
│  │ Dell | OptiPlex 7090                                │   │
│  │ 🏢 TI  🚪 Sala 101                                  │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  7️⃣ Pull to Refresh (arraste para baixo)                    │
│                                                             │
│  8️⃣ FAB - Ações Rápidas                                     │
│                                                      [⚡]   │
└─────────────────────────────────────────────────────────────┘

Legenda:
1️⃣ Voltar para tela anterior
2️⃣ Abrir busca em tempo real
3️⃣ Abrir menu de ordenação
4️⃣ Abrir filtros avançados
5️⃣ Contador contextual
6️⃣ Tocar para ver detalhes
7️⃣ Arrastar para atualizar
8️⃣ Menu de ações rápidas
```

---

## 📊 Matriz de Funcionalidades

| Funcionalidade | Entrada | Processamento | Saída |
|----------------|---------|---------------|-------|
| **Busca** | Texto digitado | Filtro em memória | Lista filtrada |
| **Ordenação** | Critério selecionado | Sort em memória | Lista ordenada |
| **Detalhes** | Toque no item | Busca dados | Diálogo completo |
| **Ações Rápidas** | Toque no FAB | Menu de opções | Ação executada |
| **Atualizar** | Pull/Menu | Request API | Lista atualizada |

---

## 🔗 Integração com Outras Telas

```
┌──────────────┐
│   Login      │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│  Dashboard   │
└──────┬───────┘
       │
       ▼
┌──────────────┐     ┌──────────────┐
│  Inventário  │────→│   Scanner    │
│   (v1.3.0)   │     │   QR Code    │
└──────┬───────┘     └──────────────┘
       │
       ├─────→ ┌──────────────┐
       │       │   Filtros    │
       │       └──────────────┘
       │
       ├─────→ ┌──────────────┐
       │       │   Detalhes   │
       │       └──────────────┘
       │
       └─────→ ┌──────────────┐
               │    Coleta    │
               └──────────────┘
```

---

## 🎨 Paleta de Cores

```
┌─────────────────────────────────────┐
│ Status de Coleta                    │
├─────────────────────────────────────┤
│ 🟢 Verde (#4CAF50)  → Coletado      │
│ 🟠 Laranja (#FF9800) → Pendente     │
│ 🔵 Azul (#2196F3)   → Número        │
│ ⚫ Cinza (#757575)  → Secundário    │
└─────────────────────────────────────┘
```

---

## 📐 Dimensões e Espaçamentos

```
Card de Item:
┌─────────────────────────────────────┐
│ ↕ 16dp padding                      │
│ ← 16dp → [Conteúdo] ← 16dp →       │
│ ↕ 16dp padding                      │
└─────────────────────────────────────┘
↕ 8dp margin

Ícones:
• Toolbar: 24dp x 24dp
• Card: 16dp x 16dp
• FAB: 56dp x 56dp

Textos:
• Número: 18sp (bold)
• Descrição: 16sp
• Marca/Modelo: 14sp
• Setor/Sala: 12sp
```

---

**Versão**: 1.3.0  
**Data**: 03/11/2025  
**Tipo**: Diagrama Visual
