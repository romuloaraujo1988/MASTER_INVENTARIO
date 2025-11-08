# Plano de Refatoração - Arquitetura em Camadas

## 🎯 Objetivo
Eliminar completamente o acoplamento entre Views e DAOs, implementando uma arquitetura em camadas limpa e manutenível.

## 📊 Análise de Acoplamento Atual

### Views com Alto Acoplamento (Prioridade CRÍTICA)

#### 1. ColetaFrame_v2.java ⚠️⚠️⚠️
**Acoplamento**: MUITO ALTO (6 DAOs)
```java
- SalaDAORefactored
- PatrimonioDAORefactored
- ColetaDAO
- InventarioDAO
- SalaInventarioDAO
- ParticipanteInventarioDAO
```
**Impacto**: Frame principal de coleta, usado constantemente
**Prioridade**: 🔴 CRÍTICA

#### 2. RelatorioFrame.java ⚠️⚠️⚠️
**Acoplamento**: MUITO ALTO (6 DAOs)
```java
- RelatorioColetaDAO
- InventarioDAO
- ResponsavelDAORefactored
- SetorDAORefactored
- PatrimonioDAORefactored
- SalaDAORefactored
```
**Impacto**: Geração de relatórios críticos
**Prioridade**: 🔴 CRÍTICA

#### 3. DashboardColetaFrame.java ⚠️⚠️
**Acoplamento**: ALTO (2 DAOs)
```java
- DashboardColetaDAO
- InventarioDAO
```
**Impacto**: Dashboard principal do sistema
**Prioridade**: 🟡 ALTA

#### 4. InventarioFrame.java ⚠️⚠️
**Acoplamento**: MÉDIO (1 DAO)
```java
- InventarioDAO
```
**Impacto**: Gerenciamento de inventários
**Prioridade**: 🟡 ALTA

#### 5. CampusFrame.java ⚠️
**Acoplamento**: MÉDIO (1 DAO + 1 Service)
```java
- CampusDAO (ainda instanciado)
- CampusService (já usa)
```
**Impacto**: Gerenciamento de campus
**Prioridade**: 🟢 MÉDIA

#### 6. ResponsavelFormDialog.java ⚠️
**Acoplamento**: MÉDIO (2 DAOs)
```java
- ResponsavelDAORefactored
- SetorDAORefactored
```
**Prioridade**: 🟢 MÉDIA

#### 7. SetorFormDialog.java ⚠️
**Acoplamento**: BAIXO (1 DAO)
```java
- SetorDAORefactored
```
**Prioridade**: 🟢 MÉDIA

#### 8. SalaFormDialog.java ⚠️
**Acoplamento**: MÉDIO (2 DAOs)
```java
- SalaDAORefactored
- SetorDAORefactored
```
**Prioridade**: 🟢 MÉDIA

#### 9. UsuarioFormDialog.java ⚠️
**Acoplamento**: MÉDIO (2 DAOs)
```java
- UsuarioDAORefactored
- SetorDAORefactored
```
**Prioridade**: 🟢 MÉDIA

## 🏗️ Arquitetura Alvo

```
┌─────────────────────────────────────────────────────┐
│                   PRESENTATION LAYER                 │
│                      (Views/UI)                      │
│  - Frames, Dialogs, Panels                          │
│  - Apenas lógica de apresentação                    │
│  - Validação de entrada básica                      │
└──────────────────┬──────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────┐
│              APPLICATION LAYER (Facade)              │
│                  ServiceFactory                      │
│  - Singleton para acesso aos serviços               │
│  - Ponto único de entrada                           │
└──────────────────┬──────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────┐
│                  DOMAIN LAYER                        │
│                   (Services)                         │
│  - Lógica de negócio                                │
│  - Validações complexas                             │
│  - Orquestração de operações                        │
│  - Transações                                       │
└──────────────────┬──────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────┐
│              INFRASTRUCTURE LAYER                    │
│                     (DAOs)                           │
│  - Acesso ao banco de dados                         │
│  - Queries SQL                                      │
│  - Mapeamento objeto-relacional                     │
└─────────────────────────────────────────────────────┘
```

## 📋 Serviços Necessários

### ✅ Já Implementados
- [x] InventarioService
- [x] ColetaService
- [x] RelatorioService
- [x] PatrimonioService
- [x] UsuarioService
- [x] SalaService
- [x] SetorService
- [x] ResponsavelService
- [x] CampusService

### ⏳ Precisam Ser Criados
- [ ] DashboardService (para DashboardColetaFrame)
- [ ] SalaInventarioService (para ColetaFrame_v2)

### 🔄 Precisam Ser Expandidos
- [ ] ColetaService (adicionar métodos para ColetaFrame_v2)
- [ ] InventarioService (adicionar métodos para InventarioFrame)
- [ ] RelatorioService (adicionar métodos para RelatorioFrame)

## 🚀 Plano de Execução

### Fase 1: Criar Serviços Faltantes (1-2 dias)

#### 1.1 DashboardService
```java
@Service
public class DashboardService {
    private final DashboardColetaDAO dashboardDAO;
    private final InventarioDAO inventarioDAO;
    
    // Métodos:
    - obterEstatisticasGerais(int idInventario)
    - obterProgressoPorSetor(int idInventario)
    - obterItensColetados(int idInventario)
    - obterDivergencias(int idInventario)
    - obterInventarioAtivo()
}
```

#### 1.2 SalaInventarioService
```java
@Service
public class SalaInventarioService {
    private final SalaInventarioDAO salaInventarioDAO;
    
    // Métodos:
    - buscarSalasPorInventario(int idInventario)
    - finalizarColetaSala(int idSala, int idInventario, int idUsuario)
    - verificarSalaColetada(int idSala, int idInventario)
    - obterProgressoSala(int idSala, int idInventario)
}
```

### Fase 2: Expandir Serviços Existentes (2-3 dias)

#### 2.1 ColetaService - Adicionar Métodos
```java
// Para ColetaFrame_v2
- buscarColetasPorSala(int idSala, int idInventario)
- registrarColetaComValidacao(Coleta coleta)
- atualizarEstadoPatrimonio(int idPatrimonio, String estado)
- buscarUltimaColetaPatrimonio(int idPatrimonio)
- contarColetasPorSala(int idSala, int idInventario)
```

#### 2.2 InventarioService - Adicionar Métodos
```java
// Para InventarioFrame
- listarInventariosComFiltro(String filtro)
- finalizarInventario(int idInventario)
- calcularProgressoInventario(int idInventario)
- validarInventarioParaFinalizacao(int idInventario)
```

#### 2.3 RelatorioService - Adicionar Métodos
```java
// Para RelatorioFrame
- gerarRelatorioCompleto(int idInventario, Map<String, Object> filtros)
- gerarRelatorioPorSala(int idInventario, int idSala)
- exportarRelatorioExcel(int idInventario, String tipo)
- exportarRelatorioPDF(int idInventario, String tipo)
```

### Fase 3: Refatorar Views Críticas (5-7 dias)

#### 3.1 ColetaFrame_v2 (2 dias)
**Antes**:
```java
private SalaDAORefactored salaDAO;
private PatrimonioDAORefactored patrimonioDAO;
private ColetaDAO coletaDAO;
// ... 6 DAOs
```

**Depois**:
```java
private final SalaService salaService;
private final PatrimonioService patrimonioService;
private final ColetaService coletaService;
private final InventarioService inventarioService;
private final SalaInventarioService salaInventarioService;

public ColetaFrame_v2() {
    ServiceFactory factory = ServiceFactory.getInstance();
    this.salaService = factory.getSalaService();
    this.patrimonioService = factory.getPatrimonioService();
    this.coletaService = factory.getColetaService();
    this.inventarioService = factory.getInventarioService();
    this.salaInventarioService = factory.getSalaInventarioService();
}
```

#### 3.2 RelatorioFrame (2 dias)
**Antes**:
```java
private RelatorioColetaDAO relatorioDAO;
private InventarioDAO inventarioDAO;
// ... 6 DAOs
```

**Depois**:
```java
private final RelatorioService relatorioService;
private final InventarioService inventarioService;
private final ResponsavelService responsavelService;
private final SetorService setorService;
private final PatrimonioService patrimonioService;
private final SalaService salaService;

public RelatorioFrame() {
    ServiceFactory factory = ServiceFactory.getInstance();
    this.relatorioService = factory.getRelatorioService();
    // ... outros serviços
}
```

#### 3.3 DashboardColetaFrame (1 dia)
**Antes**:
```java
private DashboardColetaDAO dashboardDAO;
private InventarioDAO inventarioDAO;
```

**Depois**:
```java
private final DashboardService dashboardService;

public DashboardColetaFrame() {
    this.dashboardService = ServiceFactory.getInstance().getDashboardService();
}
```

#### 3.4 InventarioFrame (1 dia)
**Antes**:
```java
private InventarioDAO inventarioDAO;
```

**Depois**:
```java
private final InventarioService inventarioService;

public InventarioFrame() {
    this.inventarioService = ServiceFactory.getInstance().getInventarioService();
}
```

### Fase 4: Refatorar Views Médias (3-4 dias)

#### 4.1 CampusFrame (0.5 dia)
- Remover instanciação direta de CampusDAO
- Usar apenas CampusService

#### 4.2 ResponsavelFormDialog (0.5 dia)
- Usar ResponsavelService e SetorService

#### 4.3 SetorFormDialog (0.5 dia)
- Usar SetorService

#### 4.4 SalaFormDialog (0.5 dia)
- Usar SalaService e SetorService

#### 4.5 UsuarioFormDialog (0.5 dia)
- Usar UsuarioService e SetorService

### Fase 5: Validação e Testes (2-3 dias)

#### 5.1 Testes de Integração
- Testar cada view refatorada
- Verificar funcionalidades críticas
- Validar fluxos completos

#### 5.2 Testes de Regressão
- Executar casos de teste manuais
- Verificar relatórios
- Testar coleta de patrimônios
- Validar sincronização mobile

#### 5.3 Documentação
- Atualizar diagramas de arquitetura
- Documentar novos serviços
- Criar guia de migração

## 📊 Cronograma Estimado

| Fase | Atividade | Duração | Prioridade |
|------|-----------|---------|------------|
| 1 | Criar DashboardService | 0.5 dia | 🔴 |
| 1 | Criar SalaInventarioService | 0.5 dia | 🔴 |
| 2 | Expandir ColetaService | 1 dia | 🔴 |
| 2 | Expandir InventarioService | 0.5 dia | 🔴 |
| 2 | Expandir RelatorioService | 1 dia | 🔴 |
| 3 | Refatorar ColetaFrame_v2 | 2 dias | 🔴 |
| 3 | Refatorar RelatorioFrame | 2 dias | 🔴 |
| 3 | Refatorar DashboardColetaFrame | 1 dia | 🟡 |
| 3 | Refatorar InventarioFrame | 1 dia | 🟡 |
| 4 | Refatorar 5 dialogs | 2.5 dias | 🟢 |
| 5 | Testes e validação | 2 dias | 🔴 |
| 5 | Documentação | 1 dia | 🟡 |
| **TOTAL** | | **15 dias** | |

## 🎯 Benefícios Esperados

### Manutenibilidade
- ✅ Mudanças em DAOs não afetam Views
- ✅ Lógica de negócio centralizada
- ✅ Código mais limpo e organizado
- ✅ Facilita onboarding de novos desenvolvedores

### Testabilidade
- ✅ Serviços podem ser testados isoladamente
- ✅ Mocks mais fáceis de criar
- ✅ Testes de UI mais simples

### Escalabilidade
- ✅ Fácil adicionar novas funcionalidades
- ✅ Reutilização de código
- ✅ Menos duplicação

### Performance
- ✅ Cache pode ser implementado nos serviços
- ✅ Transações gerenciadas adequadamente
- ✅ Connection pooling otimizado

## 📈 Métricas de Sucesso

| Métrica | Antes | Meta | Como Medir |
|---------|-------|------|------------|
| Acoplamento Views-DAOs | 70% | 0% | Grep por "new.*DAO" |
| Linhas de código duplicadas | ~500 | <100 | SonarQube |
| Complexidade ciclomática média | 15 | <10 | SonarQube |
| Cobertura de testes | 5% | 40% | JaCoCo |
| Tempo de build | 45s | <30s | Maven |

## 🚨 Riscos e Mitigações

### Risco 1: Quebrar funcionalidades existentes
**Mitigação**: 
- Testes de regressão completos
- Refatoração incremental
- Manter DAOs funcionando em paralelo

### Risco 2: Aumento temporário de complexidade
**Mitigação**:
- Documentação clara
- Code reviews
- Pair programming em partes críticas

### Risco 3: Resistência da equipe
**Mitigação**:
- Demonstrar benefícios com exemplos
- Treinamento sobre nova arquitetura
- Documentação acessível

## 📝 Checklist de Implementação

### Para Cada View Refatorada:
- [ ] Identificar todos os DAOs usados
- [ ] Verificar se serviços existem
- [ ] Criar/expandir serviços necessários
- [ ] Atualizar ServiceFactory
- [ ] Refatorar construtor da view
- [ ] Substituir chamadas de DAO por serviço
- [ ] Remover imports de DAOs
- [ ] Testar funcionalidade
- [ ] Verificar diagnostics
- [ ] Commit com mensagem descritiva

### Para Cada Serviço Criado:
- [ ] Definir interface clara
- [ ] Implementar validações
- [ ] Adicionar logging
- [ ] Tratar exceções adequadamente
- [ ] Documentar métodos (Javadoc)
- [ ] Adicionar ao ServiceFactory
- [ ] Criar testes unitários (futuro)

## 🎓 Conclusão

Esta refatoração é **CRÍTICA** para a manutenibilidade do sistema. O investimento de ~15 dias resultará em:

- **-70% de acoplamento** (de 70% para 0%)
- **+50% de manutenibilidade** (de 7/10 para 10/10)
- **+100% de testabilidade** (de 5% para 40%+)
- **Base sólida** para futuras melhorias

**Recomendação**: Iniciar imediatamente, priorizando views críticas (ColetaFrame_v2 e RelatorioFrame).
