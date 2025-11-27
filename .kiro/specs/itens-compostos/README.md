# Spec: Itens Compostos - Sistema de Inventário Patrimonial

## 📋 Status: ✅ APROVADO - Pronto para Implementação

**Data de Criação:** 27/11/2025  
**Última Atualização:** 27/11/2025  
**Versão:** 1.0  
**Escopo:** Aplicação Desktop (Java Swing)

---

## 🎯 Objetivo

Permitir que a coordenadora e gestores gerenciem patrimônios compostos por múltiplos componentes físicos (ex: mesa + cadeiras) e gerem relatórios detalhados para identificar ausências de componentes em conjuntos.

---

## 📚 Documentos da Spec

### 1. [requirements.md](requirements.md)
**10 Requirements** com 50+ critérios de aceitação:
- Req 1: Gestão de componentes
- Req 2: Relatórios detalhados
- Req 3: Exportação (Excel, PDF, CSV)
- Req 4: Estatísticas por tipo de componente
- Req 5: Detecção automática de padrões
- Req 6: Configuração de padrões
- Req 7: Filtros avançados
- Req 8: Indicadores visuais
- Req 9: Análise comparativa temporal
- Req 10: **Aplicação em lote por descrição** ⭐ NOVO

### 2. [design.md](design.md)
**Design técnico completo:**
- Arquitetura em 4 camadas
- 5 novas tabelas + 2 views
- 15+ classes de modelo
- 8 services especializados
- 30 correctness properties
- Mockups de UI detalhados
- Plano de deployment

### 3. [tasks.md](tasks.md)
**Plano de implementação:**
- 11 fases sequenciais
- 100+ tarefas específicas
- Estimativa: 20 dias úteis (~4 semanas)
- 2 checkpoints de qualidade
- Property-based tests integrados

---

## 🌟 Funcionalidades Principais

### 1. Gestão de Itens Compostos
- Marcar patrimônios como compostos
- Definir componentes esperados (tipo, descrição, quantidade)
- Validações automáticas

### 2. Aplicação em Lote por Descrição ⭐ DESTAQUE
- Agrupar patrimônios por descrição única
- Selecionar descrição → aplicar em TODOS os patrimônios
- Exemplo: "MESA COM 4 CADEIRAS" (500 patrimônios) → 1 clique
- Barra de progresso em tempo real
- Rollback automático em caso de erro

### 3. Relatórios Gerenciais
- Filtros avançados (inventário, setor, sala, status)
- Estatísticas resumidas (completos, incompletos, taxa de integridade)
- Destaque visual para conjuntos incompletos
- Estatísticas por tipo de componente
- Análise comparativa entre inventários

### 4. Exportação Profissional
- **Excel:** 4 abas (Resumo, Completos, Incompletos, Detalhamento)
- **PDF:** Documento formatado com gráficos de pizza
- **CSV:** Formato simples para análise externa

### 5. Detecção Automática
- Padrões configuráveis (regex ou palavras-chave)
- Sugestão automática de componentes
- Aplicação em lote de padrões

### 6. Análise Temporal
- Comparar integridade entre inventários
- Identificar tendências de perda/deterioração
- Linha do tempo por conjunto

---

## 🗄️ Estrutura de Banco de Dados

### Novas Tabelas (5)

1. **TABELA_ITEM_COMPOSTO**
   - Marca patrimônios como compostos
   - Relaciona 1:1 com TABELA_PATRIMONIO

2. **TABELA_COMPONENTE**
   - Define componentes esperados
   - Relaciona 1:N com TABELA_ITEM_COMPOSTO

3. **TABELA_COMPONENTE_COLETA**
   - Registra componentes encontrados
   - Relaciona com TABELA_COLETA + TABELA_INVENTARIO

4. **TABELA_PADRAO_DETECCAO**
   - Configura padrões de detecção automática

5. **TABELA_COMPONENTE_PADRAO**
   - Define componentes de cada padrão

### Views (2)

- **VIEW_ITEM_COMPOSTO_RESUMO**: Join completo com patrimônio, sala, setor
- **VIEW_COMPONENTE_STATUS_INVENTARIO**: Status por inventário

---

## 🎨 Principais Telas

### 1. ItemCompostoFrame
Gestão de componentes de um patrimônio específico

### 2. RelatorioItemCompostoFrame
Relatórios com filtros avançados e estatísticas

### 3. DeteccaoEmLoteDialog ⭐ NOVO
Lista descrições únicas → Aplicação em lote

### 4. EstatisticasPorTipoDialog
Análise agregada por tipo de componente

### 5. AnaliseComparativaDialog
Comparação entre inventários

---

## 📊 Estimativa de Implementação

| Fase | Descrição | Dias |
|------|-----------|------|
| 1 | Banco de Dados | 1 |
| 2 | Modelos | 1 |
| 3 | DAOs | 2 |
| 4 | Services | 2 |
| 5 | UI - Gestão | 2 |
| 6 | UI - Relatórios | 3 |
| 7 | Exportação | 2 |
| 8 | Integração + Lote | 3 |
| 9 | Testes | 2 |
| 10 | Documentação | 1 |
| 11 | Deploy | 1 |
| **TOTAL** | | **20 dias** |

---

## ✅ Correctness Properties

**30 propriedades formais** garantindo:
- Validações (descrição mínima, quantidades)
- Persistência (round trip)
- Cálculos (invariantes matemáticas)
- Relatórios (completude, ordenação, filtragem)
- Detecção automática (padrões, extração)
- Análise comparativa (tendências temporais)
- **Aplicação em lote (atomicidade, progresso)** ⭐ NOVO

---

## 🚀 Como Começar a Implementação

### Passo 1: Revisar Documentos
```bash
# Ler requirements
cat .kiro/specs/itens-compostos/requirements.md

# Ler design
cat .kiro/specs/itens-compostos/design.md

# Ler tasks
cat .kiro/specs/itens-compostos/tasks.md
```

### Passo 2: Executar Primeira Tarefa
Abrir `tasks.md` e clicar em **"Start task"** na tarefa 1.1:
```
- [ ] 1.1 Criar script SQL para TABELA_ITEM_COMPOSTO
```

### Passo 3: Seguir Ordem das Fases
As fases têm dependências:
```
DB → Models → DAOs → Services → UI → Integração → Testes → Deploy
```

### Passo 4: Checkpoints
Executar testes em:
- Checkpoint 1: Após Fase 8 (antes de testes)
- Checkpoint 2: Após Fase 11 (validação em produção)

---

## 🎯 Diferenciais desta Spec

✅ **Aplicação em Lote Inteligente**: Configurar 500 itens em 1 clique  
✅ **Detecção Automática**: Padrões configuráveis  
✅ **Relatórios Profissionais**: Excel com 4 abas, PDF com gráficos  
✅ **Análise Temporal**: Comparar inventários ao longo do tempo  
✅ **Property-Based Testing**: 30 propriedades formais  
✅ **Preserva Estrutura**: Sem modificar tabelas existentes  
✅ **Desktop Only**: Não impacta app mobile  

---

## 📞 Próximos Passos

1. ✅ Spec aprovada
2. ⏳ Iniciar Fase 1: Banco de Dados
3. ⏳ Implementar tarefas sequencialmente
4. ⏳ Executar checkpoints
5. ⏳ Deploy em produção

---

## 📝 Notas Importantes

- **Tarefas opcionais** marcadas com `*` (testes avançados)
- **Cada tarefa** referencia requirements específicos
- **Property-based tests** distribuídos nas fases relevantes
- **Transações** garantem atomicidade na aplicação em lote
- **Rollback automático** em caso de erro ou cancelamento

---

**Spec criada por:** Kiro AI Assistant  
**Aprovada por:** Usuário  
**Status:** ✅ Pronto para Implementação  
**Versão:** 1.0 - Final
