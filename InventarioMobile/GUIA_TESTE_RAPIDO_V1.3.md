# Guia de Teste Rápido - Versão 1.3.0

## 🎯 Objetivo

Validar rapidamente todas as novas funcionalidades implementadas na tela de inventário.

---

## ⏱️ Tempo Estimado

- **Teste Completo**: 15-20 minutos
- **Teste Rápido**: 5-10 minutos

---

## 📋 Pré-requisitos

- [ ] App instalado no dispositivo
- [ ] Usuário logado
- [ ] Conexão com servidor ativa
- [ ] Dados de patrimônios carregados

---

## 🧪 Testes Funcionais

### 1. Busca em Tempo Real (3 min)

#### Teste Básico
1. Abrir tela de Inventário
2. Tocar no ícone 🔍 no menu superior
3. Digitar "dell"
4. **Verificar**: Lista filtra automaticamente
5. **Verificar**: Contador mostra "X de Y patrimônios"

#### Teste de Busca por Diferentes Campos
```
Buscar por:
✓ Número: "123456"
✓ Descrição: "computador"
✓ Marca: "dell"
✓ Modelo: "optiplex"
✓ Setor: "TI"
✓ Sala: "101"
```

#### Teste de Limpeza
1. Apagar texto da busca
2. **Verificar**: Lista volta ao normal
3. Fechar SearchView
4. **Verificar**: Lista completa exibida

#### ✅ Critérios de Sucesso
- [ ] Busca filtra instantaneamente
- [ ] Todos os campos são pesquisados
- [ ] Contador atualiza corretamente
- [ ] Limpar busca restaura lista

---

### 2. Sistema de Ordenação (3 min)

#### Teste de Todas as Opções
1. Tocar no ícone 📊 (ordenação)
2. Selecionar "Número (Crescente)"
3. **Verificar**: Lista ordenada por número crescente
4. Repetir para cada opção:
   - [ ] Número (Decrescente)
   - [ ] Descrição (A-Z)
   - [ ] Descrição (Z-A)
   - [ ] Setor (A-Z)
   - [ ] Setor (Z-A)

#### Teste de Persistência
1. Ordenar por "Descrição (A-Z)"
2. Fazer uma busca
3. **Verificar**: Ordenação mantida nos resultados
4. Limpar busca
5. **Verificar**: Ordenação ainda aplicada

#### ✅ Critérios de Sucesso
- [ ] Todas as ordenações funcionam
- [ ] Ordenação é imediata
- [ ] Ordenação persiste durante busca
- [ ] Diálogo mostra opção selecionada

---

### 3. Visualização de Detalhes (3 min)

#### Teste de Exibição
1. Tocar em qualquer item da lista
2. **Verificar informações exibidas**:
   - [ ] Número do patrimônio
   - [ ] Descrição completa
   - [ ] Marca
   - [ ] Modelo
   - [ ] Número de série
   - [ ] Estado
   - [ ] Valor
   - [ ] Setor
   - [ ] Sala
   - [ ] Responsável
   - [ ] Status de coleta
   - [ ] Data de coleta (se coletado)
   - [ ] Observações

#### Teste de Ações
1. No diálogo de detalhes, tocar em "Escanear QR"
2. **Verificar**: Abre tela de scanner
3. Voltar e tocar em "Fechar"
4. **Verificar**: Diálogo fecha

#### Teste com Diferentes Itens
1. Testar com item coletado
2. **Verificar**: Status "Coletado" e data exibidos
3. Testar com item pendente
4. **Verificar**: Status "Pendente"

#### ✅ Critérios de Sucesso
- [ ] Todas as informações são exibidas
- [ ] Formatação está correta
- [ ] Botões funcionam
- [ ] Diálogo fecha corretamente

---

### 4. Menu de Ações Rápidas (2 min)

#### Teste de Todas as Ações
1. Tocar no FAB ⚡ (botão flutuante)
2. **Verificar**: Menu com 3 opções aparece
3. Selecionar "Escanear QR Code"
4. **Verificar**: Abre scanner
5. Voltar e tocar no FAB novamente
6. Selecionar "Atualizar Lista"
7. **Verificar**: Lista recarrega (spinner aparece)
8. Tocar no FAB novamente
9. Selecionar "Exportar Dados"
10. **Verificar**: Mensagem "Em desenvolvimento"

#### ✅ Critérios de Sucesso
- [ ] FAB abre menu
- [ ] Todas as opções aparecem
- [ ] Escanear QR funciona
- [ ] Atualizar lista funciona
- [ ] Exportar mostra mensagem

---

### 5. Layout dos Itens (2 min)

#### Teste Visual
1. Rolar a lista de patrimônios
2. **Verificar em cada card**:
   - [ ] Número do patrimônio (azul, negrito)
   - [ ] Status (verde "Coletado" ou laranja "Pendente")
   - [ ] Descrição (texto normal)
   - [ ] Marca (texto secundário)
   - [ ] Modelo (texto secundário)
   - [ ] Ícone de setor 🏢
   - [ ] Nome do setor
   - [ ] Ícone de sala 🚪
   - [ ] Nome da sala

#### Teste de Interação
1. Tocar em um card
2. **Verificar**: Efeito visual (ripple)
3. **Verificar**: Abre detalhes

#### ✅ Critérios de Sucesso
- [ ] Todos os campos visíveis
- [ ] Ícones aparecem
- [ ] Cores corretas
- [ ] Layout organizado

---

### 6. Contador Inteligente (2 min)

#### Teste em Diferentes Contextos
1. **Sem busca**: 
   - **Verificar**: "Total: X patrimônios"
2. **Com busca**:
   - Buscar algo
   - **Verificar**: "Encontrados: X de Y patrimônios"
3. **Com paginação** (se aplicável):
   - **Verificar**: "Total: X patrimônios (Y carregados)"

#### ✅ Critérios de Sucesso
- [ ] Contador sempre visível
- [ ] Texto contextual correto
- [ ] Números corretos

---

## 🔄 Testes de Integração

### Teste 1: Busca + Ordenação
1. Ordenar por "Descrição (A-Z)"
2. Buscar "computador"
3. **Verificar**: Resultados ordenados alfabeticamente
4. Limpar busca
5. **Verificar**: Ordenação mantida

### Teste 2: Filtro + Busca
1. Aplicar filtro por responsável
2. Buscar dentro dos resultados
3. **Verificar**: Busca funciona nos itens filtrados

### Teste 3: Pull to Refresh + Busca
1. Fazer uma busca
2. Arrastar lista para baixo (pull to refresh)
3. **Verificar**: Lista recarrega
4. **Verificar**: Busca é limpa

---

## 📱 Testes de Usabilidade

### Teste de Fluidez
1. Rolar lista rapidamente
2. **Verificar**: Sem travamentos
3. Buscar e ordenar várias vezes
4. **Verificar**: Resposta imediata

### Teste de Navegação
1. Abrir detalhes
2. Voltar
3. Fazer busca
4. Abrir detalhes de resultado
5. Voltar
6. **Verificar**: Navegação fluida

### Teste de Orientação
1. Girar dispositivo (portrait/landscape)
2. **Verificar**: Layout se adapta
3. **Verificar**: Estado mantido

---

## 🐛 Testes de Edge Cases

### Lista Vazia
1. Aplicar filtro que não retorna resultados
2. **Verificar**: Mensagem "Nenhum patrimônio encontrado"
3. **Verificar**: Ícone de lista vazia

### Busca Sem Resultados
1. Buscar "xyzabc123" (termo inexistente)
2. **Verificar**: "Encontrados: 0 de X patrimônios"
3. **Verificar**: Mensagem de lista vazia

### Dados Incompletos
1. Verificar item sem marca/modelo
2. **Verificar**: Exibe "N/A" ou campo vazio
3. Abrir detalhes
4. **Verificar**: Campos ausentes não quebram layout

### Conexão Perdida
1. Desativar Wi-Fi/dados
2. Tentar atualizar lista
3. **Verificar**: Mensagem de erro apropriada
4. **Verificar**: Dados em cache ainda acessíveis

---

## ⚡ Teste Rápido (5 min)

Para validação rápida, execute apenas:

1. **Busca** (1 min)
   - Buscar "dell"
   - Verificar filtro funciona

2. **Ordenação** (1 min)
   - Ordenar por "Descrição (A-Z)"
   - Verificar ordem correta

3. **Detalhes** (1 min)
   - Tocar em item
   - Verificar informações

4. **Ações Rápidas** (1 min)
   - Tocar FAB
   - Testar "Atualizar Lista"

5. **Visual** (1 min)
   - Verificar marca/modelo aparecem
   - Verificar ícones de setor/sala

---

## 📊 Checklist Final

### Funcionalidades
- [ ] Busca em tempo real funciona
- [ ] Ordenação funciona (6 opções)
- [ ] Detalhes completos exibidos
- [ ] Menu de ações funciona
- [ ] Layout melhorado visível

### Performance
- [ ] Busca é instantânea
- [ ] Ordenação é imediata
- [ ] Scroll é fluido
- [ ] Sem travamentos

### UI/UX
- [ ] Ícones corretos
- [ ] Cores apropriadas
- [ ] Textos legíveis
- [ ] Feedback visual presente

### Estabilidade
- [ ] Sem crashes
- [ ] Sem erros no logcat
- [ ] Navegação estável
- [ ] Rotação funciona

---

## 🐛 Reportar Problemas

Se encontrar problemas, anote:

```
Problema: [Descrição]
Passos para reproduzir:
1. 
2. 
3. 
Resultado esperado: 
Resultado obtido: 
Dispositivo: 
Versão Android: 
Logs (se disponível):
```

---

## ✅ Aprovação

### Critérios de Aprovação
- [ ] Todos os testes funcionais passaram
- [ ] Nenhum bug crítico encontrado
- [ ] Performance aceitável
- [ ] UI/UX satisfatória

### Assinaturas
```
Testador: _________________ Data: ___/___/___
Aprovador: ________________ Data: ___/___/___
```

---

## 📝 Notas Adicionais

### Observações do Teste
```
[Espaço para anotações]
```

### Melhorias Sugeridas
```
[Espaço para sugestões]
```

---

**Versão Testada**: 1.3.0  
**Data do Documento**: 03/11/2025  
**Autor**: Sistema SIHCP
