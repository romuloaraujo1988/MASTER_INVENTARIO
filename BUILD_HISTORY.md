# Histórico de Builds - SiHCP Android

---

## Build #028 - 31/05/2026 21:28 - Correção de registro de divergências — Release

- **Tipo:** Release (assinado, R8 + shrinkResources)
- **Versão:** 2.29.7
- **Build Code:** 83
- **Arquivo APK:** `InventarioMobile/app/build/outputs/apk/release/SiHCP-release-v2.29.7.apk` (19.62 MB, 31/05 21:28)
- **Status:** ✅ Sucesso (`assembleRelease`, BUILD SUCCESSFUL em 5m 13s)

### Mudanças

- Correção do fluxo de **divergências de coleta** (item coletado diferente do
  cadastro — localização e/ou estado). As divergências eram calculadas e
  enviadas pelo app, mas o backend as descartava (DTO sem os campos +
  `setDivergencia(false)` hardcoded), gravando sempre `DIVERGENCIA=false` no
  PostgreSQL.
- **App:** `EnviarColetasPendentesUseCase` e `ColetaRepositoryImpl` (fluxo com
  etiqueta) passam a enviar `divergencia`/`motivoDivergencia` reais da coleta
  em vez de `false`/`null` hardcoded. Ramo de item sem etiqueta mantém `false`.
- **Servidor (sihcp-server, empacotado à parte):** `MobileColetaRequest`
  recebeu os campos de divergência; `MobileColetaService` agora **recalcula a
  divergência no servidor** (fonte de verdade) comparando sala/estado coletados
  vs cadastrados, com fallback para o valor do cliente.
- Backend empacotado: `sihcp-server/target/sihcp-server-2.9.0.jar` (fat JAR,
  profile `mobile`). **Deploy do servidor é necessário** para a correção ter
  efeito — o APK sozinho não resolve, pois a gravação é responsabilidade do
  servidor.

---

## Build #027 - 31/05/2026 14:59 - Dashboard Redesign (Android) — Release

- **Tipo:** Release (assinado, R8 + shrinkResources)
- **Versão:** 2.29.7
- **Build Code:** 83
- **Arquivo APK:** `InventarioMobile/app/build/outputs/apk/release/SiHCP-release-v2.29.7.apk` (19.62 MB, 31/05 14:59)
- **Status:** ✅ Sucesso (`assembleRelease`, BUILD SUCCESSFUL em 7m 13s)

### Mudanças

- Redesign completo da camada de apresentação do Dashboard (spec `dashboard-redesign`):
  toda a lógica de apresentação foi extraída para um helper puro e testável
  (`DashboardUiMapper`), e o `DashboardFragment` passou a apenas renderizar
  (Req 9.1 — sem regra de negócio na View).
- Novo `DashboardPresentationModels.kt` (`KpiValues`, `ChartPoint`,
  `DashboardScreenState`, `ChartAreaState`).
- Layout `fragment_dashboard.xml` reescrito: cabeçalho (saudação, nome do
  inventário, última sincronização), `LinearProgressIndicator` de conclusão,
  grade 2x2 de KPIs em cards neutros (sem gradiente), área de gráfico
  (`chartEvolucao` + estados loading/empty/error) e 4 botões de ação
  (Scan Rápido filled, demais outlined).
- Interações preservadas: pull-to-refresh, indicador offline (offline-first),
  FAB de voz com overlay e navegação dos 4 botões — sem alterar destinos nem as
  URLs `api/mobile/*`.
- Cobertura de testes: 13 arquivos (10 property tests jqwik ≥100 iterações,
  contraste, interações preservadas, re-render). 106 testes passando.

---

## Build #026 - 30/05/2026 03:16 - Atalhos de coleta na câmera ao vivo — Release


- **Tipo:** Release (assinado, R8 + shrinkResources)
- **Versão:** 2.29.7
- **Build Code:** 83
- **Arquivo APK:** `InventarioMobile/app/build/outputs/apk/release/SiHCP-release-v2.29.7.apk` (19.6 MB, 30/05 03:16)
- **Status:** ✅ Sucesso (`assembleRelease`)

### Mudanças

- Melhoria de fluidez da feature `atalhos-coleta-rapida`: os três atalhos
  (Sem etiqueta / Manual / Descrição) agora também aparecem na **tela de câmera
  ao vivo** (`CustomCaptureActivity` / `custom_capture.xml`), onde o coletor
  passa a maior parte do tempo de scan. Antes só existiam na `ScannerActivity`,
  ficando escondidos atrás da câmera durante o scan ativo (Req 1.3).
- Faixa de atalhos em estilo "pill" no rodapé, fora da mira central, reutilizando
  a mesma decisão pura `ColetaShortcutNavigator` (sem duplicar lógica).
- Navegação a partir da câmera NÃO chama `finish()`: ao voltar do método de
  coleta, o coletor retorna direto à câmera ao vivo, mantendo a sessão de scan
  contínua (ciclo de coleta sem interrupção).

---

## Build #025 - 29/05/2026 23:27 - Feature: atalhos de coleta rápida (Menu_Atalhos) — Release

- **Tipo:** Release (assinado, R8 + shrinkResources)
- **Versão:** 2.29.7
- **Build Code:** 83
- **Arquivo APK:** `InventarioMobile/app/build/outputs/apk/release/SiHCP-release-v2.29.7.apk` (19.6 MB, 29/05 23:27)
- **Status:** ✅ Sucesso (`assembleRelease`)

### Mudanças

- APK release da feature `atalhos-coleta-rapida` (Menu_Atalhos na `ScannerActivity`),
  já validada por property tests (P1–P4), testes unitários de borda e testes
  instrumentados de contrato de Intent/UI.
- Build minificada com R8 + `shrinkResources` (19.6 MB vs. 30.0 MB do debug).
  `lintVitalRelease` passou sem erros bloqueantes.

---

## Build #024 - 29/05/2026 16:54 - Feature: atalhos de coleta rápida (Menu_Atalhos)

- **Tipo:** Debug
- **Versão:** 2.29.7
- **Build Code:** 83
- **Arquivo APK:** `InventarioMobile/app/build/outputs/apk/debug/SiHCP-debug-v2.29.7.apk` (30.0 MB, 29/05 16:54)
- **Status:** ✅ Sucesso (`assembleDebug`)

### Mudanças

- Feature `atalhos-coleta-rapida`: adicionado o **Menu_Atalhos** à `ScannerActivity`
  com três chips de navegação (`chipAtalhoSemEtiqueta`, `chipAtalhoManual`,
  `chipAtalhoDescricao`) para abrir os métodos de coleta já existentes
  (`ItemSemEtiquetaActivity`, `ManualCollectionActivity`, `DescricaoSelectionActivity`)
  sem encerrar a sessão de scan, preservando a Sala_Atual.
- Novo pacote `presentation/scanner/navigation/` com a lógica pura de decisão
  (`ColetaShortcutNavigator` + modelos `ColetaShortcut`, `SalaContext`,
  `ScannedItemRef`, `ShortcutNavigationPlan`), seguindo Clean Architecture.

### Revisão de compatibilidade (steering `endpoints-nao-alterar.md`)

- Chaves de extra confirmadas idênticas às lidas pelas telas de destino:
  `extra_sala_id` (Long, `getLongExtra`) e `extra_sala_nome`
  (`ManualCollectionActivity`/`DescricaoSelectionActivity`); `DESCRICAO_SUGERIDA`
  e `NUMERO_PATRIMONIO_ORIGEM` (`ItemSemEtiquetaActivity`).
- Nenhuma URL de endpoint funcional foi alterada — feature é apenas de navegação;
  o pacote `data/remote/api` permanece intacto.

### Observação

- `testDebugUnitTest` falhou na compilação de um teste **pré-existente e não
  relacionado** a esta feature (`data/util/TextNormalizerPropertyTest.kt`,
  feature `coleta-descricao-livre-com-sugestao`: `Unresolved reference: next`,
  import Kotest ausente). Não alterado nesta tarefa. O source set principal
  compila e o `assembleDebug` conclui com sucesso.

---

## Build #023 - 29/05/2026 - FIX: monitor ao vivo não recebia coletas (R8/ProGuard)

- **Tipo:** Release (apenas app — servidor não precisa trocar)
- **Versão:** 2.29.7
- **Build Code:** 83
- **Arquivo APK:** `InventarioMobile/app/build/outputs/apk/release/SiHCP-release-v2.29.7.apk` (19.6 MB, 29/05 15:35)
- **Status:** ✅ Sucesso

### Causa raiz (confirmada via log do servidor)

O servidor estava transmitindo corretamente (log confirmou):
```
→ WS monitor: transmitindo coleta id=10796 inventarioId=7 sessõesAtivas=1
WS broadcast: bucket inv-7 tem 1 sessão(ões)
Broadcast WS coletas-live: 1 sessões alcançadas (contexto=inv-7)
```

Ou seja: handshake OK, app conectado (1 sessão no bucket inv-7), mensagem
enviada sem erro. O problema era **100% no app, no build release**.

O `ColetaEvento` (data class do feed WebSocket) está no pacote
`com.inventario.mobile.data.realtime`, criado nesta sessão. Esse pacote **não
estava** na lista de `-keep` do `proguard-rules.pro`. No release, o R8 ofuscava
a data class e o Gson desserializava com os campos nulos — incluindo
`tipo=null`. O `LiveColetaMonitorViewModel` descarta eventos cujo
`tipo != "COLETA_REGISTRADA"`, então a coleta era silenciosamente ignorada.

Em debug (sem R8) funcionaria; em release, não. Por isso o sintoma só apareceu
agora, na primeira release com o feed.

### Correção

- `proguard-rules.pro`: adicionado
  `-keep class com.inventario.mobile.data.realtime.** { *; }` e o
  `-keepclassmembers` correspondente.

### Deploy

- **Servidor:** NÃO precisa trocar. O JAR 2.9.0 em produção já transmite certo.
  (Os logs de diagnóstico `→ WS monitor:` / `WS broadcast:` ficam no servidor
  atual e ajudam em futuras validações — nível INFO, sem impacto.)
- **App:** instalar `SiHCP-release-v2.29.7.apk`.

---

## Build #022 - 29/05/2026 - Monitor ao vivo: causa raiz real (ServerContainer JSR-356)

- **Tipo:** Release + Servidor
- **Versão:** 2.29.6
- **Build Code:** 82
- **Arquivo APK:** `InventarioMobile/app/build/outputs/apk/release/SiHCP-release-v2.29.6.apk` (19.6 MB, 29/05 14:40)
- **Arquivo Servidor:** `SISTEMA_INVENTARIO_PRODUCAO/sihcp-server.jar` (2.9.0, 29/05 13:44)
- **Status:** ✅ Sucesso

### Causa raiz definitiva do monitor ao vivo

As correções anteriores (path `/ws-coletas-live`, `@EnableWebSocket`) eram
necessárias, mas o bloqueio real era outro: o `application-mobile.properties`
**excluía a `WebSocketServletAutoConfiguration`**:

```properties
spring.autoconfigure.exclude=\
  ...,\
  org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration
```

Essa auto-config registra o `WsSci` do Tomcat, que cria o
`jakarta.websocket.server.ServerContainer` (JSR-356). Sem ela:
- STOMP (dashboard web) funcionava → usa SockJS sobre HTTP, não precisa do ServerContainer
- WebSocket nativo (`/ws-coletas-live`, monitor ao vivo) falhava no handshake
  com "No ServerContainer ServletContext attribute" → status "Erro" no app

### Mudanças (servidor)

- `application-mobile.properties` (sihcp-server + raiz): removida a exclusão da
  `WebSocketServletAutoConfiguration`. Mantidas só as exclusões de JMX.
- Removido `TomcatWebSocketCustomizerConfig.java` — era um workaround manual de
  registro do `WsSci`; com a auto-config reativada, registraria o ServerContainer
  duas vezes.
- Também inclui a correção do `unaccent` (busca de sugestões) via `translate()`.

### Mudanças (app — incluídas pela primeira vez em release)

O APK 2.29.5 (08:30) foi compilado ANTES do commit que trouxe o cliente
WebSocket. Esta build inclui:
- `RealtimeColetasManager` + `CollectionViewActivity` em modo monitor ao vivo
- Botão de troca de câmera no scanner, som sutil no monitor, correção de
  vibração (DND/silencioso), responsável na coleta manual, botão de coleta nos
  detalhes e proteção contra coleta duplicada.

**IMPORTANTE:** requer servidor 2.9.0+ (correção do ServerContainer) E o APK
2.29.6. Ambos juntos para o monitor funcionar de ponta a ponta.

---

## Build #021 - 29/05/2026 - Correção do monitor ao vivo (colisão SockJS)

- **Tipo:** Release + Servidor
- **Versão:** 2.29.5
- **Build Code:** 81
- **Arquivo APK:** `InventarioMobile/app/build/outputs/apk/release/SiHCP-release-v2.29.5.apk`
- **Arquivo Servidor:** `sihcp-server/target/sihcp-server-2.9.0.jar` (29/05 08:18)
- **Status:** ✅ Sucesso

### Mudanças incluídas

#### Monitor ao vivo: "Erro" no status — CORRIGIDO

**Causa raiz (confirmada via log do servidor):**
```
WARN  ...DefaultSockJsService - "Invalid SockJS path '/coletas-live' - required to have 3 path segments"
DEBUG ...DefaultSockJsService - Ignoring transport request: GET .../ws/coletas-live?inventarioId=7
```

O endpoint STOMP da dashboard web (`/ws`) está registrado com `.withSockJS()`. O SockJS **captura todo o namespace `/ws/**`** e tenta interpretar `/ws/coletas-live` como um path SockJS (que exige 3 segmentos: `/ws/{server}/{session}/{transport}`). Como não bate, **ignora a requisição** — o handshake nunca chegava ao handler raw `ColetaLiveSocketHandler`.

**Solução:** mover o endpoint do feed para fora do namespace `/ws/`:
- Antes: `/ws/coletas-live`
- Depois: `/ws-coletas-live` (com hífen, não é subpath de `/ws/`)

**Arquivos alterados:**
- `WebSocketConfig.java` — handler registrado em `/ws-coletas-live`
- `WebSocketSecurityConfig.java` — `securityMatcher` inclui `/ws-coletas-live`
- `MobileSecurityConfig.java` — `requestMatchers("/ws-coletas-live")` permitAll
- `ColetaLiveSocketHandler.java` — comentário do endpoint atualizado
- `RealtimeColetasManager.kt` (app) — URL construída com `/ws-coletas-live`

**Nota técnica:** o comentário KDoc do `construirWsUrl` foi reescrito sem as sequências `/**` literais — em Kotlin, `/**` dentro de um bloco KDoc cria aninhamento de comentário e quebrava a compilação ("Unclosed comment").

**IMPORTANTE:** requer substituir o servidor (JAR de 29/05 08:18) E atualizar o APK. Ambos precisam do novo path para o monitor funcionar.

---

## Build #020 - 27/05/2026 - Proteção contra perda de dados em duplicatas offline

- **Tipo:** Release
- **Versão:** 2.29.5
- **Build Code:** 81
- **Arquivo:** `InventarioMobile/app/build/outputs/apk/release/SiHCP-release-v2.29.5.apk`
- **Tamanho:** 19.6 MB
- **Status:** ✅ Sucesso (3m 40s)

### Mudanças incluídas

#### Problema resolvido: perda silenciosa de dados em patrimônios duplicados

**Causa raiz:** quando o app encontrava um patrimônio já coletado (offline ou online), simplesmente escondia o botão "Coletar" sem dar nenhuma opção ao usuário e sem registrar a tentativa. Dados eram perdidos sem rastro.

**Solução:** dialog de decisão obrigatório (`setCancelable(false)`) com 3 opções:

1. **Forçar nova coleta** — sobrescreve o status local e registra uma nova coleta. Útil quando o dado local está desatualizado (ex.: coleta foi excluída no servidor mas ainda aparece como coletada offline)
2. **Registrar sem etiqueta** — abre `ItemSemEtiquetaActivity` pré-preenchida com a descrição do patrimônio original. Preserva o dado sem sobrescrever a coleta anterior
3. **Cancelar** — descarta, mas registra a tentativa no audit trail para rastreabilidade

**Audit trail completo:** toda tentativa de coletar um patrimônio já coletado é registrada no `AuditService`/`LogColetaEntity` independente da decisão do usuário:
- `DUPLICATA_DETECTADA` — ao abrir o dialog
- `COLETA_FORCADA` — se escolher forçar
- `CONVERTIDA_SEM_ETIQUETA` — se escolher sem etiqueta
- `TENTATIVA_CANCELADA` — se cancelar

**Arquivos modificados:**
- `data/local/entity/LogColetaEntity.kt` — 3 novos `AcaoLog`: `TENTATIVA_CANCELADA`, `COLETA_FORCADA`, `CONVERTIDA_SEM_ETIQUETA`
- `data/audit/AuditService.kt` — 3 novos métodos: `registrarTentativaCancelada`, `registrarColetaForcada`, `registrarConversaoSemEtiqueta`
- `presentation/scanner/ScannerViewModel.kt` — novo `forcarColeta()` que reseta `jaColetado` e delega para `coletarPatrimonioComEstado`
- `presentation/scanner/ScannerActivity.kt` — botão "Coletar" agora mostra "⚠ Já coletado — ver opções" em vez de sumir; novo `mostrarDialogDuplicata()`; injeção de `AuditService`
- `presentation/coleta/ManualCollectionViewModel.kt` — `coletarPatrimonio()` emite sinal `DUPLICATA:coletadoPor:data` em vez de retornar silenciosamente; novo `forcarColeta()` e `executarColeta()` privado
- `presentation/coleta/ManualCollectionActivity.kt` — intercepta sinal `DUPLICATA:` no `updateUI()` e exibe dialog; novo `mostrarDialogDuplicata()`; injeção de `AuditService`
- `presentation/coleta/ItemSemEtiquetaActivity.kt` — lê extras `DESCRICAO_SUGERIDA` e `NUMERO_PATRIMONIO_ORIGEM` para pré-preencher o campo de descrição quando vindo do dialog de duplicata

---

## Build #019 - 27/05/2026 - Botão "Coletar" na pesquisa rápida corrigido

- **Tipo:** Release
- **Versão:** 2.29.5
- **Build Code:** 81
- **Arquivo:** `InventarioMobile/app/build/outputs/apk/release/SiHCP-release-v2.29.5.apk`
- **Tamanho:** 19.6 MB
- **Status:** ✅ Sucesso (5m 15s)

### Mudanças incluídas

#### Botão "Coletar" nos detalhes da pesquisa rápida (CORRIGIDO)

**Causa raiz:** `PatrimonioDetailActivity.navegarParaColeta()` passava `"PATRIMONIO_ID"` e `"NUMERO_PATRIMONIO"` como extras, mas `ColetaActivity` lê `EXTRA_SALA_ID` e `EXTRA_SALA_NOME`. Os extras eram silenciosamente ignorados, resultando em:
- `salaId = -1` → log "ERRO: Sala não foi recebida corretamente!"
- `etCodigo` vazio → `btnSalvar` desabilitado
- Mesmo que o usuário escaneasse, `salvarColeta()` falhava com "Sala não selecionada"

**Correção em `PatrimonioDetailActivity`:**
- `navegarParaColeta()` agora passa `EXTRA_SALA_ID` e `EXTRA_SALA_NOME` extraídos do `patrimonioAtual` (já carregado na tela)
- Também passa o novo `EXTRA_PATRIMONIO_CODIGO` com o número do patrimônio

**Correção em `ColetaActivity`:**
- Novo `EXTRA_PATRIMONIO_CODIGO` declarado no `companion object`
- Quando recebido: pré-preenche `etCodigo`, desabilita edição, oculta botão de scan (desnecessário), habilita `btnSalvar` imediatamente
- Usuário vê a tela de coleta já pronta para preencher observações e salvar — sem precisar escanear o QR code de um patrimônio que já foi selecionado

---

## Build #018 - 22/05/2026 - Som no monitor + vibração corrigida

- **Tipo:** Release
- **Versão:** 2.29.5
- **Build Code:** 81
- **Arquivo:** `InventarioMobile/app/build/outputs/apk/release/SiHCP-release-v2.29.5.apk`
- **Tamanho:** 19.60 MB
- **Status:** ✅ Sucesso (6m 16s)

### Mudanças incluídas

#### 1. Som sutil no monitor ao vivo (NOVO)

`SoundUtils.playLiveColetaSound()` — beep curto (60ms) em volume baixo (25%, stream `STREAM_NOTIFICATION`), instância dedicada de `ToneGenerator` liberada após 200ms para não acumular.

A `CollectionViewActivity` toca o som **apenas** quando a lista cresce após a primeira renderização — abrir a tela com o monitor já populado não emite som, evitando ruído desnecessário. Cada coleta nova que chega via WebSocket dispara um "tic" discreto.

#### 2. Vibração que finalmente funciona

**Causa raiz** do problema histórico: desde Android 8 (Oreo, API 26), `Vibrator.vibrate(VibrationEffect)` **sem `VibrationAttributes`/`AudioAttributes` é silenciada** quando o dispositivo está em DND, modo silencioso ou economia de bateria. Padrão: o sistema entende a chamada como "notificação de baixa prioridade" e suprime.

**Correção em `VibrationHelper`**:
- API 33+ (Tiramisu): `VibrationAttributes.USAGE_HARDWARE_FEEDBACK` — explicita que é feedback de uma ação direta do usuário, **bypassa DND**
- API 26-32: `AudioAttributes` com `USAGE_ASSISTANCE_SONIFICATION` + `CONTENT_TYPE_SONIFICATION` — equivalente legado
- API < 26: API antiga não tinha o problema, mantém comportamento direto
- Detecção de `hasAmplitudeControl()` — em vibradores sem controle de amplitude, passar 255 fazia a vibração não tocar; agora cai para `DEFAULT_AMPLITUDE`
- `isPowerSaveMode()` exposto para a UI avisar quando o sistema pode silenciar mesmo com tudo certo

**Botão "Testar vibração" em Configurações** — força a vibração ignorando a preferência do switch, com toast informando o resultado e aviso visual quando Power Save está ativo. Útil para diagnosticar ROMs problemáticos (Xiaomi, Samsung etc.) sem precisar registrar uma coleta de teste.

**Aviso de Power Save** — texto laranja abaixo do switch aparece automaticamente quando o sistema está em economia de bateria + vibração habilitada. Atualizado em `onResume` para reagir a mudanças feitas fora do app.

#### Arquivos modificados/criados

- `utils/VibrationHelper.kt` — reescrito com VibrationAttributes/AudioAttributes
- `utils/SoundUtils.kt` — novo método `playLiveColetaSound()`
- `presentation/coleta/CollectionViewActivity.kt` — toca som ao detectar nova coleta no live feed
- `presentation/settings/SettingsActivity.kt` — botão de teste, aviso de power save, listener
- `res/layout/activity_settings.xml` — botão `btnTestarVibracao`, `TextView tvAvisoPowerSave`, namespace `tools`

---

## Build #017 - 22/05/2026 - Monitor de coletas em tempo real (WebSocket)

- **Tipo:** Release + Debug
- **Versão:** 2.29.5
- **Build Code:** 81
- **Arquivo:** `InventarioMobile/app/build/outputs/apk/release/SiHCP-release-v2.29.5.apk`
- **Tamanho:** 19.60 MB (release)
- **Status:** ✅ Sucesso (4m 21s)

### Mudanças incluídas

#### 1. CollectionViewActivity convertida em "Monitor ao vivo" (NOVO)

**Problema:** existiam duas telas de listagem de coletas — `ColetasUnificadaActivity` (em uso, com filtros e gestão) e `CollectionViewActivity` (legada, fazia o mesmo). Duplicação confusa.

**Solução:** dar à `CollectionViewActivity` um propósito único — feed em tempo real para acompanhar coletas registradas por outros usuários sem precisar atualizar manualmente. Mesmo path/classe — todos os Intents existentes continuam válidos.

**Backend (sihcp-server):**
- `realtime/ColetaEventoDTO.java` — payload enxuto do evento (id, número, descrição, coletor, sala, data, semEtiqueta, temFoto)
- `realtime/ColetaLiveSocketHandler.java` — handler raw WebSocket com sessões agrupadas por inventário, broadcast com fan-out, fechamento gracioso de sessões com erro
- `realtime/ColetaLiveWebSocketConfig.java` — registra o endpoint `/ws/coletas-live` (separado da config STOMP existente da dashboard web)
- `MobileColetaService.publicarEventoWebSocket()` — chamada ao final do `registrarColeta`, encapsulada em try/catch (falha de WS nunca afeta a transação). Não publica em caminho de idempotência (evita duplicar evento quando o app reenvia POST após timeout).
- `MobileSecurityConfig` + `SecurityConfig` — `/ws/**` liberado do filtro JWT (handshake HTTP); auth dedicada do WebSocket fica como TODO

**Cliente Android:**
- `data/realtime/ColetaEvento.kt` — espelho do DTO do servidor
- `data/realtime/RealtimeColetasManager.kt` (singleton Hilt) — cliente WebSocket OkHttp com auto-reconnect (backoff exponencial até 30s), ping de 25s, `SharedFlow<ColetaEvento>` para distribuição
- `presentation/coleta/LiveColetaMonitorViewModel.kt` — converte eventos em `Coleta` para o adapter existente, mantém lista limitada a 500 itens, contador acumulado independente
- `presentation/coleta/CollectionViewActivity.kt` reescrita — header "Monitor ao vivo / Acompanhe coletas em tempo real", indicador "🟢 AO VIVO" / "Conectando…" / "Offline", reuso de `CollectionAdapter`, conexão atrelada ao `onStart`/`onStop` (economia de bateria)
- `activity_collection_view.xml` — labels "Recebidas" / "Status" no header
- `DashboardFragment` — botão "Visualizar Coletas" virou "Monitor ao vivo"; navegação `listarColetados()` redirecionada para `ColetasUnificadaActivity`
- `strings.xml` — `view_collections_button` = "Monitor ao vivo"

**Funcionamento:**
1. Usuário A registra coleta no app
2. `MobileColetaService.registrarColeta` salva no banco e publica `ColetaEventoDTO` no WS
3. App do supervisor B (ou qualquer outro com o monitor aberto) recebe o evento via OkHttp WebSocket
4. `RealtimeColetasManager` emite no `SharedFlow`
5. `LiveColetaMonitorViewModel` insere no topo da lista
6. RecyclerView atualiza — sem refresh manual

**Resiliência:**
- Auto-reconnect com backoff (1s → 2 → 4 → 8 → 16 → 30s cap)
- Eventos perdidos durante reconexão **não** são re-enviados (feed é fire-and-forget); para histórico completo, usar `ColetasUnificadaActivity`
- Limite de 500 itens em memória — descarta os mais antigos da lista mas mantém contador
- Indicador visual no header sempre reflete o estado real da conexão

**Limitações conhecidas:**
- Sem auth no handshake WebSocket nesta versão (TODO — header JWT já enviado, validação no servidor pendente)
- Single-instance: para deploy multi-servidor seria preciso broker externo (Redis/RabbitMQ)

---

## Build #016 - 22/05/2026 - Release com troca rápida de câmera

- **Tipo:** Release
- **Versão:** 2.29.5
- **Build Code:** 81
- **Arquivo:** `InventarioMobile/app/build/outputs/apk/release/SiHCP-release-v2.29.5.apk`
- **Tamanho:** 19.59 MB
- **Status:** ✅ Sucesso (4m 36s)

### Mudanças incluídas

Mesma feature da Build #015 (debug), agora minificada e otimizada com R8 + shrink resources para distribuição:

- Botão sutil de troca de câmera (frontal ↔ traseira) na `CustomCaptureActivity`, ao lado do flash
- Aparece automaticamente apenas em dispositivos com duas câmeras
- Persiste a escolha no `PreferencesManager` (mesma flag do switch em Configurações)
- Reinicialização limpa de zoom/flash após o flip

**Sem alterações de código entre #015 e #016** — apenas variant `release` com R8 ativo (`isMinifyEnabled = true`, `shrinkResources = true`).

---

## Build #015 - 22/05/2026 - Botão de troca rápida de câmera (frontal ↔ traseira)

- **Tipo:** Debug
- **Versão:** 2.29.5
- **Build Code:** 81
- **Arquivo:** `InventarioMobile/app/build/outputs/apk/debug/SiHCP-debug-v2.29.5.apk`
- **Tamanho:** 29.98 MB
- **Status:** ✅ Sucesso (2m 42s)

### Mudanças incluídas

#### 1. Troca rápida de câmera no scanner (NOVO)

**Problema:** A escolha entre câmera frontal e traseira estava disponível apenas em Configurações, exigindo navegação fora do scanner. Em campo, isso atrapalha quando a câmera traseira está suja, danificada, ou quando a frontal serve melhor para um caso pontual.

**Solução:** Botão sutil ao lado do flash na `CustomCaptureActivity`:

- Ícone "flip" (ic_camera_switch.xml) no mesmo estilo dos demais controles do overlay (pill arredondado)
- Aparece automaticamente apenas quando o dispositivo possui câmera frontal **e** traseira (verificação via `PackageManager`)
- Toque alterna a lente sem fechar o scanner: pausa o preview, ajusta `cameraSettings.requestedCameraId` e dá resume
- Persiste a escolha via `PreferencesManager.setUsarCameraFrontal()` (mesma flag usada pelo switch em Settings, mantendo consistência)
- Reinicializa zoom e flash após troca (cada câmera tem alcance/recursos diferentes)
- Desliga torch antes do flip (frontal raramente tem flash; evita exceções)
- Atualiza `contentDescription` para acessibilidade ("Trocar para câmera frontal/traseira")
- Em caso de erro reverte a preferência e mostra status — não derruba o scanner

**Arquivos criados:**
- `res/drawable/ic_camera_switch.xml`

**Arquivos modificados:**
- `presentation/scanner/CustomCaptureActivity.kt` (referências ao novo botão, métodos `inicializarTrocaCamera` e `alternarCamera`)
- `res/layout/custom_capture.xml` (novo `ImageButton btnTrocarCamera` ao lado do `btnFlash`)

---

## Build #014 - 22/05/2026 - Configuração inicial de IP + Solução híbrida online/offline para coleta excluída

- **Tipo:** Release
- **Versão:** 2.29.5
- **Build Code:** 81
- **Arquivo:** `InventarioMobile/app/build/outputs/apk/release/SiHCP-release-v2.29.5.apk`
- **Tamanho:** 19.59 MB
- **Status:** ✅ Sucesso (2m 32s)

### Mudanças incluídas

#### 1. Tela de Configuração Inicial do Servidor (NOVO)

**Problema:** Toda nova instalação do APK trazia o IP `10.14.250.214` fixado, gerando transtorno quando o servidor mudava de endereço. Os usuários precisavam editar manualmente o IP a cada nova instalação.

**Solução:** Criada `InitialSetupActivity` que aparece na primeira utilização:

- Tela amigável com campos para IP e porta do servidor
- Botão "Testar Conexão" valida o IP antes de prosseguir
- Botão "Continuar" só fica habilitado após teste bem-sucedido
- Validação de IP (formato IPv4 ou "localhost")
- Validação de porta (1-65535)
- Status visual: testando (progress) / sucesso (verde) / erro (vermelho)
- Bloqueio do botão "voltar" para forçar a configuração

**Fluxo:**
1. Splash → verifica `preferencesManager.isServerConfigured()`
2. Se `false` → `InitialSetupActivity` (configuração obrigatória)
3. Se `true` → segue fluxo normal (login ou main)
4. Após configurar com sucesso, flag é marcada e tela não aparece mais

**Arquivos criados:**
- `InitialSetupActivity.kt`
- `activity_initial_setup.xml`
- `ic_server_setup.xml`

**Arquivos modificados:**
- `PreferencesManager.kt` — métodos `isServerConfigured()` e `setServerConfigured()`
- `SplashActivity.kt` — verifica flag antes de navegar
- `LoginActivity.kt` — marca flag quando usuário muda IP manualmente
- `AndroidManifest.xml` — registro da nova Activity
- `strings.xml` — strings da tela de setup

#### 2. Solução Híbrida Online/Offline para Coleta Excluída

**Problema:** A correção v2.29.4 anterior removeu toda verificação local de duplicata, criando risco de duplicatas reais no modo offline.

**Solução:** Verificação adaptativa baseada em conectividade:

- **ONLINE** (`networkMonitor.isConnected() == true`): confia no servidor (sem verificação local)
- **OFFLINE**: verifica banco local (Room) para evitar duplicatas reais
- **Reconexão**: `SincronizarColetasDoServidorUseCase` agora detecta e remove coletas locais que foram excluídas no servidor

**Arquivos modificados:**
- `ColetaRepositoryImpl.kt` — verificação adaptativa + injeção de `NetworkMonitor`
- `SincronizarColetasDoServidorUseCase.kt` — limpeza automática de coletas excluídas no servidor
- `ColetaDao.kt` — método `deletarPorId()` para limpeza
- `SyncResult` data class — campo `removidas`

#### 3. Correção de bug em `PreferencesManager.kt`

A classe estava com chave de fechamento `}` no lugar errado, causando erros de compilação ao tentar declarar novos métodos. Corrigido durante este build.

### Cenários cobertos

| Cenário | Comportamento |
|---------|---------------|
| Primeira instalação | Mostra tela de setup, força configuração |
| Reinstalação após uso | Lembra do IP configurado (no SharedPreferences) |
| Mudança de servidor | Usuário edita IP no LoginActivity, é re-marcado como configurado |
| Online + item excluído no servidor | ✅ Permite recoletar |
| Offline + duplicata local | ✅ Bloqueia (correto) |
| Offline + item excluído durante offline | ⚠️ Bloqueio falso até sincronizar (limitação aceita) |
| Reconexão após exclusão | ✅ Sync remove cache antigo automaticamente |

### Próximos passos

1. **Testar fluxo completo de configuração inicial:**
   - Desinstalar app
   - Reinstalar APK
   - Verificar se tela de setup aparece
   - Testar IP inválido (validação)
   - Testar IP válido com servidor offline (erro)
   - Testar IP válido com servidor online (sucesso)
   - Verificar que vai direto para login após setup

2. **Testar fluxo de coleta excluída:**
   - Online: coletar item, excluir, recoletar → deve permitir
   - Offline: tentar coletar duplicata → deve bloquear
   - Reconectar após exclusão remota → cache deve limpar

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

---

## Build #012 - 21/05/2026 - Fix: coletas presas por erro de participante + UPSERT no cadastro

- **Tipo:** Release
- **Versão:** 2.29.3
- **Build Code:** 80
- **Arquivo:** `InventarioMobile/app/build/outputs/apk/release/SiHCP-release-v2.29.3.apk`
- **Tamanho:** 19.57 MB
- **Status:** ✅ Sucesso (~5m 20s)
- **Servidor:** `SERVIDOR_MOBILE_PRODUCAO/sihcp-server.jar` — 21/05/2026 11:02

### Contexto

Coletores não estavam autorizados (não eram participantes do inventário). Ao tentar sincronizar, o servidor retornava `"Usuário não é participante deste inventário"`. O app registrava esse erro como permanente e parava de tentar. Após o admin adicionar os participantes no desktop, o app não retentava automaticamente — só funcionou depois que o usuário fez "importar dados" manualmente, o que limpou o estado de erro.

### Problemas corrigidos

#### 1. Erro de participante tratado como permanente (app Android)

**Causa:** A lista `errosRecuperaveis` no `ColetaRepositoryImpl.sincronizarColetasPendentes()` só incluía erros de rede/timeout. "Usuário não é participante" era tratado como erro permanente — coleta ficava presa indefinidamente.

**Correção:**
- Adicionados `"participante"`, `"autorizado"`, `"inventário"` à lista de erros recuperáveis
- Erros de autorização são resetados a **cada ciclo de sync** (não só após 5 tentativas), pois o admin pode corrigir a qualquer momento

#### 2. Mensagem de erro genérica (app Android)

**Causa:** O servidor retornava `"Usuário não é participante deste inventário"` — mensagem técnica sem orientação.

**Correção:**
- `MobileColetaService.java` — mensagem melhorada: `"Usuário não autorizado: 'Nome' não é participante ativo do inventário 'X'. Solicite ao administrador que adicione este usuário como participante no sistema desktop."`
- `PendingCollectionsAdapter.kt` — erros de participante/autorização exibem mensagem amigável: `"⚠️ Usuário não autorizado neste inventário. Solicite ao administrador que adicione você como participante no sistema desktop e tente sincronizar novamente."`

#### 3. Acúmulo de registros inativos duplicados (desktop/servidor)

**Causa:** `salvarConfiguracaoParticipantes()` faz `removerTodosParticipantes()` (soft-delete: `ativo=FALSE`) + `inserirParticipante()` (sempre INSERT). Cada vez que o operador tentava salvar a configuração, criava novos registros inativos para os mesmos usuários. O banco acumulou 16 registros inativos para 4 usuários antes de funcionar.

**Correção:** `ParticipanteInventarioDAO.inserirParticipante()` agora usa UPSERT:
1. Tenta reativar o registro inativo mais recente (`ORDER BY id_participante DESC LIMIT 1`)
2. Só faz INSERT se não existir nenhum registro inativo

Isso evita o acúmulo de registros duplicados e garante que re-adicionar participantes seja idempotente.

### Fluxo corrigido

```
Admin adiciona participantes no desktop
  → inserirParticipante() reativa registro existente (UPSERT)
  → ativo = TRUE no banco

Coletor tenta sincronizar (com erro antigo de participante)
  → sincronizarColetasPendentes() reseta erros de autorização
  → Tenta novamente → servidor encontra participante ativo
  → Coleta sincronizada ✅

Coletor vê erro na tela de pendentes
  → Mensagem clara: "Solicite ao administrador..."
  → Botão Retry disponível
```

---

## Build #013 - 21/05/2026 - Fix: timeout no download do relatório fotográfico

- **Tipo:** Release
- **Versão:** 2.29.3
- **Build Code:** 80
- **Arquivo:** `InventarioMobile/app/build/outputs/apk/release/SiHCP-release-v2.29.3.apk`
- **Tamanho:** 19.6 MB
- **Status:** ✅ Sucesso (3m 17s)

### Problema

O app exibia "Erro de conexão. Verifique a rede e tente novamente" ao tentar baixar o relatório fotográfico. O servidor gerava o PDF corretamente (1,5 MB, 9 cards em ~1s), mas o app dava timeout antes de receber o arquivo completo.

**Causa raiz:** O `RelatorioFotoApi` usava o mesmo `OkHttpClient` das demais APIs, com `callTimeout=90s`. Em redes móveis lentas, transferir 1,5 MB pode ultrapassar esse limite, resultando em `SocketTimeoutException` — que caia no `else` do `when(error)` e exibia a mensagem genérica de conexão.

### Correção

**`ApiModule.kt`:**
- Adicionado qualifier `@DownloadClient` para isolar o cliente de download
- Criado `provideDownloadOkHttpClient()` com timeouts estendidos:
  - `readTimeout = 300s` (5 min)
  - `callTimeout = 360s` (6 min)
  - Sem cache (arquivos binários grandes não devem ser cacheados)
- Criado `provideDownloadRetrofit()` usando o cliente dedicado com a mesma baseUrl
- `provideRelatorioFotoApi()` agora usa `@DownloadClient Retrofit` em vez do Retrofit padrão

### Impacto

- ✅ Download do PDF/XLSX do relatório fotográfico não dá mais timeout em redes lentas
- ✅ Demais APIs (coleta, patrimônio, sync) não são afetadas — continuam com `callTimeout=90s`


---

## Build #029 - 31/05/2026 21:37 - Rebuild com mesmas funcionalidades (Build #028)

- **Tipo:** Release (assinado, R8 + shrinkResources)
- **Versão:** 2.29.7
- **Build Code:** 83
- **Arquivo APK:** `InventarioMobile/app/build/outputs/apk/release/SiHCP-release-v2.29.7.apk` (19.62 MB, 31/05 21:28)
- **Status:** ✅ Sucesso (`assembleRelease`, BUILD SUCCESSFUL em 20s, 53 tasks: 1 executada, 52 up-to-date)

### Mudanças

- **Rebuild do APK release** com as mesmas funcionalidades do Build #028 (correção de divergências + dashboard redesign).
- Nenhuma alteração de código foi feita — apenas recompilação solicitada pelo usuário.
- Build muito rápida (20s) devido ao cache do Gradle — apenas 1 tarefa foi executada novamente, 52 estavam up-to-date.

### Nota Técnica

O APK gerado é **idêntico funcionalmente** ao Build #028. Como não houve mudanças no código-fonte, os bytecodes compilados são os mesmos (exceto timestamps/assinaturas). O arquivo físico tem o mesmo timestamp (21:28) porque o Gradle detectou que não havia nada para recompilar e manteve o artefato existente.

**Resultado:** O APK de distribuição continua sendo `SiHCP-release-v2.29.7.apk` do Build #028, pronto para deploy.
