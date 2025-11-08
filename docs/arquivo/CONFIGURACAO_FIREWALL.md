# Configuração do Firewall para InventarioMobile

## Resumo
Este documento descreve as regras de firewall configuradas para permitir a comunicação entre o aplicativo mobile InventarioMobile e o servidor backend.

## Regras de Firewall Criadas

### 1. Regra para Servidor Mobile (Porta 8081)
- **Nome da Regra:** SIHCP Mobile Server
- **Direção:** Entrada (Inbound)
- **Protocolo:** TCP
- **Porta Local:** 8081
- **Ação:** Permitir
- **Perfis:** Domínio, Particular, Público
- **Status:** ✅ Ativa

**Comando utilizado:**
```cmd
netsh advfirewall firewall add rule name="SIHCP Mobile Server" dir=in action=allow protocol=TCP localport=8081
```

### 2. Status do Firewall do Windows
- **Perfil Domínio:** Desabilitado
- **Perfil Particular:** Desabilitado  
- **Perfil Público:** Desabilitado

## Portas Utilizadas pelo Sistema

### Servidor Mobile API
- **Porta:** 8081
- **Protocolo:** HTTP/TCP
- **Finalidade:** API REST para comunicação com aplicativo mobile
- **Endpoint de Health:** `http://localhost:8081/inventario/api/mobile/health`

### Banco de Dados PostgreSQL
- **Porta:** 5432
- **Protocolo:** TCP
- **Finalidade:** Conexão com banco de dados
- **Status:** Acesso local apenas (sem regra de firewall externa)

## Testes de Conectividade Realizados

### 1. Teste Local (Servidor)
```bash
curl -s http://localhost:8081/inventario/api/mobile/health
```
**Resultado:** ✅ Sucesso
```json
{"service":"SIHCP Mobile API","version":"1.0.0","status":"UP","timestamp":"2025-10-31T00:18:19.864487100"}
```

### 2. Teste do Emulador Android
**IP utilizado:** 10.0.2.2 (IP especial do emulador para acessar localhost do host)
**Logs do aplicativo:**
```
ServerConfigManager: Testando conectividade com o servidor: 10.0.2.2
ServerConfigManager: Conectividade testada com sucesso
```
**Resultado:** ✅ Sucesso

## Configuração do Aplicativo Mobile

### IP do Servidor
- **Emulador Android:** 10.0.2.2:8081
- **Dispositivo físico na mesma rede:** [IP_DA_MAQUINA]:8081

### Configurações no ServerConfigManager.kt
```kotlin
private const val FALLBACK_IP = "10.0.2.2"
private const val DEFAULT_PORT = 8081
private const val DEFAULT_CONTEXT_PATH = "/inventario"
private const val DEFAULT_API_PATH = "/api/mobile"
```

## Scripts Disponíveis

### Liberar Firewall
- **Arquivo:** `liberar-firewall-8081.bat`
- **Função:** Cria regra de firewall para porta 8081

### Verificar Porta
```powershell
netstat -ano | findstr :8081
```

### Verificar Regra de Firewall
```cmd
netsh advfirewall firewall show rule name="SIHCP Mobile Server"
```

## Solução de Problemas

### Aplicativo não conecta ao servidor
1. Verificar se o servidor está rodando na porta 8081
2. Verificar se a regra de firewall está ativa
3. Verificar se o IP está correto (10.0.2.2 para emulador)
4. Verificar logs do aplicativo: `adb logcat -s "ServerConfigManager"`

### Servidor não inicia na porta 8081
1. Verificar se a porta está ocupada: `netstat -ano | findstr :8081`
2. Usar script para liberar porta: `.\kill-port-8081-force.ps1`
3. Reiniciar servidor: `.\restart-spring-server.ps1`

## Data da Configuração
**Configurado em:** 31/10/2024
**Versão do aplicativo:** 1.2 (versionCode 3)
**Status:** ✅ Funcionando corretamente

## Observações
- O firewall do Windows está atualmente desabilitado em todos os perfis
- A regra foi criada preventivamente para casos onde o firewall seja habilitado
- A comunicação entre aplicativo e servidor está funcionando corretamente
- Não foi necessário criar regra para PostgreSQL (porta 5432) pois o acesso é apenas local