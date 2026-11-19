# 📱 Documentação Completa - SIHCP Mobile App

**Versão:** `2.29.8` (Build `84`)  
**Última atualização do app:** `13/11/2026` · **Revisão documental:** `13/11/2026`  
**Sistema:** Sistema de Histórico e Coleta Patrimonial — IFMT

> **Nota:** Este arquivo mantém o nome original para preservar links existentes, mas seu conteúdo já contempla integralmente as entregas até a versão **`2.29.8` (Build `84`)**.

---

## 📋 Sumário

1. [Visão Geral](#1-visão-geral)
2. [Arquitetura](#2-arquitetura)
3. [Funcionalidades Principais](#3-funcionalidades-principais)
4. [Fluxos de Uso](#4-fluxos-de-uso)
5. [Modo Online vs Offline](#5-modo-online-vs-offline)
6. [Segurança e Autenticação](#6-segurança-e-autenticação)
7. [Sincronização](#7-sincronização)
8. [Configuração Inicial](#8-configuração-inicial)
9. [Histórico de Versões Recentes](#9-histórico-de-versões-recentes)
10. [Documentação Relacionada](#10-documentação-relacionada)

---

## 1. Visão Geral

App Android nativo para coleta patrimonial em campo, integrado ao sistema SIHCP do IFMT.

### Stack Técnica

| Camada | Tecnologia |
|--------|------------|
| Linguagem | Kotlin `1.9.10` |
| UI | Android Views + Material Design 3 |
| Arquitetura | Clean Architecture + MVVM |
| Injeção de Dependência | Hilt (Dagger `2.48`) |
| Banco Local | Room `2.6.1` (SQLite v19) + SQLCipher `4.6.1` |
| Networking | Retrofit `2.9.0` + OkHttp `4.12.0` (REST + WebSocket) |
| Imagens | Glide |
| QR Code | ZXing |
| Reactive | Kotlin Flow + Coroutines |
| Background Tasks | WorkManager |
| Min SDK | 23 (Android 6.0) |
| Target SDK | 34 (Android 14) |

### Componentes do Sistema

```
┌──────────────────────┐         ┌────────────────────┐         ┌──────────────────────┐
│  App Android         │         │  Servidor          │         │  Desktop Swing       │
│  (Kotlin)            │ ◄────► │  Spring Boot       │ ◄────► │  (Java)              │
│  Coleta em campo     │  HTTP   │  API REST + JWT    │  JDBC   │  Administração       │
└──────────────────────┘         └────────┬───────────┘         └──────────────────────┘
                                          │
                                          ▼
                                ┌────────────────────┐
                                │  PostgreSQL        │
                                │  sispatrimonio     │
                                └────────────────────┘
```

---

## 2. Arquitetura

### Clean Architecture + MVVM

```
┌─────────────────────────────────────────────────────────────┐
│ Presentation Layer (UI)                                     │
│ - Activities / Fragments                                    │
│ - ViewModels (HiltViewModel)                                │
│ - States (sealed classes)                                   │
└──────────────────────┬──────────────────────────────────────┘
                       │ chama
                       ▼
┌─────────────────────────────────────────────────────────────┐
│ Domain Layer (Regras de Negócio)                            │
│ - Use Cases                                                 │
│ - Domain Models (puros, sem Android)                        │
│ - Repository Interfaces                                     │
└──────────────────────┬──────────────────────────────────────┘
                       │ implementa
                       ▼
┌─────────────────────────────────────────────────────────────┐
│ Data Layer                                                  │
│ - Repository Implementations                                │
│ - Local: Room DAOs + Entities                               │
│ - Remote: Retrofit APIs + DTOs                              │
│ - Mappers (Domain ↔ Entity ↔ DTO)                           │
└─────────────────────────────────────────────────────────────┘
```

### Estrutura de Pastas

```
com.inventario.mobile/
├── data/
│   ├── local/
│   │   ├── dao/              # ColetaDao, PatrimonioDao, SalaDao
│   │   ├── entity/           # ColetaEntity, PatrimonioEntity
│   │   └── database/         # AppDatabase
│   ├── remote/
│   │   ├── api/              # Retrofit interfaces
│   │   └── dto/              # DTOs de rede
│   ├── mapper/               # Conversores entre camadas
│   └── repository/           # Implementações
│
├── domain/
│   ├── model/                # Modelos de domínio (puros Kotlin)
│   ├── repository/           # Interfaces (contratos)
│   └── usecase/              # Casos de uso
│
├── presentation/
│   ├── login/                # Tela de login + biometria
│   ├── setup/                # InitialSetupActivity (v2.29.5)
│   ├── main/                 # Activity principal + navigation
│   ├── dashboard/            # Tela inicial
│   ├── scanner/              # QR Code scanner
│   ├── coleta/               # Telas de coleta
│   ├── coletas/              # Lista e visualização
│   ├── sala/                 # Seleção de salas
│   ├── statistics/           # Exportação de relatórios
│   ├── settings/             # Configurações
│   └── validation/           # Validação de patrimônios
│
├── di/                       # Módulos Hilt
│   ├── ApiModule.kt          # Retrofit + OkHttp + Interceptors
│   ├── DatabaseModule.kt     # Room
│   ├── RepositoryModule.kt   # Bind de interfaces
│   └── ...
│
├── network/                  # Interceptors customizados
├── security/                 # Biometria, PIN, Keystore
├── worker/                   # WorkManager (sync, fotos)
└── utils/                    # PreferencesManager, NetworkMonitor, etc.
```

---

## 3. Funcionalidades Principais

### 3.1 Coleta de Patrimônios

| Tipo | Quando usar | Identificação |
|------|-------------|---------------|
| **QR Code** | Plaqueta legível | Câmera lê o código automaticamente |
| **Manual** | QR ilegível mas número visível | Usuário digita o número |
| **Por Descrição** | Sem identificação clara | Busca por descrição entre não coletados |
| **Sem Etiqueta** | Item sem plaqueta nem identificação | Cadastra com foto + descrição livre |

### 3.2 Funcionalidades de Apoio

- **Sala fixada** — destaque visual e atalho para sala atual
- **Modo rápido (estado fixo)** — pula confirmação de estado a cada coleta
- **Coleta com foto** — opcional para itens com etiqueta, obrigatória para sem etiqueta
- **Validação prévia** — consulta servidor antes de permitir coleta (online)
- **Histórico de scans** — todas as leituras realizadas
- **Dashboard** — estatísticas em tempo real

### 3.3 Relatórios e Exportação

| Recurso | Formato | Restrição de perfil |
|---------|---------|---------------------|
| Exportação geral | PDF, Excel (TSV), CSV | CONSULTA+ |
| Relatório fotográfico | PDF, XLSX (com fotos) | SUPERVISOR+ |
| Filtros | Por sala, status, tipo | — |

---

## 4. Fluxos de Uso

### 4.1 Primeira Instalação (v2.29.5+)

```
[Splash 5s]
     ↓
[isServerConfigured?]
     │
     ├── NÃO → [InitialSetupActivity]
     │           ├── Digita IP + Porta
     │           ├── Testa Conexão
     │           └── Continuar → [LoginActivity]
     │
     └── SIM → [LoginActivity]
                 ├── Login + senha
                 ├── (opcional) Cadastro biométrico
                 └── [MainActivity / Dashboard]
```

### 4.2 Coleta de Patrimônio

```
[Dashboard]
     ↓
[Selecionar Sala]
     ↓
[Escolher tipo: QR / Manual / Descrição]
     ↓
[Validação no Servidor (se online)]
     ↓
[Preencher Estado + Observação + Foto opcional]
     ↓
[Registrar]
     ↓
[Coleta salva localmente] ──→ [Sync em background]
```

### 4.3 Sincronização Automática

```
[Periódico via SyncScheduler (política KEEP)]
     ↓
[WorkManager dispara ColetaSyncWorker]
     ↓
[Verifica: tem internet / Wi-Fi? bateria OK?]
     ↓
[SincronizarColetasPendentesUseCase]
     │
     ├── BATCH (várias coletas com clienteUuid idempotente)
     │     ↓
     │     [se falhar: fallback individual]
     │
     ↓
[SincronizarColetasDoServidorUseCase]
     │
     ├── Baixa coletas do servidor
     ├── Atualiza/insere localmente
     └── Remove coletas excluídas no servidor (v2.29.5+)
```

---

## 5. Modo Online vs Offline

### 5.1 O que funciona offline

✅ **Todas as operações de coleta:**
- QR Code, manual, por descrição, sem etiqueta
- Visualização de coletas anteriores
- Busca e filtros
- Modo rápido e sala fixada

### 5.2 O que exige internet

❌ **Operações que dependem do servidor:**
- Login inicial (primeira autenticação)
- Configuração inicial do servidor
- Sincronização (envio/recebimento)
- Exportação de relatórios (PDF/XLSX/CSV)
- Relatório fotográfico
- Renovação de sessão expirada

### 5.3 Estratégia Híbrida de Verificação de Duplicatas (v2.29.5)

| Estado | Verificação | Comportamento |
|--------|-------------|---------------|
| **Online** | Servidor (fonte de verdade) | Permite recoletar items excluídos |
| **Offline** | Cache local (Room) | Bloqueia duplicatas reais |
| **Reconexão** | Sync remove cache antigo | Limpeza automática |

### 5.4 Modo Offline Forçado

O app permite forçar modo offline mesmo com internet disponível, útil para:
- Economizar dados móveis em locais com sinal fraco
- Testar comportamento offline
- Trabalhar quando o servidor está em manutenção

Configuração: **Settings → Modo Offline Forçado**.

---

## 6. Segurança e Autenticação

### 6.1 Camadas de Segurança

```
1. Comunicação HTTPS (em produção)
2. JWT com refresh automático (RefreshTokenInterceptor)
3. SharedPreferences criptografado (EncryptedSharedPreferences)
4. Biometria opcional para login local
5. PIN de 6 dígitos (alternativa à biometria)
6. Rate limiting de login (proteção anti-força-bruta no servidor)
7. Sanitização de logs (sem vazar tokens em release)
```

### 6.2 Perfis de Usuário

| Perfil | Hierarquia | Pode |
|--------|------------|------|
| **CONSULTA** | 1 (mínimo) | Visualizar dados, dashboard |
| **COLETOR** | 2 | + Registrar coletas |
| **SUPERVISOR** | 3 | + Relatórios gerenciais, gerar relatório fotográfico |
| **ADMIN** | 4 (máximo) | + Excluir coletas, gerenciar usuários |

### 6.3 Gestão de Sessão

- **Access Token (JWT):** 1 hora de validade
- **Refresh Token:** 14 dias de validade
- **Renovação automática:** transparente para o usuário (intercepta 401)
- **Sessão completamente expirada:** força novo login
- **Login offline:** permitido se já houve login bem-sucedido naquele dispositivo + biometria/PIN configurados

---

## 7. Sincronização

### 7.1 Tipos de Sync

| Tipo | Trigger | Frequência | Mecanismo |
|------|---------|------------|-----------|
| **Background** | Automático | A cada 30 min | WorkManager |
| **Manual** | Usuário | Sob demanda | Botão "Sincronizar" |
| **Reenvio** | Usuário | Item específico | Long-click + "Reenviar" |
| **Pull-to-refresh** | Usuário | Manual na lista | Arrastar para baixo |

### 7.2 Sync de Envio (App → Servidor)

```kotlin
SincronizarColetasPendentesUseCase()
  ↓
1. Busca coletas com sincronizado = false
  ↓
2. Tenta enviar em LOTE (batch sync)
  ↓
3. Se falhar: fallback para envio individual
  ↓
4. Atualiza local com servidorId retornado
  ↓
5. Marca como sincronizado = true
```

### 7.3 Sync de Recebimento (Servidor → App)

```kotlin
SincronizarColetasDoServidorUseCase()
  ↓
1. Busca todas coletas do servidor
  ↓
2. Identifica novas, atualizadas e excluídas
  ↓
3. Insere/atualiza no Room
  ↓
4. Remove coletas locais que não existem mais no servidor (v2.29.5)
  ↓
5. Retorna SyncResult com estatísticas
```

### 7.4 Constraints do WorkManager

```kotlin
Constraints.Builder()
    .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
    .setRequiresBatteryNotLow(true)                 // Quando economia de bateria ativa
    .build()

PeriodicWorkRequestBuilder<ColetaSyncWorker>(interval, MINUTES, 15, MINUTES)
    .setConstraints(constraints)
    .setBackoffCriteria(
        BackoffPolicy.EXPONENTIAL,                  // Retry exponencial (30s iniciais)
        30, SECONDS
    )
```

---

## 8. Configuração Inicial

### 8.1 Tela de Setup (v2.29.5)

Antes desta versão, o IP do servidor (`10.14.250.214`) era hardcoded no APK. Agora:

**Fluxo:**
1. **Splash detecta** que `isServerConfigured() == false`
2. **InitialSetupActivity** é exibida
3. Usuário **digita IP + porta**
4. **Botão "Testar"** valida conexão real com `/api/mobile/health`
5. **Botão "Continuar"** habilita após teste bem-sucedido
6. **Configuração persiste** em EncryptedSharedPreferences
7. **Próximas aberturas** vão direto ao login

**Vantagens:**
- ✅ Sem IP hardcoded
- ✅ Onboarding guiado
- ✅ Validação prévia evita problemas
- ✅ Funciona com qualquer servidor

### 8.2 Alteração Posterior do IP

O usuário pode alterar o IP a qualquer momento na tela de login (campo "IP do Servidor"). Não precisa reinstalar.

### 8.3 Reset Completo

Para forçar a tela de configuração novamente:
- **Configurações Android → Apps → SIHCP → Armazenamento → Limpar dados**
- ⚠️ Apaga coletas pendentes não sincronizadas

---

## 9. Histórico de Versões Recentes

### v2.29.8 (03/06/2026 – 13/11/2026 — Build 84) — Coleta Forçada Offline & Limpeza Completa de Código Inutilizado
- **Coleta forçada em modo offline (`forcarColeta`):** `ColetaRepositoryImpl`, `RegistrarColetaUseCase`, `ManualCollectionActivity`/`ViewModel` e `ScannerActivity`/`ViewModel` passaram a honrar a decisão explícita do usuário de forçar coleta de etiqueta duplicada mesmo quando offline.
- **Saneamento completo da base de código Android:** remoção segura de **181 arquivos inutilizados/redundantes** (em três varreduras: 43 arquivos `.old`/`.disabled`/placeholders/`Validation*`/legados e 15 layouts órfãos na 1ª etapa; 89 classes Kotlin e 11 XMLs na 2ª etapa; 12 classes Kotlin mortas + bindings DI, consolidação do pacote duplicado `util/` em `utils/`, 20 recursos XML órfãos em `res/drawable`/`res/anim` e 5 testes de classes removidas na 3ª etapa), além de 19 diretórios vazios e entradas mortas no `AndroidManifest.xml`, mantendo o código 100% enxuto para geração do hash INPI e com compilação Gradle verificada.

### v2.29.7 (29–31/05/2026 — Build 83) — Monitor Live em Release, Atalhos na Câmera, Dashboard Redesign e Divergências
- **Correção de registro de divergências (Build #028/#029):** `ColetaSyncWorker` (`SincronizarColetasPendentesUseCase`) e `ColetaRepositoryImpl` enviam os valores reais de `divergencia` e `motivoDivergencia` (servidor recalcula em `MobileColetaService`).
- **Dashboard Redesign (Build #027):** lógica extraída para `DashboardUiMapper` (puro e testável, coberto por 106 testes com jqwik), `DashboardPresentationModels.kt` e `fragment_dashboard.xml` redesenhado.
- **Atalhos de Coleta Rápida na Câmera ao Vivo (Build #025/#026):** faixa em estilo *pill* (*Sem etiqueta / Manual / Descrição*) na `CustomCaptureActivity` e `ScannerActivity` via `ColetaShortcutNavigator`.
- **Fix Monitor ao Vivo em Release:** `proguard-rules.pro` preserva `com.inventario.mobile.data.realtime.**` (`ColetaEvento`), evitando ofuscação pelo R8 na desserialização Gson do WebSocket.

### v2.29.6 — Monitor ao Vivo de Coletas (WebSocket)
- Cliente `RealtimeColetasManager` conectado a `/ws-coletas-live` + `LiveColetaMonitorViewModel` e `CollectionViewActivity` em modo monitor ao vivo.
- Troca de câmera no scanner, feedback sonoro sutil no monitor live, correção de vibração (DND/silencioso), campo responsável na coleta manual, coleta direta nos detalhes e proteção contra duplicata.
- Requer servidor `sihcp-server` `2.9.0+`.

### v2.29.5 (22/05/2026) — Configuração inicial + híbrido online/offline

**Novidades:**
- 🆕 Tela de Configuração Inicial do servidor
- 🆕 Solução híbrida online/offline para coletas excluídas
- 🆕 Limpeza automática de coletas excluídas no servidor durante sync

**Correções:**
- 🐛 Coleta excluída no servidor agora pode ser recoletada (online)
- 🐛 Bug de fechamento de classe no `PreferencesManager`

### v2.29.3 (18/05/2026)

- Correção de timeout no download do relatório fotográfico
- Fix em fotos não anexadas
- Process death + foto perdida ao retornar da câmera
- UPSERT no cadastro de coletas

### v2.29.2 (15/05/2026)

- Flash do scanner funcional

### v2.29.1 (14/05/2026)

- Fix câmera em loop

### v2.29.0 (14/05/2026)

- Fix câmera scan rápido
- Câmera frontal opcional para coleta rápida

### v2.28.0 (13/05/2026)

- Top Coletores no dashboard
- Setores Pendentes
- Projeção de Término do inventário
- Relatório Fotográfico em PDF + Excel
- Câmera persistente, vibração instantânea
- Relatório por sala onde foi ENCONTRADO

### v2.21.0-security (maio/2026)

- Hardening de segurança
- Refresh token automático
- LogSanitizer para builds release
- Rate limiting de login

---

## 10. Documentação Relacionada

### Manuais por Público

- [`docs/manuais/MANUAL_COLETOR.md`](../docs/manuais/MANUAL_COLETOR.md) — Coletor em campo
- [`docs/manuais/MANUAL_ADMINISTRADOR.md`](../docs/manuais/MANUAL_ADMINISTRADOR.md) — TI / Servidor
- [`docs/manuais/MANUAL_OPERADOR.md`](../docs/manuais/MANUAL_OPERADOR.md) — Desktop Swing

### Documentação Técnica

- [`docs/GUIA_MOBILE.md`](../docs/GUIA_MOBILE.md) — Arquitetura mobile completa
- [`docs/GUIA_FUNCIONALIDADES.md`](../docs/GUIA_FUNCIONALIDADES.md) — Features detalhadas
- [`docs/HISTORICO_CORRECOES.md`](../docs/HISTORICO_CORRECOES.md) — Histórico de bugs corrigidos

### Steering Rules (.kiro/steering/)

- `clean-architecture.md` — Padrão arquitetural obrigatório
- `endpoints-nao-alterar.md` — Endpoints críticos que não devem ser modificados
- `security-roles.md` — Matriz de permissões por endpoint
- `sync-improvements-summary.md` — Sincronização avançada
- `database-verification.md` — Como verificar estrutura do banco

### Documentos Específicos da v2.29.5

- [`CONFIGURACAO_INICIAL_IP_SERVIDOR.md`](CONFIGURACAO_INICIAL_IP_SERVIDOR.md) — Tela de setup
- [`CORRECAO_COLETA_EXCLUIDA_V2.29.5_HIBRIDA.md`](CORRECAO_COLETA_EXCLUIDA_V2.29.5_HIBRIDA.md) — Solução híbrida
- [`CORRECAO_COLETA_EXCLUIDA_V2.29.4.md`](CORRECAO_COLETA_EXCLUIDA_V2.29.4.md) — Iteração anterior
- [`PROBLEMA_COLETA_EXCLUIDA_AINDA_DETECTADA.md`](PROBLEMA_COLETA_EXCLUIDA_AINDA_DETECTADA.md) — Análise inicial

### Build History

- [`BUILD_HISTORY.md`](../BUILD_HISTORY.md) (raiz) — Histórico recente de builds
- [`docs/historico_e_reunioes/BUILD_HISTORY.md`](../docs/historico_e_reunioes/BUILD_HISTORY.md) — Histórico antigo (v2.28.0 e anteriores)

---

## 📌 Notas Finais

Esta documentação consolida o estado do app na versão `2.29.8` (Build `84`, `13/11/2026`), revisada documentalmente em `13/11/2026`. Para detalhes de builds individuais, consulte o [`BUILD_HISTORY.md`](../BUILD_HISTORY.md).

**Mantido por:** Equipe de Desenvolvimento SIHCP  
**Documentação de Registro INPI:** [`REGISTRO_SOFTWARE_SIHCP_MOBILE/`](../REGISTRO_SOFTWARE_SIHCP_MOBILE/00_LEIA_ME_GUIA_REGISTRO_INPI.md)
