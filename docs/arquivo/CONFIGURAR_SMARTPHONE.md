# 📱 Configurar Aplicativo Mobile no Smartphone

## ✅ Pré-requisitos

1. **Smartphone e PC na MESMA rede Wi-Fi**
2. **Servidor mobile rodando** na aplicação desktop
3. **Firewall configurado** para permitir porta 8081

---

## 🔧 Configuração Passo a Passo

### 1️⃣ Verificar IP do Computador

Seu IP atual: **10.14.1.45**

Para verificar novamente:
```cmd
ipconfig
```
Procure por "Endereço IPv4" na sua conexão Wi-Fi.

---

### 2️⃣ Configurar Firewall do Windows

**Opção A - Comando Rápido (PowerShell como Administrador):**
```powershell
netsh advfirewall firewall add rule name="Servidor Mobile Inventário" dir=in action=allow protocol=TCP localport=8081
```

**Opção B - Interface Gráfica:**
1. Abra "Firewall do Windows Defender"
2. Clique em "Configurações avançadas"
3. Clique em "Regras de Entrada" → "Nova Regra"
4. Selecione "Porta" → Avançar
5. Selecione "TCP" e digite "8081" → Avançar
6. Selecione "Permitir a conexão" → Avançar
7. Marque todos os perfis → Avançar
8. Nome: "Servidor Mobile Inventário" → Concluir

---

### 3️⃣ Iniciar Servidor Mobile

Na aplicação desktop:
1. Faça login como administrador
2. Menu **Sistema** → **Iniciar Servidor Mobile**
3. Aguarde a mensagem de sucesso

---

### 4️⃣ Configurar Aplicativo no Smartphone

1. **Instale o APK** no smartphone:
   - Arquivo: `InventarioMobile-v1.1-debug.apk`
   - Transfira via USB, email ou compartilhamento

2. **Abra o aplicativo**

3. **Configure o IP do servidor:**
   - Digite: `10.14.1.45`
   - **NÃO** digite `http://` ou porta
   - O app construirá automaticamente: `http://10.14.1.45:8081/inventario`

4. **Faça login:**
   - Usuário: `admin`
   - Senha: `admin123`

---

## 🧪 Testar Conexão

### Teste 1: Ping
No smartphone, abra o navegador e acesse:
```
http://10.14.1.45:8081/inventario/actuator/health
```

**Resposta esperada:**
```json
{"status":"UP"}
```

### Teste 2: Endpoint de Login
```
http://10.14.1.45:8081/inventario/api/mobile/test/ping
```

---

## ❌ Problemas Comuns

### Problema: "Timeout na conexão"
**Soluções:**
1. Verifique se smartphone e PC estão na **mesma rede Wi-Fi**
2. Verifique se o **servidor está rodando** (porta 8081)
3. Verifique o **firewall** do Windows
4. Desative temporariamente o **antivírus** para testar

### Problema: "Servidor não encontrado"
**Soluções:**
1. Confirme o **IP correto** do computador
2. Verifique se o IP não mudou (DHCP)
3. Tente usar o IP completo no navegador do smartphone primeiro

### Problema: "Conexão recusada"
**Soluções:**
1. Verifique se o **servidor mobile está rodando**
2. Verifique se a porta 8081 está **livre**
3. Reinicie o servidor mobile

### Problema: "Entra no health mas não faz login"
**Soluções:**
1. Verifique os **logs do servidor** na aplicação desktop
2. Confirme que está usando a **versão mais recente** do APK
3. Limpe os dados do app no smartphone e tente novamente

---

## 📊 Verificar Logs

### No Servidor (Desktop):
Os logs aparecem no console da aplicação desktop quando o servidor mobile está rodando.

### No Smartphone (via ADB):
```cmd
adb logcat -s "LoginViewModel:*" "AuthRepositoryImpl:*" "MainActivity:*"
```

---

## 🔄 Atualizar APK

Sempre que houver mudanças no código:

1. **Recompilar:**
   ```cmd
   cd InventarioMobile
   gradlew assembleDebug
   ```

2. **Instalar no smartphone:**
   ```cmd
   adb install -r app\build\outputs\apk\debug\app-debug.apk
   ```

---

## 📝 Credenciais Padrão

- **Usuário:** admin
- **Senha:** admin123
- **Perfil:** Administrador

---

## 🆘 Suporte

Se o problema persistir:
1. Verifique os logs do servidor
2. Verifique os logs do aplicativo (via ADB)
3. Teste a conexão no navegador do smartphone primeiro
4. Confirme que ambos estão na mesma rede

---

**Última atualização:** 24/10/2025
**Versão do APK:** 1.1
**IP do Servidor:** 10.14.1.45
