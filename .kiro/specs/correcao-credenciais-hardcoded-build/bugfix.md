# Bugfix Requirements Document

## Introduction

As credenciais da keystore de release (senhas e alias) estão hardcoded em texto plano no arquivo `build.gradle`, que é versionado no Git. Isso representa um risco de segurança crítico, pois qualquer pessoa com acesso ao repositório pode visualizar as senhas da keystore de release. Além disso, impossibilita a rotação de credenciais sem criar commits no Git, viola boas práticas de segurança e pode comprometer a keystore se o repositório vazar.

**Impacto:** 🔴 CRÍTICO - Risco de segurança e compliance, violação de políticas de segurança, possível comprometimento da keystore de release.

**Componentes Afetados:**
- `InventarioMobile/app/build.gradle` (linhas 35-38) - Contém credenciais hardcoded
- `.gitignore` - Precisa incluir `keystore.properties`
- Documentação - Precisa explicar configuração local
- CI/CD - Precisa configurar variáveis de ambiente

**Solução Padrão Android:**
Criar arquivo `keystore.properties` (não versionado) e ler credenciais dele no `build.gradle`.

---

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN o arquivo `build.gradle` é aberto THEN as senhas da keystore estão visíveis em texto plano nas linhas 35-38

1.2 WHEN o repositório é clonado por qualquer desenvolvedor THEN as credenciais da keystore são expostas imediatamente

1.3 WHEN o histórico do Git é analisado THEN todas as versões anteriores das senhas estão acessíveis em commits antigos

1.4 WHEN é necessário rotacionar as senhas THEN é preciso criar um commit no Git expondo as novas credenciais

1.5 WHEN o repositório é compartilhado ou forkado THEN as credenciais são copiadas para outros repositórios

1.6 WHEN uma auditoria de segurança é realizada THEN o sistema falha por expor credenciais em código versionado

1.7 WHEN o build de release é executado THEN as credenciais hardcoded são usadas sem validação de segurança

### Expected Behavior (Correct)

2.1 WHEN o arquivo `build.gradle` é aberto THEN as credenciais SHALL estar referenciadas através de variáveis lidas de arquivo externo não versionado

2.2 WHEN o repositório é clonado por um desenvolvedor THEN as credenciais SHALL NÃO estar disponíveis até que o desenvolvedor configure seu arquivo `keystore.properties` local

2.3 WHEN o histórico do Git é analisado THEN nenhuma credencial SHALL estar visível em commits (após limpeza do histórico)

2.4 WHEN é necessário rotacionar as senhas THEN o desenvolvedor SHALL apenas atualizar o arquivo `keystore.properties` local sem criar commits

2.5 WHEN o arquivo `keystore.properties` não existe THEN o build SHALL falhar graciosamente com mensagem clara indicando a necessidade de configuração

2.6 WHEN uma auditoria de segurança é realizada THEN o sistema SHALL passar por não expor credenciais em código versionado

2.7 WHEN o build de release é executado THEN as credenciais SHALL ser lidas do arquivo `keystore.properties` com validação de existência

2.8 WHEN o arquivo `.gitignore` é verificado THEN o arquivo `keystore.properties` SHALL estar listado para não ser versionado

2.9 WHEN um novo desenvolvedor configura o projeto THEN a documentação SHALL explicar como criar o arquivo `keystore.properties` com as credenciais

### Unchanged Behavior (Regression Prevention)

3.1 WHEN o build de debug é executado THEN o sistema SHALL CONTINUE TO usar a keystore de debug padrão sem necessidade de configuração adicional

3.2 WHEN o build de release é executado com `keystore.properties` configurado corretamente THEN o sistema SHALL CONTINUE TO gerar o APK assinado normalmente

3.3 WHEN o arquivo `build.gradle` é modificado para outras configurações THEN o sistema SHALL CONTINUE TO funcionar sem afetar outras partes do build

3.4 WHEN o versionCode e versionName são atualizados THEN o sistema SHALL CONTINUE TO incrementar versões normalmente

3.5 WHEN dependências são adicionadas ou atualizadas THEN o sistema SHALL CONTINUE TO resolver dependências corretamente

---

## Bug Condition and Property

### Bug Condition Function

```pascal
FUNCTION isBugCondition(X)
  INPUT: X of type BuildConfiguration
  OUTPUT: boolean
  
  // Retorna true quando credenciais estão hardcoded
  RETURN X.signingConfig.storePassword IS_HARDCODED 
         AND X.signingConfig.keyPassword IS_HARDCODED
         AND X.buildFile IS_VERSIONED_IN_GIT
END FUNCTION
```

**Explicação:** O bug ocorre quando as senhas da keystore estão hardcoded diretamente no arquivo `build.gradle` que é versionado no Git.

### Property Specification - Fix Checking

```pascal
// Property: Fix Checking - Credenciais Externalizadas
FOR ALL X WHERE isBugCondition(X) DO
  result ← buildConfiguration'(X)
  ASSERT result.credentialsFile = "keystore.properties"
  ASSERT result.credentialsFile NOT_IN_GIT
  ASSERT result.signingConfig.storePassword = READ_FROM_FILE(result.credentialsFile)
  ASSERT result.signingConfig.keyPassword = READ_FROM_FILE(result.credentialsFile)
  ASSERT result.buildFile CONTAINS_NO_HARDCODED_CREDENTIALS
END FOR
```

**Explicação:** Para todas as configurações onde o bug ocorria, após a correção:
- As credenciais DEVEM estar em arquivo externo `keystore.properties`
- O arquivo de credenciais NÃO DEVE estar no Git
- As senhas DEVEM ser lidas do arquivo externo
- O `build.gradle` NÃO DEVE conter credenciais hardcoded

### Property Specification - Preservation Checking

```pascal
// Property: Preservation Checking - Build Funcional Preservado
FOR ALL X WHERE NOT isBugCondition(X) DO
  ASSERT buildConfiguration(X) = buildConfiguration'(X)
END FOR
```

**Explicação:** Para todas as configurações onde o bug NÃO ocorre (build debug, outras configurações), o comportamento DEVE permanecer idêntico.

---

## Counterexample (Demonstração do Bug)

**Cenário:** Desenvolvedor clona repositório e visualiza credenciais

**Entrada:**
```groovy
// Estado atual no build.gradle (linhas 35-38)
signingConfigs {
    release {
        storeFile file("../inventario-release.keystore")
        storePassword "inventario2025"
        keyAlias "inventario"
        keyPassword "inventario2025"
    }
}
```

**Comportamento Atual (Buggy):**
```bash
# Qualquer pessoa pode ver as senhas
git clone <repositorio>
cat InventarioMobile/app/build.gradle

# Resultado
storePassword "inventario2025"  # ❌ Senha exposta
keyPassword "inventario2025"    # ❌ Senha exposta

# Histórico também expõe
git log --all -p | grep "storePassword"  # ❌ Senhas em commits antigos
```

**Comportamento Esperado (Fixed):**
```groovy
// build.gradle (sem credenciais)
def keystorePropertiesFile = rootProject.file("keystore.properties")
def keystoreProperties = new Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(new FileInputStream(keystorePropertiesFile))
}

signingConfigs {
    release {
        storeFile file(keystoreProperties['storeFile'])
        storePassword keystoreProperties['storePassword']
        keyAlias keystoreProperties['keyAlias']
        keyPassword keystoreProperties['keyPassword']
    }
}
```

```properties
# keystore.properties (NÃO versionado)
storePassword=inventario2025
keyPassword=inventario2025
keyAlias=inventario
storeFile=../inventario-release.keystore
```

```bash
# Após correção
git clone <repositorio>
cat InventarioMobile/app/build.gradle

# Resultado
storePassword keystoreProperties['storePassword']  # ✅ Referência segura
keyPassword keystoreProperties['keyPassword']      # ✅ Referência segura

# Histórico limpo
git log --all -p | grep "storePassword"  # ✅ Nenhuma senha exposta
```

---

## Technical Context

### Arquitetura Atual

```
build.gradle (versionado no Git)
    ↓ (contém hardcoded)
signingConfigs.release
    ├── storePassword "inventario2025"  ❌ EXPOSTO
    ├── keyPassword "inventario2025"    ❌ EXPOSTO
    ├── keyAlias "inventario"           ❌ EXPOSTO
    └── storeFile "../inventario-release.keystore"
```

### Arquitetura Proposta

```
build.gradle (versionado no Git)
    ↓ (lê de arquivo externo)
keystore.properties (NÃO versionado, em .gitignore)
    ├── storePassword=***  ✅ SEGURO
    ├── keyPassword=***    ✅ SEGURO
    ├── keyAlias=***       ✅ SEGURO
    └── storeFile=***      ✅ SEGURO
```

### Código Problemático

**Arquivo:** `InventarioMobile/app/build.gradle`

**Linhas:** 35-38

```groovy
signingConfigs {
    release {
        storeFile file("../inventario-release.keystore")
        storePassword "inventario2025"  // ❌ HARDCODED
        keyAlias "inventario"           // ❌ HARDCODED
        keyPassword "inventario2025"    // ❌ HARDCODED
    }
}
```

### Solução Proposta

**1. Criar arquivo `keystore.properties` na raiz do projeto:**

```properties
storePassword=inventario2025
keyPassword=inventario2025
keyAlias=inventario
storeFile=../inventario-release.keystore
```

**2. Atualizar `build.gradle`:**

```groovy
// Carregar propriedades da keystore
def keystorePropertiesFile = rootProject.file("keystore.properties")
def keystoreProperties = new Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(new FileInputStream(keystorePropertiesFile))
}

signingConfigs {
    release {
        if (keystorePropertiesFile.exists()) {
            storeFile file(keystoreProperties['storeFile'])
            storePassword keystoreProperties['storePassword']
            keyAlias keystoreProperties['keyAlias']
            keyPassword keystoreProperties['keyPassword']
        } else {
            // Falha graciosamente se arquivo não existe
            throw new GradleException(
                "keystore.properties não encontrado. " +
                "Crie o arquivo na raiz do projeto com as credenciais da keystore."
            )
        }
    }
}
```

**3. Atualizar `.gitignore`:**

```gitignore
# Keystore credentials (NUNCA versionar)
keystore.properties
*.keystore
*.jks
```

**4. Criar `keystore.properties.example` (template versionado):**

```properties
# Template para keystore.properties
# Copie este arquivo para keystore.properties e preencha com suas credenciais

storePassword=SUA_SENHA_AQUI
keyPassword=SUA_SENHA_AQUI
keyAlias=SEU_ALIAS_AQUI
storeFile=../caminho/para/sua/keystore.jks
```

**5. Criar documentação `CONFIGURACAO_KEYSTORE.md`:**

```markdown
# Configuração da Keystore para Build de Release

## Passo 1: Criar arquivo keystore.properties

Na raiz do projeto, crie um arquivo chamado `keystore.properties`:

```properties
storePassword=inventario2025
keyPassword=inventario2025
keyAlias=inventario
storeFile=../inventario-release.keystore
```

## Passo 2: Verificar .gitignore

Certifique-se de que o arquivo `.gitignore` contém:

```
keystore.properties
*.keystore
*.jks
```

## Passo 3: Build de Release

Execute o build normalmente:

```bash
./gradlew assembleRelease
```

## Segurança

⚠️ NUNCA commite o arquivo `keystore.properties` no Git!
⚠️ NUNCA compartilhe suas credenciais da keystore!
```

---

## Validation Criteria

### Critério 1: Credenciais Não Estão no Git
- ✅ Verificar que `build.gradle` não contém senhas hardcoded
- ✅ Verificar que `keystore.properties` está no `.gitignore`
- ✅ Verificar que `git status` não mostra `keystore.properties`
- ✅ Verificar que `git log` não contém senhas (após limpeza)

### Critério 2: Build Funciona com Arquivo Externo
- ✅ Criar `keystore.properties` com credenciais
- ✅ Executar `./gradlew assembleRelease`
- ✅ Verificar que APK é gerado e assinado corretamente
- ✅ Verificar que assinatura é válida: `jarsigner -verify -verbose app-release.apk`

### Critério 3: Build Falha Graciosamente Sem Arquivo
- ✅ Remover `keystore.properties`
- ✅ Executar `./gradlew assembleRelease`
- ✅ Verificar que build falha com mensagem clara
- ✅ Verificar que mensagem indica necessidade de criar `keystore.properties`

### Critério 4: Documentação Está Completa
- ✅ Verificar que `CONFIGURACAO_KEYSTORE.md` existe
- ✅ Verificar que `keystore.properties.example` existe
- ✅ Verificar que instruções são claras e completas
- ✅ Verificar que avisos de segurança estão presentes

### Critério 5: Histórico do Git Está Limpo
- ✅ Executar `git log --all -p | grep "inventario2025"`
- ✅ Verificar que nenhuma senha aparece no histórico
- ✅ Verificar que commits antigos foram reescritos (se necessário)
- ✅ Verificar que `git filter-repo` foi usado para limpeza (se necessário)

### Critério 6: CI/CD Configurado
- ✅ Verificar que pipeline CI/CD usa variáveis de ambiente
- ✅ Verificar que credenciais não estão em scripts de CI/CD
- ✅ Verificar que build de release funciona no CI/CD
- ✅ Verificar que logs do CI/CD não expõem credenciais

---

## Security Checklist

### Antes da Correção
- [ ] Fazer backup da keystore atual
- [ ] Documentar credenciais atuais em local seguro (gerenciador de senhas)
- [ ] Notificar equipe sobre mudança de processo
- [ ] Planejar rotação de credenciais após correção

### Durante a Correção
- [ ] Criar `keystore.properties` localmente
- [ ] Atualizar `build.gradle` para ler do arquivo externo
- [ ] Adicionar `keystore.properties` ao `.gitignore`
- [ ] Testar build de release localmente
- [ ] Criar documentação de configuração

### Após a Correção
- [ ] Limpar histórico do Git (remover senhas de commits antigos)
- [ ] Rotacionar credenciais da keystore
- [ ] Atualizar documentação do projeto
- [ ] Treinar equipe sobre novo processo
- [ ] Configurar CI/CD com variáveis de ambiente
- [ ] Realizar auditoria de segurança

---

## Limpeza do Histórico do Git

**⚠️ IMPORTANTE:** Após implementar a correção, é necessário limpar o histórico do Git para remover as senhas dos commits antigos.

### Opção 1: git-filter-repo (Recomendado)

```bash
# Instalar git-filter-repo
pip install git-filter-repo

# Criar arquivo com strings a remover
echo 'inventario2025' > passwords.txt

# Remover senhas do histórico
git filter-repo --replace-text passwords.txt

# Force push (CUIDADO: reescreve histórico)
git push --force --all
```

### Opção 2: BFG Repo-Cleaner

```bash
# Baixar BFG
wget https://repo1.maven.org/maven2/com/madgag/bfg/1.14.0/bfg-1.14.0.jar

# Remover senhas
java -jar bfg-1.14.0.jar --replace-text passwords.txt

# Limpar e force push
git reflog expire --expire=now --all
git gc --prune=now --aggressive
git push --force --all
```

### Opção 3: Criar Novo Repositório (Mais Seguro)

Se o histórico contém muitas exposições:

1. Criar novo repositório limpo
2. Copiar código atual (sem histórico)
3. Fazer commit inicial sem credenciais
4. Migrar equipe para novo repositório
5. Arquivar repositório antigo

---

## Impacto e Riscos

### Riscos Atuais (Antes da Correção)
- 🔴 **CRÍTICO**: Senhas expostas no Git
- 🔴 **ALTO**: Comprometimento da keystore
- 🟡 **MÉDIO**: Impossibilidade de rotação segura
- 🟡 **MÉDIO**: Falha em auditorias de segurança

### Riscos da Correção
- 🟡 **MÉDIO**: Reescrever histórico do Git pode causar conflitos
- 🟢 **BAIXO**: Desenvolvedores precisam configurar arquivo local
- 🟢 **BAIXO**: CI/CD precisa ser reconfigurado

### Benefícios da Correção
- ✅ Credenciais não mais expostas no Git
- ✅ Rotação de senhas sem commits
- ✅ Conformidade com boas práticas de segurança
- ✅ Aprovação em auditorias de segurança
- ✅ Redução de risco de comprometimento

---

## Referências

- [Android Developers - Sign your app](https://developer.android.com/studio/publish/app-signing)
- [Gradle - Signing configurations](https://developer.android.com/studio/build/gradle-tips#sign-your-app)
- [OWASP - Hardcoded Credentials](https://owasp.org/www-community/vulnerabilities/Use_of_hard-coded_password)
- [Git - Removing sensitive data](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/removing-sensitive-data-from-a-repository)

