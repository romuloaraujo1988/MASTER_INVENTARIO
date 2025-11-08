# ColetaFrame_v2 - Guia Completo do Sistema

## 📋 Visão Geral

O **ColetaFrame_v2** é a interface principal para coleta de patrimônios no sistema SIHCP. Possui duas abas principais:
1. **Coleta Normal** - Para itens com etiqueta patrimonial
2. **Itens Sem Patrimônio** - Para itens sem etiqueta

---

## 🎯 Funcionalidades Principais

### 1️⃣ Aba: Coleta Normal

#### Seleção de Sala
- **Combo pesquisável** de salas
- Filtro em tempo real (300ms de delay)
- Exibe identificação completa da sala
- Atualiza automaticamente o campo de localização na aba de itens sem patrimônio

#### Busca de Patrimônio
- Busca por número ou código de barras
- Detecção automática de leitores de código de barras
- Exibe informações completas do item:
  - Número patrimonial
  - Descrição
  - Localização original
  - Estado original
  - Status atual

#### Registro de Coleta
- Seleção de estado atual (BOM, OCIOSO, ANTIECONÔMICO, RECUPERÁVEL, IRRECUPERÁVEL)
- Campo de observações
- Detecção automática de divergências de localização
- Validação de duplicatas
- Atalho: **F4** para registrar

#### Histórico de Coleta
- Tabela sempre visível com itens coletados
- Colunas: Data/Hora, Patrimônio, Descrição, Estado
- Cores alternadas para melhor leitura
- Seleção única

#### Ações Administrativas
- **Remover Item** - Disponível para todos os usuários logados
- **Excluir Coleta** - Apenas para ADMIN
- **Finalizar Coleta da Sala** - Marca sala como concluída

---

### 2️⃣ Aba: Itens Sem Patrimônio

#### Layout Otimizado
```
┌─────────────────────────────────────────────────────┐
│ 🔍 Pesquisar Descrições Existentes                  │
│ [Campo de busca] [Botão Pesquisar]                  │
│ 💡 Instruções                                        │
├──────────────────────┬──────────────────────────────┤
│ 📋 Descrições        │ ✏️ Registrar Item            │
│ Encontradas          │                              │
│                      │ Localização: [Auto]          │
│ [Tabela de          │ Descrição: [____]            │
│  Resultados]         │ Categoria: [____]            │
│                      │ Observações: [____]          │
│                      │                              │
│                      │ [Registrar] [Limpar]         │
├──────────────────────┴──────────────────────────────┤
│ 📊 Itens Sem Patrimônio Registrados                 │
│ [Tabela de itens registrados]                       │
└─────────────────────────────────────────────────────┘
```

#### Pesquisa de Descrições
- **Campo de busca** com suporte a Enter
- Busca por palavras-chave
- Resultados em tabela com:
  - Descrição do item
  - Localização (onde está cadastrado)
- Clique para selecionar e preencher formulário

#### Formulário de Registro
- **Localização**: Preenchida automaticamente pela sala selecionada (somente leitura)
- **Descrição**: Campo editável ou preenchido pela pesquisa
- **Categoria**: Seleção manual (MÓVEIS, ELETRÔNICOS, EQUIPAMENTOS, LIVROS, MATERIAIS, OUTROS)
- **Observações**: Campo opcional para notas adicionais

#### Tabela de Itens Registrados
- Exibe todos os itens sem patrimônio coletados
- Colunas: Data/Hora, Descrição, Categoria, Localização
- Botões:
  - **Remover** - Apenas ADMIN e COORDENADOR
  - **Atualizar** - Recarrega a lista

---

## 🔐 Controle de Permissões

### Perfis de Usuário

| Ação | COLETOR | COORDENADOR | ADMIN |
|------|---------|-------------|-------|
| Registrar itens | ✅ | ✅ | ✅ |
| Remover item (próprio) | ✅ | ✅ | ✅ |
| Remover item sem patrimônio | ❌ | ✅ | ✅ |
| Excluir coleta | ❌ | ❌ | ✅ |
| Finalizar coleta da sala | ✅ | ✅ | ✅ |

### Validações de Acesso
- Verifica se usuário está logado
- Verifica se usuário é participante do inventário ativo
- Controla visibilidade de botões por perfil
- Exibe mensagens claras de acesso negado

---

## 🎨 Design e Usabilidade

### Cores do Tema
- **Primária**: #34495E (Azul escuro elegante)
- **Secundária**: #ECF0F1 (Cinza claro)
- **Acento**: #2ECC71 (Verde moderno)
- **Perigo**: #E74C3C (Vermelho moderno)
- **Aviso**: #F1C40F (Amarelo moderno)
- **Texto**: #2C3E50 (Texto escuro)

### Fontes
- **Família**: Segoe UI
- **Tamanhos**:
  - Títulos: 13-14pt Bold
  - Labels: 11-12pt Bold
  - Campos: 11-12pt Regular
  - Tabelas: 11-12pt Regular

### Espaçamentos Otimizados
- Bordas dos painéis: 10px
- Espaçamento entre componentes: 5-8px
- Altura dos campos: 30px
- Altura das linhas de tabela: 28-30px

---

## 🔄 Fluxo de Trabalho

### Coleta Normal
1. Selecionar sala no combo
2. Buscar patrimônio (digitar ou usar leitor)
3. Verificar informações exibidas
4. Ajustar estado se necessário
5. Adicionar observações (opcional)
6. Pressionar F4 ou clicar em "Registrar Item"
7. Item aparece no histórico
8. Campo de busca é limpo automaticamente
9. Repetir para próximo item

### Itens Sem Patrimônio
1. Selecionar sala na aba "Coleta Normal"
2. Ir para aba "Itens Sem Patrimônio"
3. Verificar se localização está preenchida
4. **Opção A - Pesquisar descrição existente**:
   - Digitar palavras-chave
   - Pressionar Enter ou clicar em Pesquisar
   - Clicar na descrição desejada
   - Descrição é preenchida automaticamente
5. **Opção B - Digitar nova descrição**:
   - Digitar diretamente no campo
6. Selecionar categoria
7. Adicionar observações (opcional)
8. Clicar em "Registrar"
9. Item aparece na tabela inferior
10. Formulário é limpo automaticamente
11. Repetir para próximo item

---

## 📊 Estatísticas e Relatórios

### Atualização Automática
- Total de itens coletados por sala
- Quantidade de itens sem etiqueta
- Status de finalização da sala
- Histórico completo de coletas

### Informações Exibidas
- **Resumo da Sala**: Nome e status (FINALIZADA ou em andamento)
- **Inventário Atual**: Nome do inventário ativo
- **Histórico**: Todos os itens coletados na sala atual

---

## ⚙️ Configurações Técnicas

### Detecção de Código de Barras
- Timeout: 200ms entre caracteres
- Comprimento válido: 4-6 dígitos
- Busca automática após 300ms

### Filtro de Salas
- Delay: 300ms após última digitação
- Busca case-insensitive
- Mantém seleção atual

### Validações
- Sala obrigatória para registro
- Patrimônio obrigatório (coleta normal)
- Descrição obrigatória (itens sem patrimônio)
- Verificação de duplicatas
- Verificação de permissões

---

## 🐛 Solução de Problemas

### Problema: Localização não aparece na aba de itens sem patrimônio
**Solução**: Selecione uma sala na aba "Coleta Normal" primeiro

### Problema: Botão "Registrar" desabilitado
**Causas possíveis**:
- Sala não selecionada
- Usuário não é participante do inventário
- Campos obrigatórios não preenchidos

### Problema: Não consigo remover item
**Causas possíveis**:
- Perfil COLETOR tentando remover item sem patrimônio (apenas ADMIN/COORDENADOR)
- Item não selecionado na tabela
- Usuário não logado

### Problema: Leitor de código de barras não funciona
**Verificações**:
- Leitor configurado para enviar Enter após leitura
- Código tem 4-6 dígitos
- Campo de busca está focado

---

## 📝 Notas Importantes

1. **Instância Única**: Apenas uma instância do sistema pode rodar por vez
2. **Sincronização**: A localização é compartilhada entre as abas
3. **Histórico**: Sempre visível para referência rápida
4. **Atalhos**: F4 para registrar item (aba Coleta Normal)
5. **Enter**: Funciona em campos de busca
6. **Autofoco**: Campo de busca recebe foco após registro

---

## 🔄 Atualizações Recentes

### Versão 2.1 (2025-01-19)
- ✅ Implementado mecanismo de instância única
- ✅ Localização sincronizada entre abas
- ✅ Layout otimizado na aba de itens sem patrimônio
- ✅ Tabela de resultados com coluna "Localização"
- ✅ Espaçamentos reduzidos para melhor visibilidade
- ✅ Fontes e tamanhos ajustados
- ✅ JSplitPane otimizado (55% para resultados)

### Melhorias de Performance
- Filtro de salas com timer (300ms)
- Detecção de código de barras otimizada
- Atualização de estatísticas em background
- Validações assíncronas

---

## 📞 Suporte

Para dúvidas ou problemas:
1. Verifique este guia primeiro
2. Consulte a seção "Solução de Problemas"
3. Entre em contato com o administrador do sistema
4. Reporte bugs com detalhes da ação realizada

---

**Última atualização**: 2025-01-19  
**Versão**: 2.1  
**Autor**: Sistema SIHCP - IFMT
