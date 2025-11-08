# Guia de Configuração de IP para Emulador Android

**Data**: 02/11/2025  
**Problema**: Emulador tentando acessar 127.0.0.1 (localhost)

## Problema Identificado

O erro mostra que o emulador está tentando acessar:
```
Remote Addr: 127.0.0.1
```

### Por que 127.0.0.1 não funciona no emulador?

O emulador Android tem seu próprio sistema operacional isolado. Quando você usa `127.0.0.1` ou `localhost` no emulador, ele tenta acessar o próprio emulador, não a máquina host.

## Soluções

### Solução 1: Usar IP Especial do Emulador (Recomendado para Desenvolvimento)

Para o emulador acessar o servidor na máquina host, use o IP especial:

```
10.0.2.2
```

Este é um IP especial que o emulador Android mapeia para o localhost da máquina host.

**Como configurar no app:**
1. Abrir o aplicativo
2. Na tela de login, no campo "IP do Servidor"
3. Digitar: `10.0.2.2`
4. Fazer login

### Solução 2: Usar IP Real da Máquina (Recomendado para Dispositivo Físico)

Se você está usando um dispositivo físico ou quer que o emulador acesse pela rede real:

```
192.168.10.107
```

**Como configurar no app:**
1. Abrir o aplicativo
2. Na tela de login, no campo "IP do Servidor"
3. Digitar: `192.168.10.107`
4. Fazer login

## Tabela de Referência

| Cenário | IP a Usar | Porta | URL Completa |
|---------|-----------|-------|--------------|
| **Emulador → Servidor Local** | `10.0.2.2` | 8081 | `http://10.0.2.2:8081` |
| **Dispositivo Físico → Servidor Local** | `192.168.10.107` | 8081 | `http://192.168.10.107:8081` |
| **Produção** | IP do servidor | 8081 | `http://[ip-servidor]:8081` |

## Verificando o IP da Sua Máquina

### Windows
```cmd
ipconfig
```
Procure por "Endereço IPv4" na seção da sua rede ativa.

### Linux/Mac
```bash
ifconfig
# ou
ip addr show
```

## Testando a Conexão

### Teste 1: Health Check do Emulador

No emulador, abra o navegador e acesse:
```
http://10.0.2.2:8081/api/mobile/health
```

**Esperado**: Resposta JSON com status do servidor

### Teste 2: Health Check do Dispositivo Físico

No dispositivo, abra o navegador e acesse:
```
http://192.168.10.107:8081/api/mobile/health
```

**Esperado**: Resposta JSON com status do servidor

### Teste 3: Pelo Terminal

```bash
# Da máquina host
curl http://localhost:8081/api/mobile/health

# Deve retornar algo como:
{"status":"UP","timestamp":"2025-11-02T00:00:00"}
```

## Configuração Atual do App

O app está configurado com:
- **FALLBACK_IP**: `192.168.10.107`
- **DEFAULT_PORT**: `8081`
- **CONTEXT_PATH**: `/inventario`
- **API_PATH**: `/api/mobile`

### URL Base Completa
```
http://192.168.10.107:8081/inventario/api/mobile
```

## Passo a Passo para Corrigir

### Para Emulador

1. **Abrir o app no emulador**

2. **Na tela de login**, alterar o IP:
   - Campo: "IP do Servidor"
   - Valor: `10.0.2.2`

3. **Fazer login**:
   - Login: `admin`
   - Senha: `admin123`

4. **Verificar logs do servidor**:
   ```
   [MOBILE SERVER] Health check respondido com sucesso
   [MOBILE SERVER] ENDPOINT DE LOGIN MOBILE CHAMADO
   [MOBILE SERVER] LOGIN BEM-SUCEDIDO!
   ```

### Para Dispositivo Físico

1. **Garantir que dispositivo e servidor estão na mesma rede**

2. **Verificar IP da máquina**:
   ```cmd
   ipconfig
   ```

3. **No app**, configurar o IP real:
   - Campo: "IP do Servidor"
   - Valor: `192.168.10.107` (ou o IP que você encontrou)

4. **Fazer login**

## Troubleshooting

### Erro: "Connection refused"

**Causa**: Servidor não está rodando ou porta está bloqueada

**Solução**:
1. Verificar se servidor está rodando:
   ```bash
   netstat -ano | findstr :8081
   ```

2. Iniciar servidor se necessário:
   ```bash
   java -jar target/sistema-inventario-1.2.0.jar
   ```

3. Verificar firewall:
   ```bash
   # Windows
   netsh advfirewall firewall add rule name="Mobile API" dir=in action=allow protocol=TCP localport=8081
   ```

### Erro: "Host not found"

**Causa**: IP incorreto ou rede não acessível

**Solução**:
1. Verificar IP da máquina
2. Garantir que dispositivo está na mesma rede
3. Testar ping:
   ```bash
   ping 192.168.10.107
   ```

### Erro: "Timeout"

**Causa**: Firewall bloqueando ou rede lenta

**Solução**:
1. Desabilitar firewall temporariamente para teste
2. Verificar se antivírus está bloqueando
3. Testar com outro dispositivo

## IPs Especiais do Emulador Android

| IP | Descrição |
|----|-----------|
| `10.0.2.2` | Localhost da máquina host |
| `10.0.2.3` | Primeiro DNS server |
| `10.0.2.15` | IP do próprio emulador |
| `10.0.2.1` | Gateway/Router |

## Configuração Permanente

Para evitar ter que configurar toda vez, você pode:

### Opção 1: Criar Perfis no App

Adicionar botões de perfil rápido:
- "Emulador (10.0.2.2)"
- "Rede Local (192.168.10.107)"
- "Produção (IP do servidor)"

### Opção 2: Detecção Automática

O app já tem lógica para detectar se está em emulador:
```kotlin
// Em ServerConfigManager.kt
private const val FALLBACK_IP = "192.168.10.107"
```

Mas você pode adicionar detecção de emulador:
```kotlin
fun getDefaultIp(): String {
    return if (isEmulator()) {
        "10.0.2.2"
    } else {
        "192.168.10.107"
    }
}

private fun isEmulator(): Boolean {
    return (Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86"))
}
```

## Resumo Rápido

### ✅ Para Emulador
```
IP: 10.0.2.2
Porta: 8081
URL: http://10.0.2.2:8081
```

### ✅ Para Dispositivo Físico
```
IP: 192.168.10.107
Porta: 8081
URL: http://192.168.10.107:8081
```

### ✅ Verificar Servidor
```bash
# Deve estar rodando
curl http://localhost:8081/api/mobile/health
```

---

**Próximo Passo**: Configure o IP correto no app e tente fazer login novamente.
