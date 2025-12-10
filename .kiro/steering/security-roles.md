# Segurança por Roles - API Mobile

## 🔐 Visão Geral

O sistema implementa controle de acesso baseado em roles (RBAC) usando Spring Security com anotações `@PreAuthorize`.

---

## 👥 Hierarquia de Perfis

| Perfil | Descrição | Nível |
|--------|-----------|-------|
| **ADMIN** | Administrador - Acesso total | 4 (máximo) |
| **SUPERVISOR** | Supervisor - Supervisiona coletas e relatórios | 3 |
| **COLETOR** | Coletor - Pode realizar coletas | 2 |
| **CONSULTA** | Consulta - Apenas visualização | 1 (mínimo) |

---

## 🏷️ Anotações de Segurança

### @RequireAdmin
```java
@PreAuthorize("hasRole('ADMIN')")
```
Apenas administradores podem acessar.

**Uso:**
- Gerenciamento de usuários
- Exclusão de coletas
- Configurações do sistema

### @RequireSupervisor
```java
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
```
Administradores e supervisores podem acessar.

**Uso:**
- Listar todos os inventários
- Relatórios gerenciais
- Supervisão de coletas

### @RequireColetor
```java
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'COLETOR')")
```
Administradores, supervisores e coletores podem acessar.

**Uso:**
- Registrar coletas
- Sincronização de dados
- Buscar descrições pendentes

### @RequireConsulta
```java
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'COLETOR', 'CONSULTA')")
```
Qualquer usuário autenticado pode acessar.

**Uso:**
- Dashboard
- Consulta de patrimônios
- Visualização de estatísticas

---

## 📋 Matriz de Permissões por Controller

### MobileColetaController (`/api/mobile/coletas`)

| Endpoint | Método | Role Mínima | Anotação |
|----------|--------|-------------|----------|
| `/` | POST | COLETOR | @RequireColetor |
| `/batch` | POST | COLETOR | @RequireColetor |
| `/` | GET | CONSULTA | @RequireConsulta |
| `/{id}` | GET | CONSULTA | @RequireConsulta |
| `/historico` | GET | CONSULTA | @RequireConsulta |
| `/pendentes` | GET | COLETOR | @RequireColetor |
| `/{id}` | PUT | COLETOR | @RequireColetor |
| `/{id}` | DELETE | ADMIN | @RequireAdmin |
| `/verificar-duplicata` | POST | COLETOR | @RequireColetor |
| `/descricoes-pendentes` | GET | COLETOR | @RequireColetor |
| `/incremental` | GET | COLETOR | @RequireColetor |

### MobileUsuarioController (`/api/mobile/usuarios`)

| Endpoint | Método | Role Mínima | Anotação |
|----------|--------|-------------|----------|
| `/` | GET | ADMIN | @RequireAdmin |
| `/{id}` | GET | ADMIN | @RequireAdmin |
| `/login/{login}` | GET | ADMIN | @RequireAdmin |
| `/me` | GET | CONSULTA | @RequireConsulta |

### MobileInventarioController (`/api/mobile/inventario`)

| Endpoint | Método | Role Mínima | Anotação |
|----------|--------|-------------|----------|
| `/ativo` | GET | CONSULTA | @RequireConsulta |
| `/{id}` | GET | CONSULTA | @RequireConsulta |
| `/` | GET | SUPERVISOR | @RequireSupervisor |
| `/{id}/estatisticas` | GET | CONSULTA | @RequireConsulta |

### MobileSyncController (`/api/mobile/sync`)

| Endpoint | Método | Role Mínima | Anotação |
|----------|--------|-------------|----------|
| `/patrimonios` | GET | COLETOR | @RequireColetor (classe) |
| `/salas` | GET | COLETOR | @RequireColetor (classe) |
| `/stats` | GET | COLETOR | @RequireColetor (classe) |

### MobileDashboardController (`/api/mobile/dashboard`)

| Endpoint | Método | Role Mínima | Anotação |
|----------|--------|-------------|----------|
| Todos | GET | CONSULTA | @RequireConsulta (classe) |

### MobilePatrimonioController (`/api/mobile/patrimonio`)

| Endpoint | Método | Role Mínima | Anotação |
|----------|--------|-------------|----------|
| Todos | GET | CONSULTA | @RequireConsulta (classe) |

### MobileSalaController (`/api/mobile/salas`)

| Endpoint | Método | Role Mínima | Anotação |
|----------|--------|-------------|----------|
| Todos | GET | CONSULTA | @RequireConsulta (classe) |

### MobileSetorController (`/api/mobile/setores`)

| Endpoint | Método | Role Mínima | Anotação |
|----------|--------|-------------|----------|
| Todos | GET | CONSULTA | @RequireConsulta (classe) |

### MobileResponsavelController (`/api/mobile/responsaveis`)

| Endpoint | Método | Role Mínima | Anotação |
|----------|--------|-------------|----------|
| Todos | GET | CONSULTA | @RequireConsulta (classe) |

### MobileDescricaoController (`/api/mobile/descricoes`)

| Endpoint | Método | Role Mínima | Anotação |
|----------|--------|-------------|----------|
| Todos | GET | COLETOR | @RequireColetor (classe) |

---

## 🔧 Endpoints Públicos (Sem Autenticação)

```
/api/mobile/health
/api/mobile/test/**
/api/mobile/auth/login
/api/mobile/auth/validate
/api/mobile/v1/connection/**
```

---

## 🚀 Como Usar as Anotações

### Em Método Específico
```java
@GetMapping("/{id}")
@RequireAdmin
public ResponseEntity<...> buscarPorId(@PathVariable Integer id) {
    // Apenas ADMIN pode acessar
}
```

### Em Classe Inteira
```java
@RestController
@RequestMapping("/api/mobile/sync")
@RequireColetor // Todos os métodos requerem COLETOR ou superior
public class MobileSyncController {
    // ...
}
```

### Combinando Anotações
```java
@RestController
@RequestMapping("/api/mobile/usuarios")
@RequireConsulta // Padrão para a classe
public class MobileUsuarioController {
    
    @GetMapping("/me")
    // Usa @RequireConsulta da classe
    public ResponseEntity<...> obterPerfil() { }
    
    @GetMapping
    @RequireAdmin // Sobrescreve para este método
    public ResponseEntity<...> listarTodos() { }
}
```

---

## ⚠️ Tratamento de Erros

Quando um usuário não tem permissão:

```json
{
  "timestamp": "2025-12-09T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/mobile/usuarios"
}
```

---

## 📝 Boas Práticas

1. **Princípio do Menor Privilégio**: Use a role mais restritiva possível
2. **Anotação na Classe**: Use quando todos os endpoints têm a mesma restrição
3. **Anotação no Método**: Use para exceções ou controle fino
4. **Documentação**: Sempre documente a role necessária no Javadoc

---

**Última atualização:** 09/12/2025
**Versão:** 2.0.0
**Status:** ✅ Implementado
