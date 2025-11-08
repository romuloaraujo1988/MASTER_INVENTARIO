# 🔧 Solução para "Erro Desconhecido" no App Mobile

## 🎯 Problema Identificado

O **"erro desconhecido"** no aplicativo mobile geralmente ocorre por problemas de **conectividade de rede** entre o smartphone e o servidor, não necessariamente por IPs hardcoded.

## 🔍 Principais Causas

### 1. **Firewall do Windows** (Mais Comum)
- Windows bloqueia conexões na porta 8081 por padrão
- Smartphone não consegue conectar ao servidor
- App retorna "erro desconhecido"

### 2. **Servidor Mobile Não Iniciado**
- Servidor desktop rodando, mas API mobile não
- Porta 8081 não está sendo usada
- App tenta conectar mas não há resposta

### 3. **Redes Diferentes**
- PC conectado em uma rede
- Smartphone conectado em outra rede
- Não há comunicação entre as redes

### 4. **Antivírus/Firewall de Terceiros**
- Antivírus bloqueia conexões de entrada
- Firewall corporativo bloqueia porta 8081
- App não consegue estabelecer conexão

## 🛠️ Scripts de Solução Criados

### 1. **`liberar-firewall-8081.bat`** (EXECUTE PRIMEIRO)
```batch
# Libera porta 8081 no firewall do Windows
# DEVE ser executado como Administrador
netsh advfirewall firewall add rule name="SIHCP Mobile Server" dir=in action=allow protocol=TCP localport=8081
```

**Como usar:**
1. Clique com botão direito no arquivo
2. Selecione "Executar como administrador"
3. Confirme a liberação da porta

### 2. **`diagnostico-conectividade-mobile.bat`** (DIAGNÓSTICO COMPLETO)
```batch
# Verifica todos os aspectos da conectividade
# - IP do computador
# - Porta 8081 em uso
# - Regras de firewall
# - Conectividade local e externa
# - Configuração do servidor
```

**Como usar:**
1. Execute o arquivo (duplo clique)
2. Veja o relatório completo
3. Siga as soluções sugeridas

### 3. **`testar-conectividade-smartphone.bat`** (TESTE ESPECÍFICO)
```batch
# Testa especificamente a conectividade para smartphone
# - Mostra IP para configurar no app
# - Testa endpoints da API mobile
# - Verifica CORS
# - Dá instruções específicas
```

**Como usar:**
1. Execute após liberar o firewall
2. Anote o IP mostrado
3. Configure no app mobile

## 📱 Passo a Passo para Resolver

### **PASSO 1: Liberar Firewall**
```batch
# Execute como Administrador
liberar-firewall-8081.bat
```

### **PASSO 2: Iniciar Servidor Mobile**
```batch
# Execute o servidor mobile (não o desktop)
iniciar-servidor-mobile.bat
```

### **PASSO 3: Diagnosticar Conectividade**
```batch
# Verifique se tudo está funcionando
diagnostico-conectividade-mobile.bat
```

### **PASSO 4: Testar Conectividade Smartphone**
```batch
# Teste específico para mobile
testar-conectividade-smartphone.bat
```

### **PASSO 5: Configurar App**
1. Anote o IP mostrado nos scripts
2. Abra o app SIHCP Mobile
3. Configure o IP do servidor
4. Tente fazer login

## 🔧 Configurações Importantes

### **Servidor Mobile (application-mobile.properties)**
```properties
# Porta obrigatória
server.port=8081

# Permitir acesso externo
server.address=0.0.0.0

# CORS liberado para mobile
spring.web.cors.allowed-origins=*
spring.web.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
spring.web.cors.allowed-headers=*
```

### **Network Security Config (Android)**
```xml
<!-- HTTP permitido para desenvolvimento -->
<base-config cleartextTrafficPermitted="true">
    <trust-anchors>
        <certificates src="system" />
        <certificates src="user" />
    </trust-anchors>
</base-config>
```

## 🚨 Problemas Específicos e Soluções

### **"Erro Desconhecido" - Servidor não responde**
**Causa:** Firewall bloqueando porta 8081
**Solução:**
```batch
# Execute como Admin
liberar-firewall-8081.bat
```

### **"Erro Desconhecido" - Timeout de conexão**
**Causa:** Redes diferentes ou antivírus
**Solução:**
1. Verifique se smartphone e PC estão na mesma WiFi
2. Desative antivírus temporariamente
3. Teste ping do smartphone para o PC

### **"Servidor não encontrado"**
**Causa:** IP incorreto ou servidor não rodando
**Solução:**
```batch
# Veja o IP correto
testar-conectividade-smartphone.bat

# Inicie o servidor
iniciar-servidor-mobile.bat
```

### **"Credenciais inválidas" (mas era "erro desconhecido")**
**Causa:** Servidor funcionando, problema de autenticação
**Solução:**
- Use credenciais corretas: admin/admin123
- Verifique se banco de dados está configurado

## 📋 Checklist de Verificação

### ✅ **Antes de Testar o App:**
- [ ] Firewall liberado (porta 8081)
- [ ] Servidor mobile iniciado
- [ ] Porta 8081 em uso (netstat -an | findstr :8081)
- [ ] Teste local funcionando (curl localhost:8081/inventario/api/mobile/health)
- [ ] Teste externo funcionando (curl IP:8081/inventario/api/mobile/health)

### ✅ **Configuração do App:**
- [ ] IP correto do servidor
- [ ] Porta 8081
- [ ] Smartphone na mesma rede WiFi
- [ ] Credenciais corretas (admin/admin123)

### ✅ **Se Ainda Não Funcionar:**
- [ ] Desativar antivírus temporariamente
- [ ] Verificar firewall corporativo
- [ ] Testar com outro smartphone
- [ ] Verificar logs do servidor

## 🎯 URLs para Teste Manual

Substitua `IP_DO_SERVIDOR` pelo IP real:

- **Health Check:** `http://IP_DO_SERVIDOR:8081/inventario/api/mobile/health`
- **Swagger UI:** `http://IP_DO_SERVIDOR:8081/inventario/swagger-ui.html`
- **Login API:** `http://IP_DO_SERVIDOR:8081/inventario/api/mobile/auth/login`

## 📞 Comandos Úteis

```batch
# Ver IP do computador
ipconfig

# Ver processos na porta 8081
netstat -ano | findstr :8081

# Testar conectividade
curl http://localhost:8081/inventario/api/mobile/health

# Liberar firewall
netsh advfirewall firewall add rule name="SIHCP Mobile Server" dir=in action=allow protocol=TCP localport=8081

# Ver regras do firewall
netsh advfirewall firewall show rule name="SIHCP Mobile Server"
```

## 🎉 Resultado Esperado

Após seguir todos os passos:

1. **Scripts mostram:** ✅ Tudo OK - Smartphone pode conectar
2. **App mobile:** Conecta sem "erro desconhecido"
3. **Login:** Funciona normalmente
4. **Funcionalidades:** Todas operacionais

**O problema de "erro desconhecido" deve estar resolvido!** 🚀