# 📦 Thin JAR - Aplicação Desktop Swing

## ✅ Build Concluído com Sucesso

O JAR thin (leve) foi criado com sucesso usando o profile `thin-jar`.

---

## 📁 Estrutura Criada

```
target/
├── sistema-inventario-2.0.0.jar    # JAR principal (apenas classes do projeto)
└── lib/                             # Dependências externas (150+ JARs)
    ├── spring-boot-3.2.0.jar
    ├── postgresql-42.6.0.jar
    ├── poi-5.4.0.jar
    ├── jfreechart-1.5.5.jar
    └── ... (todas as dependências)
```

---

## 🎯 Vantagens do Thin JAR

### Comparação com Fat JAR

| Característica | Fat JAR | Thin JAR |
|----------------|---------|----------|
| Tamanho do JAR principal | ~150 MB | ~2-5 MB |
| Dependências | Embutidas | Pasta lib/ |
| Tempo de build | Lento | Rápido |
| Atualização | Rebuild completo | Apenas JAR principal |
| Distribuição | 1 arquivo | JAR + pasta lib/ |

### Benefícios

✅ **Build mais rápido** - Não precisa empacotar todas as dependências  
✅ **JAR menor** - Apenas classes do projeto  
✅ **Atualizações rápidas** - Só recompila o código do projeto  
✅ **Fácil debug** - Dependências separadas e visíveis  
✅ **Compatível com IDEs** - NetBeans, Eclipse, IntelliJ  

---

## 🚀 Como Executar

### Opção 1: Script Automático (Recomendado)

```bash
# Windows
run-desktop-thin.bat
```

### Opção 2: Linha de Comando

```bash
# Navegar para a pasta target
cd target

# Executar o JAR
java -jar sistema-inventario-2.0.0.jar

# Voltar para raiz
cd ..
```

### Opção 3: Com Parâmetros JVM

```bash
cd target
java -Xms256m -Xmx1g -XX:+UseG1GC -jar sistema-inventario-2.0.0.jar
cd ..
```

---

## 🔧 Como Fazer o Build

### Build Completo

```bash
# Windows
.\mvnw.cmd clean package -P thin-jar -DskipTests

# Linux/Mac
./mvnw clean package -P thin-jar -DskipTests
```

### Build Rápido (sem limpar)

```bash
# Windows
.\mvnw.cmd package -P thin-jar -DskipTests

# Linux/Mac
./mvnw package -P thin-jar -DskipTests
```

### Build com Testes

```bash
# Windows
.\mvnw.cmd clean package -P thin-jar

# Linux/Mac
./mvnw clean package -P thin-jar
```

---

## 📦 Como Distribuir

### Opção 1: Copiar Pasta Target

```bash
# Copiar toda a pasta target para o destino
xcopy /E /I target C:\Destino\SistemaInventario
```

### Opção 2: Criar ZIP

```bash
# Criar arquivo ZIP com JAR + lib
powershell Compress-Archive -Path target\sistema-inventario-2.0.0.jar, target\lib -DestinationPath SistemaInventario.zip
```

### Opção 3: Instalador (Avançado)

Use ferramentas como:
- **jpackage** (Java 14+)
- **Launch4j** (criar .exe)
- **Inno Setup** (instalador Windows)

---

## 🔍 Verificar Build

### Verificar JAR Principal

```bash
# Ver conteúdo do JAR
jar tf target\sistema-inventario-2.0.0.jar

# Ver MANIFEST.MF
jar xf target\sistema-inventario-2.0.0.jar META-INF/MANIFEST.MF
type META-INF\MANIFEST.MF
```

### Verificar Dependências

```bash
# Contar JARs na pasta lib
dir /b target\lib | find /c ".jar"

# Listar todas as dependências
dir /b target\lib
```

### Verificar Tamanho

```bash
# Tamanho do JAR principal
dir target\sistema-inventario-2.0.0.jar

# Tamanho total (JAR + lib)
dir /s target\sistema-inventario-2.0.0.jar target\lib
```

---

## 🛠️ Troubleshooting

### Problema: JAR não encontrado

**Erro:**
```
ERRO: JAR nao encontrado!
```

**Solução:**
```bash
# Fazer build novamente
.\mvnw.cmd clean package -P thin-jar -DskipTests
```

### Problema: Pasta lib não encontrada

**Erro:**
```
ERRO: Pasta lib nao encontrada!
```

**Solução:**
```bash
# Verificar se o profile thin-jar foi usado
.\mvnw.cmd clean package -P thin-jar -DskipTests
```

### Problema: ClassNotFoundException

**Erro:**
```
java.lang.ClassNotFoundException: com.inventario.SistemaInventarioApplication
```

**Solução:**
```bash
# Verificar MANIFEST.MF
jar xf target\sistema-inventario-2.0.0.jar META-INF/MANIFEST.MF
type META-INF\MANIFEST.MF

# Deve conter:
# Main-Class: com.inventario.SistemaInventarioApplication
# Class-Path: lib/spring-boot-3.2.0.jar lib/...
```

### Problema: NoClassDefFoundError

**Erro:**
```
java.lang.NoClassDefFoundError: org/springframework/boot/SpringApplication
```

**Solução:**
```bash
# Verificar se a pasta lib está no mesmo diretório do JAR
dir target

# Deve mostrar:
# sistema-inventario-2.0.0.jar
# lib (pasta)
```

---

## 📊 Comparação de Tamanhos

### Thin JAR (Profile thin-jar)

```
sistema-inventario-2.0.0.jar:  ~3 MB
lib/ (150 JARs):               ~147 MB
TOTAL:                         ~150 MB
```

### Fat JAR (Profile fat-jar)

```
sistema-inventario-2.0.0-exec.jar:  ~150 MB
TOTAL:                              ~150 MB
```

### Vantagem do Thin JAR

- **Atualização:** Apenas 3 MB ao invés de 150 MB
- **Build:** 5-10 segundos ao invés de 30-60 segundos
- **Debug:** Fácil ver e substituir dependências

---

## 🎯 Quando Usar Cada Tipo

### Use Thin JAR quando:

✅ Desenvolvimento ativo  
✅ Atualizações frequentes  
✅ Distribuição em rede local  
✅ Múltiplas versões do app  
✅ Debug de dependências  

### Use Fat JAR quando:

✅ Distribuição para usuários finais  
✅ Deploy em servidor único  
✅ Simplicidade (1 arquivo só)  
✅ Sem acesso à pasta lib  

---

## 📝 Estrutura do MANIFEST.MF

O arquivo `META-INF/MANIFEST.MF` do thin JAR contém:

```
Manifest-Version: 1.0
Main-Class: com.inventario.SistemaInventarioApplication
Class-Path: lib/spring-boot-3.2.0.jar lib/spring-boot-autoconfigure-3.2.0.jar
 lib/postgresql-42.6.0.jar lib/poi-5.4.0.jar lib/jfreechart-1.5.5.jar
 ... (todas as 150+ dependências)
Implementation-Title: Sistema de Coleta de Inventário
Implementation-Version: 2.0.0
Built-By: IFMT
```

---

## 🚀 Próximos Passos

### 1. Testar a Aplicação

```bash
# Executar e testar todas as funcionalidades
run-desktop-thin.bat
```

### 2. Criar Instalador (Opcional)

```bash
# Usar jpackage (Java 14+)
jpackage --input target --main-jar sistema-inventario-2.0.0.jar --name "Sistema Inventario" --type exe
```

### 3. Distribuir

```bash
# Criar ZIP para distribuição
powershell Compress-Archive -Path target\sistema-inventario-2.0.0.jar, target\lib, run-desktop-thin.bat -DestinationPath SistemaInventario-v2.0.0.zip
```

---

## ✅ Checklist de Distribuição

- [ ] Build concluído sem erros
- [ ] JAR principal criado (target/sistema-inventario-2.0.0.jar)
- [ ] Pasta lib criada com todas as dependências
- [ ] Script de execução testado (run-desktop-thin.bat)
- [ ] Aplicação inicia corretamente
- [ ] Todas as funcionalidades testadas
- [ ] Documentação incluída
- [ ] ZIP criado para distribuição

---

**Build concluído com sucesso!** 🎉

**Versão:** 2.0.0  
**Data:** 23/11/2025  
**Tipo:** Thin JAR (Desktop Swing)  
**Status:** ✅ Pronto para uso
