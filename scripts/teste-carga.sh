#!/bin/bash

# Script de Teste de Carga - Sistema de Inventário Mobile
# Testa endpoints críticos com múltiplas requisições simultâneas

echo "═══════════════════════════════════════════════════════"
echo "TESTE DE CARGA - SISTEMA DE INVENTÁRIO MOBILE"
echo "═══════════════════════════════════════════════════════"
echo ""

# Configurações
BASE_URL="${BASE_URL:-http://localhost:8080}"
NUM_REQUESTS="${NUM_REQUESTS:-100}"
CONCURRENT="${CONCURRENT:-10}"
USERNAME="${USERNAME:-admin}"
PASSWORD="${PASSWORD:-admin}"

echo "Configurações:"
echo "  Base URL: $BASE_URL"
echo "  Requisições: $NUM_REQUESTS"
echo "  Concorrentes: $CONCURRENT"
echo ""

# Função para fazer login e obter token
get_token() {
    echo "🔐 Fazendo login..."
    
    RESPONSE=$(curl -s -X POST "$BASE_URL/api/mobile/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\",\"deviceId\":\"test-device\",\"appVersion\":\"1.0\"}")
    
    TOKEN=$(echo $RESPONSE | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
    
    if [ -z "$TOKEN" ]; then
        echo "❌ Erro ao obter token"
        exit 1
    fi
    
    echo "✓ Token obtido com sucesso"
    echo ""
}

# Função para testar endpoint
test_endpoint() {
    local NAME=$1
    local METHOD=$2
    local ENDPOINT=$3
    local DATA=$4
    
    echo "📊 Testando: $NAME"
    echo "   Endpoint: $METHOD $ENDPOINT"
    echo "   Requisições: $NUM_REQUESTS (${CONCURRENT} concorrentes)"
    
    START_TIME=$(date +%s)
    
    # Criar arquivo temporário para resultados
    TEMP_FILE=$(mktemp)
    
    # Executar requisições
    for i in $(seq 1 $NUM_REQUESTS); do
        (
            if [ "$METHOD" = "GET" ]; then
                RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL$ENDPOINT" \
                    -H "Authorization: Bearer $TOKEN")
            else
                RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL$ENDPOINT" \
                    -H "Authorization: Bearer $TOKEN" \
                    -H "Content-Type: application/json" \
                    -d "$DATA")
            fi
            
            HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
            echo "$HTTP_CODE" >> $TEMP_FILE
        ) &
        
        # Controlar concorrência
        if [ $((i % CONCURRENT)) -eq 0 ]; then
            wait
        fi
    done
    
    wait
    
    END_TIME=$(date +%s)
    DURATION=$((END_TIME - START_TIME))
    
    # Analisar resultados
    TOTAL=$(wc -l < $TEMP_FILE)
    SUCCESS=$(grep -c "^200$" $TEMP_FILE || echo 0)
    ERRORS=$(grep -c "^[45]" $TEMP_FILE || echo 0)
    
    SUCCESS_RATE=$(awk "BEGIN {printf \"%.2f\", ($SUCCESS/$TOTAL)*100}")
    RPS=$(awk "BEGIN {printf \"%.2f\", $TOTAL/$DURATION}")
    
    echo "   ✓ Concluído em ${DURATION}s"
    echo "   📈 Taxa de sucesso: $SUCCESS_RATE% ($SUCCESS/$TOTAL)"
    echo "   ⚡ Requisições/segundo: $RPS"
    echo "   ❌ Erros: $ERRORS"
    echo ""
    
    rm $TEMP_FILE
}

# Executar testes
get_token

echo "═══════════════════════════════════════════════════════"
echo "INICIANDO TESTES DE CARGA"
echo "═══════════════════════════════════════════════════════"
echo ""

# Teste 1: Buscar Inventário Ativo
test_endpoint "Buscar Inventário Ativo" "GET" "/api/mobile/inventario/ativo"

# Teste 2: Listar Patrimônios
test_endpoint "Listar Patrimônios" "GET" "/api/mobile/patrimonio?page=0&size=50"

# Teste 3: Validar Patrimônio
test_endpoint "Validar Patrimônio" "GET" "/api/mobile/patrimonio/numero/12345/validar"

# Teste 4: Verificar Duplicata
test_endpoint "Verificar Duplicata" "POST" "/api/mobile/coletas/verificar-duplicata" \
    '{"numeroPatrimonio":"12345","inventarioId":2}'

# Teste 5: Buscar Estatísticas
test_endpoint "Buscar Estatísticas" "GET" "/api/mobile/inventario/2/estatisticas"

echo "═══════════════════════════════════════════════════════"
echo "TESTES CONCLUÍDOS"
echo "═══════════════════════════════════════════════════════"
