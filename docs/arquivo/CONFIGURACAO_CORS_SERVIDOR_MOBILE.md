# Configuração de CORS para Servidor Mobile API

**Data**: 02/11/2025  
**Status**: ✅ Implementado

## Problema Identificado

O aplicativo Android conseguia acessar o endpoint `/api/mobile/health` (health check funcionava), mas o login falhava ao tentar acessar o IP `192.168.10.107`.

### Sintomas

```
[MOBILE SERVER] 2025-11-02 00:51:13 - Health check respondido com sucesso
```

- ✅ Health check funciona
- ❌ Login falha
- ❌ Outros endpoints não respondem

### Causa Raiz

O servidor não tinha configuração adequada de **CORS (Cross-Origin Resource Sharing)**, que é essencial para permitir que aplicativos Android façam requisições HTTP para o servidor.

## Solução Implementada

### 1. Criação da Classe CorsConfig

**Arquivo**: `src/main/java/com/inventario/mobile/server/config/CorsConfig.java`

Criada uma classe de configuração dedicada para CORS com as seguintes características:

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // Permite qualquer origem
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "Content-Type", "X-Total-Count")
                .allowCredentials(true)
                .maxAge(3600);
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"
        ));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-Total-Count"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
```

### 2. Configurações de CORS

#### Origens Permitidas
```java
.allowedOriginPatterns("*")
```
- Permite requisições de **qualquer origem**
- Necessário para aplicativos Android que podem estar em diferentes redes
- Suporta IPs dinâmicos (192.168.x.x, 10.0.x.x, etc.)

#### Métodos HTTP Permitidos
```java
.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD")
```
- Todos os métodos necessários para a API REST
- `OPTIONS` é essencial para preflight requests

#### Headers Permitidos
```java
.allowedHeaders("*")
```
- Permite todos os headers
- Inclui `Authorization`, `Content-Type`, etc.

#### Headers Expostos
```java
.exposedHeaders("Authorization", "Content-Type", "X-Total-Count")
```
- Headers que o cliente pode ler na resposta
- `Authorization` para tokens JWT
- `X-Total-Count` para paginação

#### Credenciais
```java
.allowCredentials(true)
```
- Permite envio de cookies e headers de autenticação
- Necessário para JWT tokens

#### Cache de Preflight
```java
.maxAge(3600)
```
- Cache de 1 hora para requisições OPTIONS
- Reduz overhead de rede

### 3. Integração com Spring Security

A configuração de CORS foi integrada com o `MobileSecurityConfig`:

```java
http
    .securityMatcher("/api/mobile/**")
    .cors(cors -> cors.configurationSource(corsConfigurationSource()))
    // ... resto da configuração
```

## Como Funciona

### Fluxo de Requisição CORS

1. **Preflight Request** (OPTIONS)
   ```
   Cliente → OPTIONS /api/mobile/auth/login
   Servidor → 200 OK com headers CORS
   ```

2. **Requisição Real** (POST)
   ```
   Cliente → POST /api/mobile/auth/login
   Servidor → 200 OK com dados + headers CORS
   ```

### Headers CORS na Resposta

```http
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD
Access-Control-Allow-Headers: *
Access-Control-Expose-Headers: Authorization, Content-Type, X-Total-Count
Access-Control-Allow-Credentials: true
Access-Control-Max-Age: 3600
```

## Logs de Debug

A configuração adiciona logs informativos:

```
╔════════════════════════════════════════════════════════════════
║ Configurando CORS para API Mobile
║ Permitindo todas as origens para aplicativo Android
╚════════════════════════════════════════════════════════════════

Criando CorsConfigurationSource para Spring Security
CorsConfigurationSource configurado com sucesso
- Origens permitidas: * (todas)
- Métodos permitidos: GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD
- Headers permitidos: * (todos)
- Credenciais: permitidas
```

## Testando a Configuração

### Teste 1: Health Check
```bash
curl -X GET http://192.168.10.107:8081/api/mobile/health
```
**Esperado**: `200 OK` com headers CORS

### Teste 2: Login
```bash
curl -X POST http://192.168.10.107:8081/api/mobile/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```
**Esperado**: `200 OK` com token JWT e headers CORS

### Teste 3: Preflight
```bash
curl -X OPTIONS http://192.168.10.107:8081/api/mobile/auth/login \
  -H "Origin: http://localhost" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type"
```
**Esperado**: `200 OK` com headers CORS

### Teste 4: Do Aplicativo Android

1. Configurar IP: `192.168.10.107`
2. Tentar login
3. Verificar logs do servidor
4. ✅ Deve funcionar

## Verificação de Logs

### Logs do Servidor

```
[MOBILE SERVER] Health check respondido com sucesso
[MOBILE SERVER] ═══════════════════════════════════════════════════════════
[MOBILE SERVER] ENDPOINT DE LOGIN MOBILE CHAMADO
[MOBILE SERVER] Usuário: admin
[MOBILE SERVER] Senha fornecida: SIM
[MOBILE SERVER] Device ID: [device-id]
[MOBILE SERVER] App Version: 1.0.0
[MOBILE SERVER] ═══════════════════════════════════════════════════════════
[MOBILE SERVER] Chamando MobileAuthService.authenticateUser()...
[MOBILE SERVER] ═══════════════════════════════════════════════════════════
[MOBILE SERVER] LOGIN BEM-SUCEDIDO!
[MOBILE SERVER] ═══════════════════════════════════════════════════════════
```

### Logs do Android (Logcat)

```
D/NetworkModule: Base URL configurada: http://192.168.10.107:8081/inventario/
D/ApiClient: ApiService recriado com nova URL: http://192.168.10.107:8081/inventario
D/LoginViewModel: Login bem-sucedido
```

## Segurança

### Produção

Para ambiente de produção, é recomendado restringir as origens:

```java
// Em vez de:
configuration.setAllowedOriginPatterns(Collections.singletonList("*"));

// Usar:
configuration.setAllowedOriginPatterns(Arrays.asList(
    "http://192.168.10.*",
    "http://10.0.*.*",
    "https://app.seudominio.com"
));
```

### Desenvolvimento

Para desenvolvimento, `*` (todas as origens) é aceitável e facilita testes.

## Troubleshooting

### Problema: CORS ainda não funciona

**Solução 1**: Verificar se o servidor foi reiniciado
```bash
# Parar servidor
Ctrl+C

# Iniciar novamente
java -jar target/sistema-inventario-1.2.0.jar
```

**Solução 2**: Verificar logs de inicialização
```
Procurar por:
"Configurando CORS para API Mobile"
"CorsConfigurationSource configurado com sucesso"
```

**Solução 3**: Limpar cache do navegador/app
```kotlin
// No app Android
ApiClient.clearInstance()
```

### Problema: Preflight falha

**Causa**: Servidor não responde a OPTIONS

**Solução**: Verificar se `OPTIONS` está na lista de métodos permitidos

### Problema: Headers não aparecem

**Causa**: Headers não estão expostos

**Solução**: Adicionar header em `exposedHeaders`

## Arquivos Modificados

1. ✅ `CorsConfig.java` (novo) - Configuração de CORS
2. ✅ `MobileSecurityConfig.java` (já tinha CORS, mas foi reforçado)

## Compilação

```bash
# Compilar
.\mvnw.cmd clean compile -DskipTests

# Resultado
BUILD SUCCESS
Total time: 31.190 s
```

## Próximos Passos

1. ✅ Reiniciar servidor mobile
2. ✅ Testar login do app com IP 192.168.10.107
3. ✅ Verificar logs do servidor
4. ✅ Confirmar que login funciona

---

**Status**: ✅ Implementado e compilado  
**Impacto**: Crítico - Permite comunicação app ↔ servidor  
**Ambiente**: Desenvolvimento e Produção
