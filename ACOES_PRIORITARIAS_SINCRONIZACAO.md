# Ações Prioritárias: Sincronização SQLite → PostgreSQL

## ✅ IMPLEMENTADO (26/11/2025)

### 1. Correção do ID_PARTICIPANTE = 0
- **Arquivo:** `ColetaFrame_v2.java`
- **Mudança:** Adicionada busca via `ParticipanteInventarioDAO`
- **Status:** ✅ Compilado e pronto para teste

### 2. Validação de Duplicatas
- **Arquivo:** `DataSynchronizer.java`
- **Mudança:** Verifica se coleta já existe antes de inserir
- **Benefício:** Evita duplicação no PostgreSQL
- **Status:** ✅ Implementado

---

## 🔴 CRÍTICO - Fazer AGORA

### 3. Testar Sincronização Completa

```bash
# 1. Fazer nova coleta
# 2. Verificar no SQLite
sqlite3 data/inventario.db "SELECT id, id_participante, numero_patrimonio FROM local_coleta ORDER BY id DESC LIMIT 1;"

# 3. Sincronizar
# 4. Verificar logs
# Esperado: "Coleta sincronizada com sucesso!"
```

### 4. Verificar Coletas Antigas com Problema

```bash
# Identificar coletas com id_participante = 0
sqlite3 data/inventario.db "SELECT COUNT(*) FROM local_coleta WHERE id_participante = 0 AND sync_status = 'PENDING';"
```

**Se houver coletas antigas:**

```sql
-- Corrigir manualmente (CUIDADO!)
UPDATE local_coleta 
SET id_participante = (
    SELECT id FROM local_usuario LIMIT 1
)
WHERE id_participante = 0;
```

---

## ⚠️ IMPORTANTE - Fazer em Breve

### 5. Adicionar Campos no SQLite (Opcional mas Recomendado)

```bash
# Executar script
sqlite3 data/inventario.db < sql/melhorar_sqlite_coleta_campos.sql
```

**Benefícios:**
- Métricas de performance (tempo de coleta)
- Geolocalização (latitude/longitude)
- Qualidade da etiqueta
- Método de coleta (QR Code, manual, etc.)

### 6. Monitorar Logs de Sincronização

Verificar se aparecem:
- ✅ "Coleta sincronizada com sucesso!"
- ❌ "ID_PARTICIPANTE inválido"
- ⚠️ "Coleta duplicada detectada"
- ❌ "Erro ao converter timestamp"

---

## 💡 MELHORIAS - Fazer Quando Possível

### 7. Adicionar Validação de Referências Estrangeiras

Verificar se IDs existem no PostgreSQL antes de sincronizar:
- Patrimônio existe?
- Inventário existe?
- Participante existe?

### 8. Melhorar Tratamento de Erros

- Retry automático em falhas de rede
- Queue de sincronização com prioridade
- Notificações de falha

### 9. Dashboard de Sincronização

- Total de coletas pendentes
- Taxa de sucesso/falha
- Tempo médio de sincronização
- Alertas de problemas

---

## 🧪 SCRIPT DE TESTE COMPLETO

### Teste 1: Verificar Estrutura

```bash
# SQLite
sqlite3 data/inventario.db "PRAGMA table_info(local_coleta);"

# PostgreSQL (via MCP)
# SELECT column_name FROM information_schema.columns WHERE table_name = 'tabela_coleta';
```

### Teste 2: Verificar Dados Pendentes

```bash
sqlite3 data/inventario.db "
SELECT 
    COUNT(*) as total,
    SUM(CASE WHEN id_participante = 0 THEN 1 ELSE 0 END) as invalidos,
    SUM(CASE WHEN id_participante > 0 THEN 1 ELSE 0 END) as validos
FROM local_coleta 
WHERE sync_status = 'PENDING';
"
```

### Teste 3: Sincronizar e Verificar

```bash
# 1. Anotar total de coletas pendentes
# 2. Clicar em "Sincronizar"
# 3. Verificar logs
# 4. Verificar se total pendente diminuiu
```

### Teste 4: Verificar no PostgreSQL (via MCP)

```sql
-- Verificar última coleta sincronizada
SELECT 
    id,
    id_patrimonio,
    id_inventario,
    id_participante_inventario,
    data_coleta,
    localizacao_encontrada
FROM tabela_coleta 
ORDER BY id DESC 
LIMIT 5;
```

---

## 📊 MÉTRICAS DE SUCESSO

### Antes da Correção
- ❌ Coletas com `id_participante = 0`
- ❌ Sincronização falhando
- ❌ Dados presos no SQLite

### Depois da Correção
- ✅ Todas coletas com `id_participante` válido
- ✅ Sincronização funcionando
- ✅ Dados no PostgreSQL
- ✅ Duplicatas evitadas

### Metas
- **Taxa de sucesso:** > 95%
- **Tempo de sincronização:** < 5 segundos para 100 coletas
- **Duplicatas:** 0
- **Erros de validação:** 0

---

## 🚨 ALERTAS

### Se aparecer "ID_PARTICIPANTE inválido"
1. Verificar se usuário está logado
2. Verificar se usuário é participante do inventário
3. Verificar tabela `tabela_participante_inventario`

### Se aparecer "Coleta duplicada"
1. Normal se sincronizar múltiplas vezes
2. Sistema marca como sincronizada automaticamente
3. Não é erro, é proteção

### Se aparecer "Erro ao converter timestamp"
1. Verificar formato da data no SQLite
2. Verificar logs detalhados
3. Sistema usa data atual como fallback

---

## 📞 SUPORTE

### Logs Importantes
- `logs/sistema-inventario.log` - Log principal
- Console do aplicativo - Logs em tempo real

### Comandos Úteis

```bash
# Ver últimas 50 linhas do log
tail -n 50 logs/sistema-inventario.log

# Procurar erros
grep "ERROR\|WARNING" logs/sistema-inventario.log | tail -n 20

# Verificar sincronização
grep "sincronizada com sucesso" logs/sistema-inventario.log | wc -l
```

---

## ✅ CHECKLIST FINAL

- [x] Código corrigido (id_participante)
- [x] Validação de duplicatas adicionada
- [x] Compilação bem-sucedida
- [ ] Teste de coleta normal
- [ ] Teste de sincronização
- [ ] Verificação de logs
- [ ] Validação no PostgreSQL
- [ ] Documentação atualizada

---

**Versão:** 2.0.1  
**Data:** 26/11/2025  
**Status:** ✅ Pronto para teste  
**Prioridade:** 🔴 CRÍTICO
