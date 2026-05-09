#!/bin/bash
# ============================================================
# SIHCP - Script de Setup Automatizado (Linux/macOS)
# Conduz o Administrador_Campus por todas as etapas de
# implantação de forma interativa, com validação em cada passo.
# ============================================================

set -euo pipefail

# Diretório base (raiz do pacote)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_DIR="$(dirname "$SCRIPT_DIR")"
cd "$BASE_DIR"

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
GRAY='\033[0;37m'
NC='\033[0m' # No Color

write_step() { echo -e "${CYAN}$1${NC}"; }
write_ok()   { echo -e "${GREEN}[OK] $1${NC}"; }
write_warn() { echo -e "${YELLOW}[AVISO] $1${NC}"; }
write_err()  { echo -e "${RED}[ERRO] $1${NC}"; }
write_info() { echo -e "${GRAY}      $1${NC}"; }

clear
echo -e "${CYAN}============================================================${NC}"
echo -e "${CYAN}  SIHCP - Setup Automatizado de Implantação em Campus${NC}"
echo -e "${CYAN}============================================================${NC}"
echo ""

# ============================================================
# [1/6] Validar pré-requisitos
# ============================================================
write_step "[1/6] Validando pré-requisitos..."

# Verificar Java 21+
if ! command -v java &>/dev/null; then
    write_err "JDK 21 não encontrado no PATH."
    write_info "Instale o JDK 21 em: https://adoptium.net"
    exit 1
fi

JAVA_VERSION_OUTPUT=$(java -version 2>&1 | head -1)
JAVA_VER_RAW=$(echo "$JAVA_VERSION_OUTPUT" | grep -oP '(?<=version ")[^"]+' || echo "")
JAVA_MAJOR=$(echo "$JAVA_VER_RAW" | cut -d'.' -f1)

if [ -z "$JAVA_MAJOR" ] || [ "$JAVA_MAJOR" -lt 21 ] 2>/dev/null; then
    write_err "Java $JAVA_MAJOR encontrado, mas é necessário JDK 21 ou superior."
    write_info "Instale o JDK 21 em: https://adoptium.net"
    exit 1
fi
write_ok "JDK $JAVA_MAJOR encontrado."

# Verificar psql (PostgreSQL)
if ! command -v psql &>/dev/null; then
    write_err "PostgreSQL (psql) não encontrado no PATH."
    write_info "Instale o PostgreSQL 12+:"
    write_info "  Ubuntu/Debian: sudo apt install postgresql-client"
    write_info "  macOS:         brew install postgresql"
    write_info "  Fedora/RHEL:   sudo dnf install postgresql"
    exit 1
fi
PSQL_VER=$(psql --version 2>&1)
write_ok "PostgreSQL client encontrado: $PSQL_VER"

echo ""

# ============================================================
# [2/6] Coletar dados do campus
# ============================================================
write_step "[2/6] Coletando dados do campus..."
echo ""

# Função para leitura de senha sem eco
read_password() {
    local prompt="$1"
    local var_name="$2"
    local senha=""
    while IFS= read -r -s -p "  $prompt" senha; do
        echo ""
        if [ -z "$senha" ]; then
            write_warn "A senha não pode ser vazia."
        else
            break
        fi
    done
    eval "$var_name=\"\$senha\""
}

# --- Nome do campus ---
NOME_CAMPUS=""
while [ -z "$NOME_CAMPUS" ]; do
    read -r -p "  Nome do campus (ex: IFMT - Campus Cuiabá): " NOME_CAMPUS
    if [ -z "$NOME_CAMPUS" ]; then
        write_warn "O nome do campus não pode ser vazio."
    elif [ ${#NOME_CAMPUS} -gt 100 ]; then
        write_warn "O nome do campus deve ter no máximo 100 caracteres (atual: ${#NOME_CAMPUS})."
        NOME_CAMPUS=""
    fi
done

# --- Sigla ---
read -r -p "  Sigla do campus (ex: CBA): " SIGLA

# --- Cidade ---
read -r -p "  Cidade: " CIDADE

# --- Estado ---
ESTADO=""
while [ ${#ESTADO} -ne 2 ]; do
    read -r -p "  Estado (UF, ex: MT): " ESTADO
    ESTADO=$(echo "$ESTADO" | tr '[:lower:]' '[:upper:]')
    if [ ${#ESTADO} -ne 2 ]; then
        write_warn "Informe a UF com 2 letras (ex: MT, SP, RJ)."
    fi
done

# --- Porta da API ---
PORTA_API=0
while [ "$PORTA_API" -eq 0 ]; do
    read -r -p "  Porta da API Mobile (padrão: 8080): " PORTA_API_STR
    PORTA_API_STR="${PORTA_API_STR:-8080}"
    # Validar se é número
    if ! [[ "$PORTA_API_STR" =~ ^[0-9]+$ ]]; then
        write_warn "Porta inválida. Informe um número entre 1024 e 65535."
        PORTA_API=0
        continue
    fi
    PORTA_API=$PORTA_API_STR
    if [ "$PORTA_API" -lt 1024 ] || [ "$PORTA_API" -gt 65535 ]; then
        write_warn "Porta inválida. Informe um valor entre 1024 e 65535."
        PORTA_API=0
        continue
    fi
    # Verificar se a porta está em uso
    if command -v ss &>/dev/null; then
        if ss -tlnp 2>/dev/null | grep -q ":${PORTA_API} "; then
            write_warn "Porta $PORTA_API já está em uso. Escolha outra porta."
            PORTA_API=0
        fi
    elif command -v lsof &>/dev/null; then
        if lsof -i ":$PORTA_API" -sTCP:LISTEN &>/dev/null; then
            write_warn "Porta $PORTA_API já está em uso. Escolha outra porta."
            PORTA_API=0
        fi
    fi
done

# --- Host PostgreSQL ---
read -r -p "  Host do PostgreSQL (padrão: localhost): " HOST_PG
HOST_PG="${HOST_PG:-localhost}"

# --- Porta PostgreSQL ---
read -r -p "  Porta do PostgreSQL (padrão: 5432): " PORTA_PG_STR
PORTA_PG="${PORTA_PG_STR:-5432}"

# --- Nome do banco ---
read -r -p "  Nome do banco de dados (padrão: sispatrimonio): " BANCO
BANCO="${BANCO:-sispatrimonio}"

# --- Usuário PostgreSQL ---
USUARIO_PG=""
while [ -z "$USUARIO_PG" ]; do
    read -r -p "  Usuário do PostgreSQL: " USUARIO_PG
done

# --- Senha PostgreSQL ---
read_password "Senha do PostgreSQL: " SENHA_PG

# --- Senha do admin SIHCP ---
SENHA_ADMIN=""
while [ -z "$SENHA_ADMIN" ]; do
    read_password "Senha do administrador SIHCP (usuário 'admin'): " SENHA_ADMIN
done

echo ""
write_ok "Dados coletados com sucesso."
echo ""

# ============================================================
# [3/6] Gerar arquivos de configuração
# ============================================================
write_step "[3/6] Gerando arquivos de configuração..."

# Criar diretório config/ se não existir
mkdir -p config

# Gerar config/configuracao_banco.json
cat > config/configuracao_banco.json << EOF
{
  "campus": {
    "nome": "$NOME_CAMPUS",
    "sigla": "$SIGLA",
    "cidade": "$CIDADE",
    "estado": "$ESTADO",
    "responsavel_tecnico": "",
    "contato": ""
  },
  "postgresql": {
    "host": "$HOST_PG",
    "port": $PORTA_PG,
    "database": "$BANCO",
    "user": "$USUARIO_PG",
    "password": "$SENHA_PG"
  },
  "api": {
    "porta": $PORTA_API,
    "versao": "1.2.0"
  }
}
EOF
write_ok "config/configuracao_banco.json gerado."

# Gerar config/application.properties a partir do template
if [ -f "config/application.properties.template" ]; then
    sed \
        -e "s/\${PORTA_API}/$PORTA_API/g" \
        -e "s/\${NOME_CAMPUS}/$NOME_CAMPUS/g" \
        -e "s/\${SIGLA_CAMPUS}/$SIGLA/g" \
        config/application.properties.template > config/application.properties
    write_ok "config/application.properties gerado a partir do template."
else
    cat > config/application.properties << EOF
server.port=$PORTA_API
spring.profiles.active=mobile,prod
campus.nome=$NOME_CAMPUS
campus.sigla=$SIGLA
EOF
    write_ok "config/application.properties gerado (template não encontrado, usando padrão)."
fi

echo ""

# ============================================================
# [4/6] Criar banco e executar SQL
# ============================================================
write_step "[4/6] Criando banco de dados e executando SQL..."

export PGPASSWORD="$SENHA_PG"

# Criar banco de dados
write_info "Criando banco '$BANCO'..."
if ! psql -h "$HOST_PG" -p "$PORTA_PG" -U "$USUARIO_PG" -d postgres \
    -c "CREATE DATABASE $BANCO" 2>&1 | grep -v "already exists"; then
    # Verificar se o erro é apenas "já existe"
    RESULT=$(psql -h "$HOST_PG" -p "$PORTA_PG" -U "$USUARIO_PG" -d postgres \
        -c "SELECT 1 FROM pg_database WHERE datname='$BANCO'" -t 2>&1 | tr -d ' ')
    if [ "$RESULT" != "1" ]; then
        write_err "Falha ao criar banco de dados."
        write_info "Verifique as credenciais e tente novamente."
        write_info "As configurações já foram salvas em config/configuracao_banco.json"
        unset PGPASSWORD
        exit 1
    fi
fi
write_ok "Banco '$BANCO' criado (ou já existia)."

# Executar SQL de setup
if [ -f "sql/setup_banco_completo.sql" ]; then
    write_info "Executando sql/setup_banco_completo.sql..."
    if ! psql -h "$HOST_PG" -p "$PORTA_PG" -U "$USUARIO_PG" -d "$BANCO" \
        -f "sql/setup_banco_completo.sql" 2>&1; then
        write_err "Falha ao executar o script SQL."
        write_info "Etapa: Execução de sql/setup_banco_completo.sql"
        write_info "As configurações já foram salvas. Execute manualmente:"
        write_info "  psql -h $HOST_PG -p $PORTA_PG -U $USUARIO_PG -d $BANCO -f sql/setup_banco_completo.sql"
        unset PGPASSWORD
        exit 1
    fi
    write_ok "Schema do banco criado com sucesso."
else
    write_warn "Arquivo sql/setup_banco_completo.sql não encontrado. Pulando criação do schema."
fi

unset PGPASSWORD
echo ""

# ============================================================
# [5/6] Criar usuário admin
# ============================================================
write_step "[5/6] Criando usuário administrador inicial..."

export PGPASSWORD="$SENHA_PG"

SQL_ADMIN="INSERT INTO TABELA_USUARIO (LOGIN, SENHA_HASH, NOME_COMPLETO, EMAIL, PERFIL, ATIVO)
VALUES (
    'admin',
    crypt('${SENHA_ADMIN}', gen_salt('bf')),
    'Administrador',
    'admin@campus.ifmt.edu.br',
    'ADMIN',
    'S'
)
ON CONFLICT (LOGIN) DO NOTHING;"

if psql -h "$HOST_PG" -p "$PORTA_PG" -U "$USUARIO_PG" -d "$BANCO" \
    -c "$SQL_ADMIN" 2>&1; then
    write_ok "Usuário 'admin' criado com sucesso."
else
    write_warn "Não foi possível criar o usuário admin automaticamente."
    write_info "Execute manualmente no banco '$BANCO':"
    write_info "  INSERT INTO TABELA_USUARIO (LOGIN, SENHA_HASH, NOME_COMPLETO, EMAIL, PERFIL, ATIVO)"
    write_info "  VALUES ('admin', crypt('<senha>', gen_salt('bf')), 'Administrador', 'admin@campus.ifmt.edu.br', 'ADMIN', 'S')"
    write_info "  ON CONFLICT (LOGIN) DO NOTHING;"
fi

unset PGPASSWORD
echo ""

# ============================================================
# [6/6] Gerar QR Code e exibir resumo
# ============================================================
write_step "[6/6] Gerando QR Code e exibindo resumo..."

# Detectar IP local da máquina
IP_LOCAL=""
if command -v hostname &>/dev/null; then
    IP_LOCAL=$(hostname -I 2>/dev/null | awk '{print $1}' || echo "")
fi
if [ -z "$IP_LOCAL" ] && command -v ip &>/dev/null; then
    IP_LOCAL=$(ip route get 1 2>/dev/null | awk '{print $7; exit}' || echo "")
fi
if [ -z "$IP_LOCAL" ] && command -v ifconfig &>/dev/null; then
    IP_LOCAL=$(ifconfig 2>/dev/null | grep 'inet ' | grep -v '127.0.0.1' | awk '{print $2}' | head -1 || echo "")
fi
IP_LOCAL="${IP_LOCAL:-localhost}"

URL_API="http://${IP_LOCAL}:${PORTA_API}/api/mobile"

# Criar diretório qrcode/ se não existir
mkdir -p qrcode

# Gerar QR Code via QrCodeGenerator
if [ -f "bin/mobile-server.jar" ]; then
    write_info "Gerando QR Code..."
    if java -cp "bin/mobile-server.jar" com.inventario.util.QrCodeGenerator \
        "$URL_API" "qrcode/api-qrcode.png" 300 2>/dev/null; then
        write_ok "QR Code gerado em qrcode/api-qrcode.png"
    else
        write_warn "Não foi possível gerar o QR Code automaticamente."
    fi
else
    write_warn "bin/mobile-server.jar não encontrado. QR Code não gerado."
fi

# Exibir resumo
echo ""
echo -e "${GREEN}============================================================${NC}"
echo -e "${GREEN}  ✅ Implantação concluída!${NC}"
echo -e "${GREEN}============================================================${NC}"
echo ""
echo "  Campus    : $NOME_CAMPUS ($SIGLA)"
echo "  URL da API: $URL_API"
echo "  Admin     : admin / [senha definida durante o setup]"
echo "  APK       : bin/sihcp-mobile.apk"
echo "  QR Code   : qrcode/api-qrcode.png"
echo ""
echo -e "${CYAN}  Próximos passos:${NC}"
echo -e "${CYAN}  1. Inicie o servidor: bash scripts/iniciar-servidor.sh${NC}"
echo -e "${CYAN}  2. Verifique a saúde: bash scripts/verificar-saude.sh${NC}"
echo -e "${CYAN}  3. Distribua o APK : bin/sihcp-mobile.apk${NC}"
echo ""
