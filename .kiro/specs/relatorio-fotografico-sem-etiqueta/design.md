# Design Document — relatorio-fotografico-sem-etiqueta

## Overview

Integração dos endpoints de relatório fotográfico de itens sem etiqueta ao app Android, seguindo Clean Architecture + MVVM e reutilizando todos os padrões já estabelecidos no projeto (Retrofit + Hilt + Coroutines + StateFlow).

Nenhum endpoint de servidor será criado ou modificado. Toda a mudança é exclusivamente no módulo `InventarioMobile/`.

---

## Architecture

```
CollectionViewActivity (View)
    │  observa StateFlow<RelatorioFotoState>
    ▼
CollectionViewViewModelClean (ViewModel — existente, estendido)
    │  chama
    ├─► BuscarInfoRelatorioFotoUseCase (Domain)
    │       └─► RelatorioFotoApi.buscarInfo()
    │
    └─► BaixarRelatorioFotoPdfUseCase (Domain)
            └─► RelatorioFotoApi.baixarPdf()
                    └─► grava bytes → File (cache)

ApiModule (DI)
    └─► provideRelatorioFotoApi(retrofit) → RelatorioFotoApi
```

---

## Components

### 1. `RelatorioFotoApi` — Interface Retrofit

**Localização:** `data/remote/api/RelatorioFotoApi.kt`

```kotlin
package com.inventario.mobile.data.remote.api

import com.inventario.mobile.data.remote.dto.ApiResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

/**
 * API Retrofit para o Relatório Fotográfico de Coletas.
 *
 * Endpoints do servidor (não alterar URLs — regra endpoints-nao-alterar.md):
 *   GET /api/mobile/relatorios/fotos/{inventarioId}/info   → CONSULTA+
 *   GET /api/mobile/relatorios/fotos/{inventarioId}        → SUPERVISOR+
 */
interface RelatorioFotoApi {

    /**
     * Retorna contagens de fotos por tipo sem gerar o PDF.
     * Requer role CONSULTA ou superior.
     */
    @GET("api/mobile/relatorios/fotos/{inventarioId}/info")
    suspend fun buscarInfo(
        @Path("inventarioId") inventarioId: Int
    ): ApiResponse<Map<String, Any>>

    /**
     * Gera e retorna o PDF como stream binário.
     * Requer role SUPERVISOR ou superior.
     *
     * @Streaming evita carregar o PDF inteiro na memória antes de gravar.
     */
    @Streaming
    @GET("api/mobile/relatorios/fotos/{inventarioId}")
    suspend fun baixarPdf(
        @Path("inventarioId") inventarioId: Int,
        @Query("tipo") tipo: String = "sem_etiqueta"
    ): Response<ResponseBody>
}
```

---

### 2. `RelatorioFotoInfo` — Modelo de Domínio

**Localização:** `domain/model/RelatorioFotoInfo.kt`

```kotlin
package com.inventario.mobile.domain.model

/**
 * Contagens de fotos disponíveis para o relatório fotográfico.
 * Mapeado a partir da resposta do endpoint /info.
 */
data class RelatorioFotoInfo(
    val inventarioId: Int,
    val totalFotos: Int,
    val semEtiqueta: Int,
    val patrimonio: Int,
    val divergencia: Int,
    val temFotos: Boolean
)
```

---

### 3. `SemFotosException` e `PermissaoNegadaException` — Exceções de Domínio

**Localização:** `domain/model/RelatorioFotoExceptions.kt`

```kotlin
package com.inventario.mobile.domain.model

/** Lançada quando o servidor retorna HTTP 204 (nenhuma foto disponível). */
class SemFotosException : Exception("Nenhuma foto disponível para gerar o relatório")

/** Lançada quando o servidor retorna HTTP 403 (role insuficiente). */
class PermissaoNegadaException : Exception("Você não tem permissão para acessar este relatório")
```

---

### 4. `BuscarInfoRelatorioFotoUseCase` — Use Case

**Localização:** `domain/usecase/BuscarInfoRelatorioFotoUseCase.kt`

```kotlin
package com.inventario.mobile.domain.usecase

import com.inventario.mobile.data.remote.api.RelatorioFotoApi
import com.inventario.mobile.domain.model.RelatorioFotoInfo
import javax.inject.Inject

class BuscarInfoRelatorioFotoUseCase @Inject constructor(
    private val api: RelatorioFotoApi
) {
    suspend operator fun invoke(inventarioId: Int): Result<RelatorioFotoInfo> {
        return try {
            val response = api.buscarInfo(inventarioId)
            if (response.success && response.data != null) {
                val data = response.data
                Result.success(
                    RelatorioFotoInfo(
                        inventarioId  = inventarioId,
                        totalFotos    = (data["totalFotos"] as? Number)?.toInt() ?: 0,
                        semEtiqueta   = (data["semEtiqueta"] as? Number)?.toInt() ?: 0,
                        patrimonio    = (data["patrimonio"] as? Number)?.toInt() ?: 0,
                        divergencia   = (data["divergencia"] as? Number)?.toInt() ?: 0,
                        temFotos      = data["temFotos"] as? Boolean ?: false
                    )
                )
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

### 5. `BaixarRelatorioFotoPdfUseCase` — Use Case

**Localização:** `domain/usecase/BaixarRelatorioFotoPdfUseCase.kt`

```kotlin
package com.inventario.mobile.domain.usecase

import android.content.Context
import com.inventario.mobile.data.remote.api.RelatorioFotoApi
import com.inventario.mobile.domain.model.PermissaoNegadaException
import com.inventario.mobile.domain.model.SemFotosException
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class BaixarRelatorioFotoPdfUseCase @Inject constructor(
    private val api: RelatorioFotoApi,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(inventarioId: Int): Result<File> {
        return try {
            val response = api.baixarPdf(inventarioId)

            when (response.code()) {
                204 -> return Result.failure(SemFotosException())
                403 -> return Result.failure(PermissaoNegadaException())
            }

            val body = response.body()
                ?: return Result.failure(Exception("Resposta vazia do servidor"))

            val timestamp = System.currentTimeMillis()
            val arquivo = File(context.cacheDir, "relatorio_sem_etiqueta_${inventarioId}_${timestamp}.pdf")

            // Gravar com buffer de 8 KB para evitar OOM em PDFs grandes
            arquivo.outputStream().buffered(8 * 1024).use { out ->
                body.byteStream().use { input ->
                    input.copyTo(out)
                }
            }

            Result.success(arquivo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

### 6. `RelatorioFotoState` — UI State

**Localização:** `presentation/state/RelatorioFotoState.kt`

```kotlin
package com.inventario.mobile.presentation.state

import com.inventario.mobile.domain.model.RelatorioFotoInfo
import java.io.File

sealed class RelatorioFotoState {
    /** Estado inicial — nenhuma operação em andamento. */
    object Idle : RelatorioFotoState()

    /** Buscando informações de fotos no servidor. */
    object Loading : RelatorioFotoState()

    /** Informações carregadas com sucesso. */
    data class InfoCarregada(val info: RelatorioFotoInfo) : RelatorioFotoState()

    /** Download do PDF em andamento. */
    object Downloading : RelatorioFotoState()

    /** PDF baixado e salvo com sucesso. */
    data class PdfPronto(val arquivo: File) : RelatorioFotoState()

    /** Erro em qualquer operação. */
    data class Erro(val mensagem: String) : RelatorioFotoState()
}
```

---

### 7. Extensão do `CollectionViewViewModelClean`

Adicionar ao ViewModel existente os seguintes membros (sem remover nada do que já existe):

```kotlin
// --- Novos imports ---
import com.inventario.mobile.domain.usecase.BuscarInfoRelatorioFotoUseCase
import com.inventario.mobile.domain.usecase.BaixarRelatorioFotoPdfUseCase
import com.inventario.mobile.domain.model.SemFotosException
import com.inventario.mobile.domain.model.PermissaoNegadaException
import com.inventario.mobile.presentation.state.RelatorioFotoState

// --- Novos parâmetros no construtor (via @Inject) ---
private val buscarInfoRelatorioFotoUseCase: BuscarInfoRelatorioFotoUseCase,
private val baixarRelatorioFotoPdfUseCase: BaixarRelatorioFotoPdfUseCase,

// --- Novo StateFlow ---
private val _relatorioFotoState = MutableStateFlow<RelatorioFotoState>(RelatorioFotoState.Idle)
val relatorioFotoState: StateFlow<RelatorioFotoState> = _relatorioFotoState.asStateFlow()

// --- Novos métodos ---

fun carregarInfoRelatorioFoto(inventarioId: Int) {
    viewModelScope.launch {
        _relatorioFotoState.value = RelatorioFotoState.Loading
        buscarInfoRelatorioFotoUseCase(inventarioId).fold(
            onSuccess = { info ->
                _relatorioFotoState.value = RelatorioFotoState.InfoCarregada(info)
            },
            onFailure = { error ->
                Log.e(TAG, "Erro ao buscar info de fotos", error)
                _relatorioFotoState.value = RelatorioFotoState.Erro(
                    "Não foi possível carregar informações de fotos"
                )
            }
        )
    }
}

fun baixarRelatorioFoto(inventarioId: Int) {
    viewModelScope.launch {
        _relatorioFotoState.value = RelatorioFotoState.Downloading
        baixarRelatorioFotoPdfUseCase(inventarioId).fold(
            onSuccess = { arquivo ->
                _relatorioFotoState.value = RelatorioFotoState.PdfPronto(arquivo)
            },
            onFailure = { error ->
                Log.e(TAG, "Erro ao baixar relatório fotográfico", error)
                val mensagem = when (error) {
                    is SemFotosException       -> error.message ?: "Nenhuma foto disponível"
                    is PermissaoNegadaException -> error.message ?: "Sem permissão"
                    else -> "Erro de conexão. Verifique a rede e tente novamente"
                }
                _relatorioFotoState.value = RelatorioFotoState.Erro(mensagem)
            }
        )
    }
}

fun limparEstadoRelatorioFoto() {
    _relatorioFotoState.value = RelatorioFotoState.Idle
}
```

---

### 8. Alterações em `CollectionViewActivity`

#### 8.1 Observar `relatorioFotoState`

Adicionar dentro de `observeViewModel()`:

```kotlin
lifecycleScope.launchWhenStarted {
    viewModel.relatorioFotoState.collect { state ->
        when (state) {
            is RelatorioFotoState.Idle -> {
                binding.fabRelatorioFoto.isEnabled = true
            }
            is RelatorioFotoState.Loading -> {
                // Apenas aguarda — sem indicador visual extra
            }
            is RelatorioFotoState.InfoCarregada -> {
                atualizarBadgeFotos(state.info.semEtiqueta)
            }
            is RelatorioFotoState.Downloading -> {
                binding.fabRelatorioFoto.isEnabled = false
                binding.progressBarRelatorio.visibility = View.VISIBLE
            }
            is RelatorioFotoState.PdfPronto -> {
                binding.fabRelatorioFoto.isEnabled = true
                binding.progressBarRelatorio.visibility = View.GONE
                abrirPdfNativo(state.arquivo)
                viewModel.limparEstadoRelatorioFoto()
            }
            is RelatorioFotoState.Erro -> {
                binding.fabRelatorioFoto.isEnabled = true
                binding.progressBarRelatorio.visibility = View.GONE
                Toast.makeText(this@CollectionViewActivity, state.mensagem, Toast.LENGTH_LONG).show()
                viewModel.limparEstadoRelatorioFoto()
            }
        }
    }
}
```

#### 8.2 Visibilidade do FAB e chamada ao endpoint `/info`

Adicionar lógica no `setupFilters()` para o chip "Sem Etiqueta":

```kotlin
binding.chipSemEtiqueta.setOnCheckedChangeListener { _, isChecked ->
    if (isChecked) {
        val inventarioId = preferencesManager.getInventarioAtivoId()
        val perfil = preferencesManager.getUserProfile()
        val isSupervisor = perfil == "SUPERVISOR" || perfil == "ADMIN"

        // Mostrar FAB apenas para SUPERVISOR+
        binding.fabRelatorioFoto.visibility = if (isSupervisor) View.VISIBLE else View.GONE

        // Buscar info de fotos para qualquer role
        if (inventarioId != null) {
            viewModel.carregarInfoRelatorioFoto(inventarioId)
        }
    } else {
        binding.fabRelatorioFoto.visibility = View.GONE
        binding.tvFotosBadge.visibility = View.GONE
        viewModel.limparEstadoRelatorioFoto()
    }
}
```

#### 8.3 Ação do FAB

```kotlin
binding.fabRelatorioFoto.setOnClickListener {
    val inventarioId = preferencesManager.getInventarioAtivoId()
    if (inventarioId != null) {
        viewModel.baixarRelatorioFoto(inventarioId)
    } else {
        Toast.makeText(this, "Nenhum inventário ativo encontrado", Toast.LENGTH_SHORT).show()
    }
}
```

#### 8.4 Abertura do PDF

```kotlin
private fun abrirPdfNativo(arquivo: File) {
    try {
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            arquivo
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(
            this,
            "Nenhum visualizador de PDF encontrado. Instale um app para abrir PDFs",
            Toast.LENGTH_LONG
        ).show()
    }
}
```

#### 8.5 Badge de contagem de fotos

```kotlin
private fun atualizarBadgeFotos(quantidade: Int) {
    if (quantidade > 0) {
        binding.tvFotosBadge.text = "$quantidade foto(s) disponível(is)"
        binding.tvFotosBadge.visibility = View.VISIBLE
    } else {
        binding.tvFotosBadge.text = "Nenhum item sem etiqueta possui foto registrada"
        binding.tvFotosBadge.visibility = View.VISIBLE
    }
}
```

#### 8.6 Limpeza de cache em `onDestroy`

```kotlin
override fun onDestroy() {
    super.onDestroy()
    // Limpar PDFs temporários do cache
    cacheDir.listFiles { file ->
        file.name.startsWith("relatorio_sem_etiqueta_")
    }?.forEach { it.delete() }
}
```

---

### 9. Registro do `FileProvider` no `AndroidManifest.xml`

Verificar se já existe um `FileProvider` declarado. Se não existir, adicionar dentro de `<application>`:

```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.provider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_provider_paths" />
</provider>
```

Criar `res/xml/file_provider_paths.xml` (se não existir):

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <cache-path name="cache" path="." />
</paths>
```

> **Nota:** Se o projeto já tiver um `FileProvider` com outra `authority`, reutilizá-lo e apenas garantir que `<cache-path>` esteja incluído no arquivo de paths.

---

### 10. Adição do `RelatorioFotoApi` ao `ApiModule`

Adicionar ao `ApiModule.kt`:

```kotlin
@Provides
@Singleton
fun provideRelatorioFotoApi(retrofit: Retrofit): RelatorioFotoApi {
    return retrofit.create(RelatorioFotoApi::class.java)
}
```

---

### 11. Elementos de UI no layout `activity_collection_view.xml`

Adicionar os seguintes elementos ao layout existente (sem remover nada):

```xml
<!-- Badge de contagem de fotos — visível apenas com filtro Sem Etiqueta ativo -->
<TextView
    android:id="@+id/tvFotosBadge"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:visibility="gone"
    android:textSize="12sp"
    android:textColor="@color/colorPrimary"
    android:drawableStart="@drawable/ic_photo"
    android:drawablePadding="4dp"
    android:padding="8dp" />

<!-- ProgressBar para download do PDF -->
<ProgressBar
    android:id="@+id/progressBarRelatorio"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:visibility="gone" />

<!-- FAB para download do relatório fotográfico — visível apenas para SUPERVISOR+ -->
<com.google.android.material.floatingactionbutton.FloatingActionButton
    android:id="@+id/fabRelatorioFoto"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:visibility="gone"
    android:contentDescription="Baixar Relatório Fotográfico"
    app:srcCompat="@drawable/ic_picture_as_pdf" />
```

---

## Data Flow

### Fluxo 1: Usuário ativa filtro "Sem Etiqueta"

```
chipSemEtiqueta.isChecked = true
    → preferencesManager.getUserProfile() → "SUPERVISOR"
    → fabRelatorioFoto.visibility = VISIBLE
    → preferencesManager.getInventarioAtivoId() → 3
    → viewModel.carregarInfoRelatorioFoto(3)
        → RelatorioFotoState.Loading
        → BuscarInfoRelatorioFotoUseCase(3)
            → GET /api/mobile/relatorios/fotos/3/info
            → { semEtiqueta: 12, totalFotos: 15, ... }
        → RelatorioFotoState.InfoCarregada(info)
    → atualizarBadgeFotos(12)
    → tvFotosBadge.text = "12 foto(s) disponível(is)"
```

### Fluxo 2: Usuário aciona o FAB de download

```
fabRelatorioFoto.onClick
    → preferencesManager.getInventarioAtivoId() → 3
    → viewModel.baixarRelatorioFoto(3)
        → RelatorioFotoState.Downloading
        → fabRelatorioFoto.isEnabled = false
        → progressBarRelatorio.visibility = VISIBLE
        → BaixarRelatorioFotoPdfUseCase(3)
            → GET /api/mobile/relatorios/fotos/3?tipo=sem_etiqueta
            → HTTP 200, Content-Type: application/pdf, ~500 KB
            → grava bytes em cacheDir/relatorio_sem_etiqueta_3_1717000000000.pdf
        → RelatorioFotoState.PdfPronto(arquivo)
    → abrirPdfNativo(arquivo)
        → FileProvider.getUriForFile(...)
        → Intent(ACTION_VIEW, uri, "application/pdf")
        → startActivity(intent)
    → viewModel.limparEstadoRelatorioFoto()
```

### Fluxo 3: Servidor retorna 204 (sem fotos)

```
BaixarRelatorioFotoPdfUseCase
    → response.code() == 204
    → Result.failure(SemFotosException())
→ RelatorioFotoState.Erro("Nenhuma foto disponível para gerar o relatório")
→ Toast.makeText(...)
```

---

## Error Handling

| Cenário | Código HTTP / Exceção | Mensagem ao usuário |
|---|---|---|
| Sem fotos disponíveis | HTTP 204 / `SemFotosException` | "Nenhuma foto disponível para gerar o relatório" |
| Sem permissão | HTTP 403 / `PermissaoNegadaException` | "Você não tem permissão para acessar este relatório" |
| Erro de rede / timeout | `IOException`, `SocketTimeoutException` | "Erro de conexão. Verifique a rede e tente novamente" |
| Sem app PDF no dispositivo | `ActivityNotFoundException` | "Nenhum visualizador de PDF encontrado. Instale um app para abrir PDFs" |
| Sem inventário ativo | `inventarioId == null` | "Nenhum inventário ativo encontrado" |
| Erro genérico do servidor | HTTP 5xx | "Erro de conexão. Verifique a rede e tente novamente" |
| Falha ao buscar info | Qualquer exceção | "Não foi possível carregar informações de fotos" |

---

## Security

- O controle de acesso ao endpoint de PDF é feito pelo servidor (`@RequireSupervisor`). O app apenas oculta o botão para roles insuficientes como medida de UX, não como controle de segurança.
- O `FileProvider` usa `android:exported="false"` e `FLAG_GRANT_READ_URI_PERMISSION` para garantir que o arquivo só seja acessível pelo app de PDF escolhido pelo usuário, sem expor o cache do app.
- O token JWT é adicionado automaticamente pelo `authInterceptor` já configurado no `OkHttpClient`, sem necessidade de alteração.

---

## Files to Create / Modify

### Novos arquivos

| Arquivo | Tipo |
|---|---|
| `data/remote/api/RelatorioFotoApi.kt` | Interface Retrofit |
| `domain/model/RelatorioFotoInfo.kt` | Modelo de domínio |
| `domain/model/RelatorioFotoExceptions.kt` | Exceções de domínio |
| `domain/usecase/BuscarInfoRelatorioFotoUseCase.kt` | Use Case |
| `domain/usecase/BaixarRelatorioFotoPdfUseCase.kt` | Use Case |
| `presentation/state/RelatorioFotoState.kt` | UI State |
| `res/xml/file_provider_paths.xml` | Config FileProvider (se não existir) |

### Arquivos modificados

| Arquivo | Mudança |
|---|---|
| `di/ApiModule.kt` | Adicionar `provideRelatorioFotoApi()` |
| `presentation/coleta/CollectionViewViewModelClean.kt` | Adicionar novos parâmetros, StateFlow e métodos |
| `presentation/coleta/CollectionViewActivity.kt` | Observar `relatorioFotoState`, FAB, badge, `onDestroy` |
| `res/layout/activity_collection_view.xml` | Adicionar `tvFotosBadge`, `progressBarRelatorio`, `fabRelatorioFoto` |
| `AndroidManifest.xml` | Declarar `FileProvider` (se não existir) |

---

## Correctness Properties

### Análise de Testabilidade dos Critérios de Aceitação

**1.1 — Chamada ao `/info` ao ativar filtro Sem Etiqueta**
- Testável como exemplo: verificar que `BuscarInfoRelatorioFotoUseCase` é chamado com o `inventarioId` correto quando o filtro é ativado.

**1.2 — Exibição da contagem de fotos**
- Testável como exemplo: dado `RelatorioFotoInfo(semEtiqueta=5)`, verificar que `tvFotosBadge.text` contém "5".

**1.3 — Mensagem quando semEtiqueta = 0**
- Testável como exemplo (edge case): dado `RelatorioFotoInfo(semEtiqueta=0)`, verificar mensagem "Nenhum item sem etiqueta possui foto registrada".

**2.1 / 2.2 — Visibilidade do FAB por role**
- Testável como propriedade: para qualquer perfil em `{SUPERVISOR, ADMIN}`, FAB deve ser visível; para qualquer perfil em `{COLETOR, CONSULTA}`, FAB deve ser oculto.

**3.2 — Arquivo salvo no cache com nome correto**
- Testável como exemplo: verificar que o arquivo existe em `cacheDir` e o nome começa com `relatorio_sem_etiqueta_{inventarioId}_`.

**3.4 / 3.5 / 3.6 — Tratamento de erros HTTP**
- Testável como exemplos: mockar respostas 204, 403 e IOException e verificar o estado `RelatorioFotoState.Erro` com a mensagem correta.

**5.5 — Gravação com buffer de 8 KB**
- Não testável diretamente como propriedade (detalhe de implementação interno).

**8.1 — Limpeza de cache em onDestroy**
- Testável como exemplo: criar arquivo `relatorio_sem_etiqueta_*` no cache, chamar `onDestroy`, verificar que o arquivo foi deletado.

### Propriedades de Corretude

#### Propriedade 1 — Mapeamento de role para visibilidade do FAB

Para qualquer valor de `perfil` retornado por `PreferencesManager.getUserProfile()`:

```
perfil ∈ {"SUPERVISOR", "ADMIN"} → fabRelatorioFoto.visibility == VISIBLE (quando filtro ativo)
perfil ∉ {"SUPERVISOR", "ADMIN"} → fabRelatorioFoto.visibility == GONE
```

Esta é uma propriedade de invariante: independentemente de outros estados da tela, a visibilidade do FAB deve ser determinada exclusivamente pelo perfil do usuário e pelo estado do filtro.

#### Propriedade 2 — Idempotência do download

Chamar `BaixarRelatorioFotoPdfUseCase(inventarioId)` múltiplas vezes deve sempre produzir um arquivo válido no cache (sobrescrevendo o anterior), sem acumular arquivos duplicados para o mesmo `inventarioId` na mesma sessão.

#### Propriedade 3 — Transição de estados do RelatorioFotoState

O `relatorioFotoState` deve seguir as transições válidas:

```
Idle → Loading → InfoCarregada | Erro
Idle → Downloading → PdfPronto | Erro
PdfPronto → Idle (após limparEstadoRelatorioFoto)
Erro → Idle (após limparEstadoRelatorioFoto)
```

Nunca deve ocorrer a transição direta `Downloading → InfoCarregada` ou `Loading → PdfPronto`.

#### Propriedade 4 — Consistência entre info e download

Se `RelatorioFotoInfo.semEtiqueta > 0`, então uma chamada subsequente a `BaixarRelatorioFotoPdfUseCase` para o mesmo `inventarioId` não deve retornar `SemFotosException` (assumindo que nenhuma foto foi removida entre as duas chamadas).

> Esta propriedade é verificável em testes de integração com o servidor real, não em testes unitários com mocks.
