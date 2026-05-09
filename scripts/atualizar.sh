#!/bin/bash
# ============================================================
# SIHCP - Script de Atualização de Versão (Linux/macOS)
# Realiza backup, substitui JARs, aplica migrações SQL e
# reinicia o servidor com rollback automático em caso de falha.
# Uso: bash atualizar.sh [--usuario-pg USUARIO] [--senha-pg SENHA] [--update-dir DIR]
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

write_step() { echo -e "${CYAN}$1${NC}"; }
write_ok()   { echo -e "${GREEN}[OK] $1${NC}"; }
write_warn() { echo -e "${YELLOW}[AVISO] $1${NC}"; }
write_err()  { echo -e "${RED}[ERRO] $1${NC}"; }
write_info() { echo -e "${GRAY}      $1${NC}"; }

# Parâmetros opcionais
USUARIO_PG=""
SENHA_PG=""
UPDATE_DIR="update"

# Processar argumentos
while [[ $# -gt 0 ]]; do
    case "$1" in
        --usuario-pg) USUARIO_PG="$2"; shift 2 ;;
        --senha-pg)   SENHA_PG="$2";   shift 2 ;;
        --update-dir) UPDATE_DIR="$2"; shift 2 ;;
        *) shift ;;
    esac
done

clear
echo -e "${CYAN}============================================================${NC}"
echo -e "${CYAN}  SIHCP - Atualização de Versão${NC}"
echo -e "${CYAN}============================================================${NC}"
echo ""

# ============================================================
# Ler configurações
# ============================================================
if [ ! -f "config/configuracao_banco.json" ]; then
    write_err "Arquivo config/configuracao_banco.json não encontrado."
    write_info "Execute o setup primeiro: bash scripts/setup.sh"
    exit 1
fi

if command -v python3 &>/dev/null; then
    DB_HOST=$(python3 -c "import json; d=json.load(open('config/configuracao_banco.json')); print(d['postgresql']['host'])" 2>/dev/null || echo "localhost")
    DB_PORT=$(python3 -c "import json; d=json.load(open('config/configuracao_banco.json')); print(d['postgresql']['port'])" 2>/dev/null || echo "5432")
    BANCO=$(python3 -c "import json; d=json.load(open('config/configuracao_banco.json')); print(d['postgresql']['database'])" 2>/dev/null || echo "sispatrimonio")
    API_PORT=$(python3 -c "import json; d=json.load(open('config/configuracao_banco.json')); print(d['api']['porta'])" 2>/dev/null || echo "8080")
    if [ -z "$USUARIO_PG" ]; then
        USUARIO_PG=$(python3 -c "import json; d=json.load(open('config/configuracao_banco.json')); print(d['postgresql']['user'])" 2>/dev/null || echo "")
    fi
else
    DB_HOST="localhost"
    DB_PORT="5432"
    BANCO="sispatrimonio"
    API_PORT="8080"
fi

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
BANCO="${BANCO:-sispatrimonio}"
API_PORT="${API_PORT:-8080}"

# Solicitar credenciais se não fornecidas
if [ -z "$USUARIO_PG" ]; then
    read -r -p "Usuário do PostgreSQL: " USUARIO_PG
fi
if [ -z "$SENHA_PG" ]; then
    read -r -s -p "Senha do PostgreSQL: " SENHA_PG
    echo ""
fi

# Variáveis de controle
DATA_HORA=$(date +"%Y%m%d_%H%M%S")
BACKUP_PATH="backups/sispatrimonio_backup_${DATA_HORA}.backup"
VERSAO_ANTERIOR="N/A"
VERSAO_ATUAL="N/A"
MIGRACOES_APLICADAS=0

# ============================================================
# [1/6] Backup do banco de dados
# ============================================================
write_step "[1/6] Criando backup do banco de dados..."

mkdir -p backups

export PGPASSWORD="$SENHA_PG"
if pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$USUARIO_PG" \
    -F c -f "$BACKUP_PATH" "$BANCO" 2>&1; then
    TAMANHO=$(du -sh "$BACKUP_PATH" 2>/dev/null | cut -f1 || echo "?")
    write_ok "Backup criado: $BACKUP_PATH ($TAMANHO)"
else
    write_err "Falha ao criar backup. Atualização cancelada."
    write_info "Nenhuma alteração foi aplicada."
    unset PGPASSWORD
    exit 1
fi

# Obter versão anterior do schema
VERSAO_ANTERIOR=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$USUARIO_PG" -d "$BANCO" \
    -t -c "SELECT versao FROM schema_version ORDER BY id DESC LIMIT 1" 2>/dev/null | tr -d ' ' || echo "N/A")
VERSAO_ANTERIOR="${VERSAO_ANTERIOR:-N/A}"
unset PGPASSWORD

write_info "Versão atual do schema: $VERSAO_ANTERIOR"
echo ""

# ============================================================
# [2/6] Parar o servidor
# ============================================================
write_step "[2/6] Parando o servidor..."

# Encontrar PID do processo Java com mobile-server.jar
JAVA_PIDS=$(pgrep -f "mobile-server\.jar" 2>/dev/null || echo "")

if [ -n "$JAVA_PIDS" ]; then
    for PID in $JAVA_PIDS; do
        if kill "$PID" 2>/dev/null; then
            write_ok "Processo Java (PID $PID) encerrado."
        else
            write_warn "Não foi possível encerrar o processo PID $PID."
        fi
    done
    sleep 3
else
    write_info "Nenhum processo do servidor encontrado em execução."
fi
echo ""

# ============================================================
# [3/6] Substituir JARs
# ============================================================
write_step "[3/6] Substituindo JARs em bin/..."

if [ ! -d "$UPDATE_DIR" ]; then
    write_warn "Diretório '$UPDATE_DIR' não encontrado. Pulando substituição de JARs."
    write_info "Coloque os novos JARs em '$UPDATE_DIR/' antes de executar este script."
else
    JARS_NOVOS=$(find "$UPDATE_DIR" -name "*.jar" 2>/dev/null || echo "")
    if [ -z "$JARS_NOVOS" ]; then
        write_warn "Nenhum JAR encontrado em '$UPDATE_DIR'. Pulando substituição."
    else
        mkdir -p bin
        while IFS= read -r jar; do
            NOME=$(basename "$jar")
            cp -f "$jar" "bin/$NOME"
            write_ok "JAR atualizado: bin/$NOME"
        done <<< "$JARS_NOVOS"
    fi
fi
echo ""

# ============================================================
# [4/6] Aplicar scripts SQL de migração
# ============================================================
write_step "[4/6] Aplicando scripts SQL de migração..."

export PGPASSWORD="$SENHA_PG"

# Obter versões já aplicadas
VERSOES_APLICADAS=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$USUARIO_PG" -d "$BANCO" \
    -t -c "SELECT versao FROM schema_version ORDER BY id" 2>/dev/null | tr -d ' ' || echo "")

# Função de rollback
fazer_rollback() {
    write_err "Iniciando rollback via pg_restore..."
    export PGPASSWORD="$SENHA_PG"
    if pg_restore -h "$DB_HOST" -p "$DB_PORT" -U "$USUARIO_PG" \
        -d "$BANCO" -c "$BACKUP_PATH" 2>&1; then
        write_ok "Rollback concluído. Banco restaurado para o estado anterior."
    else
        write_err "Falha no rollback."
        write_info "Restaure manualmente: pg_restore -h $DB_HOST -p $DB_PORT -U $USUARIO_PG -d $BANCO -c $BACKUP_PATH"
    fi
    unset PGPASSWORD
}

# Procurar scripts de migração em sql/migrations/
MIGRATION_DIR="sql/migrations"
if [ -d "$MIGRATION_DIR" ]; then
    # Ordenar scripts por nome
    while IFS= read -r script; do
        NOME=$(basename "$script")
        # Extrair versão do nome do arquivo (ex: V1.1.0__descricao.sql → 1.1.0)
        VERSAO_SCRIPT=$(echo "$NOME" | sed 's/^V\([^_]*\)__.*/\1/')

        # Verificar se já foi aplicada
        if echo "$VERSOES_APLICADAS" | grep -qx "$VERSAO_SCRIPT"; then
            write_info "Migração $VERSAO_SCRIPT já aplicada. Pulando."
            continue
        fi

        write_info "Aplicando migração: $NOME..."
        if psql -h "$DB_HOST" -p "$DB_PORT" -U "$USUARIO_PG" -d "$BANCO" \
            -f "$script" 2>&1; then
            MIGRACOES_APLICADAS=$((MIGRACOES_APLICADAS + 1))
            VERSAO_ATUAL="$VERSAO_SCRIPT"
            write_ok "Migração $VERSAO_SCRIPT aplicada."
        else
            write_err "Falha ao aplicar migração: $NOME"
            fazer_rollback
            unset PGPASSWORD
            exit 1
        fi
    done < <(find "$MIGRATION_DIR" -name "*.sql" | sort)
else
    write_info "Diretório '$MIGRATION_DIR' não encontrado. Nenhuma migração aplicada."
fi

if [ "$MIGRACOES_APLICADAS" -eq 0 ]; then
    write_info "Nenhuma migração nova para aplicar."
fi

unset PGPASSWORD
echo ""

# ============================================================
# [5/6] Reiniciar o servidor
# ============================================================
write_step "[5/6] Reiniciando o servidor..."

if [ -f "bin/mobile-server.jar" ]; then
    nohup java -Xms256m -Xmx1g \
        -Dinventario.config.mode=APP_DIR \
        -jar bin/mobile-server.jar \
        --spring.profiles.active=mobile,prod \
        --spring.config.additional-location=config/ \
        > logs/servidor.log 2>&1 &
    SERVIDOR_PID=$!
    sleep 5
    if kill -0 "$SERVIDOR_PID" 2>/dev/null; then
        write_ok "Servidor reiniciado em segundo plano (PID $SERVIDOR_PID)."
    else
        write_warn "Servidor pode não ter iniciado corretamente. Verifique logs/servidor.log"
    fi
else
    write_warn "bin/mobile-server.jar não encontrado. Inicie o servidor manualmente."
    write_info "  bash scripts/iniciar-servidor.sh"
fi
echo ""

# ============================================================
# [6/6] Exibir resumo
# ============================================================
write_step "[6/6] Resumo da atualização..."

# Obter versão atual do schema
export PGPASSWORD="$SENHA_PG"
VERSAO_ATUAL_SCHEMA=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$USUARIO_PG" -d "$BANCO" \
    -t -c "SELECT versao FROM schema_version ORDER BY id DESC LIMIT 1" 2>/dev/null | tr -d ' ' || echo "")
if [ -n "$VERSAO_ATUAL_SCHEMA" ]; then VERSAO_ATUAL="$VERSAO_ATUAL_SCHEMA"; fi
unset PGPASSWORD

echo ""
echo -e "${GREEN}============================================================${NC}"
echo -e "${GREEN}  ✅ Atualização concluída!${NC}"
echo -e "${GREEN}============================================================${NC}"
echo ""
echo "  Versão anterior : $VERSAO_ANTERIOR"
echo "  Versão atual    : $VERSAO_ATUAL"
echo "  Migrações SQL   : $MIGRACOES_APLICADAS aplicada(s)"
echo "  Backup          : $BACKUP_PATH"
echo ""
echo -e "${CYAN}  Execute a verificação de saúde: bash scripts/verificar-saude.sh${NC}"
echo ""
