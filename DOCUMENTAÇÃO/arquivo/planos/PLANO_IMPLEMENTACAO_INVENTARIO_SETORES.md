# Plano de Implementação - Seleção de Ambientes para Inventário

## 1. Objetivo
Implementar a funcionalidade de seleção de setores/ambientes específicos para cada inventário, permitindo que o usuário escolha quais setores farão parte do processo de inventário.

## 2. Análise da Situação Atual

### 2.1 Interface Existente
- O `InventarioFormDialog.java` já possui uma aba "Escopo do Inventário" com:
  - Checkbox "Incluir todos os setores"
  - Lista de setores para seleção múltipla
  - Interface funcional mas sem persistência no banco

### 2.2 Estrutura do Banco Atual
- `TABELA_INVENTARIO`: Não possui campos para armazenar setores selecionados
- `TABELA_SETOR`: Existe e está funcional
- **Problema**: Não existe relacionamento entre inventário e setores

## 3. Solução Proposta

### 3.1 Criar Tabela de Relacionamento
Criar `TABELA_INVENTARIO_SETOR` para relacionar inventários com setores específicos.

### 3.2 Estrutura da Nova Tabela
```sql
CREATE TABLE TABELA_INVENTARIO_SETOR (
    ID SERIAL PRIMARY KEY,
    ID_INVENTARIO INTEGER NOT NULL,
    ID_SETOR INTEGER NOT NULL,
    INCLUIR_TODOS_SETORES BOOLEAN DEFAULT FALSE,
    DATA_INCLUSAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ATIVO BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (ID_INVENTARIO) REFERENCES TABELA_INVENTARIO(ID) ON DELETE CASCADE,
    FOREIGN KEY (ID_SETOR) REFERENCES TABELA_SETOR(ID),
    UNIQUE(ID_INVENTARIO, ID_SETOR)
);
```

### 3.3 Lógica de Funcionamento
1. **Incluir Todos os Setores = TRUE**: 
   - Inserir um registro especial com `ID_SETOR = NULL` e `INCLUIR_TODOS_SETORES = TRUE`
   - Não inserir registros individuais de setores

2. **Incluir Todos os Setores = FALSE**:
   - Inserir um registro para cada setor selecionado
   - `INCLUIR_TODOS_SETORES = FALSE` para todos os registros

## 4. Implementação

### 4.1 Fase 1: Estrutura do Banco de Dados
- [ ] Criar script SQL para a nova tabela
- [ ] Criar índices para performance
- [ ] Adicionar comentários e documentação

### 4.2 Fase 2: Modelo de Dados
- [ ] Criar classe `InventarioSetor.java`
- [ ] Criar `InventarioSetorDAO.java`
- [ ] Implementar métodos CRUD

### 4.3 Fase 3: Modificações na Interface
- [ ] Atualizar `InventarioFormDialog.java` para salvar setores selecionados
- [ ] Implementar carregamento de setores na edição
- [ ] Adicionar validações

### 4.4 Fase 4: Integração com Sistema de Coleta
- [ ] Modificar `ColetaFrame_v2.java` para filtrar salas por setores do inventário
- [ ] Atualizar relatórios para considerar escopo do inventário
- [ ] Implementar validações de escopo

## 5. Benefícios

### 5.1 Funcionalidades
- Seleção granular de ambientes para inventário
- Controle de escopo por inventário
- Relatórios mais precisos
- Melhor organização do processo

### 5.2 Melhorias no Processo
- Inventários parciais por setor
- Distribuição de trabalho por equipes
- Controle de progresso por ambiente
- Flexibilidade na execução

## 6. Impacto em Outras Funcionalidades

### 6.1 Sistema de Coleta
- Filtrar patrimônios por setores do inventário
- Validar se sala pertence ao escopo
- Atualizar contadores de progresso

### 6.2 Relatórios
- Incluir informações de escopo
- Filtros por setores selecionados
- Estatísticas por ambiente

### 6.3 Dashboard
- Progresso por setor (apenas setores do escopo)
- Indicadores de conclusão
- Alertas de pendências

## 7. Considerações Técnicas

### 7.1 Performance
- Índices nas chaves estrangeiras
- Consultas otimizadas
- Cache de setores por inventário

### 7.2 Integridade
- Cascade delete para limpeza automática
- Validações de negócio
- Transações para consistência

### 7.3 Migração
- Script de migração para inventários existentes
- Definir comportamento padrão (todos os setores)
- Backup antes da migração

## 8. Cronograma Estimado

- **Fase 1**: 1 dia (Banco de dados)
- **Fase 2**: 2 dias (Modelo e DAO)
- **Fase 3**: 2 dias (Interface)
- **Fase 4**: 3 dias (Integração)
- **Total**: 8 dias úteis

## 9. Riscos e Mitigações

### 9.1 Riscos
- Impacto em funcionalidades existentes
- Complexidade na migração de dados
- Performance em consultas complexas

### 9.2 Mitigações
- Testes extensivos antes da implementação
- Backup completo do banco
- Implementação incremental
- Rollback plan definido

## 10. Próximos Passos

1. Aprovação do plano
2. Criação do script SQL
3. Implementação do modelo de dados
4. Atualização da interface
5. Testes e validação
6. Deploy em produção

---

**Documento criado em**: $(date)
**Versão**: 1.0
**Status**: Proposta