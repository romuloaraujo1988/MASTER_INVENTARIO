# Changelog - Tela de Inventário

## [1.3.0] - 2025-11-03

### ✨ Novas Funcionalidades

#### Busca em Tempo Real
- Adicionado SearchView no menu superior
- Busca instantânea em múltiplos campos (número, descrição, marca, modelo, setor, sala)
- Contador contextual mostrando resultados encontrados
- Funciona offline com dados já carregados

#### Sistema de Ordenação
- 6 opções de ordenação disponíveis:
  - Número (crescente/decrescente)
  - Descrição (A-Z / Z-A)
  - Setor (A-Z / Z-A)
- Diálogo intuitivo com seleção única
- Aplicação imediata da ordenação
- Mantém ordenação durante busca

#### Diálogo de Detalhes Completo
- Visualização completa de todas as informações do patrimônio
- Exibe: número, descrição, marca, modelo, série, estado, valor
- Mostra localização: setor, sala, responsável
- Indica status de coleta e data
- Botão para escanear QR Code direto dos detalhes

#### Menu de Ações Rápidas
- FAB (Floating Action Button) com menu de ações
- Opções: Escanear QR, Atualizar Lista, Exportar Dados
- Interface intuitiva e rápida

### 🎨 Melhorias Visuais

#### Layout dos Itens
- Adicionada exibição de marca e modelo
- Ícones melhorados para setor e sala
- Melhor espaçamento e organização
- Status de coleta com cores distintas

#### Interface Geral
- Contador inteligente com contexto
- Ícones mais intuitivos no menu
- Melhor feedback visual
- Animações suaves

### 🔧 Melhorias Técnicas

#### ViewModel
- Novo enum `SortOrder` para ordenação
- Estado expandido com `patrimoniosFiltered` e `searchQuery`
- Método `searchPatrimonios()` para busca
- Método `setSortOrder()` para ordenação
- Método `applyFiltersAndSort()` para aplicar filtros e ordenação
- Melhor separação de responsabilidades

#### Activity
- Implementação de SearchView com listener
- Diálogo de ordenação com AlertDialog
- Diálogo de detalhes formatado
- Menu de ações rápidas
- Melhor tratamento de estados da UI

#### Adapter
- Exibição de marca e modelo
- Melhor formatação de dados
- Uso eficiente de DiffUtil

### 📝 Documentação

#### Novos Arquivos
- `MELHORIAS_TELA_INVENTARIO.md` - Documentação técnica completa
- `GUIA_VISUAL_INVENTARIO.md` - Guia visual para usuários
- `CHANGELOG_INVENTARIO.md` - Este arquivo

### 🐛 Correções

- Corrigido problema de lista não atualizar após busca
- Corrigido contador não refletir filtros
- Melhorado tratamento de valores nulos
- Corrigida exibição de marca e modelo

### ⚡ Performance

- Busca e ordenação em memória (rápido)
- Lista filtrada separada da original
- Uso otimizado de StateFlow
- Atualizações eficientes com DiffUtil

---

## [1.2.0] - 2025-11-02

### Funcionalidades Anteriores
- Listagem básica de patrimônios
- Paginação infinita
- Pull to refresh
- Filtros por responsável e status
- Integração com scanner QR

---

## Próximas Versões Planejadas

### [1.4.0] - Planejado
- Exportação de dados (Excel/PDF)
- Filtros avançados (estado, valor, data)
- Visualizações alternativas (grade, tabela)
- Estatísticas e gráficos

### [1.5.0] - Planejado
- Ações em lote
- Seleção múltipla
- Sincronização otimizada
- Cache inteligente

---

## Compatibilidade

### Versão Mínima
- Android 5.0 (API 21)

### Versão Recomendada
- Android 8.0 (API 26) ou superior

### Backend
- Compatível com API Mobile v1.2.0+
- Suporta endpoints antigos e novos

---

## Migração

### De 1.2.0 para 1.3.0

Não há breaking changes. A atualização é transparente:

1. Instale o novo APK
2. Todas as funcionalidades anteriores continuam funcionando
3. Novas funcionalidades estarão disponíveis imediatamente

### Dados Locais
- Nenhuma migração de dados necessária
- Cache existente continua válido
- Sincronização automática mantida

---

## Contribuidores

- Sistema SIHCP - Desenvolvimento e implementação
- IFMT - Requisitos e testes

---

## Licença

Propriedade do Instituto Federal de Mato Grosso (IFMT)

---

**Última Atualização**: 03/11/2025  
**Versão Atual**: 1.3.0
