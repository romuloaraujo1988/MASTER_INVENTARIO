# Guia Rápido - Nova Versão com IP Dinâmico

## 🎉 Novidades

### ✨ IP Configurável
- Não é mais fixo no código
- Pode ser alterado sem recompilar
- Salvo automaticamente

### ✅ Validação Automática
- Testa se o servidor está acessível
- Verifica se o endpoint está correto
- Mensagens de erro claras

### 📝 Logs Detalhados
- Veja exatamente o que está acontecendo
- Facilita debug de problemas
- Logs no Logcat do Android

### 💡 Ajuda Contextual
- Mensagens de ajuda quando algo dá errado
- IPs sugeridos automaticamente
- Instruções passo a passo

## 🚀 Instalação Rápida

### 1. Compilar (se necessário)
```cmd
cd InventarioMobile
.\gradlew.bat assembleDebug
```

### 2. Instalar
```cmd
.\instalar-novo-apk.bat
```

Ou manualmente:
```cmd
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### 3. Limpar Dados (Recomendado)
```cmd
adb shell pm clear com.inventario.mobile
```

## 📱 Como Usar

### Primeira Vez

1. **Abra o app**
   - O app sugerirá automaticamente: `10.14.250.238`

2. **Configure o servidor** (se necessário)
   - Campo "IP do Servidor"
   - Digite o IP correto
   - App valida automaticamente

3. **Faça login**
   - Usuário: `admin`
   - Senha: `admin`
   - Clique em "Login"

4. **Validação automática**
   - App testa conectividade
   - Verifica endpoint correto
   - Mostra mensagem de sucesso ou erro

### Se Já Usou Antes

1. **Limpe os dados do app**
   ```cmd
   adb shell pm clear com.inventario.mobile
   ```

2. **Abra o app novamente**
   - Configuração resetada
   - IP padrão carregado

3. **Faça login**

## 🔧 Configuração do Servidor

### No Servidor (Antes de Testar)

1. **Atualizar senha do admin**
   ```cmd
   atualizar-senha-admin.bat
   ```

2. **Verificar se está rodando**
   ```cmd
   netstat -ano | findstr :8081
   ```

3. **Ver IP do servidor**
   ```cmd
   ipconfig
   ```
   Procure por "Endereço IPv4"

### No App

1. **Tela de login**
2. **Campo "IP do Servidor"**
3. **Digite o IP** (ex: `10.14.250.238`)
4. **App salva automaticamente**

## 🐛 Troubleshooting

### Problema: "IP do servidor não está configurado"

**Solução**:
- Digite o IP no campo "IP do Servidor"
- Use o IP sugerido: `10.14.250.238`

### Problema: "Não foi possível conectar ao servidor"

**Causas possíveis**:
- Servidor não está rodando
- IP incorreto
- Smartphone em rede diferente
- Firewall bloqueando

**Soluções**:
1. Verifique se o servidor está rodando
2. Confirme o IP com `ipconfig` no servidor
3. Conecte smartphone na mesma rede Wi-Fi
4. Teste no navegador: `http://IP:8081/inventario/actuator/health`

### Problema: "API mobile não está disponível"

**Causa**: Servidor rodando mas API não está ativa

**Solução**:
- Reinicie o servidor mobile
- Verifique logs do servidor
- Confirme que está usando perfil `mobile`

### Problema: "Credenciais inválidas"

**Causa**: Senha não está em BCrypt

**Solução**:
```cmd
atualizar-senha-admin.bat
```

## 📊 Ver Logs

### Logs do App (Logcat)
```cmd
adb logcat | findstr "ServerValidator\|LoginViewModel\|NetworkModule"
```

### Logs Completos
```cmd
adb logcat | findstr "inventario"
```

### Limpar e Ver Novos
```cmd
adb logcat -c
adb logcat
```

## 🎯 Fluxo Esperado

### Logs do App
```
D/ServerValidator: INICIANDO VALIDAÇÃO DO SERVIDOR
D/ServerValidator: IP configurado: 10.14.250.238
D/ServerValidator: IP válido: 10.14.250.238
D/ServerValidator: Servidor acessível (150ms)
D/ServerValidator: Endpoint correto acessível
D/ServerValidator: VALIDAÇÃO CONCLUÍDA COM SUCESSO
D/LoginViewModel: TENTANDO LOGIN
D/LoginViewModel: Base URL: http://10.14.250.238:8081/inventario
D/LoginViewModel: Login URL: .../api/mobile/auth/login
D/AuthRepositoryImpl: CHAMANDO API DE LOGIN
D/OkHttp: --> POST .../api/mobile/auth/login
```

### Logs do Servidor
```
╔════════════════════════════════════════════════════════════════
║ REQUISIÇÃO RECEBIDA
║ Método: GET
║ URI: /inventario/actuator/health  (teste de conectividade)
╚════════════════════════════════════════════════════════════════

╔════════════════════════════════════════════════════════════════
║ REQUISIÇÃO RECEBIDA
║ Método: POST
║ URI: /inventario/api/mobile/auth/login  (login real)
╚════════════════════════════════════════════════════════════════
INFO - Tentativa de login mobile para usuário: admin
INFO - Login mobile realizado com sucesso para usuário: admin
```

## 📝 Checklist

Antes de testar:
- [ ] Servidor rodando na porta 8081
- [ ] Senha do admin atualizada (BCrypt)
- [ ] Smartphone na mesma rede Wi-Fi
- [ ] Dados do app limpos
- [ ] Novo APK instalado

Durante o teste:
- [ ] IP configurado no app
- [ ] Validação passou
- [ ] Login funcionou
- [ ] Logs verificados

## 🔄 Alterar IP Padrão (Desenvolvedores)

### Opção 1: Editar XML
```xml
<!-- app/src/main/res/values/server_config.xml -->
<string name="default_server_ip">SEU_IP_AQUI</string>
```

### Opção 2: Código (Fallback)
```kotlin
// ServerConfigManager.kt
private const val FALLBACK_IP = "SEU_IP_AQUI"
```

Depois recompile:
```cmd
.\gradlew.bat assembleDebug
```

## 📚 Documentação Completa

- `CONFIGURACAO_IP_DINAMICA.md` - Sistema de IP dinâmico
- `VERIFICACAO_ENDPOINT_CORRETO.md` - Verificação de endpoints
- `SOLUCAO_FINAL_URL.md` - Solução de problemas de URL

## 🎓 Resumo

**Antes**: IP fixo, difícil de mudar, sem validação
**Agora**: IP configurável, validação automática, mensagens de ajuda

**Resultado**: Sistema mais robusto, profissional e fácil de usar!
