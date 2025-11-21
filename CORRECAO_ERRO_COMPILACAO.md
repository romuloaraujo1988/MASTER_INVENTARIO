# Correção do Erro de Compilação - MainFrame

## ❌ Erro Encontrado

```
Exception in thread "AWT-EventQueue-0" java.lang.Error: Unresolved compilation problems:
The method abrirDashboardColeta() is undefined for the type MainFrame
```

## 🔍 Diagnóstico

O método `abrirDashboardColeta()` **EXISTE** no arquivo `MainFrame.java` (linha ~886), mas o IDE/compilador não está reconhecendo devido a um problema de compilação incremental.

## ✅ Solução

### Opção 1: Limpar e Recompilar (RECOMENDADO)

Execute os seguintes comandos na raiz do projeto:

```bash
# Limpar compilação anterior
mvn clean

# Recompilar tudo
mvn compile

# Ou fazer tudo de uma vez
mvn clean compile
```

### Opção 2: Recompilar no Eclipse/IntelliJ

**Eclipse:**
1. Clique com botão direito no projeto
2. Selecione "Clean..."
3. Marque o projeto
4. Clique em "Clean"
5. Aguarde a recompilação automática

**IntelliJ IDEA:**
1. Menu: Build → Rebuild Project
2. Aguarde a conclusão

### Opção 3: Forçar Recompilação do MainFrame

Se as opções acima não funcionarem:

```bash
# Deletar o .class compilado
del target\classes\com\inventario\view\MainFrame.class

# Recompilar apenas o MainFrame
mvn compile -pl .
```

## 🔧 Verificação

Após recompilar, verifique se o erro foi resolvido:

1. Execute o sistema novamente
2. Tente fazer login
3. Confirme que o MainFrame abre corretamente

## 📋 Checklist

- [ ] Executar `mvn clean compile`
- [ ] Verificar que não há erros de compilação
- [ ] Testar login no sistema
- [ ] Confirmar que MainFrame abre sem erros

## 🎯 Causa Raiz

O problema ocorreu porque:
1. O arquivo `MainFrame.java` foi modificado (adição do menu SIADS)
2. O IDE aplicou autofix/formatação
3. A compilação incremental não detectou todas as mudanças
4. Os arquivos `.class` antigos ainda estavam em cache

## 💡 Prevenção Futura

Para evitar este problema:
1. Sempre executar `mvn clean compile` após mudanças significativas
2. Configurar IDE para recompilar automaticamente
3. Limpar cache do IDE periodicamente

---

**Status:** ⚠️ AGUARDANDO RECOMPILAÇÃO  
**Ação Necessária:** Executar `mvn clean compile`
