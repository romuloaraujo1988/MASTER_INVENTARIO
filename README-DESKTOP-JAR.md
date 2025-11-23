# 🖥️ Sistema de Inventário - Desktop JAR

## ✅ JAR Desktop Compilado com Sucesso!

### 📦 Arquivos Gerados

**Thin-JAR Desktop:**
- **Localização:** `target/mobile-server/sistema-inventario-2.0.0.jar`
- **Tamanho:** 1.33 MB
- **Tipo:** Aplicação Desktop Swing (thin-jar)

**Dependências:**
- **Localização:** `target/mobile-server/lib/`
- **Total:** 170+ bibliotecas JAR
- **Tamanho:** ~125 MB

---

## 🚀 Como Executar

### Opção 1: Script BAT (Recomendado)

```bash
# Executar o script
.\run-desktop.bat
```

O script:
- ✅ Verifica se Java está instalado
- ✅ Verifica se o JAR existe
- ✅ Configura o classpath corretamente
- ✅ Inicia a aplicação desktop

### Opção 2: Linha de Comando

```bash
# Navegar para o diretório
cd target\mobile-server

# Executar com classpath
java -cp "sistema-inventario-2.0.0.jar;lib/*" com.inventario.SistemaInventarioApplication
```

### Opção 3: Duplo Clique (Criar Atalho)

1. Criar arquivo `Sistema-Inventario.bat` com:
```batch
@echo off
cd /d "%~dp0target\mobile-server"
java -cp "sistema-inventario-2.0.0.jar;lib/*" com.inventario.SistemaInventarioApplication
```

2. Criar atalho do BAT na área de trabalho
3. Duplo clique para executar

---

## 📋 Requisitos

- **Java:** JDK 21 ou superior
- **Sistema:** Windows 10/11
- **Memória:** Mínimo 512 MB RAM
- **Disco:** ~130 MB para aplicação + libs

---

## 📁 Estrutura do Diretório

```
target/mobile-server/
├── sistema-inventario-2.0.0.jar  (1.33 MB - thin-jar)
├── lib/                           (125 MB - dependências)
│   ├── spring-boot-3.2.0.jar
│   ├── postgresql-42.6.0.jar
│   ├── poi-5.4.0.jar              (Excel)
│   ├── jfreechart-1.5.5.jar       (Gráficos)
│   └── ... (170+ JARs)
├── application.properties         (Configuração)
├── log4j2.xml                     (Logs)
└── configuracao_banco.json        (Banco de dados)
```

---

## 🔧 Configuração

### Banco de Dados

Editar `configuracao_banco.json` no diretório do usuário:
```
C:\Users\[SEU_USUARIO]\.inventario\configuracao_banco.json
```

Ou usar a interface gráfica:
1. Abrir aplicação
2. Menu: Configurações → Banco de Dados
3. Configurar PostgreSQL

### Logs

Logs são salvos em:
```
logs/sistema-inventario.log
```

---

## ✅ Vantagens do Thin-JAR

1. **Menor tamanho:** 1.33 MB vs 126 MB (fat-jar)
2. **Atualização fácil:** Trocar apenas o JAR principal
3. **Compartilhamento de libs:** Múltiplas versões podem usar mesmas libs
4. **Deploy mais rápido:** Apenas 1.33 MB para transferir
5. **Manutenção simplificada:** Atualizar libs individualmente

---

## 🐛 Troubleshooting

### Erro: "Java não encontrado"
**Solução:** Instalar JDK 21 ou superior
```bash
java -version
```

### Erro: "JAR não encontrado"
**Solução:** Compilar o projeto
```bash
.\mvnw.cmd clean package -DskipTests
```

### Erro: "Pasta lib não encontrada"
**Solução:** Recompilar com dependências
```bash
.\mvnw.cmd clean package -DskipTests
```

### Aplicação não abre
**Solução:** Verificar logs
```bash
type logs\sistema-inventario.log
```

---

## 📊 Funcionalidades Desktop

- ✅ Gestão de Patrimônios
- ✅ Controle de Inventários
- ✅ Relatórios (Excel, PDF)
- ✅ Gráficos e Estatísticas
- ✅ Gestão de Usuários
- ✅ Gestão de Salas e Setores
- ✅ Gestão de Responsáveis
- ✅ Histórico de Coletas
- ✅ Modo Offline (SQLite)
- ✅ Sincronização com Mobile

---

## 🔄 Recompilar

Para recompilar o JAR:

```bash
# Limpar e compilar
.\mvnw.cmd clean package -DskipTests

# Executar
.\run-desktop.bat
```

---

## 📝 Notas

- O JAR desktop usa a classe principal: `com.inventario.SistemaInventarioApplication`
- Todas as dependências devem estar na pasta `lib/`
- O classpath é configurado automaticamente pelo script
- A aplicação cria arquivos de configuração em `~/.inventario/`

---

**Versão:** 2.0.0  
**Data:** 22/11/2025  
**Status:** ✅ Pronto para uso!
