# Correção de Itens Compostos Coletados Normalmente - Resumo

## 📋 Problema Identificado

**Situação:**
- 106 itens compostos foram coletados como coletas normais
- Isso viola a regra de negócio: itens compostos devem ser coletados apenas através do fluxo de coleta composta
- Dados estão incompletos (faltam registros de componentes)

**Exemplo Real:**
- Patrimônio 303752 (Conjunto Escolar): 2 componentes cadastrados
- Foi coletado como coleta normal (ID 6946)
- Nenhum componente foi registrado na `tabela_coleta_componente`

## 📊 Estatísticas

| Métrica | Valor |
|---------|-------|
| Total de itens compostos cadastrados | 822 |
| Total de componentes cadastrados | 1.670 |
| Itens compostos totalmente coletados | 179 |
| Itens compostos coletados normalmente (INCORRETO) | 106 |
| Itens compostos com coleta parcial | 537 |
| Itens com APENAS coleta normal | 80 (75.5%) |
| Itens com coleta DUPLA (normal + composta) | 71 (24.5%) |

## 🎯 Solução Planejada

### Fase 1: Detecção e Relatório (Prioridade Alta)
- Criar query SQL para identificar itens compostos coletados normalmente
- Criar endpoint REST para exportar relatório
- Gerar relatório em CSV/PDF

### Fase 2: Prevenção (Prioridade Alta)
- Adicionar validação no `ColetaService` antes de salvar
- Verificar se patrimônio é composto
- Mostrar mensagem de erro e redirecionar para fluxo composto
- Adicionar validação no frontend (ColetaFrame)

### Fase 3: Correção de Dados (Prioridade Média)
- **80 itens com APENAS coleta normal:** Converter para coleta composta
- **71 itens com coleta DUPLA:** Remover coleta normal duplicada
- Criar interface para revisão e confirmação
- Manter histórico de correções
- Validar integridade após correção

### Fase 4: Alertas e Dashboard (Prioridade Média)
- Criar endpoint para verificar status de itens compostos
- Adicionar alertas visuais na tela de coleta
- Criar dashboard com estatísticas de itens compostos
- Implementar notificações

## 📁 Arquivos Criados

### Specs
- `.kiro/specs/validacao-itens-compostos-coletas/requirements.md`
- `.kiro/specs/validacao-itens-compostos-coletas/design.md`
- `.kiro/specs/validacao-itens-compostos-coletas/tasks.md`

### Documentação
- `CORRECAO_ITENS_COMPOSTOS_COLETAS.md` (este arquivo)

## 🚀 Próximos Passos

1. **Executar Fase 1** - Detecção e Relatório
   - Identificar todos os 106 itens coletados incorretamente
   - Gerar relatório para análise

2. **Executar Fase 2** - Prevenção
   - Bloquear novas coletas incorretas
   - Garantir que itens compostos só sejam coletados no fluxo correto

3. **Executar Fase 3** - Correção
   - Converter coletas incorretas para compostas
   - Manter histórico de correções

## 📝 Notas Técnicas

### Validação de Coleta Normal
```java
List<ItemComposto> itensComposto = itemCompostoDAO.findByPatrimonioPrincipal(coleta.getIdPatrimonio());
if (!itensComposto.isEmpty()) {
    return Result.failure(new IllegalArgumentException(
        "Este patrimônio é composto e deve ser coletado através do fluxo de coleta composta."
    ));
}
```

### Query de Detecção
```sql
SELECT 
    p.id as patrimonio_id,
    p.numero as numero_patrimonio,
    c.id as coleta_id,
    COUNT(ic.id) as total_componentes,
    COUNT(cc.id) as componentes_coletados
FROM tabela_patrimonio p
INNER JOIN tabela_coleta c ON p.id = c.id_patrimonio
INNER JOIN tabela_item_composto ic ON p.id = ic.id_patrimonio_principal
LEFT JOIN tabela_coleta_componente cc ON ic.id = cc.id_item_composto AND cc.id_inventario = c.id_inventario
WHERE c.id_inventario = 2
GROUP BY p.id, p.numero, c.id
HAVING COUNT(ic.id) > 0
ORDER BY p.numero;
```

### Tipos de Correção

**Caso 1: APENAS NORMAL (80 itens - 75.5%)**
- Coleta normal existe, mas não há coleta composta
- Ação: Converter coleta normal em composta
- Criar registros de componentes
- Remover coleta normal

**Caso 2: DUPLA (71 itens - 24.5%)**
- Coleta normal E coleta composta existem
- Ação: Remover coleta normal duplicada
- Manter coleta composta válida

---

**Data da Análise:** 11/02/2026  
**Versão:** 1.0.0  
**Status:** ✅ Spec Completa - Pronto para Implementação  
**Impacto:** Alto - Garantir integridade dos dados de coleta
