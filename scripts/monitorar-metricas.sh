#!/bin/bash

# Script de Monitoramento de Métricas - Sistema de Inventário Mobile
# Monitora performance, uso de recursos e saúde do sistema

echo "═══════════════════════════════════════════════════════"
echo "MONITORAMENTO DE MÉTRICAS - INVENTÁRIO MOBILE"
echo "═══════════════════════════════════════════════════════"
echo ""

# Configurações
BASE_URL="${BASE_URL:-http://localhost:8080}"
DB_HOST="${DB_HOST:-localhost}"
DB_USER="${DB_USER:-inventario}"
DB_NAME="${DB_NAME:-sispatrimonio}"
INTERVAL="${INTERVAL:-60}" # segundos

# Cores
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# Função para obter timestamp
timestamp() {
    date "+%Y-%m-%d %H:%M:%S"
}

# Função para log
log_metric() {
    echo "[$(timestamp)] $1"
    echo "[$(timestamp)] $1" >> logs/metrics.log
}

# Criar diretório de logs se não existir
mkdir -p logs

echo "Iniciando monitoramento..."
echo "Intervalo: ${INTERVAL}s"
echo "Pressione Ctrl+C para parar"
echo ""

while true; do
    echo "═══════════════════════════════════════════════════════"
    echo "MÉTRICAS - $(timestamp)"
    echo "═══════════════════════════════════════════════════════"
    
    # 1. Saúde do Servidor
    echo ""
    echo "🏥 SAÚDE DO SERVIDOR"
    echo "---------------------------------------------------"
    
    SERVER_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}:%{time_total}" "$BASE_URL/api/mobile/inventario/ativo" 2>/dev/null)
    HTTP_CODE=$(echo $SERVER_RESPONSE | cut -d':' -f1)
    RESPONSE_TIME=$(echo $SERVER_RESPONSE | cut -d':' -f2)
    
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "401" ]; then
        echo -e "  Status: ${GREEN}✓ Online${NC}"
        echo "  HTTP Code: $HTTP_CODE"
        echo "  Tempo de Resposta: ${RESPONSE_TIME}s"
        log_metric "SERVER_STATUS=UP,HTTP_CODE=$HTTP_CODE,RESPONSE_TIME=$RESPONSE_TIME"
    else
        echo -e "  Status: ${RED}✗ Offline${NC}"
        echo "  HTTP Code: $HTTP_CODE"
        log_metric "SERVER_STATUS=DOWN,HTTP_CODE=$HTTP_CODE"
    fi
    
    # 2. Uso de CPU e Memória
    echo ""
    echo "💻 RECURSOS DO SISTEMA"
    echo "---------------------------------------------------"
    
    # CPU
    CPU_USAGE=$(top -bn1 | grep "Cpu(s)" | sed "s/.*, *\([0-9.]*\)%* id.*/\1/" | awk '{print 100 - $1}')
    echo "  CPU: ${CPU_USAGE}%"
    
    # Memória
    MEM_INFO=$(free -m | awk 'NR==2{printf "%.2f", $3*100/$2 }')
    echo "  Memória: ${MEM_INFO}%"
    
    # Disco
    DISK_USAGE=$(df -h / | awk 'NR==2{print $5}' | sed 's/%//')
    echo "  Disco: ${DISK_USAGE}%"
    
    log_metric "CPU=$CPU_USAGE,MEMORY=$MEM_INFO,DISK=$DISK_USAGE"
    
    # Alertas de recursos
    if (( $(echo "$CPU_USAGE > 80" | bc -l) )); then
        echo -e "  ${RED}⚠ ALERTA: CPU acima de 80%${NC}"
    fi
    
    if (( $(echo "$MEM_INFO > 80" | bc -l) )); then
        echo -e "  ${YELLOW}⚠ AVISO: Memória acima de 80%${NC}"
    fi
    
    # 3. Banco de Dados
    echo ""
    echo "🗄️  BANCO DE DADOS"
    echo "---------------------------------------------------"
    
    # Conexões ativas
    DB_CONNECTIONS=$(psql -h $DB_HOST -U $DB_USER -d $DB_NAME -t -c "SELECT count(*) FROM pg_stat_activity WHERE datname='$DB_NAME';" 2>/dev/null | tr -d ' ')
    echo "  Conexões Ativas: $DB_CONNECTIONS"
    
    # Tamanho do banco
    DB_SIZE=$(psql -h $DB_HOST -U $DB_USER -d $DB_NAME -t -c "SELECT pg_size_pretty(pg_database_size('$DB_NAME'));" 2>/dev/null | tr -d ' ')
    echo "  Tamanho: $DB_SIZE"
    
    # Total de patrimônios
    TOTAL_PATRIMONIOS=$(psql -h $DB_HOST -U $DB_USER -d $DB_NAME -t -c "SELECT COUNT(*) FROM patrimonio;" 2>/dev/null | tr -d ' ')
    echo "  Total Patrimônios: $TOTAL_PATRIMONIOS"
    
    # Total de coletas
    TOTAL_COLETAS=$(psql -h $DB_HOST -U $DB_USER -d $DB_NAME -t -c "SELECT COUNT(*) FROM coleta;" 2>/dev/null | tr -d ' ')
    echo "  Total Coletas: $TOTAL_COLETAS"
    
    # Coletas pendentes de sincronização
    COLETAS_PENDENTES=$(psql -h $DB_HOST -U $DB_USER -d $DB_NAME -t -c "SELECT COUNT(*) FROM coleta WHERE sincronizado = false;" 2>/dev/null | tr -d ' ')
    echo "  Coletas Pendentes: $COLETAS_PENDENTES"
    
    log_metric "DB_CONNECTIONS=$DB_CONNECTIONS,DB_SIZE=$DB_SIZE,PATRIMONIOS=$TOTAL_PATRIMONIOS,COLETAS=$TOTAL_COLETAS,PENDENTES=$COLETAS_PENDENTES"
    
    # 4. Processo Java
    echo ""
    echo "☕ PROCESSO JAVA"
    echo "---------------------------------------------------"
    
    JAVA_PID=$(pgrep -f "sistema-inventario")
    if [ -n "$JAVA_PID" ]; then
        echo "  PID: $JAVA_PID"
        
        # Memória do processo
        JAVA_MEM=$(ps -p $JAVA_PID -o rss= | awk '{printf "%.2f MB", $1/1024}')
        echo "  Memória: $JAVA_MEM"
        
        # Threads
        JAVA_THREADS=$(ps -p $JAVA_PID -o nlwp= | tr -d ' ')
        echo "  Threads: $JAVA_THREADS"
        
        # Tempo de execução
        JAVA_UPTIME=$(ps -p $JAVA_PID -o etime= | tr -d ' ')
        echo "  Uptime: $JAVA_UPTIME"
        
        log_metric "JAVA_PID=$JAVA_PID,JAVA_MEM=$JAVA_MEM,JAVA_THREADS=$JAVA_THREADS"
    else
        echo -e "  ${RED}✗ Processo não encontrado${NC}"
        log_metric "JAVA_STATUS=NOT_RUNNING"
    fi
    
    # 5. Logs de Erro
    echo ""
    echo "📋 LOGS RECENTES"
    echo "---------------------------------------------------"
    
    if [ -f "SISTEMA_INVENTARIO_PRODUCAO/logs/server.log" ]; then
        ERROR_COUNT=$(grep -c "ERROR" SISTEMA_INVENTARIO_PRODUCAO/logs/server.log 2>/dev/null || echo 0)
        WARN_COUNT=$(grep -c "WARN" SISTEMA_INVENTARIO_PRODUCAO/logs/server.log 2>/dev/null || echo 0)
        
        echo "  Erros (total): $ERROR_COUNT"
        echo "  Avisos (total): $WARN_COUNT"
        
        # Últimos erros
        RECENT_ERRORS=$(grep "ERROR" SISTEMA_INVENTARIO_PRODUCAO/logs/server.log | tail -n 3)
        if [ -n "$RECENT_ERRORS" ]; then
            echo ""
            echo "  Últimos erros:"
            echo "$RECENT_ERRORS" | sed 's/^/    /'
        fi
        
        log_metric "ERRORS=$ERROR_COUNT,WARNINGS=$WARN_COUNT"
    fi
    
    # 6. Resumo de Alertas
    echo ""
    echo "🚨 ALERTAS"
    echo "---------------------------------------------------"
    
    ALERTS=0
    
    if [ "$HTTP_CODE" != "200" ] && [ "$HTTP_CODE" != "401" ]; then
        echo -e "  ${RED}✗ Servidor não está respondendo${NC}"
        ((ALERTS++))
    fi
    
    if (( $(echo "$CPU_USAGE > 90" | bc -l) )); then
        echo -e "  ${RED}✗ CPU crítica (>90%)${NC}"
        ((ALERTS++))
    fi
    
    if (( $(echo "$MEM_INFO > 90" | bc -l) )); then
        echo -e "  ${RED}✗ Memória crítica (>90%)${NC}"
        ((ALERTS++))
    fi
    
    if [ -z "$JAVA_PID" ]; then
        echo -e "  ${RED}✗ Processo Java não está rodando${NC}"
        ((ALERTS++))
    fi
    
    if [ $ALERTS -eq 0 ]; then
        echo -e "  ${GREEN}✓ Nenhum alerta${NC}"
    else
        echo -e "  ${RED}Total de alertas: $ALERTS${NC}"
    fi
    
    log_metric "ALERTS=$ALERTS"
    
    echo ""
    echo "Próxima atualização em ${INTERVAL}s..."
    echo ""
    
    sleep $INTERVAL
done
