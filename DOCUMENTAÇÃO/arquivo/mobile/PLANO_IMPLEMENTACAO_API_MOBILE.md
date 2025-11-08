# Plano de Implementação - API REST Mobile
## Sistema de Inventário

### 🎯 **OBJETIVO**
Implementar a API REST no backend Spring Boot para suportar completamente o aplicativo móvel Android do sistema de inventário.

---

## 📋 **FASES DE IMPLEMENTAÇÃO**

### **FASE 1: CONFIGURAÇÃO BASE** ⏱️ *2-3 dias*

#### **1.1 Configuração do Spring Security**
- [ ] Configurar JWT Authentication
- [ ] Implementar filtros de segurança
- [ ] Configurar CORS para aplicativo móvel
- [ ] Definir perfis de usuário (ADMIN, OPERADOR)

**Arquivos a criar/modificar:**
- `src/main/java/com/inventario/config/SecurityConfig.java`
- `src/main/java/com/inventario/config/JwtConfig.java`
- `src/main/java/com/inventario/security/JwtAuthenticationFilter.java`
- `src/main/java/com/inventario/security/JwtTokenProvider.java`

#### **1.2 Configuração de DTOs**
- [ ] Criar DTOs para requests/responses da API mobile
- [ ] Implementar validações com Bean Validation
- [ ] Configurar mappers (ModelMapper ou MapStruct)

**Arquivos a criar:**
- `src/main/java/com/inventario/dto/mobile/`
  - `AuthRequestDTO.java`
  - `AuthResponseDTO.java`
  - `PatrimonioMobileDTO.java`
  - `ColetaMobileDTO.java`
  - `SincronizacaoDTO.java`

#### **1.3 Configuração de Exceções**
- [ ] Implementar handler global de exceções
- [ ] Criar exceções customizadas
- [ ] Padronizar respostas de erro

**Arquivos a criar:**
- `src/main/java/com/inventario/exception/GlobalExceptionHandler.java`
- `src/main/java/com/inventario/exception/MobileApiException.java`
- `src/main/java/com/inventario/dto/ErrorResponseDTO.java`

---

### **FASE 2: AUTENTICAÇÃO** ⏱️ *3-4 dias*

#### **2.1 Controller de Autenticação**
- [ ] Implementar `MobileAuthController`
- [ ] Endpoint de login com JWT
- [ ] Endpoint de refresh token
- [ ] Endpoint de logout
- [ ] Endpoint de verificação de token

**Arquivo a criar:**
- `src/main/java/com/inventario/controller/mobile/MobileAuthController.java`

#### **2.2 Serviços de Autenticação**
- [ ] Implementar `MobileAuthService`
- [ ] Validação de credenciais
- [ ] Geração e validação de tokens JWT
- [ ] Gerenciamento de refresh tokens

**Arquivos a criar:**
- `src/main/java/com/inventario/service/mobile/MobileAuthService.java`
- `src/main/java/com/inventario/service/TokenService.java`

#### **2.3 Entidades de Token**
- [ ] Criar entidade `RefreshToken`
- [ ] Repository para refresh tokens
- [ ] Limpeza automática de tokens expirados

**Arquivos a criar:**
- `src/main/java/com/inventario/model/RefreshToken.java`
- `src/main/java/com/inventario/dao/RefreshTokenRepository.java`

---

### **FASE 3: PATRIMÔNIO** ⏱️ *4-5 dias*

#### **3.1 Controller de Patrimônio**
- [ ] Implementar `MobilePatrimonioController`
- [ ] Endpoint de busca por código/QR
- [ ] Endpoint de busca por ID
- [ ] Endpoint de listagem com paginação e filtros

**Arquivo a criar:**
- `src/main/java/com/inventario/controller/mobile/MobilePatrimonioController.java`

#### **3.2 Serviços de Patrimônio**
- [ ] Implementar `MobilePatrimonioService`
- [ ] Lógica de busca otimizada
- [ ] Verificação de status de coleta
- [ ] Filtros e paginação

**Arquivo a criar:**
- `src/main/java/com/inventario/service/mobile/MobilePatrimonioService.java`

#### **3.3 Otimizações de Consulta**
- [ ] Criar queries customizadas no `PatrimonioRepository`
- [ ] Implementar cache para consultas frequentes
- [ ] Índices no banco de dados

**Arquivo a modificar:**
- `src/main/java/com/inventario/dao/PatrimonioRepository.java`

---

### **FASE 4: SINCRONIZAÇÃO** ⏱️ *3-4 dias*

#### **4.1 Controller de Sincronização**
- [ ] Implementar `MobileSyncController`
- [ ] Endpoint de sincronização de setores
- [ ] Endpoint de sincronização de salas
- [ ] Endpoint de sincronização de usuários

**Arquivo a criar:**
- `src/main/java/com/inventario/controller/mobile/MobileSyncController.java`

#### **4.2 Serviços de Sincronização**
- [ ] Implementar `MobileSyncService`
- [ ] Lógica de sincronização incremental
- [ ] Controle de timestamps
- [ ] Otimização de dados transferidos

**Arquivo a criar:**
- `src/main/java/com/inventario/service/mobile/MobileSyncService.java`

#### **4.3 Cache de Sincronização**
- [ ] Implementar cache Redis/Hazelcast
- [ ] Estratégia de invalidação
- [ ] Configuração de TTL

---

### **FASE 5: COLETA** ⏱️ *5-6 dias*

#### **5.1 Controller de Coleta**
- [ ] Implementar `MobileColetaController`
- [ ] Endpoint de criação de coleta
- [ ] Endpoint de listagem de coletas
- [ ] Endpoint de atualização de coleta

**Arquivo a criar:**
- `src/main/java/com/inventario/controller/mobile/MobileColetaController.java`

#### **5.2 Serviços de Coleta**
- [ ] Implementar `MobileColetaService`
- [ ] Validação de dados de coleta
- [ ] Processamento de fotos Base64
- [ ] Validação de coordenadas GPS

**Arquivo a criar:**
- `src/main/java/com/inventario/service/mobile/MobileColetaService.java`

#### **5.3 Processamento de Imagens**
- [ ] Implementar `ImageProcessingService`
- [ ] Conversão Base64 para arquivo
- [ ] Redimensionamento automático
- [ ] Validação de formato

**Arquivo a criar:**
- `src/main/java/com/inventario/service/ImageProcessingService.java`

#### **5.4 Auditoria e Logs**
- [ ] Implementar logs detalhados de coleta
- [ ] Rastreamento de alterações
- [ ] Métricas de performance

---

### **FASE 6: RELATÓRIOS** ⏱️ *2-3 dias*

#### **6.1 Controller de Relatórios**
- [ ] Implementar `MobileRelatorioController`
- [ ] Endpoint de resumo de coletas
- [ ] Estatísticas do usuário

**Arquivo a criar:**
- `src/main/java/com/inventario/controller/mobile/MobileRelatorioController.java`

#### **6.2 Serviços de Relatórios**
- [ ] Implementar `MobileRelatorioService`
- [ ] Cálculos de estatísticas
- [ ] Queries otimizadas

**Arquivo a criar:**
- `src/main/java/com/inventario/service/mobile/MobileRelatorioService.java`

---

### **FASE 7: TESTES E DOCUMENTAÇÃO** ⏱️ *3-4 dias*

#### **7.1 Testes Unitários**
- [ ] Testes para todos os controllers
- [ ] Testes para todos os services
- [ ] Cobertura mínima de 80%

#### **7.2 Testes de Integração**
- [ ] Testes de endpoints completos
- [ ] Testes de autenticação
- [ ] Testes de cenários de erro

#### **7.3 Documentação Swagger**
- [ ] Configurar Swagger/OpenAPI
- [ ] Documentar todos os endpoints
- [ ] Exemplos de request/response

**Arquivo a criar:**
- `src/main/java/com/inventario/config/SwaggerConfig.java`

---

## 🛠️ **CONFIGURAÇÕES NECESSÁRIAS**

### **application.yml**
```yaml
# Configurações JWT
jwt:
  secret: ${JWT_SECRET:sua_chave_secreta_aqui}
  expiration: 3600000 # 1 hora
  refresh-expiration: 604800000 # 7 dias

# Configurações de upload
upload:
  max-file-size: 5MB
  allowed-types: image/jpeg,image/png
  storage-path: ${UPLOAD_PATH:./uploads}

# Configurações de cache
spring:
  cache:
    type: redis
  redis:
    host: localhost
    port: 6379
```

### **Dependências Maven (pom.xml)**
```xml
<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>

<!-- Cache Redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- Swagger -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.0.2</version>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

---

## 📊 **CRONOGRAMA ESTIMADO**

| Fase | Duração | Desenvolvedor(es) | Dependências |
|------|---------|-------------------|--------------|
| 1 - Configuração Base | 3 dias | Backend Dev | - |
| 2 - Autenticação | 4 dias | Backend Dev | Fase 1 |
| 3 - Patrimônio | 5 dias | Backend Dev | Fase 2 |
| 4 - Sincronização | 4 dias | Backend Dev | Fase 3 |
| 5 - Coleta | 6 dias | Backend Dev | Fase 4 |
| 6 - Relatórios | 3 dias | Backend Dev | Fase 5 |
| 7 - Testes/Docs | 4 dias | Backend Dev + QA | Todas |

**Total Estimado: 29 dias úteis (~6 semanas)**

---

## ✅ **CRITÉRIOS DE ACEITAÇÃO**

### **Funcionalidade**
- [ ] Todos os endpoints da especificação implementados
- [ ] Autenticação JWT funcionando
- [ ] Sincronização incremental operacional
- [ ] Upload de fotos funcionando
- [ ] Paginação em todos os endpoints de listagem

### **Performance**
- [ ] Tempo de resposta < 500ms para 95% das requisições
- [ ] Suporte a 100 usuários simultâneos
- [ ] Cache implementado para dados de sincronização

### **Segurança**
- [ ] Todas as rotas protegidas por autenticação
- [ ] Validação de entrada em todos os endpoints
- [ ] Rate limiting implementado
- [ ] Logs de auditoria funcionando

### **Qualidade**
- [ ] Cobertura de testes > 80%
- [ ] Documentação Swagger completa
- [ ] Código seguindo padrões do projeto
- [ ] Tratamento de erros padronizado

---

## 🚀 **PRÓXIMOS PASSOS**

1. **Revisar especificação** com equipe de desenvolvimento
2. **Configurar ambiente** de desenvolvimento
3. **Criar branch** específica para API mobile
4. **Iniciar Fase 1** - Configuração Base
5. **Configurar CI/CD** para testes automatizados

---

**Responsável**: Equipe Backend  
**Revisão**: Arquiteto de Software  
**Aprovação**: Tech Lead