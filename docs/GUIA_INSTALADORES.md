# Guia de Instaladores do SIHCP

## 📦 Visão Geral

O SIHCP possui instaladores profissionais para **Windows** e **Linux**, facilitando a instalação para usuários finais.

### Instaladores Disponíveis

| Plataforma | Tipo | Arquivo | Tamanho Aprox. |
|------------|------|---------|----------------|
| **Windows** | Inno Setup | `SIHCP-Inventario-Setup-1.0.0.exe` | ~50 MB |
| **Linux** | Shell Script | `install.sh` | ~40 MB |
| **Portátil** | ZIP | `SIHCP-Inventario-Portable.zip` | ~40 MB |

---

## 🪟 Instalador Windows

### Características

- ✅ Interface gráfica moderna
- ✅ Verificação automática de Java
- ✅ Configuração do banco durante instalação
- ✅ Atalhos automáticos (Desktop, Menu Iniciar)
- ✅ Desinstalador integrado
- ✅ Registro no Windows (associação de arquivos)

### Pré-requisitos

**Para Criar o Instalador:**
- Inno Setup 6.x ([Download](https://jrsoftware.org/isdl.php))
- Java 21+ instalado
- Projeto compilado (`mvn clean package`)

**Para Instalar:**
- Windows 7 ou superior
- Java 21+ (instalador pode baixar automaticamente)
- 100 MB de espaço em disco

### Como Criar o Instalador

#### 1. Compilar o Projeto

```bash
cd MASTER_INVENTARIO
mvn clean package -DskipTests
```

#### 2. Preparar Arquivos

```bash
# Criar estrutura
mkdir -p instalador\windows\output

# Copiar JAR
copy target\sistema-inventario.jar instalador\windows\

# Copiar bibliotecas (se houver)
xcopy /E /I lib instalador\windows\lib

# Copiar ícone (criar ou usar existente)
# Colocar icon.ico em instalador\windows\
```

#### 3. Compilar com Inno Setup

**Opção A: Interface Gráfica**
1. Abra o Inno Setup Compiler
2. Abra o arquivo `instalador\windows\sihcp-setup.iss`
3. Clique em "Build" → "Compile"
4. Instalador será criado em `dist\`

**Opção B: Linha de Comando**
```cmd
"C:\Program Files (x86)\Inno Setup 6\ISCC.exe" instalador\windows\sihcp-setup.iss
```

#### 4. Testar o Instalador

```cmd
# Executar instalador
dist\SIHCP-Inventario-Setup-1.0.0.exe

# Verificar instalação
dir "C:\Program Files\SIHCP-Inventario"
dir "%LOCALAPPDATA%\SIHCP-Inventario"
```

### Estrutura Pós-Instalação (Windows)

```
C:\Program Files\SIHCP-Inventario\
├── SIHCP-Inventario.exe          # Launcher
├── sistema-inventario.jar        # Aplicação
├── icon.ico                      # Ícone
├── lib\                          # Bibliotecas
├── docs\                         # Documentação
└── sql\                          # Scripts SQL

C:\Users\[usuario]\AppData\Local\SIHCP-Inventario\
├── database-config.properties    # Configuração do banco
└── sgbd-config.properties        # Configuração do SGBD
```

### Recursos do Instalador Windows

#### Verificação de Java

O instalador verifica se Java está instalado:
- ✅ Se encontrado: Continua instalação
- ❌ Se não encontrado: Oferece download

#### Configuração Durante Instalação

Tela de configuração permite:
- Servidor do banco
- Porta
- Nome do banco
- Usuário
- Senha (criptografada automaticamente)

#### Atalhos Criados

- **Menu Iniciar**:
  - SIHCP - Sistema de Inventário
  - Configurar Banco de Dados
  - Documentação
  - Desinstalar

- **Área de Trabalho** (opcional):
  - SIHCP - Sistema de Inventário

- **Barra de Tarefas** (opcional):
  - Atalho rápido

#### Desinstalação

```
Painel de Controle → Programas → Desinstalar um programa
→ SIHCP - Sistema de Inventário → Desinstalar
```

Opções:
- Manter configurações (padrão)
- Remover tudo

---

## 🐧 Instalador Linux

### Características

- ✅ Suporte a múltiplas distribuições (Debian, Ubuntu, Fedora, Arch)
- ✅ Instalação automática de Java (se necessário)
- ✅ Configuração do banco durante instalação
- ✅ Entrada no menu de aplicativos
- ✅ Launcher global (`sihcp-inventario`)
- ✅ Script de desinstalação incluído

### Pré-requisitos

**Para Instalar:**
- Linux (Debian/Ubuntu, Fedora, Arch, ou similar)
- Permissões de root (sudo)
- Java 21+ (instalador pode instalar automaticamente)
- 100 MB de espaço em disco

### Como Instalar

#### 1. Baixar Instalador

```bash
# Opção 1: Clonar repositório
git clone https://github.com/ifmt/sihcp-inventario.git
cd sihcp-inventario

# Opção 2: Baixar release
wget https://github.com/ifmt/sihcp-inventario/releases/download/v1.0.0/sihcp-linux-installer.tar.gz
tar -xzf sihcp-linux-installer.tar.gz
cd sihcp-linux-installer
```

#### 2. Compilar Projeto (se necessário)

```bash
mvn clean package -DskipTests
```

#### 3. Executar Instalador

```bash
cd instalador/linux
chmod +x install.sh
sudo ./install.sh
```

#### 4. Seguir Assistente

O instalador irá:
1. Detectar distribuição Linux
2. Verificar/instalar Java
3. Criar diretórios
4. Copiar arquivos
5. Criar launcher
6. Criar entrada no menu
7. Configurar banco (opcional)

### Estrutura Pós-Instalação (Linux)

```
/opt/sihcp-inventario/
├── sistema-inventario.jar        # Aplicação
├── lib/                          # Bibliotecas
├── docs/                         # Documentação
└── sql/                          # Scripts SQL

/usr/local/bin/
└── sihcp-inventario              # Launcher global

/usr/share/applications/
└── sihcp-inventario.desktop      # Entrada no menu

/usr/share/icons/hicolor/256x256/apps/
└── sihcp-inventario.png          # Ícone

~/.config/sihcp-inventario/
├── database-config.properties    # Configuração do banco
└── sgbd-config.properties        # Configuração do SGBD
```

### Como Executar (Linux)

**Opção 1: Menu de Aplicativos**
```
Aplicativos → Office → SIHCP - Sistema de Inventário
```

**Opção 2: Terminal**
```bash
sihcp-inventario
```

**Opção 3: Caminho Completo**
```bash
/usr/local/bin/sihcp-inventario
```

### Desinstalação (Linux)

```bash
cd instalador/linux
chmod +x uninstall.sh
sudo ./uninstall.sh
```

Opções:
- Remover aplicação (sempre)
- Manter/remover configurações (escolha do usuário)

---

## 📦 Versão Portátil (Todas as Plataformas)

### Características

- ✅ Não requer instalação
- ✅ Pode rodar de pen drive
- ✅ Configuração junto com aplicação
- ✅ Ideal para testes ou uso temporário

### Como Criar

```bash
# Compilar projeto
mvn clean package -DskipTests

# Criar estrutura portátil
mkdir SIHCP-Portable
cd SIHCP-Portable

# Copiar arquivos
cp ../target/sistema-inventario.jar .
cp -r ../lib .
mkdir config
mkdir docs
cp -r ../DOCUMENTAÇÃO/* docs/

# Criar launcher Windows
echo @echo off > SIHCP.bat
echo java -jar sistema-inventario.jar >> SIHCP.bat

# Criar launcher Linux
echo "#!/bin/bash" > sihcp.sh
echo "java -jar sistema-inventario.jar" >> sihcp.sh
chmod +x sihcp.sh

# Criar README
cat > README.txt << EOF
SIHCP - Sistema de Inventário (Versão Portátil)

Para executar:
- Windows: Clique duas vezes em SIHCP.bat
- Linux/Mac: Execute ./sihcp.sh

Configuração:
- Configure o banco via interface
- Configurações salvas em: ./config/

Requisitos:
- Java 21 ou superior
EOF

# Compactar
cd ..
zip -r SIHCP-Inventario-Portable.zip SIHCP-Portable/
```

### Estrutura Portátil

```
SIHCP-Portable/
├── sistema-inventario.jar        # Aplicação
├── SIHCP.bat                     # Launcher Windows
├── sihcp.sh                      # Launcher Linux/Mac
├── README.txt                    # Instruções
├── config/                       # Configurações (criado automaticamente)
├── lib/                          # Bibliotecas
└── docs/                         # Documentação
```

---

## 🔧 Personalização dos Instaladores

### Alterar Informações

**Windows (sihcp-setup.iss):**
```pascal
#define MyAppName "SIHCP - Sistema de Inventário"
#define MyAppVersion "1.0.0"
#define MyAppPublisher "Instituto Federal de Mato Grosso - IFMT"
#define MyAppURL "https://ifmt.edu.br"
```

**Linux (install.sh):**
```bash
APP_NAME="SIHCP-Inventario"
APP_VERSION="1.0.0"
INSTALL_DIR="/opt/sihcp-inventario"
```

### Adicionar Ícone Personalizado

**Windows:**
1. Criar `icon.ico` (256x256 pixels)
2. Colocar em `instalador/windows/icon.ico`
3. Recompilar instalador

**Linux:**
1. Criar `icon.png` (256x256 pixels)
2. Colocar em `instalador/linux/icon.png`
3. Executar instalador

### Adicionar Arquivos Extras

**Windows (sihcp-setup.iss):**
```pascal
[Files]
Source: "meu-arquivo.txt"; DestDir: "{app}"; Flags: ignoreversion
```

**Linux (install.sh):**
```bash
# Adicionar em copy_files()
cp meu-arquivo.txt "$INSTALL_DIR/"
```

---

## 📊 Comparação dos Instaladores

| Recurso | Windows | Linux | Portátil |
|---------|---------|-------|----------|
| **Interface Gráfica** | ✅ Sim | ❌ Terminal | ❌ Manual |
| **Verificação Java** | ✅ Automática | ✅ Automática | ❌ Manual |
| **Instalação Java** | ⚠️ Link | ✅ Automática | ❌ Manual |
| **Config. Durante Install** | ✅ Sim | ✅ Sim | ❌ Depois |
| **Atalhos Automáticos** | ✅ Sim | ✅ Sim | ❌ Manual |
| **Desinstalador** | ✅ Integrado | ✅ Script | ❌ Deletar pasta |
| **Tamanho** | ~50 MB | ~40 MB | ~40 MB |
| **Requer Admin** | ✅ Sim | ✅ Sim | ❌ Não |

---

## 🚀 Distribuição

### Para Usuários Finais

**Recomendado:**
- Windows: Instalador `.exe`
- Linux: Script `.sh` + instruções
- Portátil: `.zip` para testes

### Para Desenvolvedores

**Recomendado:**
- Clone do repositório
- Build manual com Maven
- Configuração via interface

### Para Servidores

**Recomendado:**
- Linux: Script de instalação
- Configuração via arquivo
- Modo SYSTEM para múltiplos usuários

---

## 📝 Checklist de Release

### Antes de Criar Instaladores

- [ ] Código compilado sem erros
- [ ] Testes passando
- [ ] Versão atualizada em todos os arquivos
- [ ] Documentação atualizada
- [ ] Ícones criados (256x256)
- [ ] README.md atualizado

### Criar Instaladores

- [ ] Compilar projeto: `mvn clean package`
- [ ] Criar instalador Windows (Inno Setup)
- [ ] Testar instalador Windows
- [ ] Criar pacote Linux (tar.gz)
- [ ] Testar instalador Linux
- [ ] Criar versão portátil (zip)
- [ ] Testar versão portátil

### Distribuir

- [ ] Upload para GitHub Releases
- [ ] Criar release notes
- [ ] Atualizar documentação online
- [ ] Notificar usuários
- [ ] Atualizar site (se houver)

---

## 🆘 Troubleshooting

### Windows: "Java não encontrado"

**Solução:**
1. Baixar Java: https://adoptium.net/
2. Instalar Java
3. Executar instalador novamente

### Linux: "Permissão negada"

**Solução:**
```bash
chmod +x install.sh
sudo ./install.sh
```

### Portátil: "Não inicia"

**Solução:**
1. Verificar se Java está instalado: `java -version`
2. Executar via terminal: `java -jar sistema-inventario.jar`
3. Verificar mensagens de erro

### Instalador não cria atalhos

**Windows:**
- Executar como Administrador
- Verificar se instalação completou

**Linux:**
- Verificar permissões: `ls -la /usr/share/applications/`
- Atualizar cache: `update-desktop-database`

---

## 📚 Recursos Adicionais

- [Inno Setup Documentation](https://jrsoftware.org/ishelp/)
- [Debian Packaging Guide](https://www.debian.org/doc/manuals/maint-guide/)
- [FPM - Package Builder](https://github.com/jordansissel/fpm)
- [Launch4j - Java Launcher](http://launch4j.sourceforge.net/)

---

**Versão**: 1.0.0  
**Data**: 12/11/2025  
**Autor**: Sistema de Inventário IFMT
