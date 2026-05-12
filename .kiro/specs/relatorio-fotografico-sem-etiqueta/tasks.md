# Implementation Plan: Relatório Fotográfico — Itens Sem Etiqueta

## Overview

Integração dos endpoints de relatório fotográfico já existentes no servidor ao app Android (`InventarioMobile/`), seguindo Clean Architecture + MVVM + Hilt. Nenhum endpoint de servidor será criado ou modificado. Toda a implementação é exclusivamente no módulo Android.

**Linguagem:** Kotlin  
**Padrão:** Clean Architecture + MVVM + Hilt + Coroutines + StateFlow + ViewBinding  
**Regra crítica:** Todas as URLs Retrofit devem usar o prefixo `api/mobile/` completo (ver `endpoints-nao-alterar.md`).

> **Nota sobre FileProvider:** O projeto já possui um `FileProvider` declarado no `AndroidManifest.xml` com authority `${applicationId}.fileprovider` e apontando para `@xml/file_paths`. O arquivo `file_paths.xml` já contém `<cache-path name="cache" path="exports/" />`, mas o path `"exports/"` não cobre a raiz do `cacheDir`. As tarefas abaixo contemplam a atualização do `file_paths.xml` existente (adicionando `<cache-path name="cache_root" path="." />`) e o uso da authority já existente (`${applicationId}.fileprovider`) em vez de criar um novo FileProvider.

---

## Tasks

- [ ] 1. Criar os artefatos da camada de dados (API Retrofit + DI)
  - [ ] 1.1 Criar `RelatorioFotoApi.kt`
    - Criar o arquivo em `data/remote/api/RelatorioFotoApi.kt`
    - Declarar interface com dois métodos:
      - `buscarInfo(@Path inventarioId: Int): ApiResponse<Map<String, Any>>` anotado com `@GET("api/mobile/relatorios/fotos/{inventarioId}/info")`
      - `baixarPdf(@Path inventarioId: Int, @Query("tipo") tipo: String = "sem_etiqueta"): Response<ResponseBody>` anotado com `@Streaming` e `@GET("api/mobile/relatorios/fotos/{inventarioId}")`
    - Importar `okhttp3.ResponseBody`, `retrofit2.Response`, `retrofit2.http.*`
    - _Requisitos: 4.1, 4.2, 4.4_

  - [ ] 1.2 Registrar `RelatorioFotoApi` no `ApiModule.kt`
    - Abrir `di/ApiModule.kt`
    - Adicionar import de `RelatorioFotoApi`
    - Adicionar ao final do objeto `ApiModule` o provider:
      ```kotlin
      @Provides
      @Singleton
      fun provideRelatorioFotoApi(retrofit: Retrofit): RelatorioFotoApi {
          return retrofit.create(RelatorioFotoApi::class.java)
      }
      ```
    - _Requisitos: 4.3_

- [ ] 2. Criar os artefatos da camada de domínio
  - [ ] 2.1 Criar `RelatorioFotoInfo.kt`
    - Criar o arquivo em `domain/model/RelatorioFotoInfo.kt`
    - Declarar `data class RelatorioFotoInfo` com campos: `inventarioId: Int`, `totalFotos: Int`, `semEtiqueta: Int`, `patrimonio: Int`, `divergencia: Int`, `temFotos: Boolean`
    - Sem dependências Android (Kotlin puro)
    - _Requisitos: 1.2, 1.3_

  - [ ] 2.2 Criar `RelatorioFotoExceptions.kt`
    - Criar o arquivo em `domain/model/RelatorioFotoExceptions.kt`
    - Declarar `class SemFotosException : Exception("Nenhuma foto disponível para gerar o relatório")`
    - Declarar `class PermissaoNegadaException : Exception("Você não tem permissão para acessar este relatório")`
    - _Requisitos: 5.3, 5.4, 3.4, 3.5_

  - [ ] 2.3 Criar `BuscarInfoRelatorioFotoUseCase.kt`
    - Criar o arquivo em `domain/usecase/BuscarInfoRelatorioFotoUseCase.kt`
    - Anotar com `@Inject constructor` recebendo `RelatorioFotoApi`
    - Implementar `suspend operator fun invoke(inventarioId: Int): Result<RelatorioFotoInfo>`
    - Mapear os campos `totalFotos`, `semEtiqueta`, `patrimonio`, `divergencia`, `temFotos` do `Map<String, Any>` retornado pela API usando cast seguro `(data["campo"] as? Number)?.toInt() ?: 0`
    - Retornar `Result.failure(Exception(response.message))` se `response.success == false`
    - Envolver toda a lógica em `try/catch` retornando `Result.failure(e)` em caso de exceção
    - _Requisitos: 5.1_

  - [ ] 2.4 Criar `BaixarRelatorioFotoPdfUseCase.kt`
    - Criar o arquivo em `domain/usecase/BaixarRelatorioFotoPdfUseCase.kt`
    - Anotar com `@Inject constructor` recebendo `RelatorioFotoApi` e `@ApplicationContext context: Context`
    - Implementar `suspend operator fun invoke(inventarioId: Int): Result<File>`
    - Tratar `response.code() == 204` retornando `Result.failure(SemFotosException())`
    - Tratar `response.code() == 403` retornando `Result.failure(PermissaoNegadaException())`
    - Construir o nome do arquivo como `"relatorio_sem_etiqueta_${inventarioId}_${System.currentTimeMillis()}.pdf"` dentro de `context.cacheDir`
    - Gravar os bytes usando `arquivo.outputStream().buffered(8 * 1024).use { out -> body.byteStream().use { input -> input.copyTo(out) } }` para evitar OOM em PDFs grandes (Req 5.5)
    - Sobrescrever arquivo existente se já houver um com o mesmo nome (Req 8.2)
    - Envolver toda a lógica em `try/catch` retornando `Result.failure(e)` em caso de exceção
    - _Requisitos: 5.2, 5.3, 5.4, 5.5, 3.2, 8.2_

- [ ] 3. Criar o UI State e estender o ViewModel
  - [ ] 3.1 Criar `RelatorioFotoState.kt`
    - Criar o arquivo em `presentation/state/RelatorioFotoState.kt`
    - Declarar `sealed class RelatorioFotoState` com os subtipos:
      - `object Idle`
      - `object Loading`
      - `data class InfoCarregada(val info: RelatorioFotoInfo)`
      - `object Downloading`
      - `data class PdfPronto(val arquivo: File)`
      - `data class Erro(val mensagem: String)`
    - _Requisitos: 6.1_

  - [ ] 3.2 Estender `CollectionViewViewModelClean.kt`
    - Abrir `presentation/coleta/CollectionViewViewModelClean.kt`
    - Adicionar os dois novos parâmetros ao construtor `@Inject`:
      - `private val buscarInfoRelatorioFotoUseCase: BuscarInfoRelatorioFotoUseCase`
      - `private val baixarRelatorioFotoPdfUseCase: BaixarRelatorioFotoPdfUseCase`
    - Adicionar o novo `StateFlow`:
      ```kotlin
      private val _relatorioFotoState = MutableStateFlow<RelatorioFotoState>(RelatorioFotoState.Idle)
      val relatorioFotoState: StateFlow<RelatorioFotoState> = _relatorioFotoState.asStateFlow()
      ```
    - Implementar `fun carregarInfoRelatorioFoto(inventarioId: Int)`: emitir `Loading`, chamar o use case, emitir `InfoCarregada` no sucesso ou `Erro("Não foi possível carregar informações de fotos")` na falha
    - Implementar `fun baixarRelatorioFoto(inventarioId: Int)`: emitir `Downloading`, chamar o use case, emitir `PdfPronto` no sucesso ou `Erro` com mensagem específica por tipo de exceção (`SemFotosException`, `PermissaoNegadaException`, outros)
    - Implementar `fun limparEstadoRelatorioFoto()`: emitir `Idle`
    - Adicionar os imports necessários
    - _Requisitos: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6_

- [ ] 4. Atualizar o layout e configurar o FileProvider
  - [ ] 4.1 Atualizar `activity_collection_view.xml`
    - Abrir `res/layout/activity_collection_view.xml`
    - Adicionar `TextView` com `android:id="@+id/tvFotosBadge"`, `android:visibility="gone"`, `android:textSize="12sp"`, `android:drawableStart="@drawable/ic_photo"` (ou ícone equivalente disponível no projeto), `android:drawablePadding="4dp"`, `android:padding="8dp"`
    - Adicionar `ProgressBar` com `android:id="@+id/progressBarRelatorio"`, `android:visibility="gone"`, `android:layout_width="wrap_content"`, `android:layout_height="wrap_content"`
    - Adicionar `FloatingActionButton` com `android:id="@+id/fabRelatorioFoto"`, `android:visibility="gone"`, `android:contentDescription="Baixar Relatório Fotográfico"`, `app:srcCompat="@drawable/ic_picture_as_pdf"` (ou ícone PDF disponível no projeto)
    - Posicionar o FAB de forma que não sobreponha o conteúdo principal (usar `ConstraintLayout` ou `CoordinatorLayout` conforme o layout existente)
    - _Requisitos: 2.1, 3.1, 3.8_

  - [ ] 4.2 Atualizar `file_paths.xml` para cobrir a raiz do `cacheDir`
    - Abrir `res/xml/file_paths.xml`
    - Adicionar a entrada `<cache-path name="cache_root" path="." />` para cobrir arquivos salvos diretamente em `context.cacheDir` (o entry existente `path="exports/"` não cobre a raiz)
    - Manter as entradas existentes intactas
    - _Requisitos: 7.4_

- [ ] 5. Integrar as novas funcionalidades na `CollectionViewActivity`
  - [ ] 5.1 Adicionar observador de `relatorioFotoState` na `CollectionViewActivity`
    - Abrir `presentation/coleta/CollectionViewActivity.kt`
    - Dentro do método `observeViewModel()` (ou equivalente), adicionar coleta do `viewModel.relatorioFotoState` usando `lifecycleScope.launch { viewModel.relatorioFotoState.collect { state -> ... } }`
    - Implementar o `when` para cada estado:
      - `Idle`: habilitar `fabRelatorioFoto`
      - `Loading`: nenhuma ação visual extra
      - `InfoCarregada`: chamar `atualizarBadgeFotos(state.info.semEtiqueta)`
      - `Downloading`: desabilitar `fabRelatorioFoto` (Req 3.8) e exibir `progressBarRelatorio`
      - `PdfPronto`: habilitar FAB, ocultar progress bar, chamar `abrirPdfNativo(state.arquivo)`, chamar `viewModel.limparEstadoRelatorioFoto()`
      - `Erro`: habilitar FAB, ocultar progress bar, exibir `Toast.makeText(...)` com `state.mensagem`, chamar `viewModel.limparEstadoRelatorioFoto()`
    - _Requisitos: 6.1, 3.3, 3.4, 3.5, 3.6, 3.8_

  - [ ] 5.2 Implementar lógica do chip "Sem Etiqueta" para FAB e badge
    - Dentro de `setupFilters()` (ou onde o chip "Sem Etiqueta" é configurado), adicionar `setOnCheckedChangeListener` para o chip:
      - Quando `isChecked == true`:
        - Ler `preferencesManager.getUserProfile()` e verificar se é `"SUPERVISOR"` ou `"ADMIN"`
        - Definir `fabRelatorioFoto.visibility` como `VISIBLE` para SUPERVISOR/ADMIN ou `GONE` para outros perfis (Req 2.1, 2.2)
        - Se `preferencesManager.getInventarioAtivoId() != null`, chamar `viewModel.carregarInfoRelatorioFoto(inventarioId)` (Req 1.1, 1.5)
        - Se `inventarioId == null`, não chamar o endpoint e não exibir badge (Req 1.5)
      - Quando `isChecked == false`:
        - Definir `fabRelatorioFoto.visibility = View.GONE` (Req 2.3)
        - Definir `tvFotosBadge.visibility = View.GONE`
        - Chamar `viewModel.limparEstadoRelatorioFoto()`
    - _Requisitos: 1.1, 1.5, 2.1, 2.2, 2.3, 2.4_

  - [ ] 5.3 Implementar ação do FAB e método `abrirPdfNativo`
    - Adicionar `binding.fabRelatorioFoto.setOnClickListener`:
      - Ler `preferencesManager.getInventarioAtivoId()`
      - Se não nulo: chamar `viewModel.baixarRelatorioFoto(inventarioId)`
      - Se nulo: exibir `Toast` com "Nenhum inventário ativo encontrado"
    - Implementar método privado `abrirPdfNativo(arquivo: File)`:
      - Usar `FileProvider.getUriForFile(this, "${packageName}.fileprovider", arquivo)` — **usar a authority existente `fileprovider`**, não `provider`
      - Construir `Intent(Intent.ACTION_VIEW)` com `setDataAndType(uri, "application/pdf")` e `addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)` (Req 7.1, 7.2)
      - Envolver `startActivity(intent)` em `try/catch(ActivityNotFoundException)` exibindo Toast com "Nenhum visualizador de PDF encontrado. Instale um app para abrir PDFs" (Req 7.3, 3.7)
    - _Requisitos: 3.1, 3.3, 7.1, 7.2, 7.3_

  - [ ] 5.4 Implementar `atualizarBadgeFotos` e limpeza de cache em `onDestroy`
    - Implementar método privado `atualizarBadgeFotos(quantidade: Int)`:
      - Se `quantidade > 0`: definir `tvFotosBadge.text = "$quantidade foto(s) disponível(is)"` e `visibility = VISIBLE` (Req 1.2)
      - Se `quantidade == 0`: definir `tvFotosBadge.text = "Nenhum item sem etiqueta possui foto registrada"` e `visibility = VISIBLE` (Req 1.3)
    - Sobrescrever `onDestroy()`:
      - Chamar `super.onDestroy()`
      - Deletar todos os arquivos em `cacheDir` cujo nome começa com `"relatorio_sem_etiqueta_"` usando `cacheDir.listFiles { file -> file.name.startsWith("relatorio_sem_etiqueta_") }?.forEach { it.delete() }` (Req 8.1)
    - _Requisitos: 1.2, 1.3, 8.1_

- [ ] 6. Checkpoint — Verificar compilação e integração
  - Garantir que o projeto compila sem erros (`.\gradlew.bat assembleDebug`)
  - Verificar que o Hilt gera corretamente o construtor do `CollectionViewViewModelClean` com os dois novos parâmetros
  - Verificar que não há referências quebradas nos imports
  - Perguntar ao usuário se há dúvidas antes de prosseguir

---

## Notes

- Tarefas marcadas com `*` são opcionais e podem ser puladas para um MVP mais rápido
- O design não inclui uma seção "Correctness Properties" com testes de propriedade formais para implementação automatizada; os testes descritos são exemplos e edge cases
- **Authority do FileProvider:** O projeto já usa `${applicationId}.fileprovider` — usar essa authority em `FileProvider.getUriForFile(...)`, não `${applicationId}.provider` como indicado no design
- **file_paths.xml:** Apenas adicionar `<cache-path name="cache_root" path="." />` ao arquivo existente; não substituir as entradas existentes
- **Ícones:** Verificar se `ic_photo` e `ic_picture_as_pdf` existem no projeto antes de referenciar; usar ícones equivalentes disponíveis se necessário
- Cada tarefa referencia requisitos específicos para rastreabilidade completa
- O servidor já implementa o controle de acesso via `@RequireSupervisor`; o app oculta o FAB apenas como medida de UX

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "2.1", "2.2"] },
    { "id": 1, "tasks": ["1.2", "2.3", "2.4", "3.1"] },
    { "id": 2, "tasks": ["3.2", "4.1", "4.2"] },
    { "id": 3, "tasks": ["5.1", "5.2", "5.3", "5.4"] }
  ]
}
```
