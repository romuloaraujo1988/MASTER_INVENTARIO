# Refatoração: Desacoplamento entre Views e DAOs

## Objetivo
Reduzir o acoplamento alto entre a camada de apresentação (Views) e a camada de acesso a dados (DAOs), introduzindo a camada de serviço como intermediária.

## Mudanças Realizadas

### 1. Novos Serviços Criados

#### InventarioService
- **Localização**: `src/main/java/com/inventario/service/InventarioService.java`
- **Responsabilidades**:
  - Gerenciar operações de inventário (CRUD)
  - Configurar setores do inventário
  - Configurar participantes do inventário
  - Validar regras de negócio

#### ColetaService
- **Localização**: `src/main/java/com/inventario/service/ColetaService.java`
- **Responsabilidades**:
  - Gerenciar coletas de patrimônio
  - Finalizar coleta de salas
  - Validar dados de coleta

#### RelatorioService
- **Localização**: `src/main/java/com/inventario/service/RelatorioService.java`
- **Responsabilidades**:
  - Gerar relatórios de coleta
  - Gerar relatórios de divergências
  - Gerar relatórios por setor e responsável

### 2. ServiceFactory Atualizado

**Arquivo**: `src/main/java/com/inventario/service/ServiceFactory.java`

Adicionados novos métodos:
- `getInventarioService()`
- `getColetaService()`
- `getRelatorioService()`

### 3. Views Refatoradas

#### PatrimonioFormDialog
**Antes**:
```java
SalaDAORefactored salaDAO = new SalaDAORefactored();
ResponsavelDAORefactored responsavelDAO = new ResponsavelDAORefactored();
PatrimonioDAORefactored patrimonioDAO = new PatrimonioDAORefactored();
```

**Depois**:
```java
SalaService salaService = ServiceFactory.getInstance().getSalaService();
ResponsavelService responsavelService = ServiceFactory.getInstance().getResponsavelService();
PatrimonioService patrimonioService = ServiceFactory.getInstance().getPatrimonioService();
```

#### InventarioFormDialog
**Antes**:
```java
ResponsavelDAORefactored responsavelDAO = new ResponsavelDAORefactored();
SetorDAORefactored setorDAO = new SetorDAORefactored();
InventarioDAO inventarioDAO = new InventarioDAO();
InventarioSetorDAO inventarioSetorDAO = new InventarioSetorDAO();
UsuarioDAORefactored usuarioDAO = new UsuarioDAORefactored();
ParticipanteInventarioDAO participanteDAO = new ParticipanteInventarioDAO();
```

**Depois**:
```java
ResponsavelService responsavelService = ServiceFactory.getInstance().getResponsavelService();
SetorService setorService = ServiceFactory.getInstance().getSetorService();
InventarioService inventarioService = ServiceFactory.getInstance().getInventarioService();
UsuarioService usuarioService = ServiceFactory.getInstance().getUsuarioService();
```

#### AlterarSenhaDialog
**Antes**:
```java
public AlterarSenhaDialog(Frame parent, Integer idUsuario, String nomeUsuario, UsuarioDAORefactored usuarioDAO)
```

**Depois**:
```java
public AlterarSenhaDialog(Frame parent, Integer idUsuario, String nomeUsuario)
// Usa ServiceFactory.getInstance().getUsuarioService() internamente
```

### 4. Serviços Atualizados

#### UsuarioService
Adicionados métodos:
- `buscarPorId(Integer id)` - sobrecarga para aceitar Integer
- `atualizarSenha(Integer idUsuario, String senhaHash)` - atualizar senha com hash pronto

#### PatrimonioService
Ajustado método:
- `salvar(Patrimonio patrimonio)` - agora lança SQLException e usa métodos corretos do DAO

## Benefícios da Refatoração

### 1. Redução de Acoplamento
- Views não conhecem mais os DAOs diretamente
- Mudanças nos DAOs não afetam as Views
- Facilita testes unitários das Views

### 2. Centralização da Lógica de Negócio
- Validações centralizadas nos serviços
- Regras de negócio em um único lugar
- Reutilização de código

### 3. Melhor Manutenibilidade
- Código mais organizado e legível
- Responsabilidades bem definidas
- Facilita adição de novas funcionalidades

### 4. Testabilidade
- Serviços podem ser testados independentemente
- Mocks mais fáceis de criar
- Testes de integração mais simples

## Arquitetura Resultante

```
┌─────────────────┐
│     Views       │  (Swing UI)
│  (Presentation) │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  ServiceFactory │  (Singleton)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│    Services     │  (Business Logic)
│  - Inventario   │
│  - Coleta       │
│  - Patrimonio   │
│  - Usuario      │
│  - Sala         │
│  - Setor        │
│  - Responsavel  │
│  - Relatorio    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│      DAOs       │  (Data Access)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│    Database     │  (PostgreSQL)
└─────────────────┘
```

## Próximos Passos

### Views Pendentes de Refatoração
1. `ColetaFrame_v2.java` - usa múltiplos DAOs diretamente
2. `DashboardColetaFrame.java` - usa DAOs diretamente
3. `InventarioFrame.java` - usa InventarioDAO diretamente
4. `RelatorioFrame.java` - usa múltiplos DAOs diretamente
5. `CampusFrame.java` - usa CampusDAO diretamente
6. Outros frames de CRUD (Sala, Setor, Responsavel, Usuario)

### Melhorias Adicionais
1. Implementar tratamento de exceções consistente
2. Adicionar logging estruturado
3. Implementar cache em serviços quando apropriado
4. Adicionar validações mais robustas
5. Implementar transações em operações complexas
6. Adicionar testes unitários para os serviços

## Notas Técnicas

### Compatibilidade
- Mantida compatibilidade com código existente
- DAOs ainda podem ser usados diretamente onde necessário
- Migração gradual possível

### Performance
- ServiceFactory usa Singleton para evitar múltiplas instâncias
- Serviços são stateless e thread-safe
- Sem impacto negativo na performance

### Padrões Utilizados
- **Service Layer Pattern**: Camada de serviço entre apresentação e dados
- **Singleton Pattern**: ServiceFactory para instância única
- **Dependency Injection**: Serviços recebem DAOs via construtor
- **Business Exception**: Exceções específicas para regras de negócio
