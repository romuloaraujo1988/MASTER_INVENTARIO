# Correção: Filtro de Salas Abertas para Coleta

## ❌ Problema Identificado

O `ColetaFrame_v2` estava carregando **TODAS as salas** do sistema, incluindo salas que já foram finalizadas no inventário ativo.

### Código Anterior (Incorreto)
```java
private void carregarSalas() {
    try {
        todasSalas = salaDAO.listarSalas();  // ← Carrega TODAS as salas
    } catch (Exception e) {
        // ...
    }
}
```

**Impacto:**
- Coletores viam salas já finalizadas
- Possibilidade de reabrir coletas finalizadas acidentalmente
- Confusão sobre quais salas ainda precisam ser coletadas

---

## ✅ Solução Implementada

### 1. Novo Método no `SalaInventarioDAO`

Criado o método `buscarSalasAbertasParaColeta(int idInventario)`:

```java
/**
 * Busca todas as salas abertas (não finalizadas) para coleta em um inventário
 * 
 * @param idInventario ID do inventário
 * @return Lista de salas abertas para coleta
 */
public List<Sala> buscarSalasAbertasParaColeta(int idInventario) {
    String sql = "SELECT s.* FROM TABELA_SALA s " +
                "INNER JOIN TABELA_SALA_INVENTARIO si ON s.ID_SALA = si.ID_SALA " +
                "WHERE si.ID_INVENTARIO = ? " +
                "AND (si.COLETA_FINALIZADA = FALSE OR si.COLETA_FINALIZADA IS NULL) " +
                "ORDER BY s.NUMERO_SALA";
    // ...
}
```

**Lógica:**
- Faz JOIN entre `TABELA_SALA` e `TABELA_SALA_INVENTARIO`
- Filtra pelo inventário ativo
- Retorna apenas salas onde `COLETA_FINALIZADA = FALSE` ou `NULL`
- Ordena por número da sala

### 2. Atualização do `ColetaFrame_v2`

Modificado o método `carregarSalas()`:

```java
private void carregarSalas() {
    try {
        // Buscar inventário ativo
        Inventario inventarioAtivo = inventarioDAO.buscarPorStatus("EM_ANDAMENTO");
        
        if (inventarioAtivo == null) {
            todasSalas = new ArrayList<>();
            JOptionPane.showMessageDialog(this, 
                "Nenhum inventário ativo encontrado.\nNão é possível realizar coletas.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
        } else {
            // Carregar apenas salas abertas (não finalizadas)
            todasSalas = salaInventarioDAO.buscarSalasAbertasParaColeta(inventarioAtivo.getId());
            
            if (todasSalas.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Nenhuma sala aberta para coleta neste inventário.\n" +
                    "Todas as salas já foram finalizadas ou nenhuma sala foi vinculada ao inventário.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        }
    } catch (Exception e) {
        // ...
    }
}
```

---

## 🎯 Benefícios

### 1. Controle de Acesso
- ✅ Coletores veem apenas salas abertas
- ✅ Salas finalizadas não aparecem na lista
- ✅ Previne reabertura acidental de coletas

### 2. Organização
- ✅ Lista focada apenas no trabalho pendente
- ✅ Reduz confusão sobre status das salas
- ✅ Facilita o trabalho do coletor

### 3. Integridade de Dados
- ✅ Respeita o status de finalização
- ✅ Mantém consistência com o inventário
- ✅ Evita coletas em salas já concluídas

### 4. Mensagens Informativas
- ✅ Avisa se não há inventário ativo
- ✅ Informa se todas as salas foram finalizadas
- ✅ Orienta o usuário sobre o que fazer

---

## 🔍 Casos de Uso

### Caso 1: Inventário com Salas Abertas
```
1. Sistema busca inventário ativo
2. Carrega salas onde COLETA_FINALIZADA = FALSE
3. Exibe lista de salas abertas no combo
4. Coletor seleciona sala e inicia coleta
```

### Caso 2: Todas as Salas Finalizadas
```
1. Sistema busca inventário ativo
2. Não encontra salas abertas
3. Exibe mensagem: "Nenhuma sala aberta para coleta"
4. Combo fica vazio
```

### Caso 3: Sem Inventário Ativo
```
1. Sistema não encontra inventário ativo
2. Exibe mensagem: "Nenhum inventário ativo encontrado"
3. Combo fica vazio
4. Coleta não pode ser realizada
```

### Caso 4: Sala Finalizada Durante Coleta
```
1. Coletor está trabalhando em uma sala
2. Finaliza a coleta da sala
3. Ao recarregar, sala não aparece mais na lista
4. Apenas salas abertas são exibidas
```

---

## 🧪 Como Testar

### Teste 1: Verificar Filtro de Salas
```sql
-- 1. Verificar salas vinculadas ao inventário
SELECT s.NUMERO_SALA, si.COLETA_FINALIZADA
FROM TABELA_SALA s
INNER JOIN TABELA_SALA_INVENTARIO si ON s.ID_SALA = si.ID_SALA
WHERE si.ID_INVENTARIO = 1;

-- 2. Abrir ColetaFrame_v2
-- 3. Verificar que apenas salas com COLETA_FINALIZADA = FALSE aparecem
```

### Teste 2: Finalizar Sala e Verificar
```
1. Abrir ColetaFrame_v2
2. Selecionar uma sala
3. Coletar alguns itens
4. Clicar em "Finalizar Coleta da Sala"
5. Fechar e reabrir ColetaFrame_v2
6. Verificar que a sala finalizada não aparece mais
```

### Teste 3: Reabrir Sala Finalizada
```
1. Abrir ColetaFrame_v2
2. Verificar que sala finalizada não aparece
3. No banco, executar:
   UPDATE TABELA_SALA_INVENTARIO 
   SET COLETA_FINALIZADA = FALSE 
   WHERE ID_SALA = X AND ID_INVENTARIO = Y;
4. Fechar e reabrir ColetaFrame_v2
5. Verificar que a sala agora aparece na lista
```

### Teste 4: Sem Inventário Ativo
```
1. No banco, executar:
   UPDATE TABELA_INVENTARIO SET STATUS = 'FINALIZADO';
2. Abrir ColetaFrame_v2
3. Verificar mensagem: "Nenhum inventário ativo encontrado"
4. Verificar que combo está vazio
```

---

## 📋 Checklist de Validação

- [x] Método `buscarSalasAbertasParaColeta` criado
- [x] Query SQL filtra corretamente
- [x] `carregarSalas()` atualizado
- [x] Mensagens informativas adicionadas
- [x] Código compilando sem erros
- [ ] **PENDENTE:** Testar em ambiente real
- [ ] **PENDENTE:** Validar com usuários

---

## 🔧 Arquivos Modificados

1. `src/main/java/com/inventario/dao/SalaInventarioDAO.java`
   - Adicionado método `buscarSalasAbertasParaColeta()`

2. `src/main/java/com/inventario/view/ColetaFrame_v2.java`
   - Modificado método `carregarSalas()`
   - Adicionadas validações e mensagens

---

## 💡 Melhorias Futuras

### Curto Prazo
- [ ] Adicionar contador de salas abertas vs finalizadas
- [ ] Mostrar progresso do inventário (X de Y salas concluídas)
- [ ] Filtro adicional por setor/campus

### Médio Prazo
- [ ] Indicador visual de salas próximas de finalização
- [ ] Estatísticas por sala (itens coletados, pendentes)
- [ ] Ordenação por prioridade ou % de conclusão

### Longo Prazo
- [ ] Dashboard de progresso por sala
- [ ] Notificações quando sala é finalizada
- [ ] Histórico de finalizações

---

**Data de Implementação:** 18/11/2025  
**Versão:** 1.0.0  
**Status:** ✅ IMPLEMENTADO | ⏳ AGUARDANDO TESTES
