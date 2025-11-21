# 📱 Nova Tela: Patrimônios por Sala - Resumo Executivo

## 🎯 O Que Será Criado

Uma tela onde o usuário pode:
1. **Selecionar uma sala** (dropdown com todas as salas)
2. **Ver patrimônios** daquela sala
3. **Filtrar** por: Todos / Coletados / Não Coletados
4. **Ver estatísticas** da sala (total, coletados, pendentes, %)
5. **Coletar diretamente** da lista

---

## 🏗️ Arquitetura Segura

### Clean Architecture + MVVM (Sem Riscos)

```
UI (Activity)
    ↓
ViewModel (gerencia estado)
    ↓
Use Cases (regras de negócio)
    ↓
Repository (acesso a dados)
    ↓
Room Database (local)
```

**Por que é seguro:**
- ✅ Separação clara de responsabilidades
- ✅ Cada camada testável independentemente
- ✅ Mudanças isoladas (não afeta resto do app)
- ✅ Padrão já usado no app

---

## 📦 Componentes Novos (Não Afeta Código Existente)

### 1. Domain Layer (Novos arquivos)
- `PatrimonioPorSala.kt` - Model
- `SalaComEstatisticas.kt` - Model
- `FiltroColeta.kt` - Enum
- `BuscarPatrimoniosPorSalaUseCase.kt` - Use Case
- `BuscarSalasComEstatisticasUseCase.kt` - Use Case

### 2. Data Layer (Adiciona métodos, não modifica existentes)
- `PatrimonioDao.kt` - Adiciona 4 métodos novos
- `SalaDao.kt` - Adiciona 1 método novo
- `PatrimonioRepositoryImpl.kt` - Adiciona 1 método novo

### 3. Presentation Layer (Novos arquivos)
- `PatrimoniosPorSalaState.kt` - Estados
- `PatrimoniosPorSalaViewModel.kt` - ViewModel
- `PatrimoniosPorSalaActivity.kt` - Activity
- `PatrimoniosPorSalaAdapter.kt` - Adapter
- `activity_patrimonios_por_sala.xml` - Layout

**Total:** ~15 arquivos novos, 3 arquivos modificados (apenas adicionando métodos)

---

## ⚠️ Riscos Identificados e Mitigações

### ✅ Risco 1: Performance
**Problema:** Sala com 1000+ patrimônios pode travar

**Mitigação:**
- Paging 3 para paginação
- Lazy loading
- Índices no banco

### ✅ Risco 2: Memória
**Problema:** Carregar muitos dados

**Mitigação:**
- Cache LRU
- Limpar ao sair da tela
- Processar em lotes

### ✅ Risco 3: Sincronização
**Problema:** Dados desatualizados

**Mitigação:**
- Offline-first (sempre do banco local)
- Botão "Atualizar"
- Indicador de última sync

### ✅ Risco 4: Conflitos
**Problema:** Múltiplas ações simultâneas

**Mitigação:**
- StateFlow (thread-safe)
- Desabilitar botões durante loading
- Cancelar operações anteriores

---

## 📋 Implementação em 5 Fases Seguras

### Fase 1: Domain (2h - Sem Risco)
Criar models e use cases puros (sem dependências)

### Fase 2: Data (3h - Baixo Risco)
Adicionar métodos nos DAOs e Repositories

### Fase 3: Presentation (2h - Médio Risco)
Criar ViewModel e States

### Fase 4: UI (4h - Alto Risco)
Criar Activity, Adapter e Layouts

### Fase 5: Testes (3h - Médio Risco)
Testar todas as funcionalidades

**Total:** 14 horas (~2 dias de trabalho)

---

## 🧪 Plano de Testes Completo

### Testes Unitários
- [ ] Use Cases
- [ ] ViewModel
- [ ] Mappers

### Testes de Integração
- [ ] DAOs
- [ ] Repositories

### Testes de UI
- [ ] Carregar salas
- [ ] Selecionar sala
- [ ] Aplicar filtros
- [ ] Coletar patrimônio
- [ ] Performance (1000+ itens)

### Testes de Cenários
- [ ] Offline
- [ ] Sincronização
- [ ] Erros de rede
- [ ] Sala vazia
- [ ] Todos coletados

---

## 📊 Mockup da Tela

```
┌─────────────────────────────────────────┐
│ ← Patrimônios por Sala                  │
├─────────────────────────────────────────┤
│                                         │
│ Selecione uma sala ▼                    │
│ [Sala 101 - Laboratório]                │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │  Total: 50  │ Coletados: 30 │ Pend: 20│ │
│ └─────────────────────────────────────┘ │
│                                         │
│ [Todos] [Coletados] [Não Coletados]    │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ 📦 12345 - Cadeira Giratória        │ │
│ │    Responsável: João Silva          │ │
│ │    [Coletar]                        │ │
│ ├─────────────────────────────────────┤ │
│ │ 📦 12346 - Mesa de Escritório       │ │
│ │    Responsável: Maria Santos        │ │
│ │    [Coletar]                        │ │
│ ├─────────────────────────────────────┤ │
│ │ ✅ 12347 - Computador Desktop       │ │
│ │    Coletado por: Pedro (15/11)      │ │
│ └─────────────────────────────────────┘ │
│                                         │
└─────────────────────────────────────────┘
```

---

## ✅ Benefícios

### Para Usuários
- 🎯 **Foco:** Coletar sala por sala de forma organizada
- 📊 **Visibilidade:** Ver progresso de cada sala
- ⚡ **Rapidez:** Filtrar apenas o que precisa
- 📱 **Praticidade:** Coletar direto da lista

### Para Gestores
- 📈 **Controle:** Acompanhar progresso por sala
- 📊 **Métricas:** Estatísticas em tempo real
- 🎯 **Planejamento:** Priorizar salas pendentes
- 📋 **Relatórios:** Dados organizados por local

### Para o Sistema
- 🏗️ **Arquitetura:** Mantém padrão Clean Architecture
- 🔒 **Segurança:** Não afeta código existente
- 🧪 **Testável:** Cada camada testável
- 📚 **Manutenível:** Código organizado e documentado

---

## 🚀 Próximos Passos

### Opção 1: Implementação Completa
Implementar todas as 5 fases de uma vez (14h)

### Opção 2: Implementação Incremental (Recomendado)
1. **Semana 1:** Fases 1-3 (Domain + Data + Presentation)
2. **Semana 2:** Fase 4 (UI)
3. **Semana 3:** Fase 5 (Testes e ajustes)

### Opção 3: MVP Mínimo
Implementar versão básica sem filtros avançados (8h)

---

## 📝 Decisão Necessária

**Preciso de aprovação para:**
1. ✅ Criar novos arquivos (15 arquivos)
2. ✅ Modificar 3 arquivos existentes (apenas adicionar métodos)
3. ✅ Adicionar tela no menu principal
4. ✅ Tempo estimado: 14 horas

**Garantias:**
- ✅ Não afeta funcionalidades existentes
- ✅ Código isolado e testável
- ✅ Segue padrões do projeto
- ✅ Documentação completa
- ✅ Plano de testes detalhado

---

## ❓ Perguntas para Decidir

1. **Quando implementar?**
   - [ ] Agora (próximas 2 semanas)
   - [ ] Depois (após outras prioridades)
   - [ ] MVP primeiro (versão simplificada)

2. **Escopo completo ou MVP?**
   - [ ] Completo (com todos os filtros e estatísticas)
   - [ ] MVP (apenas lista básica)

3. **Prioridade?**
   - [ ] Alta (usuários precisam urgente)
   - [ ] Média (seria útil)
   - [ ] Baixa (pode esperar)

---

**Aguardando aprovação para iniciar implementação!** 🚀

---

**Versão:** 1.0.0  
**Data:** 18/11/2025  
**Status:** 📋 AGUARDANDO APROVAÇÃO  
**Documentação:** Completa  
**Riscos:** Identificados e Mitigados  
**Tempo:** 14 horas estimadas
