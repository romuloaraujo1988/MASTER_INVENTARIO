---
inclusion: always
---

# Guia de Migração - Clean Architecture + MVVM

## 🎯 Objetivo

Migrar gradualmente o app Android para Clean Architecture + MVVM sem quebrar funcionalidades existentes.

---

## 📦 1. Adicionar Dependências (build.gradle)

### build.gradle (Project level)
```gradle
buildscript {
    dependencies {
        classpath "com.google.dagger:hilt-android-gradle-plugin:2.48"
    }
}
```

### build.gradle (App level)
```gradle
plugins {
    id 'com.android.application'
    id 'kotlin-android'
    id 'kotlin-kapt'
    id 'dagger.hilt.android.plugin'
}

dependencies {
    // Hilt
    implementation "com.google.dagger:hilt-android:2.48"
    kapt "com.google.dagger:hilt-compiler:2.48"
    
    // Room (já deve estar)
    def room_version = "2.6.0"
    implementation "androidx.room:room-runtime:$room_version"
    implementation "androidx.room:room-ktx:$room_version"
    kapt "androidx.room:room-compiler:$room_version"
    
    // Lifecycle
    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2"
    implementation "androidx.lifecycle:lifecycle-runtime-ktx:2.6.2"
}
```

---

## 🔄 2. Migrar Activity/Fragment

### ANTES (Código Antigo)
```kotlin
class DescricaoSelectionActivity : AppCompatActivity() {
    
    private val viewModel: DescricaoSelectionViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Observar estado antigo
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                if (state.isLoading) {
                    showLoading()
                } else {
                    hideLoading()
                    updateDescricoes(state.descricoes)
                }
            }
        }
        
        viewModel.loadDescricoes()
    }
}
```

### DEPOIS (Clean Architecture)
```kotlin
@AndroidEntryPoint  // ← Adicionar esta anotação
class DescricaoSelectionActivity : AppCompatActivity() {
    
    // Usar ViewModel refatorado
    private val viewModel: DescricaoSelectionViewModelClean by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Observar estado com sealed class
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is DescricaoState.Idle -> hideLoading()
                    is DescricaoState.Loading -> showLoading()
                    is DescricaoState.Success -> {
                        hideLoading()
                        updateDescricoes(state.descricoes)
                    }
                    is DescricaoState.Error -> {
                        hideLoading()
                        showError(state.message)
                    }
                }
            }
        }
        
        viewModel.carregarDescricoes()
    }
}
```

---

## 🏗️ 3. Estrutura de Pastas

```
mobile/
├── data/                           # Camada de Dados
│   ├── local/
│   │   ├── dao/                   # ✅ CRIADO
│   │   ├── entity/                # ✅ CRIADO
│   │   └── database/              # ✅ CRIADO
│   ├── remote/
│   │   ├── api/                   # ✅ CRIADO
│   │   └── dto/                   # ✅ CRIADO
│   ├── mapper/                    # ✅ CRIADO
│   └── repository/                # ✅ CRIADO
│
├── domain/                         # Camada de Domínio
│   ├── model/                     # ✅ JÁ EXISTE
│   ├── repository/                # ✅ JÁ EXISTE (interfaces)
│   └── usecase/                   # ✅ CRIADO
│
├── presentation/                   # Camada de Apresentação
│   ├── state/                     # ✅ CRIADO
│   ├── viewmodel/                 # ✅ PARCIAL (refatorar)
│   └── [features]/                # ✅ JÁ EXISTE
│
└── di/                            # Injeção de Dependência
    ├── DatabaseModule.kt          # ✅ CRIADO
    ├── RepositoryModule.kt        # ✅ CRIADO
    ├── ApiModule.kt               # ✅ CRIADO
    └── MapperModule.kt            # ✅ CRIADO
```

---

## 🔧 4. Checklist de Migração por Feature

### Feature: Coleta de Patrimônios

- [x] Domain Models
- [x] Repository Interface
- [x] Use Cases
- [x] Room Entities
- [x] Room DAOs
- [x] Repository Implementation
- [x] ViewModel Clean
- [ ] Migrar Activity
- [ ] Testar fluxo completo

### Feature: Seleção de Descrição

- [x] Use Case (BuscarDescricoesNaoColetadasUseCase)
- [x] ViewModel Clean
- [ ] Migrar Activity
- [ ] Testar fluxo completo

### Feature: Sincronização Offline

- [x] Room Database
- [x] Entities
- [x] DAOs
- [ ] SincronizacaoRepositoryImpl
- [ ] WorkManager para sync
- [ ] Tela de sincronização

---

## 🚀 5. Ordem de Migração Recomendada

### Fase 1: Infraestrutura (✅ CONCLUÍDA)
1. ✅ Criar Entities Room
2. ✅ Criar DAOs
3. ✅ Criar AppDatabase
4. ✅ Criar Use Cases
5. ✅ Criar Repositories
6. ✅ Configurar Hilt

### Fase 2: Features Críticas (ATUAL)
1. Migrar tela de Coleta
2. Migrar tela de Descrição
3. Testar offline-first

### Fase 3: Features Secundárias
1. Migrar Dashboard
2. Migrar Estatísticas
3. Migrar Configurações

### Fase 4: Otimizações
1. Implementar cache
2. Otimizar queries
3. Adicionar testes

---

## 📝 6. Exemplo Completo: Migrar uma Activity

### Passo 1: Adicionar @AndroidEntryPoint
```kotlin
@AndroidEntryPoint
class MinhaActivity : AppCompatActivity() {
```

### Passo 2: Trocar ViewModel
```kotlin
// ANTES
private val viewModel: MinhaViewModel by viewModels()

// DEPOIS
private val viewModel: MinhaViewModelClean by viewModels()
```

### Passo 3: Observar Estado
```kotlin
lifecycleScope.launch {
    viewModel.state.collect { state ->
        when (state) {
            is MinhaState.Idle -> { /* nada */ }
            is MinhaState.Loading -> showLoading()
            is MinhaState.Success -> handleSuccess(state.data)
            is MinhaState.Error -> showError(state.message)
        }
    }
}
```

### Passo 4: Chamar Use Case via ViewModel
```kotlin
// ANTES
viewModel.buscarDados(parametro)

// DEPOIS (mesmo método, mas usa Use Case internamente)
viewModel.buscarDados(parametro)
```

---

## ⚠️ 7. Problemas Comuns

### Erro: "Hilt not found"
**Solução**: Adicionar plugin no build.gradle e sync

### Erro: "Cannot create an instance of ViewModel"
**Solução**: Adicionar @HiltViewModel no ViewModel e @AndroidEntryPoint na Activity

### Erro: "Room database not initialized"
**Solução**: Verificar se DatabaseModule está correto

### Erro: "API call fails"
**Solução**: Verificar se ApiModule está provendo as APIs corretamente

---

## ✅ 8. Validação

Após migrar uma feature, validar:

1. [ ] App compila sem erros
2. [ ] Feature funciona online
3. [ ] Feature funciona offline
4. [ ] Sincronização funciona
5. [ ] Não há memory leaks
6. [ ] Performance está boa

---

## 📚 9. Recursos

- [Clean Architecture - Uncle Bob](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Hilt Documentation](https://developer.android.com/training/dependency-injection/hilt-android)
- [Room Documentation](https://developer.android.com/training/data-storage/room)

