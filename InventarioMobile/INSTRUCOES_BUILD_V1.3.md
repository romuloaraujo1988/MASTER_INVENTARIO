# Instruções de Build - Versão 1.3.0

## 📋 Pré-requisitos

### Software Necessário
- ✅ Android Studio Arctic Fox ou superior
- ✅ JDK 11 ou superior
- ✅ Android SDK (API 21 - 34)
- ✅ Gradle 8.4 (incluído no projeto)

### Verificar Instalação
```bash
# Verificar Java
java -version

# Verificar Gradle (no diretório do projeto)
.\gradlew.bat --version
```

---

## 🔨 Build do Projeto

### 1. Build Debug (Desenvolvimento)

#### Via Linha de Comando
```bash
# Navegar até o diretório do app
cd InventarioMobile

# Limpar build anterior
.\gradlew.bat clean

# Compilar versão debug
.\gradlew.bat assembleDebug

# APK gerado em:
# app/build/outputs/apk/debug/app-debug.apk
```

#### Via Android Studio
1. Abrir Android Studio
2. File → Open → Selecionar pasta `InventarioMobile`
3. Build → Build Bundle(s) / APK(s) → Build APK(s)
4. Aguardar conclusão
5. Clicar em "locate" para ver o APK

### 2. Build Release (Produção)

#### Via Linha de Comando
```bash
# Compilar versão release
.\gradlew.bat assembleRelease

# APK gerado em:
# app/build/outputs/apk/release/app-release-unsigned.apk
```

#### Assinar APK (Necessário para produção)
```bash
# Gerar keystore (primeira vez)
keytool -genkey -v -keystore inventario.keystore -alias inventario -keyalg RSA -keysize 2048 -validity 10000

# Assinar APK
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 -keystore inventario.keystore app-release-unsigned.apk inventario

# Alinhar APK
zipalign -v 4 app-release-unsigned.apk inventario-v1.3.0.apk
```

---

## 📱 Instalação

### 1. Instalação via ADB

#### Dispositivo Físico
```bash
# Conectar dispositivo via USB
# Habilitar "Depuração USB" no dispositivo

# Verificar conexão
adb devices

# Instalar APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Ou forçar reinstalação
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

#### Emulador
```bash
# Iniciar emulador
emulator -avd <nome_do_emulador>

# Instalar APK
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 2. Instalação Manual

1. Copiar APK para o dispositivo
2. Abrir gerenciador de arquivos
3. Tocar no APK
4. Permitir instalação de fontes desconhecidas (se necessário)
5. Confirmar instalação

### 3. Instalação via Android Studio

1. Conectar dispositivo ou iniciar emulador
2. Run → Run 'app'
3. Selecionar dispositivo
4. Aguardar instalação automática

---

## 🧪 Testes

### Testes Unitários
```bash
# Executar todos os testes
.\gradlew.bat test

# Executar testes específicos
.\gradlew.bat testDebugUnitTest
```

### Testes de Interface
```bash
# Executar testes instrumentados
.\gradlew.bat connectedAndroidTest
```

### Verificar Código
```bash
# Lint
.\gradlew.bat lint

# Detekt (análise estática)
.\gradlew.bat detekt
```

---

## 🔍 Verificação de Build

### Verificar Integridade
```bash
# Dry-run (simular build)
.\gradlew.bat assembleDebug --dry-run

# Build com logs detalhados
.\gradlew.bat assembleDebug --info

# Build com stack trace
.\gradlew.bat assembleDebug --stacktrace
```

### Verificar Dependências
```bash
# Listar dependências
.\gradlew.bat dependencies

# Verificar atualizações
.\gradlew.bat dependencyUpdates
```

---

## 📦 Geração de Release

### Checklist Pré-Release
- [ ] Atualizar versionCode em build.gradle
- [ ] Atualizar versionName em build.gradle
- [ ] Executar testes
- [ ] Verificar lint
- [ ] Atualizar CHANGELOG
- [ ] Criar tag no Git

### Build Completo
```bash
# 1. Limpar projeto
.\gradlew.bat clean

# 2. Executar testes
.\gradlew.bat test

# 3. Verificar lint
.\gradlew.bat lint

# 4. Build release
.\gradlew.bat assembleRelease

# 5. Assinar APK (ver seção anterior)
```

### Versionamento
```gradle
// Em app/build.gradle
android {
    defaultConfig {
        versionCode 13      // Incrementar a cada release
        versionName "1.3.0" // Seguir Semantic Versioning
    }
}
```

---

## 🐛 Solução de Problemas

### Erro: "SDK location not found"
```bash
# Criar arquivo local.properties
echo sdk.dir=C:\\Users\\<usuario>\\AppData\\Local\\Android\\Sdk > local.properties
```

### Erro: "Gradle sync failed"
```bash
# Limpar cache do Gradle
.\gradlew.bat clean
.\gradlew.bat --stop

# Deletar pastas .gradle e build
rm -rf .gradle build app/build
```

### Erro: "Out of memory"
```bash
# Aumentar memória do Gradle
# Editar gradle.properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxPermSize=512m
```

### Erro: "Duplicate class"
```bash
# Limpar e rebuild
.\gradlew.bat clean build --refresh-dependencies
```

### Erro de Assinatura
```bash
# Verificar keystore
keytool -list -v -keystore inventario.keystore

# Recriar se necessário
```

---

## 📊 Tamanho do APK

### Verificar Tamanho
```bash
# Debug APK
ls -lh app/build/outputs/apk/debug/app-debug.apk

# Release APK
ls -lh app/build/outputs/apk/release/app-release.apk
```

### Reduzir Tamanho
```gradle
// Em app/build.gradle
android {
    buildTypes {
        release {
            minifyEnabled true
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

---

## 🚀 Deploy

### Google Play Store
1. Criar conta de desenvolvedor
2. Criar novo aplicativo
3. Upload do APK/AAB assinado
4. Preencher informações da loja
5. Enviar para revisão

### Distribuição Interna
1. Compartilhar APK via:
   - E-mail
   - Drive/Dropbox
   - Servidor interno
2. Instruir usuários sobre instalação manual

### Firebase App Distribution
```bash
# Instalar Firebase CLI
npm install -g firebase-tools

# Login
firebase login

# Deploy
firebase appdistribution:distribute app-debug.apk \
  --app <app-id> \
  --groups testers
```

---

## 📝 Logs e Debug

### Visualizar Logs
```bash
# Logs em tempo real
adb logcat

# Filtrar por tag
adb logcat -s InventarioActivity

# Salvar logs em arquivo
adb logcat > logs.txt
```

### Debug Remoto
```bash
# Habilitar debug remoto
adb tcpip 5555

# Conectar via IP
adb connect <ip-do-dispositivo>:5555
```

---

## 🔐 Segurança

### Ofuscar Código
```gradle
// proguard-rules.pro
-keep class com.inventario.mobile.** { *; }
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}
```

### Remover Logs de Produção
```kotlin
// Usar BuildConfig
if (BuildConfig.DEBUG) {
    Log.d("TAG", "Debug message")
}
```

---

## 📚 Recursos Adicionais

### Documentação
- [Android Developer Guide](https://developer.android.com/guide)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Gradle User Manual](https://docs.gradle.org/current/userguide/userguide.html)

### Ferramentas
- [Android Studio](https://developer.android.com/studio)
- [Scrcpy](https://github.com/Genymobile/scrcpy) - Espelhar tela
- [Vysor](https://www.vysor.io/) - Controle remoto

---

## ✅ Checklist de Build

### Antes do Build
- [ ] Código commitado no Git
- [ ] Testes passando
- [ ] Lint sem erros críticos
- [ ] Versão atualizada

### Durante o Build
- [ ] Build sem erros
- [ ] APK gerado com sucesso
- [ ] Tamanho do APK aceitável

### Após o Build
- [ ] APK instalado e testado
- [ ] Funcionalidades verificadas
- [ ] Performance aceitável
- [ ] Tag criada no Git

---

**Versão**: 1.3.0  
**Última Atualização**: 03/11/2025  
**Autor**: Sistema SIHCP
