# 🔧 Como Ativar os Workers

## 📋 Visão Geral

Os workers implementados precisam ser agendados no `Application` para funcionarem automaticamente.

---

## 🚀 Passo a Passo

### 1. Localizar o Application

Procure pelo arquivo `InventarioMobileApplication.kt`:

```
InventarioMobile/app/src/main/java/com/inventario/mobile/InventarioMobileApplication.kt
```

### 2. Adicionar Imports

```kotlin
import com.inventario.mobile.utils.SyncScheduler
```

### 3. Agendar Workers no onCreate()

```kotlin
class InventarioMobileApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Suas inicializações existentes...
        
        // ===== AGENDAR WORKERS =====
        
        // 1. Sincronização inteligente em background (a cada 15 minutos)
        SyncScheduler.scheduleSmartSync(
            context = this,
            intervalMinutes = 15
        )
        
        // 2. Limpeza automática do banco de dados (a cada 7 dias)
        SyncScheduler.scheduleDatabaseCleanup(
            context = this,
            intervalDays = 7
        )
        
        Log.d("Application", "Workers agendados com sucesso")
    }
}
```

---

## ⚙️ Configurações Opcionais

### Ajustar Intervalo de Sincronização

```kotlin
// Sincronizar a cada 30 minutos
SyncScheduler.scheduleSmartSync(this, intervalMinutes = 30)

// Sincronizar a cada 5 minutos (mais frequente)
SyncScheduler.scheduleSmartSync(this, intervalMinutes = 5)

// Sincronizar a cada 1 hora
SyncScheduler.scheduleSmartSync(this, intervalMinutes = 60)
```

### Ajustar Intervalo de Limpeza

```kotlin
// Limpar a cada 3 dias
SyncScheduler.scheduleDatabaseCleanup(this, intervalDays = 3)

// Limpar a cada 14 dias
SyncScheduler.scheduleDatabaseCleanup(this, intervalDays = 14)

// Limpar a cada 30 dias
SyncScheduler.scheduleDatabaseCleanup(this, intervalDays = 30)
```

---

## 🎛️ Controle Manual

### Forçar Sincronização Imediata

```kotlin
// Em qualquer Activity/Fragment
SyncScheduler.forceSyncNow(requireContext())
```

### Forçar Limpeza Imediata

```kotlin
// Em qualquer Activity/Fragment
SyncScheduler.forceCleanupNow(requireContext())
```

### Verificar Status dos Workers

```kotlin
val status = SyncScheduler.getWorkersStatus(requireContext())

Log.d("Workers", "Sync agendado: ${status.syncScheduled}")
Log.d("Workers", "Cleanup agendado: ${status.cleanupScheduled}")
Log.d("Workers", "Estado sync: ${status.syncState}")
Log.d("Workers", "Estado cleanup: ${status.cleanupState}")
```

### Cancelar Workers

```kotlin
// Cancelar sincronização
SyncScheduler.cancelSmartSync(this)

// Cancelar limpeza
SyncScheduler.cancelDatabaseCleanup(this)

// Cancelar todos
SyncScheduler.cancelAll(this)
```

---

## 🎨 Adicionar UI de Controle (Opcional)

### Tela de Configurações

```kotlin
class SettingsFragment : Fragment() {
    
    private val preferencesManager by lazy { PreferencesManager(requireContext()) }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupSyncSettings()
    }
    
    private fun setupSyncSettings() {
        // Switch de auto-sync
        binding.switchAutoSync.apply {
            isChecked = preferencesManager.isAutoSyncEnabled()
            setOnCheckedChangeListener { _, isChecked ->
                preferencesManager.setAutoSyncEnabled(isChecked)
                
                if (isChecked) {
                    SyncScheduler.scheduleSmartSync(requireContext())
                    showMessage("Sincronização automática ativada")
                } else {
                    SyncScheduler.cancelSmartSync(requireContext())
                    showMessage("Sincronização automática desativada")
                }
            }
        }
        
        // Switch de WiFi only
        binding.switchWifiOnly.apply {
            isChecked = preferencesManager.isWifiOnlySyncEnabled()
            setOnCheckedChangeListener { _, isChecked ->
                preferencesManager.setWifiOnlySyncEnabled(isChecked)
                showMessage(
                    if (isChecked) "Sincronizar apenas em WiFi"
                    else "Sincronizar em qualquer rede"
                )
            }
        }
        
        // Botão de sincronização manual
        binding.buttonSyncNow.setOnClickListener {
            SyncScheduler.forceSyncNow(requireContext())
            showMessage("Sincronização iniciada")
        }
        
        // Botão de limpeza manual
        binding.buttonCleanupNow.setOnClickListener {
            showConfirmDialog(
                title = "Limpar dados antigos?",
                message = "Isso irá remover coletas sincronizadas com mais de 30 dias.",
                onConfirm = {
                    SyncScheduler.forceCleanupNow(requireContext())
                    showMessage("Limpeza iniciada")
                }
            )
        }
        
        // Mostrar status
        updateWorkersStatus()
    }
    
    private fun updateWorkersStatus() {
        val status = SyncScheduler.getWorkersStatus(requireContext())
        
        binding.textSyncStatus.text = when {
            !status.syncScheduled -> "Desativado"
            status.syncState == "RUNNING" -> "Sincronizando..."
            status.syncState == "ENQUEUED" -> "Agendado"
            else -> "Ativo"
        }
        
        binding.textCleanupStatus.text = when {
            !status.cleanupScheduled -> "Desativado"
            status.cleanupState == "RUNNING" -> "Limpando..."
            status.cleanupState == "ENQUEUED" -> "Agendado"
            else -> "Ativo"
        }
    }
}
```

---

## 🧪 Testar Workers

### 1. Verificar se foram agendados

```bash
# Via logcat
adb logcat | findstr "Workers\|SmartSync\|DatabaseCleanup"
```

### 2. Forçar execução imediata

```kotlin
// No código
SyncScheduler.forceSyncNow(context)
SyncScheduler.forceCleanupNow(context)
```

### 3. Verificar no WorkManager Inspector

1. Abrir Android Studio
2. View > Tool Windows > App Inspection
3. Selecionar aba "Background Task Inspector"
4. Ver workers agendados e seu status

---

## 📊 Monitorar Execução

### Logs do SmartSyncWorker

```
D/SmartSyncWorker: SmartSyncWorker iniciado
D/SmartSyncWorker: Auto-sync desabilitado, pulando sincronização
// ou
D/SmartSyncWorker: Condições atendidas, iniciando sincronização...
D/SmartSyncWorker: Sincronização concluída com sucesso
```

### Logs do DatabaseCleanupWorker

```
D/DatabaseCleanup: Iniciando limpeza do banco de dados...
D/DatabaseCleanup: Deletadas 15 coletas antigas
D/DatabaseCleanup: Deletados 23 arquivos de cache antigos
D/DatabaseCleanup: Limpeza concluída com sucesso
```

---

## ⚠️ Troubleshooting

### Workers não estão executando

**Possíveis causas:**
1. Auto-sync desabilitado
2. Sem conexão de rede
3. Bateria baixa (< 20%)
4. WiFi only habilitado mas não está em WiFi

**Solução:**
```kotlin
// Verificar condições
val preferencesManager = PreferencesManager(context)
Log.d("Debug", "Auto-sync: ${preferencesManager.isAutoSyncEnabled()}")
Log.d("Debug", "WiFi only: ${preferencesManager.isWifiOnlySyncEnabled()}")

val networkMonitor = NetworkMonitor(context)
Log.d("Debug", "Conectado: ${networkMonitor.isConnected()}")
Log.d("Debug", "WiFi: ${networkMonitor.isWifiConnected()}")

val batteryManager = context.getSystemService<BatteryManager>()
val batteryLevel = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
Log.d("Debug", "Bateria: $batteryLevel%")
```

### Workers executando muito frequentemente

**Solução:** Aumentar intervalo

```kotlin
// Aumentar para 30 minutos
SyncScheduler.scheduleSmartSync(this, intervalMinutes = 30)
```

### Workers consumindo muita bateria

**Solução:** 
1. Aumentar intervalo de sincronização
2. Habilitar WiFi only
3. Verificar se há loops infinitos no código de sincronização

---

## 📋 Checklist de Ativação

- [ ] Adicionar imports no Application
- [ ] Agendar SmartSyncWorker no onCreate()
- [ ] Agendar DatabaseCleanupWorker no onCreate()
- [ ] Compilar e instalar app
- [ ] Verificar logs para confirmar agendamento
- [ ] Testar sincronização manual
- [ ] Testar limpeza manual
- [ ] Verificar status no WorkManager Inspector
- [ ] (Opcional) Adicionar UI de controle nas configurações

---

## 🎉 Conclusão

Após seguir estes passos, os workers estarão:

- ✅ Agendados automaticamente
- ✅ Executando em background
- ✅ Respeitando condições (rede, bateria)
- ✅ Mantendo app otimizado

**Os workers começarão a funcionar assim que o app for iniciado!**
