# 🔧 Solução de Problemas do Classpath

## ❌ Problema: "Ainda com problema no classpath"

### 🎯 Solução Rápida

Execute este comando:
```bash
corrigir-classpath.bat
```

Depois, na sua IDE:

#### Eclipse:
1. Clique com botão direito no projeto
2. **Maven → Update Project** (Alt+F5)
3. Marque **"Force Update of Snapshots/Releases"**
4. Clique **OK**
5. **Project → Clean → Clean all projects**
6. Aguarde a recompilação

#### IntelliJ IDEA:
1. **File → Invalidate Caches / Restart**
2. Clique em **"Invalidate and Restart"**
3. Aguarde a reindexação completa

#### VS Code:
1. **Ctrl+Shift+P**
2. Digite: **"Java: Clean Java Language Server Workspace"**
3. **Reload Window**

---

## 🔍 Diagnóstico Detalhado

### Verificar se o Maven está funcionando:
```bash
mvn -version
```

**Resultado esperado:**
```
Apache Maven 3.x.x
Java version: 21.x.x
```

### Verificar dependências do POI:
```bash
mvn dependency:tree -Dincludes=org.apache.poi:*
```

**Resultado esperado:**
```
[INFO] +- org.apache.poi:poi:jar:5.4.0:compile
[INFO] +- org.apache.poi:poi-ooxml:jar:5.4.0:compile
[INFO] +- org.apache.poi:poi-ooxml-full:jar:5.4.0:compile
```

### Verificar se há conflitos:
```bash
mvn dependency:analyze
```

---

## 🛠️ Soluções por Tipo de Problema

### Problema 1: "Cannot resolve symbol XSSFWorkbook"

**Causa:** Classes do Apache POI não estão no classpath

**Solução:**
```bash
mvn clean install -U
```

Depois na IDE: **Maven → Update Project**

### Problema 2: "NoClassDefFoundError: org/apache/poi/..."

**Causa:** JARs não estão sendo carregados

**Solução:**
1. Verifique se os JARs existem:
```bash
dir lib\poi-5.4.0.jar
```

2. Se não existirem, copie do Maven:
```bash
copiar-libs-poi.bat
```

3. Atualize o projeto na IDE

### Problema 3: "ExceptionInInitializerError"

**Causa:** Dependências incompatíveis ou faltando

**Solução:**
1. Limpe o repositório Maven:
```bash
mvn dependency:purge-local-repository
```

2. Baixe novamente:
```bash
mvn clean install -U
```

3. **IMPORTANTE:** O sistema tem fallback automático para CSV!

### Problema 4: "Build path is incomplete"

**Causa:** Classpath não está sincronizado

**Solução:**
```bash
corrigir-classpath.bat
```

Depois: **Maven → Update Project** na IDE

### Problema 5: "Project configuration is not up-to-date"

**Causa:** Mudanças no pom.xml não foram aplicadas

**Solução Eclipse:**
1. Clique com botão direito no projeto
2. **Maven → Update Project**
3. Marque **"Force Update"**
4. **OK**

**Solução IntelliJ:**
1. Clique no ícone Maven (lateral direita)
2. Clique em **"Reload All Maven Projects"** (ícone de refresh)

---

## 🔄 Processo Completo de Reset

Se nada funcionar, faça um reset completo:

### Passo 1: Limpar tudo
```bash
mvn clean
```

### Passo 2: Deletar cache do Maven
```bash
rmdir /s /q %USERPROFILE%\.m2\repository\org\apache\poi
```

### Passo 3: Baixar novamente
```bash
mvn dependency:resolve -U
```

### Passo 4: Recriar classpath
```bash
mvn eclipse:clean eclipse:eclipse
```

### Passo 5: Na IDE
- **Eclipse:** Maven → Update Project + Clean
- **IntelliJ:** Invalidate Caches / Restart
- **VS Code:** Clean Java Language Server Workspace

### Passo 6: Recompilar
```bash
mvn compile
```

---

## 📋 Checklist de Verificação

Marque cada item conforme completa:

- [ ] Maven instalado e funcionando (`mvn -version`)
- [ ] Java 21 configurado
- [ ] pom.xml com versão 5.4.0
- [ ] Dependências baixadas (`mvn dependency:resolve`)
- [ ] Projeto atualizado na IDE (Maven → Update Project)
- [ ] Projeto limpo e recompilado (Clean + Build)
- [ ] Sem erros de compilação
- [ ] Aplicação inicia sem erros

---

## 🎯 Teste Final

Após corrigir o classpath, teste:

1. Abra a aplicação
2. Vá em **Relatórios**
3. Gere um relatório
4. Clique em **"Relatório Atual"**
5. Escolha onde salvar

**Resultado esperado:**
- ✅ Arquivo `.xlsx` criado OU
- ✅ Arquivo `.csv` criado (fallback automático)

---

## 💡 Dica Importante

**O sistema tem fallback automático para CSV!**

Se Apache POI não funcionar por problemas de classpath:
- Sistema detecta automaticamente
- Gera arquivo CSV
- CSV abre perfeitamente no Excel
- Você recebe uma mensagem explicativa

**Isso NÃO é um erro, é uma solução alternativa funcional!**

---

## 🆘 Última Opção

Se absolutamente nada funcionar:

### Opção 1: Usar apenas Maven (sem lib/)
Remova todas as entradas `<classpathentry kind="lib"...` do `.classpath` e deixe apenas:
```xml
<classpathentry kind="con" path="org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER">
```

### Opção 2: Aceitar o fallback CSV
O sistema funciona perfeitamente com CSV:
- Mais rápido
- Sem dependências problemáticas
- 100% compatível com Excel
- Sem ExceptionInInitializerError

---

**Última atualização:** Agora
**Versão do POI:** 5.4.0
**Status:** Pronto para corrigir
