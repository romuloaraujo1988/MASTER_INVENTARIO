# Exemplo Prático de Refatoração

## 🎯 Refatorando ColetaService

Vou mostrar passo a passo como refatorar um Service existente.

---

## ❌ ANTES - Código Atual

```java
package com.inventario.service;

import com.inventario.dao.ColetaDAO;
import com.inventario.dao.SalaInventarioDAO;
import com.inventario.model.Coleta;

import java.sql.SQLException;
import java.util.List;

public class ColetaService {
    
    private ColetaDAO coletaDAO;
    private SalaInventarioDAO salaInventarioDAO;
    
    // ❌ Problema 1: Instanciação manual
    public ColetaService() {
        this.coletaDAO = new ColetaDAO();
        this.salaInventarioDAO = new SalaInventarioDAO();
    }
    
    // ❌ Problema 2: Exceções não tratadas adequadamente
    public void registrarColeta(Coleta coleta) throws SQLException {
        // ❌ Problema 3: Validação misturada com lógica
        if (coleta.getNumeroPatrimonio() == null) {
            throw new IllegalArgumentException("Número do patrimônio é obrigatório");
        }
        
        coletaDAO.insert(coleta);
    }
    
    // ❌ Problema 4: Retorna null em caso de erro
    public List<Coleta> listarColetas() {
        try {
            return coletaDAO.findAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;  // ❌ Ruim!
        }
    }
}
```

**Problemas**:
1. ❌ Instanciação manual de DAOs
2. ❌ Exceções não tratadas
3. ❌ Validações misturadas
4. ❌ Retorna null
5. ❌ Sem logs adequados
6. ❌ Não usa Spring DI

---

## ✅ DEPOIS - Código Refatorado

### Passo 1: Criar Repository Interface

```java
package com.inventario.repository;

import com.inventario.model.Coleta;
import java.util.List;
import java.util.Optional;

public interface ColetaRepository {
    void save(Coleta coleta);
    Optional<Coleta> findById(Integer id);
    List<Coleta> findAll();
    List<Coleta> findByInventario(Integer idInventario);
    void delete(Integer id);
}
```

### Passo 2: Implementar Repository

```java
package com.inventario.repository.impl;

import com.inventario.dao.ColetaDAO;
import com.inventario.model.Coleta;
import com.inventario.repository.ColetaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class ColetaRepositoryImpl implements ColetaRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(ColetaRepositoryImpl.class);
    
    private final ColetaDAO coletaDAO;
    
    @Autowired
    public ColetaRepositoryImpl(ColetaDAO coletaDAO) {
        this.coletaDAO = coletaDAO;
    }
    
    @Override
    public void save(Coleta coleta) {
        try {
            if (coleta.getId() != null && coleta.getId() > 0) {
                coletaDAO.update(coleta);
                logger.info("Coleta atualizada: {}", coleta.getId());
            } else {
                coletaDAO.insert(coleta);
                logger.info("Coleta inserida: {}", coleta.getNumeroPatrimonio());
            }
        } catch (SQLException e) {
            logger.error("Erro ao salvar coleta", e);
            throw new RepositoryException("Erro ao salvar coleta", e);
        }
    }
    
    @Override
    public Optional<Coleta> findById(Integer id) {
        try {
            Coleta coleta = coletaDAO.findById(id);
            return Optional.ofNullable(coleta);
        } catch (SQLException e) {
            logger.error("Erro ao buscar coleta por ID: {}", id, e);
            return Optional.empty();
        }
    }
    
    @Override
    public List<Coleta> findAll() {
        try {
            return coletaDAO.findAll();
        } catch (SQLException e) {
            logger.error("Erro ao listar coletas", e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<Coleta> findByInventario(Integer idInventario) {
        try {
            return coletaDAO.findByInventario(idInventario);
        } catch (SQLException e) {
            logger.error("Erro ao buscar coletas do inventário: {}", idInventario, e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public void delete(Integer id) {
        try {
            coletaDAO.delete(id);
            logger.info("Coleta excluída: {}", id);
        } catch (SQLException e) {
            logger.error("Erro ao excluir coleta: {}", id, e);
            throw new RepositoryException("Erro ao excluir coleta", e);
        }
    }
}
```

### Passo 3: Criar Validator

```java
package com.inventario.validation;

import com.inventario.model.Coleta;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ColetaValidator {
    
    public ValidationResult validate(Coleta coleta) {
        List<String> errors = new ArrayList<>();
        
        if (coleta.getNumeroPatrimonio() == null || coleta.getNumeroPatrimonio().isEmpty()) {
            errors.add("Número do patrimônio é obrigatório");
        }
        
        if (coleta.getIdInventario() == null) {
            errors.add("ID do inventário é obrigatório");
        }
        
        if (coleta.getIdSala() == null) {
            errors.add("ID da sala é obrigatório");
        }
        
        return errors.isEmpty() 
            ? ValidationResult.success() 
            : ValidationResult.failure(errors);
    }
}
```

### Passo 4: Refatorar Service

```java
package com.inventario.service;

import com.inventario.model.Coleta;
import com.inventario.repository.ColetaRepository;
import com.inventario.validation.ColetaValidator;
import com.inventario.validation.ValidationException;
import com.inventario.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ColetaService {
    
    private static final Logger logger = LoggerFactory.getLogger(ColetaService.class);
    
    private final ColetaRepository coletaRepository;
    private final ColetaValidator coletaValidator;
    private final ApplicationEventPublisher eventPublisher;
    
    @Autowired
    public ColetaService(
            ColetaRepository coletaRepository,
            ColetaValidator coletaValidator,
            ApplicationEventPublisher eventPublisher) {
        this.coletaRepository = coletaRepository;
        this.coletaValidator = coletaValidator;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * Registra uma nova coleta
     */
    public void registrarColeta(Coleta coleta) {
        logger.info("Registrando coleta: {}", coleta.getNumeroPatrimonio());
        
        // Validar
        ValidationResult validationResult = coletaValidator.validate(coleta);
        if (!validationResult.isValid()) {
            throw new ValidationException(validationResult.getErrors());
        }
        
        // Definir data/hora
        coleta.setDataHoraColeta(LocalDateTime.now());
        
        // Salvar
        coletaRepository.save(coleta);
        
        // Publicar evento
        eventPublisher.publishEvent(new ColetaRegistradaEvent(this, coleta));
        
        logger.info("Coleta registrada com sucesso: {}", coleta.getId());
    }
    
    /**
     * Busca coleta por ID
     */
    public Optional<Coleta> buscarPorId(Integer id) {
        logger.debug("Buscando coleta por ID: {}", id);
        return coletaRepository.findById(id);
    }
    
    /**
     * Lista todas as coletas
     */
    public List<Coleta> listarTodas() {
        logger.debug("Listando todas as coletas");
        return coletaRepository.findAll();
    }
    
    /**
     * Lista coletas de um inventário
     */
    public List<Coleta> listarPorInventario(Integer idInventario) {
        logger.debug("Listando coletas do inventário: {}", idInventario);
        return coletaRepository.findByInventario(idInventario);
    }
    
    /**
     * Exclui uma coleta
     */
    public void excluir(Integer id) {
        logger.info("Excluindo coleta: {}", id);
        
        Optional<Coleta> coleta = buscarPorId(id);
        if (coleta.isEmpty()) {
            throw new IllegalArgumentException("Coleta não encontrada: " + id);
        }
        
        coletaRepository.delete(id);
        
        // Publicar evento
        eventPublisher.publishEvent(new ColetaExcluidaEvent(this, coleta.get()));
        
        logger.info("Coleta excluída com sucesso: {}", id);
    }
}
```

### Passo 5: Atualizar DAO para usar Spring

```java
package com.inventario.dao;

import com.inventario.model.Coleta;
import com.inventario.util.DatabaseConnection;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component  // ✅ Adicionar esta anotação
public class ColetaDAO {
    
    public void insert(Coleta coleta) throws SQLException {
        String sql = "INSERT INTO coleta (...) VALUES (...)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // ... código existente
        }
    }
    
    // ... outros métodos
}
```

---

## 📊 Comparação

| Aspecto | Antes | Depois |
|---------|-------|--------|
| **Instanciação** | Manual (`new`) | Spring DI (`@Autowired`) |
| **Tratamento de Erros** | try-catch básico | Repository + Exceptions customizadas |
| **Validação** | Misturada no Service | Validator separado |
| **Logs** | `printStackTrace()` | SLF4J com níveis |
| **Retorno de Erro** | `null` | `Optional<>` ou Exception |
| **Eventos** | Nenhum | Spring Events |
| **Testabilidade** | Difícil | Fácil (mocks) |
| **Transações** | Manual | `@Transactional` |

---

## 🧪 Testando o Código Refatorado

```java
@SpringBootTest
class ColetaServiceTest {
    
    @MockBean
    private ColetaRepository coletaRepository;
    
    @MockBean
    private ColetaValidator coletaValidator;
    
    @MockBean
    private ApplicationEventPublisher eventPublisher;
    
    @Autowired
    private ColetaService coletaService;
    
    @Test
    void deveRegistrarColetaComSucesso() {
        // Arrange
        Coleta coleta = new Coleta();
        coleta.setNumeroPatrimonio("12345");
        coleta.setIdInventario(1);
        coleta.setIdSala(10);
        
        when(coletaValidator.validate(any())).thenReturn(ValidationResult.success());
        
        // Act
        coletaService.registrarColeta(coleta);
        
        // Assert
        verify(coletaRepository).save(coleta);
        verify(eventPublisher).publishEvent(any(ColetaRegistradaEvent.class));
    }
    
    @Test
    void deveLancarExcecaoQuandoValidacaoFalhar() {
        // Arrange
        Coleta coleta = new Coleta();
        
        when(coletaValidator.validate(any()))
            .thenReturn(ValidationResult.failure("Número é obrigatório"));
        
        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            coletaService.registrarColeta(coleta);
        });
        
        verify(coletaRepository, never()).save(any());
    }
}
```

---

## ✅ Checklist de Refatoração

Para cada Service:

- [ ] Adicionar `@Component` no DAO
- [ ] Criar interface Repository
- [ ] Implementar Repository com `@Repository`
- [ ] Criar Validator com `@Component`
- [ ] Refatorar Service:
  - [ ] Adicionar `@Service`
  - [ ] Usar constructor injection
  - [ ] Usar Repository ao invés de DAO
  - [ ] Usar Validator
  - [ ] Adicionar logs adequados
  - [ ] Publicar eventos
  - [ ] Usar Optional ao invés de null
- [ ] Criar testes unitários
- [ ] Testar integração

---

**Tempo estimado por Service**: 2-3 horas  
**Prioridade**: ALTA (começar pelos Services mais usados)
