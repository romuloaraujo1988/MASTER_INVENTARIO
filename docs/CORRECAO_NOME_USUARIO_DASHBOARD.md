# Correção: Nome do Usuário no Dashboard

## Data: 05/11/2025

## Problema
O nome do usuário parou de aparecer na tela inicial (Dashboard) do app Android após as últimas atualizações.

## Causa
O método `PreferencesManager.saveUserData()` não estava salvando o nome e perfil do usuário nas preferências compartilhadas. Ele apenas marcava o usuário como logado (`user_logged_in = true`), mas não persistia os dados necessários para exibição.

## Solução

### PreferencesManager.kt
Atualizado o método `saveUserData()` para extrair e salvar o nome e perfil do usuário:

```kotlin
fun saveUserData(usuario: Any) {
    // Implementação simplificada - pode ser expandida conforme necessário
    putBoolean("user_logged_in", true)
    
    // Salvar nome e perfil do usuário
    try {
        val usuarioClass = usuario::class.java
        val nomeField = usuarioClass.getDeclaredField("nome")
        nomeField.isAccessible = true
        val nome = nomeField.get(usuario) as? String
        if (nome != null) {
            putString("user_name", nome)
        }
        
        val perfilField = usuarioClass.getDeclaredField("perfil")
        perfilField.isAccessible = true
        val perfil = perfilField.get(usuario) as? String
        if (perfil != null) {
            putString("user_profile", perfil)
        }
    } catch (e: Exception) {
        android.util.Log.e("PreferencesManager", "Erro ao salvar dados do usuário", e)
    }
}
```

## Fluxo de Dados

```
1. Usuário faz login
2. LoginViewModel recebe resposta com dados do usuário
3. PreferencesManager.saveUserData() é chamado
4. Método extrai campos "nome" e "perfil" via reflection
5. Salva em SharedPreferences:
   - "user_name" → Nome do usuário
   - "user_profile" → Perfil (Admin, Coletor, etc.)
6. DashboardViewModel carrega os dados:
   - getUserName() → Retorna nome salvo
   - getUserProfile() → Retorna perfil salvo
7. DashboardFragment exibe na UI
```

## Campos Salvos

| Chave | Valor | Exemplo |
|-------|-------|---------|
| `user_logged_in` | Boolean | `true` |
| `user_name` | String | "João Silva Santos" |
| `user_profile` | String | "ADMIN" ou "COLETOR" |

## Exibição no Dashboard

O DashboardFragment exibe:
```
┌─────────────────────────────┐
│ Bem-vindo!                  │
│ João Silva Santos           │ ← user_name
│ ADMIN                       │ ← user_profile
└─────────────────────────────┘
```

## Arquivos Modificados

```
InventarioMobile/app/src/main/java/com/inventario/mobile/utils/
└── PreferencesManager.kt (MODIFICADO)
```

## Testes

- ✅ Login salva nome e perfil
- ✅ Dashboard carrega e exibe nome
- ✅ Dashboard carrega e exibe perfil
- ✅ Dados persistem após fechar app
- ✅ Reflection funciona corretamente

## Observações

### Uso de Reflection
A solução usa reflection para extrair os campos do objeto `usuario` porque o método recebe `Any` como parâmetro. Isso mantém a flexibilidade do código mas adiciona um pequeno overhead.

### Alternativa Futura
Para melhor performance e type-safety, considerar:
```kotlin
fun saveUserData(nome: String, perfil: String) {
    putBoolean("user_logged_in", true)
    putString("user_name", nome)
    putString("user_profile", perfil)
}
```

### Tratamento de Erros
O código tem try-catch para evitar crashes caso:
- O objeto não tenha os campos esperados
- Os campos sejam de tipo diferente
- Reflection falhe por qualquer motivo

## Status
✅ **CORRIGIDO E TESTADO**

O nome do usuário agora aparece corretamente no Dashboard após o login.
