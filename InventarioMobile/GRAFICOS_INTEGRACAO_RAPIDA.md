# 🚀 Integração Rápida - Gráficos no App

## Checklist de 5 Minutos

### ✅ Passo 1: Atualizar Versão do Banco (30s)

```kotlin
// app/src/main/java/com/inventario/mobile/data/local/AppDatabase.kt
@Database(
    entities = [...],
    version = 2, // ← Mudar de 1 para 2
    exportSchema = true
)
```

### ✅ Passo 2: Adicionar Migração (1min)

```kotlin
// No DatabaseModule.kt
@Provides
@Singleton
fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
    return Room.databaseBuilder(context, AppDatabase::class.java, "inventario_db")
        .fallbackToDestructiveMigration() // ← Adicionar esta linha (apenas dev)
        .build()
}
```

### ✅ Passo 3: Adicionar ao Navigation (2min)

```xml
<!-- res/navigation/nav_graph.xml -->
<fragment
    android:id="@+id/chartsFragment"
    android:name="com.inventario.mobile.presentation.charts.ChartsFragment"
    android:label="Gráficos"
    tools:layout="@layout/fragment_charts">
    <argument
        android:name="inventario_id"
        app:argType="integer"
        android:defaultValue="0" />
</fragment>
```

### ✅ Passo 4: Adicionar Botão no Dashboard (1min)

```kotlin
// No DashboardFragment.kt
binding.btnVerGraficos.setOnClickListener {
    val idInventario = 1 // ou pegar do ViewModel
    val action = DashboardFragmentDirections.actionDashboardToCharts(idInventario)
    findNavController().navigate(action)
}
```

```xml
<!-- No layout do Dashboard -->
<Button
    android:id="@+id/btnVerGraficos"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="📊 Ver Gráficos"
    android:textSize="16sp" />
```

### ✅ Passo 5: Testar (30s)

1. Desinstalar app antigo: `adb uninstall com.inventario.mobile`
2. Compilar: `./gradlew assembleDebug`
3. Instalar: `adb install app/build/outputs/apk/debug/app-debug.apk`
4. Abrir app e clicar em "Ver Gráficos"

---

## 🎯 Resultado Esperado

Você verá 5 gráficos:
1. **Cards de Estatísticas** - Total, Coletados, Pendentes, %
2. **Progresso** - Barras (Coletados vs Pendentes)
3. **Status** - Pizza (Ativos, Inativos, etc)
4. **Evolução** - Linhas (Acumulado por dia)
5. **Top 10** - Barras (Mais coletados)

---

## 🐛 Se Algo Der Errado

### App crasha ao abrir gráficos
```bash
# Ver erro no Logcat
adb logcat | grep -i "error\|exception"
```

### Gráficos vazios
- Verifique se há dados no banco
- Use dados de exemplo (já implementado como fallback)

### Erro de compilação
```bash
# Limpar e recompilar
./gradlew clean
./gradlew assembleDebug
```

---

## 📚 Documentação Completa

- `GRAFICOS_ANDROID_IMPLEMENTACAO.md` - Guia completo
- `MIGRACAO_BANCO_GRAFICOS.md` - Detalhes da migração

---

**Tempo Total**: ~5 minutos  
**Dificuldade**: Fácil  
**Resultado**: Gráficos funcionando! 📊
