#!/bin/bash
# Script de Build para Produção - SIHCP Modular
# Versão: 2.7.0
# Data: 01/05/2026

set -e

# Cores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Parâmetros
SKIP_TESTS=false
BUILD_DESKTOP=false
BUILD_SERVER=false
BUILD_ALL=true

# Parse argumentos
while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-tests)
            SKIP_TESTS=true
            shift
            ;;
        --desktop)
            BUILD_DESKTOP=true
            BUILD_ALL=false
            shift
            ;;
        --server)
            BUILD_SERVER=true
            BUILD_ALL=false
            shift
            ;;
        *)
            echo "Uso: $0 [--skip-tests] [--desktop] [--server]"
            exit 1
            ;;
    esac
done

echo -e "${CYAN}========================================"
echo "  SIHCP - Build para Produção Modular  "
echo -e "========================================${NC}"
echo ""

# Verificar Maven
if [ ! -f "./mvnw" ]; then
    echo -e "${RED}ERRO: mvnw não encontrado!${NC}"
    exit 1
fi

chmod +x ./mvnw

# Timestamp
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
echo -e "${YELLOW}Timestamp: $TIMESTAMP${NC}"
echo ""

# Determinar o que buildar
BUILD_CORE=$BUILD_ALL
if [ "$BUILD_DESKTOP" = true ] || [ "$BUILD_SERVER" = true ]; then
    BUILD_CORE=true
fi
if [ "$BUILD_ALL" = true ]; then
    BUILD_DESKTOP=true
    BUILD_SERVER=true
fi

# Parâmetros Maven
MVN_PARAMS="clean package"
if [ "$SKIP_TESTS" = true ]; then
    MVN_PARAMS="$MVN_PARAMS -DskipTests"
fi

# ========================================
# Fase 1: Build do Core
# ========================================
if [ "$BUILD_CORE" = true ]; then
    echo -e "${GREEN}========================================"
    echo "Fase 1: Building sihcp-core..."
    echo -e "========================================${NC}"
    
    cd sihcp-core
    ../mvnw $MVN_PARAMS
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}ERRO: Build do sihcp-core falhou!${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✓ sihcp-core build concluído com sucesso!${NC}"
    echo ""
    cd ..
fi

# ========================================
# Fase 2: Build do Desktop
# ========================================
if [ "$BUILD_DESKTOP" = true ]; then
    echo -e "${GREEN}========================================"
    echo "Fase 2: Building sihcp-desktop..."
    echo -e "========================================${NC}"
    
    cd sihcp-desktop
    ../mvnw $MVN_PARAMS
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}ERRO: Build do sihcp-desktop falhou!${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✓ sihcp-desktop build concluído com sucesso!${NC}"
    echo ""
    cd ..
fi

# ========================================
# Fase 3: Build do Server
# ========================================
if [ "$BUILD_SERVER" = true ]; then
    echo -e "${GREEN}========================================"
    echo "Fase 3: Building sihcp-server..."
    echo -e "========================================${NC}"
    
    cd sihcp-server
    ../mvnw $MVN_PARAMS
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}ERRO: Build do sihcp-server falhou!${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✓ sihcp-server build concluído com sucesso!${NC}"
    echo ""
    cd ..
fi

# ========================================
# Fase 4: Criar estrutura de distribuição
# ========================================
echo -e "${CYAN}========================================"
echo "Fase 4: Criando pacotes de distribuição..."
echo -e "========================================${NC}"

DIST_DIR="dist/sihcp-$TIMESTAMP"
mkdir -p "$DIST_DIR"

# Desktop
if [ "$BUILD_DESKTOP" = true ]; then
    echo -e "${YELLOW}Empacotando Desktop...${NC}"
    
    DESKTOP_DIR="$DIST_DIR/desktop"
    mkdir -p "$DESKTOP_DIR"
    
    # Copiar JAR principal
    cp sihcp-desktop/target/sihcp-desktop-2.7.0.jar "$DESKTOP_DIR/"
    
    # Copiar dependências
    cp -r sihcp-desktop/target/lib "$DESKTOP_DIR/"
    
    # Criar script de inicialização
    cat > "$DESKTOP_DIR/iniciar-desktop.sh" << 'EOF'
#!/bin/bash
echo "========================================"
echo "  SIHCP Desktop - Sistema de Inventario"
echo "========================================"
echo ""

java -Xms512m -Xmx2g -jar sihcp-desktop-2.7.0.jar

if [ $? -ne 0 ]; then
    echo ""
    echo "ERRO: Falha ao iniciar a aplicacao!"
    read -p "Pressione Enter para continuar..."
fi
EOF
    
    chmod +x "$DESKTOP_DIR/iniciar-desktop.sh"
    
    # Criar README
    cat > "$DESKTOP_DIR/README.md" << EOF
# SIHCP Desktop - Aplicação Swing

## Requisitos
- Java 17 ou superior
- PostgreSQL 12+

## Instalação

1. Extrair todos os arquivos para um diretório
2. Configurar banco de dados em ~/configuracao_banco.json
3. Executar o script de inicialização

## Execução

\`\`\`bash
chmod +x iniciar-desktop.sh
./iniciar-desktop.sh
\`\`\`

Ou manualmente:
\`\`\`bash
java -jar sihcp-desktop-2.7.0.jar
\`\`\`

## Estrutura
\`\`\`
desktop/
├── sihcp-desktop-2.7.0.jar    # JAR principal
├── lib/                        # Dependências
└── iniciar-desktop.sh          # Script de inicialização
\`\`\`

## Versão
2.7.0 - Build: $TIMESTAMP
EOF
    
    echo -e "${GREEN}✓ Desktop empacotado em: $DESKTOP_DIR${NC}"
fi

# Server
if [ "$BUILD_SERVER" = true ]; then
    echo -e "${YELLOW}Empacotando Server...${NC}"
    
    SERVER_DIR="$DIST_DIR/server"
    mkdir -p "$SERVER_DIR"
    
    # Copiar JAR
    cp sihcp-server/target/sihcp-server-2.7.0.jar "$SERVER_DIR/"
    
    # Copiar configurações
    cp sihcp-server/src/main/resources/application.properties "$SERVER_DIR/"
    cp sihcp-server/src/main/resources/application-mobile.properties "$SERVER_DIR/"
    
    # Criar script de inicialização
    cat > "$SERVER_DIR/iniciar-server.sh" << 'EOF'
#!/bin/bash
echo "========================================"
echo "  SIHCP Server - API REST Mobile"
echo "========================================"
echo ""

JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
SPRING_OPTS="--spring.profiles.active=mobile --server.port=8081"

java $JAVA_OPTS -jar sihcp-server-2.7.0.jar $SPRING_OPTS

if [ $? -ne 0 ]; then
    echo ""
    echo "ERRO: Falha ao iniciar o servidor!"
    read -p "Pressione Enter para continuar..."
fi
EOF
    
    chmod +x "$SERVER_DIR/iniciar-server.sh"
    
    # Criar systemd service
    cat > "$SERVER_DIR/sihcp-server.service" << 'EOF'
[Unit]
Description=SIHCP Server - API REST Mobile
After=network.target postgresql.service

[Service]
Type=simple
User=sihcp
WorkingDirectory=/opt/sihcp-server
ExecStart=/usr/bin/java -Xms512m -Xmx2g -XX:+UseG1GC -jar /opt/sihcp-server/sihcp-server-2.7.0.jar --spring.profiles.active=mobile --server.port=8081
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF
    
    # Criar README
    cat > "$SERVER_DIR/README.md" << EOF
# SIHCP Server - API REST Mobile

## Requisitos
- Java 17 ou superior
- PostgreSQL 12+

## Instalação

1. Extrair todos os arquivos para um diretório
2. Configurar banco de dados em ~/configuracao_banco.json
3. Ajustar application-mobile.properties se necessário
4. Executar o script de inicialização

## Execução

\`\`\`bash
chmod +x iniciar-server.sh
./iniciar-server.sh
\`\`\`

Ou manualmente:
\`\`\`bash
java -jar sihcp-server-2.7.0.jar --spring.profiles.active=mobile --server.port=8081
\`\`\`

## Instalação como Serviço (Linux)

\`\`\`bash
# Copiar arquivos para /opt
sudo mkdir -p /opt/sihcp-server
sudo cp sihcp-server-2.7.0.jar /opt/sihcp-server/

# Criar usuário
sudo useradd -r -s /bin/false sihcp

# Copiar service file
sudo cp sihcp-server.service /etc/systemd/system/

# Habilitar e iniciar
sudo systemctl daemon-reload
sudo systemctl enable sihcp-server
sudo systemctl start sihcp-server

# Verificar status
sudo systemctl status sihcp-server
\`\`\`

## Endpoints Principais

- Health Check: http://localhost:8081/api/mobile/health
- Swagger UI: http://localhost:8081/swagger-ui.html
- API Docs: http://localhost:8081/v3/api-docs

## Estrutura
\`\`\`
server/
├── sihcp-server-2.7.0.jar           # Fat JAR
├── application.properties            # Config base
├── application-mobile.properties     # Config mobile
├── iniciar-server.sh                 # Script inicialização
└── sihcp-server.service              # Systemd service
\`\`\`

## Versão
2.7.0 - Build: $TIMESTAMP
EOF
    
    echo -e "${GREEN}✓ Server empacotado em: $SERVER_DIR${NC}"
fi

# ========================================
# Fase 5: Criar arquivo TAR.GZ
# ========================================
echo ""
echo -e "${CYAN}========================================"
echo "Fase 5: Criando arquivo TAR.GZ..."
echo -e "========================================${NC}"

TAR_FILE="dist/sihcp-$TIMESTAMP.tar.gz"
tar -czf "$TAR_FILE" -C "$DIST_DIR" .

echo -e "${GREEN}✓ Arquivo TAR.GZ criado: $TAR_FILE${NC}"

# ========================================
# Resumo Final
# ========================================
echo ""
echo -e "${CYAN}========================================"
echo "  BUILD CONCLUÍDO COM SUCESSO!         "
echo -e "========================================${NC}"
echo ""

echo -e "${YELLOW}Artefatos gerados:${NC}"
echo ""

if [ "$BUILD_DESKTOP" = true ]; then
    echo -e "${GREEN}Desktop:${NC}"
    echo "  - $DIST_DIR/desktop/sihcp-desktop-2.7.0.jar"
    echo "  - $DIST_DIR/desktop/lib/ (dependências)"
    echo "  - $DIST_DIR/desktop/iniciar-desktop.sh"
    echo ""
fi

if [ "$BUILD_SERVER" = true ]; then
    echo -e "${GREEN}Server:${NC}"
    echo "  - $DIST_DIR/server/sihcp-server-2.7.0.jar"
    echo "  - $DIST_DIR/server/iniciar-server.sh"
    echo "  - $DIST_DIR/server/sihcp-server.service"
    echo ""
fi

echo -e "${GREEN}Pacote de distribuição:${NC}"
echo "  - $TAR_FILE"
echo ""

echo -e "${YELLOW}Para distribuir:${NC}"
echo "  1. Enviar o arquivo TAR.GZ para o servidor/cliente"
echo "  2. Extrair: tar -xzf sihcp-$TIMESTAMP.tar.gz"
echo "  3. Seguir as instruções no README.md de cada módulo"
echo ""

echo -e "${CYAN}Build finalizado em: $(date '+%d/%m/%Y %H:%M:%S')${NC}"
