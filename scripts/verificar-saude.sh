#!/bin/bash
# ============================================================
# SIHCP - Script de Verificação de Saúde (Linux/macOS)
# Executa 4 testes em sequência e gera relatório de saúde.
# Uso: bash verificar-saude.sh [--senha-admin SENHA]
# ============================================================

set -uo pipefail

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
NC='\033[0m'

write_ok()   { echo -e "${GREEN}[OK]  $1${NC}"; }
write_fail() { echo -e "${RED}[FAIL] $1${NC}"; }
write_info() { echo -e "${GRAY}      $1${NC}"; }

# Data/hora para o relatório
DATA_HORA=$(date +"%Y%m%d-%H%M")
RELATORIO_PATH="relatorio-saude-${DATA_HORA}.txt"

# Acumular linhas do relatório
RELATORIO_LINHAS=()
TODOS_OK=true

add_relatorio() { RELATORIO_LINHAS+=("$1"); }

# ============================================================
# Ler configurações do configuracao_banco.json
# ============================================================
if [ ! -f "config/configuracao_banco.json" ]; then
    echo -e "${RED}[ERRO] Arquivo config/configuracao_banco.json não encontrado.${NC}"
    echo -e "${GRAY}       Execute o setup primeiro: bash scripts/setup.sh${NC}"
    exit 1
fi

# Extrair valores do JSON
if command -v python3 &>/dev/null; then
    DB_HOST=$(python3 -c "import json; d=json.load(open('config/configuracao_banco.json')); print(d['postgresql']['host'])" 2>/dev/null || echo "localhost")
    DB_PORT=$(python3 -c "import json; d=json.load(open('config/configuracao_banco.json')); print(d['postgresql']['port'])" 2>/dev/null || echo "5432")
    API_PORT=$(python3 -c "import json; d=json.load(open('config/configuracao_banco.json')); print(d['api']['porta'])" 2>/dev/null || echo "8080")
    CAMPUS=$(python3 -c "import json; d=json.load(open('config/configuracao_banco.json')); print(d['campus']['nome'])" 2>/dev/null || echo "Campus")
    VERSAO=$(python3 -c "import json; d=json.load(open('config/configuracao_banco.json')); print(d['api']['versao'])" 2>/dev/null || echo "N/A")
else
    DB_HOST=$(grep -o '"host"[[:space:]]*:[[:space:]]*"[^"]*"' config/configuracao_banco.json | grep -o '"[^"]*"$' | tr -d '"' || echo "localhost")
    DB_PORT=$(grep -o '"port"[[:space:]]*:[[:space:]]*[0-9]*' config/configuracao_banco.json | grep -o '[0-9]*$' || echo "5432")
    API_PORT=$(grep -o '"porta"[[:space:]]*:[[:space:]]*[0-9]*' config/configuracao_banco.json | grep -o '[0-9]*$' || echo "8080")
    CAMPUS="Campus"
    VERSAO="N/A"
fi

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
API_PORT="${API_PORT:-8080}"
CAMPUS="${CAMPUS:-Campus}"
VERSAO="${VERSAO:-N/A}"

# Detectar IP local
IP_LOCAL=""
if command -v hostname &>/dev/null; then
    IP_LOCAL=$(hostname -I 2>/dev/null | awk '{print $1}' || echo "")
fi
if [ -z "$IP_LOCAL" ] && command -v ip &>/dev/null; then
    IP_LOCAL=$(ip route get 1 2>/dev/null | awk '{print $7; exit}' || echo "")
fi
IP_LOCAL="${IP_LOCAL:-localhost}"

BASE_URL="http://${IP_LOCAL}:${API_PORT}"

# Solicitar senha do admin
SENHA_ADMIN=""
# Verificar argumento --senha-admin
while [[ $# -gt 0 ]]; do
    case "$1" in
        --senha-admin) SENHA_ADMIN="$2"; shift 2 ;;
        *) shift ;;
    esac
done

if [ -z "$SENHA_ADMIN" ]; then
    read -r -s -p "Senha do administrador SIHCP (usuário 'admin'): " SENHA_ADMIN
    echo ""
fi

# Cabeçalho
echo ""
echo -e "${CYAN}============================================================${NC}"
echo -e "${CYAN}  SIHCP - Verificação de Saúde do Sistema${NC}"
echo -e "${CYAN}============================================================${NC}"
echo "  Campus : $CAMPUS"
echo "  Data   : $(date '+%d/%m/%Y %H:%M:%S')"
echo "  Versão : $VERSAO"
echo "  URL    : $BASE_URL"
echo -e "${CYAN}============================================================${NC}"
echo ""

add_relatorio "RELATÓRIO DE SAÚDE SIHCP"
add_relatorio "Campus: $CAMPUS"
add_relatorio "Data: $(date '+%d/%m/%Y %H:%M:%S')"
add_relatorio "Versão: $VERSAO"
add_relatorio ""

# ============================================================
# Teste 1: Conectividade TCP com o banco
# ============================================================
echo "Teste 1: Conectividade com o banco de dados ($DB_HOST:$DB_PORT)..."
BANCO_OK=false
if command -v nc &>/dev/null; then
    if nc -z -w 3 "$DB_HOST" "$DB_PORT" 2>/dev/null; then
        BANCO_OK=true
    fi
elif (echo > /dev/tcp/"$DB_HOST"/"$DB_PORT") 2>/dev/null; then
    BANCO_OK=true
fi

if [ "$BANCO_OK" = true ]; then
    write_ok "Banco de dados: conectado ($DB_HOST:$DB_PORT)"
    add_relatorio "[OK]  Banco de dados: conectado ($DB_HOST:$DB_PORT)"
else
    TODOS_OK=false
    write_fail "Banco de dados: inacessível ($DB_HOST:$DB_PORT)"
    write_info "Consulte: SOLUCAO_PROBLEMAS.md#banco"
    add_relatorio "[FAIL] Banco de dados: inacessível ($DB_HOST:$DB_PORT)"
    add_relatorio "       Consulte: SOLUCAO_PROBLEMAS.md#banco"
fi

# ============================================================
# Teste 2: API respondendo (GET /api/mobile/health)
# ============================================================
echo ""
echo "Teste 2: API Mobile respondendo ($BASE_URL/api/mobile/health)..."
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
    --connect-timeout 5 --max-time 10 \
    "$BASE_URL/api/mobile/health" 2>/dev/null || echo "000")

if [ "$HTTP_STATUS" = "200" ]; then
    write_ok "API Mobile: respondendo (HTTP $HTTP_STATUS)"
    add_relatorio "[OK]  API Mobile: respondendo (GET /api/mobile/health → $HTTP_STATUS)"
else
    TODOS_OK=false
    write_fail "API Mobile: não respondendo (HTTP $HTTP_STATUS)"
    write_info "Consulte: SOLUCAO_PROBLEMAS.md#api"
    add_relatorio "[FAIL] API Mobile: não respondendo (HTTP $HTTP_STATUS)"
    add_relatorio "       Consulte: SOLUCAO_PROBLEMAS.md#api"
fi

# ============================================================
# Teste 3: Autenticação (POST /api/mobile/auth/login)
# ============================================================
echo ""
echo "Teste 3: Autenticação do administrador..."
TOKEN=""
LOGIN_RESPONSE=$(curl -s -w "\n%{http_code}" \
    --connect-timeout 5 --max-time 10 \
    -X POST "$BASE_URL/api/mobile/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"login\":\"admin\",\"senha\":\"${SENHA_ADMIN}\"}" 2>/dev/null || echo "")

LOGIN_HTTP=$(echo "$LOGIN_RESPONSE" | tail -1)
LOGIN_BODY=$(echo "$LOGIN_RESPONSE" | head -n -1)

if [ "$LOGIN_HTTP" = "200" ]; then
    # Tentar extrair token de diferentes estruturas de resposta
    if command -v python3 &>/dev/null; then
        TOKEN=$(python3 -c "
import json, sys
try:
    d = json.loads('''$LOGIN_BODY''')
    t = d.get('token') or d.get('accessToken') or (d.get('data') or {}).get('token') or ''
    print(t)
except: print('')
" 2>/dev/null || echo "")
    else
        TOKEN=$(echo "$LOGIN_BODY" | grep -o '"token":"[^"]*"' | cut -d'"' -f4 || echo "")
    fi

    if [ -n "$TOKEN" ]; then
        write_ok "Autenticação: admin autenticado com sucesso"
        add_relatorio "[OK]  Autenticação: admin autenticado com sucesso"
    else
        TODOS_OK=false
        write_fail "Autenticação: token não encontrado na resposta"
        write_info "Consulte: SOLUCAO_PROBLEMAS.md#autenticacao"
        add_relatorio "[FAIL] Autenticação: token não encontrado na resposta"
        add_relatorio "       Consulte: SOLUCAO_PROBLEMAS.md#autenticacao"
    fi
else
    TODOS_OK=false
    write_fail "Autenticação: falha ao autenticar admin (HTTP $LOGIN_HTTP)"
    write_info "Consulte: SOLUCAO_PROBLEMAS.md#autenticacao"
    add_relatorio "[FAIL] Autenticação: falha ao autenticar admin (HTTP $LOGIN_HTTP)"
    add_relatorio "       Consulte: SOLUCAO_PROBLEMAS.md#autenticacao"
fi

# ============================================================
# Teste 4: Listagem de patrimônios (GET /api/mobile/patrimonio)
# ============================================================
echo ""
echo "Teste 4: Listagem de patrimônios..."
if [ -n "$TOKEN" ]; then
    PATRIM_RESPONSE=$(curl -s -w "\n%{http_code}" \
        --connect-timeout 5 --max-time 15 \
        -X GET "$BASE_URL/api/mobile/patrimonio" \
        -H "Authorization: Bearer $TOKEN" 2>/dev/null || echo "")

    PATRIM_HTTP=$(echo "$PATRIM_RESPONSE" | tail -1)
    PATRIM_BODY=$(echo "$PATRIM_RESPONSE" | head -n -1)

    if [ "$PATRIM_HTTP" = "200" ]; then
        TOTAL=0
        if command -v python3 &>/dev/null; then
            TOTAL=$(python3 -c "
import json
try:
    d = json.loads('''$PATRIM_BODY''')
    data = d.get('data', [])
    print(len(data) if isinstance(data, list) else 0)
except: print(0)
" 2>/dev/null || echo "0")
        fi
        write_ok "Patrimônios: listagem retornou $TOTAL registros (HTTP $PATRIM_HTTP)"
        add_relatorio "[OK]  Patrimônios: listagem retornou $TOTAL registros"
    else
        TODOS_OK=false
        write_fail "Patrimônios: falha na listagem (HTTP $PATRIM_HTTP)"
        write_info "Consulte: SOLUCAO_PROBLEMAS.md#patrimonio"
        add_relatorio "[FAIL] Patrimônios: falha na listagem (HTTP $PATRIM_HTTP)"
        add_relatorio "       Consulte: SOLUCAO_PROBLEMAS.md#patrimonio"
    fi
else
    write_fail "Patrimônios: teste ignorado (autenticação falhou)"
    add_relatorio "[SKIP] Patrimônios: teste ignorado (autenticação falhou)"
fi

# ============================================================
# Resultado final
# ============================================================
echo ""
echo -e "${CYAN}============================================================${NC}"

if [ "$TODOS_OK" = true ]; then
    echo -e "${GREEN}  ✅ Sistema SIHCP operacional e pronto para uso${NC}"
    echo -e "${GREEN}  URL de acesso: $BASE_URL${NC}"
    add_relatorio ""
    add_relatorio "STATUS GERAL: ✅ OPERACIONAL"
    add_relatorio "URL de acesso: $BASE_URL"
else
    echo -e "${RED}  ❌ Um ou mais testes falharam. Verifique os erros acima.${NC}"
    echo -e "${YELLOW}  Consulte: SOLUCAO_PROBLEMAS.md${NC}"
    add_relatorio ""
    add_relatorio "STATUS GERAL: ❌ COM FALHAS"
    add_relatorio "Consulte: SOLUCAO_PROBLEMAS.md"
fi

echo -e "${CYAN}============================================================${NC}"
echo ""

# Gravar relatório
printf '%s\n' "${RELATORIO_LINHAS[@]}" > "$RELATORIO_PATH"
echo -e "${GRAY}  Relatório gravado em: $RELATORIO_PATH${NC}"
echo ""

if [ "$TODOS_OK" = false ]; then exit 1; fi
