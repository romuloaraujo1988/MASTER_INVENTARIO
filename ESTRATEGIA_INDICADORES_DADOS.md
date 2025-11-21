# Estratégia de Indicadores de Dados - Local vs Sincronizado

## 🎯 Objetivo

Permitir que o usuário saiba claramente se está trabalhando com dados locais (offline) ou sincronizados (online).

---

## 📊 Indicadores Visuais

### 1. Barra de Status Global (MainFrame)

**Localização:** Rodapé do MainFrame

```
┌─────────────────────────────────────────────────────────────┐
│  [Módulos...]                                                │
│                                                              │
│  [Conteúdo Principal]                                        │
│                                                              │
├─────────────────────────────────────────────────────────────┤
│  🟢 ONLINE - Dados sincronizados | Última sync: 21/11 15:30 │
└─────────────────────────────────────────────────────────────┘
```

**Estados:**
- 🟢 **ONLINE** - Conectado ao servidor, dados sincronizados
- 🟡 **OFFLINE** - Modo offline, usando dados locais
- 🔴 **DESATUALIZADO** - Dados locais desatualizados (>7 dias)
- 🔄 **SINCRONIZANDO** - Sincronização em andamento

### 2. Badge nos Títulos das Telas

**Exemplo em ColetaFrame:**
```
┌─────────────────────────────────────────┐
│  Coleta de Patrimônios [OFFLINE] 🟡     │
├─────────────────────────────────────────┤
│  [Formulário de coleta...]              │
└─────────────────────────────────────────┘
```

### 3. Tooltip Informativo

Ao passar o mouse sobre o indicador:
```
🟡 MODO OFFLINE
─────────────────────────
• Usando dados locais
• Última sincronização: 21/11/2025 15:30
• 15 coletas pendentes de sincronização
• Clique para sincronizar agora
```

### 4. Ícones em Listas e Tabelas

**Patrimônios:**
```
┌──────────────────────────────────────────┐
│  Nº        Descrição           Status    │
├──────────────────────────────────────────┤
│  12345     Cadeira            🟢 Sync    │
│  12346     Mesa               🟡 Local   │
│  12347     Computador         🔄 Pend.   │
└──────────────────────────────────────────┘
```

**Legenda:**
- 🟢 **Sync** - Sincronizado com servidor
- 🟡 **Local** - Apenas local, não sincronizado
- 🔄 **Pend.** - Pendente de sincronização

### 5. Contador de Pendências

**No MainFrame:**
```
┌─────────────────────────────────────┐
│  📊 Estatísticas                     │
│  ─────────────────────────────────  │
│  Coletas pendentes: 15 🔄           │
│  Última sincronização: há 2 horas   │
│  [Sincronizar Agora]                │
└─────────────────────────────────────┘
```

---

## 🎨 Cores e Ícones

### Paleta de Cores
```java
// Online/Sincronizado
Color ONLINE_COLOR = new Color(46, 204, 113);  // Verde

// Offline/Local
Color OFFLINE_COLOR = new Color(243, 156, 18); // Laranja

// Desatualizado
Color OUTDATED_COLOR = new Color(231, 76, 60); // Vermelho

// Sincronizando
Color SYNCING_COLOR = new Color(52, 152, 219); // Azul
```

### Ícones Unicode
- 🟢 Online: `\u1F7E2`
- 🟡 Offline: `\u1F7E1`
- 🔴 Desatualizado: `\u1F534`
- 🔄 Sincronizando: `\u1F504`
- ⚠️ Aviso: `\u26A0`
- ✅ Sucesso: `\u2705`

---

## 💾 Metadados de Sincronização

### Tabela SQLite: sync_metadata
```sql
CREATE TABLE IF NOT EXISTS sync_metadata (
    id INTEGER PRIMARY KEY,
    entity_type VARCHAR(50),      -- 'patrimonio', 'sala', etc
    entity_id INTEGER,
    last_sync TIMESTAMP,
    sync_status VARCHAR(20),      -- 'synced', 'pending', 'local'
    created_locally BOOLEAN,
    modified_locally BOOLEAN
);
```

### Campos Adicionais nas Entidades
```java
public class Patrimonio {
    // ... campos existentes
    
    // Metadados de sincronização
    private LocalDateTime lastSync;
    private SyncStatus syncStatus;
    private boolean createdLocally;
    private boolean modifiedLocally;
}

enum SyncStatus {
    SYNCED,      // Sincronizado com servidor
    PENDING,     // Pendente de sincronização
    LOCAL_ONLY,  // Apenas local
    CONFLICT     // Conflito detectado
}
```

---

## 🔔 Notificações ao Usuário

### 1. Ao Entrar em Modo Offline
```
┌─────────────────────────────────────┐
│  ⚠️ Modo Offline Ativado            │
├─────────────────────────────────────┤
│  O sistema está operando com dados  │
│  locais. Suas coletas serão salvas  │
│  localmente e sincronizadas quando  │
│  a conexão for restaurada.          │
│                                     │
│  [OK]                               │
└─────────────────────────────────────┘
```

### 2. Ao Reconectar
```
┌─────────────────────────────────────┐
│  ✅ Conexão Restaurada              │
├─────────────────────────────────────┤
│  Você tem 15 coletas pendentes.     │
│  Deseja sincronizar agora?          │
│                                     │
│  [Agora]  [Depois]                  │
└─────────────────────────────────────┘
```

### 3. Dados Desatualizados
```
┌─────────────────────────────────────┐
│  ⚠️ Dados Desatualizados            │
├─────────────────────────────────────┤
│  Seus dados locais não são          │
│  atualizados há 8 dias.             │
│                                     │
│  Recomendamos sincronizar para      │
│  obter as informações mais          │
│  recentes.                          │
│                                     │
│  [Sincronizar]  [Ignorar]           │
└─────────────────────────────────────┘
```

---

## 📱 Implementação por Tela

### MainFrame
- ✅ Barra de status no rodapé
- ✅ Indicador de modo (Online/Offline)
- ✅ Contador de pendências
- ✅ Botão "Sincronizar Agora"
- ✅ Última data de sincronização

### ColetaFrame
- ✅ Badge no título
- ✅ Indicador de salvamento local
- ✅ Mensagem ao salvar: "Coleta salva localmente"

### PatrimonioFrame
- ✅ Ícones nas linhas da tabela
- ✅ Filtro por status de sincronização
- ✅ Coluna "Status Sync"

### RelatorioFrame
- ✅ Aviso se dados estão desatualizados
- ✅ Data da última sincronização no rodapé

---

## 🔧 Classes de Suporte

### StatusBarPanel.java
```java
public class StatusBarPanel extends JPanel {
    private JLabel lblStatus;
    private JLabel lblLastSync;
    private JButton btnSync;
    
    public void updateStatus(ConnectionStatus status) {
        // Atualiza indicador visual
    }
}
```

### SyncStatusManager.java
```java
public class SyncStatusManager {
    public SyncStatus getEntityStatus(String type, int id);
    public int getPendingCount();
    public LocalDateTime getLastSyncTime();
    public boolean isDataOutdated();
}
```

---

## ✅ Checklist de Implementação

### Fase 3A: Componentes Base
- [ ] Criar `StatusBarPanel.java`
- [ ] Criar `SyncStatusManager.java`
- [ ] Adicionar metadados de sincronização no SQLite
- [ ] Criar enum `SyncStatus`

### Fase 3B: MainFrame
- [ ] Adicionar barra de status no rodapé
- [ ] Implementar indicador de modo
- [ ] Adicionar contador de pendências
- [ ] Implementar botão "Sincronizar Agora"

### Fase 3C: Outras Telas
- [ ] Adicionar badges nos títulos
- [ ] Implementar ícones em tabelas
- [ ] Adicionar tooltips informativos

### Fase 3D: Notificações
- [ ] Implementar notificação ao entrar offline
- [ ] Implementar notificação ao reconectar
- [ ] Implementar alerta de dados desatualizados

---

**Estratégia definida!** 🎯  
**Próximo passo:** Implementar Fase 3 com integração no JLogin
