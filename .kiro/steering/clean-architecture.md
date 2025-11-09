---
inclusion: always
---

# Clean Architecture + MVVM - Diretrizes do Projeto

## 🎯 Arquitetura Obrigatória

TODO código novo ou refatorado DEVE seguir Clean Architecture + MVVM.

---

## 📁 Estrutura de Pacotes (Android)

```
com.ifmt.inventariomobile/
├── data/                           # Camada de Dados
│   ├── local/                      # Fontes de dados locais
│   │   ├── database/              # Room Database
│   │   │   ├── AppDatabase.kt
│   │   │   ├── dao/               # Data Access Objects
│   │   │   └── entity/            # Entities do Room
│   │   └── preferences/           # SharedPreferences
│   │
│   ├── remote/                     # Fontes de dados remotas
│   │   ├── api/                   # Interfaces Retrofit
│   │   ├── dto/                   # Data Transfer Objects
│   │   └── interceptor/           # Interceptors HTTP
│   │
│   └── repository/                 # Implementação dos Repositórios
│       └── ColetaRepositoryImpl.kt
│
├── domain/                         # Camada de Domínio (Regras de Negócio)
│   ├── model/                     # Modelos de Domínio (entidades puras)
│   │   ├── Patrimonio.kt
│   │   ├── Coleta.kt
│   │   └── Sala.kt
│   │
│   ├── repository/                # Interfaces dos Repositórios
│   │   └── ColetaRepository.kt
│   │
│   └── usecase/                   # Casos de Uso
│       ├── RegistrarColetaUseCase.kt
│       ├── BuscarPatrimonioUseCase.kt
│       └── SincronizarDadosUseCase.kt
│
└── presentation/                   # Camada de Apresentação
    ├── ui/                        # Activities, Fragments, Composables
    │   ├── coleta/
    │   │   ├── ColetaFragment.kt
    │   │   └── ColetaAdapter.kt
    │   ├── sync/
    │   └── dashboard/
    │
    ├── viewmodel/                 # ViewModels
    │   ├── ColetaViewModel.kt
    │   └── SyncViewModel.kt
    │
    ├── mapper/                    # Conversores entre camadas
    │   └── PatrimonioMapper.kt
    │
    └── state/                     # Estados da UI
        └── ColetaState.kt
```

---

## 🔄 Fluxo de Dados

```
UI (Fragment/Activity)
    ↓ (user action)
ViewModel
    ↓ (calls)
Use Case
    ↓ (calls)
Repository (interface)
    ↓ (implements)
Repository Implementation
    ↓ (uses)
Data Sources (Local/Remote)
```

---

## 📝 Regras de Dependência

### ❌ PROIBIDO

```kotlin
// ❌ Domain NÃO pode depender de Data ou Presentation
// domain/model/Patrimonio.kt
import com.ifmt.inventariomobile.data.local.entity.PatrimonioEntity // ERRADO!

// ❌ Domain NÃO pode depender de Android
import android.os.Parcelable // ERRADO!

// ❌ ViewModel NÃO pode acessar Data Source diretamente
class ColetaViewModel(
    private val database: AppDatabase // ERRADO!
)
```

### ✅ CORRETO

```kotlin
// ✅ Presentation depende de Domain
class ColetaViewModel(
    private val registrarColetaUseCase: RegistrarColetaUseCase // CORRETO!
)

// ✅ Data implementa interfaces de Domain
class ColetaRepositoryImpl(
    private val localDataSource: ColetaLocalDataSource,
    private val remoteDataSource: ColetaRemoteDataSource
) : ColetaRepository // CORRETO!

// ✅ Domain é puro Kotlin (sem Android)
data class Patrimonio(
    val id: Int,
    val numero: String,
    val descricao: String
) // CORRETO!
```

---

## 🎨 Templates de Código

### 1. Domain Model (Entidade Pura)

```kotlin
// domain/model/Patrimonio.kt
package com.ifmt.inventariomobile.domain.model

/**
 * Modelo de domínio para Patrimônio
 * Regra: SEM dependências Android, apenas Kotlin puro
 */
data class Patrimonio(
    val id: Int,
    val numero: String,
    val descricao: String,
    val idSala: Int?,
    val nomeSala: String?,
    val status: String
) {
    // Regras de negócio aqui
    fun isAtivo(): Boolean = status == "ATIVO"
    
    fun podeSerColetado(): Boolean = isAtivo()
}
```

### 2. Repository Interface (Domain)

```kotlin
// domain/repository/ColetaRepository.kt
package com.ifmt.inventariomobile.domain.repository

import com.ifmt.inventariomobile.domain.model.Coleta
import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de coletas
 * Regra: Define O QUE fazer, não COMO fazer
 */
interface ColetaRepository {
    
    suspend fun registrarColeta(coleta: Coleta): Result<Coleta>
    
    fun observarColetas(): Flow<List<Coleta>>
    
    suspend fun buscarColetasPendentes(): List<Coleta>
    
    suspend fun sincronizarColetas(): Result<Int>
}
```

### 3. Use Case

```kotlin
// domain/usecase/RegistrarColetaUseCase.kt
package com.ifmt.inventariomobile.domain.usecase

import com.ifmt.inventariomobile.domain.model.Coleta
import com.ifmt.inventariomobile.domain.repository.ColetaRepository
import com.ifmt.inventariomobile.domain.repository.PatrimonioRepository
import javax.inject.Inject

/**
 * Caso de uso: Registrar uma coleta
 * Regra: Contém APENAS lógica de negócio
 */
class RegistrarColetaUseCase @Inject constructor(
    private val coletaRepository: ColetaRepository,
    private val patrimonioRepository: PatrimonioRepository
) {
    suspend operator fun invoke(numeroPatrimonio: String): Result<Coleta> {
        return try {
            // 1. Validar entrada
            if (numeroPatrimonio.isBlank()) {
                return Result.failure(Exception("Número do patrimônio é obrigatório"))
            }
            
            // 2. Buscar patrimônio
            val patrimonio = patrimonioRepository.buscarPorNumero(numeroPatrimonio)
                ?: return Result.failure(Exception("Patrimônio não encontrado"))
            
            // 3. Validar regras de negócio
            if (!patrimonio.podeSerColetado()) {
                return Result.failure(Exception("Patrimônio não pode ser coletado"))
            }
            
            // 4. Verificar duplicação
            if (coletaRepository.jaFoiColetado(patrimonio.id)) {
                return Result.failure(Exception("Patrimônio já foi coletado"))
            }
            
            // 5. Criar e registrar coleta
            val coleta = Coleta(
                idPatrimonio = patrimonio.id,
                numeroPatrimonio = patrimonio.numero,
                dataColeta = System.currentTimeMillis()
            )
            
            coletaRepository.registrarColeta(coleta)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### 4. Repository Implementation (Data)

```kotlin
// data/repository/ColetaRepositoryImpl.kt
package com.ifmt.inventariomobile.data.repository

import com.ifmt.inventariomobile.data.local.dao.ColetaDao
import com.ifmt.inventariomobile.data.remote.api.ColetaApi
import com.ifmt.inventariomobile.data.mapper.ColetaMapper
import com.ifmt.inventariomobile.domain.model.Coleta
import com.ifmt.inventariomobile.domain.repository.ColetaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementação do repositório de coletas
 * Regra: Coordena fontes de dados (local e remota)
 */
class ColetaRepositoryImpl @Inject constructor(
    private val coletaDao: ColetaDao,
    private val coletaApi: ColetaApi,
    private val mapper: ColetaMapper
) : ColetaRepository {
    
    override suspend fun registrarColeta(coleta: Coleta): Result<Coleta> {
        return try {
            // 1. Salvar localmente (offline-first)
            val entity = mapper.toEntity(coleta)
            coletaDao.insert(entity)
            
            // 2. Tentar sincronizar (não bloqueia)
            try {
                val dto = mapper.toDto(coleta)
                coletaApi.registrarColeta(dto)
                coletaDao.marcarSincronizada(entity.id)
            } catch (e: Exception) {
                // Falha na sincronização não impede o sucesso local
            }
            
            Result.success(coleta)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun observarColetas(): Flow<List<Coleta>> {
        return coletaDao.observarTodas()
            .map { entities -> entities.map { mapper.toDomain(it) } }
    }
}
```

### 5. ViewModel

```kotlin
// presentation/viewmodel/ColetaViewModel.kt
package com.ifmt.inventariomobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ifmt.inventariomobile.domain.usecase.RegistrarColetaUseCase
import com.ifmt.inventariomobile.presentation.state.ColetaState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para tela de coleta
 * Regra: Gerencia estado da UI e chama Use Cases
 */
@HiltViewModel
class ColetaViewModel @Inject constructor(
    private val registrarColetaUseCase: RegistrarColetaUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow<ColetaState>(ColetaState.Idle)
    val state: StateFlow<ColetaState> = _state.asStateFlow()
    
    fun registrarColeta(numeroPatrimonio: String) {
        viewModelScope.launch {
            _state.value = ColetaState.Loading
            
            registrarColetaUseCase(numeroPatrimonio).fold(
                onSuccess = { coleta ->
                    _state.value = ColetaState.Success(coleta)
                },
                onFailure = { error ->
                    _state.value = ColetaState.Error(error.message ?: "Erro desconhecido")
                }
            )
        }
    }
    
    fun limparEstado() {
        _state.value = ColetaState.Idle
    }
}
```

### 6. UI State

```kotlin
// presentation/state/ColetaState.kt
package com.ifmt.inventariomobile.presentation.state

import com.ifmt.inventariomobile.domain.model.Coleta

/**
 * Estados possíveis da tela de coleta
 * Regra: Sealed class para estados mutuamente exclusivos
 */
sealed class ColetaState {
    object Idle : ColetaState()
    object Loading : ColetaState()
    data class Success(val coleta: Coleta) : ColetaState()
    data class Error(val message: String) : ColetaState()
}
```

### 7. Fragment/Activity

```kotlin
// presentation/ui/coleta/ColetaFragment.kt
package com.ifmt.inventariomobile.presentation.ui.coleta

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.ifmt.inventariomobile.presentation.viewmodel.ColetaViewModel
import com.ifmt.inventariomobile.presentation.state.ColetaState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment para coleta de patrimônios
 * Regra: Apenas UI, delega lógica para ViewModel
 */
@AndroidEntryPoint
class ColetaFragment : Fragment() {
    
    private val viewModel: ColetaViewModel by viewModels()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
        setupListeners()
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is ColetaState.Idle -> hideLoading()
                    is ColetaState.Loading -> showLoading()
                    is ColetaState.Success -> handleSuccess(state.coleta)
                    is ColetaState.Error -> handleError(state.message)
                }
            }
        }
    }
    
    private fun setupListeners() {
        binding.btnRegistrar.setOnClickListener {
            val numero = binding.edtNumero.text.toString()
            viewModel.registrarColeta(numero)
        }
    }
    
    // Métodos de UI...
}
```

---

## 🔧 Injeção de Dependência (Hilt)

```kotlin
// di/RepositoryModule.kt
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindColetaRepository(
        impl: ColetaRepositoryImpl
    ): ColetaRepository
}

// di/DatabaseModule.kt
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "inventario.db"
        ).build()
    }
    
    @Provides
    fun provideColetaDao(database: AppDatabase): ColetaDao {
        return database.coletaDao()
    }
}
```

---

## ✅ Checklist para Novo Código

Antes de criar/modificar código, verificar:

- [ ] Está na camada correta? (data/domain/presentation)
- [ ] Domain não tem dependências Android?
- [ ] Use Case contém apenas lógica de negócio?
- [ ] Repository usa interface de Domain?
- [ ] ViewModel usa Use Cases (não Repository direto)?
- [ ] UI apenas renderiza estado (sem lógica)?
- [ ] Injeção de dependência configurada?
- [ ] Testes unitários possíveis?

---

## 🚫 Anti-Patterns a Evitar

```kotlin
// ❌ God ViewModel (faz tudo)
class ColetaViewModel {
    fun registrarColeta() { /* SQL direto */ }
    fun buscarPatrimonio() { /* HTTP direto */ }
    fun validarDados() { /* lógica complexa */ }
}

// ❌ Anemic Domain Model (sem comportamento)
data class Patrimonio(val id: Int, val nome: String)
// Toda lógica está em Services/Utils

// ❌ Repository com lógica de negócio
class ColetaRepository {
    fun registrar(coleta: Coleta) {
        if (coleta.valor > 1000) { // ERRADO! Regra de negócio
            // ...
        }
    }
}

// ❌ Fragment com lógica de negócio
class ColetaFragment {
    fun registrar() {
        val patrimonio = database.buscar() // ERRADO! Acesso direto
        if (patrimonio.isValid()) { // ERRADO! Lógica aqui
            api.enviar() // ERRADO! Acesso direto
        }
    }
}
```

---

## 📚 Referências

- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Clean Architecture - Uncle Bob](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Guide to app architecture](https://developer.android.com/topic/architecture)

---

**IMPORTANTE**: Este documento é uma steering rule SEMPRE ATIVA. Todo código novo ou refatorado DEVE seguir estas diretrizes.

**Versão**: 2.0.0  
**Data**: 08/11/2025
