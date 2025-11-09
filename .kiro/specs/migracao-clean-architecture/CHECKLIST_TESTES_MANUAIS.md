# Checklist de Testes Manuais - Fase 1 (Descrição)

## 🎯 Objetivo
Validar que a feature de Descrição funciona corretamente após migração para Clean Architecture.

---

## ⚙️ Preparação

- [ ] App compilado sem erros
- [ ] Gradle Sync concluído com sucesso
- [ ] Feature flag "Clean Architecture" habilitada
- [ ] Feature flag "Descrição" habilitada
- [ ] App reiniciado após habilitar flags

**Como habilitar:**
1. Abrir app
2. Ir em Configurações → Configurações de Desenvolvedor
3. Ligar "Clean Architecture (Master)"
4. Ligar "Descrição (Piloto)"
5. Reiniciar app

---

## 📱 Testes Funcionais

### Teste 1: Carregamento de Descrições (Online)

**Pré-condição:** Dispositivo com internet

- [ ] Abrir tela de seleção de descrição
- [ ] Verificar que loading aparece
- [ ] Verificar que descrições são carregadas
- [ ] Verificar que APENAS descrições não coletadas aparecem
- [ ] Verificar que lista não está vazia (se houver itens pendentes)

**Resultado Esperado:** ✅ Descrições não coletadas carregam corretamente

**Log esperado:**
```
DescricaoSelectionActivity: ✅ Usando Clean Architecture
DescricaoSelectionViewModelClean: Carregando descrições...
```

---

### Teste 2: Carregamento de Descrições (Offline)

**Pré-condição:** Dispositivo SEM internet (modo avião)

- [ ] Ativar modo avião
- [ ] Abrir tela de seleção de descrição
- [ ] Verificar que loading aparece
- [ ] Verificar que descrições do banco local são carregadas
- [ ] Verificar que não há erro de conexão

**Resultado Esperado:** ✅ App funciona offline, busca do banco local

---

### Teste 3: Seleção de Descrição

**Pré-condição:** Lista de descrições carregada

- [ ] Tocar em uma descrição da lista
- [ ] Verificar que navega para tela de coleta manual
- [ ] Verificar que descrição está pré-preenchida
- [ ] Verificar que sala está correta

**Resultado Esperado:** ✅ Navegação funciona, dados passados corretamente

---

### Teste 4: Busca de Descrições

**Pré-condição:** Lista de descrições carregada

- [ ] Digitar termo na busca
- [ ] Verificar que lista é filtrada
- [ ] Limpar busca
- [ ] Verificar que lista volta ao normal

**Resultado Esperado:** ✅ Busca funciona corretamente

---

### Teste 5: Lista Vazia

**Pré-condição:** Todas descrições já coletadas

- [ ] Abrir tela de seleção de descrição
- [ ] Verificar que mensagem "Nenhuma descrição encontrada" aparece
- [ ] Verificar que não há erro

**Resultado Esperado:** ✅ Mensagem apropriada quando não há itens

---

## 🔄 Testes de Rollback

### Teste 6: Rollback para Legacy

**Objetivo:** Verificar que rollback funciona

- [ ] Ir em Configurações de Desenvolvedor
- [ ] Desligar "Descrição (Piloto)"
- [ ] Reiniciar app
- [ ] Abrir tela de seleção de descrição
- [ ] Verificar que funciona com código antigo

**Resultado Esperado:** ✅ App volta para código antigo sem problemas

**Log esperado:**
```
DescricaoSelectionActivity: ⚠️ Usando Legacy Architecture
```

---

### Teste 7: Rollback Completo

**Objetivo:** Testar rollback de emergência

- [ ] Ir em Configurações de Desenvolvedor
- [ ] Clicar em "🔄 Rollback Completo"
- [ ] Confirmar ação
- [ ] Reiniciar app
- [ ] Verificar que todas features voltaram para legacy

**Resultado Esperado:** ✅ Rollback completo funciona

---

## ⚡ Testes de Performance

### Teste 8: Tempo de Carregamento

**Objetivo:** Verificar que performance não degradou

- [ ] Medir tempo de carregamento com Clean Architecture
- [ ] Medir tempo de carregamento com Legacy
- [ ] Comparar tempos

**Critério de Aceitação:** Diferença < 20%

**Tempo Clean:** _______ ms
**Tempo Legacy:** _______ ms
**Diferença:** _______ %

---

### Teste 9: Uso de Memória

**Objetivo:** Verificar que não há memory leak

- [ ] Abrir tela de descrição
- [ ] Fechar tela
- [ ] Repetir 10 vezes
- [ ] Verificar uso de memória no Android Studio Profiler

**Resultado Esperado:** ✅ Memória estável, sem leaks

---

## 🐛 Testes de Erro

### Teste 10: Erro de Rede

**Objetivo:** Verificar tratamento de erro

- [ ] Desconectar internet durante carregamento
- [ ] Verificar que erro é tratado graciosamente
- [ ] Verificar que fallback para banco local funciona

**Resultado Esperado:** ✅ Erro tratado, fallback funciona

---

### Teste 11: Servidor Indisponível

**Objetivo:** Verificar comportamento quando servidor está offline

- [ ] Configurar servidor inválido (ou desligar servidor)
- [ ] Tentar carregar descrições
- [ ] Verificar que app não crasha
- [ ] Verificar que usa dados locais

**Resultado Esperado:** ✅ App continua funcionando com dados locais

---

## 📊 Resumo dos Testes

**Total de Testes:** 11

**Resultados:**
- ✅ Passou: _____ / 11
- ❌ Falhou: _____ / 11
- ⚠️ Com ressalvas: _____ / 11

**Status Geral:** 
- [ ] ✅ Todos os testes passaram - Pode prosseguir
- [ ] ⚠️ Alguns testes falharam - Investigar
- [ ] ❌ Muitos testes falharam - Rollback necessário

---

## 📝 Observações

**Problemas Encontrados:**
```
(Descrever problemas aqui)
```

**Melhorias Sugeridas:**
```
(Descrever melhorias aqui)
```

**Lições Aprendidas:**
```
(Documentar aprendizados aqui)
```

---

## ✅ Aprovação

**Testado por:** _________________
**Data:** _________________
**Aprovado:** [ ] Sim [ ] Não

**Assinatura:** _________________

---

## 🚀 Próximos Passos

Se todos os testes passaram:
- [ ] Documentar resultados
- [ ] Atualizar status no tasks.md
- [ ] Prosseguir para Fase 2 (Coleta)

Se houver problemas:
- [ ] Documentar problemas
- [ ] Criar issues para correção
- [ ] Decidir: corrigir ou fazer rollback
