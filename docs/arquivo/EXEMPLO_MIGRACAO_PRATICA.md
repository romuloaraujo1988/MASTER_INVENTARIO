# 🚀 Exemplo Prático de Migração

## 📋 Cenário: Migrar SetorFrame para Usar SetorDAORefactored

Este é um exemplo prático de como migrar uma classe para usar as versões refatoradas.

---

## 🎯 Passo a Passo

### **PASSO 1: Identificar a Classe Original**

Vamos supor que existe um `SetorFrame.java` que usa `SetorDAO`:

```java
// SetorFrame.java (ANTES)
package com.inventario.view;

import com.inventario.dao.SetorDAO;
import com.inventario.model.Setor;
import javax.swing.*;
import java.util.List;

public class SetorFrame extends JFrame {
    
    private SetorDAO setorDAO;  // ❌ Versão antiga
    
    public SetorFrame() {
        this.setorDAO = new SetorDAO();  // ❌ Versão antiga
        initComponents();
    }
    
    private void carregarSetores() {
        try {
            List<Setor> setores = setorDAO.listarSetores();  // ❌ Método antigo
            // ... processar setores
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,   // ❌ JOptionPane direto
                "Erro ao carregar setores: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void salvarSetor(Setor setor) {
        // Validar
        if (setor.getNome() == null || setor.getNome().trim().isEmpty()) {  // ❌ Validação manual
            JOptionPane.showMessageDialog(this,
                "Nome é obrigatório",
                "Erro de Validação",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            setorDAO.inserirSetor(setor);  // ❌ Método antigo
            JOptionPane.showMessageDialog(this,  // ❌ JOptionPane direto
                "Setor salvo com sucesso!",
                "Sucesso",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erro ao salvar setor: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
```

---

### **PASSO 2: Criar Versão Refatorada**

```java
// SetorFrameRefactored.java (DEPOIS)
package com.inventario.view;

import com.inventario.dao.SetorDAORefactored;  // ✅ Versão refatorada
import com.inventario.model.Setor;
import com.inventario.util.*;  // ✅ Classes utilitárias
import javax.swing.*;
import java.util.List;

public class SetorFrameRefactored extends JFrame {
    
    private SetorDAORefactored setorDAO;  // ✅ Versão refatorada
    
    public SetorFrameRefactored() {
        this.setorDAO = new SetorDAORefactored();  // ✅ Versão refatorada
        initComponents();
    }
    
    private void carregarSetores() {
        // ✅ Usando ExceptionHandler
        ExceptionHandler.executeWithErrorHandling(this, "carregar setores", () -> {
            List<Setor> setores = setorDAO.findAll();  // ✅ Método do BaseDAO
            // ... processar setores
        });
    }
    
    private void salvarSetor(Setor setor) {
        // ✅ Usando ValidationUtils
        ValidationUtils.ValidationResult result = ValidationUtils.validateRequired(
            setor.getNome(), "Nome"
        );
        
        if (!result.isValid()) {
            ExceptionHandler.handleValidation(this, result.getErrorMessage());
            return;
        }
        
        // ✅ Usando ExceptionHandler
        ExceptionHandler.executeWithErrorHandling(this, "salvar setor", () -> {
            setorDAO.insert(setor);  // ✅ Método do BaseDAO
            DialogUtils.showSuccess(this, "Setor salvo com sucesso!");  // ✅ DialogUtils
        });
    }
}
```

---

### **PASSO 3: Comparação das Mudanças**

| Aspecto | Antes | Depois | Benefício |
|---------|-------|--------|-----------|
| **Import DAO** | `SetorDAO` | `SetorDAORefactored` | BaseDAO com CRUD |
| **Imports Utils** | Nenhum | `com.inventario.util.*` | Classes utilitárias |
| **Criar DAO** | `new SetorDAO()` | `new SetorDAORefactored()` | Pool de conexões |
| **Listar** | `listarSetores()` | `findAll()` | Método padronizado |
| **Inserir** | `inserirSetor()` | `insert()` | Método padronizado |
| **Validação** | Manual (5 linhas) | `ValidationUtils` (2 linhas) | -60% código |
| **Erro** | `JOptionPane` (4 linhas) | `ExceptionHandler` (1 linha) | -75% código |
| **Sucesso** | `JOptionPane` (4 linhas) | `DialogUtils` (1 linha) | -75% código |

---

### **PASSO 4: Testar a Migração**

```bash
# 1. Compilar
mvn clean compile

# 2. Verificar erros
# Se houver erros, corrigir antes de prosseguir

# 3. Executar aplicação
mvn exec:java -Dexec.mainClass="com.inventario.SistemaInventarioApplication"

# 4. Testar funcionalidades:
#    - Listar setores
#    - Criar novo setor
#    - Editar setor
#    - Excluir setor
#    - Buscar setor

# 5. Verificar logs
tail -f logs/sistema-inventario.log
```

---

## 🔧 Exemplo 2: Migrar Service

### **ANTES: SetorService.java**

```java
package com.inventario.service;

import com.inventario.dao.SetorDAO;
import com.inventario.model.Setor;
import java.util.List;

public class SetorService {
    
    private SetorDAO setorDAO;
    
    public SetorService() {
        this.setorDAO = new SetorDAO();
    }
    
    public List<Setor> listarTodos() throws Exception {
        return setorDAO.listarSetores();
    }
    
    public void salvar(Setor setor) throws Exception {
        if (setor.getId() == 0) {
            setorDAO.inserirSetor(setor);
        } else {
            setorDAO.atualizarSetor(setor);
        }
    }
    
    public void excluir(int id) throws Exception {
        setorDAO.excluirSetor(id);
    }
}
```

### **DEPOIS: SetorServiceRefactored.java**

```java
package com.inventario.service;

import com.inventario.dao.SetorDAORefactored;
import com.inventario.model.Setor;
import java.util.List;

public class SetorServiceRefactored {
    
    private SetorDAORefactored setorDAO;
    
    public SetorServiceRefactored() {
        this.setorDAO = new SetorDAORefactored();
    }
    
    public List<Setor> listarTodos() throws Exception {
        return setorDAO.findAll();  // ✅ Método do BaseDAO
    }
    
    public void salvar(Setor setor) throws Exception {
        if (setor.getId() == 0) {
            setorDAO.insert(setor);  // ✅ Método do BaseDAO
        } else {
            setorDAO.update(setor);  // ✅ Método do BaseDAO
        }
    }
    
    public void excluir(int id) throws Exception {
        setorDAO.delete(id);  // ✅ Método do BaseDAO
    }
}
```

---

## 📊 Exemplo 3: Migrar Múltiplas Classes

### Ordem Recomendada

```
1. DAOs (base do sistema)
   ├── SetorDAORefactored ✅
   ├── PatrimonioDAORefactored
   ├── SalaDAORefactored
   └── ResponsavelDAORefactored

2. Services (camada intermediária)
   ├── SetorService
   ├── PatrimonioService
   ├── SalaService
   └── ResponsavelService

3. Frames (interface)
   ├── SetorFrame
   ├── PatrimonioFrame
   ├── SalaFrame
   └── ResponsavelFrame
```

---

## 🎯 Checklist de Migração

### Para Cada Classe

#### Antes
- [ ] Fazer backup da classe original
- [ ] Identificar dependências
- [ ] Listar métodos usados
- [ ] Verificar imports

#### Durante
- [ ] Criar versão refatorada
- [ ] Atualizar imports
- [ ] Substituir métodos
- [ ] Aplicar classes utilitárias
- [ ] Compilar sem erros

#### Depois
- [ ] Testar funcionalidades
- [ ] Verificar logs
- [ ] Validar performance
- [ ] Documentar mudanças
- [ ] Commit no Git

---

## 🚀 Script de Migração Automatizada

### Script Bash para Ajudar na Migração

```bash
#!/bin/bash
# migrate-class.sh

CLASS_NAME=$1
CLASS_TYPE=$2  # dao, service, frame

if [ -z "$CLASS_NAME" ] || [ -z "$CLASS_TYPE" ]; then
    echo "Uso: ./migrate-class.sh <ClassName> <dao|service|frame>"
    exit 1
fi

echo "🚀 Migrando $CLASS_NAME ($CLASS_TYPE)..."

# 1. Criar backup
echo "📦 Criando backup..."
cp "src/main/java/com/inventario/$CLASS_TYPE/$CLASS_NAME.java" \
   "src/main/java/com/inventario/$CLASS_TYPE/$CLASS_NAME.java.bak"

# 2. Criar versão refatorada
echo "✨ Criando versão refatorada..."
cp "src/main/java/com/inventario/$CLASS_TYPE/$CLASS_NAME.java" \
   "src/main/java/com/inventario/$CLASS_TYPE/${CLASS_NAME}Refactored.java"

# 3. Substituir nome da classe
echo "🔧 Atualizando nome da classe..."
sed -i "s/public class $CLASS_NAME/public class ${CLASS_NAME}Refactored/g" \
    "src/main/java/com/inventario/$CLASS_TYPE/${CLASS_NAME}Refactored.java"

# 4. Adicionar imports utilitários
echo "📚 Adicionando imports..."
sed -i "/^package/a import com.inventario.util.*;" \
    "src/main/java/com/inventario/$CLASS_TYPE/${CLASS_NAME}Refactored.java"

echo "✅ Migração inicial completa!"
echo "⚠️  Agora você precisa:"
echo "   1. Atualizar métodos manualmente"
echo "   2. Aplicar classes utilitárias"
echo "   3. Testar a classe"
echo ""
echo "📝 Arquivo criado: ${CLASS_NAME}Refactored.java"
echo "💾 Backup em: ${CLASS_NAME}.java.bak"
```

### Uso do Script

```bash
# Dar permissão de execução
chmod +x migrate-class.sh

# Migrar um DAO
./migrate-class.sh SetorDAO dao

# Migrar um Service
./migrate-class.sh SetorService service

# Migrar um Frame
./migrate-class.sh SetorFrame view
```

---

## 📚 Recursos Adicionais

### Templates Prontos

1. **Template de DAO**
```java
@Repository
public class [Nome]DAORefactored extends BaseDAO<[Entidade], Integer> {
    // Implementar 6 métodos abstratos
    // Adicionar métodos específicos
}
```

2. **Template de Service**
```java
public class [Nome]ServiceRefactored {
    private [Nome]DAORefactored dao = new [Nome]DAORefactored();
    // Usar métodos do BaseDAO
}
```

3. **Template de Frame**
```java
public class [Nome]FrameRefactored extends JFrame {
    // Usar DialogUtils, ValidationUtils, ExceptionHandler
}
```

---

## 🎉 Resultado Esperado

### Após Migração de SetorFrame

```
Código:
- Linhas: 300 → 180 (-40%)
- JOptionPane: 8 → 0 (-100%)
- Validações manuais: 5 → 0 (-100%)
- Try-catch manuais: 3 → 0 (-100%)

Performance:
- Conexões: Pool HikariCP (+75%)
- Vazamento: 0 recursos

Qualidade:
- Mensagens: 100% padronizadas
- Logs: Estruturados
- Erros: Tratamento robusto
- Código: 3x mais legível
```

---

## ✅ Conclusão

Este guia mostra como migrar classes para usar as versões refatoradas de forma **segura e gradual**.

**Próximos Passos**:
1. Escolher uma classe para migrar
2. Seguir o passo a passo
3. Testar completamente
4. Documentar resultados
5. Repetir para outras classes

**Status**: 🚀 **PRONTO PARA COMEÇAR**
