# Como Usar Informações de Versão do Git

A classe `VersionInfo` permite obter informações de versão do sistema usando Git.

## Funcionalidades

### 1. Obter Versão Simples
```java
String versao = VersionInfo.getVersion();
// Retorna: "1.2.0"
```

### 2. Obter Commit Git
```java
String commit = VersionInfo.getGitCommit();
// Retorna: "e8f2d9b73ae3683aba711d4daa0e58fcc62ea750"

String commitShort = VersionInfo.getGitCommitShort();
// Retorna: "e8f2d9b"
```

### 3. Obter Branch Atual
```java
String branch = VersionInfo.getGitBranch();
// Retorna: "main" ou "develop", etc.
```

### 4. Versão Completa
```java
String fullVersion = VersionInfo.getFullVersion();
// Retorna: "1.2.0 (commit: e8f2d9b, branch: main)"
```

### 5. Informações Detalhadas
```java
String info = VersionInfo.getVersionInfo();
// Retorna todas as informações formatadas
```

### 6. HTML para Interface Gráfica
```java
String html = VersionInfo.getVersionInfoHtml();
JLabel label = new JLabel(html);
JOptionPane.showMessageDialog(null, label, "Sobre", JOptionPane.INFORMATION_MESSAGE);
```

### 7. Verificar Mudanças Não Commitadas
```java
boolean hasChanges = VersionInfo.hasUncommittedChanges();
if (hasChanges) {
    System.out.println("⚠ Há mudanças não commitadas no repositório");
}
```

### 8. Verificar Commits à Frente do Remote
```java
int ahead = VersionInfo.getCommitsAhead();
if (ahead > 0) {
    System.out.println("↑ " + ahead + " commit(s) à frente do remote");
}
```

## Uso nas Telas

### MainFrame
```java
private void mostrarSobre() {
    JLabel label = new JLabel(VersionInfo.getVersionInfoHtml());
    label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    JOptionPane.showMessageDialog(this, 
        label, 
        "Sobre o Sistema", 
        JOptionPane.INFORMATION_MESSAGE);
}
```

### Rodapé da Aplicação
```java
JLabel lblVersion = new JLabel("v" + VersionInfo.getVersion() + 
    " (" + VersionInfo.getGitCommitShort() + ")");
lblVersion.setFont(new Font("Arial", Font.PLAIN, 10));
lblVersion.setForeground(Color.GRAY);
```

### Logs de Inicialização
```java
logger.info("Iniciando " + VersionInfo.getAppName());
logger.info("Versão: " + VersionInfo.getFullVersion());
logger.info("Commit: " + VersionInfo.getGitCommit());
logger.info("Branch: " + VersionInfo.getGitBranch());
```

## Comandos Git Utilizados

A classe executa os seguintes comandos Git:

1. `git rev-parse HEAD` - Hash completo do commit
2. `git rev-parse --abbrev-ref HEAD` - Nome do branch atual
3. `git log -1 --format=%cd --date=format:%d/%m/%Y %H:%M` - Data do último commit
4. `git status --porcelain` - Verificar mudanças não commitadas
5. `git rev-list --count @{u}..` - Contar commits à frente do remote

## Observações

- Se o Git não estiver disponível, os métodos retornam "N/A"
- A classe é thread-safe (usa synchronized)
- As informações são carregadas apenas uma vez (lazy loading)
- Não gera exceções, falha silenciosamente se Git não disponível

## Exemplo de Saída

```
============================================================
SIHCP - Sistema de Histórico e Coleta Patrimonial
Versão: 1.2.0
Git Commit: e8f2d9b73ae3683aba711d4daa0e58fcc62ea750
Git Branch: main
Data do Commit: 08/11/2025 15:30
Data de Build: 08/11/2025 15:45
Java Version: 21.0.1
OS: Windows 11 10.0
============================================================

Versão Completa: 1.2.0 (commit: e8f2d9b, branch: main)
Mudanças não commitadas: false
Commits à frente: 0
```
