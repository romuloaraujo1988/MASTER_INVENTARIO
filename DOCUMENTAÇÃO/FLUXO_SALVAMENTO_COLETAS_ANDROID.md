# Fluxo de Salvamento de Coletas - App Android

## 📋 Análise Completa do Fluxo Atual

### Caminho do Dado: Activity → ViewModel → UseCase → Repository → DAO → Banco

```
┌─────────────────────────────────────────────────────────────────┐
│                    1. ACTIVITY/FRAGMENT                          │
│  - Captura dados do usuário (QR, manual, etc)                   │
│  - Chama ViewModel.registrarColeta()                            │
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│              2. VIEWMODEL (ColetaViewModelClean)                 │
│  - Gerencia estado da UI                                         │
│  - Chama RegistrarColetaUseCase                                  │
│  - Atualiza estado: Loading → Success/Error                      │
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│           3. USE CASE (RegistrarColetaUseCase)                   │
│  - Valida entrada (número não vazio)                             │
│  - Busca patrimônio no repositório                               │
│  - Cria objeto Coleta (Domain Model)                             │
│  - Chama ColetaRepository.registrarColeta()                      │
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│          4. REPOSITORY (ColetaRepositoryImpl)                    │
│  ✅ Busca dados do patrimônio (número, sala, etc)               │
│  ✅ Converte Domain → Entity via Mapper                          │
│  ✅ Salva no banco local (ColetaDao.inserir)                     │
│  ✅ Marca patrimônio como coletado                               │
│  ✅ Tenta sincronizar com servidor (não bloqueia)                │
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│                5. MAPPER (ColetaMapper)                          │
│  ✅ Busca dados do patrimônio via PatrimonioDao                  │
│  ✅ Preenche numeroPatrimonio, nomeSala, etc                     │
│  ✅ Converte Coleta (Domain) → ColetaEntity (Room)               │
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│                   6. DAO (ColetaDao)                             │
│  ✅ INSERT INTO coleta (...)                                     │
│  ✅ Retorna ID da coleta inserida                                │
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│                7. BANCO LOCAL (Room/SQLite)                      │
│  ✅ Coleta salva com todos os campos preenchidos                 │
│  ✅ Pronta para sincronização                                    │
└─────────────────────────────────────────────────────────────────┘
```

---

## ✅ Campos Preenchidos Corretamente

### ColetaEntity - Campos Completos

```kotlin
ColetaEntity(
    id = 0,                                    // ✅ Auto-gerado
    idPatrimonio = 123,                        // ✅ Do Domain Model
    numeroPatrimonio = "12345",                // ✅ Buscado do PatrimonioDao
    idInventario = 2,                          // ⚠️ Hardcoded (precisa ajustar)
    idSala = 10,                               // ✅ Buscado do PatrimonioDao
    nomeSala = "Sala 101",                     // ✅ Buscado do PatrimonioDao
    idResponsavel = 5,                         // ✅ Buscado do PatrimonioDao
    nomeResponsavel = "João Silva",            // ✅ Buscado do PatrimonioDao
    observacao = "Patrimônio em bom estado",   // ✅ Do usuário
    estadoPatrimonio = "BOM",                  // ✅ Do usuário
    latitude = -15.123,                        // ✅ GPS
    longitude = -56.456,                       // ✅ GPS
    dataColeta = 1731700000000,                // ✅ System.currentTimeMillis()
    idUsuario = 1,                             // ✅ Do contexto
    nomeUsuario = "Maria Santos",              // ⚠️ Hardcoded (precisa ajustar)
    sincronizado = false,                      // ✅ Padrão
    tentativasSincronizacao = 0,               // ✅ Padrão
    erroSincronizacao = null,                  // ✅ Padrão
    servidorId = null                          // ✅ Preenchido após sync
)
```

---

## 🔧 Ajustes Necessários

### 1. ⚠️ ID do Inventário Ativo (CRÍTICO)

**Problema:** Hardcoded como `2` em vários lugares

**Locais a Ajustar:**

#### A. ColetaRepositoryImpl.kt
```kotlin
// ❌ ANTES (linha ~80)
idInventario = 2, // TODO: Obter ID do inventário ativo

// ✅ DEPOIS
idInventario = preferencesManager.getInventarioAtivoId() ?: 0,
```

#### B. ColetaMapper.kt
```kotlin
// ❌ ANTES
suspend fun toEntity(domain: Coleta, idInventario: Int = 0): ColetaEntity

// ✅ DEPOIS
suspend fun toEntity(
    domain: Coleta, 
    idInventario: Int = preferencesManager.getInventarioAtivoId() ?: 0
): ColetaEntity
```

**Solução:**
- Injetar `PreferencesManager` no `ColetaMapper`
- Usar `preferencesManager.getInventarioAtivoId()`
- Garantir que inventário ativo foi salvo no login

---

### 2. ⚠️ Nome do Usuário (MÉDIA PRIORIDADE)

**Problema:** Hardcoded como `"Usuário ${domain.usuarioId}"`

**Locais a Ajustar:**

#### A. ColetaMapper.kt
```kotlin
// ❌ ANTES
nomeUsuario = "Usuário ${domain.usuarioId}", // TODO: Buscar nome real

// ✅ DEPOIS
nomeUsuario = preferencesManager.getNomeUsuario() ?: "Usuário ${domain.usuarioId}",
```

**Solução:**
- Salvar nome do usuário no `PreferencesManager` durante login
- Buscar nome real ao criar ColetaEntity

---

### 3. ✅ Métricas de Performance (v2.1)

**Status:** Estrutura criada, mas não está sendo salva

**Campos Disponíveis mas Não Usados:**
```kotlin
// ColetaEntity - Campos de métricas
tempoColetaSegundos: Int? = null,
tempoScanSegundos: Int? = null,
tempoPreenchimentoSegundos: Int? = null,
metodoColeta: String? = null,
horaColeta: Int? = null,
diaSemana: Int? = null,
periodoColeta: String? = null,
tipoScan: String? = null,
tentativasScan: Int = 1,
errosScan: Int = 0,
qualidadeEtiqueta: String? = null
```

**Ajuste Necessário:**

#### A. RegistrarColetaUseCase.kt
```kotlin
// ✅ ADICIONAR parâmetros de métricas
suspend operator fun invoke(
    numeroPatrimonio: String,
    localizacaoAtual: String?,
    estadoEncontrado: String? = null,
    observacoes: String? = null,
    latitude: Double? = null,
    longitude: Double? = null,
    idUsuario: Long? = null,
    // ✅ NOVOS PARÂMETROS
    tempoColetaSegundos: Int? = null,
    tempoScanSegundos: Int? = null,
    tempoPreenchimentoSegundos: Int? = null,
    metodoColeta: String? = null,
    tipoScan: String? = null,
    tentativasScan: Int = 1,
    errosScan: Int = 0,
    qualidadeEtiqueta: String? = null
): Result<Coleta>
```

#### B. Coleta (Domain Model)
```kotlin
// ✅ ADICIONAR campos de métricas
data class Coleta(
    // ... campos existentes ...
    
    // Métricas de performance
    val tempoColetaSegundos: Int? = null,
    val tempoScanSegundos: Int? = null,
    val tempoPreenchimentoSegundos: Int? = null,
    val metodoColeta: String? = null,
    val horaColeta: Int? = null,
    val diaSemana: Int? = null,
    val periodoColeta: String? = null,
    val tipoScan: String? = null,
    val tentativasScan: Int = 1,
    val errosScan: Int = 0,
    val qualidadeEtiqueta: String? = null
)
```

#### C. ColetaMapper.kt
```kotlin
// ✅ MAPEAR métricas
suspend fun toEntity(domain: Coleta, idInventario: Int = 0): ColetaEntity {
    // ... código existente ...
    
    return ColetaEntity(
        // ... campos existentes ...
        
        // ✅ Métricas
        tempoColetaSegundos = domain.tempoColetaSegundos,
        tempoScanSegundos = domain.tempoScanSegundos,
        tempoPreenchimentoSegundos = domain.tempoPreenchimentoSegundos,
        metodoColeta = domain.metodoColeta,
        horaColeta = domain.horaColeta,
        diaSemana = domain.diaSemana,
        periodoColeta = domain.periodoColeta,
        tipoScan = domain.tipoScan,
        tentativasScan = domain.tentativasScan,
        errosScan = domain.errosScan,
        qualidadeEtiqueta = domain.qualidadeEtiqueta
    )
}
```

---

## 🎯 Prioridades de Ajuste

### 🔴 CRÍTICO (Fazer Agora)
1. **ID do Inventário Ativo**
   - Injetar `PreferencesManager` no `ColetaMapper`
   - Substituir hardcoded `2` por `getInventarioAtivoId()`
   - Garantir que inventário é salvo no login

### 🟡 IMPORTANTE (Fazer Logo)
2. **Nome do Usuário**
   - Salvar nome no `PreferencesManager` durante login
   - Usar nome real ao criar ColetaEntity

### 🟢 MELHORIA (Fazer Depois)
3. **Métricas de Performance**
   - Adicionar campos no Domain Model
   - Passar métricas do ViewModel para UseCase
   - Mapear métricas no ColetaMapper

---

## 📊 Verificação de Dados Salvos

### Query SQL para Verificar Coletas

```sql
SELECT 
    id,
    numeroPatrimonio,
    idInventario,
    nomeSala,
    nomeUsuario,
    dataColeta,
    sincronizado,
    metodoColeta,
    tempoColetaSegundos
FROM coleta
ORDER BY dataColeta DESC
LIMIT 10;
```

### Verificar no Android Studio

```kotlin
// No ColetaRepositoryImpl ou DAO
suspend fun debugColetas() {
    val coletas = coletaDao.buscarTodas()
    coletas.forEach { coleta ->
        Log.d("DEBUG", """
            Coleta ${coleta.id}:
            - Número: ${coleta.numeroPatrimonio}
            - Inventário: ${coleta.idInventario}
            - Sala: ${coleta.nomeSala}
            - Usuário: ${coleta.nomeUsuario}
            - Sincronizado: ${coleta.sincronizado}
        """.trimIndent())
    }
}
```

---

## ✅ Checklist de Implementação

### Fase 1: Correções Críticas
- [ ] Injetar `PreferencesManager` no `ColetaMapper`
- [ ] Substituir `idInventario = 2` por `getInventarioAtivoId()`
- [ ] Testar salvamento com inventário correto
- [ ] Verificar sincronização com servidor

### Fase 2: Melhorias Importantes
- [ ] Salvar nome do usuário no login
- [ ] Usar nome real no `ColetaMapper`
- [ ] Testar com usuários diferentes

### Fase 3: Métricas de Performance
- [ ] Adicionar campos de métricas no Domain Model
- [ ] Atualizar `RegistrarColetaUseCase` com parâmetros
- [ ] Atualizar `ColetaMapper` para mapear métricas
- [ ] Atualizar `ColetaViewModelClean` para passar métricas
- [ ] Testar tracking completo de métricas

---

## 🧪 Testes Recomendados

### Teste 1: Salvamento Básico
```
1. Fazer login
2. Escanear QR Code
3. Registrar coleta
4. Verificar no banco:
   - numeroPatrimonio preenchido
   - idInventario correto
   - nomeSala preenchido
   - nomeUsuario correto
```

### Teste 2: Sincronização
```
1. Coletar 5 patrimônios offline
2. Conectar internet
3. Sincronizar
4. Verificar que todos foram enviados
5. Verificar que sincronizado = true
```

### Teste 3: Métricas
```
1. Iniciar coleta
2. Escanear QR (medir tempo)
3. Preencher formulário (medir tempo)
4. Registrar
5. Verificar métricas salvas no banco
```

---

**Última atualização:** 16/11/2025  
**Versão:** 1.0.0  
**Status:** ✅ Análise Completa - Pronto para Ajustes
