# Histórico de Builds - SiHCP Android

---

## Build #001 - 14/05/2026 - Fix câmera scan rápido

- **Tipo:** Release
- **Versão:** 2.29.0
- **Build Code:** 77
- **Arquivo:** `InventarioMobile/app/build/outputs/apk/release/SiHCP-release-v2.29.0.apk`
- **Tamanho:** 19.6 MB
- **Status:** ✅ Sucesso (2m 50s)

### Mudanças incluídas

**Fix: câmera do scan rápido cancelava imediatamente ao abrir**

- `ScannerActivity.kt` — removido `finish()` no callback de cancelamento do scanner; agora reseta o estado e reabre a câmera automaticamente (300ms delay). A tela só fecha se `isFinishing || isDestroyed`.
- `CustomCaptureActivity.kt` — adicionado catch específico para `RuntimeException` em `inicializarZoom()` e `aplicarZoom()`. A Camera API 1 pode lançar `RuntimeException` em Android 14+ quando a câmera está em transição, o que propagava para o `CaptureManager` e encerrava a activity com `RESULT_CANCELED`.
- `MainActivity.kt` — `nav_scanner` do Navigation Drawer agora redireciona para `SalaSelectionActivity` (com `COLETA_TIPO=QRCODE`) em vez de abrir `ScannerActivity` diretamente sem sala.

---

## Build #002 - 14/05/2026 - Fix câmera em loop (setTimeout(0))

- **Tipo:** Release
- **Versão:** 2.29.1
- **Build Code:** 78
- **Arquivo:** `InventarioMobile/app/build/outputs/apk/release/SiHCP-release-v2.29.1.apk`
- **Tamanho:** 19.6 MB
- **Status:** ✅ Sucesso (2m 27s)

### Causa raiz identificada

`setTimeout(0)` no `ScanOptions` passava o extra `TIMEOUT=0` para o `CaptureManager` do ZXing, que agendava `handler.postDelayed(returnResultTimeout, 0)` — executando imediatamente no próximo ciclo do handler e fechando a câmera com `RESULT_CANCELED` antes do preview iniciar. Isso causava o loop: câmera abre → fecha imediatamente → `barcodeLauncher` recebe null → (v2.29.0 tentava reabrir) → loop.

### Mudanças incluídas

- `ScannerActivity.kt` — removido `setTimeout(0)`. Não chamar `setTimeout` é o correto para câmera sem timeout: o `CaptureManager` só agenda o timer se o extra `TIMEOUT` existir no intent.
- `ScannerActivity.kt` — revertido o loop de reabrir câmera da v2.29.0 (voltou ao `finish()` original no cancelamento).
- `CustomCaptureActivity.kt` — mantida a correção de `RuntimeException` no zoom (Camera API 1).
- `MainActivity.kt` — mantida a correção do `nav_scanner` → `SalaSelectionActivity`.

---

## Build #003 - 15/05/2026 - Flash do scanner funcional

- **Tipo:** Release
- **Versão:** 2.29.2
- **Build Code:** 79
- **Arquivo:** `InventarioMobile/app/build/outputs/apk/release/SiHCP-release-v2.29.2.apk`
- **Tamanho:** 19.6 MB
- **Status:** ✅ Sucesso (6m 31s — R8 demorou mais por daemon frio)

### Mudanças incluídas

**Flash do scanner agora funcional no CustomCaptureActivity:**

- `ic_flash_on.xml` / `ic_flash_off.xml` — criados ícones de raio (Material Design) para representar o estado da lanterna
- `custom_capture.xml` — botão de flash agora usa `ic_flash_off` (ícone correto) em vez de `ic_notification` (sino)
- `CustomCaptureActivity.kt`:
  - `inicializarFlash()` — verifica `FEATURE_CAMERA_FLASH` ao iniciar o preview; oculta o botão se o dispositivo não tiver flash
  - `alternarFlash()` — troca o ícone entre `ic_flash_on`/`ic_flash_off` e atualiza `contentDescription`; trata `RuntimeException` da Camera API 1
  - `onPause()` — desliga a torch ao sair da tela para não deixar o LED aceso
  - `onResume()` — restaura a torch (com delay 500ms) se estava ligada antes do pause

---

## Build #004 - 18/05/2026 - Nomes completos no PDF (sem truncamento)

- **Tipo:** Release
- **Versão:** 2.29.3
- **Build Code:** 80
- **Arquivo:** `InventarioMobile/app/build/outputs/apk/release/SiHCP-release-v2.29.3.apk`
- **Tamanho:** 19.6 MB
- **Status:** ✅ Sucesso (5m 23s)

### Mudanças incluídas

**PdfGenerator.kt — truncamentos removidos:**
- `nomeResponsavel?.take(25)` → sem limite (iText quebra linha automaticamente)
- `localizacaoEncontrada?.take(30)` → sem limite
- `descricao?.take(50)` → sem limite
- Coluna "Responsável" ampliada de peso 1.4 para 1.8 para acomodar nomes longos

---

## Build #005 - 19/05/2026 - Correções coleta sem etiqueta + clareza de código

- **Tipo:** Release
- **Versão:** 2.29.3
- **Build Code:** 80
- **Arquivo:** `InventarioMobile/app/build/outputs/apk/release/SiHCP-release-v2.29.3.apk`
- **Tamanho:** 19.6 MB
- **Status:** ✅ Sucesso (~7m — R8 + clean build)

### Mudanças incluídas

**Bug fix — `ColetaRepositoryImpl.registrarColetaSemEtiqueta()`:**
- `estadoPatrimonio = coleta.status` → `estadoPatrimonio = coleta.estadoEncontrado ?: coleta.status`
- O estado de conservação (BOM, OCIOSO, etc.) estava sendo salvo no Room como `"COLETADO"` (status da coleta) em vez do valor real selecionado pelo usuário. A sincronização com o servidor já estava correta; apenas o banco local ficava com dado errado.

**Clareza de código — `MobileColetaRequest.kt`:**
- Campo `fotoPatrimonio: String?` renomeado para `caminhoFotoLocal: String?` com `@SerializedName("fotoPatrimonio")` mantendo compatibilidade JSON
- KDoc explica que o campo deve ser sempre `null` a partir da v2.21 (upload feito pelo `PhotoSyncWorker`)
- Todos os 7 usos no código atualizados: `ColetaRepositoryImpl.kt` (5×), `EnviarColetasPendentesUseCase.kt` (1×), `data/model/Coleta.kt` (1×)
- Comentário `// Foto enviada pelo PhotoSyncWorker` adicionado em cada uso


---

## Build #006 - 20/05/2026 - Correções relatório fotográfico + fotos não anexadas

- **Tipo:** Release
- **Versão:** 2.29.3
- **Build Code:** 80
- **Arquivo:** `InventarioMobile/app/build/outputs/apk/release/SiHCP-release-v2.29.3.apk`
- **Tamanho:** 19.6 MB
- **Status:** ✅ Sucesso (~5m)

### Problema 1 — Relatório fotográfico não carregava imagens

**Causa raiz:** O banco PostgreSQL não tinha a coluna `FOTO_PATH` na `TABELA_COLETA`. A coluna existente era `FOTO_PATRIMONIO` (campo legado Base64). O `ColetaDAO.atualizarFotoPath()` tentava fazer `UPDATE ... SET FOTO_PATH = ?` em uma coluna inexistente, causando falha silenciosa no upload. O `RelatorioFotoColetaDAO` também referenciava `c.FOTO_PATH` que não existia.

**Correções:**
- `sql/adicionar_foto_path_coleta.sql` — script de migration criado para adicionar a coluna `foto_path VARCHAR(500)` com índice
- `RelatorioFotoColetaDAO.java` — query atualizada para usar `COALESCE(c.FOTO_PATH, '')` e filtrar por `FOTO_PATH IS NOT NULL`; campo `observacao` corrigido para `COALESCE(c.OBSERVACAO_COLETA, c.OBSERVACAO, '')`
- `ColetaDAO.java` — comentário adicionado indicando dependência da migration

**⚠️ AÇÃO NECESSÁRIA NO SERVIDOR:** Executar o script `sql/adicionar_foto_path_coleta.sql` no banco PostgreSQL antes de reiniciar o servidor.

### Problema 2 — App parava de anexar fotos após algum tempo

**Causa raiz:** `PhotoHelper.cleanupOldPhotos()` apagava fotos do disco sem verificar se foram sincronizadas com o servidor. Quando havia mais de 200 fotos por tipo/inventário, as mais antigas eram removidas — mesmo que ainda pendentes de upload. O `PhotoSyncWorker` tentava fazer upload mas o arquivo não existia mais.

**Correções:**
- `PhotoHelper.kt` — `cleanupOldPhotos()` agora recebe `Set<String>` com caminhos das fotos já sincronizadas; só apaga arquivos que estão nesse conjunto; sem a lista, não apaga nada (comportamento seguro por padrão)
- `PhotoSyncWorker.kt` — coleta os caminhos sincronizados com sucesso em `caminhosSincronizados: MutableSet<String>` e passa para `cleanupOldPhotos()`; arquivo ausente no disco agora marca a foto como sincronizada para evitar loop de erros infinito


---

## Build #007 - 20/05/2026 - Fix câmera não carregava imagem no app

- **Tipo:** Release
- **Versão:** 2.29.3
- **Build Code:** 80
- **Arquivo:** `InventarioMobile/app/build/outputs/apk/release/SiHCP-release-v2.29.3.apk`
- **Tamanho:** 19.6 MB
- **Status:** ✅ Sucesso (~5m 38s)

### Mudança

**`ItemSemEtiquetaActivity.kt` — bug no `handleFotoCapturada`:**

O código anterior tentava encontrar o arquivo da câmera buscando o arquivo mais recente em `getExternalFilesDir(Pictures)`:
```kotlin
// ANTES (bug) — ignora o arquivo real e busca o mais recente na pasta
val arquivoOriginal = uri.path?.let { caminho ->
    val storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
    storageDir?.listFiles()?.maxByOrNull { it.lastModified() }
}
```

Isso era frágil: `uri.path` retorna o path virtual do FileProvider (`/external_files/Pictures/...`), não o caminho real no disco. Se houvesse qualquer outro arquivo mais recente na pasta, o código pegava o arquivo errado — resultando em "Foto não foi capturada" ou preview incorreto.

**Correção:** adicionado campo `fotoArquivoTemp: File?` que guarda a referência direta ao arquivo criado em `abrirCamera()`. O `handleFotoCapturada` usa essa referência diretamente, sem inferência pelo URI.

```kotlin
// DEPOIS (correto) — usa referência direta ao arquivo criado
private var fotoArquivoTemp: File? = null

private fun abrirCamera() {
    val tempFile = File.createTempFile(...)
    fotoArquivoTemp = tempFile  // ← guardar referência
    fotoUri = FileProvider.getUriForFile(this, ..., tempFile)
    takePictureLauncher.launch(fotoUri)
}

private fun handleFotoCapturada(uri: Uri) {
    val arquivoOriginal = fotoArquivoTemp  // ← usar referência direta
    ...
}
```


---

## Build #008 - 20/05/2026 - Fix process death + foto perdida ao retornar da câmera

- **Tipo:** Release
- **Versão:** 2.29.3
- **Build Code:** 80
- **Arquivo:** `InventarioMobile/app/build/outputs/apk/release/SiHCP-release-v2.29.3.apk`
- **Tamanho:** 19.57 MB
- **Status:** ✅ Sucesso (3m 44s)

### Sintoma reportado

Após pressionar o botão da câmera em `ItemSemEtiquetaActivity`, a foto não aparecia no preview da tela de cadastro. O fix da Build #007 (referência direta ao `File`) resolveu o caso ideal, mas falhava se o sistema matava o processo enquanto a câmera estava em foreground — comportamento comum em dispositivos com pouca memória ao abrir o app de câmera nativo, que consome muita RAM.

### Causa raiz

**Process death:** Android pode matar a `ItemSemEtiquetaActivity` enquanto o app de câmera está em foreground para liberar memória. Quando o usuário tira a foto e retorna, o sistema recria a Activity, mas o campo `fotoArquivoTemp: File?` é perdido (não estava no `savedInstanceState`). Resultado: `handleFotoCapturada` recebia o URI mas `fotoArquivoTemp` era `null` → foto descartada.

### Correções

**`ItemSemEtiquetaActivity.kt`:**

- Companion object com 4 chaves de estado: `STATE_FOTO_ARQUIVO_TEMP`, `STATE_FOTO_URI`, `STATE_FOTO_PATH`, `STATE_FOTO_THUMB_PATH`
- `onSaveInstanceState()` persiste o caminho absoluto do `fotoArquivoTemp` (e demais campos de foto)
- `onCreate(savedInstanceState)` restaura os campos antes do setup da UI; o preview é re-exibido se `fotoPath != null` após a restauração
- `handleFotoCapturada()` tornado tolerante a process death:
  - Se `fotoArquivoTemp` for `null` ou inválido, valida o `URI` recebido via `ContentResolver.openInputStream(uri)` antes de tentar processar
  - Logs detalhados em cada etapa para facilitar diagnóstico
- Logs adicionados em `takePictureLauncher`, `abrirCamera()` e `handleFotoCapturada()`

**`PhotoHelper.kt`:**

- Logs em `compressAndSavePhoto` (bounds detectados, bitmap decodificado, falhas explícitas) para facilitar debug em produção

### Como testar

1. Abrir cadastro de item sem etiqueta
2. Pressionar botão da câmera, tirar uma foto
3. Voltar ao app — a foto deve aparecer no preview imediatamente
4. Para simular process death: ativar "Não manter atividades" em Opções do Desenvolvedor antes do passo 2

---

## Build #009 - 20/05/2026 - Hotfix: PhotoHelper sempre falhava ao processar foto

- **Tipo:** Release
- **Versão:** 2.29.3
- **Build Code:** 80
- **Arquivo:** `InventarioMobile/app/build/outputs/apk/release/SiHCP-release-v2.29.3.apk`
- **Tamanho:** 19.57 MB
- **Status:** ✅ Sucesso (2m 15s)

### Bug introduzido na Build #007

Para diagnosticar o problema da câmera (Tasks 6/7), foram adicionados logs e validações
em `PhotoHelper.compressAndSavePhoto()` na Build #007. Uma das validações estava errada:

```kotlin
// ❌ BUG — decodeStream(input, null, options) com inJustDecodeBounds=true
//     SEMPRE retorna null por design (apenas preenche options.outWidth/outHeight)
context.contentResolver.openInputStream(sourceUri)?.use { input ->
    BitmapFactory.decodeStream(input, null, options)
} ?: run {
    Log.e(TAG, "openInputStream retornou null no decode de bounds")
    return null   // ← sempre executava → função sempre falhava
}
```

Resultado: a partir da Build #007, **toda foto retornada pela câmera falhava** com
"Erro ao processar foto" / "Falha ao processar foto. Tente novamente." mesmo com
o arquivo válido no disco.

### Correção

`PhotoHelper.kt` — separação clara das duas fases do decode:

1. **Decode de bounds (`inJustDecodeBounds=true`)**: validar apenas o `InputStream`,
   nunca o retorno do `decodeStream` (que é `null` por design nesta fase). Após o decode,
   verificar `options.outWidth/outHeight > 0` para detectar arquivo corrompido.
2. **Decode real (`inJustDecodeBounds=false`)**: aqui sim o `decodeStream` retorna o
   `Bitmap` ou `null` — validação Elvis funciona corretamente.

### Impacto

- ✅ Volta a salvar fotos no fluxo "Item Sem Etiqueta"
- ✅ Volta a salvar fotos no fluxo de coleta de patrimônio (mesmo método)
- ✅ Mantém todas as melhorias de process death da Build #008 (state save/restore)
- ✅ Logs de debug de bounds/bitmap continuam ativos para futuro diagnóstico

---

## Build #010 - 20/05/2026 - Fix: foto chegava ao servidor horas depois + servidorId não gravado

- **Tipo:** Release
- **Versão:** 2.29.3
- **Build Code:** 80
- **Arquivo:** `InventarioMobile/app/build/outputs/apk/release/SiHCP-release-v2.29.3.apk`
- **Tamanho:** 19.57 MB
- **Status:** ✅ Sucesso (~5m 15s)

### Problemas corrigidos

#### Problema 1 — `servidorId` não era gravado após sync da coleta

Nos blocos de sincronização assíncrona de `registrarColeta()` e `registrarColetaSemEtiqueta()`,
ao receber `response.success = true`, o código chamava apenas `coletaDao.marcarSincronizada(id)`,
descartando o `servidorId` retornado pelo servidor.

O `PhotoSyncWorker` verifica `coleta.servidorId != null && servidorId > 0` antes de enviar a foto.
Sem o `servidorId`, a foto ficava bloqueada indefinidamente — nunca era enviada.

**Correção:** todos os blocos de sync bem-sucedido agora extraem `response.data?.id` e chamam
`coletaDao.marcarSincronizadaComServidor(id, servidorId)` quando o ID está disponível.

#### Problema 2 — Worker de fotos rodava apenas a cada 6h em Wi-Fi

O `PhotoSyncWorker.schedule()` usava `NetworkType.UNMETERED` (apenas Wi-Fi) e intervalo de 6 horas.
Em campo, onde a conexão é geralmente dados móveis, a foto nunca era enviada pelo worker periódico.

**Correção:**
- Intervalo reduzido de **6 horas → 15 minutos**
- Rede alterada de **Wi-Fi apenas → qualquer rede conectada** (`NetworkType.CONNECTED`)

#### Problema 3 — Sem trigger imediato após sync da coleta

Mesmo com o worker periódico, havia um delay de até 15 minutos entre a coleta ser sincronizada
e a foto ser enviada.

**Correção:** adicionado `PhotoSyncWorker.scheduleOneShot(context)` chamado imediatamente após
`marcarSincronizadaComServidor()` — dispara um one-shot com delay de 2s para o banco estabilizar.
Isso garante que a foto chegue ao servidor em segundos após a coleta ser confirmada.

### Fluxo após a correção

```
Usuário clica "Registrar"
  → Coleta salva no Room (imediato)
  → Coroutine assíncrona: POST api/mobile/coletas
      → response.success=true + servidorId=523
      → marcarSincronizadaComServidor(id, 523)   ← NOVO
      → PhotoSyncWorker.scheduleOneShot()         ← NOVO (2s delay)
          → POST api/mobile/fotos/upload
          → FOTO_PATH gravado no banco PostgreSQL
          → Relatório fotográfico já mostra a foto
```

### Arquivos modificados

- `ColetaRepositoryImpl.kt` — 3 blocos de sync corrigidos + `applicationContext` injetado + import `PhotoSyncWorker`
- `PhotoSyncWorker.kt` — `scheduleOneShot()` adicionado, intervalo 6h→15min, Wi-Fi→qualquer rede

---

## Build #011 - 20/05/2026 - Foto visível na coleta + relatório fotográfico funcional

- **Tipo:** Release
- **Versão:** 2.29.3
- **Build Code:** 80
- **Arquivo:** `InventarioMobile/app/build/outputs/apk/release/SiHCP-release-v2.29.3.apk`
- **Tamanho:** 19.57 MB
- **Status:** ✅ Sucesso (~6m — clean build)

### Problemas corrigidos

#### 1. Foto não aparecia ao visualizar coleta no app

**Causa:** `ColetasUnificadaActivity` só tentava carregar a foto do disco local. Se o arquivo não existia (outro dispositivo, app reinstalado, limpeza de cache), mostrava apenas texto "Foto enviada ao servidor".

**Correção:**
- Quando o arquivo local não existe mas `temFoto=true` ou `fotoUrl` está preenchida, o app agora baixa a foto diretamente do servidor via `GET /api/mobile/fotos/{coletaId}` e exibe no dialog.
- Download feito em coroutine (não bloqueia a UI).

#### 2. `MobileColetaResponse` não incluía referência à foto

**Causa:** O DTO de resposta de coleta não tinha campos `fotoUrl` nem `temFoto`. O app não sabia que a coleta tinha foto no servidor.

**Correção (servidor):**
- `MobileColetaResponse.java` — adicionados campos `fotoUrl` e `temFoto`
- `MobileColetaService.java` — 2 métodos de conversão agora preenchem `fotoUrl = "api/mobile/fotos/{id}"` e `temFoto = true` quando `coleta.getFotoPath() != null`
- `Coleta.java` (model) — adicionado campo `fotoPath` com getter/setter
- `ColetaDAO.java` — `criarColetaFromResultSet()` agora lê a coluna `FOTO_PATH` do banco

**Correção (app Android):**
- `ColetaDto.kt` — adicionados campos `fotoUrl` e `temFoto`
- `MobileColetaResponseDto.kt` — adicionados campos `fotoUrl` e `temFoto`
- `data/model/Coleta.kt` — adicionados campos `fotoUrl` e `temFoto`, mapeados no `fromDto()`

#### 3. Relatório fotográfico (PDF/Excel) — já funcionava

O `RelatorioFotoPDFGenerator` e `RelatorioFotoExcelGenerator` já usavam `storageService.fotoExiste(fotoPath)` e `storageService.lerFoto(fotoPath)` corretamente. O relatório carrega as fotos do disco do servidor — desde que o arquivo exista em `data/fotos/...`, a foto aparece no PDF/Excel.

### Servidor atualizado

`SERVIDOR_MOBILE_PRODUCAO/sihcp-server.jar` — 20/05/2026 20:35 (Build #011 do servidor)
Reiniciar o servidor para aplicar as mudanças do `MobileColetaResponse`.
