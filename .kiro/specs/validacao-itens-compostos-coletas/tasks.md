# Validação de Itens Compostos vs. Coletas Normais - Tasks

## 1. Detecção e Relatório

- [ ] 1.1 Criar query SQL para identificar itens compostos coletados normalmente
- [ ] 1.2 Implementar método `gerarRelatorioItensCompostosColetadosNormalmente()` no `RelatorioColetaDAO`
- [ ] 1.3 Criar endpoint REST `GET /api/mobile/coletas/compostos-coletados-normalmente`
- [ ] 1.4 Criar endpoint REST `GET /api/mobile/coletas/compostos-pendentes`
- [ ] 1.5 Criar DTO `ColetaCompostaPendente`
- [ ] 1.6 Testar relatórios com dados reais (106 itens)

## 2. Prevenção de Coletas Incorretas

- [ ] 2.1 Adicionar validação no `ColetaService.registrarColeta()`
- [ ] 2.2 Adicionar validação no `MobileColetaController.registrarColeta()`
- [ ] 2.3 Adicionar validação no `ColetaFrame.btnColetarActionPerformed()`
- [ ] 2.4 Criar mensagem de erro clara para itens compostos
- [ ] 2.5 Testar que coleta normal de itens compostos é bloqueada

## 3. Correção de Dados

- [ ] 3.1 Criar entity `Correcao.java`
- [ ] 3.2 Criar DAO `CorrecaoDAO.java`
- [ ] 3.3 Criar service `CorrecaoColetaService.java`
- [ ] 3.4 Implementar método `converterParaColetaComposta()`
- [ ] 3.5 Criar endpoint REST `POST /api/mobile/coletas/corrigir-composta`
- [ ] 3.6 Criar DTO `CorrecaoResultado`
- [ ] 3.7 Criar interface de revisão e confirmação
- [ ] 3.8 Testar correção com dados reais (106 itens)

## 4. Alertas e Dashboard

- [ ] 4.1 Criar endpoint `GET /api/mobile/coletas/compostos-pendentes`
- [ ] 4.2 Adicionar alertas visuais no `ColetaFrame`
- [ ] 4.3 Criar dashboard com estatísticas de itens compostos
- [ ] 4.4 Implementar notificações para itens pendentes
- [ ] 4.5 Testar alertas em tempo real

## 5. Validação e Testes

- [ ] 5.1 Criar testes unitários para validação
- [ ] 5.2 Criar testes de integração para correção
- [ ] 5.3 Testar fluxo completo: coleta normal → erro → coleta composta
- [ ] 5.4 Validar integridade dos dados após correção
- [ ] 5.5 Documentar mudanças

## 6. Documentação

- [ ] 6.1 Atualizar manual do usuário com nova validação
- [ ] 6.2 Documentar fluxo de coleta composta
- [ ] 6.3 Criar guia de correção de dados
- [ ] 6.4 Atualizar changelog

---

**Total de Tasks:** 26  
**Estimativa:** 9 dias úteis  
**Prioridade:** Alta (Garantir integridade dos dados)
