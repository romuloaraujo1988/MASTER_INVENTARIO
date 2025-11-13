# Configuração em Produção

## 📍 Localização dos Arquivos de Configuração

O sistema suporta **4 modos diferentes** de armazenamento de configuração, permitindo flexibilidade para diferentes cenários de implantação.

### Modos Disponíveis

| Modo | Localização | Uso Recomendado |
|------|-------------|-----------------|
| **USER_HOME** | `%LOCALAPPDATA%\SIHCP-Inventario\` (Win) ou `~/.config/sihcp-inventario/` (Linux) | Desenvolvimento, Desktop |
| **APP_DIR** | `./config/` | Aplicação portátil |
| **SYSTEM** | `C:\ProgramData\SIHCP-Inventario\` (Win) ou `/etc/sihcp-inventario/` (Linux) | Servidor compartilhado |
| **CUSTOM** | Definido por variável de ambiente | Ambientes específicos |

---

## 🏠 Modo 1: USER_HOME (Padrão)

### Localização

**Windows:**
```
C:\Users\[usuario]\AppData\Local\SIHCP-Inventario\
├── database-config.properties
└── sgbd-config.properties
```

**Alternativa Windows (se AppData não disponível):**
```
C:\Users\[usuario]\Documents\SIHCP-Inventario\
├── database-config.properties
└── sgbd-config.properties
```

**Linux/Mac:**
```
~/.config/sihcp-inventario/
├── database-config.properties
└── sgbd-config.properties
```

### Quando Usar

- ✅ Desenvolvimento local
- ✅ Aplicação desktop (um usuário por máquina)
- ✅ Configuração específica por usuário

### Vantagens

- ✅ Não requer permissões administrativas
- ✅ Isolado por usuário
- ✅ Fácil de limpar (deletar pasta do usuário)
- ✅ **Visível no Windows Explorer** (não é pasta oculta)
- ✅ Segue padrões do sistema operacional (AppData no Windows, .config no Linux)

### Desvantagens

- ❌ Não compartilhado entre usuários
- ❌ Perdido se usuário for deletado

### 🔄 Migração Automática

Se você tinha configurações na pasta antiga `~/.inventario/`, o sistema **migra automaticamente** para a nova localização na primeira execução:

```
Detectada configuração antiga em: C:\Users\usuario\.inventario
✓ Migrado: database-config.properties
✓ Migrado: sgbd-config.properties
✓ Migração concluída!
  Nova localização: C:\Users\usuario\AppData\Local\SIHCP-Inventario
  Você pode deletar a pasta antiga: C:\Users\usuario\.inventario
```

---

## 📦 Modo 2: APP_DIR (Portátil)

### Localização

```
[diretorio-da-aplicacao]/
├── sistema-inventario.jar
├── config/
│   ├── database-config.properties
│   └── sgbd-config.properties
└── lib/
```

### Quando Usar

- ✅ **Aplicação portátil** (pen drive, rede)
- ✅ **Instalação sem instalador** (copiar e executar)
- ✅ **Múltiplas instâncias** com configurações diferentes

### Como Ativar

**Opção 1: Criar pasta config/**
```bash
mkdir config
# O sistema detecta automaticamente
```

**Opção 2: Variável de ambiente**
```bash
# Windows
set INVENTARIO_CONFIG_MODE=APP_DIR
java -jar sistema-inventario.jar

# Linux/Mac
export INVENTARIO_CONFIG_MODE=APP_DIR
java -jar sistema-inventario.jar
```

**Opção 3: Propriedade do sistema**
```bash
java -Dinventario.config.mode=APP_DIR -jar sistema-inventario.jar
```

### Vantagens

- ✅ Totalmente portátil
- ✅ Backup simples (copiar pasta inteira)
- ✅ Múltiplas configurações (uma por pasta)

### Desvantagens

- ❌ Configuração visível (junto com aplicação)
- ❌ Pode ser deletada acidentalmente

---

## 🖥️ Modo 3: SYSTEM (Servidor)

### Localização

**Windows:**
```
C:\ProgramData\SIHCP-Inventario\
├── database-config.properties
└── sgbd-config.properties
```

**Linux:**
```
/etc/sihcp-inventario/
├── database-config.properties
└── sgbd-config.properties
```

### Quando Usar

- ✅ **Servidor compartilhado** (múltiplos usuários)
- ✅ **Instalação centralizada**
- ✅ **Configuração única** para todos

### Como Ativar

```bash
# Windows (como Administrador)
set INVENTARIO_CONFIG_MODE=SYSTEM
java -jar sistema-inventario.jar

# Linux (como root ou sudo)
export INVENTARIO_CONFIG_MODE=SYSTEM
sudo java -jar sistema-inventario.jar
```

### Criar Diretório Manualmente

**Windows (PowerShell como Admin):**
```powershell
New-Item -Path "C:\ProgramData\inventario" -ItemType Directory
icacls "C:\ProgramData\inventario" /grant Users:F
```

**Linux:**
```bash
sudo mkdir -p /etc/inventario
sudo chmod 755 /etc/inventario
sudo chown $USER:$USER /etc/inventario
```

### Vantagens

- ✅ Compartilhado entre todos os usuários
- ✅ Localização padrão do sistema
- ✅ Sobrevive a mudanças de usuário

### Desvantagens

- ❌ Requer permissões administrativas
- ❌ Mais difícil de limpar

---

## 🎯 Modo 4: CUSTOM (Customizado)

### Localização

Definida pela variável de ambiente `INVENTARIO_CONFIG_DIR`

### Quando Usar

- ✅ **Ambientes corporativos** com políticas específicas
- ✅ **Rede compartilhada** (NAS, servidor de arquivos)
- ✅ **Containers Docker**

### Como Ativar

**Windows:**
```cmd
set INVENTARIO_CONFIG_DIR=D:\Configs\Inventario
java -jar sistema-inventario.jar
```

**Linux/Mac:**
```bash
export INVENTARIO_CONFIG_DIR=/mnt/shared/inventario-config
java -jar sistema-inventario.jar
```

**Docker:**
```dockerfile
ENV INVENTARIO_CONFIG_DIR=/app/config
VOLUME /app/config
```

### Vantagens

- ✅ Máxima flexibilidade
- ✅ Pode apontar para rede compartilhada
- ✅ Ideal para containers

### Desvantagens

- ❌ Requer configuração manual
- ❌ Pode causar confusão se mal documentado

---

## 🚀 Recomendações por Cenário

### Desenvolvimento

```bash
# Usar padrão USER_HOME
# Nenhuma configuração necessária
```

### Produção - Desktop (Cliente Único)

```bash
# Opção 1: USER_HOME (padrão)
# Nenhuma configuração necessária

# Opção 2: APP_DIR (portátil)
mkdir config
# Copiar aplicação para pasta do cliente
```

### Produção - Servidor (Múltiplos Usuários)

```bash
# Linux
export INVENTARIO_CONFIG_MODE=SYSTEM
sudo mkdir -p /etc/inventario
sudo chmod 755 /etc/inventario

# Windows (PowerShell como Admin)
$env:INVENTARIO_CONFIG_MODE="SYSTEM"
New-Item -Path "C:\ProgramData\inventario" -ItemType Directory
```

### Produção - Rede Compartilhada

```bash
# Apontar para servidor de arquivos
export INVENTARIO_CONFIG_DIR=/mnt/servidor/inventario-config
# ou
export INVENTARIO_CONFIG_DIR=\\servidor\compartilhado\inventario-config
```

### Docker/Container

```dockerfile
FROM openjdk:21-jdk-slim

ENV INVENTARIO_CONFIG_MODE=APP_DIR
WORKDIR /app

COPY sistema-inventario.jar .
RUN mkdir config

VOLUME /app/config

CMD ["java", "-jar", "sistema-inventario.jar"]
```

---

## 📝 Script de Instalação em Produção

### Windows (install-producao.bat)

```batch
@echo off
echo Instalando Sistema de Inventario em Producao...

REM Criar diretorio de configuracao
mkdir "C:\ProgramData\inventario"

REM Copiar aplicacao
copy sistema-inventario.jar "C:\Program Files\Inventario\"

REM Configurar variavel de ambiente do sistema
setx INVENTARIO_CONFIG_MODE "SYSTEM" /M

REM Criar atalho
echo Criando atalho...
REM (adicionar comando para criar atalho)

echo Instalacao concluida!
echo Configure o banco via interface: Configurar Banco
pause
```

### Linux (install-producao.sh)

```bash
#!/bin/bash
echo "Instalando Sistema de Inventário em Produção..."

# Criar diretório de configuração
sudo mkdir -p /etc/inventario
sudo chmod 755 /etc/inventario

# Copiar aplicação
sudo cp sistema-inventario.jar /opt/inventario/

# Criar script de inicialização
cat > /usr/local/bin/inventario << 'EOF'
#!/bin/bash
export INVENTARIO_CONFIG_MODE=SYSTEM
java -jar /opt/inventario/sistema-inventario.jar "$@"
EOF

sudo chmod +x /usr/local/bin/inventario

# Criar desktop entry
cat > /usr/share/applications/inventario.desktop << 'EOF'
[Desktop Entry]
Name=Sistema de Inventário
Exec=/usr/local/bin/inventario
Icon=/opt/inventario/icon.png
Type=Application
Categories=Office;
EOF

echo "Instalação concluída!"
echo "Execute: inventario"
```

---

## 🔍 Verificar Configuração Atual

### Via Código

```java
import com.inventario.config.ConfigurationPaths;

public class VerificarConfig {
    public static void main(String[] args) {
        System.out.println(ConfigurationPaths.getConfigInfo());
    }
}
```

### Via Linha de Comando

```bash
java -cp sistema-inventario.jar com.inventario.config.ConfigurationPaths
```

### Saída Esperada

```
╔════════════════════════════════════════════════════════════════╗
║  Configuração de Caminhos                                      ║
╠════════════════════════════════════════════════════════════════╣
║  Modo: USER_HOME                                               ║
║  Diretório: C:\Users\usuario\.inventario                       ║
║  Banco: database-config.properties                             ║
║  SGBD: sgbd-config.properties                                  ║
╠════════════════════════════════════════════════════════════════╣
║  Variáveis de Ambiente:                                        ║
║  INVENTARIO_CONFIG_MODE: null                                  ║
║  INVENTARIO_CONFIG_DIR: null                                   ║
╚════════════════════════════════════════════════════════════════╝
```

---

## 🔐 Segurança em Produção

### Permissões Recomendadas

**Linux:**
```bash
# Diretório
sudo chmod 755 /etc/inventario

# Arquivos de configuração
sudo chmod 600 /etc/inventario/database-config.properties
sudo chmod 600 /etc/inventario/sgbd-config.properties

# Proprietário
sudo chown inventario:inventario /etc/inventario/*
```

**Windows:**
```powershell
# Remover herança
icacls "C:\ProgramData\inventario" /inheritance:r

# Dar permissão apenas para usuários específicos
icacls "C:\ProgramData\inventario" /grant "Administradores:F"
icacls "C:\ProgramData\inventario" /grant "SYSTEM:F"
icacls "C:\ProgramData\inventario" /grant "UsuarioApp:M"
```

### Backup

```bash
# Backup automático (Linux)
#!/bin/bash
BACKUP_DIR="/backup/inventario-config"
CONFIG_DIR="/etc/inventario"

mkdir -p "$BACKUP_DIR"
tar -czf "$BACKUP_DIR/config-$(date +%Y%m%d-%H%M%S).tar.gz" "$CONFIG_DIR"

# Manter apenas últimos 30 dias
find "$BACKUP_DIR" -name "config-*.tar.gz" -mtime +30 -delete
```

---

## 📋 Checklist de Implantação

### Antes da Implantação

- [ ] Decidir modo de configuração (USER_HOME, APP_DIR, SYSTEM, CUSTOM)
- [ ] Criar diretório de configuração
- [ ] Configurar permissões adequadas
- [ ] Testar acesso ao diretório
- [ ] Documentar localização para equipe

### Durante a Implantação

- [ ] Copiar aplicação para local definitivo
- [ ] Configurar variáveis de ambiente (se necessário)
- [ ] Executar aplicação pela primeira vez
- [ ] Configurar banco de dados via interface
- [ ] Testar conexão
- [ ] Verificar se configuração foi salva

### Após a Implantação

- [ ] Fazer backup da configuração
- [ ] Documentar credenciais (em local seguro)
- [ ] Testar recuperação de desastre
- [ ] Treinar usuários sobre configuração
- [ ] Monitorar logs de conexão

---

## 🆘 Troubleshooting

### Erro: "Permissão negada"

**Causa**: Sem permissão para criar/ler arquivo

**Solução**:
```bash
# Linux
sudo chmod 755 /etc/inventario
sudo chown $USER /etc/inventario

# Windows (PowerShell como Admin)
icacls "C:\ProgramData\inventario" /grant Users:F
```

### Erro: "Diretório não encontrado"

**Causa**: Diretório não foi criado

**Solução**:
```bash
# Criar manualmente
mkdir -p ~/.inventario  # USER_HOME
mkdir config            # APP_DIR
sudo mkdir /etc/inventario  # SYSTEM
```

### Configuração não é encontrada

**Causa**: Modo errado ou variável de ambiente não definida

**Solução**:
```bash
# Verificar modo atual
java -cp sistema-inventario.jar com.inventario.config.ConfigurationPaths

# Definir modo correto
export INVENTARIO_CONFIG_MODE=USER_HOME
```

---

**Versão**: 1.0.0  
**Data**: 12/11/2025  
**Autor**: Sistema de Inventário IFMT
