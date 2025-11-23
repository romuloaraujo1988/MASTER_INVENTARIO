# Guia: Detecção Automática de Servidor

## 🎯 O Que Foi Implementado

Sistema completo de **detecção automática** que monitora quando o servidor volta a ficar disponível e **sincroniza automaticamente** as coletas pendentes.

---

## 📦 Componentes Criados

### 1. ConnectivityMonitor
**Arquivo:** `InventarioMobile/app/src/main/java/com/inventario/mobile/network/ConnectivityMonitor.kt`

**Função:** Monitora conectividade de rede e disponibilidade do servidor

**Recursos:**
- ✅ Detecta quando internet está disponível
- ✅ Verifica se servidor está acessível (health check)
- ✅ Emite eventos via StateFlow
- ✅ Verificação periódica a cada 30 segundos
- ✅ Verificação imediata quando internet volta

**Estados:**
```kotlin
sealed class ConnectivityState {
    object NoInternet           // Sem internet
    object InternetAvailable    // Internet disponível
    object ServerAvailable      // Servidor acessível
    object ServerUnavailable    // Servidor indisponível
}
```

### 2. AutoSyncManager
**Arquivo:** `InventarioMobile/app/src/main/java/com/inventario/mobile/sync/AutoSyncManager.kt`

**Função:** Sincroniza automaticamente quando servidor volta

**Recursos:**
- ✅ Sincronização automática quando servidor volta
- ✅ Notificação ao usuário após sincronização
- ✅ Pode ser habilitado/desabilitado
- ✅ Emite eventos de progresso via StateFlow

**Estados:**
```kotlin
sealed class AutoSyncState {
    object Idle                      // Aguardando
    object Syncing                   // Sincronizando
    data class Success(val count: Int)  // Sucesso
    data class Error(val message: String)  // Erro
}
```

### 3. Health Check Endpoint (Backend)
**Arquivo:** `src/main/java/com/inventario/mobile/server/controller/MobileAuthController.java`

**Endpoint:** `GET /api/mobile/auth/health`

**Função:** Endpoint leve para verificar se servidor está disponível

**Resposta:**
```json
{
  "success": true,
  "message": "Servidor disponível",
  "data": "OK"
}
```

---

## 🚀 Como Usar

### Opção 1: Usar em Qualquer Activity/Fragment

```kotlin
import com.inventario.mobile.network.ConnectivityMonitor
import com.inventario.mobile.sync.AutoSyncManager

class MinhaActivity : AppCompatActivity() {
    
    private lateinit var connectivityMonitor: ConnectivityMonitor
    private lateinit var autoSyncManager: AutoSyncManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar
        connectivityMonitor = ConnectivityMonitor.getInstance(this)
        autoSyncManager = AutoSyncManager.getInstance(this)
        
        // Iniciar monitoramento
        autoSyncManager.start()
        
        // Observar conectividade
        lifecycleScope.launch {
            connectivityMonitor.connectivityState.collect { state ->
                when (state) {
                    is ConnectivityMonitor.ConnectivityState.ServerAvailable -> {
                        // Servidor disponível!
                        Toast.makeText(this@MinhaActivity, "✅ Servidor online", Toast.LENGTH_SHORT).show()
                    }
                    is ConnectivityMonitor.ConnectivityState.ServerUnavailable -> {
                        // Servidor indisponível
                        Toast.makeText(this@MinhaActivity, "❌ Servidor offline", Toast.LENGTH_SHORT).show()
                    }
                    // ... outros estados
                }
            }
        }
        
        // Observar sincronização automática
        lifecycleScope.launch {
            autoSyncManager.autoSyncState.collect { state ->
                when (state) {
                    is AutoSyncManager.AutoSyncState.Success -> {
                        Toast.makeText(this@MinhaActivity, "✅ ${state.count} coletas sincronizadas", Toast.LENGTH_SHORT).show()
                    }
                    // ... outros estados
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        autoSyncManager.stop()
    }
}
```

### Opção 2: Usar no Application (Recomendado)

```kotlin
class InventarioApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Iniciar monitoramento global
        val autoSyncManager = AutoSyncManager.getInstance(this)
        autoSyncManager.start()
        autoSyncManager.setEnabled(true)
    }
}
```

**Vantagem:** Funciona em todo o app automaticamente, sem precisar configurar em cada Activity.

### Opção 3: Verificação Manual

```kotlin
// Forçar verificação imediata do servidor
val connectivityMonitor = ConnectivityMonitor.getInstance(context)
connectivityMonitor.forceServerCheck()

// Verificar estado atual
val isServerAvailable = connectivityMonitor.isServerAvailable.value
val hasInternet = connectivityMonitor.hasInternet.value

if (isServerAvailable) {
    // Servidor disponível, pode fazer requisições
} else {
    // Servidor indisponível, usar modo offline
}
```

---

## 🔄 Fluxo Automático

```
1. App inicia
   ↓
2. AutoSyncManager.start() é chamado
   ↓
3. ConnectivityMonitor começa a monitorar
   ↓
4. Usuário perde conexão com servidor
   ↓
5. App detecta: ServerUnavailable
   ↓
6. Coletas são salvas localmente
   ↓
7. Servidor volta a ficar disponível
   ↓
8. ConnectivityMonitor detecta: ServerAvailable
   ↓
9. AutoSyncManager dispara sincronização automática
   ↓
10. Coletas pendentes são enviadas
    ↓
11. Notificação é mostrada ao usuário
    ↓
12. App volta ao estado normal
```

---

## 📊 Indicadores Visuais Recomendados

### 1. Ícone de Status na Toolbar

```kotlin
private fun updateServerStatusIcon(state: ConnectivityMonitor.ConnectivityState) {
    val icon = when (state) {
        is ConnectivityMonitor.ConnectivityState.NoInternet -> 
            R.drawable.ic_cloud_off
        is ConnectivityMonitor.ConnectivityState.ServerAvailable -> 
            R.drawable.ic_cloud_done
        is ConnectivityMonitor.ConnectivityState.ServerUnavailable -> 
            R.drawable.ic_cloud_queue
        else -> R.drawable.ic_cloud_sync
    }
    
    toolbar.menu.findItem(R.id.action_server_status)?.setIcon(icon)
}
```

### 2. Banner de Status

```kotlin
private fun showServerStatusBanner(state: ConnectivityMonitor.ConnectivityState) {
    when (state) {
        is ConnectivityMonitor.ConnectivityState.NoInternet -> {
            binding.bannerStatus.apply {
                visibility = View.VISIBLE
                setBackgroundColor(Color.RED)
                text = "❌ Sem internet - Modo offline"
            }
        }
        is ConnectivityMonitor.ConnectivityState.ServerAvailable -> {
            binding.bannerStatus.apply {
                visibility = View.VISIBLE
                setBackgroundColor(Color.GREEN)
                text = "✅ Servidor online"
                postDelayed({ visibility = View.GONE }, 3000) // Esconder após 3s
            }
        }
        is ConnectivityMonitor.ConnectivityState.ServerUnavailable -> {
            binding.bannerStatus.apply {
                visibility = View.VISIBLE
                setBackgroundColor(Color.ORANGE)
                text = "⚠️ Servidor offline - Dados salvos localmente"
            }
        }
    }
}
```

### 3. Snackbar Temporário

```kotlin
private fun showServerStatusSnackbar(state: ConnectivityMonitor.ConnectivityState) {
    val message = when (state) {
        is ConnectivityMonitor.ConnectivityState.ServerAvailable -> 
            "✅ Servidor online - Sincronizando..."
        is ConnectivityMonitor.ConnectivityState.ServerUnavailable -> 
            "⚠️ Servidor offline - Modo offline ativado"
        else -> return
    }
    
    Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
}
```

---

## ⚙️ Configurações

### Alterar Intervalo de Verificação

```kotlin
// Em ConnectivityMonitor.kt
companion object {
    private const val SERVER_CHECK_INTERVAL_MS = 30_000L // 30 segundos (padrão)
    // Alterar para 60_000L para 1 minuto
    // Alterar para 15_000L para 15 segundos
}
```

### Desabilitar Sincronização Automática

```kotlin
val autoSyncManager = AutoSyncManager.getInstance(context)
autoSyncManager.setEnabled(false) // Desabilitar
autoSyncManager.setEnabled(true)  // Habilitar
```

### Alterar Timeout do Health Check

```kotlin
// Em ConnectivityMonitor.kt
companion object {
    private const val SERVER_TIMEOUT_MS = 3_000L // 3 segundos (padrão)
    // Alterar para 5_000L para 5 segundos
}
```

---

## 🧪 Como Testar

### Teste 1: Servidor Offline → Online

```
1. Desligar servidor backend
2. Abrir app
3. Verificar que mostra "Servidor offline"
4. Coletar alguns patrimônios (salvos localmente)
5. Ligar servidor backend
6. Aguardar até 30 segundos
7. Verificar que app detecta servidor online
8. Verificar que coletas são sincronizadas automaticamente
9. Verificar notificação de sincronização
```

### Teste 2: Sem Internet → Com Internet

```
1. Desativar WiFi e dados móveis
2. Abrir app
3. Verificar que mostra "Sem internet"
4. Ativar WiFi ou dados móveis
5. Verificar que app detecta internet
6. Verificar que verifica servidor automaticamente
```

### Teste 3: Verificação Manual

```kotlin
// Em qualquer Activity
val connectivityMonitor = ConnectivityMonitor.getInstance(this)

// Forçar verificação
connectivityMonitor.forceServerCheck()

// Verificar resultado
lifecycleScope.launch {
    connectivityMonitor.isServerAvailable.collect { isAvailable ->
        Log.d("TEST", "Servidor disponível: $isAvailable")
    }
}
```

---

## 📝 Logs para Debug

O sistema gera logs detalhados:

```
ConnectivityMonitor:
  ✅ Rede disponível
  🔍 Verificando disponibilidade do servidor...
  ✅ Servidor DISPONÍVEL
  
AutoSyncManager:
  🚀 Iniciando AutoSyncManager
  🔄 Conectividade mudou: ServerAvailable
  🔄 Servidor voltou! Iniciando sincronização automática...
  📦 5 coletas pendentes encontradas
  ✅ Coleta 12345 sincronizada
  ✅ Sincronização automática concluída: 5/5
```

---

## 🎯 Benefícios

### Para o Usuário
- ✅ Não precisa lembrar de sincronizar manualmente
- ✅ Feedback visual sobre status do servidor
- ✅ Notificações quando sincronização ocorre
- ✅ App funciona offline sem problemas

### Para o Desenvolvedor
- ✅ Código centralizado e reutilizável
- ✅ Fácil de integrar em qualquer tela
- ✅ Logs detalhados para debug
- ✅ Configurável e extensível

### Para o Sistema
- ✅ Reduz perda de dados
- ✅ Sincronização eficiente
- ✅ Menos requisições desnecessárias
- ✅ Melhor experiência offline

---

## 🚨 Importante

### Permissões Necessárias (AndroidManifest.xml)

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

### Configurar Application

```kotlin
// No AndroidManifest.xml
<application
    android:name=".InventarioApplication"
    ...>
```

---

## 📚 Referências

- `ConnectivityMonitor.kt` - Monitor de conectividade
- `AutoSyncManager.kt` - Gerenciador de sincronização automática
- `MobileAuthController.java` - Endpoint de health check
- `MainActivity.kt` - Exemplo de uso

---

**Implementado em:** 22/11/2025  
**Versão:** 1.0.0  
**Status:** ✅ Pronto para uso

