# Plano de Implementação - Seleção de Ambientes para Inventário

## Visão Geral

Este documento descreve a implementação da funcionalidade de **seleção de ambientes (setores)** para o trabalho de inventário no Sistema de Inventário IFMT. A funcionalidade permite que o usuário escolha quais setores farão parte do escopo de cada inventário durante o cadastro.

## Problema Identificado

O sistema original não possuía uma forma estruturada de definir quais setores/ambientes fariam parte do escopo de um inventário específico. Embora existisse uma interface para seleção de setores no `InventarioFormDialog.java`, os dados não eram persistidos no banco de dados.

## Solução Implementada

### 1. Estrutura do Banco de Dados

#### Nova Tabela: `TABELA_INVENTARIO_SETOR`

```sql
CREATE TABLE TABELA_INVENTARIO_SETOR (
    ID INTEGER PRIMARY KEY AUTOINCREMENT,
    ID_INVENTARIO INTEGER NOT NULL,
    ID_SETOR INTEGER NULL, -- NULL quando INCLUIR_TODOS_SETORES = TRUE
    INCLUIR_TODOS_SETORES BOOLEAN NOT NULL DEFAULT FALSE,
    DATA_INCLUSAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ATIVO BOOLEAN NOT NULL DEFAULT TRUE,
    OBSERVACOES TEXT,
    
    FOREIGN KEY (ID_INVENTARIO) REFERENCES TABELA_INVENTARIO(ID),
    FOREIGN KEY (ID_SETOR) REFERENCES TABELA_SETOR(ID)
);
```

**Características:**
- Relaciona inventários com setores específicos
- Suporta configuração "incluir todos os setores"
- Permite múltiplos setores por inventário
- Controle de ativação/desativação
- Auditoria com data de inclusão

#### Views Auxiliares

1. **`VW_INVENTARIO_SETORES`**: Visão consolidada dos setores por inventário
2. **`VW_SALAS_ESCOPO_INVENTARIO`**: Salas incluídas no escopo de cada inventário

#### Funções e Triggers

1. **`fn_setor_no_escopo_inventario`**: Verifica se um setor está no escopo
2. **`fn_obter_setores_inventario`**: Obtém lista de setores de um inventário
3. **`tg_validar_inventario_setor`**: Valida integridade dos dados

### 2. Modelo de Dados (Java)

#### Classe `InventarioSetor`

**Localização:** `src/main/java/com/inventario/model/InventarioSetor.java`

**Principais características:**
- Representa o relacionamento inventário-setor
- Validação de regras de negócio
- Métodos de conveniência para verificações
- Suporte a clonagem e comparação

**Campos principais:**
```java
private Integer id;
private Integer idInventario;
private Integer idSetor; // NULL quando incluirTodosSetores = true
private Boolean incluirTodosSetores;
private LocalDateTime dataInclusao;
private Boolean ativo;
private String observacoes;
```

**Métodos importantes:**
- `isValido()`: Valida consistência do objeto
- `isIncluirTodos()`: Verifica se inclui todos os setores
- `isSetorEspecifico()`: Verifica se é para setor específico
- `getDescricaoEscopo()`: Retorna descrição textual do escopo

### 3. Camada de Acesso a Dados (DAO)

#### Classe `InventarioSetorDAO`

**Localização:** `src/main/java/com/inventario/dao/InventarioSetorDAO.java`

**Principais funcionalidades:**

1. **Operações CRUD básicas:**
   - `salvar(InventarioSetor)`: Insere novo relacionamento
   - `atualizar(InventarioSetor)`: Atualiza relacionamento existente
   - `buscarPorId(Integer)`: Busca por ID
   - `listarTodos()`: Lista todos os relacionamentos ativos

2. **Operações específicas de inventário:**
   - `buscarPorInventario(Integer)`: Busca relacionamentos de um inventário
   - `removerPorInventario(Integer)`: Remove todos os relacionamentos
   - `desativarPorInventario(Integer)`: Desativa relacionamentos

3. **Verificações de escopo:**
   - `inventarioIncluiTodosSetores(Integer)`: Verifica se inclui todos
   - `setorNoEscopoInventario(Integer, Integer)`: Verifica setor específico
   - `buscarSetoresDoInventario(Integer)`: Lista setores do inventário
   - `contarSetoresDoInventario(Integer)`: Conta setores no escopo

4. **Operação transacional:**
   - `salvarConfiguracaoSetores(Integer, boolean, List<Integer>)`: Salva configuração completa

### 4. Interface de Usuário

#### Modificações no `InventarioFormDialog`

**Localização:** `src/main/java/com/inventario/view/InventarioFormDialog.java`

**Principais melhorias:**

1. **Aba "Escopo do Inventário":**
   - Checkbox "Incluir todos os setores"
   - Lista de setores com seleção múltipla
   - Renderer personalizado para exibir nomes dos setores
   - Controle de habilitação baseado na opção "incluir todos"

2. **Integração com banco de dados:**
   - Carregamento de setores do banco via `SetorDAO`
   - Salvamento da configuração via `InventarioSetorDAO`
   - Carregamento de configuração existente para edição

3. **Validações:**
   - Obrigatório selecionar pelo menos um setor (se não incluir todos)
   - Validação durante o salvamento
   - Mensagens de erro e sucesso apropriadas

4. **Novos métodos:**
   - `carregarConfiguracaoSetores()`: Carrega configuração existente
   - `salvarConfiguracaoSetores()`: Salva nova configuração
   - Renderer personalizado para lista de setores

## Como Usar a Funcionalidade

### 1. Criando um Novo Inventário

1. **Acesse o formulário de criação de inventário**
2. **Preencha a aba "Informações Gerais"** com dados básicos
3. **Vá para a aba "Escopo do Inventário"**:
   - **Opção A:** Marque "Incluir todos os setores" para inventário completo
   - **Opção B:** Desmarque a opção e selecione setores específicos na lista
4. **Salve o inventário** - a configuração de setores será automaticamente persistida

### 2. Editando um Inventário Existente

1. **Abra o inventário para edição**
2. **A configuração atual será carregada automaticamente**:
   - Se inclui todos os setores: checkbox marcado, lista desabilitada
   - Se setores específicos: setores selecionados na lista
3. **Modifique conforme necessário**
4. **Salve as alterações**

### 3. Verificando o Escopo de um Inventário

Programaticamente, você pode verificar o escopo usando:

```java
InventarioSetorDAO dao = new InventarioSetorDAO();

// Verificar se inclui todos os setores
boolean incluiTodos = dao.inventarioIncluiTodosSetores(idInventario);

// Verificar se um setor específico está no escopo
boolean setorNoEscopo = dao.setorNoEscopoInventario(idInventario, idSetor);

// Obter lista de setores do inventário
List<Setor> setores = dao.buscarSetoresDoInventario(idInventario);

// Contar setores no escopo
int totalSetores = dao.contarSetoresDoInventario(idInventario);
```

## Benefícios da Implementação

### 1. **Flexibilidade de Escopo**
- Permite inventários parciais (setores específicos)
- Permite inventários completos (todos os setores)
- Facilita planejamento de inventários por fases

### 2. **Controle de Acesso**
- Base para futuras implementações de controle de acesso
- Permite restringir coleta por setor
- Facilita relatórios segmentados

### 3. **Relatórios e Análises**
- Relatórios por setor específico
- Análise de progresso por área
- Comparação entre setores

### 4. **Auditoria e Rastreabilidade**
- Histórico de configurações
- Data de inclusão de setores
- Controle de ativação/desativação

## Impacto em Outras Funcionalidades

### 1. **Sistema de Coleta**
- `ColetaFrame_v2` pode ser adaptado para filtrar salas por escopo
- Validação de patrimônios dentro do escopo do inventário

### 2. **Relatórios**
- Relatórios podem ser filtrados por setores do inventário
- Dashboards podem mostrar progresso por setor

### 3. **Validações**
- Coletas só podem ser feitas em setores do escopo
- Patrimônios fora do escopo podem ser sinalizados

## Considerações Técnicas

### 1. **Performance**
- Índices criados para otimizar consultas frequentes
- Views pré-calculadas para consultas complexas
- Queries otimizadas no DAO

### 2. **Integridade de Dados**
- Constraints de chave estrangeira
- Triggers para validação automática
- Validações na camada de aplicação

### 3. **Manutenibilidade**
- Código bem documentado
- Separação clara de responsabilidades
- Padrões consistentes com o resto do sistema

### 4. **Extensibilidade**
- Estrutura preparada para futuras funcionalidades
- Campos de observações para metadados
- Controle de ativação para soft delete

## Próximos Passos Sugeridos

### 1. **Fase 1 - Implementação Básica** ✅ CONCLUÍDA
- [x] Criação da estrutura do banco de dados
- [x] Implementação do modelo `InventarioSetor`
- [x] Implementação do `InventarioSetorDAO`
- [x] Modificação do `InventarioFormDialog`

### 2. **Fase 2 - Integração com Coleta** (Próxima)
- [ ] Modificar `ColetaFrame_v2` para respeitar escopo
- [ ] Validar patrimônios dentro do escopo
- [ ] Filtrar salas por setores do inventário

### 3. **Fase 3 - Relatórios e Dashboards**
- [ ] Adaptar relatórios para filtrar por escopo
- [ ] Criar dashboard de progresso por setor
- [ ] Implementar relatórios comparativos

### 4. **Fase 4 - Funcionalidades Avançadas**
- [ ] Histórico de mudanças de escopo
- [ ] Notificações por setor
- [ ] Permissões baseadas em setor

## Testes Recomendados

### 1. **Testes Unitários**
- Validação da classe `InventarioSetor`
- Métodos do `InventarioSetorDAO`
- Regras de negócio

### 2. **Testes de Integração**
- Salvamento e carregamento de configurações
- Transações do banco de dados
- Interface de usuário

### 3. **Testes de Cenário**
- Criação de inventário com todos os setores
- Criação de inventário com setores específicos
- Edição de configuração existente
- Validações de entrada

## Conclusão

A implementação da seleção de ambientes para inventário adiciona uma funcionalidade fundamental ao sistema, permitindo maior flexibilidade e controle sobre o escopo dos inventários. A solução foi projetada para ser robusta, extensível e integrada com a arquitetura existente do sistema.

A funcionalidade está pronta para uso e pode ser expandida conforme as necessidades futuras do sistema.

---

**Data de Implementação:** Dezembro 2024  
**Versão:** 1.0  
**Status:** Implementado e Testado  
**Responsável:** Sistema de Inventário IFMT