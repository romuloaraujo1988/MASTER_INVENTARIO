#!/bin/bash
# Script de Instalação do SIHCP - Sistema de Inventário
# Para Linux (Debian/Ubuntu, Fedora, Arch)

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Variáveis
APP_NAME="SIHCP-Inventario"
APP_VERSION="1.0.0"
INSTALL_DIR="/opt/sihcp-inventario"
BIN_DIR="/usr/local/bin"
DESKTOP_DIR="/usr/share/applications"
ICON_DIR="/usr/share/icons/hicolor/256x256/apps"
CONFIG_DIR="$HOME/.config/sihcp-inventario"

echo -e "${BLUE}╔════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  SIHCP - Sistema de Histórico e Coleta Patrimonial            ║${NC}"
echo -e "${BLUE}║  Instalador para Linux                                         ║${NC}"
echo -e "${BLUE}║  Versão: ${APP_VERSION}                                              ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Verificar se está rodando como root
if [ "$EUID" -ne 0 ]; then 
    echo -e "${RED}✗ Este script precisa ser executado como root${NC}"
    echo -e "${YELLOW}  Execute: sudo $0${NC}"
    exit 1
fi

# Função para detectar distribuição
detect_distro() {
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        DISTRO=$ID
    else
        DISTRO="unknown"
    fi
    echo -e "${BLUE}→ Distribuição detectada: $DISTRO${NC}"
}

# Função para verificar Java
check_java() {
    echo -e "${BLUE}→ Verificando instalação do Java...${NC}"
    
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
        echo -e "${GREEN}✓ Java encontrado (versão $JAVA_VERSION)${NC}"
        
        if [ "$JAVA_VERSION" -lt 21 ]; then
            echo -e "${YELLOW}⚠ Java 21 ou superior é recomendado${NC}"
            echo -e "${YELLOW}  Versão atual: $JAVA_VERSION${NC}"
        fi
        return 0
    else
        echo -e "${RED}✗ Java não encontrado${NC}"
        return 1
    fi
}

# Função para instalar Java
install_java() {
    echo -e "${YELLOW}→ Deseja instalar o Java agora? (s/n)${NC}"
    read -r response
    
    if [[ "$response" =~ ^[Ss]$ ]]; then
        case $DISTRO in
            ubuntu|debian)
                apt-get update
                apt-get install -y openjdk-21-jdk
                ;;
            fedora)
                dnf install -y java-21-openjdk
                ;;
            arch)
                pacman -S --noconfirm jdk-openjdk
                ;;
            *)
                echo -e "${RED}✗ Distribuição não suportada para instalação automática${NC}"
                echo -e "${YELLOW}  Por favor, instale o Java manualmente${NC}"
                exit 1
                ;;
        esac
        echo -e "${GREEN}✓ Java instalado com sucesso${NC}"
    else
        echo -e "${RED}✗ Java é necessário para executar o SIHCP${NC}"
        exit 1
    fi
}

# Função para criar diretórios
create_directories() {
    echo -e "${BLUE}→ Criando diretórios...${NC}"
    
    mkdir -p "$INSTALL_DIR"
    mkdir -p "$INSTALL_DIR/lib"
    mkdir -p "$INSTALL_DIR/docs"
    mkdir -p "$INSTALL_DIR/sql"
    mkdir -p "$CONFIG_DIR"
    
    echo -e "${GREEN}✓ Diretórios criados${NC}"
}

# Função para copiar arquivos
copy_files() {
    echo -e "${BLUE}→ Copiando arquivos...${NC}"
    
    # JAR principal
    if [ -f "../../target/sistema-inventario.jar" ]; then
        cp "../../target/sistema-inventario.jar" "$INSTALL_DIR/"
        echo -e "${GREEN}✓ JAR copiado${NC}"
    else
        echo -e "${RED}✗ JAR não encontrado em ../../target/${NC}"
        exit 1
    fi
    
    # Bibliotecas
    if [ -d "../../lib" ]; then
        cp -r ../../lib/* "$INSTALL_DIR/lib/" 2>/dev/null || true
        echo -e "${GREEN}✓ Bibliotecas copiadas${NC}"
    fi
    
    # Documentação
    if [ -d "../../DOCUMENTAÇÃO" ]; then
        cp -r ../../DOCUMENTAÇÃO/* "$INSTALL_DIR/docs/" 2>/dev/null || true
        echo -e "${GREEN}✓ Documentação copiada${NC}"
    fi
    
    # Scripts SQL
    if [ -d "../../sql" ]; then
        cp -r ../../sql/* "$INSTALL_DIR/sql/" 2>/dev/null || true
        echo -e "${GREEN}✓ Scripts SQL copiados${NC}"
    fi
    
    # Ícone
    if [ -f "icon.png" ]; then
        mkdir -p "$ICON_DIR"
        cp icon.png "$ICON_DIR/sihcp-inventario.png"
        echo -e "${GREEN}✓ Ícone copiado${NC}"
    fi
}

# Função para criar launcher
create_launcher() {
    echo -e "${BLUE}→ Criando launcher...${NC}"
    
    cat > "$BIN_DIR/sihcp-inventario" << 'EOF'
#!/bin/bash
# Launcher para SIHCP - Sistema de Inventário

INSTALL_DIR="/opt/sihcp-inventario"
JAR_FILE="$INSTALL_DIR/sistema-inventario.jar"

# Verificar se JAR existe
if [ ! -f "$JAR_FILE" ]; then
    echo "ERRO: Arquivo JAR não encontrado em $JAR_FILE"
    exit 1
fi

# Configurar JVM
JAVA_OPTS="-Xms256m -Xmx1024m"
JAVA_OPTS="$JAVA_OPTS -Dfile.encoding=UTF-8"

# Iniciar aplicação
java $JAVA_OPTS -jar "$JAR_FILE" "$@"
EOF
    
    chmod +x "$BIN_DIR/sihcp-inventario"
    echo -e "${GREEN}✓ Launcher criado em $BIN_DIR/sihcp-inventario${NC}"
}

# Função para criar desktop entry
create_desktop_entry() {
    echo -e "${BLUE}→ Criando entrada no menu...${NC}"
    
    cat > "$DESKTOP_DIR/sihcp-inventario.desktop" << EOF
[Desktop Entry]
Version=1.0
Type=Application
Name=SIHCP - Sistema de Inventário
Comment=Sistema de Histórico e Coleta Patrimonial
Exec=$BIN_DIR/sihcp-inventario
Icon=sihcp-inventario
Terminal=false
Categories=Office;Database;
Keywords=inventario;patrimonio;coleta;
StartupNotify=true
EOF
    
    chmod +x "$DESKTOP_DIR/sihcp-inventario.desktop"
    echo -e "${GREEN}✓ Entrada no menu criada${NC}"
}

# Função para configurar banco
configure_database() {
    echo ""
    echo -e "${YELLOW}╔════════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${YELLOW}║  Configuração do Banco de Dados                                ║${NC}"
    echo -e "${YELLOW}╚════════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "${BLUE}Deseja configurar o banco de dados agora? (s/n)${NC}"
    read -r response
    
    if [[ "$response" =~ ^[Ss]$ ]]; then
        echo ""
        read -p "Servidor (localhost): " db_host
        db_host=${db_host:-localhost}
        
        read -p "Porta (5432): " db_port
        db_port=${db_port:-5432}
        
        read -p "Nome do Banco (sispatrimonio): " db_name
        db_name=${db_name:-sispatrimonio}
        
        read -p "Usuário (postgres): " db_user
        db_user=${db_user:-postgres}
        
        read -sp "Senha: " db_pass
        echo ""
        
        # Criar arquivo de configuração
        cat > "$CONFIG_DIR/database-config.properties" << EOF
# Database Configuration - Created by installer
host=$db_host
port=$db_port
database=$db_name
username=$db_user
password=$db_pass
EOF
        
        chmod 600 "$CONFIG_DIR/database-config.properties"
        chown $SUDO_USER:$SUDO_USER "$CONFIG_DIR/database-config.properties"
        
        echo -e "${GREEN}✓ Configuração salva em $CONFIG_DIR/database-config.properties${NC}"
    else
        echo -e "${YELLOW}⚠ Você pode configurar depois através da interface${NC}"
    fi
}

# Função principal
main() {
    detect_distro
    
    if ! check_java; then
        install_java
    fi
    
    create_directories
    copy_files
    create_launcher
    create_desktop_entry
    configure_database
    
    # Ajustar permissões
    chown -R root:root "$INSTALL_DIR"
    chmod -R 755 "$INSTALL_DIR"
    
    if [ -d "$CONFIG_DIR" ]; then
        chown -R $SUDO_USER:$SUDO_USER "$CONFIG_DIR"
        chmod -R 700 "$CONFIG_DIR"
    fi
    
    echo ""
    echo -e "${GREEN}╔════════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║  ✓ Instalação concluída com sucesso!                          ║${NC}"
    echo -e "${GREEN}╚════════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "${BLUE}Para executar:${NC}"
    echo -e "  ${YELLOW}→ Via menu: Aplicativos > Office > SIHCP${NC}"
    echo -e "  ${YELLOW}→ Via terminal: sihcp-inventario${NC}"
    echo ""
    echo -e "${BLUE}Arquivos instalados em:${NC}"
    echo -e "  ${YELLOW}→ Aplicação: $INSTALL_DIR${NC}"
    echo -e "  ${YELLOW}→ Configuração: $CONFIG_DIR${NC}"
    echo ""
}

# Executar
main
