# Guia de Testes - Checkpoint Final

## 📋 Objetivo

Verificar que a implementação de renovação automática de token via biometria está funcionando corretamente e que não há regressões no comportamento existente.

---

## ✅ Pré-requisitos

Antes de iniciar os testes:

1. **Compilar o APK:**
   ```bash
   cd InventarioMobile
   .\gradlew.bat assembleDebug
   ```

2. **Instalar no dispositivo:**
   ```bash
   adb install -r app\build\outputs\apk\debug\app-debug.apk
   ```

3. **Configurar Application no RefreshTokenInterceptor:**
   - Verificar se o módulo Hilt está configurando a propriedade `application`
   - Sem isso, renovação via biometria no interceptor não funcionará

4. **Integrar RenovarTokenComBiometriaUseCase:**
   - Substituir TODOs nos métodos de renovação
   - Sem isso, renovação será simulada

---

## 🧪 Testes de Bug Condition (Devem PASSAR)

### Teste 1: LoginActivity com Token Expirado

**Objetivo:** Verificar que prompt de biometria é mostrado automaticamente

**Passos:**
1. Fazer login com biometria habilitada
2. Aguardar token expirar (ou simular expiração)
3. Fechar app completamente
4. Abrir app novamente

**Resultado Esperado:**
- ✅ Snackbar com mensagem "Token expirado. Use biometria para renovar"
- ✅ Prompt de biometria aparece automaticamente após 500ms
- ✅ Após validar biometria, usuário acessa o app normalmente

**Logs Esperados:**
```
LoginActivity: VERIFICANDO SE PRECISA MOSTRAR BIOMETRIA AUTOMATICAMENTE
LoginActivity: Token expirado (intent): true
LoginActivity: Biometria habilitada: true
LoginActivity: Usuário salvo localmente: true
LoginActivity: Deve mostrar biometria automaticamente: true
LoginActivity: ✓ Mostrando prompt de biometria: Token expirado. Use biometria para renovar
```

---

### Teste 2: MainActivity com Token Expirado

**Objetivo:** Verificar que MainActivity detecta token expirado e solicita biometria

**Passos:**
1. Fazer login normalmente
2. Simular expiração de token (modificar timestamp no PreferencesManager)
3. Navegar para MainActivity

**Resultado Esperado:**
- ✅ MainActivity detecta token expirado
- ✅ Prompt de biometria com título "Renovar Sessão"
- ✅ Após validar, token é renovado

**Logs Esperados:**
```
MainActivity: VERIFICANDO TOKEN AO INICIAR MAINACTIVITY
MainActivity: Token expirado: true
MainActivity: Online: true
MainActivity: Biometria habilitada: true
MainActivity: ✓ Online + Token expirado + Biometria → Renovar token
MainActivity: Iniciando renovação de token via biometria
MainActivity: ✅ Biometria validada, renovando token...
```

---

### Teste 3: RefreshTokenInterceptor detecta 401

**Objetivo:** Verificar que interceptor tenta renovação via biometria antes de redirecionar

**Passos:**
1. Fazer login normalmente
2. Fazer uma requisição que retorna 401 (simular no servidor ou modificar token)
3. Observar comportamento

**Resultado Esperado:**
- ✅ Interceptor detecta 401
- ✅ Verifica que biometria está habilitada
- ✅ Mostra prompt de biometria
- ✅ Após validar, retry da requisição com novo token

**Logs Esperados:**
```
RefreshTokenInterceptor: Recebido 401 Unauthorized, verificando biometria...
RefreshTokenInterceptor: Biometria habilitada: true
RefreshTokenInterceptor: Tentando renovação via biometria...
RefreshTokenInterceptor: TENTANDO RENOVAÇÃO VIA BIOMETRIA
RefreshTokenInterceptor: Activity atual: MainActivity
RefreshTokenInterceptor: ✅ Biometria validada, chamando Use Case de renovação...
RefreshTokenInterceptor: ✓ Token renovado via biometria com sucesso
RefreshTokenInterceptor: Token renovado via biometria, retrying requisição original...
```

---

### Teste 4: App Offline + Biometria (COM dados locais)

**Objetivo:** Verificar acesso offline permanente via biometria

**Passos:**
1. Fazer login com biometria habilitada (primeira autenticação)
2. Desabilitar Wi-Fi e dados móveis
3. Fechar app completamente
4. Abrir app novamente

**Resultado Esperado:**
- ✅ Snackbar com mensagem "Sem conexão. Use biometria para acessar dados offline"
- ✅ Prompt de biometria com título "Acesso Offline"
- ✅ Após validar, Snackbar "📴 Modo Offline Ativado"
- ✅ Usuário acessa dados locais normalmente

**Logs Esperados:**
```
MainActivity: VERIFICANDO TOKEN AO INICIAR MAINACTIVITY
MainActivity: Online: false
MainActivity: Biometria habilitada: true
MainActivity: ✓ Offline + Biometria → Permitir acesso offline
MainActivity: Permitindo acesso offline via biometria
MainActivity: ✓ Dados locais encontrados, permitindo acesso offline via biometria
MainActivity: ✅ Biometria validada, permitindo acesso offline
MainActivity: ✓ Acesso offline permitido com sucesso
```

---

### Teste 5: App Offline SEM Dados Locais (Primeira Autenticação)

**Objetivo:** Verificar que acesso offline é bloqueado sem primeira autenticação

**Passos:**
1. Limpar dados do app (ou instalar em dispositivo novo)
2. Desabilitar Wi-Fi e dados móveis
3. Tentar abrir app

**Resultado Esperado:**
- ✅ Mensagem de erro: "Conecte-se à internet para fazer o primeiro login"
- ✅ Campos de login desabilitados
- ✅ Não mostra prompt de biometria

**Logs Esperados:**
```
LoginActivity: VERIFICANDO SE PRECISA MOSTRAR BIOMETRIA AUTOMATICAMENTE
LoginActivity: Offline (real): true
LoginActivity: Biometria habilitada: false
LoginActivity: Usuário salvo localmente: false
LoginActivity: ❌ Offline sem dados locais. Primeira autenticação necessária.
```

---

## 🔒 Testes de Preservation (Devem PASSAR)

### Teste 6: Usuário SEM Biometria + Token Expirado

**Objetivo:** Verificar que comportamento sem biometria não mudou

**Passos:**
1. Fazer login SEM habilitar biometria
2. Aguardar token expirar
3. Tentar usar app

**Resultado Esperado:**
- ✅ Redireciona para LoginActivity normalmente
- ✅ NÃO mostra prompt de biometria
- ✅ Usuário precisa digitar senha

**Logs Esperados:**
```
MainActivity: Token expirado: true
MainActivity: Biometria habilitada: false
MainActivity: ✓ Token expirado + Sem biometria → Redirecionar para login
MainActivity: Redirecionando para login: Sessão expirada. Faça login novamente.
```

---

### Teste 7: Login Manual com Senha

**Objetivo:** Verificar que login com senha continua funcionando

**Passos:**
1. Abrir app
2. Digitar login e senha
3. Clicar em "Entrar"

**Resultado Esperado:**
- ✅ Login funciona normalmente
- ✅ Usuário acessa o app
- ✅ Nenhuma mudança no comportamento

---

### Teste 8: Token Válido

**Objetivo:** Verificar que token válido não aciona prompts

**Passos:**
1. Fazer login normalmente
2. Usar app com token válido

**Resultado Esperado:**
- ✅ Nenhum prompt de biometria mostrado
- ✅ App funciona normalmente
- ✅ Nenhuma interrupção

**Logs Esperados:**
```
MainActivity: Token expirado: false
MainActivity: ✓ Token válido, nenhuma ação necessária
```

---

### Teste 9: Botão Manual de Biometria

**Objetivo:** Verificar que botão manual continua funcionando

**Passos:**
1. Abrir LoginActivity
2. Clicar no FAB de biometria (se disponível)

**Resultado Esperado:**
- ✅ Prompt de biometria aparece
- ✅ Após validar, login é feito
- ✅ Funcionalidade preservada

---

### Teste 10: Cancelamento de Biometria

**Objetivo:** Verificar fallback para senha

**Passos:**
1. Abrir app com token expirado + biometria habilitada
2. Quando prompt de biometria aparecer, cancelar

**Resultado Esperado:**
- ✅ Redireciona para LoginActivity
- ✅ Campos de login habilitados
- ✅ Mensagem: "Autenticação cancelada. Use senha para fazer login."
- ✅ Usuário pode digitar senha

**Logs Esperados:**
```
LoginActivity: Biometria cancelada pelo usuário
LoginActivity: ❌ Biometria falhou: (mensagem)
```

---

## 🔍 Verificação de Logs

### Logs Críticos a Verificar

1. **LoginActivity:**
   - Verificação automática sendo chamada
   - Detecção correta de token expirado/offline
   - Prompt de biometria mostrado quando necessário

2. **MainActivity:**
   - Verificação de token no onCreate
   - Decisão correta baseado em estado (online/offline/biometria)
   - Acesso offline permitido apenas com dados locais

3. **RefreshTokenInterceptor:**
   - Detecção de 401
   - Verificação de biometria habilitada
   - Tentativa de renovação via biometria
   - Retry de requisição após sucesso

---

## ⚠️ Problemas Conhecidos e Soluções

### Problema 1: getCurrentActivity() retorna null

**Sintoma:** Renovação via biometria no interceptor não funciona

**Causa:** Application não configurada no RefreshTokenInterceptor

**Solução:**
```kotlin
// No módulo Hilt
@Provides
@Singleton
fun provideRefreshTokenInterceptor(
    preferencesManager: PreferencesManager,
    @ApplicationContext context: Context
): RefreshTokenInterceptor {
    val interceptor = RefreshTokenInterceptor(preferencesManager)
    interceptor.application = context.applicationContext as Application
    return interceptor
}
```

---

### Problema 2: Renovação via biometria não renova token

**Sintoma:** Biometria valida mas token não é renovado

**Causa:** TODO não substituído por chamada ao Use Case

**Solução:**
```kotlin
// Substituir TODO por:
val renovarTokenUseCase = RenovarTokenComBiometriaUseCase(...)
val result = renovarTokenUseCase()
```

---

### Problema 3: Acesso offline bloqueado mesmo com biometria

**Sintoma:** Usuário não consegue acessar offline

**Causa:** Dados locais não foram salvos na primeira autenticação

**Solução:**
- Verificar que primeira autenticação foi feita online
- Verificar que `hasUserSavedLocally()` retorna true
- Verificar que dados foram salvos no Room

---

## ✅ Checklist Final

Antes de marcar como completo:

- [ ] Todos os testes de bug condition passam
- [ ] Todos os testes de preservation passam
- [ ] Logs confirmam comportamento correto
- [ ] Nenhuma regressão detectada
- [ ] Application configurada no interceptor
- [ ] Use Case integrado
- [ ] Testes manuais realizados
- [ ] Documentação atualizada

---

## 📝 Relatório de Testes

Após executar todos os testes, preencher:

### Testes de Bug Condition
- [ ] Teste 1: LoginActivity com Token Expirado - ✅ PASSOU / ❌ FALHOU
- [ ] Teste 2: MainActivity com Token Expirado - ✅ PASSOU / ❌ FALHOU
- [ ] Teste 3: RefreshTokenInterceptor 401 - ✅ PASSOU / ❌ FALHOU
- [ ] Teste 4: App Offline COM dados - ✅ PASSOU / ❌ FALHOU
- [ ] Teste 5: App Offline SEM dados - ✅ PASSOU / ❌ FALHOU

### Testes de Preservation
- [ ] Teste 6: Sem Biometria + Token Expirado - ✅ PASSOU / ❌ FALHOU
- [ ] Teste 7: Login Manual com Senha - ✅ PASSOU / ❌ FALHOU
- [ ] Teste 8: Token Válido - ✅ PASSOU / ❌ FALHOU
- [ ] Teste 9: Botão Manual de Biometria - ✅ PASSOU / ❌ FALHOU
- [ ] Teste 10: Cancelamento de Biometria - ✅ PASSOU / ❌ FALHOU

### Problemas Encontrados
(Descrever qualquer problema encontrado durante os testes)

### Observações
(Adicionar observações relevantes)

---

**Data dos Testes:** ___/___/______  
**Testado por:** _______________  
**Dispositivo:** _______________  
**Versão Android:** _______________  
**Status Final:** ✅ APROVADO / ❌ REPROVADO

