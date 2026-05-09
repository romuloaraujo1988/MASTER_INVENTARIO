#!/bin/bash
# ============================================================
# SIHCP - Script de Coleta de Diagnóstico (Linux/macOS)
# Coleta logs, versões, configuração anonimizada e relatório
# de saúde, compacta tudo em um ZIP para envio ao suporte.
# IMPORTANTE: Senhas NÃO são incluídas no diagnóstico.
# Uso: bash coletar-diagnostico.sh [--senha-admin SENHA]
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

write_ok()   { echo -e "${GREEN}[OK] $1${NC}"; }
write_warn() { echo -e "${YELLOW}[AVISO] $1${NC}"; }
write_err()  { echo -e "${RED}[ERRO] $1${NC}"; }
write_info() { echo -e "${GRAY}      $1${NC}"; }

# Parâmetros opcionais
SENHA_ADMIN=""
while [[ $# -gt 0 ]]; do
    case "$1" in
        --senha-admin) SENHA_ADMIN="$2"; shift 2 ;;
        *) shift ;;
    esac
done

echo ""
echo -e "${CYAN}============================================================${NC}"
echo -e "${CYAN}  SIHCP - Coleta de Diagnóstico${NC}"
echo -e "${CYAN}============================================================${NC}"
echo ""

# ============================================================
# Ler nome do campus para o nome do arquivo
# ============================================================
CAMPUS="campus"
if [ -f "config/configuracao_banco.json" ] && command -v python3 &>/dev/null; then
    CAMPUS_NOME=$(python3 -c "
import json, re
try:
    d = json.load(open('config/configuracao_banco.json'))
    nome = d['campus'].get('sigla') or d['campus'].get('nome', 'campus')
    # Sanitizar: manter apenas alfanuméricos, hífens e underscores
    nome = re.sub(r'[^a-zA-Z0-9_-]', '_', nome)
    nome = re.sub(r'_+', '_', nome).strip('_').lower()
    print(nome)
except: print('campus')
" 2>/dev/null || echo "campus")
    CAMPUS="${CAMPUS_NOME:-campus}"
fi

DATA_HORA=$(date +"%Y%m%d-%H%M")
TEMP_DIR="diagnostico-temp"
ZIP_NOME="diagnostico-${CAMPUS}-${DATA_HORA}.zip"

# Limpar diretório temporário anterior se existir
rm -rf "$TEMP_DIR"
mkdir -p "$TEMP_DIR/logs"

write_info "Diretório temporário: $TEMP_DIR"
echo ""

# ============================================================
# [1] Coletar últimas 1000 linhas dos logs
# ============================================================
echo "[1/6] Coletando logs da API Mobile..."

if [ -d "logs" ]; then
    LOG_COUNT=0
    while IFS= read -r -d '' logfile; do
        NOME=$(basename "$logfile")
        # Coletar últimas 1000 linhas
        if tail -n 1000 "$logfile" > "$TEMP_DIR/logs/$NOME" 2>/dev/null; then
            LINHAS=$(wc -l < "$TEMP_DIR/logs/$NOME" 2>/dev/null || echo "?")
            write_ok "Log coletado: $NOME ($LINHAS linhas)"
            LOG_COUNT=$((LOG_COUNT + 1))
        else
            write_warn "Não foi possível coletar: $NOME"
        fi
    done < <(find "logs" -name "*.log" -print0 2>/dev/null)

    if [ "$LOG_COUNT" -eq 0 ]; then
        echo "Nenhum arquivo de log encontrado em logs/" > "$TEMP_DIR/logs/sem-logs.txt"
        write_warn "Nenhum arquivo de log encontrado em logs/"
    fi
else
    echo "Diretório logs/ não encontrado." > "$TEMP_DIR/logs/sem-logs.txt"
    write_warn "Diretório logs/ não encontrado."
fi

# ============================================================
# [2] Coletar versões de software
# ============================================================
echo ""
echo "[2/6] Coletando informações do ambiente..."

{
    echo "=== AMBIENTE DE SOFTWARE ==="
    echo "Data de coleta: $(date '+%d/%m/%Y %H:%M:%S')"
    echo ""

    echo "--- Java ---"
    java -version 2>&1 || echo "Não encontrado"
    echo ""

    echo "--- PostgreSQL Client ---"
    psql --version 2>&1 || echo "Não encontrado"
    echo ""

    echo "--- Bash ---"
    bash --version 2>&1 | head -1 || echo "Não encontrado"
    echo ""

    echo "--- curl ---"
    curl --version 2>&1 | head -1 || echo "Não encontrado"
    echo ""
} > "$TEMP_DIR/ambiente.txt"

write_ok "Informações do ambiente coletadas."

# ============================================================
# [3] Coletar configuração anonimizada (SEM senha)
# ============================================================
echo ""
echo "[3/6] Coletando configuração anonimizada..."

{
    echo "=== CONFIGURAÇÃO DO SISTEMA (ANONIMIZADA) ==="
    echo "Data de coleta: $(date '+%d/%m/%Y %H:%M:%S')"
    echo "NOTA: Senhas foram removidas por segurança."
    echo ""
} > "$TEMP_DIR/configuracao.txt"

if [ -f "config/configuracao_banco.json" ]; then
    if command -v python3 &>/dev/null; then
        python3 << 'PYEOF' >> "$TEMP_DIR/configuracao.txt" 2>/dev/null || true
import json

try:
    with open('config/configuracao_banco.json') as f:
        d = json.load(f)

    campus = d.get('campus', {})
    pg = d.get('postgresql', {})
    api = d.get('api', {})

    print("--- Campus ---")
    print(f"Nome    : {campus.get('nome', 'N/A')}")
    print(f"Sigla   : {campus.get('sigla', 'N/A')}")
    print(f"Cidade  : {campus.get('cidade', 'N/A')}")
    print(f"Estado  : {campus.get('estado', 'N/A')}")
    print("")
    print("--- Banco de Dados ---")
    print(f"Host    : {pg.get('host', 'N/A')}")
    print(f"Porta   : {pg.get('port', 'N/A')}")
    print(f"Banco   : {pg.get('database', 'N/A')}")
    print(f"Usuário : {pg.get('user', 'N/A')}")
    print("Senha   : [REMOVIDA]")
    print("")
    print("--- API ---")
    print(f"Porta   : {api.get('porta', 'N/A')}")
    print(f"Versão  : {api.get('versao', 'N/A')}")
    print("")
except Exception as e:
    print(f"Erro ao ler configuração: {e}")
PYEOF
    else
        # Fallback: extrair manualmente sem senha
        {
            echo "--- Banco de Dados (extração básica) ---"
            grep -E '"host"|"port"|"database"|"user"' config/configuracao_banco.json | grep -v '"password"' || true
            echo "Senha   : [REMOVIDA]"
            echo ""
        } >> "$TEMP_DIR/configuracao.txt"
    fi
    write_ok "Configuração anonimizada coletada (senha removida)."
else
    echo "Arquivo config/configuracao_banco.json não encontrado." >> "$TEMP_DIR/configuracao.txt"
    write_warn "Arquivo de configuração não encontrado."
fi

# ============================================================
# [4] Executar verificar-saude e copiar relatório
# ============================================================
echo ""
echo "[4/6] Executando verificação de saúde..."

SAUDE_SCRIPT="$SCRIPT_DIR/verificar-saude.sh"
if [ -f "$SAUDE_SCRIPT" ]; then
    # Solicitar senha do admin se não fornecida
    if [ -z "$SENHA_ADMIN" ]; then
        read -r -s -p "Senha do admin para verificação de saúde (Enter para pular): " SENHA_ADMIN
        echo ""
    fi

    if [ -n "$SENHA_ADMIN" ]; then
        bash "$SAUDE_SCRIPT" --senha-admin "$SENHA_ADMIN" > /dev/null 2>&1 || true
    fi

    # Copiar o relatório mais recente gerado
    RELATORIO_RECENTE=$(find "." -maxdepth 1 -name "relatorio-saude-*.txt" \
        -newer "$SAUDE_SCRIPT" 2>/dev/null | sort -r | head -1 || echo "")

    if [ -z "$RELATORIO_RECENTE" ]; then
        # Tentar qualquer relatório recente
        RELATORIO_RECENTE=$(find "." -maxdepth 1 -name "relatorio-saude-*.txt" \
            2>/dev/null | sort -r | head -1 || echo "")
    fi

    if [ -n "$RELATORIO_RECENTE" ]; then
        cp "$RELATORIO_RECENTE" "$TEMP_DIR/relatorio-saude.txt"
        write_ok "Relatório de saúde copiado: $(basename "$RELATORIO_RECENTE")"
    else
        echo "Relatório de saúde não disponível." > "$TEMP_DIR/relatorio-saude.txt"
        write_warn "Nenhum relatório de saúde encontrado."
    fi
else
    echo "Script verificar-saude.sh não encontrado." > "$TEMP_DIR/relatorio-saude.txt"
    write_warn "Script verificar-saude.sh não encontrado."
fi

# ============================================================
# [5] Coletar informações do sistema operacional
# ============================================================
echo ""
echo "[5/6] Coletando informações do sistema operacional..."

{
    echo "=== INFORMAÇÕES DO SISTEMA OPERACIONAL ==="
    echo "Data de coleta: $(date '+%d/%m/%Y %H:%M:%S')"
    echo ""

    echo "--- Sistema ---"
    uname -a 2>/dev/null || echo "uname não disponível"
    echo ""

    echo "--- Memória ---"
    free -h 2>/dev/null || vm_stat 2>/dev/null || echo "Informação de memória não disponível"
    echo ""

    echo "--- Disco ---"
    df -h 2>/dev/null || echo "Informação de disco não disponível"
    echo ""

    echo "--- Processos Java ---"
    ps aux 2>/dev/null | grep java | grep -v grep || echo "Nenhum processo Java encontrado"
    echo ""

    echo "--- Portas em uso ---"
    ss -tlnp 2>/dev/null | head -30 || netstat -tlnp 2>/dev/null | head -30 || echo "Informação de portas não disponível"
    echo ""
} > "$TEMP_DIR/sistema.txt"

write_ok "Informações do sistema coletadas."

# ============================================================
# [6] Compactar e limpar
# ============================================================
echo ""
echo "[6/6] Compactando diagnóstico..."

# Verificar se zip está disponível
if ! command -v zip &>/dev/null; then
    write_err "Comando 'zip' não encontrado."
    write_info "Instale com: sudo apt install zip  (Ubuntu/Debian)"
    write_info "             sudo dnf install zip  (Fedora/RHEL)"
    write_info "             brew install zip      (macOS)"
    write_info "Os arquivos estão disponíveis em: $TEMP_DIR/"
    exit 1
fi

if zip -r "$ZIP_NOME" "$TEMP_DIR/" 2>/dev/null; then
    TAMANHO=$(du -sh "$ZIP_NOME" 2>/dev/null | cut -f1 || echo "?")
    write_ok "Arquivo ZIP gerado: $ZIP_NOME ($TAMANHO)"
else
    write_err "Falha ao compactar diagnóstico."
    write_info "Os arquivos estão disponíveis em: $TEMP_DIR/"
    exit 1
fi

# Remover diretório temporário
rm -rf "$TEMP_DIR"
write_info "Diretório temporário removido."

# Resultado final
echo ""
echo -e "${GREEN}============================================================${NC}"
echo -e "${GREEN}  ✅ Diagnóstico coletado com sucesso!${NC}"
echo -e "${GREEN}============================================================${NC}"
echo ""
echo "  Arquivo: $(realpath "$ZIP_NOME" 2>/dev/null || echo "$ZIP_NOME")"
echo ""
echo -e "${CYAN}  Envie este arquivo para o suporte:${NC}"
echo -e "${CYAN}  E-mail    : suporte@sihcp.ifmt.edu.br${NC}"
echo -e "${CYAN}  Repositório: https://github.com/ifmt/sihcp${NC}"
echo ""
