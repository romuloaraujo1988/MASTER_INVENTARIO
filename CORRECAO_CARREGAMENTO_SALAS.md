# Correção: Carregamento de Apenas 3 Salas ao Invés de 108

## 🔍 Problema Identificado

O sistema estava carregando apenas **3 salas** ao invés das **108 salas** esperadas no combo de seleção do `ColetaFrame_v2`.

## 🎯 Causa Raiz

O método `SalaInventarioDAO.buscarSalasAbertasParaColeta()` estava filtrando as salas com a seguinte condição:

```sql
WHERE s.ATIVO = TRUE 
AND (si.COLETA_FINALIZADA = FALSE OR si.COLETA_FINALIZADA IS NULL)
```

Isso significa que **apenas salas não finalizadas** eram retornadas. Se você tinha:
- **108 salas ativas** no total
- **3 salas abertas** (não finalizadas)
- **105 salas finalizadas** (marcadas como concluídas)

Apenas as 3 salas abertas apareciam no combo.

## ✅ Solução Aplicada

### Alteração no `SalaInventarioDAO.java`

**ANTES:**
```java
sql = "SELECT DISTINCT s.ID_SALA, s.NUMERO_SALA, s.DESCRICAO, s.ID_SETOR, s.ATIVO, " +
     "CAST(NULL AS TIMESTAMP) AS DATA_CADASTRO " +
     "FROM TABELA_SALA s " +
     "LEFT JOIN TABELA_SALA_INVENTARIO si ON s.ID_SALA = si.ID_SALA AND si.ID_INVENTARIO = ? " +
     "WHERE s.ATIVO = TRUE " +
     "AND (si.COLETA_FINALIZADA = FALSE OR si.COLETA_FINALIZADA IS NULL) " +  // ❌ FILTRO REMOVIDO
     "ORDER BY s.NUMERO_SALA";
```

**DEPOIS:**
```java
sql = "SELECT DISTINCT s.ID_SALA, s.NUMERO_SALA, s.DESCRICAO, s.ID_SETOR, s.ATIVO, " +
     "CAST(NULL AS TIMESTAMP) AS DATA_CADASTRO " +
     "FROM TABELA_SALA s " +
     "LEFT JOIN TABELA_SALA_INVENTARIO si ON s.ID_SALA = si.ID_SALA AND si.ID_INVENTARIO = ? " +
     "WHERE s.ATIVO = TRUE " +
     "ORDER BY s.NUMERO_SALA";  // ✅ MOSTRA TODAS AS SALAS ATIVAS
```

### Resultado

Agora **todas as 108 salas ativas** aparecerão no combo, independente de estarem finalizadas ou não.

## 📊 Como Verificar o Status das Salas

Execute o script SQL fornecido:

```bash
psql -h localhost -U inventario -d sispatrimonio -f verificar-salas-finalizadas.sql
```

Isso mostrará:
- Total de salas ativas
- Quantas estão finalizadas
- Quantas estão abertas
- Detalhes de cada sala

## 🔄 Como Reabrir Salas Finalizadas (Opcional)

Se você quiser **reabrir todas as salas finalizadas** para permitir novas coletas:

```powershell
.\reabrir-todas-salas.ps1
```

**⚠️ ATENÇÃO:** Isso reabrirá TODAS as salas finalizadas. Use com cuidado!

## 🎨 Comportamento Atual

### No ColetaFrame_v2

1. **Combo de Salas**: Mostra todas as 108 salas ativas
2. **Seleção de Sala**: Ao selecionar uma sala:
   - Se **não finalizada**: Botão "🏁 Finalizar" habilitado (verde)
   - Se **finalizada**: Botão "⚠️ Reabrir" habilitado (amarelo) - apenas admin/supervisor

3. **Indicador Visual**: O label de resumo mostra:
   - `"Coletando em: Sala 101"` - sala aberta
   - `"Coletando em: Sala 101 [FINALIZADA]"` - sala finalizada

### Permissões

- **Coletor**: Pode coletar em salas abertas, não pode reabrir
- **Supervisor**: Pode coletar e reabrir salas finalizadas
- **Admin**: Pode coletar, reabrir e remover itens

## 📝 Arquivos Alterados

1. ✅ `src/main/java/com/inventario/dao/SalaInventarioDAO.java`
   - Método `buscarSalasAbertasParaColeta()` - Removido filtro de finalização

2. 🆕 `verificar-salas-finalizadas.sql`
   - Script para diagnosticar status das salas

3. 🆕 `reabrir-todas-salas.ps1`
   - Script para reabrir salas finalizadas em lote

4. 🆕 `CORRECAO_CARREGAMENTO_SALAS.md`
   - Este documento

## 🧪 Como Testar

1. **Recompilar o projeto:**
   ```bash
   mvn clean compile
   ```

2. **Executar a aplicação:**
   ```bash
   mvn exec:java -Dexec.mainClass="com.inventario.SistemaInventarioApplication"
   ```

3. **Abrir ColetaFrame_v2:**
   - Menu → Coleta → Coleta de Patrimônios v2

4. **Verificar combo de salas:**
   - Deve mostrar todas as 108 salas ativas
   - Salas finalizadas terão indicador visual ao serem selecionadas

## 📈 Logs Esperados

Ao abrir o ColetaFrame_v2, você verá nos logs:

```
=== INÍCIO buscarSalasAbertasParaColeta ===
Inventário ID: 1
Tipo de banco detectado: postgresql (PostgreSQL/Online)
SQL preparado:
SELECT DISTINCT s.ID_SALA, s.NUMERO_SALA, s.DESCRICAO, s.ID_SETOR, s.ATIVO, ...
FROM TABELA_SALA s ...
WHERE s.ATIVO = TRUE 
ORDER BY s.NUMERO_SALA

Executando query...
Query executada com sucesso, processando resultados...

--- Processando sala 1 ---
  ID_SALA: 1
  NUMERO_SALA: 101
  ...
  ✓ Sala criada: 101 - Sala de Aula

--- Processando sala 2 ---
  ...

=== Total de salas processadas: 108 ===
=== Salas adicionadas à lista: 108 ===
DEBUG ColetaFrame: buscarSalasAbertasParaColeta() retornou 108 salas
DEBUG ColetaFrame: Combo preenchido com 109 itens (incluindo null)
```

## ✅ Resultado Final

- ✅ Todas as 108 salas ativas aparecem no combo
- ✅ Salas finalizadas podem ser reabertas por admin/supervisor
- ✅ Indicador visual mostra status de cada sala
- ✅ Logs detalhados para debugging

---

**Data da Correção:** 21/11/2025  
**Versão:** 2.0.1  
**Status:** ✅ CORRIGIDO
