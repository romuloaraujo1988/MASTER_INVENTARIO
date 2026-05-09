# Mudança de Localização dos Arquivos de Configuração

## 🎯 Motivação

A pasta `.inventario` (com ponto) era **muito restritiva** e causava problemas:

### Problemas da Localização Antiga

❌ **Pasta oculta** - Difícil de encontrar no Windows Explorer  
❌ **Não segue padrões** - Não usa convenções do sistema operacional  
❌ **Confusa para usuários** - Usuários não técnicos não sabem onde procurar  
❌ **Backup difícil** - Pastas ocultas geralmente não são incluídas em backups  
❌ **Suporte complicado** - Difícil de guiar usuários por telefone  

## ✅ Nova Localização

### Windows

**Antes:**
```
C:\Users\[usuario]\.inventario\          ❌ Oculta, não padrão
```

**Depois:**
```
C:\Users\[usuario]\AppData\Local\SIHCP-Inventario\    ✅ Padrão Windows
```

**Alternativa (se AppData não disponível):**
```
C:\Users\[usuario]\Documents\SIHCP-Inventario\        ✅ Visível, fácil acesso
```

### Linux/Mac

**Antes:**
```
~/.inventario/                           ❌ Não segue XDG
```

**Depois:**
```
~/.config/sihcp-inventario/              ✅ Padrão XDG Base Directory
```

## 🔄 Migração Automática

O sistema **migra automaticamente** as configurações antigas na primeira execução:

```
⚠ Detectada configuração antiga em: C:\Users\usuario\.inventario
✓ Migrado: database-config.properties
✓ Migrado: sgbd-config.properties
✓ Migração concluída!
  Nova localização: C:\Users\usuario\AppData\Local\SIHCP-Inventario
  Você pode deletar a pasta antiga: C:\Users\usuario\.inventario
```

### Como Funciona

1. Sistema inicia
2. `DatabaseConfigManager` é criado
3. Chama `ConfigurationPaths.migrateOldConfiguration()`
4. Verifica se existe `~/.inventario/`
5. Se existir, copia arquivos para nova localização
6. Informa usuário sobre migração
7. Continua normalmente

### Arquivos Migrados

- ✅ `database-config.properties` (com senha criptografada)
- ✅ `sgbd-config.properties`

## 📊 Comparação

| Aspecto | Antiga (`.inventario`) | Nova (`SIHCP-Inventario`) |
|---------|------------------------|---------------------------|
| **Visibilidade** | ❌ Oculta | ✅ Visível |
| **Padrão SO** | ❌ Não | ✅ Sim (AppData/XDG) |
| **Fácil de encontrar** | ❌ Não | ✅ Sim |
| **Backup automático** | ❌ Geralmente não | ✅ Geralmente sim |
| **Suporte ao usuário** | ❌ Difícil | ✅ Fácil |
| **Nome descritivo** | ❌ Genérico | ✅ Identifica o app |

## 🚀 Benefícios

### Para Usuários

- ✅ **Fácil de encontrar** - Pasta visível e com nome descritivo
- ✅ **Backup simples** - Incluída em backups automáticos
- ✅ **Suporte facilitado** - Fácil de guiar por telefone/email
- ✅ **Sem surpresas** - Segue convenções conhecidas

### Para Desenvolvedores

- ✅ **Padrões do SO** - Segue convenções Windows/Linux
- ✅ **Migração automática** - Usuários não precisam fazer nada
- ✅ **Compatibilidade** - Funciona com configurações antigas
- ✅ **Manutenibilidade** - Código mais limpo e organizado

### Para Suporte

- ✅ **Localização previsível** - Sempre no mesmo lugar
- ✅ **Nome identificável** - "SIHCP-Inventario" é claro
- ✅ **Logs claros** - Sistema informa onde está a configuração
- ✅ **Troubleshooting fácil** - Usuário consegue encontrar sozinho

## 📝 Instruções para Usuários

### Como Encontrar a Configuração

**Windows:**
1. Pressione `Win + R`
2. Digite: `%LOCALAPPDATA%\SIHCP-Inventario`
3. Pressione Enter

Ou:
1. Abra o Explorador de Arquivos
2. Vá para: `C:\Users\[seu-usuario]\AppData\Local\SIHCP-Inventario`

**Linux/Mac:**
```bash
cd ~/.config/sihcp-inventario
ls -la
```

### Como Fazer Backup

**Windows:**
1. Copie a pasta: `%LOCALAPPDATA%\SIHCP-Inventario`
2. Cole em local seguro (pen drive, nuvem, etc.)

**Linux/Mac:**
```bash
# Backup
tar -czf sihcp-backup.tar.gz ~/.config/sihcp-inventario

# Restaurar
tar -xzf sihcp-backup.tar.gz -C ~/
```

### Como Limpar Configuração

**Windows:**
1. Feche o aplicativo
2. Delete a pasta: `%LOCALAPPDATA%\SIHCP-Inventario`
3. Reabra o aplicativo e configure novamente

**Linux/Mac:**
```bash
rm -rf ~/.config/sihcp-inventario
```

## 🔧 Para Desenvolvedores

### Código de Migração

```java
// ConfigurationPaths.java
public static boolean migrateOldConfiguration() {
    String oldDir = System.getProperty("user.home") + File.separator + ".inventario";
    File oldDirectory = new File(oldDir);
    
    if (!oldDirectory.exists()) {
        return true; // Nada para migrar
    }
    
    String newDir = getConfigDirectory();
    File newDirectory = new File(newDir);
    newDirectory.mkdirs();
    
    // Copiar arquivos...
    copyFile(oldDbConfig, newDbConfig);
    copyFile(oldSgbdConfig, newSgbdConfig);
    
    return true;
}
```

### Chamada Automática

```java
// DatabaseConfigManager.java
public DatabaseConfigManager() {
    // Migração automática na inicialização
    ConfigurationPaths.migrateOldConfiguration();
    
    createConfigDirectoryIfNotExists();
    loadConfiguration();
}
```

## ⚠️ Notas Importantes

### Pasta Antiga

- A pasta antiga `~/.inventario/` **NÃO é deletada automaticamente**
- Usuário pode deletar manualmente após confirmar que tudo funciona
- Sistema informa onde está a pasta antiga

### Compatibilidade

- ✅ Configurações antigas são migradas automaticamente
- ✅ Senhas criptografadas continuam funcionando
- ✅ Nenhuma ação manual necessária

### Rollback

Se precisar voltar para pasta antiga (não recomendado):

```java
// Definir variável de ambiente
export INVENTARIO_CONFIG_DIR=~/.inventario

// Ou no código
ConfigurationPaths.setConfigMode(ConfigMode.CUSTOM);
System.setProperty("INVENTARIO_CONFIG_DIR", 
    System.getProperty("user.home") + "/.inventario");
```

## 📚 Referências

- [XDG Base Directory Specification](https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html)
- [Windows Known Folders](https://docs.microsoft.com/en-us/windows/win32/shell/known-folders)
- [AppData vs ProgramData](https://stackoverflow.com/questions/22107812/appdata-vs-programdata)

---

**Versão**: 2.0.0  
**Data**: 12/11/2025  
**Autor**: Sistema de Inventário IFMT

**Mudança**: Localização dos arquivos de configuração  
**Impacto**: Baixo (migração automática)  
**Ação Necessária**: Nenhuma (automático)
