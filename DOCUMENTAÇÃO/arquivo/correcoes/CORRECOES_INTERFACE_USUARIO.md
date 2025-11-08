# Correções de Interface do Usuário

## Data: 05/11/2025

### Correções Implementadas

#### 1. Mensagem de Status de Coleta
**Problema**: Após coletar um patrimônio, a mensagem exibia "Status: JÁ COLETADO", dando uma impressão errada.

**Solução**: Alterada a mensagem para "Status: COLETADO" (mais concisa e clara).

**Arquivo**: `InventarioMobile/app/src/main/java/com/inventario/mobile/presentation/scanner/ScannerActivity.kt`

**Linha**: 296

```kotlin
// ANTES:
val statusText = if (result.jaColetado) {
    "Status: JÁ COLETADO"
} else {
    "Status: Disponível para coleta"
}

// DEPOIS:
val statusText = if (result.jaColetado) {
    "Status: COLETADO"
} else {
    "Status: Disponível para coleta"
}
```

#### 2. Mensagem de Erro de Login
**Problema**: Quando o usuário errava a senha, aparecia "Erro desconhecido" ao invés de uma mensagem clara.

**Solução**: 
1. Melhorado o `ErrorMapper` para detectar erros de autenticação mesmo quando não são HttpException
2. Atualizada a string de erro 401 para ser mais clara

**Arquivos Modificados**:

**a) ErrorMapper.kt**
- Adicionada lógica para detectar palavras-chave relacionadas a autenticação na mensagem de erro
- Agora detecta: "401", "unauthorized", "credenciais", "credentials", "authentication"

```kotlin
else -> {
    // Verificar se a mensagem contém indicações de erro de autenticação
    val message = throwable.message?.lowercase() ?: ""
    when {
        message.contains("401") || 
        message.contains("unauthorized") || 
        message.contains("credenciais") ||
        message.contains("credentials") ||
        message.contains("authentication") -> {
            context.getString(R.string.error_http_401)
        }
        // ... outros casos
    }
}
```

**b) strings.xml**
- Alterada a mensagem de erro 401

```xml
<!-- ANTES -->
<string name="error_http_401">Credenciais inválidas</string>

<!-- DEPOIS -->
<string name="error_http_401">Usuário ou senha incorretos</string>
```

### Correções Adicionais no Backend

#### MobilePatrimonioService.java
**Problema**: Métodos inexistentes sendo chamados nos DAOs

**Correções**:
1. `verificarPatrimonioColetado()` → substituído por `coletaExiste()`
2. `obterInventarioAtivo()` → substituído por `buscarInventarioPorStatus("EM_ANDAMENTO")`

```java
// ANTES:
inventarioAtivo = inventarioDAO.obterInventarioAtivo();
boolean patrimonioColetado = coletaDAO.verificarPatrimonioColetado(
    patrimonio.getId(), 
    inventarioAtivo.getId()
);

// DEPOIS:
inventarioAtivo = inventarioDAO.buscarInventarioPorStatus("EM_ANDAMENTO");
boolean patrimonioColetado = coletaDAO.coletaExiste(
    inventarioAtivo.getId(), 
    patrimonio.getId()
);
```

### Impacto das Mudanças

#### Experiência do Usuário
- ✅ Mensagens mais claras e diretas
- ✅ Feedback mais preciso sobre erros de autenticação
- ✅ Redução de confusão ao visualizar patrimônios coletados

#### Manutenibilidade
- ✅ Código mais robusto para tratamento de erros
- ✅ Melhor detecção de diferentes tipos de erro de autenticação
- ✅ Uso correto dos métodos existentes nos DAOs

### Testes Recomendados

1. **Teste de Login com Credenciais Incorretas**
   - Entrar com usuário válido e senha errada
   - Verificar se aparece "Usuário ou senha incorretos"

2. **Teste de Coleta de Patrimônio**
   - Escanear um QR Code
   - Coletar o patrimônio
   - Verificar se aparece "Status: COLETADO"
   - Escanear novamente o mesmo patrimônio
   - Verificar se o status permanece "Status: COLETADO"

3. **Teste de Erros de Rede**
   - Desconectar do servidor
   - Tentar fazer login
   - Verificar se a mensagem de erro é apropriada

### Arquivos Modificados

```
InventarioMobile/app/src/main/java/com/inventario/mobile/
├── presentation/scanner/ScannerActivity.kt (linha 296)
├── utils/ErrorMapper.kt (linhas 17-45)
└── res/values/strings.xml (linha 42)

src/main/java/com/inventario/mobile/server/service/
└── MobilePatrimonioService.java (linhas 172, 180-186)
```

### Notas Técnicas

- As mudanças são retrocompatíveis
- Não há impacto em funcionalidades existentes
- Melhorias focadas em UX e clareza de mensagens
- Correções de bugs no backend para usar métodos corretos dos DAOs
