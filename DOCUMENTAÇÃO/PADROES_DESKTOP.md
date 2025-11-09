# Padrões de Projeto - Aplicação Desktop

## 🎯 Problemas Identificados

### ❌ Problemas Atuais

1. **Instanciação Manual de DAOs**
```java
public ColetaService() {
    this.coletaDAO = new ColetaDAO();  // ❌ Acoplamento forte
    this.salaInventarioDAO = new SalaInventarioDAO();
}
```

2. **Mistura de Anotações Spring com Instanciação Manual**
```java
@Service  // ✓ Usa Spring
public class PatrimonioService {
    @Autowired
    private PatrimonioDAORefactored patrimonioDAO;  // ✓ Injeção
}

// Mas em outros lugares:
public class ColetaService {
    private ColetaDAO coletaDAO = new ColetaDAO();  // ❌ Manual
}
```

3. **Falta de Abstração**
- DAOs acoplados diretamente aos Services
- Difícil testar
- Difícil trocar implementações

4. **Código Duplicado**
- Tratamento de exceções repetido
- Validações repetidas
- Logs repetidos

---

## 🏗️ Padrões Recomendados

### 1. Dependency Injection (Injeção de Dependência)

**Problema**: Instanciação manual cria acoplamento forte.

**Solução**: Usar Spring DI consistentemente.

#### ❌ ANTES
```java
public class ColetaService {
    private ColetaDAO coletaDAO;
    
    public ColetaService() {
        this.coletaDAO = new ColetaDAO();  // Acoplamento
    }
}
```

#### ✅ DEPOIS
```java
@Service
public class ColetaService {
    
    private final ColetaDAO coletaDAO;
    
    @Autowired
    public ColetaService(ColetaDAO coletaDAO) {
        this.coletaDAO = coletaDAO;  // Injetado
    }
}
```

**Benefícios**:
- ✅ Desacoplamento
- ✅ Testável (pode injetar mocks)
- ✅ Gerenciado pelo Spring
- ✅ Singleton automático

---

### 2. Repository Pattern (Padrão Repositório)

**Problema**: DAOs expõem detalhes de implementação SQL.

**Solução**: Criar camada de Repository.

#### Estrutura
```
Service → Repository (interface) → DAO (implementação)
```

#### Implementação

**Interface Repository**:
```java
public interface PatrimonioRepository {
    Optional<Patrimonio> findById(Integer id);
    List<Patrimonio> findAll();
    List<Patrimonio> findByNumero(String numero);
    void save(Patrimonio patrimonio);
    void delete(Integer id);
}
```

**Implementação com DAO**:
```java
@Repository
public class PatrimonioRepositoryImpl implements PatrimonioRepository {
    
    private final PatrimonioDAO patrimonioDAO;
    
    @Autowired
    public PatrimonioRepositoryImpl(PatrimonioDAO patrimonioDAO) {
        this.patrimonioDAO = patrimonioDAO;
    }
    
    @Override
    public Optional<Patrimonio> findById(Integer id) {
        try {
            Patrimonio patrimonio = patrimonioDAO.findById(id);
            return Optional.ofNullable(patrimonio);
        } catch (SQLException e) {
            logger.error("Erro ao buscar patrimônio", e);
            return Optional.empty();
        }
    }
    
    @Override
    public List<Patrimonio> findAll() {
        try {
            return patrimonioDAO.findAll();
        } catch (SQLException e) {
            logger.error("Erro ao listar patrimônios", e);
            return Collections.emptyList();
        }
    }
}
```

**Service usando Repository**:
```java
@Service
public class PatrimonioService {
    
    private final PatrimonioRepository repository;
    
    @Autowired
    public PatrimonioService(PatrimonioRepository repository) {
        this.repository = repository;
    }
    
    public Patrimonio buscarPorId(Integer id) {
        return repository.findById(id).orElse(null);
    }
}
```

**Benefícios**:
- ✅ Abstração de persistência
- ✅ Fácil trocar DAO por JPA
- ✅ Tratamento de exceções centralizado
- ✅ Testável com mocks

---

### 3. Builder Pattern (Padrão Construtor)

**Problema**: Objetos complexos com muitos parâmetros.

**Solução**: Builder para construção fluente.

#### ❌ ANTES
```java
Patrimonio patrimonio = new Patrimonio();
patrimonio.setNumero("12345");
patrimonio.setDescricao("Notebook");
patrimonio.setIdSala(10);
patrimonio.setEstado("BOM");
patrimonio.setValor(3000.0);
patrimonio.setDataAquisicao("2024-01-01");
```

#### ✅ DEPOIS
```java
Patrimonio patrimonio = Patrimonio.builder()
    .numero("12345")
    .descricao("Notebook")
    .idSala(10)
    .estado("BOM")
    .valor(3000.0)
    .dataAquisicao("2024-01-01")
    .build();
```

**Implementação**:
```java
public class Patrimonio {
    private Integer id;
    private String numero;
    private String descricao;
    // ... outros campos
    
    // Construtor privado
    private Patrimonio(Builder builder) {
        this.numero = builder.numero;
        this.descricao = builder.descricao;
        this.idSala = builder.idSala;
        // ...
    }
    
    // Builder estático
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String numero;
        private String descricao;
        private Integer idSala;
        // ... outros campos
        
        public Builder numero(String numero) {
            this.numero = numero;
            return this;
        }
        
        public Builder descricao(String descricao) {
            this.descricao = descricao;
            return this;
        }
        
        public Builder idSala(Integer idSala) {
            this.idSala = idSala;
            return this;
        }
        
        public Patrimonio build() {
            // Validações
            if (numero == null || numero.isEmpty()) {
                throw new IllegalArgumentException("Número é obrigatório");
            }
            return new Patrimonio(this);
        }
    }
}
```

**Benefícios**:
- ✅ Código mais legível
- ✅ Validações no build()
- ✅ Imutabilidade (opcional)
- ✅ Parâmetros opcionais claros

---

### 4. Factory Pattern (Padrão Fábrica)

**Problema**: Criação de objetos complexos espalhada.

**Solução**: Factory centralizada.

#### Implementação

**Factory para Relatórios**:
```java
@Component
public class RelatorioFactory {
    
    public enum TipoRelatorio {
        COLETA,
        PATRIMONIO,
        INVENTARIO,
        DASHBOARD
    }
    
    public Relatorio criarRelatorio(TipoRelatorio tipo, Map<String, Object> parametros) {
        return switch (tipo) {
            case COLETA -> new RelatorioColeta(parametros);
            case PATRIMONIO -> new RelatorioPatrimonio(parametros);
            case INVENTARIO -> new RelatorioInventario(parametros);
            case DASHBOARD -> new RelatorioDashboard(parametros);
        };
    }
}
```

**Uso**:
```java
@Service
public class RelatorioService {
    
    private final RelatorioFactory relatorioFactory;
    
    @Autowired
    public RelatorioService(RelatorioFactory relatorioFactory) {
        this.relatorioFactory = relatorioFactory;
    }
    
    public void gerarRelatorio(TipoRelatorio tipo, Map<String, Object> params) {
        Relatorio relatorio = relatorioFactory.criarRelatorio(tipo, params);
        relatorio.gerar();
    }
}
```

**Benefícios**:
- ✅ Criação centralizada
- ✅ Fácil adicionar novos tipos
- ✅ Validações centralizadas
- ✅ Reutilizável

---

### 5. Command Pattern (Padrão Comando)

**Problema**: Operações complexas misturadas com UI.

**Solução**: Encapsular operações em Commands.

#### Implementação

**Interface Command**:
```java
public interface Command<T> {
    T execute() throws Exception;
    void undo() throws Exception;
    String getDescription();
}
```

**Command para Salvar Patrimônio**:
```java
public class SalvarPatrimonioCommand implements Command<Patrimonio> {
    
    private final PatrimonioService service;
    private final Patrimonio patrimonio;
    private Patrimonio patrimonioAnterior;
    
    public SalvarPatrimonioCommand(PatrimonioService service, Patrimonio patrimonio) {
        this.service = service;
        this.patrimonio = patrimonio;
    }
    
    @Override
    public Patrimonio execute() throws Exception {
        // Salvar estado anterior para undo
        if (patrimonio.getId() != null) {
            patrimonioAnterior = service.buscarPorId(patrimonio.getId());
        }
        
        service.salvar(patrimonio);
        return patrimonio;
    }
    
    @Override
    public void undo() throws Exception {
        if (patrimonioAnterior != null) {
            service.salvar(patrimonioAnterior);
        } else {
            service.excluir(patrimonio.getId());
        }
    }
    
    @Override
    public String getDescription() {
        return "Salvar patrimônio: " + patrimonio.getNumero();
    }
}
```

**Executor de Commands**:
```java
@Component
public class CommandExecutor {
    
    private final Stack<Command<?>> executedCommands = new Stack<>();
    
    public <T> T execute(Command<T> command) throws Exception {
        T result = command.execute();
        executedCommands.push(command);
        return result;
    }
    
    public void undo() throws Exception {
        if (!executedCommands.isEmpty()) {
            Command<?> command = executedCommands.pop();
            command.undo();
        }
    }
}
```

**Uso no Frame**:
```java
public class PatrimonioFrame extends JFrame {
    
    @Autowired
    private CommandExecutor commandExecutor;
    
    @Autowired
    private PatrimonioService patrimonioService;
    
    private void salvarPatrimonio() {
        Patrimonio patrimonio = obterDadosDoFormulario();
        
        Command<Patrimonio> command = new SalvarPatrimonioCommand(
            patrimonioService, 
            patrimonio
        );
        
        try {
            commandExecutor.execute(command);
            JOptionPane.showMessageDialog(this, "Salvo com sucesso!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }
    
    private void desfazer() {
        try {
            commandExecutor.undo();
            JOptionPane.showMessageDialog(this, "Operação desfeita!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao desfazer: " + e.getMessage());
        }
    }
}
```

**Benefícios**:
- ✅ Operações encapsuladas
- ✅ Undo/Redo fácil
- ✅ Histórico de operações
- ✅ Testável

---

### 6. Observer Pattern (Padrão Observador)

**Problema**: Frames precisam ser notificados de mudanças.

**Solução**: Event Bus ou Listeners.

#### Implementação com Spring Events

**Evento**:
```java
public class PatrimonioSalvoEvent extends ApplicationEvent {
    
    private final Patrimonio patrimonio;
    
    public PatrimonioSalvoEvent(Object source, Patrimonio patrimonio) {
        super(source);
        this.patrimonio = patrimonio;
    }
    
    public Patrimonio getPatrimonio() {
        return patrimonio;
    }
}
```

**Publisher (Service)**:
```java
@Service
public class PatrimonioService {
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    public void salvar(Patrimonio patrimonio) throws SQLException {
        // Salvar no banco
        repository.save(patrimonio);
        
        // Publicar evento
        eventPublisher.publishEvent(
            new PatrimonioSalvoEvent(this, patrimonio)
        );
    }
}
```

**Listener (Frame)**:
```java
@Component
public class PatrimonioFrame extends JFrame {
    
    @EventListener
    public void onPatrimonioSalvo(PatrimonioSalvoEvent event) {
        SwingUtilities.invokeLater(() -> {
            // Atualizar tabela
            atualizarTabela();
            
            // Mostrar notificação
            JOptionPane.showMessageDialog(
                this,
                "Patrimônio " + event.getPatrimonio().getNumero() + " salvo!"
            );
        });
    }
}
```

**Benefícios**:
- ✅ Desacoplamento total
- ✅ Múltiplos listeners
- ✅ Assíncrono (opcional)
- ✅ Fácil adicionar novos listeners

---

### 7. Strategy Pattern para Validações

**Problema**: Validações espalhadas e duplicadas.

**Solução**: Strategy para diferentes validações.

#### Implementação

**Interface**:
```java
public interface ValidationStrategy<T> {
    ValidationResult validate(T object);
}

public class ValidationResult {
    private final boolean valid;
    private final List<String> errors;
    
    public static ValidationResult success() {
        return new ValidationResult(true, Collections.emptyList());
    }
    
    public static ValidationResult failure(String... errors) {
        return new ValidationResult(false, Arrays.asList(errors));
    }
}
```

**Estratégias**:
```java
@Component
public class PatrimonioNumeroValidation implements ValidationStrategy<Patrimonio> {
    
    @Override
    public ValidationResult validate(Patrimonio patrimonio) {
        if (patrimonio.getNumero() == null || patrimonio.getNumero().isEmpty()) {
            return ValidationResult.failure("Número é obrigatório");
        }
        
        if (!patrimonio.getNumero().matches("\\d+")) {
            return ValidationResult.failure("Número deve conter apenas dígitos");
        }
        
        return ValidationResult.success();
    }
}

@Component
public class PatrimonioValorValidation implements ValidationStrategy<Patrimonio> {
    
    @Override
    public ValidationResult validate(Patrimonio patrimonio) {
        if (patrimonio.getValor() != null && patrimonio.getValor() < 0) {
            return ValidationResult.failure("Valor não pode ser negativo");
        }
        
        return ValidationResult.success();
    }
}
```

**Validator Composto**:
```java
@Component
public class PatrimonioValidator {
    
    private final List<ValidationStrategy<Patrimonio>> strategies;
    
    @Autowired
    public PatrimonioValidator(List<ValidationStrategy<Patrimonio>> strategies) {
        this.strategies = strategies;
    }
    
    public ValidationResult validate(Patrimonio patrimonio) {
        List<String> allErrors = new ArrayList<>();
        
        for (ValidationStrategy<Patrimonio> strategy : strategies) {
            ValidationResult result = strategy.validate(patrimonio);
            if (!result.isValid()) {
                allErrors.addAll(result.getErrors());
            }
        }
        
        return allErrors.isEmpty() 
            ? ValidationResult.success() 
            : ValidationResult.failure(allErrors.toArray(new String[0]));
    }
}
```

**Uso**:
```java
@Service
public class PatrimonioService {
    
    @Autowired
    private PatrimonioValidator validator;
    
    public void salvar(Patrimonio patrimonio) throws ValidationException {
        ValidationResult result = validator.validate(patrimonio);
        
        if (!result.isValid()) {
            throw new ValidationException(result.getErrors());
        }
        
        repository.save(patrimonio);
    }
}
```

**Benefícios**:
- ✅ Validações reutilizáveis
- ✅ Fácil adicionar novas validações
- ✅ Testável isoladamente
- ✅ Composição flexível

---

## 📊 Arquitetura Proposta

```
┌─────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                    │
│                                                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │           Swing Frames/Dialogs                    │  │
│  │  - PatrimonioFrame                               │  │
│  │  - ColetaFrame                                   │  │
│  │  - RelatorioFrame                                │  │
│  └──────────────────┬───────────────────────────────┘  │
└────────────────────┼────────────────────────────────────┘
                     │ @Autowired
                     ▼
┌─────────────────────────────────────────────────────────┐
│                     SERVICE LAYER                        │
│                                                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │           @Service Classes                        │  │
│  │  - PatrimonioService                             │  │
│  │  - ColetaService                                 │  │
│  │  - RelatorioService                              │  │
│  └──────────────────┬───────────────────────────────┘  │
└────────────────────┼────────────────────────────────────┘
                     │ @Autowired
                     ▼
┌─────────────────────────────────────────────────────────┐
│                   REPOSITORY LAYER                       │
│                                                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │      @Repository Implementations                  │  │
│  │  - PatrimonioRepositoryImpl                      │  │
│  │  - ColetaRepositoryImpl                          │  │
│  └──────────────────┬───────────────────────────────┘  │
└────────────────────┼────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│                      DAO LAYER                           │
│                                                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │           DAO Classes                             │  │
│  │  - PatrimonioDAO                                 │  │
│  │  - ColetaDAO                                     │  │
│  └──────────────────┬───────────────────────────────┘  │
└────────────────────┼────────────────────────────────────┘
                     │
                     ▼
              ┌──────────────┐
              │  PostgreSQL  │
              └──────────────┘
```

---

## 🔧 Plano de Refatoração

### Fase 1: Dependency Injection (Prioridade ALTA)
1. ✅ Adicionar `@Component` em todos os DAOs
2. ✅ Remover `new DAO()` dos Services
3. ✅ Usar `@Autowired` constructor injection
4. ✅ Testar cada Service refatorado

### Fase 2: Repository Pattern (Prioridade ALTA)
1. Criar interfaces Repository
2. Implementar Repositories com DAOs
3. Atualizar Services para usar Repositories
4. Adicionar tratamento de exceções nos Repositories

### Fase 3: Builder Pattern (Prioridade MÉDIA)
1. Adicionar Builders em classes de modelo complexas
2. Refatorar código que cria objetos

### Fase 4: Command Pattern (Prioridade MÉDIA)
1. Criar Commands para operações principais
2. Implementar CommandExecutor
3. Adicionar Undo/Redo nos Frames

### Fase 5: Observer Pattern (Prioridade BAIXA)
1. Criar eventos para operações importantes
2. Adicionar listeners nos Frames
3. Remover acoplamento direto

### Fase 6: Strategy Pattern (Prioridade BAIXA)
1. Extrair validações para Strategies
2. Criar Validators compostos
3. Usar em Services

---

## 📚 Benefícios Esperados

| Aspecto | Antes | Depois |
|---------|-------|--------|
| **Acoplamento** | Alto (new DAO()) | Baixo (DI) |
| **Testabilidade** | Difícil | Fácil (mocks) |
| **Manutenção** | Complexa | Simples |
| **Reutilização** | Baixa | Alta |
| **Extensibilidade** | Difícil | Fácil |

---

**Versão**: 1.0.0  
**Data**: 09/11/2025  
**Autor**: Sistema de Inventário
