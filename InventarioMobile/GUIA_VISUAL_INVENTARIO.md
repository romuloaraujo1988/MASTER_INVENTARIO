# Guia Visual - Tela de Inventário

## 📱 Visão Geral da Tela

```
┌─────────────────────────────────────┐
│  ← Inventário    🔍 📊 ⚙️          │ ← Toolbar
├─────────────────────────────────────┤
│ ┌─────────────────────────────────┐ │
│ │ 📦 Total: 1,234 patrimônios     │ │ ← Contador
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ 123456              [Coletado]  │ │
│ │ Computador Desktop Dell         │ │
│ │ Dell | OptiPlex 7090            │ │
│ │ 🏢 TI    🚪 Sala 101            │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ 123457              [Pendente]  │ │
│ │ Monitor LCD 24"                 │ │
│ │ LG | 24MK430H                   │ │
│ │ 🏢 TI    🚪 Sala 102            │ │
│ └─────────────────────────────────┘ │
│                                     │
│                              [⚡]   │ ← FAB (Ações)
└─────────────────────────────────────┘
```

## 🔍 Funcionalidade de Busca

### Como Usar
1. Toque no ícone 🔍 no menu superior
2. Digite o termo de busca
3. A lista filtra automaticamente

### O que pode buscar
- ✅ Número do patrimônio
- ✅ Descrição
- ✅ Marca
- ✅ Modelo
- ✅ Setor
- ✅ Sala

### Exemplo de Busca
```
Busca: "dell"
Resultado:
┌─────────────────────────────────┐
│ 📦 Encontrados: 15 de 1,234     │
├─────────────────────────────────┤
│ 123456 - Computador Dell        │
│ 123789 - Notebook Dell Latitude │
│ 124001 - Monitor Dell 27"       │
└─────────────────────────────────┘
```

## 📊 Funcionalidade de Ordenação

### Como Usar
1. Toque no ícone 📊 no menu superior
2. Selecione o critério de ordenação
3. A lista reordena imediatamente

### Opções Disponíveis
```
┌─────────────────────────────┐
│     Ordenar por             │
├─────────────────────────────┤
│ ○ Número (Crescente)        │
│ ○ Número (Decrescente)      │
│ ○ Descrição (A-Z)           │
│ ○ Descrição (Z-A)           │
│ ○ Setor (A-Z)               │
│ ● Setor (Z-A)               │ ← Selecionado
├─────────────────────────────┤
│           [Cancelar]        │
└─────────────────────────────┘
```

## 📋 Detalhes do Patrimônio

### Como Acessar
- Toque em qualquer item da lista

### Informações Exibidas
```
┌─────────────────────────────────┐
│  Detalhes do Patrimônio         │
├─────────────────────────────────┤
│ Número: 123456                  │
│                                 │
│ Descrição: Computador Desktop   │
│ Dell OptiPlex                   │
│                                 │
│ Marca: Dell                     │
│ Modelo: OptiPlex 7090           │
│ Nº Série: BR123456789           │
│ Estado: Bom                     │
│ Valor: R$ 3.500,00              │
│                                 │
│ Setor: Tecnologia da Informação│
│ Sala: Sala 101                  │
│ Responsável: João Silva         │
│                                 │
│ Status: Coletado                │
│ Data Coleta: 01/11/2025         │
│                                 │
│ Observações: Equipamento novo   │
├─────────────────────────────────┤
│ [Escanear QR]      [Fechar]    │
└─────────────────────────────────┘
```

## ⚡ Menu de Ações Rápidas

### Como Acessar
- Toque no botão flutuante (FAB) no canto inferior direito

### Ações Disponíveis
```
┌─────────────────────────────┐
│    Ações Rápidas            │
├─────────────────────────────┤
│ 📷 Escanear QR Code         │
│ 🔄 Atualizar Lista          │
│ 📤 Exportar Dados           │
├─────────────────────────────┤
│         [Cancelar]          │
└─────────────────────────────┘
```

## 🎨 Indicadores Visuais

### Status de Coleta
```
[Coletado]  ← Verde (patrimônio já coletado)
[Pendente]  ← Laranja (aguardando coleta)
```

### Contador Contextual
```
Normal:
📦 Total: 1,234 patrimônios

Com Busca:
📦 Encontrados: 15 de 1,234 patrimônios

Com Paginação:
📦 Total: 5,000 patrimônios (1,234 carregados)
```

## 🔄 Atualização da Lista

### Métodos de Atualização
1. **Pull to Refresh**: Arraste a lista para baixo
2. **Menu de Ações**: Toque em "Atualizar Lista"
3. **Automático**: Ao aplicar filtros

### Indicador de Carregamento
```
┌─────────────────────────────────┐
│  ← Inventário    🔍 📊 ⚙️       │
├─────────────────────────────────┤
│        ⟳ Carregando...          │ ← Spinner
│                                 │
└─────────────────────────────────┘
```

## 📱 Navegação

### Voltar
- Toque na seta ← no canto superior esquerdo
- Ou use o botão voltar do Android

### Ir para Scanner
- Menu de Ações → Escanear QR Code
- Ou detalhes do item → Escanear QR

### Aplicar Filtros
- Menu superior → ícone ⚙️
- Selecione responsável e/ou status

## 💡 Dicas de Uso

### Busca Eficiente
- Use termos curtos e específicos
- Busque por número para encontrar rapidamente
- Busque por setor para ver todos de uma área

### Ordenação Inteligente
- Use "Setor (A-Z)" para organizar por localização
- Use "Descrição (A-Z)" para encontrar por tipo
- Use "Número" para ordem padrão

### Visualização Rápida
- Toque no item para ver todos os detalhes
- Use o contador para saber quantos itens há
- Observe as cores dos status

### Performance
- A busca funciona offline (dados já carregados)
- Ordenação é instantânea
- Scroll infinito carrega mais itens automaticamente

## 🎯 Casos de Uso Comuns

### 1. Encontrar um Patrimônio Específico
```
1. Toque em 🔍
2. Digite o número ou descrição
3. Toque no resultado
4. Veja os detalhes completos
```

### 2. Ver Todos de um Setor
```
1. Toque em ⚙️ (Filtros)
2. Selecione o responsável do setor
3. Aplique o filtro
4. Opcionalmente, ordene por sala
```

### 3. Verificar Pendências
```
1. Toque em ⚙️ (Filtros)
2. Selecione "Não Coletado"
3. Veja lista de pendentes
4. Toque em item para coletar
```

### 4. Coletar Patrimônio
```
1. Toque no FAB ⚡
2. Selecione "Escanear QR Code"
3. Escaneie o código
4. Confirme a coleta
```

## 🔧 Solução de Problemas

### Lista Vazia
- Verifique se há conexão com internet
- Tente atualizar (pull to refresh)
- Verifique se há filtros aplicados

### Busca Não Encontra
- Verifique a ortografia
- Tente termos mais genéricos
- Limpe filtros aplicados

### Lentidão
- Aguarde carregamento completo
- Feche e reabra o app
- Limpe cache nas configurações

---

**Versão**: 1.3.0  
**Última Atualização**: 03/11/2025
