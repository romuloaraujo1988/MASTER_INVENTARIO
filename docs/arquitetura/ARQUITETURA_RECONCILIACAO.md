# Arquitetura - Sistema de Reconciliação

**Data:** 12/12/2025  
**Versão:** 1.0.0

---

## 🏗️ Arquitetura Geral

```
┌─────────────────────────────────────────────────────────────────┐
│                    ReconciliacaoFrame (UI)                       │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ Painel Superior: Filtros + Estatísticas                 │   │
│  │ - Seletor de Inventário                                 │   │
│  │ - Slider de Similaridade (0-100%)                       │   │
│  │ - Botão "Buscar Sugestões"                              │   │
│  │ - Labels de Estatísticas                                │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ Abas (JTabbedPane)                                       │   │
│  │ ├─ Sugestões de Reconciliação (com checkbox)            │   │
│  │ ├─ Patrimônios Não Encontrados                          │   │
│  │ ├─ Itens Sem Etiqueta                                   │   │
│  │ └─ Histórico de Reconciliações                          │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ Painel Inferior: Botões de Ação                         │   │
│  │ - Atualizar, Exportar, Fechar                           │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    ReconciliacaoDAO (Data)                       │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ Métodos:                                                 │   │
│  │ • buscarPatrimoniosNaoEncontrados()                      │   │
│  │ • buscarItensSemEtiqueta()                               │   │
│  │ • calcularSimilaridade()                                 │   │
│  │ • buscarSugestoesReconciliacao()                         │   │
│  │ • registrarReconciliacao()                               │   │
│  │ • atualizarStatusReconciliacao()                         │   │
│  │ • buscarReconciliacoesPorInventario()                    │   │
│  │ • contarEstatisticas()                                   │   │
│  │ • excluirReconciliacao()                                 │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                  PostgreSQL Database                             │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ Tabelas:                                                 │   │
│  │ • tabela_patrimonio (11.428 registros)                   │   │
│  │ • tabela_coleta (18 registros)                           │   │
│  │ • tabela_reconciliacao (0 registros)                     │   │
│  │ • tabela_inventario (1 registro)                         │   │
│  │ • tabela_usuario (múltiplos)                             │   │
│  │ • tabela_sala (múltiplas)                                │   │
│  │ • tabela_responsavel (múltiplos)                         │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔄 Fluxo de Dados - Buscar Sugestões

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. Usuário clica "Buscar Sugestões"                             │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 2. ReconciliacaoFrame.buscarSugestoes()                         │
│    - Obtém idInventario                                         │
│    - Obtém limiarSimilaridade do slider                         │
│    - Inicia SwingWorker (thread assíncrona)                     │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 3. ReconciliacaoDAO.buscarSugestoesReconciliacao()              │
│    a. Chama buscarPatrimoniosNaoEncontrados()                   │
│       └─ Query SQL: SELECT patrimônios não coletados            │
│    b. Chama buscarItensSemEtiqueta()                            │
│       └─ Query SQL: SELECT coletas sem etiqueta                 │
│    c. Para cada par (patrimônio, item):                         │
│       └─ Calcula similaridade (Levenshtein)                     │
│    d. Filtra por limiar                                         │
│    e. Ordena por similaridade decrescente                       │
│    f. Retorna List<Map<String, Object>>                         │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 4. ReconciliacaoFrame.done() (volta à thread principal)         │
│    - Limpa tabela de sugestões                                  │
│    - Popula com resultados                                      │
│    - Atualiza label de estatísticas                             │
│    - Mostra mensagem de sucesso/vazio                           │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 5. UI Atualizada                                                │
│    - Tabela com sugestões                                       │
│    - Checkbox para seleção                                      │
│    - Cores por similaridade (verde/amarelo/vermelho)            │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔄 Fluxo de Dados - Confirmar Sugestões

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. Usuário seleciona checkbox + clica "Confirmar"               │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 2. ReconciliacaoFrame.confirmarSelecionados()                   │
│    - Valida seleção (pelo menos 1)                              │
│    - Pede confirmação ao usuário                                │
│    - Inicia SwingWorker                                         │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 3. Para cada sugestão selecionada:                              │
│    a. Extrai dados da tabela                                    │
│    b. Busca IDs reais no banco                                  │
│    c. Chama ReconciliacaoDAO.registrarReconciliacao()           │
│       └─ INSERT INTO tabela_reconciliacao                       │
│    d. Incrementa contador de processados                        │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 4. ReconciliacaoFrame.done()                                    │
│    - Mostra mensagem de sucesso                                 │
│    - Recarrega dados (carregarDados())                          │
│    - Recarrega sugestões (buscarSugestoes())                    │
│    - Atualiza UI                                                │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📊 Estrutura de Dados - Sugestão

```
Map<String, Object> sugestao = {
    "patrimonio": {
        "id": 1,
        "numero": "001",
        "descricao": "CADEIRA GIRATÓRIA",
        "estadoConservacao": "BOM",
        "salaEsperada": "Sala 101",
        "numeroSala": "101",
        "responsavel": "João Silva"
    },
    "itemSemEtiqueta": {
        "id": 135,
        "descricao": "CADEIRA PARA LABORATÓRIO",
        "categoria": "MÓVEL",
        "localizacaoEncontrada": "Sala 107",
        "estadoEncontrado": "BOM",
        "dataColeta": Timestamp,
        "fotoPatrimonio": "path/to/photo.jpg",
        "observacao": "Encontrada em bom estado",
        "coletor": "Maria Santos"
    },
    "similaridade": 70.5
}
```

---

## 🧮 Algoritmo de Similaridade

```
┌─────────────────────────────────────────────────────────────────┐
│ calcularSimilaridade(s1, s2)                                    │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 1. Normalizar strings                                           │
│    - Converter para UPPER CASE                                  │
│    - Remover espaços extras (trim)                              │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 2. Verificar casos especiais                                    │
│    - Se iguais: retornar 100%                                   │
│    - Se vazio: retornar 0%                                      │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 3. Verificar containment                                        │
│    - Se s1 contém s2 ou vice-versa:                             │
│      similaridade = (menor_tamanho / maior_tamanho) * 100       │
│    - Exemplo: "CADEIRA" em "CADEIRA GIRATÓRIA" = 100%           │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 4. Calcular Levenshtein Distance                                │
│    - Matriz DP[i][j] = distância entre s1[0..i] e s2[0..j]     │
│    - Operações: inserção, deleção, substituição                 │
│    - Custo: 1 por operação                                      │
│                                                                  │
│    Exemplo: "CADEIRA" vs "CADEIRA GIRATÓRIA"                    │
│    - Distância: 10 (inserir " GIRATÓRIA")                       │
│    - Tamanho maior: 17                                          │
│    - Similaridade: ((17 - 10) / 17) * 100 = 41%                │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 5. Retornar percentual (0-100%)                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📋 Estrutura de Tabelas

### tabela_patrimonio
```
id (PK)
numero (VARCHAR)
descricao (VARCHAR)
estado_conservacao (VARCHAR)
id_sala (FK)
id_responsavel (FK)
status (VARCHAR) = 'ATIVO'
...
```

### tabela_coleta
```
id (PK)
id_inventario (FK)
id_patrimonio (FK, nullable)
id_coletor (FK)
data_coleta (TIMESTAMP)
sem_etiqueta (BOOLEAN)
descricao_item_sem_etiqueta (VARCHAR)
categoria_item_sem_etiqueta (VARCHAR)
localizacao_encontrada (VARCHAR)
estado_encontrado (VARCHAR)
...
```

### tabela_reconciliacao
```
id (PK)
id_patrimonio (FK)
id_coleta_sem_etiqueta (FK)
id_inventario (FK)
id_usuario (FK)
data_reconciliacao (TIMESTAMP)
similaridade (NUMERIC)
status (VARCHAR) = 'PENDENTE'
observacoes (TEXT)
acao_tomada (VARCHAR)
```

---

## 🎨 Componentes UI

### ReconciliacaoFrame
```
JFrame
├── JPanel (BorderLayout)
│   ├── NORTH: Painel Superior
│   │   ├── Filtros (FlowLayout)
│   │   │   ├── JComboBox (Inventários)
│   │   │   ├── JSlider (Similaridade)
│   │   │   └── JButton (Buscar)
│   │   └── Estatísticas (FlowLayout)
│   │       ├── JLabel (Não Encontrados)
│   │       ├── JLabel (Sem Etiqueta)
│   │       ├── JLabel (Sugestões)
│   │       ├── JLabel (Pendentes)
│   │       └── JLabel (Confirmadas)
│   │
│   ├── CENTER: Abas (JTabbedPane)
│   │   ├── Sugestões
│   │   │   ├── JTable (com checkbox)
│   │   │   └── Botões (Confirmar/Rejeitar)
│   │   ├── Não Encontrados
│   │   │   ├── JTable
│   │   │   └── Botões (Ações/Vincular)
│   │   ├── Sem Etiqueta
│   │   │   └── JTable
│   │   └── Histórico
│   │       ├── JTable
│   │       └── Botão (Excluir)
│   │
│   └── SOUTH: Painel Botões
│       ├── JButton (Atualizar)
│       ├── JButton (Exportar)
│       └── JButton (Fechar)
```

---

## 🔌 Integrações

### Com Banco de Dados
```
ReconciliacaoDAO
    ↓
DatabaseConnection.getConnection()
    ↓
PostgreSQL (jdbc:postgresql://...)
```

### Com Outras Classes
```
ReconciliacaoFrame
    ├── ReconciliacaoDAO (operações de dados)
    ├── InventarioDAO (carregar inventários)
    ├── AcoesPatrimonioDialog (diálogo de ações)
    └── SwingWorker (operações assíncronas)
```

---

## 🚀 Fluxo de Inicialização

```
1. MainFrame abre ReconciliacaoFrame
2. ReconciliacaoFrame.__init__()
   ├── Cria ReconciliacaoDAO
   ├── Cria InventarioDAO
   ├── Chama initComponents()
   └── Chama carregarInventarios()
3. carregarInventarios()
   ├── Busca inventários do banco
   ├── Popula JComboBox
   └── Dispara carregarDados()
4. carregarDados()
   ├── Inicia SwingWorker
   ├── Carrega não encontrados
   ├── Carrega sem etiqueta
   ├── Carrega histórico
   └── Carrega estatísticas
5. UI Pronta para Uso
```

---

## 📊 Estatísticas em Tempo Real

```
ReconciliacaoDAO.contarEstatisticas(idInventario)
    ↓
Executa 3 queries SQL:
    1. COUNT(*) patrimônios não encontrados
    2. COUNT(*) itens sem etiqueta
    3. COUNT(*) reconciliações por status
    ↓
Retorna Map<String, Integer>:
    {
        "naoEncontrados": 11415,
        "semEtiqueta": 5,
        "reconciliacao_pendente": 0,
        "reconciliacao_confirmado": 0,
        "reconciliacao_rejeitado": 0
    }
    ↓
ReconciliacaoFrame atualiza labels
```

---

## ⚡ Performance

### Operação: Buscar Sugestões
```
Tempo Total: ~1 segundo

Breakdown:
├── Query patrimônios não encontrados: ~100ms
├── Query itens sem etiqueta: ~50ms
├── Cálculo de similaridade (5 x 11415 = 57.075 comparações): ~800ms
├── Filtro e ordenação: ~50ms
└── Renderização UI: ~50ms
```

### Escalabilidade
```
Com 1.000 itens sem etiqueta:
├── Comparações: 11.415.000
├── Tempo estimado: ~10 segundos
└── Recomendação: Implementar paginação

Com 10.000 itens sem etiqueta:
├── Comparações: 114.150.000
├── Tempo estimado: ~100 segundos
└── Recomendação: Implementar cache ou índices
```

---

## 🔐 Segurança

### Validações Implementadas
```
✅ Validação de seleção (pelo menos 1 item)
✅ Confirmação do usuário antes de ação
✅ Tratamento de exceções SQL
✅ Rastreamento de usuário (auditoria)
✅ Transações ACID no banco
```

### Dados Sensíveis
```
✅ Senhas não são exibidas
✅ Dados pessoais protegidos
✅ Histórico de auditoria mantido
✅ Exclusão lógica (soft delete) possível
```

---

**Arquitetura documentada em:** 12/12/2025  
**Versão:** 1.0.0  
**Status:** Completa e Operacional
