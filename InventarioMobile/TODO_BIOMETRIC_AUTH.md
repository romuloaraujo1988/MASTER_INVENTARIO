# 🔐 Autenticação Biométrica - Plano de Implementação

## 📋 Status: AGENDADO

## 🎯 Objetivo
Implementar autenticação biométrica (impressão digital / reconhecimento facial) como alternativa ao login tradicional com usuário e senha.

## 🔧 Tecnologias

### AndroidX Biometric Library
```gradle
implementation "androidx.biometric:biometric:1.2.0-alpha05"
```

### Tipos de Autenticação Suportados
- ✅ Impressão Digital (Fingerprint)
- ✅ Reconhecimento Facial (Face Recognition)
- ✅ Íris (Iris Scanner)
- ✅ Qualquer biometria configurada no dispositivo

## 📱 Funcionalidades Planejadas

### 1. **Login com Biometria**
- Tela de login com opção "Usar Biometria"
- Fallback para senha se biometria falhar
- Armazenamento seguro de credenciais (EncryptedSharedPreferences)

### 2. **Configuração de Biometria**
- Ativar/desativar biometria nas configurações
- Verificar disponibilidade do dispositivo
- Solicitar configuração se não estiver ativa

### 3. **Segurança Adicional**
- Timeout de sessão com re-autenticação
- Biometria para ações sensíveis (sincronização, exportação)
- Criptografia de dados locais

### 4. **Experiência do Usuário**
- Prompt nativo do Android
- Mensagens claras de erro
- Opção de usar senha como fallback
- Lembrar preferência do usuário

## 🏗️ Arquitetura

### Componentes a Criar

```
com.inventario.mobile/
├── security/
│   ├── BiometricManager.kt          # Gerenciador de biometria
│   ├── BiometricPromptHelper.kt     # Helper para prompt
│   ├── SecureStorage.kt             # Armazenamento seguro
│   └── CryptoManager.kt             # Criptografia
├── presentation/
│   ├── login/
│   │   ├── BiometricLoginFragment.kt
│   │   └── BiometricSetupDialog.kt
│   └── settings/
│       └── BiometricSettingsFragment.kt
└── data/
    └── preferences/
        └── BiometricPreferences.kt
```

## 📝 Fluxo de Implementação

### Fase 1: Infraestrutura Base
- [ ] Adicionar dependência androidx.biometric
- [ ] Criar BiometricManager
- [ ] Criar SecureStorage com EncryptedSharedPreferences
- [ ] Implementar CryptoManager

### Fase 2: Tela de Login
- [ ] Adicionar botão "Usar Biometria" na LoginActivity
- [ ] Implementar BiometricPromptHelper
- [ ] Integrar com sistema de autenticação existente
- [ ] Adicionar animações e feedback visual

### Fase 3: Configurações
- [ ] Adicionar seção "Segurança" nas configurações
- [ ] Toggle para ativar/desativar biometria
- [ ] Verificação de disponibilidade
- [ ] Guia de configuração para usuários

### Fase 4: Segurança Adicional
- [ ] Timeout de sessão
- [ ] Re-autenticação para ações sensíveis
- [ ] Criptografia de dados offline
- [ ] Logs de tentativas de acesso

### Fase 5: Testes e Refinamento
- [ ] Testar em diferentes dispositivos
- [ ] Testar diferentes tipos de biometria
- [ ] Testar cenários de erro
- [ ] Otimizar UX

## 🎨 Design de Interface

### Tela de Login com Biometria
```
┌─────────────────────────────┐
│   SIHCP - Inventário        │
│                             │
│   [Ícone de Impressão       │
│    Digital Animado]         │
│                             │
│   Toque para autenticar     │
│                             │
│   ─────────── ou ───────────│
│                             │
│   [Campo Usuário]           │
│   [Campo Senha]             │
│   [Botão Entrar]            │
│                             │
│   [Esqueci minha senha]     │
└─────────────────────────────┘
```

### Prompt de Biometria (Nativo Android)
```
┌─────────────────────────────┐
│  Autenticação Necessária    │
│                             │
│  [Ícone de Impressão        │
│   Digital]                  │
│                             │
│  Toque no sensor para       │
│  fazer login                │
│                             │
│  [Usar Senha]  [Cancelar]   │
└─────────────────────────────┘
```

## 🔒 Segurança

### Armazenamento de Credenciais
```kotlin
// Usar EncryptedSharedPreferences
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val encryptedPrefs = EncryptedSharedPreferences.create(
    context,
    "biometric_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

### Níveis de Autenticação
1. **BIOMETRIC_STRONG**: Impressão digital, facial (Classe 3)
2. **BIOMETRIC_WEAK**: Facial menos seguro (Classe 2)
3. **DEVICE_CREDENTIAL**: PIN, padrão, senha do dispositivo

## 📊 Casos de Uso

### Caso 1: Primeiro Login
```
1. Usuário faz login com usuário/senha
2. Sistema detecta biometria disponível
3. Pergunta: "Deseja usar biometria no próximo login?"
4. Se sim, salva credenciais criptografadas
5. Próximo login: mostra opção de biometria
```

### Caso 2: Login Recorrente
```
1. Usuário abre app
2. Tela mostra ícone de biometria
3. Usuário toca no ícone
4. Prompt nativo aparece
5. Usuário autentica
6. Login automático
```

### Caso 3: Falha de Biometria
```
1. Usuário tenta biometria
2. Falha (3 tentativas)
3. Sistema oferece fallback para senha
4. Usuário digita senha
5. Login bem-sucedido
```

### Caso 4: Ação Sensível
```
1. Usuário tenta sincronizar dados
2. Sistema solicita re-autenticação
3. Prompt de biometria aparece
4. Usuário autentica
5. Sincronização prossegue
```

## 🎯 Benefícios

### Para o Usuário
- ✅ Login mais rápido (< 2 segundos)
- ✅ Não precisa lembrar senha
- ✅ Mais seguro que senha
- ✅ Experiência moderna

### Para o Sistema
- ✅ Maior segurança
- ✅ Menos tentativas de login falhadas
- ✅ Rastreabilidade de acessos
- ✅ Conformidade com padrões de segurança

## 📱 Compatibilidade

### Requisitos Mínimos
- Android 6.0 (API 23) ou superior
- Dispositivo com sensor biométrico
- Biometria configurada no dispositivo

### Fallback
- Sempre oferecer login com senha
- Detectar disponibilidade antes de mostrar opção
- Mensagens claras se não disponível

## 🚀 Prioridade

**MÉDIA-ALTA** - Funcionalidade importante para UX e segurança, mas não bloqueia uso do app.

## 📅 Estimativa

- **Fase 1**: 2-3 horas
- **Fase 2**: 3-4 horas
- **Fase 3**: 2-3 horas
- **Fase 4**: 2-3 horas
- **Fase 5**: 2-3 horas

**Total**: 11-16 horas de desenvolvimento

## 📚 Referências

- [AndroidX Biometric Library](https://developer.android.com/jetpack/androidx/releases/biometric)
- [Biometric Authentication Guide](https://developer.android.com/training/sign-in/biometric-auth)
- [EncryptedSharedPreferences](https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences)

---

## ✅ Checklist de Implementação

Quando for implementar, seguir esta ordem:

- [ ] 1. Adicionar dependências no build.gradle
- [ ] 2. Criar classes de segurança (BiometricManager, SecureStorage)
- [ ] 3. Modificar LoginActivity para suportar biometria
- [ ] 4. Criar UI para configuração de biometria
- [ ] 5. Implementar armazenamento seguro de credenciais
- [ ] 6. Adicionar re-autenticação para ações sensíveis
- [ ] 7. Testar em múltiplos dispositivos
- [ ] 8. Documentar uso para usuários finais
- [ ] 9. Criar guia de troubleshooting
- [ ] 10. Deploy e monitoramento

---

**Status**: 📋 AGENDADO - Pronto para implementação quando solicitado
