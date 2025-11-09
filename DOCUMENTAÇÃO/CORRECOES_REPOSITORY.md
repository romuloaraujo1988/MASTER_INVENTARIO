# Correções no ColetaRepositoryImpl

## 🔧 Ajustes Realizados

### Problema Identificado
O `ColetaRepositoryImpl` estava chamando métodos que não existiam no `ColetaDAO`. Os métodos do DAO têm nomes diferentes dos padrões Spring Data.

### Mapeamento de Métodos

| Método Repository | Método DAO Correto |
|-------------------|-------------------|
| `findById()` | `buscarPorId()` ✅ |
| `findAll()` | `listarTodas()` ✅ |
| `findByInventario()` | `buscarPorInventario()` ✅ |
| `findBySala()` | `buscarColetasPorSala()` ✅ |
| `findByUsuario()` | `buscarPorColetor()` ✅ |
| `save()` → insert | `inserirColeta()` ✅ |
| `save()` → update | `atualizarColeta()` ✅ |
| `delete()` | `excluirColeta()` ✅ |
| `countByInventario()` | `contarColetasPorInventario()` ✅ |

### Métodos Especiais Implementados

#### 1. `findByNumeroPatrimonio()`
Como o DAO não tem método direto, implementamos:
```java
@Override
public Optional<Coleta> findByNumeroPatrimonio(String numeroPatrimonio) {
    try {
        // 1. Buscar ID do patrimônio pelo número
        int idPatrimonio = buscarIdPatrimonioPorNumero(numeroPatrimonio);
        
        // 2. Buscar coletas deste patrimônio
        List<Coleta> coletas = coletaDAO.buscarPorPatrimonio(idPatrimonio);
        
        // 3. Retornar primeira coleta ou empty
        return coletas.isEmpty() ? Optional.empty() : Optional.of(coletas.get(0));
    } catch (SQLException e) {
        logger.error("Erro", e);
        return Optional.empty();
    }
}
```

#### 2. `existsByNumeroPatrimonio()`
Implementação similar:
```java
@Override
public boolean existsByNumeroPatrimonio(String numeroPatrimonio) {
    try {
        int idPatrimonio = buscarIdPatrimonioPorNumero(numeroPatrimonio);
        if (idPatrimonio > 0) {
            List<Coleta> coletas = coletaDAO.buscarPorPatrimonio(idPatrimonio);
            return !coletas.isEmpty();
        }
        return false;
    } catch (SQLException e) {
        logger.error("Erro", e);
        return false;
    }
}
```

#### 3. Método Auxiliar
```java
private int buscarIdPatrimonioPorNumero(String numeroPatrimonio) throws SQLException {
    String sql = "SELECT ID FROM TABELA_PATRIMONIO WHERE NUMERO = ?";
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, numeroPatrimonio);
        
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("ID");
            }
        }
    }
    
    return 0;
}
```

### Imports Adicionados
```java
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
```

---

## ✅ Status Atual

### ColetaRepositoryImpl
- ✅ Todos os métodos corrigidos
- ✅ Usa métodos corretos do DAO
- ✅ Métodos auxiliares implementados
- ✅ Imports completos
- ✅ Logs apropriados
- ✅ Tratamento de exceções

### Próximos Passos
1. Adicionar `@Component` no `ColetaDAO`
2. Refatorar `ColetaService` para usar `ColetaRepository`
3. Testar integração

---

## 📋 Checklist para Outros Repositories

Ao criar novos Repositories, verificar:

- [ ] Nomes dos métodos do DAO (podem não seguir padrão Spring)
- [ ] Métodos que precisam de implementação especial
- [ ] Imports necessários (Connection, PreparedStatement, etc)
- [ ] Logs em todos os métodos
- [ ] Tratamento de exceções (Optional.empty() ou Collections.emptyList())
- [ ] Lançar RepositoryException em operações de escrita

---

## 🎯 Padrão Estabelecido

### Para Operações de Leitura (SELECT)
```java
@Override
public Optional<Entidade> findById(Integer id) {
    try {
        logger.debug("Buscando...");
        Entidade entidade = dao.metodoDoDAO(id);
        return Optional.ofNullable(entidade);
    } catch (SQLException e) {
        logger.error("Erro", e);
        return Optional.empty();  // ✅ Nunca null
    }
}

@Override
public List<Entidade> findAll() {
    try {
        logger.debug("Listando...");
        return dao.metodoDoDAO();
    } catch (SQLException e) {
        logger.error("Erro", e);
        return Collections.emptyList();  // ✅ Nunca null
    }
}
```

### Para Operações de Escrita (INSERT/UPDATE/DELETE)
```java
@Override
public Entidade save(Entidade entidade) {
    try {
        if (entidade.getId() != null && entidade.getId() > 0) {
            logger.info("Atualizando...");
            dao.metodoUpdate(entidade);
        } else {
            logger.info("Inserindo...");
            dao.metodoInsert(entidade);
        }
        return entidade;
    } catch (SQLException e) {
        logger.error("Erro", e);
        throw new RepositoryException("Erro ao salvar", e);  // ✅ Lançar exceção
    }
}
```

---

**Versão**: 1.0.0  
**Data**: 09/11/2025  
**Status**: ✅ Correções Aplicadas
