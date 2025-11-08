# Guia Rápido - Importação de Patrimônios

## 🎯 Objetivo

Importar dados patrimoniais do arquivo Excel `PATRIMONIO IFMT 25.05.2025.xls` para o sistema.

---

## ⏱️ Tempo Estimado

- **Preparação**: 5 minutos
- **Importação**: 3-5 minutos
- **Verificação**: 5 minutos
- **Total**: 15 minutos

---

## ✅ Pré-requisitos

- [ ] Sistema de inventário instalado e funcionando
- [ ] Banco de dados PostgreSQL configurado
- [ ] Arquivo `PATRIMONIO IFMT 25.05.2025.xls` disponível
- [ ] Backup do banco de dados realizado (recomendado)

---

## 📋 Passo a Passo

### 1. Fazer Backup (Recomendado)

**Por que?** Para poder reverter em caso de problemas.

**Como fazer:**
```bash
# Windows PowerShell
pg_dump -h localhost -U inventario sispatrimonio > backup_antes_importacao.sql
```

**Tempo**: 1-2 minutos

---

### 2. Abrir o Sistema

1. Executar o sistema de inventário
2. Fazer login com suas credenciais
3. Aguardar carregamento da tela principal

---

### 3. Acessar Importação

**Opção 1**: Via Menu
- Menu → **Ferramentas** → **Importação de Dados**

**Opção 2**: Via Atalho (se disponível)
- Botão "Importar" na tela principal

---

### 4. Selecionar Arquivo

1. Clicar no botão **"Selecionar"**
2. Navegar até a pasta onde está o arquivo
3. Selecionar `PATRIMONIO IFMT 25.05.2025.xls`
4. Clicar em **"Abrir"**

**Resultado**: Caminho do arquivo aparece no campo de texto

---

### 5. Configurar Opções

**Opções Recomendadas** (já vêm marcadas por padrão):

- ✅ **Criar responsáveis automaticamente**
  - Sistema cria responsáveis que não existem

- ✅ **Criar setores automaticamente**
  - Sistema cria setores que não existem

- ✅ **Criar salas automaticamente**
  - Sistema cria salas que não existem

- ✅ **Atualizar patrimônios existentes**
  - Atualiza dados de patrimônios já cadastrados

- ✅ **Continuar processamento mesmo com erros**
  - Não para a importação se encontrar erros

**Recomendação**: Manter todas marcadas

---

### 6. Iniciar Importação

1. Clicar no botão **"Iniciar Importação"**
2. Aguardar processamento
3. Acompanhar progresso:
   - Barra de progresso
   - Log em tempo real
   - Estatísticas atualizadas

**Não feche a janela durante o processo!**

---

### 7. Acompanhar Progresso

**O que você verá:**

```
[LOG]
Iniciando leitura do arquivo Excel...
Cabeçalho do arquivo lido. Iniciando processamento dos dados...
Processadas 50 linhas - Inseridos: 35, Atualizados: 15, Erros: 0
Processadas 100 linhas - Inseridos: 68, Atualizados: 32, Erros: 0
Processadas 150 linhas - Inseridos: 102, Atualizados: 48, Erros: 0
...
Processamento concluído. Gerando relatório...
```

**Barra de Progresso**: Mostra percentual concluído

**Tempo Estimado**: 3-5 minutos para ~5.000 patrimônios

---

### 8. Verificar Relatório

**Ao final, você verá:**

```
=== RELATÓRIO DE IMPORTAÇÃO ===
Linhas processadas: 5,234
Itens inseridos: 3,156
Itens atualizados: 2,078
Erros: 12
Tempo de execução: 4m 23s

=== DETALHES DOS ERROS ===
- Linha 123: Número do patrimônio vazio
- Linha 456: Erro ao inserir patrimônio 789
...
```

**O que significa:**
- **Linhas processadas**: Total de patrimônios no arquivo
- **Itens inseridos**: Novos patrimônios adicionados
- **Itens atualizados**: Patrimônios existentes atualizados
- **Erros**: Linhas com problemas (detalhes abaixo)

---

### 9. Analisar Erros (se houver)

**Erros comuns:**

1. **"Número do patrimônio vazio"**
   - Linha sem número de patrimônio
   - Pode ignorar

2. **"Número insuficiente de campos"**
   - Linha com dados incompletos
   - Pode ignorar

3. **"Erro ao inserir/atualizar"**
   - Problema no banco de dados
   - Verificar detalhes

**Taxa de erro aceitável**: Até 1% (50 erros em 5.000 linhas)

**Se erros > 5%**: Verificar estrutura do arquivo

---

### 10. Verificar Dados Importados

**No Sistema:**
1. Ir para tela de **Patrimônios**
2. Verificar se novos itens aparecem
3. Conferir alguns patrimônios aleatórios

**No Banco (opcional):**
```sql
-- Total de patrimônios
SELECT COUNT(*) FROM patrimonio;

-- Últimos importados
SELECT numero, descricao, data_cadastro 
FROM patrimonio 
ORDER BY data_cadastro DESC 
LIMIT 10;

-- Patrimônios sem responsável
SELECT COUNT(*) 
FROM patrimonio 
WHERE id_responsavel IS NULL;
```

---

## 🎉 Pronto!

Sua importação está concluída. Os dados do arquivo Excel agora estão no sistema.

---

## ❓ Perguntas Frequentes

### P: Posso importar o mesmo arquivo duas vezes?

**R**: Sim. Na segunda vez, os patrimônios serão **atualizados** (não duplicados).

---

### P: O que acontece se eu fechar a janela durante a importação?

**R**: A importação será **interrompida**. Os itens já processados permanecerão no banco. Você pode reimportar o arquivo.

---

### P: Como desfazer uma importação?

**R**: Restaurar o backup do banco de dados:
```bash
psql -h localhost -U inventario -d sispatrimonio < backup_antes_importacao.sql
```

---

### P: Posso importar arquivos CSV também?

**R**: Sim! O sistema aceita tanto Excel (.xls, .xlsx) quanto CSV.

---

### P: Quanto tempo demora para importar 10.000 patrimônios?

**R**: Aproximadamente 8-10 minutos (cerca de 20 itens por segundo).

---

### P: O que fazer se der erro "Arquivo não encontrado"?

**R**: 
1. Verificar se o arquivo existe
2. Verificar se o caminho está correto
3. Verificar se o arquivo não está aberto no Excel

---

### P: Posso importar enquanto outros usuários usam o sistema?

**R**: Sim, mas é recomendado fazer em horário de menor uso para melhor performance.

---

## 🚨 Problemas Comuns

### Problema: "Erro ao ler arquivo Excel"

**Solução**:
1. Fechar o arquivo no Excel
2. Tentar novamente
3. Se persistir, salvar como .xlsx e tentar

---

### Problema: "Muitos erros de inserção"

**Solução**:
1. Verificar se opção "Atualizar patrimônios existentes" está marcada
2. Verificar estrutura do arquivo Excel
3. Verificar se banco de dados está acessível

---

### Problema: "Importação muito lenta"

**Solução**:
1. Fechar outros programas
2. Verificar conexão com banco de dados
3. Dividir arquivo em partes menores

---

### Problema: "Sistema travou durante importação"

**Solução**:
1. Aguardar alguns minutos (pode ser processamento pesado)
2. Se não responder, fechar e reabrir sistema
3. Verificar log de erros
4. Reimportar arquivo

---

## 📞 Suporte

**Em caso de dúvidas ou problemas:**

1. Consultar este guia
2. Verificar `ANALISE_IMPORTACAO_EXCEL_PATRIMONIO.md` (detalhes técnicos)
3. Verificar logs do sistema
4. Contatar suporte técnico

---

## 📊 Checklist de Verificação

Após a importação, verificar:

- [ ] Total de patrimônios aumentou
- [ ] Novos patrimônios aparecem na listagem
- [ ] Dados estão corretos (número, descrição, etc.)
- [ ] Responsáveis foram criados/associados
- [ ] Setores foram criados/associados
- [ ] Salas foram criadas/associadas
- [ ] Taxa de erro está aceitável (< 1%)
- [ ] Sistema está funcionando normalmente

---

## 💡 Dicas

1. **Primeira vez?** Teste com arquivo pequeno (10-20 linhas) primeiro

2. **Arquivo grande?** Importe em horário de menor uso do sistema

3. **Muitos erros?** Verifique estrutura do arquivo antes de reimportar

4. **Quer velocidade?** Desmarque "Criar automaticamente" se entidades já existem

5. **Precisa de auditoria?** Salve o relatório de importação (copiar log)

---

**Versão**: 1.0  
**Data**: 03/11/2025  
**Autor**: Sistema SIHCP
