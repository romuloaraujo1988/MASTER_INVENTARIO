# Otimização de Performance na Coleta Patrimonial Desktop

## 📋 Resumo Executivo

Esta spec define otimizações abrangentes para eliminar lentidões no ColetaFrame_v2, proporcionando uma experiência ágil e responsiva para os coletores.

## 🎯 Objetivos

### Metas de Performance

| Operação | Antes | Meta | Melhoria |
|----------|-------|------|----------|
| Abrir ColetaFrame_v2 | 2-3s | < 500ms | **83% mais rápido** |
| Buscar patrimônio | 500ms | < 100ms | **80% mais rápido** |
| Carregar tabela | 3-5s | < 500ms | **90% mais rápido** |
| Registrar coleta | 800ms | < 300ms | **62% mais rápido** |
| Selecionar sala | 1-2s | < 200ms | **90% mais rápido** |

## 🏗️ Arquitetura de Solução

### Componentes Principais

1. **HikariCP Connection Pool**
   - Reutilização de conexões
   - 5-10 conexões ativas
   - Reduz latência de conexão

2. **CacheManager**
   - Cache em memória com TTL
   - Salas, inventário, patrimônios
   - Acesso < 10ms

3. **OptimizedSwingWorker**
   - Operações assíncronas
   - EDT sempre livre
   - Logging automático

4. **DebouncedSearchField**
   - Debounce de 300ms
   - Reduz queries desnecessárias
   - Busca imediata com Enter

5. **Índices SQL**
   - Busca por número: < 100ms
   - Busca por sala: < 500ms
   - Full-text search em descrição

6. **Paginação**
   - Carregar apenas 100 linhas
   - Carregamento incremental
   - Reduz uso de memória

## 📊 Benefícios Esperados

### Performance
- ✅ **83-90% redução** em tempos de carregamento
- ✅ **UI sempre responsiva** (EDT nunca bloqueia)
- ✅ **Cache hit < 10ms** para dados frequentes
- ✅ **Queries otimizadas** com índices

### Experiência do Usuário
- ✅ **Feedback imediato** (< 100ms)
- ✅ **Sem travamentos** durante operações
- ✅ **Busca inteligente** com debounce
- ✅ **Som de sucesso** ao coletar

### Recursos
- ✅ **Memória otimizada** (< 50MB para tabela)
- ✅ **Conexões reutilizadas** (5-10 conexões)
- ✅ **Paginação** reduz carga inicial

## 🚀 Plano de Implementação

### Fase 1: Infraestrutura (Alta Prioridade)
- Connection Pool (HikariCP)
- Migração de DAOs
- **Impacto:** Reduz latência de conexão

### Fase 2: Cache
- CacheManager singleton
- Cache de Salas e Inventário
- **Impacto:** Reduz queries repetidas

### Fase 3: Async Operations (Alta Prioridade)
- SwingWorker para todas operações pesadas
- EDT sempre livre
- **Impacto:** Elimina travamentos

### Fase 4: Busca Otimizada
- Debounce em campos de busca
- **Impacto:** Reduz queries desnecessárias

### Fase 5: SQL Otimizado (Alta Prioridade)
- Índices no PostgreSQL
- PreparedStatements
- **Impacto:** Acelera queries em 80-90%

### Fase 6: Paginação
- Carregar apenas 100 linhas
- **Impacto:** Reduz uso de memória

### Fases 7-9: Polimento
- Feedback visual
- Pré-carregamento
- Monitoramento

### Fase 10: Validação
- Testes de performance
- Documentação

## 📈 Métricas de Sucesso

### Obrigatórias
- [ ] Abrir frame < 500ms
- [ ] Buscar patrimônio < 100ms
- [ ] Carregar tabela < 500ms
- [ ] Registrar coleta < 300ms
- [ ] UI sempre responsiva (EDT livre)

### Desejáveis
- [ ] Cache hit < 10ms
- [ ] Connection pool reuse > 90%
- [ ] Queries usam índices
- [ ] Memória < 50MB

## 🧪 Estratégia de Testes

### Unit Tests (Opcionais)
- CacheManager
- DebouncedSearchField
- OptimizedSwingWorker

### Integration Tests (Opcionais)
- Database performance
- Connection pool reuse
- Query optimization

### Property-Based Tests (Opcionais)
- Cache consistency
- Debounce behavior
- EDT safety

### Manual Tests (Obrigatórios)
- Fluxo completo de coleta
- Performance benchmarks
- Validação de métricas

## 📝 Documentos

- [requirements.md](requirements.md) - 12 requisitos detalhados
- [design.md](design.md) - Arquitetura e componentes
- [tasks.md](tasks.md) - 60 tarefas de implementação

## 🎯 Próximos Passos

1. **Revisar e aprovar** esta spec
2. **Começar Fase 1** (Connection Pool)
3. **Testar incrementalmente** após cada fase
4. **Medir performance** e ajustar conforme necessário
5. **Documentar resultados** ao final

## ⚠️ Riscos e Mitigações

### Risco: Mudanças quebram funcionalidade existente
**Mitigação:** Implementação incremental com testes após cada fase

### Risco: Performance não atinge metas
**Mitigação:** Monitoramento contínuo e ajustes baseados em métricas

### Risco: Complexidade aumenta manutenção
**Mitigação:** Código bem documentado e padrões consistentes

## 📞 Suporte

Para dúvidas ou problemas durante implementação:
- Consultar [design.md](design.md) para detalhes técnicos
- Consultar [tasks.md](tasks.md) para ordem de implementação
- Executar testes após cada mudança

---

**Status:** ✅ Spec Aprovada  
**Versão:** 1.0  
**Data:** 25/11/2025  
**Prioridade:** Alta  
**Tempo Estimado:** 3-4 semanas
