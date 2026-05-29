# 📱 Documentação Completa - SIHCP Mobile App

**Versão:** 2.29.5 (Build 81)
**Última atualização:** 22/05/2026
**Sistema:** Sistema de Histórico e Coleta Patrimonial — IFMT

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
| Linguagem | Kotlin |
| UI | Android Views + Material Design 3 |
| Arquitetura | Clean Architecture + MVVM |
| Injeção de Dependência | Hilt (Dagger) |
| Banco Local | Room (SQLite) |
| Networking | Retrofit + OkHttp |
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
[A cada 30 min]
     ↓
[WorkManager dispara SyncWorker]
     ↓
[Verifica: tem internet? bateria OK?]
     ↓
[SincronizarColetasPendentesUseCase]
     │
     ├── BATCH (várias coletas em uma requisição)
     │     ↓
     │     [se falhar: fallback individual]
     │
     ↓
[SincronizarColetasDoServidorUseCase]
     │
     ├── Baixa coletas do servidor
     ├── Atualiza/insere localmente
     └── Remove coletas excluídas no servidor (v2.29.5)
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
    .setRequiredNetworkType(NetworkType.CONNECTED)  // Internet OK
    .setRequiresBatteryNotLow(true)                 // Bateria > 15%
    .build()

PeriodicWorkRequestBuilder<SyncWorker>(30, MINUTES)
    .setConstraints(constraints)
    .setBackoffCriteria(
        BackoffPolicy.EXPONENTIAL,                  // Retry exponencial
        15, MINUTES
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

Esta documentação consolida o estado do app em 22/05/2026. Para informações sobre features anteriores, consulte os manuais específicos. Para mudanças futuras, este documento deve ser atualizado em paralelo com o BUILD_HISTORY.md.

**Mantido por:** Equipe de Desenvolvimento SIHCP
**Próxima revisão sugerida:** quando v2.30.0 for lançada
