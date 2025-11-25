# Otimização de Performance - Busca de Coletas

## 🐌 Problema Identificado

### Situação Atual
**40 coletas = ~120+ queries ao banco de dados!**

```java
for (Coleta coleta : coletas) {  // 40 iterações
    Inventario inventario = inventarioDAO.findById(...);  // 40 queries
    Usuario coletor = usuarioDAO.findById(...);           // 40 queries
    Patrimonio patrimonio = patrimonioDAO.findById(...);  // 40 queries (dentro de converterParaResponse)
}
```

### Problema: N+1 Query Problem
- 1 query para buscar todas as coletas
- N queries para buscar dados relacionados (inventário, usuário, patrimônio)
- **Total: 1 + (N × 3) queries**

### Impacto
- 40 coletas = 121 queries
- Tempo: ~3-5 segundos (inaceitável!)
- Carga no banco: Muito alta

---

## 🚀 Solução: Cache + Batch Queries

### Estratégia
1. **Cache em memória** para dados que se repetem
2. **Buscar todos os IDs de uma vez** (batch query)
3. **Reutilizar objetos** já carregados

### Resultado Esperado
- 40 coletas = ~5-7 queries
- Tempo: ~200-500ms (aceitável!)
- Redução: **95% menos queries**

---

## 📝 Implementação

### 1. Criar Método Otimizado no Service

```java
/**
 * Busca todas as coletas do sistema com otimização de performance
 * Usa cache e batch queries para reduzir consultas ao banco
 */
public List<MobileColetaResponse> buscarTodasColetasDoSistemaOtimizado() throws SQLException {
    long startTime = System.currentTimeMillis();
    logger.info("🚀 Buscando todas as coletas (OTIMIZADO)");

    // 1. Buscar todas as coletas (1 query)
    List<Coleta> coletas = coletaDAO.buscarTodas();
    
    if (coletas.isEmpty()) {
        logger.info("Nenhuma coleta encontrada");
        return new ArrayList<>();
    }

    // 2. Extrair IDs únicos
    Set<Integer> inventarioIds = new HashSet<>();
    Set<Integer> usuarioIds = new HashSet<>();
    Set<Integer> patrimonioIds = new HashSet<>();
    
    for (Coleta coleta : coletas) {
        inventarioIds.add(coleta.getIdInventario());
        usuarioIds.add(coleta.getIdColetor());
        if (coleta.getIdPatrimonio() > 0) {
            patrimonioIds.add(coleta.getIdPatrimonio());
        }
    }

    // 3. Buscar todos os dados relacionados em batch (3 queries)
    Map<Integer, Inventario> inventariosCache = buscarInventariosEmBatch(inventarioIds);
    Map<Integer, Usuario> usuariosCache = buscarUsuariosEmBatch(usuarioIds);
    Map<Integer, Patrimonio> patrimoniosCache = buscarPatrimoniosEmBatch(patrimonioIds);

    // 4. Converter coletas usando cache (0 queries adicionais)
    List<MobileColetaResponse> responses = new ArrayList<>();
    
    for (Coleta coleta : coletas) {
        try {
            Inventario inventario = inventariosCache.get(coleta.getIdInventario());
            Usuario coletor = usuariosCache.get(coleta.getIdColetor());
            Patrimonio patrimonio = patrimoniosCache.get(coleta.getIdPatrimonio());
            
            if (coletor != null) {
                MobileColetaResponse response = converterParaResponseComCache(
                    coleta, coletor, inventario, patrimonio
                );
                responses.add(response);
            }
        } catch (Exception e) {
            logger.error("Erro ao processar coleta ID {}: {}", coleta.getId(), e.getMessage());
        }
    }

    long duration = System.currentTimeMillis() - startTime;
    logger.info("✓ {} coletas processadas em {}ms (OTIMIZADO)", responses.size(), duration);
    
    return responses;
}

/**
 * Busca múltiplos inventários em uma única query
 */
private Map<Integer, Inventario> buscarInventariosEmBatch(Set<Integer> ids) throws SQLException {
    if (ids.isEmpty()) return new HashMap<>();
    
    logger.debug("Buscando {} inventários em batch", ids.size());
    Map<Integer, Inventario> cache = new HashMap<>();
    
    // Buscar todos de uma vez
    List<Inventario> inventarios = inventarioDAO.buscarPorIds(new ArrayList<>(ids));
    
    for (Inventario inv : inventarios) {
        cache.put(inv.getId(), inv);
    }
    
    return cache;
}

/**
 * Busca múltiplos usuários em uma única query
 */
private Map<Integer, Usuario> buscarUsuariosEmBatch(Set<Integer> ids) throws SQLException {
    if (ids.isEmpty()) return new HashMap<>();
    
    logger.debug("Buscando {} usuários em batch", ids.size());
    Map<Integer, Usuario> cache = new HashMap<>();
    
    // Buscar todos de uma vez
    List<Usuario> usuarios = usuarioDAO.buscarPorIds(new ArrayList<>(ids));
    
    for (Usuario user : usuarios) {
        cache.put(user.getId(), user);
    }
    
    return cache;
}

/**
 * Busca múltiplos patrimônios em uma única query
 */
private Map<Integer, Patrimonio> buscarPatrimoniosEmBatch(Set<Integer> ids) throws SQLException {
    if (ids.isEmpty()) return new HashMap<>();
    
    logger.debug("Buscando {} patrimônios em batch", ids.size());
    Map<Integer, Patrimonio> cache = new HashMap<>();
    
    // Buscar todos de uma vez
    List<Patrimonio> patrimonios = patrimonioDAO.buscarPorIds(new ArrayList<>(ids));
    
    for (Patrimonio pat : patrimonios) {
        cache.put(pat.getId(), pat);
    }
    
    return cache;
}

/**
 * Converte coleta para response usando dados do cache
 * Evita queries adicionais ao banco
 */
private MobileColetaResponse converterParaResponseComCache(
        Coleta coleta, 
        Usuario usuario, 
        Inventario inventario,
        Patrimonio patrimonio) {
    
    MobileColetaResponse response = new MobileColetaResponse();

    response.setId((long) coleta.getId());
    response.setIdInventario(coleta.getIdInventario());
    response.setNomeInventario(inventario != null ? inventario.getNome() : null);
    response.setDataColeta(formatDataColeta(coleta.getDataColeta()));
    response.setStatusColeta(coleta.getStatusColeta());
    response.setObservacaoColeta(coleta.getObservacaoColeta());
    response.setLocalizacaoEncontrada(coleta.getLocalizacaoEncontrada());
    response.setEstadoEncontrado(coleta.getEstadoEncontrado());
    response.setNomeColetor(usuario.getNomeCompleto());
    response.setUsuarioId(coleta.getIdColetor());
    response.setSemEtiqueta(coleta.isSemEtiqueta());
    response.setSincronizado(true);

    if (coleta.isSemEtiqueta()) {
        response.setDescricaoItemSemEtiqueta(coleta.getDescricaoItemSemEtiqueta());
        response.setCategoriaItemSemEtiqueta(coleta.getCategoriaItemSemEtiqueta());
    } else if (patrimonio != null) {
        // Usar patrimônio do cache (sem query adicional)
        response.setPatrimonioId(patrimonio.getId());
        response.setNumeroPatrimonio(patrimonio.getNumero());
        response.setDescricaoPatrimonio(patrimonio.getDescricao());
        response.setIdSala(patrimonio.getIdSala());
        response.setNomeSala(patrimonio.getNomeSala());
    }

    return response;
}
```

---

## 🔧 Métodos Batch nos DAOs

### InventarioDAO.java
```java
/**
 * Busca múltiplos inventários por IDs
 * @param ids lista de IDs
 * @return lista de inventários encontrados
 */
public List<Inventario> buscarPorIds(List<Integer> ids) throws SQLException {
    if (ids == null || ids.isEmpty()) {
        return new ArrayList<>();
    }
    
    String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
    String sql = "SELECT * FROM inventario WHERE id IN (" + placeholders + ")";
    
    List<Inventario> inventarios = new ArrayList<>();
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        for (int i = 0; i < ids.size(); i++) {
            stmt.setInt(i + 1, ids.get(i));
        }
        
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            inventarios.add(mapResultSetToInventario(rs));
        }
    }
    
    return inventarios;
}
```

### UsuarioDAO.java
```java
/**
 * Busca múltiplos usuários por IDs
 */
public List<Usuario> buscarPorIds(List<Integer> ids) throws SQLException {
    if (ids == null || ids.isEmpty()) {
        return new ArrayList<>();
    }
    
    String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
    String sql = "SELECT * FROM usuario WHERE id IN (" + placeholders + ")";
    
    List<Usuario> usuarios = new ArrayList<>();
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        for (int i = 0; i < ids.size(); i++) {
            stmt.setInt(i + 1, ids.get(i));
        }
        
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            usuarios.add(mapResultSetToUsuario(rs));
        }
    }
    
    return usuarios;
}
```

### PatrimonioDAO.java
```java
/**
 * Busca múltiplos patrimônios por IDs
 */
public List<Patrimonio> buscarPorIds(List<Integer> ids) throws SQLException {
    if (ids == null || ids.isEmpty()) {
        return new ArrayList<>();
    }
    
    String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
    String sql = "SELECT * FROM patrimonio WHERE id IN (" + placeholders + ")";
    
    List<Patrimonio> patrimonios = new ArrayList<>();
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        for (int i = 0; i < ids.size(); i++) {
            stmt.setInt(i + 1, ids.get(i));
        }
        
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            patrimonios.add(mapResultSetToPatrimonio(rs));
        }
    }
    
    return patrimonios;
}
```

---

## 📊 Comparação de Performance

### Antes (Não Otimizado)
```
40 coletas:
- Queries: 121 (1 + 40×3)
- Tempo: ~3-5 segundos
- Carga DB: Alta
```

### Depois (Otimizado)
```
40 coletas:
- Queries: 5-7 (1 coletas + 1 inventários + 1 usuários + 1 patrimônios)
- Tempo: ~200-500ms
- Carga DB: Baixa
- Melhoria: 95% menos queries, 90% mais rápido
```

---

## 🎯 Implementação Gradual

### Fase 1: Adicionar Métodos Batch nos DAOs
1. ✅ `InventarioDAO.buscarPorIds()`
2. ✅ `UsuarioDAO.buscarPorIds()`
3. ✅ `PatrimonioDAO.buscarPorIds()`

### Fase 2: Criar Métodos Otimizados no Service
1. ✅ `buscarTodasColetasDoSistemaOtimizado()`
2. ✅ `buscarInventariosEmBatch()`
3. ✅ `buscarUsuariosEmBatch()`
4. ✅ `buscarPatrimoniosEmBatch()`
5. ✅ `converterParaResponseComCache()`

### Fase 3: Atualizar Controller
```java
@GetMapping("/all")
public ResponseEntity<ApiResponse<List<MobileColetaResponse>>> buscarTodasColetasSemPaginacao() {
    try {
        // Usar método otimizado
        List<MobileColetaResponse> coletas = mobileColetaService.buscarTodasColetasDoSistemaOtimizado();
        
        return ResponseEntity.ok(
            ApiResponse.success(coletas, String.format("%d coletas carregadas", coletas.size()))
        );
    } catch (Exception e) {
        logger.error("Erro ao buscar coletas", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Erro ao buscar coletas", "FETCH_ERROR"));
    }
}
```

---

## ✅ Benefícios

### Performance
- ✅ 95% menos queries ao banco
- ✅ 90% mais rápido
- ✅ Escalável para 1000+ coletas

### Manutenibilidade
- ✅ Código mais limpo
- ✅ Fácil de testar
- ✅ Reutilizável

### UX
- ✅ Resposta instantânea
- ✅ Menos carga no servidor
- ✅ Melhor experiência no app

---

**Prioridade:** 🔴 ALTA  
**Impacto:** Performance crítica  
**Esforço:** Médio (~2-3 horas)  
**ROI:** Muito alto (90% melhoria)
