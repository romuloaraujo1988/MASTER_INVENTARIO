# 🔄 MIGRAÇÃO PARA DAOs REFATORADOS

**Data**: 2025-11-06  
**Status**: 🔄 EM EXECUÇÃO  
**Objetivo**: Substituir DAOs antigos pelos refatorados em todo o sistema

---

## 📊 ANÁLISE DE IMPACTO

### DAOs a Migrar

| DAO | Instâncias Encontradas | Arquivos Afetados |
|-----|------------------------|-------------------|
| **ResponsavelDAO** | 8 | 7 arquivos |
| **PatrimonioDAO** | 13 | 12 arquivos |
| **UsuarioDAO** | 11 | 10 arquivos |
| **SalaDAO** | 9 | 8 arquivos |
| **TOTAL** | **41** | **37 arquivos** |

---

## 🎯 ESTRATÉGIA DE MIGRAÇÃO

### Fase 1: Configuração Spring (DAOConfiguration)
✅ Atualizar beans para usar classes refatoradas

### Fase 2: Services
✅ Migrar services do mobile API
✅ Migrar services do desktop

### Fase 3: Views/Frames
✅ Migrar frames principais
✅ Migrar dialogs

### Fase 4: Utilitários
✅ Migrar classes de importação
✅ Migrar classes de teste

---

## 📝 ARQUIVOS A MIGRAR

### 1. Configuração Spring (1 arquivo)
- `src/main/java/com/inventario/config/DAOConfiguration.java`

### 2. Mobile Services (4 arquivos)
- `src/main/java/com/inventario/mobile/server/service/MobileResponsavelService.java`
- `src/main/java/com/inventario/mobile/server/service/MobilePatrimonioService.java`
- `src/main/java/com/inventario/mobile/server/service/MobileColetaService.java`
- `src/main/java/com/inventario/mobile/server/service/MobileSalaService.java`
- `src/main/java/com/inventario/mobile/server/service/DashboardService.java`

### 3. Desktop Services (2 arquivos)
- `src/main/java/com/inventario/service/AutenticacaoServiceDB.java`
- `src/main/java/com/inventario/service/DataSyncService.java`

### 4. Views/Frames (8 arquivos)
- `src/main/java/com/inventario/view/ResponsavelFrame.java`
- `src/main/java/com/inventario/view/ResponsavelFormDialog.java`
- `src/main/java/com/inventario/view/PatrimonioFrame.java`
- `src/main/java/com/inventario/view/PatrimonioFormDialog.java`
- `src/main/java/com/inventario/view/UsuarioFrame.java`
- `src/main/java/com/inventario/view/SalaFrame.java`
- `src/main/java/com/inventario/view/ColetaFrame_v2.java`
- `src/main/java/com/inventario/view/RelatorioFrame.java`
- `src/main/java/com/inventario/view/QRCodeFrame.java`
- `src/main/java/com/inventario/view/InventarioFormDialog.java`

### 5. Utilitários (4 arquivos)
- `src/main/java/com/inventario/util/ImportacaoCSV.java`
- `src/main/java/com/inventario/util/ImportacaoExcel.java`
- `src/main/java/com/inventario/util/RecriateAdminUser.java`
- `src/main/java/com/inventario/util/MigrarSenhasParaBCrypt.java`
- `src/main/java/com/inventario/util/AtualizarSenhaAdmin.java`

### 6. Testes (2 arquivos)
- `src/main/java/com/inventario/test/TesteColetaCompleto.java`
- `src/main/java/com/inventario/test/TesteAdminColeta.java`

---

## 🔧 MUDANÇAS NECESSÁRIAS

### Imports
```java
// ANTES
import com.inventario.dao.ResponsavelDAO;
import com.inventario.dao.PatrimonioDAO;
import com.inventario.dao.UsuarioDAO;
import com.inventario.dao.SalaDAO;

// DEPOIS
import com.inventario.dao.ResponsavelDAORefactored;
import com.inventario.dao.PatrimonioDAORefactored;
import com.inventario.dao.UsuarioDAORefactored;
import com.inventario.dao.SalaDAORefactored;
```

### Instanciação
```java
// ANTES
ResponsavelDAO responsavelDAO = new ResponsavelDAO();
PatrimonioDAO patrimonioDAO = new PatrimonioDAO();
UsuarioDAO usuarioDAO = new UsuarioDAO();
SalaDAO salaDAO = new SalaDAO();

// DEPOIS
ResponsavelDAORefactored responsavelDAO = new ResponsavelDAORefactored();
PatrimonioDAORefactored patrimonioDAO = new PatrimonioDAORefactored();
UsuarioDAORefactored usuarioDAO = new UsuarioDAORefactored();
SalaDAORefactored salaDAO = new SalaDAORefactored();
```

### Métodos (Compatibilidade Mantida)
```java
// Os métodos legados ainda funcionam!
responsavelDAO.inserirResponsavel(responsavel);  // ✅ Funciona
responsavelDAO.listarResponsaveis();              // ✅ Funciona
usuarioDAO.buscarUsuarioPorLogin(login);          // ✅ Funciona

// Mas os novos métodos são recomendados:
responsavelDAO.insert(responsavel);               // ✅ Recomendado
responsavelDAO.findAll();                         // ✅ Recomendado
usuarioDAO.buscarPorLogin(login);                 // ✅ Recomendado
```

---

## ⚠️ PONTOS DE ATENÇÃO

### 1. Tratamento de Exceções
```java
// ANTES (métodos retornam boolean)
if (dao.inserirResponsavel(responsavel)) {
    // sucesso
}

// DEPOIS (métodos lançam SQLException)
try {
    dao.insert(responsavel);
    // sucesso
} catch (SQLException e) {
    // erro
}

// OU usar métodos legados (mantém compatibilidade)
if (dao.inserirResponsavel(responsavel)) {
    // sucesso - método legado trata SQLException internamente
}
```

### 2. Soft Delete
```java
// ANTES
dao.excluirResponsavel(id);  // DELETE físico

// DEPOIS
dao.delete(id);  // Soft delete (ATIVO = FALSE)
// OU
dao.excluirResponsavel(id);  // Método legado - soft delete
```

### 3. Métodos Renomeados
```java
// Alguns métodos foram padronizados:
// ANTES                          // DEPOIS
listarResponsaveis()              findAll()
buscarResponsavelPorId(id)        findById(id)
inserirResponsavel(obj)           insert(obj)
atualizarResponsavel(obj)         update(obj)
excluirResponsavel(id)            delete(id)

// Mas métodos legados ainda funcionam!
```

---

## 📋 CHECKLIST DE MIGRAÇÃO

### Configuração
- [ ] DAOConfiguration.java

### Mobile Services
- [ ] MobileResponsavelService.java
- [ ] MobilePatrimonioService.java
- [ ] MobileColetaService.java
- [ ] MobileSalaService.java
- [ ] DashboardService.java

### Desktop Services
- [ ] AutenticacaoServiceDB.java
- [ ] DataSyncService.java

### Views/Frames
- [ ] ResponsavelFrame.java
- [ ] ResponsavelFormDialog.java
- [ ] PatrimonioFrame.java
- [ ] PatrimonioFormDialog.java
- [ ] UsuarioFrame.java
- [ ] SalaFrame.java
- [ ] ColetaFrame_v2.java
- [ ] RelatorioFrame.java
- [ ] QRCodeFrame.java
- [ ] InventarioFormDialog.java

### Utilitários
- [ ] ImportacaoCSV.java
- [ ] ImportacaoExcel.java
- [ ] RecriateAdminUser.java
- [ ] MigrarSenhasParaBCrypt.java
- [ ] AtualizarSenhaAdmin.java

### Testes
- [ ] TesteColetaCompleto.java
- [ ] TesteAdminColeta.java

### Validação
- [ ] Compilar projeto (mvn compile)
- [ ] Executar testes
- [ ] Testar funcionalidades principais
- [ ] Verificar logs

---

## 🚀 EXECUÇÃO

Iniciando migração...

