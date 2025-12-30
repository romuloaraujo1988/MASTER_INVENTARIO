# Checklist de Implementação - Otimização de Coleta

## Fase 1: Rápida (CONCLUÍDA)

### Remover Query de Debug
- [x] Arquivo: `src/main/java/com/inventario/dao/ParticipanteInventarioDAO.java`
- [x] Remover query de debug que era executada a cada coleta
- [x] Impacto: -10% de queries
- [x] Compilar: `mvn clean compile`
- [x] Testar: Verificar se não há erros

---

## Fase 2: Cache (PRÓXIMA)

### Adicionar Variáveis de Cache
- [ ] Arquivo: `src/main/java/com/inventario/view/ColetaFrame_v2.java`
- [ ] Adicionar `idParticipanteCache`
- [ ] Adicionar `inventarioAtivoCache`
- [ ] Adicionar `ultimoRefreshCache`
- [ ] Adicionar `INTERVALO_REFRESH_CACHE`

### Implementar Métodos de Cache
- [ ] Implementar `obterIdParticipante()`
- [ ] Implementar `obterInventarioAtivo()`
- [ ] Implementar `invalidarCache()`

### Usar Cache em registrarItemEncontrado()
- [ ] Substituir `inventarioDAO.buscarPorStatus()` por `obterInventarioAtivo()`
- [ ] Substituir `participanteDAO.buscarIdParticipantePorUsuario()` por `obterIdParticipante()`
- [ ] Testar se coleta ainda funciona

### Invalidar Cache em Eventos
- [ ] Adicionar `invalidarCache()` em `logout()`
- [ ] Adicionar `invalidarCache()` em `mudarInventario()`
- [ ] Adicionar `invalidarCache()` em `atualizarParticipantes()`

### Compilar e Testar
- [ ] Compilar: `mvn clean compile`
- [ ] Testar: Registrar 5 coletas
- [ ] Verificar logs: Procurar por `[CACHE]`
- [ ] Verificar performance: Deve ser mais rápido

---

## Fase 3: Índices (PRÓXIMA)

### Executar Script SQL
- [ ] Arquivo: `sql/criar_indices_performance_coleta.sql`
- [ ] Conectar ao PostgreSQL
- [ ] Executar script: `psql -h localhost -U inventario -d sispatrimonio -f sql/criar_indices_performance_coleta.sql`
- [ ] Verificar se índices foram criados

### Verificar Índices
- [ ] Executar query: `SELECT indexname FROM pg_indexes WHERE tablename IN ('tabela_coleta', 'tabela_participante_inventario')`
- [ ] Verificar se 7 índices foram criados:
  - [ ] `idx_participante_inventario_usuario`
  - [ ] `idx_coleta_inventario_patrimonio`
  - [ ] `idx_coleta_patrimonio_sala`
  - [ ] `idx_coleta_inventario`
  - [ ] `idx_coleta_coletor`
  - [ ] `idx_patrimonio_numero`
  - [ ] `idx_inventario_status`

---

## Fase 4: Testes (PRÓXIMA)

### Teste de Performance
- [ ] Registrar 50 coletas consecutivas
- [ ] Medir tempo total
- [ ] Comparar com tempo anterior
- [ ] Esperado: 3 segundos (antes: 25 segundos)

### Teste de Funcionalidade
- [ ] Coleta registra corretamente
- [ ] Sem duplicação de coletas
- [ ] Relatórios funcionam
- [ ] Sincronização funciona
- [ ] Sem erros no log

### Teste de Carga
- [ ] Registrar 100+ coletas
- [ ] Verificar se performance se degrada
- [ ] Verificar uso de memória
- [ ] Verificar uso de CPU

### Verificar Logs
- [ ] Procurar por `[CACHE]` - deve aparecer na primeira coleta
- [ ] Procurar por `[PERFORMANCE]` - deve mostrar tempo de cada coleta
- [ ] Procurar por `[AVISO]` - não deve aparecer
- [ ] Procurar por `[ERRO]` - não deve aparecer

---

## Fase 5: Deploy (FINAL)

### Preparar Build
- [ ] Compilar: `mvn clean package -DskipTests`
- [ ] Verificar se build foi bem-sucedida
- [ ] Criar backup do banco de dados

### Deploy em Produção
- [ ] Parar servidor atual
- [ ] Fazer backup do JAR anterior
- [ ] Copiar novo JAR
- [ ] Iniciar servidor
- [ ] Verificar se servidor iniciou corretamente

### Monitorar em Produção
- [ ] Verificar logs por 1 hora
- [ ] Testar coleta com dados reais
- [ ] Verificar se performance melhorou
- [ ] Verificar se não há erros

---

## Documentação

### Arquivos Criados
- [x] `ANALISE_LENTIDAO_COLETA.md` - Análise técnica completa
- [x] `IMPLEMENTACAO_OTIMIZACAO_COLETA.md` - Guia passo-a-passo
- [x] `sql/criar_indices_performance_coleta.sql` - Script SQL
- [x] `RESUMO_INVESTIGACAO_LENTIDAO.md` - Resumo executivo
- [x] `CHECKLIST_IMPLEMENTACAO_OTIMIZACAO.md` - Este arquivo

### Arquivos Modificados
- [x] `src/main/java/com/inventario/dao/ParticipanteInventarioDAO.java` - Remover query de debug

---

## Métricas de Sucesso

### Performance
- [ ] Coleta 1: < 300ms (antes: 500ms)
- [ ] Coleta 5: < 400ms (antes: 1.5s)
- [ ] Coleta 10: < 500ms (antes: 3s)
- [ ] Coleta 20: < 600ms (antes: 8s)
- [ ] Coleta 50: < 3s (antes: 25s)

### Queries
- [ ] Queries por coleta: 3 (antes: 7)
- [ ] Redução: 57% (antes: 100%)

### Funcionalidade
- [ ] Taxa de erro: 0%
- [ ] Duplicação: 0%
- [ ] Sincronização: 100%

---

## Notas Importantes

### Cache
- Cache é invalidado a cada 5 minutos
- Cache é invalidado quando usuário faz logout
- Cache é invalidado quando muda de inventário
- Cache é invalidado quando atualiza participantes

### Índices
- Índices devem ser reindexados mensalmente
- Índices melhoram performance de queries
- Índices aumentam tamanho do banco em ~5%

### Performance
- Performance melhora progressivamente com cache
- Performance melhora com índices
- Performance pode variar com carga do servidor

---

## Troubleshooting

### Problema: Cache não está funcionando
**Solução**: Verificar logs por `[CACHE]`, verificar se `obterIdParticipante()` está sendo chamado

### Problema: Índices não foram criados
**Solução**: Executar script SQL novamente, verificar se há erros no PostgreSQL

### Problema: Performance não melhorou
**Solução**: Verificar se cache está sendo usado, verificar se índices foram criados, verificar logs

### Problema: Coleta não funciona
**Solução**: Verificar se `obterInventarioAtivo()` retorna null, verificar se `obterIdParticipante()` retorna null

---

## Contato

Para dúvidas ou problemas:
1. Consultar `ANALISE_LENTIDAO_COLETA.md`
2. Consultar `IMPLEMENTACAO_OTIMIZACAO_COLETA.md`
3. Verificar logs em `logs/sistema-inventario.log`

---

**Checklist Criado**: 26/11/2025  
**Versão**: 1.0.0  
**Status**: Pronto para Implementação
