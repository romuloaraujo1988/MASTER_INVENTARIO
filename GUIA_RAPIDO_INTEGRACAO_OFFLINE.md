# 🚀 Guia Rápido - Integração do Modo Offline

**Tempo de Integração:** 5 minutos por Activity  
**Dificuldade:** ⭐ Fácil

---

## 📋 Passo a Passo

### 1. Herdar BaseActivity

```kotlin
// ANTES
class MinhaActivity : AppCompatActivity() {

// DEPOIS
class MinhaActivity : BaseActivity() {
```

### 2. Adicionar Indicador Offline

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_minha)
    
    // Adicionar esta linha
    setupOfflineIndicator()
}
```

### 3. Pronto! 🎉

O indicador já está funcionando:
- 🟢 Escondido quando online
- 🟠 Visível quando offline
- 🔵 Mostra "Sincronizando..." durante sync

---

## 🎯 Exemplo Completo

```kotlin
package com.inventario.mobile.ui.minha

import android.os.Bundle
import com.inventario.mobile.R
import com.inventario.mobile.ui.base.BaseActivity

class MinhaActivity : BaseActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_minha)
        
        // Adicionar indicador offline
        setupOfflineIndicator()
        
        // Seu código normal aqui...
    }
    
    // OPCIONAL: Reagir a mudanças de rede
    override fun onNetworkStatusChanged(isConnected: Boolean) {
        if (isConnected) {
            // Reconectou - fazer algo
            showSyncingIndicator()
        } else {
            // Desconectou - avisar usuário
            showIndicatorMessage("Trabalhando offline")
        }
    }
}
```

---

## 🔧 Métodos Úteis da BaseActivity

```kotlin
// Verificar conectividade
if (isOnline()) {
    // Fazer requisição
}

if (isOffline()) {
    // Usar dados locais
}

// Mostrar indicador de sync
showSyncingIndicator()

// Mensagem customizada
showIndicatorMessage("5 itens pendentes")

// Mensagem de erro
showIndicatorMessage("Erro ao sincronizar", isError = true)
```

---

## 📱 Activities Prioritárias para Integração

### Alta Prioridade
- [x] `ColetaActivity` - Coleta de patrimônios
- [x] `SalaSelectionActivity` - Seleção de salas
- [x] `ScannerActivity` - Scanner QR Code
- [x] `ManualCollectionActivity` - Coleta manual

### Média Prioridade
- [ ] `DashboardFragment` - Dashboard principal
- [ ] `CollectionViewActivity` - Visualização de coletas
- [ ] `SyncActivity` - Tela de sincronização

### Baixa Prioridade
- [ ] `SettingsActivity` - Configurações
- [ ] `StatisticsActivity` - Estatísticas
- [ ] `LoginActivity` - Login (sempre online)

---

## ✅ Checklist de Integração

Para cada Activity:

- [ ] Mudar herança para `BaseActivity`
- [ ] Adicionar `setupOfflineIndicator()` no `onCreate()`
- [ ] Testar com internet
- [ ] Testar sem internet
- [ ] Verificar que indicador aparece/esconde corretamente

---

## 🧪 Como Testar

### Teste Rápido (30 segundos)

1. Abrir Activity com internet
   - ✅ Indicador deve estar escondido

2. Desligar WiFi/Dados
   - ✅ Indicador laranja aparece: "Modo Offline"

3. Religar WiFi/Dados
   - ✅ Indicador azul aparece: "Sincronizando..."
   - ✅ Depois esconde automaticamente

---

## 🎨 Customização Rápida

### Mudar Cor de Fundo

Editar `res/layout/view_offline_indicator.xml`:
```xml
<LinearLayout
    android:background="#FFF3E0"  <!-- Mudar aqui -->
```

### Mudar Texto

```kotlin
// No código
indicator.setCustomMessage("Seu texto aqui")
```

---

## 🐛 Troubleshooting

### Indicador não aparece
- Verificar se chamou `setupOfflineIndicator()`
- Verificar se herdou `BaseActivity`

### Indicador não esconde
- Verificar conectividade real do dispositivo
- Verificar logs: `NetworkUtils`

### Erro de compilação
- Verificar imports
- Sync Gradle
- Clean + Rebuild

---

## 📊 Resultado Esperado

**Antes:**
- ❌ Usuário não sabe se está offline
- ❌ Erros sem explicação
- ❌ Confusão sobre sincronização

**Depois:**
- ✅ Indicador visual claro
- ✅ Feedback imediato
- ✅ Usuário sempre informado

---

**Tempo Total:** 5 minutos por Activity  
**Benefício:** 🚀 UX muito melhor  
**Dificuldade:** ⭐ Muito fácil

