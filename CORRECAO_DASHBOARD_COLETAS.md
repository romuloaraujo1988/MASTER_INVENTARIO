# Correção: Dashboard não enviava quantidade de coletas do inventário ativo

## Problema Identificado

O endpoint `/api/mobile/dashboard/stats` estava retornando a contagem de **TODAS** as coletas do banco de dados, independente do inventário. Isso causava números incorretos no dashboard mobile, pois incluía coletas de inventários antigos, concluídos ou cancelados.

## Causa Raiz

No arquivo `MobileDashboardController.java`, o código estava tentando usar um método inexistente:

```java
// ❌ CÓDIGO ANTIGO (ERRADO)
List<com.inventario.model.Coleta> todasColetas = coletaDAO.findAll();
patrimoniosColetados = todasColetas != null ? todasColetas.size() : 0;
```

**Problemas:**
1. O método `findAll()` não existe em `ColetaDAO`
2. Mesmo que existisse, retornaria coletas de TODOS os inventários
3. Não filtrava pelo inventário ativo (STATUS_EM_ANDAMENTO)

## Solução Implementada

### 1. Injeção do InventarioDAO

Adicionado o `InventarioDAO` para buscar o inventário ativo:

```java
@Autowired
private com.inventario.dao.InventarioDAO inventarioDAO;
```

### 2. Buscar Inventário Ativo

Antes de contar coletas, agora buscamos o inventário com status `EM_ANDAMENTO`:

```java
// Buscar inventário ativo (EM_ANDAMENTO)
com.inventario.model.Inventario inventarioAtivo = null;
try {
    inventarioAtivo = inventarioDAO.buscarInventarioPorStatus(
        com.inventario.model.Inventario.STATUS_EM_ANDAMENTO
    );
    if (inventarioAtivo != null) {
        logger.info("Inventário ativo encontrado: ID={}, Nome={}", 
            inventarioAtivo.getId(), inventarioAtivo.getNome());
    } else {
        logger.warn("Nenhum inventário EM_ANDAMENTO encontrado");
    }
} catch (Exception e) {
    logger.error("Erro ao buscar inventário ativo: {}", e.getMessage(), e);
}
```

### 3. Filtrar Coletas por Inventário

Agora usamos o método correto `buscarPorInventario()` que já existe no `ColetaDAO`:

```java
// ✅ CÓDIGO NOVO (CORRETO)
if (inventarioAtivo != null) {
    try {
        List<com.inventario.model.Coleta> coletasInventario = 
            coletaDAO.buscarPorInventario(inventarioAtivo.getId());
        patrimoniosColetados = coletasInventario != null ? coletasInventario.size() : 0;
        logger.info("Total de coletas do inventário ativo (ID={}): {}", 
            inventarioAtivo.getId(), patrimoniosColetados);
    } catch (Exception e) {
        logger.error("Erro ao buscar coletas do inventário: {}", e.getMessage(), e);
        patrimoniosColetados = 0;
    }
} else {
    logger.warn("Sem inventário ativo, patrimoniosColetados = 0");
    patrimoniosColetados = 0;
}
```

## Benefícios da Correção

✅ **Precisão**: Dashboard mostra apenas coletas do inventário atual  
✅ **Performance**: Não busca coletas desnecessárias de inventários antigos  
✅ **Logs**: Melhor rastreabilidade com logs detalhados  
✅ **Robustez**: Tratamento de erro quando não há inventário ativo  

## Comportamento Esperado

### Cenário 1: Inventário Ativo Existe
- Busca inventário com `STATUS_EM_ANDAMENTO`
- Conta apenas coletas desse inventário
- Retorna número correto no campo `patrimoniosColetados`

### Cenário 2: Nenhum Inventário Ativo
- Retorna `patrimoniosColetados = 0`
- Log de aviso é gerado
- Dashboard mostra 0 coletas (correto)

## Testes Recomendados

1. **Teste com inventário ativo:**
   - Criar inventário com status `EM_ANDAMENTO`
   - Fazer algumas coletas
   - Verificar se dashboard mostra número correto

2. **Teste sem inventário ativo:**
   - Finalizar todos os inventários
   - Verificar se dashboard mostra 0 coletas

3. **Teste com múltiplos inventários:**
   - Ter inventários concluídos com coletas antigas
   - Ter inventário ativo com coletas novas
   - Verificar se dashboard mostra apenas coletas do ativo

## Arquivos Modificados

- `src/main/java/com/inventario/mobile/server/controller/MobileDashboardController.java`

## Status dos Inventários

Conforme definido em `Inventario.java`:

```java
public static final String STATUS_PLANEJADO = "PLANEJADO";
public static final String STATUS_EM_ANDAMENTO = "EM_ANDAMENTO";
public static final String STATUS_CONCLUIDO = "CONCLUIDO";
public static final String STATUS_CANCELADO = "CANCELADO";
```

Apenas inventários com `STATUS_EM_ANDAMENTO` são considerados ativos.

---

**Data da Correção:** 09/11/2025  
**Versão:** 1.2.0
