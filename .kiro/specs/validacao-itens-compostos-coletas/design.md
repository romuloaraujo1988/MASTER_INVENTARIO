# Validação de Itens Compostos vs. Coletas Normais - Design

## 1. Arquitetura da Solução

### 1.1 Visão Geral

Sistema de validação em 4 camadas:
1. **Validação de Dados** - Verifica se patrimônio é composto antes de coleta
2. **Detecção de Incorreções** - Identifica itens compostos coletados normalmente
3. **Correção de Dados** - Converte coletas normais em compostas
4. **Alertas e Monitoramento** - Notifica sobre itens pendentes

### 1.2 Fluxo de Dados

```
Coleta Normal → Validação → [É composto?] → Erro/Redirecionamento
                                    ↓
                              Coleta Composta
                                    ↓
                              Tabela Coleta + Componentes
```

---

## 2. Metodologias de Validação

### 2.1 Validação de Coleta Normal

**Arquivo:** `ColetaService.java`

**Método:** `registrarColeta(Coleta coleta)`

**Lógica:**
```java
public Result<Coleta> registrarColeta(Coleta coleta) {
    // 1. Buscar patrimônio
    Patrimonio patrimonio = patrimonioDAO.findById(coleta.getIdPatrimonio());
    
    // 2. VALIDAÇÃO CRÍTICA: Verificar se é composto
    List<ItemComposto> componentes = itemCompostoDAO.findByPatrimonioPrincipal(patrimonio.getId());
    
    if (!componentes.isEmpty()) {
        // Patrimônio é composto - NÃO permitir coleta normal
        return Result.failure(new IllegalArgumentException(
            "Este patrimônio é composto e deve ser coletado através do fluxo de coleta composta."
        ));
    }
    
    // 3. Prosseguir com coleta normal
    // ...
}
```

### 2.2 Detecção de Itens Compostos Coletados Normalmente

**Arquivo:** `RelatorioColetaDAO.java`

**Método:** `gerarRelatorioItensCompostosColetadosNormalmente(int idInventario)`

**Query SQL:**
```sql
SELECT 
    p.id as patrimonio_id,
    p.numero as numero_patrimonio,
    p.descricao,
    c.id as coleta_id,
    c.data_coleta,
    c.metodo_coleta,
    c.localizacao_encontrada,
    c.estado_encontrado,
    COUNT(ic.id) as total_componentes,
    COUNT(cc.id) as componentes_coletados
FROM tabela_patrimonio p
INNER JOIN tabela_coleta c ON p.id = c.id_patrimonio
INNER JOIN tabela_item_composto ic ON p.id = ic.id_patrimonio_principal
LEFT JOIN tabela_coleta_componente cc ON ic.id = cc.id_item_composto AND cc.id_inventario = c.id_inventario
WHERE c.id_inventario = ?
GROUP BY p.id, p.numero, p.descricao, c.id, c.data_coleta, c.metodo_coleta, c.localizacao_encontrada, c.estado_encontrado
HAVING COUNT(ic.id) > 0
ORDER BY p.numero;
```

### 2.3 Correção de Coletas Incorretas

**Arquivo:** `CorrecaoColetaService.java` (novo)

**Método:** `converterParaColetaComposta(int coletaId, Map<Integer, Integer> componentesEncontrados)`

**Lógica:**
```java
public Result<CorrecaoResultado> converterParaColetaComposta(int coletaId, Map<Integer, Integer> componentesEncontrados) {
    // 1. Buscar coleta
    Coleta coleta = coletaDAO.findById(coletaId);
    
    // 2. Verificar se já existe coleta composta
    List<ColetaComponente> coletasCompostasExistentes = coletaComponenteDAO.findByPatrimonioId(coleta.getIdPatrimonio(), coleta.getIdInventario());
    
    if (!coletasCompostasExistentes.isEmpty()) {
        // Caso 2: Coleta duplicada - remover a normal e manter a composta
        coletaDAO.delete(coletaId);
        return Result.success(new CorrecaoResultado(true, "Coleta duplicada removida", true));
    }
    
    // 3. Caso 1: Converter coleta normal em composta
    List<ItemComposto> itensComposto = itemCompostoDAO.findByPatrimonioPrincipal(coleta.getIdPatrimonio());
    
    // 4. Iniciar transação
    try {
        // 5. Criar registros de coleta de componentes
        for (ItemComposto item : itensComposto) {
            int quantidadeEncontrada = componentesEncontrados.getOrDefault(item.getId(), 0);
            
            ColetaComponente cc = new ColetaComponente();
            cc.setIdItemComposto(item.getId());
            cc.setIdInventario(coleta.getIdInventario());
            cc.setIdColetor(coleta.getIdColetor());
            cc.setQuantidadeEncontrada(quantidadeEncontrada);
            cc.setDataColeta(coleta.getDataColeta());
            cc.setLocalizacaoEncontrada(coleta.getLocalizacaoEncontrada());
            
            coletaComponenteDAO.insert(cc);
        }
        
        // 6. Remover coleta normal
        coletaDAO.delete(coletaId);
        
        // 7. Registrar correção
        Correcao correcao = new Correcao();
        correcao.setIdColeta(coletaId);
        correcao.setDataCorrecao(new Date());
        correcao.setIdUsuarioCorretor(coleta.getIdColetor());
        correcao.setObservacao("Correção: coleta normal convertida para composta");
        
        correcaoDAO.insert(correcao);
        
        // 6. Commit
        return Result.success(new CorrecaoResultado(true, "Coleta convertida com sucesso"));
        
    } catch (Exception e) {
        // Rollback
        return Result.failure(e);
    }
}
```

### 2.4 Alerta de Itens Compostos Pendentes

**Arquivo:** `DashboardService.java`

**Método:** `obterAlertasItensCompostos(int idInventario)`

**Query SQL:**
```sql
SELECT 
    p.id as patrimonio_id,
    p.numero as numero_patrimonio,
    p.descricao,
    COUNT(ic.id) as total_componentes,
    COUNT(cc.id) as componentes_coletados,
    COUNT(ic.id) - COUNT(cc.id) as componentes_pendentes
FROM tabela_patrimonio p
INNER JOIN tabela_item_composto ic ON p.id = ic.id_patrimonio_principal
LEFT JOIN tabela_coleta_componente cc ON ic.id = cc.id_item_composto AND cc.id_inventario = ?
WHERE p.id NOT IN (
    SELECT id_patrimonio FROM tabela_coleta WHERE id_inventario = ?
)
GROUP BY p.id, p.numero, p.descricao
HAVING COUNT(ic.id) > 0
ORDER BY componentes_pendentes DESC;
```

---

## 3. Estrutura de Classes

### 3.1 Novas Classes

#### `CorrecaoColetaService.java`
```java
@Service
public class CorrecaoColetaService {
    @Autowired
    private ColetaDAO coletaDAO;
    
    @Autowired
    private ItemCompostoDAO itemCompostoDAO;
    
    @Autowired
    private ColetaComponenteDAO coletaComponenteDAO;
    
    @Autowired
    private CorrecaoDAO correcaoDAO;
    
    public Result<CorrecaoResultado> converterParaColetaComposta(int coletaId, Map<Integer, Integer> componentesEncontrados) {
        // Implementação
    }
    
    public List<ColetaCompostaPendente> obterColetasCompostasPendentes(int idInventario) {
        // Implementação
    }
}
```

#### `Correcao.java` (Entity)
```java
@Entity
@Table(name = "tabela_correcao")
public class Correcao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "id_coleta")
    private Integer idColeta;
    
    @Column(name = "data_correcao")
    private Date dataCorrecao;
    
    @Column(name = "id_usuario_corretor")
    private Integer idUsuarioCorretor;
    
    @Column(name = "observacao")
    private String observacao;
    
    // Getters e Setters
}
```

#### `ColetaCompostaPendente.java` (DTO)
```java
public class ColetaCompostaPendente {
    private Integer patrimonioId;
    private String numeroPatrimonio;
    private String descricao;
    private Integer totalComponentes;
    private Integer componentesColetados;
    private Integer componentesPendentes;
    
    // Getters e Setters
}
```

### 3.2 Classes Modificadas

#### `ColetaService.java`
```java
@Service
public class ColetaService {
    
    public Result<Coleta> registrarColeta(Coleta coleta) {
        // ... código existente ...
        
        // NOVA VALIDAÇÃO
        List<ItemComposto> itensComposto = itemCompostoDAO.findByPatrimonioPrincipal(coleta.getIdPatrimonio());
        if (!itensComposto.isEmpty()) {
            return Result.failure(new IllegalArgumentException(
                "Este patrimônio é composto e deve ser coletado através do fluxo de coleta composta."
            ));
        }
        
        // ... restante do código ...
    }
}
```

#### `MobileColetaController.java`
```java
@PostMapping("/")
@RequireColetor
public ResponseEntity<?> registrarColeta(@RequestBody MobileColetaRequest request) {
    // ... código existente ...
    
    // NOVA VALIDAÇÃO
    List<ItemComposto> itensComposto = itemCompostoDAO.findByPatrimonioPrincipal(coleta.getIdPatrimonio());
    if (!itensComposto.isEmpty()) {
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "message", "Este patrimônio é composto. Use o endpoint /composto para registrar coleta composta."
        ));
    }
    
    // ... restante do código ...
}
```

---

## 4. Endpoints REST

### 4.1 Novos Endpoints

#### `GET /api/mobile/coletas/compostos-coletados-normalmente`
**Descrição:** Retorna todos os itens compostos que foram coletados como coletas normais

**Resposta:**
```json
{
  "success": true,
  "data": [
    {
      "patrimonioId": 417,
      "numeroPatrimonio": "303752",
      "descricao": "CONJUNTO ESCOLAR...",
      "coletaId": 6946,
      "dataColeta": "2025-12-09T14:20:39.866Z",
      "localizacaoEncontrada": "LAB. DE MAKE 2",
      "totalComponentes": 2,
      "componentesColetados": 0
    }
  ]
}
```

#### `POST /api/mobile/coletas/corrigir-composta`
**Descrição:** Converte uma coleta normal em coleta composta

**Request:**
```json
{
  "coletaId": 6946,
  "componentesEncontrados": {
    "123": 2,
    "124": 1
  }
}
```

**Resposta:**
```json
{
  "success": true,
  "message": "Coleta convertida com sucesso",
  "correcaoId": 1
}
```

#### `GET /api/mobile/coletas/compostos-pendentes`
**Descrição:** Retorna itens compostos com componentes pendentes

**Resposta:**
```json
{
  "success": true,
  "data": [
    {
      "patrimonioId": 1234,
      "numeroPatrimonio": "360128",
      "descricao": "CONJUNTO ESCOLAR...",
      "totalComponentes": 2,
      "componentesColetados": 1,
      "componentesPendentes": 1
    }
  ]
}
```

---

## 5. Validação no Frontend

### 5.1 ColetaFrame.java

**Método:** `btnColetarActionPerformed()`

**Lógica:**
```java
private void btnColetarActionPerformed() {
    // ... código existente ...
    
    // NOVA VALIDAÇÃO
    Patrimonio patrimonio = (Patrimonio) comboPatrimonios.getSelectedItem();
    
    List<ItemComposto> itensComposto = itemCompostoDAO.findByPatrimonioPrincipal(patrimonio.getId());
    if (!itensComposto.isEmpty()) {
        JOptionPane.showMessageDialog(
            this,
            "Este patrimônio é composto e deve ser coletado através do fluxo de coleta composta.\n" +
            "Por favor, selecione o item composto na lista ou use a funcionalidade de coleta composta.",
            "Patrimônio Composto",
            JOptionPane.WARNING_MESSAGE
        );
        return;
    }
    
    // ... restante do código ...
}
```

### 5.2 Mobile API - Validação

**Arquivo:** `MobileColetaController.java`

**Endpoint:** `POST /api/mobile/coletas`

**Validação:**
```java
@PostMapping("/")
@RequireColetor
public ResponseEntity<?> registrarColeta(@RequestBody MobileColetaRequest request) {
    // ... código existente ...
    
    // NOVA VALIDAÇÃO
    List<ItemComposto> itensComposto = itemCompostoDAO.findByPatrimonioPrincipal(coleta.getIdPatrimonio());
    if (!itensComposto.isEmpty()) {
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "message", "Este patrimônio é composto. Use o endpoint /composto para registrar coleta composta.",
            "itensComposto", itensComposto.stream()
                .map(ic -> Map.of("id", ic.getId(), "descricao", ic.getDescricaoComponente()))
                .collect(Collectors.toList())
        ));
    }
    
    // ... restante do código ...
}
```

---

## 6. Testes

### 6.1 Teste de Validação

```java
@Test
void registrarColeta_PatrimonioComposto_DeveRetornarErro() {
    // Given
    Coleta coleta = new Coleta();
    coleta.setIdPatrimonio(417); // Patrimônio composto
    
    // When
    Result<Coleta> resultado = coletaService.registrarColeta(coleta);
    
    // Then
    assertFalse(resultado.isSuccess());
    assertTrue(resultado.getFailure().getMessage().contains("composto"));
}
```

### 6.2 Teste de Correção

```java
@Test
void converterParaColetaComposta_DeveCriarRegistros() {
    // Given
    Map<Integer, Integer> componentes = Map.of(123, 2, 124, 1);
    
    // When
    Result<CorrecaoResultado> resultado = correcaoService.converterParaColetaComposta(6946, componentes);
    
    // Then
    assertTrue(resultado.isSuccess());
    assertNotNull(resultado.getSuccess().getCorrecaoId());
    
    // Verificar que registros foram criados
    List<ColetaComponente> componentesCriados = coletaComponenteDAO.findByColetaId(6946);
    assertEquals(2, componentesCriados.size());
}
```

---

## 7. Cronograma

### Fase 1: Detecção e Relatório (2 dias)
- [ ] Criar query SQL de detecção
- [ ] Implementar método no DAO
- [ ] Criar endpoint REST
- [ ] Testar com dados reais

### Fase 2: Prevenção (2 dias)
- [ ] Adicionar validação no ColetaService
- [ ] Adicionar validação no MobileColetaController
- [ ] Adicionar validação no ColetaFrame
- [ ] Testar fluxo completo

### Fase 3: Correção de Dados (3 dias)
- [ ] Criar CorrecaoColetaService
- [ ] Criar entity Correcao
- [ ] Criar endpoint de correção
- [ ] Criar interface de revisão
- [ ] Testar correção com dados reais

### Fase 4: Alertas e Dashboard (2 dias)
- [ ] Criar endpoint de alertas
- [ ] Adicionar alertas visuais
- [ ] Criar dashboard
- [ ] Testar notificações

**Total:** 9 dias úteis

---

## 8. Validação e Qualidade

### 8.1 Checklist de Validação

**Validação de Coleta:**
- [ ] Sistema rejeita coleta normal de itens compostos
- [ ] Mensagem de erro é clara e informativa
- [ ] Redirecionamento para coleta composta funciona

**Correção de Dados:**
- [ ] Conversão mantém histórico
- [ ] Transação é atômica
- [ ] Dados são consistentes após correção

**Alertas:**
- [ ] Alertas são exibidos em tempo real
- [ ] Informações são precisas
- [ ] Performance não é impactada

---

**Versão:** 1.0.0  
**Data:** 11/02/2026  
**Status:** ✅ Design Completo
