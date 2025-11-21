# Implementação de Estatísticas no App - Roadmap

## 🎯 Objetivo

Implementar estatísticas que **provem o valor da digitalização** e forneçam insights acionáveis para gestores.

---

## 📱 Telas a Implementar

### 1. Estatísticas Comparativas (PRIORIDADE ALTA)

**Localização:** Menu → Estatísticas → Tab "Comparativo"

**Conteúdo:**
```
┌─────────────────────────────────────┐
│ 📊 Digital vs Papel                 │
├─────────────────────────────────────┤
│                                     │
│ ⚡ VELOCIDADE                       │
│ Digital:  300 itens/dia             │
│ Papel:     75 itens/dia             │
│ Ganho:     4x mais rápido           │
│ [████████████░░░░] 400%             │
│                                     │
│ 💰 ECONOMIA                         │
│ Economizado: R$ 20.000              │
│ Por item:    R$ 2,00                │
│ Total:       80% economia           │
│ [████████████████] 80%              │
│                                     │
│ ⏰ TEMPO                            │
│ Economizado: 293 horas              │
│ Equivalente: 37 dias                │
│ Redução:     84%                    │
│ [████████████████░] 84%             │
│                                     │
│ 🎯 QUALIDADE                        │
│ Taxa de erro: 1%                    │
│ Papel:        15%                   │
│ Melhoria:     15x                   │
│ [████████████████████] 95%          │
│                                     │
└─────────────────────────────────────┘
```

**Endpoints Necessários:**
- `GET /api/mobile/estatisticas/comparativo?inventarioId={id}`

**Response:**
```json
{
  "velocidade": {
    "digital": 300,
    "papel": 75,
    "ganho": 4.0
  },
  "economia": {
    "financeira": 20000.00,
    "porItem": 2.00,
    "percentual": 80.0
  },
  "tempo": {
    "horasEconomizadas": 293,
    "diasEquivalentes": 37,
    "percentual": 84.0
  },
  "qualidade": {
    "taxaErroDigital": 1.0,
    "taxaErroPapel": 15.0,
    "melhoria": 15.0
  }
}
```

---

### 2. Produtividade (PRIORIDADE ALTA)

**Localização:** Menu → Estatísticas → Tab "Produtividade"

**Conteúdo:**
```
┌─────────────────────────────────────┐
│ 🏆 Ranking de Coletores             │
├─────────────────────────────────────┤
│                                     │
│ 1. 🥇 João Silva                    │
│    450 itens/dia | 2.250 total      │
│    [████████████████████] 100%      │
│                                     │
│ 2. 🥈 Maria Santos                  │
│    380 itens/dia | 1.900 total      │
│    [████████████████░░░░] 84%       │
│                                     │
│ 3. 🥉 Pedro Costa                   │
│    320 itens/dia | 1.600 total      │
│    [██████████████░░░░░░] 71%       │
│                                     │
│ ─────────────────────────────       │
│                                     │
│ 📊 ESTATÍSTICAS GERAIS              │
│ Média:        350 itens/dia         │
│ Meta:         300 itens/dia ✅      │
│ Melhor dia:   520 itens             │
│ Pior dia:     180 itens             │
│                                     │
│ ⏱️ TEMPO MÉDIO                      │
│ Por coleta:   18 segundos           │
│ Meta:         15 segundos           │
│ Papel:        3 minutos             │
│ Ganho:        10x mais rápido       │
│                                     │
└─────────────────────────────────────┘
```

**Endpoints Necessários:**
- `GET /api/mobile/estatisticas/produtividade?inventarioId={id}`

**Response:**
```json
{
  "ranking": [
    {
      "posicao": 1,
      "nome": "João Silva",
      "itensPorDia": 450,
      "totalItens": 2250,
      "percentualMeta": 150.0
    }
  ],
  "estatisticasGerais": {
    "media": 350,
    "meta": 300,
    "melhorDia": 520,
    "piorDia": 180
  },
  "tempoMedio": {
    "porColeta": 18,
    "meta": 15,
    "papel": 180,
    "ganho": 10.0
  }
}
```

---

### 3. Economia (PRIORIDADE MÉDIA)

**Localização:** Menu → Estatísticas → Tab "Economia"

**Conteúdo:**
```
┌─────────────────────────────────────┐
│ 💰 Economia Gerada                  │
├─────────────────────────────────────┤
│                                     │
│ 💵 FINANCEIRA                       │
│ Total economizado: R$ 20.000        │
│ Por patrimônio:    R$ 2,00          │
│ Percentual:        80%              │
│                                     │
│ [Gráfico de pizza]                  │
│ Mão de obra:  R$ 15.000 (75%)       │
│ Materiais:    R$  2.000 (10%)       │
│ Retrabalho:   R$  3.000 (15%)       │
│                                     │
│ ⏰ TEMPO                            │
│ Horas economizadas:  293h           │
│ Dias de trabalho:    37 dias        │
│ Valor estimado:      R$ 14.650      │
│                                     │
│ 🌱 AMBIENTAL                        │
│ Papel não usado:     10.000 folhas  │
│ Árvores salvas:      5 árvores      │
│ CO2 não emitido:     50 kg          │
│ Água economizada:    50.000 litros  │
│                                     │
│ 📈 ROI                              │
│ Investimento:        R$ 65.000      │
│ Economia/ano:        R$ 44.000      │
│ Payback:             1,5 anos       │
│ ROI 5 anos:          600%           │
│                                     │
└─────────────────────────────────────┘
```

**Endpoints Necessários:**
- `GET /api/mobile/estatisticas/economia?inventarioId={id}`

---

### 4. Qualidade (PRIORIDADE MÉDIA)

**Localização:** Menu → Estatísticas → Tab "Qualidade"

**Conteúdo:**
```
┌─────────────────────────────────────┐
│ 🎯 Qualidade dos Dados              │
├─────────────────────────────────────┤
│                                     │
│ 📊 TAXA DE ERRO                     │
│ Digital:  1.0% (12 erros)           │
│ Papel:    15.0% (estimado)          │
│ Melhoria: 15x menos erros           │
│                                     │
│ [Gráfico de barras]                 │
│ Digital: [█░░░░░░░░░░░░░░░░░░░░]    │
│ Papel:   [███████████████░░░░░]     │
│                                     │
│ ✅ COMPLETUDE                       │
│ Campos obrigatórios:  100%          │
│ Com foto:             95%           │
│ Com GPS:              90%           │
│ Com observações:      60%           │
│                                     │
│ ⚠️ DIVERGÊNCIAS                     │
│ Total detectadas:     24 (0.2%)     │
│ Resolvidas:           18 (75%)      │
│ Pendentes:            6 (25%)       │
│ Tempo médio:          2 dias        │
│                                     │
│ 📍 TIPOS DE DIVERGÊNCIA             │
│ [Gráfico de pizza]                  │
│ Localização:  15 (62%)              │
│ Estado:       6 (25%)               │
│ Responsável:  3 (13%)               │
│                                     │
└─────────────────────────────────────┘
```

---

### 5. Evolução Temporal (PRIORIDADE BAIXA)

**Localização:** Menu → Estatísticas → Tab "Evolução"

**Conteúdo:**
```
┌─────────────────────────────────────┐
│ 📈 Evolução do Inventário           │
├─────────────────────────────────────┤
│                                     │
│ [Gráfico de linha - 30 dias]        │
│                                     │
│ 📊 VELOCIDADE                       │
│ Dia 1:    120 itens/dia             │
│ Dia 15:   350 itens/dia             │
│ Dia 30:   420 itens/dia             │
│ Evolução: +250% 🚀                  │
│                                     │
│ 🎯 PREVISÃO                         │
│ Concluído:    78%                   │
│ Restante:     2.200 itens           │
│ Velocidade:   320 itens/dia         │
│ Previsão:     7 dias                │
│ Data final:   23/11/2025            │
│                                     │
│ ⚡ ACELERAÇÃO                       │
│ Semana 1:     180 itens/dia         │
│ Semana 2:     280 itens/dia         │
│ Semana 3:     350 itens/dia         │
│ Semana 4:     420 itens/dia         │
│ Tendência:    ↗️ Crescente          │
│                                     │
└─────────────────────────────────────┘
```

---

## 🔧 Implementação Técnica

### Backend - Novos Endpoints

#### 1. Estatísticas Comparativas
```java
@GetMapping("/estatisticas/comparativo")
public ResponseEntity<ApiResponse<EstatisticasComparativasDTO>> 
    buscarEstatisticasComparativas(@RequestParam Integer inventarioId) {
    // Implementação
}
```

#### 2. Produtividade
```java
@GetMapping("/estatisticas/produtividade")
public ResponseEntity<ApiResponse<ProdutividadeDTO>> 
    buscarProdutividade(@RequestParam Integer inventarioId) {
    // Implementação
}
```

#### 3. Economia
```java
@GetMapping("/estatisticas/economia")
public ResponseEntity<ApiResponse<EconomiaDTO>> 
    buscarEconomia(@RequestParam Integer inventarioId) {
    // Implementação
}
```

#### 4. Qualidade
```java
@GetMapping("/estatisticas/qualidade")
public ResponseEntity<ApiResponse<QualidadeDTO>> 
    buscarQualidade(@RequestParam Integer inventarioId) {
    // Implementação
}
```

### Android - Novos Componentes

#### 1. Repository
```kotlin
interface EstatisticasRepository {
    suspend fun buscarComparativo(inventarioId: Int): Result<EstatisticasComparativas>
    suspend fun buscarProdutividade(inventarioId: Int): Result<Produtividade>
    suspend fun buscarEconomia(inventarioId: Int): Result<Economia>
    suspend fun buscarQualidade(inventarioId: Int): Result<Qualidade>
}
```

#### 2. Use Cases
```kotlin
class BuscarEstatisticasComparativasUseCase
class BuscarProdutividadeUseCase
class BuscarEconomiaUseCase
class BuscarQualidadeUseCase
```

#### 3. ViewModel
```kotlin
@HiltViewModel
class EstatisticasViewModel @Inject constructor(
    private val buscarComparativoUseCase: BuscarEstatisticasComparativasUseCase,
    private val buscarProdutividadeUseCase: BuscarProdutividadeUseCase,
    private val buscarEconomiaUseCase: BuscarEconomiaUseCase,
    private val buscarQualidadeUseCase: BuscarQualidadeUseCase
) : ViewModel()
```

#### 4. Fragments
```kotlin
ComparativoFragment.kt
ProdutividadeFragment.kt
EconomiaFragment.kt
QualidadeFragment.kt
EvolucaoFragment.kt
```

---

## 📅 Cronograma de Implementação

### Sprint 1 (1 semana) - ESSENCIAL
- [ ] Backend: Endpoint de comparativo
- [ ] Backend: Endpoint de produtividade
- [ ] Android: ComparativoFragment
- [ ] Android: ProdutividadeFragment
- [ ] Testes e validação

### Sprint 2 (1 semana) - IMPORTANTE
- [ ] Backend: Endpoint de economia
- [ ] Backend: Endpoint de qualidade
- [ ] Android: EconomiaFragment
- [ ] Android: QualidadeFragment
- [ ] Testes e validação

### Sprint 3 (1 semana) - COMPLEMENTAR
- [ ] Backend: Endpoint de evolução
- [ ] Android: EvolucaoFragment
- [ ] Gráficos avançados
- [ ] Exportação de relatórios
- [ ] Testes finais

---

## 🎯 Métricas de Sucesso

### Técnicas
- [ ] Todos os endpoints respondendo < 500ms
- [ ] Taxa de erro < 1%
- [ ] Cobertura de testes > 80%
- [ ] App não crasha

### Negócio
- [ ] Gestores usam estatísticas semanalmente
- [ ] Decisões baseadas em dados aumentam 50%
- [ ] Satisfação dos usuários > 90%
- [ ] ROI comprovado com dados reais

---

## 📊 Queries SQL Necessárias

### 1. Velocidade Média
```sql
SELECT 
    COUNT(*) as total_coletas,
    EXTRACT(EPOCH FROM (MAX(DATA_COLETA) - MIN(DATA_COLETA))) / 3600.0 as horas_totais,
    COUNT(*) / NULLIF(EXTRACT(EPOCH FROM (MAX(DATA_COLETA) - MIN(DATA_COLETA))) / 3600.0, 0) as itens_por_hora
FROM TABELA_COLETA
WHERE ID_INVENTARIO = ?;
```

### 2. Ranking de Coletores
```sql
SELECT 
    u.NOME_COMPLETO,
    COUNT(*) as total_coletas,
    COUNT(*) / NULLIF(EXTRACT(DAY FROM (MAX(c.DATA_COLETA) - MIN(c.DATA_COLETA))), 0) as coletas_por_dia,
    RANK() OVER (ORDER BY COUNT(*) DESC) as posicao
FROM TABELA_COLETA c
JOIN TABELA_USUARIO u ON c.ID_COLETOR = u.ID
WHERE c.ID_INVENTARIO = ?
GROUP BY u.ID, u.NOME_COMPLETO
ORDER BY total_coletas DESC
LIMIT 10;
```

### 3. Taxa de Erro
```sql
SELECT 
    COUNT(*) as total_coletas,
    SUM(CASE WHEN DIVERGENCIA = true THEN 1 ELSE 0 END) as divergencias,
    (SUM(CASE WHEN DIVERGENCIA = true THEN 1 ELSE 0 END) * 100.0 / NULLIF(COUNT(*), 0)) as taxa_divergencia
FROM TABELA_COLETA
WHERE ID_INVENTARIO = ?;
```

### 4. Economia de Tempo
```sql
SELECT 
    COUNT(*) as total_coletas,
    COUNT(*) * 18.0 / 3600 as horas_digital,
    COUNT(*) * 180.0 / 3600 as horas_papel,
    (COUNT(*) * 180.0 / 3600) - (COUNT(*) * 18.0 / 3600) as horas_economizadas,
    ((COUNT(*) * 180.0 / 3600) - (COUNT(*) * 18.0 / 3600)) / 8.0 as dias_economizados
FROM TABELA_COLETA
WHERE ID_INVENTARIO = ?;
```

---

## ✅ Checklist de Implementação

### Backend
- [ ] Criar DTOs de resposta
- [ ] Implementar services
- [ ] Criar controllers
- [ ] Adicionar queries SQL
- [ ] Testar endpoints
- [ ] Documentar API

### Android
- [ ] Criar DTOs Kotlin
- [ ] Implementar Repository
- [ ] Criar Use Cases
- [ ] Implementar ViewModel
- [ ] Criar Fragments
- [ ] Adicionar gráficos
- [ ] Testar fluxo completo

### Documentação
- [ ] Atualizar README
- [ ] Criar guia de uso
- [ ] Documentar métricas
- [ ] Preparar apresentação

---

**Preparado por:** Kiro AI Assistant  
**Data:** 16/11/2025  
**Versão:** 1.0  
**Status:** Pronto para implementação
