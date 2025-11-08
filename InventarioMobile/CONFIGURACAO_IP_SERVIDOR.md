# Configuração Simplificada do Servidor por IP

## Visão Geral

O aplicativo agora permite que o usuário configure o servidor informando apenas o **IP**, simplificando significativamente o processo de configuração. O aplicativo automaticamente constrói as URLs necessárias baseadas no IP fornecido.

## Como Funciona

### 1. Interface do Usuário
- O usuário agora vê apenas um campo "IP do Servidor" na tela de login
- Exemplo de entrada: `192.168.1.100`
- O campo possui validação automática de formato de IP

### 2. Construção Automática de URLs
Baseado no IP fornecido, o aplicativo automaticamente constrói:

```
IP informado: 192.168.1.100

URLs geradas automaticamente:
- URL Base: http://192.168.1.100:8081/api/v1/
- Login: http://192.168.1.100:8081/api/v1/auth/login
- Refresh Token: http://192.168.1.100:8081/api/v1/auth/refresh
- Health Check: http://192.168.1.100:8081/actuator/health
- Patrimônio: http://192.168.1.100:8081/api/v1/patrimonio
```

### 3. Configurações Padrão
- **Porta**: 8081 (configurável no ServerConfigManager)
- **Protocolo**: HTTP para desenvolvimento, HTTPS para produção
- **Contexto**: /api/v1/
- **Endpoints**: Pré-definidos para cada funcionalidade

## Exemplos Práticos

### Cenário 1: Servidor Local (Desenvolvimento)
```
IP informado: 127.0.0.1
URL resultante: http://127.0.0.1:8081/api/v1/
```

### Cenário 2: Servidor na Rede Local
```
IP informado: 192.168.1.100
URL resultante: http://192.168.1.100:8081/api/v1/
```

### Cenário 3: Servidor Remoto (IP Público)
```
IP informado: 203.0.113.10
URL resultante: http://203.0.113.10:8081/api/v1/
```

### Cenário 4: Configuração para Produção
```
IP informado: 10.0.0.50
URL resultante: https://10.0.0.50:8081/api/v1/ (se HTTPS habilitado)
```

## Validações Implementadas

### 1. Validação de Formato de IP
- Verifica se o formato está correto (xxx.xxx.xxx.xxx)
- Valida se cada octeto está entre 0-255
- Rejeita IPs inválidos como 999.999.999.999

### 2. Teste de Conectividade
- Antes do login, testa se o servidor está acessível
- Verifica se a porta está aberta
- Exibe mensagem clara se não conseguir conectar

### 3. Feedback Visual
- Campo fica vermelho se IP for inválido
- Mensagem de erro específica para cada tipo de problema
- Loading durante teste de conectividade

## Configurações Avançadas

### Alterando a Porta Padrão
No arquivo `ServerConfigManager.kt`:
```kotlin
private const val DEFAULT_PORT = 8081 // Altere aqui
```

### Habilitando HTTPS
```kotlin
private const val USE_HTTPS_DEFAULT = true // Para produção
```

### Alterando Contexto da API
```kotlin
private const val API_CONTEXT = "/api/v1/" // Contexto da API
```

## Vantagens da Nova Abordagem

1. **Simplicidade**: Usuário só precisa saber o IP
2. **Menos Erros**: Reduz erros de digitação em URLs complexas
3. **Padronização**: Garante que todas as URLs sigam o mesmo padrão
4. **Flexibilidade**: Fácil de adaptar para diferentes ambientes
5. **Validação**: Verifica formato e conectividade automaticamente

## Migração de Configurações Antigas

Se o aplicativo já tinha URLs configuradas, elas serão automaticamente convertidas:

```
URL antiga: http://192.168.1.100:8081/api/v1/
IP extraído: 192.168.1.100
```

O `ServerConfigManager` possui métodos para extrair o IP de URLs existentes e manter compatibilidade.

## Troubleshooting

### Problema: "IP inválido"
- Verifique se o formato está correto (xxx.xxx.xxx.xxx)
- Certifique-se de que cada número está entre 0-255

### Problema: "Não foi possível conectar ao servidor"
- Verifique se o servidor está rodando
- Confirme se a porta 8081 está aberta
- Teste conectividade de rede com o IP

### Problema: "Timeout de conexão"
- Servidor pode estar sobrecarregado
- Verifique configurações de firewall
- Confirme se o IP está correto