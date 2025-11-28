# Build Tracking - Registro de Compilações

## 🎯 Regra Obrigatória

**SEMPRE que compilar o APK Android, registrar a build no arquivo `BUILD_HISTORY.md`.**

---

## 📋 Formato do Registro

Após cada build bem-sucedida, adicionar entrada no arquivo `BUILD_HISTORY.md` na raiz do projeto:

```markdown
## Build #XXX - DD/MM/YYYY HH:MM

- **Tipo:** Debug / Release
- **Versão:** X.X.X (versionName do build.gradle)
- **Build Code:** XXX (versionCode do build.gradle)
- **Arquivo:** app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** XX MB
- **Mudanças:** Breve descrição das alterações desde a última build
- **Status:** ✅ Sucesso / ❌ Falha
```

---

## 📝 Exemplo de Registro

```markdown
## Build #001 - 28/11/2025 14:30

- **Tipo:** Debug
- **Versão:** 2.6.0
- **Build Code:** 26
- **Arquivo:** app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** 12.5 MB
- **Mudanças:** Correção de sincronização de coletas
- **Status:** ✅ Sucesso
```

---

## 🔧 Processo de Build

1. Executar comando de build:
   ```bash
   cd InventarioMobile
   .\gradlew.bat assembleDebug   # ou assembleRelease
   ```

2. Verificar resultado do build

3. Se sucesso, registrar no `BUILD_HISTORY.md`:
   - Incrementar número da build
   - Preencher todos os campos
   - Descrever mudanças relevantes

4. Informar ao usuário:
   - Número da build
   - Localização do APK
   - Mudanças incluídas

---

## 📊 Informações a Coletar

### Obter Versão do App
```bash
# No build.gradle do app
grep -E "versionName|versionCode" InventarioMobile/app/build.gradle
```

### Obter Tamanho do APK
```powershell
(Get-Item InventarioMobile\app\build\outputs\apk\debug\app-debug.apk).Length / 1MB
```

---

## ✅ Checklist Pós-Build

- [ ] Build executada com sucesso
- [ ] Versão verificada no build.gradle
- [ ] Registro adicionado ao BUILD_HISTORY.md
- [ ] Usuário informado sobre a build

---

**Esta regra é OBRIGATÓRIA para todas as compilações do APK Android.**
