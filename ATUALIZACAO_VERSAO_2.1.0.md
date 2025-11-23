# 🔄 Atualização de Versão - 2.1.0

## 📊 Mudanças de Versão

### Versão Anterior
- **versionCode:** 3
- **versionName:** "1.2"
- **Exibição:** "Versão 1.0.0"

### Nova Versão
- **versionCode:** 4
- **versionName:** "2.1.0"
- **Exibição:** "Versão 2.1.0"

---

## 📝 Arquivos Atualizados

### 1. build.gradle ✅
**Arquivo:** `InventarioMobile/app/build.gradle`

```gradle
defaultConfig {
    versionCode 4        // Incrementado de 3 para 4
    versionName "2.1.0"  // Atualizado de "1.2" para "2.1.0"
}
```

### 2. strings.xml ✅
**Arquivo:** `InventarioMobile/app/src/main/res/values/strings.xml`

```xml
<string name="app_version">Versão 2.1.0</string>
```

---

## 🎯 Onde a Versão Aparece

### 1. Splash Screen
- Exibe "Versão 2.1.0" abaixo do nome do app
- Visível durante 5 segundos ao abrir o app

### 2. Tela de Login
- Exibe versão no rodapé (se configurado)

### 3. Sobre/Configurações
- Informações do app mostram versão

---

## 🆕 Novidades da Versão 2.1.0

### Funcionalidades Implementadas

1. **Logout Automático** ✅
   - Detecta token expirado automaticamente
   - Redireciona para login com mensagem clara
   - Limpa dados de sessão corretamente

2. **SessionManager** ✅
   - Gerenciamento centralizado de sessão
   - Verificação automática de validade

3. **BaseActivity** ✅
   - Verificação de sessão em todas as telas
   - Proteção automática contra token inválido

4. **Correção Dashboard** ✅
   - Corrigida duplicação de coletas
   - Agora conta apenas coletas não sincronizadas

---

## 📋 Changelog Completo

### v2.1.0 (23/11/2025)

**Adicionado:**
- SessionManager para gerenciamento de sessão
- TokenExpiredListener para notificação de token expirado
- BaseActivity com verificação automática de sessão
- Logout automático quando token expira
- Mensagem clara de sessão expirada

**Corrigido:**
- Duplicação de coletas no dashboard
- Contagem incorreta de coletas sincronizadas
- Usuário ficava "preso" com token inválido

**Melhorado:**
- Experiência do usuário ao expirar sessão
- Tratamento de erros de autenticação
- Limpeza de dados ao fazer logout

---

## 🔢 Histórico de Versões

| Versão | versionCode | Data | Principais Mudanças |
|--------|-------------|------|---------------------|
| 2.1.0 | 4 | 23/11/2025 | Logout automático + Correções |
| 1.2 | 3 | - | Versão anterior |
| 1.0.0 | 1-2 | - | Versão inicial |

---

## 🚀 Como Compilar Nova Versão

```bash
cd InventarioMobile
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

---

## ✅ Checklist de Atualização

- [x] Atualizar versionCode no build.gradle
- [x] Atualizar versionName no build.gradle
- [x] Atualizar app_version no strings.xml
- [ ] Compilar APK
- [ ] Testar splash screen
- [ ] Verificar versão exibida
- [ ] Gerar APK release (quando pronto)

---

## 📱 Verificação Visual

Após instalar, verificar:

1. **Splash Screen:**
   - Deve mostrar "Versão 2.1.0"
   
2. **Sobre o App:**
   - Versão deve ser 2.1.0
   
3. **Build Info:**
   - versionCode: 4
   - versionName: 2.1.0

---

**Status:** ✅ Versão atualizada  
**Próximo passo:** Compilar e testar
