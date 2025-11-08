# 🧪 Guia Rápido de Teste - Autenticação Biométrica

## 🎯 Objetivo
Testar a autenticação biométrica implementada no aplicativo.

---

## 📋 Pré-requisitos

### 1. Dispositivo Android
- ✅ Android 6.0+ (API 23+)
- ✅ Sensor biométrico (impressão digital ou facial)
- ✅ Biometria configurada no dispositivo

### 2. Configurar Biometria no Dispositivo

#### Samsung/Xiaomi/Motorola
```
Configurações → Segurança → Biometria
→ Adicionar Impressão Digital
```

#### Google Pixel
```
Configurações → Segurança → Desbloqueio de Tela
→ Impressão Digital
```

#### Emulador (Limitado)
```
Settings → Security → Fingerprint
→ Add Fingerprint
```
⚠️ **Nota**: Emulador tem suporte limitado. Recomendado testar em dispositivo real.

---

## 🚀 Passo a Passo do Teste

### Teste 1: Primeiro Login (Configuração)

#### 1.1 Abrir o App
```
1. Instalar APK no dispositivo
2. Abrir aplicativo
3. Tela de login deve aparecer
```

#### 1.2 Fazer Login Tradicional
```
1. Digitar IP do servidor
2. Digitar usuário
3. Digitar senha
4. Clicar em "ENTRAR"
```

#### 1.3 Configurar Biometria
```
1. Dialog aparece: "Deseja usar biometria no próximo login?"
2. Clicar em "Sim"
3. Prompt de biometria aparece
4. Autenticar com impressão digital/facial
5. Toast: "Biometria habilitada com sucesso!"
```

**✅ Resultado Esperado**: 
- Dialog de configuração aparece
- Prompt biométrico funciona
- Toast de sucesso é exibido
- App navega para tela principal

---

### Teste 2: Login com Biometria

#### 2.1 Fechar e Reabrir o App
```
1. Fechar app completamente (não apenas minimizar)
2. Reabrir app
3. Tela de login aparece
```

#### 2.2 Verificar Botão de Biometria
```
1. Verificar se botão "Entrar com Biometria" está visível
2. Verificar texto: "Impressão Digital / Facial"
3. Verificar ícone de biometria
```

#### 2.3 Login Biométrico
```
1. Clicar no botão "Entrar com Biometria"
2. Prompt nativo do Android aparece
3. Autenticar com biometria
4. Login automático
5. Navega para tela principal
```

**✅ Resultado Esperado**:
- Botão de biometria visível
- Prompt aparece ao clicar
- Login automático após autenticação
- Navegação para tela principal

---

### Teste 3: Falha de Biometria

#### 3.1 Tentar Biometria Errada
```
1. Clicar em "Entrar com Biometria"
2. Usar dedo/rosto não cadastrado (3x)
3. Verificar mensagem de erro
```

#### 3.2 Usar Fallback
```
1. Após 3 tentativas, clicar em "Usar Senha"
2. Digitar senha do dispositivo
3. Verificar login bem-sucedido
```

**✅ Resultado Esperado**:
- Mensagem de erro após tentativas falhadas
- Opção "Usar Senha" disponível
- Login com senha do dispositivo funciona

---

### Teste 4: Cancelamento

#### 4.1 Cancelar Autenticação
```
1. Clicar em "Entrar com Biometria"
2. Clicar em "Cancelar" no prompt
3. Verificar que volta para tela de login
4. Campos de usuário/senha ainda disponíveis
```

**✅ Resultado Esperado**:
- Cancelamento não causa erro
- Volta para tela de login normalmente
- Pode fazer login com senha

---

### Teste 5: Dispositivo sem Biometria

#### 5.1 Remover Biometria do Dispositivo
```
1. Ir em Configurações do dispositivo
2. Remover todas as biometrias cadastradas
3. Voltar ao app
4. Verificar que botão de biometria não aparece
```

**✅ Resultado Esperado**:
- Botão de biometria oculto
- Apenas login tradicional disponível
- Sem erros ou crashes

---

## 📊 Checklist de Testes

### Funcionalidades Básicas
- [ ] App abre sem erros
- [ ] Tela de login carrega corretamente
- [ ] Login tradicional funciona
- [ ] Dialog de configuração aparece após login

### Biometria
- [ ] Botão de biometria aparece quando disponível
- [ ] Botão oculto quando biometria não disponível
- [ ] Prompt biométrico abre corretamente
- [ ] Autenticação com impressão digital funciona
- [ ] Autenticação com reconhecimento facial funciona (se disponível)
- [ ] Login automático após autenticação

### Erros e Fallbacks
- [ ] Falha de biometria mostra mensagem adequada
- [ ] Opção "Usar Senha" funciona
- [ ] Cancelamento não causa erro
- [ ] Lockout temporário funciona (após muitas tentativas)

### Segurança
- [ ] Credenciais não aparecem em logs
- [ ] Dados criptografados (verificar com Android Studio Profiler)
- [ ] Logout limpa dados biométricos

---

## 🐛 Problemas Comuns e Soluções

### Problema 1: Botão de Biometria Não Aparece

**Possíveis Causas**:
- Biometria não configurada no dispositivo
- minSdk < 23
- Permissões não concedidas

**Solução**:
```
1. Verificar se biometria está configurada no dispositivo
2. Verificar logs: adb logcat | findstr "Biometric"
3. Verificar permissões no AndroidManifest.xml
```

### Problema 2: Erro ao Autenticar

**Possíveis Causas**:
- Sensor biométrico com problema
- App não tem permissão USE_BIOMETRIC

**Solução**:
```
1. Testar biometria em outras apps
2. Verificar permissões: adb shell dumpsys package com.inventario.mobile
3. Reinstalar app
```

### Problema 3: Crash ao Clicar em Biometria

**Possíveis Causas**:
- Dependências não sincronizadas
- FragmentActivity não encontrada

**Solução**:
```
1. Sync Gradle
2. Clean + Rebuild
3. Verificar logs de erro
```

---

## 📱 Comandos Úteis para Debug

### Ver Logs de Biometria
```bash
adb logcat | findstr "Biometric"
```

### Ver Logs do App
```bash
adb logcat | findstr "LoginActivity"
```

### Verificar Permissões
```bash
adb shell dumpsys package com.inventario.mobile | findstr "permission"
```

### Limpar Dados do App
```bash
adb shell pm clear com.inventario.mobile
```

### Reinstalar App
```bash
adb uninstall com.inventario.mobile
adb install app-debug.apk
```

---

## ✅ Critérios de Sucesso

O teste é considerado bem-sucedido se:

1. ✅ Login tradicional funciona
2. ✅ Dialog de configuração aparece
3. ✅ Biometria pode ser configurada
4. ✅ Botão de biometria aparece após configuração
5. ✅ Login biométrico funciona
6. ✅ Fallback para senha funciona
7. ✅ Sem crashes ou erros críticos

---

## 📝 Relatório de Teste

Após testar, preencha:

```
Data: ___/___/______
Dispositivo: _________________
Android Version: _____________
Tipo de Biometria: ___________

Testes Realizados:
[ ] Teste 1: Primeiro Login
[ ] Teste 2: Login com Biometria
[ ] Teste 3: Falha de Biometria
[ ] Teste 4: Cancelamento
[ ] Teste 5: Sem Biometria

Resultado Geral: [ ] PASSOU  [ ] FALHOU

Observações:
_________________________________
_________________________________
_________________________________
```

---

## 🎉 Próximos Passos

Após testes bem-sucedidos:

1. ✅ Testar em diferentes dispositivos
2. ✅ Testar com diferentes tipos de biometria
3. ✅ Testar cenários de erro
4. ✅ Documentar bugs encontrados
5. ✅ Preparar para produção

---

**Status**: 🟢 PRONTO PARA TESTAR
