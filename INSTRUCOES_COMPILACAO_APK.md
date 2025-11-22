# 📱 Instruções - Compilação do APK

**Data:** 22/11/2025  
**Versão:** 2.0.0 (com melhorias offline)

---

## 🚀 Compilação Rápida

### Opção 1: Script Automático (Recomendado)

```batch
compilar-apk-melhorias.bat
```

Este script:
1. Limpa build anterior
2. Compila APK debug
3. Verifica se foi gerado com sucesso

---

### Opção 2: Manual via Gradle

```batch
cd InventarioMobile
.\gradlew.bat assembleDebug
```

---

### Opção 3: Android Studio

1. Abrir projeto `InventarioMobile` no Android Studio
2. Menu: Build → Build Bundle(s) / APK(s) → Build APK(s)
3. Aguardar compilação
4. Clicar em "locate" quando aparecer notificação

---

## ⏱️ Tempo Esperado

- **Primeira compilação:** 5-10 minutos
- **Compilações subsequentes:** 2-3 minutos
- **Com cache:** 1-2 minutos

---

## 📁 Localização do APK

Após compilação bem-sucedida:

```
InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
```

---

## ✅ Verificar Compilação

### Verificar se APK existe

```batch
dir InventarioMobile\app\build\outputs\apk\debug\app-debug.apk
```

### Verificar tamanho

O APK deve ter aproximadamente:
- **Tamanho:** 15-25 MB
- **Versão:** 2.0.0

---

## 🐛 Problemas Comuns

### Problema 1: Compilação Muito Lenta

**Causa:** Primeira compilação ou muitas mudanças  
**Solução:** Aguardar (normal demorar 5-10 minutos)

### Problema 2: Erro de Memória

**Causa:** Gradle sem memória suficiente  
**Solução:** 

```batch
# Editar gradle.properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=512m
```

### Problema 3: Erro de Dependências

**Causa:** Dependências não baixadas  
**Solução:**

```batch
cd InventarioMobile
.\gradlew.bat --refresh-dependencies assembleDebug
```

### Problema 4: Daemon Travado

**Causa:** Processo Gradle travado  
**Solução:**

```batch
cd InventarioMobile
.\gradlew.bat --stop
.\gradlew.bat assembleDebug
```

---

## 🔧 Compilação Otimizada

### Para Desenvolvimento (Mais Rápido)

```batch
cd InventarioMobile
.\gradlew.bat assembleDebug --parallel --build-cache
```

### Para Produção (Release)

```batch
cd InventarioMobile
.\gradlew.bat assembleRelease
```

**Nota:** Release requer keystore configurado

---

## 📊 Status da Compilação

### Durante Compilação

Você verá progresso como:
```
> Task :app:compileDebugKotlin
> Task :app:processDebugResources
> Task :app:mergeDebugAssets
...
```

### Compilação Bem-Sucedida

```
BUILD SUCCESSFUL in 3m 45s
123 actionable tasks: 123 executed
```

### Compilação com Erro

```
BUILD FAILED in 2m 10s

FAILURE: Build failed with an exception.
```

---

## 🎯 Após Compilação

### Instalar no Emulador

```batch
cd InventarioMobile
adb install app\build\outputs\apk\debug\app-debug.apk
```

### Instalar em Dispositivo Real

1. Conectar dispositivo via USB
2. Habilitar "Depuração USB" no dispositivo
3. Executar:

```batch
cd InventarioMobile
adb devices
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### Copiar APK para Distribuição

```batch
copy InventarioMobile\app\build\outputs\apk\debug\app-debug.apk dist\app-melhorias-offline-v2.0.0.apk
```

---

## 📝 Logs de Compilação

### Ver Logs Detalhados

```batch
cd InventarioMobile
.\gradlew.bat assembleDebug --info > build-log.txt
```

### Ver Apenas Erros

```batch
cd InventarioMobile
.\gradlew.bat assembleDebug --quiet
```

---

## ✅ Checklist Pré-Compilação

Antes de compilar, verificar:

- [ ] Java JDK 17+ instalado
- [ ] Android SDK instalado
- [ ] Variáveis de ambiente configuradas
- [ ] Gradle atualizado
- [ ] Dependências sincronizadas
- [ ] Sem erros de código

---

## 🚀 Compilação em Background

Para compilar sem travar o terminal:

```batch
start /B compilar-apk-melhorias.bat
```

Ou usar Android Studio que compila em background automaticamente.

---

## 📈 Melhorias Incluídas no APK

Este APK inclui:

- ✅ Indicador visual de modo offline
- ✅ Notificações de sincronização
- ✅ Sincronização automática ao reconectar
- ✅ NetworkUtils e observers
- ✅ BaseActivity para fácil integração

**Versão:** 2.0.0  
**Data:** 22/11/2025

---

## 🎉 Próximos Passos

Após compilação bem-sucedida:

1. Instalar APK em dispositivo de teste
2. Testar indicador offline
3. Testar notificações
4. Testar sincronização automática
5. Validar todas as funcionalidades

---

**Boa compilação!** 🚀

