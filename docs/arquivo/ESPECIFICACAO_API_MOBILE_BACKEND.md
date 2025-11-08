# Especificação API REST - Backend para Aplicativo Móvel
## Sistema de Inventário

### 📋 **VISÃO GERAL**

Esta especificação define a API REST que deve ser implementada no backend Spring Boot para suportar o aplicativo móvel Android do sistema de inventário.

**Base URL**: `http://localhost:8080/api/mobile`
**Versão**: v1.0
**Autenticação**: JWT Bearer Token

---

## 🔐 **1. AUTENTICAÇÃO**

### **POST /api/mobile/auth/login**
**Descrição**: Autenticação de usuário móvel

**Request Body**:
```json
{
  "username": "string",
  "password": "string",
  "deviceId": "string",
  "appVersion": "string"
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "data": {
    "accessToken": "jwt_token_here",
    "refreshToken": "refresh_token_here",
    "expiresIn": 3600,
    "tokenType": "Bearer",
    "user": {
      "id": 1,
      "username": "operador01",
      "nome": "João Silva",
      "email": "joao@empresa.com",
      "setorId": 5,
      "setorNome": "TI",
      "perfil": "OPERADOR"
    }
  }
}
```

### **POST /api/mobile/auth/refresh**
**Descrição**: Renovação de token de acesso

**Request Body**:
```json
{
  "refreshToken": "refresh_token_here"
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "data": {
    "accessToken": "new_jwt_token",
    "expiresIn": 3600,
    "tokenType": "Bearer"
  }
}
```

### **POST /api/mobile/auth/logout**
**Descrição**: Logout do usuário

**Headers**: `Authorization: Bearer {token}`

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Logout realizado com sucesso"
}
```

### **GET /api/mobile/auth/verify**
**Descrição**: Verificação de validade do token

**Headers**: `Authorization: Bearer {token}`

**Response Success (200)**:
```json
{
  "success": true,
  "data": {
    "valid": true,
    "expiresIn": 1800
  }
}
```

---

## 📦 **2. PATRIMÔNIO**

### **POST /api/mobile/patrimonio/buscar**
**Descrição**: Busca patrimônio por código ou QR Code

**Headers**: `Authorization: Bearer {token}`

**Request Body**:
```json
{
  "codigo": "PAT001234",
  "qrCode": "QR_CODE_STRING"
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "numeroPatrimonio": "PAT001234",
    "descricao": "Notebook Dell Inspiron",
    "marca": "Dell",
    "modelo": "Inspiron 15 3000",
    "numeroSerie": "ABC123456",
    "estado": "ATIVO",
    "valor": 2500.00,
    "dataAquisicao": "2023-01-15",
    "setorId": 5,
    "setorNome": "TI",
    "salaId": 12,
    "salaNome": "Sala 101",
    "qrCode": "QR_CODE_STRING",
    "observacoes": "Equipamento em bom estado",
    "jaColetado": false,
    "ultimaColeta": null
  }
}
```

### **GET /api/mobile/patrimonio/{id}**
**Descrição**: Busca patrimônio por ID

**Headers**: `Authorization: Bearer {token}`

**Response Success (200)**:
```json
{
  "success": true,
  "data": {
    // Mesmo formato do endpoint de busca
  }
}
```

### **GET /api/mobile/patrimonio**
**Descrição**: Lista patrimônios com paginação e filtros

**Headers**: `Authorization: Bearer {token}`

**Query Parameters**:
- `page` (int): Página (default: 1)
- `perPage` (int): Itens por página (default: 50, max: 100)
- `search` (string): Busca por código ou descrição
- `setorId` (int): Filtro por setor
- `salaId` (int): Filtro por sala
- `estado` (string): Filtro por estado (ATIVO, INATIVO, BAIXADO)
- `coletado` (boolean): Filtro por status de coleta

**Response Success (200)**:
```json
{
  "success": true,
  "data": {
    "patrimonios": [
      {
        // Formato do patrimônio
      }
    ],
    "pagination": {
      "currentPage": 1,
      "totalPages": 10,
      "totalItems": 500,
      "perPage": 50,
      "hasNext": true,
      "hasPrevious": false
    }
  }
}
```

---

## 🏢 **3. SINCRONIZAÇÃO DE DADOS**

### **GET /api/mobile/sync/setores**
**Descrição**: Sincronização de setores

**Headers**: `Authorization: Bearer {token}`

**Query Parameters**:
- `lastSync` (string): Data da última sincronização (ISO 8601)

**Response Success (200)**:
```json
{
  "success": true,
  "data": {
    "setores": [
      {
        "id": 1,
        "nome": "Tecnologia da Informação",
        "descricao": "Setor responsável pela TI",
        "responsavelSetor": "João Silva",
        "telefone": "(11) 99999-9999",
        "email": "ti@empresa.com",
        "ativo": true,
        "dataCriacao": "2023-01-01T00:00:00Z",
        "dataAtualizacao": "2023-06-15T10:30:00Z"
      }
    ],
    "lastSync": "2023-12-01T15:30:00Z"
  }
}
```

### **GET /api/mobile/sync/salas**
**Descrição**: Sincronização de salas

**Headers**: `Authorization: Bearer {token}`

**Query Parameters**:
- `lastSync` (string): Data da última sincronização
- `setorId` (int): Filtro por setor (opcional)

**Response Success (200)**:
```json
{
  "success": true,
  "data": {
    "salas": [
      {
        "id": 1,
        "nome": "Sala 101",
        "descricao": "Sala de reuniões",
        "setorId": 1,
        "setorNome": "TI",
        "capacidade": 10,
        "ativa": true,
        "dataCriacao": "2023-01-01T00:00:00Z",
        "dataAtualizacao": "2023-06-15T10:30:00Z"
      }
    ],
    "lastSync": "2023-12-01T15:30:00Z"
  }
}
```

### **GET /api/mobile/sync/usuarios**
**Descrição**: Sincronização de usuários

**Headers**: `Authorization: Bearer {token}`

**Query Parameters**:
- `lastSync` (string): Data da última sincronização

**Response Success (200)**:
```json
{
  "success": true,
  "data": {
    "usuarios": [
      {
        "id": 1,
        "username": "operador01",
        "nome": "João Silva",
        "email": "joao@empresa.com",
        "setorId": 1,
        "setorNome": "TI",
        "perfil": "OPERADOR",
        "ativo": true,
        "dataCriacao": "2023-01-01T00:00:00Z"
      }
    ],
    "lastSync": "2023-12-01T15:30:00Z"
  }
}
```

---

## 📋 **4. COLETA**

### **POST /api/mobile/coleta**
**Descrição**: Registra uma nova coleta

**Headers**: `Authorization: Bearer {token}`

**Request Body**:
```json
{
  "patrimonioId": 1,
  "patrimonioCodigo": "PAT001234",
  "usuarioId": 1,
  "dataColeta": "2023-12-01T15:30:00Z",
  "setorEncontradoId": 1,
  "salaEncontradaId": 12,
  "situacaoEncontrada": "LOCALIZADO",
  "observacoes": "Patrimônio encontrado em bom estado",
  "latitude": -23.5505,
  "longitude": -46.6333,
  "fotoBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQ...",
  "metodoColeta": "QR_CODE",
  "dispositivoId": "DEVICE_123"
}
```

**Response Success (201)**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "patrimonioId": 1,
    "usuarioId": 1,
    "dataColeta": "2023-12-01T15:30:00Z",
    "status": "COLETADO",
    "servidorId": 1001,
    "dataCriacao": "2023-12-01T15:30:00Z"
  }
}
```

### **GET /api/mobile/coleta**
**Descrição**: Lista coletas do usuário

**Headers**: `Authorization: Bearer {token}`

**Query Parameters**:
- `page` (int): Página (default: 1)
- `perPage` (int): Itens por página (default: 50)
- `dataInicio` (string): Data início (ISO 8601)
- `dataFim` (string): Data fim (ISO 8601)
- `status` (string): Filtro por status

**Response Success (200)**:
```json
{
  "success": true,
  "data": {
    "coletas": [
      {
        "id": 1,
        "patrimonioId": 1,
        "patrimonioCodigo": "PAT001234",
        "patrimonioDescricao": "Notebook Dell",
        "usuarioId": 1,
        "dataColeta": "2023-12-01T15:30:00Z",
        "status": "COLETADO",
        "observacoes": "Patrimônio em bom estado",
        "sincronizado": true
      }
    ],
    "pagination": {
      "currentPage": 1,
      "totalPages": 5,
      "totalItems": 250,
      "perPage": 50
    }
  }
}
```

### **PUT /api/mobile/coleta/{id}**
**Descrição**: Atualiza uma coleta existente

**Headers**: `Authorization: Bearer {token}`

**Request Body**:
```json
{
  "observacoes": "Observações atualizadas",
  "situacaoEncontrada": "LOCALIZADO",
  "fotoBase64": "nova_foto_base64"
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "dataAtualizacao": "2023-12-01T16:00:00Z"
  }
}
```

---

## 📊 **5. RELATÓRIOS E ESTATÍSTICAS**

### **GET /api/mobile/relatorio/resumo**
**Descrição**: Resumo de coletas do usuário

**Headers**: `Authorization: Bearer {token}`

**Query Parameters**:
- `dataInicio` (string): Data início
- `dataFim` (string): Data fim

**Response Success (200)**:
```json
{
  "success": true,
  "data": {
    "totalPatrimonios": 1000,
    "patrimoniosColetados": 750,
    "percentualConclusao": 75.0,
    "coletasHoje": 25,
    "coletasSemana": 150,
    "ultimaColeta": "2023-12-01T15:30:00Z"
  }
}
```

---

## ⚠️ **6. TRATAMENTO DE ERROS**

### **Estrutura Padrão de Erro**:
```json
{
  "success": false,
  "error": {
    "code": "PATRIMONIO_NOT_FOUND",
    "message": "Patrimônio não encontrado",
    "details": "O patrimônio com código PAT001234 não foi encontrado no sistema",
    "timestamp": "2023-12-01T15:30:00Z"
  }
}
```

### **Códigos de Erro Comuns**:
- `INVALID_CREDENTIALS` (401): Credenciais inválidas
- `TOKEN_EXPIRED` (401): Token expirado
- `INSUFFICIENT_PERMISSIONS` (403): Permissões insuficientes
- `PATRIMONIO_NOT_FOUND` (404): Patrimônio não encontrado
- `PATRIMONIO_ALREADY_COLLECTED` (409): Patrimônio já coletado
- `VALIDATION_ERROR` (422): Erro de validação
- `INTERNAL_SERVER_ERROR` (500): Erro interno do servidor

---

## 🔧 **7. CONFIGURAÇÕES TÉCNICAS**

### **Headers Obrigatórios**:
- `Content-Type: application/json`
- `Authorization: Bearer {token}` (exceto endpoints de login)
- `X-App-Version: 1.0.0` (opcional, para versionamento)

### **Rate Limiting**:
- 100 requisições por minuto por usuário
- 1000 requisições por hora por usuário

### **Timeouts**:
- Conexão: 30 segundos
- Leitura: 60 segundos
- Upload de foto: 120 segundos

### **Tamanho Máximo**:
- Request Body: 10MB
- Foto Base64: 5MB

---

## 📝 **8. NOTAS DE IMPLEMENTAÇÃO**

1. **Autenticação JWT**: Implementar com refresh token automático
2. **Sincronização**: Usar timestamps para sincronização incremental
3. **Fotos**: Aceitar formato Base64 e salvar como arquivo no servidor
4. **Coordenadas GPS**: Validar formato e precisão
5. **Paginação**: Implementar paginação consistente em todos os endpoints
6. **Logs**: Registrar todas as operações para auditoria
7. **Cache**: Implementar cache para dados de sincronização
8. **Validação**: Validar todos os dados de entrada rigorosamente

---

**Versão**: 1.0  
**Data**: Dezembro 2023  
**Autor**: Sistema de Inventário - Equipe de Desenvolvimento