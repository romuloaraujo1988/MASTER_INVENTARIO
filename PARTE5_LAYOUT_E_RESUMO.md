# 📱 Sistema de Sincronização Offline - Parte 5

## Layout XML

### fragment_sync.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fillViewport="true">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">

        <!-- Título -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Sincronização de Dados"
            android:textSize="24sp"
            android:textStyle="bold"
            android:layout_marginBottom="24dp"/>

        <!-- Card de Estatísticas -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardElevation="4dp"
            app:cardCornerRadius="8dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="Dados Locais"
                    android:textSize="18sp"
                    android:textStyle="bold"
                    android:layout_marginBottom="12dp"/>

                <!-- Patrimônios -->
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:layout_marginBottom="8dp">

                    <TextView
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:text="Patrimônios:"
                        android:textSize="16sp"/>

                    <TextView
                        android:id="@+id/tvTotalPatrimonios"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="0"
                        android:textSize="16sp"
                        android:textStyle="bold"/>
                </LinearLayout>

                <!-- Salas -->
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:layout_marginBottom="8dp">

                    <TextView
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:text="Salas:"
                        android:textSize="16sp"/>

                    <TextView
                        android:id="@+id/tvTotalSalas"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="0"
                        android:textSize="16sp"
                        android:textStyle="bold"/>
                </LinearLayout>

                <!-- Responsáveis -->
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:layout_marginBottom="8dp">

                    <TextView
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:text="Responsáveis:"
                        android:textSize="16sp"/>

                    <TextView
                        android:id="@+id/tvTotalResponsaveis"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="0"
                        android:textSize="16sp"
                        android:textStyle="bold"/>
                </LinearLayout>

                <!-- Coletas Pendentes -->
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:layout_marginBottom="8dp">

                    <TextView
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:text="Coletas Pendentes:"
                        android:textSize="16sp"
                        android:textColor="@color/orange_warning"/>

                    <TextView
                        android:id="@+id/tvColetasPendentes"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="0"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:textColor="@color/orange_warning"/>

                    <TextView
                        android:id="@+id/badgeColetasPendentes"
                        android:layout_width="24dp"
                        android:layout_height="24dp"
                        android:layout_marginStart="8dp"
                        android:background="@drawable/badge_circle"
                        android:gravity="center"
                        android:text="0"
                        android:textColor="@android:color/white"
                        android:textSize="12sp"
                        android:visibility="gone"/>
                </LinearLayout>

                <!-- Última Sincronização -->
                <View
                    android:layout_width="match_parent"
                    android:layout_height="1dp"
                    android:background="@color/divider"
                    android:layout_marginVertical="8dp"/>

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="Última Sincronização:"
                    android:textSize="14sp"
                    android:textColor="@color/text_secondary"/>

                <TextView
                    android:id="@+id/tvUltimaSincronizacao"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="Nunca sincronizado"
                    android:textSize="14sp"
                    android:textStyle="bold"/>
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- Botões de Sincronização -->
        <LinearLayout
            android:id="@+id/layoutButtons"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical">

            <!-- Sincronização Completa -->
            <Button
                android:id="@+id/btnSyncCompleto"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="🔄 Sincronização Completa"
                android:textSize="16sp"
                android:padding="16dp"
                android:layout_marginBottom="12dp"
                app:backgroundTint="@color/blue_primary"/>

            <!-- Sincronização Incremental -->
            <Button
                android:id="@+id/btnSyncIncremental"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="⚡ Atualizar Dados"
                android:textSize="16sp"
                android:padding="16dp"
                android:layout_marginBottom="12dp"
                app:backgroundTint="@color/green_success"/>

            <!-- Sincronizar Coletas -->
            <Button
                android:id="@+id/btnSyncColetas"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="📤 Enviar Coletas Pendentes"
                android:textSize="16sp"
                android:padding="16dp"
                app:backgroundTint="@color/orange_warning"/>
        </LinearLayout>

        <!-- Loading -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:gravity="center"
            android:layout_marginTop="24dp">

            <ProgressBar
                android:id="@+id/progressBar"
                android:layout_width="48dp"
                android:layout_height="48dp"
                android:visibility="gone"/>

            <TextView
                android:id="@+id/tvLoadingMessage"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="8dp"
                android:text="Sincronizando..."
                android:textSize="14sp"
                android:visibility="gone"/>
        </LinearLayout>

        <!-- Informações -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="24dp"
            app:cardElevation="2dp"
            app:cardCornerRadius="8dp"
            app:cardBackgroundColor="@color/info_background">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="ℹ️ Informações"
                    android:textSize="16sp"
                    android:textStyle="bold"
                    android:layout_marginBottom="8dp"/>

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="• Sincronização Completa: Baixa todos os dados novamente\n\n• Atualizar Dados: Baixa apenas as alterações recentes\n\n• Enviar Coletas: Envia coletas offline para o servidor"
                    android:textSize="14sp"
                    android:lineSpacingExtra="4dp"/>
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

    </LinearLayout>
</ScrollView>
```

---

## 📊 Resumo do Sistema

### ✅ Backend (API)
- ✅ `MobileSyncController` - Endpoints de sincronização
- ✅ `MobileSyncService` - Lógica de preparação de dados
- ✅ Endpoints: `/sync/full`, `/sync/patrimonios`, `/sync/salas`, etc.

### ✅ Android (App)
- ✅ Room Database com 5 tabelas
- ✅ DAOs para acesso aos dados
- ✅ `SyncRepository` - Gerencia sincronização
- ✅ `SyncViewModel` - Lógica de UI
- ✅ `SyncFragment` - Interface do usuário
- ✅ Layout completo e responsivo

### 🎯 Funcionalidades

1. **Sincronização Completa**
   - Baixa TODOS os dados
   - Limpa banco local
   - Insere dados novos

2. **Sincronização Incremental**
   - Baixa apenas atualizações
   - Mais rápido
   - Economiza dados

3. **Sincronização de Coletas**
   - Envia coletas offline
   - Marca como sincronizadas
   - Limpa após envio

4. **Estatísticas**
   - Total de dados locais
   - Coletas pendentes
   - Última sincronização

### 🚀 Uso

```kotlin
// No app
val viewModel: SyncViewModel by viewModels()

// Sincronizar completo (primeira vez)
viewModel.sincronizarCompleto()

// Atualizar dados (diariamente)
viewModel.sincronizarIncremental()

// Enviar coletas (quando online)
viewModel.sincronizarColetas()
```

### 📱 Fluxo Ideal

```
1. Primeira vez: Sincronização Completa
   ↓
2. Trabalhar offline (coletas salvas localmente)
   ↓
3. Quando online: Enviar Coletas Pendentes
   ↓
4. Diariamente: Atualizar Dados (incremental)
```

### 💡 Benefícios

✅ **Funciona 100% offline**
✅ **Sincronização rápida e eficiente**
✅ **Interface simples e intuitiva**
✅ **Dados sempre disponíveis**
✅ **Fácil de atualizar**

---

**Versão**: 2.0.0  
**Data**: 08/11/2025  
**Sistema Completo de Sincronização Offline** ✅
