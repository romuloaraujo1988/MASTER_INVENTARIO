#!/bin/bash
# ============================================================
# SIHCP - Script de Inicialização do Servidor (Linux/macOS)
# Verifica pré-condições e inicia a API Mobile
# ============================================================

set -euo pipefail

# Diretório base do script (raiz do pacote)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_DIR="$(dirname "$SCRIPT_DIR")"
cd "$BASE_DIR"

echo "============================================================"
echo " SIHCP - Iniciando Servidor API Mobile"
echo "============================================================"
echo ""

# ------------------------------------------------------------
# [1/4] Verificar JDK 21
# ------------------------------------------------------------
echo "[1/4] Verificando JDK 21..."

if ! command -v java &>/dev/null; then
    echo ""
    echo "[ERRO] JDK 21 não encontrado no PATH."
    echo "       Instale o JDK 21 em: https://adoptium.net"
    echo "       Após instalar, reinicie o terminal e tente novamente."
    exit 1
fi

# Extrair versão principal do Java
JAVA_VERSION_OUTPUT=$(java -version 2>&1 | head -1)
# Suporta formatos: "java version \"21.0.1\"" e "openjdk version \"21.0.1\""
JAVA_VER_RAW=$(echo "$JAVA_VERSION_OUTPUT" | grep -oP '(?<=version ")[^"]+' || echo "")

if [ -z "$JAVA_VER_RAW" ]; then
    echo "[ERRO] Não foi possível determinar a versão do Java."
    echo "       Instale o JDK 21 em: https://adoptium.net"
    exit 1
fi

# Extrair número principal (antes do primeiro ponto)
JAVA_MAJOR=$(echo "$JAVA_VER_RAW" | cut -d'.' -f1)

if [ -z "$JAVA_MAJOR" ] || [ "$JAVA_MAJOR" -lt 21 ] 2>/dev/null; then
    echo ""
    echo "[ERRO] Java $JAVA_MAJOR encontrado, mas é necessário JDK 21 ou superior."
    echo "       Instale o JDK 21 em: https://adoptium.net"
    exit 1
fi

echo "[OK] JDK $JAVA_MAJOR encontrado."

# ------------------------------------------------------------
# [2/4] Verificar arquivo de configuração
# ------------------------------------------------------------
echo "[2/4] Verificando arquivo de configuração..."

if [ ! -f "config/configuracao_banco.json" ]; then
    echo ""
    echo "[ERRO] Arquivo de configuração não encontrado: config/configuracao_banco.json"
    echo "       Execute o script de setup primeiro: scripts/setup.sh"
    exit 1
fi

echo "[OK] Arquivo de configuração encontrado."

# ------------------------------------------------------------
# [3/4] Verificar conectividade com o banco de dados
# ------------------------------------------------------------
echo "[3/4] Verificando conectividade com o banco de dados..."

# Ler host e porta do JSON (usando python3 ou python como fallback)
if command -v python3 &>/dev/null; then
    DB_HOST=$(python3 -c "import json,sys; d=json.load(open('config/configuracao_banco.json')); print(d['postgresql']['host'])" 2>/dev/null || echo "localhost")
    DB_PORT=$(python3 -c "import json,sys; d=json.load(open('config/configuracao_banco.json')); print(d['postgresql']['port'])" 2>/dev/null || echo "5432")
elif command -v python &>/dev/null; then
    DB_HOST=$(python -c "import json,sys; d=json.load(open('config/configuracao_banco.json')); print(d['postgresql']['host'])" 2>/dev/null || echo "localhost")
    DB_PORT=$(python -c "import json,sys; d=json.load(open('config/configuracao_banco.json')); print(d['postgresql']['port'])" 2>/dev/null || echo "5432")
else
    # Fallback: extrair com grep/sed
    DB_HOST=$(grep -o '"host"[[:space:]]*:[[:space:]]*"[^"]*"' config/configuracao_banco.json | grep -o '"[^"]*"$' | tr -d '"' || echo "localhost")
    DB_PORT=$(grep -o '"port"[[:space:]]*:[[:space:]]*[0-9]*' config/configuracao_banco.json | grep -o '[0-9]*$' || echo "5432")
fi

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"

# Testar conectividade TCP com o banco
CONECTADO=false
if command -v nc &>/dev/null; then
    # Usar netcat
    if nc -z -w 3 "$DB_HOST" "$DB_PORT" 2>/dev/null; then
        CONECTADO=true
    fi
elif (echo > /dev/tcp/"$DB_HOST"/"$DB_PORT") 2>/dev/null; then
    # Usar /dev/tcp (bash built-in)
    CONECTADO=true
fi

if [ "$CONECTADO" = false ]; then
    echo ""
    echo "[ERRO] Banco de dados inacessível. Verifique as configurações em configuracao_banco.json"
    echo "       Host: $DB_HOST"
    echo "       Porta: $DB_PORT"
    exit 1
fi

echo "[OK] Banco de dados acessível em $DB_HOST:$DB_PORT."

# ------------------------------------------------------------
# [4/4] Verificar se a porta da API está em uso
# ------------------------------------------------------------
echo "[4/4] Verificando disponibilidade da porta da API..."

# Ler porta da API do JSON
if command -v python3 &>/dev/null; then
    API_PORT=$(python3 -c "import json; d=json.load(open('config/configuracao_banco.json')); print(d['api']['porta'])" 2>/dev/null || echo "8080")
elif command -v python &>/dev/null; then
    API_PORT=$(python -c "import json; d=json.load(open('config/configuracao_banco.json')); print(d['api']['porta'])" 2>/dev/null || echo "8080")
else
    API_PORT=$(grep -o '"porta"[[:space:]]*:[[:space:]]*[0-9]*' config/configuracao_banco.json | grep -o '[0-9]*$' || echo "8080")
fi

API_PORT="${API_PORT:-8080}"

# Verificar se a porta está em uso
if command -v ss &>/dev/null; then
    if ss -tlnp 2>/dev/null | grep -q ":${API_PORT} "; then
        echo ""
        echo "[ERRO] Porta $API_PORT já está em uso por outro processo."
        echo "       Altere a porta em config/configuracao_banco.json ou encerre o processo que usa a porta."
        exit 1
    fi
elif command -v lsof &>/dev/null; then
    if lsof -i ":$API_PORT" -sTCP:LISTEN &>/dev/null; then
        echo ""
        echo "[ERRO] Porta $API_PORT já está em uso por outro processo."
        echo "       Altere a porta em config/configuracao_banco.json ou encerre o processo que usa a porta."
        exit 1
    fi
fi

echo "[OK] Porta $API_PORT disponível."

# ------------------------------------------------------------
# Iniciar o servidor
# ------------------------------------------------------------
echo ""
echo "============================================================"
echo " Iniciando API Mobile na porta $API_PORT..."
echo " Pressione Ctrl+C para encerrar."
echo "============================================================"
echo ""

exec java -Xms256m -Xmx1g \
    -Dinventario.config.mode=APP_DIR \
    -jar bin/mobile-server.jar \
    --spring.profiles.active=mobile,prod \
    --spring.config.additional-location=config/
