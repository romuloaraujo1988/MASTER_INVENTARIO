# Solução para Erros de Conexão no App Mobile

## Problema Identificado

O aplicativo está apresentando os seguintes erros:
- `IOException: Canceled`
- `JobCancellationException`
- `Erro ao chamar getDashboardStats`
- `Erro ao carregar dados do dashboard`

**Causa:** O app não consegue se conectar ao servidor Spring Boot na porta 8081.

---

## Soluções

### 1. Verificar se o Servidor está Rodando

O servidor Spring Boot precisa estar rodando na porta 8081.

**Verificar se a porta está em uso:**
```powershell
netstat -ano | findstr :8081
```

**Se não houver nenhum processo, inicie o servidor:**

#### Opção A - Maven:
```powershell
.\mvnw.cmd spring-boot:run
```

#### Opção B - Gradle:
```powershell
.\gradlew.bat bootRun
```

#### Opção C - Usar o script de reinicialização:
```powershell
.\restart-spring-server.ps1
```

---

### 2. Configurar o IP do Servidor no App

O app está configurado para usar `10.0.2.2:8081` (IP especial do emulador para acessar localhost).

**No emulador Android:**
- `10.0.2.2` = localhost da máquina host
- Porta: `8081`
- URL completa: `http://10.0.2.2:8081/inventario`

**Se estiver usando dispositivo físico:**
- Use o IP da sua máquina na rede local (ex: `192.168.1.100`)
- Configure no app durante o login

---

### 3. Verificar Firewall do Windows

O firewall pode estar bloqueando a conexão na porta 8081.

**Adicionar regra de entrada:**
```powershell
# Execute como Administrador
New-NetFirewallRule -DisplayName "Spring Boot 8081" -Direction Inbound -LocalPort 8081 -Protocol TCP -Action Allow
```

**Ou manualmente:**
1. Abra "Firewall do Windows com Segurança Avançada"
2. Clique em "Regras de Entrada"
3. Clique em "Nova Regra..."
4. Selecione "Porta" → Avançar
5. TCP → Portas locais específicas: `8081` → Avançar
6. Permitir a conexão → Avançar
7. Marque todos os perfis → Avançar
8. Nome: "Spring Boot 8081" → Concluir

---

### 4. Verificar Configuração do Servidor

**Arquivo: `src/main/resources/application.properties`**

Certifique-se de que o servidor está configurado para aceitar conexões:

```properties
# Porta do servidor
server.port=8081

# Permitir acesso de qualquer origem (desenvolvimento)
server.address=0.0.0.0

# Context path
server.servlet.context-path=/inventario
```

---

### 5. Testar Conectividade Manualmente

**Do computador (navegador):**
```
http://localhost:8081/inventario/actuator/health
```

**Do emulador (adb shell):**
```bash
adb shell
curl http://10.0.2.2:8081/inventario/actuator/health
```

---

### 6. Logs Úteis para Diagnóstico

**Ver logs do servidor:**
- Verifique o console onde o Spring Boot está rodando
- Procure por erros de inicialização
- Confirme que está escutando na porta 8081

**Ver logs do app (Android Studio):**
```
Logcat → Filtrar por "ServerConfigManager" ou "ApiClient"
```

---

### 7. Checklist de Verificação

- [ ] Servidor Spring Boot está rodando?
- [ ] Servidor está na porta 8081?
- [ ] Firewall permite conexões na porta 8081?
- [ ] App está configurado com IP correto (10.0.2.2 para emulador)?
- [ ] Emulador tem acesso à internet?
- [ ] CORS está configurado no servidor?

---

### 8. Configuração CORS no Servidor

Verifique se o arquivo `MobileSecurityConfig.java` tem a configuração CORS:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("*"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(false);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

---

### 9. Solução Rápida (Passo a Passo)

1. **Feche processos na porta 8081:**
   ```powershell
   .\kill-port-8081-force.ps1
   ```

2. **Inicie o servidor:**
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

3. **Aguarde o servidor inicializar completamente** (veja no console: "Started InventarioApplication")

4. **Teste no navegador:**
   ```
   http://localhost:8081/inventario/actuator/health
   ```
   Deve retornar: `{"status":"UP"}`

5. **Abra o app no emulador** e faça login

---

### 10. Problemas Comuns

**Erro: "Address already in use"**
- Solução: Use `.\kill-port-8081-force.ps1` para liberar a porta

**Erro: "Connection refused"**
- Solução: Verifique se o servidor está rodando

**Erro: "Unknown host"**
- Solução: Verifique a configuração de IP no app

**Erro: "Timeout"**
- Solução: Verifique firewall e conexão de rede

---

## Comandos Úteis

```powershell
# Verificar porta 8081
netstat -ano | findstr :8081

# Matar processo na porta 8081
.\kill-port-8081-force.ps1

# Iniciar servidor
.\mvnw.cmd spring-boot:run

# Reiniciar servidor (mata e inicia)
.\restart-spring-server.ps1

# Ver logs do emulador
adb logcat | findstr "ServerConfigManager"
```

---

## Contato e Suporte

Se o problema persistir:
1. Capture os logs completos do servidor
2. Capture os logs do Logcat do Android
3. Verifique se há erros de autenticação ou autorização
4. Teste os endpoints manualmente com Postman ou curl
