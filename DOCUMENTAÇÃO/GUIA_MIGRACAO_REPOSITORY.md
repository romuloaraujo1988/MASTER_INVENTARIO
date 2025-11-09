# Guia de Migração para Repository Pattern

## ✅ Implementado

### Repositories Criados
- ✅ `PatrimonioRepository` + `PatrimonioRepositoryImpl`
- ✅ `ColetaRepository` + `ColetaRepositoryImpl`
- ✅ `RepositoryException` (exceção customizada)

### Services Refatorados
- ✅ `PatrimonioService` - Usando Repository Pattern

---

## 📋 Checklist de Migração

Para cada Service que precisa ser migrado:

### 1. Criar Interface Repository

```java
package com.inventario.repository;

import com.inventario.model.SuaEntidade;
import java.util.List;
import java.util.Optional;

public interface SuaEntidadeRepository {
    Optional<SuaEntidade> findById(Integer id);
    List<SuaEntidade> findAll();
    SuaEntidade save(SuaEntidade entidade);
    void delete(Integer id);
    // ... outros métodos específicos
}
```

### 2. Criar Implementação Repository

```java
package com.inventario.repository.impl;

import com.inventario.dao.SuaEntidadeDAO;
import com.inventario.exception.RepositoryException;
import com.inventario.model.SuaEntidade;
import com.inventario.repository.SuaEntidadeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class SuaEntidadeRepositoryImpl implements SuaEntidadeRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(SuaEntidadeRepositoryImpl.class);
    
    private final SuaEntidadeDAO dao;
    
    @Autowired
    public SuaEntidadeRepositoryImpl(SuaEntidadeDAO dao) {
        this.dao = dao;
    }
    
    @Override
    public Optional<SuaEntidade> findById(Integer id) {
        try {
            logger.debug("Buscando entidade por ID: {}", id);
            SuaEntidade entidade = dao.findById(id);
            return Optional.ofNullable(entidade);
        } catch (SQLException e) {
            logger.error("Erro ao buscar entidade por ID: {}", id, e);
            return Optional.empty();
        }
    }
    
    @Override
    public List<SuaEntidade> findAll() {
        try {
            logger.debug("Listando todas as entidades");
            return dao.findAll();
        } catch (SQLException e) {
            logger.error("Erro ao listar entidades", e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public SuaEntidade save(SuaEntidade entidade) {
        try {
            if (entidade.getId() != null && entidade.getId() > 0) {
                logger.info("Atualizando entidade: {}", entidade.getId());
                dao.update(entidade);
            } else {
                logger.info("Inserindo nova entidade");
                dao.insert(entidade);
            }
            return entidade;
        } catch (SQLException e) {
            logger.error("Erro ao salvar entidade", e);
            throw new RepositoryException("Erro ao salvar entidade", e);
        }
    }
    
    @Override
    public void delete(Integer id) {
        try {
            logger.info("Excluindo entidade: {}", id);
            dao.delete(id);
        } catch (SQLException e) {
            logger.error("Erro ao excluir entidade: {}", id, e);
            throw new RepositoryException("Erro ao excluir entidade", e);
        }
    }
}
```

### 3. Adicionar @Component no DAO

```java
@Component  // ← Adicionar esta anotação
public class SuaEntidadeDAO {
    // ... código existente
}
```

### 4. Refatorar Service

#### ANTES:
```java
@Service
public class SuaEntidadeService {
    
    private SuaEntidadeDAO dao;
    
    public SuaEntidadeService() {
        this.dao = new SuaEntidadeDAO();  // ❌ Manual
    }
    
    public SuaEntidade buscarPorId(Integer id) {
        try {
            return dao.findById(id);  // ❌ Retorna null
        } catch (SQLException e) {
            e.printStackTrace();  // ❌ Ruim
            return null;
        }
    }
}
```

#### DEPOIS:
```java
@Service
@Transactional
public class SuaEntidadeService {
    
    private static final Logger logger = LoggerFactory.getLogger(SuaEntidadeService.class);
    
    private final SuaEntidadeRepository repository;
    
    @Autowired
    public SuaEntidadeService(SuaEntidadeRepository repository) {
        this.repository = repository;  // ✅ Injetado
    }
    
    public Optional<SuaEntidade> buscarPorId(Integer id) {
        logger.debug("Buscando entidade por ID: {}", id);
        return repository.findById(id);  // ✅ Retorna Optional
    }
    
    public List<SuaEntidade> listarTodos() {
        logger.debug("Listando todas as entidades");
        return repository.findAll();  // ✅ Nunca retorna null
    }
    
    public SuaEntidade salvar(SuaEntidade entidade) {
        logger.info("Salvando entidade");
        
        // Validações
        if (entidade.getCampoObrigatorio() == null) {
            throw new IllegalArgumentException("Campo obrigatório não informado");
        }
        
        return repository.save(entidade);  // ✅ Exceção tratada no Repository
    }
}
```

---

## 🎯 Services Prioritários para Migração

### Alta Prioridade
1. ✅ `PatrimonioService` - CONCLUÍDO
2. ⏳ `ColetaService` - Repository criado, falta refatorar Service
3. ⏳ `InventarioService`
4. ⏳ `SalaService`
5. ⏳ `UsuarioService`

### Média Prioridade
6. ⏳ `ResponsavelService`
7. ⏳ `SetorService`
8. ⏳ `CampusService`
9. ⏳ `RelatorioService`

### Baixa Prioridade
10. ⏳ `DashboardService`
11. ⏳ `QRCodeService`
12. ⏳ `DispositivoMobileService`

---

## 📊 Padrões a Seguir

### ✅ Boas Práticas

1. **Sempre retornar Optional para buscas únicas**
```java
Optional<Patrimonio> findById(Integer id);
```

2. **Nunca retornar null em listas**
```java
// ✅ BOM
return Collections.emptyList();

// ❌ RUIM
return null;
```

3. **Usar logs apropriados**
```java
logger.debug("Operação de leitura");  // Para consultas
logger.info("Operação de escrita");   // Para insert/update/delete
logger.error("Erro", exception);      // Para erros
```

4. **Lançar RepositoryException em operações de escrita**
```java
try {
    dao.insert(entidade);
} catch (SQLException e) {
    throw new RepositoryException("Erro ao inserir", e);
}
```

5. **Retornar Collections.emptyList() em operações de leitura**
```java
try {
    return dao.findAll();
} catch (SQLException e) {
    logger.error("Erro", e);
    return Collections.emptyList();  // ✅ Nunca null
}
```

6. **Usar constructor injection**
```java
@Autowired
public SuaEntidadeService(SuaEntidadeRepository repository) {
    this.repository = repository;
}
```

---

## 🧪 Como Testar

### 1. Teste Manual

```java
// No main ou em um teste
@SpringBootTest
public class PatrimonioServiceTest {
    
    @Autowired
    private PatrimonioService patrimonioService;
    
    @Test
    public void testBuscarPorId() {
        Optional<Patrimonio> patrimonio = patrimonioService.buscarPorId(1);
        
        if (patrimonio.isPresent()) {
            System.out.println("Patrimônio encontrado: " + patrimonio.get().getNumero());
        } else {
            System.out.println("Patrimônio não encontrado");
        }
    }
    
    @Test
    public void testListarTodos() {
        List<Patrimonio> patrimonios = patrimonioService.listarTodos();
        System.out.println("Total de patrimônios: " + patrimonios.size());
    }
}
```

### 2. Verificar Logs

Após executar, verificar se os logs aparecem corretamente:

```
DEBUG - Buscando patrimônio por ID: 1
INFO  - Salvando patrimônio: 12345
ERROR - Erro ao buscar patrimônio por ID: 999
```

---

## ⚠️ Problemas Comuns

### Problema 1: DAO não é injetado

**Erro**: `No qualifying bean of type 'ColetaDAO'`

**Solução**: Adicionar `@Component` no DAO
```java
@Component
public class ColetaDAO {
    // ...
}
```

### Problema 2: Circular dependency

**Erro**: `The dependencies of some of the beans in the application context form a cycle`

**Solução**: Revisar injeções e usar `@Lazy` se necessário
```java
@Autowired
public SuaEntidadeService(@Lazy SuaEntidadeRepository repository) {
    this.repository = repository;
}
```

### Problema 3: SQLException não tratada

**Erro**: Código antigo ainda lança SQLException

**Solução**: Repository já trata, remover throws do Service
```java
// ❌ ANTES
public void salvar(Patrimonio p) throws SQLException {

// ✅ DEPOIS
public Patrimonio salvar(Patrimonio p) {
```

---

## 📈 Progresso da Migração

| Service | Repository | DAO @Component | Service Refatorado | Status |
|---------|-----------|----------------|-------------------|--------|
| PatrimonioService | ✅ | ✅ | ✅ | ✅ COMPLETO |
| ColetaService | ✅ | ⏳ | ⏳ | 🔄 EM PROGRESSO |
| InventarioService | ⏳ | ⏳ | ⏳ | ⏳ PENDENTE |
| SalaService | ⏳ | ⏳ | ⏳ | ⏳ PENDENTE |
| UsuarioService | ⏳ | ⏳ | ⏳ | ⏳ PENDENTE |
| ResponsavelService | ⏳ | ⏳ | ⏳ | ⏳ PENDENTE |
| SetorService | ⏳ | ⏳ | ⏳ | ⏳ PENDENTE |
| CampusService | ⏳ | ⏳ | ⏳ | ⏳ PENDENTE |
| RelatorioService | ⏳ | ⏳ | ⏳ | ⏳ PENDENTE |
| DashboardService | ⏳ | ⏳ | ⏳ | ⏳ PENDENTE |
| QRCodeService | ⏳ | ⏳ | ⏳ | ⏳ PENDENTE |
| DispositivoMobileService | ⏳ | ⏳ | ⏳ | ⏳ PENDENTE |

---

## 🎯 Próximos Passos

1. Adicionar `@Component` no `ColetaDAO`
2. Refatorar `ColetaService` para usar `ColetaRepository`
3. Testar `ColetaService` refatorado
4. Repetir processo para próximo Service

---

**Versão**: 1.0.0  
**Data**: 09/11/2025  
**Autor**: Sistema de Inventário
