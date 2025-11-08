# Funcionalidade de Coleta Offline

## Visão Geral

Sistema completo de coleta offline que permite aos usuários baixar dados do servidor, realizar coletas sem conexão com a internet e sincronizar posteriormente.

## Componentes Implementados

### 1. Backend (Java/Spring Boot)

#### Controllers
- **MobileResponsavelController**: Endpoints para listar e buscar responsáveis
  - `GET /api/mobile/responsaveis` - Lista todos os responsáveis ativos
  - `GET /api/mobile/responsaveis/{id}` - Busca responsável por ID

- **MobileSalaController**: Endpoints para listar e buscar salas
  - `GET /api/mobile/salas` - Lista todas as salas ativas
  - `GET /api/mobile/salas/{id}` - Busca sala por ID

- **MobilePatrimonioController** (atualizado):
  - `GET /api/mobile/patrimonio` - Lista patrimônios com paginação
  - `GET /api/mobile/patrimonio/responsavel/{id}` - Lista patrimônios por responsável com filtro de coleta

#### Services
- **MobileResponsavelService**: Lógica de negócio para responsáveis
- **MobileSalaService**: Lógica de negócio para salas
- **MobilePatrimonioService** (atualizado): Suporte a paginação e filtros

#### DTOs
- **MobileResponsavelDTO**: Dados de responsável para API mobile
- **MobileSalaDTO**: Dados de sala para API mobile

### 2. Android App (Kotlin)

#### Gerenciador de Dados Offline
**OfflineDataManager** (`data/local/OfflineDataManager.kt`)
- Download de dados do servidor (patrimônios, responsáveis, salas)
- Armazenamento local em SQLite via Room
- Controle de modo offline (habilitar/desabilitar)
- Estatísticas de dados offline
- Limpeza de dados offline

Principais métodos:
- `downloadAllData()` - Baixa todos os dados necessários
- `getOfflineStats()` - Retorna estatísticas dos dados offline
- `clearOfflineData()` - Limpa dados offline
- `setOfflineModeEnabled()` - Habilita/desabilita modo offline
- `getPatrimoniosOfflineByResponsavel()` - Busca patrimônios offline
- `getPatrimonioOfflineByNumero()` - Busca patrimônio por número offline

#### Entidades Room
**PatrimonioEntity** (`data/local/entity/PatrimonioEntity.kt`)
- Tabela: `patrimonio_offline`
- Índices: numero, responsavelId, salaId, jaColetado
- Campos: todos os dados do patrimônio

**ResponsavelEntity** (`data/local/entity/ResponsavelEntity.kt`)
- Tabela: `responsavel_offline`
- Índices: nome, ativo
- Campos: dados do responsável

**SalaEntity** (`data/local/entity/SalaEntity.kt`)
- Tabela: `sala_offline`
- Índices: nome, ativa
- Campos: dados da sala

#### DAOs (Data Access Objects)
**PatrimonioDao** (`data/local/dao/PatrimonioDao.kt`)
- Operações CRUD para patrimônios offline
- Busca por número, responsável, status de coleta
- Paginação e contagem

**ResponsavelDao** (`data/local/dao/ResponsavelDao.kt`)
- Operações CRUD para responsáveis offline
- Busca e listagem

**SalaDao** (`data/local/dao/SalaDao.kt`)
- Operações CRUD para salas offline
- Busca e listagem

#### Interface de Sincronização
**OfflineSyncActivity** (`presentation/offline/OfflineSyncActivity.kt`)
- Tela para gerenciar sincronização offline
- Botões para:
  - Baixar dados para offline
  - Sincronizar coletas pendentes
  - Limpar dados offline
- Switch para habilitar/desabilitar modo offline
- Exibição de estatísticas:
  - Quantidade de patrimônios, responsáveis e salas
  - Coletas pendentes de sincronização
  - Data da última sincronização

**OfflineSyncViewModel** (`presentation/offline/OfflineSyncViewModel.kt`)
- Gerenciamento de estado da UI
- Coordenação de operações assíncronas
- Tratamento de erros e feedback ao usuário

#### Layout
**activity_offline_sync.xml** (`res/layout/activity_offline_sync.xml`)
- Interface Material Design
- Cards para:
  - Controle do modo offline
  - Estatísticas dos dados
  - Ações de sincronização
- Barra de progresso para downloads
- Feedback visual de operações

#### Modelos de Dados
**Responsavel** (`data/model/Responsavel.kt`)
- Modelo de domínio para responsável

**Sala** (`data/model/Sala.kt`)
- Modelo de domínio para sala

#### Utilitários
**PreferencesManager** (`utils/PreferencesManager.kt`)
- Gerenciamento de SharedPreferences
- Armazenamento de configurações e estado

## Fluxo de Uso

### 1. Preparação para Modo Offline

```
1. Usuário abre a tela de Sincronização Offline
2. Clica em "Baixar Dados para Offline"
3. Sistema baixa:
   - Responsáveis (todos ativos)
   - Salas (todas ativas)
   - Patrimônios (paginado, 100 por vez)
4. Dados são armazenados no SQLite local
5. Usuário habilita o "Modo Offline"
```

### 2. Coleta Offline

```
1. Usuário seleciona responsável (dados vêm do SQLite)
2. Usuário escaneia QR Code
3. Sistema busca patrimônio no SQLite local
4. Coleta é salva localmente com flag "pendente"
5. Interface mostra status "offline"
```

### 3. Sincronização

```
1. Usuário conecta à internet
2. Abre tela de Sincronização Offline
3. Clica em "Sincronizar Coletas Pendentes"
4. Sistema envia coletas pendentes para o servidor
5. Marca coletas como sincronizadas
6. Atualiza estatísticas
```

## Estrutura de Dados

### Preferências Armazenadas
- `last_offline_sync`: Timestamp da última sincronização
- `offline_mode_enabled`: Modo offline habilitado (true/false)
- `total_patrimonios_offline`: Total de patrimônios baixados
- `total_responsaveis_offline`: Total de responsáveis baixados
- `total_salas_offline`: Total de salas baixadas

### Tabelas SQLite
1. **patrimonio_offline**: Patrimônios para uso offline
2. **responsavel_offline**: Responsáveis para uso offline
3. **sala_offline**: Salas para uso offline
4. **coleta_offline**: Coletas pendentes de sincronização (já existente)

## Próximos Passos

### Pendente de Implementação

1. **Integração com ColetaViewModel**
   - Modificar para usar OfflineDataManager quando offline
   - Detectar automaticamente modo offline/online

2. **Sincronização de Coletas**
   - Implementar upload de coletas pendentes
   - Tratamento de conflitos
   - Retry automático

3. **Indicadores Visuais**
   - Badge de "offline" na interface
   - Contador de coletas pendentes
   - Status de sincronização em tempo real

4. **Otimizações**
   - Download incremental (apenas dados novos/alterados)
   - Compressão de dados
   - Download em background

5. **Testes**
   - Testes unitários dos DAOs
   - Testes de integração da sincronização
   - Testes de cenários offline/online

## Configuração Necessária

### Dependências (já devem estar no build.gradle)
```gradle
// Room Database
implementation "androidx.room:room-runtime:2.5.0"
implementation "androidx.room:room-ktx:2.5.0"
kapt "androidx.room:room-compiler:2.5.0"

// Coroutines
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4"

// Hilt (Dependency Injection)
implementation "com.google.dagger:hilt-android:2.44"
kapt "com.google.dagger:hilt-compiler:2.44"
```

### Permissões (AndroidManifest.xml)
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## Notas Técnicas

- **Paginação**: Download de patrimônios em lotes de 100 para evitar timeout
- **Índices**: Criados para otimizar buscas frequentes
- **Transações**: Operações em lote usam transações para performance
- **Thread Safety**: Todas as operações de banco são executadas em coroutines IO
- **Feedback**: Progresso detalhado durante download de dados

## Segurança

- Dados offline são armazenados apenas no dispositivo
- Limpeza de dados ao desinstalar o app
- Sem cache de credenciais sensíveis
- Validação de dados antes de sincronizar

## Performance

- Download otimizado com paginação
- Índices de banco para buscas rápidas
- Operações assíncronas não bloqueiam UI
- Cache de estatísticas para evitar queries repetidas
