# 🚀 Guia Rápido - Filtro "Itens Não Encontrados"

## ⚡ Início Rápido (30 segundos)

### Passo 1: Abrir Relatórios
```
Menu Principal → Relatórios
```

### Passo 2: Configurar Filtro
1. **Tipo de Relatório:** Selecione "Itens Não Encontrados"
2. **Inventário:** Selecione o inventário ativo
3. Clique em **"📊 Gerar Relatório"**

### Passo 3: Visualizar Resultado
- ✅ Tabela preenchida automaticamente
- ✅ Resumo estatístico disponível na aba "Resumo"
- ✅ Gráficos na aba "Gráficos"

---

## 📊 O Que Este Filtro Faz?

### Objetivo
Identifica **todos os patrimônios ativos** que **NÃO foram coletados** no inventário selecionado.

### Informações Exibidas
- Número do patrimônio
- Descrição completa
- Responsável atual
- Setor
- Sala/Localização
- Valor de aquisição

### Casos de Uso
1. **Planejamento de coleta:** Saber quais itens ainda faltam coletar
2. **Auditoria:** Identificar patrimônios não localizados
3. **Relatórios gerenciais:** Percentual de conclusão do inventário
4. **Priorização:** Focar em setores/responsáveis com mais pendências

---

## 🎯 Exemplos Práticos

### Exemplo 1: Inventário em Andamento
**Situação:** Inventário 2024 com 500 patrimônios, 450 coletados

**Resultado:**
```
╔════════════════════════════════════════════════════════╗
║  ITENS NÃO ENCONTRADOS: 50 itens (10%)                 ║
╠════════════════════════════════════════════════════════╣
║  Setor TI: 20 itens                                    ║
║  Setor Administrativo: 15 itens                        ║
║  Setor Acadêmico: 15 itens                             ║
╚════════════════════════════════════════════════════════╝
```

**Ação:** Priorizar coleta no Setor TI

### Exemplo 2: Inventário Concluído
**Situação:** Todos os patrimônios foram coletados

**Resultado:**
```
╔════════════════════════════════════════════════════════╗
║  ITENS NÃO ENCONTRADOS: 0 itens (0%)                   ║
║  ✅ Inventário 100% concluído!                         ║
╚════════════════════════════════════════════════════════╝
```

**Ação:** Gerar relatório final e encerrar inventário

### Exemplo 3: Análise por Responsável
**Situação:** Verificar pendências de um responsável específico

**Passos:**
1. Gerar relatório de itens não encontrados
2. Usar busca em tempo real (campo de busca na tabela)
3. Digitar nome do responsável
4. Visualizar apenas itens daquele responsável

---

## 📤 Exportação de Dados

### Opção 1: Excel Atual
```
1. Gerar relatório
2. Clicar "📊 Excel Atual"
3. Salvar arquivo
4. Abrir no Excel
```

**Resultado:** Arquivo `.xlsx` com todos os dados da tabela

### Opção 2: PDF
```
1. Gerar relatório
2. Clicar "📄 PDF"
3. Salvar arquivo
4. Compartilhar com equipe
```

**Resultado:** Relatório formatado em PDF

### Opção 3: Impressão
```
1. Gerar relatório
2. Clicar "🖨️ Imprimir"
3. Selecionar impressora
4. Confirmar impressão
```

---

## 🔍 Filtros Avançados

### Ativar Filtros Avançados
```
1. Marcar checkbox "Ativar filtros avançados"
2. Configurar filtros desejados
3. Gerar relatório novamente
```

### Filtros Disponíveis

#### 1. Valor Mínimo/Máximo
```
Exemplo: Mostrar apenas itens acima de R$ 1.000,00
- Valor Mínimo: 1000.00
- Valor Máximo: 999999.00
```

#### 2. Sala Específica
```
Exemplo: Apenas itens da Sala 101
- Sala: 101 - Laboratório de Informática
```

#### 3. Estado de Conservação
```
Exemplo: Apenas itens em bom estado
- Estado: BOM
```

#### 4. Excluir Sem Valor
```
Marcar: "Excluir itens sem valor"
Resultado: Remove itens com valor R$ 0,00
```

---

## 📊 Interpretando o Resumo Estatístico

### Aba "Resumo"

```
📊 RESUMO EXECUTIVO - INVENTÁRIO DE PATRIMÔNIO
==============================================

🟡 STATUS GERAL: BOM (90.0% coletado)

📈 INDICADORES PRINCIPAIS
-------------------------
• Total de Patrimônios: 500 itens
• Taxa de Coleta: 90.0% (450 itens)
• Itens Não Localizados: 10.0% (50 itens)
• Valor Total Não Localizado: R$ 125.000,00

⚠️ ANÁLISE DE RISCOS
--------------------
🟡 MÉDIO: Taxa de itens não encontrados entre 5-10%

💡 RECOMENDAÇÕES
----------------
• Intensificar esforços de localização
• Verificar setores com mais pendências
• Contatar responsáveis para confirmação
```

### Interpretação dos Indicadores

| Percentual | Status | Ação Recomendada |
|------------|--------|------------------|
| 0-5% | 🟢 Excelente | Finalizar inventário |
| 5-10% | 🟡 Bom | Busca direcionada |
| 10-20% | 🟠 Atenção | Intensificar buscas |
| >20% | 🔴 Crítico | Revisar processo |

---

## 🎨 Recursos Visuais

### Cores na Tabela
- **Vermelho claro:** Itens não encontrados (destaque)
- **Branco/Cinza:** Linhas alternadas (legibilidade)

### Gráficos Disponíveis
1. **Pizza:** Distribuição por status
2. **Barras:** Itens por setor
3. **Barras:** Itens por responsável

### Busca em Tempo Real
```
Campo de busca acima da tabela:
- Digite qualquer termo
- Filtro aplicado instantaneamente
- Contador de resultados atualizado
```

---

## ⚠️ Problemas Comuns

### Problema 1: Relatório Vazio
**Sintoma:** Tabela sem dados após gerar relatório

**Causas Possíveis:**
- ✅ Todos os patrimônios foram coletados (sucesso!)
- ❌ Inventário não selecionado
- ❌ Inventário sem patrimônios cadastrados

**Solução:**
1. Verificar se inventário está selecionado
2. Verificar se há patrimônios ativos no sistema
3. Verificar logs do sistema

### Problema 2: Erro ao Gerar
**Sintoma:** Mensagem de erro ao clicar "Gerar Relatório"

**Solução:**
1. Verificar conexão com banco de dados
2. Verificar se inventário existe
3. Consultar logs: `logs/sistema-inventario.log`

### Problema 3: Exportação Falha
**Sintoma:** Erro ao exportar para Excel/PDF

**Solução:**
1. Verificar espaço em disco
2. Verificar permissões de escrita
3. Fechar arquivo se já estiver aberto
4. Usar formato alternativo (CSV)

---

## 💡 Dicas e Truques

### Dica 1: Busca Rápida
```
Use o campo de busca para filtrar rapidamente:
- Digite "TI" → Mostra apenas itens do setor TI
- Digite "João" → Mostra apenas itens do João
- Digite "101" → Mostra apenas itens da sala 101
```

### Dica 2: Ordenação
```
Clique nos cabeçalhos das colunas para ordenar:
- 1 clique: Ordem crescente
- 2 cliques: Ordem decrescente
- 3 cliques: Ordem original
```

### Dica 3: Múltiplas Exportações
```
Você pode exportar o mesmo relatório em vários formatos:
1. Excel para análise
2. PDF para apresentação
3. Impressão para arquivo físico
```

### Dica 4: Comparação de Inventários
```
Para comparar dois inventários:
1. Gerar relatório do Inventário A
2. Exportar para Excel
3. Gerar relatório do Inventário B
4. Exportar para Excel
5. Comparar arquivos no Excel
```

---

## 📞 Suporte

### Logs do Sistema
```bash
# Ver logs em tempo real
tail -f logs/sistema-inventario.log

# Buscar erros específicos
grep "NÃO ENCONTRADOS" logs/sistema-inventario.log
```

### Validar Query SQL
```bash
# Executar script de teste
psql -h localhost -U inventario -d sispatrimonio -f sql/teste_relatorio_itens_nao_encontrados.sql
```

### Documentação Completa
- `CORRECAO_FILTRO_ITENS_NAO_ENCONTRADOS.md` - Detalhes técnicos
- `RESUMO_CORRECAO_FILTRO_NAO_ENCONTRADOS.md` - Resumo executivo

---

## ✅ Checklist de Uso

Antes de gerar o relatório:
- [ ] Inventário selecionado
- [ ] Conexão com banco ativa
- [ ] Sistema atualizado

Após gerar o relatório:
- [ ] Dados conferidos
- [ ] Resumo analisado
- [ ] Exportação realizada (se necessário)
- [ ] Ações planejadas

---

## 🎯 Próximos Passos

Após identificar itens não encontrados:

1. **Análise:** Verificar padrões (setor, responsável, localização)
2. **Planejamento:** Priorizar buscas por valor ou criticidade
3. **Execução:** Realizar buscas direcionadas
4. **Registro:** Atualizar status conforme encontrados
5. **Acompanhamento:** Gerar relatório novamente para verificar progresso

---

**Versão:** 1.2.0  
**Última atualização:** 18/11/2025  
**Status:** ✅ Funcional

🎉 **Aproveite o novo filtro de Itens Não Encontrados!**
